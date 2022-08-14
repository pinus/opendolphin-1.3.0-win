package open.dolphin.delegater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.lang.reflect.ParameterizedType;
import java.util.concurrent.Callable;

/**
 * BusinessDelegater.
 *
 * @author pns
 */
public class BusinessDelegater<T> {
    private static int RETRY = 3;

    protected Logger logger;
    private Result errorCode = Result.NO_ERROR;
    private String errorMessage = "delegate error";
    // 呼び出し元の class
    private Class<T> target;

    public BusinessDelegater() {
        // 呼び出し元の class を取得
        String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
        try {
            target = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            processError(e);
        }
        logger = LoggerFactory.getLogger(target);
    }

    public T getService() {
        try {
            return DolphinClientContext.getContext().getWebTarget().proxy(target);
        } catch (Exception e) {
            processError(e);
        }
        return null;
    }

    public <K> K submit(Callable<K> callable) {
        int retry = RETRY;
        K result = null;

        while (retry > 0) {
            try {
                result = callable.call();
                retry = 0;

            } catch (Exception e) {
                e.printStackTrace(System.err);
                if (retry > 1) {
                    retry --;
                    logger.info("retry: " + (RETRY - retry));

                } else {
                    int ans = JOptionPane.showConfirmDialog(
                        null, "エラーが発生しました。再送しますか？", e.getMessage(),
                        JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE
                    );
                    if (ans == JOptionPane.YES_OPTION) {
                        retry = RETRY;
                    } else {
                        processError(e);
                        retry = 0;
                    }
                }
            }
        }
        return result;
    }

    public Result getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Result errCode) {
        this.errorCode = errCode;
    }

    public boolean isNoError() {
        return errorCode.equals(Result.NO_ERROR);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMesseage) {
        this.errorMessage = errorMesseage;
    }

    public void processError(Exception e) {
        setErrorMessage(e.toString());
        setErrorCode(Result.ERROR);
        e.printStackTrace(System.err);
    }

    public enum Result {NO_ERROR, ERROR}
}
