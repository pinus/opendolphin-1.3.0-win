package open.dolphin.orca.orcaapi.bean;

/**
 * ORCA API responses.
 *
 * @author pns
 */
public class Response {
    /**
     * 患者基本情報の取得. https://www.orca.med.or.jp/receipt/tec/api/patientget.html
     */
    private PatientInfores patientinfores;

    /**
     * API 予約. https://www.orca.med.or.jp/receipt/tec/api/appointmod.html
     */
    private Appointres appointres;

    /**
     * 中途終了データ作成. https://www.orca.med.or.jp/receipt/tec/api/medicalmod.html
     */
    private Medicalres medicalres;

    /**
     * API 受付. https://www.orca.med.or.jp/receipt/tec/api/acceptmod.html
     */
    private Acceptres acceptres;

    /**
     * 指定された日付の受付一覧返却. https://www.orca.med.or.jp/receipt/tec/api/acceptancelst.html
     */
    private Acceptlstres acceptlstres;

    /**
     * 予約一覧. https://www.orca.med.or.jp/receipt/tec/api/appointlst.html
     */
    private Appointlstres appointlstres;

    /**
     * 点数マスタ情報登録. https://www.orca.med.or.jp/receipt/tec/api/medicatonmod.html
     */
    private Medicationres medicationres;

    /**
     * 患者番号一覧の取得. https://www.orca.med.or.jp/receipt/tec/api/patientidlist.html
     */
    private Patientlst1res patientlst1res;

    /**
     * 複数の患者情報取得, 患者情報取得(氏名検索) 共通.
     * https://www.orca.med.or.jp/receipt/tec/api/patientlist.html
     * https://www.orca.med.or.jp/receipt/tec/api/patientshimei.html
     */
    private Patientlst2res patientlst2res;

    /**
     * システム管理情報の取得. 診療科コード一覧.
     * https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     */
    private Departmentres departmentres;

    /**
     * システム管理情報の取得. ドクター・職員コード一覧.
     * https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     */
    private Physicianres physicianres;

    /**
     * システム管理情報の取得. 医療機関基本情報.
     * https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     */
    private System1001res system1001res;

    /**
     * 診療情報の返却. 受診履歴一覧取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     */
    private Medicalget01res medicalget01res;

    /**
     * 診療情報の返却. 診療行為剤内容詳細取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     */

    private Medicalget02res medicalget02res;
    /**
     * 診療情報の返却. 診療月診療コード情報取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     */

    private Medicalget03res medicalget03res;
    /**
     * 診療情報の返却. 診療区分別剤点数取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     */
    private Medicalget04res medicalget04res;

    /**
     * 患者病名情報の返却. http://www.orca.med.or.jp/receipt/tec/api/disease.html
     */
    private DiseaseInfores disease_infores;

    /**
     * API 患者登録. https://www.orca.med.or.jp/receipt/tec/api/patientmod.html
     */
    private Patientmodres patientmodres;

    /**
     * API 患者予約情報. https://www.orca.med.or.jp/receipt/tec/api/appointlst2.html
     */
    private Appointlst2res appointlst2res;

    /**
     * API 請求金額返却. https://www.orca.med.or.jp/receipt/tec/api/acsimulate.html
     */
    private Acsimulateres acsimulateres;

    /**
     * API 症状詳記. https://www.orca.med.or.jp/receipt/tec/api/subjectives.html
     */
    private Subjectivesmodres subjectivesmodres;

    /**
     * 受診日指定による来院患者一覧. 来院日一覧
     * https://www.orca.med.or.jp/receipt/tec/api/visitpatient.html
     */
    private Visitptlst01res visitptlst01res;

    /**
     * 受診日指定による来院患者一覧. 来院年月一覧
     * https://www.orca.med.or.jp/receipt/tec/api/visitpatient.html
     */
    private Visitptlst02res visitptlst02res;

    /**
     * 中途終了患者情報一覧. https://www.orca.med.or.jp/receipt/tec/api/medicaltemp.html
     */
    private Tmedicalgetres tmedicalgetres;

    /**
     * 保険者一覧情報. https://www.orca.med.or.jp/receipt/tec/api/insuranceinfo.html
     */
    private Insprogetres insprogetres;

    /**
     * API 収納情報返却, システム情報.
     * https://www.orca.med.or.jp/receipt/tec/api/shunou.html
     * https://www.orca.med.or.jp/receipt/tec/api/systemstate.html
     */
    private PrivateObjects private_objects;

    /**
     * ユーザー管理情報. https://www.orca.med.or.jp/receipt/tec/api/userkanri.html
     */
    private Manageusersres manageusersres;

    /**
     * セット登録. https://www.orca.med.or.jp/receipt/tec/api/setcode.html
     */
    private Medicalsetres medicalsetres;

    /**
     * 患者病名登録２. https://www.orca.med.or.jp/receipt/tec/api/diseasemod2.html
     */
    private Diseaseres diseaseres;

    /**
     * マスタデータ最終更新日取得. http://www.orca.med.or.jp/receipt/tec/api/master_last_update.html
     */
    private Masterlastupdatev3res masterlastupdatev3res;

    /**
     * 基本情報取得. http://www.orca.med.or.jp/receipt/tec/api/system_daily.html
     */
    private System01dailyres system01_dailyres;

    /**
     * 患者メモ取得. http://www.orca.med.or.jp/receipt/tec/api/patient_memo_list.html
     */
    private Patientlst7res patientlst7res;

    /**
     * 初診算定日登録. http://www.orca.med.or.jp/receipt/tec/api/first_calculation_date.html
     */
    private Medicalv2res3 medicalv2res3;

    /**
     * 保険・公費一覧取得. http://www.orca.med.or.jp/receipt/tec/api/insurance_list.html
     */
    private Insuranceinfres insuranceinfres;

    /**
     * 薬剤併用禁忌チェック. http://www.orca.med.or.jp/receipt/tec/api/contraindication_check.html
     */
    private ContraindicationCheckres contraindication_checkres;

    /**
     * 症状詳記情報取得. http://www.orca.med.or.jp/receipt/tec/api/subjectiveslst.html
     */
    private Subjectiveslstres subjectiveslstres;

    /**
     * PUSH通知一括取得. https://www.orca.med.or.jp/receipt/tec/api/pusheventget.html
     */
    private open.dolphin.orca.pushapi.bean.Data data;

    /**
     * 旧姓履歴情報取得. https://www.orca.med.or.jp/receipt/tec/api/kyuseirireki.html
     */
    private Patientlst8res patientlst8res;


    /**
     * 患者基本情報の取得. https://www.orca.med.or.jp/receipt/tec/api/patientget.html
     *
     * @return the patientinfores
     */
    public PatientInfores getPatientinfores() {
        return patientinfores;
    }

    /**
     * 患者基本情報の取得. https://www.orca.med.or.jp/receipt/tec/api/patientget.html
     *
     * @param patientinfores the patientinfores to set
     */
    public void setPatientinfores(PatientInfores patientinfores) {
        this.patientinfores = patientinfores;
    }

    /**
     * API 予約. https://www.orca.med.or.jp/receipt/tec/api/appointmod.html
     *
     * @return the appointres
     */
    public Appointres getAppointres() {
        return appointres;
    }

    /**
     * API 予約. https://www.orca.med.or.jp/receipt/tec/api/appointmod.html
     *
     * @param appointres the appointres to set
     */
    public void setAppointres(Appointres appointres) {
        this.appointres = appointres;
    }

    /**
     * 中途終了データ作成. https://www.orca.med.or.jp/receipt/tec/api/medicalmod.html
     *
     * @return the medicalres
     */
    public Medicalres getMedicalres() {
        return medicalres;
    }

    /**
     * 中途終了データ作成. https://www.orca.med.or.jp/receipt/tec/api/medicalmod.html
     *
     * @param medicalres the medicalres to set
     */
    public void setMedicalres(Medicalres medicalres) {
        this.medicalres = medicalres;
    }

    /**
     * API 受付. https://www.orca.med.or.jp/receipt/tec/api/acceptmod.html
     *
     * @return the acceptres
     */
    public Acceptres getAcceptres() {
        return acceptres;
    }

    /**
     * API 受付. https://www.orca.med.or.jp/receipt/tec/api/acceptmod.html
     *
     * @param acceptres the acceptres to set
     */
    public void setAcceptres(Acceptres acceptres) {
        this.acceptres = acceptres;
    }

    /**
     * 指定された日付の受付一覧返却. https://www.orca.med.or.jp/receipt/tec/api/acceptancelst.html
     *
     * @return the acceptlstres
     */
    public Acceptlstres getAcceptlstres() {
        return acceptlstres;
    }

    /**
     * 指定された日付の受付一覧返却. https://www.orca.med.or.jp/receipt/tec/api/acceptancelst.html
     *
     * @param acceptlstres the acceptlstres to set
     */
    public void setAcceptlstres(Acceptlstres acceptlstres) {
        this.acceptlstres = acceptlstres;
    }

    /**
     * 予約一覧. https://www.orca.med.or.jp/receipt/tec/api/appointlst.html
     *
     * @return the appointlstres
     */
    public Appointlstres getAppointlstres() {
        return appointlstres;
    }

    /**
     * 予約一覧. https://www.orca.med.or.jp/receipt/tec/api/appointlst.html
     *
     * @param appointlstres the appointlstres to set
     */
    public void setAppointlstres(Appointlstres appointlstres) {
        this.appointlstres = appointlstres;
    }

    /**
     * 点数マスタ情報登録. https://www.orca.med.or.jp/receipt/tec/api/medicatonmod.html
     *
     * @return the medicationres
     */
    public Medicationres getMedicationres() {
        return medicationres;
    }

    /**
     * 点数マスタ情報登録. https://www.orca.med.or.jp/receipt/tec/api/medicatonmod.html
     *
     * @param medicationres the medicationres to set
     */
    public void setMedicationres(Medicationres medicationres) {
        this.medicationres = medicationres;
    }

    /**
     * 患者番号一覧の取得. https://www.orca.med.or.jp/receipt/tec/api/patientidlist.html
     *
     * @return the patientlst1res
     */
    public Patientlst1res getPatientlst1res() {
        return patientlst1res;
    }

    /**
     * 患者番号一覧の取得. https://www.orca.med.or.jp/receipt/tec/api/patientidlist.html
     *
     * @param patientlst1res the patientlst1res to set
     */
    public void setPatientlst1res(Patientlst1res patientlst1res) {
        this.patientlst1res = patientlst1res;
    }

    /**
     * 複数の患者情報取得, 患者情報取得(氏名検索) 共通.
     * https://www.orca.med.or.jp/receipt/tec/api/patientlist.html
     * https://www.orca.med.or.jp/receipt/tec/api/patientshimei.html
     *
     * @return the patientlst2res
     */
    public Patientlst2res getPatientlst2res() {
        return patientlst2res;
    }

    /**
     * 複数の患者情報取得, 患者情報取得(氏名検索) 共通.
     * https://www.orca.med.or.jp/receipt/tec/api/patientlist.html
     * https://www.orca.med.or.jp/receipt/tec/api/patientshimei.html
     *
     * @param patientlst2res the patientlst2res to set
     */
    public void setPatientlst2res(Patientlst2res patientlst2res) {
        this.patientlst2res = patientlst2res;
    }

    /**
     * システム管理情報の取得. 診療科コード一覧.
     * https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     *
     * @return the departmentres
     */
    public Departmentres getDepartmentres() {
        return departmentres;
    }

    /**
     * システム管理情報の取得. 診療科コード一覧.
     * https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     *
     * @param departmentres the departmentres to set
     */
    public void setDepartmentres(Departmentres departmentres) {
        this.departmentres = departmentres;
    }

    /**
     * システム管理情報の取得. ドクター・職員コード一覧.
     * https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     *
     * @return the physicianres
     */
    public Physicianres getPhysicianres() {
        return physicianres;
    }

    /**
     * システム管理情報の取得. ドクター・職員コード一覧.
     * https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     *
     * @param physicianres the physicianres to set
     */
    public void setPhysicianres(Physicianres physicianres) {
        this.physicianres = physicianres;
    }

    /**
     * システム管理情報の取得. 医療機関基本情報.
     * https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     *
     * @return the system1001res
     */
    public System1001res getSystem1001res() {
        return system1001res;
    }

    /**
     * システム管理情報の取得. 医療機関基本情報.
     * https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     *
     * @param system1001res the system1001res to set
     */
    public void setSystem1001res(System1001res system1001res) {
        this.system1001res = system1001res;
    }

    /**
     * 診療情報の返却. 受診履歴一覧取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     *
     * @return the medicalget01res
     */
    public Medicalget01res getMedicalget01res() {
        return medicalget01res;
    }

    /**
     * 診療情報の返却. 受診履歴一覧取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     *
     * @param medicalget01res the medicalget01res to set
     */
    public void setMedicalget01res(Medicalget01res medicalget01res) {
        this.medicalget01res = medicalget01res;
    }

    /**
     * 診療情報の返却. 診療行為剤内容詳細取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     *
     * @return the medicalget02res
     */
    public Medicalget02res getMedicalget02res() {
        return medicalget02res;
    }

    /**
     * 診療情報の返却. 診療行為剤内容詳細取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     *
     * @param medicalget02res the medicalget02res to set
     */
    public void setMedicalget02res(Medicalget02res medicalget02res) {
        this.medicalget02res = medicalget02res;
    }

    /**
     * 診療情報の返却. 診療月診療コード情報取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     *
     * @return the medicalget03res
     */
    public Medicalget03res getMedicalget03res() {
        return medicalget03res;
    }

    /**
     * 診療情報の返却. 診療月診療コード情報取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     *
     * @param medicalget03res the medicalget03res to set
     */
    public void setMedicalget03res(Medicalget03res medicalget03res) {
        this.medicalget03res = medicalget03res;
    }

    /**
     * 診療情報の返却. 診療区分別剤点数取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     *
     * @return the medicalget04res
     */
    public Medicalget04res getMedicalget04res() {
        return medicalget04res;
    }

    /**
     * 診療情報の返却. 診療区分別剤点数取得
     * https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     *
     * @param medicalget04res the medicalget04res to set
     */
    public void setMedicalget04res(Medicalget04res medicalget04res) {
        this.medicalget04res = medicalget04res;
    }

    /**
     * 患者病名情報の返却. http://www.orca.med.or.jp/receipt/tec/api/disease.html
     *
     * @return the disease_infores
     */
    public DiseaseInfores getDisease_infores() {
        return disease_infores;
    }

    /**
     * 患者病名情報の返却. http://www.orca.med.or.jp/receipt/tec/api/disease.html
     *
     * @param disease_infores the disease_infores to set
     */
    public void setDisease_infores(DiseaseInfores disease_infores) {
        this.disease_infores = disease_infores;
    }

    /**
     * API 患者登録. https://www.orca.med.or.jp/receipt/tec/api/patientmod.html
     *
     * @return the patientmodres
     */
    public Patientmodres getPatientmodres() {
        return patientmodres;
    }

    /**
     * API 患者登録. https://www.orca.med.or.jp/receipt/tec/api/patientmod.html
     *
     * @param patientmodres the patientmodres to set
     */
    public void setPatientmodres(Patientmodres patientmodres) {
        this.patientmodres = patientmodres;
    }

    /**
     * API 患者予約情報. https://www.orca.med.or.jp/receipt/tec/api/appointlst2.html
     *
     * @return the appointlst2res
     */
    public Appointlst2res getAppointlst2res() {
        return appointlst2res;
    }

    /**
     * API 患者予約情報. https://www.orca.med.or.jp/receipt/tec/api/appointlst2.html
     *
     * @param appointlst2res the appointlst2res to set
     */
    public void setAppointlst2res(Appointlst2res appointlst2res) {
        this.appointlst2res = appointlst2res;
    }

    /**
     * API 請求金額返却. https://www.orca.med.or.jp/receipt/tec/api/acsimulate.html
     *
     * @return the acsimulateres
     */
    public Acsimulateres getAcsimulateres() {
        return acsimulateres;
    }

    /**
     * API 請求金額返却. https://www.orca.med.or.jp/receipt/tec/api/acsimulate.html
     *
     * @param acsimulateres the acsimulateres to set
     */
    public void setAcsimulateres(Acsimulateres acsimulateres) {
        this.acsimulateres = acsimulateres;
    }

    /**
     * API 症状詳記. https://www.orca.med.or.jp/receipt/tec/api/subjectives.html
     *
     * @return the subjectivesmodres
     */
    public Subjectivesmodres getSubjectivesmodres() {
        return subjectivesmodres;
    }

    /**
     * API 症状詳記. https://www.orca.med.or.jp/receipt/tec/api/subjectives.html
     *
     * @param subjectivesmodres the subjectivesmodres to set
     */
    public void setSubjectivesmodres(Subjectivesmodres subjectivesmodres) {
        this.subjectivesmodres = subjectivesmodres;
    }

    /**
     * 受診日指定による来院患者一覧. 来院日一覧
     * https://www.orca.med.or.jp/receipt/tec/api/visitpatient.html
     *
     * @return the visitptlst01res
     */
    public Visitptlst01res getVisitptlst01res() {
        return visitptlst01res;
    }

    /**
     * 受診日指定による来院患者一覧. 来院日一覧
     * https://www.orca.med.or.jp/receipt/tec/api/visitpatient.html
     *
     * @param visitptlst01res the visitptlst01res to set
     */
    public void setVisitptlst01res(Visitptlst01res visitptlst01res) {
        this.visitptlst01res = visitptlst01res;
    }

    /**
     * 受診日指定による来院患者一覧. 来院年月一覧
     * https://www.orca.med.or.jp/receipt/tec/api/visitpatient.html
     *
     * @return the visitptlst02res
     */
    public Visitptlst02res getVisitptlst02res() {
        return visitptlst02res;
    }

    /**
     * 受診日指定による来院患者一覧. 来院年月一覧
     * https://www.orca.med.or.jp/receipt/tec/api/visitpatient.html
     *
     * @param visitptlst02res the visitptlst02res to set
     */
    public void setVisitptlst02res(Visitptlst02res visitptlst02res) {
        this.visitptlst02res = visitptlst02res;
    }

    /**
     * 中途終了患者情報一覧. https://www.orca.med.or.jp/receipt/tec/api/medicaltemp.html
     *
     * @return the tmedicalgetres
     */
    public Tmedicalgetres getTmedicalgetres() {
        return tmedicalgetres;
    }

    /**
     * 中途終了患者情報一覧. https://www.orca.med.or.jp/receipt/tec/api/medicaltemp.html
     *
     * @param tmedicalgetres the tmedicalgetres to set
     */
    public void setTmedicalgetres(Tmedicalgetres tmedicalgetres) {
        this.tmedicalgetres = tmedicalgetres;
    }

    /**
     * 保険者一覧情報. https://www.orca.med.or.jp/receipt/tec/api/insuranceinfo.html
     *
     * @return the insprogetres
     */
    public Insprogetres getInsprogetres() {
        return insprogetres;
    }

    /**
     * 保険者一覧情報. https://www.orca.med.or.jp/receipt/tec/api/insuranceinfo.html
     *
     * @param insprogetres the insprogetres to set
     */
    public void setInsprogetres(Insprogetres insprogetres) {
        this.insprogetres = insprogetres;
    }

    /**
     * API 収納情報返却, システム情報.
     * https://www.orca.med.or.jp/receipt/tec/api/shunou.html
     * https://www.orca.med.or.jp/receipt/tec/api/systemstate.html
     *
     * @return the private_objects
     */
    public PrivateObjects getPrivate_objects() {
        return private_objects;
    }

    /**
     * API 収納情報返却, システム情報.
     * https://www.orca.med.or.jp/receipt/tec/api/shunou.html
     * https://www.orca.med.or.jp/receipt/tec/api/systemstate.html
     *
     * @param private_objects the private_objects to set
     */
    public void setPrivate_objects(PrivateObjects private_objects) {
        this.private_objects = private_objects;
    }

    /**
     * ユーザー管理情報. https://www.orca.med.or.jp/receipt/tec/api/userkanri.html
     *
     * @return the manageusersres
     */
    public Manageusersres getManageusersres() {
        return manageusersres;
    }

    /**
     * ユーザー管理情報. https://www.orca.med.or.jp/receipt/tec/api/userkanri.html
     *
     * @param manageusersres the manageusersres to set
     */
    public void setManageusersres(Manageusersres manageusersres) {
        this.manageusersres = manageusersres;
    }

    /**
     * セット登録. https://www.orca.med.or.jp/receipt/tec/api/setcode.html
     *
     * @return the medicalsetres
     */
    public Medicalsetres getMedicalsetres() {
        return medicalsetres;
    }

    /**
     * セット登録. https://www.orca.med.or.jp/receipt/tec/api/setcode.html
     *
     * @param medicalsetres the medicalsetres to set
     */
    public void setMedicalsetres(Medicalsetres medicalsetres) {
        this.medicalsetres = medicalsetres;
    }

    /**
     * 患者病名登録２. https://www.orca.med.or.jp/receipt/tec/api/diseasemod2.html
     *
     * @return the diseaseres
     */
    public Diseaseres getDiseaseres() {
        return diseaseres;
    }

    /**
     * 患者病名登録２. https://www.orca.med.or.jp/receipt/tec/api/diseasemod2.html
     *
     * @param diseaseres the diseaseres to set
     */
    public void setDiseaseres(Diseaseres diseaseres) {
        this.diseaseres = diseaseres;
    }

    /**
     * マスタデータ最終更新日取得. http://www.orca.med.or.jp/receipt/tec/api/master_last_update.html
     *
     * @return masterlastupdatev3res
     */
    public Masterlastupdatev3res getMasterlastupdatev3res() {
        return masterlastupdatev3res;
    }

    /**
     * マスタデータ最終更新日取得. http://www.orca.med.or.jp/receipt/tec/api/master_last_update.html
     *
     * @param masterlastupdatev3res to set
     */
    public void setMasterlastupdatev3res(Masterlastupdatev3res masterlastupdatev3res) {
        this.masterlastupdatev3res = masterlastupdatev3res;
    }

    /**
     * 基本情報取得. http://www.orca.med.or.jp/receipt/tec/api/system_daily.html
     *
     * @return system01_dailyres
     */
    public System01dailyres getSystem01_dailyres() {
        return system01_dailyres;
    }

    /**
     * 基本情報取得. http://www.orca.med.or.jp/receipt/tec/api/system_daily.html
     *
     * @param system01_dailyres to set
     */
    public void setSystem01_dailyres(System01dailyres system01_dailyres) {
        this.system01_dailyres = system01_dailyres;
    }

    /**
     * 患者メモ取得. http://www.orca.med.or.jp/receipt/tec/api/patient_memo_list.html
     *
     * @return patientlst7res
     */
    public Patientlst7res getPatientlst7res() {
        return patientlst7res;
    }

    /**
     * 患者メモ取得. http://www.orca.med.or.jp/receipt/tec/api/patient_memo_list.html
     *
     * @param patientlst7res to set
     */
    public void setPatientlst7res(Patientlst7res patientlst7res) {
        this.patientlst7res = patientlst7res;
    }

    /**
     * 初診算定日登録. http://www.orca.med.or.jp/receipt/tec/api/first_calculation_date.html
     *
     * @return medicalv2res3
     */
    public Medicalv2res3 getMedicalv2res3() {
        return medicalv2res3;
    }

    /**
     * 初診算定日登録. http://www.orca.med.or.jp/receipt/tec/api/first_calculation_date.html
     *
     * @param medicalv2res3 to set
     */
    public void setMedicalv2res3(Medicalv2res3 medicalv2res3) {
        this.medicalv2res3 = medicalv2res3;
    }

    /**
     * 薬剤併用禁忌チェック. http://www.orca.med.or.jp/receipt/tec/api/contraindication_check.html
     *
     * @return contraindication_checkres
     */
    public ContraindicationCheckres getContraindication_checkres() {
        return contraindication_checkres;
    }

    /**
     * 薬剤併用禁忌チェック. http://www.orca.med.or.jp/receipt/tec/api/contraindication_check.html
     *
     * @param contraindication_checkres to set
     */
    public void setContraindication_checkres(ContraindicationCheckres contraindication_checkres) {
        this.contraindication_checkres = contraindication_checkres;
    }

    /**
     * 保険・公費一覧取得. http://www.orca.med.or.jp/receipt/tec/api/insurance_list.html
     *
     * @return insuranceinfres
     */
    public Insuranceinfres getInsuranceinfres() {
        return insuranceinfres;
    }

    /**
     * 保険・公費一覧取得. http://www.orca.med.or.jp/receipt/tec/api/insurance_list.html
     *
     * @param insuranceinfres to set
     */
    public void setInsuranceinfres(Insuranceinfres insuranceinfres) {
        this.insuranceinfres = insuranceinfres;
    }

    /**
     * 症状詳記情報取得. http://www.orca.med.or.jp/receipt/tec/api/subjectiveslst.html
     *
     * @return subjectiveslstres
     */
    public Subjectiveslstres getSubjectiveslstres() {
        return subjectiveslstres;
    }

    /**
     * 症状詳記情報取得. http://www.orca.med.or.jp/receipt/tec/api/subjectiveslst.html
     *
     * @param subjectiveslstres to set
     */
    public void setSubjectiveslstres(Subjectiveslstres subjectiveslstres) {
        this.subjectiveslstres = subjectiveslstres;
    }

    /**
     * PUSH通知一括取得. https://www.orca.med.or.jp/receipt/tec/api/pusheventget.html
     *
     * @return data
     */
    public open.dolphin.orca.pushapi.bean.Data getData() {
        return data;
    }

    /**
     * PUSH通知一括取得. https://www.orca.med.or.jp/receipt/tec/api/pusheventget.html
     *
     * @param data to set
     */
    public void setData(open.dolphin.orca.pushapi.bean.Data data) {
        this.data = data;
    }

    private Patientlst8req patientlst8req;
    /**
     * 旧姓履歴情報取得. https://www.orca.med.or.jp/receipt/tec/api/kyuseirireki.html
     *
     * @return patientlst8res
     */
    public Patientlst8res getPatientlst8res() {
        return patientlst8res;
    }

    /**
     * 旧姓履歴情報取得. https://www.orca.med.or.jp/receipt/tec/api/kyuseirireki.html
     *
     * @param patientlst8res to set
     */
    public void setPatientlst8res(Patientlst8res patientlst8res) {
        this.patientlst8res = patientlst8res;
    }
}
