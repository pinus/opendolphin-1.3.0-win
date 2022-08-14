package open.dolphin.client;

import open.dolphin.JsonConverter;
import open.dolphin.ui.sheet.JSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 編集中の KafrteEditor を定期的に一時保存する.
 *
 * @author pns
 */
public class Autosave implements Runnable {
    private final static int INTERVAL = 2;
    private final static int INITIAL_DELAY = 2;
    private final static String SUFFIX = "open.dolphin";
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private final KarteEditor editor;
    // Logger
    private final Logger logger = LoggerFactory.getLogger(Autosave.class);
    private AutosaveModel autosaveModel;
    private File tmpFile;
    private boolean dirty = true;
    // 自動セーブのタイマー
    private ScheduledExecutorService executor;

    public Autosave(KarteEditor e) {
        editor = e;
        autosaveModel = new AutosaveModel();
    }

    /**
     * pid と doc id から temporary file を作る.
     *
     * @param patientId PatientId
     * @param docId     DocID
     * @return File
     */
    private static File getTemporaryFile(String patientId, String docId) {
        String path = String.format("%s%s-%s.%s", TMP_DIR, patientId, docId, SUFFIX);
        return new File(path);
    }

    /**
     * 保存された Temporary file を読み込んで AutosaveModel を生成する.
     *
     * @return list of AutosaveModel
     */
    private static List<AutosaveModel> load() {

        File dir = new File(TMP_DIR);

        return Stream.of(dir.listFiles(fn -> fn.getName().endsWith(SUFFIX))).map(f -> {
            StringBuilder str = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String s;
                while ((s = br.readLine()) != null) {
                    str.append(s);
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
            return JsonConverter.fromJson(str.toString(), AutosaveModel.class);
        }).collect(Collectors.toList());
    }

    /**
     * 保存されていない編集中カルテをチェックして編集するかどうか決める.
     *
     * @param chart ChartImpl
     */
    public static void checkForTemporaryFile(final ChartImpl chart) {

        String chartPid = chart.getKarte().getPatient().getPatientId();

        List<AutosaveModel> targetModels = load().stream()
                .filter(m -> chartPid.equals(m.getPatientId()))
                .collect(Collectors.toList());

        if (targetModels.isEmpty()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {

            String message = "保存されていない編集中カルテがみつかりました. \nエディタで開きますか?";
            String[] selection = {"開く", "破棄"};

            int opt = JSheet.showOptionDialog(chart.getFrame(), message, "保存されていないカルテ",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, selection, selection[0]);

            switch (opt) {
                case JOptionPane.YES_OPTION:

                    targetModels.stream().map(a -> {
                        a.composeDocumentModel();
                        return a.getDocumentModel();

                    }).forEach(m -> {
                        KarteEditor editor = chart.createEditor();
                        editor.setDocument(m);

                        editor.setEditable(true);
                        editor.setModify(true);

                        EditorFrame editorFrame = new EditorFrame();
                        editorFrame.setChart(chart);
                        editorFrame.setKarteEditor(editor);
                        editorFrame.start();

                        editor.setDirty(true);
                    });
                    break;

                case JOptionPane.NO_OPTION:
                case JOptionPane.CLOSED_OPTION: // ESC key

                    targetModels.stream()
                            .map(a -> getTemporaryFile(a.getPatientId(), a.getDocumentModel().getDocInfo().getDocId()))
                            .forEach(File::delete);
                    break;
            }
        });
    }

    /**
     * 編集中カルテの記録を開始する.
     */
    public void start() {
        String patientId = editor.getContext().getPatient().getPatientId();
        String docId = editor.getDocument().getDocInfo().getDocId();

        tmpFile = getTemporaryFile(patientId, docId);
        logger.info(String.valueOf(tmpFile));

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this, INITIAL_DELAY, INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 編集中カルテの記録を終了し, TemporaryFile を消去する.
     */
    public void stop() {
        executor.shutdownNow();
        tmpFile.delete();
    }

    /**
     * KarteEditor から Dirty 情報を受け取る.
     *
     * @param newDirty new dirty flag
     */
    public void setDirty(boolean newDirty) {
        dirty = newDirty;
    }

    /**
     * 編集中の KarteEditor を AutosaveModel にしてファイル保存する.
     */
    @Override
    public void run() {

        if (dirty) {
            long l = System.currentTimeMillis();

            autosaveModel.dump(editor);

            String json = JsonConverter.toJson(autosaveModel);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile))) {
                bw.write(json);

            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }

            dirty = false;
            logger.debug("autosave took " + (System.currentTimeMillis() - l) + " ms");
        }
    }
}
