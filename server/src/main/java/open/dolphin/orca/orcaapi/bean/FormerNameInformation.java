package open.dolphin.orca.orcaapi.bean;

/**
 * Former_Name_Information. 旧姓履歴情報
 * @author pns
 */
public class FormerNameInformation {
    /**
     * 変更年月日 (例: 2021-11-12)
     */
    private String ChangeDate;

    /**
     * 患者漢字氏名 (例: 日医　無資格)
     */
    private String WholeName;

    /**
     * 患者カナ氏名 (例: ニチイ　ムシカク)
     */
    private String WholeName_inKana;

    /**
     * 通称名 (例: 無ちゃん)
     */
    private String NickName;

    /**
     * ChangeDate
     *
     * @return ChangeDate
     */
    public String getChangeDate() {
        return ChangeDate;
    }

    /**
     * ChangeDate
     *
     * @param ChangeDate to set
     */
    public void setChangeDate(String ChangeDate) {
        this.ChangeDate = ChangeDate;
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
}
