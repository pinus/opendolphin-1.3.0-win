package open.dolphin.client;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.stampbox.LocalStampTreeNodeTransferable;
import open.dolphin.stampbox.StampTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * KartePaneTransferHandler
 *
 * @author Minagawa, Kazushi
 */
public class PTransferHandler extends TransferHandler {
    private static final long serialVersionUID = -7891004155072724783L;
    private DataFlavor stringFlavor = DataFlavor.stringFlavor;
    private Logger logger = LoggerFactory.getLogger(PTransferHandler.class);

    // KartePane
    private KartePane pPane;
    private JTextPane source;
    private boolean shouldRemove;

    // Start and end position in the source text.
    // We need this information when performing a MOVE
    // in order to remove the dragged text from the source.
    private Position p0 = null, p1 = null;

    public PTransferHandler(KartePane pPane) {
        this.pPane = pPane;
    }

    @Override
    public boolean importData(JComponent c, Transferable tr) {

        JTextPane tc = (JTextPane) c;

        if (!canImport(c, tr.getTransferDataFlavors())) {
            return false;
        }

        if (tc.equals(source) &&
                (tc.getCaretPosition() >= p0.getOffset()) &&
                (tc.getCaretPosition() <= p1.getOffset())) {
            shouldRemove = false;
            return true;
        }

        try {
            if (tr.isDataFlavorSupported(LocalStampTreeNodeTransferable.localStampTreeNodeFlavor)) {
                // スタンプボックスからのスタンプをインポートする
                shouldRemove = false;
                return doStampInfoDrop(tr);

            } else if (tr.isDataFlavorSupported(OrderListTransferable.orderListFlavor)) {
                // KartePaneからのオーダスタンプをインポートする
                return doStampDrop(tr);

            } else if (tr.isDataFlavorSupported(stringFlavor)) {
                String str = (String) tr.getTransferData(stringFlavor);
                tc.replaceSelection(str);
                shouldRemove = (tc == source);
                return true;
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            logger.error(ex.getMessage());
        }

        return false;
    }

    /**
     * Create a Transferable implementation that contains the selected text.
     */
    @Override
    protected Transferable createTransferable(JComponent c) {
        source = (JTextPane) c;
        int start = source.getSelectionStart();
        int end = source.getSelectionEnd();
        Document doc = source.getDocument();
        if (start == end) {
            return null;
        }
        try {
            p0 = doc.createPosition(start);
            p1 = doc.createPosition(end);
        } catch (BadLocationException e) {
            System.out.println("PTransferHandler.java: " + e);
        }
        String data = source.getSelectedText();

        return new StringSelection(data);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    /**
     * Remove the old text if the action is a MOVE.
     * However, we do not allow dropping on top of the selected text,
     * so in that case do nothing.
     */
    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        JTextComponent tc = (JTextComponent) c;
        if (tc.isEditable() && (shouldRemove) && (action == MOVE)) {
            if ((p0 != null) && (p1 != null)
                    && (p0.getOffset() != p1.getOffset())) {
                try {
                    tc.getDocument().remove(p0.getOffset(),
                            p1.getOffset() - p0.getOffset());
                } catch (BadLocationException e) {
                    System.out.println("PTransferHandler.java: " + e);
                }
            }
        }
        shouldRemove = false;
        source = null;
    }

    /**
     * インポート可能かどうかを返す.
     */
    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        return ((JTextPane) c).isEditable() && hasFlavor(flavors);
    }

    /**
     * Flavorリストのなかに受け入れられものがあるかどうかを返す.
     */
    protected boolean hasFlavor(DataFlavor[] flavors) {

        for (DataFlavor flavor : flavors) {
            // String OK
            if (stringFlavor.equals(flavor)) {
                return true;
            }
            // StampTreeNode(FromStampTree) OK
            if (LocalStampTreeNodeTransferable.localStampTreeNodeFlavor.equals(flavor)) {
                return true;
            }
            // OrderStamp List OK
            if (OrderListTransferable.orderListFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * DropされたModuleInfo(StampInfo)をインポートする.
     *
     * @param tr Transferable
     * @return 成功した時 true
     */
    private boolean doStampInfoDrop(Transferable tr) {

        try {
            // DropされたTreeNodeを取得する
            StampTreeNode droppedNode = (StampTreeNode) tr.getTransferData(LocalStampTreeNodeTransferable.localStampTreeNodeFlavor);

            // 葉の場合
            if (droppedNode.isLeaf()) {
                ModuleInfoBean stampInfo = droppedNode.getStampInfo();
                String role = stampInfo.getStampRole();
                switch (role) {
                    case IInfoModel.ROLE_P:
                        pPane.stampInfoDropped(stampInfo);
                        break;
                    case IInfoModel.ROLE_TEXT:
                        pPane.stampInfoDropped(stampInfo);
                        break;
                    case IInfoModel.ROLE_ORCA_SET:
                        pPane.stampInfoDropped(stampInfo);
                        break;
                }

                return true;
            }

            // Dropされたノードの葉を列挙する
            Enumeration e = droppedNode.preorderEnumeration();
            List<ModuleInfoBean> addList = new ArrayList<>(5);
            String role = null;
            while (e.hasMoreElements()) {
                StampTreeNode node = (StampTreeNode) e.nextElement();
                if (node.isLeaf()) {
                    ModuleInfoBean stampInfo = node.getStampInfo();
                    role = stampInfo.getStampRole();
                    if (stampInfo.isSerialized() && (role.equals(IInfoModel.ROLE_P) || (role.equals(IInfoModel.ROLE_TEXT)))) {
                        addList.add(stampInfo);
                    }
                }
            }

            if (role == null) {
                return true;
            }

            // まとめてデータベースからフェッチしインポートする
            if (role.equals(IInfoModel.ROLE_TEXT)) {
                pPane.textStampInfoDropped(addList);
            } else if (role.equals(IInfoModel.ROLE_P)) {
                pPane.stampInfoDropped(addList);
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace(System.err);
        } catch (UnsupportedFlavorException ex) {
            System.out.println("PTransferHandler.java: " + ex);
        }

        return false;
    }

    /**
     * DropされたStamp(ModuleModel)をインポートする.
     * カルテに実体化された stamp が drop された場合
     *
     * @param tr Transferable
     * @return インポートに成功した時 true
     */
    private boolean doStampDrop(Transferable tr) {

        try {
            // スタンプのリストを取得する
            OrderList list = (OrderList) tr.getTransferData(OrderListTransferable.orderListFlavor);
            ModuleModel[] stamps = list.getOrderList();

            // pPaneにスタンプを挿入する
            for (ModuleModel stamp : stamps) {
                pPane.stamp(stamp);
            }

            // drag されたスタンプがあるとき drop した数を設定する
            // これで同じpane内でのDnDを判定している
            if (pPane.getDraggedCount() > 0 && pPane.getDraggedStamp() != null) {
                // 同一 pane 内での DnD
                pPane.setDroppedCount(stamps.length);

            } else {
                // 他の pane からの DnD の場合は重複チェックをする
                for (ModuleModel stamp : stamps) {
                    int duplicate = StampModifier.checkDuplicates(stamp, pPane);
                    if (duplicate > 0) { break; }
                }
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace(System.err);
        } catch (UnsupportedFlavorException ex) {
            System.out.println("PTransferHandler.java: " + ex);
        }

        return false;
    }

    /**
     * クリップボードへデータを転送する.
     */
    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
        super.exportToClipboard(comp, clip, action);
        // cut の場合を処理する
        if (action == MOVE) {
            JTextPane pane = (JTextPane) comp;
            if (pane.isEditable()) {
                pane.replaceSelection("");
            }
        }
    }
}
