package open.dolphin.helper;

import open.dolphin.client.ClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ComponentListener;
import java.util.prefs.Preferences;

/**
 * ComponentBoundsManager.
 * Component の位置・サイズ変更を Listen して，リアルタイムで preference ファイルに保存する.
 *
 * @author Kazushi Minagawa
 * @author pns
 */
public class ComponentBoundsManager implements ComponentListener {
    private static final Logger logger = LoggerFactory.getLogger(ComponentBoundsManager.class);

    private final Component target;
    private final Point defaultLocation;
    private final Dimension defaultSize;
    private Preferences prefs;
    private String key;

    /**
     * @param component - target component
     * @param location  - initial component location
     * @param size      - initial component size
     * @param object    - この object のクラス名が preference の key になる. null にすると記録されない.
     */
    public ComponentBoundsManager(Component component, Point location, Dimension size, Object object) {
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
    }

    @Override
    public void componentMoved(java.awt.event.ComponentEvent e) {
        Point loc = target.getLocation();
        if (prefs != null) {
            prefs.putInt(key + "_x", loc.x);
            prefs.putInt(key + "_y", loc.y);
        }
        logger.debug(String.format("%s loc=(%d,%d)", key, loc.x, loc.y));
    }

    @Override
    public void componentResized(java.awt.event.ComponentEvent e) {
        int width = target.getWidth();
        int height = target.getHeight();
        if (prefs != null) {
            prefs.putInt(key + "_width", width);
            prefs.putInt(key + "_height", height);
        }
        logger.debug(String.format("%s size=(%d,%d)", key, width, height));
    }

    @Override
    public void componentShown(java.awt.event.ComponentEvent e) { }

    @Override
    public void componentHidden(java.awt.event.ComponentEvent e) { }

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
