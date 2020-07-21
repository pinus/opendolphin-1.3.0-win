package open.dolphin.inspector;

import open.dolphin.client.ChartImpl;
import open.dolphin.client.CompositeArea;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.DBTask;
import open.dolphin.helper.ScriptExecutor;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PatientMemoModel;
import open.dolphin.project.Project;
import open.dolphin.ui.PNSScrollPane;
import open.dolphin.ui.sheet.JSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.FocusManager;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Date;

/**
 * 患者のメモを表示し編集するクラス.
 * タイトル部分に関連文書情報を表示し，クリックで関連文書フォルダを開く機能も持つ.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class MemoInspector implements IInspector {
    public static final InspectorCategory CATEGORY = InspectorCategory.メモ;

    private static final Color[] ALERT_LINE_COLOR = {new Color(255, 100, 100), new Color(255, 130, 130), new Color(255, 180, 180)};
    private static final Color ALERT_BACK_COLOR = new Color(255, 240, 240);

    // jpeg ファイルフィルタ
    private static final FileFilter FF_JPG = file -> file.getName().toLowerCase().endsWith(".jpg");
    // 検査 ファイルフィルタ
    private static final FileFilter FF_EXAM = file -> file.getName().contains("検査");
    // 添書 ファイルフィルタ
    private static final FileFilter FF_LETTER = file -> file.getName().contains("紹介")
            | file.getName().contains("返事") | file.getName().contains("手紙");
    // 代替処方 ファイルフィルタ
    private static final FileFilter FF_ALTDRUG = file -> file.getName().contains("代替");

    private final ChartImpl context;
    private final Logger logger;
    // このカルテの関連情報ファイルのパス
    private final String path;
    private JPanel memoPanel;
    private CompositeArea memoArea;
    private PatientMemoModel patientMemoModel;
    private InspectorBorder border;
    private String titleText;
    private Font titleFont;
    private Color titleColor;
    private String oldText = "";
    private boolean shouldAlert = false;

    /**
     * MemoInspectorオブジェクトを生成する.
     *
     * @param parent PatientInspector
     */
    public MemoInspector(PatientInspector parent) {
        context = parent.getContext();
        logger = LoggerFactory.getLogger(MemoInspector.class);
        path = FileInspector.getDocumentPath(context.getKarte().getPatient().getPatientId());
        initComponents();
    }

    /**
     * GUI コンポーネントを初期化する.
     */
    private void initComponents() {

        memoArea = new CompositeArea(5, 10);
        memoArea.setName(CATEGORY.name());
        memoArea.setLineWrap(true);
        memoArea.setMargin(new java.awt.Insets(3, 3, 2, 2));
        memoArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

        // Tab でフォーカス移動
        InputMap im = memoArea.getInputMap();
        ActionMap am = memoArea.getActionMap();
        im.put(KeyStroke.getKeyStroke("TAB"), "focusNext");
        am.put("focusNext", new ProxyAction(FocusManager.getCurrentManager()::focusNextComponent));
        im.put(KeyStroke.getKeyStroke("shift TAB"), "focusPrevious");
        am.put("focusPrevious", new ProxyAction(FocusManager.getCurrentManager()::focusPreviousComponent));

        // isReadOnly対応
        memoArea.setEnabled(!context.isReadOnly());

        AttentiveViewport vport = new AttentiveViewport();
        vport.setView(memoArea);

        PNSScrollPane pane = new PNSScrollPane();
        pane.setViewport(vport);
        pane.putClientProperty("JComponent.sizeVariant", "small");

        memoPanel = new JPanel(new BorderLayout());
        memoPanel.setName(CATEGORY.name());

        memoPanel.add(pane, BorderLayout.CENTER);
        memoPanel.setMinimumSize(new Dimension(DEFAULT_WIDTH, isWin() ? 120 : 70));
        memoPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, isWin() ? 120 : 100));

        // kick AppleScript to open target folder
        memoPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                File infoFolder = new File(path);
                if (infoFolder.exists()) {
                    // folder があれば開く
                    ScriptExecutor.openPatientFolder(path);

                } else {
                    // なければダイアログを出してフォルダを作ってから表示
                    int ans = JSheet.showConfirmDialog(context.getFrame(), "関連文書フォルダを作りますか？", "",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                    if (ans == JOptionPane.OK_OPTION) {
                        infoFolder.mkdir();
                        ScriptExecutor.openPatientFolder(path);
                        // MemoInspector 表示の update
                        createTitle();
                        border.setTitle(titleText);
                        memoPanel.repaint();
                    }
                }
            }
        });
    }

    /**
     * レイアウト用のパネルを返す.
     *
     * @return レイアウトパネル
     */
    @Override
    public JPanel getPanel() {
        return memoPanel;
    }

    @Override
    public String getName() {
        return CATEGORY.name();
    }

    @Override
    public String getTitle() {
        return CATEGORY.title();
    }

    public JTextArea getMemoArea() { return memoArea; }

    /**
     * 関連文書に応じた Border を返す.
     *
     * @return Border
     */
    @Override
    public Border getBorder() {
        // もし関連文書(/Volumes/documents/.../${患者id}）があれば，メモタイトルを変える
        createTitle();

        border = new InspectorBorder(titleText);
        return border;
    }

    /**
     * memo title を作ってフィールド変数にセットする.
     */
    private void createTitle() {
        File infoFolder = new File(path);

        StringBuilder memoTitle = new StringBuilder();

        // 情報ファイルのフォルダがあるかどうか
        if (infoFolder.exists()) {

            if (infoFolder.listFiles(FF_JPG).length > 0) {
                memoTitle.append("写真・");
            }
            if (infoFolder.listFiles(FF_EXAM).length > 0) {
                memoTitle.append("検査・");
            }
            if (infoFolder.listFiles(FF_LETTER).length > 0) {
                memoTitle.append("添書・");
            }
            if (infoFolder.listFiles(FF_ALTDRUG).length > 0) {
                memoTitle.append("代替報告・");
            }
            if (memoTitle.length() > 0) {
                // 最後の「・」を取る
                memoTitle.deleteCharAt(memoTitle.length() - 1);
            } else {
                memoTitle.append("ファイル");
            }

            memoTitle.append("あり");

            titleColor = Color.blue;
            titleFont = new Font(Font.SANS_SERIF, Font.BOLD, isWin() ? 10 : 12);

        } else {
            // フォルダがない
            memoTitle.append(InspectorCategory.メモ.title());
            memoTitle.append("  [関連文書フォルダ作成]");
            titleColor = Color.BLACK;
            titleFont = null;
        }

        titleText = memoTitle.toString();
    }

    /**
     * 患者メモを表示する.
     */
    @Override
    public void update() {
        patientMemoModel = context.getKarte().getPatientMemo();
        if (patientMemoModel != null) {
            memoArea.setText(patientMemoModel.getMemo());
            memoArea.setCaretPosition(memoArea.getDocument().getLength());
            oldText = memoArea.getText();

            // 注意事項がある場合，赤い枠で注意を促す
            shouldAlert = containsContraindication();
            memoPanel.repaint();
        }
        // undo 記録開始
        memoArea.getUndoManager().discardAllEdits();
    }

    /**
     * 患者メモをサーバに保存する.
     */
    public void save() {
        // メモ内容に変更がなければ何もしない
        if (oldText.equals(memoArea.getText().trim())) {
            return;
        }

        if (patientMemoModel == null) {
            patientMemoModel = new PatientMemoModel();
        }
        // 上書き更新
        Date confirmed = new Date();
        patientMemoModel.setKarte(context.getKarte());
        patientMemoModel.setCreator(Project.getUserModel());
        patientMemoModel.setConfirmed(confirmed);
        patientMemoModel.setRecorded(confirmed);
        patientMemoModel.setStarted(confirmed);
        patientMemoModel.setStatus(IInfoModel.STATUS_FINAL);
        patientMemoModel.setMemo(memoArea.getText().trim());

        DBTask<Void> task = new DBTask<Void>(context) {

            @Override
            protected Void doInBackground() {
                logger.debug("updateMemo doInBackground");
                DocumentDelegater ddl = new DocumentDelegater();
                ddl.updatePatientMemo(patientMemoModel);
                return null;
            }

            @Override
            protected void succeeded(Void result) {
                logger.debug("updateMemo succeeded");
            }
        };

        task.execute();
    }

    /**
     * メモ内容に禁忌等の注意事項があるかどうか.
     *
     * @return contains or not
     */
    public boolean containsContraindication() {
        String text = memoArea.getText();
        return text.contains("禁")
                || text.contains("注意")
                || text.contains("発疹")
                || text.contains("皮疹")
                || text.contains("薬疹")
                || text.contains("アレルギ")
                || text.contains("ショック")
                || text.contains("アナフィラ");
    }

    /**
     * ViewPort に赤枠を付ける.
     */
    private class AttentiveViewport extends JViewport {
        private static final long serialVersionUID = 1L;

        public void setView(JComponent c) {
            c.setOpaque(false);
            super.setView(c);
        }

        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics g = graphics.create();
            if (shouldAlert) {
                g.setColor(ALERT_BACK_COLOR);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 3, 3);

                for (int i = 0; i < 3; i++) {
                    g.setColor(ALERT_LINE_COLOR[i]);
                    g.drawRoundRect(i, i, getWidth() - 1 - 2 * i, getHeight() - 1 - 2 * i, 3 - i, 3 - i);
                }
            } else {
                g.setColor(Color.WHITE);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 3, 3);
            }
            g.dispose();
        }
    }
}
