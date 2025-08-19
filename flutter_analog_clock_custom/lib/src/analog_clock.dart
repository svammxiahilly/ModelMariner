import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_analog_clock/src/hands_painter.dart';

import 'dial_painter.dart';
import 'analog_clock_listener.dart';

/// A analog clock.
class AnalogClock extends StatefulWidget {
  /// If this property is null, then [AnalogClockState._dateTime] value is [DateTime.now()].
  final DateTime? dateTime;

  /// if true, [AnalogClock] keeps time, default value is true.
  final bool isKeepTime;
  final Widget? child;

  /// This property is used to set the color of the watch face.
  final Color? dialColor;

  /// This property is used to set the border color of the watch.
  final Color? dialBorderColor;

  /// sets the border width to the clock radius multiplied by this factor.
  /// Must be less than 1.0 and non-negative.
  final double dialBorderWidthFactor;

  /// This property is used to set the color of the markings on the dial.
  final Color? markingColor;

  /// sets the radius from the center of the dial to the markings multiplied by this factor.
  /// Can be both greater and less than 1.0 but must be non-negative.
  final double markingRadiusFactor;

  /// sets marking width multiplied by this factor.
  /// Can be both greater and less than 1.0 but must be non-negative.
  final double markingWidthFactor;

  /// This property is used to set the text content of the hour markings.
  /// If this list is not empty or null, then its length must be 12.
  final List<String>? hourNumbers;

  /// This property is used to set the color of the hour markings.
  final Color? hourNumberColor;

  /// This property is used to set the color of the hour hand.
  final Color? hourHandColor;

  /// This property is used to set the color of the minute hand.
  final Color? minuteHandColor;

  /// This property is used to set the color of the second hand.
  final Color? secondHandColor;

  /// sets the width of the hour hand multiplied by this factor.
  /// Can be both greater and less than 1.0 but must be non-negative.
  final double hourHandWidthFactor;

  /// sets the width of the minute hand multiplied by this factor.
  /// Can be both greater and less than 1.0 but must be non-negative.
  final double minuteHandWidthFactor;

  /// sets the width of the minute second multiplied by this factor.
  /// Can be both greater and less than 1.0 but must be non-negative.
  final double secondHandWidthFactor;

  /// sets the length of the hour hand multiplied by this factor.
  /// Can be both greater and less than 1.0 but must be non-negative.
  final double hourHandLengthFactor;

  /// sets the length of the minute hand multiplied by this factor.
  /// Can be both greater and less than 1.0 but must be non-negative.
  final double minuteHandLengthFactor;

  /// sets the length of the second hand multiplied by this factor.
  /// Can be both greater and less than 1.0 but must be non-negative.
  final double secondHandLengthFactor;

  /// This property is used to set the font size of the hour markings.
  /// Can be both greater and less than 1.0 but must be non-negative.
  final double hourNumberSizeFactor;

  /// sets the radius from the hour marking to center of dail multiplied by this factor.
  /// Can be both greater and less than 1.0 but must be non-negative.
  final double hourNumberRadiusFactor;

  /// This property is used to set the color of the center of the watch.
  final Color? centerPointColor;

  /// sets the width of the center of the watch multiplied by this factor.
  /// Can be both greater and less than 1.0 but must be non-negative.
  final double centerPointWidthFactor;
  const AnalogClock({
    super.key,
    this.dateTime,
    bool? isKeepTime,
    this.child,
    this.dialColor = Colors.white,
    this.dialBorderColor = Colors.black,
    double? dialBorderWidthFactor,
    this.markingColor = Colors.black,
    double? markingRadiusFactor,
    double? markingWidthFactor,
    this.hourNumbers = const [
      '1',
      '2',
      '3',
      '4',
      '5',
      '6',
      '7',
      '8',
      '9',
      '10',
      '11',
      '12'
    ],
    this.hourNumberColor = Colors.black,
    this.hourHandColor = Colors.black,
    this.minuteHandColor = Colors.black,
    this.secondHandColor = Colors.black,
    double? hourHandWidthFactor,
    double? minuteHandWidthFactor,
    double? secondHandWidthFactor,
    double? hourHandLengthFactor,
    double? minuteHandLengthFactor,
    double? secondHandLengthFactor,
    double? hourNumberSizeFactor,
    double? hourNumberRadiusFactor,
    this.centerPointColor = Colors.black,
    double? centerPointWidthFactor,
  })  : assert(dialBorderWidthFactor == null ||
            (dialBorderWidthFactor >= 0.0 && dialBorderWidthFactor <= 1.0)),
        this.isKeepTime = isKeepTime ?? true,
        this.dialBorderWidthFactor = dialBorderWidthFactor ?? 0.0,
        this.markingRadiusFactor = markingRadiusFactor ?? 1.0,
        this.markingWidthFactor = markingWidthFactor ?? 1.0,
        this.hourHandWidthFactor = hourHandWidthFactor ?? 1.0,
        this.minuteHandWidthFactor = minuteHandWidthFactor ?? 1.0,
        this.secondHandWidthFactor = secondHandWidthFactor ?? 1.0,
        this.hourHandLengthFactor = hourHandLengthFactor ?? 1.0,
        this.minuteHandLengthFactor = minuteHandLengthFactor ?? 1.0,
        this.secondHandLengthFactor = secondHandLengthFactor ?? 1.0,
        this.hourNumberSizeFactor = hourNumberSizeFactor ?? 1.0,
        this.hourNumberRadiusFactor = hourNumberRadiusFactor ?? 1.0,
        this.centerPointWidthFactor = centerPointWidthFactor ?? 1.0;

  const AnalogClock.dark({
    Key? key,
    DateTime? dateTime,
    bool? isKeepTime,
    Widget? child,
    Color? dialColor = Colors.black,
    Color? dialBorderColor = Colors.black,
    double? dialBorderWidthFactor,
    Color? markingColor = Colors.grey,
    double? markingRadiusFactor,
    double? markingWidthFactor,
    List<String>? hourNumbers = const [
      '1',
      '2',
      '3',
      '4',
      '5',
      '6',
      '7',
      '8',
      '9',
      '10',
      '11',
      '12'
    ],
    Color? hourNumberColor = Colors.grey,
    Color? hourHandColor = Colors.grey,
    Color? minuteHandColor = Colors.grey,
    Color? secondHandColor = Colors.grey,
    double? hourHandWidthFactor,
    double? minuteHandWidthFactor,
    double? secondHandWidthFactor,
    double? hourHandLengthFactor,
    double? minuteHandLengthFactor,
    double? secondHandLengthFactor,
    double? hourNumberSizeFactor,
    double? hourNumberRadiusFactor,
    Color? centerPointColor = Colors.grey,
    double? centerPointWidthFactor,
  }) : this(
          key: key,
          dateTime: dateTime,
          isKeepTime: isKeepTime,
          child: child,
          dialColor: dialColor,
          dialBorderColor: dialBorderColor,
          dialBorderWidthFactor: dialBorderWidthFactor,
          markingColor: markingColor,
          markingRadiusFactor: markingRadiusFactor,
          markingWidthFactor: markingWidthFactor,
          hourNumbers: hourNumbers,
          hourNumberColor: hourNumberColor,
          hourHandColor: hourHandColor,
          minuteHandColor: minuteHandColor,
          secondHandColor: secondHandColor,
          hourHandWidthFactor: hourHandWidthFactor,
          minuteHandWidthFactor: minuteHandWidthFactor,
          secondHandWidthFactor: secondHandWidthFactor,
          hourHandLengthFactor: hourHandLengthFactor,
          minuteHandLengthFactor: minuteHandLengthFactor,
          secondHandLengthFactor: secondHandLengthFactor,
          hourNumberSizeFactor: hourNumberSizeFactor,
          hourNumberRadiusFactor: hourNumberRadiusFactor,
          centerPointColor: centerPointColor,
          centerPointWidthFactor: centerPointWidthFactor,
        );

  @override
  State<AnalogClock> createState() => AnalogClockState();
}

class AnalogClockState extends State<AnalogClock> {
  AnalogClockListener _listener = AnalogClockListener();
  Timer? _timer;

  late DateTime _dateTime;
  DateTime get dateTime => _dateTime;
  set dateTime(DateTime value) {
    if (_dateTime != value) {
      _timer?.cancel();
      _dateTime = value;
      setState(() {});
      _startOrStopTimer();
    }
  }

  late bool _isKeepTime;
  bool get isKeepTime => _isKeepTime;
  set isKeepTime(bool value) {
    _isKeepTime = value;
    _startOrStopTimer();
  }

  @override
  void initState() {
    super.initState();
    _dateTime = widget.dateTime ?? DateTime.now();
    _isKeepTime = widget.isKeepTime;
    _startOrStopTimer();
  }

  void _startOrStopTimer() {
    if (_isKeepTime) {
      if (_timer?.isActive != true) {
        _timer = Timer.periodic(Duration(seconds: 1), (Timer timer) {
          _dateTime = _dateTime.add(Duration(seconds: 1));
          if (mounted) {
            setState(() {});
          }
        });
      }
    } else {
      _timer?.cancel();
    }
  }

  @override
  Widget build(BuildContext context) {
    return AspectRatio(
      aspectRatio: 1,
      child: CustomPaint(
        child: widget.child,
        painter: DialPainter(
          listener: _listener,
          dialColor: widget.dialColor,
          dialBorderColor: widget.dialBorderColor,
          dialBorderWidthFactor: widget.dialBorderWidthFactor,
          markingColor: widget.markingColor,
          markingRadiusFactor: widget.markingRadiusFactor,
          markingWidthFactor: widget.markingWidthFactor,
          hourNumbers: widget.hourNumbers,
          hourNumberColor: widget.hourNumberColor,
          hourNumberSizeFactor: widget.hourNumberSizeFactor,
          hourNumberRadiusFactor: widget.hourNumberRadiusFactor,
        ),
        foregroundPainter: HandPainter(
          listener: _listener,
          dateTime: _dateTime,
          hourHandColor: widget.hourHandColor,
          minuteHandColor: widget.minuteHandColor,
          secondHandColor: widget.secondHandColor,
          hourHandWidthFactor: widget.hourHandWidthFactor,
          minuteHandWidthFactor: widget.minuteHandWidthFactor,
          secondHandWidthFactor: widget.secondHandWidthFactor,
          hourHandLengthFactor: widget.hourHandLengthFactor,
          minuteHandLengthFactor: widget.minuteHandLengthFactor,
          secondHandLengthFactor: widget.secondHandLengthFactor,
          centerPointColor: widget.centerPointColor,
          centerPointWidthFactor: widget.centerPointWidthFactor,
        ),
      ),
    );
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
