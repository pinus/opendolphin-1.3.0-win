package open.dolphin.ui;

import open.dolphin.client.GUIConst;

import javax.swing.*;
import java.awt.*;

/**
 * ３段構造フレーム
 * +---------------+
 * | Command Panel |
 * |---------------|
 * |               |
 * | Main Panel    |
 * |               |
 * |---------------|
 * | Status Panel  |
 * +---------------+
 *
 * @author pns
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    // Panels
    private CommandPanel commandPanel;
    private MainPanel mainPanel;
    private StatusPanel statusPanel;

    public MainFrame() {
        //setBackground(new Color(0,0,0,0));
        //AWTAccessor.getWindowAccessor().setOpacity(this, 0.5f);
        this("", true, true);
    }

    public MainFrame(String title) {
        this(title, true, true);
    }

    public MainFrame(String title, boolean commandPanelNeeded, boolean statusPanelNeeded) {
        initComponents(title, commandPanelNeeded, statusPanelNeeded);
    }

    private void initComponents(String title, boolean commandPanelNeeded, boolean statusPanelNeeded) {
        getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
        setTitle(title);
        setIconImage(GUIConst.ICON_DOLPHIN.getImage());

        // コマンドパネル
        if (commandPanelNeeded) {
            commandPanel = new CommandPanel();
        }

        // メインパネル
        mainPanel = new MainPanel();

        // ステータスパネル
        if (statusPanelNeeded) {
            statusPanel = new StatusPanel();
        }

        // 全体をレイアウトする
        this.setLayout(new BorderLayout(0, 0));
        if (commandPanelNeeded) {
            this.add(commandPanel, BorderLayout.NORTH);
        }
        this.add(mainPanel, BorderLayout.CENTER);
        if (statusPanelNeeded) {
            this.add(statusPanel, BorderLayout.SOUTH);
        }
    }

    /**
     * CommandPanel を返す
     *
     * @return Command Panel
     */
    public CommandPanel getCommandPanel() {
        return commandPanel;
    }

    /**
     * MainPanel を返す
     *
     * @return Main Panel
     */
    public MainPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * StatusPanel を返す
     *
     * @return Status Panel
     */
    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    /**
     * この frame を返す
     *
     * @return Main Frame
     */
    public MainFrame getFrame() {
        return this;
    }

    /**
     * StatusPanel を作る
     */
    public void createStatusPanel() {
        if (statusPanel != null) {
            this.remove(statusPanel);
        }
        statusPanel = new StatusPanel();
        this.add(commandPanel, BorderLayout.NORTH);
    }

    /**
     * StatusPanel を消す
     */
    public void removeStatusPanel() {
        this.remove(statusPanel);
        statusPanel = null;
    }

    /**
     * CommandPanel を作る
     */
    public void createCommandPanel() {
        if (commandPanel != null) {
            this.remove(commandPanel);
        }
        commandPanel = new CommandPanel();
        this.add(commandPanel, BorderLayout.NORTH);
    }

    /**
     * StatusPanel を消す
     */
    public void removeCommandPanel() {
        this.remove(commandPanel);
        commandPanel = null;
    }

    /**
     * 上部のコマンドパネル
     */
    public class CommandPanel extends HorizontalPanel {
        private static final long serialVersionUID = 1L;

        public CommandPanel() {
        }
    }

    /**
     * メインパネル
     */
    public class MainPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        public MainPanel() {
            super();
        }
    }

    public static void main(String[] argv) {
        open.dolphin.client.ClientContext.setClientContextStub(new open.dolphin.client.ClientContextStub());

        MainFrame f = new MainFrame();
        f.setSize(600, 700);

        f.setTitle("テスト〜タイトル");

        CommandPanel commandPanel = f.getCommandPanel();
        commandPanel.setPanelHeight(56);
        for (int i = 0; i < 5; i++) {
            JButton b = new JButton("TEST" + i);
            commandPanel.add(b);
        }

        commandPanel.addGlue();
        commandPanel.add(new JButton("TEST"));

        MainPanel mainPanel = f.getMainPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(new JTextPane(), BorderLayout.CENTER);

        StatusPanel statusPanel = f.getStatusPanel();
        statusPanel.add("TEST");
        statusPanel.addSeparator();
        statusPanel.add("test2");
        statusPanel.addGlue();
        statusPanel.addProgressBar();
        statusPanel.addSeparator();
        statusPanel.add("2011-10-21", "3rdLabel");
        statusPanel.setMargin(8);

        statusPanel.setText("ラベル変更", "0");
        statusPanel.setText("ラベル", "1");
        statusPanel.setText("2011-10-27", "3rdLabel");

        f.setVisible(true);
    }
}
