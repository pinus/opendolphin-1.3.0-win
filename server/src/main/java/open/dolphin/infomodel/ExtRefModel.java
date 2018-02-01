package open.dolphin.infomodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * 外部参照要素クラス.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
@Embeddable
public class ExtRefModel extends InfoModel {
    private static final long serialVersionUID = -3408876454565957708L;

    @Column(nullable=false)
    private String contentType;

    @Column(nullable=false)
    private String medicalRole;

    @Transient
    private String medicalRoleTableId;

    @Column(nullable=false)
    private String title;

    @Column(nullable=false)
    private String href;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String value) {
        contentType = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        title = value;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String value) {
        href = value;
    }

    public void setMedicalRole(String medicalRole) {
        this.medicalRole = medicalRole;
    }

    public String getMedicalRole() {
        return medicalRole;
    }

    public void setMedicalRoleTableId(String medicalRoleTableId) {
        this.medicalRoleTableId = medicalRoleTableId;
    }

    public String getMedicalRoleTableId() {
        return medicalRoleTableId;
    }
}
