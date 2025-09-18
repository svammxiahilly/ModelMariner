import 'package:flutter/material.dart';
import 'dart:async';
import '../widgets/wave_circularprogress.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import 'package:clockmaster/helpers/icon_helper.dart';
import '../helpers/preferences_helper.dart';

import 'package:flutter_background_service/flutter_background_service.dart';

enum TimerMode { pomodoro, shortBreak, longBreak }

class PomodoroScreen extends StatefulWidget {
  const PomodoroScreen({super.key});

  @override
  State<PomodoroScreen> createState() => _PomodoroScreenState();
}

class _PomodoroScreenState extends State<PomodoroScreen> {
  int pomodoroMinutes = PreferencesHelper.getInt("pomoFocusTime") ?? 25;

  int shortBreakMinutes = PreferencesHelper.getInt("pomoShortBreakTime") ?? 5;
  int longBreakMinutes = PreferencesHelper.getInt("pomoLongBreakTime") ?? 15;

  int cyclesBeforeLongBreak =
      PreferencesHelper.getInt("pomoCyclesBeforeLongBreak") ?? 4;
  int completedPomodoros = 0;

  TimerMode currentMode = TimerMode.pomodoro;
  Timer? _timer;
  bool isRunning = false;

  late int totalSeconds;
  late int remainingSeconds;

  bool autoStartNext =
      PreferencesHelper.getBool("autoStartSessionpomo") ?? false;

  Future<bool> isServiceRunning() async {
    final service = FlutterBackgroundService();
    final running = await service.isRunning();
    return running;
  }

  @override
  void initState() {
    super.initState();
    _updateServiceState();
    FlutterBackgroundService().on("update").listen((event) {
      if (event != null && mounted) {
        setState(() {
          if (event.containsKey('total')) {
            totalSeconds = event['total'] as int;
          }

          if (event.containsKey('remaining')) {
            remainingSeconds = event['remaining'] as int;
          }

          if (event.containsKey('isRunning')) {
            isRunning = event['isRunning'] as bool;
          }

          if (event['reset'] == true) {
            remainingSeconds = totalSeconds;
            isRunning = false;
          }
        });

        if ((event['finished'] as bool?) == true) {
          _onSessionComplete();
        }
      }
    });

    _setMode(currentMode, reset: true);
  }

  Future<void> _updateServiceState() async {
    final running = await isServiceRunning();
    if (mounted) {
      setState(() {
        isRunning = running;
      });
    }
  }

  void _setMode(TimerMode mode, {bool reset = false}) {
    currentMode = mode;
    totalSeconds = _minutesForMode(mode) * 60;
    if (reset) {
      remainingSeconds = totalSeconds;
      isRunning = false;
      _cancelTimer();
      _service.invoke("resetTimer");
    } else {
      remainingSeconds = totalSeconds;
    }
    setState(() {});
  }

  int _minutesForMode(TimerMode mode) {
    switch (mode) {
      case TimerMode.pomodoro:
        return pomodoroMinutes;
      case TimerMode.shortBreak:
        return shortBreakMinutes;
      case TimerMode.longBreak:
        return longBreakMinutes;
    }
  }

  void _cancelTimer() {}

  final _service = FlutterBackgroundService();

  void _start() async {
    if (isRunning) return;
    final duration = _minutesForMode(currentMode) * 60;

    final started = await _service.isRunning();
    if (!started) {
      await _service.startService();
      await Future.delayed(const Duration(milliseconds: 500));
    }

    _service.invoke("startTimer", {"duration": duration});

    isRunning = true;
    setState(() {});
  }

  void _pause() {
    _service.invoke("pauseTimer");
    isRunning = false;
    setState(() {});
  }

  void _reset({bool keepMode = true}) async {
    isRunning = false;
    totalSeconds = _minutesForMode(currentMode) * 60;
    remainingSeconds = totalSeconds;
    setState(() {});
    _service.invoke("resetTimer");
  }

  void _onSessionComplete() {
    if (currentMode == TimerMode.pomodoro) {
      completedPomodoros += 1;
      if (completedPomodoros % cyclesBeforeLongBreak == 0) {
        _setMode(TimerMode.longBreak, reset: true);
      } else {
        _setMode(TimerMode.shortBreak, reset: true);
      }
    } else {
      _setMode(TimerMode.pomodoro, reset: true);
    }

    if (autoStartNext) {
      Future.delayed(const Duration(milliseconds: 300), () {
        if (mounted) _start();
      });
      isRunning = true;
    } else {
      isRunning = false;
    }

    setState(() {});
  }

  String _formatTime(int seconds) {
    final m = (seconds ~/ 60).toString().padLeft(2, '0');
    final s = (seconds % 60).toString().padLeft(2, '0');
    return '$m:$s';
  }

  double get _progress {
    if (totalSeconds == 0) return 0.0;
    return 1.0 - (remainingSeconds / totalSeconds);
  }

  void _changeMinutes(TimerMode mode, int delta) {
    setState(() {
      switch (mode) {
        case TimerMode.pomodoro:
          pomodoroMinutes = (pomodoroMinutes + delta).clamp(1, 180);
          break;
        case TimerMode.shortBreak:
          shortBreakMinutes = (shortBreakMinutes + delta).clamp(1, 60);
          break;
        case TimerMode.longBreak:
          longBreakMinutes = (longBreakMinutes + delta).clamp(1, 240);
          break;
      }
      if (currentMode == mode) {
        _reset(keepMode: true);
      }
    });
  }

  String _modeName(TimerMode mode) {
    switch (mode) {
      case TimerMode.pomodoro:
        return 'Focus';
      case TimerMode.shortBreak:
        return 'Short Break';
      case TimerMode.longBreak:
        return 'Long Break';
    }
  }

  @override
  void dispose() {
    _cancelTimer();
    super.dispose();
  }

  TimerMode get _nextMode {
    if (currentMode == TimerMode.pomodoro) {
      if ((completedPomodoros + 1) % cyclesBeforeLongBreak == 0) {
        return TimerMode.longBreak;
      } else {
        return TimerMode.shortBreak;
      }
    } else {
      return TimerMode.pomodoro;
    }
  }

  @override
  Widget build(BuildContext context) {
    final minutes = _minutesForMode(currentMode);
    final colorTheme = Theme.of(context).colorScheme;
    final isLight = Theme.of(context).brightness == Brightness.light;

    Color _modeColor(currentMode) {
      switch (currentMode) {
        case TimerMode.pomodoro:
          return isLight ? Color(0xfffceae5) : Color(0xff271d1b);
        case TimerMode.shortBreak:
          return isLight ? Color(0xffeeefe3) : Color(0xff1e201a);
        case TimerMode.longBreak:
          return isLight ? Color(0xffededf4) : Color(0xff1d2024);
        default:
          return Colors.blue;
      }
    }

    return Scaffold(
      backgroundColor: _modeColor(currentMode),
      appBar: AppBar(
        backgroundColor: _modeColor(currentMode),
        title: Text(
          _modeName(currentMode),
          style: TextStyle(
            color: colorTheme.secondary,
            fontWeight: FontWeight.w900,
            fontSize: 24,
          ),
        ),
        toolbarHeight: 65,
        centerTitle: true,
        titleSpacing: 0,

        actions: [
          IconButton(
            tooltip: 'Reset timer',
            onPressed: () => _setMode(TimerMode.pomodoro, reset: true),
            icon: Icon(Icons.refresh, color: colorTheme.onSurface),
          ),
          SizedBox(width: 5),
        ],
        bottom: PreferredSize(
          preferredSize: Size(100, 20),
          child: Container(
            padding: const EdgeInsets.only(
              left: 10,
              right: 10,
              top: 3,
              bottom: 3,
            ),

            decoration: BoxDecoration(
              color: colorTheme.tertiary,
              borderRadius: BorderRadius.circular(50),
            ),
            child: Text(
              'Next • ${_modeName(_nextMode)}',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: colorTheme.onTertiary,
              ),
            ),
          ),
        ),
      ),

      body: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 18),
        child: Column(
          children: [
            const SizedBox(height: 20),

            Expanded(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  SizedBox(
                    width: MediaQuery.of(context).size.width,
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        SizedBox(
                          width: MediaQuery.of(context).size.width / 1.08,
                          height: MediaQuery.of(context).size.width / 1.08,
                          child: WavyCircularProgress(progress: _progress),
                        ),
                        LayoutBuilder(
                          builder: (context, constraints) {
                            final circleSize =
                                constraints.biggest.shortestSide / 1.1;
                            final text = _formatTime(remainingSeconds);

                            final duration = Duration(
                              seconds: remainingSeconds,
                            );

                            final factor = duration.inHours >= 1 ? 0.7 : 0.75;

                            final fontSize =
                                circleSize / (text.length * factor);

                            return Text(
                              text,
                              style: TextStyle(
                                fontFamily: "FlexFontEn",
                                fontSize: fontSize,
                                fontWeight: FontWeight.w700,

                                color: colorTheme.onSurface,
                              ),
                              textAlign: TextAlign.center,
                              textHeightBehavior: TextHeightBehavior(
                                applyHeightToFirstAscent: false,
                                applyHeightToLastDescent: false,
                              ),
                            );
                          },
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),
                ],
              ),
            ),

            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              spacing: 10,
              children: [
                GestureDetector(
                  onTap: isRunning ? _pause : _start,
                  child: TweenAnimationBuilder<double>(
                    tween: Tween<double>(end: isRunning ? 28.0 : 50.0),
                    duration: const Duration(milliseconds: 300),
                    curve: Curves.easeOutBack,
                    builder: (context, borderRadiusValue, child) {
                      return Container(
                        width: 96,
                        height: 96,
                        decoration: BoxDecoration(
                          color: isRunning
                              ? colorTheme.surfaceContainerHighest
                              : colorTheme.primary,
                          borderRadius: BorderRadius.circular(
                            borderRadiusValue,
                          ),
                        ),
                        child: child,
                      );
                    },
                    child: Material(
                      color: Colors.transparent,
                      child: Center(
                        child: IconWithWeight(
                          isRunning ? Symbols.pause : Symbols.play_arrow,
                          color: isRunning
                              ? colorTheme.primary
                              : colorTheme.onPrimary,
                          size: 40,
                        ),
                      ),
                    ),
                  ),
                ),

                FloatingActionButton.large(
                  shape: const CircleBorder(),
                  onPressed: _onSessionComplete,
                  backgroundColor: colorTheme.primaryContainer,
                  heroTag: "idk_cool_random_tag_pomo",

                  elevation: 0,
                  highlightElevation: 0,
                  child: IconWithWeight(
                    Symbols.fast_forward,
                    size: 40,
                    color: colorTheme.onPrimaryContainer,
                  ),
                ),
              ],
            ),

            SizedBox(height: 10),
            Container(
              width: 96 + 96,
              height: 70,
              clipBehavior: Clip.hardEdge,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(50),
              ),
              child: FloatingActionButton.extended(
                onPressed: () => _reset(keepMode: true),
                backgroundColor: colorTheme.tertiaryContainer,
                heroTag: "idk_random_tag_stop_wow_pomo",

                label: Text(
                  "Reset",
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.w500,
                    color: colorTheme.onTertiaryContainer,
                  ),
                ),
              ),
            ),

            SizedBox(height: 16),

            // Text(
            //   'Completed pomodoros: $completedPomodoros',
            //   style: const TextStyle(fontSize: 12, color: Colors.black54),
            // ),
            const SizedBox(height: 8),
          ],
        ),
        // ],
        // ),
      ),
    );
  }
}
