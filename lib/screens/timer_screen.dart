import 'package:easy_localization/easy_localization.dart';
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
import '../widgets/wave_circularprogress.dart';
import '../services/timer_service.dart';
import 'package:flutter_slidable/flutter_slidable.dart';

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
  bool shouldRing = true;

  bool _isAppVisible = false;

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
  void didChangeAppLifecycleState(AppLifecycleState state) async {
    super.didChangeAppLifecycleState(state);
    _isAppVisible = state == AppLifecycleState.resumed;
    shouldRing = state == AppLifecycleState.resumed;

    if (state == AppLifecycleState.paused) {
      if (timers.any((t) => t.isRunning)) {
        await _startServiceIfNotRunning();
      }
    } else if (state == AppLifecycleState.resumed) {
      if (await FlutterForegroundTask.isRunningService) {
        FlutterForegroundTask.sendDataToTask("STOP_ALARM");
        await FlutterForegroundTask.stopService();
      }
    }
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
        final baselineDuration = t.currentDuration;
        final elapsed = ((now - t.lastStartEpochMs!) / 1000).floor();

        final newRemaining = (baselineDuration - elapsed).clamp(0, 99999999);

        if (newRemaining != t.remainingSeconds) {
          t.remainingSeconds = newRemaining;
        }
      }
    }

    if (mounted) setState(() {});
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
          // UPDATE IN-PLACE instead of replacing:
          timers[idx].updateFrom(updated);
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
        (data['type'] == 'timers_updated' || data['type'] == 'timers_tick') &&
        data['timers'] != null) {
      final incoming = (data['timers'] as List)
          .map((e) => TimerModel.fromMap(Map<String, dynamic>.from(e)))
          .toList();

      for (final updated in incoming) {
        final idx = timers.indexWhere((t) => t.id == updated.id);
        if (idx != -1) {
          // you already partly do this; keep updating fields individually
          timers[idx].isRunning = updated.isRunning;
          timers[idx].lastStartEpochMs = updated.lastStartEpochMs;
          timers[idx].currentDuration = updated.currentDuration;
          timers[idx].remainingSeconds = updated.remainingSeconds;
          // ensure notifier triggers:
          timers[idx].remainingNotifier.value = updated.remainingSeconds;
        } else {
          timers.add(updated);
        }
      }

      if (mounted) setState(() {});
      return;
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
      // await _startServiceIfNotRunning();
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
            final newRem = (t.currentDuration - elapsedSec).clamp(0, 99999999);
            t.remainingSeconds = newRem;
            if (t.remainingSeconds <= 0) {
              t.isRunning = false;
              t.lastStartEpochMs = null;
              t.currentDuration = 0;
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
      // await _startServiceIfNotRunning();
    }
  }

  Future<void> _toggleStartStop(TimerModel t) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    final idx = timers.indexWhere((x) => x.id == t.id);
    if (idx == -1) return;
    final model = timers[idx];
    final prefs = await SharedPreferences.getInstance();
    if (model.isRunning) {
      alarmRingtone.stop();
      shouldRing = false;
    } else {
      shouldRing = true;
    }
    if (model.isRunning) {
      // pause
      if (model.lastStartEpochMs != null) {
        final elapsed = ((now - model.lastStartEpochMs!) / 1000).floor();
        model.remainingSeconds = (model.currentDuration - elapsed).clamp(
          0,
          99999999,
        );
        model.currentDuration = model.remainingSeconds;
      }
      model.isRunning = false;
      model.lastStartEpochMs = null;
      final activeId = prefs.getString('activeTimerId');
      if (activeId != null && activeId == model.id) {
        await prefs.remove('activeTimerId');
      }
    } else {
      // start
      model.currentDuration = model.remainingSeconds;
      model.lastStartEpochMs = now;
      model.isRunning = true;
      await prefs.setString('activeTimerId', model.id);
    }

    await _persistTimers();
    await FlutterForegroundTask.stopService();

    setState(() {});

    if (timers.any((x) => x.isRunning)) {
      // await _startServiceIfNotRunning();
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

    final prefs = await SharedPreferences.getInstance();
    final activeId = prefs.getString('activeTimerId');
    if (activeId != null && activeId == t.id) {
      await prefs.remove('activeTimerId');
    }
    await _persistTimers();
    await FlutterForegroundTask.stopService();
    setState(() {});
    await _stopServiceIfNoRunningTimers();

    alarmRingtone.stop();
    shouldRing = true;
  }

  bool startAfterAddingOneMin = false;

  Future<void> _addOneMinute(TimerModel t) async {
    final idx = timers.indexWhere((x) => x.id == t.id);
    if (idx == -1) return;

    timers[idx].remainingSeconds += 60;
    timers[idx].currentDuration =
        (timers[idx].currentDuration ?? timers[idx].remainingSeconds) + 60;

    await _persistTimers();
    setState(() {});

    alarmRingtone.stop();
    shouldRing = true;
  }

  Future<void> _deleteTimer(TimerModel t) async {
    timers.removeWhere((x) => x.id == t.id);
    await _persistTimers();
    await FlutterForegroundTask.stopService();
    setState(() {});
    alarmRingtone.stop();
    shouldRing = true;
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
        title: Text('edit_timer_label'.tr()),
        content: TextField(
          controller: controller,
          decoration: InputDecoration(
            hintText: 'Enter label',
            border: OutlineInputBorder(),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text(
              'cancel'.tr(),
              style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 16),
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
              'save'.tr(),
              style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 16),
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
                        width: 65,

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
                        width: 70,

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
                        width: 65,

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
                                "no_timers".tr(),
                                style: TextStyle(
                                  fontSize: 30,
                                  color: colorTheme.onSurfaceVariant,
                                ),
                              ),
                            ],
                          ),
                        ),
                      )
                    : ReorderableListView.builder(
                        itemCount: timers.length,
                        onReorder: (oldIndex, newIndex) async {
                          if (newIndex > oldIndex) newIndex--;
                          final movedAlarm = timers.removeAt(oldIndex);
                          timers.insert(newIndex, movedAlarm);

                          setState(() {});

                          await _persistTimers();
                        },
                        proxyDecorator: (child, index, animation) {
                          return Material(
                            type: MaterialType.transparency,
                            child: child,
                          );
                        },
                        itemBuilder: (ctx, i) {
                          final t = timers[i];
                          return Padding(
                            key: ValueKey(t.id),

                            padding: const EdgeInsets.only(bottom: 6),
                            child: Slidable(
                              key: ValueKey(t.id),
                              closeOnScroll: true,
                              endActionPane: ActionPane(
                                motion: StretchMotion(),
                                children: [
                                  CustomSlidableAction(
                                    onPressed: (context) async {
                                      _deleteTimer(t);
                                      ScaffoldMessenger.of(
                                        context,
                                      ).showSnackBar(
                                        SnackBar(
                                          content: Text('timer_deleted'.tr()),
                                        ),
                                      );
                                    },

                                    borderRadius: BorderRadius.circular(999),
                                    backgroundColor: colorTheme.errorContainer,
                                    child: Icon(
                                      Icons.delete,
                                      color: colorTheme.onErrorContainer,
                                      size: 30,
                                    ),
                                  ),
                                ],
                              ),
                              child: ValueListenableBuilder<int>(
                                valueListenable: t.remainingNotifier,
                                builder: (context, remaining, _) {
                                  final denominator = t.initialSeconds;
                                  final progress = denominator > 0
                                      ? (1.0 -
                                                (t.remainingSeconds /
                                                    denominator))
                                            .clamp(0.0, 1.0)
                                      : 0.0;

                                  final tileColor = remaining == 0
                                      ? colorTheme.primaryContainer
                                      : colorTheme.surfaceContainerLow;

                                  final finished = remaining == 0;

                                  if (finished) {
                                    if (shouldRing && _isAppVisible) {
                                      alarmRingtone.stop();
                                      alarmRingtone.play();
                                    }
                                    shouldRing = false;
                                  }

                                  final isLast = i == timers.length - 1;

                                  return OpenContainer(
                                    closedElevation: 0,
                                    closedColor: tileColor,
                                    transitionDuration: Duration(
                                      milliseconds: 500,
                                    ),
                                    openElevation: 0,
                                    closedShape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(22),
                                    ),
                                    openShape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(50),
                                    ),
                                    transitionType:
                                        ContainerTransitionType.fadeThrough,
                                    openColor: colorTheme.surface,
                                    closedBuilder: (context, openContainer) {
                                      return Container(
                                        padding: const EdgeInsets.only(
                                          top: 5,
                                          bottom: 5,
                                          right: 10,
                                          left: 5,
                                        ),
                                        child: Column(
                                          crossAxisAlignment:
                                              CrossAxisAlignment.start,
                                          children: [
                                            Row(
                                              mainAxisAlignment:
                                                  MainAxisAlignment
                                                      .spaceBetween,

                                              children: [
                                                Row(
                                                  crossAxisAlignment:
                                                      CrossAxisAlignment.end,

                                                  children: [
                                                    Stack(
                                                      alignment:
                                                          Alignment.center,
                                                      children: [
                                                        Positioned(
                                                          child: SizedBox(
                                                            width: 80,
                                                            height: 80,
                                                            child: WavyCircularProgress(
                                                              progress:
                                                                  progress,
                                                              strokeWidth: 7,
                                                              amplitude: 2.0,
                                                              gapAngle: 0.15,
                                                              gapAngleStartValue:
                                                                  0.05,
                                                            ),
                                                          ),
                                                        ),
                                                        Positioned(
                                                          child: IconButton(
                                                            onPressed: () {
                                                              _toggleStartStop(
                                                                t,
                                                              );
                                                            },
                                                            icon: Icon(
                                                              t.isRunning
                                                                  ? Icons.pause
                                                                  : Icons
                                                                        .play_arrow,
                                                              size: 30,
                                                            ),
                                                          ),
                                                        ),
                                                      ],
                                                    ),
                                                    SizedBox(width: 6),
                                                    Column(
                                                      crossAxisAlignment:
                                                          CrossAxisAlignment
                                                              .start,
                                                      mainAxisAlignment:
                                                          MainAxisAlignment
                                                              .center,
                                                      children: [
                                                        SizedBox(
                                                          width:
                                                              MediaQuery.of(
                                                                context,
                                                              ).size.width /
                                                              2.1,
                                                          child: Text(
                                                            t.uiLabel.isNotEmpty
                                                                ? t.uiLabel
                                                                : 'timer_label'
                                                                      .tr(),
                                                            style: TextStyle(
                                                              height: 1,
                                                              fontSize: 15,
                                                              fontWeight:
                                                                  FontWeight
                                                                      .w600,
                                                              color: colorTheme
                                                                  .secondary,
                                                            ),
                                                            maxLines: 1,
                                                            overflow:
                                                                TextOverflow
                                                                    .ellipsis,
                                                          ),
                                                        ),

                                                        Text(
                                                          _format(remaining),
                                                          style: TextStyle(
                                                            fontSize: 40,
                                                            fontFamily:
                                                                "FlexFontEn",
                                                            fontWeight:
                                                                FontWeight.w600,

                                                            color: t.isRunning
                                                                ? colorTheme
                                                                      .onSurface
                                                                : colorTheme
                                                                      .onSurfaceVariant,
                                                          ),
                                                        ),
                                                      ],
                                                    ),
                                                  ],
                                                ),

                                                // buttons
                                                Column(
                                                  spacing: 3,
                                                  children: [
                                                    SizedBox(
                                                      width: 42,
                                                      height: 35,
                                                      child: IconButton.filled(
                                                        tooltip: "Edit label",
                                                        onPressed: () =>
                                                            _showEditLabelDialog(
                                                              t,
                                                            ),
                                                        icon: Icon(
                                                          Icons.edit,
                                                          size: 18,
                                                        ),
                                                        style: ButtonStyle(
                                                          backgroundColor:
                                                              WidgetStateProperty.all(
                                                                colorTheme
                                                                    .surfaceContainerLowest,
                                                              ),
                                                          foregroundColor:
                                                              WidgetStateProperty.all(
                                                                colorTheme
                                                                    .onSurface,
                                                              ),
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
                                                                    : colorTheme
                                                                          .surfaceContainerLowest,
                                                              ),
                                                          foregroundColor:
                                                              WidgetStateProperty.all(
                                                                colorTheme
                                                                    .onSurface,
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
