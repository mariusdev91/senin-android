package com.mariusdev91.senin.desktop.data

import com.mariusdev91.senin.desktop.model.CityOption
import com.mariusdev91.senin.desktop.model.WeatherOverview

interface WeatherRepository {
    fun defaultCity(): CityOption
    fun favoriteCities(): List<CityOption>
    suspend fun searchCities(query: String): List<CityOption>
    suspend fun weatherFor(city: CityOption): WeatherOverview
}
