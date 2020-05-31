package open.dolphin.client;

import open.dolphin.helper.MouseHelper;
import open.dolphin.ui.Focuser;

import javax.swing.FocusManager;
import javax.swing.*;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.*;

/**
 * ComponentHolder.
 * StampHolder と SchemaHolder.
 *
 * @author Kazushi Minagawa
 * @author pns
 */
public abstract class AbstractComponentHolder extends JLabel
    implements ComponentHolder<JLabel>, MouseListener, MouseMotionListener, KeyListener {
    private static final long serialVersionUID = 1L;

    // 親の KartePane
    private KartePane kartePane;

    // TextPane内での開始と終了ポジション
    private Position start;
    private Position end;

    // エディタの二重起動を防ぐためのフラグ
    private boolean isEditable = true;

    public AbstractComponentHolder(KartePane kartePane) {
        this.kartePane = kartePane;
        initialize();
    }

    private void initialize() {
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        ActionMap am = this.getActionMap();
        am.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        am.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        am.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean b) {
        isEditable = b;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);

        if (KeyStroke.getKeyStroke("TAB").equals(key)) {
            // TAB キーでフォーカス次移動
            SwingUtilities.invokeLater(FocusManager.getCurrentManager()::focusNextComponent);

        } else if (KeyStroke.getKeyStroke("shift TAB").equals(key)) {
            // shift TAB キーでフォーカス前移動
            SwingUtilities.invokeLater(FocusManager.getCurrentManager()::focusPreviousComponent);

        } else if (KeyStroke.getKeyStroke("SPACE").equals(key)) {
            // SPACE で編集
            edit();

        } else if (KeyStroke.getKeyStroke("UP").equals(key)
            || KeyStroke.getKeyStroke("LEFT").equals(key)) {
            // JTextPane position １つ前に戻る
            int pos = getStartPos();
            if (pos != 0) {
                Focuser.requestFocus(kartePane.getTextPane());
                kartePane.getTextPane().setCaretPosition(pos - 1);
            }

        } else if (KeyStroke.getKeyStroke("DOWN").equals(key)
            || KeyStroke.getKeyStroke("RIGHT").equals(key)) {
            // JTextPane position １つ後ろに行く
            int pos = getStartPos();
            if (pos != kartePane.getDocument().getLength()) {
                Focuser.requestFocus(kartePane.getTextPane());
                kartePane.getTextPane().setCaretPosition(pos + 1);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) {
        Focuser.requestFocus(this);
        // 右クリックで popup 表示
        if (e.isPopupTrigger()) {
            maybeShowPopup(e);
        }
        // ダブルクリックでエディタ表示
        else if (e.getClickCount() == 2 && !MouseHelper.mouseMoved() && !e.isAltDown()) {
            edit();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //windows
        if (e.isPopupTrigger() && e.getClickCount() != 2) {
            maybeShowPopup(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // ドラッグの際にも，スタンプを selected 状態にする
        Focuser.requestFocus(this);

        int ctrlMask = InputEvent.CTRL_DOWN_MASK;
        int optionMask = InputEvent.ALT_DOWN_MASK;
        int action = ((e.getModifiersEx() & (ctrlMask | optionMask)) != 0) ?
                TransferHandler.COPY : TransferHandler.MOVE;

        JComponent c = (JComponent) e.getSource();
        TransferHandler handler = c.getTransferHandler();
        handler.exportAsDrag(c, e, action);
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mouseMoved(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void setEntry(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int getStartPos() { return start.getOffset(); }

    @Override
    public int getEndPos() { return end.getOffset(); }

    @Override
    public abstract void edit();

    public abstract void maybeShowPopup(MouseEvent e);
}
