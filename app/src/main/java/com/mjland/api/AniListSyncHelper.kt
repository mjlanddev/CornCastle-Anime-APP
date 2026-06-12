package com.mjland.api

import android.content.Context
import android.util.Log
import com.mjland.database.TokenManager
import com.mjland.model.GraphQLRequest
import kotlinx.coroutines.flow.firstOrNull

object AniListSyncHelper {
    private const val TAG = "AniListSyncHelper"

    suspend fun getAuthToken(context: Context): String? {
        return TokenManager.getInstance(context).anilistTokenFlow.firstOrNull()
    }

    suspend fun syncWatchProgress(context: Context, animeId: Int, episodeNumber: Int, totalEpisodes: Int? = null) {
        val token = getAuthToken(context)
        if (token.isNullOrEmpty()) return
        
        try {
            val variables = mutableMapOf<String, Any>(
                "mediaId" to animeId,
                "progress" to episodeNumber
            )

            val isCompleted = totalEpisodes != null && totalEpisodes > 0 && episodeNumber >= totalEpisodes
            if (isCompleted) {
                variables["status"] = "COMPLETED"
            }

            val mutation = """
                mutation(${'$'}mediaId: Int, ${'$'}status: MediaListStatus, ${'$'}progress: Int) {
                  SaveMediaListEntry(mediaId: ${'$'}mediaId, status: ${'$'}status, progress: ${'$'}progress) {
                    id
                    status
                    progress
                  }
                }
            """.trimIndent()

            val request = GraphQLRequest(mutation, variables)
            RetrofitClient.apiService.fetchAuthAnime("Bearer $token", request)
            Log.d(TAG, "Successfully synced progress for animeId=$animeId to episode=$episodeNumber with isCompleted=$isCompleted")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing watch progress to AniList", e)
        }
    }

    suspend fun syncBookmarkFavorite(context: Context, animeId: Int) {
        val token = getAuthToken(context)
        if (token.isNullOrEmpty()) return
        
        try {
            val mutation = """
                mutation(${'$'}animeId: Int) {
                  ToggleFavourite(animeId: ${'$'}animeId) {
                    anime {
                      nodes {
                        id
                      }
                    }
                  }
                }
            """.trimIndent()

            val variables = mapOf(
                "animeId" to animeId
            )

            val request = GraphQLRequest(mutation, variables)
            RetrofitClient.apiService.fetchAuthAnime("Bearer $token", request)
            Log.d(TAG, "Successfully synced ToggleFavourite for animeId=$animeId")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing favorite bookmark to AniList", e)
        }
    }
}
