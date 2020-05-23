package open.dolphin.client;

import open.dolphin.JsonConverter;
import open.dolphin.helper.HashUtil;
import open.dolphin.helper.Task;
import open.dolphin.infomodel.UserModel;
import open.dolphin.project.Project;
import open.dolphin.service.SystemService;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * 医療機関と管理責任者を登録するクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class AddFacilityDialog extends JDialog implements ComponentListener, Runnable {
    public static final String ACCOUNT_INFO = "accountInfo";
    private static final long serialVersionUID = 1L;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JButton okBtn;
    private JButton cancelBtn;
    private JButton nextBtn;
    private JButton backBtn;
    private PropertyChangeSupport boundSupport;
    private ServerInfo serverInfo;
    private OIDGetter oidGetter;
    private AgreementPanel agreement;
    private AccountInfoPanel accountInfo;
    private AccountState state = AccountState.COM_TEST;
    private boolean comTestOk;
    private boolean agreementOk;
    private boolean accountInfoOk;
    private Logger logger;

    public AddFacilityDialog() {
        super((Frame) null, null, true);
        setIconImage(GUIConst.ICON_DOLPHIN.getImage());
        logger = LoggerFactory.getLogger(AddFacilityDialog.class);
        boundSupport = new PropertyChangeSupport(this);
    }

    @Override
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(prop, l);
    }

    @Override
    public void removePropertyChangeListener(String prop, PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(prop, l);
    }

    @Override
    public void run() {
        initialize();
        connect();
        cardLayout.show(cardPanel, "comTest");
        this.setVisible(true);
    }

    public void setServerInfo(ServerInfo info) {
        serverInfo = info;
        boundSupport.firePropertyChange(ACCOUNT_INFO, null, serverInfo);
    }

    /**
     * GUIコンポーネントを初期化する.
     */
    private void initialize() {

        // リソースから値を取得する
        String windowTitleitle = ClientContext.getString("account.window.tile");
        int windowWidth = ClientContext.getInt("account.window.width");
        int windowHeight = ClientContext.getInt("account.window.height");
        String agreementRes = ClientContext.getString("account.agreement.resource");
        String agreementEnc = ClientContext.getString("account.agreement.encoding");

        String backBtnText = ClientContext.getString("account.backBtn.text");
        String nextBtnText = ClientContext.getString("account.nextBtn.text");
        String cancelBtnText = (String) UIManager.get("OptionPane.cancelButtonText");
        String addBtnText = ClientContext.getString("account.addBtn.text");

        // 通信テストパネルを生成する
        oidGetter = new OIDGetter();

        // 使用許諾パネルを生成する
        AgreementModel agreeModel = new AgreementModel();
        try {
            InputStreamReader ir = new InputStreamReader(ClientContext.getResourceAsStream(agreementRes), agreementEnc);
            BufferedReader reader = new BufferedReader(ir);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            agreeModel.setAgreeText(sb.toString());
        } catch (UnsupportedEncodingException e) {
            System.out.println("AddFacilityDialog.java: " + e);
            e.printStackTrace(System.err);
            return;
        } catch (IOException e) {
            System.out.println("AddFacilityDialog.java: " + e);
            return;
        }

        agreement = new AgreementPanel(agreeModel);

        // アカウント情報パネルを生成する
        accountInfo = new AccountInfoPanel();

        // カードレアイウトへ配置する
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        cardLayout.addLayoutComponent(oidGetter, "comTest");
        cardLayout.addLayoutComponent(agreement, "agreement");
        cardLayout.addLayoutComponent(accountInfo, "accountInfo");
        cardPanel.add(oidGetter, "comTest");
        cardPanel.add(agreement, "agreement");
        cardPanel.add(accountInfo, "accountInfo");

        // 戻るボタンを生成する
        backBtn = new JButton(backBtnText);
        backBtn.setEnabled(false);

        // 次項ボタンを生成する
        nextBtn = new JButton(nextBtnText);
        nextBtn.setEnabled(false);

        // 登録ボタンを生成する
        okBtn = new JButton(addBtnText);
        okBtn.setEnabled(false);

        // キャンセルボタンを生成する
        cancelBtn = new JButton(cancelBtnText);

        // ボタンパネルを生成する
        JPanel btnPanel;
        if (ClientContext.isMac()) {
            btnPanel = GUIFactory.createCommandButtonPanel(
                    new JButton[]{backBtn, nextBtn, cancelBtn, okBtn});

        } else {
            btnPanel = GUIFactory.createCommandButtonPanel(
                    new JButton[]{backBtn, nextBtn, okBtn, cancelBtn});
        }

        // 全体を配置する
        JPanel content = new JPanel(new BorderLayout(0, 17));
        content.add(cardPanel, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));

        // コンテントにする
        this.getContentPane().add(content, BorderLayout.CENTER);

        // Window の設定を行う
        this.setTitle(ClientContext.getFrameTitle(windowTitleitle));
        this.setSize(new Dimension(windowWidth, windowHeight));
        Dimension size = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int top = (size.height - windowHeight) / 3;
        int left = (size.width - windowWidth) / 2;
        this.setLocation(left, top);
    }

    /**
     * イベント接続を行う.
     */
    private void connect() {

        oidGetter.addOidPropertyListener(new OidListener());

        agreement.addAgreePropertyListener(new AgreementListener());

        accountInfo.addValidListener(valid -> {
            accountInfoOk = valid;
            controlButton();
        });

        backBtn.addActionListener(e -> {
            if (state == AccountState.AGREEMENT) {
                setState(AccountState.COM_TEST);
                cardLayout.show(cardPanel, "comTest");
            } else if (state == AccountState.ACCOUNT_INFO) {
                setState(AccountState.AGREEMENT);
                cardLayout.show(cardPanel, "agreement");
            }
        });

        nextBtn.addActionListener(e -> {
            if (state == AccountState.COM_TEST) {
                setState(AccountState.AGREEMENT);
                cardLayout.show(cardPanel, "agreement");
            } else if (state == AccountState.AGREEMENT) {
                setState(AccountState.ACCOUNT_INFO);
                cardLayout.show(cardPanel, "accountInfo");
            }
        });

        okBtn.addActionListener(e -> addFacilityAdmin());

        cancelBtn.addActionListener(e -> close());

        this.addComponentListener(this);
    }

    private void setState(AccountState s) {
        state = s;
        controlButton();
    }

    private void controlButton() {

        backBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        okBtn.setEnabled(false);

        switch (state) {

            case COM_TEST:
                nextBtn.setEnabled(comTestOk);
                break;

            case AGREEMENT:
                backBtn.setEnabled(true);
                nextBtn.setEnabled(agreementOk);
                break;

            case ACCOUNT_INFO:
                backBtn.setEnabled(true);
                okBtn.setEnabled(accountInfoOk);
                break;
        }
    }

    private void close() {
        setVisible(false);
        dispose();
    }

    /**
     * 施設及び管理者アカウントを登録する.
     */
    private void addFacilityAdmin() {

        // 登録するユーザモデルを得る
        UserModel model = accountInfo.getModel();

        // Password の hash化を行う
        String userId = model.getUserId();
        String hashPass = HashUtil.MD5(model.getPassword());
        //String Algorithm = ClientContext.getString("addUser.password.hash.algorithm");
        //String encoding = ClientContext.getString("addUser.password.hash.encoding");
        //String charset = ClientContext.getString("addUser.password.hash.charset");
        //String charset = null;
        //String hashPass = CryptoUtil.createPasswordHash(Algorithm,encoding,charset,userId,model.getPassword());
        model.setPassword(hashPass);

        String message = "アカウント登録";
        String note = userId + "を登録しています...";
        Component c = SwingUtilities.getWindowAncestor(this);

        AddFacilityTask task = new AddFacilityTask(c, message, note, 30 * 1000, model);
        // task.setMillisToPopup(200);
        task.execute();
    }

    @Override
    public void componentMoved(java.awt.event.ComponentEvent componentEvent) {
        //Point loc = getLocation();
        //System.out.println(getTitle() + " : x=" + loc.x+ " y=" + loc.y);
    }

    @Override
    public void componentResized(java.awt.event.ComponentEvent componentEvent) {
        //int width = getWidth();
        //int height = getHeight();
        //System.out.println(getTitle() + " : width=" + width + " height=" + height);
    }

    @Override
    public void componentShown(java.awt.event.ComponentEvent componentEvent) {
    }

    @Override
    public void componentHidden(java.awt.event.ComponentEvent componentEvent) {
    }

    private enum AccountState {COM_TEST, AGREEMENT, ACCOUNT_INFO}

    private class OidListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String oid = (String) e.getNewValue();
            comTestOk = oid != null && (!oid.equals(""));
            controlButton();
        }
    }

    private class AgreementListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            agreementOk = (boolean) e.getNewValue();
            controlButton();
        }
    }

    /**
     * AddFacilityTask
     */
    private class AddFacilityTask extends Task<Void> {

        private UserModel user;

        public AddFacilityTask(Component c, Object message, String note, int maxEstimation, UserModel user) {
            super(c, message, note, maxEstimation);
            this.user = user;
        }

        @Override
        protected Void doInBackground() {

            // Resteasy
            ResteasyClient client = new ResteasyClientBuilder().build();
            JsonConverter jsonConverter = new JsonConverter();
            client.register(jsonConverter);

            String hostAddr = Project.getHostAddress();
            String restUrl = String.format(("http://%s/dolphin"), hostAddr);
            ResteasyWebTarget target = client.target(restUrl);

            SystemService service = target.proxy(SystemService.class);
            service.addFacilityAdmin(user);

            return null;
        }

        @Override
        protected void succeeded(Void result) {
            logger.debug("Task succeeded");
            okBtn.setEnabled(false);

            // 成功メッセージを表示する
            String message = ClientContext.getString("account.task.successMsg1") + "\n" +
                ClientContext.getString("account.task.successMsg2") + "\n" +
                ClientContext.getString("account.task.successMsg3") + "\n" +
                ClientContext.getString("account.task.successMsg4");
            JOptionPane.showMessageDialog(
                AddFacilityDialog.this,
                message,
                AddFacilityDialog.this.getTitle(),
                JOptionPane.INFORMATION_MESSAGE);

            // サーバアカウント情報を通知する
            ServerInfo info = new ServerInfo();
            info.setAdminId(user.getUserId());
//masuda^   FacilityIdをセット
            info.setFacilityId(user.getFacilityModel().getFacilityId());
//masuda$
            setServerInfo(info);

            AddFacilityDialog.this.setVisible(false);
            AddFacilityDialog.this.dispose();
        }

        @Override
        protected void failed(java.lang.Throwable cause) {
            logger.warn(cause.getMessage());

            String errMsg = null;

            if (cause instanceof javax.ejb.EJBAccessException) {
                errMsg = "システム設定エラー\n" +
                    appendExceptionInfo(cause);

            } else if (cause instanceof javax.naming.CommunicationException) {
                errMsg = "ASPサーバに接続できません。\n" +
                    "ファイヤーウォール等がサービスを利用できない設定になっている可能性があります。\n" +
                    appendExceptionInfo(cause);

            } else if (cause instanceof javax.naming.NamingException) {
                errMsg = "アプリケーションエラー\n" +
                    appendExceptionInfo(cause);

            } else if (cause instanceof LoginException) {
                errMsg = "セキュリティエラーが生じました。\n" +
                    "クライアントの環境が実行を許可されない設定になっている可能性があります。\n" +
                    appendExceptionInfo(cause);

            } else if (cause instanceof Exception) {
                errMsg = "予期しないエラー\n" +
                    appendExceptionInfo(cause);
            }

            String title = AddFacilityDialog.this.getTitle();
            JOptionPane.showMessageDialog(AddFacilityDialog.this, errMsg, title, JOptionPane.WARNING_MESSAGE);

        }

        private String appendExceptionInfo(java.lang.Throwable cause) {
            StringBuilder sb = new StringBuilder();
            sb.append("例外クラス: ");
            sb.append(cause.getClass().getName());
            sb.append("\n");
            if (cause.getCause() != null) {
                sb.append("原因: ");
                sb.append(cause.getCause().getMessage());
                sb.append("\n");
            }
            if (cause.getMessage() != null) {
                sb.append("内容: ");
                sb.append(cause.getMessage());
                sb.append("\n");
            }
            return sb.toString();
        }
    }
}
