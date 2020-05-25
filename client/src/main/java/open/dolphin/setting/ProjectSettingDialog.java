package open.dolphin.setting;

import open.dolphin.client.ClientContext;
import open.dolphin.event.ValidListener;
import open.dolphin.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 環境設定ダイアログ.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public final class ProjectSettingDialog {
    public static final Color BACKGROUND = new Color(244, 244, 244);
    private static int DEFAULT_WIDTH = 600;
    private static int DEFAULT_HEIGHT = 700;
    // GUI
    private JDialog dialog;
    private JPanel itemPanel;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JButton okButton;
    private JButton cancelButton;
    // 全体のモデル
    private HashMap<String, AbstractSettingPanel> settingMap;
    private List<AbstractSettingPanel> allSettings;
    private List<JToggleButton> allBtns;
    private String startSettingName;
    private boolean loginState;
    private boolean okState;
    private Logger logger;
    private Frame parentFrame = null;

    private ValidListener validListener;

    public ProjectSettingDialog() {
        logger = LoggerFactory.getLogger(ProjectSettingDialog.class);
    }

    public ProjectSettingDialog(Window f) {
        this();
        parentFrame = (Frame) f;
    }

    public void addValidListener(ValidListener listener) {
        validListener = listener;
    }

    public boolean getLoginState() {
        return loginState;
    }

    public void setLoginState(boolean b) {
        loginState = b;
    }

    public boolean getValue() {
        return Project.getProjectStub().isValid();
    }

    public void notifyResult() {
        boolean valid = Project.getProjectStub().isValid();
        validListener.validity(valid);
    }

    /**
     * オープン時に表示する設定画面をセットする.
     *
     * @param startSettingName 表示する設定画面の名前
     */
    public void setProject(String startSettingName) {
        this.startSettingName = startSettingName;
    }

    /**
     * 設定画面を開始する.
     */
    public void start() {

        Runnable r = () -> {
            // モデルを得る
            allSettings = new ArrayList<>();
            allSettings.add(new KarteSettingPanel());
            allSettings.add(new CodeHelperSettingPanel());
            allSettings.add(new CalendarSettingPanel());
            allSettings.add(new MiscSettingPanel());

            // 設定パネル(AbstractSettingPanel)を格納する Hashtableを生成する
            // key=設定プラグインの名前 value=設定プラグイン
            settingMap = new HashMap<>();

            // GUI を構築しモデルをバインドする
            initComponents();
            logger.debug("component initialized");

            // オープン時に表示する設定画面を決定する
            int index = 0;

            if (startSettingName != null) {
                logger.debug("startSettingName = " + startSettingName);
                for (AbstractSettingPanel setting : allSettings) {
                    if (startSettingName.equals(setting.getId())) {
                        logger.debug("found index " + index);
                        break;
                    }
                    index++;
                }
            }

            index = (index >= 0 && index < allSettings.size()) ? index : 0;

            // ボタンを押して表示する
            allBtns.get(index).doClick();
        };

        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    /**
     * GUI を構築する.
     */
    private void initComponents() {

        itemPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        //itemPanel = new JPanel();
        //itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));

        //
        // 設定プラグインを起動するためのトグルボタンを生成し
        // パネルへ加える
        //
        allBtns = new ArrayList<>();
        ButtonGroup bg = new ButtonGroup();
        allSettings.stream().map(setting -> {
            String id = setting.getId();
            String text = setting.getTitle();
            JToggleButton tb = new JToggleButton(text, setting.getIcon());
            tb.setFocusable(false);
            if (ClientContext.isWin()) {
                tb.setMargin(new Insets(0, 0, 0, 0));
            }
            tb.setHorizontalTextPosition(SwingConstants.CENTER);
            tb.setVerticalTextPosition(SwingConstants.BOTTOM);
            itemPanel.add(tb);
            bg.add(tb);
            tb.setActionCommand(id);
            return tb;
        }).forEachOrdered(tb -> allBtns.add(tb));

        //
        // 設定パネルのコンテナとなるカードパネル
        //
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        // コマンドボタン
        okButton = new JButton("保存");
        okButton.setEnabled(false);

        // Cancel
        String text = (String) UIManager.get("OptionPane.cancelButtonText");
        cancelButton = new JButton(text);

        // 全体ダイアログのコンテントパネル
        JPanel panel = new JPanel(new BorderLayout(11, 0));
        panel.add(itemPanel, BorderLayout.NORTH);
        panel.add(cardPanel, BorderLayout.CENTER);
        // quaqua で表示が乱れるのを防ぐ
        panel.setPreferredSize(new Dimension(DEFAULT_WIDTH - 50, DEFAULT_HEIGHT - 90));
        panel.setMinimumSize(new Dimension(DEFAULT_WIDTH - 50, DEFAULT_HEIGHT - 90));

        // ダイアログを生成する
        Object[] options = new Object[]{okButton, cancelButton};

        JOptionPane jop = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                options,
                okButton);

        dialog = jop.createDialog(parentFrame, ClientContext.getFrameTitle("環境設定"));
        // この方法で作った dialog のタイトルバーは "brushMetalLook" にすると 244 の Gray １色になる
        // 構造は JDialog > JPanel (ContentPane) > JOptionPane となっている
        dialog.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);

        // Container の background が黒になってしまうので直す
        if (ClientContext.isMac()) { setContainerBackground(dialog); }

        dialog.setResizable(false);
        dialog.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));

        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (size.width - DEFAULT_WIDTH) / 2;
        int y = (size.height - DEFAULT_HEIGHT) / 3;
        dialog.setLocation(x, y);
        logger.debug("dialog created");

        // イベント接続を行う
        connect();
    }

    /**
     * GUI コンポーネントのイベント接続を行う.
     */
    private void connect() {

        // 設定項目ボタンに追加するアクションリスナを生成する
        ActionListener al = event -> {
            logger.debug("actionPerformed");

            AbstractSettingPanel theSetting = null;
            String name = event.getActionCommand();
            logger.debug("actionCmd = " + name);

            for (AbstractSettingPanel setting : allSettings) {
                String id = setting.getId();
                if (id.equals(name)) {
                    theSetting = setting;
                    logger.debug("found the setting " + theSetting.getClass().getName());
                    break;
                }
            }
            if (theSetting != null) {
                startSetting(theSetting);
            }
        };

        // 全てのボタンにリスナを追加する
        allBtns.forEach(btn -> btn.addActionListener(al));

        // Save
        okButton.addActionListener(e -> doOk());
        okButton.setEnabled(false);

        // Cancel
        cancelButton.addActionListener(e -> doCancel());

        // ESC でウインドウクローズ
        InputMap im = dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = KeyStroke.getKeyStroke("ESCAPE");
        im.put(key, "close-window");
        dialog.getRootPane().getActionMap().put("close-window", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        // Dialog
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                doCancel();
            }
        });
    }

    /**
     * 選択された項目(SettingPanel)の編集を開始する.
     */
    private void startSetting(final AbstractSettingPanel sp) {

        if (sp.getContext() != null) {
            cardLayout.show(cardPanel, sp.getTitle());
            return;
        }

        Thread t = new Thread(() -> {
            // まだ生成されていない場合は
            // 選択された設定パネルを生成しカードに追加する
            settingMap.put(sp.getId(), sp);
            sp.setContext(ProjectSettingDialog.this);
            sp.setLogInState(loginState);
            sp.addStateListener(ProjectSettingDialog.this::controlButtons);
            sp.setProjectStub(Project.getProjectStub());
            sp.start();

            SwingUtilities.invokeLater(() -> {
                // Container のバックグランドを直す
                setContainerBackground(sp.getUI());

                cardPanel.add(sp.getUI(), sp.getTitle());
                cardLayout.show(cardPanel, sp.getTitle());
                if (!dialog.isVisible()) {
                    dialog.setVisible(true);
                }
            });
        });

        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    /**
     * "apple.awt.brushMetalLook" の Container のバックグランドが黒くなるのを直す.
     *
     * @param component 修正する Component
     */
    private void setContainerBackground(Component component) {
        if (component instanceof Container) {
            if (component instanceof JComponent) {
                ((JComponent)component).setOpaque(false);
            }
            for (Component c : ((Container) component).getComponents()) {
                setContainerBackground(c);
            }
        }
    }

    /**
     * 一つの SettingPanel から state 情報が送られてきたら，全ての state を調べ直す.
     *
     * @param state SettingPanelState
     */
    public void controlButtons(SettingPanelState state) {
        // 全てのカードをスキャンして OK ボタンをコントロールする
        boolean newOk = true;
        if (settingMap.values().stream()
                .anyMatch(p -> p.getState().equals(SettingPanelState.INVALID))) {
            newOk = false;
        }

        if (okState != newOk) {
            okState = newOk;
            okButton.setEnabled(okState);
        }
    }

    public void doOk() {
        settingMap.values().forEach(AbstractSettingPanel::save);
        dialog.setVisible(false);
        dialog.dispose();
        notifyResult();
    }

    public void doCancel() {
        dialog.setVisible(false);
        dialog.dispose();
        notifyResult();
    }
}
