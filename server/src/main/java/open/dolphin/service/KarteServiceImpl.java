package open.dolphin.service;

import open.dolphin.dto.*;
import open.dolphin.infomodel.*;
import open.dolphin.util.ModelUtils;
import org.jboss.logging.Logger;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * KarteServiceImpl.
 *
 * @author pns
 */
@Stateless
public class KarteServiceImpl extends DolphinService implements KarteService {
    private static final long serialVersionUID = 1L;
    private final Logger logger = Logger.getLogger(KarteServiceImpl.class);

    /**
     * カルテの基礎的な情報をまとめて返す.
     *
     * @param spec KarteBeanSpec
     * @return 基礎的な情報をフェッチした KarteBean
     */
    @Override
    public KarteBean getKarte(KarteBeanSpec spec) {
        long patientPk = spec.getPatientPk();
        Date fromDate = spec.getFromDate();

        try {
            // 最初に患者のカルテを取得する
            KarteBean karte = em.createQuery("select k from KarteBean k where k.patient.id = :patientPk", KarteBean.class)
                    .setParameter("patientPk", patientPk).getSingleResult();

            // カルテの PK を得る
            long karteId = karte.getId();

            // アレルギーデータを取得する
            List<AllergyModel> allergyList = getAllergyList(karteId);
            karte.setAllergyEntry(allergyList);

            // 身長データを取得する
            List<PhysicalModel> physList = getPhysicalList(karteId);
            karte.setPhysicalEntry(physList);

            // 直近の来院日エントリーを取得しカルテに設定する
            List<String> pvtList = getPvtList(spec);
            karte.setPvtDateEntry(pvtList);

            // 文書履歴エントリーを取得しカルテに設定する
            DocumentSearchSpec docSpec = new DocumentSearchSpec();
            docSpec.setKarteId(karteId);
            docSpec.setFromDate(fromDate);
            docSpec.setIncludeModifid(false);
            List<DocInfoModel> docList = getDocInfoList(docSpec);
            karte.setDocInfoEntry(docList);

            // 患者Memoを取得する
            PatientMemoModel memo = getPatientMemo(karteId);
            karte.setPatientMemo(memo);

            return karte;

        } catch (NoResultException e) {
            // 患者登録の際にカルテも生成してある
            logger.info(e.getMessage(), e.getCause());
        }

        return null;
    }

    /**
     * アレルギーリストを返す.
     *
     * @param karteId karte pk
     * @return List of AllergyModel
     */
    @Override
    public List<AllergyModel> getAllergyList(Long karteId) {
        List<ObservationModel> observations = em.createQuery("select o from ObservationModel o where o.karte.id=:karteId and o.observation='Allergy'", ObservationModel.class)
                .setParameter("karteId", karteId).getResultList();

        return observations.stream().map(observation -> {
            AllergyModel allergy = new AllergyModel();
            allergy.setObservationId(observation.getId());
            allergy.setFactor(observation.getPhenomenon());
            allergy.setSeverity(observation.getCategoryValue());
            allergy.setIdentifiedDate(observation.confirmDateAsString());
            return allergy;
        }).collect(Collectors.toList());
    }

    /**
     * 身長・体重（PhysicalModel）リストを返す.
     *
     * @param karteId karte pk
     * @return List of PhysicalModel
     */
    @Override
    public List<PhysicalModel> getPhysicalList(Long karteId) {
        // 身長・体重データを取得
        List<ObservationModel> observations =
                em.createQuery("select o from ObservationModel o where o.karte.id=:karteId and o.observation='PhysicalExam' and (o.phenomenon='bodyHeight' or o.phenomenon='bodyWeight')", ObservationModel.class)
                        .setParameter("karteId", karteId).getResultList();

        // confirm date をキーとした PhysicalModel の Map
        Map<String, PhysicalModel> map = observations.stream().collect(Collectors.toMap(o -> {
            // key
            String memo = ModelUtils.getDateAsString(o.getRecorded());
            String identified = o.confirmDateAsString();
            return identified != null ? identified : memo;

        }, o -> {
            // ObservationModel から PhysicalModel を作成する
            PhysicalModel pm = new PhysicalModel();
            pm.setMemo(ModelUtils.getDateAsString(o.getRecorded()));
            pm.setIdentifiedDate(o.confirmDateAsString());

            if (o.getPhenomenon().equals(IInfoModel.PHENOMENON_BODY_WEIGHT)) {
                pm.setWeightId(o.getId());
                pm.setWeight(o.getValue());
            } else {
                pm.setHeightId(o.getId());
                pm.setHeight(o.getValue());
            }
            return pm;

        }, (p1, p2) -> {
            // 同じ confirm date にデータが2つある場合統合する
            if (p1.getWeightId() == 0) {
                // p1 は height だけのデータなので，weight を補う
                p1.setWeightId(p2.getWeightId());
                p1.setWeight(p2.getWeight());
                return p1;

            } else {
                // p2 は height だけのデータなので，weight を補う
                p2.setWeightId(p1.getWeightId());
                p2.setWeight(p1.getWeight());
                return p2;
            }
        }));

        return new ArrayList<>(map.values());
    }

    /**
     * Karte に関連した PatientVisitModel.pvtDate のリストを返す.
     *
     * @param spec KarteBeanSpec
     * @return List of PvtDate
     */
    @Override
    public List<String> getPvtList(KarteBeanSpec spec) {
        long patientPk = spec.getPatientPk();
        Date fromDate = spec.getFromDate();

        List<PatientVisitModel> latestVisits = em.createQuery("select p from PatientVisitModel p where p.patient.id = :patientPk and p.pvtDate >= :fromDate", PatientVisitModel.class)
                .setParameter("patientPk", patientPk)
                .setParameter("fromDate", ModelUtils.getDateAsString(fromDate)).getResultList();

        return latestVisits.stream()
                .filter(m -> m.getState() != KarteState.CANCEL_PVT)
                .map(PatientVisitModel::getPvtDate).collect(Collectors.toList());
    }

    /**
     * KarteId の関連する PatientMemoModel を返す.
     *
     * @param karteId karte pk
     * @return PatientMemoModel
     */
    @Override
    public PatientMemoModel getPatientMemo(Long karteId) {
        List<PatientMemoModel> memo = em.createQuery("select p from PatientMemoModel p where p.karte.id = :karteId", PatientMemoModel.class)
                .setParameter("karteId", karteId).getResultList();
        return memo.isEmpty() ? null : memo.get(0);
    }

    /**
     * 文書履歴エントリを取得する.
     *
     * @param spec DocumentSearchSpec
     * @return List of DocInfo
     */
    @Override
    public List<DocInfoModel> getDocInfoList(DocumentSearchSpec spec) {

        List<DocumentModel> documents;

        if (spec.isIncludeModifid()) {
            documents = em.createQuery("select d from DocumentModel d where d.karte.id = :karteId and d.started >= :fromDate and d.status !='D'", DocumentModel.class)
                    .setParameter("karteId", spec.getKarteId())
                    .setParameter("fromDate", spec.getFromDate()).getResultList();
        } else {
            documents = em.createQuery("select d from DocumentModel d where d.karte.id = :karteId and d.started >= :fromDate and (d.status='F' or d.status='T')", DocumentModel.class)
                    .setParameter("karteId", spec.getKarteId())
                    .setParameter("fromDate", spec.getFromDate()).getResultList();
        }

        return documents.stream().map(d -> {
            d.toDetach();
            return d.getDocInfo();
        }).collect(Collectors.toList());
    }

    /**
     * 文書(DocumentModel Object)を取得する.
     *
     * @param ids List of DocumentModel pk
     * @return List of DocumentModel
     */
    @Override
    public List<DocumentModel> getDocumentList(List<Long> ids) {
        //long t = System.currentTimeMillis();
        // まとめて query
        List<ModuleModel> mods = em.createQuery("select m from ModuleModel m where m.document.id in (:ids) order by m.id", ModuleModel.class)
                .setParameter("ids", ids)
                .getResultList();
        List<SchemaModel> imgs = em.createQuery("select m from SchemaModel m where m.document.id in (:ids) order by m.id", SchemaModel.class)
                .setParameter("ids", ids)
                .getResultList();

        // とってきた ModuleModel を id 毎に分ける
        Map<Long, List<ModuleModel>> modsMap = mods.stream()
                .collect(Collectors.groupingBy(m -> {
                    // beanBytes をいじるために detach する
                    em.detach(m);
                    // beanBytes をデコードする
                    m.setModel((IInfoModel) ModelUtils.xmlDecode(m.getBeanBytes()));
                    m.setBeanBytes(null);
                    return m.getDocument().getId();
                }));

        // とってきた SchemaModel を id 毎に分ける
        Map<Long, List<SchemaModel>> imgsMap = imgs.stream()
                .collect(Collectors.groupingBy(m -> m.getDocument().getId()));

        // とってきた list を DocumentModel に分配
        List<DocumentModel> ret = ids.stream()
                .map(id -> {
                    // DocumentModel を取得する
                    DocumentModel document = em.find(DocumentModel.class, id);
                    // detach しないと org.hibernate.PersistentObjectException: detached entity passed to persist
                    em.detach(document);

                    // ModuleBean を登録
                    List<ModuleModel> modules = modsMap.get(document.getId());
                    if (modules == null) {
                        modules = new ArrayList<>();
                    }
                    document.setModules(modules);

                    // SchemaModel を登録
                    List<SchemaModel> images = imgsMap.get(document.getId());
                    if (images == null) {
                        images = new ArrayList<>();
                    }
                    document.setSchema(images);
                    return document;

                }).collect(Collectors.toList());

        //System.out.println("---- lap= " + (System.currentTimeMillis() - t));
        return ret;
    }

    /**
     * ドキュメント DocumentModel オブジェクトを保存する.
     *
     * @param document 追加するDocumentModel オブジェクト
     * @return 追加した document の primary key
     */
    @Override
    public long addDocument(DocumentModel document) {

        // ModuleModel の永続化 beanBytes を作成する
        document.getModules().forEach(m -> {
            m.setBeanBytes(ModelUtils.xmlEncode(m.getModel()));
            m.setModel(null);
        });

        // 永続化する
        em.persist(document);

        // ID
        long id = document.getId();

        // 修正版の処理は非同期処理させる by masuda-sensei
        processPostAddDocument(document);

        return id;
    }

    /**
     * 後処置は asynchronous にしてクライアントを待たせない by masuda-sensei.
     *
     * @param document DocumentModel
     */
    @Asynchronous
    private void processPostAddDocument(DocumentModel document) {
        // 修正版の処理を行う
        DocInfoModel docInfo = document.getDocInfo();
        long parentPk = document.getDocInfo().getParentPk();

        // 親がないならリターン
        if (parentPk == 0L) {
            return;
        }

        DocumentModel old = em.find(DocumentModel.class, parentPk);
        if (old == null) {
            return;
        }

        // 親文書が仮保存文書なら残す必要なし. なぜならそれは仮保存だから. by masuda-sensei
        if (IInfoModel.STATUS_TMP.equals(old.getStatus())) {
            // 編集元文書の情報を引き継ぐ
            DocInfoModel pInfo = old.getDocInfo();
            document.setLinkId(old.getLinkId());
            document.setLinkRelation(old.getLinkRelation());
            //docInfo.setParentPk(pInfo.getParentPk());   // parentPk = linkId
            docInfo.setParentId(pInfo.getParentId());
            docInfo.setParentIdRelation(pInfo.getParentIdRelation());
            docInfo.setVersionNumber(pInfo.getVersionNumber());

            // 編集元は削除
            em.remove(old);

        } else {
            // 適合終了日を新しい版の確定日にする
            Date ended = document.getConfirmed();

            // オリジナルを取得し 終了日と status = M を設定する
            old.setEnded(ended);
            old.setStatus(InfoModel.STATUS_MODIFIED);

            // HibernateSearchのFulTextEntityManagerを用意. 修正済みのものはインデックスから削除する by masuda-sensei
            final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
            fullTextEntityManager.purge(DocumentModel.class, parentPk);

            // 関連するモジュールとイメージに同じ処理を実行する
            List<ModuleModel> oldModules = em.createQuery("select m from ModuleModel m where m.document.id = :id", ModuleModel.class)
                    .setParameter("id", parentPk).getResultList();

            oldModules.forEach(model -> {
                model.setEnded(ended);
                model.setStatus(InfoModel.STATUS_MODIFIED);
            });

            List<SchemaModel> oldImages = em.createQuery("select s from SchemaModel s where s.document.id = :id", SchemaModel.class)
                    .setParameter("id", parentPk).getResultList();

            oldImages.forEach(model -> {
                model.setEnded(ended);
                model.setStatus(InfoModel.STATUS_MODIFIED);
            });
        }
    }

    /**
     * ドキュメントを論理削除する.
     *
     * @param id 論理削除するドキュメントの primary key
     * @return 削除した件数
     */
    @Override
    public int deleteDocument(Long id) {

        //オリジナルでは修正したり仮保存をした文書を削除できないので改変 by masuda-sensei

        // 対象 Document を取得する
        DocumentModel target = em.find(DocumentModel.class, id);
        // その親文書を取得
        DocumentModel parent = getParent(target);
        // 関連するDocumentModelを再帰で取得する
        Set<DocumentModel> delSet = getChildren(parent);

        Date ended = new Date();

        for (DocumentModel delete : delSet) {

            long delId = delete.getId();

            // HibernateSearchのFulTextEntityManagerを用意. 削除済みのものはインデックスから削除する
            final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
            fullTextEntityManager.purge(DocumentModel.class, delId);

            if (InfoModel.STATUS_TMP.equals(delete.getStatus())) {
                // 仮文書の場合は抹消スル
                DocumentModel dm = em.find(DocumentModel.class, delId);
                em.remove(dm);

            } else {
                // 削除フラグをたてる
                delete.setStatus(InfoModel.STATUS_DELETE);
                delete.setEnded(ended);

                // 関連するモジュールに同じ処理を行う
                List<ModuleModel> deleteModules = em.createQuery("select m from ModuleModel m where m.document.id=:id", ModuleModel.class)
                        .setParameter("id", delId).getResultList();

                deleteModules.forEach(model -> {
                    model.setStatus(InfoModel.STATUS_DELETE);
                    model.setEnded(ended);
                });

                // 関連する画像に同じ処理を行う
                List<SchemaModel> deleteImages = em.createQuery("select i from SchemaModel i where i.document.id=:id", SchemaModel.class)
                        .setParameter("id", delId).getResultList();

                deleteImages.forEach(model -> {
                    model.setStatus(InfoModel.STATUS_DELETE);
                    model.setEnded(ended);
                });
            }
        }
        return 1;
    }

    /**
     * 親文書を追いかける by masuda-sensei.
     *
     * @param dm DocumentModel
     * @return 最親 DocumentModel
     */
    private DocumentModel getParent(DocumentModel dm) {

        long linkId = dm.getLinkId();
        DocumentModel model = dm;
        while (linkId != 0) {
            model = em.find(DocumentModel.class, linkId);
            linkId = model.getLinkId();
        }
        return model;
    }

    /**
     * 子文書を再帰で探す by masuda-sensei.
     *
     * @param parent 最親 DocumentModel
     * @return 子供達
     */
    private Set<DocumentModel> getChildren(DocumentModel parent) {

        Set<DocumentModel> ret = new HashSet<>();

        // 親を追加
        ret.add(parent);

        List<DocumentModel> children = em.createQuery("select d from DocumentModel d where d.linkId=:id", DocumentModel.class)
                .setParameter("id", parent.getId()).getResultList();

        // 子供の子供をリストに追加
        children.forEach(child -> ret.addAll(getChildren(child)));

        return ret;
    }

    /**
     * ドキュメントのタイトルを変更する.
     *
     * @param spec 変更するドキュメントの primary key, title
     * @return 変更した件数
     */
    @Override
    public int updateTitle(DocumentTitleSpec spec) {
        DocumentModel update = em.find(DocumentModel.class, spec.getDocumentPk());
        update.getDocInfo().setTitle(spec.getTitle());
        return 1;
    }

    /**
     * ModuleModel エントリを取得する.
     *
     * @param spec モジュール検索仕様
     * @return ModuleModel リストのリスト
     */
    @Override
    public List<List<ModuleModel>> getModuleList(ModuleSearchSpec spec) {

        // 抽出期間は別けられている
        Date[] fromDate = spec.getFromDate();
        Date[] toDate = spec.getToDate();
        int len = fromDate.length;
        List<List<ModuleModel>> ret = new ArrayList<>(len);

        // 抽出期間セットの数だけ繰り返す
        for (int i = 0; i < len; i++) {
            List<ModuleModel> modules = em.createQuery("select m from ModuleModel m where m.karte.id = :karteId and m.moduleInfo.entity = :entity and m.started between :fromDate and :toDate and m.status='F'", ModuleModel.class)
                    .setParameter("karteId", spec.getKarteId())
                    .setParameter("entity", spec.getEntity())
                    .setParameter("fromDate", fromDate[i])
                    .setParameter("toDate", toDate[i]).getResultList();
            // module 一つ一つ beanBytes を detach してから decode
            modules.forEach(m -> {
                em.detach(m);
                m.setModel((IInfoModel) ModelUtils.xmlDecode(m.getBeanBytes()));
                m.setBeanBytes(null);
            });
            ret.add(modules);
        }
        return ret;
    }

    /**
     * SchemaModel エントリを取得する.
     *
     * @param spec シェーマ検索仕様
     * @return SchemaModel エントリの配列
     */
    @Override
    public List<List<SchemaModel>> getImageList(ImageSearchSpec spec) {

        // 抽出期間は別けられている
        Date[] fromDate = spec.getFromDate();
        Date[] toDate = spec.getToDate();
        int len = fromDate.length;
        List<List<SchemaModel>> ret = new ArrayList<>(len);

        // 抽出期間セットの数だけ繰り返す
        for (int i = 0; i < len; i++) {
            List<SchemaModel> modules = em.createQuery("select i from SchemaModel i where i.karte.id = :karteId and i.started between :fromDate and :toDate and i.status='F'", SchemaModel.class)
                    .setParameter("karteId", spec.getKarteId())
                    .setParameter("fromDate", fromDate[i])
                    .setParameter("toDate", toDate[i]).getResultList();
            ret.add(modules);
        }
        return ret;
    }

    /**
     * 画像を取得する.
     *
     * @param id SchemaModel Id
     * @return SchemaModel
     */
    @Override
    public SchemaModel getImage(Long id) {
        return em.find(SchemaModel.class, id);
    }

    /**
     * 傷病名リストを取得する.
     *
     * @param spec 検索仕様
     * @return 傷病名のリスト
     */
    @Override
    public List<RegisteredDiagnosisModel> getDiagnosisList(DiagnosisSearchSpec spec) {

        List<RegisteredDiagnosisModel> ret;

        // ended が未設定 or 検索開始日より ended が新しいものを採択 (検索開始日より前に終了した病名を捨てる)
        if (spec.getFromDate() != null) {
            ret = em.createQuery("select r from RegisteredDiagnosisModel r where r.karte.id = :karteId and (r.ended is null or r.ended >= :fromDate)", RegisteredDiagnosisModel.class)
                    .setParameter("karteId", spec.getKarteId())
                    .setParameter("fromDate", spec.getFromDate()).getResultList();
        } else {
            // 全期間の傷病名を得る
            ret = em.createQuery("select r from RegisteredDiagnosisModel r where r.karte.id = :karteId", RegisteredDiagnosisModel.class)
                    .setParameter("karteId", spec.getKarteId()).getResultList();
        }
        return ret;
    }

    /**
     * 傷病名を追加する.
     *
     * @param addList 追加する傷病名のリスト
     * @return idのリスト
     */
    @Override
    public List<Long> addDiagnosisList(List<RegisteredDiagnosisModel> addList) {

        return addList.stream().map(m -> {
            em.persist(m);
            return m.getId();
        }).collect(Collectors.toList());
    }

    /**
     * 傷病名を更新する.
     *
     * @param updateList 更新傷病名のリスト
     * @return 更新数
     */
    @Override
    public int updateDiagnosisList(List<RegisteredDiagnosisModel> updateList) {
        updateList.forEach(em::merge);
        return updateList.size();
    }

    /**
     * 傷病名を削除する.
     *
     * @param removeList 削除する傷病名のidリスト
     * @return 削除数
     */
    @Override
    public int removeDiagnosisList(List<Long> removeList) {
        removeList.stream().map(id -> em.find(RegisteredDiagnosisModel.class, id)).forEach(em::remove);
        return removeList.size();
    }

    /**
     * Observation を取得する.
     *
     * @param spec 検索仕様
     * @return Observation のリスト
     */
    @Override
    public List<ObservationModel> getObservationList(ObservationSearchSpec spec) {

        List<ObservationModel> ret = null;
        String observation = spec.getObservation();
        String phenomenon = spec.getPhenomenon();
        Date firstConfirmed = spec.getFirstConfirmed();

        if (observation != null) {
            if (firstConfirmed != null) {
                ret = em.createQuery("select o from ObservationModel o where o.karte.id=:karteId and o.observation=:observation and o.started >= :firstConfirmed", ObservationModel.class)
                        .setParameter("karteId", spec.getKarteId())
                        .setParameter("observation", observation)
                        .setParameter("firstConfirmed", firstConfirmed).getResultList();

            } else {
                ret = em.createQuery("select o from ObservationModel o where o.karte.id=:karteId and o.observation=:observation", ObservationModel.class)
                        .setParameter("karteId", spec.getKarteId())
                        .setParameter("observation", observation).getResultList();
            }
        } else if (phenomenon != null) {
            if (firstConfirmed != null) {
                ret = em.createQuery("select o from ObservationModel o where o.karte.id=:karteId and o.phenomenon=:phenomenon and o.started >= :firstConfirmed", ObservationModel.class)
                        .setParameter("karteId", spec.getKarteId())
                        .setParameter("phenomenon", phenomenon)
                        .setParameter("firstConfirmed", firstConfirmed).getResultList();
            } else {
                ret = em.createQuery("select o from ObservationModel o where o.karte.id=:karteId and o.phenomenon=:phenomenon", ObservationModel.class)
                        .setParameter("karteId", spec.getKarteId())
                        .setParameter("phenomenon", phenomenon).getResultList();
            }
        }
        return ret;
    }

    /**
     * Observationを追加する.
     *
     * @param observations 追加する Observation のリスト
     * @return 追加した Observation のIdリスト
     */
    @Override
    public List<Long> addObservationList(List<ObservationModel> observations) {

        if (observations != null && !observations.isEmpty()) {
            return observations.stream().map(m -> {
                em.persist(m);
                return m.getId();
            }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Observation を更新する.
     *
     * @param observations 更新する Observation のリスト
     * @return 更新した数
     */
    @Override
    public int updateObservationList(List<ObservationModel> observations) {

        if (observations != null && !observations.isEmpty()) {
            observations.forEach(em::merge);
            return observations.size();
        }
        return 0;
    }

    /**
     * Observation を削除する.
     *
     * @param ids 削除する Observation の primary key リスト
     * @return 削除した数
     */
    @Override
    public int removeObservationList(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            ids.stream().map(id -> em.find(ObservationModel.class, id)).forEach(em::remove);
            return ids.size();
        }
        return 0;
    }

    /**
     * 患者メモを更新する.
     *
     * @param memo 更新するメモ
     * @return 更新した数 1
     */
    @Override
    public int updatePatientMemo(PatientMemoModel memo) {
        if (memo.getId() == 0L) {
            em.persist(memo);
        } else {
            em.merge(memo);
        }
        return 1;
    }

    /**
     * 予約を保存、更新、削除する.
     *
     * @param spec 予約情報の DTO
     */
    @Override
    public int putAppointment(AppointSpec spec) {

        Collection<AppointmentModel> added = spec.getAdded();
        Collection<AppointmentModel> updated = spec.getUpdated();
        Collection<AppointmentModel> removed = spec.getRemoved();

        int cnt = 0;

        // 登録する
        if (added != null && !added.isEmpty()) {
            added.forEach(em::persist);
            cnt += added.size();
        }

        // 更新する
        if (updated != null && !updated.isEmpty()) {
            updated.forEach(em::merge);
            cnt += updated.size();
        }

        // 削除
        if (removed != null && !removed.isEmpty()) {
            // 分離オブジェクトは remove に渡せないので対象を検索する
            removed.stream().map(m -> em.find(AppointmentModel.class, m.getId())).forEach(em::remove);
            cnt += removed.size();
        }
        return cnt;
    }

    /**
     * 予約を検索する.
     *
     * @param spec 検索仕様
     * @return 予約の List
     */
    @Override
    public List<List<AppointmentModel>> getAppointmentList(ModuleSearchSpec spec) {

        // 抽出期間は別けられている
        Date[] fromDate = spec.getFromDate();
        Date[] toDate = spec.getToDate();
        int len = fromDate.length;

        List<List<AppointmentModel>> ret = new ArrayList<>(len);

        // 抽出期間ごとに検索しコレクションに加える
        for (int i = 0; i < len; i++) {

            List<AppointmentModel> c = em.createQuery("select a from AppointmentModel a where a.karte.id = :karteId and a.date between :fromDate and :toDate", AppointmentModel.class)
                    .setParameter("karteId", spec.getKarteId())
                    .setParameter("fromDate", fromDate[i])
                    .setParameter("toDate", toDate[i]).getResultList();
            ret.add(c);
        }
        return ret;
    }
}
