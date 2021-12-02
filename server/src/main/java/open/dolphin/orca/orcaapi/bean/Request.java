package open.dolphin.orca.orcaapi.bean;

/**
 * ORCA API requests.
 *
 * @author pns
 */
public class Request {
    /**
     * API 予約. https://www.orca.med.or.jp/receipt/tec/api/appointmod.html
     */
    private Appointreq appointreq;

    /**
     * 中途終了データ作成. https://www.orca.med.or.jp/receipt/tec/api/medicalmod.html
     */
    private Medicalreq medicalreq;

    /**
     * API 受付. https://www.orca.med.or.jp/receipt/tec/api/acceptmod.html
     */
    private Acceptreq acceptreq;

    /**
     * 指定された日付の受付一覧返却. https://www.orca.med.or.jp/receipt/tec/api/acceptancelst.html
     */
    private Acceptlstreq acceptlstreq;

    /**
     * 予約一覧. https://www.orca.med.or.jp/receipt/tec/api/appointlst.html
     */
    private Appointlstreq appointlstreq;

    /**
     * 点数マスタ情報登録. https://www.orca.med.or.jp/receipt/tec/api/medicatonmod.html
     */
    private Medicationreq medicationreq;

    /**
     * 患者番号一覧の取得. https://www.orca.med.or.jp/receipt/tec/api/patientidlist.html
     */
    private Patientlst1req patientlst1req;

    /**
     * 複数の患者情報取得. https://www.orca.med.or.jp/receipt/tec/api/patientlist.html
     */
    private Patientlst2req patientlst2req;

    /**
     * 患者情報取得(氏名検索). https://www.orca.med.or.jp/receipt/tec/api/patientshimei.html
     */
    private Patientlst3req patientlst3req;

    /**
     * システム管理情報の取得. https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     */
    private System01Managereq system01_managereq;

    /**
     * 診療情報の返却. https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     */
    private Medicalgetreq medicalgetreq;

    /**
     * 患者病名情報の返却. http://www.orca.med.or.jp/receipt/tec/api/disease.html
     */
    private DiseaseInforeq disease_inforeq;
    /**
     * API 患者登録. https://www.orca.med.or.jp/receipt/tec/api/patientmod.html
     */
    private Patientmodreq patientmodreq;

    /**
     * API 患者予約情報. https://www.orca.med.or.jp/receipt/tec/api/appointlst2.html
     */
    private Appointlst2req appointlst2req;

    /**
     * API 請求金額返却. https://www.orca.med.or.jp/receipt/tec/api/acsimulate.html
     */
    private Acsimulatereq acsimulatereq;

    /**
     * API 症状詳記. https://www.orca.med.or.jp/receipt/tec/api/subjectives.html
     */
    private Subjectivesmodreq subjectivesmodreq;

    /**
     * 受診日指定による来院患者一覧. https://www.orca.med.or.jp/receipt/tec/api/visitpatient.html
     */
    private Visitptlstreq visitptlstreq;

    /**
     * 中途終了患者情報一覧. https://www.orca.med.or.jp/receipt/tec/api/medicaltemp.html
     */
    private Tmedicalgetreq tmedicalgetreq;

    /**
     * 保険者一覧情報. https://www.orca.med.or.jp/receipt/tec/api/insuranceinfo.html
     */
    private Insprogetreq insprogetreq;

    /**
     * API 収納情報返却, システム情報.
     * https://www.orca.med.or.jp/receipt/tec/api/shunou.html
     * https://www.orca.med.or.jp/receipt/tec/api/systemstate.html
     */
    private PrivateObjects private_objects;

    /**
     * ユーザー管理情報. https://www.orca.med.or.jp/receipt/tec/api/userkanri.html
     */
    private Manageusersreq manageusersreq;

    /**
     * セット登録. https://www.orca.med.or.jp/receipt/tec/api/setcode.html
     */
    private Medicalsetreq medicalsetreq;

    /**
     * 全保険組合せ一覧取得. https://www.orca.med.or.jp/receipt/tec/api/insurancecombi.html
     * patientlst2res が返ってくるが，他の patient2res と違って Patient_Information が array で返ってこない.
     */
    private Patientlst6req patientlst6req;

    /**
     * 患者病名登録２. https://www.orca.med.or.jp/receipt/tec/api/diseasemod2.html
     */
    private Diseasereq diseasereq;

    /**
     * マスタデータ最終更新日取得. http://www.orca.med.or.jp/receipt/tec/api/master_last_update.html
     */
    private Masterlastupdatev3req masterlastupdatev3req;

    /**
     * 基本情報取得. http://www.orca.med.or.jp/receipt/tec/api/system_daily.html
     */
    private System01dailyreq system01_dailyreq;

    /**
     * 患者メモ取得. http://www.orca.med.or.jp/receipt/tec/api/patient_memo_list.html
     */
    private Patientlst7req patientlst7req;

    /**
     * 初診算定日登録. http://www.orca.med.or.jp/receipt/tec/api/first_calculation_date.html
     */
    private Medicalv2req3 medicalv2req3;

    /**
     * 薬剤併用禁忌チェック. http://www.orca.med.or.jp/receipt/tec/api/contraindication_check.html
     */
    private ContraindicationCheckreq contraindication_checkreq;

    /**
     * 保険・公費一覧取得. http://www.orca.med.or.jp/receipt/tec/api/insurance_list.html
     */
    private Insuranceinfreq insuranceinfreq;

    /**
     * 症状詳記情報取得. http://www.orca.med.or.jp/receipt/tec/api/subjectiveslst.html
     */
    private Subjectiveslstreq subjectiveslstreq;

    /**
     * PUSH通知一括取得. https://www.orca.med.or.jp/receipt/tec/api/pusheventget.html
     */
    private Pusheventgetv2req pusheventgetv2req;

    /**
     * 旧姓履歴情報取得. https://www.orca.med.or.jp/receipt/tec/api/kyuseirireki.html
     */
    private Patientlst8req patientlst8req;


    /**
     * API 予約. https://www.orca.med.or.jp/receipt/tec/api/appointmod.html
     *
     * @return the appointreq
     */
    public Appointreq getAppointreq() {
        return appointreq;
    }

    /**
     * API 予約. https://www.orca.med.or.jp/receipt/tec/api/appointmod.html
     *
     * @param appointreq the appointreq to set
     */
    public void setAppointreq(Appointreq appointreq) {
        this.appointreq = appointreq;
    }

    /**
     * 中途終了データ作成. https://www.orca.med.or.jp/receipt/tec/api/medicalmod.html
     *
     * @return the medicalreq
     */
    public Medicalreq getMedicalreq() {
        return medicalreq;
    }

    /**
     * 中途終了データ作成. https://www.orca.med.or.jp/receipt/tec/api/medicalmod.html
     *
     * @param medicalreq the medicalreq to set
     */
    public void setMedicalreq(Medicalreq medicalreq) {
        this.medicalreq = medicalreq;
    }

    /**
     * API 受付. https://www.orca.med.or.jp/receipt/tec/api/acceptmod.html
     *
     * @return the acceptreq
     */
    public Acceptreq getAcceptreq() {
        return acceptreq;
    }

    /**
     * API 受付. https://www.orca.med.or.jp/receipt/tec/api/acceptmod.html
     *
     * @param acceptreq the acceptreq to set
     */
    public void setAcceptreq(Acceptreq acceptreq) {
        this.acceptreq = acceptreq;
    }

    /**
     * 指定された日付の受付一覧返却. https://www.orca.med.or.jp/receipt/tec/api/acceptancelst.html
     *
     * @return the acceptlstreq
     */
    public Acceptlstreq getAcceptlstreq() {
        return acceptlstreq;
    }

    /**
     * 指定された日付の受付一覧返却. https://www.orca.med.or.jp/receipt/tec/api/acceptancelst.html
     *
     * @param acceptlstreq the acceptlstreq to set
     */
    public void setAcceptlstreq(Acceptlstreq acceptlstreq) {
        this.acceptlstreq = acceptlstreq;
    }

    /**
     * 予約一覧. https://www.orca.med.or.jp/receipt/tec/api/appointlst.html
     *
     * @return the appointlstreq
     */
    public Appointlstreq getAppointlstreq() {
        return appointlstreq;
    }

    /**
     * 予約一覧. https://www.orca.med.or.jp/receipt/tec/api/appointlst.html
     *
     * @param appointlstreq the appointlstreq to set
     */
    public void setAppointlstreq(Appointlstreq appointlstreq) {
        this.appointlstreq = appointlstreq;
    }

    /**
     * 点数マスタ情報登録. https://www.orca.med.or.jp/receipt/tec/api/medicatonmod.html
     *
     * @return the medicationreq
     */
    public Medicationreq getMedicationreq() {
        return medicationreq;
    }

    /**
     * 点数マスタ情報登録. https://www.orca.med.or.jp/receipt/tec/api/medicatonmod.html
     *
     * @param medicationreq the medicationreq to set
     */
    public void setMedicationreq(Medicationreq medicationreq) {
        this.medicationreq = medicationreq;
    }

    /**
     * 患者番号一覧の取得. https://www.orca.med.or.jp/receipt/tec/api/patientidlist.html
     *
     * @return the patientlst1req
     */
    public Patientlst1req getPatientlst1req() {
        return patientlst1req;
    }

    /**
     * 患者番号一覧の取得. https://www.orca.med.or.jp/receipt/tec/api/patientidlist.html
     *
     * @param patientlst1req the patientlst1req to set
     */
    public void setPatientlst1req(Patientlst1req patientlst1req) {
        this.patientlst1req = patientlst1req;
    }

    /**
     * 複数の患者情報取得. https://www.orca.med.or.jp/receipt/tec/api/patientlist.html
     *
     * @return the patientlst2req
     */
    public Patientlst2req getPatientlst2req() {
        return patientlst2req;
    }

    /**
     * 複数の患者情報取得. https://www.orca.med.or.jp/receipt/tec/api/patientlist.html
     *
     * @param patientlst2req the patientlst2req to set
     */
    public void setPatientlst2req(Patientlst2req patientlst2req) {
        this.patientlst2req = patientlst2req;
    }

    /**
     * 患者情報取得(氏名検索). https://www.orca.med.or.jp/receipt/tec/api/patientshimei.html
     *
     * @return the patientlst3req
     */
    public Patientlst3req getPatientlst3req() {
        return patientlst3req;
    }

    /**
     * 患者情報取得(氏名検索). https://www.orca.med.or.jp/receipt/tec/api/patientshimei.html
     *
     * @param patientlst3req the patientlst3req to set
     */
    public void setPatientlst3req(Patientlst3req patientlst3req) {
        this.patientlst3req = patientlst3req;
    }

    /**
     * システム管理情報の取得. https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     *
     * @return the system01_managereq
     */
    public System01Managereq getSystem01_managereq() {
        return system01_managereq;
    }

    /**
     * システム管理情報の取得. https://www.orca.med.or.jp/receipt/tec/api/systemkanri.html
     *
     * @param system01_managereq the system01_managereq to set
     */
    public void setSystem01_managereq(System01Managereq system01_managereq) {
        this.system01_managereq = system01_managereq;
    }

    /**
     * 診療情報の返却. https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     *
     * @return the medicalgetreq
     */
    public Medicalgetreq getMedicalgetreq() {
        return medicalgetreq;
    }

    /**
     * 診療情報の返却. https://www.orca.med.or.jp/receipt/tec/api/medicalinfo.html
     *
     * @param medicalgetreq the medicalgetreq to set
     */
    public void setMedicalgetreq(Medicalgetreq medicalgetreq) {
        this.medicalgetreq = medicalgetreq;
    }

    /**
     * 患者病名情報の返却. http://www.orca.med.or.jp/receipt/tec/api/disease.html
     *
     * @return the disease_inforeq
     */
    public DiseaseInforeq getDisease_inforeq() {
        return disease_inforeq;
    }

    /**
     * 患者病名情報の返却. http://www.orca.med.or.jp/receipt/tec/api/disease.html
     *
     * @param disease_inforeq the disease_inforeq to set
     */
    public void setDisease_inforeq(DiseaseInforeq disease_inforeq) {
        this.disease_inforeq = disease_inforeq;
    }

    /**
     * API 患者登録. https://www.orca.med.or.jp/receipt/tec/api/patientmod.html
     *
     * @return the patientmodreq
     */
    public Patientmodreq getPatientmodreq() {
        return patientmodreq;
    }

    /**
     * API 患者登録. https://www.orca.med.or.jp/receipt/tec/api/patientmod.html
     *
     * @param patientmodreq the patientmodreq to set
     */
    public void setPatientmodreq(Patientmodreq patientmodreq) {
        this.patientmodreq = patientmodreq;
    }

    /**
     * API 患者予約情報. https://www.orca.med.or.jp/receipt/tec/api/appointlst2.html
     *
     * @return the appointlst2req
     */
    public Appointlst2req getAppointlst2req() {
        return appointlst2req;
    }

    /**
     * API 患者予約情報. https://www.orca.med.or.jp/receipt/tec/api/appointlst2.html
     *
     * @param appointlst2req the appointlst2req to set
     */
    public void setAppointlst2req(Appointlst2req appointlst2req) {
        this.appointlst2req = appointlst2req;
    }

    /**
     * API 請求金額返却. https://www.orca.med.or.jp/receipt/tec/api/acsimulate.html
     *
     * @return the acsimulatereq
     */
    public Acsimulatereq getAcsimulatereq() {
        return acsimulatereq;
    }

    /**
     * API 請求金額返却. https://www.orca.med.or.jp/receipt/tec/api/acsimulate.html
     *
     * @param acsimulatereq the acsimulatereq to set
     */
    public void setAcsimulatereq(Acsimulatereq acsimulatereq) {
        this.acsimulatereq = acsimulatereq;
    }

    /**
     * API 症状詳記. https://www.orca.med.or.jp/receipt/tec/api/subjectives.html
     *
     * @return the subjectivesmodreq
     */
    public Subjectivesmodreq getSubjectivesmodreq() {
        return subjectivesmodreq;
    }

    /**
     * API 症状詳記. https://www.orca.med.or.jp/receipt/tec/api/subjectives.html
     *
     * @param subjectivesmodreq the subjectivesmodreq to set
     */
    public void setSubjectivesmodreq(Subjectivesmodreq subjectivesmodreq) {
        this.subjectivesmodreq = subjectivesmodreq;
    }

    /**
     * 受診日指定による来院患者一覧. https://www.orca.med.or.jp/receipt/tec/api/visitpatient.html
     *
     * @return the visitptlstreq
     */
    public Visitptlstreq getVisitptlstreq() {
        return visitptlstreq;
    }

    /**
     * 受診日指定による来院患者一覧. https://www.orca.med.or.jp/receipt/tec/api/visitpatient.html
     *
     * @param visitptlstreq the visitptlstreq to set
     */
    public void setVisitptlstreq(Visitptlstreq visitptlstreq) {
        this.visitptlstreq = visitptlstreq;
    }

    /**
     * 中途終了患者情報一覧. https://www.orca.med.or.jp/receipt/tec/api/medicaltemp.html
     *
     * @return the tmedicalgetreq
     */
    public Tmedicalgetreq getTmedicalgetreq() {
        return tmedicalgetreq;
    }

    /**
     * 中途終了患者情報一覧. https://www.orca.med.or.jp/receipt/tec/api/medicaltemp.html
     *
     * @param tmedicalgetreq the tmedicalgetreq to set
     */
    public void setTmedicalgetreq(Tmedicalgetreq tmedicalgetreq) {
        this.tmedicalgetreq = tmedicalgetreq;
    }

    /**
     * 保険者一覧情報. https://www.orca.med.or.jp/receipt/tec/api/insuranceinfo.html
     *
     * @return the insprogetreq
     */
    public Insprogetreq getInsprogetreq() {
        return insprogetreq;
    }

    /**
     * 保険者一覧情報. https://www.orca.med.or.jp/receipt/tec/api/insuranceinfo.html
     *
     * @param insprogetreq the insprogetreq to set
     */
    public void setInsprogetreq(Insprogetreq insprogetreq) {
        this.insprogetreq = insprogetreq;
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
     * @return the manageusersreq
     */
    public Manageusersreq getManageusersreq() {
        return manageusersreq;
    }

    /**
     * ユーザー管理情報. https://www.orca.med.or.jp/receipt/tec/api/userkanri.html
     *
     * @param manageusersreq the manageusersreq to set
     */
    public void setManageusersreq(Manageusersreq manageusersreq) {
        this.manageusersreq = manageusersreq;
    }

    /**
     * セット登録. https://www.orca.med.or.jp/receipt/tec/api/setcode.html
     *
     * @return the medicalsetreq
     */
    public Medicalsetreq getMedicalsetreq() {
        return medicalsetreq;
    }

    /**
     * セット登録. https://www.orca.med.or.jp/receipt/tec/api/setcode.html
     *
     * @param medicalsetreq the medicalsetreq to set
     */
    public void setMedicalsetreq(Medicalsetreq medicalsetreq) {
        this.medicalsetreq = medicalsetreq;
    }

    /**
     * 全保険組合せ一覧取得. https://www.orca.med.or.jp/receipt/tec/api/insurancecombi.html
     *
     * @return the patientlst6req
     */
    public Patientlst6req getPatientlst6req() {
        return patientlst6req;
    }

    /**
     * 全保険組合せ一覧取得. https://www.orca.med.or.jp/receipt/tec/api/insurancecombi.html
     *
     * @param patientlst6req the patientlst6req to set
     */
    public void setPatientlst6req(Patientlst6req patientlst6req) {
        this.patientlst6req = patientlst6req;
    }

    /**
     * 患者病名登録２. https://www.orca.med.or.jp/receipt/tec/api/diseasemod2.html
     *
     * @return the diseasereq
     */
    public Diseasereq getDiseasereq() {
        return diseasereq;
    }

    /**
     * 患者病名登録２. https://www.orca.med.or.jp/receipt/tec/api/diseasemod2.html
     *
     * @param diseasereq the diseasereq to set
     */
    public void setDiseasereq(Diseasereq diseasereq) {
        this.diseasereq = diseasereq;
    }

    /**
     * マスタデータ最終更新日取得. http://www.orca.med.or.jp/receipt/tec/api/master_last_update.html
     *
     * @return masterlastupdatev3eq
     */
    public Masterlastupdatev3req getMasterlastupdatev3eq() {
        return masterlastupdatev3req;
    }

    /**
     * マスタデータ最終更新日取得. http://www.orca.med.or.jp/receipt/tec/api/master_last_update.html
     *
     * @return masterlastupdatev3req
     */
    public Masterlastupdatev3req getMasterlastupdatev3req() {
        return masterlastupdatev3req;
    }

    /**
     * マスタデータ最終更新日取得. http://www.orca.med.or.jp/receipt/tec/api/master_last_update.html
     *
     * @param masterlastupdatev3req to set
     */
    public void setMasterlastupdatev3req(Masterlastupdatev3req masterlastupdatev3req) {
        this.masterlastupdatev3req = masterlastupdatev3req;
    }

    /**
     * 基本情報取得. http://www.orca.med.or.jp/receipt/tec/api/system_daily.html
     *
     * @return system01_dailyreq
     */
    public System01dailyreq getSystem01_dailyreq() {
        return system01_dailyreq;
    }

    /**
     * 基本情報取得. http://www.orca.med.or.jp/receipt/tec/api/system_daily.html
     *
     * @param system01_dailyreq to set
     */
    public void setSystem01_dailyreq(System01dailyreq system01_dailyreq) {
        this.system01_dailyreq = system01_dailyreq;
    }

    /**
     * 患者メモ取得. http://www.orca.med.or.jp/receipt/tec/api/patient_memo_list.html
     *
     * @return patientlst7req
     */
    public Patientlst7req getPatientlst7req() {
        return patientlst7req;
    }

    /**
     * 患者メモ取得. http://www.orca.med.or.jp/receipt/tec/api/patient_memo_list.html
     *
     * @param patientlst7req to set
     */
    public void setPatientlst7req(Patientlst7req patientlst7req) {
        this.patientlst7req = patientlst7req;
    }

    /**
     * 初診算定日登録. http://www.orca.med.or.jp/receipt/tec/api/first_calculation_date.html
     *
     * @return medicalv2req3
     */
    public Medicalv2req3 getMedicalv2req3() {
        return medicalv2req3;
    }

    /**
     * 初診算定日登録. http://www.orca.med.or.jp/receipt/tec/api/first_calculation_date.html
     *
     * @param medicalv2req3 to set
     */
    public void setMedicalv2req3(Medicalv2req3 medicalv2req3) {
        this.medicalv2req3 = medicalv2req3;
    }

    /**
     * 薬剤併用禁忌チェック. http://www.orca.med.or.jp/receipt/tec/api/contraindication_check.html
     *
     * @return contraindication_checkreq
     */
    public ContraindicationCheckreq getContraindication_checkreq() {
        return contraindication_checkreq;
    }

    /**
     * 薬剤併用禁忌チェック. http://www.orca.med.or.jp/receipt/tec/api/contraindication_check.html
     *
     * @param contraindication_checkreq to set
     */
    public void setContraindication_checkreq(ContraindicationCheckreq contraindication_checkreq) {
        this.contraindication_checkreq = contraindication_checkreq;
    }

    /**
     * 保険・公費一覧取得. http://www.orca.med.or.jp/receipt/tec/api/insurance_list.html
     *
     * @return insuranceinfreq
     */
    public Insuranceinfreq getInsuranceinfreq() {
        return insuranceinfreq;
    }

    /**
     * 保険・公費一覧取得. http://www.orca.med.or.jp/receipt/tec/api/insurance_list.html
     *
     * @param insuranceinfreq to set
     */
    public void setInsuranceinfreq(Insuranceinfreq insuranceinfreq) {
        this.insuranceinfreq = insuranceinfreq;
    }

    /**
     * 症状詳記情報取得. http://www.orca.med.or.jp/receipt/tec/api/subjectiveslst.html
     *
     * @return subjectiveslstreq
     */
    public Subjectiveslstreq getSubjectiveslstreq() {
        return subjectiveslstreq;
    }

    /**
     * 症状詳記情報取得. http://www.orca.med.or.jp/receipt/tec/api/subjectiveslst.html
     *
     * @param subjectiveslstreq to set
     */
    public void setSubjectiveslstreq(Subjectiveslstreq subjectiveslstreq) {
        this.subjectiveslstreq = subjectiveslstreq;
    }

    /**
     * PUSH通知一括取得. https://www.orca.med.or.jp/receipt/tec/api/pusheventget.html
     *
     * @return pusheventgetv2req
     */
    public Pusheventgetv2req getPusheventgetv2req() {
        return pusheventgetv2req;
    }

    /**
     * PUSH通知一括取得. https://www.orca.med.or.jp/receipt/tec/api/pusheventget.html
     *
     * @param pusheventgetv2req to set
     */
    public void setPusheventgetv2req(Pusheventgetv2req pusheventgetv2req) {
        this.pusheventgetv2req = pusheventgetv2req;
    }

    /**
     * 旧姓履歴情報取得. https://www.orca.med.or.jp/receipt/tec/api/kyuseirireki.html
     *
     * @return patientlst8req
     */
    public Patientlst8req getPatientlst8req() {
        return patientlst8req;
    }

    /**
     * 旧姓履歴情報取得. https://www.orca.med.or.jp/receipt/tec/api/kyuseirireki.html
     *
     * @param patientlst8req to set
     */
    public void setPatientlst8req(Patientlst8req patientlst8req) {
        this.patientlst8req = patientlst8req;
    }
}
