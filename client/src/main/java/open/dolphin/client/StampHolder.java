package open.dolphin.client;

import open.dolphin.event.ProxyAction;
import open.dolphin.event.ProxyActionListener;
import open.dolphin.helper.HtmlHelper;
import open.dolphin.helper.StringTool;
import open.dolphin.infomodel.*;
import open.dolphin.orca.ClaimConst;
import open.dolphin.order.StampEditorDialog;
import open.dolphin.project.Project;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.IMEControl;
import open.dolphin.ui.PNSBorderFactory;
import open.dolphin.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Objects;

/**
 * KartePane に Component　として挿入されるスタンプを保持するクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public final class StampHolder extends AbstractComponentHolder<ModuleModel> {
    public static final String STAMP_MODIFIED = "stampModified";
    private static final long serialVersionUID = 5853431116398862958L;
    private static final Color FOREGROUND = new Color(20, 20, 140);
    private static final Color BACKGROUND = new Color(0, 0, 0, 0);
    private static final Color COMMENT_COLOR = new Color(120, 20, 140);
    private static final Border MY_SELECTED_BORDER = PNSBorderFactory.createSelectedBorder();
    private static final Border MY_CLEAR_BORDER = PNSBorderFactory.createClearBorder();
    private static final int MARGIN = 24; // JTextPane より MARGIN 分だけ小さくする

    private final KartePane kartePane;
    private ModuleModel stamp;
    private StampRenderingHints hints;
    private boolean selected;
    // 検索語にマークする
    private String searchText = null;
    private String startTag = null;
    private String endTag = null;
    // スタンプの簡易表示
    private boolean simplify = false;
    // Logger
    private Logger logger = LoggerFactory.getLogger(StampHolder.class);

    public StampHolder(final KartePane kartePane, final ModuleModel model) {
        super(kartePane);

        this.kartePane = kartePane;
        stamp = model;
        hints = new StampRenderingHints();
        initialize();
    }

    private void initialize() {
        setForeground(FOREGROUND);
        setBackground(BACKGROUND);
        setBorder(MY_CLEAR_BORDER);

        MyHierarchyBoundsListener listener = new MyHierarchyBoundsListener();
        addHierarchyBoundsListener(listener);

        // コメント用の色をセット
        hints.setCommentColor(COMMENT_COLOR);

        // 幅が決定していれば hint にセットして描画, 未定ならパスして HierarchyBoundsListener に任せる
        if (kartePane.getTextPane().getWidth() > 1) { listener.repaintStamp(); }
    }

    /**
     * Component の変化に乗じて stamp を書き換える.
     */
    private class MyHierarchyBoundsListener extends HierarchyBoundsAdapter {
        public void repaintStamp() {
            JTextPane tp = kartePane.getTextPane();
            if (Objects.nonNull(tp)) {
                int width = tp.getParent() instanceof JViewport
                    ? tp.getParent().getWidth()
                    : tp.getWidth();

                if (width > 1) {
                    hints.setWidth(Math.max(320, width - MARGIN));
                    setMyText();
                }
            }
        }
        @Override
        public void ancestorResized(HierarchyEvent e) {
            repaintStamp();
        }
    }

    /**
     * 数字キーでスタンプ数量を変更する.
     *
     * @param e KeyEvent
     */
    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);

        if (Character.isDigit(e.getKeyChar())) {
            //
            // 数字キー入力処理編集は editable でないと意味が無い
            //
            if (!kartePane.getTextPane().isEditable()
                || !StampHolder.this.isEditable()
                || !(stamp.getModel() instanceof BundleMed)) { return; }

            Color translucent = new Color(0, 0, 0, 0);
            Color lightGray = new Color(238, 238, 238);
            Color origColor;

            // 数字キー入力のための minimal な dialog を作る
            JDialog dialog = new JDialog((Frame) null, true);
            dialog.setUndecorated(true);
            dialog.setBackground(translucent);

            // 入力中はスタンプの背景を暗くする
            origColor = getBackground();
            setBackground(lightGray);
            setOpaque(true);

            // dialog closing procedure
            ProxyActionListener closeDialog = () -> {
                dialog.setVisible(false);
                setBackground(origColor);
                setOpaque(false);
                Focuser.requestFocus(this);
            };

            // text field を作って, 最初の1文字を入力する
            JTextField tf = new JTextField(3);
            tf.setText(String.valueOf(e.getKeyChar()));

            // enter key でスタンプの数量を変更する
            tf.addActionListener(actionEvent -> {
                try {
                    // 数字が入力されたかどうか
                    String num = tf.getText();
                    Float.parseFloat(num);

                    BundleMed bundle = (BundleMed) stamp.getModel();
                    if (ClaimConst.RECEIPT_CODE_NAIYO.equals(bundle.getClassCode()) ||
                        ClaimConst.RECEIPT_CODE_TONYO.equals(bundle.getClassCode())) {
                        // 投与日数を変更する
                        String old = bundle.getBundleNumber();
                        if (!old.equals(num)) {
                            ModuleModel distModel = ModelUtils.deepClone(stamp);
                            BundleMed distBundle = (BundleMed) distModel.getModel();
                            distBundle.setBundleNumber(num);
                            undoableUpdateModel(distModel);
                            kartePane.setDirty(true);
                            logger.info("bundle number changed to " + num);
                        }

                    } else {
                        // 外用剤の量を変更する
                        ModuleModel distModel = ModelUtils.deepClone(stamp);
                        boolean dirty = false;
                        for (ClaimItem item : ((BundleMed) distModel.getModel()).getClaimItem()) {
                            if (item.getCode().startsWith("6")) {
                                String old = item.getNumber();
                                if (!old.equals(num)) {
                                    item.setNumber(num);
                                    dirty = true;
                                }
                            }
                        }
                        if (dirty) {
                            undoableUpdateModel(distModel);
                            kartePane.setDirty(true);
                            logger.info("item number changed to " + num);
                        }
                    }
                } catch (NumberFormatException ex) {
                    logger.error("wrong input");
                }
                // dialog-close
                closeDialog.actionPerformed();
            });
            dialog.add(tf);
            dialog.pack();

            // escape or command-w to cancel
            InputMap im = dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            im.put(KeyStroke.getKeyStroke("ESCAPE"), "dialog-close");
            im.put(KeyStroke.getKeyStroke("meta W"), "dialog-close");
            ActionMap am = dialog.getRootPane().getActionMap();
            am.put("dialog-close", new ProxyAction(closeDialog));

            // centering
            Point stampLocation = StampHolder.this.getLocationOnScreen();
            Dimension stampSize = StampHolder.this.getSize();
            Dimension dialogSize = dialog.getSize();
            int dispX = stampLocation.x + (stampSize.width - dialogSize.width) / 2;
            int dispY = stampLocation.y + (stampSize.height - dialogSize.height) / 2;
            dialog.setLocation(dispX, dispY);

            dialog.setVisible(true);
        }
    }

    /**
     * Focus されると {@link open.dolphin.client.ChartMediator ChartMediator} から呼ばれる.
     * メニュー制御とボーダーを表示する.
     *
     * @param map ActionMap
     */
    @Override
    public void enter(ActionMap map) {
        super.enter(map);
        map.get(GUIConst.ACTION_COPY).setEnabled(true);
        map.get(GUIConst.ACTION_CUT).setEnabled(kartePane.getTextPane().isEditable());
        map.get(GUIConst.ACTION_PASTE).setEnabled(false);

        setSelected(true);
        IMEControl.off();
    }

    /**
     * Focusがはずれた場合のメニュー制御とボーダーの非表示を行う.
     *
     * @param map ActionMap
     */
    @Override
    public void exit(ActionMap map) {
        setSelected(false);
    }

    /**
     * Popupメニューを表示する.
     *
     * @param e MouseEvent
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
     * @return KartePane
     */
    @Override
    public KartePane getKartePane() {
        return kartePane;
    }

    /**
     * スタンプホルダのコンテントタイプを返す.
     *
     * @return ContentType
     */
    @Override
    public ContentType getContentType() {
        return ContentType.TT_STAMP;
    }

    /**
     * このホルダのモデルを返す.
     *
     * @return ModuleModel
     */
    @Override
    public ModuleModel getModel() { return stamp; }

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
     * スタンプの簡易表示設定.
     *
     * @param s true to simplify
     */
    public void setSimplify(boolean s) {
        simplify = s;
        setMyText();
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
            stampEditor.addPropertyChangeListener(this);
            stampEditor.start();
            // 二重起動の禁止 - エディタから戻ったら propertyChange で解除する
            //kartePane.getTextPane().setEditable(false); // こうすると，なぜか focus が次の component にうつってしまう
            this.setEditable(false);

        } else {
            // ダブルクリックで EditorFrame に入力
            List<EditorFrame> allFrames = EditorFrame.getAllEditorFrames();
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
     * @param e MouseEvent
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
                undoableUpdateModel(newStamp);
            }
        }
    }

    /**
     * スタンプの内容を置き換える.
     *
     * @param newStamp ModuleModel
     */
    @Override
    public void updateModel(ModuleModel newStamp) {

        // 「月　日」の自動挿入：replace の場合はここに入る
        // replace でない場合は，KartePane でセット
        StampModifier.modify(newStamp);
        stamp = newStamp;
        setMyText();

        kartePane.setDirty(true);
        kartePane.getTextPane().validate();
        kartePane.getTextPane().repaint();
        Focuser.requestFocus(this);
        updateMenuState();
    }

    /**
     * j2html を利用してスタンプの内容を表示する.
     */
    private void setMyText() {

        IInfoModel bundle = getModel().getModel(); // BundleMed > BundleDolphin > ClaimBundle
        String stampName = getModel().getModuleInfo().getStampName();
        logger.debug("bundle = " + bundle.getClass().getName() + ", stampName = " + stampName);

        String text;

        if (simplify) {
            if (bundle instanceof BundleMed) {
                text = HtmlHelper.bundleMed2HtmlLight((BundleMed) bundle, stampName, hints);
            } else {
                text = HtmlHelper.bundleDolphin2HtmlLight((BundleDolphin) bundle, stampName, hints);
            }

        } else {
            if (bundle instanceof BundleMed) {
                text = HtmlHelper.bundleMed2Html((BundleMed) bundle, stampName, hints);

            } else if (getModel().getModuleInfo().getEntity().equals(IInfoModel.ENTITY_LABO_TEST)
                && Project.getPreferences().getBoolean("laboFold", true)) {
                text = HtmlHelper.bundleDolphin2Html((BundleDolphin) bundle, stampName, hints, true);

            } else {
                text = HtmlHelper.bundleDolphin2Html((BundleDolphin) bundle, stampName, hints);
            }
        }

        text = StringTool.toHankakuNumber(text);
        text = StringTool.toHankakuUpperLower(text);
        text = text.replaceAll("　", " ");
        text = text.replaceAll("．", ".");
        // グラムは全角で
        text = text.replaceAll(">g<", ">ｇ<"); // ℊ

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
}
