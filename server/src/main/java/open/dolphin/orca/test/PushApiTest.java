package open.dolphin.orca.test;

import open.dolphin.orca.PvtClient;

public class PushApiTest {
    public PushApiTest() {
    }

    public static void main(String[] argv) {
        PushApiTest test = new PushApiTest();
        test.start();
    }

    private synchronized void start() {
        PvtClient client = new PvtClient();
        client.subscribe();
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }
}
