package open.dolphin.client;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.delegater.OrcaDelegater;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.dto.DiagnosisSearchSpec;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.*;
import open.dolphin.infomodel.*;
import open.dolphin.inspector.DiagnosisInspector;
import open.dolphin.order.StampEditorDialog;
import open.dolphin.project.Project;
import open.dolphin.ui.*;
import open.dolphin.ui.sheet.JSheet;
import open.dolphin.util.DateUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.prefs.Preferences;

/**
 * DiagnosisDocument.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public final class DiagnosisDocument extends AbstractChartDocument implements PropertyChangeListener {

    // 傷病名テーブルのカラム番号定義
    public static final int DIAGNOSIS_COL = 0;
    public static final int CATEGORY_COL = 1;
    public static final int OUTCOME_COL = 2;
    public static final int START_DATE_COL = 3;
    public static final int END_DATE_COL = 4;
    // propertyChange 用
    public static final String ADD_UPDATED_LIST = "addUpdatedList";
    public static final String ADD_ADDED_LIST = "addAddedList";
    // DiagnosisCategory 用
    public static final String MAIN_DIAGNOSIS = "主病名";
    public static final String SUSPECTED_DIAGNOSIS = "疑い病名";
    // RegisteredDiagnosisModel の Status
    public static final String ORCA_RECORD = "ORCA"; // ORCA 病名
    public static final String DELETED_RECORD = "DELETED"; // 削除病名
    public static final String IKOU_BYOMEI_RECORD = "IKOU_BYOMEI"; // 移行病名
    public static final Color IKOU_BYOMEI_COLOR = Color.red;
    public static final Color DELETED_COLOR = new Color(192, 192, 192); // silver
    public static final Color ENDED_COLOR = new Color(119, 136, 153); // light slate gray
    public static final Color ENDED_SELECTION_COLOR = new Color(220, 220, 220);// grains boro
    private static final String TITLE = "傷病名";
    // GUI コンポーネント定義
    private static final ImageIcon DELETE_BUTTON_IMAGE = GUIConst.ICON_REMOVE_16;
    private static final ImageIcon ADD_BUTTON_IMAGE = GUIConst.ICON_LIST_ADD_16;
    private static final ImageIcon UPDATE_BUTTON_IMAGE = GUIConst.ICON_SAVE_16;
    private static final ImageIcon ORCA_VIEW_IMAGE = GUIConst.ICON_DOWNLOAD_16;
    private static final String ORCA_VIEW = "ORCA View";
    private static final Color ORCA_BACK_COLOR = ClientContext.getColor("color.CALENDAR_BACK");
    // 新規に追加された傷病名リスト
    private final List<RegisteredDiagnosisModel> addedDiagnosis = new ArrayList<>();
    // 更新された傷病名リスト
    private final List<RegisteredDiagnosisModel> updatedDiagnosis = new ArrayList<>();
    // 削除された傷病名リスト
    private final List<RegisteredDiagnosisModel> deletedDiagnosis = new ArrayList<>();
    // 初期状態の病名リストを保存しておく（undoの際，保存している状態に戻ったかどうか判定して controlUpdate を制御する）
    private final List<DiagnosisLiteModel> initialDiagnosis = new ArrayList<>();
    // Logger
    private final Logger logger = Logger.getLogger(DiagnosisDocument.class);
    private JButton addButton;                  // 新規病名エディタボタン
    private JButton updateButton;               // 既存傷病名の転帰等の更新ボタン
    private JButton deleteButton;               // 既存傷病名の削除ボタン
    private JButton orcaButton;                 // ORCA View ボタン
    private DiagnosisDocumentTable diagTable;   // 病歴テーブル
    private DiagnosisDocumentTableModel tableModel; // TableModel
    private JComboBox<PNSPair<String, Integer>> extractionCombo;          // 抽出期間コンボ
    private JTextField countField;              // 件数フィールド
    private JTextField startDateField;
    private JTextField endDateField;
    // 昇順降順フラグ
    private boolean ascend;
    // 傷病名件数
    private int diagnosisCount;
    // 最終受診日＝今日受診している場合は今日，していないばあいは最後の受診日
    private LastVisit lastVisit;
    // Stamp から drop を受け取る場合のアクション : DiagnosisInspector でも使うので，public にした
    private int dropAction; // 通常は MOVE で，ALT が押されていたら COPY になる
    // DiagnosisInspector
    private DiagnosisInspector diagnosisInspector;
    // DiagnosisDocumentPopupMenu
    private DiagnosisDocumentPopupMenu popup;
    // ChartImpl#close() で isValidOutcome でない時，DiagnosisDocument に戻れるようにするために使う
    private boolean isValidOutcome = true;

    public DiagnosisDocument() {
        setTitle(TITLE);
    }

    /**
     * JComboBox の項目から target item の index を返す.
     *
     * @param combo JComboBox
     * @param item  targetItem
     * @return index
     */
    public static int itemToIndex(JComboBox combo, String item) {
        int index = 0;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (item.equals(combo.getItemAt(i).toString())) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * ChartMediator から呼ばれる.
     *
     * @return DiagnosisDocumentTable
     */
    public DiagnosisDocumentTable getDiagnosisTable() {
        return diagTable;
    }

    /**
     * DiagnosisDocumentPopupMenu 用.
     *
     * @return startDateField
     */
    public JTextField getStartDateField() {
        return startDateField;
    }

    public JTextField getEndDateField() {
        return endDateField;
    }

    public LastVisit getLastVisit() {
        return lastVisit;
    }

    public DiagnosisDocumentPopupMenu getDiagnosisDocumentPopup() {
        return popup;
    }

    public int getDropAction() {
        return dropAction;
    }

    public void setDropAction(int d) {
        dropAction = d;
    }

    /**
     * GUI コンポーネントを生成初期化する.
     */
    private void initialize() {

        // コマンドボタンパネルを生成する
        JPanel cmdPanel = createButtonPanel2();

        // Dolphin 傷病歴パネルを生成する
        JPanel dolphinPanel = createDiagnosisPanel();

        // 抽出期間パネルを生成する
        JPanel filterPanel = createFilterPanel();

        JPanel content = new JPanel(new BorderLayout(0, 7));
        content.add(cmdPanel, BorderLayout.NORTH);
        content.add(dolphinPanel, BorderLayout.CENTER);
        content.add(filterPanel, BorderLayout.SOUTH);

        // 全体をレイアウトする
        JPanel myPanel = getUI();
        //myPanel = getUI();
        myPanel.setLayout(new BorderLayout(0, 7));
        myPanel.add(content);
        //myPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        myPanel.setBorder(BorderFactory.createEmptyBorder());

        // Preference から昇順降順を設定する
        ascend = Project.getPreferences().getBoolean(Project.DIAGNOSIS_ASCENDING, false);

        // ポップアップメニュー用設定 (isReadOnly対応)
        if (!getContext().isReadOnly()) {
            popup = new DiagnosisDocumentPopupMenu(this);
        }

        // ショートカット登録
        // Windows XP で全画面表示すると，getRootPane() が null になる
        //ActionMap am = myPanel.getRootPane().createActionMap();
        //InputMap im = myPanel.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = myPanel.getActionMap();
        InputMap im = myPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // delete key
        im.put(KeyStroke.getKeyStroke("BACK_SPACE"), "delete");
        am.put("delete", new ProxyAction(deleteButton::doClick));

        // duplicate
        im.put(KeyStroke.getKeyStroke("meta D"), "duplicate");
        am.put("duplicate", new ProxyAction(this::duplicateDiagnosis));

        // tableModel 用設定
        lastVisit = ((ChartImpl) getContext()).getLastVisit();
        tableModel.setLastVisit(lastVisit);
        tableModel.getBoundSupport().addPropertyChangeListener(evt -> {
            String prop = evt.getPropertyName();
            // update があった場合
            if (ADD_UPDATED_LIST.equals(prop)) {
                // tableModel から呼ばれた場合は，update の場合と delete の場合がある
                RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) evt.getNewValue();

                if (DELETED_RECORD.equals(rd.getStatus())) {
                    addDeletedList(rd);
                } else {
                    addUpdatedList(rd);
                }

                // insert された場合 → 転帰を空白にして新規病名として使い回す場合は挿入として扱う
            } else if (ADD_ADDED_LIST.equals(prop)) {
                RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) evt.getNewValue();
                insertDiagnosis(rd);
            }
        });
        // PatientInspector に新しく作った DiagnosisInspector と連絡
        diagnosisInspector = ((ChartImpl) getContext()).getDiagnosisInspector();
    }

    /**
     * コマンドボタンパネルをする.
     */
    private JPanel createButtonPanel2() {

        // 更新ボタン
        updateButton = new JButton(UPDATE_BUTTON_IMAGE);
        updateButton.addActionListener(e -> save());

        updateButton.setEnabled(false);
        updateButton.setToolTipText("追加変更した傷病名をデータベースに反映します。");

        // 削除ボタン
        deleteButton = new JButton(DELETE_BUTTON_IMAGE);
        deleteButton.addActionListener(e -> delete());
        deleteButton.setEnabled(false);
        deleteButton.setToolTipText("選択した傷病名を削除します。");

        // 新規登録ボタン
        addButton = new JButton(ADD_BUTTON_IMAGE);
        addButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    // ASP StampBox が選択されていて傷病名Treeがない場合がある
                    if (getContext().getChartMediator().hasTree(IInfoModel.ENTITY_DIAGNOSIS)) {
                        JPopupMenu popup = new JPopupMenu();
                        getContext().getChartMediator().addDiseaseMenu(popup);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                        String msg1 = "現在使用中のスタンプボックスには傷病名がありません。";
                        String msg2 = "個人用のスタンプボックス等に切り替えてください。";
                        Object obj = new String[]{msg1, msg2};
                        String title = ClientContext.getFrameTitle("傷病名追加");
                        Component comp = getUI();
                        JOptionPane.showMessageDialog(comp, obj, title, JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        // Depends on readOnly prop
        addButton.setEnabled(!isReadOnly());
        addButton.setToolTipText("傷病名を追加します。");

        // ORCA View
        orcaButton = new JButton(ORCA_VIEW_IMAGE);
        orcaButton.addActionListener(e -> viewOrca());
        orcaButton.setToolTipText("ORCAに登録してある病名を取り込みます。");

        // ボタンパネル
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        p.add(orcaButton);
        p.add(deleteButton);
        p.add(addButton);
        p.add(updateButton);
        return p;
    }

    /**
     * 既傷病歴テーブルを生成する.
     */
    private JPanel createDiagnosisPanel() {

        int clickCountToStart = Project.getPreferences().getInt("diagnosis.table.clickCountToStart", 1);

        // Diagnosis テーブルモデルを生成する
        List<PNSTriple<String, Class<?>, String>> reflectionList = Arrays.asList(
                new PNSTriple<>("　疾患名/修飾語", String.class, "getAliasOrName"),
                new PNSTriple<>("　分 類", String.class, "getCategoryDesc"),
                new PNSTriple<>("　転 帰", String.class, "getOutcomeDesc"),
                new PNSTriple<>("　疾患開始日", String.class, "getStartDate"),
                new PNSTriple<>("　疾患終了日", String.class, "getEndDate")
        );
        tableModel = new DiagnosisDocumentTableModel(reflectionList, isReadOnly());

        // 傷病歴テーブルを生成する
        diagTable = new DiagnosisDocumentTable(tableModel);
        diagTable.putClientProperty("Quaqua.Table.style", "striped");
        tableModel.setDiagTable(diagTable);
        // sorter を設定
        TableRowSorter<DiagnosisDocumentTableModel> sorter = new TableRowSorter<DiagnosisDocumentTableModel>(tableModel) {
            // ASCENDING -> DESENDING -> 初期状態 と切り替える
            @Override
            public void toggleSortOrder(int column) {
                if (column >= 0 && column < getModelWrapper().getColumnCount() && isSortable(column)) {
                    List<SortKey> keys = new ArrayList<>(getSortKeys());
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
        };
        diagTable.setRowSorter(sorter);

        // 日付でソートしたとき，空白は一番最近のものとしてソート
        Comparator<String> dateComparator = Comparator.comparing(x -> StringTool.isEmpty(x) ? "9999-99-99" : x);
        sorter.setComparator(START_DATE_COL, dateComparator);
        sorter.setComparator(END_DATE_COL, dateComparator);

        // table のグリッド設定 (ORCA 病名は stripe にならないので grid だす)
        // Retina 対応
        //diagTable.setShowGrid(true);
        //diagTable.setGridColor(Color.WHITE);

        // コラム幅設定
        int[] columnWidth = {150, 80, 80, 120, 120};
        for (int i = 0; i < columnWidth.length; i++) {
            TableColumn column = diagTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidth[i]);
            if (i != 0) { //固定幅
                column.setMaxWidth(columnWidth[i]);
                column.setMinWidth(columnWidth[i]);
            }
        }

        // 奇数，偶数行の色分けをする
        diagTable.setDefaultRenderer(Object.class, new DolphinOrcaRenderer());

        // 複数選択可能にした
        diagTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        diagTable.setRowSelectionAllowed(true);

        // 行選択が起った時のリスナを設定する
        ListSelectionModel m = diagTable.getSelectionModel();
        m.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                controlButtons();
            }
        });

        // Category comboBox 設定
        JComboBox<DiagnosisCategoryModel> categoryCombo = new JComboBox<>(DiagnosisCategory.models());
        TableColumn column = diagTable.getColumnModel().getColumn(CATEGORY_COL);
        column.setCellEditor(new MyCellEditor(categoryCombo));

        // Outcome comboBox 設定
        JComboBox<DiagnosisOutcomeModel> outcomeCombo = new JComboBox<>(DiagnosisOutcome.models());
        column = diagTable.getColumnModel().getColumn(OUTCOME_COL);
        column.setCellEditor(new MyCellEditor(outcomeCombo));

        // Start Date && EndDate
        String datePattern = ClientContext.getString("common.pattern.mmlDate");
        column = diagTable.getColumnModel().getColumn(START_DATE_COL);
        startDateField = new JTextField();
        startDateField.setDocument(new RegexConstrainedDocument(datePattern));
        final DefaultCellEditor startDateCellEditor = new PNSCellEditor(startDateField);
        column.setCellEditor(startDateCellEditor);
        startDateCellEditor.setClickCountToStart(clickCountToStart);

        column = diagTable.getColumnModel().getColumn(END_DATE_COL);
        endDateField = new JTextField();
        endDateField.setDocument(new RegexConstrainedDocument(datePattern));
        final DefaultCellEditor endDateCellEditor = new PNSCellEditor(endDateField);
        column.setCellEditor(endDateCellEditor);
        endDateCellEditor.setClickCountToStart(clickCountToStart);

        // TransferHandler を設定する (isReadOnly対応)
        if (!getContext().isReadOnly()) {
            diagTable.setTransferHandler(new DiagnosisTransferHandler(this));
            //diagTable.setDragEnabled(true); // これだと，focus が当たっていないところを drag すると無視される
            diagTable.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    diagTable.getTransferHandler().exportAsDrag((JComponent) e.getSource(), e, TransferHandler.COPY);
                }
            });
        }

        // Layout
        final PNSScrollPane scroller = new PNSScrollPane(diagTable,
                PNSScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                PNSScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //scroller.setBorder(BorderFactory.createEmptyBorder());

        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.add(scroller, BorderLayout.CENTER);

        // insertStamp() でALT キーで疑い病名に変換する機能をつけるため，action を記録する
        // ALT 押した場合が COPY になり，押してないと MOVE
        DropTarget dt = new DropTarget(p, new DropTargetListener() {

            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                dropAction = dtde.getDropAction();
                scroller.setShowDropFeedback(true);
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
                dropAction = dtde.getDropAction();
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                diagTable.getTransferHandler().importData(diagTable, dtde.getTransferable());
                dtde.dropComplete(true); // これをしないとドラッグしてきたアイコンが逃げる
                scroller.setShowDropFeedback(false);
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                scroller.setShowDropFeedback(false);
            }
        });
        dt.setActive(true);

        return p;
    }

    /**
     * 抽出期間パネルを生成する.
     */
    private JPanel createFilterPanel() {

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalStrut(7));

        // 抽出期間コンボボックス
        p.add(new JLabel("抽出期間(過去)"));
        p.add(Box.createRigidArea(new Dimension(5, 0)));
        extractionCombo = ComboBoxFactory.createDiagnosisExtractionPeriodCombo();

        Preferences prefs = Project.getPreferences();
        int currentDiagnosisPeriod = prefs.getInt(Project.DIAGNOSIS_PERIOD, 0);

        int selectIndex = 0;
        for (int i = 0; i < extractionCombo.getItemCount(); i++) {
            if (currentDiagnosisPeriod == extractionCombo.getItemAt(i).getValue()) {
                selectIndex = i;
                break;
            }
        }
        extractionCombo.setSelectedIndex(selectIndex);
        extractionCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateDiagnosisHistory();
            }
        });

        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        comboPanel.add(extractionCombo);
        p.add(comboPanel);

        p.add(Box.createHorizontalGlue());

        // 件数フィールド
        countField = new JTextField(2);
        countField.setEditable(false);
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        countPanel.add(new JLabel("件数"));
        countPanel.add(countField);

        p.add(countPanel);
        p.add(Box.createHorizontalStrut(7));
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));

        return p;
    }

    @Override
    public void start() {
        initialize();
        updateDiagnosisHistory();
    }

    /**
     * extraction ComboBox の選択値に応じて DiagnosisHistory を更新する.
     */
    private void updateDiagnosisHistory() {

        int index = extractionCombo.getSelectedIndex();
        int past = extractionCombo.getItemAt(index).getValue();

        Date date;
        if (past != 0) {
            GregorianCalendar today = new GregorianCalendar();
            today.add(GregorianCalendar.MONTH, past);
            today.clear(Calendar.HOUR_OF_DAY);
            today.clear(Calendar.MINUTE);
            today.clear(Calendar.SECOND);
            today.clear(Calendar.MILLISECOND);
            date = today.getTime();
        } else {
            date = new Date(0L);
        }

        getDiagnosisHistory(date);
    }

    @Override
    public void stop() {
        // "isDirty()=true" means "破棄" or "保存だけれどセーブが完了していない"
        // There is no way distinguish them.
        if (tableModel != null) {
            tableModel.clear();
        }
    }

    @Override
    public void enter() {
        super.enter();
        getContext().enabledAction(GUIConst.ACTION_SELECT_ALL, true);
        getContext().enabledAction(GUIConst.ACTION_SAVE, true);
        getContext().enabledAction(GUIConst.ACTION_SEND_CLAIM, true);
        // フォーカスを取る
        requestFocus(diagTable);
        // ボタン制御
        controlButtons();
    }

    /**
     * フォーカス処理.
     *
     * @param c focus requester
     */
    private void requestFocus(Component c) {
        Focuser.requestFocus(c);
    }

    /**
     * 新規傷病名リストに追加する.
     *
     * @param added 追加されたRegisteredDiagnosisModel
     */
    private void addAddedList(RegisteredDiagnosisModel added) {
        // 同じものは update しない -> RegisteredDiagnosis#equals の変更が必要
        // オリジナルでは equals は id で比較しているので，id=0 は全部 contains=true になってしまう
        if (!addedDiagnosis.contains(added)) {
            addedDiagnosis.add(added);
        }
        controlButtons();
    }

    /**
     * 更新リストに追加する.
     *
     * @param updated 更新されたRegisteredDiagnosisModel
     */
    private void addUpdatedList(RegisteredDiagnosisModel updated) {
        // addedDiagnosis を編集／undo した場合ここに入ってくる
        if (updated.getId() == 0L) { // addedDiagnosis は id=0
            if (!addedDiagnosis.contains(updated)) {
                addedDiagnosis.add(updated);
            }
        } else {
            if (!updatedDiagnosis.contains(updated)) {
                updatedDiagnosis.add(updated);
            }
        }
        // 削除を undo した場合は deletedDiagnosis から削除
        deletedDiagnosis.remove(updated);

        controlButtons();
    }

    /**
     * 削除リストに追加する.
     *
     * @param deleted 削除病名
     */
    private void addDeletedList(RegisteredDiagnosisModel deleted) {
        deletedDiagnosis.add(deleted);
        // delete したら 他のリストからも削除
        addedDiagnosis.remove(deleted);
        updatedDiagnosis.remove(deleted);
        controlButtons();
    }

    /**
     * デバッグ用
     */
/*
    private void showList() {
        System.out.println("----addedDiagnosis");
        for (RegisteredDiagnosisModel rd : addedDiagnosis) {
            System.out.println(rd.getDiagnosis()+","+rd.getCategoryDesc()+","+rd.getOutcomeDesc());
        }
        System.out.println("----updatedDiagnosis");
        for (RegisteredDiagnosisModel rd : updatedDiagnosis) {
            System.out.println(rd.getDiagnosis()+","+rd.getCategoryDesc()+","+rd.getOutcomeDesc());
        }
        System.out.println("----deletedDiagnosis");
        for (RegisteredDiagnosisModel rd : deletedDiagnosis) {
            System.out.println(rd.getDiagnosis()+","+rd.getCategoryDesc()+","+rd.getOutcomeDesc());
        }
    }
*/

    /**
     * 追加及び更新リストをクリアする.
     */
    private void clearDiagnosisList() {

        addedDiagnosis.clear();
        updatedDiagnosis.clear();
        deletedDiagnosis.clear();

        // initialDiagnosis の更新
        initialDiagnosis.clear();
        tableModel.getObjectList().forEach(rd -> initialDiagnosis.add(new DiagnosisLiteModel(rd)));
        controlButtons();
    }

    /**
     * ボタン制御 update, delete, undo, redo.
     */
    private void controlButtons() {
        //showList(); //デバッグ用
        if (isReadOnly()) {
            return;
        }

        // update button
        // initDiagnosis に戻ったものがあれば updatedDiagnosis を削除
        List<RegisteredDiagnosisModel> resumed = new ArrayList<>();
        updatedDiagnosis.forEach(rd ->
                initialDiagnosis.stream().filter(Predicate.isEqual(rd)).forEach(dlm -> resumed.add(rd)));
        updatedDiagnosis.removeAll(resumed);

        boolean newDirty = !addedDiagnosis.isEmpty() || !updatedDiagnosis.isEmpty() || !deletedDiagnosis.isEmpty();
        setDirty(newDirty);

        // タブが選択されていない場合（DiagnosisInspector で操作した場合）はボタンのコントロールはしない
        if (getUI().isShowing()) {
            updateButton.setEnabled(isDirty());
        }

        // delete button : 選択中に null や ORCA や DELETED が１つでもあれば disable する
        // undo/redo     : 選択内に１つでも undo/redo 可能なものがあれば enable
        boolean isDeletable = true;
        boolean isUndoable = false;
        boolean isRedoable = false;

        // 選択された行のオブジェクトを得る
        // 空のテーブルにドロップすると，objectCount=0 なのに selectedRows.length=1  という状態になる
        if (tableModel.getObjectCount() != 0) {
            int[] rows = diagTable.getSelectedRows();
            for (int row : rows) {
                int r = diagTable.convertRowIndexToModel(row);
                RegisteredDiagnosisModel rd = tableModel.getObject(r);

                isDeletable = rd != null && !ORCA_RECORD.equals(rd.getStatus()) && !DELETED_RECORD.equals(rd.getStatus());
                isUndoable = tableModel.isUndoable(rd);
                isRedoable = tableModel.isRedoable(rd);
            }
        }
        // タブが選択されていない場合（DiagnosisInspector で操作した場合）はボタンのコントロールはしない
        if (getUI().isShowing()) {
            deleteButton.setEnabled(isDeletable);
            getContext().enabledAction(GUIConst.ACTION_UNDO, isUndoable);
            getContext().enabledAction(GUIConst.ACTION_REDO, isRedoable);
        }
    }

    /**
     * 傷病名件数を返す.
     *
     * @return 傷病名件数
     */
    public int getDiagnosisCount() {
        return diagnosisCount;
    }

    /**
     * 傷病名件数を設定する.  modified by pns.
     * DELETED_RECORD はカウントしない.
     */
    public void setDiagnosisCount() {
        diagnosisCount = 0;
        int diagnosisCountToday = 0;
        String today = SimpleDate.simpleDateToMmldate(new SimpleDate(new GregorianCalendar()));

        for (int row = 0; row < tableModel.getObjectCount(); row++) {
            RegisteredDiagnosisModel rd = tableModel.getObject(row);
            if (!DELETED_RECORD.equals(rd.getStatus())) {
                diagnosisCount++;
                if (today.equals(rd.getStartDate())) {
                    diagnosisCountToday++;
                }
            }
        }

        // 病名数セット
        PatientVisitModel pvt = getContext().getPatientVisit();
        if (pvt.getId() != 0L) { // 今日の受診がなければ(PatientSearchから開いた場合) id=0 になる
            pvt.setByomeiCount(diagnosisCount);
            pvt.setByomeiCountToday(diagnosisCountToday);
        }
        // countField にセット
        try {
            String val = String.valueOf(diagnosisCount);
            countField.setText(val);
        } catch (RuntimeException e) {
            countField.setText("");
        }
        // DiagnosisInspector を update
        diagnosisInspector.update(tableModel);
    }

    /**
     * 傷病名スタンプを取得する worker を起動する.
     *
     * @param stampList スタンプリスト
     * @param insertRow 挿入位置
     */
    public void importStampList(final List<ModuleInfoBean> stampList, final int insertRow) {
        // 4 個以上一気にドロップされたら警告を出す
        if (stampList.size() >= 4) {
            int ans = JSheet.showConfirmDialog(getContext().getFrame(),
                    stampList.size() + "個のスタンプが同時にドロップされましたが続けますか", "スタンプ挿入確認",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE
            );
            if (ans != JOptionPane.YES_OPTION) {
                return;
            }
        }

        final StampDelegater sdl = new StampDelegater();

        DBTask task = new DBTask<List<StampModel>>(getContext()) {
            @Override
            protected List<StampModel> doInBackground() {
                return sdl.getStamp(stampList);
            }

            @Override
            protected void succeeded(List<StampModel> list) {
                logger.debug("importStampList succeeded");
                if (sdl.isNoError() && list != null) {
                    for (int i = list.size() - 1; i > -1; i--) {
                        insertStamp(list.get(i), insertRow);
                    }
                }
                // 病名を drop した場合，ここに入ってくる
                setDiagnosisCount();

                // 挿入した row を選択する
                // setDiagnosisCount で DiagnosisInspector の update が行われるので，
                // その後に選択しないと DiagnosisInspector に伝えられない
                int row = (ascend) ? diagTable.getRowCount() - 1 : 0;
                row = diagTable.convertRowIndexToView(row);
                diagTable.getSelectionModel().setSelectionInterval(row, row);

                // DiagnosisInspector にフォーカス
                Focuser.requestFocus(diagnosisInspector.getList());
            }
        };

        task.execute();
    }

    /**
     * 傷病名スタンプをデータベースから取得しテーブルへ挿入する. Worker Thread で実行される.
     *
     * @param sm  StampModel
     * @param row 自動判定するので使っていない
     */
    private void insertStamp(StampModel sm, int row) {

        if (sm != null) {
            RegisteredDiagnosisModel module = (RegisteredDiagnosisModel) sm.getStamp();
            // デフォルトのカテゴリーをセットする
            module.setCategory(open.dolphin.infomodel.IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY);
            module.setCategoryDesc(open.dolphin.infomodel.IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY_DESC);
            module.setCategoryCodeSys(open.dolphin.infomodel.IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY_CODESYS);
            insertDiagnosis(module);
        }
    }

    /**
     * row に RegisteredDiagnosisModel を挿入する.
     * テーブルへの挿入をする場所はここ（スタンプ箱，DiagnosisDocumentTableModel）と propertyChange（エディタから挿入）.
     * LastVisit がないのに病名だけ付ける場合はありえない
     *
     * @param module RegisteredDiagnosisModule
     */
    private void insertDiagnosis(RegisteredDiagnosisModel module) {
        // 疾患開始日を lastVisit に設定
        if (lastVisit.getLastVisit() == null) {
            JSheet.showMessageDialog(getContext().getFrame(), "最終受診日が取得できません", "", JOptionPane.ERROR_MESSAGE);
            return;
        }
        module.setStartDate(lastVisit.getLastVisit().format(DateTimeFormatter.ISO_DATE));

        // diagnosis に「疑い」が入っていたら，疑いにセットする
        String diag = module.getDiagnosis();
        if (diag.endsWith("の疑い")) {
            String diagCode = module.getDiagnosisCode();
            module.setDiagnosis(diag.replace("の疑い", ""));
            module.setDiagnosisCode(diagCode.replace(".8002", ""));
            module.setCategory("suspectedDiagnosis");
            module.setCategoryDesc("疑い病名");
            module.setCategoryCodeSys("MML0015");
        }
        // ALT キーが押されていたら，疑いにセットする
        if (dropAction == java.awt.dnd.DnDConstants.ACTION_COPY) {
            module.setCategory("suspectedDiagnosis");
            module.setCategoryDesc("疑い病名");
            module.setCategoryCodeSys("MML0015");
        }
        // 移行病名チェック
        List<RegisteredDiagnosisModel> modules = new ArrayList<>(1);
        modules.add(module);
        checkIkouByomei(modules);

        // module 挿入
        if (ascend) {
            tableModel.addRow(module);
        } else {
            tableModel.insertRow(0, module);
        }

        addAddedList(module);
    }

    /**
     * 傷病名エディタを開く.
     * 傷病名エディタから追加した場合 openEditor2.
     * 傷病名エディタで既にある病名を編集した場合は openEditor3 が呼ばれる　　thx masuda sensei.
     */
    public void openEditor2() {
        openEditor3(null);
    }

    public void openEditor3(RegisteredDiagnosisModel rd) {
        // editor が立ち上がっている間は ウインドウを閉じられないようにする
        getContext().enabledAction(GUIConst.ACTION_CLOSE, false);

        StampEditorDialog stampEditor = new StampEditorDialog("diagnosis", rd);

        // 編集終了，値の受け取りにこのオブジェクトを設定する
        stampEditor.addPropertyChangeListener(StampEditorDialog.VALUE_PROP, this);
        stampEditor.start();
    }

    /**
     * 傷病名エディタからデータを受け取りテーブルへ追加する.
     *
     * @param e PropertyChangeEvent
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        getContext().enabledAction(GUIConst.ACTION_CLOSE, true);

        String prop = e.getPropertyName();
        if (!StampEditorDialog.VALUE_PROP.equals(prop)) {
            return;
        }

        List<RegisteredDiagnosisModel> list = (List) e.getNewValue();
        if (list == null || list.isEmpty()) {
            return;
        }

        boolean isAddedDiagnosis = (Boolean) e.getOldValue(); // openEditor2 からよばれると true になるようにした
        String today = lastVisit.getLastVisit().format(DateTimeFormatter.ISO_DATE);

        for (int i = 0; i < list.size(); i++) { // list.size() は常に 1 では？
            // ここで get する rd には 病名とコードしか入っていない
            RegisteredDiagnosisModel rd = list.get(i);
            if (isAddedDiagnosis) {
                // エディタから新規に挿入された場合（openEditor2）
                rd.setStartDate(today); // startDate は LastVisit に設定
                if (ascend) {
                    tableModel.addRow(rd);
                } // 昇順はテーブルの最後へ追加する
                else {
                    tableModel.insertRow(0, rd);
                } // 降順はテーブルの先頭へ追加する
                addAddedList(rd);
            } else {
                // openEditor3 のデータの場合ー必ず選択が起きている
                int row = diagTable.getSelectedRow();
                row = diagTable.convertRowIndexToModel(row);
                if (row >= 0) {
                    tableModel.setValueAt(new DiagnosisLiteModel(rd), row, DIAGNOSIS_COL);
                    // setValue 後は id 他の情報もそろった rd が tableModel にセットされている
                    addUpdatedList(tableModel.getObject(row));
                }
            }
        }
        checkIkouByomei(list); // 移行病名チェック

        // added の場合挿入した row を選択する
        if (isAddedDiagnosis) {
            int row = (ascend) ? diagTable.getRowCount() - 1 : 0;
            row = diagTable.convertRowIndexToView(row);
            diagTable.getSelectionModel().setSelectionInterval(row, row);
        }
        setDiagnosisCount();
    }

    private boolean isValidOutcome(RegisteredDiagnosisModel rd) {

        // outCome が null の場合は，開始日は confirm date に自動設定されているので
        // そのまま return してもいいようだが，念のため開始日もチェックするように変更した
        // if (rd.getOutcome() == null) {
        //   return true;
        // }

        // すでに JSheet が出ている場合は，toFront してリターン
        if (JSheet.isAlreadyShown(getContext().getFrame())) {
            getContext().getFrame().toFront();
            return false;
        }

        String start = rd.getStartDate();
        String end = rd.getEndDate();

        if (start == null) {
            JSheet.showMessageDialog(
                    getContext().getFrame(),
                    "「" + rd.getDiagnosisName() + "」の開始日がありません",
                    ClientContext.getFrameTitle("病名チェック"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (rd.getOutcome() != null && end == null) {
            JSheet.showMessageDialog(
                    getContext().getFrame(),
                    "「" + rd.getDiagnosisName() + "」の転帰に対応する終了日がありません",
                    ClientContext.getFrameTitle("病名チェック"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (rd.getOutcome() == null && end != null) {
            JSheet.showMessageDialog(
                    getContext().getFrame(),
                    "「" + rd.getDiagnosisName() + "」の終了日に対応する転帰がありません",
                    ClientContext.getFrameTitle("病名チェック"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        Date startDate = null;
        Date endDate = null;
        boolean formatOk = true;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            start = start.replaceAll("/", "-");
            startDate = sdf.parse(start);
            // 終了日はないこともある
            if (end != null) {
                end = end.replaceAll("/", "-");
                endDate = sdf.parse(end);
            }
        } catch (ParseException e) {
            String message = "日付のフォーマットが正しくありません\n" +
                    "疾患名：" + rd.getDiagnosisName() + "\n" +
                    "「yyyy-MM-dd」の形式で入力してください。\n" +
                    "右クリックでカレンダが使用できます。";
            JSheet.showMessageDialog(
                    getContext().getFrame(),
                    message,
                    ClientContext.getFrameTitle("病名チェック"),
                    JOptionPane.ERROR_MESSAGE);
            formatOk = false;
        }

        if (!formatOk) {
            return false;
        }

        if (endDate != null && endDate.before(startDate)) {
            String message = String.format("「%s」の終了日が開始日以前になっています.", rd.getDiagnosisName());
            JSheet.showMessageDialog(
                    getContext().getFrame(),
                    message,
                    ClientContext.getFrameTitle("病名チェック"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * ChartImpl#close() で isValidOutcome でなかった場合，DiagnosisDocument に戻れるようにするために使う.
     *
     * @return valid or not
     */
    public boolean isValidOutcome() {
        return isValidOutcome;
    }

    /**
     * 新規及び変更された傷病名を保存する.
     * deletedDiagnosis 対応 by pns.
     */
    @Override
    public void save() {

        if (addedDiagnosis.isEmpty() && updatedDiagnosis.isEmpty() && deletedDiagnosis.isEmpty()) {
            return;
        }

        final boolean sendDiagnosis = Project.getSendDiagnosis();
        logger.debug("sendDiagnosis = " + sendDiagnosis);

        // continue to save
        Date confirmed = new Date();
        logger.debug("confirmed = " + confirmed);

        boolean go = true;

        // addedDiagnosis の処理
        if (addedDiagnosis.size() > 0) {

            for (RegisteredDiagnosisModel rd : addedDiagnosis) {

                logger.debug("added rd = " + rd.getDiagnosis());
                logger.debug("id = " + rd.getId());

                // 開始日，終了日はテーブルから取得している
                // TODO confirmed, recorded
                rd.setKarte(getContext().getKarte());           // Karte
                rd.setCreator(Project.getUserModel());          // Creator
                rd.setConfirmed(confirmed);                     // 確定日
                rd.setRecorded(confirmed);                      // 記録日
                rd.setStatus(IInfoModel.STATUS_FINAL);

                // 開始日=適合開始日 not-null
                if (rd.getStarted() == null) {
                    rd.setStarted(confirmed);
                }

                // TODO トラフィック
                rd.setPatientLiteModel(getContext().getPatient().patientAsLiteModel());
                rd.setUserLiteModel(Project.getUserModel().getLiteModel());

                // 転帰をチェックする
                isValidOutcome = isValidOutcome(rd);
                if (!isValidOutcome) {
                    go = false;
                    break;
                }
            }
        }

        if (!go) {
            return;
        }

        // updatedDiagnosis の処理
        if (updatedDiagnosis.size() > 0) {

            for (RegisteredDiagnosisModel rd : updatedDiagnosis) {

                logger.debug("updated rd = " + rd.getDiagnosis());
                logger.debug("id = " + rd.getId());

                // 現バージョンは上書きしている
                rd.setCreator(Project.getUserModel());
                rd.setConfirmed(confirmed);
                rd.setRecorded(confirmed);
                rd.setStatus(IInfoModel.STATUS_FINAL);

                // TODO トラフィック
                rd.setPatientLiteModel(getContext().getPatient().patientAsLiteModel());
                rd.setUserLiteModel(Project.getUserModel().getLiteModel());

                // 転帰をチェックする
                isValidOutcome = isValidOutcome(rd);
                if (!isValidOutcome) {
                    go = false;
                    break;
                }
            }
        }

        if (!go) {
            return;
        }

        DocumentDelegater ddl = new DocumentDelegater();
        DiagnosisPutTask task = new DiagnosisPutTask(getContext(), sendDiagnosis, ddl);
        task.execute();
    }

    /**
     * 指定期間以降の傷病名を検索してテーブルへ表示する.
     * バッググランドスレッドで実行される.
     * addedDiagnosis, updatedDiagnossis, deletedDiagnosis 対応 by pns
     *
     * @param past 指定期間の開始日
     */
    public void getDiagnosisHistory(Date past) {

        final DiagnosisSearchSpec spec = new DiagnosisSearchSpec();
        spec.setCode(DiagnosisSearchSpec.PATIENT_SEARCH);
        spec.setKarteId(getContext().getKarte().getId());
        if (past != null) {
            spec.setFromDate(past);
        }

        final DocumentDelegater ddl = new DocumentDelegater();

        DBTask task = new DBTask<List<RegisteredDiagnosisModel>>(getContext()) {

            @Override
            protected List<RegisteredDiagnosisModel> doInBackground() {
                logger.debug("getDiagnosisHistory doInBackground");
                return ddl.getDiagnosisList(spec);
            }

            @Override
            protected void succeeded(List<RegisteredDiagnosisModel> list) {
                logger.debug("getDiagnosisHistory succeeded");
                // if (list == null) { list = new ArrayList<>(); } // null にはならない

                if (ddl.isNoError() && list.size() > 0) {
                    if (ascend) {
                        list.sort(Comparator.naturalOrder());
                    } else {
                        list.sort(Comparator.reverseOrder());
                    }
                }
                // addedDiagnosis がある場合は list に追加
                if (addedDiagnosis.size() > 0) {
                    if (ascend) {
                        addedDiagnosis.addAll(list);
                    } else {
                        addedDiagnosis.forEach(rd -> list.add(0, rd));
                    }
                }
                // updateDiagnosis, DeletedDiagnosis はクリア
                updatedDiagnosis.clear();
                deletedDiagnosis.clear();

                // 新しく検索しなおしたリストをセット
                tableModel.setObjectList(list);
                setDiagnosisCount();

                // undo で最初に戻ったかどうか判定するため list を保存しておく
                initialDiagnosis.clear();
                list.forEach(rd -> initialDiagnosis.add(new DiagnosisLiteModel(rd)));

                // 最後に有効期限(disUseDate)が99999999以外に設定されていたら移行病名としてセット
                checkIkouByomei(list);

                // orca ボタン enable
                orcaButton.setEnabled(true);
            }
        };

        task.execute();
    }

    /**
     * RegisteredDiagnosisModel を元に，移行病名かどうかをチェックする.
     *
     * @param rdList List of RegisteredDiagnosisModel
     */
    public void checkIkouByomei(final List<RegisteredDiagnosisModel> rdList) {
        MainFrame c = getContext().getFrame();
        String message = "ORCA 接続中";
        String note = "移行病名チェック";
        int maxEstimation = 10000;
        int delay = 3000;

        Task<Boolean> task = new Task<Boolean>(c, message, note, maxEstimation) {

            @Override
            protected Boolean doInBackground() {
                boolean found = false;

                // 病名コードを切り出して（接頭語，接尾語は捨てる）コードのリストを作る
                // 重複は不要なので，HashSet を使う
                List<String> codeSet = new ArrayList<>();
                // codes のうち，７桁のものが srycd コード → これを codeSet にためる
                rdList.stream().map(rd -> rd.getDiagnosisCode().split("\\.")).forEach(codes ->
                        Arrays.stream(codes).filter(code -> code.length() == 7).forEach(codeSet::add));

                // codeSet のうち移行病名のリストを取得
                OrcaDelegater delegater = new OrcaDelegater();
                List<String> ikouList = delegater.findIkouByomei(codeSet);

                // ikouList に合致する病名に IKOU_BYOMEI_RECORD をマークする
                ikouList.stream().forEach(code ->
                        rdList.stream()
                                .filter(rd -> rd.getDiagnosisCode().contains(code))
                                .forEach(rd -> rd.setStatus(IKOU_BYOMEI_RECORD))
                );
                return !ikouList.isEmpty();
            }

            @Override
            protected void succeeded(Boolean found) {
                if (found) {
                    // fire すると選択が解除されてしまうので，選択を保存・復帰する
                    int[] selected = diagTable.getSelectedRows();
                    tableModel.fireTableDataChanged();
                    for (int row : selected) {
                        diagTable.getSelectionModel().setSelectionInterval(row, row);
                    }
                    // DiagnosisInspector に連絡
                    diagnosisInspector.update(tableModel);
                }
            }
        };
        task.setMillisToDecidePopup(delay);
        task.execute();
    }

    /**
     * 選択された行のデータを削除する.
     * 複数行対応と，いきなり消さないで，save() 時にまとめて消すようにする by pns.
     */
    public void delete() {

        for (int row : diagTable.getSelectedRows()) {
            int r = diagTable.convertRowIndexToModel(row);
            RegisteredDiagnosisModel rd = tableModel.getObject(r);
            if (rd != null) {
                tableModel.setValueAt(DELETED_RECORD, r, 0); // tableModel 側で操作
                setDiagnosisCount();
                tableModel.fireTableRowsUpdated(r, r);
            }
        }
    }

    /**
     * ORCAに登録されている病名を取り込む. （テーブルへ追加する）
     */
    public void viewOrca() {

        DBTask<List<RegisteredDiagnosisModel>> task = new DBTask<List<RegisteredDiagnosisModel>>(getContext()) {

            @Override
            protected List<RegisteredDiagnosisModel> doInBackground() {
                // 患者IDを取得する
                final String patientId = getContext().getPatient().getPatientId();
                // 抽出期間から検索範囲の最初の日を取得する
                int index = extractionCombo.getSelectedIndex();
                int past = extractionCombo.getItemAt(index).getValue(); // months

                LocalDate from = (past == 0) ? DateUtils.getMinLocalDate() : LocalDate.now().minusMonths(past);
                LocalDate to = LocalDate.now();
                OrcaDelegater delegater = new OrcaDelegater();
                return delegater.getOrcaDisease(patientId, from, to, ascend);
            }

            @Override
            protected void succeeded(List<RegisteredDiagnosisModel> list) {
                tableModel.addRows(list);
                orcaButton.setEnabled(false);
            }
        };
        task.execute();
    }

    public void undo() {
        tableModel.undo();
        setDiagnosisCount();
    }

    public void redo() {
        tableModel.redo();
        setDiagnosisCount();
    }

    public void selectAll() {
        diagTable.selectAll();
    }

    /**
     * 選択された診断を CLAIM 送信する.
     */
    public void sendClaim() {

        // 選択された診断を CLAIM 送信する
        RegisteredDiagnosisModel rd;
        List<RegisteredDiagnosisModel> diagList = new ArrayList<>();
        Date confirmed = new Date();
        int[] rows = diagTable.getSelectedRows();
        for (int r : rows) {
            int row = diagTable.convertRowIndexToModel(r);
            rd = tableModel.getObject(row);
            rd.setKarte(getContext().getKarte());           // Karte
            rd.setCreator(Project.getUserModel());          // Creator
            rd.setConfirmed(confirmed);                     // 確定日
            rd.setRecorded(confirmed);                      // 記録日
            // 開始日=適合開始日 not-null
            if (rd.getStarted() == null) {
                rd.setStarted(confirmed);
            }
            rd.setPatientLiteModel(getContext().getPatient().patientAsLiteModel());
            rd.setUserLiteModel(Project.getUserModel().getLiteModel());

            // 転帰をチェックする
            if (!isValidOutcome(rd)) {
                return;
            }

            diagList.add(rd);
        }

        MainFrame parent = getContext().getFrame();
        String message;
        int messageType = JOptionPane.PLAIN_MESSAGE;

        if (!diagList.isEmpty()) {
            OrcaDelegater delegater = new OrcaDelegater();
            delegater.sendDiagnoses(diagList);
            message = diagList.size() + " 件を ORCA に送信しました";

        } else {
            message = "CLAIM 送信する病名を選択して下さい";
            messageType = JOptionPane.ERROR_MESSAGE;
        }

        if (JSheet.isAlreadyShown(parent)) {
            parent.toFront();
            return;
        }
        JSheet.showMessageDialog(parent, message, "", messageType);
    }

    /**
     * 選択された診断を複製する.
     */
    public void duplicateDiagnosis() {

        int[] rows = diagTable.getSelectedRows();
        for (int r : rows) {
            int row = diagTable.convertRowIndexToModel(r);

            RegisteredDiagnosisModel srcRd = tableModel.getObject(row);
            RegisteredDiagnosisModel distRd = new RegisteredDiagnosisModel();

            distRd.setDiagnosis(srcRd.getDiagnosis());
            distRd.setDiagnosisCode(srcRd.getDiagnosisCode());
            distRd.setDiagnosisCodeSystem(srcRd.getDiagnosisCodeSystem());

            distRd.setCategory(srcRd.getCategory());
            distRd.setCategoryDesc(srcRd.getCategoryDesc());
            distRd.setCategoryCodeSys(srcRd.getCategoryCodeSys());

            distRd.setOutcome(srcRd.getOutcome());
            distRd.setOutcomeDesc(srcRd.getOutcomeDesc());
            distRd.setOutcomeCodeSys(srcRd.getOutcomeCodeSys());

            // distRd.setStartDate(srcRd.getStartDate());
            // duplicate した場合は，startDate は LastVisit とする
            distRd.setStartDate(lastVisit.getLastVisit().format(DateTimeFormatter.ISO_DATE));

            // endDate は消去
            // distRd.setEndDate(srcRd.getEndDate());

            tableModel.insertRow(r, distRd);
            diagTable.getSelectionModel().setSelectionInterval(r, r);
            addAddedList(distRd);

            // DiagnosisInspector に連絡
            diagnosisInspector.update(tableModel);
        }
    }

    /**
     * DiagnosisPutTask.
     */
    private class DiagnosisPutTask extends DBTask<List<Long>> {

        private final boolean sendClaim;
        private final DocumentDelegater ddl;

        public DiagnosisPutTask(Chart chart, boolean sendClaim, DocumentDelegater ddl) {

            super(chart);
            this.sendClaim = sendClaim;
            this.ddl = ddl;
        }

        @Override
        protected List<Long> doInBackground() {

            logger.debug("doInBackground");

            // 更新する
            if (updatedDiagnosis.size() > 0) {
                logger.debug("ddl.updateDiagnosis");
                ddl.updateDiagnosis(updatedDiagnosis);
            }

            List<Long> result = null;

            // 保存する
            if (addedDiagnosis.size() > 0) {
                logger.debug("ddl.putDiagnosis");
                result = ddl.putDiagnosis(addedDiagnosis);
                // 割り当てられた id が result に帰ってくる
                if (ddl.isNoError()) {
                    logger.debug("ddl.putDiagnosis() is NoErr");
                    // 新しく当てられた id(pk) を rd にセットする
                    for (int i = 0; i < addedDiagnosis.size(); i++) {
                        long pk = result.get(i);
                        logger.debug("persist id = " + pk);
                        RegisteredDiagnosisModel rd = addedDiagnosis.get(i);
                        rd.setId(pk);
                    }
                }
            }

            // 削除する：削除は removeDiagnosis(List<Long>id)
            if (deletedDiagnosis.size() > 0) {
                logger.debug("ddl.removeDiagnosis");
                // rd のリストから，id のリストを作成（id=0 はローカルだけなので無視）
                List<Long> list = new ArrayList<>();
                deletedDiagnosis.stream().filter(rd -> rd.getId() != 0).forEach(rd -> list.add(rd.getId()));

                if (list.size() > 0) {
                    ddl.removeDiagnosis(list);
                }
            }

            // 追加・更新病名を CLAIM 送信する
            if (sendClaim) {
                List<RegisteredDiagnosisModel> sendList = new ArrayList<>();
                sendList.addAll(updatedDiagnosis);
                sendList.addAll(addedDiagnosis);
                if (!sendList.isEmpty()) {
                    OrcaDelegater delegater = new OrcaDelegater();
                    delegater.sendDiagnoses(sendList);
                }
            }
            return result;
        }

        @Override
        protected void succeeded(List<Long> list) {
            logger.debug("DiagnosisPutTask succeeded");
            // save()で status が書き換えられるので，移行病名の再チェック
            checkIkouByomei(updatedDiagnosis);
            checkIkouByomei(addedDiagnosis);

            clearDiagnosisList();

            // DiagnosisInspector に連絡
            diagnosisInspector.update(tableModel);
        }
    }

    /**
     * ORCA 病名の色を変える，DELETE 病名を薄く表示，移行病名を赤表示.
     */
    private class DolphinOrcaRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        public DolphinOrcaRenderer() {
            super();
        }

        /**
         * Retina 対応 - show holizontal grid
         *
         * @param graphics Graphics
         */
        @Override
        public void paint(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            super.paint(graphics);
            g.setColor(Color.WHITE);
            g.drawLine(0, getHeight(), getWidth(), getHeight());
            g.dispose();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean isFocused,
                                                       int row, int col) {

            JLabel comp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, isFocused, row, col);
            comp.setBorder(null); // 選択が外れた後に枠が残るのを防ぐ

            int r = diagTable.convertRowIndexToModel(row);
            RegisteredDiagnosisModel rd = tableModel.getObject(r);

            // ORCA レコードかどうかを判定する
            boolean orca = rd != null && ORCA_RECORD.equals(rd.getStatus());
            boolean deleted = rd != null && DELETED_RECORD.equals(rd.getStatus());
            boolean ikou = rd != null && IKOU_BYOMEI_RECORD.equals(rd.getStatus());
            boolean ended = rd != null && rd.getEndDate() != null;

            if (isSelected) {
                // foreground
                if (deleted || ended) {
                    int rgb = table.getSelectionForeground().getRGB();
                    int adjust = 0x4f4f4f;
                    if ((rgb & 0x00ffffff) > adjust) {
                        rgb -= adjust;
                    }
                    if ((rgb & 0x00ffffff) < adjust) {
                        rgb += adjust;
                    }
                    comp.setForeground(new Color(rgb));
                } else if (ikou) {
                    comp.setForeground(IKOU_BYOMEI_COLOR);
                } else {
                    if (table.isFocusOwner()) {
                        comp.setForeground(table.getSelectionForeground());
                    } else {
                        comp.setForeground(table.getForeground());
                    }
                }
                // background
                if (table.isFocusOwner()) {
                    comp.setBackground(table.getSelectionBackground());
                } else {
                    comp.setBackground((Color) table.getClientProperty("JTable.backgroundOffFocus"));
                }

            } else {
                // foreground
                if (deleted) {
                    comp.setForeground(DELETED_COLOR);
                } else if (ikou) {
                    comp.setForeground(IKOU_BYOMEI_COLOR);
                } else if (ended) {
                    comp.setForeground(ENDED_COLOR);
                } else {
                    comp.setForeground(table.getForeground());
                }
                // background
                if (orca) {
                    comp.setBackground(ORCA_BACK_COLOR);
                } else {
                    comp.setBackground(table.getBackground());
                }
            }

            // インデントを入れる
            if (value != null) {
                if (value instanceof String) {
                    comp.setText("  " + value);
                } else {
                    comp.setText("  " + value.toString());
                }
            } else {
                comp.setText("");
            }

            return comp;
        }
    }

    /**
     * JComboBox を細かくコントロールするための Cell Editor.
     */
    private class MyCellEditor extends PNSCellEditor {
        private static final long serialVersionUID = 1L;

        JComboBox combo;

        public MyCellEditor(JComboBox c) {
            super(c);
            int clkCountToStart = Project.getPreferences().getInt("diagnosis.table.clickCountToStart", 1);
            setClickCountToStart(clkCountToStart);
            this.combo = c;

            // JComboBox の値を変更しなかったとき，editor が残ってしまうのを回避
            combo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    cancelCellEditing();
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int col) {

            // value は String で入ってくる　combo.setSelectedItem(value)は効かない
            switch (col) {
                case CATEGORY_COL:
                case OUTCOME_COL:
                    if (value != null) {
                        int index = itemToIndex(combo, value.toString());
                        combo.setSelectedIndex(index);
                    }
                    break;
            }
            return combo;
        }
    }
}
