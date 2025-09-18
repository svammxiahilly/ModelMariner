import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:settings_tiles/settings_tiles.dart';
import '../../controllers/theme_controller.dart';
import 'package:provider/provider.dart';
import '../../helpers/preferences_helper.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import '../../helpers/icon_helper.dart';

class ScreenSaverSettingScreen extends StatefulWidget {
  const ScreenSaverSettingScreen({super.key});

  @override
  State<ScreenSaverSettingScreen> createState() =>
      _ScreenSaverSettingScreenState();
}

class _ScreenSaverSettingScreenState extends State<ScreenSaverSettingScreen> {
  double _sliderValueFraction =
      PreferencesHelper.getDouble("screensaverBrightness") ?? 0.3;

  @override
  Widget build(BuildContext context) {
    final themeController = Provider.of<ThemeController>(context);
    final currentScreenSaverClockStyle =
        PreferencesHelper.getString("ScreenSaverClockStyle") ?? "Analog";

    final optionsScreenSaverClockStyle = {
      "Analog": "clock_style_analog".tr(),
      "Digital": "clock_style_digital".tr(),
    };

    final useFullBlackForScreenSaver =
        PreferencesHelper.getBool('FullBlackScreenSaver') ?? false;

    final isLight = Theme.of(context).brightness == Brightness.light;

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surface,
      body: CustomScrollView(
        slivers: [
          SliverAppBar.large(
            title: Text('screen_saver'.tr()),
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
                      icon: IconWithWeight(Symbols.brightness_7, fill: 1),
                      title: Text("screen_saver_brightness".tr()),
                      dialogTitle: "brightness".tr(),
                      initialValue: _sliderValueFraction,
                      min: 0.0,
                      max: 1.0,
                      divisions: 10,
                      value: SettingTileValue(
                        "${(_sliderValueFraction * 100).round()}%",
                      ),
                      label: (value) => "${(value * 100).round()}%",
                      onSubmitted: (v) {
                        setState(() {
                          _sliderValueFraction = v;
                        });
                        PreferencesHelper.setDouble('screensaverBrightness', v);
                      },
                    ),
                    SettingSwitchTile(
                      icon: IconWithWeight(Symbols.backlight_high_off, fill: 1),
                      title: Text("night_mode_screen_saver".tr()),
                      description: Text("night_mode_screen_saver_sub".tr()),
                      toggled: useFullBlackForScreenSaver,
                      onChanged: (value) async {
                        PreferencesHelper.setBool(
                          "FullBlackScreenSaver",
                          value,
                        );
                        setState(() {});
                      },
                    ),
                    SettingSingleOptionTile(
                      icon: IconWithWeight(Symbols.farsight_digital, fill: 1),
                      title: Text('Clock style'),
                      value: SettingTileValue(
                        optionsScreenSaverClockStyle[currentScreenSaverClockStyle]!,
                      ),
                      dialogTitle: 'Clock style',
                      options: optionsScreenSaverClockStyle.values.toList(),
                      initialOption:
                          optionsScreenSaverClockStyle[currentScreenSaverClockStyle]!,
                      onSubmitted: (value) {
                        final selectedKey = optionsScreenSaverClockStyle.entries
                            .firstWhere((e) => e.value == value)
                            .key;
                        PreferencesHelper.setString(
                          'ScreenSaverClockStyle',
                          selectedKey,
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
