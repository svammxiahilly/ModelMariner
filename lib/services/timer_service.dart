import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_foreground_task/flutter_foreground_task.dart';
import 'package:flutter_ringtone_player/flutter_ringtone_player.dart';

@pragma('vm:entry-point')
void startCallback() {
  FlutterForegroundTask.setTaskHandler(MyTaskHandler());
}

final LoopingRingtone loopingRingtone = LoopingRingtone();

class LoopingRingtone {
  Timer? _loopTimer;

  void play({int intervalSeconds = 3}) {
    FlutterRingtonePlayer().play(
      android: AndroidSounds.notification,
      ios: IosSounds.alarm,
      looping: false,
      volume: 1.0,
    );

    _loopTimer?.cancel();
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
              loopingRingtone.stop();
              loopingRingtone.play(intervalSeconds: 3);
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
    final runningTimers = data
        .where((e) => e['isRunning'] == true)
        .toList(growable: false);
    final pausedTimers = data
        .where(
          (e) => e['isRunning'] == false && (e['remainingSeconds'] ?? 0) > 0,
        )
        .toList(growable: false);

    final activeId = prefs.getString('activeTimerId');

    Map<String, dynamic>? t;

    if (activeId != null) {
      try {
        t = data.firstWhere((x) => x['id'] == activeId);
      } catch (e) {
        t = null;
      }
    }

    if (t == null) {
      if (runningTimers.isNotEmpty) {
        t = runningTimers.first;
      } else if (pausedTimers.isNotEmpty) {
        t = pausedTimers.first;
      }
    }

    String title = 'Timer Finished';
    String text = 'Timer';

    if (t != null) {
      final int rem = (t['remainingSeconds'] ?? 0) is int
          ? t['remainingSeconds']
          : int.tryParse('${t['remainingSeconds']}') ?? 0;
      title = formatForNotification(rem);

      if (runningTimers.length > 1) {
        text = 'Running ${runningTimers.length} timers';
      } else {
        final uiLabel = t['uiLabel'] ?? '';
        text = (t['isRunning'] == true)
            ? 'Running • $uiLabel'
            : 'Paused • $uiLabel';
      }
    } else {
      title = 'Timers';
      text = 'No active timers';
    }

    await FlutterForegroundTask.updateService(
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

    int findTargetIndex() {
      if (data.isEmpty) return -1;
      final activeId = prefs.getString('activeTimerId');
      if (activeId != null) {
        final idx = data.indexWhere((t) => t['id'] == activeId);
        if (idx != -1) return idx;
      }
      final runningIdx = data.indexWhere((t) => t['isRunning'] == true);
      if (runningIdx != -1) return runningIdx;
      final pausedIdx = data.indexWhere(
        (t) => (t['remainingSeconds'] ?? 0) > 0,
      );
      if (pausedIdx != -1) return pausedIdx;
      return 0;
    }

    if (id == BTN_START_STOP) {
      if (data.isNotEmpty) {
        final idx = findTargetIndex();
        if (idx != -1) {
          Map<String, dynamic> m = Map<String, dynamic>.from(data[idx]);

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
            loopingRingtone.stop();
          } else {
            m['lastStartEpochMs'] = DateTime.now().millisecondsSinceEpoch;
            m['isRunning'] = true;
          }

          data[idx] = m;
          await prefs.setString('activeTimerId', m['id']);
          await prefs.setString(prefKey, jsonEncode(data));
        }
      }
    } else if (id == BTN_RESET || id == 'reset') {
      if (data.isNotEmpty) {
        final idx = findTargetIndex();
        if (idx != -1) {
          final m = Map<String, dynamic>.from(data[idx]);
          m['remainingSeconds'] = m['initialSeconds'];
          m['isRunning'] = false;
          m['lastStartEpochMs'] = null;
          data[idx] = m;

          final activeId = prefs.getString('activeTimerId');
          if (activeId != null && activeId == m['id']) {
            await prefs.remove('activeTimerId');
          }
        }
      }
    } else if (id == BTN_ADD_MIN) {
      if (data.isNotEmpty) {
        final idx = findTargetIndex();
        if (idx != -1) {
          final m = Map<String, dynamic>.from(data[idx]);

          final oldRemaining = (m['remainingSeconds'] ?? 0) as int;
          m['remainingSeconds'] = oldRemaining + 60;

          if (m['isRunning'] == true) {
            final nowMs = DateTime.now().millisecondsSinceEpoch;
            final oldCurr = (m['currentDuration'] is int)
                ? m['currentDuration'] as int
                : oldRemaining;
            m['currentDuration'] = oldCurr + 60;

            m['lastStartEpochMs'] = nowMs;
          }

          data[idx] = m;
        }
      }
      // await FlutterForegroundTask.stopService();
    } else if (id == BTN_RESET_ALL) {
      for (var i = 0; i < data.length; i++) {
        final m = Map<String, dynamic>.from(data[i]);
        m['remainingSeconds'] = m['initialSeconds'];
        m['isRunning'] = false;
        m['lastStartEpochMs'] = null;
        data[i] = m;
      }
      loopingRingtone.stop();
      await prefs.remove('activeTimerId');
    }

    await prefs.setString(prefKey, jsonEncode(data));

    FlutterForegroundTask.sendDataToMain({
      'type': 'notified_btn_pressed',
      'id': id,
      'timers': data,
    });

    FlutterForegroundTask.sendDataToMain({'type': 'timers_updated'});

    final anyRunning = data.any((t) => t['isRunning'] == true);
    final anyPending = data.any((t) => (t['remainingSeconds'] ?? 0) > 0);

    if (anyRunning || anyPending) {
      if (id == "btn_reset") {
        await FlutterForegroundTask.stopService();
        return;
      }
      await FlutterForegroundTask.updateService(
        notificationTitle: (data.isNotEmpty
            ? formatForNotification(
                (data.firstWhere(
                      (t) => (t['remainingSeconds'] ?? 0) > 0,
                    )['remainingSeconds'] ??
                    0),
              )
            : 'Timers'),
        notificationText: (() {
          final runningCount = data.where((t) => t['isRunning'] == true).length;
          if (runningCount > 0) return 'Running $runningCount timers';
          final firstPending = data.firstWhere(
            (t) => (t['remainingSeconds'] ?? 0) > 0,
            orElse: () => null,
          );
          return firstPending != null
              ? 'Paused • ${firstPending['uiLabel']}'
              : 'Timers';
        }()),
        notificationButtons: _buildNotificationButtons(
          data.map((e) => Map<String, dynamic>.from(e)).toList(),
        ),
      );
    } else {
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
