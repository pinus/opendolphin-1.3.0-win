package open.dolphin.order;

import open.dolphin.client.GUIConst;
import open.dolphin.delegater.OrcaDelegater;
import open.dolphin.dto.OrcaEntry;
import open.dolphin.event.OrderListener;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.PNSPair;
import open.dolphin.helper.PNSTriple;
import open.dolphin.helper.StringTool;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.orca.ClaimConst;
import open.dolphin.order.stampeditor.StampEditor;
import open.dolphin.ui.*;
import open.dolphin.util.ModelUtils;

import javax.swing.FocusManager;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * MasterSearchPanel.
 * この Panel は OrcaEntry ベース，ItemTablePanel は MasterItem ベース.
 *
 * @author pns
 */
public class MasterSearchPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /**
     * Preferences に部分一致の on/off を記録するための key
     */
    private static final String PARTIAL_MATCH = "partialMatch";

    /**
     * キーワードフィールド用の tooltip text
     */
    private static final String TOOLTIP_KEYWORD = "漢字が使用できます";
    /**
     * キーワードフィールドの長さ
     */
    private static final int KEYWORD_FIELD_LENGTH = 30;
    /**
     * この SearchPanel の entity
     */
    private final String entity;
    /**
     * 20120519 形式の今日の日付
     */
    private final String todayDate;
    /**
     * プレファレンス
     */
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    /**
     * ItemTablePanel からリスンされる
     */
    private OrderListener<MasterItem> orderListener;
    /**
     * キーワードフィールド
     */
    private CompletableSearchField keywordField;
    /**
     * 部分一致チェックボックス
     */
    private JCheckBox partialMatchBox;
    /**
     * 件数ラベル
     */
    private JLabel countLabel;
    /**
     * 用法カテゴリ ComboBox
     */
    private JComboBox<PNSPair<String, String>> adminCombo;
    /**
     * 検索結果テーブル
     */
    private JTable table;
    /**
     * 検索結果テーブルの table model
     */
    private ObjectReflectTableModel<OrcaEntry> tableModel;

    public MasterSearchPanel(String entity) {
        super();
        this.entity = entity;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        todayDate = sdf.format(new Date());

        initComponents();
    }

    /**
     * キーワード入力テキストフィールドを返す.
     *
     * @return CompletableJTextField
     */
    public CompletableJTextField getKeywordField() {
        return keywordField;
    }
    /**
     * textfield にフォーカスを取る.
     * StampEditor#enter() から呼ばれる
     */
    public void requestFocusOnTextField() {
        Focuser.requestFocus(keywordField);
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
     * ItemTablePanel に MasterItem を伝えるリスナ.
     *
     * @param listener OrderListener
     */
    public void addOrderListener(OrderListener<MasterItem> listener) {
        orderListener = listener;
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));

        JPanel northPanel = createNorthPanel();
        tableModel = createTableModel();
        table = createTable();
        PNSScrollPane scroller = new PNSScrollPane(table);
        scroller.isPermanentScrollBar = true;

        this.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
        this.add(northPanel, BorderLayout.NORTH);
        this.add(scroller, BorderLayout.CENTER);
    }

    /**
     * キーワードフィールド，部分一致，用法選択コンボ
     *
     * @return JPanel
     */
    protected JPanel createNorthPanel() {
        JPanel panel = new JPanel();
        // 高さを 32 に固定
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 32));
        panel.setMaximumSize(new Dimension(panel.getMaximumSize().width, 32));
        panel.setMinimumSize(new Dimension(panel.getMinimumSize().width, 32));

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        ActionListener listener = e -> {
            String key = keywordField.getText().trim();
            // 全ての桁が数字 or Z の場合（＝コード検索）半角に，それ以外は全角に
            if (key.matches("^[0-9,０-９Z]+$")) {
                key = StringTool.toHankakuNumber(key);
            } else {
                key = StringTool.toZenkakuNumber(key);
                key = StringTool.toZenkakuUpperLower(key);
                key = key.replaceAll("[-−]", "－"); // ダッシュ「−」を EUC に変換可能なコード（0xEFBC8D）に
            }

            if (!key.equals("")) {
                if (partialMatchBox.isSelected()) {
                    prefs.putBoolean(PARTIAL_MATCH, true);
                    search(key);
                } else {
                    prefs.putBoolean(PARTIAL_MATCH, false);
                    search("^" + key);
                }
            }
        };

        keywordField = new CompletableSearchField(KEYWORD_FIELD_LENGTH);
        keywordField.setName(StampEditor.MASTER_SEARCH_FIELD);
        keywordField.setLabel("マスタ検索");

        keywordField.setMaximumSize(new Dimension(10, 22));
        keywordField.setToolTipText(TOOLTIP_KEYWORD);
        keywordField.addActionListener(listener);

        // 上下キーでフォーカス移動
        keywordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 38) {
                    // upper key
                    FocusManager.getCurrentManager().focusPreviousComponent();
                } else if (e.getKeyCode() == 40) {
                    // down key
                    FocusManager.getCurrentManager().focusNextComponent();
                }
            }
        });

        partialMatchBox = new JCheckBox("部分一致");
        partialMatchBox.addActionListener(listener);
        partialMatchBox.setSelected(prefs.getBoolean(PARTIAL_MATCH, false));

        adminCombo = ComboBoxFactory.createAdminCategoryCombo();
        adminCombo.setToolTipText("括弧内はコードの番号台を表します。");
        adminCombo.setMaximumSize(adminCombo.getPreferredSize());
        adminCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                int index = adminCombo.getSelectedIndex();
                String code = ComboBoxFactory.getAdminCategoryModel().get(index).getValue();
                if (!code.equals("")) {
                    search("^" + code);
                }
            }
        });
        // adminCombo は処方のときだけ有効
        if (!IInfoModel.ENTITY_MED_ORDER.equals(entity)) {
            adminCombo.setEnabled(false);
        }

        countLabel = new JLabel("0 件");
        Dimension d = countLabel.getPreferredSize();
        d.width = 70;
        countLabel.setPreferredSize(d);
        countLabel.setMaximumSize(d);
        countLabel.setMinimumSize(d);
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(Box.createHorizontalStrut(10));
        panel.add(keywordField);
        panel.add(partialMatchBox);
        panel.add(Box.createHorizontalGlue());
        panel.add(adminCombo);
        panel.add(countLabel);

        return panel;
    }

    /**
     * MasterSearchPanel のテーブルモデル.
     *
     * @return {@code ObjectReflectTableModel<OrcaEntry>}
     */
    protected ObjectReflectTableModel<OrcaEntry> createTableModel() {
        PNSTriple<String, Class<?>, String> code = new PNSTriple<>(" コード", String.class, "getCode");
        PNSTriple<String, Class<?>, String> name = new PNSTriple<>(" 名 称", String.class, "getName");
        PNSTriple<String, Class<?>, String> unit = new PNSTriple<>(" 単 位", String.class, "getUnit");
        PNSTriple<String, Class<?>, String> ten = new PNSTriple<>(" 点数（薬価）", String.class, "getTen");
        PNSTriple<String, Class<?>, String> start = new PNSTriple<>(" 開 始", String.class, "getStartDate");
        PNSTriple<String, Class<?>, String> end = new PNSTriple<>(" 終 了", String.class, "getEndDate");

        PNSTriple<String, Class<?>, String> category = IInfoModel.ENTITY_DIAGNOSIS.equals(entity)
                ? new PNSTriple<>(" ICD10", String.class, "getIcd10")
                : new PNSTriple<>(" 療 区", String.class, "getClaimClassCode");

        return new ObjectReflectTableModel<>(Arrays.asList(code, name, category, unit, ten, start, end));
    }

    /**
     * MasterSearchPanel のテーブル.
     *
     * @return JTable
     */
    protected JTable createTable() {
        int[] width = new int[]{90, 200, 50, 60, 80, 100, 100};

        table = new JTable(tableModel);
        table.setName(StampEditor.MASTER_TABLE);
        table.putClientProperty("Quaqua.Table.style", "striped");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow == -1) { return; }

                table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                sendSelectedToItemTablePanel();
            }
        });

        // ENTER/SPACE で選択データを ItemTablePanel に送る
        InputMap im = table.getInputMap();
        ActionMap am = table.getActionMap();
        im.put(KeyStroke.getKeyStroke("ENTER"), "sendData");
        im.put(KeyStroke.getKeyStroke("SPACE"), "sendData");
        am.put("sendData", new ProxyAction(this::sendSelectedToItemTablePanel));

        // UP キーでシームレスに search field へ移動する
        im.put(KeyStroke.getKeyStroke("UP"), "selectPreviousRow");
        Action up = am.get("selectPreviousRow");
        am.put("selectPreviousRow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row == 0) { FocusManager.getCurrentManager().focusPreviousComponent(); }
                else { up.actionPerformed(e); }
            }
        });

        // focus 移動
        im.put(KeyStroke.getKeyStroke("TAB"), "focusNext");
        am.put("focusNext", new ProxyAction(FocusManager.getCurrentManager()::focusNextComponent));
        im.put(KeyStroke.getKeyStroke("shift TAB"), "focusPrevious");
        am.put("focusPrevious", new ProxyAction(FocusManager.getCurrentManager()::focusPreviousComponent));

        // 列幅を設定する
        TableColumn column;
        int len = width.length;
        for (int i = 0; i < len; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width[i]);
            // 名称コラム以外は固定
            if (i != 1) {
                column.setMaxWidth(width[i]);
            }
        }

        // レンダラ
        table.setDefaultRenderer(Object.class, new MasterTableRenderer());
        // sorter
        table.setRowSorter(new MasterTableSorter(tableModel));

        return table;
    }

    /**
     * 選択された OrcaEntry を MasterItem に変換して ItemTablePanel に送る.
     */
    private void sendSelectedToItemTablePanel() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) { return; }

        OrcaEntry o = tableModel.getObject(table.convertRowIndexToModel(selectedRow));
        if (o != null) {
            MasterItem mItem = new MasterItem();

            // claim 003 コード
            String code = o.getCode();
            if (code.startsWith(ClaimConst.ADMIN_CODE_START)) {
                // 部位コード 001000800-999，コメント 0010000 00-99 は薬剤コードで登録する
                if (code.matches("^001000[089].*")) {
                    mItem.setClassCode(ClaimConst.YAKUZAI);
                } else {
                    mItem.setClassCode(ClaimConst.ADMIN);
                }
            } else if (code.startsWith(ClaimConst.YAKUZAI_CODE_START)) {
                mItem.setClassCode(ClaimConst.YAKUZAI);
            } else if (code.startsWith(ClaimConst.ZAIRYO_CODE_START)) {
                mItem.setClassCode(ClaimConst.ZAIRYO);
            } else {
                mItem.setClassCode(ClaimConst.SYUGI);
            }

            mItem.setCode(code);
            mItem.setName(o.getName());
            mItem.setUnit(o.getUnit());
            mItem.setClaimClassCode(o.getClaimClassCode());
            mItem.setYkzKbn(o.getYkzkbn());

            if (IInfoModel.ENTITY_DIAGNOSIS.equals(entity)) {
                mItem.setMasterTableId(ClaimConst.DISEASE_MASTER_TABLE_ID);
            }
            // ItemTablePanel に通知
            orderListener.order(mItem);

            // 用法コンボを元に戻す
            adminCombo.setSelectedIndex(0);
        }
    }

    /**
     * ORCA でキーワードを検索して OrcaEntry を取ってきて table にセットする
     *
     * @param key Keyword
     */
    private void search(String key) {

        // スクロール状態で再検索されたとき，先頭から表示されるようにする
        tableModel.clear();
        ((JComponent) table.getParent()).scrollRectToVisible(new Rectangle(0, 0, 0, 0));

        OrcaDelegater delegater = new OrcaDelegater();

        if (IInfoModel.ENTITY_DIAGNOSIS.equals(entity)) {
            tableModel.addRows(delegater.findDiagnosis(key));
        } else {
            tableModel.addRows(delegater.findTensu(key));
        }

        countLabel.setText(tableModel.getRowCount() + " 件");
    }

    /**
     * Master のレンダラ.
     */
    private class MasterTableRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private final int TENSU_COL = 4;
        private final int START_COL = 5;
        private final int END_COL = 6;

        public MasterTableRenderer() {
            init();
        }

        private void init() {
            setBorder(GUIConst.RENDERER_BORDER_NARROW);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            comp.setBorder(null);

            String endDate = (String) table.getValueAt(row, END_COL);

            // 色の決定
            if (isSelected) {
                if (table.isFocusOwner()) {
                    comp.setBackground(table.getSelectionBackground());
                    comp.setForeground(table.getSelectionForeground());
                } else {
                    comp.setBackground((Color) table.getClientProperty("JTable.backgroundOffFocus"));
                    comp.setForeground(table.getForeground());
                }
                // out of date
                if (todayDate.compareTo(endDate) > 0) {
                    comp.setForeground(Color.GRAY);
                }

            } else {
                if (todayDate.compareTo(endDate) <= 0) {
                    comp.setForeground(table.getForeground());
                    comp.setBackground(table.getBackground());
                } else {
                    comp.setForeground(Color.GRAY);
                    comp.setBackground(table.getBackground());
                }
            }

            String text = (value == null) ? "" : String.valueOf(value);
            text = StringTool.toHankakuNumber(text);
            text = StringTool.toHankakuUpperLower(text);
            text = text.replaceAll("　", " ");

            if ("99999999".equals(text) || "00000000".equals(text) || "0.00".equals(text)) {
                text = "-";
            }

            // 日付の表示形式
            if (column == START_COL || column == END_COL) {
                text = ModelUtils.toDolphinDateString(text);
                if (text == null) {
                    text = "-";
                }
            }

            // 点数コラムは右寄せ
            if (column == TENSU_COL) {
                comp.setText(text + " "); // 偽インデント
                comp.setHorizontalAlignment(JLabel.RIGHT);

                // それ以外は左寄せ
            } else {
                comp.setText(" " + text); // 偽インデント
                comp.setHorizontalAlignment(JLabel.LEFT);
            }

            return comp;
        }
    }

    private class MasterTableSorter extends TableRowSorter<ObjectReflectTableModel<OrcaEntry>> {

        private MasterTableSorter(final ObjectReflectTableModel<OrcaEntry> tableModel) {
            super(tableModel);
        }

        // ASCENDING -> DESENDING -> 初期状態 と切り替える
        @Override
        public void toggleSortOrder(int column) {
            if (column >= 0 && column < getModelWrapper().getColumnCount() && isSortable(column)) {
                List<? extends SortKey> keys = getSortKeys();
                if (!keys.isEmpty()) {
                    SortKey sortKey = keys.get(0);
                    if (sortKey.getColumn() == column && sortKey.getSortOrder() == SortOrder.DESCENDING) {
                        setSortKeys(null);
                        return;
                    }
                }
            }
            super.toggleSortOrder(column);
        }
    }
}
