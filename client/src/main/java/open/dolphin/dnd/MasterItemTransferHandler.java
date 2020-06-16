package open.dolphin.dnd;

import open.dolphin.order.MasterItem;
import open.dolphin.ui.ObjectReflectTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * MasterItemTransferHandler.
 *
 * @author Minagawa, Kazushi
 * @author pns
 */
public class MasterItemTransferHandler extends DolphinTransferHandler {
    private static final long serialVersionUID = 4871088750931696219L;
    private Logger logger = LoggerFactory.getLogger(MasterItemTransferHandler.class);

    private JTable sourceTable;
    private boolean shouldRemove;
    private int fromIndex;
    private int toIndex;

    @Override
    protected Transferable createTransferable(JComponent c) {
        sourceTable = (JTable) c;
        ObjectReflectTableModel tableModel = (ObjectReflectTableModel) sourceTable.getModel();
        fromIndex = sourceTable.getSelectedRow();
        MasterItem dragItem = (MasterItem) tableModel.getObject(fromIndex);
        return dragItem != null ? new MasterItemTransferable(dragItem) : null;
    }

    @Override
    public int getSourceActions(JComponent c) {
        // 半透明の drag image フィードバックを作る
        JTable table = (JTable) c;
        int row = table.getSelectedRow();
        int column = 1;
        TableCellRenderer r = table.getCellRenderer(row, column);

        Object value = table.getValueAt(row, column);
        boolean isSelected = false;
        boolean hasFocus = true;

        JLabel draggedComp = (JLabel) r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        draggedComp.setSize(table.getColumnModel().getColumn(column).getWidth(), table.getRowHeight(row));

        // クリッピングあり
        setDragImage(draggedComp, true);

        return COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) { return false; }

        try {
            MasterItem dropItem =
                (MasterItem) support.getTransferable().getTransferData(DolphinDataFlavor.masterItemFlavor);
            JTable dropTable = (JTable) support.getComponent();
            ObjectReflectTableModel<MasterItem> tableModel = (ObjectReflectTableModel<MasterItem>) dropTable.getModel();
            JTable.DropLocation dropLocation = (JTable.DropLocation) support.getDropLocation();

            toIndex = dropLocation.getRow();
            shouldRemove = (dropTable == sourceTable);

            if (shouldRemove) {
                tableModel.moveRow(fromIndex, (toIndex > fromIndex) ? --toIndex : toIndex);
            } else {
                tableModel.addRow(toIndex, dropItem);
            }
            sourceTable.getSelectionModel().setSelectionInterval(toIndex, toIndex);
            return true;

        } catch (IOException | UnsupportedFlavorException ex) {
            logger.error(ex.getMessage());
        }
        return false;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        JTable dropTable = (JTable) support.getComponent();
        ObjectReflectTableModel tableModel = (ObjectReflectTableModel) dropTable.getModel();

        return Objects.nonNull(tableModel.getObject(dropTable.getSelectedRow()))
            && Stream.of(support.getDataFlavors()).anyMatch(DolphinDataFlavor.masterItemFlavor::equals);
    }
}
