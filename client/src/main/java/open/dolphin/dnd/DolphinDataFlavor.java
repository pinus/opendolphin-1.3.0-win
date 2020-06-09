package open.dolphin.dnd;

import open.dolphin.client.ImageEntry;
import open.dolphin.client.OrderList;
import open.dolphin.client.SchemaList;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.order.MasterItem;
import open.dolphin.stampbox.StampTreeNode;

import java.awt.datatransfer.DataFlavor;

public class DolphinDataFlavor extends DataFlavor {
    public static final DataFlavor stampListFlavor
        = new DataFlavor(OrderList.class, "Order List");

    public static final DataFlavor schemaListFlavor
        = new DataFlavor(SchemaList.class, "Schema List");

    public static final DataFlavor stampTreeNodeFlavor
        = new DataFlavor(StampTreeNode.class, "Stamp Tree Node");

    public static final DataFlavor imageEntryFlavor
        = new DataFlavor(ImageEntry.class, "Image Entry");

    public static final DataFlavor diagnosisFlavor
        = new DataFlavor(RegisteredDiagnosisModel.class, "Registered Diagnosis Model");

    public static final DataFlavor masterItemFlavor
        = new DataFlavor(MasterItem.class, "Master Item");

}
