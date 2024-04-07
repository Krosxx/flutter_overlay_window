package flutter.overlay.window.flutter_overlay_window;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Objects;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

public class FlutterOverlayWindowPlugin implements
        FlutterPlugin, ActivityAware,
        PluginRegistry.ActivityResultListener {


    public interface EngineListener {
        void onPreEngineRestart(@NonNull FlutterEngine engine);

        void onEngineWillDestroy(@NonNull FlutterEngine engine);
    }

    public static EngineListener engineListener;

    private MethodChannel channel;
    private Context context;
    private Activity mActivity;
    private Result pendingResult;
    final int REQUEST_CODE_FOR_OVERLAY_PERMISSION = 1248;
    @Nullable
    ActivityPluginBinding activityPluginBinding;

    static boolean isMainEngine = true;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        Log.i("FlutterOverlay", "FlutterOverlayWindowPlugin onAttachedToEngine: " + flutterPluginBinding.getFlutterEngine());
        this.context = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), OverlayConstants.CHANNEL_TAG);
        channel.setMethodCallHandler(new FloatManagerChannel(this, flutterPluginBinding));

        // boolean isMainAppEngine = !CachedMessageChannels.overlayMessageChannels.containsKey(flutterPluginBinding.getFlutterEngine());
        Log.i("FlutterOverlay", "FlutterOverlayWindowPlugin  isMain " + isMainEngine);
        registerMessageChannel(isMainEngine, flutterPluginBinding);
        isMainEngine = false;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestFloatPermission(Result result) {
        pendingResult = result;
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        mActivity.startActivityForResult(intent, REQUEST_CODE_FOR_OVERLAY_PERMISSION);
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        unregisterMessageChannel(binding);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activityPluginBinding = binding;
        mActivity = binding.getActivity();

        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        Objects.requireNonNull(activityPluginBinding).removeActivityResultListener(this);
        activityPluginBinding = null;
        mActivity = null;
    }

    private void registerMessageChannel(boolean isMainAppEngine, FlutterPluginBinding flutterPluginBinding) {
        io.flutter.plugin.common.BinaryMessenger binaryMessenger = flutterPluginBinding.getBinaryMessenger();
        if (isMainAppEngine) {
            registerMainAppMessageChannel(binaryMessenger);
        } else {
            registerOverlayMessageChannel(flutterPluginBinding);
        }
    }

    private void unregisterMessageChannel(FlutterPluginBinding pluginBinding) {
        boolean isMain = !CachedMessageChannels.overlayMessageChannels.containsKey(pluginBinding.getFlutterEngine());
        if (isMain) {
            if (CachedMessageChannels.mainAppMessageChannel == null) return;
            CachedMessageChannels.mainAppMessageChannel.setMethodCallHandler(null);
            CachedMessageChannels.mainAppMessageChannel = null;
        } else {
            if (CachedMessageChannels.overlayMessageChannels.containsKey(pluginBinding.getFlutterEngine())) {
                CachedMessageChannels.overlayMessageChannels.get(pluginBinding.getFlutterEngine()).setMethodCallHandler(null);
                CachedMessageChannels.overlayMessageChannels.remove(pluginBinding.getFlutterEngine());
            }
        }
    }

    private void registerOverlayMessageChannel(FlutterPluginBinding binding) {
        MethodChannel overlayMessageChannel = new MethodChannel(binding.getBinaryMessenger(), OverlayConstants.MESSENGER_TAG, JSONMethodCodec.INSTANCE);
        overlayMessageChannel.setMethodCallHandler((call, result) -> {
            int cnt = 0;
            if (CachedMessageChannels.mainAppMessageChannel != null) {
                CachedMessageChannels.mainAppMessageChannel.invokeMethod("message", call.arguments);
            }
            for (MethodChannel c : CachedMessageChannels.overlayMessageChannels.values()) {
                if (c != overlayMessageChannel) {
                    c.invokeMethod("message", call.arguments);
                    cnt++;
                }
            }
            Log.i("SubChannel", "转发 sub->sub/main: " + cnt + "/" + CachedMessageChannels.overlayMessageChannels.size());
            result.success(true);
        });
        CachedMessageChannels.overlayMessageChannels.put(binding.getFlutterEngine(), overlayMessageChannel);
    }

    private void registerMainAppMessageChannel(io.flutter.plugin.common.BinaryMessenger mainAppEngineBinaryMessenger) {
        MethodChannel mainAppMessageChannel = new MethodChannel(mainAppEngineBinaryMessenger, OverlayConstants.MESSENGER_TAG, JSONMethodCodec.INSTANCE);
        mainAppMessageChannel.setMethodCallHandler((call, result) -> {
            Log.i("MainChannel", "转发 main->sub: " + CachedMessageChannels.overlayMessageChannels.size());
            for (MethodChannel c : CachedMessageChannels.overlayMessageChannels.values()) {
                c.invokeMethod("message", call.arguments);
            }
            result.success(true);
        });
        CachedMessageChannels.mainAppMessageChannel = mainAppMessageChannel;
    }

    public static boolean checkOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FOR_OVERLAY_PERMISSION) {
            if (pendingResult != null) {
                pendingResult.success(checkOverlayPermission(context));
                pendingResult = null;
            }
            return true;
        }
        return false;
    }

}
