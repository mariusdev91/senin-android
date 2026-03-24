package com.mariusdev91.senin

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mariusdev91.senin.i18n.AppStrings
import com.mariusdev91.senin.i18n.LocalAppStrings
import com.mariusdev91.senin.ui.home.HomeScreen
import com.mariusdev91.senin.ui.home.HomeViewModel

@Composable
fun SeninApp() {
    val viewModel: HomeViewModel = viewModel()
    val context = LocalContext.current
    val strings = AppStrings(viewModel.uiState.selectedLanguage)
    val locationPermissions = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
    var hasRequestedLocationPermission by rememberSaveable { mutableStateOf(false) }
    var pendingManualLocationRequest by rememberSaveable { mutableStateOf(false) }
    val hasLocationPermission = {
        locationPermissions.any { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    val isLocationGranted = hasLocationPermission()
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        val isGranted = hasLocationPermission()
        viewModel.onLocationPermissionUpdated(isGranted)
        if (pendingManualLocationRequest) {
            pendingManualLocationRequest = false
            if (isGranted) {
                viewModel.onUseCurrentLocationRequested()
            }
        }
    }

    LaunchedEffect(isLocationGranted) {
        viewModel.onLocationPermissionUpdated(isLocationGranted)
    }

    LaunchedEffect(Unit) {
        if (!isLocationGranted && !hasRequestedLocationPermission) {
            hasRequestedLocationPermission = true
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    CompositionLocalProvider(LocalAppStrings provides strings) {
        HomeScreen(
            uiState = viewModel.uiState,
            onQueryChange = viewModel::onQueryChange,
            onCitySelected = viewModel::onCitySelected,
            onFavoriteToggle = viewModel::onFavoriteToggle,
            onLanguageSelected = viewModel::onLanguageSelected,
            onUseCurrentLocation = {
                if (hasLocationPermission()) {
                    viewModel.onUseCurrentLocationRequested()
                } else {
                    pendingManualLocationRequest = true
                    locationPermissionLauncher.launch(locationPermissions)
                }
            },
            onRetry = viewModel::onRetry,
        )
    }
}
