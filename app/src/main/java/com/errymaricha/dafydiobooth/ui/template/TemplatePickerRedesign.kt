package com.errymaricha.dafydiobooth.ui.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.BoothUiState
import com.errymaricha.dafydiobooth.ui.booth.ScreenFrame
import com.errymaricha.dafydiobooth.ui.booth.TemplateSurface
import com.errymaricha.dafydiobooth.ui.launch.LaunchUiState
import java.io.File

private object TemplatePickerUiTokens {
    val ink = Color(0xFF1E2144)
    val inkSoft = Color(0xFF6E7694)
    val primary = Color(0xFF5B67FF)
    val pink = Color(0xFFFF6B9D)
    val purple = Color(0xFF8B5CF6)
    val success = Color(0xFF4ADE80)
    val glass = Color(0xF5FFFFFF)
    val border = Color(0x225B67FF)
}

@Composable
fun TemplatePickerPageRedesign(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
) {
    TemplatePickerScreen(
        state = state,
        launchState = launchState,
        actions = actions,
    )
}

@Composable
fun CustomTemplatePageRedesign(state: BoothUiState, actions: BoothActions) {
    CustomTemplateScreen(state = state, actions = actions)
}

@Composable
fun TemplatePreviewPageRedesign(state: BoothUiState, actions: BoothActions) {
    TemplatePreviewScreen(state = state, actions = actions)
}

@Composable
fun TemplatePickerScreen(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
) {
    val filteredTemplateItems = if (!state.localOnlySession && state.launchAllowedTemplateIds.isNotEmpty()) {
        state.availableTemplateItems.filter { state.launchAllowedTemplateIds.contains(it.templateId) }
    } else {
        state.availableTemplateItems
    }

    ScreenFrame(title = "Pilih Template", state = state, actions = actions) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isTablet = maxWidth >= 900.dp
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TemplatePickerHero(
                    eventName = state.launchEventName,
                    sessionCode = launchState.session?.sessionCode ?: state.session?.sessionCode ?: "-",
                    totalTemplates = filteredTemplateItems.size,
                    isTablet = isTablet,
                )

                if (filteredTemplateItems.isEmpty()) {
                    TemplatePickerEmptyState(
                        message = if (state.availableTemplateItems.isEmpty()) {
                            "Belum ada template lokal. Buka Settings lalu update template dari Photobooth Station."
                        } else {
                            "Tidak ada template yang diizinkan untuk event ini. Atur di Setting Event."
                        },
                    )
                } else {
                    TemplateGrid(
                        templates = filteredTemplateItems,
                        isTablet = isTablet,
                        onSelect = actions.selectTemplate,
                    )
                }
            }
        }
    }
}

@Composable
fun CustomTemplateScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Create Custom Template", state = state, actions = actions) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = TemplatePickerUiTokens.glass),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFEFF2FF), Color(0xFFFFF3F9)),
                        ),
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "Template custom sementara dibuat lokal. Editor detail akan dihubungkan ke Photobooth Station.",
                    color = TemplatePickerUiTokens.ink,
                )
                Button(
                    onClick = { actions.saveCustomTemplate("Custom Template") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TemplatePickerUiTokens.primary),
                ) {
                    Text("Use Custom Template", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TemplatePreviewScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Preview Template", state = state, actions = actions) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isTablet = maxWidth >= 900.dp
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = TemplatePickerUiTokens.glass),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFF4F7FF), Color.White, Color(0xFFFFF4F9)),
                            ),
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Canvas Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TemplatePickerUiTokens.ink)
                        Surface(shape = RoundedCornerShape(999.dp), color = TemplatePickerUiTokens.primary.copy(alpha = 0.12f)) {
                            Text(
                                text = state.selectedTemplatePaperSize ?: "-",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = TemplatePickerUiTokens.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Text("Klik slot di preview untuk memilih area kerja yang aktif.", style = MaterialTheme.typography.bodySmall, color = TemplatePickerUiTokens.inkSoft)
                    Text("Drag foto langsung di slot untuk custom letak foto.", style = MaterialTheme.typography.bodySmall, color = TemplatePickerUiTokens.primary)
                    TemplateSurface(state)
                    if (isTablet) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            PickerInfoCard("Template", state.selectedTemplate ?: "-", Modifier.weight(1f))
                            PickerInfoCard("Captured", "${state.capturedPhotosBySlot.size}/${state.templateSlotCount}", Modifier.weight(1f))
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            PickerInfoCard("Template", state.selectedTemplate ?: "-", Modifier.fillMaxWidth())
                            PickerInfoCard("Captured", "${state.capturedPhotosBySlot.size}/${state.templateSlotCount}", Modifier.fillMaxWidth())
                        }
                    }
                    Button(
                        onClick = actions.finishSession,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TemplatePickerUiTokens.primary),
                    ) {
                        Text("Finish", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplatePickerHero(
    eventName: String,
    sessionCode: String,
    totalTemplates: Int,
    isTablet: Boolean,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = maxWidth < 680.dp
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = TemplatePickerUiTokens.glass),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFEFF2FF), Color(0xFFFFF2F8)),
                        ),
                    )
                    .padding(if (isTablet) 20.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = eventName.ifBlank { "Pilih template untuk session ini" },
                            style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                            color = TemplatePickerUiTokens.ink,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Halaman ini khusus untuk memilih template yang akan digunakan di sesi aktif.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TemplatePickerUiTokens.inkSoft,
                        )
                        Surface(shape = RoundedCornerShape(999.dp), color = TemplatePickerUiTokens.success.copy(alpha = 0.14f)) {
                            Text("PICKER", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = TemplatePickerUiTokens.success, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = eventName.ifBlank { "Pilih template untuk session ini" },
                                style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                                color = TemplatePickerUiTokens.ink,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Halaman ini khusus untuk memilih template yang akan digunakan di sesi aktif.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TemplatePickerUiTokens.inkSoft,
                            )
                        }
                        Surface(shape = RoundedCornerShape(999.dp), color = TemplatePickerUiTokens.success.copy(alpha = 0.14f)) {
                            Text("PICKER", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = TemplatePickerUiTokens.success, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        PickerInfoCard("Kode Session", sessionCode, Modifier.fillMaxWidth())
                        PickerInfoCard("Template Tersedia", totalTemplates.toString(), Modifier.fillMaxWidth())
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        PickerInfoCard("Kode Session", sessionCode, Modifier.weight(1f))
                        PickerInfoCard("Template Tersedia", totalTemplates.toString(), Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerInfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.84f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TemplatePickerUiTokens.inkSoft)
            Text(value, style = MaterialTheme.typography.titleMedium, color = TemplatePickerUiTokens.ink, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TemplatePickerEmptyState(message: String) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = TemplatePickerUiTokens.glass),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Template belum tersedia", style = MaterialTheme.typography.titleMedium, color = TemplatePickerUiTokens.ink, fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = TemplatePickerUiTokens.inkSoft)
        }
    }
}

@Composable
private fun TemplateGrid(
    templates: List<com.errymaricha.dafydiobooth.ui.booth.TemplateListItem>,
    isTablet: Boolean,
    onSelect: (String) -> Unit,
) {
    val columns = if (isTablet) 4 else 2
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        templates.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                rowItems.forEach { template ->
                    TemplateSelectionCard(
                        template = template,
                        modifier = Modifier.weight(1f).height(340.dp),
                        onSelect = onSelect,
                    )
                }
                repeat(columns - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TemplateSelectionCard(
    template: com.errymaricha.dafydiobooth.ui.booth.TemplateListItem,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    val canSelectTemplate = template.previewReady && template.overlayReady
    val statusAccent = if (canSelectTemplate) TemplatePickerUiTokens.success else TemplatePickerUiTokens.pink
    val selectionHint = when {
        canSelectTemplate -> "Template siap dipakai."
        !template.previewReady && !template.overlayReady -> "Preview dan overlay belum siap di device."
        !template.previewReady -> "Preview template belum siap di device."
        else -> "Overlay template belum siap di device."
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = TemplatePickerUiTokens.glass),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            TemplatePickerUiTokens.primary.copy(alpha = 0.05f),
                            Color.White,
                        ),
                    ),
                )
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PickerTemplateThumbnail(template = template)
            Text(template.templateName, style = MaterialTheme.typography.titleMedium, color = TemplatePickerUiTokens.ink, fontWeight = FontWeight.Bold)
            Text("${template.templateCode} • ${template.category ?: "Photobooth"}", style = MaterialTheme.typography.bodySmall, color = TemplatePickerUiTokens.inkSoft)
            Text(selectionHint, style = MaterialTheme.typography.bodySmall, color = statusAccent)
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { onSelect(template.templateId) },
                enabled = canSelectTemplate,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canSelectTemplate) TemplatePickerUiTokens.primary else MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Text(if (canSelectTemplate) "Pilih Template" else "Belum Siap")
            }
        }
    }
}

@Composable
private fun PickerTemplateThumbnail(template: com.errymaricha.dafydiobooth.ui.booth.TemplateListItem) {
    val accent = when ((template.category ?: "").lowercase()) {
        "birthday" -> TemplatePickerUiTokens.pink
        "wedding" -> TemplatePickerUiTokens.purple
        else -> TemplatePickerUiTokens.primary
    }
    val thumbnailModel = rememberPickerThumbnailModel(template.thumbnailUrl)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.25f), Color.White),
                    ),
                )
        ) {
            if (thumbnailModel != null) {
                AsyncImage(
                    model = thumbnailModel,
                    contentDescription = "Thumbnail ${template.templateName}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(template.paperSize ?: "-", style = MaterialTheme.typography.labelSmall, color = TemplatePickerUiTokens.inkSoft)
                Text("${template.slotCount} slot", style = MaterialTheme.typography.titleSmall, color = accent, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun rememberPickerThumbnailModel(thumbnailUrl: String?): Any? {
    return remember(thumbnailUrl) {
        thumbnailUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { path ->
                val file = File(path)
                if (file.exists()) file else path
            }
    }
}
