package open.dolphin.impl.psearch;

import open.dolphin.delegater.PatientDelegater;
import open.dolphin.dto.PatientSearchSpec;
import open.dolphin.helper.Task;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.ui.ObjectReflectTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 検索タスクの実務をするクラス
 *
 * @author pns
 */
class FindTask extends Task<Collection> {

    private final PatientSearchPanel view;
    private final ObjectReflectTableModel<PatientModel> tableModel;
    private final PatientSearchSpec spec;
    private final Logger logger = LoggerFactory.getLogger(FindTask.class);
    private List<PatientModel> result;

    /**
     * メモ検索 or 全文検索のコンストラクタ
     *
     * @param view PatientSearchPanel
     * @param message message
     * @param note note
     * @param spec patient search spec
     */
    @SuppressWarnings("unchecked")
    public FindTask(PatientSearchPanel view, Object message, String note, PatientSearchSpec spec) {
        super(SwingUtilities.getWindowAncestor(view), message, note);
        this.view = view;
        this.spec = spec;
        tableModel = (ObjectReflectTableModel<PatientModel>) view.getTable().getModel();
    }

    @Override
    protected Collection doInBackground() {
        logger.debug("FindTask doInBackground");

        // auto narrowing search
        // table のリストが空の場合は全患者から，リストがあればその中で検索する
        // そのために，spec に PatientModel の id をセットしておく

        List<Long> ids = new ArrayList<>();

        if (view.getNarrowingSearchCb().isSelected()) {
            List<PatientModel> ptOnTable = tableModel.getObjectList();
            ptOnTable.stream().forEach(patientModel -> ids.add(patientModel.getId()));
        }
        spec.setNarrowingIds(ids);

        PatientDelegater pdl = new PatientDelegater();
        List<PatientModel> pm = pdl.getPatients(spec);

        // カルテ検索で薬の名前を検索すると患者名と判断されてしまうので，
        // 検索結果が 0 だったら，full text search に切り替えることにする
        if (pm.isEmpty() && (
                spec.getCode() == PatientSearchSpec.KANA_SEARCH
                        || spec.getCode() == PatientSearchSpec.ROMAN_SEARCH
                        || spec.getCode() == PatientSearchSpec.NAME_SEARCH)) {

            spec.setCode(PatientSearchSpec.FULL_TEXT_SEARCH);
            spec.setSearchText(spec.getName());
            pm = pdl.getPatients(spec);
        }

        result = new ArrayList<>();
        result.addAll(pm);

        return Collections.unmodifiableCollection(result);
    }

    @Override
    protected void cancelled() {
        unfinished();
    }

    @Override
    protected void interrupted(InterruptedException ex) {
        unfinished();
    }

    @Override
    protected void failed(Throwable cause) {
        unfinished();
    }

    @Override
    protected void succeeded(Collection result) {
        setResult();
        logger.debug("FindTask succeeded");
    }

    private void unfinished() {
        // 途中経過の書き込み
        if (tableModel.getObjectCount() == 0) {
            setResult();
        }
    }

    /**
     * table に result をセットする.
     */
    protected void setResult() {

        tableModel.setObjectList(result);

        // 件数表示
        int cnt = result != null ? result.size() : 0;
        String cntStr = String.valueOf(cnt);
        view.getCntLbl().setText(cntStr + " 件");
    }
}
