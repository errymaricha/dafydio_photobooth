package com.errymaricha.dafydiobooth.ui.booth.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.BoothStep
import com.errymaricha.dafydiobooth.ui.booth.CapturePreviewScreen
import com.errymaricha.dafydiobooth.ui.booth.FinishScreen
import com.errymaricha.dafydiobooth.ui.booth.TemplatePickerScreen
import com.errymaricha.dafydiobooth.ui.booth.TemplatePreviewScreen
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme

@Preview(name = "Template Preview Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun TemplatePreviewMobile() {
    DafydioBoothTheme { TemplatePreviewScreen(state = PreviewStateProvider.templateBase, actions = BoothActions()) }
}

@Preview(name = "Template Preview Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun TemplatePreviewTablet() {
    DafydioBoothTheme { TemplatePreviewScreen(state = PreviewStateProvider.templateBase, actions = BoothActions()) }
}

@Preview(name = "Template Picker Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun TemplatePickerTablet() {
    DafydioBoothTheme {
        TemplatePickerScreen(
            state = PreviewStateProvider.templateBase.copy(step = BoothStep.TemplatePicker),
            launchState = PreviewStateProvider.launchUiBase,
            actions = BoothActions(),
        )
    }
}

@Preview(name = "Capture Preview Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun CapturePreviewTablet() {
    DafydioBoothTheme { CapturePreviewScreen(state = PreviewStateProvider.templateBase.copy(step = BoothStep.CapturePreview), actions = BoothActions()) }
}

@Preview(name = "Finish Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun FinishTablet() {
    DafydioBoothTheme { FinishScreen(state = PreviewStateProvider.templateBase.copy(step = BoothStep.Finish), actions = BoothActions()) }
}
