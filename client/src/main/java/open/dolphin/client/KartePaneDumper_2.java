package open.dolphin.client;

import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.SchemaModel;
import org.apache.log4j.Logger;

import javax.swing.text.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * KartePane の dumper.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class KartePaneDumper_2 {
    private static final String[] MATCHES = new String[]{"<", ">", "&", "'", "\""};
    private static final String[] REPLACES = new String[]{"&lt;", "&gt;", "&amp;", "&apos;", "&quot;"};
    private final Logger logger;
    private List<ModuleModel> moduleList;
    private List<SchemaModel> schemaList;
    // Document の内容を XML で表したもの.
    private String spec;

    public KartePaneDumper_2() {
        logger = Logger.getLogger(KartePaneDumper_2.class);
    }

    /**
     * ダンプした Document の XML 定義を返す.
     *
     * @return Documentの内容を XML で表したもの
     */
    public String getSpec() {
        logger.debug(spec);
        return spec;
    }

    /**
     * ダンプした Document に含まれている ModuleModel を返す.
     *
     * @return
     */
    public List<ModuleModel> getModule() {
        return Collections.unmodifiableList(moduleList);
    }

    /**
     * ダンプした Documentに含まれている SchemaModel を返す.
     *
     * @return
     */
    public List<SchemaModel> getSchema() {
        return Collections.unmodifiableList(schemaList);
    }

    /**
     * 引数の Document をダンプする.
     *
     * @param doc ダンプするドキュメント
     */
    public void dump(DefaultStyledDocument doc) {

        moduleList = new ArrayList<>();
        schemaList = new ArrayList<>();
        StringWriter sw = new StringWriter();

        try (BufferedWriter writer = new BufferedWriter(sw)) {
            // ルート要素から再帰的にダンプする
            Element root = doc.getDefaultRootElement();
            writeElemnt(root, writer);

            // 出力バッファーをフラッシュしペインのXML定義を生成する
            writer.flush();
            spec = sw.toString();

        } catch (IOException | BadLocationException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * 要素を再帰的にダンプする.
     *
     * @param element 要素
     * @param writer  出力ライター
     * @throws IOException
     * @throws BadLocationException
     */
    private void writeElemnt(Element element, Writer writer) throws IOException, BadLocationException {

        // 要素の開始及び終了のオフセット値を保存する
        int start = element.getStartOffset();
        int end = element.getEndOffset();
        logger.debug("start = " + start);
        logger.debug("end = " + end);

        // このエレメントの属性セットを得る
        AttributeSet atts = element.getAttributes().copyAttributes();

        // 属性値の文字列表現
        String asString = "";

        // 属性を調べる
        if (atts != null) {

            StringBuilder retBuffer = new StringBuilder();

            // 全ての属性を列挙する
            Enumeration<?> names = atts.getAttributeNames();

            while (names.hasMoreElements()) {

                // 属性の名前を得る
                Object nextName = names.nextElement();

                if (nextName != StyleConstants.ResolveAttribute) {
                    logger.debug("attribute name = " + nextName.toString());

                    // $enameは除外する
                    if (nextName.toString().startsWith("$")) {
                        continue;
                    }

                    // 属性= の形を準備する
                    retBuffer.append(" ");
                    retBuffer.append(nextName);
                    retBuffer.append("=");

                    // foreground 属性の場合は再構築の際に利用しやすい形に分解する
                    if (nextName.toString().equals("foreground")) {
                        Color c = (Color) atts.getAttribute(StyleConstants.Foreground);
                        logger.debug("color = " + c.toString());
                        StringBuilder buf = new StringBuilder();
                        buf.append(c.getRed());
                        buf.append(",");
                        buf.append(c.getGreen());
                        buf.append(",");
                        buf.append(c.getBlue());
                        retBuffer.append(addQuote(buf.toString()));

                    } else {
                        // 属性セットから名前をキーにして属性オブジェクトを取得する
                        Object attObject = atts.getAttribute(nextName);
                        logger.debug("attribute object = " + attObject.toString());

                        if (attObject instanceof StampHolder) {
                            // スタンプの場合
                            StampHolder sh = (StampHolder) attObject;
                            moduleList.add(sh.getStamp());
                            String value = String.valueOf(moduleList.size() - 1); // ペインに出現する順番をこの属性の値とする
                            retBuffer.append(addQuote(value));

                        } else if (attObject instanceof SchemaHolder) {
                            // シュェーマの場合
                            SchemaHolder ch = (SchemaHolder) attObject;
                            schemaList.add(ch.getSchema());
                            String value = String.valueOf(schemaList.size() - 1); // ペインに出現する順番をこの属性の値とする
                            retBuffer.append(addQuote(value));

                        } else {
                            // それ以外の属性についてはそのまま記録する
                            retBuffer.append(addQuote(attObject.toString()));
                        }
                    }
                }
            }
            asString = retBuffer.toString();
        }

        // <要素名 start="xx" end="xx" + asString>
        writer.write("<");
        writer.write(element.getName());
        writer.write(" start=");
        writer.write(addQuote(start));
        writer.write(" end=");
        writer.write(addQuote(end));
        writer.write(asString);
        writer.write(">");

        // content要素の場合はテキストを抽出する
        if (element.getName().equals("content")) {
            writer.write("<text>");
            int len = end - start;
            String text = element.getDocument().getText(start, len);
            logger.debug("text = " + text);

            // 特定の文字列を置換する
            for (int i = 0; i < REPLACES.length; i++) {
                text = text.replaceAll(MATCHES[i], REPLACES[i]);
            }
            writer.write(text);
            writer.write("</text>");

        }

        // 子要素について再帰する
        int children = element.getElementCount();
        for (int i = 0; i < children; i++) {
            writeElemnt(element.getElement(i), writer);
        }

        // この属性を終了する
        // </属性名>
        writer.write("</");
        writer.write(element.getName());
        writer.write(">");
    }

    private String addQuote(String string) {
        return "\"" + string + "\"";
    }

    private String addQuote(int number) {
        return "\"" + number + "\"";
    }
}
