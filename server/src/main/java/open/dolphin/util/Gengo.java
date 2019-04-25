package open.dolphin.util;

import java.util.Arrays;

/**
 * Gengo 元号Enum.
 *
 * @author pns
 */
public enum Gengo {
    DEFAULT("H", "㍻", "平成"),
    MEIJI("M", "㍾", "明治"),
    TAISHO("T", "㍽", "大正"),
    SHOWA("S", "㍼", "昭和"),
    HEISEI("H", "㍻", "平成"),
    REIWA("R", String.valueOf('\u32ff'), "令和");

    String alphabet, halfKanji, kanji;

    Gengo(String alphabet, String halfKanji, String kanji) {
        this.alphabet = alphabet;
        this.halfKanji = halfKanji;
        this.kanji = kanji;
    }

    /**
     * ISO_DATE -> 元号変換.
     *
     * @param isoDate 1975-01-01
     * @return 元号 S50-01-01
     */
    public static String isoDateToGengo(String isoDate) {
        int year;
        int month;
        int day;
        Gengo gengo;

        year = Integer.valueOf(isoDate.substring(0, 4));
        month = Integer.valueOf(isoDate.substring(5, 7));
        day = Integer.valueOf(isoDate.substring(8, 10));

        // 2020年より先は令和
        if (year >= 2020) {
            gengo = REIWA;
            year -= 2018;
        }
        // 2019年だったら，4月30日以前は平成
        else if (year == 2019) {
            if (month <= 4) {
                gengo = HEISEI;
                year -= 1988;
            } else {
                gengo = REIWA;
                year -= 2018;
            }
        }
        // 1990年より先は平成
        else if (year >= 1990) {
            gengo = HEISEI;
            year -= 1988;
        }
        // 1989年だったら，1月7日以前は昭和
        else if (year == 1989) {
            if (month == 1 && day <= 7) {
                gengo = SHOWA;
                year = 64;
            } else {
                gengo = HEISEI;
                year = 1;
            }
        }
        // 1927年から1988年は昭和
        else if (year >= 1927) {
            gengo = SHOWA;
            year -= 1925;
        }
        // 1926年だったら，12月25日以降は昭和
        else if (year == 1926) {
            if (month == 12 && day >= 25) {
                gengo = SHOWA;
                year = 1;
            } else {
                gengo = TAISHO;
                year = 15;
            }
        }
        // 1913年から1925年は大正
        else if (year >= 1913) {
            gengo = TAISHO;
            year -= 1911;
        }
        // 1912 年だったら，7/30 以降は大正
        else if (year == 1912) {
            if (month >= 8) {
                gengo = TAISHO;
                year = 1;
            } else if (month <= 6) {
                gengo = MEIJI;
                year = 45;
            } else if (day >= 30) {
                gengo = TAISHO;
                year = 1;
            } else {
                gengo = MEIJI;
                year = 45;
            }
        }
        // 1911年以前は明治
        else {
            gengo = MEIJI;
            year -= 1867;
        }

        return String.format("%s%02d-%02d-%02d", gengo.alphabet(), year, month, day);
    }

    /**
     * ISO_DATE を "元号xx年x月x日" に変換.
     *
     * @param isoDate ISO_DATE
     * @return 元号
     */
    public static String isoDateToFullGengo(String isoDate) {
        String[] gengo = isoDateToGengo(isoDate).split("-");
        String g = gengoAlphabetToKanji(gengo[0].substring(0, 1));
        int y = Integer.valueOf(gengo[0].substring(1));
        int m = Integer.valueOf(gengo[1]);
        int d = Integer.valueOf(gengo[2]);
        return y == 1
                ? String.format("%s元年%d月%d日", g, m, d)
                : String.format("%s%d年%d月%d日", g, y, m, d);
    }

    /**
     * 元号 -> ISO_DATE.
     *
     * @param gengoDate H22-7-26
     * @return ISO_DATE 2010-07-26
     */
    public static String gengoToIsoDate(String gengoDate) {
        String[] date = gengoDate.split("-");
        int year;
        int month;
        int day;

        if (date[0].length() == 4) {
            // 西暦で入ってきた場合
            year = Integer.valueOf(date[0]);
            month = Integer.valueOf(date[1]);
            day = Integer.valueOf(date[2]);

        } else {
            // 元号処理
            String gengo = date[0].substring(0, 1).toUpperCase();
            year = Integer.valueOf(date[0].substring(1));
            month = Integer.valueOf(date[1]);
            day = Integer.valueOf(date[2]);

            if (gengo.equals(MEIJI.alphabet())) {
                year += 1867;
            } else if (gengo.equals(TAISHO.alphabet())) {
                year += 1911;
            } else if (gengo.equals(SHOWA.alphabet())) {
                year += 1925;
            } else if (gengo.equals(HEISEI.alphabet())) {
                year += 1988;
            } else {
                year += 2018;
            }
        }

        return String.format("%d-%02d-%02d", year, month, day);
    }

    /**
     * Orca 型式の元号数字 -> 元号アルファベット変換.
     *
     * @param gengoNumber Orca で元号を表す数字 [1,2,3,4,...]
     * @return 元号を表すアルファベット [M,T,S,H,...]
     */
    public static String gengoNumberToAlphabet(String gengoNumber) {
        int num = Integer.valueOf(gengoNumber);
        if (num > values().length || num < 1) {
            return "U";
        } else return (values()[num].alphabet());
    }

    /**
     * 年号アルファベットを漢字に変換.
     *
     * @param alphabet [M,T,S,H,...]
     * @return 元号漢字 [明治,大正,昭和,平成,...]
     */
    public static String gengoAlphabetToKanji(String alphabet) {
        return Arrays.stream(values()).filter(value -> value.alphabet().equals(alphabet)).findAny().orElse(Gengo.DEFAULT).kanji();
    }

    /**
     * 年号アルファベットを複合漢字に変換.
     *
     * @param alphabet [M,T,S,H,...]
     * @return 元号漢字 [㍾,㍽,㍼,㍻,...]
     */
    public static String gengoAlphabetToHalfKanji(String alphabet) {
        return Arrays.stream(values()).filter(value -> value.alphabet().equals(alphabet)).findAny().orElse(Gengo.DEFAULT).halfKanji();
    }

    /**
     * ISO_DATE -> 元号変換の簡易呼び出し.
     *
     * @param isoDate ISO_DATE
     * @return gengo date
     */
    public static String toGengo(String isoDate) {
        return isoDateToGengo(isoDate);
    }

    /**
     * 元号 -> ISO_DATE の簡易呼び出し.
     *
     * @param gengoDate H22-7-26
     * @return ISO_DATE 2010-07-26
     */
    public static String toSeireki(String gengoDate) {
        return gengoToIsoDate(gengoDate);
    }

    public String alphabet() {
        return alphabet;
    }

    public String halfKanji() {
        return halfKanji;
    }

    public String kanji() {
        return kanji;
    }
}