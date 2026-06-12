package com.mjland

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mjland.api.RetrofitClient
import com.mjland.model.AnimeMedia
import com.mjland.model.GraphQLRequest
import com.mjland.model.MediaListGroup
import com.mjland.database.TokenManager
import com.mjland.database.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log

enum class AnimeSection {
    TRENDING, POPULAR, TOP_RATED, CURRENTLY_AIRING, FINISHED_AIRING, UPCOMING, MOVIES, ACTION, ROMANCE, FANTASY, SCI_FI, MOST_FAVORITED
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _trendingAnime = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val trendingAnime = _trendingAnime.asStateFlow()

    private val _popularAnime = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val popularAnime = _popularAnime.asStateFlow()

    private val _topRatedAnime = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val topRatedAnime = _topRatedAnime.asStateFlow()
    
    private val _currentlyAiring = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val currentlyAiring = _currentlyAiring.asStateFlow()
    
    private val _upcomingAnime = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val upcomingAnime = _upcomingAnime.asStateFlow()
    
    private val _movies = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val movies = _movies.asStateFlow()

    private val _finishedAiring = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val finishedAiring = _finishedAiring.asStateFlow()

    private val _actionAnime = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val actionAnime = _actionAnime.asStateFlow()

    private val _romanceAnime = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val romanceAnime = _romanceAnime.asStateFlow()

    private val _fantasyAnime = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val fantasyAnime = _fantasyAnime.asStateFlow()
    
    private val _sciFiAnime = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val sciFiAnime = _sciFiAnime.asStateFlow()
    
    private val _mostFavorited = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val mostFavorited = _mostFavorited.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _selectedTab = MutableStateFlow("home")
    val selectedTab = _selectedTab.asStateFlow()

    fun setSelectedTab(route: String) {
        _selectedTab.value = route
    }

    private val sectionPages = mutableMapOf<AnimeSection, Int>().withDefault { 1 }
    private val sectionLoading = mutableMapOf<AnimeSection, Boolean>().withDefault { false }

    private val _anilistFavorites = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val anilistFavorites = _anilistFavorites.asStateFlow()

    private val _anilistMediaLists = MutableStateFlow<List<MediaListGroup>>(emptyList())
    val anilistMediaLists = _anilistMediaLists.asStateFlow()

    private val tokenManager = TokenManager.getInstance(application)

    init {
        fetchHomeData()
        observeAnilistToken()
    }

    private fun observeAnilistToken() {
        viewModelScope.launch {
            tokenManager.anilistTokenFlow.distinctUntilChanged().collectLatest { token ->
                if (token != null) {
                    fetchAnilistUserData(token)
                } else {
                    _anilistFavorites.value = emptyList()
                    _anilistMediaLists.value = emptyList()
                }
            }
        }
    }

    private suspend fun fetchAnilistUserData(token: String) {
        try {
            
            val viewerQuery = """
                query {
                  Viewer {
                    id
                    name
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
            val favs = viewer?.favourites?.anime?.nodes ?: emptyList()
            _anilistFavorites.value = favs
            
            
            try {
                ProfileRepository.getInstance(getApplication()).syncAnilistFavorites(favs)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to sync AniList favorites to local database", e)
            }
            
            
            if (viewer != null) {
                val listQuery = """
                    query(${'$'}userId: Int!) {
                      MediaListCollection(userId: ${'$'}userId, type: ANIME) {
                        lists {
                          name
                          isCustomList
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
                
                val lists = listResponse.data.MediaListCollection?.lists ?: emptyList()
                _anilistMediaLists.value = lists
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to fetch AniList user data on home load", e)
        }
    }

    private fun fetchHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            var firstError: Exception? = null

            try {
                
                kotlinx.coroutines.supervisorScope {
                    fun safeLaunch(block: suspend () -> Unit) = launch {
                        try {
                            block()
                        } catch (e: Exception) {
                            Log.e("MainViewModel", "Sub-fetch failed", e)
                            synchronized(this@MainViewModel) {
                                if (firstError == null) {
                                    firstError = e
                                }
                            }
                        }
                    }

                    val tasks = listOf<suspend () -> Unit>(
                        { _trendingAnime.value = fetchAnimeList("TRENDING_DESC") },
                        { _popularAnime.value = fetchAnimeList("POPULARITY_DESC") },
                        { _topRatedAnime.value = fetchAnimeList("SCORE_DESC") },
                        { _currentlyAiring.value = fetchAnimeList("POPULARITY_DESC", status = "RELEASING") },
                        { _finishedAiring.value = fetchAnimeList("POPULARITY_DESC", status = "FINISHED") },
                        { _upcomingAnime.value = fetchAnimeList("POPULARITY_DESC", status = "NOT_YET_RELEASED") },
                        { _movies.value = fetchAnimeList("POPULARITY_DESC", format = "MOVIE") },
                        { _actionAnime.value = fetchAnimeList("POPULARITY_DESC", genre = "Action") },
                        { _romanceAnime.value = fetchAnimeList("POPULARITY_DESC", genre = "Romance") },
                        { _fantasyAnime.value = fetchAnimeList("POPULARITY_DESC", genre = "Fantasy") },
                        { _sciFiAnime.value = fetchAnimeList("POPULARITY_DESC", genre = "Sci-Fi") },
                        { _mostFavorited.value = fetchAnimeList("FAVOURITES_DESC") }
                    )
                    
                    val jobs = tasks.mapIndexed { index, task ->
                        safeLaunch {
                            if (index > 2) {
                                kotlinx.coroutines.delay(index * 250L) 
                            }
                            task()
                        }
                    }
                    jobs.forEach { it.join() }
                }

                
                val err = firstError
                if (err != null && _trendingAnime.value.isEmpty()) {
                    val isOffline = err is java.io.IOException || err is java.net.UnknownHostException
                    _error.value = if (isOffline) "OFFLINE" else (err.localizedMessage ?: err.message ?: "Unknown error")
                } else {
                    _error.value = null
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "SupervisorScope failed: ${e.message}", e)
                val isOffline = e is java.io.IOException || e is java.net.UnknownHostException
                _error.value = if (isOffline) "OFFLINE" else (e.localizedMessage ?: e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        fetchHomeData()
    }

    fun loadNextPage(section: AnimeSection) {
        if (sectionLoading[section] == true) return
        sectionLoading[section] = true
        
        viewModelScope.launch {
            try {
                val nextPage = (sectionPages[section] ?: 1) + 1
                val newAnime = when (section) {
                    AnimeSection.TRENDING -> fetchAnimeList("TRENDING_DESC", page = nextPage)
                    AnimeSection.POPULAR -> fetchAnimeList("POPULARITY_DESC", page = nextPage)
                    AnimeSection.TOP_RATED -> fetchAnimeList("SCORE_DESC", page = nextPage)
                    AnimeSection.CURRENTLY_AIRING -> fetchAnimeList("POPULARITY_DESC", status = "RELEASING", page = nextPage)
                    AnimeSection.FINISHED_AIRING -> fetchAnimeList("POPULARITY_DESC", status = "FINISHED", page = nextPage)
                    AnimeSection.UPCOMING -> fetchAnimeList("POPULARITY_DESC", status = "NOT_YET_RELEASED", page = nextPage)
                    AnimeSection.MOVIES -> fetchAnimeList("POPULARITY_DESC", format = "MOVIE", page = nextPage)
                    AnimeSection.ACTION -> fetchAnimeList("POPULARITY_DESC", genre = "Action", page = nextPage)
                    AnimeSection.ROMANCE -> fetchAnimeList("POPULARITY_DESC", genre = "Romance", page = nextPage)
                    AnimeSection.FANTASY -> fetchAnimeList("POPULARITY_DESC", genre = "Fantasy", page = nextPage)
                    AnimeSection.SCI_FI -> fetchAnimeList("POPULARITY_DESC", genre = "Sci-Fi", page = nextPage)
                    AnimeSection.MOST_FAVORITED -> fetchAnimeList("FAVOURITES_DESC", page = nextPage)
                }
                
                if (newAnime.isNotEmpty()) {
                    sectionPages[section] = nextPage
                    when (section) {
                        AnimeSection.TRENDING -> _trendingAnime.value = _trendingAnime.value + newAnime
                        AnimeSection.POPULAR -> _popularAnime.value = _popularAnime.value + newAnime
                        AnimeSection.TOP_RATED -> _topRatedAnime.value = _topRatedAnime.value + newAnime
                        AnimeSection.CURRENTLY_AIRING -> _currentlyAiring.value = _currentlyAiring.value + newAnime
                        AnimeSection.FINISHED_AIRING -> _finishedAiring.value = _finishedAiring.value + newAnime
                        AnimeSection.UPCOMING -> _upcomingAnime.value = _upcomingAnime.value + newAnime
                        AnimeSection.MOVIES -> _movies.value = _movies.value + newAnime
                        AnimeSection.ACTION -> _actionAnime.value = _actionAnime.value + newAnime
                        AnimeSection.ROMANCE -> _romanceAnime.value = _romanceAnime.value + newAnime
                        AnimeSection.FANTASY -> _fantasyAnime.value = _fantasyAnime.value + newAnime
                        AnimeSection.SCI_FI -> _sciFiAnime.value = _sciFiAnime.value + newAnime
                        AnimeSection.MOST_FAVORITED -> _mostFavorited.value = _mostFavorited.value + newAnime
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading more for section $section: ${e.message}")
            } finally {
                sectionLoading[section] = false
            }
        }
    }

    
    private suspend fun fetchAnimeList(
        sort: String, 
        status: String? = null, 
        format: String? = null,
        genre: String? = null,
        page: Int = 1
    ): List<AnimeMedia> {
        val query = """
            query (${'$'}sort: [MediaSort], ${'$'}page: Int, ${'$'}status: MediaStatus, ${'$'}format: MediaFormat, ${'$'}genre: String) {
              Page(page: ${'$'}page, perPage: 15) {
                media(sort: ${'$'}sort, type: ANIME, isAdult: false, status: ${'$'}status, format: ${'$'}format, genre: ${'$'}genre) {
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
                  averageScore
                  description
                  genres
                  tags {
                    name
                  }
                  seasonYear
                }
              }
            }
        """.trimIndent()
        
        val variables = mutableMapOf<String, Any>(
            "sort" to listOf(sort),
            "page" to page
        )
        if (status != null) variables["status"] = status
        if (format != null) variables["format"] = format
        if (genre != null) variables["genre"] = genre
        
        val request = GraphQLRequest(query, variables)
        val response = RetrofitClient.apiService.fetchAnime(request)
        return response.data.Page?.media ?: emptyList()
    }
}
