package open.dolphin.client;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.event.BadgeEvent;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.*;
import open.dolphin.impl.care.CareMapDocument;
import open.dolphin.impl.lbtest.LaboTestBean;
import open.dolphin.impl.onshi.Onshi;
import open.dolphin.impl.pinfo.PatientInfoDocument;
import open.dolphin.impl.pvt.PvtListener;
import open.dolphin.infomodel.*;
import open.dolphin.inspector.*;
import open.dolphin.project.Project;
import open.dolphin.ui.*;
import open.dolphin.ui.sheet.JSheet;
import open.dolphin.util.ModelUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * 2号カルテ，傷病名，検査結果履歴等，患者の総合的データを提供するクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class ChartImpl extends AbstractMainTool implements Chart, IInfoModel {
    private static final long serialVersionUID = 1L;
    //  Chart インスタンスを管理するstatic 変数
    private static final List<ChartImpl> allCharts = new ArrayList<>(3);
    // PvtChangeListener
    private static PvtListener pvtListener;
    // getDiagnosisDocument() が loadDocuments() の終了を待つための SecondaryLoop
    private SecondaryLoop secondaryLoop;
    // Logger
    private final Logger logger;
    private final Preferences prefs;
    /// Document Plugin を格納する TabbedPane
    private PNSBadgeTabbedPane tabbedPane;
    // Active になっているDocument Plugin
    private HashMap<String, ChartDocument> providers;
    // 患者インスペクタ
    private PatientInspector inspector;
    // Window Menu をサポートする委譲クラス
    private WindowSupport windowSupport;
    // 検索状況等を表示する共通のパネル
    private StatusPanel statusPanel;
    // 患者来院情報
    private PatientVisitModel pvt;
    // Read Only の時 true
    private boolean readOnly;
    // Chart内のドキュメントに共通の MEDIATOR
    private ChartMediator mediator;
    // State Mgr
    private StateMgr stateMgr;
    // このチャートの KarteBean
    private KarteBean karte;
    // GlassPane
    private BlockGlass blockGlass;
    // 最新の受診歴
    private LastVisit lastVisit;
    // 検索パネル
    private ChartSearchPanel searchPanel;

    /**
     * Create new ChartImpl.
     */
    public ChartImpl() {
        logger = LoggerFactory.getLogger(ChartImpl.class);
        prefs = Project.getPreferences();
    }

    /**
     * オープンしている全インスタンスを保持するリストを返す static method.
     *
     * @return オープンしている ChartPlugin のリスト
     */
    public static List<ChartImpl> getAllChart() {
        return Collections.unmodifiableList(allCharts);
    }

    /**
     * PvtListener を登録するための static method.
     *
     * @param listener PvtListener
     */
    public static void addPvtListener(PvtListener listener) {
        pvtListener = listener;
    }

    /**
     * PvtListener に PatientVisitModel を通知する static method.
     *
     * @param model PatientVisitModel
     */
    private static void firePvtChange(PatientVisitModel model) {
        pvtListener.pvtChanged(model);
    }

    /**
     * チャートウインドウのオープンを通知する static method.
     * CLOSE -> OPEN に変換する操作，ReadOnly の場合は WaitingList に通知しない
     * PatientSearchImpl で開いた場合など，pvt.id=0 の場合は WaitingList に通知しない
     *
     * @param opened オープンした Chart(=this)
     */
    public static void windowOpened(ChartImpl opened) {

        // インスタンスを保持するリストへ追加する
        allCharts.add(0, opened);

        PatientVisitModel model = opened.getPatientVisit();
        int oldState = model.getState();
        // pvt status を変更する（close -> open)
        int newState = KarteState.toOpenState(oldState);
        model.setState(newState);

        if (!opened.isReadOnly() && model.getId() != 0) {
            firePvtChange(model);
        }

        // このあと，pvt.state は karteBean 読み込みがあればサーバデータでリストアされる
    }

    /**
     * チャートウインドウのクローズを通知する static method.
     * OPEN -> CLOSE に変更する操作   ReadOnly のときも WaitingList に通知する
     * PatientSearchImpl で開いた場合など，pvt.id=0 の場合は WaitingList に通知しない
     *
     * @param closed クローズした Chart(=this)
     */
    public static void windowClosed(ChartImpl closed) {

        PatientVisitModel model = closed.getPatientVisit();

        if (closed.isReadOnly()) {
            model.setState(KarteState.READ_ONLY);
        } else {
            int oldState = model.getState();
            boolean isEmpty = new DocumentPeeker(model).isKarteEmpty();
            // pvt status を変更する（open -> close)
            int newState = KarteState.toClosedState(oldState, isEmpty);
            model.setState(newState);
        }
        // WaitingListImpl に通知する
        // 書き込み時の ReadOnly 対応は WaitingList 側で施行
        if (model.getId() != 0) {
            firePvtChange(model);
        }

        // 最後にインスタンスリストから取り除く
        boolean succeeded = allCharts.remove(closed);
        if (!succeeded) {
            // カルテが登録されていなかったと言うことで，ありえない
            throw new RuntimeException("Chart is lost!");
        }
    }

    /**
     * PatientVisitModel のカルテを前に出す static method.
     *
     * @param pvt PatientVisitModel
     */
    public static void toFront(PatientVisitModel pvt) {
        if (pvt == null) {
            return;
        }
        toFront(pvt.getPatient());
    }

    /**
     * PatientModel のカルテを前に出す static method.
     *
     * @param patient PatientModel
     */
    public static void toFront(PatientModel patient) {
        if (patient == null) {
            return;
        }
        long ptId = patient.getId();
        allCharts.stream().filter(chart -> chart.getPatient().getId() == ptId)
                .findAny().ifPresent(chart -> chart.getFrame().toFront());

    }

    /**
     * PatientVisitModel のカルテが開いているかどうか調べる static method.
     *
     * @param pvt PatientVisitModel
     * @return karte opened or not
     */
    public static boolean isKarteOpened(PatientVisitModel pvt) {
        if (pvt == null) {
            return false;
        }
        return isKarteOpened(pvt.getPatient());
    }

    /**
     * PatientModel のカルテが開いているかどうか調べる static method.
     *
     * @param patient PatientModel
     * @return karte opened or not
     */
    public static boolean isKarteOpened(PatientModel patient) {
        if (patient == null) {
            return false;
        }
        long ptId = patient.getId();

        return allCharts.stream().anyMatch(chart -> chart.getPatient().getId() == ptId);
    }

    /**
     * このチャートのカルテを返す.
     *
     * @return カルテ
     */
    @Override
    public KarteBean getKarte() {
        return karte;
    }

    /**
     * このチャートのカルテを設定する.
     *
     * @param karte このチャートのカルテ
     */
    @Override
    public void setKarte(KarteBean karte) {
        this.karte = karte;
    }

    /**
     * Chart の JFrame を返す.
     *
     * @return チャートウインドウno JFrame
     */
    @Override
    public MainFrame getFrame() {
        return windowSupport.getFrame();
    }

    /**
     * Chart内ドキュメントが共通に使用する Status パネルを返す.
     *
     * @return IStatusPanel
     */
    @Override
    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    /**
     * Chart内ドキュメントが共通に使用する Status パネルを設定する.
     *
     * @param statusPanel IStatusPanel
     */
    @Override
    public void setStatusPanel(StatusPanel statusPanel) {
        this.statusPanel = statusPanel;
    }

    /**
     * 来院情報を返す.
     *
     * @return 来院情報
     */
    @Override
    public PatientVisitModel getPatientVisit() {
        return pvt;
    }

    /**
     * 来院情報を設定する.
     *
     * @param pvt 来院情報
     */
    @Override
    public void setPatientVisit(PatientVisitModel pvt) {
        this.pvt = pvt;
    }

    /**
     * ReadOnly かどうかを返す.
     *
     * @return ReadOnlyの時 true
     */
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * ReadOnly 属性を設定する.
     *
     * @param readOnly ReadOnly user の時 true
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * このチャートが対象としている患者モデルを返す.
     *
     * @return チャートが対象としている患者モデル
     */
    @Override
    public PatientModel getPatient() {
        return getKarte().getPatient();
    }

    /**
     * このチャートが対象としている患者モデルを設定する.
     *
     * @param patientModel チャートが対象とする患者モデル
     */
    public void setPatientModel(PatientModel patientModel) {
        this.getKarte().setPatient(patientModel);
    }

    /**
     * チャートのステート属性を返す.
     *
     * @return チャートのステート属性
     */
    @Override
    public int getChartState() {
        return getPatientVisit().getState();
    }

    /**
     * チャートのステートを設定する.
     *
     * @param chartState チャートステート
     */
    @Override
    public void setChartState(int chartState) {
        getPatientVisit().setState(chartState);
    }

    /**
     * チャート内で共通に使用する Mediator を返す.
     *
     * @return ChartMediator
     */
    @Override
    public ChartMediator getChartMediator() {
        return mediator;
    }

    /**
     * チャート内で共通に使用する Mediator を設定する.
     *
     * @param mediator ChartMediator
     */
    public void setChartMediator(ChartMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Menu アクションを制御する.
     *
     * @param name    Menu name
     * @param enabled enable or not
     */
    @Override
    public void enabledAction(String name, boolean enabled) {
        Action action = mediator.getAction(name);
        if (action != null) {
            action.setEnabled(enabled);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * 文書ヒストリオブジェクトを返す.
     *
     * @return 文書ヒストリオブジェクト DocumentHistory
     */
    @Override
    public DocumentHistory getDocumentHistory() {
        return inspector.getDocumentHistory();
    }

    /**
     * 病名インスペクタを返す.
     *
     * @return DiagnosisInspector
     */
    public DiagnosisInspector getDiagnosisInspector() {
        return inspector.getDiagnosisInspector();
    }

    /**
     * 最終受診日を返す.
     *
     * @return LastVisit
     */
    public LastVisit getLastVisit() {
        return lastVisit;
    }

    /**
     * 引数で指定されたタブ番号のドキュメントを表示する.
     *
     * @param index 表示するドキュメントのタブ番号
     */
    @Override
    public void showDocument(int index) {
        int cnt = tabbedPane.getTabCount();
        if (index >= 0 && index <= cnt - 1 && index != tabbedPane.getSelectedIndex()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    /**
     * チャート内に未保存ドキュメントがあるかどうかを返す.
     *
     * @return 未保存ドキュメントがある時 true
     */
    @Override
    public boolean isDirty() {

        boolean dirty = false;

        if (providers != null && providers.size() > 0) {
            Collection<ChartDocument> docs = providers.values();
            for (ChartDocument doc : docs) {
                if (doc.isDirty()) {
                    dirty = true;
                    break;
                }
            }
        }
        return dirty;
    }

    /**
     * ChartImpl を開始する.
     */
    @Override
    public void start() {

        int maxEstimation = 30000;
        //int delay = ClientContext.getInt("chart.timerDelay"); // 200
        String message = "カルテオープン";
        String patientName = getPatientVisit().getPatient().getFullName() + "様";
        String note = patientName + "を開いています...";

        Task<KarteBean> task = new Task<KarteBean>(null, message, note, maxEstimation) {

            @Override
            protected KarteBean doInBackground() {
                logger.debug("CahrtImpl start task doInBackground");
                //
                // Database から患者のカルテを取得する
                //
                int past = Project.getPreferences().getInt(Project.DOC_HISTORY_PERIOD, -12);
                GregorianCalendar today = new GregorianCalendar();
                today.add(GregorianCalendar.MONTH, past);
                today.clear(Calendar.HOUR_OF_DAY);
                today.clear(Calendar.MINUTE);
                today.clear(Calendar.SECOND);
                today.clear(Calendar.MILLISECOND);
                DocumentDelegater ddl = new DocumentDelegater();
                return ddl.getKarte(getPatientVisit().getPatient().getId(), today.getTime());
            }

            @Override
            protected void succeeded(KarteBean karteBean) {
                logger.debug("CahrtImpl start task succeeded");

                karteBean.setPatient(getPatientVisit().getPatient());
                setKarte(karteBean);
                initComponents();
                logger.debug("initComponents end");
                SwingUtilities.invokeLater(getDocumentHistory()::showHistory);
            }

            @Override
            protected void failed(Throwable t) {
                t.printStackTrace(System.err);
            }
        };
        //task.setMillisToPopup(delay);
        task.execute();
    }

    /**
     * 患者のカルテを検索取得し，GUI を構築する.
     * このメソッドは start() 内のバックグランドスレッドで実行される.
     */
    public void initComponents() {

        // このチャート の Frame を生成し初期化する.
        // Frame のタイトルを
        // 患者氏名(カナ):患者ID に設定する
        //

        // Frame と MenuBar を生成する
        String title = String.format("%s - %s（%s）: %s",
                ClientContext.getString("chart.chartStr"),
                getPatient().getFullName(),
                getPatient().getKanaName().replace("　", " "),
                getPatient().getPatientId());

        windowSupport = WindowSupport.create(title);

        // チャート用のメニューバーを得る
        JMenuBar myMenuBar = windowSupport.getMenuBar();

        // チャートの JFrame オブジェクトを得る
        final MainFrame frame = windowSupport.getFrame();
        frame.setName("chartFrame");
        frame.getRootPane().putClientProperty(WindowSupport.MENUBAR_HEIGHT_OFFSET_PROP, 30);

        // FocusTraversalPolicy
        frame.setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getDefaultComponent(Container aContainer) {
                // 最初にフォーカスを取る component
                return getDocumentHistory().getDocumentHistoryTable();
            }

            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                if (InspectorCategory.メモ.name().equals(aComponent.getName())) {
                    JList<RegisteredDiagnosisModel> list = inspector.getDiagnosisInspector().getList();
                    if (list.getModel().getSize() == 0) {
                        // リストが空なら document history へ
                        Focuser.requestFocus(inspector.getDocumentHistory().getDocumentHistoryTable());
                    } else {
                        // 選択がない場合は選択してフォーカス
                        if (list.getSelectedIndex() < 0) { list.setSelectedIndex(0); }
                        Focuser.requestFocus(list);
                    }

                } else if (InspectorCategory.文書履歴.name().equals(aComponent.getName())) {
                    JTextArea memoArea = inspector.getMemoInspector().getMemoArea();
                    memoArea.selectAll();
                    Focuser.requestFocus(memoArea);

                } else {
                    Focuser.requestFocus(inspector.getDocumentHistory().getDocumentHistoryTable());
                }
                return null;
            }

            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                if (InspectorCategory.病名.name().equals(aComponent.getName())) {
                    JTextArea memoArea = inspector.getMemoInspector().getMemoArea();
                    memoArea.selectAll();
                    Focuser.requestFocus(memoArea);

                } else if (InspectorCategory.文書履歴.name().equals(aComponent.getName())) {
                    JList<RegisteredDiagnosisModel> list = inspector.getDiagnosisInspector().getList();
                    if (list.getModel().getSize() == 0) {
                        // リストが空なら document history へ
                        Focuser.requestFocus(inspector.getDocumentHistory().getDocumentHistoryTable());
                    } else {
                        // 選択がない場合は選択してフォーカス
                        if (list.getSelectedIndex() < 0) { list.setSelectedIndex(0); }
                        Focuser.requestFocus(list);
                    }

                } else {
                    Focuser.requestFocus(inspector.getDocumentHistory().getDocumentHistoryTable());
                }
                return inspector.getMemoInspector().getPanel();
            }

            @Override
            public Component getFirstComponent(Container aContainer) {
                return null;
            }

            @Override
            public Component getLastComponent(Container aContainer) {
                return null;
            }
        });

        // 患者インスペクタを生成する
        inspector = new PatientInspector(this);
        inspector.getPanel().setBorder(BorderFactory.createEmptyBorder()); // カット&トライ

        // Status パネルを生成する
        statusPanel = frame.getStatusPanel();
        // lastVisit を設定する
        lastVisit = new LastVisit(this);
        // Status パネルに表示する情報を生成する
        // カルテ登録日 Status パネルの右側に配置する
        String rdFormat = ClientContext.getString("common.dateFormat");         // yyyy-MM-dd
        String rdPrifix = ClientContext.getString("common.registeredDatePrefix");     // カルテ登録日:
        String patienIdPrefix = ClientContext.getString("common.registeredDatePatientIdPrefix"); // 患者ID:
        Date date = getKarte().getCreated();
        SimpleDateFormat sdf = new SimpleDateFormat(rdFormat);
        String created = sdf.format(date);

        // status panel 設定
        // 患者ID Status パネルの左に配置する
        //statusPanel.setLeftInfo(patienIdPrefix + " " + getKarte().getPatient().getPatientId()); // 患者ID:xxxxxx
        statusPanel.add("", "message"); // key "message" の JLabel : AbstractChartDocument#enter と LaboTestBean で使う
        statusPanel.addGlue();
        statusPanel.addProgressBar();
        statusPanel.addSeparator();
        statusPanel.add(patienIdPrefix + " " + getKarte().getPatient().getPatientId());
        statusPanel.addSeparator();
        statusPanel.add(rdPrifix + " " + created);

        Date pvtDate = ModelUtils.getDateTimeAsObject(pvt.getPvtDate());
        if (pvtDate != null && pvt.getState() == KarteState.CLOSE_NONE) { // window open 前に呼ばれる
            String waitingTime = DurationFormatUtils.formatPeriod(pvtDate.getTime(), new Date().getTime(), "HH:mm");
            statusPanel.addSeparator();
            statusPanel.add("待ち時間 " + waitingTime);
        }
        statusPanel.setMargin(4);

        // ChartMediator を生成する
        mediator = new ChartMediator(this);

        // Menu を生成する
        MenuFactory appMenu = new MenuFactory();
        appMenu.setMenuSupports(getContext().getMenuSupport(), mediator);
        appMenu.build(myMenuBar);
        mediator.registerActions(appMenu.getActionMap());

        // 検索フィールド
        searchPanel = new ChartSearchPanel(this);
        searchPanel.show(ChartSearchPanel.Card.KARTE);

        JPanel keywordPanel = new JPanel();
        keywordPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        keywordPanel.add(searchPanel);

        // Document プラグインのタブを生成する
        tabbedPane = loadDocuments();

        tabbedPane.getAccessoryPanel().add(keywordPanel, BorderLayout.EAST);
        JPanel agePanel = inspector.getBasicInfoInspector().getAgePanel();
        tabbedPane.getAccessoryPanel().add(agePanel, BorderLayout.WEST);

        // 全体をレイアウトする
        JPanel inspectorPanel = new JPanel() {
            // 右側の境界線を描く
            private static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.setColor(IInspector.BORDER_COLOR);
                g.drawLine(getWidth() - 1, 31, getWidth() - 1, getHeight() - 1);
            }
        };
        inspectorPanel.setOpaque(true);
        inspectorPanel.setBackground(IInspector.BACKGROUND);
        inspectorPanel.setLayout(new BoxLayout(inspectorPanel, BoxLayout.Y_AXIS));
        inspectorPanel.setPreferredSize(new Dimension(IInspector.DEFAULT_WIDTH + 20, 10));

        inspectorPanel.add(inspector.getBasicInfoInspector().getPanel());
        inspectorPanel.add(inspector.getPanel());

        final MainFrame.MainPanel mainPanel = frame.getMainPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(inspectorPanel, BorderLayout.WEST);

        // StateMgr を生成する
        stateMgr = new StateMgr();

        // BlockGlass を設定する
        blockGlass = new BlockGlass();
        frame.setGlassPane(blockGlass);

        // このチャートの Window にリスナを設定する
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                // CloseBox の処理を行う
                processWindowClosing();
            }

            @Override
            public void windowOpened(WindowEvent e) {
                // Window がオープンされた時の処理を行う
                ChartImpl.windowOpened(ChartImpl.this);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // Window がクローズされた時の処理を行う
                // stop で setVisible(false) を出した後に呼ばれる
                ChartImpl.windowClosed(ChartImpl.this);
            }

            @Override
            public void windowActivated(WindowEvent e) {
                // allCharts の順番処理 → activate されたらトップに置く
                if (allCharts.remove(ChartImpl.this)) {
                    allCharts.add(0, ChartImpl.this);
                }
            }
        });

        // Frame の大きさをストレージからロードする
        frame.pack(); // pack しないと，TextField に文字を入力してリターンを押した後にレイアウトがずれる
        Point defaultLocation = new Point(5, 20);
        Dimension defaultSize = new Dimension(900, 740);
        ComponentBoundsManager manager = new ComponentBoundsManager(frame, defaultLocation, defaultSize, this);

        // フレームの表示位置を決める J2SE 5.0
        boolean locByPlatform = Project.getPreferences().getBoolean(Project.LOCATION_BY_PLATFORM, false);

        if (locByPlatform) {
            frame.setLocationByPlatform(true);
            manager.putCenter();

        } else {
            frame.setLocationByPlatform(false);
            manager.revertToPreferenceBounds();
        }

        frame.setVisible(true);

        // command ctrl F で検索フィールドにフォーカスする裏コマンド
        if (ClientContext.isMac()) {
            InputMap im = windowSupport.getFrame().getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = windowSupport.getFrame().getRootPane().getActionMap();
            im.put(KeyStroke.getKeyStroke("meta ctrl F"), GUIConst.ACTION_SEARCH_STAMP);
            am.put(GUIConst.ACTION_SEARCH_STAMP, new ProxyAction(mediator::searchStamp));
        }
    }

    /**
     * ChartSearchPanel を返す.
     *
     * @return ChartSearchPanel
     */
    public ChartSearchPanel getChartSearchPanel() {
        return searchPanel;
    }

    /**
     * メニューを制御する.
     */
    public void controlMenu() {
        stateMgr.controlMenu();
    }

    /**
     * ドキュメントタブを生成する.
     * 遅延実行される initComponents() から呼ばれるので遅延実行される.
     */
    private PNSBadgeTabbedPane loadDocuments() {

        providers = new HashMap<>();

        PNSBadgeTabbedPane tab = new PNSBadgeTabbedPane();
        tab.setButtonPanelPadding(new Dimension(0, 4));

        ChartDocument[] plugin = new ChartDocument[6];
        plugin[0] = new DocumentBridgeImpl();
        plugin[1] = new DiagnosisDocument();
        plugin[2] = new LaboTestBean();
        plugin[3] = new CareMapDocument();
        plugin[4] = new PatientInfoDocument();
        plugin[5] = new Onshi();

        for (int index = 0; index < plugin.length; index++) {
            // start が必要な tab
            if (index == 0 || index == 5) {
                plugin[index].setContext(this);
                plugin[index].start();
            }

            tab.addTab(plugin[index].getTitle(), plugin[index].getUI());
            // tab のタイトルをキーにする
            providers.put(plugin[index].getTitle(), plugin[index]);
        }

        // ゼロ番目を選択しておき changeListener を機能させる
        tab.setSelectedIndex(0);

        // tab に プラグインを遅延生成するためのの ChangeListener を追加する
        tab.addChangeListener(this::tabChanged);

        // getDiagnosisDocument() に loadDocuments が終わったことを通知する
        if (Objects.nonNull(secondaryLoop)) {
            logger.debug("exit secondary loop");
            secondaryLoop.exit();
            secondaryLoop = null;
        }

        return tab;
    }

    /**
     * Tab に Badge を付ける.
     * @param e
     */
    public void setBadge(BadgeEvent e) {
        tabbedPane.setBadge(e);
    }

    /**
     * ChartImpl から DiagnosisDocument を取る.
     * DiangosisInspector, DiagnosisInspectorTransferHandler, UserStampBox から呼ばれる.
     * DiagnosisInspector は loadDocuments() が完了する前に呼びに来る.
     *
     * @return DiagnosisDocument
     */
    public DiagnosisDocument getDiagnosisDocument() {
        // DiagnosisDocument が load されるまで SecondaryLoop で堰き止める
        if (Objects.isNull(providers)) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            EventQueue eq = tk.getSystemEventQueue();
            secondaryLoop = eq.createSecondaryLoop();
            logger.debug("enter secondary loop");
            if (!secondaryLoop.enter()) {
                secondaryLoop = null;
                throw new RuntimeException("Could not enter secondary loop.");
            }
        }

        DiagnosisDocument doc = (DiagnosisDocument) providers.get("傷病名");
        if (doc.getContext() == null) {
            doc.setContext(ChartImpl.this);
            doc.start();
        }
        return doc;
    }

    /**
     * ドキュメントタブにプラグインを遅延生成し追加する.
     *
     * @param e ChangeEvent
     */
    public void tabChanged(ChangeEvent e) {

        // 選択されたタブ番号に対応するプラグインをテーブルから検索する
        int index = tabbedPane.getSelectedIndex();
        String key = tabbedPane.getTitleAt(index);
        ChartDocument plugin = providers.get(key);

        if (plugin.getContext() == null) {
            // まだ生成されていないプラグインを生成する
            plugin.setContext(ChartImpl.this);
            plugin.start();
            tabbedPane.setComponentAt(index, plugin.getUI());
        }
        plugin.enter();
    }

    /**
     * 新規カルテを作成する.
     */
    public void newKarte() {

        logger.debug("newKarte() in ChartImpl starts");

        // ReadOnly なら開かない
        if (isReadOnly()) {
            return;
        }

        // 新規カルテ作成はひとつだけ　masuda
        if (toFrontNewKarteIfPresent()) {
            return;
        }

        String dept = getPatientVisit().getDeptNoTokenize();
        String deptCode = getPatientVisit().getDepartmentCode();
        String insuranceUid = getPatientVisit().getInsuranceUid();

        // 新規ドキュメントのタイプ=2号カルテと可能なオプションを設定する
        String docType = IInfoModel.DOCTYPE_KARTE;
        Chart.NewKarteOption option;
        KarteViewer2 base;

        ChartDocument bridgeOrViewer = providers.get("参 照");

        if (bridgeOrViewer instanceof DocumentBridgeImpl) {
            // Chart画面のタブパネル
            logger.debug("bridgeOrViewer instanceof DocumentBridgeImpl");
            DocumentBridgeImpl bridge = (DocumentBridgeImpl) bridgeOrViewer;
            base = bridge.getBaseKarte();

        } else if (bridgeOrViewer instanceof KarteDocumentViewer) {
            logger.debug("bridgeOrViewer instanceof KarteDocumentViewer");
            KarteDocumentViewer viwer = (KarteDocumentViewer) bridgeOrViewer;
            base = viwer.getBaseKarte();
        } else {
            return;
        }

        if (base != null) {
            logger.debug("base != null");
            if (base.getDocType().equals(IInfoModel.DOCTYPE_KARTE)) {
                logger.debug("base.getDocType().equals(IInfoModel.DOCTYPE_KARTE");
                option = Chart.NewKarteOption.BROWSER_COPY_NEW;
            } else {
                // ベースがあても２号カルテでない場合
                logger.debug("base.getDocType().equals(IInfoModel.DOCTYPE_S_KARTE");
                option = Chart.NewKarteOption.BROWSER_NEW;
            }

        } else {
            // ベースのカルテがない場合
            logger.debug("base == null");
            option = Chart.NewKarteOption.BROWSER_NEW;
        }

        // 新規カルテ作成時に確認ダイアログを表示するかどうか
        NewKarteParams params;

        if (prefs.getBoolean(Project.KARTE_SHOW_CONFIRM_AT_NEW, true)) {

            // 新規カルテダイアログへパラメータを渡し，コピー新規のオプションを制御する
            logger.debug("show newKarteDialog");
            params = getNewKarteParams(docType, option, null, dept, deptCode, insuranceUid);

        } else {
            // 保険，作成モード，配置方法を手動で設定する
            params = new NewKarteParams(option);
            params.setDocType(docType);
            params.setDepartment(dept);
            params.setDepartmentCode(deptCode);

            // 保険
            PVTHealthInsuranceModel[] ins = getHealthInsurances();
            params.setPVTHealthInsurance(ins[0]);
            if (insuranceUid != null) {
                for (PVTHealthInsuranceModel model : ins) {
                    if (insuranceUid.equals(model.getGUID())) {
                        params.setPVTHealthInsurance(model);
                        break;
                    }
                }
            }

            // 作成モード
            switch (option) {

                case BROWSER_NEW:
                    params.setCreateMode(Chart.NewKarteMode.EMPTY_NEW);
                    break;

                case BROWSER_COPY_NEW:
                    int cMode = prefs.getInt(Project.KARTE_CREATE_MODE, 0);
                    switch (cMode) {
                        case 0:
                            params.setCreateMode(Chart.NewKarteMode.EMPTY_NEW);
                            break;
                        case 1:
                            params.setCreateMode(Chart.NewKarteMode.APPLY_RP);
                            break;
                        case 2:
                            params.setCreateMode(Chart.NewKarteMode.ALL_COPY);
                            break;
                        default:
                            break;
                    }
                    break;
            }

            // 配置方法
            params.setOpenFrame(prefs.getBoolean(Project.KARTE_PLACE_MODE, true));

        }

        // キャンセルした場合はリターンする
        if (params == null) {
            return;
        }

        logger.debug("returned newKarteDialog");
        DocumentModel editModel;
        KarteEditor editor;

        // Baseになるカルテがあるかどうかでモデルの生成が異なる
        if (params.getCreateMode() == Chart.NewKarteMode.EMPTY_NEW) {
            logger.debug("empty new is selected");
            editModel = getKarteModelToEdit(params);
        } else {
            logger.debug("copy new is selected");
            editModel = getKarteModelToEdit(base.getDocument(), params);
        }

        //
        // 新規カルテにいろいろモジュールを入れる
        //
        StampModulePicker picker = new StampModulePicker(this);
        // 初診・再診 stampTreeModule を自動入力する
        ModuleModel mm = picker.getBaseCharge();
        if (mm != null) {
            editModel.addModule(mm);
        }
        // 初期テキストスタンプ挿入
        mm = picker.getInitialStamp();
        if (mm != null) {
            editModel.addModule(mm);
        }

        editor = createEditor();
        editor.setDocument(editModel);
        editor.setEditable(true);

        if (params.isOpenFrame()) {
            final EditorFrame editorFrame = new EditorFrame();
            editorFrame.setChart(this);
            editorFrame.setKarteEditor(editor);
            editorFrame.start();

        } else {
            editor.setContext(this);
            editor.initialize();
            editor.start();
            this.addChartDocument(editor, params);
        }
    }

    /**
     * EmptyNew 新規カルテのモデルを生成する.
     *
     * @param params 作成パラメータセット
     * @return 新規カルテのモデル
     */
    public DocumentModel getKarteModelToEdit(NewKarteParams params) {

        // カルテモデルを生成する
        DocumentModel model = new DocumentModel();

        // DocInfoを設定する
        DocInfoModel docInfo = model.getDocInfo();

        // docId 文書ID
        docInfo.setDocId(GUIDGenerator.generate(docInfo));

        // 生成目的
        docInfo.setPurpose(PURPOSE_RECORD);

        // DocumentType
        docInfo.setDocType(params.getDocType());
        docInfo.setDepartmentDesc(getPatientVisit().getDeptNoTokenize());
        docInfo.setDepartment(getPatientVisit().getDepartmentCode());

        // 健康保険を設定する
        PVTHealthInsuranceModel insurance = params.getPVTHealthInsurance();
        docInfo.setHealthInsurance(insurance.getInsuranceClassCode());
        docInfo.setHealthInsuranceDesc(insurance.toString());
        docInfo.setHealthInsuranceGUID(insurance.getGUID());

        // Versionを設定する
        VersionModel version = new VersionModel();
        version.initialize();
        docInfo.setVersionNumber(version.getVersionNumber());

        // Document の Status を設定する
        // 新規カルテの場合は none
        docInfo.setStatus(STATUS_NONE);

        return model;
    }

    /**
     * コピーして新規カルテを生成する場合のカルテモデルを生成する.
     *
     * @param oldModel コピー元のカルテモデル
     * @param params   生成パラメータセット
     * @return 新規カルテのモデル
     */
    public DocumentModel getKarteModelToEdit(DocumentModel oldModel, NewKarteParams params) {

        // 新規モデルを作成し，表示されているモデルの内容をコピーする
        DocumentModel newModel = new DocumentModel();
        boolean applyRp = params.getCreateMode() == Chart.NewKarteMode.APPLY_RP;
        copyModel(oldModel, newModel, applyRp);

        // 新規カルテの DocInfo を設定する
        DocInfoModel docInfo = newModel.getDocInfo();

        // 文書ID
        docInfo.setDocId(GUIDGenerator.generate(docInfo));

        // 生成目的
        docInfo.setPurpose(PURPOSE_RECORD);

        // DocumentType
        docInfo.setDocType(params.getDocType());

        // 診療科を設定する 受付情報から設定する
        String dept = params.getDepartment();
        docInfo.setDepartmentDesc(dept);
        docInfo.setDepartmentDesc(getPatientVisit().getDeptNoTokenize());
        docInfo.setDepartment(getPatientVisit().getDepartmentCode());

        // 健康保険を設定する
        PVTHealthInsuranceModel insurance = params.getPVTHealthInsurance();
        docInfo.setHealthInsurance(insurance.getInsuranceClassCode());
        //docInfo.setHealthInsuranceDesc(insurance.getInsuranceClass());
        docInfo.setHealthInsuranceDesc(insurance.toString());
        docInfo.setHealthInsuranceGUID(insurance.getGUID());

        // Versionを設定する
        VersionModel version = new VersionModel();
        version.initialize();
        docInfo.setVersionNumber(version.getVersionNumber());

        // Document の Status を設定する
        // 新規カルテの場合は none
        docInfo.setStatus(STATUS_NONE);

        return newModel;
    }

    /**
     * 修正の場合のカルテモデルを生成する.
     *
     * @param oldModel 修正対象のカルテモデル
     * @return 新しい版のカルテモデル
     */
    public DocumentModel getKarteModelToEdit(DocumentModel oldModel) {

        // 修正対象の DocInfo を取得する
        DocInfoModel oldDocInfo = oldModel.getDocInfo();

        // 新しい版のモデルにモジュールと画像をコピーする
        DocumentModel newModel = new DocumentModel();
        copyModel(oldModel, newModel, false);

        // 新しい版の DocInfo を設定する
        DocInfoModel newInfo = newModel.getDocInfo();

        // 文書ID
        newInfo.setDocId(GUIDGenerator.generate(newInfo));

        // 新しい版の firstConfirmDate = 元になる版の firstConfirmDate
        newInfo.setFirstConfirmDate(oldDocInfo.getFirstConfirmDate());

        // docType = old one
        newInfo.setDocType(oldDocInfo.getDocType());

        // purpose = old one
        newInfo.setPurpose(oldDocInfo.getPurpose());

        // タイトルも引き継ぐ
        newInfo.setTitle(oldDocInfo.getTitle());

        // 診療科を設定する
        // 元になる版の情報を利用する
        newInfo.setDepartmentDesc(oldDocInfo.getDepartmentDesc());
        newInfo.setDepartment(oldDocInfo.getDepartment());

        // 健康保険を設定する
        // 元になる版の情報を利用する
        newInfo.setHealthInsuranceDesc(oldDocInfo.getHealthInsuranceDesc());
        newInfo.setHealthInsurance(oldDocInfo.getHealthInsurance());
        newInfo.setHealthInsuranceGUID(oldDocInfo.getHealthInsuranceGUID());

        // 親文書IDを設定する
        newInfo.setParentId(oldDocInfo.getDocId());
        newInfo.setParentIdRelation(PARENT_OLD_EDITION);

        // old PK を設定する
        newInfo.setParentPk(oldModel.getId());

        // Versionを設定する
        // new = old + 1.0
        VersionModel newVersion = new VersionModel();
        newVersion.setVersionNumber(oldDocInfo.getVersionNumber());
        newVersion.incrementNumber(); // version number ++
        newInfo.setVersionNumber(newVersion.getVersionNumber());

        // Document Status を設定する
        // 元になる版の status (Final | Temporal | Modified)
        newInfo.setStatus(oldDocInfo.getStatus());

        return newModel;
    }

    /**
     * カルテエディタを生成する.
     *
     * @return カルテエディタ
     */
    public KarteEditor createEditor() {
        return new KarteEditor();
    }

    /**
     * モデルをコピーする. 参照ではいけない. DocInfo の設定はない.
     */
    private void copyModel(DocumentModel oldModel, DocumentModel newModel, boolean applyRp) {

        // 前回処方を適用する場合
        if (applyRp) {
            Collection<ModuleModel> modules = oldModel.getModules();
            if (modules != null) {
                Collection<ModuleModel> apply = new ArrayList<>(5);

                modules.forEach(bean -> {
                    IInfoModel model = bean.getModel();
                    if (model instanceof ClaimBundle) {
                        // 処方かどうかを判定する
                        if (((ClaimBundle) model).getClassCode().startsWith("2")) {
                            apply.add(bean);
                        }
                        // ついでに，処置もコピー
                        if (((ClaimBundle) model).getClassCode().startsWith("4")) {
                            apply.add(bean);
                        }
                    }
                });

                if (!apply.isEmpty()) {
                    newModel.setModules(apply);
                }
            }

        } else {
            // 全てコピー
            newModel.setModules(oldModel.getModules());
            newModel.setSchema(oldModel.getSchema());
        }
    }

    /**
     * カルテ作成時にダアイログをオープンし，保険を選択させる.
     *
     * @param docType      生成するドキュメントの種類. ２号カルテで固定.
     * @param option       NewKarteOption
     * @param f            Frame
     * @param dept         Department
     * @param insuranceUid Insurance UID
     * @param deptCode     Department ocd
     * @return NewKarteParams
     */
    public NewKarteParams getNewKarteParams(String docType, Chart.NewKarteOption option, MainFrame f, String dept, String deptCode, String insuranceUid) {

        NewKarteParams params = new NewKarteParams(option);
        params.setDocType(docType);
        params.setDepartment(dept);
        params.setDepartmentCode(deptCode);

        // 患者の健康保険コレクション
        Collection<PVTHealthInsuranceModel> insurances = pvt.getPatient().getPvtHealthInsurances();

        // コレクションが null の場合は自費保険を追加する
        if (insurances == null || insurances.isEmpty()) {
            insurances = new ArrayList<>(1);
            PVTHealthInsuranceModel model = new PVTHealthInsuranceModel();
            model.setInsuranceClass(INSURANCE_SELF);
            model.setInsuranceClassCode(INSURANCE_SELF_CODE);
            model.setInsuranceClassCodeSys(INSURANCE_SYS);
            insurances.add(model);
        }

        // 保険コレクションを配列に変換し，パラメータにセットする
        // ユーザがこの中の保険を選択する
        PVTHealthInsuranceModel[] insModels = insurances.toArray(new PVTHealthInsuranceModel[0]);
        params.setInsurances(insModels);
        int index = 0;
        if (insuranceUid != null) {
            for (int i = 0; i < insModels.length; i++) {
                if (insModels[i].getGUID() != null) {
                    if (insModels[i].getGUID().equals(insuranceUid)) {
                        index = i;
                        break;
                    }
                }
            }
        }
        params.setInitialSelectedInsurance(index);

        String text = option == Chart.NewKarteOption.BROWSER_MODIFY
                ? ClientContext.getString("chart.modifyKarteTitle")
                : ClientContext.getString("chart.newKarteTitle");

        text = ClientContext.getFrameTitle(text);

        // モーダルダイアログを表示する
        MainFrame frame = f != null ? f : getFrame();
        NewKarteDialog od = new NewKarteDialog(frame, text);
        od.setValue(params);
        od.start();

        // 戻り値をリターンする
        params = (NewKarteParams) od.getValue();
        return params;
    }

    /**
     * 患者の健康保険を返す.
     *
     * @return 患者の健康保険配列
     */
    @Override
    public PVTHealthInsuranceModel[] getHealthInsurances() {

        // 患者の健康保険
        Collection<PVTHealthInsuranceModel> insurances = pvt.getPatient().getPvtHealthInsurances();

        if (insurances == null || insurances.isEmpty()) {
            insurances = new ArrayList<>(1);
            PVTHealthInsuranceModel model = new PVTHealthInsuranceModel();
            model.setInsuranceClass(INSURANCE_SELF);
            model.setInsuranceClassCode(INSURANCE_SELF_CODE);
            model.setInsuranceClassCodeSys(INSURANCE_SYS);
            insurances.add(model);
        }

        return insurances.toArray(new PVTHealthInsuranceModel[0]);
    }

    /**
     * 選択された保険を特定する
     * UUID が見つかったらそれを返す. なかったら最初に見つかった保険を返す.
     *
     * @param uuid 選択された保険のUUID
     * @return 選択された保険
     */
    @Override
    public PVTHealthInsuranceModel getHealthInsuranceToApply(String uuid) {

        // 患者の健康保険
        Collection<PVTHealthInsuranceModel> insurances = pvt.getPatient().getPvtHealthInsurances();
        // insurance model がなければ null を返す
        if (uuid == null || insurances == null || insurances.isEmpty()) {
            return null;
        }

        PVTHealthInsuranceModel ret = null;

        for (PVTHealthInsuranceModel hm : insurances) {
            if (ret == null) {
                ret = hm;
            }

            if (uuid.equals(hm.getGUID())) {
                ret = hm;
                //System.out.println("CharImpl: found uuid to apply = " + uuid);
                break;
            }
        }

        return ret;
    }

    /**
     * タブにドキュメントを追加する.
     *
     * @param doc    追加するドキュメント
     * @param params 追加するドキュメントの情報を保持する NewKarteParams
     */
    public void addChartDocument(ChartDocument doc, NewKarteParams params) {
        String title;
        if (params.getPVTHealthInsurance() != null) {
            title = getTabTitle(params.getDepartment(), params.getPVTHealthInsurance().getInsuranceClass());
        } else {
            title = getTabTitle(params.getDepartment(), null);
        }
        tabbedPane.addTab(title, doc.getUI());
        int index = tabbedPane.getTabCount() - 1;
        providers.put(String.valueOf(index), doc);
        tabbedPane.setSelectedIndex(index);
    }

    /**
     * タブにドキュメントを追加する.
     *
     * @param doc   ChartDocument
     * @param title タブタイトル
     */
    public void addChartDocument(ChartDocument doc, String title) {
        tabbedPane.addTab(title, doc.getUI());
        int index = tabbedPane.getTabCount() - 1;
        providers.put(String.valueOf(index), doc);
        tabbedPane.setSelectedIndex(index);
    }

    /**
     * 新規カルテ用のタブタイトルを作成する.
     * 使ってない.
     *
     * @param dept      Department
     * @param insurance 保険名
     * @return タブタイトル
     */
    public String getTabTitle(String dept, String insurance) {
        String[] depts = dept.split("\\s*,\\s*");
        return String.format("新規カルテ(%s・%s)", depts[0], insurance);
    }

    /**
     * 全てのドキュメントを保存する.
     *
     * @param dirtyList 未保存ドキュメントのリスト
     */
    private void saveAll(List<UnsavedDocument> dirtyList) {

        if (dirtyList == null || dirtyList.isEmpty()) {
            return;
        }

        dirtyList.stream().filter(UnsavedDocument::isNeedSave).forEach(undoc -> {
            ChartDocument doc = providers.get(tabbedPane.getTitleAt(undoc.getIndex()));
            if (doc != null && doc.isDirty()) {
                //tabbedPane.setSelectedIndex(undoc.getIndex());
                doc.save();
            }
        });
    }

    /**
     * ドキュメントのなかにdirtyのものがあるかどうかを返す.
     *
     * @return dirtyの時true
     */
    private List<UnsavedDocument> dirtyList() {
        List<UnsavedDocument> ret = null;
        int count = tabbedPane.getTabCount();
        for (int i = 0; i < count; i++) {
            //ChartDocument doc = providers.get(String.valueOf(i));
            ChartDocument doc = providers.get(tabbedPane.getTitleAt(i));
            if (doc != null && doc.isDirty()) {
                if (ret == null) {
                    ret = new ArrayList<>(3);
                }
                ret.add(new UnsavedDocument(i, doc));
            }
        }
        return ret;
    }

    /**
     * CloseBox がクリックされた時の処理を行う.
     */
    public void processWindowClosing() {
        close();
    }

    /**
     * チャートウインドウを閉じる.
     */
    @Override
    public void close() {

        // この患者の EditorFrame が開いたままなら，閉じる努力をする. EditorFame 保存がキャンセルされたらあきらめる.
        List<Chart> editorFrames = new ArrayList<>(EditorFrame.getAllEditorFrames());
        if (editorFrames.size() > 0) {
            String patientId = this.getKarte().getPatient().getPatientId();
            for (Chart chart : editorFrames) {
                String id = chart.getKarte().getPatient().getPatientId();
                if (patientId.equals(id)) {
                    chart.close();

                    if (EditorFrame.getAllEditorFrames().contains(chart)) {
                        // EditorFrame が消えていないと言うことは，キャンセルされたと言うこと. この場合，chart 終了もキャンセル.
                        // logger.info("ChartImpl#close : canceled");
                        return;
                    }
                }
            }
        }
        // 未保存ドキュメント（病名等）がある場合はダイアログを表示し
        // 保存するかどうかを確認する
        List<UnsavedDocument> dirtyList = dirtyList();

        if (dirtyList != null && dirtyList.size() > 0) {
            String saveAll = "保存";     // 保存;
            String discard = "破棄";  // 破棄;
            String question = "未保存のドキュメントがあります。保存しますか？";
            String title = "未保存処理";
            String cancelText = (String) UIManager.get("OptionPane.cancelButtonText");

            Object[] message = new Object[dirtyList.size() + 1];
            message[0] = question;
            int index = 1;
            for (UnsavedDocument doc : dirtyList) {
                message[index++] = doc.getCheckBox();
            }

            // すでに JSheet が出ている場合は，toFront してリターン
            if (JSheet.isAlreadyShown(getFrame())) {
                getFrame().toFront();
                return;
            }
            // int option = JOptionPane.showOptionDialog(
            int option = JSheet.showOptionDialog(
                    getFrame(),
                    message,
                    ClientContext.getFrameTitle(title),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new String[]{saveAll, discard, cancelText},
                    saveAll);

            switch (option) {
                case 0:
                    // save
                    saveAll(dirtyList);
                    // DiagnosisDocument が isValidOutcome でなければ，close を中止する
                    if (!getDiagnosisDocument().isValidOutcome()) {
                        break;
                    }
                    stop();
                    break;

                case 1:
                    // discard
                    stop();
                    break;

                case 2:
                    // cancel
                    break;
            }

        } else {
            stop();
        }
    }

    @Override
    public void stop() {

        if (providers != null) {
            for (String s : providers.keySet()) {
                ChartDocument doc = providers.get(s);
                if (doc != null) {
                    doc.stop();
                }
            }
            providers.clear();
        }
        mediator.dispose();
        inspector.dispose();
        windowSupport.dispose(); // ここで windowClosed が呼ばれる
    }

    /**
     * Document が modify用に既に開かれていたら toFront する
     * masuda 先生の docAlreadyOpened のパクリ
     *
     * @param baseDocumentModel doc
     * @return toFront できたら true
     */
    public boolean toFrontDocumentIfPresent(DocumentModel baseDocumentModel) {
        if (baseDocumentModel == null || baseDocumentModel.getDocInfo() == null) {
            return false;
        }
        long baseDocPk = baseDocumentModel.getDocInfo().getDocPk();
        List<EditorFrame> editorFrames = EditorFrame.getAllEditorFrames();
        if (!editorFrames.isEmpty()) {
            for (EditorFrame frame : editorFrames) {
                long parentDocPk = frame.getParentDocPk();
                if (baseDocPk == parentDocPk) {
                    // parentPkが同じEditorFrameがある場合はFrameをtoFrontする
                    frame.getFrame().setExtendedState(java.awt.Frame.NORMAL);
                    frame.getFrame().toFront();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * NewKarte が既に開かれていたら toFront する.
     * masuda 先生の newKarteAlreadyOpened のパクリ.
     *
     * @return toFront できたら true
     */
    private boolean toFrontNewKarteIfPresent() {
        List<EditorFrame> editorFrames = EditorFrame.getAllEditorFrames();
        if (!editorFrames.isEmpty()) {
            String patientId = this.getKarte().getPatient().getPatientId();
            for (EditorFrame ef : editorFrames) {
                // 新規カルテだとDocInfoのstatusは"N"
                String status = ef.getDocInfoStatus();
                String id = ef.getKarte().getPatient().getPatientId();
                if (patientId.equals(id) && IInfoModel.STATUS_NONE.equals(status)) {
                    // 新規カルテのEditorFrameがある場合はFrameをtoFrontする
                    ef.getFrame().setExtendedState(Frame.NORMAL);
                    ef.getFrame().toFront();
                    return true;
                }
            }
        }
        return false;
    }

    private abstract class ChartState {

        public ChartState() {
        }

        public abstract void controlMenu();
    }

    /**
     * ReadOnly ユーザの State クラス.
     */
    private final class ReadOnlyState extends ChartState {

        public ReadOnlyState() {
        }

        /**
         * 新規カルテ作成及び修正メニューを disable にする.
         */
        @Override
        public void controlMenu() {
            mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_MODIFY_KARTE).setEnabled(false);

        }
    }

    /**
     * 保険証がない場合の State クラス.
     */
    private final class NoInsuranceState extends ChartState {

        public NoInsuranceState() {
        }

        @Override
        public void controlMenu() {
            mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(false);
        }
    }

    /**
     * 通常の State クラス.
     */
    private final class OrdinalyState extends ChartState {

        public OrdinalyState() {
        }

        @Override
        public void controlMenu() {
            mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(true);
        }
    }

    /**
     * State Manager クラス.
     */
    private final class StateMgr {

        private final ChartState readOnlyState = new ReadOnlyState();
        private final ChartState noInsuranceState = new NoInsuranceState();
        private final ChartState ordinalyState = new OrdinalyState();
        private ChartState currentState;

        public StateMgr() {
            if (isReadOnly()) {
                enterReadOnlyState();
            } else {
                enterOrdinalyState();
            }
        }

        public void enterReadOnlyState() {
            currentState = readOnlyState;
            currentState.controlMenu();
        }

        public void enterNoInsuranceState() {
            currentState = noInsuranceState;
            currentState.controlMenu();
        }

        public void enterOrdinalyState() {
            currentState = ordinalyState;
            currentState.controlMenu();
        }

        public void controlMenu() {
            currentState.controlMenu();
        }
    }
}
