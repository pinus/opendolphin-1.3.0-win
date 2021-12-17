package open.dolphin.infomodel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;

/**
 * ModuleModel.
 * Field 'model' contains any of BundleMed, BundleDolphin, or ProgressCourse
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Entity
@Table(name = "d_module")
public class ModuleModel extends KarteEntryBean<ModuleModel> {
    private static final long serialVersionUID = -8781968977231876023L;

    @Embedded
    private ModuleInfoBean moduleInfo;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes({
        @JsonSubTypes.Type(BundleDolphin.class),
        @JsonSubTypes.Type(BundleMed.class),
        @JsonSubTypes.Type(ProgressCourse.class)
    })
    @Transient
    private IInfoModel model;

    @Lob
    @Field(index = Index.YES)                         // hibernate search
    @FieldBridge(impl = ModuleModelBridge.class)    // hibernate search
    @Analyzer(impl = CJKAnalyzer.class)             // hibernate search
    @Column(nullable = false)
    private byte[] beanBytes;

    @ManyToOne
    @JoinColumn(name = "doc_id", nullable = false)
    private DocumentModel document;

    public ModuleModel() {
        moduleInfo = new ModuleInfoBean();
    }

    public DocumentModel getDocument() {
        return document;
    }

    public void setDocument(DocumentModel document) {
        this.document = document;
    }

    public ModuleInfoBean getModuleInfo() {
        return moduleInfo;
    }

    public void setModuleInfo(ModuleInfoBean moduleInfo) {
        this.moduleInfo = moduleInfo;
    }

    public IInfoModel getModel() {
        return model;
    }

    public void setModel(IInfoModel model) { this.model = model; }

    public byte[] getBeanBytes() {
        return beanBytes;
    }

    public void setBeanBytes(byte[] beanBytes) {
        this.beanBytes = beanBytes;
    }

    @Override
    public int compareTo(ModuleModel other) {
        if (other != null && getClass() == other.getClass()) {
            ModuleInfoBean moduleInfo1 = getModuleInfo();
            ModuleInfoBean moduleInfo2 = other.getModuleInfo();
            return moduleInfo1.compareTo(moduleInfo2);
        }
        return -1;
    }
}
