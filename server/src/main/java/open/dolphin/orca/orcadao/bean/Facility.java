package open.dolphin.orca.orcadao.bean;

/**
 * 病院名, 薬局名を保持する bean.
 */
public class Facility {
    /**
     * chozai_seqnum or shoho_seqnum.
     */
    int id;

    /**
     * 自施設かどうか.
     */
    boolean isMe;

    /**
     * 施設名.
     */
    String facilityName = "不明";

    /**
     * 施設コード.
     */
    String facilityCode = "0000000000";

    /**
     * id
     *
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * id
     *
     * @param id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * isMe
     *
     * @return isMe
     */
    public boolean isMe() {
        return isMe;
    }

    /**
     * isMe
     *
     * @param isMe to set
     */
    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }

    /**
     * facilityName
     *
     * @return facilityName
     */
    public String getFacilityName() {
        return facilityName;
    }

    /**
     * facilityName
     *
     * @param facilityName to set
     */
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    /**
     * facilityCode
     *
     * @return facilityCode
     */
    public String getFacilityCode() {
        return facilityCode;
    }

    /**
     * facilityCode
     *
     * @param facilityCode to set
     */
    public void setFacilityCode(String facilityCode) {
        this.facilityCode = facilityCode;
    }

    /**
     * 施設が医科かどうかを返す.
     * @return true or false
     */
    public boolean isMedical () { return facilityCode.substring(2,3).equals("1"); }

    /**
     * 施設が歯科かどうかを返す.
     * @return true or false
     */
    public boolean isDental () { return facilityCode.substring(2,3).equals("2"); }

    /**
     * 施設が薬局かどうかを返す.
     * @return true or false
     */
    public boolean isPharmacy () { return facilityCode.substring(2,3).equals("4"); }

    /**
     * 訪問看護施設かどうかを返す.
     * @return true or false
     */
    public boolean isHomeCare () { return facilityCode.substring(2,3).equals("6"); }
}
