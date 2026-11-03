package com.cellstrap.app.data.models

data class AppSettings(
    val robloxInstalled: Boolean = false,
    val robloxPackageName: String = "com.roblox.client",
    val activeProfileId: String? = null,
    val autoInjectOnLaunch: Boolean = true,
    val showFlagWarning: Boolean = true,
    val appTheme: AppTheme = AppTheme.DARK_GREEN,
    val notificationsEnabled: Boolean = true,
    val keepFlagsAfterUpdate: Boolean = true
)

enum class AppTheme(val displayName: String) {
    DARK_GREEN("Dark Green (Default)"),
    PURE_BLACK("Pure Black"),
    DARK_GRAY("Dark Gray")
}

data class RobloxStatus(
    val isInstalled: Boolean,
    val versionName: String?,
    val packageName: String,
    val hasStorageAccess: Boolean
)
