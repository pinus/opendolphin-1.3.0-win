package open.dolphin.client;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.delegater.OrcaDelegater;
import open.dolphin.dnd.KartePaneTransferHandler;
import open.dolphin.event.CompletionListener;
import open.dolphin.helper.DBTask;
import open.dolphin.helper.StringTool;
import open.dolphin.infomodel.*;
import open.dolphin.project.Project;
import open.dolphin.ui.Focuser;
import open.dolphin.util.DateUtils;
import open.dolphin.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.print.PageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * 2号カルテクラス.
 *
 * @author Kazushi Minagawa
 * @author pns
 */
public class KarteEditor extends AbstractChartDocument implements IInfoModel {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_TITLE = "経過記録";

    // SOA Pane
    private KartePane soaPane;
    // P Pane
    private KartePane pPane;
    // 2号カルテ JPanel
    private KartePanel kartePanel;
    // 編集可能かどうかのフラグ. このフラグで KartePane を初期化する
    private boolean editable;
    // 修正時に true
    private boolean modify;
    // CLAIM 送信フラグ
    private boolean sendClaim;
    // State Manager
    private StateMgr stateMgr;
    // EditorFrame に save 完了を知らせる
    private CompletionListener completionListener;
    // KarteEditor ノードの Preferences
    private Preferences prefs;
    // 一時保存
    private Autosave autosave;
    // dirty フラグ
    private boolean dirty;
    // Logger
    private final Logger logger = LoggerFactory.getLogger(KarteEditor.class);

    public KarteEditor() {
        init();
    }

    private void init() {
        setTitle(DEFAULT_TITLE);
        prefs = Preferences.userNodeForPackage(KarteEditor.class);
        autosave = new Autosave(this);
    }

    /**
     * エディタ終了を知らせるリスナを登録.
     * EditorFrame に知らせる.
     *
     * @param listener CompletionListener
     */
    public void addFinishListener(CompletionListener listener) {
        completionListener = listener;
    }

    /**
     * Save 時ではなくメニューから CLAIM 送信する.
     * EditorFrame で編集中の場合はここが呼ばれる.
     */
    public void sendClaim() {
        logger.error("sendClaim() in KarteEditor called.");
    }

    public void selectAll() {
        logger.error("selectAll selected");
    }

    /**
     * スタンプ標準表示/簡易表示を切り替える.
     */
    public void simplifyStamp() { toggleSimplifyStamp(getPPane().getDocument()); }

    /**
     * KarteEditor の実際の高さを返す.
     *
     * @return 高さ
     */
    public int getActualHeight() {
        try {
            JTextPane pane = soaPane.getTextPane();
            int pos = pane.getDocument().getLength();
            Rectangle r = pane.modelToView(pos);
            int hsoa = r.y;

            if (pPane == null) {
                return hsoa;
            }

            pane = pPane.getTextPane();
            pos = pane.getDocument().getLength();
            r = pane.modelToView(pos);
            int hp = r.y;

            return Math.max(hsoa, hp);

        } catch (BadLocationException ex) {
            ex.printStackTrace(System.err);
        }
        return 0;
    }

    /**
     * 2号カルテ kartePanel を印刷する.
     */
    @Override
    public void print() {
        PageFormat pageFormat = getContext().getContext().getPageFormat();
        String name = getContext().getPatient().getFullName();
        kartePanel.printPanel(pageFormat, 1, true, name, getActualHeight() + 30);
    }

    public void insertImage() {
        JFileChooser chooser = new JFileChooser();
        int selected = chooser.showOpenDialog(getContext().getFrame());
        if (selected == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getPath();
            this.getSOAPane().insertImage(path);

        }
    }

    /**
     * SOAPaneを返す.
     *
     * @return SOAPane
     */
    protected KartePane getSOAPane() {
        return soaPane;
    }

    /**
     * PPaneを返す.
     *
     * @return PPane
     */
    public KartePane getPPane() {
        return pPane;
    }

    /**
     * 編集可能属性を設定する.
     *
     * @param b 編集可能な時true
     */
    protected void setEditable(boolean b) {
        editable = b;
    }

    /**
     * 修正属性を設定する.
     *
     * @param b 修正する時true
     */
    protected void setModify(boolean b) {
        modify = b;
    }

    @Override
    public void enter() {
        super.enter();
        stateMgr.controlMenu();
        getContext().enabledAction(GUIConst.ACTION_SEND_CLAIM, true);

    }

    @Override
    public boolean isDirty() {
        return stateMgr.isDirty();
    }

    /**
     * KartePane から dirty の通知を受ける.
     *
     * @param newDirty セットする dirty
     */
    @Override
    public void setDirty(boolean newDirty) {
        if (dirty ^ newDirty) {
            dirty = newDirty;
            stateMgr.setDirty(newDirty);
        }
        // autosave に dirty 情報を流す.
        autosave.setDirty(newDirty);
    }

    /**
     * 2号カルテモードで初期化する.
     */
    public void initialize() {
        stateMgr = new StateMgr();
        kartePanel = KartePanelFactory.createEditorPanel();

        // SOA Pane を生成する
        soaPane = new KartePane();
        soaPane.setTextPane(kartePanel.getSoaTextPane());
        soaPane.setParent(this);
        soaPane.setRole(ROLE_SOA);
        soaPane.getTextPane().setTransferHandler(new KartePaneTransferHandler(soaPane));
        if (Objects.nonNull(getDocument())) {
            // Schema 画像にファイル名を付けるのために必要
            String docId = getDocument().getDocInfo().getDocId();
            soaPane.setDocId(docId);
        }

        // P Pane を生成する
        pPane = new KartePane();
        pPane.setTextPane(kartePanel.getPTextPane());
        pPane.setParent(this);
        pPane.setRole(ROLE_P);
        pPane.getTextPane().setTransferHandler(new KartePaneTransferHandler(pPane));

        // ctrl-¥ で soaPane, PPane 間を移動する
        KeyListener l = new KeyAdapter() {
            KeyStroke CTRL_YEN = KeyStroke.getKeyStroke("ctrl BACK_SLASH");
            JTextPane SOA = kartePanel.getSoaTextPane(), P = kartePanel.getPTextPane();
            @Override
            public void keyPressed(KeyEvent e) {
                if (KeyStroke.getKeyStrokeForEvent(e).equals(CTRL_YEN)) {
                    Focuser.requestFocus(e.getSource() == SOA ? P : SOA);
                }
            }
        };
        kartePanel.getSoaTextPane().addKeyListener(l);
        kartePanel.getPTextPane().addKeyListener(l);

        setUI(kartePanel);

        // 初期化の前にモデルがセットしてある.
        // Model を表示する
        displayModel();
    }

    /**
     * ２号カルテモードを開始する. 初期化の後コールされる.
     */
    @Override
    public void start() {
        ChartMediator mediator = getContext().getChartMediator();
        soaPane.init(editable, mediator);
        pPane.init(editable, mediator);

        SwingUtilities.invokeLater(() -> {
            // キャレットを先頭にリセット.
            getPPane().getTextPane().setCaretPosition(0);
            getSOAPane().getTextPane().setCaretPosition(0);
            Focuser.requestFocus(getSOAPane().getTextPane());
        });
        enter();

        // 自動一時保存スタート
        autosave.start();
        logger.info("autosave start");
    }

    /**
     * KarteEditor の終了処理. EditorFrame から呼ばれる.
     */
    @Override
    public void stop() {
        logger.info("autosave stop");
        autosave.stop();
        soaPane.clear();
        pPane.clear();
    }

    /**
     * DocumentModelを表示する.
     */
    private void displayModel() {
        // Timestamp を表示する
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日(E) HH:mm"));
        String timeStamp = MODIFY_MARK + now;

        if (modify) {
            String firstConfirm = ModelUtils.getDateAsFormatString(getDocument().getDocInfo().getFirstConfirmDate(), IInfoModel.KARTE_DATE_FORMAT);
            timeStamp += ORIGINAL_MARK + firstConfirm;
        }

        // 内容を表示する
        if (Objects.nonNull(getDocument().getModules())) {
            KarteRenderer_2 renderer = new KarteRenderer_2(soaPane, pPane);
            renderer.render(getDocument());
        }

        // 健康保険を表示する
        PVTHealthInsuranceModel[] ins = null;

        // コンテキストが EditotFrame の場合と Chart の場合がある
        if (getContext() instanceof ChartImpl) {
            ins = getContext().getHealthInsurances();
        } else if (getContext() instanceof EditorFrame) {
            EditorFrame ef = (EditorFrame) getContext();
            ChartImpl chart = (ChartImpl) ef.getChart();
            ins = chart.getHealthInsurances();
        }

        // Model に設定してある健康保険を選択する
        String selecteIns = Arrays.stream(ins)
                .filter(i -> Objects.nonNull(i.getGUID()))
                .filter(i -> i.getGUID().equals(getDocument().getDocInfo().getHealthInsuranceGUID()))
                .findAny().map(m -> {
                    StringBuilder sb = new StringBuilder();
                    String insClass = m.getInsuranceClass();
                    if (Objects.nonNull(insClass)) {
                        if (insClass.length() > 2) { insClass = insClass.substring(0, 2); }
                        sb.append(insClass);
                    } else {
                        sb.append("自費");
                    }
                    PVTPublicInsuranceItemModel[] pubs = m.getPVTPublicInsuranceItem();
                    if (Objects.nonNull(pubs)) {
                        String[] items = Arrays.stream(pubs)
                                .map(PVTPublicInsuranceItemModel::toString)
                                .map(str -> str.replaceAll("独自", ""))
                                .map(str -> str.length() > 2 ? str.substring(0, 2) : str)
                                .toArray(String[]::new);
                        if (items.length > 0) {
                            sb.append(PUB_MARK);
                            sb.append(String.join("・", items));

                            Chart realChart = ((EditorFrame) getContext()).getChart();
                            String realTitle = realChart.getFrame().getTitle();
                            if (!realTitle.contains(PUB_MARK)) {
                                realChart.getFrame().setTitle(realTitle + PUB_MARK);
                            }
                        }
                    }
                    return sb.toString();
                }).orElse(null);

        StringBuilder sb = new StringBuilder();
        sb.append(getContext().getPatient().getKanaName().replaceAll("　", " "));
        sb.append("：");
        sb.append(getContext().getPatient().getPatientId());
        sb.append(" ");

        sb.append(timeStamp);
        if (selecteIns != null) {
            sb.append(" (");
            sb.append(selecteIns.trim());
            sb.append(")");
        }

        getContext().getFrame().setTitle(sb.toString());
    }

    /**
     * 保存ダイアログを表示し保存時のパラメータ (SaveParams) 作成する.
     *
     * @return SaveParams
     */
    private SaveParams getSaveParams() {
        // Title の設定
        String title = StringTool.isEmpty(getDocument().getDocInfo().getTitle()) ?
                Project.getProjectStub().isUseTop15AsTitle() ?
                        soaPane.getTitle() : Project.getProjectStub().getDefaultKarteTitle()
                : getDocument().getDocInfo().getTitle();

        // DocInfoModel
        DocInfoModel docInfo = getDocument().getDocInfo();

        // sendClaim=true の場合は条件によって sendClaimSave か sendClaimModify かをセットする
        sendClaim = Project.getSendClaim();

        if (sendClaim) {
            if (modify) {
                if (docInfo.getStatus().equals(STATUS_TMP)) {
                    // 仮保存から修正がかかっている場合
                    sendClaim = Project.getSendClaimSave();

                } else {
                    // modify 時に中途終了データがある場合は modify でも sendClaimSave
                    OrcaDelegater delegater = new OrcaDelegater();
                    boolean existsWorkingData = delegater.existsOrcaWorkingData(getContext().getPatient().getPatientId());
                    sendClaim = existsWorkingData ? Project.getSendClaimSave() : Project.getSendClaimModify();
                }

            } else {
                // 新規カルテで保存の場合
                if (docInfo.getStatus().equals(IInfoModel.STATUS_NONE)) {
                    sendClaim = Project.getSendClaimSave();
                }
            }
        }

        // ダイアログを表示し，アクセス権等の保存時のパラメータを取得する
        SaveParams params = new SaveParams();
        params.setTitle(title);
        params.setDepartment(getDocument().getDocInfo().getDepartmentDesc());

        // 印刷枚数をPreferenceから取得する
        int numPrint = prefs.getInt("karte.print.count", 0);
        params.setPrintCount(numPrint);

        // CLAIM 送信するかどうか
        params.setSendClaim(sendClaim);

        Window parent = SwingUtilities.getWindowAncestor(this.getUI());
        SaveDialog sd = new SaveDialog(parent);
        params.setAllowPatientRef(false);    // 患者の参照
        params.setAllowClinicRef(false);     // 診療履歴のある医療機関
        sd.setValue(params);
        sd.start();
        params = sd.getValue();

        // 印刷枚数を保存する
        if (params.getSelection() == SaveDialog.SAVE) {
            prefs.putInt("karte.print.count", params.getPrintCount());
        }

        return params;
    }

    /**
     * カルテを保存する.
     */
    @Override
    public void save() {

        // 何も書かれていない時はリターンする
        if (!stateMgr.isDirty()) {
            logger.debug("not dirty");
            return;
        }

        // 保存ダイアログを表示し，パラメータを得る
        SaveParams params = getSaveParams();

        // ダイアログで破棄だった場合はリターンする
        if (params.getSelection() == SaveDialog.DISPOSE) {
            // save される前に EditorFrame に Termination を送って dispose する
            completionListener.completed();
            return;

        } else if (params.getSelection() == SaveDialog.CANCEL) {
            // cancel の場合はそのまま帰る
            return;
        }

        // 文書末の余分な改行文字を削除する by masuda-senesi
        KarteStyledDocument doc = (KarteStyledDocument) soaPane.getTextPane().getDocument();
        doc.removeExtraCr();
        doc = (KarteStyledDocument) pPane.getTextPane().getDocument();
        doc.removeExtraCr();
        doc.removeRepeatedCr();

        // DocumentModel を作る
        composeModel(params);

        final DocumentDelegater ddl = new DocumentDelegater();
        final DocumentModel saveModel = getDocument();
        final Chart chart = this.getContext();

        DBTask<String> task = new DBTask<String>(chart) {

            @Override
            protected String doInBackground() {
                logger.debug("KarteSaveTask doInBackground");
                String ret = null;

                ddl.putKarte(saveModel);

                if (ddl.isNoError()) {
                    if (sendClaim) {
                        OrcaDelegater delegater = new OrcaDelegater();
                        delegater.sendDocument(saveModel);
                    }
                } else {
                    ret = ddl.getErrorMessage();
                }
                return ret;
            }

            @Override
            protected void succeeded(String errMsg) {
                logger.debug("KarteSaveTask succeeded");

                if (ddl.isNoError()) {
                    // 状態遷移する
                    stateMgr.setSaved(true);

                    // Chart の状態を設定する.
                    // Chart.setChartState しておけば，ChartImpl の fireChanged で PVT にも同じく反映される.
                    // 今日のカルテをセーブした場合のみ chartState を変更する.
                    // 今日受診していて，過去のカルテを修正しただけなのに診察完了になってしまうのを防ぐ.

                    DocInfoModel docInfo = saveModel.getDocInfo();
                    String firstConfirmDate = docInfo.getFirstConfirmDateTrimTime(); // ISO_DATE 型式
                    boolean isTodaysKarte = DateUtils.todayToIsoDate().equals(firstConfirmDate);

                    if (isTodaysKarte) {
                        if (docInfo.getStatus().equals(STATUS_TMP)) {
                            chart.setChartState(KarteState.OPEN_TEMP);
                        } else if (docInfo.getStatus().equals(STATUS_FINAL)) {
                            chart.setChartState(KarteState.OPEN_SAVE);
                        }
                    }
                    // 文書履歴の更新を通知する
                    chart.getDocumentHistory().update(docInfo.getFirstConfirmDateTrimTime());

                    // save が終了したことを EditorFrame に知らせる
                    completionListener.completed();

                } else {
                    // errMsg を処理する
                    // エラーを表示する
                    JFrame parent = chart.getFrame();
                    String title = ClientContext.getString("karte.task.saveTitle");
                    JOptionPane.showMessageDialog(parent,
                            errMsg,
                            ClientContext.getFrameTitle(title),
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        task.execute();
    }

    /**
     * save および claim 送信のために DocumentModel を compose する.
     *
     * @param params SaveParams
     */
    private void composeModel(SaveParams params) {
        //
        // DocInfoに値を設定する
        //
        DocumentModel document = getDocument();
        DocInfoModel docInfo = document.getDocInfo();

        // 現在時刻を ConfirmDate にする
        Date confirmed = new Date();
        docInfo.setConfirmDate(confirmed);
        logger.debug("composeModel confirmed = " + docInfo.getConfirmDate());

        //
        // 修正でない場合は FirstConfirmDate = ConfirmDate にする
        // 修正の場合は FirstConfirmDate は既に設定されている
        // 修正でない新規カルテは parentId = null である
        //
        if (docInfo.getParentId() == null) {
            docInfo.setFirstConfirmDate(confirmed);
        }
        logger.debug("docInfo.firstConfirmDate = " + docInfo.getFirstConfirmDate());

        //
        // Status 仮保存か確定保存かを設定する
        // final の時は CLAIM 送信するが前の状態に依存する
        //
        if (!params.isTmpSave()) {
            //
            // 編集が開始された時の state を取得する
            //
            String oldStatus = docInfo.getStatus();

            switch (oldStatus) {
                case STATUS_NONE:
                    //
                    // NONEから確定への遷移 newSave
                    //
                    sendClaim = params.isSendClaim();
                    logger.debug("NONEから確定 : " + sendClaim);
                    break;
                case STATUS_TMP:
                    //
                    // 仮保存から確定へ遷移する場合   saveFromTmp
                    //
                    sendClaim = params.isSendClaim();
                    logger.debug("仮保存から確定 : " + sendClaim);
                    break;
                default:
                    //
                    // 確定から確定（修正の場合に相当する）以前は sendClaim = false;
                    //
                    sendClaim = params.isSendClaim();
                    logger.debug("修正 : " + sendClaim);
                    break;
            }

            //
            // 保存時の state を final にセットする
            //
            docInfo.setStatus(STATUS_FINAL);

        } else {
            //
            // 仮保存の場合 CLAIM 送信しない
            //
            sendClaim = false;
            logger.debug("仮保存 : " + sendClaim);

            docInfo.setStatus(STATUS_TMP);
        }

        // titleを設定する
        docInfo.setTitle(params.getTitle());

        // デフォルトのアクセス権を設定をする
        AccessRightModel ar = new AccessRightModel();
        ar.setPermission(PERMISSION_ALL);
        ar.setLicenseeCode(ACCES_RIGHT_CREATOR);
        ar.setLicenseeName(ACCES_RIGHT_CREATOR_DISP);
        ar.setLicenseeCodeType(ACCES_RIGHT_FACILITY_CODE);
        docInfo.addAccessRight(ar);

        // 患者のアクセス権を設定をする
        if (params.isAllowPatientRef()) {
            ar = new AccessRightModel();
            ar.setPermission(PERMISSION_READ);
            ar.setLicenseeCode(ACCES_RIGHT_PATIENT);
            ar.setLicenseeName(ACCES_RIGHT_PATIENT_DISP);
            ar.setLicenseeCodeType(ACCES_RIGHT_PERSON_CODE);
            docInfo.addAccessRight(ar);
        }

        // 診療履歴のある施設のアクセス権を設定をする
        if (params.isAllowClinicRef()) {
            ar = new AccessRightModel();
            ar.setPermission(PERMISSION_READ);
            ar.setLicenseeCode(ACCES_RIGHT_EXPERIENCE);
            ar.setLicenseeName(ACCES_RIGHT_EXPERIENCE_DISP);
            ar.setLicenseeCodeType(ACCES_RIGHT_EXPERIENCE_CODE);
            docInfo.addAccessRight(ar);
        }

        // ProgressCourseModule の ModuleInfo を保存しておく
        ModuleInfoBean soaProgressInfo = null;
        ModuleInfoBean pProgressInfo = null;
        ModuleInfoBean[] progressInfos = document.getModuleInfo(MODULE_PROGRESS_COURSE);

        if (progressInfos == null) {
            // 存在しない場合は新規に作成する
            soaProgressInfo = new ModuleInfoBean();
            soaProgressInfo.setStampName(MODULE_PROGRESS_COURSE);
            soaProgressInfo.setEntity(MODULE_PROGRESS_COURSE);
            soaProgressInfo.setStampRole(ROLE_SOA_SPEC);

            pProgressInfo = new ModuleInfoBean();
            pProgressInfo.setStampName(MODULE_PROGRESS_COURSE);
            pProgressInfo.setEntity(MODULE_PROGRESS_COURSE);
            pProgressInfo.setStampRole(ROLE_P_SPEC);

            logger.debug("ModuleInfoBean[] progressInfo created");

        } else {
            if (progressInfos[0].getStampRole().equals(ROLE_SOA_SPEC)) {
                soaProgressInfo = progressInfos[0];
                pProgressInfo = progressInfos[1];
            } else if (progressInfos[1].getStampRole().equals(ROLE_SOA_SPEC)) {
                soaProgressInfo = progressInfos[1];
                pProgressInfo = progressInfos[0];
            }
        }

        //
        // モデルのモジュールをヌルに設定する
        // エディタの画面をダンプして生成したモジュールを設定する
        //
        document.clearModules();
        document.clearSchema();
        logger.debug("model.clearModules(), model.clearSchema()");

        //
        // SOAPane をダンプし model に追加する
        //
        KartePaneDumper_2 dumper = new KartePaneDumper_2();
        KarteStyledDocument doc = (KarteStyledDocument) soaPane.getTextPane().getDocument();

        dumper.dump(doc);
        dumper.getModule().forEach(document::addModule);

        // ProgressCourse SOA を生成する
        ProgressCourse soaPc = new ProgressCourse();
        soaPc.setFreeText(dumper.getSpec());
        ModuleModel soaProgressModule = new ModuleModel();
        soaProgressModule.setModuleInfo(soaProgressInfo);
        soaProgressModule.setModel(soaPc);
        document.addModule(soaProgressModule);

        //
        // Schema を追加する
        //
        dumper.getSchema().forEach(document::addSchema);

        //
        // PPane をダンプし model に追加する
        //
        KarteStyledDocument pdoc = (KarteStyledDocument) pPane.getTextPane().getDocument();
        dumper.dump(pdoc);
        dumper.getModule().forEach(document::addModule);

        // ProgressCourse P を生成する
        ProgressCourse pProgressCourse = new ProgressCourse();
        pProgressCourse.setFreeText(dumper.getSpec());
        ModuleModel pProgressModule = new ModuleModel();
        pProgressModule.setModuleInfo(pProgressInfo);
        pProgressModule.setModel(pProgressCourse);
        document.addModule(pProgressModule);

        // FLAGを設定する
        // image があるかどうか
        boolean flag = document.getSchema() != null;
        docInfo.setHasImage(flag);
        logger.debug("hasImage = " + flag);

        // RP があるかどうか
        flag = document.getModule(ENTITY_MED_ORDER) != null;
        docInfo.setHasRp(flag);
        logger.debug("hasRp = " + flag);

        // 処置があるかどうか
        flag = document.getModule(ENTITY_TREATMENT) != null;
        docInfo.setHasTreatment(flag);
        logger.debug("hasTreatment = " + flag);

        // LaboTest があるかどうか
        flag = document.getModule(ENTITY_LABO_TEST) != null;
        docInfo.setHasLaboTest(flag);
        logger.debug("hasLaboTest = " + flag);

        //
        // EJB3.0 Model の関係を構築する
        //
        // confirmed, firstConfirmed は設定済み
        KarteBean karte = getContext().getKarte();
        document.setKarte(karte);                          // karte
        document.setCreator(Project.getUserModel());       // 記録者
        document.setRecorded(docInfo.getConfirmDate());    // 記録日

        // Moduleとの関係を設定する
        Collection<ModuleModel> moduleBeans = document.getModules();
        int number = 0;
        int totalSize = 0;
        for (ModuleModel bean : moduleBeans) {

            bean.setId(0L);                             // unsaved-value
            bean.setKarte(karte);                       // Karte
            bean.setCreator(Project.getUserModel());    // 記録者
            bean.setDocument(document);                    // Document
            bean.setConfirmed(docInfo.getConfirmDate());            // 確定日
            bean.setFirstConfirmed(docInfo.getFirstConfirmDate());  // 適合開始日
            bean.setRecorded(docInfo.getConfirmDate());             // 記録日
            bean.setStatus(STATUS_FINAL);                           // status

            // 全角を Kill する
            if (bean.getModel() instanceof BundleMed) {
                BundleMed med = (BundleMed) bean.getModel();
                ClaimItem[] items = med.getClaimItem();
                if (items != null && items.length > 0) {
                    for (ClaimItem item : items) {
                        String num = item.getNumber();
                        if (num != null) {
                            num = StringTool.toHankakuNumber(num);
                            item.setNumber(num);
                        }
                    }
                }
                String bNum = med.getBundleNumber();
                if (bNum != null) {
                    bNum = StringTool.toHankakuNumber(bNum);
                    med.setBundleNumber(bNum);
                }
            } else if (bean.getModel() instanceof ClaimBundle) {
                ClaimBundle bundle = (ClaimBundle) bean.getModel();
                ClaimItem[] items = bundle.getClaimItem();
                if (items != null && items.length > 0) {
                    for (ClaimItem item : items) {
                        String num = item.getNumber();
                        if (num != null) {
                            num = StringTool.toHankakuNumber(num);
                            item.setNumber(num);
                        }
                    }
                }
                String bNum = bundle.getBundleNumber();
                if (bNum != null) {
                    bNum = StringTool.toHankakuNumber(bNum);
                    bundle.setBundleNumber(bNum);
                }
            }

            bean.setBeanBytes(ModelUtils.xmlEncode(bean.getModel()));

            // ModuleInfo を設定する
            // Name, Role, Entity は設定されている
            ModuleInfoBean mInfo = bean.getModuleInfo();
            mInfo.setStampNumber(number++);

            int size = bean.getBeanBytes().length / 1024;
            logger.debug("stamp size(KB) = " + size);
            totalSize += size;
        }
        logger.debug("stamp total size(KB) = " + totalSize);

        // 画像との関係を設定する
        number = 0;
        Collection<SchemaModel> imagesimages = document.getSchema();
        if (imagesimages != null && imagesimages.size() > 0) {
            for (SchemaModel bean : imagesimages) {
                bean.setId(0L);                                         // unsaved
                bean.setKarte(karte);                                   // Karte
                bean.setCreator(Project.getUserModel());                // Creator
                bean.setDocument(document);                                // Document
                bean.setConfirmed(docInfo.getConfirmDate());            // 確定日
                bean.setFirstConfirmed(docInfo.getFirstConfirmDate());  // 適合開始日
                bean.setRecorded(docInfo.getConfirmDate());             // 記録日
                bean.setStatus(STATUS_FINAL);                           // Status
                bean.setImageNumber(number);

                ExtRefModel ref = bean.getExtRef();
                String href = String.format("%s-%d.jpg", document.getDocInfo().getDocId(), number);
                ref.setHref(href);

                number++;
            }
        }
    }

    /**
     * このエディタの状態インターフェース.
     */
    private interface EditorState {
        /**
         * Dirty かどうか.
         *
         * @return Dirty
         */
        boolean isDirty();

        /**
         * Menu 項目のコントロール.
         */
        void controlMenu();
    }

    /**
     * No dirty 状態クラス.
     */
    private final class NoDirtyState implements EditorState {

        @Override
        public void controlMenu() {
            Chart chart = getContext();
            chart.enabledAction(GUIConst.ACTION_SAVE, false); // 保存
            chart.enabledAction(GUIConst.ACTION_PRINT, false); // 印刷
            chart.enabledAction(GUIConst.ACTION_CUT, false);
            chart.enabledAction(GUIConst.ACTION_COPY, false);
            chart.enabledAction(GUIConst.ACTION_PASTE, false);
            chart.enabledAction(GUIConst.ACTION_UNDO, false);
            chart.enabledAction(GUIConst.ACTION_REDO, false);
            chart.enabledAction(GUIConst.ACTION_INSERT_TEXT, false);
            chart.enabledAction(GUIConst.ACTION_INSERT_SCHEMA, false);
            chart.enabledAction(GUIConst.ACTION_INSERT_STAMP, false);
            chart.enabledAction(GUIConst.ACTION_SELECT_INSURANCE, !modify);

            chart.enabledAction(GUIConst.ACTION_FIND_FIRST, false);
            chart.enabledAction(GUIConst.ACTION_FIND_NEXT, false);
            chart.enabledAction(GUIConst.ACTION_FIND_PREVIOUS, false);
        }

        @Override
        public boolean isDirty() {
            return false;
        }
    }

    /**
     * Dirty 状態クラス.
     */
    private final class DirtyState implements EditorState {

        @Override
        public void controlMenu() {
            Chart chart = getContext();
            chart.enabledAction(GUIConst.ACTION_SAVE, true);
            chart.enabledAction(GUIConst.ACTION_PRINT, true);
            chart.enabledAction(GUIConst.ACTION_SELECT_INSURANCE, !modify);

            chart.enabledAction(GUIConst.ACTION_FIND_FIRST, false);
            chart.enabledAction(GUIConst.ACTION_FIND_NEXT, false);
        }

        @Override
        public boolean isDirty() {
            return true;
        }
    }

    /**
     * EmptyNew 状態クラス.
     */
    private final class SavedState implements EditorState {

        @Override
        public void controlMenu() {
            Chart chart = getContext();
            chart.enabledAction(GUIConst.ACTION_SAVE, false);
            chart.enabledAction(GUIConst.ACTION_PRINT, true);
            chart.enabledAction(GUIConst.ACTION_CUT, false);
            chart.enabledAction(GUIConst.ACTION_COPY, false);
            chart.enabledAction(GUIConst.ACTION_PASTE, false);
            chart.enabledAction(GUIConst.ACTION_UNDO, false);
            chart.enabledAction(GUIConst.ACTION_REDO, false);
            chart.enabledAction(GUIConst.ACTION_INSERT_TEXT, false);
            chart.enabledAction(GUIConst.ACTION_INSERT_SCHEMA, false);
            chart.enabledAction(GUIConst.ACTION_INSERT_STAMP, false);
            chart.enabledAction(GUIConst.ACTION_SELECT_INSURANCE, false);

            chart.enabledAction(GUIConst.ACTION_FIND_FIRST, false);
            chart.enabledAction(GUIConst.ACTION_FIND_NEXT, false);
        }

        @Override
        public boolean isDirty() {
            return false;
        }
    }

    /**
     * 状態マネージャ.
     */
    private final class StateMgr {

        private final EditorState noDirtyState = new NoDirtyState();
        private final EditorState dirtyState = new DirtyState();
        private final EditorState savedState = new SavedState();
        private EditorState currentState;

        public StateMgr() {
            currentState = noDirtyState;
        }

        public boolean isDirty() {
            return currentState.isDirty();
        }

        public void setDirty(boolean dirty) {
            currentState = dirty ? dirtyState : noDirtyState;
            currentState.controlMenu();
        }

        public void setSaved(boolean saved) {
            if (saved) {
                currentState = savedState;
                currentState.controlMenu();
            }
        }

        public void controlMenu() {
            currentState.controlMenu();
        }
    }
}
