package flutter.overlay.window.flutter_overlay_window;

import android.view.MotionEvent;

import java.util.HashMap;
import java.util.Map;

public abstract class OverlayStatusEmitter {
    static private final String methodName = "isShowingOverlay";
    static private boolean lastEmittedStatus;
    
    static void emitIsShowing(boolean isShowing) {
        if(isShowing == lastEmittedStatus) return;
        lastEmittedStatus = isShowing;
        if(CachedMessageChannels.mainAppMessageChannel != null) {
            CachedMessageChannels.mainAppMessageChannel.invokeMethod(methodName, isShowing);
        }
        if(CachedMessageChannels.overlayMessageChannel != null) {
            CachedMessageChannels.overlayMessageChannel.invokeMethod(methodName, isShowing);
        }
    }

    static void emitTouchEvent(MotionEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", event.getAction());
        data.put("x", event.getX());
        data.put("y", event.getY());
        data.put("rawX", event.getRawX());
        data.put("rawY", event.getRawY());
        data.put("pointerCount", event.getPointerCount());

        if(CachedMessageChannels.mainAppMessageChannel != null) {
            CachedMessageChannels.mainAppMessageChannel.invokeMethod("message", data);
        }
        if(CachedMessageChannels.overlayMessageChannel != null) {
            CachedMessageChannels.overlayMessageChannel.invokeMethod("message", data);
        }
    }
}
