package open.dolphin.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

/**
 * Mac で IME on/off を切り替える.
 * <ul>
 * <li>ver 1: AppleScript で on/off するバージョン: 遅すぎてストレスたまる
 * <li>ver 2: InputContext.selectInputMethod バージョン: 調子よかったが，1.6.0_29 で使えなくなる
 * <li>ver 3: Robot version 切り替わったかどうか判定するために event queue システム導入
 * <li>ver 4: enableInputMethod(true/false) バージョン: short-cut が効かなくなったり不安定
 * <li>ver 5: Robot version 復活. 物理キーが押されていると誤動作するのでキー入力でフォーカスが当たるところには使えない
 * </ul>
 *
 * @author pns
 */
public class IMEControl {
    private static Logger logger = LoggerFactory.getLogger(IMEControl.class);
    private static boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac");
    private static Robot robot;
    static {
        try { robot = new Robot(); }
        catch (AWTException e) { logger.error(e.getMessage()); }
    }

    /**
     * IME-off. Shift-Control-C で英字
     */
    public static void off() {
        if (isMac) {
            logger.info("atok eiji mode");
            type(KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL, KeyEvent.VK_C);
        }
    }

    /**
     * IME-on. Shift-Control-Z でひらがな
     */
    public static void on() {
        if (isMac) {
            logger.info("atok hiragana mode");
            type(KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL, KeyEvent.VK_Z);
        }
    }

    private static void type(int... keys) {
        for (int i = 0; i < keys.length; i++) { robot.keyPress(keys[i]); }
        for (int i = keys.length -1 ; i >= 0; i--) { robot.keyRelease(keys[i]); }
    }

    /**
     * IME-off when focused.
     *
     * @param c component to add focus listener
     */
    public static void off(final Component c) {
        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { off(); }
        });
    }

    /**
     * IME-on when focused.
     *
     * @param c component to add focus listener
     */
    public static void on(final Component c) {
        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { on(); }
        });
    }

    public static void main(String[] argv) {

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel l1 = new JLabel("TF1");
        JLabel l2 = new JLabel("TF2");

        JTextField tf1 = new JTextField(30);
        on(tf1);
        JTextField tf2 = new JTextField(30);
        off(tf2);

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(l1);
        p1.add(tf1);
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        p2.add(l2);
        p2.add(tf2);

        f.getRootPane().setLayout(new BoxLayout(f.getRootPane(), BoxLayout.Y_AXIS));
        f.getRootPane().add(p1);
        f.getRootPane().add(p2);
        f.pack();
        f.setLocation(200,100);
        f.setVisible(true);
    }
}
