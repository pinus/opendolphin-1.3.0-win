package open.dolphin.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.text.DefaultStyledDocument;
import open.dolphin.helper.ImageHelper;
import open.dolphin.infomodel.DocumentModel;
import static open.dolphin.infomodel.IInfoModel.MODULE_PROGRESS_COURSE;
import static open.dolphin.infomodel.IInfoModel.ROLE_P_SPEC;
import static open.dolphin.infomodel.IInfoModel.ROLE_SOA_SPEC;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.ProgressCourse;
import open.dolphin.infomodel.SchemaModel;
import org.apache.log4j.Logger;

/**
 * Model for KarteEditor Autosave.
 * @author pns
 */
public class AutosaveModel {
    private DocumentModel documentModel;
    private String soaSpec;
    private String pSpec;
    private List<ModuleModel> moduleList;
    private List<SchemaModel> schemaList;
    private String patientId;
    @JsonIgnore
    private KartePaneDumper_2 dumper;
    @JsonIgnore
    private HashSet<Integer> imageHash;
    @JsonIgnore
    private Logger logger;

    public AutosaveModel() {
        dumper = new KartePaneDumper_2();
        imageHash = new HashSet<>();
        logger = Logger.getLogger(AutosaveModel.class);
    }

    public DocumentModel getDocumentModel() {
        return documentModel;
    }

    public String getPatientId() {
        return patientId;
    }

    /**
     * KarteEditor を dump して　AutosaveModel を作成する.
     * @param editor
     */
    public void dump(KarteEditor editor) {
        documentModel = editor.getModel();
        patientId = editor.getContext().getKarte().getPatient().getPatientId();

        DefaultStyledDocument soa = (DefaultStyledDocument) editor.getSOAPane().getTextPane().getDocument();
        DefaultStyledDocument p = (DefaultStyledDocument) editor.getPPane().getTextPane().getDocument();

        moduleList = new ArrayList<>();
        schemaList = new ArrayList<>();

        // soa
        dumper.dump(soa);
        soaSpec = dumper.getSpec();
        dumper.getModule().forEach(moduleList::add);
        dumper.getSchema().forEach(m -> {
            Image image = m.getIcon().getImage();
            int hash = image.hashCode();

            if (! imageHash.contains(hash)) {
                // image を byte array に変換
                m.setJpegByte(ImageHelper.imageToByteArray(image));
                imageHash.add(hash);
                logger.info("image updated");
            }
            schemaList.add(m);
        });

        // p
        dumper.dump(p);
        pSpec = dumper.getSpec();
        dumper.getModule().forEach(moduleList::add);
    }

    /**
     * AutosaveModel から DocumentModel を作成する.
     * ここで作った DocumentModel は KarteEditor#displayModel で rendering できる.
     */
    public void composeDocumentModel() {
        documentModel.clearModules();
        documentModel.clearSchema();

        // module model
        moduleList.forEach(documentModel::addModule);

        // soa ProgressCourse
        ModuleInfoBean soaInfo = new ModuleInfoBean();
        soaInfo.setStampName(MODULE_PROGRESS_COURSE);
        soaInfo.setEntity(MODULE_PROGRESS_COURSE);
        soaInfo.setStampRole(ROLE_SOA_SPEC);

        ProgressCourse soaPc = new ProgressCourse();
        soaPc.setFreeText(soaSpec);
        ModuleModel soaMm = new ModuleModel();
        soaMm.setModuleInfo(soaInfo);
        soaMm.setModel(soaPc);
        documentModel.addModule(soaMm);

        // p ProgressCourse
        ModuleInfoBean pInfo = new ModuleInfoBean();
        pInfo.setStampName(MODULE_PROGRESS_COURSE);
        pInfo.setEntity(MODULE_PROGRESS_COURSE);
        pInfo.setStampRole(ROLE_P_SPEC);

        ProgressCourse pPc = new ProgressCourse();
        pPc.setFreeText(pSpec);
        ModuleModel pMm = new ModuleModel();
        pMm.setModuleInfo(pInfo);
        pMm.setModel(pPc);
        documentModel.addModule(pMm);

        // schema
        schemaList.forEach(m -> {
            m.setIcon(new ImageIcon(m.getJpegByte()));
            m.setJpegByte(null);
            documentModel.addSchema(m);
        });
    }
}
