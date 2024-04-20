package flutter.overlay.window.flutter_overlay_window;

import android.content.Context;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import io.flutter.embedding.android.FlutterSurfaceView;
import io.flutter.embedding.android.FlutterTextureView;
import io.flutter.embedding.android.FlutterView;

public class FloatFlutterView extends FlutterView {
    public FloatFlutterView(@NonNull Context context, @NonNull FlutterSurfaceView flutterSurfaceView) {
        super(context, flutterSurfaceView);
    }

    public FloatFlutterView(@NonNull Context context, @NonNull FlutterTextureView flutterTextureView) {
        super(context, flutterTextureView);
    }

    public FloatFlutterView(@NonNull Context context) {
        super(context);
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (isAttachedToFlutterEngine() && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                getAttachedFlutterEngine().getNavigationChannel().popRoute();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
