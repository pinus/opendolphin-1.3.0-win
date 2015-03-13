package open.dolphin.orca;

import open.dolphin.WebSocket;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import open.dolphin.JsonConverter;
import open.dolphin.infomodel.PatientVisitModel;

/**
 * PatientVisitModel が変化したら通知を受ける Listener.
 * see PatientVisitModel @EntityListeners
 * @author pns
 */
public class PatientVisitModelListener {

    /**
     * Inform pvt update to WebSocket clients.
     * @param pvt
     */
    @PostPersist
    @PostUpdate
    public void pvtUpdated(final PatientVisitModel pvt) {
        WebSocket.getSessions().forEach(session -> session.getAsyncRemote().sendText(JsonConverter.toJson(pvt)));
    }
}
