package com.mariusdev91.senin.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mariusdev91.senin.data.FakeWeatherRepository
import com.mariusdev91.senin.data.WeatherRepository
import com.mariusdev91.senin.model.CityOption
import com.mariusdev91.senin.model.WeatherOverview

data class HomeUiState(
    val selectedCity: CityOption,
    val favoriteCities: List<CityOption>,
    val suggestions: List<CityOption>,
    val weather: WeatherOverview,
    val query: String = "",
)

class HomeViewModel : ViewModel() {
    private val repository: WeatherRepository = FakeWeatherRepository()
    private val defaultCity = repository.defaultCity()
    private val favorites = repository.favoriteCities()

    var uiState by mutableStateOf(
        HomeUiState(
            selectedCity = defaultCity,
            favoriteCities = favorites,
            suggestions = repository.searchCities(""),
            weather = repository.weatherFor(defaultCity),
        ),
    )
        private set

    fun onQueryChange(query: String) {
        uiState = uiState.copy(
            query = query,
            suggestions = repository.searchCities(query),
        )
    }

    fun onCitySelected(city: CityOption) {
        uiState = uiState.copy(
            selectedCity = city,
            query = "",
            suggestions = repository.searchCities(""),
            weather = repository.weatherFor(city),
        )
    }
}
