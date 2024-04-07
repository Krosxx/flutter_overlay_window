package flutter.overlay.window.flutter_overlay_window;

import static flutter.overlay.window.flutter_overlay_window.FlutterOverlayWindowPlugin.checkOverlayPermission;
import static flutter.overlay.window.flutter_overlay_window.FlutterOverlayWindowPlugin.engineListener;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

import io.flutter.FlutterInjector;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.FlutterEngineGroup;
import io.flutter.embedding.engine.FlutterEngineGroupCache;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class FloatManagerChannel implements MethodChannel.MethodCallHandler {

    @NonNull
    FlutterPlugin.FlutterPluginBinding flutterPluginBinding;
    FlutterOverlayWindowPlugin overlayPlugin;

    public FloatManagerChannel(FlutterOverlayWindowPlugin overlayPlugin, @NonNull FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
        this.flutterPluginBinding = flutterPluginBinding;
        this.overlayPlugin = overlayPlugin;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        Log.i("FloatManagerChannel", "onMethodCall: " + call.method + " (" + call.arguments + ")");
        switch (call.method) {
            case "checkPermission":
                result.success(checkOverlayPermission(flutterPluginBinding.getApplicationContext()));
                break;
            case "requestPermission":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    overlayPlugin.requestFloatPermission(result);
                } else {
                    result.success(true);
                }
                break;
            case "showOverlay":
                if (!checkOverlayPermission(flutterPluginBinding.getApplicationContext())) {
                    result.error("PERMISSION", "overlay permission is not enabled", null);
                    return;
                }

                String winName = call.argument("win_name");
                FloatingWindow win = FloatManager.getByName(winName);

                if (win != null) {
                    win.show();
                } else {
                    Integer height = call.argument("height");
                    Integer width = call.argument("width");
                    String alignment = call.argument("alignment");
                    String flag = call.argument("flag");
                    boolean enableDrag = Boolean.TRUE.equals(call.argument("enableDrag"));
                    String positionGravity = call.argument("positionGravity");
                    int xPos = call.argument("xPos");
                    int yPos = call.argument("yPos");
                    FloatingConfig config = new FloatingConfig(
                            height != null ? height : -1, width != null ? width : -1,
                            flag != null ? flag : "flagNotFocusable", alignment != null ? alignment : "center",
                            positionGravity, enableDrag, xPos, yPos);
                    win = FloatManager.makeNewFloating(flutterPluginBinding.getApplicationContext(), winName, config,
                            createEngine(winName));
                    win.show();
                }
                result.success(null);
                return;
            case "updateFlag":
                winName = call.argument("win_name");
                String flag = call.argument("flag");
                win = FloatManager.getByName(winName);
                if (win != null) {
                    win.updateFlag(flag);
                    result.success(true);
                } else {
                    result.success(false);
                }
                return;
            case "resizeOverlay":
                winName = call.argument("win_name");
                int width = call.argument("width");
                int height = call.argument("height");
                win = FloatManager.getByName(winName);
                if (win != null) {
                    result.success(win.resizeOverlay(width, height));
                } else {
                    result.success(false);
                }
                return;
            case "isOverlayActive":
                result.success(FloatManager.isShowing(call.argument("win_name")));
                return;
            case "closeOverlay":
                result.success(FloatManager.hide(call.argument("win_name")));
                return;
            case "removeOverlay":
                result.success(FloatManager.remove(call.argument("win_name")));
                return;
            default:
                result.notImplemented();
                break;
        }
    }

    private FlutterEngineGroup ensureEngineGroupCreated(android.content.Context context, String winName) {
        FlutterEngineGroup enn = FlutterEngineGroupCache.getInstance().get(OverlayConstants.CACHED_TAG);
        if (enn == null) {
            enn = new FlutterEngineGroup(context);
            FlutterEngineGroupCache.getInstance().put(OverlayConstants.CACHED_TAG, enn);
        }
        return enn;
    }

    private FlutterEngine createEngine(String winName) {
        Context context = flutterPluginBinding.getApplicationContext();
        FlutterEngine engine = FlutterEngineCache.getInstance().get(winName);
        if (engine == null) {
            FlutterEngineGroup enn = ensureEngineGroupCreated(context, winName);
            DartExecutor.DartEntrypoint dEntry = new DartExecutor.DartEntrypoint(
                    FlutterInjector.instance().flutterLoader().findAppBundlePath(), winName);
            engine = Objects.requireNonNull(enn).createAndRunEngine(context, dEntry);
            FlutterEngineCache.getInstance().put(winName, engine);
            FlutterEngine finalEngine = engine;
            if (engineListener != null) {
                engineListener.onPreEngineRestart(engine);
            }
            engine.addEngineLifecycleListener(new FlutterEngine.EngineLifecycleListener() {
                @Override
                public void onPreEngineRestart() {
                }

                @Override
                public void onEngineWillDestroy() {
                    FlutterEngineCache.getInstance().remove(winName);
                    if (engineListener != null) {
                        engineListener.onEngineWillDestroy(finalEngine);
                    }
                }
            });
        }
        return engine;
    }


}
