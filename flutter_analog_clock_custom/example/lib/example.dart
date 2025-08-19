import 'dart:async';
import 'package:flutter/material.dart';
import 'dart:math' as math;

import 'package:flutter_analog_clock/flutter_analog_clock.dart';

void main() => runApp(const ExampleApp());

class ExampleApp extends StatelessWidget {
  const ExampleApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: ThemeData(
        useMaterial3: true,
      ),
      home: const AnalogClockDemo(),
    );
  }
}

class AnalogClockDemo extends StatefulWidget {
  const AnalogClockDemo({super.key});

  @override
  State<AnalogClockDemo> createState() => _AnalogClockDemoState();
}

class _AnalogClockDemoState extends State<AnalogClockDemo> {
  static const List<String> hourNumberTemplate1 = [
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
  ];
  static const List<String> hourNumberTemplate2 = [
    'I',
    'II',
    'III',
    'IV',
    'V',
    'VI',
    'VII',
    'VIII',
    'IX',
    'X',
    'XI',
    'XII'
  ];
  static const List<String> hourNumberTemplate3 = [
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
  ];
  Color? _dialColor = Colors.white;
  Color? _dialBorderColor = Colors.black;
  double? _dialBorderWidthFactor = 0.1;
  Color? _markingColor = Colors.black;
  double? _markingRadiusFactor;
  double? _markingWidthFactor;
  List<String> _hourNumbers = hourNumberTemplate1;
  Color? _hourNumberColor = Colors.black;
  double? _hourNumberSizeFactor;
  double? _hourNumberRadiusFactor;
  Color? _hourHandColor = Colors.black;
  Color? _minuteHandColor = Colors.black;
  Color? _secondHandColor = Colors.black;
  Color? _centerPointColor = Colors.black;
  double? _hourHandWidthFactor;
  double? _hourHandLengthFactor;
  double? _minuteHandWidthFactor;
  double? _minuteHandLengthFactor;
  double? _secondHandWidthFactor;
  double? _secondHandLengthFactor;
  double? _centerPointWidthFactor;
  final math.Random _random = math.Random();
  final GlobalKey<AnalogClockState> _analogClockKey = GlobalKey();
  static const List<Color> colors = [
    Colors.black,
    Colors.white,
    Colors.red,
    Colors.green,
    Colors.blue,
    Colors.teal,
    Colors.purple,
    Colors.pink,
    Colors.orange,
    Colors.lime,
    Colors.indigo,
    Colors.brown,
    Colors.cyan,
    Colors.yellow,
    Colors.amber,
  ];
  SettingGroup _selectedSettingGroup = SettingGroup.values[0];
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            Container(
              color: Colors.grey.shade300,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: AnalogClock(
                  key: _analogClockKey,
                  dateTime: DateTime.now(),
                  isKeepTime: true,
                  dialColor: _dialColor,
                  dialBorderColor: _dialBorderColor,
                  dialBorderWidthFactor: _dialBorderWidthFactor,
                  markingColor: _markingColor,
                  markingRadiusFactor: _markingRadiusFactor,
                  markingWidthFactor: _markingWidthFactor,
                  hourNumberColor: _hourNumberColor,
                  hourNumbers: _hourNumbers,
                  hourNumberSizeFactor: _hourNumberSizeFactor,
                  hourNumberRadiusFactor: _hourNumberRadiusFactor,
                  hourHandColor: _hourHandColor,
                  hourHandWidthFactor: _hourHandWidthFactor,
                  hourHandLengthFactor: _hourHandLengthFactor,
                  minuteHandColor: _minuteHandColor,
                  minuteHandWidthFactor: _minuteHandWidthFactor,
                  minuteHandLengthFactor: _minuteHandLengthFactor,
                  secondHandColor: _secondHandColor,
                  secondHandWidthFactor: _secondHandWidthFactor,
                  secondHandLengthFactor: _secondHandLengthFactor,
                  centerPointColor: _centerPointColor,
                  centerPointWidthFactor: _centerPointWidthFactor,
                  child: Align(
                    alignment: const FractionalOffset(0.5, 0.75),
                    child: ElevatedButton(
                      onPressed: () => setState(() => _reset()),
                      child: const Text('Reset'),
                    ),
                  ),
                ),
              ),
            ),
            Expanded(
              child: SingleChildScrollView(
                  child: Padding(
                padding: const EdgeInsets.all(8.0),
                child: _buildSettingGroup(),
              )),
            ),
            _buildFooterButtons(),
          ],
        ),
      ),
    );
  }

  void _reset() {
    _analogClockKey.currentState!.dateTime = DateTime.now();
    _analogClockKey.currentState!.isKeepTime = true;
    _dialColor = Colors.white;
    _dialBorderColor = Colors.black;
    _dialBorderWidthFactor = 0.1;
    _markingColor = Colors.black;
    _markingRadiusFactor = null;
    _markingWidthFactor = null;
    _hourNumbers = hourNumberTemplate1;
    _hourNumberColor = Colors.black;
    _hourNumberSizeFactor = null;
    _hourNumberRadiusFactor = null;
    _hourHandColor = Colors.black;
    _minuteHandColor = Colors.black;
    _secondHandColor = Colors.black;
    _centerPointColor = Colors.black;
    _hourHandWidthFactor = null;
    _hourHandLengthFactor = null;
    _minuteHandWidthFactor = null;
    _minuteHandLengthFactor = null;
    _secondHandWidthFactor = null;
    _secondHandLengthFactor = null;
    _centerPointWidthFactor = null;
  }

  Widget _buildFooterButtons() {
    return Container(
      color: Colors.grey.shade300,
      height: 50,
      child: Row(
        children: List.generate(
          SettingGroup.values.length,
          (index) {
            return Expanded(
              child: InkWell(
                onTap: () {
                  setState(() {
                    _selectedSettingGroup = SettingGroup.values[index];
                  });
                },
                child: Center(
                  child: Text(
                    SettingGroup.values[index].name,
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      color: SettingGroup.values[index] == _selectedSettingGroup
                          ? Colors.blueAccent
                          : Colors.black,
                    ),
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _buildSettingGroup() {
    switch (_selectedSettingGroup) {
      case SettingGroup.time:
        return _buildTimeSetting();
      case SettingGroup.dial:
        return _buildDialSetting();
      case SettingGroup.marking:
        return _buildMarkSetting();
      case SettingGroup.number:
        return _buildNumberSetting();
      case SettingGroup.hands:
        return _buildHandSetting();
      default:
        return const Text('Error!');
    }
  }

  Timer? _longPressTimer;
  MaterialStateProperty<Color?>? _hourPlusBackgroundColor;
  MaterialStateProperty<Color?>? _minutePlusBackgroundColor;
  MaterialStateProperty<Color?>? _secondPlusBackgroundColor;
  MaterialStateProperty<Color?>? _hourNegBackgroundColor;
  MaterialStateProperty<Color?>? _minuteNegBackgroundColor;
  MaterialStateProperty<Color?>? _secondNegBackgroundColor;
  Widget _buildTimeSetting() {
    Widget buildTimeSetup(PartColor partColor) {
      MaterialStateProperty<Color?>? plusButtonBackgroundColor;
      MaterialStateProperty<Color?>? negButtonBackgroundColor;
      void switchBackgroundColor(
          MaterialStateProperty<Color?>? value, bool isPlusAction) {
        switch (partColor) {
          case PartColor.hourHand:
            isPlusAction
                ? _hourPlusBackgroundColor = value
                : _hourNegBackgroundColor = value;
            break;
          case PartColor.minuteHand:
            isPlusAction
                ? _minutePlusBackgroundColor = value
                : _minuteNegBackgroundColor = value;
            break;
          case PartColor.secondHand:
            isPlusAction
                ? _secondPlusBackgroundColor = value
                : _secondNegBackgroundColor = value;
            break;
          default:
            break;
        }
      }

      String? label;
      Duration plusDuration = const Duration();
      Duration negDuration = const Duration();
      switch (partColor) {
        case PartColor.hourHand:
          label = 'Hour:';
          plusDuration = const Duration(hours: 1);
          negDuration = const Duration(hours: -1);
          plusButtonBackgroundColor = _hourPlusBackgroundColor;
          negButtonBackgroundColor = _hourNegBackgroundColor;
          break;
        case PartColor.minuteHand:
          label = 'Minute:';
          plusDuration = const Duration(minutes: 1);
          negDuration = const Duration(minutes: -1);
          plusButtonBackgroundColor = _minutePlusBackgroundColor;
          negButtonBackgroundColor = _minuteNegBackgroundColor;
          break;
        case PartColor.secondHand:
          label = 'Second:';
          plusDuration = const Duration(seconds: 1);
          negDuration = const Duration(seconds: -1);
          plusButtonBackgroundColor = _secondPlusBackgroundColor;
          negButtonBackgroundColor = _secondNegBackgroundColor;
          break;
        default:
          break;
      }
      return Padding(
        padding: const EdgeInsets.only(bottom: 8.0),
        child: Row(
          children: [
            Expanded(flex: 1, child: Text(label ?? 'Error!')),
            GestureDetector(
              onLongPressStart: (details) {
                setState(() => switchBackgroundColor(
                    MaterialStateProperty.all(Colors.lightBlue.shade100),
                    true));
                _longPressTimer =
                    Timer.periodic(const Duration(milliseconds: 50), (timer) {
                  _analogClockKey.currentState!.dateTime =
                      _analogClockKey.currentState!.dateTime.add(plusDuration);
                });
              },
              onLongPressEnd: (details) {
                setState(() => switchBackgroundColor(null, true));
                _longPressTimer?.cancel();
              },
              child: ElevatedButton(
                style: ButtonStyle(
                  overlayColor:
                      MaterialStateProperty.all(Colors.lightBlue.shade100),
                  backgroundColor: plusButtonBackgroundColor,
                ),
                onPressed: () => _analogClockKey.currentState!.dateTime =
                    _analogClockKey.currentState!.dateTime.add(plusDuration),
                child: const Icon(Icons.exposure_plus_1),
              ),
            ),
            const SizedBox(
              width: 8,
            ),
            GestureDetector(
              onLongPressStart: (details) {
                setState(() => switchBackgroundColor(
                    MaterialStateProperty.all(Colors.lightBlue.shade100),
                    false));
                _longPressTimer =
                    Timer.periodic(const Duration(milliseconds: 50), (timer) {
                  _analogClockKey.currentState!.dateTime =
                      _analogClockKey.currentState!.dateTime.add(negDuration);
                });
              },
              onLongPressEnd: (details) {
                setState(() {
                  switchBackgroundColor(null, false);
                });
                _longPressTimer?.cancel();
              },
              child: ElevatedButton(
                style: ButtonStyle(
                  overlayColor:
                      MaterialStateProperty.all(Colors.lightBlue.shade100),
                  backgroundColor: negButtonBackgroundColor,
                ),
                onPressed: () => _analogClockKey.currentState!.dateTime =
                    _analogClockKey.currentState!.dateTime.add(negDuration),
                child: const Icon(Icons.exposure_neg_1),
              ),
            ),
            const Expanded(
              flex: 2,
              child: SizedBox(
                width: 1,
              ),
            ),
          ],
        ),
      );
    }

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.only(bottom: 8.0),
          child: Row(
            children: [
              const Expanded(flex: 1, child: Text('Keep time:')),
              ToggleButtons(
                isSelected: _analogClockKey.currentState?.isKeepTime ?? true
                    ? [true, false]
                    : [false, true],
                children: const [
                  Icon(Icons.check_circle_outline),
                  Icon(Icons.block),
                ],
                onPressed: (index) {
                  setState(() {
                    _analogClockKey.currentState?.isKeepTime = index == 0;
                  });
                },
              ),
              const Expanded(
                flex: 2,
                child: SizedBox(
                  width: 1,
                ),
              ),
            ],
          ),
        ),
        buildTimeSetup(PartColor.hourHand),
        buildTimeSetup(PartColor.minuteHand),
        buildTimeSetup(PartColor.secondHand),
      ],
    );
  }

  Widget _buildDialSetting() {
    return Column(
      children: [
        Row(
          children: [
            const Expanded(flex: 1, child: Text('Dial:')),
            Expanded(flex: 4, child: _buildColorPicker(PartColor.dial)),
          ],
        ),
        const SizedBox(
          height: 8,
        ),
        Row(
          children: [
            const Expanded(flex: 1, child: Text('Border:')),
            Expanded(flex: 4, child: _buildColorPicker(PartColor.dialBorder)),
          ],
        ),
        Row(
          children: [
            const Text('Border:'),
            Expanded(
                flex: 1, child: _buildFactorSlider(PartFactor.borderWidth)),
          ],
        ),
      ],
    );
  }

  Widget _buildMarkSetting() {
    return Column(
      children: [
        Row(
          children: [
            const Expanded(flex: 1, child: Text('Marking:')),
            Expanded(flex: 4, child: _buildColorPicker(PartColor.marking)),
          ],
        ),
        const SizedBox(
          height: 8,
        ),
        Row(
          children: [
            const Expanded(flex: 1, child: Text('Radius:')),
            Expanded(
                flex: 5, child: _buildFactorSlider(PartFactor.markingRadius)),
          ],
        ),
        Row(
          children: [
            const Expanded(flex: 1, child: Text('Width:')),
            Expanded(
                flex: 5, child: _buildFactorSlider(PartFactor.markingWidth)),
          ],
        ),
      ],
    );
  }

  Widget _buildNumberSetting() {
    void onHourNumberChanged(List<String>? numbers) {
      if (numbers != null) {
        setState(() {
          _hourNumbers = numbers;
        });
      }
    }

    return Column(
      children: [
        Row(
          children: [
            const Expanded(flex: 1, child: Text('Number:')),
            Expanded(flex: 4, child: _buildColorPicker(PartColor.hourNumber)),
          ],
        ),
        const SizedBox(
          height: 8,
        ),
        Row(
          children: [
            const Expanded(flex: 1, child: Text('Size:')),
            Expanded(flex: 5, child: _buildFactorSlider(PartFactor.numberSize)),
          ],
        ),
        Row(
          children: [
            const Expanded(flex: 1, child: Text('Radius:')),
            Expanded(
                flex: 5, child: _buildFactorSlider(PartFactor.numberRadius)),
          ],
        ),
        Row(
          children: [
            const Expanded(flex: 1, child: Text('Hour\nnumber:')),
            Expanded(
              flex: 4,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  hourNumberTemplate1,
                  hourNumberTemplate2,
                  hourNumberTemplate3
                ].map((element) {
                  return RadioListTile(
                    dense: true,
                    contentPadding: EdgeInsets.zero,
                    value: element,
                    groupValue: _hourNumbers,
                    onChanged: onHourNumberChanged,
                    title: Text(
                      element.toString().replaceAll(' ', ''),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    selected: _hourNumbers == element,
                  );
                }).toList(growable: false),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildHandSetting() {
    return Column(
      children: [
        ...[
          PartColor.hourHand,
          PartColor.minuteHand,
          PartColor.secondHand,
          PartColor.centerPoint
        ].map((element) {
          String label = '';
          if (PartColor.hourHand == element) {
            label = 'Hour:';
          } else if (PartColor.minuteHand == element) {
            label = 'Minute:';
          } else if (PartColor.secondHand == element) {
            label = 'Second:';
          } else if (PartColor.centerPoint == element) {
            label = 'Center:';
          }
          return Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: Row(
              children: [
                Expanded(flex: 1, child: Text(label)),
                Expanded(flex: 4, child: _buildColorPicker(element)),
              ],
            ),
          );
        }).toList(growable: false),
        ...[
          PartFactor.hourWidth,
          PartFactor.hourLength,
          PartFactor.minuteWidth,
          PartFactor.minuteLength,
          PartFactor.secondWidth,
          PartFactor.secondLength,
          PartFactor.centerPointWidth
        ].map((element) {
          String label = '';
          if (PartFactor.hourWidth == element) {
            label = 'Hour width:';
          } else if (PartFactor.hourLength == element) {
            label = 'Hour length:';
          } else if (PartFactor.minuteWidth == element) {
            label = 'Minute width:';
          } else if (PartFactor.minuteLength == element) {
            label = 'Minute length:';
          } else if (PartFactor.secondWidth == element) {
            label = 'Second width:';
          } else if (PartFactor.secondLength == element) {
            label = 'Second length:';
          } else if (PartFactor.centerPointWidth == element) {
            label = 'Center point:';
          }
          return Row(
            children: [
              Expanded(flex: 3, child: Text(label)),
              Expanded(flex: 7, child: _buildFactorSlider(element)),
            ],
          );
        }).toList(growable: false),
      ],
    );
  }

  Widget _buildColorPicker(PartColor partColor) {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Row(
        children: List.generate(
          colors.length,
          growable: false,
          (index) {
            return InkWell(
              onTap: () {
                setState(() {
                  if (PartColor.dial == partColor) {
                    _dialColor = colors[index];
                  } else if (PartColor.dialBorder == partColor) {
                    _dialBorderColor = colors[index];
                  } else if (PartColor.marking == partColor) {
                    _markingColor = colors[index];
                  } else if (PartColor.hourNumber == partColor) {
                    _hourNumberColor = colors[index];
                  } else if (PartColor.hourHand == partColor) {
                    _hourHandColor = colors[index];
                  } else if (PartColor.minuteHand == partColor) {
                    _minuteHandColor = colors[index];
                  } else if (PartColor.secondHand == partColor) {
                    _secondHandColor = colors[index];
                  } else if (PartColor.centerPoint == partColor) {
                    _centerPointColor = colors[index];
                  }
                });
              },
              child: Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: colors[index],
                ),
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _buildFactorSlider(PartFactor partFactor) {
    double? value;
    double maxValue = 2.0;
    double defaultValue = 1.0;
    void switchValue(bool isChanged) {
      switch (partFactor) {
        case PartFactor.borderWidth:
          if (isChanged) {
            _dialBorderWidthFactor = value;
          } else {
            value = _dialBorderWidthFactor;
            maxValue = 0.5;
            defaultValue = 0.0;
          }
          break;
        case PartFactor.markingRadius:
          isChanged
              ? _markingRadiusFactor = value
              : value = _markingRadiusFactor;
          break;
        case PartFactor.markingWidth:
          isChanged ? _markingWidthFactor = value : value = _markingWidthFactor;
          break;
        case PartFactor.numberSize:
          isChanged
              ? _hourNumberSizeFactor = value
              : value = _hourNumberSizeFactor;
          break;
        case PartFactor.numberRadius:
          isChanged
              ? _hourNumberRadiusFactor = value
              : value = _hourNumberRadiusFactor;
          break;
        case PartFactor.hourWidth:
          isChanged
              ? _hourHandWidthFactor = value
              : value = _hourHandWidthFactor;
          break;
        case PartFactor.minuteWidth:
          isChanged
              ? _minuteHandWidthFactor = value
              : value = _minuteHandWidthFactor;
          break;
        case PartFactor.secondWidth:
          isChanged
              ? _secondHandWidthFactor = value
              : value = _secondHandWidthFactor;
          break;
        case PartFactor.hourLength:
          isChanged
              ? _hourHandLengthFactor = value
              : value = _hourHandLengthFactor;
          break;
        case PartFactor.minuteLength:
          isChanged
              ? _minuteHandLengthFactor = value
              : value = _minuteHandLengthFactor;
          break;
        case PartFactor.secondLength:
          isChanged
              ? _secondHandLengthFactor = value
              : value = _secondHandLengthFactor;
          break;
        case PartFactor.centerPointWidth:
          isChanged
              ? _centerPointWidthFactor = value
              : value = _centerPointWidthFactor;
          break;
        default:
          break;
      }
    }

    switchValue(false);
    return Slider(
      value: value ?? defaultValue,
      min: 0,
      max: maxValue,
      divisions: 100,
      label: (value ?? defaultValue).toString(),
      onChanged: (newValue) {
        setState(() {
          value = newValue;
          switchValue(true);
        });
      },
    );
  }

  Color getRandomColor() => Color.fromARGB(
      255, _random.nextInt(256), _random.nextInt(256), _random.nextInt(256));
}

enum SettingGroup {
  time('Time'),
  dial('Dial'),
  marking('Marking'),
  number('Number'),
  hands('Hands');

  final String name;
  const SettingGroup(this.name);
}

enum PartColor {
  dial,
  dialBorder,
  marking,
  hourNumber,
  hourHand,
  minuteHand,
  secondHand,
  centerPoint,
}

enum PartFactor {
  borderWidth,
  markingRadius,
  markingWidth,
  numberSize,
  numberRadius,
  hourWidth,
  minuteWidth,
  secondWidth,
  hourLength,
  minuteLength,
  secondLength,
  centerPointWidth,
}
