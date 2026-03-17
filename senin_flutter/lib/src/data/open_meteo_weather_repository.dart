import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/city_option.dart';
import '../models/weather_models.dart';
import 'weather_repository.dart';

class OpenMeteoWeatherRepository implements WeatherRepository {
  final List<CityOption> _favorites = const <CityOption>[
    CityOption(
      id: 'oradea',
      name: 'Oradea',
      region: 'Bihor',
      country: 'România',
      countryCode: 'RO',
      latitude: 47.0465,
      longitude: 21.9189,
      isDefault: true,
    ),
    CityOption(
      id: 'bucharest',
      name: 'București',
      region: 'București',
      country: 'România',
      countryCode: 'RO',
      latitude: 44.4268,
      longitude: 26.1025,
    ),
    CityOption(
      id: 'cluj',
      name: 'Cluj-Napoca',
      region: 'Cluj',
      country: 'România',
      countryCode: 'RO',
      latitude: 46.7712,
      longitude: 23.6236,
    ),
    CityOption(
      id: 'timisoara',
      name: 'Timișoara',
      region: 'Timiș',
      country: 'România',
      countryCode: 'RO',
      latitude: 45.7489,
      longitude: 21.2087,
    ),
    CityOption(
      id: 'london',
      name: 'London',
      region: 'England',
      country: 'United Kingdom',
      countryCode: 'GB',
      latitude: 51.5072,
      longitude: -0.1276,
    ),
    CityOption(
      id: 'tokyo',
      name: 'Tokyo',
      region: 'Tokyo',
      country: 'Japan',
      countryCode: 'JP',
      latitude: 35.6764,
      longitude: 139.6500,
    ),
  ];

  @override
  CityOption defaultCity() => _favorites.first;

  @override
  List<CityOption> favoriteCities() => _favorites;

  @override
  Future<List<CityOption>> searchCities(String query) async {
    final normalized = query.trim();
    if (normalized.isEmpty) return _favorites;
    if (normalized.length < 2) {
      return _favorites
          .where(
            (city) =>
                city.name.toLowerCase().contains(normalized.toLowerCase()) ||
                city.country.toLowerCase().contains(normalized.toLowerCase()),
          )
          .toList();
    }

    final uri = Uri.parse(
      'https://geocoding-api.open-meteo.com/v1/search?name=${Uri.encodeQueryComponent(normalized)}&count=12&language=ro&format=json',
    );
    final response = await http.get(
      uri,
      headers: const <String, String>{
        'Accept': 'application/json',
        'User-Agent': 'Senin-Flutter/0.1',
      },
    );

    if (response.statusCode < 200 || response.statusCode > 299) {
      throw Exception('Serviciul meteo a răspuns cu o eroare temporară.');
    }

    final body = jsonDecode(response.body) as Map<String, dynamic>;
    final results = (body['results'] as List<dynamic>? ?? <dynamic>[])
        .whereType<Map<String, dynamic>>()
        .map(_cityFromJson)
        .toList();

    final unique = <String, CityOption>{};
    for (final city in results) {
      unique['${city.name}|${city.countryCode}|${city.latitude}|${city.longitude}'] = city;
    }
    return unique.values.toList();
  }

  @override
  Future<WeatherOverview> weatherFor(CityOption city) async {
    final uri = Uri.parse(
      'https://api.open-meteo.com/v1/forecast'
      '?latitude=${city.latitude}'
      '&longitude=${city.longitude}'
      '&timezone=auto'
      '&forecast_days=5'
      '&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m'
      '&hourly=temperature_2m,precipitation_probability,weather_code,wind_speed_10m'
      '&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max'
      '&wind_speed_unit=kmh',
    );

    final response = await http.get(
      uri,
      headers: const <String, String>{
        'Accept': 'application/json',
        'User-Agent': 'Senin-Flutter/0.1',
      },
    );

    if (response.statusCode < 200 || response.statusCode > 299) {
      throw Exception('Serviciul meteo a răspuns cu o eroare temporară.');
    }

    final body = jsonDecode(response.body) as Map<String, dynamic>;
    return _weatherFromJson(body, city);
  }

  CityOption _cityFromJson(Map<String, dynamic> json) {
    final latitude = (json['latitude'] as num?)?.toDouble();
    final longitude = (json['longitude'] as num?)?.toDouble();
    if (latitude == null || longitude == null) {
      throw const FormatException('Coordonate invalide');
    }

    final country = (json['country'] as String?)?.trim();
    final region = ((json['admin1'] as String?)?.trim().isNotEmpty ?? false)
        ? (json['admin1'] as String).trim()
        : (country?.isNotEmpty ?? false)
            ? country!
            : 'Necunoscut';

    final name = ((json['name'] as String?)?.trim().isNotEmpty ?? false)
        ? (json['name'] as String).trim()
        : 'Oraș necunoscut';

    return CityOption(
      id: '${json['id'] ?? '${name.toLowerCase()}-${country?.toLowerCase() ?? 'unknown'}-$latitude-$longitude'}',
      name: name,
      region: region,
      country: (country?.isNotEmpty ?? false) ? country! : 'Necunoscut',
      countryCode: (json['country_code'] as String?)?.trim() ?? '',
      latitude: latitude,
      longitude: longitude,
      isDefault: latitude == defaultCity().latitude &&
          longitude == defaultCity().longitude,
    );
  }

  WeatherOverview _weatherFromJson(Map<String, dynamic> body, CityOption city) {
    final current = body['current'] as Map<String, dynamic>?;
    final hourly = body['hourly'] as Map<String, dynamic>?;
    final daily = body['daily'] as Map<String, dynamic>?;

    if (current == null || hourly == null || daily == null) {
      throw const FormatException('Răspuns invalid de la serviciul meteo.');
    }

    final currentCondition =
        _weatherCondition((current['weather_code'] as num?)?.toInt() ?? 0);
    final dailyForecast = _dailyForecast(daily);
    final currentTime = (current['time'] as String?) ?? '';
    final hourlyForecast = _hourlyForecast(hourly, currentTime);

    return WeatherOverview(
      current: CurrentWeather(
        temperatureC: _roundedInt(current['temperature_2m']),
        feelsLikeC: _roundedInt(current['apparent_temperature']),
        condition: currentCondition,
        summary: _summary(currentCondition, city, dailyForecast.isNotEmpty ? dailyForecast.first : null),
        precipitationChance: hourlyForecast.isNotEmpty
            ? hourlyForecast.first.precipitationChance
            : (dailyForecast.isNotEmpty ? dailyForecast.first.precipitationChance : 0),
        humidity: _roundedInt(current['relative_humidity_2m']),
        windKph: _roundedInt(current['wind_speed_10m']),
        uvIndex: 0,
      ),
      hourly: hourlyForecast,
      daily: dailyForecast,
      updatedAtLabel:
          'Actualizat ${_hourLabel(currentTime)} ${((body['timezone_abbreviation'] as String?) ?? '').trim()}'.trim(),
      sourceLabel: 'Date live via Open-Meteo',
    );
  }

  List<HourlyForecast> _hourlyForecast(
    Map<String, dynamic> hourly,
    String currentTime,
  ) {
    final times = (hourly['time'] as List<dynamic>? ?? <dynamic>[])
        .whereType<String>()
        .toList();
    final temps = (hourly['temperature_2m'] as List<dynamic>? ?? <dynamic>[]);
    final rain = (hourly['precipitation_probability'] as List<dynamic>? ?? <dynamic>[]);
    final wind = (hourly['wind_speed_10m'] as List<dynamic>? ?? <dynamic>[]);
    final codes = (hourly['weather_code'] as List<dynamic>? ?? <dynamic>[]);

    final startIndex = _startIndexForCurrentTime(times, currentTime);
    final currentDate = DateTime.tryParse(currentTime);
    final items = <HourlyForecast>[];

    for (var index = startIndex; index < times.length; index++) {
      final forecastTime = DateTime.tryParse(times[index]);
      if (forecastTime == null) continue;
      if (currentDate != null &&
          (forecastTime.year != currentDate.year ||
              forecastTime.month != currentDate.month ||
              forecastTime.day != currentDate.day)) {
        break;
      }

      items.add(
        HourlyForecast(
          timeLabel: index == startIndex ? 'Acum' : _hourLabel(times[index]),
          temperatureC: _roundedInt(index < temps.length ? temps[index] : null),
          precipitationChance: _roundedInt(index < rain.length ? rain[index] : null),
          windKph: _roundedInt(index < wind.length ? wind[index] : null),
          condition: _weatherCondition(
            index < codes.length ? ((codes[index] as num?)?.toInt() ?? 0) : 0,
          ),
        ),
      );
    }

    return items;
  }

  List<DailyForecast> _dailyForecast(Map<String, dynamic> daily) {
    final times = (daily['time'] as List<dynamic>? ?? <dynamic>[])
        .whereType<String>()
        .toList();
    final highs = (daily['temperature_2m_max'] as List<dynamic>? ?? <dynamic>[]);
    final lows = (daily['temperature_2m_min'] as List<dynamic>? ?? <dynamic>[]);
    final rain = (daily['precipitation_probability_max'] as List<dynamic>? ?? <dynamic>[]);
    final codes = (daily['weather_code'] as List<dynamic>? ?? <dynamic>[]);

    final items = <DailyForecast>[];
    for (var index = 0; index < times.length && index < 5; index++) {
      final date = DateTime.tryParse(times[index]);
      if (date == null) continue;
      items.add(
        DailyForecast(
          dayLabel: _dayLabel(date, index),
          highC: _roundedInt(index < highs.length ? highs[index] : null),
          lowC: _roundedInt(index < lows.length ? lows[index] : null),
          precipitationChance: _roundedInt(index < rain.length ? rain[index] : null),
          condition: _weatherCondition(
            index < codes.length ? ((codes[index] as num?)?.toInt() ?? 0) : 0,
          ),
        ),
      );
    }

    return items;
  }

  int _startIndexForCurrentTime(List<String> times, String currentTime) {
    final targetTime = DateTime.tryParse(currentTime);
    if (targetTime == null) return 0;
    var candidate = 0;

    for (var index = 0; index < times.length; index++) {
      final time = DateTime.tryParse(times[index]);
      if (time == null) continue;
      if (time.isAfter(targetTime)) break;
      candidate = index;
    }

    return candidate;
  }

  WeatherCondition _weatherCondition(int code) {
    switch (code) {
      case 0:
        return WeatherCondition.clear;
      case 1:
      case 2:
        return WeatherCondition.partlyCloudy;
      case 3:
        return WeatherCondition.cloudy;
      case 45:
      case 48:
        return WeatherCondition.mist;
      case 71:
      case 73:
      case 75:
      case 77:
      case 85:
      case 86:
        return WeatherCondition.snow;
      case 95:
      case 96:
      case 99:
        return WeatherCondition.thunderstorm;
      default:
        if (<int>{51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82}.contains(code)) {
          return WeatherCondition.rain;
        }
        return WeatherCondition.cloudy;
    }
  }

  String _summary(
    WeatherCondition condition,
    CityOption city,
    DailyForecast? today,
  ) {
    final prefix = switch (condition) {
      WeatherCondition.clear => 'Cer senin',
      WeatherCondition.partlyCloudy => 'Cer variabil',
      WeatherCondition.cloudy => 'Înnorat',
      WeatherCondition.rain => 'Ploaie sau averse',
      WeatherCondition.thunderstorm => 'Instabilitate și furtuni',
      WeatherCondition.snow => 'Ninsori ușoare',
      WeatherCondition.mist => 'Ceață și vizibilitate redusă',
    };

    if (today == null) return '$prefix acum în ${city.name}.';
    return '$prefix azi în ${city.name}. Max ${today.highC}°, min ${today.lowC}°, ploaie ${today.precipitationChance}%.';
  }

  int _roundedInt(Object? value) {
    if (value is num) return value.round();
    return 0;
  }

  String _hourLabel(String raw) {
    final date = DateTime.tryParse(raw);
    if (date == null) return '--:--';
    final hour = date.hour.toString().padLeft(2, '0');
    return '$hour:00';
  }

  String _dayLabel(DateTime date, int index) {
    if (index == 0) return 'Azi';
    if (index == 1) return 'Mâine';

    const names = <int, String>{
      DateTime.monday: 'Luni',
      DateTime.tuesday: 'Marți',
      DateTime.wednesday: 'Miercuri',
      DateTime.thursday: 'Joi',
      DateTime.friday: 'Vineri',
      DateTime.saturday: 'Sâmbătă',
      DateTime.sunday: 'Duminică',
    };

    return names[date.weekday] ?? 'Zi';
  }
}
