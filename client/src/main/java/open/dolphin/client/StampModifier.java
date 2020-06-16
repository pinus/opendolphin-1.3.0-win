package open.dolphin.client;

import open.dolphin.helper.StringTool;
import open.dolphin.infomodel.*;
import open.dolphin.orca.ClaimConst;
import open.dolphin.ui.sheet.JSheet;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * drop された stamp を加工する.
 *
 * @author pns
 */
public class StampModifier {

    private static String[] UNIT_MAI = {
            "620005757", "620005758", "620007608", "620007706", "620007807", "620614501"
    };

    /**
     * スタンプ加工バッチ処理.
     *
     * @param stamp
     */
    public static void modify(ModuleModel stamp) {
        //checkDuplicate(stamp, kartePane);
        if (IInfoModel.ENTITY_TREATMENT.equals(stamp.getModuleInfo().getEntity())) {
            addTodaysDate(stamp);
        }

        if (IInfoModel.ENTITY_MED_ORDER.equals(stamp.getModuleInfo().getEntity())) {
            adjustNumber(stamp);
            addUnit(stamp);
        }
    }

    /**
     * 重複スタンプがあれば注意を促す.
     * KartePane，KartePaneTransferHandler から呼ばれる.
     *
     * @param srcStamp
     * @param kartePane
     * @return duplicate の数
     */
    public static int checkDuplicates(ModuleModel srcStamp, KartePane kartePane) {

        BundleDolphin srcModel = (BundleDolphin) srcStamp.getModel();
        String classCode = srcModel.getClassCode();

        List<StampHolder> distStamps = kartePane.getDocument().getStampHolders();
        ClaimItem[] srcItems = srcModel.getClaimItem();

        final List<String> duplicates = new ArrayList<>();

        for (ClaimItem srcItem : srcItems) {

            for (StampHolder distHolder : distStamps) {
                BundleDolphin distModel = (BundleDolphin) distHolder.getStamp().getModel();

                // classCode が一致した場合に重複チェック
                if (classCode.equals(distModel.getClassCode())) {

                    for (ClaimItem distItem : distModel.getClaimItem()) {
                        if (distItem.getCode().matches("^[1,6].*") && // 6.. 薬剤，1.. 検査その他
                                distItem.getCode().equals(srcItem.getCode())) {

                            String text = StringTool.toHankakuNumber(distItem.getName());
                            text = StringTool.toHankakuUpperLower(text);
                            text = text.replaceAll("　", " ");
                            duplicates.add(text);
                        }
                    }
                }
            }
        }

        if (!duplicates.isEmpty()) {
            final Window parent = SwingUtilities.getWindowAncestor(kartePane.getComponent());

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    StringBuilder sb = new StringBuilder();
                    sb.append("重複したスタンプがあります");
                    for (String item : duplicates) {
                        sb.append("\n「").append(item).append("」");
                    }
                    JSheet.showMessageDialog(parent, sb.toString(), "スタンプ重複", JOptionPane.WARNING_MESSAGE);
                }
            });
        }

        return duplicates.size();
    }

    /**
     * 初回実施で number が入力されていない場合，今日の日付を入れる
     *
     * @param stamp
     */
    private static void addTodaysDate(ModuleModel stamp) {
        ClaimBundle bundle = (ClaimBundle) stamp.getModel();
        ClaimItem[] items = bundle.getClaimItem();
        for (ClaimItem c : items) {
            // 初回実施で number が入力されていない場合，今日の日付を入れる
            if ("840000085".equals(c.getCode())) {
                // 既に入力されていなければ今日の日付を入力
                if (c.getNumber() == null || !c.getNumber().matches("[0-9]*-[0-9]*")) {
                    Calendar calendar = Calendar.getInstance();
                    c.setNumber(String.format("%02d-%02d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE)));
                    break;
                }
            }
        }
    }

    /**
     * 外用剤の bundleNumber，number を補正する.
     * 外用剤の場合は bundle は常に 1 にして，その分を dose の方に増やす.
     * ドレニゾンテープ 1枚 x 3回分 → ドレニゾンテープ 3枚 x 1回分 という形式にする.
     *
     * @param stamp
     */
    public static void adjustNumber(ModuleModel stamp) {
        BundleDolphin bundle = (BundleDolphin) stamp.getModel();
        String bundleStr = bundle.getBundleNumber();

        // 外用剤で bundleNumber が 1 でないばあいは補正する
        if (ClaimConst.RECEIPT_CODE_GAIYO.equals(bundle.getClassCode()) && !"1".equals(bundleStr)) {
            bundle.setBundleNumber("1");
            ClaimItem[] items = bundle.getClaimItem();

            try {
                int bdl = Integer.parseInt(bundleStr);
                for (ClaimItem c : items) {
                    int dose = Integer.parseInt(c.getNumber());
                    c.setNumber(String.valueOf(dose * bdl));
                }
            } catch (NumberFormatException ex) {
                //System.out.println("StampModifier: " + ex.getMessage());
            }
        }
    }

    /**
     * 単位の付いてない薬剤に単位を付ける.
     *
     * @param stamp
     */
    private static void addUnit(ModuleModel stamp) {
        ClaimItem[] items = ((BundleDolphin) stamp.getModel()).getClaimItem();
        for (ClaimItem c : items) {
            if (c.getUnit() == null || c.getUnit().equals("")) {
                c.setUnit(getUnit(c.getCode()));
            }
        }
    }

    private static String getUnit(String code) {
        for (String s : UNIT_MAI) {
            if (s.equals(code)) return "枚";
        }
        return "";
    }
}
