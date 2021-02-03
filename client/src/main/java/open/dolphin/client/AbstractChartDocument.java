package open.dolphin.client;

import open.dolphin.infomodel.DocumentModel;
import open.dolphin.ui.sheet.JSheet;

import javax.swing.*;
import java.awt.*;

/**
 * チャートドキュメントのルートクラス.
 * KarteDocumentViewer, KarteViewer2, KarteEditor で extend.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public abstract class AbstractChartDocument implements ChartDocument {

    public static final String ORIGINAL_MARK = "📌";
    public static final String MODIFY_MARK = "🖋️";
    public static final String PUB_MARK = "🈶️";

    private static final String[] CHART_MENUS = {
            GUIConst.ACTION_OPEN_KARTE, GUIConst.ACTION_SAVE, GUIConst.ACTION_DELETE, GUIConst.ACTION_PRINT, GUIConst.ACTION_MODIFY_KARTE,
            GUIConst.ACTION_ASCENDING, GUIConst.ACTION_DESCENDING, GUIConst.ACTION_SHOW_MODIFIED,
            GUIConst.ACTION_INSERT_TEXT, GUIConst.ACTION_INSERT_SCHEMA,
            GUIConst.ACTION_INSERT_STAMP, GUIConst.ACTION_SIMPLIFY_STAMP, GUIConst.ACTION_SELECT_INSURANCE,
            GUIConst.ACTION_CUT, GUIConst.ACTION_COPY, GUIConst.ACTION_PASTE, GUIConst.ACTION_UNDO, GUIConst.ACTION_REDO,
            GUIConst.ACTION_FIND_FIRST, GUIConst.ACTION_FIND_NEXT, GUIConst.ACTION_FIND_PREVIOUS, GUIConst.ACTION_SEND_CLAIM
    };

     // この ChartDocument を保持する Chart. (ChartImpl or EditorFrame)
    private Chart chartContext;
     // この ChartDocument が保持する DocumentModel.
    private DocumentModel documentModel;
    private String title;
    private JPanel ui;
    private boolean dirty;

    public AbstractChartDocument() {
        initComponent();
    }

    private void initComponent() {
        setUI(new PrintablePanel());
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        getUI().setName(title);
    }

    @Override
    public Chart getContext() {
        return chartContext;
    }

    @Override
    public void setContext(Chart chart) {
        this.chartContext = chart;
    }

    @Override
    public DocumentModel getDocument() {
        return documentModel;
    }

    @Override
    public void setDocument(DocumentModel model) {
        documentModel = model;
    }

    @Override
    public void enter() {
        chartContext.getStatusPanel().setText("", "message");
        getContext().getChartMediator().addChartDocumentChain(this);
        disableMenus();
        getContext().enabledAction(GUIConst.ACTION_NEW_KARTE, true);
        getContext().enabledAction(GUIConst.ACTION_SIMPLIFY_STAMP, true);
    }

    @Override
    public JPanel getUI() {
        return ui;
    }

    public void setUI(JPanel ui) {
        this.ui = ui;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isReadOnly() {
        return chartContext.isReadOnly();
    }

    public void disableMenus() {
        // このウインドウに関連する全てのメニューをdisableにする
        ChartMediator mediator = getContext().getChartMediator();
        mediator.disableMenus(CHART_MENUS);
    }

    /**
     * スタンプ標準表示/簡易表示を切り替える.
     *
     * @param doc KarteStyledDocument
     */
    public void toggleSimplifyStamp(KarteStyledDocument doc) {
        doc.setSimplifyStamp(!doc.isSimplifyStamp());
        doc.getStampHolders().stream().forEach(sh -> sh.setSimplify(doc.isSimplifyStamp()));
    }

    /**
     * 共通の警告表示を行う.
     *
     * @param title   タイトル
     * @param message メッセージ
     */
    protected void warning(String title, String message) {
        Window parent = SwingUtilities.getWindowAncestor(getUI());
        // すでに JSheet が出ている場合は，toFront してリターン
        if (JSheet.isAlreadyShown(parent)) {
            parent.toFront();
            return;
        }
        JSheet.showMessageDialog(parent, message, ClientContext.getFrameTitle(title), JOptionPane.WARNING_MESSAGE);
    }
}
