package open.dolphin.laf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPasswordFieldUI;

/**
 *
 * @author pns
 */
public class MyPasswordFieldUI extends BasicPasswordFieldUI {

    private JPasswordField tf;

    public static ComponentUI createUI(JComponent c) {
        return new MyPasswordFieldUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        tf = (JPasswordField) c;

        Border border = new EmptyBorder(0, 8, 0, 8);
        c.setBorder(border);

        c.setBackground(Color.WHITE);
        c.setOpaque(false);

        c.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                c.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                c.repaint();
            }
        });
    }

    @Override
    public void paintSafely(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (tf.isFocusOwner()) {
            g.setColor(MyTextFieldUI.LIGHT_BLUE);
            g.fillRoundRect(0, 0, tf.getWidth(), tf.getHeight(), 10, 10);

        } else {
            // focus されていないときにグレーの枠線を描く
            g.setColor(MyTextFieldUI.LIGHTER_GRAY);
            g.drawRoundRect(2, 2, tf.getWidth()-5, tf.getHeight()-5, 5, 5);
        }
        // 背景を塗る
        g.setColor(tf.getBackground());
        g.fillRoundRect(3, 3, tf.getWidth()-6, tf.getHeight()-6, 5, 5);

        g.dispose();
        super.paintSafely(graphics);
    }
}
