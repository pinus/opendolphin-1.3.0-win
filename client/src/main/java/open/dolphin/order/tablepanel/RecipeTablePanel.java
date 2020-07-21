package open.dolphin.order.tablepanel;

import open.dolphin.client.GUIConst;
import open.dolphin.helper.PNSTriple;
import open.dolphin.helper.StringTool;
import open.dolphin.infomodel.*;
import open.dolphin.orca.ClaimConst;
import open.dolphin.order.IStampEditor;
import open.dolphin.order.MasterItem;
import open.dolphin.project.Project;
import open.dolphin.ui.UndoableObjectReflectTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

/**
 * ItemTablePanel を extend して作った RecipeTablePanel.
 *
 * @author pns
 */
public class RecipeTablePanel extends ItemTablePanel {
    private static final long serialVersionUID = 1L;
    Logger logger = LoggerFactory.getLogger(RecipeTablePanel.class);

    private static final ImageIcon INFO_BUTTON_IMAGE = GUIConst.ICON_INFORMATION_16;
    private static final String ADMIN_MARK = "[用法] ";
    private static final String REG_ADMIN_MARK = "\\[用法\\] ";
    private static final String LABEL_TEXT_IN_MED = "院内";
    private static final String LABEL_TEXT_OUT_MED = "院外";
    private static final String IN_MEDICINE = "院内処方";
    private static final String OUT_MEDICINE = "院外処方";

    // 院内処方
    private JRadioButton inMedButton;
    // 院外処方
    private JRadioButton outMedButton;
    // 再編集の場合に保存しておくレセ電算コード
    private String saveReceiptCode;
    // State Label
    private JLabel stateLabel;
    // 親からの値のコピー
    private JTable table;
    private UndoableObjectReflectTableModel<MasterItem> tableModel;
    private JTextField stampNameField;
    private JButton removeButton;
    private JButton clearButton;

    public RecipeTablePanel(IStampEditor parent) {
        super(parent);
        init();
    }

    private void init() {
        // 親からコピー
        table = getTable();
        tableModel = getTableModel();
    }

    /**
     * RecipeTablePanel の TableModel を作る.
     *
     * @return
     */
    @Override
    public UndoableObjectReflectTableModel<MasterItem> createTableModel() {
        List<PNSTriple<String, Class<?>, String>> reflectList = Arrays.asList(
                new PNSTriple<>(" コード", String.class, "getCode"),
                new PNSTriple<>("　診療内容", String.class, "getName"),
                new PNSTriple<>(" 数 量", String.class, "getNumber"),
                new PNSTriple<>(" 単 位", String.class, "getUnit"),
                new PNSTriple<>(" ", String.class, "getDummy"),
                new PNSTriple<>(" 回 数", String.class, "getBundleNumber")
        );
        setTableColumnWidth(new int[]{90, 200, 50, 80, 30, 50});

        return new UndoableObjectReflectTableModel<MasterItem>(reflectList) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) {
                // 数量と回数は編集可能
                // code がコメントコード（810000001)なら，診療内カラムを編集可能とする
                return (col == 1 && "810000001".equals(this.getValueAt(row, 0))) || col == 2 || col == 5;
            }

            @Override
            public void setValueAt(Object o, int row, int col) {
                super.setValueAt(o, row, col); // undo 登録
                updateTable(o, row, col);
            }

            @Override
            public void undoSetValueAt(Object o, int row, int col) {
                updateTable(o, row, col);
            }

            private void updateTable(Object o, int row, int col) {
                if (o == null || ((String) o).trim().equals("")) {
                    return;
                }
                // tableModel のオブジェクトは MasterItem
                MasterItem mItem = getObject(row);
                if (mItem == null) { return; }

                if (col == 2) {
                    mItem.setNumber((String) o);
                } // １日量
                else if (col == 5) {
                    mItem.setBundleNumber((String) o);
                }// 何日分
                else if (col == 1) {
                    mItem.setName((String) o);
                }// 入力したコメントは name に入れる

                fireTableCellUpdated(row, col);
            }
        };
    }

    /**
     * テーブル下のコンポネントを作る.
     *
     * @return
     */
    @Override
    public JPanel createSouthPanel() {

        // 親のコンポネント
        stampNameField = getStampNameField();
        removeButton = getRemoveButton();
        clearButton = getClearButton();

        // RecipeTablePanel 特有のコンポネント
        // 院内・院外処方ラジオボタン
        inMedButton = new JRadioButton(LABEL_TEXT_IN_MED);
        outMedButton = new JRadioButton(LABEL_TEXT_OUT_MED);
        ButtonGroup g = new ButtonGroup();
        g.add(inMedButton);
        g.add(outMedButton);

        boolean bOut = Project.getPreferences().getBoolean(Project.RP_OUT, true);
        if (bOut) {
            outMedButton.setSelected(true);
        } else {
            inMedButton.setSelected(true);
        }

        ActionListener al = e -> {
            boolean b = outMedButton.isSelected();
            Project.getPreferences().putBoolean(Project.RP_OUT, b);
        };
        inMedButton.addActionListener(al);
        outMedButton.addActionListener(al);

        // 状態表示ラベル
        stateLabel = new JLabel();

        // パネルのレイアウト
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(SET_NAME_LABEL_TEXT));
        panel.add(stampNameField);
        panel.add(inMedButton);
        panel.add(outMedButton);
        panel.add(new JLabel(INFO_BUTTON_IMAGE));
        panel.add(stateLabel);
        panel.add(Box.createHorizontalGlue());
        panel.add(removeButton);
        panel.add(clearButton);

        return panel;
    }

    /**
     * MasterPanel から医薬品及び用法の通知を受け，データをセットする.
     */
    @Override
    public void receiveMaster(MasterItem item) {

        // テーブルに登録されているのオブジェクトの数
        int oCount = tableModel.getObjectCount();
        //テーブルの１つ上のアイテムが[用法]かどうか
        MasterItem pItem = oCount > 0 ? tableModel.getObject(oCount - 1) : null;
        boolean isAdmin = ((pItem != null) && (pItem.getClassCode() == ClaimConst.ADMIN));

        // stampNameField がセットされていなかったら item の名前をセットする
        String name = stampNameField.getText().trim();
        if (name.equals("") || name.equals(DEFAULT_STAMP_NAME)) {
            stampNameField.setText(item.getName());
        }

        switch (item.getClassCode()) {
            case ClaimConst.YAKUZAI:
                String inputNum = DEFAULT_NUMBER;
                if (item.getUnit() != null) {
                    String unit = item.getUnit();
                    switch (unit) {
                        case "錠":
                            inputNum = Project.getPreferences().get("defaultZyozaiNum", "3");
                            break;
                        case "ｇ":
                            inputNum = Project.getPreferences().get("defaultSanyakuNum", "1.0");
                            break;
                        case "ｍＬ":
                            inputNum = Project.getPreferences().get("defaultMizuyakuNum", "1");
                            break;
                        default:
                            break;
                    }
                }
                item.setNumber(inputNum);

                // １つ上のアイテムが[用法]ならば，その上に挿入する
                if (isAdmin) {
                    tableModel.undoableInsertRow(oCount - 1, item);
                } else {
                    tableModel.undoableAddRow(item);
                }
                break;

            case ClaimConst.ZAIRYO:
                item.setNumber(DEFAULT_NUMBER);
                // １つ上のアイテムが[用法]ならば，その上に挿入する
                if (isAdmin) {
                    tableModel.undoableInsertRow(oCount - 1, item);
                } else {
                    tableModel.undoableAddRow(item);
                }
                break;

            case ClaimConst.SYUGI:
                // １つ上のアイテムが[用法]ならば，その上に挿入する
                if (isAdmin) {
                    tableModel.undoableInsertRow(oCount - 1, item);
                } else {
                    tableModel.undoableAddRow(item);
                }
                break;

            case ClaimConst.ADMIN:
                item.setName(ADMIN_MARK + item.getName());
                item.setDummy("X");
                // 投与日数の設定
                Object xxTD = tableModel.getValueAt(oCount - 1, 5); // bundle
                if (item.isNaiyo()) {
                    // 内服なら
                    if (xxTD == null) {
                        item.setBundleNumber(Project.getPreferences().get("defaultRpNum", DEFAULT_NUMBER));
                    } else {
                        item.setBundleNumber(xxTD.toString());
                    }
                } else {
                    // 外用なら
                    item.setBundleNumber("1");
                }   //もし，一つ上が用法だったら，それは消す
                if (isAdmin) {
                    tableModel.undoableDeleteRow(oCount - 1);
                }
                tableModel.undoableAddRow(item);
                break;

            default:
                break;
        }
        // ユーザ登録領域（用法，部位），コメントなら，投与量の初期値 を "." にセットする
        if (item.getCode().startsWith("8") || item.getCode().startsWith("0")) {
            //item.setNumber(".");
            item.setNumber("");
        }

        // 用法が登録されていなければデフォルトの用法をセット
        boolean hasAdmin = false;
        for (Object o : tableModel.getObjectList()) {
            if (((MasterItem) o).getClassCode() == ClaimConst.ADMIN) {
                hasAdmin = true;
                break;
            }
        }

        if (!hasAdmin) {
            MasterItem defaultAdmin = new MasterItem();
            defaultAdmin.setClassCode(ClaimConst.ADMIN);
            defaultAdmin.setDummy("X");
            defaultAdmin.setNumber(".");
            defaultAdmin.setYkzKbn("0");

            if ("1".equals(item.getYkzKbn())) {
                defaultAdmin.setCode("001000202");
                defaultAdmin.setName(ADMIN_MARK + "１日２回朝夕食後に");
                defaultAdmin.setBundleNumber(Project.getPreferences().get("defaultRpNum", DEFAULT_NUMBER));

            } else {
                defaultAdmin.setCode("001000603");
                defaultAdmin.setName(ADMIN_MARK + "１日２回外用");
                defaultAdmin.setBundleNumber("1");
            }
            tableModel.undoableAddRow(defaultAdmin);
        }
    }

    /**
     * BundleMed, Entity，StampRole，StampName のセットされた ModuleModel を返す.
     *
     * @return
     */
    private ModuleModel createModuleModel() {

        ModuleModel retModel = new ModuleModel();
        BundleMed med = new BundleMed();
        retModel.setModel(med);

        // StampInfoを設定する
        ModuleInfoBean moduleInfo = retModel.getModuleInfo();
        moduleInfo.setEntity(getEntity());
        moduleInfo.setStampRole(IInfoModel.ROLE_P);

        //　スタンプ名を設定する
        String stampName = stampNameField.getText().trim();
        if (!stampName.equals("")) {
            moduleInfo.setStampName(stampName);
        } else {
            moduleInfo.setStampName(DEFAULT_STAMP_NAME);
        }

        return retModel;
    }

    /**
     * MasterItem から ClaimItem を作る.
     *
     * @param mItem
     * @return
     */
    private ClaimItem createClaimItem(MasterItem mItem) {
        ClaimItem item = new ClaimItem();
        item.setClassCode(String.valueOf(mItem.getClassCode())); // ３桁の数字
        item.setClassCodeSystem(ClaimConst.SUBCLASS_CODE_ID);
        item.setCode(mItem.getCode());
        item.setName(mItem.getName());
        return item;
    }

    /**
     * table の内容を ModuleModel にまとめて返す.
     * ModuleModel
     * |- ModuleInfo
     * |- BundleMed(ClaimBundle)
     * |- ClaimItem1
     * |- ClaimItem2
     * :
     * <p>
     * 各項目は ClaimItem に入る
     * ADMIN 情報と，外用/内容/頓用 情報は BundleMed に入る
     *
     * @return
     */
    @Override
    public Object getValue() {

        List items = tableModel.getObjectList();
        // 基本的な ModuleModel を作る
        ModuleModel module = createModuleModel();
        BundleMed bundle = (BundleMed) module.getModel();

        // まず，頓用フラグ処理
        boolean tonyo = false;
        for (Object item : items) {
            MasterItem mItem = (MasterItem) item;
            if (mItem == null) {
                break;
            } // ありえない
            if (mItem.getClassCode() == ClaimConst.ADMIN && mItem.isTonyo()) {
                tonyo = true;
                break;
            }
        }

        // MasterItem を全部調べて ModuleModel にセットしていく
        for (Object item : items) {
            MasterItem mItem = (MasterItem) item;
            if (mItem == null) {
                break;
            } // ありえない

            // number（１日量）を半角変換して設定し直す
            String number = mItem.getNumber();
            if (number != null && (!number.trim().equals(""))) {
                number = StringTool.toHankakuNumber(number.trim());
                mItem.setNumber(number);
            } else {
                number = null;
            }

            switch (mItem.getClassCode()) {

                case ClaimConst.SYUGI:
                    ClaimItem sItem = createClaimItem(mItem);
                    bundle.addClaimItem(sItem);
                    break;

                case ClaimConst.YAKUZAI:
                    ClaimItem yItem = createClaimItem(mItem);
                    bundle.addClaimItem(yItem);
                    if (number != null) {
                        yItem.setNumber(number);
                        yItem.setUnit(mItem.getUnit());
                        // 数量コード 10/11/12 2007-05 現在のORCAの実装では採用していない
                        yItem.setNumberCode(ClaimConst.YAKUZAI_TOYORYO);
                        yItem.setNumberCodeSystem(ClaimConst.NUMBER_CODE_ID);
                    }

                    // bundle に RECEIPT_CODE がなければ設定する
                    if (bundle.getClassCode() == null) {
                        String rCode; // レセプトコード（= 診療種別区分）

                        // 薬剤区分（内用1，外用6，注射薬4）がセットされていればそれを使う
                        if (mItem.getYkzKbn() != null) {
                            rCode = mItem.getYkzKbn().equals(ClaimConst.YKZ_KBN_NAIYO)
                                    ? ClaimConst.RECEIPT_CODE_NAIYO     // 210 診療種別区分
                                    : ClaimConst.RECEIPT_CODE_GAIYO;    // 230 診療種別区分
                            // 薬剤区分がセットされていなければ，setValue の時に保存しておいたコードを使う
                        } else if (saveReceiptCode != null) {
                            rCode = saveReceiptCode;

                            // 薬剤区分がセットされておらず，保存もされていない場合は内用としておく
                        } else {
                            rCode = ClaimConst.RECEIPT_CODE_NAIYO;  // 220 診療種別区分
                        }

                        // 内用の場合は頓用かどうか処理する
                        if (rCode.equals(ClaimConst.RECEIPT_CODE_NAIYO) || rCode.equals(ClaimConst.RECEIPT_CODE_TONYO)) {
                            rCode = tonyo ? ClaimConst.RECEIPT_CODE_TONYO : ClaimConst.RECEIPT_CODE_NAIYO;
                            //System.out.println("----- Tonyo="+ tonyo);
                        }

                        // 作成した薬剤区分コードを bundle にセットする
                        bundle.setClassCode(rCode); // 診療種別区分（３桁の数字）が入る
                        bundle.setClassCodeSystem(ClaimConst.CLASS_CODE_ID);
                        bundle.setClassName(ClaimConst.getSrysyukbnName(rCode));
                    }
                    break;

                case ClaimConst.ZAIRYO:
                    ClaimItem zItem = createClaimItem(mItem);
                    bundle.addClaimItem(zItem);
                    if (number != null) {
                        zItem.setNumber(number);
                        zItem.setUnit(mItem.getUnit());
                        zItem.setNumberCode(ClaimConst.ZAIRYO_KOSU);
                        zItem.setNumberCodeSystem(ClaimConst.NUMBER_CODE_ID);
                    }
                    break;

                case ClaimConst.ADMIN:
                    String ommit = mItem.getName().replaceAll(REG_ADMIN_MARK, "");
                    bundle.setAdmin(ommit);
                    bundle.setAdminCode(mItem.getCode());
                    String bNum = mItem.getBundleNumber();
                    if (bNum != null && !bNum.trim().equals("")) {
                        bNum = StringTool.toHankakuNumber(bNum.trim());
                        bundle.setBundleNumber(bNum);
                    }
                    String memo = inMedButton.isSelected() ? IN_MEDICINE : OUT_MEDICINE;
                    bundle.setMemo(memo);
                    break;
            }
        }

        return module;
    }

    /**
     * 編集するスタンプを表示する.
     *
     * @param theStamp 編集するスタンプ
     */
    @Override
    public void setValue(Object theStamp) {

        // 連続して編集される場合があるのでテーブル内容等をクリアする
        tableModel.clear();
        tableModel.discardAllUndoableEdits();

        if (theStamp == null) { return; }

        // 引数で渡された Stamp をキャストする
        ModuleModel target = (ModuleModel) theStamp;

        // Entityを保存する
        setEntity(target.getModuleInfo().getEntity());

        // スタンプ名を表示する
        String stampName = target.getModuleInfo().getStampName();
        boolean serialized = target.getModuleInfo().isSerialized();

        if (!serialized && stampName.startsWith(FROM_EDITOR_STAMP_NAME)) {
            stampName = DEFAULT_STAMP_NAME;
        } else if (stampName.equals("")) {
            stampName = DEFAULT_STAMP_NAME;
        }
        stampNameField.setText(stampName);

        BundleMed med = (BundleMed) target.getModel();
        if (med == null) {
            return;
        }

        // レセ電算コードを保存する
        if (med.getClassCode() != null) {
            saveReceiptCode = med.getClassCode();
        }

        ClaimItem[] items = med.getClaimItem();

        for (ClaimItem item : items) {

            MasterItem mItem = new MasterItem();
            mItem.setClassCode(Integer.parseInt(item.getClassCode()));

            // Code Name TableId
            mItem.setName(item.getName());
            mItem.setCode(item.getCode());

            String number = item.getNumber();
            if (number != null && (!number.equals(""))) {
                number = StringTool.toHankakuNumber(number.trim());
                mItem.setNumber(number);
                mItem.setUnit(item.getUnit());
            } else {
                mItem.setNumber("");
            }

            tableModel.addRow(mItem);
        }

        // Save Administration
        if (med.getAdmin() != null) {
            MasterItem item = new MasterItem();
            item.setClassCode(3);
            item.setCode(med.getAdminCode());
            item.setName(ADMIN_MARK + med.getAdmin());
            item.setDummy("X");
            String bNumber = med.getBundleNumber();
            bNumber = StringTool.toHankakuNumber(bNumber);
            item.setBundleNumber(bNumber);
            tableModel.addRow(item);
        }

        // Memo
        String memo = med.getMemo();
        if (memo != null && memo.equals(IN_MEDICINE)) {
            inMedButton.setSelected(true);
        } else {
            outMedButton.setSelected(true);
        }
    }

    /**
     * RecipeTableModel 独自の checkState
     */
    @Override
    public void checkState() {
        // empty
        if (tableModel.getObjectCount() == 0) {
            removeButton.setEnabled(false);
            clearButton.setEnabled(false);
            stampNameField.setText(DEFAULT_STAMP_NAME);
            stateLabel.setText("医薬品を入力してください");
            setValid(false);

        } else {
            int index = table.getSelectedRow();
            removeButton.setEnabled(tableModel.getObject(index) != null);
            clearButton.setEnabled(true);

            if (!hasMedicine()) {
                stateLabel.setText("医薬品を入力してください");
            } else if (!hasAdmin()) {
                stateLabel.setText("用法を入力してください");
            } else if (!isNumberOk()) {
                stateLabel.setText("数量が正しくありません");
            } else {
                stateLabel.setText("カルテに展開できます");
            }

            setValid(hasMedicine() && hasAdmin() && isNumberOk());
        }
    }

    /**
     * 薬剤を含んでいるかどうか
     *
     * @return
     */
    private boolean hasMedicine() {
        return tableModel.getObjectList().stream().anyMatch(mItem -> (mItem.getClassCode() == ClaimConst.YAKUZAI));
    }

    /**
     * 用法を含んでいるかどうか
     *
     * @return
     */
    private boolean hasAdmin() {
        return tableModel.getObjectList().stream().anyMatch(mItem -> (mItem.getClassCode() == ClaimConst.ADMIN));
    }

    // １日量が妥当かどうか
    private boolean isNumberOk() {
        for (Object i : tableModel.getObjectList()) {
            MasterItem mItem = (MasterItem) i;

            // 器材または医薬品の場合，数量を調べる
            switch (mItem.getClassCode()) {
                case ClaimConst.YAKUZAI:
                case ClaimConst.ZAIRYO:
                    if (!isNumber(mItem.getNumber().trim())) {
                        return false;
                    }
                    break;

                case ClaimConst.ADMIN:
                    // バンドル数を調べる
                    if (!isNumber(mItem.getBundleNumber().trim())) {
                        return false;
                    }
                    break;

                case ClaimConst.SYUGI:
                    // 手技の場合 null "" 可
                    if (mItem.getNumber() == null || mItem.getNumber().equals("")) {
                        continue;
                    }
                    if (!isNumber(mItem.getNumber().trim())) {
                        return false;
                    }
                    break;

                default:
                    break;
            }
        }
        return true;
    }

    // number かどうか
    private boolean isNumber(String test) {
        // 部位などに使う "." と "" は許す
        if (test.equals(".") || test.equals("")) {
            return true;
        }

        try {
            float num = Float.parseFloat(test);
            if (num > 0F) {
                return true;
            }
        } catch (NumberFormatException e) {
            System.out.println("RecipeTablePanel.java: " + e);
        }
        return false;
    }
}
