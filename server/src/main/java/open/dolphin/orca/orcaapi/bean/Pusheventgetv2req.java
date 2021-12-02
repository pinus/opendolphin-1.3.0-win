package open.dolphin.orca.orcaapi.bean;

/**
 * pusheventgetv2req.
 * @author pns
 */
public class Pusheventgetv2req {
    /**
     * イベント名 (例: patient_accept)
     */
    private String event;

    /**
     * ユーザＩＤ (例: ormaster)
     */
    private String user;

    /**
     * 範囲指定の開始日時 (例: 2017-01-2413:00)
     */
    private String start_time;

    /**
     * 範囲指定の終了日時 (例: 2017-01-2512:59)
     */
    private String end_time;

    /**
     * event
     *
     * @return event
     */
    public String getEvent() {
        return event;
    }

    /**
     * event
     *
     * @param event to set
     */
    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * user
     *
     * @return user
     */
    public String getUser() {
        return user;
    }

    /**
     * user
     *
     * @param user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * start_time
     *
     * @return start_time
     */
    public String getStart_time() {
        return start_time;
    }

    /**
     * start_time
     *
     * @param start_time to set
     */
    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    /**
     * end_time
     *
     * @return end_time
     */
    public String getEnd_time() {
        return end_time;
    }

    /**
     * end_time
     *
     * @param end_time to set
     */
    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }
}