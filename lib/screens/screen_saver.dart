import 'dart:async';
import 'dart:math';
import 'package:intl/intl.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_analog_clock/flutter_analog_clock.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import '../helpers/preferences_helper.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../controllers/theme_controller.dart';

class ScreenSaver extends StatefulWidget {
  const ScreenSaver({super.key});

  @override
  State<ScreenSaver> createState() => _ScreenSaverState();
}

class _ScreenSaverState extends State<ScreenSaver> {
  static const MethodChannel _channelBrightness = MethodChannel(
    'com.pranshulgg.alarm/alarm',
  );
  final Random _random = Random();
  double? _originalBrightness;
  double _top = 100;
  double _left = 100;
  double _opacity = 1.0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      _immersiveActive = true;
      _startScreenSaverLoop();
      _keepImmersive();
    });
    Future.delayed(const Duration(milliseconds: 1000), () {
      _enableWakeLock();
      _setDimBrightness();
    });
  }

  Future<void> _enableWakeLock() async {
    bool wakelockEnabled = await WakelockPlus.enabled;

    if (!wakelockEnabled) {
      WakelockPlus.toggle(enable: true);
    }
  }

  bool _immersiveActive = true;

  void _keepImmersive() {
    if (!_immersiveActive) return;
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
    WidgetsBinding.instance.addPostFrameCallback((_) => _keepImmersive());
  }

  Future<void> _setDimBrightness() async {
    try {
      await _channelBrightness.invokeMethod('setBrightness', {
        "brightness": 0.0,
      });
    } on PlatformException catch (e) {}
  }

  Timer? _timer;

  void _startScreenSaverLoop() {
    const fadeDuration = Duration(seconds: 2);
    const stayDuration = Duration(seconds: 5);
    const interval = Duration(seconds: 5 + 2 + 2);

    _timer = Timer.periodic(interval, (timer) async {
      if (!mounted) {
        timer.cancel();
        return;
      }
      // SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
      setState(() => _opacity = 0.0);
      await Future.delayed(fadeDuration);

      if (!mounted) return;

      final screenSize = MediaQuery.of(context).size;
      setState(() {
        _top = Random().nextDouble() * (screenSize.height - 215);
        _left = Random().nextDouble() * (screenSize.width - 242);
      });

      if (!mounted) return;

      setState(() => _opacity = 1.0);
      await Future.delayed(fadeDuration);

      await Future.delayed(stayDuration);
    });
  }

  @override
  void dispose() {
    _immersiveActive = false;
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final themeController = Provider.of<ThemeController>(
      context,
      listen: false,
    );
    final colorTheme = ColorScheme.fromSeed(
      seedColor: themeController.seedColor,
      brightness: Brightness.dark,
    );
    final isNightMode =
        PreferencesHelper.getBool("FullBlackScreenSaver") ?? false;

    final clockStyle = PreferencesHelper.getString("ScreenSaverClockStyle");
    double brightness =
        PreferencesHelper.getDouble("screensaverBrightness") ?? 0.3;
    return PopScope(
      canPop: true,
      child: Scaffold(
        backgroundColor: Colors.black,
        body: GestureDetector(
          behavior: HitTestBehavior.opaque,
          onTapDown: (_) {
            _immersiveActive = false;
            Navigator.of(context).pop(true);
          },
          child: Stack(
            children: [
              AnimatedPositioned(
                duration: const Duration(milliseconds: 0),
                top: _top,
                left: _left,
                child: AnimatedOpacity(
                  duration: const Duration(seconds: 1),
                  opacity: _opacity,
                  child: clockStyle == 'Digital'
                      ? DigitalClock()
                      : SizedBox(
                          width: 242,
                          height: 215,
                          child: Stack(
                            alignment: Alignment.center,
                            children: [
                              SvgPicture.string(
                                buildClockFaceSvg(
                                  isNightMode
                                      ? Color(0xff0e0e0e)
                                      : colorTheme.surfaceContainer,
                                  isNightMode
                                      ? Color(0xff000000)
                                      : colorTheme.primaryContainer,
                                ),
                              ),
                              AnalogClock(
                                dialColor: Colors.transparent,
                                hourNumberColor: null,
                                markingColor: null,
                                hourHandColor: isNightMode
                                    ? Color.fromARGB(255, 83, 83, 83)
                                    : colorTheme.primary,
                                hourHandWidthFactor: 1.4,
                                minuteHandWidthFactor: 1.2,
                                hourHandLengthFactor: 0.9,
                                secondHandWidthFactor: 1.4,
                                centerPointWidthFactor: 0.9,
                                secondHandLengthFactor: 0.9,
                                minuteHandColor: isNightMode
                                    ? Color.fromARGB(255, 83, 83, 83)
                                    : colorTheme.secondary,
                                centerPointColor: isNightMode
                                    ? Color.fromARGB(255, 0, 0, 0)
                                    : colorTheme.secondary,
                                secondHandColor: isNightMode
                                    ? Color.fromARGB(255, 83, 83, 83)
                                    : colorTheme.onSurface,
                              ),
                            ],
                          ),
                        ),
                ),
              ),
              Container(
                color: Colors.black.withValues(alpha: 1.0 - brightness),
                width: double.infinity,
                height: double.infinity,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

String buildClockFaceSvg(Color colorSurfaceContainer, Color colorPrimary) {
  final hexColorSurfaceContainer =
      '#${colorSurfaceContainer.value.toRadixString(16).padLeft(8, '0').substring(2)}';
  final hexColorPrimary =
      '#${colorPrimary.value.toRadixString(16).padLeft(8, '0').substring(2)}';

  String returnSvg = '';

  returnSvg =
      ''' <svg width="242" height="215" viewBox="0 0 242 215" fill="none" xmlns="http://www.w3.org/2000/svg">
<circle cx="121" cy="107.028" r="107" fill="$hexColorSurfaceContainer"/>
<path d="M189.589 140.843C184.891 140.843 180.717 140.038 177.067 138.426C173.437 136.796 170.583 134.553 168.506 131.7C166.429 128.846 165.381 125.565 165.361 121.857H181.668C181.688 123.003 182.037 124.032 182.717 124.944C183.415 125.837 184.367 126.536 185.57 127.041C186.774 127.545 188.152 127.798 189.705 127.798C191.2 127.798 192.52 127.536 193.665 127.011C194.811 126.468 195.704 125.721 196.344 124.769C196.985 123.818 197.296 122.731 197.276 121.508C197.296 120.304 196.927 119.237 196.17 118.305C195.432 117.373 194.393 116.645 193.054 116.121C191.714 115.597 190.171 115.335 188.424 115.335H182.367V104.153H188.424C190.035 104.153 191.452 103.891 192.675 103.367C193.918 102.842 194.879 102.114 195.558 101.183C196.257 100.251 196.597 99.1831 196.577 97.9795C196.597 96.8148 196.315 95.7859 195.733 94.8929C195.15 93.9999 194.335 93.301 193.287 92.7963C192.258 92.2916 191.064 92.0392 189.705 92.0392C188.23 92.0392 186.91 92.3013 185.745 92.8254C184.6 93.3496 183.697 94.0775 183.037 95.0094C182.377 95.9412 182.037 97.0089 182.018 98.2125H166.526C166.546 94.5629 167.545 91.3403 169.525 88.5449C171.525 85.7494 174.262 83.5558 177.737 81.9639C181.212 80.3721 185.201 79.5761 189.705 79.5761C194.112 79.5761 198.004 80.3235 201.382 81.8183C204.779 83.3131 207.429 85.3709 209.332 87.9916C211.253 90.5929 212.205 93.5728 212.185 96.9312C212.224 100.27 211.079 103.007 208.749 105.143C206.439 107.278 203.508 108.54 199.955 108.928V109.394C204.77 109.918 208.39 111.394 210.817 113.82C213.243 116.228 214.437 119.256 214.398 122.906C214.418 126.4 213.369 129.496 211.253 132.195C209.157 134.893 206.235 137.009 202.489 138.543C198.761 140.076 194.461 140.843 189.589 140.843Z" fill="$hexColorPrimary"/>
<path d="M51.1883 78.5761C54.6632 78.5761 57.9537 79.1294 61.0597 80.2359C64.1658 81.323 66.9127 83.0411 69.3005 85.39C71.7077 87.739 73.5908 90.8062 74.9497 94.5918C76.328 98.3578 77.0268 102.92 77.0463 108.278C77.0657 113.17 76.4542 117.567 75.2117 121.469C73.9887 125.351 72.2319 128.661 69.9411 131.399C67.6698 134.136 64.9326 136.232 61.7295 137.688C58.5458 139.125 54.9932 139.843 51.0718 139.843C46.6651 139.843 42.7922 138.999 39.4532 137.31C36.1142 135.621 33.4546 133.35 31.4745 130.496C29.4944 127.642 28.3393 124.468 28.0093 120.974H43.9667C44.355 122.779 45.2091 124.09 46.5292 124.905C47.8493 125.701 49.3635 126.099 51.0718 126.099C54.4885 126.099 56.983 124.623 58.5555 121.673C60.1473 118.703 60.953 114.742 60.9724 109.792H60.623C59.8659 111.617 58.7205 113.18 57.1869 114.48C55.6533 115.781 53.8673 116.781 51.8289 117.48C49.7906 118.178 47.6357 118.528 45.3644 118.528C41.7731 118.528 38.6282 117.722 35.9298 116.111C33.2314 114.48 31.1251 112.257 29.6109 109.443C28.0967 106.608 27.3299 103.386 27.3105 99.775C27.291 95.543 28.2908 91.8351 30.3097 88.6514C32.3287 85.4677 35.1338 82.9926 38.7252 81.226C42.3166 79.4594 46.471 78.5761 51.1883 78.5761ZM51.3048 90.8062C49.6935 90.8062 48.257 91.1751 46.9951 91.9128C45.7527 92.6505 44.7723 93.6502 44.0541 94.9121C43.3552 96.1739 43.0155 97.6007 43.0349 99.1926C43.0543 100.784 43.4134 102.211 44.1123 103.473C44.8306 104.735 45.8012 105.735 47.0242 106.472C48.2667 107.21 49.6935 107.579 51.3048 107.579C52.489 107.579 53.5858 107.375 54.5953 106.967C55.6047 106.54 56.4783 105.948 57.216 105.191C57.9731 104.415 58.5555 103.522 58.9632 102.512C59.3902 101.483 59.5941 100.377 59.5747 99.1926C59.5553 97.6007 59.1864 96.1739 58.4681 94.9121C57.7499 93.6502 56.7695 92.6505 55.5271 91.9128C54.2847 91.1751 52.8772 90.8062 51.3048 90.8062Z" fill="$hexColorPrimary"/>
<path d="M121.819 200.843C118.344 200.843 115.044 200.3 111.918 199.212C108.812 198.106 106.056 196.369 103.649 194C101.241 191.632 99.3486 188.545 97.9703 184.74C96.6114 180.935 95.9416 176.325 95.961 170.909C95.9804 166.075 96.6017 161.726 97.8247 157.863C99.0671 154 100.824 150.709 103.095 147.992C105.386 145.274 108.123 143.197 111.307 141.76C114.491 140.304 118.033 139.576 121.935 139.576C126.323 139.576 130.186 140.421 133.525 142.11C136.864 143.798 139.524 146.06 141.504 148.894C143.503 151.709 144.668 154.815 144.998 158.212H129.041C128.672 156.524 127.818 155.291 126.478 154.514C125.158 153.718 123.644 153.32 121.935 153.32C118.538 153.32 116.044 154.796 114.452 157.747C112.879 160.697 112.074 164.58 112.035 169.394H112.384C113.141 167.569 114.287 166.007 115.82 164.706C117.354 163.405 119.14 162.406 121.178 161.707C123.217 161.008 125.372 160.659 127.643 160.659C131.254 160.659 134.408 161.474 137.107 163.105C139.824 164.735 141.94 166.968 143.455 169.802C144.969 172.636 145.716 175.878 145.697 179.528C145.716 183.799 144.707 187.536 142.668 190.739C140.649 193.942 137.844 196.427 134.253 198.193C130.662 199.96 126.517 200.843 121.819 200.843ZM121.703 188.613C123.314 188.613 124.741 188.244 125.983 187.507C127.245 186.769 128.225 185.759 128.924 184.478C129.642 183.197 129.992 181.741 129.972 180.11C129.992 178.46 129.642 177.004 128.924 175.742C128.225 174.461 127.245 173.452 125.983 172.714C124.741 171.976 123.314 171.607 121.703 171.607C120.518 171.607 119.421 171.821 118.412 172.248C117.422 172.656 116.558 173.248 115.82 174.024C115.083 174.781 114.5 175.684 114.073 176.732C113.666 177.761 113.452 178.887 113.433 180.11C113.452 181.741 113.821 183.197 114.539 184.478C115.257 185.759 116.238 186.769 117.48 187.507C118.723 188.244 120.13 188.613 121.703 188.613Z" fill="$hexColorPrimary"/>
<path d="M111.64 20.3915V80.0278H95.4496V35.3006H95.1002L82.0547 43.1045V29.3602L96.7308 20.3915H111.64ZM116.505 80.0278V68.3801L138.752 49.9767C140.227 48.7537 141.489 47.6083 142.537 46.5406C143.605 45.4535 144.42 44.3373 144.983 43.1919C145.566 42.0465 145.857 40.775 145.857 39.3773C145.857 37.8436 145.527 36.5333 144.867 35.4462C144.226 34.359 143.333 33.5243 142.188 32.9419C141.042 32.3401 139.722 32.0392 138.228 32.0392C136.733 32.0392 135.413 32.3401 134.267 32.9419C133.141 33.5437 132.268 34.427 131.647 35.5918C131.025 36.7565 130.715 38.1737 130.715 39.8432H115.34C115.34 35.65 116.281 32.0392 118.164 29.0108C120.047 25.9824 122.707 23.6528 126.143 22.0221C129.579 20.3915 133.607 19.5761 138.228 19.5761C143.003 19.5761 147.138 20.3429 150.632 21.8766C154.146 23.3908 156.854 25.5262 158.757 28.2828C160.679 31.0394 161.64 34.2717 161.64 37.9795C161.64 40.2703 161.164 42.5513 160.213 44.8226C159.261 47.0745 157.553 49.569 155.088 52.3062C152.622 55.0435 149.118 58.3048 144.576 62.0903L138.985 66.7494V67.0989H162.28V80.0278H116.505Z" fill="$hexColorPrimary"/>
</svg>
''';

  return returnSvg;
}

class DigitalClock extends StatefulWidget {
  const DigitalClock({super.key});

  @override
  State<DigitalClock> createState() => _DigitalClockState();
}

class _DigitalClockState extends State<DigitalClock> {
  final bool is24HourFormat =
      PreferencesHelper.getString("timeFormat") == '24 hr';
  late Timer _timer;

  DateTime _now = DateTime.now();

  @override
  void initState() {
    super.initState();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      setState(() {
        _now = DateTime.now();
      });
    });
  }

  @override
  void dispose() {
    _timer.cancel();
    super.dispose();
  }

  String get formattedTime {
    String pattern = is24HourFormat ? ('HH:mm') : ('hh:mm');
    return DateFormat(pattern).format(_now);
  }

  String get amPm {
    if (is24HourFormat) return '';
    return DateFormat('a').format(_now);
  }

  String get dateFormat1 => DateFormat('EE, dd').format(_now);

  @override
  Widget build(BuildContext context) {
    final themeController = Provider.of<ThemeController>(
      context,
      listen: false,
    );
    final colorTheme = ColorScheme.fromSeed(
      seedColor: themeController.seedColor,
      brightness: Brightness.dark,
    );

    final isNightMode =
        PreferencesHelper.getBool("FullBlackScreenSaver") ?? false;

    return Column(
      spacing: 6,
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          decoration: BoxDecoration(
            color: isNightMode
                ? Color(0xff0e0e0e)
                : colorTheme.primaryContainer,
            borderRadius: BorderRadius.circular(999),
          ),
          padding: const EdgeInsets.only(
            left: 30,
            right: 30,
            top: 0,
            bottom: 0,
          ),
          child: Center(
            child: Text(
              formattedTime,

              style: TextStyle(
                fontSize: 76,

                color: isNightMode
                    ? Colors.grey[700]
                    : colorTheme.onPrimaryContainer,
              ),
            ),
          ),
        ),

        Container(
          decoration: BoxDecoration(
            color: isNightMode
                ? Color(0xff0e0e0e)
                : colorTheme.tertiaryContainer,
            borderRadius: BorderRadius.circular(16),
          ),
          padding: const EdgeInsets.only(
            left: 30,
            right: 30,
            top: 10,
            bottom: 10,
          ),
          child: Text(
            dateFormat1,

            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w500,
              color: isNightMode
                  ? Colors.grey[600]
                  : colorTheme.onTertiaryContainer,
            ),
          ),
        ),
      ],
    );
  }
}
