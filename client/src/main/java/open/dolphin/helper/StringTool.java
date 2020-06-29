package open.dolphin.helper;

import java.util.Objects;

/**
 * Utilities to handle String.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public final class StringTool {

    private static final Character[] HIRAGANA = {'ぁ', 'ゟ'};
    private static final Character[] KATAKANA = {'ァ', 'ヾ'};

    private static final Character[] ZENKAKU_UPPER = {'Ａ', 'Ｚ'};
    private static final Character[] ZENKAKU_LOWER = {'ａ', 'ｚ'};
    private static final Character[] ZENKAKU_NUMBER = {'０', '９'};

    private static final Character[] HANKAKU_UPPER = {'A', 'Z'};
    private static final Character[] HANKAKU_LOWER = {'a', 'z'};
    private static final Character[] HANKAKU_NUMBER = {'0', '9'};

    private static final char[] ZENKAKU_KATAKANA = {'ァ', 'ア', 'ィ', 'イ', 'ゥ',
            'ウ', 'ェ', 'エ', 'ォ', 'オ', 'カ', 'ガ', 'キ', 'ギ', 'ク', 'グ', 'ケ', 'ゲ',
            'コ', 'ゴ', 'サ', 'ザ', 'シ', 'ジ', 'ス', 'ズ', 'セ', 'ゼ', 'ソ', 'ゾ', 'タ',
            'ダ', 'チ', 'ヂ', 'ッ', 'ツ', 'ヅ', 'テ', 'デ', 'ト', 'ド', 'ナ', 'ニ', 'ヌ',
            'ネ', 'ノ', 'ハ', 'バ', 'パ', 'ヒ', 'ビ', 'ピ', 'フ', 'ブ', 'プ', 'ヘ', 'ベ',
            'ペ', 'ホ', 'ボ', 'ポ', 'マ', 'ミ', 'ム', 'メ', 'モ', 'ャ', 'ヤ', 'ュ', 'ユ',
            'ョ', 'ヨ', 'ラ', 'リ', 'ル', 'レ', 'ロ', 'ヮ', 'ワ', 'ヰ', 'ヱ', 'ヲ', 'ン',
            'ヴ', 'ヵ', 'ヶ'};

    private static final String[] HANKAKU_KATAKANA = {"ｧ", "ｱ", "ｨ", "ｲ", "ｩ",
            "ｳ", "ｪ", "ｴ", "ｫ", "ｵ", "ｶ", "ｶﾞ", "ｷ", "ｷﾞ", "ｸ", "ｸﾞ", "ｹ",
            "ｹﾞ", "ｺ", "ｺﾞ", "ｻ", "ｻﾞ", "ｼ", "ｼﾞ", "ｽ", "ｽﾞ", "ｾ", "ｾﾞ", "ｿ",
            "ｿﾞ", "ﾀ", "ﾀﾞ", "ﾁ", "ﾁﾞ", "ｯ", "ﾂ", "ﾂﾞ", "ﾃ", "ﾃﾞ", "ﾄ", "ﾄﾞ",
            "ﾅ", "ﾆ", "ﾇ", "ﾈ", "ﾉ", "ﾊ", "ﾊﾞ", "ﾊﾟ", "ﾋ", "ﾋﾞ", "ﾋﾟ", "ﾌ",
            "ﾌﾞ", "ﾌﾟ", "ﾍ", "ﾍﾞ", "ﾍﾟ", "ﾎ", "ﾎﾞ", "ﾎﾟ", "ﾏ", "ﾐ", "ﾑ", "ﾒ",
            "ﾓ", "ｬ", "ﾔ", "ｭ", "ﾕ", "ｮ", "ﾖ", "ﾗ", "ﾘ", "ﾙ", "ﾚ", "ﾛ", "ﾜ",
            "ﾜ", "ｲ", "ｴ", "ｦ", "ﾝ", "ｳﾞ", "ｶ", "ｹ"};

    private static final char ZENKAKU_KATAKANA_FIRST_CHAR = ZENKAKU_KATAKANA[0];

    private static final char ZENKAKU_KATAKANA_LAST_CHAR = ZENKAKU_KATAKANA[ZENKAKU_KATAKANA.length - 1];

    private StringTool() {
    }

    /**
     * 文字 c がカタカナかどうか. 長音記号はカタカナに含む.
     *
     * @param c 文字
     * @return true if カタカナ
     */
    public static boolean isKatakana(char c) {
        // ア 12449 (12353 半角)
        // ン 12531　ヴ 12532
        Character test = c;
        return (test.compareTo(KATAKANA[0]) >= 0 && test.compareTo(KATAKANA[1]) <= 0) || c == '-' || c == '−' || c == 'ー';
    }

    /**
     * 文字列 target が全てカタカナかどうか.
     *
     * @param target 文字列
     * @return true if all カタカナ
     */
    public static boolean isAllKatakana(String target) {
        return target.chars().allMatch(c -> isKatakana((char) c));
    }

    /**
     * 文字 c がひらがなかどうか.
     *
     * @param c 文字
     * @return true if ひらがな
     */
    public static boolean isHiragana(char c) {
        // あ 12354
        // ん 12435
        Character test = c;
        return (test.compareTo(HIRAGANA[0]) >= 0 && test.compareTo(HIRAGANA[1]) <= 0);
    }

    /**
     * 文字 c がひらがななら，カタカナに変換する.
     *
     * @param c 文字
     * @return カタカナ
     */
    private static char toKatakana(char c) {
        return isHiragana(c) ? (char) (c + 96) : c;
    }

    /**
     * 文字列 s のひらがな部分をカタカナに変換する.
     *
     * @param s 文字列
     * @return 変換した文字列
     */
    public static String hiraganaToKatakana(String s) {
        int len = s.length();
        char[] src = new char[len];
        s.getChars(0, s.length(), src, 0);
        char[] dst = new char[len];
        for (int i = 0; i < len; i++) {
            dst[i] = toKatakana(src[i]);
        }
        return new String(dst);
    }

    /**
     * 文字列 target が全て半角数字かどうか.
     *
     * @param target 文字列
     * @return true if all 半角数字
     */
    public static boolean isAllDigit(String target) {
        return target.chars().allMatch(Character::isDigit);
    }

    /**
     * 文字 c が全角の大文字アルファベットかどうか.
     *
     * @param c 文字
     * @return true if 全角大文字アルファベット
     */
    public static boolean isZenkakuUpper(char c) {
        Character test = c;
        return (test.compareTo(ZENKAKU_UPPER[0]) >= 0 && test.compareTo(ZENKAKU_UPPER[1]) <= 0);
    }

    /**
     * 文字 c が全角の小文字アルファベットかどうか.
     *
     * @param c 文字
     * @return true if 全角小文字アルファベット
     */
    public static boolean isZenkakuLower(char c) {
        Character test = c;
        return (test.compareTo(ZENKAKU_LOWER[0]) >= 0 && test.compareTo(ZENKAKU_LOWER[1]) <= 0);
    }

    /**
     * 文字 c が全角の数字かどうか.
     *
     * @param c 文字
     * @return true if 全角数字
     */
    public static boolean isZenkakuNumber(char c) {
        Character test = c;
        return (test.compareTo(ZENKAKU_NUMBER[0]) >= 0 && test.compareTo(ZENKAKU_NUMBER[1]) <= 0);
    }

    /**
     * 文字 c が半角の大文字アルファベットかどうか.
     *
     * @param c 文字
     * @return true if 半角大文字アルファベット
     */
    public static boolean isHankakuUpper(char c) {
        Character test = c;
        return (test.compareTo(HANKAKU_UPPER[0]) >= 0 && test.compareTo(HANKAKU_UPPER[1]) <= 0);
    }

    /**
     * 文字 c が半角の小文字アルファベットかどうか.
     *
     * @param c 文字
     * @return true if 半角小文字アルファベット
     */
    public static boolean isHanakuLower(char c) {
        Character test = c;
        return (test.compareTo(HANKAKU_LOWER[0]) >= 0 && test.compareTo(HANKAKU_LOWER[1]) <= 0);
    }

    /**
     * 文字 c が半角の数字かどうか.
     *
     * @param c 文字
     * @return true if 半角数字
     */
    public static boolean isHankakuNumber(char c) {
        Character test = c;
        return test.compareTo(HANKAKU_NUMBER[0]) >= 0 && test.compareTo(HANKAKU_NUMBER[1]) <= 0;
    }

    /**
     * 半角または全角のスペースかどうか.
     *
     * @param c 文字
     * @return true if 半角 or 全角スペース
     */
    public static boolean isSpace(char c) {
        Character test = c;
        return (test.equals(' ') || test.equals('　'));
    }

    /**
     * 文字列 s の半角アルファベットを全角に変換する.
     *
     * @param s 文字列
     * @return 変換した文字列
     */
    public static String toZenkakuUpperLower(String s) {
        int len = s.length();
        char[] src = new char[len];
        s.getChars(0, s.length(), src, 0);

        StringBuilder sb = new StringBuilder();
        for (char c : src) {
            if (isHankakuUpper(c) || isHanakuLower(c)) {
                sb.append((char) (c + 65248));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 文字列 s の全角アルファベットを半角に変換する.
     *
     * @param s 文字列
     * @return 変換した文字列
     */
    public static String toHankakuUpperLower(String s) {
        int len = s.length();
        char[] src = new char[len];
        s.getChars(0, s.length(), src, 0);
        StringBuilder sb = new StringBuilder();
        for (char c : src) {
            if (isZenkakuUpper(c) || isZenkakuLower(c)) {
                sb.append((char) (c - 65248));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 文字列 s の半角数字を全角に変換する.
     *
     * @param s 文字列
     * @return 変換した文字列
     */
    public static String toZenkakuNumber(String s) {
        int len = s.length();
        char[] src = new char[len];
        s.getChars(0, s.length(), src, 0);
        StringBuilder sb = new StringBuilder();
        for (char c : src) {
            if (isHankakuNumber(c)) {
                sb.append((char) (c + 65248));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 文字列 s の全角数字を半角に変換する.
     *
     * @param s 文字列
     * @return 変換した文字列
     */
    public static String toHankakuNumber(String s) {
        int len = s.length();
        char[] src = new char[len];
        s.getChars(0, s.length(), src, 0);
        StringBuilder sb = new StringBuilder();
        for (char c : src) {
            if (isZenkakuNumber(c)) {
                sb.append((char) (c - 65248));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 文字列をシングルクオートで囲む.
     *
     * @param s 文字列
     * @return 変換した文字列
     */
    public static String addSingleQuote(String s) {
        return String.format("'%s'", s);
    }

    /**
     * 文字列をシングルクオートで囲み，コンマをつける.
     *
     * @param s 文字列
     * @return 変換した文字列
     */
    public static String addSingleQuoteComma(String s) {
        return String.format("'%s',", s);
    }

    /**
     * 全角カタカナを半角カタカナに変換.
     * Java Master から引用.
     *
     * @param c 全角カタカナ文字
     * @return 半角カタカナ文字
     */
    public static String zenkakuKatakanaToHankakuKatakana(char c) {
        if (c >= ZENKAKU_KATAKANA_FIRST_CHAR && c <= ZENKAKU_KATAKANA_LAST_CHAR) {
            return HANKAKU_KATAKANA[c - ZENKAKU_KATAKANA_FIRST_CHAR];
        } else {
            return String.valueOf(c);
        }
    }

    /**
     * 全角カタカナ文字列を半角カタカナ文字列に変換.
     * Java Master から引用.
     *
     * @param s 全角カタカナ文字列
     * @return 半角カタカナ文字列
     */
    public static String zenkakuKatakanaToHankakuKatakana(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char originalChar = s.charAt(i);
            String convertedChar = zenkakuKatakanaToHankakuKatakana(originalChar);
            sb.append(convertedChar);
        }
        return sb.toString();
    }

    /**
     * 文字変数 str が空かどうかを返す.
     *
     * @param str 文字列
     * @return null or "" で空と判断
     */
    public static boolean isEmpty(String str) {
        return Objects.isNull(str) || str.length() == 0;
    }
}
