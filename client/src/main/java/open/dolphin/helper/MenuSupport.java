package open.dolphin.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Object に対して順番にリフレクションで method を実行していく.
 * MainWindow 層では
 * <ul>
 * <li>chains[0] = MainWindow (WaitingListImpl/PatientSearchImpl/LaboTestImporter)
 * <li>chains[1] = null
 * <li>chains[2] = this
 * <li>chains[3] = Dolphin
 * </ul>
 * addChain するところは {@link open.dolphin.client.Dolphin#initComponents() Dolphin} のタブ切換設定の部分.
 * StampBoxPlugin, ImageBox もインスタンスを持っているが，メニュー表示だけで addChain は未実装.<br>
 * ChartDocument 層での使用は {@link open.dolphin.client.ChartMediator ChartMediator} で行う.
 *
 * @author Minagawa, Kazushi
 * @author pns
 */
public class MenuSupport implements MenuListener {

    private final Object[] chains = new Object[4];
    private final Logger logger = LoggerFactory.getLogger(MenuSupport.class);
    private ActionMap actions;

    public MenuSupport(Object owner) {
        setDefaultChains(owner);
    }

    private void setDefaultChains(Object owner) {
        chains[2] = this;
        chains[3] = owner;
    }

    @Override
    public void menuSelected(MenuEvent e) {
    }

    @Override
    public void menuDeselected(MenuEvent e) {
    }

    @Override
    public void menuCanceled(MenuEvent e) {
    }

    /**
     * メニューの ActionMap を登録する.
     *
     * @param actions ActionMap
     */
    public void registerActions(ActionMap actions) {
        this.actions = actions;
    }

    /**
     * action key の action を返す.
     *
     * @param actionKey aciton key name
     * @return Action
     */
    public Action getAction(String actionKey) {
        if (actions != null) {
            return actions.get(actionKey);
        }
        return null;
    }

    /**
     * ActionMap を返す.
     *
     * @return ActionMap
     */
    public ActionMap getActions() {
        return actions;
    }

    /**
     * 全てのメニューを disable する.
     */
    public void disableAllMenus() {
        if (actions != null) {
            Stream.of(actions.keys()).forEach(obj -> actions.get(obj).setEnabled(false));
        }
    }

    /**
     * action key に該当する menu を disable する.
     *
     * @param actionKeys array of action key
     */
    public void disableMenus(String[] actionKeys) {
        enableMenus(actionKeys, false);
    }

    /**
     * action key に該当する menu を enable する.
     *
     * @param actionKeys array of action key
     */
    public void enableMenus(String[] actionKeys) {
        enableMenus(actionKeys, true);
    }

    /**
     * action key に該当する menu を enable/disable する.
     *
     * @param actionKeys array of action key
     */
    public void enableMenus(String[] actionKeys, boolean enable) {
        if (actions != null && actionKeys != null) {
            Stream.of(actionKeys)
                    .map(actions::get)
                    .filter(Objects::nonNull)
                    .forEach(action -> action.setEnabled(enable));
        }
    }

    /**
     * action key に該当する menu を enable/disable する.
     *
     * @param actionKey action key
     */
    public void enableAction(String actionKey, boolean enabled) {
        if (actions != null) {
            Action action = actions.get(actionKey);
            if (action != null) {
                action.setEnabled(enabled);
            }
        }
    }

    /**
     * 最初のターゲットに設定する.
     *
     * @param obj first target
     */
    public void addChain(Object obj) {
        chains[0] = obj;
        logger.debug("MenuSupport: addChain = " + obj);
        logger.debug("chains[1] = " + chains[1]);
        logger.debug("chains[2] = " + chains[2]);
    }

    /**
     * 最初のターゲットを返す.
     *
     * @return first target
     */
    public Object getChain() {
        return chains[0];
    }

    /**
     * 二番目のターゲットに設定する.
     *
     * @param obj second target
     */
    public void addChain2(Object obj) {
        chains[1] = obj;
        logger.debug("MenuSupport: addChain2 = " + obj);
        logger.debug("chains[0] = " + chains[0]);
        logger.debug("chains[2] = " + chains[2]);
    }

    /**
     * ２番目のターゲットを返す.
     *
     * @return second target
     */
    public Object getChain2() {
        return chains[1];
    }

    /**
     * chain にそってリフレクションでメソッドを実行する.
     * メソッドを実行するオブジェクトがあればそこで終了する.
     * メソッドを実行するオブジェクトが存在しない場合もそこで終了する.
     * コマンドチェインパターンのリフレクション版.
     *
     * @param method メソッド
     * @return メソッドが実行された時 true
     */
    public boolean sendToChain(String method) {
        logger.debug(String.format("MenuSupport: sendToChain: %s: [0]=%s, [1]=%s,[2]=%s", method, chains[0], chains[1], chains[2]));

        boolean handled = false;

        for (Object target : chains) {

            if (target != null) {
                try {
                    Method mth = target.getClass().getMethod(method, (Class[]) null);
                    // System.out.println("invoked: " + target.getClass() + "#" + method);
                    mth.invoke(target, (Object[]) null);
                    handled = true;
                    break;

                } catch (IllegalAccessException | IllegalArgumentException | SecurityException ex) {
                    System.out.println("MenuSupport.java: " + ex);
                } catch (InvocationTargetException ex) {
                    System.out.println("MenuSupport.java: " + ex);
                    ex.printStackTrace(System.err);

                    // この target では実行できない. NoSuchMethodException が出るのは問題なし.
                } catch (NoSuchMethodException ex) { //System.out.println("MenuSupport.java: " + ex);
                }
            }
        }

        return handled;
    }
}
