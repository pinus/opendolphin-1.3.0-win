package open.dolphin.inspector;

import open.dolphin.calendar.CalendarPanel;
import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.event.ProxyAction;
import open.dolphin.event.ProxyDocumentListener;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PhysicalModel;
import open.dolphin.infomodel.SimpleDate;
import open.dolphin.ui.Focuser;

import javax.swing.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 身長体重データを編集するエディタクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class PhysicalEditor {

    private final PhysicalInspector inspector;
    private final JDialog dialog;
    private final JButton addBtn;
    private final JButton clearBtn;
    private PhysicalEditorView view;
    private boolean ok;

    public PhysicalEditor(PhysicalInspector inspector) {
        this.inspector = inspector;

        view = new PhysicalEditorView();

        ProxyDocumentListener dl = e -> checkBtn();

        view.getHeightFld().getDocument().addDocumentListener(dl);
        view.getWeightFld().getDocument().addDocumentListener(dl);
        view.getIdentifiedDateFld().getDocument().addDocumentListener(dl);

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.DATE_WITHOUT_TIME);
        String todayString = sdf.format(date);
        view.getIdentifiedDateFld().setText(todayString);
        view.getIdentifiedDateFld().addMouseListener(new PopupListener());

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
        dialog.setIconImage(GUIConst.ICON_DOLPHIN.getImage());

        // dialog が開いたら WeightFld にフォーカスを当てる
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                Focuser.requestFocus(view.getWeightFld());
            }
        });

        // command-w でウインドウクローズ
        InputMap im = dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = KeyStroke.getKeyStroke("meta W");
        im.put(key, "close-window");
        dialog.getRootPane().getActionMap().put("close-window", new ProxyAction(dialog::dispose));

        dialog.setVisible(true);
    }

    public static void show(PhysicalInspector ins) {
        new PhysicalEditor(ins);
    }

    private void checkBtn() {

        String height = view.getHeightFld().getText().trim();
        String weight = view.getWeightFld().getText().trim();
        String dateStr = view.getIdentifiedDateFld().getText().trim();

        boolean newOk = !(height.equals("") && weight.equals("")) && !dateStr.equals("");

        if (ok != newOk) {
            ok = newOk;
            addBtn.setEnabled(ok);
            clearBtn.setEnabled(ok);
        }
    }

    private void add() {

        String h = view.getHeightFld().getText().trim();
        String w = view.getWeightFld().getText().trim();
        final PhysicalModel model = new PhysicalModel();

        if (!h.equals("")) {
            model.setHeight(h);
        }
        if (!w.equals("")) {
            model.setWeight(w);
        }

        // 同定日
        String confirmedStr = view.getIdentifiedDateFld().getText().trim();
        model.setIdentifiedDate(confirmedStr);

        addBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        inspector.add(model);
    }

    private void clear() {
        view.getHeightFld().setText("");
        view.getWeightFld().setText("");
        view.getIdentifiedDateFld().setText("");
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
                    view.getIdentifiedDateFld().setText(SimpleDate.simpleDateToMmldate(date));
                    popup.setVisible(false);
                    popup = null;
                });

                popup.insert(cp, 0);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
