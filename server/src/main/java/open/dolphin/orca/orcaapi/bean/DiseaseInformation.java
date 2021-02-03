package open.dolphin.orca.orcaapi.bean;

/**
 * Disease_Information. 病名情報(繰り返し50)
 *
 * @author pns
 */
public class DiseaseInformation {
    /**
     * 診療科コード　※１(01:内科) (例: 01)
     */
    private String Department_Code;

    /**
     * 一連病名コード (例: 8830052)
     */
    private String Disease_Code;

    /**
     * 入外区分(O:外来、I:入院)(半角大文字) (例:  )
     */
    private String Disease_InOut;

    /**
     * 一連病名名称(全角40文字まで) (例: ACバイパス術後機械的合併症)
     */
    private String Disease_Name;

    /**
     * 単独病名情報(繰り返し6) (例:  )
     */
    private DiseaseSingle[] Disease_Single;

    /**
     * 病名補足コメント情報 (例:  )
     */
    private DiseaseSupplement Disease_Supplement;

    /**
     * 主病フラグ（PD:主病名） (例:  )
     */
    private String Disease_Category;

    /**
     * 疑いフラグ (例: S)
     */
    private String Disease_SuspectedFlag;

    /**
     * 病名開始日 (例: 2010-11-23)
     */
    private String Disease_StartDate;

    /**
     * 転帰日 (例: 2010-11-24)
     */
    private String Disease_EndDate;

    /**
     * 転帰区分 (例: D)
     * <table>
     * <tr><td>O</td><td>削除</td><td>疑いフラグ、開始日、病名、補足コメント名称、転帰日、入外区分、保険組合せ番号等完全一致したものに対し、削除フラグを設定する。</td></tr>
     * <tr><td>D</td><td>死亡</td><td>２（死亡）</td></tr>
     * <tr><td>F</td><td>完治</td><td>１（治ゆ）</td></tr>
     * <tr><td>N</td><td>不変</td><td>３（中止）</td></tr>
     * <tr><td>R</td><td>軽快</td><td>３（中止）</td></tr>
     * <tr><td>S</td><td>後遺症残</td><td>３（中止）</td></tr>
     * <tr><td>U</td><td>不明</td><td>３（中止）</td></tr>
     * <tr><td>W</td><td>悪化</td><td>３（中止）</td></tr>
     * <tr><td>上記以外</td><td></td><td>１（治ゆ）</td></tr>
     * </table>
     */
    private String Disease_OutCome;

    /**
     * 保険区分(１：医保(自費)以外、１以外：医保(自費)) (例: 1)
     */
    private String Disease_Insurance_Class;

    /**
     * 補足コメント情報 (例:  )
     */
    private String Disease_Supplement_Name;

    /**
     * 補足コメントコード情報(繰り返し　３) (例:  )
     */
    private DiseaseSupplementSingle[] Disease_Supplement_Single;

    /**
     * カルテ病名 (例:  )
     */
    private String Disease_Karte_Name;

    /**
     * 疾患区分（０３：皮膚科特定疾患指導管理料（１）、０４：皮膚科特定疾患指導管理料（２）、０５：:特定疾患療養管理料、０７：てんかん指導料、０８：特定疾患療養管理料又はてんかん指導料、０９：難病外来指導管理料） (例: Auto)
     */
    private String Disease_Class;

    /**
     * 保険組合せ番号 (例: 0003)
     */
    private String Insurance_Combination_Number;

    /**
     * レセプト表示（１：表示しない） (例: 1)
     */
    private String Disease_Receipt_Print;

    /**
     * レセプト表示期間（００～９９） (例: 99)
     */
    private String Disease_Receipt_Print_Period;

    /**
     * 保険病名（１：保険病名） (例: 1)
     */
    private String Insurance_Disease;

    /**
     * 退院証明書（空白または０：記載しない、１：記載する） (例: 0)
     */
    private String Discharge_Certificate;

    /**
     * 原疾患区分（０１：原疾患ア、０２：原疾患イ、０３：原疾患ウ、０４：原疾患エ、０５：原疾患オ） (例: 02)
     */
    private String Main_Disease_Class;

    /**
     * 合併症区分（０１：アの合併症、０２：イの合併症、０３：ウの合併症、０４：エの合併症、０５：オの合併 症） (例: 03)
     */
    private String Sub_Disease_Class;

    /**
     * 分類番号（主） (例:  )
     */
    private String Classification_Number_Mater;

    /**
     * 分類番号（従） (例:  )
     */
    private String Classification_Number_Servant;

    /**
     * 急性フラグ（A：急性） (例:  )
     */
    private String Disease_AcuteFlag;

    /**
     * 第三者行為区分(True:対象の保険組合せが第三者行為である) (例: True)
     */
    private String Third_Party_Mark;

    /**
     * 診療科コード　※１(01:内科) (例: 01)
     *
     * @return the Department_Code
     */
    public String getDepartment_Code() {
        return Department_Code;
    }

    /**
     * 診療科コード　※１(01:内科) (例: 01)
     *
     * @param Department_Code the Department_Code to set
     */
    public void setDepartment_Code(String Department_Code) {
        this.Department_Code = Department_Code;
    }

    /**
     * 一連病名コード (例: 8830052)
     *
     * @return the Disease_Code
     */
    public String getDisease_Code() {
        return Disease_Code;
    }

    /**
     * 一連病名コード (例: 8830052)
     *
     * @param Disease_Code the Disease_Code to set
     */
    public void setDisease_Code(String Disease_Code) {
        this.Disease_Code = Disease_Code;
    }

    /**
     * 入外区分(O:外来、I:入院)(半角大文字) (例:  )
     *
     * @return the Disease_InOut
     */
    public String getDisease_InOut() {
        return Disease_InOut;
    }

    /**
     * 入外区分(O:外来、I:入院)(半角大文字) (例:  )
     *
     * @param Disease_InOut the Disease_InOut to set
     */
    public void setDisease_InOut(String Disease_InOut) {
        this.Disease_InOut = Disease_InOut;
    }

    /**
     * 一連病名名称(全角40文字まで) (例: ACバイパス術後機械的合併症)
     *
     * @return the Disease_Name
     */
    public String getDisease_Name() {
        return Disease_Name;
    }

    /**
     * 一連病名名称(全角40文字まで) (例: ACバイパス術後機械的合併症)
     *
     * @param Disease_Name the Disease_Name to set
     */
    public void setDisease_Name(String Disease_Name) {
        this.Disease_Name = Disease_Name;
    }

    /**
     * 単独病名情報(繰り返し6) (例:  )
     *
     * @return the Disease_Single
     */
    public DiseaseSingle[] getDisease_Single() {
        return Disease_Single;
    }

    /**
     * 単独病名情報(繰り返し6) (例:  )
     *
     * @param Disease_Single the Disease_Single to set
     */
    public void setDisease_Single(DiseaseSingle[] Disease_Single) {
        this.Disease_Single = Disease_Single;
    }

    /**
     * 病名補足コメント情報 (例:  )
     *
     * @return the Disease_Supplement
     */
    public DiseaseSupplement getDisease_Supplement() {
        return Disease_Supplement;
    }

    /**
     * 病名補足コメント情報 (例:  )
     *
     * @param Disease_Supplement the Disease_Supplement to set
     */
    public void setDisease_Supplement(DiseaseSupplement Disease_Supplement) {
        this.Disease_Supplement = Disease_Supplement;
    }

    /**
     * 主病フラグ（PD:主病名） (例:  )
     *
     * @return the Disease_Category
     */
    public String getDisease_Category() {
        return Disease_Category;
    }

    /**
     * 主病フラグ（PD:主病名） (例:  )
     *
     * @param Disease_Category the Disease_Category to set
     */
    public void setDisease_Category(String Disease_Category) {
        this.Disease_Category = Disease_Category;
    }

    /**
     * 疑いフラグ (例: S)
     *
     * @return the Disease_SuspectedFlag
     */
    public String getDisease_SuspectedFlag() {
        return Disease_SuspectedFlag;
    }

    /**
     * 疑いフラグ (例: S)
     *
     * @param Disease_SuspectedFlag the Disease_SuspectedFlag to set
     */
    public void setDisease_SuspectedFlag(String Disease_SuspectedFlag) {
        this.Disease_SuspectedFlag = Disease_SuspectedFlag;
    }

    /**
     * 病名開始日 (例: 2010-11-23)
     *
     * @return the Disease_StartDate
     */
    public String getDisease_StartDate() {
        return Disease_StartDate;
    }

    /**
     * 病名開始日 (例: 2010-11-23)
     *
     * @param Disease_StartDate the Disease_StartDate to set
     */
    public void setDisease_StartDate(String Disease_StartDate) {
        this.Disease_StartDate = Disease_StartDate;
    }

    /**
     * 転帰日 (例: 2010-11-24)
     *
     * @return the Disease_EndDate
     */
    public String getDisease_EndDate() {
        return Disease_EndDate;
    }

    /**
     * 転帰日 (例: 2010-11-24)
     *
     * @param Disease_EndDate the Disease_EndDate to set
     */
    public void setDisease_EndDate(String Disease_EndDate) {
        this.Disease_EndDate = Disease_EndDate;
    }

    /**
     * 転帰区分 (例: D)
     * <table>
     * <tr><td>O</td><td>削除</td><td>疑いフラグ、開始日、病名、補足コメント名称、転帰日、入外区分、保険組合せ番号等完全一致したものに対し、削除フラグを設定する。</td></tr>
     * <tr><td>D</td><td>死亡</td><td>２（死亡）</td></tr>
     * <tr><td>F</td><td>完治</td><td>１（治ゆ）</td></tr>
     * <tr><td>N</td><td>不変</td><td>３（中止）</td></tr>
     * <tr><td>R</td><td>軽快</td><td>３（中止）</td></tr>
     * <tr><td>S</td><td>後遺症残</td><td>３（中止）</td></tr>
     * <tr><td>U</td><td>不明</td><td>３（中止）</td></tr>
     * <tr><td>W</td><td>悪化</td><td>３（中止）</td></tr>
     * <tr><td>上記以外</td><td></td><td>１（治ゆ）</td></tr>
     * </table>
     *
     * @return the Disease_OutCome
     */
    public String getDisease_OutCome() {
        return Disease_OutCome;
    }

    /**
     * 転帰区分 (例: D)
     * <table>
     * <tr><td>O</td><td>削除</td><td>疑いフラグ、開始日、病名、補足コメント名称、転帰日、入外区分、保険組合せ番号等完全一致したものに対し、削除フラグを設定する。</td></tr>
     * <tr><td>D</td><td>死亡</td><td>２（死亡）</td></tr>
     * <tr><td>F</td><td>完治</td><td>１（治ゆ）</td></tr>
     * <tr><td>N</td><td>不変</td><td>３（中止）</td></tr>
     * <tr><td>R</td><td>軽快</td><td>３（中止）</td></tr>
     * <tr><td>S</td><td>後遺症残</td><td>３（中止）</td></tr>
     * <tr><td>U</td><td>不明</td><td>３（中止）</td></tr>
     * <tr><td>W</td><td>悪化</td><td>３（中止）</td></tr>
     * <tr><td>上記以外</td><td></td><td>１（治ゆ）</td></tr>
     * </table>
     *
     * @param Disease_OutCome the Disease_OutCome to set
     */
    public void setDisease_OutCome(String Disease_OutCome) {
        this.Disease_OutCome = Disease_OutCome;
    }

    /**
     * 保険区分(１：医保(自費)以外、１以外：医保(自費)) (例: 1)
     *
     * @return the Disease_Insurance_Class
     */
    public String getDisease_Insurance_Class() {
        return Disease_Insurance_Class;
    }

    /**
     * 保険区分(１：医保(自費)以外、１以外：医保(自費)) (例: 1)
     *
     * @param Disease_Insurance_Class the Disease_Insurance_Class to set
     */
    public void setDisease_Insurance_Class(String Disease_Insurance_Class) {
        this.Disease_Insurance_Class = Disease_Insurance_Class;
    }

    /**
     * 補足コメント情報 (例:  )
     *
     * @return the Disease_Supplement_Name
     */
    public String getDisease_Supplement_Name() {
        return Disease_Supplement_Name;
    }

    /**
     * 補足コメント情報 (例:  )
     *
     * @param Disease_Supplement_Name the Disease_Supplement_Name to set
     */
    public void setDisease_Supplement_Name(String Disease_Supplement_Name) {
        this.Disease_Supplement_Name = Disease_Supplement_Name;
    }

    /**
     * 補足コメントコード情報(繰り返し　３) (例:  )
     *
     * @return the Disease_Supplement_Single
     */
    public DiseaseSupplementSingle[] getDisease_Supplement_Single() {
        return Disease_Supplement_Single;
    }

    /**
     * 補足コメントコード情報(繰り返し　３) (例:  )
     *
     * @param Disease_Supplement_Single the Disease_Supplement_Single to set
     */
    public void setDisease_Supplement_Single(DiseaseSupplementSingle[] Disease_Supplement_Single) {
        this.Disease_Supplement_Single = Disease_Supplement_Single;
    }

    /**
     * カルテ病名 (例:  )
     *
     * @return the Disease_Karte_Name
     */
    public String getDisease_Karte_Name() {
        return Disease_Karte_Name;
    }

    /**
     * カルテ病名 (例:  )
     *
     * @param Disease_Karte_Name the Disease_Karte_Name to set
     */
    public void setDisease_Karte_Name(String Disease_Karte_Name) {
        this.Disease_Karte_Name = Disease_Karte_Name;
    }

    /**
     * 疾患区分（０３：皮膚科特定疾患指導管理料（１）、０４：皮膚科特定疾患指導管理料（２）、０５：:特定疾患療養管理料、０７：てんかん指導料、０８：特定疾患療養管理料又はてんかん指導料、０９：難病外来指導管理料） (例: Auto)
     *
     * @return the Disease_Class
     */
    public String getDisease_Class() {
        return Disease_Class;
    }

    /**
     * 疾患区分（０３：皮膚科特定疾患指導管理料（１）、０４：皮膚科特定疾患指導管理料（２）、０５：:特定疾患療養管理料、０７：てんかん指導料、０８：特定疾患療養管理料又はてんかん指導料、０９：難病外来指導管理料） (例: Auto)
     *
     * @param Disease_Class the Disease_Class to set
     */
    public void setDisease_Class(String Disease_Class) {
        this.Disease_Class = Disease_Class;
    }

    /**
     * 保険組合せ番号 (例: 0003)
     *
     * @return the Insurance_Combination_Number
     */
    public String getInsurance_Combination_Number() {
        return Insurance_Combination_Number;
    }

    /**
     * 保険組合せ番号 (例: 0003)
     *
     * @param Insurance_Combination_Number the Insurance_Combination_Number to set
     */
    public void setInsurance_Combination_Number(String Insurance_Combination_Number) {
        this.Insurance_Combination_Number = Insurance_Combination_Number;
    }

    /**
     * レセプト表示（１：表示しない） (例: 1)
     *
     * @return the Disease_Receipt_Print
     */
    public String getDisease_Receipt_Print() {
        return Disease_Receipt_Print;
    }

    /**
     * レセプト表示（１：表示しない） (例: 1)
     *
     * @param Disease_Receipt_Print the Disease_Receipt_Print to set
     */
    public void setDisease_Receipt_Print(String Disease_Receipt_Print) {
        this.Disease_Receipt_Print = Disease_Receipt_Print;
    }

    /**
     * レセプト表示期間（００～９９） (例: 99)
     *
     * @return the Disease_Receipt_Print_Period
     */
    public String getDisease_Receipt_Print_Period() {
        return Disease_Receipt_Print_Period;
    }

    /**
     * レセプト表示期間（００～９９） (例: 99)
     *
     * @param Disease_Receipt_Print_Period the Disease_Receipt_Print_Period to set
     */
    public void setDisease_Receipt_Print_Period(String Disease_Receipt_Print_Period) {
        this.Disease_Receipt_Print_Period = Disease_Receipt_Print_Period;
    }

    /**
     * 保険病名（１：保険病名） (例: 1)
     *
     * @return the Insurance_Disease
     */
    public String getInsurance_Disease() {
        return Insurance_Disease;
    }

    /**
     * 保険病名（１：保険病名） (例: 1)
     *
     * @param Insurance_Disease the Insurance_Disease to set
     */
    public void setInsurance_Disease(String Insurance_Disease) {
        this.Insurance_Disease = Insurance_Disease;
    }

    /**
     * 退院証明書（空白または０：記載しない、１：記載する） (例: 0)
     *
     * @return the Discharge_Certificate
     */
    public String getDischarge_Certificate() {
        return Discharge_Certificate;
    }

    /**
     * 退院証明書（空白または０：記載しない、１：記載する） (例: 0)
     *
     * @param Discharge_Certificate the Discharge_Certificate to set
     */
    public void setDischarge_Certificate(String Discharge_Certificate) {
        this.Discharge_Certificate = Discharge_Certificate;
    }

    /**
     * 原疾患区分（０１：原疾患ア、０２：原疾患イ、０３：原疾患ウ、０４：原疾患エ、０５：原疾患オ） (例: 02)
     *
     * @return the Main_Disease_Class
     */
    public String getMain_Disease_Class() {
        return Main_Disease_Class;
    }

    /**
     * 原疾患区分（０１：原疾患ア、０２：原疾患イ、０３：原疾患ウ、０４：原疾患エ、０５：原疾患オ） (例: 02)
     *
     * @param Main_Disease_Class the Main_Disease_Class to set
     */
    public void setMain_Disease_Class(String Main_Disease_Class) {
        this.Main_Disease_Class = Main_Disease_Class;
    }

    /**
     * 合併症区分（０１：アの合併症、０２：イの合併症、０３：ウの合併症、０４：エの合併症、０５：オの合併 症） (例: 03)
     *
     * @return the Sub_Disease_Class
     */
    public String getSub_Disease_Class() {
        return Sub_Disease_Class;
    }

    /**
     * 合併症区分（０１：アの合併症、０２：イの合併症、０３：ウの合併症、０４：エの合併症、０５：オの合併 症） (例: 03)
     *
     * @param Sub_Disease_Class the Sub_Disease_Class to set
     */
    public void setSub_Disease_Class(String Sub_Disease_Class) {
        this.Sub_Disease_Class = Sub_Disease_Class;
    }

    /**
     * 分類番号（主） (例:  )
     *
     * @return the Classification_Number_Mater
     */
    public String getClassification_Number_Mater() {
        return Classification_Number_Mater;
    }

    /**
     * 分類番号（主） (例:  )
     *
     * @param Classification_Number_Mater the Classification_Number_Mater to set
     */
    public void setClassification_Number_Mater(String Classification_Number_Mater) {
        this.Classification_Number_Mater = Classification_Number_Mater;
    }

    /**
     * 分類番号（従） (例:  )
     *
     * @return the Classification_Number_Servant
     */
    public String getClassification_Number_Servant() {
        return Classification_Number_Servant;
    }

    /**
     * 分類番号（従） (例:  )
     *
     * @param Classification_Number_Servant the Classification_Number_Servant to set
     */
    public void setClassification_Number_Servant(String Classification_Number_Servant) {
        this.Classification_Number_Servant = Classification_Number_Servant;
    }

    /**
     * Disease_AcuteFlag
     *
     * @return Disease_AcuteFlag
     */
    public String getDisease_AcuteFlag() {
        return Disease_AcuteFlag;
    }

    /**
     * Disease_AcuteFlag
     *
     * @param Disease_AcuteFlag to set
     */
    public void setDisease_AcuteFlag(String Disease_AcuteFlag) {
        this.Disease_AcuteFlag = Disease_AcuteFlag;
    }

    /**
     * Third_Party_Mark
     *
     * @return Third_Party_Mark
     */
    public String getThird_Party_Mark() {
        return Third_Party_Mark;
    }

    /**
     * Third_Party_Mark
     *
     * @param Third_Party_Mark to set
     */
    public void setThird_Party_Mark(String Third_Party_Mark) {
        this.Third_Party_Mark = Third_Party_Mark;
    }
}