package open.dolphin.laf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import open.dolphin.ui.PNSTreeCellEditor;

/**
 * ストライプな TreeUI.
 * @author pns
 */
public class MyTreeUI extends BasicTreeUI {

    private UIHelper helper;

    public static ComponentUI createUI(JComponent c) {
        return new MyTreeUI();
    }

    @Override
    protected void installDefaults() {
        UIManager.put("Tree.selectionBackground", UIHelper.DEFAULT_BACKGROUND_SELECTION_FOCUSED);
        UIManager.put("Tree.paintLines", Boolean.FALSE);
        UIManager.put("Tree.rendererFillBackground", Boolean.FALSE);
        UIManager.put("Tree.drawsFocusBorderAroundIcon", Boolean.FALSE);
        UIManager.put("Tree.drawDashedFocusIndicator", Boolean.FALSE);
        UIManager.put("Tree.repaintWholeRow", Boolean.TRUE);
        UIManager.put("Tree.font", new Font(Font.SANS_SERIF, Font.PLAIN, UIHelper.isWin()? 12:13));

        super.installDefaults();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        JTree t = (JTree) c;
        helper = new UIHelper(c);

        t.putClientProperty("Quaqua.Tree.style", "striped");
        t.setRowHeight(UIHelper.DEFAULT_ROW_HEIGHT);
        t.setShowsRootHandles(true);
    }

    /**
     * Background を塗る.
     * @param g
     * @param c
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        Object property = tree.getClientProperty("Quaqua.Tree.style");
        boolean isStriped = property != null && property.equals("striped");
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        renderer.setBorderSelectionColor(null);
        helper.setRendererColors(renderer);

        if (isStriped) {
            Rectangle clip = g.getClipBounds();
            int[] top = getTopY(clip.y);
            // １行前から開始
            int topY = top[0]-tree.getRowHeight();
            int currentRow = top[1]-1;

            while (topY < clip.y + clip.height) {
                int bottomY = topY + tree.getRowHeight();
                int row = tree.getRowForLocation(clip.x + clip.width-1, topY);

                boolean isSelected = tree.isRowSelected(row);
                boolean isFocused = tree.isFocusOwner();

                helper.setBackgroundNonSelectionColor(UIHelper.ROW_COLORS[currentRow & 1]);
                g.setColor(helper.getBackground(isSelected, isFocused));

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
     * Text の色を renderer に設定する.
     * @param g
     * @param clip
     * @param insets
     * @param bounds
     * @param path
     * @param row
     * @param isExpanded
     * @param hasBeenExpanded
     * @param isLeaf
     */
    @Override
    protected void paintRow(Graphics g, Rectangle clip,
                            Insets insets, Rectangle bounds, TreePath path,
                            int row, boolean isExpanded,
                            boolean hasBeenExpanded, boolean isLeaf) {

        boolean isFocused = tree.isFocusOwner();
        Color col = helper.getForeground(true, isFocused);
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        Color orig = renderer.getTextSelectionColor();

        // Focus に応じて foreground 色も変える
        renderer.setTextSelectionColor(col);
        super.paintRow(g, bounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
        // 戻す
        renderer.setTextSelectionColor(orig);
    }

    /**
     * PathBounds を行全体に広げる.
     * 行のどこを click しても選択できるようになる.
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

    /**
     * BasicTreeUI#getPathBounds もこのパターンで public と private が組み合わされている.
     * @param path
     * @param insets
     * @param bounds
     * @return
     */
    private Rectangle getPathBounds(TreePath path, Insets insets, Rectangle bounds) {
        bounds = treeState.getBounds(path, bounds);

        if(bounds != null) {
            bounds.width = tree.getWidth() - bounds.x - insets.left - insets.right;
            bounds.y += insets.top;
        }
        return bounds;
    }

    /**
     * Drag 中は CellEdit しない.
     * 1クリック後にドラッグを開始すると，CellEditor のタイマーは止まらないので，
     * 1200 msec 後に CellEditor がスタートしてしまうのの対策.
     * @param tree
     * @param path
     */
    @Override
    public void startEditingAtPath(JTree tree, TreePath path) {
        if (!helper.isDragging()) { super.startEditingAtPath(tree, path); }
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

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                    boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                l.setToolTipText(l.getText());
                return l;
            }
        };

        DefaultTreeCellEditor editor = new PNSTreeCellEditor(tree, renderer);

        tree.setCellRenderer(renderer);
        tree.setCellEditor(editor);
        tree.setEditable(true);

        tree.setDragEnabled(true);

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        f.add(tree, BorderLayout.CENTER);
        f.pack();
        f.setLocation(300, 100);
        f.setVisible(true);
    }
}
