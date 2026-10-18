package com.omni.ai.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.omni.ai.ChatViewModel
import com.omni.ai.ui.screens.HomeScreen
import com.omni.ai.ui.screens.SettingsScreen

enum class OmniScreen { HOME, SETTINGS }

@Composable
fun OmniAppRoot(vm: ChatViewModel = viewModel()) {
    var screen by rememberSaveable { mutableStateOf(OmniScreen.HOME) }

    BackHandler(enabled = screen != OmniScreen.HOME) {
        screen = OmniScreen.HOME
    }

    when (screen) {
        OmniScreen.HOME -> HomeScreen(
            vm = vm,
            onOpenSettings = { screen = OmniScreen.SETTINGS },
        )
        OmniScreen.SETTINGS -> SettingsScreen(
            vm = vm,
            onBack = { screen = OmniScreen.HOME },
        )
    }
}
