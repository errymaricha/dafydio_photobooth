package com.errymaricha.dafydiobooth.ui.launch

import androidx.compose.foundation.background
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import com.errymaricha.dafydiobooth.ui.booth.MockPrintStatus
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.errymaricha.dafydiobooth.domain.model.LaunchEvent
import com.errymaricha.dafydiobooth.domain.model.LaunchPricing
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import java.io.File
import coil3.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameMillis
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.drawscope.rotate
import com.errymaricha.dafydiobooth.domain.model.BoothSession
import com.errymaricha.dafydiobooth.domain.model.PaymentQuote
import com.errymaricha.dafydiobooth.domain.model.PaymentStatus
import com.errymaricha.dafydiobooth.domain.model.VoucherVerification
import com.errymaricha.dafydiobooth.ui.booth.BoothStep
import com.errymaricha.dafydiobooth.ui.booth.preview.PreviewStateProvider
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme
import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.BoothUiState
import com.errymaricha.dafydiobooth.ui.booth.LaunchActions
import com.errymaricha.dafydiobooth.ui.booth.ScreenFrame
import com.errymaricha.dafydiobooth.ui.booth.AndroidCameraCapture
import com.errymaricha.dafydiobooth.ui.booth.CameraSurface
import com.errymaricha.dafydiobooth.ui.booth.CameraSource
import com.errymaricha.dafydiobooth.ui.booth.TemplateSurface

private val launchPreviewEvents = listOf(
    LaunchEvent(
        eventId = "evt-001",
        eventCode = "PROM-001",
        eventName = "Prom Night Dafydio",
        cloudEnabled = true,
        cloudUploadMode = "instant",
        cloudSyncTiming = "after-session",
        cloudTemplateMarketplaceEnabled = true,
    ),
    LaunchEvent(
        eventId = "evt-002",
        eventCode = "WEDD-014",
        eventName = "Rina & Bima Wedding",
        cloudEnabled = true,
        cloudUploadMode = "batched",
        cloudSyncTiming = "hourly",
        cloudTemplateMarketplaceEnabled = false,
    ),
)

private val launchPreviewBoothState = PreviewStateProvider.launchBase.copy(
    step = BoothStep.LaunchEvent,
    stationIp = "10.10.116.4:8000",
    deviceId = "PB-DEVICE-01",
    isStationConnected = true,
    customerWhatsapp = "628123456789",
    voucherCode = "PROMO2026",
    voucherType = "regular",
    sessionType = "photo",
    launchSelectedEventId = "evt-001",
    paymentMethod = "manual",
    eventStatusMessage = "Station synced. Event flow siap dipakai.",
)

private val launchPreviewUiState = LaunchUiState(
    events = launchPreviewEvents,
    selectedEventId = "evt-001",
    eventCodeInput = "PROM-001",
    eventNameInput = "Prom Night Dafydio",
    customerWhatsapp = "628123456789",
    voucherCode = "PROMO2026",
    voucher = VoucherVerification(
        code = "PROMO2026",
        type = "regular",
        isValid = true,
        status = "valid",
        message = "Voucher valid",
        customerName = "Alya",
        remainingUses = 1,
        paymentRequired = true,
        unlockPhoto = false,
    ),
    additionalPrintCount = 2,
    pricing = LaunchPricing(
        photoboothPrice = 35000.0,
        additionalPrintPrice = 7000.0,
        currencyCode = "IDR",
    ),
    quote = PaymentQuote(
        quoteId = "quote-001",
        amount = 49000,
        currency = "IDR",
        paymentRequired = true,
        paymentUrl = "https://station.local/pay/quote-001",
        expiresAt = "2026-05-28T10:15:00Z",
        subtotalAmount = 49000,
        discountAmount = 0,
        unlockPhoto = false,
        discountReason = null,
    ),
    finalAmount = 49000.0,
    message = "Quote berhasil dibuat.",
)

private object LaunchUiTokens {
    val primary = Color(0xFF5B67FF)
    val pink = Color(0xFFFF6B9D)
    val purple = Color(0xFF8B5CF6)
    val success = Color(0xFF4ADE80)
    val warning = Color(0xFFFDBA74)
    val ink = Color(0xFF1E2144)
    val inkSoft = Color(0xFF6E7694)
    val glass = Color(0xF7FFFFFF)
    val softSurface = Color(0xFFF7F8FF)
    val border = Color(0x225B67FF)
}

@Composable
fun LaunchPageRedesign(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
    launchActions: LaunchActions,
    onBackToDashboard: () -> Unit = {},
) {
    LaunchEventNavHostDemo(
        state = state,
        actions = actions,
        launchState = launchState,
        launchActions = launchActions,
        initialConfig = state.toLaunchEventConfig(launchState),
        templates = state.availableTemplateItems.toLaunchTemplates(state.launchAllowedTemplateIds),
        kioskExitCode = state.kioskExitCode,
        welcomeBgUri = state.welcomeBgUri,
        welcomeBgIsVideo = state.welcomeBgIsVideo,
        onBackToDashboard = onBackToDashboard,
    )
}

@Composable
fun VoucherCheckPageRedesign(state: BoothUiState, actions: BoothActions) {
    VoucherCheckScreen(state = state, actions = actions)
}

@Composable
fun PaymentGatePageRedesign(state: BoothUiState, actions: BoothActions) {
    PaymentGateScreen(state = state, actions = actions)
}

@Composable
fun WaitingApprovalPageRedesign(state: BoothUiState, actions: BoothActions) {
    WaitingApprovalScreen(state = state, actions = actions)
}

@Composable
fun LaunchEventScreen(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
    launchActions: LaunchActions,
) {
    val invalidWaMessage = when {
        launchState.message?.startsWith("No WA tidak valid", ignoreCase = true) == true -> launchState.message
        launchState.error?.contains("customer", ignoreCase = true) == true &&
            (launchState.error.contains("invalid", ignoreCase = true) ||
                launchState.error.contains("tidak valid", ignoreCase = true) ||
                launchState.error.contains("not found", ignoreCase = true) ||
                launchState.error.contains("tidak ditemukan", ignoreCase = true) ||
                launchState.error.contains("unregistered", ignoreCase = true) ||
                launchState.error.contains("tidak terdaftar", ignoreCase = true)) -> "No WA tidak valid"
        else -> null
    }
    ScreenFrame(title = "Launch Event", state = state, actions = actions, loading = launchState.loading) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isTablet = maxWidth >= 900.dp
            val activeEvent = launchState.events.firstOrNull { it.eventId == launchState.selectedEventId }
            var showWelcome by remember { mutableStateOf(activeEvent != null) }
            var showForm by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LaunchHeroCard(
                    title = activeEvent?.eventName ?: state.launchEventName.ifBlank { "Launch Event" },
                    subtitle = "Cek setup event, jalankan welcome screen, lalu mulai sesi pembayaran booth.",
                    badge = if (state.isStationReachable) "READY" else "OFFLINE",
                    badgeAccent = if (state.isStationReachable) LaunchUiTokens.success else LaunchUiTokens.warning,
                )
                LaunchInfoRow(
                    "Station" to state.stationIp.ifBlank { "-" },
                    "Event" to (activeEvent?.eventCode ?: "Belum aktif"),
                    "Mode" to "Launch",
                )
                LaunchStatusCard(
                    title = "Event check",
                    message = state.eventStatusMessage ?: "Pastikan station, event aktif, dan welcome screen sudah siap.",
                    accent = when {
                        launchState.error != null -> MaterialTheme.colorScheme.error
                        state.isStationReachable -> LaunchUiTokens.success
                        else -> LaunchUiTokens.warning
                    },
                )

                if (isTablet) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Max)
                    ) {
                        Column(
                            modifier = Modifier.weight(0.95f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            LaunchSectionCard(title = "Event Setup Check") {
                                LaunchStatusPills(
                                    "Station" to if (state.isStationReachable) "Connected" else "Offline",
                                    "Event" to (activeEvent?.eventName ?: "Belum dipilih"),
                                    "Template" to if (state.launchAllowedTemplateIds.isEmpty()) "Semua" else "${state.launchAllowedTemplateIds.size} selected",
                                )
                                launchState.pricing?.let { pricing ->
                                    LaunchMetricRow(
                                        "Photo" to "${pricing.currencyCode} ${pricing.photoboothPrice.toLong()}",
                                        "Print +" to "${pricing.currencyCode} ${pricing.additionalPrintPrice.toLong()}",
                                        "Total" to "${pricing.currencyCode} ${launchState.finalAmount.toLong()}",
                                    )
                                }
                                LaunchPrimaryButton(
                                    text = "Run Launch Event",
                                    onClick = { showWelcome = true },
                                    enabled = state.isStationReachable && activeEvent != null,
                                )
                            }
                            if (showForm) {
                                LaunchSectionCard(title = "Guest Input") {
                                    OutlinedTextField(
                                        value = launchState.customerWhatsapp,
                                        onValueChange = launchActions.onWhatsappChanged,
                                        label = { Text("No WA") },
                                        placeholder = { Text("628123456789") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = invalidWaMessage != null,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    if (invalidWaMessage != null) {
                                        Text(invalidWaMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    }
                                    OutlinedTextField(
                                        value = launchState.voucherCode,
                                        onValueChange = launchActions.onVoucherCodeChanged,
                                        label = { Text("Voucher") },
                                        placeholder = { Text("Opsional") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    LaunchActionRow {
                                        LaunchSecondaryButton(
                                            text = "Cek Voucher",
                                            onClick = launchActions.checkVoucherAndQuote,
                                            enabled = !launchState.loading && launchState.voucherCode.isNotBlank(),
                                            modifier = Modifier.weight(1f),
                                        )
                                        LaunchPrimaryButton(
                                            text = "Manual Payment",
                                            onClick = launchActions.submitManualPaymentRequest,
                                            enabled = launchState.canSubmitManualPayment,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1.05f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            LaunchSectionCard(title = "Welcome Screen Event") {
                                WelcomeLaunchPreviewCard(
                                    title = activeEvent?.eventName ?: "Welcome Event",
                                    subtitle = "Smile, pose, and make your memory.",
                                    startLabel = if (showForm) "Form Active" else "Mulai",
                                )
                                if (showWelcome) {
                                    LaunchPrimaryButton(
                                        text = "Mulai",
                                        onClick = { showForm = true },
                                        enabled = state.isStationReachable && activeEvent != null,
                                    )
                                }
                            }
                            if (showForm) {
                                LaunchSectionCard(title = "Payment Gateway") {
                                    launchState.quote?.let { quote ->
                                        LaunchMetricRow(
                                            "Subtotal" to "${quote.currency} ${quote.subtotalAmount ?: launchState.finalAmount.toLong()}",
                                            "Diskon" to "${quote.currency} ${quote.discountAmount ?: 0}",
                                            "Total bayar" to "${quote.currency} ${quote.amount}",
                                        )
                                    }
                                    PaymentMethodChips(
                                        currentMethod = state.paymentMethod,
                                        onSelectMethod = actions.updatePaymentMethod,
                                    )
                                    LaunchActionRow {
                                        LaunchSecondaryButton(
                                            text = "Buat Quote",
                                            onClick = actions.requestQuote,
                                            enabled = !state.isLoading,
                                            modifier = Modifier.weight(1f),
                                        )
                                        LaunchSecondaryButton(
                                            text = "Check Approval",
                                            onClick = actions.checkPayment,
                                            enabled = !state.isLoading && state.session != null,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LaunchSectionCard(title = "Event Setup Check") {
                        LaunchStatusPills(
                            "Station" to if (state.isStationReachable) "Connected" else "Offline",
                            "Event" to (activeEvent?.eventName ?: "Belum dipilih"),
                            "Template" to if (state.launchAllowedTemplateIds.isEmpty()) "Semua" else "${state.launchAllowedTemplateIds.size} selected",
                        )
                        launchState.pricing?.let { pricing ->
                            LaunchMetricRow(
                                "Photo" to "${pricing.currencyCode} ${pricing.photoboothPrice.toLong()}",
                                "Print +" to "${pricing.currencyCode} ${pricing.additionalPrintPrice.toLong()}",
                                "Total" to "${pricing.currencyCode} ${launchState.finalAmount.toLong()}",
                            )
                        }
                        LaunchPrimaryButton(
                            text = "Run Launch Event",
                            onClick = { showWelcome = true },
                            enabled = state.isStationReachable && activeEvent != null,
                        )
                    }
                    if (showWelcome) {
                        LaunchSectionCard(title = "Welcome Screen Event") {
                            WelcomeLaunchPreviewCard(
                                title = activeEvent?.eventName ?: "Welcome Event",
                                subtitle = "Smile, pose, and make your memory.",
                                startLabel = if (showForm) "Form Active" else "Mulai",
                            )
                            LaunchPrimaryButton(
                                text = "Mulai",
                                onClick = { showForm = true },
                                enabled = state.isStationReachable && activeEvent != null,
                            )
                        }
                    }
                    if (showForm) {
                        LaunchSectionCard(title = "Guest Input") {
                            OutlinedTextField(
                                value = launchState.customerWhatsapp,
                                onValueChange = launchActions.onWhatsappChanged,
                                label = { Text("No WA") },
                                placeholder = { Text("628123456789") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = invalidWaMessage != null,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (invalidWaMessage != null) {
                                Text(invalidWaMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                            OutlinedTextField(
                                value = launchState.voucherCode,
                                onValueChange = launchActions.onVoucherCodeChanged,
                                label = { Text("Voucher") },
                                placeholder = { Text("Opsional") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            LaunchActionRow {
                                LaunchSecondaryButton(
                                    text = "Cek Voucher",
                                    onClick = launchActions.checkVoucherAndQuote,
                                    enabled = !launchState.loading && launchState.voucherCode.isNotBlank(),
                                    modifier = Modifier.weight(1f),
                                )
                                LaunchPrimaryButton(
                                    text = "Manual Payment",
                                    onClick = launchActions.submitManualPaymentRequest,
                                    enabled = launchState.canSubmitManualPayment,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        LaunchSectionCard(title = "Payment Gateway") {
                            launchState.quote?.let { quote ->
                                LaunchMetricRow(
                                    "Subtotal" to "${quote.currency} ${quote.subtotalAmount ?: launchState.finalAmount.toLong()}",
                                    "Diskon" to "${quote.currency} ${quote.discountAmount ?: 0}",
                                    "Total bayar" to "${quote.currency} ${quote.amount}",
                                )
                            }
                            PaymentMethodChips(
                                currentMethod = state.paymentMethod,
                                onSelectMethod = actions.updatePaymentMethod,
                            )
                            LaunchActionRow {
                                LaunchSecondaryButton(
                                    text = "Buat Quote",
                                    onClick = actions.requestQuote,
                                    enabled = !state.isLoading,
                                    modifier = Modifier.weight(1f),
                                )
                                LaunchSecondaryButton(
                                    text = "Check Approval",
                                    onClick = actions.checkPayment,
                                    enabled = !state.isLoading && state.session != null,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
                launchState.error?.takeIf { invalidWaMessage == null }?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}


@Composable
fun VoucherCheckScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Voucher Check", state = state, actions = actions) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isTablet = maxWidth >= 900.dp
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LaunchStatusCard(
                    title = "Voucher flow",
                    message = state.eventStatusMessage ?: "Verifikasi voucher sebelum sesi dilanjutkan.",
                    accent = LaunchUiTokens.primary,
                )
                LaunchHeroCard(
                    title = "Voucher Check",
                    subtitle = "Cek voucher sebelum lanjut ke sesi pembayaran event.",
                    badge = "STEP 1",
                    badgeAccent = LaunchUiTokens.primary,
                )
                if (isTablet) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Max)
                    ) {
                        LaunchSectionCard(
                            title = "Voucher Input",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            OutlinedTextField(value = state.voucherCode, onValueChange = actions.updateVoucherCode, label = { Text("Voucher Code") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = state.voucherType, onValueChange = actions.updateVoucherType, label = { Text("Voucher Type") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = state.sessionType, onValueChange = actions.updateSessionType, label = { Text("Session Type") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                        LaunchSectionCard(
                            title = "Action",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            LaunchStatusPills(
                                "Voucher" to state.voucherCode.ifBlank { "-" },
                                "Type" to state.voucherType.ifBlank { "-" },
                                "Session" to state.sessionType.ifBlank { "-" },
                            )
                            LaunchSecondaryButton(text = "No Voucher", onClick = actions.continueWithoutVoucher, enabled = !state.isLoading)
                            LaunchPrimaryButton(text = "Verify", onClick = actions.verifyVoucher, enabled = !state.isLoading)
                        }
                    }
                } else {
                    LaunchSectionCard(title = "Voucher Input") {
                        OutlinedTextField(value = state.voucherCode, onValueChange = actions.updateVoucherCode, label = { Text("Voucher Code") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = state.voucherType, onValueChange = actions.updateVoucherType, label = { Text("Voucher Type") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = state.sessionType, onValueChange = actions.updateSessionType, label = { Text("Session Type") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    LaunchActionRow {
                        LaunchSecondaryButton(text = "No Voucher", onClick = actions.continueWithoutVoucher, enabled = !state.isLoading, modifier = Modifier.weight(1f))
                        LaunchPrimaryButton(text = "Verify", onClick = actions.verifyVoucher, enabled = !state.isLoading, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


@Composable
fun PaymentGateScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Payment Gate", state = state, actions = actions) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isTablet = maxWidth >= 900.dp
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LaunchStatusCard(
                    title = "Payment flow",
                    message = state.eventStatusMessage ?: "Pilih metode bayar dan buat session pembayaran.",
                    accent = LaunchUiTokens.pink,
                )
                LaunchHeroCard(
                    title = "Payment Gate",
                    subtitle = "Pilih metode bayar dan lanjutkan session event.",
                    badge = "STEP 2",
                    badgeAccent = LaunchUiTokens.pink,
                )
                if (isTablet) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Max)
                    ) {
                        LaunchSectionCard(
                            title = "Payment Summary",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            LaunchMetricRow(
                                "Voucher" to (state.voucher?.code ?: state.voucherCode.ifBlank { "-" }),
                                "Type" to (state.voucher?.type ?: state.voucherType.ifBlank { "-" }),
                                "Amount" to "${state.quote?.currency ?: "IDR"} ${state.quote?.amount ?: 0}",
                            )
                            LaunchStatusPills(
                                "Payment required" to "${state.quote?.paymentRequired ?: true}",
                                "Unlock photo" to "${state.quote?.unlockPhoto ?: false}",
                            )
                            Text("Payment Method", style = MaterialTheme.typography.labelLarge)
                            PaymentMethodChips(
                                currentMethod = state.paymentMethod,
                                onSelectMethod = actions.updatePaymentMethod,
                            )
                        }
                        LaunchSectionCard(
                            title = "Action",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            LaunchSecondaryButton(text = "Quote", onClick = actions.requestQuote, enabled = !state.isLoading)
                            LaunchPrimaryButton(
                                text = "Manual Payment",
                                onClick = actions.createManualPaymentSession,
                                enabled = !state.isLoading && state.launchSelectedEventId.isNotBlank(),
                            )
                            LaunchSecondaryButton(
                                text = "Continue Without Payment",
                                onClick = actions.continueAfterFreeQuote,
                                enabled = state.quote?.paymentRequired == false && !state.isLoading,
                            )
                        }
                    }
                } else {
                    LaunchSectionCard(title = "Payment Summary") {
                        LaunchMetricRow(
                            "Voucher" to (state.voucher?.code ?: state.voucherCode.ifBlank { "-" }),
                            "Type" to (state.voucher?.type ?: state.voucherType.ifBlank { "-" }),
                            "Amount" to "${state.quote?.currency ?: "IDR"} ${state.quote?.amount ?: 0}",
                        )
                        LaunchStatusPills(
                            "Payment required" to "${state.quote?.paymentRequired ?: true}",
                            "Unlock photo" to "${state.quote?.unlockPhoto ?: false}",
                        )
                        Text("Payment Method", style = MaterialTheme.typography.labelLarge)
                        PaymentMethodChips(
                            currentMethod = state.paymentMethod,
                            onSelectMethod = actions.updatePaymentMethod,
                        )
                    }
                    LaunchSectionCard(title = "Action") {
                        LaunchActionRow {
                            LaunchSecondaryButton(text = "Quote", onClick = actions.requestQuote, enabled = !state.isLoading, modifier = Modifier.weight(1f))
                            LaunchPrimaryButton(
                                text = "Manual Payment",
                                onClick = actions.createManualPaymentSession,
                                enabled = !state.isLoading && state.launchSelectedEventId.isNotBlank(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        LaunchSecondaryButton(
                            text = "Continue Without Payment",
                            onClick = actions.continueAfterFreeQuote,
                            enabled = state.quote?.paymentRequired == false && !state.isLoading,
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun WaitingApprovalScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Waiting Approval", state = state, actions = actions) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isTablet = maxWidth >= 900.dp
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LaunchStatusCard(
                    title = "Approval flow",
                    message = state.eventStatusMessage ?: "Approval manual dilakukan dari Photobooth Station.",
                    accent = LaunchUiTokens.warning,
                )
                LaunchHeroCard(
                    title = "Waiting Approval",
                    subtitle = "Approval manual dilakukan dari Photobooth Station.",
                    badge = "STEP 3",
                    badgeAccent = LaunchUiTokens.warning,
                )
                if (isTablet) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Max)
                    ) {
                        LaunchSectionCard(
                            title = "Status Approval",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            LaunchMetricRow(
                                "Kode Session" to (state.session?.sessionCode ?: "-"),
                                "Session ID" to (state.session?.sessionId ?: "-"),
                                "Payment" to (state.paymentStatus?.paymentStatus ?: state.session?.paymentStatus ?: "pending"),
                            )
                        }
                        LaunchSectionCard(
                            title = "Action",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            LaunchStatusPills(
                                "Review" to (state.paymentStatus?.reviewStatus ?: "-"),
                                "Approval" to (state.paymentStatus?.approvalStatus ?: "-"),
                            )
                            LaunchPrimaryButton(
                                text = "Check Approval",
                                onClick = actions.checkPayment,
                                enabled = !state.isLoading,
                            )
                        }
                    }
                } else {
                    LaunchSectionCard(title = "Status Approval") {
                        LaunchMetricRow(
                            "Kode Session" to (state.session?.sessionCode ?: "-"),
                            "Session ID" to (state.session?.sessionId ?: "-"),
                            "Payment" to (state.paymentStatus?.paymentStatus ?: state.session?.paymentStatus ?: "pending"),
                        )
                    }
                    LaunchPrimaryButton(
                        text = "Check Approval",
                        onClick = actions.checkPayment,
                        enabled = !state.isLoading,
                    )
                }
            }
        }
    }
}


@Composable
private fun LaunchHeroCard(title: String, subtitle: String, badge: String, badgeAccent: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFEEF2FF), Color(0xFFFFF0F7)),
                    ),
                )
                .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(28.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = LaunchUiTokens.ink, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(999.dp), color = badgeAccent.copy(alpha = 0.14f)) {
                    Text(badge, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = badgeAccent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.inkSoft)
        }
    }
}

@Composable
private fun LaunchSectionCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = LaunchUiTokens.glass,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(colors = listOf(Color.White, LaunchUiTokens.softSurface)))
                .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(24.dp))
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                Text(title, fontWeight = FontWeight.Bold, color = LaunchUiTokens.ink)
                content()
            },
        )
    }
}



@Composable
private fun LaunchStatusCard(title: String, message: String, accent: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = LaunchUiTokens.glass,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.10f),
                            Color.White,
                        ),
                    ),
                )
                .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = accent.copy(alpha = 0.14f),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = accent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(message, color = LaunchUiTokens.ink, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun LaunchEventPreviewCard(title: String, code: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .background(Brush.verticalGradient(colors = listOf(LaunchUiTokens.primary.copy(alpha = 0.16f), LaunchUiTokens.pink.copy(alpha = 0.10f), Color.White)))
                .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(shape = RoundedCornerShape(999.dp), color = LaunchUiTokens.primary.copy(alpha = 0.14f)) {
                Text(code, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = LaunchUiTokens.primary, fontWeight = FontWeight.SemiBold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = LaunchUiTokens.ink, fontWeight = FontWeight.Bold)
                Text("Session launch siap dipakai untuk event aktif.", style = MaterialTheme.typography.bodySmall, color = LaunchUiTokens.inkSoft)
            }
        }
    }
}

@Composable
private fun WelcomeLaunchPreviewCard(
    title: String,
    subtitle: String,
    startLabel: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            LaunchUiTokens.primary.copy(alpha = 0.22f),
                            LaunchUiTokens.pink.copy(alpha = 0.18f),
                            Color.White,
                        ),
                    ),
                )
                .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(22.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            LaunchStatusPills(
                "Welcome" to "Active",
                "Countdown" to "3s",
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LaunchUiTokens.ink,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LaunchUiTokens.inkSoft,
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.82f),
            ) {
                Text(
                    text = startLabel,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = LaunchUiTokens.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun LaunchPrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = if (enabled) listOf(LaunchUiTokens.primary, LaunchUiTokens.pink) else listOf(LaunchUiTokens.inkSoft.copy(alpha = 0.3f), LaunchUiTokens.inkSoft.copy(alpha = 0.2f)),
                    ),
                    RoundedCornerShape(22.dp),
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LaunchSecondaryButton(text: String, onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = if (enabled) 0.88f else 0.5f),
            contentColor = LaunchUiTokens.ink,
            disabledContentColor = LaunchUiTokens.inkSoft,
            disabledContainerColor = Color.White.copy(alpha = 0.5f),
        ),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LaunchInfoRow(first: Pair<String, String>, second: Pair<String, String>, third: Pair<String, String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        listOf(first, second, third).forEach { item ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 6.dp,
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(item.first, style = MaterialTheme.typography.labelSmall, color = LaunchUiTokens.inkSoft)
                    Text(item.second, style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.ink, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}



@Composable
private fun LaunchMetricRow(vararg items: Pair<String, String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 3.dp,
            ) {
                Column(
                    modifier = Modifier
                        .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(18.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(item.first, style = MaterialTheme.typography.labelSmall, color = LaunchUiTokens.inkSoft)
                    Text(item.second, style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.ink, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun LaunchStatusPills(vararg items: Pair<String, String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items.forEach { item ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = LaunchUiTokens.primary.copy(alpha = 0.10f),
            ) {
                Text(
                    text = "${item.first}: ${item.second}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = LaunchUiTokens.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun LaunchActionRow(content: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
        content = content,
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun PaymentMethodChips(
    currentMethod: String,
    onSelectMethod: (String) -> Unit,
) {
    val current = currentMethod.trim().lowercase()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        FilterChip(
            selected = current == "manual",
            onClick = { onSelectMethod("manual") },
            label = { Text("Manual") },
        )
        FilterChip(
            selected = current == "qris",
            onClick = { onSelectMethod("qris") },
            label = { Text("QRIS") },
        )
    }
}

enum class LaunchPaymentOption(val label: String) {
    Manual("Manual / Cash"),
    Qris("QRIS"),
    Free("Free / Voucher"),
}

data class VoucherConfig(
    val code: String,
    val label: String,
    val discountLabel: String,
)

data class WelcomeConfig(
    val backgroundLabel: String,
    val title: String,
    val subtitle: String,
    val buttonLabel: String,
    val buttonColor: Color,
)

data class PaymentConfig(
    val defaultWhatsapp: String,
    val defaultVoucher: VoucherConfig?,
    val availablePaymentTypes: List<LaunchPaymentOption>,
)

data class OrderSummary(
    val eventName: String,
    val packageName: String,
    val priceLabel: String,
    val discountLabel: String,
    val totalLabel: String,
)

data class PhotoTemplate(
    val id: String,
    val name: String,
    val sizeLabel: String,
    val frameCount: Int,
    val accent: Color,
)

data class EventLaunchConfig(
    val eventName: String,
    val eventDate: String,
    val statusLabel: String,
    val packageLabel: String,
    val pricingLabel: String,
    val defaultTemplateLabel: String,
    val welcomeConfig: WelcomeConfig,
    val paymentConfig: PaymentConfig,
    val orderSummary: OrderSummary,
    val checklist: List<String>,
)

sealed class LaunchEventRoute(val route: String) {
    data object PraLaunch : LaunchEventRoute("pra_launch_event")
    data object Welcome : LaunchEventRoute("event_welcome")
    data object Payment : LaunchEventRoute("payment")
    data object PickTemplate : LaunchEventRoute("pick_template")
    data object Capture : LaunchEventRoute("capture")
    data object Finished : LaunchEventRoute("capture_finished")
}

private val sampleLaunchConfig = EventLaunchConfig(
    eventName = "Prom Night Dafydio",
    eventDate = "29 Mei 2026",
    statusLabel = "Active",
    packageLabel = "Paket Premium Event",
    pricingLabel = "IDR 49.000",
    defaultTemplateLabel = "Mirror Pop",
    welcomeConfig = WelcomeConfig(
        backgroundLabel = "Backdrop gradient pink-blue",
        title = "Welcome to Prom Night",
        subtitle = "Ambil momen terbaikmu malam ini.",
        buttonLabel = "Mulai",
        buttonColor = LaunchUiTokens.primary,
    ),
    paymentConfig = PaymentConfig(
        defaultWhatsapp = "628123456789",
        defaultVoucher = VoucherConfig(
            code = "PROMO2026",
            label = "Voucher Prom 2026",
            discountLabel = "IDR 10.000",
        ),
        availablePaymentTypes = listOf(
            LaunchPaymentOption.Manual,
            LaunchPaymentOption.Qris,
            LaunchPaymentOption.Free,
        ),
    ),
    orderSummary = OrderSummary(
        eventName = "Prom Night Dafydio",
        packageName = "Paket Premium Event",
        priceLabel = "IDR 49.000",
        discountLabel = "IDR 10.000",
        totalLabel = "IDR 39.000",
    ),
    checklist = listOf(
        "Event aktif sudah dipilih",
        "Welcome screen sudah tersimpan",
        "Template event sudah tersedia",
        "Pembayaran event sudah aktif",
    ),
)

private val sampleTemplates = listOf(
    PhotoTemplate("tpl-01", "Mirror Pop", "4R Strip", 4, LaunchUiTokens.pink),
    PhotoTemplate("tpl-02", "Glow Frame", "4R", 3, LaunchUiTokens.primary),
    PhotoTemplate("tpl-03", "After Party", "2x6 Strip", 4, LaunchUiTokens.warning),
    PhotoTemplate("tpl-04", "Soft Flash", "4R", 2, LaunchUiTokens.success),
)


@Composable
fun LaunchEventNavHostDemo(
    state: BoothUiState = BoothUiState(),
    actions: BoothActions = BoothActions(),
    launchState: LaunchUiState = LaunchUiState(),
    launchActions: LaunchActions = LaunchActions(),
    modifier: Modifier = Modifier,
    initialConfig: EventLaunchConfig = sampleLaunchConfig,
    templates: List<PhotoTemplate> = sampleTemplates,
    kioskExitCode: String = "",
    welcomeBgUri: String = "",
    welcomeBgIsVideo: Boolean = false,
    onBackToDashboard: () -> Unit = {},
) {
    val navController = rememberNavController()
    var selectedTemplate by remember { mutableStateOf(templates.firstOrNull() ?: sampleTemplates.first()) }

    LaunchedEffect(launchState.shouldNavigateToTemplates) {
        if (launchState.shouldNavigateToTemplates) {
            navController.navigate(LaunchEventRoute.PickTemplate.route)
            launchActions.consumeTemplateNavigation()
        }
    }

    NavHost(
        navController = navController,
        startDestination = LaunchEventRoute.PraLaunch.route,
        modifier = modifier,
    ) {
        composable(LaunchEventRoute.PraLaunch.route) {
            PraLaunchEventScreen(
                config = initialConfig,
                onRunEventClick = {
                    launchActions.resetSessionState()
                    navController.navigate(LaunchEventRoute.Welcome.route)
                },
            )
        }
        composable(LaunchEventRoute.Welcome.route) {
            EventWelcomeScreen(
                config = initialConfig.welcomeConfig,
                eventName = initialConfig.eventName,
                onStartClick = { navController.navigate(LaunchEventRoute.Payment.route) },
                kioskExitCode = kioskExitCode,
                welcomeBgUri = welcomeBgUri,
                welcomeBgIsVideo = welcomeBgIsVideo,
                onExitToDashboard = onBackToDashboard,
                onStartSession = { navController.navigate(LaunchEventRoute.Payment.route) },
            )
        }
        composable(LaunchEventRoute.Payment.route) {
            if (launchState.isManualPaymentWaiting) {
                KioskWaitingApprovalScreen(
                    launchState = launchState,
                    launchActions = launchActions,
                    kioskExitCode = kioskExitCode,
                    onExitToDashboard = onBackToDashboard
                )
            } else {
                PaymentScreen(
                    config = initialConfig,
                    state = state,
                    launchState = launchState,
                    launchActions = launchActions,
                    onPaymentConfirmed = {
                        launchActions.submitManualPaymentRequest()
                    },
                )
            }
        }
        composable(LaunchEventRoute.PickTemplate.route) {
            PickTemplateScreen(
                eventName = initialConfig.eventName,
                templates = templates,
                onTemplateSelected = {
                    selectedTemplate = it
                    actions.selectTemplate(it.id)
                    navController.navigate(LaunchEventRoute.Capture.route)
                },
            )
        }
        composable(LaunchEventRoute.Capture.route) {
            CaptureScreen(
                eventName = initialConfig.eventName,
                template = selectedTemplate,
                state = state,
                actions = actions,
                onCaptureFinished = { navController.navigate(LaunchEventRoute.Finished.route) },
                kioskExitCode = kioskExitCode,
                onExitToDashboard = onBackToDashboard,
            )
        }
        composable(LaunchEventRoute.Finished.route) {
            CaptureFinishedScreen(
                eventName = initialConfig.eventName,
                template = selectedTemplate,
                sessionCode = state.session?.sessionCode ?: launchState.session?.sessionCode ?: "SES-DEFAULT",
                kioskExitCode = kioskExitCode,
                capturedPhotos = state.capturedPhotosBySlot.values.toList(),
                onPrintClick = {
                    actions.triggerMockPrint()
                },
                printUsePhotoboothStation = state.printUsePhotoboothStation,
                isStationConnected = state.isStationConnected,
                mockPrintStatus = state.mockPrintStatus,
                mockPrintMessage = state.mockPrintMessage,
                previewContent = {
                    TemplateSurface(state = state, modifier = Modifier.fillMaxSize())
                },
                onBackToWelcome = {
                    actions.newSession()
                    launchActions.resetSessionState()
                    navController.navigate(LaunchEventRoute.Welcome.route) {
                        popUpTo(LaunchEventRoute.Welcome.route) { inclusive = true }
                    }
                },
                onStartNewSession = {
                    actions.newSession()
                    launchActions.resetSessionState()
                    navController.navigate(LaunchEventRoute.Payment.route) {
                        popUpTo(LaunchEventRoute.Payment.route) { inclusive = true }
                    }
                },
                onExitToDashboard = {
                    actions.newSession()
                    launchActions.resetSessionState()
                    onBackToDashboard()
                },
            )
        }
    }
}

private fun BoothUiState.toLaunchEventConfig(launchState: LaunchUiState): EventLaunchConfig {
    val activeEvent = launchState.events.firstOrNull { it.eventId == launchState.selectedEventId }
    val activeTemplate = availableTemplateItems.firstOrNull()
    val paymentOptions = buildList {
        add(LaunchPaymentOption.Manual)
        add(LaunchPaymentOption.Qris)
        if (voucherCode.isNotBlank() || voucher != null) add(LaunchPaymentOption.Free)
    }.distinct()

    return EventLaunchConfig(
        eventName = activeEvent?.eventName ?: launchEventName.ifBlank { sampleLaunchConfig.eventName },
        eventDate = sampleLaunchConfig.eventDate,
        statusLabel = when {
            activeEvent == null -> "Belum dipilih"
            isStationConnected -> "Active"
            else -> "Offline"
        },
        packageLabel = launchState.pricing?.let { "Photo ${it.currencyCode} ${it.photoboothPrice.toLong()}" }
            ?: sampleLaunchConfig.packageLabel,
        pricingLabel = launchState.quote?.let { "${it.currency} ${it.amount}" }
            ?: launchState.finalAmount.takeIf { it > 0 }?.let { "IDR ${it.toLong()}" }
            ?: sampleLaunchConfig.pricingLabel,
        defaultTemplateLabel = activeTemplate?.templateName ?: sampleLaunchConfig.defaultTemplateLabel,
        welcomeConfig = WelcomeConfig(
            backgroundLabel = "Background welcome event",
            title = activeEvent?.eventName ?: sampleLaunchConfig.welcomeConfig.title,
            subtitle = "Tap mulai untuk masuk ke sesi photobooth.",
            buttonLabel = "Mulai",
            buttonColor = LaunchUiTokens.primary,
        ),
        paymentConfig = PaymentConfig(
            defaultWhatsapp = customerWhatsapp.ifBlank { sampleLaunchConfig.paymentConfig.defaultWhatsapp },
            defaultVoucher = (voucher?.code ?: voucherCode).takeIf { !it.isNullOrBlank() }?.let {
                VoucherConfig(
                    code = it,
                    label = voucher?.message ?: "Voucher default",
                    discountLabel = launchState.quote?.discountAmount?.let { amount -> "IDR $amount" } ?: "Auto",
                )
            },
            availablePaymentTypes = paymentOptions.ifEmpty { sampleLaunchConfig.paymentConfig.availablePaymentTypes },
        ),
        orderSummary = OrderSummary(
            eventName = activeEvent?.eventName ?: sampleLaunchConfig.orderSummary.eventName,
            packageName = launchState.pricing?.let { "Paket Booth" } ?: sampleLaunchConfig.orderSummary.packageName,
            priceLabel = launchState.quote?.subtotalAmount?.let { "IDR $it" } ?: sampleLaunchConfig.orderSummary.priceLabel,
            discountLabel = launchState.quote?.discountAmount?.let { "IDR $it" } ?: sampleLaunchConfig.orderSummary.discountLabel,
            totalLabel = launchState.quote?.amount?.let { "IDR $it" }
                ?: launchState.finalAmount.takeIf { it > 0 }?.let { "IDR ${it.toLong()}" }
                ?: sampleLaunchConfig.orderSummary.totalLabel,
        ),
        checklist = listOf(
            if (isStationConnected) "Station terhubung" else "Station belum terhubung",
            if (activeEvent != null) "Event aktif dipilih" else "Pilih event aktif",
            if (availableTemplateItems.isNotEmpty()) "Template tersedia" else "Template belum tersedia",
            if (kioskExitCode.isNotBlank()) "Kode keluar kiosk sudah diatur" else "Kode keluar kiosk belum diatur",
        ),
    )
}

private fun List<com.errymaricha.dafydiobooth.ui.booth.TemplateListItem>.toLaunchTemplates(allowedIds: Set<String>): List<PhotoTemplate> {
    val filtered = if (allowedIds.isEmpty()) this else this.filter { it.templateId in allowedIds }
    if (filtered.isEmpty()) return sampleTemplates
    return filtered.map { template ->
        PhotoTemplate(
            id = template.templateId,
            name = template.templateName,
            sizeLabel = template.paperSize ?: "-",
            frameCount = template.slotCount,
            accent = when ((template.category ?: "").lowercase()) {
                "birthday", "party" -> LaunchUiTokens.pink
                "wedding" -> LaunchUiTokens.purple
                else -> LaunchUiTokens.primary
            },
        )
    }
}

@Composable
private fun StationRadarAnimation(
    modifier: Modifier = Modifier,
    isOnline: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "Radar")
    val radarScale1 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarScale1"
    )
    val radarAlpha1 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha1"
    )

    val radarScale2 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarScale2"
    )
    val radarAlpha2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha2"
    )

    val radarColor = if (isOnline) LaunchUiTokens.success else LaunchUiTokens.warning

    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = radarScale1
                    scaleY = radarScale1
                    alpha = radarAlpha1
                }
                .border(2.dp, radarColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = radarScale2
                    scaleY = radarScale2
                    alpha = radarAlpha2
                }
                .border(2.dp, radarColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(radarColor.copy(alpha = 0.15f), CircleShape)
                .border(2.dp, radarColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isOnline) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = "Radar Status",
                tint = radarColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun PraLaunchEventScreen(
    config: EventLaunchConfig,
    onRunEventClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LaunchUiTokens.softSurface)
            .safeDrawingPadding(),
    ) {
        val isTablet = maxWidth >= 900.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isTablet) 32.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Event Control Center",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = LaunchUiTokens.ink
                    )
                    Text(
                        text = "Review event details and launch the kiosk station.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LaunchUiTokens.inkSoft
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = LaunchUiTokens.success.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, LaunchUiTokens.success.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = config.statusLabel.uppercase(),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        color = LaunchUiTokens.success,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (isTablet) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1.1f).fillMaxHeight(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, LaunchUiTokens.border)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Station Connection",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = LaunchUiTokens.ink,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            
                            StationRadarAnimation(
                                modifier = Modifier.size(76.dp),
                                isOnline = true
                            )
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Photobooth Station Active",
                                    color = LaunchUiTokens.success,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Device is paired with station server.",
                                    color = LaunchUiTokens.inkSoft,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(LaunchUiTokens.border))
                            
                            CompactEventDetailsCard(config)
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, LaunchUiTokens.border)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                TransactionConfigCard(config)
                                ChecklistCard(config.checklist)
                            }
                        }

                        Button(
                            onClick = onRunEventClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.horizontalGradient(listOf(LaunchUiTokens.primary, LaunchUiTokens.pink)),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LaunchUiTokens.primary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                LaunchUiTokens.primary,
                                                LaunchUiTokens.pink
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "RUN KIOSK EVENT",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, LaunchUiTokens.border)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StationRadarAnimation(isOnline = true)
                            Text("Station Active", color = LaunchUiTokens.success, fontWeight = FontWeight.Bold)
                        }
                    }
                    EventInfoCard(config)
                    WelcomeConfigCard(config.welcomeConfig, config.defaultTemplateLabel)
                    TransactionConfigCard(config)
                    ChecklistCard(config.checklist)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Button(
                        onClick = onRunEventClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(listOf(LaunchUiTokens.primary, LaunchUiTokens.pink)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = LaunchUiTokens.primary),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(LaunchUiTokens.primary, LaunchUiTokens.pink))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "RUN KIOSK EVENT",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KioskDialpad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("Clear", "0", "Backspace")
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keys.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowKeys.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(2.3f)
                            .background(
                                color = if (key == "Backspace" || key == "Clear") {
                                    LaunchUiTokens.primary.copy(alpha = 0.08f)
                                } else {
                                    Color.White
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (key == "Backspace" || key == "Clear") {
                                    LaunchUiTokens.primary.copy(alpha = 0.15f)
                                } else {
                                    LaunchUiTokens.border
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                when (key) {
                                    "Backspace" -> onBackspaceClick()
                                    "Clear" -> onClearClick()
                                    else -> onDigitClick(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            "Backspace" -> {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = LaunchUiTokens.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            "Clear" -> {
                                Text(
                                    text = "CLR",
                                    color = LaunchUiTokens.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            else -> {
                                Text(
                                    text = key,
                                    color = LaunchUiTokens.ink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeVideoPlayer(
    filePath: String,
    modifier: Modifier = Modifier,
    isMuted: Boolean = true
) {
    val context = LocalContext.current
    val fileExists = remember(filePath) {
        if (filePath.startsWith("content://")) {
            try {
                context.contentResolver.openAssetFileDescriptor(Uri.parse(filePath), "r")?.use { }
                true
            } catch (e: Exception) {
                false
            }
        } else {
            File(filePath).exists()
        }
    }
    if (!fileExists) {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Video looping tidak ditemukan", color = Color.White)
        }
        return
    }
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    if (isMuted) {
                        mp.setVolume(0f, 0f)
                    } else {
                        mp.setVolume(1f, 1f)
                    }
                    mp.start()
                }
            }
        },
        update = { videoView ->
            try {
                val currentTag = videoView.tag as? String
                if (currentTag != filePath) {
                    videoView.tag = filePath
                    if (filePath.startsWith("content://")) {
                        videoView.setVideoURI(Uri.parse(filePath))
                    } else {
                        videoView.setVideoPath(filePath)
                    }
                    videoView.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        modifier = modifier
    )
}

@Composable
fun EventWelcomeScreen(
    config: WelcomeConfig,
    eventName: String,
    onStartClick: () -> Unit,
    kioskExitCode: String = "123456",
    onExitToDashboard: () -> Unit = {},
    onStartSession: (() -> Unit)? = null,
    welcomeBgUri: String = "",
    welcomeBgIsVideo: Boolean = false,
    welcomeBgIsMuted: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Warna overlay nav bar — putih jika ada bg, primary jika tidak
    val hasBg = welcomeBgUri.isNotBlank()
    val navSurfaceColor = if (hasBg) Color.Black.copy(alpha = 0.35f) else LaunchUiTokens.primary.copy(alpha = 0.10f)
    val navContentColor = if (hasBg) Color.White else LaunchUiTokens.ink

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // ── Background ──────────────────────────────────────────────
        if (hasBg) {
            if (welcomeBgIsVideo) {
                WelcomeVideoPlayer(
                    filePath = welcomeBgUri,
                    isMuted = welcomeBgIsMuted,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = if (welcomeBgUri.startsWith("content://")) Uri.parse(welcomeBgUri) else File(welcomeBgUri),
                    contentDescription = "Welcome Background Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                LaunchUiTokens.primary.copy(alpha = 0.24f),
                                LaunchUiTokens.pink.copy(alpha = 0.18f),
                                Color.White,
                            ),
                        ),
                    )
            )
        }

        // ── Main content ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.78f),
                ) {
                    Text(
                        text = eventName,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = LaunchUiTokens.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = config.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (hasBg) Color.White else LaunchUiTokens.ink,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = config.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (hasBg) Color.White.copy(alpha = 0.85f) else LaunchUiTokens.inkSoft,
                )
                Surface(
                    onClick = onStartClick,
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 48.dp, vertical = 18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(config.buttonColor, RoundedCornerShape(999.dp))
                        )
                        Text(
                            text = config.buttonLabel.uppercase(),
                            color = LaunchUiTokens.ink,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // ── Floating nav bar top ─────────────────────────────────
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Tombol back → Dashboard
                Surface(
                    onClick = onExitToDashboard,
                    shape = RoundedCornerShape(999.dp),
                    color = navSurfaceColor,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "←",
                            color = navContentColor,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = "Dashboard",
                            color = navContentColor,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                // Tombol next → Mulai Sesi (opsional)
                if (onStartSession != null) {
                    Surface(
                        onClick = onStartSession,
                        shape = RoundedCornerShape(999.dp),
                        color = navSurfaceColor,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Mulai Sesi",
                                color = navContentColor,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                text = "→",
                                color = navContentColor,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }

            KioskExitOverlay(
                exitCode = kioskExitCode,
                onAuthorizedExit = onExitToDashboard,
            )
        }
    }
}

@Composable
fun KioskWaitingApprovalScreen(
    launchState: LaunchUiState,
    launchActions: LaunchActions,
    kioskExitCode: String,
    onExitToDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LaunchUiTokens.softSurface)
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        val isTablet = maxWidth >= 900.dp
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            LaunchStatusCard(
                title = "Approval Flow",
                message = launchState.message ?: "Menunggu approval manual dari Photobooth Station...",
                accent = LaunchUiTokens.warning,
            )
            LaunchHeroCard(
                title = "Menunggu Persetujuan",
                subtitle = "Pembayaran manual sedang diperiksa oleh operator.",
                badge = "WAITING",
                badgeAccent = LaunchUiTokens.warning,
            )
            if (isTablet) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    LaunchSectionCard(
                        title = "Status Pembayaran",
                        modifier = Modifier.weight(1f)
                    ) {
                        LaunchMetricRow(
                            "Kode Sesi" to (launchState.session?.sessionCode ?: "-"),
                            "WhatsApp" to launchState.customerWhatsapp,
                            "Status" to (launchState.approvalStatus ?: "pending"),
                        )
                    }
                    LaunchSectionCard(
                        title = "Aksi Operator",
                        modifier = Modifier.weight(1f)
                    ) {
                        LaunchPrimaryButton(
                            text = "Periksa Status Manual",
                            onClick = launchActions.checkManualPaymentApproval,
                            enabled = !launchState.loading,
                        )
                    }
                }
            } else {
                LaunchSectionCard(
                    title = "Status Pembayaran"
                ) {
                    LaunchMetricRow(
                        "Kode Sesi" to (launchState.session?.sessionCode ?: "-"),
                        "WhatsApp" to launchState.customerWhatsapp,
                        "Status" to (launchState.approvalStatus ?: "pending"),
                    )
                }
                LaunchPrimaryButton(
                    text = "Periksa Status Manual",
                    onClick = launchActions.checkManualPaymentApproval,
                    enabled = !launchState.loading,
                )
            }
        }
        
        KioskExitOverlay(
            exitCode = kioskExitCode,
            onAuthorizedExit = onExitToDashboard,
        )
    }
}

@Composable
fun PaymentScreen(
    config: EventLaunchConfig,
    state: BoothUiState = BoothUiState(),
    launchState: LaunchUiState = LaunchUiState(),
    launchActions: LaunchActions = LaunchActions(),
    onPaymentConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedPayment by remember { mutableStateOf(config.paymentConfig.availablePaymentTypes.first()) }
    var showVoucherInput by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LaunchUiTokens.softSurface)
            .safeDrawingPadding(),
    ) {
        val isTablet = maxWidth >= 900.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = if (isTablet) 28.dp else 16.dp,
                vertical = if (isTablet) 24.dp else 16.dp,
            ),
        ) {
            item {
                LaunchHeroCard(
                    title = "Payment",
                    subtitle = "Lengkapi nomor WhatsApp Anda dan selesaikan pembayaran untuk mulai.",
                    badge = "READY",
                    badgeAccent = LaunchUiTokens.primary,
                )
            }
            if (isTablet) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Kiri: WhatsApp & Dialpad (weight 1.1f)
                        LaunchSectionCard(
                            title = "1. Nomor WhatsApp",
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = LaunchUiTokens.softSurface,
                                    border = BorderStroke(1.dp, LaunchUiTokens.border)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Masukkan nomor WhatsApp aktif Anda untuk menerima foto:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = LaunchUiTokens.inkSoft
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = LaunchUiTokens.primary.copy(alpha = 0.1f),
                                            ) {
                                                Text(
                                                    text = "+62",
                                                    color = LaunchUiTokens.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            
                                            OutlinedTextField(
                                                value = launchState.customerWhatsapp,
                                                onValueChange = { val clean = it.filter { c -> c.isDigit() }; launchActions.onWhatsappChanged(clean) },
                                                readOnly = true,
                                                placeholder = { Text("8123456789", color = LaunchUiTokens.inkSoft.copy(alpha = 0.5f)) },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = LaunchUiTokens.ink
                                                ),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                            )
                                        }
                                    }
                                }

                                KioskDialpad(
                                    onDigitClick = { digit ->
                                        val current = launchState.customerWhatsapp
                                        if (current.length < 13) {
                                            launchActions.onWhatsappChanged(current + digit)
                                        }
                                    },
                                    onBackspaceClick = {
                                        val current = launchState.customerWhatsapp
                                        if (current.isNotEmpty()) {
                                            launchActions.onWhatsappChanged(current.dropLast(1))
                                        }
                                    },
                                    onClearClick = {
                                        launchActions.onWhatsappChanged("")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Kanan: Summary, Payment Option, Voucher, Action (weight 0.9f)
                        LaunchSectionCard(
                            title = "2. Informasi Pembayaran",
                            modifier = Modifier.weight(0.9f)
                        ) {
                            val totalAmount = launchState.quote?.amount ?: launchState.finalAmount
                            val isFree = totalAmount == 0.0

                            // 1. Rincian Harga Simple
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LaunchUiTokens.softSurface,
                                border = BorderStroke(1.dp, LaunchUiTokens.border),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Paket", style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.inkSoft)
                                        Text(config.orderSummary.packageName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = LaunchUiTokens.ink)
                                    }
                                    val subtotal = launchState.pricing?.photoboothPrice ?: 0.0
                                    val discount = launchState.quote?.discountAmount?.toDouble() ?: 0.0
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Harga", style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.inkSoft)
                                        Text("Rp ${subtotal.toLong()}", style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.ink)
                                    }
                                    if (discount > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Diskon", style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.inkSoft)
                                            Text("-Rp ${discount.toLong()}", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(LaunchUiTokens.border))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text("Total Bayar", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = LaunchUiTokens.ink)
                                        Text(
                                            text = if (isFree) "GRATIS" else "Rp ${totalAmount.toLong()}",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = LaunchUiTokens.primary)
                                        )
                                    }
                                }
                            }

                            // 2. Metode Pembayaran Visual
                            Text("Pilih Metode Pembayaran", color = LaunchUiTokens.ink, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                config.paymentConfig.availablePaymentTypes.forEach { option ->
                                    val isSelected = selectedPayment == option
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedPayment = option },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) LaunchUiTokens.primary.copy(alpha = 0.08f) else LaunchUiTokens.softSurface,
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) LaunchUiTokens.primary else LaunchUiTokens.border
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val icon = when (option) {
                                                LaunchPaymentOption.Qris -> Icons.Default.QrCode
                                                LaunchPaymentOption.Manual -> Icons.Default.Payments
                                                LaunchPaymentOption.Free -> Icons.Default.CheckCircle
                                            }
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = option.label,
                                                tint = if (isSelected) LaunchUiTokens.primary else LaunchUiTokens.inkSoft,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = when (option) {
                                                    LaunchPaymentOption.Qris -> "QRIS / Scan QR"
                                                    LaunchPaymentOption.Manual -> "Bayar di Kasir"
                                                    LaunchPaymentOption.Free -> "Gratis"
                                                },
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) LaunchUiTokens.primary else LaunchUiTokens.ink,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Voucher Collapsible
                            if (!showVoucherInput) {
                                TextButton(
                                    onClick = { showVoucherInput = true },
                                    modifier = Modifier.align(Alignment.Start)
                                ) {
                                    Text("+ Gunakan Kode Voucher", color = LaunchUiTokens.primary, fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                OutlinedTextField(
                                    value = launchState.voucherCode,
                                    onValueChange = launchActions.onVoucherCodeChanged,
                                    label = { Text("Masukkan Kode Voucher") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        if (launchState.voucherCode.isNotBlank()) {
                                            Button(
                                                onClick = launchActions.checkVoucherAndQuote,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.padding(end = 8.dp)
                                            ) {
                                                Text("Cek")
                                            }
                                        }
                                    }
                                )
                            }

                            // QR Code
                            if (selectedPayment == LaunchPaymentOption.Qris && !launchState.quote?.paymentUrl.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FakeQrCode(
                                        value = launchState.quote!!.paymentUrl!!,
                                        modifier = Modifier.size(160.dp),
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Scan QR Code untuk membayar",
                                        color = LaunchUiTokens.inkSoft,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            val btnText = when {
                                isFree -> "Mulai Foto (Gratis)"
                                selectedPayment == LaunchPaymentOption.Qris && launchState.quote?.paymentUrl.isNullOrBlank() -> "Dapatkan QR Code"
                                selectedPayment == LaunchPaymentOption.Qris -> "Saya Sudah Bayar (Lanjutkan)"
                                else -> "Bayar Cash di Kasir"
                            }

                            if (!launchState.error.isNullOrBlank()) {
                                Text(
                                    text = launchState.error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            if (!launchState.message.isNullOrBlank()) {
                                Text(
                                    text = launchState.message,
                                    color = LaunchUiTokens.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            LaunchPrimaryButton(
                                text = btnText,
                                onClick = {
                                    if (isFree) {
                                        launchActions.submitManualPaymentRequest()
                                    } else if (selectedPayment == LaunchPaymentOption.Qris) {
                                        if (launchState.quote?.paymentUrl.isNullOrBlank()) {
                                            launchActions.quoteQrPayment()
                                        } else {
                                            launchActions.submitManualPaymentRequest()
                                        }
                                    } else {
                                        launchActions.submitManualPaymentRequest()
                                    }
                                },
                                enabled = !launchState.loading,
                            )
                        }
                    }
                }
            } else {
                // Mobile stacked layout
                item {
                    LaunchSectionCard(title = "1. Nomor WhatsApp") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = LaunchUiTokens.softSurface,
                                border = BorderStroke(1.dp, LaunchUiTokens.border)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Masukkan nomor WhatsApp aktif Anda untuk menerima foto:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LaunchUiTokens.inkSoft
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = LaunchUiTokens.primary.copy(alpha = 0.1f),
                                        ) {
                                            Text(
                                                text = "+62",
                                                color = LaunchUiTokens.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        
                                        OutlinedTextField(
                                            value = launchState.customerWhatsapp,
                                            onValueChange = { val clean = it.filter { c -> c.isDigit() }; launchActions.onWhatsappChanged(clean) },
                                            readOnly = true,
                                            placeholder = { Text("8123456789", color = LaunchUiTokens.inkSoft.copy(alpha = 0.5f)) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = LaunchUiTokens.ink
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                    }
                                }
                            }
                            
                            KioskDialpad(
                                onDigitClick = { digit ->
                                    val current = launchState.customerWhatsapp
                                    if (current.length < 13) {
                                        launchActions.onWhatsappChanged(current + digit)
                                    }
                                },
                                onBackspaceClick = {
                                    val current = launchState.customerWhatsapp
                                    if (current.isNotEmpty()) {
                                        launchActions.onWhatsappChanged(current.dropLast(1))
                                    }
                                },
                                onClearClick = {
                                    launchActions.onWhatsappChanged("")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                item {
                    LaunchSectionCard(title = "2. Informasi Pembayaran") {
                        val totalAmount = launchState.quote?.amount ?: launchState.finalAmount
                        val isFree = totalAmount == 0.0

                        // 1. Rincian Harga Simple
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LaunchUiTokens.softSurface,
                            border = BorderStroke(1.dp, LaunchUiTokens.border),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Paket", style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.inkSoft)
                                    Text(config.orderSummary.packageName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = LaunchUiTokens.ink)
                                }
                                val subtotal = launchState.pricing?.photoboothPrice ?: 0.0
                                val discount = launchState.quote?.discountAmount?.toDouble() ?: 0.0
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Harga", style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.inkSoft)
                                    Text("Rp ${subtotal.toLong()}", style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.ink)
                                }
                                if (discount > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Diskon", style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.inkSoft)
                                        Text("-Rp ${discount.toLong()}", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold))
                                    }
                                }
                                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(LaunchUiTokens.border))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text("Total Bayar", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = LaunchUiTokens.ink)
                                    Text(
                                        text = if (isFree) "GRATIS" else "Rp ${totalAmount.toLong()}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = LaunchUiTokens.primary)
                                    )
                                }
                            }
                        }

                        // 2. Metode Pembayaran Visual
                        Text("Pilih Metode Pembayaran", color = LaunchUiTokens.ink, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            config.paymentConfig.availablePaymentTypes.forEach { option ->
                                val isSelected = selectedPayment == option
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPayment = option },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) LaunchUiTokens.primary.copy(alpha = 0.08f) else LaunchUiTokens.softSurface,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) LaunchUiTokens.primary else LaunchUiTokens.border
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val icon = when (option) {
                                            LaunchPaymentOption.Qris -> Icons.Default.QrCode
                                            LaunchPaymentOption.Manual -> Icons.Default.Payments
                                            LaunchPaymentOption.Free -> Icons.Default.CheckCircle
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = option.label,
                                            tint = if (isSelected) LaunchUiTokens.primary else LaunchUiTokens.inkSoft,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = when (option) {
                                                LaunchPaymentOption.Qris -> "QRIS / Scan QR"
                                                LaunchPaymentOption.Manual -> "Bayar di Kasir"
                                                LaunchPaymentOption.Free -> "Gratis"
                                            },
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) LaunchUiTokens.primary else LaunchUiTokens.ink,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Voucher Collapsible
                        if (!showVoucherInput) {
                            TextButton(
                                onClick = { showVoucherInput = true },
                                modifier = Modifier.align(Alignment.Start)
                            ) {
                                Text("+ Gunakan Kode Voucher", color = LaunchUiTokens.primary, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            OutlinedTextField(
                                value = launchState.voucherCode,
                                onValueChange = launchActions.onVoucherCodeChanged,
                                label = { Text("Masukkan Kode Voucher") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    if (launchState.voucherCode.isNotBlank()) {
                                        Button(
                                            onClick = launchActions.checkVoucherAndQuote,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text("Cek")
                                        }
                                    }
                                }
                            )
                        }

                        // QR Code
                        if (selectedPayment == LaunchPaymentOption.Qris && !launchState.quote?.paymentUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FakeQrCode(
                                    value = launchState.quote!!.paymentUrl!!,
                                    modifier = Modifier.size(160.dp),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Scan QR Code untuk membayar",
                                    color = LaunchUiTokens.inkSoft,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        val btnText = when {
                            isFree -> "Mulai Foto (Gratis)"
                            selectedPayment == LaunchPaymentOption.Qris && launchState.quote?.paymentUrl.isNullOrBlank() -> "Dapatkan QR Code"
                            selectedPayment == LaunchPaymentOption.Qris -> "Saya Sudah Bayar (Lanjutkan)"
                            else -> "Bayar Cash di Kasir"
                        }

                        if (!launchState.error.isNullOrBlank()) {
                            Text(
                                text = launchState.error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        if (!launchState.message.isNullOrBlank()) {
                            Text(
                                text = launchState.message,
                                color = LaunchUiTokens.primary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        LaunchPrimaryButton(
                            text = btnText,
                            onClick = {
                                if (isFree) {
                                    launchActions.submitManualPaymentRequest()
                                } else if (selectedPayment == LaunchPaymentOption.Qris) {
                                    if (launchState.quote?.paymentUrl.isNullOrBlank()) {
                                        launchActions.quoteQrPayment()
                                    } else {
                                        launchActions.submitManualPaymentRequest()
                                    }
                                } else {
                                    launchActions.submitManualPaymentRequest()
                                }
                            },
                            enabled = !launchState.loading,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PickTemplateScreen(
    eventName: String,
    templates: List<PhotoTemplate>,
    onTemplateSelected: (PhotoTemplate) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LaunchUiTokens.softSurface)
            .safeDrawingPadding(),
    ) {
        val isTablet = maxWidth >= 900.dp
        val columns = if (isTablet) 2 else 1
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = if (isTablet) 28.dp else 16.dp,
                vertical = if (isTablet) 24.dp else 16.dp,
            ),
        ) {
            item {
                LaunchHeroCard(
                    title = "Pick Template",
                    subtitle = "Pilih template foto yang tersedia untuk event $eventName.",
                    badge = "${templates.size} template",
                    badgeAccent = LaunchUiTokens.purple,
                )
            }
            items(templates.chunked(columns)) { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { template ->
                        TemplateSelectionCard(
                            template = template,
                            modifier = Modifier.weight(1f),
                            onSelect = { onTemplateSelected(template) },
                        )
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CaptureScreen(
    eventName: String,
    template: PhotoTemplate,
    state: BoothUiState = BoothUiState(),
    actions: BoothActions = BoothActions(),
    onCaptureFinished: () -> Unit,
    kioskExitCode: String = "123456",
    onExitToDashboard: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val capturedCount = state.capturedPhotosBySlot.size
    val totalSlots = state.templateSlotCount.takeIf { it > 0 } ?: template.frameCount

    LaunchedEffect(capturedCount) {
        if (capturedCount >= totalSlots && totalSlots > 0) {
            onCaptureFinished()
        }
    }

    val orderedSlots = state.selectedTemplateSlots.sortedBy { it.slotIndex }
    val captureSlots = orderedSlots.map { it.sourceSlotIndex }.distinct().sorted()
    val nextCaptureSlot = captureSlots.firstOrNull { !state.capturedPhotosBySlot.containsKey(it) }
        ?: captureSlots.lastOrNull()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEFF3FF))
            .safeDrawingPadding()
            .padding(20.dp),
    ) {
        val isTablet = maxWidth >= 900.dp
        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LaunchSectionCard(
                    title = "Template & Capture",
                    modifier = Modifier.weight(0.78f),
                ) {
                    LaunchStatusPills(
                        "Event" to eventName,
                        "Template" to template.name,
                    )
                    Text(
                        text = "${template.sizeLabel} • $totalSlots frame",
                        color = LaunchUiTokens.inkSoft,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        TemplateSurface(state = state, modifier = Modifier.fillMaxSize())
                    }
                    Text(
                        text = "Hasil capture",
                        color = LaunchUiTokens.ink,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        captureSlots.forEach { slotIdx ->
                            val photoPath = state.capturedPhotosBySlot[slotIdx]
                            val captured = photoPath != null
                            val isActive = slotIdx == nextCaptureSlot && !captured

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (isActive) template.accent.copy(alpha = 0.22f) else Color.White,
                                    )
                                    .border(
                                        width = if (isActive) 2.dp else 1.dp,
                                        color = if (isActive) template.accent else LaunchUiTokens.border,
                                        shape = RoundedCornerShape(18.dp)
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (photoPath != null) {
                                    AsyncImage(
                                        model = File(photoPath),
                                        contentDescription = "Slot $slotIdx",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp))
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (isActive) template.accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.82f),
                                        ) {
                                            Text(
                                                text = if (isActive) "Aktif" else "Frame ${slotIdx + 1}",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                color = if (isActive) template.accent else LaunchUiTokens.inkSoft,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        }
                                        Text("Shot ${slotIdx + 1}", color = LaunchUiTokens.inkSoft)
                                    }
                                }
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1.42f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val activeSlotLabel = nextCaptureSlot?.let { "Shot ${it + 1}" } ?: "Siap"
                    LaunchStatusPills(
                        "Event" to eventName,
                        "Template" to template.name,
                        "Status" to activeSlotLabel,
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.cameraSource == CameraSource.AndroidDefault) {
                            AndroidCameraCapture(
                                state = state,
                                onCaptured = actions.capturePhotoFile,
                                onCameraAvailabilityChanged = actions.updateDetectedCameras,
                                onCapturingStateChanged = { },
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            CameraSurface(state = state)
                        }
                    }
                    if (state.cameraSource == CameraSource.ExternalCanon) {
                        LaunchPrimaryButton(
                            text = "Capture (DSLR)",
                            onClick = actions.capturePhoto,
                            enabled = !state.isLoading,
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxSize()) {
                val activeSlotLabel = nextCaptureSlot?.let { "Shot ${it + 1}" } ?: "Siap"
                LaunchStatusPills(
                    "Event" to eventName,
                    "Template" to template.name,
                    "Status" to activeSlotLabel,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.cameraSource == CameraSource.AndroidDefault) {
                        AndroidCameraCapture(
                            state = state,
                            onCaptured = actions.capturePhotoFile,
                            onCameraAvailabilityChanged = actions.updateDetectedCameras,
                            onCapturingStateChanged = { },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        CameraSurface(state = state)
                    }
                }
                if (state.cameraSource == CameraSource.ExternalCanon) {
                    LaunchPrimaryButton(
                        text = "Capture (DSLR)",
                        onClick = actions.capturePhoto,
                        enabled = !state.isLoading,
                    )
                }
            }
        }
        KioskExitOverlay(
            exitCode = kioskExitCode,
            onAuthorizedExit = onExitToDashboard,
        )
    }
}

@Composable
fun CaptureFinishedScreen(
    eventName: String,
    template: PhotoTemplate,
    sessionCode: String = "SES-1XCDSRVZ",
    kioskExitCode: String = "123456",
    onBackToWelcome: () -> Unit,
    onStartNewSession: () -> Unit,
    onExitToDashboard: () -> Unit = {},
    capturedPhotos: List<String> = emptyList(),
    onDownloadClick: (() -> Unit)? = null,
    onPrintClick: (() -> Unit)? = null,
    mockPrintStatus: MockPrintStatus = MockPrintStatus.Idle,
    mockPrintMessage: String? = null,
    printUsePhotoboothStation: Boolean = false,
    isStationConnected: Boolean = false,
    previewContent: @Composable (() -> Unit)? = null,
    isQuickBooth: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val cloudDownloadUrl = "https://dafydio.com/$sessionCode"

    // Kiosk Auto-Close & Dialog States
    var countdownSeconds by remember { mutableStateOf(30) }
    var selectedPhotoPathForLightbox by remember { mutableStateOf<String?>(null) }
    var showQrZoomDialog by remember { mutableStateOf(false) }

    val resetTimer = { countdownSeconds = 30 }

    LaunchedEffect(countdownSeconds) {
        if (countdownSeconds > 0) {
            kotlinx.coroutines.delay(1000L)
            countdownSeconds--
        } else {
            if (isQuickBooth) {
                onExitToDashboard()
            } else {
                onBackToWelcome()
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LaunchUiTokens.softSurface)
            .safeDrawingPadding()
            .padding(24.dp),
    ) {
        // Celebratory Confetti background effect
        ConfettiEffect()

        val isTablet = maxWidth >= 900.dp
        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Left Column: 4x6 Portrait collage preview taking full height
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(2f / 3f) // 4x6 format
                            .clickable {
                                resetTimer()
                                // Let the main collage preview also trigger lightbox of the first image if clicked
                                capturedPhotos.firstOrNull()?.let {
                                    selectedPhotoPathForLightbox = it
                                }
                            },
                        shape = RoundedCornerShape(28.dp),
                        color = Color.Black.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, LaunchUiTokens.border),
                    ) {
                        if (previewContent != null) {
                            previewContent()
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Preview hasil foto", color = LaunchUiTokens.inkSoft)
                            }
                        }
                    }
                }

                // Right Column: Scrollable controls and summaries
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Kiosk Auto-Close Info Bar
                    Surface(
                        color = LaunchUiTokens.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { resetTimer() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kiosk Auto-Close",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = LaunchUiTokens.primary
                            )
                            Text(
                                text = "Kembali ke Welcome dalam ${countdownSeconds}s",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = LaunchUiTokens.primary
                            )
                        }
                    }

                    LaunchHeroCard(
                        title = if (isQuickBooth) "Sesi Quick Booth selesai" else "Sesi foto selesai",
                        subtitle = "Preview hasil sesi untuk event $eventName dengan template ${template.name}.",
                        badge = "DONE",
                        badgeAccent = LaunchUiTokens.success,
                    )

                    LaunchSectionCard(title = "Ringkasan Sesi") {
                        LaunchStatusPills(
                            "Event" to eventName,
                            "Template" to template.name,
                            "Status" to "Selesai",
                        )
                        Text(
                            text = "${template.sizeLabel} • ${template.frameCount} frame",
                            color = LaunchUiTokens.inkSoft,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            repeat(template.frameCount.coerceAtMost(4)) { index ->
                                val photoPath = capturedPhotos.getOrNull(index)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    template.accent.copy(alpha = 0.22f),
                                                    Color.White,
                                                ),
                                            ),
                                        )
                                        .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(14.dp))
                                        .then(
                                            if (!photoPath.isNullOrBlank()) {
                                                Modifier.clickable {
                                                    resetTimer()
                                                    selectedPhotoPathForLightbox = photoPath
                                                }
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (!photoPath.isNullOrBlank()) {
                                        AsyncImage(
                                            model = File(photoPath),
                                            contentDescription = "Hasil ${index + 1}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text("Hasil ${index + 1}", color = LaunchUiTokens.inkSoft, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    CloudQrCard(
                        sessionCode = sessionCode,
                        cloudDownloadUrl = cloudDownloadUrl,
                        onQrClick = {
                            resetTimer()
                            showQrZoomDialog = true
                        }
                    )
                    
                    if (onPrintClick != null || onDownloadClick != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (onDownloadClick != null) {
                                OutlinedButton(
                                    onClick = {
                                        resetTimer()
                                        onDownloadClick()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, LaunchUiTokens.primary.copy(alpha = 0.3f))
                                ) {
                                    Text("Download", color = LaunchUiTokens.primary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (onPrintClick != null) {
                                when (mockPrintStatus) {
                                    MockPrintStatus.Queued -> {
                                        Button(
                                            onClick = {},
                                            enabled = false,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = LaunchUiTokens.purple)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Printing...", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    MockPrintStatus.Sent -> {
                                        Button(
                                            onClick = {
                                                resetTimer()
                                                onPrintClick()
                                            },
                                            enabled = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = LaunchUiTokens.success)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Success",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Printed!", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    else -> {
                                        Button(
                                            onClick = {
                                                resetTimer()
                                                onPrintClick()
                                            },
                                            enabled = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = LaunchUiTokens.purple)
                                        ) {
                                            Text("Print Strip", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                        
                        Text(
                            text = if (printUsePhotoboothStation && isStationConnected) {
                                "Print dikirim ke Photobooth Station."
                            } else {
                                "Print memakai Android printer lokal."
                            },
                            color = LaunchUiTokens.inkSoft,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        
                        if (!mockPrintMessage.isNullOrBlank()) {
                            Text(
                                text = mockPrintMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = LaunchUiTokens.inkSoft,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    if (isQuickBooth) {
                        LaunchPrimaryButton(
                            text = "Kembali ke Dashboard (${countdownSeconds}s)",
                            onClick = {
                                resetTimer()
                                onExitToDashboard()
                            },
                            enabled = true,
                        )
                    } else {
                        LaunchSecondaryButton(
                            text = "Kembali ke Welcome Screen",
                            onClick = {
                                resetTimer()
                                onBackToWelcome()
                            },
                            enabled = true,
                        )
                        LaunchPrimaryButton(
                            text = "Mulai Sesi Baru (${countdownSeconds}s)",
                            onClick = {
                                resetTimer()
                                onStartNewSession()
                            },
                            enabled = true,
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Kiosk Auto-Close Info Bar
                Surface(
                    color = LaunchUiTokens.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable { resetTimer() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kiosk Auto-Close",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = LaunchUiTokens.primary
                        )
                        Text(
                            text = "Kembali ke Welcome dalam ${countdownSeconds}s",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = LaunchUiTokens.primary
                        )
                    }
                }

                LaunchHeroCard(
                    title = if (isQuickBooth) "Sesi Quick Booth selesai" else "Sesi foto selesai",
                    subtitle = "Preview hasil sesi untuk event $eventName dengan template ${template.name}.",
                    badge = "DONE",
                    badgeAccent = LaunchUiTokens.success,
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f) // Lock aspect ratio for 4x6 photostrip
                        .clickable {
                            resetTimer()
                            capturedPhotos.firstOrNull()?.let {
                                selectedPhotoPathForLightbox = it
                            }
                        },
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, LaunchUiTokens.border),
                ) {
                    if (previewContent != null) {
                        previewContent()
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Preview hasil foto", color = LaunchUiTokens.inkSoft)
                        }
                    }
                }

                CloudQrCard(
                    sessionCode = sessionCode,
                    cloudDownloadUrl = cloudDownloadUrl,
                    onQrClick = {
                        resetTimer()
                        showQrZoomDialog = true
                    }
                )
                
                if (onPrintClick != null || onDownloadClick != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (onDownloadClick != null) {
                            OutlinedButton(
                                onClick = {
                                    resetTimer()
                                    onDownloadClick()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, LaunchUiTokens.primary.copy(alpha = 0.3f))
                            ) {
                                Text("Download", color = LaunchUiTokens.primary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (onPrintClick != null) {
                            when (mockPrintStatus) {
                                MockPrintStatus.Queued -> {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LaunchUiTokens.purple)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Printing...", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                MockPrintStatus.Sent -> {
                                    Button(
                                        onClick = {
                                            resetTimer()
                                            onPrintClick()
                                        },
                                        enabled = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LaunchUiTokens.success)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Success",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Printed!", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = {
                                            resetTimer()
                                            onPrintClick()
                                        },
                                        enabled = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LaunchUiTokens.purple)
                                    ) {
                                        Text("Print Strip", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    
                    Text(
                        text = if (printUsePhotoboothStation && isStationConnected) {
                            "Print dikirim ke Photobooth Station."
                        } else {
                            "Print memakai Android printer lokal."
                        },
                        color = LaunchUiTokens.inkSoft,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    
                    if (!mockPrintMessage.isNullOrBlank()) {
                        Text(
                            text = mockPrintMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = LaunchUiTokens.inkSoft,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                if (isQuickBooth) {
                    LaunchPrimaryButton(
                        text = "Kembali ke Dashboard (${countdownSeconds}s)",
                        onClick = {
                            resetTimer()
                            onExitToDashboard()
                        },
                        enabled = true,
                    )
                } else {
                    LaunchSecondaryButton(
                        text = "Kembali ke Welcome Screen",
                        onClick = {
                            resetTimer()
                            onBackToWelcome()
                        },
                        enabled = true,
                    )
                    LaunchPrimaryButton(
                        text = "Mulai Sesi Baru (${countdownSeconds}s)",
                        onClick = {
                            resetTimer()
                            onStartNewSession()
                        },
                        enabled = true,
                    )
                }
            }
        }
        if (!isQuickBooth) {
            KioskExitOverlay(
                exitCode = kioskExitCode,
                onAuthorizedExit = onExitToDashboard,
            )
        }
    }

    // Modal Overlays
    selectedPhotoPathForLightbox?.let { path ->
        PhotoLightboxDialog(
            photoPath = path,
            onDismiss = {
                selectedPhotoPathForLightbox = null
                resetTimer()
            }
        )
    }

    if (showQrZoomDialog) {
        QrCodeZoomDialog(
            url = cloudDownloadUrl,
            sessionCode = sessionCode,
            onDismiss = {
                showQrZoomDialog = false
                resetTimer()
            }
        )
    }
}

@Composable
private fun CloudQrCard(
    sessionCode: String,
    cloudDownloadUrl: String,
    onQrClick: () -> Unit,
) {
    LaunchSectionCard(
        title = "Download Cloud",
        modifier = Modifier.clickable { onQrClick() }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            FakeQrCode(
                value = cloudDownloadUrl,
                modifier = Modifier.size(116.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Scan untuk download hasil photobooth",
                    color = LaunchUiTokens.ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Session: $sessionCode",
                    color = LaunchUiTokens.inkSoft,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = cloudDownloadUrl,
                    color = LaunchUiTokens.primary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "💡 Ketuk untuk memperbesar",
                    color = LaunchUiTokens.inkSoft,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun KioskExitOverlay(
    exitCode: String,
    onAuthorizedExit: () -> Unit,
) {
    var tapCount by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var inputCode by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(56.dp)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    tapCount += 1
                    if (tapCount >= 5) {
                        tapCount = 0
                        showDialog = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color.Black.copy(alpha = 0.08f), CircleShape)
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                inputCode = ""
                errorText = null
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (exitCode.isBlank()) {
                            errorText = "Kode keluar belum diatur di Settings"
                        } else if (inputCode == exitCode) {
                            showDialog = false
                            inputCode = ""
                            errorText = null
                            onAuthorizedExit()
                        } else {
                            errorText = "Kode salah"
                        }
                    },
                ) {
                    Text("Masuk Dashboard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        inputCode = ""
                        errorText = null
                    },
                ) {
                    Text("Batal")
                }
            },
            title = { Text("Akses Dashboard") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (exitCode.isBlank()) {
                            "Kode keluar belum diatur. Buka Settings lebih dulu untuk mengisi kode keluar kiosk."
                        } else {
                            "Tap logo 5x terdeteksi. Masukkan kode untuk keluar dari mode event."
                        },
                        color = LaunchUiTokens.inkSoft,
                    )
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = {
                            inputCode = it
                            errorText = null
                        },
                        label = { Text("Kode keluar") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    errorText?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
        )
    }
}

class ConfettiParticle(
    var x: Float,
    var y: Float,
    var velocityY: Float,
    var velocityX: Float,
    val color: Color,
    val size: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    val shapeCircle: Boolean
)

@Composable
fun ConfettiEffect(modifier: Modifier = Modifier) {
    val colors = listOf(
        Color(0xFF5B67FF), Color(0xFF8B5CF6), Color(0xFFEC4899),
        Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF3B82F6)
    )
    val particles = remember {
        List(60) {
            ConfettiParticle(
                x = (0..1000).random().toFloat() / 1000f,
                y = -(0..800).random().toFloat() / 1000f,
                velocityY = (30..80).random().toFloat() / 10f,
                velocityX = (-15..15).random().toFloat() / 10f,
                color = colors.random(),
                size = (8..18).random().toFloat(),
                rotation = (0..360).random().toFloat(),
                rotationSpeed = (-40..40).random().toFloat() / 10f,
                shapeCircle = java.util.Random().nextBoolean()
            )
        }
    }
    var frameTime by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { time ->
                frameTime = time
            }
        }
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        particles.forEach { p ->
            p.y += p.velocityY / height * 60f
            p.x += p.velocityX / width * 60f
            p.rotation += p.rotationSpeed
            if (p.y > 1.1f) {
                p.y = -0.1f
                p.x = (0..1000).random().toFloat() / 1000f
            }
            val px = p.x * width
            val py = p.y * height
            rotate(degrees = p.rotation, pivot = androidx.compose.ui.geometry.Offset(px, py)) {
                if (p.shapeCircle) {
                    drawCircle(color = p.color, radius = p.size / 2, center = androidx.compose.ui.geometry.Offset(px, py))
                } else {
                    drawRect(
                        color = p.color,
                        topLeft = androidx.compose.ui.geometry.Offset(px - p.size / 2, py - p.size / 3),
                        size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun QrCodeCanvas(
    data: String,
    modifier: Modifier = Modifier
) {
    val bitMatrix = remember(data) {
        try {
            QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 512, 512)
        } catch (e: Exception) {
            null
        }
    }

    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        if (bitMatrix != null) {
            val numModules = bitMatrix.width
            val moduleSize = sizePx / numModules
            drawRect(color = Color.White)
            for (r in 0 until numModules) {
                for (c in 0 until numModules) {
                    if (bitMatrix.get(c, r)) {
                        drawRect(
                            color = Color.Black,
                            topLeft = androidx.compose.ui.geometry.Offset(c * moduleSize, r * moduleSize),
                            size = androidx.compose.ui.geometry.Size(moduleSize, moduleSize)
                        )
                    }
                }
            }
        } else {
            drawRect(color = Color.LightGray)
        }
    }
}

@Composable
fun PhotoLightboxDialog(
    photoPath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.8f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(12.dp)
                        .clickable(enabled = false) {}
                ) {
                    AsyncImage(
                        model = File(photoPath),
                        contentDescription = "Lightbox Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(99.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Text("Tutup", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QrCodeZoomDialog(
    url: String,
    sessionCode: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .width(360.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .clickable(enabled = false) {}
                    .padding(28.dp)
            ) {
                Text(
                    text = "Scan QR Code",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LaunchUiTokens.ink
                )
                QrCodeCanvas(
                    data = url,
                    modifier = Modifier
                        .size(240.dp)
                        .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Session: $sessionCode",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = LaunchUiTokens.inkSoft
                    )
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall,
                        color = LaunchUiTokens.primary,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LaunchUiTokens.primary)
                ) {
                    Text("Selesai", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FakeQrCode(
    value: String,
    modifier: Modifier = Modifier,
) {
    QrCodeCanvas(
        data = value,
        modifier = modifier
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(20.dp))
            .padding(10.dp)
    )
}

@Composable
private fun CompactEventDetailsCard(config: EventLaunchConfig) {
    LaunchSectionCard(title = "Detail Event & Welcome") {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "Nama Event" to config.eventName,
                "Tanggal" to config.eventDate,
                "Paket" to config.packageLabel,
                "Template" to config.defaultTemplateLabel,
                "Background Welcome" to config.welcomeConfig.backgroundLabel
            ).forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LaunchUiTokens.inkSoft
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = LaunchUiTokens.ink
                    )
                }
            }
        }
    }
}

@Composable
private fun EventInfoCard(config: EventLaunchConfig) {
    LaunchSectionCard(title = "Informasi Event") {
        LaunchMetricRow(
            "Nama Event" to config.eventName,
            "Tanggal" to config.eventDate,
        )
        LaunchMetricRow(
            "Status" to config.statusLabel,
            "Paket" to config.packageLabel,
            "Pricing" to config.pricingLabel,
        )
    }
}

@Composable
private fun WelcomeConfigCard(config: WelcomeConfig, defaultTemplateLabel: String) {
    LaunchSectionCard(title = "Konfigurasi Welcome Screen") {
        LaunchMetricRow(
            "Background" to config.backgroundLabel,
            "Template" to defaultTemplateLabel,
        )
        Text(config.title, style = MaterialTheme.typography.titleLarge, color = LaunchUiTokens.ink, fontWeight = FontWeight.Bold)
        Text(config.subtitle, style = MaterialTheme.typography.bodyMedium, color = LaunchUiTokens.inkSoft)
        Surface(shape = RoundedCornerShape(999.dp), color = config.buttonColor.copy(alpha = 0.14f)) {
            Text(
                text = "Warna tombol welcome",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = config.buttonColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun TransactionConfigCard(config: EventLaunchConfig) {
    LaunchSectionCard(title = "Konfigurasi Transaksi") {
        LaunchMetricRow(
            "No WA default" to config.paymentConfig.defaultWhatsapp,
            "Voucher default" to (config.paymentConfig.defaultVoucher?.code ?: "-"),
        )
        Text(
            text = "Tipe pembayaran tersedia",
            color = LaunchUiTokens.ink,
            fontWeight = FontWeight.SemiBold,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            config.paymentConfig.availablePaymentTypes.forEach { option ->
                Surface(shape = RoundedCornerShape(999.dp), color = LaunchUiTokens.primary.copy(alpha = 0.10f)) {
                    Text(
                        text = option.label,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = LaunchUiTokens.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        LaunchMetricRow(
            "Harga" to config.orderSummary.priceLabel,
            "Diskon" to config.orderSummary.discountLabel,
            "Total" to config.orderSummary.totalLabel,
        )
    }
}

@Composable
private fun ChecklistCard(items: List<String>) {
    LaunchSectionCard(title = "Checklist Kesiapan Event") {
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(LaunchUiTokens.success, RoundedCornerShape(999.dp)),
                )
                Text(item, color = LaunchUiTokens.inkSoft)
            }
        }
    }
}

@Composable
private fun TemplateSelectionCard(
    template: PhotoTemplate,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = LaunchUiTokens.glass,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            template.accent.copy(alpha = 0.14f),
                            Color.White,
                        ),
                    ),
                )
                .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(24.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(template.accent.copy(alpha = 0.28f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("Preview Template", color = LaunchUiTokens.inkSoft)
            }
            Text(template.name, style = MaterialTheme.typography.titleMedium, color = LaunchUiTokens.ink, fontWeight = FontWeight.Bold)
            Text(
                "${template.sizeLabel} • ${template.frameCount} frame",
                color = LaunchUiTokens.inkSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            LaunchPrimaryButton(
                text = "Pilih Template",
                onClick = onSelect,
                enabled = true,
            )
        }
    }
}


@Preview(name = "Launch Event Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun LaunchEventTabletPreview() {
    DafydioBoothTheme {
        LaunchEventScreen(
            state = launchPreviewBoothState,
            launchState = launchPreviewUiState,
            actions = BoothActions(),
            launchActions = LaunchActions(),
        )
    }
}


@Preview(name = "Pra Launch Event Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun PraLaunchEventTabletPreview() {
    DafydioBoothTheme {
        PraLaunchEventScreen(
            config = sampleLaunchConfig,
            onRunEventClick = {},
        )
    }
}

@Preview(name = "Event Welcome Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun EventWelcomeTabletPreview() {
    DafydioBoothTheme {
        EventWelcomeScreen(
            config = sampleLaunchConfig.welcomeConfig,
            eventName = sampleLaunchConfig.eventName,
            onStartClick = {},
        )
    }
}

@Preview(name = "Payment Screen Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun PaymentScreenTabletPreview() {
    DafydioBoothTheme {
        PaymentScreen(
            config = sampleLaunchConfig,
            onPaymentConfirmed = {},
        )
    }
}

@Preview(name = "Pick Template Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun PickTemplateTabletPreview() {
    DafydioBoothTheme {
        PickTemplateScreen(
            eventName = sampleLaunchConfig.eventName,
            templates = sampleTemplates,
            onTemplateSelected = {},
        )
    }
}

@Preview(name = "Capture Screen Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun CaptureScreenTabletPreview() {
    DafydioBoothTheme {
        CaptureScreen(
            eventName = sampleLaunchConfig.eventName,
            template = sampleTemplates.first(),
            onCaptureFinished = {},
        )
    }
}

@Preview(name = "Capture Screen Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CaptureScreenMobilePreview() {
    DafydioBoothTheme {
        CaptureScreen(
            eventName = sampleLaunchConfig.eventName,
            template = sampleTemplates.first(),
            onCaptureFinished = {},
        )
    }
}

@Preview(name = "Capture Finished Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun CaptureFinishedTabletPreview() {
    DafydioBoothTheme {
        CaptureFinishedScreen(
            eventName = sampleLaunchConfig.eventName,
            template = sampleTemplates.first(),
            onBackToWelcome = {},
            onStartNewSession = {},
        )
    }
}

@Preview(name = "Voucher Check Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun VoucherCheckTabletPreview() {
    DafydioBoothTheme {
        VoucherCheckScreen(
            state = launchPreviewBoothState.copy(
                step = BoothStep.VoucherCheck,
                eventStatusMessage = "Voucher belum diverifikasi. Masukkan kode lalu lanjutkan.",
            ),
            actions = BoothActions(),
        )
    }
}

@Preview(name = "Payment Gate Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun PaymentGateTabletPreview() {
    DafydioBoothTheme {
        PaymentGateScreen(
            state = launchPreviewBoothState.copy(
                step = BoothStep.PaymentGate,
                quote = launchPreviewUiState.quote,
                voucher = launchPreviewUiState.voucher,
                paymentMethod = "manual",
                launchSelectedEventId = "evt-001",
                eventStatusMessage = "Session siap dibuka setelah metode pembayaran dipilih.",
            ),
            actions = BoothActions(),
        )
    }
}

@Preview(name = "Waiting Approval Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun WaitingApprovalTabletPreview() {
    DafydioBoothTheme {
        WaitingApprovalScreen(
            state = launchPreviewBoothState.copy(
                step = BoothStep.WaitingApproval,
                eventStatusMessage = "Menunggu approval dari station operator.",
                session = BoothSession(
                    sessionId = "sess-001",
                    sessionCode = "SS-240528-01",
                    customerId = "CUST-001",
                    uploadUrl = null,
                    paymentStatus = "pending",
                    paymentRequired = true,
                    unlockPhoto = false,
                ),
                paymentStatus = PaymentStatus(
                    sessionId = "sess-001",
                    sessionCode = "SS-240528-01",
                    customerId = "CUST-001",
                    paymentStatus = "pending",
                    reviewStatus = "waiting",
                    approvalStatus = "pending",
                    canUpload = false,
                    paymentRequired = true,
                    unlockPhoto = false,
                    rejectionReason = null,
                ),
            ),
            actions = BoothActions(),
        )
    }
}
