package com.mariusdev91.senin.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mariusdev91.senin.data.CurrentLocationProvider
import com.mariusdev91.senin.data.FavoriteCitiesStore
import com.mariusdev91.senin.data.LanguageStore
import com.mariusdev91.senin.data.OpenMeteoWeatherRepository
import com.mariusdev91.senin.data.SelectedCityStore
import com.mariusdev91.senin.data.WeatherRepository
import com.mariusdev91.senin.i18n.AppLanguage
import com.mariusdev91.senin.i18n.AppStrings
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
    val selectedLanguage: AppLanguage = AppLanguage.Romanian,
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
    private val languageStore = LanguageStore(application)
    private val currentLocationProvider = CurrentLocationProvider(application)
    private val defaultCity = repository.defaultCity()

    private var weatherJob: Job? = null
    private var searchJob: Job? = null
    private var locationPreviewJob: Job? = null
    private var currentLocationJob: Job? = null
    private var hasAttemptedStartupLocation by mutableStateOf(false)

    private val initialLanguage = languageStore.load()
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
            selectedLanguage = initialLanguage,
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

    fun onLocationPermissionUpdated(isGranted: Boolean) {
        if (!isGranted || hasAttemptedStartupLocation) return
        hasAttemptedStartupLocation = true

        currentLocationJob?.cancel()
        currentLocationJob = viewModelScope.launch {
            val currentCity = runSuspendCatching {
                currentLocationProvider.resolveCurrentCity(uiState.selectedLanguage)
            }.getOrNull() ?: return@launch

            val activeCity = uiState.pendingCity ?: uiState.selectedCity
            val sameCoordinates =
                activeCity.latitude == currentCity.latitude &&
                    activeCity.longitude == currentCity.longitude

            if (sameCoordinates) return@launch

            refreshWeather(currentCity, preserveCurrentWeather = uiState.weather != null)
        }
    }

    fun onLanguageSelected(language: AppLanguage) {
        if (language == uiState.selectedLanguage) return
        languageStore.save(language)
        val strings = AppStrings(language)
        val currentQuery = uiState.query
        uiState = uiState.copy(
            selectedLanguage = language,
            searchStatusMessage = null,
            errorMessage = null,
            suggestions = if (currentQuery.isBlank()) uiState.favoriteCities else uiState.suggestions,
        )
        loadLocationPreviews(uiState.favoriteCities)
        refreshWeather(uiState.pendingCity ?: uiState.selectedCity, preserveCurrentWeather = uiState.weather != null)
        if (currentQuery.isBlank()) {
            uiState = uiState.copy(
                searchStatusMessage = if (uiState.favoriteCities.isEmpty()) strings.searchFavoritesHint() else null,
            )
        } else {
            onQueryChange(currentQuery)
        }
    }

    fun onQueryChange(query: String) {
        val normalized = query.trim()
        val favorites = uiState.favoriteCities
        val strings = AppStrings(uiState.selectedLanguage)
        searchJob?.cancel()

        if (normalized.isBlank()) {
            uiState = uiState.copy(
                query = query,
                suggestions = favorites,
                isSearching = false,
                searchStatusMessage = if (favorites.isEmpty()) strings.searchFavoritesHint() else null,
            )
            return
        }

        if (normalized.length < 2) {
            val localResults = localCityMatches(normalized)
            uiState = uiState.copy(
                query = query,
                suggestions = localResults,
                isSearching = false,
                searchStatusMessage = if (localResults.isEmpty()) strings.searchMinCharsHint() else null,
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

            val results = runSuspendCatching { repository.searchCities(normalized, uiState.selectedLanguage) }
                .getOrElse { localCityMatches(normalized) }

            if (uiState.query.trim() != normalized) return@launch

            uiState = uiState.copy(
                suggestions = results,
                isSearching = false,
                searchStatusMessage = when {
                    results.isEmpty() -> strings.searchNoResults(normalized)
                    results.size <= favorites.size && results.all { city ->
                        favorites.any { it.id == city.id }
                    } -> strings.searchLocalFavoritesOnly()
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
        val strings = AppStrings(uiState.selectedLanguage)

        favoriteCitiesStore.save(updatedFavorites)
        uiState = uiState.copy(
            favoriteCities = updatedFavorites,
            suggestions = updatedSuggestionsForFavorites(updatedFavorites),
            locationPreviews = uiState.locationPreviews.filterKeys { key ->
                updatedFavorites.any { it.id == key }
            },
            searchStatusMessage = if (!isFavorite) {
                strings.addedToFavorites(city.name)
            } else {
                strings.removedFromFavorites(city.name)
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
            runSuspendCatching { repository.weatherFor(city, uiState.selectedLanguage) }
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
                        errorMessage = error.toUserMessage(uiState.selectedLanguage),
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
                    val preview = runSuspendCatching { repository.currentFor(city, uiState.selectedLanguage) }.getOrNull() ?: continue
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

    private suspend inline fun <T> runSuspendCatching(crossinline block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun Throwable.toUserMessage(language: AppLanguage): String {
        val strings = AppStrings(language)
        return when (this) {
            is SocketTimeoutException -> strings.timeoutError()
            is UnknownHostException, is ConnectException -> strings.networkError()
            is IOException -> message ?: strings.genericLoadError()
            else -> strings.unexpectedError()
        }
    }

    private fun normalizedFavorites(cities: List<CityOption>): List<CityOption> =
        cities
            .distinctBy { it.id }
            .sortedWith(compareByDescending<CityOption> { it.isDefault }.thenBy { it.name })
}
