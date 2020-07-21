package open.dolphin.stampbox;

import open.dolphin.client.*;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.dnd.StampTreeNodeTransferHandler;
import open.dolphin.helper.ComponentBoundsManager;
import open.dolphin.helper.MenuSupport;
import open.dolphin.helper.WindowSupport;
import open.dolphin.infomodel.*;
import open.dolphin.order.stampeditor.StampEditor;
import open.dolphin.project.Project;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.MainFrame;
import open.dolphin.ui.PNSSafeToggleButton;
import open.dolphin.ui.PNSTabbedPane;
import open.dolphin.ui.sheet.JSheet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

/**
 * StampBoxPlugin.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class StampBoxPlugin extends AbstractMainTool {
    private static final String NAME = "スタンプ箱";

    // frameのデフォルトの大きさ及びタイトル
    private final int DEFAULT_WIDTH = 320;
    private final int DEFAULT_HEIGHT = 690;
    private final int IMPORT_TREE_OFFSET = 1;
    // mac フラグ
    private final boolean isMac;
    // Logger
    private final Logger logger;
    // StampBox の JFrame
    private MainFrame frame;
    // StampBox
    private PNSTabbedPane parentBox;
    //ユーザ個人用の StampBox
    private AbstractStampBox userBox;
    // 現在選択されている StampBox
    private AbstractStampBox curBox;
    // インポートしている StampTree のリスト
    private List<Long> importedTreeList;
    // Stampmaker ボタン
    private JToggleButton toolBtn;
    // StampMakerPanel
    private StampMakerPanel stampMaker;
    // StampMakerPanel モードのフラグ
    private boolean editing;
    // Editorの編集値リスナ StampMakerPanel をリスンする
    private EditorValueListener editorValueListener;
    // StampBox 位置
    private Point stampBoxLoc;
    // StampBox 幅
    private int stampBoxWidth;
    // StampBox 高さ
    private int stampBoxHeight;
    // Block Glass Pane
    private BlockGlass glass;
    // このスタンプボックスの StmpTreeModel
    private List<StampTreeBean> stampTreeModels;
    // mac でメニューが消えないようにするために使う
    private MenuSupport mediator;
    // ロックボタン
    private PNSSafeToggleButton lockBtn;
    // ExtraMenu(gear) ボタン
    private JButton extraBtn;
    // ExtraMenu を押すと呼ばれる
    private StampBoxExtraMenu extraMenu;
    // collapseAll ボタン
    private JButton collapseBtn;
    private boolean isLocked = true;

    public StampBoxPlugin() {
        setName(NAME);
        logger = LoggerFactory.getLogger(StampBoxPlugin.class);
        isMac = ClientContext.isMac();
    }

    /**
     * スタンプ項目の移動がロックされているかどうか.
     *
     * @return true if locked
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * スタンプ項目の移動ロックを設定する.
     *
     * @param lock true to lock
     */
    private void setLocked(boolean lock) {
        this.isLocked = lock;
        if (lock) {
            frame.setTitle(getCurrentBox().getInfo());
        } else {
            // lock が外れていたら注意マークを出す
            int spaces = 0;
            if (isMac && stampBoxWidth != 0) {
                // stamp maker が起動していたらタイトルを右の方に出す
                spaces = (int) ((frame.getWidth() - stampBoxWidth) * 0.27f);
            }
            frame.setTitle(StringUtils.repeat(' ', spaces) + "⚠️" + getCurrentBox().getInfo());
        }
    }

    /**
     * 現在の StampBox を返す.
     *
     * @return 現在選択されているStampBox
     */
    public AbstractStampBox getCurrentBox() {
        return curBox;
    }

    /**
     * 現在の StampBox を設定する.
     *
     * @param curBox 選択されたStampBox
     */
    public void setCurrentBox(AbstractStampBox curBox) {
        this.curBox = curBox;
    }

    /**
     * User(個人用)の StampBox を返す.
     *
     * @return User(個人用)のStampBox
     */
    public AbstractStampBox getUserStampBox() {
        return userBox;
    }

    /**
     * User(個人用)の StampBox を設定する.
     *
     * @param userBox User(個人用)のStampBox
     */
    public void setUserStampBox(AbstractStampBox userBox) {
        this.userBox = userBox;
    }

    /**
     * StampBox の JFrame を返す.
     *
     * @return StampBox の JFrame
     */
    public MainFrame getFrame() {
        return frame;
    }

    /**
     * インポートしている StampTree のリストを返す.
     *
     * @return インポートしているStampTreeのリスト
     */
    public List<Long> getImportedTreeList() {
        return importedTreeList;
    }

    /**
     * Block用GlassPaneを返す.
     *
     * @return Block用GlassPane
     */
    public BlockGlass getBlockGlass() {
        return glass;
    }

    /**
     * StampTree をデータベースまたはリソースから読み込む.
     * アプリケーションの起動時に一括してコールされる.
     *
     * @return Callable
     */
    @Override
    public Callable<Boolean> getStartingTask() {
        return () -> {
            try {
                // UserPkを取得する
                long userPk = Project.getUserModel().getId();

                // データベース検索を行う
                StampDelegater stampDel = new StampDelegater();
                List<StampTreeBean> treeList = stampDel.getTrees(userPk);
                if (!stampDel.isNoError()) {
                    logger.error("Could't read the stamp tree");
                    return false;
                }
                logger.info("Read the user's tree successfully");

                // User 用の StampTree が存在するかどうか
                boolean hasTree = false;
                if (!treeList.isEmpty()) {
                    for (StampTreeBean tree : treeList) {
                        if (tree != null) { // null の可能性がある
                            long id = tree.getUser().getId();
                            if (id == userPk && tree instanceof PersonalTreeModel) {
                                hasTree = true;
                                break;
                            }
                        }
                    }
                }
                // 新規ユーザでデータベースに個人用の StampTree が存在しなかった場合，stamptree-seed.xml から新規に作成する
                if (!hasTree) {
                    logger.info("New user, constract user's tree by resource");
                    try (
                            InputStream in = ClientContext.getResourceAsStream("stamptree-seed.xml");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

                        String line;
                        StringBuilder sb = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        // Tree情報を設定し保存する
                        StampTreeBean bean = new PersonalTreeModel();
                        bean.setUser(Project.getUserModel());
                        bean.setName(ClientContext.getString("stampTree.personal.box.name"));
                        bean.setDescription(ClientContext.getString("stampTree.personal.box.tooltip"));
                        FacilityModel facility = Project.getUserModel().getFacilityModel();
                        bean.setPartyName(facility.getFacilityName());
                        String url = facility.getUrl();
                        if (url != null) {
                            bean.setUrl(url);
                        }
                        bean.setTreeXml(sb.toString());
                        // リストの先頭へ追加する
                        treeList.add(0, bean);
                    }
                }
                stampTreeModels = treeList;
                return true;

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            return false;
        };
    }

    /**
     * プログラムを開始する.
     */
    @Override
    public void start() {
        if (stampTreeModels == null) {
            logger.error("StampTreeModel is null");
            throw new RuntimeException("Fatal error: StampTreeModel is null at start.");
        }

        //
        // StampBoxのJFrameを生成する
        //
        String title = NAME;
        Rectangle setBounds = new Rectangle(0, 0, 1000, 690);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int defaultX = (screenSize.width - setBounds.width) / 2;
        int defaultY = (screenSize.height - setBounds.height) / 2;
        int defaultWidth = setBounds.width;
        int defaultHeight = setBounds.height;
        int x = (defaultX + defaultWidth) - DEFAULT_WIDTH;
        int y = defaultY;
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;

        // mac で StampBox にもメニューバーを出す
        if (isMac) {
            WindowSupport windowSupport = WindowSupport.create(title);
            frame = windowSupport.getFrame();
            frame.getRootPane().putClientProperty(WindowSupport.MENUBAR_HEIGHT_OFFSET_PROP, 105);
            javax.swing.JMenuBar myMenuBar = windowSupport.getMenuBar();
            mediator = new MenuSupport(this);
            MenuFactory appMenu = new MenuFactory();

            // mainWindow の menuSupport をセットしておけばメニュー処理は mainWindow がしてくれる
            appMenu.setMenuSupports(getContext().getMenuSupport(), mediator);
            appMenu.build(myMenuBar);
            mediator.registerActions(appMenu.getActionMap());
            mediator.disableAllMenus();
            String[] enables = new String[]{
                GUIConst.ACTION_SHOW_SCHEMABOX,
                GUIConst.ACTION_SET_KARTE_ENVIROMENT,
                GUIConst.ACTION_SHOW_WAITING_LIST,
                GUIConst.ACTION_SHOW_PATIENT_SEARCH,
                GUIConst.ACTION_UNDO,
                GUIConst.ACTION_REDO
            };
            mediator.enableMenus(enables);

        } else {
            frame = new MainFrame(title);
        }
        frame.removeStatusPanel();

        glass = new BlockGlass();
        frame.setGlassPane(glass);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        ComponentBoundsManager cm = new ComponentBoundsManager(frame, new Point(x, y), new Dimension(width, height), this);
        cm.revertToPreferenceBounds();

        //
        // 全体のボックスを生成する
        //
        parentBox = new PNSTabbedPane();
        parentBox.setTabPlacement(JTabbedPane.BOTTOM);
        parentBox.setButtonPanelPadding(new Dimension(0, 4));

        //
        // 読み込んだStampTreeをTabbedPaneに格納し，さらにそれをparentBoxに追加する
        //
        for (StampTreeBean model : stampTreeModels) {
            if (model != null) {
                logger.debug("id = " + model.getId());
                logger.debug("name = " + model.getName());
                logger.debug("publishType = " + model.getPublishType());
                logger.debug("category = " + model.getCategory());
                logger.debug("partyName = " + model.getPartyName());
                logger.debug("url = " + model.getUrl());
                logger.debug("description = " + model.getDescription());
                logger.debug("publishedDate = " + model.getPublishedDate());
                logger.debug("lastUpdated = " + model.getLastUpdated());
                logger.debug("userId = " + model.getUser());

                //
                // ユーザ個人用StampTreeの場合
                //
                if (model.getUser().getId() == Project.getUserModel().getId() && model instanceof PersonalTreeModel) {

                    //
                    // 個人用のスタンプボックス(JTabbedPane)を生成する
                    //
                    userBox = new UserStampBox();

                    userBox.setButtonPanelPadding(new Dimension(16, 4));
                    userBox.setContext(this);
                    userBox.setStampTreeModel(model);
                    userBox.buildStampBox();

                    //
                    // ParentBox に追加する
                    //
                    parentBox.addTab(ClientContext.getString("stampTree.personal.box.name"), userBox);

                } else if (model instanceof PublishedTreeModel) {
                    //
                    // インポートしているTreeの場合
                    //
                    importPublishedTree(model);
                }
                model.setTreeXml(null);
            }
        }

        //
        // StampTreeModel を clear する
        //
        stampTreeModels.clear();

        // ParentBox のTab に tooltips を設定する
        for (int i = 0; i < parentBox.getTabCount(); i++) {
            AbstractStampBox box = (AbstractStampBox) parentBox.getComponentAt(i);
            parentBox.setToolTipTextAt(i, box.getInfo());
        }

        //
        // ParentBoxにChangeListenerを登録しスタンプメーカの制御を行う
        //
        parentBox.addChangeListener(new BoxChangeListener());
        setCurrentBox(userBox);

        //
        // ユーザBox用にChangeListenerを設定する
        //
        userBox.addChangeListener(new TabChangeListener());

        //
        // スタンプメーカを起動するためのボタンを生成する
        //
        toolBtn = new JToggleButton(GUIConst.ICON_EDIT_ALT1_16);
        toolBtn.setSelectedIcon(GUIConst.ICON_EDIT_ALT2_16);
        toolBtn.setPressedIcon(GUIConst.ICON_EDIT_ALT2_16);
        toolBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        toolBtn.setToolTipText("スタンプメーカを起動します");
        toolBtn.setFocusable(false);
        toolBtn.putClientProperty("Quaqua.Button.style", "bevel");
        toolBtn.setContentAreaFilled(false);
        toolBtn.addActionListener(e -> {
            if (!editing) {
                startStampMake();
                editing = true;
                if (isLocked) { lockBtn.doClick(); }

            } else {
                stopStampMake();
                editing = false;
                if (!isLocked) { lockBtn.doClick(); }
            }
        });

        // ロックボタンを生成する
        lockBtn = new PNSSafeToggleButton();
        lockBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        lockBtn.setIcon(GUIConst.ICON_PADLOCK_CLOSED_16);
        lockBtn.setSelectedIcon(GUIConst.ICON_PADLOCK_OPEN_16);
        lockBtn.setToolTipText("ダブルクリックでツリー内での入れ替えのロック／解除をします");
        lockBtn.setFocusable(false);
        lockBtn.setPreferredSize(new java.awt.Dimension(16, 16));
        // lockBtn は選択されていたらロック解除
        lockBtn.addActionListener(e -> setLocked(!lockBtn.isSelected()));

        // 特別メニュー(gear)ボタンを生成する
        extraBtn = new JButton();
        extraBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        extraBtn.setContentAreaFilled(false);
        extraBtn.setIcon(GUIConst.ICON_GEAR_16);
        extraBtn.setToolTipText("特別メニュー");
        extraBtn.setFocusable(false);
        extraBtn.setPreferredSize(new java.awt.Dimension(16, 16));
        extraMenu = new StampBoxExtraMenu(this);
        extraBtn.addMouseListener(extraMenu);

        // collapseAll ボタン
        collapseBtn = new JButton();
        collapseBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        collapseBtn.setContentAreaFilled(false);
        collapseBtn.setIcon(GUIConst.ICON_TREE_COLLAPSED_16);
        collapseBtn.setToolTipText("フォルダを全て閉じる");
        collapseBtn.setFocusable(false);
        collapseBtn.setPreferredSize(new java.awt.Dimension(16, 16));
        collapseBtn.addActionListener(e -> {
            StampTreePanel p = (StampTreePanel) getCurrentBox().getSelectedComponent();
            StampTree tree = p.getTree();
            tree.collapseAll();
        });

        //
        // レイアウトする
        //
        MainFrame.MainPanel mainPanel = frame.getMainPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(parentBox, BorderLayout.CENTER);

        MainFrame.CommandPanel comPanel = frame.getCommandPanel();
        comPanel.setPanelHeight(24);
        comPanel.addGlue();
        comPanel.add(toolBtn);
        comPanel.addSpace(12);
        comPanel.add(collapseBtn);
        comPanel.addSpace(4);
        comPanel.add(extraBtn);
        comPanel.addSpace(4);
        comPanel.add(lockBtn);
        comPanel.setMargin(4);

        //
        // 前回終了時のタブを選択する
        //
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String name = this.getClass().getName();
        int index = prefs.getInt(name + "_parentBox", 0);
        index = (index >= 0 && index <= (parentBox.getTabCount() - 1)) ? index : 0;
        parentBox.setSelectedIndex(index);
        index = prefs.getInt(name + "_stampBox", 0);
        index = (index >= 0 && index <= (userBox.getTabCount() - 1)) ? index : 0;

        //
        // ORCA タブが選択されていて ORCA に接続がない場合を避ける
        //
        // index = index == IInfoModel.TAB_INDEX_ORCA ? 0 : index; // 不要
        userBox.setSelectedIndex(index);

        //
        // ボタンをコントロールする
        //
        boxChanged();
    }

    /**
     * 選択されているIndexでボタンを制御する.
     */
    private void boxChanged() {
        int index = parentBox.getSelectedIndex();
        setCurrentBox((AbstractStampBox) parentBox.getComponentAt(index));
        frame.setTitle(getCurrentBox().getInfo());

        if (getCurrentBox() == userBox) {
            extraMenu.setPublishEnabled(true);
            int index2 = userBox.getSelectedIndex();
            boolean enabled = userBox.hasEditor(index2);
            toolBtn.setEnabled(enabled);

        } else {
            toolBtn.setEnabled(false);
            extraMenu.setPublishEnabled(false);
        }
    }

    /**
     * ImportしたStampBoxの選択可能を制御する.
     *
     * @param enabled 選択可能な時 true
     */
    private void enabledImportBox(boolean enabled) {
        int cnt = parentBox.getTabCount();
        for (int i = 0; i < cnt; i++) {
            if (parentBox.getComponentAt(i) != userBox) {
                parentBox.setEnabledAt(i, enabled);
            }
        }
    }

    /**
     * 選択しなおして，treeSelectionListener を fire する.（左矢印を出すため）
     *
     * @param tree StampTree
     */
    private void fireTreeSelectionListener(StampTree tree) {
        javax.swing.tree.TreePath tp = tree.getSelectionPath();
        if (tp != null) {
            tree.clearSelection();
            tree.getSelectionModel().setSelectionPath(tp);
        }
    }

    /**
     * スタンプメーカを起動する.
     */
    public void startStampMake() {
        if (editing) { return; }

        // 現在の位置と大きさを保存する
        stampBoxLoc = frame.getLocation();
        stampBoxWidth = frame.getWidth();
        stampBoxHeight = frame.getHeight();

        //
        // ASP ボックスを選択不可にする
        //
        enabledImportBox(false);

        // 現在のタブからtreeのEntityを得る
        int index = userBox.getSelectedIndex();
        StampTree tree = userBox.getStampTree(index);
        String entity = tree.getEntity();

        userBox.setHasNoEditorEnabled(false);
        List<StampTree> allTrees = userBox.getAllTrees();

        // エディタを生成する
        stampMaker = new StampMakerPanel();

        // 全ての Tree に TreeSelectionChangeListener を付ける
        allTrees.forEach(st -> st.addTreeSelectionListener(stampMaker::treeSelectionChanged));

        // StampMakerPanel で編集が終わると fire されて，ここの EditorValueListener が呼ばれる
        editorValueListener = new EditorValueListener();
        stampMaker.addPropertyChangeListener(StampMakerPanel.EDITOR_VALUE_PROP, editorValueListener);
        stampMaker.show(entity);

        fireTreeSelectionListener(tree);

        MainFrame.MainPanel mainPanel = frame.getMainPanel();
        mainPanel.add(parentBox, BorderLayout.EAST);
        mainPanel.add(stampMaker, BorderLayout.CENTER);

        // parentBox は EAST に入れ直したのでサイズ固定になる. 現在のサイズに固定する
        parentBox.setMaximumSize(parentBox.getSize());
        parentBox.setMinimumSize(parentBox.getSize());
        parentBox.setPreferredSize(parentBox.getSize());
        stampMaker.setPreferredSize(new Dimension(724, 690));

        // 前回終了時の位置とサイズを取得する
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String name = this.getClass().getName();
        int locX = prefs.getInt(name + ".stampmMaker.x", 0);
        int locY = prefs.getInt(name + ".stampmMaker.y", 0);
        int width = prefs.getInt(name + ".stampmMaker.width", 0);
        int height = prefs.getInt(name + ".stampmMaker.height", 0);

        if (width == 0 || height == 0 || Math.abs(stampBoxWidth - width) < 100) {
            // センタリングする
            frame.pack();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (screen.width - frame.getWidth()) / 2;
            int y = (screen.height - frame.getHeight()) / 2;
            frame.setLocation(x, y);
        } else {
            // parentBox と位置を合わせる
            locX = stampBoxLoc.x - (width - stampBoxWidth);
            locY = stampBoxLoc.y;
            height = stampBoxHeight;
            frame.setBounds(locX, locY, width, height);
        }

        editing = true;
        toolBtn.setToolTipText("スタンプメーカを終了します");
        extraMenu.setPublishEnabled(false);
        extraMenu.setImportEnabled(false);
    }

    /**
     * スタンプメーカを終了する.
     */
    public void stopStampMake() {
        if (!editing) { return; }

        // 現在の大きさと位置をPreferenceに保存ずる
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String name = this.getClass().getName();
        prefs.putInt(name + ".stampmMaker.x", frame.getLocation().x);
        prefs.putInt(name + ".stampmMaker.y", frame.getLocation().y);
        prefs.putInt(name + ".stampmMaker.width", frame.getWidth());
        prefs.putInt(name + ".stampmMaker.height", frame.getHeight());

        stampMaker.close();
        stampMaker.removePropertyChangeListener(StampMakerPanel.EDITOR_VALUE_PROP, editorValueListener);
        List<StampTree> allTrees = userBox.getAllTrees();

        MainFrame.MainPanel mainPanel = frame.getMainPanel();
        mainPanel.remove(stampMaker);
        mainPanel.add(parentBox, BorderLayout.CENTER);

        // サイズ固定解除（不要のようだ）
        //parentBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        //parentBox.setMinimumSize(new Dimension(0, 0));

        stampMaker = null;
        editorValueListener = null;
        userBox.setHasNoEditorEnabled(true);
        frame.setBounds(stampBoxLoc.x, stampBoxLoc.y, stampBoxWidth, stampBoxHeight);
        editing = false;
        toolBtn.setToolTipText("スタンプメーカを起動します");
        extraMenu.setPublishEnabled(true);
        extraMenu.setImportEnabled(true);

        //
        // ASP ボックスを選択可にする
        //
        enabledImportBox(true);
    }

    /**
     * スタンプパブリッシャーを起動する.
     */
    public void publishStamp() {
        StampPublisher publisher = new StampPublisher(this);
        publisher.start();
    }

    /**
     * スタンプインポーターを起動する.
     */
    public void importStamp() {
        StampImporter importer = new StampImporter(this);
        importer.start();
    }

    /**
     * 公開されているスタンプTreeをインポートする.
     *
     * @param importTree インポートする公開Tree
     */
    public void importPublishedTree(StampTreeBean importTree) {
        //
        // Asp StampBox を生成し parentBox に加える
        //
        AbstractStampBox aspBox = new AspStampBox();
        aspBox.setButtonPanelPadding(new Dimension(16, 4));
        aspBox.setContext(this);
        aspBox.setStampTreeModel(importTree);
        aspBox.buildStampBox();
        parentBox.addTab(importTree.getName(), aspBox);

        //
        // インポートリストに追加する
        //
        if (importedTreeList == null) {
            importedTreeList = new ArrayList<>(5);
        }
        importedTreeList.add(importTree.getId());
    }

    /**
     * インポートしている公開Treeを削除する.
     *
     * @param removeId 削除する公開TreeのId
     */
    public void removeImportedTree(long removeId) {

        if (importedTreeList != null) {
            for (int i = 0; i < importedTreeList.size(); i++) {
                Long id = importedTreeList.get(i);
                if (id == removeId) {
                    parentBox.removeTabAt(i + IMPORT_TREE_OFFSET);
                    importedTreeList.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * プログラムを終了する.
     */
    @Override
    public void stop() {
        frame.setVisible(false);
        frame.dispose();
    }

    /**
     * フレームを前面に出す.
     */
    @Override
    public void enter() {
        if (frame != null) {
            frame.toFront();
        }
    }

    @Override
    public Callable<Boolean> getStoppingTask() {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String name = (StampBoxPlugin.this).getClass().getName();

        // StampMeker modeで終了した場合，
        // 次回起動時に通常モードの位置と大きさで表示するため
        if (editing) {
            prefs.putInt(name + "_x", stampBoxLoc.x);
            prefs.putInt(name + "_y", stampBoxLoc.y);
            prefs.putInt(name + "_width", stampBoxWidth);
            prefs.putInt(name + "_height", stampBoxHeight);
        }

        // 終了時のタブ選択インデックスを保存する
        prefs.putInt(name + "_parentBox", parentBox.getSelectedIndex());
        prefs.putInt(name + "_stampBox", userBox.getSelectedIndex());

        //
        // User Tree のみを保存する
        //
        List<StampTree> list = userBox.getAllTrees();
        if (list == null || list.isEmpty()) { return null; }

        //
        // ORCA セットは除く
        //
        for (StampTree tree : list) {
            if (tree.getTreeInfo().getEntity().equals(IInfoModel.ENTITY_ORCA)) {
                list.remove(tree);
                logger.debug("ORCAセットを除きました");
                break;
            }
        }

        // StampTree を表す XML データを生成する
        DefaultStampTreeXmlBuilder builder = new DefaultStampTreeXmlBuilder();
        StampTreeXmlDirector director = new StampTreeXmlDirector(builder);
        String treeXml = director.build(list);

        // 個人用のStampTreeModelにXMLをセットする
        final PersonalTreeModel treeM = (PersonalTreeModel) userBox.getStampTreeModel();
        treeM.setTreeXml(treeXml);

        // StampTree を保存する Callable Object を返す
        return () -> {
            StampDelegater stampDel = new StampDelegater();
            stampDel.putTree(treeM);
            return stampDel.isNoError();
        };
    }

    /**
     * 引数のカテゴリに対応するTreeを返す.
     *
     * @param entity Treeのカテゴリ
     * @return カテゴリにマッチするStampTree
     */
    public StampTree getStampTree(String entity) {
        return getCurrentBox().getStampTree(entity);
    }

    public StampTree getStampTreeFromUserBox(String entity) {
        return getUserStampBox().getStampTree(entity);
    }

    /**
     * スタンプボックスに含まれる全treeのTreeInfoリストを返す.
     *
     * @return TreeInfoのリスト
     */
    public List<TreeInfo> getAllTress() {
        return getCurrentBox().getAllTreeInfos();
    }

    /**
     * スタンプボックスに含まれる全treeを返す.
     *
     * @return StampTreeのリスト
     */
    public List<StampTree> getAllTrees() {
        return getCurrentBox().getAllTrees();
    }

    /**
     * スタンプボックスに含まれる全treeを返す.
     *
     * @return StampTreeのリスト
     */
    public List<StampTree> getAllAllPTrees() {
        int cnt = parentBox.getTabCount();
        List<StampTree> ret = new ArrayList<>();

        for (int i = 0; i < cnt; i++) {
            AbstractStampBox stb = (AbstractStampBox) parentBox.getComponentAt(i);
            ret.addAll(stb.getAllPTrees());
        }

        return ret;
    }

    /**
     * Currentボックスの P 関連Staptreeを返す.
     *
     * @return StampTreeのリスト
     */
    public List<StampTree> getAllPTrees() {
        AbstractStampBox stb = getCurrentBox();
        return stb.getAllPTrees();
    }

    /**
     * 引数のエンティティ配下にある全てのスタンプを返す.
     * これはメニュー等で使用する.
     *
     * @param entity Treeのエンティティ
     * @return 全てのスタンプのリスト
     */
    public List<ModuleInfoBean> getAllStamps(String entity) {
        return getCurrentBox().getAllStamps(entity);
    }

    /**
     * StampTree 編集の undo.
     * StampMaker が起動しているときは StampMaker の undo.
     */
    public void undo() {
        if (Objects.nonNull(stampMaker)
            && stampMaker.getEditor() instanceof StampEditor
            && ((StampEditor) stampMaker.getEditor()).getTablePanel().canUndo()) {
                ((StampEditor) stampMaker.getEditor()).getTablePanel().undo();
        } else {
            int index = userBox.getSelectedIndex();
            StampTree tree = userBox.getStampTree(index);
            ((StampTreeModel) tree.getModel()).undo();
        }
    }

    /**
     * StampTree 編集の redo.
     * StampMaker が起動しているときは StampMaker の redo.
     */
    public void redo() {
        if (Objects.nonNull(stampMaker)
            && stampMaker.getEditor() instanceof StampEditor
            && ((StampEditor) stampMaker.getEditor()).getTablePanel().canRedo()) {
                ((StampEditor) stampMaker.getEditor()).getTablePanel().redo();
        } else {
            int index = userBox.getSelectedIndex();
            StampTree tree = userBox.getStampTree(index);
            ((StampTreeModel) tree.getModel()).redo();
        }
    }

    /**
     * User用StampBoxのTab切り替えリスナクラス.
     */
    private class TabChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (!editing) {
                // スタンプメーカ起動中でない時
                // テキストスタンプタブが選択されたらスタンプメーカボタンを disabledにする
                // ORCA セットタブの場合を処理する
                int index = userBox.getSelectedIndex();
                StampTree tree = userBox.getStampTree(index);
                tree.enter();
                boolean enabled = userBox.hasEditor(index);
                toolBtn.setEnabled(enabled);

            } else {
                // スタンプメーカ起動中の時
                // 選択されたタブに対応するエディタを表示する
                int index = userBox.getSelectedIndex();
                StampTree tree = userBox.getStampTree(index);
                if (stampMaker != null && userBox.hasEditor(index)) {
                    stampMaker.show(tree.getEntity());
                    fireTreeSelectionListener(tree);
                }
            }
        }
    }

    /**
     * ParentBox の TabChangeListenerクラス.
     */
    private class BoxChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            boxChanged();
        }
    }

    /**
     * エディタで作成したスタンプをStampTreeに加える.
     */
    private class EditorValueListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            Object obj = e.getNewValue();

            if (obj instanceof ModuleModel) {
                // 傷病名以外の場合
                ModuleModel stamp = (ModuleModel) obj;
                String entity = stamp.getModuleInfo().getEntity();
                final StampTree tree = userBox.getStampTree(entity);

                StampTreeNode target = tree.getSelectedNode();
                if (target == null) {
                    // 選択がない場合は，tree の最後に追加
                    tree.addStamp(stamp, null);

                } else {
                    if (target.isLeaf()) {
                        // Leaf の場合，上書きするか確認
                        String targetName = target.getStampInfo().getStampName();
                        String sourceName = stamp.getModuleInfo().getStampName();
                        if (targetName.equals(sourceName)) {
                            // 名前が同じなら置き換えるかどうか確認
                            int confirm = JSheet.showConfirmDialog(frame,
                                    "「" + targetName + "」を上書きしますか？", "上書き確認",
                                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (confirm == JOptionPane.OK_OPTION) {
                                ((StampTreeNodeTransferHandler) tree.getTransferHandler()).setPosition(StampTreeNodeTransferHandler.Insert.AFTER);
                                tree.replaceStamp(stamp, target);
                            }
                        } else {
                            // 名前が違えば選択された項目の下に入れる
                            ((StampTreeNodeTransferHandler) tree.getTransferHandler()).setPosition(StampTreeNodeTransferHandler.Insert.AFTER);
                            tree.addStamp(stamp, target);
                        }
                    } else {
                        // Folder ならその中に入れる
                        ((StampTreeNodeTransferHandler) tree.getTransferHandler()).setPosition(StampTreeNodeTransferHandler.Insert.INTO_FOLDER);
                        tree.addStamp(stamp, target);
                    }
                }
                // 終わったら tree にフォーカスを取る
                Focuser.requestFocus(tree);

            } else if (obj instanceof ArrayList) {
                // 傷病名の場合
                StampTree tree = getStampTree(IInfoModel.ENTITY_DIAGNOSIS);
                tree.addDiagnosis((List<RegisteredDiagnosisModel>) obj);
            }
        }
    }
}
