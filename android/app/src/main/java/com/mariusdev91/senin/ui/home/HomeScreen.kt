package com.mariusdev91.senin.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ModeNight
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

private enum class HomeTab(val title: String) {
    Forecast("Prognoza"),
    Locations("Locatii"),
    Details("Detalii"),
}

private val MidnightStart = Color(0xFF1E3A8A)
private val MidnightMid = Color(0xFF142B63)
private val MidnightEnd = Color(0xFF0C0E11)
private val ColorSurface = Color(0xFF0C0E11)
private val ColorSurfaceBright = Color(0xFF292C31)
private val ColorSurfaceContainer = Color(0xFF171A1D)
private val ColorSurfaceContainerHigh = Color(0xFF1D2024)
private val ColorSurfaceContainerHighest = Color(0xFF23262A)
private val ColorPrimary = Color(0xFF80AFFD)
private val ColorTertiary = Color(0xFFFFB151)
private val ColorTertiaryDim = Color(0xFFE4942A)
private val ColorOnSurface = Color(0xFFF9F9FD)
private val ColorOnSurfaceVariant = Color(0xFFAAABAF)
private val ColorOutlineVariant = Color(0xFF46484B)
private val Glass = Color(0x331D2024)
private val GlassStrong = Color(0x66292C31)
private val Warm = Color(0xFFFFB151)
private val Cool = Color(0xFF80AFFD)
private val Danger = Color(0xFFFF716C)
private val TextPrimary = ColorOnSurface
private val TextSecondary = Color(0xFFD7E5EC)
private val TextMuted = ColorOnSurfaceVariant

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    onCitySelected: (CityOption) -> Unit,
    onFavoriteToggle: (CityOption) -> Unit,
    onRetry: () -> Unit,
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var isSearchOpen by rememberSaveable { mutableStateOf(false) }
    val activeTab = HomeTab.entries[tabIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MidnightStart, MidnightMid, MidnightEnd))),
    ) {
        BackdropGlow()
        NoiseOverlay()

        when (activeTab) {
            HomeTab.Forecast -> ForecastScreen(
                uiState = uiState,
                onOpenSearch = { isSearchOpen = true },
                onOpenLocations = { tabIndex = HomeTab.Locations.ordinal },
                onOpenDetails = { tabIndex = HomeTab.Details.ordinal },
                onRetry = onRetry,
            )

            HomeTab.Locations -> LocationsScreen(
                uiState = uiState,
                onOpenSearch = { isSearchOpen = true },
                onCitySelected = {
                    onCitySelected(it)
                    tabIndex = HomeTab.Forecast.ordinal
                },
                onFavoriteToggle = onFavoriteToggle,
            )

            HomeTab.Details -> DetailsScreen(uiState = uiState)
        }

        BottomTabs(
            activeTab = activeTab,
            onTabSelected = { tabIndex = it.ordinal },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 18.dp, vertical = 18.dp),
        )

        if (uiState.isLoadingWeather) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33040B12)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Warm)
            }
        }
    }

    if (isSearchOpen) {
        SearchDialog(
            uiState = uiState,
            onDismiss = { isSearchOpen = false },
            onQueryChange = onQueryChange,
            onCitySelected = {
                isSearchOpen = false
                onCitySelected(it)
                tabIndex = HomeTab.Forecast.ordinal
            },
            onFavoriteToggle = onFavoriteToggle,
        )
    }
}

@Composable
private fun ForecastScreen(
    uiState: HomeUiState,
    onOpenSearch: () -> Unit,
    onOpenLocations: () -> Unit,
    onOpenDetails: () -> Unit,
    onRetry: () -> Unit,
) {
    val weather = uiState.weather
    val selectedCity = uiState.pendingCity ?: uiState.selectedCity

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 76.dp, bottom = 104.dp),
        ) {
            Box(
                modifier = Modifier
                    .offset(x = 31.dp, y = 26.dp)
                    .width(2.dp)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                ColorPrimary.copy(alpha = 0.3f),
                                ColorPrimary.copy(alpha = 0.3f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (weather == null && uiState.errorMessage != null) {
                    item {
                        ErrorCard(
                            message = uiState.errorMessage,
                            actionLabel = "Incearca din nou",
                            onAction = onRetry,
                        )
                    }
                }

        if (weather != null) {
            val visibleHours = weather.hourly.take(8)
            val peakHourIndex = visibleHours.indexOfFirst { hour ->
                hour.temperatureC == visibleHours.maxOfOrNull { it.temperatureC }
            }

            item {
                TodayOutlookSection(
                    summary = weather.current.summary,
                )
            }

            items(
                items = visibleHours,
                key = { it.timeLabel },
            ) { hour ->
                val currentIndex = visibleHours.indexOf(hour)
                val isNow = currentIndex == 0
                val isPeak = currentIndex == peakHourIndex
                val sunsetHour = weather.details.sunSchedule.sunsetLabel.take(2)
                val isSunset = sunsetHour.isNotBlank() && hour.timeLabel.startsWith(sunsetHour)

                TimelineWeatherSection(
                            hour = hour,
                            isNow = isNow,
                            isPeak = isPeak,
                            isSunset = isSunset,
                        )
                    }

                    item {
                        Next24HoursSection(
                            hourly = weather.hourly.take(24),
                        )
                    }

                    item {
                        SevenDayForecastSection(
                            daily = weather.daily.take(7),
                        )
                    }

                    item {
                        ForecastBentoDetails(
                            current = weather.current,
                            details = weather.details,
                        )
                    }
                }
            }
        }

        ForecastTopBar(
            city = selectedCity,
            current = weather?.current,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun LocationsScreen(
    uiState: HomeUiState,
    onOpenSearch: () -> Unit,
    onCitySelected: (CityOption) -> Unit,
    onFavoriteToggle: (CityOption) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LocationsTopBar(
            modifier = Modifier.align(Alignment.TopCenter),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 92.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SearchFieldCard(
                    placeholder = "Cauta un oras",
                    onClick = onOpenSearch,
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Locatii salvate",
                        color = ColorOnSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.2.sp,
                        ),
                    )
                    Text(
                        text = "${uiState.favoriteCities.size} orase",
                        color = ColorOnSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (uiState.favoriteCities.isEmpty()) {
                item {
                    EmptyLocationsHint()
                }
            } else {
                items(uiState.favoriteCities, key = { it.id }) { city ->
                    SavedLocationGlassCard(
                        city = city,
                        preview = uiState.locationPreviews[city.id],
                        isSelected = city.id == uiState.selectedCity.id,
                        onClick = { onCitySelected(city) },
                        onFavoriteToggle = { onFavoriteToggle(city) },
                    )
                }

                item {
                    EmptyLocationsHint()
                }
            }
        }
    }
}

@Composable
private fun DetailsScreen(uiState: HomeUiState) {
    val weather = uiState.weather
    Box(modifier = Modifier.fillMaxSize()) {
        DetailsTopBar(modifier = Modifier.align(Alignment.TopCenter))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 92.dp, bottom = 124.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                DetailsHeaderSection(
                    city = uiState.selectedCity,
                    updatedLabel = weather?.updatedAtLabel,
                )
            }

            if (weather == null && uiState.errorMessage != null) {
                item {
                    ErrorCard(
                        message = uiState.errorMessage,
                        actionLabel = "Inapoi",
                        onAction = {},
                    )
                }
            }

            if (weather != null) {
                item { AirQualityCard(weather.details.airQuality) }
                item { PrimaryMetricsGrid(weather.details) }
                item { SunCard(weather.details.sunSchedule) }
                item { BonusMetricsGrid(weather.details, weather.current) }
            }
        }
    }
}

@Composable
private fun DetailsTopBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xCC0F172A),
        shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Senin",
                color = ColorOnSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.8.sp,
                ),
            )
        }
    }
}

@Composable
private fun DetailsHeaderSection(city: CityOption, updatedLabel: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "DETALII METEO",
            color = ColorPrimary,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
            ),
        )
        Text(
            text = city.name,
            color = TextPrimary,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Light),
        )
        Text(
            text = updatedLabel ?: "Ultima actualizare indisponibila",
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
        )
    }
}

@Composable
private fun ForecastTopBar(
    city: CityOption,
    current: CurrentWeather?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xCC0F172A),
        shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Senin",
                    color = ColorOnSurface,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.4.sp,
                    ),
                )
                Text(
                    text = city.name,
                    color = ColorPrimary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.2).sp,
                    ),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = current?.temperatureC?.let { "$it°" } ?: "--°",
                        color = ColorOnSurface,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Light),
                    )
                    Text(
                        text = current?.condition?.label() ?: "Loading",
                        color = ColorOnSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Icon(
                    imageVector = current?.condition?.icon() ?: Icons.Rounded.Cloud,
                    contentDescription = null,
                    tint = current?.condition?.accent() ?: ColorPrimary,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

@Composable
private fun LocationsTopBar(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xCC0F172A),
        shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Senin",
                color = Color(0xFFF8FAFC),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.8.sp,
                ),
            )
        }
    }
}

@Composable
private fun SearchFieldCard(
    placeholder: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        color = ColorSurfaceContainerHighest.copy(alpha = 0.9f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = ColorOnSurfaceVariant,
            )
            Text(
                text = placeholder,
                color = ColorOnSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun SavedLocationGlassCard(
    city: CityOption,
    preview: SavedLocationPreview?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
) {
    val glow = when (preview?.condition ?: WeatherCondition.Cloudy) {
        WeatherCondition.Clear -> ColorTertiary.copy(alpha = 0.05f)
        WeatherCondition.PartlyCloudy -> ColorPrimary.copy(alpha = 0.05f)
        WeatherCondition.Cloudy -> ColorPrimary.copy(alpha = 0.05f)
        WeatherCondition.Rain -> ColorPrimary.copy(alpha = 0.08f)
        WeatherCondition.Thunderstorm -> Danger.copy(alpha = 0.08f)
        WeatherCondition.Snow -> Color.White.copy(alpha = 0.06f)
        WeatherCondition.Mist -> Color.White.copy(alpha = 0.04f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color(0x99171A1D),
        shape = RoundedCornerShape(12.dp),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(glow, Color.Transparent),
                        ),
                    ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = null,
                                tint = ColorOnSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Text(
                            text = city.name,
                            color = ColorOnSurface,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Light,
                                letterSpacing = (-0.6).sp,
                            ),
                        )
                    }
                    Text(
                        text = preview?.let { "${it.localTimeLabel} · ${it.conditionLabel}" } ?: city.subtitle,
                        color = ColorOnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = (preview?.condition ?: WeatherCondition.Cloudy).icon(),
                        contentDescription = null,
                        tint = (preview?.condition ?: WeatherCondition.Cloudy).accent(),
                        modifier = Modifier.size(38.dp),
                    )
                    Text(
                        text = preview?.temperatureC?.let { "$it°" } ?: "--°",
                        color = ColorOnSurface,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1.8).sp,
                        ),
                    )
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Scoate din locatii salvate",
                            tint = Danger,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLocationsHint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ColorSurfaceContainer.copy(alpha = 1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.AddLocationAlt,
                contentDescription = null,
                tint = Color(0xFF747579),
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            text = "Adauga mai multe orase pentru a urmari vremea din alte locuri.",
            color = ColorOnSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(240.dp),
        )
    }
}

@Composable
private fun TodayOutlookSection(summary: String) {
    Box(modifier = Modifier.padding(start = 48.dp)) {
        Box(
            modifier = Modifier
                .offset(x = (-23).dp, y = 6.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(ColorPrimary),
        )
        Box(
            modifier = Modifier
                .offset(x = (-23).dp, y = 6.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color.Transparent),
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0x33292C31),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TODAY'S OUTLOOK",
                    color = ColorPrimary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                    ),
                )
                Text(
                    text = summary,
                    color = ColorOnSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light),
                )
            }
        }
    }
}

@Composable
private fun TimelineWeatherSection(
    hour: HourlyForecast,
    isNow: Boolean,
    isPeak: Boolean,
    isSunset: Boolean,
) {
    val dotColor = when {
        isNow -> ColorPrimary
        isPeak -> ColorTertiary
        else -> Color(0xFF374151)
    }
    val dotRing = when {
        isNow -> ColorPrimary.copy(alpha = 0.2f)
        isPeak -> ColorTertiary.copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    val cardColor = when {
        isNow -> ColorSurfaceContainerHigh.copy(alpha = 0.40f)
        isPeak -> ColorTertiary.copy(alpha = 0.10f)
        else -> ColorSurfaceContainerHigh.copy(alpha = 0.20f)
    }
    val cardBorder = when {
        isPeak -> ColorTertiary.copy(alpha = 0.20f)
        else -> ColorOutlineVariant.copy(alpha = 0.10f)
    }
    val leadingTextColor = if (isNow) ColorOnSurface else ColorOnSurfaceVariant

    Box(modifier = Modifier.padding(start = 48.dp)) {
        Box(
            modifier = Modifier
                .offset(x = (-21).dp, y = 8.dp)
                .size(if (isNow || isPeak) 12.dp else 12.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        if (dotRing != Color.Transparent) {
            Box(
                modifier = Modifier
                    .offset(x = (-25).dp, y = 4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(dotRing),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier.width(48.dp).padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = if (isNow) "Now" else hour.timeLabel,
                    color = leadingTextColor,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                )
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = cardColor,
                contentColor = ColorOnSurface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = when {
                                isSunset -> Icons.Rounded.WbSunny
                                else -> hour.condition.icon()
                            },
                            contentDescription = null,
                            tint = when {
                                isPeak -> ColorTertiary
                                isSunset -> ColorOnSurfaceVariant
                                else -> hour.condition.accent()
                            },
                            modifier = Modifier.size(30.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${hour.temperatureC}°",
                                color = if (isPeak) ColorTertiary else ColorOnSurface,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            )
                            Text(
                                text = when {
                                    isPeak -> "Daily High"
                                    isSunset -> "Sunset"
                                    else -> hour.condition.label()
                                },
                                color = if (isPeak) ColorTertiaryDim else ColorOnSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    letterSpacing = 1.1.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (isNow || (!isPeak && !isSunset)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                TimelineStat("Wind", "${hour.windKph}km/h")
                                TimelineStat("Hum", "${hour.precipitationChance}%")
                            }
                        } else if (isPeak) {
                            TimelineStat("Peak UV", "6 High", alignEnd = true)
                        } else {
                            TimelineStat("Visibility", "12km", alignEnd = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineStat(label: String, value: String, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            text = label.uppercase(),
            color = ColorOnSurfaceVariant,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 9.sp,
                letterSpacing = 0.8.sp,
            ),
        )
        Text(
            text = value,
            color = ColorOnSurface,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
private fun Next24HoursSection(hourly: List<HourlyForecast>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Next 24 Hours",
                color = ColorOnSurface.copy(alpha = 0.9f),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            )
            Text(
                text = "Today",
                color = ColorOnSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            itemsIndexed(hourly) { index, hour ->
                HourlyCapsule(
                    hour = hour,
                    isActive = index == 0,
                )
            }
        }
    }
}

@Composable
private fun HourlyCapsule(
    hour: HourlyForecast,
    isActive: Boolean,
) {
    Surface(
        modifier = Modifier
            .width(64.dp)
            .height(128.dp),
        shape = RoundedCornerShape(999.dp),
        color = if (isActive) ColorPrimary.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.05f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (isActive) "Now" else hour.timeLabel,
                color = if (isActive) hour.condition.accent() else ColorOnSurfaceVariant,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            )
            Icon(
                imageVector = hour.condition.icon(),
                contentDescription = null,
                tint = if (isActive) hour.condition.accent() else hour.condition.mutedAccent(),
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = "${hour.temperatureC}°",
                color = ColorOnSurface,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            )
        }
    }
}

@Composable
private fun SevenDayForecastSection(daily: List<DailyForecast>) {
    val lowMin = daily.minOfOrNull { it.lowC } ?: 0
    val highMax = daily.maxOfOrNull { it.highC } ?: 1
    val range = (highMax - lowMin).coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "7-Day Forecast",
            color = ColorOnSurface.copy(alpha = 0.9f),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
        )

        daily.forEach { day ->
            val startFraction = (day.lowC - lowMin).toFloat() / range.toFloat()
            val endFraction = (day.highC - lowMin).toFloat() / range.toFloat()
            SevenDayRow(
                day = day,
                startFraction = startFraction,
                endFraction = endFraction,
            )
        }
    }
}

@Composable
private fun SevenDayRow(
    day: DailyForecast,
    startFraction: Float,
    endFraction: Float,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.05f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = day.dayLabel.shortDayLabel(),
                modifier = Modifier.width(40.dp),
                color = ColorOnSurface,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = day.condition.icon(),
                        contentDescription = null,
                        tint = day.condition.accent(),
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = if (day.precipitationChance > 0) "${day.precipitationChance}%" else "0%",
                        color = if (day.precipitationChance > 0) Color(0xFF72A1EE) else Color.Transparent,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${day.highC}°",
                        modifier = Modifier.width(32.dp),
                        color = ColorOnSurface,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    )
                    RangeBar(startFraction = startFraction, endFraction = endFraction)
                    Text(
                        text = "${day.lowC}°",
                        modifier = Modifier.width(32.dp),
                        color = ColorOnSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light),
                    )
                }
            }
        }
    }
}

@Composable
private fun RangeBar(startFraction: Float, endFraction: Float) {
    Box(
        modifier = Modifier
            .width(96.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(endFraction.coerceIn(0f, 1f))
                .padding(start = (96.dp * startFraction.coerceIn(0f, 1f))),
        )
        Box(
            modifier = Modifier
                .offset(x = 96.dp * startFraction.coerceIn(0f, 1f))
                .width((96.dp * (endFraction - startFraction).coerceAtLeast(0.05f)))
                .height(4.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(ColorPrimary, ColorTertiary),
                    ),
                ),
        )
    }
}

@Composable
private fun ForecastBentoDetails(
    current: CurrentWeather,
    details: WeatherDetails,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ForecastDetailCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.WaterDrop,
            title = "Humidity",
            value = "${current.humidity}%",
            subtitle = "Dew point similar to the current air mass.",
        )
        ForecastDetailCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Air,
            title = "Wind",
            value = "${details.windKph}",
            suffix = " km/h",
            subtitle = "Direction: ${details.windDirectionLabel}",
        )
    }
}

@Composable
private fun ForecastDetailCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    suffix: String = "",
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.05f),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ColorOnSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = title.uppercase(),
                    color = ColorOnSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 1.5.sp),
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = ColorOnSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light),
                )
                if (suffix.isNotBlank()) {
                    Text(
                        text = suffix,
                        color = ColorOnSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Text(
                text = subtitle,
                color = ColorOnSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun String.shortDayLabel(): String = when {
    length <= 3 -> this
    startsWith("Mai", ignoreCase = true) -> "Mai"
    startsWith("Mar", ignoreCase = true) -> "Mar"
    startsWith("Mie", ignoreCase = true) -> "Mie"
    else -> take(3)
}

@Composable
private fun HeroCard(
    city: CityOption,
    weather: WeatherOverview?,
    onOpenSearch: () -> Unit,
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Atmosfera momentului",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                    )
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = weather?.current?.summary ?: "Actualizam vremea pentru ${city.name}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                    )
                }

                IconButton(onClick = onOpenSearch) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Schimba orasul",
                        tint = TextPrimary,
                    )
                }
            }

            val current = weather?.current
            if (current != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ConditionBadge(condition = current.condition, size = 84.dp, iconSize = 40.dp)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "${current.temperatureC}°",
                            style = MaterialTheme.typography.displayLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = current.condition.label(),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary,
                        )
                        Text(
                            text = "Se simte ca ${current.feelsLikeC}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricPill(
                            title = "Umiditate",
                            value = "${current.humidity}%",
                            modifier = Modifier.weight(1f),
                        )
                        MetricPill(
                            title = "Vant",
                            value = "${current.windKph} km/h",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    MetricPill(
                        title = "UV",
                        value = current.uvIndex.toString(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (weather.sourceLabel.isNotBlank()) {
                    Text(
                        text = weather.sourceLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyTimeline(hourly: List<HourlyForecast>) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            hourly.take(10).forEachIndexed { index, hour ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(
                        modifier = Modifier.width(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(hour.condition.accent()),
                        )
                        if (index < 9) {
                            Spacer(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .width(2.dp)
                                    .height(68.dp)
                                    .background(Color(0x28FFFFFF)),
                            )
                        }
                    }

                    InsetCard(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = hour.timeLabel,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Text(
                                    text = hour.condition.label(),
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "Ploaie ${hour.precipitationChance}% · Vant ${hour.windKph} km/h",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ConditionBadge(condition = hour.condition, size = 42.dp, iconSize = 20.dp)
                                Text(
                                    text = "${hour.temperatureC}°",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyCard(day: DailyForecast) {
    GlassCard(padding = PaddingValues(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ConditionBadge(condition = day.condition, size = 58.dp, iconSize = 26.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = day.dayLabel,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = day.condition.label(),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Ploaie ${day.precipitationChance}%",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${day.highC}° / ${day.lowC}°",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Max / Min",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun LocationCard(
    city: CityOption,
    preview: SavedLocationPreview?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
) {
    val brush = if (isSelected) {
        Brush.linearGradient(listOf(Color(0x26FFD59B), Color(0x2269D4FF)))
    } else {
        Brush.linearGradient(listOf(GlassStrong, Glass))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(28.dp),
    ) {
        Row(
            modifier = Modifier
                .background(brush)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ConditionBadge(
                condition = preview?.condition ?: WeatherCondition.PartlyCloudy,
                size = 56.dp,
                iconSize = 26.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = city.name,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = city.subtitle,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (preview != null) {
                    Text(
                        text = "${preview.localTimeLabel} · ${preview.conditionLabel}",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Scoate din favorite",
                        tint = Warm,
                    )
                }
                if (preview != null) {
                    Text(
                        text = "${preview.temperatureC}°",
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchDialog(
    uiState: HomeUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCitySelected: (CityOption) -> Unit,
    onFavoriteToggle: (CityOption) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .heightIn(max = 720.dp),
            color = Color(0xFF10253A),
            shape = RoundedCornerShape(32.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Schimba orasul",
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Badge("${uiState.suggestions.size} rezultate")
                }

                TextField(
                    value = uiState.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Cauta orase din Romania sau din lume",
                            color = TextMuted,
                        )
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ColorSurfaceContainerHighest.copy(alpha = 0.88f),
                        unfocusedContainerColor = ColorSurfaceContainerHighest.copy(alpha = 0.72f),
                        disabledContainerColor = ColorSurfaceContainerHighest.copy(alpha = 0.72f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        disabledTextColor = TextPrimary,
                        cursorColor = Warm,
                    ),
                    shape = RoundedCornerShape(28.dp),
                )

                if (uiState.searchStatusMessage != null) {
                    Text(
                        text = uiState.searchStatusMessage,
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.suggestions, key = { it.id }) { city ->
                        val isFavorite = uiState.favoriteCities.any { it.id == city.id }
                        InsetCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCitySelected(city) },
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = city.name,
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = city.subtitle,
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                IconButton(onClick = { onFavoriteToggle(city) }) {
                                    Icon(
                                        imageVector = if (isFavorite) {
                                            Icons.Rounded.Favorite
                                        } else {
                                            Icons.Rounded.FavoriteBorder
                                        },
                                        contentDescription = if (isFavorite) "Scoate din favorite" else "Adauga la favorite",
                                        tint = if (isFavorite) Warm else TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AirQualityCard(airQuality: AirQuality) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Air,
                        contentDescription = null,
                        tint = ColorPrimary,
                    )
                    Text(
                        text = "AIR QUALITY",
                        color = TextPrimary.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.2.sp,
                        ),
                    )
                }
                Badge("Primary: ${airQuality.primaryPollutant}")
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = airQuality.aqi.toString(),
                    color = TextPrimary,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Light),
                )
                Text(
                    text = "- ${airQuality.category}",
                    color = ColorTertiary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ColorSurfaceContainerHighest),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((airQuality.aqi.coerceIn(0, 300) / 300f).coerceAtLeast(0.06f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.horizontalGradient(listOf(ColorPrimary, ColorTertiary))),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("0", "50", "100", "150", "200", "300+").forEach { tick ->
                    Text(
                        text = tick,
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                        ),
                    )
                }
            }

            Text(
                text = airQuality.description,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun SunCard(schedule: SunSchedule) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.WbSunny,
                    contentDescription = null,
                    tint = TextPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "SUN SCHEDULE",
                    color = TextPrimary.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    ),
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp),
            ) {
                val baseline = size.height * 0.78f
                val start = Offset(size.width * 0.02f, baseline)
                val end = Offset(size.width * 0.98f, baseline)
                val control1 = Offset(size.width * 0.30f, size.height * -0.10f)
                val control2 = Offset(size.width * 0.70f, size.height * -0.10f)
                val path = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(control1.x, control1.y, control2.x, control2.y, end.x, end.y)
                }
                drawPath(
                    path = path,
                    color = Color(0x26FFFFFF),
                    style = Stroke(width = 3f, cap = StrokeCap.Round),
                )
                drawLine(
                    color = Color(0x24FFFFFF),
                    start = start,
                    end = end,
                    strokeWidth = 2f,
                    cap = StrokeCap.Round,
                )
                val sun = bezierPoint(start, control1, control2, end, 0.70f)
                drawPath(
                    path = Path().apply {
                        moveTo(start.x, baseline)
                        cubicTo(control1.x, control1.y, control2.x, control2.y, sun.x, sun.y)
                    },
                    color = ColorTertiary,
                    style = Stroke(width = 4f, cap = StrokeCap.Round),
                )
                drawCircle(color = ColorTertiary.copy(alpha = 0.2f), radius = 24f, center = sun)
                drawCircle(color = ColorTertiary, radius = 11f, center = sun)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("SUNRISE", color = TextMuted, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp))
                    Text(
                        text = schedule.sunriseLabel,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Light),
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(38.dp)
                        .background(Color(0x14FFFFFF)),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("DAYLIGHT", color = TextMuted, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp))
                    Text(
                        text = schedule.daylightDurationLabel,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Light),
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(38.dp)
                        .background(Color(0x14FFFFFF)),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("SUNSET", color = TextMuted, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp))
                    Text(
                        text = schedule.sunsetLabel,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Light),
                    )
                }
            }
        }
    }
}

private fun bezierPoint(
    p0: Offset,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    t: Float,
): Offset {
    val oneMinus = 1f - t
    val x =
        oneMinus * oneMinus * oneMinus * p0.x +
            3f * oneMinus * oneMinus * t * p1.x +
            3f * oneMinus * t * t * p2.x +
            t * t * t * p3.x
    val y =
        oneMinus * oneMinus * oneMinus * p0.y +
            3f * oneMinus * oneMinus * t * p1.y +
            3f * oneMinus * t * t * p2.y +
            t * t * t * p3.y
    return Offset(x, y)
}

@Composable
private fun PrimaryMetricsGrid(details: WeatherDetails) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            DetailMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Air,
                title = "WIND",
                value = details.windKph.toString(),
                unit = "km/h",
                caption = details.windDirectionLabel,
                accentIcon = Icons.Rounded.LocationOn,
                accentText = details.windDirectionLabel,
            )
            DetailMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.WaterDrop,
                title = "RAINFALL",
                value = details.precipitationChance.toString(),
                unit = "%",
                caption = rainfallCaption(details.precipitationChance),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            DetailMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Visibility,
                title = "VISIBILITY",
                value = details.visibilityKm.toString(),
                unit = "km",
                caption = visibilityCaption(details.visibilityKm),
                captionColor = ColorPrimary,
            )
            DetailMetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Speed,
                title = "PRESSURE",
                value = details.pressureHpa.toString(),
                unit = "hPa",
                caption = pressureCaption(details.pressureHpa),
            )
        }
    }
}

@Composable
private fun BonusMetricsGrid(details: WeatherDetails, current: CurrentWeather) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        DetailMetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.WaterDrop,
            title = "HUMIDITY",
            value = details.humidity.toString(),
            unit = "%",
            caption = "Dew point: ${humidityDewPoint(current)}°",
        )
        DetailMetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.WbSunny,
            title = "UV INDEX",
            value = details.uvIndex.toString(),
            unit = uvLabel(details.uvIndex),
            caption = uvRecommendation(details.uvIndex),
        )
    }
}

@Composable
private fun DetailMetricCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    unit: String,
    caption: String,
    accentIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    accentText: String? = null,
    captionColor: Color = TextPrimary,
) {
    InsetCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = title,
                    color = TextPrimary.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    ),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light),
                )
                Text(
                    text = unit,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            if (accentIcon != null && accentText != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = accentIcon,
                        contentDescription = null,
                        tint = ColorPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = accentText,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            } else {
                Text(
                    text = caption,
                    color = captionColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun rainfallCaption(chance: Int): String = when {
    chance >= 70 -> "Averse probabile"
    chance >= 30 -> "Posibile ploi scurte"
    chance > 0 -> "Ploaie usoara posibila"
    else -> "Nu se asteapta ploaie"
}

private fun visibilityCaption(visibilityKm: Int): String = when {
    visibilityKm >= 10 -> "Clear view"
    visibilityKm >= 6 -> "Vizibilitate buna"
    visibilityKm >= 3 -> "Vizibilitate moderata"
    else -> "Vizibilitate redusa"
}

private fun pressureCaption(pressureHpa: Int): String = when {
    pressureHpa < 1005 -> "Scazuta"
    pressureHpa > 1020 -> "Ridicata"
    else -> "Steady"
}

private fun humidityDewPoint(current: CurrentWeather): Int = (current.feelsLikeC - 2).coerceAtLeast(-10)

private fun uvLabel(uvIndex: Int): String = when {
    uvIndex <= 2 -> "Low"
    uvIndex <= 5 -> "Moderate"
    uvIndex <= 7 -> "High"
    uvIndex <= 10 -> "Very High"
    else -> "Extreme"
}

private fun uvRecommendation(uvIndex: Int): String = when {
    uvIndex <= 2 -> "Protectie minima necesara"
    uvIndex <= 5 -> "SPF 30 recomandat"
    uvIndex <= 7 -> "Evita expunerea lunga"
    else -> "Stai la umbra la pranz"
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(text = subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SearchLauncher(label: String, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        padding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Rounded.Search, contentDescription = null, tint = TextSecondary)
            Text(text = label, color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ErrorCard(message: String, actionLabel: String, onAction: () -> Unit) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Momentan nu pot afisa vremea",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = message, color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
            SearchLauncher(label = actionLabel, onClick = onAction)
        }
    }
}

@Composable
private fun BottomTabs(
    activeTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        HomeTab.Forecast to Icons.Rounded.Cloud,
        HomeTab.Locations to Icons.Rounded.LocationOn,
        HomeTab.Details to Icons.Rounded.Settings,
    )

    Surface(
        modifier = modifier,
        color = Color(0xCC0F172A),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            items.forEach { (tab, icon) ->
                val selected = activeTab == tab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) Color(0x3380AFFD) else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ColorPrimary.copy(alpha = 0.18f)),
                            )
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.title,
                            tint = if (selected) Color(0xFF93C5FD) else Color(0xFF64748B),
                        )
                    }
                    Text(
                        text = tab.title,
                        color = if (selected) Color(0xFFBFDBFE) else Color(0xFF64748B),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                        ),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun Badge(label: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(ColorSurfaceContainerHighest.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            color = TextPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ConditionBadge(
    condition: WeatherCondition,
    size: Dp,
    iconSize: Dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        ColorSurfaceContainerHigh.copy(alpha = 0.45f),
                        ColorSurfaceContainerHighest.copy(alpha = 0.28f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size((iconSize.value * 1.9f).dp)
                .clip(CircleShape)
                .background(condition.accent().copy(alpha = 0.12f)),
        )
        Icon(
            imageVector = condition.icon(),
            contentDescription = condition.label(),
            tint = condition.accent(),
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun MetricPill(title: String, value: String, modifier: Modifier = Modifier) {
    InsetCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(20.dp),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ColorSurfaceContainerHigh.copy(alpha = 0.42f),
                            ColorSurfaceContainer.copy(alpha = 0.28f),
                        ),
                    ),
                ),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent,
                                ColorPrimary.copy(alpha = 0.03f),
                            ),
                        ),
                    ),
            )
            Column(modifier = Modifier.padding(padding)) {
                content()
            }
        }
    }
}

@Composable
private fun InsetCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = ColorSurfaceContainerHighest.copy(alpha = 0.28f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun BackdropGlow() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0x221E3A8A),
                    Color.Transparent,
                    Color(0x120C0E11),
                ),
            ),
        )
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0x1480AFFD),
                    Color.Transparent,
                    Color(0x10FFB151),
                ),
                start = Offset(0f, size.height * 0.08f),
                end = Offset(size.width, size.height * 0.92f),
            ),
        )
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.028f),
                    Color.Transparent,
                ),
                start = Offset(size.width * 0.18f, 0f),
                end = Offset(size.width * 0.82f, size.height),
            ),
        )
    }
}

@Composable
private fun NoiseOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stepX = size.width / 18f
        val stepY = size.height / 32f
        for (row in 0..32) {
            for (col in 0..18) {
                val alpha = if ((row + col) % 3 == 0) 0.018f else 0.01f
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 1.1f,
                    center = Offset(
                        x = col * stepX + (row % 2) * 6f,
                        y = row * stepY,
                    ),
                )
            }
        }
    }
}

private fun WeatherCondition.label(): String = when (this) {
    WeatherCondition.Clear -> "Senin"
    WeatherCondition.PartlyCloudy -> "Cer variabil"
    WeatherCondition.Cloudy -> "Innorat"
    WeatherCondition.Rain -> "Ploaie"
    WeatherCondition.Thunderstorm -> "Furtuna"
    WeatherCondition.Snow -> "Ninsoare"
    WeatherCondition.Mist -> "Ceata"
}

private fun WeatherCondition.icon() = when (this) {
    WeatherCondition.Clear -> Icons.Rounded.WbSunny
    WeatherCondition.PartlyCloudy -> Icons.Rounded.WbSunny
    WeatherCondition.Cloudy -> Icons.Rounded.Cloud
    WeatherCondition.Rain -> Icons.Rounded.Grain
    WeatherCondition.Thunderstorm -> Icons.Rounded.Thunderstorm
    WeatherCondition.Snow -> Icons.Rounded.Grain
    WeatherCondition.Mist -> Icons.Rounded.ModeNight
}

private fun WeatherCondition.accent(): Color = when (this) {
    WeatherCondition.Clear -> Warm
    WeatherCondition.PartlyCloudy -> Color(0xFFFFD37A)
    WeatherCondition.Cloudy -> Color(0xFFD7E5EC)
    WeatherCondition.Rain -> Cool
    WeatherCondition.Thunderstorm -> Danger
    WeatherCondition.Snow -> Color(0xFFF0FBFF)
    WeatherCondition.Mist -> Color(0xFFAAABAF)
}

private fun WeatherCondition.mutedAccent(): Color = when (this) {
    WeatherCondition.Clear -> Color(0xFFDFAF63)
    WeatherCondition.PartlyCloudy -> Color(0xFFD1BE8E)
    WeatherCondition.Cloudy -> Color(0xFFB7C4CB)
    WeatherCondition.Rain -> Color(0xFF93B7E8)
    WeatherCondition.Thunderstorm -> Color(0xFFD59090)
    WeatherCondition.Snow -> Color(0xFFE5EEF6)
    WeatherCondition.Mist -> Color(0xFFB1B5BB)
}
