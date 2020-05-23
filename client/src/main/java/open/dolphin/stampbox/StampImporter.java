package open.dolphin.stampbox;

import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.client.GUIFactory;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.helper.ComponentBoundsManager;
import open.dolphin.helper.PNSTriple;
import open.dolphin.helper.Task;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PublishedTreeModel;
import open.dolphin.infomodel.SubscribedTreeModel;
import open.dolphin.project.Project;
import open.dolphin.ui.ObjectReflectTableModel;
import open.dolphin.ui.PNSScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * StampImporter.
 *
 * @author Minagawa, Kazushi
 * @author pns
 */
public class StampImporter {
    private static final String TITLE = "スタンプインポート";

    private static final Color ODD_COLOR = ClientContext.getColor("color.odd");
    private static final Color EVEN_COLOR = ClientContext.getColor("color.even");
    private static final ImageIcon WEB_ICON = GUIConst.ICON_EARTH_16;
    private static final ImageIcon HOME_ICON = GUIConst.ICON_HOME_16;
    private static final ImageIcon FLAG_ICON = GUIConst.ICON_FLAG_16;

    private static final int WIDTH = 780;
    private static final int HEIGHT = 380;
    private final StampBoxPlugin stampBox;
    private final List<Long> importedTreeList;
    private final Logger logger;
    private JFrame frame;
    //private ObjectListTable browseTable;
    private JTable table;
    private ObjectReflectTableModel<PublishedTreeModel> tableModel;
    private JButton importBtn;
    private JButton deleteBtn;
    private JButton cancelBtn;
    private JLabel publicLabel;
    private JLabel localLabel;
    private JLabel importedLabel;
    private StampDelegater sdl;

    public StampImporter(StampBoxPlugin stampBox) {
        this.stampBox = stampBox;
        importedTreeList = stampBox.getImportedTreeList();
        logger = LoggerFactory.getLogger(StampImporter.class);
    }

    /**
     * 公開されているTreeのリストを取得しテーブルへ表示する.
     */
    public void start() {

        sdl = new StampDelegater();

        int delay = 200;
        int maxEstimation = 60 * 1000;
        String mmsg = "公開スタンプを取得しています...";
        String message = "スタンプ取り込み";
        Component c = null;

        Task<List<PublishedTreeModel>> task = new Task<List<PublishedTreeModel>>(c, message, mmsg, maxEstimation) {

            @Override
            protected List<PublishedTreeModel> doInBackground() {
                return sdl.getPublishedTrees();
            }

            @Override
            protected void succeeded(List<PublishedTreeModel> result) {
                logger.debug("Task succeeded");
                if (sdl.isNoError() && result != null) {
                    // DBから取得が成功したらGUIコンポーネントを生成する
                    initComponent();
                    if (importedTreeList != null && importedTreeList.size() > 0) {
                        for (PublishedTreeModel model : result) {
                            for (Long id : importedTreeList) {
                                if (id == model.getId()) {
                                    model.setImported(true);
                                    break;
                                }
                            }
                        }
                    }
                    tableModel.setObjectList(result);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            sdl.getErrorMessage(),
                            ClientContext.getFrameTitle(TITLE),
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        //task.setMillisToPopup(delay);
        task.execute();
    }

    /**
     * GUIコンポーネントを初期化する.
     */
    public void initComponent() {
        frame = new JFrame(ClientContext.getFrameTitle(TITLE));
        frame.setIconImage(GUIConst.ICON_DOLPHIN.getImage());

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stop();
            }
        });
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int n = ClientContext.isMac() ? 3 : 2;
        int x = (screen.width - WIDTH) / 2;
        int y = (screen.height - HEIGHT) / n;
        ComponentBoundsManager cm = new ComponentBoundsManager(frame, new Point(x, y), new Dimension(WIDTH, HEIGHT), this);
        cm.revertToPreferenceBounds();

        JPanel contentPane = createBrowsePane();
        contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));

        contentPane.setOpaque(true);
        frame.setContentPane(contentPane);
        frame.setVisible(true);
    }

    /**
     * 終了する.
     */
    public void stop() {
        frame.setVisible(false);
        frame.dispose();
    }

    /**
     * 公開スタンプブラウズペインを生成する.
     */
    private JPanel createBrowsePane() {

        JPanel browsePane = new JPanel();

        List<PNSTriple<String, Class<?>, String>> reflectList = Arrays.asList(
                new PNSTriple<>("名  称", String.class, "getName"),
                new PNSTriple<>("カテゴリ", String.class, "getCategory"),
                new PNSTriple<>("公開者", String.class, "getPartyName"),
                new PNSTriple<>("説  明", String.class, "getDescription"),
                new PNSTriple<>("公開先", String.class, "getPublishType"),
                new PNSTriple<>("インポート", Boolean.class, "isImported")
        );

        table = new JTable();
        table.putClientProperty("Quaqua.Table.style", "striped");
        tableModel = new ObjectReflectTableModel<>(reflectList);
        table.setModel(tableModel);

        int[] columnWidth = {120, 90, 170, 270, 40, 40};
        for (int i = 0; i < columnWidth.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);
        }

        importBtn = new JButton("インポート");
        importBtn.setEnabled(false);
        cancelBtn = new JButton("ダイアログを閉じる");
        deleteBtn = new JButton("削除");
        deleteBtn.setEnabled(false);

        publicLabel = new JLabel("グローバル", WEB_ICON, SwingConstants.CENTER);
        localLabel = new JLabel("院内", HOME_ICON, SwingConstants.CENTER);
        importedLabel = new JLabel("インポート済", FLAG_ICON, SwingConstants.CENTER);

        // レイアウトする
        browsePane.setLayout(new BorderLayout(0, 17));
        JPanel flagPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 5));
        flagPanel.add(localLabel);
        flagPanel.add(publicLabel);
        flagPanel.add(importedLabel);
        JPanel cmdPanel = GUIFactory.createCommandButtonPanel(new JButton[]{cancelBtn, deleteBtn, importBtn});
        browsePane.add(flagPanel, BorderLayout.NORTH);
        browsePane.add(new PNSScrollPane(table), BorderLayout.CENTER);
        browsePane.add(cmdPanel, BorderLayout.SOUTH);

        // レンダラを設定する
        PublishTypeRenderer pubTypeRenderer = new PublishTypeRenderer();
        table.getColumnModel().getColumn(4).setCellRenderer(pubTypeRenderer);
        ImportedRenderer importedRenderer = new ImportedRenderer();
        table.getColumnModel().getColumn(5).setCellRenderer(importedRenderer);

        // BrowseTableをシングルセレクションにする
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // コンポーネント間のイベント接続を行う
        table.getSelectionModel().addListSelectionListener(e -> controlButtons());

        // import
        importBtn.addActionListener(e -> importPublishedTree());
        // remove
        deleteBtn.addActionListener(e -> removeImportedTree());
        // キャンセル
        cancelBtn.addActionListener(e -> stop());
        return browsePane;
    }

    private void controlButtons() {
        int row = table.getSelectedRow();
        if (row != -1) {
            PublishedTreeModel model = tableModel.getObject(row);
            if (model.isImported()) {
                importBtn.setEnabled(false);
                deleteBtn.setEnabled(true);
            } else {
                importBtn.setEnabled(true);
                deleteBtn.setEnabled(false);
            }

        } else {
            importBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }
    }

    /**
     * ブラウザテーブルで選択した公開Treeをインポートする.
     */
    public void importPublishedTree() {
        int row = table.getSelectedRow();
        PublishedTreeModel importTree = tableModel.getObject(row);
        if (importTree == null) {
            return;
        }

        // サブスクライブリストに追加する
        SubscribedTreeModel sm = new SubscribedTreeModel();
        sm.setUser(Project.getUserModel());
        sm.setTreeId(importTree.getId());
        List<SubscribedTreeModel> subscribeList = new ArrayList<>(1);
        subscribeList.add(sm);

        // デリゲータを生成する
        sdl = new StampDelegater();

        // Worker, Timer を実行する
        int delay = 200;
        int maxEstimation = 60 * 1000;
        String mmsg = "公開スタンプをインポートしています...";
        String message = "スタンプ取り込み";
        Component c = frame;

        Task<Boolean> task = new Task<Boolean>(c, message, mmsg, maxEstimation) {

            @Override
            protected Boolean doInBackground() {
                sdl.subscribeTrees(subscribeList);
                return sdl.isNoError();
            }

            @Override
            protected void succeeded(Boolean succeeded) {
                if (succeeded) {
                    // スタンプボックスへインポートする
                    stampBox.importPublishedTree(importTree);
                    // Browser表示をインポート済みにする
                    importTree.setImported(true);
                    tableModel.fireTableDataChanged();

                } else {
                    JOptionPane.showMessageDialog(frame,
                            sdl.getErrorMessage(),
                            ClientContext.getFrameTitle(TITLE),
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        //task.setMillisToPopup(delay);
        task.execute();
    }

    /**
     * インポートしているスタンプを削除する.
     */
    public void removeImportedTree() {
        int row = table.getSelectedRow();
        PublishedTreeModel removeTree = tableModel.getObject(row);
        if (removeTree == null) {
            return;
        }

        SubscribedTreeModel sm = new SubscribedTreeModel();
        sm.setTreeId(removeTree.getId());
        sm.setUser(Project.getUserModel());
        final List<SubscribedTreeModel> list = new ArrayList<>(1);
        list.add(sm);

        // DeleteTaskを実行する
        sdl = new StampDelegater();

        // Unsubscribeタスクを実行する
        int delay = 200;
        int maxEstimation = 60 * 1000;
        String mmsg = "インポート済みスタンプを削除しています...";
        String message = "スタンプ取り込み";
        Component c = frame;

        Task<Boolean> task = new Task<Boolean>(c, message, mmsg, maxEstimation) {

            @Override
            protected Boolean doInBackground() {
                sdl.unsubscribeTrees(list);
                return sdl.isNoError();
            }

            @Override
            protected void succeeded(Boolean succeeded) {
                if (succeeded) {
                    // スタンプボックスから削除する
                    stampBox.removeImportedTree(removeTree.getId());
                    // ブラウザ表示を変更する
                    removeTree.setImported(false);
                    tableModel.fireTableDataChanged();

                } else {
                    JOptionPane.showMessageDialog(frame,
                            sdl.getErrorMessage(),
                            ClientContext.getFrameTitle(TITLE),
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        //task.setMillisToPopup(delay);
        task.execute();
    }

    private class PublishTypeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        public PublishTypeRenderer() {
            super();
            setOpaque(true);
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean isFocused,
                                                       int row, int col) {

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setForeground(table.getForeground());
                if (row % 2 == 0) {
                    setBackground(EVEN_COLOR);
                } else {
                    setBackground(ODD_COLOR);
                }
            }

            if (value instanceof String) {

                String pubType = (String) value;

                if (pubType.equals(IInfoModel.PUBLISHED_TYPE_GLOBAL)) {
                    setIcon(WEB_ICON);
                } else {
                    setIcon(HOME_ICON);
                }
                this.setText("");

            } else {
                setIcon(null);
                this.setText(value == null ? "" : value.toString());
            }
            return this;
        }
    }

    private class ImportedRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        public ImportedRenderer() {
            super();
            setOpaque(true);
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean isFocused,
                                                       int row, int col) {

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setForeground(table.getForeground());
                if (row % 2 == 0) {
                    setBackground(EVEN_COLOR);
                } else {
                    setBackground(ODD_COLOR);
                }
            }

            if (value instanceof Boolean) {

                Boolean imported = (Boolean) value;

                if (imported) {
                    this.setIcon(FLAG_ICON);
                } else {
                    this.setIcon(null);
                }
                this.setText("");

            } else {
                setIcon(null);
                this.setText(value == null ? "" : value.toString());
            }
            return this;
        }
    }
}
