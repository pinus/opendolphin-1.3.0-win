package open.dolphin.dnd;

import open.dolphin.client.ClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;

/**
 * DragImage の utility 付きの TransferHandler.
 *
 * @author pns
 */
public class DolphinTransferHandler extends TransferHandler {
    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.getLogger(DolphinTransferHandler.class);
    private final boolean isWin = ClientContext.isWin();

    private final Point offset = new Point(0, 0);

    /**
     * String から DragImage を作ってセットする.
     *
     * @param text String
     */
    protected void setDragImage(String text) {
        if (text.contains("\n")) {
            // 改行があれば html 化する
            text = text.replaceAll("\n", "<br>");
            text = "<html>" + text + "</html>";
        }
        JLabel label = new JLabel(text);
        label.setSize(label.getPreferredSize());
        setDragImage(label, true);
    }

    /**
     * JLabel から DragImage を作ってセットする.
     *
     * @param label JLabel
     */
    protected void setDragImage(JLabel label) {
        Point mousePosition = label.getMousePosition();
        if (mousePosition != null) {
            offset.x = -mousePosition.x;
            offset.y = -mousePosition.y;
        }
        setDragImage(label, false);
    }

    /**
     * JLabel から DragImage を作ってセットする. 文字ラベル対応.
     *
     * @param label JLabel
     * @param clip  true の場合，文字幅に合わせて clipping する
     */
    protected void setDragImage(JLabel label, boolean clip) {
        int width = label.getWidth() != 0? label.getWidth() : label.getPreferredSize().width;
        int height = label.getHeight() != 0? label.getHeight() : label.getPreferredSize().height;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();

        // background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        label.paint(g);

        if (clip) {
            // 文字列の長さに応じて幅を調節する
            int stringWidth = g.getFontMetrics().stringWidth(label.getText());
            if (stringWidth + 8 < width) { // 8ドット余裕
                width = stringWidth + 8;
                image = image.getSubimage(0, 0, width, height);
            }
        }

        // グレーの枠を付ける
        g.setColor(Color.gray);
        g.drawRect(0, 0, width - 1, height - 1);

        setDragImage(image);
    }

    /**
     * DragImageOffset を設定してから setDragImage する.
     *
     * @param image drag image
     */
    @Override
    public void setDragImage(Image image) {
        if (offset.x == 0 && offset.y == 0) {
            // センタリング
            offset.x = -image.getWidth(null) / 2;
            offset.y = -image.getHeight(null) / 2;
        }
        // windows では offset の方向が逆
        if (isWin) {
            offset.x = -offset.x;
            offset.y = -offset.y;
        }

        setDragImageOffset(offset);

        offset.x = 0;
        offset.y = 0;

        super.setDragImage(image);
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
        logger.error("deprecated: super class = " + super.getClass());
        return false;
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        logger.error("deprecated: super class = " + super.getClass());
        return false;
    }
}
