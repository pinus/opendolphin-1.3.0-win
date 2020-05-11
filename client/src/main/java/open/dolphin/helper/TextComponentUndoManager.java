package open.dolphin.helper;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * JTextComponent に Undo 機能を付ける.<br>
 * 使用例:
 * <pre>{@code
 * TextComponentUndoManager manager = new TextComponentUndoManager();
 * JTextPane textComponent = new JTextPane();
 * manager.addUndoActionTo(textComponent);
 * textComponent.getDocument().addUndoableEditListener(manager::listener);
 * }</pre>
 *
 * @author pns
 */
public class TextComponentUndoManager extends UndoManager {
    private static final long serialVersionUID = 1L;

    private Action undoAction;
    private Action redoAction;

    public TextComponentUndoManager() {
        // default undo action
        undoAction = new AbstractAction("undo") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        };
        // default redo action
        redoAction = new AbstractAction("redo") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        };
    }

    /**
     * JTextComponent に Undo 機能を付ける utility static method.
     *
     * @param c JTextComponent
     * @return TextComponentUndoManager
     */
    public static TextComponentUndoManager getManager(JTextComponent c) {
        TextComponentUndoManager manager = new TextComponentUndoManager();
        manager.addUndoActionTo(c);
        c.getDocument().addUndoableEditListener(manager::listener);
        return manager;
    }

    public void addUndoActionTo(JTextComponent c) {
        ActionMap am = c.getActionMap();
        InputMap im = c.getInputMap();
        am.put("undo", undoAction);
        im.put(KeyStroke.getKeyStroke("meta Z"), "undo");
        am.put("redo", redoAction);
        im.put(KeyStroke.getKeyStroke("shift meta Z"), "redo");
    }

    public void setUndoAction(Action action) {
        undoAction = action;
    }

    public void setRedoAction(Action action) {
        redoAction = action;
    }

    public void listener(UndoableEditEvent e) {
        addEdit(e.getEdit());
        updateActionStatus(); // 文字入力毎に action が enable/disable される
    }

    @Override
    public void undo() {
        if (canUndo()) {
            super.undo();
        }
        updateActionStatus();
    }

    @Override
    public void redo() {
        if (canRedo()) {
            super.redo();
        }
        updateActionStatus();
    }

    private void updateActionStatus() {
        undoAction.setEnabled(canUndo());
        redoAction.setEnabled(canRedo());
    }
}
