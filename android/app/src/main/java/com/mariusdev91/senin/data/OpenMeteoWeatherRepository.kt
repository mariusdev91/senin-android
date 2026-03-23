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
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
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

    override suspend fun searchCities(query: String, language: AppLanguage): List<CityOption> = withContext(Dispatchers.IO) {
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
            append("&language=").append(language.code)
            append("&format=json")
        }

        val body = getJson(url)
        val results = body.optJSONArray("results") ?: JSONArray()
        buildList {
            for (index in 0 until results.length()) {
                val item = results.optJSONObject(index)?.toCityOptionOrNull() ?: continue
                add(item)
            }
        }.distinctBy { "${it.name}|${it.countryCode}|${it.latitude}|${it.longitude}" }
    }

    override suspend fun currentFor(city: CityOption, language: AppLanguage): SavedLocationPreview = withContext(Dispatchers.IO) {
        val strings = AppStrings(language)
        val url = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=").append(city.latitude)
            append("&longitude=").append(city.longitude)
            append("&timezone=auto")
            append("&current=temperature_2m,weather_code")
        }

        val body = getJson(url)
        val current = body.optJSONObject("current")
            ?: throw IOException("Lipseste blocul de vreme curenta.")

        SavedLocationPreview(
            cityId = city.id,
            localTimeLabel = current.optString("time").toHourLabel(),
            conditionLabel = strings.localizedCondition(current.optInt("weather_code").toWeatherCondition()),
            condition = current.optInt("weather_code").toWeatherCondition(),
            temperatureC = current.optRoundedInt("temperature_2m"),
        )
    }

    override suspend fun weatherFor(city: CityOption, language: AppLanguage): WeatherOverview = withContext(Dispatchers.IO) {
        val forecastUrl = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=").append(city.latitude)
            append("&longitude=").append(city.longitude)
            append("&timezone=auto")
            append("&forecast_days=7")
            append("&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m,surface_pressure,visibility,uv_index")
            append("&hourly=temperature_2m,precipitation_probability,weather_code,wind_speed_10m")
            append("&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset,daylight_duration")
            append("&wind_speed_unit=kmh")
        }

        val forecast = getJson(forecastUrl)
        val strings = AppStrings(language)
        val airQuality = fetchAirQuality(city, strings)
        forecast.toWeatherOverview(city, airQuality, strings)
    }

    private fun fetchAirQuality(city: CityOption, strings: AppStrings): AirQuality {
        return runCatching {
            val url = buildString {
                append("https://air-quality-api.open-meteo.com/v1/air-quality?")
                append("latitude=").append(city.latitude)
                append("&longitude=").append(city.longitude)
                append("&timezone=auto")
                append("&current=us_aqi,ozone")
            }

            val body = getJson(url)
            val current = body.optJSONObject("current")
                ?: throw IOException("Lipseste blocul de calitate a aerului.")

            val aqi = current.optRoundedInt("us_aqi")
            val ozone = current.optFiniteDouble("ozone")?.roundToInt() ?: 0
            val category = strings.aqiCategory(aqi)
            AirQuality(
                aqi = aqi,
                category = category,
                primaryPollutant = if (ozone > 0) "O3" else "AQI",
                description = strings.aqiDescription(category),
            )
        }.getOrElse {
            val unknown = strings.aqiCategory(0)
            AirQuality(
                aqi = 0,
                category = unknown,
                primaryPollutant = "AQI",
                description = strings.aqiDescription(unknown),
            )
        }
    }

    private fun getJson(url: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Senin-Android/0.2")
        }

        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw IOException("Serviciul meteo a raspuns cu o eroare temporara.")
            }
            JSONObject(text)
        } catch (error: SocketTimeoutException) {
            throw IOException("Serviciul meteo raspunde prea greu.", error)
        } catch (error: UnknownHostException) {
            throw IOException("Nu am putut contacta serviciul meteo.", error)
        } catch (error: ConnectException) {
            throw IOException("Conexiunea catre serviciul meteo a esuat.", error)
        } catch (error: JSONException) {
            throw IOException("Raspunsul primit de la serviciul meteo este invalid.", error)
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toCityOptionOrNull(): CityOption? {
        val lat = optFiniteDouble("latitude") ?: return null
        val lon = optFiniteDouble("longitude") ?: return null
        val region = optString("admin1").ifBlank {
            optString("country").ifBlank { "Necunoscut" }
        }
        val country = optString("country").ifBlank { "Necunoscut" }
        val name = optString("name").ifBlank { "Oras necunoscut" }
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

    private fun JSONObject.toWeatherOverview(
        city: CityOption,
        airQuality: AirQuality,
        strings: AppStrings,
    ): WeatherOverview {
        val current = optJSONObject("current") ?: throw IOException("Lipseste blocul de vreme curenta.")
        val hourly = optJSONObject("hourly") ?: throw IOException("Lipseste blocul orar.")
        val daily = optJSONObject("daily") ?: throw IOException("Lipseste blocul zilnic.")

        val currentCondition = current.optInt("weather_code").toWeatherCondition()
        val dailyForecast = daily.toDailyForecast(strings)
        val currentTime = current.optString("time")
        val hourlyForecast = hourly.toHourlyForecast(currentTime, strings)
        val sunSchedule = daily.toSunSchedule()
        val headline = strings.weatherSummary(
            condition = currentCondition,
            cityName = city.name,
            high = dailyForecast.firstOrNull()?.highC,
            low = dailyForecast.firstOrNull()?.lowC,
            precipitation = dailyForecast.firstOrNull()?.precipitationChance,
        )

        return WeatherOverview(
            current = CurrentWeather(
                temperatureC = current.optRoundedInt("temperature_2m"),
                feelsLikeC = current.optRoundedInt("apparent_temperature"),
                condition = currentCondition,
                summary = headline,
                precipitationChance = hourlyForecast.firstOrNull()?.precipitationChance
                    ?: dailyForecast.firstOrNull()?.precipitationChance
                    ?: 0,
                humidity = current.optRoundedInt("relative_humidity_2m"),
                windKph = current.optRoundedInt("wind_speed_10m"),
                uvIndex = current.optRoundedInt("uv_index"),
            ),
            hourly = hourlyForecast,
            daily = dailyForecast,
            details = WeatherDetails(
                airQuality = airQuality,
                visibilityKm = current.optRoundedInt("visibility") / 1000,
                pressureHpa = current.optRoundedInt("surface_pressure"),
                humidity = current.optRoundedInt("relative_humidity_2m"),
                windKph = current.optRoundedInt("wind_speed_10m"),
                windDirectionLabel = current.optRoundedInt("wind_direction_10m").toCompassLabel(),
                uvIndex = current.optRoundedInt("uv_index"),
                precipitationChance = hourlyForecast.firstOrNull()?.precipitationChance
                    ?: dailyForecast.firstOrNull()?.precipitationChance
                    ?: 0,
                sunSchedule = sunSchedule,
            ),
            updatedAtLabel = strings.updatedAt(
                currentTime.toHourLabel(),
                optString("timezone_abbreviation").ifBlank { "" },
            ),
            sourceLabel = strings.sourceLiveOpenMeteo,
        )
    }

    private fun JSONObject.toHourlyForecast(currentTime: String, strings: AppStrings): List<HourlyForecast> {
        val times = optJSONArray("time") ?: JSONArray()
        val temps = optJSONArray("temperature_2m") ?: JSONArray()
        val rain = optJSONArray("precipitation_probability") ?: JSONArray()
        val wind = optJSONArray("wind_speed_10m") ?: JSONArray()
        val codes = optJSONArray("weather_code") ?: JSONArray()

        val startIndex = startIndexForCurrentTime(times, currentTime)
        val endExclusive = minOf(times.length(), startIndex + 24)

        return buildList {
            for (index in startIndex until endExclusive) {
                add(
                    HourlyForecast(
                        timeLabel = if (index == startIndex) strings.now else times.optString(index).toHourLabel(),
                        temperatureC = temps.optRoundedInt(index),
                        precipitationChance = rain.optRoundedInt(index),
                        windKph = wind.optRoundedInt(index),
                        condition = codes.optInt(index).toWeatherCondition(),
                    ),
                )
            }
        }
    }

    private fun JSONObject.toDailyForecast(strings: AppStrings): List<DailyForecast> {
        val times = optJSONArray("time") ?: JSONArray()
        val highs = optJSONArray("temperature_2m_max") ?: JSONArray()
        val lows = optJSONArray("temperature_2m_min") ?: JSONArray()
        val rain = optJSONArray("precipitation_probability_max") ?: JSONArray()
        val codes = optJSONArray("weather_code") ?: JSONArray()

        return buildList {
            for (index in 0 until minOf(times.length(), 7)) {
                val date = runCatching { LocalDate.parse(times.optString(index)) }.getOrNull() ?: continue
                add(
                    DailyForecast(
                        dayLabel = strings.localizedDayLabel(index, date.dayOfWeek),
                        highC = highs.optRoundedInt(index),
                        lowC = lows.optRoundedInt(index),
                        precipitationChance = rain.optRoundedInt(index),
                        condition = codes.optInt(index).toWeatherCondition(),
                    ),
                )
            }
        }
    }

    private fun JSONObject.toSunSchedule(): SunSchedule {
        val sunrise = optJSONArray("sunrise")?.optString(0).orEmpty().toHourLabel()
        val sunset = optJSONArray("sunset")?.optString(0).orEmpty().toHourLabel()
        val daylightSeconds = optJSONArray("daylight_duration")?.optDouble(0, Double.NaN) ?: Double.NaN

        return SunSchedule(
            sunriseLabel = sunrise,
            sunsetLabel = sunset,
            daylightDurationLabel = daylightSeconds.toDaylightLabel(),
        )
    }

    private fun startIndexForCurrentTime(times: JSONArray, target: String): Int {
        val targetTime = runCatching { LocalDateTime.parse(target) }.getOrNull() ?: return 0
        var candidate = 0

        for (index in 0 until times.length()) {
            val time = runCatching { LocalDateTime.parse(times.optString(index)) }.getOrNull() ?: continue
            if (time.isAfter(targetTime)) break
            candidate = index
        }

        return candidate
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

    private fun String.toHourLabel(): String {
        if (isBlank()) return "--:--"
        return runCatching {
            LocalDateTime.parse(this).format(DateTimeFormatter.ofPattern("HH:mm"))
        }.getOrElse { "--:--" }
    }

    private fun Double.toDaylightLabel(): String {
        if (!isFinite()) return "--"
        val duration = Duration.ofSeconds(roundToInt().toLong())
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()
        return "${hours}h ${minutes}m"
    }

    private fun Int.toCompassLabel(): String {
        val normalized = ((this % 360) + 360) % 360
        return when (normalized) {
            in 23..67 -> "NE"
            in 68..112 -> "E"
            in 113..157 -> "SE"
            in 158..202 -> "S"
            in 203..247 -> "SV"
            in 248..292 -> "V"
            in 293..337 -> "NV"
            else -> "N"
        }
    }

    private fun JSONObject.optFiniteDouble(key: String): Double? {
        val value = optDouble(key, Double.NaN)
        return value.takeIf { it.isFinite() }
    }

    private fun JSONObject.optRoundedInt(key: String, fallback: Int = 0): Int {
        return optFiniteDouble(key)?.roundToInt() ?: fallback
    }

    private fun JSONArray.optRoundedInt(index: Int, fallback: Int = 0): Int {
        val value = optDouble(index, Double.NaN)
        return if (value.isFinite()) value.roundToInt() else fallback
    }
}
