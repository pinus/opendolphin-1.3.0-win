package open.dolphin.client;

import open.dolphin.helper.DailyDoseStringTool;
import open.dolphin.infomodel.BundleMed;
import open.dolphin.infomodel.ClaimItem;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.orca.ClaimConst;
import open.dolphin.ui.Focuser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * StampHolder を右クリックでいろいろいじる.
 *
 * @author pns
 */
public class StampHolderPopupMenu extends JPopupMenu {
    private static final long serialVersionUID = 1L;
    /**
     * メニューに載せる処方日数のリスト
     */
    private static final int[] BUNDLE_NUMS = {3, 4, 5, 7, 10, 14, 21, 28, 30, 56, 60};
    /**
     * メニューに載せる外用剤の量のリスト
     */
    private static final int[] DOSES = {5, 10, 15, 20, 25, 30, 40, 50, 60, 75, 100, 125, 150, 200, 250};
    private final StampHolder ctx;

    public StampHolderPopupMenu(StampHolder ctx) {
        this.ctx = ctx;
    }

    /**
     * 分数対応で文字列を double にして返す.
     *
     * @param str 分数
     * @return double
     */
    public static double stringToDouble(String str) {
        String[] num = str.split("/");
        if (num.length == 1) {
            return Double.parseDouble(str);
        } else {
            return Double.parseDouble(num[0]) / Double.parseDouble(num[1]);
        }
    }

    /**
     * 外用剤，内服薬，それぞれに適切なメニュー項目を加える.
     */
    public void addStampChangeMenu() {

        BundleMed bundle = (BundleMed) ctx.getStamp().getModel();

        if (ClaimConst.RECEIPT_CODE_NAIYO.equals(bundle.getClassCode()) ||
                ClaimConst.RECEIPT_CODE_TONYO.equals(bundle.getClassCode())) {

            // 内服薬用の変更メニューを加える
            addBundleChangeMenu();

        } else {
            // 外用剤用の変更メニューを加える
            addDoseChangeMenu();
        }
    }

    /**
     * 内服薬の変更 menu を作る.
     */
    public void addBundleChangeMenu() {

        // コメント追加
        add(new PutCommentAction());
        // 一般名処方
        add(new PutGenericNameAction());
        addSeparator();

        // 処方日数メニュー
        JMenu bundles = new JMenu("処方日数");
        for (int n : BUNDLE_NUMS) {
            bundles.add(new BundleChangeAction(n));
        }
        add(bundles);
        addSeparator();

        // 用法メニュー
        add(new AdminChangeAction(Admin.once1));
        add(new AdminChangeAction(Admin.once2));
        add(new AdminChangeAction(Admin.once3));
        add(new AdminChangeAction(Admin.once4));
        addSeparator();
        add(new AdminChangeAction(Admin.twice1));
        add(new AdminChangeAction(Admin.twice2));
        addSeparator();
        add(new AdminChangeAction(Admin.thrice1));

        addSeparator();

        // 量調整メニュー
        add(new DoseChangeAction2("2"));
        add(new DoseChangeAction2("3"));
        add(new DoseChangeAction2("1/2"));
        add(new DoseChangeAction2("1/3"));
    }

    /**
     * 外用剤の変更 menu を作る.
     */
    public void addDoseChangeMenu() {

        // コメント追加
        add(new PutCommentAction());
        // 外用剤の部位メニュー
        add(new PutRegionAction());
        // 一般名処方
        add(new PutGenericNameAction());

        addSeparator();

        // 外用剤の量メニュー
        JMenu dose = new JMenu("処方量");
        for (int n : DOSES) {
            dose.add(new DoseChangeAction(n));
        }
        add(dose);
        addSeparator();

        // 量調整メニュー
        add(new DoseChangeAction2("2"));
        add(new DoseChangeAction2("3"));
        add(new DoseChangeAction2("1/2"));
        add(new DoseChangeAction2("1/3"));
        addSeparator();

        // 外用回数変更メニュー
        for (Admin2 admin : Admin2.values()) {
            add(new AdminChangeAction2(admin));
        }
    }

    /**
     * StampHolder に通知.
     *
     * @param stamp stamp
     */
    public void propertyChanged(ModuleModel stamp) {
        firePropertyChange(StampHolder.STAMP_MODIFIED, null, stamp);
    }

    /**
     * ClaimItem の code から，１日何回投与かを判断して返す.
     * cf. order/AdminMaster.java
     *
     * @param code コード
     * @return 1日何回か
     */
    private int getTimes(String code) {
        if (code.startsWith("0010001")) {
            return 1;
        }
        if (code.startsWith("0010002")) {
            return 2;
        }
        if (code.startsWith("0010003")) {
            return 3;
        }

        return 0;
    }

    /**
     * スタンプを複製して返す. bundle もコピーされる. ただし ClaimItem は空.
     *
     * @return cloned stamp
     */
    private ModuleModel createModuleModel(ModuleModel src) {

        ModuleInfoBean srcModuleInfo = src.getModuleInfo();
        BundleMed srcBundle = (BundleMed) src.getModel();

        // 複製
        ModuleModel dist = new ModuleModel();

        BundleMed distBundle = createBundleMed(srcBundle);
        dist.setModel(distBundle);

        ModuleInfoBean distModuleInfo = dist.getModuleInfo();
        distModuleInfo.setEntity(srcModuleInfo.getEntity());
        distModuleInfo.setStampRole(srcModuleInfo.getStampRole());
        distModuleInfo.setStampName(srcModuleInfo.getStampName());

        return dist;
    }

    /**
     * BundleMed を複製して返す. ただし ClaimItem は空.
     *
     * @param src BundleMed
     * @return cloned BundleMed
     */
    private BundleMed createBundleMed(BundleMed src) {
        BundleMed dist = new BundleMed();
        dist.setAdmin(src.getAdmin());
        dist.setAdminCode(src.getAdminCode());
        dist.setAdminCodeSystem(src.getAdminCodeSystem());
        dist.setAdminMemo(src.getAdminMemo());
        dist.setBundleNumber(src.getBundleNumber());
        dist.setClassCode(src.getClassCode());
        dist.setClassCodeSystem(src.getClassCodeSystem());
        dist.setClassName(src.getClassName());
        dist.setMemo(src.getMemo());
        dist.setOrderName(src.getOrderName());
        return dist;
    }

    /**
     * ClaimItem を複製して返す.
     *
     * @param src ClaimItem
     * @return cloned ClaimItem
     */
    private ClaimItem createClaimItem(ClaimItem src) {
        ClaimItem dist = new ClaimItem();
        dist.setClassCode(src.getClassCode());
        dist.setClassCodeSystem(src.getClassCodeSystem());
        dist.setCode(src.getCode());
        dist.setName(src.getName());
        dist.setNumber(src.getNumber());
        dist.setNumberCode(src.getNumberCode());
        dist.setNumberCodeSystem(src.getNumberCodeSystem());
        dist.setUnit(src.getUnit());
        return dist;
    }

    /**
     * オリジナルの ClaimItem[] を複製して返す.
     *
     * @return array of ClaimItem
     */
    private ClaimItem[] createClaimItemInArray() {
        ClaimItem[] src = ((BundleMed) ctx.getStamp().getModel()).getClaimItem();

        List<ClaimItem> dist = new ArrayList<>();
        for (ClaimItem c : src) {
            dist.add(createClaimItem(c));
        }

        return dist.toArray(new ClaimItem[0]);
    }

    /**
     * メニューに載せる用法のリスト
     */
    enum Admin {
        once1("001000103", "１日１回朝食後に"),
        once2("001000105", "１日１回昼食後に"),
        once3("001000107", "１日１回夕食後に"),
        once4("001000111", "１日１回就寝前に"),
        twice1("001000202", "１日２回朝夕食後に"),
        twice2("001000214", "１日２回朝食後及び就寝前に"),
        thrice1("001000303", "１日３回毎食後に");

        public final String code, str;

        Admin(String code, String str) {
            this.code = code;
            this.str = str;
        }
    }

    /**
     * メニューに載せる外用回数のリスト
     */
    enum Admin2 {
        once("001000602", "１日１回外用"),
        twice("001000603", "１日２回外用"),
        thrice("001000604", "１日数回外用"),
        onceTwice("001000614", "１日１〜２回外用"),
        twiceThrice("001000615", "１日２〜３回外用");

        public final String code, str;

        Admin2(String code, String str) {
            this.code = code;
            this.str = str;
        }
    }

    /**
     * 処方日数を変更するアクション.
     */
    private class BundleChangeAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        private final int value;

        public BundleChangeAction(int value) {
            this.value = value;
            BundleMed bundle = (BundleMed) ctx.getStamp().getModel();
            String label = ClaimConst.RECEIPT_CODE_NAIYO.equals(bundle.getClassCode()) ? " 日分" : " 回分";
            putValue(AbstractAction.NAME, value + label);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // 変更されていなければそのまま帰る
            BundleMed srcBundle = (BundleMed) ctx.getStamp().getModel();
            if (srcBundle.getBundleNumber().equals(String.valueOf(value))) {
                return;
            }

            // コピーして stamp を新たに作成
            ModuleModel stamp = createModuleModel(ctx.getStamp());
            BundleMed bundle = (BundleMed) stamp.getModel();
            // ClaimItem をコピー
            bundle.setClaimItem(createClaimItemInArray());
            // 何日分の部分をセット
            bundle.setBundleNumber(String.valueOf(value));

            propertyChanged(stamp);
        }
    }

    /**
     * 内服の用法（１日３回毎食後など）を変更するアクション
     */
    private class AdminChangeAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        private final Admin admin;

        public AdminChangeAction(Admin admin) {
            this.admin = admin;
            putValue(AbstractAction.NAME, admin.str);
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            // 変更されていなければそのまま帰る
            BundleMed srcBundle = (BundleMed) ctx.getStamp().getModel();
            if (admin.code.equals(srcBundle.getAdminCode())) {
                return;
            }

            // 新たなスタンプ作成
            ModuleModel stamp = createModuleModel(ctx.getStamp());
            BundleMed bundle = (BundleMed) stamp.getModel();
            bundle.setAdmin(admin.str);
            bundle.setAdminCode(admin.code);

            // 変更に応じて投与量が何倍になるか = factor
            int srcTimes = getTimes(srcBundle.getAdminCode());
            int distTimes = getTimes(admin.code);
            double factor = (srcTimes == 0) ? 1 : (double) distTimes / srcTimes;

            List<ClaimItem> list = new ArrayList<>();
            // ClaimItem をチェックしながらコピー
            for (ClaimItem src : srcBundle.getClaimItem()) {
                ClaimItem dist = createClaimItem(src);
                String str = dist.getName();
                // １日量文字列がある場合は，投与量を調節する
                if (str.contains("日量")) {
                    double dose = DailyDoseStringTool.getDose(str) * factor;
                    dist.setName(DailyDoseStringTool.getString(str, dose));
                }
                list.add(dist);
            }
            // できた list を srcBundle に登録
            bundle.setClaimItem(list.toArray(new ClaimItem[0]));

            propertyChanged(stamp);
        }
    }

    /**
     * 外用剤の外用回数（１日２回外用など）を変更するアクション
     */
    private class AdminChangeAction2 extends AbstractAction {
        private static final long serialVersionUID = 1L;
        private final Admin2 admin;

        public AdminChangeAction2(Admin2 admin) {
            this.admin = admin;
            putValue(AbstractAction.NAME, admin.str);
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            BundleMed srcBundle = (BundleMed) ctx.getStamp().getModel();

            // 変更されていなければそのまま帰る
            if (admin.code.equals(srcBundle.getAdminCode())) {
                return;
            }

            ModuleModel stamp = createModuleModel(ctx.getStamp());
            BundleMed bundle = (BundleMed) stamp.getModel();
            bundle.setAdmin(admin.str);
            bundle.setAdminCode(admin.code);
            bundle.setClaimItem(createClaimItemInArray());

            propertyChanged(stamp);
        }
    }

    /**
     * 外用剤の処方量を変更するアクション.
     */
    private class DoseChangeAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        private final int value;

        public DoseChangeAction(int value) {
            this.value = value;
            ClaimItem[] item = ((BundleMed) ctx.getStamp().getModel()).getClaimItem();
            putValue(AbstractAction.NAME, value + " " + item[0].getUnit());
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            BundleMed srcBundle = (BundleMed) ctx.getStamp().getModel();

            // 新たなスタンプ作成
            ModuleModel stamp = createModuleModel(ctx.getStamp());
            BundleMed bundle = (BundleMed) stamp.getModel();
            List<ClaimItem> list = new ArrayList<>();

            // ClaimItem をチェックしながらコピー
            boolean changed = false;
            String num = String.valueOf(value);

            for (ClaimItem src : srcBundle.getClaimItem()) {
                ClaimItem dist = createClaimItem(src);
                if (src.getCode().startsWith("6") && !src.getNumber().equals(num)) {
                    dist.setNumber(num);
                    changed = true;
                }
                list.add(dist);
            }

            if (changed) {
                // できた list を srcBundle に登録
                bundle.setClaimItem(list.toArray(new ClaimItem[0]));
                propertyChanged(stamp);
            }
        }
    }

    /**
     * 外用剤の部位を変更するアクション.
     */
    private class PutRegionAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public PutRegionAction() {
            putValue(AbstractAction.NAME, "部位・指示...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            BundleMed srcBundle = (BundleMed) ctx.getStamp().getModel();

            RegionView dialog = new RegionView(null, true);
            dialog.setValue(srcBundle.getClaimItem());

            // ダイアログ表示位置計算　できればスタンプの上に，上にスペースがなければ下に
            Point p = ctx.getLocationOnScreen();
            int y;
            p.y = (y = p.y - dialog.getHeight() - 10) > 30 ? y : p.y + ctx.getHeight() + 10;
            dialog.setLocation(p);
            dialog.setVisible(true);

            // キャンセルされた場合はそのまま帰る
            if (dialog.isCancelled()) {
                dialog.dispose();
                return;
            }

            // 新たなスタンプ作成
            ModuleModel stamp = createModuleModel(ctx.getStamp());
            BundleMed bundle = (BundleMed) stamp.getModel();

            // あらたな ClaimItem を作る
            List<ClaimItem> list = new ArrayList<>();

            // 部位 ClaimItem 以外をコピー  001000607,608,001,002 はマスター設定の問題で別扱い
            for (ClaimItem src : srcBundle.getClaimItem()) {
                if (!src.getCode().startsWith("0010008")
                        && !src.getCode().startsWith("0010009")
                        && !src.getCode().equals("001000607")
                        && !src.getCode().equals("001000608")
                        && !src.getCode().equals("001000001")
                        && !src.getCode().equals("001000002")
                ) {

                    list.add(createClaimItem(src));
                }
            }
            // 部位 ClaimItem は dialog からデータを取り込む
            list.addAll(dialog.getValue());

            // できた list を srcBundle に登録
            bundle.setClaimItem(list.toArray(new ClaimItem[0]));

            dialog.dispose();
            propertyChanged(stamp);
        }
    }

    /**
     * 内服薬の用量を変更するアクション. 何倍にするかで指定する
     */
    private class DoseChangeAction2 extends AbstractAction {
        private static final long serialVersionUID = 1L;
        private final double value;

        public DoseChangeAction2(String str) {
            value = stringToDouble(str);
            putValue(AbstractAction.NAME, "用量を " + str + " 倍に");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BundleMed srcBundle = (BundleMed) ctx.getStamp().getModel();

            // 新たなスタンプ作成
            ModuleModel stamp = createModuleModel(ctx.getStamp());
            BundleMed bundle = (BundleMed) stamp.getModel();

            // あらたな ClaimItem を作る
            List<ClaimItem> list = new ArrayList<>();

            for (ClaimItem src : srcBundle.getClaimItem()) {
                ClaimItem dist = createClaimItem(src);

                // 薬剤コードがあれば，その量を調整
                if (src.getCode().startsWith("6")) {
                    double num = Double.parseDouble(src.getNumber());

                    // 外用剤の場合，最後に ".0" を付けないためのトリック
                    String s = ClaimConst.RECEIPT_CODE_GAIYO.equals(bundle.getClassCode())
                        ? DailyDoseStringTool.doubleToString(num * value, "")
                        : DailyDoseStringTool.doubleToString(num * value, src.getUnit());
                    dist.setNumber(s);
                }

                // １日量文字列があれば，その量を調節
                String str = src.getName();
                if (str.contains("日量")) {
                    double dose = DailyDoseStringTool.getDose(str);
                    dose *= value;
                    dist.setName(DailyDoseStringTool.getString(str, dose));
                }
                list.add(dist);
            }
            // できた list を srcBundle に登録
            bundle.setClaimItem(list.toArray(new ClaimItem[0]));

            propertyChanged(stamp);
        }
    }

    /**
     * コメントを付けるアクション.
     */
    private class PutCommentAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public PutCommentAction() {
            putValue(AbstractAction.NAME, "コメント...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            final JTextField tf = new JTextField(10);
            tf.setPreferredSize(new Dimension(100, 26));
            String[] options = {"追加", "上書き", "キャンセル"};
            JOptionPane pane = new JOptionPane(tf,
                    JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                    options, options[0]) {
                // 初期状態でボタンでなく，tf にフォーカスを取る
                private static final long serialVersionUID = 1L;

                @Override
                public void selectInitialValue() {
                    Focuser.requestFocus(tf);
                }
            };

            JDialog dialog = pane.createDialog("コメント入力");
            dialog.setModal(true);

            // ダイアログ表示位置計算　できればスタンプの上に，上にスペースがなければ下に
            Point p = ctx.getLocationOnScreen();
            int y;
            p.y = (y = p.y - dialog.getHeight() - 10) > 30 ? y : p.y + ctx.getHeight() + 10;
            dialog.setLocation(p);
            dialog.setVisible(true);

            // getValue() の値 -1=エスケープでキャンセル，null=赤ボタンで消した, 文字=optionsの文字
            Object ans = pane.getValue();

            // キャンセルの場合はそのまま帰る
            if (!options[0].equals(ans) && !options[1].equals(ans)) {
                return;
            }

            // 新たなスタンプ作成
            BundleMed srcBundle = (BundleMed) ctx.getStamp().getModel();
            ModuleModel stamp = createModuleModel(ctx.getStamp());
            BundleMed bundle = (BundleMed) stamp.getModel();

            List<ClaimItem> list = new ArrayList<>();

            // 既存のコメント以外はそのまま登録，追加登録の場合は既存のコメントも登録
            for (ClaimItem src : srcBundle.getClaimItem()) {
                if (!src.getCode().startsWith("810000001") || options[0].equals(ans)) {
                    list.add(createClaimItem(src));
                }
            }
            // あらたな ClaimItem を作る
            ClaimItem newComment = new ClaimItem();
            newComment.setClassCode("2");
            newComment.setClassCodeSystem("Claim003");
            newComment.setCode("810000001");
            newComment.setName(tf.getText());
            newComment.setNumber(".");
            newComment.setNumberCode(ClaimConst.YAKUZAI_TOYORYO); // = "10"
            newComment.setNumberCodeSystem("Claim004");

            // コメントを登録，ただしコメントに何も入力されていなければリストに追加しない
            if (!tf.getText().trim().equals("")) {
                list.add(newComment);
            }

            // できた list を srcBundle に登録
            bundle.setClaimItem(list.toArray(new ClaimItem[0]));

            dialog.dispose();
            propertyChanged(stamp);
        }
    }

    /**
     * 一般名記載コメントをつけるアクション.
     */
    private class PutGenericNameAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public PutGenericNameAction() {
            putValue(AbstractAction.NAME, "一般名記載");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // 一般名処方 ClaimItem を作る
            ClaimItem generic = new ClaimItem();
            generic.setClassCode("2");
            generic.setClassCodeSystem("Claim003");
            generic.setCode("099209908");
            generic.setName("一般名記載");
            generic.setNumber(".");
            generic.setNumberCode(ClaimConst.YAKUZAI_TOYORYO); // = "10"
            generic.setNumberCodeSystem("Claim004");

            // 新たなスタンプ作成
            BundleMed srcBundle = (BundleMed) ctx.getStamp().getModel();
            ModuleModel stamp = createModuleModel(ctx.getStamp());
            BundleMed bundle = (BundleMed) stamp.getModel();

            List<ClaimItem> list = new ArrayList<>();

            // 薬を検索して，最初に見つかった薬の後に一般名処方を入れる
            boolean found = false;
            for (ClaimItem src : srcBundle.getClaimItem()) {
                list.add(createClaimItem(src));
                if (!found && src.getCode().startsWith("6")) {
                    list.add(generic);
                    found = true;
                }
            }

            // できた list を srcBundle に登録
            bundle.setClaimItem(list.toArray(new ClaimItem[0]));
            propertyChanged(stamp);
        }
    }
}

