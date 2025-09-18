import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:settings_tiles/settings_tiles.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import 'setting_screens/appearance_screen.dart';
import 'setting_screens/clock_setting_screen.dart';
import 'setting_screens/alarm_setting_screen.dart';
import 'setting_screens/screensaver_setting_screen.dart';
import 'setting_screens/pomo_setting_screen.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  @override
  Widget build(BuildContext context) {
    final isLight = Theme.of(context).brightness == Brightness.light;
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surface,
      body: CustomScrollView(
        slivers: [
          SliverAppBar.large(
            title: Text('settings'.tr()),
            titleSpacing: 0,
            backgroundColor: Theme.of(context).colorScheme.surface,
            scrolledUnderElevation: 1,
          ),

          SliverToBoxAdapter(
            child: Column(
              children: [
                SettingSection(
                  styleTile: true,
                  tiles: [
                    SettingActionTile(
                      icon: iconContainer(
                        Symbols.format_paint,
                        isLight ? Color(0xfff8e287) : Color(0xff534600),
                        isLight ? Color(0xff534600) : Color(0xfff8e287),
                      ),
                      title: Text("appearance".tr()),
                      description: Text("appearance_sub".tr()),
                      onTap: () async {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const AppearanceScreen(),
                          ),
                        );
                      },
                    ),
                  ],
                ),
                SizedBox(height: 16),
                SettingSection(
                  styleTile: true,
                  tiles: [
                    SettingActionTile(
                      icon: iconContainer(
                        Symbols.schedule,
                        isLight ? Color(0xffcdeda3) : Color(0xff354e16),
                        isLight ? Color(0xff354e16) : Color(0xffcdeda3),
                      ),
                      title: Text("Clock"),
                      description: Text("Clock style, format, and more"),
                      onTap: () async {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const ClockSettingScreen(),
                          ),
                        );
                      },
                    ),

                    SettingActionTile(
                      icon: iconContainer(
                        Symbols.alarm,
                        isLight ? Color(0xffd6e3ff) : Color(0xff284777),
                        isLight ? Color(0xff284777) : Color(0xffd6e3ff),
                      ),
                      title: Text("Alarm"),
                      description: Text("Volume, background service"),
                      onTap: () async {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const AlarmSettingScreen(),
                          ),
                        );
                      },
                    ),

                    SettingActionTile(
                      icon: iconContainer(
                        Symbols.mobile_text_2,
                        isLight ? Color(0xffffdbd1) : Color(0xff723523),
                        isLight ? Color(0xff723523) : Color(0xffffdbd1),
                      ),
                      title: Text("Screen saver"),
                      description: Text("Brightness, style, theme"),
                      onTap: () async {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const ScreenSaverSettingScreen(),
                          ),
                        );
                      },
                    ),

                    SettingActionTile(
                      icon: iconContainer(
                        Symbols.nest_clock_farsight_analog,
                        isLight ? Color(0xff9df0f8) : Color(0xff004f54),
                        isLight ? Color(0xff004f54) : Color(0xff9df0f8),
                      ),
                      title: Text("Pomodoro"),
                      description: Text("Breaks, cycles, autostart"),
                      onTap: () async {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (_) => const PomoSettingScreen(),
                          ),
                        );
                      },
                    ),
                  ],
                ),

                SizedBox(height: MediaQuery.of(context).padding.bottom + 30),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

Widget iconContainer(IconData icon, Color color, Color onColor) {
  return Container(
    width: 40,
    height: 40,
    decoration: BoxDecoration(
      borderRadius: BorderRadius.circular(50),
      color: color,
    ),
    child: Icon(icon, fill: 1, weight: 500, color: onColor),
  );
}
