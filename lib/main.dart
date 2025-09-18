import 'package:flutter/material.dart';
import 'package:animations/animations.dart';
import 'package:hive_flutter/adapters.dart';
import 'package:material_color_utilities/contrast/contrast.dart';
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
import 'package:easy_localization/easy_localization.dart';
import 'dart:ui' as ui;

const easySupportedLocales = [Locale('en')];

final flutterSupportedLocales = easySupportedLocales
    .map((l) => Locale(l.languageCode))
    .toSet()
    .toList();

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await EasyLocalization.ensureInitialized();

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

  runApp(
    EasyLocalization(
      supportedLocales: easySupportedLocales,
      path: 'assets/translations',
      fallbackLocale: Locale('en'),
      saveLocale: true,
      child: MultiProvider(
        providers: [
          ChangeNotifierProvider(create: (_) => themeController),
          ChangeNotifierProvider(create: (_) => UnitSettingsNotifier()),
        ],
        child: const MyApp(),
      ),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    final themeController = Provider.of<ThemeController>(context);

    final useExpressiveVariant = context
        .watch<UnitSettingsNotifier>()
        .useExpressiveVariant;
    final colorThemeDark = ColorScheme.fromSeed(
      seedColor: themeController.seedColor ?? Colors.blue,
      brightness: Brightness.dark,
    );

    final colorThemeLight = ColorScheme.fromSeed(
      seedColor: themeController.seedColor ?? Colors.blue,
      brightness: Brightness.light,
    );
    return MaterialApp(
      title: 'ClockMaster',
      debugShowCheckedModeBanner: false,
      locale: context.locale,
      supportedLocales: flutterSupportedLocales,
      localizationsDelegates: context.localizationDelegates,
      localeResolutionCallback: (locale, supportedLocales) {
        return context.locale;
      },
      builder: (context, child) {
        return Directionality(
          textDirection: ui.TextDirection.ltr,
          child: child!,
        );
      },
      theme:
          ThemeData.from(
            colorScheme: useExpressiveVariant
                ? ColorScheme.fromSeed(
                    seedColor: themeController.seedColor ?? Colors.blue,
                    brightness: Brightness.light,
                    dynamicSchemeVariant: DynamicSchemeVariant.expressive,
                  )
                : isMonochrome(themeController.seedColor)
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
                .apply(fontFamily: 'FlexFontEn')
                .copyWith(
                  bodyLarge: TextStyle(
                    color: colorThemeLight.onSurface,

                    fontFamily: 'FlexFontEn',
                  ),
                  bodyMedium: TextStyle(
                    color: colorThemeLight.onSurface,
                    fontFamily: 'FlexFontEn',
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
            colorScheme: useExpressiveVariant
                ? ColorScheme.fromSeed(
                    seedColor: themeController.seedColor ?? Colors.blue,
                    brightness: Brightness.dark,
                    dynamicSchemeVariant: DynamicSchemeVariant.expressive,
                  )
                : isMonochrome(themeController.seedColor)
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
                .apply(fontFamily: 'FlexFontEn')
                .copyWith(
                  bodyLarge: TextStyle(
                    color: colorThemeDark.onSurface,

                    fontFamily: 'FlexFontEn',
                  ),
                  bodyMedium: TextStyle(
                    color: colorThemeDark.onSurface,
                    fontFamily: 'FlexFontEn',
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
