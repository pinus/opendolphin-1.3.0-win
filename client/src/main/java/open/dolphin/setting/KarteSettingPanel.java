package open.dolphin.setting;

import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.client.GUIFactory;
import open.dolphin.event.ProxyDocumentListener;
import open.dolphin.helper.GridBagBuilder;
import open.dolphin.helper.PNSPair;
import open.dolphin.helper.StringTool;
import open.dolphin.inspector.InspectorCategory;
import open.dolphin.project.Project;
import open.dolphin.project.ProjectStub;
import open.dolphin.ui.ComboBoxFactory;
import open.dolphin.ui.PNSTabbedPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * KarteSettingPanel.
 *
 * @author Minagawa, Kazushi
 * @author pns
 */
public class KarteSettingPanel extends AbstractSettingPanel {

    private static final String ID = "karteSetting";
    private static final String TITLE = "　カルテ　";
    private static final ImageIcon ICON = GUIConst.ICON_KARTE_EDIT_32;
    private static final int INSPECTOR_COMBO_COUNT = 5;

    private Preferences prefs;

    // デフォルト値
    private boolean defaultLocator;
    private boolean defaultAsc;
    private boolean defaultShowModified;
    private int defaultFetchCount;
    private int minFetchCount;
    private int maxFetchCount;
    private int stepFetchCount;
    private boolean defaultScDirection;
    private int defaultPeriod;
    private boolean defaultDiagnosisAsc;
    private int defaultDiagnosisPeriod;
    private boolean defaultAutoOutcomeInput;
    private int defaultOffsetOutcomeDate;
    private int defaultLaboTestPeriod;

    // インスペクタ選択 Combo
    private JComboBox<InspectorCategory>[] inspectorCombo;
    private JLabel infoLabel;
    private JRadioButton pltform;
    private JRadioButton prefLoc;

    // カルテ文書関係
    private JRadioButton asc;
    private JRadioButton desc;
    private JCheckBox showModifiedCB;
    private JSpinner spinner;
    private JComboBox<PNSPair<String, Integer>> periodCombo;
    private JRadioButton vSc;
    private JRadioButton hSc;

    // 病名関係
    private JRadioButton diagnosisAsc;
    private JRadioButton diagnosisDesc;
    private JComboBox<PNSPair<String, Integer>> diagnosisPeriodCombo;
    private JCheckBox autoOutcomeInput;
    private JSpinner outcomeSpinner;

    // 検体検査
    private JComboBox<PNSPair<String, Integer>> laboTestPeriodCombo;

    // コマンドボタン
    private JButton restoreDefaultBtn;

    // Stamp
    private JRadioButton replaceStamp;
    private JRadioButton showAlert;
    private JCheckBox stampSpace;
    private JCheckBox laboFold;
    private JTextField defaultZyozaiNum;
    private JTextField defaultMizuyakuNum;
    private JTextField defaultSanyakuNum;
    private JTextField defaultRpNum;

    // CLAIM 送信関係
    private JRadioButton sendAtTmp;
    private JRadioButton noSendAtTmp;
    private JRadioButton sendAtSave;
    private JRadioButton noSendAtSave;
    private JRadioButton sendAtModify;
    private JRadioButton noSendAtModify;
    private JRadioButton sendDiagnosis;
    private JRadioButton noSendDiagnosis;
    private JCheckBox useTop15AsTitle;
    private JTextField defaultKarteTitle;

    // 確認ダイアログ関係
    private JCheckBox noConfirmAtNew;
    private JRadioButton copyNew;
    private JRadioButton applyRp;
    private JRadioButton emptyNew;
    private JRadioButton placeWindow;
    private JRadioButton palceTabbedPane;
    private JCheckBox noConfirmAtSave;
    private JRadioButton save;
    private JRadioButton saveTmp;
    private JFormattedTextField printCount;

    private JTextField pdfStore;
    private JButton pdfDir;

    private KarteModel model;
    private boolean ok = true;

    public KarteSettingPanel() {
        init();
    }

    private void init() {
        setId(ID);
        setTitle(TITLE);
        setIcon(ICON);
    }

    /**
     * 設定画面を開始する.
     */
    @Override
    public void start() {

        prefs = Project.getPreferences();

        // モデルを生成し初期化する
        model = new KarteModel();
        model.populate(getProjectStub());

        // GUI を構築する
        initComponents();

        // bindModel
        bindModelToView();
    }

    /**
     * 設定値を保存する.
     */
    @Override
    public void save() {
        bindViewToModel();
        model.restore(getProjectStub());
    }

    /**
     * GUI を構築する.
     */
    private void initComponents() {

        // デフォルト値を取得する
        defaultLocator = false;
        defaultAsc = ClientContext.getBoolean("docHistory.default.ascending");
        defaultShowModified = ClientContext.getBoolean("docHistory.default.showModified");
        defaultFetchCount = ClientContext.getInt("docHistory.default.fetchCount");
        minFetchCount = ClientContext.getInt("docHistory.min.fetchCount");
        maxFetchCount = ClientContext.getInt("docHistory.max.fetchCount");
        stepFetchCount = ClientContext.getInt("docHistory.step.fetchCount");
        defaultScDirection = ClientContext.getBoolean("karte.default.scDirection");
        defaultPeriod = ClientContext.getInt("docHistory.default.period");
        defaultDiagnosisAsc = ClientContext.getBoolean("diagnosis.default.ascending");
        defaultDiagnosisPeriod = ClientContext.getInt("diagnosis.default.period");
        defaultAutoOutcomeInput = false;
        defaultOffsetOutcomeDate = ClientContext.getInt("diagnosis.default.offsetOutcomeDate");
        defaultLaboTestPeriod = ClientContext.getInt("laboTest.default.period");

        //
        // GUI コンポーネントを生成する
        //

        // インスペクタ選択 Combo を INSPECTOR_COMBO_COUNT だけ作る.
        inspectorCombo = new JComboBox[INSPECTOR_COMBO_COUNT];
        for (int i = 0; i < INSPECTOR_COMBO_COUNT; i++) {
            inspectorCombo[i] = new JComboBox<>(InspectorCategory.values());
        }

        infoLabel = new JLabel("有効な組み合わせになっています。");

        // 患者インスペクタ画面のロケータ
        pltform = new JRadioButton("プラットフォーム");
        prefLoc = new JRadioButton("位置と大きさを記憶する");

        // PDF store
        pdfStore = GUIFactory.createTextField(30, null, null, null);
        pdfDir = new JButton("設定");

        // カルテ文書関係
        asc = new JRadioButton("昇順");
        desc = new JRadioButton("降順");
        showModifiedCB = new JCheckBox("修正履歴表示");
        periodCombo = ComboBoxFactory.createDocumentExtractionPeriodCombo();

        vSc = new JRadioButton("垂直");
        hSc = new JRadioButton("水平");

        // 病名関係
        diagnosisAsc = new JRadioButton("昇順");
        diagnosisDesc = new JRadioButton("降順");
        diagnosisPeriodCombo = ComboBoxFactory.createDiagnosisExtractionPeriodCombo();
        autoOutcomeInput = new JCheckBox("終了日を自動入力する");

        // 検体検査
        laboTestPeriodCombo = ComboBoxFactory.createLaboExtractionPeriodCombo();

        // コマンドボタン
        restoreDefaultBtn = new JButton("デフォルト設定に戻す");

        // スタンプ動作
        replaceStamp = new JRadioButton("置き換える");
        showAlert = new JRadioButton("警告する");
        stampSpace = new JCheckBox("DnD時にスタンプの間隔を空ける");
        laboFold = new JCheckBox("検体検査の項目を折りたたみ表示する");
        defaultZyozaiNum = GUIFactory.createTextField(3, null, null, null);
        defaultMizuyakuNum = GUIFactory.createTextField(3, null, null, null);
        defaultSanyakuNum = GUIFactory.createTextField(3, null, null, null);
        defaultRpNum = GUIFactory.createTextField(3, null, null, null);

        // CLAIM 送信関係
        sendAtTmp = new JRadioButton("送信する");
        noSendAtTmp = new JRadioButton("送信しない");
        sendAtSave = new JRadioButton("送信する");
        noSendAtSave = new JRadioButton("送信しない");
        sendAtModify = new JRadioButton("送信する");
        noSendAtModify = new JRadioButton("送信しない");
        sendDiagnosis = new JRadioButton("送信する");
        noSendDiagnosis = new JRadioButton("送信しない");
        useTop15AsTitle = new JCheckBox("カルテの先頭15文字を使用する");
        defaultKarteTitle = GUIFactory.createTextField(10, null, null, null);

        // 確認ダイアログ関係
        noConfirmAtNew = new JCheckBox("確認ダイアログを表示しない");
        copyNew = new JRadioButton("全てコピー");
        applyRp = new JRadioButton("前回処方を適用");
        emptyNew = new JRadioButton("空白の新規カルテ");
        placeWindow = new JRadioButton("別ウィンドウで編集");
        palceTabbedPane = new JRadioButton("タブパネルへ追加");

        noConfirmAtSave = new JCheckBox("確認ダイアログを表示しない");
        save = new JRadioButton("保 存");
        saveTmp = new JRadioButton("仮保存");

        // 自動文書取得数の Spinner
        int currentFetchCount = prefs.getInt(Project.DOC_HISTORY_FETCHCOUNT, defaultFetchCount);
        SpinnerModel fetchModel = new SpinnerNumberModel(currentFetchCount, minFetchCount, maxFetchCount, stepFetchCount);
        spinner = new JSpinner(fetchModel);
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));

        //転帰入力時に日付を入力する場合のオフセット値
        int currentOffsetOutcomeDate = prefs.getInt(Project.OFFSET_OUTCOME_DATE, defaultOffsetOutcomeDate);
        SpinnerModel outcomeModel = new SpinnerNumberModel(currentOffsetOutcomeDate, -31, 0, 1);
        outcomeSpinner = new JSpinner(outcomeModel);
        outcomeSpinner.setEditor(new JSpinner.NumberEditor(outcomeSpinner, "#"));

        // インスペクタ画面 Memo & ロケータ
        JPanel frameLocator = new JPanel();
        frameLocator.add(pltform);
        frameLocator.add(prefLoc);

        // 文書履歴の昇順降順
        JPanel ascDesc = new JPanel();
        ascDesc.add(asc);
        ascDesc.add(desc);
        ascDesc.add(showModifiedCB);

        // スクロール方向
        JPanel scrP = new JPanel();
        scrP.add(vSc);
        scrP.add(hSc);

        // インスペクタタブ
        GridBagBuilder gbb = new GridBagBuilder("インスペクタ画面");
        int row = 0;
        JLabel label = new JLabel("左側トップ:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(inspectorCombo[row], 1, row, 1, 1, GridBagConstraints.WEST);
        row++;
        label = new JLabel("2番目:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(inspectorCombo[row], 1, row, 1, 1, GridBagConstraints.WEST);
        row++;
        label = new JLabel("3番目:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(inspectorCombo[row], 1, row, 1, 1, GridBagConstraints.WEST);
        row++;
        label = new JLabel("4番目:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(inspectorCombo[row], 1, row, 1, 1, GridBagConstraints.WEST);
        row++;
        label = new JLabel("ボトム:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(inspectorCombo[row], 1, row, 1, 1, GridBagConstraints.WEST);
        row++;

        label = new JLabel(GUIConst.ICON_INFORMATION_16);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(infoLabel, 1, row, 1, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("画面ロケータ:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(frameLocator, 1, row, 1, 1, GridBagConstraints.WEST);
        JPanel insP = gbb.getProduct();

        gbb = new GridBagBuilder();
        gbb.add(insP, 0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbb.add(new JLabel(""), 0, 2, GridBagConstraints.BOTH, 1.0, 1.0);
        JPanel inspectorPanel = gbb.getProduct();

        // 文書関連タブ
        // Karte
        gbb = new GridBagBuilder("カルテ");
        row = 0;

        row++;
        label = new JLabel("文書履歴:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(ascDesc, 1, row, 1, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("自動文書取得数:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(spinner, 1, row, 1, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("スクロール方向:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(scrP, 1, row, 1, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("文書抽出期間:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(periodCombo, 1, row, 1, 1, GridBagConstraints.WEST);
        JPanel kartePanel = gbb.getProduct();

        // Diagnosis
        JPanel diagAscDesc = new JPanel();
        diagAscDesc.add(diagnosisAsc);
        diagAscDesc.add(diagnosisDesc);
        gbb = new GridBagBuilder("傷病名");
        row = 0;
        label = new JLabel("表示順:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(diagAscDesc, 1, row, 1, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("抽出期間:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(diagnosisPeriodCombo, 1, row, 1, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("転帰入力時:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(autoOutcomeInput, 1, row, 1, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("入力する日(前):", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(outcomeSpinner, 1, row, 1, 1, GridBagConstraints.WEST);
        JPanel diagnosisPanel = gbb.getProduct();

        // LaboTest
        gbb = new GridBagBuilder("ラボテスト");
        row = 0;
        label = new JLabel("抽出期間:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(laboTestPeriodCombo, 1, row, 1, 1, GridBagConstraints.WEST);
        JPanel laboPanel = gbb.getProduct();

        // Set default button
        JPanel cmd = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        cmd.add(restoreDefaultBtn);

        gbb = new GridBagBuilder();
        gbb.add(kartePanel, 0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbb.add(diagnosisPanel, 0, 1, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbb.add(laboPanel, 0, 2, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbb.add(cmd, 0, 3, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbb.add(new JLabel(""), 0, 4, GridBagConstraints.BOTH, 1.0, 1.0);

        JPanel docPanel = gbb.getProduct();

        ButtonGroup bg = new ButtonGroup();

        bg.add(asc);
        bg.add(desc);

        bg = new ButtonGroup();
        bg.add(diagnosisAsc);
        bg.add(diagnosisDesc);

        bg = new ButtonGroup();
        bg.add(pltform);
        bg.add(prefLoc);

        bg = new ButtonGroup();
        bg.add(vSc);
        bg.add(hSc);

        restoreDefaultBtn.addActionListener(e -> restoreDefault());

        // スタンプ動作
        JPanel stampPanel = new JPanel();
        stampPanel.setLayout(new BoxLayout(stampPanel, BoxLayout.Y_AXIS));

        gbb = new GridBagBuilder("スタンプ動作の設定");
        row = 0;
        label = new JLabel("スタンプの上にDnDした場合:", SwingConstants.RIGHT);
        JPanel stmpP = GUIFactory.createRadioPanel(new JRadioButton[]{replaceStamp, showAlert});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(stmpP, 1, row, 1, 1, GridBagConstraints.WEST);
        row++;
        gbb.add(stampSpace, 0, row, 2, 1, GridBagConstraints.WEST);
        row++;
        gbb.add(laboFold, 0, row, 2, 1, GridBagConstraints.WEST);
        stampPanel.add(gbb.getProduct());
        bg = new ButtonGroup();
        bg.add(replaceStamp);
        bg.add(showAlert);

        gbb = new GridBagBuilder("スタンプエディタのデフォルト数量");
        row = 0;
        label = new JLabel("錠剤の場合:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(createUnitFieldPanel(defaultZyozaiNum, "T"), 1, row, 1, 1, GridBagConstraints.WEST);
        row++;
        label = new JLabel("水薬の場合:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(createUnitFieldPanel(defaultMizuyakuNum, "ml"), 1, row, 1, 1, GridBagConstraints.WEST);
        row++;
        label = new JLabel("散薬の場合:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(createUnitFieldPanel(defaultSanyakuNum, "g"), 1, row, 1, 1, GridBagConstraints.WEST);
        row++;
        label = new JLabel("処方日数:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(createUnitFieldPanel(defaultRpNum, "日/回"), 1, row, 1, 1, GridBagConstraints.WEST);
        stampPanel.add(gbb.getProduct());
        stampPanel.add(Box.createVerticalStrut(400));
        stampPanel.add(Box.createVerticalGlue());

        // CLAIM 送信のデフォルト設定
        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BoxLayout(sendPanel, BoxLayout.Y_AXIS));

        gbb = new GridBagBuilder("カルテの保存時に設定するタイトル");
        row = 0;
        gbb.add(useTop15AsTitle, 0, row, 2, 1, GridBagConstraints.WEST);
        row++;
        label = new JLabel("デフォルトのタイトル:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(defaultKarteTitle, 1, row, 1, 1, GridBagConstraints.WEST);
        sendPanel.add(gbb.getProduct());

        gbb = new GridBagBuilder("診療行為送信のデフォルト設定");
        row = 0;
        label = new JLabel("仮保存時:", SwingConstants.RIGHT);
        JPanel p9 = GUIFactory.createRadioPanel(new JRadioButton[]{sendAtTmp, noSendAtTmp});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p9, 1, row, 1, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("保存時:", SwingConstants.RIGHT);
        p9 = GUIFactory.createRadioPanel(new JRadioButton[]{sendAtSave, noSendAtSave});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p9, 1, row, 1, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("修正時:", SwingConstants.RIGHT);
        p9 = GUIFactory.createRadioPanel(new JRadioButton[]{sendAtModify, noSendAtModify});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p9, 1, row, 1, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("傷病名:", SwingConstants.RIGHT);
        p9 = GUIFactory.createRadioPanel(new JRadioButton[]{sendDiagnosis, noSendDiagnosis});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p9, 1, row, 1, 1, GridBagConstraints.WEST);

        sendPanel.add(gbb.getProduct());
        sendPanel.add(Box.createVerticalStrut(500));
        sendPanel.add(Box.createVerticalGlue());

        // 新規カルテ作成時と保存時の確認ダイアログオプション
        JPanel confirmPanel = new JPanel();
        confirmPanel.setLayout(new BoxLayout(confirmPanel, BoxLayout.Y_AXIS));
        NumberFormat numFormat = NumberFormat.getNumberInstance();
        printCount = new JFormattedTextField(numFormat);
        printCount.setValue(0);

        row = 0;
        gbb = new GridBagBuilder("新規カルテ作成時");
        gbb.add(noConfirmAtNew, 0, row, 2, 1, GridBagConstraints.WEST);

        row += 1;
        label = new JLabel("作成方法:", SwingConstants.RIGHT);
        JPanel p = GUIFactory.createRadioPanel(new JRadioButton[]{copyNew, applyRp, emptyNew});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p, 1, row, 1, 1, GridBagConstraints.WEST);

        row += 1;
        label = new JLabel("配置方法:", SwingConstants.RIGHT);
        JPanel p2 = GUIFactory.createRadioPanel(new JRadioButton[]{placeWindow, palceTabbedPane});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p2, 1, row, 1, 1, GridBagConstraints.WEST);
        confirmPanel.add(gbb.getProduct());

        gbb = new GridBagBuilder("カルテ保存時");
        row = 0;
        gbb.add(noConfirmAtSave, 0, row, 2, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("印刷枚数:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(printCount, 1, row, 1, 1, GridBagConstraints.WEST);

        row++;
        label = new JLabel("動 作:", SwingConstants.RIGHT);
        JPanel p4 = GUIFactory.createRadioPanel(new JRadioButton[]{save, saveTmp});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p4, 1, row, 1, 1, GridBagConstraints.WEST);
        confirmPanel.add(gbb.getProduct());

        confirmPanel.add(Box.createVerticalStrut(200));
        confirmPanel.add(Box.createVerticalGlue());

        bg = new ButtonGroup();
        bg.add(copyNew);
        bg.add(applyRp);
        bg.add(emptyNew);

        bg = new ButtonGroup();
        bg.add(placeWindow);
        bg.add(palceTabbedPane);

        bg = new ButtonGroup();
        bg.add(sendAtTmp);
        bg.add(noSendAtTmp);

        bg = new ButtonGroup();
        bg.add(sendAtSave);
        bg.add(noSendAtSave);

        bg = new ButtonGroup();
        bg.add(sendAtModify);
        bg.add(noSendAtModify);

        bg = new ButtonGroup();
        bg.add(sendDiagnosis);
        bg.add(noSendDiagnosis);

        bg = new ButtonGroup();
        bg.add(save);
        bg.add(saveTmp);

        PNSTabbedPane tabbedPane = new PNSTabbedPane();
        tabbedPane.addTab("インスペクタ", inspectorPanel);
        tabbedPane.addTab("文 書", docPanel);
        tabbedPane.addTab("スタンプ", stampPanel);
        tabbedPane.addTab("診療行為", sendPanel);
        tabbedPane.addTab("確認ダイアログ", confirmPanel);

        getUI().setLayout(new BorderLayout());
        getUI().add(tabbedPane);
    }

    /**
     * KarteSettingPanel の Valid 判定.
     */
    private void checkState() {

        boolean inspectorOk = true;
        // ComboBox の選択 index
        int[] index = new int[INSPECTOR_COMBO_COUNT];
        // 各 Category の選択された個数
        int[] category = new int[InspectorCategory.values().length];

        for (int i = 0; i < INSPECTOR_COMBO_COUNT; i++) {
            index[i] = inspectorCombo[i].getSelectedIndex();
        }

        // 無効な組み合わせを検出して，infoLabel を設定する部
        // 各 Category がいくつあったか数えて，「なし」以外の重複を検出
        for (int combo = 0; combo < INSPECTOR_COMBO_COUNT; combo++) {
            int selected = index[combo];
            category[selected]++;
            // 「なし」以外が重複してたらダメ
            if (selected != InspectorCategory.なし.ordinal() && category[selected] >= 2) {
                inspectorOk = false;
                infoLabel.setText("「" + inspectorCombo[combo].getSelectedItem() + "」が重複しています");
            }
        }
        // 文書履歴はかならず選ばれていなくてはならない
        if (category[InspectorCategory.文書履歴.ordinal()] == 0) {
            inspectorOk = false;
            infoLabel.setText("文書履歴は必ず設定して下さい");
        }

        // 上記全てにひっかからなかった場合，有効な組み合わせになっている
        if (inspectorOk) {
            infoLabel.setText("有効な組み合わせになっています");
        }

        boolean titleOk = true;
        if (!useTop15AsTitle.isSelected()) {
            String test = defaultKarteTitle.getText().trim();
            if (test.equals("")) {
                titleOk = false;
                infoLabel.setText("タイトルが未入力です");
            }
        }

        // valid_state セット部
        boolean newOk = (inspectorOk && titleOk);

        if (ok != newOk) {
            ok = newOk;
            if (ok) {
                setState(SettingPanelState.VALID);
                infoLabel.setForeground(Color.black);
            } else {
                setState(SettingPanelState.INVALID);
                infoLabel.setForeground(Color.red);
            }
        }
    }

    private void choosePDFDirectory() {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        String baseDir = pdfStore.getText().trim();
        if (!baseDir.equals("")) {
            File f = new File(baseDir);
            chooser.setSelectedFile(f);
        }
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            pdfStore.setText(chooser.getSelectedFile().getPath());
        }
    }

    /**
     * ModelToView.
     */
    private void bindModelToView() {

        // インスペクタの左  String name から index を調べる
        inspectorCombo[0].setSelectedIndex(InspectorCategory.orderOfName(model.getTopInspector()));
        inspectorCombo[1].setSelectedIndex(InspectorCategory.orderOfName(model.getSecondInspector()));
        inspectorCombo[2].setSelectedIndex(InspectorCategory.orderOfName(model.getThirdInspector()));
        inspectorCombo[3].setSelectedIndex(InspectorCategory.orderOfName(model.getForthInspector()));
        inspectorCombo[4].setSelectedIndex(InspectorCategory.orderOfName(model.getFifthInspector()));

        // PDF 出力先
        pdfStore.setText(model.getPdfStore());
        pdfDir.addActionListener(e -> choosePDFDirectory());

        // 選択インスペクタの重複をチェックするためのリスナを付ける
        for (JComboBox<InspectorCategory> box : inspectorCombo) {
            box.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) { checkState(); }
            });
        }

        // インスペクタ画面のロケータ
        boolean curLocator = model.isLocateByPlatform();
        pltform.setSelected(curLocator);
        prefLoc.setSelected(!curLocator);

        // カルテの昇順表示
        boolean currentAsc = model.isAscendingKarte();
        asc.setSelected(currentAsc);
        desc.setSelected(!currentAsc);

        // 修正履歴表示
        showModifiedCB.setSelected(model.isShowModifiedKarte());

        // 抽出期間
        int currentPeriod = model.getKarteExtractionPeriod();
        List<PNSPair<String, Integer>> periodList = ComboBoxFactory.getDocumentExtractionPeriodModel();
        periodCombo.setSelectedIndex(PNSPair.getIndex(currentPeriod, periodList));

        // カルテの取得枚数
        spinner.setValue(model.getFetchKarteCount());

        // 複数カルテのスクロール方向
        boolean vscroll = model.isScrollKarteV();
        vSc.setSelected(vscroll);
        hSc.setSelected(!vscroll);

        // 病名の昇順表示
        boolean currentDiagnosisAsc = model.isAscendingDiagnosis();
        diagnosisAsc.setSelected(currentDiagnosisAsc);
        diagnosisDesc.setSelected(!currentDiagnosisAsc);

        // 病名の抽出期間
        int currentDiagnosisPeriod = model.getDiagnosisExtractionPeriod();
        List<PNSPair<String, Integer>> diagPeriodList = ComboBoxFactory.getDiagnosisExtractionPeriodModel();
        diagnosisPeriodCombo.setSelectedIndex(PNSPair.getIndex(currentDiagnosisPeriod, diagPeriodList));

        // 転帰のオフセット
        autoOutcomeInput.setSelected(model.isAutoOutcomeInput());
        outcomeSpinner.setEnabled(autoOutcomeInput.isSelected());
        autoOutcomeInput.addActionListener(e -> outcomeSpinner.setEnabled(autoOutcomeInput.isSelected()));

        // ラボテストの抽出期間
        int currentLaboTestPeriod = model.getLabotestExtractionPeriod();
        List<PNSPair<String, Integer>> laboPeriodList = ComboBoxFactory.getLaboExtractionPeriodModel();
        laboTestPeriodCombo.setSelectedIndex(PNSPair.getIndex(currentLaboTestPeriod, laboPeriodList));

        // スタンプ動作
        replaceStamp.setSelected(model.isReplaceStamp());
        showAlert.setSelected(!model.isReplaceStamp());
        stampSpace.setSelected(model.isStampSpace());
        laboFold.setSelected(model.isLaboFold());
        defaultZyozaiNum.setText(model.getDefaultZyozaiNum());
        defaultMizuyakuNum.setText(model.getDefaultMizuyakuNum());
        defaultSanyakuNum.setText(model.getDefaultSanyakuNum());
        defaultRpNum.setText(model.getDefaultRpNum());

        //
        // CLAIM 送信関係
        // 仮保存の時は送信できない. 理由は CRC 等の入力するケース.
        //
        noSendAtTmp.doClick();
        sendAtTmp.setEnabled(false);
        noSendAtTmp.setEnabled(false);

        // 保存時の送信
        if (model.isSendClaimSave()) {
            sendAtSave.doClick();
        } else {
            noSendAtSave.doClick();
        }

        // 修正時の送信
        if (model.isSendClaimModify()) {
            sendAtModify.doClick();
        } else {
            noSendAtModify.doClick();
        }

        // 病名送信
        if (model.isSendDiagnosis()) {
            sendDiagnosis.doClick();
        } else {
            noSendDiagnosis.doClick();
        }

        // カルテタイトル
        useTop15AsTitle.addActionListener(e -> {
            boolean enabled = useTop15AsTitle.isSelected();
            defaultKarteTitle.setEnabled(!enabled);
        });

        defaultKarteTitle.setText(model.getDefaultKarteTitle());
        if (model.isUseTop15AsTitle()) {
            useTop15AsTitle.doClick();
        }

        defaultKarteTitle.getDocument().addDocumentListener((ProxyDocumentListener) e -> checkState());

        //
        // 確認ダイアログ関係
        //

        // カルテの作成モード
        switch (model.getCreateKarteMode()) {
            case 0:
                emptyNew.setSelected(true);
                break;

            case 1:
                applyRp.setSelected(true);
                break;

            case 2:
                copyNew.setSelected(true);
                break;
        }

        // 配置方法
        if (model.isPlaceKarteMode()) {
            placeWindow.setSelected(true);
        } else {
            palceTabbedPane.setSelected(true);
        }

        // 新規カルテ時の確認ダイログ
        boolean curConfirmAtNew = model.isConfirmAtNew();
        noConfirmAtNew.setSelected(!curConfirmAtNew);
        emptyNew.setEnabled(!curConfirmAtNew);
        applyRp.setEnabled(!curConfirmAtNew);
        copyNew.setEnabled(!curConfirmAtNew);
        placeWindow.setEnabled(!curConfirmAtNew);
        palceTabbedPane.setEnabled(!curConfirmAtNew);
        noConfirmAtNew.addActionListener(e -> {
            boolean enabled = noConfirmAtNew.isSelected();
            emptyNew.setEnabled(enabled);
            applyRp.setEnabled(enabled);
            copyNew.setEnabled(enabled);
            placeWindow.setEnabled(enabled);
            palceTabbedPane.setEnabled(enabled);
        });

        // 保存時のデフォルト動作
        if (model.getSaveKarteMode() == 0) {
            save.setSelected(true);
        } else {
            saveTmp.setSelected(true);
        }

        // 保存時の確認ダイログ
        boolean curConfirmAtSave = model.isConfirmAtSave();
        noConfirmAtSave.setSelected(!curConfirmAtSave);
        printCount.setValue(model.getPrintKarteCount());
        printCount.setEnabled(!curConfirmAtSave);
        save.setEnabled(!curConfirmAtSave);
        saveTmp.setEnabled(!curConfirmAtSave);
        noConfirmAtSave.addActionListener(e -> {
            boolean enabled = noConfirmAtSave.isSelected();
            printCount.setEnabled(enabled);
            save.setEnabled(enabled);
            saveTmp.setEnabled(enabled);
        });

        // この設定画面は常に有効状態である
        setState(SettingPanelState.VALID);
    }

    /**
     * ViewToModel.
     */
    private void bindViewToModel() {

        // インスペクタの左
        model.setTopInspector(InspectorCategory.values()[inspectorCombo[0].getSelectedIndex()].name());
        model.setSecondInspector(InspectorCategory.values()[inspectorCombo[1].getSelectedIndex()].name());
        model.setThirdInspector(InspectorCategory.values()[inspectorCombo[2].getSelectedIndex()].name());
        model.setForthInspector(InspectorCategory.values()[inspectorCombo[3].getSelectedIndex()].name());
        model.setFifthInspector(InspectorCategory.values()[inspectorCombo[4].getSelectedIndex()].name());

        model.setPdfStore(pdfStore.getText().trim());

        // インスペクタ画面のロケータ
        model.setLocateByPlatform(pltform.isSelected());

        // カルテの昇順表示
        model.setAscendingKarte(asc.isSelected());

        // カルテの修正履歴表示
        model.setShowModifiedKarte(showModifiedCB.isSelected());

        // カルテの取得枚数
        String value = spinner.getValue().toString();
        model.setFetchKarteCount(Integer.parseInt(value));

        // 複数カルテのスクロール方向
        model.setScrollKarteV(vSc.isSelected());

        // カルテの抽出期間
        int index = periodCombo.getSelectedIndex();
        int period = ComboBoxFactory.getDocumentExtractionPeriodModel().get(index).getValue();
        model.setKarteExtractionPeriod(period);

        // 病名の昇順表示
        model.setAscendingDiagnosis(diagnosisAsc.isSelected());

        // 病名の抽出期間
        index = diagnosisPeriodCombo.getSelectedIndex();
        period = ComboBoxFactory.getDiagnosisExtractionPeriodModel().get(index).getValue();
        model.setDiagnosisExtractionPeriod(period);

        // 転帰入力時の終了日オフセット
        model.setAutoOutcomeInput(autoOutcomeInput.isSelected());
        String val = outcomeSpinner.getValue().toString();
        prefs.putInt(Project.OFFSET_OUTCOME_DATE, Integer.parseInt(val));

        // ラボテストの抽出期間
        index = laboTestPeriodCombo.getSelectedIndex();
        period = ComboBoxFactory.getLaboExtractionPeriodModel().get(index).getValue();
        model.setLabotestExtractionPeriod(period);

        // スタンプ関連
        model.setReplaceStamp(replaceStamp.isSelected());
        model.setStampSpace(stampSpace.isSelected());
        model.setLaboFold(laboFold.isSelected());
        model.setDefaultZyozaiNum(defaultZyozaiNum.getText().trim());
        model.setDefaultMizuyakuNum(defaultMizuyakuNum.getText().trim());
        model.setDefaultSanyakuNum(defaultSanyakuNum.getText().trim());
        model.setDefaultRpNum(defaultRpNum.getText().trim());

        // 仮保存時の CLAIM 送信
        model.setSendClaimTmp(sendAtTmp.isSelected());

        // 保存時の CLAIM 送信
        model.setSendClaimSave(sendAtSave.isSelected());

        // 修正時の CLAIM 送信
        model.setSendClaimModify(sendAtModify.isSelected());

        // 保存時に設定するカルテのタイトル
        model.setUseTop15AsTitle(useTop15AsTitle.isSelected());
        model.setDefaultKarteTitle(defaultKarteTitle.getText().trim());

        // 病名の CLAIM 送信
        model.setSendDiagnosis(sendDiagnosis.isSelected());

        // 新規カルテ時の確認ダイアログ
        model.setConfirmAtNew(!noConfirmAtNew.isSelected());

        // 保存時の確認ダイアログ
        model.setConfirmAtSave(!noConfirmAtSave.isSelected());

        // 新規カルテの作成モード
        int cMode = 0; // emptyNew
        if (applyRp.isSelected()) { cMode = 1; }
        else if (copyNew.isSelected()) { cMode = 2; }
        model.setCreateKarteMode(cMode); // 0=emptyNew, 1=applyRp, 2=copyNew

        // 新規カルテの配置方法
        model.setPlaceKarteMode(placeWindow.isSelected());

        // 印刷枚数
        Integer ival = (Integer) printCount.getValue();
        model.setPrintKarteCount(ival);

        // 保存時のデフォルト動作
        int sMode = save.isSelected() ? 0 : 1;
        model.setSaveKarteMode(sMode); // 0=save, 1=saveTmp
    }

    public void restoreDefault() {

        pltform.setSelected(defaultLocator);
        prefLoc.setSelected(!defaultLocator);

        asc.setSelected(defaultAsc);
        desc.setSelected(!defaultAsc);
        showModifiedCB.setSelected(defaultShowModified);
        spinner.setValue(defaultFetchCount);
        periodCombo.setSelectedIndex(PNSPair.getIndex(defaultPeriod, ComboBoxFactory.getDocumentExtractionPeriodModel()));
        vSc.setSelected(defaultScDirection);

        diagnosisAsc.setSelected(defaultDiagnosisAsc);
        diagnosisDesc.setSelected(!defaultDiagnosisAsc);
        diagnosisPeriodCombo.setSelectedIndex(PNSPair.getIndex(defaultDiagnosisPeriod, ComboBoxFactory.getDiagnosisExtractionPeriodModel()));
        autoOutcomeInput.setSelected(defaultAutoOutcomeInput);
        outcomeSpinner.setValue(defaultOffsetOutcomeDate);

        laboTestPeriodCombo.setSelectedIndex(PNSPair.getIndex(defaultLaboTestPeriod, ComboBoxFactory.getLaboExtractionPeriodModel()));
    }

    private JPanel createUnitFieldPanel(JTextField tf, String unit) {

        JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        ret.add(tf);
        ret.add(new JLabel(unit));
        return ret;
    }

    private String testNumber(String test) {
        String ret = null;
        try {
            Float.parseFloat(test);
            ret = StringTool.toHankakuNumber(test);
        } catch (NumberFormatException e) {
            System.out.println("KarteSettingPanel.java: " + e);
        }
        return ret;
    }

    /**
     * 画面モデルクラス.
     */
    private class KarteModel {

        // インスペクタ
        private String topInspector;
        private String secondInspector;
        private String thirdInspector;
        private String forthInspector;
        private String fifthInspector;
        private String pdfStore;

        // インスペクタ画面のロケータ
        private boolean locateByPlatform;
        // カルテ文書関係
        private int fetchKarteCount;
        private boolean ascendingKarte;
        private boolean showModifiedKarte;
        private boolean scrollKarteV;
        private int karteExtractionPeriod;
        // 病名関係
        private boolean ascendingDiagnosis;
        private int diagnosisExtractionPeriod;
        private boolean autoOutcomeInput;
        // 検体検査
        private int labotestExtractionPeriod;
        // スタンプ動作
        private boolean replaceStamp;
        private boolean stampSpace;
        private boolean laboFold;
        private String defaultZyozaiNum;
        private String defaultMizuyakuNum;
        private String defaultSanyakuNum;
        private String defaultRpNum;
        // CLAIM 送信関係
        private boolean sendClaimTmp;
        private boolean sendClaimSave;
        private boolean sendClaimModify;
        private boolean sendDiagnosis;
        private String defaultKarteTitle;
        private boolean useTop15AsTitle;
        // 確認ダイアログ関係
        private boolean confirmAtNew;
        private int createKarteMode;
        private boolean placeKarteMode;
        private boolean confirmAtSave;
        private int saveKarteMode;
        private int printKarteCount;

        /**
         * ProjectStub から populate する.
         */
        public void populate(ProjectStub stub) {

            setTopInspector(stub.getTopInspector());

            setSecondInspector(stub.getSecondInspector());

            setThirdInspector(stub.getThirdInspector());

            setForthInspector(stub.getForthInspector());

            setFifthInspector(stub.getFifthInspector());

            setLocateByPlatform(stub.getLocateByPlatform());

            setPdfStore(stub.getPDFStore());

            setFetchKarteCount(stub.getFetchKarteCount());

            setScrollKarteV(stub.getScrollKarteV());

            setAscendingKarte(stub.getAscendingKarte());
            //System.out.println("populate asc = " + stub.getAscendingKarte());

            setKarteExtractionPeriod(stub.getKarteExtractionPeriod());

            setShowModifiedKarte(stub.getShowModifiedKarte());

            setAscendingDiagnosis(stub.getAscendingDiagnosis());

            setDiagnosisExtractionPeriod(stub.getDiagnosisExtractionPeriod());

            setAutoOutcomeInput(stub.isAutoOutcomeInput());

            setLabotestExtractionPeriod(stub.getLabotestExtractionPeriod());

            setReplaceStamp(stub.isReplaceStamp());

            setStampSpace(stub.isStampSpace());

            setLaboFold(stub.isLaboFold());

            setDefaultZyozaiNum(stub.getDefaultZyozaiNum());

            setDefaultMizuyakuNum(stub.getDefaultMizuyakuNum());

            setDefaultSanyakuNum(stub.getDefaultSanyakuNum());

            setDefaultRpNum(stub.getDefaultRpNum());

            setSendClaimTmp(stub.getSendClaimTmp());

            setSendClaimSave(stub.getSendClaimSave());

            setSendClaimModify(stub.getSendClaimModify());

            setUseTop15AsTitle(stub.isUseTop15AsTitle());

            setDefaultKarteTitle(stub.getDefaultKarteTitle());

            setSendDiagnosis(stub.getSendDiagnosis());

            setConfirmAtNew(stub.getConfirmAtNew());

            setCreateKarteMode(stub.getCreateKarteMode());

            setPlaceKarteMode(stub.getPlaceKarteMode());

            setConfirmAtSave(stub.getConfirmAtSave());

            setPrintKarteCount(stub.getPrintKarteCount());

            setSaveKarteMode(stub.getSaveKarteMode());

        }

        public void restore(ProjectStub stub) {

            stub.setTopInspector(getTopInspector());

            stub.setSecondInspector(getSecondInspector());

            stub.setThirdInspector(getThirdInspector());

            stub.setForthInspector(getForthInspector());

            stub.setFifthInspector(getFifthInspector());

            stub.setLocateByPlatform(isLocateByPlatform());

            String pdfDir = getPdfStore();
            if (pdfDir != null && (!pdfDir.equals(""))) {
                stub.setPDFStore(pdfDir);
            }

            stub.setFetchKarteCount(getFetchKarteCount());

            stub.setScrollKarteV(isScrollKarteV());

            stub.setAscendingKarte(isAscendingKarte());

            stub.setKarteExtractionPeriod(getKarteExtractionPeriod());

            stub.setShowModifiedKarte(isShowModifiedKarte());

            stub.setAscendingDiagnosis(isAscendingDiagnosis());

            stub.setDiagnosisExtractionPeriod(getDiagnosisExtractionPeriod());

            stub.setAutoOutcomeInput(isAutoOutcomeInput());

            stub.setLabotestExtractionPeriod(getLabotestExtractionPeriod());

            stub.setReplaceStamp(isReplaceStamp());

            stub.setStampSpace(isStampSpace());

            stub.setLaboFold(isLaboFold());

            String test = testNumber(getDefaultZyozaiNum());
            if (test != null) {
                stub.setDefaultZyozaiNum(test);
            }

            test = testNumber(getDefaultMizuyakuNum());
            if (test != null) {
                stub.setDefaultMizuyakuNum(test);
            }

            test = testNumber(getDefaultSanyakuNum());
            if (test != null) {
                stub.setDefaultSanyakuNum(test);
            }

            test = testNumber(getDefaultRpNum());
            if (test != null) {
                stub.setDefaultRpNum(test);
            }

            stub.setSendClaimTmp(isSendClaimTmp());

            stub.setSendClaimSave(isSendClaimSave());

            stub.setSendClaimModify(isSendClaimModify());

            stub.setUseTop15AsTitle(isUseTop15AsTitle());

            test = getDefaultKarteTitle();
            if (test != null && (!test.equals(""))) {
                stub.setDefaultKarteTitle(test);
            }

            stub.setSendDiagnosis(isSendDiagnosis());

            stub.setConfirmAtNew(isConfirmAtNew());

            stub.setCreateKarteMode(getCreateKarteMode());

            stub.setPlaceKarteMode(isPlaceKarteMode());

            stub.setConfirmAtSave(isConfirmAtSave());

            stub.setPrintKarteCount(getPrintKarteCount());

            stub.setSaveKarteMode(getSaveKarteMode());

        }

        public boolean isLocateByPlatform() {
            return locateByPlatform;
        }

        public void setLocateByPlatform(boolean locateByPlatform) {
            this.locateByPlatform = locateByPlatform;
        }

        public int getFetchKarteCount() {
            return fetchKarteCount;
        }

        public void setFetchKarteCount(int fetchKarteCount) {
            this.fetchKarteCount = fetchKarteCount;
        }

        public boolean isAscendingKarte() {
            return ascendingKarte;
        }

        public void setAscendingKarte(boolean ascendingKarte) {
            this.ascendingKarte = ascendingKarte;
        }

        public boolean isShowModifiedKarte() {
            return showModifiedKarte;
        }

        public void setShowModifiedKarte(boolean showModifiedKarte) {
            this.showModifiedKarte = showModifiedKarte;
        }

        public boolean isScrollKarteV() {
            return scrollKarteV;
        }

        public void setScrollKarteV(boolean scrollKarteV) {
            this.scrollKarteV = scrollKarteV;
        }

        public int getKarteExtractionPeriod() {
            return karteExtractionPeriod;
        }

        public void setKarteExtractionPeriod(int karteExtractionPeriod) {
            this.karteExtractionPeriod = karteExtractionPeriod;
        }

        public boolean isAscendingDiagnosis() {
            return ascendingDiagnosis;
        }

        public void setAscendingDiagnosis(boolean ascendingDiagnosis) {
            this.ascendingDiagnosis = ascendingDiagnosis;
        }

        public int getDiagnosisExtractionPeriod() {
            return diagnosisExtractionPeriod;
        }

        public void setDiagnosisExtractionPeriod(int diagnosisExtractionPeriod) {
            this.diagnosisExtractionPeriod = diagnosisExtractionPeriod;
        }

        public boolean isAutoOutcomeInput() {
            return autoOutcomeInput;
        }

        public void setAutoOutcomeInput(boolean b) {
            autoOutcomeInput = b;
        }

        public int getLabotestExtractionPeriod() {
            return labotestExtractionPeriod;
        }

        public void setLabotestExtractionPeriod(int laboTestExtractionPeriod) {
            this.labotestExtractionPeriod = laboTestExtractionPeriod;
        }

        public boolean isSendClaimTmp() {
            return sendClaimTmp;
        }

        public void setSendClaimTmp(boolean sendClaimTmp) {
            this.sendClaimTmp = sendClaimTmp;
        }

        public boolean isSendClaimSave() {
            return sendClaimSave;
        }

        public void setSendClaimSave(boolean sendClaimSave) {
            this.sendClaimSave = sendClaimSave;
        }

        public boolean isSendClaimModify() {
            return sendClaimModify;
        }

        public void setSendClaimModify(boolean sendClaimModify) {
            this.sendClaimModify = sendClaimModify;
        }

        public boolean isSendDiagnosis() {
            return sendDiagnosis;
        }

        public void setSendDiagnosis(boolean sendDiagnosis) {
            this.sendDiagnosis = sendDiagnosis;
        }

        public boolean isConfirmAtNew() {
            return confirmAtNew;
        }

        public void setConfirmAtNew(boolean confirmAtNew) {
            this.confirmAtNew = confirmAtNew;
        }

        public int getCreateKarteMode() {
            return createKarteMode;
        }

        public void setCreateKarteMode(int createKarteMode) {
            this.createKarteMode = createKarteMode;
        }

        public boolean isPlaceKarteMode() {
            return placeKarteMode;
        }

        public void setPlaceKarteMode(boolean placeKarteMode) {
            this.placeKarteMode = placeKarteMode;
        }

        public boolean isConfirmAtSave() {
            return confirmAtSave;
        }

        public void setConfirmAtSave(boolean confirmAtSave) {
            this.confirmAtSave = confirmAtSave;
        }

        public int getSaveKarteMode() {
            return saveKarteMode;
        }

        public void setSaveKarteMode(int saveKarteMode) {
            this.saveKarteMode = saveKarteMode;
        }

        public int getPrintKarteCount() {
            return printKarteCount;
        }

        public void setPrintKarteCount(int printKarteCount) {
            this.printKarteCount = printKarteCount;
        }

        public boolean isReplaceStamp() {
            return replaceStamp;
        }

        public void setReplaceStamp(boolean replaceStamp) {
            this.replaceStamp = replaceStamp;
        }

        public boolean isStampSpace() {
            return stampSpace;
        }

        public void setStampSpace(boolean stampSpace) {
            this.stampSpace = stampSpace;
        }

        public boolean isLaboFold() {
            return laboFold;
        }

        public void setLaboFold(boolean laboFold) {
            this.laboFold = laboFold;
        }

        public String getTopInspector() {
            return topInspector;
        }

        public void setTopInspector(String topInspector) {
            this.topInspector = topInspector;
        }

        public String getSecondInspector() {
            return secondInspector;
        }

        public void setSecondInspector(String secondInspector) {
            this.secondInspector = secondInspector;
        }

        public String getThirdInspector() {
            return thirdInspector;
        }

        public void setThirdInspector(String thirdInspector) {
            this.thirdInspector = thirdInspector;
        }

        public String getForthInspector() {
            return forthInspector;
        }

        public void setForthInspector(String forthInspector) {
            this.forthInspector = forthInspector;
        }

        public String getFifthInspector() {
            return fifthInspector;
        }

        public void setFifthInspector(String fifthInspector) {
            this.fifthInspector = fifthInspector;
        }

        public String getDefaultZyozaiNum() {
            return defaultZyozaiNum;
        }

        public void setDefaultZyozaiNum(String defaultZyozaiNum) {
            this.defaultZyozaiNum = defaultZyozaiNum;
        }

        public String getDefaultMizuyakuNum() {
            return defaultMizuyakuNum;
        }

        public void setDefaultMizuyakuNum(String defaultMizuyakuNum) {
            this.defaultMizuyakuNum = defaultMizuyakuNum;
        }

        public String getDefaultSanyakuNum() {
            return defaultSanyakuNum;
        }

        public void setDefaultSanyakuNum(String defaultSanyakuNum) {
            this.defaultSanyakuNum = defaultSanyakuNum;
        }

        public String getDefaultRpNum() {
            return defaultRpNum;
        }

        public void setDefaultRpNum(String defaultRpNum) {
            this.defaultRpNum = defaultRpNum;
        }

        public String getDefaultKarteTitle() {
            return defaultKarteTitle;
        }

        public void setDefaultKarteTitle(String defaultKarteTitle) {
            this.defaultKarteTitle = defaultKarteTitle;
        }

        public boolean isUseTop15AsTitle() {
            return useTop15AsTitle;
        }

        public void setUseTop15AsTitle(boolean useTop15AsTitle) {
            this.useTop15AsTitle = useTop15AsTitle;
        }

        public String getPdfStore() {
            return pdfStore;
        }

        public void setPdfStore(String pdfStore) {
            this.pdfStore = pdfStore;
        }
    }
}
