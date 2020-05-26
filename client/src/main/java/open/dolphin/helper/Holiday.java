package open.dolphin.helper;

import open.dolphin.delegater.PnsDelegater;
import open.dolphin.infomodel.SimpleDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.stream.Stream;

/**
 * 休日かどうか調べる.
 *
 * @author pns
 */
public class Holiday {
    private static final Logger logger = LoggerFactory.getLogger(Holiday.class);

    // yyyyMMdd 文字列をキー，休日名を value とする HashMap
    private static final HashMap<String, String> DATABASE = new HashMap<>();

    /**
     * サーバからデータを取得してカレンダーデータベースを作成する.
     */
    public static void setupCalendarData() {
        DBTask<Void> setup = new DBTask<Void>() {
            @Override
            protected Void doInBackground() {
                PnsDelegater dlg = new PnsDelegater();
                String[][] holidayData = dlg.getCalendarData();
                if (holidayData == null) {
                    // サーバにデータが無いときは, ハードコーディングされたデータを読み込む
                    holidayData = HolidayDatabase.HOLIDAY_DATA;
                    logger.info("No holiday data in server, use local instead.");
                }
                Stream.of(holidayData).forEach(data -> DATABASE.put(data[0], data[1]));
                logger.info("Holiday database created.");
                return null;
            }
        };
        setup.execute();
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
