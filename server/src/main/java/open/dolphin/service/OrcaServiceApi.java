package open.dolphin.service;

import open.dolphin.dto.*;
import open.dolphin.infomodel.*;
import open.dolphin.orca.ClaimConst;
import open.dolphin.orca.OrcaUserInfo;
import open.dolphin.orca.orcaapi.OrcaApi;
import open.dolphin.orca.orcaapi.bean.*;
import open.dolphin.orca.orcadao.OrcaDao;
import open.dolphin.orca.orcadao.OrcaDbConnection;
import open.dolphin.orca.orcadao.bean.Syskanri;
import open.dolphin.orca.orcadao.bean.Wksryact;
import open.dolphin.util.ModelUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * OrcaServiceApi.
 * ORCA API による ORCA DAO の置き換えの試み.
 *
 * @author pns
 */
public class OrcaServiceApi {

    private OrcaApi api = OrcaApi.getInstance();
    private OrcaDao dao = OrcaDao.getInstance();
    private Logger logger = Logger.getLogger(OrcaServiceApi.class);

    /**
     * 中途終了患者情報.
     * TBL_WKSRYACT ワーク診療行為 (中途終了データ). Mode:画面展開, Mode2:中途データ有無
     *
     * @param spec PatientVisitSpec (patientId と date を使用)
     * @return Wksryact
     */
    public Wksryact getWksryact(PatientVisitSpec spec) {
        Wksryact wksryact = new Wksryact();

        String ptId = spec.getPatientId();
        String date = spec.getDate(); // 2019-02-20 型式

        Tmedicalgetreq req = new Tmedicalgetreq();
        req.setPatient_ID(ptId);
        req.setPerform_Date(date);
        Tmedicalgetres res = api.post(req);
        TmedicalListInformation[] infos = res.getTmedical_List_Information();

        if (infos != null) {
            for (TmedicalListInformation info : infos) {
                if (info.getPatient_Information().getPatient_ID().equals(ptId)) {
                    wksryact.setMedicalUid(info.getMedical_Uid());
                    wksryact.setMedicalMode(info.getMedical_Mode());
                    wksryact.setMedicalMode2(info.getMedical_Mode2());
                    break;
                }
            }
        }
        // bug? Perform_Date を本日以外で指定した場合, その日の中途データがあっても Mode2 = 0 が返る.
        // 中途データがなければ null が返るので，0 が返ってきたということは即ち中途データがあるということである.
        if (Objects.nonNull(wksryact.getMedicalMode2())) {
            wksryact.setMedicalMode2("1");
        }

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
     * 職員情報. 職員コードと名前を返す.
     *
     * @return Syskanri のリスト
     */
    public List<Syskanri> getSyskanri() {
        List<Syskanri> ret = new ArrayList<>();

        System01Managereq req = new System01Managereq();
        req.setRequest_Number("02"); // Dr.
        System01Manageres res = api.post(req);
        List<PhysicianInformation> infos = new ArrayList<>(Arrays.asList(res.getPhysicianres().getPhysician_Information()));

        req.setRequest_Number("03"); // Dr. 以外
        res = api.post(req);
        infos.addAll(Arrays.asList(res.getPhysicianres().getPhysician_Information()));

        infos.forEach(info -> {
            Syskanri syskanri = new Syskanri();
            syskanri.setCode(info.getCode());
            syskanri.setWholeName(info.getWholeName());
            syskanri.setKanaName(info.getWholeName_inKana());
            ret.add(syskanri);
        });

        return ret;
    }

    /**
     * StampInfo を元に OrcaApi と TBL_TENSU でスタンプの実体を作る.
     * OrcaApi では taniname が取れない.
     *
     * @param stampInfo ModuleInfoBean
     * @return List of ModuleModel
     */
    public List<ModuleModel> getStamp(ModuleInfoBean stampInfo) {

        Medicalsetreq req = new Medicalsetreq();
        req.setRequest_Number("04"); // セット内容取得
        req.setSet_Code(stampInfo.getStampId()); // P00001
        Medicalsetres res = api.post(req);

        logger.info("Set name = " + res.getSet_Code_Name());

        // TBL_TENSU からの情報が必要な ClaimItem を srycd をキーに保存する
        HashMap<String, ClaimItem> claimItemMap = new HashMap<>();
        List<ModuleModel> ret = new ArrayList<>();

        Arrays.stream(res.getMedical_Information().getMedical_Info())
                .filter(info -> Objects.nonNull(info.getMedical_Class())).forEach(info -> {
            String srysyukbn = info.getMedical_Class();

            // ModuleModel 作成.
            // ModuleModel = ModuleInfoBean (stampInfo) + ClaimBundle (IInfoModel)
            ModuleModel stamp = new ModuleModel();
            ret.add(stamp);

            // ModuleModel 内に作られた ModuleInfoBean に stampInfo 情報をセット
            ModuleInfoBean moduleInfoBean = stamp.getModuleInfo();
            moduleInfoBean.setStampName(stampInfo.getStampName());
            moduleInfoBean.setStampRole(IInfoModel.ROLE_P);

            // kbn の範囲からエンティティーを取得
            String entity = ClaimConst.getEntity(srysyukbn);
            moduleInfoBean.setEntity(entity);

            // entity の名前を取得
            String orderName = ClaimConst.EntityNameMap.get(entity); // 処方,生体検査, etc

            // ClaimBundle 作成 (BundleMed > BundleDolphine > ClaimBundle)
            BundleDolphin bundle;

            // ENTITY_MED_ORDER の場合，BundleMed にして院内／院外をセット
            if (entity.equals(IInfoModel.ENTITY_MED_ORDER)) {
                bundle = new BundleMed();
                bundle.setMemo(ClaimConst.EXT_MEDICINE);

            } else {
                // 処方以外は BundleDolphin
                bundle = new BundleDolphin();
            }

            bundle.setOrderName(orderName);
            bundle.setClassCode(srysyukbn);
            bundle.setClassCodeSystem(ClaimConst.CLASS_CODE_ID);
            bundle.setClassName(ClaimConst.getSrysyukbnName(srysyukbn)); // 別紙2 診療種区分一覧
            bundle.setBundleNumber(info.getMedical_Class_Number()); // 14TD

            // stamp のひな形完成 (ModuleInfoBean + ClaimBundle)
            stamp.setModel(bundle);

            // bundle に入れていく ClaimItem 作成
            Arrays.stream(info.getMedication_Info())
                    .filter(med -> Objects.nonNull(med.getMedication_Name())).forEach(med -> {
                ClaimItem item = new ClaimItem();
                item.setCode(med.getMedication_Code()); // 612320391
                item.setName(med.getMedication_Name()); // イサロン顆粒２５％
                item.setClassCodeSystem(ClaimConst.SUBCLASS_CODE_ID);

                // 数量の小数点以下処理 (.0 は削除)
                String dose = med.getMedication_Number(); // 0.5
                if (Objects.nonNull(dose) && dose.endsWith(".0")) {
                    dose = dose.substring(0, dose.length() - 2);
                }
                item.setNumber(dose);

                String srycd = item.getCode(); // 612320391

                if (srycd.startsWith(ClaimConst.SYUGI_CODE_START)) { // 1
                    // 手技の場合
                    item.setClassCode(String.valueOf(ClaimConst.SYUGI));

                } else if (srycd.startsWith(ClaimConst.YAKUZAI_CODE_START)) {
                    // 薬剤の場合
                    item.setClassCode(String.valueOf(ClaimConst.YAKUZAI));
                    item.setNumberCode(ClaimConst.YAKUZAI_TOYORYO);
                    item.setNumberCodeSystem(ClaimConst.NUMBER_CODE_ID);
                    // item.setUnit(tensu.getTaniname());
                    claimItemMap.put(srycd, item); // 後で TBL_TENSU から単位を設定するために保存

                } else if (srycd.startsWith(ClaimConst.ZAIRYO_CODE_START)) {
                    // 材料の場合
                    item.setClassCode(String.valueOf(ClaimConst.ZAIRYO));
                    item.setNumberCode(ClaimConst.ZAIRYO_KOSU);
                    item.setNumberCodeSystem(ClaimConst.NUMBER_CODE_ID);
                    // item.setUnit(tensu.getTaniname());
                    claimItemMap.put(srycd, item); // 後で TBL_TENSU から単位を設定するために保存

                } else if (srycd.startsWith(ClaimConst.ADMIN_CODE_START)) {
                    // 部位は bundle に addClaimItem されるが，用法の場合は item は使われない.
                    item.setClassCode(String.valueOf(ClaimConst.YAKUZAI));
                    item.setNumber("");

                } else if (srycd.startsWith(ClaimConst.RBUI_CODE_START)) {
                    // 放射線部位の場合
                    item.setClassCode(String.valueOf(ClaimConst.SYUGI));

                } else if (srycd.startsWith(ClaimConst.COMMENT_CODE_START)) {
                    // コメントコードの場合
                    item.setClassCode(String.valueOf(ClaimConst.SYUGI));
                    item.setName(med.getMedication_Name());

                } else {
                    // どれでもない場合
                    logger.info("OrcaMasterDao: no srysykbn found : inputcd = " + srycd);
                }

                // 用法 (001000000-799) の処理. (001000800-999 = 部位)
                // 用法は bundle にセットして ClaimItem としては保存しない.
                if (srycd.matches("^001000[0-7].*")) {

                    // 頓用処理
                    if (srycd.startsWith("0010005")) {
                        bundle.setClassCode(IInfoModel.RECEIPT_CODE_TONYO);
                    }

                    bundle.setAdmin(med.getMedication_Name());
                    bundle.setAdminCode(srycd);

                } else {
                    bundle.addClaimItem(item);
                }
            });

        });

        // tbl_tensu から単位を検索 (api では取れない)
        Set<String> srycds = claimItemMap.keySet();
        if (srycds.isEmpty()) {
            return ret;
        }

        String tensuSql = "select taniname, srycd from tbl_tensu where hospnum = ? and srycd in (?)";

        if (srycds.size() > 1) {
            tensuSql = tensuSql.replace("(?)", "(?" + StringUtils.repeat(",?", srycds.size() - 1) + ")");
        }

        OrcaDbConnection con = dao.getConnection(rs -> {
            while (rs.next()) {
                String taniname = rs.getString(1);
                String srycd = rs.getString(2);
                ClaimItem item = claimItemMap.get(srycd);
                item.setUnit(taniname);
            }
        });
        con.setParam(1, dao.getHospNum());
        int index = 2;
        for (String srycd : srycds) {
            con.setParam(index++, srycd);
        }
        con.executeQuery(tensuSql);

        return ret;
    }

    /**
     * 患者病名情報の取得 (diseasegetv2) から RegisteredDiagnosisModel を作る.
     * toDate : 基準月 2015-05
     * fromDate : 基準月を古い方にこの月まで拡張する. 開院月まで拡張すると全病名がひっかかる.
     *
     * @param spec DiagnosisSearchSpec (patientId, toDate, fromDate を使用)
     * @return List of RegisteredDiagnosisModel
     */
    public List<RegisteredDiagnosisModel> getOrcaDisease(DiagnosisSearchSpec spec) {
        List<RegisteredDiagnosisModel> ret = new ArrayList<>();

        DiseaseInforeq req = new DiseaseInforeq();
        req.setPatient_ID(spec.getPatientId());

        // 基準日
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String baseDate = spec.getToDate() == null ? "" : sdf.format(spec.getToDate()); // "" の場合本日になる
        req.setBase_Date(baseDate);

        // 古い病名も取りたい場合 fromDate で古い方へ拡張
        req.setSelect_Mode(Objects.isNull(spec.getFromDate()) ? null : "All"); //  All で全部取ってきてあとで不要なものを捨てる
        String fromDate = Objects.isNull(spec.getFromDate()) ?
                "0000-00-00" : sdf.format(spec.getFromDate()) + "-01";

        DiseaseInfores res = api.post(req);

        Arrays.stream(res.getDisease_Information())
                .filter(info -> Objects.nonNull(info.getDisease_Name())
                        && (!"All".equals(req.getSelect_Mode()) // "All" が付いていなければ全部採用, 付いていれば必要なものを抽出
                        || Objects.isNull(info.getDisease_EndDate()) //  endDate null なら採用
                        || info.getDisease_EndDate().compareTo(fromDate) > 0)) // endDate が fromDate より新しければ採用
                .forEach(info -> {

                    RegisteredDiagnosisModel rd = new RegisteredDiagnosisModel();

                    StringBuilder name = new StringBuilder();
                    List<String> code = new ArrayList<>();
                    Arrays.stream(info.getDisease_Single())
                            .filter(single -> Objects.nonNull(single.getDisease_Single_Code())).forEach(single -> {
                        name.append(single.getDisease_Single_Name());
                        code.add(single.getDisease_Single_Code().replace("ZZZ", ""));
                    });
                    rd.setDiagnosis(name.toString());
                    rd.setDiagnosisCode(String.join(".", code));
                    rd.setStartDate(info.getDisease_StartDate());
                    rd.setEndDate(info.getDisease_EndDate());
                    rd.setStatus("ORCA");
                    rd.setDiagnosisOutcomeModel(ModelUtils.toDolphinOutcome(info.getDisease_OutCome()));

                    ret.add(rd);
                });

        return ret;
    }

    /**
     * DocumentModel から中途終了データ作成 (medicalmodv2).
     *
     * @param doc DocumentModel
     * @return ApiResult
     */
    public ApiResult sendDocument(DocumentModel doc) {

        Medicalreq req = new Medicalreq();
        String ptId = doc.getKarte().getPatient().getPatientId(); // 000001

        // Patient_ID, Perform_Date, Perform_Time, Medical_Uid をセット
        req.setPatient_ID(ptId);
        String confirmDate = ModelUtils.getDateTimeAsString(doc.getDocInfo().getConfirmDate()); // 2008-02-01T08:30:00
        String[] date = confirmDate.split("T");
        req.setPerform_Date(date[0]);
        req.setPerform_Time(date[1]);

        // 中途終了データがあれば karte uid を調べてセットする
        PatientVisitSpec spec = new PatientVisitSpec();
        spec.setPatientId(ptId);
        spec.setDate(date[0]);
        String karteUid = getWksryact(spec).getMedicalUid();
        req.setMedical_Uid(karteUid);

        // 健康保険
        String insuranceUid = doc.getDocInfo().getHealthInsuranceGUID();
        Collection<PVTHealthInsuranceModel> ins = doc.getKarte().getPatient().getPvtHealthInsurances();

        // 該当する uid を探す. なければ最初のものを採用.
        PVTHealthInsuranceModel first = ins.iterator().next();
        PVTHealthInsuranceModel insuranceModel = ins.stream()
                .filter(model -> insuranceUid.equals(model.getGUID())).findFirst().orElse(first);

        String combinationNumber = insuranceModel.getGUID();
        String providerClass = insuranceModel.getInsuranceClassCode();
        String providerNumber = insuranceModel.getInsuranceNumber();
        String providerWholeName = insuranceModel.getInsuranceClass();
        String personSymbol = insuranceModel.getClientGroup();
        String personNumber = insuranceModel.getClientNumber();
        String familyClass = "true".equals(insuranceModel.getFamilyClass()) ? "1" : "2"; // true=1=本人

        // == CLAIM 対応 == class code 変換, 公費単独・自費の場合の処理, GUID の処理.
        if ("XX".equals(providerClass)) {
            // 公費単独
            providerClass = "";
            providerNumber = "";
            providerWholeName = "";
            personSymbol = "";
            personNumber = "";
        } else {
            providerClass = ModelUtils.claimInsuranceCodeToOrcaInsuranceCode(providerClass);
            // 自費の場合
            if ("9999".equals(providerNumber)) {
                providerNumber = "";
            }
            if ("記載なし".equals(insuranceModel.getClientGroup())) {
                personSymbol = "";
            }
            if ("記載なし".equals(insuranceModel.getClientNumber())) {
                personNumber = "";
            }
        }
        if (combinationNumber.length() != 4) { // Claim だと長い GUID が付いてる
            combinationNumber = "";
        }


        // api bean に入力する
        HealthInsuranceInformation hinsInfo = new HealthInsuranceInformation();
        hinsInfo.setInsurance_Combination_Number(combinationNumber);
        hinsInfo.setInsuranceProvider_Class(providerClass);
        hinsInfo.setInsuranceProvider_Number(providerNumber);
        hinsInfo.setInsuranceProvider_WholeName(providerWholeName);
        hinsInfo.setHealthInsuredPerson_Symbol(personSymbol);
        hinsInfo.setHealthInsuredPerson_Number(personNumber);
        hinsInfo.setRelationToInsuredPerson(familyClass);
        hinsInfo.setCertificate_StartDate(insuranceModel.getStartDate());
        hinsInfo.setCertificate_ExpiredDate(insuranceModel.getExpiredDate());

        // 公費
        PVTPublicInsuranceItemModel[] pIns = insuranceModel.getPVTPublicInsuranceItem();
        if (Objects.nonNull(pIns)) {
            List<PublicinsuranceInformation> pubInfos = new ArrayList<>();

            Arrays.stream(pIns).forEach(pModel -> {
                // == CLAIM 対応 ==
                if ("mikinyu".equals(pModel.getRecipient())) {
                    pModel.setRecipient("");
                }

                // api bean に入力
                PublicinsuranceInformation pubInfo = new PublicinsuranceInformation();
                pubInfo.setPublicInsurance_Name(pModel.getProviderName());
                pubInfo.setPublicInsurer_Number(pModel.getProvider());
                pubInfo.setPublicInsuredPerson_Number(pModel.getRecipient());
                pubInfo.setCertificate_IssuedDate(pModel.getStartDate());
                pubInfo.setCertificate_ExpiredDate(pModel.getExpiredDate());
                pubInfos.add(pubInfo);
            });

            hinsInfo.setPublicInsurance_Information(pubInfos.toArray(new PublicinsuranceInformation[0]));
        }

        // ClaimBundle: ClaimBundle=MedicalInformation, ClaimItem=MedicationInfo
        List<MedicalInformation> medicalInfos = new ArrayList<>();
        doc.getModules().forEach(moduleModel -> {
            IInfoModel infoModel = moduleModel.getModel();

            if (infoModel instanceof ClaimBundle) {
                // MedicalInformation
                MedicalInformation medicalInfo = new MedicalInformation();

                ClaimBundle bundle = (ClaimBundle) infoModel;
                medicalInfo.setMedical_Class(bundle.getClassCode());
                medicalInfo.setMedical_Class_Name(bundle.getClassName());
                medicalInfo.setMedical_Class_Number(bundle.getBundleNumber());

                // MedicationInfo
                List<MedicationInfo> medicationInfos = new ArrayList<>();

                // ClaimItems into medicationInfos
                ClaimItem[] cItems = bundle.getClaimItem();
                if (Objects.nonNull(cItems)) {
                    Arrays.stream(cItems).forEach(item -> {
                        MedicationInfo medInfo = new MedicationInfo();
                        medInfo.setMedication_Code(item.getCode());
                        medInfo.setMedication_Name(item.getName());
                        medInfo.setMedication_Number(item.getNumber());
                        medicationInfos.add(medInfo);
                    });
                }

                // Admin into medicationInfos
                if (Objects.nonNull(bundle.getAdmin())) {
                    MedicationInfo admin = new MedicationInfo();
                    admin.setMedication_Code(bundle.getAdminCode());
                    admin.setMedication_Name(bundle.getAdmin());
                    medicationInfos.add(admin);
                }
                // MedicationInfos を MedicalInfo にセット
                medicalInfo.setMedication_info(medicationInfos.toArray(new MedicationInfo[0]));

                // MedicalInformation を追加
                medicalInfos.add(medicalInfo);
            }
        });

        // DiagnosisInformation を構築して medilcalreq にセット.
        String drName = doc.getCreator().getCommonName();
        String orcaUserCode = OrcaUserInfo.getOrcaUserCode(drName);
        String deptCode = doc.getCreator().getDepartmentModel().getDepartment(); // 19

        DiagnosisInformation diagnosisInfo = new DiagnosisInformation();
        diagnosisInfo.setDepartment_Code(deptCode);
        diagnosisInfo.setPhysician_Code(orcaUserCode);
        diagnosisInfo.setHealthInsurance_Information(hinsInfo);
        diagnosisInfo.setMedical_Information(medicalInfos.toArray(new MedicalInformation[0]));
        req.setDiagnosis_Information(diagnosisInfo);

        String classNum = Objects.isNull(karteUid) ? "01" : "03"; // 新規 or 変更

        Medicalres res = api.post(req, classNum);

        // 結果の構築
        ApiResult result = new ApiResult();
        result.setDate(res.getInformation_Date());
        result.setTime(res.getInformation_Time());
        result.setApiResult(res.getApi_Result());
        result.setApiResultMessage(res.getApi_Result_Message());

        logger.info(String.format("sendDocument [%s] class[%s] karteId[%s] apiResult[%s]",
                ptId, classNum, karteUid, result.getApiResult()));

        MedicalMessageInformation messageInfo = res.getMedical_Message_Information();
        if (Objects.nonNull(messageInfo)) {
            result.setErrorCode(messageInfo.getMedical_Result());
            result.setErrorMessage(messageInfo.getMedical_Result_Message());

            MedicalWarningInfo[] warningInfo = messageInfo.getMedical_Warning_Info();
            if (Objects.nonNull(warningInfo)) {
                List<ApiWarning> warnings = new ArrayList<>();
                Arrays.stream(warningInfo).filter(w -> Objects.nonNull(w.getMedical_Warning())).forEach(w -> {
                    ApiWarning warning = new ApiWarning();
                    warning.setWarning(w.getMedical_Warning());
                    warning.setWarningMessage(w.getMedical_Warning_Message());
                    warning.setWarningCode(w.getMedical_Warning_Code());
                    warnings.add(warning);
                    logger.info(String.format("sendDocument warning[%s]", warning.getWarning()));
                });
                result.setWarningInfo(warnings.toArray(new ApiWarning[0]));
            }
        }

        return result;
    }

    /**
     * medicalmodv2 で ORCA に病名を送る.
     *
     * @param diagnoses List of RegisteredDiagnosisModel
     * @return ApiResult
     */
    public ApiResult sendDiagnoses(List<RegisteredDiagnosisModel> diagnoses) {
        RegisteredDiagnosisModel firstDiag = diagnoses.get(0);

        Medicalreq req = new Medicalreq();

        String ptId = firstDiag.getKarte().getPatient().getPatientId();
        String ptName = firstDiag.getKarte().getPatient().getFullName();
        String confirmDate = ModelUtils.getDateTimeAsString(firstDiag.getConfirmed()); // 2008-02-01T08:30:00
        String[] date = confirmDate.split("T");

        req.setPatient_ID(ptId);
        req.setPerform_Date(date[0]);
        req.setPerform_Time(date[1]);

        List<DiseaseInformation> diseaseInfos = new ArrayList<>();

        // updated -> added の順番に並んでいるので, その順番を変更してはならない
        diagnoses.stream().forEachOrdered(rd -> {
            List<DiseaseSingle> singles = new ArrayList<>();
            Arrays.stream(ModelUtils.toOrcaDiseaseSingle(rd.getDiagnosisCode())).forEach(code -> {
                DiseaseSingle single = new DiseaseSingle();
                single.setDisease_Single_Code(code);
                singles.add(single);
            });

            DiseaseInformation diseaseInfo = new DiseaseInformation();
            diseaseInfo.setDisease_Single(singles.toArray(new DiseaseSingle[0]));
            diseaseInfo.setDisease_StartDate(rd.getStartDate());
            diseaseInfo.setDisease_EndDate(rd.getEndDate());
            diseaseInfo.setDisease_OutCome(ModelUtils.toOrcaOutcome(rd.getDiagnosisOutcomeModel()));
            diseaseInfo.setDisease_InOut("O");

            String category = rd.getCategory();
            if ("mainDiagnosis".equals(category)) {
                diseaseInfo.setDisease_Category("PD"); // category
            } else if ("suspectedDiagnosis".equals(category)) {
                diseaseInfo.setDisease_SuspectedFlag("S"); // suspected flag
            }
            diseaseInfos.add(diseaseInfo);
        });

        // DiagnosisInformation を構築して medilcalreq にセット.
        String drName = firstDiag.getCreator().getCommonName();
        String orcaUserCode = OrcaUserInfo.getOrcaUserCode(drName);
        String deptCode = firstDiag.getCreator().getDepartmentModel().getDepartment(); // 19

        DiagnosisInformation diagnosisInfo = new DiagnosisInformation();
        diagnosisInfo.setDepartment_Code(deptCode);
        diagnosisInfo.setPhysician_Code(orcaUserCode);
        diagnosisInfo.setDisease_Information(diseaseInfos.toArray(new DiseaseInformation[0]));
        req.setDiagnosis_Information(diagnosisInfo);

        // ORCA Api に post する
        ApiResult result = postDiagnosis(req);
        String apiResult = result.getApiResult();

        // 他端末で使用中(apiResult=90)の場合はバックグランドで再送を繰り返す.
        int retryCounter = 0;
        int maxRetry = 100;
        long wait = 15000; // 15秒毎に100回=25分

        while ("90".equals(apiResult) && retryCounter < maxRetry) {
            retryCounter++;
            logger.info("[" + ptId + "] busy, waiting for retrial: " + retryCounter);

            try {
                Thread.sleep(wait);
            } catch (Exception e) {
            }

            result = postDiagnosis(req);
            apiResult = result.getApiResult();
        }

        logger.info(String.format("sendDiagnosis [%s] User[%s] apiResult[%s]", ptId , orcaUserCode, apiResult));
        return result;
    }

    /**
     * sendDiagnoses の Api post 部分.
     *
     * @param req Medicalreq
     * @return ApiResult
     */
    private ApiResult postDiagnosis(Medicalreq req) {
        Medicalres res = api.post(req, "01");

        // 結果の構築
        ApiResult result = new ApiResult();
        result.setDate(res.getInformation_Date());
        result.setTime(res.getInformation_Time());
        result.setApiResult(res.getApi_Result());
        result.setApiResultMessage(res.getApi_Result_Message());

        DiseaseMessageInformation messageInfo = res.getDisease_Message_Information();
        if (Objects.nonNull(messageInfo)) {
            result.setErrorCode(messageInfo.getDisease_Result());
            result.setErrorMessage(messageInfo.getDisease_Result_Message());

            DiseaseWarningInfo[] warningInfo = messageInfo.getDisease_Warning_Info();
            if (Objects.nonNull(warningInfo)) {
                List<ApiWarning> warnings = new ArrayList<>();
                Arrays.stream(warningInfo).filter(w -> Objects.nonNull(w.getDisease_Warning())).forEach(w -> {
                    ApiWarning warning = new ApiWarning();
                    warning.setWarning(w.getDisease_Warning());
                    warning.setWarningMessage(w.getDisease_Warning_Message());
                    warning.setWarningCode(w.getDisease_Warning_Code());
                    warnings.add(warning);
                    logger.info(String.format("postDiagnosis warning[%s]", warning.getWarning()));
                });
                result.setWarningInfo(warnings.toArray(new ApiWarning[0]));
            }
        }
        return result;
    }

    /**
     * subjectivesv2 で ORCA に症状詳記を送る.
     *
     * @param spec SubjectivesSpec
     * @return ApiResult
     */
    public ApiResult sendSubjectives(SubjectivesSpec spec) {
        Subjectivesmodreq req = new Subjectivesmodreq();
        req.setPatient_ID(spec.getPatientId());
        req.setPerform_Date(spec.getPerformDate());
        req.setDepartment_Code(spec.getDepartmentCode());
        req.setInsurance_Combination_Number(spec.getInsuranceCombinationNumber());
        req.setSubjectives_Detail_Record(spec.getCode()); // API で code と record が
        req.setSubjectives_Code(spec.getRecord());        // 逆に定義されてしまっている

        Subjectivesmodres res = api.post(req, spec.getRequestNumber());

        ApiResult result = new ApiResult();
        result.setDate(res.getInformation_Date());
        result.setTime(res.getInformation_Time());
        result.setApiResult(res.getApi_Result());
        result.setApiResultMessage(res.getApi_Result_Message());

        ApiWarningMessageInformation[] warningInfo = res.getApi_Warning_Message_Information();
        if (Objects.nonNull(warningInfo)) {
            ApiWarning[] warnings = Arrays.stream(warningInfo)
                    .map(ApiWarningMessageInformation::getApi_Warning_Message)
                    .map(w -> {
                        ApiWarning warning = new ApiWarning();
                        warning.setWarningMessage(w);
                        return warning;
                    }).toArray(ApiWarning[]::new);
            result.setWarningInfo(warnings);
        }
        return result;
    }

    /**
     * subjectiveslstv2 で ORCA から症状詳記を取得する.
     *
     * @param spec SubjectivesSpec
     * @return List of SubjectivesSpec
     */
    public List<SubjectivesSpec> getSubjectives(SubjectivesSpec spec) {
        Subjectiveslstreq req = new Subjectiveslstreq();
        req.setRequest_Number("02"); // 該当詳記を返却
        req.setPatient_ID(spec.getPatientId());
        req.setPerform_Date(spec.getPerformDate());
        req.setDepartment_Code(spec.getDepartmentCode());
        req.setInsurance_Combination_Number(spec.getInsuranceCombinationNumber());
        req.setSubjectives_Detail_Record(spec.getCode());

        Subjectiveslstres res = api.post(req);

        List<SubjectivesSpec> ret = new ArrayList<>();
        if (Objects.nonNull(res.getSubjectives_Information())) {
            Arrays.stream(res.getSubjectives_Information()).forEach(info -> {
                SubjectivesSpec s = new SubjectivesSpec();
                s.setPatientId(res.getPatient_Information().getPatient_ID());
                s.setPerformDate(res.getPerform_Date());
                s.setDepartmentCode(info.getDepartment_Code());
                //s.setInsuranceCombinationNumber(info.getHealthInsurance_Information().getInsurance_Combination_Number());
                s.setCode(info.getSubjectives_Detail_Record());
                s.setRecord(info.getSubjectives_Detail_Record_WholeName());
                ret.add(s);
            });
        }

        return ret;
    }
}
