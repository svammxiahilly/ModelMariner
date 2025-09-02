import 'package:easy_localization/easy_localization.dart';
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
import 'package:flex_color_picker/flex_color_picker.dart';

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
    final colortheme = Theme.of(context).colorScheme;
    final currentTimeFormat =
        PreferencesHelper.getString("timeFormat") ?? "12 hr";
    final currentShowSeconds =
        PreferencesHelper.getBool("showSeconds") ?? false;
    final currentScreenSaverClockStyle =
        PreferencesHelper.getString("ScreenSaverClockStyle") ?? "Analog";
    final currentClockStyle =
        PreferencesHelper.getString("ClockStyle") ?? "Digital";

    final optionsTheme = {
      "Auto": "sys_theme".tr(),
      "Dark": "dark_theme".tr(),
      "Light": "light_theme".tr(),
    };
    final optionsTimeFormat = {
      "12 hr": "12 ${'hr'.tr()}",
      "24 hr": "24 ${'hr'.tr()}",
    };
    final optionsScreenSaverClockStyle = {
      "Analog": "clock_style_analog".tr(),
      "Digital": "clock_style_digital".tr(),
    };

    final optionsClockStyle = {
      "Analog": "clock_style_analog".tr(),
      "Digital": "clock_style_digital".tr(),
    };

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
                  title: SettingSectionTitle("app_looks".tr(), noPadding: true),
                  tiles: [
                    SettingSingleOptionTile(
                      icon: IconWithWeight(Symbols.routine, fill: 1),
                      title: Text('app_theme'.tr()),
                      dialogTitle: 'app_theme'.tr(),
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
                      icon: _showTile
                          ? GestureDetector(
                              onTap: () {
                                Color selectedColor =
                                    PreferencesHelper.getColor(
                                      "CustomMaterialColor",
                                    ) ??
                                    Colors.blue;

                                showModalBottomSheet(
                                  context: context,
                                  isScrollControlled: true,
                                  showDragHandle: true,
                                  shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.vertical(
                                      top: Radius.circular(28),
                                    ),
                                  ),
                                  builder: (context) {
                                    return Container(
                                      width: MediaQuery.of(context).size.width,
                                      padding: EdgeInsets.only(
                                        top: 0,
                                        bottom:
                                            MediaQuery.of(
                                              context,
                                            ).padding.bottom +
                                            10,
                                      ),
                                      child: StatefulBuilder(
                                        builder: (context, setModalState) {
                                          return Column(
                                            mainAxisSize: MainAxisSize.min,
                                            children: [
                                              ColorPicker(
                                                color: selectedColor,
                                                onColorChanged: (Color color) {
                                                  setModalState(() {
                                                    selectedColor = color;
                                                  });
                                                },
                                                pickersEnabled:
                                                    const <
                                                      ColorPickerType,
                                                      bool
                                                    >{
                                                      ColorPickerType.primary:
                                                          false,
                                                      ColorPickerType.accent:
                                                          false,
                                                      ColorPickerType.both:
                                                          true,
                                                      ColorPickerType.custom:
                                                          false,
                                                      ColorPickerType.wheel:
                                                          false,
                                                    },
                                                spacing: 6,
                                                runSpacing: 6,
                                                subheading: Divider(),
                                                borderRadius: 50,
                                              ),
                                              SizedBox(height: 12),
                                              Padding(
                                                padding:
                                                    const EdgeInsets.symmetric(
                                                      horizontal: 20,
                                                    ),
                                                child: Row(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment
                                                          .spaceBetween,
                                                  children: [
                                                    OutlinedButton(
                                                      onPressed: () {
                                                        Navigator.of(
                                                          context,
                                                        ).pop();
                                                      },
                                                      child: Text(
                                                        'cancel'.tr(),
                                                        style: TextStyle(
                                                          fontSize: 16,
                                                          fontWeight:
                                                              FontWeight.w600,
                                                        ),
                                                      ),
                                                    ),
                                                    FilledButton(
                                                      onPressed: () {
                                                        Navigator.of(
                                                          context,
                                                        ).pop();
                                                        setState(() {
                                                          PreferencesHelper.setColor(
                                                            "CustomMaterialColor",
                                                            selectedColor,
                                                          );
                                                          Provider.of<
                                                                ThemeController
                                                              >(
                                                                context,
                                                                listen: false,
                                                              )
                                                              .setSeedColor(
                                                                selectedColor,
                                                              );
                                                        });
                                                      },
                                                      child: Text(
                                                        'save'.tr(),
                                                        style: TextStyle(
                                                          fontSize: 16,
                                                          fontWeight:
                                                              FontWeight.w600,
                                                        ),
                                                      ),
                                                    ),
                                                  ],
                                                ),
                                              ),
                                            ],
                                          );
                                        },
                                      ),
                                    );
                                  },
                                );
                              },
                              child: Container(
                                width: 24,
                                height: 36,
                                decoration: BoxDecoration(
                                  color: PreferencesHelper.getColor(
                                    "CustomMaterialColor",
                                  ),
                                  borderRadius: BorderRadius.circular(50),
                                  border: Border.all(
                                    width: 1,
                                    color: colortheme.outlineVariant,
                                  ),
                                ),
                              ),
                            )
                          : Icon(Symbols.colorize, fill: 1, weight: 500),
                      title: Text("use_custom_color".tr()),
                      description: Text("use_custom_color_sub".tr()),
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
                            ).setSeedColor(
                              PreferencesHelper.getColor("weatherThemeColor") ??
                                  Colors.blue,
                            );
                          }
                        });
                        _showTile = value;
                      },
                    ),
                    SettingSwitchTile(
                      icon: Icon(null, fill: 1, weight: 500),
                      title: Text('use_expressive_color'.tr()),
                      toggled:
                          PreferencesHelper.getBool("useExpressiveVariant") ??
                          false,
                      onChanged: (value) {
                        context.read<UnitSettingsNotifier>().updateColorVariant(
                          value,
                        );
                        setState(() {});
                      },
                    ),

                    SettingSwitchTile(
                      enabled: isSupported
                          ? _showTile
                                ? false
                                : true
                          : false,
                      icon: IconWithWeight(Symbols.wallpaper, fill: 1),
                      title: Text("dynamic_colors".tr()),

                      description: Text(
                        "${"dynamic_colors_sub".tr()} ${isSupported ? "" : "(Android 12+)"}",
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
                      title: Text("prevent_screen_sleep".tr()),
                      description: Text("prevent_screen_sleep_sub".tr()),
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
                  title: SettingSectionTitle("clock".tr(), noPadding: true),
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

                SizedBox(height: 10),
                SettingSection(
                  styleTile: true,
                  title: SettingSectionTitle("alarm".tr(), noPadding: true),
                  tiles: [
                    //                     Slider(
                    //   value: _volume,
                    //   min: 0.0,
                    //   max: 1.0,
                    //   onChanged: _setVolume,
                    // ),
                    SettingSliderTile(
                      icon: IconWithWeight(Symbols.volume_up, fill: 1),
                      title: Text("alarm_volume".tr()),
                      dialogTitle: "alarm_volume".tr(),
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
                      title: Text("always_run_service".tr()),
                      description: Text("always_run_service_sub".tr()),
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
                  title: SettingSectionTitle(
                    "screen_saver".tr(),
                    noPadding: true,
                  ),
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

                SizedBox(height: MediaQuery.of(context).padding.bottom + 30),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
