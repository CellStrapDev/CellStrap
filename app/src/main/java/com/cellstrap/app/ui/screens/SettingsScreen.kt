package com.cellstrap.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cellstrap.app.ui.CellstrapViewModel
import com.cellstrap.app.ui.components.*
import com.cellstrap.app.ui.theme.*

@Composable
fun SettingsScreen(viewModel: CellstrapViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CellBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text("Settings", style = MaterialTheme.typography.headlineSmall, color = CellTextPrimary)
        Text("Configure Cellstrap behaviour", fontSize = 11.sp, color = CellTextMuted)
        Spacer(modifier = Modifier.height(20.dp))

        // Permissions section
        SectionHeader("Permissions")
        Spacer(modifier = Modifier.height(10.dp))

        CellCard {
            SettingRow(
                icon = Icons.Default.Folder,
                title = "All Files Access",
                subtitle = if (state.hasStoragePermission) "Granted — Cellstrap can write FFlags" else "Required to inject FFlags to Roblox",
                trailing = {
                    if (state.hasStoragePermission) {
                        StatusBadge("GRANTED", CellGreen)
                    } else {
                        GlowButton(
                            "Grant",
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                    context.startActivity(intent)
                                }
                            },
                            variant = ButtonVariant.OUTLINE
                        )
                    }
                }
            )

            CellDividerLine(modifier = Modifier.padding(vertical = 10.dp))

            SettingRow(
                icon = Icons.Default.SportsEsports,
                title = "Roblox Detected",
                subtitle = if (state.robloxStatus.isInstalled)
                    "com.roblox.client (v${state.robloxStatus.versionName})"
                else
                    "Roblox is not installed",
                trailing = {
                    StatusBadge(
                        if (state.robloxStatus.isInstalled) "FOUND" else "MISSING",
                        if (state.robloxStatus.isInstalled) CellGreen else CellError
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader("Injection")
        Spacer(modifier = Modifier.height(10.dp))

        CellCard {
            SettingRow(
                icon = Icons.Default.FlashOn,
                title = "Inject on Launch",
                subtitle = "Automatically inject active profile when launching Roblox",
                trailing = {
                    // Would be a toggle in a full implementation
                    StatusBadge("ON", CellGreen)
                }
            )
            CellDividerLine(modifier = Modifier.padding(vertical = 10.dp))
            SettingRow(
                icon = Icons.Default.Shield,
                title = "Safe Mode",
                subtitle = "Validate flags before injecting",
                trailing = {
                    StatusBadge("ON", CellGreen)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader("Actions")
        Spacer(modifier = Modifier.height(10.dp))

        CellCard {
            GlowButton(
                "Refresh Roblox Status",
                onClick = { viewModel.refreshRobloxStatus() },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Refresh,
                variant = ButtonVariant.SECONDARY
            )
            Spacer(modifier = Modifier.height(10.dp))
            GlowButton(
                "Clear All Injected Flags",
                onClick = { viewModel.clearAllFlags() },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Delete,
                variant = ButtonVariant.DANGER
            )
            Spacer(modifier = Modifier.height(10.dp))
            GlowButton(
                "Open App Settings",
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Settings,
                variant = ButtonVariant.SECONDARY
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader("About")
        Spacer(modifier = Modifier.height(10.dp))

        CellCard {
            InfoRow("App Version", "1.0.0", CellGreen)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Build", "Release")
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Developer", "Cellstrap Team")
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Based on", "Bloxstrap / Voidstrap concepts")
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Target", "Roblox Android")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // How to use
        SectionHeader("How to Use")
        Spacer(modifier = Modifier.height(10.dp))
        CellCard {
            listOf(
                "1" to "Grant 'All Files Access' permission above",
                "2" to "Create a Profile in the Profiles tab",
                "3" to "Add flags manually or apply a Preset",
                "4" to "Set the profile as Active",
                "5" to "Tap ⚡ Inject from the Home screen",
                "6" to "Launch Roblox — flags are loaded on start"
            ).forEach { (num, step) ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(CellGreenDark.copy(alpha = 0.4f), androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(num, fontSize = 10.sp, color = CellGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(step, fontSize = 12.sp, color = CellTextSecondary)
                }
            }
        }
    }
}

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = CellGreenDim, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, color = CellTextPrimary, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, color = CellTextMuted)
        }
        Spacer(modifier = Modifier.width(8.dp))
        trailing()
    }
}
