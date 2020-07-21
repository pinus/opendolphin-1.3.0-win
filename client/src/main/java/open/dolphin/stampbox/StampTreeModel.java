package open.dolphin.stampbox;

import open.dolphin.infomodel.ModuleInfoBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * スタンプツリーのモデルクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class StampTreeModel extends DefaultTreeModel {
    private static final long serialVersionUID = -2227174337081687786L;
    private Logger logger = LoggerFactory.getLogger(StampTreeModel.class);
    private UndoManager undoManager = new UndoManager();
    private CompoundEdit current = new CompoundEdit();
    private Timer timer = new Timer(30, e -> flush());

    public StampTreeModel(TreeNode node) {
        super(node);
    }

    /**
     * Undoable insert.
     *
     * @param newChild to insert
     * @param parent to insert into
     * @param index to insert at
     */
    public void undoableInsertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index) {
        undoableEditHappened(new InsertEdit(newChild, parent, index));
        insertNodeInto(newChild, parent, index);
    }

    /**
     * Undoable remove.
     *
     * @param node to remove
     */
    public void undoableRemoveNodeFromParent(MutableTreeNode node) {
        undoableEditHappened(new RemoveEdit(node));
        removeNodeFromParent(node);
    }

    /**
     * ノード名の変更をインターセプトして処理する.
     *
     * @param path     tree path
     * @param newValue stamp name
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        // 変更ノードを取得する
        StampTreeNode node = (StampTreeNode) path.getLastPathComponent();
        String oldValue = node.isLeaf()
            ? ((ModuleInfoBean) node.getUserObject()).getStampName()
            : (String) node.getUserObject();
        undoableEditHappened(new RenameEdit(path, oldValue, (String) newValue));
        setName(path, (String) newValue);
    }

    /**
     * ノート名を設定する.
     *
     * @param path TreePath
     * @param name to set
     */
    private void setName(TreePath path, String name) {
        // 変更ノードを取得する
        StampTreeNode node = (StampTreeNode) path.getLastPathComponent();
        // 葉ノードの場合は StampInfo の name を変更する.
        // そうでない場合は新しい文字列を userObject に設定する.
        if (node.isLeaf()) {
            ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
            info.setStampName(name);

        } else {
            node.setUserObject(name);
        }
        // リスナへ通知する
        nodeChanged(node);
    }

    public void flush() {
        timer.stop();
        current.end();
        undoManager.addEdit(current);
        current = new CompoundEdit();
    }

    public void undoableEditHappened(UndoableEdit edit) {
        timer.restart();
        current.addEdit(edit);
    }

    public void undo() {
        logger.info("undo");
        if (undoManager.canUndo()) { undoManager.undo(); }
    }

    public void redo() {
        logger.info("redo");
        if (undoManager.canRedo()) { undoManager.redo(); }
    }

    /**
     * UndoableEdit for rename.
     */
    private class RenameEdit extends AbstractUndoableEdit {
        private TreePath path;
        private String oldName, newName;
        public RenameEdit(TreePath path, String oldValue, String newValue) {
            this.path = path; oldName = oldValue; newName = newValue;
        }
        @Override
        public void undo() { setName(path, oldName); }
        @Override
        public void redo() { setName(path, newName); }
    }

    /**
     * UndoableEdit for insert.
     */
    private class InsertEdit extends AbstractUndoableEdit {
        protected MutableTreeNode child, parent;
        protected int index;

        public InsertEdit(MutableTreeNode child, MutableTreeNode parent, int index) {
            this.child = child; this.parent = parent; this.index = index;
        }
        @Override
        public void undo() { removeNodeFromParent(child); }
        @Override
        public void redo() { insertNodeInto(child, parent, index); }
    }

    /**
     * UndoableEdit for remove.
     */
    private class RemoveEdit extends AbstractUndoableEdit {
        protected MutableTreeNode child, parent;
        protected int index;

        public RemoveEdit(MutableTreeNode child) {
            this.child = child;
            parent = (MutableTreeNode) child.getParent();
            index = child.getParent().getIndex(child);
        }
        @Override
        public void undo() { insertNodeInto(child, parent, index); }
        @Override
        public void redo() { removeNodeFromParent(child); }
    }
}
