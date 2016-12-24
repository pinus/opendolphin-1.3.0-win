package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import open.dolphin.ui.*;

/**
 * ImagePalette.
 *
 * @author Minagawa,Kazushi modified by pns
 */
public class ImagePalette extends JPanel {
    private static final long serialVersionUID = -6218860704261308773L;

    private static final int DEFAULT_COLUMN_COUNT 	=   3;
    private static final int DEFAULT_IMAGE_WIDTH 	= 120;
    private static final int DEFAULT_IMAGE_HEIGHT 	= 120;
    private static final String[] DEFAULT_IMAGE_SUFFIX = {".jpg"};

    private ImageTableModel imageTableModel;
    private int imageWidth;
    private int imageHeight;
    private JTable imageTable;
    private File imageDirectory;
    private String[] suffix = DEFAULT_IMAGE_SUFFIX;
    private boolean showHeader;

    private final Border selectedBorder = MyBorderFactory.createSelectedBorder();
    // private Border normalBorder = MyBorderFactory.createClearBorder();
    private final Border normalBorder = BorderFactory.createEmptyBorder();

    public ImagePalette(String[] columnNames, int columnCount, int width, int height) {
        imageTableModel = new ImageTableModel(columnNames, columnCount);
        imageWidth = width;
        imageHeight = height;
        initComponent(columnCount);
    }

    public ImagePalette() {
        this(null, DEFAULT_COLUMN_COUNT, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }

    public List getImageList() {
        return imageTableModel.getImageList();
    }

    public void setImageList(List<ImageEntry> list) {
        imageTableModel.setImageList(list);
    }

    public JTable getable() {
        return imageTable;
    }

    public String[] getimageSuffix() {
        return suffix;
    }

    public void setImageSuffix(String[] suffix) {
        this.suffix = suffix;
    }

    public File getImageDirectory() {
        return imageDirectory;
    }

    public void setImageDirectory(File imageDirectory) {
        this.imageDirectory = imageDirectory;
        refresh();
    }

    public void dispose() {
        if (imageTableModel != null) {
            imageTableModel.clear();
        }
    }

    public void refresh() {

        if ( (! imageDirectory.exists()) || (! imageDirectory.isDirectory()) ) {
            return;
        }

        Dimension imageSize = new Dimension(imageWidth, imageHeight);
        File[] imageFiles = listImageFiles(imageDirectory, suffix);
        if (imageFiles != null && imageFiles.length > 0) {
            List<ImageEntry> imageList = new ArrayList<>();
            for (File imageFile : imageFiles) {
                try {
                    URL url = imageFile.toURI().toURL();
                    ImageIcon icon = new ImageIcon(url);
                    ImageEntry entry = new ImageEntry();
                    entry.setImageIcon(adjustImageSize(icon, imageSize));
                    entry.setUrl(url.toString());
                    imageList.add(entry);

                }catch (MalformedURLException e) {
                    e.printStackTrace(System.err);
                }
            }
            imageTableModel.setImageList(imageList);
        }
    }

    private void initComponent(int columnCount) {

        // Image table を生成する
        imageTable = new JTable(imageTableModel);
        imageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imageTable.setCellSelectionEnabled(true);
        imageTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        imageTable.setTransferHandler(new ImageTransferHandler());

        // ドラッグ処理
        imageTable.addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseDragged (MouseEvent e) {
                imageTable.getTransferHandler().exportAsDrag((JComponent) e.getSource(), e, TransferHandler.COPY);
            }
        });

        // ダブルクリックで直接入力
        imageTable.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // ダブルクリックしてすぐドラッグした場合はドラッグを優先する
                    // Protection Time ms 後にマウスが動いていない場合のみダブルクリックと判定する
                    Point p1 = imageTable.getMousePosition();
                    try{ Thread.sleep(GUIConst.PROTECTION_TIME); } catch (Exception ex) {}
                    Point p2 = imageTable.getMousePosition();

                    if (p1 != null && p2 != null && p1.x == p2.x && p1.y == p2.y) {
                        int row = imageTable.getSelectedRow();
                        int col = imageTable.getSelectedColumn();
                        ImageEntry entry = (ImageEntry) imageTable.getModel().getValueAt(row, col);

                        List<Chart> allFrames = EditorFrame.getAllEditorFrames();
                        if (! allFrames.isEmpty()) {
                            Chart frame = allFrames.get(0);
                            KartePane pane = ((EditorFrame) frame).getEditor().getSOAPane();
                            // caret を最後に送ってから import する
                            JTextPane textPane = pane.getTextPane();
                            KarteStyledDocument doc = pane.getDocument();
                            textPane.setCaretPosition(doc.getLength());
                            // import
                            pane.imageEntryDropped(entry);
                        }
                    }
                }
            }
        });

        for (int i = 0; i < columnCount; i++) {
            imageTable.getColumnModel().getColumn(i).setPreferredWidth(imageWidth);
        }
        imageTable.setRowHeight(imageHeight);

        ImageRenderer imageRenderer = new ImageRenderer();
        imageRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        imageTable.setDefaultRenderer(java.lang.Object.class, imageRenderer);

        setLayout(new BorderLayout());
        MyJScrollPane scroller = new MyJScrollPane();

        if (showHeader) {
            scroller.setViewportView(imageTable);
            this.add(scroller);
        } else {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(imageTable);
            scroller.setViewportView(panel);
            this.add(scroller);
        }

        imageTable.setIntercellSpacing(new Dimension(0,0));
    }

    private ImageIcon adjustImageSize(ImageIcon icon, Dimension dim) {

        if ( (icon.getIconHeight() > dim.height) || (icon.getIconWidth() > dim.width) ) {
            Image img = icon.getImage();
            float hRatio = (float)icon.getIconHeight() / dim.height;
            float wRatio = (float)icon.getIconWidth() / dim.width;
            int h, w;
            if (hRatio > wRatio) {
                h = dim.height;
                w = (int)(icon.getIconWidth() / hRatio);
            } else {
                w = dim.width;
                h = (int)(icon.getIconHeight() / wRatio);
            }
            img = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            return icon;
        }
    }

    private File[] listImageFiles(File dir, String[] suffix) {
        ImageFileFilter filter = new ImageFileFilter(suffix);
        return dir.listFiles(filter);
    }

    private class ImageFileFilter implements FilenameFilter {

        private final String[] suffix;

        public ImageFileFilter(String[] suffix) {
            this.suffix = suffix;
        }

        @Override
        public boolean accept(File dir, String name) {

            boolean accept = false;
            for (int i = 0; i < suffix.length; i++) {
                if (name.toLowerCase().endsWith(suffix[i])) {
                    accept = true;
                    break;
                }
            }
            return accept;
        }
    }

    private class ImageRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = -7952145522385412194L;

        public ImageRenderer() {
            initComponent();
        }

        private void initComponent() {
            setVerticalTextPosition(JLabel.BOTTOM);
            setHorizontalTextPosition(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {

            Component compo = super.getTableCellRendererComponent(table,
                    value,
                    isSelected,
                    isFocused,
                    row, col);

            JLabel l = (JLabel)compo;

            l.setBackground(new Color(254,255,255)); // なぜか WHITE は無視される => quaqua のせい
            if (isSelected) { l.setBorder(selectedBorder); }
            else { l.setBorder(normalBorder); }

            if (value != null) {
                ImageEntry entry = (ImageEntry)value;
                l.setIcon(entry.getImageIcon());
                l.setText(null);

            } else {
                l.setIcon(null);
                l.setText(null);
            }
            return compo;
        }
    }

    /**
     * TransferHandler by pns
     */
    private class ImageTransferHandler extends PatchedTransferHandler {
        private static final long serialVersionUID = 1L;

        private JComponent draggedComp = null;

        @Override
        protected Transferable createTransferable(JComponent c) {
            int row = imageTable.getSelectedRow();
            int col = imageTable.getSelectedColumn();

            ImageEntry entry = null;
            if (row != -1 && col != -1) {
                entry = (ImageEntry)imageTable.getValueAt(row, col);
            }
            return new ImageEntryTransferable(entry);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        /**
         * 半透明 drag のために dragged component とマウス位置を保存する
         * @param comp
         * @param e
         * @param action
         */
        @Override
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            JTable table = (JTable) comp;
            int row = table.getSelectedRow();
            int column = table.getSelectedColumn();
            TableCellRenderer r = table.getCellRenderer(row, column);

            Object value = table.getValueAt(row, column);
            boolean isSelected = false;
            boolean hasFocus = true;

            draggedComp = (JComponent) r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            draggedComp.setSize(table.getColumnModel().getColumn(0).getWidth(), table.getRowHeight(row));

            // calculate MousePosition
            Rectangle cellBounds = table.getCellRect(row, column, true);
            mousePosition = table.getMousePosition();
            if (mousePosition != null) {
                mousePosition.x -= cellBounds.x;
                mousePosition.y -= cellBounds.y;
            }

            super.exportAsDrag(comp, e, action);
        }

        /**
         * 半透明のフィードバックを返す
         * @param t
         * @return
         */
        @Override
        public Icon getVisualRepresentation(Transferable t) {
            if (draggedComp == null) { return null; }

            int width = draggedComp.getWidth();
            int height = draggedComp.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics g = image.getGraphics();
            draggedComp.paint(g);
            MyBorder.drawSelectedRect(draggedComp, g, 0, 0, width-1, height-1);
            return new ImageIcon(image);
        }
    }
}
