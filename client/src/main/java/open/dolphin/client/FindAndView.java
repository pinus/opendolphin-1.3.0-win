package open.dolphin.client;

import open.dolphin.ui.sheet.JSheet;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author pns thx for masuda sensei
 */
public class FindAndView {

    private static final Color SELECTED_COLOR = new Color(255, 180, 66); //カーソルがある部分の色
    private static final String SELECTED_COLOR_HEX = "#FFB442";
    private static final Color FOUND_COLOR = new Color(243, 255, 15); //黄色っぽい色
    private static final String FOUND_COLOR_HEX = "#F3FF0F";
    //private static final Color SELECTED_BORDER = new Color(255, 0, 153); //stampHolder の選択色

    private final SimpleAttributeSet foundAttr = new SimpleAttributeSet(); // 見つかった
    private final SimpleAttributeSet onCursorAttr = new SimpleAttributeSet(); // 現在いるところ
    private final SimpleAttributeSet defaultAttr = new SimpleAttributeSet(); // もともとの背景色（panelに応じて変化）

    // StampHolder にマークするためのタグ
    private final String FONT_END = "</font>";
    private final String FONT_FOUND = "<font style=\"background-color:" + FOUND_COLOR_HEX + "\">";
    private final String FONT_SELECTED = "<font style=\"background-color:" + SELECTED_COLOR_HEX + "\">";
    private String searchText;
    private JPanel scrollerPanel; // 検索対象の Panel (KarteDocumentViewer からもってくる
    private FoundDataList foundDataList;
    private int row; // 現在の row

    private Chart chart;

    public FindAndView(Chart context) {
        chart = context;
        foundAttr.addAttribute(StyleConstants.Background, FOUND_COLOR);
        onCursorAttr.addAttribute(StyleConstants.Background, SELECTED_COLOR);
    }

    /**
     * findFirst 検索対象のパネルをスキャンして，検索結果を positions データベースに入れる.
     * さらに最初に見つかった部分を表示する
     *
     * @param text    target text
     * @param soaIsOn include soa or not
     * @param pIsOn   include p or not
     * @param panel   target panel
     */
    public void showFirst(String text, boolean soaIsOn, boolean pIsOn, JPanel panel) {

        foundDataList = new FoundDataList(); // 検索結果を入れるためのテーブル
        searchText = text;
        scrollerPanel = panel;

        int kpCount = panel.getComponentCount(); // panel に組み込まれている kartePanel の数
        if (kpCount == 0) { return; }

        int kpHeight = 0; // 後でソートするために，scrollerPanel 上の y 座標を記録するとき使う
        defaultAttr.addAttribute(StyleConstants.Background,
                ((KartePanel) panel.getComponent(0)).getSoaTextPane().getBackground());

        // 前回検索のマーキング全部クリアする
        clearMarking(panel);

        for (int i = 0; i < kpCount; i++) {
            KartePanel kp = (KartePanel) panel.getComponent(i);
            JTextPane soaPane = kp.getSoaTextPane();
            JTextPane pPane = kp.getPTextPane();

            try {
                // soa Pane の text 検索 --------------------------------------------
                if (soaIsOn) {
                    // 最初の検索
                    int pos = soaPane.getText().indexOf(text);
                    // もし見つかったら，マーキングと positions データベース登録
                    while (pos != -1) {
                        //　見つかったテキストに，foundAttr をセットして，見つかった位置の y 座標を positions データベースに入れる
                        int y = kpHeight + soaPane.modelToView(pos).y;
                        foundDataList.addRow(y, true, soaPane, pos, null);
                        // 次の検索
                        pos = soaPane.getText().indexOf(text, pos + 1);
                    }
                }
                // pPane の text, stamp 検索 ----------------------------------------
                if (pIsOn) {
                    // まずは text の検索
                    int pos = pPane.getText().indexOf(text);
                    // もし見つかったら，マーキングと positions データベース登録
                    while (pos != -1) {
                        //　見つかったテキストに，foundAttr をセットして，見つかった位置の y 座標を positions データベースに入れる
                        int y = kpHeight + pPane.modelToView(pos).y;
                        foundDataList.addRow(y, true, pPane, pos, null);

                        // 次の検索
                        pos = pPane.getText().indexOf(text, pos + 1);
                    }

                    // 次に stamp 検索
                    KarteStyledDocument kd = (KarteStyledDocument) pPane.getStyledDocument();
                    List<StampHolder> list = kd.getStampHolders();
                    for (StampHolder sh : list) {
                        // sh は JLabel で HTML で setText してあるので，searchText に HTML タグが
                        // 入ってたりすると大変なことになるが，めんどくさいので処理はしない
                        String stampText = sh.getText();
                        pos = stampText.indexOf(searchText);
                        if (pos != -1) { // 見つかった
                            int y = kpHeight + pPane.modelToView(sh.getStartPos()).y;
                            foundDataList.addRow(y, false, pPane, sh.getStartPos(), sh);
                        }
                    }
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace(System.err);
            }

            kpHeight += kp.getHeight(); // kartePanel の高さ分だけずらす
        }

        if (!foundDataList.isEmpty()) {
            foundDataList.sort();

            // 全てマーキング
//            for (int i=0; i<foundDataList.getRowCount(); i++) {
//                if (foundDataList.isText(i)) {
//                    setFoundAttr(foundDataList.getPane(i), foundDataList.getPos(i));
//                } else {
//                    setFoundAttr(foundDataList.getStamp(i));
//                }
//            }

            // 最初に見つかった部分に移動
            row = 0;
            if (foundDataList.isText(row)) {
                setOnCursorAttr(foundDataList.getPane(row), foundDataList.getPos(row));
            } else {
                setOnCursorAttr(foundDataList.getStamp(row));
            }
            scrollToCenter(panel, foundDataList.getPane(row), foundDataList.getPos(row));

            // enable findNext action
            chart.enabledAction(GUIConst.ACTION_FIND_NEXT, true);
            chart.enabledAction(GUIConst.ACTION_FIND_PREVIOUS, true);

        } else {
            showNotFoundDialog("検索", "はみつかりませんでした");
            // disable findNext action
            chart.enabledAction(GUIConst.ACTION_FIND_NEXT, false);
            chart.enabledAction(GUIConst.ACTION_FIND_PREVIOUS, false);
        }
    }

    /**
     * 検索結果データベース(positions)を元に見つかった部分を表示する.
     *
     * @param panel target panel
     * @param next  true=次を検索
     */
    private void show(JPanel panel, boolean next) {
        // すでに JSheet が出ている場合は，toFront してリターン
        if (JSheet.isAlreadyShown(scrollerPanel)) {
            SwingUtilities.getWindowAncestor(scrollerPanel).toFront();
            return;
        }

        // 検索結果が１つだったらすぐリターン
        if (foundDataList.getRowCount() <= 1) {
            showNotFoundDialog(next ? "次を検索" : "前を検索", "はこれだけです");
            return;
        }

        // scrollerPanel が変化していなければ次を探す. 変化していたらクリア.
        if (panel == scrollerPanel) {

            // 次の検索の前に onCursorAttr を foundAttr に戻す
            // if (isText(row)) setFoundAttr(getPane(row), getPos(row));
            // else setFoundAttr(getStamp(row));
            if (foundDataList.isText(row)) {
                removeAttr(foundDataList.getPane(row), foundDataList.getPos(row));
            } else {
                removeAttr(foundDataList.getStamp(row));
            }

            // findNext or findPrevious
            if (next) {
                row++;
                if (row == foundDataList.getRowCount()) {
                    row = 0;
                    if (showConfirmDialog("文書の最後まで検索しました。最初からもう一度検索しますか？") == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
            } else {
                row--;
                if (row < 0) {
                    row = foundDataList.getRowCount() - 1;
                    if (showConfirmDialog("文書の最初に戻りました。最後からもう一度検索しますか？") == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
            }

            // 次の見つかった部分を表示して onCursorAttr セット
            if (foundDataList.isText(row)) {
                setOnCursorAttr(foundDataList.getPane(row), foundDataList.getPos(row));
            } else {
                setOnCursorAttr(foundDataList.getStamp(row));
            }
            scrollToCenter(panel, foundDataList.getPane(row), foundDataList.getPos(row));

        } else {
            // scroller が変わってたら，マーキングを全てクリアする
            clearMarking(panel);
        }
    }

    /**
     * 検索文字列をハイライト表示するための関連メソッド.
     *
     * @param pane JTextPane
     * @param pos  position
     */
    private void setOnCursorAttr(JTextPane pane, int pos) {
        pane.getStyledDocument().setCharacterAttributes(pos, searchText.length(), onCursorAttr, false);
    }

    private void setOnCursorAttr(StampHolder sh) {
        setAttr(sh, FONT_SELECTED);
    }

    private void setFoundAttr(JTextPane pane, int pos) {
        pane.getStyledDocument().setCharacterAttributes(pos, searchText.length(), foundAttr, false);
    }

    private void setFoundAttr(StampHolder sh) {
        setAttr(sh, FONT_FOUND);
    }

    private void setAttr(StampHolder sh, String fontTag) {
        sh.setAttr(searchText, fontTag, FONT_END);
        //sh.repaint();
    }

    private void removeAttr(JTextPane pane, int pos) {
        pane.getStyledDocument().setCharacterAttributes(pos, searchText.length(), defaultAttr, false);
    }

    private void removeAttr(StampHolder sh) {
        sh.removeAttr();
        //sh.repaint();
    }

    public void showNext(JPanel panel) {
        show(panel, true);
    }

    public void showPrevious(JPanel panel) {
        show(panel, false);
    }

    /**
     * 検索してカーソルがある部分をできるだけ画面の中央に表示する.
     *
     * @param panel target panel
     * @param pane  JTextPane
     * @param pos   position
     */
    private void scrollToCenter(JPanel panel, JTextPane pane, int pos) {
        try {
            Rectangle r = pane.modelToView(pos);
            int h = panel.getParent().getBounds().height; // viewport の高さ
            r.y -= h / 2;
            r.height = h;
            // 上のようにすると，同じ画面に found text があっても，いちいちスクロールしてしまう
            //r.height += 100;
            pane.scrollRectToVisible(r);

        } catch (BadLocationException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * マーキングを全てクリアする.
     *
     * @param panel target panel
     */
    private void clearMarking(JPanel panel) {
        int kpCount = panel.getComponentCount(); // panel に組み込まれている kartePanel の数

        for (int i = 0; i < kpCount; i++) {
            // text
            KartePanel kp = (KartePanel) panel.getComponent(i);
            JTextPane soaPane = kp.getSoaTextPane();
            JTextPane pPane = kp.getPTextPane();

            soaPane.getStyledDocument().setCharacterAttributes(0, soaPane.getText().length(), defaultAttr, false);
            pPane.getStyledDocument().setCharacterAttributes(0, pPane.getText().length(), defaultAttr, false);

            // stamp
            KarteStyledDocument kd = (KarteStyledDocument) pPane.getStyledDocument();
            for (int j = 0; j < kd.getLength(); j++) {
                StampHolder sh = (StampHolder) StyleConstants.getComponent(kd.getCharacterElement(j).getAttributes());
                if (sh != null) {
                    removeAttr(sh);
                }
            }
        }
    }

    private void showNotFoundDialog(String title, String message) {
        JSheet.showMessageDialog(scrollerPanel.getParent(),
                "「" + searchText + "」" + message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    private int showConfirmDialog(String message) {
        return JSheet.showConfirmDialog(scrollerPanel.getParent(),
                message,
                "",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * 検索で見つかったデータを入れておく model.
     */
    private class FoundDataList {

        private final List<Model> list;

        public FoundDataList() {
            list = new ArrayList<>();
        }

        public void addRow(int y, boolean isText, JTextPane pane, int pos, StampHolder sh) {
            list.add(new Model(y, isText, pane, pos, sh));
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        public int getRowCount() {
            return list.size();
        }

        public void sort() {
            list.sort(Comparator.naturalOrder());
        }

        public int getY(int row) {
            return list.get(row).y;
        }

        public boolean isText(int row) {
            return list.get(row).isText;
        }

        public JTextPane getPane(int row) {
            return list.get(row).pane;
        }

        public int getPos(int row) {
            return list.get(row).pos;
        }

        public StampHolder getStamp(int row) {
            return list.get(row).stampHolder;
        }

        private class Model implements Comparable<Model> {
            public int y;
            public boolean isText;
            public JTextPane pane;
            public int pos;
            public StampHolder stampHolder;

            private Model(int y, boolean isText, JTextPane pane, int pos, StampHolder sh) {
                this.y = y;
                this.isText = isText;
                this.pane = pane;
                this.pos = pos;
                stampHolder = sh;
            }

            @Override
            public int compareTo(Model o) {
                int test = o.y;
                if (test == y) {
                    return 0;
                } else if (test > y) {
                    return -1;
                }
                return 1;
            }
        }
    }
}

