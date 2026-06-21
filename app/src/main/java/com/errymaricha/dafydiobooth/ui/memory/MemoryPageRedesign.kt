package com.errymaricha.dafydiobooth.ui.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme

private data class MomentEntry(
    val title: String,
    val time: String,
    val subtitle: String,
    val accent: Color,
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
    val moments = remember {
        listOf(
            MomentEntry("Glow party strip saved", "2 min ago", "4-frame collage berhasil diexport ke gallery event.", MemoryTokens.pink),
            MomentEntry("Mirror booth countdown", "6 min ago", "Live preview dan flash preview aktif untuk sesi berikutnya.", MemoryTokens.primary),
            MomentEntry("Instant print queued", "11 min ago", "Dua strip sedang menunggu di printer booth station.", MemoryTokens.yellow),
            MomentEntry("Cloud album updated", "18 min ago", "Moment terbaru terkirim ke cloud gallery event.", MemoryTokens.sky),
            MomentEntry("VIP session started", "24 min ago", "Template premium dipakai untuk sesi backstage.", MemoryTokens.purple),
            MomentEntry("Booth ready again", "31 min ago", "Countdown reset dan booth siap untuk group berikutnya.", MemoryTokens.green),
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MemoryTokens.background),
    ) {
        val isMobile = maxWidth < 900.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isMobile) 16.dp else 24.dp, vertical = if (isMobile) 18.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(if (isMobile) 16.dp else 20.dp),
        ) {
            MemoryHeroCard(isMobile = isMobile, totalMoments = moments.size)
            if (isMobile) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    moments.forEach { item ->
                        MomentCard(item = item, isMobile = true)
                    }
                }
            } else {
                moments.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { item ->
                            MomentCard(item = item, isMobile = false, modifier = Modifier.weight(1f))
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryHeroCard(isMobile: Boolean, totalMoments: Int) {
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
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MemoryMetaChip("Print", MemoryTokens.yellow, Modifier.weight(1f))
                MemoryMetaChip("Cloud", MemoryTokens.sky, Modifier.weight(1f))
                MemoryMetaChip("Booth", MemoryTokens.primary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MemoryMetaChip(label: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.12f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, color = accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MomentCard(
    item: MomentEntry,
    isMobile: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
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
                }
            }
        }
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
