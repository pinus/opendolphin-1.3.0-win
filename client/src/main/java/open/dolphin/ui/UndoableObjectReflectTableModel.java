package open.dolphin.ui;

import open.dolphin.helper.PNSTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.util.List;

/**
 * Undo 可能な ObjectReflectTableModel.
 *
 * @param <T> Class of item
 * @author pns
 */
public class UndoableObjectReflectTableModel<T> extends ObjectReflectTableModel<T> {
    Logger logger = LoggerFactory.getLogger(UndoableObjectReflectTableModel.class);

    // active row
    private int editingRow = -1;

    // UndoManager
    private UndoManager undoManager = new UndoManager();
    private CompoundEdit current = new CompoundEdit();
    private Timer timer = new Timer(30, e -> undoFlush());

    public UndoableObjectReflectTableModel(List<PNSTriple<String, Class<?>, String>> reflectionList) {
        super(reflectionList);
    }

    /**
     * Undo 情報を保存する setValueAt. Undo 情報を保存するだけ.
     *
     * @param value to set
     * @param row row
     * @param col column
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        Object oldValue = getValueAt(row, col);
        current.addEdit(new SetValueAtEdit(oldValue, value, row, col));
        current.end();
        undoFlush();
        super.setValueAt(value, row, col); // empty
    }

    /**
     * setValueAt を undo する. SetValueAtEdit から呼ばれる.
     * これ自体は何もしないので override して使う.
     *
     * @param value value to set
     * @param row row
     * @param col column
     */
    public void undoSetValueAt(Object value, int row, int col) {
        super.setValueAt(value, row, col); // empty
    }

    /**
     * Undo 可能な addRow.
     *
     * @param item item to add
     */
    public void undoableAddRow(T item) {
        undoableInsertRow(getRowCount(), item);
    }

    /**
     * Undo 可能な addRow.
     *
     * @param row row to add at
     * @param item item to add
     */
    public void undoableInsertRow(int row, T item) {
        editingRow = row;
        undoableEditHappened(new InsertEdit(row, item));
        insertRow(row, item);
    }

    /**
     * Undo 可能な deleteRow.
     *
     * @param row row to delete
     */
    public void undoableDeleteRow(int row) {
        editingRow = row;
        undoableEditHappened(new DeleteEdit(row));
        deleteRow(row);
    }

    /**
     * Timer で呼ばれて UndoManager に UndoableEdit を登録する.
     */
    private void undoFlush() {
        timer.stop();
        current.end();
        undoManager.addEdit(current);
        current = new CompoundEdit();
    }

    public void undoableEditHappened(UndoableEdit edit) {
        timer.restart();
        current.addEdit(edit);
    }

    /**
     * setValueAt の UndoableEdit.
     */
    private class SetValueAtEdit extends AbstractUndoableEdit {
        Object oldValue, newValue;
        int row, col;
        public SetValueAtEdit(Object oldVal, Object newVal, int r, int c) {
            oldValue = oldVal; newValue = newVal; row = r; col = c;
        }
        @Override
        public void undo() {
            editingRow = row;
            undoSetValueAt(oldValue, row, col);
        }
        @Override
        public void redo() {
            editingRow = row;
            undoSetValueAt(newValue, row, col);
        }
    }

    /**
     * Insert の UndoableEdit.
     */
    private class InsertEdit extends AbstractUndoableEdit {
        private int row;
        private T item;

        public InsertEdit(int r, T i) {
            row = r; item = i;
        }
        @Override
        public void undo() {
            editingRow = row;
            deleteRow(row);
        }
        @Override
        public void redo() {
            editingRow = row;
            insertRow(row, item);
        }
    }

    /**
     * Delete の UndoableEdit
     */
    private class DeleteEdit extends AbstractUndoableEdit {
        private int row;
        private T item;

        public DeleteEdit(int r) {
            row = r; item = getObject(r);
        }
        @Override
        public void undo() {
            editingRow = row;
            insertRow(row, item);
        }
        @Override
        public void redo() {
            editingRow = row;
            deleteRow(row);
        }
    }

    /**
     * Editing row を返す.
     *
     * @return active row
     */
    public int editingRow() { return editingRow; }

    /**
     * Undo.
     */
    public void undo() {
        if (canUndo()) { undoManager.undo(); }
    }

    /**
     * Redo.
     */
    public void redo() {
        if (canRedo()) { undoManager.redo(); }
    }

    public boolean canUndo() {
        return undoManager.canUndo();
    }

    public boolean canRedo() {
        return undoManager.canRedo();
    }

    /**
     * Undo 情報の破棄.
     * If timer is running,
     * stop timer, discard current, and then discardAllEdits.
     */
    public void discardAllUndoableEdits() {
        if (timer.isRunning()) {
            timer.stop();
            current = new CompoundEdit();
        }
        undoManager.discardAllEdits();
    }
}
