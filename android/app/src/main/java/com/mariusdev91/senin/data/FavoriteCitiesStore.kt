package com.mariusdev91.senin.data

import android.content.Context
import com.mariusdev91.senin.model.CityOption
import org.json.JSONArray
import org.json.JSONObject

class FavoriteCitiesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasStoredFavorites(): Boolean = prefs.contains(KEY_FAVORITES)

    fun load(): List<CityOption> {
        val raw = prefs.getString(KEY_FAVORITES, null).orEmpty()
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index)?.toCityOptionOrNull() ?: continue
                    add(item)
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(cities: List<CityOption>) {
        val array = JSONArray()
        cities.forEach { city ->
            array.put(
                JSONObject()
                    .put("id", city.id)
                    .put("name", city.name)
                    .put("region", city.region)
                    .put("country", city.country)
                    .put("countryCode", city.countryCode)
                    .put("latitude", city.latitude)
                    .put("longitude", city.longitude)
                    .put("isDefault", city.isDefault),
            )
        }

        prefs.edit()
            .putString(KEY_FAVORITES, array.toString())
            .apply()
    }

    private fun JSONObject.toCityOptionOrNull(): CityOption? {
        val id = optString("id").ifBlank { return null }
        val name = optString("name").ifBlank { return null }
        val region = optString("region")
        val country = optString("country")
        val countryCode = optString("countryCode")
        val latitude = optDouble("latitude", Double.NaN)
        val longitude = optDouble("longitude", Double.NaN)

        if (!latitude.isFinite() || !longitude.isFinite()) return null

        return CityOption(
            id = id,
            name = name,
            region = region,
            country = country,
            countryCode = countryCode,
            latitude = latitude,
            longitude = longitude,
            isDefault = optBoolean("isDefault", false),
        )
    }

    companion object {
        private const val PREFS_NAME = "senin_favorite_cities"
        private const val KEY_FAVORITES = "favorite_cities"
    }
}
