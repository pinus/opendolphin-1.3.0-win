package open.dolphin.orca.orcaapi.bean;

/**
 * Patient_Information. 患者基本情報
 *
 * @author pns
 */
public class PatientInformation {
    /**
     * 患者番号 (例: 00017)
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
     * 生年月日 (例: 1975-01-01)
     */
    private String BirthDate;

    /**
     * 性別(1:男性、2:女性) (例: 1)
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
     * 自宅住所情報 (例:  )
     */
    private HomeAddressInformation Home_Address_Information;

    /**
     * 勤務先情報 (例:  )
     */
    private WorkplaceInformation WorkPlace_Information;

    /**
     * 連絡先情報 (例:  )
     */
    private ContactInformation Contact_Information;

    /**
     * 帰省先情報 (例:  )
     */
    private Home2Information Home2_Information;

    /**
     * 禁忌１ (例: 禁忌)
     */
    private String Contraindication1;

    /**
     * 禁忌２ (例:  )
     */
    private String Contraindication2;

    /**
     * アレルギー１ (例: アレルギー)
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
     * テスト患者区分(0:患者、1:テスト患者) (例: 0)
     */
    private String TestPatient_Flag;

    /**
     * 死亡区分(1:死亡) (例:  )
     */
    private String Death_Flag;

    /**
     * 職業 (例:  )
     */
    private String Occupation;

    /**
     * 通称名 (例:  )
     */
    private String NickName;

    /**
     * 携帯電話番号 (例:  )
     */
    private String CellularNumber;

    /**
     * ＦＡＸ番号 (例:  )
     */
    private String FaxNumber;

    /**
     * 電子メールアドレス (例:  )
     */
    private String EmailAddress;

    /**
     * 減免事由番号 (例:  )
     */
    private String Reduction_Reason;

    /**
     * 減免事由 (例:  )
     */
    private String Reduction_Reason_Name;

    /**
     * 割引率 (例:  )
     */
    private String Discount;

    /**
     * 割引率 (例:  )
     */
    private String Discount_Name;

    /**
     * 状態番号１ (例:  )
     */
    private String Condition1;

    /**
     * 状態１ (例:  )
     */
    private String Condition1_Name;

    /**
     * 状態番号２ (例:  )
     */
    private String Condition2;

    /**
     * 状態２ (例:  )
     */
    private String Condition2_Name;

    /**
     * 状態番号３ (例:  )
     */
    private String Condition3;

    /**
     * 状態３ (例:  )
     */
    private String Condition3_Name;

    /**
     * 入金方法区分 (例:  )
     */
    private String Ic_Code;

    /**
     * 入金方法 (例:  )
     */
    private String Ic_Code_Name;

    /**
     * 地域連携ID (例:  )
     */
    private String Community_Cid;

    /**
     * 同意フラグ（True：同意する、False：それ以外） (例: False)
     */
    private String Community_Cid_Agree;

    /**
     * 初回受診日 (例: 2014-01-06)
     */
    private String FirstVisit_Date;

    /**
     * 最終受診日 (例:  )
     */
    private String LastVisit_Date;

    /**
     * 入院中 (例: 1)
     */
    private String Outpatient_Class;

    /**
     * 入院日 (例: 2014-06-03)
     */
    private String Admission_Date;

    /**
     * 退院日 (例:  )
     */
    private String Discharge_Date;

    /**
     * 保険組合せ情報(繰り返し　２０） (例:  )
     */
    private HealthInsuranceInformation[] HealthInsurance_Information;

    /**
     * 介護情報 (例:  )
     */
    private CareInformation Care_Information;

    /**
     * 患者個別情報 (例:  )
     */
    private PersonallyInformation Personally_Information;

    /**
     * 個人番号情報（繰り返し　２０） (例:  )
     */
    private IndividualNumber[] Individual_Number;

    /**
     * 管理料等自動算定情報（繰り返し　３） (例:  )
     */
    private AutoManagementInformation[] Auto_Management_Information;

    /**
     * 患者禁忌薬剤情報 (例:  )
     */
    private PatientContraInformation Patient_Contra_Information;

    /**
     * 登録日付 (例: )
     */
    private String CreateDate;

    /**
     * 更新日付 (例: )
     */
    private String UpdateDate;

    /**
     * 更新時間 (例: )
     */
    private String UpdateTime;

    /**
     * 支払情報 (例:  )
     */
    private PaymentInformation Payment_Information;

    /**
     * 状態情報 (例:  )
     */
    private ConditionInformation Condition_Information;

    /**
     * Patient_ID
     *
     * @return Patient_ID
     */
    public String getPatient_ID() {
        return Patient_ID;
    }

    /**
     * Patient_ID
     *
     * @param Patient_ID to set
     */
    public void setPatient_ID(String Patient_ID) {
        this.Patient_ID = Patient_ID;
    }

    /**
     * WholeName
     *
     * @return WholeName
     */
    public String getWholeName() {
        return WholeName;
    }

    /**
     * WholeName
     *
     * @param WholeName to set
     */
    public void setWholeName(String WholeName) {
        this.WholeName = WholeName;
    }

    /**
     * WholeName_inKana
     *
     * @return WholeName_inKana
     */
    public String getWholeName_inKana() {
        return WholeName_inKana;
    }

    /**
     * WholeName_inKana
     *
     * @param WholeName_inKana to set
     */
    public void setWholeName_inKana(String WholeName_inKana) {
        this.WholeName_inKana = WholeName_inKana;
    }

    /**
     * BirthDate
     *
     * @return BirthDate
     */
    public String getBirthDate() {
        return BirthDate;
    }

    /**
     * BirthDate
     *
     * @param BirthDate to set
     */
    public void setBirthDate(String BirthDate) {
        this.BirthDate = BirthDate;
    }

    /**
     * Sex
     *
     * @return Sex
     */
    public String getSex() {
        return Sex;
    }

    /**
     * Sex
     *
     * @param Sex to set
     */
    public void setSex(String Sex) {
        this.Sex = Sex;
    }

    /**
     * HouseHolder_WholeName
     *
     * @return HouseHolder_WholeName
     */
    public String getHouseHolder_WholeName() {
        return HouseHolder_WholeName;
    }

    /**
     * HouseHolder_WholeName
     *
     * @param HouseHolder_WholeName to set
     */
    public void setHouseHolder_WholeName(String HouseHolder_WholeName) {
        this.HouseHolder_WholeName = HouseHolder_WholeName;
    }

    /**
     * Relationship
     *
     * @return Relationship
     */
    public String getRelationship() {
        return Relationship;
    }

    /**
     * Relationship
     *
     * @param Relationship to set
     */
    public void setRelationship(String Relationship) {
        this.Relationship = Relationship;
    }

    /**
     * Home_Address_Information
     *
     * @return Home_Address_Information
     */
    public HomeAddressInformation getHome_Address_Information() {
        return Home_Address_Information;
    }

    /**
     * Home_Address_Information
     *
     * @param Home_Address_Information to set
     */
    public void setHome_Address_Information(HomeAddressInformation Home_Address_Information) {
        this.Home_Address_Information = Home_Address_Information;
    }

    /**
     * WorkPlace_Information
     *
     * @return WorkPlace_Information
     */
    public WorkplaceInformation getWorkPlace_Information() {
        return WorkPlace_Information;
    }

    /**
     * WorkPlace_Information
     *
     * @param WorkPlace_Information to set
     */
    public void setWorkPlace_Information(WorkplaceInformation WorkPlace_Information) {
        this.WorkPlace_Information = WorkPlace_Information;
    }

    /**
     * Contact_Information
     *
     * @return Contact_Information
     */
    public ContactInformation getContact_Information() {
        return Contact_Information;
    }

    /**
     * Contact_Information
     *
     * @param Contact_Information to set
     */
    public void setContact_Information(ContactInformation Contact_Information) {
        this.Contact_Information = Contact_Information;
    }

    /**
     * Home2_Information
     *
     * @return Home2_Information
     */
    public Home2Information getHome2_Information() {
        return Home2_Information;
    }

    /**
     * Home2_Information
     *
     * @param Home2_Information to set
     */
    public void setHome2_Information(Home2Information Home2_Information) {
        this.Home2_Information = Home2_Information;
    }

    /**
     * Contraindication1
     *
     * @return Contraindication1
     */
    public String getContraindication1() {
        return Contraindication1;
    }

    /**
     * Contraindication1
     *
     * @param Contraindication1 to set
     */
    public void setContraindication1(String Contraindication1) {
        this.Contraindication1 = Contraindication1;
    }

    /**
     * Contraindication2
     *
     * @return Contraindication2
     */
    public String getContraindication2() {
        return Contraindication2;
    }

    /**
     * Contraindication2
     *
     * @param Contraindication2 to set
     */
    public void setContraindication2(String Contraindication2) {
        this.Contraindication2 = Contraindication2;
    }

    /**
     * Allergy1
     *
     * @return Allergy1
     */
    public String getAllergy1() {
        return Allergy1;
    }

    /**
     * Allergy1
     *
     * @param Allergy1 to set
     */
    public void setAllergy1(String Allergy1) {
        this.Allergy1 = Allergy1;
    }

    /**
     * Allergy2
     *
     * @return Allergy2
     */
    public String getAllergy2() {
        return Allergy2;
    }

    /**
     * Allergy2
     *
     * @param Allergy2 to set
     */
    public void setAllergy2(String Allergy2) {
        this.Allergy2 = Allergy2;
    }

    /**
     * Infection1
     *
     * @return Infection1
     */
    public String getInfection1() {
        return Infection1;
    }

    /**
     * Infection1
     *
     * @param Infection1 to set
     */
    public void setInfection1(String Infection1) {
        this.Infection1 = Infection1;
    }

    /**
     * Infection2
     *
     * @return Infection2
     */
    public String getInfection2() {
        return Infection2;
    }

    /**
     * Infection2
     *
     * @param Infection2 to set
     */
    public void setInfection2(String Infection2) {
        this.Infection2 = Infection2;
    }

    /**
     * Comment1
     *
     * @return Comment1
     */
    public String getComment1() {
        return Comment1;
    }

    /**
     * Comment1
     *
     * @param Comment1 to set
     */
    public void setComment1(String Comment1) {
        this.Comment1 = Comment1;
    }

    /**
     * Comment2
     *
     * @return Comment2
     */
    public String getComment2() {
        return Comment2;
    }

    /**
     * Comment2
     *
     * @param Comment2 to set
     */
    public void setComment2(String Comment2) {
        this.Comment2 = Comment2;
    }

    /**
     * TestPatient_Flag
     *
     * @return TestPatient_Flag
     */
    public String getTestPatient_Flag() {
        return TestPatient_Flag;
    }

    /**
     * TestPatient_Flag
     *
     * @param TestPatient_Flag to set
     */
    public void setTestPatient_Flag(String TestPatient_Flag) {
        this.TestPatient_Flag = TestPatient_Flag;
    }

    /**
     * Death_Flag
     *
     * @return Death_Flag
     */
    public String getDeath_Flag() {
        return Death_Flag;
    }

    /**
     * Death_Flag
     *
     * @param Death_Flag to set
     */
    public void setDeath_Flag(String Death_Flag) {
        this.Death_Flag = Death_Flag;
    }

    /**
     * Occupation
     *
     * @return Occupation
     */
    public String getOccupation() {
        return Occupation;
    }

    /**
     * Occupation
     *
     * @param Occupation to set
     */
    public void setOccupation(String Occupation) {
        this.Occupation = Occupation;
    }

    /**
     * NickName
     *
     * @return NickName
     */
    public String getNickName() {
        return NickName;
    }

    /**
     * NickName
     *
     * @param NickName to set
     */
    public void setNickName(String NickName) {
        this.NickName = NickName;
    }

    /**
     * CellularNumber
     *
     * @return CellularNumber
     */
    public String getCellularNumber() {
        return CellularNumber;
    }

    /**
     * CellularNumber
     *
     * @param CellularNumber to set
     */
    public void setCellularNumber(String CellularNumber) {
        this.CellularNumber = CellularNumber;
    }

    /**
     * FaxNumber
     *
     * @return FaxNumber
     */
    public String getFaxNumber() {
        return FaxNumber;
    }

    /**
     * FaxNumber
     *
     * @param FaxNumber to set
     */
    public void setFaxNumber(String FaxNumber) {
        this.FaxNumber = FaxNumber;
    }

    /**
     * EmailAddress
     *
     * @return EmailAddress
     */
    public String getEmailAddress() {
        return EmailAddress;
    }

    /**
     * EmailAddress
     *
     * @param EmailAddress to set
     */
    public void setEmailAddress(String EmailAddress) {
        this.EmailAddress = EmailAddress;
    }

    /**
     * Reduction_Reason
     *
     * @return Reduction_Reason
     */
    public String getReduction_Reason() {
        return Reduction_Reason;
    }

    /**
     * Reduction_Reason
     *
     * @param Reduction_Reason to set
     */
    public void setReduction_Reason(String Reduction_Reason) {
        this.Reduction_Reason = Reduction_Reason;
    }

    /**
     * Reduction_Reason_Name
     *
     * @return Reduction_Reason_Name
     */
    public String getReduction_Reason_Name() {
        return Reduction_Reason_Name;
    }

    /**
     * Reduction_Reason_Name
     *
     * @param Reduction_Reason_Name to set
     */
    public void setReduction_Reason_Name(String Reduction_Reason_Name) {
        this.Reduction_Reason_Name = Reduction_Reason_Name;
    }

    /**
     * Discount
     *
     * @return Discount
     */
    public String getDiscount() {
        return Discount;
    }

    /**
     * Discount
     *
     * @param Discount to set
     */
    public void setDiscount(String Discount) {
        this.Discount = Discount;
    }

    /**
     * Discount_Name
     *
     * @return Discount_Name
     */
    public String getDiscount_Name() {
        return Discount_Name;
    }

    /**
     * Discount_Name
     *
     * @param Discount_Name to set
     */
    public void setDiscount_Name(String Discount_Name) {
        this.Discount_Name = Discount_Name;
    }

    /**
     * Condition1
     *
     * @return Condition1
     */
    public String getCondition1() {
        return Condition1;
    }

    /**
     * Condition1
     *
     * @param Condition1 to set
     */
    public void setCondition1(String Condition1) {
        this.Condition1 = Condition1;
    }

    /**
     * Condition1_Name
     *
     * @return Condition1_Name
     */
    public String getCondition1_Name() {
        return Condition1_Name;
    }

    /**
     * Condition1_Name
     *
     * @param Condition1_Name to set
     */
    public void setCondition1_Name(String Condition1_Name) {
        this.Condition1_Name = Condition1_Name;
    }

    /**
     * Condition2
     *
     * @return Condition2
     */
    public String getCondition2() {
        return Condition2;
    }

    /**
     * Condition2
     *
     * @param Condition2 to set
     */
    public void setCondition2(String Condition2) {
        this.Condition2 = Condition2;
    }

    /**
     * Condition2_Name
     *
     * @return Condition2_Name
     */
    public String getCondition2_Name() {
        return Condition2_Name;
    }

    /**
     * Condition2_Name
     *
     * @param Condition2_Name to set
     */
    public void setCondition2_Name(String Condition2_Name) {
        this.Condition2_Name = Condition2_Name;
    }

    /**
     * Condition3
     *
     * @return Condition3
     */
    public String getCondition3() {
        return Condition3;
    }

    /**
     * Condition3
     *
     * @param Condition3 to set
     */
    public void setCondition3(String Condition3) {
        this.Condition3 = Condition3;
    }

    /**
     * Condition3_Name
     *
     * @return Condition3_Name
     */
    public String getCondition3_Name() {
        return Condition3_Name;
    }

    /**
     * Condition3_Name
     *
     * @param Condition3_Name to set
     */
    public void setCondition3_Name(String Condition3_Name) {
        this.Condition3_Name = Condition3_Name;
    }

    /**
     * Ic_Code
     *
     * @return Ic_Code
     */
    public String getIc_Code() {
        return Ic_Code;
    }

    /**
     * Ic_Code
     *
     * @param Ic_Code to set
     */
    public void setIc_Code(String Ic_Code) {
        this.Ic_Code = Ic_Code;
    }

    /**
     * Ic_Code_Name
     *
     * @return Ic_Code_Name
     */
    public String getIc_Code_Name() {
        return Ic_Code_Name;
    }

    /**
     * Ic_Code_Name
     *
     * @param Ic_Code_Name to set
     */
    public void setIc_Code_Name(String Ic_Code_Name) {
        this.Ic_Code_Name = Ic_Code_Name;
    }

    /**
     * Community_Cid
     *
     * @return Community_Cid
     */
    public String getCommunity_Cid() {
        return Community_Cid;
    }

    /**
     * Community_Cid
     *
     * @param Community_Cid to set
     */
    public void setCommunity_Cid(String Community_Cid) {
        this.Community_Cid = Community_Cid;
    }

    /**
     * Community_Cid_Agree
     *
     * @return Community_Cid_Agree
     */
    public String getCommunity_Cid_Agree() {
        return Community_Cid_Agree;
    }

    /**
     * Community_Cid_Agree
     *
     * @param Community_Cid_Agree to set
     */
    public void setCommunity_Cid_Agree(String Community_Cid_Agree) {
        this.Community_Cid_Agree = Community_Cid_Agree;
    }

    /**
     * FirstVisit_Date
     *
     * @return FirstVisit_Date
     */
    public String getFirstVisit_Date() {
        return FirstVisit_Date;
    }

    /**
     * FirstVisit_Date
     *
     * @param FirstVisit_Date to set
     */
    public void setFirstVisit_Date(String FirstVisit_Date) {
        this.FirstVisit_Date = FirstVisit_Date;
    }

    /**
     * LastVisit_Date
     *
     * @return LastVisit_Date
     */
    public String getLastVisit_Date() {
        return LastVisit_Date;
    }

    /**
     * LastVisit_Date
     *
     * @param LastVisit_Date to set
     */
    public void setLastVisit_Date(String LastVisit_Date) {
        this.LastVisit_Date = LastVisit_Date;
    }

    /**
     * Outpatient_Class
     *
     * @return Outpatient_Class
     */
    public String getOutpatient_Class() {
        return Outpatient_Class;
    }

    /**
     * Outpatient_Class
     *
     * @param Outpatient_Class to set
     */
    public void setOutpatient_Class(String Outpatient_Class) {
        this.Outpatient_Class = Outpatient_Class;
    }

    /**
     * Admission_Date
     *
     * @return Admission_Date
     */
    public String getAdmission_Date() {
        return Admission_Date;
    }

    /**
     * Admission_Date
     *
     * @param Admission_Date to set
     */
    public void setAdmission_Date(String Admission_Date) {
        this.Admission_Date = Admission_Date;
    }

    /**
     * Discharge_Date
     *
     * @return Discharge_Date
     */
    public String getDischarge_Date() {
        return Discharge_Date;
    }

    /**
     * Discharge_Date
     *
     * @param Discharge_Date to set
     */
    public void setDischarge_Date(String Discharge_Date) {
        this.Discharge_Date = Discharge_Date;
    }

    /**
     * HealthInsurance_Information
     *
     * @return HealthInsurance_Information
     */
    public HealthInsuranceInformation[] getHealthInsurance_Information() {
        return HealthInsurance_Information;
    }

    /**
     * HealthInsurance_Information
     *
     * @param HealthInsurance_Information to set
     */
    public void setHealthInsurance_Information(HealthInsuranceInformation[] HealthInsurance_Information) {
        this.HealthInsurance_Information = HealthInsurance_Information;
    }

    /**
     * Care_Information
     *
     * @return Care_Information
     */
    public CareInformation getCare_Information() {
        return Care_Information;
    }

    /**
     * Care_Information
     *
     * @param Care_Information to set
     */
    public void setCare_Information(CareInformation Care_Information) {
        this.Care_Information = Care_Information;
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

    /**
     * Individual_Number
     *
     * @return Individual_Number
     */
    public IndividualNumber[] getIndividual_Number() {
        return Individual_Number;
    }

    /**
     * Individual_Number
     *
     * @param Individual_Number to set
     */
    public void setIndividual_Number(IndividualNumber[] Individual_Number) {
        this.Individual_Number = Individual_Number;
    }

    /**
     * Auto_Management_Information
     *
     * @return Auto_Management_Information
     */
    public AutoManagementInformation[] getAuto_Management_Information() {
        return Auto_Management_Information;
    }

    /**
     * Auto_Management_Information
     *
     * @param Auto_Management_Information to set
     */
    public void setAuto_Management_Information(AutoManagementInformation[] Auto_Management_Information) {
        this.Auto_Management_Information = Auto_Management_Information;
    }

    /**
     * Patient_Contra_Information
     *
     * @return Patient_Contra_Information
     */
    public PatientContraInformation getPatient_Contra_Information() {
        return Patient_Contra_Information;
    }

    /**
     * Patient_Contra_Information
     *
     * @param Patient_Contra_Information to set
     */
    public void setPatient_Contra_Information(PatientContraInformation Patient_Contra_Information) {
        this.Patient_Contra_Information = Patient_Contra_Information;
    }

    /**
     * CreateDate
     *
     * @return CreateDate
     */
    public String getCreateDate() {
        return CreateDate;
    }

    /**
     * CreateDate
     *
     * @param CreateDate to set
     */
    public void setCreateDate(String CreateDate) {
        this.CreateDate = CreateDate;
    }

    /**
     * UpdateDate
     *
     * @return UpdateDate
     */
    public String getUpdateDate() {
        return UpdateDate;
    }

    /**
     * UpdateDate
     *
     * @param UpdateDate to set
     */
    public void setUpdateDate(String UpdateDate) {
        this.UpdateDate = UpdateDate;
    }

    /**
     * UpdateTime
     *
     * @return UpdateTime
     */
    public String getUpdateTime() {
        return UpdateTime;
    }

    /**
     * UpdateTime
     *
     * @param UpdateTime to set
     */
    public void setUpdateTime(String UpdateTime) {
        this.UpdateTime = UpdateTime;
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
}
