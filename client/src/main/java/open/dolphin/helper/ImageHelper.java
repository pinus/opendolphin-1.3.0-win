package open.dolphin.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ImageHelper.
 *
 * @author kazm
 * @author pns
 */
public class ImageHelper {
    private static Logger logger = LoggerFactory.getLogger(ImageHelper.class);

    /**
     * ImageIcon から BufferedImage に変換. alpha 対応.
     *
     * @param src source ImageIcon
     * @return BufferedImage
     */
    public static BufferedImage imageToBufferedImage(ImageIcon src) {
        if (src == null) {
            return null;
        }

        int width = src.getImage().getWidth(null);
        int height = src.getImage().getHeight(null);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        src.paintIcon(null, g, 0, 0);

        g.dispose();

        return image;
    }

    /**
     * inImage の幅と高さの長い方が maxDim になるように縮小する.
     *
     * @param inImage BufferedImage
     * @param maxDim Dimension
     * @return BufferedImage
     */
    public static BufferedImage getFirstScaledInstance(BufferedImage inImage, int maxDim) {

        if (inImage.getWidth() <= maxDim && inImage.getHeight() <= maxDim) {
            return inImage;
        }

        double scale = maxDim / (double) inImage.getHeight(null);
        if (inImage.getWidth(null) > inImage.getHeight(null)) {
            scale = maxDim / (double) inImage.getWidth(null);
        }

        // Determine size of new image.
        // One of them should equal maxDim.
        int scaledW = (int) (scale * inImage.getWidth(null));
        int scaledH = (int) (scale * inImage.getHeight(null));

        // Create an image buffer in which to paint on.
        BufferedImage outImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_BGR);

        // Set the scale.
        AffineTransform tx = new AffineTransform();

        // If the image is smaller than the desired image size,
        // don't bother scaling.
        if (scale < 1.0d) {
            tx.scale(scale, scale);
        }

        // Paint image.
        Graphics2D g2d = outImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(inImage, tx, null);
        g2d.dispose();

        return outImage;
    }

    /**
     * Convert Image to PNG ByteArray.
     *
     * @param image java.awt.Image
     * @return PNG ByteArray
     */
    public static byte[] imageToByteArray(Image image) {
        byte[] ret = null;

        try (ByteArrayOutputStream bo = new ByteArrayOutputStream()) {
            Dimension d = new Dimension(image.getWidth(null), image.getHeight(null));

            BufferedImage bf = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_BGR);
            Graphics g = bf.getGraphics();
            g.setColor(Color.white);
            g.drawImage(image, 0, 0, d.width, d.height, null);

            ImageIO.write(bf, "png", bo);

            ret = bo.toByteArray();

        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        return ret;
    }

    /**
     * ImageIcon のサイズを dim サイズ以内になるように調節する.
     *
     * @param icon ImageIcon
     * @param dim Dimension
     * @return adjusted ImageIcon
     */
    public static ImageIcon adjustImageSize(ImageIcon icon, Dimension dim) {
        if ((icon.getIconHeight() > dim.height) || (icon.getIconWidth() > dim.width)) {

            Image img = icon.getImage();
            float hRatio = (float) icon.getIconHeight() / dim.height;
            float wRatio = (float) icon.getIconWidth() / dim.width;
            int h, w;

            if (hRatio > wRatio) {
                h = dim.height;
                w = (int) (icon.getIconWidth() / hRatio);

            } else {
                w = dim.width;
                h = (int) (icon.getIconHeight() / wRatio);
            }

            img = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);

        } else {
            return icon;
        }
    }

    /**
     * Convert JPEG ByteArray to PNG ByteArray.
     *
     * @param jpegBytes JPEG ByteArray
     * @return PNG ByteArray
     */
    public static byte[] toPngByteArray(byte[] jpegBytes) {
        byte[] ret = null;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(jpegBytes);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            BufferedImage bImage = ImageIO.read(bis);
            ImageIO.write(bImage, "png", bos);
            ret = bos.toByteArray();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Extract PNG Metadata "UnknownChunk".
     * Format Name: javax_imageio_png_1.0 (nativeMetadataFormatClassName)
     *
     * @param bytes PNG ByteArray
     * @param type type (4 chars)
     * @return value for the type
     */
    public static String extractMetadata(byte[] bytes, String type) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            ImageInputStream iis = ImageIO.createImageInputStream(bis);
            ImageReader reader = ImageIO.getImageReaders(iis).next();
            reader.setInput(iis, true);

            IIOMetadata metadata = reader.getImageMetadata(0);
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_png_1.0");

            // UnknownChunks length = 0 or 1, 1の場合その中に UnknownChunk が複数入る
            NodeList chunksList = root.getElementsByTagName("UnknownChunks");
            if (chunksList.getLength() > 0) {
                IIOMetadataNode chunks = (IIOMetadataNode) root.getElementsByTagName("UnknownChunks").item(0);
                for (int i = 0; i < chunks.getLength(); i++) {
                    IIOMetadataNode chunk = (IIOMetadataNode) chunks.item(i);
                    String chunkType = chunk.getAttributes().getNamedItem("type").getNodeValue();
                    if (chunkType.equals(type)) {
                        return new String((byte[]) chunk.getUserObject());
                    }
                }
            }

        } catch (IOException | RuntimeException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * Add optional data to UnknownChunks. (Metadata = com.sun.imageio.plugins.png.PNGMetadata)
     * "javax_imageio_png_1.0" > UnknownChunks > UnknownChunk [type (4 chars), UserObject (byte[])]
     *
     * @param bytes PNG ByteArray
     * @param type type (4 chars)
     * @param value UserObject for the type
     * @return PNG ByteArray with added metadata
     */
    public static byte[] addMetadata(byte[] bytes, String type, String value) {
        byte[] ret = null;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            ImageInputStream iis = ImageIO.createImageInputStream(bis);
            ImageReader reader = ImageIO.getImageReaders(iis).next();
            reader.setInput(iis, true);

            // if not PNG bytes, throw exception
            if (!reader.getFormatName().equals("png")) {
                throw new IOException("not png bytes");
            }

            IIOImage image = reader.readAll(0, null);

            // preparing nodes
            IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
            IIOMetadataNode chunks = new IIOMetadataNode("UnknownChunks");
            IIOMetadataNode chunk = new IIOMetadataNode("UnknownChunk");

            chunk.setAttribute("type", type);
            chunk.setUserObject(value.getBytes());

            chunks.appendChild(chunk);
            root.appendChild(chunks);

            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, param);

            // mergeNativeTree(root)
            metadata.mergeTree("javax_imageio_png_1.0", root);
            image.setMetadata(metadata);

            ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
            writer.setOutput(ios);
            writer.write(metadata, image, param);

            ret = bos.toByteArray();

        } catch (IOException | RuntimeException e) {
            logger.error(e.getMessage());
        }
        return ret;
    }

    /**
     * Show all Nodes.
     *
     * @param node node to show
     */
    private static void showNode(IIOMetadataNode node) {
        System.out.println("node name = " + node.getNodeName());
        System.out.println("node value = " + node.getNodeValue());
        System.out.println("node type = " + node.getNodeType());
        System.out.println("node attribute size = " + node.getAttributes().getLength());

        for (int i=0; i<node.getAttributes().getLength(); i++) {
            System.out.println("attribute node name = " + node.getAttributes().item(i).getNodeName());
            System.out.println("attribute node value = " + node.getAttributes().item(i).getNodeValue());
            System.out.println("attribute node type = " + node.getAttributes().item(i).getNodeType());
        }

        int len = node.getChildNodes().getLength();
        if (len > 0) {
            for (int i=0; i<len; i++) {
                showNode((IIOMetadataNode) node.getChildNodes().item(i));
            }
        }
    }

    /**
     * "apple.awt.brushMetalLook" の Container のバックグランドが黒くなるのを直す.
     *
     * @param component 修正する Component
     */
    public static void setContainerTransparent(Component component) {
        if (component instanceof Container) {
            if (component instanceof JComponent) {
                ((JComponent)component).setOpaque(false);
            }
            for (Component c : ((Container) component).getComponents()) {
                setContainerTransparent(c);
            }
        }
    }

    public static void main (String[] arg) {
        String sample1 = "/schemaeditor/Sample-square.JPG";

        InputStream in = ImageHelper.class.getResourceAsStream(sample1);

        byte[] buf = null;
        try {
            int n = in.available();
            buf = new byte[n];
            for (int i = 0; i < n; i++) buf[i] = (byte) in.read();
        } catch (IOException ex) {
        }

        byte[] pngBytes = toPngByteArray(buf);

        String type = "DSIZ";
        String value = "100x200";

        byte[] buf2 = addMetadata(pngBytes, type, value);
        String val = extractMetadata(buf2, type);

        System.out.println("type = " + type);
        System.out.println("value = " + val);
    }
}
