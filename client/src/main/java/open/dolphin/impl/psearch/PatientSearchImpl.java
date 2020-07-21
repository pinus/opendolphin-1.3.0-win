package open.dolphin.impl.psearch;

import open.dolphin.client.AbstractMainComponent;
import open.dolphin.client.ClientContext;
import open.dolphin.client.Dolphin;
import open.dolphin.client.GUIConst;
import open.dolphin.delegater.PnsDelegater;
import open.dolphin.delegater.PvtDelegater;
import open.dolphin.dto.PatientSearchSpec;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.*;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.ui.*;
import open.dolphin.ui.sheet.JSheet;
import open.dolphin.util.Gengo;
import open.dolphin.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * 患者検索.
 *
 * @author Kazushi Minagawa
 * @author pns
 */
public class PatientSearchImpl extends AbstractMainComponent {
    private static final String NAME = "患者検索";

    // 年齢表示カラム
    private static final int AGE_COLUMN = 4;

    private static final int[] COLUMN_WIDTH = {80, 100, 120, 40, 120, 120};
    // Preferences の key
    private static final String NARROWING_SEARCH = "narrowingSearch";
    private static final String AGE_DISPLAY = "ageDisplay";
    // 年齢生年月日メソッド
    private static final String[] AGE_METHOD = new String[]{"getAgeBirthday", "getBirthday"};
    private final Logger logger;
    private final Preferences prefs;
    // 選択されている患者情報
    private PatientModel[] selectedPatient;
    // 年齢表示をするかどうか
    private boolean ageDisplay;
    // View
    private PatientSearchPanel view;
    private KeyBlocker keyBlocker;

    public PatientSearchImpl() {
        setName(NAME);
        logger = LoggerFactory.getLogger(PatientSearchImpl.class);
        prefs = Preferences.userNodeForPackage(this.getClass());
    }

    /**
     * Keyword Field にフォーカスを取る.
     */
    public void requestFocus() {
        Focuser.requestFocus(view.getKeywordFld());
        view.getKeywordFld().selectAll();
    }

    @Override
    public void start() {
        initComponents();
        connect();

        // Tabbed pane にサーチフィールドを表示してしまう.
        JPanel keywordPanel = new JPanel();
        keywordPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        keywordPanel.add(view.getKeywordFld());
        view.getKeywordFld().setPreferredSize(new Dimension(300, 26));

        PNSBadgeTabbedPane tab = ((Dolphin) getContext()).getTabbedPane();
        JPanel panel = tab.getAccessoryPanel();
        panel.add(keywordPanel, BorderLayout.EAST);

        // 別の MainComponent に tab が切り替わったらクリアする
        tab.addChangeListener(e -> {
            if (tab.getSelectedIndex() != tab.indexOfTab(getName())) {
                view.getKeywordFld().setText("");
            }
        });
    }

    @Override
    public void enter() {
        controlMenu();
        // 入ってきたら，キーワードフィールドにフォーカス
        requestFocus();
    }

    @Override
    public void stop() {

    }

    /**
     * GUI コンポーネントを初期化する.
     */
    private void initComponents() {

        view = new PatientSearchPanel();
        setUI(view);
        JTable table = view.getTable();

        List<PNSTriple<String, Class<?>, String>> reflectList = Arrays.asList(
                new PNSTriple<>(" 　患者 ID", String.class, "getPatientId"),
                new PNSTriple<>("　 氏   名", String.class, "getFullName"),
                new PNSTriple<>("　 カ  ナ", String.class, "getKanaName"),
                new PNSTriple<>(" 性別", String.class, "getGenderDesc"),
                new PNSTriple<>("　生年月日", String.class, "getAgeBirthday"),
                new PNSTriple<>("　最終受診日", String.class, "getFormattedLastVisit")
        );

        ObjectReflectTableModel<PatientModel> tableModel = new ObjectReflectTableModel<>(reflectList);
        table.setModel(tableModel);
        TableRowSorter<ObjectReflectTableModel<PatientModel>> sorter = new TableRowSorter<ObjectReflectTableModel<PatientModel>>(tableModel) {
            @Override
            public void toggleSortOrder(int column) {
                List<SortKey> keys = new ArrayList<>(getSortKeys());
                SortKey key = null;

                // SortKey があるかどうか
                int index = -1;
                for (int i = 0; i < keys.size(); i++) {
                    if (keys.get(i).getColumn() == column) {
                        index = i;
                        break;
                    }
                }
                // SortKey がない場合作る
                if (index == -1) {
                    // 受診日コラムは DESCENDING -> ASCENDING -> UNSORTED の純
                    if (column == 5) {
                        key = new SortKey(5, SortOrder.DESCENDING);

                        // それ以外は ASCENDING -> DESCENDING -> UNSORTED の純
                    } else {
                        key = new SortKey(column, SortOrder.ASCENDING);
                    }
                    // SortKey がある場合新たなキーと置き換える
                } else {
                    SortOrder order = keys.get(index).getSortOrder();
                    switch (order) {
                        case ASCENDING:
                            if (column == 5) {
                                key = new SortKey(5, SortOrder.UNSORTED);
                            } else {
                                key = new SortKey(column, SortOrder.DESCENDING);
                            }
                            break;
                        case DESCENDING:
                            if (column == 5) {
                                key = new SortKey(5, SortOrder.ASCENDING);
                            } else {
                                key = new SortKey(column, SortOrder.UNSORTED);
                            }
                            break;
                        case UNSORTED:
                            if (column == 5) {
                                key = new SortKey(5, SortOrder.DESCENDING);
                            } else {
                                key = new SortKey(column, SortOrder.ASCENDING);
                            }
                            break;
                    }
                    keys.remove(index);
                }
                keys.add(0, key);
                setSortKeys(keys);
            }
        };
        //
        // 年齢表示をしないなんて信じられない要望!
        //
        ageDisplay = prefs.getBoolean(AGE_DISPLAY, true);
        if (!ageDisplay) {
            tableModel.setMethodName(AGE_METHOD[1], AGE_COLUMN);
        }

        // 生年月日コラムに comparator を設定「32.10 歳(S60-01-01)」というのをソートできるようにする
        sorter.setComparator(AGE_COLUMN, (String o1, String o2) -> {
            String birthday1;
            String birthday2;
            if (ageDisplay) {
                birthday1 = Gengo.gengoToIsoDate(o1.split("[()]")[1]);
                birthday2 = Gengo.gengoToIsoDate(o2.split("[()]")[1]);
                return birthday2.compareTo(birthday1);
            } else {
                birthday1 = o1;
                birthday2 = o2;
                return birthday1.compareTo(birthday2);
            }
        });

        table.setRowSorter(sorter);

        // カラム幅を変更する
        for (int i = 0; i < COLUMN_WIDTH.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(COLUMN_WIDTH[i]);
            if (i == 3 || i == 0) { // ID, 性別の幅は不変にする
                column.setMaxWidth(COLUMN_WIDTH[i]);
                column.setMinWidth(COLUMN_WIDTH[i]);
            }
        }

        // レンダラを設定する
        table.setDefaultRenderer(Object.class, new IndentTableCellRenderer(7));

        // 日付を設定する
        String formatStr = "yyyy-M-d (EEE)";
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        view.getDateLbl().setText(sdf.format(new Date()));

        // hibernate index ボタン
        view.getHibernateIndexItem().addActionListener(e -> {
            final String message = "<html><b><u>Hibernate Search のインデックス作成をします</u></b><br><br>"
                    + "終了するまで操作不能になります。<br>"
                    + "途中でキャンセルはできません。<br>"
                    + "よろしいですか？</html>";
            //int option = JSheet.showConfirmDialog(view, message, "インデックス作成", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
            int option = JOptionPane.showConfirmDialog(view, message, "インデックス作成", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                PnsDelegater dl = new PnsDelegater();
                dl.makeInitialIndex();
            }
        });

        // 絞り込み検索ボタンの初期値
        view.getNarrowingSearchCb().setSelected(prefs.getBoolean(NARROWING_SEARCH, false));
        view.getNarrowingSearchCb().addActionListener(e -> setTextFieldBackground(view.getNarrowingSearchCb().isSelected()));
        setTextFieldBackground(view.getNarrowingSearchCb().isSelected());
    }

    /**
     * コンポネントにリスナを登録し接続する.
     */
    private void connect() {
        // コンテキスト・リスナを登録する
        view.getTable().addMouseListener(new ContextListener());

        // キーブロッカー
        keyBlocker = new KeyBlocker(view.getKeywordFld());

        // KeywordFld に ActionListener 登録
        view.getKeywordFld().addActionListener(e -> {
            JTextField tf = (JTextField) e.getSource();
            String test = tf.getText().trim();

            if (!test.equals("")) {
                // 検索開始
                find(test);

                // この MainComponent を選択
                PNSBadgeTabbedPane pane = ((Dolphin) getContext()).getTabbedPane();
                pane.setSelectedIndex(pane.indexOfTab(getName()));

            } else {
                // キーワードが "" ならテーブルクリアして，検索件数をリセット
                ((ObjectReflectTableModel) view.getTable().getModel()).clear();
                view.getCntLbl().setText("0 件");
            }
            requestFocus();
        });

        // 履歴保存する prefs をセット
        view.getKeywordFld().setPreferences(prefs);

        // Table に ListSelectionListener 登録
        final JTable table = view.getTable();
        final ObjectReflectTableModel<PatientModel> tableModel = (ObjectReflectTableModel<PatientModel>) table.getModel();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {

                int[] rows = table.getSelectedRows();
                if (rows == null) {
                    setSelectedPatinet(null);
                } else {
                    PatientModel[] patients = new PatientModel[rows.length];
                    for (int i = 0; i < rows.length; i++) {
                        rows[i] = table.convertRowIndexToModel(rows[i]);
                        patients[i] = tableModel.getObject(rows[i]);
                    }
                    setSelectedPatinet(patients);
                }
            }
        });

        // SPACE でカルテオープン
        table.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "openKarte");
        table.getActionMap().put("openKarte", new ProxyAction(this::openKarte));

        // command-F で search field にフォーカスする裏コマンド
        table.getInputMap().put(KeyStroke.getKeyStroke("meta F"), "showWaitingList");
        table.getActionMap().put("showWaitingList", new ProxyAction(((Dolphin)getContext())::showPatientSearch));

        // ENTER の行送りをやめる
        table.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doNothing");
        table.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"), "doNothing");
        table.getActionMap().put("doNothing", new ProxyAction(() -> {}));

        // Tab キー
        table.getInputMap().put(KeyStroke.getKeyStroke("TAB"), "focusPrevious");
        table.getInputMap().put(KeyStroke.getKeyStroke("shift TAB"), "focusPrevious");
        table.getActionMap().put("focusPrevious", new ProxyAction(KeyboardFocusManager.getCurrentKeyboardFocusManager()::focusPreviousComponent));
    }

    /**
     * 検索フィールドの背景ラベルを変更する.
     *
     * @param isNarrowing 絞り込み検索モードかどうか
     */
    private void setTextFieldBackground(boolean isNarrowing) {
        if (isNarrowing) {
            view.getKeywordFld().setLabel("絞り込み検索モード");
        } else {
            view.getKeywordFld().setLabel("患者検索");
        }
        view.getKeywordFld().repaint();
    }

    /**
     * メニューを制御する.
     * 複数行選択対応 by pns.
     * １つでも開けられないものがあれば false とする.
     */
    private void controlMenu() {
        getContext().enableAction(GUIConst.ACTION_OPEN_KARTE, canOpen());
    }

    /**
     * 現在選択されている PatientModel[] を返す
     *
     * @return array of PatientModel
     */
    public PatientModel[] getSelectedPatinet() {
        return selectedPatient;
    }

    /**
     * SelectionListener から呼ばれて selectedPatient をセットする.
     *
     * @param model array of PatientModel
     */
    public void setSelectedPatinet(PatientModel[] model) {
        selectedPatient = model;
        controlMenu();
    }

    /**
     * 年齢表示をオンオフする.
     */
    public void switchAgeDisplay() {
        ageDisplay = !ageDisplay;
        prefs.putBoolean(AGE_DISPLAY, ageDisplay);

        if (view.getTable() != null) {
            ObjectReflectTableModel tModel = (ObjectReflectTableModel) view.getTable().getModel();
            String method = ageDisplay ? AGE_METHOD[0] : AGE_METHOD[1];
            tModel.setMethodName(method, AGE_COLUMN);
        }
    }

    /**
     * 現在の selectedPatinet が canOpen かどうか判定 by pns.
     * １つでも開けられないものがあれば false とする.
     */
    private boolean canOpen() {
        // 既に開かれているカルテを open すると，toFront として処理されるので
        // PatientSearchImpl では open できないカルテというのはない
        return true;
    }

    /**
     * カルテを開く.
     * popupMenu で選択した場合はこれが呼ばれる　複数行選択対応.
     * openKarte(patient) は abstract に移動.
     * by pns
     */
    public void openKarte() {
        PatientModel[] patient = getSelectedPatinet();
        if (patient == null) {
            return;
        }
        Arrays.asList(patient).forEach(this::openKarte);
    }

    /**
     * リストで選択された患者を受付に登録する.
     * 複数行選択対応 by pns
     */
    public void addAsPvt() {

        PatientModel[] patients = getSelectedPatinet();
        if (patients == null) {
            return;
        }

        PatientVisitModel[] pvts = new PatientVisitModel[patients.length];
        String pvtDate = ModelUtils.getDateTimeAsString(new Date());
        String dept = constarctDept();

        for (int i = 0; i < patients.length; i++) {
            // 来院情報を生成する
            PatientVisitModel pvt = new PatientVisitModel();
            pvt.setId(0L);
            pvt.setNumber(getNewPvtNumber()); //10000から割り当て
            pvt.setPatient(patients[i]);
            // 受け付けを通していないので診療科はユーザ登録してあるものを使用する
            pvt.setDepartment(dept);
            // 来院日
            pvt.setPvtDate(pvtDate);

            pvts[i] = pvt;
        }

        AddAsPvtTask task = new AddAsPvtTask(pvts);
        task.execute();
    }

    /**
     * 検索テキストを解析して，該当する検索タスクを呼び出す.
     *
     * @param text キーワード
     */
    private void find(String text) {

        PatientSearchSpec spec = new PatientSearchSpec();

        if (text.startsWith("N ") || text.startsWith("n ")) {
            spec.setCode(PatientSearchSpec.NAME_SEARCH);
            text = text.substring(2);
            spec.setName(text);

        } else if (text.startsWith("K ") || text.startsWith("k ")) {
            spec.setCode(PatientSearchSpec.KANA_SEARCH);
            text = text.substring(2);
            spec.setName(text);

        } else if (text.startsWith("A ") || text.startsWith("a ")) {
            spec.setCode(PatientSearchSpec.ADDRESS_SEARCH);
            text = text.substring(2);
            spec.setAddress(text);

        } else if (text.startsWith("Z ") || text.startsWith("z ")) {
            spec.setCode(PatientSearchSpec.ZIPCODE_SEARCH);
            text = text.substring(2);
            spec.setZipCode(text);

        } else if (text.startsWith("T ") || text.startsWith("t ")) {
            spec.setCode(PatientSearchSpec.TELEPHONE_SEARCH);
            text = text.substring(2);
            spec.setTelephone(text);

        } else if (text.startsWith("I ") || text.startsWith("i ")) {
            spec.setCode(PatientSearchSpec.ID_SEARCH);
            text = text.substring(2);
            spec.setPatientId(text);

        } else if (text.startsWith("E ") || text.startsWith("e ")) {
            spec.setCode(PatientSearchSpec.EMAIL_SEARCH);
            text = text.substring(2);
            spec.setEmail(text);

        } else if (text.startsWith("O ") || text.startsWith("o ")) {
            spec.setCode(PatientSearchSpec.OTHERID_SEARCH);
            text = text.substring(2);
            spec.setOtherId(text);

        } else if (text.startsWith("B ") || text.startsWith("b ")) {
            spec.setCode(PatientSearchSpec.BIRTHDAY_SEARCH);
            text = text.substring(2);
            spec.setBirthday(Gengo.toSeireki(text));

        } else if (text.startsWith("M ") || text.startsWith("m ")) {
            spec.setCode(PatientSearchSpec.MEMO_SEARCH);
            text = text.substring(2);
            spec.setSearchText(text);

        } else if (text.startsWith("F ") || text.startsWith("f ")) {
            spec.setCode(PatientSearchSpec.FULL_TEXT_SEARCH);
            text = text.substring(2);
            spec.setSearchText(text);

        } else if (isNengoDate(text)) {
            spec.setCode(PatientSearchSpec.BIRTHDAY_SEARCH);
            spec.setBirthday(Gengo.toSeireki(text));

        } else if (isOrcaDate(text)) {
            spec.setCode(PatientSearchSpec.BIRTHDAY_SEARCH);
            spec.setBirthday(Gengo.toSeireki(ModelUtils.orcaDateToGengo(text)));

        } else if (isDate(text)) {
            //System.out.println("Date search");
            spec.setCode(PatientSearchSpec.DATE_SEARCH);
            spec.setDate(Gengo.toSeireki(text)); // 月/日が１桁で指定された場合の調整 by pns

        } else if (isKana(text)) {
            spec.setCode(PatientSearchSpec.KANA_SEARCH);
            spec.setName(text);

        } else if (isHiragana(text)) {
            spec.setCode(PatientSearchSpec.KANA_SEARCH);
            spec.setName(StringTool.hiraganaToKatakana(text));

        } else if (isId(text)) {
            spec.setCode(PatientSearchSpec.ID_SEARCH);
            spec.setPatientId(text);

        } else if (text.length() <= 1) {
            JSheet.showMessageDialog(getContext().getFrame(),
                    "検索文字列は２文字以上入力して下さい", "検索文字列入力エラー", JOptionPane.ERROR_MESSAGE);
            return;

        } else {
            spec.setCode(PatientSearchSpec.FULL_TEXT_SEARCH);
            spec.setSearchText(text);
        }

        FindTask task = new FindTask(view, "患者検索", "検索中...", spec);

        task.setInputBlocker(new Blocker());
        // キャンセルした際 interrupt すると delegater で NamigException が出る
        task.setInterruptOnCancel(false);
        task.execute();
    }

    private boolean isDate(String text) {
        return text != null && text.matches("[0-9][0-9][0-9][0-9]-[0-9]+-[0-9]+");
    }

    private boolean isNengoDate(String text) {
        return text != null && text.matches("[MmTtSsHhRr][0-9]+-[0-9]+-[0-9]+");
    }

    private boolean isOrcaDate(String text) {
        // S12-3-4 = 3120304
        return text != null && text.matches("[1-6][0-6][0-9][0-1][0-9][0-3][0-9]");
    }

    private boolean isKana(String text) {
        boolean maybe = true;
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                // スペースが入っていてもカタカナと判断するようにする
                if (!StringTool.isKatakana(c) && !StringTool.isSpace(c)) {
                    maybe = false;
                    break;
                }
            }
            return maybe;
        }
        return false;
    }

    private boolean isHiragana(String text) {
        boolean maybe = true;
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (!StringTool.isHiragana(c) && !StringTool.isSpace(c)) {
                    maybe = false;
                    break;
                }
            }
            return maybe;
        }
        return false;
    }

    private boolean isId(String text) {
        if (text == null || text.length() > 6) {
            return false;
        }
        return text.matches("[0-9]+");
    }

    /**
     * 検索結果をファイルに書き出す.
     * 複数行選択対応.
     */
    public void exportSearchResult() {
        final JFileChooser fileChooser = new JFileChooser();
        JSheet.showSaveSheet(fileChooser, getContext().getFrame(), e -> {
            if (e.getOption() == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                if (!file.exists() || overwriteConfirmed(file)) {

                    try (FileWriter writer = new FileWriter(file)) {
                        JTable table = view.getTable();
                        // 書き出す内容　選択されたものを書き出す
                        StringBuilder sb = new StringBuilder();
                        int[] rows = table.getSelectedRows();
                        for (int i = 0; i < rows.length; i++) {
                            rows[i] = table.convertRowIndexToModel(rows[i]);
                            for (int column = 0; column < table.getColumnCount(); column++) {
                                sb.append(column == 0 ? '"' : ",\"");
                                sb.append(table.getValueAt(rows[i], column));
                                sb.append('"');
                            }
                            sb.append('\n');
                        }
                        writer.write(sb.toString());

                    } catch (IOException ex) {
                        System.out.println("PatientSearchImpl.java: " + ex);
                    }
                }
            }
        });
    }

    /**
     * ファイル上書き確認ダイアログを表示する.
     *
     * @param file 上書き対象ファイル
     * @return 上書きOKが指示されたらtrue
     */
    private boolean overwriteConfirmed(File file) {
        String title = "上書き確認";
        String message = "既存のファイル「" + file.toString() + "」\n"
                + "を上書きしようとしています。続けますか？";

        int confirm = JSheet.showConfirmDialog(
                getContext().getFrame(), message, title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

        return confirm == JOptionPane.OK_OPTION;
    }

    /**
     * 選択患者を受付する.
     * 複数行選択対応　by pns.
     */
    private class AddAsPvtTask extends SwingWorker<Void, Void> {

        private final PatientVisitModel[] pvts;

        public AddAsPvtTask(PatientVisitModel[] pvts) {
            super();
            this.pvts = pvts;
        }

        @Override
        protected Void doInBackground() {

            PvtDelegater pdl = new PvtDelegater();
            for (PatientVisitModel pvt : pvts) {
                pdl.addPvt(pvt);
            }
            return null;
        }
    }

    /**
     * FindTask で使う InputBlocker.
     */
    private class Blocker implements Task.InputBlocker {

        @Override
        public void block() {
            keyBlocker.block();
            getContext().getGlassPane().block();
            JProgressBar bar = view.getProgressBar();
            //bar.setIndeterminate(true);
        }

        @Override
        public void unblock() {
            keyBlocker.unblock();
            getContext().getGlassPane().unblock();
            JProgressBar bar = view.getProgressBar();
            //bar.setIndeterminate(false);
            bar.setValue(0);
        }
    }

    /**
     * 受付リストのコンテキストメニュークラス.
     * modified by pns
     */
    private class ContextListener extends AbstractMainComponent.ContextListener<PatientModel> {
        private final JPopupMenu contextMenu;

        public ContextListener() {
            contextMenu = getContextMenu();
        }

        @Override
        public void openKarte(PatientModel pvt) {
            PatientSearchImpl.this.openKarte(pvt);
        }

        @Override
        public void maybeShowPopup(MouseEvent e) {

            if (e.isPopupTrigger()) {

                contextMenu.removeAll();

                if (canOpen()) {
                    String pop1 = ClientContext.getString("waitingList.popup.openKarte");
                    JMenuItem openKarte = new JMenuItem(new ProxyAction(pop1, PatientSearchImpl.this::openKarte));
                    JMenuItem addAsPvt = new JMenuItem(new ProxyAction("受付登録", PatientSearchImpl.this::addAsPvt));
                    openKarte.setIconTextGap(8);
                    addAsPvt.setIconTextGap(8);
                    contextMenu.add(openKarte);
                    contextMenu.add(addAsPvt);
                }

                JCheckBoxMenuItem item = new JCheckBoxMenuItem("年齢表示");
                item.setIconTextGap(12);
                contextMenu.add(item);
                item.setSelected(ageDisplay);
                item.addActionListener(ae -> switchAgeDisplay());

                // 検索結果をファイル保存
                int selectedRowCount = view.getTable().getSelectedRowCount();
                if (selectedRowCount > 0) {
                    JMenuItem export = new JMenuItem(
                            new ProxyAction("選択された " + selectedRowCount + " 件をファイル保存", PatientSearchImpl.this::exportSearchResult));
                    export.setIconTextGap(8);
                    contextMenu.add(export);
                }

                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
