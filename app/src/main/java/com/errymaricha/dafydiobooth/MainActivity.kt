package com.errymaricha.dafydiobooth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.errymaricha.dafydiobooth.data.api.ApiClient
import com.errymaricha.dafydiobooth.data.local.DeviceConfigStore
import com.errymaricha.dafydiobooth.data.local.TemplateAssetStore
import com.errymaricha.dafydiobooth.data.local.TemplateSqliteStore
import com.errymaricha.dafydiobooth.data.repository.ApiPhotoboothRepository
import com.errymaricha.dafydiobooth.data.repository.LaunchRepositoryImpl
import com.errymaricha.dafydiobooth.data.session.SessionStateManager
import com.errymaricha.dafydiobooth.data.station.StationConnectionChecker
import com.errymaricha.dafydiobooth.domain.usecase.CalculateFinalAmountUseCase
import com.errymaricha.dafydiobooth.domain.usecase.CheckLaunchPaymentUseCase
import com.errymaricha.dafydiobooth.domain.usecase.CheckPaymentUseCase
import com.errymaricha.dafydiobooth.domain.usecase.CompleteSessionUseCase
import com.errymaricha.dafydiobooth.domain.usecase.ConfirmPaymentUseCase
import com.errymaricha.dafydiobooth.domain.usecase.CreateEditJobUseCase
import com.errymaricha.dafydiobooth.domain.usecase.CreateSessionUseCase
import com.errymaricha.dafydiobooth.domain.usecase.OpenManualSessionUseCase
import com.errymaricha.dafydiobooth.domain.usecase.PhotoboothUseCases
import com.errymaricha.dafydiobooth.domain.usecase.PrepareLaunchUseCase
import com.errymaricha.dafydiobooth.domain.usecase.RequestPaymentQuoteUseCase
import com.errymaricha.dafydiobooth.domain.usecase.RequestLaunchPaymentQuoteUseCase
import com.errymaricha.dafydiobooth.domain.usecase.RefreshTemplatesUseCase
import com.errymaricha.dafydiobooth.domain.usecase.RenderSessionUseCase
import com.errymaricha.dafydiobooth.domain.usecase.UploadRenderedOutputUseCase
import com.errymaricha.dafydiobooth.domain.usecase.UploadCaptureUseCase
import com.errymaricha.dafydiobooth.domain.usecase.VerifyLaunchVoucherUseCase
import com.errymaricha.dafydiobooth.domain.usecase.VerifyVoucherUseCase
import com.errymaricha.dafydiobooth.ui.booth.BoothApp
import com.errymaricha.dafydiobooth.ui.booth.TemplateRenderedOutputComposer
import com.errymaricha.dafydiobooth.ui.booth.BoothViewModel
import com.errymaricha.dafydiobooth.ui.booth.BoothViewModelFactory
import com.errymaricha.dafydiobooth.ui.launch.LaunchViewModel
import com.errymaricha.dafydiobooth.ui.launch.LaunchViewModelFactory
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val configStore = DeviceConfigStore(applicationContext)
        val stationBootstrap = (application as DafydioApplication).stationBootstrap
        val templateSqliteStore = TemplateSqliteStore(applicationContext)
        val templateAssetStore = TemplateAssetStore(applicationContext)
        val sessionStateManager = SessionStateManager()
        lifecycleScope.launch {
            configStore.config.collect { config ->
                stationBootstrap.setStationBaseUrl(config.stationIp)
                sessionStateManager.updateConnection(
                    stationIp = config.stationIp,
                    deviceId = config.deviceId,
                    apiKey = config.token,
                    authToken = config.authToken,
                )
            }
        }
        stationBootstrap.startHeartbeat()
        val api = ApiClient.create(
            stationBaseUrlProvider = { sessionStateManager.snapshot().stationIp },
            tokenProvider = { sessionStateManager.snapshot().authToken.ifBlank { sessionStateManager.snapshot().apiKey } },
            deviceIdProvider = { sessionStateManager.snapshot().deviceId },
        )
        val repository = ApiPhotoboothRepository(api)
        val launchRepository = LaunchRepositoryImpl(api)
        val useCases = PhotoboothUseCases(
            verifyVoucher = VerifyVoucherUseCase(repository),
            requestPaymentQuote = RequestPaymentQuoteUseCase(repository),
            createSession = CreateSessionUseCase(repository),
            checkPayment = CheckPaymentUseCase(repository),
            confirmPayment = ConfirmPaymentUseCase(repository),
            uploadCapture = UploadCaptureUseCase(repository),
            refreshTemplates = RefreshTemplatesUseCase(repository),
            completeSession = CompleteSessionUseCase(repository),
            renderSession = RenderSessionUseCase(repository),
            createEditJob = CreateEditJobUseCase(repository),
            uploadRenderedOutput = UploadRenderedOutputUseCase(repository),
        )
        val factory = BoothViewModelFactory(
            useCases = useCases,
            configStore = configStore,
            templateSqliteStore = templateSqliteStore,
            templateAssetStore = templateAssetStore,
            stationConnectionChecker = StationConnectionChecker(),
            sessionStateManager = sessionStateManager,
            renderedOutputComposer = TemplateRenderedOutputComposer(applicationContext),
            heartbeatStatusStore = stationBootstrap.heartbeatStatusStore,
            stationTokenStore = stationBootstrap.stationTokenStore,
            stationDeviceRepository = stationBootstrap.deviceRepository,
        )
        val launchFactory = LaunchViewModelFactory(
            prepareLaunch = PrepareLaunchUseCase(launchRepository),
            openManualSession = OpenManualSessionUseCase(launchRepository),
            verifyLaunchVoucher = VerifyLaunchVoucherUseCase(launchRepository),
            requestLaunchPaymentQuote = RequestLaunchPaymentQuoteUseCase(launchRepository),
            checkLaunchPayment = CheckLaunchPaymentUseCase(launchRepository),
            calculateFinalAmount = CalculateFinalAmountUseCase(),
            sessionStateManager = sessionStateManager,
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
