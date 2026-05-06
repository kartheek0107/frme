package com.fibcam.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.fibcam.model.OverlayColor
import com.fibcam.model.OverlayType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences-backed app preferences.
 * Manages overlay state, camera settings, and UI preferences.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "fibcam_prefs",
        Context.MODE_PRIVATE
    )

    // ── Overlay preferences ──────────────────────────────────────────────────

    fun getActiveOverlayIds(): Set<String> {
        return prefs.getStringSet("active_overlays", setOf("thirds")) ?: setOf("thirds")
    }

    fun setActiveOverlayIds(ids: Set<String>) {
        prefs.edit().putStringSet("active_overlays", ids).apply()
    }

    fun getOverlayColor(): String {
        return prefs.getString("overlay_color", "GOLD") ?: "GOLD"
    }

    fun setOverlayColor(color: String) {
        prefs.edit().putString("overlay_color", color).apply()
    }

    fun getOverlayAlpha(): Float {
        return prefs.getFloat("overlay_alpha", 0.75f)
    }

    fun setOverlayAlpha(alpha: Float) {
        prefs.edit().putFloat("overlay_alpha", alpha).apply()
    }

    // ── Camera settings ──────────────────────────────────────────────────────

    fun getLastFlashMode(): String {
        return prefs.getString("last_flash_mode", "AUTO") ?: "AUTO"
    }

    fun setLastFlashMode(mode: String) {
        prefs.edit().putString("last_flash_mode", mode).apply()
    }

    fun getLastZoom(): Float {
        return prefs.getFloat("last_zoom", 1f)
    }

    fun setLastZoom(zoom: Float) {
        prefs.edit().putFloat("last_zoom", zoom).apply()
    }

    fun getShowLevelIndicator(): Boolean {
        return prefs.getBoolean("show_level_indicator", false)
    }

    fun setShowLevelIndicator(show: Boolean) {
        prefs.edit().putBoolean("show_level_indicator", show).apply()
    }

    // ── Manual mode settings ─────────────────────────────────────────────────

    fun getIsManualMode(): Boolean {
        return prefs.getBoolean("manual_mode", false)
    }

    fun setIsManualMode(enabled: Boolean) {
        prefs.edit().putBoolean("manual_mode", enabled).apply()
    }

    fun getLastIso(): Int {
        return prefs.getInt("last_iso", 100)
    }

    fun setLastIso(iso: Int) {
        prefs.edit().putInt("last_iso", iso).apply()
    }
}