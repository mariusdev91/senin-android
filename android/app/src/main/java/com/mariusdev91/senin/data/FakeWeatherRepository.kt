package com.mariusdev91.senin.data

import com.mariusdev91.senin.model.CityOption
import com.mariusdev91.senin.model.CurrentWeather
import com.mariusdev91.senin.model.DailyForecast
import com.mariusdev91.senin.model.HourlyForecast
import com.mariusdev91.senin.model.WeatherCondition
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

    override fun searchCities(query: String): List<CityOption> {
        if (query.isBlank()) return favoriteCities() + cities.filter { !it.isDefault }.take(4)

        val normalizedQuery = query.trim().lowercase()
        return cities.filter { city ->
            city.name.lowercase().contains(normalizedQuery) ||
                city.region.lowercase().contains(normalizedQuery) ||
                city.country.lowercase().contains(normalizedQuery)
        }
    }

    override fun weatherFor(city: CityOption): WeatherOverview {
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
                updatedAtLabel = "actualizat acum 3 min",
                sourceLabel = "Preview data pentru designul initial",
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
        "oradea" to WeatherSeed(14, 13, WeatherCondition.PartlyCloudy, "Lumină bună și nori subțiri peste oraș.", 18, 58, 16, 4, 17, 8),
        "bucharest" to WeatherSeed(17, 18, WeatherCondition.Clear, "Zi caldă, cu mult soare și aer uscat.", 8, 44, 13, 5, 20, 10),
        "cluj" to WeatherSeed(11, 10, WeatherCondition.Cloudy, "Nori compacți și un ritm mai rece decât ieri.", 28, 65, 18, 3, 14, 6),
        "timisoara" to WeatherSeed(15, 15, WeatherCondition.PartlyCloudy, "Cer variabil și o după-amiază plăcută.", 16, 54, 14, 4, 18, 9),
        "iasi" to WeatherSeed(9, 7, WeatherCondition.Rain, "Front umed, averse scurte și aer răcoros.", 62, 79, 20, 2, 11, 5),
        "london" to WeatherSeed(10, 8, WeatherCondition.Rain, "Plouă mărunt și destul de constant.", 74, 81, 19, 1, 12, 6),
        "lisbon" to WeatherSeed(19, 20, WeatherCondition.Clear, "Lumină aurie și vreme deschisă, cu vânt lejer.", 6, 41, 12, 6, 22, 13),
        "reykjavik" to WeatherSeed(3, 0, WeatherCondition.Mist, "Aer rece, ceață joasă și atmosferă calmă.", 24, 88, 11, 1, 5, -1),
        "new-york" to WeatherSeed(13, 12, WeatherCondition.Cloudy, "Cer dens și rafale moderate pe parcursul zilei.", 36, 61, 24, 3, 16, 7),
        "tokyo" to WeatherSeed(18, 19, WeatherCondition.Thunderstorm, "Căldură umedă și instabilitate spre seară.", 68, 76, 17, 5, 21, 14),
    )
}
