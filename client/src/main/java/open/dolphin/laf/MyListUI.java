package open.dolphin.laf;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicListUI;

/**
 * ストライプな ListUI.
 *
 * @author pns
 */
public class MyListUI extends BasicListUI {

    public static ComponentUI createUI(JComponent list) {
        return new MyListUI();
    }

    @Override
    protected void installDefaults() {
        UIManager.put("List.selectionBackground", UIHelper.DEFAULT_BACKGROUND_SELECTION_FOCUSED);

        super.installDefaults();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);

        JList l = (JList) c;
        // off focus のバックグランドを client property にセット
        l.putClientProperty("JList.backgroundOffFocus", UIHelper.DEFAULT_BACKGROUND_SELECTION_OFF_FOCUS);

        l.setFixedCellHeight(UIHelper.DEFAULT_ROW_HEIGHT);
        //l.setCellRenderer(new Renderer());
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Object property = list.getClientProperty("Quaqua.List.style");
        boolean isStriped = property != null && property.equals("striped");

        if (isStriped) {
            Rectangle clip = g.getClipBounds();
            int[] top = getTopY(clip.y);
            // １行前から開始
            int currentRow = top[1]-1;
            int topY = top[0]-getCellHeight(currentRow);

            while (topY < clip.y + clip.height) {
                int bottomY = topY + getCellHeight(currentRow);
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

        int listSize = list.getModel().getSize();

        if (listSize != 0) {
            int row = 0;
            int y = list.getCellBounds(row, row).y;

            while (y < clipY) {
                y += getCellHeight(row);
                row++;
            }
            return new int[]{y, row};

        } else {
            return new int[]{0, 0};
        }
    }

    /**
     * row の高さを得る.
     *
     * @param row
     * @return
     */
    private int getCellHeight(int row) {
        int fixed = list.getFixedCellHeight();
        if (fixed > 0) {
            return fixed;
        }

        int listSize = list.getModel().getSize();
        if (listSize == 0) {
            // 空のリスト
            return UIHelper.DEFAULT_ROW_HEIGHT;

        } else {
            Rectangle bounds = row < listSize?
                    list.getCellBounds(row, row) :
                    list.getCellBounds(listSize -1, listSize -1);

            return bounds == null? UIHelper.DEFAULT_ROW_HEIGHT : bounds.height;
        }
    }

    /**
     * Focus に応じてバックグランド色を調節する Renderer.
     */
    private class Renderer extends DefaultListCellRenderer.UIResource {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (isSelected) {
                if (! c.isFocusOwner()) {
                    c.setForeground(c.getForeground());
                    c.setBackground(UIHelper.DEFAULT_BACKGROUND_SELECTION_OFF_FOCUS);
                }
            } else {
                c.setBackground(null);
            }

            return c;
        }
    }
}
