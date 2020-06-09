package open.dolphin.impl.care;

import open.dolphin.infomodel.AppointmentModel;
import open.dolphin.dnd.DolphinTransferHandler;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * AppointLabel.
 * Draggable label.
 *
 * @author Kauzshi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class AppointLabel extends JLabel {
    private static final long serialVersionUID = 2843710174202998473L;

    public AppointLabel(String text, Icon icon, int align) {
        super(text, icon, align);
        init();
    }

    private void init() {
        AppointTransferHandler th = new AppointTransferHandler();
        setTransferHandler(th);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                th.exportAsDrag(AppointLabel.this, e, AppointTransferHandler.COPY);
            }
        });
    }

    private class AppointTransferHandler extends DolphinTransferHandler {
        private static final long serialVersionUID = 1L;

        public AppointTransferHandler() {
            super();
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JLabel label = (JLabel) c;
            AppointmentModel appo = new AppointmentModel();
            appo.setName(label.getText());
            return new AppointEntryTransferable(appo);
        }

        @Override
        public int getSourceActions(JComponent c) {
            Icon icon = ((JLabel) c).getIcon();
            JLabel label = new JLabel(icon);
            label.setSize(icon.getIconWidth(), icon.getIconHeight());

            setDragImage(label);

            return COPY_OR_MOVE;
        }
    }
}
