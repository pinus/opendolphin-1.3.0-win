package open.dolphin.dnd;

import open.dolphin.stampbox.StampTreeNode;

import java.awt.datatransfer.DataFlavor;

/**
 * Tranferable class of the StampTreeNode.
 *
 * @author Kazushi Minagawa
 * @author pns
 */
public class StampTreeNodeTransferable extends DolphinTransferable<StampTreeNode> {

    public StampTreeNodeTransferable(StampTreeNode node) {
        super(node);
        setTransferDataFlavors(new DataFlavor[] { DolphinDataFlavor.stampTreeNodeFlavor });
    }
}
