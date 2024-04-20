package flutter.overlay.window.flutter_overlay_window;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.flutter.embedding.android.FlutterTextureView;
import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;

public class FloatingWindow implements View.OnTouchListener {
    String winName;
    FloatingConfig config;

    FlutterEngine flutterEngine;
    private final int DEFAULT_NAV_BAR_HEIGHT_DP = 48;
    private final int DEFAULT_STATUS_BAR_HEIGHT_DP = 25;

    private Integer mStatusBarHeight = -1;
    private Integer mNavigationBarHeight = -1;
    private final Resources mResources;

    public boolean isShowing = false;
    @NonNull
    private WindowManager windowManager;
    private FlutterView flutterView;
    private int clickableFlag = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

    private final Handler mAnimationHandler = new Handler(Looper.getMainLooper());
    private float lastX, lastY;
    private int lastYPosition;
    private boolean dragging;
    private static final float MAXIMUM_OPACITY_ALLOWED_FOR_S_AND_HIGHER = 0.8f;
    private Timer mTrayAnimationTimer;

    private final Context context;
    private boolean freezeTouch = false;


    public FloatingWindow(Context context, String winName, FloatingConfig config, FlutterEngine engine) {
        this.winName = winName;
        this.config = config;
        this.flutterEngine = engine;
        this.context = context;

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mResources = context.getResources();
    }

    public boolean isShowing() {
        return isShowing;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void show() {
        if (flutterView != null) {
            // flutterView.attachToFlutterEngine(flutterEngine);
            windowManager.addView(flutterView, flutterView.getLayoutParams());
            OverlayStatusEmitter.emitIsShowing(winName, true);
            isShowing = true;
            return;
        }
        isShowing = true;
        flutterEngine.getLifecycleChannel().appIsResumed();

        flutterView = new FloatFlutterView(context, new FlutterTextureView(context));
        flutterView.setAccessibilityDelegate(null);
        flutterView.attachToFlutterEngine(flutterEngine);
        flutterView.setFitsSystemWindows(true);
        flutterView.setFocusable(true);
        flutterView.setFocusableInTouchMode(true);
        flutterView.setBackgroundColor(Color.TRANSPARENT);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                config.width < 0 ? config.width : dpToPx(config.width),
                config.height >= 0 ? dpToPx(config.height) : config.height,
                config.xPos == -1 ? screenWidth() : dpToPx(config.xPos),
                dpToPx(config.yPos),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        // | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.RGBA_8888
        );
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && config.flag == clickableFlag) {
        //     params.alpha = MAXIMUM_OPACITY_ALLOWED_FOR_S_AND_HIGHER;
        // }
        params.gravity = config.gravity;
        if (config.enableDrag) {
            flutterView.setOnTouchListener(this);
        }
        windowManager.addView(flutterView, params);
        OverlayStatusEmitter.emitIsShowing(winName, true);
    }

    public void hide() {
        if (isShowing && flutterView != null) {
            flutterEngine.getLifecycleChannel().appIsPaused();
            // flutterView.detachFromFlutterEngine();
            windowManager.removeView(flutterView);
        }
        isShowing = false;
        OverlayStatusEmitter.emitIsShowing(winName, false);
    }

    public void release() {
        hide();
        if (flutterView != null) {
            flutterView = null;
        }
    }


    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                Float.parseFloat(dp + ""), mResources.getDisplayMetrics());
    }

    private boolean inPortrait() {
        return mResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int screenHeight() {
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        return inPortrait() ?
                dm.heightPixels + statusBarHeightPx() + navigationBarHeightPx()
                :
                dm.heightPixels + statusBarHeightPx();
    }

    private int statusBarHeightPx() {
        if (mStatusBarHeight == -1) {
            int statusBarHeightId = mResources.getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarHeightId > 0) {
                mStatusBarHeight = mResources.getDimensionPixelSize(statusBarHeightId);
            } else {
                mStatusBarHeight = dpToPx(DEFAULT_STATUS_BAR_HEIGHT_DP);
            }
        }

        return mStatusBarHeight;
    }

    int navigationBarHeightPx() {
        if (mNavigationBarHeight == -1) {
            int navBarHeightId = mResources.getIdentifier("navigation_bar_height", "dimen", "android");

            if (navBarHeightId > 0) {
                mNavigationBarHeight = mResources.getDimensionPixelSize(navBarHeightId);
            } else {
                mNavigationBarHeight = dpToPx(DEFAULT_NAV_BAR_HEIGHT_DP);
            }
        }

        return mNavigationBarHeight;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int screenWidth() {
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        return dm.widthPixels;
    }

    public boolean resizeOverlay(int width, int height) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) flutterView.getLayoutParams();
        params.width = width < 0 ? width : dpToPx(width);
        params.height = height >= 0 ? dpToPx(height) : height;
        if (isShowing) {
            windowManager.updateViewLayout(flutterView, params);
            flutterView.requestLayout();
        }
        return true;
    }

    // public boolean updateFlag(String flag) {
    //     config.setFlag(flag);
    //     WindowManager.LayoutParams params = (WindowManager.LayoutParams) flutterView.getLayoutParams();
    //     params.flags = config.flag |
    //             WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
    //             WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
    //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && config.flag == clickableFlag) {
    //         params.alpha = MAXIMUM_OPACITY_ALLOWED_FOR_S_AND_HIGHER;
    //     } else {
    //         params.alpha = 1;
    //     }
    //     if (isShowing) {
    //         windowManager.updateViewLayout(flutterView, params);
    //     }
    //     return true;
    // }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (config.enableDrag && !freezeTouch) {
            OverlayStatusEmitter.emitTouchEvent(event, view, winName);
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) flutterView.getLayoutParams();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragging = false;
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - lastX;
                    float dy = event.getRawY() - lastY;
                    if (!dragging && dx * dx + dy * dy < 25) {
                        return false;
                    }
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    params.x += (int) dx;
                    params.y += (int) dy;
                    windowManager.updateViewLayout(flutterView, params);
                    dragging = true;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    lastYPosition = params.y;
                    Map<String, Object> data = fillViewData(view, new HashMap<>());
                    if (!Objects.equals(config.positionGravity, "none")) {
                        windowManager.updateViewLayout(flutterView, params);
                        int[] loc = new int[2];
                        view.getLocationOnScreen(loc);
                        TrayAnimationTimerTask mTrayTimerTask = new TrayAnimationTimerTask(data, loc[0] + view.getWidth() / 2, view.getWidth(), screenWidth());
                        mTrayAnimationTimer = new Timer();
                        mTrayAnimationTimer.schedule(mTrayTimerTask, 0, 25);
                    } else {
                        OverlayStatusEmitter.emitAnimationEnd(data);
                    }
                    return false;
                default:
                    return false;
            }
            return false;
        }
        return false;
    }

    public Map<String, Object> fillViewData(View view, Map<String, Object> data) {
        data.put("win_name", winName);
        data.put("height", view.getHeight());
        data.put("width", view.getWidth());
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        data.put("viewX", loc[0]);
        data.put("viewY", loc[1]);
        return data;
    }

    public void freezeTouch() {
        freezeTouch = true;
    }

    public void resumeTouch() {
        freezeTouch = false;
    }

    private class TrayAnimationTimerTask extends TimerTask {
        int mDestX;
        int mDestY;
        Map<String, Object> data;
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) flutterView.getLayoutParams();

        public TrayAnimationTimerTask(Map<String, Object> data, int x, int vw, int screenWidth) {
            super();
            this.data = data;
            mDestY = lastYPosition;
            switch (config.positionGravity) {
                case "auto":
                    mDestX = x < screenWidth / 2 ? 0 : screenWidth - vw;
                    return;
                case "left":
                    mDestX = 0;
                    return;
                case "right":
                    mDestX = screenWidth - vw;
                    return;
                default:
                    mDestX = params.x;
                    mDestY = params.y;
            }
        }

        @Override
        public void run() {
            mAnimationHandler.post(() -> {
                params.x = (2 * (params.x - mDestX)) / 3 + mDestX;
                params.y = (2 * (params.y - mDestY)) / 3 + mDestY;
                windowManager.updateViewLayout(flutterView, params);
                if (Math.abs(params.x - mDestX) < 2 && Math.abs(params.y - mDestY) < 2) {
                    TrayAnimationTimerTask.this.cancel();
                    mTrayAnimationTimer.cancel();
                    OverlayStatusEmitter.emitAnimationEnd(data);
                }
            });
        }
    }

}
