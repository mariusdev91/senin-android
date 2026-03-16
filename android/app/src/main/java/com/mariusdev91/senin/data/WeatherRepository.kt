package com.mariusdev91.senin.data

import com.mariusdev91.senin.model.CityOption
import com.mariusdev91.senin.model.WeatherOverview

interface WeatherRepository {
    fun defaultCity(): CityOption
    fun favoriteCities(): List<CityOption>
    fun searchCities(query: String): List<CityOption>
    fun weatherFor(city: CityOption): WeatherOverview
}
