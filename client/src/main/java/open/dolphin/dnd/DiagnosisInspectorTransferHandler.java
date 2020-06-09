package open.dolphin.dnd;

import open.dolphin.client.ChartImpl;
import open.dolphin.client.DiagnosisDocument;

import javax.swing.*;
import java.util.stream.Stream;

/**
 * DiagnosisInspecterTransferHandler.
 *
 * @author pns
 */
public class DiagnosisInspectorTransferHandler extends DolphinTransferHandler {
    private static final long serialVersionUID = 1L;
    private final ChartImpl context;

    public DiagnosisInspectorTransferHandler(ChartImpl context) {
        this.context = context;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support) || !support.isDrop()) { return false; }

        DiagnosisDocument doc = context.getDiagnosisDocument();
        TransferHandler handler = doc.getDiagnosisTable().getTransferHandler();
        JComponent target = doc.getDiagnosisTable();

        // DiagnosisTable の Transferhandler に丸投げ
        return handler.importData(new TransferSupport(target, support.getTransferable()));
    }

    @Override
    public boolean canImport(TransferSupport support) {
        // drop position の選択をしないようにする
        support.setShowDropLocation(false);
        return Stream.of(support.getDataFlavors()).anyMatch(DolphinDataFlavor.stampTreeNodeFlavor::equals);
    }
}
