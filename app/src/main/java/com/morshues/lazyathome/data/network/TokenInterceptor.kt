package com.morshues.lazyathome.data.network

import android.util.Log
import com.morshues.lazyathome.data.repository.AuthRepository
import com.morshues.lazyathome.settings.SettingsManager
import com.morshues.lazyathome.util.JwtUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class TokenInterceptor(
    private val settingsManager: SettingsManager,
    private val authRepository: AuthRepository
) : Interceptor {
    private val refreshMutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking {
            getValidAccessToken()
        }

        // If no token available, return 401 response without making the request
        if (token == null) {
            return Response.Builder()
                .request(originalRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized - No access token available")
                .body("".toResponseBody())
                .build()
        }

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }

    private suspend fun getValidAccessToken(): String? {
        val currentToken = settingsManager.getAccessToken()
            ?: return null

        val expiresAt = settingsManager.getTokenExpiresAt()
        if (expiresAt != null) {
            val now = System.currentTimeMillis()

            if (expiresAt - now < REFRESH_THRESHOLD) {
                refreshMutex.withLock {
                    // Double-check after acquiring lock (another coroutine might have refreshed)
                    val latestToken = settingsManager.getAccessToken()
                    val latestExpiry = settingsManager.getTokenExpiresAt()

                    if (latestExpiry != null && latestExpiry - System.currentTimeMillis() < REFRESH_THRESHOLD) {
                        val refreshToken = settingsManager.getRefreshToken()
                            ?: throw IllegalStateException("No refresh token available")
                        val deviceId = settingsManager.getOrCreateDeviceId()

                        try {
                            val newTokens = authRepository.refresh(refreshToken, deviceId)
                            val newExpiresAt = JwtUtils.getExpirationTime(newTokens.accessToken)
                            settingsManager.saveTokens(
                                newTokens.accessToken,
                                newTokens.refreshToken,
                                newExpiresAt,
                            )

                            return newTokens.accessToken
                        } catch (e: Exception) {
                            Log.i(TAG, "Refresh token failed: ${e.message}")
                        }
                    }

                    return latestToken ?: throw IllegalStateException("No access token available")
                }
            }
        }

        return currentToken
    }

    companion object {
        private const val TAG = "TokenInterceptor"

        private const val REFRESH_THRESHOLD = 2 * 60 * 1000
    }
}
