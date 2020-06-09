package open.dolphin.dnd;

import open.dolphin.client.ComponentHolder;
import open.dolphin.client.KartePane;
import open.dolphin.client.OrderList;
import open.dolphin.client.StampHolder;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.infomodel.*;
import open.dolphin.orca.ClaimConst;
import open.dolphin.project.Project;
import open.dolphin.stampbox.StampTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * カルテの StampHolder の TransferHandler.
 *
 * @author Kazushi Minagawa
 * @author pns
 */
public class StampListTransferHandler extends DolphinTransferHandler {
    private static final long serialVersionUID = -9182879162438446790L;
    private Logger logger = LoggerFactory.getLogger(StampListTransferHandler.class);

    public StampListTransferHandler() { }

    /**
     * OrderListTransferable を作る.
     *
     * @param c StampHolder
     * @return OrderListTransferable
     */
    @Override
    protected Transferable createTransferable(JComponent c) {
        StampHolder source = (StampHolder) c;
        KartePane kartePane = source.getKartePane();
        kartePane.setDraggedStamp(new ComponentHolder[]{source});
        kartePane.setDraggedCount(1);
        ModuleModel stamp = source.getStamp();
        OrderList list = new OrderList(new ModuleModel[]{stamp});

        return new StampListTransferable(list);
    }

    @Override
    public int getSourceActions(JComponent c) {
        setDragImage((JLabel) c);
        return COPY_OR_MOVE;
    }

    private void replaceStamp(final StampHolder target, final ModuleInfoBean stampInfo) {

        Thread t = new Thread(() -> {
            StampDelegater sdl = new StampDelegater();
            StampModel stampModel = sdl.getStamp(stampInfo.getStampId());
            final ModuleModel module = new ModuleModel();
            if (stampModel != null) {
                module.setModel(stampModel.getStamp());
                module.setModuleInfo(stampInfo);
            }
            SwingUtilities.invokeLater(() -> {
                // 薬の場合は操作する
                if (module.getModel() instanceof BundleMed) {
                    // 内服薬同士で置き換えの場合，bundle number を保存する
                    BundleMed bundle = (BundleMed) module.getModel();
                    BundleMed orgBundle = (BundleMed) target.getStamp().getModel();
                    if ((ClaimConst.RECEIPT_CODE_NAIYO.equals(bundle.getClassCode()) &&
                            ClaimConst.RECEIPT_CODE_NAIYO.equals(orgBundle.getClassCode())) ||
                            (ClaimConst.RECEIPT_CODE_TONYO.equals(bundle.getClassCode()) &&
                                    ClaimConst.RECEIPT_CODE_TONYO.equals(orgBundle.getClassCode()))) {

                        bundle.setBundleNumber(orgBundle.getBundleNumber());
                    }
                    // 外用剤同士で置き換えの場合は，量とコメントを保存
                    if (ClaimConst.RECEIPT_CODE_GAIYO.equals(bundle.getClassCode()) &&
                            ClaimConst.RECEIPT_CODE_GAIYO.equals(orgBundle.getClassCode())) {
                        bundle.setBundleNumber(orgBundle.getBundleNumber());

                        // 元の量とコメントを取り出し
                        String dose = null;

                        for (ClaimItem c : orgBundle.getClaimItem()) {
                            String code = c.getCode();
                            // 量を保存
                            if (dose == null && code.startsWith("6")) {
                                dose = c.getNumber();
                            }
                            // コメントを追加
                            if (code.matches("^[089].*") &&
                                    !code.equals("001000001") && // 混合 は除外
                                    !code.equals("099209908")    // 一般名処方は除外
                            ) {
                                // 重複していないコードを追加する
                                boolean found = false;
                                for (ClaimItem cc : bundle.getClaimItem()) {
                                    if (cc.getCode().equals(code)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    bundle.addClaimItem(c);
                                }
                            }
                        }
                        // 量を設定
                        if (dose != null) {
                            for (ClaimItem c : bundle.getClaimItem()) {
                                if (c.getCode().startsWith("6")) {
                                    c.setNumber(dose);
                                }
                            }
                        }
                    }
                }
                target.importStamp(module);
            });
        });
        t.start();
    }

    private void confirmReplace(StampHolder target, ModuleInfoBean stampInfo) {

        Window w = SwingUtilities.getWindowAncestor(target);
        String replace = "置き換える";
        String cancel = "取消し";

        int option = JOptionPane.showOptionDialog(
                w,
                "スタンプを置き換えますか?",
                "スタンプ Drag and Drop",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{replace, cancel}, replace);

        if (option == 0) {
            replaceStamp(target, stampInfo);
        }
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) { return false; }

        try {
            StampHolder target = (StampHolder) support.getComponent();
            StampTreeNode droppedNode =
                (StampTreeNode) support.getTransferable().getTransferData(DolphinDataFlavor.stampTreeNodeFlavor);

            // 葉しか扱わない
            if (!droppedNode.isLeaf()) { return false; }

            final ModuleInfoBean stampInfo = droppedNode.getStampInfo();
            String role = stampInfo.getStampRole();

            // Role P しか扱わない
            if (!role.equals(IInfoModel.ROLE_P)) { return false; }

            if (Project.getPreferences().getBoolean("replaceStamp", false)) {
                replaceStamp(target, stampInfo);
            } else {
                SwingUtilities.invokeLater(() -> confirmReplace(target, stampInfo));
            }
            return true;

        } catch (UnsupportedFlavorException | IOException ex) {
            logger.error(ex.getMessage());
        }
        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable tr, int action) {
        if (action == NONE) { return; }

        if (action == MOVE) {
            StampHolder test = (StampHolder) c;
            KartePane kartePane = test.getKartePane();

            // 同一 KartePane 内の DnD の場合だけ移動, それ以外はコピー
            logger.debug("dropped count = " + kartePane.getDroppedCount());
            if (kartePane.getComponent().isEditable() && kartePane.getDroppedCount() > 0) {
                kartePane.removeStamp(test);
            }
            kartePane.setDraggedStamp(null);
            kartePane.setDraggedCount(0);
            kartePane.setDroppedCount(0);
        }
    }

    @Override
    public boolean canImport(TransferSupport support) {
        StampHolder test = (StampHolder) support.getComponent();
        JTextPane tc = test.getKartePane().getTextPane();
        return tc.isEditable()
            && Stream.of(support.getDataFlavors()).
            anyMatch(DolphinDataFlavor.stampTreeNodeFlavor::equals);
    }

    /**
     * スタンプをクリップボードへ転送する.
     */
    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
        StampHolder sh = (StampHolder) comp;
        Transferable tr = createTransferable(comp);
        clip.setContents(tr, null);

        if (action == MOVE) {
            KartePane kartePane = sh.getKartePane();
            if (kartePane.getTextPane().isEditable()) {
                kartePane.removeStamp(sh);
            }
        }
    }

    private BufferedImage changeSize(BufferedImage image, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, width, height, null);
        return scaledImage;
    }
}
