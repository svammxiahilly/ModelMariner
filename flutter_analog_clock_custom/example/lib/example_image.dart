import 'package:flutter/material.dart';
import 'package:flutter_analog_clock/flutter_analog_clock.dart';

void main() => runApp(MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('FlutterAnalogClock'),
        ),
        body: Center(
          child: Container(
            alignment: Alignment.center,
            decoration: const BoxDecoration(
              image: DecorationImage(image: AssetImage('assets/dial02.webp')),
            ),
            child: AnalogClock(
              dateTime: DateTime(2022, 12, 17, 17, 43, 7),
              isKeepTime: false,
              dialColor: null,
              markingColor: null,
              hourNumberColor: null,
              hourHandLengthFactor: 0.8,
            ),
          ),
        ),
      ),
    ));
