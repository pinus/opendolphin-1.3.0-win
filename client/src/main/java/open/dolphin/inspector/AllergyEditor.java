package open.dolphin.inspector;

import open.dolphin.calendar.CalendarPanel;
import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.event.ProxyAction;
import open.dolphin.event.ProxyDocumentListener;
import open.dolphin.helper.ImageHelper;
import open.dolphin.helper.TextComponentUndoManager;
import open.dolphin.infomodel.AllergyModel;
import open.dolphin.infomodel.SimpleDate;
import open.dolphin.ui.Focuser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;

/**
 * アレルギデータを編集するエディタクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class AllergyEditor {
    private Logger logger = LoggerFactory.getLogger(AllergyEditor.class);

    private final AllergyInspector inspector;
    private static AllergyEditor editor;
    private AllergyModel model;
    private final JDialog dialog;
    private final JButton addBtn;
    private final JButton clearBtn;
    private JTextField factorFld;
    private JTextField identifiedFld;
    private JTextField memoFld;
    private JComboBox<String> reactionCombo;
    private boolean ok;

    public AllergyEditor(AllergyInspector inspector) {
        this.inspector = inspector;

        // モデルの準備
        model = new AllergyModel();
        if (inspector.getSelectedModel() != null) {
            // コピー
            AllergyModel src = inspector.getSelectedModel();
            model.setFactor(src.getFactor());
            model.setIdentifiedDate(src.getIdentifiedDate());
            model.setMemo(src.getMemo());
            model.setSeverity(src.getSeverity());
            model.setSeverityTableId(src.getSeverityTableId());
        }

        // init components
        JLabel causeLbl = new JLabel("要因：");
        JLabel levelLbl = new JLabel("反応：");
        JLabel memoLbl = new JLabel("メモ：");
        JLabel dateLbl = new JLabel("同定日：");

        factorFld = new JTextField(30);
        identifiedFld = new JTextField();
        memoFld = new JTextField(30);
        reactionCombo = new JComboBox<>();
        reactionCombo.setModel(new DefaultComboBoxModel<>(new String[]{"severe", "moderate", "mild", "none"}));

        JPanel causePanel = new JPanel();
        causePanel.setLayout(new BoxLayout(causePanel, BoxLayout.X_AXIS));
        causePanel.add(causeLbl); causePanel.add(factorFld);

        JPanel datePanel = new JPanel();
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.X_AXIS));
        datePanel.add(levelLbl); datePanel.add(reactionCombo); datePanel.add(Box.createHorizontalStrut(70));
        datePanel.add(dateLbl);datePanel.add(identifiedFld);

        JPanel memoPanel = new JPanel();
        memoPanel.setLayout(new BoxLayout(memoPanel, BoxLayout.X_AXIS));
        memoPanel.add(memoLbl); memoPanel.add(memoFld);

        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.add(causePanel); view.add(memoPanel); view.add(datePanel);

        // compose dialog
        addBtn = new JButton("OK");
        addBtn.addActionListener(e -> add());
        addBtn.setEnabled(false);

        clearBtn = new JButton("クリア");
        clearBtn.addActionListener(e -> clear());
        clearBtn.setEnabled(false);

        JOptionPane pane = new JOptionPane(view,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            new Object[]{addBtn, clearBtn, "キャンセル"}, addBtn);
        dialog = pane.createDialog(inspector.getContext().getFrame(), "アレルギー登録");
        dialog.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
        if (ClientContext.isMac()) { ImageHelper.setContainerTransparent(dialog); }
        dialog.setIconImage(GUIConst.ICON_DOLPHIN.getImage());

        // dialog が開いたら FactorFld にフォーカスを当てる
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                Focuser.requestFocus(factorFld);
            }
        });

        // command-w でウインドウクローズ
        InputMap im = dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(inspector.META_W, "close-window");
        dialog.getRootPane().getActionMap().put("close-window", new ProxyAction(dialog::dispose));

        // model to view
        factorFld.setText(model.getFactor());
        memoFld.setText(model.getMemo());
        if (StringUtils.isEmpty(model.getSeverity())) {
            model.setSeverity(reactionCombo.getItemAt(0));
        }
        reactionCombo.setSelectedItem(model.getSeverity());
        if (StringUtils.isEmpty(model.getIdentifiedDate())) {
            model.setIdentifiedDate(inspector.today());
        }
        identifiedFld.setText(model.getIdentifiedDate());

        // connect
        factorFld.getDocument().addDocumentListener((ProxyDocumentListener) e -> checkBtn());
        factorFld.getDocument().addUndoableEditListener(TextComponentUndoManager.createManager(factorFld));

        identifiedFld.addMouseListener(new PopupListener());
        identifiedFld.getDocument().addDocumentListener((ProxyDocumentListener) e -> checkBtn());
        identifiedFld.getDocument().addUndoableEditListener(TextComponentUndoManager.createManager(identifiedFld));
        identifiedFld.putClientProperty("Quaqua.TextComponent.showPopup", false);

        memoFld.getDocument().addDocumentListener((ProxyDocumentListener) e -> checkBtn());
        memoFld.getDocument().addUndoableEditListener(TextComponentUndoManager.createManager(memoFld));

        // show dialogs
        dialog.setVisible(true);
    }

    // デザインパターン
    public static void show(AllergyInspector inspector) {
        editor = new AllergyEditor(inspector);
    }

    /**
     * Button enable/disable.
     */
    private void checkBtn() {
        String factor = factorFld.getText().trim();
        String date = identifiedFld.getText().trim();

        boolean newOk = !factor.equals("") && !date.equals("");

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
        // view to model
        model.setFactor(factorFld.getText().trim());
        model.setSeverity((String) reactionCombo.getSelectedItem());
        String memo = memoFld.getText().trim();
        if (!memo.equals("")) {
            model.setMemo(memo);
        }
        String dateStr = identifiedFld.getText().trim();
        if (dateStr.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")) {
            model.setIdentifiedDate(dateStr);
        } else {
            model.setIdentifiedDate(inspector.today());
        }

        inspector.add(model);

        // renewal for next edit
        model = new AllergyModel();
        clear();
    }

    private void clear() {
        addBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        factorFld.setText("");
        memoFld.setText("");
        identifiedFld.setText(inspector.today());
        model.setIdentifiedDate(inspector.today());
    }

    private class PopupListener extends MouseAdapter {

        private JPopupMenu popup;

        public PopupListener() { }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // windows
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
