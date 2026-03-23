package com.mariusdev91.senin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mariusdev91.senin.i18n.AppStrings
import com.mariusdev91.senin.i18n.LocalAppStrings
import com.mariusdev91.senin.ui.home.HomeScreen
import com.mariusdev91.senin.ui.home.HomeViewModel

@Composable
fun SeninApp() {
    val viewModel: HomeViewModel = viewModel()
    val strings = AppStrings(viewModel.uiState.selectedLanguage)

    CompositionLocalProvider(LocalAppStrings provides strings) {
        HomeScreen(
            uiState = viewModel.uiState,
            onQueryChange = viewModel::onQueryChange,
            onCitySelected = viewModel::onCitySelected,
            onFavoriteToggle = viewModel::onFavoriteToggle,
            onLanguageSelected = viewModel::onLanguageSelected,
            onRetry = viewModel::onRetry,
        )
    }
}
