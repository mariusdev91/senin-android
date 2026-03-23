package com.mariusdev91.senin.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mariusdev91.senin.data.FavoriteCitiesStore
import com.mariusdev91.senin.data.OpenMeteoWeatherRepository
import com.mariusdev91.senin.data.SelectedCityStore
import com.mariusdev91.senin.data.WeatherRepository
import com.mariusdev91.senin.model.CityOption
import com.mariusdev91.senin.model.SavedLocationPreview
import com.mariusdev91.senin.model.WeatherOverview
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedCity: CityOption,
    val pendingCity: CityOption? = null,
    val favoriteCities: List<CityOption>,
    val locationPreviews: Map<String, SavedLocationPreview> = emptyMap(),
    val suggestions: List<CityOption>,
    val weather: WeatherOverview? = null,
    val query: String = "",
    val isLoadingWeather: Boolean = false,
    val isLoadingLocationPreviews: Boolean = false,
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val searchStatusMessage: String? = null,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WeatherRepository = OpenMeteoWeatherRepository()
    private val selectedCityStore = SelectedCityStore(application)
    private val favoriteCitiesStore = FavoriteCitiesStore(application)
    private val defaultCity = repository.defaultCity()

    private var weatherJob: Job? = null
    private var searchJob: Job? = null
    private var locationPreviewJob: Job? = null

    private val initialFavorites = normalizedFavorites(
        if (favoriteCitiesStore.hasStoredFavorites()) {
            favoriteCitiesStore.load()
        } else {
            repository.favoriteCities()
        },
    )
    private val initialSelectedCity = selectedCityStore.load() ?: defaultCity

    var uiState by mutableStateOf(
        HomeUiState(
            selectedCity = initialSelectedCity,
            favoriteCities = initialFavorites,
            suggestions = initialFavorites,
            isLoadingWeather = true,
            isLoadingLocationPreviews = initialFavorites.isNotEmpty(),
        ),
    )
        private set

    init {
        if (!favoriteCitiesStore.hasStoredFavorites()) {
            favoriteCitiesStore.save(initialFavorites)
        }
        loadLocationPreviews(initialFavorites)
        refreshWeather(initialSelectedCity, preserveCurrentWeather = false)
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
            val localResults = localCityMatches(normalized)
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

        searchJob = viewModelScope.launch {
            delay(250)

            val results = runSuspendCatching { repository.searchCities(normalized) }
                .getOrElse { localCityMatches(normalized) }

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

    fun onCitySelected(city: CityOption) {
        searchJob?.cancel()
        val currentState = uiState
        val preserveCurrentWeather = currentState.weather != null

        uiState = uiState.copy(
            query = "",
            suggestions = currentState.favoriteCities,
            isSearching = false,
            searchStatusMessage = null,
            errorMessage = null,
            pendingCity = if (city.id == currentState.selectedCity.id) null else city,
        )

        refreshWeather(city, preserveCurrentWeather = preserveCurrentWeather)
    }

    fun onFavoriteToggle(city: CityOption) {
        val isFavorite = uiState.favoriteCities.any { it.id == city.id }
        val updatedFavorites = if (isFavorite) {
            uiState.favoriteCities.filterNot { it.id == city.id }
        } else {
            normalizedFavorites(listOf(city) + uiState.favoriteCities)
        }

        favoriteCitiesStore.save(updatedFavorites)
        uiState = uiState.copy(
            favoriteCities = updatedFavorites,
            suggestions = updatedSuggestionsForFavorites(updatedFavorites),
            locationPreviews = uiState.locationPreviews.filterKeys { key ->
                updatedFavorites.any { it.id == key }
            },
            searchStatusMessage = if (!isFavorite) {
                "${city.name} a fost adaugat la favorite."
            } else {
                "${city.name} a fost scos din favorite."
            },
        )

        loadLocationPreviews(updatedFavorites)
    }

    fun onRetry() {
        if (uiState.isLoadingWeather) return
        refreshWeather(uiState.selectedCity, preserveCurrentWeather = uiState.weather != null)
        loadLocationPreviews(uiState.favoriteCities)
    }

    private fun refreshWeather(city: CityOption, preserveCurrentWeather: Boolean) {
        weatherJob?.cancel()
        uiState = uiState.copy(
            weather = if (preserveCurrentWeather) uiState.weather else null,
            isLoadingWeather = true,
            errorMessage = null,
            pendingCity = if (city.id == uiState.selectedCity.id) uiState.pendingCity else city,
        )

        weatherJob = viewModelScope.launch {
            runSuspendCatching { repository.weatherFor(city) }
                .onSuccess { weather ->
                    selectedCityStore.save(city)
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

    private fun loadLocationPreviews(cities: List<CityOption>) {
        locationPreviewJob?.cancel()

        if (cities.isEmpty()) {
            uiState = uiState.copy(
                isLoadingLocationPreviews = false,
                locationPreviews = emptyMap(),
            )
            return
        }

        uiState = uiState.copy(isLoadingLocationPreviews = true)

        locationPreviewJob = viewModelScope.launch {
            val previews = buildMap {
                for (city in cities) {
                    val preview = runSuspendCatching { repository.currentFor(city) }.getOrNull() ?: continue
                    put(city.id, preview)
                }
            }

            uiState = uiState.copy(
                locationPreviews = previews,
                isLoadingLocationPreviews = false,
            )
        }
    }

    private fun updatedSuggestionsForFavorites(updatedFavorites: List<CityOption>): List<CityOption> {
        val normalizedQuery = uiState.query.trim()
        if (normalizedQuery.isBlank()) return updatedFavorites

        return uiState.suggestions.map { suggestion ->
            updatedFavorites.find { it.id == suggestion.id } ?: suggestion
        }
    }

    private fun localCityMatches(query: String): List<CityOption> = uiState.favoriteCities.filter {
        it.name.contains(query, ignoreCase = true) ||
            it.region.contains(query, ignoreCase = true) ||
            it.country.contains(query, ignoreCase = true)
    }

    private fun normalizedFavorites(cities: List<CityOption>): List<CityOption> {
        val distinct = cities.distinctBy { it.id }
        val default = distinct.filter { it.isDefault }
        val rest = distinct.filterNot { it.isDefault }
        return default + rest
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
}
