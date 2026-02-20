package com.morshues.lazyathome.settings

import android.content.Context
import androidx.preference.PreferenceManager
import com.morshues.lazyathome.BuildConfig
import com.morshues.lazyathome.ui.settings.RowOrderFragment.Companion.DEFAULT_ROW_OPTIONS
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    fun getServerPath(): String {
        return prefs.getString("server_path", DEFAULT_SERVER_PATH) ?: DEFAULT_SERVER_PATH
    }

    fun getNSFW(): Boolean {
        return prefs.getBoolean("nsfw", true)
    }

    fun getRowOrderWithEnabled(): MutableList<RowSetting> {
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

    fun saveRowOrderAndEnabled(rows: List<RowSetting>) {
        prefs.edit {
            putString("row_order", rows.joinToString(",") { it.id })
                .putStringSet("enabled_rows", rows.filter { it.enabled }.map { it.id }.toSet())
        }
    }

    fun getRemoteSeekStepMs(): Long {
        return 1_000L * prefs.getInt("remote_seek_step_ms", 5)
    }

    fun getTimeBarSeekStepMs(): Long {
        return 1_000L * prefs.getInt("time_bar_seek_step_ms", 30)
    }

    fun getButtonSeekStepMs(): Long {
        return 1_000L * prefs.getInt("button_seek_step_ms", 120)
    }

    fun getPageScrollSpeed(): Float {
        return prefs.getInt("link_page_scroll_speed", 100).toFloat()
    }

    fun getWebSocketPort(): Int {
        return prefs.getInt("websocket_port", 8765)
    }

    fun getOrCreateDeviceId(): String {
        val existingId = prefs.getString(KEY_DEVICE_ID, null)
        return if (existingId != null) {
            existingId
        } else {
            val newId = UUID.randomUUID().toString()
            prefs.edit {
                putString(KEY_DEVICE_ID, newId)
            }
            newId
        }
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getTokenExpiresAt(): Long? {
        return prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0)
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_CACHED_EMAIL, null)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_CACHED_USER_NAME, null)
    }

    fun saveAuthData(accessToken: String, refreshToken: String, email: String, name: String) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_CACHED_EMAIL, email)
            putString(KEY_CACHED_USER_NAME, name)
        }
    }

    fun saveTokens(access: String, refresh: String, expiresAt: Long? = null) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, access)
            putString(KEY_REFRESH_TOKEN, refresh)
            if (expiresAt != null) {
                putLong(KEY_TOKEN_EXPIRES_AT, expiresAt)
            }
        }
    }

    fun clearAuthData() {
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_CACHED_EMAIL)
            remove(KEY_CACHED_USER_NAME)
        }
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    companion object {
        private const val DEFAULT_SERVER_PATH = BuildConfig.BASE_URL

        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_CACHED_EMAIL = "cached_email"
        private const val KEY_CACHED_USER_NAME = "user_name"
    }
}
