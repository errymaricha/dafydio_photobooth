package com.errymaricha.dafydiobooth.ui.booth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import java.io.File
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
        val activeEvent = launchState.events.firstOrNull { it.eventId == launchState.selectedEventId }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Event Aktif", fontWeight = FontWeight.Bold)
                Text(activeEvent?.eventCode ?: "-")
                Text(activeEvent?.eventName ?: "Belum ada event terpilih")
            }
        }
        OutlinedTextField(
            value = launchState.customerWhatsapp,
            onValueChange = launchActions.onWhatsappChanged,
            label = { Text("Nomor Customer") },
            placeholder = { Text("628123456789") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = invalidWaMessage != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (invalidWaMessage != null) {
            Text(invalidWaMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = "Hanya angka. Kosong = default customer station.",
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
        launchState.session?.let { session ->
            val waiting = launchState.isManualPaymentWaiting
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (waiting) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (waiting) "Menunggu Approval" else "Status Session",
                        fontWeight = FontWeight.Bold,
                    )
                    Text("Customer ID: ${session.customerId ?: state.customerId.ifBlank { "-"} }")
                    Text("Kode Session: ${session.sessionCode ?: session.sessionId}")
                    Text("Approval: ${launchState.approvalStatus ?: session.paymentStatus}")
                }
            }
        } ?: Text(
            text = "Kode Session: -",
            fontWeight = FontWeight.Bold,
        )
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
fun SettingEventScreen(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
    launchActions: LaunchActions,
) {
    ScreenFrame(title = "Setting Event", state = state, actions = actions) {
        EventStatus(state)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Setting Default Device", fontWeight = FontWeight.Bold)
                Text("Perubahan disimpan otomatis di device untuk prefill flow Launch Event.")
                Text("Master event tersimpan di Station lewat endpoint /api/device/events.")
            }
        }
        Text("Create / Update Event (Station)", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = launchState.eventCodeInput,
            onValueChange = launchActions.onEventCodeChanged,
            label = { Text("Event Code") },
            placeholder = { Text("HBD-DAFYDIO-001") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = launchState.eventNameInput,
            onValueChange = launchActions.onEventNameChanged,
            label = { Text("Event Name") },
            placeholder = { Text("ULANG TAHUN DAFYDIO") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = launchActions.createOrUpdateEvent,
                enabled = !launchState.loading && state.isStationConnected,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (launchState.selectedEventId.isBlank()) "Create Event" else "Update Event")
            }
            OutlinedButton(
                onClick = actions.openLaunchEvent,
                enabled = state.isStationConnected,
                modifier = Modifier.weight(1f),
            ) {
                Text("Open Launch Event")
            }
        }
        Text("Pilih Event Aktif", fontWeight = FontWeight.Bold)
        Text(
            text = "Event aktif: ${launchState.events.firstOrNull { it.eventId == launchState.selectedEventId }?.eventName ?: "Belum dipilih"}",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedButton(
            onClick = launchActions.refreshEvents,
            enabled = !launchState.loading && state.isStationConnected,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sinkronkan Daftar Event")
        }
        if (launchState.events.isEmpty()) {
            Text(
                text = if (state.isStationConnected) {
                    "Belum ada event di station. Buat event baru, lalu sinkronkan daftar event."
                } else {
                    "Hubungkan station dulu untuk mengambil daftar event."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(launchState.events, key = { it.eventId }) { event ->
                    val selected = event.eventId == launchState.selectedEventId
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${event.eventCode} - ${event.eventName}", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "upload=${event.cloudUploadMode ?: "-"} | sync=${event.cloudSyncTiming ?: "-"}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    launchActions.onSelectEvent(event.eventId)
                                    actions.setLaunchSelectedEventId(event.eventId)
                                },
                                label = { Text(if (selected) "Aktif" else "Pilih") },
                            )
                        }
                    }
                }
            }
        }

        Text("Template Diizinkan Untuk Event", fontWeight = FontWeight.Bold)
        Text(
            text = if (state.launchAllowedTemplateIds.isEmpty()) {
                "Semua template diizinkan."
            } else {
                "${state.launchAllowedTemplateIds.size} template diizinkan."
            },
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedButton(
            onClick = actions.openSettingAllowedTemplates,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Buka Pengaturan Template Diizinkan")
        }

        Text("Input Customer", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = state.customerWhatsapp,
            onValueChange = actions.updateCustomerWhatsapp,
            label = { Text("Nomor Customer / WA") },
            placeholder = { Text("628123456789") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.launchAdditionalPrintCount.toString(),
            onValueChange = actions.updateLaunchAdditionalPrintCount,
            label = { Text("Default Additional Print") },
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Input Voucher", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = state.voucherCode,
            onValueChange = actions.updateVoucherCode,
            label = { Text("Default Voucher Code") },
            placeholder = { Text("Kosong jika tidak ada voucher") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = actions.openDashboard,
                modifier = Modifier.weight(1f),
            ) {
                Text("Back")
            }
            Button(
                onClick = actions.openLaunchEvent,
                enabled = state.isStationConnected,
                modifier = Modifier.weight(1f),
            ) {
                Text("Go to Launch Event")
            }
        }
        Text(
            text = if (state.isStationConnected) {
                "Station tersambung. Setting siap dipakai."
            } else {
                "Connect Photobooth Station dulu untuk memakai event setting."
            },
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun AllowedTemplatesScreen(state: BoothUiState, actions: BoothActions) {
    val query = state.launchTemplateSearchQuery.trim().lowercase()
    val filtered = if (query.isBlank()) {
        state.availableTemplateItems
    } else {
        state.availableTemplateItems.filter {
            it.templateName.lowercase().contains(query) ||
                it.templateCode.lowercase().contains(query) ||
                (it.category?.lowercase()?.contains(query) == true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Template Diizinkan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = actions.openSettingEvent) { Text("Kembali") }
        }
        Text("Event: ${state.launchEventName.ifBlank { "-" }}")
        OutlinedTextField(
            value = state.launchTemplateSearchQuery,
            onValueChange = actions.updateLaunchTemplateSearchQuery,
            label = { Text("Cari template") },
            placeholder = { Text("Nama / kode / kategori") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = actions.selectAllLaunchAllowedTemplates, modifier = Modifier.weight(1f)) {
                Text("Pilih All Template")
            }
            OutlinedButton(onClick = actions.clearLaunchAllowedTemplates, modifier = Modifier.weight(1f)) {
                Text("Kosongkan Pilihan")
            }
        }
        Text(
            text = "Ditampilkan ${filtered.size}/${state.availableTemplateItems.size} template. Terpilih ${state.launchAllowedTemplateIds.size}.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(items = filtered, key = { it.templateId }) { template ->
                val selected = state.launchAllowedTemplateIds.contains(template.templateId)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (!template.thumbnailUrl.isNullOrBlank()) {
                            val thumbnailModel: Any = template.thumbnailUrl
                                ?.takeIf { it.startsWith("/") || it.startsWith("file:/") }
                                ?.let { File(it.removePrefix("file://")) }
                                ?: template.thumbnailUrl
                            AsyncImage(
                                model = thumbnailModel,
                                contentDescription = "Thumbnail ${template.templateName}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(width = 88.dp, height = 58.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(template.templateName, fontWeight = FontWeight.Bold)
                            Text("${template.templateCode} | ${template.category ?: "-"}", style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(onClick = { actions.toggleLaunchAllowedTemplate(template.templateId) }) {
                            Text(if (selected) "Diizinkan" else "Izinkan")
                        }
                    }
                }
            }
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
