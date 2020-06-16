package open.dolphin.client;

import open.dolphin.helper.PNSTriple;
import open.dolphin.infomodel.DiagnosisCategoryModel;
import open.dolphin.infomodel.DiagnosisLiteModel;
import open.dolphin.infomodel.DiagnosisOutcomeModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.project.Project;
import open.dolphin.ui.ObjectReflectTableModel;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

/**
 * DiagnosisDocumentTableModel を独立させて，undo/redo に対応した.
 *
 * @author pns
 */
public class DiagnosisDocumentTableModel extends ObjectReflectTableModel<RegisteredDiagnosisModel> {
    private static final long serialVersionUID = 1L;

    private final boolean isReadOnly;
    private final PropertyChangeSupport boundSupport = new PropertyChangeSupport(new Object());
    // undo/redo 用 map（rd ごとに queue を作っておく）
    private final Map<Integer, Deque<DiagnosisLiteModel>> undoMap = new HashMap<>();
    private final Map<Integer, Deque<DiagnosisLiteModel>> redoMap = new HashMap<>();
    private DiagnosisOutcomeGenerator outcomeGenerator = new DiagnosisOutcomeGenerator();
    private DiagnosisDocumentTable diagTable;
    private LastVisit lastVisit;

    public DiagnosisDocumentTableModel(List<PNSTriple<String, Class<?>, String>> triples, boolean readOnly) {
        super(triples);
        isReadOnly = readOnly;
    }

    public PropertyChangeSupport getBoundSupport() {
        return boundSupport;
    }

    /**
     * LastVisit をセットする.
     *
     * @param lastVisit LastVisit
     */
    public void setLastVisit(LastVisit lastVisit) { this.lastVisit = lastVisit; }

    /**
     * この Model を保持する DiagnosisDocumentTable をセットする.
     *
     * @param table DiagnosisDocumentTable
     */
    public void setDiagTable(DiagnosisDocumentTable table) { diagTable = table; }

    // Diagnosisは編集不可
    @Override
    public boolean isCellEditable(int row, int col) {
        // licenseCodeで制御
        if (isReadOnly) {
            return false;
        }

        // 病名レコードが存在しない場合は false
        RegisteredDiagnosisModel rd = getObject(row);
        if (rd == null) {
            return false;
        }

        // ORCA に登録されている病名の場合
        if (rd.getStatus() != null && rd.getStatus().equals(DiagnosisDocument.ORCA_RECORD)) {
            return false;
        }

        // 診断名 column は直接編集不可（編集は popup，エディタでの編集はできる）
        if (col == DiagnosisDocument.DIAGNOSIS_COL) {
            return false;
        }

        // DELETED_RECORD フラグが立っていたら cell editor による編集不可
        return !DiagnosisDocument.DELETED_RECORD.equals(rd.getStatus());
    }

    // オブジェクトの値を設定する
    @Override
    public void setValueAt(Object value, int row, int col) {

        RegisteredDiagnosisModel rd = getObject(row);

        if (value == null || rd == null) {
            return;
        }
        String status = rd.getStatus();
        if (status != null && status.equals(DiagnosisDocument.ORCA_RECORD)) {
            return;
        }

        // value = DELETED_RECORD で呼ばれた場合は DELETED_RECORD をセットする
        if (DiagnosisDocument.DELETED_RECORD.equals(value)) {
            // undo 用に保存
            offerQueue(undoMap, rd);
            // rd 書き換え
            rd.setStatus(DiagnosisDocument.DELETED_RECORD);
            update(row, rd);
            return;
        }

        // DELETED_RECORD フラグが立っていたらポップアップ編集も不可
        if (DiagnosisDocument.DELETED_RECORD.equals(rd.getStatus())) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        switch (col) {
            case DiagnosisDocument.DIAGNOSIS_COL:
                // JTextField から入ってきた String 分は無視
                if (value instanceof DiagnosisLiteModel) {
                    DiagnosisLiteModel newDiag = (DiagnosisLiteModel) value;
                    // 変更されていたら更新する
                    if (!rd.getDiagnosis().equals(newDiag.getDiagnosisDesc()) || !rd.getDiagnosisCode().equals(newDiag.getDiagnosisDesc())) {
                        // undo 用に保存
                        offerQueue(undoMap, rd);
                        // rd 書き換え
                        rd.setDiagnosis(newDiag.getDiagnosisDesc());
                        rd.setDiagnosisCode(newDiag.getDiagnosisCode());
                        update(row, rd);
                    }
                }
                break;

            case DiagnosisDocument.CATEGORY_COL:
                // JComboBox から選択
                String saveCategory = rd.getCategory();
                DiagnosisCategoryModel dcm = (DiagnosisCategoryModel) value;
                String test = dcm.getDiagnosisCategory();
                test = test != null && (!test.equals("")) ? test : null;
                if (saveCategory != null) {
                    if (test != null) {
                        if (!test.equals(saveCategory)) {
                            // undo 用に保存
                            offerQueue(undoMap, rd);
                            rd.setCategory(dcm.getDiagnosisCategory());
                            rd.setCategoryDesc(dcm.getDiagnosisCategoryDesc());
                            rd.setCategoryCodeSys(dcm.getDiagnosisCategoryCodeSys());
                            update(row, rd);
                        }
                    } else {
                        // undo 用に保存
                        offerQueue(undoMap, rd);
                        rd.setDiagnosisCategoryModel(null);
                        update(row, rd);
                    }

                } else {
                    if (test != null) {
                        // undo 用に保存
                        offerQueue(undoMap, rd);
                        rd.setCategory(dcm.getDiagnosisCategory());
                        rd.setCategoryDesc(dcm.getDiagnosisCategoryDesc());
                        rd.setCategoryCodeSys(dcm.getDiagnosisCategoryCodeSys());
                        update(row, rd);
                    }
                }
                break;

            case DiagnosisDocument.OUTCOME_COL:
                // JComboBox から選択
                String saveOutcome = rd.getOutcome();
                DiagnosisOutcomeModel dom = (DiagnosisOutcomeModel) value;
                test = dom.getOutcome();
                test = test != null && (!test.equals("")) ? test : null;
                    if (test != null) {
                        if (Objects.isNull(saveOutcome) || !saveOutcome.equals(test)) {
                            // undo 用に保存
                            offerQueue(undoMap, rd);
                            rd.setOutcome(dom.getOutcome());
                            rd.setOutcomeDesc(dom.getOutcomeDesc());
                            rd.setOutcomeCodeSys(dom.getOutcomeCodeSys());
                            // 疾患終了日を入れる
                            if (Project.getPreferences().getBoolean("autoOutcomeInput", false)) {
                                String val = rd.getEndDate();
                                if (StringUtils.isEmpty(val)) {
                                    outcomeGenerator.setParams(rd, lastVisit);
                                    if (dom.getOutcome().equals(DiagnosisOutcome.fullyRecovered.name())) {
                                        // 終了の場合
                                        rd.setEndDate(outcomeGenerator.standard());
                                    } else if (dom.getOutcome().equals(DiagnosisOutcome.pause.name())) {
                                        // 中止の場合
                                        rd.setEndDate(outcomeGenerator.special());
                                    }
                                }
                            }
                            update(row, rd);
                        }
                    } else {
                        // 転帰が消去された場合は新規病名として登録し直すことにした
                        RegisteredDiagnosisModel newRd = new RegisteredDiagnosisModel();
                        newRd.setDiagnosis(rd.getDiagnosis());
                        newRd.setDiagnosisCode(rd.getDiagnosisCode());
                        newRd.setDiagnosisCodeSystem(rd.getDiagnosisCodeSystem());
                        newRd.setCategory(rd.getCategory());
                        newRd.setCategoryDesc(rd.getCategoryDesc());
                        newRd.setCategoryCodeSys(rd.getCategoryCodeSys());

                        insert(newRd);
                    }
                break;

            case DiagnosisDocument.START_DATE_COL:
                String strVal = (String) value;
                test = rd.getStartDate();
                if (test == null || !test.equals(strVal)) {
                    rd.setStartDate(strVal);
                    update(row, rd);
                }
                break;

            case DiagnosisDocument.END_DATE_COL:
                strVal = (String) value;
                test = rd.getEndDate();
                if (test == null || !test.equals(strVal)) {
                    rd.setEndDate(strVal);
                    update(row, rd);
                }
                break;
        }
    }

    /**
     * DiagnosisDocument の propertyChange を呼び出す
     *
     * @param row
     * @param rd
     */
    private void update(int row, RegisteredDiagnosisModel rd) {
        fireTableRowsUpdated(row, row);
        boundSupport.firePropertyChange(DiagnosisDocument.ADD_UPDATED_LIST, "oldValue", rd);
    }

    /**
     * DiagnosisDocument の insertDiagnosis を呼び出す
     *
     * @param rd
     */
    private void insert(RegisteredDiagnosisModel rd) {
        boundSupport.firePropertyChange(DiagnosisDocument.ADD_ADDED_LIST, "oldValue", rd);
    }

    /**
     * 加えられた病名を undo で delete するために undo queue に deleted recored を積む
     *
     * @param rd
     */
    private void addDeletedRecordForUndo(RegisteredDiagnosisModel rd) {
        String org = rd.getStatus();
        rd.setStatus(DiagnosisDocument.DELETED_RECORD);
        offerQueue(undoMap, rd);
        rd.setStatus(org);
    }

    /**
     * 加えられた病名は undo で delete されるようにする
     *
     * @param row
     * @param rd
     */
    @Override
    public void insertRow(int row, RegisteredDiagnosisModel rd) {
        super.insertRow(row, rd);
        addDeletedRecordForUndo(rd);
    }

    @Override
    public void addRow(RegisteredDiagnosisModel rd) {
        super.addRow(rd);
        addDeletedRecordForUndo(rd);
    }

    /**
     * 選択された行の undo ： DiagnosisDocument から undo メニューで呼ばれる
     */
    public void undo() {
        int[] rows = diagTable.getSelectedRows();
        for (int r : rows) {
            int row = diagTable.convertRowIndexToModel(r);
            RegisteredDiagnosisModel rd = getObject(row);
            offerQueue(redoMap, rd);
            if (pollQueue(undoMap, rd) == PollResult.succeeded) {
                update(row, rd);
            } else {
                cancelOffer(redoMap, rd);
            } // poll に失敗した場合は offer した分は取り消す
        }
    }

    /**
     * 選択された行の redo ： DiagnosisDocument から redo メニューで呼ばれる
     */
    public void redo() {
        int[] rows = diagTable.getSelectedRows();
        for (int r : rows) {
            int row = diagTable.convertRowIndexToModel(r);
            RegisteredDiagnosisModel rd = getObject(row);
            offerQueue(undoMap, rd);
            if (pollQueue(redoMap, rd) == PollResult.succeeded) {
                update(row, rd);
            } else {
                cancelOffer(undoMap, rd);
            } // poll に失敗したときは，offer した分は取り消す
        }
    }

    public boolean isUndoable(RegisteredDiagnosisModel rd) {
        Deque<DiagnosisLiteModel> dq = undoMap.get(System.identityHashCode(rd));
        return (dq != null && !dq.isEmpty());
    }

    public boolean isRedoable(RegisteredDiagnosisModel rd) {
        Deque<DiagnosisLiteModel> dq = redoMap.get(System.identityHashCode(rd));
        return (dq != null && !dq.isEmpty());
    }

    /**
     * Queue にためる
     * id は system hash を使う（厳密に一意ではないが重なる可能性はほとんど無い）
     *
     * @param dequeMap
     * @param rd
     */
    private void offerQueue(Map<Integer, Deque<DiagnosisLiteModel>> dequeMap, RegisteredDiagnosisModel rd) {
        int id = System.identityHashCode(rd);
        //Deque<DiagnosisLiteModel> dq = dequeMap.get(id);
        //if (dq == null) {
        //    dq = new LinkedList<>();
        //    dequeMap.put(id, dq);
        //}
        //dq.offerFirst(new DiagnosisLiteModel(rd));
        dequeMap.computeIfAbsent(id, key -> new LinkedList<>());
        dequeMap.get(id).offerFirst(new DiagnosisLiteModel(rd));
    }

    /**
     * Queue から取り出す　成功：true　取り出すもの無し：false
     *
     * @param dequeMap
     * @param rd
     * @return
     */
    private PollResult pollQueue(Map<Integer, Deque<DiagnosisLiteModel>> dequeMap, RegisteredDiagnosisModel rd) {
        Deque<DiagnosisLiteModel> dq = dequeMap.get(System.identityHashCode(rd));
        if (dq != null) {
            DiagnosisLiteModel set = dq.pollFirst();
            if (set != null) {
                set.resume(rd);
                return PollResult.succeeded;
            }
        }
        return PollResult.noMore;
    }

    /**
     * Queue に積んだ分を取り消す
     *
     * @param dequeMap
     * @param rd
     */
    private void cancelOffer(Map<Integer, Deque<DiagnosisLiteModel>> dequeMap, RegisteredDiagnosisModel rd) {
        Deque<DiagnosisLiteModel> dq = dequeMap.get(System.identityHashCode(rd));
        if (dq != null) {
            dq.removeFirst();
        }
    }

    private enum PollResult {succeeded, noMore}
}
