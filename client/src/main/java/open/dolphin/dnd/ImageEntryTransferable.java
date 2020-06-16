package open.dolphin.dnd;

import open.dolphin.client.ImageEntry;

import java.awt.datatransfer.*;

/**
 * Transferable class of the ImageIcon.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public final class ImageEntryTransferable extends DolphinTransferable<ImageEntry> {

    public ImageEntryTransferable(ImageEntry entry) {
        super(entry);
        setTransferDataFlavors(new DataFlavor[] { DolphinDataFlavor.imageEntryFlavor });
    }
}
