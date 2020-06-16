package open.dolphin.client;

import open.dolphin.helper.ComponentBoundsManager;
import open.dolphin.helper.MenuSupport;
import open.dolphin.helper.Task;
import open.dolphin.helper.WindowSupport;
import open.dolphin.ui.MainFrame;
import open.dolphin.ui.PNSTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * ImageBox.
 *
 * @author Minagawa, Kazushi
 */
public class ImageBox extends AbstractMainTool {

    private static final int DEFAULT_COLUMN_COUNT = 3;
    private static final int DEFAULT_IMAGE_WIDTH = 120;
    private static final int DEFAULT_IMAGE_HEIGHT = 120;
    private static final Point DEFAULT_LOC = new Point(537, 22);
    private static final Dimension DEFAULT_SIZE = new Dimension(406, 587);
    private static final String[] DEFAULT_IMAGE_SUFFIX = {".jpg"};
    private static final int TIMER_DELAY = 200;        // 200 msec 毎にチェック
    private static final int MAX_ESTIMATION = 5000;        // 全体の見積もり時間
    private static final String PROGRESS_NOTE = "画像をロードしています...";
    private final String title = "シェーマ箱";
    private String imageLocation = ClientContext.getLocation("schema");
    private PNSTabbedPane tabbedPane;
    private JButton refreshBtn;
    private int columnCount = DEFAULT_COLUMN_COUNT;
    private int imageWidth = DEFAULT_IMAGE_WIDTH;
    private int imageHeight = DEFAULT_IMAGE_HEIGHT;
    private String[] suffix = DEFAULT_IMAGE_SUFFIX;
    private MainFrame frame;
    private Logger logger;

    // SchemaBox でもメニューを出すため
    private MenuSupport mediator;
    private boolean isMac;

    @Override
    public void start() {
        logger = LoggerFactory.getLogger(ImageBox.class);
        isMac = ClientContext.isMac();

        initComponent();
        connect();
        setImageLocation(imageLocation);
    }

    // 多重起動しないための入り口
    @Override
    public void enter() {
        if (frame != null) {
            frame.setVisible(true);
        }
    }

    @Override
    public void stop() {
        frame.setVisible(false);
    }

    public MainFrame getFrame() {
        return frame;
    }

    public void toFront() {
        if (frame != null) {
            if (!frame.isVisible()) {
                frame.setVisible(true);
            }
            frame.toFront();
        }
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public void setImageLocation(String loc) {

        this.imageLocation = loc;
        String message = "シェーマ画像";
        Component c = null;

        Task<Void> task = new Task<Void>(c, message, PROGRESS_NOTE, MAX_ESTIMATION) {

            @Override
            protected Void doInBackground() {
                createImagePalettes();
                return null;
            }

            @Override
            protected void succeeded(Void result) {
                if (!frame.isVisible()) {
                    frame.setVisible(true);
                }
                logger.debug("Task succeeded");
            }
        };
        //task.setMillisToPopup(TIMER_DELAY);
        task.execute();
    }

    public void refresh() {

        final ImagePalette imageTable = (ImagePalette) tabbedPane.getSelectedComponent();
        String message = "シェーマ画像";
        Component c = this.getFrame();
        String note = "画像リストを更新しています";

        Task<Void> task = new Task<Void>(c, message, note, MAX_ESTIMATION) {

            @Override
            protected Void doInBackground() {
                imageTable.refresh();
                return null;
            }

            @Override
            protected void succeeded(Void result) {
                if (!frame.isVisible()) {
                    frame.setVisible(true);
                }
                logger.debug("Task succeeded");
            }
        };
        //task.setMillisToPopup(TIMER_DELAY);
        task.execute();
    }

    private void initComponent() {
        //
        // TabbedPane を生成する
        //
        tabbedPane = new PNSTabbedPane();
        tabbedPane.setButtonVgap(4);

        //
        // 更新ボタンを生成する
        //
        //refreshBtn = new JButton(GUIConst.ICON_ARROW_CIRCLE_DOUBLE_16);
        //refreshBtn.setText("更新");
        //refreshBtn.setHorizontalAlignment(SwingConstants.LEADING);
        //refreshBtn.putClientProperty("Quaqua.Button.style", "bevel");
        //refreshBtn.setFocusable(false);
        //refreshBtn.addActionListener(new ProxyAction(this::refresh));
        //refreshBtn.setToolTipText("シェーマリストを更新します");
        //JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // mac で SchemaBox にもメニューバーを出す
        if (isMac) {
            WindowSupport windowSupport = WindowSupport.create(title);
            frame = windowSupport.getFrame();
            javax.swing.JMenuBar myMenuBar = windowSupport.getMenuBar();
            mediator = new MenuSupport(this);
            MenuFactory appMenu = new MenuFactory();

            // mainWindow の menuSupport をセットしておけばメニュー処理は mainWindow がしてくれる
            appMenu.setMenuSupports(getContext().getMenuSupport(), mediator);
            appMenu.build(myMenuBar);
            mediator.registerActions(appMenu.getActionMap());
            mediator.disableAllMenus();
            String[] enables = new String[]{
                    GUIConst.ACTION_SHOW_STAMPBOX,
                    GUIConst.ACTION_SET_KARTE_ENVIROMENT,
                    GUIConst.ACTION_SHOW_WAITING_LIST,
                    GUIConst.ACTION_SHOW_PATIENT_SEARCH
            };
            mediator.enableMenus(enables);
        } else {
            frame = new MainFrame(title);
        }

        ComponentBoundsManager cm = new ComponentBoundsManager(frame, DEFAULT_LOC, DEFAULT_SIZE, this);
        cm.revertToPreferenceBounds();

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                processWindowClosing();
            }
        });

        // command panel, status panel は使わない
        frame.removeCommandPanel();
        frame.removeStatusPanel();
        // MainPanel に TabbedPane を挿入
        MainFrame.MainPanel mainPanel = frame.getMainPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void connect() {
    }

    public void createImagePalettes() {

        File baseDir = new File(imageLocation);
        if ((!baseDir.exists()) || (!baseDir.isDirectory())) {
            return;
        }

        File[] directories = listDirectories(baseDir);
        if (directories == null || directories.length == 0) {
            return;
        }
        Stream.of(directories).sorted(Comparator.comparing(File::getName)).forEach(dir -> {
            String tabName = dir.getName();
            ImagePalette imageTable = new ImagePalette(null, columnCount, imageWidth, imageHeight);
            imageTable.setImageSuffix(suffix);
            imageTable.setImageDirectory(dir);
            tabbedPane.addTab(tabName, imageTable);
        });
    }

    private File[] listDirectories(File dir) {
        DirectoryFilter filter = new DirectoryFilter();
        return dir.listFiles(filter);
    }

    public void processWindowClosing() {
        stop();
    }

    /**
     * @return Returns the columnCount.
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * @param columnCount The columnCount to set.
     */
    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * @return Returns the imageWidth.
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * @param imageWidth The imageWidth to set.
     */
    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    /**
     * @return Returns the imageHeight.
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * @param imageHeight The imageHeight to set.
     */
    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    /**
     * @return Returns the suffix.
     */
    public String[] getSuffix() {
        return suffix;
    }

    /**
     * @param suffix The suffix to set.
     */
    public void setSuffix(String[] suffix) {
        this.suffix = suffix;
    }

    private class DirectoryFilter implements FileFilter {
        @Override
        public boolean accept(File path) {
            return path.isDirectory();
        }
    }
}
