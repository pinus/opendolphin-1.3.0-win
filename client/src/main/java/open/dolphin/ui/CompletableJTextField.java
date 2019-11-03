package open.dolphin.ui;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

/**
 * CompletableJTextField.
 * modified from JAVA SWING HACKS.
 *
 * @author pns
 */
public class CompletableJTextField extends JTextField
        implements ListSelectionListener, FocusListener, KeyListener, ComponentListener, ActionListener {
    private static final long serialVersionUID = 1L;
    private static final String PREFS = "prefs";

    private Completer completer;
    private JList<String> completionList;
    private DefaultListModel<String> completionListModel;
    private JWindow listWindow;
    private Window parentFrame;
    private int keyCode;
    private Preferences prefs;
    private boolean isWin = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public CompletableJTextField(int col) {
        super(col);
        initComponents();
        connect();
    }

    private void initComponents() {
        completer = new Completer();
        completionListModel = new DefaultListModel<>();
        completionList = new JList<>(completionListModel);
        completionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        completionList.setBackground(new Color(255, 255, 240));

        listWindow = new JWindow();
        listWindow.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        if (isWin) {
            Border outer = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
            Border inner = BorderFactory.createEmptyBorder(0, 10, 10, 10);
            Border compound = BorderFactory.createCompoundBorder(outer, inner);
            completionList.setBorder(compound);
            listWindow.setOpacity(0.9f);
        } else {
            completionList.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            listWindow.setOpacity(0.7f);
        }
        listWindow.getContentPane().setLayout(new BorderLayout());
        listWindow.getContentPane().add(completionList, BorderLayout.CENTER);
    }

    private void connect() {
        completionList.addListSelectionListener(this);
        getDocument().addDocumentListener(completer);
        addFocusListener(this);
        addKeyListener(this);
        addComponentListener(this);
        addActionListener(this);
    }

    public void dispose() {
        listWindow.dispose();
    }

    /**
     * 補完した内容を保存する preferences をセットする.
     *
     * @param prefs Preferences
     */
    public void setPreferences(Preferences prefs) {
        this.prefs = prefs;
        loadPrefs();
    }

    private void savePrefs() {
        StringBuilder sb = new StringBuilder();
        List<String> items = getCompletions();
        items.forEach(s -> sb.append(s).append("\t"));

        prefs.put(PREFS, StringUtils.chop(sb.toString()));
    }

    private void loadPrefs() {
        String str = prefs.get(PREFS, "");
        String[] items = str.split("\t");
        // 古い方から登録
        for (int i = items.length - 1; i >= 0; i--) {
            if (!items[i].matches("^ *$")) { // 空行は除く
                completer.addCompletion(items[i]);
            }
        }
    }

    public void addCompletion(String s) {
        completer.addCompletion(s);
        if (prefs != null) {
            savePrefs();
        }
    }

    public void removeCompletion(String s) {
        completer.removeCompletion(s);
    }

    public void clearCompletions() {
        completer.clearCompletions();
    }

    private List<String> getCompletions() {
        return completer.getCompletions();
    }

    /**
     * 補完ウインドウを適切な場所に表示する.
     */
    private void showListWindow() {
        // figure out where the text field is,
        // and where its bottom left is
        java.awt.Point los = getLocationOnScreen();
        int popX = los.x;
        int popY = los.y + getHeight();
        listWindow.pack();
        int h = listWindow.getHeight();
        listWindow.setBounds(popX + 5, popY, getWidth() - 10, h);
        listWindow.setVisible(true);
    }

    /**
     * リストが選択されたときの処理.
     *
     * @param e ListSelectionEvent
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || completionList.getModel().getSize() == 0) {
            return;
        }

        final String completionString = completionList.getSelectedValue();
        if (completionString == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            // リストが選択されたら，選択された文字を text field に挿入
            // その間，completer には止まっていてもらう必要がある
            completer.setUpdate(false);
            setText(completionString);
            completer.setUpdate(true);
        });
    }

    /**
     * フォーカスを取ったら補完ウインドウを出す.
     *
     * @param e FocusEvent
     */
    @Override
    public void focusGained(FocusEvent e) {
        completer.buildAndShowPopup();
    }

    /**
     * フォーカスを失ったら補完ウインドウは消す.
     *
     * @param e FocusEvent
     */
    @Override
    public void focusLost(FocusEvent e) {
        listWindow.setVisible(false);
    }

    /**
     * Enter キー入力の動作.
     * リストが選択された状態でリターン＝リストの文字をフィールドにセット.
     *
     * @param e ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // 空行は無視
        if (getText().matches("^ *$")) {
            return;
        }

        // リストが選択されている時の enter の処理
        if (completionList.getSelectedIndex() != -1) {
            completer.setUpdate(true);
            completionList.getSelectionModel().clearSelection();
            listWindow.setVisible(false);
        }
        addCompletion(getText());
    }

    /**
     * キー入力を監視.
     * 上キー：選択を上へ，下キー：選択を下へ
     *
     * @param e KeyEvent
     */
    @Override
    public void keyPressed(KeyEvent e) {
        keyCode = e.getKeyCode();
        int modifier = e.getModifiers();

        // リストが表示されているとき
        if (listWindow.isVisible()) {
            int size = completionListModel.getSize();
            int selection = completionList.getSelectedIndex();

            switch (keyCode) {
                case KeyEvent.VK_UP:
                    if (selection > 0) {
                        selection--;
                        completionList.getSelectionModel().setSelectionInterval(selection, selection);
                    }
                    e.consume();
                    break;

                case KeyEvent.VK_DOWN:
                    if (selection < size - 1) {
                        selection++;
                        completionList.getSelectionModel().setSelectionInterval(selection, selection);
                    } else if (selection == -1) {
                        // ウインドウが表示されていて選択されていない状態
                        selection = 0;
                        completionList.getSelectionModel().setSelectionInterval(selection, selection);
                    }
                    e.consume();
                    break;

                case KeyEvent.VK_CLEAR: // 全クリア
                    clearCompletions();
                    e.consume();
                    break;

                case KeyEvent.VK_D: // Ctrl-D 1項目削除
                    int index = completionList.getSelectedIndex();
                    if (modifier == KeyEvent.CTRL_MASK && index != -1) {
                        String s = completionListModel.get(index);
                        removeCompletion(s);
                        e.consume();
                    }
                    break;

                case KeyEvent.VK_ESCAPE:
                    listWindow.setVisible(false);
                    e.consume();
                    break;

                default:
                    break;
            }
        } else {
            // リストが表示されていないとき，下キーで候補を全部出す
            if (keyCode == KeyEvent.VK_DOWN && getText().length() < 1 && !getCompletions().isEmpty()) {
                completer.showAll();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // window が動いたら listWindow は消す
    @Override
    public void componentResized(ComponentEvent e) {
        if (parentFrame == null) {
            parentFrame = SwingUtilities.getWindowAncestor(this);
            removeComponentListener(this);
            parentFrame.addComponentListener(this);
        }
        listWindow.setVisible(false);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        listWindow.setVisible(false);
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        listWindow.setVisible(false);
    }

    /**
     * inner class does the matching of the JTextField's
     * document to completion strings kept in an ArrayList.
     */
    private class Completer implements DocumentListener {

        private final List<String> completions;
        private boolean update = true;
        private Pattern pattern;

        public Completer() {
            completions = new ArrayList<>();
        }

        public void addCompletion(String s) {
            // 新しく追加したものが最初に来る
            completions.remove(s);
            completions.add(0, s);
            // 50項目まで保存
            if (completions.size() > 50) {
                completions.remove(50);
            }

            buildAndShowPopup();
        }

        public void removeCompletion(String s) {
            completions.remove(s);
            buildAndShowPopup();
        }

        public void clearCompletions() {
            completions.clear();
            buildPopup();
            listWindow.setVisible(false);
        }

        public List<String> getCompletions() {
            return Collections.unmodifiableList(completions);
        }

        public void setUpdate(boolean b) {
            update = b;
        }

        private void buildPopup() {
            completionListModel.clear();
            //System.out.println("buildPopup for " + completions.size() + " completions");
            //pattern = Pattern.compile(getText() + ".*");
            pattern = Pattern.compile(getText() + ".+");

            for (String s : completions) {
                if (pattern.matcher(s).matches()) {
                    completionListModel.add(completionListModel.getSize(), s);
                }
            }
            //completions.stream().filter(completion -> pattern.matcher(completion).matches())
            //        .forEachOrdered(completion -> completionListModel.add(completionListModel.getSize(), completion));
        }

        private void showPopup() {
            if (completionListModel.isEmpty()) {
                listWindow.setVisible(false);
            } else if (isShowing()) {
                showListWindow();
            }
        }

        private void buildAndShowPopup() {
            if (!update) {
                return;
            }

            if (getText().length() < 1) {
                listWindow.setVisible(false);
                return;
            }
            buildPopup();
            showPopup();
        }

        private void showAll() {
            // complitionListModel に completions 全部入れる
            completionListModel.clear();
            completions.forEach(completion -> completionListModel.add(completionListModel.getSize(), completion));
            showPopup();
        }

        // DocumentListener implementation
        @Override
        public void insertUpdate(DocumentEvent e) {
            buildAndShowPopup();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            buildAndShowPopup();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            buildAndShowPopup();
        }

    }

    public static void main(String[] argv) {
        CompletableJTextField completableField = new CompletableJTextField(30);

        // 履歴を保存するための pref を作る
        String prefKey = CompletableJTextField.class.getName() + ".pref";
        System.out.println("pref key = " + prefKey);
        Preferences userRoot = Preferences.userRoot();
        Preferences prefs = userRoot.node(prefKey);
        completableField.setPreferences(prefs);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(300, 300, 300, 300);
        JPanel p = new JPanel();
        frame.add(p);
        p.add(completableField);
        frame.pack();

        frame.setVisible(true);
    }
}
