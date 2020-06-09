package open.dolphin.stampbox;

import open.dolphin.client.BlockGlass;
import open.dolphin.client.GUIConst;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.dnd.StampTreeNodeTransferHandler;
import open.dolphin.helper.HexBytesTool;
import open.dolphin.helper.MenuActionManager;
import open.dolphin.helper.MenuActionManager.MenuAction;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.StampModel;
import open.dolphin.project.Project;
import open.dolphin.util.ModelUtils;
import org.jdom2.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * StampBox の特別(gear)メニュー.
 *
 * @author pns
 * @author masuda-sensei
 */
public class StampBoxExtraMenu extends MouseAdapter {

    private final StampBoxPlugin context;
    private final BlockGlass blockGlass;
    private JPopupMenu popup;
    // @MenuAction で定義された action の actionMap， key は method 名
    private ActionMap actionMap;

    public StampBoxExtraMenu(StampBoxPlugin ctx) {
        super();
        context = ctx;
        blockGlass = new BlockGlass();
        context.getFrame().setGlassPane(blockGlass);
        blockGlass.setSize(context.getFrame().getSize());
        buildPopupMenu();
    }

    @MenuAction
    public void collapseAll() {
        StampTreePanel p = (StampTreePanel) context.getCurrentBox().getSelectedComponent();
        StampTree tree = p.getTree();
        tree.collapseAll();
    }

    @MenuAction
    public void expandAll() {
        StampTreePanel p = (StampTreePanel) context.getCurrentBox().getSelectedComponent();
        StampTree tree = p.getTree();
        tree.expandAll();
    }

    @MenuAction
    public void publishStamp() {
        context.publishStamp();
    }

    @MenuAction
    public void importStamp() {
        context.importStamp();
    }

    /**
     * ポップアップメニューを作る.
     */
    private void buildPopupMenu() {
        MenuActionManager m = new MenuActionManager(this);
        actionMap = m.getActionMap();

        popup = new JPopupMenu();
        popup.add(m.getMenuItem("collapseAll", "フォルダを全て閉じる", GUIConst.ICON_TREE_COLLAPSED_16));
        popup.add(m.getMenuItem("expandAll", "フォルダを全て展開する", GUIConst.ICON_TREE_EXPANDED_16));
        popup.addSeparator();
        //popup.add(m.getMenuItem("exportUserStampBox", "スタンプをファイルに保存する...", GUIConst.ICON_DISK_16));
        popup.add(m.getMenuItem("exportUserStampBox", "スタンプをファイルに保存する...", GUIConst.ICON_SAVE_16));
        popup.add(m.getMenuItem("importUserStampBox", "スタンプをファイルから読み込む...", GUIConst.ICON_EMPTY_16));
        popup.addSeparator();
        popup.add(m.getMenuItem("publishStamp", "スタンプ公開...", GUIConst.ICON_EMPTY_16));
        popup.add(m.getMenuItem("importStamp", "公開スタンプのインポート...", GUIConst.ICON_EMPTY_16));
    }

    /**
     * publish メニューの enable/disable.
     *
     * @param b enable
     */
    public void setPublishEnabled(boolean b) {
        actionMap.get("publishStamp").setEnabled(b);
    }

    /**
     * import メニューの enable/disable.
     *
     * @param b enable
     */
    public void setImportEnabled(boolean b) {
        actionMap.get("importStamp").setEnabled(b);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // shift + click or double click で，ツリーを全て閉じる
        if (e.isShiftDown() || e.getClickCount() == 2) {
            collapseAll();
            return;
        }
        popup.show((Component) e.getSource(), e.getX(), e.getY());
    }

    /**
     * スタンプを xml ファイルに書き出す.
     * by masuda-sensei
     */
    @MenuAction
    public void exportUserStampBox() {

        // stampBytesを含めたデータを書き出す
        ExtendedStampTreeXmlBuilder builder = new ExtendedStampTreeXmlBuilder();
        final StampTreeXmlDirector director = new StampTreeXmlDirector(builder);
        AbstractStampBox stampBox = context.getCurrentBox();

        FileDialog fd = new FileDialog(context.getFrame(), "ファイルにスタンプを保存する", FileDialog.SAVE);
        fd.setVisible(true);
        File[] files = fd.getFiles();
        if (files.length != 1) { return; }
        final File file = files[0];

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                blockGlass.block();
                List<StampTree> publishList = new ArrayList<>(IInfoModel.STAMP_ENTITIES.length);
                List<StampTree> trees = stampBox.getAllTrees();
                publishList.addAll(trees);
                return director.build(publishList);
            }

            @Override
            protected void done() {
                blockGlass.setText("");
                blockGlass.unblock();

                try (FileOutputStream f = new FileOutputStream(file);
                     OutputStreamWriter w = new OutputStreamWriter(f, StandardCharsets.UTF_8)) {

                    w.write(get());

                } catch (IOException | InterruptedException | ExecutionException ex) {
                    processException(ex);
                }
            }

            private void processException(Exception ex) {
                System.out.println("StampBoxPluginExtraMenu.java: " + ex);
            }
        };
        worker.execute();
    }

    /**
     * xml ファイルから新しい userStampBox を作る.
     * by masuda-sensei
     */
    @MenuAction
    public void importUserStampBox() {
        AbstractStampBox stampBox = context.getCurrentBox();

        FileDialog fd = new FileDialog(context.getFrame(), "ファイルからスタンプを読み込む", FileDialog.LOAD);
        fd.setVisible(true);
        File[] files = fd.getFiles();
        if (files.length != 1) { return; }
        File file = files[0];

        SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() {
                blockGlass.block();

                try {
                    // xml ファイルから StampTree 作成
                    FileInputStream in = new FileInputStream(file);
                    List<StampTree> userTrees;
                    // stampBytesを含めたデータを読み込む
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                        // stampBytesを含めたデータを読み込む
                        ExtendedStampTreeBuilder builder = new ExtendedStampTreeBuilder();
                        ExtendedStampTreeDirector director = new ExtendedStampTreeDirector(builder);
                        userTrees = director.build(reader);
                    }

                    // StampTree に組み込む transfer handler を作っておく
                    StampTreeNodeTransferHandler transferHandler = new StampTreeNodeTransferHandler();

                    userTrees.stream().filter(stampTree -> !stampTree.getEntity().equals(IInfoModel.ENTITY_ORCA))
                        .forEach(stampTree -> {
                            // 読み込んだ stampTree に transfer handler などを組み込む
                            stampTree.setUserTree(true);
                            stampTree.setTransferHandler(transferHandler);
                            stampTree.setStampBox(context);

                            // ポップアップメニュー組込
                            final StampTreePopupMenu popup = new StampTreePopupMenu(stampTree);
                            stampTree.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mousePressed(MouseEvent e) {
                                    showPopup(e);
                                }

                                @Override
                                public void mouseReleased(MouseEvent e) {
                                    showPopup(e);
                                }

                                private void showPopup(MouseEvent e) {
                                    if (e.isPopupTrigger()) {
                                        popup.show(stampTree, e.getX(), e.getY());
                                    }
                                }
                            });

                            // StampTreePanel 作成
                            StampTreePanel treePanel = new StampTreePanel(stampTree);

                            // 作った StampTreePanel を該当する tab に replace
                            String treeName = stampTree.getTreeName();
                            int index = stampBox.indexOfTab(treeName);
                            if (index == -1) {
                                // 同じ名前のタブがなければ最後に加える
                                stampBox.add(treeName, treePanel);
                            } else {
                                // 同じタブがあれば，replace
                                stampBox.setComponentAt(index, treePanel);
                            }
                        });

                } catch (IOException ex) {
                    System.out.println("StampBoxPluginExtraMenu.java: " + ex);
                }
                return null;
            }

            @Override
            protected void done() {
                blockGlass.setText("");
                blockGlass.unblock();
            }
        };
        worker.execute();
    }

    /**
     * 増田内科様の ExtendedStampTreeXmlBuilder.java を組み込む.
     * DefaultStampTreeXmlBuilder の必要箇所を private -> protected に変更して無理矢理 extends した.
     */
    private class ExtendedStampTreeXmlBuilder extends DefaultStampTreeXmlBuilder {
        int count = 0;

        @Override
        public void buildStart() throws IOException {
            stringWriter = new StringWriter();
            writer = new BufferedWriter(stringWriter);
            writer.write(makeComment());
            writer.write("<extendedStampTree project=");
            writer.write(addQuote("open.dolphin"));
            writer.write(" version=");
            writer.write(addQuote("1.0"));
            writer.write(">\n");
        }

        private String makeComment() {
            StringBuilder sb = new StringBuilder();
            sb.append("<!-- StampBox Export Data, Creator: ");
            sb.append(Project.getUserModel().getFacilityModel().getFacilityName());
            sb.append(", Created on: ");
            sb.append(new Date().toString());
            sb.append(" -->\n");
            return sb.toString();
        }

        @Override
        protected void buildLeafNode(StampTreeNode node) throws IOException {

            StampTreeNode myParent = (StampTreeNode) node.getParent();
            StampTreeNode curNode = getCurrentNode();

            if (myParent != curNode) {
                closeBeforeMyParent(myParent);
            }

            // 特殊文字を変換する
            writer.write("<stampInfo name=");
            String val = toXmlText(node.toString());
            writer.write(addQuote(val));

            ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();

            writer.write(" role=");
            writer.write(addQuote(info.getStampRole()));

            writer.write(" entity=");
            writer.write(addQuote(info.getEntity()));

            writer.write(" editable=");
            val = String.valueOf(info.isEditable());
            writer.write(addQuote(val));

            val = info.getStampMemo();
            if (val != null) {
                writer.write(" memo=");
                val = toXmlText(val);
                writer.write(addQuote(val));
            }

            if (info.isSerialized()) {
                val = info.getStampId();
                writer.write(" stampId=");
                writer.write(addQuote(val));
                // ここで対応するstampBytesをデータベースから読み込み登録する.
                // Zombie Stamp だと Exception が発生する
                String stampHexBytes = null;
                try {
                    stampHexBytes = getHexStampBytes(val);
                } catch (RuntimeException e) {
                    //e.printStackTrace(System.err);
                    System.out.println("StampBoxPluginExtraMenu: Zombie Stamp" + toXmlText(node.toString()));
                }
                // 実態のないスタンプの場合 null になる
                if (stampHexBytes != null) {
                    writer.write(" stampBytes=");
                    writer.write(addQuote(stampHexBytes));
                }
            }
            writer.write("/>\n");
            blockGlass.setText(count + " 個のスタンプを保存しました");
            count++;
        }

        /**
         * StampIdから対応するStampModelを取得してstampBytesのHex文字列を作成する.
         */
        private String getHexStampBytes(String stampId) throws RuntimeException {
            StampDelegater del = new StampDelegater();
            // スタンプの実態を取得
            // rest 化でゾンビスタンプがあると，ここで InternalServerErrorException が発生するようになった
            StampModel model = del.getStamp(stampId);

            // stampBytesを返す
            byte[] stampBytes = ModelUtils.xmlEncode(model.getStamp());
            return HexBytesTool.bytesToHex(stampBytes);
        }

        @Override
        public void buildEnd() throws IOException {
            writer.write("</extendedStampTree>\n");
            writer.flush();
        }
    }

    /**
     * 増田内科様の ExtendedStampTreeBuilder.java を組み込む.
     * DefaultStampTreeBuilder の必要箇所を private -> protected に変更して無理矢理 extends した.
     */
    private class ExtendedStampTreeBuilder extends DefaultStampTreeBuilder {

        private void buildStampInfo(String name,
                                    String role,
                                    String entity,
                                    String editable,
                                    String memo,
                                    String id,
                                    String stampHexBytes) {     // stampBytesのHex文字列を追加

            // 「エディタから発行」以外は stampHexBytes がなければ無効なので無視
            if (stampHexBytes == null && !FROM_EDITOR.equals(name)) {
                System.out.println("ExtendedStampTreeBuilder: hexBytes is null: stamp '" + name + "' ignored");
                return;
            }

            buildStampInfo(name, role, entity, editable, memo, id);

            if (id != null) {
                StampDelegater sdl = new StampDelegater();

                // rest 化で，InternalServerErrorException が出るようになった
                StampModel model = null;
                try {
                    model = sdl.getStamp(id);
                } catch (RuntimeException e) {
                    System.out.println("adding stamp: " + name);
                }

                // データベースにスタンプが存在しない場合は新たに作成して登録する.
                if (model == null) {
                    model = new StampModel();
                    long userId = Project.getUserModel().getId();
                    model.setId(id); // id 再利用
                    model.setEntity(entity);
                    model.setUserId(userId);
                    byte[] stampBytes = HexBytesTool.hexToBytes(stampHexBytes);
                    IInfoModel stamp = (IInfoModel) ModelUtils.xmlDecode(stampBytes);
                    model.setStamp(stamp);
                    // 新たに作成したStampModelをデータベースに登録する
                    sdl.putStamp(model);
                }
            }
        }
    }

    /**
     * 増田内科様の ExtendedStampTreeDirector.java を組み込む.
     * StampTreeDirector の final を除去したり，コンストラクタを作ったり，private->protected にして無理矢理 extends した.
     */
    private class ExtendedStampTreeDirector extends StampTreeDirector {
        private int count = 0;

        private ExtendedStampTreeDirector(ExtendedStampTreeBuilder builder) {
            this.builder = builder;
        }

        @Override
        public int startElement(String eName, Element e) {

            switch (eName) {
                case "stampInfo":
                    ((ExtendedStampTreeBuilder) builder).buildStampInfo(
                            e.getAttributeValue("name"),
                            e.getAttributeValue("role"),
                            e.getAttributeValue("entity"),
                            e.getAttributeValue("editable"),
                            e.getAttributeValue("memo"),
                            e.getAttributeValue("stampId"),
                            e.getAttributeValue("stampBytes")
                    );
                    blockGlass.setText(count + " 個のスタンプを読み込みました");
                    count++;
                    return TT_STAMP_INFO;

                case "node":
                    builder.buildNode(e.getAttributeValue("name"));
                    return TT_NODE;

                case "root":
                    builder.buildRoot(e.getAttributeValue("name"), e.getAttributeValue("entity"));
                    return TT_ROOT;

                default:
                    break;
            }
            return -1;
        }
    }
}
