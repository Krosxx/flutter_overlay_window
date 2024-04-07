package flutter.overlay.window.flutter_overlay_window;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

public abstract class OverlayStatusEmitter {

    static void emitIsShowing(String winName, boolean isShowing) {
        Map<String, Object> data = new HashMap<>();
        data.put("win_name", winName);
        data.put("show", isShowing);
        broadcast("isShowingOverlay", data);
    }

    public static Map<String, Object> fillViewData(View view, Map<String, Object> data) {
        data.put("height", view.getHeight());
        data.put("width", view.getWidth());
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        data.put("viewX", loc[0]);
        data.put("viewY", loc[1]);
        return data;
    }

    static void emitTouchEvent(MotionEvent event, View view, String winName) {
        Map<String, Object> data = new HashMap<>();
        data.put("win_name", winName);
        data.put("action", event.getAction());
        data.put("x", event.getX());
        data.put("y", event.getY());
        data.put("rawX", event.getRawX());
        data.put("rawY", event.getRawY());
        data.put("pointerCount", event.getPointerCount());
        fillViewData(view, data);
        Log.i("emitTouchEvent", "emitTouchEvent " + data);

        broadcast("message", data);
    }

    public static void emitAnimationEnd(Map<String, Object> data) {
        data.put("AnimationEnd", 1);
        broadcast("message", data);
    }

    private static void broadcast(String method, Object data) {
        if (CachedMessageChannels.mainAppMessageChannel != null) {
            CachedMessageChannels.mainAppMessageChannel.invokeMethod(method, data);
        }
        for (MethodChannel c : CachedMessageChannels.overlayMessageChannels.values()) {
            c.invokeMethod(method, data);
        }
    }
}
