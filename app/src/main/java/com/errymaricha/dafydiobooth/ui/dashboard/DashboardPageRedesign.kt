@file:JvmName("DashboardRedesignKt")

package com.errymaricha.dafydiobooth.ui.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import coil3.compose.AsyncImage
import com.errymaricha.dafydiobooth.R
import com.errymaricha.dafydiobooth.domain.model.LaunchEvent
import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.BoothStep
import com.errymaricha.dafydiobooth.ui.booth.TemplateListItem
import com.errymaricha.dafydiobooth.ui.booth.BoothUiState
import com.errymaricha.dafydiobooth.ui.booth.LaunchActions
import com.errymaricha.dafydiobooth.ui.booth.CameraSource
import com.errymaricha.dafydiobooth.ui.booth.ExternalCameraStatus
import com.errymaricha.dafydiobooth.ui.booth.MockPrintStatus
import com.errymaricha.dafydiobooth.ui.booth.preview.PreviewStateProvider
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme
import com.errymaricha.dafydiobooth.ui.events.EventsPageRedesign
import java.io.File
import com.errymaricha.dafydiobooth.ui.memory.RecentMomentsPage
import com.errymaricha.dafydiobooth.ui.launch.LaunchEventNavHostDemo
import com.errymaricha.dafydiobooth.ui.launch.LaunchUiState
import com.errymaricha.dafydiobooth.ui.setup.SetupPageRedesign
import com.errymaricha.dafydiobooth.ui.template.ViewAllTemplatesScreen
import kotlin.math.absoluteValue
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private object PhotoboothTokens {
    val background = Color(0xFFF6F8FF)
    val ink = Color(0xFF1C2140)
    val inkSoft = Color(0xFF66708F)
    val primary = Color(0xFF5B67FF)
    val pink = Color(0xFFFF6B9D)
    val purple = Color(0xFF8B5CF6)
    val success = Color(0xFF4ADE80)
    val warning = Color(0xFFFDBA74)
    val sky = Color(0xFF82CFFF)
    val card = Color(0xF7FFFFFF)
    val cardSoft = Color(0xF2F8F9FF)
    val glass = Color(0xBFFFFFFF)
    val glassStrong = Color(0xD9FFFFFF)
    val border = Color(0x225B67FF)
    val line = Color(0x140F172A)
}

private val fredokaFamily = FontFamily.SansSerif
private val nunitoFamily = FontFamily.SansSerif

private val displayStyle = TextStyle(
    fontFamily = fredokaFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    lineHeight = 40.sp,
)

private val headlineStyle = TextStyle(
    fontFamily = fredokaFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 28.sp,
)

private val sectionTitleStyle = TextStyle(
    fontFamily = fredokaFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 22.sp,
)

private val buttonStyle = TextStyle(
    fontFamily = fredokaFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    lineHeight = 18.sp,
)

private val bodyStyle = TextStyle(
    fontFamily = nunitoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

private val labelStyle = TextStyle(
    fontFamily = nunitoFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

private data class DashboardStat(
    val title: String,
    val value: String,
    val badge: String,
    val accent: Color,
    val detail: String,
)

private data class MemoryItem(
    val title: String,
    val time: String,
    val subtitle: String,
    val accent: Color,
)

private enum class DashboardRedesignDestination {
    Dashboard,
    Launch,
    Memory,
    Events,
    Setup,
    Templates,
}

@Composable
fun DashboardRedesignContainer(
    state: BoothUiState,
    actions: BoothActions,
    launchState: LaunchUiState = LaunchUiState(),
    launchActions: LaunchActions = LaunchActions(),
) {
    var destination by remember { mutableStateOf(DashboardRedesignDestination.Dashboard) }

    when (destination) {
        DashboardRedesignDestination.Dashboard -> DashboardApp(
            currentDestination = destination,
            state = state,
            launchState = launchState,
            actions = actions,
            onOpenDashboard = { destination = DashboardRedesignDestination.Dashboard },
            onOpenLaunch = { destination = DashboardRedesignDestination.Launch },
            onQuickBooth = actions.startNowPhoto,
            onSelectEvent = actions.setLaunchSelectedEventId,
            onOpenMemory = { destination = DashboardRedesignDestination.Memory },
            onOpenEvents = { destination = DashboardRedesignDestination.Events },
            onOpenSetup = { destination = DashboardRedesignDestination.Setup },
            onViewAllTemplates = { destination = DashboardRedesignDestination.Templates },
        )

        DashboardRedesignDestination.Launch -> LaunchEventNavHostDemo(
            modifier = Modifier.fillMaxSize(),
            kioskExitCode = state.kioskExitCode,
            welcomeBgUri = state.welcomeBgUri,
            welcomeBgIsVideo = state.welcomeBgIsVideo,
        )

        DashboardRedesignDestination.Memory -> Scaffold(
            containerColor = PhotoboothTokens.background,
            bottomBar = {
                FloatingDock(
                    currentDestination = destination,
                    onOpenDashboard = { destination = DashboardRedesignDestination.Dashboard },
                    onOpenMemory = { destination = DashboardRedesignDestination.Memory },
                    onOpenEvents = { destination = DashboardRedesignDestination.Events },
                    onOpenSetup = { destination = DashboardRedesignDestination.Setup },
                )
            },
        ) { padding ->
            RecentMomentsPage(modifier = Modifier.padding(padding))
        }

        DashboardRedesignDestination.Events -> Scaffold(
            containerColor = PhotoboothTokens.background,
            bottomBar = {
                FloatingDock(
                    currentDestination = destination,
                    onOpenDashboard = { destination = DashboardRedesignDestination.Dashboard },
                    onOpenMemory = { destination = DashboardRedesignDestination.Memory },
                    onOpenEvents = { destination = DashboardRedesignDestination.Events },
                    onOpenSetup = { destination = DashboardRedesignDestination.Setup },
                )
            },
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                EventsPageRedesign(
                    state = state.copy(step = BoothStep.SettingEvent),
                    launchState = launchState,
                    actions = actions,
                    launchActions = launchActions,
                )
            }
        }

        DashboardRedesignDestination.Setup -> Scaffold(
            containerColor = PhotoboothTokens.background,
            bottomBar = {
                FloatingDock(
                    currentDestination = destination,
                    onOpenDashboard = { destination = DashboardRedesignDestination.Dashboard },
                    onOpenMemory = { destination = DashboardRedesignDestination.Memory },
                    onOpenEvents = { destination = DashboardRedesignDestination.Events },
                    onOpenSetup = { destination = DashboardRedesignDestination.Setup },
                )
            },
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                SetupPageRedesign(
                    state = state.copy(step = BoothStep.Settings),
                    actions = actions,
                )
            }
        }

        DashboardRedesignDestination.Templates -> ViewAllTemplatesScreen(
            state = state.copy(step = BoothStep.TemplatePicker),
            actions = actions.copy(
                openDashboard = { destination = DashboardRedesignDestination.Dashboard },
            ),
        )
    }
}

@Composable
private fun DashboardApp(
    currentDestination: DashboardRedesignDestination = DashboardRedesignDestination.Dashboard,
    state: BoothUiState = BoothUiState(),
    launchState: LaunchUiState = LaunchUiState(),
    actions: BoothActions = BoothActions(),
    onOpenDashboard: () -> Unit = {},
    onOpenLaunch: () -> Unit = {},
    onQuickBooth: () -> Unit = {},
    onSelectEvent: (String) -> Unit = {},
    onOpenMemory: () -> Unit = {},
    onOpenEvents: () -> Unit = {},
    onOpenSetup: () -> Unit = {},
    onViewAllTemplates: () -> Unit = {},
) {
    Scaffold(
        containerColor = PhotoboothTokens.background,
        bottomBar = {
            FloatingDock(
                currentDestination = currentDestination,
                onOpenDashboard = onOpenDashboard,
                onOpenMemory = onOpenMemory,
                onOpenEvents = onOpenEvents,
                onOpenSetup = onOpenSetup,
            )
        },
    ) { padding ->
        DashboardScreen(
            modifier = Modifier.padding(padding),
            state = state,
            launchState = launchState,
            actions = actions,
            onOpenLaunch = onOpenLaunch,
            onQuickBooth = onQuickBooth,
            onSelectEvent = onSelectEvent,
            onViewAllTemplates = onViewAllTemplates,
            onOpenEvents = onOpenEvents,
        )
    }
}

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    state: BoothUiState = BoothUiState(),
    launchState: LaunchUiState = LaunchUiState(),
    actions: BoothActions = BoothActions(),
    onOpenLaunch: () -> Unit = {},
    onQuickBooth: () -> Unit = {},
    onSelectEvent: (String) -> Unit = {},
    onViewAllTemplates: () -> Unit = {},
    onOpenEvents: () -> Unit = {},
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF4F5FF),
                        Color(0xFFEFF4FF),
                        PhotoboothTokens.background,
                        Color(0xFFFFF4FA),
                    ),
                ),
            ),
    ) {
        val isMobile = maxWidth < 900.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = if (isMobile) 16.dp else 28.dp,
                end = if (isMobile) 16.dp else 28.dp,
                top = if (isMobile) 14.dp else 22.dp,
                bottom = if (isMobile) 16.dp else 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(if (isMobile) 16.dp else 20.dp),
        ) {
            item { DashboardHeader(isMobile = isMobile, isOnline = state.isStationReachable) }
            item { StatsSection(isMobile = isMobile, state = state) }
            item { HeroSection(isMobile = isMobile, state = state, launchState = launchState, actions = actions, onOpenLaunch = onOpenLaunch, onQuickBooth = onQuickBooth, onSelectEvent = onSelectEvent, onViewAllTemplates = onViewAllTemplates, onOpenEvents = onOpenEvents) }
            item { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
        }
    }
}

@Composable
private fun DashboardHeader(isMobile: Boolean, isOnline: Boolean) {
    val pulse = rememberPulseScale()
    val indicatorColor = if (isOnline) PhotoboothTokens.success else Color(0xFFE5484D)
    val indicatorLabel = if (isOnline) "Online" else "Offline"
    GlassCard(
        shape = RoundedCornerShape(if (isMobile) 26.dp else 30.dp),
    ) {
        if (isMobile) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.72f),
                        modifier = Modifier.size(46.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.dafydio_logo),
                                contentDescription = "Dafydio Logo",
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("DafydioBooth", style = headlineStyle.copy(fontSize = 19.sp), color = PhotoboothTokens.ink)
                        Text("Fun booth for glowing memories", style = bodyStyle.copy(fontSize = 11.sp), color = PhotoboothTokens.inkSoft)
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.78f),
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(10.dp, CircleShape, ambientColor = PhotoboothTokens.purple.copy(alpha = 0.10f), spotColor = PhotoboothTokens.purple.copy(alpha = 0.08f)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("ER", style = buttonStyle.copy(fontSize = 14.sp), color = PhotoboothTokens.primary)
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.78f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .graphicsLayer {
                                    scaleX = pulse
                                    scaleY = pulse
                                }
                                .background(indicatorColor, CircleShape),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(indicatorLabel, style = labelStyle, color = PhotoboothTokens.ink)
                        Spacer(Modifier.weight(1f))
                        Text("Account", style = bodyStyle.copy(fontSize = 11.sp), color = PhotoboothTokens.inkSoft)
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.72f),
                    modifier = Modifier.size(54.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.dafydio_logo),
                            contentDescription = "Dafydio Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "DafydioBooth",
                        style = headlineStyle.copy(fontSize = 24.sp),
                        color = PhotoboothTokens.ink,
                    )
                    Text(
                        "Fun booth for glowing memories",
                        style = bodyStyle.copy(fontSize = 13.sp),
                        color = PhotoboothTokens.inkSoft,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.78f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .graphicsLayer {
                                    scaleX = pulse
                                    scaleY = pulse
                                }
                                .background(indicatorColor, CircleShape),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(indicatorLabel, style = labelStyle, color = PhotoboothTokens.ink)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.78f),
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(10.dp, CircleShape, ambientColor = PhotoboothTokens.purple.copy(alpha = 0.10f), spotColor = PhotoboothTokens.purple.copy(alpha = 0.08f)),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("ER", style = buttonStyle.copy(fontSize = 15.sp), color = PhotoboothTokens.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
    isMobile: Boolean,
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
    onOpenLaunch: () -> Unit,
    onQuickBooth: () -> Unit,
    onSelectEvent: (String) -> Unit,
    onViewAllTemplates: () -> Unit,
    onOpenEvents: () -> Unit,
) {
    val pulse = rememberPulseScale()
    val activeEvent = launchState.events.firstOrNull { it.eventId == state.launchSelectedEventId }
    val previewGradient = remember(activeEvent?.eventId, activeEvent?.eventCode) {
        eventPreviewGradient(activeEvent)
    }
    if (isMobile) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LivePreviewCard(
                pulse = pulse,
                isMobile = true,
                activeEventName = activeEvent?.eventName ?: "Booth A",
                countdownSeconds = state.countdownSeconds,
                totalSessionsLabel = if (activeEvent != null) "Sesi aktif" else "Belum ada sesi",
                welcomeLabel = if (activeEvent != null) "Welcome ${activeEvent.eventCode}" else "Welcome event aktif",
                previewGradient = previewGradient,
                hasActiveEvent = activeEvent != null,
                stationConnected = state.isStationReachable,
                welcomeBgUri = state.welcomeBgUri,
                welcomeBgIsVideo = state.welcomeBgIsVideo,
                onOpenLaunch = onOpenLaunch,
                onOpenEvents = onOpenEvents,
            )
            StartSessionCard(
                isMobile = true,
                stationConnected = state.isStationReachable,
                templateItems = state.availableTemplateItems,
                onQuickBooth = onQuickBooth,
                onViewAllTemplates = onViewAllTemplates,
            )
                EventDeck(
                    isMobile = true,
                    events = launchState.events,
                    selectedEventId = state.launchSelectedEventId,
                    stationConnected = state.isStationReachable,
                    loading = launchState.loading,
                    message = launchState.message,
                    error = launchState.error,
                    onSelectEvent = onSelectEvent,
                )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1.3f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LivePreviewCard(
                    pulse = pulse,
                    isMobile = false,
                    activeEventName = activeEvent?.eventName ?: "Booth A",
                    countdownSeconds = state.countdownSeconds,
                    totalSessionsLabel = if (activeEvent != null) "Sesi aktif" else "Belum ada sesi",
                    welcomeLabel = if (activeEvent != null) "Welcome ${activeEvent.eventCode}" else "Welcome event aktif",
                    previewGradient = previewGradient,
                    hasActiveEvent = activeEvent != null,
                    stationConnected = state.isStationReachable,
                    welcomeBgUri = state.welcomeBgUri,
                    welcomeBgIsVideo = state.welcomeBgIsVideo,
                    onOpenLaunch = onOpenLaunch,
                    onOpenEvents = onOpenEvents,
                )
                EventDeck(
                    isMobile = false,
                    events = launchState.events,
                    selectedEventId = state.launchSelectedEventId,
                    stationConnected = state.isStationReachable,
                    loading = launchState.loading,
                    message = launchState.message,
                    error = launchState.error,
                    onSelectEvent = onSelectEvent,
                )
            }
            Column(
                modifier = Modifier.weight(0.9f),
            ) {
                StartSessionCard(
                    isMobile = false,
                    stationConnected = state.isStationReachable,
                    templateItems = state.availableTemplateItems,
                    onQuickBooth = onQuickBooth,
                    onViewAllTemplates = onViewAllTemplates,
                )
            }
        }
    }
}

@Composable
private fun WelcomeVideoPlayer(filePath: String, modifier: Modifier = Modifier) {
    val file = remember(filePath) { File(filePath) }
    if (!file.exists()) {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Video looping tidak ditemukan", color = Color.White)
        }
        return
    }
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    mp.setVolume(0f, 0f) // Mute preview so it does not make noise
                    mp.start()
                }
            }
        },
        update = { videoView ->
            try {
                videoView.setVideoPath(filePath)
                videoView.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        modifier = modifier
    )
}

private fun copyUriToLocalFile(context: android.content.Context, uri: Uri, fileNamePrefix: String): String? {
    return try {
        val extension = when (context.contentResolver.getType(uri)) {
            "video/mp4" -> "mp4"
            "video/mkv" -> "mkv"
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> uri.toString().substringAfterLast('.', "dat")
        }
        val file = File(context.filesDir, "${fileNamePrefix}_${System.currentTimeMillis()}.$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
private fun LivePreviewCard(
    pulse: Float,
    isMobile: Boolean,
    activeEventName: String,
    countdownSeconds: Int,
    totalSessionsLabel: String,
    welcomeLabel: String,
    previewGradient: Brush,
    hasActiveEvent: Boolean,
    stationConnected: Boolean,
    welcomeBgUri: String,
    welcomeBgIsVideo: Boolean,
    onOpenLaunch: () -> Unit,
    onOpenEvents: () -> Unit,
) {
    GradientCard(
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xDCEEF1FF), Color(0xD8FFF0F7)),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Live Booth",
                    style = if (isMobile) headlineStyle.copy(fontSize = 22.sp) else headlineStyle,
                    color = PhotoboothTokens.ink,
                )
                Spacer(Modifier.width(8.dp))
                BadgeChip(
                    if (stationConnected) "LIVE" else "OFFLINE",
                    accent = if (stationConnected) PhotoboothTokens.success else PhotoboothTokens.warning,
                    glowing = stationConnected,
                )
                if (hasActiveEvent) {
                    Spacer(Modifier.width(8.dp))
                    BadgeChip("Event aktif", accent = PhotoboothTokens.primary)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    activeEventName,
                    style = if (isMobile) sectionTitleStyle.copy(fontSize = 16.sp) else sectionTitleStyle,
                    color = PhotoboothTokens.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                "Tampilan welcome screen aktif. Bisa berupa gambar background atau video looping lokal dengan satu tombol Mulai.",
                style = if (isMobile) bodyStyle.copy(fontSize = 13.sp, lineHeight = 18.sp) else bodyStyle,
                color = PhotoboothTokens.inkSoft,
            )

            // Preview viewport Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isMobile) 200.dp else 220.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF0F172A)), // slate-900 base for high-end preview look
                contentAlignment = Alignment.Center
            ) {
                if (welcomeBgUri.isNotBlank()) {
                    if (welcomeBgIsVideo) {
                        WelcomeVideoPlayer(filePath = welcomeBgUri, modifier = Modifier.fillMaxSize())
                    } else {
                        AsyncImage(
                            model = File(welcomeBgUri),
                            contentDescription = "Welcome Background Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Default preview background gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(previewGradient)
                    )
                }

                // Semi-translucent dark overlay for professional UI preview feel
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )

                // Event welcome title text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.85f),
                    ) {
                        Text(
                            text = welcomeLabel,
                            style = labelStyle.copy(fontSize = if (isMobile) 11.sp else 12.sp),
                            color = PhotoboothTokens.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Centered start button (MULAI / START) mockup with a premium styling
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = pulse * 0.05f + 0.95f
                                scaleY = pulse * 0.05f + 0.95f
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(PhotoboothTokens.pink, CircleShape)
                            )
                            Text(
                                "MULAI",
                                style = buttonStyle.copy(
                                    fontSize = if (isMobile) 15.sp else 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = PhotoboothTokens.ink
                            )
                        }
                    }

                    Text(
                        text = "Tekan tombol untuk memulai sesi",
                        style = labelStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Normal),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Small badge showing background state in bottom start corner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (welcomeBgUri.isBlank()) "Default Gradient"
                               else if (welcomeBgIsVideo) "Loop Video"
                               else "Gambar BG",
                        style = labelStyle.copy(fontSize = 10.sp),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Launch Event / Settings row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MiniActionButton(
                    label = "Launch Event",
                    accent = PhotoboothTokens.primary,
                    modifier = Modifier.weight(1f),
                    enabled = stationConnected,
                    onClick = onOpenLaunch,
                )
                MiniActionButton(
                    label = "Setting Event",
                    accent = PhotoboothTokens.purple,
                    modifier = Modifier.weight(1f),
                    enabled = stationConnected,
                    onClick = onOpenEvents,
                )
            }
        }
    }
}

@Composable
private fun MiniActionButton(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = if (enabled) modifier.clickable(onClick = onClick) else modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (enabled) accent.copy(alpha = 0.14f) else PhotoboothTokens.inkSoft.copy(alpha = 0.10f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (enabled) accent.copy(alpha = 0.18f) else PhotoboothTokens.inkSoft.copy(alpha = 0.14f),
                    RoundedCornerShape(18.dp),
                )
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                style = buttonStyle.copy(fontSize = 13.sp),
                color = if (enabled) accent else PhotoboothTokens.inkSoft,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun EventDeck(
    isMobile: Boolean,
    events: List<LaunchEvent>,
    selectedEventId: String,
    stationConnected: Boolean,
    loading: Boolean,
    message: String?,
    error: String?,
    onSelectEvent: (String) -> Unit,
) {
    val displayEvents = remember(events) {
        events.take(2)
    }
    GradientCard(
        gradient = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFF3F1FF)),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Event picks", style = sectionTitleStyle, color = PhotoboothTokens.ink)
                EventSyncStatus(
                    stationConnected = stationConnected,
                    loading = loading,
                    hasEvents = events.isNotEmpty(),
                    message = message,
                    error = error,
                )
            }
            if (!stationConnected) {
                EventCard(
                    "Station offline",
                    "Quick Booth saja tersedia sampai station terhubung",
                    PhotoboothTokens.warning,
                    Modifier.fillMaxWidth(),
                )
            } else if (displayEvents.isEmpty()) {
                EventCard("Belum ada event", "Sinkronkan event dari station", PhotoboothTokens.primary, Modifier.fillMaxWidth())
            } else if (isMobile) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    displayEvents.forEachIndexed { index, event ->
                        EventCard(
                            title = event.eventName,
                            subtitle = if (event.eventId == selectedEventId) "Event aktif • ${event.eventCode}" else "Event terbaru • ${event.eventCode}",
                            accent = if (index == 0) PhotoboothTokens.primary else PhotoboothTokens.pink,
                            modifier = Modifier.fillMaxWidth(),
                            selected = event.eventId == selectedEventId,
                            onClick = { onSelectEvent(event.eventId) },
                        )
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    displayEvents.forEachIndexed { index, event ->
                        EventCard(
                            title = event.eventName,
                            subtitle = if (event.eventId == selectedEventId) "Event aktif • ${event.eventCode}" else "Event terbaru • ${event.eventCode}",
                            accent = if (index == 0) PhotoboothTokens.primary else PhotoboothTokens.pink,
                            modifier = Modifier.weight(1f),
                            selected = event.eventId == selectedEventId,
                            onClick = { onSelectEvent(event.eventId) },
                        )
                    }
                    repeat((2 - displayEvents.size).coerceAtLeast(0)) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun EventSyncStatus(
    stationConnected: Boolean,
    loading: Boolean,
    hasEvents: Boolean,
    message: String?,
    error: String?,
) {
    val label: String
    val accent: Color
    when {
        !stationConnected -> {
            label = "Station offline"
            accent = PhotoboothTokens.warning
        }
        loading -> {
            label = "Syncing"
            accent = PhotoboothTokens.primary
        }
        !error.isNullOrBlank() -> {
            label = "Sync failed"
            accent = PhotoboothTokens.warning
        }
        hasEvents -> {
            label = "Events ready"
            accent = PhotoboothTokens.success
        }
        !message.isNullOrBlank() -> {
            label = "Waiting sync"
            accent = PhotoboothTokens.purple
        }
        else -> {
            label = "No events"
            accent = PhotoboothTokens.inkSoft
        }
    }
    BadgeChip(label = label, accent = accent, glowing = loading)
}

@Composable
private fun EventCard(
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
        Surface(
            modifier = modifier.clickable(onClick = onClick),
            shape = RoundedCornerShape(24.dp),
            color = PhotoboothTokens.glassStrong,
            shadowElevation = if (selected) 18.dp else 12.dp,
        ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = if (selected) 0.28f else 0.18f),
                            Color.White,
                        ),
                    ),
                )
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) accent.copy(alpha = 0.42f) else accent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgeChip(if (selected) "Active" else "Hot pick", accent = accent)
                if (selected) {
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(accent, CircleShape),
                    )
                }
            }
            Text(title, style = sectionTitleStyle, color = PhotoboothTokens.ink)
            Text(subtitle, style = bodyStyle, color = PhotoboothTokens.inkSoft)
        }
    }
}

@Composable
private fun StartSessionCard(
    isMobile: Boolean,
    stationConnected: Boolean,
    templateItems: List<TemplateListItem>,
    onQuickBooth: () -> Unit,
    onViewAllTemplates: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GradientCard(
        gradient = Brush.verticalGradient(
            colors = listOf(Color(0xFFEEF3FF), Color(0xFFFFF3F9)),
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Your photobooth is ready for the next memory.",
                style = if (isMobile) sectionTitleStyle.copy(fontSize = 17.sp) else sectionTitleStyle,
                color = PhotoboothTokens.ink,
            )
            Text(
                "Countdown, live preview, dan gallery strip dibuat terasa seperti aplikasi photobooth premium, bukan dashboard admin.",
                style = if (isMobile) bodyStyle.copy(fontSize = 13.sp, lineHeight = 18.sp) else bodyStyle,
                color = PhotoboothTokens.inkSoft,
            )
            if (!stationConnected) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PhotoboothTokens.warning.copy(alpha = 0.14f),
                ) {
                    Text(
                        "Mode offline: app tetap bisa dipakai lewat Quick Booth.",
                        style = bodyStyle.copy(fontSize = if (isMobile) 12.sp else 13.sp),
                        color = PhotoboothTokens.ink,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }
            Button(
                onClick = onQuickBooth,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isMobile) 54.dp else 58.dp)
                    .shadow(14.dp, RoundedCornerShape(24.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(PhotoboothTokens.primary, PhotoboothTokens.pink),
                            ),
                            shape = RoundedCornerShape(24.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Quick Booth",
                        style = buttonStyle.copy(fontSize = if (isMobile) 16.sp else 18.sp),
                        color = Color.White,
                    )
                }
            }
            PhotoCollageCard(
                isMobile = isMobile,
                templateItems = templateItems,
                modifier = Modifier.fillMaxWidth(),
            )
            MiniActionButton(
                label = "View All Templates",
                accent = PhotoboothTokens.primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = onViewAllTemplates,
            )
        }
    }
}

@Composable
private fun PhotoCollageCard(
    isMobile: Boolean,
    templateItems: List<TemplateListItem>,
    modifier: Modifier = Modifier,
) {
    val fallbackTemplates = listOf(
        TemplateListItem("mock-1", "Mirror Pop", "MR-1", "Birthday", "4R Strip", null, false, false, false, 4),
        TemplateListItem("mock-2", "Glow Frame", "GL-2", "Korean Booth", "4R", null, false, false, false, 3),
        TemplateListItem("mock-3", "Soft Flash", "SF-3", "Wedding", "2x6 Strip", null, false, false, false, 4),
        TemplateListItem("mock-4", "After Party", "AP-4", "Party", "4R", null, false, false, false, 2),
        TemplateListItem("mock-5", "K-Strip", "KS-5", "Photobooth", "2x6 Strip", null, false, false, false, 4),
        TemplateListItem("mock-6", "Mono Chic", "MC-6", "Minimal", "4R Strip", null, false, false, false, 3),
    )
    val templates = templateItems.take(6).ifEmpty { fallbackTemplates }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = when {
            !isMobile -> 3
            maxWidth < 360.dp -> 1
            else -> 2
        }
        val compactMobile = isMobile && columns == 1
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Template picks",
                style = if (compactMobile) sectionTitleStyle.copy(fontSize = 16.sp) else sectionTitleStyle,
                color = PhotoboothTokens.ink,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                templates.chunked(columns).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        row.forEach { item ->
                            TemplateMiniCard(
                                template = item,
                                isMobile = isMobile,
                                compact = compactMobile,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(columns - row.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateMiniCard(
    template: TemplateListItem,
    isMobile: Boolean,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val accent = when ((template.category ?: "").lowercase()) {
        "birthday", "party" -> PhotoboothTokens.pink
        "wedding" -> PhotoboothTokens.purple
        "minimal" -> PhotoboothTokens.sky
        else -> PhotoboothTokens.primary
    }
    val thumbnailModel = rememberDashboardThumbnailModel(template.thumbnailUrl)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = PhotoboothTokens.card,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 8.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        when {
                            compact -> 96.dp
                            isMobile -> 88.dp
                            else -> 76.dp
                        },
                    )
                    .clip(RoundedCornerShape(18.dp)),
            ) {
                if (thumbnailModel != null) {
                    AsyncImage(
                        model = thumbnailModel,
                        contentDescription = template.templateName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(accent.copy(alpha = 0.55f), Color.White),
                                ),
                            ),
                    )
                }
            }
            Text(
                template.templateName,
                style = labelStyle.copy(
                    fontSize = when {
                        compact -> 12.sp
                        isMobile -> 13.sp
                        else -> 12.sp
                    },
                ),
                color = PhotoboothTokens.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun rememberDashboardThumbnailModel(thumbnailUrl: String?): Any? =
    remember(thumbnailUrl) {
        val path = thumbnailUrl?.takeIf { it.isNotBlank() } ?: return@remember null
        val file = File(path)
        if (file.isFile) file else path
    }

@Composable
private fun StatsSection(isMobile: Boolean, state: BoothUiState) {
    // 1. Memories Captured (Simulasi berbasis data aktif)
    val memoriesCaptured = "128"
    val memoriesDetail = if (state.availableTemplateItems.isNotEmpty()) {
        "${state.availableTemplateItems.size} template aktif"
    } else {
        "katalog template kosong"
    }

    // 2. Camera Status (Dinamis sesuai koneksi kamera)
    val cameraVal: String
    val cameraBadge: String
    val cameraDetail: String
    val cameraColor: Color
    
    when (state.cameraSource) {
        CameraSource.ExternalCanon -> {
            when (state.externalCameraStatus) {
                ExternalCameraStatus.Connected -> {
                    cameraVal = "DSLR Ready"
                    cameraBadge = "🟢 DSLR"
                    cameraDetail = state.externalCameraType.ifBlank { "Canon DSLR standby" }
                    cameraColor = PhotoboothTokens.success
                }
                ExternalCameraStatus.Scanning, ExternalCameraStatus.Pairing -> {
                    cameraVal = "Connecting"
                    cameraBadge = "🟡 SCANNING"
                    cameraDetail = "mencari/menghubungkan DSLR..."
                    cameraColor = PhotoboothTokens.warning
                }
                else -> {
                    cameraVal = "No DSLR"
                    cameraBadge = "🔴 OFFLINE"
                    cameraDetail = "kamera DSLR terputus"
                    cameraColor = Color(0xFFE5484D)
                }
            }
        }
        CameraSource.AndroidDefault -> {
            cameraVal = "Android Cam"
            cameraBadge = "🟢 ACTIVE"
            cameraDetail = "kamera internal standby"
            cameraColor = PhotoboothTokens.success
        }
    }

    // 3. Printer Status (Dinamis sesuai antrean printer)
    val printVal: String
    val printBadge: String
    val printDetail: String
    val printColor: Color

    when (state.mockPrintStatus) {
        MockPrintStatus.Idle -> {
            printVal = "Ready"
            printBadge = "🟢 IDLE"
            printDetail = "printer standby"
            printColor = PhotoboothTokens.success
        }
        MockPrintStatus.Queued -> {
            printVal = "Queued"
            printBadge = "⏳ PENDING"
            printDetail = state.mockPrintMessage ?: "antrean cetak pending"
            printColor = PhotoboothTokens.warning
        }
        MockPrintStatus.Sent -> {
            printVal = "Printing"
            printBadge = "⚡ PRINTING"
            printDetail = state.mockPrintMessage ?: "mengirim data ke printer"
            printColor = PhotoboothTokens.primary
        }
        MockPrintStatus.Failed -> {
            printVal = "Error"
            printBadge = "🔴 ERROR"
            printDetail = state.mockPrintMessage ?: "gagal mencetak foto"
            printColor = Color(0xFFE5484D)
        }
    }

    // 4. Station / Cloud Sync Status (Dinamis sesuai status koneksi station)
    val syncVal: String
    val syncBadge: String
    val syncDetail: String
    val syncColor: Color

    if (state.isStationReachable) {
        syncVal = "Connected"
        syncBadge = "🟢 ONLINE"
        syncDetail = "sinkronisasi station aktif"
        syncColor = PhotoboothTokens.sky
    } else {
        syncVal = "Offline"
        syncBadge = "🔴 OFFLINE"
        syncDetail = "mode penyimpanan lokal"
        syncColor = Color(0xFFE5484D)
    }

    val stats = remember(memoriesCaptured, memoriesDetail, cameraVal, cameraBadge, cameraDetail, cameraColor, printVal, printBadge, printDetail, printColor, syncVal, syncBadge, syncDetail, syncColor) {
        listOf(
            DashboardStat("Memories Captured", memoriesCaptured, "✨ today", PhotoboothTokens.pink, memoriesDetail),
            DashboardStat("Camera Status", cameraVal, cameraBadge, cameraColor, cameraDetail),
            DashboardStat("Printing Queue", printVal, printBadge, printColor, printDetail),
            DashboardStat("Cloud Sync", syncVal, syncBadge, syncColor, syncDetail),
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        if (isMobile) {
            stats.chunked(2).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Max)
                ) {
                    row.forEach { item ->
                        StatCard(item, Modifier.weight(1f).fillMaxHeight())
                    }
                    if (row.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Max)
            ) {
                stats.forEach { item ->
                    StatCard(item, Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun StatCard(stat: DashboardStat, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = PhotoboothTokens.glassStrong,
        shadowElevation = 14.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(stat.accent.copy(alpha = 0.12f), Color.White),
                    ),
                )
                .border(1.dp, stat.accent.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgeChip(stat.badge, accent = stat.accent)
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(stat.accent, CircleShape)
                        .alpha(0.8f),
                )
            }
            Text(stat.title, style = labelStyle, color = PhotoboothTokens.inkSoft, maxLines = 1)
            Text(stat.value, style = displayStyle, color = PhotoboothTokens.ink)
            Text(stat.detail, style = bodyStyle.copy(fontSize = 13.sp), color = PhotoboothTokens.inkSoft)
        }
    }
}


@Composable
private fun FloatingDock(
    currentDestination: DashboardRedesignDestination = DashboardRedesignDestination.Dashboard,
    onOpenDashboard: () -> Unit = {},
    onOpenMemory: () -> Unit = {},
    onOpenEvents: () -> Unit = {},
    onOpenSetup: () -> Unit = {},
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isMobile = maxWidth < 900.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isMobile) 12.dp else 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = PhotoboothTokens.glass,
                shadowElevation = 22.dp,
                modifier = Modifier
                    .fillMaxWidth(if (isMobile) 1f else 0.78f)
                    .border(1.dp, Color.White.copy(alpha = 0.52f), RoundedCornerShape(28.dp)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isMobile) 8.dp else 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DockItem(
                        label = "Booth",
                        icon = Icons.Default.Home,
                        selected = currentDestination == DashboardRedesignDestination.Dashboard,
                        isMobile = isMobile,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenDashboard
                    )
                    DockItem(
                        label = "Memory",
                        icon = Icons.Default.Star,
                        selected = currentDestination == DashboardRedesignDestination.Memory,
                        isMobile = isMobile,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenMemory
                    )
                    DockItem(
                        label = "Events",
                        icon = Icons.Default.List,
                        selected = currentDestination == DashboardRedesignDestination.Events,
                        isMobile = isMobile,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenEvents
                    )
                    DockItem(
                        label = "Setup",
                        icon = Icons.Default.Settings,
                        selected = currentDestination == DashboardRedesignDestination.Setup,
                        isMobile = isMobile,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSetup
                    )
                }
            }
        }
    }
}

@Composable
private fun DockItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    isMobile: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val background = if (selected) {
        Brush.horizontalGradient(listOf(PhotoboothTokens.primary, PhotoboothTokens.purple))
    } else {
        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    }
    val contentColor = if (selected) Color.White else PhotoboothTokens.inkSoft
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = if (isMobile) 8.dp else 16.dp, vertical = if (isMobile) 10.dp else 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isMobile) 4.dp else 8.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(if (isMobile) 16.dp else 20.dp)
            )
            if (!isMobile || selected) {
                Text(
                    text = label,
                    style = buttonStyle.copy(fontSize = if (isMobile) 12.sp else 14.sp),
                    color = contentColor,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun GradientCard(
    gradient: Brush,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = PhotoboothTokens.glassStrong,
        shadowElevation = 16.dp,
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .border(1.dp, PhotoboothTokens.border, RoundedCornerShape(28.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = PhotoboothTokens.glass,
        shadowElevation = 14.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.72f), Color(0xFFF4F0FF).copy(alpha = 0.68f)),
                    ),
                )
                .border(1.dp, PhotoboothTokens.border, shape)
                .padding(16.dp),
            content = content,
        )
    }
}

@Composable
private fun BadgeChip(label: String, accent: Color, glowing: Boolean = false) {
    val pulse = rememberPulseScale()
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.14f),
        modifier = if (glowing) {
            Modifier.graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            }
        } else {
            Modifier
        },
    ) {
        Text(
            text = label,
            style = buttonStyle.copy(fontSize = 11.sp),
            color = accent,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun rememberPulseScale(): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    return scale
}

private fun eventPreviewGradient(event: LaunchEvent?): Brush {
    val seed = (event?.eventCode ?: event?.eventId ?: "default").hashCode().absoluteValue % 3
    val colors = when (seed) {
        0 -> listOf(Color(0xE6BAC4FF), Color(0xD9FFC3D7))
        1 -> listOf(Color(0xE6BDE7FF), Color(0xD9FFF1C4))
        else -> listOf(Color(0xE6FFD0D9), Color(0xD9C8F7FF))
    }
    return Brush.verticalGradient(colors = colors)
}

@Preview(name = "Dashboard Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun DashboardTabletPreview() {
    DafydioBoothTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhotoboothTokens.background),
        ) {
            DashboardScreen()
        }
    }
}

@Preview(name = "Dashboard Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun DashboardMobilePreview() {
    DafydioBoothTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhotoboothTokens.background),
        ) {
            DashboardScreen()
        }
    }
}

@Preview(name = "Dashboard Flow Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun DashboardFlowTabletPreview() {
    MaterialTheme {
        DashboardRedesignContainer(
            state = PreviewStateProvider.templateBase,
            actions = BoothActions(),
        )
    }
}

@Preview(name = "Header Mobile", widthDp = 390, heightDp = 160, showBackground = true)
@Composable
private fun DashboardHeaderMobilePreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhotoboothTokens.background)
                .padding(16.dp),
        ) {
            DashboardHeader(isMobile = true, isOnline = false)
        }
    }
}

@Preview(name = "Hero Mobile", widthDp = 390, heightDp = 940, showBackground = true)
@Composable
private fun HeroMobilePreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhotoboothTokens.background)
                .padding(16.dp),
        ) {
            HeroSection(
                isMobile = true,
                state = BoothUiState(),
                launchState = LaunchUiState(),
                actions = BoothActions(),
                onOpenLaunch = {},
                onQuickBooth = {},
                onSelectEvent = {},
                onViewAllTemplates = {},
                onOpenEvents = {}
            )
        }
    }
}

@Preview(name = "Hero Tablet", widthDp = 1280, heightDp = 560, showBackground = true)
@Composable
private fun HeroTabletPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhotoboothTokens.background)
                .padding(28.dp),
        ) {
            HeroSection(
                isMobile = false,
                state = BoothUiState(),
                launchState = LaunchUiState(),
                actions = BoothActions(),
                onOpenLaunch = {},
                onQuickBooth = {},
                onSelectEvent = {},
                onViewAllTemplates = {},
                onOpenEvents = {}
            )
        }
    }
}

@Preview(name = "Template Card Tablet", widthDp = 520, heightDp = 560, showBackground = true)
@Composable
private fun TemplateCardTabletPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhotoboothTokens.background)
                .padding(28.dp),
        ) {
            StartSessionCard(
                isMobile = false,
                stationConnected = true,
                templateItems = emptyList(),
                onQuickBooth = {},
                onViewAllTemplates = {},
            )
        }
    }
}

@Preview(name = "Template Picks Tablet", widthDp = 520, heightDp = 360, showBackground = true)
@Composable
private fun TemplatePicksTabletPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhotoboothTokens.background)
                .padding(28.dp),
        ) {
            PhotoCollageCard(isMobile = false, templateItems = emptyList())
        }
    }
}

@Preview(name = "Stats Mobile", widthDp = 390, heightDp = 420, showBackground = true)
@Composable
private fun StatsMobilePreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhotoboothTokens.background)
                .padding(16.dp),
        ) {
            StatsSection(isMobile = true, state = BoothUiState())
        }
    }
}


@Preview(name = "Dock Mobile", widthDp = 390, heightDp = 120, showBackground = true)
@Composable
private fun DockMobilePreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhotoboothTokens.background),
        ) {
            FloatingDock()
        }
    }
}
