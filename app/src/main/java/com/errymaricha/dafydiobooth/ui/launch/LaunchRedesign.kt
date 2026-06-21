package com.errymaricha.dafydiobooth.ui.launch

import androidx.compose.foundation.background
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
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer
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
        initialConfig = state.toLaunchEventConfig(launchState),
        templates = state.availableTemplateItems.toLaunchTemplates(),
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
    ScreenFrame(title = "Launch Event", state = state, actions = actions) {
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
                if (launchState.loading) {
                    CircularProgressIndicator()
                }

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
    data object Dashboard : LaunchEventRoute("dashboard")
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
fun DashboardLaunchButton(
    onLaunchEventClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchPrimaryButton(
        text = "Launch Event",
        onClick = onLaunchEventClick,
        enabled = true,
        modifier = modifier,
    )
}

@Composable
fun LaunchEventNavHostDemo(
    modifier: Modifier = Modifier,
    initialConfig: EventLaunchConfig = sampleLaunchConfig,
    templates: List<PhotoTemplate> = sampleTemplates,
    kioskExitCode: String = "",
    welcomeBgUri: String = "",
    welcomeBgIsVideo: Boolean = false,
    onBackToDashboard: () -> Unit = {},
) {
    val navController = rememberNavController()
    var selectedTemplate by remember { mutableStateOf(templates.first()) }

    NavHost(
        navController = navController,
        startDestination = LaunchEventRoute.Dashboard.route,
        modifier = modifier,
    ) {
        composable(LaunchEventRoute.Dashboard.route) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LaunchUiTokens.softSurface)
                    .safeDrawingPadding()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                DashboardLaunchButton(
                    onLaunchEventClick = { navController.navigate(LaunchEventRoute.PraLaunch.route) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        composable(LaunchEventRoute.PraLaunch.route) {
            PraLaunchEventScreen(
                config = initialConfig,
                onRunEventClick = { navController.navigate(LaunchEventRoute.Welcome.route) },
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
            PaymentScreen(
                config = initialConfig,
                onPaymentConfirmed = { navController.navigate(LaunchEventRoute.PickTemplate.route) },
            )
        }
        composable(LaunchEventRoute.PickTemplate.route) {
            PickTemplateScreen(
                eventName = initialConfig.eventName,
                templates = templates,
                onTemplateSelected = {
                    selectedTemplate = it
                    navController.navigate(LaunchEventRoute.Capture.route)
                },
            )
        }
        composable(LaunchEventRoute.Capture.route) {
            CaptureScreen(
                eventName = initialConfig.eventName,
                template = selectedTemplate,
                onCaptureFinished = { navController.navigate(LaunchEventRoute.Finished.route) },
                kioskExitCode = kioskExitCode,
                onExitToDashboard = {
                    navController.navigate(LaunchEventRoute.Dashboard.route) {
                        popUpTo(LaunchEventRoute.Dashboard.route) { inclusive = true }
                    }
                },
            )
        }
        composable(LaunchEventRoute.Finished.route) {
            CaptureFinishedScreen(
                eventName = initialConfig.eventName,
                template = selectedTemplate,
                kioskExitCode = kioskExitCode,
                onBackToWelcome = {
                    navController.navigate(LaunchEventRoute.Welcome.route) {
                        popUpTo(LaunchEventRoute.Welcome.route) { inclusive = true }
                    }
                },
                onStartNewSession = {
                    navController.navigate(LaunchEventRoute.Payment.route) {
                        popUpTo(LaunchEventRoute.Payment.route) { inclusive = true }
                    }
                },
                onExitToDashboard = {
                    navController.navigate(LaunchEventRoute.Dashboard.route) {
                        popUpTo(LaunchEventRoute.Dashboard.route) { inclusive = true }
                    }
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

private fun List<com.errymaricha.dafydiobooth.ui.booth.TemplateListItem>.toLaunchTemplates(): List<PhotoTemplate> {
    if (isEmpty()) return sampleTemplates
    return map { template ->
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
                    title = "Pra Launch Event",
                    subtitle = "Cek seluruh konfigurasi event sebelum station masuk ke mode kiosk.",
                    badge = config.statusLabel,
                    badgeAccent = LaunchUiTokens.success,
                )
            }
            if (isTablet) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            EventInfoCard(config)
                            WelcomeConfigCard(config.welcomeConfig, config.defaultTemplateLabel)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            TransactionConfigCard(config)
                            ChecklistCard(config.checklist)
                        }
                    }
                }
            } else {
                item { EventInfoCard(config) }
                item { WelcomeConfigCard(config.welcomeConfig, config.defaultTemplateLabel) }
                item { TransactionConfigCard(config) }
                item { ChecklistCard(config.checklist) }
            }
            item {
                LaunchPrimaryButton(
                    text = "Run Event",
                    onClick = onRunEventClick,
                    enabled = true,
                )
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
    val file = remember(filePath) { File(filePath) }
    if (!file.exists()) {
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
            if (videoView.tag != filePath) {
                videoView.tag = filePath
                try {
                    videoView.setVideoPath(filePath)
                    videoView.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
                    model = File(welcomeBgUri),
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
fun PaymentScreen(
    config: EventLaunchConfig,
    onPaymentConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var whatsapp by remember(config.paymentConfig.defaultWhatsapp) { mutableStateOf(config.paymentConfig.defaultWhatsapp) }
    var voucher by remember(config.paymentConfig.defaultVoucher?.code) { mutableStateOf(config.paymentConfig.defaultVoucher?.code.orEmpty()) }
    var selectedPayment by remember { mutableStateOf(config.paymentConfig.availablePaymentTypes.first()) }

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
                    subtitle = "Lengkapi data guest dan konfirmasi pembayaran sebelum pilih template.",
                    badge = "READY",
                    badgeAccent = LaunchUiTokens.primary,
                )
            }
            if (isTablet) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                        LaunchSectionCard(title = "Data Transaksi", modifier = Modifier.weight(1f)) {
                            OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("Nomor WhatsApp") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = voucher, onValueChange = { voucher = it }, label = { Text("Voucher") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Text("Tipe Pembayaran", color = LaunchUiTokens.ink, fontWeight = FontWeight.SemiBold)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                config.paymentConfig.availablePaymentTypes.forEach { option ->
                                    FilterChip(
                                        selected = selectedPayment == option,
                                        onClick = { selectedPayment = option },
                                        label = { Text(option.label) },
                                    )
                                }
                            }
                        }
                        LaunchSectionCard(title = "Ringkasan Order", modifier = Modifier.weight(1f)) {
                            LaunchMetricRow(
                                "Event" to config.orderSummary.eventName,
                                "Paket" to config.orderSummary.packageName,
                            )
                            LaunchMetricRow(
                                "Harga" to config.orderSummary.priceLabel,
                                "Diskon" to if (voucher.isBlank()) "IDR 0" else config.orderSummary.discountLabel,
                                "Total" to config.orderSummary.totalLabel,
                            )
                            LaunchPrimaryButton(
                                text = "Konfirmasi Pembayaran",
                                onClick = onPaymentConfirmed,
                                enabled = true,
                            )
                        }
                    }
                }
            } else {
                item {
                    LaunchSectionCard(title = "Data Transaksi") {
                        OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("Nomor WhatsApp") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = voucher, onValueChange = { voucher = it }, label = { Text("Voucher") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Text("Tipe Pembayaran", color = LaunchUiTokens.ink, fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            config.paymentConfig.availablePaymentTypes.forEach { option ->
                                FilterChip(
                                    selected = selectedPayment == option,
                                    onClick = { selectedPayment = option },
                                    label = { Text(option.label) },
                                )
                            }
                        }
                    }
                }
                item {
                    LaunchSectionCard(title = "Ringkasan Order") {
                        LaunchMetricRow(
                            "Event" to config.orderSummary.eventName,
                            "Paket" to config.orderSummary.packageName,
                        )
                        LaunchMetricRow(
                            "Harga" to config.orderSummary.priceLabel,
                            "Diskon" to if (voucher.isBlank()) "IDR 0" else config.orderSummary.discountLabel,
                            "Total" to config.orderSummary.totalLabel,
                        )
                        LaunchPrimaryButton(
                            text = "Konfirmasi Pembayaran",
                            onClick = onPaymentConfirmed,
                            enabled = true,
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
    onCaptureFinished: () -> Unit,
    kioskExitCode: String = "123456",
    onExitToDashboard: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
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
                        text = "${template.sizeLabel} • ${template.frameCount} frame",
                        color = LaunchUiTokens.inkSoft,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                template.accent.copy(alpha = 0.22f),
                                RoundedCornerShape(20.dp),
                            )
                            .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Preview template", color = LaunchUiTokens.inkSoft)
                    }
                    Text(
                        text = "Hasil capture",
                        color = LaunchUiTokens.ink,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        repeat(template.frameCount.coerceAtMost(4)) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(110.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                template.accent.copy(alpha = 0.26f),
                                                Color.White,
                                            ),
                                        ),
                                        RoundedCornerShape(18.dp),
                                    )
                                    .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (index == 0) LaunchUiTokens.primary.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.82f),
                                    ) {
                                        Text(
                                            text = if (index == 0) "Aktif" else "Frame ${index + 1}",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            color = if (index == 0) LaunchUiTokens.primary else LaunchUiTokens.inkSoft,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    Text("Shot ${index + 1}", color = LaunchUiTokens.inkSoft)
                                }
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1.42f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    LaunchStatusPills(
                        "Event" to eventName,
                        "Template" to template.name,
                        "Countdown" to "3s",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        template.accent.copy(alpha = 0.24f),
                                        Color.White,
                                    ),
                                ),
                                RoundedCornerShape(28.dp),
                            )
                            .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Placeholder Camera Preview",
                            color = LaunchUiTokens.inkSoft,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    LaunchPrimaryButton(
                        text = "Capture",
                        onClick = onCaptureFinished,
                        enabled = true,
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxSize()) {
                LaunchStatusPills(
                    "Event" to eventName,
                    "Template" to template.name,
                    "Countdown" to "3s",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    template.accent.copy(alpha = 0.24f),
                                    Color.White,
                                ),
                            ),
                            RoundedCornerShape(28.dp),
                        )
                        .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Placeholder Camera Preview",
                        color = LaunchUiTokens.inkSoft,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                LaunchPrimaryButton(
                    text = "Capture",
                    onClick = onCaptureFinished,
                    enabled = true,
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
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LaunchUiTokens.softSurface)
            .safeDrawingPadding()
            .padding(24.dp),
    ) {
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
                            .aspectRatio(2f / 3f), // 4x6 format
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
                                        .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(14.dp)),
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
                    )
                    
                    if (onPrintClick != null || onDownloadClick != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (onDownloadClick != null) {
                                OutlinedButton(
                                    onClick = onDownloadClick,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, LaunchUiTokens.primary.copy(alpha = 0.3f))
                                ) {
                                    Text("Download", color = LaunchUiTokens.primary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (onPrintClick != null) {
                                val printing = mockPrintStatus == MockPrintStatus.Queued
                                Button(
                                    onClick = onPrintClick,
                                    enabled = !printing,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LaunchUiTokens.purple)
                                ) {
                                    Text(
                                        text = if (printing) "Printing..." else "Print Strip",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
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
                            text = "Kembali ke Dashboard",
                            onClick = onExitToDashboard,
                            enabled = true,
                        )
                    } else {
                        LaunchSecondaryButton(
                            text = "Kembali ke Welcome Screen",
                            onClick = onBackToWelcome,
                            enabled = true,
                        )
                        LaunchPrimaryButton(
                            text = "Mulai Sesi Baru",
                            onClick = onStartNewSession,
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
                LaunchHeroCard(
                    title = if (isQuickBooth) "Sesi Quick Booth selesai" else "Sesi foto selesai",
                    subtitle = "Preview hasil sesi untuk event $eventName dengan template ${template.name}.",
                    badge = "DONE",
                    badgeAccent = LaunchUiTokens.success,
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f), // Lock aspect ratio for 4x6 photostrip
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
                )
                
                if (onPrintClick != null || onDownloadClick != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (onDownloadClick != null) {
                            OutlinedButton(
                                onClick = onDownloadClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, LaunchUiTokens.primary.copy(alpha = 0.3f))
                            ) {
                                Text("Download", color = LaunchUiTokens.primary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (onPrintClick != null) {
                            val printing = mockPrintStatus == MockPrintStatus.Queued
                            Button(
                                onClick = onPrintClick,
                                enabled = !printing,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LaunchUiTokens.purple)
                            ) {
                                Text(
                                    text = if (printing) "Printing..." else "Print Strip",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
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
                        text = "Kembali ke Dashboard",
                        onClick = onExitToDashboard,
                        enabled = true,
                    )
                } else {
                    LaunchSecondaryButton(
                        text = "Kembali ke Welcome Screen",
                        onClick = onBackToWelcome,
                        enabled = true,
                    )
                    LaunchPrimaryButton(
                        text = "Mulai Sesi Baru",
                        onClick = onStartNewSession,
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
}

@Composable
private fun CloudQrCard(
    sessionCode: String,
    cloudDownloadUrl: String,
) {
    LaunchSectionCard(title = "Download Cloud") {
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
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .clickable {
                    tapCount += 1
                    if (tapCount >= 5) {
                        tapCount = 0
                        showDialog = true
                    }
                },
            shape = RoundedCornerShape(999.dp),
            color = Color.White.copy(alpha = 0.14f),
        ) {
            Text(
                text = "Dafydio",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = LaunchUiTokens.ink,
                fontWeight = FontWeight.SemiBold,
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

@Composable
private fun FakeQrCode(
    value: String,
    modifier: Modifier = Modifier,
) {
    val cells = remember(value) {
        List(11) { row ->
            List(11) { col ->
                ((value.hashCode() shr ((row * 11 + col) % 24)) and 1) == 1 ||
                    (row < 3 && col < 3) ||
                    (row < 3 && col > 7) ||
                    (row > 7 && col < 3)
            }
        }
    }
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, LaunchUiTokens.border, RoundedCornerShape(20.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        cells.forEach { row ->
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                row.forEach { filled ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (filled) LaunchUiTokens.ink else Color.Transparent,
                                RoundedCornerShape(2.dp),
                            ),
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

@Preview(name = "Dashboard Launch Button", widthDp = 390, heightDp = 120, showBackground = true)
@Composable
private fun DashboardLaunchButtonPreview() {
    DafydioBoothTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LaunchUiTokens.softSurface)
                .padding(16.dp),
        ) {
            DashboardLaunchButton(onLaunchEventClick = {})
        }
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
