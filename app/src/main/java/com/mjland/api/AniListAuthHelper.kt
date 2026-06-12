package com.mjland.api

import android.util.Log
import com.mjland.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object AniListAuthHelper {
    private const val TAG = "AniListAuthHelper"
    private const val AUTH_BASE_URL = "https://anilist.co/api/v2/"
    private const val REDIRECT_URI = "corncastle://anilist-auth"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val authService: AniListAuthService by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AniListAuthService::class.java)
    }

    
    suspend fun exchangeCodeForToken(code: String): String? {
        val clientId = BuildConfig.ANILIST_API_KEY
        val clientSecret = BuildConfig.ANILIST_CLIENT_SECRET

        if (clientId.isEmpty() || clientSecret.isEmpty() || clientId == "YOUR_KEY_HERE" || clientSecret == "YOUR_CLIENT_SECRET_HERE") {
            Log.e(TAG, "AniList Client ID or Secret is missing or contains placeholder values.")
            return null
        }

        return try {
            val request = TokenRequest(
                client_id = clientId,
                client_secret = clientSecret,
                redirect_uri = REDIRECT_URI,
                code = code
            )
            val response = authService.getAccessToken(request)
            response.access_token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to exchange auth code for token", e)
            null
        }
    }
}
