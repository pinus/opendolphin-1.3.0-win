package open.dolphin.inspector;

import open.dolphin.calendar.CalendarPanel;
import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.event.ProxyAction;
import open.dolphin.event.ProxyDocumentListener;
import open.dolphin.helper.ImageHelper;
import open.dolphin.helper.TextComponentUndoManager;
import open.dolphin.infomodel.PhysicalModel;
import open.dolphin.infomodel.SimpleDate;
import open.dolphin.ui.Focuser;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.*;

/**
 * 身長体重データを編集するエディタクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class PhysicalEditor {

    private final PhysicalInspector inspector;
    private static PhysicalEditor editor;
    private PhysicalModel model;
    private final JDialog dialog;
    private final JButton addBtn;
    private final JButton clearBtn;
    private javax.swing.JTextField heightFld;
    private javax.swing.JTextField identifiedFld;
    private javax.swing.JTextField weightFld;
    private boolean ok;

    public PhysicalEditor(PhysicalInspector inspector) {
        this.inspector = inspector;

        // モデルの準備
        model = new PhysicalModel();
        if (inspector.getSelectedModel() != null) {
            PhysicalModel src = inspector.getSelectedModel();
            model.setWeight(src.getWeight());
            model.setWeightId(src.getWeightId());
            model.setHeight(src.getHeight());
            model.setHeightId(src.getHeightId());
            model.setMemo(src.getMemo());
            model.setIdentifiedDate(src.getIdentifiedDate());
        }

        // init components
        JLabel weightLbl = new JLabel("体重：");
        JLabel heightLbl = new JLabel("身長：");
        JLabel dateLbl = new JLabel("測定日：");

        weightFld = new JTextField(5);
        heightFld = new JTextField(5);
        identifiedFld = new JTextField(10);

        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.X_AXIS));
        view.add(weightLbl); view.add(weightFld); view.add(Box.createHorizontalStrut(20));
        view.add(heightLbl); view.add(heightFld); view.add(Box.createHorizontalStrut(20));
        view.add(dateLbl); view.add(identifiedFld);

        addBtn = new JButton("追加");
        addBtn.addActionListener(e -> add());
        addBtn.setEnabled(false);

        clearBtn = new JButton("クリア");
        clearBtn.addActionListener(e -> clear());
        clearBtn.setEnabled(false);

        Object[] options = new Object[]{addBtn, clearBtn};

        JOptionPane pane = new JOptionPane(view,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            options, addBtn);
        dialog = pane.createDialog(inspector.getContext().getFrame(), ClientContext.getFrameTitle("身長体重登録"));
        dialog.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
        if (ClientContext.isMac()) { ImageHelper.setContainerTransparent(dialog); }
        dialog.setIconImage(GUIConst.ICON_DOLPHIN.getImage());

        // dialog が開いたら WeightFld にフォーカスを当てる
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                Focuser.requestFocus(weightFld);
            }
        });

        // command-w でウインドウクローズ
        InputMap im = dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = KeyStroke.getKeyStroke("meta W");
        im.put(key, "close-window");
        dialog.getRootPane().getActionMap().put("close-window", new ProxyAction(dialog::dispose));

        // model to view
        heightFld.setText(model.getHeight());
        weightFld.setText(model.getWeight());
        if (StringUtils.isEmpty(model.getIdentifiedDate())) {
            model.setIdentifiedDate(inspector.today());
        }
        identifiedFld.setText(model.getIdentifiedDate());

        // connect
        heightFld.getDocument().addDocumentListener((ProxyDocumentListener) e -> checkBtn());
        heightFld.getDocument().addUndoableEditListener(TextComponentUndoManager.createManager(heightFld));
        weightFld.getDocument().addDocumentListener((ProxyDocumentListener) e -> checkBtn());
        weightFld.getDocument().addUndoableEditListener(TextComponentUndoManager.createManager(weightFld));
        identifiedFld.getDocument().addDocumentListener((ProxyDocumentListener) e -> checkBtn());
        identifiedFld.getDocument().addUndoableEditListener(TextComponentUndoManager.createManager(identifiedFld));
        identifiedFld.addMouseListener(new PopupListener());
        identifiedFld.putClientProperty("Quaqua.TextComponent.showPopup", false);

        // show dialog
        dialog.setVisible(true);
    }

    public static void show(PhysicalInspector inspector) {
        editor = new PhysicalEditor(inspector);
    }

    /**
     * Button enable/disable.
     */
    private void checkBtn() {
        String height = heightFld.getText().trim();
        String weight = weightFld.getText().trim();
        String dateStr = identifiedFld.getText().trim();

        boolean newOk = !(height.equals("") && weight.equals("")) && !dateStr.equals("");

        if (ok != newOk) {
            ok = newOk;
            addBtn.setEnabled(ok);
            clearBtn.setEnabled(ok);
        }
    }

    /**
     * Add model to inspector.
     */
    private void add() {
        // vie to model
        String h = heightFld.getText().trim();
        String w = weightFld.getText().trim();

        if (!h.equals("")) { model.setHeight(h); }
        if (!w.equals("")) { model.setWeight(w); }

        // 同定日
        String dateStr = identifiedFld.getText().trim();
        if (dateStr.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")) {
            model.setIdentifiedDate(dateStr);
        } else {
            model.setIdentifiedDate(inspector.today());
        }

        inspector.add(model);

        // renewal for next edit
        model = new PhysicalModel();
        clear();
    }

    private void clear() {
        addBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        heightFld.setText("");
        weightFld.setText("");
        identifiedFld.setText(inspector.today());
        model.setIdentifiedDate(inspector.today());
    }

    private class PopupListener extends MouseAdapter {

        private JPopupMenu popup;

        public PopupListener() {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // Windows
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {

                popup = new JPopupMenu();
                CalendarPanel cp = new CalendarPanel();
                cp.getTable().addCalendarListener(date -> {
                    identifiedFld.setText(SimpleDate.simpleDateToMmldate(date));
                    popup.setVisible(false);
                    popup = null;
                });

                popup.insert(cp, 0);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
