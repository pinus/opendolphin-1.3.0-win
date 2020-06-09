package open.dolphin.order.tablepanel;

import open.dolphin.client.GUIConst;
import open.dolphin.dnd.MasterItemTransferHandler;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.PNSTriple;
import open.dolphin.helper.StringTool;
import open.dolphin.infomodel.*;
import open.dolphin.orca.ClaimConst;
import open.dolphin.order.IStampEditor;
import open.dolphin.order.MasterItem;
import open.dolphin.order.stampeditor.StampEditor;
import open.dolphin.project.Project;
import open.dolphin.ui.*;
import open.dolphin.ui.sheet.JSheet;

import javax.swing.FocusManager;
import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * ItemTablePanel.
 * BaseCharge, General, Injection, InstructionCharge, Other, Physiology,
 * Surgery, Test, Treatment の StampEditor で共通して使う TablePanel.
 * <p>
 * RecipeTablePanel, DiagnosisTablePanel, RadiologyTablePanel は，
 * これをベースに extend して作る.
 * <pre>
 * +---------------+
 * |               |
 * |  CenterPanel  | JTable or JTable + RadiologyMethodPanel
 * |               |
 * |               |
 * +---------------+
 * |  SouthPanel   | stampNameField, deleteButton, clearButton etc
 * +---------------+
 * </pre>
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class ItemTablePanel extends JPanel {
    public static final String DEFAULT_STAMP_NAME = "新規スタンプ";
    public static final String FROM_EDITOR_STAMP_NAME = "エディタから";
    public static final String DEFAULT_NUMBER = "1";
    public static final String TOOLTIP_DELETE_TEXT = "選択したアイテムを削除します。";
    public static final String TOOLTIP_CLEAR_TEXT = "セット内容をクリアします。";
    public static final String TOOLTIP_DND_TEXT = "ドラッグ & ドロップで順番を入れ替えることができます。";
    // ラベルテキスト
    public static final String NUMBER_LABEL_TEXT = "回 数";
    public static final String SET_NAME_LABEL_TEXT = "セット名：";
    public static final String MEMO_LABEL_TEXT = "メ モ";
    private static final long serialVersionUID = 1L;
    // 数量コンボ用のデータを生成する
    private static String[] NUMBER_LIST;

    static {
        NUMBER_LIST = new String[31];
        for (int i = 0; i < 31; i++) {
            NUMBER_LIST[i] = String.valueOf(i + 1);
        }
    }

    public final ImageIcon REMOVE_BUTTON_IMAGE = GUIConst.ICON_REMOVE_16;
    public final ImageIcon CLEAR_BUTTON_IMAGE = GUIConst.ICON_ERASER_16;
    // GUI コンポーネント
    private JTable table;
    private ObjectReflectTableModel<MasterItem> tableModel;
    private int[] tableColumnWidth;
    private JTextField stampNameField;
    private JTextField commentField;
    private JComboBox<String> numberCombo;
    private JButton removeButton;
    private JButton clearButton;

    private IStampEditor parent;

    // CLAIM 関係
    //private boolean findClaimClassCode; // 診療行為区分を診療行為アイテムから取得するとき true
    private String orderName;           // ドルフィンのオーダ履歴用の名前 (ClaimBundle#OrderName)
    private String classCode;           // 診療行為区分 400,500,600 .. etc
    private String classCodeId;         // 診療行為区分定義のテーブルID == Claim007
    private String subclassCodeId;      // == Claim003
    private String entity;              //

    public ItemTablePanel(IStampEditor parent) {
        super(new BorderLayout());
        this.parent = parent;
        initComponents();
    }

    /**
     * TableModel, Components, Panel Arrangement.
     */
    private void initComponents() {

        // テーブルモデル作成
        tableModel = createTableModel();
        // 共通コンポネント作成
        createCommonComponents();
        // テーブル部分（RadItemTablePanel では，RadiologyMethod が加わる
        JComponent centerPanel = createCenterPanel();
        // テーブル下のパネル作成
        JComponent southPanel = createSouthPanel();

        // 全体を配置する
        this.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(southPanel, BorderLayout.SOUTH);
        this.setPreferredSize(new Dimension(GUIConst.DEFAULT_EDITOR_WIDTH, GUIConst.DEFAULT_EDITOR_HEIGHT));
    }

    /**
     * TableModel を返す.
     *
     * @return {@code ObjectReflectTableModel<MasterItem>}
     */
    public ObjectReflectTableModel<MasterItem> createTableModel() {
        // セットテーブルのモデルを生成する
        List<PNSTriple<String, Class<?>, String>> reflectList = Arrays.asList(
                new PNSTriple<>(" コード", String.class, "getCode"),
                new PNSTriple<>("　診療内容", String.class, "getName"),
                new PNSTriple<>(" 数 量", String.class, "getNumber"),
                new PNSTriple<>(" 単 位", String.class, "getUnit")
        );
        setTableColumnWidth(new int[]{90, 200, 60, 60});

        return new ObjectReflectTableModel<MasterItem>(reflectList) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) {
                // col=0 がコメントコード（810000001)なら，col=1 を編集可能とする
                // col=2 なら編集可能
                return (col == 1 && "810000001".equals(this.getValueAt(row, 0)))
                        || (col == 2);
            }

            @Override
            public void setValueAt(Object o, int row, int col) {
                //if (o == null || ((String) o).trim().equals("")) return;
                // MasterItem に数量を設定する
                MasterItem mItem = getObject(row);

                if (col == 2 && mItem != null) {
                    mItem.setNumber((String) o);
                    // 状態をチェックして，ボタン制御＋parent に伝える
                    checkState();
                }
                // MasterItem に診療内容（入力したコメント）を設定する
                if (col == 1 && mItem != null) {
                    mItem.setName((String) o);
                    // 状態をチェックして，ボタン制御＋parent に伝える
                    checkState();
                }
            }
        };
    }

    /**
     * 共通コンポネント作成.
     */
    private void createCommonComponents() {

        table = new JTable(tableModel);
        table.setName(StampEditor.ITEM_TABLE);
        table.putClientProperty("Quaqua.Table.style", "striped");
        table.setTransferHandler(new MasterItemTransferHandler()); // TransferHandler
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setDefaultRenderer(Object.class, new TablePanelRenderer());

        final int[] columnWidth = getTableColumnWidth();

        table.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int ctrlMask = InputEvent.CTRL_DOWN_MASK;
                int action = ((e.getModifiersEx() & ctrlMask) == ctrlMask)
                        ? TransferHandler.COPY
                        : TransferHandler.MOVE;
                JTable c = (JTable) e.getSource();
                // 非選択状態からいきなりドラッグを開始すると cellEditor が残ってしまう問題の workaround
                for (int i = 0; i < columnWidth.length; i++) {
                    javax.swing.CellEditor ce = c.getColumnModel().getColumn(i).getCellEditor();
                    if (ce != null) {
                        ce.stopCellEditing();
                    }
                }

                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, action);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 選択モード
        table.setRowSelectionAllowed(true);

        table.getSelectionModel().addListSelectionListener(e -> {
            // 普通に select しても true を１回呼んだ後，知らんプリすることがあるのの workaround
            // true と false で notifySelectedRow() が２回呼ばれてしまうが，この場合問題ない
            //if (e.getValueIsAdjusting() == false) {
            int index = table.getSelectedRow();
            boolean b = tableModel.getObject(index) != null;
            removeButton.setEnabled(b);
        });
        table.setToolTipText(TOOLTIP_DND_TEXT);

        // カラムの cell editor とカラム幅を設定
        for (int i = 0; i < columnWidth.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);

            JTextField tf = new JTextField();
            IMEControl.setImeOffIfFocused(tf);
            DefaultCellEditor de = new PNSCellEditor(tf);
            int ccts = Project.getPreferences().getInt("order.table.clickCountToStart", 1);
            de.setClickCountToStart(ccts);
            column.setCellEditor(de);

            // カラム幅を固定しないカラム
            String[] openColumn = new String[] {"診療内容", "疾患名"};

            // カラム幅を設定する
            column.setPreferredWidth(columnWidth[i]);
            if (Stream.of(openColumn).noneMatch(tableModel.getColumnName(i)::contains)) {
                column.setMaxWidth(columnWidth[i]); // 固定する
            }
        }

        // 数量コンボを設定する
        numberCombo = new JComboBox<>(NUMBER_LIST);

        // コメントフィールド（メモ）を生成する
        commentField = new JTextField(15);
        commentField.setMaximumSize(new Dimension(10, 22));
        IMEControl.setImeOnIfFocused(commentField);

        // スタンプ名フィールドを生成する
        stampNameField = new JTextField(20);
        stampNameField.setMaximumSize(new Dimension(10, 22));
        // stampNameField.setOpaque(true); opaque にすると，色が枠からはみ出す
        //stampNameField.setBackground(new Color(251, 239, 128));  // TODO
        IMEControl.setImeOnIfFocused(stampNameField);

        // 削除ボタンを生成する
        removeButton = new JButton(REMOVE_BUTTON_IMAGE);
        removeButton.setEnabled(false);
        removeButton.addActionListener(e -> removeSelectedItem());
        removeButton.setToolTipText(TOOLTIP_DELETE_TEXT);

        //　DELETE キーで remove ボタンを押す
        InputMap im = table.getInputMap();
        ActionMap am = table.getActionMap();
        im.put(KeyStroke.getKeyStroke("BACK_SPACE"), "remove");
        am.put("remove", new ProxyAction(removeButton::doClick));

        // DOWN キーでシームレスに search field へ移動する
        im.put(KeyStroke.getKeyStroke("DOWN"), "selectNextRow");
        Action down = am.get("selectNextRow");
        am.put("selectNextRow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRow() == table.getRowCount() - 1) {
                    FocusManager.getCurrentManager().focusNextComponent();
                } else {
                    down.actionPerformed(e);
                }
            }
        });

        // focus 移動
        im.put(KeyStroke.getKeyStroke("TAB"), "focusNext");
        am.put("focusNext", new ProxyAction(FocusManager.getCurrentManager()::focusNextComponent));
        im.put(KeyStroke.getKeyStroke("shift TAB"), "focusPrevious");
        am.put("focusPrevious", new ProxyAction(FocusManager.getCurrentManager()::focusPreviousComponent));

        // ENTER キーで cell editor を activate する
        im.put(KeyStroke.getKeyStroke("ENTER"), "startEditing");
        am.put("startEditing", new ProxyAction(() -> {
            int row = table.getSelectedRow();
            if (row < 0) { return; }
            MasterItem item = tableModel.getObject(row);

            int activeCol = item.getCode().startsWith("810")
                ? 1 // コメントコードの場合 col=1 (診療内容) を activate
                : item.getCode().startsWith("6") || item.getCode().startsWith("160")
                ? 2 // 薬剤 or 検査コードの場合 col=2 (数量)
                : item.getCode().startsWith("001000")
                ? 5 // 用法コードの場合 col=5 (回数)
                : 0;

            if (table.isCellEditable(row, activeCol)) {
                PNSCellEditor editor = (PNSCellEditor) table.getColumnModel().getColumn(activeCol).getCellEditor();
                Focuser.requestFocus(editor.getComponent());
                table.editCellAt(row, activeCol);
            }
        }));

        // SHIFT+UP/DOWN で項目を上下に移動する
        im.put(KeyStroke.getKeyStroke("shift UP"), "moveUp");
        am.put("moveUp", new ProxyAction(() -> {
            int row = table.getSelectedRow();
            if (row <= 0) { return; }
            // 上の項目と入れ替えて，選択を1つあげる
            MasterItem target = tableModel.getObject(row);
            tableModel.deleteRow(row);
            tableModel.addRow(row - 1, target);
            table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
        }));

        im.put(KeyStroke.getKeyStroke("shift DOWN"), "moveDown");
        am.put("moveDown", new ProxyAction(() -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= table.getRowCount() - 1) { return; }
            // 下の項目と入れ替えて，選択を1つさげる
            MasterItem target = tableModel.getObject(row);
            tableModel.deleteRow(row);
            tableModel.addRow(row + 1, target);

            table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
        }));

        // クリアボタンを生成する
        clearButton = new JButton(CLEAR_BUTTON_IMAGE);
        clearButton.setEnabled(false);
        clearButton.addActionListener(e -> {
            int ans = JSheet.showOptionDialog(SwingUtilities.getWindowAncestor(table), "クリアしますか？", "",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
                    new String[]{"はい", "いいえ", "キャンセル"}, "はい");

            if (ans == 0) {
                tableModel.clear();
            }
        });
        clearButton.setToolTipText(TOOLTIP_CLEAR_TEXT);
    }

    /**
     * テーブルを含むパネルを作成.
     * RadiologyTablePanel の RadiologyMethod はここで加える.
     *
     * @return テーブルを含む PNSScrollPane
     */
    public JComponent createCenterPanel() {
        // スクローラ
        PNSScrollPane scroller = new PNSScrollPane(table);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //scroller.setPreferredSize(new Dimension(GUIConst.DEFAULT_EDITOR_WIDTH, GUIConst.DEFAULT_EDITOR_HEIGHT));

        return scroller;
    }

    /**
     * テーブル下のコンポネントを作る.
     *
     * @return テーブル下の JPanel
     */
    public JPanel createSouthPanel() {
        // 南パネルを生成する
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(SET_NAME_LABEL_TEXT));
        panel.add(stampNameField);

        panel.add(new JLabel(NUMBER_LABEL_TEXT));
        panel.add(numberCombo);

        panel.add(new JLabel(MEMO_LABEL_TEXT));
        panel.add(commentField);
        panel.add(Box.createHorizontalGlue());
        panel.add(removeButton);
        panel.add(clearButton);

        return panel;
    }

    /**
     * Table にフォーカスを取る.
     */
    public void requestFocusOnTable() {
        if (table.getModel().getRowCount() == 0) {
            // 先送り
            SwingUtilities.invokeLater(() -> FocusManager.getCurrentManager().focusNextComponent(table));
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            table.getSelectionModel().setSelectionInterval(0, 0);
        }
        Focuser.requestFocus(table);
    }

    /**
     * テーブルのコラム幅.
     * これをオーバーライドするとテーブルのコラム幅が変更できる.
     *
     * @return TableColumnWidth
     */
    public int[] getTableColumnWidth() {
        return tableColumnWidth;
    }

    /**
     * テーブルのコラム幅を設定する.
     *
     * @param columnWidth コラム幅
     */
    public void setTableColumnWidth(int[] columnWidth) {
        this.tableColumnWidth = columnWidth;
    }

    /**
     * テーブルを返す.
     *
     * @return JTable
     */
    public JTable getTable() {
        return table;
    }

    /**
     * テーブルをセット.
     *
     * @param table JTable
     */
    public void setTable(JTable table) {
        this.table = table;
    }

    /**
     * テーブルモデルを返す.
     *
     * @return ObjectTableModel
     */
    public ObjectReflectTableModel<MasterItem> getTableModel() {
        return tableModel;
    }

    /**
     * テーブルモデルをセットする.
     *
     * @param tableModel ObjectReflectTableModel
     */
    public void setTableModel(ObjectReflectTableModel<MasterItem> tableModel) {
        this.tableModel = tableModel;
    }

    /**
     * StampNameField(JTextField) を返す.
     *
     * @return StampNameField
     */
    public JTextField getStampNameField() {
        return stampNameField;
    }

    /**
     * スタンプ名フィールドのテキストを返す.
     *
     * @return stamp name
     */
    public String getStampName() {
        return stampNameField.getText();
    }

    /**
     * スタンプ名フィールド.
     *
     * @param stampName stamp name
     */
    public void setStampName(String stampName) {
        stampNameField.setText(stampName);
    }

    /**
     * コメントフィールドのテキストを返す.
     *
     * @return comment field text
     */
    public String getComment() {
        return commentField.getText();
    }

    /**
     * コメントフィールドにテキストを設定する.
     *
     * @param comment テキスト
     */
    public void setComment(String comment) {
        commentField.setText(comment);
    }

    /**
     * BundleNumber を numberCombo から調べて返す.
     *
     * @return bundle number
     */
    public String getBundleNumber() {
        return (String) numberCombo.getSelectedItem();
    }

    /**
     * BundleNumber を numberCombo にセットする.
     *
     * @param val bundle number
     */
    public void setBundleNumber(String val) {
        numberCombo.setSelectedItem(val);
    }

    public JButton getRemoveButton() {
        return removeButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    /**
     * 親の StampEditor を返す.
     *
     * @return StampEditor
     */
    public IStampEditor getMyParent() {
        return parent;
    }

    /**
     * 親の StampEditor をセットする.
     *
     * @param parent StampEditor
     */
    public void setMyParent(IStampEditor parent) {
        this.parent = parent;
    }

    /**
     * エンティティー（IInfoModel.ENTITY_XXX）を返す.
     *
     * @return Entity
     */
    public String getEntity() {
        return entity;
    }

    /**
     * エンティティー（IInfoModel.ENTITY_XXX）をセットする.
     *
     * @param val Entity
     */
    public void setEntity(String val) {
        entity = val;
    }

    /**
     * Valid model かどうかをセットする.
     * これをセットすると，StampEditor の setValid が呼ばれて，
     * そこで登録されたリスナに伝達される.
     *
     * @param valid validity
     */
    public void setValid(boolean valid) {
        getMyParent().setValid(valid);
    }

    /**
     * OrderName を返す (ClaimBundle#OrderName).
     * ClaimConst.ClaimsSpec.BASE_CHARGE 等に入っている名前.
     * 処方はここに入ってない.
     *
     * @return order name
     */
    public String getOrderName() {
        return orderName;
    }

    /**
     * OrderName をセットする (ClaimBundle#OrderName).
     *
     * @param val order name
     */
    public void setOrderName(String val) {
        orderName = val;
    }

    /**
     * ClaimClassCode を返す.
     *
     * @return claim class code
     */
    public String getClassCode() {
        return classCode;
    }

    /**
     * ClaimClassCode をセットする.
     *
     * @param val claim class code
     */
    public void setClassCode(String val) {
        classCode = val;
    }

    /**
     * ClaimClassCodeSystem を返す（Claim007固定）.
     *
     * @return claim class code system
     */
    public String getClassCodeId() {
        return classCodeId;
    }

    /**
     * ClaimClassCodeSystem をセットする（Claim007固定）.
     *
     * @param val claim class code system
     */
    public void setClassCodeId(String val) {
        classCodeId = val;
    }

    /**
     * ClaimItem.ClassCodeSystem を返す（Claim003固定）.
     *
     * @return class code system
     */
    public String getSubClassCodeId() {
        return subclassCodeId;
    }

    /**
     * ClaimItem.ClassCodeSystem をセットする（Claim003固定）.
     *
     * @param val class code system
     */
    public void setSubClassCodeId(String val) {
        subclassCodeId = val;
    }

    /**
     * テーブルをクリアする.
     */
    public void clear() {
        tableModel.clear();
        // 状態をチェックして，ボタン制御＋parent に伝える
        checkState();
    }

    /**
     * 選択した行の項目を削除する.
     */
    public void removeSelectedItem() {
        int row = table.getSelectedRow();
        if (tableModel.getObject(row) != null) {
            // cell editor が active になった状態で delete すると editor が残るのを防ぐ
            javax.swing.CellEditor ce = table.getCellEditor();
            if (ce != null) {
                ce.cancelCellEditing();
            }
            tableModel.deleteRow(row);
            // 状態をチェックして，ボタン制御＋parent に伝える
            checkState();
        }
    }

    /**
     * ClaimItem の NumberCode を返す.
     * MasterItem ClassCode（手技，材料，薬剤，用法）の種類で，
     * 手技の場合「材料個数」，それ以外の場合「薬剤投与量１回」を返す.
     * <p>
     * Returns Claim004 Number Code 21 材料個数 when subclassCode = 1 11.
     * 薬剤投与量（１回）when subclassCode = 2 -- カスタム.
     *
     * @param masterItemClassCode master item class code
     * @return number code
     */
    private String getNumberCode(int masterItemClassCode) {
        return (masterItemClassCode == 1) ? ClaimConst.ZAIRYO_KOSU : ClaimConst.YAKUZAI_TOYORYO_1KAI;
    }

    /**
     * マスターテーブルで選択されたアイテムの通知を受け，セットテーブルへ追加する.
     * ItemTablePanel, RadiologyTablePanel 共通.
     *
     * @param item MasterItem
     */
    public void receiveMaster(MasterItem item) {

        String textVal = stampNameField.getText().trim();

        // マスターアイテムを判別して自動設定を行う
        switch (item.getClassCode()) {
            // 手技：class code = 0
            case ClaimConst.SYUGI:
                // 材料及び薬剤の場合は数量1を設定する
                //item.setNumber(DEFAULT_NUMBER);
                if (textVal.equals("") || textVal.equals(DEFAULT_STAMP_NAME)) {
                    // 手技の場合はスタンプ名フィールドに名前を設定する
                    stampNameField.setText(item.getName());
                }
                break;

            // 薬剤：class code = 2
            case ClaimConst.YAKUZAI:
                String inputNum = "1";
                if (item.getUnit() != null) {
                    String unit = item.getUnit();
                    switch (unit) {
                        case "錠":
                            inputNum = Project.getPreferences().get("defaultZyozaiNum", "1");
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
                break;

            // 材料：class code = 1
            case ClaimConst.ZAIRYO:
                item.setNumber(DEFAULT_NUMBER);
                break;

            default:
                break;
        }

        tableModel.addRow(item);
        // 状態をチェックして，ボタン制御＋parent に伝える
        checkState();
    }

    /**
     * エディタで編集したスタンプの値を返す.
     * ItemTablePanel, RadiologyTablePanel 共通.
     *
     * @return スタンプ(ModuleMode = ModuleInfo + InfoModel)
     */
    public Object getValue() {

        // 常に新規のモデルとして返す
        ModuleModel retModel = new ModuleModel();
        ModuleInfoBean moduleInfo = retModel.getModuleInfo();
        moduleInfo.setEntity(getEntity());
        moduleInfo.setStampRole(IInfoModel.ROLE_P);

        // スタンプ名を設定する
        String text = stampNameField.getText().trim();
        if (!text.equals("")) {
            moduleInfo.setStampName(text);
        } else {
            moduleInfo.setStampName(DEFAULT_STAMP_NAME);
        }

        // BundleDolphin を生成する
        BundleDolphin bundle = new BundleDolphin();

        // Dolphin Appli で使用するオーダ名称を設定する
        // StampHolder で使用される（タブ名に相当）
        bundle.setOrderName(getOrderName());

        // セットテーブルのマスターアイテムを取得する
        List<MasterItem> itemList = tableModel.getObjectList();

        if (itemList != null) {

            // 診療行為があるかどうかのフラグ
            // boolean found = false;

            for (MasterItem mItem : itemList) {

                ClaimItem item = new ClaimItem();

                // 名称，コードを設定する
                item.setName(mItem.getName()); // 名称
                item.setCode(mItem.getCode()); // コード

                // 手技0／材料1／薬剤2／用法3   mItem が保持を設定する
                String subclassCode = String.valueOf(mItem.getClassCode());
                item.setClassCode(subclassCode); //  ClaimItem#classCode には１桁の数字が入る
                item.setClassCodeSystem(subclassCodeId); // == Claim003

                // 診療行為コードを取得する
                // 最初に見つかった手技の診療行為コードをCLAIMに設定する
                // Dolphin Project の決定事項
                // if (isFindClaimClassCode() && (mItem.getClassCode() == ClaimConst.SYUGI) && (!found)) {
                // classCode が設定されていない場合，MasterItem から classCode を取得する
                if (classCode == null && (mItem.getClassCode() == ClaimConst.SYUGI)) {

                    if (mItem.getClaimClassCode() != null) {

                        // 注射の場合，点数集計先コードから新たに診療行為コードを生成する
                        // ------- INJECTION_311 とかになることはないのでは？
                        // Kirishima ver. より
                        switch (mItem.getClaimClassCode()) {
                            case ClaimConst.INJECTION_311:
                                classCode = ClaimConst.INJECTION_310;
                                break;

                            case ClaimConst.INJECTION_321:
                                classCode = ClaimConst.INJECTION_320;
                                break;

                            case ClaimConst.INJECTION_331:
                                classCode = ClaimConst.INJECTION_330;
                                break;

                            default:
                                // 注射以外のケース
                                classCode = mItem.getClaimClassCode();
                                break;
                        }
                    }
                }

                String number = mItem.getNumber();
                if (number != null) {
                    number = number.trim();
                    if (!number.equals("")) {
                        number = StringTool.toHankakuNumber(number);
                        item.setNumber(number);
                        item.setUnit(mItem.getUnit());
                        item.setNumberCode(getNumberCode(mItem.getClassCode()));
                        item.setNumberCodeSystem(ClaimConst.NUMBER_CODE_ID);
                    }
                }
                bundle.addClaimItem(item);
            }
        }

        // バンドルメモ
        String memo = commentField.getText();
        if (!memo.equals("")) {
            bundle.setMemo(memo);
        }

        // バンドル数
        bundle.setBundleNumber((String) numberCombo.getSelectedItem());
        bundle.setClassCode(classCode); // 診療行為区分（３桁の数字）が入る
        bundle.setClassCodeSystem(classCodeId); // Claim007 固定の値
        bundle.setClassName(ClaimConst.getSrysyukbnName(classCode)); // 上記テーブルで定義されている診療行為の名称

        retModel.setModel(bundle);

        return retModel;
    }

    /**
     * 編集するスタンプの内容を表示する.
     * ItemTablePanel, RadiologyTablePanel 共通
     *
     * @param theStamp ModuleModel - 編集するスタンプ，戻り値は常に新規スタンプである.
     */
    public void setValue(Object theStamp) {

        // 連続して編集される場合があるのでテーブル内容等をクリアする
        clear();

        // null であればリターンする
        if (theStamp == null) {
            // 状態をチェックして，ボタン制御＋parent に伝える
            checkState();
            return;
        }

        // 引数で渡された Stamp をキャストする
        ModuleModel target = (ModuleModel) theStamp;

        // Entityを保存する
        setEntity(target.getModuleInfo().getEntity());

        // Stamp 名と表示形式を設定する
        String stampName = target.getModuleInfo().getStampName();
        boolean serialized = target.getModuleInfo().isSerialized();

        // スタンプ名がエディタから発行の場合はデフォルトの名称にする
        if (!serialized && stampName.startsWith(FROM_EDITOR_STAMP_NAME)) {
            stampName = DEFAULT_STAMP_NAME;
        } else if (stampName.equals("")) {
            stampName = DEFAULT_STAMP_NAME;
        }
        stampNameField.setText(stampName);

        // Model を表示する
        BundleDolphin bundle = (BundleDolphin) target.getModel();
        if (bundle == null) {
            return;
        }

        // 診療行為区分を保存
        classCode = bundle.getClassCode();

        ClaimItem[] items = bundle.getClaimItem();
        int count = items.length;

        for (ClaimItem item : items) {

            MasterItem mItem = new MasterItem();

            // 手技・材料・薬品のフラグ
            String val = item.getClassCode();
            if (Objects.nonNull(val)) {
                mItem.setClassCode(Integer.parseInt(val));
            } else {
                mItem.setClassCode(ClaimConst.SYUGI);
            }

            // Name Code TableId
            mItem.setName(item.getName());
            mItem.setCode(item.getCode());

            val = item.getNumber();
            if (val != null && (!val.equals(""))) {
                val = StringTool.toHankakuNumber(val.trim());
                mItem.setNumber(val);
                val = item.getUnit();
                if (val != null) {
                    mItem.setUnit(val);
                }
            }

            // Show item
            tableModel.addRow(mItem);
        }

        // Bundle Memo
        String memo = bundle.getMemo();
        if (memo != null) {
            commentField.setText(memo);
        }

        String number = bundle.getBundleNumber();
        if (number != null && (!number.equals(""))) {
            number = StringTool.toHankakuNumber(number);
            numberCombo.setSelectedItem(number);
        }

        // 状態をチェックするして，parent に伝える
        checkState();
    }

    /**
     * データが正しいかどうか判定してボタンコントロール＋parent へ伝える
     * ItemTablePanel, RadiologyTablePanel 共通
     */
    public void checkState() {
        // empty
        if (tableModel.getObjectCount() == 0) {
            removeButton.setEnabled(false);
            clearButton.setEnabled(false);
            stampNameField.setText(DEFAULT_STAMP_NAME);

            setValid(false);

        } else {
            int index = table.getSelectedRow();
            removeButton.setEnabled(tableModel.getObject(index) != null);
            clearButton.setEnabled(true);

            setValid(hasSyugi() && isNumberOk());
        }
    }

    // 手技を含んでいる必要がある
    private boolean hasSyugi() {
        return tableModel.getObjectList().stream().anyMatch(mItem -> (mItem.getClassCode() == ClaimConst.SYUGI));
    }

    // number が正しく入っている必要がある
    private boolean isNumberOk() {
        for (Object i : tableModel.getObjectList()) {
            MasterItem mItem = (MasterItem) i;

            // コードが 84xxxxxxx コメントの場合，number にパラメータを入れるので，number チェックしない
            if (mItem.getCode().startsWith("84")) {
                return true;
            }
            //System.out.println("---- code= " + mItem.getCode().substring(0,2));

            // 手技の場合
            if (mItem.getClassCode() == ClaimConst.SYUGI) {
                // null "" ok
                if (mItem.getNumber() == null || mItem.getNumber().equals("")) {
                    continue;
                } else if (!isNumber(mItem.getNumber())) {
                    return false;
                }

            } else {
                // 医薬品及び器材の場合は数量をチェックする
                if (!isNumber(mItem.getNumber())) {
                    return false;
                }
            }
        }
        return true;
    }

    // number かどうか
    private boolean isNumber(String test) {
        if (!test.equals(".")) {
            try {
                float num = Float.parseFloat(test);
                if (num > 0F) {
                    return true;
                }
            } catch (NumberFormatException e) {
            }
        }
        return false;
    }
}
