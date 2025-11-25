package com.morshues.lazyathome.data.repository

import com.morshues.lazyathome.data.api.AuthApiService
import com.morshues.lazyathome.data.model.LoginRequest
import com.morshues.lazyathome.data.model.LoginResponse
import com.morshues.lazyathome.data.model.RefreshTokenRequest
import com.morshues.lazyathome.data.model.RefreshTokenResponse

/**
 * Repository for authentication operations (login, refresh).
 * Uses AuthApiService which does NOT include TokenAuthenticator to avoid circular dependencies.
 */
class AuthRepository(private val authApi: AuthApiService) {
    suspend fun login(email: String, password: String, deviceId: String): LoginResponse {
        return authApi.login(LoginRequest(email = email, password = password, deviceId = deviceId))
    }

    suspend fun refresh(refreshToken: String, deviceId: String): RefreshTokenResponse {
        return authApi.refresh(RefreshTokenRequest(refreshToken = refreshToken, deviceId = deviceId))
    }
}