package open.dolphin.orca.orcaapi.bean;

/**
 * HealthInsurance_Information. 保険組合せ情報
 *
 * @author pns
 */
public class HealthInsuranceInformation {
    /**
     * 保険組合せ番号 (例: 0001)
     */
    private String Insurance_Combination_Number;

    /**
     * 保険組合せ番号 (例: 0002). acsimulatev2 のみこの名前
     */
    private String Combination_Number;

    /**
     * 保険の種類 (例: 060)
     */
    private String InsuranceProvider_Class;

    /**
     * 保険者番号 (例: 138057)
     */
    private String InsuranceProvider_Number;

    /**
     * 保険の制度名称 (例: 国保)
     */
    private String InsuranceProvider_WholeName;

    /**
     * 法別番号 (例: )
     */
    private String InsuranceProvider_Identification_Number;

    /**
     * 記号 (例: ０１)
     */
    private String HealthInsuredPerson_Symbol;

    /**
     * 番号 (例: １２３４５６７)
     */
    private String HealthInsuredPerson_Number;

    /**
     * 枝番 (例: 01)
     */
    private String HealthInsuredPerson_Branch_Number;

    /**
     * 継続区分(1:継続療養、2:任意継続) (例:  )
     */
    private String HealthInsuredPerson_Continuation;

    /**
     * 補助区分(詳細については、「日医標準レセプトソフトデータベーステーブル定義書」を参照して下さい。) ( 例: 3)
     */
    private String HealthInsuredPerson_Assistance;

    /**
     * 本人家族区分(1:本人、2:家族) (例: 1)
     */
    private String RelationToInsuredPerson;

    /**
     * 被保険者名 (例: 日医　太郎)
     */
    private String HealthInsuredPerson_WholeName;

    /**
     * 適用開始日 (例: 2010-05-01)
     */
    private String Certificate_StartDate;

    /**
     * 適用終了日 (例: 9999-12-31)
     */
    private String Certificate_ExpiredDate;

    /**
     * 公費情報（繰り返し4） (例:  )
     */
    private PublicinsuranceInformation[] PublicInsurance_Information;

    /**
     * 入院負担割合 (例: 0.30)
     */
    private String InsuranceCombination_Rate_Admission;

    /**
     * 外来負担割合 (例: 0.30)
     */
    private String InsuranceCombination_Rate_Outpatient;

    /**
     * 保険組合せ非表示区分(O:外来非表示、I:入院非表示、N:非表示無し) (例:  )
     */
    private String Insurance_Nondisplay;

    /**
     * 補助区分名称 (例: ３割)
     */
    private String HealthInsuredPerson_Assistance_Name;

    /**
     * 補助区分名称※２ (例: ３割)
     */
    private String HealthInsuredPerson_Assistance_WholeName;

    /**
     * 補助区分情報※2（繰り返し30） (例: )
     */
    private HealthInsuredPersonAssistanceInfo[] HealthInsuredPerson_Assistance_Info;

    /**
     * 最終確認日 (例: 2014-01-06)
     */
    private String Insurance_CheckDate;

    /**
     * 労災情報 (例:  )
     */
    private AccidentInsuranceInformation Accident_Insurance_Information;


    /**
     * 保険組合せ入院負担割合 (例: 0.30)
     */
    private String Insurance_Combination_Rate_Admission;

    /**
     * 保険組合せ外来負担割合 (例: 0.30)
     */
    private String Insurance_Combination_Rate_Outpatient;

    /**
     * 保険組合せ有効開始日 (例: 2013-01-01)
     */
    private String Insurance_Combination_StartDate;

    /**
     * 保険組合せ有効終了日 (例: 9999-12-31)
     */
    private String Insurance_Combination_ExpiredDate;

    /**
     * 資格取得日 (例:  )
     */
    private String Certificate_GetDate;

    /**
     * 保険組合せ有効開始日 (例: 2013-01-01)
     */
    private String InsuranceCombination_StartDate;

    /**
     * 保険組合せ有効終了日 (例: 9999-12-31)
     */
    private String InsuranceCombination_ExpiredDate;

    /**
     * 高齢者負担割（１０：１割、３０：３割） (例: 30)
     */
    private String Rate_Class;

    /**
     * 患者個別情報 (例:  )
     */
    private PersonallyInformation Personally_Information;

    /**
     * 保険組合せ番号 (例: 0001)
     *
     * @return the Insurance_Combination_Number
     */
    public String getInsurance_Combination_Number() {
        return Insurance_Combination_Number;
    }

    /**
     * 保険組合せ番号 (例: 0001)
     *
     * @param Insurance_Combination_Number the Insurance_Combination_Number to set
     */
    public void setInsurance_Combination_Number(String Insurance_Combination_Number) {
        this.Insurance_Combination_Number = Insurance_Combination_Number;
    }

    /**
     * 保険組合せ番号 (例: 0002). acsimulatev2 のみ
     *
     * @return the Combination_Number
     */
    public String getCombination_Number() {
        return Combination_Number;
    }

    /**
     * 保険組合せ番号 (例: 0002). acsimulatev2 のみ
     *
     * @param Combination_Number the Combination_Number to set
     */
    public void setCombination_Number(String Combination_Number) {
        this.Combination_Number = Combination_Number;
    }

    /**
     * 保険の種類 (例: 060)
     *
     * @return the InsuranceProvider_Class
     */
    public String getInsuranceProvider_Class() {
        return InsuranceProvider_Class;
    }

    /**
     * 保険の種類 (例: 060)
     *
     * @param InsuranceProvider_Class the InsuranceProvider_Class to set
     */
    public void setInsuranceProvider_Class(String InsuranceProvider_Class) {
        this.InsuranceProvider_Class = InsuranceProvider_Class;
    }

    /**
     * 保険者番号 (例: 138057)
     *
     * @return the InsuranceProvider_Number
     */
    public String getInsuranceProvider_Number() {
        return InsuranceProvider_Number;
    }

    /**
     * 保険者番号 (例: 138057)
     *
     * @param InsuranceProvider_Number the InsuranceProvider_Number to set
     */
    public void setInsuranceProvider_Number(String InsuranceProvider_Number) {
        this.InsuranceProvider_Number = InsuranceProvider_Number;
    }

    /**
     * 保険の制度名称 (例: 国保)
     *
     * @return the InsuranceProvider_WholeName
     */
    public String getInsuranceProvider_WholeName() {
        return InsuranceProvider_WholeName;
    }

    /**
     * 保険の制度名称 (例: 国保)
     *
     * @param InsuranceProvider_WholeName the InsuranceProvider_WholeName to set
     */
    public void setInsuranceProvider_WholeName(String InsuranceProvider_WholeName) {
        this.InsuranceProvider_WholeName = InsuranceProvider_WholeName;
    }

    /**
     * 法別番号 (例: )
     *
     * @return InsuranceProvider_Identification_Number
     */
    public String getInsuranceProvider_Identification_Number() {
        return InsuranceProvider_Identification_Number;
    }

    /**
     * 法別番号 (例: )
     *
     * @param InsuranceProvider_Identification_Number to set
     */
    public void setInsuranceProvider_Identification_Number(String InsuranceProvider_Identification_Number) {
        this.InsuranceProvider_Identification_Number = InsuranceProvider_Identification_Number;
    }

    /**
     * 補助区分情報※2（繰り返し30） (例: )
     *
     * @return HealthInsuredPerson_Assistance_Info
     */
    public HealthInsuredPersonAssistanceInfo[] getHealthInsuredPerson_Assistance_Info() {
        return HealthInsuredPerson_Assistance_Info;
    }

    /**
     * 補助区分情報※2（繰り返し30） (例: )
     *
     * @param HealthInsuredPerson_Assistance_Info to set
     */
    public void setHealthInsuredPerson_Assistance_Info(HealthInsuredPersonAssistanceInfo[] HealthInsuredPerson_Assistance_Info) {
        this.HealthInsuredPerson_Assistance_Info = HealthInsuredPerson_Assistance_Info;
    }

    /**
     * 記号 (例: ０１)
     *
     * @return the HealthInsuredPerson_Symbol
     */
    public String getHealthInsuredPerson_Symbol() {
        return HealthInsuredPerson_Symbol;
    }

    /**
     * 記号 (例: ０１)
     *
     * @param HealthInsuredPerson_Symbol the HealthInsuredPerson_Symbol to set
     */
    public void setHealthInsuredPerson_Symbol(String HealthInsuredPerson_Symbol) {
        this.HealthInsuredPerson_Symbol = HealthInsuredPerson_Symbol;
    }

    /**
     * 番号 (例: １２３４５６７)
     *
     * @return the HealthInsuredPerson_Number
     */
    public String getHealthInsuredPerson_Number() {
        return HealthInsuredPerson_Number;
    }

    /**
     * 番号 (例: １２３４５６７)
     *
     * @param HealthInsuredPerson_Number the HealthInsuredPerson_Number to set
     */
    public void setHealthInsuredPerson_Number(String HealthInsuredPerson_Number) {
        this.HealthInsuredPerson_Number = HealthInsuredPerson_Number;
    }

    /**
     * HealthInsuredPerson_Branch_Number
     *
     * @return HealthInsuredPerson_Branch_Number
     */
    public String getHealthInsuredPerson_Branch_Number() {
        return HealthInsuredPerson_Branch_Number;
    }

    /**
     * HealthInsuredPerson_Branch_Number
     *
     * @param HealthInsuredPerson_Branch_Number to set
     */
    public void setHealthInsuredPerson_Branch_Number(String HealthInsuredPerson_Branch_Number) {
        this.HealthInsuredPerson_Branch_Number = HealthInsuredPerson_Branch_Number;
    }

    /**
     * 継続区分(1:継続療養、2:任意継続) (例:  )
     *
     * @return the HealthInsuredPerson_Continuation
     */
    public String getHealthInsuredPerson_Continuation() {
        return HealthInsuredPerson_Continuation;
    }

    /**
     * 継続区分(1:継続療養、2:任意継続) (例:  )
     *
     * @param HealthInsuredPerson_Continuation the HealthInsuredPerson_Continuation to set
     */
    public void setHealthInsuredPerson_Continuation(String HealthInsuredPerson_Continuation) {
        this.HealthInsuredPerson_Continuation = HealthInsuredPerson_Continuation;
    }

    /**
     * 補助区分(詳細については、「日医標準レセプトソフトデータベーステーブル定義書」を参照して下さい。) ( 例: 3)
     *
     * @return the HealthInsuredPerson_Assistance
     */
    public String getHealthInsuredPerson_Assistance() {
        return HealthInsuredPerson_Assistance;
    }

    /**
     * 補助区分(詳細については、「日医標準レセプトソフトデータベーステーブル定義書」を参照して下さい。) ( 例: 3)
     *
     * @param HealthInsuredPerson_Assistance the HealthInsuredPerson_Assistance to set
     */
    public void setHealthInsuredPerson_Assistance(String HealthInsuredPerson_Assistance) {
        this.HealthInsuredPerson_Assistance = HealthInsuredPerson_Assistance;
    }

    /**
     * 本人家族区分(1:本人、2:家族) (例: 1)
     *
     * @return the RelationToInsuredPerson
     */
    public String getRelationToInsuredPerson() {
        return RelationToInsuredPerson;
    }

    /**
     * 本人家族区分(1:本人、2:家族) (例: 1)
     *
     * @param RelationToInsuredPerson the RelationToInsuredPerson to set
     */
    public void setRelationToInsuredPerson(String RelationToInsuredPerson) {
        this.RelationToInsuredPerson = RelationToInsuredPerson;
    }

    /**
     * 被保険者名 (例: 日医　太郎)
     *
     * @return the HealthInsuredPerson_WholeName
     */
    public String getHealthInsuredPerson_WholeName() {
        return HealthInsuredPerson_WholeName;
    }

    /**
     * 被保険者名 (例: 日医　太郎)
     *
     * @param HealthInsuredPerson_WholeName the HealthInsuredPerson_WholeName to set
     */
    public void setHealthInsuredPerson_WholeName(String HealthInsuredPerson_WholeName) {
        this.HealthInsuredPerson_WholeName = HealthInsuredPerson_WholeName;
    }

    /**
     * 適用開始日 (例: 2010-05-01)
     *
     * @return the Certificate_StartDate
     */
    public String getCertificate_StartDate() {
        return Certificate_StartDate;
    }

    /**
     * 適用開始日 (例: 2010-05-01)
     *
     * @param Certificate_StartDate the Certificate_StartDate to set
     */
    public void setCertificate_StartDate(String Certificate_StartDate) {
        this.Certificate_StartDate = Certificate_StartDate;
    }

    /**
     * 適用終了日 (例: 9999-12-31)
     *
     * @return the Certificate_ExpiredDate
     */
    public String getCertificate_ExpiredDate() {
        return Certificate_ExpiredDate;
    }

    /**
     * 適用終了日 (例: 9999-12-31)
     *
     * @param Certificate_ExpiredDate the Certificate_ExpiredDate to set
     */
    public void setCertificate_ExpiredDate(String Certificate_ExpiredDate) {
        this.Certificate_ExpiredDate = Certificate_ExpiredDate;
    }

    /**
     * 公費情報（繰り返し4） (例:  )
     *
     * @return the PublicInsurance_Information
     */
    public PublicinsuranceInformation[] getPublicInsurance_Information() {
        return PublicInsurance_Information;
    }

    /**
     * 公費情報（繰り返し4） (例:  )
     *
     * @param PublicInsurance_Information the PublicInsurance_Information to set
     */
    public void setPublicInsurance_Information(PublicinsuranceInformation[] PublicInsurance_Information) {
        this.PublicInsurance_Information = PublicInsurance_Information;
    }

    /**
     * 入院負担割合 (例: 0.30)
     *
     * @return the InsuranceCombination_Rate_Admission
     */
    public String getInsuranceCombination_Rate_Admission() {
        return InsuranceCombination_Rate_Admission;
    }

    /**
     * 入院負担割合 (例: 0.30)
     *
     * @param InsuranceCombination_Rate_Admission the InsuranceCombination_Rate_Admission to set
     */
    public void setInsuranceCombination_Rate_Admission(String InsuranceCombination_Rate_Admission) {
        this.InsuranceCombination_Rate_Admission = InsuranceCombination_Rate_Admission;
    }

    /**
     * 外来負担割合 (例: 0.30)
     *
     * @return the InsuranceCombination_Rate_Outpatient
     */
    public String getInsuranceCombination_Rate_Outpatient() {
        return InsuranceCombination_Rate_Outpatient;
    }

    /**
     * 外来負担割合 (例: 0.30)
     *
     * @param InsuranceCombination_Rate_Outpatient the InsuranceCombination_Rate_Outpatient to set
     */
    public void setInsuranceCombination_Rate_Outpatient(String InsuranceCombination_Rate_Outpatient) {
        this.InsuranceCombination_Rate_Outpatient = InsuranceCombination_Rate_Outpatient;
    }

    /**
     * 保険組合せ非表示区分(O:外来非表示、I:入院非表示、N:非表示無し) (例:  )
     *
     * @return the Insurance_Nondisplay
     */
    public String getInsurance_Nondisplay() {
        return Insurance_Nondisplay;
    }

    /**
     * 保険組合せ非表示区分(O:外来非表示、I:入院非表示、N:非表示無し) (例:  )
     *
     * @param Insurance_Nondisplay the Insurance_Nondisplay to set
     */
    public void setInsurance_Nondisplay(String Insurance_Nondisplay) {
        this.Insurance_Nondisplay = Insurance_Nondisplay;
    }

    /**
     * 補助区分名称 (例: ３割)
     *
     * @return the HealthInsuredPerson_Assistance_Name
     */
    public String getHealthInsuredPerson_Assistance_Name() {
        return HealthInsuredPerson_Assistance_Name;
    }

    /**
     * 補助区分名称 (例: ３割)
     *
     * @param HealthInsuredPerson_Assistance_Name the HealthInsuredPerson_Assistance_Name to set
     */
    public void setHealthInsuredPerson_Assistance_Name(String HealthInsuredPerson_Assistance_Name) {
        this.HealthInsuredPerson_Assistance_Name = HealthInsuredPerson_Assistance_Name;
    }

    /**
     * 最終確認日 (例: 2014-01-06)
     *
     * @return the Insurance_CheckDate
     */
    public String getInsurance_CheckDate() {
        return Insurance_CheckDate;
    }

    /**
     * 最終確認日 (例: 2014-01-06)
     *
     * @param Insurance_CheckDate the Insurance_CheckDate to set
     */
    public void setInsurance_CheckDate(String Insurance_CheckDate) {
        this.Insurance_CheckDate = Insurance_CheckDate;
    }

    /**
     * 労災情報 (例:  )
     *
     * @return the Accident_Insurance_Information
     */
    public AccidentInsuranceInformation getAccident_Insurance_Information() {
        return Accident_Insurance_Information;
    }

    /**
     * 労災情報 (例:  )
     *
     * @param Accident_Insurance_Information the Accident_Insurance_Information to set
     */
    public void setAccident_Insurance_Information(AccidentInsuranceInformation Accident_Insurance_Information) {
        this.Accident_Insurance_Information = Accident_Insurance_Information;
    }

    /**
     * 補助区分名称※２ (例: ３割)
     *
     * @return the HealthInsuredPerson_Assistance_WholeName
     */
    public String getHealthInsuredPerson_Assistance_WholeName() {
        return HealthInsuredPerson_Assistance_WholeName;
    }

    /**
     * 補助区分名称※２ (例: ３割)
     *
     * @param HealthInsuredPerson_Assistance_WholeName the HealthInsuredPerson_Assistance_WholeName to set
     */
    public void setHealthInsuredPerson_Assistance_WholeName(String HealthInsuredPerson_Assistance_WholeName) {
        this.HealthInsuredPerson_Assistance_WholeName = HealthInsuredPerson_Assistance_WholeName;
    }

    /**
     * 保険組合せ入院負担割合 (例: 0.30)
     *
     * @return the Insurance_Combination_Rate_Admission
     */
    public String getInsurance_Combination_Rate_Admission() {
        return Insurance_Combination_Rate_Admission;
    }

    /**
     * 保険組合せ入院負担割合 (例: 0.30)
     *
     * @param Insurance_Combination_Rate_Admission the Insurance_Combination_Rate_Admission to set
     */
    public void setInsurance_Combination_Rate_Admission(String Insurance_Combination_Rate_Admission) {
        this.Insurance_Combination_Rate_Admission = Insurance_Combination_Rate_Admission;
    }

    /**
     * 保険組合せ外来負担割合 (例: 0.30)
     *
     * @return the Insurance_Combination_Rate_Outpatient
     */
    public String getInsurance_Combination_Rate_Outpatient() {
        return Insurance_Combination_Rate_Outpatient;
    }

    /**
     * 保険組合せ外来負担割合 (例: 0.30)
     *
     * @param Insurance_Combination_Rate_Outpatient the Insurance_Combination_Rate_Outpatient to set
     */
    public void setInsurance_Combination_Rate_Outpatient(String Insurance_Combination_Rate_Outpatient) {
        this.Insurance_Combination_Rate_Outpatient = Insurance_Combination_Rate_Outpatient;
    }

    /**
     * 保険組合せ有効開始日 (例: 2013-01-01)
     *
     * @return the Insurance_Combination_StartDate
     */
    public String getInsurance_Combination_StartDate() {
        return Insurance_Combination_StartDate;
    }

    /**
     * 保険組合せ有効開始日 (例: 2013-01-01)
     *
     * @param Insurance_Combination_StartDate the Insurance_Combination_StartDate to set
     */
    public void setInsurance_Combination_StartDate(String Insurance_Combination_StartDate) {
        this.Insurance_Combination_StartDate = Insurance_Combination_StartDate;
    }

    /**
     * 保険組合せ有効終了日 (例: 9999-12-31)
     *
     * @return the Insurance_Combination_ExpiredDate
     */
    public String getInsurance_Combination_ExpiredDate() {
        return Insurance_Combination_ExpiredDate;
    }

    /**
     * 保険組合せ有効終了日 (例: 9999-12-31)
     *
     * @param Insurance_Combination_ExpiredDate the Insurance_Combination_ExpiredDate to set
     */
    public void setInsurance_Combination_ExpiredDate(String Insurance_Combination_ExpiredDate) {
        this.Insurance_Combination_ExpiredDate = Insurance_Combination_ExpiredDate;
    }

    /**
     * 資格取得日 (例:  )
     *
     * @return the Certificate_GetDate
     */
    public String getCertificate_GetDate() {
        return Certificate_GetDate;
    }

    /**
     * 資格取得日 (例:  )
     *
     * @param Certificate_GetDate the Certificate_GetDate to set
     */
    public void setCertificate_GetDate(String Certificate_GetDate) {
        this.Certificate_GetDate = Certificate_GetDate;
    }

    /**
     * InsuranceCombination_StartDate
     *
     * @return InsuranceCombination_StartDate
     */
    public String getInsuranceCombination_StartDate() {
        return InsuranceCombination_StartDate;
    }

    /**
     * InsuranceCombination_StartDate
     *
     * @param InsuranceCombination_StartDate to set
     */
    public void setInsuranceCombination_StartDate(String InsuranceCombination_StartDate) {
        this.InsuranceCombination_StartDate = InsuranceCombination_StartDate;
    }

    /**
     * InsuranceCombination_ExpiredDate
     *
     * @return InsuranceCombination_ExpiredDate
     */
    public String getInsuranceCombination_ExpiredDate() {
        return InsuranceCombination_ExpiredDate;
    }

    /**
     * InsuranceCombination_ExpiredDate
     *
     * @param InsuranceCombination_ExpiredDate to set
     */
    public void setInsuranceCombination_ExpiredDate(String InsuranceCombination_ExpiredDate) {
        this.InsuranceCombination_ExpiredDate = InsuranceCombination_ExpiredDate;
    }

    /**
     * Rate_Class
     *
     * @return Rate_Class
     */
    public String getRate_Class() {
        return Rate_Class;
    }

    /**
     * Rate_Class
     *
     * @param Rate_Class to set
     */
    public void setRate_Class(String Rate_Class) {
        this.Rate_Class = Rate_Class;
    }

    /**
     * Personally_Information
     *
     * @return Personally_Information
     */
    public PersonallyInformation getPersonally_Information() {
        return Personally_Information;
    }

    /**
     * Personally_Information
     *
     * @param Personally_Information to set
     */
    public void setPersonally_Information(PersonallyInformation Personally_Information) {
        this.Personally_Information = Personally_Information;
    }
}