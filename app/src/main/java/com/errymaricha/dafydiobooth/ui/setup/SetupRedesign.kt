package com.errymaricha.dafydiobooth.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.net.NetworkInterface
import java.util.Collections
import com.errymaricha.dafydiobooth.BuildConfig

import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.BoothUiState
import com.errymaricha.dafydiobooth.ui.booth.CameraSource
import com.errymaricha.dafydiobooth.ui.booth.ExternalCameraStatus
import com.errymaricha.dafydiobooth.ui.booth.ImageQuality
import com.errymaricha.dafydiobooth.ui.booth.ScreenFrame
import com.errymaricha.dafydiobooth.ui.booth.hasLegacyStorageWritePermission
import com.errymaricha.dafydiobooth.ui.booth.preview.PreviewStateProvider
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme
import coil3.compose.AsyncImage
import java.io.File

private object SetupUiTokens {
    val primary = Color(0xFF5B67FF)
    val pink = Color(0xFFFF6B9D)
    val success = Color(0xFF4ADE80)
    val warning = Color(0xFFFDBA74)
    val ink = Color(0xFF1E2144)
    val inkSoft = Color(0xFF6E7694)
    val glass = Color(0xF7FFFFFF)
    val softSurface = Color(0xFFF7F8FF)
    val border = Color(0x225B67FF)
}

@Composable
fun SetupPageRedesign(
    state: BoothUiState,
    actions: BoothActions,
) {
    SetupScreen(state = state, actions = actions)
}

@Composable
fun SetupScreen(state: BoothUiState, actions: BoothActions) {
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
    SetupHeroCard(
        title = "Booth Setup",
        subtitle = "Atur koneksi station, kamera, dan printer dalam satu tempat.",
        badge = if (state.isStationReachable) "CONNECTED" else "OFFLINE",
        accent = if (state.isStationReachable) SetupUiTokens.success else SetupUiTokens.warning,
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isTablet = maxWidth >= 900.dp
        if (isTablet) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SetupSummaryRail(
                    stationStatus = if (state.isStationReachable) "Connected" else "Offline",
                    cameraSource = if (state.cameraSource == CameraSource.ExternalCanon) "External Canon" else "Android",
                    printerMode = if (state.printUsePhotoboothStation) "Via Station" else "Android Printer",
                    templateStatus = if (state.templatesUpdated) {
                        "${state.availableTemplates.size} template synced"
                    } else {
                        "Template not synced"
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SetupStationSection(
                            state = state,
                            actions = actions,
                            storageGranted = storageGranted,
                            displayedIp = displayedIp,
                            displayedApp = displayedApp,
                            displayedOs = displayedOs,
                            displayedCapabilities = displayedCapabilities,
                            displayedHeartbeat = displayedHeartbeat,
                            displayedSync = displayedSync,
                            heartbeatStatusColor = heartbeatStatusColor,
                            heartbeatStatusLabel = heartbeatStatusLabel,
                        )
                        SetupPrinterSection(state = state, actions = actions)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SetupCameraSection(state = state, actions = actions)
                        SetupExternalCameraSection(state = state, actions = actions)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SetupSummaryRail(
                    stationStatus = if (state.isStationReachable) "Connected" else "Offline",
                    cameraSource = if (state.cameraSource == CameraSource.ExternalCanon) "External Canon" else "Android",
                    printerMode = if (state.printUsePhotoboothStation) "Via Station" else "Android Printer",
                    templateStatus = if (state.templatesUpdated) {
                        "${state.availableTemplates.size} template synced"
                    } else {
                        "Template not synced"
                    },
                )
                SetupStationSection(
                    state = state,
                    actions = actions,
                    storageGranted = storageGranted,
                    displayedIp = displayedIp,
                    displayedApp = displayedApp,
                    displayedOs = displayedOs,
                    displayedCapabilities = displayedCapabilities,
                    displayedHeartbeat = displayedHeartbeat,
                    displayedSync = displayedSync,
                    heartbeatStatusColor = heartbeatStatusColor,
                    heartbeatStatusLabel = heartbeatStatusLabel,
                )
                SetupExternalCameraSection(state = state, actions = actions)
                SetupCameraSection(state = state, actions = actions)
                SetupPrinterSection(state = state, actions = actions)
            }
        }
    }
}

}


@Composable
private fun SetupSummaryRail(
    stationStatus: String,
    cameraSource: String,
    printerMode: String,
    templateStatus: String,
) {
    val items = listOf(
        Triple("Station", stationStatus, if (stationStatus == "Connected") SetupUiTokens.success else SetupUiTokens.warning),
        Triple("Camera", cameraSource, SetupUiTokens.primary),
        Triple("Printer", printerMode, SetupUiTokens.pink),
        Triple("Templates", templateStatus, SetupUiTokens.primary),
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = maxWidth < 680.dp
        val cardWidth = (maxWidth - 10.dp) / 2f
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items.forEach { item ->
                Surface(
                    modifier = if (compact) Modifier.fillMaxWidth() else Modifier.width(cardWidth),
                    shape = RoundedCornerShape(20.dp),
                    color = SetupUiTokens.glass,
                    shadowElevation = 6.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .background(Brush.verticalGradient(listOf(Color.White, SetupUiTokens.softSurface)))
                            .border(1.dp, SetupUiTokens.border, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(item.first, style = MaterialTheme.typography.labelSmall, color = SetupUiTokens.inkSoft)
                        Text(item.second, style = MaterialTheme.typography.bodyMedium, color = SetupUiTokens.ink, fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(999.dp), color = item.third.copy(alpha = 0.14f)) {
                            Text(
                                text = "Live",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = item.third,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupStationSection(
    state: BoothUiState,
    actions: BoothActions,
    storageGranted: Boolean,
    displayedIp: String,
    displayedApp: String,
    displayedOs: String,
    displayedCapabilities: String,
    displayedHeartbeat: String,
    displayedSync: String,
    heartbeatStatusColor: Color,
    heartbeatStatusLabel: String,
) {
    SetupSectionCard("Photobooth Station") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = SetupUiTokens.softSurface,
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, SetupUiTokens.border, RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (state.isStationReachable) "Status: Connected" else "Status: Not connected",
                    fontWeight = FontWeight.Bold,
                    color = SetupUiTokens.ink,
                )
                Text("Station: ${state.stationIp.ifBlank { "-" }}", color = SetupUiTokens.inkSoft)
                Text("Device: ${state.deviceId.ifBlank { "-" }}", color = SetupUiTokens.inkSoft)
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
        OutlinedTextField(
            value = state.kioskExitCode,
            onValueChange = actions.updateKioskExitCode,
            label = { Text("Kode Keluar Kiosk") },
            placeholder = { Text("Contoh: 123456") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Dipakai untuk keluar dari mode event fullscreen dengan tap logo 5x.",
            style = MaterialTheme.typography.bodySmall,
            color = SetupUiTokens.inkSoft,
        )
        SetupPrimaryButton(text = "Connect Photobooth Station", onClick = actions.retry, enabled = !state.isLoading)
        SetupSecondaryButton(text = "Logout / Disconnect Station", onClick = actions.disconnectStation, enabled = state.isStationConnected && !state.isLoading)
        SetupSecondaryButton(text = "Send Heartbeat Now", onClick = { actions.sendHeartbeatNow(displayedIp) }, enabled = state.isStationConnected && !state.isLoading)
        SetupSecondaryButton(
            text = if (state.availableTemplates.isEmpty()) "Update Template" else "Update Template (${state.availableTemplates.size})",
            onClick = actions.refreshTemplates,
            enabled = state.isStationReachable && !state.isLoading,
        )
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
        if (!state.isStationReachable && (!state.errorMessage.isNullOrBlank() || state.heartbeatLastResult.equals("failed", ignoreCase = true))) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SetupUiTokens.warning.copy(alpha = 0.14f),
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, SetupUiTokens.warning.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Station connection issue",
                        fontWeight = FontWeight.Bold,
                        color = SetupUiTokens.ink,
                    )
                    Text(
                        text = state.errorMessage ?: "Heartbeat terakhir gagal. Station mungkin offline atau tidak bisa dijangkau.",
                        color = SetupUiTokens.inkSoft,
                    )
                    Text(
                        text = "App tetap bisa dipakai lewat Quick Booth selama station belum terhubung.",
                        color = SetupUiTokens.ink,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        EventMetaRow("IP" to displayedIp, "App" to displayedApp, "OS" to displayedOs)
        Text("Capabilities: $displayedCapabilities")
        Text(heartbeatStatusLabel, color = heartbeatStatusColor, fontWeight = FontWeight.Bold)
        Text("Last heartbeat: $displayedHeartbeat")
        Text("Last sync: $displayedSync")
    }
}

@Composable
private fun SetupExternalCameraSection(state: BoothUiState, actions: BoothActions) {
    SetupSectionCard("External Camera") {
        SetupSubSectionCard("Connection") {
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
            EventMetaRow(
                "Status" to externalCameraStatusLabel(state.externalCameraStatus),
                "Camera" to state.externalCameraType,
                "Source" to if (state.cameraSource == CameraSource.ExternalCanon) "USB Canon" else "Android",
            )
            ChipRow {
                SetupSecondaryButton(text = "Scan", onClick = actions.scanExternalCamera, fillWidth = false)
                SetupSecondaryButton(text = "Pairing", onClick = actions.pairExternalCamera, fillWidth = false)
                SetupPrimaryButton(text = "Connect", onClick = actions.markExternalCameraConnected, fillWidth = false)
            }
            Text(
                "Pastikan kabel USB, permission, dan kamera dalam mode yang benar sebelum connect.",
                style = MaterialTheme.typography.bodySmall,
                color = SetupUiTokens.inkSoft,
            )
        }
        SetupSubSectionCard("Live Preview") {
            SetupCameraPreviewCard(
                status = state.externalCameraStatus,
                cameraType = state.externalCameraType.ifBlank { "-" },
                previewPath = state.externalPreviewPath,
                previewBytes = state.externalPreviewBytes,
            )
            Text("Preview FPS: ${state.externalPreviewFps}")
            ChipRow {
                listOf(15).forEach { fps ->
                    FilterChip(
                        selected = state.externalPreviewFps == fps,
                        onClick = { actions.setExternalPreviewFps(fps) },
                        label = { Text("${fps} FPS") },
                    )
                }
            }
            SwitchRow("Mirror live view", state.mirrorLiveView, actions.setMirrorLiveView)
        }
        SetupSubSectionCard("Capture Output") {
            SwitchRow("Mirror capture", state.mirrorCapture, actions.setMirrorCapture)
        }
    }
}

@Composable
private fun SetupCameraSection(state: BoothUiState, actions: BoothActions) {
    SetupSectionCard("Camera Settings") {
        SetupSubSectionCard("Device Camera") {
            Text(
                text = "Detected camera: Back ${if (state.hasBackCamera) "Available" else "Not found"} | Front ${if (state.hasFrontCamera) "Available" else "Not found"}",
                style = MaterialTheme.typography.bodySmall,
            )
            SwitchRow("Back Camera", state.useBackCamera, actions.setUseBackCamera, enabled = state.hasBackCamera)
            SwitchRow("Front Camera", state.useFrontCamera, actions.setUseFrontCamera, enabled = state.hasFrontCamera)
        }
        SetupSubSectionCard("Quality") {
            ChipRow {
                ImageQuality.entries.forEach { quality ->
                    FilterChip(
                        selected = state.imageQuality == quality,
                        onClick = { actions.setImageQuality(quality) },
                        label = { Text(quality.name) },
                    )
                }
            }
            SwitchRow("Denoise Foto", state.denoisePhoto, actions.setDenoisePhoto)
        }
        SetupSubSectionCard("Countdown & Audio") {
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
        }
    }
}

@Composable
private fun SetupPrinterSection(state: BoothUiState, actions: BoothActions) {
    SetupSectionCard("Printer") {
        val printMode = when {
            !state.defaultPrinting -> "none"
            state.printUsePhotoboothStation -> "station"
            else -> "android"
        }
        Text(
            text = "Pilih mode output print untuk sesi photobooth.",
            style = MaterialTheme.typography.bodySmall,
            color = SetupUiTokens.inkSoft,
        )
        ChipRow {
            FilterChip(
                selected = printMode == "station",
                onClick = {
                    actions.setDefaultPrinting(true)
                    actions.setPrintUsePhotoboothStation(true)
                },
                label = { Text("Print via Booth Station") },
            )
            FilterChip(
                selected = printMode == "android",
                onClick = {
                    actions.setDefaultPrinting(true)
                    actions.setPrintUsePhotoboothStation(false)
                },
                label = { Text("Sambung Android Printer") },
            )
            FilterChip(
                selected = printMode == "none",
                onClick = {
                    actions.setDefaultPrinting(false)
                    actions.setPrintUsePhotoboothStation(false)
                },
                label = { Text("Tanpa Print") },
            )
        }
        EventMetaRow(
            "Mode" to when (printMode) {
                "station" -> "Booth Station"
                "android" -> "Android Printer"
                else -> "No Print"
            },
            "Default" to if (state.defaultPrinting) "Enabled" else "Disabled",
            "Route" to if (state.defaultPrinting && state.printUsePhotoboothStation) "Station" else if (state.defaultPrinting) "Android" else "-",
        )
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
private fun SetupHeroCard(title: String, subtitle: String, badge: String, accent: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 10.dp,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 680.dp
            Column(
                modifier = Modifier
                    .background(Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFFFF3F9))))
                    .border(1.dp, SetupUiTokens.border, RoundedCornerShape(28.dp))
                    .padding(if (compact) 14.dp else 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(title, style = MaterialTheme.typography.headlineSmall, color = SetupUiTokens.ink, fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.14f)) {
                            Text(badge, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(title, style = MaterialTheme.typography.headlineSmall, color = SetupUiTokens.ink, fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.14f)) {
                            Text(badge, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = SetupUiTokens.inkSoft)
            }
        }
    }
}

@Composable
private fun SetupSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SetupUiTokens.glass,
        shadowElevation = 10.dp,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 680.dp
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.White, SetupUiTokens.softSurface),
                        ),
                    )
                    .border(1.dp, SetupUiTokens.border, RoundedCornerShape(24.dp))
                    .padding(if (compact) 12.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = SetupUiTokens.ink, fontWeight = FontWeight.Bold)
                content()
            }
        }
    }
}

@Composable
private fun SetupSubSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SetupUiTokens.softSurface,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 680.dp
            Column(
                modifier = Modifier
                    .border(1.dp, SetupUiTokens.border, RoundedCornerShape(20.dp))
                    .padding(if (compact) 10.dp else 12.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = SetupUiTokens.ink,
                    fontWeight = FontWeight.Bold,
                )
                content()
            }
        }
    }
}

@Composable
private fun SetupCameraPreviewCard(
    status: ExternalCameraStatus,
    cameraType: String,
    previewPath: String?,
    previewBytes: ByteArray?,
) {
    val statusAccent = externalCameraStatusAccent(status)
    val previewBitmap = remember(previewBytes) {
        previewBytes?.let { bytes ->
            runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() }.getOrNull()
        }
    }
    val previewFile = previewPath?.takeIf { it.isNotBlank() }?.let { File(it) }?.takeIf { it.exists() }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SetupUiTokens.primary.copy(alpha = 0.22f),
                            SetupUiTokens.pink.copy(alpha = 0.14f),
                            Color.White,
                        ),
                    ),
                )
                .border(1.dp, SetupUiTokens.border, RoundedCornerShape(18.dp))
                .padding(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = statusAccent.copy(alpha = 0.14f),
            ) {
                Text(
                    text = externalCameraStatusLabel(status),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = statusAccent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (previewBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = previewBitmap,
                    contentDescription = "External live preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.Center)
                        .border(1.dp, SetupUiTokens.border, RoundedCornerShape(14.dp)),
                )
            } else if (previewFile != null) {
                AsyncImage(
                    model = previewFile,
                    contentDescription = "External live preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.Center),
                )
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "External Live Preview",
                        style = MaterialTheme.typography.titleSmall,
                        color = SetupUiTokens.ink,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = cameraType,
                        style = MaterialTheme.typography.bodySmall,
                        color = SetupUiTokens.inkSoft,
                    )
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd),
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.8f),
            ) {
                Text(
                    text = when {
                        previewBitmap != null -> "Live Bytes"
                        previewFile != null -> "Preview File"
                        else -> "Preview Frame"
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = SetupUiTokens.ink,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomStart),
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.82f),
            ) {
                Text(
                    text = when {
                        previewBytes != null -> "${previewBytes.size} bytes | memory"
                        previewFile != null -> "file | ${previewFile.name}"
                        else -> "no live frame"
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = SetupUiTokens.inkSoft,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun externalCameraStatusAccent(status: ExternalCameraStatus): Color = when (status) {
    ExternalCameraStatus.Connected -> SetupUiTokens.success
    ExternalCameraStatus.Pairing -> SetupUiTokens.primary
    ExternalCameraStatus.Scanning -> SetupUiTokens.warning
    ExternalCameraStatus.Disconnected -> SetupUiTokens.inkSoft
}

private fun externalCameraStatusLabel(status: ExternalCameraStatus): String = when (status) {
    ExternalCameraStatus.Connected -> "Connected"
    ExternalCameraStatus.Pairing -> "Pairing"
    ExternalCameraStatus.Scanning -> "Scanning"
    ExternalCameraStatus.Disconnected -> "Disconnected"
}

@Composable
private fun EventMetaRow(first: Pair<String, String>, second: Pair<String, String>, third: Pair<String, String>) {
    val items = listOf(first, second, third)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = maxWidth < 680.dp
        val cardWidth = (maxWidth - 20.dp) / 3f
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items.forEach { item ->
                Surface(
                    modifier = if (compact) Modifier.fillMaxWidth() else Modifier.width(cardWidth),
                    shape = RoundedCornerShape(18.dp),
                    color = SetupUiTokens.softSurface,
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, SetupUiTokens.border, RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(item.first, style = MaterialTheme.typography.labelSmall, color = SetupUiTokens.inkSoft)
                        Text(item.second, style = MaterialTheme.typography.bodyMedium, color = SetupUiTokens.ink, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
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
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun SetupPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    fillWidth: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .shadow(14.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(),
    ) {
        Box(
            modifier = Modifier
                .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .background(
                    Brush.horizontalGradient(
                        colors = if (enabled) {
                            listOf(SetupUiTokens.primary, SetupUiTokens.pink)
                        } else {
                            listOf(SetupUiTokens.inkSoft.copy(alpha = 0.35f), SetupUiTokens.inkSoft.copy(alpha = 0.2f))
                        },
                    ),
                    RoundedCornerShape(24.dp),
                )
                .padding(horizontal = 18.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SetupSecondaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    fillWidth: Boolean = true,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.then(if (fillWidth) Modifier.fillMaxWidth() else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = if (enabled) 0.88f else 0.5f),
            contentColor = SetupUiTokens.ink,
            disabledContainerColor = Color.White.copy(alpha = 0.5f),
            disabledContentColor = SetupUiTokens.inkSoft,
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(name = "Setup Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SetupMobilePreview() {
    DafydioBoothTheme {
        SetupScreen(
            state = PreviewStateProvider.settingsBase,
            actions = BoothActions(),
        )
    }
}

@Preview(name = "Setup Tablet Disconnected", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun SetupTabletDisconnectedPreview() {
    DafydioBoothTheme {
        SetupScreen(
            state = PreviewStateProvider.settingsBase.copy(
                isStationConnected = false,
                stationIp = "",
                deviceId = "PB-DEVICE-01",
                heartbeatLastResult = "pending",
                templatesUpdated = false,
                availableTemplates = emptyList(),
            ),
            actions = BoothActions(),
        )
    }
}

@Preview(name = "Setup Tablet Connected", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun SetupTabletConnectedPreview() {
    DafydioBoothTheme {
        SetupScreen(
            state = PreviewStateProvider.settingsBase.copy(
                isStationConnected = true,
                stationIp = "10.10.116.4:8000",
                deviceId = "PB-DEVICE-01",
                heartbeatLastResult = "success",
                templatesUpdated = true,
                availableTemplates = listOf("Mirror Pop", "Glow Frame", "Soft Flash"),
            ),
            actions = BoothActions(),
        )
    }
}

@Preview(name = "Setup Mobile External Camera", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SetupMobileExternalCameraPreview() {
    DafydioBoothTheme {
        SetupScreen(
            state = PreviewStateProvider.settingsBase.copy(
                isStationConnected = true,
                cameraSource = CameraSource.ExternalCanon,
                externalCameraStatus = ExternalCameraStatus.Pairing,
                externalCameraType = "Canon EOS",
                externalPreviewBytes = ByteArray(2048),
                externalPreviewFps = 15,
            ),
            actions = BoothActions(),
        )
    }
}

@Preview(name = "External Camera Section Mobile", widthDp = 390, heightDp = 760, showBackground = true)
@Composable
private fun ExternalCameraSectionMobilePreview() {
    DafydioBoothTheme {
        Column(
            modifier = Modifier
                .background(SetupUiTokens.softSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SetupExternalCameraSection(
                state = PreviewStateProvider.settingsBase.copy(
                    cameraSource = CameraSource.ExternalCanon,
                    externalCameraStatus = ExternalCameraStatus.Pairing,
                    externalCameraType = "Canon EOS R50",
                    externalPreviewBytes = ByteArray(4096),
                    externalPreviewFps = 15,
                    mirrorLiveView = true,
                    mirrorCapture = false,
                ),
                actions = BoothActions(),
            )
        }
    }
}

@Preview(name = "External Camera Section Tablet", widthDp = 900, heightDp = 760, showBackground = true)
@Composable
private fun ExternalCameraSectionTabletPreview() {
    DafydioBoothTheme {
        Column(
            modifier = Modifier
                .background(SetupUiTokens.softSurface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SetupExternalCameraSection(
                state = PreviewStateProvider.settingsBase.copy(
                    cameraSource = CameraSource.ExternalCanon,
                    externalCameraStatus = ExternalCameraStatus.Connected,
                    externalCameraType = "Canon EOS 200D",
                    externalPreviewBytes = ByteArray(8192),
                    externalPreviewFps = 15,
                    mirrorLiveView = true,
                    mirrorCapture = true,
                ),
                actions = BoothActions(),
            )
        }
    }
}

@Preview(name = "External Camera Section Disconnected", widthDp = 900, heightDp = 760, showBackground = true)
@Composable
private fun ExternalCameraSectionDisconnectedPreview() {
    DafydioBoothTheme {
        Column(
            modifier = Modifier
                .background(SetupUiTokens.softSurface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SetupExternalCameraSection(
                state = PreviewStateProvider.settingsBase.copy(
                    cameraSource = CameraSource.ExternalCanon,
                    externalCameraStatus = ExternalCameraStatus.Disconnected,
                    externalCameraType = "-",
                    externalPreviewBytes = null,
                    externalPreviewPath = null,
                    externalPreviewFps = 15,
                    mirrorLiveView = false,
                    mirrorCapture = false,
                ),
                actions = BoothActions(),
            )
        }
    }
}

@Preview(name = "External Camera Section Scanning", widthDp = 900, heightDp = 760, showBackground = true)
@Composable
private fun ExternalCameraSectionScanningPreview() {
    DafydioBoothTheme {
        Column(
            modifier = Modifier
                .background(SetupUiTokens.softSurface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SetupExternalCameraSection(
                state = PreviewStateProvider.settingsBase.copy(
                    cameraSource = CameraSource.ExternalCanon,
                    externalCameraStatus = ExternalCameraStatus.Scanning,
                    externalCameraType = "Detecting...",
                    externalPreviewBytes = null,
                    externalPreviewPath = null,
                    externalPreviewFps = 15,
                    mirrorLiveView = false,
                    mirrorCapture = false,
                ),
                actions = BoothActions(),
            )
        }
    }
}

@Preview(name = "Printer Section Tablet", widthDp = 900, heightDp = 420, showBackground = true)
@Composable
private fun PrinterSectionTabletPreview() {
    DafydioBoothTheme {
        Column(
            modifier = Modifier
                .background(SetupUiTokens.softSurface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SetupPrinterSection(
                state = PreviewStateProvider.settingsBase.copy(
                    defaultPrinting = true,
                    printUsePhotoboothStation = true,
                ),
                actions = BoothActions(),
            )
        }
    }
}

@Preview(name = "Printer Section Mobile", widthDp = 390, heightDp = 420, showBackground = true)
@Composable
private fun PrinterSectionMobilePreview() {
    DafydioBoothTheme {
        Column(
            modifier = Modifier
                .background(SetupUiTokens.softSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SetupPrinterSection(
                state = PreviewStateProvider.settingsBase.copy(
                    defaultPrinting = false,
                    printUsePhotoboothStation = false,
                ),
                actions = BoothActions(),
            )
        }
    }
}

@Preview(name = "Setup Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun SetupTabletPreview() {
    DafydioBoothTheme {
        SetupScreen(
            state = PreviewStateProvider.settingsBase,
            actions = BoothActions(),
        )
    }
}
