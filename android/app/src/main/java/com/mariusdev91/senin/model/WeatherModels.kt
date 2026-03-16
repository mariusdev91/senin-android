package com.mariusdev91.senin.model

data class WeatherOverview(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val updatedAtLabel: String,
    val sourceLabel: String,
)

data class CurrentWeather(
    val temperatureC: Int,
    val feelsLikeC: Int,
    val condition: WeatherCondition,
    val summary: String,
    val precipitationChance: Int,
    val humidity: Int,
    val windKph: Int,
    val uvIndex: Int,
)

data class HourlyForecast(
    val timeLabel: String,
    val temperatureC: Int,
    val precipitationChance: Int,
    val windKph: Int,
    val condition: WeatherCondition,
)

data class DailyForecast(
    val dayLabel: String,
    val highC: Int,
    val lowC: Int,
    val precipitationChance: Int,
    val condition: WeatherCondition,
)

enum class WeatherCondition {
    Clear,
    PartlyCloudy,
    Cloudy,
    Rain,
    Thunderstorm,
    Snow,
    Mist,
}
