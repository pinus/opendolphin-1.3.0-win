package open.dolphin.laf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import open.dolphin.ui.PNSTreeCellEditor;

/**
 *
 * @author pns
 */
public class MyTreeUI extends BasicTreeUI {

    private static final Color DEFAULT_ODD_COLOR = Color.WHITE;
    private static final Color DEFAULT_EVEN_COLOR = new Color(237,243,254);
    private static final Color[] ROW_COLORS = {DEFAULT_EVEN_COLOR, DEFAULT_ODD_COLOR};
    private static final int DEFAULT_ROW_HEIGHT = 22;

    public static ComponentUI createUI(JComponent c) {
        return new MyTreeUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        JTree t = (JTree) c;
        t.putClientProperty("Quaqua.Tree.style", "striped");
        t.setRowHeight(DEFAULT_ROW_HEIGHT);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Object property = tree.getClientProperty("Quaqua.Tree.style");
        boolean isStriped = property != null && property.equals("striped");

        if (isStriped) {
            Rectangle clip = g.getClipBounds();
            int[] top = getTopY(clip.y);
            int topY = top[0];
            int currentRow = top[1]-1;

            // ClipBounds.y から topY まで塗る
            g.setColor(ROW_COLORS[currentRow & 1]);
            g.fillRect(clip.x, clip.y, clip.width, topY);
            currentRow ++;

            // 続きを塗る
            while (topY < clip.y + clip.height) {
                int bottomY = topY + tree.getRowHeight();

                if (tree.isRowSelected(currentRow)) {
                    // row selection
                    g.setColor(((DefaultTreeCellRenderer)tree.getCellRenderer()).getBackgroundSelectionColor());
                } else {
                    g.setColor(ROW_COLORS[currentRow & 1]);
                }
                g.fillRect(clip.x, topY, clip.width, bottomY);
                topY = bottomY;
                currentRow++;
            }
        }
        super.paint(g, c);
    }

    /**
     * ClipBound.y を越えた初めての Cell の Y 座標とその行数.
     * @param clipY
     * @return
     */
    private int[] getTopY(int clipY) {

        if (tree.getRowCount() > 0) {
            int rowHeight = tree.getRowHeight();
            int row = 0;
            TreePath path = tree.getPathForRow(0);
            int ｙ = tree.getPathBounds(path).y;
            while (ｙ < clipY) {
                ｙ += rowHeight;
                row++;
            }
            return new int[]{ ｙ, row };

        } else {
            return new int[]{ 0, 0 };
        }
    }

    /**
     * PathBounds を行全体に広げる.
     * @param tree
     * @param path
     * @return
     */
    @Override
    public Rectangle getPathBounds(JTree tree, TreePath path) {
        if(tree != null && treeState != null) {
            return getPathBounds(path, tree.getInsets(), new Rectangle());
        }
        return null;
    }

    private Rectangle getPathBounds(TreePath path, Insets insets, Rectangle bounds) {
        bounds = treeState.getBounds(path, bounds);
        if(bounds != null) {
            bounds.width = tree.getWidth();
            bounds.y += insets.top;
        }
        return bounds;
    }

    public static void main(String[] arg) {
        UIManager.put("TreeUI", MyTreeUI.class.getName());
        UIManager.put("TextFieldUI", MyTextFieldUI.class.getName());

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTree tree = new JTree();
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setPreferredSize(new Dimension(300,400));
        tree.setRootVisible(false);
        tree.setCellEditor(new PNSTreeCellEditor(tree, new DefaultTreeCellRenderer()));
        tree.setEditable(true);

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.addMouseMotionListener(new MouseAdapter(){
            @Override
            public void mouseDragged(MouseEvent e) {
                tree.getCellEditor().stopCellEditing(); // drag 開始したら、cell editor を止める
                tree.getTransferHandler().exportAsDrag((JComponent) e.getSource(), e, TransferHandler.COPY);
            }
        });

        f.add(tree, BorderLayout.CENTER);
        f.pack();
        f.setLocation(300, 100);
        f.setVisible(true);
    }
}
