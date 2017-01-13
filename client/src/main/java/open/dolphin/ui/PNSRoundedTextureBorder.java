package open.dolphin.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.border.AbstractBorder;

/**
 * イメージを texture で，角丸 fill するボーダ.
 * @author pns
 */
public class PNSRoundedTextureBorder extends AbstractBorder {
    private static final long serialVersionUID = 1L;

    private static final Color EDGE_COLOR = new Color(200,200,200);
    private final ImageIcon image;
    private final Insets insets;

    public PNSRoundedTextureBorder(ImageIcon image, Insets insets) {
        this.image = image;
        this.insets = insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();

        BufferedImage buf = PNSBorderFactory.imageToBufferedImage(image);
        TexturePaint paint = new TexturePaint(buf, new Rectangle2D.Double(0, 0, buf.getWidth(), buf.getHeight()));
        g2d.setPaint(paint);
        g2d.fillRoundRect(x, y, width-1, height-1, 10, 10);

        g2d.setColor(EDGE_COLOR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawRoundRect(x, y, width-1, height-1, 10, 10);
   }

    @Override
    public Insets getBorderInsets(Component c){
        return insets;
    }

    @Override
    public boolean isBorderOpaque(){
        return false;
    }
}