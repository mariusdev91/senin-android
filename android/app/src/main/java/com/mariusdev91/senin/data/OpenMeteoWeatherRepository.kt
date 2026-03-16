package com.mariusdev91.senin.data

import com.mariusdev91.senin.model.CityOption
import com.mariusdev91.senin.model.CurrentWeather
import com.mariusdev91.senin.model.DailyForecast
import com.mariusdev91.senin.model.HourlyForecast
import com.mariusdev91.senin.model.WeatherCondition
import com.mariusdev91.senin.model.WeatherOverview
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class OpenMeteoWeatherRepository : WeatherRepository {
    private val favorites = listOf(
        CityOption("oradea", "Oradea", "Bihor", "Romania", "RO", 47.0465, 21.9189, true),
        CityOption("bucharest", "Bucuresti", "Bucuresti", "Romania", "RO", 44.4268, 26.1025),
        CityOption("cluj", "Cluj-Napoca", "Cluj", "Romania", "RO", 46.7712, 23.6236),
        CityOption("timisoara", "Timisoara", "Timis", "Romania", "RO", 45.7489, 21.2087),
        CityOption("london", "London", "England", "United Kingdom", "GB", 51.5072, -0.1276),
        CityOption("tokyo", "Tokyo", "Tokyo", "Japan", "JP", 35.6764, 139.6500),
    )

    override fun defaultCity(): CityOption = favorites.first()

    override fun favoriteCities(): List<CityOption> = favorites

    override suspend fun searchCities(query: String): List<CityOption> = withContext(Dispatchers.IO) {
        val normalized = query.trim()
        if (normalized.isBlank()) return@withContext favorites
        if (normalized.length < 2) {
            return@withContext favorites.filter {
                it.name.contains(normalized, ignoreCase = true) ||
                    it.country.contains(normalized, ignoreCase = true)
            }
        }

        val encoded = URLEncoder.encode(normalized, StandardCharsets.UTF_8.name())
        val url = buildString {
            append("https://geocoding-api.open-meteo.com/v1/search?")
            append("name=").append(encoded)
            append("&count=12")
            append("&language=ro")
            append("&format=json")
        }

        val body = getJson(url)
        val results = body.optJSONArray("results") ?: JSONArray()
        buildList {
            for (index in 0 until results.length()) {
                val item = results.optJSONObject(index) ?: continue
                add(item.toCityOption())
            }
        }.distinctBy { "${it.name}|${it.countryCode}|${it.latitude}|${it.longitude}" }
    }

    override suspend fun weatherFor(city: CityOption): WeatherOverview = withContext(Dispatchers.IO) {
        val url = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=").append(city.latitude)
            append("&longitude=").append(city.longitude)
            append("&timezone=auto")
            append("&forecast_days=5")
            append("&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m")
            append("&hourly=temperature_2m,precipitation_probability,weather_code,wind_speed_10m")
            append("&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max")
            append("&wind_speed_unit=kmh")
        }

        val body = getJson(url)
        body.toWeatherOverview(city)
    }

    private fun getJson(url: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Senin-Android/0.1")
        }

        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw IOException("Open-Meteo error $code: $text")
            }
            JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toCityOption(): CityOption {
        val lat = optDouble("latitude")
        val lon = optDouble("longitude")
        val region = optString("admin1").ifBlank {
            optString("country").ifBlank { "Unknown" }
        }
        val country = optString("country").ifBlank { "Unknown" }
        val name = optString("name").ifBlank { "Unknown city" }
        val id = opt("id")?.toString()
            ?: "${name.lowercase()}-${country.lowercase()}-${lat}-${lon}"

        return CityOption(
            id = id,
            name = name,
            region = region,
            country = country,
            countryCode = optString("country_code"),
            latitude = lat,
            longitude = lon,
            isDefault = lat == defaultCity().latitude && lon == defaultCity().longitude,
        )
    }

    private fun JSONObject.toWeatherOverview(city: CityOption): WeatherOverview {
        val current = optJSONObject("current") ?: throw IOException("Missing current weather block")
        val hourly = optJSONObject("hourly") ?: throw IOException("Missing hourly weather block")
        val daily = optJSONObject("daily") ?: throw IOException("Missing daily weather block")

        val currentCondition = current.optInt("weather_code").toWeatherCondition()
        val dailyForecast = daily.toDailyForecast()
        val currentTime = current.optString("time")
        val hourlyForecast = hourly.toHourlyForecast(currentTime)
        val headline = currentCondition.summary(city, dailyForecast.firstOrNull())

        return WeatherOverview(
            current = CurrentWeather(
                temperatureC = current.optDouble("temperature_2m").roundToInt(),
                feelsLikeC = current.optDouble("apparent_temperature").roundToInt(),
                condition = currentCondition,
                summary = headline,
                precipitationChance = hourlyForecast.firstOrNull()?.precipitationChance ?: dailyForecast.firstOrNull()?.precipitationChance ?: 0,
                humidity = current.optDouble("relative_humidity_2m").roundToInt(),
                windKph = current.optDouble("wind_speed_10m").roundToInt(),
                uvIndex = 0,
            ),
            hourly = hourlyForecast,
            daily = dailyForecast,
            updatedAtLabel = "Actualizat ${currentTime.toHourLabel()} ${optString("timezone_abbreviation").ifBlank { "" }}".trim(),
            sourceLabel = "Date live via Open-Meteo",
        )
    }

    private fun JSONObject.toHourlyForecast(currentTime: String): List<HourlyForecast> {
        val times = optJSONArray("time") ?: JSONArray()
        val temps = optJSONArray("temperature_2m") ?: JSONArray()
        val rain = optJSONArray("precipitation_probability") ?: JSONArray()
        val wind = optJSONArray("wind_speed_10m") ?: JSONArray()
        val codes = optJSONArray("weather_code") ?: JSONArray()

        val startIndex = indexOfTime(times, currentTime).coerceAtLeast(0)
        val endExclusive = minOf(times.length(), startIndex + 6)

        return buildList {
            for (index in startIndex until endExclusive) {
                add(
                    HourlyForecast(
                        timeLabel = if (index == startIndex) "Acum" else times.optString(index).toHourLabel(),
                        temperatureC = temps.optDouble(index).roundToInt(),
                        precipitationChance = rain.optDouble(index).roundToInt(),
                        windKph = wind.optDouble(index).roundToInt(),
                        condition = codes.optInt(index).toWeatherCondition(),
                    ),
                )
            }
        }
    }

    private fun JSONObject.toDailyForecast(): List<DailyForecast> {
        val times = optJSONArray("time") ?: JSONArray()
        val highs = optJSONArray("temperature_2m_max") ?: JSONArray()
        val lows = optJSONArray("temperature_2m_min") ?: JSONArray()
        val rain = optJSONArray("precipitation_probability_max") ?: JSONArray()
        val codes = optJSONArray("weather_code") ?: JSONArray()

        return buildList {
            for (index in 0 until minOf(times.length(), 5)) {
                val date = LocalDate.parse(times.optString(index))
                add(
                    DailyForecast(
                        dayLabel = date.toDayLabel(index),
                        highC = highs.optDouble(index).roundToInt(),
                        lowC = lows.optDouble(index).roundToInt(),
                        precipitationChance = rain.optDouble(index).roundToInt(),
                        condition = codes.optInt(index).toWeatherCondition(),
                    ),
                )
            }
        }
    }

    private fun indexOfTime(times: JSONArray, target: String): Int {
        for (index in 0 until times.length()) {
            if (times.optString(index) == target) return index
        }
        return 0
    }

    private fun Int.toWeatherCondition(): WeatherCondition = when (this) {
        0 -> WeatherCondition.Clear
        1, 2 -> WeatherCondition.PartlyCloudy
        3 -> WeatherCondition.Cloudy
        45, 48 -> WeatherCondition.Mist
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.Rain
        71, 73, 75, 77, 85, 86 -> WeatherCondition.Snow
        95, 96, 99 -> WeatherCondition.Thunderstorm
        else -> WeatherCondition.Cloudy
    }

    private fun WeatherCondition.summary(city: CityOption, today: DailyForecast?): String {
        val prefix = when (this) {
            WeatherCondition.Clear -> "Cer senin"
            WeatherCondition.PartlyCloudy -> "Cer variabil"
            WeatherCondition.Cloudy -> "Innorat"
            WeatherCondition.Rain -> "Ploaie sau averse"
            WeatherCondition.Thunderstorm -> "Instabilitate si furtuni"
            WeatherCondition.Snow -> "Ninsori usoare"
            WeatherCondition.Mist -> "Ceata si vizibilitate redusa"
        }

        return if (today == null) {
            "$prefix acum in ${city.name}."
        } else {
            "$prefix azi in ${city.name}. Max ${today.highC}°, min ${today.lowC}°, ploaie ${today.precipitationChance}%."
        }
    }

    private fun String.toHourLabel(): String {
        if (isBlank()) return "--:--"
        return LocalDateTime.parse(this).format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    private fun LocalDate.toDayLabel(index: Int): String = when (index) {
        0 -> "Azi"
        1 -> "Maine"
        else -> when (dayOfWeek.value) {
            1 -> "Luni"
            2 -> "Marti"
            3 -> "Miercuri"
            4 -> "Joi"
            5 -> "Vineri"
            6 -> "Sambata"
            else -> "Duminica"
        }
    }
}
