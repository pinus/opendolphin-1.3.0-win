package open.dolphin.ui.desktop;

public class Taskbar {
    private static Taskbar TASKBAR = new Taskbar();

    public static Taskbar getTaskbar() {
        return TASKBAR;
    }

    public void setIconBadge(String badge) {
        //com.apple.eawt.Application.getApplication().setDockIconBadge(badge);
    }
}
