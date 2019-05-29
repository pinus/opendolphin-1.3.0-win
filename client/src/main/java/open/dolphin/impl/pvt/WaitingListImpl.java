package open.dolphin.impl.pvt;

import open.dolphin.client.*;
import open.dolphin.delegater.DolphinClientContext;
import open.dolphin.delegater.PatientDelegater;
import open.dolphin.delegater.PvtDelegater;
import open.dolphin.dto.PvtStateSpec;
import open.dolphin.event.BadgeEvent;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.PNSTriple;
import open.dolphin.helper.ScriptExecutor;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.KarteState;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.project.Project;
import open.dolphin.ui.IMEControl;
import open.dolphin.ui.IndentTableCellRenderer;
import open.dolphin.ui.ObjectReflectTableModel;
import open.dolphin.ui.PNSBadgeTabbedPane;
import open.dolphin.ui.sheet.JSheet;
import open.dolphin.util.Gengo;
import open.dolphin.util.ModelUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.prefs.Preferences;

/**
 * 受付リスト.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.,
 * @author pns
 */
public class WaitingListImpl extends AbstractMainComponent {
    private static final String NAME = "受付リスト";

    // 来院情報テーブルの年齢カラム
    public static final int AGE_COLUMN = 5;
    public static final int BIRTHDAY_COLUMN = 6;
    // アイコン
    protected static final ImageIcon DONE_ICON = GUIConst.ICON_CHECK_LIGHTBLUE_16;
    protected static final ImageIcon OPEN_ICON = GUIConst.ICON_BOOK_OPEN_16;
    protected static final ImageIcon OPEN_READ_ONLY_ICON = GUIConst.ICON_BOOK_OPEN_16;
    protected static final ImageIcon UNFINISHED_ICON = GUIConst.ICON_CHECK_RED_16;
    protected static final ImageIcon TEMPORARY_ICON = GUIConst.ICON_ONEDIT_16;
    protected static final ImageIcon OPEN_USED_NONE = GUIConst.ICON_USER_WHITE_16;
    protected static final ImageIcon OPEN_USED_SAVE = GUIConst.ICON_USER_BLUE_16;
    protected static final ImageIcon OPEN_USED_UNFINISHED = GUIConst.ICON_USER_RED_16;
    protected static final Color SHOSHIN_COLOR = new Color(180, 220, 240); //青っぽい色
    protected static final Color KARTE_EMPTY_COLOR = new Color(250, 200, 160); //茶色っぽい色
    protected static final Color DIAGNOSIS_EMPTY_COLOR = new Color(243, 255, 15); //黄色
    // Font
    private static final Font NORMAL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final Font SMALL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
    // JTableレンダラ用のカラー
    private static final Color MALE_COLOR = new Color(230, 243, 243);
    private static final Color FEMALE_COLOR = new Color(254, 221, 242);
    private static final Color CANCEL_PVT_COLOR = new Color(128, 128, 128);
    // テーブルの row height
    private static final int ROW_HEIGHT = 18;
    // WaitingList の表示パネル
    private WaitingListPanel view;
    // PVT Table
    private JTable pvtTable;
    private ObjectReflectTableModel<PatientVisitModel> pvtTableModel;
    // Preference
    private Preferences preferences;
    // 性別レンダラフラグ
    private boolean sexRenderer;
    // 生年月日の元号表示
    private boolean ageDisplay;
    // 選択されている患者情報
    private PatientVisitModel[] selectedPvt; // 複数行選択対応
    private int[] selectedIndex; // 複数行選択対応
    // Pvt チェックの runnable
    private PvtChecker pvtChecker;
    // PvtChecker の臨時起動等, runnable を乗せるための ExecutorService
    private ExecutorService executor;
    // 時計を更新するための ExecutorService
    private ScheduledExecutorService schedule;
    // Logger
    private Logger logger = Logger.getLogger(WaitingListImpl.class);

    /**
     * Creates new WaitingList.
     */
    public WaitingListImpl() {
        setName(NAME);
        // 使い回すオブジェクト
        schedule = Executors.newSingleThreadScheduledExecutor();
        executor = Executors.newSingleThreadExecutor();
        pvtChecker = new PvtChecker();
    }

    /**
     * プログラムを開始する.
     */
    @Override
    public void start() {
        setup();
        initComponents();
        connect();

        // 初回のチェック
        executor.submit(pvtChecker);

        // １分ごとに setCheckedTime() を呼んで待ち時間を更新して時計として使う
        Runnable r = () -> setCheckedTime(LocalDateTime.now());
        schedule.scheduleAtFixedRate(r, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * Preferences を取得.
     */
    private void setup() {
        preferences = Preferences.userNodeForPackage(this.getClass());
        sexRenderer = preferences.getBoolean("sexRenderer", false);
        ageDisplay = preferences.getBoolean("ageDisplay", true);
    }

    /**
     * GUI コンポーネントを初期化しレアイアウトする.
     */
    private void initComponents() {

        view = new WaitingListPanel();
        setUI(view);

        // popup で状態アイコンの legend を出す
        JLabel legend = view.getLegendLbl();
        legend.setIcon(GUIConst.ICON_QUESTION_16);
        legend.setText("");
        view.getLegendLbl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu legend = new LegendPopup();
                legend.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        // 来院テーブル用のパラメータ
        List<PNSTriple<String, Class<?>, String>> reflectList = Arrays.asList(
                new PNSTriple<>(" 受付", Integer.class, "getNumber"),
                new PNSTriple<>("　患者 ID", String.class, "getPatientId"),
                new PNSTriple<>("　来院時間", String.class, "getPvtDateTrimDate"),
                new PNSTriple<>("　氏　　名", String.class, "getPatientName"),
                new PNSTriple<>(" 性別", String.class, "getPatientGenderDesc"),
                new PNSTriple<>("　年齢", String.class, "getPatientAge"),
                new PNSTriple<>("　生年月日", String.class, "getPatientBirthday"),
                new PNSTriple<>("　ドクター", String.class, "getAssignedDoctorName"),
                new PNSTriple<>(" メモ", String.class, "getMemo"),
                new PNSTriple<>(" 予約", String.class, "getAppointment"),
                new PNSTriple<>("状態", Integer.class, "getState")
        );
        int[] columnWidth = {34, 68, 72, 140, 40, 50, 100, 75, 50, 40, 30};

        // 生成する
        pvtTable = view.getTable();
        pvtTableModel = new ObjectReflectTableModel<PatientVisitModel>(reflectList) {
            @Override
            public Object getValueAt(int row, int col) {
                Object obj = super.getValueAt(row, col);
                return (col == BIRTHDAY_COLUMN && ageDisplay) ? Gengo.toGengo((String) obj) : obj;
            }
        };
        pvtTable.setModel(pvtTableModel);

        // 来院情報テーブルの属性を設定する
        pvtTable.setRowHeight(ROW_HEIGHT);
        pvtTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        pvtTable.setRowSelectionAllowed(true);

        // sorter を設定
        TableRowSorter<ObjectReflectTableModel> sorter = new TableRowSorter<ObjectReflectTableModel>(pvtTableModel) {
            // ASCENDING -> DESCENDING -> 初期状態 と切り替える
            @Override
            public void toggleSortOrder(int column) {
                if (column >= 0 && column < getModelWrapper().getColumnCount() && isSortable(column)) {
                    List<RowSorter.SortKey> keys = new ArrayList<>(getSortKeys());
                    if (!keys.isEmpty()) {
                        RowSorter.SortKey sortKey = keys.get(0);
                        if (sortKey.getColumn() == column && sortKey.getSortOrder() == SortOrder.DESCENDING) {
                            setSortKeys(null);
                            return;
                        }
                    }
                }
                super.toggleSortOrder(column);
            }
        };
        pvtTable.setRowSorter(sorter);

        // 年齢コラム 32.10 の型式をソートできるようにする
        sorter.setComparator(AGE_COLUMN, Comparator.comparing(x -> {
            String[] age = ((String) x).split("\\.");
            int y = Integer.valueOf(age[0]); // 歳
            int m = Integer.valueOf(age[1]); // ヶ月
            return 100 * y + m;
        }));

        // 生年月日コラム
        sorter.setComparator(BIRTHDAY_COLUMN,
                Comparator.comparing(x -> ageDisplay ? Gengo.toSeireki((String) x) : (String) x));

        // コラム幅の設定
        for (int i = 0; i < columnWidth.length; i++) {
            TableColumn column = pvtTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidth[i]);
            if (i != 3 && i != 7) { //固定幅
                column.setMaxWidth(columnWidth[i]);
                column.setMinWidth(columnWidth[i]);
            }
        }

        // レンダラを生成する
        MaleFemaleRenderer sRenderer = new MaleFemaleRenderer();
        MaleFemaleRenderer centerRenderer = new MaleFemaleRenderer(JLabel.CENTER);
        MaleFemaleRenderer rightRenderer = new MaleFemaleRenderer(JLabel.RIGHT);
        KarteStateRenderer stateRenderer = new KarteStateRenderer();

        pvtTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // 0 受付
        pvtTable.getColumnModel().getColumn(1).setCellRenderer(sRenderer); // 1 患者 ID
        pvtTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // 2 来院時間
        pvtTable.getColumnModel().getColumn(3).setCellRenderer(sRenderer); // 3 氏名
        pvtTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // 4 性別
        pvtTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // 5 年齢
        pvtTable.getColumnModel().getColumn(6).setCellRenderer(sRenderer); // 6 生年月日
        pvtTable.getColumnModel().getColumn(7).setCellRenderer(sRenderer); // 7 ドクター
        pvtTable.getColumnModel().getColumn(8).setCellRenderer(sRenderer); // 8 メモ
        pvtTable.getColumnModel().getColumn(9).setCellRenderer(sRenderer); // 9 予約
        pvtTable.getColumnModel().getColumn(10).setCellRenderer(stateRenderer);

        // 日付ラベルに値を設定する
        setOperationDate(LocalDateTime.now());
        // 来院数を表示する
        updatePvtCount();
    }

    /**
     * コンポーネントにイベントハンドラーを登録し相互に接続する.
     */
    private void connect() {

        // ChartImpl から PatientVisitModel を受け取って update する.
        ChartImpl.addPvtListener(this::updateState);

        // 靴のアイコンをクリックした時来院情報をフルチェックする
        view.getKutuBtn().addActionListener(e -> executor.submit(pvtChecker));

        // コンテキストリスナを登録する
        pvtTable.addMouseListener(new ContextListener());

        // ListSelectionListener を組み込む
        pvtTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int[] rows = pvtTable.getSelectedRows();
                if (rows == null) {
                    setSelectedPvt(null);
                } else {
                    PatientVisitModel[] patients = new PatientVisitModel[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        rows[i] = pvtTable.convertRowIndexToModel(rows[i]);
                        patients[i] = pvtTableModel.getObject(rows[i]);
                    }
                    setSelectedPvt(patients);
                }
            }
        });

        // SPACE でカルテオープン
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "openKarte");
        pvtTable.getActionMap().put("openKarte", new ProxyAction(this::openKarte));

        // command-F で search field にフォーカスする裏コマンド
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("meta F"), "showWaitingList");
        pvtTable.getActionMap().put("showWaitingList", new ProxyAction(((Dolphin)getContext())::showPatientSearch));

        // ENTER の行送りをやめる
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doNothing");
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"), "doNothing");
        pvtTable.getActionMap().put("doNothing", new ProxyAction(() -> {}));

        // Tab キー
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("TAB"), "focusPrevious");
        pvtTable.getInputMap().put(KeyStroke.getKeyStroke("shift TAB"), "focusPrevious");
        pvtTable.getActionMap().put("focusPrevious", new ProxyAction(KeyboardFocusManager.getCurrentKeyboardFocusManager()::focusPreviousComponent));

        // pvt 受信待ち endpoint
        PvtEndpoint endpoint = new PvtEndpoint();
        DolphinClientContext.getContext().setEndpoint(endpoint);
        endpoint.addPvtListener(this::hostPvtReceiver);
    }

    /**
     * メインウインドウのタブで受付リストに切り替わった時コールされる.
     */
    @Override
    public void enter() {
        IMEControl.setImeOff(view);
        controlMenu();
    }

    /**
     * プログラムを終了する.
     */
    @Override
    public void stop() {
    }

    /**
     * 性別レンダラかどうかを返す.
     *
     * @return 性別レンダラの時 true
     */
    public boolean isSexRenderer() {
        return sexRenderer;
    }

    /**
     * レンダラをトグルで切り替える.
     */
    public void switchRenderer() {
        sexRenderer = !sexRenderer;
        preferences.putBoolean("sexRenderer", sexRenderer);
        if (pvtTable != null) {
            pvtTableModel.fireTableDataChanged();
        }
    }

    /**
     * 来院情報を取得する日を設定する.
     *
     * @param date 取得する日
     */
    private void setOperationDate(LocalDateTime date) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd (EE)"); // 2006-11-20 (水)
        view.getDateLbl().setText(dtf.format(date));
    }

    /**
     * 来院情報をチェックした時刻を設定する.
     *
     * @param time チェックした時刻
     */
    private void setCheckedTime(LocalDateTime time) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        view.getCheckedTimeLbl().setText(dtf.format(time));
    }

    /**
     * 来院数, 待人数, 待時間表示.
     */
    private void updatePvtCount() {
        List<PatientVisitModel> pvtList = pvtTableModel.getObjectList();
        int pvtCount = pvtList.size();
        int waitingCount = 0;
        String waitingTime = "00:00";

        boolean found = false; // 待ち患者がいるかどうか
        int continuousCount = 0;

        for (int i = 0; i < pvtCount; i++) {
            PatientVisitModel pvt = pvtList.get(i);
            int state = pvt.getState();
            if (state == KarteState.CLOSE_NONE || state == KarteState.OPEN_NONE) {
                // 診察未終了レコードをカウント, 最初に見つかった未終了レコードの時間から待ち時間を計算
                waitingCount++;
                // 診療未終了レコードの連続回数をカウント
                continuousCount++;

                if (!found) {
                    Date pvtDate = ModelUtils.getDateTimeAsObject(pvt.getPvtDate());
                    waitingTime = DurationFormatUtils.formatPeriod(pvtDate.getTime(), new Date().getTime(), "HH:mm");
                    found = true;
                }
            } else {
                // 診療未終了レコードが２件以下しかなければ待ち時間に入れない（待合室にいなかった人と判断）
                if (continuousCount > 0 && continuousCount <= 2) {
                    //System.out.println("continuous count: " + continuousCount);
                    found = false;
                    continuousCount = 0;
                }
            }
        }
        view.getCountLbl().setText(String.format("来院数%d人, 待ち%d人, 待ち時間 %s", pvtCount, waitingCount, waitingTime));

        // PNSBadgeTabbedPane に待ち人数を伝える
        PNSBadgeTabbedPane pane = ((Dolphin) getContext()).getTabbedPane();
        BadgeEvent e = new BadgeEvent(this);
        e.setBadgeNumber(waitingCount);
        e.setTabIndex(pane.indexOfTab(getName()));
        pane.setBadge(e);

        // Dock のアイコンにバッジを出す
        //com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
        //app.setDockIconBadge(waitingCount == 0 ? null : String.valueOf(waitingCount));
    }

    /**
     * テーブル及び靴アイコンの enable/disable 制御を行う.
     *
     * @param busy pvt 検索中は true
     */
    public void setBusy(boolean busy) {

        view.getKutuBtn().setEnabled(!busy);

        if (busy) {
            getContext().block();
            selectedIndex = pvtTable.getSelectedRows();
        } else {
            getContext().getGlassPane().setText("");
            getContext().unblock();
            if (selectedIndex != null && selectedIndex.length != 0) {
                for (int i = 0; i < selectedIndex.length; i++) {
                    pvtTable.getSelectionModel().addSelectionInterval(selectedIndex[i], selectedIndex[i]);
                }
            }
        }
    }

    /**
     * 選択されている来院情報を設定返す.
     *
     * @return 選択されている来院情報
     */
    public PatientVisitModel[] getSelectedPvt() {
        return selectedPvt;
    }

    /**
     * 選択された来院情報を設定する.
     *
     * @param selectedPvt selected PatientVisitModel
     */
    public void setSelectedPvt(PatientVisitModel[] selectedPvt) {
        this.selectedPvt = selectedPvt;
        controlMenu();
    }

    /**
     * カルテオープンメニューを制御する.
     */
    private void controlMenu() {
        getContext().enableAction(GUIConst.ACTION_OPEN_KARTE, canOpen());
    }

    /**
     * Popupメニューから, 現在選択されている全ての患者のカルテを開く.
     */
    public void openKarte() {
        PatientVisitModel[] pvtModel = getSelectedPvt();
        if (pvtModel != null) {
            Arrays.asList(pvtModel).forEach(this::openKarte);
        }
    }

    /**
     * 指定されたカルテを開く.
     *
     * @param pvtModel PatientVisitModel
     */
    public void openKarte(final PatientVisitModel pvtModel) {

        if (canOpen(pvtModel)) {
            setBusy(true);

            // isReadOnly対応
            Runnable r = () -> {
                // 健康保険情報をフェッチする
                PatientDelegater ptdl = new PatientDelegater();
                ptdl.fetchHealthInsurance(pvtModel.getPatient());

                // 現在の state をサーバからとってくる
                PvtDelegater pvdl = new PvtDelegater();
                int state = pvdl.getPvtState(pvtModel.getId());
                // 読んだら table を update 　　　→ カルテが開くと update がよばれるのでここでは不要
                //int row = getRowForPvt(pvtModel);
                //pvtModel.setState(state);
                //pvtTableModel.fireTableRowsUpdated(row, row);

                // すでに OPEN ならどっかで開いているということなので編集不可に設定
                if (KarteState.isOpen(state)) {
                    openReadOnlyKarte(pvtModel, state);
                }
                // OPEN でなければ, 通常どおりオープン （Dolphin#openKarte を呼ぶ）
                else {
                    getContext().openKarte(pvtModel);
                }
                setBusy(false);
                // startCheckTimer(); // openKarte すると ChartImpl が open するので, updateState が必ず呼ばれるので, そちらで startCheckTimer される
            };
            // ここは database とは関係ないので thread で
            Thread t = new Thread(r);
            t.start();

        } else {
            // 既に開かれていれば, そのカルテを前に
            ChartImpl.toFront(pvtModel);
            EditorFrame.toFront(pvtModel);
        }
    }

    /**
     * Read Only でカルテを開く.
     *
     * @param pvtModel PatientVisitModel
     * @param state State
     */
    @Override
    public void openReadOnlyKarte(final PatientVisitModel pvtModel, final int state) {
        // 元々 ReadOnly のユーザーならそのまま開いて OK
        if (Project.isReadOnly()) {
            getContext().openKarte(pvtModel);
            return;
        }

        // ダイアログで確認する
        String ptName = pvtModel.getPatientName();
        String[] options = {"閲覧のみ", "強制的に編集", "キャンセル"};

        JOptionPane pane = new JOptionPane(
                "<html>" +
                        "<h3>" + ptName + " 様のカルテは他の端末で編集中です</h3>" +
                        "<p><nobr>強制的に編集した場合、後から保存したカルテが最新カルテになります<nobr></p></html>",
                JOptionPane.WARNING_MESSAGE);
        pane.setOptions(options);
        pane.setInitialValue(options[0]);
        // メッセージが改行しないように大きさをセット（JDialog なら自動でやってくれる）
        Dimension size = pane.getPreferredSize();
        size.width = 540; // cut and try
        pane.setPreferredSize(size);

        // JOptionPane から component を再帰検索して forceEditBtn を取り出す
        JButton tmpForce = null;

        Component[] components = pane.getComponents();
        List<Component> cc = java.util.Arrays.asList(components);

        while (!cc.isEmpty()) {
            List<Component> stack = new ArrayList<>();
            for (Component c : cc) {
                if (c instanceof JButton) {
                    JButton button = (JButton) c;
                    String name = button.getText();
                    if (options[1].equals(name)) {
                        tmpForce = button;
                    }

                } else if (c instanceof JComponent) {
                    components = ((JComponent) c).getComponents();
                    stack.addAll(java.util.Arrays.asList(components));
                }
            }
            cc = stack;
        }
        final JButton forceEditBtn = tmpForce;

        JSheet dialog = JSheet.createDialog(pane, getContext().getFrame());
        dialog.addSheetListener(se -> {
            int result = se.getOption();
            if (result == 0) { // 閲覧
                pvtModel.setState(KarteState.READ_ONLY);
                getContext().openKarte(pvtModel);
            } else if (result == 1) { // 強制編集
                pvtModel.setState(KarteState.toClosedState(state));
                getContext().openKarte(pvtModel);
            }
            // それ以外はキャンセル
        });

        // 強制編集ボタンにショートカット登録
        ActionMap am = dialog.getRootPane().getActionMap();
        InputMap im = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "force-edit");
        am.put("force-edit", new ProxyAction(forceEditBtn::doClick));

        dialog.setVisible(true);
    }

    /**
     * 現在の selectedPvt が canOpen かどうか判定.
     * １つでも開けられないものがあれば false とする.
     */
    private boolean canOpen() {

        PatientVisitModel[] pvt = getSelectedPvt();

        if (pvt == null || pvt.length == 0) {
            return false;
        } else {
            for (PatientVisitModel model : pvt) {
                // 既に開かれているカルテを openKarte すると toFront になるので,
                // open できないカルテは cancel されたカルテのみである
                if (isKarteCanceled(model)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 指定されたカルテを開くことが可能かどうかを返す.
     *
     * @return 開くことが可能な時 true
     */
    private boolean canOpen(PatientVisitModel pvt) {
        if (pvt == null || isKarteCanceled(pvt)) {
            return false;
        }
        return !ChartImpl.isKarteOpened(pvt);
    }

    /**
     * 受付がキャンセルされているかどうかを返す.
     *
     * @return キャンセルされている時 true
     */
    private boolean isKarteCanceled(PatientVisitModel pvtModel) {
        return pvtModel != null && pvtModel.getState() == KarteState.CANCEL_PVT;
    }

    /**
     * チャートステートの状態をデータベースに書き込む.
     * ChartImpl の windowOpened, windowClosed で呼ばれる.
     * state, byomeiCount, byomeiCountToday が変化している可能性がある.
     *
     * @param updated PatientVisitModel
     */
    public void updateState(final PatientVisitModel updated) {

        final int row = getRowForPvt(updated);
        if (row < 0) {
            logger.info("Something weird! Updated pvt is not found in the present pvtTable");
            return;
        }
        final int state = updated.getState();

        if (state != KarteState.READ_ONLY) {
            // データベースへの書き込み
            Runnable r = () -> {
                PvtDelegater pdl = new PvtDelegater();
                int serverState = pdl.getPvtState(updated.getId());
                // サーバが NONE 以外かつクライアントが NONE の時はサーバの state を優先する
                // i.e. NONE のカルテを強制変更でひらいて, 変更しないで終了した場合, 他の端末で SAVE になったのを NONE に戻してしまうのを防ぐ
                if (!KarteState.isNone(serverState) && KarteState.isNone(state)) {
                    updated.setState(serverState);
                }
                pdl.updatePvt(updated);
                pvtTableModel.fireTableRowsUpdated(row, row);
            };
            executor.submit(r);
        } else {
            // ReadOnly の時, state を読み直す
            Runnable r = () -> {
                PvtDelegater pdl = new PvtDelegater();
                updated.setState(pdl.getPvtState(updated.getId()));
                pvtTableModel.fireTableRowsUpdated(row, row);
            };
            executor.submit(r);
        }
    }

    /**
     * 終了時にカルテが OPEN になったまま残るのを防ぐためにすべて CLOSED に変換.
     *
     * @return stoppingTask
     */
    @Override
    public Callable<Boolean> getStoppingTask() {
        logger.info("WaitingListImpl stoppingTask starts");

        return () -> {
            // 開いているカルテを調べる
            // java.util.ConcurrentModificationException 対策のためにコピーで実行
            List<ChartImpl> allCharts = new ArrayList<>(ChartImpl.getAllChart());

            allCharts.stream()
                    .map(ChartImpl::getPatientVisit)
                    .filter(pvt -> pvt.getPvtDate() != null) // 今日の受診と関係あるカルテのみ選択
                    .forEach(pvt -> {
                        // 今日の受診がある場合は pvt status を変更する（open -> close)
                        int oldState = pvt.getState();
                        boolean isEmpty = new DocumentPeeker(pvt).isKarteEmpty();
                        int newState = KarteState.toClosedState(oldState, isEmpty);

                        // サーバに書き込む
                        PvtDelegater pdl = new PvtDelegater();
                        // NONE 以外から NONE への状態変更はありえないので無視
                        // NONE のカルテを強制変更でひらいて, 変更しないで終了した場合, 他の端末で SAVE になったのを NONE に戻してしまうのを防ぐ
                        int serverState = pdl.getPvtState(pvt.getId());
                        if (KarteState.isNone(serverState) || !KarteState.isNone(newState)) {
                            pvt.setState(newState);
                            pdl.updatePvt(pvt);
                        }
                    });
            return true;
        };
    }

    /**
     * 与えられた pvt に対応する TableModel の行を返す.
     * 要素が別オブジェクトになっている場合があるため, レコードIDで探す.
     *
     * @param updated PatientVisitModel
     * @return row
     */
    private int getRowForPvt(PatientVisitModel updated) {
        List<PatientVisitModel> pvtList = pvtTableModel.getObjectList();
        int updatedRow = -1;

        for (int i = 0; i < pvtList.size(); i++) {
            PatientVisitModel pvt = pvtList.get(i);
            if (updated.getId() == pvt.getId()) {
                updatedRow = i;
                break;
            }
        }
        return updatedRow;
    }

    /**
     * Server からの pvt message を受け取って pvtTableModel を更新する.
     *
     * @param hostPvt host pvt
     */
    public void hostPvtReceiver(PatientVisitModel hostPvt) {
        // 送られてきた pvt と同じものを local で探す
        // pvtDate & patientId で判定する → Patient ID が同じでも, pvtDate が違えば違う受付と判断する
        PatientVisitModel localPvt = null;
        int row = -1;
        int totalRowCount = pvtTableModel.getObjectCount();

        // hostPvt と同じ localPvt を探す
        for (int i = 0; i < totalRowCount; i++) {
            PatientVisitModel p = pvtTableModel.getObject(i);
            if (p.getPvtDate().equals(hostPvt.getPvtDate()) && p.getPatientId().equals(hostPvt.getPatientId())) {
                localPvt = p;
                row = i;
                break;
            }
        }

        if (localPvt != null) {
            // localPvt が見つかった場合
            //logger.info("pvt state local = " + localPvt.getState() + ", server = " + hostPvt.getState());

            // キャンセルかどうか
            if (hostPvt.getState() == KarteState.CANCEL_PVT) {
                // キャンセルの場合, 削除して番号を付け直す
                pvtTableModel.deleteRow(row);
                for (int i=row; i<pvtTableModel.getObjectCount(); i++) {
                    pvtTableModel.getObject(i).setNumber(i+1);
                }

            } else {
                // pvt を置き換える
                hostPvt.setNumber(localPvt.getNumber());
                pvtTableModel.getObjectList().set(row, hostPvt);
                // changeRow を fire, ただしカルテが開いていたら fire しない
                if (!ChartImpl.isKarteOpened(localPvt)) {
                    pvtTableModel.fireTableRowsUpdated(row, row);
                }
            }

        } else if (hostPvt.getState() != KarteState.CANCEL_PVT){
            // localPvt がなければ, それは追加である
            row = pvtTableModel.getObjectCount();
            // 番号付加
            hostPvt.setNumber(row + 1);
            pvtTableModel.addRow(hostPvt);
            //logger.info("pvt added at row " + row);
            // 通知を表示
            ScriptExecutor.displayNotification(hostPvt.getPatientAgeBirthday(), "受付 " + (row + 1), hostPvt.getPatientName());
        }
        // pvt count 更新
        updatePvtCount();
    }

    /**
     * 患者来院情報をフルチェックするタスク.
     */
    private class PvtChecker implements Runnable {

        @Override
        public void run() {
            PatientVisitModel pvt;
            PvtDelegater delegater = new PvtDelegater();

            SwingUtilities.invokeLater(() -> setBusy(true));

            // PVTDelegater で使う date を作成する
            // [0] = today, date[1] = AppodateFrom, date[2] = AppodateTo
            // [0] 今日, [1] 2ヶ月前(AppodateFrom), [2]その2ヶ月後(AppodateTo) それは今日
            final LocalDateTime date = LocalDateTime.now();
            String[] dateToSearch = new String[3];
            dateToSearch[0] = dateToSearch[2] = date.format(DateTimeFormatter.ISO_DATE);
            dateToSearch[1] = date.plusMonths(-2).format((DateTimeFormatter.ISO_DATE));

            // フルチェックする
            final List<PatientVisitModel> result = delegater.getPvt(dateToSearch, 0);

            // 結果を全部更新する
            pvtTableModel.clear();
            for (int i = 0; i < result.size(); i++) {
                pvt = result.get(i);
                // 受付番号セット
                pvt.setNumber(i + 1);
                // 受付リスト追加
                pvtTableModel.addRow(pvt);
            }

            // pvtState をアップデート pvtStateList.size は table の ObjectCount と同じはず
            List<PvtStateSpec> pvtStateList = delegater.getPvtState();

            if (pvtStateList != null && (pvtStateList.size() == pvtTableModel.getObjectCount())) {
                logger.info("pvt state update");

                PatientVisitModel myPvt;
                List<Integer> changedRows = new ArrayList<>();

                for (int i = 0; i < pvtStateList.size(); i++) {
                    PvtStateSpec serverPvt = pvtStateList.get(i);
                    myPvt = pvtTableModel.getObject(i);

                    // サーバと違いがあればアップデート
                    // 自分でカルテを開いている場合はアップデートしない
                    if (!ChartImpl.isKarteOpened(myPvt)) {
                        boolean lineChanged = false;
                        if (myPvt.getState() != serverPvt.getState()) {
                            //logger.info("PVT state changed row=" + i + " state=" + serverPvt.getState());
                            myPvt.setState(serverPvt.getState());
                            lineChanged = true;
                        }
                        if (myPvt.getByomeiCount() != serverPvt.getByomeiCount()) {
                            //logger.info("PVT byomeiCount changed row=" + i + " byomeiCount=" + serverPvt.getByomeiCount());
                            myPvt.setByomeiCount(serverPvt.getByomeiCount());
                            lineChanged = true;
                        }
                        if (myPvt.getByomeiCountToday() != serverPvt.getByomeiCountToday()) {
                            //logger.info("PVT byomeiCountToday changed row=" + i + " byomeiCountToday=" + serverPvt.getByomeiCountToday());
                            myPvt.setByomeiCountToday(serverPvt.getByomeiCountToday());
                            lineChanged = true;
                        }
                        if (lineChanged) {
                            changedRows.add(i);
                        }
                    }
                }
            } else {
                logger.info("object count and pvtState count discrepant??");
            }

            pvtTableModel.fireTableDataChanged();

            SwingUtilities.invokeLater(() -> {
                setCheckedTime(date);
                updatePvtCount();
                setBusy(false);
            });

            //logger.info("check time " + (new Date().getTime()-startTime) + " msec");
        }
    }

    /**
     * Retina 対応 Grid を描くレンダラのベース.
     */
    private abstract class TableCellRendererBase extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        protected boolean horizontalGrid = false;
        protected boolean verticalGrid = false;
        protected boolean marking = false;
        protected Color markingColor = Color.WHITE;

        public TableCellRendererBase() {
        }

        /**
         * Show grids and markings.
         *
         * @param graphics Graphics
         */
        @Override
        public void paint(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            super.paint(graphics);
            if (horizontalGrid) {
                g.setColor(Color.WHITE);
                g.drawLine(0, getHeight(), getWidth(), getHeight());
            }
            if (verticalGrid) {
                g.setColor(Color.WHITE);
                g.drawLine(0, 0, 0, getHeight());

            }
            if (marking) {
                g.setColor(markingColor);
                g.fillRect(0, 0, 6, getHeight());
            }
            g.dispose();
        }

        /**
         * Set background color.
         *
         * @param table JTable
         * @param value value
         * @param isSelected isSelected
         * @param isFocused isFocused
         * @param row row
         * @param col col
         */
        public void setBackground(JTable table,
                                  Object value,
                                  boolean isSelected,
                                  boolean isFocused,
                                  int row, int col) {

            Color bg = table.getBackground();

            if (isSexRenderer()) {
                PatientVisitModel pvt = pvtTableModel.getObject(table.convertRowIndexToModel(row));
                if (pvt != null) {
                    String gender = pvt.getPatient().getGender();
                    if (gender.equals(IInfoModel.MALE)) {
                        bg = MALE_COLOR;
                    } else if (gender.equals(IInfoModel.FEMALE)) {
                        bg = FEMALE_COLOR;
                    }
                }
            }
            setBackground(bg);
        }
    }

    /**
     * KarteStateRenderer.
     * カルテ（チャート）の状態をレンダリングするクラス.
     */
    private class KarteStateRenderer extends TableCellRendererBase {
        private static final long serialVersionUID = -7654410476024116413L;

        public KarteStateRenderer() {
            horizontalGrid = true;
            verticalGrid = true;
            initComponent();
        }

        private void initComponent() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean isFocused,
                                                       int row, int col) {

            PatientVisitModel pvt = pvtTableModel.getObject(table.convertRowIndexToModel(row));

            // 背景色の設定
            setBackground(table, value, isSelected, isFocused, row, col);

            // 文字色
            Color fore = pvt != null && pvt.getState() == KarteState.CANCEL_PVT ? CANCEL_PVT_COLOR : table.getForeground();
            this.setForeground(fore);

            // state の value チェック, 本日のカルテがあるかどうか（今日のカルテはないけど, 以前のカルテを編集しただけの場合）
            if (value instanceof Integer) {
                int state = (Integer) value;

                switch (state) {

                    // 診察終了
                    case KarteState.CLOSE_SAVE:
                        this.setIcon(DONE_ICON);
                        break;

                    // カルテ記載がまだ
                    case KarteState.CLOSE_UNFINISHED:
                        this.setIcon(UNFINISHED_ICON);
                        break;

                    // 仮保存
                    case KarteState.CLOSE_TEMP:
                        this.setIcon(TEMPORARY_ICON);
                        break;

                    // READ_ONLY で開いている
                    case KarteState.READ_ONLY:
                        this.setIcon(OPEN_ICON);
                        break;

                    // カルテが開いている
                    case KarteState.OPEN_NONE:
                    case KarteState.OPEN_TEMP:
                        if (ChartImpl.isKarteOpened(pvt)) {
                            this.setIcon(OPEN_ICON);
                        } else {
                            this.setIcon(OPEN_USED_NONE);
                        }
                        break;

                    case KarteState.OPEN_SAVE:
                        if (ChartImpl.isKarteOpened(pvt)) {
                            this.setIcon(OPEN_ICON);
                        } else {
                            this.setIcon(OPEN_USED_SAVE);
                        }
                        break;

                    case KarteState.OPEN_UNFINISHED:
                        if (ChartImpl.isKarteOpened(pvt)) {
                            this.setIcon(OPEN_ICON);
                        } else {
                            this.setIcon(OPEN_USED_UNFINISHED);
                        }
                        break;

                    // その他の場合はアイコン無し CLOSE_NONE はここ
                    default:
                        this.setIcon(null);
                        break;
                }

                // マーキングする
                marking = false;
                if (pvt != null && pvt.getState() != KarteState.CANCEL_PVT) {
                    if (!pvt.hasByomei()) {
                        marking = true;
                        markingColor = DIAGNOSIS_EMPTY_COLOR;
                    } else if (pvt.isShoshin()) {
                        marking = true;
                        markingColor = SHOSHIN_COLOR;
                    }
                }

                this.setText("");

            } else {
                setIcon(null);
                this.setText(value == null ? "" : value.toString());
            }
            return this;
        }
    }

    /**
     * MaleFemaleRenderer.
     * 男女で色を変える renderer.
     */
    private class MaleFemaleRenderer extends TableCellRendererBase {
        private static final long serialVersionUID = 1L;

        public MaleFemaleRenderer() {
            this(JLabel.LEFT);
        }

        public MaleFemaleRenderer(int alignment) {
            horizontalGrid = true;
            initComponent(alignment);
        }

        private void initComponent(int alignment) {
            setHorizontalAlignment(alignment);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean isFocused,
                                                       int row, int col) {

            PatientVisitModel pvt = pvtTableModel.getObject(table.convertRowIndexToModel(row));

            if (isSelected) {
                Color fore;
                Color back;
                if (table.isFocusOwner()) {
                    fore = pvt != null && pvt.getState() == KarteState.CANCEL_PVT ?
                            CANCEL_PVT_COLOR : table.getSelectionForeground();
                    back = table.getSelectionBackground();

                } else {
                    fore = pvt != null && pvt.getState() == KarteState.CANCEL_PVT ?
                            CANCEL_PVT_COLOR : table.getForeground();
                    back = (Color) table.getClientProperty("JTable.backgroundOffFocus");
                }
                setForeground(fore);
                setBackground(back);

            } else {
                setBackground(table, value, isSelected, isFocused, row, col);
                Color fore = pvt != null && pvt.getState() == KarteState.CANCEL_PVT ? CANCEL_PVT_COLOR : table.getForeground();
                this.setForeground(fore);
            }

            if (value instanceof String) {
                switch (col) {
                    case 1: // ID
                    case 3: // 名前
                    case 6: // 生年月日
                        this.setText(IndentTableCellRenderer.addIndent((String) value, IndentTableCellRenderer.WIDE, this.getForeground()));
                        this.setFont(NORMAL_FONT);
                        break;
                    case 5: // 年齢
                        String[] age = ((String) value).split("\\.");
                        if (age[0].equals("0")) {
                            setText(age[1] + " ヶ月");
                        } else {
                            setText(age[0] + " 歳");
                        }
                        break;
                    case 7: // ドクターに変更
                    case 8: // メモ
                        this.setText(IndentTableCellRenderer.addIndent((String) value, IndentTableCellRenderer.WIDE, this.getForeground()));
                        this.setFont(SMALL_FONT);
                        break;
                    default:
                        this.setText((String) value);
                        this.setFont(NORMAL_FONT);
                }
            } else {
                setIcon(null);
                this.setText(value == null ? "" : value.toString());
            }
            return this;
        }
    }

    /**
     * 受付リストのコンテキストメニュークラス.
     */
    private class ContextListener extends AbstractMainComponent.ContextListener<PatientVisitModel> {
        private final JPopupMenu contextMenu;

        public ContextListener() {
            contextMenu = getContextMenu();
        }

        @Override
        public void openKarte(PatientVisitModel pvt) {
            WaitingListImpl.this.openKarte(pvt);
        }

        @Override
        public void maybeShowPopup(MouseEvent e) {

            if (e.isPopupTrigger()) {

                contextMenu.removeAll();
                String pop3 = "偶数奇数レンダラを使用する";
                String pop4 = "性別レンダラを使用する";
                String pop5 = "生年月日の元号表示";

                if (canOpen()) {
                    String pop1 = "カルテを開く";
                    JMenuItem openKarte = new JMenuItem(new ProxyAction(pop1, WaitingListImpl.this::openKarte));
                    openKarte.setIconTextGap(8);
                    contextMenu.add(openKarte);
                    contextMenu.addSeparator();
                }

                JRadioButtonMenuItem oddEven = new JRadioButtonMenuItem(new ProxyAction(pop3, WaitingListImpl.this::switchRenderer));
                JRadioButtonMenuItem sex = new JRadioButtonMenuItem(new ProxyAction(pop4, WaitingListImpl.this::switchRenderer));
                ButtonGroup bg = new ButtonGroup();
                bg.add(oddEven);
                bg.add(sex);
                contextMenu.add(oddEven);
                contextMenu.add(sex);
                if (sexRenderer) {
                    sex.setSelected(true);
                } else {
                    oddEven.setSelected(true);
                }

                JCheckBoxMenuItem item = new JCheckBoxMenuItem(pop5);
                contextMenu.add(item);
                item.setSelected(ageDisplay);
                item.addActionListener(ae -> {
                    ageDisplay = item.isSelected();
                    preferences.putBoolean("ageDisplay", ageDisplay);
                });
                oddEven.setIconTextGap(12);
                sex.setIconTextGap(12);
                item.setIconTextGap(12);

                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /**
     * 配列の中から最大値, 最小値を調べる.
     */
    private class MinMax {
        public int max = -1;
        public int min = -1;

        public MinMax(int[] rows) {
            set(rows);
        }

        public MinMax(List<Integer> list) {
            if (list == null || list.isEmpty()) {
                return;
            }
            int[] rows = new int[list.size()];
            int count = 0;
            for (Integer i : list) {
                rows[count++] = i;
            }
            set(rows);
        }

        private void set(int[] rows) {
            if (rows == null || rows.length == 0) {
                return;
            }

            min = rows[0];
            max = rows[0];
            if (rows.length == 1) {
                return;
            }

            for (int i = 1; i < rows.length; i++) {
                min = Math.min(min, rows[i]);
                max = Math.max(max, rows[i]);
            }
        }
    }
}
