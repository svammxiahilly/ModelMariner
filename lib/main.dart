import 'package:flutter/material.dart';
import 'package:animations/animations.dart';
import 'package:hive_flutter/adapters.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'screens/home.dart';
import 'package:flutter/services.dart';
import 'helpers/preferences_helper.dart';
import 'controllers/theme_controller.dart';
import 'package:provider/provider.dart';
import 'services/alarm_service.dart';
import 'notifiers/settings_notifier.dart';
import 'package:flutter_foreground_task/flutter_foreground_task.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import 'package:flutter_displaymode/flutter_displaymode.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);

  await SharedPreferences.getInstance();

  FlutterForegroundTask.initCommunicationPort();

  await AlarmService.instance.init();
  await Hive.initFlutter();
  await Hive.openBox<String>('savedTimezones');

  await PreferencesHelper.init();

  final themeController = ThemeController();
  await themeController.initialize();
  await themeController.checkDynamicColorSupport();

  WakelockPlus.enable();

  await FlutterDisplayMode.setHighRefreshRate();

  // FlutterForegroundTask.init(
  //   androidNotificationOptions: AndroidNotificationOptions(
  //     channelId: 'timer_channel',
  //     channelName: 'Timers',
  //     channelDescription: 'Running timers in background',
  //     channelImportance: NotificationChannelImportance.LOW,
  //     priority: NotificationPriority.LOW,
  //   ),
  //   iosNotificationOptions: const IOSNotificationOptions(),
  //   foregroundTaskOptions: ForegroundTaskOptions(
  //     eventAction: ForegroundTaskEventAction.repeat(1000),
  //     autoRunOnBoot: false,
  //   ),
  // );

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => themeController),
        ChangeNotifierProvider(create: (_) => UnitSettingsNotifier()),
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    final themeController = Provider.of<ThemeController>(context);
    final colorThemeDark = ColorScheme.fromSeed(
      seedColor: themeController.seedColor,
      brightness: Brightness.dark,
    );

    final colorThemeLight = ColorScheme.fromSeed(
      seedColor: themeController.seedColor,
      brightness: Brightness.light,
    );

    return MaterialApp(
      title: 'ClockMaster',
      debugShowCheckedModeBanner: false,
      theme:
          ThemeData.from(
            colorScheme: isMonochrome(themeController.seedColor)
                ? ColorScheme.fromSeed(
                    seedColor: themeController.seedColor,
                    brightness: Brightness.light,
                    dynamicSchemeVariant: DynamicSchemeVariant.monochrome,
                  )
                : ColorScheme.fromSeed(
                    seedColor: themeController.seedColor,
                    brightness: Brightness.light,
                  ),
            useMaterial3: true,
          ).copyWith(
            textTheme: ThemeData.light().textTheme
                .apply(fontFamily: 'DefaultFont')
                .copyWith(
                  bodyLarge: TextStyle(
                    fontSize: 15.3,
                    color: colorThemeLight.onSurface,

                    fontFamily: 'DefaultFont',
                  ),
                  bodyMedium: TextStyle(
                    fontSize: 13.3,
                    color: colorThemeLight.onSurface,
                    fontFamily: 'DefaultFont',
                  ),
                ),

            highlightColor: Colors.transparent,

            pageTransitionsTheme: const PageTransitionsTheme(
              builders: {
                TargetPlatform.android: SharedAxisPageTransitionsBuilder(
                  transitionType: SharedAxisTransitionType.horizontal,
                ),
                TargetPlatform.iOS: SharedAxisPageTransitionsBuilder(
                  transitionType: SharedAxisTransitionType.horizontal,
                ),
              },
            ),
          ),

      darkTheme:
          ThemeData.from(
            colorScheme: isMonochrome(themeController.seedColor)
                ? ColorScheme.fromSeed(
                    seedColor: themeController.seedColor,
                    brightness: Brightness.dark,
                    dynamicSchemeVariant: DynamicSchemeVariant.monochrome,
                  )
                : ColorScheme.fromSeed(
                    seedColor: themeController.seedColor,
                    brightness: Brightness.dark,
                  ),
            useMaterial3: true,
          ).copyWith(
            textTheme: ThemeData.dark().textTheme
                .apply(fontFamily: 'DefaultFont')
                .copyWith(
                  bodyLarge: TextStyle(
                    fontSize: 15.3,
                    color: colorThemeDark.onSurface,

                    fontFamily: 'DefaultFont',
                  ),
                  bodyMedium: TextStyle(
                    fontSize: 13.3,
                    color: colorThemeDark.onSurface,
                    fontFamily: 'DefaultFont',
                  ),
                ),
            highlightColor: Colors.transparent,
            pageTransitionsTheme: const PageTransitionsTheme(
              builders: {
                TargetPlatform.android: SharedAxisPageTransitionsBuilder(
                  transitionType: SharedAxisTransitionType.horizontal,
                ),
                TargetPlatform.iOS: SharedAxisPageTransitionsBuilder(
                  transitionType: SharedAxisTransitionType.horizontal,
                ),
              },
            ),
          ),

      themeMode: themeController.themeMode,
      home: const HomePage(),
    );
  }
}

bool isMonochrome(Color c, {double tol = 1.0 / 255.0}) {
  final r = c.r, g = c.g, b = c.b;
  return (r - g).abs() <= tol && (g - b).abs() <= tol && (r - b).abs() <= tol;
}
