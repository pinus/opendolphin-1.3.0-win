package open.dolphin.helper;

import java.util.Arrays;
import java.util.List;

/**
 * StackTracer.
 *
 * @author pns
 */
public class StackTracer {

    public static void showTrace() {
        Throwable t = new Throwable();
        StackTraceElement[] e = t.getStackTrace();

        for (int i = 1; i < e.length; i++) {
            System.out.println(String.format("%d:%s#%s:%d", i, e[i].getClassName(), e[i].getMethodName(), e[i].getLineNumber()));
        }
    }

    public static List<StackTraceElement> getTrace() {
        return Arrays.asList(new Throwable().getStackTrace());
    }
}
