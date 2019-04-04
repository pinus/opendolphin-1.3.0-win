package open.dolphin.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ダブルクリック or shift-クリックで動作する JToggleButton.
 *
 * @author pns
 */
public class PNSSafeToggleButton extends JLabel {
    private ImageIcon icon;
    private ImageIcon selectedIcon;
    private ActionListener action;
    private boolean isSelected;

    public PNSSafeToggleButton() {
        super();
        isSelected = false;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isShiftDown() || e.getClickCount() == 2) { doClick(); }
            }
        });
    }

    public void doClick() {
        if (isSelected) {
            isSelected = false;
            super.setIcon(icon);
        } else {
            isSelected = true;
            super.setIcon(selectedIcon);
        }
        action.actionPerformed(new ActionEvent(this, 0, "clicked"));
    }

    public void setIcon(ImageIcon i) {
        this.icon = i;
        super.setIcon(i);
    }

    public void setSelectedIcon(ImageIcon i) {
        this.selectedIcon = i;
    }

    public void addActionListener(ActionListener l) {
        action = l;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
