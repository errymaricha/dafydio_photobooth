package com.errymaricha.dafydiobooth.ui.booth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import com.errymaricha.dafydiobooth.ui.launch.LaunchUiState
import com.errymaricha.dafydiobooth.ui.launch.LaunchViewModel
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme

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
    VoucherCheck("voucher-check"),
    PaymentGate("payment-gate"),
    WaitingApproval("waiting-approval"),
}

private fun BoothViewModel.toActions() = BoothActions(
    continueFromSplash = ::continueFromSplash,
    openDashboard = ::openDashboard,
    startNowPhoto = ::startNowPhoto,
    openCustomTemplate = ::openCustomTemplate,
    openSettings = ::openSettings,
    openLaunchEvent = ::openLaunchEvent,
    openSettingEvent = ::openSettingEvent,
    startLaunchEventGate = ::startLaunchEventGate,
    selectTemplate = ::selectTemplate,
    saveCustomTemplate = ::saveCustomTemplate,
    capturePhoto = ::capturePhoto,
    retakePhoto = ::retakePhoto,
    acceptCapturePreview = ::acceptCapturePreview,
    finishSession = ::finishSession,
    newSession = ::newSession,
    updateStationIp = ::updateStationIp,
    updateDeviceId = ::updateDeviceId,
    updateToken = ::updateToken,
    updateVoucherCode = ::updateVoucherCode,
    updateVoucherType = ::updateVoucherType,
    updateSessionType = ::updateSessionType,
    updateCustomerId = ::updateCustomerId,
    updatePaymentMethod = ::updatePaymentMethod,
    verifyVoucher = ::verifyVoucher,
    continueWithoutVoucher = ::continueWithoutVoucher,
    requestQuote = ::requestQuote,
    createManualPaymentSession = ::createManualPaymentSession,
    continueAfterFreeQuote = ::continueAfterFreeQuote,
    checkPayment = ::checkPayment,
    setCameraSource = ::setCameraSource,
    scanExternalCamera = ::scanExternalCamera,
    pairExternalCamera = ::pairExternalCamera,
    markExternalCameraConnected = ::markExternalCameraConnected,
    setMirrorLiveView = ::setMirrorLiveView,
    setMirrorCapture = ::setMirrorCapture,
    setImageQuality = ::setImageQuality,
    setUseBackCamera = ::setUseBackCamera,
    setUseFrontCamera = ::setUseFrontCamera,
    setDenoisePhoto = ::setDenoisePhoto,
    setCountdownSeconds = ::setCountdownSeconds,
    setCountdownAudio = ::setCountdownAudio,
    setShutterSound = ::setShutterSound,
    setDefaultPrinting = ::setDefaultPrinting,
    setPrintUsePhotoboothStation = ::setPrintUsePhotoboothStation,
    retry = ::retry,
)

data class BoothActions(
    val continueFromSplash: () -> Unit = {},
    val openDashboard: () -> Unit = {},
    val startNowPhoto: () -> Unit = {},
    val openCustomTemplate: () -> Unit = {},
    val openSettings: () -> Unit = {},
    val openLaunchEvent: () -> Unit = {},
    val openSettingEvent: () -> Unit = {},
    val startLaunchEventGate: () -> Unit = {},
    val selectTemplate: (String) -> Unit = {},
    val saveCustomTemplate: (String) -> Unit = {},
    val capturePhoto: () -> Unit = {},
    val retakePhoto: () -> Unit = {},
    val acceptCapturePreview: () -> Unit = {},
    val finishSession: () -> Unit = {},
    val newSession: () -> Unit = {},
    val updateStationIp: (String) -> Unit = {},
    val updateDeviceId: (String) -> Unit = {},
    val updateToken: (String) -> Unit = {},
    val updateVoucherCode: (String) -> Unit = {},
    val updateVoucherType: (String) -> Unit = {},
    val updateSessionType: (String) -> Unit = {},
    val updateCustomerId: (String) -> Unit = {},
    val updatePaymentMethod: (String) -> Unit = {},
    val verifyVoucher: () -> Unit = {},
    val continueWithoutVoucher: () -> Unit = {},
    val requestQuote: () -> Unit = {},
    val createManualPaymentSession: () -> Unit = {},
    val continueAfterFreeQuote: () -> Unit = {},
    val checkPayment: () -> Unit = {},
    val setCameraSource: (CameraSource) -> Unit = {},
    val scanExternalCamera: () -> Unit = {},
    val pairExternalCamera: () -> Unit = {},
    val markExternalCameraConnected: () -> Unit = {},
    val setMirrorLiveView: (Boolean) -> Unit = {},
    val setMirrorCapture: (Boolean) -> Unit = {},
    val setImageQuality: (ImageQuality) -> Unit = {},
    val setUseBackCamera: (Boolean) -> Unit = {},
    val setUseFrontCamera: (Boolean) -> Unit = {},
    val setDenoisePhoto: (Boolean) -> Unit = {},
    val setCountdownSeconds: (Int) -> Unit = {},
    val setCountdownAudio: (Boolean) -> Unit = {},
    val setShutterSound: (Boolean) -> Unit = {},
    val setDefaultPrinting: (Boolean) -> Unit = {},
    val setPrintUsePhotoboothStation: (Boolean) -> Unit = {},
    val retry: () -> Unit = {},
)

private fun LaunchViewModel.toActions() = LaunchActions(
    onWhatsappChanged = ::onWhatsappChanged,
    onAdditionalPrintChanged = ::onAdditionalPrintChanged,
    onVoucherCodeChanged = ::onVoucherCodeChanged,
    checkVoucherAndQuote = ::checkVoucherAndQuote,
    quoteQrPayment = ::quoteQrPayment,
    submitManualPaymentRequest = ::submitManualPaymentRequest,
    checkManualPaymentApproval = ::checkManualPaymentApproval,
)

data class LaunchActions(
    val onWhatsappChanged: (String) -> Unit = {},
    val onAdditionalPrintChanged: (Int) -> Unit = {},
    val onVoucherCodeChanged: (String) -> Unit = {},
    val checkVoucherAndQuote: () -> Unit = {},
    val quoteQrPayment: () -> Unit = {},
    val submitManualPaymentRequest: () -> Unit = {},
    val checkManualPaymentApproval: () -> Unit = {},
)

@Composable
fun BoothApp(
    viewModel: BoothViewModel,
    launchViewModel: LaunchViewModel,
) {
    val state by viewModel.state.collectAsState()
    val launchState by launchViewModel.ui.collectAsState()
    val navController = rememberNavController()
    val actions = viewModel.toActions()
    val launchActions = launchViewModel.toActions()

    LaunchedEffect(state.step) {
        navController.navigate(state.step.toRoute().route) {
            launchSingleTop = true
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            restoreState = true
        }
    }

    LaunchedEffect(state.step, state.deviceId, state.token) {
        if (state.step == BoothStep.LaunchEvent && state.isStationConnected) {
            launchViewModel.init(
                deviceCode = state.deviceId,
                apiKey = state.token,
            )
            launchViewModel.onWhatsappChanged(state.customerId)
        }
    }

    LaunchedEffect(launchState.shouldNavigateToTemplates) {
        if (launchState.shouldNavigateToTemplates) {
            actions.startNowPhoto()
            launchViewModel.consumeTemplateNavigation()
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
                SplashScreen(actions = actions)
            }
            composable(BoothRoute.Dashboard.route) {
                DashboardScreen(state = state, actions = actions)
            }
            composable(BoothRoute.TemplatePicker.route) {
                TemplatePickerScreen(
                    state = state,
                    launchState = launchState,
                    templates = viewModel.defaultTemplates,
                    actions = actions,
                )
            }
            composable(BoothRoute.CustomTemplate.route) {
                CustomTemplateScreen(state = state, actions = actions)
            }
            composable(BoothRoute.Camera.route) {
                CameraScreen(state = state, launchState = launchState, actions = actions)
            }
            composable(BoothRoute.CapturePreview.route) {
                CapturePreviewScreen(state = state, actions = actions)
            }
            composable(BoothRoute.TemplatePreview.route) {
                TemplatePreviewScreen(state = state, actions = actions)
            }
            composable(BoothRoute.Finish.route) {
                FinishScreen(state = state, actions = actions)
            }
            composable(BoothRoute.Settings.route) {
                SettingsScreen(state = state, actions = actions)
            }
            composable(BoothRoute.LaunchEvent.route) {
                LaunchEventScreen(
                    state = state,
                    launchState = launchState,
                    actions = actions,
                    launchActions = launchActions,
                )
            }
            composable(BoothRoute.SettingEvent.route) {
                SettingEventScreen(state = state, actions = actions)
            }
            composable(BoothRoute.VoucherCheck.route) {
                VoucherCheckScreen(state = state, actions = actions)
            }
            composable(BoothRoute.PaymentGate.route) {
                PaymentGateScreen(state = state, actions = actions)
            }
            composable(BoothRoute.WaitingApproval.route) {
                WaitingApprovalScreen(state = state, actions = actions)
            }
        }
    }
}

@Composable
private fun SplashScreen(actions: BoothActions) {
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
private fun DashboardScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Dashboard", state = state, actions = actions) {
        DashboardContent(state = state, actions = actions)
    }
}

@Composable
private fun DashboardContent(state: BoothUiState, actions: BoothActions) {
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

@Composable
private fun DashboardActions(state: BoothUiState, actions: BoothActions, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        Button(onClick = actions.startNowPhoto, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Start Now Photo")
        }
        OutlinedButton(onClick = actions.openCustomTemplate, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Create Custom Template")
        }
        OutlinedButton(onClick = actions.startNowPhoto, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("List Default Template")
        }
        OutlinedButton(onClick = actions.openSettings, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Settings")
        }
        if (state.isStationConnected) {
            Button(onClick = actions.openLaunchEvent, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Launch Event")
            }
            OutlinedButton(onClick = actions.openSettingEvent, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Setting Event")
            }
        }
    }
}

@Composable
private fun DashboardStatusPanel(state: BoothUiState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Station", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            StatusLine(state)
            Text(
                text = if (state.isStationConnected) {
                    "Data akan direkap ke Photobooth Station."
                } else {
                    "Mode lokal: data tidak disimpan ke database."
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("Next", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(if (state.isStationConnected) "Launch Event untuk voucher/payment." else "Settings untuk connect station.")
        }
    }
}

@Composable
private fun LaunchEventScreen(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
    launchActions: LaunchActions,
) {
    ScreenFrame(title = "Launch Event", state = state, actions = actions) {
        Text("Station: ${state.stationIp}")
        Text("Device: ${state.deviceId}")
        Text("Pembayaran event connected.")
        if (launchState.loading) {
            CircularProgressIndicator()
        }
        launchState.pricing?.let { pricing ->
            Text("Harga photobooth: ${pricing.currencyCode} ${pricing.photoboothPrice.toLong()}")
            Text("Tambahan print: ${pricing.currencyCode} ${pricing.additionalPrintPrice.toLong()}")
            Text("Total: ${pricing.currencyCode} ${launchState.finalAmount.toLong()}", fontWeight = FontWeight.Bold)
        } ?: Text("Pricing belum tersinkron dari Photobooth Station.")
        OutlinedTextField(
            value = state.customerId,
            onValueChange = { value ->
                actions.updateCustomerId(value)
                launchActions.onWhatsappChanged(value)
            },
            label = { Text("ID Customer / ID Pelanggan") },
            placeholder = { Text("Nomor WA terdaftar") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Kosongkan untuk memakai default customer dari Photobooth Station.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = launchState.additionalPrintCount.toString(),
            onValueChange = { value ->
                launchActions.onAdditionalPrintChanged(value.toIntOrNull() ?: 0)
            },
            label = { Text("Additional Print Count") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = launchState.voucherCode,
            onValueChange = launchActions.onVoucherCodeChanged,
            label = { Text("Kode Voucher") },
            placeholder = { Text("Kosongkan jika tanpa voucher") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = launchActions.checkVoucherAndQuote,
                enabled = !launchState.loading && launchState.voucherCode.isNotBlank(),
                modifier = Modifier.weight(1f),
            ) {
                Text("Cek Voucher")
            }
            OutlinedButton(
                onClick = launchActions.quoteQrPayment,
                enabled = !launchState.loading,
                modifier = Modifier.weight(1f),
            ) {
                Text("QR Code")
            }
        }
        launchState.quote?.let { quote ->
            Text("Subtotal: ${quote.currency} ${quote.subtotalAmount ?: launchState.finalAmount.toLong()}")
            Text("Diskon: ${quote.currency} ${quote.discountAmount ?: 0}")
            Text("Total bayar: ${quote.currency} ${quote.amount}", fontWeight = FontWeight.Bold)
            Text("Payment URL: ${quote.paymentUrl ?: "-"}")
        }
        launchState.message?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = "Kode Session: ${launchState.session?.sessionCode ?: "-"}",
            fontWeight = FontWeight.Bold,
        )
        Text("Approval: ${launchState.approvalStatus ?: launchState.session?.paymentStatus ?: "-"}")
        launchState.error?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        OutlinedButton(
            onClick = launchActions.submitManualPaymentRequest,
            enabled = launchState.canSubmitManualPayment,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (launchState.isManualPaymentWaiting) "Menunggu Approval Station" else "Pembayaran Manual")
        }
        OutlinedButton(
            onClick = launchActions.checkManualPaymentApproval,
            enabled = !launchState.loading && launchState.session != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Check Approval")
        }
    }
}

@Composable
private fun SettingEventScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Setting Event", state = state, actions = actions) {
        Text("Event setting tersambung ke Photobooth Station.")
        Text("Konfigurasi event backend akan ditambahkan setelah contract event tersedia.")
        OutlinedButton(onClick = actions.openDashboard, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
private fun VoucherCheckScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Voucher Check", state = state, actions = actions) {
        EventStatus(state)
        OutlinedTextField(
            value = state.voucherCode,
            onValueChange = actions.updateVoucherCode,
            label = { Text("Voucher Code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.voucherType,
            onValueChange = actions.updateVoucherType,
            label = { Text("Voucher Type") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.sessionType,
            onValueChange = actions.updateSessionType,
            label = { Text("Session Type") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = actions.continueWithoutVoucher, modifier = Modifier.weight(1f)) {
                Text("No Voucher")
            }
            Button(onClick = actions.verifyVoucher, enabled = !state.isLoading, modifier = Modifier.weight(1f)) {
                Text("Verify")
            }
        }
    }
}

@Composable
private fun PaymentGateScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Payment Gate", state = state, actions = actions) {
        EventStatus(state)
        Text("Voucher: ${state.voucher?.code ?: state.voucherCode.ifBlank { "-" }}")
        Text("Type: ${state.voucher?.type ?: state.voucherType}")
        Text("Amount: ${state.quote?.currency ?: "IDR"} ${state.quote?.amount ?: 0}")
        Text("Payment required: ${state.quote?.paymentRequired ?: true}")
        Text("Unlock photo: ${state.quote?.unlockPhoto ?: false}")
        OutlinedTextField(
            value = state.paymentMethod,
            onValueChange = actions.updatePaymentMethod,
            label = { Text("Payment Method") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = actions.requestQuote, enabled = !state.isLoading, modifier = Modifier.weight(1f)) {
                Text("Quote")
            }
            Button(
                onClick = actions.createManualPaymentSession,
                enabled = !state.isLoading,
                modifier = Modifier.weight(1f),
            ) {
                Text("Manual Payment")
            }
        }
        OutlinedButton(
            onClick = actions.continueAfterFreeQuote,
            enabled = state.quote?.paymentRequired == false && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Continue Without Payment")
        }
    }
}

@Composable
private fun WaitingApprovalScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Waiting Approval", state = state, actions = actions) {
        EventStatus(state)
        Text("Kode Session: ${state.session?.sessionCode ?: "-"}", fontWeight = FontWeight.Bold)
        Text("Session ID: ${state.session?.sessionId ?: "-"}")
        Text("Payment: ${state.paymentStatus?.paymentStatus ?: state.session?.paymentStatus ?: "pending"}")
        Text("Approval dilakukan dari Photobooth Station.")
        Button(onClick = actions.checkPayment, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
            Text("Check Approval")
        }
    }
}

@Composable
private fun TemplatePickerScreen(
    state: BoothUiState,
    launchState: LaunchUiState,
    templates: List<String>,
    actions: BoothActions,
) {
    ScreenFrame(title = "Pilih Template", state = state, actions = actions) {
        Text(
            text = "Kode Session: ${launchState.session?.sessionCode ?: state.session?.sessionCode ?: "-"}",
            fontWeight = FontWeight.Bold,
        )
        templates.forEach { template ->
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
private fun CustomTemplateScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Create Custom Template", state = state, actions = actions) {
        Text("Template custom sementara dibuat lokal. Editor detail akan dihubungkan ke Photobooth Station.")
        Button(onClick = { actions.saveCustomTemplate("Custom Template") }, modifier = Modifier.fillMaxWidth()) {
            Text("Use Custom Template")
        }
    }
}

@Composable
private fun CameraScreen(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
) {
    ScreenFrame(title = "Camera", state = state, actions = actions) {
        CameraSurface(state)
        Text(
            text = "Kode Session: ${launchState.session?.sessionCode ?: state.session?.sessionCode ?: "-"}",
            fontWeight = FontWeight.Bold,
        )
        Text("ID Pelanggan / No WA: ${launchState.customerWhatsapp.ifBlank { state.customerId.ifBlank { "-" } }}")
        Text("Template: ${state.selectedTemplate ?: "-"}")
        Text("Camera: ${if (state.cameraSource == CameraSource.AndroidDefault) "Android default" else "External Canon"}")
        Text("Timer: ${state.countdownSeconds}s")
        Button(onClick = actions.capturePhoto, modifier = Modifier.fillMaxWidth()) {
            Text("Capture")
        }
    }
}

@Composable
private fun CapturePreviewScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Preview Capture", state = state, actions = actions) {
        CameraSurface(state)
        Text("Photo: ${state.capturedPhotoName ?: "-"}")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = actions.retakePhoto, modifier = Modifier.weight(1f)) {
                Text("Retake")
            }
            Button(onClick = actions.acceptCapturePreview, modifier = Modifier.weight(1f)) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun TemplatePreviewScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Preview Template", state = state, actions = actions) {
        TemplateSurface(state)
        Text("Template: ${state.selectedTemplate ?: "-"}")
        Text("Photo: ${state.capturedPhotoName ?: "-"}")
        Button(onClick = actions.finishSession, modifier = Modifier.fillMaxWidth()) {
            Text("Finish")
        }
    }
}

@Composable
private fun FinishScreen(state: BoothUiState, actions: BoothActions) {
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
        Button(onClick = actions.newSession, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
private fun SettingsScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Settings", state = state, actions = actions) {
        SectionTitle("Photobooth Station")
        OutlinedTextField(
            value = state.stationIp,
            onValueChange = actions.updateStationIp,
            label = { Text("Station URL / IP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
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
        ChipRow {
            ImageQuality.entries.forEach { quality ->
                FilterChip(
                    selected = state.imageQuality == quality,
                    onClick = { actions.setImageQuality(quality) },
                    label = { Text(quality.name) },
                )
            }
        }
        SwitchRow("Back Camera", state.useBackCamera, actions.setUseBackCamera)
        SwitchRow("Front Camera", state.useFrontCamera, actions.setUseFrontCamera)
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

@Composable
private fun ScreenFrame(
    title: String,
    state: BoothUiState,
    actions: BoothActions,
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
                OutlinedButton(onClick = actions.openDashboard) {
                    Text("Home")
                }
            }
        }
        if (state.isLoading) {
            CircularProgressIndicator()
        }
        state.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = actions.retry, enabled = !state.isLoading) {
                Text("Retry")
            }
        }
        content()
    }
}

@Composable
private fun EventStatus(state: BoothUiState) {
    state.eventStatusMessage?.let { message ->
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(text = message, modifier = Modifier.padding(14.dp))
        }
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
@OptIn(ExperimentalLayoutApi::class)
private fun ChipRow(content: @Composable () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
    BoothStep.VoucherCheck -> BoothRoute.VoucherCheck
    BoothStep.PaymentGate -> BoothRoute.PaymentGate
    BoothStep.WaitingApproval -> BoothRoute.WaitingApproval
}

private val PreviewLocalState = BoothUiState(
    step = BoothStep.Dashboard,
    stationIp = "http://10.10.116.4:8000/",
    deviceId = "PB-DEVICE-01",
    isStationConnected = false,
)

private val PreviewConnectedState = PreviewLocalState.copy(
    isStationConnected = true,
    selectedTemplate = "Classic 2 Slot",
    capturedPhotoName = "capture-preview.jpg",
    printUsePhotoboothStation = true,
)

@Preview(name = "Dashboard Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun DashboardMobilePreview() {
    DafydioBoothTheme {
        DashboardScreen(state = PreviewLocalState, actions = BoothActions())
    }
}

@Preview(name = "Dashboard Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun DashboardTabletPreview() {
    DafydioBoothTheme {
        DashboardScreen(state = PreviewConnectedState, actions = BoothActions())
    }
}

@Preview(name = "Settings Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun SettingsTabletPreview() {
    DafydioBoothTheme {
        SettingsScreen(
            state = PreviewConnectedState.copy(
                step = BoothStep.Settings,
                cameraSource = CameraSource.ExternalCanon,
                externalCameraStatus = ExternalCameraStatus.Connected,
                mirrorLiveView = true,
                countdownSeconds = 5,
            ),
            actions = BoothActions(),
        )
    }
}

@Preview(name = "Finish Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun FinishTabletPreview() {
    DafydioBoothTheme {
        FinishScreen(
            state = PreviewConnectedState.copy(step = BoothStep.Finish),
            actions = BoothActions(),
        )
    }
}

@Preview(name = "Payment Gate Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun PaymentGateTabletPreview() {
    DafydioBoothTheme {
        PaymentGateScreen(
            state = PreviewConnectedState.copy(
                step = BoothStep.PaymentGate,
                voucherCode = "PROMO-EVENT",
                eventStatusMessage = "Payment dibutuhkan. Pilih manual payment untuk menunggu approval station.",
            ),
            actions = BoothActions(),
        )
    }
}
