package open.dolphin.inspector;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import open.dolphin.client.ChartImpl;
import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.ui.ExecuteScript;
import open.dolphin.ui.MyJScrollPane;
import open.dolphin.util.StringTool;
import org.apache.log4j.Logger;

/**
 * 関連文書ファイルを表示するクラス.
 * @author pns
 */
public class FileInspector {

    private JPanel filePanel;
    private JList<File> list;
    private final ChartImpl context;
    public static final String NAME = "fileInspector";
    private DefaultListModel<File> model;
    private FileFilter filter;
    private static final String DEFAULT_DOCUMENT_FOLDER = "/Volumes/documents/";

    private final Logger logger;

    /**
     * MemoInspectorオブジェクトを生成する.
     * @param context
     */
    public FileInspector(ChartImpl context) {

        this.context = context;
        logger = ClientContext.getBootLogger();
        initComponents();
    }

    /**
     * 患者 id から，関連文書の path を作る.
     * <pre>
     * 関連文書ファイル構造
     *   １万台フォルダ/千台フォルダ/関連文書
     *   000001-010000/000001-001000/${id}
     *   000001-010000/001001-002000/${id}
     *         :
     *   010001-020000/010001-011000/${id}
     *         :
     * </pre>
     * @param id
     * @return
     */
    public static String getDocumentPath(String id) {
        // id の範囲によって subfolder を決める
        int index10k = (Integer.parseInt(id) - 1) / 10000; // 上位2桁
        int index1k = (Integer.parseInt(id) - 1) / 1000; // 上位3桁

        String subfolder = String.format("%02d0001-%02d0000/%03d001-%03d000/",
                index10k, (index10k+1), index1k, (index1k+1));

        return DEFAULT_DOCUMENT_FOLDER + subfolder + id;
    }

    /**
     * GUI コンポーネントを初期化する.
     */
    private void initComponents() {

        model = new DefaultListModel<>();
        list = new JList<>(model);
        list.putClientProperty("Quaqua.List.style", "striped");
        list.setCellRenderer(new FileListCellRenderer());
        list.setFixedCellHeight(GUIConst.DEFAULT_LIST_ROW_HEIGHT);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new FileSelectionListener());

        filePanel = new JPanel(new BorderLayout());

        final MyJScrollPane scrollPane = new MyJScrollPane(list);
        scrollPane.putClientProperty("JComponent.sizeVariant", "small");
        filePanel.add(scrollPane);

        // Hidden ファイルフィルタ
        filter = file -> !file.getName().startsWith(".");

        update();
    }

    /**
     * PaientInspector にレイアウト用のパネルを返す.
     * @return レイアウトパネル
     */
    public JPanel getPanel() {
        return filePanel;
    }

    /**
     * データのアップデート.
     */
    public void update() {
        //String path = DEFAULT_DOCUMENT_FOLDER + context.getKarte().getPatient().getPatientId();
        File infoFolder = new File (getDocumentPath(context.getKarte().getPatient().getPatientId()));
        File[] files = infoFolder.listFiles(filter);
        if (files == null) { return; }

        Arrays.sort(files, new FileNameComparator());

        for (File file : files) {
            model.addElement(file);
        }
    }

    /**
     * 選択されたら，QuickLook で文書を見る.
     */
    private class FileSelectionListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            Point mousePoint = e.getPoint();
            int index = list.locationToIndex(mousePoint);
            if (index == -1) { return; }

            Point indexPoint = list.indexToLocation(index);
            // あまりマウスが離れたところをクリックしてたらクリアする
            if (indexPoint != null && Math.abs((indexPoint.y+6) - mousePoint.y) > 12) {
                list.clearSelection();
            } else {
                String path = model.getElementAt(index).getPath();
                ExecuteScript.quickLook(path);
            }
        }
    }

    private class FileListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(
            JList list,              // the list
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)  { // does the cell have focus

            if (isSelected) {
                this.setForeground(list.getSelectionForeground());
                this.setBackground(list.getSelectionBackground());
            } else {
                this.setForeground(list.getForeground());
                this.setBackground(list.getBackground());
            }

            String fileName = ((File)value).getName();
            this.setText(" " + fileName);
            //this.setText(" " + fileName + " " + ModelUtils.getDateAsString(new java.util.Date(((File)value).lastModified())));
            return this;
        }
    }

    /**
     * file name comparator.
     * File2, File10 などの数字付きのファイル名が正しくソートできるようにする.
     */
    private class FileNameComparator implements Comparator<File> {

        // 紹介状15.ods の '15.' を切り出すパターン
        private final Pattern NUMBER_PLUS_DOT = Pattern.compile("[0-9]+\\.");

        @Override
        public int compare(File o1, File o2) {
            // 全角数字だった場合，半角数字に変換してから比較する
            String name1 = StringTool.toHankakuNumber(o1.getName());
            String name2 = StringTool.toHankakuNumber(o2.getName());

            Matcher m1 = NUMBER_PLUS_DOT.matcher(name1);
            Matcher m2 = NUMBER_PLUS_DOT.matcher(name2);

            // 両方に '数字.' のパターンがみつかった
            if (m1.find() && m2.find()) {
                // 数字部分の文字列
                String n1 = m1.group();
                String n2 = m2.group();
                // 長い方に桁をそろえる
                int len = Math.max(n1.length(), n2.length());
                n1 = adjustDigit(n1, len);
                n2 = adjustDigit(n2, len);

                name1 = m1.replaceFirst(n1);
                name2 = m2.replaceFirst(n2);
            }

            return name2.compareTo(name1);
        }

        /**
         * 先頭に 0 をつけて，桁数を n 桁にする
         * @param str
         * @param n
         * @return
         */
        private String adjustDigit(String str, int n) {
            StringBuilder sb = new StringBuilder();
            for(int i=n; i> str.length(); i--) { sb.append('0'); }
            sb.append(str);
            return sb.toString();
        }
    }

    public static void main (String[] arg) {
        System.out.println(getDocumentPath("000125"));
    }
}
