package com.morshues.lazyathome.settings

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.preference.PreferenceManager
import com.morshues.lazyathome.BuildConfig
import com.morshues.lazyathome.ui.settings.RowOrderFragment.Companion.DEFAULT_ROW_OPTIONS
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
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

    fun saveLoginCredentials(email: String, password: String) {
        val encryptedPassword = try {
            encrypt(password)
        } catch (e: Exception) {
            return
        }
        prefs.edit {
            putString(KEY_SAVED_LOGIN_EMAIL, email)
            putString(KEY_SAVED_LOGIN_PASSWORD, encryptedPassword)
        }
    }

    fun getSavedLoginCredentials(): Pair<String, String>? {
        val email = prefs.getString(KEY_SAVED_LOGIN_EMAIL, null) ?: return null
        val encryptedPassword = prefs.getString(KEY_SAVED_LOGIN_PASSWORD, null) ?: return null
        val password = try {
            decrypt(encryptedPassword)
        } catch (e: Exception) {
            return null
        }
        return email to password
    }

    private fun getOrCreateLoginKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(LOGIN_KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                LOGIN_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateLoginKey())
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + cipherText, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val data = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = data.copyOfRange(0, GCM_IV_LENGTH)
        val cipherText = data.copyOfRange(GCM_IV_LENGTH, data.size)
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateLoginKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    companion object {
        private const val DEFAULT_SERVER_PATH = BuildConfig.BASE_URL

        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_CACHED_EMAIL = "cached_email"
        private const val KEY_CACHED_USER_NAME = "user_name"
        private const val KEY_SAVED_LOGIN_EMAIL = "saved_login_email"
        private const val KEY_SAVED_LOGIN_PASSWORD = "saved_login_password"

        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val LOGIN_KEY_ALIAS = "lazyathome_login_key"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH_BITS = 128
    }
}
