package open.dolphin.client;

import open.dolphin.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.*;
import java.util.ArrayList;
import java.util.List;

/**
 * KartePane の StyledDocument class.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class KarteStyledDocument extends DefaultStyledDocument {
    private static final long serialVersionUID = 1L;
    private static final String STAMP_STYLE = "stampHolder";
    private static final String SCHEMA_STYLE = "schemaHolder";
    private Logger logger = LoggerFactory.getLogger(KarteStyledDocument.class);

    private KartePane kartePane;

    public KarteStyledDocument() { }

    public void setParent(KartePane kartePane) {
        this.kartePane = kartePane;
    }

    /**
     * 最後に改行を挿入する.
     */
    public void makeParagraph() {
        try {
            insertString(getLength(), "\n", null);
        } catch (BadLocationException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Stamp を挿入する. DnD の場合.
     *
     * @param sh 挿入するスタンプホルダ
     */
    public void stamp(final StampHolder sh) {

        try {
            Style runStyle = this.getStyle(STAMP_STYLE);
            if (runStyle == null) {
                runStyle = addStyle(STAMP_STYLE, null);
            }
            StyleConstants.setComponent(runStyle, sh);

            // キャレット位置を取得する
            int start = kartePane.getTextPane().getCaretPosition();

            // Stamp を挿入する
            if (toPutTopCr(start)) { insertString(start++, "\n", null); }
            insertString(start, " ", runStyle);

            // 改行をつけないとテキスト入力制御がやりにくくなる
            insertString(start + 1, "\n", null);

            // スタンプの開始と終了位置を生成して保存する
            sh.setEntry(createPosition(start), createPosition(start + 1));

        } catch (BadLocationException | NullPointerException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Stamp を挿入する. Rendering から呼ばれる場合.
     *
     * @param sh 挿入するスタンプホルダ
     */
    public void flowStamp(final StampHolder sh) {

        try {
            Style runStyle = this.getStyle(STAMP_STYLE);
            if (runStyle == null) {
                runStyle = addStyle(STAMP_STYLE, null);
            }
            // このスタンプ用のスタイルを動的に生成する
            StyleConstants.setComponent(runStyle, sh);

            // キャレット位置を取得する
            int start = kartePane.getTextPane().getCaretPosition();

            // Stamp を挿入する
            insertString(start, " ", runStyle);
            sh.setEntry(createPosition(start), createPosition(start + 1));
            if (Project.getProjectStub().isStampSpace()) {
                insertString(start + 1, "\n", null);
            }

        } catch (BadLocationException | NullPointerException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Stampを削除する.
     *
     * @param start 削除開始のオフセット位置
     * @param len length
     */
    public void removeStamp(int start, int len) {
        try {
//masuda^   Stamp/Schemaをremoveするときは直後の改行も削除する
            // Stamp は一文字で表されている
            //remove(start, 1);
            if (start < getLength() - 1 && "\n".equals(getText(start + 1, 1))) {
                remove(start, 2);
            } else {
                remove(start, 1);
            }
//masuda$
        } catch (BadLocationException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Schema を挿入する.
     *
     * @param sc SchemaHolder
     */
    public void stampSchema(SchemaHolder sc) {
        try {
            Style runStyle = this.getStyle(SCHEMA_STYLE);
            if (runStyle == null) {
                runStyle = addStyle(SCHEMA_STYLE, null);
            }
            // このスタンプ用のスタイルを動的に生成する
            //Style runStyle = addStyle(SCHEMA_STYLE, null);
            StyleConstants.setComponent(runStyle, sc);

            // Stamp同様
            int start = kartePane.getTextPane().getCaretPosition();
            insertString(start, " ", runStyle);
            insertString(start + 1, "\n", null);
            sc.setEntry(createPosition(start), createPosition(start + 1));

        } catch (BadLocationException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Schema を挿入する. Rendering から呼ばれる場合.
     * @param sh SchemaHolder
     */
    public void flowSchema(final SchemaHolder sh) {
        try {
            Style runStyle = this.getStyle(SCHEMA_STYLE);
            if (runStyle == null) {
                runStyle = addStyle(SCHEMA_STYLE, null);
            }
            // このスタンプ用のスタイルを動的に生成する
            //Style runStyle = addStyle(SCHEMA_STYLE, null);
            StyleConstants.setComponent(runStyle, sh);

            // キャレット位置を取得する
            int start = kartePane.getTextPane().getCaretPosition();

            // Stamp を挿入する
            insertString(start, " ", runStyle);

            // スタンプの開始と終了位置を生成して保存する
            sh.setEntry(createPosition(start), createPosition(start + 1));

        } catch (BadLocationException | NullPointerException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * 現在位置に Text を挿入する.
     *
     * @param text 文字列
     */
    public void insertTextStamp(String text) {
        try {
            int pos = kartePane.getTextPane().getCaretPosition();
            insertString(pos, text, null);

        } catch (BadLocationException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * 現在位置に Attribute 付きの Text を挿入する.
     *
     * @param text 文字列
     * @param a AttributeSet
     */
    public void insertFreeString(String text, AttributeSet a) {
        try {
            insertString(getLength(), text, a);
        } catch (BadLocationException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * スタンプの直前に Cr を挿入するかどうか.
     *
     * @param pos 挿入しようとしている場所
     * @return スペースを挿入する場合 true
     */
    private boolean toPutTopCr(int pos) throws BadLocationException {
        // Preferences で挿入しない設定になっていたら false
        if (!Project.getProjectStub().isStampSpace()) { return false; }

        Document doc = kartePane.getTextPane().getDocument();
        return (pos == 1 && !doc.getText(0, 1).equals("\n")
            || (pos > 1 && !doc.getText(pos - 2, 2).equals("\n\n")));
    }

    //masuda^   KarteStyledDocument内のStampHolderを取得する. pns先生のコード
    public List<StampHolder> getStampHolders() {
        List<StampHolder> list = new ArrayList<>();
        int length = getLength();
        for (int i = 0; i < length; ++i) {
            StampHolder sh = (StampHolder) StyleConstants.getComponent(getCharacterElement(i).getAttributes());
            if (sh != null) {
                list.add(sh);
            }
        }
        return list;
    }

    // 最終の改行を取り除く
    public void removeLastCr() {
        try {
            int endPos = getLength() - 1;
            String last = getText(endPos, 1);
            if ("\n".equals(last)) { remove(endPos, 1); }

        } catch (BadLocationException ex) { logger.error(ex.getMessage()); }
    }

    /**
     * 文頭・文末の無駄な改行文字を削除する.
     * original by masuda-sensei
     */
    public void removeExtraCr() {
        // これが一番速い！ 20個の改行削除に2msec!!
        try {
            int len = getLength();
            int pos;
            // 改行文字以外が出てくるまで文頭からスキャン
            for (pos = 0; pos < len - 1; pos++) {
                if (!"\n".equals(getText(pos, 1))) {
                    break;
                }
            }
            if (pos > 0) { remove(0, pos); }

            len = getLength();
            // 改行文字以外が出てくるまで文書末からスキャン
            for (pos = len - 1; pos >= 0; --pos) {
                if (!"\n".equals(getText(pos, 1))) {
                    break;
                }
            }
            ++pos;  // 一文字戻す
            if (len - pos > 0) {
                remove(pos, len - pos);
            }
        } catch (BadLocationException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * 3個以上連続する改行を2個にする.
     * つまり，２つ以上連続する空行を１つにする.
     */
    public void removeRepeatedCr() {
        int pos = 0;
        int crPos = 0;

        while (pos < getLength()) {
            try {
                if (crPos == 0 && "\n".equals(getText(pos, 1))) {
                    crPos = pos;
                }
                if (crPos != 0 && !"\n".equals(getText(pos, 1))) {
                    int len = pos - crPos;
                    if (len > 2) {
                        remove(crPos + 1, len - 2);
                        pos -= (len - 2);
                    }
                    crPos = 0;
                }
                pos++;

            } catch (BadLocationException ex) {
                logger.error(ex.getMessage());
            }
        }
    }
}
