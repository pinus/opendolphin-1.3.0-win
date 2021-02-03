package open.dolphin.helper;

import open.dolphin.client.ClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.regex.Pattern;

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
    private static Pattern ALPHABET = Pattern.compile("^[a-z,A-Z]*$");

    private Action undoAction;
    private Action redoAction;

    // 連続する UndoableEditEvent をまとめる CompoundEdit
    private TextComponentUndoableEdit current;

    // delay msec 以内に起きた UndoableEditEvent はまとめて1つにする
    private final Timer timer;
    private final int delay = 30;

    public TextComponentUndoManager(JTextComponent c) {
        timer = new Timer(delay, e -> flush());
        current = new TextComponentUndoableEdit();

        // ATOK 関連
        if (ClientContext.isMac()) {
            AtokListener atokListener = new AtokListener(c, this);
            c.addKeyListener(atokListener);
            c.addInputMethodListener(atokListener);
        }
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        timer.restart();
        current.addEdit(e.getEdit());
        updateActionStatus(); // 文字入力毎に action が enable/disable される
    }

    /**
     * current にまとめた UndoableEdit を addEdit する. Timer から呼ばれる.
     */
    public void flush() {
        timer.stop();
        current.end();
        addEdit(current);

        current = new TextComponentUndoableEdit();
        updateActionStatus();
    }

    /**
     * Word 単位で undo をまとめる addEdit.
     *
     * @param e UndoableEdit to add
     * @return return value of addEdit
     */
    @Override
    public boolean addEdit(UndoableEdit e) {
        if (toMergeEdit(e, lastEdit())) {
            TextComponentUndoableEdit last = (TextComponentUndoableEdit) lastEdit();
            TextComponentUndoableEdit toMerge = (TextComponentUndoableEdit) e;
            return last.mergeEdit(toMerge.lastEdit());

        } else {
            return super.addEdit(e);
        }
    }

    /**
     * 指定した UndoableEdit を前の UndoableEdit に merge するかどうか.
     *
     * @param cur TextComponentUndoableEdit which contains current DocumentEvent
     * @param last TextComponentUndoableEdit which contains last DocumentEvent
     * @return true to merge
     */
    private boolean toMergeEdit(UndoableEdit cur, UndoableEdit last) {
        // last が canUndo でなければ merge しない
        if (last == null || !last.canUndo()) { return false; }

        // 両方とも TextComponentUndoableEdit でなければ merge しない
        if (!(cur instanceof TextComponentUndoableEdit)
            || !(last instanceof TextComponentUndoableEdit)) { return false; }

        // cur size > 1 なら merge しない (CodeHelper 入力で cur size > 1 になる)
        if (((TextComponentUndoableEdit)cur).size() > 1) { return false; }

        UndoableEdit curEdit = ((TextComponentUndoableEdit) cur).lastEdit();
        UndoableEdit lastEdit = ((TextComponentUndoableEdit) last).lastEdit();

        // 内容が　DefaultDocumentEvent でなければ merge しない
        if (!(curEdit instanceof AbstractDocument.DefaultDocumentEvent)
            || !(lastEdit instanceof AbstractDocument.DefaultDocumentEvent)) { return false; }

        AbstractDocument.DefaultDocumentEvent curEvent = (AbstractDocument.DefaultDocumentEvent) curEdit;
        AbstractDocument.DefaultDocumentEvent lastEvent = (AbstractDocument.DefaultDocumentEvent) lastEdit;

        try {
            if (curEvent.getType() == DocumentEvent.EventType.REMOVE) {
                // REMOVE が続いている場合はまとめる
                if (lastEvent.getType() == DocumentEvent.EventType.REMOVE) { return true; }

            } else {
                if (lastEvent.getType() == DocumentEvent.EventType.INSERT) {
                    // Alphabet 入力が続いていたらまとめる
                    String curText = curEvent.getDocument().getText(curEvent.getOffset(), curEvent.getLength());
                    String lastText = lastEvent.getDocument().getText(lastEvent.getOffset(), lastEvent.getLength());
                    if (ALPHABET.matcher(curText).matches() && ALPHABET.matcher(lastText).matches()) { return true; }
                }
            }
        } catch (BadLocationException ex) {
            logger.error(ex.getMessage());
        }
        return false;
    }

    /**
     * If timer is running,
     * stop timer, discard current, and then discardAllEdits.
     */
    @Override
    public void discardAllEdits() {
        if (timer.isRunning()) {
            timer.stop();
            current = new TextComponentUndoableEdit();
        }
        super.discardAllEdits();
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
     * TextComponentUndoManager 用の UndoableEdit.
     */
    private class TextComponentUndoableEdit extends CompoundEdit {
        @Override
        public UndoableEdit lastEdit() {
            return super.lastEdit();
        }

        public int size() {
            return super.edits.size();
        }

        /**
         * この UndoableEdit が保持する DocumentEvent に edit を merge する.
         *
         * @param edit DocumentEvent
         * @return addEdit した戻り値
         */
        public boolean mergeEdit(UndoableEdit edit) {
            setInProgress(true);
            boolean ret = addEdit(edit);
            end();
            return ret;
        }

        /**
         * package-private field の inProgress を書き換える.
         *
         * @param inProgress inProgress to set
         */
        public void setInProgress(boolean inProgress) {
            try {
                Field field = getClass().getSuperclass().getDeclaredField("inProgress");
                field.setAccessible(true);
                field.set(this, inProgress);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
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
