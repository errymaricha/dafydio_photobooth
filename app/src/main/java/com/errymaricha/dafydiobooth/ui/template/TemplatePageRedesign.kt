package com.errymaricha.dafydiobooth.ui.template

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.errymaricha.dafydiobooth.ui.booth.BoothStep

import com.errymaricha.dafydiobooth.ui.booth.BoothActions
import com.errymaricha.dafydiobooth.ui.booth.BoothUiState
import com.errymaricha.dafydiobooth.ui.booth.TemplateListItem
import com.errymaricha.dafydiobooth.ui.booth.preview.PreviewStateProvider
import com.errymaricha.dafydiobooth.ui.theme.DafydioBoothTheme
import java.io.File

private enum class TemplateSortOption(val label: String) {
    NameAsc("Nama A-Z"),
    NameDesc("Nama Z-A"),
    Newest("Terbaru"),
    Category("Kategori"),
}

private object TemplateUiTokens {
    val background = Color(0xFFF7F8FF)
    val ink = Color(0xFF1E2144)
    val inkSoft = Color(0xFF6E7694)
    val primary = Color(0xFF5B67FF)
    val pink = Color(0xFFFF6B9D)
    val purple = Color(0xFF8B5CF6)
    val success = Color(0xFF4ADE80)
    val warning = Color(0xFFFDBA74)
    val glass = Color(0xF5FFFFFF)
    val border = Color(0x225B67FF)
}

private val templatePreviewMockItems = listOf(
    TemplateListItem(
        templateId = "tpl-01",
        templateName = "Mirror Pop",
        templateCode = "MR-POP-01",
        category = "Birthday",
        paperSize = "4R Strip",
        thumbnailUrl = null,
        thumbnailReady = true,
        previewReady = true,
        overlayReady = true,
        slotCount = 4,
    ),
    TemplateListItem(
        templateId = "tpl-02",
        templateName = "Glow Frame",
        templateCode = "GLW-FRM-02",
        category = "Korean Booth",
        paperSize = "4R",
        thumbnailUrl = null,
        thumbnailReady = true,
        previewReady = true,
        overlayReady = true,
        slotCount = 3,
    ),
    TemplateListItem(
        templateId = "tpl-03",
        templateName = "Soft Flash",
        templateCode = "SFT-FLS-03",
        category = "Wedding",
        paperSize = "2x6 Strip",
        thumbnailUrl = null,
        thumbnailReady = true,
        previewReady = true,
        overlayReady = true,
        slotCount = 4,
    ),
    TemplateListItem(
        templateId = "tpl-04",
        templateName = "After Party",
        templateCode = "AFT-PTY-04",
        category = "Party",
        paperSize = "4R",
        thumbnailUrl = null,
        thumbnailReady = true,
        previewReady = true,
        overlayReady = true,
        slotCount = 2,
    ),
    TemplateListItem(
        templateId = "tpl-05",
        templateName = "K-Strip",
        templateCode = "KST-05",
        category = "Photobooth",
        paperSize = "2x6 Strip",
        thumbnailUrl = null,
        thumbnailReady = true,
        previewReady = true,
        overlayReady = true,
        slotCount = 4,
    ),
    TemplateListItem(
        templateId = "tpl-06",
        templateName = "Mono Chic",
        templateCode = "MNC-06",
        category = "Minimal",
        paperSize = "4R Strip",
        thumbnailUrl = null,
        thumbnailReady = true,
        previewReady = true,
        overlayReady = true,
        slotCount = 3,
    ),
)

@Composable
fun ViewAllTemplatesScreen(
    state: BoothUiState,
    actions: BoothActions,
) {
    val rawTemplates = state.availableTemplateItems
    var selectedTemplateId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(TemplateSortOption.NameAsc) }
    val normalizedQuery = remember(searchQuery) { searchQuery.trim().lowercase() }
    val templates by remember(rawTemplates, normalizedQuery, sortOption) {
        derivedStateOf {
            rawTemplates
                .asSequence()
                .filter { item ->
                    normalizedQuery.isBlank() ||
                        item.templateName.lowercase().contains(normalizedQuery) ||
                        item.templateCode.lowercase().contains(normalizedQuery) ||
                        (item.category?.lowercase()?.contains(normalizedQuery) == true)
                }
                .let { filtered ->
                    when (sortOption) {
                        TemplateSortOption.NameAsc -> filtered.sortedBy { it.templateName.lowercase() }
                        TemplateSortOption.NameDesc -> filtered.sortedByDescending { it.templateName.lowercase() }
                        TemplateSortOption.Newest -> filtered.sortedByDescending { it.templateId }
                        TemplateSortOption.Category -> filtered.sortedWith(compareBy({ it.category.orEmpty().lowercase() }, { it.templateName.lowercase() }))
                    }
                }
                .toList()
        }
    }
    val selectedTemplate by remember(templates, selectedTemplateId) {
        derivedStateOf { templates.firstOrNull { it.templateId == selectedTemplateId } }
    }
    val isDetailVisible = selectedTemplate != null

    LaunchedEffect(templates) {
        if (templates.isEmpty()) {
            selectedTemplateId = null
        } else if (selectedTemplateId != null && templates.none { it.templateId == selectedTemplateId }) {
            selectedTemplateId = null
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(TemplateUiTokens.background),
    ) {
        val isTablet = maxWidth >= 900.dp
        val isCompactMobile = !isTablet && maxWidth < 420.dp
        val screenPadding = when {
            isTablet -> 24.dp
            isCompactMobile -> 12.dp
            else -> 16.dp
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(screenPadding),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(if (isCompactMobile) 10.dp else 16.dp),
                ) {
                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "View All Templates",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TemplateUiTokens.ink,
                                fontWeight = FontWeight.Bold,
                            )
                            if (state.step != BoothStep.Dashboard) {
                                OutlinedButton(onClick = actions.openDashboard) {
                                    Text("Home")
                                }
                            }
                        }
                    } else if (state.step != BoothStep.Dashboard) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = actions.openDashboard, modifier = Modifier.fillMaxWidth()) {
                                Text("Home")
                            }
                            if (!isCompactMobile) {
                                Text(
                                    "View All Templates",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TemplateUiTokens.ink,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    TemplateToolbar(
                        stationLabel = state.stationIp.ifBlank { "Station belum terhubung" },
                        totalTemplates = templates.size,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        sortOption = sortOption,
                        onSortChange = { sortOption = it },
                        isTablet = isTablet,
                        compact = isCompactMobile,
                    )

                    if (templates.isEmpty()) {
                        TemplateEmptyState("Belum ada template yang cocok untuk ditampilkan.")
                    } else {
                        TemplateCatalogGrid(
                            templates = templates,
                            selectedTemplateId = selectedTemplateId,
                            onSelect = { selectedTemplateId = it },
                            isTablet = isTablet,
                            compact = isCompactMobile,
                            modifier = Modifier.weight(1f, fill = true),
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = isDetailVisible,
                    enter = fadeIn() + slideInHorizontally { it / 3 },
                    exit = fadeOut() + slideOutHorizontally { it / 3 },
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x660F172A))
                                .clickable { selectedTemplateId = null },
                        )
                        selectedTemplate?.let {
                            TemplateDetailPanel(
                                template = it,
                                onClose = { selectedTemplateId = null },
                                modifier = Modifier
                                    .align(if (isTablet) Alignment.CenterEnd else Alignment.BottomCenter)
                                    .widthIn(max = if (isTablet) 420.dp else 520.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateToolbar(
    stationLabel: String,
    totalTemplates: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortOption: TemplateSortOption,
    onSortChange: (TemplateSortOption) -> Unit,
    isTablet: Boolean,
    compact: Boolean,
) {
    var showSortMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (isTablet) 30.dp else 26.dp),
        color = TemplateUiTokens.glass,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEFF3FF),
                            Color(0xFFFFF2F8),
                            Color(0xFFF8FBFF),
                        ),
                    ),
                )
                .border(1.dp, TemplateUiTokens.border, RoundedCornerShape(if (isTablet) 30.dp else 26.dp))
                .padding(
                    horizontal = if (isTablet) 20.dp else 16.dp,
                    vertical = if (compact) 12.dp else if (isTablet) 20.dp else 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp),
        ) {
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Template Library",
                        style = MaterialTheme.typography.titleMedium,
                        color = TemplateUiTokens.ink,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Cari dan buka detail template lokal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TemplateUiTokens.inkSoft,
                    )
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
                            "View All Templates",
                            style = if (isTablet) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                            color = TemplateUiTokens.ink,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Cari, urutkan, dan buka detail template yang sudah terimport dari station.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TemplateUiTokens.inkSoft,
                        )
                    }
                    StatusPill(label = "Library", accent = TemplateUiTokens.primary)
                }
            }

            if (isTablet) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    HeroInfoCard(
                        label = "Station",
                        value = stationLabel,
                        accent = TemplateUiTokens.primary,
                        modifier = Modifier.weight(1f),
                    )
                    HeroInfoCard(
                        label = "Imported",
                        value = totalTemplates.toString(),
                        accent = TemplateUiTokens.pink,
                        modifier = Modifier.weight(1f),
                    )
                    HeroInfoCard(
                        label = "Sort",
                        value = sortOption.label,
                        accent = TemplateUiTokens.purple,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else if (!compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    HeroInfoCard(
                        label = "Station",
                        value = stationLabel,
                        accent = TemplateUiTokens.primary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        HeroInfoCard(
                            label = "Imported",
                            value = totalTemplates.toString(),
                            accent = TemplateUiTokens.pink,
                            modifier = Modifier.weight(1f),
                        )
                        HeroInfoCard(
                            label = "Sort",
                            value = sortOption.label,
                            accent = TemplateUiTokens.purple,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text("Search template") },
                        placeholder = { Text("Nama, kode, kategori") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Box {
                        OutlinedButton(onClick = { showSortMenu = true }, modifier = Modifier.height(56.dp)) {
                            Text(sortOption.label)
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            TemplateSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        onSortChange(option)
                                        showSortMenu = false
                                    },
                                )
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text(if (compact) "Search" else "Search template") },
                        placeholder = { Text("Nama, kode, kategori") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!compact) {
                        Box {
                            OutlinedButton(onClick = { showSortMenu = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Sort: ${sortOption.label}")
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                TemplateSortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        onClick = {
                                            onSortChange(option)
                                            showSortMenu = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroInfoCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.84f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TemplateUiTokens.inkSoft)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TemplateCatalogGrid(
    templates: List<TemplateListItem>,
    selectedTemplateId: String?,
    onSelect: (String) -> Unit,
    isTablet: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val minGalleryHeight = when {
        isTablet -> 520.dp
        compact -> 420.dp
        else -> 460.dp
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        color = TemplateUiTokens.glass,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(min = minGalleryHeight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5F8FF),
                            TemplateUiTokens.primary.copy(alpha = 0.04f),
                            Color.White,
                            Color(0xFFFFF5FA),
                            Color.White,
                        ),
                    ),
                )
                .border(1.dp, TemplateUiTokens.border, RoundedCornerShape(28.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Template Gallery",
                            style = MaterialTheme.typography.titleLarge,
                            color = TemplateUiTokens.ink,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Pilih kartu untuk membuka overlay detail.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TemplateUiTokens.inkSoft,
                        )
                    }
                    StatusPill(
                        label = "Gallery",
                        accent = TemplateUiTokens.primary,
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Template Gallery",
                        style = MaterialTheme.typography.titleMedium,
                        color = TemplateUiTokens.ink,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Pilih item untuk membuka detail.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TemplateUiTokens.inkSoft,
                    )
                    StatusPill(
                        label = "List",
                        accent = TemplateUiTokens.primary,
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(
                    when {
                        isTablet -> 4
                        compact -> 1
                        else -> 2
                    },
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
            ) {
                items(
                    items = templates,
                    key = { it.templateId },
                    contentType = { "template_tile" },
                ) { template ->
                    TemplateCatalogTile(
                        template = template,
                        selected = template.templateId == selectedTemplateId,
                        compact = compact,
                        onClick = { onSelect(template.templateId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCatalogTile(
    template: TemplateListItem,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
) {
    BoxWithConstraints {
        val tileCompact = compact || maxWidth < 220.dp
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFF3F5FF) else Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 10.dp else 5.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (tileCompact) 8.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (tileCompact) 8.dp else 10.dp),
        ) {
            TemplateCatalogThumbnail(
                template = template,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (tileCompact) 168.dp else 190.dp),
            )
            if (tileCompact) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        template.templateName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TemplateUiTokens.ink,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (selected) {
                        StatusPill(label = "Open", accent = TemplateUiTokens.primary)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        template.templateName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = TemplateUiTokens.ink,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (selected) {
                        StatusPill(label = "Open", accent = TemplateUiTokens.primary)
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun TemplateCatalogThumbnail(
    template: TemplateListItem,
    modifier: Modifier = Modifier,
) {
    val thumbnailModel = rememberThumbnailModel(template.thumbnailUrl)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TemplateUiTokens.primary.copy(alpha = 0.20f),
                        TemplateUiTokens.purple.copy(alpha = 0.20f),
                        TemplateUiTokens.pink.copy(alpha = 0.12f),
                        Color.White,
                    ),
                ),
            ),
    ) {
        if (thumbnailModel != null) {
            AsyncImage(
                model = thumbnailModel,
                contentDescription = "Thumbnail ${template.templateName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("No", color = TemplateUiTokens.primary, fontWeight = FontWeight.Bold)
                Text("Preview", style = MaterialTheme.typography.labelSmall, color = TemplateUiTokens.inkSoft)
            }
        }
    }
}

@Composable
private fun TemplateDetailPanel(
    template: TemplateListItem,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 520.dp
        Surface(
            modifier = Modifier
                .then(if (compact) Modifier.fillMaxWidth() else Modifier.widthIn(min = 320.dp))
                .padding(if (compact) 10.dp else 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = TemplateUiTokens.glass,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF8FBFF),
                                TemplateUiTokens.primary.copy(alpha = 0.04f),
                                TemplateUiTokens.pink.copy(alpha = 0.05f),
                                Color.White,
                            ),
                        ),
                    )
                    .border(1.dp, TemplateUiTokens.border, RoundedCornerShape(28.dp))
                    .padding(if (compact) 14.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Template Detail",
                                style = MaterialTheme.typography.titleLarge,
                                color = TemplateUiTokens.ink,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Informasi lengkap template terpilih.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TemplateUiTokens.inkSoft,
                            )
                        }
                        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                            Text("Tutup")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Template Detail",
                                style = MaterialTheme.typography.titleLarge,
                                color = TemplateUiTokens.ink,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Informasi lengkap template terpilih.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TemplateUiTokens.inkSoft,
                            )
                        }
                        OutlinedButton(onClick = onClose) {
                            Text("Tutup")
                        }
                    }
                }
                Text(
                    template.templateName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TemplateUiTokens.ink,
                    fontWeight = FontWeight.Bold,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    TemplateUiTokens.primary.copy(alpha = 0.07f),
                                    TemplateUiTokens.pink.copy(alpha = 0.05f),
                                    Color.White,
                                ),
                            ),
                        )
                        .padding(12.dp),
                ) {
                    TemplateThumbnail(template = template)
                }
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        CompactDetailValue(
                            label = "Code",
                            value = template.templateCode,
                            accent = TemplateUiTokens.primary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        CompactDetailValue(
                            label = "Category",
                            value = template.category ?: "-",
                            accent = TemplateUiTokens.purple,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        CompactDetailValue(
                            label = "Paper",
                            value = template.paperSize ?: "-",
                            accent = TemplateUiTokens.primary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        CompactDetailValue(
                            label = "Slots",
                            value = template.slotCount.toString(),
                            accent = TemplateUiTokens.pink,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CompactDetailValue(
                            label = "Code",
                            value = template.templateCode,
                            accent = TemplateUiTokens.primary,
                            modifier = Modifier.weight(1f),
                        )
                        CompactDetailValue(
                            label = "Category",
                            value = template.category ?: "-",
                            accent = TemplateUiTokens.purple,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CompactDetailValue(
                            label = "Paper",
                            value = template.paperSize ?: "-",
                            accent = TemplateUiTokens.primary,
                            modifier = Modifier.weight(1f),
                        )
                        CompactDetailValue(
                            label = "Slots",
                            value = template.slotCount.toString(),
                            accent = TemplateUiTokens.pink,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                DetailValue(
                    label = "Admin Note",
                    value = "Template ini sudah tersimpan lokal dan siap dicek operator sebelum dipakai di session picker.",
                )
            }
        }
    }
}

@Composable
private fun DetailValue(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.82f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TemplateUiTokens.inkSoft)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = TemplateUiTokens.ink, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CompactDetailValue(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.10f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TemplateUiTokens.inkSoft)
            Text(value, style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TemplateEmptyState(message: String) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = TemplateUiTokens.glass,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(TemplateUiTokens.primary.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("T", color = TemplateUiTokens.primary, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "Template belum siap",
                style = MaterialTheme.typography.titleMedium,
                color = TemplateUiTokens.ink,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TemplateUiTokens.inkSoft,
            )
        }
    }
}

@Composable
private fun TemplateThumbnail(template: TemplateListItem) {
    val thumbnailModel = rememberThumbnailModel(template.thumbnailUrl)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TemplateUiTokens.purple.copy(alpha = 0.30f),
                        TemplateUiTokens.pink.copy(alpha = 0.18f),
                        Color.White,
                    ),
                ),
            ),
    ) {
        if (thumbnailModel != null) {
            AsyncImage(
                model = thumbnailModel,
                contentDescription = "Thumbnail ${template.templateName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    "Preview belum tersedia",
                    style = MaterialTheme.typography.titleSmall,
                    color = TemplateUiTokens.ink,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Template tetap bisa tampil setelah asset selesai sync.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TemplateUiTokens.inkSoft,
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color.White.copy(alpha = 0.82f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
        ) {
            Text(
                text = if (template.thumbnailReady) "Thumbnail ready" else "Sync preview",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelMedium,
                color = TemplateUiTokens.ink,
            )
        }
    }
}

@Composable
private fun rememberThumbnailModel(thumbnailUrl: String?): Any? {
    return remember(thumbnailUrl) {
        thumbnailUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { path ->
                if (path.startsWith("/") || path.startsWith("file:/")) {
                    File(path.removePrefix("file://"))
                } else {
                    path
                }
            }
    }
}

@Composable
private fun StatusPill(label: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.14f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(name = "View All Templates Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun ViewAllTemplatesTabletPreview() {
    DafydioBoothTheme {
        ViewAllTemplatesScreen(
            state = PreviewStateProvider.templateBase.copy(
                step = BoothStep.TemplatePicker,
                stationIp = "10.10.116.4:8000",
                availableTemplateItems = templatePreviewMockItems,
            ),
            actions = BoothActions(),
        )
    }
}

@Preview(name = "View All Templates Mobile", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun ViewAllTemplatesMobilePreview() {
    DafydioBoothTheme {
        ViewAllTemplatesScreen(
            state = PreviewStateProvider.templateBase.copy(
                step = BoothStep.TemplatePicker,
                stationIp = "10.10.116.4:8000",
                availableTemplateItems = templatePreviewMockItems,
            ),
            actions = BoothActions(),
        )
    }
}

