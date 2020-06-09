package open.dolphin.impl.care;

import open.dolphin.calendar.CalendarEvent;
import open.dolphin.calendar.CalendarListener;
import open.dolphin.calendar.CalendarTable;
import open.dolphin.calendar.CalendarTableModel;
import open.dolphin.client.Chart;
import open.dolphin.client.ImageEntry;
import open.dolphin.event.ProxyAction;
import open.dolphin.infomodel.AppointmentModel;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.SimpleDate;
import open.dolphin.dnd.DolphinTransferHandler;
import open.dolphin.util.MMLDate;
import open.dolphin.util.ModelUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * SimpleCalendarPanel.
 *
 * @author Kazushi Minagawa
 * @author pns
 */
public final class SimpleCalendarPanel extends JPanel {
    private static final long serialVersionUID = 3030024622746649784L;
    // MmlDate 型式の日付をキー，AppointmentModel を value とする HashMap
    private final HashMap<String, AppointmentModel> map = new HashMap<>();
    private SimpleDate today;
    private int relativeMonth;
    private CalendarTable table;
    private CalendarTableModel tableModel;
    private Chart context;
    private CareMapDocument parent;
    private boolean dirty;

    private CalendarListener listener;

    public SimpleCalendarPanel() {
        this(0);
    }

    public SimpleCalendarPanel(int n) {
        // 今月を基点とした相対月数
        relativeMonth = n;
        init();
    }

    private void init() {
        // Get right now
        today = new SimpleDate(new GregorianCalendar());
        GregorianCalendar gc = new GregorianCalendar(today.getYear(), today.getMonth(), today.getDay());

        // Create requested month calendar
        // Add relative number to create
        gc.add(Calendar.MONTH, relativeMonth);

        table = new CalendarTable(gc);
        tableModel = (CalendarTableModel) table.getModel();

        CalendarTableTransferHandler th = new CalendarTableTransferHandler();
        table.setTransferHandler(th);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    // 取り消しポップアップ
                    doPopup(e);

                } else {
                    // ドラッグ開始
                    th.exportAsDrag(table, e, TransferHandler.MOVE);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Windows
                if (e.isPopupTrigger()) {
                    // 取り消しポップアップ
                    doPopup(e);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                int col = table.columnAtPoint(p);
                if (row != -1 && col != -1) {
                    SimpleDate date = (SimpleDate) table.getValueAt(row, col);
                    listener.dateSelected(date);
                }
            }
        });

        setLayout(new BorderLayout());
        add(table.getTitledPanel(), BorderLayout.CENTER);
    }

    /**
     * ChartImpl を保存，誕生日を登録.
     *
     * @param context
     */
    public void setChartContext(Chart context) {
        this.context = context;
        String mmlBirthday = context.getPatient().getBirthday();
        tableModel.setBirthday(mmlBirthday);
    }

    /**
     * 親の ChartDocument を登録する.
     *
     * @param doc
     */
    public void setParent(CareMapDocument doc) {
        parent = doc;
    }

    /**
     * CalendarTableModel を返す.
     *
     * @return
     */
    public CalendarTableModel getTableModel() {
        return tableModel;
    }

    /**
     * 今月からの相対月数を返す.
     *
     * @return
     */
    public int getRelativeMonth() {
        return relativeMonth;
    }

    /**
     * 今月かどうかを返す.
     *
     * @return
     */
    public boolean isThisMonth() {
        return relativeMonth == 0;
    }

    /**
     * CalendarListener を登録する.
     *
     * @param l
     */
    public void addCalendarListener(CalendarListener l) {
        listener = l;
    }

    /**
     * この月の初日を MML 型式で返す.
     *
     * @return
     */
    public String getFirstDate() {
        int year = tableModel.getYear();
        int month = tableModel.getMonth();
        GregorianCalendar firstDay = new GregorianCalendar(year, month, 1);

        return MMLDate.getDate(firstDay);
    }

    /**
     * この月の末日を MML 型式で返す.
     *
     * @return
     */
    public String getLastDate() {
        int year = tableModel.getYear();
        int month = tableModel.getMonth();
        GregorianCalendar gc = new GregorianCalendar(year, month, 1);
        int days = gc.getActualMaximum(Calendar.DAY_OF_MONTH);
        gc.add(Calendar.DAY_OF_MONTH, days - 1);

        return MMLDate.getDate(gc);
    }

    /**
     * MmlDate に対応する AppointmentModel を返す.
     *
     * @param mmlDate
     * @return
     */
    public AppointmentModel getAppointmentModel(String mmlDate) {
        return map.get(mmlDate);
    }

    /**
     * この月の有効な AppointmentModel のリストを返す.
     *
     * @return 予約日リスト
     */
    public List<AppointmentModel> getAppointDays() {

        List<AppointmentModel> results = new ArrayList<>();

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                SimpleDate date = (SimpleDate) table.getValueAt(row, col);

                if (date.getYear() == tableModel.getYear() && date.getMonth() == tableModel.getMonth()) {
                    String mmlDate = SimpleDate.simpleDateToMmldate(date);

                    AppointmentModel appoint = map.get(mmlDate);

                    // 取り消し済み予約は appoint.getName = null になっている
                    if (appoint != null && appoint.getName() != null) {
                        results.add(appoint);
                    }
                }
            }
        }

        return results;
    }

    /**
     * この月の更新された AppointModel のリストを返す.
     *
     * @return 更新された予約のリスト
     */
    public List<AppointmentModel> getUpdatedAppoints() {

        List<AppointmentModel> results = new ArrayList<>();

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                SimpleDate date = (SimpleDate) table.getValueAt(row, col);

                if (date.getYear() == tableModel.getYear() && date.getMonth() == tableModel.getMonth()) {
                    String mmlDate = SimpleDate.simpleDateToMmldate(date);

                    AppointmentModel appoint = map.get(mmlDate);

                    if (appoint != null && appoint.getState() != AppointmentModel.TT_NONE) {
                        results.add(appoint);
                    }
                }
            }
        }

        return results;
    }

    /**
     * ModuleModel のリストを登録する.
     *
     * @param event
     * @param list
     */
    public void setModuleList(String event, List<ModuleModel> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        list.forEach(module -> {
            String mmlDate = ModelUtils.getDateAsString(module.getConfirmed());
            SimpleDate date = SimpleDate.mmlDateToSimpleDate(mmlDate);

            date.setEventCode(event);

            // CalendarTableModel で row, col は使ってない
            table.setValueAt(date, 0, 0);
        });

        tableModel.fireTableDataChanged();
    }

    /**
     * ImageEntry のリストを登録する.
     *
     * @param event
     * @param list
     */
    public void setImageList(String event, List<ImageEntry> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        list.forEach(entry -> {
            String mmlDate = entry.getConfirmDate();
            SimpleDate date = SimpleDate.mmlDateToSimpleDate(mmlDate);

            date.setEventCode(event);

            // CalendarTableModel で row, col は使ってない
            table.setValueAt(date, 0, 0);
        });

        tableModel.fireTableDataChanged();
    }

    /**
     * AppointModel のリストを登録する.
     *
     * @param list
     */
    public void setAppointmentList(List<AppointmentModel> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        list.forEach(appoint -> {
            appoint.setState(AppointmentModel.TT_HAS);
            String mmlToday = SimpleDate.simpleDateToMmldate(today);
            String mmlAppointDate = ModelUtils.getDateAsString(appoint.getDate());

            // 今日以降のものだけ登録
            if (mmlAppointDate.compareTo(mmlToday) >= 0) {
                SimpleDate date = SimpleDate.mmlDateToSimpleDate(mmlAppointDate);
                date.setEventCode(CalendarEvent.getCode(appoint.getName()));

                map.put(mmlAppointDate, appoint);
                table.setValueAt(date, 0, 0);
            }
        });

        tableModel.fireTableDataChanged();
    }

    /**
     * 取り消しのポップアップを出す.
     *
     * @param e
     */
    private void doPopup(MouseEvent e) {

        // ReadOnly 時の予約は不可
        if (context.isReadOnly()) {
            return;
        }

        int row = table.rowAtPoint(e.getPoint());
        int col = table.columnAtPoint(e.getPoint());
        if (row == -1 || col == -1) {
            return;
        }

        SimpleDate date = (SimpleDate) table.getValueAt(row, col);
        AppointmentModel appoint = map.get(SimpleDate.simpleDateToMmldate(date));

        // 予約のない日. popup menu がキャンセルのみなので
        if (appoint == null) {
            return;
        }

        // 本日以前の予約は不可
        if (today.compareTo(date) >= 0) {
            return;
        }

        // Embed popup menu
        JPopupMenu appointMenu = new JPopupMenu();
        JMenuItem item = new JMenuItem(new ProxyAction("取り消し", () -> processCancel(row, col)));
        appointMenu.add(item);

        appointMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * 予約を設定する.
     *
     * @param row
     * @param col
     * @param appointName
     * @param memo
     */
    private void processAppoint(int row, int col, String appointName, String memo) {

        SimpleDate date = (SimpleDate) table.getValueAt(row, col);
        String mmlDate = SimpleDate.simpleDateToMmldate(date);
        AppointmentModel appoint = map.get(mmlDate);

        if (appoint == null) {
            appoint = new AppointmentModel();
            appoint.setDate(ModelUtils.getDateAsObject(mmlDate));
            map.put(mmlDate, appoint);
        }

        int oldState = appoint.getState();
        int next = 0;
        switch (oldState) {

            case AppointmentModel.TT_NONE:
                next = AppointmentModel.TT_NEW;
                break;

            case AppointmentModel.TT_NEW:
                next = AppointmentModel.TT_NEW;
                break;

            case AppointmentModel.TT_HAS:
                next = AppointmentModel.TT_REPLACE;
                break;

            case AppointmentModel.TT_REPLACE:
                next = AppointmentModel.TT_REPLACE;
                break;
        }
        appoint.setState(next);
        appoint.setName(appointName);
        appoint.setMemo(memo);
        date.setEventCode(CalendarEvent.getCode(appointName));

        tableModel.fireTableCellUpdated(row, col);
        listener.dateSelected(date);

        if (!dirty) {
            dirty = true;
            parent.setDirty(dirty);
        }
    }

    /**
     * 予約をキャンセルする.
     *
     * @param row
     * @param col
     */
    private void processCancel(int row, int col) {

        SimpleDate date = (SimpleDate) table.getValueAt(row, col);
        AppointmentModel appoint = map.get(SimpleDate.simpleDateToMmldate(date));

        if (appoint == null) {
            return;
        }

        int oldState = appoint.getState();
        int nextState = 0;

        switch (oldState) {
            case AppointmentModel.TT_NONE:
            case AppointmentModel.TT_NEW:
                nextState = AppointmentModel.TT_NONE;
                break;

            case AppointmentModel.TT_HAS:
            case AppointmentModel.TT_REPLACE:
                nextState = AppointmentModel.TT_REPLACE;
                break;
        }

        // 変更されたことを記憶するために，キャンセルされる前の AppointMentModel を残す
        appoint.setState(nextState);
        appoint.setName(null);
        date.setEventCode(null);

        tableModel.fireTableCellUpdated(row, col);
        listener.dateSelected(date);

        if (!dirty) {
            dirty = true;
            parent.setDirty(dirty);
        }
    }

    /**
     * 予約の TransferHandler.
     */
    private class CalendarTableTransferHandler extends DolphinTransferHandler {
        private static final long serialVersionUID = 1L;

        private int srcRow;
        private int srcCol;
        private SimpleDate srcDate;

        @Override
        public int getSourceActions(JComponent c) {
            srcRow = table.getSelectedRow();
            srcCol = table.getSelectedColumn();
            srcDate = (SimpleDate) table.getValueAt(srcRow, srcCol);

            setDragImage(getVisualRepresentation());

            return MOVE;
        }

        /**
         * 半透明の visual representation を返す.
         * table からドラッグ該当部分のイメージを切り出して使う.
         *
         * @return
         */
        private Image getVisualRepresentation() {
            int width = table.getWidth();
            int height = table.getHeight();
            int x = srcCol * width / 7;
            int y = srcRow * height / 6;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            table.paint(image.getGraphics());
            image = image.getSubimage(x, y, width / 7, height / 6);

            return image;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            if (srcRow == -1 || srcCol == -1) {
                return null;
            }

            SimpleDate date = (SimpleDate) table.getValueAt(srcRow, srcCol);
            String mmlDate = SimpleDate.simpleDateToMmldate(date);
            AppointmentModel appo = map.get(mmlDate);

            // appo がないか，変更されて name = null になっている場合
            if (appo == null || appo.getName() == null) {
                return null;
            }

            return new AppointEntryTransferable(appo);
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            support.setShowDropLocation(true);

            Point loc = support.getDropLocation().getDropPoint();
            int row = table.rowAtPoint(loc);
            int col = table.columnAtPoint(loc);

            if (row == -1 || col == -1) {
                return false;
            }

            SimpleDate date = (SimpleDate) table.getValueAt(row, col);

            // 自分?
            if (date.equals(srcDate)) {
                return false;
            }

            // outOfMonth ?
            if (tableModel.getYear() != date.getYear() || tableModel.getMonth() != date.getMonth()) {
                return false;
            }

            // 本日以前?
            return today.compareTo(date) < 0;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            try {
                Transferable tr = support.getTransferable();
                AppointmentModel appoint = (AppointmentModel) tr.getTransferData(AppointEntryTransferable.appointFlavor);

                Point loc = support.getDropLocation().getDropPoint();
                int row = table.rowAtPoint(loc);
                int col = table.columnAtPoint(loc);

                if (row != -1 && col != -1) {
                    processAppoint(row, col, appoint.getName(), appoint.getMemo());
                    return true;

                } else {
                    return false;
                }

            } catch (UnsupportedFlavorException | IOException ue) {
                System.out.println(ue);
                return false;
            }
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            if (action == MOVE) {
                // 移動の場合ソースは消す
                processCancel(srcRow, srcCol);

                // リセット
                srcRow = -1;
                srcCol = -1;
                srcDate = null;
            }
        }
    }
}
