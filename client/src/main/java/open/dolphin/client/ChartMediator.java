package open.dolphin.client;

import open.dolphin.helper.MenuSupport;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.project.Project;
import open.dolphin.stampbox.*;
import open.dolphin.ui.Focuser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Objects;

/**
 * Mediator (MenuSupport) class to control ChartDocument menus.<br>
 * ChartImpl, EditorFrame でインスタンスが作られ，KarteComposite インターフェースを持つクラスが選択されると，
 * メニューをコントロールする {@link open.dolphin.client.KarteComposite KarteComposite#enter/exit} を送る.<br>
 * MenuSupport の chains[0] を KarteComposite レイヤー，chains[1] を ChartDocument レイヤーとして使うために，
 * {@link open.dolphin.client.ChartMediator#addChartDocumentChain addChartDocumentChain} (= addChain2)，
 * {@link  open.dolphin.client.ChartMediator#addKarteCompositeChain addKarteCompositeChain} (= addChain) を作ってある.
 * <ul>
 * <li>chains[0] = KaretComposite
 * <li>chains[1] = ChartDocument
 * <li>chains[2] = this
 * <li>chains[3] = ChartImpl or EditorFrame
 * </ul>
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public final class ChartMediator extends MenuSupport {
    // Font size
    public static final Integer[] FONT_SIZE = {9, 11, 13, 16, 18, 24, 36};
    public static final int DEFAULT_FONT_SIZE = FONT_SIZE[2];

    private final Chart chart;
    private final Logger logger;
    private int curFontSizeIndex = 2;
    private KarteComposite<?> curKarteComposit;

    /**
     * Create ChartMediator.
     *
     * @param owner Owner of this mediator
     */
    public ChartMediator(Chart owner) {
        super(owner);

        logger = LoggerFactory.getLogger(ChartMediator.class);
        chart = owner;

        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

        // focus を取った KarteComposite に menu の activate/deactivate 情報を送る
        focusManager.addPropertyChangeListener("focusOwner", e -> {

            Window focusedWindow = ((KeyboardFocusManager) e.getSource()).getActiveWindow();

            if (chart.getFrame() == focusedWindow) {
                Component comp = (Component) e.getNewValue();
                if (comp instanceof JTextPane) {
                    // KartePane に入っている JTextPane は ClientProperty に親の KartePane を入れてある
                    Object obj = ((JComponent) comp).getClientProperty("kartePane");
                    if (obj instanceof KartePane) {
                        // KartePane
                        setCurKarteComposit((KarteComposite) obj);
                    }
                } else if (comp instanceof KarteComposite) {
                    // StampHolder など
                    setCurKarteComposit((KarteComposite) comp);
                }
            }
        });
    }

    public void setCurKarteComposit(KarteComposite<?> newComposit) {

        KarteComposite<?> old = curKarteComposit;
        curKarteComposit = newComposit;
        addKarteCompositeChain(curKarteComposit);

        if (old != curKarteComposit) {
            logger.debug("ChartMediator: composit changed in " + chart.getClass());

            if (old != null) {
                old.exit(getActions());
            }
            // KarteComposite.enter 内で enable/disable の初期化が必要な action
            enableAction(GUIConst.ACTION_CUT, false);
            enableAction(GUIConst.ACTION_COPY, false);
            enableAction(GUIConst.ACTION_PASTE, false);
            enableAction(GUIConst.ACTION_INSERT_TEXT, false);
            enableAction(GUIConst.ACTION_INSERT_SCHEMA, false);
            enableAction(GUIConst.ACTION_INSERT_STAMP, false);
            // undo/redo は基本 false で，あとは KarteComposite で制御されるべき
            enableAction(GUIConst.ACTION_UNDO, false);
            enableAction(GUIConst.ACTION_REDO, false);

            if (curKarteComposit != null) {
                logger.debug("ChartMediator: curKarteComposit != null");
                // KarteComposite 内で enable/disable は初期化される
                curKarteComposit.enter(getActions());
            }
        }
    }

    /**
     * KarteComosite 層の Object を addChain する.
     * chain[0] に入る.
     *
     * @param karteComposite KarteComposite
     */
    public void addKarteCompositeChain(KarteComposite<?> karteComposite) {
        addChain(karteComposite);
    }

    /**
     * KarteComposite 層の Object を返す.
     *
     * @return KarteComposite
     */
    public KarteComposite<?> getKarteCompositeChain() {
        return (KarteComposite) getChain();
    }

    /**
     * ChartDocument 層の Object を addChain する.
     * chain[1] に入る.
     *
     * @param chartDocument ChartDocument
     */
    public void addChartDocumentChain(ChartDocument chartDocument) {
        addChain2(chartDocument);
    }

    /**
     * ChartDocument 層の Object を返す.
     *
     * @return ChartDocument
     */
    public ChartDocument getChartDocumentChain() {
        return (ChartDocument) getChain2();
    }

    @Override
    public void registerActions(ActionMap map) {

        super.registerActions(map);

        // 昇順降順を Preference から取得し設定しておく
        boolean asc = Project.getPreferences().getBoolean(Project.DOC_HISTORY_ASCENDING, false);
        Action action = asc ? map.get(GUIConst.ACTION_ASCENDING) : map.get(GUIConst.ACTION_DESCENDING);
        JRadioButtonMenuItem rdi = (JRadioButtonMenuItem) action.getValue("menuItem");
        rdi.setSelected(true);
    }

    public void dispose() {
    }

    /**
     * 現在の {@code KarteCompsite<T>} の中身の T (JComponent) を返す.
     *
     * @return KarteComposite の中身の JComponent
     */
    public JComponent getCurrentComponent() {
        if (curKarteComposit != null) {
            return (JComponent) curKarteComposit.getComponent();
        }
        return null;
    }

    /**
     * メニューリスナの実装.
     * 挿入及びテキストメニューが選択された時の処理を行う.
     *
     * @param e MenuEvent
     */
    @Override
    public void menuSelected(MenuEvent e) {

        // 挿入とテキストメニューにリスナが登録されている
        JMenu selectedMenu = (JMenu) e.getSource();
        String cmd = selectedMenu.getActionCommand();

        //
        // 挿入メニューの時
        // StampBox のツリーをメニューにする
        //
        if (cmd.equals(GUIConst.ACTION_INSERT_MENU)) {

            selectedMenu.removeAll();

            // StampBox の全ツリーを取得する
            List<StampTree> trees = getStampBox().getAllTrees();

            // ツリーをイテレートする
            trees.forEach(tree -> {

                switch (tree.getEntity()) {
                    case IInfoModel.ENTITY_DIAGNOSIS:
                        // 傷病名の時，傷病名メニューを構築し追加する
                        selectedMenu.add(createDiagnosisMenu(tree));
                        selectedMenu.addSeparator();
                        break;
                    case IInfoModel.ENTITY_TEXT:
                        // テキストの時，テキストメニューを構築し追加する
                        selectedMenu.add(createTextMenu(tree));
                        selectedMenu.addSeparator();
                        break;
                    default:
                        // 通常のPオーダの時
                        selectedMenu.add(createStampMenu(tree));
                        break;
                }
            });

        } else if (cmd.equals(GUIConst.ACTION_TEXT_MENU)) {
            //
            // テキストメニューの場合，スタイルを制御する
            //
            adjustStyleMenu();
        }
    }

    @Override
    public void menuDeselected(MenuEvent e) {
    }

    @Override
    public void menuCanceled(MenuEvent e) {
    }

    /**
     * フォーマット関連メニューを調整する.
     */
    private void adjustStyleMenu() {

        boolean enabled = false;
        KartePane kartePane;

        if (getChartDocumentChain() instanceof KarteEditor) {
            KarteEditor editor = (KarteEditor) getChartDocumentChain();
            kartePane = editor.getSOAPane();
            enabled = kartePane.getTextPane().isEditable();
        }

        // サブメニューを制御する
        getAction("size").setEnabled(enabled);
        getAction("style").setEnabled(enabled);
        getAction("justify").setEnabled(enabled);
        getAction("color").setEnabled(enabled);

        // メニューアイテムを制御する
        //getAction(GUIConst.ACTION_RESET_STYLE).setEnabled(enabled);

        getAction("fontRed").setEnabled(enabled);
        getAction("fontOrange").setEnabled(enabled);
        getAction("fontYellow").setEnabled(enabled);
        getAction("fontGreen").setEnabled(enabled);
        getAction("fontBlue").setEnabled(enabled);
        getAction("fontPurple").setEnabled(enabled);
        getAction("fontGray").setEnabled(enabled);

        getAction("fontLarger").setEnabled(enabled);
        getAction("fontSmaller").setEnabled(enabled);
        getAction("fontStandard").setEnabled(enabled);

        getAction("fontBold").setEnabled(enabled);
        getAction("fontItalic").setEnabled(enabled);
        getAction("fontUnderline").setEnabled(enabled);

        getAction("leftJustify").setEnabled(enabled);
        getAction("centerJustify").setEnabled(enabled);
        getAction("rightJustify").setEnabled(enabled);
    }

    /**
     * スタンプTreeから傷病名メニューを構築する.
     *
     * @param stampTree StampTree
     */
    private JMenu createDiagnosisMenu(StampTree stampTree) {
        //
        // chain の先頭が DiagnosisDocument の時のみ使用可能とする
        //
        JMenu myMenu;
        Object obj = getChartDocumentChain(); // chain の先頭
        DiagnosisDocument diagnosis = obj instanceof DiagnosisDocument ? (DiagnosisDocument) obj : null;

        if (diagnosis == null) {
            // chainの先頭がDiagnosisでない場合はメニューを disable にする
            myMenu = new JMenu(stampTree.getTreeName());
            myMenu.setEnabled(false);

        } else {
            // 傷病名Tree，テーブル，ハンドラからメニューを構築する
            StampTreeMenuBuilder builder = new StampTreeMenuBuilder(stampTree);
            builder.addStampTreeMenuListener(new DefaultStampTreeMenuListener(diagnosis.getDiagnosisTable()));
            myMenu = new JMenu();
            builder.build(myMenu);
        }
        return myMenu;
    }

    /**
     * 引数のポップアップメニューへ傷病名メニューを追加する.
     *
     * @param popup 傷病名メニューを追加するポップアップメニュー
     */
    public void addDiseaseMenu(JPopupMenu popup) {
        //
        // Chain の ChartDocument層 が DiagnosisDocument の時のみ追加する
        //
        Object obj = getChartDocumentChain();
        DiagnosisDocument diagnosis = obj instanceof DiagnosisDocument ? (DiagnosisDocument) obj : null;

        StampTree stampTree = getStampBox().getStampTree(IInfoModel.ENTITY_DIAGNOSIS);

        if (stampTree != null) {

            if (diagnosis == null) {
                JMenu myMenu = new JMenu(stampTree.getTreeName());
                myMenu.setEnabled(false);
                popup.add(myMenu);

            } else {
                StampTreeMenuBuilder builder = new StampTreeMenuBuilder(stampTree);
                builder.addStampTreeMenuListener(new DefaultStampTreeMenuListener(diagnosis.getDiagnosisTable()));
                builder.build(popup);
            }
        }
    }

    /**
     * スタンプTreeからテキストメニューを構築する.
     *
     * @param stampTree StampTree
     */
    private JMenu createTextMenu(StampTree stampTree) {
        //
        // chain の先頭が KarteEditor でかつ SOAane が編集可の場合のみメニューが使える
        //
        boolean enabled = false;

        KartePane kartePane = null;
        Object obj = getChartDocumentChain();

        if (obj instanceof KarteEditor) {
            KarteEditor editor = (KarteEditor) obj;
            kartePane = editor.getSOAPane();
            if (kartePane != null) {
                enabled = kartePane.getTextPane().isEditable();
            }
        }

        JMenu myMenu = null;

        if (!enabled) {
            myMenu = new JMenu(stampTree.getTreeName());
            myMenu.setEnabled(false);

        } else {
            //
            // TextTree，JTextPane，handler からメニューを構築する
            // PPane にも落とさなければならない TODO
            // JComponent comp = kartePane.getTextPane();
            // TransferHandler handler = comp.getTransferHandler();

            // 2007-03-31
            // 直近でフォーカスを得ているコンポーネント(JTextPan）へ挿入する
            //
            JComponent comp = getCurrentComponent();
            if (comp == null) {
                comp = kartePane.getTextPane();
            }
            if (comp != null) {
                StampTreeMenuBuilder builder = new StampTreeMenuBuilder(stampTree);
                builder.addStampTreeMenuListener(new DefaultStampTreeMenuListener(comp));
                myMenu = new JMenu();
                builder.build(myMenu);
            }
        }

        return myMenu;
    }

    /**
     * 引数のポップアップメニューへテキストメニューを追加する.
     *
     * @param popup テキストメニューを追加するポップアップメニュー
     */
    public void addTextMenu(JPopupMenu popup) {

        boolean enabled = false;

        KartePane kartePane = null;
        Object obj = getChartDocumentChain();

        if (obj instanceof KarteEditor) {
            KarteEditor editor = (KarteEditor) obj;
            kartePane = editor.getSOAPane();
            if (kartePane != null) {
                enabled = kartePane.getTextPane().isEditable();
            }
        }

        StampTree stampTree = getStampBox().getStampTree(IInfoModel.ENTITY_TEXT);

        // ASP スタンプボックスで entity に対応する Tree がない場合がある
        if (stampTree != null) {

            if (!enabled) {
                JMenu myMenu = new JMenu(stampTree.getTreeName());
                myMenu.setEnabled(false);
                popup.add(myMenu);

            } else {
                JComponent comp = getCurrentComponent();
                if (comp == null) {
                    comp = kartePane.getTextPane();
                }
                if (comp != null) {
                    StampTreeMenuBuilder builder = new StampTreeMenuBuilder(stampTree);
                    builder.addStampTreeMenuListener(new DefaultStampTreeMenuListener(comp));
                    builder.build(popup);
                }
            }
        }
    }

    /**
     * スタンプメニューを構築する.
     *
     * @param stampTree StampTree
     */
    private JMenu createStampMenu(StampTree stampTree) {
        //
        // chain の先頭が KarteEditor でかつ Pane が編集可の場合のみメニューが使える
        //
        boolean enabled = false;

        KartePane kartePane = null;
        Object obj = getChartDocumentChain();

        if (obj instanceof KarteEditor) {
            KarteEditor editor = (KarteEditor) obj;
            kartePane = editor.getPPane();
            if (kartePane != null) {
                enabled = kartePane.getTextPane().isEditable();
            }
        }

        JMenu myMenu = null;

        if (!enabled) {
            myMenu = new JMenu(stampTree.getTreeName());
            myMenu.setEnabled(false);

        } else {
            // StampTree，JTextPane，Handler からメニューを構築する
            StampTreeMenuBuilder builder = new StampTreeMenuBuilder(stampTree);
            builder.addStampTreeMenuListener(new DefaultStampTreeMenuListener(kartePane.getTextPane()));
            myMenu = new JMenu();
            builder.build(myMenu);
        }

        return myMenu;
    }

    /**
     * PPane のコンテキストメニューまたはツールバーの stampIcon へスタンプメニューを追加する.
     *
     * @param menu      Ppane のコンテキストメニュー
     * @param kartePane PPnae
     */
    public void addStampMenu(JPopupMenu menu, final KartePane kartePane) {

        // 引数のPaneがPかつ編集可の時のみ追加する
        // コンテキストメニューなのでこれはOK
        if (kartePane != null && kartePane.getMyRole().equals(IInfoModel.ROLE_P) && kartePane.getTextPane().isEditable()) {

            StampBoxPlugin stampBox = getStampBox();

            List<StampTree> trees = stampBox.getAllPTrees();

            StampTreeMenuBuilder builder = new StampTreeMenuBuilder(trees);
            builder.addStampTreeMenuListener(new DefaultStampTreeMenuListener(kartePane.getTextPane()));
            builder.build(menu);
        }
    }

    /**
     * 引数のポップアップメニューへスタンプメニューを追加する.
     * このメソッドはツールバーの stamp icon の actionPerformed からコールされる.
     *
     * @param popup JPopupMenu
     */
    public void addStampMenu(JPopupMenu popup) {

        boolean enabled = false;

        KartePane kartePane = null;
        Object obj = getChartDocumentChain();

        if (obj instanceof KarteEditor) {
            KarteEditor editor = (KarteEditor) obj;
            kartePane = editor.getPPane();
            if (kartePane != null) {
                enabled = kartePane.getTextPane().isEditable();
            }
        }

        if (enabled) {
            addStampMenu(popup, kartePane);
        }
    }

    /**
     * search pattern に一致した diagnosis の popup menu を作る.
     *
     * @param searchPattern 検索パターン
     * @param listener メニューが選択された場合に通知するリスナ
     * @return JPopupMenu
     */
    public JPopupMenu createDiagnosisPopup(String searchPattern, StampTreeMenuListener listener) {

        StampTree tree = getStampTree(IInfoModel.ENTITY_DIAGNOSIS);
        StampTreeMenuBuilder builder = new StampTreeMenuBuilder(tree, searchPattern);
        builder.addStampTreeMenuListener(listener);

        JPopupMenu popup = new JPopupMenu();
        builder.buildRootless(popup);
        return popup;
    }

    /**
     * search pattern に一致した全スタンプの popup menu を作る.
     *
     * @param searchPattern 検索パターン
     * @param listener メニューが選択された場合に通知するリスナ
     * @return JPopupMenu
     */
    public JPopupMenu createAllStampPopup(String searchPattern, StampTreeMenuListener listener) {
        List<StampTree> allTrees = getStampBox().getAllTrees();
        StampTreeMenuBuilder builder = new StampTreeMenuBuilder(allTrees, searchPattern);
        builder.addStampTreeMenuListener(listener);


        JPopupMenu popup = new JPopupMenu();
        builder.buildRootless(popup);
        return popup;
    }

    /**
     * 指定された entity の StampTree を返す.
     *
     * @param entity Entity
     * @return StampTree
     */
    public StampTree getStampTree(String entity) {
        return getStampBox().getStampTree(entity);
    }

    /**
     * StampBoxPlugin を返す.
     *
     * @return StampBoxPlugin
     */
    public StampBoxPlugin getStampBox() {
        return (StampBoxPlugin) chart.getContext().getPlugin("stampBox");
    }

    /**
     * 指定された entity の StampTree が存在するかどうか.
     *
     * @param entity Entity
     * @return StampTree が存在すれば true
     */
    public boolean hasTree(String entity) {
        StampBoxPlugin stBox = (StampBoxPlugin) chart.getContext().getPlugin("stampBox");
        StampTree tree = stBox.getStampTree(entity);
        return tree != null;
    }

    //
    // メニューのアクション (MenuSupport から reflection で実行される
    //
    public void cut() {
        if (curKarteComposit != null) {
            JComponent focusOwner = getCurrentComponent();
            if (focusOwner != null) {
                Action a = focusOwner.getActionMap().get(TransferHandler.getCutAction().getValue(Action.NAME));
                if (a != null) {
                    a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
                    setCurKarteComposit(null);
                }
            }
        }
    }

    public void copy() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = focusOwner.getActionMap().get(TransferHandler.getCopyAction().getValue(Action.NAME));
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }

    public void paste() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = focusOwner.getActionMap().get(TransferHandler.getPasteAction().getValue(Action.NAME));
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }

    public void delete() {
    }

    public void resetStyle() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner instanceof JTextPane) {
            JTextPane pane = (JTextPane) focusOwner;
            pane.setCharacterAttributes(SimpleAttributeSet.EMPTY, true);
        }
    }

    public void fontLarger() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            if (curFontSizeIndex < 6) {
                setFontSize(FONT_SIZE[++curFontSizeIndex]);
            }
            enableAction("fontLarger", curFontSizeIndex < 6);
        }
    }

    public void fontSmaller() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            if (curFontSizeIndex > 0) {
                setFontSize(FONT_SIZE[--curFontSizeIndex]);
            }
            enableAction("fontSmaller", curFontSizeIndex > 0);
        }
    }

    public void fontStandard() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            setFontSize(DEFAULT_FONT_SIZE);
            enableAction("fontSmaller", true);
            enableAction("fontLarger", true);
        }
    }

    public void setFontSize(int size) {
        JComponent focusOwner = getCurrentComponent();
        Action a = new StyledEditorKit.FontSizeAction("font-size-" + size, size);
        a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
    }

    public void fontBold() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = focusOwner.getActionMap().get("font-bold");
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }

    public void fontItalic() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = focusOwner.getActionMap().get("font-italic");
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }


    public void fontUnderline() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = focusOwner.getActionMap().get("font-underline");
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }

    public void leftJustify() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = focusOwner.getActionMap().get("left-justify");
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }

    public void centerJustify() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = focusOwner.getActionMap().get("center-justify");
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }

    public void rightJustify() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = focusOwner.getActionMap().get("right-justify");
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
            }
        }
    }

    public void colorAction(Color color) {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = new StyledEditorKit.ForegroundAction("color", color);
            a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, "foreground"));
        }
    }

    public void fontRed() {
        colorAction(new Color(255, 43, 58));
    }

    public void fontOrange() {
        colorAction(new Color(255, 148, 44));
    }

    public void fontYellow() {
        colorAction(new Color(242, 207, 43));
    }

    public void fontGreen() {
        colorAction(new Color(139, 209, 40));
    }

    public void fontBlue() {
        colorAction(new Color(10, 140, 211));
    }

    public void fontPurple() {
        colorAction(new Color(223, 61, 154));
    }

    public void fontGray() {
        colorAction(new Color(130, 130, 130));
    }

    public void fontBlack() {
        colorAction(Color.BLACK);
    }

    public void searchStamp() {
        ChartImpl chart = ChartImpl.getAllChart().stream().filter(c -> c.getFrame().isActive()).findAny().orElse(null);
        if (Objects.nonNull(chart)) {
            ChartSearchPanel panel = chart.getChartSearchPanel();
            panel.show(ChartSearchPanel.Card.STAMP);
            Focuser.requestFocus(panel.getStampSearchField());
            return;
        }

        EditorFrame frame = EditorFrame.getAllEditorFrames().stream().filter(f -> f.getFrame().isActive()).findAny().orElse(null);
        if (Objects.nonNull(frame)) {
            Focuser.requestFocus(frame.getChartToolBar().getStampSearchField());
        }
    }
}
