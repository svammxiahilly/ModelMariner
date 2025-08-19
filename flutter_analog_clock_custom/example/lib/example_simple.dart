import 'package:flutter/material.dart';
import 'package:flutter_analog_clock/flutter_analog_clock.dart';

void main() => runApp(MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('FlutterAnalogClock'),
        ),
        body: const Center(child: AnalogClock()),
      ),
    ));
