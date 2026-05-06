package com.errymaricha.dafydiobooth.ui.booth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.errymaricha.dafydiobooth.ui.launch.LaunchUiState

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
            isError = invalidWaMessage != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (invalidWaMessage != null) {
            Text(invalidWaMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
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
        launchState.message?.takeIf { !it.equals("No WA tidak valid", ignoreCase = true) }?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = "Kode Session: ${launchState.session?.sessionCode ?: "-"}",
            fontWeight = FontWeight.Bold,
        )
        Text("Approval: ${launchState.approvalStatus ?: launchState.session?.paymentStatus ?: "-"}")
        launchState.error?.takeIf { invalidWaMessage == null }?.let { error ->
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
fun SettingEventScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Setting Event", state = state, actions = actions) {
        Text("Event setting tersambung ke Photobooth Station.")
        Text("Konfigurasi event backend akan ditambahkan setelah contract event tersedia.")
        OutlinedButton(onClick = actions.openDashboard, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
fun VoucherCheckScreen(state: BoothUiState, actions: BoothActions) {
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
fun PaymentGateScreen(state: BoothUiState, actions: BoothActions) {
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
fun WaitingApprovalScreen(state: BoothUiState, actions: BoothActions) {
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
private fun EventStatus(state: BoothUiState) {
    state.eventStatusMessage?.let { message ->
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(text = message, modifier = Modifier.padding(14.dp))
        }
    }
}
