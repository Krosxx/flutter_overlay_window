import 'dart:async';
import 'dart:developer';

import 'package:flutter/services.dart';
import 'package:flutter_overlay_window/src/overlay_config.dart';

class FlutterOverlayWindow {
  FlutterOverlayWindow._();

  static final _controller = StreamController<dynamic>.broadcast();
  static final _controllerOverlayStatus = StreamController<bool>.broadcast();

  static const _channel = MethodChannel("x-slayer/overlay_channel");
  static const _overlayChannel = MethodChannel("x-slayer/overlay");
  static const _overlayMessageChannel =
  MethodChannel("x-slayer/overlay_messenger", JSONMethodCodec());

  /// Open overLay content
  ///
  /// - Optional arguments:
  /// `height` the overlay height and default is [WindowSize.fullCover]
  /// `width` the overlay width and default is [WindowSize.matchParent]
  /// `alignment` the alignment postion on screen and default is [OverlayAlignment.center]
  /// `OverlayFlag` the overlay flag and default is [OverlayFlag.defaultFlag]
  /// `enableDrag` to enable/disable dragging the overlay over the screen and default is "false"
  /// `positionGravity` the overlay postion after drag and default is [PositionGravity.none]
  static Future<void> showOverlay(String winName, {
    int height = WindowSize.fullCover,
    int width = WindowSize.matchParent,
    int xPos = 0,
    int yPos = 100,
    OverlayAlignment alignment = OverlayAlignment.center,
    OverlayFlag flag = OverlayFlag.defaultFlag,
    bool enableDrag = false,
    PositionGravity positionGravity = PositionGravity.none,
  }) async {
    await _channel.invokeMethod(
      'showOverlay',
      {
        "win_name": winName,
        "height": height,
        "width": width,
        "alignment": alignment.name,
        "flag": flag.name,
        "enableDrag": enableDrag,
        "xPos": xPos,
        "yPos": yPos,
        "positionGravity": positionGravity.name,
      },
    );
  }

  /// Check if overlay permission is granted
  static Future<bool> isPermissionGranted() async {
    try {
      return await _channel.invokeMethod<bool>('checkPermission') ?? false;
    } on PlatformException catch (error) {
      log("$error");
      return Future.value(false);
    }
  }

  /// Request overlay permission
  /// it will open the overlay settings page and return `true` once the permission granted.
  static Future<bool?> requestPermission() async {
    try {
      return await _channel.invokeMethod<bool?>('requestPermission');
    } on PlatformException catch (error) {
      log("Error requestPermession: $error");
      rethrow;
    }
  }

  /// Closes overlay if open
  static Future<bool?> closeOverlay(String winName) async {
    final bool? _res = await _channel.invokeMethod(
        'closeOverlay', {"win_name": winName});
    return _res;
  }

  /// Broadcast [data] to and from overlay app.
  ///
  /// If `true` is returned, it indicates that the [data] was sent. However, this doesn't mean
  /// that the [data] has already reached the listeners of the [overlayListener] stream.
  ///
  /// If `false` is returned, it indicates that the [data] was not sent.
  ///
  /// This method may return `false` when invoked from the overlay while the application is closed.
  ///
  /// Returns `true` if the [data] was sent successfully, otherwise `false`.
  static Future<bool> shareData(dynamic data) async {
    final isSent = await _overlayMessageChannel.invokeMethod('', data);
    return isSent as bool;
  }

  /// Streams message shared between overlay and main app
  static Stream<dynamic> get overlayListener {
    _registerOverlayMessageHandler();
    return _controller.stream;
  }

  /// Overlay status stream.
  ///
  /// Emit `true` when overlay is showing, and `false` when overlay is closed.
  ///
  /// Emit value only once for every state change.
  ///
  /// Doesn't emit a change when the overlay is already showing and [showOverlay] is called,
  /// as in this case the overlay will almost immediately reopen.
  static Stream<bool> get overlayStatusListener {
    _registerOverlayMessageHandler();
    return _controllerOverlayStatus.stream;
  }

  /// Update the overlay flag while the overlay in action
  static Future<bool?> updateFlag(String winName, OverlayFlag flag) async {
    final bool? _res = await _channel.invokeMethod<bool?>('updateFlag',
        {"win_name": winName, 'flag': flag.name});
    return _res;
  }

  /// Update the overlay size in the screen
  static Future<bool?> resizeOverlay(String winName, int width,
      int height) async {
    final bool? _res = await _channel.invokeMethod<bool?>(
      'resizeOverlay',
      {
        "win_name": winName,
        'width': width,
        'height': height,
      },
    );
    return _res;
  }

  /// Check if the current overlay is active
  static Future<bool> isActive(String winName) async {
    final bool? _res = await _channel.invokeMethod<bool?>(
        'isOverlayActive', {"win_name": winName});
    return _res ?? false;
  }

  static void _registerOverlayMessageHandler() {
    _overlayMessageChannel.setMethodCallHandler((call) async {
      switch (call.method) {
        case 'isShowingOverlay':
          _controllerOverlayStatus.add(call.arguments as bool);
          break;
        case 'message':
          _controller.add(call.arguments);
          break;
      }
    });
  }

  /// Dispose overlay stream.
  ///
  /// Once disposed, only a complete restart of the application will re-initialize the listener.
  static Future<dynamic> disposeOverlayListener() {
    return _controller.close();
  }
}
