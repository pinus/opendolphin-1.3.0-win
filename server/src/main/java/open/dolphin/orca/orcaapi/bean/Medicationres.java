package open.dolphin.orca.orcaapi.bean;

/**
 * medicationres.
 *
 * @author pns
 */
public class Medicationres {
    /**
     * 実施日 (例: 2014-07-15)
     */
    private String Information_Date;

    /**
     * 実施時間 (例: 15:02:19)
     */
    private String Information_Time;

    /**
     * 結果コード（ゼロ以外エラー） (例: 00)
     */
    private String Api_Result;

    /**
     * エラーメッセージ (例: 点数マスタ登録終了)
     */
    private String Api_Result_Message;

    /**
     *   (例: PatientInfo)
     */
    private String Reskey;

    /**
     * コード(自費、ユーザコメントコードetc) (例: 001700001)
     */
    private String Medication_Code;

    /**
     * コード漢字名称 (例: 朝夕　錠から)
     */
    private String Medication_Name;

    /**
     * コードカナ名称 (例:  )
     */
    private String Medication_Name_inKana;

    /**
     * 有効開始日 (例: 2014-07-01)
     */
    private String StartDate;

    /**
     * 有効終了日 (例: 9999-12-31)
     */
    private String EndDate;

    /**
     * 自費金額 (例:  )
     */
    private String Amount_Money;

    /**
     * 金額内容(0:税抜き、4:税込み) (例:  )
     */
    private String Content_Amount_Money;

    /**
     * 集計先（外来） (例:  )
     */
    private String Total_Destination_Out;

    /**
     * 集計先（入院） (例:  )
     */
    private String Total_Destination_In;

    /**
     * 自賠責集計先（外来） (例:  )
     */
    private String Liability_Insurance_Total_Destination_Out;

    /**
     * 自賠責集計先（入院） (例:  )
     */
    private String Liability_Insurance_Total_Destination_In;

    /**
     * 部位区分(0:その他、1:頭部、2:躯幹、3:四肢、5:胸部、6:腹部、7:脊髄、8:消化管) (例:  )
     */
    private String Location_Category;

    /**
     * ユーザコメント情報(繰り返し5) (例:  )
     */
    private CommentInformation[] Comment_Information;

    /**
     * 服用情報(繰り返し5) (例:  )
     */
    private MedicationInformation[] Medication_Information;

    /**
     * 用法コメント区分 (例: 2)
     */
    private String Medication_Category;

    /**
     * 単位コード(詳細については、「日医標準レセプトソフトデータベーステーブル定義書」を参照して下さい。) (例:  )
     */
    private String Unit_Code;

    /**
     * データ区分(0:その他、3:フィルム) (例:  )
     */
    private String Data_Category;

    /**
     * 商品名称 (例: 機材商品名称)
     */
    private String CommercialName;

    /**
     * 特定器材コード (例: 700590000)
     */
    private String Specific_Equipment_Code;

    /**
     * 部位選択式コメントコード (例:  )
     */
    private String Location_Comment_Code;

    /**
     * 実施日 (例: 2014-07-15)
     *
     * @return the Information_Date
     */
    public String getInformation_Date() {
        return Information_Date;
    }

    /**
     * 実施日 (例: 2014-07-15)
     *
     * @param Information_Date the Information_Date to set
     */
    public void setInformation_Date(String Information_Date) {
        this.Information_Date = Information_Date;
    }

    /**
     * 実施時間 (例: 15:02:19)
     *
     * @return the Information_Time
     */
    public String getInformation_Time() {
        return Information_Time;
    }

    /**
     * 実施時間 (例: 15:02:19)
     *
     * @param Information_Time the Information_Time to set
     */
    public void setInformation_Time(String Information_Time) {
        this.Information_Time = Information_Time;
    }

    /**
     * 結果コード（ゼロ以外エラー） (例: 00)
     *
     * @return the Api_Result
     */
    public String getApi_Result() {
        return Api_Result;
    }

    /**
     * 結果コード（ゼロ以外エラー） (例: 00)
     *
     * @param Api_Result the Api_Result to set
     */
    public void setApi_Result(String Api_Result) {
        this.Api_Result = Api_Result;
    }

    /**
     * エラーメッセージ (例: 点数マスタ登録終了)
     *
     * @return the Api_Result_Message
     */
    public String getApi_Result_Message() {
        return Api_Result_Message;
    }

    /**
     * エラーメッセージ (例: 点数マスタ登録終了)
     *
     * @param Api_Result_Message the Api_Result_Message to set
     */
    public void setApi_Result_Message(String Api_Result_Message) {
        this.Api_Result_Message = Api_Result_Message;
    }

    /**
     *   (例: PatientInfo)
     *
     * @return the Reskey
     */
    public String getReskey() {
        return Reskey;
    }

    /**
     *   (例: PatientInfo)
     *
     * @param Reskey the Reskey to set
     */
    public void setReskey(String Reskey) {
        this.Reskey = Reskey;
    }

    /**
     * コード(自費、ユーザコメントコードetc) (例: 001700001)
     *
     * @return the Medication_Code
     */
    public String getMedication_Code() {
        return Medication_Code;
    }

    /**
     * コード(自費、ユーザコメントコードetc) (例: 001700001)
     *
     * @param Medication_Code the Medication_Code to set
     */
    public void setMedication_Code(String Medication_Code) {
        this.Medication_Code = Medication_Code;
    }

    /**
     * コード漢字名称 (例: 朝夕　錠から)
     *
     * @return the Medication_Name
     */
    public String getMedication_Name() {
        return Medication_Name;
    }

    /**
     * コード漢字名称 (例: 朝夕　錠から)
     *
     * @param Medication_Name the Medication_Name to set
     */
    public void setMedication_Name(String Medication_Name) {
        this.Medication_Name = Medication_Name;
    }

    /**
     * コードカナ名称 (例:  )
     *
     * @return the Medication_Name_inKana
     */
    public String getMedication_Name_inKana() {
        return Medication_Name_inKana;
    }

    /**
     * コードカナ名称 (例:  )
     *
     * @param Medication_Name_inKana the Medication_Name_inKana to set
     */
    public void setMedication_Name_inKana(String Medication_Name_inKana) {
        this.Medication_Name_inKana = Medication_Name_inKana;
    }

    /**
     * 有効開始日 (例: 2014-07-01)
     *
     * @return the StartDate
     */
    public String getStartDate() {
        return StartDate;
    }

    /**
     * 有効開始日 (例: 2014-07-01)
     *
     * @param StartDate the StartDate to set
     */
    public void setStartDate(String StartDate) {
        this.StartDate = StartDate;
    }

    /**
     * 有効終了日 (例: 9999-12-31)
     *
     * @return the EndDate
     */
    public String getEndDate() {
        return EndDate;
    }

    /**
     * 有効終了日 (例: 9999-12-31)
     *
     * @param EndDate the EndDate to set
     */
    public void setEndDate(String EndDate) {
        this.EndDate = EndDate;
    }

    /**
     * 自費金額 (例:  )
     *
     * @return the Amount_Money
     */
    public String getAmount_Money() {
        return Amount_Money;
    }

    /**
     * 自費金額 (例:  )
     *
     * @param Amount_Money the Amount_Money to set
     */
    public void setAmount_Money(String Amount_Money) {
        this.Amount_Money = Amount_Money;
    }

    /**
     * 金額内容(0:税抜き、4:税込み) (例:  )
     *
     * @return the Content_Amount_Money
     */
    public String getContent_Amount_Money() {
        return Content_Amount_Money;
    }

    /**
     * 金額内容(0:税抜き、4:税込み) (例:  )
     *
     * @param Content_Amount_Money the Content_Amount_Money to set
     */
    public void setContent_Amount_Money(String Content_Amount_Money) {
        this.Content_Amount_Money = Content_Amount_Money;
    }

    /**
     * 集計先（外来） (例:  )
     *
     * @return the Total_Destination_Out
     */
    public String getTotal_Destination_Out() {
        return Total_Destination_Out;
    }

    /**
     * 集計先（外来） (例:  )
     *
     * @param Total_Destination_Out the Total_Destination_Out to set
     */
    public void setTotal_Destination_Out(String Total_Destination_Out) {
        this.Total_Destination_Out = Total_Destination_Out;
    }

    /**
     * 集計先（入院） (例:  )
     *
     * @return the Total_Destination_In
     */
    public String getTotal_Destination_In() {
        return Total_Destination_In;
    }

    /**
     * 集計先（入院） (例:  )
     *
     * @param Total_Destination_In the Total_Destination_In to set
     */
    public void setTotal_Destination_In(String Total_Destination_In) {
        this.Total_Destination_In = Total_Destination_In;
    }

    /**
     * 自賠責集計先（外来） (例:  )
     *
     * @return the Liability_Insurance_Total_Destination_Out
     */
    public String getLiability_Insurance_Total_Destination_Out() {
        return Liability_Insurance_Total_Destination_Out;
    }

    /**
     * 自賠責集計先（外来） (例:  )
     *
     * @param Liability_Insurance_Total_Destination_Out the Liability_Insurance_Total_Destination_Out to set
     */
    public void setLiability_Insurance_Total_Destination_Out(String Liability_Insurance_Total_Destination_Out) {
        this.Liability_Insurance_Total_Destination_Out = Liability_Insurance_Total_Destination_Out;
    }

    /**
     * 自賠責集計先（入院） (例:  )
     *
     * @return the Liability_Insurance_Total_Destination_In
     */
    public String getLiability_Insurance_Total_Destination_In() {
        return Liability_Insurance_Total_Destination_In;
    }

    /**
     * 自賠責集計先（入院） (例:  )
     *
     * @param Liability_Insurance_Total_Destination_In the Liability_Insurance_Total_Destination_In to set
     */
    public void setLiability_Insurance_Total_Destination_In(String Liability_Insurance_Total_Destination_In) {
        this.Liability_Insurance_Total_Destination_In = Liability_Insurance_Total_Destination_In;
    }

    /**
     * 部位区分(0:その他、1:頭部、2:躯幹、3:四肢、5:胸部、6:腹部、7:脊髄、8:消化管) (例:  )
     *
     * @return the Location_Category
     */
    public String getLocation_Category() {
        return Location_Category;
    }

    /**
     * 部位区分(0:その他、1:頭部、2:躯幹、3:四肢、5:胸部、6:腹部、7:脊髄、8:消化管) (例:  )
     *
     * @param Location_Category the Location_Category to set
     */
    public void setLocation_Category(String Location_Category) {
        this.Location_Category = Location_Category;
    }

    /**
     * ユーザコメント情報(繰り返し5) (例:  )
     *
     * @return the Comment_Information
     */
    public CommentInformation[] getComment_Information() {
        return Comment_Information;
    }

    /**
     * ユーザコメント情報(繰り返し5) (例:  )
     *
     * @param Comment_Information the Comment_Information to set
     */
    public void setComment_Information(CommentInformation[] Comment_Information) {
        this.Comment_Information = Comment_Information;
    }

    /**
     * 服用情報(繰り返し5) (例:  )
     *
     * @return the Medication_Information
     */
    public MedicationInformation[] getMedication_Information() {
        return Medication_Information;
    }

    /**
     * 服用情報(繰り返し5) (例:  )
     *
     * @param Medication_Information the Medication_Information to set
     */
    public void setMedication_Information(MedicationInformation[] Medication_Information) {
        this.Medication_Information = Medication_Information;
    }

    /**
     * 用法コメント区分 (例: 2)
     *
     * @return the Medication_Category
     */
    public String getMedication_Category() {
        return Medication_Category;
    }

    /**
     * 用法コメント区分 (例: 2)
     *
     * @param Medication_Category the Medication_Category to set
     */
    public void setMedication_Category(String Medication_Category) {
        this.Medication_Category = Medication_Category;
    }

    /**
     * 単位コード(詳細については、「日医標準レセプトソフトデータベーステーブル定義書」を参照して下さい。) (例:  )
     *
     * @return the Unit_Code
     */
    public String getUnit_Code() {
        return Unit_Code;
    }

    /**
     * 単位コード(詳細については、「日医標準レセプトソフトデータベーステーブル定義書」を参照して下さい。) (例:  )
     *
     * @param Unit_Code the Unit_Code to set
     */
    public void setUnit_Code(String Unit_Code) {
        this.Unit_Code = Unit_Code;
    }

    /**
     * データ区分(0:その他、3:フィルム) (例:  )
     *
     * @return the Data_Category
     */
    public String getData_Category() {
        return Data_Category;
    }

    /**
     * データ区分(0:その他、3:フィルム) (例:  )
     *
     * @param Data_Category the Data_Category to set
     */
    public void setData_Category(String Data_Category) {
        this.Data_Category = Data_Category;
    }

    /**
     * 商品名称 (例: 機材商品名称)
     *
     * @return the CommercialName
     */
    public String getCommercialName() {
        return CommercialName;
    }

    /**
     * 商品名称 (例: 機材商品名称)
     *
     * @param CommercialName the CommercialName to set
     */
    public void setCommercialName(String CommercialName) {
        this.CommercialName = CommercialName;
    }

    /**
     * 特定器材コード (例: 700590000)
     *
     * @return the Specific_Equipment_Code
     */
    public String getSpecific_Equipment_Code() {
        return Specific_Equipment_Code;
    }

    /**
     * 特定器材コード (例: 700590000)
     *
     * @param Specific_Equipment_Code the Specific_Equipment_Code to set
     */
    public void setSpecific_Equipment_Code(String Specific_Equipment_Code) {
        this.Specific_Equipment_Code = Specific_Equipment_Code;
    }

    /**
     * Location_Comment_Code
     *
     * @return Location_Comment_Code
     */
    public String getLocation_Comment_Code() {
        return Location_Comment_Code;
    }

    /**
     * Location_Comment_Code
     *
     * @param Location_Comment_Code to set
     */
    public void setLocation_Comment_Code(String Location_Comment_Code) {
        this.Location_Comment_Code = Location_Comment_Code;
    }
}