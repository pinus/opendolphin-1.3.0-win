package open.dolphin.client;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.stampbox.StampBoxPlugin;
import open.dolphin.stampbox.StampTree;
import open.dolphin.stampbox.StampTreeMenuBuilder;
import open.dolphin.ui.CompletableSearchField;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.PNSToggleButton;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import java.util.prefs.Preferences;

/**
 * EditorFrame 特有の JToolBar.
 *
 * @author pns
 */
public class ChartToolBar extends JToolBar {
    private static final long serialVersionUID = 1L;

    private final MainWindow window;
    private final EditorFrame editorFrame;
    private final ChartMediator mediator;
    private final Preferences prefs;

    private FontButton boldButton;
    private FontButton italicButton;
    private FontButton underlineButton;
    private ColorButton colorButton;

    private JustifyButton leftJustify;
    private JustifyButton centerJustify;
    private JustifyButton rightJustify;

    private JTextPane focusedPane;

    public ChartToolBar(final EditorFrame chart) {
        super();
        window = chart.getContext();
        mediator = chart.getChartMediator();
        prefs = Preferences.userNodeForPackage(ChartToolBar.class).node(ChartToolBar.class.getName());
        editorFrame = chart;

        initComponents();
        connect();
    }

    /**
     * コンポネントの組み立て.
     */
    private void initComponents() {
        setFloatable(false);
        setOpaque(false);
        setBorderPainted(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        add(Box.createHorizontalStrut(10));
        add(createFontPanel());
        add(Box.createHorizontalStrut(32));
        add(createJustifyPanel());
        add(Box.createHorizontalStrut(32));
        add(createDiagnosisSearchPanel());
    }

    /**
     * リスナーの接続を行う.
     */
    private void connect() {
        FocusListener focusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focusedPane = (JTextPane) e.getSource();
            }
        };
        editorFrame.getEditor().getSOAPane().getTextPane().addFocusListener(focusListener);
        editorFrame.getEditor().getPPane().getTextPane().addFocusListener(focusListener);

        boldButton.addActionListener(e -> {
            mediator.fontBold();
            Focuser.requestFocus(focusedPane);
        });
        italicButton.addActionListener(e -> {
            mediator.fontItalic();
            Focuser.requestFocus(focusedPane);
        });
        underlineButton.addActionListener(e -> {
            mediator.fontUnderline();
            Focuser.requestFocus(focusedPane);
        });

        colorButton.addActionListener(e -> {
            ColorButton b = (ColorButton) e.getSource();
            JPopupMenu menu = new JPopupMenu();
            ColorChooserComp chooser = new ColorChooserComp();
            menu.add(chooser);
            chooser.addPropertyChangeListener(ColorChooserComp.SELECTED_COLOR, pe -> {
                Color color = (Color) pe.getNewValue();
                mediator.colorAction(color);
                repaint();
                menu.setVisible(false);
                Focuser.requestFocus(focusedPane);
            });
            menu.show(b, 0, b.getHeight());
            b.setSelected(false);
        });

        leftJustify.addActionListener(e -> {
            mediator.leftJustify();
            Focuser.requestFocus(focusedPane);
        });
        centerJustify.addActionListener(e -> {
            mediator.centerJustify();
            Focuser.requestFocus(focusedPane);
        });
        rightJustify.addActionListener(e -> {
            mediator.rightJustify();
            Focuser.requestFocus(focusedPane);
        });

        // caret を listen してボタンを制御する
        CaretListener caretListener = e -> {
            JTextPane pane = (JTextPane)e.getSource();
            int p = pane.getSelectionStart() - 1;
            AttributeSet a = pane.getStyledDocument().getCharacterElement(p).getAttributes();

            boldButton.setSelected(StyleConstants.isBold(a));
            italicButton.setSelected(StyleConstants.isItalic(a));
            underlineButton.setSelected((StyleConstants.isUnderline(a)));
            colorButton.setColor(StyleConstants.getForeground(a));

            int align = StyleConstants.getAlignment(a);
            leftJustify.setSelected(align == StyleConstants.ALIGN_LEFT);
            centerJustify.setSelected(align == StyleConstants.ALIGN_CENTER);
            rightJustify.setSelected(align == StyleConstants.ALIGN_RIGHT);
        };
        editorFrame.getEditor().getSOAPane().getTextPane().addCaretListener(caretListener);
        editorFrame.getEditor().getPPane().getTextPane().addCaretListener(caretListener);
    }

    /**
     * Bold, Italic, Underline ボタンのパネルを作る.
     *
     * @return Font Panel
     */
    private JPanel createFontPanel() {
        boldButton = new FontButton("B", "bold left");
        italicButton = new FontButton("I", "italic");
        underlineButton = new FontButton("U", "underline center");
        colorButton = new ColorButton("right");

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        add(boldButton);
        add(italicButton);
        add(underlineButton);
        add(colorButton);
        return panel;
    }

    /**
     * 書式ボタンのパネルを作る.
     *
     * @return Justification Panel
     */
    private JPanel createJustifyPanel() {
        leftJustify = new JustifyButton("left");
        centerJustify = new JustifyButton("center");
        rightJustify = new JustifyButton("right");
        ButtonGroup justifyGroup = new ButtonGroup();
        justifyGroup.add(leftJustify);
        justifyGroup.add(centerJustify);
        justifyGroup.add(rightJustify);
        leftJustify.setSelected(true);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(0));
        add(leftJustify);
        add(centerJustify);
        add(rightJustify);
        return panel;
    }

    /**
     * Color ボタン.
     */
    private class ColorButton extends PNSToggleButton {
        private String LETTER = "A";
        private double SCALE = 1.3d;
        private Font font = new Font("Arial", Font.BOLD, 12)
                .deriveFont(AffineTransform.getScaleInstance(SCALE, 1));
        private Color color = Color.BLACK;

        public ColorButton(String format) {
            super(format);
            setPreferredSize(new Dimension(48, 24));
            setMaximumSize(new Dimension(48, 24));
            setMinimumSize(new Dimension(48, 24));
            setBorderPainted(false);
            setSelected(false);
        }

        public void setColor(Color color) {
            this.color = color;
            repaint();
        }

        @Override
        public void paintIcon(Graphics2D g) {
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int strW = (int) ((double)fm.stringWidth(LETTER) * SCALE);
            int strH = fm.getAscent()-4;
            int w = getWidth();
            int h = getHeight();

            g.drawString(LETTER, (w - strW) / 2, (h + strH) / 2);
            g.setColor(color);
            g.fillRect((w - strW) / 2 - 1, h - 8, strW, 4);
        }
    }

    /**
     * Bold, Italic, Underline ボタン.
     */
    private class FontButton extends PNSToggleButton {
        private double SCALE = 1.3d;
        private Font boldFont = new Font("Courier", Font.BOLD, 14)
                .deriveFont(AffineTransform.getScaleInstance(SCALE, 1));
        private Font italicFont = new Font("Courier", Font.ITALIC, 14)
                .deriveFont(AffineTransform.getScaleInstance(SCALE, 1));
        private Font plainFont = new Font("Courier", Font.PLAIN, 14)
                .deriveFont(AffineTransform.getScaleInstance(SCALE, 1));

        private String letter;
        private boolean bold, italic, underline;

        public FontButton(String letter, String format) {
            super(format);
            this.letter = letter;
            bold = format.contains("bold");
            italic = format.contains("italic");
            underline = format.contains("underline");

            setPreferredSize(new Dimension(48, 24));
            setMaximumSize(new Dimension(48, 24));
            setMinimumSize(new Dimension(48, 24));
            setBorderPainted(false);
            setSelected(false);
        }

        @Override
        public void paintIcon(Graphics2D g) {
            FontMetrics fm = g.getFontMetrics();
            int strW = (int) ((double)fm.stringWidth(letter) * SCALE);
            int strH = fm.getAscent()-4;
            int w = getWidth();
            int h = getHeight();

            if (bold) {
                g.setFont(boldFont);
            } else if (italic) {
                g.setFont(italicFont);
            } else {
                g.setFont(plainFont);
            }

            int x = (w - strW) / 2;
            int y = (h + strH) / 2;
            // fine tuning
            if (italic) {
                x = x - (int) (4d * SCALE);
                y = y + 1;
            } else if (bold){
                y = y + 1;
            }
            g.drawString(letter, x, y);
            if (underline) {
                g.drawLine((w - strW) / 2, h - 5, (w + strW) / 2, h - 5);
            }
        }
    }

    /**
     * 書式ボタン.
     */
    private class JustifyButton extends PNSToggleButton {
        private int LONG = 20;
        private int SHORT = 14;

        public JustifyButton(String format) {
            super(format);
            setPreferredSize(new Dimension(48, 24));
            setMaximumSize(new Dimension(48, 24));
            setMinimumSize(new Dimension(48, 24));
            setBorderPainted(false);
            setSelected(false);
        }

        @Override
        public void paintIcon(Graphics2D g) {
            int interval = 3;
            int l = (getWidth() - LONG) / 2;
            int s = swingConstant == SwingConstants.LEFT
                    ? l
                    : swingConstant == SwingConstants.RIGHT
                    ? l + (LONG-SHORT)
                    : (getWidth() - SHORT) / 2;

            int y = 6;
            g.drawLine(l, y, l+LONG, y); y += interval;
            g.drawLine(s, y, s+SHORT, y); y += interval;
            g.drawLine(l, y, l+LONG, y); y += interval;
            g.drawLine(s, y, s+SHORT, y); y += interval;
            g.drawLine(l, y, l+LONG, y);
        }
    }

    /**
     * スタンプ検索パネル.
     *
     * @return Stamp Search Panel
     */
    private JPanel createDiagnosisSearchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        CompletableSearchField keywordFld = new CompletableSearchField(30);
        keywordFld.setLabel("病名検索");
        keywordFld.setPreferences(prefs);
        keywordFld.putClientProperty("Quaqua.TextField.style", "search");
        keywordFld.setPreferredSize(new Dimension(10, 26));
        keywordFld.addActionListener(e -> {
            String text = keywordFld.getText();

            if (text != null && !text.equals("")) {
                JPopupMenu popup = new JPopupMenu();
                String pattern = ".*" + keywordFld.getText() + ".*";

                StampBoxPlugin stampBox = mediator.getStampBox();
                StampTree tree = stampBox.getStampTree(IInfoModel.ENTITY_DIAGNOSIS);

                StampTreeMenuBuilder builder = new StampTreeMenuBuilder(tree, pattern);
                //builder.addStampTreeMenuListener(new DefaultStampTreeMenuListener(realChart.getDiagnosisDocument().getDiagnosisTable()));
                builder.addStampTreeMenuListener(ev -> {
                    JComponent c = ((ChartImpl)editorFrame.getChart()).getDiagnosisDocument().getDiagnosisTable();
                    TransferHandler handler = c.getTransferHandler();
                    handler.importData(c, ev.getTransferable());
                    // transfer 後にキーワードフィールドをクリアする
                    keywordFld.setText("");
                });
                builder.buildRootless(popup);

                if (popup.getComponentCount() != 0) {
                    Point loc = keywordFld.getLocation();
                    popup.show(keywordFld.getParent(), loc.x, loc.y + keywordFld.getHeight());
                }
            }
        });

        // ctrl-return でもリターンキーの notify-field-accept が発生するようにする
        InputMap map = keywordFld.getInputMap();
        Object value = map.get(KeyStroke.getKeyStroke("ENTER"));
        map.put(KeyStroke.getKeyStroke("ctrl ENTER"), value);

        panel.add(keywordFld, BorderLayout.CENTER);
        //panel.add(Box.createVerticalStrut(9), BorderLayout.NORTH);
        //panel.add(Box.createVerticalStrut(9), BorderLayout.SOUTH);
        //panel.add(Box.createHorizontalStrut(5), BorderLayout.WEST);
        //panel.add(Box.createHorizontalStrut(5), BorderLayout.EAST);

        return panel;
    }
}
