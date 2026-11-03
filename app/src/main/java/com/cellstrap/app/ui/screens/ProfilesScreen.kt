package com.cellstrap.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cellstrap.app.data.models.FFlagProfile
import com.cellstrap.app.ui.CellstrapViewModel
import com.cellstrap.app.ui.components.*
import com.cellstrap.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfilesScreen(viewModel: CellstrapViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CellBlack)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Profiles", style = MaterialTheme.typography.headlineSmall, color = CellTextPrimary)
                Text("${state.profiles.size} profiles", fontSize = 11.sp, color = CellTextMuted)
            }
            GlowButton(
                text = "New Profile",
                onClick = { showCreateDialog = true },
                icon = Icons.Default.Add,
                variant = ButtonVariant.OUTLINE
            )
        }

        if (state.profiles.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📁", fontSize = 48.sp)
                    Text("No Profiles", fontWeight = FontWeight.Bold, color = CellTextSecondary, fontSize = 16.sp)
                    Text(
                        "Create a profile to start managing\nyour FFlag configurations",
                        fontSize = 13.sp,
                        color = CellTextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GlowButton(
                        "Create First Profile",
                        onClick = { showCreateDialog = true },
                        icon = Icons.Default.Add
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.profiles) { profile ->
                    ProfileCard(
                        profile = profile,
                        isActive = profile.id == state.activeProfileId,
                        onActivate = { viewModel.setActiveProfile(profile.id) },
                        onDeactivate = { viewModel.setActiveProfile(null) },
                        onDelete = { confirmDeleteId = profile.id },
                        onInject = {
                            viewModel.setActiveProfile(profile.id)
                            viewModel.injectActiveProfile()
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }

    if (showCreateDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createProfile(name)
                showCreateDialog = false
            }
        )
    }

    confirmDeleteId?.let { id ->
        val profile = state.profiles.find { it.id == id }
        if (profile != null) {
            AlertDialog(
                onDismissRequest = { confirmDeleteId = null },
                containerColor = CellCard,
                title = { Text("Delete Profile", color = CellTextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("Delete \"${profile.name}\"? This cannot be undone.", color = CellTextSecondary, fontSize = 13.sp) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteProfile(id)
                        confirmDeleteId = null
                    }) {
                        Text("Delete", color = CellError, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDeleteId = null }) {
                        Text("Cancel", color = CellTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileCard(
    profile: FFlagProfile,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onDelete: () -> Unit,
    onInject: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val createdDate = dateFormat.format(Date(profile.createdAt))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CellCard)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) CellGreen.copy(alpha = 0.6f) else CellCardBorder,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (isActive) CellGreen else CellTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(profile.name, fontWeight = FontWeight.SemiBold, color = CellTextPrimary, fontSize = 15.sp)
                        Text("Created $createdDate", fontSize = 10.sp, color = CellTextMuted)
                    }
                }
                if (isActive) {
                    StatusBadge("ACTIVE", CellGreen)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            CellDividerLine()
            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${profile.flags.size}",
                        fontWeight = FontWeight.Bold,
                        color = CellGreenGlow,
                        fontSize = 18.sp
                    )
                    Text("Flags", fontSize = 10.sp, color = CellTextMuted)
                }
            }

            // Preview of flags
            if (profile.flags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                profile.flags.entries.take(3).forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            key,
                            fontSize = 10.sp,
                            color = CellTextMuted,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(value, fontSize = 10.sp, color = CellTextSecondary)
                    }
                }
                if (profile.flags.size > 3) {
                    Text("+ ${profile.flags.size - 3} more...", fontSize = 10.sp, color = CellTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isActive) {
                    GlowButton(
                        "Deactivate",
                        onClick = onDeactivate,
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.SECONDARY,
                        icon = Icons.Default.FolderOpen
                    )
                    GlowButton(
                        "Inject",
                        onClick = onInject,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FlashOn
                    )
                } else {
                    GlowButton(
                        "Set Active",
                        onClick = onActivate,
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.OUTLINE,
                        icon = Icons.Default.CheckCircle
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CellError.copy(alpha = 0.1f))
                        .border(1.dp, CellError.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CellError, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CreateProfileDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

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
                Text("New Profile", fontWeight = FontWeight.Bold, color = CellTextPrimary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Create a new FFlag profile to organize your configurations.", fontSize = 12.sp, color = CellTextMuted)
                Spacer(modifier = Modifier.height(16.dp))
                CellTextField("Profile Name", name) { name = it }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlowButton("Cancel", onClick = onDismiss, modifier = Modifier.weight(1f), variant = ButtonVariant.SECONDARY)
                    GlowButton(
                        "Create",
                        onClick = { if (name.isNotBlank()) onCreate(name.trim()) },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank(),
                        icon = Icons.Default.Add
                    )
                }
            }
        }
    }
}
