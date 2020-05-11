package open.dolphin.client;

import open.dolphin.event.ProxyAction;
import open.dolphin.event.ProxyDocumentListener;
import open.dolphin.ui.sheet.JSheet;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * カルテ保存時の SaveDialog.
 *
 * @author pns
 */
public class SaveDialog {
    // result code
    public static final int SAVE = 0;
    public static final int TMP_SAVE = 1;
    public static final int DISPOSE = 2;
    public static final int CANCEL = 3;
    private static final String[] PRINT_COUNT = {"0", "1", "2", "3", "4", "5"};
    private static final String[] TITLE_LIST = {"経過記録", "処方", "処置", "検査", "画像", "指導"};
    private static final String DIALOG_TITLE = "ドキュメント保存";
    private final Window parent;
    // 戻り値のSaveParams
    private final SaveParams value = new SaveParams();
    private JOptionPane pane;
    private JSheet dialog;
    private JTextField titleField;
    private JComboBox<String> titleCombo;
    //private JComboBox<String> printCombo;
    private JLabel departmentLabel;
    private JCheckBox sendClaim;
    private JButton okButton;
    private JButton tmpButton;
    private JButton disposeButton;
    private JButton cancelButton;

    public SaveDialog(Window parent) {
        this.parent = parent;
        initComponent();
    }

    /**
     * GUIコンポーネントを初期化する.
     */
    private void initComponent() {

        // content
        JPanel content = new JPanel();
        content.setLayout(new GridLayout(0, 1));

        // 文書Title
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleCombo = new JComboBox<>(TITLE_LIST);
        titleCombo.setPreferredSize(new Dimension(220, titleCombo.getPreferredSize().height));
        titleCombo.setMaximumSize(titleCombo.getPreferredSize());
        titleCombo.setEditable(true);
        p1.add(new JLabel("タイトル:"));
        p1.add(titleCombo);
        content.add(p1);

        // ComboBox のエディタコンポーネントへリスナを設定する
        titleField = (JTextField) titleCombo.getEditor().getEditorComponent();
        titleField.getDocument().addDocumentListener((ProxyDocumentListener) e -> checkTitle());

        // 診療科，印刷部数を表示するラベルとパネルを生成する
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        departmentLabel = new JLabel();
        p2.add(new JLabel("診療科:"));
        p2.add(departmentLabel);
        p2.add(Box.createRigidArea(new Dimension(11, 0)));

        // Print
        //printCombo = new JComboBox<>(PRINT_COUNT);
        //printCombo.setSelectedIndex(1);
        //p2.add(new JLabel("印刷部数:"));
        //p2.add(printCombo);
        //content.add(p2);

        // CLAIM 送信ありなし
        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sendClaim = new JCheckBox("診療行為を送信する (仮保存の場合は送信しない)");
        p3.add(sendClaim);
        content.add(p3);

        okButton = new JButton(new ProxyAction("保 存", this::doOk));
        okButton.setEnabled(false);
        okButton.setToolTipText("Return");

        tmpButton = new JButton(new ProxyAction("仮保存", this::doTemp));
        tmpButton.setEnabled(false);
        tmpButton.setToolTipText("<html>&#8984;T</html>");

        disposeButton = new JButton(new ProxyAction("破 棄", this::doDispose));
        disposeButton.setToolTipText("<html>&#8984;ESC</html>");

        cancelButton = new JButton(new ProxyAction("キャンセル", this::doCancel));
        cancelButton.setToolTipText("ESC");

        // JOptionPane
        pane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                new JButton[]{okButton, tmpButton, disposeButton, cancelButton}, okButton);

        dialog = JSheet.createDialog(pane, parent);
        dialog.addSheetListener(se -> {
            // Escape を押したときだけここに入る
            if (se.getOption() == JOptionPane.CLOSED_OPTION) {
                doCancel();
            }
        });

        // ショートカット登録
        ActionMap am = dialog.getRootPane().getActionMap();
        InputMap im = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        // SPACE で CLAIM 送信のチェックボックスの ON/OFF をする
        im.put(KeyStroke.getKeyStroke("SPACE"), "toggle-claim");
        am.put("toggle-claim", new ProxyAction(sendClaim::doClick));

        // Cmd-T で一時保存
        im.put(KeyStroke.getKeyStroke("meta T"), "tmpSave");
        am.put("tmpSave", new ProxyAction(tmpButton::doClick));

        // Cmd-ESC で破棄
        im.put(KeyStroke.getKeyStroke("meta ESCAPE"), "dispose");
        am.put("dispose", new ProxyAction(disposeButton::doClick));
    }

    public void start() {
        dialog.setVisible(true);
    }

    public SaveParams getValue() {
        return value;
    }

    public void setValue(SaveParams params) {

        // Titleを表示する
        String val = params.getTitle();
        if (val != null && (!val.equals("") && (!val.equals("経過記録")))) {
            titleCombo.insertItemAt(val, 0);
        }
        titleCombo.setSelectedIndex(0);

        //
        // 診療科を表示する
        // 受付情報からの診療科を設定する
        val = params.getDepartment();
        if (val != null) {
            String[] depts = val.split("\\s*,\\s*");
            if (depts[0] != null) {
                departmentLabel.setText(depts[0]);
            } else {
                departmentLabel.setText(val);
            }
        }

        // 印刷部数選択
        //int count = params.getPrintCount();
        //if (count != -1) {
        //    printCombo.setSelectedItem(String.valueOf(count));

        //} else {
        // いつのまにか preferences: open.dolphin.client.plist
        // karte.print.count が -1 になっていてどうにも直せなくなったことがあった
        // printCombo.setEnabled(false);
        //    printCombo.setSelectedItem(0);
        //}

        //
        // CLAIM 送信をチェックする
        //
        if (params.isDisableSendClaim()) {
            // シングルカルテで CLAIM 送信自体を行わない場合
            sendClaim.setEnabled(false);
        } else {
            sendClaim.setSelected(params.isSendClaim());
        }

        checkTitle();
    }

    /**
     * タイトルフィールドの有効性をチェックする.
     */
    public void checkTitle() {
        boolean enabled = !titleField.getText().trim().equals("");
        okButton.setEnabled(enabled);
        tmpButton.setEnabled(enabled);
    }

    /**
     * GUIコンポーネントから値を取得し，saveparamsに設定する.
     */
    private void doOk() {

        // 文書タイトルを取得する
        String val = (String) titleCombo.getSelectedItem();
        if (!StringUtils.isEmpty(val)) {
            value.setTitle(val);
        } else {
            value.setTitle("経過記録");
        }

        // Department
        val = departmentLabel.getText();
        value.setDepartment(val);

        // 印刷部数を取得する
        //int count = Integer.parseInt((String)printCombo.getSelectedItem());
        //value.setPrintCount(count);
        value.setPrintCount(0); // 廃止

        //
        // CLAIM 送信
        //
        value.setSendClaim(sendClaim.isSelected());

        // 患者への参照許可を取得する
        value.setAllowPatientRef(false);
        // 診療歴のある施設への参照許可を設定する
        value.setAllowClinicRef(false);

        value.setSelection(SAVE);
        close();
    }

    /**
     * 仮保存の場合のパラメータを設定する.
     */
    private void doTemp() {
        //
        // 仮保存であることを設定する
        //
        value.setTmpSave(true);

        // 文書タイトルを取得する
        String val = (String) titleCombo.getSelectedItem();
        if (!StringUtils.isEmpty(val)) {
            value.setTitle(val);
        } else {
            value.setTitle("経過記録");
        }

        // Department
        val = departmentLabel.getText();
        value.setDepartment(val);

        //
        // 印刷部数を取得する
        // 仮保存でも印刷するかも知れない
        //
        //int count = Integer.parseInt((String)printCombo.getSelectedItem());
        //value.setPrintCount(count);

        //
        // CLAIM 送信
        //
        value.setSendClaim(false);

        // 患者への参照許可を取得する
        boolean b = false;
        value.setAllowPatientRef(b);

        // 診療歴のある施設への参照許可を設定する
        b = false;
        value.setAllowClinicRef(b);

        // 患者への参照許可を取得する
        value.setAllowPatientRef(false);
        // 診療歴のある施設への参照許可を設定する
        value.setAllowClinicRef(false);

        value.setSelection(TMP_SAVE);
        close();
    }

    private void doDispose() {
        value.setSelection(DISPOSE);
        close();
    }

    private void doCancel() {
        value.setSelection(CANCEL);
        close();
    }

    private void close() {
        dialog.setVisible(false);
        dialog.dispose();
    }

    public static void main(String[] args) {

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton b = new JButton("Show Sheet");
        b.addActionListener(e -> {
            SaveDialog sd = new SaveDialog(f);
            SaveParams param = new SaveParams();
            sd.setValue(param);
            sd.start();

            System.out.println("----modal--- ");

            param = sd.getValue();
            System.out.println("selection = " + param.getSelection());
        });

        f.add(b);
        f.setSize(500, 200);
        f.setVisible(true);
    }
}
