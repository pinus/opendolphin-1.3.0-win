package open.dolphin.order;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import open.dolphin.client.GUIConst;
import open.dolphin.dao.OrcaEntry;
import open.dolphin.dao.OrcaMasterDao;
import open.dolphin.dao.SqlDaoFactory;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.table.ObjectReflectTableModel;
import open.dolphin.ui.AdditionalTableSettings;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.IMEControl;
import open.dolphin.ui.MyJScrollPane;
import open.dolphin.util.StringTool;

/**
 * MasterSearchPanel
 * この Panel は OrcaEntry ベース，ItemTablePanel は MasterItem ベース
 * @author pns
 */
public class MasterSearchPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    /** マスタ項目選択プロパティ名，リスナは ItemTablePanel */
    public static final String SELECTED_ITEM_PROP = "selectedItemProp";
    /** Preferences に部分一致の on/off を記録するための key */
    private static final String PARTIAL_MATCH = "partialMatch";

    /** キーワードフィールド用の tooltip text */
    private static final String TOOLTIP_KEYWORD = "漢字が使用できます";
    /** キーワードフィールドの長さ */
    private static final int KEYWORD_FIELD_LENGTH = 30;
    /** この SearchPanel の entity */
    private String entity;
    /** キーワードフィールド */
    private JTextField keywordField;
    /** 検索アイコン */
    private JLabel searchLabel;
    /** 部分一致チェックボックス */
    private JCheckBox partialMatchBox;
    /** 件数ラベル */
    private JLabel countLabel;
    /** 用法カテゴリ ComboBox */
    private JComboBox adminCombo;
    /** 用法カテゴリ */
    private static final String[] ADMIN_CATEGORY = {"用法検索","内服１回等(100)", "内服２回等(200)", "内服３回等(300)", "内服その他(400)", "頓用等(500)", "外用等(600)", "点眼等(700)","部位等(800)", "全て", "コメント", "一般名記載"};
    /** 用法カテゴリに割り当てたコード */
    private static final String[] ADMIN_CODE_RANGE = {"","0010001",  "0010002", "0010003", "0010004", "0010005", "0010006", "0010007", "0010008", "001", "810000001", "099209908"};
    /** 検索結果テーブル */
    private JTable table;
    /** 検索結果テーブルの table model */
    private ObjectReflectTableModel tableModel;
    /** 20120519 形式の今日の日付 */
    private String todayDate;
    /** プレファレンス */
    private Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    public MasterSearchPanel(String entity) {
        super();
        this.entity = entity;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        todayDate = sdf.format(new Date());

        initComponents();
    }

    /**
     * textfield にフォーカスを取る.
     * StampEditor#enter() から呼ばれる
     */
    public void requestFocusOnTextField() {
        Focuser.requestFocus(keywordField);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0,0));

        JPanel northPanel = createNorthPanel();
        tableModel = createTableModel();
        table = createTable();
        MyJScrollPane scroller = new MyJScrollPane(table);
        scroller.isPermanentScrollBar = true;
        AdditionalTableSettings.setOrderTable(table);

        this.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
        this.add(northPanel, BorderLayout.NORTH);
        this.add(scroller, BorderLayout.CENTER);
    }

    /**
     * キーワードフィールド，部分一致，用法選択コンボ
     * @return
     */
    protected JPanel createNorthPanel() {
        JPanel panel = new JPanel();
        // 高さを 32 に固定
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 32));
        panel.setMaximumSize(new Dimension(panel.getMaximumSize().width, 32));
        panel.setMinimumSize(new Dimension(panel.getMinimumSize().width, 32));

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = keywordField.getText().trim();
                // 全ての桁が数字 or Z の場合（＝コード検索）半角に，それ以外は全角に
                if (key.matches("^[0-9,０-９,Z]+$")) {
                    key = StringTool.toHankakuNumber(key);
                } else {
                    key = StringTool.toZenkakuNumber(key);
                    key = StringTool.toZenkakuUpperLower(key);
                    key = key.replaceAll("[-−]","－"); // ダッシュ「−」を EUC に変換可能なコード（0xEFBC8D）に
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
            }
        };

        searchLabel = new JLabel(GUIConst.ICON_SYSTEM_SEARCH_16);
        searchLabel.setText("マスタ検索：");

        keywordField = new JTextField(KEYWORD_FIELD_LENGTH);
        keywordField.setAlignmentY(0.6f); // tuning for el capitan
        keywordField.setMaximumSize(keywordField.getPreferredSize());
        keywordField.setToolTipText(TOOLTIP_KEYWORD);
        keywordField.addActionListener(listener);
        IMEControl.setImeOnIfFocused(keywordField);

        partialMatchBox = new JCheckBox("部分一致");
        partialMatchBox.addActionListener(listener);
        partialMatchBox.setSelected(prefs.getBoolean(PARTIAL_MATCH, false));

        adminCombo = new JComboBox(ADMIN_CATEGORY);
        adminCombo.setMaximumRowCount(ADMIN_CATEGORY.length + 1);
        adminCombo.setToolTipText("括弧内はコードの番号台を表します。");
        adminCombo.setMaximumSize(adminCombo.getPreferredSize());
        adminCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int index = adminCombo.getSelectedIndex();
                    String code = ADMIN_CODE_RANGE[index];
                    if (!code.equals("")) {
                        search("^" + code);
                    }
                }
            }
        });
        // adminCombo は処方のときだけ有効
        if (! IInfoModel.ENTITY_MED_ORDER.equals(entity)) {
            adminCombo.setEnabled(false);
        }

        countLabel = new JLabel("0 件");
        Dimension d = countLabel.getPreferredSize();
        d.width = 70;
        countLabel.setPreferredSize(d);
        countLabel.setMaximumSize(d);
        countLabel.setMinimumSize(d);
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(searchLabel);
        panel.add(keywordField);
        panel.add(partialMatchBox);
        panel.add(Box.createHorizontalGlue());
        panel.add(adminCombo);
        panel.add(countLabel);

        return panel;
    }

    /**
     * MasterSearchPanel のテーブルモデル
     * @return
     */
    protected ObjectReflectTableModel createTableModel() {
        if (IInfoModel.ENTITY_DIAGNOSIS.equals(entity)) {
            String[] columns = { " コード", " 名 称", " ICD10", " 単 位", " 点数（薬価）", " 開 始", " 終 了" };
            String[] methods = { "getCode", "getName", "getIcd10", "getUnit", "getTen", "getStartDate", "getEndDate" };
            return new ObjectReflectTableModel(columns, 1, methods, null);
        } else  {
            String[] columns = { " コード", " 名 称", " 療 区", " 単 位", " 点数（薬価）", " 開 始", " 終 了" };
            String[] methods = { "getCode", "getName", "getClaimClassCode", "getUnit", "getTen", "getStartDate", "getEndDate" };
            return new ObjectReflectTableModel(columns, 1, methods, null);
        }
    }

    /**
     * MasterSearchPanel のテーブル
     * @return
     */
    protected JTable createTable() {
        int[] width = new int[]{90, 200, 50, 60, 80, 100, 100};

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow == -1) return;
                table.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                OrcaEntry o = (OrcaEntry) tableModel.getObject(table.convertRowIndexToModel(viewRow));
                if (o != null) {
                    MasterItem mItem = new MasterItem();

                    // claim 003 コード
                    String code = o.getCode();
                    if (code.startsWith(ClaimConst.ADMIN_CODE_START)) {
                        // 部位コード 001000800-999，コメント 0010000 00-99 は薬剤コードで登録する
                        if (code.matches("^001000[0,8,9].*")) {
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
                    firePropertyChange(SELECTED_ITEM_PROP, null, mItem);

                    // 用法コンボを元に戻す
                    adminCombo.setSelectedIndex(0);
                }
            }
        });
       // 列幅を設定する
        TableColumn column;
        int len = width.length;
        for (int i = 0; i < len; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width[i]);
            // 名称コラム以外は固定
            if (i != 1) column.setMaxWidth(width[i]);
        }

        // レンダラ
        table.setDefaultRenderer(Object.class, new MasterTableRenderer());
        // sorter
        table.setRowSorter(new MasterTableSorter(tableModel));

        return table;
    }

    /**
     * ORCA でキーワードを検索して OrcaEntry を取ってきて table にセットする
     * @param key
     */
    private void search(String key) {
        OrcaMasterDao dao = SqlDaoFactory.createOrcaMasterDao();
        tableModel.clear();
        // スクロール状態で再検索されたとき，先頭から表示されるようにする
        ((JViewport)table.getParent()).scrollRectToVisible(new Rectangle(0,0,0,0));

        if (IInfoModel.ENTITY_DIAGNOSIS.equals(entity)) {
            tableModel.addRows(dao.getByomeiEntries(key));
        } else  {
            tableModel.addRows(dao.getTensuEntries(key));
        }
        countLabel.setText(tableModel.getRowCount() + " 件");
    }

    /**
     * Master のレンダラ
     */
    private class MasterTableRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private final int TENSU_COL = 4;
        private final int START_COL = 5;
        private final int END_COL = 6;

        public MasterTableRenderer() {
            this.setBorder(GUIConst.RENDERER_BORDER_NARROW);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column ) {

            JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            comp.setBorder(null);

            String endDate = (String) table.getValueAt(row, END_COL);

            // 色の決定
            if (isSelected) {
                if (todayDate.compareTo(endDate) <= 0) {
                    comp.setForeground(table.getSelectionForeground());
                    comp.setBackground(table.getSelectionBackground());
                } else {
                    comp.setForeground(Color.GRAY);
                    comp.setBackground(table.getSelectionBackground());
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

            String text = (value==null)? "" : String.valueOf(value);
            text = StringTool.toHankakuNumber(text);
            text = StringTool.toHankakuUpperLower(text);
            text = text.replaceAll("　", " ");

            if ("99999999".equals(text) || "00000000".equals(text) || "0.00".equals(text)) {
                text = "-";
            }

            // 日付の表示形式
            if (column == START_COL || column == END_COL) {
                text = OrcaMasterDao.toDolphinDateStr(text);
                if (text == null) text = "-";
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

    private class MasterTableSorter extends TableRowSorter<ObjectReflectTableModel> {

        private MasterTableSorter(final ObjectReflectTableModel tableModel) {
            super(tableModel);
        }

        // ASCENDING -> DESENDING -> 初期状態 と切り替える
        @Override
        public void toggleSortOrder(int column) {
            if(column >= 0 && column < getModelWrapper().getColumnCount() && isSortable(column)) {
                List<? extends SortKey> keys = getSortKeys();
                if(!keys.isEmpty()) {
                    SortKey sortKey = keys.get(0);
                    if(sortKey.getColumn() == column && sortKey.getSortOrder() == SortOrder.DESCENDING) {
                        setSortKeys(null);
                        return;
                    }
                }
            }
            super.toggleSortOrder(column);
        }
    }
}
