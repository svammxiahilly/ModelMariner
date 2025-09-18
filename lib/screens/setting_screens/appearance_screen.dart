import 'package:flutter/material.dart';
import 'package:settings_tiles/settings_tiles.dart';
import '../../controllers/theme_controller.dart';
import 'package:provider/provider.dart';
import '../../helpers/preferences_helper.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import '../../helpers/icon_helper.dart';
import '../../notifiers/settings_notifier.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import 'package:flex_color_picker/flex_color_picker.dart';
import 'package:easy_localization/easy_localization.dart';
import '../../utils/snack_util.dart';
import 'package:restart_app/restart_app.dart';

class AppearanceScreen extends StatefulWidget {
  const AppearanceScreen({super.key});

  @override
  State<AppearanceScreen> createState() => _AppearanceScreenState();
}

class _AppearanceScreenState extends State<AppearanceScreen> {
  bool _showTile = PreferencesHelper.getBool("usingCustomSeed") ?? false;
  bool _useCustomTile = PreferencesHelper.getBool("DynamicColors") == true
      ? false
      : true;
  @override
  Widget build(BuildContext context) {
    final themeController = Provider.of<ThemeController>(context);
    final currentMode = themeController.themeMode;

    final isSupported = themeController.isDynamicColorSupported;

    final colorTheme = Theme.of(context).colorScheme;

    final optionsTheme = {
      "Auto": "sys_theme".tr(),
      "Dark": "dark_theme".tr(),
      "Light": "light_theme".tr(),
    };
    final openContainerAnimation =
        PreferencesHelper.getBool("UseopenContainerAnimation") ?? true;

    final isLight = Theme.of(context).brightness == Brightness.light;

    final preventScreenSleep =
        PreferencesHelper.getBool('PreventScreenSleep') ?? false;

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surface,
      body: CustomScrollView(
        slivers: [
          SliverAppBar.large(
            title: Text('appearance'.tr()),
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
                                    color: colorTheme.outlineVariant,
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
                  ],
                ),

                SizedBox(height: 10),
                SettingSection(
                  styleTile: true,
                  title: SettingSectionTitle("Display", noPadding: true),
                  tiles: [
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
                  title: SettingSectionTitle("Animations", noPadding: true),
                  tiles: [
                    SettingSwitchTile(
                      icon: IconWithWeight(Symbols.animation, fill: 1),
                      title: Text("Container transform animation"),
                      description: Text(
                        "Smoothly transition a container into a full screen page. Turn this off if you notice lag or freezing",
                      ),
                      toggled: openContainerAnimation,
                      onChanged: (value) {
                        PreferencesHelper.setBool(
                          "UseopenContainerAnimation",
                          value,
                        );
                        setState(() {
                          SnackUtil.showSnackBar(
                            context: context,
                            message: "restart_for_changes".tr(),
                            actionLabel: "Restart",
                            duration: Duration(seconds: 30),
                            onActionPressed: () {
                              Restart.restartApp();
                            },
                          );
                        });
                      },
                    ),
                  ],
                ),

                SizedBox(height: 200),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
