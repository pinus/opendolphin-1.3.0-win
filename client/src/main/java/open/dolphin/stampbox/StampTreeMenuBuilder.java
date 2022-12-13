package open.dolphin.stampbox;

import open.dolphin.client.GUIConst;
import open.dolphin.dnd.StampTreeNodeTransferable;
import open.dolphin.infomodel.ModuleInfoBean;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.regex.Pattern;

/**
 * StampTree から JMenu または JPopupMenu を構築する.
 *
 * @author pns
 */
public class StampTreeMenuBuilder {
    public static final Icon ICON = GUIConst.ICON_FOLDER_16;

    private List<StampTreeMenuModel> product;
    private List<StampTree> trees;
    private String text; // 検索文字列
    private StampTreeMenuListener menuListener;

    public StampTreeMenuBuilder(StampTree stampTree) {
        this(stampTree, null);
    }

    public StampTreeMenuBuilder(StampTree stampTree, String searchText) {
        this(Arrays.asList(stampTree), searchText);
    }

    public StampTreeMenuBuilder(List<StampTree> stampTrees) {
        this(stampTrees, null);
    }

    public StampTreeMenuBuilder(List<StampTree> stampTrees, String searchText) {
        trees = stampTrees;
        text = searchText;
    }

    /**
     * StampTree をスキャンして StampTreeMenuModel を構築する.
     */
    private void build() {
        // 既に作成済み
        if (product != null) {
            return;
        }

        product = new ArrayList<>();
        trees.forEach(tree -> {

            StampTreeMenuModel model = new StampTreeMenuModel();
            model.setRoot(new JMenu(tree.getTreeName()));
            model.setEntity(tree.getEntity());

            // 親メニューのスタックを生成する
            List<JMenu> subMenus = new ArrayList<>();
            // 親のない item のスタック
            List<JMenuItem> rootItems = new ArrayList<>();
            // parent をキーに，対応する JMenu を取り出す HashMap
            HashMap<StampTreeNode, JMenu> parentMap = new HashMap<>();

            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
            Enumeration<TreeNode> e = rootNode.preorderEnumeration();

            // スタンプをスキャンする
            e.nextElement(); // consume root

            while (e.hasMoreElements()) {
                // 調査対象のノードを得る
                StampTreeNode node = (StampTreeNode) e.nextElement();
                // その親を得る
                StampTreeNode parent = (StampTreeNode) node.getParent();

                if (parentMap.containsKey(parent)) {
                    // 既に親が登録されている場合
                    if (!node.isLeaf()) {
                        // フォルダの場合
                        String folderName = node.getUserObject().toString();
                        JMenu subMenu = new JMenu(folderName);
                        // subMenu は parent の subMenu 内に加える
                        parentMap.get(parent).add(subMenu);
                        // この node も parentMap に加える
                        parentMap.put(node, subMenu);

                        // フォルダ item を作って menu 中に入れておく
                        JMenuItem item = new JMenuItem(folderName);
                        item.setIcon(ICON);
                        subMenu.add(item);
                        addActionListener(item, tree, node);

                    } else {
                        // 親のいる item の場合は，親のもとに入れる
                        ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
                        String completion = info.getStampName();
                        JMenuItem item = new JMenuItem(completion);
                        addActionListener(item, tree, node);
                        parentMap.get(parent).add(item);
                    }

                } else {
                    // 親がいない場合は検索する
                    if (!node.isLeaf()) {
                        // フォルダの場合
                        String completion = node.getUserObject().toString();

                        // searchText が null の場合は合致と判断して全部返す
                        if (matches(completion)) {
                            String folderName = node.getUserObject().toString();
                            JMenu subMenu = new JMenu(folderName);
                            // 親として加える
                            subMenus.add(subMenu);
                            parentMap.put(node, subMenu);

                            // フォルダ item を作って menu 中に入れておく
                            JMenuItem item = new JMenuItem(folderName);
                            item.setIcon(ICON);
                            subMenu.add(item);
                            addActionListener(item, tree, node);
                        }

                    } else {
                        // 親のない item の場合
                        ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
                        String completion = info.getStampName();

                        // searchText が null の場合は合致と判断して全部返す
                        if (matches(completion)) {
                            // 一致した場合
                            JMenuItem item = new JMenuItem(completion);
                            addActionListener(item, tree, node);
                            // 親のない item
                            rootItems.add(item);
                        }
                    }
                }
            }

            model.setRootItems(rootItems);
            model.setSubMenus(subMenus);

            product.add(model);
        });
    }

    /**
     * Menu item にリスナを登録する. ActionListener から StampMenuActionListener へブリッジする.
     *
     * @param item menu item
     * @param tree stamp tree
     * @param node stamp tree node
     */
    private void addActionListener(JMenuItem item, StampTree tree, StampTreeNode node) {
        StampTreeMenuEvent menuEvent = new StampTreeMenuEvent(item);
        menuEvent.setTransferable(new StampTreeNodeTransferable(node));
        menuEvent.setEntity(tree.getEntity());

        item.addActionListener(e -> menuListener.actionPerformed(menuEvent));
        String[] paths = Arrays.stream(node.getPath()).map(TreeNode::toString).toArray(String[]::new);
        item.setToolTipText(String.join("/", paths));
    }

    /**
     * searchText が completion にマッチするかどうかを判断する.
     *
     * @param completion completion
     * @return matched
     */
    private boolean matches(String completion) {
        // searchText が null の場合は合致と判断
        if (text == null) {
            return true;
        }

        // 大文字，小文字を無視するために小文字に変換して比較
        Pattern pattern = Pattern.compile(text.toLowerCase());

        return pattern.matcher(completion.toLowerCase()).matches();
    }

    /**
     * StampTreeMenuModel から JMenu を構築する.
     *
     * @param menu JMenu
     */
    public void build(JMenu menu) {
        build();
        if (product.size() == 1) {
            buildRootless(menu);

        } else {
            product.forEach(model -> {
                JMenu root = model.getRoot();
                model.getSubMenus().forEach(root::add);
                model.getRootItems().forEach(root::add);
                menu.add(root);
            });
        }
    }

    /**
     * StampTreeMenuModel から JPopupMenu を構築する.
     *
     * @param popup popup menu
     */
    public void build(JPopupMenu popup) {
        build();
        if (product.size() == 1) {
            buildRootless(popup);

        } else {
            product.forEach(model -> {
                JMenu root = model.getRoot();
                model.getSubMenus().forEach(root::add);
                model.getRootItems().forEach(root::add);
                popup.add(root);
            });
        }
    }

    /**
     * StampTreeMenuModel から Entity 部分のない Rootless の JMenu を作る.
     *
     * @param menu JMenu
     */
    public void buildRootless(JMenu menu) {
        build();
        product.forEach(model -> model.getSubMenus().forEach(menu::add));
        product.forEach(model -> model.getRootItems().forEach(menu::add));
    }

    /**
     * StampTreeMenuModel から Entity 部分のない Rootless の JPopupMenu を作る.
     *
     * @param popup popup menu
     */
    public void buildRootless(JPopupMenu popup) {
        build();
        product.forEach(model -> model.getSubMenus().forEach(popup::add));
        product.forEach(model -> model.getRootItems().forEach(popup::add));
    }

    /**
     * メニュー選択時のアクションを定義するリスナを登録する.
     *
     * @param listener StampTreeMenuListener
     */
    public void addStampTreeMenuListener(StampTreeMenuListener listener) {
        menuListener = listener;
    }
}
