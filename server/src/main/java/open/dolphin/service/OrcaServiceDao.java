package open.dolphin.service;

import open.dolphin.JsonConverter;
import open.dolphin.dto.DiagnosisSearchSpec;
import open.dolphin.dto.OrcaEntry;
import open.dolphin.dto.PatientVisitSpec;
import open.dolphin.infomodel.*;
import open.dolphin.orca.ClaimConst;
import open.dolphin.orca.orcadao.OrcaDao;
import open.dolphin.orca.orcadao.OrcaDbConnection;
import open.dolphin.orca.orcadao.bean.*;
import open.dolphin.util.ModelUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OrcaServiceDao.
 * DAO による OrcaService の implementation.
 *
 * @author pns
 */
public class OrcaServiceDao {

    // ORCA 形式の今日の日付
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
    String today = dtf.format(LocalDate.now());
    private OrcaDao dao = OrcaDao.getInstance();
    private Logger logger = Logger.getLogger(OrcaServiceApi.class);

    /**
     * 中途終了患者情報.
     * TBL_WKSRYACT ワーク診療行為 (中途終了データ).
     *
     * @param spec PatientVisitSpec (patientId と date を使用)
     * @return Wksryact
     */
    public Wksryact getWksryact(PatientVisitSpec spec) {
        String sql = "select karte_key, mod_flg from tbl_wksryact "
                + "where sryymd = ? and ptid = (select ptid from tbl_ptnum where ptnum = ?)";

        Wksryact wksryact = new Wksryact();

        String ptId = spec.getPatientId();
        String date = Objects.isNull(spec.getDate()) ?
                today : spec.getDate().replaceAll("-", "");

        OrcaDbConnection con = dao.getConnection(rs -> {
            while (rs.next()) {
                wksryact.setMedicalUid(rs.getString(1)); // UUID karte_key
                wksryact.setMedicalMode(rs.getString(2)); // mod_flg 1=展開されている, 0=展開されていない
                wksryact.setMedicalMode2("1"); // ここに入ってきたと言うことは中途終了データがあるということ
            }
        });
        con.setParam(1, date);
        con.setParam(2, ptId);
        con.executeQuery(sql);

        return wksryact;
    }

    /**
     * 中途終了患者情報が存在するかどうか.
     *
     * @param ptId "000001"
     * @return 中途終了情報あり=true
     */
    public boolean existsOrcaWorkingData(String ptId) {
        PatientVisitSpec spec = new PatientVisitSpec();
        spec.setPatientId(ptId);
        Wksryact w = getWksryact(spec);
        return Objects.nonNull(w.getMedicalMode());
    }

    /**
     * 職員情報.
     * TBL_SYSKANRI システム管理 kanricd = '1010'.
     *
     * @return Syskanri のリスト
     */
    public List<Syskanri> getSyskanri() {
        String sql = "select kbncd, kanritbl from tbl_syskanri where kanricd = '1010'"; // 1010 = 職員情報

        List<Syskanri> ret = new ArrayList<>();

        OrcaDbConnection con = dao.getConnection(rs -> {
            while (rs.next()) {
                Syskanri syskanri = new Syskanri();
                syskanri.setCode(rs.getString(1).trim());   // kbncd
                String kanritbl = rs.getString(2);   // kanritlb
                String[] kanriTblItem = kanritbl.split(" +");
                // 0:login id, 1:kana name, 2:kanji name
                syskanri.setWholeName(kanriTblItem[2]);
                syskanri.setKanaName(kanriTblItem[1]);
                ret.add(syskanri);
            }
        });
        con.executeQuery(sql);

        return ret;
    }

    /**
     * TBL_TENSU からキーワードを検索.
     * OrcaApi では TBL_TENSU の検索はできない.
     *
     * @param keyword キーワード
     * @return OrcaEntry のリスト
     */
    public List<OrcaEntry> findTensu(String keyword) {
        String sql = "select srycd, name, kananame, yukostymd, yukoedymd, "
                + "formalname, taniname, tensikibetu, ten, nyugaitekkbn, routekkbn, srysyukbn, hospsrykbn, yakkakjncd, ykzkbn "
                + "from tbl_tensu where srycd ~ ? or name ~ ? or kananame ~ ? order by srycd";

        List<OrcaEntry> ret = new ArrayList<>();
        List<OrcaEntry> expired = new ArrayList<>();

        OrcaDbConnection con = dao.getConnection(rs -> {
            while (rs.next()) {
                OrcaEntry tensu = new OrcaEntry();
                tensu.setCode(rs.getString(1)); // srycd
                tensu.setName(rs.getString(2)); // name
                tensu.setKanaName(rs.getString(3)); // kananame
                tensu.setStartDate(rs.getString(4)); // yukostymd
                tensu.setEndDate(rs.getString(5)); // yukoedymd
                tensu.setFormalName(rs.getString(6)); // formalname
                tensu.setUnit(rs.getString(7)); // taniname
                tensu.setTensikibetu(rs.getString(8)); // tensikibetu
                tensu.setTen(rs.getString(9)); // ten
                tensu.setNyugaitekkbn(rs.getString(10)); // nyugaitekkbn
                tensu.setRoutekkbn(rs.getString(11)); // routekkbn
                tensu.setClaimClassCode(rs.getString(12)); // srysyukbn
                tensu.setHospsrykbn(rs.getString(13)); // hospsrykbn
                tensu.setYakkakjncd(rs.getString(14)); // yakkakjncd
                tensu.setYkzkbn(rs.getString(15)); // ykzkbn

                // 日付のフォーマット確認
                if (!tensu.getStartDate().matches("[0-9]*")) {
                    tensu.setStartDate("00000000");
                }
                if (!tensu.getEndDate().matches("[0-9]*")) {
                    tensu.setEndDate("99999999");
                }

                if (today.compareTo(tensu.getEndDate()) <= 0) {
                    ret.add(tensu);
                } else {
                    expired.add(tensu);
                }
            }
        });
        con.setParam(1, keyword); // srycd
        con.setParam(2, keyword); // name
        con.setParam(3, keyword); // kananame
        con.executeQuery(sql);

        ret.addAll(expired);
        return ret;
    }

    /**
     * TBL_BYOMEI からキーワードを検索.
     * OrcaApi では TBL_BYOMEI の検索はできない.
     *
     * @param keyword キーワード
     * @return RegisteredDiagnosisModel のリスト
     */
    public List<OrcaEntry> findDiagnosis(String keyword) {
        String sql = "select byomeicd, byomei, byomeikana, syusaiymd, haisiymd, icd10_1_2 from tbl_byomei "
                + "where (byomei ~ ? or byomeikana ~ ? or byomeicd ~ ?) and haisiymd >= ? order by icd10_1_2, byomeicd";

        List<OrcaEntry> ret = new ArrayList<>();

        OrcaDbConnection con = dao.getConnection(rs -> ret.addAll(createOrcaEntry(rs)));
        con.setParam(1, keyword); // byomei
        con.setParam(2, keyword); // byomeikana
        con.setParam(3, keyword); // byomeicd
        con.setParam(4, "00000000"); // haisiymd
        con.executeQuery(sql);

        return ret;
    }

    /**
     * TBL_BYOMEI から病名コードのリストに対応する病名を検索.
     * OrcaApi では TBL_BYOMEI の検索はできない.
     *
     * @param srycds 病名コードのリスト
     * @return RegisteredDiagnosisModel のリスト
     */
    public List<OrcaEntry> findDiagnosis(List<String> srycds) {

        List<OrcaEntry> ret = new ArrayList<>();
        if (srycds.isEmpty()) {
            return ret;
        } // 空の場合

        String sql = "select byomeicd, byomei, byomeikana, syusaiymd, haisiymd, icd10_1_2 from tbl_byomei "
                + "where byomeicd in (?) order by icd10_1_2, byomeicd";

        if (srycds.size() > 1) {
            sql = sql.replace("?", "?" + StringUtils.repeat(",?", srycds.size() - 1));
        }

        OrcaDbConnection con = dao.getConnection(rs -> ret.addAll(createOrcaEntry(rs)));
        for (int i = 0; i < srycds.size(); i++) {
            con.setParam(i + 1, srycds.get(i));
        }
        con.executeQuery(sql);

        return ret;
    }

    /**
     * findDiagnosisByKeyword に共通する ResultSet から List of RegisteredDiagnosisModel を作る部分.
     *
     * @param rs ResultSet
     * @return 作った List of OrcaEntry
     * @throws SQLException SQLException
     */
    private List<OrcaEntry> createOrcaEntry(ResultSet rs) throws SQLException {
        List<OrcaEntry> ret = new ArrayList<>();
        List<OrcaEntry> expired = new ArrayList<>();

        while (rs.next()) {
            OrcaEntry diag = new OrcaEntry();
            diag.setCode(rs.getString(1)); // byomeicd
            diag.setName(rs.getString(2)); // byomei
            diag.setKanaName(rs.getString(3)); // byomeikana
            diag.setStartDate(rs.getString(4)); // syusaiymd
            diag.setEndDate(rs.getString(5)); // haisiymd
            diag.setIcd10(rs.getString(6)); // icd10_1

            // 日付のフォーマット確認
            if (!diag.getStartDate().matches("[0-9]*")) {
                diag.setStartDate("00000000");
            }
            if (!diag.getEndDate().matches("[0-9]*")) {
                diag.setEndDate("99999999");
            }

            if (today.compareTo(diag.getEndDate()) <= 0) {
                ret.add(diag);
            } else {
                expired.add(diag);
            }
        }
        ret.addAll(expired);
        return ret;
    }

    /**
     * 移行病名を調べる. endDate = "99999999" 以外は移行または廃止病名.
     *
     * @param srycds 病名コードのリスト
     * @return そのうち移行病名になっているののリスト (null にはならない)
     */
    public List<String> findIkouByomei(List<String> srycds) {
        return findDiagnosis(srycds).stream().filter(entry -> !"99999999".equals(entry.getEndDate()))
                .map(OrcaEntry::getCode).collect(Collectors.toList());
    }

    /**
     * TBL_INPUTCD を検索して入力セット（約束処方、診療セット）の ModulenfoBean リストを返す.
     * OrcaApi では診療セットを個別に取得することはできるが一覧を得ることができない.
     *
     * @return StampInfo (ModuleInfoBean) のリスト
     */
    public List<ModuleInfoBean> getOrcaInputCdList() {
        String sql = "select inputcd, dspname from tbl_inputcd "
                + "where hospnum = ? and (inputcd like 'P%' or inputcd like 'S%') order by inputcd";

        List<ModuleInfoBean> ret = new ArrayList<>();

        OrcaDbConnection con = dao.getConnection(rs -> {
            while (rs.next()) {
                String inputcd = rs.getString(1); // inputcd
                String dspName = rs.getString(2); // dspname

                // inputcd が7桁以降スペース全20桁で返ってくるので6桁にする
                if (inputcd.length() > 6) {
                    inputcd = inputcd.substring(0, 6);
                }

                ModuleInfoBean stampInfo = new ModuleInfoBean();
                stampInfo.setStampName(dspName);
                stampInfo.setStampRole(IInfoModel.ROLE_ORCA_SET);
                stampInfo.setEntity(IInfoModel.ENTITY_MED_ORDER);
                stampInfo.setStampId(inputcd);

                ret.add(stampInfo);
            }
        });
        con.setParam(1, dao.getHospNum());
        con.executeQuery(sql);

        return ret;
    }

    /**
     * StampInfo を元に TBL_INPUTSET，TBL_TENSU を検索してスタンプの実体を作る.
     *
     * @param stampInfo ModuleInfoBean
     * @return List of ModuleModel
     */
    public List<ModuleModel> getStamp(ModuleInfoBean stampInfo) {

        String inputsetSql = "select inputcd, suryo1, kaisu, coment from tbl_inputset "
                + "where hospnum = ? and setcd = ? order by setseq";

        List<List<Inputset>> lists = new ArrayList<>(); // スタンプ List<Inputset> を複数保持する
        List<String> srycds = new ArrayList<>(); // .210 or 616130532 tbl_tensu 検索用リスト

        OrcaDbConnection con = dao.getConnection(rs -> {

            List<Inputset> buffer = new ArrayList<>();
            boolean onAdmin = false;

            while (rs.next()) {
                Inputset inputset = new Inputset();
                inputset.setInputCd(rs.getString(1)); // .210 or 616130532 ...
                inputset.setSuryo1(rs.getFloat(2)); // suryo1
                inputset.setKaisu(rs.getInt(3)); // bundle 数
                inputset.setComment(rs.getString(4)); // coment

                // Inputset を解釈して，stamp に対応したリストに分割する
                String inputcd = inputset.getInputCd();

                // 用法の場合 onAdmin をセット (001xxxxxx のパターン)
                if (inputcd.startsWith(ClaimConst.ADMIN_CODE_START)) {
                    onAdmin = true;
                    // tbl_tensu に問い合わせなくてはならない inputcd (001xxxxxx) を記録
                    srycds.add(inputcd);

                } else {
                    // admin でなくなった行があったらそこで分離する
                    if (onAdmin) {
                        onAdmin = false;
                        // バッファの更新
                        lists.add(buffer);
                        buffer = new ArrayList<>();

                    } else if (inputcd.startsWith(".")) {
                        // .400 のパターンがきたら，そこでバッファ更新
                        if (!buffer.isEmpty()) {
                            lists.add(buffer);
                            buffer = new ArrayList<>();
                        }

                    } else {
                        // tbl_tensu に問い合わせなくてはならない inputcd (616130532等) を記録
                        srycds.add(inputcd);
                    }
                }
                buffer.add(inputset);
            }
            // 最後の buffer を保存
            lists.add(buffer);
        });
        con.setParam(1, dao.getHospNum());
        con.setParam(2, stampInfo.getStampId()); // "P00001" セットコード (= inputcd)
        con.executeQuery(inputsetSql);

        // その情報を元に，tbl_tensu を検索して ModuleModel を作成する
        String tensuSql = "select srysyukbn, name, taniname, ykzkbn, srycd from tbl_tensu where hospnum = ? and srycd in (?)";
        if (srycds.size() > 1) {
            tensuSql = tensuSql.replace("(?)", "(?" + StringUtils.repeat(",?", srycds.size() - 1) + ")");
        }

        // tbl_tensu から必要な情報をまとめてクエリ，srysyukbn をキーとする HashMap に格納
        HashMap<String, OrcaEntry> tensuMap = new HashMap<>();

        con = dao.getConnection(rs -> {
            while (rs.next()) {
                OrcaEntry tensu = new OrcaEntry();
                tensu.setClaimClassCode(rs.getString(1));
                tensu.setName(rs.getString(2));
                tensu.setUnit(rs.getString(3));
                tensu.setYkzkbn(rs.getString(4));
                tensu.setCode(rs.getString(5));
                tensuMap.put(tensu.getCode(), tensu); // srycd 616130532
            }
        });
        con.setParam(1, dao.getHospNum());
        for (int i = 0; i < srycds.size(); i++) {
            con.setParam(i + 2, srycds.get(i));
        }
        con.executeQuery(tensuSql);

        List<ModuleModel> ret = new ArrayList<>();
        ModuleModel stamp = null;
        ClaimBundle bundle = null;

        for (List<Inputset> list : lists) {
            for (Inputset inputset : list) {
                String inputcd = inputset.getInputCd();
                String stampName = stampInfo.getStampName();

                if (inputcd.startsWith(".")) {
                    //
                    // inputcd が .210 のパターン
                    //
                    stamp = createStampModel(stampName, inputcd);
                    if (stamp != null) {
                        bundle = (BundleDolphin) stamp.getModel();
                    }

                } else {
                    //
                    // inputcd が 616130532 のパターン
                    //
                    OrcaEntry tensu = tensuMap.get(inputcd); // inputcd が 616130532のパターン

                    ClaimItem item = new ClaimItem();
                    item.setCode(inputcd);
                    item.setName(tensu.getName());
                    item.setClassCodeSystem(ClaimConst.SUBCLASS_CODE_ID);

                    // 数量の小数点以下処理
                    String dose = String.valueOf(inputset.getSuryo1());
                    if (dose.endsWith(".0")) dose = dose.substring(0, dose.length() - 2);
                    item.setNumber(dose);

                    String kbn;
                    if (inputcd.startsWith(ClaimConst.SYUGI_CODE_START)) {
                        // 手技の場合
                        item.setClassCode(String.valueOf(ClaimConst.SYUGI));
                        kbn = tensu.getClaimClassCode();

                    } else if (inputcd.startsWith(ClaimConst.YAKUZAI_CODE_START)) {
                        // 薬剤の場合
                        item.setClassCode(String.valueOf(ClaimConst.YAKUZAI));
                        item.setNumberCode(ClaimConst.YAKUZAI_TOYORYO);
                        item.setNumberCodeSystem(ClaimConst.NUMBER_CODE_ID);
                        item.setUnit(tensu.getUnit());
                        kbn = tensu.getYkzkbn().equals(ClaimConst.YKZ_KBN_NAIYO) ?
                                ClaimConst.RECEIPT_CODE_NAIYO : ClaimConst.RECEIPT_CODE_GAIYO;

                    } else if (inputcd.startsWith(ClaimConst.ZAIRYO_CODE_START)) {
                        // 材料の場合
                        item.setClassCode(String.valueOf(ClaimConst.ZAIRYO));
                        item.setNumberCode(ClaimConst.ZAIRYO_KOSU);
                        item.setNumberCodeSystem(ClaimConst.NUMBER_CODE_ID);
                        item.setUnit(tensu.getUnit());
                        kbn = "999";

                    } else if (inputcd.startsWith(ClaimConst.ADMIN_CODE_START)) {
                        // 用法の場合
                        item.setClassCode(String.valueOf(ClaimConst.YAKUZAI));
                        item.setNumber("");
                        kbn = "220";

                    } else if (inputcd.startsWith(ClaimConst.RBUI_CODE_START)) {
                        // 放射線部位の場合
                        item.setClassCode(String.valueOf(ClaimConst.SYUGI));
                        kbn = "700";

                    } else if (inputcd.startsWith(ClaimConst.COMMENT_CODE_START)) {
                        // コメントコードの場合
                        item.setClassCode(String.valueOf(ClaimConst.SYUGI));
                        item.setName(inputset.getComment());
                        kbn = "800";

                    } else {
                        // どれでもない場合
                        logger.info("OrcaMasterDao: no srysykbn found : inputcd = " + inputcd);
                        continue;
                    }

                    //
                    // stamp 作成. inputcd が .210 のパターンを通過していない場合は null のままなので.
                    //
                    if (bundle == null) {
                        stamp = createStampModel(stampName, kbn);
                        bundle = (ClaimBundle) stamp.getModel();
                    }

                    // 用法 (001000000-799) の処理
                    // 部位 (001000800-999) は admin に入れない
                    if (inputcd.matches("^001000[0-7].*")) {

                        // 頓用処理
                        if (inputcd.startsWith("0010005")) {
                            bundle.setClassCode(IInfoModel.RECEIPT_CODE_TONYO);
                        }

                        bundle.setAdmin(tensu.getName());
                        bundle.setAdminCode(inputcd);
                        bundle.setBundleNumber(String.valueOf(inputset.getKaisu()));

                    } else {
                        bundle.addClaimItem(item);
                    }
                }

            } // end for

            ret.add(stamp);
        }

        return ret;
    }

    /**
     * スタンプ名，診療区分から Stamp のひな形を生成する.
     *
     * @param stampName Stamp名
     * @param srysyukbn 診療区分コード（３桁のコード）
     * @return Stamp
     */
    private ModuleModel createStampModel(String stampName, String srysyukbn) {

        if (srysyukbn == null) {
            return null;
        }

        // .210 のパターンなら，. を削除
        if (srysyukbn.startsWith(".")) {
            srysyukbn = srysyukbn.substring(1);
        }
        // kbn はコードの上位３桁
        if (srysyukbn.length() > 3) {
            srysyukbn = srysyukbn.substring(0, 3);
        }

        // ModuleModel 作成
        ModuleModel stamp = new ModuleModel();

        ModuleInfoBean stampInfo = stamp.getModuleInfo();
        stampInfo.setStampName(stampName);
        stampInfo.setStampRole(IInfoModel.ROLE_P);

        // kbn の範囲からエンティティーを取得
        String entity = ClaimConst.getEntity(srysyukbn);
        stampInfo.setEntity(entity);

        // entity の名前を取得
        String orderName = ClaimConst.EntityNameMap.get(entity);

        // ClaimBundle 作成 (BundleMed > BundleDolphine > ClaimBundle)
        BundleDolphin bundle;

        // 処方（2xx） の場合，BundleMed にして院内／院外をセット
        if (srysyukbn.startsWith("2")) {
            bundle = new BundleMed();
            bundle.setMemo(ClaimConst.EXT_MEDICINE);
            bundle.setOrderName(orderName);

        } else {
            // 処方以外は BundleDolphin
            bundle = new BundleDolphin();
            bundle.setOrderName(orderName);
        }

        bundle.setClassCode(srysyukbn);
        bundle.setClassCodeSystem(ClaimConst.CLASS_CODE_ID);
        bundle.setClassName(ClaimConst.getSrysyukbnName(srysyukbn));
        bundle.setBundleNumber("1");

        stamp.setModel(bundle);

        return stamp;
    }


    /**
     * TBL_PTBYOMEI を検索して RegisteredDiagnosisModel を作る.
     * toDate : 基準月 - この月の月終わりより sryymd が古くてかつ月初めより tenkiymd が新しいものをその月の有効病名とする.
     * fromDate : 基準月を古い方にこの月まで拡張する. 開院月まで拡張すると全病名がひっかかる.
     *
     * @param spec DiagnosisSearchSpec (patientId, fromDate. toDate を使用)
     * @return List of RegisteredDiagnosisModel
     */
    public List<RegisteredDiagnosisModel> getOrcaDisease(DiagnosisSearchSpec spec) {
        String ptnumSql = "select ptid from tbl_ptnum where hospnum = ? and ptnum = ?";

        // byomeicd_1 〜 byomei_21 を生成
        List<String> byomeicds = new ArrayList<>();
        for (int i = 1; i <= 21; i++) {
            byomeicds.add("byomeicd_" + i);
        } // String + int で行ける

        String byomeiSql = "select khnbyomeicd, byomei, sryymd, tenkikbn, tenkiymd, "
                + String.join(", ", byomeicds)
                + " from tbl_ptbyomei "
                + "where hospnum=? and ptid=? and dltflg = '' "
                + "and sryymd <= ? and (tenkiymd >= ? or tenkiymd = '') order by sryymd desc ";

        List<RegisteredDiagnosisModel> ret = new ArrayList<>();

        // 患者番号から ORCA の ptid を得る
        List<Integer> ptid = new ArrayList<>();
        OrcaDbConnection con = dao.getConnection(rs -> {
            while (rs.next()) {
                ptid.add(rs.getInt(1));
            }
        });
        con.setParam(1, dao.getHospNum());
        con.setParam(2, spec.getPatientId());
        con.executeQuery(ptnumSql);

        if (ptid.isEmpty()) {
            return null;
        }

        // tbl_ptbyomei 検索
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        String toDate = Objects.isNull(spec.getToDate()) ? sdf.format(new Date()) : sdf.format(spec.getToDate());

        String sryymdNewestLimit = toDate + "31";
        String tenkiymdOldestLimit = Objects.isNull(spec.getFromDate()) ? toDate + "01" : sdf.format(spec.getFromDate()) + "01";

        con = dao.getConnection(rs -> {
            while (rs.next()) {

                RegisteredDiagnosisModel rd = new RegisteredDiagnosisModel();
                // 病名コード
                // rd.setDiagnosisCode(rs.getString(1)); // khnbyomeicd
                // 病名
                rd.setDiagnosis(rs.getString(2)); // byomei
                // 開始日
                rd.setStartDate(ModelUtils.toDolphinDateString(rs.getString(3))); // sryymd
                // 転帰
                rd.setDiagnosisOutcomeModel(ModelUtils.toDolphinOutcome(rs.getString(4))); //tenkikbn
                // 転帰日
                rd.setEndDate(ModelUtils.toDolphinDateString(rs.getString(5))); // tenkiymd
                // ORCA 病名識別
                rd.setStatus("ORCA");

                // 病名コード生成
                List<String> cds = new ArrayList<>();
                for (int i = 6; i <= 25; i++) {
                    String c = rs.getString(i);
                    if (Objects.nonNull(c) && !c.equals("")) {
                        cds.add(c.replace("ZZZ", ""));
                    }
                }
                rd.setDiagnosisCode(String.join(".", cds));

                ret.add(rd);
            }
        });
        con.setParam(1, dao.getHospNum());
        con.setParam(2, ptid.get(0));
        con.setParam(3, sryymdNewestLimit);
        con.setParam(4, tenkiymdOldestLimit);
        con.executeQuery(byomeiSql);

        return ret;
    }

    /**
     * 資格確認薬剤情報格納.
     * TBL_ONSHI_YAKUZAI_SUB 資格確認薬剤情報格納副テーブル.
     *
     * @param ptnum
     * @return List of Onshi Yakuzai
     */
    public List<OnshiYakuzai> getDrugHistory(String ptnum) {
        String sql = "select sryym, hospcd, hospname, chozaicd, chozainame, chozai_seqnum, chozai_kbn, shoho_seqnum, shoho_kbn, ptid "
            + "from tbl_onshi_yakuzai_main "
            + "where ptid = (select ptid from tbl_ptnum where ptnum = ?)";

        // String.valueOf(ptid) + srymm をキーとして facility を保持する map
        HashMap<String, List<Facility>> facilities = new HashMap<>();

        OrcaDbConnection con = dao.getConnection(rs -> {
            while (rs.next()) {
                String sryym = rs.getString(1);
                String hospcd = rs.getString(2);
                String hospname = rs.getString(3);
                String chozaicd = rs.getString(4);
                String chozainame = rs.getString(5);
                int chozaiSeqnum = rs.getInt(6);
                String chozaiKbn = rs.getString(7);
                int shohoSeqnum = rs.getInt(8);
                String shohoKbn = rs.getString(9);
                int ptid = rs.getInt(10);

                String key = String.valueOf(ptid) + sryym;
                List<Facility> facilityList = facilities.get(key);
                if (Objects.isNull(facilityList)) { facilityList = new ArrayList<>(); }

                // seqnum !=0 を id として, facility beans を作る
                // chozaiSequnum は hospNameに対応, shohoSeqnum は chozaiName に対応している
                if (chozaiSeqnum != 0 && facilityList.stream().noneMatch(f -> f.getId() == chozaiSeqnum)) {
                    Facility facility = new Facility();
                    facility.setId(chozaiSeqnum);
                    facility.setMe(chozaiKbn == "1");
                    // hospname, hospcode が入力されていないことがある
                    if (!StringUtils.isEmpty(hospname))  { facility.setFacilityName(hospname); }
                    if (!StringUtils.isEmpty(hospcd)) { facility.setFacilityCode(hospcd); }
                    facilityList.add(facility);
                    facilities.put(key, facilityList);
                }
                if (shohoSeqnum != 0 && facilityList.stream().noneMatch(f -> f.getId() == shohoSeqnum)) {
                    Facility facility = new Facility();
                    facility.setId(shohoSeqnum);
                    facility.setMe(shohoKbn == "1");
                    if (!StringUtils.isEmpty(chozainame)) { facility.setFacilityName(chozainame); }
                    if (!StringUtils.isEmpty(chozaicd)) { facility.setFacilityCode(chozaicd); }
                    facilityList.add(facility);
                    facilities.put(key, facilityList);
                }
            }
        });

        con.setParam(1, ptnum);
        con.executeQuery(sql);

        sql = "select sryym, srydd, shoho_hakkoymd, rennum, yohocd, yohoname, shiji, srycd, yakuzainame, taniname, suryo, yoryo, kaisu, chozai_seqnum, shoho_seqnum, ptid "
            + "from tbl_onshi_yakuzai_sub "
            + "where ptid = (select ptid from tbl_ptnum where ptnum = ?)";

        List<OnshiYakuzai> bundle = new ArrayList<>();
        con = dao.getConnection(rs -> {
            while (rs.next()) {
                OnshiYakuzai onshiYakuzai = new OnshiYakuzai();
                String sryym = rs.getString(1); // 202107
                String srydd = rs.getString(2); // 25
                onshiYakuzai.setIsoDate(String.format("%s-%s-%s", sryym.substring(0,4), sryym.substring(4), srydd));
                onshiYakuzai.setShohoHakkoymd(rs.getString(3));
                onshiYakuzai.setRennum(rs.getInt(4)); // 明細毎の連番
                onshiYakuzai.setYohocd(rs.getString(5)); // 3桁の用法コード: 別表12 外用剤は 900
                onshiYakuzai.setYohoname(rs.getString(6)); // 用法名称: 外用剤の場合空欄
                onshiYakuzai.setShiji(rs.getString(7)); // 特別指示: 外用回数、部位
                onshiYakuzai.setSrycd(rs.getString(8)); // 医薬品コード
                onshiYakuzai.setYakuzainame(rs.getString(9)); // 薬剤名
                onshiYakuzai.setTaniname(rs.getString(10)); // 単位名
                onshiYakuzai.setSuryo(rs.getFloat(11)); // 1日量
                onshiYakuzai.setYoryo(rs.getFloat(12)); // 1回量: 0 が入っている
                onshiYakuzai.setKaisu(rs.getInt(13)); // x日分: 外用剤は 1
                int chozaiSeqnum = rs.getInt(14);
                int shohoSeqnum = rs.getInt(15);
                int ptid = rs.getInt(16);

                // facility beans から facility name を取得
                // chozaiSequnum は hospNameに対応, shohoSeqnum は chozaiName に対応している
                String key = String.valueOf(ptid) + sryym;
                List<Facility> facilityList = facilities.get(key);
                Optional<Facility> chozai = facilityList.stream().filter(f -> f.getId() == chozaiSeqnum).findAny();
                onshiYakuzai.setHosp(chozai.isPresent()? chozai.get() : new Facility());
                Optional<Facility> shoho = facilityList.stream().filter(f -> f.getId() == shohoSeqnum).findAny();
                onshiYakuzai.setChozai(shoho.isPresent()? shoho.get() : new Facility());

                bundle.add(onshiYakuzai);
            }
        });
        con.setParam(1, ptnum);
        con.executeQuery(sql);

        // sort
        Collections.sort(bundle, (o1, o2) -> {
            int date = o1.getIsoDate().compareTo(o2.getIsoDate());
            int rennum = o1.getRennum() - o2.getRennum();
            return date == 0? rennum : date;
        });

        return bundle;
    }

    /**
     * 資格確認薬剤情報の有無の確認.
     *
     * @param ptnum
     * @return has Onshi Yakuzai data or not
     */
    public boolean hasDrugHistory(String ptnum) {
        String sql = "select sryym from tbl_onshi_yakuzai_sub where ptid = (select ptid from tbl_ptnum where ptnum = ?) limit 1";
        List<String> sryym = new ArrayList<>();
        OrcaDbConnection con = dao.getConnection(rs -> {
            while (rs.next()) {
                sryym.add(rs.getString(1));
            }
        });
        con.setParam(1, ptnum);
        con.executeQuery(sql);

        return !sryym.isEmpty();
    }

    /**
     * 資格確認特定健診情報格納.
     * TBL_ONSHI_KENSHIN_SUB 資格確認特定健診情報格納副テーブル.
     *
     * @param ptnum
     * @return List of Onshi Kenshin
     */
    public List<OnshiKenshin> getKenshin(String ptnum) {
        String sql = "select jisshiymd, rennum, komokucd, komokuname, data_type, data_value, data_tani "
            + "from tbl_onshi_kenshin_sub "
            + "where ptid = (select ptid from tbl_ptnum where ptnum = ?)";

        List<OnshiKenshin> bundle = new ArrayList<>();
        OrcaDbConnection con = dao.getConnection(rs -> {
            while (rs.next()) {
                OnshiKenshin onshiKenshin = new OnshiKenshin();
                String jisshiymd = rs.getString(1); // 20200418
                onshiKenshin.setIsoDate(String.format("%s-%s-%s", jisshiymd.substring(0,4), jisshiymd.substring(4,6), jisshiymd.substring((6))));
                onshiKenshin.setRennum(rs.getInt(2)); // 1,2,...
                onshiKenshin.setKomokucd(rs.getString(3)); // 9N001000000000001
                onshiKenshin.setKomokuname(rs.getString(4)); //
                onshiKenshin.setDataType(rs.getString(5)); // PQ (結果が単一の物理量), CD (順序なし), ST (文字列), CO (順序付き)
                onshiKenshin.setDataValue(rs.getString(6)); //
                onshiKenshin.setDataTani(rs.getString(7));

                bundle.add(onshiKenshin);
            }
        });
        con.setParam(1, ptnum);
        con.executeQuery(sql);

        // sort
        Collections.sort(bundle, (o1, o2) -> {
            int date = o1.getIsoDate().compareTo(o2.getIsoDate());
            int rennum = o1.getRennum() - o2.getRennum();
            return date == 0? rennum : date;
        });

        return bundle;
    }

    /**
     * 資格確認特定健診情報の有無の確認.
     *
     * @param ptnum
     * @return has Onshi Kenshin or not
     */
    public boolean hasKenshin(String ptnum) {
        String sql = "select jisshiymd from tbl_onshi_kenshin_sub where ptid = (select ptid from tbl_ptnum where ptnum = ?) limit 1";
        List<String> jisshiymd = new ArrayList<>();
        OrcaDbConnection con = dao.getConnection(rs -> {
            while (rs.next()) {
                jisshiymd.add(rs.getString(1));
            }
        });
        con.setParam(1, ptnum);
        con.executeQuery(sql);

        return !jisshiymd.isEmpty();
    }
}
