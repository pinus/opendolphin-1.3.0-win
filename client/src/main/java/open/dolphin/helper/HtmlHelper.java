package open.dolphin.helper;

import j2html.attributes.Attr;
import j2html.tags.Tag;
import open.dolphin.client.StampRenderingHints;
import open.dolphin.infomodel.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static j2html.TagCreator.*;

/**
 * j2html で html を作る.
 *
 * @author pns
 */
public class HtmlHelper {
    private static Logger logger = Logger.getLogger(HtmlHelper.class);

    //static { logger.setLevel(Level.DEBUG); }

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
            table().attr(BORDER, 0).attr(CELLPADDING, 1).with(
                titleTr("RP", stampName, memo, hints.getWidth(), hints.getLabelColorAs16String())
            ),
            // 項目部分 列数=5
            table().attr(BORDER, 0).attr(CELLPADDING, 1).attr(CELLSPACING, 0).with(
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
            td(orderName + "）").attr(VALIGN, TOP).attr(NOWRAP),
            td(stampName).attr(VALIGN, TOP).attr(WIDTH, width),
            td().with(tag(FONT).attr(SIZE, -2).withText(memo))
                .attr(ALIGN, RIGHT).attr(ALIGN, BOTTOM).attr(NOWRAP)
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
            if (item.getCode().matches("0085[0-9]{5}")
                || item.getCode().matches("001000[0-9]{3}")
                || item.getCode().matches("0992099[0-9]{2}")) {
                trs.add(tr().with(
                    td("　"),
                    td(item.getName()).attr(COLSPAN, 3).attr(WIDTH, width),
                    td(" ")));

            } else if (item.getCode().matches("810000001")) {
                trs.add(tr().with(
                    td("　"),
                    td().attr(COLSPAN, 3).with(tag(FONT).attr(COLOR, color).withText(item.getName())),
                    td(" ")));

            } else {
                trs.add(tr().with(
                    td("・").attr(VALIGN, TOP).attr(WIDTH, 12),
                    td(item.getName()).attr(WIDTH, width),
                    td(item.getNumber()).attr(ALIGN, RIGHT).attr(VALIGN, BOTTOM).attr(NOWRAP),
                    td(item.getUnit() + " ").attr(ALIGN, RIGHT).attr(VALIGN, BOTTOM).attr(NOWRAP),
                    td(" ")));
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
                td(bundle.getAdmin()).attr(COLSPAN, 3).attr(WIDTH, hints.getWidth()),
                td(" "));
        }
        // それ以外は "日分/回分" を付ける
        String admin = " " + bundle.getBundleNumber()
            + (bundle.getClassCode().startsWith(IInfoModel.RECEIPT_CODE_NAIYO.substring(0, 2)) ? " 日分" : " 回分");
        return tr().with(
            td("　"),
            td(bundle.getAdmin()).attr(WIDTH, hints.getWidth()),
            td(admin).attr(COLSPAN, 2).attr(NOWRAP),
            td(" "));
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
            table().attr(BORDER, 0).attr(CELLPADDING, 1).with(
                titleTr(bundle.getOrderName(), stampName, memo, hints.getWidth(), hints.getLabelColorAs16String())
            ),
            // 項目部分
            table().attr(BORDER, 0).attr(CELLPADDING, 1).attr(CELLSPACING, 0).with(
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
            trs.add(tr().with(
                td("・").attr(VALIGN, TOP),
                td(bundle.getItemNames()).attr(COLSPAN, 2).attr(WIDTH, width),
                td(" ")
            ));
        } else {
            for (ClaimItem item : bundle.getClaimItem()) {
                if (StringTool.isEmpty(item.getNumber())) {
                    trs.add(tr().with(
                        td("・").attr(VALIGN, TOP),
                        td(item.getName()).attr(COLSPAN, 2).attr(WIDTH, width),
                        td(" ")
                    ));
                } else {
                    String number = item.getNumber(); //.replaceAll("[^0-9]0$", "");
                    String unit = Objects.isNull(item.getUnit()) ? "" : item.getUnit();
                    if ("1".equals(number) && "".equals(unit)) { number = ""; }

                    trs.add(tr().with(
                        td("・").attr(VALIGN, TOP),
                        td(item.getName()).attr(WIDTH, width),
                        td( number +  " " + unit).attr(NOWRAP),
                        td(" ")
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
}


