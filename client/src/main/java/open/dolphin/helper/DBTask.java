package open.dolphin.helper;

import open.dolphin.client.Chart;
import open.dolphin.client.ClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * DBTask.
 *
 * @param <T> object to return
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public abstract class DBTask<T> extends SwingWorker<T, Void> implements PropertyChangeListener {

    private static long TIMEOUT = 60 * 1000L;

    private Chart context;
    private Logger logger;

    protected DBTask() {
    }

    public DBTask(Chart context) {
        this.context = context;
        logger = LoggerFactory.getLogger(DBTask.class);
        connect();
    }

    private void connect() {
        addPropertyChangeListener(this);
    }

    @Override
    protected abstract T doInBackground() throws Exception;

    protected void succeeded(T result) {
    }

    protected void cancelled() {
        logger.debug("DBTask cancelled");
    }

    protected void timeout() {
        JFrame parent = context.getFrame();
        String title = "DBタスク";
        String message = "データベースアクセスにタイムアウトが生じました。\n" +
            "リトライをお願いします。";
        JOptionPane.showMessageDialog(
                parent,
                message,
                ClientContext.getFrameTitle(title),
                JOptionPane.WARNING_MESSAGE);
        logger.debug("DBTask timeout");
    }

    protected void failed(Throwable cause) {
        logger.warn("DBTask failed");
        cause.printStackTrace(System.err);
    }

    private void startProgress() {
        context.getDocumentHistory().blockHistoryTable(true);
        //progressBar.setIndeterminate(true);
    }

    private void stopProgress() {
        //progressBar.setIndeterminate(false);
        //progressBar.setValue(0);
        context.getDocumentHistory().blockHistoryTable(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {

        if ("state".equals(e.getPropertyName())) {
            switch ((StateValue) e.getNewValue()) {
                case STARTED:
                    startProgress();
                    break;

                case DONE:
                    stopProgress();
                    break;
            }
        }
    }

    @Override
    protected void done() {

        if (isCancelled()) {
            cancelled();

        } else {
            try {
                succeeded(get(TIMEOUT, TimeUnit.SECONDS));

            } catch (InterruptedException ex) {
                cancelled();
            } catch (ExecutionException | RuntimeException ex) {
                failed(ex);
            } catch (TimeoutException ex) {
                timeout();
            }
        }
    }
}
