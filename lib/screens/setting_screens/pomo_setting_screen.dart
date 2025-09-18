import 'package:clockmaster/helpers/preferences_helper.dart';
import 'package:flutter/material.dart';
import 'package:settings_tiles/settings_tiles.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import '../../helpers/icon_helper.dart';

class PomoSettingScreen extends StatefulWidget {
  const PomoSettingScreen({super.key});

  @override
  State<PomoSettingScreen> createState() => _PomoSettingScreenState();
}

class _PomoSettingScreenState extends State<PomoSettingScreen> {
  @override
  Widget build(BuildContext context) {
    final currentFocusTime = PreferencesHelper.getInt("pomoFocusTime") ?? 25;
    final currentShortBreakTime =
        PreferencesHelper.getInt("pomoShortBreakTime") ?? 5;

    final currentLongBreakTime =
        PreferencesHelper.getInt("pomoLongBreakTime") ?? 15;

    final cyclesBeforeLongBreak =
        PreferencesHelper.getInt("pomoCyclesBeforeLongBreak") ?? 4;

    final autoStartSessionpomo =
        PreferencesHelper.getBool("autoStartSessionpomo") ?? false;

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surface,
      body: CustomScrollView(
        slivers: [
          SliverAppBar.large(
            title: Text('Pomodoro'),
            titleSpacing: 0,
            backgroundColor: Theme.of(context).colorScheme.surface,
            scrolledUnderElevation: 1,
          ),

          SliverToBoxAdapter(
            child: Column(
              children: [
                SettingSection(
                  styleTile: true,
                  title: SettingSectionTitle("General", noPadding: true),
                  tiles: [
                    SettingSliderTile(
                      icon: IconWithWeight(
                        Symbols.center_focus_strong,
                        fill: 1,
                      ),

                      title: Text("Focus length"),
                      value: SettingTileValue(
                        "${currentFocusTime.toString()}  ${currentShortBreakTime > 1 ? "min" : "mins"}",
                      ),
                      dialogTitle: "Focus",
                      min: 15,
                      max: 60,
                      divisions: 45,
                      initialValue: 25,
                      label: (value) =>
                          "${(value).round()} ${value > 1 ? "min" : "mins"}",
                      onSubmitted: (value) {
                        PreferencesHelper.setInt(
                          "pomoFocusTime",
                          value.round(),
                        );
                        setState(() {});
                      },
                    ),

                    SettingSliderTile(
                      icon: IconWithWeight(Symbols.hourglass_pause, fill: 1),

                      title: Text("Short break length"),
                      value: SettingTileValue(
                        "${currentShortBreakTime.toString()}  ${currentShortBreakTime > 1 ? "min" : "mins"}",
                      ),
                      dialogTitle: "Short break",
                      min: 1,
                      max: 15,
                      divisions: 15,
                      initialValue: 5,
                      label: (value) =>
                          "${(value).round()} ${value > 1 ? "min" : "mins"}",
                      onSubmitted: (value) {
                        PreferencesHelper.setInt(
                          "pomoShortBreakTime",
                          value.round(),
                        );
                        setState(() {});
                      },
                    ),

                    SettingSliderTile(
                      icon: IconWithWeight(Symbols.coffee, fill: 1),

                      title: Text("Long break length"),
                      value: SettingTileValue(
                        "${currentLongBreakTime.toString()}  ${currentLongBreakTime > 1 ? "min" : "mins"}",
                      ),
                      dialogTitle: "Long break",
                      min: 5,
                      max: 60,
                      divisions: 45,
                      initialValue: 15,
                      label: (value) =>
                          "${(value).round()} ${value > 1 ? "min" : "mins"}",
                      onSubmitted: (value) {
                        PreferencesHelper.setInt(
                          "pomoLongBreakTime",
                          value.round(),
                        );
                        setState(() {});
                      },
                    ),
                  ],
                ),
                SizedBox(height: 10),

                SettingSection(
                  styleTile: true,
                  title: SettingSectionTitle("Sessions", noPadding: true),

                  tiles: [
                    SettingSwitchTile(
                      icon: IconWithWeight(Symbols.autostop, fill: 1),
                      title: Text("Auto-Start Sessions"),
                      description: Text(
                        "Automatically begin the next focus or break session without manual input",
                      ),
                      toggled: autoStartSessionpomo,
                      onChanged: (value) {
                        PreferencesHelper.setBool(
                          "autoStartSessionpomo",
                          value,
                        );
                        setState(() {});
                      },
                    ),

                    SettingSliderTile(
                      icon: IconWithWeight(Symbols.cycle, fill: 1),

                      title: Text("Focus Cycles Before Long Break"),
                      value: SettingTileValue(cyclesBeforeLongBreak.toString()),

                      dialogTitle: "Focus Cycles",
                      min: 4,
                      max: 10,
                      divisions: 6,
                      initialValue: 4,
                      label: (value) => "${(value).round()}",
                      onSubmitted: (value) {
                        PreferencesHelper.setInt(
                          "pomoCyclesBeforeLongBreak",
                          value.round(),
                        );
                        setState(() {});
                      },
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
