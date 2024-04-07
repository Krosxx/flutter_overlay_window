package flutter.overlay.window.flutter_overlay_window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import java.util.Map;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodChannel;

public abstract class CachedMessageChannels {
    @Nullable
    public static MethodChannel mainAppMessageChannel;
    @NonNull
    public static Map<FlutterEngine, MethodChannel> overlayMessageChannels = new ArrayMap<>();
}
