/// Placement of overlay within the screen.
enum OverlayAlignment {
  topLeft,
  topCenter,
  topRight,
  centerLeft,
  center,
  centerRight,
  bottomLeft,
  bottomCenter,
  bottomRight
}

/// Type of dragging behavior for the overlay.
enum PositionGravity {
  /// The `PositionGravity.none` will allow the overlay to postioned anywhere on the screen.
  none,

  /// The `PositionGravity.right` will allow the overlay to stick on the right side of the screen.
  right,

  /// The `PositionGravity.left` will allow the overlay to stick on the left side of the screen.
  left,

  /// The `PositionGravity.auto` will allow the overlay to stick either on the left or right side of the screen depending on the overlay position.
  auto,
}

enum OverlayFlag {

  NotFocusable,

  NotTouchable,

  NotTouchModal,
}


class WindowSize {
  WindowSize._();

  /// default size when the overlay match the parent size
  /// basically it will take the full screen width and height
  static const int matchParent = -1;

}
