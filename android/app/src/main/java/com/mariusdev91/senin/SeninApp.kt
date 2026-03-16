package com.mariusdev91.senin

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mariusdev91.senin.ui.home.HomeScreen
import com.mariusdev91.senin.ui.home.HomeViewModel

@Composable
fun SeninApp() {
    val viewModel: HomeViewModel = viewModel()

    HomeScreen(
        uiState = viewModel.uiState,
        onQueryChange = viewModel::onQueryChange,
        onCitySelected = viewModel::onCitySelected,
    )
}
