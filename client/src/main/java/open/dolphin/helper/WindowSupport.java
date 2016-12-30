package open.dolphin.helper;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import open.dolphin.client.GUIConst;
import open.dolphin.ui.MainFrame;

/**
 * Window Menu をサポートするためのクラス.
 * Factory method で WindowMenu をもつ JFrame を生成する.
 * @author Minagawa,Kazushi
 * @author pns
 */
public class WindowSupport implements MenuListener {

    final private static ArrayList<WindowSupport> allWindows = new ArrayList<>();

    private static final String WINDOW_MWNU_NAME = "ウインドウ";

    private static enum State { OPENED, CLOSED };

    // frame を整列させるときの初期位置と移動幅
    final public static int INITIAL_X = 256;
    final public static int INITIAL_Y = 40;
    final public static int INITIAL_DX = 192;
    final public static int INITIAL_DY = 20;

    // Window support が提供するスタッフ
    // フレーム
    final private MainFrame frame;

    // メニューバー
    final private JMenuBar menuBar;

    // ウインドウメニュー
    final private JMenu windowMenu;

    // Window Action
    final private Action windowAction;

    /**
     * WindowSupportを生成する.
     * @param title フレームタイトル
     * @return WindowSupport
     */
    public static WindowSupport create(String title) {

        // フレームを生成する
        final MainFrame frame = new MainFrame(title);

        // メニューバーを生成する
        JMenuBar menuBar = new JMenuBar();

        // Window メニューを生成する
        JMenu windowMenu = new JMenu(WINDOW_MWNU_NAME);

        // メニューバーへWindow メニューを追加する
        menuBar.add(windowMenu);

        // フレームにメニューバーを設定する
        frame.setJMenuBar(menuBar);

        // Windowメニューのアクション
        // 選択されたらフレームを前面にする
        Action windowAction = new AbstractAction(title) {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.toFront();
            }
        };

        // インスタンスを生成する
        final WindowSupport ret = new WindowSupport(frame, menuBar, windowMenu, windowAction);

        // WindowEvent をこのクラスに通知しリストの管理を行う
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                allWindows.add(ret);
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                allWindows.remove(ret);
            }
        });

        // windowMenu にメニューリスナを設定しこのクラスで処理をする
        windowMenu.addMenuListener(ret);
        return ret;
    }

  public static ArrayList<WindowSupport> getAllWindows() {
        return allWindows;
    }

    // プライベートコンストラクタ
    private WindowSupport(MainFrame frame, JMenuBar menuBar, JMenu windowMenu, Action windowAction) {
        this.frame = frame;
        this.menuBar = menuBar;
        this.windowMenu = windowMenu;
        this.windowAction = windowAction;

        // インスペクタを整列するアクションだけはあらかじめ入れておく
        // こうしておかないと，１回 window メニューを開かないと accelerator が効かないことになる
        windowMenu.add(new ArrangeInspectorAction());
    }

    public MainFrame getFrame() {
        return frame;
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public JMenu getWindowMenu() {
        return windowMenu;
    }

    public Action getWindowAction() {
        return windowAction;
    }

    /**
     * ウインドウメニューが選択された場合，現在オープンしているウインドウのリストを使用し，
     * それらを選択するための MenuItem を追加する.
     * リストをインスペクタとカルテに整理 by pns
     */
    @Override
    public void menuSelected(MenuEvent e) {

        // 全てリムーブする
        JMenu wm = (JMenu) e.getSource();
        wm.removeAll();
        // リストから新規に生成する
        Action action;
        String name;
        int count = 0;
        // まず，カルテとインスペクタ以外
        for (WindowSupport ws : allWindows) {
            action = ws.getWindowAction();
            name = action.getValue(Action.NAME).toString();
            if (!name.contains("インスペクタ") && !name.contains("カルテ")) {
                wm.add(action);
                count ++;
            }
        }
        // カルテ，インスペクタが開いていない場合はリターン
        if (allWindows.size() == count) { return; }

        count = 0;
        wm.addSeparator();

        // 次にカルテ
        for (WindowSupport ws : allWindows) {
            action = ws.getWindowAction();
            name = action.getValue(Action.NAME).toString();
            if (name.contains("カルテ")) {
                action.putValue(Action.SMALL_ICON, getIcon(ws.getFrame()));
                wm.add(action);
                count++;
            }
        }
        if (count != 0) {
            wm.addSeparator();
            count = 0;
        }

        // 次にインスペクタ
        for (WindowSupport ws : allWindows) {
            action = ws.getWindowAction();
            name = action.getValue(Action.NAME).toString();
            if (name.contains("インスペクタ")) {
                action.putValue(Action.SMALL_ICON, getIcon(ws.getFrame()));
                wm.add(action);
                count++;
            }
        }

        // "インスペクタを整列する" 項目を最後に
        if (count != 0) {
            wm.addSeparator();
            Action a = new ArrangeInspectorAction();
            wm.add(a);
        }
    }

    /**
     * インスペクタを整列する action.
     */
    private class ArrangeInspectorAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ArrangeInspectorAction() {
            initComponent();
        }

        private void initComponent() {
            putValue(Action.NAME, "インスペクタを整列");
            putValue(Action.SMALL_ICON, GUIConst.ICON_WINDOWS_22);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UNDERSCORE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame f;
            int x = INITIAL_X; int y = INITIAL_Y; int width = 0; int height = 0;

            for (WindowSupport ws : allWindows) {
                f = ws.getFrame();
                if (f.getTitle().contains("インスペクタ")) {
                    if (width == 0) { width = f.getBounds().width; }
                    if (height == 0) { height = f.getBounds().height; }

                    f.setBounds(x, y, width, height);
                    f.toFront();
                    x += INITIAL_DX; y += INITIAL_DY;
                }
            }
        }
    }

    @Override
    public void menuDeselected(MenuEvent e) {
    }

    @Override
    public void menuCanceled(MenuEvent e) {
    }

    public static ImageIcon getIcon(JFrame frame)  {
        return frame.isActive()? GUIConst.ICON_STATUS_BUSY_16 : GUIConst.ICON_STATUS_OFFLINE_16;
    }
}
