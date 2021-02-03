package open.dolphin.client;

import open.dolphin.helper.MenuActionManager;
import open.dolphin.helper.MenuActionManager.MenuAction;
import open.dolphin.helper.MenuSupport;

import javax.swing.*;
import java.util.Objects;

/**
 * Menu Factory.
 *
 * @author Minagawa, Kazushi
 * @author pns
 */
public class MenuFactory {
    private static final String SHORT_CUT_KEY_MASK = ClientContext.isMac() ? "meta" : "ctrl";

    private MenuSupport main;
    private MenuSupport chart; // ChartMediator
    private JMenuBar menuBar;
    private JPanel toolPanel;
    private ActionMap actionMap;

    public MenuFactory() {
    }

    public void setMenuSupports(MenuSupport main, MenuSupport chart) {
        this.main = main;
        this.chart = chart;
    }

    /**
     * 作った JMenuBar を返す.
     *
     * @return JMenuBar
     */
    public JMenuBar getMenuBarProduct() {
        return menuBar;
    }

    /**
     * JToolBar (fileBar と editoBar) を組み合わせた JPanel を返す.
     *
     * @return JPanel
     */
    public JPanel getToolPanelProduct() {
        return toolPanel;
    }

    /**
     * ActionMap を返す.
     *
     * @return ActionMap
     */
    public ActionMap getActionMap() { return actionMap; }

    @MenuAction
    public void newKarte() {
        chart.sendToChain(GUIConst.ACTION_NEW_KARTE);
    }

    @MenuAction
    public void newDocument() {
        chart.sendToChain(GUIConst.ACTION_NEW_DOCUMENT);
    }

    @MenuAction
    public void openKarte() { main.sendToChain(GUIConst.ACTION_OPEN_KARTE); }

    @MenuAction
    public void close() {
        chart.sendToChain(GUIConst.ACTION_CLOSE);
    }

    @MenuAction
    public void save() {
        chart.sendToChain(GUIConst.ACTION_SAVE);
    }

    @MenuAction
    public void delete() {
        chart.sendToChain(GUIConst.ACTION_DELETE);
    }

    @MenuAction
    public void printerSetup() {
        main.sendToChain(GUIConst.ACTION_PRINTER_SETUP);
    }

    @MenuAction
    public void print() {
        chart.sendToChain(GUIConst.ACTION_PRINT);
    }

    @MenuAction
    public void processExit() {
        main.sendToChain(GUIConst.ACTION_PROCESS_EXIT);
    }

    @MenuAction
    public void modifyKarte() {
        chart.sendToChain(GUIConst.ACTION_MODIFY_KARTE);
    }

    @MenuAction
    public void undo() {
        chart.sendToChain(GUIConst.ACTION_UNDO);
    }

    @MenuAction
    public void redo() {
        chart.sendToChain(GUIConst.ACTION_REDO);
    }

    @MenuAction
    public void cut() {
        chart.sendToChain(GUIConst.ACTION_CUT);
    }

    @MenuAction
    public void copy() { chart.sendToChain(GUIConst.ACTION_COPY); }

    @MenuAction
    public void paste() { chart.sendToChain(GUIConst.ACTION_PASTE); }

    @MenuAction
    public void selectAll() {
        chart.sendToChain(GUIConst.ACTION_SELECT_ALL);
    }

    @MenuAction
    public void sendClaim() {
        chart.sendToChain(GUIConst.ACTION_SEND_CLAIM);
    }

    @MenuAction
    public void ascending() {
        chart.sendToChain(GUIConst.ACTION_ASCENDING);
    }

    @MenuAction
    public void descending() {
        chart.sendToChain(GUIConst.ACTION_DESCENDING);
    }

    @MenuAction
    public void showModified() {
        chart.sendToChain(GUIConst.ACTION_SHOW_MODIFIED);
    }

    @MenuAction
    public void setKarteEnviroment() {
        main.sendToChain(GUIConst.ACTION_SET_KARTE_ENVIROMENT);
    }

    @MenuAction
    public void insertDisease() { }

    @MenuAction
    public void insertText() { }

    @MenuAction
    public void insertSchema() { }

    @MenuAction
    public void insertStamp() { }

    @MenuAction
    public void simplifyStamp() { chart.sendToChain(GUIConst.ACTION_SIMPLIFY_STAMP); }

    @MenuAction
    public void selectInsurance() { }

    @MenuAction
    public void size() { }

    @MenuAction
    public void fontLarger() {
        chart.sendToChain(GUIConst.ACTION_FONT_LARGER);
    }

    @MenuAction
    public void fontSmaller() {
        chart.sendToChain(GUIConst.ACTION_FONT_SMALLER);
    }

    @MenuAction
    public void fontStandard() {
        chart.sendToChain(GUIConst.ACTION_FONT_STANDARD);
    }

    @MenuAction
    public void style() { }

    @MenuAction
    public void fontBold() {
        chart.sendToChain(GUIConst.ACTION_FONT_BOLD);
    }

    @MenuAction
    public void fontItalic() {
        chart.sendToChain(GUIConst.ACTION_FONT_ITALIC);
    }

    @MenuAction
    public void fontUnderline() {
        chart.sendToChain(GUIConst.ACTION_FONT_UNDERLINE);
    }

    @MenuAction
    public void justify() { }

    @MenuAction
    public void leftJustify() {
        chart.sendToChain(GUIConst.ACTION_LEFT_JUSTIFY);
    }

    @MenuAction
    public void centerJustify() {
        chart.sendToChain(GUIConst.ACTION_CENTER_JUSTIFY);
    }

    @MenuAction
    public void rightJustify() {
        chart.sendToChain(GUIConst.ACTION_RIGHT_JUSTIFY);
    }

    @MenuAction
    public void color() { }

    @MenuAction
    public void fontRed() {
        chart.sendToChain(GUIConst.ACTION_FONT_RED);
    }

    @MenuAction
    public void fontOrange() {
        chart.sendToChain(GUIConst.ACTION_FONT_ORANGE);
    }

    @MenuAction
    public void fontYellow() {
        chart.sendToChain(GUIConst.ACTION_FONT_YELLOW);
    }

    @MenuAction
    public void fontGreen() {
        chart.sendToChain(GUIConst.ACTION_FONT_GREEN);
    }

    @MenuAction
    public void fontBlue() {
        chart.sendToChain(GUIConst.ACTION_FONT_BLUE);
    }

    @MenuAction
    public void fontPurple() {
        chart.sendToChain(GUIConst.ACTION_FONT_PURPLE);
    }

    @MenuAction
    public void fontGray() {
        chart.sendToChain(GUIConst.ACTION_FONT_GRAY);
    }

    @MenuAction
    public void fontBlack() {
        chart.sendToChain(GUIConst.ACTION_FONT_BLACK);
    }

    @MenuAction
    public void resetStyle() {
        chart.sendToChain(GUIConst.ACTION_RESET_STYLE);
    }

    @MenuAction
    public void showStampBox() {
        main.sendToChain(GUIConst.ACTION_SHOW_STAMPBOX);
    }

    @MenuAction
    public void showSchemaBox() {
        main.sendToChain(GUIConst.ACTION_SHOW_SCHEMABOX);
    }

    @MenuAction
    public void showWaitingList() {
        main.sendToChain(GUIConst.ACTION_SHOW_WAITING_LIST);
    }

    @MenuAction
    public void showPatientSearch() { main.sendToChain(GUIConst.ACTION_SHOW_PATIENT_SEARCH); }

    @MenuAction
    public void changePassword() {
        main.sendToChain(GUIConst.ACTION_CHANGE_PASSWORD);
    }

    @MenuAction
    public void addUser() {
        main.sendToChain(GUIConst.ACTION_ADD_USER);
    }

    @MenuAction
    public void showAbout() {
        main.sendToChain(GUIConst.ACTION_SHOW_ABOUT);
    }

    @MenuAction
    public void findFirst() {
        chart.sendToChain(GUIConst.ACTION_FIND_FIRST);
    }

    @MenuAction
    public void findNext() {
        chart.sendToChain(GUIConst.ACTION_FIND_NEXT);
    }

    @MenuAction
    public void findPrevious() {
        chart.sendToChain(GUIConst.ACTION_FIND_PREVIOUS);
    }

    @MenuAction
    public void searchStamp() { chart.sendToChain(GUIConst.ACTION_SEARCH_STAMP); }

    public void build(JMenuBar menuBar) {

        actionMap = MenuActionManager.createActionMap(this);
        this.menuBar = menuBar;

        //
        // File メニュー
        //
        JMenu file = createMenu("ファイル", GUIConst.ACTION_FILE_MENU);

        // 新規カルテ
        file.add(createMenuItem("新規カルテ...", GUIConst.ACTION_NEW_KARTE,
                "N", GUIConst.ICON_FILE_16, "新しいカルテを作成します"));
        // 開く
        file.add(createMenuItem("開く...", GUIConst.ACTION_OPEN_KARTE,
                "O", GUIConst.ICON_OPEN_16, "カルテを開きます"));

        file.addSeparator();

        // 閉じる
        file.add(createMenuItem("閉じる", GUIConst.ACTION_CLOSE,
                "W", GUIConst.ICON_EMPTY_16, "カルテを閉じます"));
        // 保存
        file.add(createMenuItem("保存...", GUIConst.ACTION_SAVE,
                "S", GUIConst.ICON_SAVE_16, "カルテを保存します"));

        file.add(new JSeparator());

        // 削除
        file.add(createMenuItem("削除", GUIConst.ACTION_DELETE,
                null, GUIConst.ICON_DELETE_16, "削除します"));

        file.addSeparator();

        // 印刷設定
        file.add(createMenuItem("ページ設定...", GUIConst.ACTION_PRINTER_SETUP,
                null, GUIConst.ICON_EMPTY_16, "プリンタの設定をします"));
        // 印刷
        file.add(createMenuItem("プリント...", GUIConst.ACTION_PRINT,
                "P", GUIConst.ICON_PRINT_16, "プリントします"));

        // 終了 - Window のみ
        if (ClientContext.isWin()) {
            file.add(createMenuItem("終了", GUIConst.ACTION_PROCESS_EXIT,
                    "Q", GUIConst.ICON_EMPTY_16, "プログラムを終了します"));
        }

        //
        // Edit メニュー
        //
        JMenu edit = createMenu("編集", GUIConst.ACTION_EDIT_MENU);

        // 修正
        edit.add(createMenuItem("修正", GUIConst.ACTION_MODIFY_KARTE,
                "M", GUIConst.ICON_FILE_EDIT_16, "表示されているカルテを編集します"));

        edit.addSeparator();

        // Undo
        edit.add(createMenuItem("取り消す", GUIConst.ACTION_UNDO,
                "Z", GUIConst.ICON_UNDO_16, "作業を取り消します"));

        // Redo
        edit.add(createMenuItem("やり直す", GUIConst.ACTION_REDO,
                "shift Z", GUIConst.ICON_REDO_16, "作業をやり直します"));

        edit.addSeparator();

        // Cut
        edit.add(createMenuItem("カット", GUIConst.ACTION_CUT,
                "X", GUIConst.ICON_CUT_16, "選択部分をカットします"));
        // Copy
        edit.add(createMenuItem("コピー", GUIConst.ACTION_COPY,
                "C", GUIConst.ICON_COPY_16, "選択部分をコピーします"));
        // Paste
        edit.add(createMenuItem("ペースト", GUIConst.ACTION_PASTE,
                "V", GUIConst.ICON_PASTE_16, "カーソル位置にペーストします"));
        // SelectAll
        edit.add(createMenuItem("全てを選択", GUIConst.ACTION_SELECT_ALL,
                "A", GUIConst.ICON_EMPTY_16, "全てを選択します"));

        edit.addSeparator();

        // Find
        edit.add(createMenuItem("検索", GUIConst.ACTION_FIND_FIRST,
                "F", GUIConst.ICON_SEARCH_16, "表示されているカルテを検索します"));
        // Find Next
        edit.add(createMenuItem("次を検索", GUIConst.ACTION_FIND_NEXT,
                "G", GUIConst.ICON_EMPTY_16, "次を検索します"));
        // Find Previous
        edit.add(createMenuItem("前を検索", GUIConst.ACTION_FIND_PREVIOUS,
                "shift G", GUIConst.ICON_EMPTY_16, "前を検索します"));

        //
        // Karte メニュー
        //
        JMenu karte = createMenu("カルテ", GUIConst.ACTION_KARTE_MENU);

        // CLAIM 送信
        karte.add(createMenuItem("CLAIM 送信", GUIConst.ACTION_SEND_CLAIM,
                "L", null, "CLAIM 送信します"));
        // 昇順/降順
        JRadioButtonMenuItem ascending = createRadioButtonMenuItem("昇順", GUIConst.ACTION_ASCENDING);
        JRadioButtonMenuItem descending = createRadioButtonMenuItem("降順", GUIConst.ACTION_DESCENDING);
        karte.add(ascending);
        karte.add(descending);

        ButtonGroup bg = new ButtonGroup();
        bg.add(ascending);
        bg.add(descending);

        // 修正履歴表示
        karte.add(createRadioButtonMenuItem("修正履歴表示", GUIConst.ACTION_SHOW_MODIFIED));

        // 環境設定. Mac の場合は SettingForMac で設定済み.
        if (ClientContext.isWin()) {
            karte.add(createMenuItem("環境設定...", GUIConst.ACTION_SET_KARTE_ENVIROMENT));
        }

        // スタンプの簡易表示
        karte.add(createMenuItem("スタンプ簡易表示切替", GUIConst.ACTION_SIMPLIFY_STAMP, "T"));

        //
        // 挿入メニュー
        //
        JMenu insert = createMenu("挿 入", GUIConst.ACTION_INSERT_MENU);
        if (chart != null) {
            insert.addMenuListener(chart); // ChartMediator
        }
        insert.add(createMenuItem("傷病名", GUIConst.ACTION_INSERT_DISEASE));
        insert.add(createMenuItem("テキスト", GUIConst.ACTION_INSERT_TEXT));
        insert.add(createMenuItem("スタンプ", GUIConst.ACTION_INSERT_STAMP));
        insert.add(createMenuItem("シェーマ", GUIConst.ACTION_INSERT_SCHEMA));

        //
        // Text メニュー
        //
        JMenu text = createMenu("テキスト", GUIConst.ACTION_TEXT_MENU);
        if (chart != null) {
            text.addMenuListener(chart); // ChartMediator
        }

        // Size メニュー
        JMenu size = createMenu("サイズ", GUIConst.ACTION_SIZE);
        size.add(createMenuItem("大きく", GUIConst.ACTION_FONT_LARGER));
        size.add(createMenuItem("小さく", GUIConst.ACTION_FONT_SMALLER));
        size.add(createMenuItem("標準サイズ", GUIConst.ACTION_FONT_STANDARD));
        text.add(size);

        // Style メニュー
        JMenu style = createMenu("スタイル", GUIConst.ACTION_STYLE);
        style.add(createMenuItem("ボールド", GUIConst.ACTION_FONT_BOLD, "B"));
        style.add(createMenuItem("イタリック", GUIConst.ACTION_FONT_ITALIC, "I"));
        style.add(createMenuItem("アンダーライン", GUIConst.ACTION_FONT_UNDERLINE, "U"));
        text.add(style);

        // justify
        JMenu justify = createMenu("行揃え", GUIConst.ACTION_JUSTIFY);
        justify.add(createMenuItem("左揃え", GUIConst.ACTION_LEFT_JUSTIFY));
        justify.add(createMenuItem("中央揃え", GUIConst.ACTION_CENTER_JUSTIFY));
        justify.add(createMenuItem("右揃え", GUIConst.ACTION_RIGHT_JUSTIFY));
        text.add(justify);

        // Color
        JMenu color = createMenu("カラー", GUIConst.ACTION_COLOR);
        color.add(createMenuItem("赤", GUIConst.ACTION_FONT_RED));
        color.add(createMenuItem("橙", GUIConst.ACTION_FONT_ORANGE));
        color.add(createMenuItem("黄", GUIConst.ACTION_FONT_YELLOW));
        color.add(createMenuItem("緑", GUIConst.ACTION_FONT_GREEN));
        color.add(createMenuItem("青", GUIConst.ACTION_FONT_BLUE));
        color.add(createMenuItem("紫", GUIConst.ACTION_FONT_PURPLE));
        color.add(createMenuItem("灰色", GUIConst.ACTION_FONT_GRAY));
        color.add(createMenuItem("黒", GUIConst.ACTION_FONT_BLACK));
        text.add(color);

        //
        // Tool メニュー
        //
        JMenu tool = createMenu("ツール", GUIConst.ACTION_TOOL_MENU);
        tool.add(createMenuItem("スタンプ検索", GUIConst.ACTION_SEARCH_STAMP, "shift F"));
        tool.add(createMenuItem("受付リスト", GUIConst.ACTION_SHOW_WAITING_LIST, "1"));
        tool.add(createMenuItem("シェーマ箱", GUIConst.ACTION_SHOW_SCHEMABOX, "2"));
        tool.add(createMenuItem("患者検索", GUIConst.ACTION_SHOW_PATIENT_SEARCH, "3"));
        tool.add(createMenuItem("スタンプ箱", GUIConst.ACTION_SHOW_STAMPBOX, "4"));
        tool.addSeparator();
        tool.add(createMenuItem("プロフィール変更", GUIConst.ACTION_CHANGE_PASSWORD));
        tool.add(createMenuItem("院内ユーザー登録", GUIConst.ACTION_ADD_USER));

        //
        // Help
        //
        JMenu help = createMenu("ヘルプ", GUIConst.ACTION_HELP_MENU);

        if (ClientContext.isWin()) {
            help.add(createMenuItem("OpenDolphin について", GUIConst.ACTION_SHOW_ABOUT));
        }

        //
        // この時点で menuBar には既に window メニューが入っている (menuCount = 1 となっている)
        //
        menuBar.add(file, 0);
        menuBar.add(edit, 1);
        menuBar.add(karte, 2);
        menuBar.add(text, 3);
        menuBar.add(tool, 4);
        // window menu = 5
        menuBar.add(help, 6);

        //
        // ToolBars
        //
        if (chart != null) {
            JToolBar fileBar = new JToolBar();
            fileBar.setName("fileBar");
            fileBar.setFloatable(false);
            fileBar.setOpaque(false);
            fileBar.setBorderPainted(false);

            JToolBar editBar = new JToolBar();
            editBar.setName("editBar");
            editBar.setFloatable(false);
            editBar.setOpaque(false);
            editBar.setBorderPainted(false);

            fileBar.add(createButton(GUIConst.ACTION_SAVE, GUIConst.ICON_SAVE_32));
            fileBar.add(createButton(GUIConst.ACTION_PRINT, GUIConst.ICON_PRINT_32));

            //editBar.add(createButton(GUIConst.ACTION_NEW_KARTE, GUIConst.ICON_FILE_32));
            //editBar.add(createButton(GUIConst.ACTION_MODIFY_KARTE, GUIConst.ICON_FILE_EDIT_32));
            editBar.add(createButton(GUIConst.ACTION_UNDO, GUIConst.ICON_UNDO_32));
            editBar.add(createButton(GUIConst.ACTION_REDO, GUIConst.ICON_REDO_32));

            toolPanel = new JPanel();
            BoxLayout layout = new BoxLayout(toolPanel, BoxLayout.X_AXIS);
            toolPanel.setLayout(layout);
            toolPanel.add(editBar);
            toolPanel.add(fileBar);
        }
    }

    /**
     * JMenu を作る.
     *
     * @param menuName メニュー表示名
     * @param actionKey action key
     * @return JMenu
     */
    private JMenu createMenu(String menuName, String actionKey) {
        JMenu menu = new JMenu();
        menu.setName(actionKey);
        menu.setText(menuName);
        Action action = actionMap.get(actionKey);
        if (Objects.nonNull(action)) {
            action.putValue(Action.NAME, menuName);
            menu.setAction(action);
        }
        return menu;
    }

    /**
     * JMenuItem を作る.
     *
     * @param menuName メニュー表示名
     * @param actionKey action key
     * @return JMenuItem
     */
    private JMenuItem createMenuItem(String menuName, String actionKey) {
        return createMenuItem(menuName, actionKey, null, null, null);
    }

    /**
     * JMenuItem を作る.
     *
     * @param menuName メニュー表示名
     * @param actionKey action key
     * @param keyStroke ショートカットキー
     * @return JMenuItem
     */
    private JMenuItem createMenuItem(String menuName, String actionKey, String keyStroke) {
        return createMenuItem(menuName, actionKey, keyStroke, null, null);
    }

    /**
     * JMenuItem を作る.
     *
     * @param menuName メニュー表示名
     * @param actionKey action key
     * @param keyStroke ショートカットキー
     * @param icon アイコン
     * @param toolTipText ツールチップ
     * @return JMenuItem
     */
    private JMenuItem createMenuItem(String menuName, String actionKey, String keyStroke, ImageIcon icon, String toolTipText) {
        JMenuItem item = new JMenuItem();
        item.setName(actionKey);
        Action action = actionMap.get(actionKey);
        action.putValue(Action.NAME, menuName);
        action.putValue(Action.SMALL_ICON, icon);
        action.putValue(Action.SHORT_DESCRIPTION, toolTipText);
        action.putValue(GUIConst.KEY_MENU_ITEM, item); // action から JMenuItem を取得できるようにする
        item.setAction(action);
        if (Objects.nonNull(keyStroke)) {
            if (!keyStroke.contains("ctrl") || ClientContext.isMac()) {
                item.setAccelerator(KeyStroke.getKeyStroke(String.join(" ", SHORT_CUT_KEY_MASK, keyStroke)));
            }
       }
        return item;
    }

    /**
     * JRadioButtonMenuItem を作る.
     *
     * @param menuName メニュー表示名
     * @param actionKey action key
     * @return JRadioButtonMenuItem
     */
    private JRadioButtonMenuItem createRadioButtonMenuItem(String menuName, String actionKey) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem();
        item.setName(actionKey);

        Action action = actionMap.get(actionKey);
        action.putValue(Action.NAME, menuName);
        action.putValue(GUIConst.KEY_MENU_ITEM, item); // action から JRadioButtonMenuItem を取得できるようにする
        item.setAction(action);

        return item;
    }

    /**
     * JButton を作る.
     *
     * @param actionKey action key
     * @param icon Icon
     * @return JButton
     */
    private JButton createButton(String actionKey, ImageIcon icon) {
        JButton button = new JButton();
        button.setBorderPainted(false);
        button.setAction(actionMap.get(actionKey));
        button.setText(null);
        button.setIcon(icon);
        button.setOpaque(false);
        return button;
    }
}
