import 'package:flutter/material.dart';

import 'weather_models.dart';

IconData conditionIcon(WeatherCondition condition) {
  switch (condition) {
    case WeatherCondition.clear:
      return Icons.wb_sunny_rounded;
    case WeatherCondition.partlyCloudy:
      return Icons.wb_cloudy_rounded;
    case WeatherCondition.cloudy:
      return Icons.cloud_rounded;
    case WeatherCondition.rain:
      return Icons.umbrella_rounded;
    case WeatherCondition.thunderstorm:
      return Icons.thunderstorm_rounded;
    case WeatherCondition.snow:
      return Icons.ac_unit_rounded;
    case WeatherCondition.mist:
      return Icons.cloud_rounded;
  }
}

String conditionLabel(WeatherCondition condition) {
  switch (condition) {
    case WeatherCondition.clear:
      return 'Senin';
    case WeatherCondition.partlyCloudy:
      return 'Cer variabil';
    case WeatherCondition.cloudy:
      return 'Înnorat';
    case WeatherCondition.rain:
      return 'Ploaie';
    case WeatherCondition.thunderstorm:
      return 'Furtuni';
    case WeatherCondition.snow:
      return 'Ninsoare';
    case WeatherCondition.mist:
      return 'Ceață';
  }
}
