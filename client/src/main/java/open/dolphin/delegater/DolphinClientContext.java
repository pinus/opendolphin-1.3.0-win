package open.dolphin.delegater;

import open.dolphin.JsonConverter;
import open.dolphin.helper.HashUtil;
import open.dolphin.infomodel.InfoModel;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.util.Encode;

import javax.websocket.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.net.URI;

/**
 * DolphinClientContext.
 *
 * @author pns
 */
public class DolphinClientContext {
    private static final String DOLPHIN_PATH = "dolphin";

    private static final DolphinClientContext context;
    private static ResteasyWebTarget target;
    private static WebSocketContainer webSocketContainer;
    private static URI webSocketUri;

    static {
        context = new DolphinClientContext();
    }

    /**
     * hostAddress, userId, password で context を設定する.
     * userId は facilityId:username というコンポジット型式.
     *
     * @param hostAddress 8080 ポートも指定されたホストアドレス
     * @param userId      コンポジット形式の userId (=facilityId:username)
     * @param password    hash されていない生パスワード
     */
    public static void configure(String hostAddress, String userId, String password) {
        // password を MD5 変換
        String hashPass = HashUtil.MD5(password);

        // Resteasy
        ResteasyClientBuilder builder = (ResteasyClientBuilder) ClientBuilder.newBuilder();
        ResteasyClient client = builder.connectionPoolSize(20).build();

        // register providers
        try {
            // ResteasyJackson2Provider: only needed for jar file
            ResteasyJackson2Provider jackson2Provider = new ResteasyJackson2Provider();
            client.register(jackson2Provider);

            // Authorization
            AuthorizationFilter authorizationFilter = new AuthorizationFilter(userId, hashPass);
            client.register(authorizationFilter);

            // Customized ObjectMapper
            JsonConverter jsonConverter = new JsonConverter();
            client.register(jsonConverter);

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        String restUrl = String.format(("http://%s/%s"), hostAddress, DOLPHIN_PATH);
        target = client.target(restUrl);

        // WebSocket
        webSocketContainer = ContainerProvider.getWebSocketContainer();
        webSocketUri = URI.create(String.format(
                "ws://%s/%s/ws/%s/%s",
                hostAddress, DOLPHIN_PATH, userId, hashPass));
    }

    /**
     * DolphinClientContext を返す.
     *
     * @return
     */
    public static DolphinClientContext getContext() {
        return context;
    }

    /**
     * ResteasyWebTarget を返す.
     *
     * @return
     */
    public ResteasyWebTarget getWebTarget() {
        return target;
    }

    /**
     * WebSocket の Endpoint に登録する.
     *
     * @param endpoint
     * @return 登録されたセッション
     */
    public Session setEndpoint(Endpoint endpoint) {
        try {
            return webSocketContainer.connectToServer(endpoint, webSocketUri);
        } catch (DeploymentException | IOException ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    /**
     * Authorization header を作成する.
     * facilityId:username;password という型式を Base64 変換したものを送る.
     */
    private static class AuthorizationFilter implements ClientRequestFilter {
        private final String header;

        public AuthorizationFilter(String userId, String hashPass) {

            String str = userId + InfoModel.PASSWORD_SEPARATOR + hashPass;
            header = Encode.encodeString(str); // application/x-www-form-urlencoded
            // System.out.println("DolphinClientContext: authorization header = " + header);
        }

        @Override
        public void filter(ClientRequestContext requestContext) {
            requestContext.getHeaders().add("Authorization", header);
        }
    }
}
