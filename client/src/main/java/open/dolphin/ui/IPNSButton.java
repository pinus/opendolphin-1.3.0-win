package open.dolphin.ui;

import javax.swing.*;
import java.awt.*;

/**
 * ボタンの色と角丸処理.
 *
 * @author pns
 */
interface IPNSButton {

    Color INACTIVE_FRAME = new Color(219, 219, 219);
    Color INACTIVE_FILL_SELECTED = new Color(227, 227, 227);
    Color INACTIVE_FILL = new Color(246, 246, 246);
    Color ACTIVE_FRAME = new Color(175, 175, 175);
    Color ACTIVE_FRAME_SELECTED = new Color(91, 91, 91);
    Color ACTIVE_FILL = new Color(240, 240, 240);
    Color ACTIVE_FILL_SELECTED = new Color(100, 100, 100);
    Color INACTIVE_TEXT = new Color(180, 180, 180);
    Color INACTIVE_TEXT_SELECTED = new Color(50, 50, 50);

    int RADIUS = 6; // 角丸の半径

    /**
     * 枠を書く.
     *
     * @param g Graphics2D
     * @param w width
     * @param h height
     * @param swingConstant SwingConstants
     */
    default void frame(Graphics2D g, int w, int h, int swingConstant) {
        if (swingConstant == SwingConstants.LEFT) {
            g.drawLine(RADIUS, 0, w-1, 0); // 上
            g.drawLine(RADIUS, h-1, w-1, h-1); // 下
            g.drawLine(0, RADIUS, 0, h-RADIUS-1); // 左
            g.drawArc(0, 0, RADIUS*2, RADIUS*2, 90, 90);
            g.drawArc(0,h-RADIUS*2-1, RADIUS*2, RADIUS*2, 180,90);

        } else if (swingConstant == SwingConstants.RIGHT) {
            g.drawLine(0, 0, w-RADIUS-1, 0); // 上
            g.drawLine(0, 0, 0, h-1); // 左
            g.drawLine(w-1,RADIUS, w-1, h-RADIUS-1); // 右
            g.drawLine(0, h-1, w-RADIUS-1, h-1); // 下
            g.drawArc(w-RADIUS*2-1, 0, RADIUS*2, RADIUS*2, 0, 90);
            g.drawArc(w-RADIUS*2-1,h-RADIUS*2-1, RADIUS*2, RADIUS*2, 270,90);

        } else {
            g.drawLine(0, 0, w-1, 0); // 上
            g.drawLine(0, 0, 0, h-1); // 左
            g.drawLine(0, h-1, w-1, h-1); // 下
        }
    }

    /**
     * 塗る.
     *
     * @param g Graphics2D
     * @param w width
     * @param h height
     * @param swingConstant SwingConstants
     */
    default void fill(Graphics2D g, int w, int h, int swingConstant) {
        if (swingConstant == SwingConstants.LEFT) {
            g.fillRoundRect(0, 0, w, h, RADIUS*2, RADIUS*2);
            g.fillRect(w-RADIUS*2,0, w, h);
        } else if (swingConstant == SwingConstants.RIGHT) {
            g.fillRoundRect(0, 0, w-1, h, RADIUS*2, RADIUS*2);
            g.fillRect(0,0, RADIUS*2, h);
        } else {
            g.fillRect(0, 0, w, h);
        }
    }
}
