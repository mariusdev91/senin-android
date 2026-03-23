package com.mariusdev91.senin.data

import com.mariusdev91.senin.i18n.AppLanguage
import com.mariusdev91.senin.model.CityOption
import com.mariusdev91.senin.model.SavedLocationPreview
import com.mariusdev91.senin.model.WeatherOverview

interface WeatherRepository {
    fun defaultCity(): CityOption
    fun favoriteCities(): List<CityOption>
    suspend fun searchCities(query: String, language: AppLanguage): List<CityOption>
    suspend fun currentFor(city: CityOption, language: AppLanguage): SavedLocationPreview
    suspend fun weatherFor(city: CityOption, language: AppLanguage): WeatherOverview
}
