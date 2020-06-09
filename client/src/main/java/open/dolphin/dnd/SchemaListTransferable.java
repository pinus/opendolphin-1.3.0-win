package open.dolphin.dnd;

import open.dolphin.client.SchemaList;

import java.awt.datatransfer.*;

/**
 * Transferable class of the Schema list.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public final class SchemaListTransferable extends DolphinTransferable<SchemaList> {

    public SchemaListTransferable(SchemaList list) {
        super(list);
        setTransferDataFlavors(new DataFlavor[] { DolphinDataFlavor.schemaListFlavor });
    }
}
