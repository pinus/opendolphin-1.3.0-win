import open.dolphin.client.ClientContext;
import open.dolphin.client.ClientContextStub;
import open.dolphin.client.ColorChooserComp;
import open.dolphin.ui.PNSToggleButton;
import sun.awt.AppContext;

import javax.swing.*;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;

/**
 * @author pns
 */
public class Test51 {

    private void start() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextField tf = new JTextField();
        f.getContentPane().add(tf, BorderLayout.NORTH);
        f.setBounds(600, 0, 600, 400);
        f.setVisible(true);

        JWindow w = new JWindow(f);
        JTextField tf2 = new JTextField() ;
        tf2.enableInputMethods(true);
        w.setAlwaysOnTop(true);
        w.getContentPane().add(tf2, BorderLayout.CENTER);
        w.setBounds(620, 80, 560, 30);
        w.setVisible(true);



    }

    private void p(Object o) {
        System.out.println(o);
    }

    public static void main(String[] argv) {
        new Test51().start();
    }
}
