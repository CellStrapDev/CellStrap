package com.cellstrap.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cellstrap.app.data.models.FFlag
import com.cellstrap.app.data.models.FFlagCategory
import com.cellstrap.app.data.models.FFlagProfile
import com.cellstrap.app.data.models.FFlagType
import com.cellstrap.app.ui.CellstrapViewModel
import com.cellstrap.app.ui.components.*
import com.cellstrap.app.ui.theme.*

@Composable
fun FFlagsScreen(viewModel: CellstrapViewModel) {
    val state by viewModel.uiState.collectAsState()
    val activeProfile = state.profiles.find { it.id == state.activeProfileId }
    var selectedCategory by remember { mutableStateOf<FFlagCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingFlag by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showProfilePicker by remember { mutableStateOf(false) }

    val knownFlags = viewModel.knownFlags
    val filteredFlags = knownFlags.filter { flag ->
        (selectedCategory == null || flag.category == selectedCategory) &&
        (searchQuery.isEmpty() || flag.key.contains(searchQuery, ignoreCase = true) ||
         flag.description.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CellBlack)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("FFlag Editor", style = MaterialTheme.typography.headlineSmall, color = CellTextPrimary)
                    Text(
                        "Active: ${activeProfile?.name ?: "None"}  •  ${activeProfile?.flags?.size ?: 0} flags",
                        fontSize = 11.sp, color = CellTextMuted
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showProfilePicker = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CellCard)
                            .border(1.dp, CellCardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Profile", tint = CellGreenDim, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CellGreenDark.copy(alpha = 0.3f))
                            .border(1.dp, CellGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Flag", tint = CellGreen, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search flags...", fontSize = 13.sp, color = CellTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CellTextMuted, modifier = Modifier.size(18.dp)) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null, tint = CellTextMuted, modifier = Modifier.size(16.dp)) } }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CellGreen,
                    unfocusedBorderColor = CellCardBorder,
                    focusedTextColor = CellTextPrimary,
                    unfocusedTextColor = CellTextPrimary,
                    cursorColor = CellGreen,
                    focusedContainerColor = CellCard,
                    unfocusedContainerColor = CellCard
                ),
                singleLine = true
            )
        }

        // Category filter
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryChip("All", selectedCategory == null) { selectedCategory = null }
            }
            items(FFlagCategory.values()) { cat ->
                CategoryChip(cat.displayName, selectedCategory == cat) { selectedCategory = cat }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active profile flags section
        if (activeProfile != null && activeProfile.flags.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    SectionHeader(
                        "Profile Flags",
                        subtitle = "Currently in \"${activeProfile.name}\""
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(activeProfile.flags.entries.toList()) { (key, value) ->
                    FlagRow(
                        flagKey = key,
                        flagValue = value,
                        onEdit = { editingFlag = key to value },
                        onDelete = { viewModel.removeFlagFromProfile(activeProfile.id, key) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionHeader("Available Flags", subtitle = "${filteredFlags.size} flags")
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(filteredFlags) { flag ->
                    val inProfile = activeProfile.flags.containsKey(flag.key)
                    KnownFlagRow(
                        flag = flag,
                        isInProfile = inProfile,
                        onAdd = {
                            viewModel.addFlagToProfile(activeProfile.id, flag.key, flag.value)
                        },
                        onRemove = {
                            viewModel.removeFlagFromProfile(activeProfile.id, flag.key)
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (activeProfile == null) {
                    item {
                        NoProfileWarning()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                item {
                    SectionHeader("Available Flags", subtitle = "${filteredFlags.size} flags")
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(filteredFlags) { flag ->
                    val inProfile = activeProfile?.flags?.containsKey(flag.key) == true
                    KnownFlagRow(
                        flag = flag,
                        isInProfile = inProfile,
                        onAdd = if (activeProfile != null) ({
                            viewModel.addFlagToProfile(activeProfile.id, flag.key, flag.value)
                        }) else null,
                        onRemove = if (activeProfile != null && inProfile) ({
                            viewModel.removeFlagFromProfile(activeProfile.id, flag.key)
                        }) else null
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }

    // Dialogs
    if (showAddDialog && activeProfile != null) {
        AddFlagDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { key, value ->
                viewModel.addFlagToProfile(activeProfile.id, key, value)
                showAddDialog = false
            }
        )
    }

    if (editingFlag != null && activeProfile != null) {
        val (key, currentValue) = editingFlag!!
        EditFlagDialog(
            flagKey = key,
            currentValue = currentValue,
            onDismiss = { editingFlag = null },
            onSave = { newValue ->
                viewModel.addFlagToProfile(activeProfile.id, key, newValue)
                editingFlag = null
            }
        )
    }

    if (showProfilePicker) {
        ProfilePickerDialog(
            profiles = state.profiles,
            activeId = state.activeProfileId,
            onSelect = { id ->
                viewModel.setActiveProfile(id)
                showProfilePicker = false
            },
            onDismiss = { showProfilePicker = false }
        )
    }
}

@Composable
fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) CellGreen else CellCard)
            .border(1.dp, if (selected) CellGreen else CellCardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) CellBlack else CellTextSecondary
        )
    }
}

@Composable
fun KnownFlagRow(
    flag: FFlag,
    isInProfile: Boolean,
    onAdd: (() -> Unit)?,
    onRemove: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isInProfile) CellGreenDark.copy(alpha = 0.15f) else CellSurface)
            .border(
                1.dp,
                if (isInProfile) CellGreen.copy(alpha = 0.3f) else CellDivider,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    flag.key,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isInProfile) CellGreenGlow else CellTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isInProfile) {
                    Spacer(modifier = Modifier.width(6.dp))
                    GreenDot(6.dp)
                }
            }
            Text(
                flag.description.ifEmpty { "Default: ${flag.value}" },
                fontSize = 10.sp,
                color = CellTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusBadge(flag.type.name, CellTextMuted)
                StatusBadge(flag.category.displayName, CellTextMuted)
            }
        }
        if (isInProfile && onRemove != null) {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Remove", tint = CellError, modifier = Modifier.size(16.dp))
            }
        } else if (!isInProfile && onAdd != null) {
            IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = CellGreen, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun NoProfileWarning() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CellWarning.copy(alpha = 0.08f))
            .border(1.dp, CellWarning.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = CellWarning, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("No active profile. Create a profile in the Profiles tab to start adding flags.", fontSize = 12.sp, color = CellWarning)
        }
    }
}

@Composable
fun AddFlagDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

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
                Text("Add Custom Flag", fontWeight = FontWeight.Bold, color = CellTextPrimary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                CellTextField("Flag Key (e.g. DFIntTaskSchedulerTargetFps)", key) { key = it }
                Spacer(modifier = Modifier.height(10.dp))
                CellTextField("Value (e.g. 9999 or True)", value) { value = it }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlowButton("Cancel", onClick = onDismiss, modifier = Modifier.weight(1f), variant = ButtonVariant.SECONDARY)
                    GlowButton("Add Flag", onClick = {
                        if (key.isNotBlank() && value.isNotBlank()) onAdd(key.trim(), value.trim())
                    }, modifier = Modifier.weight(1f), enabled = key.isNotBlank() && value.isNotBlank())
                }
            }
        }
    }
}

@Composable
fun EditFlagDialog(flagKey: String, currentValue: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember { mutableStateOf(currentValue) }

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
                Text("Edit Flag", fontWeight = FontWeight.Bold, color = CellTextPrimary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(flagKey, fontSize = 12.sp, color = CellGreenGlow)
                Spacer(modifier = Modifier.height(14.dp))
                CellTextField("Value", value) { value = it }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlowButton("Cancel", onClick = onDismiss, modifier = Modifier.weight(1f), variant = ButtonVariant.SECONDARY)
                    GlowButton("Save", onClick = { if (value.isNotBlank()) onSave(value.trim()) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ProfilePickerDialog(
    profiles: List<FFlagProfile>,
    activeId: String?,
    onSelect: (String) -> Unit,
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
                Text("Select Profile", fontWeight = FontWeight.Bold, color = CellTextPrimary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))
                if (profiles.isEmpty()) {
                    Text("No profiles yet. Create one in Profiles tab.", fontSize = 12.sp, color = CellTextMuted)
                } else {
                    profiles.forEach { profile ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (profile.id == activeId) CellGreenDark.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (profile.id == activeId) CellGreen.copy(alpha = 0.4f) else CellDivider, RoundedCornerShape(10.dp))
                                .clickable { onSelect(profile.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(profile.name, fontWeight = FontWeight.Medium, color = CellTextPrimary, fontSize = 13.sp)
                                Text("${profile.flags.size} flags", fontSize = 11.sp, color = CellTextMuted)
                            }
                            if (profile.id == activeId) {
                                GreenDot()
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                GlowButton("Close", onClick = onDismiss, modifier = Modifier.fillMaxWidth(), variant = ButtonVariant.SECONDARY)
            }
        }
    }
}

@Composable
fun CellTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CellGreen,
            unfocusedBorderColor = CellCardBorder,
            focusedLabelColor = CellGreen,
            unfocusedLabelColor = CellTextMuted,
            focusedTextColor = CellTextPrimary,
            unfocusedTextColor = CellTextPrimary,
            cursorColor = CellGreen,
            focusedContainerColor = CellSurface,
            unfocusedContainerColor = CellSurface
        ),
        singleLine = true
    )
}
