package open.dolphin.helper;

import open.dolphin.JsonConverter;
import open.dolphin.infomodel.SimpleDate;
import open.dolphin.setting.CalendarSettingPanel;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

/**
 * 休日かどうか調べる.
 *
 * @author pns
 */
public class Holiday {

    // yyyyMMdd 文字列をキー，休日名を value とする HashMap
    private static final HashMap<String, String> DATABASE = new HashMap<>();

    // 休診 DATABASE を作製する. 初めてこのクラスが call された時に読まれる.
    static {
        Preferences prefs = Preferences.userNodeForPackage(CalendarSettingPanel.class);
        String jsonData = prefs.get(CalendarSettingPanel.CALENDAR_DATA, "");

        if (StringUtils.isEmpty(jsonData)) {
            // CalendarSettingPanel の Google Calendar データが無いときは, ハードコーディングされたデータを読み込む
            Stream.of(HolidayDatabase.HOLIDAY_DATA).forEach(data -> DATABASE.put(data[0], data[1]));

        } else {
            // Google Calendar data を読み込む
            String[][] holidayData = JsonConverter.fromJson(jsonData, String[][].class);
            Stream.of(holidayData).forEach(data -> DATABASE.put(data[0], data[1]));
        }
    }


    /**
     * SimpleDate から yyyyMMdd 型式のキーを作る.
     *
     * @param sd SimpleDate
     * @return key
     */
    private static String createKey(SimpleDate sd) {
        return String.format("%d%02d%02d", sd.getYear(), sd.getMonth() + 1, sd.getDay());
    }

    /**
     * 指定された SimpleDate が休日かどうかを返す.
     *
     * @param sd SimpleDate
     * @return 休日の場合 true
     */
    public static boolean isHoliday(SimpleDate sd) {
        String key = createKey(sd);
        return DATABASE.containsKey(key);
    }

    /**
     * 指定された SimpleDate に休日名をセットする.
     *
     * @param sd SimpleDate
     */
    public static void setTo(SimpleDate sd) {
        String key = createKey(sd);
        sd.setEventCode(DATABASE.get(key));
    }
}
