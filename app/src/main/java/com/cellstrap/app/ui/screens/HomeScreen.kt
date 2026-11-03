package com.cellstrap.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cellstrap.app.ui.CellstrapViewModel
import com.cellstrap.app.ui.components.*
import com.cellstrap.app.ui.theme.*

@Composable
fun HomeScreen(viewModel: CellstrapViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CellBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Hero banner
        HeroBanner()

        Spacer(modifier = Modifier.height(20.dp))

        // Roblox status card
        RobloxStatusCard(state = state, viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Active profile card
        ActiveProfileCard(state = state, viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Quick actions
        SectionHeader("Quick Actions")
        Spacer(modifier = Modifier.height(10.dp))
        QuickActionsGrid(state = state, viewModel = viewModel)

        Spacer(modifier = Modifier.height(20.dp))

        // Current flags summary
        if (state.currentFlags.isNotEmpty()) {
            SectionHeader(
                title = "Active Flags",
                subtitle = "${state.currentFlags.size} flags currently injected"
            )
            Spacer(modifier = Modifier.height(10.dp))
            ActiveFlagsSummary(flags = state.currentFlags)
        }
    }
}

@Composable
fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CellGreenDark.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
            .border(1.dp, CellGreenDark, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CELLSTRAP",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CellGreen,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "Roblox Android Bootstrapper",
                    fontSize = 12.sp,
                    color = CellTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "v1.0.0",
                    fontSize = 10.sp,
                    color = CellTextMuted
                )
            }
            // Logo placeholder (glowing square)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CellGreenDark.copy(alpha = 0.4f))
                    .border(2.dp, CellGreen.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(CellBlack)
                        .border(1.dp, CellGreenDark, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun RobloxStatusCard(state: com.cellstrap.app.ui.UiState, viewModel: CellstrapViewModel) {
    CellCard(borderGlow = state.robloxStatus.isInstalled) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = if (state.robloxStatus.isInstalled) CellGreen else CellTextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Roblox",
                        fontWeight = FontWeight.SemiBold,
                        color = CellTextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        if (state.robloxStatus.isInstalled)
                            "v${state.robloxStatus.versionName ?: "Unknown"}"
                        else "Not installed",
                        fontSize = 11.sp,
                        color = CellTextMuted
                    )
                }
            }
            StatusBadge(
                text = if (state.robloxStatus.isInstalled) "DETECTED" else "MISSING",
                color = if (state.robloxStatus.isInstalled) CellGreen else CellError
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        CellDividerLine()
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoRow(
                label = "Storage",
                value = if (state.hasStoragePermission) "Granted" else "Required",
                valueColor = if (state.hasStoragePermission) CellGreen else CellWarning
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(
            label = "Flags Injected",
            value = "${state.currentFlags.size}",
            valueColor = if (state.currentFlags.isNotEmpty()) CellGreen else CellTextMuted
        )
    }
}

@Composable
fun ActiveProfileCard(state: com.cellstrap.app.ui.UiState, viewModel: CellstrapViewModel) {
    val activeProfile = state.profiles.find { it.id == state.activeProfileId }

    CellCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = CellGreenDim,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Active Profile",
                    fontWeight = FontWeight.SemiBold,
                    color = CellTextPrimary,
                    fontSize = 13.sp
                )
            }
            if (activeProfile != null) {
                StatusBadge(text = "ACTIVE", color = CellGreen)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = activeProfile?.name ?: "No profile selected",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (activeProfile != null) CellGreenGlow else CellTextMuted
        )
        if (activeProfile != null) {
            Text(
                text = "${activeProfile.flags.size} flags configured",
                fontSize = 11.sp,
                color = CellTextMuted
            )
        }
    }
}

@Composable
fun QuickActionsGrid(state: com.cellstrap.app.ui.UiState, viewModel: CellstrapViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Inject button - big and prominent
        GlowButton(
            text = if (state.isLoading) "Injecting..." else "⚡ Inject Active Profile",
            onClick = { viewModel.injectActiveProfile() },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.activeProfileId != null && !state.isLoading && state.hasStoragePermission,
            isLoading = state.isLoading,
            variant = ButtonVariant.PRIMARY
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlowButton(
                text = "Launch Roblox",
                onClick = { viewModel.launchRoblox() },
                modifier = Modifier.weight(1f),
                enabled = state.robloxStatus.isInstalled,
                icon = Icons.Default.PlayArrow,
                variant = ButtonVariant.OUTLINE
            )
            GlowButton(
                text = "Clear Flags",
                onClick = { viewModel.clearAllFlags() },
                modifier = Modifier.weight(1f),
                enabled = state.currentFlags.isNotEmpty(),
                icon = Icons.Default.Delete,
                variant = ButtonVariant.SECONDARY
            )
        }

        if (!state.hasStoragePermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CellWarning.copy(alpha = 0.1f))
                    .border(1.dp, CellWarning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CellWarning, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Storage permission required to inject flags. Grant 'All Files Access' in App Settings.",
                        fontSize = 11.sp,
                        color = CellWarning
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveFlagsSummary(flags: Map<String, String>) {
    CellCard {
        flags.entries.take(5).forEach { (key, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    key,
                    fontSize = 11.sp,
                    color = CellGreenGlow,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(
                    value,
                    fontSize = 11.sp,
                    color = CellTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (flags.size > 5) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "+ ${flags.size - 5} more flags",
                fontSize = 10.sp,
                color = CellTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
