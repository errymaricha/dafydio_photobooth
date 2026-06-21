package com.errymaricha.dafydiobooth.ui.booth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaActionSound
import android.media.ToneGenerator
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.AspectRatio
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun AndroidCameraCapture(
    state: BoothUiState,
    onCaptured: (String) -> Unit,
    onCameraAvailabilityChanged: (Boolean, Boolean) -> Unit = { _, _ -> },
    onCapturingStateChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val previewAspectRatio = remember(state.selectedTemplateCanvasWidth, state.selectedTemplateCanvasHeight) {
        val width = state.selectedTemplateCanvasWidth
        val height = state.selectedTemplateCanvasHeight
        if (width > 0 && height > 0) width.toFloat() / height.toFloat() else 4f / 3f
    }
    val cameraAspectRatio = remember(previewAspectRatio) {
        if (previewAspectRatio > 1.55f) AspectRatio.RATIO_16_9 else AspectRatio.RATIO_4_3
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setTargetAspectRatio(cameraAspectRatio)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val countdownTone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }
    val shutterClick = remember {
        MediaActionSound().apply {
            load(MediaActionSound.SHUTTER_CLICK)
        }
    }
    var hasCameraPermission by remember {
        mutableStateOf(context.hasCameraPermission())
    }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var captureError by remember { mutableStateOf<String?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf<Int?>(null) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    var focusIndicator by remember { mutableStateOf<Offset?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            captureError = "Permission kamera dibutuhkan untuk capture."
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(hasCameraPermission, state.useBackCamera, state.useFrontCamera) {
        if (!hasCameraPermission) return@LaunchedEffect
        val provider = context.getCameraProvider()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        runCatching {
            val hasBack = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            val hasFront = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            onCameraAvailabilityChanged(hasBack, hasFront)
            val selector = when {
                state.useFrontCamera && hasFront -> CameraSelector.DEFAULT_FRONT_CAMERA
                state.useBackCamera && hasBack -> CameraSelector.DEFAULT_BACK_CAMERA
                hasBack -> CameraSelector.DEFAULT_BACK_CAMERA
                hasFront -> CameraSelector.DEFAULT_FRONT_CAMERA
                else -> CameraSelector.DEFAULT_BACK_CAMERA
            }
            provider.unbindAll()
            val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            cameraControl = camera.cameraControl
            cameraProvider = provider
            captureError = null
        }.onFailure { error ->
            captureError = error.message ?: "Camera preview gagal dibuka."
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            countdownTone.release()
            shutterClick.release()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(cameraControl, previewView) {
                            detectTapGestures { tapOffset ->
                                focusIndicator = tapOffset
                                cameraControl?.let { control ->
                                    val point = previewView.meteringPointFactory.createPoint(tapOffset.x, tapOffset.y)
                                    val action = FocusMeteringAction.Builder(point)
                                        .setAutoCancelDuration(2, TimeUnit.SECONDS)
                                        .build()
                                    control.startFocusAndMetering(action)
                                }
                                scope.launch {
                                    delay(800)
                                    focusIndicator = null
                                }
                            }
                        },
                )
                GridOverlay(state = state)
                focusIndicator?.let { indicator ->
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White,
                            radius = 34f,
                            center = indicator,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f),
                        )
                    }
                }
                countdown?.let { remaining ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = remaining.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                color = Color.White,
                            )
                            Text("Get ready...", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = !isCapturing,
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
                            .background(Color.White.copy(alpha = 0.9f))
                            .border(3.dp, Color.White, CircleShape)
                            .clickable(enabled = !isCapturing) {
                                scope.launch {
                                    isCapturing = true
                                    onCapturingStateChanged(true)
                                    captureError = null
                                    runCountdown(
                                        seconds = state.countdownSeconds,
                                        playAudio = state.countdownAudio,
                                        playTone = { countdownTone.startTone(ToneGenerator.TONE_PROP_BEEP, 160) },
                                    ) { remaining ->
                                        countdown = remaining
                                    }
                                    countdown = null
                                    if (state.shutterSound) {
                                        shutterClick.play(MediaActionSound.SHUTTER_CLICK)
                                    }
                                    imageCapture.captureToFile(
                                        context = context,
                                        onSuccess = { file ->
                                            isCapturing = false
                                            onCapturingStateChanged(false)
                                            onCaptured(file.absolutePath)
                                        },
                                        onError = { message ->
                                            isCapturing = false
                                            onCapturingStateChanged(false)
                                            captureError = message
                                        },
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935)),
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
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Permission kamera belum aktif.")
                OutlinedButton(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Allow Camera")
                }
            }
        }
        captureError?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp, start = 16.dp, end = 16.dp),
            )
        }
    }
}

private suspend fun runCountdown(seconds: Int, playAudio: Boolean, playTone: () -> Unit, onTick: (Int) -> Unit) {
    for (remaining in seconds.coerceAtLeast(0) downTo 1) {
        onTick(remaining)
        if (playAudio) {
            playTone()
        }
        delay(1_000)
    }
}

@Composable
private fun GridOverlay(state: BoothUiState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val lineColor = Color.White.copy(alpha = 0.28f)
        val slotColor = Color(0xFFFF3B30)

        // 3x3 composition grid
        for (i in 1..2) {
            val x = w * i / 3f
            val y = h * i / 3f
            drawLine(color = lineColor, start = Offset(x, 0f), end = Offset(x, h), strokeWidth = 1.5f)
            drawLine(color = lineColor, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1.5f)
        }

        // Safe area for active slot only (next uncaptured slot in template order)
        val canvasWidth = state.selectedTemplateCanvasWidth.takeIf { it > 0 } ?: return@Canvas
        val canvasHeight = state.selectedTemplateCanvasHeight.takeIf { it > 0 } ?: return@Canvas
        val orderedSlots = state.selectedTemplateSlots.sortedBy { it.slotIndex }
        val captureSlots = orderedSlots.map { it.sourceSlotIndex }.distinct().sorted()
        val nextCaptureSlot = captureSlots.firstOrNull { !state.capturedPhotosBySlot.containsKey(it) }
            ?: captureSlots.lastOrNull()
        val activeSlot = orderedSlots.firstOrNull { it.sourceSlotIndex == nextCaptureSlot }
            ?: orderedSlots.lastOrNull()
            ?: return@Canvas

        val slotAspect = (activeSlot.width.toFloat() / activeSlot.height.toFloat()).coerceAtLeast(0.1f)
        val previewAspect = (w / h).coerceAtLeast(0.1f)

        val safeWidth: Float
        val safeHeight: Float
        if (slotAspect > previewAspect) {
            // Wider slot: fit to preview width
            safeWidth = w * 0.9f
            safeHeight = safeWidth / slotAspect
        } else {
            // Taller slot: fit to preview height
            safeHeight = h * 0.9f
            safeWidth = safeHeight * slotAspect
        }

        val left = (w - safeWidth) / 2f
        val top = (h - safeHeight) / 2f
        val right = left + safeWidth
        val bottom = top + safeHeight
        val maskColor = Color.Black.copy(alpha = 0.45f)
        drawRect(maskColor, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(w, top))
        drawRect(maskColor, topLeft = Offset(0f, bottom), size = androidx.compose.ui.geometry.Size(w, h - bottom))
        drawRect(maskColor, topLeft = Offset(0f, top), size = androidx.compose.ui.geometry.Size(left, bottom - top))
        drawRect(maskColor, topLeft = Offset(right, top), size = androidx.compose.ui.geometry.Size(w - right, bottom - top))

        drawLine(slotColor, Offset(left, top), Offset(right, top), 3f)
        drawLine(slotColor, Offset(right, top), Offset(right, bottom), 3f)
        drawLine(slotColor, Offset(right, bottom), Offset(left, bottom), 3f)
        drawLine(slotColor, Offset(left, bottom), Offset(left, top), 3f)
    }
}

private fun ImageCapture.captureToFile(
    context: Context,
    onSuccess: (File) -> Unit,
    onError: (String) -> Unit,
) {
    val outputDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "DafydioBooth")
    outputDir.mkdirs()
    val outputFile = File(outputDir, "capture-${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
    takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onSuccess(outputFile)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception.message ?: "Capture gagal.")
            }
        },
    )
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    val future = ProcessCameraProvider.getInstance(this)
    future.addListener(
        { continuation.resume(future.get()) },
        ContextCompat.getMainExecutor(this),
    )
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
