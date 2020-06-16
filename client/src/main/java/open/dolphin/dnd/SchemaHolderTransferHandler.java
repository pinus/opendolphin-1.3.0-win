package open.dolphin.dnd;

import open.dolphin.client.*;
import open.dolphin.infomodel.SchemaModel;

import javax.swing.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;

/**
 * SchemaHolder に付ける TransferHandler.
 *
 * @author Kazushi Minagawa
 * @author pns
 */
public class SchemaHolderTransferHandler extends DolphinTransferHandler {
    private static final long serialVersionUID = -1293765478832142035L;

    public SchemaHolderTransferHandler() { }

    @Override
    protected Transferable createTransferable(JComponent c) {
        SchemaHolder source = (SchemaHolder) c;
        KartePane kartePane = source.getKartePane();
        kartePane.setDraggedStamp(new ComponentHolder[]{source});
        kartePane.setDraggedCount(1);
        SchemaModel schema = source.getSchema();
        SchemaList list = new SchemaList();
        list.setSchemaList(new SchemaModel[]{schema});

        return new SchemaListTransferable(list);
    }

    @Override
    public int getSourceActions(JComponent c) {
        setDragImage((JLabel) c);
        return COPY_OR_MOVE;
    }

    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        if (action == NONE) {
            return;
        }

        if (action == MOVE) {
            SchemaHolder test = (SchemaHolder) c;
            KartePane context = test.getKartePane();

            if (context.getComponent().isEditable()) {
                context.removeSchema(test);
            }
            context.setDraggedStamp(null);
            context.setDraggedCount(0);
            context.setDroppedCount(0);
        }
    }

    @Override
    public boolean importData(TransferSupport support) {
        return false;
    }

    /**
     * SchemaHolder の上への Drop はできない.
     *
     * @param support TransferSupport
     * @return can import
     */
    @Override
    public boolean canImport(TransferSupport support) {
        return false;
    }

    /**
     * スタンプをクリップボードへ転送する.
     */
    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
        SchemaHolder sh = (SchemaHolder) comp;
        Transferable tr = createTransferable(comp);
        clip.setContents(tr, null);

        if (action == MOVE) {
            KartePane kartePane = sh.getKartePane();
            if (kartePane.getTextPane().isEditable()) {
                kartePane.removeSchema(sh);
            }
        }
    }
}
