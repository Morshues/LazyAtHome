package com.morshues.lazyathome.data.api

import com.morshues.lazyathome.data.model.LoginRequest
import com.morshues.lazyathome.data.model.LoginResponse
import com.morshues.lazyathome.data.model.RefreshTokenRequest
import com.morshues.lazyathome.data.model.RefreshTokenResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API service for authentication endpoints (login, refresh).
 * These endpoints don't require authentication and should NOT use TokenAuthenticator
 * to avoid circular dependencies.
 */
interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body requestBody: LoginRequest): LoginResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body requestBody: RefreshTokenRequest): RefreshTokenResponse
}
