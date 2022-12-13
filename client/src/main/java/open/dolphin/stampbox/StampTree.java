package open.dolphin.stampbox;

import open.dolphin.client.ClientContext;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.dnd.StampTreeNodeTransferHandler;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.GUIDGenerator;
import open.dolphin.helper.Task;
import open.dolphin.infomodel.*;
import open.dolphin.project.Project;
import open.dolphin.ui.PNSTreeCellEditor;
import open.dolphin.ui.sheet.JSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * StampTree.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class StampTree extends JTree implements TreeModelListener {
    private static final long serialVersionUID = -4651151848166376384L;

    private final Logger logger = LoggerFactory.getLogger(StampTree.class);
    public static final String SELECTED_NODE_PROP = "selectedNodeProp";
    private static final int TOOLTIP_LENGTH = 300;
    private static final String NEW_FOLDER_NAME = "新規フォルダ";
    private static final String STAMP_SAVE_TASK_NAME = "スタンプ保存";

    // StampTreeModel
    private StampTreeModel treeModel;

    // 個人用Treeかどうかのフラグ
    private boolean isUserTree;

    // StampBox
    private StampBoxPlugin stampBox;

    // 飛ばない getPreferredSize 用変数 by pns
    private Dimension oldPreferredSize = new Dimension(0, 0);

    /**
     * StampTreeオブジェクトを生成する.
     *
     * @param model StampTreeModel
     */
    public StampTree(TreeModel model) {
        super(model);
        treeModel = (StampTreeModel) model;
        init();
    }

    private void init() {
        putClientProperty("JTree.lineStyle", "Angled"); // 水平及び垂直線を使用する

        // setEditable(false); // ノード名を編集不可にする
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); // Single Selection// にする

        // ロックをかけたときに inset のないボーダーを表示したら，選択したときと，focus がうつった時にボーダーを消してしまう
        // inset のないボーダについては StampBoxPlugin を inset で検索
        addTreeSelectionListener(e -> stampBox.getCurrentBox().repaint());

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                stampBox.getCurrentBox().repaint();
            }
        });

        addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                stampBox.getCurrentBox().repaint();
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                stampBox.getCurrentBox().repaint();
            }
        });

        setRootVisible(false);

        putClientProperty("Quaqua.Tree.style", "striped");
        setDragEnabled(true);

        // JTree のデフォルトの DropTarget を置き換える
        DropTarget dt = new DropTarget(this, new StampTreeDropTargetListener());
        dt.setActive(true);

        // デフォルトのセルレンダラーを置き換える
        DefaultTreeCellRenderer renderer = new StampTreeRenderer();
        setCellRenderer(renderer);

        // TreeCellEditor セット
        setEditable(true);
        setCellEditor(new PNSTreeCellEditor(this, renderer));

        // Listens TreeModelEvent
        treeModel.addTreeModelListener(this);

        // Enable ToolTips
        enableToolTips(true);

        // ENTER で cell editing
        getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "start-editing");
        getActionMap().put("start-editing", new ProxyAction(() -> startEditingAtPath(getSelectionPath())));
    }

    /**
     * tooltip を html で出す.
     *
     * @param event mouse event
     * @return tool tip
     */
    @Override
    public String getToolTipText(MouseEvent event) {
        StringBuilder tip = new StringBuilder();

        if (event != null) {
            Point p = event.getPoint();
            int selRow = getRowForLocation(p.x, p.y);

            if (selRow != -1) {
                TreePath path = getPathForRow(selRow);
                StampTreeNode lastPath = (StampTreeNode) path.getLastPathComponent();
                Object info = lastPath.getUserObject();

                if (info instanceof ModuleInfoBean) {
                    tip.append("<html>");
                    String str = ((ModuleInfoBean) info).getStampMemo();
                    if (str != null) {
                        tip.append(str.replaceAll(",", "<br>"));
                        tip.append("</html>");
                    }

                } else {
                    tip.append(info.toString());
                }
            }
        }
        // No tip from the renderer get our own tip
        if (tip.toString().equals("null")) {
            tip.append(getToolTipText());
        }

        return (tip.toString().equals("")) ? null : tip.toString();
    }

    /**
     * このStampTreeのTreeInfoを返す.
     *
     * @return Tree情報
     */
    public TreeInfo getTreeInfo() {
        StampTreeNode node = (StampTreeNode) this.getModel().getRoot();
        return (TreeInfo) node.getUserObject();
    }

    /**
     * このStampTreeのエンティティを返す.
     *
     * @return エンティティ
     */
    public String getEntity() {
        return getTreeInfo().getEntity();
    }

    /**
     * このStampTreeの名前を返す.
     *
     * @return 名前
     */
    public String getTreeName() {
        return getTreeInfo().getName();
    }

    /**
     * UserTreeかどうかを返す.
     *
     * @return UserTreeの時true
     */
    public boolean isUserTree() {
        return isUserTree;
    }

    /**
     * UserTreeかどうかを設定する.
     *
     * @param userTree UserTreeの時true
     */
    public void setUserTree(boolean userTree) {
        this.isUserTree = userTree;
        this.setEditable(userTree);
    }

    /**
     * Enable or disable tooltip.
     *
     * @param state state
     */
    public void enableToolTips(boolean state) {

        ToolTipManager mgr = ToolTipManager.sharedInstance();
        if (state) {
            // Enable tooltips
            mgr.registerComponent(this);

        } else {
            mgr.unregisterComponent(this);
        }
    }

    /**
     * Return StampBox reference.
     *
     * @return stamp box
     */
    public StampBoxPlugin getStampBox() {
        return stampBox;
    }

    /**
     * Set StampBox reference.
     *
     * @param stampBox stamp box
     */
    public void setStampBox(StampBoxPlugin stampBox) {
        this.stampBox = stampBox;
    }

    /**
     * 選択されているノードを返す.
     *
     * @return StampTreeNode
     */
    public StampTreeNode getSelectedNode() {
        return (StampTreeNode) this.getLastSelectedPathComponent();
    }

    /**
     * 引数のポイント位置のノードを返す.
     *
     * @param p Point
     * @return StampTreeNode
     */
    public StampTreeNode getNode(Point p) {
        TreePath path = this.getPathForLocation(p.x, p.y);
        return (path != null)
                ? (StampTreeNode) path.getLastPathComponent()
                : null;
    }

    /**
     * この StampTree に enter() する.
     */
    public void enter() {
        //experimental---
        collapseAll();
        //this.scrollRowToVisible(0);
    }

    /**
     * KartePaneから drop されたスタンプをツリーに加える.
     *
     * @param droppedStamp dropped ModuleModel
     * @param selected selected StampTreeNode
     * @return true if succeeded
     */
    public boolean addStamp(ModuleModel droppedStamp, StampTreeNode selected) {
        return addStamp(droppedStamp, selected, false);
    }

    /**
     * KartePaneから drop されたスタンプをツリーのスタンプと置き換える.
     *
     * @param droppedStamp dropped ModuleModel
     * @param selected selected StampTreeNode
     * @return true if succeeded
     */
    public boolean replaceStamp(ModuleModel droppedStamp, StampTreeNode selected) {
        return addStamp(droppedStamp, selected, true);
    }

    /**
     * KartePaneから drop されたスタンプをツリーに加える.
     *
     * @param droppedStamp dropped ModuleModel
     * @param selected selected StampTreeNode
     * @param toReplace true if to replace
     * @return true if succeeded
     */
    public boolean addStamp(ModuleModel droppedStamp, final StampTreeNode selected, final boolean toReplace) {
        if (droppedStamp == null) { return false; }

        // Drop された Stamp の ModuleInfoを得る
        ModuleInfoBean droppedInfo = droppedStamp.getModuleInfo();

        // データベースへ droppedStamp のデータモデルを保存する
        final StampModel stampModel = new StampModel();
        final String stampId = GUIDGenerator.generate(stampModel);    // stampId
        stampModel.setId(stampId);
        stampModel.setUserId(Project.getUserModel().getId());   // userId
        stampModel.setEntity(droppedInfo.getEntity());          // entity
        stampModel.setStamp(droppedStamp.getModel());           // model

        // Tree に加える新しい StampInfo を生成する
        final ModuleInfoBean info = new ModuleInfoBean();
        info.setStampName(droppedInfo.getStampName());      // オリジナル名
        info.setEntity(droppedInfo.getEntity());            // Entity
        info.setStampRole(droppedInfo.getStampRole());      // Role
        info.setStampMemo(constractToolTip(droppedStamp));  // Tooltip
        info.setStampId(stampId);                           // StampID

        // Delegator を生成する
        final StampDelegater sdl = new StampDelegater();

        String message = "スタンプ保存";
        String note = info.getStampName() + "を保存しています...";
        Component c = SwingUtilities.getWindowAncestor(this);

        Task<String> task = new Task<String>(c, message, note, 60 * 1000) {

            @Override
            protected String doInBackground() {
                logger.debug("addStamp doInBackground");
                return sdl.putStamp(stampModel); // ret は StampId (UUID)
            }

            @Override
            protected void succeeded(String result) {
                logger.debug("addStamp succeeded");
                if (sdl.isNoError() && result.equals(stampId)) {
                    // replace 対応
                    addInfoToTree(info, selected, toReplace);
                } else {
                    logger.warn(sdl.getErrorMessage());
                }
            }
        };
        //task.setMillisToPopup(200);
        task.execute();

        return true;
    }

    /**
     * StampTree に新しいノードを加える.
     *
     * @param info     追加するノードの情報
     * @param selected カーソルの下にあるノード(Drop 位置のノード）
     */
    public void addInfoToTree(ModuleInfoBean info, StampTreeNode selected) {
        addInfoToTree(info, selected, false);
    }

    /**
     * StampTree に新しいノードを加える.
     *
     * @param info     追加するノードの情報
     * @param selected カーソルの下にあるノード(Drop 位置のノード）
     * @param toReplace true to replace
     */
    public void addInfoToTree(ModuleInfoBean info, StampTreeNode selected, boolean toReplace) {
        StampTreeNodeTransferHandler handler = (StampTreeNodeTransferHandler) this.getTransferHandler();
        StampTreeNodeTransferHandler.Insert insertPosition = handler.getInsertPosition();

        // StampInfo から新しい StampTreeNode を生成する
        StampTreeNode node = new StampTreeNode(info);

        // Drop 位置のノードによって追加する位置を決める
        if (selected != null) {
            StampTreeNode newParent = (StampTreeNode) selected.getParent();
            int index = newParent.getIndex(selected);

            switch (insertPosition) {
                case BEFORE:
                    treeModel.undoableInsertNodeInto(node, newParent, index);
                    break;
                case AFTER:
                    treeModel.undoableInsertNodeInto(node, newParent, index + 1);
                    break;
                default: // INTO_FOLDER
                    treeModel.undoableInsertNodeInto(node, selected, selected.getChildCount());
            }
            // replace
            if (toReplace) {
                treeModel.undoableRemoveNodeFromParent(getSelectedNode());
            }

            // 追加したノードを選択する
            TreeNode[] path = treeModel.getPathToRoot(node);
            this.setSelectionPath(new TreePath(path));

        } else {

            // Drop 位置のノードが null でコールされるケースがある
            // 1. このtreeのスタンプではない場合，該当するTreeのルートに加える
            // 2. パス Tree など，まだノードを持たない初期状態の時
            // Stamp ボックスから entity に対応する tree を得る
            // stamp の名前を編集しようとして，テキストを選択したまま操作ミスでスタンプ箱にドロップしてしまうと
            // テキストスタンプとしてスタンプ箱が認識してしまう. TODO

            StampTree another = stampBox.getStampTree(info.getEntity());
            boolean myTree = (another == this);
            final String treeName = another.getTreeName();
            StampTreeNode root = (StampTreeNode) treeModel.getRoot();
            root.add(node);
            treeModel.reload(root);

            // 追加したノードを選択する
            TreeNode[] path = treeModel.getPathToRoot(node);
            this.setSelectionPath(new TreePath(path));

            // メッセージを表示する
            if (!myTree) {
                SwingUtilities.invokeLater(() -> {
                    String buf = "スタンプは個人用の " + treeName + " に保存しました。";
                    JSheet.showMessageDialog(
                            StampTree.this,
                            buf,
                            ClientContext.getFrameTitle(STAMP_SAVE_TASK_NAME),
                            JOptionPane.INFORMATION_MESSAGE);
                });
            }
            logger.info("dropped Entity= " + info.getEntity() + "  this entity = " + this.getEntity());
        }
    }

    /**
     * Diagnosis Table から Drag & Drop された RegisteredDiagnosis をスタンプ化する.
     *
     * @param rd RegisteredDiagnosis
     * @param selected selected StampTreeNode
     * @return true if succeeded
     */
    public boolean addDiagnosis(RegisteredDiagnosisModel rd, final StampTreeNode selected) {
        if (rd == null) { return false; }

        RegisteredDiagnosisModel add = new RegisteredDiagnosisModel();
        add.setDiagnosis(rd.getDiagnosis());
        add.setDiagnosisCode(rd.getDiagnosisCode());
        add.setDiagnosisCodeSystem(rd.getDiagnosisCodeSystem());

        ModuleModel module = new ModuleModel();
        module.setModel(add);

        // データベースへ Stamp のデータモデルを永続化する
        final StampModel addStamp = new StampModel();
        final String stampId = GUIDGenerator.generate(addStamp);
        addStamp.setId(stampId);
        addStamp.setUserId(Project.getUserModel().getId());
        addStamp.setEntity(IInfoModel.ENTITY_DIAGNOSIS);
        addStamp.setStamp(module.getModel());

        // Tree に加える 新しい StampInfo を生成する
        final ModuleInfoBean info = new ModuleInfoBean();
        info.setStampId(stampId);                       // Stamp ID
        info.setStampName(add.getDiagnosis());          // 傷病名
        info.setEntity(IInfoModel.ENTITY_DIAGNOSIS);    // カテゴリ
        info.setStampRole(IInfoModel.ENTITY_DIAGNOSIS); // Role

        StringBuilder buf = new StringBuilder();
        buf.append(add.getDiagnosis());
        String cd = add.getDiagnosisCode();
        if (cd != null) {
            buf.append("(");
            buf.append(cd);
            buf.append(")"); // Tooltip
        }
        info.setStampMemo(buf.toString());

        final StampDelegater sdl = new StampDelegater();

        String message = "スタンプ保存";
        String note = info.getStampName() + "を保存しています...";
        Component c = SwingUtilities.getWindowAncestor(this);

        Task<String> task = new Task<String>(c, message, note, 60 * 1000) {

            @Override
            protected String doInBackground() {
                logger.debug("addDiagnosis doInBackground");
                return sdl.putStamp(addStamp);
            }

            @Override
            protected void succeeded(String result) {
                logger.debug("addDiagnosis succeeded");
                if (sdl.isNoError() && result.equals(stampId)) {
                    addInfoToTree(info, selected);
                } else {
                    logger.warn(sdl.getErrorMessage());
                }
            }
        };
        //task.setMillisToPopup(200);
        task.execute();

        return true;
    }

    /**
     * エディタで生成した病名リストを登録する.
     *
     * @param list list of RegisteredDiagnosis
     */
    public void addDiagnosis(List<RegisteredDiagnosisModel> list) {
        if (list == null || list.isEmpty()) { return; }

        final List<StampModel> stampList = new ArrayList<>();
        final List<ModuleInfoBean> infoList = new ArrayList<>();

        list.stream().map(rd -> {
            RegisteredDiagnosisModel add = new RegisteredDiagnosisModel();
            add.setDiagnosis(rd.getDiagnosis());
            add.setDiagnosisCode(rd.getDiagnosisCode());
            add.setDiagnosisCodeSystem(rd.getDiagnosisCodeSystem());
            return add;
        }).map(add -> {
            ModuleModel module = new ModuleModel();
            module.setModel(add);
            // データベースへ Stamp のデータモデルを永続化する
            StampModel stampModel = new StampModel();
            String stampId = GUIDGenerator.generate(stampModel);
            stampModel.setId(stampId);
            stampModel.setUserId(Project.getUserModel().getId());
            stampModel.setEntity(IInfoModel.ENTITY_DIAGNOSIS);
            stampModel.setStamp(module.getModel());
            stampList.add(stampModel);
            // Tree に加える 新しい StampInfo を生成する
            ModuleInfoBean info = new ModuleInfoBean();
            info.setStampId(stampId);                       // Stamp ID
            info.setStampName(add.getDiagnosis());          // 傷病名
            info.setEntity(IInfoModel.ENTITY_DIAGNOSIS);    // カテゴリ
            info.setStampRole(IInfoModel.ENTITY_DIAGNOSIS); // Role
            StringBuilder buf = new StringBuilder();
            buf.append(add.getDiagnosis());
            String cd = add.getDiagnosisCode();
            if (cd != null) {
                buf.append("(");
                buf.append(cd);
                buf.append(")"); // Tooltip
            }
            info.setStampMemo(buf.toString());
            return info;
        }).forEach(infoList::add);

        final StampDelegater sdl = new StampDelegater();

        String message = "スタンプ保存";
        String note = "病名スタンプを保存しています...";
        Component c = SwingUtilities.getWindowAncestor(this);

        Task<List<String>> task = new Task<List<String>>(c, message, note, 60 * 1000) {

            @Override
            protected List<String> doInBackground() {
                logger.debug("addDiagnosis doInBackground");
                return sdl.putStamp(stampList);
            }

            @Override
            protected void succeeded(List<String> result) {
                logger.debug("addDiagnosis succeeded");
                if (sdl.isNoError()) {
                    infoList.forEach(info -> {
                        // 選択されているとき
                        StampTreeNode target = getSelectedNode();
                        if (target != null) {
                            if (target.isLeaf()) {
                                // 葉の時は後ろに挿入する
                                ((StampTreeNodeTransferHandler) getTransferHandler()).setPosition(StampTreeNodeTransferHandler.Insert.AFTER);
                            } else {
                                // フォルダの時は中に挿入する
                                ((StampTreeNodeTransferHandler) getTransferHandler()).setPosition(StampTreeNodeTransferHandler.Insert.INTO_FOLDER);
                            }
                        }
                        addInfoToTree(info, target);
                    });
                } else {
                    logger.warn(sdl.getErrorMessage());
                }
            }
        };
        //task.setMillisToPopup(200);
        task.execute();
    }

    /**
     * テキストスタンプを追加する.
     *
     * @param text text
     * @param selected selected StampTreeNode
     * @return true if succeeded
     */
    public boolean addTextStamp(String text, final StampTreeNode selected) {
        if (text == null || text.length() == 0) { return false; }

        TextStampModel module = new TextStampModel();
        module.setText(text);

        // データベースへ Stamp のデータモデルを永続化する
        final StampModel addStamp = new StampModel();
        final String stampId = GUIDGenerator.generate(addStamp);
        addStamp.setId(stampId);
        addStamp.setUserId(Project.getUserModel().getId());
        addStamp.setEntity(IInfoModel.ENTITY_TEXT);
        addStamp.setStamp(module);

        // Tree へ加える 新しい StampInfo を生成する
        final ModuleInfoBean info = new ModuleInfoBean();
        int len = Math.min(text.length(), 16);
        String name = text.substring(0, len);
        len = name.indexOf("\n");
        if (len > 0) {
            name = name.substring(0, len);
        }
        info.setStampName(name);                    //
        info.setEntity(IInfoModel.ENTITY_TEXT);     // カテゴリ
        info.setStampRole(IInfoModel.ENTITY_TEXT);  // Role
        info.setStampMemo(text);                    // Tooltip
        info.setStampId(stampId);                   // Stamp ID

        final StampDelegater sdl = new StampDelegater();

        String message = "スタンプ保存";
        String note = info.getStampName() + "を保存しています...";
        Component c = SwingUtilities.getWindowAncestor(this);

        Task<String> task = new Task<String>(c, message, note, 60 * 1000) {

            @Override
            protected String doInBackground() {
                logger.debug("addTextStamp doInBackground");
                return sdl.putStamp(addStamp);
            }

            @Override
            protected void succeeded(String result) {
                logger.debug("addTextStamp succeeded");
                if (sdl.isNoError() && result.equals(stampId)) {
                    addInfoToTree(info, selected);
                } else {
                    logger.warn(sdl.getErrorMessage());
                }
            }
        };
        //task.setMillisToPopup(200);
        task.execute();

        return true;
    }

    /**
     * スタンプの情報を表示するための文字列を生成する.
     *
     * @param stamp 情報を生成するスタンプ
     * @return スタンプの情報文字列
     */
    protected String constractToolTip(ModuleModel stamp) {

        try (BufferedReader reader = new BufferedReader(new StringReader(stamp.getModel().toString()))) {

            StringBuilder buf = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buf.append(line);
                if (buf.length() < TOOLTIP_LENGTH) {
                    buf.append(",");
                } else {
                    break;
                }
            }
            if (buf.length() > TOOLTIP_LENGTH) {
                buf.setLength(TOOLTIP_LENGTH);
                buf.append("...");
            }
            return buf.toString();

        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    /**
     * スタンプタスク共通の warning ダイアログを表示する.
     *
     * @param message 　エラーメッセージ
     */
    private void warning(String message) {
        String title = ClientContext.getString("stamptree.title");
        JOptionPane.showMessageDialog(
                StampTree.this,
                message,
                ClientContext.getFrameTitle(title),
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * ノードの名前を変更する.
     */
    public void renameNode() {
        if (!isUserTree()) { return; }

        // Root へのパスを取得する
        StampTreeNode node = getSelectedNode();
        if (node == null) { return; }

        TreeNode[] nodes = node.getPath();
        TreePath path = new TreePath(nodes);

        // 編集を開始する
        // this.setEditable(true);
        this.startEditingAtPath(path);
        // this.setEditable (false); は TreeModelListener で行う
    }

    /**
     * ノードを削除する.
     */
    public void deleteNode(StampTreeNode n) {
        logger.debug("stampTree deleteNode");

        if (!isUserTree()) { return; }

        // 削除するノードを取得する
        // 右クリックで選択されている
        final StampTreeNode theNode = getSelectedNode();
        if (theNode == null) { return; }

        // このノードをルートにするサブツリーを前順走査する列挙を生成して返します.
        // 列挙の nextElement() メソッドによって返される最初のノードは，この削除するノードです.
        Enumeration<TreeNode> e = theNode.preorderEnumeration();

        // このリストのなかに削除するノードとその子を含める
        final List<String> deleteList = new ArrayList<>();

        // エディタから発行があるかどうかのフラグ
        boolean hasEditor = false;

        // 列挙する
        while (e.hasMoreElements()) {
            logger.debug("stampTree deleteNode e.hasMoreElements()");
            StampTreeNode node = (StampTreeNode) e.nextElement();

            if (node.isLeaf()) {

                ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
                String stampId = info.getStampId();

                // エディタから発行がある場合は中止する
                if (info.getStampName().equals("エディタから発行...") && (!info.isSerialized())) {
                    hasEditor = true;
                    break;
                }

                // IDが付いているもののみを加える
                if (stampId != null) {
                    deleteList.add(stampId);
                    logger.debug("added " + info.getStampName());
                }
            }
        }

        // エディタから発行が有った場合はダイアログを表示し
        // リターンする
        if (hasEditor) {
            String msg0 = "エディタから発行は消去できません。フォルダに含まれている";
            String msg1 = "場合は Drag & Drop で移動後、再度実行してください。";
            String taskTitle = ClientContext.getString("stamptree.title");
            JOptionPane.showMessageDialog(
                    null,
                    new Object[]{msg0, msg1},
                    ClientContext.getFrameTitle(taskTitle),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // 削除するフォルダが空の場合は削除してリターンする
        // リストのサイズがゼロかつ theNode が葉でない時
        if (deleteList.isEmpty() && (!theNode.isLeaf())) {
            treeModel.undoableRemoveNodeFromParent(theNode);
            return;
        }

        // データベースのスタンプを削除するデリゲータを生成する
        final StampDelegater sdl = new StampDelegater();

        String message = "スタンプ削除";
        String note = "スタンプを削除しています...";
        Component c = SwingUtilities.getWindowAncestor(this);

        Task<Void> task = new Task<Void>(c, message, note, 60 * 1000) {

            @Override
            protected Void doInBackground() {
                logger.debug("deleteNode doInBackground");
                sdl.removeStamp(deleteList);
                return null;
            }

            @Override
            protected void succeeded(Void result) {
                logger.debug("deleteNode succeeded");
                treeModel.undoableRemoveNodeFromParent(theNode);
            }
        };
        //task.setMillisToPopup(200);
        task.execute();
    }

    /**
     * 新規のフォルダを追加する.
     */
    public void createNewFolder() {
        if (!isUserTree()) { return; }

        // フォルダノードを生成する
        StampTreeNode folder = new StampTreeNode(NEW_FOLDER_NAME);

        // 生成位置となる選択されたノードを得る
        StampTreeNode selected = getSelectedNode();
        TreePath selectedPath = getSelectionModel().getSelectionPath();

        if (selected == null) {
            // 選択されていなかったら，ルートの一番下に挿入
            treeModel.undoableInsertNodeInto(folder, (MutableTreeNode) treeModel.getRoot(), ((TreeNode) treeModel.getRoot()).getChildCount());

        } else if (selected.isLeaf()) {
            // 選択位置のノードが葉の場合，その前に挿入する
            StampTreeNode newParent = (StampTreeNode) selected.getParent();
            int index = newParent.getIndex(selected);
            treeModel.undoableInsertNodeInto(folder, newParent, index);

        } else {
            // 選択位置のノードが子を持つ時，最後の子として挿入する
            treeModel.undoableInsertNodeInto(folder, selected, selected.getChildCount());
            expandPath(selectedPath);
        }
        //TreePath parentPath = new TreePath(parent.getPath());
        //this.expandPath(parentPath);
    }

    /**
     * Tree をすべて展開する.
     */
    public void expandAll() {
        int row = 0;
        while (row < getRowCount()) { expandRow(row++); }
    }

    /**
     * Tree をすべて閉じる.
     */
    public void collapseAll() {
        oldPreferredSize.height = 0;
        int row = getRowCount();
        while (row > 0) { collapseRow(--row); }
    }

    /**
     * 飛ばない StampTree.
     *
     * @return new preferred size
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension newPreferredSize = super.getPreferredSize();

        if (newPreferredSize.height < oldPreferredSize.height) {
            Rectangle r = ((JViewport) getParent()).getViewRect();
            int offsetSize = r.y + r.height - newPreferredSize.height;
            offsetSize = Math.max(offsetSize, 0);
            newPreferredSize.height += offsetSize;
        }

        oldPreferredSize = newPreferredSize;
        return newPreferredSize;
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        //logger.info("tree node changed: " + e);
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        //logger.info("tree node inserted: " + e);
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        //logger.info("tree node removed: " + e);
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        //logger.info("tree structure changed: " + e);
    }
}
