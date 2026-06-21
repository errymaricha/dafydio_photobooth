package com.errymaricha.dafydiobooth.ui.booth

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import coil3.compose.AsyncImage
import com.errymaricha.dafydiobooth.ui.launch.LaunchUiState
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff

@Composable
fun CameraPageRedesign(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
) {
    CameraScreen(
        state = state,
        launchState = launchState,
        actions = actions,
    )
}

@Composable
fun CameraScreen(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 900.dp
        var isCapturing by remember { mutableStateOf(false) }
        val isCanonCapturing = state.eventStatusMessage?.startsWith("Capture Canon dalam ") == true

        // Preview Area (Android Default or External Canon Liveview)
        if (state.cameraSource == CameraSource.AndroidDefault) {
            AndroidCameraCapture(
                state = state,
                onCaptured = actions.capturePhotoFile,
                onCameraAvailabilityChanged = actions.updateDetectedCameras,
                onCapturingStateChanged = { isCapturing = it },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CameraSurface(state)
        }

        // Floating Header Control Bar (Event Name, Session, and Statuses)
        CameraHeaderBar(
            state = state,
            launchState = launchState,
            actions = actions,
            isTablet = isTablet
        )

        // Shutter Button for External Canon Camera
        if (state.cameraSource == CameraSource.ExternalCanon) {
            AnimatedVisibility(
                visible = !(isCapturing || isCanonCapturing),
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = if (state.isLoading) 0.45f else 0.9f))
                            .border(3.dp, Color.White, CircleShape)
                            .clickable(
                                enabled = state.externalCameraStatus == ExternalCameraStatus.Connected && !state.isLoading
                            ) {
                                actions.capturePhoto()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(if (state.isLoading) Color(0xFFB0B0B0) else Color(0xFFE53935)),
                        )
                    }
                    Text(
                        text = "Tap to capture",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Recovery Panel overlay (in case of disconnection/error)
            ExternalCameraRecoveryPanel(
                state = state,
                actions = actions,
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp),
            )

            // Countdown Overlay
            val countdownText = state.eventStatusMessage
                ?.takeIf { it.startsWith("Capture Canon dalam ") }
            if (!countdownText.isNullOrBlank()) {
                val numberOnly = countdownText.filter { it.isDigit() }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(4.dp, Color(0xFF5B67FF)),
                            modifier = Modifier.size(150.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = numberOnly.ifBlank { "!" },
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Siap-siap Capture Canon...",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bottom Roll for Photo Thumbnails (satisfying progress roll)
        androidx.compose.animation.AnimatedVisibility(
            visible = !(isCapturing || isCanonCapturing),
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            TemplateSlotRoll(state = state, isTablet = isTablet)
        }
    }
}

@Composable
private fun CameraHeaderBar(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
    isTablet: Boolean,
) {
    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.65f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event Name & Session Code
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.launchEventName.ifBlank { "Sesi Foto" },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Sesi: ${launchState.session?.sessionCode ?: state.session?.sessionCode ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Connection & Slot Status Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Connection Badge
                val (connLabel, connIcon, connColor) = if (state.cameraSource == CameraSource.ExternalCanon) {
                    if (state.externalCameraStatus == ExternalCameraStatus.Connected) {
                        Triple("Canon", Icons.Default.Wifi, Color(0xFF4ADE80))
                    } else {
                        Triple("Disconnect", Icons.Default.WifiOff, Color(0xFFFF6B9D))
                    }
                } else {
                    Triple("Android Cam", Icons.Default.Wifi, Color(0xFF5B67FF))
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = connColor.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, connColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = connIcon,
                            contentDescription = connLabel,
                            tint = connColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = connLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = connColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Progress Badge
                val slotCount = state.capturedPhotosBySlot.size
                val totalSlots = state.templateSlotCount
                val progressColor = if (slotCount == totalSlots) Color(0xFF4ADE80) else Color(0xFFFFB74D)

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = progressColor.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, progressColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Foto: $slotCount/$totalSlots",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = progressColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Camera Switch Button (Android default camera only)
                if (state.hasBackCamera && state.hasFrontCamera && state.cameraSource == CameraSource.AndroidDefault) {
                    IconButton(
                        onClick = { actions.setUseFrontCamera(!state.useFrontCamera) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.15f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateSlotRoll(
    state: BoothUiState,
    isTablet: Boolean,
) {
    val orderedSlots = state.selectedTemplateSlots.sortedBy { it.slotIndex }
    val captureSlots = orderedSlots.map { it.sourceSlotIndex }.distinct().sorted()
    val nextCaptureSlot = captureSlots.firstOrNull { !state.capturedPhotosBySlot.containsKey(it) }
        ?: captureSlots.lastOrNull()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isTablet) 24.dp else 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Black.copy(alpha = 0.65f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Alur Pengambilan Foto",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                orderedSlots.forEach { slot ->
                    val photoPath = state.capturedPhotosBySlot[slot.sourceSlotIndex]
                    val captured = photoPath != null
                    val isActive = slot.sourceSlotIndex == nextCaptureSlot && !captured

                    Box(
                        modifier = Modifier
                            .width(85.dp)
                            .height(115.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isActive) Color(0xFF1E2144).copy(alpha = 0.8f) else Color(0x33FFFFFF)
                            )
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                brush = if (isActive) {
                                    Brush.linearGradient(listOf(Color(0xFF5B67FF), Color(0xFF8B5CF6)))
                                } else {
                                    Brush.linearGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.15f)))
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        if (photoPath != null) {
                            AsyncImage(
                                model = File(photoPath),
                                contentDescription = "Slot ${slot.slotIndex}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Captured",
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(16.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Camera",
                                    tint = if (isActive) Color(0xFF5B67FF) else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Slot ${slot.slotIndex}",
                                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }

                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(Color(0xFF5B67FF))
                                    .align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraSurface(state: BoothUiState) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val previewBytes = state.externalPreviewBytes
            if (previewBytes != null) {
                val previewBitmap = remember(previewBytes) {
                    val opts = BitmapFactory.Options().apply {
                        inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
                        inSampleSize = 2
                    }
                    BitmapFactory.decodeByteArray(previewBytes, 0, previewBytes.size, opts)
                }
                DisposableEffect(previewBitmap) {
                    onDispose { previewBitmap?.recycle() }
                }
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "Canon live preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Text(
                        text = "External Canon Connected: decode preview gagal",
                    )
                }
            } else {
                Text(
                    text = if (state.cameraSource == CameraSource.AndroidDefault) {
                        "Android Camera Preview"
                    } else {
                        "External Canon Connected: ${state.externalCameraStatus.name} (menunggu live preview...)"
                    },
                )
            }
        }
    }
}

@Composable
private fun ExternalCameraRecoveryPanel(
    state: BoothUiState,
    actions: BoothActions,
    modifier: Modifier = Modifier,
) {
    val statusMessage = state.eventStatusMessage?.takeIf { it.isNotBlank() }
    val connectionHint = when (state.externalCameraStatus) {
        ExternalCameraStatus.Connected -> "Kamera Canon terhubung. Siap digunakan."
        ExternalCameraStatus.Pairing -> "Menghubungkan USB... Izinkan izin popup pada layar."
        ExternalCameraStatus.Scanning -> "Mencari kamera Canon. Pastikan kabel OTG terpasang erat."
        ExternalCameraStatus.Disconnected -> "Kamera Canon belum terhubung."
    }
    val error = state.errorMessage?.takeIf {
        it.contains("Canon", ignoreCase = true) || it.contains("USB", ignoreCase = true)
    }
    val shouldShow = state.externalCameraStatus != ExternalCameraStatus.Connected || !error.isNullOrBlank()
    if (!shouldShow) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.75f),
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFFFF6B9D).copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "External Camera Recovery",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFFF6B9D).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = state.externalCameraStatus.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6B9D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = statusMessage ?: connectionHint,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium
            )
            if (state.externalCameraType.isNotBlank()) {
                Text(
                    text = "Tipe Kamera: ${state.externalCameraType}",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!error.isNullOrBlank()) {
                Text(
                    text = "Error: $error",
                    color = Color(0xFFFF8A80),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = actions.scanExternalCamera,
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan", modifier = Modifier.size(14.dp))
                        Text("Scan", fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = actions.pairExternalCamera,
                    enabled = !state.isLoading && state.externalCameraStatus != ExternalCameraStatus.Scanning,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, contentDescription = "Pairing", modifier = Modifier.size(14.dp))
                        Text("Pairing", fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = actions.markExternalCameraConnected,
                    enabled = !state.isLoading && state.externalCameraStatus != ExternalCameraStatus.Scanning,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B67FF)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Connect", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
