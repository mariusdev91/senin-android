package com.mariusdev91.senin.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mariusdev91.senin.desktop.ui.DesktopApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Senin",
        state = rememberWindowState(width = 1480.dp, height = 920.dp),
    ) {
        DesktopApp()
    }
}
