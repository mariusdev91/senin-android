package com.mariusdev91.senin.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mariusdev91.senin.model.CityOption
import com.mariusdev91.senin.model.DailyForecast
import com.mariusdev91.senin.model.HourlyForecast
import com.mariusdev91.senin.model.WeatherCondition
import com.mariusdev91.senin.model.WeatherOverview

private val HeroCardBackground = Color(0xFFF5EBDD)
private val HeroPrimaryText = Color(0xFF173046)
private val HeroSecondaryText = Color(0xFF5C6F81)
private val HeroAccent = Color(0xFF2C93CB)
private val HeroBadgeBackground = Color(0xFFDCEFF9)
private val DialogBackground = Color(0xFFF7F3EB)
private val DialogPrimaryText = Color(0xFF18324B)
private val DialogSecondaryText = Color(0xFF65788A)
private val DialogAccent = Color(0xFF2C93CB)
private val DialogFieldBackground = Color(0xFFFFFCF7)
private val DialogChipInactive = Color(0xFFDCE8F0)
private val DialogChipActive = Color(0xFF18324B)
private val DialogResultSelected = Color(0xFFDFF0F8)
private val DialogResultDefaultBadge = Color(0xFFE9F3F8)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    onCitySelected: (CityOption) -> Unit,
    onFavoriteToggle: (CityOption) -> Unit,
    onRetry: () -> Unit,
) {
    var showCityPicker by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoadingWeather,
        onRefresh = onRetry,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphereBackground()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .pullRefresh(pullRefreshState),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    Header(
                        selectedCity = uiState.selectedCity,
                        pendingCity = uiState.pendingCity,
                        onPickCity = { showCityPicker = true },
                    )
                }

                item {
                    HeroCard(
                        uiState = uiState,
                        onRetry = onRetry,
                    )
                }

                if (uiState.favoriteCities.isNotEmpty()) {
                    item { SectionTitle("Orase favorite") }
                    item {
                        FavoriteCitiesSection(
                            favoriteCities = uiState.favoriteCities,
                            selectedCity = uiState.selectedCity,
                            onCitySelected = onCitySelected,
                            onFavoriteToggle = onFavoriteToggle,
                        )
                    }
                }

                uiState.weather?.let { weather ->
                    item { MetricsRow(weather = weather) }
                    item { SectionTitle("Ritmul de azi") }
                    item { HourlySection(hourly = weather.hourly) }
                    item { SectionTitle("Urmatoarele zile") }

                    items(weather.daily) { day ->
                        DailyForecastCard(day = day)
                    }

                    item {
                        Text(
                            text = weather.sourceLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoadingWeather,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp),
                backgroundColor = DialogBackground,
                contentColor = HeroAccent,
            )
        }

        if (showCityPicker) {
            CityPickerDialog(
                uiState = uiState,
                onDismiss = { showCityPicker = false },
                onQueryChange = onQueryChange,
                onFavoriteToggle = onFavoriteToggle,
                onCitySelected = {
                    onCitySelected(it)
                    showCityPicker = false
                },
            )
        }
    }
}

@Composable
private fun AtmosphereBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFC57B),
                        Color(0xFFFF8E63),
                        Color(0xFF1E5877),
                        Color(0xFF0D1B2A),
                    ),
                ),
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sunriseCenter = Offset(size.width * 0.18f, size.height * 0.12f)
            val oceanCenter = Offset(size.width * 0.82f, size.height * 0.28f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF1C7).copy(alpha = 0.85f), Color.Transparent),
                    center = sunriseCenter,
                    radius = size.minDimension * 0.45f,
                ),
                radius = size.minDimension * 0.45f,
                center = sunriseCenter,
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF97D7FF).copy(alpha = 0.35f), Color.Transparent),
                    center = oceanCenter,
                    radius = size.minDimension * 0.55f,
                ),
                radius = size.minDimension * 0.55f,
                center = oceanCenter,
            )
        }
    }
}

@Composable
private fun Header(
    selectedCity: CityOption,
    pendingCity: CityOption?,
    onPickCity: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Senin",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (pendingCity != null) {
                    "Pregatesc vremea pentru ${pendingCity.name}"
                } else {
                    "Acum in ${selectedCity.name}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.76f),
            )
        }

        Button(
            onClick = onPickCity,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF14344D).copy(alpha = 0.28f),
                contentColor = Color.White,
            ),
            shape = MaterialTheme.shapes.large,
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
    }
}

@Composable
private fun HeroCard(
    uiState: HomeUiState,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HeroCardBackground),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (uiState.selectedCity.isDefault) "Oradea implicit" else uiState.selectedCity.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = HeroPrimaryText,
                    )
                    Text(
                        text = uiState.selectedCity.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = HeroSecondaryText,
                    )
                    Text(
                        text = uiState.weather?.updatedAtLabel ?: "Se pregatesc datele live...",
                        style = MaterialTheme.typography.labelMedium,
                        color = HeroAccent,
                    )
                }

                if (uiState.weather != null) {
                    WeatherConditionBadge(condition = uiState.weather.current.condition)
                }
            }

            when {
                uiState.isLoadingWeather && uiState.weather == null -> LoadingBlock()
                uiState.weather != null -> WeatherSummary(weather = uiState.weather)
            }

            uiState.errorMessage?.let { message ->
                ErrorCard(
                    message = message,
                    onRetry = onRetry,
                )
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
                        color = HeroSecondaryText,
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteCitiesSection(
    favoriteCities: List<CityOption>,
    selectedCity: CityOption,
    onCitySelected: (CityOption) -> Unit,
    onFavoriteToggle: (CityOption) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(end = 20.dp),
    ) {
        items(favoriteCities) { city ->
            FavoriteCityCard(
                city = city,
                isSelected = city.id == selectedCity.id,
                onClick = { onCitySelected(city) },
                onRemove = { onFavoriteToggle(city) },
            )
        }
    }
}

@Composable
private fun FavoriteCityCard(
    city: CityOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val cardBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF6E8D8),
                Color(0xFFDCEFF8),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.20f),
                Color.White.copy(alpha = 0.10f),
            ),
        )
    }
    val primaryText = if (isSelected) HeroPrimaryText else Color.White
    val secondaryText = if (isSelected) HeroSecondaryText else Color.White.copy(alpha = 0.72f)
    val accent = if (isSelected) HeroAccent else Color(0xFFF8D38E)
    val border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))

    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = border,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .background(cardBrush)
                .width(170.dp)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = if (isSelected) Color(0xFF173046).copy(alpha = 0.10f) else Color.White.copy(alpha = 0.10f),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text = if (city.isDefault) "Default" else "Favorit",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) HeroPrimaryText else Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Scoate din favorite",
                        tint = accent,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = primaryText,
                )
                Text(
                    text = city.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryText,
                )
            }
        }
    }
}

@Composable
private fun LoadingBlock() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            strokeWidth = 3.dp,
            color = HeroAccent,
        )
        Column {
            Text(
                text = "Cer forecast-ul live...",
                style = MaterialTheme.typography.titleMedium,
                color = HeroPrimaryText,
            )
            Text(
                text = "Open-Meteo raspunde de obicei foarte repede.",
                style = MaterialTheme.typography.bodyMedium,
                color = HeroSecondaryText,
            )
        }
    }
}

@Composable
private fun WeatherSummary(weather: WeatherOverview) {
    val current = weather.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = current.temperatureC.toDegreesLabel(),
            style = MaterialTheme.typography.displayLarge,
            color = HeroPrimaryText,
        )

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = current.condition.label,
                style = MaterialTheme.typography.headlineSmall,
                color = HeroPrimaryText,
            )
            Text(
                text = "Se simte ca ${current.feelsLikeC.toDegreesLabel()}",
                style = MaterialTheme.typography.bodyMedium,
                color = HeroSecondaryText,
            )
        }
    }

    Text(
        text = current.summary,
        style = MaterialTheme.typography.bodyLarge,
        color = HeroPrimaryText.copy(alpha = 0.82f),
    )
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
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
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
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

@Composable
private fun MetricsRow(weather: WeatherOverview) {
    val current = weather.current

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item { WeatherMetricCard("Ploaie", "${current.precipitationChance}%", Icons.Rounded.Umbrella) }
        item { WeatherMetricCard("Vant", "${current.windKph} km/h", Icons.Rounded.Air) }
        item { WeatherMetricCard("Umiditate", "${current.humidity}%", Icons.Rounded.WaterDrop) }
        item { WeatherMetricCard("UV", current.uvIndex.toString(), Icons.Rounded.WbSunny) }
    }
}

@Composable
private fun WeatherMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.72f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun HourlySection(hourly: List<HourlyForecast>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(end = 20.dp),
    ) {
        items(hourly) { hour ->
            HourlyForecastCard(
                hour = hour,
                isCurrent = hour.timeLabel == "Acum",
            )
        }
    }
}

@Composable
private fun HourlyForecastCard(
    hour: HourlyForecast,
    isCurrent: Boolean,
) {
    val cardBrush = if (isCurrent) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF6E8D8),
                Color(0xFFE5F2F8),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.20f),
                Color.White.copy(alpha = 0.08f),
            ),
        )
    }

    val primaryText = if (isCurrent) HeroPrimaryText else Color.White
    val secondaryText = if (isCurrent) HeroSecondaryText else Color.White.copy(alpha = 0.72f)
    val tertiaryText = if (isCurrent) HeroSecondaryText.copy(alpha = 0.88f) else Color.White.copy(alpha = 0.6f)
    val accent = if (isCurrent) HeroAccent else Color(0xFFF9D58B)
    val border = if (isCurrent) {
        null
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = border,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .background(cardBrush)
                .width(if (isCurrent) 156.dp else 132.dp)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = hour.timeLabel,
                style = MaterialTheme.typography.labelLarge,
                color = accent,
            )
            Icon(
                imageVector = hour.condition.icon,
                contentDescription = null,
                tint = primaryText,
                modifier = Modifier.size(if (isCurrent) 30.dp else 26.dp),
            )
            Text(
                text = hour.temperatureC.toDegreesLabel(),
                style = if (isCurrent) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                color = primaryText,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Ploaie ${hour.precipitationChance}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                )
                Text(
                    text = "Vant ${hour.windKph} km/h",
                    style = MaterialTheme.typography.bodySmall,
                    color = tertiaryText,
                )
            }
        }
    }
}

@Composable
private fun DailyForecastCard(day: DailyForecast) {
    val isToday = day.dayLabel == "Azi"
    val cardBrush = if (isToday) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFF3E4D1),
                Color(0xFFD8EAF5),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.10f),
            ),
        )
    }
    val primaryText = if (isToday) HeroPrimaryText else Color.White
    val secondaryText = if (isToday) HeroSecondaryText else Color.White.copy(alpha = 0.72f)
    val accent = if (isToday) HeroAccent else Color(0xFFF8D38E)
    val iconBubble = if (isToday) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.12f)
    val chipBackground = if (isToday) Color(0xFF173046).copy(alpha = 0.10f) else Color.White.copy(alpha = 0.10f)
    val chipText = if (isToday) HeroPrimaryText else Color.White
    val border = if (isToday) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = border,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .background(cardBrush)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = day.dayLabel,
                        style = if (isToday) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        color = primaryText,
                    )
                    Text(
                        text = day.condition.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText,
                    )
                }

                Surface(
                    color = chipBackground,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = "${day.highC.toDegreesLabel()} / ${day.lowC.toDegreesLabel()}",
                            style = MaterialTheme.typography.titleMedium,
                            color = chipText,
                        )
                        Text(
                            text = "Max / min",
                            style = MaterialTheme.typography.bodySmall,
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
                    modifier = Modifier.padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(iconBubble)
                            .padding(12.dp),
                    ) {
                        Icon(
                            imageVector = day.condition.icon,
                            contentDescription = null,
                            tint = primaryText,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Text(
                        text = if (isToday) "Restul zilei" else "Pe scurt",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Surface(
                    color = chipBackground,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Ploaie",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText,
                            maxLines = 1,
                        )
                        Text(
                            text = "${day.precipitationChance}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = accent,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherConditionBadge(condition: WeatherCondition) {
    Surface(
        color = HeroBadgeBackground,
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = condition.icon,
                contentDescription = condition.label,
                tint = HeroAccent,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

@Composable
private fun CityPickerDialog(
    uiState: HomeUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onFavoriteToggle: (CityOption) -> Unit,
    onCitySelected: (CityOption) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = DialogBackground,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Schimba orasul",
                            style = MaterialTheme.typography.headlineSmall,
                            color = DialogPrimaryText,
                        )
                        Text(
                            text = "Romania mai intai, apoi orice oras important din lume.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DialogSecondaryText,
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = DialogAccent,
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
                        focusedContainerColor = DialogFieldBackground,
                        unfocusedContainerColor = DialogFieldBackground,
                        disabledContainerColor = DialogFieldBackground,
                        focusedTextColor = DialogPrimaryText,
                        unfocusedTextColor = DialogPrimaryText,
                        cursorColor = DialogAccent,
                        focusedBorderColor = DialogAccent,
                        unfocusedBorderColor = DialogAccent.copy(alpha = 0.45f),
                        focusedLabelColor = DialogAccent,
                        unfocusedLabelColor = DialogSecondaryText,
                        focusedPlaceholderColor = DialogSecondaryText.copy(alpha = 0.8f),
                        unfocusedPlaceholderColor = DialogSecondaryText.copy(alpha = 0.8f),
                        focusedLeadingIconColor = DialogAccent,
                        unfocusedLeadingIconColor = DialogSecondaryText,
                    )
                )

                uiState.searchStatusMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DialogSecondaryText,
                    )
                }

                Text(
                    text = "Favorite rapide",
                    style = MaterialTheme.typography.titleMedium,
                    color = DialogPrimaryText,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.favoriteCities) { city ->
                        AssistChip(
                            onClick = { onCitySelected(city) },
                            label = { Text(city.name) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (city.id == uiState.selectedCity.id) {
                                    DialogChipActive
                                } else {
                                    DialogChipInactive
                                },
                                labelColor = if (city.id == uiState.selectedCity.id) {
                                    Color.White
                                } else {
                                    DialogPrimaryText
                                },
                            ),
                        )
                    }
                }

                Text(
                    text = "Rezultate",
                    style = MaterialTheme.typography.titleMedium,
                    color = DialogPrimaryText,
                )

                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    items(uiState.suggestions) { city ->
                        val isFavorite = uiState.favoriteCities.any { it.id == city.id }
                        CitySuggestionRow(
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
private fun CitySuggestionRow(
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
            containerColor = if (isSelected) {
                DialogResultSelected
            } else {
                Color.White
            },
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
            Column {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = DialogPrimaryText,
                )
                Text(
                    text = city.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DialogSecondaryText,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (city.isDefault) {
                    AssistChip(
                        onClick = onClick,
                        label = { Text("Default") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = DialogResultDefaultBadge,
                            labelColor = DialogPrimaryText,
                        ),
                    )
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isFavorite) "Scoate din favorite" else "Adauga la favorite",
                        tint = if (isFavorite) DialogAccent else DialogSecondaryText,
                    )
                }
            }
        }
    }
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
