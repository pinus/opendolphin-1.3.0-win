package open.dolphin.client;

import open.dolphin.ui.CompletableSearchField;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

/**
 * カルテ検索と病名検索を CardLayout で切り替えて使うパネル.
 */
public class ChartSearchPanel extends JPanel {
    public enum Card {STAMP, KARTE}

    private ChartImpl chart;
    private CardLayout card;
    private CompletableSearchField stampSearchField;
    private CompletableSearchField karteSearchField;

    private FindAndView findAndView;
    private JPanel scrollerPanel;

    public ChartSearchPanel(ChartImpl ctx) {
        chart = ctx;
        card = new CardLayout();
        setLayout(card);
        initComponents();
        connect();
    }

    private void initComponents() {
        stampSearchField = new CompletableSearchField(15);
        stampSearchField.setPreferredSize(new Dimension(300, 26)); // width は JTextField の columns が優先される
        stampSearchField.setLabel("病名検索");
        stampSearchField.setPreferences(Preferences.userNodeForPackage(ChartToolBar.class).node(ChartImpl.class.getName()));

        karteSearchField = new CompletableSearchField(15);
        karteSearchField.setPreferredSize(new Dimension(300, 26)); // width は JTextField の columns が優先される
        karteSearchField.setLabel("カルテ検索");
        karteSearchField.setPreferences(Preferences.userNodeForPackage(KarteDocumentViewer.class).node(KarteDocumentViewer.class.getName()));

        add(stampSearchField, Card.STAMP.name());
        add(karteSearchField, Card.KARTE.name());
    }

    private void connect() {
        stampSearchField.addActionListener(e -> {
            String text = stampSearchField.getText();
            String pattern = ".*" + stampSearchField.getText() + ".*";

            if (!StringUtils.isEmpty(text)) {
                JPopupMenu popup = chart.getChartMediator().createDiagnosisPopup(pattern, ev -> {
                    JComponent c = chart.getDiagnosisDocument().getDiagnosisTable();
                    TransferHandler handler = c.getTransferHandler();
                    handler.importData(new TransferHandler.TransferSupport(c, ev.getTransferable()));
                    // transfer 後にキーワードフィールドをクリアする
                    stampSearchField.setText("");
                });

                if (popup.getComponentCount() != 0) {
                    popup.show(stampSearchField,0, stampSearchField.getHeight());
                }
            }
        });

        karteSearchField.addActionListener(e -> {
            String keyWord = karteSearchField.getText();
            if (!StringUtils.isEmpty(keyWord)) {
                String[] option = keyWord.split(":");
                boolean searchSoa = true;
                boolean searchP = true;
                if (option.length == 2) {
                    searchSoa = option[0].startsWith("s") || option[0].startsWith("S");
                    searchP = option[0].startsWith("p") || option[0].startsWith("P");
                    keyWord = option[1];
                }
                findAndView.showFirst(keyWord, searchSoa, searchP, scrollerPanel);
            }
        });

        // ctrl-return でもリターンキーの notify-field-accept が発生するようにする
        InputMap map = stampSearchField.getInputMap();
        Object value = map.get(KeyStroke.getKeyStroke("ENTER"));
        map.put(KeyStroke.getKeyStroke("ctrl ENTER"), value);
    }

    /**
     * カードを切り替える.
     * @param c ChartSearchPanel.Card.STAMP で病名, KARTE でカルテ検索
     */
    public void show(Card c) {
        card.show(this, c.name());
    }

    public CompletableSearchField getStampSearchField() {
        return stampSearchField;
    }

    public CompletableSearchField getKarteSearchField() {
        return karteSearchField;
    }

    /**
     * カルテ検索用のパラメータを設定する.
     * @param fav FindAndView
     * @param scroller JPanel
     */
    public void setParams(FindAndView fav, JPanel scroller) {
        findAndView = fav;
        scrollerPanel = scroller;
    }
}
