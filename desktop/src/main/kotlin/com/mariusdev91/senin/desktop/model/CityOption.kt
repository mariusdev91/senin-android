package com.mariusdev91.senin.desktop.model

data class CityOption(
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false,
) {
    val subtitle: String
        get() = if (region.isBlank() || region == country) country else "$region, $country"
}
