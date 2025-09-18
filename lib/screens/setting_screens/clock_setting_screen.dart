import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:settings_tiles/settings_tiles.dart';
import '../../controllers/theme_controller.dart';
import 'package:provider/provider.dart';
import '../../helpers/preferences_helper.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import '../../helpers/icon_helper.dart';
import '../../notifiers/settings_notifier.dart';

class ClockSettingScreen extends StatefulWidget {
  const ClockSettingScreen({super.key});

  @override
  State<ClockSettingScreen> createState() => _ClockSettingScreenState();
}

class _ClockSettingScreenState extends State<ClockSettingScreen> {
  @override
  Widget build(BuildContext context) {
    final currentTimeFormat =
        PreferencesHelper.getString("timeFormat") ?? "12 hr";
    final currentShowSeconds =
        PreferencesHelper.getBool("showSeconds") ?? false;
    final currentClockStyle =
        PreferencesHelper.getString("ClockStyle") ?? "Digital";

    final optionsTimeFormat = {
      "12 hr": "12 ${'hr'.tr()}",
      "24 hr": "24 ${'hr'.tr()}",
    };

    final optionsClockStyle = {
      "Analog": "clock_style_analog".tr(),
      "Digital": "clock_style_digital".tr(),
    };

    final isLight = Theme.of(context).brightness == Brightness.light;

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surface,
      body: CustomScrollView(
        slivers: [
          SliverAppBar.large(
            title: Text('clock'.tr()),
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
                    SettingSingleOptionTile(
                      icon: IconWithWeight(Symbols.farsight_digital, fill: 1),
                      title: Text('clock_style'.tr()),
                      value: SettingTileValue(
                        optionsClockStyle[currentClockStyle]!,
                      ),
                      dialogTitle: 'clock_style'.tr(),
                      options: optionsClockStyle.values.toList(),
                      initialOption: optionsClockStyle[currentClockStyle]!,
                      onSubmitted: (value) {
                        final selectedKey = optionsClockStyle.entries
                            .firstWhere((e) => e.value == value)
                            .key;
                        context
                            .read<UnitSettingsNotifier>()
                            .updateClockStyleMain(selectedKey);
                        setState(() {});
                      },
                    ),
                    SettingSingleOptionTile(
                      icon: IconWithWeight(
                        Symbols.nest_clock_farsight_analog,
                        fill: 1,
                      ),
                      title: Text('time_format'.tr()),
                      value: SettingTileValue(
                        optionsTimeFormat[currentTimeFormat]!,
                      ),
                      dialogTitle: 'time_format'.tr(),
                      options: optionsTimeFormat.values.toList(),
                      initialOption: optionsTimeFormat[currentTimeFormat]!,
                      onSubmitted: (value) {
                        final selectedKey = optionsTimeFormat.entries
                            .firstWhere((e) => e.value == value)
                            .key;
                        context.read<UnitSettingsNotifier>().updateTimeUnit(
                          selectedKey,
                        );
                        setState(() {});
                      },
                    ),
                    SettingSwitchTile(
                      icon: IconWithWeight(Symbols.timer_10_select, fill: 1),
                      title: Text("display_time_seconds".tr()),
                      toggled: currentShowSeconds,
                      onChanged: (value) {
                        context.read<UnitSettingsNotifier>().updateShowSeconds(
                          value,
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
