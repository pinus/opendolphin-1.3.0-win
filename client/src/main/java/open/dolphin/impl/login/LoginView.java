/*
 * NewJDialog.java
 *
 * Created on 2007/11/22, 15:15
 */

package open.dolphin.impl.login;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.UIManager;
import open.dolphin.client.GUIConst;

/**
 *
 * @author  kazushi
 */
public class LoginView extends javax.swing.JDialog {

    /** Creates new form NewJDialog */
    public LoginView(java.awt.Frame parent, boolean modal) {
       super(parent, modal);
       getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);

       initComponents();
    }

    public javax.swing.JButton getCancelBtn() {
        return cancelBtn;
    }

    public javax.swing.JButton getLoginBtn() {
        return loginBtn;
    }

    public javax.swing.JPasswordField getPasswordField() {
        return passwordField;
    }

    public javax.swing.JProgressBar getProgressBar() {
        return progressBar;
    }

    public javax.swing.JButton getSettingBtn() {
        return settingBtn;
    }

    public javax.swing.JTextField getUserIdField() {
        return userIdField;
    }

    public javax.swing.JCheckBox getSavePasswordCbx() {
        return savePasswordCbx;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jLabel2 = new javax.swing.JLabel();
        userIdField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        settingBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        loginBtn = new javax.swing.JButton();
        savePasswordCbx = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(211, 211, 211));
        setResizable(false);

        jLabel1.setIcon(GUIConst.ICON_SPLASH_DOLPHIN);
        //pns いたずら
        jLabel1.addMouseListener(new MouseAdapter() {
            boolean flg = true;
            public void mouseClicked(MouseEvent e) {
                if (flg) {
                    ((javax.swing.JLabel) e.getSource()).setIcon(GUIConst.ICON_SPLASH_USAGI);
                    flg = false;
                }
                else {
                    ((javax.swing.JLabel) e.getSource()).setIcon(GUIConst.ICON_SPLASH_DOLPHIN);
                    flg = true;
                }
            }
        });

        jLabel2.setText("ユーザ ID：");

        userIdField.setPreferredSize(new java.awt.Dimension(10, 27));

        jLabel3.setText("パスワード：");

        passwordField.setPreferredSize(new java.awt.Dimension(10, 27));

        settingBtn.setText("設 定");

        cancelBtn.setText("キャンセル");
        cancelBtn.setText((String)UIManager.get("OptionPane.cancelButtonText"));

        loginBtn.setText("ログイン");
        loginBtn.setEnabled(false);

        savePasswordCbx.setText("パスワードを保存する");
        savePasswordCbx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePasswordCbxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(settingBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loginBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(savePasswordCbx)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(userIdField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(savePasswordCbx)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loginBtn)
                    .addComponent(cancelBtn)
                    .addComponent(settingBtn))
                .addContainerGap())
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void savePasswordCbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePasswordCbxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_savePasswordCbxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton loginBtn;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JCheckBox savePasswordCbx;
    private javax.swing.JButton settingBtn;
    private javax.swing.JTextField userIdField;
    // End of variables declaration//GEN-END:variables

}
