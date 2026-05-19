package com.errymaricha.dafydiobooth.ui.booth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.errymaricha.dafydiobooth.ui.launch.LaunchUiState
import java.io.File

@Composable
fun TemplatePickerScreen(
    state: BoothUiState,
    launchState: LaunchUiState,
    actions: BoothActions,
) {
    ScreenFrame(title = "Pilih Template", state = state, actions = actions) {
        if (state.launchEventName.isNotBlank()) {
            Text("Event: ${state.launchEventName}", fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Kode Session: ${launchState.session?.sessionCode ?: state.session?.sessionCode ?: "-"}",
            fontWeight = FontWeight.Bold,
        )
        val filteredTemplateItems = if (!state.localOnlySession && state.launchAllowedTemplateIds.isNotEmpty()) {
            state.availableTemplateItems.filter { state.launchAllowedTemplateIds.contains(it.templateId) }
        } else {
            state.availableTemplateItems
        }
        if (filteredTemplateItems.isEmpty()) {
            if (state.availableTemplateItems.isEmpty()) {
                Text("Belum ada template lokal. Buka Settings lalu update template dari Photobooth Station.")
            } else {
                Text("Tidak ada template yang diizinkan untuk event ini. Atur di Setting Event.")
            }
        } else {
            filteredTemplateItems.forEach { template ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            if (!template.thumbnailUrl.isNullOrBlank()) {
                                val thumbnailModel: Any = template.thumbnailUrl
                                    ?.takeIf { it.startsWith("/") || it.startsWith("file:/") }
                                    ?.let { File(it.removePrefix("file://")) }
                                    ?: template.thumbnailUrl
                                AsyncImage(
                                    model = thumbnailModel,
                                    contentDescription = "Thumbnail ${template.templateName}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(96.dp, 64.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                                Text(template.templateName, fontWeight = FontWeight.Bold)
                                Text(
                                    "${template.templateCode}  |  Slot ${template.slotCount}  |  ${template.paperSize ?: "-"}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(template.category ?: "-", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        val canSelectTemplate = template.previewReady && template.overlayReady
                        val selectionHint = when {
                            canSelectTemplate -> "Template siap dipakai."
                            !template.previewReady && !template.overlayReady -> "Preview dan overlay template belum siap di device."
                            !template.previewReady -> "Preview template belum siap di device."
                            else -> "Overlay template belum siap di device."
                        }
                        Text(
                            text = selectionHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (canSelectTemplate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        )
                        OutlinedButton(
                            onClick = { actions.selectTemplate(template.templateId) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canSelectTemplate,
                        ) {
                            Text("Pilih Template")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTemplateScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Create Custom Template", state = state, actions = actions) {
        Text("Template custom sementara dibuat lokal. Editor detail akan dihubungkan ke Photobooth Station.")
        Button(onClick = { actions.saveCustomTemplate("Custom Template") }, modifier = Modifier.fillMaxWidth()) {
            Text("Use Custom Template")
        }
    }
}

@Composable
fun CapturePreviewScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Preview Capture", state = state, actions = actions) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    CapturedPhotoSurface(state)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Quick Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = "Slot ${state.nextCaptureIndex}/${state.templateSlotCount}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                Text(
                    text = "Pastikan wajah ada di area aman dan tidak terpotong.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = state.capturedPhotoName ?: "-",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = actions.retakePhoto, enabled = !state.isLoading, modifier = Modifier.weight(1f)) {
                Text("Retake")
            }
            Button(onClick = actions.acceptCapturePreview, enabled = !state.isLoading, modifier = Modifier.weight(1f)) {
                Text("Use Photo")
            }
        }
    }
}

@Composable
fun TemplatePreviewScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Preview Template", state = state, actions = actions) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Canvas Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = state.selectedTemplatePaperSize ?: "-",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                Text("Klik slot di preview untuk memilih area kerja yang aktif.", style = MaterialTheme.typography.bodySmall)
                Text("Drag foto langsung di slot untuk custom letak foto.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        TemplateSurface(state)
        Text("Template: ${state.selectedTemplate ?: "-"}")
        Text("Captured: ${state.capturedPhotosBySlot.size}/${state.templateSlotCount}")
        Button(onClick = actions.finishSession, modifier = Modifier.fillMaxWidth()) {
            Text("Finish")
        }
    }
}

@Composable
fun FinishScreen(state: BoothUiState, actions: BoothActions) {
    ScreenFrame(title = "Finish", state = state, actions = actions) {
        TemplateSurface(state)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = actions.downloadResult, modifier = Modifier.weight(1f)) {
                Text("Download")
            }
            OutlinedButton(
                onClick = actions.triggerMockPrint,
                enabled = !state.isLoading && state.mockPrintStatus != MockPrintStatus.Queued,
                modifier = Modifier.weight(1f),
            ) {
                Text("Print")
            }
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                Text("Share")
            }
        }
        Text(
            text = when (state.mockPrintStatus) {
                MockPrintStatus.Idle -> "Mock print: idle"
                MockPrintStatus.Queued -> "Mock print: queued"
                MockPrintStatus.Sent -> "Mock print: sent"
                MockPrintStatus.Failed -> "Mock print: failed"
            },
        )
        state.mockPrintMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        Text(
            text = if (!state.localOnlySession && state.printUsePhotoboothStation && state.isStationConnected) {
                "Print akan dikirim ke Photobooth Station."
            } else {
                "Print memakai default printing device."
            },
        )
        Button(onClick = actions.newSession, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Dashboard")
        }
    }
}
