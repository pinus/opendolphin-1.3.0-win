package open.dolphin.ui;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import open.dolphin.client.Dolphin;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;

/**
 * @author pns
 */
public class SettingForWin {
    /**
     * Windows のデフォルトフォントを設定する。
     */
    private static String MEIRYO_NAME_JP = "メイリオ";
    private static String MEIRYO_NAME = "Meiryo";
    private static String DEFAULT_FONT_NAME = Font.SANS_SERIF;

    public static void set(Dolphin context) {
        // necessary to initialize JavaFX Toolkit
        new JFXPanel();
        // JavaFX thread が SchemaEditor 終了後に shutdown してしまわないようにする
        Platform.setImplicitExit(false);

        try {
            // true だと白で書かれてしまう
            UIManager.put("Tree.rendererFillBackground", false);

            UIManager.setLookAndFeel(new MyLookAndFeel());
            //UIManager.setLookAndFeel(new MyNimbusLookAndFeel());

        } catch (UnsupportedLookAndFeelException ex) {
        }
        //setUIFonts();
    }

    private static void setUIFonts() {

        if (isMeiryoAvailable()) setMeiryoFont();
        else setDefaultFont();
    }

    /**
     * メイリオがインストールされているかどうか
     */
    public static boolean isMeiryoAvailable() {

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] names = env.getAvailableFontFamilyNames();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            System.out.println("SettingForWin.java: " + name);
            if (name.equals(MEIRYO_NAME_JP) || name.equals(MEIRYO_NAME)) {
                return true;
            }
        }
        return false;
    }

    /**
     * フォントを Meiryo に設定
     */
    private static void setMeiryoFont() {

        Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                Font font = (Font) value;
                UIManager.put(key, new Font(MEIRYO_NAME, font.getStyle(), font.getSize()));
            }
        }
        System.out.println("デフォルトのフォントをメイリオに変更しました");
    }

    /**
     * デフォルトフォントを設定
     */
    private static void setDefaultFont() {
        Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                Font font = (Font) value;
                UIManager.put(key, new Font(DEFAULT_FONT_NAME, font.getStyle(), font.getSize()));
            }
        }
    }

    private static class MyLookAndFeel extends WindowsLookAndFeel {
        private static final long serialVersionUID = 1L;

        @Override
        public UIDefaults getDefaults() {
            UIDefaults uiDefaults = super.getDefaults();

            uiDefaults.putDefaults(new Object[]{
                    "ListUI", "open.dolphin.laf.MyListUI",
                    "TreeUI", "open.dolphin.laf.MyTreeUI",
                    "TableUI", "open.dolphin.laf.MyTableUI",
                    "TableHeaderUI", "open.dolphin.laf.MyTableHeaderUI",
                    "TextFieldUI", "open.dolphin.laf.MyTextFieldUI",
                    "PasswordFieldUI", "open.dolphin.laf.MyPasswordFieldUI",
            });

            return uiDefaults;
        }
    }
}
