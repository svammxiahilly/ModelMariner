import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import '../helpers/icon_helper.dart';
import 'alarm_screen.dart';
import 'stopwatch_screen.dart';
import 'timer_screen.dart';
import 'world_clock_screen.dart';
import 'settings.dart';
import 'timezone_search_page.dart';
import 'package:hive/hive.dart';
import 'package:animations/animations.dart';
import 'alarm_edit_screen.dart';
import '../models/alarm_data_model.dart';
import '../services/alarm_service.dart';
import '../notifiers/settings_notifier.dart';
import 'package:provider/provider.dart';
import '../services/notificationservice_native.dart';
import 'package:settings_tiles/settings_tiles.dart';
import 'package:permission_handler/permission_handler.dart';
import '../screens/screen_saver.dart';
import '../helpers/preferences_helper.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import 'package:device_info_plus/device_info_plus.dart';
import 'about_screen.dart';

class BatteryOptimizationHelper {
  static Future<bool> isIgnoringBatteryOptimizations() async {
    final status = await Permission.ignoreBatteryOptimizations.status;
    return status.isGranted;
  }

  static Future<bool> requestIgnoreBatteryOptimizations() async {
    final status = await Permission.ignoreBatteryOptimizations.request();
    return status.isGranted;
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> with WidgetsBindingObserver {
  int _selectedIndex = 0;
  String _selectedIndexLabel = "Alarm";
  bool _showFab = true;
  bool loaded = false;

  bool get is24HourFormat =>
      context.watch<UnitSettingsNotifier>().timeFormat == "24 hr";

  final GlobalKey<AlarmScreenState> _alarmScreenKey =
      GlobalKey<AlarmScreenState>();

  final GlobalKey<TimerScreenState> _timersScreenKey =
      GlobalKey<TimerScreenState>();

  late final List<Widget> _pages;

  @override
  void initState() {
    super.initState();
    _pages = <Widget>[
      AlarmScreen(key: _alarmScreenKey),
      WorldClockScreen(),
      StopWatchScreen(),
      TimerScreen(key: _timersScreenKey),
    ];
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      final isIgnoring =
          await BatteryOptimizationHelper.isIgnoringBatteryOptimizations();
      final bool grantedNoti = await NotificationService.checkPermission();

      if (!isIgnoring || !grantedNoti) {
        _showRequiredActionSheet(context);
      }

      if (PreferencesHelper.getBool('PreventScreenSleep') == true) {
        WakelockPlus.toggle(enable: true);
      }
    });
    _startAlwaysOnService();
  }

  final List<String> _pagesLabel = [
    "Alarm",
    "World clock",
    "StopWatch",
    "Timer",
  ];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
      _selectedIndexLabel = _pagesLabel[index];
      if (index == 2) {
        _showFab = false;
      } else {
        _showFab = true;
      }
    });
  }

  static const MethodChannel _channelBrightness = MethodChannel(
    'com.pranshulgg.alarm/alarm',
  );

  Future<void> resetBrightness() async {
    try {
      await _channelBrightness.invokeMethod('resetBrightness');
    } on PlatformException catch (e) {
      print("Failed to reset brightness: ${e.message}");
    }
  }

  Future<bool> _isAndroid9OrAbove() async {
    final androidInfo = await DeviceInfoPlugin().androidInfo;
    final isAndroid9OrAbove = androidInfo.version.sdkInt >= 28;
    return isAndroid9OrAbove;
  }

  Future<void> _startAlwaysOnService() async {
    final count = await AlarmService.instance.activeAlarmCount();
    final value = PreferencesHelper.getBool("alwaysRunService") ?? false;

    if (value == true && count > 0) {
      _channelBrightness.invokeMethod('refreshAlwaysRunning');
    } else if (value == false) {
      _channelBrightness.invokeMethod('StopAlwaysRunning');
    }
  }

  Future<void> _setupSystemUI() async {
    if (!loaded) {
      final isAndroid9OrAbove = await _isAndroid9OrAbove();
      SystemChrome.setSystemUIOverlayStyle(
        SystemUiOverlayStyle(
          statusBarColor: const Color(0x01000000),
          statusBarIconBrightness:
              Theme.of(context).brightness == Brightness.light
              ? Brightness.dark
              : Brightness.light,
          systemNavigationBarIconBrightness:
              Theme.of(context).brightness == Brightness.light
              ? Brightness.dark
              : Brightness.light,
          systemNavigationBarColor:
              MediaQuery.of(context).systemGestureInsets.left > 0
              ? const Color(0x01000000)
              : isAndroid9OrAbove
              ? const Color(0x01000000)
              : Theme.of(context).scaffoldBackgroundColor,
        ),
      );
      SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
      loaded = true;
    }
  }

  void revertSettings() async {
    final isAndroid9OrAbove = await _isAndroid9OrAbove();

    SystemChrome.setEnabledSystemUIMode(
      SystemUiMode.manual,
      overlays: SystemUiOverlay.values,
    );

    resetBrightness();
    if (PreferencesHelper.getBool('PreventScreenSleep') == false) {
      WakelockPlus.toggle(enable: false);
    }
    SystemChrome.setSystemUIOverlayStyle(
      SystemUiOverlayStyle(
        statusBarColor: Color(0x01000000),
        statusBarIconBrightness:
            Theme.of(context).brightness == Brightness.light
            ? Brightness.dark
            : Brightness.light,
        systemNavigationBarIconBrightness:
            Theme.of(context).brightness == Brightness.light
            ? Brightness.dark
            : Brightness.light,
        systemNavigationBarColor:
            MediaQuery.of(context).systemGestureInsets.left > 0
            ? Color(0x01000000)
            : isAndroid9OrAbove
            ? Color(0x01000000)
            : Theme.of(context).scaffoldBackgroundColor,
      ),
    );
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
  }

  @override
  Widget build(BuildContext context) {
    final colorTheme = Theme.of(context).colorScheme;
    print(ThemeData.light().textTheme.bodyLarge!.color);

    _setupSystemUI();

    return Scaffold(
      appBar: AppBar(
        title: Text(
          _selectedIndexLabel,
          style: TextStyle(
            fontWeight: FontWeight.w500,
            color: colorTheme.onSurface,
          ),
        ),
        toolbarHeight: 65,
        backgroundColor: colorTheme.surface,
        scrolledUnderElevation: 0,
        actions: [
          PopupMenuButton<String>(
            icon: const IconWithWeight(Symbols.more_vert, weight: 900),
            elevation: 0,
            color: colorTheme.surfaceContainerHigh,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(13),
            ),
            clipBehavior: Clip.hardEdge,
            onSelected: (value) async {
              if (value == "openSettings") {
                final result = await Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const SettingsScreen()),
                );
                if (result == true) {
                  WidgetsBinding.instance.addPostFrameCallback((_) {
                    Future.delayed(Duration(milliseconds: 500), () {
                      revertSettings();
                    });
                  });
                }
              } else if (value == "openScreenSaver") {
                final result = await Navigator.of(
                  context,
                ).push(MaterialPageRoute(builder: (_) => const ScreenSaver()));

                if (result == true) {
                  WidgetsBinding.instance.addPostFrameCallback((_) {
                    Future.delayed(Duration(milliseconds: 500), () {
                      revertSettings();
                    });
                  });
                }
              } else if (value == "openAboutScreen") {
                await Navigator.of(
                  context,
                ).push(MaterialPageRoute(builder: (_) => const AboutScreen()));
              }
            },
            itemBuilder: (BuildContext context) => <PopupMenuEntry<String>>[
              PopupMenuItem<String>(
                value: 'openSettings',

                padding: EdgeInsets.only(left: 14),
                // child: Text('Settings', style: TextStyle(fontSize: 16)),
                child: menuItemRow(Symbols.settings, "Settings"),
              ),
              PopupMenuItem<String>(
                value: 'openScreenSaver',
                padding: EdgeInsets.only(left: 14, right: 6),
                // child: Text('Screen saver', style: TextStyle(fontSize: 16)),
                child: menuItemRow(Symbols.mobile_text_2, "Screen saver"),
              ),
              PopupMenuItem<String>(
                value: 'openAboutScreen',
                padding: EdgeInsets.only(left: 14, right: 6),
                // child: Text('About', style: TextStyle(fontSize: 16)),
                child: menuItemRow(Symbols.info, "About"),
              ),
            ],
          ),

          SizedBox(width: 8),
        ],
      ),
      body: IndexedStack(index: _selectedIndex, children: _pages),
      floatingActionButton: AnimatedScale(
        scale: _showFab ? 1.0 : 0.0,
        duration: Duration(milliseconds: 200),
        curve: _showFab ? Curves.easeOutBack : Curves.easeInBack,

        child: _selectedIndex == 1
            ? OpenContainer<List<String>?>(
                transitionType: ContainerTransitionType.fadeThrough,
                openBuilder: (context, _) => TimezoneSearchPage(),
                closedElevation: 0,
                closedShape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(20),
                ),
                openShape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(50),
                ),
                openElevation: 0,
                transitionDuration: Duration(milliseconds: 500),
                closedColor: colorTheme.tertiaryContainer,
                openColor: colorTheme.surfaceContainerHigh,

                closedBuilder: (context, openContainer) {
                  return SizedBox(
                    height: 80,
                    width: 80,
                    child: FloatingActionButton.large(
                      onPressed: openContainer,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(20),
                      ),
                      heroTag: "idk_random_tag",
                      elevation: 0,
                      highlightElevation: 0,

                      backgroundColor: colorTheme.tertiaryContainer,
                      foregroundColor: colorTheme.onTertiaryContainer,
                      child: IconWithWeight(Symbols.add),
                    ),
                  );
                },
                onClosed: (result) async {
                  if (result != null) {
                    await Hive.openBox<String>('savedTimezones');
                    final box = Hive.box<String>('savedTimezones');
                    for (var tzName in result) {
                      if (!box.values.contains(tzName)) {
                        box.add(tzName);
                      }
                    }
                  }
                },
              )
            : _selectedIndex == 0
            ? SizedBox(
                height: 80,
                width: 80,
                child: FloatingActionButton.large(
                  onPressed: () {
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
                          bottom: MediaQuery.of(context).viewInsets.bottom,
                        ),
                        child: AlarmEditContent(is24HourFormat: is24HourFormat),
                      ),
                    ).then((result) async {
                      if (result is Alarm) {
                        await AlarmService.instance.saveAndSchedule(result);
                        _alarmScreenKey.currentState?.load();
                      }
                    });
                  },
                  heroTag: "idk_random_tag_2",
                  elevation: 0,
                  highlightElevation: 0,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(20),
                  ),
                  backgroundColor: colorTheme.tertiaryContainer,
                  foregroundColor: colorTheme.onTertiaryContainer,
                  child: IconWithWeight(Symbols.add),
                ),
              )
            : SizedBox(
                height: 80,
                width: 80,
                child: FloatingActionButton.large(
                  onPressed: () {
                    _selectedIndex == 3
                        ? _timersScreenKey.currentState?.showAddBottomSheet()
                        : null;
                  },
                  heroTag: "idk_random_tag_3",
                  elevation: 0,
                  highlightElevation: 0,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(20),
                  ),
                  backgroundColor: colorTheme.tertiaryContainer,
                  foregroundColor: colorTheme.onTertiaryContainer,
                  child: IconWithWeight(Symbols.add),
                ),
              ),
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selectedIndex,

        onDestinationSelected: _onItemTapped,
        labelTextStyle: WidgetStateProperty.all(
          TextStyle(fontWeight: FontWeight.w500, color: colorTheme.onSurface),
        ),

        destinations: const [
          NavigationDestination(
            icon: IconWithWeight(Symbols.alarm),
            selectedIcon: IconWithWeight(Symbols.alarm, fill: 1),
            label: 'Alarm',
          ),
          NavigationDestination(
            icon: IconWithWeight(Symbols.schedule),
            selectedIcon: IconWithWeight(Symbols.schedule, fill: 1),
            label: 'Clock',
          ),
          NavigationDestination(
            icon: IconWithWeight(Symbols.hourglass_empty),
            selectedIcon: IconWithWeight(Symbols.hourglass_top, fill: 1),
            label: 'Stopwatch',
          ),
          NavigationDestination(
            icon: IconWithWeight(Symbols.timer),
            selectedIcon: IconWithWeight(Symbols.timer, fill: 1),
            label: 'Timer',
          ),
        ],
      ),
    );
  }
}

void _showRequiredActionSheet(BuildContext context) async {
  final colorTheme = Theme.of(context).colorScheme;

  Future<void> _requestloadPermission() async {
    final bool granted = await NotificationService.requestPermission();
  }

  final grantedNotification = await NotificationService.checkPermission();

  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    isDismissible: false,
    enableDrag: false,
    backgroundColor: colorTheme.surface,
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
    ),
    builder: (context) {
      return PopScope(
        canPop: false,
        child: Padding(
          padding: EdgeInsets.fromLTRB(
            0,
            10,
            0,
            MediaQuery.of(context).padding.bottom + 20,
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Center(
                child: Text(
                  "Required permissions",
                  style: TextStyle(
                    color: colorTheme.primary,
                    fontWeight: FontWeight.bold,
                    fontSize: 18,
                  ),
                ),
              ),
              SizedBox(height: 16 - 10),
              SettingSection(
                styleTile: true,

                tiles: [
                  SettingActionTile(
                    title: Text("Allow notification permission"),
                    description: Text(
                      "Notification permission is required to show notfications like alarms and stopwatch and also to be able to run in the background",
                    ),
                    visible: !grantedNotification,
                    onTap: () async {
                      await _requestloadPermission();
                    },
                  ),
                ],
              ),
              SizedBox(height: 10),
              SettingSection(
                styleTile: true,

                tiles: [
                  SettingActionTile(
                    title: Text("Disable battery optimizations"),
                    description: Text(
                      "Disabling battery optimization is required for Alarms to function properly",
                    ),
                    onTap: () {
                      BatteryOptimizationHelper.requestIgnoreBatteryOptimizations();
                    },
                  ),
                ],
              ),
              SizedBox(height: 10),
              Align(
                alignment: Alignment.center,
                child: FilledButton(
                  onPressed: () {
                    checkAllPermission(context);
                  },
                  child: Text(
                    "Done",
                    style: TextStyle(fontWeight: FontWeight.w600),
                  ),
                ),
              ),
            ],
          ),
        ),
      );
    },
  );
}

void checkAllPermission(BuildContext context) async {
  final isIgnoringBatteryOptimizations =
      await BatteryOptimizationHelper.isIgnoringBatteryOptimizations();
  final grantedNotification = await NotificationService.checkPermission();

  if (isIgnoringBatteryOptimizations && grantedNotification) {
    Navigator.pop(context);
  }
}

Widget menuItemRow(IconData leadingIcon, String label) {
  return Row(
    spacing: 6,
    children: [
      IconWithWeight(leadingIcon, fill: 1, size: 20),
      Text(label, style: TextStyle(fontSize: 16)),
    ],
  );
}
