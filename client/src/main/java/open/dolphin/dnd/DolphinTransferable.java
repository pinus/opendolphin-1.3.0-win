package open.dolphin.dnd;

import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.stream.Stream;

public class DolphinTransferable<T> implements Transferable, ClipboardOwner {
    private DataFlavor[] flavors;
    private T targetData;

    public DolphinTransferable(T target) {
        targetData = target;
    }

    protected void setTransferDataFlavors(DataFlavor[] flavors) {
        this.flavors = flavors;
    }

    protected T getObject() { return targetData; }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Stream.of(flavors).anyMatch(flavor::equals);
    }

    @NotNull
    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (Stream.of(flavors).anyMatch(flavor::equals)) {
            return targetData;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        targetData = null;
    }
}
