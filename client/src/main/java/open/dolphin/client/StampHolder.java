package open.dolphin.client;

import open.dolphin.event.ProxyAction;
import open.dolphin.helper.HtmlHelper;
import open.dolphin.helper.PreferencesUtils;
import open.dolphin.helper.StringTool;
import open.dolphin.infomodel.*;
import open.dolphin.order.StampEditorDialog;
import open.dolphin.project.Project;
import open.dolphin.ui.PNSBorderFactory;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

/**
 * KartePane に Component　として挿入されるスタンプを保持するクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public final class StampHolder extends AbstractComponentHolder {
    public static final String STAMP_MODIFIED = "stampModified";
    private static final long serialVersionUID = 5853431116398862958L;
    private static final Color FOREGROUND = new Color(20, 20, 140);
    private static final Color BACKGROUND = new Color(0, 0, 0, 0);
    private static final Color COMMENT_COLOR = new Color(120, 20, 140);
    private static final Border MY_SELECTED_BORDER = PNSBorderFactory.createSelectedBorder();
    private static final Border MY_CLEAR_BORDER = PNSBorderFactory.createClearBorder();
    private final KartePane kartePane;
    private ModuleModel stamp;
    private StampRenderingHints hints;
    private Position start;
    private Position end;
    private boolean selected;
    // 検索語にマークする
    private String searchText = null;
    private String startTag = null;
    private String endTag = null;
    // Logger
    private Logger logger = Logger.getLogger(StampHolder.class);

    public StampHolder(final KartePane kartePane, final ModuleModel model) {
        super();
        //logger.setLevel(Level.DEBUG);

        this.kartePane = kartePane;
        hints = new StampRenderingHints();
        hints.setCommentColor(COMMENT_COLOR);

        // スタンプの初期幅は ChartImpl の幅から決定する
        Rectangle bounds = PreferencesUtils.getRectangle(Project.getPreferences(), ChartImpl.PN_FRAME, new Rectangle(0, 0, 0, 0));
        int w = (bounds.width + 1) / 2 - 168; // 実験から連立方程式で求めた
        hints.setWidth((w < 320) ? 320 : w);

        init(model);
    }

    private void init(ModuleModel model) {
        setForeground(FOREGROUND);
        setBackground(BACKGROUND);
        setBorder(MY_CLEAR_BORDER);

        setStamp(model);
    }

    /**
     * Focusされた場合のメニュー制御とボーダーを表示する.
     *
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
     * Focusがはずれた場合のメニュー制御とボーダーの非表示を行う.
     *
     * @param map
     */
    @Override
    public void exit(ActionMap map) {
        setSelected(false);
        // 隠しコマンド除去
        removeHiddenCommand();
    }

    /**
     * Popupメニューを表示する.
     *
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
    public JLabel getComponent() {
        return this;
    }

    /**
     * このスタンプホルダのKartePaneを返す.
     *
     * @return
     */
    @Override
    public KartePane getKartePane() {
        return kartePane;
    }

    /**
     * スタンプホルダのコンテントタイプを返す.
     *
     * @return
     */
    @Override
    public ContentType getContentType() {
        return ContentType.TT_STAMP;
    }

    /**
     * このホルダのモデルを返す.
     *
     * @return
     */
    public ModuleModel getStamp() {
        return stamp;
    }

    /**
     * このホルダのモデルを設定する.
     *
     * @param model
     */
    public void setStamp(ModuleModel model) {
        stamp = model;
        setMyText();
    }

    public StampRenderingHints getHints() {
        return hints;
    }

    public void setHints(StampRenderingHints hints) {
        this.hints = hints;
    }

    /**
     * 選択されているかどうかを返す.
     *
     * @return 選択されている時 true
     */
    @Override
    public boolean isSelected() {
        return selected;
    }

    /**
     * 選択属性を設定する.
     *
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
     * KartePane でこのスタンプがダブルクリックされた時コールされる.
     * StampEditor を開いてこのスタンプを編集する.
     */
    @Override
    public void edit() {
        if (kartePane.getTextPane().isEditable() && this.isEditable()) {
            String entity = stamp.getModuleInfo().getEntity();
            StampEditorDialog stampEditor = new StampEditorDialog(entity, stamp);
            stampEditor.addPropertyChangeListener(StampEditorDialog.VALUE_PROP, this);
            stampEditor.start();
            // 二重起動の禁止 - エディタから戻ったら propertyChange で解除する
            //kartePane.getTextPane().setEditable(false); // こうすると，なぜか focus が次の component にうつってしまう
            this.setEditable(false);

        } else {
            // ダブルクリックで EditorFrame に入力
            java.util.List<EditorFrame> allFrames = EditorFrame.getAllEditorFrames();
            if (!allFrames.isEmpty()) {
                EditorFrame frame = allFrames.get(0);
                if (this.isEditable()) {
                    KartePane pane = frame.getEditor().getPPane();
                    // caret を最後に送ってから import する
                    JTextPane textPane = pane.getTextPane();
                    KarteStyledDocument doc = pane.getDocument();
                    textPane.setCaretPosition(doc.getLength());

                    pane.stampWithDuplicateCheck(stamp);
                }
            }
        }
    }

    /**
     * エディタで編集した値を受け取り内容を表示する.
     *
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
     * スタンプの内容を置き換える.
     *
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
     * TextPane内での開始と終了ポジションを保存する.
     *
     * @param start
     * @param end
     */
    @Override
    public void setEntry(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    /**
     * 開始ポジションを返す.
     *
     * @return
     */
    @Override
    public int getStartPos() {
        return start.getOffset();
    }

    /**
     * 終了ポジションを返す.
     *
     * @return
     */
    @Override
    public int getEndPos() {
        return end.getOffset();
    }

    /**
     * Velocity を利用してスタンプの内容を表示する.
     */
    private void setMyText() {

            IInfoModel bundle = getStamp().getModel(); // BundleMed > BundleDolphin > ClaimBundle
            String stampName = getStamp().getModuleInfo().getStampName();
            logger.debug("bundle = " + bundle.getClass().getName() + ", stampName = " + stampName);

            String text;

            if (bundle instanceof BundleMed) {
                text = HtmlHelper.bundleMed2Html((BundleMed) bundle, stampName, hints);

            } else if (getStamp().getModuleInfo().getEntity().equals(IInfoModel.ENTITY_LABO_TEST)
                && Project.getPreferences().getBoolean("laboFold", true)) {
                text = HtmlHelper.bundleDolphin2Html((BundleDolphin) bundle, stampName, hints, true);

            } else {
                text = HtmlHelper.bundleDolphin2Html((BundleDolphin) bundle, stampName, hints);
            }


            text = StringTool.toHankakuNumber(text);
            text = StringTool.toHankakuUpperLower(text);
            text = text.replaceAll("　", " ");

            // 検索語の attribute をセットする
            if (searchText != null) {
                String taggedText = startTag + searchText + endTag;
                int pos = text.indexOf(searchText);
                while (pos != -1) {
                    text = text.substring(0, pos) + taggedText + text.substring(pos + searchText.length());
                    pos = text.indexOf(searchText, pos + taggedText.length());
                }
            }

            this.setText(text);

            // カルテペインへ展開された時広がるのを防ぐ
            this.setMaximumSize(this.getPreferredSize());

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
     * Shift-commnad-C ショートカットでクリップボードにスタンプをコピーする.
     */
    private void addHiddenCommand() {

        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke("shift meta C"), "copyAsText");

        this.getActionMap().put("copyAsText", new ProxyAction(() -> {

            if (getStamp().getModel() instanceof BundleMed) {
                BundleMed bundle = (BundleMed) getStamp().getModel();

                StringBuilder sb = new StringBuilder();

                for (ClaimItem item : bundle.getClaimItem()) {
                    if (!item.getCode().matches("099[0-9]{6}")) {
                        sb.append(item.getName());
                        sb.append(" ");

                        if (!item.getCode().matches("0085[0-9]{5}")
                            && !item.getCode().matches("001000[0-9]{3}")
                            && !item.getCode().matches("810000001")) {
                            sb.append(item.getNumber());
                            sb.append(item.getUnit());
                        }
                    }
                }
                sb.append(bundle.getAdminDisplayString());

                // 全角数字とスペースを直す
                String text = sb.toString();
                text = StringTool.toHankakuNumber(text);
                text = StringTool.toHankakuUpperLower(text);
                text = text.replaceAll("　", " ");
                text = text.replace("\n", " ");

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(text.toString()), null);
            }
        }));
    }

    /**
     * 登録した Shift-command-C ショートカットを削除する.
     */
    private void removeHiddenCommand() {
        // Shift+command C
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
    }
}
