package com.cellstrap.app.data.models

import com.google.gson.annotations.SerializedName

data class FFlag(
    val key: String,
    val value: String,
    val type: FFlagType = FFlagType.STRING,
    val description: String = "",
    val category: FFlagCategory = FFlagCategory.MISC
)

enum class FFlagType {
    BOOL, INT, STRING, FLOAT
}

enum class FFlagCategory(val displayName: String) {
    PERFORMANCE("Performance"),
    GRAPHICS("Graphics"),
    NETWORK("Network"),
    UI("UI / Interface"),
    GAMEPLAY("Gameplay"),
    DEBUG("Debug"),
    MISC("Miscellaneous")
}

data class FFlagPreset(
    val name: String,
    val description: String,
    val icon: String,
    val flags: Map<String, String>,
    val category: PresetCategory = PresetCategory.PERFORMANCE
)

enum class PresetCategory(val displayName: String) {
    PERFORMANCE("Performance"),
    GRAPHICS("Graphics"),
    NETWORK("Network"),
    BALANCED("Balanced"),
    EXTREME("Extreme")
}

data class FFlagProfile(
    val id: String,
    val name: String,
    val flags: MutableMap<String, String> = mutableMapOf(),
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
)
