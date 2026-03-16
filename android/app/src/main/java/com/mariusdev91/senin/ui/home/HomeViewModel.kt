package com.mariusdev91.senin.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mariusdev91.senin.data.OpenMeteoWeatherRepository
import com.mariusdev91.senin.data.WeatherRepository
import com.mariusdev91.senin.model.CityOption
import com.mariusdev91.senin.model.WeatherOverview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedCity: CityOption,
    val favoriteCities: List<CityOption>,
    val suggestions: List<CityOption>,
    val weather: WeatherOverview? = null,
    val query: String = "",
    val isLoadingWeather: Boolean = false,
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
)

class HomeViewModel : ViewModel() {
    private val repository: WeatherRepository = OpenMeteoWeatherRepository()
    private val defaultCity = repository.defaultCity()
    private val favorites = repository.favoriteCities()

    private var weatherJob: Job? = null
    private var searchJob: Job? = null

    var uiState by mutableStateOf(
        HomeUiState(
            selectedCity = defaultCity,
            favoriteCities = favorites,
            suggestions = favorites,
            isLoadingWeather = true,
        ),
    )
        private set

    init {
        refreshWeather(defaultCity)
    }

    fun onQueryChange(query: String) {
        uiState = uiState.copy(
            query = query,
            isSearching = query.trim().length >= 2,
            suggestions = if (query.isBlank()) favorites else uiState.suggestions,
        )

        searchJob?.cancel()
        val normalized = query.trim()

        if (normalized.isBlank()) {
            uiState = uiState.copy(suggestions = favorites, isSearching = false)
            return
        }

        if (normalized.length < 2) {
            uiState = uiState.copy(
                suggestions = favorites.filter {
                    it.name.contains(normalized, ignoreCase = true) ||
                        it.country.contains(normalized, ignoreCase = true)
                },
                isSearching = false,
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(250)
            val results = runCatching { repository.searchCities(normalized) }
                .getOrElse { favorites.filter { it.name.contains(normalized, ignoreCase = true) } }

            if (uiState.query.trim() == normalized) {
                uiState = uiState.copy(
                    suggestions = if (results.isEmpty()) favorites else results,
                    isSearching = false,
                )
            }
        }
    }

    fun onCitySelected(city: CityOption) {
        uiState = uiState.copy(
            selectedCity = city,
            query = "",
            suggestions = favorites,
        )
        refreshWeather(city)
    }

    fun onRetry() {
        refreshWeather(uiState.selectedCity)
    }

    private fun refreshWeather(city: CityOption) {
        weatherJob?.cancel()
        uiState = uiState.copy(
            selectedCity = city,
            isLoadingWeather = true,
            errorMessage = null,
        )

        weatherJob = viewModelScope.launch {
            runCatching { repository.weatherFor(city) }
                .onSuccess { weather ->
                    uiState = uiState.copy(
                        weather = weather,
                        isLoadingWeather = false,
                        errorMessage = null,
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoadingWeather = false,
                        errorMessage = error.message ?: "Nu am putut incarca vremea acum.",
                    )
                }
        }
    }
}
