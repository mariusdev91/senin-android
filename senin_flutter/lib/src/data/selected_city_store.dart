import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';

import '../models/city_option.dart';

class SelectedCityStore {
  const SelectedCityStore();

  static const _key = 'selected_city';

  Future<CityOption?> load() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_key);
    if (raw == null || raw.isEmpty) return null;
    final json = jsonDecode(raw) as Map<String, dynamic>;
    return CityOption.fromJson(json);
  }

  Future<void> save(CityOption city) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, jsonEncode(city.toJson()));
  }
}
