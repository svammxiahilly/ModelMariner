import 'dart:async';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import 'package:clockmaster/helpers/icon_helper.dart';
import 'package:flutter/material.dart';
import 'package:flutter_foreground_task/flutter_foreground_task.dart';

@pragma('vm:entry-point')
void startCallback() {
  FlutterForegroundTask.setTaskHandler(StopwatchTaskHandler());
}

bool firstStart = true;

class StopwatchTaskHandler extends TaskHandler {
  Stopwatch stopwatch = Stopwatch();
  Timer? timer;

  @override
  Future<void> onStart(DateTime timestamp, TaskStarter starter) async {
    if (!firstStart) {
      stopwatch.start();
    } else {
      stopwatch.reset();
    }

    timer?.cancel();

    // timer = Timer.periodic(const Duration(milliseconds: 100), (timer) {
    // });
  }

  @override
  void onRepeatEvent(DateTime timestamp) {
    FlutterForegroundTask.sendDataToMain({
      'elapsed': stopwatch.elapsedMilliseconds,
    });

    FlutterForegroundTask.updateService(
      notificationTitle: _formatTime(stopwatch.elapsedMilliseconds),
      notificationText: 'Stopwatch',
      notificationButtons: [
        NotificationButton(
          id: stopwatch.isRunning ? 'pause' : 'start',
          text: stopwatch.isRunning ? 'Pause' : 'Start',
        ),
        const NotificationButton(id: 'reset', text: 'Reset'),
      ],
    );
  }

  @override
  Future<void> onDestroy(DateTime timestamp, bool isTimeout) async {
    timer?.cancel();
  }

  void _sendState() {
    FlutterForegroundTask.sendDataToMain({
      'elapsed': stopwatch.elapsedMilliseconds,
      'isRunning': stopwatch.isRunning,
    });
  }

  @override
  void onReceiveData(Object data) {
    if (data is Map<String, dynamic>) {
      final action = data['action'] as String?;
      if (action == 'start') {
        stopwatch.start();
      } else if (action == 'pause') {
        stopwatch.stop();
      } else if (action == 'reset') {
        stopwatch.reset();
      }
      _sendState();
    }
  }

  @override
  void onNotificationButtonPressed(String id) async {
    if (id == 'pause') {
      stopwatch.stop();
      FlutterForegroundTask.sendDataToMain("stopTimer");
    } else if (id == 'start') {
      stopwatch.start();
      FlutterForegroundTask.sendDataToMain("startTimer");
    } else if (id == 'reset') {
      stopwatch.reset();
      _stopService();
    }
    _sendState();
  }

  @override
  void onNotificationPressed() {}

  @override
  void onNotificationDismissed() {}

  Future<void> _stopService() async {
    await FlutterForegroundTask.stopService();
    firstStart = true;
    FlutterForegroundTask.sendDataToMain("resetFromNotification");
  }
}

String _formatTime(int ms) {
  final seconds = ((ms / 1000) % 60).floor();
  final minutes = ((ms / 60000) % 60).floor();
  final hours = (ms / 3600000).floor();

  final hoursStr = hours > 0 ? '${hours.toString().padLeft(2, '0')}:' : '';
  final minutesStr = minutes.toString().padLeft(2, '0');
  final secondsStr = seconds.toString().padLeft(2, '0');

  return '$hoursStr$minutesStr:$secondsStr';
}

class StopWatchScreen extends StatefulWidget {
  const StopWatchScreen({super.key});

  @override
  State<StopWatchScreen> createState() => _StopWatchScreenState();
}

class _StopWatchScreenState extends State<StopWatchScreen> {
  int _elapsed = 0;
  bool _isRunning = false;
  List<String> _laps = [];

  @override
  void initState() {
    super.initState();

    FlutterForegroundTask.addTaskDataCallback(_onReceiveTaskData);

    WidgetsBinding.instance.addPostFrameCallback((_) {
      _initForegroundTask();
    });
  }

  @override
  void dispose() {
    FlutterForegroundTask.removeTaskDataCallback(_onReceiveTaskData);
    super.dispose();
  }

  void _onReceiveTaskData(Object data) {
    if (data is Map<String, dynamic>) {
      final int? elapsed = data['elapsed'] as int?;
      final bool? isRunning = data['isRunning'] as bool?;
      if (elapsed != null) {
        setState(() {
          _elapsed = elapsed;
          if (isRunning != null) _isRunning = isRunning;
        });
      }
    } else if (data is String) {
      if (data == "resetFromNotification") {
        setState(() {
          _elapsed = 0;
          _isRunning = false;
        });
        final count = _laps.length;
        for (var i = count - 1; i >= 0; i--) {
          final removedItem = _laps.removeAt(i);
          _listKey.currentState?.removeItem(
            i,
            (context, animation) => FadeTransition(
              opacity: animation,
              child: ListTile(
                leading: Text('Lap ${i + 1}'),
                trailing: Text(removedItem),
              ),
            ),
            duration: Duration(milliseconds: 200),
          );
        }
      }
    }
  }

  void _initForegroundTask() {
    FlutterForegroundTask.init(
      androidNotificationOptions: AndroidNotificationOptions(
        channelId: 'stopwatch_channel',
        channelName: 'Stopwatch Service',
        channelDescription: 'Stopwatch is running',
        channelImportance: NotificationChannelImportance.LOW,
        priority: NotificationPriority.LOW,
        onlyAlertOnce: true,
      ),
      iosNotificationOptions: const IOSNotificationOptions(
        showNotification: true,
        playSound: false,
      ),
      foregroundTaskOptions: ForegroundTaskOptions(
        allowWakeLock: true,
        allowWifiLock: true,
        autoRunOnBoot: true,
        eventAction: ForegroundTaskEventAction.repeat(1000),
      ),
    );
  }

  Future<void> _startService() async {
    if (await FlutterForegroundTask.isRunningService) {
      return;
    }
    _initForegroundTask();
    await FlutterForegroundTask.startService(
      serviceId: 999,
      notificationTitle: _formatTime(_elapsed),
      notificationText: 'Stopwatch',
      notificationIcon: const NotificationIcon(
        metaDataName: "com.pranshulgg.service.stopwatch_icon",
      ),

      notificationButtons: [
        NotificationButton(
          id: _isRunning ? 'pause' : 'start',
          text: _isRunning ? 'Pause' : 'Start',
        ),
        const NotificationButton(id: 'reset', text: 'Reset'),
      ],
      notificationInitialRoute: '/',

      callback: startCallback,
    );
  }

  Future<void> _stopService() async {
    await FlutterForegroundTask.stopService();
  }

  void _sendAction(String action) {
    FlutterForegroundTask.sendDataToTask({'action': action});
  }

  void _startPause() async {
    if (!_isRunning) {
      firstStart = false;
      if (!await FlutterForegroundTask.isRunningService) {
        await _startService();
      }
      _sendAction('start');
    } else {
      _sendAction('pause');
    }
    setState(() {
      _isRunning = !_isRunning;
    });
  }

  void _reset() async {
    _sendAction('reset');
    await _stopService();
    setState(() {
      _isRunning = false;
      _elapsed = 0;
    });
    final count = _laps.length;
    for (var i = count - 1; i >= 0; i--) {
      final removedItem = _laps.removeAt(i);
      _listKey.currentState?.removeItem(
        i,
        (context, animation) => FadeTransition(
          opacity: animation,
          child: ListTile(
            leading: Text('Lap ${i + 1}'),
            trailing: Text(removedItem),
          ),
        ),
        duration: Duration(milliseconds: 200),
      );
    }
  }

  final GlobalKey<AnimatedListState> _listKey = GlobalKey<AnimatedListState>();

  List<Duration> _lapDurations = [];

  void _lap() {
    final formattedLap = _formatTime(_elapsed);

    _laps.insert(0, formattedLap);
    _lapDurations.insert(0, Duration(milliseconds: _elapsed));
    _listKey.currentState?.insertItem(0, duration: Duration(milliseconds: 300));

    setState(() {});
  }

  Color _getLapColor(int index, ColorScheme colorTheme) {
    if (_lapDurations.isEmpty) return colorTheme.secondary;

    final fastestLap = _lapDurations.reduce((a, b) => a < b ? a : b);
    final slowestLap = _lapDurations.reduce((a, b) => a > b ? a : b);

    final lapDuration = _lapDurations[index];
    if (lapDuration.inMilliseconds == fastestLap.inMilliseconds) {
      return Color(0xffb1d18a);
    } else if (lapDuration.inMilliseconds == slowestLap.inMilliseconds) {
      return Color(0xffffb4ab);
    } else {
      return colorTheme.secondary;
    }
  }

  @override
  Widget build(BuildContext context) {
    final elapsedStr = _formatTime(_elapsed);
    final colorTheme = Theme.of(context).colorScheme;

    return Scaffold(
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: AnimatedSwitcher(
              duration: const Duration(milliseconds: 300),
              switchInCurve: Curves.easeInBack,
              switchOutCurve: Curves.easeOutBack,
              transitionBuilder: (child, animation) {
                final offsetAnimation = Tween<Offset>(
                  begin: const Offset(0, 0.1),

                  end: Offset.zero,
                ).animate(animation);
                return SlideTransition(
                  position: offsetAnimation,

                  child: FadeTransition(opacity: animation, child: child),
                );
              },
              child: Text(
                elapsedStr,
                key: ValueKey<String>(elapsedStr),
                style: TextStyle(
                  fontSize: MediaQuery.of(context).size.width / 3.5,
                  fontFamily: "FunFont",
                  fontVariations: [
                    FontVariation.weight(600),
                    FontVariation("ROND", 100),
                  ],
                ),
              ),
            ),
          ),

          Divider(),

          SizedBox(
            height: 100,
            child: AnimatedList(
              key: _listKey,
              scrollDirection: Axis.horizontal,

              initialItemCount: _laps.length,
              itemBuilder: (context, index, animation) {
                final lapNum = _laps.length - index;
                final lapTime = _laps[index];
                final isLast = index == _laps.length - 1;
                final isFirst = index == 0;

                final lapColor = _getLapColor(index, colorTheme);
                return FadeTransition(
                  opacity: animation,
                  child: Padding(
                    // padding: const EdgeInsets.all(4),
                    padding: EdgeInsetsGeometry.only(
                      left: isFirst ? 16 : 4,
                      top: 4,
                      bottom: 4,
                      right: isLast ? 16 : 4,
                    ),
                    child: Container(
                      padding: const EdgeInsets.all(18),
                      decoration: BoxDecoration(
                        color: colorTheme.surfaceContainerLow,
                        borderRadius: BorderRadius.circular(50),
                      ),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(
                            "#$lapNum",

                            style: TextStyle(color: lapColor, fontSize: 16),
                          ),

                          Text(
                            lapTime,

                            style: TextStyle(color: lapColor, fontSize: 16),
                          ),
                        ],
                      ),
                    ),
                  ),
                );
              },
            ),
          ),

          SizedBox(height: 10),

          Expanded(child: SizedBox.shrink()),

          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            spacing: 10,
            children: [
              GestureDetector(
                onTap: _startPause,
                child: TweenAnimationBuilder<double>(
                  tween: Tween<double>(end: _isRunning ? 28.0 : 50.0),
                  duration: const Duration(milliseconds: 300),
                  curve: Curves.easeOutBack,
                  builder: (context, borderRadiusValue, child) {
                    return Container(
                      width: 96,
                      height: 96,
                      decoration: BoxDecoration(
                        color: _isRunning
                            ? colorTheme.surfaceContainerHighest
                            : colorTheme.primary,
                        borderRadius: BorderRadius.circular(borderRadiusValue),
                      ),
                      child: child,
                    );
                  },
                  child: Material(
                    color: Colors.transparent,
                    child: Center(
                      child: IconWithWeight(
                        _isRunning ? Symbols.pause : Symbols.play_arrow,
                        color: _isRunning
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
                onPressed: _reset,
                backgroundColor: colorTheme.secondaryContainer,
                heroTag: "idk_cool_random_tag",

                elevation: 0,
                highlightElevation: 0,
                child: IconWithWeight(
                  Symbols.restart_alt,
                  size: 40,
                  color: colorTheme.onSecondaryContainer,
                ),
              ),
            ],
          ),

          SizedBox(height: 10),
          Container(
            width: 96 + 96,
            height: 70,
            clipBehavior: Clip.hardEdge,
            decoration: BoxDecoration(borderRadius: BorderRadius.circular(50)),
            child: FloatingActionButton.extended(
              onPressed: _isRunning ? _lap : null,
              backgroundColor: colorTheme.secondaryContainer,
              heroTag: "idk_random_tag_stop_wow",

              label: Text(
                "Lap",
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.w500),
              ),
            ),
          ),

          SizedBox(height: 16),
        ],
      ),
    );
  }
}
