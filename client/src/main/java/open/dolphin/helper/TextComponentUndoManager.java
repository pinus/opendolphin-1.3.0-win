package open.dolphin.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import java.awt.event.*;
import java.util.Objects;

/**
 * JTextComponent に Undo 機能を付ける.<br>
 * 使用例:
 * <pre>{@code
 * JTextPane c = new JTextPane();
 * c.getDocument().addUndoableEditListener(TextComponentUndoManager.createManager(c));
 * }</pre>
 *
 * @author pns
 */
public class TextComponentUndoManager extends UndoManager {
    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.getLogger(TextComponentUndoManager.class);

    private Action undoAction;
    private Action redoAction;

    // 連続する UndoableEditEvent をまとめる CompoundEdit
    private CompoundEdit current;

    // delay msec 以内に起きた UndoableEditEvent はまとめて1つにする
    private final Timer timer;
    private final int delay = 100;

    public TextComponentUndoManager(JTextComponent c) {
        timer = new Timer(delay, e -> flush());
        current = new CompoundEdit();

        // ATOK 関連
        AtokListener atokListener = new AtokListener(c, this);
        c.addKeyListener(atokListener);
        c.addInputMethodListener(atokListener);
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        timer.restart();
        current.addEdit(e.getEdit());
        updateActionStatus(); // 文字入力毎に action が enable/disable される
    }

    /**
     * まとめた UndoableEdit を addEdit する. Timer から呼ばれる.
     */
    public void flush() {
        timer.stop();
        current.end();
        addEdit(current);

        current = new CompoundEdit();
        updateActionStatus();
    }

    @Override
    public void undo() {
        if (canUndo()) { super.undo(); }
        updateActionStatus();
    }

    @Override
    public void redo() {
        if (canRedo()) { super.redo(); }
        updateActionStatus();
    }

    private void updateActionStatus() {
        if (Objects.isNull(undoAction) || Objects.isNull(redoAction)) { return; }
        undoAction.setEnabled(canUndo());
        redoAction.setEnabled(canRedo());
    }

    /**
     * enable/disable を update するために action を登録する.
     *
     * @param undo undo action
     * @param redo redo action
     */
    public void setActions(Action undo, Action redo) {
        undoAction = undo;
        redoAction = redo;
    }

    /**
     * JTextComponent に Undo 機能を付ける utility static method.
     *
     * @param c JTextComponent
     * @return TextComponentUndoManager
     */
    public static TextComponentUndoManager createManager(JTextComponent c) {
        TextComponentUndoManager manager = new TextComponentUndoManager(c);

        // default undo/redo action
        Action undo = new AbstractAction("undo") {
            @Override
            public void actionPerformed(ActionEvent e) { manager.undo(); }
        };
        Action redo = new AbstractAction("redo") {
            @Override
            public void actionPerformed(ActionEvent e) { manager.redo(); }
        };
        manager.setActions(undo, redo);

        // short cut を付ける
        ActionMap am = c.getActionMap();
        InputMap im = c.getInputMap();
        am.put("undo", undo);
        im.put(KeyStroke.getKeyStroke("meta Z"), "undo");
        am.put("redo", redo);
        im.put(KeyStroke.getKeyStroke("shift meta Z"), "redo");

        return manager;
    }
}
