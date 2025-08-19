import 'package:flutter/material.dart';
import 'package:flutter_analog_clock/flutter_analog_clock.dart';

void main() => runApp(MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('FlutterAnalogClock'),
        ),
        body: Center(
          child: AnalogClock(
            dateTime: DateTime.now(),
            isKeepTime: true,
            dialColor: Colors.white,
            dialBorderColor: Colors.black,
            dialBorderWidthFactor: 0.02,
            markingColor: Colors.black,
            markingRadiusFactor: 1.0,
            markingWidthFactor: 1.0,
            hourNumberColor: Colors.black,
            hourNumbers: const [
              '',
              '',
              '3',
              '',
              '',
              '6',
              '',
              '',
              '9',
              '',
              '',
              '12'
            ],
            hourNumberSizeFactor: 1.0,
            hourNumberRadiusFactor: 1.0,
            hourHandColor: Colors.black,
            hourHandWidthFactor: 1.0,
            hourHandLengthFactor: 1.0,
            minuteHandColor: Colors.black,
            minuteHandWidthFactor: 1.0,
            minuteHandLengthFactor: 1.0,
            secondHandColor: Colors.black,
            secondHandWidthFactor: 1.0,
            secondHandLengthFactor: 1.0,
            centerPointColor: Colors.black,
            centerPointWidthFactor: 1.0,
          ),
        ),
      ),
    ));
