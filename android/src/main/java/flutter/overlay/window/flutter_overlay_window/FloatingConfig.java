package flutter.overlay.window.flutter_overlay_window;

import android.view.Gravity;
import android.view.WindowManager;

public class FloatingConfig {


    int height = WindowManager.LayoutParams.MATCH_PARENT;
    int width = WindowManager.LayoutParams.MATCH_PARENT;
    // int flag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    int gravity = Gravity.CENTER;
    String positionGravity = "none";
    boolean enableDrag = false;

    int xPos, yPos;

    public FloatingConfig(
            int height, int width,
            String flagName, String gravity,
            String positionGravity,
            boolean enableDrag,
            int xPos, int yPos) {
        this.height = height;
        this.width = width;
        this.xPos = xPos;
        this.yPos = yPos;
        setGravityFromAlignment(gravity);
        this.positionGravity = positionGravity;
        this.enableDrag = enableDrag;
    }

    void setGravityFromAlignment(String alignment) {
        if (alignment.equalsIgnoreCase("topLeft")) {
            gravity = Gravity.TOP | Gravity.LEFT;
            return;
        }
        if (alignment.equalsIgnoreCase("topCenter")) {
            gravity = Gravity.TOP;
        }
        if (alignment.equalsIgnoreCase("topRight")) {
            gravity = Gravity.TOP | Gravity.RIGHT;
            return;
        }

        if (alignment.equalsIgnoreCase("centerLeft")) {
            gravity = Gravity.CENTER | Gravity.LEFT;
            return;
        }
        if (alignment.equalsIgnoreCase("center")) {
            gravity = Gravity.CENTER;
        }
        if (alignment.equalsIgnoreCase("centerRight")) {
            gravity = Gravity.CENTER | Gravity.RIGHT;
            return;
        }

        if (alignment.equalsIgnoreCase("bottomLeft")) {
            gravity = Gravity.BOTTOM | Gravity.LEFT;
            return;
        }
        if (alignment.equalsIgnoreCase("bottomCenter")) {
            gravity = Gravity.BOTTOM;
        }
        if (alignment.equalsIgnoreCase("bottomRight")) {
            gravity = Gravity.BOTTOM | Gravity.RIGHT;
            return;
        }

    }


    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }


    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public String getPositionGravity() {
        return positionGravity;
    }

    public void setPositionGravity(String positionGravity) {
        this.positionGravity = positionGravity;
    }

    public boolean isEnableDrag() {
        return enableDrag;
    }

    public void setEnableDrag(boolean enableDrag) {
        this.enableDrag = enableDrag;
    }
}
