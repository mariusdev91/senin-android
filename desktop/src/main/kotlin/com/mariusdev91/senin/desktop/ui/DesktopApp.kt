package com.mariusdev91.senin.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.ModeNight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import com.mariusdev91.senin.desktop.data.DesktopPreferencesStore
import com.mariusdev91.senin.desktop.data.OpenMeteoWeatherRepository
import com.mariusdev91.senin.desktop.model.CityOption
import com.mariusdev91.senin.desktop.model.DailyForecast
import com.mariusdev91.senin.desktop.model.HourlyForecast
import com.mariusdev91.senin.desktop.model.WeatherCondition
import com.mariusdev91.senin.desktop.model.WeatherOverview
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AppLightScheme = lightColorScheme(
    primary = Color(0xFF1D87BE),
    secondary = Color(0xFF89C7E8),
    surface = Color(0xFFF6EDE2),
)

private val SidebarSurface = Color(0xFF14344D).copy(alpha = 0.34f)
private val ContentSurface = Color.White.copy(alpha = 0.10f)
private val HeroSurface = Color(0xFFF5EBDD)
private val HeroPrimary = Color(0xFF173046)
private val HeroSecondary = Color(0xFF5C6F81)
private val HeroAccent = Color(0xFF2C93CB)

data class DesktopUiState(
    val selectedCity: CityOption,
    val pendingCity: CityOption? = null,
    val favoriteCities: List<CityOption>,
    val suggestions: List<CityOption>,
    val weather: WeatherOverview? = null,
    val query: String = "",
    val isLoadingWeather: Boolean = false,
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val searchStatusMessage: String? = null,
    val showSearch: Boolean = false,
)

@Composable
fun DesktopApp() {
    MaterialTheme(colorScheme = AppLightScheme) {
        val repository = remember { OpenMeteoWeatherRepository() }
        val store = remember { DesktopPreferencesStore() }
        val defaultCity = remember { repository.defaultCity() }
        val initialFavorites = remember {
            normalizedFavorites(
                if (store.hasStoredFavorites()) store.loadFavorites() else repository.favoriteCities(),
            )
        }
        val initialSelectedCity = remember { store.loadSelectedCity() ?: defaultCity }

        var uiState by remember {
            mutableStateOf(
                DesktopUiState(
                    selectedCity = initialSelectedCity,
                    favoriteCities = initialFavorites,
                    suggestions = initialFavorites,
                    isLoadingWeather = true,
                ),
            )
        }
        val scope = rememberCoroutineScope()
        var weatherJob by remember { mutableStateOf<Job?>(null) }
        var searchJob by remember { mutableStateOf<Job?>(null) }

        fun refreshWeather(city: CityOption, preserveCurrentWeather: Boolean) {
            weatherJob?.cancel()
            uiState = uiState.copy(
                weather = if (preserveCurrentWeather) uiState.weather else null,
                isLoadingWeather = true,
                errorMessage = null,
                pendingCity = if (city.id == uiState.selectedCity.id) uiState.pendingCity else city,
            )

            weatherJob = scope.launch {
                runSuspendCatching { repository.weatherFor(city) }
                    .onSuccess { weather ->
                        store.saveSelectedCity(city)
                        uiState = uiState.copy(
                            selectedCity = city,
                            pendingCity = null,
                            weather = weather,
                            isLoadingWeather = false,
                            errorMessage = null,
                        )
                    }
                    .onFailure { error ->
                        uiState = uiState.copy(
                            pendingCity = null,
                            isLoadingWeather = false,
                            errorMessage = error.toUserMessage(),
                        )
                    }
            }
        }

        fun onQueryChange(query: String) {
            val normalized = query.trim()
            val favorites = uiState.favoriteCities
            searchJob?.cancel()

            if (normalized.isBlank()) {
                uiState = uiState.copy(
                    query = query,
                    suggestions = favorites,
                    isSearching = false,
                    searchStatusMessage = if (favorites.isEmpty()) {
                        "Adauga orase la favorite pentru acces rapid."
                    } else {
                        null
                    },
                )
                return
            }

            if (normalized.length < 2) {
                val localResults = localCityMatches(normalized, favorites)
                uiState = uiState.copy(
                    query = query,
                    suggestions = localResults,
                    isSearching = false,
                    searchStatusMessage = if (localResults.isEmpty()) {
                        "Scrie macar 2 litere pentru cautare live."
                    } else {
                        null
                    },
                )
                return
            }

            uiState = uiState.copy(
                query = query,
                isSearching = true,
                searchStatusMessage = null,
            )

            searchJob = scope.launch {
                delay(250)
                val results = runSuspendCatching { repository.searchCities(normalized) }
                    .getOrElse { localCityMatches(normalized, favorites) }

                if (uiState.query.trim() != normalized) return@launch

                uiState = uiState.copy(
                    suggestions = results,
                    isSearching = false,
                    searchStatusMessage = when {
                        results.isEmpty() -> "Nu am gasit niciun oras pentru \"$normalized\"."
                        results.size <= favorites.size && results.all { city ->
                            favorites.any { it.id == city.id }
                        } -> "Momentan iti arat rezultatele favorite salvate local."
                        else -> null
                    },
                )
            }
        }

        fun onFavoriteToggle(city: CityOption) {
            val isFavorite = uiState.favoriteCities.any { it.id == city.id }
            val updatedFavorites = if (isFavorite) {
                uiState.favoriteCities.filterNot { it.id == city.id }
            } else {
                normalizedFavorites(listOf(city) + uiState.favoriteCities)
            }

            store.saveFavorites(updatedFavorites)
            uiState = uiState.copy(
                favoriteCities = updatedFavorites,
                suggestions = updatedSuggestions(uiState.query.trim(), updatedFavorites, uiState.suggestions),
                searchStatusMessage = if (isFavorite) {
                    "${city.name} a fost scos din favorite."
                } else {
                    "${city.name} a fost adaugat la favorite."
                },
            )
        }

        fun onCitySelected(city: CityOption) {
            searchJob?.cancel()
            val preserveCurrentWeather = uiState.weather != null
            uiState = uiState.copy(
                query = "",
                suggestions = uiState.favoriteCities,
                isSearching = false,
                searchStatusMessage = null,
                errorMessage = null,
                showSearch = false,
                pendingCity = if (city.id == uiState.selectedCity.id) null else city,
            )
            refreshWeather(city, preserveCurrentWeather = preserveCurrentWeather)
        }

        fun onRetry() {
            if (uiState.isLoadingWeather) return
            refreshWeather(uiState.selectedCity, preserveCurrentWeather = uiState.weather != null)
        }

        LaunchedEffect(Unit) {
            if (!store.hasStoredFavorites()) {
                store.saveFavorites(initialFavorites)
            }
            refreshWeather(initialSelectedCity, preserveCurrentWeather = false)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFC57B),
                            Color(0xFFFF8E63),
                            Color(0xFF1E5877),
                            Color(0xFF0D1B2A),
                        ),
                    ),
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                DesktopSidebar(
                    uiState = uiState,
                    onOpenSearch = {
                        uiState = uiState.copy(
                            showSearch = true,
                            suggestions = if (uiState.query.isBlank()) uiState.favoriteCities else uiState.suggestions,
                        )
                    },
                    onRefresh = ::onRetry,
                    onCitySelected = ::onCitySelected,
                    onFavoriteToggle = ::onFavoriteToggle,
                )

                DesktopContent(
                    uiState = uiState,
                    onRetry = ::onRetry,
                    modifier = Modifier.weight(1f),
                )
            }

            if (uiState.showSearch) {
                CitySearchDialog(
                    uiState = uiState,
                    onDismiss = {
                        uiState = uiState.copy(
                            showSearch = false,
                            query = "",
                            suggestions = uiState.favoriteCities,
                            isSearching = false,
                            searchStatusMessage = null,
                        )
                    },
                    onQueryChange = ::onQueryChange,
                    onFavoriteToggle = ::onFavoriteToggle,
                    onCitySelected = ::onCitySelected,
                )
            }
        }
    }
}

@Composable
private fun DesktopSidebar(
    uiState: DesktopUiState,
    onOpenSearch: () -> Unit,
    onRefresh: () -> Unit,
    onCitySelected: (CityOption) -> Unit,
    onFavoriteToggle: (CityOption) -> Unit,
) {
    Card(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = SidebarSurface),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Senin",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (uiState.pendingCity != null) {
                        "Pregatesc vremea pentru ${uiState.pendingCity.name}"
                    } else {
                        "Acum in ${uiState.selectedCity.name}"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.76f),
                )
            }

            Button(
                onClick = onOpenSearch,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.14f),
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Schimba orasul",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDCEFF9),
                    contentColor = HeroPrimary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Actualizeaza",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            if (uiState.pendingCity != null && uiState.isLoadingWeather) {
                StatusCard("Se incarca ${uiState.pendingCity.name}...")
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Orase favorite",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )

                if (uiState.favoriteCities.isEmpty()) {
                    StatusCard("Nu ai inca orase favorite. Le poti adauga din cautare.")
                } else {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        uiState.favoriteCities.forEach { city ->
                            FavoriteSidebarItem(
                                city = city,
                                isSelected = city.id == uiState.selectedCity.id,
                                onClick = { onCitySelected(city) },
                                onRemove = { onFavoriteToggle(city) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteSidebarItem(
    city: CityOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val container = if (isSelected) {
        Brush.verticalGradient(listOf(Color(0xFFF6E8D8), Color(0xFFDCEFF8)))
    } else {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.08f)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier
                .background(container)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) HeroPrimary else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = city.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) HeroSecondary else Color.White.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "Scoate din favorite",
                    tint = if (isSelected) HeroAccent else Color(0xFFF8D38E),
                )
            }
        }
    }
}

@Composable
private fun DesktopContent(
    uiState: DesktopUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DesktopHeroCard(
            uiState = uiState,
            onRetry = onRetry,
        )

        uiState.weather?.let { weather ->
            MetricsStrip(weather)
            HourlySection(weather.hourly)
            DailySection(weather.daily)
            Text(
                text = weather.sourceLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
    }
}

@Composable
private fun DesktopHeroCard(
    uiState: DesktopUiState,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HeroSurface),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = uiState.selectedCity.name,
                        style = MaterialTheme.typography.displaySmall,
                        color = HeroPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = uiState.selectedCity.subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = HeroSecondary,
                    )
                    Text(
                        text = uiState.weather?.updatedAtLabel ?: "Se pregatesc datele live...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HeroAccent,
                    )
                }

                uiState.weather?.let { weather ->
                    Surface(
                        color = Color(0xFFDCEFF9),
                        shape = MaterialTheme.shapes.extraLarge,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = weather.current.condition.icon,
                                contentDescription = null,
                                tint = HeroAccent,
                                modifier = Modifier.size(34.dp),
                            )
                        }
                    }
                }
            }

            when {
                uiState.isLoadingWeather && uiState.weather == null -> LoadingBlock()
                uiState.weather != null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = uiState.weather.current.temperatureC.toDegreesLabel(),
                            style = MaterialTheme.typography.displayLarge,
                            color = HeroPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = uiState.weather.current.condition.label,
                                style = MaterialTheme.typography.headlineSmall,
                                color = HeroPrimary,
                            )
                            Text(
                                text = "Se simte ca ${uiState.weather.current.feelsLikeC.toDegreesLabel()}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = HeroSecondary,
                            )
                        }
                    }

                    Text(
                        text = uiState.weather.current.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = HeroPrimary.copy(alpha = 0.86f),
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0D2)),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Nu am putut lua datele live.",
                            style = MaterialTheme.typography.titleMedium,
                            color = HeroPrimary,
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = HeroPrimary.copy(alpha = 0.74f),
                        )
                        Button(onClick = onRetry) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Incearca din nou",
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }

            if (uiState.isLoadingWeather && uiState.weather != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = HeroAccent,
                    )
                    Text(
                        text = if (uiState.pendingCity != null) {
                            "Actualizez pentru ${uiState.pendingCity.name}..."
                        } else {
                            "Actualizez vremea live..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = HeroSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricsStrip(weather: WeatherOverview) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        WeatherMetricCard("Ploaie", "${weather.current.precipitationChance}%", Icons.Rounded.Umbrella)
        WeatherMetricCard("Vant", "${weather.current.windKph} km/h", Icons.Rounded.Air)
        WeatherMetricCard("Umiditate", "${weather.current.humidity}%", Icons.Rounded.WaterDrop)
        WeatherMetricCard("UV", weather.current.uvIndex.toString(), Icons.Rounded.WbSunny)
    }
}

@Composable
private fun WeatherMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
) {
    Card(
        modifier = Modifier.width(210.dp),
        colors = CardDefaults.cardColors(containerColor = ContentSurface),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.82f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun HourlySection(hourly: List<HourlyForecast>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle("Ritmul de azi")

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            hourly.forEach { hour ->
                HourCard(hour)
            }
        }
    }
}

@Composable
private fun HourCard(hour: HourlyForecast) {
    Card(
        modifier = Modifier.width(190.dp),
        colors = CardDefaults.cardColors(containerColor = ContentSurface),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = hour.timeLabel,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Icon(
                imageVector = hour.condition.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = hour.temperatureC.toDegreesLabel(),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Ploaie ${hour.precipitationChance}%",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.76f),
            )
            Text(
                text = "Vant ${hour.windKph} km/h",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.76f),
            )
        }
    }
}

@Composable
private fun DailySection(daily: List<DailyForecast>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle("Urmatoarele zile")
        daily.forEach { day ->
            DailyForecastCard(day)
        }
    }
}

@Composable
private fun DailyForecastCard(day: DailyForecast) {
    val isToday = day.dayLabel == "Azi"
    val cardBrush = if (isToday) {
        Brush.linearGradient(
            colors = listOf(Color(0xFFF3E4D1), Color(0xFFD8EAF5)),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.10f)),
        )
    }
    val primaryText = if (isToday) HeroPrimary else Color.White
    val secondaryText = if (isToday) HeroSecondary else Color.White.copy(alpha = 0.72f)
    val chipBackground = if (isToday) Color(0xFF173046).copy(alpha = 0.10f) else Color.White.copy(alpha = 0.10f)
    val accent = if (isToday) HeroAccent else Color(0xFFF8D38E)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .background(cardBrush)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        color = primaryText,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = day.condition.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = secondaryText,
                    )
                }

                Surface(
                    color = chipBackground,
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = "${day.highC.toDegreesLabel()} / ${day.lowC.toDegreesLabel()}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = primaryText,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Max / min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryText,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isToday) Color.White.copy(alpha = 0.68f) else Color.White.copy(alpha = 0.12f))
                            .padding(14.dp),
                    ) {
                        Icon(
                            imageVector = day.condition.icon,
                            contentDescription = null,
                            tint = primaryText,
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Text(
                        text = if (isToday) "Restul zilei" else "Pe scurt",
                        style = MaterialTheme.typography.titleMedium,
                        color = secondaryText,
                    )
                }

                Surface(
                    color = chipBackground,
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Ploaie",
                            style = MaterialTheme.typography.bodyLarge,
                            color = secondaryText,
                        )
                        Text(
                            text = "${day.precipitationChance}%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = accent,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySearchDialog(
    uiState: DesktopUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onFavoriteToggle: (CityOption) -> Unit,
    onCitySelected: (CityOption) -> Unit,
) {
    DialogWindow(
        onCloseRequest = onDismiss,
        title = "Schimba orasul",
    ) {
        Surface(
            color = Color(0xFFF7F3EB),
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Schimba orasul",
                            style = MaterialTheme.typography.headlineMedium,
                            color = HeroPrimary,
                        )
                        Text(
                            text = "Romania mai intai, apoi orice oras important din lume.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = HeroSecondary,
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = HeroAccent,
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cauta oras") },
                    placeholder = { Text("Oradea, Cluj-Napoca, London, Tokyo...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFFCF7),
                        unfocusedContainerColor = Color(0xFFFFFCF7),
                        disabledContainerColor = Color(0xFFFFFCF7),
                        focusedTextColor = HeroPrimary,
                        unfocusedTextColor = HeroPrimary,
                        cursorColor = HeroAccent,
                        focusedBorderColor = HeroAccent,
                        unfocusedBorderColor = HeroAccent.copy(alpha = 0.45f),
                    ),
                )

                uiState.searchStatusMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = HeroSecondary,
                    )
                }

                if (uiState.isSearching) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = HeroAccent,
                        )
                        Text(
                            text = "Caut orase...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = HeroSecondary,
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp),
                ) {
                    items(uiState.suggestions) { city ->
                        val isFavorite = uiState.favoriteCities.any { it.id == city.id }
                        SearchResultRow(
                            city = city,
                            isSelected = city.id == uiState.selectedCity.id,
                            isFavorite = isFavorite,
                            onFavoriteToggle = { onFavoriteToggle(city) },
                            onClick = { onCitySelected(city) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    city: CityOption,
    isSelected: Boolean,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFDFF0F8) else Color.White,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = HeroPrimary,
                )
                Text(
                    text = city.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = HeroSecondary,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (city.isDefault) {
                    Surface(
                        color = Color(0xFFE9F3F8),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Text(
                            text = "Default",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = HeroPrimary,
                        )
                    }
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isFavorite) "Scoate din favorite" else "Adauga la favorite",
                        tint = if (isFavorite) HeroAccent else HeroSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingBlock() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            strokeWidth = 3.dp,
            color = HeroAccent,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Cer forecast-ul live...",
                style = MaterialTheme.typography.headlineSmall,
                color = HeroPrimary,
            )
            Text(
                text = "Open-Meteo raspunde de obicei foarte repede.",
                style = MaterialTheme.typography.bodyLarge,
                color = HeroSecondary,
            )
        }
    }
}

@Composable
private fun StatusCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        shape = MaterialTheme.shapes.large,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.84f),
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 6.dp),
    )
}

private fun localCityMatches(query: String, favorites: List<CityOption>): List<CityOption> = favorites.filter {
    it.name.contains(query, ignoreCase = true) ||
        it.region.contains(query, ignoreCase = true) ||
        it.country.contains(query, ignoreCase = true)
}

private fun updatedSuggestions(
    normalizedQuery: String,
    updatedFavorites: List<CityOption>,
    currentSuggestions: List<CityOption>,
): List<CityOption> {
    if (normalizedQuery.isBlank()) return updatedFavorites
    return currentSuggestions.map { suggestion ->
        updatedFavorites.find { it.id == suggestion.id } ?: suggestion
    }
}

private fun normalizedFavorites(cities: List<CityOption>): List<CityOption> {
    val distinct = cities.distinctBy { it.id }
    val defaults = distinct.filter { it.isDefault }
    val rest = distinct.filterNot { it.isDefault }
    return defaults + rest
}

private suspend fun <T> runSuspendCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: Throwable) {
    Result.failure(error)
}

private fun Throwable.toUserMessage(): String = when (this) {
    is SocketTimeoutException -> "Actualizarea dureaza prea mult. Incearca din nou."
    is UnknownHostException, is ConnectException -> "Nu am putut contacta serviciul meteo. Verifica internetul."
    is IOException -> message ?: "Momentan nu pot incarca vremea."
    else -> "A aparut o eroare neasteptata. Incearca din nou."
}

private val WeatherCondition.icon: ImageVector
    get() = when (this) {
        WeatherCondition.Clear -> Icons.Rounded.WbSunny
        WeatherCondition.PartlyCloudy -> Icons.Rounded.WbSunny
        WeatherCondition.Cloudy -> Icons.Rounded.Cloud
        WeatherCondition.Rain -> Icons.Rounded.Umbrella
        WeatherCondition.Thunderstorm -> Icons.Rounded.Thunderstorm
        WeatherCondition.Snow -> Icons.Rounded.ModeNight
        WeatherCondition.Mist -> Icons.Rounded.Cloud
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

private fun Int.toDegreesLabel(): String = "${this}\u00B0"
