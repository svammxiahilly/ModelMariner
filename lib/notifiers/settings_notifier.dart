import 'package:flutter/material.dart';
import '../helpers/preferences_helper.dart';

class UnitSettingsNotifier extends ChangeNotifier {
  // Default values
  String _timeFormat = "12 hr";
  bool _showSeconds = false;
  String _clockStyleMain = "Digital";
  bool _useExpressiveVariant = false;
  // Getters
  String get timeFormat => _timeFormat;
  bool get showSeconds => _showSeconds;
  String get clockStyleMain => _clockStyleMain;
  bool get useExpressiveVariant => _useExpressiveVariant;
  UnitSettingsNotifier() {
    _loadAllSettings();
  }

  Future<void> _loadAllSettings() async {
    _timeFormat =
        await PreferencesHelper.getString("timeFormat") ?? _timeFormat;
    _showSeconds =
        await PreferencesHelper.getBool("showSeconds") ?? _showSeconds;
    _clockStyleMain =
        await PreferencesHelper.getString("ClockStyle") ?? _clockStyleMain;
    _useExpressiveVariant =
        await PreferencesHelper.getBool("useExpressiveVariant") ??
        _useExpressiveVariant;
    notifyListeners();
  }

  // Setters with notification

  void updateTimeUnit(String value) {
    if (_timeFormat != value) {
      _timeFormat = value;
      PreferencesHelper.setString("timeFormat", value);
      notifyListeners();
    }
  }

  void updateShowSeconds(bool value) {
    if (_showSeconds != value) {
      _showSeconds = value;
      PreferencesHelper.setBool("showSeconds", value);
      notifyListeners();
    }
  }

  void updateClockStyleMain(String value) {
    if (_clockStyleMain != value) {
      _clockStyleMain = value;
      PreferencesHelper.setString("ClockStyle", value);
      notifyListeners();
    }
  }

  void updateColorVariant(bool value) {
    _useExpressiveVariant = value;
    PreferencesHelper.setBool("useExpressiveVariant", value);
    notifyListeners();
  }
}
