package com.mariusdev91.senin.data

import android.content.Context
import com.mariusdev91.senin.model.CityOption

class SelectedCityStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): CityOption? {
        val id = prefs.getString(KEY_ID, null) ?: return null
        val name = prefs.getString(KEY_NAME, null) ?: return null
        val region = prefs.getString(KEY_REGION, null) ?: return null
        val country = prefs.getString(KEY_COUNTRY, null) ?: return null
        val countryCode = prefs.getString(KEY_COUNTRY_CODE, null) ?: return null
        val latitude = prefs.getString(KEY_LATITUDE, null)?.toDoubleOrNull() ?: return null
        val longitude = prefs.getString(KEY_LONGITUDE, null)?.toDoubleOrNull() ?: return null

        return CityOption(
            id = id,
            name = name,
            region = region,
            country = country,
            countryCode = countryCode,
            latitude = latitude,
            longitude = longitude,
            isDefault = id == DEFAULT_CITY_ID,
        )
    }

    fun save(city: CityOption) {
        prefs.edit()
            .putString(KEY_ID, city.id)
            .putString(KEY_NAME, city.name)
            .putString(KEY_REGION, city.region)
            .putString(KEY_COUNTRY, city.country)
            .putString(KEY_COUNTRY_CODE, city.countryCode)
            .putString(KEY_LATITUDE, city.latitude.toString())
            .putString(KEY_LONGITUDE, city.longitude.toString())
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "senin_selected_city"
        private const val DEFAULT_CITY_ID = "oradea"
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_REGION = "region"
        private const val KEY_COUNTRY = "country"
        private const val KEY_COUNTRY_CODE = "country_code"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }
}
