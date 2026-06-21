package com.errymaricha.dafydiobooth.ui.template

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.BoothUiState
import com.errymaricha.dafydiobooth.ui.booth.ScreenFrame
import com.errymaricha.dafydiobooth.ui.booth.TemplateSurface
import com.errymaricha.dafydiobooth.ui.booth.TemplateListItem
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

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember(filteredTemplateItems) {
        listOf("All") + filteredTemplateItems.mapNotNull { it.category }.distinct().sorted()
    }

    val displayedTemplates = remember(filteredTemplateItems, searchQuery, selectedCategory) {
        filteredTemplateItems.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.templateName.contains(searchQuery, ignoreCase = true) ||
                    item.templateCode.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    // Set scrollable = false pada ScreenFrame agar scroll dinonaktifkan di parent Column
    ScreenFrame(title = "Pilih Template", state = state, actions = actions, scrollable = false) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val isTablet = maxWidth >= 900.dp
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // Header info bar tipis pengganti Hero Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.launchEventName.ifBlank { "Pilih template untuk session ini" },
                        style = MaterialTheme.typography.titleMedium,
                        color = TemplatePickerUiTokens.ink,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = TemplatePickerUiTokens.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Sesi: ${launchState.session?.sessionCode ?: state.session?.sessionCode ?: "-"}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TemplatePickerUiTokens.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = TemplatePickerUiTokens.success.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${filteredTemplateItems.size} Tersedia",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TemplatePickerUiTokens.success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Search & Filter Panel
                if (filteredTemplateItems.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = TemplatePickerUiTokens.glass),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFF6F8FF), Color(0xFFFFF7FC)),
                                    ),
                                )
                                .border(1.dp, TemplatePickerUiTokens.border, RoundedCornerShape(20.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cari nama atau kode template...", color = TemplatePickerUiTokens.inkSoft) },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = TemplatePickerUiTokens.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = TemplatePickerUiTokens.inkSoft,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TemplatePickerUiTokens.primary,
                                    unfocusedBorderColor = TemplatePickerUiTokens.border,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (categories.size > 1) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(categories) { category ->
                                        CategoryPill(
                                            label = category,
                                            selected = selectedCategory == category,
                                            onClick = { selectedCategory = category }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (filteredTemplateItems.isEmpty()) {
                    TemplatePickerEmptyState(
                        message = if (state.availableTemplateItems.isEmpty()) {
                            "Belum ada template lokal. Buka Settings lalu update template dari Photobooth Station."
                        } else {
                            "Tidak ada template yang diizinkan untuk event ini. Atur di Setting Event."
                        },
                    )
                } else if (displayedTemplates.isEmpty()) {
                    TemplatePickerEmptyState(
                        message = "Tidak ada template yang cocok dengan pencarian atau filter kategori Anda."
                    )
                } else {
                    // Menggunakan LazyVerticalGrid bawaan Compose untuk optimasi memory daur ulang (Lazy Loading)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (isTablet) 6 else 4),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(
                            items = displayedTemplates,
                            key = { it.templateId }
                        ) { template ->
                            TemplateSelectionCard(
                                template = template,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp),
                                onSelect = actions.selectTemplate
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        Brush.horizontalGradient(
            colors = listOf(TemplatePickerUiTokens.primary, TemplatePickerUiTokens.purple)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.White, Color.White)
        )
    }

    val textColor = if (selected) Color.White else TemplatePickerUiTokens.inkSoft
    val borderColor = if (selected) Color.Transparent else TemplatePickerUiTokens.border

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall
        )
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
private fun TemplateSelectionCard(
    template: TemplateListItem,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    val canSelectTemplate = template.previewReady && template.overlayReady
    val statusAccent = if (canSelectTemplate) TemplatePickerUiTokens.success else TemplatePickerUiTokens.pink

    val cardBorder = if (canSelectTemplate) {
        TemplatePickerUiTokens.border
    } else {
        TemplatePickerUiTokens.pink.copy(alpha = 0.3f)
    }

    Card(
        onClick = { onSelect(template.templateId) },
        enabled = canSelectTemplate,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canSelectTemplate) TemplatePickerUiTokens.glass else Color(0xFFF1F3F9).copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (canSelectTemplate) 4.dp else 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(
                    if (canSelectTemplate) {
                        Brush.verticalGradient(
                            colors = listOf(
                                TemplatePickerUiTokens.primary.copy(alpha = 0.03f),
                                Color.White,
                            ),
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFE2E5EC), Color(0xFFEFF1F6))
                        )
                    }
                )
                .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                .fillMaxSize()
        ) {
            PickerTemplateThumbnail(template = template)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = template.templateName,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canSelectTemplate) TemplatePickerUiTokens.ink else TemplatePickerUiTokens.inkSoft,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = template.templateCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = TemplatePickerUiTokens.inkSoft,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(statusAccent)
                    )
                    Text(
                        text = if (canSelectTemplate) "Siap pakai" else "Belum lengkap",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerTemplateThumbnail(template: TemplateListItem) {
    val accent = when ((template.category ?: "").lowercase()) {
        "birthday" -> TemplatePickerUiTokens.pink
        "wedding" -> TemplatePickerUiTokens.purple
        else -> TemplatePickerUiTokens.primary
    }
    val thumbnailModel = rememberPickerThumbnailModel(template.thumbnailUrl)
    Surface(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        color = Color.White,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.15f), Color.White),
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
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(accent.copy(alpha = 0.1f), Color.White)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = template.category?.take(3)?.uppercase() ?: "TPL",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Overlay for slots count (top-left)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accent.copy(alpha = 0.9f),
                modifier = Modifier.padding(6.dp).align(Alignment.TopStart)
            ) {
                Text(
                    text = "${template.slotCount} Slot",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Overlay for paper size (bottom banner)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(vertical = 2.dp, horizontal = 6.dp)
            ) {
                Text(
                    text = template.paperSize?.replace(" Strip", "") ?: "Custom",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
