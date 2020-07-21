package open.dolphin.inspector;

import open.dolphin.client.ClientContext;
import open.dolphin.event.BadgeListener;
import open.dolphin.infomodel.IInfoModel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Inspector が持つインターフェース.
 * Calendar の cell 幅が一定になるために DEFAULT_WIDTH は
 * 261 から 14ドット単位で調節する必要がある.
 *
 * @author pns
 */
public interface IInspector {

    public static final boolean IS_WIN = ClientContext.isWin();
    public static final int DEFAULT_WIDTH = IS_WIN ? 261 - 14 * 2 : 261;
    public static final int DEFAULT_HEIGHT = IS_WIN ? 150 : 175;
    public static final Color BACKGROUND = new Color(240, 240, 240);
    public static final Color BORDER_COLOR = Color.LIGHT_GRAY;

    public static final KeyStroke META_W = KeyStroke.getKeyStroke("meta W");
    public static final KeyStroke META_Z = KeyStroke.getKeyStroke("meta Z");
    public static final KeyStroke SHIFT_META_Z = KeyStroke.getKeyStroke("shift meta Z");
    public static final KeyStroke BACK_SPACE = KeyStroke.getKeyStroke("BACK_SPACE");

    /**
     * Inspector を区別する ID としての名前.
     *
     * @return
     */
    public String getName();

    /**
     * GUI で使うタイトル. Name と違うものがある.
     *
     * @return
     */
    public String getTitle();

    /**
     * Inspector をのせた JPanel.
     *
     * @return
     */
    public JPanel getPanel();

    /**
     * Border が必要な場合はここから引き出す.
     *
     * @return
     */
    default public Border getBorder() {
        return new InspectorBorder(getTitle());
    }

    /**
     * 内容を更新する.
     */
    public void update();

    /**
     * Tab のバッジを表示したい時に使う.
     *
     * @param listener BadgeListener
     * @param index    Badge を付ける tab のインデックス番号
     */
    default public void addBadgeListener(BadgeListener listener, int index) { }

    /**
     * Windows かどうか.
     *
     * @return true if win
     */
    default public boolean isWin() {
        return IS_WIN;
    }

    /**
     * 今日の日付を ISO 型式で返す.
     *
     * @return ISO 型式の日付
     */
    default public String today() {
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.DATE_WITHOUT_TIME);
        return sdf.format(today);
    }
}
