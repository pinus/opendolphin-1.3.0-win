package open.dolphin.inspector;

import open.dolphin.helper.PNSPair;
import open.dolphin.ui.ComboBoxFactory;
import open.dolphin.ui.PNSScrollPane;
import open.dolphin.ui.StatusPanel;

import javax.swing.*;
import java.awt.*;

/**
 * DocumentHistoryPanel.
 *
 * @author pns
 */
public class DocumentHistoryPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JLabel cntLbl;
    private JComboBox<PNSPair<String, Integer>> extractCombo;
    private JTable table;

    public DocumentHistoryPanel() {
        initComponents();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout(0, 0));

        table = new JTable();
        table.putClientProperty("Quaqua.Table.style", "striped"); // MyTableUI で使ってる
        PNSScrollPane scroller = new PNSScrollPane(table);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        scroller.putClientProperty("JComponent.sizeVariant", "small");

        cntLbl = new JLabel("0件");
        cntLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        // 文書抽出期間の項目は DocumentHistory で管理
        extractCombo = ComboBoxFactory.createDocumentExtractionPeriodCombo();
        extractCombo.setPreferredSize(new Dimension(76, 24));
        extractCombo.setMaximumSize(new Dimension(76, 24));
        extractCombo.setMinimumSize(new Dimension(76, 24));

        StatusPanel statusPanel = new StatusPanel();
        statusPanel.setPanelHeight(26);
        statusPanel.setOpaque(true);
        statusPanel.setBackground(IInspector.BACKGROUND);

        statusPanel.add(extractCombo);
        statusPanel.addGlue();
        statusPanel.add(cntLbl);
        statusPanel.setMargin(4);

        this.add(scroller, BorderLayout.CENTER);
        this.add(statusPanel, BorderLayout.SOUTH);
    }

    public JLabel getCntLbl() {
        return cntLbl;
    }

    public JComboBox<PNSPair<String, Integer>> getExtractCombo() {
        return extractCombo;
    }

    public JTable getTable() {
        return table;
    }
}
