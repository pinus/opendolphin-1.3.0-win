package open.dolphin.setting;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;

import open.dolphin.JsonConverter;
import open.dolphin.client.GUIConst;
import open.dolphin.helper.GridBagBuilder;
import open.dolphin.helper.Task;
import open.dolphin.ui.CompletableJTextField;
import open.dolphin.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;
import java.awt.*;

/**
 * Calendar Setting Panel.
 *
 * @author pns
 */
public class CalendarSettingPanel extends AbstractSettingPanel {
    private static final String ID = "calendarSetting";
    private static final String TITLE = "カレンダー";
    private static final ImageIcon ICON = GUIConst.ICON_CALENDAR_32;
    private static final int TEXT_FIELD_WIDTH = 40;
    private static final Dimension TEXT_FIELD_SIZE = new Dimension(300, 28);
    private static long DAY_LENGTH = 86400000L;

    // Google Calendar
    private static final String APPLICATION_NAME = "open.dolphin";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String TOKENS_DIRECTORY_PATH = TMP_DIR + "com.google.calendar.tokens";

    // Keys for preferences
    private static String CALENDAR_ID = "calendarId";
    private static String HOLIDAY_CALENDAR_ID = "holidayCalendarId";
    private static String CREDENTIAL = "calendarCredential";
    public static String CALENDAR_DATA = "calendarData";

    // GUI
    private CompletableJTextField holidayCalendarIdField;
    private CompletableJTextField calendarIdField;
    private JTextField credentialField;

    private String calendarJsonData = "";
    private final Preferences prefs = Preferences.userNodeForPackage(CalendarSettingPanel.class);
    private final Logger logger = LoggerFactory.getLogger(CalendarSettingPanel.class);

    public CalendarSettingPanel() { init(); }

    private void init() {
        setId(ID);
        setTitle(TITLE);
        setIcon(ICON);
    }

    @Override
    public void start() {
        initComponents();
        bindModelToView();
    }

    private void initComponents() {
        // init panel
        GridBagBuilder gbb = new GridBagBuilder("Google Calendar 設定");
        int row = 0;

        JLabel calendarLabel = new JLabel("カレンダーID");
        calendarIdField = new CompletableJTextField(TEXT_FIELD_WIDTH);
        calendarIdField.setPreferredSize(TEXT_FIELD_SIZE);
        JLabel holidayCalendarLabel = new JLabel("休日カレンダーID");
        holidayCalendarIdField = new CompletableJTextField(TEXT_FIELD_WIDTH);
        holidayCalendarIdField.setPreferredSize(TEXT_FIELD_SIZE);
        JLabel credentialLabel = new JLabel("証明書データ");
        credentialField = new JTextField(TEXT_FIELD_WIDTH);
        credentialField.setPreferredSize(TEXT_FIELD_SIZE);

        JButton updateButton = new JButton("アップデート");

        gbb.add(calendarLabel, 0, row++, 1, 1, GridBagConstraints.WEST);
        gbb.add(calendarIdField, 0, row++, 1, 1, GridBagConstraints.WEST);
        gbb.add(holidayCalendarLabel, 0, row++, 1, 1, GridBagConstraints.WEST);
        gbb.add(holidayCalendarIdField, 0, row++, 1, 1, GridBagConstraints.WEST);
        gbb.add(credentialLabel, 0, row++, 1, 1, GridBagConstraints.WEST);
        gbb.add(credentialField, 0, row++, 1, 1, GridBagConstraints.WEST);
        gbb.add(updateButton, 0, row++, 1, 1, GridBagConstraints.CENTER);

        getUI().add(gbb.getProduct());

        // connect
        updateButton.addActionListener(this::updateAction);
    }

    public void updateAction(ActionEvent e) {
        if (StringUtils.isEmpty(holidayCalendarIdField.getText())
            || StringUtils.isEmpty(calendarIdField.getText())
            || StringUtils.isEmpty(credentialField.getText())) {
            showMessage("必要項目が入力されていません", JOptionPane.ERROR_MESSAGE);
            return;
        }

        GoogleTask task = new GoogleTask(getUI(), "Google カレンダーに", "問い合わせ中...");
        task.setTimeOut(5000);
        task.execute();
    }

    /**
     * Google Calendar API にアクセスする実務.
     */
    private class GoogleTask extends Task<List<Event>> {

        public GoogleTask(Component parent, Object message, String note) {
            super(parent, message, note);
        }

        @Override
        protected List<Event> doInBackground() throws Exception {
            try (InputStream in = new ByteArrayInputStream(credentialField.getText().getBytes());
                 InputStreamReader reader = new InputStreamReader(in)) {

                final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);

                // Build flow and trigger user authorization request.
                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline").build();
                logger.info("tokens path = " + TOKENS_DIRECTORY_PATH);

                LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
                Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

                Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();

                // 前後 1年部のデータを読み込む
                LocalDate today = LocalDate.now();
                Date nextYear = Date.from(today.plusYears(1L).atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date prevYear = Date.from(today.minusYears(1L).atStartOfDay(ZoneId.systemDefault()).toInstant());

                Events events = service.events().list(calendarIdField.getText())
                    .setTimeMin(new DateTime(prevYear.getTime()))
                    .setTimeMax(new DateTime(nextYear.getTime()))
                    .setOrderBy("startTime")
                    .setSingleEvents(true).execute();

                Events holidays = service.events().list(holidayCalendarIdField.getText())
                    .setTimeMin(new DateTime(prevYear.getTime()))
                    .setTimeMax(new DateTime(nextYear.getTime()))
                    .setOrderBy("startTime")
                    .setSingleEvents(true).execute();

                List<Event> eventList = events.getItems();
                eventList.addAll(holidays.getItems());

                return eventList;
            }
        }

        @Override
        protected void succeeded(List<Event> eventList) {

            // 連続日のイベントを解析して, 単日イベントに分解する
            List<Event> additionalEvent = new ArrayList<>();

            for (Event event : eventList) {

                // DateTime で設定されていた場合, Date に揃える.
                // Date は 86,400,000 (1日の msec) の倍数なので，余りをカットする.
                DateTime start = event.getStart().getDateTime();
                if (start != null) {
                    long floordiv = Math.floorDiv(start.getValue(), DAY_LENGTH);
                    event.getStart().setDate(new DateTime(true, floordiv, 0));
                }
                DateTime end = event.getEnd().getDateTime();
                if (end != null) {
                    long floordiv = Math.floorDiv(start.getValue(), DAY_LENGTH);
                    event.getEnd().setDate(new DateTime(true, floordiv, 0));
                }

                // Date に揃ってるはず
                start = event.getStart().getDate();
                end = event.getEnd().getDate();

                // additional day がある場合は event を増やす
                long diff = end.getValue() - start.getValue();
                long additionalDays = Math.floorDiv(diff, DAY_LENGTH) - 1;
                for (long i=0; i<additionalDays; i++) {
                    Event add = event.clone();
                    long startValue = add.getStart().getDate().getValue();
                    add.getStart().setDate(new DateTime(true, startValue + DAY_LENGTH * (1L + i), 0));
                    add.getEnd().setDate(new DateTime(true, startValue + DAY_LENGTH * (2L + i), 0));
                    additionalEvent.add(add);
                    //logger.info("additional day = " + add.getStart().getDate() + "  " + add.getSummary());
                }
            }
            eventList.addAll(additionalEvent);

            if (eventList.isEmpty()) {
                logger.info("No upcoming events found.");

            } else {
                String[][] dataArray = new String[eventList.size()][2];

                for (int i=0; i<eventList.size(); i++) {
                    Event event = eventList.get(i);
                    DateTime start = event.getStart().getDate();

                    // 2019-05-26 -> 20190526
                    dataArray[i][0] = start.toString().substring(0,10).replaceAll("-", "");
                    dataArray[i][1] = event.getSummary();
                }

                calendarJsonData = JsonConverter.toJson(dataArray);
                logger.info("Event fetch succeeded.");
            }

            showMessage("休日データを更新しました\n保存後再起動が必要です", JOptionPane.WARNING_MESSAGE);
        }

        @Override
        protected void cancelled() {
            logger.info("Canceled");
        }

        @Override
        protected void failed(Throwable cause) {
            logger.error("failed " + cause);
            showMessage("データ取得に失敗しました", JOptionPane.ERROR_MESSAGE);
        }

        @Override
        protected void interrupted(InterruptedException ex) {
            logger.info("interrupted " + ex);
        }
    }

    @Override
    public void save() {
        bindViewToModel();
    }

    private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(getUI(), message, "", type);
    }

    private void bindModelToView() {
        calendarIdField.setText(prefs.get(CALENDAR_ID, ""));
        holidayCalendarIdField.setText(prefs.get(HOLIDAY_CALENDAR_ID, ""));
        credentialField.setText(prefs.get(CREDENTIAL, ""));
    }

    private void bindViewToModel() {
        prefs.put(CALENDAR_ID, calendarIdField.getText());
        prefs.put(HOLIDAY_CALENDAR_ID, holidayCalendarIdField.getText());
        prefs.put(CREDENTIAL, credentialField.getText());
        prefs.put(CALENDAR_DATA, calendarJsonData);
    }
}
