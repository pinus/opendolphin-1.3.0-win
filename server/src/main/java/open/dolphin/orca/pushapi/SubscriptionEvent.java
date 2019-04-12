package open.dolphin.orca.pushapi;

/**
 * PushAPI subscription events.
 * "patient_infomation", not "information"
 *
 * @author pns
 */
public enum SubscriptionEvent {
    ACCEPT("patient_accept"), // 受付通知
    INFORMATION("patient_infomation"), // 患者登録通知
    ACCOUNT("patient_account"), // 診療行為通知
    MEMO("patient_memo"), // メモ登録通知
    ALL("*") // 全イベント
    ;

    private String eventName;

    SubscriptionEvent(String str) {
        eventName = str;
    }

    public String eventName() {
        return eventName;
    }
}
