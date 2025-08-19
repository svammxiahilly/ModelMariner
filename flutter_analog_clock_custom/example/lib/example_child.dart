import 'package:flutter/material.dart';
import 'package:flutter_analog_clock/flutter_analog_clock.dart';

void main() => runApp(MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('FlutterAnalogClock'),
        ),
        body: Center(
          child: AnalogClock(
            dateTime: DateTime(2022, 10, 24, 10, 12, 07),
            isKeepTime: false,
            child: const Align(
              alignment: FractionalOffset(0.5, 0.75),
              child: Text(
                'GMT-8',
                style: TextStyle(
                    fontSize: 18,
                    color: Colors.blue,
                    fontWeight: FontWeight.bold),
              ),
            ),
          ),
        ),
      ),
    ));
