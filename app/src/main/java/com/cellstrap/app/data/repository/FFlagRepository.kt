package com.cellstrap.app.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import com.cellstrap.app.data.models.FFlag
import com.cellstrap.app.data.models.FFlagCategory
import com.cellstrap.app.data.models.FFlagPreset
import com.cellstrap.app.data.models.FFlagProfile
import com.cellstrap.app.data.models.FFlagType
import com.cellstrap.app.data.models.PresetCategory
import com.cellstrap.app.data.models.RobloxStatus
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class FFlagRepository(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val TAG = "FFlagRepository"

    // Roblox package name
    private val ROBLOX_PACKAGE = "com.roblox.client"

    // Paths where Roblox stores its app data on Android
    // These are the standard paths Roblox uses for ClientAppSettings.json
    private val robloxDataPaths = listOf(
        "/sdcard/Android/data/com.roblox.client/files/ClientSettings/ClientAppSettings.json",
        "/storage/emulated/0/Android/data/com.roblox.client/files/ClientSettings/ClientAppSettings.json",
        "${Environment.getExternalStorageDirectory()}/Android/data/com.roblox.client/files/ClientSettings/ClientAppSettings.json"
    )

    private val profilesFile: File
        get() = File(context.filesDir, "fflag_profiles.json")

    // ---- Profiles ----

    suspend fun loadProfiles(): List<FFlagProfile> = withContext(Dispatchers.IO) {
        try {
            if (!profilesFile.exists()) return@withContext emptyList()
            val json = profilesFile.readText()
            val type = object : TypeToken<List<FFlagProfile>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load profiles", e)
            emptyList()
        }
    }

    suspend fun saveProfiles(profiles: List<FFlagProfile>) = withContext(Dispatchers.IO) {
        try {
            profilesFile.parentFile?.mkdirs()
            profilesFile.writeText(gson.toJson(profiles))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save profiles", e)
        }
    }

    // ---- FFlag injection ----

    /**
     * Finds the active Roblox ClientSettings directory.
     * On unrooted Android, this requires the app to have been granted access to
     * Android/data via the MANAGE_EXTERNAL_STORAGE permission (Android 11+)
     * or through DocumentsUI/SAF.
     */
    suspend fun findRobloxClientSettingsPath(): File? = withContext(Dispatchers.IO) {
        for (path in robloxDataPaths) {
            val f = File(path)
            if (f.parentFile?.exists() == true || f.exists()) {
                return@withContext f.parentFile
            }
        }
        // Try to create the directory if Roblox is installed
        val dir = File("/sdcard/Android/data/com.roblox.client/files/ClientSettings")
        if (isRobloxInstalled()) {
            try {
                dir.mkdirs()
                return@withContext if (dir.exists()) dir else null
            } catch (e: Exception) {
                Log.e(TAG, "Cannot create ClientSettings dir", e)
            }
        }
        null
    }

    suspend fun getCurrentFlags(): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            for (path in robloxDataPaths) {
                val file = File(path)
                if (file.exists()) {
                    val json = file.readText()
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    return@withContext gson.fromJson(json, type) ?: emptyMap()
                }
            }
            emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read current flags", e)
            emptyMap()
        }
    }

    /**
     * Writes FFlags to Roblox's ClientAppSettings.json.
     * Returns a Result indicating success or the error that occurred.
     */
    suspend fun injectFlags(flags: Map<String, String>): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Build the ClientAppSettings.json content
            // Roblox on Android reads from ClientSettings/ClientAppSettings.json
            val flagsJson = gson.toJson(flags)

            var successPath: String? = null
            var lastError: Exception? = null

            for (path in robloxDataPaths) {
                try {
                    val file = File(path)
                    file.parentFile?.mkdirs()
                    file.writeText(flagsJson)
                    if (file.exists() && file.readText() == flagsJson) {
                        successPath = path
                        break
                    }
                } catch (e: Exception) {
                    lastError = e
                    Log.w(TAG, "Cannot write to $path: ${e.message}")
                }
            }

            if (successPath != null) {
                Log.i(TAG, "FFlags written to $successPath")
                Result.success("Flags injected to $successPath")
            } else {
                Result.failure(
                    lastError ?: Exception(
                        "Cannot write to Roblox data directory.\n\n" +
                        "On Android 11+, you need to grant 'All Files Access' permission.\n" +
                        "Go to Settings → Apps → Cellstrap → Permissions → Files and storage → Allow management of all files."
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to inject flags", e)
            Result.failure(e)
        }
    }

    suspend fun clearFlags(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            for (path in robloxDataPaths) {
                val file = File(path)
                if (file.exists()) {
                    file.writeText("{}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---- Roblox detection ----

    fun isRobloxInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(ROBLOX_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getRobloxStatus(): RobloxStatus {
        return try {
            val info = context.packageManager.getPackageInfo(ROBLOX_PACKAGE, 0)
            RobloxStatus(
                isInstalled = true,
                versionName = info.versionName,
                packageName = ROBLOX_PACKAGE,
                hasStorageAccess = hasStorageAccess()
            )
        } catch (e: PackageManager.NameNotFoundException) {
            RobloxStatus(
                isInstalled = false,
                versionName = null,
                packageName = ROBLOX_PACKAGE,
                hasStorageAccess = false
            )
        }
    }

    fun launchRoblox() {
        val intent = context.packageManager.getLaunchIntentForPackage(ROBLOX_PACKAGE)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun hasStorageAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val externalState = Environment.getExternalStorageState()
            externalState == Environment.MEDIA_MOUNTED
        }
    }

    // ---- Built-in presets ----

    fun getBuiltInPresets(): List<FFlagPreset> = listOf(

        FFlagPreset(
            name = "Max FPS (Unlocked)",
            description = "Removes the 60 FPS cap and allows higher frame rates. Best for high-refresh-rate displays.",
            icon = "⚡",
            category = PresetCategory.PERFORMANCE,
            flags = mapOf(
                "DFIntTaskSchedulerTargetFps" to "9999",
                "FFlagGameBasicSettingsFramerateCap5" to "False",
                "DFFlagDebugRenderForceTechnologyVoxel" to "False"
            )
        ),

        FFlagPreset(
            name = "Potato Mode",
            description = "Absolute minimum graphics for maximum performance on low-end devices.",
            icon = "🥔",
            category = PresetCategory.PERFORMANCE,
            flags = mapOf(
                "DFFlagDebugRenderForceTechnologyVoxel" to "True",
                "FIntRenderShadowIntensity" to "0",
                "DFIntCullFactorPixelThresholdShadowMapHighQuality" to "2147483647",
                "DFIntCullFactorPixelThresholdShadowMapLowQuality" to "2147483647",
                "FFlagNewAvatarShadowLogic" to "False",
                "DFIntMaxFrameBufferSize" to "4",
                "FIntRobloxGuiBlurIntensity" to "0",
                "DFIntGrassMovementReducedMotionFactor" to "0",
                "DFIntConnectionMTUSize" to "900"
            )
        ),

        FFlagPreset(
            name = "Low Latency Network",
            description = "Reduces ping and network latency for better responsiveness in competitive games.",
            icon = "📶",
            category = PresetCategory.NETWORK,
            flags = mapOf(
                "DFIntConnectionMTUSize" to "1200",
                "DFIntMaximumFrequencyForMtuDiscovery" to "60",
                "DFIntPhysicsSenderMaxBandwidthBps" to "50000",
                "DFIntPhysicsSenderMaxBandwidthBpsScaling" to "50000",
                "DFIntDataSendRate" to "25"
            )
        ),

        FFlagPreset(
            name = "High Quality Graphics",
            description = "Maxes out visual fidelity. Best for screenshots and powerful devices.",
            icon = "🎨",
            category = PresetCategory.GRAPHICS,
            flags = mapOf(
                "DFFlagDebugRenderForceTechnologyVoxel" to "False",
                "FFlagNewAvatarShadowLogic" to "True",
                "DFFlagTextureCompositorEnabled" to "True",
                "FIntRobloxGuiBlurIntensity" to "24",
                "DFIntGrassMovementReducedMotionFactor" to "100"
            )
        ),

        FFlagPreset(
            name = "Balanced (Recommended)",
            description = "Balanced settings for the best mix of performance and visual quality.",
            icon = "⚖️",
            category = PresetCategory.BALANCED,
            flags = mapOf(
                "DFIntTaskSchedulerTargetFps" to "60",
                "FIntRenderShadowIntensity" to "127",
                "DFIntGrassMovementReducedMotionFactor" to "50",
                "DFFlagDebugRenderForceTechnologyVoxel" to "False"
            )
        ),

        FFlagPreset(
            name = "Disable Telemetry",
            description = "Disables Roblox analytics and telemetry tracking.",
            icon = "🔇",
            category = PresetCategory.PERFORMANCE,
            flags = mapOf(
                "FFlagEnableInGameMenuChrome" to "False",
                "FFlagDebugDisableTelemetryEphemeralCounter" to "True",
                "FFlagDebugDisableTelemetryEphemeralStat" to "True",
                "FFlagDebugDisableTelemetryEventIngest" to "True",
                "FFlagDebugDisableTelemetryPoint" to "True",
                "FFlagDebugDisableTelemetryV2Counter" to "True",
                "FFlagDebugDisableTelemetryV2Event" to "True",
                "FFlagDebugDisableTelemetryV2Stat" to "True"
            )
        ),

        FFlagPreset(
            name = "Disable Chat",
            description = "Disables the in-game chat system.",
            icon = "💬",
            category = PresetCategory.GAMEPLAY,
            flags = mapOf(
                "FFlagUserShowGuiHideToggles" to "True",
                "DFFlagVoiceChat4" to "False",
                "FFlagInGameMenuV3PhotoMode" to "False"
            )
        ),

        FFlagPreset(
            name = "Better Shadows",
            description = "Improves shadow rendering quality.",
            icon = "🌑",
            category = PresetCategory.GRAPHICS,
            flags = mapOf(
                "FFlagNewAvatarShadowLogic" to "True",
                "FIntRenderShadowIntensity" to "200",
                "DFIntCullFactorPixelThresholdShadowMapHighQuality" to "0",
                "DFIntCullFactorPixelThresholdShadowMapLowQuality" to "100"
            )
        ),

        FFlagPreset(
            name = "Disable Grass",
            description = "Removes grass animations for better performance.",
            icon = "🌿",
            category = PresetCategory.PERFORMANCE,
            flags = mapOf(
                "DFIntGrassMovementReducedMotionFactor" to "0",
                "FIntGrassDetailStrands" to "0",
                "FIntFRMMinGrassDistance" to "0",
                "FIntFRMMaxGrassDistance" to "0"
            )
        ),

        FFlagPreset(
            name = "Fast Startup",
            description = "Reduces Roblox startup time.",
            icon = "🚀",
            category = PresetCategory.PERFORMANCE,
            flags = mapOf(
                "FFlagPreloadAllFonts" to "False",
                "DFIntHttpCurlRequestTimeoutInMs" to "5000",
                "DFIntPerfStatManagerDataRetentionTime" to "60"
            )
        )
    )

    // ---- Known FFlags reference ----
    fun getKnownFlags(): List<FFlag> = listOf(
        FFlag("DFIntTaskSchedulerTargetFps", "60", FFlagType.INT, "Target FPS cap", FFlagCategory.PERFORMANCE),
        FFlag("FFlagGameBasicSettingsFramerateCap5", "True", FFlagType.BOOL, "Enable FPS cap", FFlagCategory.PERFORMANCE),
        FFlag("DFFlagDebugRenderForceTechnologyVoxel", "False", FFlagType.BOOL, "Force voxel (low-quality) renderer", FFlagCategory.GRAPHICS),
        FFlag("FIntRenderShadowIntensity", "127", FFlagType.INT, "Shadow intensity (0=off, 255=max)", FFlagCategory.GRAPHICS),
        FFlag("DFIntCullFactorPixelThresholdShadowMapHighQuality", "150", FFlagType.INT, "High-quality shadow cull factor", FFlagCategory.GRAPHICS),
        FFlag("DFIntCullFactorPixelThresholdShadowMapLowQuality", "500", FFlagType.INT, "Low-quality shadow cull factor", FFlagCategory.GRAPHICS),
        FFlag("FFlagNewAvatarShadowLogic", "True", FFlagType.BOOL, "New avatar shadow system", FFlagCategory.GRAPHICS),
        FFlag("DFIntMaxFrameBufferSize", "4", FFlagType.INT, "Max framebuffer size", FFlagCategory.PERFORMANCE),
        FFlag("FIntRobloxGuiBlurIntensity", "10", FFlagType.INT, "GUI background blur intensity", FFlagCategory.GRAPHICS),
        FFlag("DFIntGrassMovementReducedMotionFactor", "50", FFlagType.INT, "Grass animation factor (0=off)", FFlagCategory.GRAPHICS),
        FFlag("DFIntConnectionMTUSize", "1200", FFlagType.INT, "Network MTU packet size", FFlagCategory.NETWORK),
        FFlag("DFIntPhysicsSenderMaxBandwidthBps", "50000", FFlagType.INT, "Max physics network bandwidth", FFlagCategory.NETWORK),
        FFlag("DFIntDataSendRate", "25", FFlagType.INT, "Data send rate (Hz)", FFlagCategory.NETWORK),
        FFlag("DFFlagVoiceChat4", "True", FFlagType.BOOL, "Enable voice chat", FFlagCategory.GAMEPLAY),
        FFlag("FFlagUserShowGuiHideToggles", "True", FFlagType.BOOL, "Show GUI hide toggles", FFlagCategory.UI),
        FFlag("FFlagEnableInGameMenuChrome", "True", FFlagType.BOOL, "In-game menu chrome", FFlagCategory.UI),
        FFlag("FFlagDebugDisableTelemetryEphemeralCounter", "False", FFlagType.BOOL, "Disable telemetry counter", FFlagCategory.DEBUG),
        FFlag("FFlagDebugDisableTelemetryEventIngest", "False", FFlagType.BOOL, "Disable telemetry event ingest", FFlagCategory.DEBUG),
        FFlag("FFlagDebugDisableTelemetryPoint", "False", FFlagType.BOOL, "Disable telemetry point", FFlagCategory.DEBUG),
        FFlag("FFlagPreloadAllFonts", "True", FFlagType.BOOL, "Preload all fonts on startup", FFlagCategory.PERFORMANCE),
        FFlag("FIntGrassDetailStrands", "4096", FFlagType.INT, "Grass strand count", FFlagCategory.GRAPHICS),
        FFlag("FIntFRMMinGrassDistance", "100", FFlagType.INT, "Min grass render distance", FFlagCategory.GRAPHICS),
        FFlag("FIntFRMMaxGrassDistance", "500", FFlagType.INT, "Max grass render distance", FFlagCategory.GRAPHICS),
        FFlag("DFIntHttpCurlRequestTimeoutInMs", "10000", FFlagType.INT, "HTTP request timeout (ms)", FFlagCategory.NETWORK),
        FFlag("DFFlagTextureCompositorEnabled", "True", FFlagType.BOOL, "Enable texture compositor", FFlagCategory.GRAPHICS)
    )
}
