package open.dolphin;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * WebSocket with authorization.
 *
 * @author pns
 */
@ServerEndpoint("/ws/{uid}/{password}")
public class WebSocket {
    // one session corresponds to one client
    private static final List<Session> sessions = new ArrayList<>();
    // logger
    private final Logger logger = Logger.getLogger(WebSocket.class);
    /**
     * Websocket endpoints running in the Java EE platform must have full dependency injection support as described in the CDI specification.
     * Websocket implementations part of the Java EE platform are required to support field, method, and constructor injection using
     * the javax.inject.Inject annotation into all websocket endpoint classes, as well as the use of interceptors for these classes.
     */
    @Inject
    private SecurityFilter securityFilter;

    /**
     * 保持中の session の List を返す
     *
     * @return List of Session
     */
    public static List<Session> getSessions() {
        return Collections.unmodifiableList(sessions);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("uid") String userId, @PathParam("password") String password) {
        logger.info("WebSocket opened: id = " + session.getId());

        if (securityFilter.getValidUserModel(userId, password) == null) {
            closeSession(session, CloseReason.CloseCodes.CANNOT_ACCEPT, "Authentication error.");
            return;
        }
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        logger.info("WebSocket closed: id = " + session.getId() + " " + reason.getReasonPhrase());
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        closeSession(session, CloseReason.CloseCodes.CLOSED_ABNORMALLY, "Error " + t);
    }

    @OnMessage
    public void onMessage(String message) {
        logger.info(("WebSocket received message: ") + message);
    }

    private void closeSession(Session session, CloseReason.CloseCode code, String reasonPhrase) {
        try {
            CloseReason reason = new CloseReason(code, reasonPhrase);
            session.close(reason);
        } catch (IOException e) {
        }
    }
}
