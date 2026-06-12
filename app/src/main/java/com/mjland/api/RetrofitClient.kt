package com.mjland.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://graphql.anilist.co"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY 
    }

    private val retryInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request()
        var response = chain.proceed(request)
        var tryCount = 0
        val maxLimit = 3
        while (!response.isSuccessful && response.code == 429 && tryCount < maxLimit) {
            tryCount++
            val retryAfterHeader = response.header("Retry-After")
            val retryAfter = retryAfterHeader?.toLongOrNull() ?: (tryCount * 1L)
            
            response.close()
            
            try {
                Thread.sleep(retryAfter * 1000L) 
            } catch (e: InterruptedException) {
                
            }
            response = chain.proceed(request)
        }
        response
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(retryInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: AniListService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AniListService::class.java)
    }
}
