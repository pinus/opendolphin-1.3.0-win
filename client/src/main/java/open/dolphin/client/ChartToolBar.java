package open.dolphin.client;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.ui.CompletableSearchField;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.PNSToggleButton;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * EditorFrame 特有の JToolBar.
 *
 * @author pns
 */
public class ChartToolBar extends JToolBar {
    private static final long serialVersionUID = 1L;

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

    private JComboBox<Integer> sizeCombo;
    private CompletableSearchField stampSearchField;

    private boolean pause = false;

    public ChartToolBar(final EditorFrame chart) {
        super();
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

        add(Box.createHorizontalStrut(4));
        add(createFontPanel());
        add(createSizePanel());
        add(Box.createHorizontalStrut(24));
        add(createJustifyPanel());
        add(Box.createHorizontalStrut(24));
        add(createDiagnosisSearchPanel());
        add(Box.createHorizontalStrut(4));
    }

    /**
     * リスナーの接続を行う.
     */
    private void connect() {

        boldButton.addActionListener(e -> {
            mediator.fontBold();
            Focuser.requestFocus(mediator.getCurrentComponent());
        });
        italicButton.addActionListener(e -> {
            mediator.fontItalic();
            Focuser.requestFocus(mediator.getCurrentComponent());
        });
        underlineButton.addActionListener(e -> {
            mediator.fontUnderline();
            Focuser.requestFocus(mediator.getCurrentComponent());
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
                Focuser.requestFocus(mediator.getCurrentComponent());
            });
            menu.show(b, 0, b.getHeight());
            b.setSelected(false);
        });

        leftJustify.addActionListener(e -> {
            mediator.leftJustify();
            Focuser.requestFocus(mediator.getCurrentComponent());
        });
        centerJustify.addActionListener(e -> {
            mediator.centerJustify();
            Focuser.requestFocus(mediator.getCurrentComponent());
        });
        rightJustify.addActionListener(e -> {
            mediator.rightJustify();
            Focuser.requestFocus(mediator.getCurrentComponent());
        });

        sizeCombo.addItemListener(e -> {
            if (pause) { return; }
            int size = (int) e.getItem();
            mediator.setFontSize(size);
            Focuser.requestFocus(mediator.getCurrentComponent());
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

            int size = StyleConstants.getFontSize(a);
            pause = true;
            sizeCombo.setSelectedItem(size);
            SwingUtilities.invokeLater(() -> pause = false);
        };
        editorFrame.getEditor().getSOAPane().getTextPane().addCaretListener(caretListener);
        editorFrame.getEditor().getPPane().getTextPane().addCaretListener(caretListener);
    }

    /**
     * Font size 設定パネル.
     *
     * @return Size Panel
     */
    private JPanel createSizePanel() {
        sizeCombo = new JComboBox<>(mediator.FONT_SIZE);
        sizeCombo.setBorder(BorderFactory.createEmptyBorder());
        sizeCombo.setSelectedItem(mediator.DEFAULT_FONT_SIZE);
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(sizeCombo);
        return panel;
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
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
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
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
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
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        stampSearchField = new CompletableSearchField(30);
        stampSearchField.setLabel("スタンプ検索");
        stampSearchField.setPreferences(prefs);
        stampSearchField.putClientProperty("Quaqua.TextField.style", "search");
        stampSearchField.setPreferredSize(new Dimension(10, 26));
        stampSearchField.addActionListener(e -> {
            String text = stampSearchField.getText();

            if (text != null && !text.equals("")) {
                String pattern = ".*" + stampSearchField.getText() + ".*";

                JPopupMenu popup = mediator.createAllStampPopup(pattern, ev -> {

                    JComponent c = null;
                    switch (ev.getEntity()) {
                        case IInfoModel.ENTITY_DIAGNOSIS:
                            c = ((ChartImpl) editorFrame.getChart()).getDiagnosisDocument().getDiagnosisTable();
                            break;

                        case IInfoModel.ENTITY_TEXT:
                            c = mediator.getCurrentComponent();
                            break;

                        default:
//                        case IInfoModel.ENTITY_PATH:
//                        case IInfoModel.ENTITY_GENERAL_ORDER:
//                        case IInfoModel.ENTITY_OTHER_ORDER:
//                        case IInfoModel.ENTITY_TREATMENT:
//                        case IInfoModel.ENTITY_SURGERY_ORDER:
//                        case IInfoModel.ENTITY_RADIOLOGY_ORDER:
//                        case IInfoModel.ENTITY_LABO_TEST:
//                        case IInfoModel.ENTITY_PHYSIOLOGY_ORDER:
//                        case IInfoModel.ENTITY_BACTERIA_ORDER:
//                        case IInfoModel.ENTITY_INJECTION_ORDER:
//                        case IInfoModel.ENTITY_MED_ORDER:
//                        case IInfoModel.ENTITY_BASE_CHARGE_ORDER:
//                        case IInfoModel.ENTITY_INSTRACTION_CHARGE_ORDER:
                            c = editorFrame.getEditor().getPPane().getTextPane();
                            break;
                    }

                    if (Objects.nonNull(c)) {
                        TransferHandler handler = c.getTransferHandler();
                        handler.importData(c, ev.getTransferable());
                    }

                    // transfer 後にキーワードフィールドをクリアする
                    stampSearchField.setText("");
                });

                if (popup.getComponentCount() != 0) {
                    popup.show(stampSearchField, 0, stampSearchField.getHeight());
                }
            }
        });

        // ctrl-return でもリターンキーの notify-field-accept が発生するようにする
        InputMap map = stampSearchField.getInputMap();
        Object value = map.get(KeyStroke.getKeyStroke("ENTER"));
        map.put(KeyStroke.getKeyStroke("ctrl ENTER"), value);

        panel.add(stampSearchField);

        return panel;
    }

    /**
     * stamp search field を返す.
     * @return stamp search field
     */
    public CompletableSearchField getStampSearchField() {
        return stampSearchField;
    }
}
