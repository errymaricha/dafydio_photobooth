package com.errymaricha.dafydiobooth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.errymaricha.dafydiobooth.data.api.ApiClient
import com.errymaricha.dafydiobooth.data.local.DeviceConfigStore
import com.errymaricha.dafydiobooth.data.repository.ApiPhotoboothRepository
import com.errymaricha.dafydiobooth.data.repository.LaunchRepositoryImpl
import com.errymaricha.dafydiobooth.data.station.StationConnectionChecker
import com.errymaricha.dafydiobooth.domain.usecase.CalculateFinalAmountUseCase
import com.errymaricha.dafydiobooth.domain.usecase.CheckPaymentUseCase
import com.errymaricha.dafydiobooth.domain.usecase.ConfirmPaymentUseCase
import com.errymaricha.dafydiobooth.domain.usecase.CreateSessionUseCase
import com.errymaricha.dafydiobooth.domain.usecase.OpenManualSessionUseCase
import com.errymaricha.dafydiobooth.domain.usecase.PhotoboothUseCases
import com.errymaricha.dafydiobooth.domain.usecase.PrepareLaunchUseCase
import com.errymaricha.dafydiobooth.domain.usecase.RequestPaymentQuoteUseCase
import com.errymaricha.dafydiobooth.domain.usecase.VerifyVoucherUseCase
import com.errymaricha.dafydiobooth.ui.booth.BoothApp
import com.errymaricha.dafydiobooth.ui.booth.BoothViewModel
import com.errymaricha.dafydiobooth.ui.booth.BoothViewModelFactory
import com.errymaricha.dafydiobooth.ui.launch.LaunchViewModel
import com.errymaricha.dafydiobooth.ui.launch.LaunchViewModelFactory
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val configStore = DeviceConfigStore(applicationContext)
        val api = ApiClient.create(
            tokenProvider = {
                val config = configStore.currentConfigBlocking()
                config.authToken.ifBlank { config.token }
            },
            deviceIdProvider = { configStore.currentConfigBlocking().deviceId },
        )
        val repository = ApiPhotoboothRepository(api)
        val launchRepository = LaunchRepositoryImpl(api)
        val useCases = PhotoboothUseCases(
            verifyVoucher = VerifyVoucherUseCase(repository),
            requestPaymentQuote = RequestPaymentQuoteUseCase(repository),
            createSession = CreateSessionUseCase(repository),
            checkPayment = CheckPaymentUseCase(repository),
            confirmPayment = ConfirmPaymentUseCase(repository),
        )
        val factory = BoothViewModelFactory(
            useCases = useCases,
            configStore = configStore,
            stationConnectionChecker = StationConnectionChecker(),
        )
        val launchFactory = LaunchViewModelFactory(
            prepareLaunch = PrepareLaunchUseCase(launchRepository),
            openManualSession = OpenManualSessionUseCase(launchRepository),
            calculateFinalAmount = CalculateFinalAmountUseCase(),
        )

        setContent {
            DafydioBoothTheme {
                val boothViewModel = viewModel<BoothViewModel>(factory = factory)
                val launchViewModel = viewModel<LaunchViewModel>(factory = launchFactory)
                BoothApp(
                    viewModel = boothViewModel,
                    launchViewModel = launchViewModel,
                )
            }
        }
    }
}

@Composable
private fun BoothPreviewPlaceholder() {
    DafydioBoothTheme {}
}

@Preview(showBackground = true)
@Composable
fun BoothPreview() {
    BoothPreviewPlaceholder()
}
