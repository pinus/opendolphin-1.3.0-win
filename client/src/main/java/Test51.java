import open.dolphin.client.ClientContext;
import open.dolphin.client.ClientContextStub;
import open.dolphin.client.ColorChooserComp;
import open.dolphin.ui.PNSToggleButton;

import javax.swing.*;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;

/**
 * @author pns
 */
public class Test51 {

    public static void main(String[] argv) {
        ClientContextStub stub = new ClientContextStub();
        ClientContext.setClientContextStub(stub);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);

        JPanel cmdPanel = new JPanel();
        cmdPanel.setPreferredSize(new Dimension(600, 28));
        cmdPanel.setLayout(new BoxLayout(cmdPanel, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        cmdPanel.add(buttonPanel);
        cmdPanel.add(Box.createVerticalGlue());

        JTextArea textArea = new JTextArea();
        textArea.setPreferredSize(new Dimension(600, 500));
        textArea.setText("TEST");

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(cmdPanel);
        content.add(textArea);

        JToggleButton boldButton = new LetterButton("B", "bold left");
        JToggleButton italicButton = new LetterButton("I", "italic");
        JToggleButton underlineButton = new LetterButton("U", "underline center");
        JToggleButton colorButton = new ColorButton("right");

        JToggleButton leftJustify = new JustifyButton("left");
        JToggleButton centerJustify = new JustifyButton("center");
        JToggleButton rightJustify = new JustifyButton("right");
        ButtonGroup justifyGroup = new ButtonGroup();
        justifyGroup.add(leftJustify);
        justifyGroup.add(centerJustify);
        justifyGroup.add(rightJustify);

        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(boldButton);
        buttonPanel.add(italicButton);
        buttonPanel.add(underlineButton);
        buttonPanel.add(colorButton);
        buttonPanel.add(Box.createHorizontalStrut(32));
        buttonPanel.add(leftJustify);
        buttonPanel.add(centerJustify);
        buttonPanel.add(rightJustify);

        buttonPanel.add(Box.createHorizontalGlue());

        frame.add(content);
        frame.pack();
        frame.setVisible(true);
    }

    static class ColorButton extends PNSToggleButton {

        private static String LETTER = "A";
        private double SCALE = 1.3d;
        private Font font = new Font("Arial", Font.BOLD, 12)
                .deriveFont(AffineTransform.getScaleInstance(SCALE, 1));
        private Color color = Color.BLACK;
        private ColorChooserComp chooser;
        private JPopupMenu menu;

        public ColorButton(String format) {
            super(format);
            setPreferredSize(new Dimension(48, 24));
            setBorderPainted(false);
            setSelected(false);
            addActionListener(this::pressed);

            menu = new JPopupMenu();
            chooser = new ColorChooserComp();
            menu.add(chooser);
            chooser.addPropertyChangeListener(ColorChooserComp.SELECTED_COLOR, e -> {
                color = (Color) e.getNewValue();
                repaint();
                Action action = new StyledEditorKit.ForegroundAction("selected", color);
                //action.actionPerformed(new ActionEvent(textPane, ActionEvent.ACTION_PERFORMED, "foreground"));
                menu.setVisible(false);
            });

        }

        public void pressed(ActionEvent ae) {
            menu.show(this, 0, this.getHeight());
            setSelected(false);
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


    static class LetterButton extends PNSToggleButton {
        private double SCALE = 1.3d;
        private Font boldFont = new Font("Courier", Font.BOLD, 14)
                .deriveFont(AffineTransform.getScaleInstance(SCALE, 1));
        private Font italicFont = new Font("Courier", Font.ITALIC, 14)
                .deriveFont(AffineTransform.getScaleInstance(SCALE, 1));
        private Font plainFont = new Font("Courier", Font.PLAIN, 14)
                .deriveFont(AffineTransform.getScaleInstance(SCALE, 1));

        private String letter;
        private boolean bold, italic, underline;

        public LetterButton(String letter, String format) {
            super(format);
            this.letter = letter;
            bold = format.contains("bold");
            italic = format.contains("italic");
            underline = format.contains("underline");

            setPreferredSize(new Dimension(48, 24));
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

            if (italic) {
                g.drawString(letter, (w - strW) / 2 - (int) (4d * SCALE), (h + strH) / 2);
            } else {
                g.drawString(letter, (w - strW) / 2, (h + strH) / 2);
            }
            if (underline) {
                g.drawLine((w - strW) / 2, h - 5, (w + strW) / 2, h - 5);
            }
        }
    }

    static class JustifyButton extends PNSToggleButton {
        private int LONG = 20;
        private int SHORT = 14;


        public JustifyButton(String format) {
            super(format);
            setPreferredSize(new Dimension(48, 24));
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
}
