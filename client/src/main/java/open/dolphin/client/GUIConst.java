package open.dolphin.client;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GUIConst {

    //
    // client package で使用する定数
    //
    // メニュー関連
    public static final String KEY_MENU_ITEM = "menuItem"; // action に JMenuItem を埋め込むキー

    public static final String ACTION_FILE_MENU = "fileMenu";
    public static final String ACTION_NEW_KARTE = "newKarte";
    public static final String ACTION_NEW_DOCUMENT = "newDocument";
    public static final String ACTION_OPEN_KARTE = "openKarte";
    public static final String ACTION_CLOSE = "close";
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_PRINTER_SETUP = "printerSetup";
    public static final String ACTION_PRINT = "print";
    public static final String ACTION_PROCESS_EXIT = "processExit";

    public static final String ACTION_EDIT_MENU = "editMenu";
    public static final String ACTION_MODIFY_KARTE = "modifyKarte";
    public static final String ACTION_UNDO = "undo";
    public static final String ACTION_REDO = "redo";
    public static final String ACTION_CUT = "cut";
    public static final String ACTION_COPY = "copy";
    public static final String ACTION_PASTE = "paste";

    public static final String ACTION_FIND_FIRST = "findFirst";
    public static final String ACTION_FIND_NEXT = "findNext";
    public static final String ACTION_FIND_PREVIOUS = "findPrevious";
    public static final String ACTION_SELECT_ALL = "selectAll";

    public static final String ACTION_KARTE_MENU = "karteMenu";
    public static final String ACTION_SEND_CLAIM = "sendClaim";
    public static final String ACTION_ASCENDING = "ascending";
    public static final String ACTION_DESCENDING = "descending";
    public static final String ACTION_SHOW_MODIFIED = "showModified";
    public static final String ACTION_SET_KARTE_ENVIROMENT = "setKarteEnviroment";

    public static final String ACTION_INSERT_MENU = "insertMenu";
    public static final String ACTION_INSERT_DISEASE = "insertDisease";
    public static final String ACTION_INSERT_TEXT = "insertText";
    public static final String ACTION_INSERT_SCHEMA = "insertSchema";
    public static final String ACTION_INSERT_STAMP = "insertStamp";
    public static final String ACTION_SELECT_INSURANCE = "selectInsurance";

    public static final String ACTION_TEXT_MENU = "textMenu";
    public static final String ACTION_SIZE = "size";
    public static final String ACTION_FONT_LARGER = "fontLarger";
    public static final String ACTION_FONT_SMALLER = "fontSmaller";
    public static final String ACTION_FONT_STANDARD = "fontStandard";
    public static final String ACTION_STYLE = "style";
    public static final String ACTION_FONT_BOLD = "fontBold";
    public static final String ACTION_FONT_ITALIC = "fontItalic";
    public static final String ACTION_FONT_UNDERLINE = "fontUnderline";
    public static final String ACTION_JUSTIFY = "justify";
    public static final String ACTION_LEFT_JUSTIFY = "leftJustify";
    public static final String ACTION_CENTER_JUSTIFY = "centerJustify";
    public static final String ACTION_RIGHT_JUSTIFY = "rightJustify";
    public static final String ACTION_COLOR = "color";
    public static final String ACTION_FONT_RED = "fontRed";
    public static final String ACTION_FONT_ORANGE = "fontOrange";
    public static final String ACTION_FONT_YELLOW = "fontYellow";
    public static final String ACTION_FONT_GREEN = "fontGreen";
    public static final String ACTION_FONT_BLUE = "fontBlue";
    public static final String ACTION_FONT_PURPLE = "fontPurple";
    public static final String ACTION_FONT_GRAY = "fontGray";
    public static final String ACTION_FONT_BLACK = "fontBlack";
    public static final String ACTION_RESET_STYLE = "resetStyle";

    public static final String ACTION_TOOL_MENU = "toolMenu";
    public static final String ACTION_SEARCH_STAMP = "searchStamp";
    public static final String ACTION_SHOW_STAMPBOX = "showStampBox";
    public static final String ACTION_SHOW_SCHEMABOX = "showSchemaBox";
    public static final String ACTION_SHOW_WAITING_LIST = "showWaitingList";
    public static final String ACTION_SHOW_PATIENT_SEARCH = "showPatientSearch";
    public static final String ACTION_CHANGE_PASSWORD = "changePassword";
    public static final String ACTION_ADD_USER = "addUser";

    public static final String ACTION_HELP_MENU = "helpMenu";
    public static final String ACTION_SHOW_ABOUT = "showAbout";

    // Role
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    //
    // order package で使用する定数
    //
    public static final int DEFAULT_CMP_V_SPACE = 11;

    public static final int DEFAULT_STAMP_EDITOR_WIDTH = 700;
    public static final int DEFAULT_STAMP_EDITOR_HEIGHT = 690;
    public static final Dimension DEFAULT_STAMP_EDITOR_SIZE = new Dimension(DEFAULT_STAMP_EDITOR_WIDTH, DEFAULT_STAMP_EDITOR_HEIGHT);

    public static final int DEFAULT_EDITOR_WIDTH = 680;  //724
    public static final int DEFAULT_EDITOR_HEIGHT = 256;  //230

    // ウインドウメニューのメニューバー内の位置
    public static final int DEFAULT_WINDOWMENU_POSITION = 5;

    // JList，JTable のデフォルト row height
    public static final int DEFAULT_LIST_ROW_HEIGHT = ClientContext.isWin() ? 16 : 18;
    public static final int DEFAULT_TABLE_ROW_HEIGHT = ClientContext.isWin() ? 16 : 18;

    // レンダラのインデント用のボーダー
    public static final Border RENDERER_BORDER_NARROW = BorderFactory.createEmptyBorder(0, 6, 0, 6);
    public static final Border RENDERER_BORDER_WIDE = BorderFactory.createEmptyBorder(0, 12, 0, 12);

    // パネルの default background
    public static final Color PANEL_BACKGROUND = new Color(246, 246, 246);

    //
    // アイコンはここで集中管理
    //
    public static final ImageIcon ICON_DOLPHIN = ClientContext.getImageIcon("icons/dolphin32.png");

    // 空のアイコン
    public static final ImageIcon ICON_EMPTY_16 = ClientContext.getImageIcon("icons/16x16/empty.png");
    public static final ImageIcon ICON_EMPTY_22 = ClientContext.getImageIcon("icons/22x22/empty.png");
    // カレンダなどで使う矢印
    public static final ImageIcon ICON_CHECK_LIGHTBLUE_16 = ClientContext.getImageIcon("icons/16x16/check-lightblue.png");
    public static final ImageIcon ICON_CHECK_RED_16 = ClientContext.getImageIcon("icons/16x16/check-red.png");
    public static final ImageIcon ICON_CHECK_GRAY_16 = ClientContext.getImageIcon("icons/16x16/check-gray.png");
    //public static final ImageIcon ICON_ARROW_DOWN_16 = ClientContext.getImageIcon("icons/16x16/arrow-down.gif");
    //public static final ImageIcon ICON_ARROW_RIGHT_16 = ClientContext.getImageIcon("icons/16x16/arrow-right.gif");
    //public static final ImageIcon ICON_ARROW_LEFT_16 = ClientContext.getImageIcon("icons/16x16/arrow-left.gif");
    //public static final ImageIcon ICON_ARROW_UP_16 = ClientContext.getImageIcon("icons/16x16/arrow-up.gif");
    // Tree の expand, collapse アイコン
    public static final ImageIcon ICON_TREE_EXPANDED_16 = ClientContext.getImageIcon("icons/16x16/Tree.expandedIcon.png");
    public static final ImageIcon ICON_TREE_COLLAPSED_16 = ClientContext.getImageIcon("icons/16x16/Tree.collapsedIcon.png");

    // OptionPane icons
    public static final ImageIcon ICON_WARNING_32 = ClientContext.getImageIcon("icons/48x48/OptionPane.warningIcon.png");
    public static final ImageIcon ICON_INFORMATION_32 = ClientContext.getImageIcon("icons/48x48/OptionPane.questionIcon.png");
    public static final ImageIcon ICON_QUESTION_32 = ClientContext.getImageIcon("icons/48x48/OptionPane.questionIcon.png");
    public static final ImageIcon ICON_ERROR_32 = ClientContext.getImageIcon("icons/48x48/OptionPane.errorIcon.png");
    // 健康保険証
    public static final ImageIcon ICON_INSURANCE_CARD_22 = ClientContext.getImageIcon("icons/22x22/insurance-card.png");
    public static final ImageIcon ICON_INSURANCE_CARD_32 = ClientContext.getImageIcon("icons/32x32/insurance-card.png");
    // 一般文書
    public static final ImageIcon ICON_DOCUMENT_GENERIC_32 = ClientContext.getImageIcon("icons/32x32/generic-document.png");
    // カルテ
    public static final ImageIcon ICON_KARTE_NEW_22 = ClientContext.getImageIcon("icons/22x22/karte-new.png");
    public static final ImageIcon ICON_KARTE_NEW_32 = ClientContext.getImageIcon("icons/32x32/karte-new.png");
    public static final ImageIcon ICON_KARTE_EDIT_22 = ClientContext.getImageIcon("icons/22x22/karte-edit.png");
    public static final ImageIcon ICON_KARTE_EDIT_32 = ClientContext.getImageIcon("icons/32x32/karte-edit.png");
    // スタンプ箱
    public static final ImageIcon ICON_STAMP_EXPORT_22 = ClientContext.getImageIcon("icons/22x22/database_up.png");
    public static final ImageIcon ICON_STAMP_IMPORT_22 = ClientContext.getImageIcon("icons/22x22/database_down.png");

    // カレンダー
    public static final ImageIcon ICON_CALENDAR_32 = ClientContext.getImageIcon("icons/32x32/google-calendar.png");

    // black & white icons
    public static final ImageIcon ICON_SEARCH_16 = ClientContext.getImageIcon("bw/16x16/search.png");
    public static final ImageIcon ICON_PRINT_16 = ClientContext.getImageIcon("bw/16x16/print.png");
    public static final ImageIcon ICON_PRINT_32 = ClientContext.getImageIcon("bw/32x32/print.png");
    public static final ImageIcon ICON_SAVE_16 = ClientContext.getImageIcon("bw/16x16/save.png");
    public static final ImageIcon ICON_SAVE_32 = ClientContext.getImageIcon("bw/32x32/save.png");
    public static final ImageIcon ICON_DELETE_16 = ClientContext.getImageIcon("bw/16x16/delete.png");
    public static final ImageIcon ICON_FILE_16 = ClientContext.getImageIcon("bw/16x16/file-add.png");
    public static final ImageIcon ICON_FILE_32 = ClientContext.getImageIcon("bw/32x32/file-add.png");
    //public static final ImageIcon ICON_FILE_32 = ClientContext.getImageIcon("bw/32x32/note32.png");
    public static final ImageIcon ICON_FILE_EDIT_16 = ClientContext.getImageIcon("bw/16x16/file-edit.png");
    public static final ImageIcon ICON_FILE_EDIT_32 = ClientContext.getImageIcon("bw/32x32/file-edit.png");
    //public static final ImageIcon ICON_FILE_EDIT_32 = ClientContext.getImageIcon("bw/32x32/notepencil32.png");
    public static final ImageIcon ICON_UNDO_16 = ClientContext.getImageIcon("bw/16x16/arrow-1-backward.png");
    public static final ImageIcon ICON_UNDO_32 = ClientContext.getImageIcon("bw/32x32/arrow-1-backward.png");
    public static final ImageIcon ICON_REDO_16 = ClientContext.getImageIcon("bw/16x16/arrow-1-forward.png");
    public static final ImageIcon ICON_REDO_32 = ClientContext.getImageIcon("bw/32x32/arrow-1-forward.png");
    public static final ImageIcon ICON_WINDOW_STACK_16 = ClientContext.getImageIcon("bw/16x16/window-stack.png");
    public static final ImageIcon ICON_WINDOW_STACK_32 = ClientContext.getImageIcon("bw/32x32/window-stack.png");
    public static final ImageIcon ICON_STAMP_32 = ClientContext.getImageIcon("bw/32x32/stamp.png");
    public static final ImageIcon ICON_BRUSH_32 = ClientContext.getImageIcon("bw/32x32/brush.png");
    public static final ImageIcon ICON_CUT_16 = ClientContext.getImageIcon("bw/16x16/cut.png");
    public static final ImageIcon ICON_COPY_16 = ClientContext.getImageIcon("bw/16x16/copy.png");
    public static final ImageIcon ICON_PASTE_16 = ClientContext.getImageIcon("bw/16x16/paste.png");
    public static final ImageIcon ICON_OPEN_16 = ClientContext.getImageIcon("bw/16x16/open-in-new-window.png");
    public static final ImageIcon ICON_EDIT_ALT1_16 = ClientContext.getImageIcon("bw/16x16/edit-alt-1.png");
    public static final ImageIcon ICON_EDIT_ALT2_16 = ClientContext.getImageIcon("bw/16x16/edit-alt-2.png");
    public static final ImageIcon ICON_PADLOCK_CLOSED_16 = ClientContext.getImageIcon("bw/16x16/padlock-closed.png");
    public static final ImageIcon ICON_PADLOCK_OPEN_16 = ClientContext.getImageIcon("bw/16x16/padlock-open.png");
    public static final ImageIcon ICON_GEAR_16 = ClientContext.getImageIcon("bw/16x16/settings.png");
    public static final ImageIcon ICON_ARROW_CIRCULAR_ALT1_16 = ClientContext.getImageIcon("bw/16x16/arrow-circular-alt-1.png");
    public static final ImageIcon ICON_INFORMATION_16 = ClientContext.getImageIcon("bw/16x16/about.png");
    public static final ImageIcon ICON_REMOVE_16 = ClientContext.getImageIcon("bw/16x16/cancel.png");
    public static final ImageIcon ICON_CROSS_16 = ClientContext.getImageIcon("bw/16x16/cross.png");
    public static final ImageIcon ICON_ARROW1_LEFT_16 = ClientContext.getImageIcon("bw/16x16/arrow-1-left.png");
    public static final ImageIcon ICON_ARROW1_RIGHT_16 = ClientContext.getImageIcon("bw/16x16/arrow-1-right.png");
    public static final ImageIcon ICON_ERASER_16 = ClientContext.getImageIcon("bw/16x16/eraser.png");
    public static final ImageIcon ICON_LIST_ADD_16 = ClientContext.getImageIcon("bw/16x16/add.png");
    public static final ImageIcon ICON_LIST_REMOVE_16 = ClientContext.getImageIcon("bw/16x16/trash-empty.png");
    public static final ImageIcon ICON_FOLDER_NEW_16 = ClientContext.getImageIcon("bw/16x16/folder.png");
    public static final ImageIcon ICON_DOWNLOAD_16 = ClientContext.getImageIcon("bw/16x16/download-alt-3.png");

    public static final ImageIcon ICON_MD_FORWARD_16 = ClientContext.getImageIcon("bw/16x16/MD-play.png");
    public static final ImageIcon ICON_MD_BACKWARD_16 = ClientContext.getImageIcon("bw/16x16/MD-backward.png");
    public static final ImageIcon ICON_MD_FAST_FORWARD_16 = ClientContext.getImageIcon("bw/16x16/MD-fast-forward.png");
    public static final ImageIcon ICON_MD_FAST_BACKWARD_16 = ClientContext.getImageIcon("bw/16x16/MD-fast-backward.png");
    public static final ImageIcon ICON_MD_STOP_16 = ClientContext.getImageIcon("bw/16x16/MD-stop.png");
    public static final ImageIcon ICON_MD_EJECT_16 = ClientContext.getImageIcon("bw/16x16/MD-eject.png");


    //  fugue series ----------------------
    // 受付リストのアイコン
    public static final ImageIcon ICON_BOOK_OPEN_BOOKMARK_16 = ClientContext.getImageIcon("fugue/16x16/book-open-bookmark.png");
    public static final ImageIcon ICON_BOOK_OPEN_16 = ClientContext.getImageIcon("fugue/16x16/book-open.png");
    public static final ImageIcon ICON_USER_WHITE_16 = ClientContext.getImageIcon("fugue/16x16/user-white.png");
    public static final ImageIcon ICON_USER_RED_16 = ClientContext.getImageIcon("fugue/16x16/user-red.png");
    public static final ImageIcon ICON_USER_BLUE_16 = ClientContext.getImageIcon("fugue/16x16/user-blue.png");
    // 受付リスト，シェーマボックスの refresh アイコン
    //public static final ImageIcon ICON_ARROW_CIRCLE_DOUBLE_16 = ClientContext.getImageIcon("fugue/16x16/arrow-circle-double-135.png");
    // スタンプ箱のカギ
    //public static final ImageIcon ICON_LOCK_16 = ClientContext.getImageIcon("fugue/16x16/lock.png");
    //public static final ImageIcon ICON_LOCK_OPEN_16 = ClientContext.getImageIcon("fugue/16x16/lock-unlock.png");
    // スタンプアイコン
    //public static final ImageIcon ICON_STAMP_EDIT_22 = ClientContext.getImageIcon("fugue/22x22/stamp-edit.png");
    //public static final ImageIcon ICON_STAMP_16 = ClientContext.getImageIcon("fugue/16x16/stamp.png");
    //public static final ImageIcon ICON_STAMP_EDIT_16 = ClientContext.getImageIcon("fugue/16x16/stamp-edit.png");
    //public static final ImageIcon ICON_STAMP_EXPORT_16 = ClientContext.getImageIcon("fugue/16x16/stamp-export.png");
    //public static final ImageIcon ICON_STAMP_IMPORT_16 = ClientContext.getImageIcon("fugue/16x16/stamp-import.png");
    //public static final ImageIcon ICON_STAMP_EXPORT_22 = ClientContext.getImageIcon("fugue/22x22/stamp-export.png");
    //public static final ImageIcon ICON_STAMP_IMPORT_22 = ClientContext.getImageIcon("fugue/22x22/stamp-import.png");
    //public static final ImageIcon ICON_STAMP_22 = ClientContext.getImageIcon("fugue/22x22/stamp.png");
    //public static final ImageIcon ICON_STAMP_32 = ClientContext.getImageIcon("fugue/32x32/stamp.png");
    public static final ImageIcon ICON_STAMP_TEXT_22 = ClientContext.getImageIcon("fugue/22x22/stamp-text.png");
    //public static final ImageIcon ICON_STAMP_TEXT_32 = ClientContext.getImageIcon("fugue/32x32/stamp-text.png");
    // StampTree node アイコン
    //public static final ImageIcon ICON_DOCUMENT_CONVERT_16 = ClientContext.getImageIcon("fugue/16x16/document-convert.png");
    public static final ImageIcon ICON_DOCUMENT_CONVERT_16 = ClientContext.getImageIcon("fugue/16x16/document.png");
    public static final ImageIcon ICON_DOCUMENT_ATTRIBUTE_16 = ClientContext.getImageIcon("fugue/16x16/document-attribute.png");
    // スタンプエディタ
    //public static final ImageIcon ICON_DOCUMENT_IMPORT_16 = ClientContext.getImageIcon("fugue/16x16/document-import.png");
    // スタンプ箱のギア
    //public static final ImageIcon ICON_GEAR_16 = ClientContext.getImageIcon("fugue/16x16/gear.png");
    // ギアメニューに入っているディスクアイコン
    //public static final ImageIcon ICON_DISK_16 = ClientContext.getImageIcon("fugue/16x16/disk-black.png");
    // 鉛筆
    //public static final ImageIcon ICON_PENCIL_16 = ClientContext.getImageIcon("fugue/16x16/pencil.png");
    //public static final ImageIcon ICON_PENCIL_SELECTED_16 = ClientContext.getImageIcon("fugue/16x16/pencil-color.png");
    //public static final ImageIcon ICON_PENCIL_PRESSED_16 = ClientContext.getImageIcon("fugue/16x16/pencil-pressed.png");
    // info, question
    //public static final ImageIcon ICON_INFORMATION_16 = ClientContext.getImageIcon("fugue/16x16/information.png");
    public static final ImageIcon ICON_QUESTION_16 = ClientContext.getImageIcon("fugue/16x16/question.png");

    // イメージブラウザで使う pdf アイコン
    //public static final ImageIcon ICON_PDF_32 = ClientContext.getImageIcon("fugue/32x32/pdf.png");
    // StampImporter
    public static final ImageIcon ICON_HOME_16 = ClientContext.getImageIcon("fugue/16x16/home.png");
    public static final ImageIcon ICON_FLAG_16 = ClientContext.getImageIcon("fugue/16x16/flag.png");
    // chartToolBar
    public static final ImageIcon ICON_STATUS_BUSY_16 = ClientContext.getImageIcon("fugue/16x16/status-busy.png");
    public static final ImageIcon ICON_STATUS_OFFLINE_16 = ClientContext.getImageIcon("fugue/16x16/status-offline.png");
    // 削除アイコン
    //public static final ImageIcon ICON_REMOVE_16 = ClientContext.getImageIcon("fugue/16x16/cross.png");
    public static final ImageIcon ICON_REMOVE_22 = ClientContext.getImageIcon("icons/22x22/gtk_cancel.png");
    // 仮保存アイコン
    public static final ImageIcon ICON_ONEDIT_16 = ClientContext.getImageIcon("fugue/16x16/edit-signiture.png");
    // スタンプ箱からスタンプをカルテに送る
    //public static final ImageIcon ICON_ARROW1_LEFT_16 = ClientContext.getImageIcon("fugue/16x16/arrow-135.png");

    //  tango series ---------------------
    //public static final ImageIcon ICON_SYSTEM_SEARCH_16 = ClientContext.getImageIcon("tango/16x16/actions/system-search.png");
    //public static final ImageIcon ICON_SYSTEM_SEARCH_22 = ClientContext.getImageIcon("tango/22x22/actions/system-search.png");
    public static final ImageIcon ICON_DOCUMENT_SAVE_22 = ClientContext.getImageIcon("tango/22x22/actions/document-save.png");
    //public static final ImageIcon ICON_GRAPHICS_BRUSH_22 = ClientContext.getImageIcon("tango/22x22/categories/applications-graphics.png");
    //public static final ImageIcon ICON_GRAPHICS_BRUSH_32 = ClientContext.getImageIcon("tango/32x32/categories/applications-graphics.png");
    //public static final ImageIcon ICON_DOCUMENT_NEW_22 = ClientContext.getImageIcon("tango/22x22/actions/document-new.png");
    //public static final ImageIcon ICON_DOCUMENT_NEW_32 = ClientContext.getImageIcon("tango/32x32/actions/document-new.png");
    //public static final ImageIcon ICON_FLOPPY_16 = ClientContext.getImageIcon("tango/16x16/devices/media-floppy.png");
    //public static final ImageIcon ICON_FLOPPY_22 = ClientContext.getImageIcon("tango/22x22/devices/media-floppy.png");
    public static final ImageIcon ICON_FLOPPY_32 = ClientContext.getImageIcon("tango/32x32/devices/media-floppy.png");
    public static final ImageIcon ICON_DOCUMENT_OPEN_22 = ClientContext.getImageIcon("tango/22x22/actions/document-open.png");
    //public static final ImageIcon ICON_DOCUMENT_PRINT_22 = ClientContext.getImageIcon("tango/22x22/actions/document-print.png");
    //public static final ImageIcon ICON_DOCUMENT_PRINT_32 = ClientContext.getImageIcon("tango/32x32/actions/document-print.png");
    //public static final ImageIcon ICON_WINDOWS_22 = ClientContext.getImageIcon("tango/22x22/apps/preferences-system-windows.png");

    //public static final ImageIcon ICON_EDIT_DELETE_22 = ClientContext.getImageIcon("tango/22x22/actions/edit-delete.png");
    //public static final ImageIcon ICON_EDIT_CLEAR_16 = ClientContext.getImageIcon("tango/16x16/actions/edit-clear.png");
    //public static final ImageIcon ICON_EDIT_CUT_22 = ClientContext.getImageIcon("tango/22x22/actions/edit-cut.png");
    //public static final ImageIcon ICON_EDIT_CUT_32 = ClientContext.getImageIcon("tango/32x32/actions/edit-cut.png");
    //public static final ImageIcon ICON_EDIT_PASTE_22 = ClientContext.getImageIcon("tango/22x22/actions/edit-paste.png");
    //public static final ImageIcon ICON_EDIT_PASTE_32 = ClientContext.getImageIcon("tango/32x32/actions/edit-paste.png");
    //public static final ImageIcon ICON_EDIT_COPY_22 = ClientContext.getImageIcon("tango/22x22/actions/edit-copy.png");
    //public static final ImageIcon ICON_EDIT_COPY_32 = ClientContext.getImageIcon("tango/32x32/actions/edit-copy.png");
    //public static final ImageIcon ICON_EDIT_UNDO_22 = ClientContext.getImageIcon("tango/22x22/actions/edit-undo.png");
    //public static final ImageIcon ICON_EDIT_UNDO_32 = ClientContext.getImageIcon("tango/32x32/actions/edit-undo.png");
    //public static final ImageIcon ICON_EDIT_REDO_22 = ClientContext.getImageIcon("tango/22x22/actions/edit-redo.png");
    //public static final ImageIcon ICON_EDIT_REDO_32 = ClientContext.getImageIcon("tango/32x32/actions/edit-redo.png");
    //public static final ImageIcon ICON_EDIT_FIND_22 = ClientContext.getImageIcon("tango/22x22/actions/edit-find.png");
    //public static final ImageIcon ICON_EDIT_FIND_32 = ClientContext.getImageIcon("tango/32x32/actions/edit-find.png");
    //public static final ImageIcon ICON_EDIT_FIND_NEXT_32 = ClientContext.getImageIcon("tango/32x32/actions/edit-find-next.png");
    //public static final ImageIcon ICON_EDIT_FIND_PREVIOUS_32 = ClientContext.getImageIcon("tango/32x32/actions/edit-find-previous.png");
    //public static final ImageIcon ICON_EDIT_SELECT_ALL_22 = ClientContext.getImageIcon("tango/22x22/actions/edit-select-all.png");
    //public static final ImageIcon ICON_GO_NEXT_16 = ClientContext.getImageIcon("tango/16x16/actions/go-next.png");
    //public static final ImageIcon ICON_GO_PREVIOUS_16 = ClientContext.getImageIcon("tango/16x16/actions/go-previous.png");
    //public static final ImageIcon ICON_LIST_ADD_16 = ClientContext.getImageIcon("tango/16x16/actions/list-add.png");
    //public static final ImageIcon ICON_LIST_REMOVE_16 = ClientContext.getImageIcon("tango/16x16/actions/list-remove.png");
    public static final ImageIcon ICON_FOLDER_16 = ClientContext.getImageIcon("icons/16x16/Folder-icon.png");
    //public static final ImageIcon ICON_FOLDER_REMOTE_16 = ClientContext.getImageIcon("tango/16x16/places/folder-remote.png");
    //public static final ImageIcon ICON_FOLDER_NEW_16 = ClientContext.getImageIcon("tango/16x16/actions/folder-new.png");
    // 地球儀
    public static final ImageIcon ICON_EARTH_16 = ClientContext.getImageIcon("tango/16x16/apps/internet-web-browser.png");
    //public static final ImageIcon ICON_EARTH_32 = ClientContext.getImageIcon("tango/32x32/apps/internet-web-browser.png");
    // コンピューター
    public static final ImageIcon ICON_COMPUTER_32 = ClientContext.getImageIcon("tango/32x32/devices/computer.png");
    // ショートカット
    public static final ImageIcon ICON_SHORTCUTS_32 = ClientContext.getImageIcon("tango/32x32/apps/preferences-desktop-keyboard-shortcuts.png");
    // ネットワーク
    public static final ImageIcon ICON_NETWORK_16 = ClientContext.getImageIcon("tango/16x16/places/network-workgroup.png");
    public static final ImageIcon ICON_NETWORK_32 = ClientContext.getImageIcon("tango/32x32/places/network-workgroup.png");
    // CD-ROM ドライブ
    public static final ImageIcon ICON_OPTICAL_DRIVE_32 = ClientContext.getImageIcon("tango/32x32/devices/drive-optical.png");
    // emblems
    public static final ImageIcon ICON_EMBLEM_IMPORTANT_32 = ClientContext.getImageIcon("tango/32x32/emblem/emblem-important.png");
    public static final ImageIcon ICON_EMBLEM_SYSTEM_32 = ClientContext.getImageIcon("tango/32x32/emblem/emblem-system.png");

    // スプラッシュ
    public static final ImageIcon ICON_SPLASH_DOLPHIN = ClientContext.getImageIcon("splash/splash.jpg");
    public static final ImageIcon ICON_SPLASH_USAGI = ClientContext.getImageIcon("splash/splash-usagi.jpg");

    // ボーダー
    public static final ImageIcon ICON_BORDER_TITLE_12 = ClientContext.getImageIcon("borders/12/Frame.titlePane.mini.png");
    public static final ImageIcon ICON_BORDER_TITLE_16 = ClientContext.getImageIcon("borders/16/Frame.titlePane.small.png");
    public static final ImageIcon ICON_BORDER_TITLE_22 = ClientContext.getImageIcon("borders/22/Frame.titlePane.png");
    public static final ImageIcon ICON_BORDER_TITLE_38 = ClientContext.getImageIcon("borders/38/Frame.titlePane.png");
    public static final ImageIcon ICON_BORDER_TITLE_LIGHT_BLUE_38 = ClientContext.getImageIcon("borders/38/Frame.titlePane.lightBlue.png");
    public static final ImageIcon ICON_BORDER_TITLE_PINK_38 = ClientContext.getImageIcon("borders/38/Frame.titlePane.Pink.png");
    public static final ImageIcon ICON_BORDER_GROUPBOX_18 = ClientContext.getImageIcon("borders/18/GroupBox.png");
    public static final ImageIcon ICON_BORDER_GROUPBOX_EMPTY_18 = ClientContext.getImageIcon("borders/18/GroupBox.empty.png");

    // progress bar for ATOK memory monitor
    //public static final ImageIcon PROGRSS_BAR_1 = ClientContext.getImageIcon("icons/64x8/progressBar1.png");
    //public static final ImageIcon PROGRSS_BAR_2 = ClientContext.getImageIcon("icons/64x8/progressBar2.png");
    //public static final ImageIcon PROGRSS_BAR_3 = ClientContext.getImageIcon("icons/64x8/progressBar3.png");
    //public static final ImageIcon PROGRSS_BAR_4 = ClientContext.getImageIcon("icons/64x8/progressBar4.png");

    // RegionView の背景
    public static final ImageIcon ICON_BODY = ClientContext.getImageIcon("body.png");
    public static final BufferedImage IMAGE_BODY;

    static {
        IMAGE_BODY = new BufferedImage(ICON_BODY.getIconWidth(), ICON_BODY.getIconHeight(), BufferedImage.TYPE_INT_BGR);
        Graphics2D g = IMAGE_BODY.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(GUIConst.ICON_BODY.getImage(), 0, 0, null);
        g.dispose();
    }
}
