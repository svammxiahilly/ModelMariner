import 'package:flutter/material.dart';
import 'package:settings_tiles/settings_tiles.dart';
import '../controllers/theme_controller.dart';
import 'package:provider/provider.dart';
import '../helpers/preferences_helper.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import '../helpers/icon_helper.dart';
import '../notifiers/settings_notifier.dart';
import 'package:flutter/services.dart';
import '../services/alarm_service.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import 'package:flutter_volume_controller/flutter_volume_controller.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool _showTile = PreferencesHelper.getBool("usingCustomSeed") ?? false;
  bool _useCustomTile = PreferencesHelper.getBool("DynamicColors") == true
      ? false
      : true;

  double _volume = 0.5;
  AudioStream _audioStream = AudioStream.alarm;
  @override
  void initState() {
    super.initState();
    FlutterVolumeController.setAndroidAudioStream(stream: _audioStream);
    FlutterVolumeController.updateShowSystemUI(false);
    _initVolume();
  }

  Future<void> _initVolume() async {
    double volume =
        await FlutterVolumeController.getVolume(stream: _audioStream) ?? 0.5;
    setState(() => _volume = volume);
  }

  void _setVolume(double value) {
    setState(() => _volume = value);
    FlutterVolumeController.setVolume(value, stream: _audioStream);
  }

  double _sliderValueFraction =
      PreferencesHelper.getDouble("screensaverBrightness") ?? 0.3;

  @override
  Widget build(BuildContext context) {
    final themeController = Provider.of<ThemeController>(context);
    final currentMode = themeController.themeMode;
    final isSupported = themeController.isDynamicColorSupported;
    final currentTimeFormat =
        PreferencesHelper.getString("timeFormat") ?? "12 hr";
    final currentShowSeconds =
        PreferencesHelper.getBool("showSeconds") ?? false;
    final currentScreenSaverClockStyle =
        PreferencesHelper.getString("ScreenSaverClockStyle") ?? "Analog";
    final currentClockStyle =
        PreferencesHelper.getString("ClockStyle") ?? "Digital";
    final optionsTheme = {"Auto": "Auto", "Dark": "Dark", "Light": "Light"};
    final optionsTimeFormat = {"12 hr": "12 hr", "24 hr": "24 hr"};
    final optionsScreenSaverClockStyle = {
      "Analog": "Analog",
      "Digital": "Digital",
    };

    final optionsClockStyle = {"Analog": "Analog", "Digital": "Digital"};

    final alwaysRunService =
        PreferencesHelper.getBool('alwaysRunService') ?? false;
    final preventScreenSleep =
        PreferencesHelper.getBool('PreventScreenSleep') ?? false;

    final useFullBlackForScreenSaver =
        PreferencesHelper.getBool('FullBlackScreenSaver') ?? false;

    const MethodChannel _channel = MethodChannel('com.pranshulgg.alarm/alarm');
    final isLight = Theme.of(context).brightness == Brightness.light;

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surface,
      body: CustomScrollView(
        slivers: [
          SliverAppBar.large(
            title: Text('Settings'),
            titleSpacing: 0,
            backgroundColor: Theme.of(context).colorScheme.surface,
            scrolledUnderElevation: 1,
          ),

          SliverToBoxAdapter(
            child: Column(
              children: [
                SettingSection(
                  styleTile: true,
                  title: SettingSectionTitle("App looks", noPadding: true),
                  tiles: [
                    SettingSingleOptionTile(
                      icon: IconWithWeight(Symbols.routine, fill: 1),
                      title: Text('App theme'),
                      dialogTitle: 'App theme',
                      value: SettingTileValue(
                        optionsTheme[currentMode == ThemeMode.light
                            ? "Light"
                            : currentMode == ThemeMode.system
                            ? "Auto"
                            : "Dark"]!,
                      ),
                      options: optionsTheme.values.toList(),
                      initialOption:
                          optionsTheme[currentMode == ThemeMode.light
                              ? "Light"
                              : currentMode == ThemeMode.system
                              ? "Auto"
                              : "Dark"]!,
                      onSubmitted: (value) {
                        setState(() {
                          final selectedKey = optionsTheme.entries
                              .firstWhere((e) => e.value == value)
                              .key;
                          PreferencesHelper.setString("AppTheme", selectedKey);
                          themeController.setThemeMode(
                            selectedKey == "Dark"
                                ? ThemeMode.dark
                                : selectedKey == "Auto"
                                ? ThemeMode.system
                                : ThemeMode.light,
                          );
                          WidgetsBinding.instance.addPostFrameCallback((_) {
                            Navigator.of(context).pop(true);
                          });
                        });
                      },
                    ),

                    SettingSwitchTile(
                      enabled: _useCustomTile,
                      icon: IconWithWeight(Symbols.colorize, fill: 1),
                      title: Text("Use custom color"),
                      toggled:
                          PreferencesHelper.getBool("usingCustomSeed") ?? false,
                      onChanged: (value) {
                        setState(() {
                          PreferencesHelper.setBool("usingCustomSeed", value);
                          if (value == true) {
                            Provider.of<ThemeController>(
                              context,
                              listen: false,
                            ).setSeedColor(
                              PreferencesHelper.getColor(
                                    "CustomMaterialColor",
                                  ) ??
                                  Colors.blue,
                            );
                          } else {
                            Provider.of<ThemeController>(
                              context,
                              listen: false,
                            ).setSeedColor(Colors.blue);
                          }
                        });
                        _showTile = value;
                      },
                    ),

                    SettingColorTile(
                      enabled: _showTile,
                      icon: IconWithWeight(Symbols.colors, fill: 1),
                      title: Text('Primary color'),
                      description: Text(
                        'Select a seed color to generate the theme',
                      ),
                      dialogTitle: 'Color',
                      initialColor:
                          PreferencesHelper.getColor("CustomMaterialColor") ??
                          Colors.blue,
                      colorPickers: [ColorPickerType.primary],
                      onSubmitted: (value) {
                        setState(() {
                          PreferencesHelper.setColor(
                            "CustomMaterialColor",
                            value,
                          );
                          Provider.of<ThemeController>(
                            context,
                            listen: false,
                          ).setSeedColor(value);
                        });
                      },
                    ),

                    SettingSwitchTile(
                      enabled: isSupported
                          ? _showTile
                                ? false
                                : true
                          : false,
                      icon: IconWithWeight(Symbols.wallpaper, fill: 1),
                      title: Text("Dynamic colors"),

                      description: Text(
                        "${"Use wallpaper colors"} ${isSupported ? "" : "(Android 12+)"}",
                      ),
                      toggled:
                          PreferencesHelper.getBool("DynamicColors") ?? false,
                      onChanged: (value) async {
                        final themeController = context.read<ThemeController>();

                        PreferencesHelper.setBool("DynamicColors", value);

                        if (value) {
                          await themeController.loadDynamicColors();
                        } else {
                          Provider.of<ThemeController>(
                            context,
                            listen: false,
                          ).setSeedColor(Colors.blue);
                        }
                        setState(() {
                          if (value) {
                            _useCustomTile = false;
                          } else {
                            _useCustomTile = true;
                          }
                        });
                      },
                    ),

                    SettingSwitchTile(
                      icon: IconWithWeight(Symbols.lock_clock, fill: 1),
                      title: Text("Prevent Screen Sleep"),
                      description: Text(
                        "Keeps the device awake while the app is running",
                      ),
                      toggled: preventScreenSleep,
                      onChanged: (value) async {
                        PreferencesHelper.setBool('PreventScreenSleep', value);

                        WakelockPlus.toggle(enable: value);

                        setState(() {});
                      },
                    ),
                  ],
                ),
                SizedBox(height: 10),
                SettingSection(
                  styleTile: true,
                  title: SettingSectionTitle("Clock", noPadding: true),
                  tiles: [
                    SettingSingleOptionTile(
                      icon: IconWithWeight(Symbols.farsight_digital, fill: 1),
                      title: Text('Clock style'),
                      value: SettingTileValue(
                        optionsClockStyle[currentClockStyle]!,
                      ),
                      dialogTitle: 'Clock style',
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
                      title: Text('Time format'),
                      value: SettingTileValue(
                        optionsTimeFormat[currentTimeFormat]!,
                      ),
                      dialogTitle: 'Time format',
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
                      title: Text("Display time with seconds"),
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

                SizedBox(height: 10),
                SettingSection(
                  styleTile: true,
                  title: SettingSectionTitle("Alarm", noPadding: true),
                  tiles: [
                    //                     Slider(
                    //   value: _volume,
                    //   min: 0.0,
                    //   max: 1.0,
                    //   onChanged: _setVolume,
                    // ),
                    SettingSliderTile(
                      icon: IconWithWeight(Symbols.volume_up, fill: 1),
                      title: Text("Alarm volume"),
                      dialogTitle: "Alarm volume",
                      initialValue: _volume,
                      min: 0.0,
                      max: 1.0,
                      divisions: 10,
                      value: SettingTileValue("${(_volume * 100).round()}%"),
                      label: (value) => "${(value * 100).round()}%",
                      onSubmitted: (v) {
                        _setVolume(v);
                      },
                    ),
                    SettingSwitchTile(
                      icon: IconWithWeight(Symbols.construction, fill: 1),
                      title: Text("Always run background service"),
                      description: Text(
                        "Use this if alarms aren’t working; it may prevent the app from being killed but uses more battery",
                      ),
                      toggled: alwaysRunService,
                      onChanged: (value) async {
                        PreferencesHelper.setBool('alwaysRunService', value);

                        final count = await AlarmService.instance
                            .activeAlarmCount();

                        if (value == true && count > 0) {
                          _channel.invokeMethod('refreshAlwaysRunning');
                        } else if (value == false) {
                          _channel.invokeMethod('StopAlwaysRunning');
                        }

                        setState(() {});
                      },
                    ),
                  ],
                ),

                SizedBox(height: 10),
                SettingSection(
                  styleTile: true,
                  title: SettingSectionTitle("Screen saver", noPadding: true),
                  tiles: [
                    SettingSliderTile(
                      icon: IconWithWeight(Symbols.brightness_7, fill: 1),
                      title: Text("Screen saver brightness"),
                      dialogTitle: "Brightness",
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
                      title: Text("Night mode"),
                      description: Text(
                        "Uses a full black scheme for dark rooms",
                      ),
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

                SizedBox(height: MediaQuery.of(context).padding.bottom + 30),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
