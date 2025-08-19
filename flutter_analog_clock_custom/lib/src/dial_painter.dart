import 'dart:math' as math;
import 'dart:ui';
import 'package:flutter/material.dart';

import 'analog_clock_listener.dart';
import 'analog_clock_util.dart';

/// Draw the dial and markings
class DialPainter extends CustomPainter {
  static const int hourNumbersLength = 12;
  final AnalogClockListener listener;
  final Color? dialColor;
  final Color? dialBorderColor;
  final double dialBorderWidthFactor;
  final Color? markingColor;
  final double markingRadiusFactor;
  final double markingWidthFactor;
  final List<String>? hourNumbers;
  final Color? hourNumberColor;
  final double hourNumberSizeFactor;
  final double hourNumberRadiusFactor;

  const DialPainter({
    required this.listener,
    this.dialColor,
    this.dialBorderColor,
    required this.dialBorderWidthFactor,
    this.markingColor,
    required this.markingRadiusFactor,
    required this.markingWidthFactor,
    this.hourNumbers,
    this.hourNumberColor,
    required this.hourNumberSizeFactor,
    required this.hourNumberRadiusFactor,
  })  : assert(hourNumbers == null ||
            hourNumbers.length == 0 ||
            hourNumbers.length == 12),
        assert(dialBorderWidthFactor >= 0.0 && dialBorderWidthFactor <= 1.0),
        super(repaint: listener);

  @override
  void paint(Canvas canvas, Size size) {
    double clockRadius = math.min(size.width, size.height) / 2;
    double dialBorderWidth = clockRadius * dialBorderWidthFactor;
    listener.dialRadius = clockRadius - dialBorderWidth;
    // translate to center of clock
    canvas.translate(size.width / 2, size.height / 2);

    // draw dial
    if (dialColor != null && dialColor != Colors.transparent) {
      canvas.drawCircle(
        Offset.zero,
        clockRadius - dialBorderWidth,
        Paint()
          ..isAntiAlias = true
          ..color = dialColor!
          ..style = PaintingStyle.fill,
      );
    }

    // draw border
    if (dialBorderWidth > 0) {
      canvas.drawCircle(
        Offset.zero,
        clockRadius - dialBorderWidth / 2,
        Paint()
          ..isAntiAlias = true
          ..color = dialBorderColor ?? Colors.transparent
          ..style = PaintingStyle.stroke
          ..strokeWidth = dialBorderWidth,
      );
    }
    final hourNumberRadius =
        _drawMarkings(canvas, clockRadius - dialBorderWidth) * 0.85;
    _drawHourNumber(canvas, hourNumberRadius * hourNumberRadiusFactor);
    listener.hourNumberRadius = hourNumberRadius;
  }

  double _drawMarkings(final Canvas canvas, final double radius) {
    final double markingRadius = radius * 0.95;
    if (markingColor == null ||
        markingColor == Colors.transparent ||
        markingRadiusFactor <= 0.0 ||
        markingWidthFactor <= 0.0) {
      listener.markingRadius = markingRadius;
      return radius;
    }
    final double markingRadiusWithFactor = markingRadius * markingRadiusFactor;
    listener.markingRadius = markingRadiusWithFactor;
    double smallMarkingWidth =
        (radius - markingRadius) / 1.5 * markingWidthFactor;
    if (smallMarkingWidth / 2 + markingRadius > radius) {
      smallMarkingWidth = (radius - markingRadius) * 2;
    }
    double bigMarkingWidth = (radius - markingRadius) / 1 * markingWidthFactor;
    if (bigMarkingWidth / 2 + markingRadius > radius) {
      bigMarkingWidth = (radius - markingRadius) * 2;
    }

    final List<Offset> smallMarkings = [];
    final List<Offset> bigMarkings = [];
    for (var i = 0; i < 60; i++) {
      double _angle = i * 6.0;
      if (i % 5 != 0) {
        double x = math.cos(AnalogClockUtil.getRadians(_angle)) *
            markingRadiusWithFactor;
        double y = math.sin(AnalogClockUtil.getRadians(_angle)) *
            markingRadiusWithFactor;
        smallMarkings.add(Offset(x, y));
      } else {
        double x = math.cos(AnalogClockUtil.getRadians(_angle)) *
            markingRadiusWithFactor;
        double y = math.sin(AnalogClockUtil.getRadians(_angle)) *
            markingRadiusWithFactor;
        bigMarkings.add(Offset(x, y));
      }
    }
    Paint smallMarkingPaint = Paint()
      ..isAntiAlias = true
      ..color = this.markingColor!
      ..strokeWidth = smallMarkingWidth
      ..strokeCap = StrokeCap.round;
    canvas.drawPoints(PointMode.points, smallMarkings, smallMarkingPaint);
    Paint bigMarkingPaint = Paint()
      ..isAntiAlias = true
      ..color = this.markingColor!
      ..strokeWidth = bigMarkingWidth
      ..strokeCap = StrokeCap.round;
    canvas.drawPoints(PointMode.points, bigMarkings, bigMarkingPaint);
    return markingRadiusWithFactor - bigMarkingWidth / 2;
  }

  /// draw hour number (1 - 12)
  void _drawHourNumber(final Canvas canvas, final double radius) {
    if (hourNumberColor == null ||
        hourNumberColor == Colors.transparent ||
        hourNumbers == null ||
        hourNumbers!.isEmpty ||
        hourNumberSizeFactor <= 0.0 ||
        hourNumberRadiusFactor <= 0.0) {
      return;
    }
    double fontSize = radius / 4 * hourNumberSizeFactor;
    for (int i = 0; i < hourNumbersLength; i++) {
      int hourNumberIndex = i + 2;
      if (hourNumberIndex >= hourNumbersLength)
        hourNumberIndex -= hourNumbersLength;
      final textPainter = TextPainter(
        textAlign: TextAlign.center,
        textDirection: TextDirection.ltr,
        text: TextSpan(
          text: hourNumbers![hourNumberIndex],
          style: TextStyle(fontSize: fontSize, color: this.hourNumberColor),
        ),
      )..layout();

      double angle = i * 30.0;
      double hourNumberX = math.cos(AnalogClockUtil.getRadians(angle)) * radius;
      double hourNumberY = math.sin(AnalogClockUtil.getRadians(angle)) * radius;
      canvas.save();
      canvas.translate(hourNumberX, hourNumberY);
      textPainter.paint(
          canvas, Offset(-textPainter.width / 2, -textPainter.height / 2));
      canvas.restore();
    }
  }

  @override
  bool shouldRepaint(covariant DialPainter oldDelegate) {
    return oldDelegate.dialColor != dialColor ||
        oldDelegate.dialBorderColor != dialBorderColor ||
        oldDelegate.dialBorderWidthFactor != dialBorderWidthFactor ||
        oldDelegate.markingColor != markingColor ||
        oldDelegate.markingRadiusFactor != markingRadiusFactor ||
        oldDelegate.markingWidthFactor != markingWidthFactor ||
        oldDelegate.hourNumbers != hourNumbers ||
        oldDelegate.hourNumberColor != hourNumberColor ||
        oldDelegate.hourNumberSizeFactor != hourNumberSizeFactor ||
        oldDelegate.hourNumberRadiusFactor != hourNumberRadiusFactor;
  }
}
