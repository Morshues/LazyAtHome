package com.morshues.lazyathome.data.model

import kotlinx.serialization.Serializable

data class LoginRequest(
    var email: String,
    var password: String,
    var deviceId: String,
)

data class LoginResponse(
    var ok: Boolean,
    var accessToken: String,
    var refreshToken: String,
    var user: UserDto,
)

data class RefreshTokenRequest(
    var refreshToken: String,
    var deviceId: String,
)

data class RefreshTokenResponse(
    var ok: Boolean,
    var accessToken: String,
    var refreshToken: String,
)

@Serializable
data class UserDto(
    val name: String,
    val email: String,
)