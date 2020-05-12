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
        JOptionPane optionPane = new JOptionPane();
        JDialog dialog = optionPane.createDialog("dialog");

        showComp(dialog.getContentPane());

        JWindow w = new JWindow();
        w.setBackground(new Color(0,0,0,0));
        w.getRootPane().setOpaque(false);
        w.add(dialog.getContentPane(), BorderLayout.CENTER);
        w.pack();
        w.setLocation(1000,500);
        w.setVisible(true);
    }

    private void showComp(Component component) {
        p(component);
        if (component instanceof JComponent) {
            for (Component c : ((JComponent)component).getComponents()) {
                p("parent: " + component);
                ((JComponent)component).setOpaque(false);
                showComp(c);
            }
        }
    }

    private void p(Object o) {
        System.out.println(o);
    }

    public static void main(String[] argv) {
        new Test51().start();
    }
}
