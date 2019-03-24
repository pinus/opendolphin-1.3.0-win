package open.dolphin.impl.login;

import open.dolphin.client.GUIConst;
import open.dolphin.ui.CompletableJTextField;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * LoginView.
 *
 * @author pns
 */
public class LoginView extends JFrame {
    private static final int TEXT_LENGTH = 15; // letters
    private static final int TEXT_HEIGHT = 28; // dots

    private JButton loginBtn;
    private JButton cancelBtn;
    private JButton settingBtn;

    private CompletableJTextField hostField;
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JCheckBox savePasswordCbx;
    private JProgressBar progressBar;

    private ImageIcon splash1 = GUIConst.ICON_SPLASH_DOLPHIN;
    private ImageIcon splash2 = GUIConst.ICON_SPLASH_USAGI;

    /**
     * create LoginView JFrame.
     */
    public LoginView() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);

        initComponents();
        pack();
    }

    private void initComponents() {
        // 各 Component の生成と設定
        JToggleButton splashLabel = new JToggleButton();
        splashLabel.setBorderPainted(false);
        splashLabel.setBorder(BorderFactory.createEmptyBorder());
        splashLabel.setIcon(splash1);
        splashLabel.setSelectedIcon(splash2);

        // ボタン
        loginBtn = new JButton("ログイン");
        cancelBtn = new JButton("キャンセル");
        settingBtn = new JButton("設定");

        // チェックボックス
        savePasswordCbx = new JCheckBox("パスワードを保存する");

        // プログレスバー
        progressBar = new JProgressBar();

        // テキストフィールド
        hostField = new CompletableJTextField(TEXT_LENGTH);
        hostField.setPreferences(Preferences.userNodeForPackage(getClass()));
        userIdField = new JTextField(TEXT_LENGTH);
        passwordField = new JPasswordField(TEXT_LENGTH);

        // テキストフィールドのフォーカス移動
        hostField.addActionListener(e -> userIdField.requestFocusInWindow());
        userIdField.addActionListener(e -> passwordField.requestFocusInWindow());

        // ラベル＋テキストフィールドの combo 作成
        JPanel hostPanel = createFieldCombo("サーバ：", hostField);
        JPanel userPanel = createFieldCombo("ユーザ：", userIdField);
        JPanel passwordPanel = createFieldCombo("パスワード：", passwordField);

        // ボタンパネル作成
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(settingBtn);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelBtn);
        buttonPanel.add(loginBtn);

        // 右側のパネル
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(hostPanel);
        rightPanel.add(userPanel);
        rightPanel.add(passwordPanel);
        rightPanel.add(savePasswordCbx);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(progressBar);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(buttonPanel);
        rightPanel.add(Box.createVerticalStrut(12));

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        getContentPane().add(splashLabel);
        getContentPane().add(Box.createHorizontalStrut(10));
        getContentPane().add(rightPanel);
        getContentPane().add(Box.createHorizontalStrut(10));

    }

    private JPanel createFieldCombo(String labelText, JTextComponent comp) {
        // size 指定
        comp.setMaximumSize(new Dimension(500, TEXT_HEIGHT));
        comp.setMinimumSize(new Dimension(0, TEXT_HEIGHT));

        JLabel label = new JLabel(labelText);
        label.setHorizontalAlignment(JLabel.RIGHT);
        label.setPreferredSize(new Dimension(100, TEXT_HEIGHT + 10));
        label.setOpaque(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalGlue());
        panel.add(label);
        panel.add(comp);
        return panel;
    }

    /**
     * loginBtn
     *
     * @return loginBtn
     */
    public JButton getLoginBtn() {
        return loginBtn;
    }

    /**
     * cancelBtn
     *
     * @return cancelBtn
     */
    public JButton getCancelBtn() {
        return cancelBtn;
    }

    /**
     * settingBtn
     *
     * @return settingBtn
     */
    public JButton getSettingBtn() {
        return settingBtn;
    }

    /**
     * hostField
     *
     * @return hostField
     */
    public CompletableJTextField getHostField() {
        return hostField;
    }

    /**
     * userIdField
     *
     * @return userIdField
     */
    public JTextField getUserIdField() {
        return userIdField;
    }

    /**
     * passwordField
     *
     * @return passwordField
     */
    public JPasswordField getPasswordField() {
        return passwordField;
    }

    /**
     * savePasswordCbx
     *
     * @return savePasswordCbx
     */
    public JCheckBox getSavePasswordCbx() {
        return savePasswordCbx;
    }

    /**
     * progressBar
     *
     * @return progressBar
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }
}

