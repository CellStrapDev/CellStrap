package com.cellstrap.app.ui

import android.app.Application
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cellstrap.app.data.models.FFlagPreset
import com.cellstrap.app.data.models.FFlagProfile
import com.cellstrap.app.data.models.RobloxStatus
import com.cellstrap.app.data.repository.FFlagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class UiState(
    val profiles: List<FFlagProfile> = emptyList(),
    val activeProfileId: String? = null,
    val currentFlags: Map<String, String> = emptyMap(),
    val robloxStatus: RobloxStatus = RobloxStatus(false, null, "com.roblox.client", false),
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val injectSuccess: Boolean = false,
    val hasStoragePermission: Boolean = false
)

class CellstrapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FFlagRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val presets: List<FFlagPreset> get() = repository.getBuiltInPresets()
    val knownFlags get() = repository.getKnownFlags()

    init {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val profiles = repository.loadProfiles()
            val currentFlags = repository.getCurrentFlags()
            val robloxStatus = repository.getRobloxStatus()
            val hasStorage = checkStoragePermission()
            _uiState.value = _uiState.value.copy(
                profiles = profiles,
                activeProfileId = profiles.firstOrNull { it.isActive }?.id,
                currentFlags = currentFlags,
                robloxStatus = robloxStatus,
                isLoading = false,
                hasStoragePermission = hasStorage
            )
        }
    }

    fun refreshRobloxStatus() {
        viewModelScope.launch {
            val status = repository.getRobloxStatus()
            val hasStorage = checkStoragePermission()
            _uiState.value = _uiState.value.copy(
                robloxStatus = status,
                hasStoragePermission = hasStorage
            )
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    // ---- Profile management ----

    fun createProfile(name: String) {
        viewModelScope.launch {
            val newProfile = FFlagProfile(
                id = UUID.randomUUID().toString(),
                name = name
            )
            val updated = _uiState.value.profiles + newProfile
            repository.saveProfiles(updated)
            _uiState.value = _uiState.value.copy(profiles = updated)
            showMessage("Profile \"$name\" created")
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            val updated = _uiState.value.profiles.filter { it.id != profileId }
            repository.saveProfiles(updated)
            val newActiveId = if (_uiState.value.activeProfileId == profileId) null
            else _uiState.value.activeProfileId
            _uiState.value = _uiState.value.copy(
                profiles = updated,
                activeProfileId = newActiveId
            )
            showMessage("Profile deleted")
        }
    }

    fun setActiveProfile(profileId: String?) {
        viewModelScope.launch {
            val updated = _uiState.value.profiles.map { p ->
                p.copy(isActive = p.id == profileId)
            }
            repository.saveProfiles(updated)
            _uiState.value = _uiState.value.copy(
                profiles = updated,
                activeProfileId = profileId
            )
        }
    }

    fun updateProfileFlags(profileId: String, flags: Map<String, String>) {
        viewModelScope.launch {
            val updated = _uiState.value.profiles.map { p ->
                if (p.id == profileId) p.copy(flags = flags.toMutableMap()) else p
            }
            repository.saveProfiles(updated)
            _uiState.value = _uiState.value.copy(profiles = updated)
        }
    }

    fun addFlagToProfile(profileId: String, key: String, value: String) {
        val profile = _uiState.value.profiles.find { it.id == profileId } ?: return
        val newFlags = profile.flags.toMutableMap().apply { this[key] = value }
        updateProfileFlags(profileId, newFlags)
        showMessage("Flag added: $key")
    }

    fun removeFlagFromProfile(profileId: String, key: String) {
        val profile = _uiState.value.profiles.find { it.id == profileId } ?: return
        val newFlags = profile.flags.toMutableMap().apply { remove(key) }
        updateProfileFlags(profileId, newFlags)
        showMessage("Flag removed")
    }

    // ---- Preset application ----

    fun applyPreset(preset: FFlagPreset, targetProfileId: String? = null) {
        val id = targetProfileId ?: _uiState.value.activeProfileId ?: return
        val profile = _uiState.value.profiles.find { it.id == id } ?: return
        val merged = profile.flags.toMutableMap().apply { putAll(preset.flags) }
        updateProfileFlags(id, merged)
        showMessage("Applied preset: ${preset.name}")
    }

    // ---- Injection ----

    fun injectActiveProfile() {
        viewModelScope.launch {
            val profileId = _uiState.value.activeProfileId
            if (profileId == null) {
                showMessage("No active profile selected")
                return@launch
            }
            val profile = _uiState.value.profiles.find { it.id == profileId }
            if (profile == null) {
                showMessage("Profile not found")
                return@launch
            }
            if (profile.flags.isEmpty()) {
                showMessage("Active profile has no flags to inject")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.injectFlags(profile.flags)
            _uiState.value = _uiState.value.copy(isLoading = false)
            result.onSuccess { path ->
                val currentFlags = repository.getCurrentFlags()
                _uiState.value = _uiState.value.copy(
                    currentFlags = currentFlags,
                    injectSuccess = true
                )
                showMessage("✓ Flags injected successfully! (${profile.flags.size} flags)")
            }.onFailure { e ->
                showMessage("✗ Inject failed: ${e.message}")
            }
        }
    }

    fun injectCustomFlags(flags: Map<String, String>) {
        viewModelScope.launch {
            if (flags.isEmpty()) {
                showMessage("No flags to inject")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.injectFlags(flags)
            _uiState.value = _uiState.value.copy(isLoading = false)
            result.onSuccess {
                val currentFlags = repository.getCurrentFlags()
                _uiState.value = _uiState.value.copy(
                    currentFlags = currentFlags,
                    injectSuccess = true
                )
                showMessage("✓ ${flags.size} flags injected!")
            }.onFailure { e ->
                showMessage("✗ Inject failed: ${e.message}")
            }
        }
    }

    fun clearAllFlags() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.clearFlags()
            _uiState.value = _uiState.value.copy(
                currentFlags = emptyMap(),
                isLoading = false
            )
            showMessage("All flags cleared")
        }
    }

    fun launchRoblox() {
        if (_uiState.value.robloxStatus.isInstalled) {
            repository.launchRoblox()
        } else {
            showMessage("Roblox is not installed")
        }
    }

    fun dismissSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null, injectSuccess = false)
    }

    private fun showMessage(msg: String) {
        _uiState.value = _uiState.value.copy(snackbarMessage = msg)
    }
}
