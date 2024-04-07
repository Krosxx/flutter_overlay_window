package flutter.overlay.window.flutter_overlay_window;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;

public class FloatManager {

    public static Map<String, FloatingWindow> floatingCache = new HashMap<>();

    @Nullable
    public static FloatingWindow getByName(String winName) {
        return floatingCache.get(winName);
    }

    public static FloatingWindow makeNewFloating(Context context, String winName, FloatingConfig config, FlutterEngine engine) {
        FloatingWindow floatingWindow = new FloatingWindow(context, winName, config, engine);
        floatingCache.put(winName, floatingWindow);
        return floatingWindow;
    }

    public static boolean isShowing(String winName) {
        FloatingWindow win = getByName(winName);
        return win != null && win.isShowing();
    }

    public static boolean hide(String winName) {
        FloatingWindow win = getByName(winName);
        if (win != null && win.isShowing) {
            win.hide();
            return true;
        }
        return false;
    }

    public static boolean remove(String winName) {
        FloatingWindow win = getByName(winName);
        if (win != null && win.isShowing) {
            win.release();
        }
        if (win != null) {
            CachedMessageChannels.overlayMessageChannels.remove(win.flutterEngine);
        }
        floatingCache.remove(winName);
        FlutterEngineCache.getInstance().remove(winName);
        return true;
    }

    public static void pauseAllTouch() {
        for (FloatingWindow win : floatingCache.values()) {
            win.freezeTouch();
        }
    }

    public static void resumeAllTouch() {
        for (FloatingWindow win : floatingCache.values()) {
            win.resumeTouch();
        }
    }
}
