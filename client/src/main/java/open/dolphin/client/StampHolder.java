package open.dolphin.client;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Position;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.order.StampEditorDialog;
import open.dolphin.project.Project;
import open.dolphin.ui.MyBorderFactory;
import open.dolphin.util.PreferencesUtils;
import open.dolphin.util.StringTool;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * KartePane に Component　として挿入されるスタンプを保持するクラス。
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc. modified by pns
 */
public final class StampHolder extends AbstractComponentHolder implements ComponentHolder {
    private static final long serialVersionUID = 5853431116398862958L;

    public static final String STAMP_MODIFIED = "stampModified";

    private static final Color FOREGROUND = new Color(20, 20, 140);
    private static final Color BACKGROUND = new Color(0,0,0,0);
    private static final Color COMMENT_COLOR = new Color(120, 20, 140);

    private ModuleModel stamp;
    private StampRenderingHints hints;
    private final KartePane kartePane;
    private Position start;
    private Position end;
    private boolean selected;

    private static final Border MY_SELECTED_BORDER = MyBorderFactory.createSelectedBorder();
    private static final Border MY_CLEAR_BORDER = MyBorderFactory.createClearBorder();

    // 検索語にマークする
    private String searchText = null;
    private String startTag = null;
    private String endTag = null;

    public StampHolder(final KartePane kartePane, final ModuleModel stamp) {
        super();

        setForeground(FOREGROUND);
        setBackground(BACKGROUND);
        setBorder(MY_CLEAR_BORDER);

        this.kartePane = kartePane;
        this.hints = new StampRenderingHints();
        hints.setCommentColor(COMMENT_COLOR);

        // スタンプの初期幅は ChartImpl の幅から決定する
        Rectangle bounds = PreferencesUtils.getRectangle(Project.getPreferences(), ChartImpl.PN_FRAME, new Rectangle(0,0,0,0));
        int w = (bounds.width + 1)/2 - 168; // 実験から連立方程式で求めた
        hints.setWidth((w<320)? 320 : w);

        setStamp(stamp);
    }

    /**
     * Focusされた場合のメニュー制御とボーダーを表示する。
     * @param map
     */
    @Override
    public void enter(ActionMap map) {

        map.get(GUIConst.ACTION_COPY).setEnabled(true);

        if (kartePane.getTextPane().isEditable()) {
            map.get(GUIConst.ACTION_CUT).setEnabled(true);
        } else {
            map.get(GUIConst.ACTION_CUT).setEnabled(false);
        }

        map.get(GUIConst.ACTION_PASTE).setEnabled(false);

        setSelected(true);
        // 隠しコマンドセット
        addHiddenCommand();
    }

    /**
     * Focusがはずれた場合のメニュー制御とボーダーの非表示を行う。
     * @param map
     */
    @Override
    public void exit(ActionMap map) {
        setSelected(false);
        // 隠しコマンド除去
        removeHiddenCommand();
    }

    /**
     * Popupメニューを表示する。
     * @param e
     */
    @Override
    public void maybeShowPopup(MouseEvent e) {
        StampHolderPopupMenu popup = new StampHolderPopupMenu(this);
        popup.addPropertyChangeListener(this);

        // 内服薬の場合は処方日数，外用剤の場合は処方量を選択するポップアップを作成
        if (kartePane.getTextPane().isEditable() &&
                IInfoModel.ENTITY_MED_ORDER.equals(stamp.getModuleInfo().getEntity())) {

            popup.addStampChangeMenu();
            popup.addSeparator();
        }

        ChartMediator mediator = kartePane.getMediator();
        popup.add(mediator.getAction(GUIConst.ACTION_CUT));
        popup.add(mediator.getAction(GUIConst.ACTION_COPY));
        popup.add(mediator.getAction(GUIConst.ACTION_PASTE));
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public Component getComponent() {
        return this;
    }

    /**
     * このスタンプホルダのKartePaneを返す。
     * @return
     */
    @Override
    public KartePane getKartePane() {
        return kartePane;
    }

    /**
     * スタンプホルダのコンテントタイプを返す。
     * @return
     */
    @Override
    public int getContentType() {
        return ComponentHolder.TT_STAMP;
    }

    /**
     * このホルダのモデルを返す。
     * @return
     */
    public ModuleModel getStamp() {
        return stamp;
    }

    /**
     * このホルダのモデルを設定する。
     * @param stamp
     */
    public void setStamp(ModuleModel stamp) {
        this.stamp = stamp;
        setMyText();
    }

    public StampRenderingHints getHints() {
        return hints;
    }

    public void setHints(StampRenderingHints hints) {
        this.hints = hints;
    }

    /**
     * 選択されているかどうかを返す。
     * @return 選択されている時 true
     */
    @Override
    public boolean isSelected() {
        return selected;
    }

    /**
     * 選択属性を設定する。
     * @param selected 選択の時 true
     */
    @Override
    public void setSelected(boolean selected) {
        boolean old = this.selected;
        this.selected = selected;
        if (old != this.selected) {
            if (this.selected) {
                this.setBorder(MY_SELECTED_BORDER);
            } else {
                this.setBorder(MY_CLEAR_BORDER);
            }
        }
    }

    /**
     * KartePane でこのスタンプがダブルクリックされた時コールされる。
     * StampEditor を開いてこのスタンプを編集する。
     */
    @Override
    public void edit() {

        if (kartePane.getTextPane().isEditable() && this.isEditable()) {
            String category = stamp.getModuleInfo().getEntity();
            StampEditorDialog stampEditor = new StampEditorDialog(category, stamp);
            stampEditor.addPropertyChangeListener(StampEditorDialog.VALUE_PROP, this);
            stampEditor.start();
            // 二重起動の禁止 - エディタから戻ったら propertyChange で解除する
            //kartePane.getTextPane().setEditable(false); // こうすると，なぜか focus が次の component にうつってしまう
            this.setEditable(false);
        } else {
            // ダブルクリックで EditorFrame に入力
            java.util.List<Chart> allFrames = EditorFrame.getAllEditorFrames();
            if (! allFrames.isEmpty()) {
                Chart frame = allFrames.get(0);
                KartePane pane = ((EditorFrame) frame).getEditor().getPPane();
                // caret を最後に送ってから import する
                JTextPane textPane = pane.getTextPane();
                KarteStyledDocument doc = pane.getDocument();
                textPane.setCaretPosition(doc.getLength());

                pane.flowStamp(stamp);
            }
        }
    }

    /**
     * エディタで編集した値を受け取り内容を表示する。
     * @param e
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {

        String prop = e.getPropertyName();

        // StampEditor から値がセットされた場合 or StampHolderPopupMenu からセットされた場合
        if (StampEditorDialog.VALUE_PROP.equals(prop)
                || StampHolder.STAMP_MODIFIED.equals(prop)) {

            // 二重起動禁止の解除
            //kartePane.getTextPane().setEditable(true);
            this.setEditable(true);

            ModuleModel newStamp = (ModuleModel) e.getNewValue();
            if (newStamp != null) {
                // スタンプを置き換える
                importStamp(newStamp);
            }
        }
    }

    /**
     * スタンプの内容を置き換える。
     * @param newStamp
     */
    public void importStamp(ModuleModel newStamp) {
        // 「月　日」の自動挿入：replace の場合はここに入る
        // replace でない場合は，KartePane でセット
        StampModifier.modify(newStamp);

        setStamp(newStamp);
        kartePane.setDirty(true);
        kartePane.getTextPane().validate();
        kartePane.getTextPane().repaint();
    }

    /**
     * TextPane内での開始と終了ポジションを保存する。
     * @param start
     * @param end
     */
    @Override
    public void setEntry(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    /**
     * 開始ポジションを返す。
     * @return
     */
    @Override
    public int getStartPos() {
        return start.getOffset();
    }

    /**
     * 終了ポジションを返す。
     * @return
     */
    @Override
    public int getEndPos() {
        return end.getOffset();
    }

    /**
     * Velocity を利用してスタンプの内容を表示する。
     */
    private void setMyText() {

        try {
            IInfoModel model = getStamp().getModel();
            VelocityContext context = ClientContext.getVelocityContext();
            context.put("model", model);
            context.put("hints", getHints());
            context.put("stampName", getStamp().getModuleInfo().getStampName());
            String templateFile = getStamp().getModel().getClass().getName() + ".vm";
            // このスタンプのテンプレートファイルを得る
            if (getStamp().getModuleInfo().getEntity().equals(IInfoModel.ENTITY_LABO_TEST)) {
                if (Project.getPreferences().getBoolean("laboFold", true)) {
                    templateFile = "labo.vm";
                }
            }

            // Merge する
            StringWriter sw = new StringWriter();
            BufferedReader reader;
            try (BufferedWriter bw = new BufferedWriter(sw)) {
                InputStream instream = ClientContext.getTemplateAsStream(templateFile);
                reader = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
                Velocity.evaluate(context, bw, "stmpHolder", reader);
                bw.flush();
            }
            reader.close();

            // 全角数字とスペースを直す
            String text = sw.toString();
            text = StringTool.toHankakuNumber(text);
            text = StringTool.toHankakuUpperLower(text);
            text = text.replaceAll("　", " ");

            // 検索語の attribute をセットする
            if (searchText != null) {
                String taggedText = startTag + searchText + endTag;
                int pos = text.indexOf(searchText);
                while (pos != -1) {
                    text = text.substring(0,pos) + taggedText + text.substring(pos + searchText.length());
                    pos = text.indexOf(searchText, pos + taggedText.length());
                }
            }

            this.setText(text);

            // カルテペインへ展開された時広がるのを防ぐ
            this.setMaximumSize(this.getPreferredSize());

        } catch (IOException | ParseErrorException | MethodInvocationException | ResourceNotFoundException ex) {
            System.out.println("StampHolder.java: " + ex);
        }
    }

    public void setAttr(String searchText, String startTag, String endTag) {
        this.searchText = searchText;
        this.startTag = startTag;
        this.endTag = endTag;
        setMyText();
    }

    public void removeAttr() {
        this.searchText = null;
        this.startTag = null;
        this.endTag = null;
        setMyText();
    }

    /**
     * Shift-commnad-C ショートカットでクリップボードにスタンプをコピーする
     */
    private void addHiddenCommand() {

        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "copyAsText");
        this.getActionMap().put("copyAsText", new AbstractAction(){
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getStamp().getModel().getClass().getName().equals("open.dolphin.infomodel.BundleMed")) {
                    try {
                        IInfoModel model = getStamp().getModel();
                        VelocityContext context = ClientContext.getVelocityContext();
                        context.put("model", model);

                        String templateFile = "open.dolphin.infomodel.BundleMed-text.vm";
                        StringWriter sw = new StringWriter();
                        BufferedReader reader;
                        try (BufferedWriter bw = new BufferedWriter(sw)) {
                            InputStream instream = ClientContext.getTemplateAsStream(templateFile);
                            reader = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
                            Velocity.evaluate(context, bw, "stmpHolder", reader);
                            bw.flush();
                        }
                        reader.close();

                        // 全角数字とスペースを直す
                        String text = sw.toString();
                        text = StringTool.toHankakuNumber(text);
                        text = StringTool.toHankakuUpperLower(text);
                        text = text.replaceAll("　", " ");
                        text = text.replace("\n", " ");

                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(new StringSelection(text), null);

                    } catch (ParseErrorException | MethodInvocationException | ResourceNotFoundException ex) {
                        System.out.println("StampHolder.java: " + ex);
                    } catch (UnsupportedEncodingException ex) {
                        System.out.println("StampHolder.java: " + ex);
                    } catch (IOException ex) {
                        System.out.println("StampHolder.java: " + ex);
                    }
                }
            }
        });

    }

    /**
     * 登録した Shift-command-C ショートカットを削除する
     */
    private void removeHiddenCommand() {
        // Shift+command C
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
    }
}
