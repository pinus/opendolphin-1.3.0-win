package open.dolphin.orca.orcaapi;

import open.dolphin.orca.OrcaHostInfo;
import org.jboss.logging.Logger;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URLConnection;

/**
 * Low level orca api handler. Authentication and GET/POST.
 *
 * @author pns
 */
public class OrcaApiHandler {

    private static final OrcaApiHandler ORCA_API_HANDLER = new OrcaApiHandler();
    private final OrcaHostInfo hostInfo = OrcaHostInfo.getInstance();
    private Logger logger = Logger.getLogger(OrcaApiHandler.class);

    private OrcaApiHandler() {
    }

    public static OrcaApiHandler getInstance() {
        return ORCA_API_HANDLER;
    }

    /**
     * Authenticate.
     */
    private void authenticate() {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        hostInfo.getUserId(),
                        hostInfo.getPassword().toCharArray());
            }
        });
    }

    /**
     * GET.
     *
     * @param uri URI
     * @return response
     */
    public String get(URI uri) {

        String response = null;

        try {
            authenticate();
            URLConnection con = uri.toURL().openConnection();

            try (InputStream in = con.getInputStream()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                StringBuilder sb = new StringBuilder();
                while ((response = br.readLine()) != null) {
                    sb.append(response);
                }
                response = sb.toString();
            }

        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        return response;
    }

    /**
     * POST.
     *
     * @param uri URI
     * @param doc Document
     * @return response
     */
    public String post(URI uri, String doc) {

        String response = null;

        try {
            authenticate();
            URLConnection con = uri.toURL().openConnection();

            con.setDoOutput(true);

            try (OutputStream out = con.getOutputStream();
                 PrintWriter writer = new PrintWriter(out)) {
                writer.print(doc);
            }

            try (InputStream in = con.getInputStream()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                while ((response = br.readLine()) != null) {
                    sb.append(response);
                }
                response = sb.toString();
            }

        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        return response;
    }
}
