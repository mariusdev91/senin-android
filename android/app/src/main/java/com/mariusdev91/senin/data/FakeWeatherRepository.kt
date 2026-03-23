package com.mariusdev91.senin.data

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

    override suspend fun searchCities(query: String): List<CityOption> {
        if (query.isBlank()) return favoriteCities() + cities.filter { !it.isDefault }.take(4)

        val normalizedQuery = query.trim().lowercase()
        return cities.filter { city ->
            city.name.lowercase().contains(normalizedQuery) ||
                city.region.lowercase().contains(normalizedQuery) ||
                city.country.lowercase().contains(normalizedQuery)
        }
    }

    override suspend fun currentFor(city: CityOption): SavedLocationPreview {
        val seed = weatherSeeds[city.id] ?: weatherSeeds.getValue(defaultCity().id)
        return SavedLocationPreview(
            cityId = city.id,
            localTimeLabel = "10:45",
            conditionLabel = seed.condition.label,
            condition = seed.condition,
            temperatureC = seed.currentTemp,
        )
    }

    override suspend fun weatherFor(city: CityOption): WeatherOverview {
        val seed = weatherSeeds[city.id] ?: weatherSeeds.getValue(defaultCity().id)
        return seed.toOverview(city)
    }

    private data class WeatherSeed(
        val currentTemp: Int,
        val feelsLike: Int,
        val condition: WeatherCondition,
        val summary: String,
        val precipitationChance: Int,
        val humidity: Int,
        val windKph: Int,
        val uvIndex: Int,
        val pressureHpa: Int,
        val visibilityKm: Int,
        val windDirectionLabel: String,
        val airQualityValue: Int,
        val airQualityCategory: String,
        val dayHigh: Int,
        val dayLow: Int,
    ) {
        fun toOverview(city: CityOption): WeatherOverview {
            val delta = city.name.hashCode().absoluteValue % 3
            return WeatherOverview(
                current = CurrentWeather(
                    temperatureC = currentTemp,
                    feelsLikeC = feelsLike,
                    condition = condition,
                    summary = summary,
                    precipitationChance = precipitationChance,
                    humidity = humidity,
                    windKph = windKph,
                    uvIndex = uvIndex,
                ),
                hourly = listOf(
                    HourlyForecast("Acum", currentTemp, precipitationChance, windKph, condition),
                    HourlyForecast("11:00", currentTemp + 1, (precipitationChance - 6).coerceAtLeast(5), windKph + 1, condition),
                    HourlyForecast("14:00", dayHigh, (precipitationChance - 10).coerceAtLeast(5), windKph + 2, condition),
                    HourlyForecast("17:00", dayHigh - 1, precipitationChance + 4, windKph + 3, condition.laterDayCondition()),
                    HourlyForecast("20:00", dayLow + 4, precipitationChance + 6, windKph + 1, WeatherCondition.PartlyCloudy),
                ),
                daily = listOf(
                    DailyForecast("Azi", dayHigh, dayLow, precipitationChance, condition),
                    DailyForecast("Maine", dayHigh + delta, dayLow + 1, (precipitationChance + 8).coerceAtMost(95), condition.nextDayCondition()),
                    DailyForecast("Miercuri", dayHigh - 1, dayLow - 1, (precipitationChance + 4).coerceAtMost(90), WeatherCondition.PartlyCloudy),
                    DailyForecast("Joi", dayHigh + 2, dayLow, (precipitationChance - 12).coerceAtLeast(5), WeatherCondition.Clear),
                    DailyForecast("Vineri", dayHigh + 1, dayLow - 2, (precipitationChance + 10).coerceAtMost(95), condition.nextDayCondition()),
                ),
                details = WeatherDetails(
                    airQuality = AirQuality(
                        aqi = airQualityValue,
                        category = airQualityCategory,
                        primaryPollutant = "O3",
                        description = "Date preview pentru ecranul de detalii.",
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
                updatedAtLabel = "Actualizat acum 3 min",
                sourceLabel = "Date preview pentru design",
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
        "oradea" to WeatherSeed(14, 13, WeatherCondition.PartlyCloudy, "Lumina buna si nori subtiri peste oras.", 18, 58, 16, 4, 1014, 10, "NV", 42, "Bun", 17, 8),
        "bucharest" to WeatherSeed(17, 18, WeatherCondition.Clear, "Zi calda, cu mult soare si aer uscat.", 8, 44, 13, 5, 1011, 10, "NE", 51, "Moderat", 20, 10),
        "cluj" to WeatherSeed(11, 10, WeatherCondition.Cloudy, "Nori compacti si un ritm mai rece decat ieri.", 28, 65, 18, 3, 1008, 8, "V", 55, "Moderat", 14, 6),
        "timisoara" to WeatherSeed(15, 15, WeatherCondition.PartlyCloudy, "Cer variabil si o dupa-amiaza placuta.", 16, 54, 14, 4, 1015, 10, "SV", 44, "Bun", 18, 9),
        "iasi" to WeatherSeed(9, 7, WeatherCondition.Rain, "Front umed, averse scurte si aer racoros.", 62, 79, 20, 2, 1004, 7, "N", 61, "Moderat", 11, 5),
        "london" to WeatherSeed(10, 8, WeatherCondition.Rain, "Ploua marunt si destul de constant.", 74, 81, 19, 1, 1006, 9, "V", 72, "Moderat", 12, 6),
        "lisbon" to WeatherSeed(19, 20, WeatherCondition.Clear, "Lumina aurie si vreme deschisa, cu vant lejer.", 6, 41, 12, 6, 1017, 10, "E", 35, "Bun", 22, 13),
        "reykjavik" to WeatherSeed(3, 0, WeatherCondition.Mist, "Aer rece, ceata joasa si atmosfera calma.", 24, 88, 11, 1, 998, 5, "NV", 30, "Bun", 5, -1),
        "new-york" to WeatherSeed(13, 12, WeatherCondition.Cloudy, "Cer dens si rafale moderate pe parcursul zilei.", 36, 61, 24, 3, 1010, 10, "SV", 68, "Moderat", 16, 7),
        "tokyo" to WeatherSeed(18, 19, WeatherCondition.Thunderstorm, "Caldura umeda si instabilitate spre seara.", 68, 76, 17, 5, 1007, 8, "SE", 79, "Moderat", 21, 14),
    )
}

private val WeatherCondition.label: String
    get() = when (this) {
        WeatherCondition.Clear -> "Senin"
        WeatherCondition.PartlyCloudy -> "Cer variabil"
        WeatherCondition.Cloudy -> "Innorat"
        WeatherCondition.Rain -> "Ploaie"
        WeatherCondition.Thunderstorm -> "Furtuni"
        WeatherCondition.Snow -> "Ninsoare"
        WeatherCondition.Mist -> "Ceata"
    }
