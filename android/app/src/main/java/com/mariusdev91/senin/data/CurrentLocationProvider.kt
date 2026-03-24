package com.mariusdev91.senin.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mariusdev91.senin.i18n.AppLanguage
import com.mariusdev91.senin.i18n.AppStrings
import com.mariusdev91.senin.model.CityOption
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class CurrentLocationProvider(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

    @SuppressLint("MissingPermission")
    suspend fun resolveCurrentCity(language: AppLanguage): CityOption? {
        val location = currentLocation() ?: singleFreshLocation() ?: lastKnownLocation() ?: return null
        return reverseGeocodedCity(location, language)
    }

    @SuppressLint("MissingPermission")
    private suspend fun currentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        runCatching {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(it)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }.getOrElse {
            if (continuation.isActive) continuation.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun lastKnownLocation(): Location? = suspendCancellableCoroutine { continuation ->
        runCatching {
            fusedLocationClient.lastLocation
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(it)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }.getOrElse {
            if (continuation.isActive) continuation.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun singleFreshLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1_000L)
            .setWaitForAccurateLocation(false)
            .setMaxUpdates(1)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                if (continuation.isActive) {
                    continuation.resume(result.lastLocation)
                }
            }
        }

        continuation.invokeOnCancellation {
            fusedLocationClient.removeLocationUpdates(callback)
        }

        runCatching {
            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
                .addOnFailureListener {
                    fusedLocationClient.removeLocationUpdates(callback)
                    if (continuation.isActive) continuation.resume(null)
                }
        }.getOrElse {
            fusedLocationClient.removeLocationUpdates(callback)
            if (continuation.isActive) continuation.resume(null)
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun reverseGeocodedCity(
        location: Location,
        language: AppLanguage,
    ): CityOption = withContext(Dispatchers.IO) {
        val strings = AppStrings(language)
        if (!Geocoder.isPresent()) {
            return@withContext fallbackCity(location, strings)
        }

        val locale = Locale.forLanguageTag(language.code)
        val geocoder = Geocoder(appContext, locale)
        val address = runCatching { geocoder.getFromLocation(location.latitude, location.longitude, 1) }
            .getOrNull()
            ?.firstOrNull()

        if (address == null) {
            return@withContext fallbackCity(location, strings)
        }

        val cityName = listOf(
            address.locality,
            address.subAdminArea,
            address.adminArea,
        ).firstOrNull { !it.isNullOrBlank() } ?: strings.currentLocationLabel

        val region = listOf(
            address.subAdminArea,
            address.adminArea,
            address.countryName,
        ).firstOrNull { !it.isNullOrBlank() }.orEmpty()

        val country = address.countryName.orEmpty()
        val countryCode = address.countryCode.orEmpty()
        val slug = cityName
            .lowercase(locale)
            .replace(Regex("[^\\p{L}\\p{N}]+"), "-")
            .trim('-')
            .ifBlank { "current-location" }

        CityOption(
            id = "gps-$slug-${countryCode.lowercase(locale)}",
            name = cityName,
            region = region,
            country = country,
            countryCode = countryCode,
            latitude = location.latitude,
            longitude = location.longitude,
        )
    }

    private fun fallbackCity(location: Location, strings: AppStrings): CityOption {
        return CityOption(
            id = "gps-current-location",
            name = strings.currentLocationLabel,
            region = strings.gpsLocationLabel,
            country = "",
            countryCode = "",
            latitude = location.latitude,
            longitude = location.longitude,
        )
    }
}
