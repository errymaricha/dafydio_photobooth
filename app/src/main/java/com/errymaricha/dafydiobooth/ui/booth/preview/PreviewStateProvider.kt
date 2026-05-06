package com.errymaricha.dafydiobooth.ui.booth.preview

import com.errymaricha.dafydiobooth.ui.booth.BoothStep
import com.errymaricha.dafydiobooth.ui.booth.BoothUiState
import com.errymaricha.dafydiobooth.ui.booth.CameraSource
import com.errymaricha.dafydiobooth.ui.booth.ExternalCameraStatus
import com.errymaricha.dafydiobooth.ui.launch.LaunchUiState

object PreviewStateProvider {
    val dashboardDisconnected = BoothUiState(
        step = BoothStep.Dashboard,
        stationIp = "http://10.10.116.4:8000/",
        deviceId = "PB-DEVICE-01",
        isStationConnected = false,
    )

    val dashboardConnected = dashboardDisconnected.copy(
        isStationConnected = true,
        selectedTemplate = "Classic 2 Slot",
        capturedPhotoName = "capture-preview.jpg",
        printUsePhotoboothStation = true,
    )

    val cameraBase = dashboardConnected.copy(
        step = BoothStep.Camera,
        templateSlotCount = 2,
        nextCaptureIndex = 1,
        countdownSeconds = 3,
    )

    val launchBase = dashboardConnected.copy(step = BoothStep.LaunchEvent)
    val templateBase = dashboardConnected.copy(step = BoothStep.TemplatePreview, templateSlotCount = 2, nextCaptureIndex = 1)
    val settingsBase = dashboardConnected.copy(
        step = BoothStep.Settings,
        cameraSource = CameraSource.ExternalCanon,
        externalCameraStatus = ExternalCameraStatus.Connected,
        mirrorLiveView = true,
        countdownSeconds = 5,
    )

    val launchUiBase = LaunchUiState()
}
