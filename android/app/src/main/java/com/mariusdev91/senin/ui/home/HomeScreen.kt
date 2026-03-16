package com.mariusdev91.senin.ui.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ModeNight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    onCitySelected: (CityOption) -> Unit,
    onRetry: () -> Unit,
) {
    var showCityPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AtmosphereBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Header(
                    city = uiState.selectedCity,
                    onPickCity = { showCityPicker = true },
                )
            }

            item {
                HeroCard(
                    uiState = uiState,
                    onRetry = onRetry,
                )
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

        if (showCityPicker) {
            CityPickerDialog(
                uiState = uiState,
                onDismiss = { showCityPicker = false },
                onQueryChange = onQueryChange,
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
    city: CityOption,
    onPickCity: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Senin",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.72f),
            )
            Text(
                text = "Vreme calma, fara reclame.",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
        }

        AssistChip(
            onClick = onPickCity,
            label = {
                Text(
                    text = city.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = Color.White.copy(alpha = 0.16f),
                labelColor = Color.White,
                leadingIconContentColor = Color.White,
            ),
        )
    }
}

@Composable
private fun HeroCard(
    uiState: HomeUiState,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F6F1)),
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
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = uiState.selectedCity.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    )
                    Text(
                        text = uiState.weather?.updatedAtLabel ?: "Se pregatesc datele live...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                if (uiState.weather != null) {
                    WeatherConditionBadge(condition = uiState.weather.current.condition)
                }
            }

            when {
                uiState.isLoadingWeather && uiState.weather == null -> {
                    LoadingBlock()
                }

                uiState.weather != null -> {
                    WeatherSummary(weather = uiState.weather)
                }
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
                    )
                    Text(
                        text = "Actualizez vremea live...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    )
                }
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
        )
        Column {
            Text(
                text = "Cer forecast-ul live...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Open-Meteo raspunde de obicei foarte repede.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
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
            text = "${current.temperatureC}°",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = current.condition.label,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Se simte ca ${current.feelsLikeC}°",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
        }
    }

    Text(
        text = current.summary,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
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
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(hourly) { hour ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier
                        .width(116.dp)
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = hour.timeLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                    Icon(
                        imageVector = hour.condition.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                    Text(
                        text = "${hour.temperatureC}°",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                    Text(
                        text = "Ploaie ${hour.precipitationChance}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f),
                    )
                    Text(
                        text = "Vant ${hour.windKph}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.64f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastCard(day: DailyForecast) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
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
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(10.dp),
                ) {
                    Icon(
                        imageVector = day.condition.icon,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
                Column {
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Text(
                        text = day.condition.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${day.highC}° / ${day.lowC}°",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    text = "${day.precipitationChance}% precipitatii",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.68f),
                )
            }
        }
    }
}

@Composable
private fun WeatherConditionBadge(condition: WeatherCondition) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = condition.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = condition.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun CityPickerDialog(
    uiState: HomeUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCitySelected: (CityOption) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = Color(0xFFF7F5F0),
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
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Romania mai intai, apoi orice oras important din lume.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
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
                )

                if (uiState.isSearching) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = "Caut live in Open-Meteo...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    }
                }

                Text(
                    text = "Favorite rapide",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.favoriteCities) { city ->
                        AssistChip(
                            onClick = { onCitySelected(city) },
                            label = { Text(city.name) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (city.id == uiState.selectedCity.id) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                labelColor = if (city.id == uiState.selectedCity.id) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            ),
                        )
                    }
                }

                Text(
                    text = "Rezultate",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    items(uiState.suggestions) { city ->
                        CitySuggestionRow(
                            city = city,
                            isSelected = city.id == uiState.selectedCity.id,
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
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
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
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = city.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                )
            }

            if (city.isDefault) {
                AssistChip(
                    onClick = onClick,
                    label = { Text("Default") },
                )
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
