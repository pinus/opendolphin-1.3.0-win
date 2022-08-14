package open.dolphin.delegater;

import open.dolphin.dto.*;
import open.dolphin.infomodel.*;
import open.dolphin.orca.orcadao.bean.OnshiKenshin;
import open.dolphin.orca.orcadao.bean.OnshiYakuzai;
import open.dolphin.orca.orcadao.bean.Syskanri;
import open.dolphin.orca.orcadao.bean.Wksryact;
import open.dolphin.service.OrcaService;

import javax.swing.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * OrcaDelegater.
 *
 * @author pns
 */
public class OrcaDelegater extends BusinessDelegater<OrcaService> {

    /**
     * 中途終了患者情報.
     *
     * @param spec PatientVisitSpec (patientId と date を使用)
     * @return Wksryact
     */
    public Wksryact getWksryact(PatientVisitSpec spec) {
        return getService().getWksryact(spec);
    }

    /**
     * 中途終了患者情報が存在するかどうか.
     *
     * @param ptId "000001"
     * @return 中途終了情報あり=true
     */
    public boolean existsOrcaWorkingData(String ptId) {
        return getService().existsOrcaWorkingData(ptId);
    }

    /**
     * 職員情報.
     *
     * @return Syskanri のリスト
     */
    public List<Syskanri> getSyskanri() {
        return getService().getSyskanri();
    }

    /**
     * TBL_TENSU からキーワードを検索.
     *
     * @param keyword キーワード
     * @return OrcaEntry の List
     */
    public List<OrcaEntry> findTensu(String keyword) {
        return getService().findTensu(keyword);
    }

    /**
     * TBL_BYOMEI からキーワードを検索.
     *
     * @param keyword キーワード
     * @return OrcaEntry の List
     */
    public List<OrcaEntry> findDiagnosis(String keyword) {
        return getService().findDiagnosisByKeyword(keyword);
    }

    /**
     * TBL_BYOMEI から病名コードのリストに対応する病名を検索.
     *
     * @param srycds 病名コードのセット
     * @return OrcaEntry の List
     */
    public List<OrcaEntry> findDiagnosis(List<String> srycds) {
        return getService().findDiagnosisByCodes(srycds);
    }

    /**
     * 移行病名を調べる.
     *
     * @param srycds 病名コードのリスト
     * @return そのうち移行病名になっているののリスト
     */
    public List<String> findIkouByomei(List<String> srycds) {
        return getService().findIkouByomei(srycds);
    }

    /**
     * TBL_INPUTCD を検索して入力セット（約束処方、診療セット）のリストを返す.
     *
     * @return ModuleInfoBean
     */
    public List<ModuleInfoBean> getOrcaInputCdList() {
        return getService().getOrcaInputCdList();
    }

    /**
     * StampInfo を元に TBL_INPUTSET，TBL_TENSU を検索してスタンプの実体を作る
     *
     * @param stampInfo ModuleInfoBean
     * @return ModuleModel の List
     */
    public List<ModuleModel> getStamp(ModuleInfoBean stampInfo) {
        return getService().getStamp(stampInfo);
    }

    /**
     * TBL_PTBYOMEI を検索して RegisteredDiagnosisModel を作る
     *
     * @param patientId 患者 ID "000001"
     * @param from      Date
     * @param to        Date
     * @param ascend    昇順=true
     * @return List of RegisteredDiagnosisModel
     */
    public List<RegisteredDiagnosisModel> getOrcaDisease(String patientId, LocalDate from, LocalDate to, boolean ascend) {

        DiagnosisSearchSpec spec = new DiagnosisSearchSpec();
        spec.setPatientId(patientId);
        spec.setFromDate(Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        spec.setToDate(Date.from(to.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        return getService().getOrcaDisease(spec);
    }

    /**
     * DocumentModel から中途終了データ作成 (medicalmodv2).
     *
     * @param document DocumentModel
     * @return ApiResult
     */
    public Result sendDocument(DocumentModel document) {
        // 2022-06-25 健康保険情報が null 化するなぞ事態が発生
        // 新患登録で保険登録をミスったようだが詳細不明
        Collection<PVTHealthInsuranceModel> insurances = document.getKarte().getPatient().getPvtHealthInsurances();
        if (insurances == null || insurances.isEmpty()) {
            insurances = new ArrayList<>(1);
            PVTHealthInsuranceModel model = new PVTHealthInsuranceModel();
            model.setInsuranceClass(IInfoModel.INSURANCE_SELF);
            model.setInsuranceClassCode(IInfoModel.INSURANCE_SELF_CODE);
            model.setInsuranceClassCodeSys(IInfoModel.INSURANCE_SYS);
            insurances.add(model);
        }
        if (document.getDocInfo().getHealthInsuranceGUID() == null) {
            document.getDocInfo().setHealthInsuranceGUID("00000");
        }

        ApiResult result = getService().sendDocument(document);
        String apiResult = result.getApiResult();
        String ptId = document.getKarte().getPatient().getPatientId();
        String ptName = document.getKarte().getPatient().getFullName();
        String message = String.format("[%s] %s\nORCA で使用中のため送信できませんでした。再送しますか？", ptId, ptName);

        // 他端末で使用中(90)の場合は，手動でリトライする
        while ("90".equals(apiResult)) {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null,
                    message, "ORCA 送信エラー", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) {
                return Result.ERROR;
            }
            result = getService().sendDocument(document);
            apiResult = result.getApiResult();
        }
        return Result.NO_ERROR;
    }

    /**
     * medicalmodv2 で ORCA に病名を送る.
     *
     * @param diagnoses List of RegisteredDiagnosisModel
     * @return ApiResult
     */
    public Result sendDiagnoses(List<RegisteredDiagnosisModel> diagnoses) {
        // 非同期で送信
        Executors.newCachedThreadPool().submit(() -> getService().sendDiagnoses(diagnoses));
        // 再送処理はサーバー側で行う
        return Result.NO_ERROR;
    }

    /**
     * subjectivesv2 で ORCA に症状詳記を送る.
     *
     * @param spec SubjectivesSpec
     * @return ApiResult
     */
    public Result sendSubjectives(SubjectivesSpec spec) {
        ApiResult result = getService().sendSubjectives(spec);
        logger.info(result.getApiResultMessage());

        // ここは busy (90) にならないみたい
        String apiResult = result.getApiResult();
        String ptId = spec.getPatientId();
        String message = String.format("[%s] ORCA で使用中のため送信できませんでした。再送しますか？", ptId);

        // 他端末で使用中(90)の場合は，手動でリトライする
        while ("90".equals(apiResult)) {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(null,
                    message, "ORCA 送信エラー", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE)) {
                return Result.ERROR;
            }
            result = getService().sendSubjectives(spec);
            apiResult = result.getApiResult();
        }
        return Result.NO_ERROR;
    }

    /**
     * subjectiveslstv2 で ORCA から症状詳記の一覧を得る.
     *
     * @param spec SubjectivesSpec
     * @return ApiResult
     */
    public List<SubjectivesSpec> getSubjectives(SubjectivesSpec spec) {
        return getService().getSubjectives(spec);
    }

    /**
     * TBL_ONSHI_YAKUZAI_SUB から資格確認薬剤情報を得る.
     *
     * @param ptnum
     * @return List of Onshi Yakuzai
     */
    public List<OnshiYakuzai> getDrugHistory(String ptnum) { return getService().getDrugHistory(ptnum); }

    /**
     * TBL_ONSHI_YAKUZAI_SUB に資格確認薬剤情報があるかどうかを返す.
     *
     * @param ptnum
     * @return has drug history or not
     */
    public boolean hasDrugHistory(String ptnum) { return getService().hasDrugHistory(ptnum); }

    /**
     * TBL_ONSHI_KENSHIN_SUB から資格確認特定健診情報を得る.
     *
     * @param ptnum
     * @return List of Onshi Kenshin
     */
    public List<OnshiKenshin> getKenshin(String ptnum) { return getService().getKenshin(ptnum); }

    /**
     * TBL_ONSHI_KENSHIN_SUB に資格確認特定健診情報があるかどうかを返す.
     *
     * @param ptnum
     * @return has onshi kenshin or not
     */
    public boolean hasKenshin(String ptnum) { return getService().hasKenshin(ptnum); }
}
