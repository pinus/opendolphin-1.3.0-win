package open.dolphin.client;

import javafx.application.Platform;
import open.dolphin.codehelper.PCodeHelper;
import open.dolphin.codehelper.SOACodeHelper;
import open.dolphin.delegater.OrcaDelegater;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.helper.DBTask;
import open.dolphin.helper.ImageHelper;
import open.dolphin.helper.TextComponentUndoManager;
import open.dolphin.impl.scheam.SchemaEditorImpl;
import open.dolphin.infomodel.*;
import open.dolphin.order.StampEditorDialog;
import open.dolphin.ui.IMEControl;
import open.dolphin.ui.sheet.JSheet;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * KarteComposite の一つで，内部に JTextPane を１つ保持している.
 * 保持している JTextPane は，{@link open.dolphin.client.KartePane#setTextPane setTextPane} されるときに
 * client property "kartePane" に親の KartePane を入れる.<br>
 * ２号カルテはこれを２枚 KarteViewer / KarteEditor に入れて使う.
 *
 * @author Kazushi Minagawa, Digital Globe, inc.
 * @author pns
 */
public class KartePane implements DocumentListener, MouseListener, CaretListener, PropertyChangeListener, KarteComposite<JTextPane> {

    // 編集不可時の背景色
    protected static final Color UNEDITABLE_COLOR = new Color(227, 250, 207);
    // 文書に付けるタイトルを自動で取得する時の長さ
    private static final int TITLE_LENGTH = 15;
    // KartePane の UndoManager
    private final TextComponentUndoManager undoManager;
    private final UndoableEditListener undoListener;
    // ロガー
    private final Logger logger = Logger.getLogger(KartePane.class);
    private State curState;
    // JTextPane
    private JTextPane textPane;
    // SOA または P のロール
    private String myRole;
    // この KartePaneのオーナ
    private ChartDocument parent;
    // StampHolderのTransferHandler
    private StampHolderTransferHandler stampHolderTransferHandler;
    // SchemaHolderのTransferHandler
    private SchemaHolderTransferHandler schemaHolderTransferHandler;
    // SchemaModel にファイル名を付けるときの Id (インクリメントする)
    private int stampId;
    // Dirty Flag
    private boolean dirty;
    // Selection Flag
    private boolean hasSelection;
    // 初期化された時のDocumentの長さ
    private int initialLength;
    // ChartMediator(MenuSupport)
    private ChartMediator mediator;
    // このオブジェクトで生成する文書DocumentModelの文書ID
    private String docId;
    // 保存後及びブラウズ時の編集不可を表すカラー
    private Color uneditableColor = UNEDITABLE_COLOR;
    // このペインからDragg及びDroppされたスタンプの情報
    private ComponentHolder<?>[] draggedStamp;
    private int draggedCount;
    private int droppedCount;

    public KartePane() {
        undoManager = new TextComponentUndoManager();
        undoListener = undoManager::listener;
    }

    public void setMargin(Insets margin) {
        textPane.setMargin(margin);
    }

    public void setPreferredSize(Dimension size) {
        textPane.setPreferredSize(size);
    }

    public void setSize(Dimension size) {
        textPane.setMinimumSize(size);
        textPane.setMaximumSize(size);
    }

    /**
     * このPaneのオーナを返す.
     *
     * @return KarteEditorオーナ
     */
    public ChartDocument getParent() {
        return parent;
    }

    /**
     * このPaneのオーナを設定する.
     *
     * @param parent KarteEditorオーナ
     */
    public void setParent(ChartDocument parent) {
        this.parent = parent;
    }

    /**
     * 編集不可を表すカラーを返す.
     *
     * @return 編集不可を表すカラー
     */
    public Color getUneditableColor() {
        return uneditableColor;
    }

    /**
     * 編集不可を表すカラーを設定する.
     *
     * @param uneditableColor 編集不可を表すカラー
     */
    public void setUneditableColor(Color uneditableColor) {
        this.uneditableColor = uneditableColor;
    }

    /**
     * このPaneで生成するDocumentModelの文書IDを返す.
     *
     * @return 文書ID
     */
    protected String getDocId() {
        return docId;
    }

    /**
     * このPaneで生成するDocumentModelの文書IDを設定する.
     *
     * @param docId 文書ID
     */
    protected void setDocId(String docId) {
        this.docId = docId;
    }

    /**
     * ChartMediatorを返す.
     *
     * @return ChartMediator
     */
    protected ChartMediator getMediator() {
        return mediator;
    }

    /**
     * このPaneのロールを返す.
     *
     * @return SOAまたはPのロール
     */
    public String getMyRole() {
        return myRole;
    }

    /**
     * このPaneのロールを設定する.
     *
     * @param myRole SOAまたはPのロール
     */
    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    /**
     * JTextPaneを返す.
     *
     * @return JTextPane
     */
    public JTextPane getTextPane() {
        return textPane;
    }

    /**
     * JTextPaneを設定する.
     *
     * @param textPane JTextPane
     */
    public void setTextPane(JTextPane textPane) {
        this.textPane = textPane;
        if (this.textPane != null) {
            KarteStyledDocument doc = new KarteStyledDocument();
            this.textPane.setDocument(doc);
            this.textPane.putClientProperty("kartePane", this);

            doc.setParent(this);
            stampHolderTransferHandler = new StampHolderTransferHandler();
            schemaHolderTransferHandler = new SchemaHolderTransferHandler();
        }
    }

    /**
     * JTextPaneのStyledDocumentを返す.
     *
     * @return JTextPaneのStyledDocument
     */
    public KarteStyledDocument getDocument() {
        return (KarteStyledDocument) getTextPane().getDocument();
    }

    /**
     * 初期長を返す.
     *
     * @return Documentの初期長
     */
    public int getInitialLength() {
        return initialLength;
    }

    /**
     * 初期長を設定する.
     *
     * @param initialLength Documentの初期長
     */
    public void setInitialLength(int initialLength) {
        this.initialLength = initialLength;
    }

    /**
     * このPaneからDragされたスタンプ数を返す.
     *
     * @return このPaneからDragされたスタンプ数
     */
    protected int getDraggedCount() {
        return draggedCount;
    }

    /**
     * このPaneからDragされたスタンプ数を設定する.
     *
     * @param draggedCount このPaneからDragされたスタンプ数
     */
    protected void setDraggedCount(int draggedCount) {
        this.draggedCount = draggedCount;
    }

    /**
     * このPaneにDropされたスタンプ数を返す.
     *
     * @return このPaneにDropされたスタンプ数
     */
    protected int getDroppedCount() {
        return droppedCount;
    }

    /**
     * このPaneにDropされたスタンプ数を設定する.
     *
     * @param droppedCount このPaneにDropされたスタンプ数
     */
    protected void setDroppedCount(int droppedCount) {
        this.droppedCount = droppedCount;
    }

    /**
     * このPaneからDragされたスタンプを返す.
     *
     * @return このPaneからDragされたスタンプ配列
     */
    protected ComponentHolder<?>[] getDraggedStamp() {
        return draggedStamp;
    }

    /**
     * このPaneからDragされたスタンプを設定（記録）する.
     *
     * @param drragedStamp このPaneからDragされたスタンプ配列
     */
    protected void setDraggedStamp(ComponentHolder<?>[] drragedStamp) {
        this.draggedStamp = drragedStamp;
    }

    /**
     * 初期化する.
     * KarteViewer, KarteEditor から呼ばれる.
     *
     * @param editable      編集可能かどうかのフラグ
     * @param chartMediator チャートメディエータ（メニューサポート）
     */
    public void init(boolean editable, ChartMediator chartMediator) {

        // Mediatorを保存する
        mediator = chartMediator;

        // 入れておかないと最初に focus 取る前に Drop したときヌルポが出る
        enter(mediator.getActions());

        // Drag は editable に関係なく可能
        //getTextPane().setDragEnabled(true);

        // リスナを登録する
        getTextPane().addMouseListener(this);
        getTextPane().addCaretListener(this);
        getTextPane().getDocument().addDocumentListener(this);
        getTextPane().getDocument().addUndoableEditListener(undoListener);

        getTextPane().setEditable(editable);
        // ChartImpl で DocumentHistory が focus を取れないことがあるのの workaround
        // editable でないときはフォーカスを取らない
        // ただし，マウスで選択する時は MouseListener で focus とって選択できるようにする
        getTextPane().setFocusable(editable);

        if (editable) {
            IMEControl.setImeOnIfFocused(getTextPane());

            if (myRole.equals(IInfoModel.ROLE_SOA)) {
                SOACodeHelper helper = new SOACodeHelper(this, getMediator());
            } else {
                PCodeHelper helper = new PCodeHelper(this, getMediator());
            }
            getTextPane().setBackground(Color.WHITE);
            getTextPane().setOpaque(true);

        } else {
            setBackgroundUneditable();
        }
    }

    // JTextPaneへの挿入でdirtyかどうかを判定する
    @Override
    public void insertUpdate(DocumentEvent e) {
        boolean newDirty = getDocument().getLength() > getInitialLength();
        setDirty(newDirty);
    }

    // 削除が起こった時dirtyかどうかを判定する
    @Override
    public void removeUpdate(DocumentEvent e) {
        boolean newDirty = getDocument().getLength() > getInitialLength();
        setDirty(newDirty);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        boolean newSelection = (e.getDot() != e.getMark());
        if (newSelection != hasSelection) {
            hasSelection = newSelection;

            // テキスト選択の状態へ遷移する
            if (hasSelection) {
                curState = getMyRole().equals(IInfoModel.ROLE_SOA) ? State.SOA_TEXT : State.P_TEXT;
            } else {
                curState = getMyRole().equals(IInfoModel.ROLE_SOA) ? State.SOA : State.P;
            }
            controlMenus(mediator.getActions());
        }
    }

    /**
     * リソースをclearする.
     */
    public void clear() {
        JTextPane pane = getTextPane();
        if (pane == null) {
            return;
        }

        pane.getDocument().removeDocumentListener(this);
        pane.getDocument().removeUndoableEditListener(undoListener);
        pane.removeMouseListener(this);
        pane.removeCaretListener(this);

        try {
            KarteStyledDocument doc = getDocument();
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace(System.err);
        }

        setTextPane(null);
    }

    /**
     * メニューを制御する.
     */
    private void controlMenus(ActionMap map) {
        // undo redo
        map.get(GUIConst.ACTION_UNDO).setEnabled(undoManager.canUndo());
        map.get(GUIConst.ACTION_REDO).setEnabled(undoManager.canRedo());

        // 各Stateはenableになる条件だけを管理する
        switch (curState) {

            case NONE:
                break;

            case SOA:
                // SOAPaneにFocusがありテキスト選択がない状態
                if (getTextPane().isEditable()) {
                    map.get(GUIConst.ACTION_PASTE).setEnabled(canPaste());
                    map.get(GUIConst.ACTION_INSERT_TEXT).setEnabled(true);
                    map.get(GUIConst.ACTION_INSERT_SCHEMA).setEnabled(true);
                }
                break;

            case SOA_TEXT:
            case P_TEXT:
                // SOAPaneにFocusがありテキスト選択がある状態
                map.get(GUIConst.ACTION_CUT).setEnabled(getTextPane().isEditable());
                map.get(GUIConst.ACTION_COPY).setEnabled(true);
                boolean pasteOk = (getTextPane().isEditable() && canPaste());
                map.get(GUIConst.ACTION_PASTE).setEnabled(pasteOk);
                break;

            case P:
                // PPaneにFocusがありテキスト選択がない状態
                if (getTextPane().isEditable()) {
                    map.get(GUIConst.ACTION_PASTE).setEnabled(canPaste());
                    map.get(GUIConst.ACTION_INSERT_TEXT).setEnabled(true);
                    map.get(GUIConst.ACTION_INSERT_STAMP).setEnabled(true);
                }
                break;
        }
    }

    @Override
    public void enter(ActionMap map) {
        curState = getMyRole().equals(IInfoModel.ROLE_SOA) ? State.SOA : State.P;

        undoManager.setUndoAction(map.get(GUIConst.ACTION_UNDO));
        undoManager.setRedoAction(map.get(GUIConst.ACTION_REDO));

        controlMenus(map);
    }

    @Override
    public void exit(ActionMap map) {
    }

    @Override
    public JTextPane getComponent() {
        return getTextPane();
    }

    private void showPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            KartePanePopupMenu contextMenu = new KartePanePopupMenu(this, e.getModifiers());
            contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        showPopup(e);
        // focusable false でもマウスで選択してコピーできるように
        getTextPane().setFocusable(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        showPopup(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * 背景を編集不可カラーに設定する.
     */
    protected void setBackgroundUneditable() {
        getTextPane().setBackground(getUneditableColor());
        getTextPane().setOpaque(true);
    }

    /**
     * ロールとパートナを設定する.
     *
     * @param role このペインのロール
     */
    public void setRole(String role) {
        setMyRole(role);
    }

    /**
     * Dirtyかどうかを返す.
     *
     * @return editable かつ dirty の時 true
     */
    protected boolean isDirty() {
        return getTextPane().isEditable() && dirty;
    }

    protected void setDirty(boolean newDirty) {
        if (dirty ^ newDirty) {
            dirty = newDirty;
        }
        // 親の KarteEditor に通知
        getParent().setDirty(dirty);
    }

    /**
     * 保存時につけるドキュメントのタイトルをDocument Objectから抽出する.
     *
     * @return 先頭から指定された長さを切り出した文字列
     */
    protected String getTitle() {
        try {
            KarteStyledDocument doc = getDocument();
            int len = doc.getLength();
            int freeTop = 0; // doc.getFreeTop();
            int freeLen = len - freeTop;
            freeLen = Math.min(freeLen, TITLE_LENGTH);
            return getTextPane().getText(freeTop, freeLen).trim();
        } catch (BadLocationException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    /**
     * 段落を構成する.
     */
    public void makeParagraph() {
        getDocument().makeParagraph();
    }

    /**
     * Documentに文字列を挿入する.
     *
     * @param s 挿入する文字列
     * @param a 属性
     */
    public void insertFreeString(String s, AttributeSet a) {
        getDocument().insertFreeString(s, a);
    }

    /**
     * このペインに Stamp を挿入する. (drag & drop した場合)
     *
     * @param stamp ModuleModel
     */
    public void stamp(final ModuleModel stamp) {
        if (stamp != null) {
            // text stamp がここに入った時の対策（新規カルテにテキストスタンプ挿入するときここに来る）
            if (stamp.getModuleInfo().getEntity().equals(IInfoModel.ENTITY_TEXT)) {
                //insertTextStamp(stamp.getDocument().toString()); ←これだとキャレット位置がおかしくなる
                insertFreeString(stamp.getModel().toString(), null);
                return;
            }
            // 「月　日」の自動挿入
            // replace の場合はここに入らないので，StampHolder#importStamp でセット
            StampModifier.modify(stamp);
            EventQueue.invokeLater(() -> {
                StampHolder h = new StampHolder(KartePane.this, stamp);
                h.setTransferHandler(stampHolderTransferHandler);
                KarteStyledDocument doc = getDocument();
                doc.stamp(h);
            });
        }
    }

    /**
     * 重複をチェックする stamp(ModuleModel) - StampHolder から呼ばれる
     *
     * @param stamp ModuleModel
     */
    public void stampWithDuplicateCheck(final ModuleModel stamp) {
        stamp(stamp);
        StampModifier.checkDuplicates(stamp, this);
    }

    /**
     * このペインに Stamp を挿入する. (KarteRenderer_2 から呼ばれる)
     *
     * @param stamp ModuleModel
     */
    public void flowStamp(ModuleModel stamp) {
        if (stamp != null) {
            // 外用剤の bundleNumber を補正する
            StampModifier.adjustNumber(stamp);
            StampHolder h = new StampHolder(this, stamp);
            h.setTransferHandler(stampHolderTransferHandler);
            KarteStyledDocument doc = getDocument();
            doc.flowStamp(h);
        }
    }

    /**
     * このペインにシェーマを挿入する.
     *
     * @param schema シェーマ
     */
    public void stampSchema(final SchemaModel schema) {
        if (schema != null) {
            EventQueue.invokeLater(() -> {
                SchemaHolder h = new SchemaHolder(KartePane.this, schema);
                h.setTransferHandler(schemaHolderTransferHandler);
                KarteStyledDocument doc = getDocument();
                doc.stampSchema(h);
            });
        }
    }

    /**
     * このペインにシェーマを挿入する.
     *
     * @param schema シェーマ
     */
    public void flowSchema(SchemaModel schema) {
        if (schema != null) {
            SchemaHolder h = new SchemaHolder(this, schema);
            h.setTransferHandler(schemaHolderTransferHandler);
            KarteStyledDocument doc = (KarteStyledDocument) getTextPane().getDocument();
            doc.flowSchema(h);
        }
    }

    /**
     * このペインに TextStamp を挿入する.
     *
     * @param s TextStamp
     */
    public void insertTextStamp(final String s) {
        EventQueue.invokeLater(() -> {
            KarteStyledDocument doc = getDocument();
            doc.insertTextStamp(s);
        });
    }

    /**
     * StampInfoがDropされた時，そのデータをペインに挿入する.
     * ドロップされたスタンプが１個の時
     *
     * @param stampInfo ドロップされたスタンプ情報
     */
    public void stampInfoDropped(ModuleInfoBean stampInfo) {
        // stampInfoDropped(list) で処理してコードを簡略化するため要素１個の List にする
        List<ModuleInfoBean> list = new ArrayList<>(1);
        list.add(stampInfo);

        // Drop された StampInfo の属性に応じて処理を振分ける
        String entity = stampInfo.getEntity();
        String role = stampInfo.getStampRole();

        // 病名の場合は２号カルテペインには展開しない
        if (entity.equals(IInfoModel.ENTITY_DIAGNOSIS)) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        // Text スタンプを挿入する
        if (entity.equals(IInfoModel.ENTITY_TEXT)) {
            textStampInfoDropped(list);
            return;
        }
        // ORCA 入力セットの場合
        if (role.equals(IInfoModel.ROLE_ORCA_SET)) {
            applyOrcaSet(stampInfo);
            return;
        }
        // データベースに保存されているスタンプを挿入する
        if (stampInfo.isSerialized()) {
            stampInfoDropped(list);
            return;
        }
        // Stamp エディタを起動する
        logger.debug("launch stampEditor = " + entity);
        ModuleModel stamp = new ModuleModel();
        stamp.setModuleInfo(stampInfo);

        StampEditorDialog stampEditor = new StampEditorDialog(entity, stamp);
        stampEditor.addPropertyChangeListener(this);
        stampEditor.start();
        logger.debug("stampEditor started");
    }

    /**
     * StampInfo（スタンプ箱の項目）が 複数 Drop された時，そのデータをペインに挿入する.
     * 複数がドロップされたときはこちらが呼ばれる
     *
     * @param addList スタンプ情報のリスト
     */
    public void stampInfoDropped(final List<ModuleInfoBean> addList) {
        // 4 個以上一気にドロップされたら警告を出す
        if (addList.size() >= 4) {
            int ans = JSheet.showConfirmDialog(parent.getContext().getFrame(),
                    addList.size() + "個のスタンプが同時にドロップされましたが続けますか", "スタンプ挿入確認",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE
            );
            if (ans != JOptionPane.YES_OPTION) {
                return;
            }
        }

        final StampDelegater sdl = new StampDelegater();

        DBTask<List<StampModel>> task = new DBTask<List<StampModel>>(parent.getContext()) {

            @Override
            protected List<StampModel> doInBackground() {
                return sdl.getStamp(addList);
            }

            @Override
            public void succeeded(List<StampModel> list) {
                logger.debug("stampInfoDropped succeeded");
                if (list != null) {

                    List<ModuleModel> duplicateCheckList = new ArrayList<>();

                    for (int i = 0; i < list.size(); i++) {
                        ModuleInfoBean stampInfo = addList.get(i);
                        StampModel stampModel = list.get(i);
                        IInfoModel stamp = stampModel.getStamp();
                        if (stamp != null) {
                            ModuleModel module = new ModuleModel();
                            module.setModel(stamp);
                            module.setModuleInfo(stampInfo);
                            stamp(module);

                            duplicateCheckList.add(module);
                        }
                    }

                    // スタンプ重複チェック
                    for (ModuleModel module : duplicateCheckList) {
                        int count = StampModifier.checkDuplicates(module, KartePane.this);
                        if (count > 0) {
                            break;
                        }
                    }
                } else {
                    showNoStampModelMessage();
                }
            }
        };

        task.execute();
    }

    /**
     * TextStampInfo が Drop された時の処理を行なう.
     *
     * @param addList List of ModuleInfoBean (= stampInfo)
     */
    public void textStampInfoDropped(final List<ModuleInfoBean> addList) {

        final StampDelegater sdl = new StampDelegater();

        DBTask<List<StampModel>> task = new DBTask<List<StampModel>>(parent.getContext()) {

            @Override
            protected List<StampModel> doInBackground() {
                return sdl.getStamp(addList);
            }

            @Override
            public void succeeded(List<StampModel> list) {
                logger.debug("textStampInfoDropped succeeded");
                if (list != null) {
                    list.stream().map(StampModel::getStamp).filter(Objects::nonNull)
                            .map(model -> model.toString() + "\n").forEach(KartePane.this::insertTextStamp);
                } else {
                    showNoStampModelMessage();
                }
            }
        };

        task.execute();
    }

    private void showNoStampModelMessage() {
        JOptionPane.showMessageDialog(null, "実体のないスタンプです。削除してください。",
                ClientContext.getFrameTitle("実体のないスタンプ"),
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * ORCA の入力セットを取得してこのペインに展開する.
     */
    private void applyOrcaSet(final ModuleInfoBean stampInfo) {

        DBTask<List<ModuleModel>> task = new DBTask<List<ModuleModel>>(parent.getContext()) {

            @Override
            protected List<ModuleModel> doInBackground() {
                OrcaDelegater delegater = new OrcaDelegater();
                return delegater.getStamp(stampInfo);
            }

            @Override
            public void succeeded(List<ModuleModel> models) {
                logger.debug("applyOrcaSet succeeded");
                if (models != null) {
                    models.forEach(KartePane.this::stamp);
                }
            }
        };

        task.execute();
    }

    private void showMetaDataMessage() {

        Window w = SwingUtilities.getWindowAncestor(getTextPane());
        JOptionPane.showMessageDialog(w,
                "画像のメタデータが取得できず、読み込むことができません。",
                ClientContext.getFrameTitle("画像インポート"),
                JOptionPane.WARNING_MESSAGE);
    }

    private boolean showMaxSizeMessage() {

        int maxImageWidth = ClientContext.getInt("image.max.width");
        int maxImageHeight = ClientContext.getInt("image.max.height");
        final Preferences pref = Preferences.userNodeForPackage(this.getClass());

        String title = ClientContext.getFrameTitle("画像サイズについて");
        JLabel msg1 = new JLabel("カルテに挿入する画像は、最大で " + maxImageWidth + " x " + maxImageHeight + " pixcel に制限しています。");
        JLabel msg2 = new JLabel("画像を縮小しカルテに展開しますか?");

        final JCheckBox cb = new JCheckBox("今後このメッセージを表示しない");
        cb.setFont(new Font("Dialog", Font.PLAIN, 10));
        cb.addActionListener(e -> pref.putBoolean("showImageSizeMessage", !cb.isSelected()));

        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        p1.add(msg1);
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        p2.add(msg2);
        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        p3.add(cb);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(p1);
        box.add(p2);
        box.add(p3);
        box.setBorder(BorderFactory.createEmptyBorder(0, 0, 11, 11));
        Window w = SwingUtilities.getWindowAncestor(getTextPane());

        int option = JOptionPane.showOptionDialog(w,
                new Object[]{box},
                ClientContext.getFrameTitle(title),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"縮小する", "取消す"}, "縮小する");
        return option == 0;
    }

    private void showNoReaderMessage() {
        Window w = SwingUtilities.getWindowAncestor(getTextPane());
        JOptionPane.showMessageDialog(w,
                "選択した画像を読むことができるリーダが存在しません。",
                ClientContext.getFrameTitle("画像インポート"),
                JOptionPane.WARNING_MESSAGE);
    }

    public void imageEntryDropped(final ImageEntry entry) {

        int width = entry.getWidth();
        int height = entry.getHeight();
        int maxImageWidth = ClientContext.getInt("image.max.width");
        int maxImageHeight = ClientContext.getInt("image.max.height");
        Preferences pref = Preferences.userNodeForPackage(this.getClass());
        boolean ok = true;

        if (width == 0 || height == 0) {
            Icon icon = entry.getImageIcon();
            width = icon.getIconWidth();
            height = icon.getIconHeight();
            if (width > maxImageWidth || height > maxImageHeight) {
                if (pref.getBoolean("showImageSizeMessage", true)) {
                    ok = showMaxSizeMessage();
                }
                //showMetaDataMessage();
                //ok = false;
            }
        } else if (width > maxImageWidth || height > maxImageHeight) {
            if (pref.getBoolean("showImageSizeMessage", true)) {
                ok = showMaxSizeMessage();
            }
        }

        if (!ok) {
            return;
        }

        DBTask<ImageIcon> task = new DBTask<ImageIcon>(parent.getContext()) {

            @Override
            protected ImageIcon doInBackground() throws Exception {

                URL url = new URL(entry.getUrl());
                BufferedImage importImage = ImageIO.read(url);

                int width = importImage.getWidth();
                int height = importImage.getHeight();
                int maxImageWidth = ClientContext.getInt("image.max.width");
                int maxImageHeight = ClientContext.getInt("image.max.height");

                if (width > maxImageWidth || height > maxImageHeight) {
                    importImage = ImageHelper.getFirstScaledInstance(importImage, maxImageWidth);
                }

                return new ImageIcon(importImage);
            }

            @Override
            public void succeeded(ImageIcon icon) {

                logger.debug("imageEntryDropped succeeded");

                if (icon != null) {

                    final SchemaModel schema = new SchemaModel();
                    schema.setIcon(icon);

                    // IInfoModel として ExtRef を保持している
                    ExtRefModel ref = new ExtRefModel();
                    ref.setContentType("image/jpeg");
                    ref.setTitle("Schema Image");
                    schema.setExtRef(ref);

                    stampId++;
                    String fileName = getDocId() + "-" + stampId + ".jpg";
                    schema.setFileName(fileName);
                    ref.setHref(fileName);

                    // JavaFX thread で立ち上げ
                    Platform.runLater(() -> {
                        SchemaEditor editor = new SchemaEditorImpl();
                        editor.setSchema(schema);
                        editor.setEditable(true);
                        editor.addPropertyChangeListener(KartePane.this);
                        editor.start();
                    });
                }
            }
        };
        task.execute();
    }

    /**
     * Schema が DnD された場合，シェーマエディタを開いて編集する.
     *
     * @param path path to image
     */
    public void insertImage(String path) {

        if (path == null) {
            return;
        }

        String suffix = path.toLowerCase();
        int index = suffix.lastIndexOf('.');
        if (index == 0) {
            showNoReaderMessage();
            return;
        }
        suffix = suffix.substring(index + 1);

        Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(suffix);

        if (!readers.hasNext()) {
            showNoReaderMessage();
            return;
        }

        ImageReader reader = readers.next();
        logger.debug("reader = " + reader.getClass().getName());
        int width, height;
        String name;
        try {
            File file = new File(path);
            name = file.getName();
            reader.setInput(new FileImageInputStream(file), true);
            width = reader.getWidth(0);
            height = reader.getHeight(0);

        } catch (IOException e) {
            System.out.println("KartePane.java: " + e);
            logger.warn(e.getMessage());
            return;
        }

        ImageEntry entry = new ImageEntry();
        entry.setPath(path);
        entry.setFileName(name);
        entry.setNumImages(1);
        entry.setWidth(width);
        entry.setHeight(height);
        // エディタ起動
        imageEntryDropped(entry);
    }

    /**
     * StampEditor の編集が終了するとここへ通知される.
     * 通知されたスタンプをペインに挿入する.
     *
     * @param e PropertyChangeEvent
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {

        switch (e.getPropertyName()) {
            case SchemaEditor.IMAGE_PROP:
                SchemaModel schema = (SchemaModel) e.getNewValue();
                if (schema != null) {
                    // 編集されたシェーマをこのペインに挿入する
                    stampSchema(schema);
                }
                break;

            case StampEditorDialog.VALUE_PROP:
                Object o = e.getNewValue();
                if (o != null) {
                    // 編集された Stamp をこのペインに挿入する
                    ModuleModel stamp = (ModuleModel) o;
                    stamp(stamp);
                }
                break;
        }
        // stamp が挿入されたら toFront する.
        this.getParent().getContext().getFrame().toFront();
    }

    /**
     * メニュー制御のため，ペースト可能かどうかを返す.
     *
     * @return ペースト可能な時 true
     */
    protected boolean canPaste() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

        return Objects.nonNull(t) &&
                (t.isDataFlavorSupported(DataFlavor.stringFlavor)
                || (getMyRole().equals(IInfoModel.ROLE_P) && t.isDataFlavorSupported(OrderListTransferable.orderListFlavor))
                || (getMyRole().equals(IInfoModel.ROLE_SOA) && t.isDataFlavorSupported(SchemaListTransferable.schemaListFlavor)));
    }

    /**
     * このペインからスタンプを削除する.
     *
     * @param sh 削除するスタンプのホルダ
     */
    public void removeStamp(StampHolder sh) {
        getDocument().removeStamp(sh.getStartPos(), 2);
    }

    /**
     * このペインからスタンプを削除する.
     *
     * @param sh 削除するスタンプのホルダリスト
     */
    public void removeStamp(StampHolder[] sh) {
        if (sh != null && sh.length > 0) {
            for (StampHolder h : sh) {
                removeStamp(h);
            }
        }
    }

    /**
     * このペインからシェーマを削除する.
     *
     * @param sh 削除するシェーマのホルダ
     */
    public void removeSchema(SchemaHolder sh) {
        getDocument().removeStamp(sh.getStartPos(), 2);
    }

    /**
     * このペインからシェーマを削除する.
     *
     * @param sh 削除するシェーマのホルダリスト
     */
    public void removeSchema(SchemaHolder[] sh) {
        if (sh != null && sh.length > 0) {
            for (SchemaHolder h : sh) {
                removeSchema(h);
            }
        }
    }

    /**
     * ChartMediator で addChain されてここが呼ばれる.
     */
    public void undo() {
        undoManager.undo();
    }

    public void redo() {
        undoManager.redo();
    }

    // KartePane の状態　(_TEXT はテキストが選択された状態)
    private enum State {
        NONE, SOA, SOA_TEXT, SCHEMA, P, P_TEXT, STAMP
    }
}
