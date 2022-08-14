package open.dolphin.orca.orcadao.bean;

public class OnshiKenshin {
    /**
     * 検診実施年月日.
     */
    private String isoDate;

    /**
     * 明細番号.
     */
    private int rennum;

    /**
     * 項目コード.
     */
    private String komokucd;

    /**
     * 項目名.
     */
    private String komokuname;

    /**
     * データ型.
     */
    private String dataType;

    /**
     * データ値.
     */
    private String dataValue;

    /**
     * 単位.
     */
    private String dataTani;

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
     * komokucd
     *
     * @return komokucd
     */
    public String getKomokucd() {
        return komokucd;
    }

    /**
     * komokucd
     *
     * @param komokucd to set
     */
    public void setKomokucd(String komokucd) {
        this.komokucd = komokucd;
    }

    /**
     * komokuname
     *
     * @return komokuname
     */
    public String getKomokuname() {
        return komokuname;
    }

    /**
     * komokuname
     *
     * @param komokuname to set
     */
    public void setKomokuname(String komokuname) {
        this.komokuname = komokuname;
    }

    /**
     * dataType
     *
     * @return dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * dataType
     *
     * @param dataType to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * dataValue
     *
     * @return dataValue
     */
    public String getDataValue() {
        return dataValue;
    }

    /**
     * dataValue
     *
     * @param dataValue to set
     */
    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    /**
     * dataTani
     *
     * @return dataTani
     */
    public String getDataTani() {
        return dataTani;
    }

    /**
     * dataTani
     *
     * @param dataTani to set
     */
    public void setDataTani(String dataTani) {
        this.dataTani = dataTani;
    }
}
