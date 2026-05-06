package com.errymaricha.dafydiobooth.ui.booth.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.CameraScreen
import com.errymaricha.dafydiobooth.ui.booth.CameraSource
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme

@Preview(name = "Camera Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CameraMobilePreview() {
    DafydioBoothTheme {
        CameraScreen(state = PreviewStateProvider.cameraBase, launchState = PreviewStateProvider.launchUiBase, actions = BoothActions())
    }
}

@Preview(name = "Camera Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun CameraTabletPreview() {
    DafydioBoothTheme {
        CameraScreen(
            state = PreviewStateProvider.cameraBase.copy(cameraSource = CameraSource.ExternalCanon),
            launchState = PreviewStateProvider.launchUiBase,
            actions = BoothActions(),
        )
    }
}
