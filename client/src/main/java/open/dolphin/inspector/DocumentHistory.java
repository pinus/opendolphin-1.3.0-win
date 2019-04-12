package open.dolphin.inspector;

import open.dolphin.client.BlockGlass;
import open.dolphin.client.Chart;
import open.dolphin.client.ChartImpl;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.dto.DocumentSearchSpec;
import open.dolphin.helper.DBTask;
import open.dolphin.helper.PNSPair;
import open.dolphin.helper.PNSTriple;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.project.Project;
import open.dolphin.ui.*;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.*;

/**
 * 文書履歴を取得し，表示するクラス.
 *
 * @author Minagawa, Kazushi
 * @author pns
 */
public class DocumentHistory implements IInspector {
    public static final InspectorCategory CATEGORY = InspectorCategory.文書履歴;
    // context
    private final ChartImpl context;
    // 文書履歴テーブル
    private ObjectReflectTableModel<DocInfoModel> tableModel;
    //private DocumentHistoryView view;
    private DocumentHistoryPanel view;
    // 抽出期間コンボボックス
    private JComboBox<PNSPair<String, Integer>> extractionCombo;
    // 件数フィールド
    private JLabel countField;
    // DocumentHistoryUpdate リスナ
    private DocumentHistoryUpdateListener updateListener;
    // DocumentHistorySelection リスナ
    private DocumentHistorySelectionListener selectionListener;
    // 選択された文書情報(DocInfo)の配列
    private DocInfoModel[] selectedHistories;
    // 抽出開始日
    private Date extractionPeriod;
    // 自動的に取得する文書数
    private int autoFetchCount;
    // 昇順降順のフラグ
    private boolean ascending;
    // 修正版も表示するかどうかのフラグ
    private boolean showModified;
    // Key入力をブロックするリスナ
    private BlockKeyListener blockKeyListener;
    // 編集終了したカルテの日付を保存する. 編集終了した時に必ず選択するために使う
    private String editDate = null;
    // ロガー
    private Logger logger = Logger.getLogger(DocumentHistory.class);

    /**
     * 文書履歴オブジェクトを生成する.
     *
     * @param parent PatientInspector
     */
    public DocumentHistory(PatientInspector parent) {
        context = parent.getContext();
        // Preference から自動文書取得数を設定する
        autoFetchCount = Project.getPreferences().getInt(Project.DOC_HISTORY_FETCHCOUNT, 1);
        // Preference から昇順降順を設定する
        ascending = Project.getPreferences().getBoolean(Project.DIAGNOSIS_ASCENDING, false);
        // Preference から修正履歴表示を設定する
        showModified = Project.getPreferences().getBoolean(Project.DOC_HISTORY_SHOWMODIFIED, false);

        initComponent();
        connect();
    }

    /**
     * GUI コンポーネントを生成する.
     */
    private void initComponent() {

        view = new DocumentHistoryPanel();
        view.setName(CATEGORY.name());

        // サイズ
        view.setPreferredSize(new Dimension(DEFAULT_WIDTH, 350));

        // selectAll (command-A) を横取りするため削除
        view.getInputMap().remove(KeyStroke.getKeyStroke("meta A"));

        // 履歴テーブルのパラメータを取得する
        List<PNSTriple<String, Class<?>, String>> reflectList = Arrays.asList(
                new PNSTriple<>(" 確定日", String.class, "getFirstConfirmDateTrimTime"),
                new PNSTriple<>(" 内容", String.class, "getTitle")
        );

        // 文書履歴テーブルを生成する
        tableModel = new ObjectReflectTableModel<DocInfoModel>(reflectList) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) {
                // "内容" は editable
                return col == 1 && getObject(row) != null;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                // 内容コラム以外，またはデータがない場合は何もしない
                if (col != 1 || value == null || value.equals("")) {
                    return;
                }

                DocInfoModel docInfo = getObject(row);
                if (docInfo == null) {
                    return;
                }

                // 文書タイトルを変更し通知する
                docInfo.setTitle((String) value);
                titleChanged(docInfo);
            }
        };
        view.getTable().setModel(tableModel);

        // カラム幅を調整する
        // カラム幅は日付の入る１カラム目だけを固定にする
        view.getTable().getColumnModel().getColumn(0).setPreferredWidth(90);
        view.getTable().getColumnModel().getColumn(0).setMaxWidth(90);
        view.getTable().getColumnModel().getColumn(0).setMinWidth(90);

        // タイトルカラムに CellEditor を登録
        JTextField tf = new JTextField();
        tf.setBackground(Color.WHITE);
        IMEControl.setImeOnIfFocused(tf);
        TableColumn column = view.getTable().getColumnModel().getColumn(1);
        column.setCellEditor(new PNSCellEditor(tf));

        // isReadOnly対応
        tf.setEnabled(!context.isReadOnly());

        // 奇数偶数レンダラを設定する
        view.getTable().setDefaultRenderer(Object.class, new IndentTableCellRenderer(IndentTableCellRenderer.NARROW));

        // 抽出機関 ComboBox を生成する
        extractionCombo = view.getExtractCombo();

        // 件数フィールドを生成する
        countField = view.getCntLbl();

        // 文書履歴テーブルのキーボード入力をブロックするリスナ
        blockKeyListener = new BlockKeyListener();
    }

    /**
     * Event 接続を行う.
     */
    private void connect() {
        // 履歴テーブルで選択された行の文書を表示する
        view.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                DocInfoModel[] selected = Arrays.stream(view.getTable().getSelectedRows()).boxed()
                        .map(tableModel::getObject).filter(Objects::nonNull).toArray(DocInfoModel[]::new);
                setSelectedHistories(selected.length > 0 ? selected : null);
            }
        });

        // ENTER で cell edit 開始
        view.getTable().getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "startEditing");
        view.getTable().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                int[] row = view.getTable().getSelectedRows();
                if (row.length == 1) {
                    view.getTable().changeSelection(row[0], 1, false, false);
                }
            }
        });

        // column 0 をクリックした場合は column 1 を選択
        view.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                int col = view.getTable().columnAtPoint(p);
                if (col == 0) {
                    int row = view.getTable().rowAtPoint(p);
                    view.getTable().changeSelection(row, 1, false, false);
                }
            }
        });

        // 抽出期間コンボボックスの選択を処理する
        extractionCombo.addItemListener(this::periodChanged);

        // Preference から抽出期間を設定する
        int past = Project.getPreferences().getInt(Project.DOC_HISTORY_PERIOD, -12);
        int index = PNSPair.getIndex(past, ComboBoxFactory.getDocumentExtractionPeriodModel());
        extractionCombo.setSelectedIndex(index);

        LocalDate startDate = LocalDate.now().plusMonths(past);
        // ここで update がかかる
        setExtractionPeriod(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    /**
     * 全部のカルテを選択する command-A を押すと，KarteDocumentViewer の selectAll が呼ばれて，そこからここが呼ばれる.
     */
    public void selectAll() {
        int r = tableModel.getObjectCount(); //rowCount だとだめ。データがないところも全部選択されてしまう
        ListSelectionModel lsm = view.getTable().getSelectionModel();
        lsm.setSelectionInterval(0, r - 1);
    }

    /**
     * 履歴テーブルのコレクションを clear する.
     */
    public void clear() {
        tableModel.clear();
    }

    /**
     * update listener (from DocumentBridge)
     *
     * @param listener DocumentHistoryUpdateListener
     */
    public void addDocumentHistoryUpdateListener(DocumentHistoryUpdateListener listener) {
        updateListener = listener;
    }

    /**
     * selection listener (from DocumentBridge)
     *
     * @param listener DocumentHistorySelectionListener
     */
    public void addDocumentHistorySelectionListener(DocumentHistorySelectionListener listener) {
        selectionListener = listener;
    }

    /**
     * 選択された文書履歴(複数)を返す.
     *
     * @return 選択された文書履歴(複数)
     */
    public DocInfoModel[] getSelectedHistories() {
        return selectedHistories;
    }

    /**
     * 束縛プロパティの選択された文書履歴(複数)を設定する. 通知を行う.
     *
     * @param newSelected 選択された文書履歴(複数)
     */
    public void setSelectedHistories(DocInfoModel[] newSelected) {

        DocInfoModel[] old = selectedHistories;
        selectedHistories = newSelected;
        //
        // リスナへ通知を行う  -> DocumentBridgeImpl
        //
        if (selectedHistories != null) {
            selectionListener.selected(selectedHistories);
        }
    }

    /**
     * 履歴の検索時にテーブルのキー入力をブロックする.
     *
     * @param busy true の時検索中
     */
    public void blockHistoryTable(boolean busy) {
        BlockGlass blockGlass = (BlockGlass) context.getFrame().getGlassPane();
        if (busy) {
            view.getTable().addKeyListener(blockKeyListener);
            blockGlass.setText("読み込み中");
            blockGlass.block();
        } else {
            view.getTable().removeKeyListener(blockKeyListener);
            blockGlass.setText("");
            blockGlass.unblock();
        }
    }

    /**
     * 文書履歴を Karte から取得し表示する.
     */
    public void showHistory() {
        //List list = context.getKarte().getEntryCollection("docInfo");
        List<DocInfoModel> list = context.getKarte().getDocInfoEntry();
        updateHistory(list);
    }

    /**
     * 文書履歴を取得，更新する.
     * 取得するパラメータ(患者ID，文書タイプ，抽出期間)はこのクラスの属性として定義されている.
     * これらのパラメータは comboBox等で選択される. 値が変化する度にこのメソッドがコールされる.
     */
    @Override
    public void update() {

        if (extractionPeriod != null) {

            // 検索パラメータセットのDTOを生成する
            DocumentSearchSpec spec = new DocumentSearchSpec();
            spec.setKarteId(context.getKarte().getId());    // カルテID
            spec.setDocType(IInfoModel.DOCTYPE_KARTE);            // 文書タイプ
            spec.setFromDate(extractionPeriod);            // 抽出期間開始
            spec.setIncludeModifid(showModified);        // 修正履歴
            spec.setCode(DocumentSearchSpec.DOCTYPE_SEARCH);    // 検索タイプ
            spec.setAscending(ascending);

            DocInfoTask task = new DocInfoTask(context, spec, new DocumentDelegater());
            task.execute();
            // DocInfoTask から updateHistory が呼ばれる.
        }
    }

    /**
     * KarteEditor が編集終了したときはここで更新するようにする.
     * 編集したカルテを必ず選択するため.
     *
     * @param date ISO_DATE
     */
    public void update(String date) {
        // 選択状態にすべきカルテを editDate に入れてから update()
        editDate = date;
        update();
    }

    /**
     * 抽出期間の変化・カルテの編集終了で，履歴を再取得した場合等の処理で，
     * 履歴テーブルの更新， 最初の行の自動選択，束縛プロパティの変化通知を行う.
     * update() -> DocInfoTask から呼ばれる.
     */
    private void updateHistory(List<DocInfoModel> newHistory) {

        // ソーティングする
        if (newHistory != null && !newHistory.isEmpty()) {
            if (ascending) {
                newHistory.sort(Comparator.naturalOrder());
            } else {
                newHistory.sort(Comparator.reverseOrder());
            }
        }

        // 抽出期間が変化しても，できるだけ現在の選択を再現するため保存しておく
        JTable table = view.getTable();
        List<String> oldSelection = new ArrayList<>();
        for (int r : table.getSelectedRows()) {
            int row = table.convertRowIndexToModel(r);
            String date = tableModel.getObject(row).getFirstConfirmDateTrimTime();
            oldSelection.add(date);
        }

        // 文書履歴テーブルにデータを設定する
        tableModel.setObjectList(newHistory);
        // 束縛プロパティの通知を行う -> DocumetnBridgeImpl
        updateListener.updated(editDate);

        if (newHistory != null && !newHistory.isEmpty()) {
            int historySize = newHistory.size();
            countField.setText(historySize + " 件");
            int fetchCount = historySize > autoFetchCount ? autoFetchCount : historySize;

            ListSelectionModel selectionModel = table.getSelectionModel();

            // 編集終了後に呼ばれた場合（editDate != null）は，そのカルテを必ず選択状態にする
            if (editDate != null) {
                oldSelection.add(editDate);
                editDate = null;
            }

            // oldSelection を復元する
            selectionModel.setValueIsAdjusting(true);

            oldSelection.forEach(oldDate -> {
                for (int i = 0; i < newHistory.size(); i++) {
                    String date = newHistory.get(i).getFirstConfirmDateTrimTime();
                    if (date.equals(oldDate)) {
                        int row = table.convertRowIndexToView(i);
                        selectionModel.addSelectionInterval(row, row);
                    }
                }
            });

            // 復元した結果，選択がない場合はデフォルトの選択
            // ascending なら最後の行の fetchCount 分を選択
            // descending なら最初の fetchCount 分
            if (table.getSelectedRowCount() == 0) {
                // テーブルの最初の行の自動選択を行う
                int first = ascending ? historySize - fetchCount : 0;
                int last = ascending ? historySize - 1 : fetchCount - 1;
                // 選択
                selectionModel.addSelectionInterval(first, last);
                // 選択した行が表示されるようにスクロールする
                Rectangle r = table.getCellRect(first, last, true);
                table.scrollRectToVisible(r);
            }

            selectionModel.setValueIsAdjusting(false);

            // LastVisit update
            context.getLastVisit().update(newHistory);

        } else {
            // カルテが見つかるまで抽出期間を自動的に延ばす
            int selected = extractionCombo.getSelectedIndex();
            if (selected < extractionCombo.getItemCount() - 1) {
                extractionCombo.setSelectedIndex(++selected);
                //System.out.println("extraction period extended to " + extractionObjects[selected]);
            } else {
                // 最後まで見つからない＝初診カルテ
                countField.setText("0 件");
                // LastVisit update
                context.getLastVisit().update(newHistory);
            }
        }
    }

    /**
     * 文書履歴のタイトルを変更する.
     *
     * @param docInfo DocInfoModel
     */
    public void titleChanged(DocInfoModel docInfo) {
        if (docInfo != null && docInfo.getTitle() != null) {
            ChangeTitleTask task = new ChangeTitleTask(context, docInfo, new DocumentDelegater());
            task.execute();
        }
    }

    /**
     * 抽出期間を変更し再検索する.
     *
     * @param e ItemEvent
     */
    public void periodChanged(ItemEvent e) {

        if (e.getStateChange() == ItemEvent.SELECTED) {
            int index = extractionCombo.getSelectedIndex();
            int addValue = ComboBoxFactory.getDocumentExtractionPeriodModel().get(index).getValue();

            GregorianCalendar today = new GregorianCalendar();
            today.add(GregorianCalendar.MONTH, addValue);
            today.clear(Calendar.HOUR_OF_DAY);
            today.clear(Calendar.MINUTE);
            today.clear(Calendar.SECOND);
            today.clear(Calendar.MILLISECOND);
            setExtractionPeriod(today.getTime());
        }
    }

    /**
     * レイアウトパネルを返す.
     *
     * @return JPanel
     */
    @Override
    public JPanel getPanel() {
        return view;
    }

    @Override
    public String getName() {
        return CATEGORY.name();
    }

    @Override
    public String getTitle() {
        return CATEGORY.title();
    }

    /**
     * ChartImpl のフォーカス処理用.
     *
     * @return DocumentHistoryTable
     */
    public JTable getDocumentHistoryTable() {
        return view.getTable();
    }

    /**
     * 検索パラメータの抽出期間を返す.
     *
     * @return 抽出期間
     */
    public Date getExtractionPeriod() {
        return extractionPeriod;
    }

    /**
     * 検索パラメータの抽出期間を設定する.
     *
     * @param extractionPeriod 抽出期間
     */
    public void setExtractionPeriod(Date extractionPeriod) {
        this.extractionPeriod = extractionPeriod;
        update();
    }

    /**
     * 文書履歴表示の昇順/降順を返す.
     *
     * @return 昇順の時 true
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * 文書履歴表示の昇順/降順を設定する.
     *
     * @param ascending 昇順の時 true
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
        update();
    }

    /**
     * 修正版を表示するかどうかを返す.
     *
     * @return 表示する時 true
     */
    public boolean isShowModified() {
        return showModified;
    }

    /**
     * 修正版を表示するかどうかを設定する.
     *
     * @param showModifyed 表示する時 true
     */
    public void setShowModified(boolean showModifyed) {
        this.showModified = showModifyed;
        update();
    }

    /**
     * キーボード入力をブロックするリスナクラス.
     */
    private class BlockKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            e.consume();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            e.consume();
        }
    }

    /**
     * 検索タスク.
     */
    private class DocInfoTask extends DBTask<List<DocInfoModel>> {

        // Delegator
        private final DocumentDelegater ddl;
        // 検索パラメータを保持するオブジェクト
        private final DocumentSearchSpec spec;

        public DocInfoTask(Chart ctx, DocumentSearchSpec spec, DocumentDelegater ddl) {
            super(ctx);
            this.spec = spec;
            this.ddl = ddl;
        }

        @Override
        protected List<DocInfoModel> doInBackground() {
            List<DocInfoModel> result = ddl.getDocInfoList(spec);
            //logger.info("DocInfoTask result count: " + result.size());
            return ddl.isNoError() ? result : null;
        }

        @Override
        protected void succeeded(List<DocInfoModel> result) {
            //logger.debug("DocInfoTask succeeded");
            if (result != null) {
                updateHistory(result);
            }
        }
    }

    /**
     * タイトル変更タスククラス.
     */
    private class ChangeTitleTask extends DBTask<Boolean> {

        // DocInfo
        private final DocInfoModel docInfo;
        // Delegator
        private final DocumentDelegater ddl;

        public ChangeTitleTask(Chart ctx, DocInfoModel docInfo, DocumentDelegater ddl) {
            super(ctx);
            this.docInfo = docInfo;
            this.ddl = ddl;
        }

        @Override
        protected Boolean doInBackground() {
            //logger.debug("ChangeTitleTask started");
            ddl.updateTitle(docInfo);
            return ddl.isNoError();
        }

        @Override
        protected void succeeded(Boolean result) {
            //logger.debug("ChangeTitleTask succeeded");
        }
    }
}
