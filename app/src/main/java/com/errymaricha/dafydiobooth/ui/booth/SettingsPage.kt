package com.errymaricha.dafydiobooth.ui.booth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.net.NetworkInterface
import java.util.Collections
import com.errymaricha.dafydiobooth.BuildConfig

@Composable
fun SettingsScreen(state: BoothUiState, actions: BoothActions) {
    val context = LocalContext.current
    val storageGranted = hasLegacyStorageWritePermission(context)
    val directLocalIp = if (state.heartbeatLocalIp.isBlank() || state.heartbeatLocalIp == "-") {
        resolveLocalIpNow(context)
    } else {
        state.heartbeatLocalIp
    }
    val displayedIp = state.heartbeatLocalIp.takeIf { it.isNotBlank() && it != "-" } ?: directLocalIp
    val displayedApp = state.heartbeatAppVersion.takeIf { it.isNotBlank() && it != "-" } ?: BuildConfig.VERSION_NAME
    val displayedOs = state.heartbeatOsVersion.takeIf { it.isNotBlank() && it != "-" } ?: "Android ${Build.VERSION.RELEASE}"
    val displayedCapabilities = state.heartbeatCapabilities.takeIf { it.isNotBlank() && it != "-" }
        ?: "camera=true, printer=false, offline_queue=true, local_render=true"
    val displayedHeartbeat = state.heartbeatLastAt.takeIf { it.isNotBlank() && it != "-" } ?: "belum ada heartbeat terkirim"
    val displayedSync = state.heartbeatLastSyncAt.takeIf { it.isNotBlank() && it != "-" } ?: "belum ada sync"
    val heartbeatStatusColor = when (state.heartbeatLastResult.lowercase()) {
        "success" -> Color(0xFF2E7D32)
        "failed" -> Color(0xFFC62828)
        else -> Color(0xFFF9A825)
    }
    val heartbeatStatusLabel = when (state.heartbeatLastResult.lowercase()) {
        "success" -> "Heartbeat status: SUCCESS"
        "failed" -> "Heartbeat status: FAILED"
        else -> "Heartbeat status: PENDING"
    }

    ScreenFrame(title = "Settings", state = state, actions = actions) {
        SectionTitle("Photobooth Station")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (state.isStationConnected) "Status: Connected" else "Status: Not connected",
                    fontWeight = FontWeight.Bold,
                )
                Text("Station: ${state.stationIp.ifBlank { "-" }}")
                Text("Device: ${state.deviceId.ifBlank { "-" }}")
            }
        }
        OutlinedTextField(
            value = state.stationIp,
            onValueChange = actions.updateStationIp,
            label = { Text("Station URL / IP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Kosongkan untuk auto detect station di jaringan lokal.")
        Text("Device fisik: 10.10.116.4:8000. Emulator: 10.0.2.2:8000.")
        OutlinedTextField(
            value = state.deviceId,
            onValueChange = actions.updateDeviceId,
            label = { Text("Device ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.token,
            onValueChange = actions.updateToken,
            label = { Text("API Key / Token") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = actions.retry, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
            Text("Connect Photobooth Station")
        }
        OutlinedButton(
            onClick = actions.disconnectStation,
            enabled = state.isStationConnected && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Logout / Disconnect Station")
        }
        OutlinedButton(
            onClick = { actions.sendHeartbeatNow(displayedIp) },
            enabled = state.isStationConnected && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Send Heartbeat Now")
        }
        OutlinedButton(
            onClick = actions.refreshTemplates,
            enabled = state.isStationConnected && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.availableTemplates.isEmpty()) {
                    "Update Template"
                } else {
                    "Update Template (${state.availableTemplates.size})"
                },
            )
        }
        Text(
            text = if (state.templatesUpdated) {
                "Status template: sudah terupdate (${state.availableTemplates.size} template lokal)."
            } else {
                "Status template: belum terupdate."
            },
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "Storage permission: not required (Android 10+)."
            } else if (storageGranted) {
                "Storage permission: allowed."
            } else {
                "Storage permission: not allowed."
            },
            style = MaterialTheme.typography.bodySmall,
        )
        Text("IP: $displayedIp")
        Text("App: $displayedApp")
        Text("OS: $displayedOs")
        Text("Capabilities: $displayedCapabilities")
        Text(heartbeatStatusLabel, color = heartbeatStatusColor, fontWeight = FontWeight.Bold)
        Text("Last heartbeat: $displayedHeartbeat")
        Text("Last sync: $displayedSync")

        SectionTitle("External Camera")
        ChipRow {
            FilterChip(
                selected = state.cameraSource == CameraSource.AndroidDefault,
                onClick = { actions.setCameraSource(CameraSource.AndroidDefault) },
                label = { Text("Android") },
            )
            FilterChip(
                selected = state.cameraSource == CameraSource.ExternalCanon,
                onClick = { actions.setCameraSource(CameraSource.ExternalCanon) },
                label = { Text("External Canon") },
            )
        }
        Text("Status: ${state.externalCameraStatus.name}")
        ChipRow {
            OutlinedButton(onClick = actions.scanExternalCamera) { Text("Scan") }
            OutlinedButton(onClick = actions.pairExternalCamera) { Text("Pairing") }
            OutlinedButton(onClick = actions.markExternalCameraConnected) { Text("Connected") }
        }
        SwitchRow("Mirror live view", state.mirrorLiveView, actions.setMirrorLiveView)
        SwitchRow("Mirror capture", state.mirrorCapture, actions.setMirrorCapture)

        SectionTitle("Camera Settings")
        Text(
            text = "Detected camera: Back ${if (state.hasBackCamera) "Available" else "Not found"} | Front ${if (state.hasFrontCamera) "Available" else "Not found"}",
            style = MaterialTheme.typography.bodySmall,
        )
        ChipRow {
            ImageQuality.entries.forEach { quality ->
                FilterChip(
                    selected = state.imageQuality == quality,
                    onClick = { actions.setImageQuality(quality) },
                    label = { Text(quality.name) },
                )
            }
        }
        SwitchRow("Back Camera", state.useBackCamera, actions.setUseBackCamera, enabled = state.hasBackCamera)
        SwitchRow("Front Camera", state.useFrontCamera, actions.setUseFrontCamera, enabled = state.hasFrontCamera)
        SwitchRow("Denoise Foto", state.denoisePhoto, actions.setDenoisePhoto)
        ChipRow {
            listOf(0, 3, 5, 10).forEach { seconds ->
                FilterChip(
                    selected = state.countdownSeconds == seconds,
                    onClick = { actions.setCountdownSeconds(seconds) },
                    label = { Text("${seconds}s") },
                )
            }
        }
        SwitchRow("Countdown Audio", state.countdownAudio, actions.setCountdownAudio)
        SwitchRow("Shutter Sound", state.shutterSound, actions.setShutterSound)

        SectionTitle("Printer")
        SwitchRow("Default printing", state.defaultPrinting, actions.setDefaultPrinting)
        SwitchRow("Print use Photobooth Station", state.printUsePhotoboothStation, actions.setPrintUsePhotoboothStation)
    }
}

private fun resolveLocalIpNow(context: Context): String {
    val fromNetworkInterface = runCatching {
        Collections.list(NetworkInterface.getNetworkInterfaces())
            .flatMap { Collections.list(it.inetAddresses) }
            .firstOrNull { !it.isLoopbackAddress && it.hostAddress?.contains(':') == false }
            ?.hostAddress
    }.getOrNull().orEmpty()
    if (fromNetworkInterface.isNotBlank()) return fromNetworkInterface

    val fromConnectivity = runCatching {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return@runCatching ""
        val link = cm.getLinkProperties(network) ?: return@runCatching ""
        link.linkAddresses
            .mapNotNull { it.address?.hostAddress }
            .firstOrNull { !it.contains(':') && it != "127.0.0.1" }
            .orEmpty()
    }.getOrDefault("")
    return fromConnectivity.ifBlank { "-" }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(8.dp))
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ChipRow(content: @Composable () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        content()
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}
