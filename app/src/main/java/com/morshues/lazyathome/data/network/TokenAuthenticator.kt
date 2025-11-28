package com.morshues.lazyathome.data.network

import android.content.Context
import com.morshues.lazyathome.data.repository.AuthRepository
import com.morshues.lazyathome.settings.SettingsManager
import com.morshues.lazyathome.util.JwtUtils
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val context: Context,
    private val authRepository: AuthRepository
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Close the response body to avoid leaking connections
        response.close()

        val originalAuthHeader = response.request.header("Authorization")
        if (originalAuthHeader == null) {
            return null
        }

        // Use synchronized block to prevent multiple concurrent token refreshes
        synchronized(this) {
            return runBlocking {
                try {
                    val currentToken = SettingsManager.getAccessToken(context)
                    val requestToken = originalAuthHeader.removePrefix("Bearer ")

                    // Check if token was already refreshed by another concurrent request
                    if (currentToken != null && currentToken != requestToken) {
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer $currentToken")
                            .build()
                    }

                    val refreshToken = SettingsManager.getRefreshToken(context)
                        ?: return@runBlocking null
                    val deviceId = SettingsManager.getOrCreateDeviceId(context)

                    val newTokens = authRepository.refresh(refreshToken, deviceId)
                    val expiresAt = JwtUtils.getExpirationTime(newTokens.accessToken)
                    SettingsManager.saveTokens(context, newTokens.accessToken, newTokens.refreshToken, expiresAt)

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()

                } catch (e: Exception) {
                    e.printStackTrace()
                    SettingsManager.clearAuthData(context)
                    null
                }
            }
        }
    }
}
