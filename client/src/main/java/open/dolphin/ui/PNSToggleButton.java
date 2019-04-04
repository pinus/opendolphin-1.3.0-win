package open.dolphin.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Mac風デザインの ToggleButton.
 * paintIcon を override して使う.
 *
 * @author pns
 */
public class PNSToggleButton extends JToggleButton implements IPNSButton {

    protected Window parent = null;
    protected boolean appForeground = true;
    protected int swingConstant;

    /**
     * format に応じて角丸処理をする.
     *
     * @param format toggle button の位置
     */
    public PNSToggleButton(String format) {
        swingConstant = format.contains("right")
                ? SwingConstants.RIGHT
                : format.contains("left")
                ? SwingConstants.LEFT
                : SwingConstants.CENTER;
    }

    /**
     * 組み込まれるときに addNotify が呼ばれるのを利用して parent を登録する.
     */
    @Override
    public void addNotify() {
        super.addNotify();

        if (parent == null) {
            parent = SwingUtilities.windowForComponent(this);

            // AppForegroundListener
//            com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
//            app.addAppEventListener(new com.apple.eawt.AppForegroundListener() {
//                @Override
//                public void appRaisedToForeground(com.apple.eawt.AppEvent.AppForegroundEvent afe) {
//                    appForeground = true;
//                    repaint();
//                }
//
//                @Override
//                public void appMovedToBackground(com.apple.eawt.AppEvent.AppForegroundEvent afe) {
//                    appForeground = false;
//                    repaint();
//                }
//            });

        }
    }

    @Override
    public void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // background and border
        if (parent.isActive() && appForeground) {
            if (this.isSelected()) {
                g.setColor(ACTIVE_FILL_SELECTED);
                fill(g, w, h, swingConstant);
                g.setColor(ACTIVE_FRAME_SELECTED);
                frame(g, w, h, swingConstant);

            } else {
                g.setColor(ACTIVE_FILL);
                fill(g, w, h, swingConstant);
                g.setColor(ACTIVE_FRAME);
                frame(g, w, h, swingConstant);
            }

        } else {
            if (this.isSelected()) {
                g.setColor(INACTIVE_FILL_SELECTED);
                fill(g, w, h, swingConstant);
                g.setColor(INACTIVE_FRAME);
                frame(g, w, h, swingConstant);

            } else {
                g.setColor(INACTIVE_FILL);
                fill(g, w, h, swingConstant);
                g.setColor(INACTIVE_FRAME);
                frame(g, w, h, swingConstant);
            }
        }

        // foreground
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
        paintIcon(g);
    }

    /**
     * ここを override して前景を書く.
     *
     * @param g Graphics2D
     */
    public void paintIcon(Graphics2D g) { }
}