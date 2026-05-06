package com.errymaricha.dafydiobooth.ui.booth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.isSystemInDarkTheme
import com.errymaricha.dafydiobooth.R
import com.errymaricha.dafydiobooth.ui.theme.BoothSize
import com.errymaricha.dafydiobooth.ui.theme.BoothSpacing
import com.errymaricha.dafydiobooth.ui.theme.DashboardStatusCardDark
import com.errymaricha.dafydiobooth.ui.theme.DashboardStatusCardLight

@Composable
fun DashboardActions(state: BoothUiState, actions: BoothActions, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(BoothSpacing.md), modifier = modifier) {
        Button(onClick = actions.startNowPhoto, modifier = Modifier.fillMaxWidth().height(BoothSize.buttonPrimaryHeight)) {
            Text(stringResource(R.string.dashboard_action_start_now_photo))
        }
        OutlinedButton(onClick = actions.openCustomTemplate, modifier = Modifier.fillMaxWidth().height(BoothSize.buttonSecondaryHeight)) {
            Text(stringResource(R.string.dashboard_action_create_custom_template))
        }
        OutlinedButton(onClick = actions.startNowPhoto, modifier = Modifier.fillMaxWidth().height(BoothSize.buttonSecondaryHeight)) {
            Text(stringResource(R.string.dashboard_action_list_default_template))
        }
        OutlinedButton(onClick = actions.openSettings, modifier = Modifier.fillMaxWidth().height(BoothSize.buttonSecondaryHeight)) {
            Text(stringResource(R.string.dashboard_action_settings))
        }
        if (state.isStationConnected) {
            Button(onClick = actions.openLaunchEvent, modifier = Modifier.fillMaxWidth().height(BoothSize.buttonPrimaryHeight)) {
                Text(stringResource(R.string.dashboard_action_launch_event))
            }
            OutlinedButton(onClick = actions.openSettingEvent, modifier = Modifier.fillMaxWidth().height(BoothSize.buttonSecondaryHeight)) {
                Text(stringResource(R.string.dashboard_action_setting_event))
            }
        }
    }
}

@Composable
fun DashboardStatusPanel(state: BoothUiState, modifier: Modifier = Modifier) {
    val statusCardColor = if (isSystemInDarkTheme()) DashboardStatusCardDark else DashboardStatusCardLight
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = statusCardColor),
    ) {
        Column(modifier = Modifier.padding(BoothSpacing.xl), verticalArrangement = Arrangement.spacedBy(BoothSpacing.sm)) {
            Text(stringResource(R.string.dashboard_station_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            StatusLine(state)
            Text(
                text = if (state.isStationConnected) {
                    stringResource(R.string.dashboard_connected_description)
                } else {
                    stringResource(R.string.dashboard_disconnected_description)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(stringResource(R.string.dashboard_next_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(if (state.isStationConnected) stringResource(R.string.dashboard_connected_next) else stringResource(R.string.dashboard_disconnected_next))
        }
    }
}

@Composable
fun StatusLine(state: BoothUiState) {
    val stationStatus = if (state.isStationConnected) {
        stringResource(R.string.dashboard_station_connected)
    } else {
        stringResource(R.string.dashboard_station_not_connected)
    }
    val fallback = stringResource(R.string.dashboard_empty_placeholder)
    Text(stringResource(R.string.dashboard_station_status, stationStatus))
    Text(stringResource(R.string.dashboard_station_ip, state.stationIp.ifBlank { fallback }))
    Text(stringResource(R.string.dashboard_device_id, state.deviceId.ifBlank { fallback }))
}
