package com.errymaricha.dafydiobooth.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import java.io.File
import android.net.Uri
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable

import androidx.compose.ui.tooling.preview.Preview
import com.errymaricha.dafydiobooth.domain.model.LaunchEvent
import com.errymaricha.dafydiobooth.ui.booth.BoothStep
import com.errymaricha.dafydiobooth.ui.booth.TemplateListItem
import com.errymaricha.dafydiobooth.ui.booth.preview.PreviewStateProvider
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme
import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.BoothUiState
import com.errymaricha.dafydiobooth.ui.booth.ScreenFrame
import com.errymaricha.dafydiobooth.ui.booth.LaunchActions
import com.errymaricha.dafydiobooth.ui.launch.LaunchUiState


private val eventsPreviewLaunchState = LaunchUiState(
    events = listOf(
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
            eventCode = "GRAD-014",
            eventName = "Graduation Memory Booth",
            cloudEnabled = true,
            cloudUploadMode = "batched",
            cloudSyncTiming = "hourly",
            cloudTemplateMarketplaceEnabled = false,
        ),
    ),
    selectedEventId = "evt-001",
    eventCodeInput = "PROM-001",
    eventNameInput = "Prom Night Dafydio",
)

private val eventsPreviewBoothState = PreviewStateProvider.settingEventBase.copy(
    step = BoothStep.SettingEvent,
    stationIp = "10.10.116.4:8000",
    isStationConnected = true,
    launchSelectedEventId = "evt-001",
    launchAllowedTemplateIds = setOf("tpl-01", "tpl-03"),
    eventStatusMessage = "Event station tersinkron. Pilih event aktif dan atur template yang diizinkan.",
    availableTemplateItems = listOf(
        TemplateListItem(
            templateId = "tpl-01",
            templateName = "Mirror Pop",
            templateCode = "MR-POP-01",
            category = "Birthday",
            paperSize = "4R Strip",
            thumbnailUrl = null,
            thumbnailReady = true,
            previewReady = true,
            overlayReady = true,
            slotCount = 4,
        ),
        TemplateListItem(
            templateId = "tpl-02",
            templateName = "Glow Frame",
            templateCode = "GLW-FRM-02",
            category = "Korean Booth",
            paperSize = "4R",
            thumbnailUrl = null,
            thumbnailReady = true,
            previewReady = true,
            overlayReady = true,
            slotCount = 3,
        ),
        TemplateListItem(
            templateId = "tpl-03",
            templateName = "Soft Flash",
            templateCode = "SFT-FLS-03",
            category = "Wedding",
            paperSize = "2x6 Strip",
            thumbnailUrl = null,
            thumbnailReady = true,
            previewReady = true,
            overlayReady = true,
            slotCount = 4,
        ),
        TemplateListItem(
            templateId = "tpl-04",
            templateName = "After Party",
            templateCode = "AFT-PTY-04",
            category = "Party",
            paperSize = "4R",
            thumbnailUrl = null,
            thumbnailReady = true,
            previewReady = true,
            overlayReady = true,
            slotCount = 2,
        ),
    ),
)

private object EventsUiTokens {
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
fun EventsPageRedesign(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
    launchActions: LaunchActions,
    isShortcutMode: Boolean = false,
) {
    SettingEventScreen(
        state = state,
        launchState = launchState,
        actions = actions,
        launchActions = launchActions,
        isShortcutMode = isShortcutMode,
    )
}

@Composable
fun AllowedTemplatesPageRedesign(
    state: BoothUiState,
    actions: BoothActions,
) {
    AllowedTemplatesScreen(state = state, actions = actions)
}

@Composable
private fun WelcomeScreenConfigCard(
    state: BoothUiState,
    activeEventName: String,
    actions: BoothActions,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val path = copyUriToLocalFile(context, uri, "welcome_img")
                if (path != null) {
                    actions.setWelcomeBgUri(path)
                    actions.setWelcomeBgIsVideo(false)
                }
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val path = copyUriToLocalFile(context, uri, "welcome_vid")
                if (path != null) {
                    actions.setWelcomeBgUri(path)
                    actions.setWelcomeBgIsVideo(true)
                }
            }
        }
    }

    EventSectionCard(title = "Welcome Screen") {
        WelcomePreviewCard(
            title = activeEventName,
            subtitle = "Smile, pose, and make your memory.",
            countdownLabel = "Countdown ${state.countdownSeconds}s",
            startLabel = "Mulai",
            welcomeBgUri = state.welcomeBgUri,
            welcomeBgIsVideo = state.welcomeBgIsVideo,
        )

        Spacer(Modifier.height(10.dp))

        // Picker Buttons Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Button to choose Image
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, EventsUiTokens.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        "Gambar BG",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = EventsUiTokens.primary
                    )
                }
            }

            // Button to choose Video
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { videoPickerLauncher.launch("video/*") },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, EventsUiTokens.purple.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        "Video BG",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = EventsUiTokens.purple
                    )
                }
            }

            // Reset button
            if (state.welcomeBgUri.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .clickable {
                            actions.setWelcomeBgUri("")
                            actions.setWelcomeBgIsVideo(false)
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEE2E2), // soft red
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            "Reset",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Kustom latar belakang welcome screen untuk event aktif. Gambar/video kustom disimpan di perangkat lokal.",
            style = MaterialTheme.typography.bodySmall,
            color = EventsUiTokens.inkSoft,
        )
    }
}

@Composable
fun SettingEventScreen(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
    launchActions: LaunchActions,
    isShortcutMode: Boolean = false,
) {
    ScreenFrame(title = if (isShortcutMode) "Setting Event Aktif" else "Event Manager", state = state, actions = actions) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isTablet = maxWidth >= 900.dp
            val activeEvent = launchState.events.firstOrNull { it.eventId == launchState.selectedEventId }

            val marginLeftText = remember(state.printerMarginLeft) { mutableStateOf(if (state.printerMarginLeft == 0.0f) "" else state.printerMarginLeft.toString()) }
            val marginRightText = remember(state.printerMarginRight) { mutableStateOf(if (state.printerMarginRight == 0.0f) "" else state.printerMarginRight.toString()) }
            val marginTopText = remember(state.printerMarginTop) { mutableStateOf(if (state.printerMarginTop == 0.0f) "" else state.printerMarginTop.toString()) }
            val marginBottomText = remember(state.printerMarginBottom) { mutableStateOf(if (state.printerMarginBottom == 0.0f) "" else state.printerMarginBottom.toString()) }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                EventStatus(state)
                EventsHeroCard(
                    title = if (isShortcutMode) "Event Setup" else "Event Manager",
                    subtitle = if (isShortcutMode)
                        "Atur konfigurasi default session prefill, welcome screen background, dan printer output untuk event yang aktif."
                    else
                        "Kelola daftar event di station lokal, pilih event aktif, atau buat event baru.",
                    badge = if (state.isStationReachable) "SYNCED" else "OFFLINE",
                    badgeAccent = if (state.isStationReachable) EventsUiTokens.success else EventsUiTokens.warning,
                )
                EventInfoRow(
                    "Event aktif" to (activeEvent?.eventName ?: "Belum dipilih"),
                    "Template aktif" to if (state.launchAllowedTemplateIds.isEmpty()) "Semua" else "${state.launchAllowedTemplateIds.size} selected",
                    "Station" to if (state.isStationReachable) "Connected" else "Offline",
                )

                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(0.95f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (!isShortcutMode) {
                                EventSectionCard(title = "General Event Setup") {
                                    OutlinedTextField(value = launchState.eventCodeInput, onValueChange = launchActions.onEventCodeChanged, label = { Text("Event Code") }, placeholder = { Text("HBD-DAFYDIO-001") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = launchState.eventNameInput, onValueChange = launchActions.onEventNameChanged, label = { Text("Event Name") }, placeholder = { Text("ULANG TAHUN DAFYDIO") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    Button(
                                        onClick = launchActions.createOrUpdateEvent,
                                        enabled = !launchState.loading && state.isStationReachable,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (launchState.selectedEventId.isBlank()) "Create Event" else "Update Event")
                                    }
                                }
                            }
                            if (isShortcutMode) {
                                EventSectionCard(title = "Default Session Prefills") {
                                    OutlinedTextField(value = state.customerWhatsapp, onValueChange = actions.updateCustomerWhatsapp, label = { Text("Nomor Customer / WA") }, placeholder = { Text("628123456789") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = state.launchAdditionalPrintCount.toString(), onValueChange = actions.updateLaunchAdditionalPrintCount, label = { Text("Default Additional Print") }, placeholder = { Text("0") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedTextField(value = state.voucherCode, onValueChange = actions.updateVoucherCode, label = { Text("Default Voucher Code") }, placeholder = { Text("Kosong jika tidak ada voucher") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                }
                                PrintSettingsCard(
                                    state = state,
                                    actions = actions,
                                    marginLeftText = marginLeftText,
                                    marginRightText = marginRightText,
                                    marginTopText = marginTopText,
                                    marginBottomText = marginBottomText,
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1.05f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (isShortcutMode) {
                                WelcomeScreenConfigCard(
                                    state = state,
                                    activeEventName = activeEvent?.eventName ?: launchState.eventNameInput.ifBlank { "Welcome Event" },
                                    actions = actions,
                                )
                            }
                            if (!isShortcutMode) {
                                EventSectionCard(title = "Pilih Event Aktif") {
                                    Text(text = "Event aktif: ${activeEvent?.eventName ?: "Belum dipilih"}", style = MaterialTheme.typography.bodySmall)
                                    OutlinedButton(onClick = launchActions.refreshEvents, enabled = !launchState.loading && state.isStationReachable, modifier = Modifier.fillMaxWidth()) {
                                        Text("Sinkronkan Daftar Event")
                                    }
                                    if (launchState.events.isEmpty()) {
                                        Text(
                                            text = if (state.isStationReachable) "Belum ada event di station. Buat event baru, lalu sinkronkan daftar event." else "Hubungkan station dulu untuk mengambil daftar event.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    } else {
                                        LazyColumn(modifier = Modifier.fillMaxWidth().height(380.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(launchState.events, key = { it.eventId }) { event ->
                                                EventSelectionRow(
                                                    title = event.eventName,
                                                    subtitle = "${event.eventCode} | cloud=${if (event.cloudEnabled) "on" else "off"} | sync=${event.cloudSyncTiming ?: "-"}",
                                                    selected = event.eventId == launchState.selectedEventId,
                                                    onSelect = {
                                                        launchActions.onSelectEvent(event.eventId)
                                                        actions.setLaunchSelectedEventId(event.eventId)
                                                    },
                                                    onClickActive = actions.openSettingEvent,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Mobile layout
                    if (!isShortcutMode) {
                        EventSectionCard(title = "General Event Setup") {
                            OutlinedTextField(value = launchState.eventCodeInput, onValueChange = launchActions.onEventCodeChanged, label = { Text("Event Code") }, placeholder = { Text("HBD-DAFYDIO-001") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = launchState.eventNameInput, onValueChange = launchActions.onEventNameChanged, label = { Text("Event Name") }, placeholder = { Text("ULANG TAHUN DAFYDIO") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Button(
                                onClick = launchActions.createOrUpdateEvent,
                                enabled = !launchState.loading && state.isStationReachable,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (launchState.selectedEventId.isBlank()) "Create Event" else "Update Event")
                            }
                        }
                    }
                    if (isShortcutMode) {
                        EventSectionCard(title = "Default Session Prefills") {
                            OutlinedTextField(value = state.customerWhatsapp, onValueChange = actions.updateCustomerWhatsapp, label = { Text("Nomor Customer / WA") }, placeholder = { Text("628123456789") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = state.launchAdditionalPrintCount.toString(), onValueChange = actions.updateLaunchAdditionalPrintCount, label = { Text("Default Additional Print") }, placeholder = { Text("0") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(value = state.voucherCode, onValueChange = actions.updateVoucherCode, label = { Text("Default Voucher Code") }, placeholder = { Text("Kosong jika tidak ada voucher") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                        WelcomeScreenConfigCard(
                            state = state,
                            activeEventName = activeEvent?.eventName ?: launchState.eventNameInput.ifBlank { "Welcome Event" },
                            actions = actions,
                        )
                        PrintSettingsCard(
                            state = state,
                            actions = actions,
                            marginLeftText = marginLeftText,
                            marginRightText = marginRightText,
                            marginTopText = marginTopText,
                            marginBottomText = marginBottomText,
                        )
                    }
                    if (!isShortcutMode) {
                        EventSectionCard(title = "Pilih Event Aktif") {
                            Text(text = "Event aktif: ${activeEvent?.eventName ?: "Belum dipilih"}", style = MaterialTheme.typography.bodySmall)
                            OutlinedButton(onClick = launchActions.refreshEvents, enabled = !launchState.loading && state.isStationReachable, modifier = Modifier.fillMaxWidth()) {
                                Text("Sinkronkan Daftar Event")
                            }
                            if (launchState.events.isEmpty()) {
                                Text(
                                    text = if (state.isStationReachable) "Belum ada event di station. Buat event baru, lalu sinkronkan daftar event." else "Hubungkan station dulu untuk mengambil daftar event.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(launchState.events, key = { it.eventId }) { event ->
                                        EventSelectionRow(
                                            title = event.eventName,
                                            subtitle = "${event.eventCode} | cloud=${if (event.cloudEnabled) "on" else "off"} | sync=${event.cloudSyncTiming ?: "-"}",
                                            selected = event.eventId == launchState.selectedEventId,
                                            onSelect = {
                                                launchActions.onSelectEvent(event.eventId)
                                                actions.setLaunchSelectedEventId(event.eventId)
                                            },
                                            onClickActive = actions.openSettingEvent,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = actions.openDashboard, modifier = Modifier.weight(1f)) { Text("Back") }
                    Button(onClick = actions.openLaunchEvent, enabled = state.isStationReachable, modifier = Modifier.weight(1f)) { Text("Launch Event") }
                }
            }
        }
    }
}

@Composable
private fun PrintSettingsCard(
    state: BoothUiState,
    actions: BoothActions,
    marginLeftText: MutableState<String>,
    marginRightText: MutableState<String>,
    marginTopText: MutableState<String>,
    marginBottomText: MutableState<String>,
) {
    EventSectionCard(title = "Print Settings") {
        Text(
            text = "Pilih output print default untuk event aktif.",
            style = MaterialTheme.typography.bodySmall,
            color = EventsUiTokens.inkSoft,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { actions.setPrintUsePhotoboothStation(true) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.printUsePhotoboothStation) EventsUiTokens.primary else Color.White,
                    contentColor = if (state.printUsePhotoboothStation) Color.White else EventsUiTokens.ink,
                ),
                border = if (state.printUsePhotoboothStation) null else BorderStroke(1.dp, EventsUiTokens.border),
            ) {
                Text("Via Station")
            }
            Button(
                onClick = { actions.setPrintUsePhotoboothStation(false) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!state.printUsePhotoboothStation) EventsUiTokens.primary else Color.White,
                    contentColor = if (!state.printUsePhotoboothStation) Color.White else EventsUiTokens.ink,
                ),
                border = if (!state.printUsePhotoboothStation) null else BorderStroke(1.dp, EventsUiTokens.border),
            ) {
                Text("Android Printer")
            }
        }

        if (!state.printUsePhotoboothStation) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Printer Margins (mm):",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = EventsUiTokens.inkSoft,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = marginLeftText.value,
                    onValueChange = { newVal ->
                        marginLeftText.value = newVal
                        newVal.toFloatOrNull()?.let { actions.setPrinterMarginLeft(it) }
                    },
                    label = { Text("Left") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = marginRightText.value,
                    onValueChange = { newVal ->
                        marginRightText.value = newVal
                        newVal.toFloatOrNull()?.let { actions.setPrinterMarginRight(it) }
                    },
                    label = { Text("Right") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = marginTopText.value,
                    onValueChange = { newVal ->
                        marginTopText.value = newVal
                        newVal.toFloatOrNull()?.let { actions.setPrinterMarginTop(it) }
                    },
                    label = { Text("Top") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = marginBottomText.value,
                    onValueChange = { newVal ->
                        marginBottomText.value = newVal
                        newVal.toFloatOrNull()?.let { actions.setPrinterMarginBottom(it) }
                    },
                    label = { Text("Bottom") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Print Scale Mode:",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = EventsUiTokens.inkSoft,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { actions.setPrinterScaleMode("fit") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.printerScaleMode == "fit") EventsUiTokens.primary else Color.White,
                        contentColor = if (state.printerScaleMode == "fit") Color.White else EventsUiTokens.ink,
                    ),
                    border = if (state.printerScaleMode == "fit") null else BorderStroke(1.dp, EventsUiTokens.border),
                ) {
                    Text("Fit to Page")
                }
                Button(
                    onClick = { actions.setPrinterScaleMode("fill") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.printerScaleMode == "fill") EventsUiTokens.primary else Color.White,
                        contentColor = if (state.printerScaleMode == "fill") Color.White else EventsUiTokens.ink,
                    ),
                    border = if (state.printerScaleMode == "fill") null else BorderStroke(1.dp, EventsUiTokens.border),
                ) {
                    Text("Fill Page")
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        OutlinedButton(onClick = actions.openSettingAllowedTemplates, modifier = Modifier.fillMaxWidth()) {
            Text("Pengaturan Template Diizinkan")
        }
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 900.dp
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
            EventsHeroCard(
                title = state.launchEventName.ifBlank { "Template Access" },
                subtitle = "Batasi template yang boleh dipakai di event aktif.",
                badge = "${state.launchAllowedTemplateIds.size} selected",
                badgeAccent = EventsUiTokens.purple,
            )
            EventInfoRow(
                "Visible" to filtered.size.toString(),
                "Selected" to state.launchAllowedTemplateIds.size.toString(),
                "Source" to "Station import",
            )
            EventSectionCard(title = "Filter & Selection") {
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
                        Text("Pilih Semua")
                    }
                    OutlinedButton(onClick = actions.clearLaunchAllowedTemplates, modifier = Modifier.weight(1f)) {
                        Text("Kosongkan")
                    }
                }
                Text(
                    text = "Pilih template yang boleh muncul saat operator masuk ke flow launch event.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EventsUiTokens.inkSoft,
                )
            }
            EventSectionCard(title = "Template Library") {
                Text(
                    text = "Ditampilkan ${filtered.size}/${state.availableTemplateItems.size} template. Terpilih ${state.launchAllowedTemplateIds.size}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EventsUiTokens.inkSoft,
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isTablet) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                        filtered.chunked(4).forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                row.forEach { template ->
                                    val selected = state.launchAllowedTemplateIds.contains(template.templateId)
                                    AllowedTemplateCard(
                                        templateName = template.templateName,
                                        templateCode = template.templateCode,
                                        category = template.category ?: "-",
                                        paperSize = template.paperSize ?: "-",
                                        slotCount = template.slotCount,
                                        thumbnailUrl = template.thumbnailUrl,
                                        selected = selected,
                                        modifier = Modifier.weight(1f).height(330.dp),
                                        onToggle = { actions.toggleLaunchAllowedTemplate(template.templateId) },
                                    )
                                }
                                repeat(4 - row.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        items(items = filtered, key = { it.templateId }) { template ->
                            val selected = state.launchAllowedTemplateIds.contains(template.templateId)
                            AllowedTemplateCard(
                                templateName = template.templateName,
                                templateCode = template.templateCode,
                                category = template.category ?: "-",
                                paperSize = template.paperSize ?: "-",
                                slotCount = template.slotCount,
                                thumbnailUrl = template.thumbnailUrl,
                                selected = selected,
                                modifier = Modifier.fillMaxWidth(),
                                onToggle = { actions.toggleLaunchAllowedTemplate(template.templateId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventStatus(state: BoothUiState) {
    state.eventStatusMessage?.let { message ->
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = EventsUiTokens.glass,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                EventsUiTokens.primary.copy(alpha = 0.08f),
                                EventsUiTokens.pink.copy(alpha = 0.06f),
                            ),
                        ),
                    )
                    .border(1.dp, EventsUiTokens.border, RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = EventsUiTokens.success.copy(alpha = 0.18f),
                    modifier = Modifier.size(12.dp),
                ) {}
                Text(
                    text = message,
                    color = EventsUiTokens.ink,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun EventSelectionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit,
    onClickActive: () -> Unit = {},
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (selected) {
                    onClickActive()
                } else {
                    onSelect()
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 10.dp else 4.dp),
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = if (selected) {
                            listOf(
                                EventsUiTokens.primary.copy(alpha = 0.10f),
                                EventsUiTokens.pink.copy(alpha = 0.06f),
                            )
                        } else {
                            listOf(
                                Color.White,
                                EventsUiTokens.softSurface,
                            )
                        },
                    ),
                )
                .border(1.dp, if (selected) EventsUiTokens.primary.copy(alpha = 0.28f) else EventsUiTokens.border, RoundedCornerShape(22.dp))
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, fontWeight = FontWeight.Bold, color = EventsUiTokens.ink)
                Text(subtitle.replace("â€¢", "|"), style = MaterialTheme.typography.bodySmall, color = EventsUiTokens.inkSoft)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) EventsUiTokens.primary.copy(alpha = 0.14f) else EventsUiTokens.softSurface,
                ) {
                    Text(
                        if (selected) "Event aktif • Tap untuk edit setting" else "Tap untuk jadikan event aktif",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) EventsUiTokens.primary else EventsUiTokens.inkSoft,
                    )
                }
            }
            FilterChip(
                selected = selected,
                onClick = {
                    if (selected) {
                        onClickActive()
                    } else {
                        onSelect()
                    }
                },
                label = { Text(if (selected) "Setting" else "Pilih") },
            )
        }
    }
}

@Composable
private fun AllowedTemplateCard(
    templateName: String,
    templateCode: String,
    category: String,
    paperSize: String,
    slotCount: Int,
    thumbnailUrl: String?,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 10.dp else 5.dp),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = if (selected) {
                            listOf(
                                EventsUiTokens.primary.copy(alpha = 0.10f),
                                EventsUiTokens.pink.copy(alpha = 0.06f),
                                Color.White,
                            )
                        } else {
                            listOf(
                                Color.White,
                                EventsUiTokens.softSurface,
                            )
                        },
                    ),
                )
                .border(1.dp, if (selected) EventsUiTokens.primary.copy(alpha = 0.24f) else EventsUiTokens.border, RoundedCornerShape(24.dp))
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!thumbnailUrl.isNullOrBlank()) {
                val thumbnailModel: Any = thumbnailUrl
                    .takeIf { it.startsWith("/") || it.startsWith("file:/") }
                    ?.let { File(it.removePrefix("file://")) }
                    ?: thumbnailUrl
                AsyncImage(
                    model = thumbnailModel,
                    contentDescription = "Thumbnail $templateName",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(108.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    EventsUiTokens.purple.copy(alpha = 0.22f),
                                    EventsUiTokens.pink.copy(alpha = 0.16f),
                                    Color.White,
                                ),
                            ),
                        ),
                ) {
                    Text(
                        text = "Template Preview",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = EventsUiTokens.ink,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EventMetaChip(label = category, accent = EventsUiTokens.purple)
                EventMetaChip(label = templateCode, accent = EventsUiTokens.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EventMetaChip(label = paperSize, accent = EventsUiTokens.warning)
                EventMetaChip(label = "$slotCount slot", accent = EventsUiTokens.success)
            }
            Text(templateName, fontWeight = FontWeight.Bold, color = EventsUiTokens.ink)
            Text("Template ini tersedia untuk event selection operator.", style = MaterialTheme.typography.bodySmall, color = EventsUiTokens.inkSoft)
            Spacer(Modifier.weight(1f))
            if (selected) {
                Button(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EventsUiTokens.primary),
                ) {
                    Text("Diizinkan")
                }
            } else {
                OutlinedButton(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
                    Text("Izinkan")
                }
            }
        }
    }
}

@Composable
private fun WelcomePreviewCard(
    title: String,
    subtitle: String,
    countdownLabel: String,
    startLabel: String,
    welcomeBgUri: String = "",
    welcomeBgIsVideo: Boolean = false,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 6.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF0F172A)), // slate-900 base for dark theme preview
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            if (welcomeBgUri.isNotBlank()) {
                if (welcomeBgIsVideo) {
                    WelcomeVideoPlayer(filePath = welcomeBgUri, modifier = Modifier.fillMaxSize())
                } else {
                    AsyncImage(
                        model = File(welcomeBgUri),
                        contentDescription = "Welcome Background Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    EventsUiTokens.purple.copy(alpha = 0.26f),
                                    EventsUiTokens.pink.copy(alpha = 0.20f),
                                    Color.White,
                                ),
                            ),
                        )
                )
            }

            // Dark overlay for preview text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            // Content container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EventMetaChip(
                        label = countdownLabel,
                        accent = EventsUiTokens.primary,
                    )
                    EventMetaChip(
                        label = "Welcome Active",
                        accent = EventsUiTokens.success,
                    )
                }
                Column(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (welcomeBgUri.isNotBlank()) Color.White else EventsUiTokens.ink,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (welcomeBgUri.isNotBlank()) Color.White.copy(alpha = 0.85f) else EventsUiTokens.inkSoft,
                    )
                }

                // Small pill "Mulai" button mockup
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                ) {
                    Text(
                        text = startLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EventsUiTokens.ink,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventsHeroCard(title: String, subtitle: String, badge: String, badgeAccent: Color) {
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
                .border(1.dp, EventsUiTokens.border, RoundedCornerShape(28.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = EventsUiTokens.ink, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(999.dp), color = badgeAccent.copy(alpha = 0.14f)) {
                    Text(badge, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = badgeAccent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = EventsUiTokens.inkSoft)
        }
    }
}

@Composable
private fun EventSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = EventsUiTokens.glass,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            EventsUiTokens.softSurface,
                        ),
                    ),
                )
                .border(1.dp, EventsUiTokens.border, RoundedCornerShape(24.dp))
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, fontWeight = FontWeight.Bold, color = EventsUiTokens.ink, style = MaterialTheme.typography.titleMedium)
                content()
            },
        )
    }
}

@Composable
private fun EventInfoRow(first: Pair<String, String>, second: Pair<String, String>, third: Pair<String, String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        listOf(first, second, third).forEach { item ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 6.dp,
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(item.first, style = MaterialTheme.typography.labelSmall, color = EventsUiTokens.inkSoft)
                    Text(item.second, style = MaterialTheme.typography.bodyMedium, color = EventsUiTokens.ink, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EventMetaChip(label: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.12f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}


@Preview(name = "Setting Event Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun SettingEventTabletPreview() {
    DafydioBoothTheme {
        SettingEventScreen(
            state = eventsPreviewBoothState,
            launchState = eventsPreviewLaunchState,
            actions = BoothActions(),
            launchActions = LaunchActions(),
        )
    }
}

@Preview(name = "Setting Event Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SettingEventMobilePreview() {
    DafydioBoothTheme {
        SettingEventScreen(
            state = eventsPreviewBoothState.copy(step = BoothStep.SettingEvent),
            launchState = eventsPreviewLaunchState,
            actions = BoothActions(),
            launchActions = LaunchActions(),
        )
    }
}

@Preview(name = "Allowed Templates Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun AllowedTemplatesTabletPreview() {
    DafydioBoothTheme {
        AllowedTemplatesScreen(
            state = eventsPreviewBoothState.copy(
                step = BoothStep.SettingAllowedTemplates,
                launchTemplateSearchQuery = "",
            ),
            actions = BoothActions(),
        )
    }
}

@Composable
private fun WelcomeVideoPlayer(filePath: String, modifier: Modifier = Modifier) {
    val file = remember(filePath) { File(filePath) }
    if (!file.exists()) {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = androidx.compose.ui.Alignment.Center
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
                    mp.setVolume(0f, 0f) // Mute preview so it does not make noise
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

private fun copyUriToLocalFile(context: Context, uri: Uri, fileNamePrefix: String): String? {
    return try {
        val extension = when (context.contentResolver.getType(uri)) {
            "video/mp4" -> "mp4"
            "video/mkv" -> "mkv"
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> uri.toString().substringAfterLast('.', "dat")
        }
        val file = File(context.filesDir, "${fileNamePrefix}_${System.currentTimeMillis()}.$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
