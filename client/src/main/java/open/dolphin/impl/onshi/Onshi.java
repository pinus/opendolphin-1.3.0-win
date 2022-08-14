package open.dolphin.impl.onshi;

import open.dolphin.client.AbstractChartDocument;
import open.dolphin.client.ChartImpl;
import open.dolphin.delegater.OrcaDelegater;
import open.dolphin.event.BadgeEvent;
import open.dolphin.helper.DBTask;
import open.dolphin.helper.StringTool;
import open.dolphin.orca.orcadao.bean.OnshiKenshin;
import open.dolphin.orca.orcadao.bean.OnshiYakuzai;
import open.dolphin.ui.PNSScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * オン資 ChartDocument.
 */
public class Onshi extends AbstractChartDocument {
    // Title
    private static final String TITLE = "オン資";

    private JTextPane textPane;

    public Onshi() { setTitle(TITLE); }

    @Override
    public void start() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        final JButton loadDrugHistoryButton = new JButton("薬剤情報");
        final JButton loadKenshinButton = new JButton("特定健診情報");
        loadDrugHistoryButton.addActionListener(this::loadDrugHistory);
        loadKenshinButton.addActionListener(this::loadKenshin);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(loadDrugHistoryButton);
        buttonPanel.add(loadKenshinButton);
        buttonPanel.add(Box.createHorizontalGlue());

        textPane = new JTextPane();
        textPane.setMargin(new Insets(7, 7, 7, 7));
        PNSScrollPane scroller = new PNSScrollPane(textPane);
        scroller.setVerticalScrollBarPolicy(PNSScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(PNSScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel myPanel = getUI();
        myPanel.setLayout(new BorderLayout());
        myPanel.add(buttonPanel, BorderLayout.NORTH);
        myPanel.add(scroller, BorderLayout.CENTER);

        /**
         * バッジ表示.
         */
        DBTask<Integer> task = new DBTask<Integer>() {
            @Override
            protected Integer doInBackground() throws Exception {
                String ptnum = getContext().getPatient().getPatientId();
                OrcaDelegater delegater = new OrcaDelegater();

                boolean hasDrugHistory = delegater.hasDrugHistory(ptnum);
                boolean hasKenshin = delegater.hasKenshin(ptnum);
                loadDrugHistoryButton.setEnabled(hasDrugHistory);
                loadKenshinButton.setEnabled(hasKenshin);

                int badgeNum = (hasDrugHistory? 1:0) + (hasKenshin? 1:0);
                return badgeNum;
            }

            public void succeeded(Integer badgeNum) {
                BadgeEvent e = new BadgeEvent(Onshi.this);
                e.setTabIndex(5);
                e.setBadgeNumber(badgeNum);
                ((ChartImpl) getContext()).setBadge(e);
            }
        };
        task.execute();
    }

    public void loadDrugHistory(ActionEvent e) {
        DBTask<List<OnshiYakuzai>> task = new DBTask<List<OnshiYakuzai>>() {

            @Override
            protected List<OnshiYakuzai> doInBackground() {
                String ptnum = getContext().getPatient().getPatientId();
                OrcaDelegater delegater = new OrcaDelegater();
                return delegater.getDrugHistory(ptnum);
            }

            @Override
            public void succeeded(List<OnshiYakuzai> onshiYakuzai) {
                StringBuilder sb = new StringBuilder();

                // 日付順
                sb.append("日付順\n");
                String date = "";
                int shoho = 0;
                int chozai = 0;
                for (OnshiYakuzai o : onshiYakuzai) {
                    if (!date.equals(o.getIsoDate()) || o.getShohoSeqnum() != shoho || o.getChozaiSeqnum() != chozai) {
                        date = o.getIsoDate();
                        shoho = o.getShohoSeqnum();
                        chozai = o.getChozaiSeqnum();
                        sb.append(String.format("\n%s 医療機関:%d 薬局:%d\n", date, shoho, chozai));
                    }
                    String yakuzainame = o.getYakuzainame();
                    yakuzainame = StringTool.toHankakuNumber(yakuzainame);
                    yakuzainame = StringTool.toHankakuUpperLower(yakuzainame);
                    yakuzainame = yakuzainame.replaceAll("．", ".");

                    sb.append(String.format("    %s ", yakuzainame));
                    if (o.getYohoname().equals("")) {
                        // 外用剤
                        sb.append(String.format("%s%s %s\n", o.getSuryo(), o.getTaniname(), o.getShiji()));
                    } else {
                        sb.append(String.format("%s%s %s %sTD\n", o.getSuryo(), o.getTaniname(), o.getYohoname(), o.getKaisu()));
                    }
                }

                // 薬剤別
                sb.append("\n薬剤別\n\n");
                // sort
                Collections.sort(onshiYakuzai, (o1, o2) -> {
                    int dat = o1.getIsoDate().compareTo(o2.getIsoDate());
                    int name = o1.getYakuzainame().compareTo(o2.getYakuzainame());
                    int yoho = o1.getYohocd().compareTo(o2.getYohocd());
                    return yoho == 0? name == 0? dat : name : yoho;
                });

                for (OnshiYakuzai o : onshiYakuzai) {
                    if (o.getYohocd().equals("900")) {
                        // 外用剤
                        sb.append(String.format("%s %s %s%s\n", o.getYakuzainame(), o.getIsoDate(), o.getSuryo(), o.getTaniname()));
                    } else {
                        LocalDate startDate = LocalDate.parse(o.getIsoDate());
                        LocalDate endDate = startDate.plusDays(o.getKaisu());
                        date = String.format("%s〜%s", startDate.format(DateTimeFormatter.ISO_DATE), endDate.format(DateTimeFormatter.ISO_DATE));
                        sb.append(String.format("%s %s\n", o.getYakuzainame(), date));
                    }
                }

                textPane.setText(sb.toString());
            }
        };
        task.execute();
    }

    public void loadKenshin(ActionEvent e) {
        DBTask<List<OnshiKenshin>> task = new DBTask<List<OnshiKenshin>>() {

            @Override
            protected List<OnshiKenshin> doInBackground() {
                String ptnum = getContext().getPatient().getPatientId();
                OrcaDelegater delegater = new OrcaDelegater();
                return delegater.getKenshin(ptnum);
            }

            @Override
            public void succeeded(List<OnshiKenshin> onshiKenshin) {
                StringBuilder sb = new StringBuilder();
                String date = "";

                for (OnshiKenshin k : onshiKenshin) {
                    if (!date.equals(k.getIsoDate())) {
                        date = k.getIsoDate();
                        sb.append(date); sb.append("\n");
                    }
                    String v = k.getDataValue();
                    switch (k.getKomokucd()) {
                        case "1A020000000191111": // 尿糖 機械
                        case "1A020000000190111": // 尿糖 目視
                        case "1A010000000191111": // 尿蛋白 機械
                        case "1A010000000190111": // 尿蛋白 目視
                            k.setDataValue(valToString("1:-、2:±、3:+、4:++、5:+++", v));
                            break;
                        case "1A105160700166211": // 尿沈渣
                        case "9A110160700000011": // 心電図
                            k.setDataValue(valToString("1:所見あり、2:所見なし", v));
                            break;
                        case "9N056000000000011": // 既往歴
                        case "9N061000000000011": // 自覚症状
                        case "9N066000000000011": // 他覚症状
                            k.setDataValue(valToString("1:特記すべきことあり、2:特記すべきことなし", v));
                            break;
                        case "9N501000000000011": // メタボリックシンドローム判定
                            k.setDataValue(valToString("1:基準該当、2:予備群該当、3:非該当、4:判定不能", v));
                            break;
                        case "9N701000000000011": // 服薬1(血圧)
                        case "9N706000000000011": // 服薬2(血糖)
                        case "9N711000000000011": // 服薬3(脂質)
                            k.setDataValue(valToString("1:服薬あり、2:服薬なし", v));
                            break;
                        case "9N736000000000011": // 喫煙
                        case "9N716000000000011": // 既往歴1(脳血管)
                        case "9N721000000000011": // 既往歴2(心血管)
                        case "9N726000000000011": // 既往歴3(腎不全・人工透析)
                        case "9N731000000000011": // 貧血
                        case "9N741000000000011": // 20歳からの体重変化
                        case "9N746000000000011": // 30分以上の運動習慣
                        case "9N751000000000011": // 歩行又は身体活動
                        case "9N761000000000011": // 1年の体重変化
                        case "9N756000000000011": // 歩行速度
                        case "9N771000000000011": // 食べ方2(就寝前)
                        case "9N781000000000011": // 食習慣
                        case "9N796000000000011": // 睡眠
                        case "9N806000000000011": // 保健指導の希望
                            k.setDataValue(valToString("1:はい、2:いいえ", v));
                            break;
                        case "9N782000000000011": // 食べ方3(間食)
                            k.setDataValue(valToString("1:毎日、2:時々、3:ほとんど摂取しない", v));
                            break;
                        case "9N506000000000011": // 保健指導レベル
                            k.setDataValue(valToString("1:積極的支援、2:動機付け支援、3:なし、4:判定不能", v));
                            break;
                        case "9N766000000000011": // 食べ方1(早食い等)
                            k.setDataValue(valToString("1:速い、2:ふつう、3:遅い", v));
                            break;
                        case "9N786000000000011": // 飲酒
                            k.setDataValue(valToString("1:毎日、2:時々、3:ほとんど飲まない", v));
                            break;
                        case "9N791000000000011": // 飲酒量
                            k.setDataValue(valToString("1:1合未満、2:1~2合未満、3:2~3合未満、4:3合以上", v));
                            break;
                        case "9N801000000000011": // 生活習慣の改善
                            k.setDataValue(valToString("1:意志なし、2:意志あり(6ヶ月以内)、3:意志あり(近いうち)、 4:取組済み(6ヶ月未満)、5:取組済み(6ヶ月以上)", v));
                            break;
                        case "9N872000000000011": // 咀嚼
                            k.setDataValue(valToString("1:何でも、2:かみにくい、3:ほとんどかめない", v));
                            break;
                    }
                    sb.append(String.format("    %s %s %s\n", k.getKomokuname(), k.getDataValue(), k.getDataTani()));
                }
                textPane.setText(sb.toString());
            }
        };
        task.execute();
    }

    /**
     * "1:-、2:±、3:+、4:++、5:+++", 1 から - を返す.
     *
     * @param str
     * @return string
     */
    private String valToString(String str, String val) {
        String[] item = str.split("、");
        for (int i = 0; i<item.length; i++) {
            String[] map = item[i].split(":");
            if (val.equals(map[0])) {
                return map[1];
            }
        }
        return "";
    }
}
