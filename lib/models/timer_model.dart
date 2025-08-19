import 'package:flutter/material.dart';

class TimerModel {
  String id;
  String label;
  int initialSeconds;
  bool isRunning;
  int currentDuration;
  int? lastStartEpochMs;

  final ValueNotifier<int> remainingNotifier;

  TimerModel({
    required this.id,
    required this.label,
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
    remainingSeconds: m['remainingSeconds'],
    initialSeconds: m['initialSeconds'],
    isRunning: m['isRunning'],
    lastStartEpochMs: m['lastStartEpochMs'],
  );

  Map<String, dynamic> toMap() => {
    'id': id,
    'label': label,
    'remainingSeconds': remainingSeconds,
    'initialSeconds': initialSeconds,
    'isRunning': isRunning,
    'lastStartEpochMs': lastStartEpochMs,
  };
}
