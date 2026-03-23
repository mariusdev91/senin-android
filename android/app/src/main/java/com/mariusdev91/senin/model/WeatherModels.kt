package com.mariusdev91.senin.model

data class WeatherOverview(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val details: WeatherDetails,
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

data class WeatherDetails(
    val airQuality: AirQuality,
    val visibilityKm: Int,
    val pressureHpa: Int,
    val humidity: Int,
    val windKph: Int,
    val windDirectionLabel: String,
    val uvIndex: Int,
    val precipitationChance: Int,
    val sunSchedule: SunSchedule,
)

data class AirQuality(
    val aqi: Int,
    val category: String,
    val primaryPollutant: String,
    val description: String,
)

data class SunSchedule(
    val sunriseLabel: String,
    val sunsetLabel: String,
    val daylightDurationLabel: String,
)

data class SavedLocationPreview(
    val cityId: String,
    val localTimeLabel: String,
    val conditionLabel: String,
    val condition: WeatherCondition,
    val temperatureC: Int,
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
