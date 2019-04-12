package open.dolphin.orca.pushapi;

import open.dolphin.JsonConverter;
import open.dolphin.orca.pushapi.bean.Response;
import org.apache.log4j.Logger;

import javax.websocket.*;

/**
 * WebSocket の Endpoint (Tyrus).
 *
 * @author pns
 */
@ClientEndpoint(configurator = PushApiEndopointConfigurator.class)
public class PushApiEndpoint {

    /**
     * 受け取った message を伝える listener.
     */
    private ResponseListener responseListener;

    private final Logger logger = Logger.getLogger(PushApiEndpoint.class);

    public PushApiEndpoint() {
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        logger.info("open id = " + session.getId());
    }

    @OnMessage
    public void onMessage(String str) {
        Response response = JsonConverter.fromJson(str, Response.class);
        responseListener.onResponse(response);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info("closeReason = " + closeReason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        thr.printStackTrace(System.err);
    }

    /**
     * ResponseListener を登録する.
     *
     * @param listener ResponseListener
     */
    public void addResponseListener(ResponseListener listener) {
        responseListener = listener;
    }
}
