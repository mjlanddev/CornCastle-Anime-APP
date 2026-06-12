package com.mjland.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjland.api.RetrofitClient
import com.mjland.database.EpisodeWatchHistoryEntity
import com.mjland.database.ProfileRepository
import com.mjland.model.AnimeDetails
import com.mjland.model.GraphQLRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    private val _animeDetails = MutableStateFlow<AnimeDetails?>(null)
    val animeDetails = _animeDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _currentEpisode = MutableStateFlow(1)
    val currentEpisode = _currentEpisode.asStateFlow()

    private val _currentLanguage = MutableStateFlow("sub") 
    val currentLanguage = _currentLanguage.asStateFlow()

    private val _watchHistory = MutableStateFlow<List<EpisodeWatchHistoryEntity>>(emptyList())
    val watchHistory = _watchHistory.asStateFlow()

    private val _isAutoNextEnabled = MutableStateFlow(false)
    val isAutoNextEnabled = _isAutoNextEnabled.asStateFlow()

    fun toggleAutoNext() {
        _isAutoNextEnabled.value = !_isAutoNextEnabled.value
    }

    fun fetchDetails(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val query = """
                    query (${'$'}id: Int) {
                      Media(id: ${'$'}id, type: ANIME) {
                        id
                        idMal
                        title {
                          romaji
                          english
                          native
                        }
                        coverImage {
                          extraLarge
                          large
                        }
                        bannerImage
                        genres
                        episodes
                        status
                        format
                        duration
                        averageScore
                        nextAiringEpisode {
                          episode
                          timeUntilAiring
                          airingAt
                        }
                        relations {
                          edges {
                            id
                            relationType(version: 2)
                            node {
                              id
                              idMal
                              title {
                                romaji
                                english
                                native
                              }
                              type
                              format
                              status
                              bannerImage
                              coverImage {
                                large
                                extraLarge
                              }
                              averageScore
                            }
                          }
                        }
                        recommendations(sort: [RATING_DESC, ID_DESC]) {
                          nodes {
                            mediaRecommendation {
                              id
                              idMal
                              title {
                                romaji
                                english
                                native
                              }
                              type
                              format
                              status
                              bannerImage
                              coverImage {
                                large
                                extraLarge
                              }
                              averageScore
                            }
                          }
                        }
                      }
                    }
                """.trimIndent()

                val variables = mapOf("id" to id)
                val request = GraphQLRequest(query, variables)
                val response = RetrofitClient.apiService.fetchAnime(request)
                
                _animeDetails.value = response.data.Media
                _error.value = null
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error fetching details: ${e.message}", e)
                val isOffline = e is java.io.IOException || e is java.net.UnknownHostException
                _error.value = if (isOffline) "OFFLINE" else (e.localizedMessage ?: e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun observeWatchHistory(context: Context, animeId: Int, initialEpisode: Int? = null) {
        viewModelScope.launch {
            if (initialEpisode != null) {
                _currentEpisode.value = initialEpisode
            }
            val repo = ProfileRepository.getInstance(context)
            repo.getWatchHistoryForAnimeFlow(animeId).collect { list ->
                _watchHistory.value = list
                
                
                if (list.isNotEmpty() && _currentEpisode.value == 1 && initialEpisode == null) {
                    val lastWatched = list.maxByOrNull { it.timestamp }
                    if (lastWatched != null) {
                        _currentEpisode.value = lastWatched.episodeNumber
                        _currentLanguage.value = lastWatched.language
                    }
                }
            }
        }
    }

    private var lastAutoNextEpisodeNum = -1
    private val syncedEpisodesToAnilist = mutableSetOf<Int>()

    fun selectEpisode(episodeNum: Int) {
        if (_currentEpisode.value != episodeNum) {
            lastAutoNextEpisodeNum = -1
        }
        _currentEpisode.value = episodeNum
    }

    fun setLanguage(lang: String) {
        if (lang == "sub" || lang == "dub") {
            _currentLanguage.value = lang
        }
    }

    fun saveWatchProgress(
        context: Context,
        progressPercent: Float,
        currentTimeSeconds: Long,
        durationSeconds: Long,
        episodeOverride: Int? = null
    ) {
        val details = _animeDetails.value ?: return
        val currentEp = episodeOverride ?: _currentEpisode.value
        val lang = _currentLanguage.value
        val title = details.title?.english ?: details.title?.romaji ?: "Anime"
        val coverImage = details.coverImage?.large ?: details.coverImage?.extraLarge
        
        if (isAutoNextEnabled.value && (progressPercent >= 0.95f)) {
            val totalEps = details.nextAiringEpisode?.episode?.let { it - 1 } ?: details.episodes ?: 1
            val maxEps = if (totalEps <= 0) 1 else totalEps
            if (currentEp < maxEps && lastAutoNextEpisodeNum != currentEp) {
                lastAutoNextEpisodeNum = currentEp
                _currentEpisode.value = currentEp + 1
            }
        }

        viewModelScope.launch {
            try {
                val repo = ProfileRepository.getInstance(context)
                val totalEps = details.nextAiringEpisode?.episode?.let { it - 1 } ?: details.episodes
                repo.saveEpisodeWatchHistory(
                    animeId = details.id,
                    episodeNumber = currentEp,
                    title = title,
                    coverImage = coverImage,
                    progressPercent = progressPercent,
                    currentTimeSeconds = currentTimeSeconds,
                    durationSeconds = durationSeconds,
                    language = lang,
                    format = details.format,
                    bannerImage = details.bannerImage,
                    genres = details.genres,
                    totalEpisodes = totalEps
                )

                if (progressPercent >= 0.85f && !syncedEpisodesToAnilist.contains(currentEp)) {
                    syncedEpisodesToAnilist.add(currentEp)
                    com.mjland.api.AniListSyncHelper.syncWatchProgress(
                        context = context,
                        animeId = details.id,
                        episodeNumber = currentEp,
                        totalEpisodes = details.episodes
                    )
                }
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Failed to save watch progress", e)
            }
        }
    }
}
