package open.dolphin.delegater;

import open.dolphin.client.ImageEntry;
import open.dolphin.dto.*;
import open.dolphin.helper.ImageHelper;
import open.dolphin.infomodel.*;
import open.dolphin.service.KarteService;
import open.dolphin.util.ModelUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * DocumentDelegater.
 *
 * @author pns
 */
public class DocumentDelegater extends BusinessDelegater<KarteService> {
    private static final int MAX_IMAGE_WIDTH = 522; // ClientContext.getInt("image.max.width")
    private static final int MAX_IMAGE_HEIGHT = 522; // ClientContext.getInt("image.max.height")
    private static final Dimension MAX_IMAGE_SIZE = new Dimension(MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);

    /**
     * 患者のカルテを取得する.
     *
     * @param patientPk 患者PK
     * @param fromDate  履歴の検索開始日
     * @return KarteBean
     */
    public KarteBean getKarte(long patientPk, Date fromDate) {
        KarteBeanSpec spec = new KarteBeanSpec();
        spec.setPatientPk(patientPk);
        spec.setFromDate(fromDate);
        return getService().getKarte(spec);
    }

    /**
     * Documentを保存する.
     *
     * @param karteModel KarteModel
     * @return 保存した document の primary key
     */
    public long putKarte(DocumentModel karteModel) {
        // 確定日，適合開始日，記録日，ステータスを DocInfo から DocumentModel(KarteEntry) に移す
        karteModel.toPersist();
        // 保存する
        return getService().addDocument(karteModel); // returns document pk
    }

    /**
     * Documentを検索して返す.
     * ModuleModel の beanBytes はサーバでデコード済み
     *
     * @param ids DocumentModel の primary key リスト
     * @return DocumentValue
     */
    public List<DocumentModel> getDocuments(List<Long> ids) {
        // 検索する
        List<DocumentModel> ret = getService().getDocumentList(ids);

        // ByteArray をアイコンへ戻す (getSchema() は必ず non null)
        ret.stream().map(DocumentModel::getSchema).forEach(sc -> sc.stream().forEach(schema -> {
            byte[] bytes = schema.getJpegByte();
            ImageIcon icon = new ImageIcon(bytes);
            schema.setIcon(icon);
        }));
        return ret;
    }

    /**
     * 文書履歴を検索して返す.
     *
     * @param spec DocumentSearchSpec 検索仕様
     * @return DocInfoModel の List
     */
    public List<DocInfoModel> getDocInfoList(DocumentSearchSpec spec) {
        return spec.getDocType().equals(InfoModel.DOCTYPE_KARTE) ?
                getService().getDocInfoList(spec) : null;
    }

    /**
     * ドキュメントを論理削除する.
     *
     * @param pk 論理削除するドキュメントの prmary key
     * @return 削除件数
     */
    public int deleteDocument(long pk) {
        return getService().deleteDocument(pk);
    }

    /**
     * 文書履歴のタイトルを変更する.
     *
     * @param docInfo DocInfoModel
     * @return 変更した件数
     */
    public int updateTitle(DocInfoModel docInfo) {
        DocumentTitleSpec spec = new DocumentTitleSpec();
        spec.setDocInfoPk(docInfo.getDocPk());
        spec.setTitle(docInfo.getTitle());
        return getService().updateTitle(spec);
    }

    /**
     * Moduleを検索して返す.
     * beanBytes はデコード済み
     *
     * @param spec ModuleSearchSpec 検索仕様
     * @return {@code List<ModuleModel>} の List
     */
    public List<List<ModuleModel>> getModuleList(ModuleSearchSpec spec) {
        return getService().getModuleList(spec);
    }

    /**
     * イメージを取得する.
     *
     * @param id 画像のId
     * @return SchemaModel
     */
    public SchemaModel getImage(long id) {

        SchemaModel model = getService().getImage(id);
        // JpegBytes を icon に戻す
        if (model != null) {
            byte[] bytes = model.getJpegByte();
            ImageIcon icon = new ImageIcon(bytes);
            model.setIcon(icon);
        }
        return model;
    }

    /**
     * Imageを検索して返す.
     *
     * @param spec ImageSearchSpec 検索仕様
     * @return ImageEntryリストのリスト
     */
    public List<List<ImageEntry>> getImageList(ImageSearchSpec spec) {

        List<List<ImageEntry>> ret = new ArrayList<>();

        // 検索結果
        List<List<SchemaModel>> result = getService().getImageList(spec);

        for (List<SchemaModel> periodList : result) {
            // ImageEntry 用のリスト
            List<ImageEntry> el = new ArrayList<>();

            // 抽出期間をイテレートする
            for (SchemaModel model : periodList) {
                ImageEntry entry = getImageEntry(model, spec.getIconSize());
                el.add(entry);
            }
            // リターンリストへ追加する
            ret.add(el);
        }
        return ret;
    }

    /**
     * SchemaModel を ImageEntry に変換する.
     *
     * @param schema   シェーマモデル
     * @param iconSize アイコンのサイズ
     * @return ImageEntry
     */
    private ImageEntry getImageEntry(SchemaModel schema, Dimension iconSize) {

        ImageEntry entry = new ImageEntry();

        entry.setId(schema.getId());
        entry.setConfirmDate(ModelUtils.getDateTimeAsString(schema.getConfirmed()));  // First?
        entry.setContentType(schema.getExtRef().getContentType());
        entry.setTitle(schema.getExtRef().getTitle());
        entry.setMedicalRole(schema.getExtRef().getMedicalRole());

        byte[] bytes = schema.getJpegByte();

        // Create ImageIcon
        ImageIcon icon = new ImageIcon(bytes);
        entry.setImageIcon(ImageHelper.adjustImageSize(icon, iconSize));

        return entry;
    }

    /**
     * RegisteredDiagnosisModel を登録する.
     *
     * @param beans List of RegisteredDiagnosisModel
     * @return 登録した Model の primary key のリスト
     */
    public List<Long> putDiagnosis(List<RegisteredDiagnosisModel> beans) {
        return getService().addDiagnosisList(beans);
    }

    /**
     * RegisteredDiagnosisModel を更新する.
     *
     * @param beans List of RegisteredDiagnosisModel
     * @return 更新した数
     */
    public int updateDiagnosis(List<RegisteredDiagnosisModel> beans) {
        return getService().updateDiagnosisList(beans);
    }

    /**
     * 病名を削除する.
     *
     * @param ids 削除する傷病名の primary key リスト
     * @return 削除数
     */
    public int removeDiagnosis(List<Long> ids) {
        return getService().removeDiagnosisList(ids);
    }

    /**
     * Diagnosisを検索して返す.
     *
     * @param spec DiagnosisSearchSpec 検索仕様
     * @return DiagnosisModel の Collection
     */
    public List<RegisteredDiagnosisModel> getDiagnosisList(DiagnosisSearchSpec spec) {
        return getService().getDiagnosisList(spec);
    }

    /**
     * Observationを追加する.
     *
     * @param observations 追加するObservationのリスト
     * @return 追加したObservationのIdリスト
     */
    public List<Long> addObservations(List<ObservationModel> observations) {
        return getService().addObservationList(observations);
    }

    /**
     * Observationを取得する.
     *
     * @param spec 検索仕様
     * @return Observationのリスト
     */
    public List<ObservationModel> getObservations(ObservationSearchSpec spec) {
        return getService().getObservationList(spec);
    }

    /**
     * Observationを更新する.
     *
     * @param observations 更新するObservationのリスト
     * @return 更新した数
     */
    public int updateObservations(List<ObservationModel> observations) {
        return getService().updateObservationList(observations);
    }

    /**
     * Observationを削除する.
     *
     * @param ids 削除する Observation の primary key リスト
     * @return 削除した数
     */
    public int removeObservations(List<Long> ids) {
        return getService().removeObservationList(ids);
    }

    /**
     * 患者メモを更新する.
     *
     * @param memo 更新するメモ
     * @return 更新した数 1
     */
    public int updatePatientMemo(PatientMemoModel memo) {
        return getService().updatePatientMemo(memo);
    }

    /**
     * 予約を検索する.
     *
     * @param spec 検索仕様
     * @return 予約の List
     */
    public List<List<AppointmentModel>> getAppoinmentList(ModuleSearchSpec spec) {
        return getService().getAppointmentList(spec);
    }
}
