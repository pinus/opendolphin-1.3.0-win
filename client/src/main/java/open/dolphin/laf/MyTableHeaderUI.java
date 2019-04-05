package open.dolphin.laf;

import com.sun.java.swing.plaf.windows.WindowsTableHeaderUI;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.table.JTableHeader;

/**
 *
 * @author pns
 */
public class MyTableHeaderUI extends WindowsTableHeaderUI {
    public static ComponentUI createUI(JComponent c) {
        return new MyTableHeaderUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        JTableHeader h = (JTableHeader)c;
        h.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        h.setPreferredSize(new Dimension(1000,16));
    }
}
