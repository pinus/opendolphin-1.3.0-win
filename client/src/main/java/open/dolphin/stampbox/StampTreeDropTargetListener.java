package open.dolphin.stampbox;

import open.dolphin.dnd.StampTreeNodeTransferHandler;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

/**
 * StampTree の DropTargetListener.（StampTree から分離）
 *
 * @author pns
 */
public class StampTreeDropTargetListener implements DropTargetListener {
    private StampTree tree;

    //private static final Color UNLOCKED_COLOR = new Color(0x0A,0x53,0xB6);
    //private static final Color LOCKED_COLOR = Color.lightGray;
    private StampTreeRenderer renderer;
    private StampTreeNodeTransferHandler handler;
    private TreePath source;
    private TreePath target;

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        // drag 開始時の path を記録しておく
        tree = (StampTree) dtde.getDropTargetContext().getComponent();
        handler = (StampTreeNodeTransferHandler) tree.getTransferHandler();
        renderer = (StampTreeRenderer) tree.getCellRenderer();
        source = tree.getSelectionPath();
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

        Point p = dtde.getLocation();
        target = tree.getClosestPathForLocation(p.x, p.y);
        if (target == null) {   // target に何もなければ帰る
            handler.setTargetPath(null);
            return;
        }

        // レンダラで drop 先を表示するのに使う色をセット
        renderer.setEditable(!tree.getStampBox().isLocked());

        // drop しようとしている部分が見えるまでスクロール
        Rectangle r = tree.getPathBounds(target);
        scrollTargetToVisible();

        StampTreeNode node = (StampTreeNode) target.getLastPathComponent();
        renderer.setTargetNode(target.getLastPathComponent());
        handler.setTargetPath(target);

        if (node.isLeaf()) {
            if (topOrBottom(p, r) == Position.TOP) {
                targetWithUpperLine();
            } else {
                targetWithUnderLine();
            }
        } else {
            switch (topOrBottomOrCenter(p, r)) {
                case TOP:
                    targetWithUpperLine();
                    break;
                case BOTTOM:
                    targetWithUnderLine();
                    break;
                default: //CENTER
                    targetWithSquare();
            }
        }
        tree.repaint(); // renderer に処理させるため
    }

    /**
     * drop 位置の node の上半分にいるか，下半分にいるか.
     *
     * @param p Point
     * @param r Rectangle
     * @return TOP or BOTTOM
     */
    private Position topOrBottom(Point p, Rectangle r) {
        int offsetToTop = p.y - r.y;
        if (offsetToTop < r.height / 2) {
            return Position.TOP;
        } else {
            return Position.BOTTOM;
        }
    }

    /**
     * drop 位置の上半分にいるか，下半分にいるか，真ん中にいるか.
     *
     * @param p Point
     * @param r Rectangle
     * @return TOP or CENTER or BOTTOM
     */
    private Position topOrBottomOrCenter(Point p, Rectangle r) {
        int offsetToTop = p.y - r.y;
        int offsetToBottom = r.y + r.height - p.y;
        if (offsetToTop < r.height / 3) {
            return Position.TOP;
        } else if (offsetToBottom < r.height / 3) {
            return Position.BOTTOM;
        } else {
            return Position.CENTER;
        }
    }

    private void targetWithUpperLine() {
        // 一番上か，上と親が違う場合は UpperLine で処理. それ以外は UnderLine に変換
        int row = tree.getRowForPath(target);
        boolean parentIsDifferent;
        if (row == 0) {
            parentIsDifferent = true;
        } else {
            StampTreeNode thisNode = (StampTreeNode) target.getLastPathComponent();
            StampTreeNode aboveNode = (StampTreeNode) tree.getPathForRow(row - 1).getLastPathComponent();
            parentIsDifferent = (thisNode.getParent() != aboveNode.getParent());
        }
        if (parentIsDifferent) {
            renderer.setDrawMode(StampTreeRenderer.UPPER_LINE);
            handler.setPosition(StampTreeNodeTransferHandler.Insert.BEFORE);
        } else {
            // UnderLine で処理
            target = tree.getPathForRow(row - 1);
            renderer.setTargetNode(target.getLastPathComponent());
            handler.setTargetPath(target);
            targetWithUnderLine();
        }
/*      renderer.setDrawMode(StampTreeRenderer.UPPER_LINE);
        handler.setPosition(StampTreeTransferHandler.INSERT_BEFORE);*/
    }

    private void targetWithUnderLine() {
        renderer.setDrawMode(StampTreeRenderer.UNDER_LINE);
        handler.setPosition(StampTreeNodeTransferHandler.Insert.AFTER);
    }

    private void targetWithSquare() {
        renderer.setDrawMode(StampTreeRenderer.SQUARE);
        handler.setPosition(StampTreeNodeTransferHandler.Insert.INTO_FOLDER);
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        // drop 領域から出たら，開始時の path に戻しておく
        tree.setSelectionPath(source);
        renderer.setTargetNode(null);
        tree.repaint();
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        handler.importData(new TransferHandler.TransferSupport(tree, dtde.getTransferable()));
        dtde.dropComplete(true); // これをしないとドラッグしてきたアイコンが逃げる

        renderer.setTargetNode(null); // あとしまつ
        handler.setTargetPath(null);
        tree.repaint();
    }

    private void scrollTargetToVisible() {
        int row = tree.getRowForPath(target);
        if (row >= 1) {
            tree.scrollRowToVisible(row - 1);
        }
        if (row < tree.getRowCount()) {
            tree.scrollRowToVisible(row + 1);
        }
        tree.scrollRowToVisible(row);
    }

    enum Position {TOP, BOTTOM, CENTER}
}
