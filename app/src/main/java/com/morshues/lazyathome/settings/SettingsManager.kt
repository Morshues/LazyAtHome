package com.morshues.lazyathome.settings

import android.content.Context
import androidx.preference.PreferenceManager
import com.morshues.lazyathome.BuildConfig
import com.morshues.lazyathome.ui.settings.RowOrderFragment.Companion.DEFAULT_ROW_OPTIONS
import androidx.core.content.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

object SettingsManager {
    private const val DEFAULT_SERVER_PATH = BuildConfig.BASE_URL

    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
    private const val KEY_DEVICE_ID = "device_id"
    private const val KEY_CACHED_EMAIL = "cached_email"
    private const val KEY_CACHED_USER_NAME = "user_name"

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

    fun getOrCreateDeviceId(context: Context): String {
        val existingId = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_DEVICE_ID, null)
        return if (existingId != null) {
            existingId
        } else {
            val newId = UUID.randomUUID().toString()
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit {
                    putString(KEY_DEVICE_ID, newId)
                }
            newId
        }
    }

    fun getAccessToken(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_REFRESH_TOKEN, null)
    }

    fun getTokenExpiresAt(context: Context): Long? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getLong(KEY_TOKEN_EXPIRES_AT, 0)
    }

    fun getUserEmail(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_CACHED_EMAIL, null)
    }

    fun getUserName(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_CACHED_USER_NAME, null)
    }

    fun saveAuthData(context: Context, accessToken: String, refreshToken: String, email: String, name: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(KEY_ACCESS_TOKEN, accessToken)
                putString(KEY_REFRESH_TOKEN, refreshToken)
                putString(KEY_CACHED_EMAIL, email)
                putString(KEY_CACHED_USER_NAME, name)
            }
    }

    suspend fun saveTokens(context: Context, access: String, refresh: String, expiresAt: Long? = null) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(KEY_ACCESS_TOKEN, access)
                putString(KEY_REFRESH_TOKEN, refresh)
                if (expiresAt != null) {
                    putLong(KEY_TOKEN_EXPIRES_AT, expiresAt)
                }
            }
    }

    fun clearAuthData(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                remove(KEY_ACCESS_TOKEN)
                remove(KEY_REFRESH_TOKEN)
                remove(KEY_CACHED_EMAIL)
                remove(KEY_CACHED_USER_NAME)
            }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getAccessToken(context) != null
    }

}
