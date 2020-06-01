package open.dolphin.client;

import open.dolphin.helper.StringTool;
import open.dolphin.infomodel.BundleMed;
import open.dolphin.infomodel.ClaimItem;
import open.dolphin.infomodel.ModuleModel;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.*;
import java.util.stream.Stream;

/**
 * Transferable class for Stamps.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public final class OrderListTransferable implements Transferable, ClipboardOwner {
    public static DataFlavor orderListFlavor = new DataFlavor(OrderList.class, "Order List");
    public static final DataFlavor[] flavors = {OrderListTransferable.orderListFlavor, DataFlavor.stringFlavor};

    // contains array of ModuleModel (stamp)
    private OrderList list;

    public OrderListTransferable(OrderList list) {
        this.list = list;
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Stream.of(flavors).anyMatch(flavor::equals);
    }

    @NotNull
    @Override
    public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {

        if (flavor.equals(orderListFlavor)) {
            // Stamp が要求されている場合
            return list;

        } if (flavor.equals(DataFlavor.stringFlavor)) {
            // 文字列が要求されている場合, 最初のスタンプの文字列型式を返す
            return getStampText(list.getOrderList()[0]);

        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    /**
     * スタンプの文字列型式を作る.
     *
     * @return string expression of the stamp
     */
    private String getStampText(ModuleModel stamp) {
        if (stamp.getModel() instanceof BundleMed) {
            BundleMed bundle = (BundleMed) stamp.getModel();

            StringBuilder sb = new StringBuilder();

            for (ClaimItem item : bundle.getClaimItem()) {
                if (!item.getCode().matches("099[0-9]{6}")) {
                    sb.append(item.getName());
                    sb.append(" ");

                    if (!item.getCode().matches("0085[0-9]{5}")
                        && !item.getCode().matches("001000[0-9]{3}")
                        && !item.getCode().matches("810000001")) {
                        sb.append(item.getNumber());
                        sb.append(item.getUnit());
                    }
                }
            }
            sb.append(bundle.getAdminDisplayString());

            // 全角数字とスペースを直す
            String text = sb.toString();
            text = StringTool.toHankakuNumber(text);
            text = StringTool.toHankakuUpperLower(text);
            text = text.replaceAll("　", " ");
            text = text.replace("\n", " ");

            return text;
        }
        return "";
    }

    @Override
    public String toString() {
        return "Order List Transferable";
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        list = null;
    }
}
