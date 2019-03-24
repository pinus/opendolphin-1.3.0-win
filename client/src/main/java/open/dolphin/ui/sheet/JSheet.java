package open.dolphin.ui.sheet;

import javax.swing.*;
import javax.swing.plaf.OptionPaneUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Mac で JDialog を Quaqua の JSheet に置き換えたのを JDialog に戻す
 * @author pns
 */
public class JSheet extends JDialog {
    private static final long serialVersionUID = 1L;

    private Object value = null;
    private SheetListener sheetListener = null;

    public JSheet(Frame frame) {
        super(frame, true);
    }
    
    public JSheet(Dialog dialog) {
        super(dialog, true);
    }

    public void addSheetListener(SheetListener listener) {
        this.sheetListener = listener;
    }

    public static JSheet createDialog(JOptionPane pane, Window parent) {
        JSheet dialog = null;
        if (parent instanceof Dialog) dialog = new JSheet((Dialog)parent);
        else dialog = new JSheet((Frame)parent);

        return dialog.createDialogInstance(pane, parent);
    }

    private JSheet createDialogInstance(final JOptionPane pane, Window parent) {
        final JSheet dialog = this;

        this.setComponentOrientation(pane.getComponentOrientation());
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(pane, BorderLayout.CENTER);
        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(parent);
        WindowAdapter adapter = new WindowAdapter() {
            private boolean gotFocus = false;
            @Override
            public void windowClosing(WindowEvent we) {
                setValue(null);
            }
            @Override
            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    OptionPaneUI ui = pane.getUI();
                    if (ui != null) ui.selectInitialValue(pane);
                    gotFocus = true;
                }
            }
        };
        this.addWindowListener(adapter);
        this.addWindowFocusListener(adapter);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
                // reset value to ensure closing works properly
                setValue(JOptionPane.UNINITIALIZED_VALUE);
            }
        });
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                // Let the defaultCloseOperation handle the closing
                // if the user closed the window without selecting a button
                // (newValue = null in that case).  Otherwise, close the dialog.
                if (dialog.isVisible() && event.getSource() == pane &&
                    (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) &&
                    event.getNewValue() != null &&
                    event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    dialog.setVisible(false);

                    if (sheetListener != null) {
                        SheetEvent se = new SheetEvent();
                        se.setOption(getReturnValue(pane));
                        sheetListener.optionSelected(se);
                    }
                }
            }
        });

        return dialog;
    }

    public static void showMessageSheet(Window parent, String message) {
        JOptionPane.showMessageDialog(parent, message);
    }

    public static void showMessageSheet(Window parent, String message, int messageType) {
        JOptionPane.showMessageDialog(parent, message, "", messageType);
    }

    public static void showSheet(JOptionPane pane, Window parent, SheetListener listener) {
        JDialog dialog = pane.createDialog(parent, "");
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        SheetEvent se = new SheetEvent();
        se.setOption(getReturnValue(pane));
        listener.optionSelected(se);
    }

    // JOptionPane.showConfirmDialog 互換
    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType, int messageType) {
        return showOptionDialog(parentComponent, message, title, optionType, messageType, null, null, null);
    }
    
    public static void showConfirmSheet(Component parentComponent, Object message, int optionType, int messageType, SheetListener listener) {
        int answer = JOptionPane.showConfirmDialog(parentComponent, message, "", optionType, messageType);
        SheetEvent se = new SheetEvent();
        se.setOption(answer);
        listener.optionSelected(se);
    }

    public static void showConfirmSheet(Component parentComponent, Object message, int optionType, SheetListener listener) {
        int answer = JOptionPane.showConfirmDialog(parentComponent, message, "", optionType, JOptionPane.WARNING_MESSAGE);
        SheetEvent se = new SheetEvent();
        se.setOption(answer);
        listener.optionSelected(se);
    }

    public static void showConfirmSheet(Component parentComponent, Object message, SheetListener listener) {
        int answer = JOptionPane.showConfirmDialog(parentComponent, message, "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        SheetEvent se = new SheetEvent();
        se.setOption(answer);
        listener.optionSelected(se);
    }
    
    // JOptionPane.showMessageDialog 互換
    public static void showMessageDialog(Component parentComponent, Object message, String title, int messageType) {
        showOptionDialog(parentComponent, message, title, JOptionPane.DEFAULT_OPTION, messageType, null, null, null);
    }

    // JOptionPane.showOptionDialog 互換
    public static int showOptionDialog(Component parentComponent, Object message, String title,
            int optionType, int messageType, Icon icon, final Object[] options, Object initialValue) {

        return JOptionPane.showOptionDialog(parentComponent, message, title, optionType, messageType, icon, options, initialValue);
    }

    /**
     * その component に既に JSheet が表示されているかどうか
     * @param parentComponent
     * @return
     */
    public static boolean isAlreadyShown(Component parentComponent) {
        Window window = getWindowForComponent(parentComponent);
        Window[] windowList = window.getOwnedWindows();
        for (Window w : windowList) {
            if (w instanceof JSheet && w.isVisible()) {
                // すでに JSheet が表示されている
                return true;
            }
        }
        return false;
    }

    private static Window getWindowForComponent(Component parentComponent) {
        if (parentComponent == null) {
            return JOptionPane.getRootFrame();
        }
        if (parentComponent instanceof Frame || parentComponent instanceof Dialog) {
            return (Window) parentComponent;
        }
        return getWindowForComponent(parentComponent.getParent());
    }
    
    public void setValue(Object newValue) {
        Object oldValue = value;
        value = newValue;
        firePropertyChange(JOptionPane.VALUE_PROPERTY, oldValue, value);
    }

    private static int getReturnValue(JOptionPane pane) {
        Object selectedValue = pane.getValue();
        Object[] options = pane.getOptions();

        if(selectedValue == null)
            return JOptionPane.CLOSED_OPTION;
        if(options == null) {
            if(selectedValue instanceof Integer)
                return ((Integer)selectedValue).intValue();
            return JOptionPane.CLOSED_OPTION;
        }
        for(int counter = 0, maxCounter = options.length;
            counter < maxCounter; counter++) {
            if(options[counter].equals(selectedValue))
                return counter;
        }
        return JOptionPane.CLOSED_OPTION;
    }

    public static void showConfirmSheet(Window parent, String message, int optionType, int messageType, SheetListener listener) {
        int answer = JOptionPane.showConfirmDialog(parent, message, "", optionType, messageType);
        SheetEvent se = new SheetEvent();
        se.setOption(answer);
        listener.optionSelected(se);
    }

    public static void showSaveSheet(JFileChooser chooser, Window parent, SheetListener listener) {
        int answer = chooser.showSaveDialog(parent);
        SheetEvent se = new SheetEvent();
        se.setOption(answer);
        listener.optionSelected(se);
    }
    
    public static void showOpenSheet(JFileChooser chooser, Window parent, SheetListener listener) {
        int answer = chooser.showOpenDialog(parent);
        SheetEvent se = new SheetEvent();
        se.setOption(answer);
        listener.optionSelected(se);
    }

    public interface SheetListener {
        public void optionSelected(SheetEvent se);
    }
    
    public static class SheetEvent {

        private int option;

        public SheetEvent() {}

        public void setOption(int option) {
            this.option = option;
        }

        public int getOption() {
            return option;
        }
    }
}
