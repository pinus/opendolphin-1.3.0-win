package open.dolphin.client;

import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.ProgressCourse;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.*;

/**
 * KarteRenderer_2.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class KarteRenderer_2 {

    private static final String COMPONENT_ELEMENT_NAME = "component";

    private static final String STAMP_HOLDER = "stampHolder";

    private static final String SCHEMA_HOLDER = "schemaHolder";

    private static final int TT_SECTION = 0;

    private static final int TT_PARAGRAPH = 1;

    private static final int TT_CONTENT = 2;

    private static final int TT_ICON = 3;

    private static final int TT_COMPONENT = 4;

    private static final int TT_PROGRESS_COURSE = 5;

    private static final String SECTION_NAME = "section";

    private static final String PARAGRAPH_NAME = "paragraph";

    private static final String CONTENT_NAME = "content";

    private static final String COMPONENT_NAME = "component";

    private static final String ICON_NAME = "icon";

    private static final String ALIGNMENT_NAME = "Alignment";

    private static final String FOREGROUND_NAME = "foreground";

    private static final String SIZE_NAME = "size";

    private static final String BOLD_NAME = "bold";

    private static final String ITALIC_NAME = "italic";

    private static final String UNDERLINE_NAME = "underline";

    private static final String TEXT_NAME = "text";

    private static final String NAME_NAME = "name";

    private static final String LOGICAL_STYLE_NAME = "logicalStyle";

    private static final String PROGRESS_COURSE_NAME = "kartePane";

    private static final String[] REPLACES = new String[]{"<", ">", "&", "'", "\""};

    private static final String[] MATCHES = new String[]{"&lt;", "&gt;", "&amp;", "&apos;", "&quot;"};

    private static final String NAME_STAMP_HOLDER = "name=\"stampHolder\"";

    private int paragraphStart;
    private MutableAttributeSet paragraphAtts;

    private final KartePane soaPane;
    private final KartePane pPane;
    private final Logger logger;
    private DocumentModel model;
    private KartePane thePane;
    private boolean isSoaPane;

    private List<ModuleModel> soaModules;
    private List<ModuleModel> pModules;

    public KarteRenderer_2(KartePane soaPane, KartePane pPane) {
        this.soaPane = soaPane;
        this.pPane = pPane;
        logger = Logger.getLogger(KarteRenderer_2.class);
    }

    /**
     * DocumentModel をレンダリングする.
     *
     * @param model レンダリングする DocumentModel
     */
    public void render(DocumentModel model) {

        this.model = model;

        Collection<ModuleModel> modules = model.getModules();

        // SOA と P のモジュールをわける
        // また夫々の Pane の spec を取得する
        soaModules = new ArrayList<>();
        pModules = new ArrayList<>();
        String soaSpec = null;
        String pSpec = null;

        for (ModuleModel bean : modules) {

            String role = bean.getModuleInfo().getStampRole();

            switch (role) {
                case IInfoModel.ROLE_SOA:
                    soaModules.add(bean);
                    break;
                case IInfoModel.ROLE_SOA_SPEC:
                    soaSpec = ((ProgressCourse) bean.getModel()).getFreeText();
                    break;
                case IInfoModel.ROLE_P:
                    pModules.add(bean);
                    break;
                case IInfoModel.ROLE_P_SPEC:
                    pSpec = ((ProgressCourse) bean.getModel()).getFreeText();
                    break;
            }
        }

        if (soaSpec != null && pSpec != null) {
            int index = soaSpec.indexOf(NAME_STAMP_HOLDER);
            if (index > 0) {
                String sTmp = soaSpec;
                soaSpec = pSpec;
                pSpec = sTmp;
            }
        }

        // SOA Pane をレンダリングする
        if (soaSpec == null || soaSpec.equals("")) {
            soaModules.forEach(moduleModel -> {
                soaPane.stamp(moduleModel);
                soaPane.makeParagraph();
            });
            // モジュールのみの場合も dirty にセット
            if (soaModules != null) {
                soaPane.setDirty(true);
            }

        } else {
            debug("Render SOA Pane");
            debug("Module count = " + soaModules.size());
            isSoaPane = true;
            thePane = soaPane;
            renderPane(soaSpec);
        }

        // P Pane をレンダリングする
        if (pSpec == null || pSpec.equals("")) {
            // 前回処方適用のようにモジュールのみの場合
            pModules.forEach(moduleModel -> {
                //pPane.stamp(mm);
                pPane.flowStamp(moduleModel);
                pPane.makeParagraph();
                pPane.makeParagraph();
            });
            // モジュールだけの場合も dirty にセット
            if (pModules != null && pPane != null) {
                pPane.setDirty(true);
            }

        } else {
            isSoaPane = false;
            thePane = pPane;
            renderPane(pSpec);

            // StampHolder直後の改行がない場合は補う
            pPane.getDocument().fixCrAfterStamp();
        }

        // 最後の CR は attribute が変になってるので取り除く
        soaPane.getDocument().removeLastCr();
        pPane.getDocument().removeLastCr();
    }

    /**
     * TextPane Dump の XML を解析する.
     *
     * @param xml TextPane Dump の XML
     */
    private void renderPane(String xml) {

        debug(xml);

        SAXBuilder docBuilder = new SAXBuilder();
        StringReader sr = new StringReader(xml);

        try (BufferedReader br = new BufferedReader(sr)) {
            Document doc = docBuilder.build(br);
            Element root = doc.getRootElement();

            writeChildren(root);
        }
        // indicates a well-formedness error
        catch (JDOMException | IOException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 子要素をパースする.
     *
     * @param current 要素
     */
    private void writeChildren(Element current) {
        int eType = -1;
        String eName = current.getName();

        switch (eName) {
            case SECTION_NAME:
                eType = TT_SECTION;
                startSection();
                break;

            case PARAGRAPH_NAME:
                eType = TT_PARAGRAPH;
                startParagraph(current.getAttributeValue(ALIGNMENT_NAME));
                break;

            case CONTENT_NAME:
                if (Objects.nonNull(current.getChild(TEXT_NAME))) {
                    eType = TT_CONTENT;
                    startContent(
                            current.getAttributeValue(FOREGROUND_NAME),
                            current.getAttributeValue(SIZE_NAME),
                            current.getAttributeValue(BOLD_NAME),
                            current.getAttributeValue(ITALIC_NAME),
                            current.getAttributeValue(UNDERLINE_NAME),
                            current.getChildText(TEXT_NAME));
                }
                break;

            case COMPONENT_NAME:
                eType = TT_COMPONENT;
                startComponent(
                        current.getAttributeValue(NAME_NAME),
                        current.getAttributeValue(COMPONENT_ELEMENT_NAME));
                break;

            case ICON_NAME:
                eType = TT_ICON;
                startIcon(current);
                break;

            case PROGRESS_COURSE_NAME:
                eType = TT_PROGRESS_COURSE;
                startProgressCourse();
                break;

            default:
                debug("Other element:" + eName);
        }

        // 子を探索するのはパラグフとトップ要素のみ
        if (eType == TT_SECTION
                || eType == TT_PARAGRAPH
                || eType == TT_PROGRESS_COURSE)  {
            current.getChildren().stream().forEach(this::writeChildren);
        }

        switch (eType) {
            case TT_SECTION:
                endSection();
                break;

            case TT_PARAGRAPH:
                endParagraph();
                break;

            case TT_PROGRESS_COURSE:
                endProgressCourse();
                break;
        }
    }

    private void startSection() { }

    private void endSection() { }

    private void startParagraph(String alignStr) {
        DefaultStyledDocument doc = (DefaultStyledDocument) thePane.getTextPane().getDocument();
        paragraphStart = doc.getLength();
        paragraphAtts = new SimpleAttributeSet();

        if (alignStr != null) {
            switch (alignStr) {
                case "0":
                    StyleConstants.setAlignment(paragraphAtts, StyleConstants.ALIGN_LEFT);
                    break;
                case "1":
                    StyleConstants.setAlignment(paragraphAtts, StyleConstants.ALIGN_CENTER);
                    break;
                case "2":
                    StyleConstants.setAlignment(paragraphAtts, StyleConstants.ALIGN_RIGHT);
                    break;
            }
        }
    }

    private void endParagraph() {
        DefaultStyledDocument doc = (DefaultStyledDocument) thePane.getTextPane().getDocument();
        int pos = doc.getLength();
        doc.setParagraphAttributes(paragraphStart, pos - paragraphStart - 1, paragraphAtts, false);
    }

    private void startContent(String foreground, String size, String bold,
                              String italic, String underline, String text) {
        // 特殊文字を戻す
        for (int i = 0; i < REPLACES.length; i++) {
            text = text.replaceAll(MATCHES[i], REPLACES[i]);
        }

        // このコンテントに設定する AttributeSet
        MutableAttributeSet atts = new SimpleAttributeSet();

        // foreground 属性を設定する
        if (foreground != null) {
            StringTokenizer stk = new StringTokenizer(foreground, ",");
            if (stk.hasMoreTokens()) {
                int r = Integer.parseInt(stk.nextToken());
                int g = Integer.parseInt(stk.nextToken());
                int b = Integer.parseInt(stk.nextToken());
                StyleConstants.setForeground(atts, new Color(r, g, b));
            }
        }

        // size 属性を設定する
        if (size != null) {
            StyleConstants.setFontSize(atts, Integer.parseInt(size));
        }

        // bold 属性を設定する
        if (bold != null) {
            StyleConstants.setBold(atts, Boolean.valueOf(bold));
        }

        // italic 属性を設定する
        if (italic != null) {
            StyleConstants.setItalic(atts, Boolean.valueOf(italic));
        }

        // underline 属性を設定する
        if (underline != null) {
            StyleConstants.setUnderline(atts, Boolean.valueOf(underline));
        }

        // テキストを挿入する
        thePane.insertFreeString(text, atts);
    }

    private void startComponent(String name, String number) {
        debug("Entering startComponent");
        debug("Name = " + name);
        debug("Number = " + number);
        debug("soaPane = " + isSoaPane);

        try {
            if (name != null && name.equals(STAMP_HOLDER)) {
                int index = Integer.parseInt(number);
                ModuleModel stamp = isSoaPane
                        ? soaModules.get(index)
                        : pModules.get(index);
                thePane.flowStamp(stamp);

            } else if (name != null && name.equals(SCHEMA_HOLDER)) {
                int index = Integer.parseInt(number);
                thePane.flowSchema(model.getSchema(index));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    private void endComponent() { }

    private void startProgressCourse() { }

    private void endProgressCourse() { }

    private void startIcon(Element current) {
        String name = current.getChildTextTrim("name");
        if (name != null) {
            debug(name);
        }
    }

    private void debug(String msg) {
        logger.debug(msg);
    }
}
