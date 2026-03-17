import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';

import '../models/city_option.dart';

class FavoriteCitiesStore {
  const FavoriteCitiesStore();

  static const _key = 'favorite_cities';

  Future<bool> hasStoredFavorites() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.containsKey(_key);
  }

  Future<List<CityOption>> load() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_key);
    if (raw == null || raw.isEmpty) return const <CityOption>[];
    final json = jsonDecode(raw) as List<dynamic>;
    return json
        .whereType<Map<String, dynamic>>()
        .map(CityOption.fromJson)
        .toList();
  }

  Future<void> save(List<CityOption> cities) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(
      _key,
      jsonEncode(cities.map((city) => city.toJson()).toList()),
    );
  }
}
