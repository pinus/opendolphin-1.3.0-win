package open.dolphin.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import open.dolphin.infomodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static open.dolphin.infomodel.IInfoModel.*;

/**
 * Model for KarteEditor Autosave.
 *
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
        logger = LoggerFactory.getLogger(AutosaveModel.class);
    }

    public DocumentModel getDocumentModel() {
        return documentModel;
    }

    public String getPatientId() {
        return patientId;
    }

    /**
     * KarteEditor を dump して　AutosaveModel を作成する.
     *
     * @param editor KarteEditor
     */
    public void dump(KarteEditor editor) {
        documentModel = editor.getDocument();
        patientId = editor.getContext().getKarte().getPatient().getPatientId();

        DefaultStyledDocument soa = (DefaultStyledDocument) editor.getSOAPane().getTextPane().getDocument();
        DefaultStyledDocument p = (DefaultStyledDocument) editor.getPPane().getTextPane().getDocument();

        moduleList = new ArrayList<>();
        schemaList = new ArrayList<>();

        // soa
        dumper.dump(soa);
        soaSpec = dumper.getSpec();
        moduleList.addAll(dumper.getModule());
        schemaList.addAll(dumper.getSchema());

        // p
        dumper.dump(p);
        pSpec = dumper.getSpec();
        moduleList.addAll(dumper.getModule());
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
            documentModel.addSchema(m);
        });
    }
}
