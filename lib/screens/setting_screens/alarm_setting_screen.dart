import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:settings_tiles/settings_tiles.dart';
import '../../controllers/theme_controller.dart';
import 'package:provider/provider.dart';
import '../../helpers/preferences_helper.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import '../../helpers/icon_helper.dart';
import 'package:flutter/services.dart';
import '../../services/alarm_service.dart';
import 'package:flutter_volume_controller/flutter_volume_controller.dart';

class AlarmSettingScreen extends StatefulWidget {
  const AlarmSettingScreen({super.key});

  @override
  State<AlarmSettingScreen> createState() => _AlarmSettingScreenState();
}

class _AlarmSettingScreenState extends State<AlarmSettingScreen> {
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

  @override
  Widget build(BuildContext context) {
    final themeController = Provider.of<ThemeController>(context);

    final alwaysRunService =
        PreferencesHelper.getBool('alwaysRunService') ?? false;
    const MethodChannel _channel = MethodChannel('com.pranshulgg.alarm/alarm');

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surface,
      body: CustomScrollView(
        slivers: [
          SliverAppBar.large(
            title: Text('alarm'.tr()),
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
                  ],
                ),
                SizedBox(height: 10),
                SettingSection(
                  styleTile: true,
                  title: SettingSectionTitle("Services", noPadding: true),
                  tiles: [
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
              ],
            ),
          ),
        ],
      ),
    );
  }
}
