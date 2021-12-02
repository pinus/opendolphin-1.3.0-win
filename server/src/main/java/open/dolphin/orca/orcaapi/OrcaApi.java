package open.dolphin.orca.orcaapi;

import open.dolphin.JsonConverter;
import open.dolphin.orca.OrcaHostInfo;
import open.dolphin.orca.orcaapi.bean.*;

import java.net.URI;

/**
 * OrcaApi.
 *
 * @author pns
 */
public class OrcaApi {
    private static final OrcaApi ORCA_API = new OrcaApi();

    private final OrcaHostInfo hostInfo = OrcaHostInfo.getInstance();
    private final OrcaApiHandler handler = OrcaApiHandler.getInstance();

    private OrcaApi() {
    }

    /**
     * OrcaApi のインスタンス.
     *
     * @return OrcaApi
     */
    public static OrcaApi getInstance() {
        return ORCA_API;
    }

    /**
     * 患者基本情報の取得.
     *
     * @param id 患者 id
     * @return PatientInfores
     */
    public PatientInfores get(String id) {
        String url = String.format("%s?id=%s&format=json", OrcaApiUrl.PATIENTGETV2, id);
        Response response = request(url);
        return response.getPatientinfores();
    }

    /**
     * 予約.
     *
     * @param appointreq Appointreq
     * @param classNum   class=01(予約受付), class=02(予約取消)
     * @return Appointres
     */
    public Appointres post(Appointreq appointreq, String classNum) {
        Request req = new Request();
        req.setAppointreq(appointreq);

        String url = OrcaApiUrl.APPOINTMODV2 + "?class=" + classNum;
        Response response = request(url, req);
        return response.getAppointres();
    }

    /**
     * 中途終了データ作成.
     *
     * @param medicalreq Medicalreq
     * @param classNum   class=01(登録), class=02(削除), class=03(変更), class=04(外来追加)
     * @return Medicalres
     */
    public Medicalres post(Medicalreq medicalreq, String classNum) {
        Request req = new Request();
        req.setMedicalreq(medicalreq);

        String url = OrcaApiUrl.MEDICALMODV2 + "?class=" + classNum;
        Response response = request(url, req);
        return response.getMedicalres();
    }

    /**
     * 受付.
     *
     * @param acceptreq Acceptreq
     * @return Accreptres
     */
    public Acceptres post(Acceptreq acceptreq) {
        Request req = new Request();
        req.setAcceptreq(acceptreq);

        String url = OrcaApiUrl.ACCEPTMODV2;
        Response response = request(url, req);
        return response.getAcceptres();
    }

    /**
     * 指定された日付の受付一覧返却.
     *
     * @param acceptreq Acceptreq
     * @param classNum  class=01(会計待ち対象), class=02(会計済み対象), class=03(全受付対象)
     * @return Acceptlstres
     */
    public Acceptlstres post(Acceptlstreq acceptreq, String classNum) {
        Request req = new Request();
        req.setAcceptlstreq(acceptreq);

        String url = OrcaApiUrl.ACCEPTLSTV2 + "?class=" + classNum;
        Response response = request(url, req);
        return response.getAcceptlstres();
    }

    /**
     * 予約一覧. class=01(予約一覧取得)
     *
     * @param appointreq Appointlstreq
     * @return Appointlstres
     */
    public Appointlstres post(Appointlstreq appointreq) {
        Request req = new Request();
        req.setAppointlstreq(appointreq);

        String url = OrcaApiUrl.APPOINTLSTV2 + "?class=01";
        Response response = request(url, req);
        return response.getAppointlstres();
    }

    /**
     * 点数マスタ情報登録.
     *
     * @param medicationreq Medicationreq
     * @param classNum      class=01(登録処理), class=02(削除処理), class=03(終了日設定処理), class=04(期間変更処理)
     * @return Medicationres
     */
    public Medicationres post(Medicationreq medicationreq, String classNum) {
        Request req = new Request();
        req.setMedicationreq(medicationreq);

        String url = OrcaApiUrl.MEDICATONMODV2 + "?class=" + classNum;
        Response response = request(url, req);
        return response.getMedicationres();
    }

    /**
     * 患者番号一覧の取得.
     *
     * @param patientlst1req Patientlst1req
     * @param classNum       class=01(新規・更新対象) ※患者番号テーブルの更新日・登録日が、開始日〜終了日の範囲内であれば対象とします。
     *                       class=02 (新規対象) ※患者番号テーブルの登録日が、開始日〜終了日の範囲内であれば対象とします。期間内に新規登録した患者のみ対象となります。
     * @return Patientlst1res
     */
    public Patientlst1res post(Patientlst1req patientlst1req, String classNum) {
        Request req = new Request();
        req.setPatientlst1req(patientlst1req);

        String url = OrcaApiUrl.PATIENTLST1V2 + "?class=" + classNum;
        Response response = request(url, req);
        return response.getPatientlst1res();
    }

    /**
     * 複数の患者情報取得.
     *
     * @param patientlst2req Patientlst2req
     * @return Patientlst2res
     */
    public Patientlst2res post(Patientlst2req patientlst2req) {
        Request req = new Request();
        req.setPatientlst2req(patientlst2req);

        String url = OrcaApiUrl.PATIENTLST2V2 + "?class=01";
        Response response = request(url, req);
        return response.getPatientlst2res();
    }

    /**
     * 患者情報取得 (氏名検索).
     *
     * @param patientlst3req Patientlst3req
     * @return Patientlst2res
     */
    public Patientlst2res post(Patientlst3req patientlst3req) {
        Request req = new Request();
        req.setPatientlst3req(patientlst3req);

        String url = OrcaApiUrl.PATIENTLST3V2 + "?class=01";
        Response response = request(url, req);
        return response.getPatientlst2res();
    }

    /**
     * システム管理情報の取得. (診療科コード一覧/ドクター・職員コード一覧/医療機関基本情報)
     *
     * @param system01managereq System01managereq
     * @return System01Manageres (contains either Departmentres/Physicianres/System1001res)
     */
    public System01Manageres post(System01Managereq system01managereq) {
        Request req = new Request();
        req.setSystem01_managereq(system01managereq);

        String url = OrcaApiUrl.SYSTEM01LSTV2;
        Response response = request(url, req);
        System01Manageres system01manageres = new System01Manageres();
        system01manageres.setDepartmentres(response.getDepartmentres());
        system01manageres.setPhysicianres(response.getPhysicianres());
        system01manageres.setSystem1001res(response.getSystem1001res());
        return system01manageres;
    }

    /**
     * 診療情報の返却.
     *
     * @param medicalgetreq Medicalgetreq
     * @param classNum      class=01(受診履歴一覧取得)※診療年月の受診履歴日の取得
     *                      class=02(診療行為剤内容詳細取得)※診療日付と診療科の診療行為内容の剤内容を取得
     *                      class=03(診療月診療コード情報取得)※診療年月の診療行為内容の診療コード内容を取得
     *                      class=04(診療区分別剤点数取得)(xml2のみ)※診療年月の剤点数を算定日・診療区分順に取得
     * @return Medicalgetres (contains either medicalget01res/02res/03res/04res)
     */
    public Medicalgetres post(Medicalgetreq medicalgetreq, String classNum) {
        Request req = new Request();
        req.setMedicalgetreq(medicalgetreq);

        String url = OrcaApiUrl.MEDICALGETV2 + "?class=" + classNum;
        Response response = request(url, req);

        Medicalgetres medicalgetres = new Medicalgetres();
        medicalgetres.setMedicalget01res(response.getMedicalget01res());
        medicalgetres.setMedicalget02res(response.getMedicalget02res());
        medicalgetres.setMedicalget03res(response.getMedicalget03res());
        medicalgetres.setMedicalget04res(response.getMedicalget04res());
        return medicalgetres;
    }

    /**
     * 患者病名情報の取得.
     *
     * @param diseaseInforeq DiseaseInforeq
     * @return DiseaseInfores
     */
    public DiseaseInfores post(DiseaseInforeq diseaseInforeq) {
        Request req = new Request();
        req.setDisease_inforeq(diseaseInforeq);

        String url = OrcaApiUrl.DISEASEGETV2 + "?class=01";
        Response response = request(url, req);
        return response.getDisease_infores();
    }

    /**
     * 患者登録.
     *
     * @param patientmodreq Patientmodreq
     * @param classNum      class=01(患者登録), class=02(患者情報更新), class=03(患者情報削除), class=04(保険情報追加)
     * @return Patientmodres
     */
    public Patientmodres post(Patientmodreq patientmodreq, String classNum) {
        Request req = new Request();
        req.setPatientmodreq(patientmodreq);

        String url = OrcaApiUrl.PATIENTMODV2 + "?class=" + classNum;
        Response response = request(url, req);
        return response.getPatientmodres();
    }

    /**
     * 患者予約情報.
     *
     * @param appointlst2req Appointlst2req
     * @return Appointlst2res
     */
    public Appointlst2res post(Appointlst2req appointlst2req) {
        Request req = new Request();
        req.setAppointlst2req(appointlst2req);

        String url = OrcaApiUrl.APPOINTLST2V2 + "?class=01";
        Response response = request(url, req);
        return response.getAppointlst2res();
    }

    /**
     * 請求金額返却.
     *
     * @param acsimulatereq Acsimulatereq
     * @return Acsimulateres
     */
    public Acsimulateres post(Acsimulatereq acsimulatereq) {
        Request req = new Request();
        req.setAcsimulatereq(acsimulatereq);

        String url = OrcaApiUrl.ACSIMULATEV2 + "?class=01";
        Response response = request(url, req);
        return response.getAcsimulateres();
    }

    /**
     * 症状詳記.
     *
     * @param subReq   Subjectivemodreq
     * @param classNum class=01(症状詳記登録), class=02(症状詳記削除)
     * @return Subjectivemodres
     */
    public Subjectivesmodres post(Subjectivesmodreq subReq, String classNum) {
        Request req = new Request();
        req.setSubjectivesmodreq(subReq);

        String url = OrcaApiUrl.SUBJECTIVESV2 + "?class=" + classNum;
        Response response = request(url, req);
        return response.getSubjectivesmodres();
    }

    /**
     * 受診日指定による来院患者一覧.
     * (01:来院日一覧/02:来院年月一覧)
     *
     * @param visitptlstreq Visitptlstreq
     * @return Visitptlstres contains either visitptlst01res/02res
     */
    public Visitptlstres post(Visitptlstreq visitptlstreq) {
        Request req = new Request();
        req.setVisitptlstreq(visitptlstreq);

        String url = OrcaApiUrl.VISITPTLSTV2;
        Response response = request(url, req);
        Visitptlstres visitptlstres = new Visitptlstres();
        visitptlstres.setVisitptlst01res(response.getVisitptlst01res());
        visitptlstres.setVisitptlst02res(response.getVisitptlst02res());
        return visitptlstres;
    }

    /**
     * 中途終了患者情報一覧.
     *
     * @param tmedicalgetreq Tmedicalgetreq
     * @return Tmedicalgetres
     */
    public Tmedicalgetres post(Tmedicalgetreq tmedicalgetreq) {
        Request req = new Request();
        req.setTmedicalgetreq(tmedicalgetreq);

        String url = OrcaApiUrl.TMEDICALGETV2;
        Response response = request(url, req);
        return response.getTmedicalgetres();
    }

    /**
     * 保険者一覧情報.
     *
     * @param insprogetreq Insprogetreq
     * @return Insprogetres
     */
    public Insprogetres post(Insprogetreq insprogetreq) {
        Request req = new Request();
        req.setInsprogetreq(insprogetreq);

        String url = OrcaApiUrl.INSPROGETV2;
        Response response = request(url, req);
        return response.getInsprogetres();
    }

    /**
     * 収納情報返却.
     *
     * @param incomeinfreq 中身はPrivateObjects
     * @return PrivateObjects
     */
    public PrivateObjects post(Incomeinfreq incomeinfreq) {
        Request req = new Request();
        req.setPrivate_objects(incomeinfreq);

        String url = OrcaApiUrl.INCOMEINFV2;
        Response response = request(url, req);
        return response.getPrivate_objects();
    }

    /**
     * システム情報.
     *
     * @param systeminfreq 中身はPrivateObjects
     * @return PrivateObjects
     */
    public PrivateObjects post(Systeminfreq systeminfreq) {
        Request req = new Request();
        req.setPrivate_objects(systeminfreq);

        String url = OrcaApiUrl.SYSTEMINFV2;
        Response response = request(url, req);
        return response.getPrivate_objects();
    }

    /**
     * ユーザー管理情報.
     *
     * @param manageusersreq Manageusersreq
     * @return Manageusersres
     */
    public Manageusersres post(Manageusersreq manageusersreq) {
        Request req = new Request();
        req.setManageusersreq(manageusersreq);

        String url = OrcaApiUrl.MANAGEUSERSV2;
        Response response = request(url, req);
        return response.getManageusersres();
    }

    /**
     * セット登録.
     *
     * @param medicalsetreq Medicalsetreq
     * @return Medicalsetres
     */
    public Medicalsetres post(Medicalsetreq medicalsetreq) {
        Request req = new Request();
        req.setMedicalsetreq(medicalsetreq);

        String url = OrcaApiUrl.MEDICALSETV2;
        Response response = request(url, req);
        return response.getMedicalsetres();
    }

    /**
     * 全保険組合せ一覧取得.
     *
     * @param patientlst6req Patientlst6req
     * @return Patientlst6res
     */
    public Patientlst6res post(Patientlst6req patientlst6req) {
        Request req = new Request();
        req.setPatientlst6req(patientlst6req);

        String url = OrcaApiUrl.PATIENTLST6V2;
        Response2 response = request2(url, req);
        return response.getPatientlst6res();
    }

    /**
     * 患者病名登録２.
     *
     * @param diseasereq Diseasereq
     * @return Diseaseres
     */
    public Diseaseres post(Diseasereq diseasereq) {
        Request req = new Request();
        req.setDiseasereq(diseasereq);

        String url = OrcaApiUrl.DISEASEV3;
        Response response = request(url, req);
        return response.getDiseaseres();
    }

    /**
     * マスタデータ最終更新日取得.
     *
     * @param masterreq Masterlastupdatev3req
     * @return Masterlastupdatev3res
     */
    public Masterlastupdatev3res post(Masterlastupdatev3req masterreq) {
        Request req = new Request();
        req.setMasterlastupdatev3req(masterreq);

        String url = OrcaApiUrl.MASTERLASTUPDATEV3;
        Response response = request(url, req);
        return response.getMasterlastupdatev3res();
    }

    /**
     * 基本情報取得.
     *
     * @param system01dailyreq System01dailyreq
     * @return System01dailyres
     */
    public System01dailyres post(System01dailyreq system01dailyreq) {
        Request req = new Request();
        req.setSystem01_dailyreq(system01dailyreq);

        String url = OrcaApiUrl.SYSTEM01DAILYV2;
        Response response = request(url, req);
        return response.getSystem01_dailyres();
    }

    /**
     * 患者メモ取得.
     *
     * @param patientlst7req Patientlst7req
     * @return Patientlst7res
     */
    public Patientlst7res post(Patientlst7req patientlst7req) {
        Request req = new Request();
        req.setPatientlst7req(patientlst7req);

        String url = OrcaApiUrl.PATIENTLST7V2;
        Response response = request(url, req);
        return response.getPatientlst7res();
    }

    /**
     * 初診算定日登録.
     *
     * @param medicalv2req3 Medicalv2req3
     * @return Medicalv2res3
     */
    public Medicalv2res3 post(Medicalv2req3 medicalv2req3) {
        Request req = new Request();
        req.setMedicalv2req3(medicalv2req3);

        String url = OrcaApiUrl.MEDICALMODV23;
        Response response = request(url, req);
        return response.getMedicalv2res3();
    }

    /**
     * 薬剤併用禁忌チェック.
     *
     * @param contraindicationCheckreq ContraindicationCheckreq
     * @return ContraindicationCheckres
     */
    public ContraindicationCheckres post(ContraindicationCheckreq contraindicationCheckreq) {
        Request req = new Request();
        req.setContraindication_checkreq(contraindicationCheckreq);

        String url = OrcaApiUrl.CONTRAINDICATIONCHECKV2;
        Response response = request(url, req);
        return response.getContraindication_checkres();
    }

    /**
     * 保険・公費一覧取得.
     *
     * @param insuranceinfreq Insuranceinfreq
     * @return Insuranceinfres
     */
    public Insuranceinfres post(Insuranceinfreq insuranceinfreq) {
        Request req = new Request();
        req.setInsuranceinfreq(insuranceinfreq);

        String url = OrcaApiUrl.INSURANCEINF1V2;
        Response response = request(url, req);
        return response.getInsuranceinfres();
    }

    /**
     * 症状詳記情報取得.
     *
     * @param subjectiveslstreq Subjectiveslstreq
     * @return Subjectiveslstres
     */
    public Subjectiveslstres post(Subjectiveslstreq subjectiveslstreq) {
        Request req = new Request();
        req.setSubjectiveslstreq(subjectiveslstreq);

        String url = OrcaApiUrl.SUBJECTIVESLSTV2;
        Response response = request(url, req);
        return response.getSubjectiveslstres();
    }

    /**
     * PUSH通知一括取得.
     *
     * @param pusheventgetv2req Pusheventgetv2req
     * @return open.dolphin.orca.pushapi.bean.Data
     */
    public open.dolphin.orca.pushapi.bean.Data post(Pusheventgetv2req pusheventgetv2req) {
        Request req = new Request();
        req.setPusheventgetv2req(pusheventgetv2req);

        String url = OrcaApiUrl.PUSHEVENTGETV2;
        Response response = request(url, req);
        return response.getData();
    }

    /**
     * 旧姓履歴情報取得.
     *
     * @param patientlst8req Patientlst8req
     * @return Patientlst8res
     */
    public Patientlst8res post(Patientlst8req patientlst8req) {
        Request req = new Request();
        req.setPatientlst8req(patientlst8req);

        String url = OrcaApiUrl.PATIENTLST8V2;
        Response response = request(url, req);
        return response.getPatientlst8res();
    }

    /**
     * Orca に Request を POST して Response を得る.
     *
     * @param url URL
     * @param req Request
     * @return Response
     */
    private Response request(String url, Request req) {
        URI uri = hostInfo.getOrcaApiUri(url);
        String doc = JsonConverter.toJson(req);

        String response = handler.post(uri, doc);
        return JsonConverter.fromJson(response, Response.class);
    }

    /**
     * patientlst6v2 専用 request.
     *
     * @param url URL
     * @param req Request
     * @return Response2
     */
    private Response2 request2(String url, Request req) {
        URI uri = hostInfo.getOrcaApiUri(url);
        String doc = JsonConverter.toJson(req);

        String response = handler.post(uri, doc);
        return JsonConverter.fromJson(response, Response2.class);
    }

    /**
     * Orca から GET で Response を得る.
     *
     * @param url URL
     * @return Response
     */
    private Response request(String url) {
        URI uri = hostInfo.getOrcaApiUri(url);

        String response = handler.get(uri);
        return JsonConverter.fromJson(response, Response.class);
    }
}
