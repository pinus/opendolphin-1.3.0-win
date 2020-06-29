package open.dolphin.impl.psearch;

import open.dolphin.calendar.CalendarPanel;
import open.dolphin.client.MainComponentPanel;
import open.dolphin.helper.PNSPair;
import open.dolphin.helper.TextComponentUndoManager;
import open.dolphin.ui.CompletableSearchField;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.PNSScrollPane;
import open.dolphin.ui.StatusPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

/**
 * PatientSearchView 互換 JPanel.
 * Keyword Field を各 MainComponent で共有することにした.
 *
 * @author pns
 */
public class PatientSearchPanel extends MainComponentPanel {
    private static final long serialVersionUID = 1L;

    // command panel
    private CompletableSearchField keywordFld;
    private JCheckBoxMenuItem narrowingSearchCb;
    // main panel
    private JTable table;
    // status panel
    private JLabel cntLbl;
    private JLabel dateLbl;
    private JProgressBar progressBar;
    private JMenuItem hibernateIndexItem;

    public PatientSearchPanel() {
        initComponents();
    }

    private void initComponents() {

        CommandPanel comPanel = getCommandPanel();
        remove(comPanel);

        final JPopupMenu popup = new JPopupMenu();
        final List<PNSPair<String, String>> menuPair = Arrays.asList(
                new PNSPair<>("名前検索", "N"),
                new PNSPair<>("カナ検索", "K"),
                new PNSPair<>("患者番号検索", "T"),
                new PNSPair<>("誕生日検索", "B"),
                new PNSPair<>("メモ検索", "M"),
                new PNSPair<>("全文検索", "F")
        );

        menuPair.forEach(pair -> {
            JMenuItem item = new JMenuItem(pair.getName());
            item.addActionListener(e -> {
                keywordFld.setText(pair.getValue() + " ");
                Focuser.requestFocus(keywordFld);
            });
            popup.add(item);
        });

        JMenu pvtMenu = new JMenu("受診日検索");
        CalendarPanel cp = new CalendarPanel();
        cp.setPreferredSize(new Dimension(200, 150));
        cp.getTable().addCalendarListener(date -> {
            keywordFld.setText(String.format("%d-%02d-%02d", date.getYear(), date.getMonth() + 1, date.getDay()));
            popup.setVisible(false);
            keywordFld.postActionEvent();
        });
        pvtMenu.add(cp);
        popup.add(pvtMenu);

        popup.addSeparator();

        narrowingSearchCb = new JCheckBoxMenuItem("絞り込みモード");
        narrowingSearchCb.setSelected(true);

        popup.add(narrowingSearchCb);
        popup.addSeparator();

        hibernateIndexItem = new JMenuItem("インデックス作成");
        popup.add(hibernateIndexItem);

        keywordFld = new CompletableSearchField(20);
        keywordFld.getDocument().addUndoableEditListener(TextComponentUndoManager.createManager(keywordFld));
        keywordFld.setLabel("患者検索");
        keywordFld.putClientProperty("Quaqua.TextField.style", "search");

        keywordFld.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        MainPanel mainPanel = getMainPanel();

        table = new AddressTipsTable();
        table.putClientProperty("Quaqua.Table.style", "striped");
        PNSScrollPane scroller = new PNSScrollPane(table);
        mainPanel.add(scroller);

        StatusPanel statusPanel = getStatusPanel();

        progressBar = new JProgressBar();
        Dimension pbSize = new Dimension(100, 14);
        progressBar.setMaximumSize(pbSize);
        progressBar.setPreferredSize(pbSize);
        cntLbl = new JLabel("0件");
        dateLbl = new JLabel("2011-11-11(土)");
        statusPanel.addGlue();
        statusPanel.add(progressBar);
        statusPanel.addSeparator();
        statusPanel.add(cntLbl);
        statusPanel.addSeparator();
        statusPanel.add(dateLbl);
        statusPanel.setMargin(4);
    }

    public JLabel getDateLbl() {
        return dateLbl;
    }

    public JLabel getCntLbl() {
        return cntLbl;
    }

    public JTable getTable() {
        return table;
    }

    public CompletableSearchField getKeywordFld() {
        return keywordFld;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JMenuItem getHibernateIndexItem() {
        return hibernateIndexItem;
    }

    public JCheckBoxMenuItem getNarrowingSearchCb() {
        return narrowingSearchCb;
    }
}
