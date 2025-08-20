import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:clockmaster/helpers/icon_helper.dart';
import 'package:clockmaster/helpers/preferences_helper.dart';
import 'package:flutter/material.dart';
import 'package:flutter_foreground_task/flutter_foreground_task.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import 'package:animations/animations.dart';
import '../models/timer_model.dart';
import 'full_screen_timer.dart';
import 'package:flutter_ringtone_player/flutter_ringtone_player.dart';

@pragma('vm:entry-point')
void startCallback() {
  FlutterForegroundTask.setTaskHandler(MyTaskHandler());
}

class LoopingRingtone {
  Timer? _loopTimer;

  void play({int intervalSeconds = 3}) {
    // First play immediately
    FlutterRingtonePlayer().play(
      android: AndroidSounds.notification,
      ios: IosSounds.alarm,
      looping:
          false, // We'll loop manually, cause the plugin is broken and won't stop
      volume: 1.0,
    );

    // Schedule repeats
    _loopTimer = Timer.periodic(Duration(seconds: intervalSeconds), (_) {
      FlutterRingtonePlayer().play(
        android: AndroidSounds.notification,
        ios: IosSounds.alarm,
        looping: false,
        volume: 1.0,
      );
    });
  }

  void stop() {
    _loopTimer?.cancel();
    _loopTimer = null;
    FlutterRingtonePlayer().stop();
  }
}

class MyTaskHandler extends TaskHandler {
  static const String BTN_START_STOP = 'btn_start_stop';
  static const String BTN_RESET = 'btn_reset';
  static const String BTN_ADD_MIN = 'btn_add_minute';
  static const String BTN_RESET_ALL = 'btn_reset_all';

  final String prefKey = 'timers';

  @override
  Future<void> onStart(DateTime timestamp, TaskStarter starter) async {}

  @override
  void onRepeatEvent(DateTime timestamp) {
    _tickAndPersist();
  }

  Future<void> _tickAndPersist() async {
    final prefs = await SharedPreferences.getInstance();
    final jsonString = prefs.getString(prefKey);
    if (jsonString == null) return;

    final List data = jsonDecode(jsonString);
    final now = DateTime.now().millisecondsSinceEpoch;

    bool changed = false;
    List<Map<String, dynamic>> changedTimers = [];

    for (var i = 0; i < data.length; i++) {
      final m = Map<String, dynamic>.from(data[i]);
      bool shouldUpdate = false;

      if (m['isRunning'] == true && m['lastStartEpochMs'] != null) {
        int lastStart = m['lastStartEpochMs'];
        int elapsedMs = now - lastStart;
        int elapsedSec = (elapsedMs / 1000).floor();

        if (elapsedSec > 0) {
          int newRemaining = m['remainingSeconds'] - elapsedSec;
          if (newRemaining <= 0) {
            if (m['isRunning'] == true) {
              LoopingRingtone().stop();
              LoopingRingtone().play(intervalSeconds: 3);
              FlutterForegroundTask.updateService(
                notificationTitle: 'Timer Finished',
                notificationText: 'Timer',
                notificationButtons: [
                  NotificationButton(
                    id: m['isRunning'] ? 'pause' : 'start',
                    text: m['isRunning'] ? 'Pause' : 'Start',
                  ),
                  const NotificationButton(id: 'reset', text: 'Reset'),
                ],
              );
            }
            m['remainingSeconds'] = 0;
            m['isRunning'] = false;
            m['lastStartEpochMs'] = null;
          } else {
            m['remainingSeconds'] = newRemaining;
            m['lastStartEpochMs'] = now;
            LoopingRingtone().stop();
          }
        }

        shouldUpdate = true;
      }

      if (shouldUpdate) {
        data[i] = m;
        changed = true;
        changedTimers.add(m);
      }
    }

    final runningTimers = data.where((e) => e['isRunning'] == true).toList();
    final pausedTimers = data
        .where((e) => e['isRunning'] == false && e['remainingSeconds'] > 0)
        .toList();

    final activeId = prefs.getString('activeTimerId');

    Map<String, dynamic>? t;

    if (runningTimers.isNotEmpty) {
      t = activeId != null
          ? runningTimers.firstWhere(
              (x) => x['id'] == activeId,
              orElse: () => runningTimers.first,
            )
          : runningTimers.first;
    } else if (pausedTimers.isNotEmpty) {
      t = activeId != null
          ? pausedTimers.firstWhere(
              (x) => x['id'] == activeId,
              orElse: () => pausedTimers.first,
            )
          : pausedTimers.first;
    }

    String title = 'Timer Finished';
    String text = 'Timer';

    if (t != null) {
      title = formatForNotification(t['remainingSeconds']);
      if (runningTimers.length > 1) {
        text = 'Running ${runningTimers.length} timers';
      } else {
        text = t['isRunning'] ? 'Running' : 'Paused';
      }
    }

    FlutterForegroundTask.updateService(
      notificationTitle: title,
      notificationText: text,
      notificationButtons: _buildNotificationButtons(
        data.map((e) => Map<String, dynamic>.from(e)).toList(),
      ),
    );

    if (changed) {
      await prefs.setString(prefKey, jsonEncode(data));
      FlutterForegroundTask.sendDataToMain({
        'type': 'timers_tick',
        'changedTimers': changedTimers,
      });
    }
  }

  String formatForNotification(int seconds) {
    final h = seconds ~/ 3600;
    final m = (seconds % 3600) ~/ 60;
    final s = seconds % 60;
    if (h > 0) {
      return '${h.toString()}:${m.toString().padLeft(2, '0')}:${s.toString().padLeft(2, '0')}';
    } else {
      return '${m.toString().padLeft(2, '0')}:${s.toString().padLeft(2, '0')}';
    }
  }

  @override
  void onReceiveData(Object data) {}

  @override
  void onNotificationButtonPressed(String id) async {
    print('Notification button pressed: $id');

    final prefs = await SharedPreferences.getInstance();
    final jsonString = prefs.getString(prefKey);
    if (jsonString == null) return;
    final List data = jsonDecode(jsonString);

    if (id == BTN_START_STOP) {
      if (data.isNotEmpty) {
        Map<String, dynamic> m = data.firstWhere(
          (t) => t['isRunning'] == true,
          orElse: () => data.firstWhere((t) => t['remainingSeconds'] > 0),
        );

        final idx = data.indexWhere((t) => t['id'] == m['id']);

        if (m['isRunning'] == true) {
          int lastStart =
              m['lastStartEpochMs'] ?? DateTime.now().millisecondsSinceEpoch;
          int elapsedSec =
              ((DateTime.now().millisecondsSinceEpoch - lastStart) / 1000)
                  .floor();
          m['remainingSeconds'] = (m['remainingSeconds'] - elapsedSec).clamp(
            0,
            99999999,
          );
          m['isRunning'] = false;
          m['lastStartEpochMs'] = null;
        } else {
          m['lastStartEpochMs'] = DateTime.now().millisecondsSinceEpoch;
          m['isRunning'] = true;
        }

        if (idx != -1) data[idx] = m;
        await prefs.setString('activeTimerId', m['id']);
        await prefs.setString(prefKey, jsonEncode(data));
      }
    } else if (id == BTN_RESET) {
      if (data.isNotEmpty) {
        final m = Map<String, dynamic>.from(data[0]);
        m['remainingSeconds'] = m['initialSeconds'];
        m['isRunning'] = false;
        m['lastStartEpochMs'] = null;
        data[0] = m;
        await prefs.setString(prefKey, jsonEncode(data));
      }
    } else if (id == BTN_ADD_MIN) {
      if (data.isNotEmpty) {
        final m = Map<String, dynamic>.from(data[0]);
        m['remainingSeconds'] = m['remainingSeconds'] + 60;
        data[0] = m;
        await prefs.setString(prefKey, jsonEncode(data));
      }
    } else if (id == BTN_RESET_ALL) {
      for (var i = 0; i < data.length; i++) {
        final m = Map<String, dynamic>.from(data[i]);
        m['remainingSeconds'] = m['initialSeconds'];
        m['isRunning'] = false;
        m['lastStartEpochMs'] = null;
        data[i] = m;
      }
      await prefs.setString(prefKey, jsonEncode(data));
      await FlutterForegroundTask.stopService();
    }

    await prefs.setString(prefKey, jsonEncode(data));

    FlutterForegroundTask.sendDataToMain({
      'type': 'notified_btn_pressed',
      'id': id,
      'timers': data,
    });

    FlutterForegroundTask.sendDataToMain({'type': 'timers_updated'});

    if (id == 'btn_reset') {
      await FlutterForegroundTask.stopService();
    }
  }

  @override
  void onNotificationPressed() {
    FlutterForegroundTask.sendDataToMain({'type': 'notification_tapped'});
  }

  List<NotificationButton> _buildNotificationButtons(
    List<Map<String, dynamic>> timersData,
  ) {
    final runningCount = timersData.where((t) => t['isRunning'] == true).length;

    if (runningCount > 1) {
      return [NotificationButton(id: BTN_RESET_ALL, text: 'Reset All')];
    } else {
      return [
        NotificationButton(
          id: BTN_START_STOP,
          text: timersData.any((t) => t['isRunning'] == true)
              ? 'Pause'
              : 'Start',
        ),
        NotificationButton(id: BTN_RESET, text: 'Reset'),
        NotificationButton(id: BTN_ADD_MIN, text: '+1:00'),
      ];
    }
  }

  @override
  Future<void> onDestroy(DateTime timestamp, bool isTimeout) async {}
}

class TimerScreen extends StatefulWidget {
  const TimerScreen({super.key});

  @override
  State<TimerScreen> createState() => TimerScreenState();
}

class TimerScreenState extends State<TimerScreen> with WidgetsBindingObserver {
  final String prefKey = 'timers';
  List<TimerModel> timers = [];
  final ValueNotifier<Object?> _taskDataListenable = ValueNotifier(null);
  Timer? _uiTimer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);

    FlutterForegroundTask.addTaskDataCallback(_onReceiveTaskData);
    _startUITimer();
    _initForegroundTaskAndLoad();
    PreferencesHelper.setBool("isFullScreen", false);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    FlutterForegroundTask.removeTaskDataCallback(_onReceiveTaskData);
    _uiTimer?.cancel();
    _taskDataListenable.dispose();
    super.dispose();
  }

  void _startUITimer() {
    _uiTimer?.cancel();

    void scheduleNextTick() {
      final now = DateTime.now().millisecondsSinceEpoch;
      final msToNextSecond = 1000 - (now % 1000);

      _uiTimer = Timer(Duration(milliseconds: msToNextSecond), () {
        _onTick();
        if (mounted) scheduleNextTick();
      });
    }

    scheduleNextTick();
  }

  void _onTick() {
    final now = DateTime.now().millisecondsSinceEpoch;

    for (var t in timers) {
      if (t.isRunning && t.lastStartEpochMs != null) {
        final elapsed = ((now - t.lastStartEpochMs!) / 1000).floor();
        final newRemaining = (t.initialSeconds - elapsed).clamp(0, 99999999);

        if (newRemaining != t.remainingSeconds) {
          t.remainingSeconds = newRemaining;
        }
      }
    }
  }

  void _onReceiveTaskData(Object data) async {
    if (data is Map &&
        data['type'] == 'notified_btn_pressed' &&
        data['timers'] != null) {
      final incoming = (data['timers'] as List)
          .map((e) => TimerModel.fromMap(Map<String, dynamic>.from(e)))
          .toList();

      for (final updated in incoming) {
        final idx = timers.indexWhere((t) => t.id == updated.id);
        if (idx != -1) {
          timers[idx] = updated;
        } else {
          timers.add(updated);
        }
      }

      if (data['type'] == 'notified_btn_pressed') {
        final id = data['id'];
        if (id == 'btn_reset') {
          if (mounted) {
            if (PreferencesHelper.getBool("isFullScreen") == true) {
              Navigator.pop(context);
              print(PreferencesHelper.getBool("isFullScreen"));
            }
          }
        }
      }

      if (mounted) setState(() {});
      return;
    }

    if (data is Map &&
        data['type'] == 'timers_updated' &&
        data['timers'] != null) {
      final incoming = (data['timers'] as List)
          .map((e) => TimerModel.fromMap(Map<String, dynamic>.from(e)))
          .toList();

      for (final updated in incoming) {
        final idx = timers.indexWhere((t) => t.id == updated.id);
        if (idx != -1) {
          if (updated.remainingSeconds < timers[idx].remainingSeconds) {
            timers[idx].remainingSeconds = updated.remainingSeconds;
            timers[idx].isRunning = updated.isRunning;
            timers[idx].lastStartEpochMs = updated.lastStartEpochMs;
          }
        } else {
          timers.add(updated);
        }
      }

      if (mounted) setState(() {});
    }
  }

  Future<void> _initForegroundTaskAndLoad() async {
    final NotificationPermission permission =
        await FlutterForegroundTask.checkNotificationPermission();
    if (permission != NotificationPermission.granted) {}

    if (Platform.isAndroid) {}

    FlutterForegroundTask.init(
      androidNotificationOptions: AndroidNotificationOptions(
        channelId: 'timer_foreground_service',
        channelName: 'Timer Foreground Service',
        channelDescription: 'Shows active timer(s) and controls',
        channelImportance: NotificationChannelImportance.DEFAULT,
        onlyAlertOnce: true,
      ),
      iosNotificationOptions: const IOSNotificationOptions(
        showNotification: false,
        playSound: false,
      ),
      foregroundTaskOptions: ForegroundTaskOptions(
        eventAction: ForegroundTaskEventAction.repeat(1000),
        allowWakeLock: true,
        allowWifiLock: false,
      ),
    );

    await _loadTimers();

    if (timers.any((t) => t.isRunning)) {
      await _startServiceIfNotRunning();
    }
  }

  Future<void> _loadTimers() async {
    final prefs = await SharedPreferences.getInstance();
    final jsonString = prefs.getString(prefKey);
    if (jsonString == null) {
      timers = [];
    } else {
      final List data = jsonDecode(jsonString);
      timers = data
          .map((e) => TimerModel.fromMap(Map<String, dynamic>.from(e)))
          .toList();

      final now = DateTime.now().millisecondsSinceEpoch;
      for (var t in timers) {
        if (t.isRunning && t.lastStartEpochMs != null) {
          final elapsedSec = ((now - t.lastStartEpochMs!) / 1000).floor();
          if (elapsedSec > 0) {
            t.remainingSeconds = (t.remainingSeconds - elapsedSec).clamp(
              0,
              99999999,
            );
            if (t.remainingSeconds <= 0) {
              t.isRunning = false;
              t.lastStartEpochMs = null;
            } else {
              t.lastStartEpochMs = now;
            }
          }
        }
      }
    }
    if (mounted) setState(() {});
  }

  List<NotificationButton> _buildNotificationButtons(List<TimerModel> timers) {
    final runningCount = timers.where((t) => t.isRunning).length;

    if (runningCount > 1) {
      return [NotificationButton(id: 'btn_reset_all', text: 'Reset All')];
    } else {
      return [
        NotificationButton(
          id: 'btn_start_stop',
          text: timers.any((t) => t.isRunning) ? 'Pause' : 'Start',
        ),
        NotificationButton(id: 'btn_reset', text: 'Reset'),
        NotificationButton(id: 'btn_add_minute', text: '+1:00'),
      ];
    }
  }

  Future<void> _persistTimers() async {
    final prefs = await SharedPreferences.getInstance();
    final data = timers.map((t) => t.toMap()).toList();
    await prefs.setString(prefKey, jsonEncode(data));
  }

  Future<void> _startServiceIfNotRunning() async {
    if (await FlutterForegroundTask.isRunningService) {
      await FlutterForegroundTask.updateService(
        notificationTitle: 'Timers',
        notificationButtons: _buildNotificationButtons(timers),
      );
      return;
    }

    await FlutterForegroundTask.startService(
      serviceId: 150,
      notificationTitle: 'Timers',
      notificationText: 'Foreground service running',
      notificationIcon: const NotificationIcon(
        metaDataName: "com.pranshulgg.service.timer_icon",
      ),

      notificationButtons: _buildNotificationButtons(timers),
      notificationInitialRoute: '/',
      callback: startCallback,
    );
  }

  Future<void> _stopServiceIfNoRunningTimers() async {
    if (!timers.any((t) => t.isRunning) &&
        await FlutterForegroundTask.isRunningService) {
      await FlutterForegroundTask.stopService();
    }
  }

  int getRunningTimersCount() {
    return timers.where((t) => t.isRunning).length;
  }

  bool startAfterAdding = false;

  String formatDefaultLabel(int totalSeconds) {
    final hours = totalSeconds ~/ 3600;
    final minutes = (totalSeconds % 3600) ~/ 60;
    final seconds = totalSeconds % 60;

    final parts = <String>[];
    if (hours > 0) parts.add('${hours}h');
    if (minutes > 0) parts.add('${minutes}m');
    if (seconds > 0 || parts.isEmpty) parts.add('${seconds}s');

    return parts.join(' ');
  }

  Future<void> _addTimerFromDuration(int seconds, {String? labelTimer}) async {
    final id = const Uuid().v4();
    final label = 'Timer ${timers.length + 1}';
    final formattedLabel = formatDefaultLabel(seconds);
    final model = TimerModel(
      id: id,
      label: label,
      uiLabel: labelTimer ?? "$formattedLabel timer",
      remainingSeconds: seconds,
      initialSeconds: seconds,
      isRunning: false,
      lastStartEpochMs: null,
    );
    timers.add(model);

    if (getRunningTimersCount() == 0) {
      startAfterAdding = false;
    } else {
      startAfterAdding = true;
    }

    await FlutterForegroundTask.stopService();
    await _persistTimers();
    setState(() {});

    if (startAfterAdding) {
      await _startServiceIfNotRunning();
    }
  }

  Future<void> _toggleStartStop(TimerModel t) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    final idx = timers.indexWhere((x) => x.id == t.id);
    if (idx == -1) return;
    final model = timers[idx];

    if (model.isRunning) {
      if (model.lastStartEpochMs != null) {
        final elapsed = ((now - model.lastStartEpochMs!) / 1000).floor();
        model.remainingSeconds = (model.remainingSeconds - elapsed).clamp(
          0,
          99999999,
        );
      }
      model.isRunning = false;
      model.lastStartEpochMs = null;
    } else {
      model.isRunning = true;
      model.lastStartEpochMs = now;
    }

    await _persistTimers();
    await FlutterForegroundTask.stopService();

    setState(() {});

    if (timers.any((x) => x.isRunning)) {
      await _startServiceIfNotRunning();
    } else {
      await _stopServiceIfNoRunningTimers();
    }
  }

  Future<void> _resetTimer(TimerModel t) async {
    final idx = timers.indexWhere((x) => x.id == t.id);
    if (idx == -1) return;
    timers[idx].remainingSeconds = timers[idx].initialSeconds;
    timers[idx].currentDuration = timers[idx].initialSeconds;
    timers[idx].isRunning = false;
    timers[idx].lastStartEpochMs = null;
    await _persistTimers();
    await FlutterForegroundTask.stopService();
    setState(() {});
    await _stopServiceIfNoRunningTimers();
  }

  bool startAfterAddingOneMin = false;

  Future<void> _addOneMinute(TimerModel t) async {
    await FlutterForegroundTask.stopService();
    final idx = timers.indexWhere((x) => x.id == t.id);
    if (idx == -1) return;
    timers[idx].remainingSeconds += 60;
    timers[idx].currentDuration += 60;
    await _persistTimers();
    if (getRunningTimersCount() == 0) {
      startAfterAddingOneMin = false;
    } else {
      startAfterAddingOneMin = true;
    }

    if (startAfterAddingOneMin) {
      await _startServiceIfNotRunning();
    }

    setState(() {});
  }

  Future<void> _deleteTimer(TimerModel t) async {
    timers.removeWhere((x) => x.id == t.id);
    await _persistTimers();
    await FlutterForegroundTask.stopService();
    setState(() {});
    await _stopServiceIfNoRunningTimers();
  }

  String _format(int seconds) {
    final h = seconds ~/ 3600;
    final m = (seconds % 3600) ~/ 60;
    final s = seconds % 60;
    if (h > 0) {
      return '${h.toString()}:${m.toString().padLeft(2, '0')}:${s.toString().padLeft(2, '0')}';
    } else {
      return '${m.toString()}:${s.toString().padLeft(2, '0')}';
    }
  }

  void _showEditLabelDialog(TimerModel t) {
    final controller = TextEditingController(text: t.uiLabel);

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text('Edit Timer Label'),
        content: TextField(
          controller: controller,
          decoration: InputDecoration(hintText: 'Enter label'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text(
              'Cancel',
              style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
            ),
          ),
          TextButton(
            onPressed: () async {
              t.uiLabel = controller.text.trim();
              await _persistTimers();
              setState(() {});
              Navigator.pop(ctx);
            },
            child: Text(
              'Save',
              style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16),
            ),
          ),
        ],
      ),
    );
  }

  void showAddBottomSheet() {
    int hours = 0, minutes = 0, seconds = 0;
    final colorTheme = Theme.of(context).colorScheme;
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
      ),
      builder: (ctx) {
        return StatefulBuilder(
          builder: (ctx2, setSt) {
            final total = hours * 3600 + minutes * 60 + seconds;

            return Container(
              padding: EdgeInsets.fromLTRB(16, 0, 16, 0),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  // Text(
                  // 'Add Timer — ${hours}h ${minutes}m ${seconds}s',
                  // style: const TextStyle(fontSize: 18),
                  // ),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    spacing: 8,
                    children: [
                      Container(
                        height: 50,
                        width: 60,

                        decoration: BoxDecoration(
                          color: colorTheme.tertiaryContainer,
                          borderRadius: BorderRadius.circular(50),
                        ),
                        child: Center(
                          child: Text(
                            "${hours}h",
                            style: TextStyle(
                              color: colorTheme.onTertiaryContainer,
                              fontSize: 24,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                      ),
                      Container(
                        height: 50,
                        width: 60,

                        decoration: BoxDecoration(
                          color: colorTheme.tertiaryContainer,
                          borderRadius: BorderRadius.circular(50),
                        ),
                        child: Center(
                          child: Text(
                            "${minutes}m",
                            style: TextStyle(
                              color: colorTheme.onTertiaryContainer,
                              fontSize: 24,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                      ),
                      Container(
                        height: 50,
                        width: 60,

                        decoration: BoxDecoration(
                          color: colorTheme.tertiaryContainer,
                          borderRadius: BorderRadius.circular(50),
                        ),
                        child: Center(
                          child: Text(
                            "${seconds}s",
                            style: TextStyle(
                              color: colorTheme.onTertiaryContainer,
                              fontSize: 24,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Divider(),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      // const Text('H'),
                      CircleAvatar(
                        foregroundColor: colorTheme.onPrimaryContainer,
                        backgroundColor: colorTheme.primaryContainer,
                        child: Text('H'),
                      ),
                      Expanded(
                        child: Slider(
                          min: 0,
                          max: 23,
                          divisions: 23,

                          value: hours.toDouble(),
                          year2023: false,
                          onChanged: (v) => setSt(() => hours = v.toInt()),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      CircleAvatar(
                        foregroundColor: colorTheme.onPrimaryContainer,
                        backgroundColor: colorTheme.primaryContainer,
                        child: Text('M'),
                      ),
                      Expanded(
                        child: Slider(
                          min: 0,
                          max: 59,
                          divisions: 59,
                          value: minutes.toDouble(),

                          year2023: false,
                          onChanged: (v) => setSt(() => minutes = v.toInt()),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      CircleAvatar(
                        foregroundColor: colorTheme.onPrimaryContainer,
                        backgroundColor: colorTheme.primaryContainer,
                        child: Text('S'),
                      ),
                      Expanded(
                        child: Slider(
                          min: 0,
                          max: 59,
                          divisions: 59,
                          value: seconds.toDouble(),
                          year2023: false,
                          onChanged: (v) => setSt(() => seconds = v.toInt()),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Divider(),

                  Row(
                    spacing: 10,
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      TextButton(
                        onPressed: () {
                          Navigator.pop(context);
                        },
                        child: Text(
                          "Cancel",
                          style: TextStyle(
                            fontWeight: FontWeight.w600,
                            fontSize: 18,
                          ),
                        ),
                      ),
                      TextButton(
                        onPressed: total > 0
                            ? () {
                                Navigator.of(ctx).pop();
                                _addTimerFromDuration(total);
                              }
                            : null,
                        child: const Text(
                          'Add Timer',
                          style: TextStyle(
                            fontWeight: FontWeight.w600,
                            fontSize: 18,
                          ),
                        ),
                      ),
                    ],
                  ),
                  SizedBox(height: MediaQuery.of(ctx2).padding.bottom + 10),
                ],
              ),
            );
          },
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final colorTheme = Theme.of(context).colorScheme;
    return WithForegroundTask(
      child: Scaffold(
        body: Padding(
          padding: const EdgeInsets.fromLTRB(13, 0, 13, 0),
          child: Column(
            children: [
              Expanded(
                child: timers.isEmpty
                    ? Center(
                        child: Opacity(
                          opacity: 0.6,
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              IconWithWeight(
                                Symbols.hourglass_disabled,
                                color: colorTheme.onSurfaceVariant,
                                size: 60,
                              ),
                              Text(
                                "No timers",
                                style: TextStyle(
                                  fontSize: 30,
                                  color: colorTheme.onSurfaceVariant,
                                ),
                              ),
                            ],
                          ),
                        ),
                      )
                    : ListView.builder(
                        itemCount: timers.length,
                        itemBuilder: (ctx, i) {
                          final t = timers[i];

                          return ClipRRect(
                            borderRadius: BorderRadius.circular(18),

                            child: Dismissible(
                              key: ValueKey(t.id),
                              direction: DismissDirection.endToStart,
                              background: Container(
                                alignment: Alignment.centerRight,
                                padding: EdgeInsets.symmetric(horizontal: 20),
                                decoration: BoxDecoration(
                                  color: colorTheme.errorContainer,
                                ),
                                child: Icon(
                                  Icons.delete,
                                  color: colorTheme.onErrorContainer,
                                ),
                              ),
                              onDismissed: (_) {
                                _deleteTimer(t);
                                ScaffoldMessenger.of(context).showSnackBar(
                                  SnackBar(content: Text('Timer deleted')),
                                );
                              },
                              child: ValueListenableBuilder<int>(
                                valueListenable: t.remainingNotifier,
                                builder: (context, remaining, _) {
                                  final progress = t.currentDuration > 0
                                      ? 1 -
                                            (t.remainingSeconds /
                                                t.currentDuration)
                                      : 0.0;

                                  final tileColor = remaining == 0
                                      ? colorTheme.primaryContainer
                                      : colorTheme.surfaceContainerLow;

                                  final finished = remaining == 0;

                                  final isLast = i == timers.length - 1;

                                  return Padding(
                                    padding: EdgeInsets.only(
                                      // bottom: isLast ? 130 : 8,
                                      // bottom: 8,
                                    ),
                                    child: OpenContainer(
                                      closedElevation: 0,
                                      closedColor: tileColor,
                                      transitionDuration: Duration(
                                        milliseconds: 500,
                                      ),
                                      transitionType:
                                          ContainerTransitionType.fadeThrough,
                                      openColor: colorTheme.surface,
                                      closedShape: RoundedRectangleBorder(
                                        // borderRadius: BorderRadius.circular(18),
                                      ),
                                      closedBuilder: (context, openContainer) {
                                        return Container(
                                          padding: const EdgeInsets.only(
                                            top: 5,
                                            bottom: 10,
                                            right: 12,
                                            left: 12,
                                          ),
                                          child: Column(
                                            crossAxisAlignment:
                                                CrossAxisAlignment.start,
                                            children: [
                                              Padding(
                                                padding: const EdgeInsets.only(
                                                  left: 0,
                                                  top: 10,
                                                ),
                                                child: Text(
                                                  t.uiLabel.isNotEmpty
                                                      ? t.uiLabel
                                                      : 'Timer label',
                                                  style: TextStyle(
                                                    height: 1,
                                                    fontSize: 16,
                                                    fontWeight: FontWeight.bold,
                                                    color: colorTheme.secondary,
                                                  ),
                                                ),
                                              ),
                                              Row(
                                                mainAxisAlignment:
                                                    MainAxisAlignment
                                                        .spaceBetween,

                                                children: [
                                                  Text(
                                                    _format(remaining),
                                                    style: TextStyle(
                                                      fontSize: 45,
                                                      fontWeight:
                                                          FontWeight.w600,
                                                      fontFamily: "FunFont2",
                                                      color: t.isRunning
                                                          ? colorTheme.onSurface
                                                          : colorTheme
                                                                .onSurfaceVariant,
                                                    ),
                                                  ),
                                                  Column(
                                                    crossAxisAlignment:
                                                        CrossAxisAlignment.end,
                                                    children: [
                                                      SizedBox(
                                                        width: 38,
                                                        height: 38,
                                                        child: IconButton(
                                                          onPressed: () =>
                                                              _showEditLabelDialog(
                                                                t,
                                                              ),
                                                          icon: IconWithWeight(
                                                            Symbols.edit,
                                                          ),
                                                        ),
                                                      ),
                                                    ],
                                                  ),
                                                ],
                                              ),
                                              LinearProgressIndicator(
                                                value: progress,
                                                year2023: false,
                                              ),
                                              const SizedBox(height: 4),
                                              Row(
                                                mainAxisAlignment:
                                                    MainAxisAlignment
                                                        .spaceBetween,
                                                children: [
                                                  FilledButton.tonal(
                                                    style:
                                                        FilledButton.styleFrom(
                                                          minimumSize: Size(
                                                            46,
                                                            35,
                                                          ),
                                                          backgroundColor:
                                                              finished
                                                              ? colorTheme
                                                                    .surface
                                                              : null,
                                                        ),
                                                    onPressed: () =>
                                                        _addOneMinute(t),

                                                    child: const Text("+1 Min"),
                                                  ),

                                                  Row(
                                                    spacing: 5,
                                                    children: [
                                                      SizedBox(
                                                        width: 42,
                                                        height: 35,
                                                        child: IconButton.filled(
                                                          tooltip: t.isRunning
                                                              ? 'Stop'
                                                              : 'Start',
                                                          onPressed: () =>
                                                              _toggleStartStop(
                                                                t,
                                                              ),
                                                          icon: Icon(
                                                            t.isRunning
                                                                ? Icons.pause
                                                                : Icons
                                                                      .play_arrow,
                                                            size: 18,
                                                          ),
                                                        ),
                                                      ),
                                                      SizedBox(
                                                        width: 42,
                                                        height: 35,
                                                        child: IconButton.filledTonal(
                                                          style: ButtonStyle(
                                                            backgroundColor:
                                                                WidgetStateProperty.all(
                                                                  finished
                                                                      ? colorTheme
                                                                            .surface
                                                                      : null,
                                                                ),
                                                          ),
                                                          tooltip: 'Reset',
                                                          onPressed: () =>
                                                              _resetTimer(t),
                                                          icon: const Icon(
                                                            Icons.refresh,
                                                            size: 18,
                                                          ),
                                                        ),
                                                      ),
                                                    ],
                                                  ),
                                                ],
                                              ),
                                            ],
                                          ),
                                        );
                                      },
                                      openBuilder: (context, _) {
                                        return TimerDetailPage(
                                          timer: t,
                                          onToggle: () => _toggleStartStop(t),
                                          onReset: () => _resetTimer(t),
                                          onAddMinute: () => _addOneMinute(t),
                                          onDelete: () => _deleteTimer(t),
                                        );
                                      },
                                    ),
                                  );
                                },
                              ),
                            ),
                          );
                        },
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
