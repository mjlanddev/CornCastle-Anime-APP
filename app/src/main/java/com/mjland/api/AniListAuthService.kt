package com.mjland.api

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class TokenRequest(
    val grant_type: String = "authorization_code",
    val client_id: String,
    val client_secret: String,
    val redirect_uri: String,
    val code: String
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    val token_type: String?,
    val expires_in: Long?,
    val access_token: String?,
    val refresh_token: String?
)

interface AniListAuthService {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("oauth/token")
    suspend fun getAccessToken(@Body request: TokenRequest): TokenResponse
}
