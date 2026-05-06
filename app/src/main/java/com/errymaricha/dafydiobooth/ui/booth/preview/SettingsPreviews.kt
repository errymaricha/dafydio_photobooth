package com.errymaricha.dafydiobooth.ui.booth.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.SettingsScreen
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme

@Preview(name = "Settings Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SettingsMobilePreview() {
    DafydioBoothTheme { SettingsScreen(state = PreviewStateProvider.settingsBase, actions = BoothActions()) }
}

@Preview(name = "Settings Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun SettingsTabletPreview() {
    DafydioBoothTheme { SettingsScreen(state = PreviewStateProvider.settingsBase, actions = BoothActions()) }
}
