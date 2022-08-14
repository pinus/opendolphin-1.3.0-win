package open.dolphin.orca.orcadao.bean;

/**
 * TBL_ONSHI_YAKUZAI_SUB 関連.
 */
public class OnshiYakuzai {
    /**
     * 処方箋発行日.
     */
    private String isoDate;

    /**
     * 処方箋発行日.
     */
    private String shohoHakkoymd;

    /**
     * 明細番号.
     */
    private int rennum;

    /**
     * 用法コード.
     */
    private String yohocd;

    /**
     * 用法名称.
     */
    private String yohoname;

    /**
     * 特別指示.
     */
    private String shiji;

    /**
     * 医薬品コード.
     */
    private String srycd;

    /**
     * 薬剤名.
     */
    private String yakuzainame;

    /**
     * 単位名.
     */
    private String taniname;

    /**
     * 使用量.
     */
    private int suryo;

    /**
     * 1回用量.
     */
    private int yoryo;

    /**
     * 回数
     */
    private int kaisu;

    /**
     * 調剤期間毎連番.
     */
    private int chozaiSeqnum;

    /**
     * 処方期間毎連番.
     */
    private int shohoSeqnum;

    /**
     * isoDate
     *
     * @return isoDate
     */
    public String getIsoDate() {
        return isoDate;
    }

    /**
     * isoDate
     *
     * @param isoDate to set
     */
    public void setIsoDate(String isoDate) {
        this.isoDate = isoDate;
    }

    /**
     * shohoHakkoymd
     *
     * @return shohoHakkoymd
     */
    public String getShohoHakkoymd() {
        return shohoHakkoymd;
    }

    /**
     * shohoHakkoymd
     *
     * @param shohoHakkoymd to set
     */
    public void setShohoHakkoymd(String shohoHakkoymd) {
        this.shohoHakkoymd = shohoHakkoymd;
    }

    /**
     * rennum
     *
     * @return rennum
     */
    public int getRennum() {
        return rennum;
    }

    /**
     * rennum
     *
     * @param rennum to set
     */
    public void setRennum(int rennum) {
        this.rennum = rennum;
    }

    /**
     * yohocd
     *
     * @return yohocd
     */
    public String getYohocd() {
        return yohocd;
    }

    /**
     * yohocd
     *
     * @param yohocd to set
     */
    public void setYohocd(String yohocd) {
        this.yohocd = yohocd;
    }

    /**
     * yohoname
     *
     * @return yohoname
     */
    public String getYohoname() {
        return yohoname;
    }

    /**
     * yohoname
     *
     * @param yohoname to set
     */
    public void setYohoname(String yohoname) {
        this.yohoname = yohoname;
    }

    /**
     * shiji
     *
     * @return shiji
     */
    public String getShiji() {
        return shiji;
    }

    /**
     * shiji
     *
     * @param shiji to set
     */
    public void setShiji(String shiji) {
        this.shiji = shiji;
    }

    /**
     * srycd
     *
     * @return srycd
     */
    public String getSrycd() {
        return srycd;
    }

    /**
     * srycd
     *
     * @param srycd to set
     */
    public void setSrycd(String srycd) {
        this.srycd = srycd;
    }

    /**
     * yakuzainame
     *
     * @return yakuzainame
     */
    public String getYakuzainame() {
        return yakuzainame;
    }

    /**
     * yakuzainame
     *
     * @param yakuzainame to set
     */
    public void setYakuzainame(String yakuzainame) {
        this.yakuzainame = yakuzainame;
    }

    /**
     * taniname
     *
     * @return taniname
     */
    public String getTaniname() {
        return taniname;
    }

    /**
     * taniname
     *
     * @param taniname to set
     */
    public void setTaniname(String taniname) {
        this.taniname = taniname;
    }

    /**
     * suryo
     *
     * @return suryo
     */
    public int getSuryo() {
        return suryo;
    }

    /**
     * suryo
     *
     * @param suryo to set
     */
    public void setSuryo(int suryo) {
        this.suryo = suryo;
    }

    /**
     * yoryo
     *
     * @return yoryo
     */
    public int getYoryo() {
        return yoryo;
    }

    /**
     * yoryo
     *
     * @param yoryo to set
     */
    public void setYoryo(int yoryo) {
        this.yoryo = yoryo;
    }

    /**
     * kaisu
     *
     * @return kaisu
     */
    public int getKaisu() {
        return kaisu;
    }

    /**
     * kaisu
     *
     * @param kaisu to set
     */
    public void setKaisu(int kaisu) {
        this.kaisu = kaisu;
    }

    /**
     * chozaiSeqnum
     *
     * @return chozaiSeqnum
     */
    public int getChozaiSeqnum() {
        return chozaiSeqnum;
    }

    /**
     * chozaiSeqnum
     *
     * @param chozaiSeqnum to set
     */
    public void setChozaiSeqnum(int chozaiSeqnum) {
        this.chozaiSeqnum = chozaiSeqnum;
    }

    /**
     * shohoSeqnum
     *
     * @return shohoSeqnum
     */
    public int getShohoSeqnum() {
        return shohoSeqnum;
    }

    /**
     * shohoSeqnum
     *
     * @param shohoSeqnum to set
     */
    public void setShohoSeqnum(int shohoSeqnum) {
        this.shohoSeqnum = shohoSeqnum;
    }
}
