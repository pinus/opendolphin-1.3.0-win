package open.dolphin.laf;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * ストライプなテーブルUI.
 *
 * @autho pns
 */
public class MyTableUI extends BasicTableUI {

    public static ComponentUI createUI(JComponent c) {
        return new MyTableUI();
    }

    @Override
    protected void installDefaults() {
        UIManager.put("Table.selectionBackground", UIHelper.DEFAULT_BACKGROUND_SELECTION_FOCUSED);

        super.installDefaults();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        JTable t = (JTable) c;
        t.setDefaultRenderer(Object.class, new Renderer());

        // データのないところもストライプで埋める
        t.setFillsViewportHeight(true);

        // データのないところをクリックしたら選択を外す
        t.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = t.rowAtPoint(e.getPoint());
                if (row == -1) { t.clearSelection(); }
            }
        });

        //キー入力によるセル編集開始を禁止する
        t.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setRowHeight(UIHelper.DEFAULT_ROW_HEIGHT);

        // off focus のバックグランドを client property にセット
        t.putClientProperty("JTable.backgroundOffFocus", UIHelper.DEFAULT_BACKGROUND_SELECTION_OFF_FOCUS);

        // focus が変わったら selected row 全体を描き直す
        t.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { fireRowChanged(); }
            @Override
            public void focusLost(FocusEvent e) { fireRowChanged(); }

            private void fireRowChanged() {
                t.repaint();
                //int row = t.getSelectedRow();
                //for (int col=0; col<t.getColumnCount(); col++) {
                //    t.repaint(t.getCellRect(row, col, true));
                //}
            }
        });
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Object property = table.getClientProperty("Quaqua.Table.style");
        boolean isStriped = property != null && property.equals("striped");

        if (isStriped) {
            Rectangle clip = g.getClipBounds();

            int[] top = getTopY(clip.y);
            // １行前から開始
            int topY = top[0]-table.getRowHeight();
            int currentRow = top[1]-1;

            while (topY < clip.y + clip.height) {
                int bottomY = topY + table.getRowHeight();
                g.setColor(UIHelper.ROW_COLORS[currentRow & 1]);
                g.fillRect(clip.x, topY, clip.width, bottomY);
                topY = bottomY;
                currentRow++;
            }
        }
        super.paint(g, c);
    }

    /**
     * ClipBound.y を越えた初めての Cell の Y 座標とその行数.
     *
     * @param clipY
     * @return
     */
    private int[] getTopY(int clipY) {

        if (table.getRowCount() > 0) {
            int rowHeight = table.getRowHeight();
            int row = 0;
            int ｙ = table.getCellRect(0, 0, true).y;

            while (ｙ < clipY) {
                ｙ += rowHeight;
                row++;
            }
            return new int[]{ｙ, row};

        } else {
            return new int[]{0, 0};
        }
    }

    /**
     * Focus に応じてバックグランド色を調節する Renderer.
     */
    private class Renderer extends DefaultTableCellRenderer.UIResource {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                              boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                if (! table.isFocusOwner()) {
                    c.setForeground(table.getForeground());
                    c.setBackground(UIHelper.DEFAULT_BACKGROUND_SELECTION_OFF_FOCUS);
                }
            } else {
                c.setBackground(null);
            }

            return c;
        }
    }
}
