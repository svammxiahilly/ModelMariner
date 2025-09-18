import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'dart:async';
import 'package:flutter_ringtone_player/flutter_ringtone_player.dart';
import 'dart:convert';

final FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
    FlutterLocalNotificationsPlugin();

final AlarmRingtone alarmRingtone = AlarmRingtone();

class AlarmRingtone {
  bool _isPlaying = false;

  void play() {
    if (_isPlaying) return;
    try {
      FlutterRingtonePlayer().playNotification(
        looping: false,
        volume: 1.0,
        asAlarm: true,
      );
      _isPlaying = true;
    } catch (e) {
      print('AlarmRingtone.play error: $e');
    }
  }

  void stop() {
    if (!_isPlaying) return;
    try {
      FlutterRingtonePlayer().stop();
    } catch (e) {
      print('AlarmRingtone.stop error: $e');
    } finally {
      _isPlaying = false;
    }
  }
}

@pragma('vm:entry-point')
void notificationTapBackground(NotificationResponse notificationResponse) {
  final actionId = notificationResponse.actionId;
  final payload = notificationResponse.payload;
  Map<String, dynamic> data = {};
  if (payload != null && payload.isNotEmpty) {
    try {
      data = jsonDecode(payload) as Map<String, dynamic>;
    } catch (_) {}
  }

  final service = FlutterBackgroundService();

  if (actionId == 'pause') {
    service.invoke('pauseTimer');
    service.invoke('update', {
      'isRunning': false,
      'paused': true,
      'remaining': data['remaining'] ?? data['total'] ?? 0,
      'total': data['total'] ?? 0,
    });
  } else if (actionId == 'resume') {
    final resumeFrom =
        (data['remaining'] as int?) ?? (data['total'] as int?) ?? 0;
    service.invoke('startTimer', {'duration': resumeFrom});
    service.invoke('update', {
      'isRunning': true,
      'remaining': resumeFrom,
      'total': data['total'] ?? resumeFrom,
    });
  } else if (actionId == 'reset') {
    service.invoke('resetTimer');

    service.invoke('update', {
      'isRunning': false,
      'reset': true,
      'remaining': data['total'] ?? 0,
      'total': data['total'] ?? 0,
    });
  }
}

Future<void> initializeService() async {
  final service = FlutterBackgroundService();

  const AndroidNotificationChannel channel = AndroidNotificationChannel(
    'pomodoro_channel',
    'Pomodoro Timer',
    description: 'Background Pomodoro Timer',
    importance: Importance.high,
  );

  await flutterLocalNotificationsPlugin
      .resolvePlatformSpecificImplementation<
        AndroidFlutterLocalNotificationsPlugin
      >()
      ?.createNotificationChannel(channel);

  const AndroidInitializationSettings initializationSettingsAndroid =
      AndroidInitializationSettings('baseline_energy_savings_leaf_24');

  const InitializationSettings initializationSettings = InitializationSettings(
    android: initializationSettingsAndroid,
  );

  await flutterLocalNotificationsPlugin.initialize(initializationSettings);

  await service.configure(
    androidConfiguration: AndroidConfiguration(
      onStart: onStart,
      autoStart: false,
      isForegroundMode: true,
      notificationChannelId: 'pomodoro_channel',
      initialNotificationTitle: 'Pomodoro Timer',
      initialNotificationContent: 'Waiting to start...',
      foregroundServiceNotificationId: 888,
    ),
    iosConfiguration: IosConfiguration(),
  );

  await flutterLocalNotificationsPlugin.initialize(
    initializationSettings,
    onDidReceiveNotificationResponse: (NotificationResponse response) async {
      final actionId = response.actionId;
      final payload = response.payload;
      Map<String, dynamic> data = {};
      if (payload != null && payload.isNotEmpty) {
        try {
          data = jsonDecode(payload) as Map<String, dynamic>;
        } catch (_) {}
      }
      final service = FlutterBackgroundService();
      if (actionId == 'pause') {
        service.invoke('pauseTimer');
        service.invoke('update', {
          'isRunning': false,
          'paused': true,
          'remaining': data['remaining'] ?? data['total'] ?? 0,
          'total': data['total'] ?? 0,
        });
      } else if (actionId == 'resume') {
        final resumeFrom =
            (data['remaining'] as int?) ?? (data['total'] as int?) ?? 0;
        service.invoke('startTimer', {'duration': resumeFrom});
        service.invoke('update', {
          'isRunning': true,
          'remaining': resumeFrom,
          'total': data['total'] ?? resumeFrom,
        });
      } else if (actionId == 'reset') {
        service.invoke('resetTimer');
        service.invoke('update', {
          'isRunning': false,
          'reset': true,
          'remaining': data['total'] ?? 0,
          'total': data['total'] ?? 0,
        });
      }
    },
    onDidReceiveBackgroundNotificationResponse: notificationTapBackground,
  );
}

@pragma('vm:entry-point')
void onStart(ServiceInstance service) async {
  final FlutterLocalNotificationsPlugin notifications =
      FlutterLocalNotificationsPlugin();

  const AndroidInitializationSettings initSettingsAndroid =
      AndroidInitializationSettings('baseline_energy_savings_leaf_24');
  const InitializationSettings initSettings = InitializationSettings(
    android: initSettingsAndroid,
  );
  await notifications.initialize(
    initSettings,
    onDidReceiveNotificationResponse: (r) {},
    onDidReceiveBackgroundNotificationResponse: notificationTapBackground,
  );

  int remainingSeconds = 0;
  int totalSeconds = 0;
  Timer? timer;

  service.on("startTimer").listen((event) {
    final incomingDuration = event?["duration"] ?? 1500;
    totalSeconds = (incomingDuration > 0) ? incomingDuration : totalSeconds;
    remainingSeconds = (incomingDuration > 0)
        ? incomingDuration
        : remainingSeconds;
    timer?.cancel();

    _showProgressNotification(
      notifications,
      totalSeconds,
      remainingSeconds,
      isRunning: true,
    );

    timer = Timer.periodic(const Duration(seconds: 1), (t) {
      if (remainingSeconds > 0) {
        remainingSeconds--;
      }
      _showProgressNotification(
        notifications,
        totalSeconds,
        remainingSeconds,
        isRunning: true,
      );

      service.invoke("update", {
        "remaining": remainingSeconds,
        "isRunning": true,
        "total": totalSeconds,
      });

      if (remainingSeconds <= 0) {
        timer?.cancel();
        alarmRingtone.stop();
        alarmRingtone.play();
        service.invoke("update", {
          "remaining": 0,
          "isRunning": false,
          "finished": true,
          "total": totalSeconds,
        });
      }
    });
  });

  service.on("pauseTimer").listen((event) {
    timer?.cancel();
    timer = null;
    service.invoke("update", {
      "isRunning": false,
      "paused": true,
      "remaining": remainingSeconds,
      "total": totalSeconds,
    });
    _showProgressNotification(
      notifications,
      totalSeconds,
      remainingSeconds,
      isPaused: true,
    );
  });

  service.on("resetTimer").listen((event) async {
    timer?.cancel();
    timer = null;
    remainingSeconds = totalSeconds;
    service.invoke("update", {
      "remaining": remainingSeconds,
      "isRunning": false,
      "reset": true,
      "total": totalSeconds,
    });
    await notifications.cancel(888);
    await flutterLocalNotificationsPlugin.cancelAll();
    await Future.delayed(const Duration(milliseconds: 200));
    service.stopSelf();
  });
}

void _showProgressNotification(
  FlutterLocalNotificationsPlugin notifications,
  int totalSeconds,
  int remainingSeconds, {
  String title = "Pomodoro Timer",
  String? body,
  bool isRunning = false,
  bool isPaused = false,
}) {
  final completed = (totalSeconds - remainingSeconds).clamp(0, totalSeconds);

  final actions = <AndroidNotificationAction>[];
  if (isRunning) {
    actions.add(
      AndroidNotificationAction('pause', 'Pause', showsUserInterface: false),
    );
  } else if (isPaused) {
    actions.add(
      AndroidNotificationAction('resume', 'Resume', showsUserInterface: false),
    );
  }
  actions.add(
    AndroidNotificationAction('reset', 'Reset', showsUserInterface: false),
  );

  final androidDetails = AndroidNotificationDetails(
    'pomodoro_channel',
    'Pomodoro Timer',
    channelDescription: 'Background Pomodoro Timer',
    ongoing: true,
    importance: Importance.low,
    priority: Priority.low,
    onlyAlertOnce: true,
    showProgress: true,
    maxProgress: totalSeconds > 0 ? totalSeconds : 100,
    progress: completed,
    indeterminate: false,
    actions: actions,
  );

  final notificationDetails = NotificationDetails(android: androidDetails);

  final payload = jsonEncode({
    'total': totalSeconds,
    'remaining': remainingSeconds,
  });

  final text =
      body ??
      "Time left: ${remainingSeconds ~/ 60}:${(remainingSeconds % 60).toString().padLeft(2, '0')}";

  notifications.show(888, title, text, notificationDetails, payload: payload);
}
