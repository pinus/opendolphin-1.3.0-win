package open.dolphin.helper;

import j2html.attributes.Attr;
import j2html.tags.Tag;
import open.dolphin.client.StampRenderingHints;
import open.dolphin.infomodel.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static j2html.TagCreator.*;

/**
 * j2html で html を作る.
 *
 * @author pns
 */
public class HtmlHelper {
    private static Logger logger = LoggerFactory.getLogger(HtmlHelper.class);
    //static { (( ch.qos.logback.classic.Logger) logger).setLevel( ch.qos.logback.classic.Level.DEBUG); }

    public static final String ALIGN = Attr.ALIGN;
    public static final String BGCOLOR = "bgcolor";
    public static final String BORDER = Attr.BORDER;
    public static final String BOTTOM = "bottom";
    public static final String CELLPADDING = "cellpadding";
    public static final String CELLSPACING = "cellspacing";
    public static final String COLOR = Attr.COLOR;
    public static final String COLSPAN = Attr.COLSPAN;
    public static final String FONT = "font";
    public static final String NOWRAP = "nowrap";
    public static final String RIGHT = "right";
    public static final String SIZE = Attr.SIZE;
    public static final String TOP = "top";
    public static final String VALIGN = "valign";
    public static final String WIDTH = Attr.WIDTH;

    private static final int MARKER_WIDTH = 16;
    private static final int AMOUNT_WIDTH = 32;
    private static final int UNIT_WIDTH = 24;
    private static final int UNIT_MARGIN = 8;

    /**
     * BundleMed を html 化する.
     *
     * @param bundle    BundleMed
     * @param stampName スタンプ名
     * @param hints     StampRenderingHints
     * @return html
     */
    public static String bundleMed2Html(BundleMed bundle, String stampName, StampRenderingHints hints) {
        String memo = bundle.getMemo().replace("処方", "");
        String html = html().with(body().with(
            // タイトル部分
            table().attr(BORDER, 0).attr(CELLPADDING, 1).attr(WIDTH, hints.getWidth()).with(
                titleTr("RP", stampName, memo, hints.getWidth(), hints.getLabelColorAs16String())
            ),
            // 項目部分 列数=5
            table().attr(BORDER, 0).attr(CELLPADDING, 1).attr(CELLSPACING, 0).attr(WIDTH, hints.getWidth()).with(
                each(Arrays.stream(bundleMedTr(bundle.getClaimItem(), hints.getWidth(), hints.getCommentColorAs16String()))),
                // 用法部分
                adminTr(bundle, hints)
            )
        )).render();

        logger.debug(html);
        return html;
    }

    /**
     * Title 部分の TR タグを作る.
     *
     * @param stampName スタンプ名
     * @param memo      メモ
     * @param width     width
     * @param color     color
     * @return TR Tag
     */
    public static Tag titleTr(String orderName, String stampName, String memo, int width, String color) {
        return tr().attr(BGCOLOR, color).with(
            td(orderName + "）" + stampName).attr(VALIGN, TOP).attr(NOWRAP),
            td().with(tag(FONT).attr(SIZE, -2).withText(memo))
                .attr(ALIGN, RIGHT).attr(VALIGN, BOTTOM).attr(NOWRAP)
        );
    }

    /**
     * ClaimItem の TR タグを作る. (列数=5)
     *
     * @param items array of ClaimItem
     * @param width width
     * @param color color
     * @return array of TR Tags
     */
    public static Tag[] bundleMedTr(ClaimItem[] items, int width, String color) {
        List<Tag> trs = new ArrayList<>();

        for (ClaimItem item : items) {
            if (item.getCode().matches("810000001")) {
                trs.add(tr().with(
                    td("　").attr(WIDTH, MARKER_WIDTH),
                    td().with(tag(FONT).attr(COLOR, color).withText(item.getName())))); // 列数分用意しなくても大丈夫

            } else if (item.getCode().matches("008[0-9]{6}") // コメントコード 008xxxxxx
                || item.getCode().startsWith("8") // コメントコード 8xxxxxxxx
                || item.getCode().matches("0992099[0-9]{2}") // 一般名記載, 後発変更不可, etc
                || item.getCode().matches("001000[0-9]{3}")) { // 用法
                trs.add(tr().with(
                    td("　").attr(WIDTH, MARKER_WIDTH),
                    td(item.getName()))); // 列数分用意しなくても大丈夫

            } else {
                trs.add(tr().with(
                    td("・").attr(VALIGN, TOP).attr(WIDTH, MARKER_WIDTH),
                    td(item.getName()),
                    td(item.getNumber()).attr(ALIGN, RIGHT).attr(VALIGN, BOTTOM).attr(NOWRAP).attr(WIDTH, AMOUNT_WIDTH),
                    td(item.getUnit()).attr(ALIGN, RIGHT).attr(VALIGN, BOTTOM).attr(NOWRAP).attr(WIDTH, UNIT_WIDTH),
                    td(" ").attr(WIDTH, UNIT_MARGIN)));
            }
        }
        return trs.toArray(new Tag[0]);
    }

    /**
     * 用法の TR タグを作る. (列数=5)
     *
     * @param bundle ClaimBundle
     * @param hints  StampRenderingHints
     * @return TR Tag
     */
    public static Tag adminTr(ClaimBundle bundle, StampRenderingHints hints) {
        // 外用剤で bundle number が 1 の場合
        if (bundle.getClassCode().startsWith(IInfoModel.RECEIPT_CODE_GAIYO.substring(0, 2))
            && "1".equals(bundle.getBundleNumber())) {
            return tr().with(
                td("　"),
                td(bundle.getAdmin()).attr(COLSPAN, 3),
                td(" "));
        }
        // それ以外は "日分/回分" を付ける
        String admin = " " + bundle.getBundleNumber()
            + (bundle.getClassCode().startsWith(IInfoModel.RECEIPT_CODE_NAIYO.substring(0, 2)) ? " 日分" : " 回分");
        return tr().with(
            td("　"),
            td(bundle.getAdmin()),
            td(admin).attr(COLSPAN, 2).attr(NOWRAP).attr(ALIGN, RIGHT),
            td(" ").attr(WIDTH, UNIT_MARGIN));
    }

    /**
     * BundleMed を html 化する. 簡易表示バージョン.
     *
     * @param bundle    BundleMed
     * @param stampName スタンプ名
     * @param hints     StampRenderingHints
     * @return html
     */
    public static String bundleMed2HtmlLight(BundleMed bundle, String stampName, StampRenderingHints hints) {
        String memo = bundle.getMemo().replace("処方", "");
        String html = html().with(body().with(
            // タイトル部分
            table().attr(BORDER, 0).attr(CELLPADDING, 1).attr(WIDTH, hints.getWidth()).with(
                titleTr("RP", stampName, memo, hints.getWidth(), hints.getLabelColorAs16String())
            ),
            // 項目部分
            table().attr(BORDER, 0).attr(CELLPADDING, 1).attr(CELLSPACING, 0).attr(WIDTH, hints.getWidth()).with(
                bundleMedTrLight(bundle, hints)
            )
        )).render();

        return html;
    }

    /**
     * ClaimItem 簡易表示用の TR Tag を作る.
     *
     * @param bundle ClaimBundle
     * @param hints  StampRenderingHints
     * @return TR Tag
     */
    public static Tag bundleMedTrLight(BundleMed bundle, StampRenderingHints hints) {
        String name = Stream.of(bundle.getClaimItem())
            .filter(item -> item.getCode().startsWith("6"))
            .map(ClaimItem::getName)
            .collect(Collectors.joining(", "));

        name = name.replaceAll("[．０-９.0-9]*[％]", "")
            .replaceAll("「[^,]*」", "")
            .replaceAll("（[^,]*）", "")
            .replaceAll("ＭＹＫ", "")
            .replaceAll("エステル", "")
            .replaceAll("塩酸塩", "")
            .replaceAll("硫酸塩", "")
            .replaceAll("硝酸塩", "")
            .replaceAll("マレイン酸塩", "")
            .replaceAll("ベシル酸塩", "")
            .replaceAll("[．０-９]*ｍｇ", "")
            .replaceAll("酪酸", "B")
            .replaceAll("吉草酸", "V")
            .replaceAll("プロピオン酸", "P")
            .replaceAll("酢酸", "A")
            .replaceAll("アセトニド", "A")
            ;

        String admin;
        if (bundle.getClassCode().startsWith(IInfoModel.RECEIPT_CODE_GAIYO)) {
            admin = Stream.of(bundle.getClaimItem())
                .filter(item -> item.getCode().matches("001000[7-9][0-9][0-9]"))
                .map(ClaimItem::getName).collect(Collectors.joining(","));
            admin += Stream.of(bundle.getClaimItem())
                .filter(item -> item.getCode().equals("810000001"))
                .map(ClaimItem::getName).collect(Collectors.joining(","));
            admin = StringUtils.truncate(admin, 4);

        } else {
            admin = StringUtils.truncate(bundle.getAdmin()
                .replace("１日", "").replace("回", "x"), 4);
        }

        String num = bundle.getClassCode().startsWith(IInfoModel.RECEIPT_CODE_GAIYO)
            ? bundle.getClaimItem()[0].getNumber()
            : bundle.getBundleNumber();

        return tr().with(
            td("・").attr(VALIGN, TOP).attr(WIDTH, MARKER_WIDTH),
            td(name),
            td(admin).attr(ALIGN, RIGHT).attr(VALIGN, BOTTOM).attr(NOWRAP),
            td(num).attr(ALIGN, RIGHT).attr(VALIGN, BOTTOM).attr(NOWRAP).attr(WIDTH, AMOUNT_WIDTH));
    }

    /**
     * BundleDolphin を html 化する.
     *
     * @param bundle    BundleDolphin
     * @param stampName スタンプ名
     * @param hints     StampRenderingHints
     * @return html
     */
    public static String bundleDolphin2Html(BundleDolphin bundle, String stampName, StampRenderingHints hints) {
        return bundleDolphin2Html(bundle, stampName, hints, false);
    }

    /**
     * BundleDolphin を html 化する.
     *
     * @param bundle    BundleDolphin
     * @param stampName スタンプ名
     * @param hints     StampRenderingHints
     * @param fold      検査スタンプを折りたたみ表示するかどうか
     * @return html
     */
    public static String bundleDolphin2Html(BundleDolphin bundle, String stampName, StampRenderingHints hints, boolean fold) {
        String memo = bundle.getClassCode();
        logger.debug("bundle class code = " + memo);

        String html = html().with(body().with(
            // タイトル部分
            table().attr(BORDER, 0).attr(CELLPADDING, 1).attr(WIDTH, hints.getWidth()).with(
                titleTr(bundle.getOrderName(), stampName, memo, hints.getWidth(), hints.getLabelColorAs16String())
            ),
            // 項目部分
            table().attr(BORDER, 0).attr(CELLPADDING, 1).attr(WIDTH, hints.getWidth()).attr(CELLSPACING, 0).with(
                bundleDolphinTr(bundle, hints.getWidth(), fold))

        )).render();

        logger.debug(html);
        return html;
    }

    /**
     * DolphinBundle の TR を作る. (列数=4)
     *
     * @param bundle BundleDolphin
     * @param width  width
     * @param fold   折りたたむかどうか
     * @return array of TR
     */
    public static Tag[] bundleDolphinTr(BundleDolphin bundle, int width, boolean fold) {
        List<Tag> trs = new ArrayList<>();

        if (fold) {
            String itemNames = bundle.getClaimItem() == null
                ? null
                : Stream.of(bundle.getClaimItem()).map(item -> {
                String num = item.getNumber();
                if (num == null || "".equals(num) || "1".equals(num)) { num = ""; }
                else { num = "(" + num + ")"; }
                return item.getName() + num;
            }).collect(Collectors.joining(","));

            trs.add(tr().with(
                td("・").attr(VALIGN, TOP).attr(WIDTH, MARKER_WIDTH),
                td(itemNames).attr(COLSPAN, 2).attr(WIDTH, width - 16), // trial and error
                td(" ")
            ));
        } else {
            for (ClaimItem item : bundle.getClaimItem()) {
                if (StringTool.isEmpty(item.getNumber())) {
                    trs.add(tr().with(
                        td("・").attr(VALIGN, TOP).attr(WIDTH, MARKER_WIDTH),
                        td(item.getName()).attr(COLSPAN, 2),
                        td(" ")
                    ));
                } else {
                    String number = item.getNumber(); //.replaceAll("[^0-9]0$", "");
                    String unit = Objects.isNull(item.getUnit()) ? "" : item.getUnit();
                    if ("1".equals(number) && "".equals(unit)) { number = ""; }

                    trs.add(tr().with(
                        td("・").attr(VALIGN, TOP).attr(WIDTH, MARKER_WIDTH),
                        td(item.getName()),
                        td( number + " " + unit).attr(NOWRAP).attr(WIDTH, AMOUNT_WIDTH + UNIT_WIDTH).attr(ALIGN, RIGHT),
                        td(" ").attr(WIDTH, UNIT_MARGIN)
                    ));
                }
            }
        }

        if (!StringTool.isEmpty(bundle.getMemo())) {
            trs.add(tr().with(
                td("　").attr(NOWRAP),
                td(bundle.getMemo()).attr(COLSPAN, 2),
                td(" ")
            ));
        }

        return trs.toArray(new Tag[0]);
    }

    /**
     * BundleDolphin を html 化する. 簡易表示バージョン.
     *
     * @param bundle    BundleDolphin
     * @param stampName スタンプ名
     * @param hints     StampRenderingHints
     * @return html
     */
    public static String bundleDolphin2HtmlLight(BundleDolphin bundle, String stampName, StampRenderingHints hints) {
        String memo = bundle.getClassCode();
        logger.debug("bundle class code = " + memo);

        String html = html().with(body().with(
            // タイトル部分
            table().attr(BORDER, 0).attr(CELLPADDING, 1).attr(WIDTH, hints.getWidth()).with(
                titleTr(bundle.getOrderName(), stampName, memo, hints.getWidth(), hints.getLabelColorAs16String())
            ),
            // 項目部分
            table().attr(BORDER, 0).attr(CELLPADDING, 1).attr(WIDTH, hints.getWidth()).attr(CELLSPACING, 0).with(
                bundleDolphinTrLight(bundle, hints.getWidth()))

        )).render();

        logger.debug(html);
        return html;
    }

    /**
     * DolphinBundle 簡易表示用の TR tag を作る.
     *
     * @param bundle BundleDolphin
     * @param width  width
     * @return array of TR
     */
    public static Tag bundleDolphinTrLight(BundleDolphin bundle, int width) {

        String itemNames = bundle.getClaimItem() == null
            ? null
            : Stream.of(bundle.getClaimItem()).map(item -> {
            String num = item.getNumber();
            if (num == null || "".equals(num) || "1".equals(num)) {
                num = "";
            } else {
                num = "(" + num + ")";
            }
            return item.getName() + num;
        }).collect(Collectors.joining(","));
        itemNames = StringUtils.truncate(itemNames, 30);

        return tr().with(
            td("・").attr(VALIGN, TOP).attr(WIDTH, MARKER_WIDTH),
            td(itemNames).attr(COLSPAN, 2).attr(WIDTH, width - 16), // trial and error
            td(" ")
        );
    }
}


