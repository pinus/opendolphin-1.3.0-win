package open.dolphin.setting;

import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.client.GUIFactory;
import open.dolphin.codehelper.AbstractCodeHelper;
import open.dolphin.event.ProxyDocumentListener;
import open.dolphin.helper.GridBagBuilder;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.project.ProjectStub;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * コードヘルパー設定パネル.
 * Preferences の node は AbstractCodeHelper.class を使っている.
 *
 * @author Kazushi
 * @author pns
 */
public class CodeHelperSettingPanel extends AbstractSettingPanel {

    private static final String ID = "codeHelperSetting";
    private static final String TITLE = "　コード　";
    private static final ImageIcon ICON = GUIConst.ICON_SHORTCUTS_32;

    private JRadioButton ctrlMask;
    private JRadioButton metaMask;

    private JTextField text;
    private JTextField path;
    private JTextField general;
    private JTextField other;
    private JTextField treatment;
    private JTextField surgery;
    private JTextField radiology;
    private JTextField labo;
    private JTextField physiology;
    private JTextField bacteria;
    private JTextField injection;
    private JTextField rp;
    private JTextField baseCharge;
    private JTextField instraction;
    private JTextField orca;
    private JTextField diag;

    private SettingPanelState curState = SettingPanelState.NONE;

    private HelperModel model;


    /**
     * Creates a new instance of CodeHelperSettingPanel.
     */
    public CodeHelperSettingPanel() {
        init();
    }

    private void init() {
        setId(ID);
        setTitle(TITLE);
        setIcon(ICON);
    }

    /**
     * GUI を生成しプログラムを開始する.
     */
    @Override
    public void start() {

        //
        // モデルを生成する
        //
        model = new HelperModel();

        //
        // GUI を構築する
        //
        initComponents();

        //
        // ModelToView
        //
        model.populate(getProjectStub());

    }

    /**
     * 保存する.
     */
    @Override
    public void save() {
        model.restore(getProjectStub());
    }

    /**
     * GUI を構築する.
     */
    private void initComponents() {

        ctrlMask = new JRadioButton("<html>&#8963;</html>"); // ^
        String str = ClientContext.isMac() ? "<html>&#8984;</html>" : "<html>&#8997;</html>"; // command / alt
        metaMask = new JRadioButton(str);
        text = GUIFactory.createTextField(5, null, null, null);
        path = GUIFactory.createTextField(5, null, null, null);
        general = GUIFactory.createTextField(5, null, null, null);
        other = GUIFactory.createTextField(5, null, null, null);
        treatment = GUIFactory.createTextField(5, null, null, null);
        surgery = GUIFactory.createTextField(5, null, null, null);
        radiology = GUIFactory.createTextField(5, null, null, null);
        labo = GUIFactory.createTextField(5, null, null, null);
        physiology = GUIFactory.createTextField(5, null, null, null);
        bacteria = GUIFactory.createTextField(5, null, null, null);
        injection = GUIFactory.createTextField(5, null, null, null);
        rp = GUIFactory.createTextField(5, null, null, null);
        baseCharge = GUIFactory.createTextField(5, null, null, null);
        instraction = GUIFactory.createTextField(5, null, null, null);
        orca = GUIFactory.createTextField(5, null, null, null);
        diag = GUIFactory.createTextField(5, null, null, null);
        //
        // 修飾キー
        //
        GridBagBuilder gbl = new GridBagBuilder("スタンプ箱キーワード入力後に「修飾キー + RETURN」でポップアップが出ます");

        gbl.add(new JLabel("修飾キー:"), 0, 0, GridBagConstraints.EAST);
        gbl.add(GUIFactory.createRadioPanel(new JRadioButton[]{ctrlMask, metaMask}), 1, 0, GridBagConstraints.CENTER);
        JPanel keyBind = gbl.getProduct();

        //
        // Stamptree
        //
        gbl = new GridBagBuilder("スタンプ箱のキーワード");

        gbl.add(new JLabel("テキスト:"), 0, 0, GridBagConstraints.EAST);
        gbl.add(text, 1, 0, GridBagConstraints.WEST);

        gbl.add(new JLabel("パス:"), 2, 0, GridBagConstraints.EAST);
        gbl.add(path, 3, 0, GridBagConstraints.WEST);

        gbl.add(new JLabel("汎 用:"), 0, 1, GridBagConstraints.EAST);
        gbl.add(general, 1, 1, GridBagConstraints.WEST);

        gbl.add(new JLabel("その他:"), 2, 1, GridBagConstraints.EAST);
        gbl.add(other, 3, 1, GridBagConstraints.WEST);

        gbl.add(new JLabel("処 置:"), 0, 2, GridBagConstraints.EAST);
        gbl.add(treatment, 1, 2, GridBagConstraints.WEST);

        gbl.add(new JLabel("手 術:"), 2, 2, GridBagConstraints.EAST);
        gbl.add(surgery, 3, 2, GridBagConstraints.WEST);

        gbl.add(new JLabel("放射線:"), 0, 3, GridBagConstraints.EAST);
        gbl.add(radiology, 1, 3, GridBagConstraints.WEST);

        gbl.add(new JLabel("検体検査:"), 2, 3, GridBagConstraints.EAST);
        gbl.add(labo, 3, 3, GridBagConstraints.WEST);

        gbl.add(new JLabel("生体検査:"), 0, 4, GridBagConstraints.EAST);
        gbl.add(physiology, 1, 4, GridBagConstraints.WEST);

        gbl.add(new JLabel("細菌検査:"), 2, 4, GridBagConstraints.EAST);
        gbl.add(bacteria, 3, 4, GridBagConstraints.WEST);

        gbl.add(new JLabel("注 射:"), 0, 5, GridBagConstraints.EAST);
        gbl.add(injection, 1, 5, GridBagConstraints.WEST);

        gbl.add(new JLabel("処 方:"), 2, 5, GridBagConstraints.EAST);
        gbl.add(rp, 3, 5, GridBagConstraints.WEST);

        gbl.add(new JLabel("診断料:"), 0, 6, GridBagConstraints.EAST);
        gbl.add(baseCharge, 1, 6, GridBagConstraints.WEST);

        gbl.add(new JLabel("指導・在宅:"), 2, 6, GridBagConstraints.EAST);
        gbl.add(instraction, 3, 6, GridBagConstraints.WEST);

        gbl.add(new JLabel("ORCA:"), 0, 7, GridBagConstraints.EAST);
        gbl.add(orca, 1, 7, GridBagConstraints.WEST);

        gbl.add(new JLabel("病名:"), 2, 7, GridBagConstraints.EAST);
        gbl.add(diag, 3, 7, GridBagConstraints.WEST);

        JPanel stamp = gbl.getProduct();

        // 全体をレイアウトする
        gbl = new GridBagBuilder();
        gbl.add(keyBind, 0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(stamp, 0, 1, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(new JLabel(""), 0, 2, GridBagConstraints.BOTH, 1.0, 1.0);

        setUI(gbl.getProduct());

    }

    private void connect() {

        ButtonGroup bg = new ButtonGroup();
        bg.add(ctrlMask);
        bg.add(metaMask);

        //
        // DocumentListener
        //
        ProxyDocumentListener dl = e -> checkState();

        text.getDocument().addDocumentListener(dl);
        path.getDocument().addDocumentListener(dl);
        general.getDocument().addDocumentListener(dl);
        other.getDocument().addDocumentListener(dl);
        treatment.getDocument().addDocumentListener(dl);
        surgery.getDocument().addDocumentListener(dl);
        radiology.getDocument().addDocumentListener(dl);
        labo.getDocument().addDocumentListener(dl);
        physiology.getDocument().addDocumentListener(dl);
        bacteria.getDocument().addDocumentListener(dl);
        injection.getDocument().addDocumentListener(dl);
        rp.getDocument().addDocumentListener(dl);
        baseCharge.getDocument().addDocumentListener(dl);
        instraction.getDocument().addDocumentListener(dl);
        orca.getDocument().addDocumentListener(dl);
        diag.getDocument().addDocumentListener(dl);

    }

    public void checkState() {

        SettingPanelState newState;

        if (text.getText().trim().equals("") ||
                path.getText().trim().equals("") ||
                general.getText().trim().equals("") ||
                other.getText().trim().equals("") ||
                treatment.getText().trim().equals("") ||
                surgery.getText().trim().equals("") ||
                radiology.getText().trim().equals("") ||
                labo.getText().trim().equals("") ||
                physiology.getText().trim().equals("") ||
                bacteria.getText().trim().equals("") ||
                injection.getText().trim().equals("") ||
                injection.getText().trim().equals("") ||
                rp.getText().trim().equals("") ||
                baseCharge.getText().trim().equals("") ||
                instraction.getText().trim().equals("") ||
                orca.getText().trim().equals("") ||
                diag.getText().trim().equals("")) {

            newState = SettingPanelState.INVALID;

        } else {
            newState = SettingPanelState.VALID;
        }

        if (curState != newState) {
            curState = newState;
            setState(curState);
        }
    }

    private class HelperModel {

        /**
         * 設定した値をプレファレンスに保存する.
         */
        public void restore(ProjectStub stub) {

            Preferences prefs = Preferences.userNodeForPackage(AbstractCodeHelper.class);

            String mask = ctrlMask.isSelected() ? "ctrl" : "meta";

            prefs.put("modifier", mask);

            prefs.put(IInfoModel.ENTITY_TEXT, text.getText().trim());
            prefs.put(IInfoModel.ENTITY_PATH, path.getText().trim());
            prefs.put(IInfoModel.ENTITY_GENERAL_ORDER, general.getText().trim());
            prefs.put(IInfoModel.ENTITY_OTHER_ORDER, other.getText().trim());
            prefs.put(IInfoModel.ENTITY_TREATMENT, treatment.getText().trim());
            prefs.put(IInfoModel.ENTITY_SURGERY_ORDER, surgery.getText().trim());
            prefs.put(IInfoModel.ENTITY_RADIOLOGY_ORDER, radiology.getText().trim());
            prefs.put(IInfoModel.ENTITY_LABO_TEST, labo.getText().trim());
            prefs.put(IInfoModel.ENTITY_PHYSIOLOGY_ORDER, physiology.getText().trim());
            prefs.put(IInfoModel.ENTITY_BACTERIA_ORDER, bacteria.getText().trim());
            prefs.put(IInfoModel.ENTITY_INJECTION_ORDER, injection.getText().trim());
            prefs.put(IInfoModel.ENTITY_MED_ORDER, rp.getText().trim());
            prefs.put(IInfoModel.ENTITY_BASE_CHARGE_ORDER, baseCharge.getText().trim());
            prefs.put(IInfoModel.ENTITY_INSTRACTION_CHARGE_ORDER, instraction.getText().trim());
            prefs.put(IInfoModel.ENTITY_ORCA, orca.getText().trim());
            prefs.put(IInfoModel.ENTITY_DIAGNOSIS, diag.getText().trim());
        }

        /**
         * プレファレンスから値をGUIにセットする.
         */
        public void populate(ProjectStub stub) {

            Preferences prefs = Preferences.userNodeForPackage(AbstractCodeHelper.class);

            String modifier = prefs.get("modifier", "ctrl");

            if (modifier.equals("ctrl")) {
                ctrlMask.setSelected(true);
                metaMask.setSelected(false);
            } else {
                ctrlMask.setSelected(false);
                metaMask.setSelected(true);
            }

            text.setText(prefs.get(IInfoModel.ENTITY_TEXT, "tx").trim());
            path.setText(prefs.get(IInfoModel.ENTITY_PATH, "pat").trim());
            general.setText(prefs.get(IInfoModel.ENTITY_GENERAL_ORDER, "gen").trim());
            other.setText(prefs.get(IInfoModel.ENTITY_OTHER_ORDER, "oth").trim());
            treatment.setText(prefs.get(IInfoModel.ENTITY_TREATMENT, "tr").trim());
            surgery.setText(prefs.get(IInfoModel.ENTITY_SURGERY_ORDER, "sur").trim());
            radiology.setText(prefs.get(IInfoModel.ENTITY_RADIOLOGY_ORDER, "rad").trim());
            labo.setText(prefs.get(IInfoModel.ENTITY_LABO_TEST, "lab").trim());
            physiology.setText(prefs.get(IInfoModel.ENTITY_PHYSIOLOGY_ORDER, "phy").trim());
            bacteria.setText(prefs.get(IInfoModel.ENTITY_BACTERIA_ORDER, "bac").trim());
            injection.setText(prefs.get(IInfoModel.ENTITY_INJECTION_ORDER, "inj").trim());
            rp.setText(prefs.get(IInfoModel.ENTITY_MED_ORDER, "rp").trim());
            baseCharge.setText(prefs.get(IInfoModel.ENTITY_BASE_CHARGE_ORDER, "base").trim());
            instraction.setText(prefs.get(IInfoModel.ENTITY_INSTRACTION_CHARGE_ORDER, "ins").trim());
            orca.setText(prefs.get(IInfoModel.ENTITY_ORCA, "orca").trim());
            diag.setText(prefs.get(IInfoModel.ENTITY_DIAGNOSIS, "dx").trim());

            connect();
            checkState();
        }
    }
}












