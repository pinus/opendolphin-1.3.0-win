package open.dolphin.ui;

import com.alderstone.multitouch.mac.touchpad.Finger;
import com.alderstone.multitouch.mac.touchpad.FingerState;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

/**
 * GlulogicMT.jar を使って Magic Trackpad のデバイス状況を調べる.
 *
 * @author pns
 */
public class TouchpadTest implements Observer {
    // 指の最大数
    private static final int MAX_FINGER_BLOBS = 20;
    // それぞれの指を入れておく配列
    private static Finger blobs[] = new Finger[MAX_FINGER_BLOBS];
    // Observer（自分）のインスタンス
    private static TouchpadTest me = new TouchpadTest();
    // TouchpadObservable
    //private static TouchpadObservable tpo = TouchpadObservable.getInstance();

    // Trackpad に触っているかどうか
    private static boolean pressed;
    // 指の移動スピード
    private static float xVelocity;
    private static float yVelocity;

    private static Logger logger;

    private TouchpadTest() {
        logger = Logger.getLogger(TouchpadTest.class);
        logger.setLevel(Level.DEBUG);
    }

    /**
     * Observe 開始.
     */
//    public static void startListening() {
//        tpo.addObserver(me);
//    }

    /**
     * Observe を止める.
     */
//    public static void stopListening() {
//        tpo.deleteObserver(me);
//    }

    /**
     * マウスの背中に触っているかどうかを返す.
     *
     * @return
     */
    public static boolean isPressed() {
        return pressed;
    }

    /**
     * X 軸方向の速度　右がプラス.
     *
     * @return
     */
    public static float getXVelocity() {
        return xVelocity;
    }

    /**
     * Y 軸方向の速度　上がプラス.
     *
     * @return
     */
    public static float getYVelocity() {
        return yVelocity;
    }

    /**
     * TouchpadObservable からデータ（Finger）がここに送られてくる.
     *
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        Finger finger = (Finger) arg;
        int id = finger.getID();
        if (id <= MAX_FINGER_BLOBS) {
            blobs[id - 1] = finger;
        }

        // blob が１つでも press だったら pressed = true
        pressed = false;
        // 速度は最初に見つかった pressed の finger の値とする
        xVelocity = 0;
        yVelocity = 0;

        for (int i = 0; i < MAX_FINGER_BLOBS; i++) {
            if (blobs[i] != null) {
                // pressed の判定
                if (blobs[i] != null && blobs[i].getState() == FingerState.PRESSED) {
                    pressed = true;
                    xVelocity = blobs[i].getXVelocity();
                    yVelocity = blobs[i].getYVelocity();
                    break;
                }
            }
        }
        //showRawData(finger);
    }

    private void showRawData(Finger f) {
        System.out.printf("id=%2d frame=%d size=%1.2f x=%1.2f xvel=%1.2f y=%1.2f yvel=%1.2f state=%s\n",
                f.getID(), f.getFrame(), f.getSize(), f.getX(), f.getXVelocity(), f.getY(), f.getYVelocity(), f.getState());
    }

    public static void main(String[] argv) {
        //TouchpadTest.startListening();
        JOptionPane.showMessageDialog(null, "showing raw data");
    }
}
