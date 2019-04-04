package open.dolphin.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * PNSProgressMonitor
 * ProgressMonitor が，エスケープキーを押してキャンセルした場合 isCanceled() が true にならないのを workaround
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6357974
 *
 * @author pns
 */
public class PNSProgressMonitor extends ProgressMonitor {

    private boolean isCanceled = false;

    public PNSProgressMonitor(Component parentComponent, Object message, String note, int min, int max) {
        super(parentComponent, message, note, min, max);
    }

    @Override
    public void setProgress(int nv) {
        super.setProgress(nv);
        JDialog d = (JDialog) this.getAccessibleContext().getAccessibleParent();

        if (d != null) {
            ActionMap am = d.getRootPane().getActionMap();
            InputMap im = d.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

            // ESC でキャンセル
            im.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
            am.put("cancel", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //System.out.println("-----escape----");
                    isCanceled = true;
                }
            });
        }
    }

    @Override
    public boolean isCanceled() {
        return super.isCanceled() || isCanceled;
    }
}
