package open.dolphin.infomodel;

import open.dolphin.util.ModelUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.StringTokenizer;

/**
 * PatientVisitModel.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Entity
@Table(name = "d_patient_visit")
public class PatientVisitModel extends InfoModel {
    private static final long serialVersionUID = 7049490761810599245L;

    public static final DataFlavor PVT_FLAVOR =
            new DataFlavor(open.dolphin.infomodel.PatientVisitModel.class, "Patient Visit");

    public static final DataFlavor[] flavors = {PVT_FLAVOR};

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /**
     * 患者
     */
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientModel patient;

    /**
     * 施設ID
     */
    @Column(nullable = false)
    private String facilityId;

    /**
     * 受付リスト上の番号
     */
    @Transient
    private int number;

    /**
     * 来院時間
     */
    @Column(nullable = false)
    private String pvtDate;

    /**
     * 予約
     */
    @Transient
    private String appointment;

    /**
     * 診療科
     */
    private String department;

    /**
     * 終了フラグ
     */
    private int status;

    /**
     * 健康保険GUID 2006-05-01
     */
    private String insuranceUid;

    /**
     * 今まで付いた病名の総数
     */
    @Column(nullable = false)
    private int byomeiCount;

    /**
     * 今日付いた病名の数
     */
    @Column(nullable = false)
    private int byomeiCountToday;

    /**
     * カルテ記載があるかどうか
     */
    @Transient
    private boolean karteEmpty;

    /**
     * ORCA から送られる「診療内容」
     */
    private String memo;

    public boolean isShoshin() {
        return (byomeiCount == byomeiCountToday);
    }

    public boolean hasByomei() {
        return (byomeiCount != 0);
    }

    public boolean isKarteEmpty() {
        return karteEmpty;
    }

    public void setKarteEmpty(boolean k) {
        karteEmpty = k;
    }

    public int getByomeiCount() {
        return byomeiCount;
    }

    public void setByomeiCount(int bc) {
        byomeiCount = bc;
    }

    public int getByomeiCountToday() {
        return byomeiCountToday;
    }

    public void setByomeiCountToday(int bct) {
        byomeiCountToday = bct;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String val) {
        memo = val;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PatientModel getPatient() {
        return patient;
    }

    public void setPatient(PatientModel patientModel) {
        this.patient = patientModel;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getPvtDate() { return pvtDate; }

    public void setPvtDate(String time) {
        this.pvtDate = time;
    }

    public String getPvtDateTrimTime() {
        return ModelUtils.trimTime(pvtDate);
    }

    public String getPvtDateTrimDate() {
        return ModelUtils.trimDate(pvtDate);
    }

    public String getAppointment() {
        return appointment;
    }

    public void setAppointment(String appointment) {
        this.appointment = appointment;
    }

    public String getDepartment() {
        // 1.3 までの暫定
        String[] tokens = tokenizeDept(department);
        return tokens[0];
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartmentCode() {
        // 1.3 までの暫定
        String[] tokens = tokenizeDept(department);
        return tokens[1];
    }

    public String getAssignedDoctorName() {
        // 1.3 までの暫定
        String[] tokens = tokenizeDept(department);
        return tokens[2];
    }

    public String getAssignedDoctorId() {
        // 1.3 までの暫定
        String[] tokens = tokenizeDept(department);
        return tokens[3];
    }

    public String getJmariCode() {
        // 1.3 までの暫定
        String[] tokens = tokenizeDept(department);
        return tokens[4];
    }

    public String getDeptNoTokenize() {
        return department;
    }

    private String[] tokenizeDept(String dept) {

        // 診療科名、コード、担当医名、担当医コード、JMARI コード
        // を格納する配列を生成する
        String[] ret = new String[5];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = null;
        }

        if (dept != null) {
            int index = 0;
            StringTokenizer st = new StringTokenizer(dept, ",");
            while (st.hasMoreTokens()) {
                ret[index++] = st.nextToken();
            }
        }
        return ret;
    }

    public int getState() {
        return status;
    }

    public void setState(int state) {
        this.status = state;
    }

    public String getPatientId() {
        return getPatient().getPatientId();
    }

    public String getPatientName() {
        return getPatient().getFullName();
    }

    public String getPatientGenderDesc() {
        return ModelUtils.getGenderDesc(getPatient().getGender());
    }

    public String getPatientAgeBirthday() {
        return ModelUtils.getAgeBirthday(getPatient().getBirthday());
    }

    public String getPatientBirthday() {
        return getPatient().getBirthday();
    }

    public String getPatientAge() {
        return ModelUtils.getAge(getPatient().getBirthday());
    }

    public String getInsuranceUid() {
        return insuranceUid;
    }

    public void setInsuranceUid(String insuranceUid) {
        this.insuranceUid = insuranceUid;
    }

    /////////////////// Transferable 処理 //////////////////////////
    public boolean isDataFlavorSupported(DataFlavor df) {
        return df.equals(PVT_FLAVOR);
    }

    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException {
        if (df.equals(PVT_FLAVOR)) {
            return this;
        } else throw new UnsupportedFlavorException(df);
    }

    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
