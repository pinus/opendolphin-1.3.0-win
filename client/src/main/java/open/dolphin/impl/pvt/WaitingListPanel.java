package open.dolphin.impl.pvt;

import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.client.MainComponentPanel;
import open.dolphin.ui.PNSScrollPane;
import open.dolphin.ui.StatusPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * WaitingListView 互換 JPanel
 *
 * @author pns
 */
public class WaitingListPanel extends MainComponentPanel {
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.getLogger(WaitingListPanel.class);

    // アイコン
    private static final ImageIcon KUTU_ICON = GUIConst.ICON_ARROW_CIRCULAR_ALT1_16;
    // Font
    private static final Font NORMAL_FONT_12 = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final Font SMALL_FONT_12 = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
    private static final Font NORMAL_FONT_18 = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
    private static final Font SMALL_FONT_18 = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font NORMAL_FONT_24 = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
    private static final Font SMALL_FONT_24 = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
    // テーブルの row height, column length
    private static final int ROW_HEIGHT_12 = 18;
    private static final int ROW_HEIGHT_18 = 27;
    private static final int ROW_HEIGHT_24 = 36;
    private static int[] COLUMN_WIDTH_12 = {34, 68, 72, 140, 40, 50, 100, 75, 50, 40, 30};
    private static int[] COLUMN_WIDTH_18 = {51, 102, 108, 210, 60, 75, 150, 112, 75, 60, 45};
    private static int[] COLUMN_WIDTH_24 = {68, 136, 144, 280, 80, 100, 200, 150, 100, 80, 60};
    // command panel
    private JButton kutuBtn;
    private JLabel legendLbl;
    // main panel
    private JTable table;
    // status panel
    private JLabel checkedTimeLbl;
    private JLabel countLbl;
    private JLabel dateLbl;
    // table parameters
    private int rowHeight;
    private Font normalFont = NORMAL_FONT_12;
    private Font smallFont = SMALL_FONT_12;
    private int[] columnWidth;

    public WaitingListPanel() {
        initComponents();
    }

    private void initComponents() {

        CommandPanel comPanel = getCommandPanel();
        remove(comPanel);

        kutuBtn = new JButton(KUTU_ICON);
        if (ClientContext.isWin()) {
            kutuBtn.setBorder(null);
            kutuBtn.setContentAreaFilled(false);
        } else {
            kutuBtn.setBorderPainted(false);
        }
        kutuBtn.setFocusable(false);

        // popup で状態アイコンの legend を出す
        legendLbl = new JLabel();
        legendLbl.setIcon(GUIConst.ICON_QUESTION_16);
        legendLbl.setText("");
        legendLbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu legend = new LegendPopup();
                legend.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        MainPanel mainPanel = getMainPanel();
        table = new RowTipsTable();
        table.putClientProperty("Quaqua.Table.style", "striped");

        PNSScrollPane scroller = new PNSScrollPane(table);
        scroller.isPermanentScrollBar = true;
        mainPanel.add(scroller);

        StatusPanel statusPanel = getStatusPanel();
        checkedTimeLbl = new JLabel("00:00");
        countLbl = new JLabel("来院数10人，待ち10人，待ち時間 00:00");
        dateLbl = new JLabel("2011-11-11(土)");
        statusPanel.add(kutuBtn);
        if (ClientContext.isWin()) {
            statusPanel.addSpace(5);
        }
        statusPanel.add(checkedTimeLbl);
        statusPanel.addSeparator();
        statusPanel.addGlue();
        statusPanel.add(dateLbl);
        statusPanel.addSeparator();
        statusPanel.add(countLbl);
        statusPanel.setMargin(4);
    }

    public JLabel getCheckedTimeLbl() {
        return checkedTimeLbl;
    }

    public JLabel getCountLbl() {
        return countLbl;
    }

    public JLabel getDateLbl() { return dateLbl; }

    public JButton getKutuBtn() {
        return kutuBtn;
    }

    public JTable getTable() {
        return table;
    }

    public Font getNormalFont() {
        return normalFont;
    }

    public Font getSmallFont() {
        return smallFont;
    }

    public void setFontSize(int size) {
        switch (size) {
            case 18:
                columnWidth = COLUMN_WIDTH_18;
                rowHeight = ROW_HEIGHT_18;
                normalFont = NORMAL_FONT_18;
                smallFont = SMALL_FONT_18;
                break;
            case 24:
                columnWidth = COLUMN_WIDTH_24;
                rowHeight = ROW_HEIGHT_24;
                normalFont = NORMAL_FONT_24;
                smallFont = SMALL_FONT_24;
                break;

            default:
                columnWidth = COLUMN_WIDTH_12;
                rowHeight = ROW_HEIGHT_12;
                normalFont = NORMAL_FONT_12;
                smallFont = SMALL_FONT_12;
        }
        setColumnWidth(columnWidth);
        table.setRowHeight(rowHeight);
    }

    private void setColumnWidth(int[] width) {
        columnWidth = width;
        // コラム幅の設定
        for (int i = 0; i < width.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width[i]);
            if (i != 3 && i != 7) { //固定幅
                column.setMaxWidth(width[i]);
                column.setMinWidth(width[i]);
            }
        }
    }
}
