package com.errymaricha.dafydiobooth.ui.booth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.LinearEasing
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.errymaricha.dafydiobooth.R
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.errymaricha.dafydiobooth.domain.model.LaunchSession
import com.errymaricha.dafydiobooth.ui.dashboard.DashboardRedesignContainer
import com.errymaricha.dafydiobooth.ui.events.AllowedTemplatesPageRedesign
import com.errymaricha.dafydiobooth.ui.events.EventsPageRedesign
import com.errymaricha.dafydiobooth.ui.launch.LaunchPageRedesign
import com.errymaricha.dafydiobooth.ui.launch.LaunchUiState
import com.errymaricha.dafydiobooth.ui.launch.LaunchViewModel
import com.errymaricha.dafydiobooth.ui.launch.PaymentGatePageRedesign
import com.errymaricha.dafydiobooth.ui.launch.VoucherCheckPageRedesign
import com.errymaricha.dafydiobooth.ui.launch.WaitingApprovalPageRedesign
import com.errymaricha.dafydiobooth.ui.setup.SetupPageRedesign
import com.errymaricha.dafydiobooth.ui.template.CustomTemplatePageRedesign
import com.errymaricha.dafydiobooth.ui.template.TemplatePickerPageRedesign
import com.errymaricha.dafydiobooth.ui.template.TemplatePreviewPageRedesign
import java.io.File
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    SettingAllowedTemplates("setting-allowed-templates"),
    VoucherCheck("voucher-check"),
    PaymentGate("payment-gate"),
    WaitingApproval("waiting-approval"),
}

private fun BoothViewModel.toActions() = BoothActions(
    continueFromSplash = ::continueFromSplash,
    openDashboard = ::openDashboard,
    startNowPhoto = ::startNowPhoto,
    openConnectedTemplateFlow = ::openConnectedTemplateFlow,
    openCustomTemplate = ::openCustomTemplate,
    openSettings = ::openSettings,
    disconnectStation = ::disconnectStation,
    openLaunchEvent = ::openLaunchEvent,
    openSettingEvent = ::openSettingEvent,
    openSettingAllowedTemplates = ::openSettingAllowedTemplates,
    syncLaunchSession = ::syncLaunchSession,
    startLaunchEventGate = ::startLaunchEventGate,
    selectTemplate = ::selectTemplate,
    saveCustomTemplate = ::saveCustomTemplate,
    capturePhoto = ::capturePhoto,
    capturePhotoFile = ::capturePhoto,
    retakePhoto = ::retakePhoto,
    acceptCapturePreview = ::acceptCapturePreview,
    finishSession = ::finishSession,
    downloadResult = ::downloadResult,
    newSession = ::newSession,
    updateStationIp = ::updateStationIp,
    updateDeviceId = ::updateDeviceId,
    updateToken = ::updateToken,
    refreshTemplates = ::refreshTemplates,
    updateCustomerWhatsapp = ::updateCustomerWhatsapp,
    updateLaunchEventName = ::updateLaunchEventName,
    setLaunchSelectedEventId = ::setLaunchSelectedEventId,
    toggleLaunchAllowedTemplate = ::toggleLaunchAllowedTemplate,
    clearLaunchAllowedTemplates = ::clearLaunchAllowedTemplates,
    selectAllLaunchAllowedTemplates = ::selectAllLaunchAllowedTemplates,
    updateLaunchTemplateSearchQuery = ::updateLaunchTemplateSearchQuery,
    updateLaunchAdditionalPrintCount = ::updateLaunchAdditionalPrintCount,
    updateVoucherCode = ::updateVoucherCode,
    updateVoucherType = ::updateVoucherType,
    updateSessionType = ::updateSessionType,
    updateCustomerId = ::updateCustomerId,
    updatePaymentMethod = ::updatePaymentMethod,
    updateKioskExitCode = ::updateKioskExitCode,
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
    setExternalPreviewFps = ::setExternalPreviewFps,
    setMirrorCapture = ::setMirrorCapture,
    setImageQuality = ::setImageQuality,
    updateDetectedCameras = ::updateDetectedCameras,
    setUseBackCamera = ::setUseBackCamera,
    setUseFrontCamera = ::setUseFrontCamera,
    setDenoisePhoto = ::setDenoisePhoto,
    setCountdownSeconds = ::setCountdownSeconds,
    setCountdownAudio = ::setCountdownAudio,
    setShutterSound = ::setShutterSound,
    setDefaultPrinting = ::setDefaultPrinting,
    setPrintUsePhotoboothStation = ::setPrintUsePhotoboothStation,
    triggerMockPrint = ::triggerMockPrint,
    onStoragePermissionDenied = ::onStoragePermissionDenied,
    retry = ::retry,
    sendHeartbeatNow = ::sendHeartbeatNow,
    onStepChanged = ::onStepChanged,
    setWelcomeBgUri = ::setWelcomeBgUri,
    setWelcomeBgIsVideo = ::setWelcomeBgIsVideo,
)

data class BoothActions(
    val continueFromSplash: () -> Unit = {},
    val openDashboard: () -> Unit = {},
    val startNowPhoto: () -> Unit = {},
    val openConnectedTemplateFlow: () -> Unit = {},
    val openCustomTemplate: () -> Unit = {},
    val openSettings: () -> Unit = {},
    val disconnectStation: () -> Unit = {},
    val openLaunchEvent: () -> Unit = {},
    val openSettingEvent: () -> Unit = {},
    val openSettingAllowedTemplates: () -> Unit = {},
    val syncLaunchSession: (LaunchSession?, String, String?, String) -> Unit = { _, _, _, _ -> },
    val startLaunchEventGate: () -> Unit = {},
    val selectTemplate: (String) -> Unit = {},
    val saveCustomTemplate: (String) -> Unit = {},
    val capturePhoto: () -> Unit = {},
    val capturePhotoFile: (String) -> Unit = {},
    val retakePhoto: () -> Unit = {},
    val acceptCapturePreview: () -> Unit = {},
    val finishSession: () -> Unit = {},
    val downloadResult: () -> Unit = {},
    val newSession: () -> Unit = {},
    val updateStationIp: (String) -> Unit = {},
    val updateDeviceId: (String) -> Unit = {},
    val updateToken: (String) -> Unit = {},
    val refreshTemplates: () -> Unit = {},
    val updateCustomerWhatsapp: (String) -> Unit = {},
    val updateLaunchEventName: (String) -> Unit = {},
    val setLaunchSelectedEventId: (String) -> Unit = {},
    val toggleLaunchAllowedTemplate: (String) -> Unit = {},
    val clearLaunchAllowedTemplates: () -> Unit = {},
    val selectAllLaunchAllowedTemplates: () -> Unit = {},
    val updateLaunchTemplateSearchQuery: (String) -> Unit = {},
    val updateLaunchAdditionalPrintCount: (String) -> Unit = {},
    val updateVoucherCode: (String) -> Unit = {},
    val updateVoucherType: (String) -> Unit = {},
    val updateSessionType: (String) -> Unit = {},
    val updateCustomerId: (String) -> Unit = {},
    val updatePaymentMethod: (String) -> Unit = {},
    val updateKioskExitCode: (String) -> Unit = {},
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
    val setExternalPreviewFps: (Int) -> Unit = {},
    val setMirrorCapture: (Boolean) -> Unit = {},
    val setImageQuality: (ImageQuality) -> Unit = {},
    val updateDetectedCameras: (Boolean, Boolean) -> Unit = { _, _ -> },
    val setUseBackCamera: (Boolean) -> Unit = {},
    val setUseFrontCamera: (Boolean) -> Unit = {},
    val setDenoisePhoto: (Boolean) -> Unit = {},
    val setCountdownSeconds: (Int) -> Unit = {},
    val setCountdownAudio: (Boolean) -> Unit = {},
    val setShutterSound: (Boolean) -> Unit = {},
    val setDefaultPrinting: (Boolean) -> Unit = {},
    val setPrintUsePhotoboothStation: (Boolean) -> Unit = {},
    val triggerMockPrint: () -> Unit = {},
    val onStoragePermissionDenied: () -> Unit = {},
    val retry: () -> Unit = {},
    val sendHeartbeatNow: (String) -> Unit = {},
    val onStepChanged: (BoothStep) -> Unit = {},
    val setWelcomeBgUri: (String) -> Unit = {},
    val setWelcomeBgIsVideo: (Boolean) -> Unit = {},
)

private fun LaunchViewModel.toActions() = LaunchActions(
    onEventCodeChanged = ::onEventCodeChanged,
    onEventNameChanged = ::onEventNameChanged,
    onSelectEvent = ::onSelectEvent,
    refreshEvents = ::refreshEvents,
    createOrUpdateEvent = ::createOrUpdateEvent,
    onWhatsappChanged = ::onWhatsappChanged,
    onAdditionalPrintChanged = ::onAdditionalPrintChanged,
    onVoucherCodeChanged = ::onVoucherCodeChanged,
    checkVoucherAndQuote = ::checkVoucherAndQuote,
    quoteQrPayment = ::quoteQrPayment,
    submitManualPaymentRequest = ::submitManualPaymentRequest,
    checkManualPaymentApproval = ::checkManualPaymentApproval,
)

data class LaunchActions(
    val onEventCodeChanged: (String) -> Unit = {},
    val onEventNameChanged: (String) -> Unit = {},
    val onSelectEvent: (String) -> Unit = {},
    val refreshEvents: () -> Unit = {},
    val createOrUpdateEvent: () -> Unit = {},
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
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val launchState by launchViewModel.ui.collectAsState()
    val navController = rememberNavController()
    val actions = viewModel.toActions()
    val launchActions = launchViewModel.toActions()
    var pendingDownload by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            actions.downloadResult()
        } else {
            actions.onStoragePermissionDenied()
        }
        pendingDownload = false
    }

    fun downloadWithStoragePermissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || hasLegacyStorageWritePermission(context)) {
            actions.downloadResult()
            return
        }
        pendingDownload = true
        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    LaunchedEffect(state.step) {
        actions.onStepChanged(state.step)
        navController.navigate(state.step.toRoute().route) {
            launchSingleTop = true
            if (state.step == BoothStep.Splash) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
            } else {
                popUpTo(BoothRoute.Splash.route) {
                    inclusive = true
                }
            }
            restoreState = true
        }
    }

    LaunchedEffect(state.isStationReachable, state.deviceId, state.token) {
        if (state.isStationReachable && state.deviceId.isNotBlank() && state.token.isNotBlank()) {
            launchViewModel.init(
                deviceCode = state.deviceId,
                apiKey = state.token,
            )
        }
    }

    LaunchedEffect(state.step, state.launchSelectedEventId, state.customerWhatsapp, state.voucherCode, state.launchAdditionalPrintCount) {
        if ((state.step == BoothStep.LaunchEvent || state.step == BoothStep.SettingEvent) && state.isStationReachable) {
            if (state.launchSelectedEventId.isNotBlank()) {
                launchViewModel.onSelectEvent(state.launchSelectedEventId)
            }
            if (launchState.customerWhatsapp.isBlank() && state.customerWhatsapp.isNotBlank()) {
                launchViewModel.onWhatsappChanged(state.customerWhatsapp)
            }
            if (launchState.voucherCode.isBlank() && state.voucherCode.isNotBlank()) {
                launchViewModel.onVoucherCodeChanged(state.voucherCode)
            }
            if (launchState.additionalPrintCount == 0 && state.launchAdditionalPrintCount > 0) {
                launchViewModel.onAdditionalPrintChanged(state.launchAdditionalPrintCount)
            }
        }
    }

    LaunchedEffect(launchState.shouldNavigateToTemplates) {
        if (launchState.shouldNavigateToTemplates) {
            actions.syncLaunchSession(
                launchState.session,
                launchState.customerWhatsapp,
                launchState.token,
                launchState.selectedEventId,
            )
            actions.openConnectedTemplateFlow()
            launchViewModel.consumeTemplateNavigation()
        }
    }

    LaunchedEffect(pendingDownload) {
        if (!pendingDownload) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || hasLegacyStorageWritePermission(context)) {
            actions.downloadResult()
            pendingDownload = false
        }
    }

    BackHandler {
        if (state.step != BoothStep.Dashboard) {
            actions.openDashboard()
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Keluar Aplikasi") },
            text = { Text("Yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        (context as? android.app.Activity)?.finish()
                    },
                ) { Text("Keluar") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Batal") }
            },
        )
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
                SplashPageRedesign(actions = actions)
            }
            composable(BoothRoute.Dashboard.route) {
                DashboardRedesignContainer(
                    state = state,
                    actions = actions,
                    launchState = launchState,
                    launchActions = launchActions,
                )
            }
            composable(BoothRoute.TemplatePicker.route) {
                TemplatePickerPageRedesign(
                    state = state,
                    launchState = launchState,
                    actions = actions,
                )
            }
            composable(BoothRoute.CustomTemplate.route) {
                CustomTemplatePageRedesign(state = state, actions = actions)
            }
            composable(BoothRoute.Camera.route) {
                CameraPageRedesign(state = state, launchState = launchState, actions = actions)
            }
            composable(BoothRoute.CapturePreview.route) {
                CapturePreviewPageRedesign(state = state, actions = actions)
            }
            composable(BoothRoute.TemplatePreview.route) {
                TemplatePreviewPageRedesign(state = state, actions = actions)
            }
            composable(BoothRoute.Finish.route) {
                FinishPageRedesign(
                    state = state,
                    actions = actions.copy(downloadResult = ::downloadWithStoragePermissionCheck),
                )
            }
            composable(BoothRoute.Settings.route) {
                SetupPageRedesign(state = state, actions = actions)
            }
            composable(BoothRoute.LaunchEvent.route) {
                LaunchPageRedesign(
                    state = state,
                    launchState = launchState,
                    actions = actions,
                    launchActions = launchActions,
                    onBackToDashboard = actions.openDashboard,
                )
            }
            composable(BoothRoute.SettingEvent.route) {
                EventsPageRedesign(
                    state = state,
                    launchState = launchState,
                    actions = actions,
                    launchActions = launchActions,
                )
            }
            composable(BoothRoute.SettingAllowedTemplates.route) {
                AllowedTemplatesPageRedesign(state = state, actions = actions)
            }
            composable(BoothRoute.VoucherCheck.route) {
                VoucherCheckPageRedesign(state = state, actions = actions)
            }
            composable(BoothRoute.PaymentGate.route) {
                PaymentGatePageRedesign(state = state, actions = actions)
            }
            composable(BoothRoute.WaitingApproval.route) {
                WaitingApprovalPageRedesign(state = state, actions = actions)
            }
        }
    }
}

@Composable
private fun SplashPageRedesign(actions: BoothActions) {
    SplashScreen(actions = actions)
}

@Composable
private fun SplashScreen(actions: BoothActions) {
    var phase by remember { mutableStateOf(2) } // 2: Camera flash shutter, 3: Title, logo and loading progress
    var flashAlpha by remember { mutableStateOf(0f) }
    
    val logoScale = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val loadingProgress = remember { Animatable(0f) }
    val shutterProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Phase 2: Shutter Capture Flash
        // Animate shutter blades closing & opening (0f to 1f)
        val job = launch {
            shutterProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(800, easing = LinearEasing)
            )
        }
        
        delay(400) // wait until half-way (fully closed shutter)
        
        // Trigger White Flash!
        flashAlpha = 1f
        animate(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = tween(450)
        ) { value, _ ->
            flashAlpha = value
        }
        
        job.join() // wait until shutter fully opens
        delay(200)

        // Phase 3: Dafydio Booth Title + loading bar
        phase = 3
        logoScale.animateTo(1f, animationSpec = spring())
        contentAlpha.animateTo(1f, animationSpec = tween(450))
        loadingProgress.animateTo(1f, animationSpec = tween(2000, easing = LinearEasing))
        delay(400)

        // Done: Enter dashboard
        actions.continueFromSplash()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1123),
                        Color(0xFF07080F),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Main Container for content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            // Main Logo / Shutter animation area
            Box(
                modifier = Modifier.size(540.dp),
                contentAlignment = Alignment.Center
            ) {
                if (phase == 1 || phase == 3) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .graphicsLayer {
                                scaleX = logoScale.value
                                scaleY = logoScale.value
                                alpha = if (phase == 3) contentAlpha.value else 1f
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Track Circle
                            drawCircle(
                                color = Color.White.copy(alpha = 0.08f),
                                radius = size.minDimension / 2f - 4.dp.toPx(),
                                style = Stroke(width = 4.dp.toPx())
                            )
                            
                            // Circular Progress Arc
                            if (phase == 3) {
                                val strokeWidth = 5.dp.toPx()
                                val arcSize = size.minDimension - (strokeWidth * 2)
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFF5B67FF),
                                            Color(0xFF8B5CF6),
                                            Color(0xFF5B67FF)
                                        )
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = loadingProgress.value * 360f,
                                    useCenter = false,
                                    topLeft = Offset(strokeWidth, strokeWidth),
                                    size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        }
                        Image(
                            painter = painterResource(R.drawable.dafydio_logo),
                            contentDescription = "Dafydio Logo",
                            modifier = Modifier.size(130.dp)
                        )
                    }
                }
                
                if (phase == 2) {
                    // Draw the custom camera shutter lens animation
                    Canvas(
                        modifier = Modifier
                            .size(500.dp)
                            .border(8.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f
                        
                        // Draw lens background
                        drawCircle(
                            color = Color(0xFF131524),
                            radius = radius,
                            center = center
                        )
                        
                        // Calculate blade aperture closing
                        val progress = shutterProgress.value
                        val progressFactor = if (progress <= 0.5f) {
                            progress * 2f // 0f to 1f
                        } else {
                            (1f - progress) * 2f // 1f to 0f
                        }
                        val innerRadius = radius * (1f - progressFactor)
                        
                        // Draw 6 shutter blades
                        val bladeCount = 6
                        for (i in 0 until bladeCount) {
                            val angleDeg = i * (360f / bladeCount) + (progressFactor * 35f)
                            val rad = Math.toRadians(angleDeg.toDouble())
                            
                            val startX = center.x + Math.cos(rad).toFloat() * radius
                            val startY = center.y + Math.sin(rad).toFloat() * radius
                            
                            val endAngleRad = Math.toRadians((angleDeg + 60f).toDouble())
                            val endX = center.x + Math.cos(endAngleRad).toFloat() * innerRadius
                            val endY = center.y + Math.sin(endAngleRad).toFloat() * innerRadius
                            
                            drawLine(
                                color = Color(0xFF5B67FF),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 8.dp.toPx()
                            )
                        }
                        
                        // Inner lens glass reflection
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = radius * 0.8f,
                            center = Offset(center.x - radius * 0.15f, center.y - radius * 0.15f)
                        )
                    }
                }
            }

            // Title & loading bar (Fade in during Phase 3)
            if (phase >= 3) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.graphicsLayer {
                        alpha = contentAlpha.value
                    }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Dafydio ",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Booth",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF5B67FF), Color(0xFF8B5CF6))
                                    )
                                ),
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Premium Photobooth Station",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Camera Shutter white flash overlay (Phase 2)
        if (phase == 2 && flashAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAlpha))
            )
        }
    }
}


@Composable
fun ScreenFrame(
    title: String,
    state: BoothUiState,
    actions: BoothActions,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val modifier = if (scrollable) {
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    } else {
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    }
    Column(
        modifier = modifier,
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
            val isTemplateMappingError = message.contains("Template mapping invalid", ignoreCase = true)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (isTemplateMappingError) "Template Mapping Error" else "Terjadi Kesalahan",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    if (isTemplateMappingError) {
                        Text(
                            text = "Langkah: sync template ulang dari station, lalu pilih template lagi.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    OutlinedButton(
                        onClick = actions.retry,
                        enabled = !state.isLoading,
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
        content()
    }
}

@Composable
fun CapturedPhotoSurface(state: BoothUiState) {
    val path = state.capturedPhotoPath
    if (path != null) {
        AsyncImage(
            model = File(path),
            contentDescription = "Captured photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
        )
    } else {
        CameraSurface(state)
    }
}

fun hasLegacyStorageWritePermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun TemplateSurface(state: BoothUiState) {
    val context = LocalContext.current
    val orderedSlots = remember(state.selectedTemplateSlots) {
        state.selectedTemplateSlots.map { it.sourceSlotIndex }.distinct().sorted()
    }
    val currentActiveSlot = remember(orderedSlots, state.nextCaptureIndex) {
        val index = (state.nextCaptureIndex - 1).coerceIn(0, orderedSlots.lastIndex)
        if (orderedSlots.isNotEmpty()) orderedSlots[index] else -1
    }
    val localPreview = state.selectedTemplatePreviewLocalPath
        ?.takeIf { it.isNotBlank() }
        ?.let { File(it) }
        ?.takeIf { it.exists() && it.length() > 0L }
    val previewModel: Any? = localPreview
    val localOverlay = state.selectedTemplateOverlayLocalPath
        ?.takeIf { it.isNotBlank() }
        ?.let { File(it) }
        ?.takeIf { it.exists() && it.length() > 0L }
    val overlayModel: Any? = localOverlay
    val hasOverlay = overlayModel != null
    val canvasWidth = state.selectedTemplateCanvasWidth.takeIf { it > 0 } ?: 1
    val canvasHeight = state.selectedTemplateCanvasHeight.takeIf { it > 0 } ?: 1
    val aspect = canvasWidth.toFloat() / canvasHeight.toFloat()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp, max = 460.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight
        val containerAspect = containerWidth.value / containerHeight.value
        val frameWidth = if (containerAspect > aspect) containerHeight * aspect else containerWidth
        val frameHeight = if (containerAspect > aspect) containerHeight else containerWidth / aspect
        val offsetX = (containerWidth - frameWidth) / 2f
        val offsetY = (containerHeight - frameHeight) / 2f

        Box(
            modifier = Modifier
                .offset(x = offsetX, y = offsetY)
                .size(frameWidth, frameHeight)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.05f)),
        ) {
            // Prioritas preview canvas: capture + overlay.
            // Preview bitmap dipakai hanya kalau overlay belum ada.
            if (!hasOverlay && previewModel != null) {
                AsyncImage(
                    model = previewModel,
                    contentDescription = "Template preview",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            state.selectedTemplateSlots.forEach { slot ->
                val x = frameWidth * (slot.x.toFloat() / canvasWidth.toFloat())
                val y = frameHeight * (slot.y.toFloat() / canvasHeight.toFloat())
                val w = frameWidth * (slot.width.toFloat() / canvasWidth.toFloat())
                val h = frameHeight * (slot.height.toFloat() / canvasHeight.toFloat())
                val r = frameWidth * (slot.borderRadius.toFloat() / canvasWidth.toFloat())
                val photoPath = state.capturedPhotosBySlot[slot.sourceSlotIndex]
                    ?: if (slot.sourceSlotIndex == currentActiveSlot) state.capturedPhotoPath else null
                if (photoPath != null) {
                    AsyncImage(
                        model = File(photoPath),
                        contentDescription = "Slot ${slot.slotIndex}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .offset(x = x, y = y)
                            .size(w, h)
                            .graphicsLayer { rotationZ = slot.rotation.toFloat() }
                            .clip(RoundedCornerShape(r)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .offset(x = x, y = y)
                            .size(w, h)
                            .graphicsLayer { rotationZ = slot.rotation.toFloat() }
                            .clip(RoundedCornerShape(r))
                            .background(Color.White.copy(alpha = 0.95f)),
                    )
                }
                Surface(
                    modifier = Modifier.offset(x = x + 6.dp, y = y + 6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = slot.slotIndex.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            if (overlayModel != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(overlayModel)
                        .listener(
                            onError = { _, result ->
                                Log.e("TemplateOverlay", "Overlay load gagal: ${result.throwable.message}; url=$overlayModel")
                            },
                        )
                        .build(),
                    contentDescription = "Template overlay",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize(),
                )
            }
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
    BoothStep.SettingAllowedTemplates -> BoothRoute.SettingAllowedTemplates
    BoothStep.VoucherCheck -> BoothRoute.VoucherCheck
    BoothStep.PaymentGate -> BoothRoute.PaymentGate
    BoothStep.WaitingApproval -> BoothRoute.WaitingApproval
}
