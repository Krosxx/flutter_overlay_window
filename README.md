# flutter_overlay_window

Flutter plugin for displaying your flutter views over other apps on the screen

<!-- ## Preview -->


## Installation

Add package to your pubspec:

```yaml
dependencies:
  flutter_overlay_window:
    git:
      url: https://github.com/Krosxx/flutter_overlay_window.git
      ref: main
```


### Entry point

Inside `main.dart` create an entry point for your Overlay widget;

```dart

// overlay entry point
@pragma("vm:entry-point")
void overlayMain() {
  runApp(const MaterialApp(
    debugShowCheckedModeBanner: false,
    home: Material(child: Text("My overlay"))
  ));
}

// overlay entry point
@pragma("vm:entry-point")
void overlayView2() {
  runApp(const MaterialApp(
    debugShowCheckedModeBanner: false,
    home: Material(child: Text("My overlay window2."))
  ));
}

```

### USAGE

```dart
 /// check if overlay permission is granted
 final bool status = await FlutterOverlayWindow.isPermissionGranted();

 /// request overlay permission
 /// it will open the overlay settings page and return `true` once the permission granted.
 final bool status = await FlutterOverlayWindow.requestPermission();

 /// Open overLay content
 
await FlutterOverlayWindow.showOverlay(
  "overlayMain",
  height: 70,
  width: 70,
  xPos: -1,
  yPos: 100,
  alignment: OverlayAlignment.topLeft,
  enableDrag: true,
  positionGravity: PositionGravity.auto,
);

/// show float window 2
await FlutterOverlayWindow.showOverlay("overlayView2");

 /// closes overlay if open
 await FlutterOverlayWindow.closeOverlay("overlayMain");

 /// broadcast data to app and otther overlays.
 await FlutterOverlayWindow.shareData("Hello from the other side");

 /// streams message shared between overlays and main app
 // drag touch event and animation end event
  FlutterOverlayWindow.overlayListener.listen((event) {
      log("Current Event: $event");
    });

 /// use [OverlayFlag.focusPointer] when you want to use fields that show keyboards
 await FlutterOverlayWindow.showOverlay(flag: OverlayFlag.focusPointer);


 /// update the overlay flag while the overlay in action
 await FlutterOverlayWindow.updateFlag(OverlayFlag.defaultFlag);

 /// Update the overlay size in the screen
 await FlutterOverlayWindow.resizeOverlay("overlayMain",80, 120);

```

```dart

enum OverlayFlag {
  /// Window flag: this window can never receive touch events.
  /// Usefull if you want to display click-through overlay
  clickThrough,

  /// Window flag: this window won't ever get key input focus
  /// so the user can not send key or other button events to it.
  defaultFlag,

  /// Window flag: allow any pointer events outside of the window to be sent to the windows behind it.
  /// Usefull when you want to use fields that show keyboards.
  focusPointer,
}

```

```dart

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


```
