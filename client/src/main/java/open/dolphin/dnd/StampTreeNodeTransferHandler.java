package open.dolphin.dnd;

import open.dolphin.client.OrderList;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.stampbox.StampTree;
import open.dolphin.stampbox.StampTreeModel;
import open.dolphin.stampbox.StampTreeNode;
import open.dolphin.stampbox.StampTreeRenderer;
import open.dolphin.ui.sheet.JSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * スタンプ箱への DnD.
 *
 * @author Minagawa, Kazushi
 * @author pns
 */
public class StampTreeNodeTransferHandler extends DolphinTransferHandler {
    private static final long serialVersionUID = 1205897976539749194L;
    private Logger logger = LoggerFactory.getLogger(StampTreeNodeTransferHandler.class);

    // StampTreeNode
    private final DataFlavor stampTreeNodeFlavor = DolphinDataFlavor.stampTreeNodeFlavor;

    // KartePane から　Drop される StampList
    private final DataFlavor orderFlavor = DolphinDataFlavor.stampListFlavor;

    // KartePane から Drop されるテキスト Flavor
    private final DataFlavor stringFlavor = DolphinDataFlavor.stringFlavor;

    // 病名エディタから Drop される Registered Diagnosis Flavor
    private final DataFlavor diagnosisFlavor = DolphinDataFlavor.diagnosisFlavor;

    // Drop する target の path
    private TreePath targetPath;
    private Insert insertPosition;
    private StampTree tree;

    public void setTargetPath(TreePath t) {
        targetPath = t;
    }

    public void setPosition(Insert p) {
        insertPosition = p;
    }

    public Insert getInsertPosition() {
        return insertPosition;
    }

    /**
     * 選択されたノードでDragを開始する.
     *
     * @return StampTreeNodeTransferable
     */
    @Override
    protected Transferable createTransferable(JComponent c) {
        tree = (StampTree) c;
        StampTreeNode dragNode = (StampTreeNode) tree.getLastSelectedPathComponent();
        return new StampTreeNodeTransferable(dragNode);
    }

    @Override
    public int getSourceActions(JComponent c) {
        insertPosition = null;

        StampTree tree = (StampTree) c;
        TreePath path = tree.getSelectionPath();

        if (path != null) {
            TreeNode node = (TreeNode) path.getLastPathComponent();
            StampTreeRenderer r = (StampTreeRenderer) tree.getCellRenderer();

            boolean selected = false;
            boolean expanded = tree.isExpanded(path);
            boolean isLeaf = node.isLeaf();
            int row = tree.getRowForPath(path);
            boolean hasFocus = true;

            JLabel draggedComp = (JLabel) r.getTreeCellRendererComponent(tree, node, selected, expanded, isLeaf, row, hasFocus);
            draggedComp.setSize(draggedComp.getPreferredSize());
            setDragImage(draggedComp);
        }

        return COPY_OR_MOVE;
    }

    /**
     * Drop された Flavor を StampTree にインポートする.
     *
     * @param support TransferSupport
     * @return succeeded
     */
    @Override
    public boolean importData(TransferSupport support) {

        if (targetPath == null) {
            logger.error("targetPath is null");
            return false;
        }
        if (insertPosition == null) {
            logger.error("insertPosition is null");
            return false;
        }
        if (!canImport(support)) {
            return false;
        }

        try {
            // Dropを受けるStampTreeを取得する
            StampTree dropTree = (StampTree) support.getComponent();
            String targetEntity = dropTree.getEntity();

            // Drop位置のノードを取得する
            StampTreeNode targetNode = (StampTreeNode) targetPath.getLastPathComponent();

            // StampTree 内の DnD
            Transferable tr = support.getTransferable();
            if (tr.isDataFlavorSupported(DolphinDataFlavor.stampTreeNodeFlavor)) {

                // ソースのノードを取得する
                // transferHandler からとると root が入ってこない
                //StampTreeNode sourceNode = (StampTreeNode) tr.getTransferData(DolphinDataFlavor.stampTreeNodeFlavor);
                StampTreeNode sourceNode = dropTree.getSelectedNode();

                // Drop 位置の親
                StampTreeNode newParent = (StampTreeNode) targetNode.getParent();

                // root までの親のパスのなかに自分がいるかどうかを判定する
                // Drop 先が DragNode の子である時は DnD できない i.e 親が自分の子になることはできない
                StampTreeModel treeModel = (StampTreeModel) dropTree.getModel();
                TreeNode[] parents = treeModel.getPathToRoot(targetNode);
                boolean exist = false;
                for (TreeNode parent : parents) {
                    if (parent == sourceNode) {
                        exist = true;
                        Toolkit.getDefaultToolkit().beep();
                        //System.out.println("new Child is ancestor");
                        break;
                    }
                }

                // 親が自分の子になろうとしているわけではない場合，処理開始
                if (!exist) {
                    int index = newParent.getIndex(targetNode);
                    if (index == -1) {
                        logger.error("StampTreeTransferHandler: index is -1");
                        return false;
                    }

                    // 同じ親の場合，下に移動した場合 index が変わる
                    if (sourceNode.getParent() == newParent && insertPosition != Insert.INTO_FOLDER) {
                        int sourceRow = dropTree.getRowForPath(new TreePath(sourceNode.getPath()));
                        int targetRow = dropTree.getRowForPath(targetPath);
                        if (sourceRow < targetRow) {
                            index--;
                        }
                    }

                    switch (insertPosition) {
                        case BEFORE:
                            treeModel.undoableRemoveNodeFromParent(sourceNode);
                            treeModel.undoableInsertNodeInto(sourceNode, newParent, index);
                            break;
                        case AFTER:
                            treeModel.undoableRemoveNodeFromParent(sourceNode);
                            treeModel.undoableInsertNodeInto(sourceNode, newParent, index + 1);
                            break;
                        case INTO_FOLDER: //最後の子として挿入
                            treeModel.undoableRemoveNodeFromParent(sourceNode);
                            treeModel.undoableInsertNodeInto(sourceNode, targetNode, targetNode.getChildCount());
                    }
                    TreeNode[] path = treeModel.getPathToRoot(sourceNode);
                    dropTree.setSelectionPath(new TreePath(path));
                }
                return true;

                // KartePane から Drop されたオーダをインポートする
            } else if (tr.isDataFlavorSupported(orderFlavor)) {

                OrderList list = (OrderList) tr.getTransferData(DolphinDataFlavor.stampListFlavor);
                ModuleModel droppedStamp = list.getOrderList()[0];

                // 同一エンティティの場合，選択は必ず起っている
                if (droppedStamp.getModuleInfo().getEntity().equals(targetEntity)) {
                    return dropTree.addStamp(droppedStamp, targetNode);

                    // パス Tree の場合
                } else if (targetEntity.equals(IInfoModel.ENTITY_PATH)) {
                    if (targetNode == null) {
                        targetNode = (StampTreeNode) dropTree.getModel().getRoot();
                    }
                    return dropTree.addStamp(droppedStamp, targetNode);

                } else {
                    // Rootの最後に追加する
                    return dropTree.addStamp(droppedStamp, null);
                }

                // KartePaneからDropされたテキストをインポートする
            } else if (tr.isDataFlavorSupported(stringFlavor)) {

                String text = (String) tr.getTransferData(DataFlavor.stringFlavor);
                if (targetEntity.equals(IInfoModel.ENTITY_TEXT)) {
                    return dropTree.addTextStamp(text, targetNode);
                } else {
                    return dropTree.addTextStamp(text, null);
                }

                // DiagnosisEditorからDropされた病名をインポートする
            } else if (tr.isDataFlavorSupported(diagnosisFlavor)) {

                RegisteredDiagnosisModel rd =
                    (RegisteredDiagnosisModel) tr.getTransferData(DolphinDataFlavor.diagnosisFlavor);
                if (targetEntity.equals(IInfoModel.ENTITY_DIAGNOSIS)) {
                    return dropTree.addDiagnosis(rd, targetNode);
                } else {
                    return dropTree.addDiagnosis(rd, null);
                }
            } else {
                return false;
            }

        } catch (IOException | UnsupportedFlavorException ex) {
            logger.error(ex.getMessage());
        }
        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
    }

    /**
     * インポート可能かどうかを返す.
     *
     * @param support TransferSupport
     * @return can import
     */
    @Override
    public boolean canImport(TransferSupport support) {
        boolean isLocked = ((StampTree) support.getComponent()).getStampBox().isLocked();
        logger.info("stamp box is locked");
        return isFlavorMatched(support.getDataFlavors()) && !isLocked && targetPath != null;
    }

    private boolean isFlavorMatched(DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (stampTreeNodeFlavor.equals(flavor)) {
                return true;
            }
            if (orderFlavor.equals(flavor)) {
                return true;
            }
            if (stringFlavor.equals(flavor)) {
                return true;
            }
            if (diagnosisFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    private void showMessage(String message) {
        JSheet.showMessageDialog(tree, message, "", JOptionPane.WARNING_MESSAGE);
    }

    // target の中に入れるか，前に挿入するか，後ろに挿入するかの情報（StampTreeDropTargetListener でセットする）
    public enum Insert {
        AFTER, BEFORE, INTO_FOLDER
    }
}
