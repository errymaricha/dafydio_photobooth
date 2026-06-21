package com.errymaricha.dafydiobooth.ui.booth

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.errymaricha.dafydiobooth.ui.launch.LaunchUiState

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

        if (state.cameraSource == CameraSource.AndroidDefault) {
            AndroidCameraCapture(
                state = state,
                onCaptured = actions.capturePhotoFile,
                onCameraAvailabilityChanged = actions.updateDetectedCameras,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CameraSurface(state)
        }

        IconButton(
            onClick = actions.startNowPhoto,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 12.dp, top = 8.dp)
                .size(42.dp)
                .background(color = Color.Black.copy(alpha = 0.42f), shape = CircleShape)
                .align(Alignment.TopStart),
        ) {
            Text(
                text = "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }

        Surface(
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.44f),
        ) {
            Text(
                text = "Capture slot: ${state.capturedPhotosBySlot.size}/${state.templateSlotCount}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }

        if (state.hasBackCamera && state.hasFrontCamera && state.cameraSource == CameraSource.AndroidDefault) {
            OutlinedButton(
                onClick = { actions.setUseFrontCamera(!state.useFrontCamera) },
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(if (state.useFrontCamera) "Use Back Camera" else "Use Front Camera")
            }
        }

        if (state.cameraSource == CameraSource.ExternalCanon) {
            Button(
                onClick = actions.capturePhoto,
                enabled = state.externalCameraStatus == ExternalCameraStatus.Connected && !state.isLoading,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isTablet) 136.dp else 116.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B67FF)),
            ) {
                Text("Capture Canon")
            }

            ExternalCameraRecoveryPanel(
                state = state,
                actions = actions,
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp, start = 12.dp, end = 12.dp),
            )

            val countdownText = state.eventStatusMessage
                ?.takeIf { it.startsWith("Capture Canon dalam ") }
            if (!countdownText.isNullOrBlank()) {
                Text(
                    text = countdownText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = if (isTablet) 20.dp else 12.dp, vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Black.copy(alpha = 0.56f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.04f), Color.Transparent),
                        ),
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Template Frames",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.selectedTemplateSlots.sortedBy { it.slotIndex }.forEach { slot ->
                        val captured = state.capturedPhotosBySlot.containsKey(slot.sourceSlotIndex)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (captured) Color(0xFF1B5E20) else Color(0xFF2A2A2A),
                            ),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                text = "Slot ${slot.slotIndex} (S${slot.sourceSlotIndex})",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
        ExternalCameraStatus.Connected -> "Canon connected. Siap capture."
        ExternalCameraStatus.Pairing -> "Pairing USB berjalan. Izinkan popup permission lalu tunggu koneksi otomatis."
        ExternalCameraStatus.Scanning -> "Sedang scan kamera Canon. Pastikan kabel USB/OTG terpasang rapat."
        ExternalCameraStatus.Disconnected -> "Canon belum terhubung. Jalankan Scan -> Pairing -> Connect."
    }
    val error = state.errorMessage?.takeIf {
        it.contains("Canon", ignoreCase = true) || it.contains("USB", ignoreCase = true)
    }
    val shouldShow = state.externalCameraStatus != ExternalCameraStatus.Connected || !error.isNullOrBlank()
    if (!shouldShow) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.72f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "External Camera Recovery",
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = statusMessage ?: connectionHint,
                color = Color.White,
            )
            Text(
                text = "Camera: ${state.externalCameraType}",
                color = Color.White,
            )
            if (!error.isNullOrBlank()) {
                Text(
                    text = "Error: $error",
                    color = Color(0xFFFFCDD2),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = actions.scanExternalCamera, enabled = !state.isLoading) { Text("Scan") }
                OutlinedButton(
                    onClick = actions.pairExternalCamera,
                    enabled = !state.isLoading && state.externalCameraStatus != ExternalCameraStatus.Scanning,
                ) { Text("Pairing") }
                OutlinedButton(
                    onClick = actions.markExternalCameraConnected,
                    enabled = !state.isLoading && state.externalCameraStatus != ExternalCameraStatus.Scanning,
                ) { Text("Connect") }
            }
        }
    }
}
