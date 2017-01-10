package open.dolphin.inspector;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileFilter;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import open.dolphin.client.ChartImpl;
import open.dolphin.client.ClientContext;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.project.Project;
import open.dolphin.ui.HorizontalPanel;
import open.dolphin.ui.PNSBadgeTabbedPane;
import open.dolphin.ui.PNSBorderFactory;

/**
 * 各々の Inspecter を生成して配置する.
 * @author kazm
 * @author pns
 */
public class PatientInspector {

    public static final int DEFAULT_WIDTH = ClientContext.isMac()? 280 : 260;
    public static final int DEFAULT_HEIGHT = ClientContext.isMac()? 175 : 178;

    // 個々のインスペクタ
    // 患者基本情報
    private BasicInfoInspector basicInfoInspector;
    // 来院歴
    private PatientVisitInspector patientVisitInspector;
    // 患者メモ
    private MemoInspector memoInspector;
    // 文書履歴
    private DocumentHistory docHistory;
    // アレルギ
    private AllergyInspector allergyInspector;
    // 身長体重
    private PhysicalInspector physicalInspector;
    // 病名インスペクタ
    private DiagnosisInspector diagnosisInspector;
    // 関連文書インスペクタ
    private FileInspector fileInspector;
    // DocumentHistory インスペクタを格納するタブペイン. ６個目以降のインスペクタはここに追加される.
    private PNSBadgeTabbedPane tabbedPane;
    // このクラスのコンテナパネル
    private JPanel container;
    // Context このインスペクタの親コンテキスト
    private ChartImpl context;

    // 優先される 5つに入ったかどうか. これが false ならタブに格納.
    private boolean bMemo;
    private boolean bAllergy;
    private boolean bPhysical;
    private boolean bCalendar;
    private boolean bDiagnosis;
    private boolean bFile;

    /**
     * 患者インスペクタクラスを生成する.
     * @param context インスペクタの親コンテキスト
     */
    public PatientInspector(ChartImpl context) {
        // このインスペクタが格納される Chart Object
        setContext(context);
        // GUI を初期化する
        initComponents();
    }

    private void initComponents() {

        // Preference に保存されているインスペクタの順番
        String topInspector = Project.getPreferences().get("topInspector", InspectorCategory.メモ.name()); //0"メモ"
        String secondInspector = Project.getPreferences().get("secondInspector", InspectorCategory.病名.name()); //5"病名"
        String thirdInspector = Project.getPreferences().get("thirdInspector", InspectorCategory.カレンダー.name()); //1"カレンダ"
        String forthInspector = Project.getPreferences().get("forthInspector", InspectorCategory.文書履歴.name()); //2"文書履歴"
        String fifthInspector = Project.getPreferences().get("fifthInspector", InspectorCategory.アレルギー.name()); //3"アレルギー"

        // 各インスペクタを生成する
        basicInfoInspector = new BasicInfoInspector(context);
        memoInspector = new MemoInspector(context);
        patientVisitInspector = new PatientVisitInspector(context);
        docHistory = new DocumentHistory(getContext());
        allergyInspector = new AllergyInspector(context);
        physicalInspector = new PhysicalInspector(context);
        diagnosisInspector = new DiagnosisInspector(context);
        fileInspector = new FileInspector(context);

        // タブパネル
        tabbedPane = new PNSBadgeTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        // docHistory は必ずタブに入れる
        tabbedPane.addTab(InspectorCategory.文書履歴.title(), docHistory.getPanel());

        // 全体の container
        container = new HorizontalPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // 左側のレイアウトを行う
        layoutRow(container, topInspector);
        layoutRow(container, secondInspector);
        layoutRow(container, thirdInspector);
        layoutRow(container, forthInspector);
        layoutRow(container, fifthInspector);

        // 左側にレイアウトされなかったものをタブに格納する
        if (!bMemo) {
            tabbedPane.addTab(memoInspector.getTitle(), memoInspector.getPanel());
        }

        if (!bCalendar) {
            tabbedPane.addTab(patientVisitInspector.getTitle(), patientVisitInspector.getPanel());
        }

        if (!bAllergy) {
            tabbedPane.addTab(allergyInspector.getTitle(), allergyInspector.getPanel());
        }

        if (!bPhysical) {
            tabbedPane.addTab(physicalInspector.getTitle(), physicalInspector.getPanel());
        }

        if (!bDiagnosis) {
            tabbedPane.addTab(diagnosisInspector.getTitle(), diagnosisInspector.getPanel());
        }
        if (!bFile) {
            tabbedPane.addTab(fileInspector.getTitle(), fileInspector.getPanel());
        }

        // BadgeListener
        if (!bFile) {
            fileInspector.addBadgeListener(tabbedPane::setBadge, tabbedPane.getTabCount()-1);
        }

        memoInspector.update();
        basicInfoInspector.update();
        fileInspector.update();
        allergyInspector.update();
        physicalInspector.update();
        patientVisitInspector.update();
        docHistory.update();
        diagnosisInspector.update();

        Dimension d = container.getMinimumSize();
        d.width = DEFAULT_WIDTH;
        container.setMinimumSize(d);
    }

    /**
     * content に inspector をレイアウトする.
     * @param content
     * @param itype
     */
    private void layoutRow(JPanel content, String itype) {

        if (itype.equals(InspectorCategory.メモ.name())) { //"メモ"

            // もし関連文書(/Volumes/documents/${患者id}）があれば，メモタイトルを変える
            final String path = FileInspector.getDocumentPath(context.getKarte().getPatient().getPatientId());
            File infoFolder = new File (path);

            // jpeg ファイルフィルタ
            FileFilter ffJpg = file -> file.getName().toLowerCase().endsWith(".jpg");
            // 検査 ファイルフィルタ
            FileFilter ffExam = file -> file.getName().contains("検査");
            // 添書 ファイルフィルタ
            FileFilter ffLetter = file -> file.getName().contains("紹介") | file.getName().contains("返事") | file.getName().contains("手紙");
            // 代替処方 ファイルフィルタ
            FileFilter ffAltDrug = file -> file.getName().contains("代替");

            StringBuilder memoTitle = new StringBuilder();
            Color mColor = null;
            Font mFont = null;

            // 情報ファイルのフォルダがあるかどうか
            if (infoFolder.exists()) {
                boolean miscellaneous = true;

                if (infoFolder.listFiles(ffJpg).length > 0) {
                    if (!memoTitle.toString().equals("")) { memoTitle.append("・"); }
                    memoTitle.append("写真");
                    miscellaneous = false;
                }
                if (infoFolder.listFiles(ffExam).length > 0) {
                    if (!memoTitle.toString().equals("")) { memoTitle.append("・"); }
                    memoTitle.append("検査");
                    miscellaneous = false;
                }
                if (infoFolder.listFiles(ffLetter).length > 0) {
                    if (!memoTitle.toString().equals("")) { memoTitle.append("・"); }
                    memoTitle.append("添書");
                    miscellaneous = false;
                }
                if (infoFolder.listFiles(ffAltDrug).length > 0) {
                    if (!memoTitle.toString().equals("")) { memoTitle.append("・"); }
                    memoTitle.append("代替報告");
                    miscellaneous = false;
                }
                if (miscellaneous) {
                    if (!memoTitle.toString().equals("")) { memoTitle.append("・"); }
                    memoTitle.append("ファイル");
                }

                memoTitle.append("あり");
                if (mColor == null) { mColor = Color.blue; }
                mFont = new Font(Font.SANS_SERIF,Font.BOLD,12);


            } else {
                // フォルダがない
                if (memoTitle.toString().equals("")) {
                    memoTitle.append(InspectorCategory.メモ.title());
                    mColor = Color.BLACK;
                    mFont = null;
                }
            }

            memoInspector.getPanel().setBorder(PNSBorderFactory.createTitledBorder(
                    null, memoTitle.toString(), TitledBorder.LEFT, TitledBorder.TOP, mFont, mColor));

            content.add(memoInspector.getPanel());
            bMemo = true;

        } else if (itype.equals(InspectorCategory.カレンダー.name())) { //"カレンダ"
            patientVisitInspector.getPanel().setBorder(patientVisitInspector.getBorder());
            content.add(patientVisitInspector.getPanel());
            bCalendar = true;

        } else if (itype.equals(InspectorCategory.文書履歴.name())) { //"文書履歴"
            content.add(tabbedPane);

        } else if (itype.startsWith(InspectorCategory.アレルギー.name())) { //"アレルギ"
            allergyInspector.getPanel().setBorder(allergyInspector.getBorder());
            content.add(allergyInspector.getPanel());
            bAllergy = true;

        } else if (itype.equals(InspectorCategory.身長体重.name())) { // "身長体重"
            physicalInspector.getPanel().setBorder(physicalInspector.getBorder());
            content.add(physicalInspector.getPanel());
            bPhysical = true;
        }

        else if (itype.equals(InspectorCategory.病名.name())) { // "病名"
            diagnosisInspector.getPanel().setBorder(diagnosisInspector.getBorder());
            content.add(diagnosisInspector.getPanel());
            bDiagnosis = true;

        } else if (itype.equals(InspectorCategory.関連文書.name())) { // "関連文書"
            fileInspector.getPanel().setBorder(fileInspector.getBorder());
            content.add(fileInspector.getPanel());
            bFile = true;
        }
    }

    /**
     * コンテキストを返す.
     * @return
     */
    public ChartImpl getContext() {
        return context;
    }

    /**
     * コンテキストを設定する.
     */
    private void setContext(ChartImpl context) {
        this.context = context;
    }

    /**
     * 患者カルテを返す.
     * @return  患者カルテ
     */
    public KarteBean getKarte() {
        return context.getKarte();
    }

    /**
     * 患者を返す.
     * @return 患者
     */
    public PatientModel getPatient() {
        return context.getKarte().getPatient();
    }

    /**
     * 基本情報インスペクタを返す.
     * @return 基本情報インスペクタ
     */
    public BasicInfoInspector getBasicInfoInspector() {
        return basicInfoInspector;
    }

    /**
     * 来院歴インスペクタを返す.
     * @return 来院歴インスペクタ
     */
    public PatientVisitInspector getPatientVisitInspector() {
        return patientVisitInspector;
    }

    /**
     * 患者メモインスペクタを返す.
     * @return 患者メモインスペクタ
     */
    public MemoInspector getMemoInspector() {
        return memoInspector;
    }

    /**
     * 文書履歴インスペクタを返す.
     * @return 文書履歴インスペクタ
     */
    public DocumentHistory getDocumentHistory() {
        return docHistory;
    }

    /**
     * 病名インスペクタを返す.
     * @return
     */
    public DiagnosisInspector getDiagnosisInspector() {
        return diagnosisInspector;
    }

    /**
     * 病名インスペクタを返す.
     * @return
     */
    public FileInspector getFileInspector() {
        return fileInspector;
    }

    /**
     * レイアウトのためにインスペクタのコンテナパネルを返す.
     * @return インスペクタのコンテナパネル
     */
    public JPanel getPanel() {
        return container;
    }

    /**
     * 終了処理.
     */
    public void dispose() {
        // List をクリアする
        //docHistory.clear();
        //allergyInspector.clear();
        //physicalInspector.clear();

        // memo 欄の自動セーブ
        memoInspector.save();
    }
}
