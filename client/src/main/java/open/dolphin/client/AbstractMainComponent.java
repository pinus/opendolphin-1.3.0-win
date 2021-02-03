package open.dolphin.client;

import open.dolphin.delegater.PatientDelegater;
import open.dolphin.delegater.PvtDelegater;
import open.dolphin.infomodel.KarteState;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.project.Project;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.ObjectReflectTableModel;
import open.dolphin.ui.sheet.JSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Main Window コンポーネントプラグインの抽象クラス.
 * WaitingListImpl, PatientSearchImpl, LaboTestImpoter で これを extends している.
 * <br>
 * <pre>
 * AbstractMainComponent > MainComponent > MainTool > MainService
 * </pre>
 */
public abstract class AbstractMainComponent extends MouseAdapter implements MainComponent {
    private Logger logger = LoggerFactory.getLogger(AbstractMainComponent.class);

    private String name;
    private String icon;
    private MainWindow context;
    private JPanel ui;
    private int number = 10000; // pvt がない場合の受付番号 10000から連番で作る

    public AbstractMainComponent() {
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public final void setName(String name) {
        this.name = name;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public MainWindow getContext() {
        return context;
    }

    @Override
    public void setContext(MainWindow context) {
        this.context = context;
    }

    @Override
    public JPanel getUI() {
        return ui;
    }

    @Override
    public void setUI(JPanel ui) {
        this.ui = ui;
    }

    @Override
    public void enter() {
    }

    @Override
    public Callable<Boolean> getStartingTask() {
        return null;
    }

    @Override
    public Callable<Boolean> getStoppingTask() {
        return null;
    }

    @Override
    public abstract void start();

    @Override
    public abstract void stop();

    /**
     * 指定されたカルテ (PatientModel)を開く.
     * 最終的に Dolphin#openKarte を呼び出す.
     * called by PatientSearchImpl, LaboTestImporter
     *
     * @param patient 対象患者
     */
    public void openKarte(final PatientModel patient) {
        if (canOpen(patient)) {
            Thread t = new Thread(() -> {
                // 健康保険情報をフェッチする
                PatientDelegater pdl = new PatientDelegater();
                pdl.fetchHealthInsurance(patient);

                // pvt 情報があるかどうかチェック
                PvtDelegater pvtdl = new PvtDelegater();
                List<PatientVisitModel> pvtModels = pvtdl.getPvt(patient);

                // PatientVisitModel to open
                PatientVisitModel pvtToOpen;

                if (pvtModels.isEmpty()) {
                    // 来院がない場合, 来院情報を生成する
                    pvtToOpen = new PatientVisitModel();
                    pvtToOpen.setId(0L);
                    pvtToOpen.setNumber(getNewPvtNumber()); //10000から割り当て
                    pvtToOpen.setPatient(patient);
                    // 受け付けを通していないのでログイン情報及び設定ファイルを使用する
                    // 診療科名，診療科コード，医師名，医師コード，JMARI
                    pvtToOpen.setDepartment(constarctDept());

                } else {
                    // 来院している場合, 最初の pvt を採用
                    pvtToOpen = pvtModels.get(0);
                }

                // すでに OPEN ならどっかで開いているということなので編集不可に設定
                int state = pvtToOpen.getState();
                if (KarteState.isOpen(state)) {
                    openReadOnlyKarte(pvtToOpen, state);
                }
                // OPEN でなければ，通常どおりオープン
                else {
                    getContext().openKarte(pvtToOpen);
                }
            });
            t.start();

        } else {
            // 既に開かれていれば，そのカルテを前に
            ChartImpl.toFront(patient);
            EditorFrame.toFront(patient);
        }
    }

    /**
     * 指定されたカルテ (PatientVisitModel) を開く.
     * 最終的に Dolphin#openKarte を呼び出す.
     * WaitingListImpl.
     *
     * @param pvtModel PatientVisitModel
     */
    public void openKarte(final PatientVisitModel pvtModel) {
        if (canOpen(pvtModel)) {
            // isReadOnly対応
            Thread t = new Thread(() -> {
                // 健康保険情報をフェッチする
                PatientDelegater ptdl = new PatientDelegater();
                ptdl.fetchHealthInsurance(pvtModel.getPatient());

                // 現在の state をサーバからとってくる
                PvtDelegater pvdl = new PvtDelegater();
                int state = pvdl.getPvtState(pvtModel.getId());
                // 読んだら table を update 　　　→ カルテが開くと update がよばれるのでここでは不要
                //int row = getRowForPvt(pvtModel);
                //pvtModel.setState(state);
                //pvtTableModel.fireTableRowsUpdated(row, row);

                // すでに OPEN ならどっかで開いているということなので編集不可に設定
                if (KarteState.isOpen(state)) {
                    openReadOnlyKarte(pvtModel, state);
                }
                // OPEN でなければ, 通常どおりオープン （Dolphin#openKarte を呼ぶ）
                else {
                    getContext().openKarte(pvtModel);
                }
            });
            t.start();

        } else {
            // 既に開かれていれば, そのカルテを前に
            ChartImpl.toFront(pvtModel);
            EditorFrame.toFront(pvtModel);
        }
    }

    /**
     * ReadOnly でカルテを開く
     *
     * @param pvtModel PatientVisitModel
     * @param state KarteState
     */
    public void openReadOnlyKarte(final PatientVisitModel pvtModel, final int state) {
        // 元々 ReadOnly のユーザーならそのまま開いて OK
        if (Project.isReadOnly()) {
            getContext().openKarte(pvtModel);
            return;
        }
        // ダイアログで確認する
        String ptName = pvtModel.getPatientName();
        String message = "<html>" +
                "<h3>" + ptName + " 様のカルテは他の端末で編集中です</h3>" +
                "<p><nobr>閲覧のみで、編集はできません<nobr></p></html>";

        int ans = JSheet.showConfirmDialog(getUI(), message, "", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.OK_OPTION) {
            pvtModel.setState(KarteState.READ_ONLY);
            getContext().openKarte(pvtModel);
        }
    }

    /**
     * カルテ (PatientModel) を開くことが可能かどうかを返す.
     *
     * @param patient PatientModel
     * @return 開くことが可能な時 true
     */
    public boolean canOpen(PatientModel patient) {
        return !ChartImpl.isKarteOpened(patient);
    }

    /**
     * カルテ (PatientVisitModel) を開くことが可能かどうかを返す.
     *
     * @return 開くことが可能な時 true
     */
    public boolean canOpen(PatientVisitModel pvt) {
        return Objects.nonNull(pvt) && !ChartImpl.isKarteOpened(pvt);
    }

    /**
     * department description を返す.
     *
     * @return "診療科名, 診療科コード, 医師名, 医師コード, dummy JMARI"
     */
    public String constarctDept() {
        return String.join(",",
                Project.getUserModel().getDepartmentModel().getDepartmentDesc(),
                Project.getUserModel().getDepartmentModel().getDepartment(),
                Project.getUserModel().getCommonName(),
                Project.getUserModel().getUserId(),
                Project.getJMARICode()
        );
    }

    /**
     * ニセの受付番号を連番で作成.
     *
     * @return ニセの受付番号
     */
    public int getNewPvtNumber() {
        return number++;
    }

    /**
     * MainComponent (WaitingList, PatientSearch, LaboImporter) で使われる ContextListener.
     *
     * @param <T>
     */
    public abstract class ContextListener<T> extends MouseAdapter {

        private final JPopupMenu contextMenu = new JPopupMenu();
        private JTable table;
        private ObjectReflectTableModel<T> tableModel;

        public ContextListener() {
        }

        /**
         * {@code ObjectReflectTableModel<T>} に格納されている T からカルテを開く.
         *
         * @param value PatientModel or PatientVisitModel
         */
        public abstract void openKarte(T value);

        /**
         * PopupMenu を表示する.
         *
         * @param e MouseEvent
         */
        public abstract void maybeShowPopup(MouseEvent e);

        public JPopupMenu getContextMenu() {
            return contextMenu;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // ダブルクリック
            if (e.getClickCount() == 2 && !contextMenu.isShowing() && e.getSource() instanceof JTable) {
                table = (JTable) e.getSource();
                tableModel = (ObjectReflectTableModel<T>) table.getModel();

                int rowIndex = table.getSelectedRow();

                if (rowIndex != -1) {
                    int row = table.convertRowIndexToModel(rowIndex);
                    T value = tableModel.getObject(row);
                    if (value != null) {
                        openKarte(value);
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            preparePopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // windows はこちら
            preparePopup(e);
        }

        private void preparePopup(MouseEvent e) {
            if (e.getSource() instanceof JTable && e.isPopupTrigger()) {
                table = (JTable) e.getSource();
                tableModel = (ObjectReflectTableModel<T>) table.getModel();

                int clickedRow = table.rowAtPoint(e.getPoint());

                // クリックされた場所が選択されているかどうか
                boolean isSelected = false;
                for (int row : table.getSelectedRows()) {
                    if (row == clickedRow) {
                        isSelected = true;
                        break;
                    }
                }
                // 選択されていない場所でクリックした場合は選択し直す
                if (!isSelected) {
                    table.getSelectionModel().setSelectionInterval(clickedRow, clickedRow);
                    requestFocus(table);
                }
                maybeShowPopup(e);
            }
        }

        /**
         * フォーカス要求.
         *
         * @param c Component
         */
        private void requestFocus(Component c) {
            Focuser.requestFocus(c);
        }
    }
}
