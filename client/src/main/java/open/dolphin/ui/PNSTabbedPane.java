package open.dolphin.ui;

import open.dolphin.client.ClientContext;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * JTabbedPane 的な何か.
 *
 * @author pns
 */
public class PNSTabbedPane extends JPanel implements ChangeListener {
    private static final long serialVersionUID = 1L;
    /**
     * １行の最低タブ数
     */
    private static final int MIN_TAB_PER_LINE = 3;
    /**
     * ChangeListener
     */
    private final List<ChangeListener> listeners = new ArrayList<>();
    /**
     * タブ切り替えボタン格納パネル
     */
    private ButtonPanel buttonPanel;
    /**
     * ボタンパネルのフォント
     */
    private Font buttonPanelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    /**
     * 切り替えボタンを入れておくリスト
     */
    private List<TabButton> buttonList;
    /**
     * 切り替えボタン格納パネルのレイアウト
     */
    private RightJustifiedFlowLayout buttonLayout;
    /**
     * コンテント表示パネル
     */
    private JPanel contentPanel;
    /**
     * コンテントを切り替えるためのカードレイアウト
     */
    private CardLayout card;
    /**
     * タブ切り替えボタンのボタングループ
     */
    private ButtonGroup buttonGroup;
    /**
     * ButtonPanel 部分に何かを加えたりするためのフックパネル
     */
    private JPanel accessoryPanel;
    /**
     * セレクションモデル
     */
    private DefaultSingleSelectionModel selectionModel;
    /**
     * タブの総数
     */
    private int tabCount = 0;
    /**
     * タブの場所　上か下か
     */
    private int tabPlacement = JTabbedPane.TOP;
    /**
     * 親の Window
     */
    private Window parent = null;
    /**
     * Application が foreground かどうか
     */
    private boolean appForeground = true;
    /**
     * Logger
     */
    private Logger logger = Logger.getLogger(PNSTabbedPane.class);

    public PNSTabbedPane() {
        initComponents();
    }

    private static JPanel createMainPanel() {
        JPanel mainComponentPanel = new JPanel();
        mainComponentPanel.setLayout(new BorderLayout(0, 0));

        HorizontalPanel commandPanel = new HorizontalPanel();
        commandPanel.setPanelHeight(36);
        commandPanel.add(new JButton("テスト１"));
        commandPanel.addSeparator();
        commandPanel.add(new JButton("テスト２"));
        commandPanel.addGlue();
        commandPanel.add(new JButton("右端"));

        StatusPanel statusPanel = new StatusPanel();
        statusPanel.add("Label1");
        statusPanel.addSeparator();
        statusPanel.add("Label2");
        statusPanel.addGlue();
        statusPanel.add("END");
        statusPanel.setMargin(8);

        JTable table = new JTable(50, 10);
        table.setGridColor(Color.gray);

        mainComponentPanel.add(commandPanel, BorderLayout.NORTH);
        mainComponentPanel.add(table, BorderLayout.CENTER);
        mainComponentPanel.add(statusPanel, BorderLayout.SOUTH);

        return mainComponentPanel;
    }

    private static PNSTabbedPane createTreeTabPane(String[] tabStr) {
        // 内側の tabbed pane
        PNSTabbedPane tab = new PNSTabbedPane();
        // ボタンパネルの余白設定
        tab.setButtonPanelPadding(new Dimension(4, 4));
        // status panel を inner tab に設定
        StatusPanel statusPanel = new StatusPanel();
        tab.add(statusPanel, BorderLayout.SOUTH);
        statusPanel.add(new JLabel("STATUS PANEL TEST"));
        statusPanel.setMargin(16);

        JTree[] panes = new JTree[tabStr.length];

        for (int i = 0; i < tabStr.length; i++) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(tabStr[i]);
            DefaultMutableTreeNode swing = new DefaultMutableTreeNode("Swing");
            DefaultMutableTreeNode java2d = new DefaultMutableTreeNode("Java2D");
            DefaultMutableTreeNode java3d = new DefaultMutableTreeNode("Java3D");
            DefaultMutableTreeNode javamail = new DefaultMutableTreeNode("JavaMail");

            DefaultMutableTreeNode swingSub1 = new DefaultMutableTreeNode("JLabel");
            DefaultMutableTreeNode swingSub2 = new DefaultMutableTreeNode("JButton");
            DefaultMutableTreeNode swingSub3 = new DefaultMutableTreeNode("JTextField");

            swing.add(swingSub1);
            swing.add(swingSub2);
            swing.add(swingSub3);

            root.add(swing);
            root.add(java2d);
            root.add(java3d);
            root.add(javamail);

            panes[i] = new JTree(root);
            panes[i].setPreferredSize(new Dimension(500, 700));
            tab.addTab(tabStr[i], panes[i]);
        }

        return tab;
    }

    /**
     * 組み込まれるときに addNotify が呼ばれるのを利用して parent を登録する.
     */
    @Override
    public void addNotify() {
        super.addNotify();

        if (parent == null) {
            parent = SwingUtilities.windowForComponent(this);

            // ButtonPanel がうまく repaint されないことがある
            parent.addWindowListener(new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
                    buttonPanel.repaint();
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                    buttonPanel.repaint();
                }
            });

            // AppForegroundListener
//            com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
//            app.addAppEventListener(new com.apple.eawt.AppForegroundListener() {
//                @Override
//                public void appRaisedToForeground(com.apple.eawt.AppEvent.AppForegroundEvent afe) {
//                    appForeground = true;
//                    buttonPanel.repaint();
//                }
//
//                @Override
//                public void appMovedToBackground(com.apple.eawt.AppEvent.AppForegroundEvent afe) {
//                    appForeground = false;
//                    buttonPanel.repaint();
//                }
//            });

        }
    }

    private void initComponents() {
        // selection model 作成
        selectionModel = new DefaultSingleSelectionModel();
        selectionModel.addChangeListener(this);

        // ボタン格納パネル作成
        buttonPanel = new ButtonPanel();
        buttonLayout = new RightJustifiedFlowLayout();
        buttonPanel.setLayout(buttonLayout);

        accessoryPanel = new JPanel(new BorderLayout());
        accessoryPanel.add(buttonPanel, BorderLayout.CENTER);

        // 内容表示パネル作成
        contentPanel = new JPanel();
        card = new CardLayout(0, 0);
        contentPanel.setLayout(card);

        // ボタングループの設定
        buttonGroup = new ButtonGroup();
        buttonList = new ArrayList<>();

        // 全体のレイアウト
        setLayout(new BorderLayout(0, 0));
        add(accessoryPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * ButtonPanel を入れる Container.
     * BorderLayout にしてある.
     *
     * @return JPanel that contains ButtonPanel
     */
    public JPanel getAccessoryPanel() {
        return accessoryPanel;
    }

    /**
     * タブにコンポネントを加える.
     *
     * @param name タブ名
     * @param c    Component
     */
    public void addTab(String name, Component c) {
        TabButton button = new TabButton(name, tabCount);

        buttonPanel.add(button);
        contentPanel.add(c, String.valueOf(tabCount));
        buttonGroup.add(button);
        buttonList.add(button);

        if (tabCount == 0) {
            selectionModel.setSelectedIndex(0);
        }
        tabCount++;
    }

    /**
     * index の card に component をセットする.
     *
     * @param index index
     * @param c     Component
     */
    public void setComponentAt(int index, Component c) {
        contentPanel.remove(index);
        contentPanel.add(c, String.valueOf(index), index);

        // もし selected だったら，その component を表示し直す
        if (index == selectionModel.getSelectedIndex()) {
            card.show(contentPanel, String.valueOf(index));
        }
    }

    /**
     * index の component を返す.
     *
     * @param index index
     * @return Component
     */
    public Component getComponentAt(int index) {
        return contentPanel.getComponent(index);
    }

    /**
     * index のボタンの title を返す.
     *
     * @param index index
     * @return ボタン名
     */
    public String getTitleAt(int index) {
        TabButton button = (TabButton) buttonPanel.getComponent(index);
        return button.getName();
    }

    /**
     * index のボタンの title を設定する.
     *
     * @param index index
     * @param title ボタン名を設定
     */
    public void setTitleAt(int index, String title) {
        TabButton button = (TabButton) buttonPanel.getComponent(index);
        button.setName(title);
    }

    /**
     * ボタンパネルを上につけるか，下につけるか.
     *
     * @param tabPlacement ボタン位置
     */
    public void setTabPlacement(int tabPlacement) {
        if (this.tabPlacement != tabPlacement) {
            this.tabPlacement = tabPlacement;
            removeAll();
            if (tabPlacement == JTabbedPane.BOTTOM) {
                add(contentPanel, BorderLayout.CENTER);
                add(buttonPanel, BorderLayout.SOUTH);
            } else {
                add(buttonPanel, BorderLayout.NORTH);
                add(contentPanel, BorderLayout.CENTER);
            }
            revalidate();
            repaint();
        }
    }

    /**
     * index のボタンに ToolTipText をつける.
     *
     * @param index index
     * @param text  ToolTipText
     */
    public void setToolTipTextAt(int index, String text) {
        buttonList.get(index).setToolTipText(text);
    }

    /**
     * 選択されている index を返す.
     *
     * @return selected index
     */
    public int getSelectedIndex() {
        return selectionModel.getSelectedIndex();
    }

    /**
     * index のタブを選択する.
     *
     * @param index index
     */
    public void setSelectedIndex(int index) {
        if (buttonList.get(index).isEnabled()) {
            selectionModel.setSelectedIndex(index);
        }
    }

    /**
     * タブの総数を返す.
     *
     * @return total tab count
     */
    public int getTabCount() {
        return tabCount;
    }

    /**
     * 表示されている Component を返す.
     *
     * @return selected Component
     */
    public Component getSelectedComponent() {
        return contentPanel.getComponent(selectionModel.getSelectedIndex());
    }

    /**
     * ChangeListener を登録する.
     *
     * @param listener ChangeListener
     */
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * index のタブが使用可能であるかどうかを設定.
     *
     * @param index     index
     * @param isEnabled enabled or not
     */
    public void setEnabledAt(int index, boolean isEnabled) {
        buttonList.get(index).setEnabled(isEnabled);
    }

    /**
     * text をタイトルに持つタブ番号を返す. ない場合は -1 を返す.
     *
     * @param title タイトル
     * @return index
     */
    public int indexOfTab(String title) {
        int ret = -1;
        for (int i = 0; i < tabCount; i++) {
            if (buttonList.get(i).getText().equals(title)) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    /**
     * index のタブを削除する.
     *
     * @param index index
     */
    public void removeTabAt(int index) {
        buttonGroup.remove(buttonList.get(index));
        buttonList.remove(index);
        buttonPanel.remove(index);
        contentPanel.remove(index);

        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    /**
     * ボタンレイアウトの水平方向の隙間.
     *
     * @param hgap holizontal gap
     */
    public void setButtonHgap(int hgap) {
        buttonLayout.setHgap(hgap);
    }

    /**
     * ボタンレイアウトの垂直方向の隙間.
     *
     * @param vgap vertical gpg
     */
    public void setButtonVgap(int vgap) {
        buttonLayout.setVgap(vgap);
    }

    /**
     * selection model が変更されると呼ばれる.
     *
     * @param e ChangeEvent
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        listeners.forEach(listener -> listener.stateChanged(e));

        int index = selectionModel.getSelectedIndex();
        card.show(contentPanel, String.valueOf(index));
        buttonList.get(index).setSelected(true);
    }

    /**
     * ボタンパネルの回りの余白を返す.
     *
     * @return dmension of padding
     */
    public Dimension getButtonPanelPadding() {
        return buttonPanel.getPadding();
    }

    /**
     * ボタンパネルの回りの余白を設定する.
     *
     * @param d dimension of padding
     */
    public void setButtonPanelPadding(Dimension d) {
        buttonPanel.setPadding(d);
    }

    /**
     * ボタンパネルのフォントをセットする.
     *
     * @param font Font
     */
    public void setButtonPanelFont(Font font) {
        buttonPanelFont = font;
    }

    /**
     * ButtonBanel 描画装飾フック用.
     *
     * @param g Graphics
     */
    public void paintButtonPanel(Graphics g) {
    }

    /**
     * n 番目のボタンの右上のコーナーの座標を返す.
     *
     * @param n button number
     * @return coordinate at upper-right
     */
    public Point getButtonTopRightCornerLocation(int n) {
        TabButton button = buttonList.get(n);
        Point p = button.getLocation();
        p.x += button.getWidth();
        return p;
    }

    /**
     * ButtonPanel のバックグランド色を設定する.
     *
     * @param background background color
     */
    public void setButtonPanelBackground(Color background) {
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(background);
    }

    /**
     * ボタンパネル.
     */
    private class ButtonPanel extends HorizontalPanel {
        private static final long serialVersionUID = 1L;
        private Dimension padding = new Dimension(0, 0);

        public ButtonPanel() {
        }

        public Dimension getPadding() {
            return padding;
        }

        public void setPadding(Dimension d) {
            padding = d;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            paintButtonPanel(g);
        }

        @Override
        public boolean isOptimizedDrawingEnabled() { return false; }
    }

    /**
     * タブボタンクラス.
     */
    private class TabButton extends JToggleButton implements IPNSButton, ActionListener {
        private static final long serialVersionUID = 1L;

        public boolean isTop;
        public boolean isBottom;
        public boolean isRightEnd;
        public boolean isLeftEnd;
        // レイアウトマネージャーでボタンの大きさを調節する時使う
        public Dimension margin = new Dimension(0, 0); // no quaqua
        private String name;
        private int index;
        //public Dimension margin = new Dimension(0,-2); // quaqua 8.0
        //public Dimension margin = new Dimension(0,4); // quaqua 7.2

        public TabButton(String name, int index) {
            this.name = name;
            this.index = index;
            initComponent();
        }

        private void initComponent() {
            setName(name);
            addActionListener(this);
            setFocusable(false);
            setBorderPainted(false);
            setFont(buttonPanelFont);

            float w = ((float) name.length() * buttonPanelFont.getSize()) * 1.3f;
            int h = buttonPanelFont.getSize() * 2;
            setPreferredSize(new Dimension((int) w, h));
        }

        @Override
        public final void setName(String name) {
            super.setName(name);
            super.setText(name);
            this.name = name;
        }

        public final void setIndex(int index) {
            this.index = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selectionModel.setSelectedIndex(index);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.setSize(d.width + margin.width, d.height + margin.height);
            return d;
        }

        @Override
        public void paintComponent(Graphics graphics) {
            //super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            int w = this.getWidth();
            int h = this.getHeight();

            if (ClientContext.isMac()) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }

            if (parent.isActive() && appForeground) {
                if (this.isSelected()) {
                    renderButton(g, w, h, ACTIVE_FRAME_SELECTED, ACTIVE_FILL_SELECTED);

                } else {
                    renderButton(g, w, h, ACTIVE_FRAME, ACTIVE_FILL);
                }

            } else {
                if (this.isSelected()) {
                    renderButton(g, w, h, INACTIVE_FRAME, INACTIVE_FILL_SELECTED);

                } else {
                    renderButton(g, w, h, INACTIVE_FRAME, INACTIVE_FILL);

                }
            }

            // 文字記入
            FontMetrics fm = g.getFontMetrics();

            int strWidth = fm.stringWidth(name);

            if (parent.isActive() && appForeground) {
                if (this.isSelected()) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.BLACK);
                }

            } else {
                if (this.isSelected()) {
                    g.setColor(INACTIVE_TEXT_SELECTED);
                } else {
                    g.setColor(INACTIVE_TEXT);
                }
            }

            g.drawString(name, (w - strWidth) / 2, (h + fm.getHeight()) / 2 - fm.getDescent());

            g.dispose();
        }

        private void renderButton(Graphics2D g, int w, int h, Color frameColor, Color fillColor) {
            if (this.isLeftEnd && isTop && isBottom) {
                // 左端のボタンの角を取る
                // fill
                int r = RADIUS;
                g.setColor(fillColor);
                g.fillRoundRect(0, 0, w, h, r * 2, r * 2); // 全体を角丸で塗って
                g.fillRect(w - r, 0, r, h); // 右端を四角く塗り直す
                // frame
                g.setColor(frameColor);
                g.drawLine(r, 0, w - 1, 0); // 上
                g.drawLine(0, r, 0, h - r - 1); // 左
                g.drawArc(0, 0, r * 2, r * 2, 90, 90); // 左上 12時から反時計 90度
                g.drawArc(0, h - r * 2 - 1, r * 2, r * 2, 180, 90);
                g.drawLine(r, h - 1, w - 1, h - 1); // 下

            } else if (this.isRightEnd && isTop && isBottom) {
                // 右端のボタンの角を取る
                // fill
                int r = RADIUS;
                g.setColor(fillColor);
                g.fillRoundRect(0, 0, w - 1, h, r * 2, r * 2); // 右端のボタンは 1ドット狭い
                g.fillRect(0, 0, r, h);
                // frame
                g.setColor(frameColor);
                g.drawLine(0, 0, w - r - 1, 0); // 上
                g.drawLine(0, 0, 0, h - 1); // 左
                g.drawArc(w - 1 - r * 2, 0, r * 2, r * 2, 0, 90); // 右上 3時から反時計 90度
                g.drawArc(w - 1 - r * 2, h - r * 2 - 1, r * 2, r * 2, 0, -90); // 右下 3時から時計回り90度
                g.drawLine(0, h - 1, w - 1 - r, h - 1); // 下
                g.drawLine(w - 1, r, w - 1, h - 1 - r); // 右

            } else {
                // fill
                g.setColor(fillColor);
                g.fillRect(0, 0, w, h);
                // frame
                g.setColor(frameColor);
                g.drawLine(0, 0, w - 1, 0); // 上
                g.drawLine(0, 0, 0, h - 1); // 左
                if (this.isBottom || buttonLayout.getVgap() != 0) {
                    g.drawLine(0, h - 1, w - 1, h - 1);
                } // 下
                if (this.isRightEnd || buttonLayout.getHgap() != 0) {
                    g.drawLine(w - 1, 0, w - 1, h - 1);
                } // 右
            }
        }
    }

    /**
     * 両端そろえる FlowLayout.
     */
    private class RightJustifiedFlowLayout extends FlowLayout {
        private static final long serialVersionUID = 1L;

        public RightJustifiedFlowLayout() {
            super(FlowLayout.CENTER, 0, 0);
        }

        @Override
        public Dimension preferredLayoutSize(Container buttonPanel) {
            synchronized (buttonPanel.getTreeLock()) {
                Dimension padding = ((ButtonPanel) buttonPanel).getPadding();

                int width = PNSTabbedPane.this.getWidth() - padding.width;
                if (width <= 0) {
                    return new Dimension(1, 1);
                }
                //logger.info("width=" + width + " container=" + buttonPanel);

                int hgap = this.getHgap();
                int vgap = this.getVgap();
                int buttonCount = buttonPanel.getComponentCount();
                int tempWidth = 0;
                int tempHeight = 0;
                int totalHeight = 0;

                int lineCount = 1;

                int maxButtonWidth = 0;
                int tempButtonCount = 0;
                // wrap した場合の各行のボタン数
                List<Integer> buttonCountAtLine = new ArrayList<>(10);

                // 行数を計算
                for (int i = 0; i < buttonCount; i++) {
                    TabButton button = (TabButton) buttonPanel.getComponent(i);
                    button.margin.width = 0;
                    Dimension b = button.getPreferredSize();

                    tempWidth += hgap;
                    // 次のボタンを加えたらはみ出す場合の処理
                    if (tempWidth + b.width > width) {
                        totalHeight += tempHeight;
                        tempHeight = 0;
                        tempWidth = 0;
                        buttonCountAtLine.add(tempButtonCount);
                        lineCount++;
                        tempButtonCount = 0;
                    }
                    tempHeight = Math.max(tempHeight, b.height);
                    tempWidth += b.width;

                    maxButtonWidth = Math.max(maxButtonWidth, b.width);
                    tempButtonCount++;
                }
                totalHeight += tempHeight + (lineCount + 1) * vgap;
                buttonCountAtLine.add(tempButtonCount);

                // debug code
                if (((JFrame)parent).getTitle().startsWith("インスペクタ")) {
                    logger.info(String.format("line:%d,width:%d,%s", lineCount, width, ((JFrame)parent).getTitle()));
                }

                // １行だったら
                if (lineCount == 1) {
                    for (int i = 0; i < buttonCount; i++) {
                        TabButton button = (TabButton) buttonPanel.getComponent(i);
                        //ボタンの長さをできるだけそろえる
                        //button.margin.width = (maxButtonWidth - button.getPreferredSize().width);
                        button.isTop = true;
                        button.isBottom = true;
                        button.isRightEnd = (i == buttonCount - 1);
                        button.isLeftEnd = (i == 0);
                    }
                } else {
                    // ２行以上だったら right justification する

                    if (tempButtonCount < MIN_TAB_PER_LINE) {
                        //　再配分
                        float n = (float) buttonCount / lineCount;
                        float residue = 0;
                        int assigned = 0;

                        for (int i = 0; i < lineCount - 1; i++) {
                            residue += (n - (int) n);
                            if (residue >= 1.0f) {
                                residue -= 1.0f;
                                buttonCountAtLine.set(i, (int) n + 1);
                                assigned += ((int) n + 1);
                            } else {
                                buttonCountAtLine.set(i, (int) n);
                                assigned += (int) n;
                            }
                        }
                        buttonCountAtLine.set(lineCount - 1, buttonCount - assigned);
                    }

                    int offset = 0;
                    for (int line = 0; line < lineCount; line++) {
                        int bc = buttonCountAtLine.get(line);
                        if (bc == 0) {
                            continue;
                        }
                        // 隙間を測る
                        int gap = hgap; // hgap の数はボタンよりも１つ多い
                        for (int i = 0; i < bc; i++) {
                            TabButton b = (TabButton) buttonPanel.getComponent(i + offset);
                            gap += (b.getPreferredSize().width + hgap);
                            b.isBottom = (line == lineCount - 1 || vgap != 0);
                            b.isRightEnd = (i == bc - 1 || hgap != 0);
                        }
                        gap = width - gap;
                        // gap に応じて margin 調節
                        int delta = gap / bc;
                        for (int i = 0; i < bc; i++) {
                            TabButton b = (TabButton) buttonPanel.getComponent(i + offset);
                            b.margin.width = delta;
                            gap -= delta;
                            if (i == bc - 1) {
                                b.margin.width += gap;
                            }
                        }

                        offset += buttonCountAtLine.get(line);
                    }
                }
                return new Dimension(width, totalHeight + padding.height);
            }
        }
    }
    //================== TEST =================
    public static void main(String[] argv) {
        open.dolphin.client.ClientContext.setClientContextStub(new open.dolphin.client.ClientContextStub());
        //testPattern1();
        //testPattern2();
        testPattern3();
    }

    /**
     * MainWindow Style
     * +---------------+
     * | Tab Panel     |
     * |---------------|
     * | Command Panel |
     * |---------------|
     * |               |
     * | Table         |
     * |               |
     * |---------------|
     * | Status Panel  |
     * +---------------+
     */
    private static void testPattern1() {
        MainFrame f = new MainFrame("", false, false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(600, 800);

        JPanel mainComponentPanel_1 = createMainPanel();
        JPanel mainComponentPanel_2 = createMainPanel();
        JPanel mainComponentPanel_3 = createMainPanel();

        final PNSTabbedPane tabPane = new PNSTabbedPane();
        tabPane.setButtonVgap(4);
        tabPane.addTab("受付リスト", mainComponentPanel_1);
        tabPane.addTab("患者検索", mainComponentPanel_2);
        tabPane.addTab("ラボレシーバ", mainComponentPanel_3);

        MainFrame.MainPanel mainPanel = f.getMainPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(tabPane, BorderLayout.CENTER);

        f.pack();
        f.setVisible(true);

        JPanel p = tabPane.getAccessoryPanel();
        JButton button1 = new JButton("西ボタン");
        JButton button2 = new JButton("東ボタン");
        p.add(button1, BorderLayout.WEST);
        p.add(button2, BorderLayout.EAST);
        f.pack();
    }
    /**
     * ChartImpl Style
     * +---------------+
     * | Command Panel |
     * |---------------|
     * |   | TabPanel  |
     * |p1 |-----------|
     * |---| TextPane  |
     * |   |           |
     * |p2 |           |
     * |---------------|
     * | Status Panel  |
     * +---------------+
     */
    private static void testPattern2() {
        MainFrame f = new MainFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(800, 600);

        MainFrame.CommandPanel commandPanel = f.getCommandPanel();
        commandPanel.setPanelHeight(56);
        commandPanel.add(new JButton("FIRST"));
        commandPanel.addSeparator();
        commandPanel.add(new JButton("NEXT"));
        commandPanel.addGlue();
        commandPanel.add(new JButton("END"));

        StatusPanel statusPanel = f.getStatusPanel();
        statusPanel.add("Label1");
        statusPanel.addSeparator();
        statusPanel.add("Label2");
        statusPanel.addGlue();
        statusPanel.add("END");
        statusPanel.setMargin(8);

        MainFrame.MainPanel mainPanel = f.getMainPanel();
        mainPanel.setLayout(new BorderLayout(1, 1));

        JPanel p = new JPanel(new BorderLayout());
        JPanel p1 = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics grahics) {
                super.paintComponent(grahics);
                Graphics g = grahics.create();
                g.setColor(Color.GRAY);
                g.drawLine(0, 0, getWidth(), 0);
                g.dispose();
            }
        };
        p1.setPreferredSize(new Dimension(200, 100));
        p1.setBorder(BorderFactory.createTitledBorder("PANEL1"));
        JPanel p2 = new JPanel();
        p2.setPreferredSize(new Dimension(200, 100));
        p2.setBorder(BorderFactory.createTitledBorder("PANEL2"));
        p.add(p1, BorderLayout.NORTH);
        p.add(p2, BorderLayout.CENTER);

        PNSTabbedPane tab = new PNSTabbedPane();
        tab.setButtonVgap(4);
        tab.addTab("カルテ", new JTextPane());
        JTable table = new JTable(50, 10);
        table.setGridColor(Color.gray);
        tab.addTab("病名", table);

        mainPanel.add(p, BorderLayout.WEST);
        mainPanel.add(tab, BorderLayout.CENTER);

        f.setVisible(true);
    }

    /**
     * StampBox Style
     * +---------------+
     * | Command Panel |
     * |---------------|
     * |               |
     * | Tab Panel in  |
     * |               |
     * |---------------|
     * | Status Panel  |
     * |---------------|
     * | TabPanel out  |
     * +---------------+
     */
    private static void testPattern3() {
        MainFrame f = new MainFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // MainFrame の StatusPanel は使わない
        f.removeStatusPanel();
        // コマンドパネル
        MainFrame.CommandPanel commandPanel = f.getCommandPanel();
        commandPanel.add(new JButton("FIRST"));
        commandPanel.addSeparator();
        commandPanel.add(new JButton("NEXT"));
        commandPanel.addGlue();
        commandPanel.add(new JButton("END"));

        // 内側のタブ
        String[] tabStr = {"細菌検査", "注 射", "処 方", "初診・再診", "指導・在宅",
                "処 置", "手 術", "放射線", "検体検査", "生体検査", "傷病名", "テキスト", "パ ス", "ORCA", "汎 用", "その他"};
        PNSTabbedPane tabIn1 = createTreeTabPane(tabStr);
        final String[] tabStr2 = {"細菌検査", "処 方", "初診・再診", "指導・在宅",
                "処 置", "手 術", "放射線", "検体検査", "生体検査", "傷病名", "テキスト", "パ ス", "ORCA", "汎 用", "その他"};
        final PNSTabbedPane tabIn2 = createTreeTabPane(tabStr2);

        // 遅延生成テスト
        tabIn2.addChangeListener(e -> {
            System.out.println("stateChanged");
            int index = tabIn2.getSelectedIndex();
            tabIn2.setComponentAt(index, new JLabel(index + ":" + tabStr2[index]));
        });

        // 外側の tabbed pane
        PNSTabbedPane tabOut = new PNSTabbedPane();
        // 上下に隙間を入れる
        tabOut.setButtonVgap(4);
        // タブ位置は下
        tabOut.setTabPlacement(JTabbedPane.BOTTOM);
        tabOut.addTab("個人用", tabIn1);
        tabOut.addTab("ネットワーク", tabIn2);


        // main panel に tab を格納
        MainFrame.MainPanel mainPanel = f.getMainPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(tabOut, BorderLayout.CENTER);

        f.pack();
        f.setVisible(true);
    }
}
