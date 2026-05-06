package com.errymaricha.dafydiobooth.ui.booth.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.DashboardActions
import com.errymaricha.dafydiobooth.ui.booth.DashboardScreen
import com.errymaricha.dafydiobooth.ui.booth.DashboardStatusPanel
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme

@Preview(name = "Dashboard Mobile - Disconnected", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun DashboardMobileDisconnectedPreview() {
    DafydioBoothTheme { DashboardScreen(state = PreviewStateProvider.dashboardDisconnected, actions = BoothActions()) }
}

@Preview(name = "Dashboard Mobile - Connected", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun DashboardMobileConnectedPreview() {
    DafydioBoothTheme { DashboardScreen(state = PreviewStateProvider.dashboardConnected, actions = BoothActions()) }
}

@Preview(name = "Dashboard Tablet - Disconnected", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun DashboardTabletDisconnectedPreview() {
    DafydioBoothTheme { DashboardScreen(state = PreviewStateProvider.dashboardDisconnected, actions = BoothActions()) }
}

@Preview(name = "Dashboard Tablet - Connected", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun DashboardTabletConnectedPreview() {
    DafydioBoothTheme { DashboardScreen(state = PreviewStateProvider.dashboardConnected, actions = BoothActions()) }
}

@Preview(name = "Dashboard Tablet - Loading Error", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun DashboardTabletLoadingErrorPreview() {
    DafydioBoothTheme {
        DashboardScreen(
            state = PreviewStateProvider.dashboardDisconnected.copy(isLoading = true, errorMessage = "Gagal connect station. Silakan Retry."),
            actions = BoothActions(),
        )
    }
}

@Preview(name = "Dashboard Actions - Connected", widthDp = 420, heightDp = 620, showBackground = true)
@Composable
private fun DashboardActionsConnectedPreview() {
    DafydioBoothTheme { DashboardActions(state = PreviewStateProvider.dashboardConnected, actions = BoothActions()) }
}

@Preview(name = "Dashboard Status - Connected", widthDp = 420, heightDp = 320, showBackground = true)
@Composable
private fun DashboardStatusConnectedPreview() {
    DafydioBoothTheme { DashboardStatusPanel(state = PreviewStateProvider.dashboardConnected) }
}
