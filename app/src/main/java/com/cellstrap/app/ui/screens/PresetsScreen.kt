package com.cellstrap.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cellstrap.app.data.models.FFlagPreset
import com.cellstrap.app.data.models.FFlagProfile
import com.cellstrap.app.data.models.PresetCategory
import com.cellstrap.app.ui.CellstrapViewModel
import com.cellstrap.app.ui.components.*
import com.cellstrap.app.ui.theme.*

@Composable
fun PresetsScreen(viewModel: CellstrapViewModel) {
    val state by viewModel.uiState.collectAsState()
    val presets = viewModel.presets
    var selectedCategory by remember { mutableStateOf<PresetCategory?>(null) }
    var previewPreset by remember { mutableStateOf<FFlagPreset?>(null) }

    val filteredPresets = if (selectedCategory == null) presets
    else presets.filter { it.category == selectedCategory }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CellBlack)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Presets", style = MaterialTheme.typography.headlineSmall, color = CellTextPrimary)
            Text(
                "Curated FFlag configurations • Apply to active profile",
                fontSize = 11.sp, color = CellTextMuted
            )
        }

        // Category filter
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { CategoryChip("All", selectedCategory == null) { selectedCategory = null } }
            items(PresetCategory.values()) { cat ->
                CategoryChip(cat.displayName, selectedCategory == cat) { selectedCategory = cat }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredPresets) { preset ->
                PresetCard(
                    preset = preset,
                    activeProfile = state.profiles.find { it.id == state.activeProfileId },
                    onApply = {
                        if (state.activeProfileId != null) {
                            viewModel.applyPreset(preset)
                        }
                    },
                    onPreview = { previewPreset = preset }
                )
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    previewPreset?.let { preset ->
        PresetPreviewDialog(
            preset = preset,
            hasActiveProfile = state.activeProfileId != null,
            onApply = {
                viewModel.applyPreset(preset)
                previewPreset = null
            },
            onDismiss = { previewPreset = null }
        )
    }
}

@Composable
fun PresetCard(
    preset: FFlagPreset,
    activeProfile: FFlagProfile?,
    onApply: () -> Unit,
    onPreview: () -> Unit
) {
    val categoryColor = when (preset.category) {
        PresetCategory.PERFORMANCE -> CellGreen
        PresetCategory.GRAPHICS -> Color(0xFF5B8FFF)
        PresetCategory.NETWORK -> Color(0xFFFFAA00)
        PresetCategory.BALANCED -> CellGreenDim
        PresetCategory.EXTREME -> CellError
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CellCard)
            .border(1.dp, categoryColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable(onClick = onPreview)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(preset.icon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(preset.name, fontWeight = FontWeight.SemiBold, color = CellTextPrimary, fontSize = 14.sp)
                        Text("${preset.flags.size} flags", fontSize = 11.sp, color = CellTextMuted)
                    }
                }
                StatusBadge(preset.category.displayName, categoryColor)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(preset.description, fontSize = 12.sp, color = CellTextSecondary, lineHeight = 18.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlowButton(
                    "Preview",
                    onClick = onPreview,
                    modifier = Modifier.weight(1f),
                    variant = ButtonVariant.OUTLINE,
                    icon = Icons.Default.Visibility
                )
                GlowButton(
                    if (activeProfile != null) "Apply" else "No Profile",
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                    enabled = activeProfile != null,
                    icon = Icons.Default.FlashOn
                )
            }
        }
    }
}

@Composable
fun PresetPreviewDialog(
    preset: FFlagPreset,
    hasActiveProfile: Boolean,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CellCard)
                .border(1.dp, CellCardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(preset.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(preset.name, fontWeight = FontWeight.Bold, color = CellTextPrimary, fontSize = 16.sp)
                        Text(preset.category.displayName, fontSize = 11.sp, color = CellTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(preset.description, fontSize = 12.sp, color = CellTextSecondary)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Flags in this preset:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CellTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CellSurface)
                        .border(1.dp, CellDivider, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        preset.flags.forEach { (key, value) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(key, fontSize = 10.sp, color = CellGreenGlow, modifier = Modifier.weight(1f), maxLines = 1)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(value, fontSize = 10.sp, color = CellTextSecondary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                if (!hasActiveProfile) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CellWarning.copy(alpha = 0.1f))
                            .padding(10.dp)
                    ) {
                        Text("Create a profile first to apply presets.", fontSize = 11.sp, color = CellWarning)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlowButton("Close", onClick = onDismiss, modifier = Modifier.weight(1f), variant = ButtonVariant.SECONDARY)
                    GlowButton(
                        "Apply Preset",
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                        enabled = hasActiveProfile,
                        icon = Icons.Default.FlashOn
                    )
                }
            }
        }
    }
}
