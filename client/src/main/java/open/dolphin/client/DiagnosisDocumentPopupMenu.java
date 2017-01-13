package open.dolphin.client;

import java.awt.Component;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import open.dolphin.infomodel.*;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.MyJPopupMenu;

/**
 * DiagnosisDocument のポップアップメニューを一手に引き受ける
 * @author pns
 */
public class DiagnosisDocumentPopupMenu extends MouseAdapter implements MouseMotionListener,PropertyChangeListener {

    private DiagnosisDocument diagnosisDocument;
    private DiagnosisDocumentTable diagTable;
    private DiagnosisDocumentTableModel diagTableModel;

    private JTextField textField;
    private JTextField startDateField;
    private JTextField endDateField;
    private DiagnosisDocument parent;

    private int[] lastVisitYmd;
    private MyJPopupMenu calendarPopup;
    private MyJPopupMenu diagPopup;
    private MyJPopupMenu categoryPopup;
    private MyJPopupMenu outcomePopup;
    private int targetColumn;

    private PropertyChangeSupport boundSupport;

    // 病名修飾語リスト
    private enum DiagnosisPreposition {
        右("2056"), 左("2049"), 両 ("2057");
        private final String code;
        private DiagnosisPreposition(String code) { this.code = code; }
        public String desc() { return name(); }
        public String code() { return code; }
    }

    private enum DiagnosisPostposition {
        の急性増悪("8061"), の二次感染("8069"),の再発("8065"), の術後("8048"),の治療後("8075");
        private final String code;
        private DiagnosisPostposition(String code) { this.code = code; }
        public String desc() { return name(); }
        public String code() { return code; }
    }

    public DiagnosisDocumentPopupMenu(DiagnosisDocument parent) {
        super();
        this.parent = parent;
        diagnosisDocument = parent;
        createDiagnosisPopupMenu();
        createCategoryPopupMenu();
        createOutcomePopupMenu();

        boundSupport = new PropertyChangeSupport(new Object());
        diagTable = parent.getDiagnosisTable();
        diagTableModel = (DiagnosisDocumentTableModel) diagTable.getModel();
        startDateField = parent.getStartDateField();
        endDateField = parent.getEndDateField();
        lastVisitYmd = parent.getLastVisitYmd();

        addMouseListeners();
    }

    // コンストラクタ内で this を使うのは危険らしい
    private void addMouseListeners() {
        diagTable.addMouseListener(this);
        startDateField.addMouseListener(this);
        endDateField.addMouseListener(this);
    }

    public PropertyChangeSupport getBoundSupport() {
        return boundSupport;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // マウスがクリックされた column を記録
        targetColumn = diagTable.columnAtPoint(e.getPoint());

        if (e.isPopupTrigger()) { rightPressed(e); }
        else { leftPressed(e); }
    }

    /**
     * 右クリックの処理
     * @param e
     */
    private void rightPressed(MouseEvent e) {

        // マウス位置の行が選択されていなければ，選択し直す
        boolean shouldReselect = true;
        int[] selectedRows = diagTable.getSelectedRows();
        int row = diagTable.rowAtPoint(e.getPoint());
        for (int r : selectedRows) {
            // 選択とマウスが一致している場合
            if (r == row) {
                shouldReselect = false;
                break;
            }
        }
        if (shouldReselect) {
            Focuser.requestFocus(diagTable);
            diagTable.getSelectionModel().setSelectionInterval(row, row);
        }

        // source が JTextField で DATE の 場合
        Object source = e.getSource();
        if (source == startDateField || source == endDateField) {
            textField = (JTextField) source;
            popupCalendar(e);
            return;
        }

        // CellEditor を立ち上げずに右クリックした場合
        int column = diagTable.columnAtPoint(e.getPoint());
        switch(column) {
            case DiagnosisDocument.DIAGNOSIS_COL:
                popupDiagnosis(e);
                break;
            case DiagnosisDocument.CATEGORY_COL:
                popupCategory(e);
                break;
            case DiagnosisDocument.OUTCOME_COL:
                popupOutcome(e);
                break;
            case DiagnosisDocument.START_DATE_COL:
                textField = startDateField;
                popupCalendar(e);
                break;
            case DiagnosisDocument.END_DATE_COL:
                textField = endDateField;
                popupCalendar(e);
                break;
        }
    }

    /**
     * 左クリックの処理
     * @param e
     */
    private void leftPressed(MouseEvent e) {
        if (e.getClickCount() == 2) {
            // 診断名をダブルクリックしたらエディタを立ち上げることにした thx to masuda sensei
            int column = diagTable.getSelectedColumn();
            if (column == DiagnosisDocument.DIAGNOSIS_COL) {
                int row = diagTable.getSelectedRow();
                row = diagTable.convertRowIndexToModel(row);
                RegisteredDiagnosisModel model = diagTableModel.getObject(row);
                diagnosisDocument.openEditor3(model);
            }
        }
    }

    /**
     * カレンダーポップアップを出す
     * @param e
     */
    private void popupCalendar(MouseEvent e) {
        calendarPopup = new MyJPopupMenu();
        calendarPopup.setBorder(BorderFactory.createEmptyBorder());
        GregorianCalendar gc = new GregorianCalendar();
        int this_month = gc.get(Calendar.MONTH);
        int dif = lastVisitYmd[1] - this_month; //lastVisit, this_month は gc なので，両者とも値が0-11 になる
        if (dif > 0) { dif -= 12; }
        CalendarCardPanel cc = new CalendarCardPanel(ClientContext.getEventColorTable(), dif);

        cc.addPropertyChangeListener(CalendarCardPanel.PICKED_DATE, this);
        cc.setCalendarRange(new int[]{-12, 0});
        calendarPopup.insert(cc, 0);
        calendarPopup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * PropertyChange のお仕事
     * @param e
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        // カレンダーで選択した場合
        if (CalendarCardPanel.PICKED_DATE.equals(prop)) {
            SimpleDate sd = (SimpleDate) e.getNewValue();
            String date = SimpleDate.simpleDateToMmldate(sd);
            textField.setText(date);
            int[] rows = diagTable.getSelectedRows();
            for (int r : rows) {
                int row = diagTable.convertRowIndexToModel(r);
                diagTableModel.setValueAt(date, row, targetColumn);
            }
            calendarPopup.setVisible(false);
            calendarPopup = null;
        }
    }

    /**
     * 診断修飾語ポップアップを出す
     * @param e
     */
    private void popupDiagnosis(final MouseEvent e) {
        // diagPopup は最初に作ってあるので show するだけでよい
        diagPopup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * 病名修飾ポップアップメニュー作成
     */
    private void createDiagnosisPopupMenu() {
        diagPopup = new MyJPopupMenu();

        // Diagnosis preposition 項目の作成
        for (DiagnosisPreposition p : DiagnosisPreposition.values()) {
            JMenuItem item = new JMenuItem(p.desc());
            item.addActionListener(new DiagAction(p));
            diagPopup.add(item);
        }
        diagPopup.addSeparator();

        // Diagnosis postposition 項目の作成
        for (DiagnosisPostposition p : DiagnosisPostposition.values()) {
            JMenuItem item = new JMenuItem(p.desc());
            item.addActionListener(new DiagAction(p));
            diagPopup.add(item);
        }
    }

    /**
     * DiagnosisInspector で diagPopup を使う
     * @return
     */
    public MyJPopupMenu getDiagPopup() {
        return diagPopup;
    }

    /**
     * DiagnosisInspector で outcomePopup を使う
     * @return
     */
    public MyJPopupMenu getOutcomePopup() {
        return outcomePopup;
    }

    /**
     * DiagnosisInspector で categoryPopup を使う
     * @return
     */
    public MyJPopupMenu getCategoryPopup() {
        return categoryPopup;
    }

    /**
     * 病名修飾の実務 action
     */
    private class DiagAction implements ActionListener {
        Object modifier;
        boolean prep; // preposition or postposition

        public DiagAction(DiagnosisPreposition p) {
            modifier = p;
            prep = true;
        }
        public DiagAction(DiagnosisPostposition p) {
            modifier = p;
            prep = false;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] rows = diagTable.getSelectedRows();
            for (int r : rows) {
                int row = diagTable.convertRowIndexToModel(r);
                RegisteredDiagnosisModel rd = diagTableModel.getObject(row);

                // 新しく作った診断名を設定
                String diagDesc = rd.getDiagnosis();
                String diagCode = rd.getDiagnosisCode();
                String newDiagDesc;
                String newDiagCode;

                if (prep) {
                    String desc = ((DiagnosisPreposition)modifier).desc();
                    String code = ((DiagnosisPreposition)modifier).code() + ".";

                    if (diagCode.contains(code)) {
                        // すでに入力されていたらそれを消去することにした
                        newDiagDesc = diagDesc.replaceFirst(desc, "");
                        newDiagCode = diagCode.replaceFirst(code, "");
                    } else {
                        // 重複がなければ追加
                        newDiagDesc = desc + diagDesc;
                        newDiagCode = code + diagCode;
                    }
                } else {
                    String desc = ((DiagnosisPostposition)modifier).desc();
                    String code = "." + ((DiagnosisPostposition)modifier).code();

                    if (diagCode.contains(code)) {
                        // すでに入力されていたらそれを消去する
                        newDiagDesc = diagDesc.replaceFirst(desc, "");
                        newDiagCode = diagCode.replaceFirst(code, "");
                    } else {
                        newDiagDesc = diagDesc + desc;
                        newDiagCode = diagCode + code;
                    }
                }

                DiagnosisLiteModel newDiag = new DiagnosisLiteModel(rd);
                newDiag.setDiagnosisDesc(newDiagDesc);
                newDiag.setDiagnosisCode(newDiagCode);
                diagTableModel.setValueAt(newDiag, row, DiagnosisDocument.DIAGNOSIS_COL);

                // diagnosisInspector にも知らせる
                ((ChartImpl)parent.getContext()).getDiagnosisInspector().update(diagTableModel);
            }
        }
    }

    /**
     * 診断名から preposition を取り去る
     */
    public void dropPreposition() {
        int[] rows = diagTable.getSelectedRows();
        for (int r : rows) {
            int row = diagTable.convertRowIndexToModel(r);
            RegisteredDiagnosisModel rd = diagTableModel.getObject(row);

            // 新しく作った診断名を設定
            String newDiagDesc = null;
            String newDiagCode = null;

            for (DiagnosisPreposition prep : DiagnosisPreposition.values()) {
                String code = prep.code() + ".";
                int index = rd.getDiagnosisCode().indexOf(code);

                if (index == 0) {
                    newDiagDesc = rd.getDiagnosis().replaceFirst(prep.desc(), "");
                    newDiagCode = rd.getDiagnosisCode().substring(code.length());
                    break;
                }
            }

            if (newDiagDesc != null && newDiagCode != null) {
                DiagnosisLiteModel newDiag = new DiagnosisLiteModel(rd);
                newDiag.setDiagnosisDesc(newDiagDesc);
                newDiag.setDiagnosisCode(newDiagCode);
                diagTableModel.setValueAt(newDiag, row, DiagnosisDocument.DIAGNOSIS_COL);

                // diagnosisInspector にも知らせる
                ((ChartImpl)parent.getContext()).getDiagnosisInspector().update(diagTableModel);
            }
        }
    }

    /**
     * 診断名から postposition を取り去る
     */
    public void dropPostposition() {
        int[] rows = diagTable.getSelectedRows();
        for (int r : rows) {
            int row = diagTable.convertRowIndexToModel(r);
            RegisteredDiagnosisModel rd = diagTableModel.getObject(row);

            // 新しく作った診断名を設定
            String newDiagDesc = null;
            String newDiagCode = null;

            for (DiagnosisPostposition post : DiagnosisPostposition.values()) {
                String code = "." + post.code();
                int index = rd.getDiagnosisCode().indexOf(code);

                if (index != -1) {
                    newDiagDesc = rd.getDiagnosis().replaceFirst(post.desc(), "");
                    newDiagCode = rd.getDiagnosisCode().replaceFirst(code, "");
                    break;
                }
            }

            if (newDiagDesc != null && newDiagCode != null) {
                DiagnosisLiteModel newDiag = new DiagnosisLiteModel(rd);
                newDiag.setDiagnosisDesc(newDiagDesc);
                newDiag.setDiagnosisCode(newDiagCode);
                diagTableModel.setValueAt(newDiag, row, DiagnosisDocument.DIAGNOSIS_COL);

                // diagnosisInspector にも知らせる
                ((ChartImpl)parent.getContext()).getDiagnosisInspector().update(diagTableModel);
            }
        }
    }

    /**
     * 主病名／疑い病名ポップアップを出す
     * @param e
     */
    private void popupCategory(MouseEvent e) {
        categoryPopup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * 転帰ポップアップを出す
     * @param e
     */
    private void popupOutcome(MouseEvent e) {
        outcomePopup.show(e.getComponent(), e.getX(), e.getY());
    }
    /**
     * 主病名／疑い病名ポップアップを作る
     */
    private void createCategoryPopupMenu() {
        categoryPopup = new MyJPopupMenu();

        for (DiagnosisCategory c : DiagnosisCategory.values()) {
            JMenuItem item = new JMenuItem(c.model().getDiagnosisCategoryDesc());
            item.addActionListener(new CategoryOutcomeAction(c.model()));
            categoryPopup.add(item);
        }
    }
    /**
     * 転帰ポップアップを作る
     */
    private void createOutcomePopupMenu() {
        outcomePopup = new MyJPopupMenu();

        for (DiagnosisOutcome o : DiagnosisOutcome.values()) {
            JMenuItem item = new JMenuItem(o.model().getOutcomeDesc());
            item.addActionListener(new CategoryOutcomeAction(o.model()));
            outcomePopup.add(item);
        }
    }
    /**
     * 主病名／疑い病名，転帰の実務
     */
    private class CategoryOutcomeAction implements ActionListener {
        Object model;
        int column;

        public CategoryOutcomeAction() {
            super();
        }
        public CategoryOutcomeAction(DiagnosisCategoryModel model) {
            this();
            this.model = model;
            this.column = DiagnosisDocument.CATEGORY_COL;
        }
        public CategoryOutcomeAction(DiagnosisOutcomeModel model) {
            this();
            this.model = model;
            this.column = DiagnosisDocument.OUTCOME_COL;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            // 処理途中に insert が発生して選択行の行番号が変わってしまうことがあるので hash 処理する
            int[] rows = diagTable.getSelectedRows();
            int[] hashArray = diagTable.convertViewRowsToHashArray(rows);

            for (int hash : hashArray) {
                int row = diagTable.convertHashToModelRow(hash);
                diagTable.getModel().setValueAt(model, row, column);
            }
            // diagnosisInspector にも知らせる
            ((ChartImpl)parent.getContext()).getDiagnosisInspector().update(diagTableModel);
        }
    }

    /**
     * 外部からポップアップの action を使うためのメソッド群
     * @param s
     */
    public void doClickDiagPopup(String s) {
        doClickPopup(diagPopup, s);
    }

    public void doClickCategoryPopup(String s) {
        doClickPopup(categoryPopup, s);
    }

    public void doClickOutcomePopup(String s) {
        doClickPopup(outcomePopup, s);
    }

    private void doClickPopup(MyJPopupMenu popup, String s) {
        JMenuItem m = null;
        for(Component c : popup.getComponents()) {
            if (c instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) c;
                if (s.equals(item.getText())) {
                    m = item;
                    break;
                }
            }
        }
        if (m != null) { m.doClick(); }
    }
}
