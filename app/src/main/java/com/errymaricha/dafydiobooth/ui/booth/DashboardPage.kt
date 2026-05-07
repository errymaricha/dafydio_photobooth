package com.errymaricha.dafydiobooth.ui.booth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Dashboard", state = state, actions = actions) {
        DashboardContent(state = state, actions = actions)
    }
}

@Composable
fun DashboardContent(state: BoothUiState, actions: BoothActions) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isTablet = maxWidth >= 840.dp
        if (isTablet) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardActions(state = state, actions = actions, modifier = Modifier.weight(1.1f))
                DashboardStatusPanel(state = state, modifier = Modifier.weight(0.9f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardStatusPanel(state = state)
                DashboardActions(state = state, actions = actions)
            }
        }
    }
}
