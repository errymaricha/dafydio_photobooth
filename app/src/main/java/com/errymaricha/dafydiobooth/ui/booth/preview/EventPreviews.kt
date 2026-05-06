package com.errymaricha.dafydiobooth.ui.booth.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.BoothStep
import com.errymaricha.dafydiobooth.ui.booth.LaunchActions
import com.errymaricha.dafydiobooth.ui.booth.LaunchEventScreen
import com.errymaricha.dafydiobooth.ui.booth.PaymentGateScreen
import com.errymaricha.dafydiobooth.ui.booth.VoucherCheckScreen
import com.errymaricha.dafydiobooth.ui.booth.WaitingApprovalScreen
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme

@Preview(name = "Launch Event Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun LaunchEventMobilePreview() {
    DafydioBoothTheme {
        LaunchEventScreen(
            state = PreviewStateProvider.launchBase,
            launchState = PreviewStateProvider.launchUiBase,
            actions = BoothActions(),
            launchActions = LaunchActions(),
        )
    }
}

@Preview(name = "Launch Event Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun LaunchEventTabletPreview() {
    DafydioBoothTheme {
        LaunchEventScreen(
            state = PreviewStateProvider.launchBase,
            launchState = PreviewStateProvider.launchUiBase,
            actions = BoothActions(),
            launchActions = LaunchActions(),
        )
    }
}

@Preview(name = "Voucher Check Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun VoucherCheckTabletPreview() {
    DafydioBoothTheme {
        VoucherCheckScreen(state = PreviewStateProvider.launchBase.copy(step = BoothStep.VoucherCheck), actions = BoothActions())
    }
}

@Preview(name = "Payment Gate Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun PaymentGateTabletPreview() {
    DafydioBoothTheme {
        PaymentGateScreen(state = PreviewStateProvider.launchBase.copy(step = BoothStep.PaymentGate), actions = BoothActions())
    }
}

@Preview(name = "Waiting Approval Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun WaitingApprovalTabletPreview() {
    DafydioBoothTheme {
        WaitingApprovalScreen(state = PreviewStateProvider.launchBase.copy(step = BoothStep.WaitingApproval), actions = BoothActions())
    }
}
