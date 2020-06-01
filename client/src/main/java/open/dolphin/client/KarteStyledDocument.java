package open.dolphin.client;

import open.dolphin.project.Project;

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
    // スタンプの先頭を改行する
    private boolean putTopCr;
    // KartePane
    private KartePane kartePane;

    public KarteStyledDocument() {
        putTopCr = Project.getPreferences().getBoolean("stampSpace", true);
    }

    public void setParent(KartePane kartePane) {
        this.kartePane = kartePane;
    }

    public void makeParagraph() {
        try {
            insertString(getLength(), "\n", null);
        } catch (BadLocationException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Stamp を挿入する.
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
            if (putTopCr) {
                insertString(start, "\n", null);
                insertString(start + 1, " ", runStyle);
                insertString(start + 2, "\n", null);                           // 改行をつけないとテキスト入力制御がやりにくくなる
                sh.setEntry(createPosition(start + 1), createPosition(start + 2)); // スタンプの開始と終了位置を生成して保存する
            } else {
                insertString(start, " ", runStyle);
                insertString(start + 1, "\n", null);                           // 改行をつけないとテキスト入力制御がやりにくくなる
                sh.setEntry(createPosition(start), createPosition(start + 1)); // スタンプの開始と終了位置を生成して保存する
            }
            removeRepeatedCr();

        } catch (BadLocationException | NullPointerException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Stamp を挿入する.
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
            //Style runStyle = addStyle(STAMP_STYLE, null);
            StyleConstants.setComponent(runStyle, sh);

            // キャレット位置を取得する
            int start = kartePane.getTextPane().getCaretPosition();

            // Stamp を挿入する
            insertString(start, " ", runStyle);

            // スタンプの開始と終了位置を生成して保存する
            sh.setEntry(createPosition(start), createPosition(start + 1));

        } catch (BadLocationException | NullPointerException ex) {
            ex.printStackTrace(System.err);
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
            ex.printStackTrace(System.err);
        }
    }

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
            ex.printStackTrace(System.err);
        }
    }

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
            ex.printStackTrace(System.err);
        }
    }

    public void insertTextStamp(String text) {
        try {
            int pos = kartePane.getTextPane().getCaretPosition();
            insertString(pos, text, null);

        } catch (BadLocationException e) {
            e.printStackTrace(System.err);
        }
    }

    public void insertFreeString(String text, AttributeSet a) {
        try {
            insertString(getLength(), text, a);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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

    // StampHolder直後の改行がない場合は補う
    public void fixCrAfterStamp() {
        try {
            int i = 0;
            while (i < getLength()) {
                StampHolder sh = (StampHolder) StyleConstants.getComponent(getCharacterElement(i).getAttributes());
                String strNext = getText(++i, 1);
                if (sh != null && !"\n".equals(strNext)) {
                    insertString(i, "\n", null);
                }

            }
        } catch (BadLocationException ex) {
        }
    }

    // 最終の改行を取り除く
    public void removeLastCr() {
        try {
            int endPos = getLength() - 1;
            String last = getText(endPos, 1);
            if ("\n".equals(last)) { remove(endPos, 1); }
        } catch (BadLocationException ex) {}
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
            ex.printStackTrace(System.err);
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
                    if (len > 1) {
                        remove(crPos + 1, len - 1);
                        pos -= (len - 1);
                    }
                    crPos = 0;
                }
                pos++;

            } catch (BadLocationException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }
}
