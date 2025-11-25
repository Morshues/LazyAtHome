package com.morshues.lazyathome.settings

import android.content.Context
import androidx.preference.PreferenceManager
import com.morshues.lazyathome.BuildConfig
import com.morshues.lazyathome.ui.settings.RowOrderFragment.Companion.DEFAULT_ROW_OPTIONS

object SettingsManager {
    private const val DEFAULT_SERVER_PATH = BuildConfig.BASE_URL

    fun getServerPath(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString("server_path", DEFAULT_SERVER_PATH) ?: DEFAULT_SERVER_PATH
    }

    fun getNSFW(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("nsfw", true)
    }

    fun getRowOrderWithEnabled(context: Context): MutableList<RowSetting> {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val order = prefs.getString("row_order", "") ?: ""
        val enabledSet = prefs.getStringSet("enabled_rows", emptySet()) ?: emptySet()

        val savedOrder = order.split(",")
            .filter { it.isNotBlank() }

        val defaultMap = DEFAULT_ROW_OPTIONS.associateBy { it.id }

        val savedSet = savedOrder.toSet()
        val missingRows = DEFAULT_ROW_OPTIONS.filter { it.id !in savedSet }

        val result = mutableListOf<RowSetting>()

        for (id in savedOrder) {
            defaultMap[id]?.let {
                result.add(RowSetting(id, enabledSet.contains(id)))
            }
        }

        result += missingRows

        return result
    }

    fun saveRowOrderAndEnabled(context: Context, rows: List<RowSetting>) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
            .putString("row_order", rows.joinToString(",") { it.id })
            .putStringSet("enabled_rows", rows.filter { it.enabled }.map { it.id }.toSet())
            .apply()
    }

    fun getRemoteSeekStepMs(context: Context): Long {
        return 1_000L * PreferenceManager.getDefaultSharedPreferences(context)
            .getInt("remote_seek_step_ms", 5)
    }

    fun getTimeBarSeekStepMs(context: Context): Long {
        return 1_000L * PreferenceManager.getDefaultSharedPreferences(context)
            .getInt("time_bar_seek_step_ms", 30)
    }

    fun getButtonSeekStepMs(context: Context): Long {
        return 1_000L * PreferenceManager.getDefaultSharedPreferences(context)
            .getInt("button_seek_step_ms", 120)
    }

    fun getPageScrollSpeed(context: Context): Float {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getInt("link_page_scroll_speed", 100).toFloat()
    }

    fun getAccessToken(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString("access_token", null)
    }

    fun getRefreshToken(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString("refresh_token", null)
    }

    fun getUserEmail(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString("user_email", null)
    }

    fun getUserName(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString("user_name", null)
    }

    fun saveAuthData(context: Context, accessToken: String, refreshToken: String, email: String, name: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putString("user_email", email)
            .putString("user_name", name)
            .apply()
    }

    fun clearAuthData(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .remove("access_token")
            .remove("refresh_token")
            .remove("user_email")
            .remove("user_name")
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getAccessToken(context) != null
    }

}
