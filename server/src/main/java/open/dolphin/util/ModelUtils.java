package open.dolphin.util;

import open.dolphin.infomodel.*;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ModelUtils.
 *
 * @author Minagawa, Kazushi
 * @author pns
 */
public class ModelUtils {
    private static final long serialVersionUID = 1L;

    /**
     * MML 形式から日付だけ取り出す.
     *
     * @param mmlDate MML 型式の日付 (2008-02-01T12:23:34)
     * @return 日付 (2008-02-01)
     */
    public static String trimTime(String mmlDate) {
        if (mmlDate == null) {
            return null;
        }

        int index = mmlDate.indexOf('T');
        if (index > -1) {
            return mmlDate.substring(0, index);
        } else {
            return mmlDate;
        }
    }

    /**
     * MML 形式から時：分だけ取り出す.
     *
     * @param mmlDate MML 型式の日付 (2008-02-01T12:23:34)
     * @return 時：分 (12:23)
     */
    public static String trimDate(String mmlDate) {
        if (mmlDate == null) {
            return null;
        }

        int index = mmlDate.indexOf('T');
        if (index > -1) {
            return mmlDate.substring(index + 1, index + 6); // THH:mm:ss -> HH:mm
        } else {
            return mmlDate;
        }
    }

    /**
     * mml 形式の生年月日から年齢付きの形式を作る.
     *
     * @param mmlBirthday 1975-01-01
     * @return 32.10 歳 (S50-01-01)
     */
    public static String getAgeBirthday(String mmlBirthday) {
        String age = getAge(mmlBirthday);
        if (age == null) return null;
        return String.format("%s %s (%s)", age, IInfoModel.AGE, Gengo.isoDateToGengo(mmlBirthday));
    }

    /**
     * ORCA 形式を年号形式に.
     *
     * @param orcaBirthday 4220726
     * @return h22-07-26
     */
    public static String orcaDateToGengo(String orcaBirthday) {
        //元号
        String nengo = Gengo.gengoNumberToAlphabet(orcaBirthday.substring(0, 1));
        //年
        String y = orcaBirthday.substring(1, 3);
        String m = orcaBirthday.substring(3, 5);
        String d = orcaBirthday.substring(5, 7);

        return nengo.toLowerCase() + y + "-" + m + "-" + d;
    }

    /**
     * 年齢を作る.
     *
     * @param mmlBirthday 1975-01-01
     * @return 32.10
     */
    public static String getAge(String mmlBirthday) {

        GregorianCalendar gc1 = getCalendar(mmlBirthday);
        if (gc1 == null) {
            return null;
        }

        GregorianCalendar gc2 = new GregorianCalendar(); // Today
        int years = 0;

        gc1.clear(Calendar.MILLISECOND);
        gc1.clear(Calendar.SECOND);
        gc1.clear(Calendar.MINUTE);
        gc1.clear(Calendar.HOUR_OF_DAY);

        gc2.clear(Calendar.MILLISECOND);
        gc2.clear(Calendar.SECOND);
        gc2.clear(Calendar.MINUTE);
        gc2.clear(Calendar.HOUR_OF_DAY);

        while (gc1.before(gc2)) {
            gc1.add(Calendar.YEAR, 1);
            years++;
        }
        years--;

        int month = 12;

        while (gc1.after(gc2)) {
            gc1.add(Calendar.MONTH, -1);
            month--;
        }

        return String.format("%d.%d", years, month);
    }

    /**
     * mmlDate 形式から GregorianCalendar を作る.
     *
     * @param mmlDate 1975-01-01
     * @return GregorianCalendar
     */
    public static GregorianCalendar getCalendar(String mmlDate) {

        Date date;
        if (mmlDate.contains("T")) {
            date = getDateTimeAsObject(mmlDate);
        } else {
            date = getDateAsObject(mmlDate);
        }

        if (date == null) {
            return null;
        }

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(date.getTime());
        return gc;
    }

    /**
     * 時間なしの mmlDate 形式から Date を作る.
     *
     * @param mmlDate 1975-01-01
     * @return parsed Date
     */
    public static Date getDateAsObject(String mmlDate) {
        if (mmlDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.DATE_WITHOUT_TIME);
                return sdf.parse(mmlDate);

            } catch (ParseException e) {
                e.printStackTrace(System.err);
            }
        }
        return null;
    }

    /**
     * 時間付きの mmlDate 形式から Date を作る.
     *
     * @param mmlDate 1975-01-01T12:23:34
     * @return parsed Date
     */
    public static Date getDateTimeAsObject(String mmlDate) {
        if (mmlDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.ISO_8601_DATE_FORMAT);
                return sdf.parse(mmlDate);

            } catch (ParseException e) {
                e.printStackTrace(System.err);
            }
        }
        return null;
    }

    /**
     * Date から時間なしの mmlDate 形式を作る.
     *
     * @param date Date
     * @return 1975-01-01
     */
    public static String getDateAsString(Date date) {
        return getDateAsFormatString(date, IInfoModel.DATE_WITHOUT_TIME);
    }

    /**
     * Date から時間付きの mmlDate 形式を作る.
     *
     * @param date Date
     * @return 1975-01-01T12:23:34
     */
    public static String getDateTimeAsString(Date date) {
        return getDateAsFormatString(date, IInfoModel.ISO_8601_DATE_FORMAT);
    }

    /**
     * Date から format で指定した形式の日付文字列を作る.
     *
     * @param date   Date
     * @param format SimpleDateFormat string
     * @return formatted string
     */
    public static String getDateAsFormatString(Date date, String format) {
        if (date == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * ORCA日付（20120401）を MMLフォーマット（2012-04-01）に変換.
     *
     * @param orcaDateString ORCA日付
     * @return MML日付
     */
    public static String toDolphinDateString(String orcaDateString) {
        if (orcaDateString == null || !orcaDateString.matches("[0-9]+")) {
            return null;
        }
        return String.join("-", orcaDateString.substring(0, 4), orcaDateString.substring(4, 6), orcaDateString.substring(6, 8));
    }

    /**
     * ISO_DATE -> 元号変換の簡易呼び出し.
     *
     * @param isoDate ISO_DATE
     * @return gengo date
     */
    public static String toNengo(String isoDate) {
        return Gengo.isoDateToGengo(isoDate);
    }

    /**
     * male -> 男　変換.
     *
     * @param gender male/female
     * @return 男/女
     */
    public static String getGenderDesc(String gender) {
        if (gender != null) {
            switch (gender.toLowerCase()) {
                case IInfoModel.MALE:
                    return IInfoModel.MALE_DISP;
                case IInfoModel.FEMALE:
                    return IInfoModel.FEMALE_DISP;
            }
        }
        return IInfoModel.UNKNOWN;
    }

    /**
     * エリアス付きの病名文字列を "," で分離する.
     *
     * @param diagnosis 病名, エリアス
     * @return [0] 病名, [1] エリアス
     */
    public static String[] splitDiagnosis(String diagnosis) {
        return (diagnosis == null) ? null : diagnosis.split("\\s*,\\s*");
    }

    /**
     * エリアス付きの病名から病名を取り出す.
     *
     * @param hasAlias エリアス付き病名
     * @return 病名. エリアスがない場合はそのまま返す.
     */
    public static String getDiagnosisName(String hasAlias) {
        String[] splits = splitDiagnosis(hasAlias);
        return (splits != null && splits.length == 2 && splits[0] != null) ? splits[0] : hasAlias;
    }

    /**
     * エリアス付き病名からエリアスを取り出す.
     *
     * @param hasAlias エリアス付き病名
     * @return エリアス. ない場合は null を返す.
     */
    public static String getDiagnosisAlias(String hasAlias) {
        String[] splits = splitDiagnosis(hasAlias);
        return (splits != null && splits.length == 2 && splits[1] != null) ? splits[1] : null;
    }

    /**
     * PVTDelegater で使う date を作成する（WaitingListImpl から移動).
     * date[0] = today, date[1] = AppodateFrom, date[2] = AppodateTo
     *
     * @param date Date
     * @return [0] 今日, [1] 2ヶ月前, [2] あれっ??
     */
    public static String[] getSearchDateAsString(Date date) {

        String[] ret = new String[3];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ret[0] = sdf.format(date);

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);

        gc.add(Calendar.DAY_OF_MONTH, -2);
        ret[1] = sdf.format(gc.getTime());

        gc.add(Calendar.DAY_OF_MONTH, 2);
        ret[2] = sdf.format(gc.getTime());

        return ret;
    }

    /**
     * ORCA転帰を Dolphin転帰に変換.
     *
     * @param orcaOutcome ORCA転帰 (DAO=1,2,3,8 or API=F,D,C,S)
     * @return Dolphin転帰
     */
    public static DiagnosisOutcomeModel toDolphinOutcome(String orcaOutcome) {
        if (Objects.nonNull(orcaOutcome)) {
            switch (orcaOutcome) {
                // 数字=DAO, 文字=API
                case "1":
                case "F":
                    return DiagnosisOutcome.FULLY_RECOVERED.model(); // 治癒
                case "2":
                case "D":
                    return DiagnosisOutcome.END.model(); // 死亡→終了に変換
                case "3":
                case "C":
                    return DiagnosisOutcome.PAUSE.model(); // 中止
                case "8":
                case "S":
                    return DiagnosisOutcome.PAUSE.model(); // 移行→中止に変換
            }
        }
        return DiagnosisOutcome.NONE.model();
    }

    /**
     * Dolphin転帰を ORCA転帰に変換.
     *
     * @param outcome DiagnosisOutcomeModel
     * @return ORCA転帰
     */
    public static String toOrcaOutcome(DiagnosisOutcomeModel outcome) {
        if (Objects.nonNull(outcome) && Objects.nonNull(outcome.getOutcome())) {
            if (outcome.getOutcome().equals(DiagnosisOutcome.PAUSE.name())) {
                return "C";
            } else if (outcome.getOutcome().equals(DiagnosisOutcome.FULLY_RECOVERED.name())
                    || outcome.getOutcome().equals(DiagnosisOutcome.END.name())) {
                return "F";
            }
        }
        return "";
    }

    /**
     * Object を beanBytes にエンコードする.
     *
     * @param bean エンコード対象の Object
     * @return エンコードされた byte array
     */
    public static byte[] xmlEncode(Object bean) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (XMLEncoder e = new XMLEncoder(new BufferedOutputStream(bo))) {
            e.writeObject(bean);
        }
        return bo.toByteArray();
    }

    /**
     * beanBytes をデコードする.
     *
     * @param bytes byte array
     * @return デコードされた Object
     */
    public static Object xmlDecode(byte[] bytes) {

        Object o;
        try (XMLDecoder d = new XMLDecoder(
                new BufferedInputStream(
                        new ByteArrayInputStream(bytes)))) {
            o = d.readObject();
        }

        return o;
    }

    /**
     * xml から&lt;text&gt;テキスト&lt;/text&gt;のテキストを取り出す.
     *
     * @param xml xmlテキスト
     * @return 取り出したテキスト
     */
    public static String extractText(String xml) {
        StringBuilder buf = new StringBuilder();
        String[] head = xml.split("<text>");
        for (String str : head) {
            String[] tail = str.split("</text>");
            if (tail.length == 2) {
                buf.append(tail[0].trim());
            }
        }
        return buf.toString();
    }

    /**
     * バイナリの健康保険データをオブジェクトにデコードする.
     *
     * @param insurances List of HealthInsuranceModel with BeanBytes
     * @return List of PVTHealthInsuranceModel decoded from BeanBytes
     */
    public static List<PVTHealthInsuranceModel> decodeHealthInsurance(List<HealthInsuranceModel> insurances) {

        if (insurances != null) {
            return insurances.stream().map(ins ->
                    (PVTHealthInsuranceModel) xmlDecode(ins.getBeanBytes())).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * treeBytes を treeXml に変換して返す.
     *
     * @param treeBytes byte array
     * @return tree XML
     */
    public static String toTreeXml(byte[] treeBytes) {
        return new String(treeBytes, StandardCharsets.UTF_8);
    }

    /**
     * treeXml を treeBytes に変換して返す.
     *
     * @param treeXml treeXml
     * @return byte array
     */
    public static byte[] toTreeBytes(String treeXml) {
        return treeXml.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Convert claim insurance code to orca insurance code.
     * https://www.orca.med.or.jp/receipt/tec/claim.html
     * <table>
     * <tr><th>保険の種類</th><th>労災</th><th>自費</th><th>治験</th><th>治験</th><th>公害</th><th>国保</th><th>後期高齢者</th><th>後期特療費</th><th>協会けんぽ</th><th>公費単独</th></tr>
     * <tr><th>Claim</th><td>Rx</td><td>Zx</td><td>Ax</td><td>Bx</td><td>K5</td><td>00</td><td>39</td><td>40</td><td>09</td><td>XX</td></tr>
     * <tr><th>API</th><td>97x</td><td>980</td><td>90x</td><td>91x</td><td>975</td><td>060</td><td>039</td><td>040</td><td>009</td><td>980</td></tr>
     * </table>
     * xは該当の保険番号マスタの保険番号の３桁目
     *
     * @param code Claim Insurance Code R1,R3,Zx,...
     * @return Orca Insurance Code 971,973,901,...
     */
    public static String claimInsuranceCodeToOrcaInsuranceCode(String code) {

        if (Objects.nonNull(code) && code.length() == 2) {
            if (code.startsWith("R")) {
                // 労災
                code = code.replace("R", "97");
            } else if (code.startsWith("Z")) {
                // 自費
                code = "980";
            } else if (code.startsWith("A")) {
                // 治験
                code = code.replace("A", "90");
            } else if (code.startsWith("B")) {
                // 治験
                code = code.replace("B", "91");
            } else if (code.equals("K5")) {
                // 公害
                code = "975";
            } else if (code.equals("00")) {
                // 国保
                code = "060";
            } else if (code.equals("XX")) {
                // 公費単独
                code = "980";
            } else if (code.matches("[0-9][0-9]")) {
                // 数字２桁は頭に 0 を補う
                code = "0" + code;
            }
        }
        return code;
    }

    /**
     * RegisteredDiagnosisModel の病名コードを，Orca Api 用に変換する.
     * eg) "1013.7061017" → { "ZZZ1013", "7061017" }
     *
     * @param claimByomei Dolphin 型式の病名
     * @return ORCA single 型式病名
     */
    public static String[] toOrcaDiseaseSingle(String claimByomei) {
        String[] singles = claimByomei.split("\\.");
        for (int i = 0; i < singles.length; i++) {
            if (singles[i].length() == 4) {
                singles[i] = "ZZZ" + singles[i];
            }
        }
        return singles;
    }

    /**
     * スタンプを複製して返す. bundle もコピーされる. ただし ClaimItem は空.
     *
     * @param src source stamp
     * @return cloned stamp
     */
    public static ModuleModel clone(ModuleModel src) {

        ModuleInfoBean srcModuleInfo = src.getModuleInfo();
        BundleMed srcBundle = (BundleMed) src.getModel();

        // 複製
        ModuleModel dist = new ModuleModel();

        BundleMed distBundle = clone(srcBundle);
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
     * @param src source BundleMed
     * @return cloned BundleMed
     */
    public static BundleMed clone(BundleMed src) {
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
    public static ClaimItem clone(ClaimItem src) {
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
     * ClaimItem[] を複製して返す.
     *
     * @param src source array of ClaimItem
     * @return array of ClaimItem
     */
    public static ClaimItem[] clone(ClaimItem[] src) {
        ClaimItem[] dist = new ClaimItem[src.length];
        for (int i=0; i<src.length; i++) {
            dist[i] = clone(src[i]);
        }
        return dist;
    }

    /**
     * スタンプを複製して返す. Bundle, ClaimItem も複製する.
     *
     * @param src source stamp
     * @return cloned stamp
     */
    public static ModuleModel deepClone(ModuleModel src) {
        BundleMed srcBundle = (BundleMed) src.getModel();
        ClaimItem[] srcItems = srcBundle.getClaimItem();

        ModuleModel dist = clone(src);
        BundleMed distBundle = (BundleMed) dist.getModel();
        distBundle.setClaimItem(clone(srcItems));
        return dist;
    }

    /**
     * SchemaModel を複製して返す.
     *
     * @param src source SchemaModel
     * @return cloned SchemaModel
     */
    public static SchemaModel clone(SchemaModel src) {
        SchemaModel dist = new SchemaModel();
        dist.setExtRef(src.getExtRef());
        dist.setJpegByte(src.getJpegByte());
        dist.setDocument(src.getDocument());
        dist.setFileName(src.getFileName());
        dist.setIcon(src.getIcon());
        dist.setImageNumber(src.getImageNumber());
        return dist;
    }

    public static void main(String[] argv) {
        System.out.println(orcaDateToGengo("3300101"));
        System.out.println(orcaDateToGengo("4300430"));
        System.out.println(orcaDateToGengo("5010501"));
        System.out.println(claimInsuranceCodeToOrcaInsuranceCode("00"));
        System.out.println(claimInsuranceCodeToOrcaInsuranceCode("09"));
        System.out.println(claimInsuranceCodeToOrcaInsuranceCode("39"));
        System.out.println(claimInsuranceCodeToOrcaInsuranceCode("XX"));
        System.out.println(claimInsuranceCodeToOrcaInsuranceCode("Z0"));
        System.out.println(claimInsuranceCodeToOrcaInsuranceCode("060"));
    }
}
