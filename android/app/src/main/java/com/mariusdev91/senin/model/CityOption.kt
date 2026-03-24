package com.mariusdev91.senin.model

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
        get() {
            val normalizedRegion = region
                .removePrefix("Judetul ")
                .trim()

            return when {
                country.isBlank() -> normalizedRegion.ifBlank { name }
                countryCode.equals("RO", ignoreCase = true) -> normalizedRegion.ifBlank { country }
                normalizedRegion.isBlank() || normalizedRegion.equals(country, ignoreCase = true) -> country
                else -> "$normalizedRegion, $country"
            }
        }
}
