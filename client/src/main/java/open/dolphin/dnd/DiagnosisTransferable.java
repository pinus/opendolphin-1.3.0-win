package open.dolphin.dnd;

import open.dolphin.infomodel.RegisteredDiagnosisModel;

import java.awt.datatransfer.*;

/**
 * Transferable class of the RegisteredDiagnosisModel.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public final class DiagnosisTransferable extends DolphinTransferable<RegisteredDiagnosisModel> {

    public DiagnosisTransferable(RegisteredDiagnosisModel model) {
        super(model);
        setTransferDataFlavors(new DataFlavor[] { DolphinDataFlavor.diagnosisFlavor });
    }
}
