package open.dolphin.service;

import open.dolphin.dto.*;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.orca.orcadao.bean.OnshiKenshin;
import open.dolphin.orca.orcadao.bean.OnshiYakuzai;
import open.dolphin.orca.orcadao.bean.Syskanri;
import open.dolphin.orca.orcadao.bean.Wksryact;

import java.util.List;

/**
 * OrcaService Implementation Bridge.
 *
 * @author pns
 */
public class OrcaServiceImpl implements OrcaService {

    private OrcaServiceDao dao = new OrcaServiceDao();
    private OrcaServiceApi api = new OrcaServiceApi();

    @Override
    public Wksryact getWksryact(PatientVisitSpec spec) {
        return api.getWksryact(spec);
    }

    @Override
    public boolean existsOrcaWorkingData(String ptId) {
        return api.existsOrcaWorkingData(ptId);
    }

    @Override
    public List<Syskanri> getSyskanri() {
        return api.getSyskanri();
    }

    @Override
    public List<OrcaEntry> findTensu(String keyword) {
        return dao.findTensu(keyword);
    }

    @Override
    public List<OrcaEntry> findDiagnosisByKeyword(String keyword) {
        return dao.findDiagnosis(keyword);
    }

    @Override
    public List<OrcaEntry> findDiagnosisByCodes(List<String> srycds) {
        return dao.findDiagnosis(srycds);
    }

    @Override
    public List<String> findIkouByomei(List<String> srycds) {
        return dao.findIkouByomei(srycds);
    }

    @Override
    public List<ModuleInfoBean> getOrcaInputCdList() {
        return dao.getOrcaInputCdList();
    }

    @Override
    public List<ModuleModel> getStamp(ModuleInfoBean stampInfo) {
        return api.getStamp(stampInfo);
    }

    @Override
    public List<RegisteredDiagnosisModel> getOrcaDisease(DiagnosisSearchSpec spec) {
        return api.getOrcaDisease(spec);
    }

    @Override
    public ApiResult sendDocument(DocumentModel document) {
        return api.sendDocument(document);
    }

    @Override
    public ApiResult sendDiagnoses(List<RegisteredDiagnosisModel> diagnoses) {
        return api.sendDiagnoses(diagnoses);
    }

    @Override
    public ApiResult sendSubjectives(SubjectivesSpec spec) {
        return api.sendSubjectives(spec);
    }

    @Override
    public List<SubjectivesSpec> getSubjectives(SubjectivesSpec spec) {
        return api.getSubjectives(spec);
    }

    @Override
    public List<OnshiYakuzai> getDrugHistory(String ptnum) { return dao.getDrugHistory(ptnum); }

    @Override
    public boolean hasDrugHistory(String ptnum) { return dao.hasDrugHistory(ptnum); }

    @Override
    public List<OnshiKenshin> getKenshin(String ptnum) { return dao.getKenshin(ptnum); }

    @Override
    public boolean hasKenshin(String ptnum) { return dao.hasKenshin(ptnum); }
}
