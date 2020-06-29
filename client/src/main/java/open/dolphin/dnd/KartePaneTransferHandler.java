package open.dolphin.dnd;

import open.dolphin.client.*;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.SchemaModel;
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
import java.util.Objects;

public class KartePaneTransferHandler extends DolphinTransferHandler {
    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.getLogger(KartePaneTransferHandler.class);

    private KartePane kartePane;
    private JTextPane source;
    private boolean shouldRemove;

    // Document 編集に伴って自動的に動く
    private Position srcP0 = null, srcP1 = null;

    public KartePaneTransferHandler(KartePane kp) {
        kartePane = kp;
    }

    /**
     * Create a Transferable implementation that contains the selected text.
     *
     * @param c source JTextPane
     * @return Transferable of selected text
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
            // この Posision は Document 編集に伴って自動的に動く
            srcP0 = doc.createPosition(start);
            srcP1 = doc.createPosition(end);
        } catch (BadLocationException e) {
            logger.error(e.getMessage());
        }
        String data = source.getSelectedText();
        return new StringSelection(data);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) { return false; }

        JTextPane target = (JTextPane) support.getComponent();

        // 選択した文字列を選択した範囲内にドロップした場合は何もしない
        if (target.equals(source)
            && (target.getCaretPosition() >= srcP0.getOffset())
            && (target.getCaretPosition() <= srcP1.getOffset())) {
            return true;
        }

        try {
            Transferable tr = support.getTransferable();

            if (tr.isDataFlavorSupported(DolphinDataFlavor.stampTreeNodeFlavor)) {
                // StampTreeNodeを受け入れる
                return doStampInfoDrop(tr);

            } else if (tr.isDataFlavorSupported(DolphinDataFlavor.stampListFlavor)
                && kartePane.getMyRole().equals(IInfoModel.ROLE_P)) {
                // KartePaneからのオーダスタンプをインポートする. P に入れる分はここで捕捉.
                // stringFlavor のスタンプは最後に捕捉される.
                return doStampDrop(tr);

            } else if (tr.isDataFlavorSupported(DolphinDataFlavor.imageEntryFlavor)
                && kartePane.getMyRole().equals(IInfoModel.ROLE_SOA)) {
                // シェーマボックスからのDnDを受け入れる - SOA のみ
                return doImageEntryDrop(tr);

            } else if (tr.isDataFlavorSupported(DolphinDataFlavor.schemaListFlavor)
                && kartePane.getMyRole().equals(IInfoModel.ROLE_SOA)) {
                // Paneからのシェーマを受け入れる - SOA のみ
                return doSchemaDrop(tr);

            } else if (tr.isDataFlavorSupported(DolphinDataFlavor.stringFlavor)) {
                // 文字列 Drop は同一 JTextPane なら移動なので shouldRemove = true にする
                String str = (String) tr.getTransferData(DolphinDataFlavor.stringFlavor);
                target.replaceSelection(str);
                shouldRemove = (target == source);
                return true;
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            logger.error(ex.getMessage());
        }
        return false;
    }

    @Override
    public int getSourceActions(JComponent c) {
        // 選択テキストのイメージを作る
        String text = ((JTextPane) c).getSelectedText();
        setDragImage(text);
        return COPY_OR_MOVE;
    }

    /**
     * Remove the old text if the action is a MOVE.
     * However, we do not allow dropping on top of the selected text,
     * so in that case do nothing.
     *
     * @param c source JTextPane
     * @param data Transferable
     * @param action COPY or MOVE
     */
    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        // ここに入ってくるデータは text のみ
        JTextComponent tc = (JTextComponent) c;
        if (tc.isEditable() && shouldRemove && (action == MOVE)) {
            if (srcP0 != null && srcP1 != null && srcP0.getOffset() != srcP1.getOffset()) {
                try {
                    tc.getDocument().remove(srcP0.getOffset(), srcP1.getOffset() - srcP0.getOffset());
                } catch (BadLocationException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }
        source = null;
    }

    /**
     * インポート可能かどうかを返す.
     *
     * @param support TransferSupport
     * @return can import
     */
    @Override
    public boolean canImport(TransferSupport support) {
        if (!kartePane.getTextPane().isEditable()) { return false; }

        String myRole = kartePane.getMyRole();

        for (DataFlavor flavor : support.getDataFlavors()) {
            // String OK
            if (DolphinDataFlavor.stringFlavor.equals(flavor)) {
                return true;
            }
            // Schema OK on SOA
            if (DolphinDataFlavor.schemaListFlavor.equals(flavor)
                && IInfoModel.ROLE_SOA.equals(myRole)) {
                return true;
            }
            // Image OK on SOA
            if (DolphinDataFlavor.imageEntryFlavor.equals(flavor)
                && IInfoModel.ROLE_SOA.equals(myRole)) {
                return true;
            }
            // OrderStamp OK on P
            if (DolphinDataFlavor.stampListFlavor.equals(flavor)
                && IInfoModel.ROLE_P.equals(myRole)) {
                return true;
            }
            // StampTreeNode needs furthur inspection
            if (DolphinDataFlavor.stampTreeNodeFlavor.equals(flavor)) {
                try {
                    StampTreeNode testNode = null;
                    StampTreeNode droppedNode =
                        (StampTreeNode) support.getTransferable().getTransferData(DolphinDataFlavor.stampTreeNodeFlavor);

                    // leaf を探す
                    if (droppedNode.isLeaf()) {
                        testNode = droppedNode;

                    } else {
                        Enumeration<StampTreeNode> e = droppedNode.preorderEnumeration();
                        while (e.hasMoreElements()) {
                            StampTreeNode node = e.nextElement();
                            if (node.isLeaf()) {
                                // leaf が1つでもとれたら, その leaf で判断する
                                testNode = node;
                                break;
                            }
                        }
                    }
                    if (Objects.nonNull(testNode)) {
                        String role = testNode.getStampInfo().getStampRole();
                        return canImport(role);
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * 受入可能な Role かどうか判断する.
     * KartePane と Role が合致, またはテキスト, または Orca で P なら受入可能.
     *
     * @param role テストする Role
     * @return 受入可能かどうか
     */
    private boolean canImport(String role) {
        return kartePane.getMyRole().equals(role)
            || IInfoModel.ROLE_TEXT.equals(role)
            || (IInfoModel.ROLE_ORCA_SET.equals(role) && IInfoModel.ROLE_P.equals(kartePane.getMyRole()));
    }

    /**
     * Drop された ModuleInfo (StampInfo) をインポートする.
     *
     * @param tr Transferable
     * @return 成功した時 true
     */
    private boolean doStampInfoDrop(Transferable tr) {
        try {
            // Drop された StampTreeNode から addList を作る
            StampTreeNode droppedNode = (StampTreeNode) tr.getTransferData(DolphinDataFlavor.stampTreeNodeFlavor);
            List<ModuleInfoBean> addList = new ArrayList<>();

            if (droppedNode.isLeaf()) {
                // 葉の場合
                ModuleInfoBean stampInfo = droppedNode.getStampInfo();
                if (canImport(stampInfo.getStampRole())) {
                    addList.add(stampInfo);
                }
            } else {
                // 葉を探す
                Enumeration<StampTreeNode> e = droppedNode.preorderEnumeration();
                while (e.hasMoreElements()) {
                    StampTreeNode node = e.nextElement();
                    if (node.isLeaf()) {
                        ModuleInfoBean stampInfo = node.getStampInfo();
                        if (canImport(stampInfo.getStampRole())) {
                            addList.add(stampInfo);
                        }
                    }
                }

            }
            kartePane.stampInfoDropped(addList);
            return true;

        } catch (IOException | UnsupportedFlavorException ex) {
            logger.info(ex.getMessage());
        }
        return false;
    }

    /**
     * Drop された Stamp (ModuleModel) をインポートする.
     * カルテに実体化された stamp が drop された場合. Role P しか呼ばれない.
     *
     * @param tr Transferable
     * @return インポートに成功した時 true
     */
    private boolean doStampDrop(Transferable tr) {
        try {
            // スタンプのリストを取得する
            OrderList list = (OrderList) tr.getTransferData(DolphinDataFlavor.stampListFlavor);
            ModuleModel[] stamps = list.getOrderList();

            // pPaneにスタンプを挿入する
            for (ModuleModel stamp : stamps) {
                kartePane.stamp(stamp);
            }

            // drag されたスタンプがあるとき drop した数を設定する
            // これで同じpane内でのDnDを判定している
            if (kartePane.getDraggedCount() > 0 && kartePane.getDraggedStamp() != null) {
                // 同一 pane 内での DnD
                kartePane.setDroppedCount(stamps.length);

            } else {
                // 他の pane からの DnD の場合は重複チェックをする
                for (ModuleModel stamp : stamps) {
                    int duplicate = StampModifier.checkDuplicates(stamp, kartePane);
                    if (duplicate > 0) { break; }
                }
            }
            return true;

        } catch (IOException | UnsupportedFlavorException ex) {
            logger.error(ex.getMessage());
        }
        return false;
    }

    /**
     * カルテから Drop されたシェーマをインポートする. Role SOA しか呼ばれない.
     *
     * @param tr Transferable
     * @return succeeded
     */
    private boolean doSchemaDrop(Transferable tr) {
        try {
            // Schemaリストを取得する
            SchemaList list = (SchemaList) tr.getTransferData(DolphinDataFlavor.schemaListFlavor);
            SchemaModel[] schemas = list.getSchemaList();
            for (SchemaModel schema : schemas) {
                kartePane.stampSchema(schema);
            }
            if (kartePane.getDraggedCount() > 0 && kartePane.getDraggedStamp() != null) {
                kartePane.setDroppedCount(schemas.length);
            }
            return true;
        } catch (IOException | UnsupportedFlavorException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    /**
     * ImageBox から Drop されたイメージをインポートする. Role SOA しか呼ばれない.
     */
    private boolean doImageEntryDrop(final Transferable tr) {
        try {
            // Imageを取得する
            ImageEntry entry = (ImageEntry) tr.getTransferData(DolphinDataFlavor.imageEntryFlavor);
            kartePane.imageEntryDropped(entry);
            return true;
        } catch (IOException | UnsupportedFlavorException ex) {
            logger.error(ex.getMessage());
        }
        return false;
    }

    /**
     * クリップボードへデータを転送する.
     */
    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
        super.exportToClipboard(comp, clip, action);
        // cut の時 ...?
        if (action == MOVE) {
            JTextPane pane = (JTextPane) comp;
            if (pane.isEditable()) {
                pane.replaceSelection("");
            }
        }
    }
}
