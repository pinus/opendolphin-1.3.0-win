package open.dolphin.orca.test;

import open.dolphin.JsonConverter;
import open.dolphin.orca.orcaapi.OrcaApi;
import open.dolphin.orca.orcaapi.bean.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * @author pns
 */
public class OrcaApiTest {

    private final OrcaApi api = OrcaApi.getInstance();

    public static void main(String[] argv) {
        String userDir = System.getProperty("user.dir");
        System.setProperty("jboss.server.base.dir", userDir);

        OrcaApiTest test = new OrcaApiTest();
        //test.patientgetv2();
        //test.appointmodv2();
        //test.medicalmodv2();
        //test.acceptmodv2();
        //test.acceptlstacceptlstv2();
        //test.appointlstv2();
        //test.medicationmodv2();
        //test.patientlst1v2();
        //test.patientlst2v2();
        //test.patientlst3v2();
        //test.system01lstv2();
        //test.medicalgetv2();
        //test.diseasegetv2();
        test.patientmodv2();
        //test.appointlst2v2();
        //test.acsimulatev2();;
        //test.subjectivesv2();
        //test.visitptlstv2();
        //test.tmedicalgetv2();
        //test.insprogetv2();
        //test.incomeinfv2();
        //test.systeminfv2();
        //test.manageusersv2();
        //test.medicalsetv2();
        //test.patientlst6v2();
        //test.diseasev3();
        //test.masterlastupdatev3();
        //test.system01dailyv2();
        //test.patientlst7v2();
        //test.medicalmodv23();
        //test.contraindicationcheckv2();
        //test.insuranceinf1v2();
        //test.subjectiveslstv2();
    }

    private void patientgetv2() {
        System.out.println("患者基本情報");
        PatientInfores info = api.get("000001");

        System.out.println(JsonConverter.toJson(info));
    }

    private void appointmodv2() {
        System.out.println("予約");

        Appointreq req = new Appointreq();
        req.setPatient_ID("000001");
        req.setAppointment_Date("2019-12-31");
        req.setAppointment_Time("09:00:00");
        req.setDepartment_Code("19");
        req.setPhysician_Code("10001");

        Appointres res = api.post(req, "01");

        System.out.println(JsonConverter.toJson(res));
    }

    private void medicalmodv2() {
        System.out.println("診療行為");

        MedicationInfo mi1 = new MedicationInfo();
        mi1.setMedication_Code("111000110");
        MedicationInfo mi2 = new MedicationInfo();
        mi2.setMedication_Code("199000610");

        MedicalInformation medicalInformation = new MedicalInformation();
        medicalInformation.setMedical_Class("110");
        medicalInformation.setMedical_Class_Number("1");
        medicalInformation.setMedication_info(new MedicationInfo[]{mi1, mi2});

        DiagnosisInformation di = new DiagnosisInformation();
        di.setDepartment_Code("19");
        di.setPhysician_Code("10001");
        di.setMedical_Information(new MedicalInformation[]{medicalInformation});

        Medicalreq req = new Medicalreq();
        req.setPatient_ID("000001");
        req.setDiagnosis_Information(di);

        Medicalres res = api.post(req, "01");

        System.out.println(JsonConverter.toJson(res));
    }

    private void acceptmodv2() {
        System.out.println("受付");

        Acceptreq req = new Acceptreq();
        req.setRequest_Number("01");
        req.setPatient_ID("000001");
        req.setDepartment_Code(("19"));
        req.setPhysician_Code(("10001"));

        Acceptres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void acceptlstv2() {
        System.out.println("指定された日付の受付一覧返却");

        Acceptlstreq req = new Acceptlstreq();
        req.setAcceptance_Date("2017-10-31");
        req.setDepartment_Code("19");
        req.setPhysician_Code("10001");

        Acceptlstres res = api.post(req, "03");

        System.out.println(JsonConverter.toJson(res));
    }

    private void appointlstv2() {
        System.out.println("予約一覧");

        Appointlstreq req = new Appointlstreq();
        req.setAppointment_Date("2017-12-31");
        req.setPhysician_Code("10001");

        Appointlstres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void medicationmodv2() {
        System.out.println("点数マスタ情報登録");

        CommentInformation com = new CommentInformation();
        com.setColumn_Position("4");

        Medicationreq req = new Medicationreq();
        req.setMedication_Code("001700001");
        req.setMedication_Name("朝夕　錠から");
        req.setStartDate("2014-07-01");
        req.setEndDate("9999-12-31");
        req.setComment_Information(new CommentInformation[]{com});
        req.setMedication_Category("2");
        req.setCommercialName("機材商品名称");
        req.setSpecific_Equipment_Code("700590000");

        Medicationres res = api.post(req, "01");

        System.out.println(JsonConverter.toJson(res));
    }

    private void patientlst1v2() {
        System.out.println("患者番号一覧の取得");

        Patientlst1req req = new Patientlst1req();
        req.setBase_StartDate("2017-11-01");
        req.setBase_EndDate("2017-11-30");

        Patientlst1res res = api.post(req, "02");

        System.out.println(JsonConverter.toJson(res));
    }

    private void patientlst2v2() {
        System.out.println("複数の患者情報取得");

        PatientIdInformation pt1 = new PatientIdInformation();
        pt1.setPatient_ID("000001");
        PatientIdInformation pt2 = new PatientIdInformation();
        pt2.setPatient_ID("000002");

        Patientlst2req req = new Patientlst2req();
        req.setPatient_ID_Information(new PatientIdInformation[]{pt1, pt2});

        Patientlst2res res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void patientlst3v2() {
        System.out.println("患者情報取得(氏名検索)");

        Patientlst3req req = new Patientlst3req();
        req.setWholeName("四月朔日*");

        Patientlst2res res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void system01lstv2() {
        System.out.println("システム管理情報の取得");

        System01Managereq req = new System01Managereq();
        req.setBase_Date("2017-12");

        req.setRequest_Number("01");
        Departmentres dRes = api.post(req).getDepartmentres();
        System.out.println(dRes.getDepartment_Information()[0].getWholeName());

        req.setRequest_Number("02");
        Physicianres pRes = api.post(req).getPhysicianres();
        System.out.println(pRes.getPhysician_Information()[0].getWholeName());

        req.setRequest_Number("03");
        pRes = api.post(req).getPhysicianres();
        System.out.println(pRes.getPhysician_Information()[0].getWholeName());

        req.setRequest_Number("04");
        System1001res sRes = api.post(req).getSystem1001res();
        System.out.println(sRes.getMedical_Information().getInstitution_WholeName());
        System.out.println(sRes.getMedical_Information().getInstitution_Code_Kanji());
    }

    private void medicalgetv2() {
        System.out.println("診療情報の返却");

        MedicalInformation3 mInfo = new MedicalInformation3();
        mInfo.setDepartment_Code("19");
        mInfo.setContain_Migration("True");

        Medicalgetreq req = new Medicalgetreq();
        req.setPatient_ID("000001");
        req.setFor_Months("99");
        req.setPerform_Date("2014-10-06");
        req.setMedical_Information(mInfo);

        Medicalget01res res01 = api.post(req, "01").getMedicalget01res();
        Arrays.stream(res01.getMedical_List_Information()).map(MedicalListInformation::getPerform_Date).filter(Objects::nonNull).forEach(System.out::println);

        Medicalget02res res02 = api.post(req, "02").getMedicalget02res();
        Arrays.stream(res02.getMedical_List_Information()[0].getMedical_Information()).map(MedicalInformation::getMedical_Class_Name).filter(Objects::nonNull).forEach(System.out::println);

        Medicalget03res res03 = api.post(req, "03").getMedicalget03res();
        Arrays.stream(res03.getMedical_List_Information()).map(MedicationInfo::getMedical_Class_Name).filter(Objects::nonNull).forEach(System.out::println);

        Medicalget04res res04 = api.post(req, "04").getMedicalget04res();
        Arrays.stream(res04.getMedical_Information()[0].getMedical_Information2()).map(MedicalInformation::getMedical_Class_Name).filter(Objects::nonNull).forEach(System.out::println);
    }

    private void diseasegetv2() {
        System.out.println("病名");

        DiseaseInforeq req = new DiseaseInforeq();
        req.setPatient_ID("000001");
        //req.setBase_Date("2008-02");
        req.setSelect_Mode("All");

        DiseaseInfores res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void patientmodv2() {
        System.out.println("患者登録");

        Patientmodreq req = new Patientmodreq();
        req.setPatient_ID("000002");
        req.setWholeName("淀橋　加米良");
        req.setWholeName_inKana("ヨドバシ　カメラ");
        req.setBirthDate("2017-01-01");
        req.setSex("2");

        HealthInsuranceInformation hi = new HealthInsuranceInformation();
        PersonallyInformation pi = new PersonallyInformation();
        pi.setPregnant_Class("True");
        hi.setPersonally_Information(pi);
        req.setHealthInsurance_Information(hi);

        PaymentInformation pay = new PaymentInformation();
        pay.setReduction_Reason("01");
        ConditionInformation cond = new ConditionInformation();
        cond.setCondition1("condition1");
        req.setPayment_Information(pay);
        req.setCondition_Information(cond);

        Patientmodres res = api.post(req, "01");

        System.out.println(JsonConverter.toJson(res));
    }

    private void appointlst2v2() {
        System.out.println("患者予約情報");

        Appointlst2req req = new Appointlst2req();
        req.setPatient_ID("000001");
        req.setBase_Date("2017-12-01");

        Appointlst2res res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void acsimulatev2() {
        System.out.println("請求金額返却");

        MedicalInformation mi = new MedicalInformation();
        mi.setMedical_Class("120");
        mi.setMedical_Class_Number("1");

        DiagnosisInformation di = new DiagnosisInformation();
        di.setDepartment_Code("19");
        di.setMedical_Information(new MedicalInformation[]{mi});

        Acsimulatereq req = new Acsimulatereq();
        req.setPatient_ID("000001");
        req.setDiagnosis_Information(di);

        Acsimulateres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void subjectivesv2() {
        System.out.println("症状詳記");

        Subjectivesmodreq req = new Subjectivesmodreq();
        req.setPatient_ID("000001");
        req.setPerform_Date("2014-10");
        req.setDepartment_Code("19");
        req.setSubjectives_Detail_Record("03");
        req.setInsurance_Combination_Number("0003");
        req.setSubjectives_Code("必要なんだよ");

        Subjectivesmodres res = api.post(req, "01");

        System.out.println(JsonConverter.toJson(res));
    }

    private void visitptlstv2() {
        System.out.println("受診日指定による来院患者一覧");

        Visitptlstreq req = new Visitptlstreq();
        req.setVisit_Date("2017-10-31");
        req.setDepartment_Code("19");
        req.setRequest_Number("01");

        Visitptlst01res res1 = api.post(req).getVisitptlst01res();
        System.out.println(JsonConverter.toJson(res1));

        req.setRequest_Number("02");
        Visitptlst02res res2 = api.post(req).getVisitptlst02res();
        System.out.println(JsonConverter.toJson(res2));
    }

    private void tmedicalgetv2() {
        System.out.println("中途終了患者情報一覧");
        Tmedicalgetreq req = new Tmedicalgetreq();
        req.setPerform_Date("2019-01-20");

        Tmedicalgetres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void insprogetv2() {
        System.out.println("保険者一覧情報");

        Insprogetreq req = new Insprogetreq();
        req.setInsurance_Number("060");

        Insprogetres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void incomeinfv2() {
        System.out.println("収納情報返却");

        Incomeinfreq req = new Incomeinfreq();
        req.setPatient_ID("000001");
        req.setPerform_Month("2014-10");

        PrivateObjects res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void systeminfv2() {
        System.out.println("システム状態の取得");

        Date date = new Date();
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat t = new SimpleDateFormat("kk:mm:ss");

        Systeminfreq req = new Systeminfreq();
        req.setRequest_Date(d.format(date));
        req.setRequest_Time(t.format(date));

        PrivateObjects res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void manageusersv2() {
        System.out.println("ユーザー管理情報");

        Manageusersreq req = new Manageusersreq();
        req.setRequest_Number("01");

        Manageusersres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void medicalsetv2() {
        System.out.println("セット登録");

        Medicalsetreq req = new Medicalsetreq();
        req.setRequest_Number("04");
        req.setSet_Code("P00100");

        Medicalsetres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void patientlst6v2() {
        System.out.println("全保険組合せ一覧取得");

        Patientlst6req req = new Patientlst6req();
        req.setReqest_Number("01");
        req.setPatient_ID("000001");

        Patientlst6res res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void diseasev3() {
        System.out.println("患者病名登録２");

        DiseaseSingle s1 = new DiseaseSingle();
        DiseaseSingle s2 = new DiseaseSingle();
        s1.setDisease_Single_Code("ZZZ1083");
        s2.setDisease_Single_Code("6900012");

        DiseaseInformation disInfo = new DiseaseInformation();
        disInfo.setDisease_StartDate("2017-12-19");
        disInfo.setDisease_Single(new DiseaseSingle[]{s1, s2});

        DiagnosisInformation diagInfo = new DiagnosisInformation();
        diagInfo.setDepartment_Code("19");

        Diseasereq req = new Diseasereq();
        req.setPatient_ID("000001");
        req.setDiagnosis_Information(diagInfo);
        req.setDisease_Information(new DiseaseInformation[]{disInfo});

        Diseaseres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void masterlastupdatev3() {
        System.out.println("マスタデータ最終更新日取得");

        Masterlastupdatev3req req = new Masterlastupdatev3req();
        //req.setMaster_Id("medication_master");
        //req.setMaster_Id("disease_master");

        Masterlastupdatev3res res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void system01dailyv2() {
        System.out.println("基本情報取得");

        System01dailyreq req = new System01dailyreq();
        req.setRequest_Number("01");

        System01dailyres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void patientlst7v2() {
        System.out.println("患者メモ取得");

        Patientlst7req req = new Patientlst7req();
        req.setRequest_Number("01");
        req.setPatient_ID("000001");
        req.setBase_Date("2019-01-19");
        //req.setMemo_Class("1");

        Patientlst7res res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void medicalmodv23() {
        System.out.println("初診算定日登録");

        Medicalv2req3 req = new Medicalv2req3();
        req.setRequest_Number("00");
        req.setPatient_ID("000001");

        Medicalv2res3 res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void contraindicationcheckv2() {
        System.out.println("薬剤併用禁忌チェック ");

        MedicalInformation6 mi = new MedicalInformation6();
        mi.setMedication_Code("611120055");
        MedicalInformation6[] mis = {mi};

        ContraindicationCheckreq req = new ContraindicationCheckreq();
        req.setRequest_Number("01"); //固定
        req.setPatient_ID("001331");
        req.setPerform_Month("2008-11");
        req.setCheck_Term("1");
        req.setMedical_Information(mis);

        ContraindicationCheckres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void insuranceinf1v2() {
        System.out.println("保険・公費一覧取得 ");

        Insuranceinfreq req = new Insuranceinfreq();
        req.setRequest_Number("01");

        Insuranceinfres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }

    private void subjectiveslstv2() {
        System.out.println("症状詳記情報取得");

        Subjectiveslstreq req = new Subjectiveslstreq();
        req.setRequest_Number("02");
        req.setPatient_ID("016971");
        req.setPerform_Date("2018-10");
        req.setInsurance_Combination_Number("0002");

        Subjectiveslstres res = api.post(req);

        System.out.println(JsonConverter.toJson(res));
    }
}
