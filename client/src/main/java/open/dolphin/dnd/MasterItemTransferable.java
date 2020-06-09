package open.dolphin.dnd;

import open.dolphin.order.MasterItem;

import java.awt.datatransfer.DataFlavor;

/**
 * マスタアイテム Transferable クラス.
 *
 * @author kazm
 * @author pns
 */
public class MasterItemTransferable extends DolphinTransferable<MasterItem> {

    public MasterItemTransferable(MasterItem masterItem) {
        super(masterItem);
        setTransferDataFlavors(new DataFlavor[] { DolphinDataFlavor.masterItemFlavor });
    }
}
