package com.mjland.api

import com.mjland.model.AniListResponse
import com.mjland.model.GraphQLRequest
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AniListService {
    @Headers("Content-Type: application/json")
    @POST("/")
    suspend fun fetchAnime(@Body request: GraphQLRequest): AniListResponse

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("/")
    suspend fun fetchAuthAnime(
        @retrofit2.http.Header("Authorization") authHeader: String,
        @Body request: GraphQLRequest
    ): AniListResponse
}
