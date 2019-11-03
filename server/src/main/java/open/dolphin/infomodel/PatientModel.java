package open.dolphin.infomodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import open.dolphin.util.ModelUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * PatientModel.
 *
 * @author Minagawa, kazushi
 */
@Entity
@Table(name = "d_patient")
public class PatientModel extends InfoModel {
    private static final long serialVersionUID = 2863696294817965234L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /**
     * 施設ID
     */
    @Column(nullable = false)
    private String facilityId;

    /**
     * 施設内の患者ID
     */
    @Column(nullable = false)
    private String patientId;

    private String familyName;

    private String givenName;

    @Column(nullable = false)
    private String fullName;

    private String kanaFamilyName;

    private String kanaGivenName;

    private String kanaName;

    private String romanFamilyName;

    private String romanGivenName;

    private String romanName;

    @Column(nullable = false)
    private String gender;

    private String genderDesc;

    @Transient
    private String genderCodeSys;

    private String birthday;

    private String nationality;

    @Transient
    private String nationalityDesc;

    @Transient
    private String nationalityCodeSys;

    private String maritalStatus;

    @Transient
    private String maritalStatusDesc;

    @Transient
    private String maritalStatusCodeSys;

    @Lob
    private byte[] jpegPhoto;

    private String memo;

    @Embedded
    private SimpleAddressModel address;

    private String telephone;

    private String mobilePhone;

    private String email;

    @JsonIgnore
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Collection<HealthInsuranceModel> healthInsurances;

    @Transient
    private Collection<PVTHealthInsuranceModel> pvtHealthInsurances;

    @Transient
    private Collection<AddressModel> addresses;

    @Transient
    private Collection<TelephoneModel> telephones;

    @Transient
    private String lastVisit;   // 最終受診日

    public String getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(String lastVisit) {
        this.lastVisit = lastVisit;
    }

    public String getNengoFormattedLastVisit() {
        String date = ModelUtils.toNengo(ModelUtils.trimTime(lastVisit));
        String time = ModelUtils.trimDate(lastVisit).substring(0, 5);
        return String.format("%s　%s", date, time);
    }

    public String getFormattedLastVisit() {
        String date = ModelUtils.trimTime(lastVisit);
        String time = ModelUtils.trimDate(lastVisit).substring(0, 5);
        return String.format("%s　%s", date, time);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String sirName) {
        this.familyName = sirName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String name) {
        this.fullName = name;
    }

    public String getKanaFamilyName() {
        return kanaFamilyName;
    }

    public void setKanaFamilyName(String kanaSirName) {
        this.kanaFamilyName = kanaSirName;
    }

    public String getKanaGivenName() {
        return kanaGivenName;
    }

    public void setKanaGivenName(String kanaGivenName) {
        this.kanaGivenName = kanaGivenName;
    }

    public String getKanaName() {
        return kanaName;
    }

    public void setKanaName(String kanaName) {
        this.kanaName = kanaName;
    }

    public String getRomanFamilyName() {
        return romanFamilyName;
    }

    public void setRomanFamilyName(String romanSirName) {
        this.romanFamilyName = romanSirName;
    }

    public String getRomanGivenName() {
        return romanGivenName;
    }

    public void setRomanGivenName(String romanGivenName) {
        this.romanGivenName = romanGivenName;
    }

    public String getRomanName() {
        return romanName;
    }

    public void setRomanName(String romanName) {
        this.romanName = romanName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGenderDesc() {
        return genderDesc != null
                ? genderDesc
                : ModelUtils.getGenderDesc(gender);
    }

    public void setGenderDesc(String genderDesc) {
        this.genderDesc = genderDesc;
    }

    public String getGenderCodeSys() {
        return genderCodeSys;
    }

    public void setGenderCodeSys(String genderCodeSys) {
        this.genderCodeSys = genderCodeSys;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAgeBirthday() {
        return ModelUtils.getAgeBirthday(birthday);
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getNationalityDesc() {
        return nationalityDesc;
    }

    public void setNationalityDesc(String nationalityDesc) {
        this.nationalityDesc = nationalityDesc;
    }

    public String getNationalityCodeSys() {
        return nationalityCodeSys;
    }

    public void setNationalityCodeSys(String nationalityCodeSys) {
        this.nationalityCodeSys = nationalityCodeSys;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getMaritalStatusDesc() {
        return maritalStatusDesc;
    }

    public void setMaritalStatusDesc(String maritalStatusDesc) {
        this.maritalStatusDesc = maritalStatusDesc;
    }

    public String getMaritalStatusCodeSys() {
        return maritalStatusCodeSys;
    }

    public void setMaritalStatusCodeSys(String maritalStatusCodeSys) {
        this.maritalStatusCodeSys = maritalStatusCodeSys;
    }

    public byte[] getJpegPhoto() {
        return jpegPhoto;
    }

    public void setJpegPhoto(byte[] jpegPhoto) {
        this.jpegPhoto = jpegPhoto;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public SimpleAddressModel getAddress() {
        return address;
    }

    public void setAddress(SimpleAddressModel address) {
        this.address = address;
    }

    public String contactZipCode() {
        return (address != null) ? address.getZipCode() : null;
    }

    public String contactAddress() {
        return (address != null) ? address.getAddress() : null;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Collection<HealthInsuranceModel> getHealthInsurances() {
        return healthInsurances;
    }

    public void setHealthInsurances(
            Collection<HealthInsuranceModel> healthInsurances) {
        this.healthInsurances = healthInsurances;
    }

    public void addHealthInsurance(HealthInsuranceModel value) {
        if (healthInsurances == null) {
            healthInsurances = new ArrayList<>(2);
        }
        healthInsurances.add(value);
    }

    public Collection<PVTHealthInsuranceModel> getPvtHealthInsurances() {
        return pvtHealthInsurances;
    }

    public void setPvtHealthInsurances(
            Collection<PVTHealthInsuranceModel> pvtHealthInsurances) {
        this.pvtHealthInsurances = pvtHealthInsurances;
    }

    public void addPvtHealthInsurance(PVTHealthInsuranceModel model) {
        if (pvtHealthInsurances == null) {
            pvtHealthInsurances = new ArrayList<>(2);
        }
        pvtHealthInsurances.add(model);
    }

    public Collection<AddressModel> getAddresses() {
        return addresses;
    }

    public void setAddresses(Collection<AddressModel> addresses) {
        this.addresses = addresses;
    }

    public void addAddress(AddressModel address) {
        if (addresses == null) {
            addresses = new ArrayList<>(1);
        }
        addresses.add(address);
    }

    public Collection<TelephoneModel> getTelephones() {
        return telephones;
    }

    public void setTelephones(Collection<TelephoneModel> telephones) {
        this.telephones = telephones;
    }

    public void addTelephone(TelephoneModel telephone) {
        if (telephones == null) {
            telephones = new ArrayList<>(1);
        }
        telephones.add(telephone);
    }

    public PatientLiteModel patientAsLiteModel() {
        PatientLiteModel model = new PatientLiteModel();
        model.setPatientId(getPatientId());
        model.setName(getFullName());
        model.setGender(getGender());
        model.setGenderDesc(getGenderDesc());
        model.setGenderCodeSys(getGenderCodeSys());
        model.setBirthday(getBirthday());
        return model;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PatientModel other = (PatientModel) obj;
        return (id == other.getId());
    }
}
