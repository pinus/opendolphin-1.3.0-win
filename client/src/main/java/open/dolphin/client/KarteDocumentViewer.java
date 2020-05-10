package open.dolphin.client;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.delegater.OrcaDelegater;
import open.dolphin.helper.DBTask;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.project.Project;
import open.dolphin.ui.CompletableSearchField;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.PNSScrollPane;
import open.dolphin.ui.sheet.JSheet;
import open.dolphin.util.ModelUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * KarteDocumentViewer.
 * KarteViewer2 をまとめて DocumentBridge の scroller に入れる.
 *
 * @author Minagawa, Kazushi
 */
public class KarteDocumentViewer extends AbstractChartDocument {

    // Busy プロパティ名
    public static final String BUSY_PROP = "busyProp";
    // 更新を表す文字
    private static final String TITLE_UPDATE = "更新";
    private static final String TITLE = "参 照";
    // 検索用
    private final FindAndView findAndView = new FindAndView();
    private final Logger logger = ClientContext.getBootLogger();
    // このアプリケーションは文書履歴を複数選択することができる
    // このリストはそれに対応した KarteViewer(2号カルテ)を保持している
    // このリストの内容（KarteViewer)が一枚のパネルに並べて表示される
    private List<KarteViewer2> karteList;
    // 上記パネル内でマウスで選択されているカルテ(karteViewer)
    // 前回処方を適用した新規カルテはこの選択されたカルテが元になる
    private KarteViewer2 selectedKarte; // 選択されている karteViewer
    // busy プリパティ
    private boolean busy;
    // 文書履を昇順で表示する場合に true
    private boolean ascending;
    // 文書の修正履歴を表示する場合に true
    private boolean showModified;
    // このクラスの状態マネージャ
    private StateMgr stateMgr;
    // 選択を解除されたカルテのリスト
    private List<KarteViewer2> removed;
    // karteViewer を並べたパネル
    private JPanel scrollerPanel;
    // scrollerPanel を表示する JScrollPane: DocumentBridgeImpl で作られる
    //private KarteScrollPane scrollPane;
    private PNSScrollPane scrollPane;
    // 選択された history
    private DocInfoModel[] selectedHistories;
    // 今日の日付 2008-02-12 形式
    private String todayDateAsString;
    // 縦スクロールかどうか
    private boolean isVerticalScroll = true;

    /**
     * DocumentViewerオブジェクトを生成する.
     */
    public KarteDocumentViewer() {
        super();
        initComponents();
    }

    private void initComponents() {
        setTitle(TITLE);

        todayDateAsString = ModelUtils.getDateAsString(new Date());

        Preferences prefs = Project.getPreferences();
        isVerticalScroll = prefs.getBoolean(Project.KARTE_SCROLL_DIRECTION, true);

        scrollerPanel = new JPanel();
        if (isVerticalScroll) {
            scrollerPanel.setLayout(new BoxLayout(scrollerPanel, BoxLayout.Y_AXIS));
        } else {
            scrollerPanel.setLayout(new BoxLayout(scrollerPanel, BoxLayout.X_AXIS));
        }
    }

    @Override
    public void setContext(Chart ctx) {
        super.setContext(ctx);

        // カルテ検索のためのパラメータセット
        ChartImpl chart = (ChartImpl) ctx;
        ChartSearchPanel panel = chart.getChartSearchPanel();
        panel.setParams(findAndView, scrollerPanel);
    }

    /**
     * 表示されているカルテの中身を検索する
     */
    public void findFirst() {
        ChartImpl chart = (ChartImpl) getContext();
        ChartSearchPanel panel = chart.getChartSearchPanel();
        panel.show(ChartSearchPanel.Card.KARTE);

        CompletableSearchField searchField = panel.getKarteSearchField();
        searchField.setText("");
        Focuser.requestFocus(searchField);
 /*
        // すでに JSheet が出ている場合は，toFront してリターン
        Frame parent = getContext().getFrame();
        if (JSheet.isAlreadyShown(parent)) {
            parent.toFront();
            return;
        }
        FindDialog sheet = new FindDialog(parent);
        sheet.start();
        String searchText = sheet.getSearchText();
        if (searchText != null && !searchText.equals("")) {
            findAndView.showFirst(searchText, sheet.isSoaBoxOn(), sheet.isPBoxOn(), scrollerPanel);
        }
*/
    }

    public void findNext() {
        findAndView.showNext(scrollerPanel);
    }

    public void findPrevious() {
        findAndView.showPrevious(scrollerPanel);
    }

    /**
     * 全てを選択　の動作.
     */
    public void selectAll() {
        this.getContext().getDocumentHistory().selectAll();
    }

    /**
     * busy かどうかを返す.
     *
     * @return busy の時 true
     */
    public boolean isBusy() {
        return busy;
    }

    @Override
    public void start() {
        // 文書履歴に昇順／降順，修正履歴表示の設定をする
        // この値の初期値はデフォルト値であり，個々のドキュメント（画面）単位にメニューで変更できる. （適用されるのは個々のドキュメントのみ）
        // デフォルト値の設定は環境設定で行う.
        ascending = getContext().getDocumentHistory().isAscending();
        showModified = getContext().getDocumentHistory().isShowModified();
        karteList = new ArrayList<>(1);
        stateMgr = new StateMgr();

        // 保存されていない編集中カルテチェック
        Autosave.checkForTemporaryFile((ChartImpl) getContext());

        enter();
    }

    @Override
    public void stop() {
        if (karteList != null) {
            karteList.stream().forEach(KarteViewer2::stop);
            karteList.clear();
        }
    }

    @Override
    public void enter() {
        super.enter();
        stateMgr.enter();
    }

    /**
     * 選択されているKarteViwerを返す.
     *
     * @return 選択されているKarteViwer
     */
    public KarteViewer2 getSelectedKarte() {
        boolean found = false;
        for (KarteViewer2 kv : karteList) {
            if (kv == selectedKarte) {
                found = true;
                break;
            }
        }
        if (!found) {
            selectedKarte = null;
        }

        return selectedKarte;
    }

    /**
     * マウスクリック(選択)されたKarteViwerをselectedKarteに設定する.
     * 他のカルテが選択されている場合はそれを解除する.
     * StateMgrを Haskarte State にする.
     *
     * @param view 選択されたKarteViwer
     */
    public void setSelectedKarte(KarteViewer2 view) {

        KarteViewer2 old = getSelectedKarte();
        selectedKarte = view;
        //
        // 他のカルテが選択されている場合はそれを解除する
        //
        if (selectedKarte != old) {
            if (selectedKarte != null) {
                karteList.forEach((karte) -> karte.setSelected(false));
                selectedKarte.setSelected(true);
                stateMgr.processCleanEvent();

            } else {
                // null
                stateMgr.processEmptyEvent();
            }
            scrollPane.repaint();
        }
    }

    /**
     * 新規カルテ作成の元になるカルテを返す.
     *
     * @return 作成の元になるカルテ
     */
    public KarteViewer2 getBaseKarte() {
        KarteViewer2 ret = getSelectedKarte();
        if (ret == null) {
            if (karteList != null && !karteList.isEmpty()) {
                ret = ascending ? karteList.get(karteList.size() - 1) : karteList.get(0);
            }
        }
        return ret;
    }

    /**
     * 文書履歴の抽出期間が変更された場合，
     * karteList をclear，選択されているkarteViewerを解除，sateMgrをNoKarte状態に設定する.
     */
    public void historyPeriodChanged() {
        if (karteList != null) {
            karteList.clear();
        }
        setSelectedKarte(null);
        getContext().showDocument(0);
    }

    /**
     * DocumentBridgeImpl から呼ばれる.
     *
     * @param selected 選択された文書情報 DocInfo 配列
     * @param scroller PNSScrollPane
     */
    public void showDocuments(DocInfoModel[] selected, final PNSScrollPane scroller) {
        getContext().showDocument(0);  // Chart のカルテ参照タブの選択

        //this.scrollPane = (KarteScrollPane) scroller;
        scrollPane = scroller;
        scrollPane.setViewportView(scrollerPanel);
        selectedHistories = selected;

        if (selectedHistories == null || selectedHistories.length == 0) {
            return;
        }

        // KarteTask でデータベースから DocumentModel を取ってきて EDT で addKarteViewer を呼ぶ
        KarteTask task = new KarteTask();
        task.execute();
    }

    /**
     * データベースで検索した KarteModelを Viewer で表示する.
     * KarteTask から呼ばれる
     *
     * @param models List of DocumentModel
     */
    private void addKarteViewer(List<DocumentModel> models) {

        if (models != null) {
            // 選択解除されたリストを入れる
            removed = new ArrayList<>(); // 選択が解除されているもの
            // karteList にあって選択リストにないものは除去リストに入れる
            for (KarteViewer2 viewer : karteList) {
                boolean found = false;
                String id1 = viewer.getDocument().getDocInfo().getDocId();
                for (DocInfoModel selectedDocInfo : selectedHistories) {
                    String id2 = selectedDocInfo.getDocId();
                    if (id1.equals(id2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    removed.add(viewer);
                    viewer.stop();
                }
            }
            // 削除リストを karteList から取り除く
            karteList.removeAll(removed);

            // 追加された DocumentModel から　karteViewer をつくって karteList に加える
            for (DocumentModel karteModel : models) {
                DocInfoModel docInfo = karteModel.getDocInfo();
                String docId = docInfo.getDocId();

                // 重複チェック
                boolean duplicated = false;
                for (KarteViewer2 viewer : karteList) {
                    String viewerId = viewer.getDocument().getDocInfo().getDocId();
                    if (docId.equals(viewerId)) {
                        duplicated = true;
                        break;
                    }
                }
                if (duplicated) {
                    continue;
                }

                // 対応する DocInfoModel を探す
                DocInfoModel foundInfo = null;
                for (DocInfoModel m : selectedHistories) {
                    String selectedDocId = m.getDocId();
                    if (selectedDocId.equals(docId)) {
                        foundInfo = m;
                        break;
                    }
                }

                if (foundInfo != null) {
                    // 実体のある docInfo をセットする
                    karteModel.setDocInfo(foundInfo);
                } else {
                    // 選択が変化して，せっかく取ってきた DocumentModel が無駄になった
                    //logger.info("selection changed, document fetched in vain");
                    break;
                }

                // KarteViewer を生成する
                final KarteViewer2 karteViewer = new KarteViewer2();

                karteViewer.setContext(getContext());
                karteViewer.setDocument(karteModel);
                karteViewer.setAvoidEnter(true);

                // このコールでモデルのレンダリングが開始される
                karteViewer.start();
                // 2号カルテの場合ダブルクリックされたカルテを別画面で表示する
                // MouseListener を生成して KarteViewer の Pane にアタッチする
                if (docInfo.getDocType().equals(IInfoModel.DOCTYPE_KARTE)) {
                    final MouseListener ml = new MouseAdapter() {

                        @Override
                        public void mouseClicked(MouseEvent e) {
                            int cnt = e.getClickCount();
                            if (cnt == 2) {
                                // 選択した Karte を EditoFrame で開く
                                setSelectedKarte(karteViewer);
                                openKarte();
                            } else if (cnt == 1) {
                                setSelectedKarte(karteViewer);
                            }
                        }
                    };
                    karteViewer.addMouseListener(ml);
                }
                // ボーダーをあらかじめ付けておく
                karteViewer.getKartePanel().setBackground(karteViewer.getSOAPane().getTextPane().getBackground());
                karteViewer.setSelected(false);
                karteList.add(karteViewer);
            }

            // 時間軸でソート
            if (ascending) {
                karteList.sort(Comparator.naturalOrder());
            } else {
                karteList.sort(Comparator.reverseOrder());
            }
        }

        if (!karteList.isEmpty()) {

            // JPanel を使い回してメモリ節約をはかる
            scrollerPanel.removeAll();

            karteList.forEach(view -> scrollerPanel.add(view.getUI(), -1)); // index -1 で最後に追加になる
            scrollerPanel.revalidate();
            scrollerPanel.repaint();

            // 編集したときの snap() 取り直しのために通知
            //scrollPane.setViewComponentChanged();
        }
    }

    /**
     * カルテを修正する.
     */
    public void modifyKarte() {
        logger.debug("modifyKarte() in KarteDocumentViewer starts");

        if (getBaseKarte() == null) {
            return;
        }

        ChartImpl chart = (ChartImpl) getContext();
        DocumentModel baseDocumentModel = getBaseKarte().getDocument();

        // すでに修正中の document があれば toFront するだけで帰る
        if (chart.toFrontDocumentIfPresent(baseDocumentModel)) {
            return;
        }
        // 削除フラグが設定されていたらenterしない
        if (IInfoModel.STATUS_DELETE.equals(baseDocumentModel.getDocInfo().getStatus())) {
            return;
        }

        DocumentModel editModel = chart.getKarteModelToEdit(baseDocumentModel);
        KarteEditor editor = chart.createEditor();
        editor.setDocument(editModel);
        editor.setEditable(true);
        editor.setModify(true);

        // このフラグはカルテを別ウインドウで編集するかどうか (使ってない)
        //if (Project.getPreferences().getBoolean(Project.KARTE_PLACE_MODE, true)) {
        EditorFrame editorFrame = new EditorFrame();
        editorFrame.setChart(getContext());
        editorFrame.setKarteEditor(editor);
        editorFrame.start();
        //} else {
        //    editor.setContext(chart);
        //    editor.initialize();
        //    editor.start();
        //    chart.addChartDocument(editor, TITLE_UPDATE);
        //}
    }

    /**
     * masuda-sensei.
     */
    @Override
    public void print() {

        // インスペクタに表示されているカルテをまとめて印刷する.
        // ブザイクなんだけど，あまり使わない機能なのでこれでヨシとする masuda
        // modifyed by pns
        // 背景色が緑だとインクがもったいないので白にする. 選択も解除しておく.
        karteList.forEach((kv) -> {
            //kv.panel2.setBorder(BorderFactory.createEmptyBorder());
            KartePane kp = kv.getSOAPane();
            kp.getTextPane().setBackground(Color.WHITE);
            kp = kv.getPPane();
            kp.getTextPane().setBackground(Color.WHITE);
        });

        // 患者名を取得
        String name = getContext().getPatient().getFullName();
        String id = getContext().getPatient().getPatientId();
        // scrollerPanelを印刷する
        PrintKarteDocumentView.printComponent(scrollerPanel, name, id);

        // 背景色を戻しておく
        karteList.forEach(kv -> {
            //kv.panel2.setBorder(MyBorderFactory.createClearBorder());
            if (kv.isSelected()) {
                setSelectedKarte(kv);
            }
            KartePane kp = kv.getSOAPane();
            kp.getTextPane().setBackground(KartePane.UNEDITABLE_COLOR);
            kp = kv.getPPane();
            kp.getTextPane().setBackground(KartePane.UNEDITABLE_COLOR);
        });
    }

    /**
     * 昇順表示にする.
     */
    public void ascending() {
        ascending = true;
        getContext().getDocumentHistory().setAscending(ascending);
    }

    /**
     * 降順表示にする.
     */
    public void descending() {
        ascending = false;
        getContext().getDocumentHistory().setAscending(ascending);
    }

    /**
     * 修正履歴の表示モードにする.
     */
    public void showModified() {
        showModified = !showModified;
        getContext().getDocumentHistory().setShowModified(showModified);
    }

    /**
     * karteList 内でダブルクリックされたカルテ（文書）を EditorFrame で開く.
     */
    public void openKarte() {
        // ダブルクリックで modifyKarte することにした (isReadOnly対応)
        if (!getContext().isReadOnly()) {
            modifyKarte();
        }
    }

    /**
     * 表示選択されているカルテを論理削除する.
     * 患者を間違えた場合等に履歴に表示されないようにするため.
     */
    public void delete() {

        // 対象のカルテを得る
        KarteViewer2 delete = getBaseKarte();
        if (delete == null) {
            return;
        }

        // Dialog を表示し理由を求める
        String message = "このドキュメントを削除しますか ?   ";
        final JCheckBox box1 = new JCheckBox("作成ミス");
        final JCheckBox box2 = new JCheckBox("診察キャンセル");
        final JCheckBox box3 = new JCheckBox("その他");
        box1.setSelected(true);

        ActionListener al = e -> {
            if (!box1.isSelected() && !box2.isSelected() && !box3.isSelected()) {
                box3.setSelected(true);
            }
        };

        box1.addActionListener(al);
        box2.addActionListener(al);
        box3.addActionListener(al);

        Object[] msg = new Object[5];
        msg[0] = message;
        msg[1] = box1;
        msg[2] = box2;
        msg[3] = box3;
        msg[4] = new JLabel(" ");
        String deleteText = "削除する";
        String cancelText = (String) UIManager.get("OptionPane.cancelButtonText");

        int option = JOptionPane.showOptionDialog(
                this.getUI(),
                msg,
                ClientContext.getFrameTitle("ドキュメント削除"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{deleteText, cancelText},
                cancelText);

        // System.out.println(option);

        // キャンセルの場合はリターンする
        if (option != 0) {
            return;
        }

        //
        // 削除する status = 'D'
        //
        long deletePk = delete.getDocument().getId();
        DocumentDelegater ddl = new DocumentDelegater();
        DeleteTask task = new DeleteTask(getContext(), deletePk, ddl);
        task.execute();
    }

    /**
     * 表示されているカルテを CLAIM 送信する.
     */
    public void sendClaim() {
        logger.debug("sendClaim() in KarteDocumentViewer called.");

        String message;
        int messageType;

        if (!Project.getSendClaim()) {
            message = "CLAIM を送信しない設定になっています";
            messageType = JOptionPane.ERROR_MESSAGE;

        } else {
            // claim を送るのはカルテだけ
            // getBaseKarte() は選択カルテを返す
            String docType = getBaseKarte().getDocument().getDocInfo().getDocType();
            if (!IInfoModel.DOCTYPE_KARTE.equals(docType)) {
                return;
            }

            ChartImpl chart = (ChartImpl) getContext();
            DocumentModel model = chart.getKarteModelToEdit(getBaseKarte().getDocument());
            model.setKarte(getContext().getKarte());
            model.getDocInfo().setConfirmDate(new Date());
            model.setCreator(Project.getUserModel());       // 送信者

            OrcaDelegater delegater = new OrcaDelegater();
            OrcaDelegater.Result result = delegater.sendDocument(model);

            if (result.equals(OrcaDelegater.Result.NO_ERROR)) {
                message = "ORCA に送信しました";
                messageType = JOptionPane.PLAIN_MESSAGE;
            } else {
                message = "ORCA に送信できませんでした";
                messageType = JOptionPane.ERROR_MESSAGE;
            }
        }

        Frame parent = getContext().getFrame();
        if (JSheet.isAlreadyShown(parent)) {
            parent.toFront();
            return;
        }
        JSheet.showMessageDialog(parent, message, "", messageType);
    }

    /**
     * State Interface.
     */
    private interface BrowserState {
        void enter();
    }

    /**
     * 文書をデータベースから取得するタスククラス.
     */
    private class KarteTask extends SwingWorker<Integer, List<DocumentModel>> {
        private final int FRACTION = 10;
        private DocInfoModel[] savedSelectedHistories;
        private JProgressBar progressBar;

        @Override
        protected Integer doInBackground() {
            progressBar = getContext().getStatusPanel().getProgressBar();

            // 選択リストにあって 現在の karteList にないものは追加する
            List<DocInfoModel> added = new ArrayList<>(); // 追加されたもの
            //for (DocInfoModel selectedDocInfo : selectedDocInfoList) {
            savedSelectedHistories = selectedHistories;
            for (DocInfoModel selectedDocInfo : selectedHistories) {
                boolean found = false;
                String id1 = selectedDocInfo.getDocId();
                for (KarteViewer2 viewer : karteList) {
                    String id2 = viewer.getDocument().getDocInfo().getDocId();
                    if (id1.equals(id2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    added.add(selectedDocInfo);
                }
            }

            if (!added.isEmpty()) {
                progressBar.setMaximum(added.size());

                DocumentDelegater ddl = new DocumentDelegater();
                int count = 0;
                boolean hasNext = true;
                while (hasNext) {
                    List<Long> pkList = new ArrayList<>(FRACTION);
                    for (int i = 0; i < FRACTION; i++) {
                        pkList.add(added.get(count++).getDocPk());
                        if (count >= added.size()) {
                            hasNext = false;
                            break;
                        }
                    }
                    // 途中で選択が変わったり，ウインドウが閉じられている可能性がある
                    if (savedSelectedHistories == selectedHistories
                            && getContext().getFrame().isVisible()) {

                        List<DocumentModel> dm = ddl.getDocuments(pkList);
                        publish(dm);
                        progressBar.setValue(count);

                    } else {
                        //logger.info("selection has changed");
                        break;
                    }
                }
            } else {
                // removed 処理のために空の addKarteViewer 呼ぶ必要有り
                addKarteViewer(new ArrayList<>(1));
            }
            return added.size();
        }

        @Override
        protected void done() {
            progressBar.setValue(0);
            // リスト先頭のカルテを選択状態に
            if (!karteList.isEmpty()) {
                setSelectedKarte(karteList.get(0));
            }
        }

        @Override
        protected void process(List<List<DocumentModel>> chunks) {
            //logger.info("process published chunks");
            chunks.forEach(KarteDocumentViewer.this::addKarteViewer);
        }
    }

    /**
     * カルテの削除タスククラス.
     */
    private class DeleteTask extends DBTask<Boolean> {

        private final DocumentDelegater ddl;
        private final long docPk;

        public DeleteTask(Chart ctx, long docPk, DocumentDelegater ddl) {
            super(ctx);
            this.docPk = docPk;
            this.ddl = ddl;
        }

        @Override
        protected Boolean doInBackground() {
            logger.debug("DeleteTask started");
            ddl.deleteDocument(docPk);
            return ddl.isNoError();
        }

        @Override
        protected void succeeded(Boolean result) {
            logger.debug("DeleteTask succeeded");
            if (result) {
                Chart chart = (KarteDocumentViewer.this).getContext();
                chart.getDocumentHistory().update();
            } else {
                warning(ClientContext.getString("ドキュメント削除"), ddl.getErrorMessage());
            }
        }
    }

    /**
     * StateContext クラス.
     */
    private final class StateMgr {

        private final BrowserState emptyState = new EmptyState();
        private final BrowserState cleanState = new CleanState();

        private BrowserState currentState;

        public StateMgr() {
            currentState = emptyState;
        }

        public void processEmptyEvent() {
            currentState = emptyState;
            this.enter();
        }

        public void processCleanEvent() {
            currentState = cleanState;
            this.enter();
        }

        public void enter() {
            currentState.enter();
        }
    }

    /**
     * EmptyState.
     * 表示するカルテがない状態を表す.
     */
    private final class EmptyState implements BrowserState {

        public EmptyState() {
        }

        @Override
        public void enter() {
            boolean canEdit = !isReadOnly();
            getContext().enabledAction(GUIConst.ACTION_NEW_KARTE, canEdit);     // 新規カルテ
            getContext().enabledAction(GUIConst.ACTION_NEW_DOCUMENT, canEdit);  // 新規文書
            getContext().enabledAction(GUIConst.ACTION_MODIFY_KARTE, false);    // 修正
            getContext().enabledAction(GUIConst.ACTION_DELETE, false);          // 削除
            getContext().enabledAction(GUIConst.ACTION_PRINT, false);           // 印刷
            getContext().enabledAction(GUIConst.ACTION_ASCENDING, false);       // 昇順
            getContext().enabledAction(GUIConst.ACTION_DESCENDING, false);      // 降順
            getContext().enabledAction(GUIConst.ACTION_SHOW_MODIFIED, false);   // 修正履歴表示

            getContext().enabledAction(GUIConst.ACTION_FIND_FIRST, false);
            getContext().enabledAction(GUIConst.ACTION_FIND_NEXT, false);
            getContext().enabledAction(GUIConst.ACTION_FIND_PREVIOUS, false);
            getContext().enabledAction(GUIConst.ACTION_SEND_CLAIM, false);
        }
    }

    /**
     * CleanState.
     * カルテが表示されている状態を表す.
     */
    private final class CleanState implements BrowserState {

        public CleanState() {
        }

        @Override
        public void enter() {
            //
            // 新規カルテが可能なケース 仮保存でないことを追加
            //
            boolean canEdit = !isReadOnly();
            boolean tmpKarte = false;
            KarteViewer2 base = getBaseKarte();
            if (base != null) {
                String state = base.getDocument().getDocInfo().getStatus();
                String confirmDate = base.getDocument().getDocInfo().getConfirmDateTrimTime();

                // もし今日のカルテが一時保存なら新規カルテは作らない
                if (state.equals(IInfoModel.STATUS_TMP) && todayDateAsString.equals(confirmDate)) {
                    tmpKarte = true;
                }
            }
            boolean newOk = canEdit && !tmpKarte;
            getContext().enabledAction(GUIConst.ACTION_NEW_KARTE, newOk);        // 新規カルテ
            getContext().enabledAction(GUIConst.ACTION_NEW_DOCUMENT, canEdit);   // 新規文書
            getContext().enabledAction(GUIConst.ACTION_MODIFY_KARTE, canEdit);   // 修正
            getContext().enabledAction(GUIConst.ACTION_DELETE, canEdit);         // 削除
            getContext().enabledAction(GUIConst.ACTION_PRINT, true);             // 印刷
            getContext().enabledAction(GUIConst.ACTION_ASCENDING, true);         // 昇順
            getContext().enabledAction(GUIConst.ACTION_DESCENDING, true);        // 降順
            getContext().enabledAction(GUIConst.ACTION_SHOW_MODIFIED, true);     // 修正履歴表示

            getContext().enabledAction(GUIConst.ACTION_FIND_FIRST, true);
            getContext().enabledAction(GUIConst.ACTION_FIND_NEXT, true);
            getContext().enabledAction(GUIConst.ACTION_FIND_PREVIOUS, true);
            getContext().enabledAction(GUIConst.ACTION_SEND_CLAIM, true);
        }
    }
}
