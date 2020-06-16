package open.dolphin.stampbox;

import open.dolphin.client.GUIConst;
import open.dolphin.helper.MenuActionManager;
import open.dolphin.helper.MenuActionManager.MenuAction;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.ui.sheet.JSheet;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * StampTree に PopupMenu を表示する.
 * StampTreePopupAdapter から切換.
 *
 * @author pns
 */
public class StampTreePopupMenu extends JPopupMenu {
    private static final long serialVersionUID = 1L;

    private final StampTree tree;

    public StampTreePopupMenu(StampTree tree) {
        super();
        this.tree = tree;
        buildPopupMenu();
    }

    private void buildPopupMenu() {
        MenuActionManager m = new MenuActionManager(this);

        boolean b = Objects.nonNull(tree.getSelectedNode());
        addItem(m.getMenuItem("createNewFolder", "新規フォルダ", GUIConst.ICON_FOLDER_NEW_16), b);
        addItem(m.getMenuItem("deleteNode", "削　除", GUIConst.ICON_REMOVE_16), b);
        addItem(m.getMenuItem("renameNode", "名前を変更", GUIConst.ICON_EMPTY_16), b);
        addSeparator();

        addItem(m.getMenuItem("collapseAll", "フォルダを全て閉じる", GUIConst.ICON_TREE_COLLAPSED_16), true);
        addItem(m.getMenuItem("expandAll", "フォルダを全て展開する", GUIConst.ICON_TREE_EXPANDED_16), true);
    }

    private void addItem(JMenuItem item, boolean enable) {
        item.setEnabled(enable);
        add(item);
    }

    @MenuAction
    public void createNewFolder() {
        tree.createNewFolder();
    }

    @MenuAction
    public void deleteNode() {
        StampTreeNode node = tree.getSelectedNode();
        String stampName = node.isLeaf()
            ? node.getStampInfo().getStampName()
            : node.getUserObject().toString();

        int ans = JSheet.showConfirmDialog(tree,
                stampName + "\n本当に削除しますか", "スタンプ削除", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.OK_OPTION) {
            if (isEditable()) {
                ((StampTreeModel)tree.getModel()).removeNodeFromParent(node);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    @MenuAction
    public void renameNode() {
        if (isEditable()) {
            tree.renameNode();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @MenuAction
    public void expandAll() {
        tree.expandAll();
    }

    @MenuAction
    public void collapseAll() {
        tree.collapseAll();
    }

    private boolean isEditable() {
        StampTreeNode node = tree.getSelectedNode();
        if (node.isLeaf()) {
            // Leaf なので StampInfo 　を得る
            ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
            return info.isEditable();
        }
        return true;
    }
}
