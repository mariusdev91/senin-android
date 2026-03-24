package com.mariusdev91.senin.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.mariusdev91.senin.model.WeatherCondition
import java.time.DayOfWeek

@Immutable
data class AppStrings(
    val language: AppLanguage,
) {
    val appName: String = "Senin"

    val tabForecast: String
        get() = when (language) {
            AppLanguage.Romanian -> "Prognoza"
            AppLanguage.English -> "Forecast"
            AppLanguage.Hungarian -> "Elorejelzes"
        }

    val tabLocations: String
        get() = when (language) {
            AppLanguage.Romanian -> "Locatii"
            AppLanguage.English -> "Locations"
            AppLanguage.Hungarian -> "Helyek"
        }

    val tabDetails: String
        get() = when (language) {
            AppLanguage.Romanian -> "Detalii"
            AppLanguage.English -> "Details"
            AppLanguage.Hungarian -> "Reszletek"
        }

    val languagesMenu: String
        get() = when (language) {
            AppLanguage.Romanian -> "Limbi"
            AppLanguage.English -> "Language"
            AppLanguage.Hungarian -> "Nyelv"
        }

    val languageDialogTitle: String
        get() = when (language) {
            AppLanguage.Romanian -> "Alege limba"
            AppLanguage.English -> "Choose language"
            AppLanguage.Hungarian -> "Valassz nyelvet"
        }

    val searchDialogTitle: String
        get() = when (language) {
            AppLanguage.Romanian -> "Schimba orasul"
            AppLanguage.English -> "Change city"
            AppLanguage.Hungarian -> "Varos valtas"
        }

    fun searchResultsCount(count: Int): String = when (language) {
        AppLanguage.Romanian -> "$count rezultate"
        AppLanguage.English -> "$count results"
        AppLanguage.Hungarian -> "$count talalat"
    }

    val searchCityPlaceholder: String
        get() = when (language) {
            AppLanguage.Romanian -> "Cauta un oras"
            AppLanguage.English -> "Search for a city"
            AppLanguage.Hungarian -> "Varos keresese"
        }

    val searchDialogHint: String
        get() = when (language) {
            AppLanguage.Romanian -> "Cauta orase din Romania sau din lume"
            AppLanguage.English -> "Search cities in Romania or worldwide"
            AppLanguage.Hungarian -> "Keress varosokat Romaniabol vagy a vilagbol"
        }

    val clearSearch: String
        get() = when (language) {
            AppLanguage.Romanian -> "Curata cautarea"
            AppLanguage.English -> "Clear search"
            AppLanguage.Hungarian -> "Kereses torlese"
        }

    val savedLocations: String
        get() = when (language) {
            AppLanguage.Romanian -> "Locatii salvate"
            AppLanguage.English -> "Saved Locations"
            AppLanguage.Hungarian -> "Mentett helyek"
        }

    val currentLocationLabel: String
        get() = when (language) {
            AppLanguage.Romanian -> "Locatia curenta"
            AppLanguage.English -> "Current location"
            AppLanguage.Hungarian -> "Jelenlegi hely"
        }

    val gpsLocationLabel: String
        get() = when (language) {
            AppLanguage.Romanian -> "GPS"
            AppLanguage.English -> "GPS"
            AppLanguage.Hungarian -> "GPS"
        }

    fun savedCitiesCount(count: Int): String = when (language) {
        AppLanguage.Romanian -> "$count orase"
        AppLanguage.English -> "$count cities"
        AppLanguage.Hungarian -> "$count varos"
    }

    val noSavedLocationsHint: String
        get() = when (language) {
            AppLanguage.Romanian -> "Adauga mai multe orase pentru a urmari vremea din alte locuri."
            AppLanguage.English -> "Add more cities to track weather in more places."
            AppLanguage.Hungarian -> "Adj hozza tobb varost, hogy mashol is kovethesd az idojarast."
        }

    val todaysOutlook: String
        get() = when (language) {
            AppLanguage.Romanian -> "TODAY'S OUTLOOK"
            AppLanguage.English -> "TODAY'S OUTLOOK"
            AppLanguage.Hungarian -> "MAI KILATASOK"
        }

    val now: String
        get() = when (language) {
            AppLanguage.Romanian -> "Acum"
            AppLanguage.English -> "Now"
            AppLanguage.Hungarian -> "Most"
        }

    val next24Hours: String
        get() = when (language) {
            AppLanguage.Romanian -> "Urmatoarele 24 de ore"
            AppLanguage.English -> "Next 24 Hours"
            AppLanguage.Hungarian -> "Kovetkezo 24 ora"
        }

    val today: String
        get() = when (language) {
            AppLanguage.Romanian -> "Astazi"
            AppLanguage.English -> "Today"
            AppLanguage.Hungarian -> "Ma"
        }

    val sevenDayForecast: String
        get() = when (language) {
            AppLanguage.Romanian -> "Prognoza pe 7 zile"
            AppLanguage.English -> "7-Day Forecast"
            AppLanguage.Hungarian -> "7 napos elorejelzes"
        }

    val detailedMetrics: String
        get() = when (language) {
            AppLanguage.Romanian -> "DETALII METEO"
            AppLanguage.English -> "DETAILED METRICS"
            AppLanguage.Hungarian -> "RESZLETES ADATOK"
        }

    val currentAtmosphere: String
        get() = when (language) {
            AppLanguage.Romanian -> "Atmosfera momentului"
            AppLanguage.English -> "Current atmosphere"
            AppLanguage.Hungarian -> "Aktualis hangulat"
        }

    fun lastUpdated(label: String?): String = label ?: when (language) {
        AppLanguage.Romanian -> "Ultima actualizare indisponibila"
        AppLanguage.English -> "Last update unavailable"
        AppLanguage.Hungarian -> "Az utolso frissites nem erheto el"
    }

    val airQuality: String
        get() = when (language) {
            AppLanguage.Romanian -> "CALITATEA AERULUI"
            AppLanguage.English -> "AIR QUALITY"
            AppLanguage.Hungarian -> "LEVEGO MINOSEG"
        }

    fun primaryPollutant(pollutant: String): String = when (language) {
        AppLanguage.Romanian -> "Principal: $pollutant"
        AppLanguage.English -> "Primary: $pollutant"
        AppLanguage.Hungarian -> "Fo: $pollutant"
    }

    val wind: String
        get() = when (language) {
            AppLanguage.Romanian -> "VANT"
            AppLanguage.English -> "WIND"
            AppLanguage.Hungarian -> "SZEL"
        }

    val humidity: String
        get() = when (language) {
            AppLanguage.Romanian -> "UMIDITATE"
            AppLanguage.English -> "HUMIDITY"
            AppLanguage.Hungarian -> "PARATARTALOM"
        }

    val humidityShort: String
        get() = when (language) {
            AppLanguage.Romanian -> "UMID."
            AppLanguage.English -> "HUM."
            AppLanguage.Hungarian -> "PARA"
        }

    val visibility: String
        get() = when (language) {
            AppLanguage.Romanian -> "VIZIBILITATE"
            AppLanguage.English -> "VISIBILITY"
            AppLanguage.Hungarian -> "LATASI TAV"
        }

    val visibilityShort: String
        get() = when (language) {
            AppLanguage.Romanian -> "VIZ."
            AppLanguage.English -> "VIS."
            AppLanguage.Hungarian -> "LAT."
        }

    val pressure: String
        get() = when (language) {
            AppLanguage.Romanian -> "PRESIUNE"
            AppLanguage.English -> "PRESSURE"
            AppLanguage.Hungarian -> "LEGNYOMAS"
        }

    val rainfall: String
        get() = when (language) {
            AppLanguage.Romanian -> "PLOAIE"
            AppLanguage.English -> "RAINFALL"
            AppLanguage.Hungarian -> "ESO"
        }

    val uvIndex: String
        get() = when (language) {
            AppLanguage.Romanian -> "INDICE UV"
            AppLanguage.English -> "UV INDEX"
            AppLanguage.Hungarian -> "UV INDEX"
        }

    val sunrise: String
        get() = when (language) {
            AppLanguage.Romanian -> "RASARIT"
            AppLanguage.English -> "SUNRISE"
            AppLanguage.Hungarian -> "NAPKELTE"
        }

    val sunset: String
        get() = when (language) {
            AppLanguage.Romanian -> "APUS"
            AppLanguage.English -> "SUNSET"
            AppLanguage.Hungarian -> "NAPNYUGTA"
        }

    val daylight: String
        get() = when (language) {
            AppLanguage.Romanian -> "LUMINA ZILEI"
            AppLanguage.English -> "DAYLIGHT"
            AppLanguage.Hungarian -> "NAPPALI FENY"
        }

    val sourceLiveOpenMeteo: String
        get() = when (language) {
            AppLanguage.Romanian -> "Date live via Open-Meteo"
            AppLanguage.English -> "Live data via Open-Meteo"
            AppLanguage.Hungarian -> "Elo adatok az Open-Meteo szolgaltatastol"
        }

    val peakUv: String
        get() = when (language) {
            AppLanguage.Romanian -> "UV MAXIM"
            AppLanguage.English -> "PEAK UV"
            AppLanguage.Hungarian -> "CSUCS UV"
        }

    val dailyHigh: String
        get() = when (language) {
            AppLanguage.Romanian -> "Maxima zilei"
            AppLanguage.English -> "Daily High"
            AppLanguage.Hungarian -> "Napi maximum"
        }

    val addToFavorites: String
        get() = when (language) {
            AppLanguage.Romanian -> "Adauga la favorite"
            AppLanguage.English -> "Add to favorites"
            AppLanguage.Hungarian -> "Hozzaadas a kedvencekhez"
        }

    val removeFromFavorites: String
        get() = when (language) {
            AppLanguage.Romanian -> "Scoate din favorite"
            AppLanguage.English -> "Remove from favorites"
            AppLanguage.Hungarian -> "Eltavolitas a kedvencekbol"
        }

    val removeFromSavedLocations: String
        get() = when (language) {
            AppLanguage.Romanian -> "Scoate din locatii salvate"
            AppLanguage.English -> "Remove from saved locations"
            AppLanguage.Hungarian -> "Eltavolitas a mentett helyek kozul"
        }

    val retry: String
        get() = when (language) {
            AppLanguage.Romanian -> "Incearca din nou"
            AppLanguage.English -> "Try again"
            AppLanguage.Hungarian -> "Probald ujra"
        }

    val unavailableWeather: String
        get() = when (language) {
            AppLanguage.Romanian -> "Momentan nu pot afisa vremea"
            AppLanguage.English -> "I cannot show the weather right now"
            AppLanguage.Hungarian -> "Az idojaras most nem erheto el"
        }

    val loading: String
        get() = when (language) {
            AppLanguage.Romanian -> "Se incarca"
            AppLanguage.English -> "Loading"
            AppLanguage.Hungarian -> "Betoltes"
        }

    val clearView: String
        get() = when (language) {
            AppLanguage.Romanian -> "Vizibilitate excelenta"
            AppLanguage.English -> "Clear view"
            AppLanguage.Hungarian -> "Tiszta kilatas"
        }

    val goodVisibility: String
        get() = when (language) {
            AppLanguage.Romanian -> "Vizibilitate buna"
            AppLanguage.English -> "Good visibility"
            AppLanguage.Hungarian -> "Jo latotav"
        }

    val moderateVisibility: String
        get() = when (language) {
            AppLanguage.Romanian -> "Vizibilitate moderata"
            AppLanguage.English -> "Moderate visibility"
            AppLanguage.Hungarian -> "Kozepes latotav"
        }

    val lowVisibility: String
        get() = when (language) {
            AppLanguage.Romanian -> "Vizibilitate redusa"
            AppLanguage.English -> "Low visibility"
            AppLanguage.Hungarian -> "Alacsony latotav"
        }

    val lowPressure: String
        get() = when (language) {
            AppLanguage.Romanian -> "Scazuta"
            AppLanguage.English -> "Low"
            AppLanguage.Hungarian -> "Alacsony"
        }

    val highPressure: String
        get() = when (language) {
            AppLanguage.Romanian -> "Ridicata"
            AppLanguage.English -> "High"
            AppLanguage.Hungarian -> "Magas"
        }

    val steadyPressure: String
        get() = when (language) {
            AppLanguage.Romanian -> "Stabila"
            AppLanguage.English -> "Steady"
            AppLanguage.Hungarian -> "Stabil"
        }

    val minProtection: String
        get() = when (language) {
            AppLanguage.Romanian -> "Protectie minima necesara"
            AppLanguage.English -> "Minimal protection needed"
            AppLanguage.Hungarian -> "Minimalis vedelem eleg"
        }

    val spf30Recommended: String
        get() = when (language) {
            AppLanguage.Romanian -> "SPF 30 recomandat"
            AppLanguage.English -> "SPF 30 recommended"
            AppLanguage.Hungarian -> "SPF 30 ajanlott"
        }

    val avoidLongExposure: String
        get() = when (language) {
            AppLanguage.Romanian -> "Evita expunerea lunga"
            AppLanguage.English -> "Avoid long exposure"
            AppLanguage.Hungarian -> "Keruld a hosszu kitetseget"
        }

    val stayInShadeAtNoon: String
        get() = when (language) {
            AppLanguage.Romanian -> "Stai la umbra la pranz"
            AppLanguage.English -> "Stay in the shade at noon"
            AppLanguage.Hungarian -> "Delben maradj arnyekban"
        }

    fun localizedCondition(condition: WeatherCondition): String = when (language) {
        AppLanguage.Romanian -> when (condition) {
            WeatherCondition.Clear -> "Senin"
            WeatherCondition.PartlyCloudy -> "Cer variabil"
            WeatherCondition.Cloudy -> "Innorat"
            WeatherCondition.Rain -> "Ploaie"
            WeatherCondition.Thunderstorm -> "Furtuna"
            WeatherCondition.Snow -> "Ninsoare"
            WeatherCondition.Mist -> "Ceata"
        }
        AppLanguage.English -> when (condition) {
            WeatherCondition.Clear -> "Clear"
            WeatherCondition.PartlyCloudy -> "Partly cloudy"
            WeatherCondition.Cloudy -> "Cloudy"
            WeatherCondition.Rain -> "Rain"
            WeatherCondition.Thunderstorm -> "Thunderstorm"
            WeatherCondition.Snow -> "Snow"
            WeatherCondition.Mist -> "Mist"
        }
        AppLanguage.Hungarian -> when (condition) {
            WeatherCondition.Clear -> "Derult"
            WeatherCondition.PartlyCloudy -> "Valtozo felhozet"
            WeatherCondition.Cloudy -> "Felhos"
            WeatherCondition.Rain -> "Eso"
            WeatherCondition.Thunderstorm -> "Vihar"
            WeatherCondition.Snow -> "Havazas"
            WeatherCondition.Mist -> "Kod"
        }
    }

    fun localizedDayLabel(index: Int, dayOfWeek: DayOfWeek): String = when (language) {
        AppLanguage.Romanian -> when (index) {
            0 -> "Azi"
            1 -> "Maine"
            else -> when (dayOfWeek) {
                DayOfWeek.MONDAY -> "Luni"
                DayOfWeek.TUESDAY -> "Marti"
                DayOfWeek.WEDNESDAY -> "Miercuri"
                DayOfWeek.THURSDAY -> "Joi"
                DayOfWeek.FRIDAY -> "Vineri"
                DayOfWeek.SATURDAY -> "Sambata"
                DayOfWeek.SUNDAY -> "Duminica"
            }
        }
        AppLanguage.English -> when (index) {
            0 -> "Today"
            1 -> "Tomorrow"
            else -> when (dayOfWeek) {
                DayOfWeek.MONDAY -> "Monday"
                DayOfWeek.TUESDAY -> "Tuesday"
                DayOfWeek.WEDNESDAY -> "Wednesday"
                DayOfWeek.THURSDAY -> "Thursday"
                DayOfWeek.FRIDAY -> "Friday"
                DayOfWeek.SATURDAY -> "Saturday"
                DayOfWeek.SUNDAY -> "Sunday"
            }
        }
        AppLanguage.Hungarian -> when (index) {
            0 -> "Ma"
            1 -> "Holnap"
            else -> when (dayOfWeek) {
                DayOfWeek.MONDAY -> "Hetfo"
                DayOfWeek.TUESDAY -> "Kedd"
                DayOfWeek.WEDNESDAY -> "Szerda"
                DayOfWeek.THURSDAY -> "Csutortok"
                DayOfWeek.FRIDAY -> "Pentek"
                DayOfWeek.SATURDAY -> "Szombat"
                DayOfWeek.SUNDAY -> "Vasarnap"
            }
        }
    }

    fun weatherSummary(condition: WeatherCondition, cityName: String, high: Int?, low: Int?, precipitation: Int?): String {
        val prefix = when (language) {
            AppLanguage.Romanian -> when (condition) {
                WeatherCondition.Clear -> "Cer senin"
                WeatherCondition.PartlyCloudy -> "Cer variabil"
                WeatherCondition.Cloudy -> "Innorat"
                WeatherCondition.Rain -> "Ploaie sau averse"
                WeatherCondition.Thunderstorm -> "Instabilitate si furtuni"
                WeatherCondition.Snow -> "Ninsori usoare"
                WeatherCondition.Mist -> "Ceata si vizibilitate redusa"
            }
            AppLanguage.English -> when (condition) {
                WeatherCondition.Clear -> "Clear skies"
                WeatherCondition.PartlyCloudy -> "Partly cloudy"
                WeatherCondition.Cloudy -> "Cloudy"
                WeatherCondition.Rain -> "Rain showers"
                WeatherCondition.Thunderstorm -> "Stormy conditions"
                WeatherCondition.Snow -> "Light snow"
                WeatherCondition.Mist -> "Mist and low visibility"
            }
            AppLanguage.Hungarian -> when (condition) {
                WeatherCondition.Clear -> "Derult ido"
                WeatherCondition.PartlyCloudy -> "Valtozo felhozet"
                WeatherCondition.Cloudy -> "Felhos ido"
                WeatherCondition.Rain -> "Eso vagy zaporok"
                WeatherCondition.Thunderstorm -> "Zivataros ido"
                WeatherCondition.Snow -> "Gyenge havazas"
                WeatherCondition.Mist -> "Kod es gyenge latotav"
            }
        }

        return if (high == null || low == null || precipitation == null) {
            when (language) {
                AppLanguage.Romanian -> "$prefix acum in $cityName."
                AppLanguage.English -> "$prefix now in $cityName."
                AppLanguage.Hungarian -> "$prefix most itt: $cityName."
            }
        } else {
            when (language) {
                AppLanguage.Romanian -> "$prefix azi in $cityName. Max ${high}\u00B0, min ${low}\u00B0, ploaie $precipitation%."
                AppLanguage.English -> "$prefix today in $cityName. High ${high}\u00B0, low ${low}\u00B0, rain $precipitation%."
                AppLanguage.Hungarian -> "$prefix ma itt: $cityName. Max ${high}\u00B0, min ${low}\u00B0, eso $precipitation%."
            }
        }
    }

    fun updatedAt(hourLabel: String, timezoneAbbreviation: String): String = when (language) {
        AppLanguage.Romanian -> "Actualizat $hourLabel $timezoneAbbreviation".trim()
        AppLanguage.English -> "Updated $hourLabel $timezoneAbbreviation".trim()
        AppLanguage.Hungarian -> "Frissitve $hourLabel $timezoneAbbreviation".trim()
    }

    fun searchFavoritesHint(): String = when (language) {
        AppLanguage.Romanian -> "Adauga orase la favorite pentru acces rapid."
        AppLanguage.English -> "Add cities to favorites for quick access."
        AppLanguage.Hungarian -> "Adj varosokat a kedvencekhez a gyors elereshez."
    }

    fun searchMinCharsHint(): String = when (language) {
        AppLanguage.Romanian -> "Scrie macar 2 litere pentru cautare live."
        AppLanguage.English -> "Type at least 2 letters for live search."
        AppLanguage.Hungarian -> "Irj be legalabb 2 betut az elo keresesehez."
    }

    fun searchNoResults(query: String): String = when (language) {
        AppLanguage.Romanian -> "Nu am gasit niciun oras pentru \"$query\"."
        AppLanguage.English -> "No city found for \"$query\"."
        AppLanguage.Hungarian -> "Nem talaltam varost ehhez: \"$query\"."
    }

    fun searchLocalFavoritesOnly(): String = when (language) {
        AppLanguage.Romanian -> "Momentan iti arat rezultatele favorite salvate local."
        AppLanguage.English -> "Showing locally saved favorites for now."
        AppLanguage.Hungarian -> "Most a helyben mentett kedvenceket mutatom."
    }

    fun addedToFavorites(cityName: String): String = when (language) {
        AppLanguage.Romanian -> "$cityName a fost adaugat la favorite."
        AppLanguage.English -> "$cityName was added to favorites."
        AppLanguage.Hungarian -> "$cityName hozzaadva a kedvencekhez."
    }

    fun removedFromFavorites(cityName: String): String = when (language) {
        AppLanguage.Romanian -> "$cityName a fost scos din favorite."
        AppLanguage.English -> "$cityName was removed from favorites."
        AppLanguage.Hungarian -> "$cityName eltavolitva a kedvencekbol."
    }

    fun timeoutError(): String = when (language) {
        AppLanguage.Romanian -> "Actualizarea dureaza prea mult. Incearca din nou."
        AppLanguage.English -> "The update is taking too long. Try again."
        AppLanguage.Hungarian -> "A frissites tul sokaig tart. Probald ujra."
    }

    fun networkError(): String = when (language) {
        AppLanguage.Romanian -> "Nu am putut contacta serviciul meteo. Verifica internetul."
        AppLanguage.English -> "Could not reach the weather service. Check your internet."
        AppLanguage.Hungarian -> "Nem sikerult elerni az idojaras szolgaltatast. Ellenorizd az internetet."
    }

    fun genericLoadError(): String = when (language) {
        AppLanguage.Romanian -> "Momentan nu pot incarca vremea."
        AppLanguage.English -> "I cannot load the weather right now."
        AppLanguage.Hungarian -> "Most nem tudom betolteni az idojarast."
    }

    fun unexpectedError(): String = when (language) {
        AppLanguage.Romanian -> "A aparut o eroare neasteptata. Incearca din nou."
        AppLanguage.English -> "An unexpected error occurred. Try again."
        AppLanguage.Hungarian -> "Varatlan hiba tortent. Probald ujra."
    }

    fun aqiCategory(aqi: Int): String = when (language) {
        AppLanguage.Romanian -> when {
            aqi <= 0 -> "Necunoscut"
            aqi <= 50 -> "Bun"
            aqi <= 100 -> "Moderat"
            aqi <= 150 -> "Sensibil"
            aqi <= 200 -> "Slab"
            aqi <= 300 -> "Foarte slab"
            else -> "Periculos"
        }
        AppLanguage.English -> when {
            aqi <= 0 -> "Unknown"
            aqi <= 50 -> "Good"
            aqi <= 100 -> "Moderate"
            aqi <= 150 -> "Sensitive"
            aqi <= 200 -> "Poor"
            aqi <= 300 -> "Very poor"
            else -> "Hazardous"
        }
        AppLanguage.Hungarian -> when {
            aqi <= 0 -> "Ismeretlen"
            aqi <= 50 -> "Jo"
            aqi <= 100 -> "Kozepes"
            aqi <= 150 -> "Erzekeny"
            aqi <= 200 -> "Gyenge"
            aqi <= 300 -> "Nagyon gyenge"
            else -> "Veszelyes"
        }
    }

    fun aqiDescription(category: String): String = when (language) {
        AppLanguage.Romanian -> when (category) {
            "Bun" -> "Calitatea aerului este buna si potrivita pentru activitati afara."
            "Moderat" -> "Aerul este acceptabil, dar persoanele sensibile ar trebui sa fie atente."
            "Sensibil" -> "Persoanele sensibile pot simti disconfort in exterior."
            "Slab" -> "E mai bine sa limitezi activitatile lungi in aer liber."
            "Foarte slab" -> "Aerul este poluat. Activitatile afara ar trebui scurtate."
            "Periculos" -> "Calitatea aerului este foarte slaba. Evita expunerea prelungita."
            else -> "Datele pentru calitatea aerului nu sunt disponibile momentan."
        }
        AppLanguage.English -> when (category) {
            "Good" -> "Air quality is good and suitable for outdoor activities."
            "Moderate" -> "Air quality is acceptable, but sensitive people should be careful."
            "Sensitive" -> "Sensitive people may feel discomfort outdoors."
            "Poor" -> "It is better to limit long outdoor activities."
            "Very poor" -> "The air is polluted. Outdoor activity should be shortened."
            "Hazardous" -> "Air quality is very poor. Avoid prolonged exposure."
            else -> "Air quality data is unavailable right now."
        }
        AppLanguage.Hungarian -> when (category) {
            "Jo" -> "A levego minosege jo, a szabadteri tevekenysegekhez megfelelo."
            "Kozepes" -> "A levego elfogadhato, de az erzekeny emberek legyenek ovatosak."
            "Erzekeny" -> "Az erzekeny emberek kellemetlenseget erezhetnek a szabadban."
            "Gyenge" -> "Erdemes korlatozni a hosszu szabadtéri tevekenysegeket."
            "Nagyon gyenge" -> "A levego szennyezett. A szabadtéri ido roviditese ajanlott."
            "Veszelyes" -> "A levego minosege nagyon gyenge. Keruld a hosszu kitetseget."
            else -> "A levego minosegere vonatkozo adatok most nem erhetoek el."
        }
    }

    fun rainfallCaption(chance: Int): String = when (language) {
        AppLanguage.Romanian -> when {
            chance >= 70 -> "Averse probabile"
            chance >= 30 -> "Posibile ploi scurte"
            chance > 0 -> "Ploaie usoara posibila"
            else -> "Nu se asteapta ploaie"
        }
        AppLanguage.English -> when {
            chance >= 70 -> "Showers likely"
            chance >= 30 -> "Brief rain possible"
            chance > 0 -> "Light rain possible"
            else -> "No rain expected"
        }
        AppLanguage.Hungarian -> when {
            chance >= 70 -> "Zapor valoszinu"
            chance >= 30 -> "Rovid eso lehetseges"
            chance > 0 -> "Gyenge eso lehetseges"
            else -> "Eso nem varhato"
        }
    }

    fun visibilityCaption(visibilityKm: Int): String = when {
        visibilityKm >= 10 -> clearView
        visibilityKm >= 6 -> goodVisibility
        visibilityKm >= 3 -> moderateVisibility
        else -> lowVisibility
    }

    fun pressureCaption(pressureHpa: Int): String = when {
        pressureHpa < 1005 -> lowPressure
        pressureHpa > 1020 -> highPressure
        else -> steadyPressure
    }

    fun dewPointLabel(value: Int): String = when (language) {
        AppLanguage.Romanian -> "Punct de roua: ${value}\u00B0"
        AppLanguage.English -> "Dew point: ${value}\u00B0"
        AppLanguage.Hungarian -> "Harmatpont: ${value}\u00B0"
    }

    fun uvLabel(uvIndex: Int): String = when (language) {
        AppLanguage.Romanian -> when {
            uvIndex <= 2 -> "Scazut"
            uvIndex <= 5 -> "Moderat"
            uvIndex <= 7 -> "Ridicat"
            uvIndex <= 10 -> "Foarte ridicat"
            else -> "Extrem"
        }
        AppLanguage.English -> when {
            uvIndex <= 2 -> "Low"
            uvIndex <= 5 -> "Moderate"
            uvIndex <= 7 -> "High"
            uvIndex <= 10 -> "Very High"
            else -> "Extreme"
        }
        AppLanguage.Hungarian -> when {
            uvIndex <= 2 -> "Alacsony"
            uvIndex <= 5 -> "Kozepes"
            uvIndex <= 7 -> "Magas"
            uvIndex <= 10 -> "Nagyon magas"
            else -> "Extrem"
        }
    }

    fun uvRecommendation(uvIndex: Int): String = when {
        uvIndex <= 2 -> minProtection
        uvIndex <= 5 -> spf30Recommended
        uvIndex <= 7 -> avoidLongExposure
        else -> stayInShadeAtNoon
    }
}

val LocalAppStrings = staticCompositionLocalOf { AppStrings(AppLanguage.Romanian) }

@Composable
@ReadOnlyComposable
fun currentStrings(): AppStrings = LocalAppStrings.current
