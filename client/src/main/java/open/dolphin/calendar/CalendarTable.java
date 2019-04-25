package open.dolphin.calendar;

import open.dolphin.infomodel.SimpleDate;
import open.dolphin.ui.PNSBorderFactory;
import open.dolphin.ui.PNSTitledBorder;
import open.dolphin.util.Gengo;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Calendar を表示する JTable.
 *
 * @author pns
 */
public class CalendarTable extends JTable {
    private static final long serialVersionUID = 1L;

    //private static final String[] MONTH_NAME = new String[] {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    private static final String[] MONTH_NAME = new String[]{"睦月", "如月", "弥生", "卯月", "皐月", "水無月", "文月", "葉月", "長月", "神無月", "霜月", "師走"};

    private static final Color CALENDAR_BACKGROUND = new Color(227, 250, 207);
    private static final Color SUNDAY_FOREGROUND = new Color(255, 0, 130);
    private static final Color SATURDAY_FOREGROUND = new Color(0, 0, 255);
    private static final Color WEEKDAY_FOREGROUND = new Color(20, 20, 70);
    private static final Font TITLE_FONT = new Font("Meiryo", Font.BOLD, 72);
    private static final Font CALENDAR_FONT = new Font(Font.DIALOG, Font.PLAIN, 13);
    private static final Font CALENDAR_FONT_SMALL = new Font(Font.DIALOG, Font.PLAIN, 10);

    private CalendarTableModel tableModel;

    // 日付がマウスで選択された時に呼ばれる listener
    private CalendarListener listener;
    // header 付きのカレンダー
    private JPanel calendarPanel;
    // バックグランドのタイトルを付けるかどうか
    private boolean showBackgroundTitle = true;

    /**
     * 今月のカレンダーを作る.
     */
    public CalendarTable() {
        initComponents(new GregorianCalendar());
    }

    /**
     * GregorianCalendar で指定された月のカレンダーを作る.
     *
     * @param gc
     */
    public CalendarTable(GregorianCalendar gc) {
        initComponents(gc);
    }

    public static void main(String[] arg) {
        open.dolphin.client.ClientContext.setClientContextStub(new open.dolphin.client.ClientContextStub());

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CalendarTable table = new CalendarTable();

        List<SimpleDate> dates = new ArrayList<>();
        GregorianCalendar gc = new GregorianCalendar();

        for (CalendarEvent e : CalendarEvent.values()) {
            SimpleDate d = new SimpleDate(gc);
            d.setEventCode(e.name());
            dates.add(d);
            gc.add(Calendar.DAY_OF_MONTH, 1);
        }

        CalendarTableModel model = (CalendarTableModel) table.getModel();
        model.setMarkDates(dates);

        f.add(table.getTitledPanel());
        f.pack();
        f.setVisible(true);
    }

    private void initComponents(GregorianCalendar gc) {
        // TableModel をセット
        tableModel = new CalendarTableModel(gc);
        setModel(tableModel);

        // SelectionModel をセット
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellSelectionEnabled(false);
        setRowSelectionAllowed(false);

        // TableCellRenderer をセット
        setDefaultRenderer(Object.class, new DateRenderer());
        getTableHeader().setDefaultRenderer(new CalendarHeaderRenderer());

        // Header の設定
        getTableHeader().setResizingAllowed(false);
        getTableHeader().setReorderingAllowed(false);

        // その他の設定
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        setBackground(CALENDAR_BACKGROUND);
        setIntercellSpacing(new Dimension(0, 0));
        setShowGrid(false);

        // MouseListener から CalendarListener にブリッジする
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listener != null) {
                    Point p = e.getPoint();
                    int row = rowAtPoint(p);
                    int col = columnAtPoint(p);

                    if (row != -1 && col != -1) {
                        SimpleDate date = (SimpleDate) tableModel.getValueAt(row, col);
                        fireDateSelected(date);
                    }
                }
            }
        });

        // RowHeight をサイズに合わせる
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                setRowHeight(getHeight() / getRowCount());
            }

            @Override
            public void componentShown(ComponentEvent e) {
                setRowHeight(getHeight() / getRowCount());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        // ヘッダのついた Panel を作る
        calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.setPreferredSize(new Dimension(200, 150));
        calendarPanel.add(getTableHeader(), BorderLayout.NORTH);
        calendarPanel.add(this, BorderLayout.CENTER);
    }

    /**
     * 曜日ヘッダ付きのカレンダーを返す.
     *
     * @return
     */
    public JPanel getPanel() {
        return calendarPanel;
    }

    /**
     * タイトル部分に年月ラベルを付けた，TitledBorder 付きカレンダーを返す.
     *
     * @return
     */
    public JPanel getTitledPanel() {
        // タイトルを変えるためにリスナを付ける
        tableModel.addTableModelListener(e -> setCalendarTitle());

        // タイトルボーダーを付ける
        Border border = PNSBorderFactory.createTitledBorder("");
        calendarPanel.setBorder(border);

        // タイトルボーダーにタイトルを記入
        setCalendarTitle();

        return calendarPanel;
    }

    /**
     * カレンダーのタイトル部分を描画する.
     *
     * @return
     */
    private void setCalendarTitle() {
        String title = String.format("%d年%d月", tableModel.getYear(), tableModel.getMonth() + 1);

        // 今月はラベルの色を変える
        GregorianCalendar gc = new GregorianCalendar();
        int y = gc.get(Calendar.YEAR);
        int m = gc.get(Calendar.MONTH);

        Color color = null;
        Font font = null;

        if ((y == tableModel.getYear()) && (m == tableModel.getMonth())) {
            title += " (今月)";
            color = Color.BLUE;
            font = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        }

        PNSTitledBorder border = (PNSTitledBorder) calendarPanel.getBorder();
        border.setTitle(null, title, 0, 0, font, color);
        calendarPanel.repaint();
    }

    /**
     * 日付選択リスナーを登録する.
     *
     * @param l
     */
    public void addCalendarListener(CalendarListener l) {
        listener = l;
    }

    /**
     * 日付選択リスナーを返す.
     *
     * @return
     */
    public CalendarListener getCalendarListener() {
        return listener;
    }

    /**
     * リスナに日付選択を通知する.
     */
    private void fireDateSelected(SimpleDate date) {
        if (listener != null) {
            listener.dateSelected(date);
        }
    }

    /**
     * バックグランドに月と年を出すかどうか.
     *
     * @param b
     */
    public void setShowBackgroundTitle(boolean b) {
        showBackgroundTitle = b;
    }

    /**
     * SimpleDate の Event 名を表示する.
     *
     * @param e
     * @return
     */
    @Override
    public String getToolTipText(MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        int col = columnAtPoint(e.getPoint());
        SimpleDate d = (SimpleDate) getValueAt(row, col);
        return CalendarEvent.getTitle(d.getEventCode());
    }

    /**
     * バックグランドに月と年を出す.
     *
     * @param graphics
     */
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (!showBackgroundTitle) {
            return;
        }

        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));

        String month = MONTH_NAME[tableModel.getMonth()];
        //String year = String.valueOf(tableModel.getYear());
        String year = getNengo();

        g.setColor(Color.BLACK);
        g.setFont(TITLE_FONT);
        FontMetrics fm = g.getFontMetrics();

        // 画面の大きさに合わせて，フォントの大きさを調節する.
        float h = getHeight();
        float fh = fm.getHeight() * 2 - 25; // cut and try
        float s = h / fh;
        g.setFont(TITLE_FONT.deriveFont(AffineTransform.getScaleInstance(s * 1.4, s)));
        fm = g.getFontMetrics();

        int x1 = (getWidth() - fm.stringWidth(month)) / 2;
        int x2 = (getWidth() - fm.stringWidth(year)) / 2;
        int y = (getHeight() - fm.getHeight() * 2) / 2 + 5; // 5 dot shift

        g.drawString(month, x1, y + fm.getAscent());
        g.drawString(year, x2, y + fm.getHeight() + fm.getAscent());

        g.dispose();
    }

    /**
     * バックグランドタイトル表示用に年号型式の年を作る.
     *
     * @return
     */
    private String getNengo() {
        SimpleDate date = new SimpleDate(tableModel.getYear(), tableModel.getMonth(), 1);
        String mmlDate = SimpleDate.simpleDateToMmldate(date);
        String nengoDate = Gengo.toGengo(mmlDate);
        String[] split = nengoDate.split("-");

        String nengoAlphabet = String.valueOf(split[0].charAt(0));
        int year = Integer.valueOf(split[0].substring(1));
        String nengo = Gengo.gengoAlphabetToHalfKanji(nengoAlphabet);

        return nengo + (year == 1 ? "元" : year);
    }

    /**
     * Custom table cell renderer for CalendarTable.
     */
    private class DateRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 5817292848730765481L;

        private Color eventColor;

        public DateRenderer() {
            super();
            init();
        }

        private void init() {
            setBorder(BorderFactory.createLineBorder(Color.red));
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        /**
         * Event の色を，円のバックグランドで描く.
         */
        @Override
        public void paintComponent(Graphics graphics) {
            //super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (eventColor != null) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
                int d = Math.min(w, h);
                int x = (w - d) / 2;
                int y = (h - d) / 2;
                g.setColor(eventColor);
                g.fillOval(x, y, d - 1, d - 1);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g.drawOval(x, y, d - 1, d - 1);
            }

            String text = getText();
            FontMetrics fm = g.getFontMetrics();
            int strWidth = fm.stringWidth(text);

            int x = (w - strWidth) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent(); // height でセンタリングして ascent 分下げる

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g.setColor(getForeground());
            g.drawString(text, x, y);

            g.dispose();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean isFocused, int row,
                                                       int col) {

            Component compo = super.getTableCellRendererComponent(table, value, isSelected, isFocused, row, col);

            if (compo != null && value != null) {

                SimpleDate targetDate = (SimpleDate) value;

                Color foregroundColor;

                // 曜日によって ForeColor を変える
                switch (col) {
                    // 当院は日・水は休み
                    case 0:
                    case 3:
                        foregroundColor = SUNDAY_FOREGROUND;
                        break;
                    case 6:
                        foregroundColor = SATURDAY_FOREGROUND;
                        break;
                    default:
                        foregroundColor = WEEKDAY_FOREGROUND;
                        break;
                }

                // Event "PVT", "TODAY", "BIRTHDAY"
                eventColor = CalendarEvent.getColor(targetDate.getEventCode());
                if ("TODAY".equals(targetDate.getEventCode())) {
                    foregroundColor = Color.WHITE;
                }

                // Holiday
                if (eventColor == null && targetDate.getEventCode() != null) {
                    foregroundColor = SUNDAY_FOREGROUND;
                }

                String day = String.valueOf(targetDate.getDay());
                ((JLabel) compo).setText(day);

                // このカレンダ月内の日かどうかでフォントを変える
                if (tableModel.isOutOfMonth(row, col)) {
                    setFont(CALENDAR_FONT_SMALL);

                } else {
                    setFont(CALENDAR_FONT);
                }

                setForeground(foregroundColor);
            }
            return compo;
        }
    }
}
