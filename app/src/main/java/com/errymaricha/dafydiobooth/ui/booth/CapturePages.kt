package com.errymaricha.dafydiobooth.ui.booth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.errymaricha.dafydiobooth.ui.launch.CaptureFinishedScreen
import com.errymaricha.dafydiobooth.ui.launch.PhotoTemplate

@Composable
fun CapturePreviewPageRedesign(state: BoothUiState, actions: BoothActions) {
    CapturePreviewScreen(state = state, actions = actions)
}

@Composable
fun FinishPageRedesign(state: BoothUiState, actions: BoothActions) {
    val template = PhotoTemplate(
        id = state.selectedTemplateId ?: "template-finish",
        name = state.selectedTemplate ?: "Template Session",
        sizeLabel = state.selectedTemplatePaperSize ?: "-",
        frameCount = state.templateSlotCount.coerceAtLeast(1),
        accent = Color(0xFF5B67FF),
    )
    CaptureFinishedScreen(
        eventName = state.launchEventName.ifBlank { "Photobooth Session" },
        template = template,
        sessionCode = state.session?.sessionCode ?: "SES-PENDING",
        kioskExitCode = state.kioskExitCode,
        onBackToWelcome = actions.newSession,
        onStartNewSession = actions.newSession,
        onExitToDashboard = actions.openDashboard,
        capturedPhotos = state.capturedPhotosBySlot.entries.sortedBy { it.key }.map { it.value },
        onDownloadClick = actions.downloadResult,
        onPrintClick = actions.triggerMockPrint,
        mockPrintStatus = state.mockPrintStatus,
        mockPrintMessage = state.mockPrintMessage,
        printUsePhotoboothStation = state.printUsePhotoboothStation,
        isStationConnected = state.isStationConnected,
        isQuickBooth = state.localOnlySession,
        previewContent = {
            TemplateSurface(state = state)
        }
    )
}

@Composable
fun CapturePreviewScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Preview Capture", state = state, actions = actions) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isTablet = maxWidth >= 900.dp
            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CapturePreviewInfoCard(
                        state = state,
                        modifier = Modifier.weight(0.82f),
                    )
                    CapturePreviewCanvasCard(
                        state = state,
                        modifier = Modifier.weight(1.18f),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    CapturePreviewCanvasCard(
                        state = state,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    CapturePreviewInfoCard(
                        state = state,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = actions.retakePhoto,
                enabled = !state.isLoading,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text("Retake")
            }
            Button(
                onClick = actions.acceptCapturePreview,
                enabled = !state.isLoading,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B67FF)),
            ) {
                Text("Use Photo")
            }
        }
    }
}

@Composable
private fun CapturePreviewCanvasCard(
    state: BoothUiState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF4F7FF), Color.White, Color(0xFFFFF5FA)),
                    ),
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Preview Capture", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFF5B67FF).copy(alpha = 0.12f)) {
                    Text(
                        text = "Slot ${state.nextCaptureIndex}/${state.templateSlotCount}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color(0xFF5B67FF),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(1.dp, Color(0x225B67FF), RoundedCornerShape(22.dp))
                    .padding(10.dp),
            ) {
                CapturedPhotoSurface(state)
            }
        }
    }
}

@Composable
private fun CapturePreviewInfoCard(
    state: BoothUiState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFEFF3FF), Color(0xFFFFF3F9)),
                    ),
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Quick Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Overlay template aktif ditampilkan di sini untuk cek posisi dan area aman sebelum lanjut.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.85f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (
                        !state.selectedTemplatePreviewLocalPath.isNullOrBlank() ||
                        !state.selectedTemplateOverlayLocalPath.isNullOrBlank() ||
                        !state.selectedTemplatePreviewUrl.isNullOrBlank() ||
                        !state.selectedTemplateOverlayUrl.isNullOrBlank()
                    ) {
                        TemplateSurface(state)
                    } else {
                        Text(
                            text = "Overlay template belum tersedia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                text = state.capturedPhotoName ?: "Belum ada nama file",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(state.templateSlotCount.coerceAtMost(4)) { index ->
                    val active = index == (state.nextCaptureIndex - 1).coerceAtLeast(0)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .background(
                                if (active) Color(0xFF5B67FF).copy(alpha = 0.16f) else Color.White.copy(alpha = 0.9f),
                                RoundedCornerShape(16.dp),
                            )
                            .border(
                                1.dp,
                                if (active) Color(0xFF5B67FF) else Color(0x225B67FF),
                                RoundedCornerShape(16.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Frame ${index + 1}", style = MaterialTheme.typography.labelMedium, color = if (active) Color(0xFF5B67FF) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun FinishScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Finish", state = state, actions = actions) {
        TemplateSurface(state)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = actions.downloadResult, modifier = Modifier.weight(1f)) {
                Text("Download")
            }
            OutlinedButton(
                onClick = actions.triggerMockPrint,
                enabled = !state.isLoading && state.mockPrintStatus != MockPrintStatus.Queued,
                modifier = Modifier.weight(1f),
            ) {
                Text("Print")
            }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                Text("Share")
            }
        }
        Text(
            text = when (state.mockPrintStatus) {
                MockPrintStatus.Idle -> "Mock print: idle"
                MockPrintStatus.Queued -> "Mock print: queued"
                MockPrintStatus.Sent -> "Mock print: sent"
                MockPrintStatus.Failed -> "Mock print: failed"
            },
        )
        state.mockPrintMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        Text(
            text = if (!state.localOnlySession && state.printUsePhotoboothStation && state.isStationConnected) {
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
