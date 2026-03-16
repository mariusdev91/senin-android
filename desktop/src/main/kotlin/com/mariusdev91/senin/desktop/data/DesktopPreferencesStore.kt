package com.mariusdev91.senin.desktop.data

import com.mariusdev91.senin.desktop.model.CityOption
import java.nio.file.Files
import java.nio.file.Path
import org.json.JSONArray
import org.json.JSONObject

class DesktopPreferencesStore {
    private val appDirectory: Path = Path.of(System.getProperty("user.home"), ".senin")
    private val stateFile: Path = appDirectory.resolve("desktop-state.json")

    fun hasStoredFavorites(): Boolean = readState()?.has(KEY_FAVORITES) == true

    fun loadSelectedCity(): CityOption? = readState()
        ?.optJSONObject(KEY_SELECTED_CITY)
        ?.toCityOption()

    fun saveSelectedCity(city: CityOption) {
        val state = readState() ?: JSONObject()
        state.put(KEY_SELECTED_CITY, city.toJson())
        writeState(state)
    }

    fun loadFavorites(): List<CityOption> {
        val favorites = readState()?.optJSONArray(KEY_FAVORITES) ?: return emptyList()
        return buildList {
            for (index in 0 until favorites.length()) {
                favorites.optJSONObject(index)?.toCityOption()?.let(::add)
            }
        }
    }

    fun saveFavorites(cities: List<CityOption>) {
        val state = readState() ?: JSONObject()
        val favorites = JSONArray()
        cities.forEach { city -> favorites.put(city.toJson()) }
        state.put(KEY_FAVORITES, favorites)
        writeState(state)
    }

    private fun readState(): JSONObject? = runCatching {
        if (!Files.exists(stateFile)) return null
        JSONObject(Files.readString(stateFile))
    }.getOrNull()

    private fun writeState(state: JSONObject) {
        Files.createDirectories(appDirectory)
        Files.writeString(stateFile, state.toString(2))
    }

    private fun JSONObject.toCityOption(): CityOption? = runCatching {
        CityOption(
            id = optString("id"),
            name = optString("name"),
            region = optString("region"),
            country = optString("country"),
            countryCode = optString("countryCode"),
            latitude = optDouble("latitude"),
            longitude = optDouble("longitude"),
            isDefault = optBoolean("isDefault"),
        )
    }.getOrNull()

    private fun CityOption.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("region", region)
        put("country", country)
        put("countryCode", countryCode)
        put("latitude", latitude)
        put("longitude", longitude)
        put("isDefault", isDefault)
    }

    private companion object {
        private const val KEY_SELECTED_CITY = "selectedCity"
        private const val KEY_FAVORITES = "favoriteCities"
    }
}
