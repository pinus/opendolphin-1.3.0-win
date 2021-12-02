package open.dolphin.orca.orcaapi.bean;

/**
 * patientlst8req.
 * @author pns
 */
public class Patientlst8req {
    /**
     * リクエスト番号 (例: 01)
     */
    private String Request_Number;

    /**
     * 患者番号 (例: 00001)
     */
    private String Patient_ID;

    /**
     * Request_Number
     *
     * @return Request_Number
     */
    public String getRequest_Number() {
        return Request_Number;
    }

    /**
     * Request_Number
     *
     * @param Request_Number to set
     */
    public void setRequest_Number(String Request_Number) {
        this.Request_Number = Request_Number;
    }

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
}