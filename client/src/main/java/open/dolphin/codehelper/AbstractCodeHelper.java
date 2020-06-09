package open.dolphin.codehelper;

import open.dolphin.client.*;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.stampbox.StampBoxPlugin;
import open.dolphin.stampbox.StampTree;
import open.dolphin.stampbox.StampTreeMenuBuilder;
import open.dolphin.stampbox.StampTreeMenuEvent;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * KartePane の抽象コードヘルパークラス.
 *
 * @author Kazyshi Minagawa
 * @author pns
 */
public abstract class AbstractCodeHelper {

    /**
     * キーワードの境界となる文字
     */
    public static final String[] WORD_SEPARATOR = {" ", "　", "，", ",", "、", "。", ".", "：", ":", "；", ";", "\n", "\t"};
    /**
     * 対応する ChartImpl
     */
    private final ChartImpl realChart;
    /**
     * ChartMediator
     */
    private final ChartMediator mediator;
    /**
     * KartePane の JTextPane
     */
    private final JTextPane textPane;
    /**
     * DiagnosisDocument の JTable
     */
    private final DiagnosisDocumentTable diagTable;
    /**
     * Preferences
     */
    private final Preferences prefs = Preferences.userNodeForPackage(AbstractCodeHelper.class);
    /**
     * 補完リストメニュー
     */
    private JPopupMenu popup;
    /**
     * キーワードの開始位置
     */
    private int start;
    /**
     * キーワードの終了位置
     */
    private int end;

    /**
     * Creates a new instance of CodeHelper.
     *
     * @param kartePane
     * @param chartMediator
     */
    public AbstractCodeHelper(KartePane kartePane, ChartMediator chartMediator) {

        mediator = chartMediator;
        textPane = kartePane.getTextPane();
        realChart = (ChartImpl) ((EditorFrame) kartePane.getParent().getContext()).getChart();
        diagTable = realChart.getDiagnosisDocument().getDiagnosisTable();

        int modifier = prefs.get("modifier", "ctrl").equals("ctrl") ? KeyEvent.CTRL_DOWN_MASK : KeyEvent.META_DOWN_MASK;

        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getModifiersEx() == modifier) && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    buildAndShowPopup();
                }
            }
        });
    }

    /**
     * 単語の境界からキャレットの位置までのテキストを取得し，長さがゼロ以上でれば補完メニューをポップアップする.
     */
    protected void buildAndShowPopup() {

        end = textPane.getCaretPosition();
        start = end;
        boolean found = false;

        while (start > 0) {

            start--;

            try {
                String text = textPane.getText(start, 1);
                for (String test : WORD_SEPARATOR) {
                    if (test.equals(text)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    start++;
                    break;
                }

            } catch (BadLocationException e) {
                e.printStackTrace(System.err);
            }
        }

        try {
            String str = textPane.getText(start, end - start);

            if (str.length() > 0) {
                buildPopup(str);
                showPopup();
            }

        } catch (BadLocationException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * popup menu を構築する.
     *
     * @param text キーワード
     */
    protected abstract void buildPopup(String text);

    /**
     * popup menu を表示する.
     */
    protected void showPopup() {

        if (popup == null || popup.getComponentCount() < 1) {
            return;
        }

        try {
            int pos = textPane.getCaretPosition();
            Rectangle r = textPane.modelToView(pos);

            popup.show(textPane, r.x, r.y);

        } catch (BadLocationException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 引数の entityに対応するスタンプの popup を作る.
     *
     * @param entity
     */
    protected void buildEntityPopup(String entity) {

        StampBoxPlugin stampBox = mediator.getStampBox();
        StampTree tree = stampBox.getStampTree(entity);
        if (tree == null) {
            return;
        }

        popup = new JPopupMenu();

        StampTreeMenuBuilder builder = new StampTreeMenuBuilder(tree);
        builder.addStampTreeMenuListener(this::importStamp);
        builder.build(popup);
    }

    /**
     * SearchText に一致するスタンプの popup を作る.
     *
     * @param trees
     * @param searchText
     */
    protected void buildMatchedPopup(List<StampTree> trees, String searchText) {
        popup = new JPopupMenu();
        String pattern = ".*" + searchText + ".*";

        StampTreeMenuBuilder builder = new StampTreeMenuBuilder(trees, pattern);
        builder.addStampTreeMenuListener(this::importStamp);
        builder.buildRootless(popup);
    }

    /**
     * StampTree menu action.
     *
     * @param e
     */
    public void importStamp(StampTreeMenuEvent e) {
        JComponent comp;
        boolean isDiagnosis = e.getEntity().equals(IInfoModel.ENTITY_DIAGNOSIS);

        textPane.setSelectionStart(start);
        textPane.setSelectionEnd(end);

        if (isDiagnosis) {
            comp = diagTable;

            // 病名の場合は "dx" だけ消して，入力した検索語は使うので残す
            if (textPane.getSelectedText().equals(prefs.get(IInfoModel.ENTITY_DIAGNOSIS, "dx"))) {
                textPane.replaceSelection("");
            } else {
                // 選択クリア
                textPane.setSelectionStart(end);
            }
        } else {
            comp = textPane;

            // 病名以外の場合はテキストは不要なので消す
            textPane.replaceSelection("");
        }
        comp.getTransferHandler().importData(new TransferHandler.TransferSupport(comp, e.getTransferable()));
    }

    /**
     * ChartMediator を返す.
     *
     * @return
     */
    protected ChartMediator getMediator() {
        return mediator;
    }

    /**
     * Preferences を返す.
     *
     * @return
     */
    protected Preferences getPreferences() {
        return prefs;
    }
}
