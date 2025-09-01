import 'package:clockmaster/helpers/icon_helper.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/alarm_data_model.dart';
import '../services/alarm_service.dart';
import '../screens/alarm_edit_screen.dart';
import '../utils/snack_util.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import '../notifiers/settings_notifier.dart';
import 'package:provider/provider.dart';
import 'dart:async';

class AlarmScreen extends StatefulWidget {
  const AlarmScreen({super.key});

  @override
  State<AlarmScreen> createState() => AlarmScreenState();
}

class AlarmScreenState extends State<AlarmScreen> {
  List<Alarm> alarms = [];

  bool get is24HourFormat =>
      context.watch<UnitSettingsNotifier>().timeFormat == "24 hr";

  @override
  void initState() {
    super.initState();
    load();
  }

  Future<void> load() async {
    final loaded = await AlarmService.instance.loadAlarms();

    setState(() => alarms = loaded);
  }

  Future<void> _saveAndSchedule(Alarm alarm) async {
    await AlarmService.instance.saveAndSchedule(alarm);
    load();
  }

  Future<void> _delete(Alarm alarm) async {
    alarms.removeWhere((a) => a.id == alarm.id);

    await AlarmService.instance.saveAlarms(alarms);

    await AlarmService.instance.cancelAlarm(alarm.id);

    await load();
  }

  Future<void> _openEditor({Alarm? edit}) async {
    showModalBottomSheet<Alarm>(
      context: context,
      isScrollControlled: true,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
        ),
        child: AlarmEditContent(alarm: edit),
      ),
    ).then((result) async {
      if (result is Alarm) {
        await _saveAndSchedule(result);
      }
    });
  }

  String formatTimeSeparateAmPm(Alarm a) {
    final dt = DateTime(0, 1, 1, a.hour, a.minute);
    final fullTime = DateFormat.jm().format(dt);

    final parts = fullTime.split(' ');
    final time = parts[0];
    final ampm = parts.length > 1 ? parts[1] : '';

    return '$time $ampm';
  }

  Map<String, String> getTimeAndAmPm(Alarm a) {
    final dt = DateTime(0, 1, 1, a.hour, a.minute);

    if (is24HourFormat) {
      final fullTime = DateFormat.Hm().format(dt);
      return {'time': fullTime, 'ampm': ''};
    } else {
      final fullTime = DateFormat.jm().format(dt);
      final regex = RegExp(
        r'(\d{1,2}:\d{2})(?:\s*)?(AM|PM)?',
        caseSensitive: false,
      );
      final match = regex.firstMatch(fullTime);

      if (match != null) {
        final time = match.group(1) ?? '';
        final ampm = match.group(2) ?? '';
        return {'time': time, 'ampm': ampm};
      }
      return {'time': fullTime, 'ampm': ''};
    }
  }

  String _repeatDaysText(List<int> days) {
    if (days.isEmpty) return 'One-time';
    const names = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    if (days.length == 7) return 'Every day';
    return days.map((d) => names[d - 1]).join(', ');
  }

  @override
  Widget build(BuildContext context) {
    final colorTheme = Theme.of(context).colorScheme;
    final themeBrightness = Theme.of(context).brightness;
    // - ${a.label}
    return Scaffold(
      body: alarms.isEmpty
          ? Center(
              child: Opacity(
                opacity: 0.6,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    IconWithWeight(
                      Symbols.alarm_off,
                      color: colorTheme.onSurfaceVariant,
                      size: 60,
                    ),
                    Text(
                      "No alarms",
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
              padding: const EdgeInsets.fromLTRB(13, 0, 13, 130),
              onReorder: (oldIndex, newIndex) async {
                if (newIndex > oldIndex) newIndex--;
                final movedAlarm = alarms.removeAt(oldIndex);
                alarms.insert(newIndex, movedAlarm);

                setState(() {});

                await AlarmService.instance.saveAlarms(alarms);
              },

              proxyDecorator: (child, index, animation) {
                return Material(type: MaterialType.transparency, child: child);
              },
              itemCount: alarms.length,
              itemBuilder: (context, i) {
                final a = alarms[i];
                final resultAlarms = getTimeAndAmPm(a);
                final isLast = i == alarms.length - 1;
                final isFirst = i == 0;
                final isOnly = alarms.length == 1;

                final firstBorderRadius = BorderRadius.only(
                  topLeft: Radius.circular(18),
                  topRight: Radius.circular(18),
                  bottomLeft: Radius.circular(2.6),
                  bottomRight: Radius.circular(2.6),
                );
                final lastBorderRadius = BorderRadius.only(
                  bottomLeft: Radius.circular(18),
                  bottomRight: Radius.circular(18),
                  topLeft: Radius.circular(2.6),
                  topRight: Radius.circular(2.6),
                );

                return Padding(
                  padding: const EdgeInsets.only(bottom: 2),
                  key: ValueKey(a.id),

                  child: ClipRRect(
                    borderRadius: isOnly
                        ? BorderRadius.circular(18)
                        : isFirst
                        ? firstBorderRadius
                        : isLast
                        ? lastBorderRadius
                        : BorderRadius.zero,

                    child: Dismissible(
                      key: ValueKey(a.id),

                      direction: DismissDirection.endToStart,
                      background: Container(
                        alignment: Alignment.centerRight,
                        padding: EdgeInsets.symmetric(horizontal: 30),
                        decoration: BoxDecoration(
                          color: colorTheme.errorContainer,
                        ),
                        child: Icon(
                          Icons.delete,
                          color: colorTheme.onErrorContainer,
                          size: 40,
                        ),
                      ),
                      onDismissed: (direction) {
                        _delete(a);
                        SnackUtil.showSnackBar(
                          context: context,
                          message: "Alarm deleted",
                        );
                      },
                      child: GestureDetector(
                        child: Container(
                          padding: const EdgeInsets.only(
                            left: 16,
                            right: 16,
                            bottom: 10,
                            top: 10,
                          ),
                          decoration: BoxDecoration(
                            color: a.enabled
                                ? themeBrightness == Brightness.dark
                                      ? colorTheme.onPrimary
                                      : colorTheme.primaryContainer
                                : colorTheme.surfaceContainerLow,
                          ),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    _repeatDaysText(a.repeatDays),
                                    style: TextStyle(
                                      fontSize: 16,
                                      color: colorTheme.onSurfaceVariant,
                                    ),
                                  ),
                                  SizedBox(height: 2),
                                  RichText(
                                    text: TextSpan(
                                      children: [
                                        TextSpan(
                                          text: resultAlarms['time'],
                                          style: TextStyle(
                                            fontSize: 53,
                                            fontFamily: "FlexFontEn",
                                            fontWeight: FontWeight.w600,
                                            color: a.enabled
                                                ? colorTheme.onSurface
                                                : colorTheme.onSurfaceVariant,
                                            height: 1.25,
                                          ),
                                        ),
                                        TextSpan(text: " "),
                                        WidgetSpan(
                                          alignment:
                                              PlaceholderAlignment.baseline,
                                          baseline: TextBaseline.alphabetic,
                                          child: Text(
                                            resultAlarms['ampm'].toString(),
                                            style: TextStyle(
                                              fontSize: 26,
                                              color:
                                                  colorTheme.onSurfaceVariant,
                                            ),
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                  SizedBox(
                                    width:
                                        MediaQuery.of(context).size.width / 1.7,
                                    child: Text(
                                      a.label,
                                      style: TextStyle(
                                        height: 1,
                                        fontSize: 16,
                                        color: colorTheme.secondary,
                                        fontWeight: FontWeight.w500,
                                      ),
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  ),
                                ],
                              ),
                              // Column(
                              //   mainAxisAlignment: MainAxisAlignment.spaceBetween,

                              //   children: [
                              Switch(
                                value: a.enabled,
                                thumbIcon:
                                    WidgetStateProperty.resolveWith<Icon?>(
                                      (states) =>
                                          states.contains(WidgetState.selected)
                                          ? Icon(
                                              Icons.notifications_active,
                                              color: colorTheme.primary,
                                            )
                                          : null,
                                    ),
                                onChanged: (v) async {
                                  a.enabled = v;
                                  await _saveAndSchedule(a);
                                  setState(() {});
                                },
                                // ),
                                // ],
                              ),
                            ],
                          ),
                        ),

                        onTap: () {
                          showModalBottomSheet<Alarm>(
                            context: context,
                            isScrollControlled: true,
                            backgroundColor: colorTheme.surface,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.vertical(
                                top: Radius.circular(28),
                              ),
                            ),
                            builder: (context) => Padding(
                              padding: EdgeInsets.only(
                                bottom: MediaQuery.of(
                                  context,
                                ).viewInsets.bottom,
                              ),
                              child: AlarmEditContent(
                                alarm: a,
                                is24HourFormat: is24HourFormat,
                                onDelete: a != null ? () => _delete(a) : null,
                              ),
                            ),
                          ).then((result) async {
                            if (result is Alarm) {
                              await _saveAndSchedule(result);
                            }
                          });
                        },
                      ),
                    ),
                  ),
                );
              },
            ),
    );
  }
}
