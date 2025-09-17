import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import '../helpers/icon_helper.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import '../widgets/wave_circularprogress.dart';
import '../models/timer_model.dart';
import '../helpers/preferences_helper.dart';

class TimerDetailPage extends StatefulWidget {
  final TimerModel timer;
  final VoidCallback onToggle;
  final VoidCallback onReset;
  final VoidCallback onAddMinute;
  final VoidCallback onDelete;

  const TimerDetailPage({
    super.key,
    required this.timer,
    required this.onToggle,
    required this.onReset,
    required this.onAddMinute,
    required this.onDelete,
  });

  @override
  State<TimerDetailPage> createState() => _TimerDetailPageState();
}

class _TimerDetailPageState extends State<TimerDetailPage> {
  @override
  void initState() {
    super.initState();
    PreferencesHelper.setBool("isFullScreen", true);
  }

  @override
  void dispose() {
    PreferencesHelper.setBool("isFullScreen", false);
    super.dispose();
  }

  String _format(int seconds) {
    final h = seconds ~/ 3600;
    final m = (seconds % 3600) ~/ 60;
    final s = seconds % 60;
    if (h > 0) {
      return '${h}:${m.toString().padLeft(2, '0')}:${s.toString().padLeft(2, '0')}';
    } else {
      return '${m}:${s.toString().padLeft(2, '0')}';
    }
  }

  @override
  Widget build(BuildContext context) {
    final colorTheme = Theme.of(context).colorScheme;

    return ValueListenableBuilder<int>(
      valueListenable: widget.timer.remainingNotifier,
      builder: (context, remaining, _) {
        // final progress = widget.timer.currentDuration > 0
        //     ? 1 - (widget.timer.remainingSeconds / widget.timer.currentDuration)
        //     : 0.0;

        final denominator =
            (widget.timer.isRunning && widget.timer.currentDuration != null)
            ? widget.timer.currentDuration!
            : widget.timer.initialSeconds;

        final progress = denominator > 0
            ? (1.0 - (widget.timer.remainingSeconds / denominator)).clamp(
                0.0,
                1.0,
              )
            : 0.0;
        final finished = widget.timer.remainingSeconds == 0;

        return Scaffold(
          backgroundColor: finished
              ? colorTheme.inversePrimary
              : colorTheme.surface,
          appBar: AppBar(
            title: Padding(
              padding: const EdgeInsets.only(left: 6),
              child: Text(widget.timer.uiLabel, style: TextStyle(fontSize: 20)),
            ),

            automaticallyImplyLeading: false,
            backgroundColor: finished
                ? colorTheme.inversePrimary
                : colorTheme.surface,
            actions: [
              SizedBox(
                width: 42,
                height: 42,
                child: IconButton(
                  onPressed: () {
                    Navigator.pop(context);
                  },
                  icon: IconWithWeight(Symbols.close_fullscreen),
                ),
              ),
              SizedBox(width: 10),
            ],
          ),
          body: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                SizedBox(
                  width: MediaQuery.of(context).size.width,
                  height: MediaQuery.of(context).size.width,
                  child: Stack(
                    alignment: Alignment.center,
                    children: [
                      SizedBox(
                        width: MediaQuery.of(context).size.width / 1.08,
                        height: MediaQuery.of(context).size.width / 1.08,
                        child: WavyCircularProgress(progress: progress),
                      ),
                      LayoutBuilder(
                        builder: (context, constraints) {
                          final circleSize =
                              constraints.biggest.shortestSide / 1.1;
                          final text = _format(remaining);

                          final duration = Duration(seconds: remaining);

                          final factor = duration.inHours >= 1 ? 0.7 : 0.75;

                          final fontSize = circleSize / (text.length * factor);

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
                      Positioned(
                        bottom: 70,
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              Symbols.notification_sound,
                              color: colorTheme.onSurface,
                              size: 19,
                              fill: 1,
                            ),
                            const SizedBox(width: 5),
                            Text(
                              _getEndTime(remaining),
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w500,
                                color: colorTheme.onSurfaceVariant,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),

                const SizedBox(height: 40),

                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  spacing: 10,
                  children: [
                    PlayPauseButton(
                      isRunning: widget.timer.isRunning,
                      onToggle: widget.onToggle,
                    ),

                    SizedBox(
                      width: 100,
                      height: 100,
                      child: FloatingActionButton.large(
                        shape: const CircleBorder(),
                        onPressed: widget.onReset,
                        backgroundColor: colorTheme.primaryContainer,
                        heroTag: "idk_cool_random_tag_WOWOWWWWW",

                        elevation: 0,
                        highlightElevation: 0,
                        child: IconWithWeight(
                          Symbols.restart_alt,
                          size: 40,
                          color: colorTheme.onPrimaryContainer,
                        ),
                      ),
                    ),
                  ],
                ),

                SizedBox(height: 10),
                Container(
                  width: 100 + 100,
                  height: 80,
                  clipBehavior: Clip.hardEdge,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(50),
                  ),
                  child: FloatingActionButton.extended(
                    onPressed: widget.onAddMinute,
                    backgroundColor: colorTheme.tertiaryContainer,
                    heroTag: "idk_random_tag_stop_wow_WOWOW",

                    label: Text(
                      "add_one_min_large".tr(),
                      style: TextStyle(
                        fontSize: 24,
                        color: colorTheme.onTertiaryContainer,
                      ),
                    ),
                  ),
                ),
              ],

              // const SizedBox(height: 40),
              // TextButton(
              //   onPressed: () => Navigator.pop(context),
              //   child: const Text("Close"),
              // ),
              // ],
            ),
          ),
        );
      },
    );
  }
}

class PlayPauseButton extends StatefulWidget {
  final bool isRunning;
  final VoidCallback onToggle;

  const PlayPauseButton({
    super.key,
    required this.isRunning,
    required this.onToggle,
  });

  @override
  State<PlayPauseButton> createState() => _PlayPauseButtonState();
}

class _PlayPauseButtonState extends State<PlayPauseButton> {
  late bool _isRunning;

  @override
  void initState() {
    super.initState();
    _isRunning = widget.isRunning;
  }

  @override
  void didUpdateWidget(covariant PlayPauseButton oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.isRunning != widget.isRunning) {
      setState(() => _isRunning = widget.isRunning);
    }
  }

  @override
  Widget build(BuildContext context) {
    final colorTheme = Theme.of(context).colorScheme;
    return GestureDetector(
      onTap: () {
        widget.onToggle();
        setState(() {
          _isRunning = !_isRunning;
        });
      },
      child: TweenAnimationBuilder<double>(
        tween: Tween<double>(end: _isRunning ? 28.0 : 50.0),
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOutBack,
        builder: (context, borderRadiusValue, child) {
          return Container(
            width: 100,
            height: 100,
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
              color: _isRunning ? colorTheme.primary : colorTheme.onPrimary,
              size: 40,
            ),
          ),
        ),
      ),
    );
  }
}

String _getEndTime(int remainingSeconds) {
  final is24HourFormat = PreferencesHelper.getString("timeFormat") == '24 hr';

  final now = DateTime.now();
  final endTime = now.add(Duration(seconds: remainingSeconds));

  if (is24HourFormat) {
    return DateFormat.Hm().format(endTime);
  } else {
    return DateFormat.jm().format(endTime);
  }
}
