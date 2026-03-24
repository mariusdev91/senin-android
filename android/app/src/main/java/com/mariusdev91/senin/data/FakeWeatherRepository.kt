package com.mariusdev91.senin.data

import com.mariusdev91.senin.i18n.AppLanguage
import com.mariusdev91.senin.i18n.AppStrings
import com.mariusdev91.senin.model.AirQuality
import com.mariusdev91.senin.model.CityOption
import com.mariusdev91.senin.model.CurrentWeather
import com.mariusdev91.senin.model.DailyForecast
import com.mariusdev91.senin.model.HourlyForecast
import com.mariusdev91.senin.model.SavedLocationPreview
import com.mariusdev91.senin.model.SunSchedule
import com.mariusdev91.senin.model.WeatherCondition
import com.mariusdev91.senin.model.WeatherDetails
import com.mariusdev91.senin.model.WeatherOverview
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.absoluteValue

class FakeWeatherRepository : WeatherRepository {
    private val cities = listOf(
        CityOption("oradea", "Oradea", "Bihor", "Romania", "RO", 47.05, 21.93, true),
        CityOption("bucharest", "Bucuresti", "Bucuresti", "Romania", "RO", 44.43, 26.10),
        CityOption("cluj", "Cluj-Napoca", "Cluj", "Romania", "RO", 46.77, 23.59),
        CityOption("timisoara", "Timisoara", "Timis", "Romania", "RO", 45.76, 21.23),
        CityOption("iasi", "Iasi", "Iasi", "Romania", "RO", 47.16, 27.58),
        CityOption("london", "London", "England", "United Kingdom", "GB", 51.51, -0.13),
        CityOption("lisbon", "Lisbon", "Lisbon", "Portugal", "PT", 38.72, -9.14),
        CityOption("reykjavik", "Reykjavik", "Capital Region", "Iceland", "IS", 64.15, -21.94),
        CityOption("new-york", "New York", "New York", "United States", "US", 40.71, -74.00),
        CityOption("tokyo", "Tokyo", "Tokyo", "Japan", "JP", 35.67, 139.65),
    )

    override fun defaultCity(): CityOption = cities.first { it.isDefault }

    override fun favoriteCities(): List<CityOption> = cities.filter {
        it.id in setOf("oradea", "bucharest", "cluj", "london", "tokyo")
    }

    override suspend fun searchCities(query: String, language: AppLanguage): List<CityOption> {
        if (query.isBlank()) return favoriteCities() + cities.filter { !it.isDefault }.take(4)

        val normalizedQuery = query.trim().lowercase()
        return cities.filter { city ->
            city.name.lowercase().contains(normalizedQuery) ||
                city.region.lowercase().contains(normalizedQuery) ||
                city.country.lowercase().contains(normalizedQuery)
        }
    }

    override suspend fun currentFor(city: CityOption, language: AppLanguage): SavedLocationPreview {
        val strings = AppStrings(language)
        val seed = weatherSeeds[city.id] ?: weatherSeeds.getValue(defaultCity().id)
        return SavedLocationPreview(
            cityId = city.id,
            localTimeLabel = "10:45",
            conditionLabel = strings.localizedCondition(seed.condition),
            condition = seed.condition,
            temperatureC = seed.currentTemp,
        )
    }

    override suspend fun weatherFor(city: CityOption, language: AppLanguage): WeatherOverview {
        val strings = AppStrings(language)
        val seed = weatherSeeds[city.id] ?: weatherSeeds.getValue(defaultCity().id)
        return seed.toOverview(city, strings)
    }

    private data class WeatherSeed(
        val currentTemp: Int,
        val feelsLike: Int,
        val condition: WeatherCondition,
        val precipitationChance: Int,
        val humidity: Int,
        val windKph: Int,
        val uvIndex: Int,
        val pressureHpa: Int,
        val visibilityKm: Int,
        val windDirectionLabel: String,
        val airQualityValue: Int,
        val dayHigh: Int,
        val dayLow: Int,
    ) {
        fun toOverview(city: CityOption, strings: AppStrings): WeatherOverview {
            val delta = city.name.hashCode().absoluteValue % 3
            val airCategory = strings.aqiCategory(airQualityValue)
            val todayDate = LocalDate.now()
            val nowTime = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
            val today = DailyForecast(
                todayDate,
                strings.localizedDayLabel(0, todayDate.dayOfWeek),
                dayHigh,
                dayLow,
                precipitationChance,
                condition,
            )
            return WeatherOverview(
                current = CurrentWeather(
                    temperatureC = currentTemp,
                    feelsLikeC = feelsLike,
                    condition = condition,
                    summary = strings.weatherSummary(condition, city.name, dayHigh, dayLow, precipitationChance),
                    precipitationChance = precipitationChance,
                    humidity = humidity,
                    windKph = windKph,
                    uvIndex = uvIndex,
                ),
                hourly = listOf(
                    HourlyForecast(nowTime, strings.now, currentTemp, precipitationChance, windKph, condition),
                    HourlyForecast(nowTime.plusHours(1), nowTime.plusHours(1).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), currentTemp + 1, (precipitationChance - 6).coerceAtLeast(5), windKph + 1, condition),
                    HourlyForecast(nowTime.plusHours(4), nowTime.plusHours(4).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), dayHigh, (precipitationChance - 10).coerceAtLeast(5), windKph + 2, condition),
                    HourlyForecast(nowTime.plusHours(7), nowTime.plusHours(7).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), dayHigh - 1, precipitationChance + 4, windKph + 3, condition.laterDayCondition()),
                    HourlyForecast(nowTime.plusHours(10), nowTime.plusHours(10).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), dayLow + 4, precipitationChance + 6, windKph + 1, WeatherCondition.PartlyCloudy),
                ),
                daily = listOf(
                    today,
                    DailyForecast(todayDate.plusDays(1), strings.localizedDayLabel(1, todayDate.plusDays(1).dayOfWeek), dayHigh + delta, dayLow + 1, (precipitationChance + 8).coerceAtMost(95), condition.nextDayCondition()),
                    DailyForecast(todayDate.plusDays(2), strings.localizedDayLabel(2, todayDate.plusDays(2).dayOfWeek), dayHigh - 1, dayLow - 1, (precipitationChance + 4).coerceAtMost(90), WeatherCondition.PartlyCloudy),
                    DailyForecast(todayDate.plusDays(3), strings.localizedDayLabel(3, todayDate.plusDays(3).dayOfWeek), dayHigh + 2, dayLow, (precipitationChance - 12).coerceAtLeast(5), WeatherCondition.Clear),
                    DailyForecast(todayDate.plusDays(4), strings.localizedDayLabel(4, todayDate.plusDays(4).dayOfWeek), dayHigh + 1, dayLow - 2, (precipitationChance + 10).coerceAtMost(95), condition.nextDayCondition()),
                ),
                details = WeatherDetails(
                    airQuality = AirQuality(
                        aqi = airQualityValue,
                        category = airCategory,
                        primaryPollutant = "O3",
                        description = strings.aqiDescription(airCategory),
                    ),
                    visibilityKm = visibilityKm,
                    pressureHpa = pressureHpa,
                    humidity = humidity,
                    windKph = windKph,
                    windDirectionLabel = windDirectionLabel,
                    uvIndex = uvIndex,
                    precipitationChance = precipitationChance,
                    sunSchedule = SunSchedule(
                        sunriseLabel = "06:14",
                        sunsetLabel = "19:56",
                        daylightDurationLabel = "13h 42m",
                    ),
                ),
                updatedAtLabel = strings.updatedAt("10:45", ""),
                sourceLabel = strings.sourceLiveOpenMeteo,
            )
        }

        private fun WeatherCondition.laterDayCondition(): WeatherCondition =
            when (this) {
                WeatherCondition.Clear -> WeatherCondition.PartlyCloudy
                WeatherCondition.PartlyCloudy -> WeatherCondition.Cloudy
                WeatherCondition.Cloudy -> WeatherCondition.Rain
                WeatherCondition.Rain -> WeatherCondition.Thunderstorm
                WeatherCondition.Thunderstorm -> WeatherCondition.Rain
                WeatherCondition.Snow -> WeatherCondition.Cloudy
                WeatherCondition.Mist -> WeatherCondition.PartlyCloudy
            }

        private fun WeatherCondition.nextDayCondition(): WeatherCondition =
            when (this) {
                WeatherCondition.Clear -> WeatherCondition.PartlyCloudy
                WeatherCondition.PartlyCloudy -> WeatherCondition.Clear
                WeatherCondition.Cloudy -> WeatherCondition.PartlyCloudy
                WeatherCondition.Rain -> WeatherCondition.Cloudy
                WeatherCondition.Thunderstorm -> WeatherCondition.Rain
                WeatherCondition.Snow -> WeatherCondition.Cloudy
                WeatherCondition.Mist -> WeatherCondition.Clear
            }
    }

    private val weatherSeeds = mapOf(
        "oradea" to WeatherSeed(14, 13, WeatherCondition.PartlyCloudy, 18, 58, 16, 4, 1014, 10, "NV", 42, 17, 8),
        "bucharest" to WeatherSeed(17, 18, WeatherCondition.Clear, 8, 44, 13, 5, 1011, 10, "NE", 51, 20, 10),
        "cluj" to WeatherSeed(11, 10, WeatherCondition.Cloudy, 28, 65, 18, 3, 1008, 8, "V", 55, 14, 6),
        "timisoara" to WeatherSeed(15, 15, WeatherCondition.PartlyCloudy, 16, 54, 14, 4, 1015, 10, "SV", 44, 18, 9),
        "iasi" to WeatherSeed(9, 7, WeatherCondition.Rain, 62, 79, 20, 2, 1004, 7, "N", 61, 11, 5),
        "london" to WeatherSeed(10, 8, WeatherCondition.Rain, 74, 81, 19, 1, 1006, 9, "V", 72, 12, 6),
        "lisbon" to WeatherSeed(19, 20, WeatherCondition.Clear, 6, 41, 12, 6, 1017, 10, "E", 35, 22, 13),
        "reykjavik" to WeatherSeed(3, 0, WeatherCondition.Mist, 24, 88, 11, 1, 998, 5, "NV", 30, 5, -1),
        "new-york" to WeatherSeed(13, 12, WeatherCondition.Cloudy, 36, 61, 24, 3, 1010, 10, "SV", 68, 16, 7),
        "tokyo" to WeatherSeed(18, 19, WeatherCondition.Thunderstorm, 68, 76, 17, 5, 1007, 8, "SE", 79, 21, 14),
    )
}
