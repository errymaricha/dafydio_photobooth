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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import java.io.File
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
    ScreenFrame(title = "Preview Capture", state = state, actions = actions, scrollable = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isTablet = maxWidth >= 900.dp
                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            CapturePreviewCanvasCard(
                                state = state,
                                modifier = Modifier.weight(1f),
                            )
                            CapturePreviewInfoCard(
                                state = state,
                                modifier = Modifier.weight(1f),
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
            }
            
            // Floating glassmorphic bottom bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = actions.retakePhoto,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5B67FF)),
                        border = BorderStroke(1.5.dp, Color(0xFF5B67FF).copy(alpha = 0.4f))
                    ) {
                        Text("Retake", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = actions.acceptCapturePreview,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B67FF)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Use Photo", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
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
                        colors = listOf(Color(0xFFF4F7FF), Color.White),
                    ),
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Hasil Capture",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFF5B67FF).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Slot ${state.nextCaptureIndex}/${state.templateSlotCount}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color(0xFF5B67FF),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            
            // Polaroid-style print frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(22.dp))
                    .border(1.dp, Color(0x155B67FF), RoundedCornerShape(22.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(bottom = 6.dp)
                ) {
                    val path = state.capturedPhotoPath
                    if (path != null) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Captured Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x105B67FF), RoundedCornerShape(12.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Foto tidak ditemukan", color = Color.Gray)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Footer details inside Polaroid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.capturedPhotoName ?: "Belum ada nama file",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "DAFYDIO BOOTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF5B67FF).copy(alpha = 0.6f),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Canvas Preview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2144)
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFF5B67FF).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = state.selectedTemplatePaperSize ?: "-",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5B67FF),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Text(
                text = "Desain akhir cetak dengan template overlay aktif. Pastikan posisi foto pas sebelum dilanjutkan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            
            if (
                !state.selectedTemplatePreviewLocalPath.isNullOrBlank() ||
                !state.selectedTemplateOverlayLocalPath.isNullOrBlank() ||
                !state.selectedTemplatePreviewUrl.isNullOrBlank() ||
                !state.selectedTemplateOverlayUrl.isNullOrBlank()
            ) {
                TemplateSurface(state)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Overlay template belum tersedia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Dynamic Interactive Slot Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(state.templateSlotCount.coerceAtMost(4)) { index ->
                    val active = index == (state.nextCaptureIndex - 1).coerceAtLeast(0)
                    val photoPath = if (active) {
                        state.capturedPhotoPath
                    } else {
                        state.capturedPhotosBySlot[index + 1]
                    }
                    
                    if (photoPath != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = if (active) 2.dp else 1.dp,
                                    color = if (active) Color(0xFF5B67FF) else Color(0x335B67FF),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = File(photoPath),
                                contentDescription = "Slot ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Dim overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )
                            // Label number
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            // Checkmark badge for completed accepted slots
                            if (index < state.nextCaptureIndex - 1) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .background(Color(0xFF10B981), RoundedCornerShape(999.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            // Active status badge
                            if (active) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 4.dp)
                                        .background(Color(0xFF5B67FF), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PREVIEW",
                                        color = Color.White,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty slot placeholder
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color(0x155B67FF),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold
                            )
                        }
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
