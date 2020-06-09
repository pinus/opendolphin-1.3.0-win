package open.dolphin.dnd;

import open.dolphin.client.ImageEntry;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.datatransfer.Transferable;

public class ImageEntryTransferHandler extends DolphinTransferHandler {
        private static final long serialVersionUID = 1L;

        @Override
        protected Transferable createTransferable(JComponent c) {
            JTable imageTable = (JTable) c;
            int row = imageTable.getSelectedRow();
            int col = imageTable.getSelectedColumn();

            ImageEntry entry = null;
            if (row != -1 && col != -1) {
                entry = (ImageEntry) imageTable.getValueAt(row, col);
            }
            return new ImageEntryTransferable(entry);
        }

        @Override
        public int getSourceActions(JComponent c) {
            JTable table = (JTable) c;
            int row = table.getSelectedRow();
            int column = table.getSelectedColumn();
            TableCellRenderer r = table.getCellRenderer(row, column);

            Object value = table.getValueAt(row, column);
            boolean isSelected = false;
            boolean hasFocus = true;

            JLabel draggedComp = (JLabel) r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            draggedComp.setSize(table.getColumnModel().getColumn(0).getWidth(), table.getRowHeight(row));

            setDragImage(draggedComp);

            return COPY_OR_MOVE;
        }

        // TODO: 未実装
        @Override
        public boolean canImport(TransferSupport support) { return false; }

        public boolean importData(TransferSupport support) { return false; }
    }
