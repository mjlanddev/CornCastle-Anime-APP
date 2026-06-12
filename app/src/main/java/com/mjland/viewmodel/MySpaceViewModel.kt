package com.mjland.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mjland.database.BookmarkEntity
import com.mjland.database.ContinueWatchingEntity
import com.mjland.database.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.mjland.database.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import com.mjland.model.ViewerNode
import com.mjland.model.MediaListGroup
import com.mjland.model.GraphQLRequest
import com.mjland.api.RetrofitClient

class MySpaceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository.getInstance(application)
    private val tokenManager = TokenManager.getInstance(application)

    val anilistToken = tokenManager.anilistTokenFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _viewerProfile = MutableStateFlow<ViewerNode?>(null)
    val viewerProfile = _viewerProfile.asStateFlow()

    private val _anilistMediaLists = MutableStateFlow<List<MediaListGroup>?>(null)
    val anilistMediaLists = _anilistMediaLists.asStateFlow()

    private val _anilistFavorites = MutableStateFlow<List<com.mjland.model.AnimeMedia>?>(null)
    val anilistFavorites = _anilistFavorites.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            tokenManager.anilistTokenFlow.distinctUntilChanged().collectLatest { token ->
                if (token != null) {
                    syncAnilistData(token)
                } else {
                    _viewerProfile.value = null
                    _anilistMediaLists.value = null
                    _anilistFavorites.value = null
                }
            }
        }
    }

    fun refreshAnilist() {
        val currentToken = anilistToken.value
        if (currentToken != null) {
            viewModelScope.launch {
                syncAnilistData(currentToken)
            }
        }
    }

    private suspend fun syncAnilistData(token: String) {
        _isSyncing.value = true
        try {
            
            val viewerQuery = """
                query {
                  Viewer {
                    id
                    name
                    avatar {
                      large
                    }
                    favourites {
                      anime {
                        nodes {
                          id
                          title {
                            romaji
                            english
                          }
                          coverImage {
                            large
                            extraLarge
                          }
                          bannerImage
                          format
                          episodes
                          status
                          genres
                          averageScore
                        }
                      }
                    }
                  }
                }
            """.trimIndent()
            
            val viewerResponse = RetrofitClient.apiService.fetchAuthAnime(
                "Bearer $token",
                GraphQLRequest(viewerQuery, emptyMap())
            )
            
            val viewer = viewerResponse.data.Viewer
            _viewerProfile.value = viewer
            _anilistFavorites.value = viewer?.favourites?.anime?.nodes
            
            
            if (viewer != null) {
                val listQuery = """
                    query(${'$'}userId: Int!) {
                      MediaListCollection(userId: ${'$'}userId, type: ANIME) {
                        lists {
                          name
                          isCustomList
                          isSplitCompletedList
                          status
                          entries {
                            id
                            mediaId
                            status
                            score
                            progress
                            updatedAt
                            media {
                              id
                              title {
                                romaji
                                english
                              }
                              coverImage {
                                large
                                extraLarge
                              }
                              bannerImage
                              format
                              episodes
                              status
                              genres
                              averageScore
                            }
                          }
                        }
                      }
                    }
                """.trimIndent()
                
                val listResponse = RetrofitClient.apiService.fetchAuthAnime(
                    "Bearer $token",
                    GraphQLRequest(listQuery, mapOf("userId" to viewer.id))
                )
                
                _anilistMediaLists.value = listResponse.data.MediaListCollection?.lists
                
                
                val currentLists = listResponse.data.MediaListCollection?.lists?.filter { it.status == "CURRENT" }
                currentLists?.forEach { listGroup ->
                    listGroup.entries?.forEach { entry ->
                        val media = entry.media
                        val progress = entry.progress ?: 0
                        if (media != null && progress > 0) {
                            val history = repository.getWatchHistoryForAnime(media.id)
                            val localMax = history.maxByOrNull { it.timestamp }?.episodeNumber ?: 0
                            if (progress > localMax) {
                                repository.saveEpisodeWatchHistory(
                                    animeId = media.id,
                                    episodeNumber = progress,
                                    title = media.title?.english ?: media.title?.romaji ?: "Unknown",
                                    coverImage = media.coverImage?.large ?: media.coverImage?.extraLarge,
                                    progressPercent = 1.0f,
                                    currentTimeSeconds = 0,
                                    durationSeconds = 0,
                                    language = "sub",
                                    format = media.format,
                                    bannerImage = media.bannerImage,
                                    genres = media.genres,
                                    totalEpisodes = media.episodes
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isSyncing.value = false
        }
    }

    fun logoutAnilist() {
        viewModelScope.launch {
            tokenManager.clearAnilistToken()
        }
    }

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val continueWatching: StateFlow<List<ContinueWatchingEntity>> = repository.allContinueWatching
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeFromContinueWatching(animeId: Int) {
        viewModelScope.launch {
            repository.removeContinueWatching(animeId)
        }
    }

    fun clearAllContinueWatching() {
        viewModelScope.launch {
            repository.clearContinueWatching()
            
            
            
            
            
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearContinueWatching()
            repository.clearAllEpisodeWatchHistory()
        }
    }

    fun clearWatchlist() {
        viewModelScope.launch {
            repository.clearAllBookmarks()
        }
    }
}
