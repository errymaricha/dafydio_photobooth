package com.errymaricha.dafydiobooth.ui.booth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

private enum class BoothRoute(val route: String) {
    Splash("splash"),
    Dashboard("dashboard"),
    TemplatePicker("template-picker"),
    CustomTemplate("custom-template"),
    Camera("camera"),
    CapturePreview("capture-preview"),
    TemplatePreview("template-preview"),
    Finish("finish"),
    Settings("settings"),
    LaunchEvent("launch-event"),
    SettingEvent("setting-event"),
}

@Composable
fun BoothApp(viewModel: BoothViewModel) {
    val state by viewModel.state.collectAsState()
    val navController = rememberNavController()

    LaunchedEffect(state.step) {
        navController.navigate(state.step.toRoute().route) {
            launchSingleTop = true
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            restoreState = true
        }
    }

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BoothRoute.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(BoothRoute.Splash.route) {
                SplashScreen(actions = viewModel)
            }
            composable(BoothRoute.Dashboard.route) {
                DashboardScreen(state = state, actions = viewModel)
            }
            composable(BoothRoute.TemplatePicker.route) {
                TemplatePickerScreen(state = state, actions = viewModel)
            }
            composable(BoothRoute.CustomTemplate.route) {
                CustomTemplateScreen(state = state, actions = viewModel)
            }
            composable(BoothRoute.Camera.route) {
                CameraScreen(state = state, actions = viewModel)
            }
            composable(BoothRoute.CapturePreview.route) {
                CapturePreviewScreen(state = state, actions = viewModel)
            }
            composable(BoothRoute.TemplatePreview.route) {
                TemplatePreviewScreen(state = state, actions = viewModel)
            }
            composable(BoothRoute.Finish.route) {
                FinishScreen(state = state, actions = viewModel)
            }
            composable(BoothRoute.Settings.route) {
                SettingsScreen(state = state, actions = viewModel)
            }
            composable(BoothRoute.LaunchEvent.route) {
                LaunchEventScreen(state = state, actions = viewModel)
            }
            composable(BoothRoute.SettingEvent.route) {
                SettingEventScreen(state = state, actions = viewModel)
            }
        }
    }
}

@Composable
private fun SplashScreen(actions: BoothViewModel) {
    LaunchedEffect(Unit) {
        delay(900)
        actions.continueFromSplash()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Dafydio Booth", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("Photobooth station")
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun DashboardScreen(state: BoothUiState, actions: BoothViewModel) {
    ScreenFrame(title = "Dashboard", state = state, actions = actions) {
        StatusLine(state)
        Button(onClick = actions::startNowPhoto, modifier = Modifier.fillMaxWidth()) {
            Text("Start Now Photo")
        }
        OutlinedButton(onClick = actions::openCustomTemplate, modifier = Modifier.fillMaxWidth()) {
            Text("Create Custom Template")
        }
        OutlinedButton(onClick = actions::startNowPhoto, modifier = Modifier.fillMaxWidth()) {
            Text("List Default Template")
        }
        OutlinedButton(onClick = actions::openSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Settings")
        }
        if (state.isStationConnected) {
            Button(onClick = actions::openLaunchEvent, modifier = Modifier.fillMaxWidth()) {
                Text("Launch Event")
            }
            OutlinedButton(onClick = actions::openSettingEvent, modifier = Modifier.fillMaxWidth()) {
                Text("Setting Event")
            }
        }
        Text(
            text = if (state.isStationConnected) {
                "Data akan direkap ke Photobooth Station."
            } else {
                "Mode lokal: data tidak disimpan ke database."
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LaunchEventScreen(state: BoothUiState, actions: BoothViewModel) {
    ScreenFrame(title = "Launch Event", state = state, actions = actions) {
        Text("Station: ${state.stationIp}")
        Text("Device: ${state.deviceId}")
        Text("Voucher/payment gate akan dimulai di fase berikutnya.")
        Button(onClick = actions::startNowPhoto, modifier = Modifier.fillMaxWidth()) {
            Text("Continue to Template")
        }
    }
}

@Composable
private fun SettingEventScreen(state: BoothUiState, actions: BoothViewModel) {
    ScreenFrame(title = "Setting Event", state = state, actions = actions) {
        Text("Event setting tersambung ke Photobooth Station.")
        Text("Konfigurasi event backend akan ditambahkan setelah contract event tersedia.")
        OutlinedButton(onClick = actions::openDashboard, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
private fun TemplatePickerScreen(state: BoothUiState, actions: BoothViewModel) {
    ScreenFrame(title = "Pilih Template", state = state, actions = actions) {
        actions.defaultTemplates.forEach { template ->
            OutlinedButton(
                onClick = { actions.selectTemplate(template) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(template)
            }
        }
    }
}

@Composable
private fun CustomTemplateScreen(state: BoothUiState, actions: BoothViewModel) {
    ScreenFrame(title = "Create Custom Template", state = state, actions = actions) {
        Text("Template custom sementara dibuat lokal. Editor detail akan dihubungkan ke Photobooth Station.")
        Button(onClick = { actions.saveCustomTemplate("Custom Template") }, modifier = Modifier.fillMaxWidth()) {
            Text("Use Custom Template")
        }
    }
}

@Composable
private fun CameraScreen(state: BoothUiState, actions: BoothViewModel) {
    ScreenFrame(title = "Camera", state = state, actions = actions) {
        CameraSurface(state)
        Text("Template: ${state.selectedTemplate ?: "-"}")
        Text("Camera: ${if (state.cameraSource == CameraSource.AndroidDefault) "Android default" else "External Canon"}")
        Text("Timer: ${state.countdownSeconds}s")
        Button(onClick = actions::capturePhoto, modifier = Modifier.fillMaxWidth()) {
            Text("Capture")
        }
    }
}

@Composable
private fun CapturePreviewScreen(state: BoothUiState, actions: BoothViewModel) {
    ScreenFrame(title = "Preview Capture", state = state, actions = actions) {
        CameraSurface(state)
        Text("Photo: ${state.capturedPhotoName ?: "-"}")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = actions::retakePhoto, modifier = Modifier.weight(1f)) {
                Text("Retake")
            }
            Button(onClick = actions::acceptCapturePreview, modifier = Modifier.weight(1f)) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun TemplatePreviewScreen(state: BoothUiState, actions: BoothViewModel) {
    ScreenFrame(title = "Preview Template", state = state, actions = actions) {
        TemplateSurface(state)
        Text("Template: ${state.selectedTemplate ?: "-"}")
        Text("Photo: ${state.capturedPhotoName ?: "-"}")
        Button(onClick = actions::finishSession, modifier = Modifier.fillMaxWidth()) {
            Text("Finish")
        }
    }
}

@Composable
private fun FinishScreen(state: BoothUiState, actions: BoothViewModel) {
    ScreenFrame(title = "Finish", state = state, actions = actions) {
        TemplateSurface(state)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                Text("Download")
            }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                Text("Print")
            }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                Text("Share")
            }
        }
        Text(
            text = if (state.printUsePhotoboothStation && state.isStationConnected) {
                "Print akan dikirim ke Photobooth Station."
            } else {
                "Print memakai default printing device."
            },
        )
        Button(onClick = actions::newSession, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
private fun SettingsScreen(state: BoothUiState, actions: BoothViewModel) {
    ScreenFrame(title = "Settings", state = state, actions = actions) {
        SectionTitle("Photobooth Station")
        OutlinedTextField(
            value = state.stationIp,
            onValueChange = actions::updateStationIp,
            label = { Text("Station IP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.deviceId,
            onValueChange = actions::updateDeviceId,
            label = { Text("Device ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.token,
            onValueChange = actions::updateToken,
            label = { Text("Token") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = actions::loginDevice, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
            Text("Connect Photobooth Station")
        }

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
            OutlinedButton(onClick = actions::scanExternalCamera) { Text("Scan") }
            OutlinedButton(onClick = actions::pairExternalCamera) { Text("Pairing") }
            OutlinedButton(onClick = actions::markExternalCameraConnected) { Text("Connected") }
        }
        SwitchRow("Mirror live view", state.mirrorLiveView, actions::setMirrorLiveView)
        SwitchRow("Mirror capture", state.mirrorCapture, actions::setMirrorCapture)

        SectionTitle("Camera Settings")
        ChipRow {
            ImageQuality.entries.forEach { quality ->
                FilterChip(
                    selected = state.imageQuality == quality,
                    onClick = { actions.setImageQuality(quality) },
                    label = { Text(quality.name) },
                )
            }
        }
        SwitchRow("Back Camera", state.useBackCamera, actions::setUseBackCamera)
        SwitchRow("Front Camera", state.useFrontCamera, actions::setUseFrontCamera)
        SwitchRow("Denoise Foto", state.denoisePhoto, actions::setDenoisePhoto)
        ChipRow {
            listOf(0, 3, 5, 10).forEach { seconds ->
                FilterChip(
                    selected = state.countdownSeconds == seconds,
                    onClick = { actions.setCountdownSeconds(seconds) },
                    label = { Text("${seconds}s") },
                )
            }
        }
        SwitchRow("Countdown Audio", state.countdownAudio, actions::setCountdownAudio)
        SwitchRow("Shutter Sound", state.shutterSound, actions::setShutterSound)

        SectionTitle("Printer")
        SwitchRow("Default printing", state.defaultPrinting, actions::setDefaultPrinting)
        SwitchRow("Print use Photobooth Station", state.printUsePhotoboothStation, actions::setPrintUsePhotoboothStation)
    }
}

@Composable
private fun ScreenFrame(
    title: String,
    state: BoothUiState,
    actions: BoothViewModel,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (state.step != BoothStep.Dashboard) {
                OutlinedButton(onClick = actions::openDashboard) {
                    Text("Home")
                }
            }
        }
        if (state.isLoading) {
            CircularProgressIndicator()
        }
        state.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = actions::retry, enabled = !state.isLoading) {
                Text("Retry")
            }
        }
        content()
    }
}

@Composable
private fun StatusLine(state: BoothUiState) {
    Text("Station: ${if (state.isStationConnected) "Connected" else "Not connected"}")
    Text("Station IP: ${state.stationIp.ifBlank { "-" }}")
    Text("Device: ${state.deviceId.ifBlank { "-" }}")
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(8.dp))
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        content()
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun CameraSurface(state: BoothUiState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = if (state.cameraSource == CameraSource.AndroidDefault) {
                    "Android Camera Preview"
                } else {
                    "External Canon Live View: ${state.externalCameraStatus.name}"
                },
            )
        }
    }
}

@Composable
private fun TemplateSurface(state: BoothUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(state.selectedTemplate ?: "Template")
            Text(state.capturedPhotoName ?: "Photo")
        }
    }
}

private fun BoothStep.toRoute(): BoothRoute = when (this) {
    BoothStep.Splash -> BoothRoute.Splash
    BoothStep.Dashboard -> BoothRoute.Dashboard
    BoothStep.TemplatePicker -> BoothRoute.TemplatePicker
    BoothStep.CustomTemplate -> BoothRoute.CustomTemplate
    BoothStep.Camera -> BoothRoute.Camera
    BoothStep.CapturePreview -> BoothRoute.CapturePreview
    BoothStep.TemplatePreview -> BoothRoute.TemplatePreview
    BoothStep.Finish -> BoothRoute.Finish
    BoothStep.Settings -> BoothRoute.Settings
    BoothStep.LaunchEvent -> BoothRoute.LaunchEvent
    BoothStep.SettingEvent -> BoothRoute.SettingEvent
}
