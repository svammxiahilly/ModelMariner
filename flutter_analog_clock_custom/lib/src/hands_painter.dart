import 'dart:ui';

import 'package:flutter/material.dart';
import 'dart:math' as math;

import 'analog_clock_listener.dart';
import 'analog_clock_util.dart';

/// Draw hands and center point
class HandPainter extends CustomPainter {
  final AnalogClockListener listener;
  final DateTime dateTime;
  final Color? hourHandColor;
  final Color? minuteHandColor;
  final Color? secondHandColor;
  final Color? centerPointColor;
  final double hourHandWidthFactor;
  final double minuteHandWidthFactor;
  final double secondHandWidthFactor;
  final double hourHandLengthFactor;
  final double minuteHandLengthFactor;
  final double secondHandLengthFactor;
  final double centerPointWidthFactor;
  const HandPainter({
    required this.listener,
    required this.dateTime,
    this.hourHandColor,
    this.minuteHandColor,
    this.secondHandColor,
    this.centerPointColor,
    required this.hourHandWidthFactor,
    required this.minuteHandWidthFactor,
    required this.secondHandWidthFactor,
    required this.hourHandLengthFactor,
    required this.minuteHandLengthFactor,
    required this.secondHandLengthFactor,
    required this.centerPointWidthFactor,
  }) : super(repaint: listener);

  @override
  void paint(Canvas canvas, Size size) {
    double hourWidth = 0, minuteWidth = 0, secondWidth = 0;

    // translate to center of clock
    canvas.translate(size.width / 2, size.height / 2);

    if (hourHandLengthFactor > 0.0 &&
        hourHandWidthFactor > 0.0 &&
        hourHandColor != null &&
        hourHandColor != Colors.transparent) {
      double baseHourLength = listener.hourNumberRadius * 0.75;
      hourWidth = baseHourLength * 0.09 * hourHandWidthFactor;
      _drawHourHand(canvas, baseHourLength * hourHandLengthFactor, hourWidth);
    }
    if (minuteHandLengthFactor > 0.0 &&
        minuteHandWidthFactor > 0.0 &&
        minuteHandColor != null &&
        minuteHandColor != Colors.transparent) {
      minuteWidth = listener.hourNumberRadius * 0.03 * minuteHandWidthFactor;
      _drawMinuteHand(
        canvas,
        listener.hourNumberRadius * minuteHandLengthFactor,
        minuteWidth,
      );
    }
    if (secondHandLengthFactor > 0.0 &&
        secondHandWidthFactor > 0.0 &&
        secondHandColor != null &&
        secondHandColor != Colors.transparent) {
      secondWidth = listener.markingRadius * 0.01 * secondHandWidthFactor;
      _drawSecondHand(
        canvas,
        listener.markingRadius * secondHandLengthFactor,
        secondWidth,
      );
    }

    //draw center point
    if (centerPointColor != null &&
        centerPointColor != Colors.transparent &&
        centerPointWidthFactor > 0.0) {
      Paint centerPointPaint = Paint()
        ..strokeWidth =
            math.max(hourWidth, math.max(minuteWidth, secondWidth)) *
                2.0 *
                centerPointWidthFactor
        ..strokeCap = StrokeCap.round
        ..color = this.centerPointColor!;
      canvas.drawPoints(PointMode.points, [Offset.zero], centerPointPaint);
    }
  }

  void _drawHourHand(Canvas canvas, double radius, double strokeWidth) {
    double angle = (dateTime.hour % 12 + dateTime.minute / 60.0) * 30 - 90;

    final paint = Paint()
      ..isAntiAlias = true
      ..color = this.hourHandColor ?? Colors.transparent
      ..style = PaintingStyle.fill;

    final handRect = RRect.fromRectAndRadius(
      Rect.fromLTWH(0, -strokeWidth / 2, radius, strokeWidth),
      Radius.circular(strokeWidth / 2),
    );

    canvas.save();
    canvas.rotate(AnalogClockUtil.getRadians(angle));
    canvas.drawRRect(handRect, paint);
    canvas.restore();
  }

  /// draw minute hand
  void _drawMinuteHand(Canvas canvas, double radius, double strokeWidth) {
    double angle = dateTime.minute * 6 - 90; // correct angle in degrees

    final paint = Paint()
      ..isAntiAlias = true
      ..color = this.minuteHandColor ?? Colors.transparent
      ..style = PaintingStyle.fill;

    final handRect = RRect.fromRectAndRadius(
      Rect.fromLTWH(0, -strokeWidth / 2, radius, strokeWidth),
      Radius.circular(strokeWidth / 2), // rounded corners
    );

    canvas.save();
    canvas.rotate(AnalogClockUtil.getRadians(angle));
    canvas.drawRRect(handRect, paint);
    canvas.restore();
  }

  /// draw second hand
  void _drawSecondHand(Canvas canvas, double radius, double strokeWidth) {
    double angle = dateTime.second * 6 - 90; // correct angle in degrees

    final paint = Paint()
      ..isAntiAlias = true
      ..color = this.secondHandColor ?? Colors.transparent
      ..style = PaintingStyle.fill;

    final handRect = RRect.fromRectAndRadius(
      Rect.fromLTWH(0, -strokeWidth / 2, radius, strokeWidth),
      Radius.circular(strokeWidth / 2), // rounded corners
    );

    canvas.save();
    canvas.rotate(AnalogClockUtil.getRadians(angle));
    canvas.drawRRect(handRect, paint);
    canvas.restore();
  }

  @override
  bool shouldRepaint(covariant HandPainter oldDelegate) {
    return oldDelegate.dateTime != dateTime ||
        oldDelegate.hourHandColor != hourHandColor ||
        oldDelegate.minuteHandColor != minuteHandColor ||
        oldDelegate.secondHandColor != secondHandColor ||
        oldDelegate.centerPointColor != centerPointColor ||
        oldDelegate.hourHandWidthFactor != hourHandWidthFactor ||
        oldDelegate.minuteHandWidthFactor != minuteHandWidthFactor ||
        oldDelegate.secondHandWidthFactor != secondHandWidthFactor ||
        oldDelegate.hourHandLengthFactor != hourHandLengthFactor ||
        oldDelegate.minuteHandLengthFactor != minuteHandLengthFactor ||
        oldDelegate.secondHandLengthFactor != secondHandLengthFactor ||
        oldDelegate.centerPointWidthFactor != centerPointWidthFactor;
  }
}
