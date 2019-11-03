package open.dolphin.impl.pvt;

import open.dolphin.infomodel.KarteState;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.ui.ObjectReflectTableModel;
import open.dolphin.util.ModelUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.util.Date;

/**
 * @author kazm
 * @author pns
 */
public class RowTipsTable extends JTable {
    private static final long serialVersionUID = 1L;

    public RowTipsTable() {
        // フォーカス取ったら選択する
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getRowCount() > 0 && getSelectedRow() < 0) {
                    getSelectionModel().setSelectionInterval(0, 0);
                }
            }
        });
    }

    @Override
    public String getToolTipText(MouseEvent e) {

        ObjectReflectTableModel model = (ObjectReflectTableModel) getModel();
        int row = rowAtPoint(e.getPoint());
        int col = columnAtPoint(e.getPoint());
        PatientVisitModel pvt = (PatientVisitModel) model.getObject(row);

        if (pvt == null) {
            return null;
        }

        String text = null;

        switch (col) {
            case WaitingListImpl.BIRTHDAY_COLUMN:
                // 生年月日
                text = pvt.getPatientBirthday();
                break;

            case WaitingListImpl.AGE_COLUMN:
                // 年齢
                String[] age = pvt.getPatientAge().split("\\.");
                text = String.format("%s 歳 %s ヶ月", age[0], age[1]);
                break;

            default:
                Date pvtDate = ModelUtils.getDateTimeAsObject(pvt.getPvtDate());
                int pvtState = pvt.getState();
                if (pvtDate != null && (pvtState == KarteState.CLOSE_NONE || pvtState == KarteState.OPEN_NONE)) {
                    text = pvt.getPatient().getKanaName();
                    text += " - 待ち時間 " + DurationFormatUtils.formatPeriod(pvtDate.getTime(), new Date().getTime(), "HH:mm");
                }
        }

        return text;
    }
}
