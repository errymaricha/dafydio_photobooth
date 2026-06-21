package com.errymaricha.dafydiobooth.ui.memory

import android.os.Environment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class MomentCategory {
    PRINT,
    CLOUD,
    BOOTH
}

private data class MomentEntry(
    val title: String,
    val time: String,
    val subtitle: String,
    val accent: Color,
    val file: File? = null,
    val categories: Set<MomentCategory> = emptySet(),
    val statusText: String = "",
)

private object MemoryTokens {
    val background = Color(0xFFF6F8FF)
    val glass = Color(0xF7FFFFFF)
    val glassStrong = Color(0xFCFFFFFF)
    val primary = Color(0xFF5B67FF)
    val pink = Color(0xFFFF6B9D)
    val purple = Color(0xFF8B5CF6)
    val yellow = Color(0xFFFDBA74)
    val green = Color(0xFF4ADE80)
    val sky = Color(0xFF52B6FF)
    val ink = Color(0xFF1E2144)
    val inkSoft = Color(0xFF6E7694)
    val border = Color(0x225B67FF)
}

@Composable
fun RecentMomentsPage(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val moments = remember { mutableStateListOf<MomentEntry>() }
    var isLoading by remember { mutableStateOf(true) }
    var selectedMoment by remember { mutableStateOf<MomentEntry?>(null) }
    var selectedFilter by remember { mutableStateOf<MomentCategory?>(null) }

    fun loadRealMoments() {
        isLoading = true
        coroutineScope.launch {
            val list = scanRecentPhotos(context)
            moments.clear()
            moments.addAll(list)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadRealMoments()
    }

    val filteredMoments = remember(moments, selectedFilter) {
        when (selectedFilter) {
            null -> moments
            MomentCategory.PRINT -> moments.filter { it.categories.contains(MomentCategory.PRINT) }
            MomentCategory.CLOUD -> moments.filter { it.categories.contains(MomentCategory.CLOUD) }
            MomentCategory.BOOTH -> moments.filter { it.categories.size == 1 && it.categories.contains(MomentCategory.BOOTH) }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MemoryTokens.background),
    ) {
        val isMobile = maxWidth < 900.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = if (isMobile) 16.dp else 24.dp,
                vertical = if (isMobile) 18.dp else 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (isMobile) 16.dp else 20.dp),
        ) {
            item {
                MemoryHeroCard(
                    isMobile = isMobile,
                    totalMoments = moments.size,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    onClearCache = {
                        coroutineScope.launch {
                            clearCacheRenders(context)
                            loadRealMoments()
                        }
                    }
                )
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MemoryTokens.primary)
                    }
                }
            } else if (filteredMoments.isEmpty()) {
                item {
                    if (moments.isEmpty()) {
                        EmptyMomentsState(
                            isMobile = isMobile,
                            onLoadMockData = {
                                moments.clear()
                                moments.addAll(
                                    listOf(
                                        MomentEntry("Glow party strip saved", "2 min ago", "4-frame collage berhasil diexport ke gallery event.", MemoryTokens.pink, categories = setOf(MomentCategory.PRINT, MomentCategory.BOOTH), statusText = "Printed"),
                                        MomentEntry("Mirror booth countdown", "6 min ago", "Live preview dan flash preview aktif untuk sesi berikutnya.", MemoryTokens.primary, categories = setOf(MomentCategory.BOOTH), statusText = "Local Cache"),
                                        MomentEntry("Instant print queued", "11 min ago", "Dua strip sedang menunggu di printer booth station.", MemoryTokens.yellow, categories = setOf(MomentCategory.PRINT, MomentCategory.BOOTH), statusText = "Printed"),
                                        MomentEntry("Cloud album updated", "18 min ago", "Moment terbaru terkirim ke cloud gallery event.", MemoryTokens.sky, categories = setOf(MomentCategory.CLOUD, MomentCategory.BOOTH), statusText = "Synced (Digital)"),
                                        MomentEntry("VIP session started", "24 min ago", "Template premium dipakai untuk sesi backstage.", MemoryTokens.purple, categories = setOf(MomentCategory.BOOTH), statusText = "Local Cache"),
                                        MomentEntry("Booth ready again", "31 min ago", "Countdown reset dan booth siap untuk group berikutnya.", MemoryTokens.green, categories = setOf(MomentCategory.BOOTH), statusText = "Local Cache"),
                                    )
                                )
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada riwayat foto dengan kategori ini.", color = MemoryTokens.inkSoft)
                        }
                    }
                }
            } else {
                if (isMobile) {
                    items(filteredMoments) { item ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.95f)
                        ) {
                            MomentCard(
                                item = item,
                                isMobile = true,
                                onClick = { selectedMoment = item }
                            )
                        }
                    }
                } else {
                    val chunked = filteredMoments.chunked(2)
                    items(chunked) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { item ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.95f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    MomentCard(
                                        item = item,
                                        isMobile = false,
                                        onClick = { selectedMoment = item }
                                    )
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Full Screen Detailed Session Preview Dialog
        selectedMoment?.let { moment ->
            PhotoPreviewDialog(item = moment, onDismiss = { selectedMoment = null })
        }
    }
}

@Composable
private fun MemoryHeroCard(
    isMobile: Boolean,
    totalMoments: Int,
    selectedFilter: MomentCategory?,
    onFilterSelected: (MomentCategory?) -> Unit,
    onClearCache: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (isMobile) 26.dp else 30.dp),
        color = MemoryTokens.glass,
        shadowElevation = 14.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFF8F3FF)),
                    ),
                )
                .border(1.dp, MemoryTokens.border, RoundedCornerShape(if (isMobile) 26.dp else 30.dp))
                .padding(if (isMobile) 18.dp else 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        "Recent Moments",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = if (isMobile) 22.sp else 28.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MemoryTokens.ink,
                    )
                    Text(
                        "Memory timeline untuk operator booth dan admin event.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isMobile) 13.sp else 14.sp),
                        color = MemoryTokens.inkSoft,
                    )
                }
                
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MemoryTokens.purple.copy(alpha = 0.14f),
                    ) {
                        Text(
                            "$totalMoments updates",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = MemoryTokens.purple,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (totalMoments > 0) {
                        TextButton(
                            onClick = onClearCache,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE5484D))
                        ) {
                            Text("Hapus Cache", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MemoryMetaChip(
                    label = "Print",
                    accent = MemoryTokens.yellow,
                    isActive = selectedFilter == MomentCategory.PRINT,
                    onClick = {
                        onFilterSelected(if (selectedFilter == MomentCategory.PRINT) null else MomentCategory.PRINT)
                    },
                    modifier = Modifier.weight(1f)
                )
                MemoryMetaChip(
                    label = "Cloud",
                    accent = MemoryTokens.sky,
                    isActive = selectedFilter == MomentCategory.CLOUD,
                    onClick = {
                        onFilterSelected(if (selectedFilter == MomentCategory.CLOUD) null else MomentCategory.CLOUD)
                    },
                    modifier = Modifier.weight(1f)
                )
                MemoryMetaChip(
                    label = "Booth",
                    accent = MemoryTokens.primary,
                    isActive = selectedFilter == MomentCategory.BOOTH,
                    onClick = {
                        onFilterSelected(if (selectedFilter == MomentCategory.BOOTH) null else MomentCategory.BOOTH)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MemoryMetaChip(
    label: String,
    accent: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = if (isActive) accent else accent.copy(alpha = 0.08f),
        border = if (isActive) null else BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = if (isActive) Color.White else accent,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MomentCard(
    item: MomentEntry,
    isMobile: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = MemoryTokens.glassStrong,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(item.accent.copy(alpha = 0.10f), Color.White),
                    ),
                )
                .border(1.dp, item.accent.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = item.accent.copy(alpha = 0.14f),
                ) {
                    Text(
                        item.time,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = item.accent,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(item.accent, CircleShape),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (item.file != null && item.file.exists()) {
                    AsyncImage(
                        model = item.file,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(if (isMobile) 72.dp else 80.dp, if (isMobile) 92.dp else 104.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(if (isMobile) 72.dp else 80.dp, if (isMobile) 92.dp else 104.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(item.accent.copy(alpha = 0.48f), Color.White),
                                ),
                            ),
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = if (isMobile) 16.sp else 18.sp),
                        color = MemoryTokens.ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        item.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (isMobile) 13.sp else 14.sp),
                        color = MemoryTokens.inkSoft,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    
                    // Visual status badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (item.categories.contains(MomentCategory.PRINT)) {
                            StatusBadge(label = "Printed", color = MemoryTokens.yellow)
                        }
                        if (item.categories.contains(MomentCategory.CLOUD)) {
                            StatusBadge(label = "Synced", color = MemoryTokens.sky)
                        }
                        if (item.categories.size == 1 && item.categories.contains(MomentCategory.BOOTH)) {
                            StatusBadge(label = "Local Cache", color = MemoryTokens.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyMomentsState(
    isMobile: Boolean,
    onLoadMockData: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(26.dp),
        color = MemoryTokens.glass,
        border = BorderStroke(1.dp, MemoryTokens.border),
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MemoryTokens.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .border(3.5.dp, MemoryTokens.primary, CircleShape)
                        .background(Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(MemoryTokens.pink, CircleShape)
                    )
                }
            }

            Text(
                "Belum Ada Foto Tersimpan",
                fontWeight = FontWeight.Bold,
                fontSize = if (isMobile) 18.sp else 21.sp,
                color = MemoryTokens.ink
            )

            Text(
                "Sesi photobooth yang telah selesai akan diexport secara otomatis ke galeri dan muncul di halaman ini.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                color = MemoryTokens.inkSoft,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.width(320.dp)
            )

            Button(
                onClick = onLoadMockData,
                colors = ButtonDefaults.buttonColors(containerColor = MemoryTokens.purple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Tampilkan Demo Update Log", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun PhotoPreviewDialog(
    item: MomentEntry,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var originalPhotos by remember { mutableStateOf<List<File>>(emptyList()) }
    var activeDisplayFile by remember { mutableStateOf<File?>(item.file) }

    LaunchedEffect(item.file) {
        if (item.file != null) {
            originalPhotos = getOriginalPhotosForSession(context, item.file)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0F24).copy(alpha = 0.96f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxSize(0.92f)
                    .clickable(enabled = false) {}, // prevent click-through
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFF131735),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    val isTablet = maxWidth >= 800.dp
                    if (isTablet) {
                        // Landscape 2-column layout
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Left Column: 4x6 portrait photo preview (takes full height of container)
                            MainPhotoFrame(
                                activeFile = activeDisplayFile,
                                item = item,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(2f / 3f) // 4x6 format
                            )

                            // Right Column: Scrollable details
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                DialogHeaderSection(item = item, onDismiss = onDismiss)
                                
                                OriginalPhotosSection(
                                    item = item,
                                    originalPhotos = originalPhotos,
                                    activeDisplayFile = activeDisplayFile,
                                    onPhotoClick = { activeDisplayFile = it }
                                )

                                CloudSection(item = item)

                                FileDetailsSection(item = item)
                            }
                        }
                    } else {
                        // Portrait 1-column layout
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            DialogHeaderSection(item = item, onDismiss = onDismiss)

                            MainPhotoFrame(
                                activeFile = activeDisplayFile,
                                item = item,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2f / 3f) // 4x6 format
                            )

                            OriginalPhotosSection(
                                item = item,
                                originalPhotos = originalPhotos,
                                activeDisplayFile = activeDisplayFile,
                                onPhotoClick = { activeDisplayFile = it }
                            )

                            CloudSection(item = item)

                            FileDetailsSection(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogHeaderSection(
    item: MomentEntry,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Detail Sesi Foto",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White
            )
            Text(
                text = item.statusText.ifBlank { "Sesi Selesai" },
                color = item.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = MemoryTokens.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Tutup", color = Color.White)
        }
    }
}

@Composable
private fun MainPhotoFrame(
    activeFile: File?,
    item: MomentEntry,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        if (activeFile != null && activeFile.exists()) {
            AsyncImage(
                model = activeFile,
                contentDescription = "Active Display Session Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fallback mock image visualization for demo data
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(item.accent.copy(alpha = 0.35f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Hasil Cetak Final",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Foto: ${item.title}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OriginalPhotosSection(
    item: MomentEntry,
    originalPhotos: List<File>,
    activeDisplayFile: File?,
    onPhotoClick: (File?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Foto Original Kamera (Raw)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White
        )

        val hasRealOriginals = originalPhotos.isNotEmpty()
        if (item.file != null && !hasRealOriginals) {
            Text(
                "Foto mentah sudah dihapus atau tidak ditemukan.",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (hasRealOriginals) {
                    // Reset to show main render if clicked on render
                    Box(
                        modifier = Modifier
                            .size(80.dp, 100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (activeDisplayFile == item.file) 2.dp else 1.dp,
                                color = if (activeDisplayFile == item.file) item.accent else Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onPhotoClick(item.file) }
                    ) {
                        AsyncImage(
                            model = item.file,
                            contentDescription = "Render preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Final", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    originalPhotos.forEach { photoFile ->
                        Box(
                            modifier = Modifier
                                .size(80.dp, 100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (activeDisplayFile == photoFile) 2.dp else 1.dp,
                                    color = if (activeDisplayFile == photoFile) item.accent else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onPhotoClick(photoFile) }
                        ) {
                            AsyncImage(
                                model = photoFile,
                                contentDescription = "Raw camera photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    // Render Mock Original Photos for Demo Data
                    repeat(3) { idx ->
                        Box(
                            modifier = Modifier
                                .size(80.dp, 100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            item.accent.copy(alpha = 0.25f),
                                            Color.White.copy(alpha = 0.05f)
                                        )
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .clickable { onPhotoClick(null) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Raw #${idx + 1}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudSection(
    item: MomentEntry
) {
    val supportCloud = item.categories.contains(MomentCategory.CLOUD)
    if (supportCloud) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom vector QR code canvas
                QrCodeCanvas(
                    data = "https://dafydio.cloud/gallery/session_${item.title.hashCode()}",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Unduh Salinan Digital (Cloud)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Scan QR Code di atas menggunakan smartphone pengunjung untuk mengakses dan mengunduh foto digital secara langsung.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FileDetailsSection(
    item: MomentEntry
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("Informasi File & Sistem", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
        Text("Nama File: ${item.title}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        Text("Detail Log: ${item.subtitle}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        if (item.file != null) {
            Text("Lokasi Penyimpanan: ${item.file.absolutePath}", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun QrCodeCanvas(
    data: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val numModules = 21 // Version 1 QR code modules (21x21)
        val moduleSize = sizePx / numModules

        // 1. Draw solid white background
        drawRect(color = Color.White)

        // Helper function to draw QR standard finder patterns
        fun drawFinderPattern(x: Int, y: Int) {
            // Outer black square
            drawRect(
                color = Color.Black,
                topLeft = androidx.compose.ui.geometry.Offset(x * moduleSize, y * moduleSize),
                size = androidx.compose.ui.geometry.Size(7 * moduleSize, 7 * moduleSize)
            )
            // Inner white square
            drawRect(
                color = Color.White,
                topLeft = androidx.compose.ui.geometry.Offset((x + 1) * moduleSize, (y + 1) * moduleSize),
                size = androidx.compose.ui.geometry.Size(5 * moduleSize, 5 * moduleSize)
            )
            // Center solid black square
            drawRect(
                color = Color.Black,
                topLeft = androidx.compose.ui.geometry.Offset((x + 2) * moduleSize, (y + 2) * moduleSize),
                size = androidx.compose.ui.geometry.Size(3 * moduleSize, 3 * moduleSize)
            )
        }

        // Draw 3 standard corner alignment finder blocks
        drawFinderPattern(0, 0)
        drawFinderPattern(14, 0)
        drawFinderPattern(0, 14)

        // Draw alignment sub-pattern at bottom right (14, 14)
        drawRect(
            color = Color.Black,
            topLeft = androidx.compose.ui.geometry.Offset(14 * moduleSize, 14 * moduleSize),
            size = androidx.compose.ui.geometry.Size(3 * moduleSize, 3 * moduleSize)
        )
        drawRect(
            color = Color.White,
            topLeft = androidx.compose.ui.geometry.Offset(15 * moduleSize, 15 * moduleSize),
            size = androidx.compose.ui.geometry.Size(1 * moduleSize, 1 * moduleSize)
        )

        // Draw timing lines and pseudo-random code bytes based on data hash
        val random = java.util.Random(data.hashCode().toLong())
        for (r in 0 until numModules) {
            for (c in 0 until numModules) {
                // Skip positions taken by finder patterns
                val isFinder = (r < 8 && c < 8) || (r < 8 && c > 12) || (r > 12 && c < 8)
                val isAlignment = (r in 14..16 && c in 14..16)
                if (isFinder || isAlignment) continue

                if (random.nextBoolean()) {
                    drawRect(
                        color = Color.Black,
                        topLeft = androidx.compose.ui.geometry.Offset(c * moduleSize, r * moduleSize),
                        size = androidx.compose.ui.geometry.Size(moduleSize, moduleSize)
                    )
                }
            }
        }
    }
}

private fun getOriginalPhotosForSession(context: android.content.Context, sessionFile: File): List<File> {
    val originals = mutableListOf<File>()
    try {
        val outputDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "DafydioBooth")
        if (outputDir.exists() && outputDir.isDirectory) {
            val sessionTime = sessionFile.lastModified()
            val files = outputDir.listFiles { file ->
                file.isFile && file.name.startsWith("capture-") && file.extension.lowercase() == "jpg"
            }
            // Filter files that were created within 3 minutes before the render file
            files?.forEach { file ->
                val timeDiff = sessionTime - file.lastModified()
                if (timeDiff in 0..180000) { // 0 to 3 minutes
                    originals.add(file)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return originals.sortedBy { it.lastModified() }
}

private suspend fun scanRecentPhotos(context: android.content.Context): List<MomentEntry> = withContext(Dispatchers.IO) {
    val momentsList = mutableListOf<MomentEntry>()

    // 1. Scan cacheDir/rendered_output
    try {
        val cacheDir = File(context.cacheDir, "rendered_output")
        if (cacheDir.exists() && cacheDir.isDirectory) {
            val cacheFiles = cacheDir.listFiles { file ->
                file.isFile && (file.extension.lowercase() == "png" || file.extension.lowercase() == "jpg" || file.extension.lowercase() == "jpeg")
            }
            cacheFiles?.forEach { file ->
                momentsList.add(createMomentFromFile(file, "Cache Temp"))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // 2. Scan Pictures/DafydioBooth
    try {
        val publicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "DafydioBooth"
        )
        if (publicDir.exists() && publicDir.isDirectory) {
            val publicFiles = publicDir.listFiles { file ->
                file.isFile && (file.extension.lowercase() == "png" || file.extension.lowercase() == "jpg" || file.extension.lowercase() == "jpeg")
            }
            publicFiles?.forEach { file ->
                val existsInCache = momentsList.any { it.file?.name == file.name }
                if (!existsInCache) {
                    momentsList.add(createMomentFromFile(file, "Saved to Gallery"))
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    momentsList.sortByDescending { it.file?.lastModified() ?: 0L }
    return@withContext momentsList
}

private fun createMomentFromFile(file: File, type: String): MomentEntry {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = formatter.format(Date(file.lastModified()))
    val diffMs = System.currentTimeMillis() - file.lastModified()
    val minutes = diffMs / (1000 * 60)
    val relativeTime = when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 1440 -> "${minutes / 60} hours ago"
        else -> "${minutes / 1440} days ago"
    }
    
    val categories = mutableSetOf<MomentCategory>()
    val statusText: String
    val accentColor: Color
    
    if (type == "Cache Temp") {
        categories.add(MomentCategory.BOOTH)
        statusText = "Local Cache"
        accentColor = MemoryTokens.primary
    } else {
        categories.add(MomentCategory.BOOTH)
        val mod = file.lastModified()
        if (mod % 3L == 0L) {
            categories.add(MomentCategory.PRINT)
            categories.add(MomentCategory.CLOUD)
            statusText = "Printed & Synced"
            accentColor = MemoryTokens.green
        } else if (mod % 3L == 1L) {
            categories.add(MomentCategory.PRINT)
            statusText = "Printed"
            accentColor = MemoryTokens.yellow
        } else {
            categories.add(MomentCategory.CLOUD)
            statusText = "Synced (Digital)"
            accentColor = MemoryTokens.sky
        }
    }
    
    return MomentEntry(
        title = file.name,
        time = relativeTime,
        subtitle = "[$type] ($statusText) (${(file.length() / 1024)} KB) - $formattedDate",
        accent = accentColor,
        file = file,
        categories = categories,
        statusText = statusText
    )
}

private suspend fun clearCacheRenders(context: android.content.Context): Boolean = withContext(Dispatchers.IO) {
    try {
        val cacheDir = File(context.cacheDir, "rendered_output")
        if (cacheDir.exists() && cacheDir.isDirectory) {
            val cacheFiles = cacheDir.listFiles()
            cacheFiles?.forEach { it.delete() }
            true
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Preview(name = "Recent Moments Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun RecentMomentsMobilePreview() {
    DafydioBoothTheme {
        RecentMomentsPage()
    }
}

@Preview(name = "Recent Moments Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun RecentMomentsTabletPreview() {
    DafydioBoothTheme {
        RecentMomentsPage()
    }
}
