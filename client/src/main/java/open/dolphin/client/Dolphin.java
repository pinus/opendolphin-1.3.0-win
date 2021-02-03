package open.dolphin.client;

import open.dolphin.helper.*;
import open.dolphin.impl.labrcv.LaboTestImporter;
import open.dolphin.impl.login.LoginDialog;
import open.dolphin.impl.psearch.PatientSearchImpl;
import open.dolphin.impl.pvt.WaitingListImpl;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.infomodel.RoleModel;
import open.dolphin.project.AbstractProjectFactory;
import open.dolphin.project.Project;
import open.dolphin.project.ProjectStub;
import open.dolphin.setting.ProjectSettingDialog;
import open.dolphin.stampbox.StampBoxPlugin;
import open.dolphin.ui.MainFrame;
import open.dolphin.ui.PNSBadgeTabbedPane;
import open.dolphin.ui.SettingForWin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.MenuEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

/**
 * アプリケーションのメインウインドウクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class Dolphin implements MainWindow {

    // Window と Menu サポート
    private WindowSupport windowSupport;
    // Mediator
    private Mediator mediator;
    // 状態制御
    private StateManager stateMgr;
    // プラグインのプロバイダ
    private HashMap<String, MainService> providers;
    // pluginを格納する tabbedPane
    private PNSBadgeTabbedPane tabbedPane;
    // timerTask 関連
    private javax.swing.Timer taskTimer;
    // ロガー
    private Logger logger;
    // 環境設定用の Properties
    private Properties saveEnv;
    // BlockGlass
    private BlockGlass blockGlass;
    // SchemaBox
    private ImageBox imageBox;
    // StampBox
    private StampBoxPlugin stampBox;
    // URL クラスローダ
    private ClassLoader pluginClassLoader;
    // dirty 警告を出す Frame を保持
    private Chart dirtyChart;

    // マウスクリックしすぎて同一カルテが重複して開かれるのを防ぐための不応期タイマー
    private Timer refractoryTimer = new Timer(500, e -> refractoryEnd());
    private HashSet<PatientVisitModel> refractoryList = new HashSet<>();

    public Dolphin() {
    }

    /**
     * システムの出力を console.log にリダイレクトする.
     */
    private static void redirectConsole() {
        if (Preferences.userNodeForPackage(Dolphin.class).getBoolean(Project.REDIRECT_CONSOLE, false)) {
            try {
                String logName = System.getProperty("user.dir") + "/console.log";
                PrintStream ps = new PrintStream(new FileOutputStream(logName, true), true); // append, auto flush
                System.setOut(ps);
                System.setErr(ps);
                System.out.println("Console redirected to " + logName);
            } catch (FileNotFoundException ex) {
            }
        }
    }

    /**
     * bash script "startup.sh" を実行する.
     */
    private static void executeStartupScript() {
        String scriptName = System.getProperty("user.dir") + "/startup.sh";
        Path path = Paths.get(scriptName);
        if (Files.exists(path)) {
            try {
                String command = String.join("\n", Files.readAllLines(path));
                List<String> response = ScriptExecutor.executeShellScriptWithResponce(new String[] {"bash", "-c", command});
                response.stream().forEach(System.out::println);

            } catch (IOException e) {
            }
        }
    }

    public static void main(String[] args) {
        // コンソールのリダイレクト
        redirectConsole();

        // startup script
        executeStartupScript();

        // Dolphin 本体の実行
        Dolphin d = new Dolphin();
        d.initialize();
        d.startup();
    }

    /**
     * 初期化. 最初に呼ばれる.
     */
    private void initialize() {

        SettingForWin.set(this);

        // default locale を設定する
        Locale.setDefault(new Locale("ja", "JP"));

        // ClientContext を生成する
        ClientContextStub stub = new ClientContextStub();
        ClientContext.setClientContextStub(stub);

        // プロジェクトスタブを生成する
        Project.setProjectStub(new ProjectStub());

        // Resources
        Project.getPreferences().putInt("diagnosis.table.clickCountToStart", 2);
        //UIManager.put("Component.visualMargin", new Insets(0,2,0,2));
        UIManager.put("ComboBox.maximumRowCount", ClientContext.getInt("ComboBox.maximumRowCount"));
        UIManager.put("TextComponent.autoSelect", ClientContext.getBoolean("TextComponent.autoSelect"));

        //Option Pane Strings and Icons
        UIManager.put("OptionPane.okButtonText", ClientContext.getString("OptionPane.okButtonText"));
        UIManager.put("OptionPane.yesButtonText", ClientContext.getString("OptionPane.yesButtonText"));
        UIManager.put("OptionPane.noButtonText", ClientContext.getString("OptionPane.noButtonText"));
        UIManager.put("OptionPane.cancelButtonText", ClientContext.getString("OptionPane.cancelButtonText"));
        UIManager.put("OptionPane.errorIcon", GUIConst.ICON_ERROR_32);
        UIManager.put("OptionPane.informationIcon", GUIConst.ICON_INFORMATION_32);
        UIManager.put("OptionPane.questionIcon", GUIConst.ICON_QUESTION_32);
        UIManager.put("OptionPane.warningIcon", GUIConst.ICON_WARNING_32);

        // ToolTip を自然に消えないようにする
        ToolTipManager manager = ToolTipManager.sharedInstance();
        manager.setInitialDelay(500);
        manager.setDismissDelay(Integer.MAX_VALUE);

        // PluginClassLoader
        pluginClassLoader = ClientContext.getPluginClassLoader();

        // ロガーを取得する
        logger = LoggerFactory.getLogger(Dolphin.class);

        // ドキュメントフォルダの有無をチェック
        checkDocumentFolder();
    }

    /**
     * initialize() の次に呼ばれる.
     */
    private void startup() {
        // ログインダイアログを表示し認証を行う
        LoginDialog login = new LoginDialog();

        login.addLoginListener(state -> {
            switch (state) {
                case AUTHENTICATED:
                    startServices();
                    initComponents();
                    break;
                case NOT_AUTHENTICATED:
                case CANCELD:
                    exit();
                    break;
            }
        });

        login.start();
    }

    /**
     * 起動時のバックグラウンドで実行されるべきタスクを行う. ログイン後に startup() から呼ばれる.
     */
    private void startServices() {

        // プラグインのプロバイダマップを生成する
        setProviders(new HashMap<>());

        // 環境設定ダイアログで変更される場合があるので保存する
        saveEnv = new Properties();

        // Holiday database 構築
        Holiday.setupCalendarData();
    }

    /**
     * GUI を構築して最初の画面を表示する. ログイン後に startup() から呼ばれる.
     */
    private void initComponents() {

        // デバッグ用-------------------------
        // javax.swing.RepaintManager.setCurrentManager(new VerboseRepaintManager());
        //new FocusMonitor();

        // 設定に必要な定数をコンテキストから取得する
        String windowTitle = ClientContext.getString("title");
        Rectangle setBounds = new Rectangle(0, 0, 1000, 690);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int defaultX = (screenSize.width - setBounds.width) / 2;
        int defaultY = (screenSize.height - setBounds.height) / 2;
        int defaultWidth = 666;
        int defaultHeight = 678;

        // WindowSupport を生成する この時点で Frame,WindowMenu を持つMenuBar が生成されている
        String title = ClientContext.getFrameTitle(windowTitle);
        // System.out.println(title);
        windowSupport = WindowSupport.create(title);
        MainFrame myFrame = windowSupport.getFrame();        // MainWindow の JFrame
        JMenuBar myMenuBar = windowSupport.getMenuBar();    // MainWindow の JMenuBar

        // Windowにこのクラス固有の設定をする
        Point loc = new Point(defaultX, defaultY);
        Dimension size = new Dimension(defaultWidth, defaultHeight);
        myFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                processExit();
            }
        });
        ComponentBoundsManager cm = new ComponentBoundsManager(myFrame, loc, size, this);
        cm.revertToPreferenceBounds();

        // BlockGlass を設定する
        blockGlass = new BlockGlass();
        blockGlass.setSize(myFrame.getSize());
        myFrame.setGlassPane(blockGlass);

        // mainWindowのメニューを生成しメニューバーに追加する
        mediator = new Mediator(this);
        MenuFactory appMenu = new MenuFactory();
        appMenu.setMenuSupports(mediator, null);
        appMenu.build(myMenuBar);
        mediator.registerActions(appMenu.getActionMap());

        // mainWindowのコンテントGUIを生成しFrameに追加する
        tabbedPane = new PNSBadgeTabbedPane();
        //tabbedPane.setButtonVgap(4);

        MainFrame.MainPanel mainPanel = myFrame.getMainPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // MainComponent では，MainFrame の CommandPanel, StatusPanel は使わない
        // MainComponentPanel で自前で用意する
        myFrame.removeCommandPanel();
        myFrame.removeStatusPanel();

        // タブペインに格納する Plugin をロードする
        MainComponent[] plugin = new MainComponent[3];
        plugin[0] = new WaitingListImpl();
        plugin[1] = new PatientSearchImpl();
        plugin[2] = new LaboTestImporter();

        for (int index = 0; index < plugin.length; index++) {
            plugin[index].setContext(this);
            plugin[index].start();
            tabbedPane.addTab(plugin[index].getName(), plugin[index].getUI());
            providers.put(String.valueOf(index), plugin[index]);
        }

        mediator.addChain(plugin[0]);

        // タブの切り替えで plugin.enter() をコールする
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            MainComponent plugin1 = (MainComponent) providers.get(String.valueOf(index));
            if (plugin1.getContext() == null) {
                plugin1.setContext(Dolphin.this);
                plugin1.start();
                tabbedPane.setComponentAt(index, plugin1.getUI());
            }
            plugin1.enter();
            mediator.addChain(plugin1);
        });

        // StaeMagrを使用してメインウインドウの状態を制御する
        stateMgr = new StateManager();
        stateMgr.processLogin(true);

        stampBox = new StampBoxPlugin();
        stampBox.setContext(Dolphin.this);

        final Callable<Boolean> task = stampBox.getStartingTask();

        String message = "スタンプ箱";
        String note = "スタンプツリーを読み込んでいます...";
        Component c = windowSupport.getFrame();

        Task<Boolean> stampTask = new Task<Boolean>(c, message, note, 30 * 1000) {

            @Override
            protected Boolean doInBackground() throws Exception {
                logger.debug("stampTask doInBackground");
                return task.call();
            }

            @Override
            protected void succeeded(Boolean result) {
                logger.debug("stampTask succeeded");
                if (result) {
                    stampBox.start();
                    stampBox.getFrame().setVisible(true);
                    providers.put("stampBox", stampBox);
                    windowSupport.getFrame().setVisible(true);

                } else {
                    System.exit(1);
                }
            }

            @Override
            protected void failed(Throwable cause) {
                cause.printStackTrace(System.err);
                System.exit(1);
            }

            @Override
            protected void cancelled() {
                logger.debug("stampTask cancelled");
                System.exit(1);
            }
        };
        //stampTask.setMillisToPopup(200);
        stampTask.execute();
    }

    /**
     * MainComponent を入れる TabbedPane を返す.
     *
     * @return PNSBadgeTabbedPane
     */
    public PNSBadgeTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    @Override
    public BlockGlass getGlassPane() {
        return blockGlass;
    }

    @Override
    public MainService getPlugin(String id) {
        return providers.get(id);
    }

    @Override
    public HashMap<String, MainService> getProviders() {
        return providers;
    }

    @Override
    public void setProviders(HashMap<String, MainService> providers) {
        this.providers = providers;
    }

    /**
     * カルテをオープンする.
     *
     * @param pvt 患者来院情報
     */
    @Override
    public synchronized void openKarte(PatientVisitModel pvt) {
        // 不応期タイマースタート
        refractoryTimer.restart();
        if (refractoryList.stream().map(PatientVisitModel::getPatientId).anyMatch(pvt.getPatientId()::equals)) {
            logger.info("openKarte does not respond in refractory period");
            return;
        }
        refractoryList.add(pvt);

        Chart chart = new ChartImpl();
        chart.setContext(this);
        chart.setPatientVisit(pvt);                 //
        // isReadOnly対応
        //chart.setReadOnly(Project.isReadOnly() || pvt.getState() == KarteState.READ_ONLY);    // RedaOnlyProp
        chart.setReadOnly(Project.isReadOnly());    // RedaOnlyProp
        chart.start();
    }

    /**
     * 不応期終了.
     */
    public void refractoryEnd() {
        refractoryTimer.stop();
        refractoryList.clear();
    }

    /**
     * 新規診療録を作成する. 使ってない？ (MainWindow の implement に必要)
     */
    @Override
    public void addNewPatient() {
        // not implemented
    }

    @Override
    public MenuSupport getMenuSupport() {
        return mediator;
    }

    /**
     * MainWindow のアクションを返す.
     *
     * @param name Action名
     * @return Action
     */
    @Override
    public Action getAction(String name) {
        return mediator.getAction(name);
    }

    @Override
    public JMenuBar getMenuBar() {
        return windowSupport.getMenuBar();
    }

    @Override
    public void registerActions(ActionMap actions) {
        mediator.registerActions(actions);
    }

    @Override
    public void enableAction(String name, boolean b) {
        mediator.enableAction(name, b);
    }

    @Override
    public JFrame getFrame() {
        return windowSupport.getFrame();
    }

    /**
     * 使ってない. (MainWindow の implement に必要)
     *
     * @return PageFormat
     */
    @Override
    public PageFormat getPageFormat() {
        return PrinterJob.getPrinterJob().getPageFormat(null);
    }

    /**
     * ブロックする.
     */
    @Override
    public void block() {
        blockGlass.block();
    }

    /**
     * ブロックを解除する.
     */
    @Override
    public void unblock() {
        blockGlass.unblock();
    }

    /**
     * カルテの環境設定を行う. メニューから reflection で呼ばれる.
     */
    public void setKarteEnviroment() {
        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        ProjectSettingDialog sd = new ProjectSettingDialog(focusManager.getActiveWindow());
        sd.addValidListener(this::controlService);
        sd.setLoginState(stateMgr.isLogin());
        sd.setProject("karteSetting");
        sd.start();
    }

    /**
     * 環境設定を行う.
     * desktop PreferencesHandler  から呼ばれる.
     */
    public void doPreference() {
        setKarteEnviroment();
    }

    /**
     * 環境設定から呼ばれる. 値によりサービスを制御する.
     *
     * @param valid ValidListener validity
     */
    private void controlService(boolean valid) {
        if (!valid) {
            return;
        }

        // 設定の変化を調べ，サービスの制御を行う
        List<String> messages = new ArrayList<>();

        // ここで処理して message をセットして下の Dialog で表示する

        if (!messages.isEmpty()) {
            String[] msgArray = messages.toArray(new String[0]);
            Component cmp = null;
            String title = ClientContext.getString("settingDialog.title");

            JOptionPane.showMessageDialog(
                    cmp,
                    msgArray,
                    ClientContext.getFrameTitle(title),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Dirty check.
     *
     * @return dirty or not
     */
    private boolean isDirty() {

        // 未保存のカルテがある場合は警告しリターンする
        // カルテを保存または破棄してから再度実行する
        boolean dirty = false;

        // Chart を調べる
        List<ChartImpl> allChart = ChartImpl.getAllChart();
        for (ChartImpl chart : allChart) {
            if (chart.isDirty()) {
                dirty = true;
                dirtyChart = chart;
                break;
            }
        }
        // 保存してないものがあればリターンする
        if (dirty) {
            return true;
        }

        // EditorFrameのチェックを行う
        List<EditorFrame> allEditorFrames = EditorFrame.getAllEditorFrames();
        for (EditorFrame chart : allEditorFrames) {
            if (chart.isDirty()) {
                dirty = true;
                dirtyChart = chart;
                break;
            }
        }

        return dirty;
    }

    /**
     * MainTool の StoppingTask を集めた Callable リストを生成する.
     *
     * @return 作った Callabel のリスト
     */
    private List<Callable<Boolean>> getStoppingTask() {

        // StoppingTask を集める
        List<Callable<Boolean>> stoppingTasks = new ArrayList<>(1);

        // ログイン前に終了すると，providers = null でここに入って exception が出る
        if (providers == null) {
            return stoppingTasks;
        }

        providers.values().forEach(service -> {
            if (service instanceof MainTool) {
                Callable<Boolean> task = ((MainTool) service).getStoppingTask();
                if (task != null) {
                    stoppingTasks.add(task);
                }
            }
        });
        // WaitingListImpl と StampBoxPlugin
        // stoppingTasks.forEach(task -> System.out.println("stopping task = " + task));
        return stoppingTasks;
    }

    /**
     * 終了処理.
     */
    public void processExit() {

        if (isDirty()) {
            dirtyChart.close(); // dirtyChart あれば，クローズして，終了処理自体はキャンセル
            return;
        }

        // 終了確認
        if (saveEnv != null && !ClientContext.isWin()) {
            Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            int ans = JOptionPane.showConfirmDialog(null,
                    "本当に終了しますか", "終了確認", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ans != JOptionPane.YES_OPTION) {
                activeWindow.toFront();
                return;
            }
        }

        // MainTool, MainComponent で getStoppingTask を override するとここで呼ばれる
        final List<Callable<Boolean>> tasks = getStoppingTask();

        if (tasks.isEmpty()) {
            exit();

        } else {
            // 釣れた stoppingTask を実行
            String message = ClientContext.getString("exitDolphin.taskTitle");
            String note = ClientContext.getString("exitDolphin.savingNote");
            Component c = getFrame();

            Task<Boolean> stampTask = new Task<Boolean>(c, message, note, 60 * 1000) {

                @Override
                protected Boolean doInBackground() throws Exception {
                    logger.debug("stoppingTask doInBackground");
                    boolean success = true;
                    for (Callable<Boolean> c : tasks) {
                        Boolean result = c.call();
                        if (!result) {
                            success = false;
                            break;
                        }
                    }
                    return success;
                }

                @Override
                protected void succeeded(Boolean result) {
                    logger.debug("stoppingTask succeeded");
                    if (result) {
                        exit();
                    } else {
                        doStoppingAlert();
                    }
                }

                @Override
                protected void failed(Throwable t) {
                    t.printStackTrace(System.err);
                    doStoppingAlert();
                }
            };
            //stampTask.setMillisToPopup(200);
            stampTask.execute();
        }
    }

    /**
     * 終了処理中にエラーが生じた場合の警告をダイアログを表示する.
     */
    private void doStoppingAlert() {

        String msg1 = ClientContext.getString("exitDolphin.err.msg1");
        String msg2 = ClientContext.getString("exitDolphin.err.msg2");
        String msg3 = ClientContext.getString("exitDolphin.err.msg3");
        String msg4 = ClientContext.getString("exitDolphin.err.msg4");
        Object message = new Object[]{msg1, msg2, msg3, msg4};

        // 終了する
        String exitOption = ClientContext.getString("exitDolphin.exitOption");

        // キャンセルする
        String cancelOption = ClientContext.getString("exitDolphin.cancelOption");

        // 環境保存
        String taskTitle = ClientContext.getString("exitDolphin.taskTitle");

        String title = ClientContext.getFrameTitle(taskTitle);

        String[] options = new String[]{cancelOption, exitOption};

        int option = JOptionPane.showOptionDialog(
                null, message, title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);

        if (option == 1) {
            exit();
        }
    }

    /**
     * 最終 exit.
     */
    private void exit() {

        if (providers != null) {
            providers.values().forEach(MainService::stop);
        }

        if (windowSupport != null) {
            JFrame myFrame = windowSupport.getFrame();
            myFrame.setVisible(false);
            myFrame.dispose();
        }
        logger.info("アプリケーションを終了します");
        System.exit(0);
    }

    /**
     * ユーザのパスワードを変更する. メニューから reflection で呼ばれる.
     */
    public void changePassword() {
        ChangePassword cp = new ChangePassword();
        cp.setContext(this);
        cp.start();
    }

    /**
     * ユーザ登録を行う. 管理者メニュー. メニューから reflection で呼ばれる.
     */
    public void addUser() {
        AddUser au = new AddUser();
        au.setContext(this);
        au.start();
    }

    /**
     * ドルフィンサポートをオープンする.
     */
    public void browseDolphinSupport() {
        browseURL(ClientContext.getString("menu.dolphinSupportUrl"));
    }

    /**
     * ドルフィンプロジェクトをオープンする.
     */
    public void browseDolphinProject() {
        browseURL(ClientContext.getString("menu.dolphinUrl"));
    }

    /**
     * MedXMLをオープンする.
     */
    public void browseMedXml() {
        browseURL(ClientContext.getString("menu.medXmlUrl"));
    }

    /**
     * SGをオープンする.
     */
    public void browseSeaGaia() {
        browseURL(ClientContext.getString("menu.seaGaiaUrl"));
    }

    /**
     * URLをオープンする.
     *
     * @param url URL
     */
    private void browseURL(String url) {

        try {
            if (ClientContext.isMac()) {
                ProcessBuilder builder = new ProcessBuilder("open", url);
                builder.start();

            } else if (ClientContext.isWin()) {
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "start", url);
                builder.start();

            } else {
                String[] browsers = {
                        "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"
                };
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(
                            new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                    if (browser == null) {
                        throw new Exception("Could not find web browser");
                    } else {
                        Runtime.getRuntime().exec(new String[]{browser, url});
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * About を表示する.
     */
    public void showAbout() {
        Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        AbstractProjectFactory f = Project.getProjectFactory();
        f.createAboutDialog();
        activeWindow.toFront();
    }

    /**
     * シェーマボックスを表示する.
     */
    @Override
    public void showSchemaBox() {
        if (imageBox == null) {
            imageBox = new ImageBox();
            imageBox.setContext(this);
            imageBox.start();
        } else {
            imageBox.enter();
        }
    }

    /**
     * スタンプボックスを表示する.
     */
    @Override
    public void showStampBox() {
        if (stampBox != null) {
            stampBox.enter();
        }
    }

    /**
     * Waiting list を表示する.
     * MenuSupport から呼ばれる.
     */
    public void showWaitingList() {
        // Search field に focus させるためのトリック
        windowSupport.getFrame().toFront();
        tabbedPane.setSelectedIndex(1);
        tabbedPane.setSelectedIndex(0);
    }

    /**
     * Patient search を表示する.
     * MenuSupport から呼ばれる.
     */
    public void showPatientSearch() {
        // Search field に focus させるためのトリック
        windowSupport.getFrame().toFront();
        tabbedPane.setSelectedIndex(0);
        tabbedPane.setSelectedIndex(1);
    }

    /**
     * DocumentFolder をチェック.
     */
    private void checkDocumentFolder() {
        Path path = Paths.get(ClientContext.getDocumentDirectory());
        logger.info("document folder = " + path);

        try {
            if (Files.list(path).count() != 0) {
                return;
            }
        } catch (IOException e) {
        }
        JOptionPane.showMessageDialog(null, "文書フォルダが見つかりません", "", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * MainWindowState.
     */
    private interface MainWindowState {
        void enter();

        boolean isLogin();
    }

    /**
     * Mediator.
     */
    private final class Mediator extends MenuSupport {

        public Mediator(Object owner) {
            super(owner);
        }

        // global property の制御
        @Override
        public void menuSelected(MenuEvent e) {
        }

        @Override
        public void registerActions(ActionMap actions) {
            super.registerActions(actions);
            // メインウインドウなので閉じるだけは無効にする
            //getAction(GUIConst.ACTION_WINDOW_CLOSING).setEnabled(false);
        }
    }

    /**
     * LoginState.
     */
    private class LoginState implements MainWindowState {

        public LoginState() {
        }

        @Override
        public boolean isLogin() {
            return true;
        }

        @Override
        public void enter() {

            // Menuを制御する
            mediator.disableAllMenus();

            String[] enables = new String[]{
                    GUIConst.ACTION_PRINTER_SETUP,
                    GUIConst.ACTION_PROCESS_EXIT,
                    GUIConst.ACTION_SET_KARTE_ENVIROMENT,
                    GUIConst.ACTION_SHOW_STAMPBOX,
                    GUIConst.ACTION_SHOW_SCHEMABOX,
                    GUIConst.ACTION_SHOW_WAITING_LIST,
                    GUIConst.ACTION_SHOW_PATIENT_SEARCH,
                    GUIConst.ACTION_CHANGE_PASSWORD,
                    GUIConst.ACTION_SHOW_ABOUT,
            };
            mediator.enableMenus(enables);

            Action addUserAction = mediator.getAction(GUIConst.ACTION_ADD_USER);
            boolean admin = Project.getUserModel().getRoles().stream().map(RoleModel::getRole).anyMatch(GUIConst.ROLE_ADMIN::equals);
            addUserAction.setEnabled(admin);
        }
    }

    /**
     * LogoffState.
     */
    private class LogoffState implements MainWindowState {

        public LogoffState() {
        }

        @Override
        public boolean isLogin() {
            return false;
        }

        @Override
        public void enter() {
            mediator.disableAllMenus();
        }
    }

    /**
     * StateManager.
     */
    private class StateManager {

        private final MainWindowState loginState = new LoginState();
        private final MainWindowState logoffState = new LogoffState();
        private MainWindowState currentState = logoffState;

        public StateManager() {
        }

        public boolean isLogin() {
            return currentState.isLogin();
        }

        public void processLogin(boolean b) {
            currentState = b ? loginState : logoffState;
            currentState.enter();
        }
    }

    // デバッグ用
    private static class FocusMonitor implements PropertyChangeListener {
        private final KeyboardFocusManager focusManager;

        public FocusMonitor() {
            focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            focusManager.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            System.out.println("FocusManager Report ----------------");
            System.out.println(" oldValue=" + e.getOldValue());
            System.out.println(" newValue=" + e.getNewValue());
            System.out.println(" Active Window=" + focusManager.getActiveWindow());
            System.out.println(" Focused Window=" + focusManager.getFocusedWindow());
            System.out.println(" Focus Owner=" + focusManager.getFocusOwner());
            System.out.println(" Permanent Focus Owner=" + focusManager.getPermanentFocusOwner());
            System.out.println("---");
        }
    }

    private class VerboseRepaintManager extends javax.swing.RepaintManager {

        @Override
        public synchronized void addDirtyRegion(javax.swing.JComponent c, int x, int y, int w, int h) {
            if (windowSupport != null) {
                if (javax.swing.SwingUtilities.getWindowAncestor(c) == windowSupport.getFrame()) {
                    System.out.println(c.getClass().toString().replace("class javax.swing.", "") + ":" + x + ":" + y + ":" + w + ":" + h);
                }
            }
            super.addDirtyRegion(c, x, y, w, h);
        }

        @Override
        public void paintDirtyRegions() {
            // Unfortunately most of the RepaintManager state is package
            // private and not accessible from the subclass at the moment,
            // so we can't print more info about what's being painted.
            super.paintDirtyRegions();
        }
    }
}
