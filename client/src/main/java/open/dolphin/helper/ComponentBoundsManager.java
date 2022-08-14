package open.dolphin.helper;

import open.dolphin.client.ClientContext;
import open.dolphin.event.ProxyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * ComponentBoundsManager.
 * Component の位置・サイズ変更を Listen して，リアルタイムで preference ファイルに保存する.
 *
 * @author Kazushi Minagawa
 * @author pns
 */
public class ComponentBoundsManager extends WindowAdapter implements ComponentListener {
    private static final Logger logger = LoggerFactory.getLogger(ComponentBoundsManager.class);

    private final Window target;
    private final Point defaultLocation;
    private final Dimension defaultSize;
    private Preferences prefs;
    private String key;
    private Timer timer;
    private int delay = 500;
    private boolean resized, moved;

    /**
     * @param component - target component
     * @param location  - initial component location
     * @param size      - initial component size
     * @param object    - この object のクラス名が preference の key になる. null にすると記録されない.
     */
    public ComponentBoundsManager(Window component, Point location, Dimension size, Object object) {
        target = component;
        defaultLocation = location;
        defaultSize = size;
        if (object != null) {
            prefs = Preferences.userNodeForPackage(object.getClass());
            key = object.getClass().getName();
        }
        // 初期値
        component.setLocation(defaultLocation);
        component.setSize(defaultSize);
        // リスナ
        component.addComponentListener(this);
        component.addWindowListener(this);
        // Timer
        timer = new Timer(delay, new ProxyAction(this::flush));
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        if (Objects.isNull(prefs)) { return; }
        moved = true;
        timer.restart();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (Objects.isNull(prefs)) { return; }
        resized = true;
        timer.restart();
    }

    private void flush() {
        timer.stop();

        Rectangle r = target.getBounds();
        if (moved) {
            prefs.putInt(key + "_x", r.x);
            prefs.putInt(key + "_y", r.y);
        }
        if (resized) {
            prefs.putInt(key + "_width", r.width);
            prefs.putInt(key + "_height", r.height);
        }
        moved = false; resized = false;
        logger.debug(String.format("%s loc(%d,%d) size(%d,%d)", key, r.x, r.y, r.width, r.height));
    }

    @Override
    public void componentShown(ComponentEvent e) { }

    @Override
    public void componentHidden(ComponentEvent e) { }

    @Override
    public void windowClosed(WindowEvent e) {
        if (Objects.isNull(prefs)) { return; }

        Rectangle r = target.getBounds();
        prefs.putInt(key + "_x", r.x);
        prefs.putInt(key + "_y", r.y);
        prefs.putInt(key + "_width", r.width);
        prefs.putInt(key + "_height", r.height);
        logger.debug(String.format("%s loc(%d,%d) size(%d,%d)", key, r.x, r.y, r.width, r.height));
    }

    /**
     * Preferences に記録された Bounds に戻す.
     */
    public void revertToPreferenceBounds() {
        if (prefs != null) {
            int x = prefs.getInt(key + "_x", defaultLocation.x);
            int y = prefs.getInt(key + "_y", defaultLocation.y);
            int width = prefs.getInt(key + "_width", defaultSize.width);
            int height = prefs.getInt(key + "_height", defaultSize.height);
            target.setBounds(x, y, width, height);
        }
    }

    /**
     * target component を画面中央に設定する.
     */
    public void putCenter() {
        putCenter(ClientContext.isMac()? 3 : 2);
    }

    private void putCenter(int n) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = target.getSize();
        int x = (screenSize.width - size.width) / 2;
        int y = (screenSize.height - size.height) / n;
        target.setBounds(x, y, size.width, size.height);
    }
}
