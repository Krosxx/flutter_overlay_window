package flutter.overlay.window.flutter_overlay_window;

import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public abstract class OverlayStatusEmitter {
    static private final String methodName = "isShowingOverlay";
    static private boolean lastEmittedStatus;

    static void emitIsShowing(boolean isShowing) {
        if (isShowing == lastEmittedStatus) return;
        lastEmittedStatus = isShowing;
        if (CachedMessageChannels.mainAppMessageChannel != null) {
            CachedMessageChannels.mainAppMessageChannel.invokeMethod(methodName, isShowing);
        }
        if (CachedMessageChannels.overlayMessageChannel != null) {
            CachedMessageChannels.overlayMessageChannel.invokeMethod(methodName, isShowing);
        }
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

    static void emitTouchEvent(MotionEvent event, View view) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", event.getAction());
        data.put("x", event.getX());
        data.put("y", event.getY());
        data.put("rawX", event.getRawX());
        data.put("rawY", event.getRawY());
        data.put("pointerCount", event.getPointerCount());
        fillViewData(view, data);

        if (CachedMessageChannels.mainAppMessageChannel != null) {
            CachedMessageChannels.mainAppMessageChannel.invokeMethod("message", data);
        }
        if (CachedMessageChannels.overlayMessageChannel != null) {
            CachedMessageChannels.overlayMessageChannel.invokeMethod("message", data);
        }
    }

    public static void emitAnimationEnd(Map<String, Object> data) {
        data.put("AnimationEnd" ,1);
        if (CachedMessageChannels.mainAppMessageChannel != null) {
            CachedMessageChannels.mainAppMessageChannel.invokeMethod("message", data);
        }
        if (CachedMessageChannels.overlayMessageChannel != null) {
            CachedMessageChannels.overlayMessageChannel.invokeMethod("message", data);
        }
    }
}
