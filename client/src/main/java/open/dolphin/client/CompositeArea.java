package open.dolphin.client;

import open.dolphin.helper.TextComponentUndoManager;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Objects;

/**
 * CompositeArea.
 * KarteComposite インターフェースを持つ JTextArea - Memo で使っている.
 *
 * @author kazm
 * @author pns
 */
public class CompositeArea extends JTextArea implements KarteComposite<JTextArea>, CaretListener {
    private static final long serialVersionUID = 1L;
    private TextComponentUndoManager undoManager;
    private boolean hasSelection;
    private ActionMap map;

    public CompositeArea(int row, int col) {
        super(row, col);
        initComponents();
    }

    private void initComponents() {
        putClientProperty("Quaqua.TextComponent.showPopup ", false);
        addCaretListener(this);
        undoManager = new TextComponentUndoManager(this);
        getDocument().addUndoableEditListener(undoManager);
    }

    public TextComponentUndoManager getUndoManager() {
        return undoManager;
    }

    @Override
    public void enter(ActionMap map) {
        this.map = map;
        map.get(GUIConst.ACTION_PASTE).setEnabled(canPaste());

        // undo / redo の enable/disabel を設定してもらう
        undoManager.setActions(map.get(GUIConst.ACTION_UNDO), map.get(GUIConst.ACTION_REDO));

        JPopupMenu menu = new JPopupMenu();
        menu.add(map.get(GUIConst.ACTION_COPY));
        menu.add(map.get(GUIConst.ACTION_CUT));
        menu.add(map.get(GUIConst.ACTION_PASTE));
        setComponentPopupMenu(menu);
    }

    @Override
    public void exit(ActionMap map) {
    }

    @Override
    public JTextArea getComponent() {
        return this;
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        if (Objects.isNull(map)) { return; }
        boolean newSelection = (e.getDot() != e.getMark());

        if (newSelection != hasSelection) {
            hasSelection = newSelection;
            map.get(GUIConst.ACTION_PASTE).setEnabled(canPaste());
            map.get(GUIConst.ACTION_CUT).setEnabled(hasSelection);
            map.get(GUIConst.ACTION_COPY).setEnabled(hasSelection);
        }
    }

    private boolean canPaste() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        return t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    /*
     * ChartMediator で addChain されて呼ばれる.
     */
    public void undo() {
        undoManager.undo();
    }

    public void redo() {
        undoManager.redo();
    }
}
