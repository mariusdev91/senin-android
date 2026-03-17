enum WeatherCondition {
  clear,
  partlyCloudy,
  cloudy,
  rain,
  thunderstorm,
  snow,
  mist,
}

class WeatherOverview {
  const WeatherOverview({
    required this.current,
    required this.hourly,
    required this.daily,
    required this.updatedAtLabel,
    required this.sourceLabel,
  });

  final CurrentWeather current;
  final List<HourlyForecast> hourly;
  final List<DailyForecast> daily;
  final String updatedAtLabel;
  final String sourceLabel;
}

class CurrentWeather {
  const CurrentWeather({
    required this.temperatureC,
    required this.feelsLikeC,
    required this.condition,
    required this.summary,
    required this.precipitationChance,
    required this.humidity,
    required this.windKph,
    required this.uvIndex,
  });

  final int temperatureC;
  final int feelsLikeC;
  final WeatherCondition condition;
  final String summary;
  final int precipitationChance;
  final int humidity;
  final int windKph;
  final int uvIndex;
}

class HourlyForecast {
  const HourlyForecast({
    required this.timeLabel,
    required this.temperatureC,
    required this.precipitationChance,
    required this.windKph,
    required this.condition,
  });

  final String timeLabel;
  final int temperatureC;
  final int precipitationChance;
  final int windKph;
  final WeatherCondition condition;
}

class DailyForecast {
  const DailyForecast({
    required this.dayLabel,
    required this.highC,
    required this.lowC,
    required this.precipitationChance,
    required this.condition,
  });

  final String dayLabel;
  final int highC;
  final int lowC;
  final int precipitationChance;
  final WeatherCondition condition;
}
