package open.dolphin.ui;

import open.dolphin.helper.TextComponentUndoManager;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Mac っぽい Border の TreeCellEditor.
 * undo 対応.
 *
 * @author pns
 */
public final class PNSTreeCellEditor extends DefaultTreeCellEditor {

    // 編集が始まるまでの delay
    private static final int DELAY = 900; //in msec (default = 1200)
    // UndoManager
    private TextComponentUndoManager undoManager;

    public PNSTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
        realEditor = this.createTreeCellEditor();
    }

    @Override
    protected TreeCellEditor createTreeCellEditor() {
        final DefaultTextField textField = new DefaultTextField(null) {
            private static final long serialVersionUID = 1L;

            @Override
            public void setText(String text) {
                super.setText(text);
                // restart undoing
                undoManager.discardAllEdits();
                // editor の cell を５文字分大きめに作る
                //setColumns(text.length() + 5);
            }
        };

        undoManager = TextComponentUndoManager.createManager(textField);
        textField.getDocument().addUndoableEditListener(undoManager);

        // 勝手に cut,copy,past の popup を作らせない
        textField.putClientProperty("Quaqua.TextComponent.showPopup", false);
        textField.setOpaque(false);
        textField.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        // focus を失ったら編集はやめる
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                stopCellEditing();
            }
        });

        DefaultCellEditor editor = new DefaultCellEditor(textField);

        // One click to edit.
        editor.setClickCountToStart(1);
        return editor;
    }

    /**
     * 編集が始まるまでの delay を設定.
     */
    @Override
    protected void startEditingTimer() {
        if (timer == null) {
            timer = new Timer(DELAY, this);
            timer.setRepeats(false);
        }
        timer.start();
    }
}
