package com.mjland.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjland.api.RetrofitClient
import com.mjland.model.AnimeDetails
import com.mjland.model.GraphQLRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.util.Log

import com.mjland.database.TokenManager

class DetailsViewModel : ViewModel() {
    private val _animeDetails = MutableStateFlow<AnimeDetails?>(null)
    val animeDetails = _animeDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked = _isBookmarked.asStateFlow()

    private val _lastWatchedEpisode = MutableStateFlow<Int?>(null)
    val lastWatchedEpisode = _lastWatchedEpisode.asStateFlow()

    fun fetchDetails(context: android.content.Context, id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
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
                        format
                        status
                        description
                        averageScore
                        meanScore
                        genres
                        season
                        seasonYear
                        episodes
                        duration
                        popularity
                        favourites
                        mediaListEntry {
                          id
                          mediaId
                          status
                          progress
                        }
                        studios {
                          edges {
                            isMain
                            node {
                              id
                              name
                            }
                          }
                        }
                        nextAiringEpisode {
                          episode
                          timeUntilAiring
                          airingAt
                        }
                        streamingEpisodes {
                          title
                          thumbnail
                          url
                          site
                        }
                        trailer {
                          id
                          site
                        }
                        characters(sort: [ROLE, RELEVANCE, ID], perPage: 10) {
                          edges {
                            node {
                              id
                              name {
                                userPreferred
                              }
                              image {
                                large
                              }
                            }
                            role
                            voiceActors(language: JAPANESE) {
                              id
                              name {
                                userPreferred
                              }
                              image {
                                large
                              }
                            }
                          }
                        }
                        tags {
                          name
                        }
                        staff(sort: [RELEVANCE, ID], perPage: 10) {
                          edges {
                            node {
                              id
                              name {
                                userPreferred
                              }
                              image {
                                large
                              }
                            }
                            role
                          }
                        }
                        recommendations(sort: RATING_DESC, perPage: 10) {
                          nodes {
                            mediaRecommendation {
                              id
                              title {
                                english
                                romaji
                              }
                              coverImage {
                                large
                              }
                              format
                              episodes
                              seasonYear
                            }
                          }
                        }
                        relations {
                          edges {
                            relationType
                            node {
                              id
                              title {
                                english
                                romaji
                              }
                              coverImage {
                                large
                              }
                              format
                              episodes
                              seasonYear
                              status
                            }
                          }
                        }
                      }
                    }
                """.trimIndent()
                
                val variables = mapOf("id" to id)
                val request = GraphQLRequest(query, variables)
                val token = TokenManager.getInstance(context).anilistTokenFlow.firstOrNull()
                val response = if (!token.isNullOrEmpty()) {
                    RetrofitClient.apiService.fetchAuthAnime("Bearer $token", request)
                } else {
                    RetrofitClient.apiService.fetchAnime(request)
                }
                
                _animeDetails.value = response.data.Media
                _error.value = null 
                
                
                updateNextEpisodeToWatch(context, id)
            } catch (e: Exception) {
                Log.e("DetailsViewModel", "Error fetching details: ${e.message}", e)
                val isOffline = e is java.io.IOException || e is java.net.UnknownHostException
                _error.value = if (isOffline) "OFFLINE" else (e.localizedMessage ?: e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkBookmarkedStatus(context: android.content.Context, animeId: Int) {
        viewModelScope.launch {
            val repo = com.mjland.database.ProfileRepository.getInstance(context)
            _isBookmarked.value = repo.isBookmarked(animeId)
            
            
            updateNextEpisodeToWatch(context, animeId)
        }
    }

    private suspend fun updateNextEpisodeToWatch(context: android.content.Context, animeId: Int) {
        val repo = com.mjland.database.ProfileRepository.getInstance(context)
        val history = repo.getWatchHistoryForAnime(animeId)
        
        
        var nextEpToWatch: Int? = null
        
        val localHistoryItem = history.maxByOrNull { it.timestamp }
        val anilistProgress = _animeDetails.value?.mediaListEntry?.progress ?: 0
        
        if (localHistoryItem != null) {
            val localEp = localHistoryItem.episodeNumber
            val localPercent = localHistoryItem.progressPercent
            
            if (anilistProgress > localEp) {
                
                nextEpToWatch = anilistProgress + 1
            } else if (anilistProgress == localEp) {
                
                if (localPercent < 0.85f) {
                    nextEpToWatch = localEp
                } else {
                    nextEpToWatch = localEp + 1
                }
            } else {
                
                if (localPercent < 0.85f) {
                    nextEpToWatch = localEp
                } else {
                    nextEpToWatch = localEp + 1
                }
            }
        } else {
            
            if (anilistProgress > 0) {
                nextEpToWatch = anilistProgress + 1
            }
        }
        
        
        val totalEps = _animeDetails.value?.episodes ?: 0
        if (totalEps > 0 && nextEpToWatch != null && nextEpToWatch > totalEps) {
            nextEpToWatch = totalEps
        }
        
        _lastWatchedEpisode.value = nextEpToWatch
        
        
        if (anilistProgress > 0 && anilistProgress > (localHistoryItem?.episodeNumber ?: 0)) {
            val details = _animeDetails.value
            if (details != null) {
                
                repo.saveEpisodeWatchHistory(
                    animeId = animeId,
                    episodeNumber = anilistProgress,
                    title = details.title?.english ?: details.title?.romaji ?: "Unknown",
                    coverImage = details.coverImage?.large ?: details.coverImage?.extraLarge,
                    progressPercent = 1.0f,
                    currentTimeSeconds = 0,
                    durationSeconds = 0,
                    language = localHistoryItem?.language ?: "sub",
                    format = details.format,
                    bannerImage = details.bannerImage,
                    genres = details.genres,
                    totalEpisodes = totalEps
                )
            }
        }
    }

    fun toggleBookmark(context: android.content.Context, anime: AnimeDetails) {
        viewModelScope.launch {
            val repo = com.mjland.database.ProfileRepository.getInstance(context)
            repo.toggleBookmark(
                animeId = anime.id,
                title = anime.title?.english ?: anime.title?.romaji ?: "Unknown",
                coverImage = anime.coverImage?.large,
                bannerImage = anime.bannerImage,
                genres = anime.genres,
                averageScore = anime.averageScore,
                format = anime.format,
                episodes = anime.episodes
            )
            _isBookmarked.value = repo.isBookmarked(anime.id)
        }
    }

    fun addToContinueWatching(context: android.content.Context, anime: AnimeDetails) {
        viewModelScope.launch {
            val repo = com.mjland.database.ProfileRepository.getInstance(context)
            repo.addContinueWatching(
                animeId = anime.id,
                title = anime.title?.english ?: anime.title?.romaji ?: "Unknown",
                coverImage = anime.coverImage?.large,
                bannerImage = anime.bannerImage,
                genres = anime.genres,
                averageScore = anime.averageScore,
                format = anime.format,
                episodes = anime.episodes
            )
        }
    }
}
