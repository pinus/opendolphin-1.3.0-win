package open.dolphin.setting;

import open.dolphin.client.ClientContext;
import open.dolphin.project.ProjectStub;
import org.apache.log4j.Logger;

import javax.swing.*;

/**
 * AbstractSettingPanel.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public abstract class AbstractSettingPanel {

    public static final String STATE_PROP   = "stateProp";

    private ProjectSettingDialog context;
    private ProjectStub projectStub;
    private SettingPanelState state = SettingPanelState.NONE;
    private JPanel ui;
    private boolean loginState;
    private String title;
    private ImageIcon icon;
    private String id;
    private StateListener stateListener;

    private final Logger logger;

    /**
     * Creates a new instance of SettingPanel.
     */
    public AbstractSettingPanel() {
        logger = ClientContext.getBootLogger();
        init();
    }

    private void init() {
        setUI(new JPanel());
    }

    public abstract void start();

    public abstract void save();

    public void addStateListener(StateListener listener) {
        stateListener = listener;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public ProjectSettingDialog getContext() {
        return context;
    }

    public void setContext(ProjectSettingDialog dialog) {
        context = dialog;
    }

    /**
     * ログイン後に呼ばれた場合 true.
     * @return
     */
    public boolean isLoginState() {
        return loginState;
    }

    public void setLogInState(boolean login) {
        loginState = login;
    }

    public JPanel getUI() {
        return ui;
    }

    public void setUI(JPanel p) {
        ui = p;
    }

    public ProjectStub getProjectStub() {
        return projectStub;
    }

    public void setProjectStub(ProjectStub projectStub) {
        this.projectStub = projectStub;
    }

    /**
     * @param state The state to set.
     */
    public void setState(SettingPanelState state) {
        this.state = state;
        stateListener.state(state);
    }

    /**
     * @return Returns the state.
     */
    public SettingPanelState getState() {
        return state;
    }
}
