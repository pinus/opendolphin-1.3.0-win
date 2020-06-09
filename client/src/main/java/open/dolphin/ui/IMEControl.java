package open.dolphin.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Mac で IME on/off を切り替える.
 * <ul>
 * <li>ver 1: AppleScript で on/off するバージョン　：　遅すぎてストレスたまる
 * <li>ver 2: InputContext.selectInputMethod バージョン　：　調子よかったが，1.6.0_29 で使えなくなる
 * <li>ver 3: Robot version 切り替わったかどうか判定するために event queue システム導入
 * <li>ver 4: enableInputMethod(true/false) バージョン
 * <li>ver 5: To DO
 * </ul>
 *
 * @author pns
 */
public class IMEControl {
    private static Logger logger = LoggerFactory.getLogger(IMEControl.class);

    static {
        // 「かな」キーで ime on
/*        KeyboardFocusManager.setCurrentKeyboardFocusManager(new DefaultKeyboardFocusManager() {
            @Override
            public void processKeyEvent(Component focusedComponent, KeyEvent e) {
                if (focusedComponent instanceof JTextComponent) {
                    if (e.getKeyCode() == KeyEvent.VK_KATAKANA) {
                        focusedComponent.enableInputMethods(true);
                    //} else if (e.getKeyCode() == KeyEvent.VK_ALPHANUMERIC) {
                    //    focusedComponent.enableInputMethods(false);
                    }
                }
                super.processKeyEvent(focusedComponent, e);
            }
        });*/

        // 上の方法だと，時々ショートカットキーが効かなくなることがあったので，
/*        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener(){
            @Override
            public void eventDispatched(AWTEvent event) {
                if (((KeyEvent)event).getKeyCode() == KeyEvent.VK_KATAKANA) {
                    Object source = event.getSource();
                    if (source instanceof Component) {
                        ((Component)source).enableInputMethods(true);
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK);*/
    }

    /**
     * IME off
     *
     * @param c
     */
    public static void setImeOff(Component c) {
        //c.enableInputMethods(false);
    }

    /**
     * IME on
     *
     * @param c
     */
    public static void setImeOn(Component c) {
        //if (c instanceof JTextComponent) {
        //    c.enableInputMethods(true);
        //}
    }

    /**
     * Component c がフォーカスを取ったら ime を切る
     *
     * @param c
     */
    public static void setImeOffIfFocused(final Component c) {
        //c.addFocusListener(new FocusAdapter() {
        //    @Override
        //    public void focusGained(FocusEvent e) {
        //        setImeOff(c);
        //    }
        //});
    }

    /**
     * Component c がフォーカスを取ったら ime を on にする
     *
     * @param c
     */
    public static void setImeOnIfFocused(final Component c) {
        //c.addFocusListener(new FocusAdapter() {
        //    @Override
        //    public void focusGained(FocusEvent e) {
        //        setImeOn(c);
        //    }
        //    @Override
        //    public void focusLost(FocusEvent e) {
        //        Component w = SwingUtilities.getWindowAncestor(c);
        //        if (w != null) { setImeOff(w); }
        //    }
        //});
    }

    public static void main(String[] argv) {
        //System.out.println("java.version = " + System.getProperty("java.version") + " " + System.getProperty("sun.arch.data.model") + " bit");

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel l1 = new JLabel("TF1");
        JLabel l2 = new JLabel("TF2");
        JList list = new JList(new String[] { "ITEM1", "ITEM2" });
        list.setSelectedIndex(0);

        JTextField tf1 = new JTextField(30);
        tf1.addActionListener(e -> {
            list.requestFocusInWindow();
            list.enableInputMethods(false);
            tf1.enableInputMethods(false);
        });
        JTextField tf2 = new JTextField(30);
        tf2.addActionListener(e -> {
            list.requestFocusInWindow();
        });

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(l1);
        p1.add(tf1);
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        p2.add(l2);
        p2.add(tf2);
        JPanel p3 = new JPanel();
        p3.add(list);

        f.getRootPane().setLayout(new BoxLayout(f.getRootPane(), BoxLayout.Y_AXIS));
        f.getRootPane().add(p1);
        f.getRootPane().add(p2);
        f.getRootPane().add(p3);
        f.pack();
        f.setLocation(200,100);
        f.setVisible(true);
    }
}
