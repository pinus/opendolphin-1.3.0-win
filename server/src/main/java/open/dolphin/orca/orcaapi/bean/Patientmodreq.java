package open.dolphin.orca.orcaapi.bean;

/**
 * patientmodreq.
 *
 * @author pns
 */
public class Patientmodreq {
    /**
     * 変更キー (例: 2).
     * Mod_Keyが１または設定無しの場合、患者番号・性別・生年月日が一致する患者を対象として更新します。
     * Mod_Keyが２の場合、患者番号・漢字氏名・カナ氏名が一致する患者を対象として更新します。
     */
    private String Mod_Key;

    /**
     * 患者番号 (例: ＊)
     */
    private String Patient_ID;

    /**
     * 患者氏名 (例: 日医　太郎)
     */
    private String WholeName;

    /**
     * 患者カナ氏名 (例: ニチイ　タロウ)
     */
    private String WholeName_inKana;

    /**
     * 生年月日 (例: 1970-01-01)
     */
    private String BirthDate;

    /**
     * 性別(1:男、2:女) (例: 1)
     */
    private String Sex;

    /**
     * 世帯主名 (例: 日医　太郎)
     */
    private String HouseHolder_WholeName;

    /**
     * 続柄 (例: 本人)
     */
    private String Relationship;

    /**
     * 職業 (例: 会社員)
     */
    private String Occupation;

    /**
     * 携帯番号 (例: 09011112222)
     */
    private String CellularNumber;

    /**
     * FAX番号 (例: 03-0011-2233)
     */
    private String FaxNumber;

    /**
     * 電子メールアドレス (例: test@tt.dot.jp)
     */
    private String EmailAddress;

    /**
     * 自宅情報 (例:  )
     */
    private HomeAddressInformation Home_Address_Information;

    /**
     * 勤務先情報 (例:  )
     */
    private WorkplaceInformation WorkPlace_Information;

    /**
     * 禁忌１ (例: 状態)
     */
    private String Contraindication1;

    /**
     * 禁忌２ (例:  )
     */
    private String Contraindication2;

    /**
     * アレルギー１ (例: アレルギ)
     */
    private String Allergy1;

    /**
     * アレルギー２ (例:  )
     */
    private String Allergy2;

    /**
     * 感染症１ (例: 感染症)
     */
    private String Infection1;

    /**
     * 感染症２ (例:  )
     */
    private String Infection2;

    /**
     * コメント１ (例: コメント)
     */
    private String Comment1;

    /**
     * コメント２ (例:  )
     */
    private String Comment2;

    /**
     * 保険組合せ情報 (例:  )
     */
    private HealthInsuranceInformation HealthInsurance_Information;

    /**
     * 支払情報 (例:  )
     */
    private PaymentInformation Payment_Information;

    /**
     * 状態情報 (例:  )
     */
    private ConditionInformation Condition_Information;

    /**
     * 変更キー (例: 2)
     *
     * @return the Mod_Key
     */
    public String getMod_Key() {
        return Mod_Key;
    }

    /**
     * 変更キー (例: 2)
     *
     * @param Mod_Key the Mod_Key to set
     */
    public void setMod_Key(String Mod_Key) {
        this.Mod_Key = Mod_Key;
    }

    /**
     * 患者番号 (例: ＊)
     *
     * @return the Patient_ID
     */
    public String getPatient_ID() {
        return Patient_ID;
    }

    /**
     * 患者番号 (例: ＊)
     *
     * @param Patient_ID the Patient_ID to set
     */
    public void setPatient_ID(String Patient_ID) {
        this.Patient_ID = Patient_ID;
    }

    /**
     * 患者氏名 (例: 日医　太郎)
     *
     * @return the WholeName
     */
    public String getWholeName() {
        return WholeName;
    }

    /**
     * 患者氏名 (例: 日医　太郎)
     *
     * @param WholeName the WholeName to set
     */
    public void setWholeName(String WholeName) {
        this.WholeName = WholeName;
    }

    /**
     * 患者カナ氏名 (例: ニチイ　タロウ)
     *
     * @return the WholeName_inKana
     */
    public String getWholeName_inKana() {
        return WholeName_inKana;
    }

    /**
     * 患者カナ氏名 (例: ニチイ　タロウ)
     *
     * @param WholeName_inKana the WholeName_inKana to set
     */
    public void setWholeName_inKana(String WholeName_inKana) {
        this.WholeName_inKana = WholeName_inKana;
    }

    /**
     * 生年月日 (例: 1970-01-01)
     *
     * @return the BirthDate
     */
    public String getBirthDate() {
        return BirthDate;
    }

    /**
     * 生年月日 (例: 1970-01-01)
     *
     * @param BirthDate the BirthDate to set
     */
    public void setBirthDate(String BirthDate) {
        this.BirthDate = BirthDate;
    }

    /**
     * 性別(1:男、2:女) (例: 1)
     *
     * @return the Sex
     */
    public String getSex() {
        return Sex;
    }

    /**
     * 性別(1:男、2:女) (例: 1)
     *
     * @param Sex the Sex to set
     */
    public void setSex(String Sex) {
        this.Sex = Sex;
    }

    /**
     * 世帯主名 (例: 日医　太郎)
     *
     * @return the HouseHolder_WholeName
     */
    public String getHouseHolder_WholeName() {
        return HouseHolder_WholeName;
    }

    /**
     * 世帯主名 (例: 日医　太郎)
     *
     * @param HouseHolder_WholeName the HouseHolder_WholeName to set
     */
    public void setHouseHolder_WholeName(String HouseHolder_WholeName) {
        this.HouseHolder_WholeName = HouseHolder_WholeName;
    }

    /**
     * 続柄 (例: 本人)
     *
     * @return the Relationship
     */
    public String getRelationship() {
        return Relationship;
    }

    /**
     * 続柄 (例: 本人)
     *
     * @param Relationship the Relationship to set
     */
    public void setRelationship(String Relationship) {
        this.Relationship = Relationship;
    }

    /**
     * 職業 (例: 会社員)
     *
     * @return the Occupation
     */
    public String getOccupation() {
        return Occupation;
    }

    /**
     * 職業 (例: 会社員)
     *
     * @param Occupation the Occupation to set
     */
    public void setOccupation(String Occupation) {
        this.Occupation = Occupation;
    }

    /**
     * 携帯番号 (例: 09011112222)
     *
     * @return the CellularNumber
     */
    public String getCellularNumber() {
        return CellularNumber;
    }

    /**
     * 携帯番号 (例: 09011112222)
     *
     * @param CellularNumber the CellularNumber to set
     */
    public void setCellularNumber(String CellularNumber) {
        this.CellularNumber = CellularNumber;
    }

    /**
     * FAX番号 (例: 03-0011-2233)
     *
     * @return the FaxNumber
     */
    public String getFaxNumber() {
        return FaxNumber;
    }

    /**
     * FAX番号 (例: 03-0011-2233)
     *
     * @param FaxNumber the FaxNumber to set
     */
    public void setFaxNumber(String FaxNumber) {
        this.FaxNumber = FaxNumber;
    }

    /**
     * 電子メールアドレス (例: test@tt.dot.jp)
     *
     * @return the EmailAddress
     */
    public String getEmailAddress() {
        return EmailAddress;
    }

    /**
     * 電子メールアドレス (例: test@tt.dot.jp)
     *
     * @param EmailAddress the EmailAddress to set
     */
    public void setEmailAddress(String EmailAddress) {
        this.EmailAddress = EmailAddress;
    }

    /**
     * 自宅情報 (例:  )
     *
     * @return the Home_Address_Information
     */
    public HomeAddressInformation getHome_Address_Information() {
        return Home_Address_Information;
    }

    /**
     * 自宅情報 (例:  )
     *
     * @param Home_Address_Information the Home_Address_Information to set
     */
    public void setHome_Address_Information(HomeAddressInformation Home_Address_Information) {
        this.Home_Address_Information = Home_Address_Information;
    }

    /**
     * 勤務先情報 (例:  )
     *
     * @return the WorkPlace_Information
     */
    public WorkplaceInformation getWorkPlace_Information() {
        return WorkPlace_Information;
    }

    /**
     * 勤務先情報 (例:  )
     *
     * @param WorkPlace_Information the WorkPlace_Information to set
     */
    public void setWorkPlace_Information(WorkplaceInformation WorkPlace_Information) {
        this.WorkPlace_Information = WorkPlace_Information;
    }

    /**
     * 禁忌１ (例: 状態)
     *
     * @return the Contraindication1
     */
    public String getContraindication1() {
        return Contraindication1;
    }

    /**
     * 禁忌１ (例: 状態)
     *
     * @param Contraindication1 the Contraindication1 to set
     */
    public void setContraindication1(String Contraindication1) {
        this.Contraindication1 = Contraindication1;
    }

    /**
     * 禁忌２ (例:  )
     *
     * @return the Contraindication2
     */
    public String getContraindication2() {
        return Contraindication2;
    }

    /**
     * 禁忌２ (例:  )
     *
     * @param Contraindication2 the Contraindication2 to set
     */
    public void setContraindication2(String Contraindication2) {
        this.Contraindication2 = Contraindication2;
    }

    /**
     * アレルギー１ (例: アレルギ)
     *
     * @return the Allergy1
     */
    public String getAllergy1() {
        return Allergy1;
    }

    /**
     * アレルギー１ (例: アレルギ)
     *
     * @param Allergy1 the Allergy1 to set
     */
    public void setAllergy1(String Allergy1) {
        this.Allergy1 = Allergy1;
    }

    /**
     * アレルギー２ (例:  )
     *
     * @return the Allergy2
     */
    public String getAllergy2() {
        return Allergy2;
    }

    /**
     * アレルギー２ (例:  )
     *
     * @param Allergy2 the Allergy2 to set
     */
    public void setAllergy2(String Allergy2) {
        this.Allergy2 = Allergy2;
    }

    /**
     * 感染症１ (例: 感染症)
     *
     * @return the Infection1
     */
    public String getInfection1() {
        return Infection1;
    }

    /**
     * 感染症１ (例: 感染症)
     *
     * @param Infection1 the Infection1 to set
     */
    public void setInfection1(String Infection1) {
        this.Infection1 = Infection1;
    }

    /**
     * 感染症２ (例:  )
     *
     * @return the Infection2
     */
    public String getInfection2() {
        return Infection2;
    }

    /**
     * 感染症２ (例:  )
     *
     * @param Infection2 the Infection2 to set
     */
    public void setInfection2(String Infection2) {
        this.Infection2 = Infection2;
    }

    /**
     * コメント１ (例: コメント)
     *
     * @return the Comment1
     */
    public String getComment1() {
        return Comment1;
    }

    /**
     * コメント１ (例: コメント)
     *
     * @param Comment1 the Comment1 to set
     */
    public void setComment1(String Comment1) {
        this.Comment1 = Comment1;
    }

    /**
     * コメント２ (例:  )
     *
     * @return the Comment2
     */
    public String getComment2() {
        return Comment2;
    }

    /**
     * コメント２ (例:  )
     *
     * @param Comment2 the Comment2 to set
     */
    public void setComment2(String Comment2) {
        this.Comment2 = Comment2;
    }

    /**
     * 保険組合せ情報 (例:  )
     *
     * @return the HealthInsurance_Information
     */
    public HealthInsuranceInformation getHealthInsurance_Information() {
        return HealthInsurance_Information;
    }

    /**
     * 保険組合せ情報 (例:  )
     *
     * @param HealthInsurance_Information the HealthInsurance_Information to set
     */
    public void setHealthInsurance_Information(HealthInsuranceInformation HealthInsurance_Information) {
        this.HealthInsurance_Information = HealthInsurance_Information;
    }

    /**
     * Payment_Information
     *
     * @return Payment_Information
     */
    public PaymentInformation getPayment_Information() {
        return Payment_Information;
    }

    /**
     * Payment_Information
     *
     * @param Payment_Information to set
     */
    public void setPayment_Information(PaymentInformation Payment_Information) {
        this.Payment_Information = Payment_Information;
    }

    /**
     * Condition_Information
     *
     * @return Condition_Information
     */
    public ConditionInformation getCondition_Information() {
        return Condition_Information;
    }

    /**
     * Condition_Information
     *
     * @param Condition_Information to set
     */
    public void setCondition_Information(ConditionInformation Condition_Information) {
        this.Condition_Information = Condition_Information;
    }
}