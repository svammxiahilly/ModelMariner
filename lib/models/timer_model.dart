import 'package:flutter/material.dart';

class TimerModel {
  String id;
  String label;
  int initialSeconds;
  bool isRunning;
  int currentDuration;
  int? lastStartEpochMs;
  String uiLabel;

  final ValueNotifier<int> remainingNotifier;

  TimerModel({
    required this.id,
    required this.label,
    required this.uiLabel,
    required int remainingSeconds,
    required this.initialSeconds,
    required this.isRunning,
    required this.lastStartEpochMs,
  }) : currentDuration = remainingSeconds,
       remainingNotifier = ValueNotifier(remainingSeconds);

  int get remainingSeconds => remainingNotifier.value;
  set remainingSeconds(int val) => remainingNotifier.value = val;

  factory TimerModel.fromMap(Map<String, dynamic> m) => TimerModel(
    id: m['id'],
    label: m['label'],
    uiLabel: m['uiLabel'],
    remainingSeconds: m['remainingSeconds'],
    initialSeconds: m['initialSeconds'],
    isRunning: m['isRunning'],
    lastStartEpochMs: m['lastStartEpochMs'],
  )..currentDuration = (m['currentDuration'] ?? m['remainingSeconds']);

  Map<String, dynamic> toMap() => {
    'id': id,
    'label': label,
    'uiLabel': uiLabel,
    'remainingSeconds': remainingSeconds,
    'initialSeconds': initialSeconds,
    'isRunning': isRunning,
    'lastStartEpochMs': lastStartEpochMs,
    'currentDuration': currentDuration,
  };

  void updateFrom(TimerModel other) {
    label = other.label;
    uiLabel = other.uiLabel;
    remainingSeconds = other.remainingSeconds;
    initialSeconds = other.initialSeconds;
    isRunning = other.isRunning;
    lastStartEpochMs = other.lastStartEpochMs;
    currentDuration = other.currentDuration;

    remainingNotifier.value = remainingSeconds;
  }
}
