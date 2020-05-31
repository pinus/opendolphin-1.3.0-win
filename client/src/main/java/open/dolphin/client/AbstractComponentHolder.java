package open.dolphin.client;

import open.dolphin.event.ProxyAction;
import open.dolphin.helper.MouseHelper;
import open.dolphin.ui.Focuser;

import javax.swing.FocusManager;
import javax.swing.*;
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

    /**
     * エディタの二重起動を防ぐためのフラグ
     */
    private boolean isEditable = true;

    public AbstractComponentHolder() {
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
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) {
        // requestFocus はここの方がいい. mouseClicked だと，mouseRelease されるまで focus とれないから
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
    public abstract void edit();

    public abstract void maybeShowPopup(MouseEvent e);
}
