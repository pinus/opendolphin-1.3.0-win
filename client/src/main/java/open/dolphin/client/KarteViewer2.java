package open.dolphin.client;

import open.dolphin.dnd.KartePaneTransferHandler;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.util.ModelUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.print.PageFormat;
import java.util.Objects;

/**
 * KarteViewer2. 2号カルテを View する ChartDocument.
 * KartePanel はタイムスタンプ, SoaTextPane, PTextPane を含んだ JPanel. これを KarteDocumentViewer で見る.
 * KartePane は KartePanel の SoaTextPane または PTextPane を含んだ KarteComposite. いろいろな機能を持つ.
 * Viewer で KartePane も生成される.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class KarteViewer2 extends AbstractChartDocument implements Comparable<KarteViewer2> {

    // タイムスタンプの foreground カラー
    private static final Color TIMESTAMP_FORE = Color.BLUE;
    // タイムスタンプのフォントサイズ
    private static final int TIMESTAMP_FONT_SIZE = 12;
    // タイムスタンプフォント
    private static final Font TIMESTAMP_FONT = new Font("Dialog", Font.PLAIN, TIMESTAMP_FONT_SIZE);
    private static final Font TIMESTAMP_FONT_BOLD = new Font("Dialog", Font.BOLD, TIMESTAMP_FONT_SIZE);
    // 仮保存中のドキュメントを表す文字
    private static final String UNDER_TMP_SAVE = " - 仮保存中";

    // タイムスタンプラベル
    private JLabel timeStampLabel;
    // SOA Pane
    private KartePane soaPane;
    // P Pane
    private KartePane pPane;
    // 2号カルテ
    private KartePanel kartePanel;

    private boolean avoidEnter;

    // 選択されているかどうかのフラグ
    private boolean selected;


    public KarteViewer2() {
    }

    /**
     * SOA Pane を返す.
     *
     * @return soaPane
     */
    public KartePane getSOAPane() {
        return soaPane;
    }

    /**
     * P Pane を返す.
     *
     * @return pPane
     */
    public KartePane getPPane() {
        return pPane;
    }

    /**
     * KartePanel を返す.
     *
     * @return KartePanel
     */
    public KartePanel getKartePanel() {
        return kartePanel;
    }

    /**
     * ２号カルテで初期化する. start() から呼ばれる.
     */
    private void initialize() {

        kartePanel = KartePanelFactory.createViewerPanel();

        // viewer では編集不可: start() でも init(false,mediator) しているが，
        // 表示直後の ChartImpl の ToolBar の制御に間に合わない.
        kartePanel.getSoaTextPane().setEditable(false);
        kartePanel.getPTextPane().setEditable(false);

        // TimeStampLabel を生成する
        timeStampLabel = kartePanel.getTimeStampLabel();
        timeStampLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeStampLabel.setForeground(TIMESTAMP_FORE);
        timeStampLabel.setFont(TIMESTAMP_FONT);

        // SOA Pane を生成する
        soaPane = new KartePane();
        soaPane.setTextPane(kartePanel.getSoaTextPane());
        soaPane.getTextPane().setTransferHandler(new KartePaneTransferHandler(soaPane));
        soaPane.setRole(IInfoModel.ROLE_SOA);
        soaPane.setParent(this);
        if (Objects.nonNull(getDocument())) {
            // Schema 画像にファイル名を付けるのために必要
            String docId = getDocument().getDocInfo().getDocId();
            soaPane.setDocId(docId);
        }

        // P Pane を生成する
        pPane = new KartePane();
        pPane.setTextPane(kartePanel.getPTextPane());
        pPane.getTextPane().setTransferHandler(new KartePaneTransferHandler(pPane));
        pPane.setRole(IInfoModel.ROLE_P);
        pPane.setParent(this);

        setUI(kartePanel);
    }

    /**
     * プログラムを開始する.
     */
    @Override
    public void start() {
        // Creates GUI
        initialize();

        DocumentModel document = getDocument();

        // Model を表示する
        if (document != null) {

            StringBuilder timeStamp = new StringBuilder();
            String dateFormat = IInfoModel.KARTE_DATE_FORMAT;

            // time stamp
            String firstConfirmDate = ModelUtils.getDateAsFormatString(document.getDocInfo().getFirstConfirmDate(), dateFormat);
            timeStamp.append(firstConfirmDate);

            // 修正日表示
            String modifyDate = ModelUtils.getDateAsFormatString(document.getDocInfo().getConfirmDate(), dateFormat);
            boolean showModified = getContext().getDocumentHistory().isShowModified(); // 修正履歴表示モードかどうか
            String parent = document.getDocInfo().getParentId(); // 親があるかどうか

            if (showModified && Objects.nonNull(parent)) {
                timeStamp.append(" [");
                timeStamp.append(MODIFY_MARK); // update マーク
                timeStamp.append(modifyDate);
                timeStamp.append("]");
            }

            if (document.getDocInfo().getStatus().equals(IInfoModel.STATUS_TMP)) {
                timeStamp.append(UNDER_TMP_SAVE); // - 仮保存中
            }

            // timeStamp にカルテ作成者を入れる
            String drName = document.getCreator().getCommonName();
            timeStamp.append(" 記載医師: ");
            timeStamp.append(drName);

            timeStampLabel.setText(timeStamp.toString());
            KarteRenderer_2 renderer = new KarteRenderer_2(soaPane, pPane);
            renderer.render(document);
        }

        // モデル表示後にリスナ等を設定する init(editable, mediator)
        ChartMediator mediator = getContext().getChartMediator();
        soaPane.init(false, mediator);
        pPane.init(false, mediator);
        enter();
    }

    @Override
    public void stop() {
        soaPane.clear();
        pPane.clear();
    }

    public void addMouseListener(MouseListener ml) {
        soaPane.getTextPane().addMouseListener(ml);
        pPane.getTextPane().addMouseListener(ml);
    }

    public String getDocType() {
        return Objects.nonNull(getDocument())
                ? getDocument().getDocInfo().getDocType()
                : null;
    }

    /**
     * KarteDocumentViewer#addKarteViewer 中に enter() をブロックする.
     *
     * @param b block or not
     */
    public void setAvoidEnter(boolean b) {
        avoidEnter = b;
    }

    /**
     * KartePanel を 1枚，ダイアログを出してプリントする.
     * Junzo SATO
     */
    @Override
    public void print() {
        PageFormat pageFormat = getContext().getContext().getPageFormat();
        String name = getContext().getPatient().getFullName();
        kartePanel.printPanel(pageFormat, 1, true, name, kartePanel.getPreferredSize().height + 30);
    }

    /**
     * コンテナからコールされる enter() メソッドでメニューを制御する.
     */
    @Override
    public void enter() {
        if (avoidEnter) {
            return;
        }
        super.enter();

        // ReadOnly 属性
        boolean canEdit = !getContext().isReadOnly();

        // 仮保存かどうか
        boolean tmp = getDocument().getDocInfo().getStatus().equals(IInfoModel.STATUS_TMP);

        // 新規カルテ作成が可能な条件
        boolean newOk = canEdit && (!tmp);

        ChartMediator mediator = getContext().getChartMediator();
        mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(newOk);        // 新規カルテ
        mediator.getAction(GUIConst.ACTION_PRINT).setEnabled(true);             // 印刷
        mediator.getAction(GUIConst.ACTION_MODIFY_KARTE).setEnabled(canEdit);   // 修正
    }

    /**
     * 選択されているかどうかを返す.
     *
     * @return 選択されている時 true
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * 選択状態を設定する.
     * 選択状態により Viewer のタイムスタンプ部分のフォントを変える (BOLD にする).
     *
     * @param selected 選択された時 true
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            timeStampLabel.setFont(TIMESTAMP_FONT_BOLD);
        } else {
            timeStampLabel.setFont(TIMESTAMP_FONT);
        }
    }

    @Override
    public int hashCode() {
        return getDocument().getDocInfo().getDocId().hashCode() + 72;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == this.getClass()) {
            DocInfoModel otheInfo = ((KarteViewer2) other).getDocument().getDocInfo();
            return getDocument().getDocInfo().equals(otheInfo);
        }
        return false;
    }

    /**
     * 順序を DocInfoModel の順序で決める.
     * firstConfirmDate で並べて，同日の場合は confirmDate の順番に並べる.
     *
     * @param other 相手
     * @return compareTo 値
     */
    @Override
    public int compareTo(@NotNull KarteViewer2 other) {
        if (other != null && other.getClass() == this.getClass()) {
            DocInfoModel otheInfo = other.getDocument().getDocInfo();
            return getDocument().getDocInfo().compareTo(otheInfo);
        }
        return -1;
    }
}
