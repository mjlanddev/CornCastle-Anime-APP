package com.mjland.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjland.api.RetrofitClient
import com.mjland.model.AnimeMedia
import com.mjland.model.GraphQLRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class SearchViewModel : ViewModel() {
    private val _searchResults = MutableStateFlow<List<AnimeMedia>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    
    private val _selectedFormat = MutableStateFlow<String?>(null)
    val selectedFormat = _selectedFormat.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus = _selectedStatus.asStateFlow()

    private val _selectedCountry = MutableStateFlow<String?>(null)
    val selectedCountry = _selectedCountry.asStateFlow()

    private val _selectedSeason = MutableStateFlow<String?>(null)
    val selectedSeason = _selectedSeason.asStateFlow()

    private val _fromYear = MutableStateFlow<Int?>(null)
    val fromYear = _fromYear.asStateFlow()

    private val _toYear = MutableStateFlow<Int?>(null)
    val toYear = _toYear.asStateFlow()

    private val _episodesFrom = MutableStateFlow<Int?>(null)
    val episodesFrom = _episodesFrom.asStateFlow()

    private val _episodesTo = MutableStateFlow<Int?>(null)
    val episodesTo = _episodesTo.asStateFlow()

    private val _durationFrom = MutableStateFlow<Int?>(null)
    val durationFrom = _durationFrom.asStateFlow()

    private val _durationTo = MutableStateFlow<Int?>(null)
    val durationTo = _durationTo.asStateFlow()

    private val _selectedGenres = MutableStateFlow<Set<String>>(emptySet())
    val selectedGenres = _selectedGenres.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags = _selectedTags.asStateFlow()

    private val _selectedSort = MutableStateFlow<String>("POPULARITY_DESC")
    val selectedSort = _selectedSort.asStateFlow()

    
    private val _availableGenres = MutableStateFlow<List<String>>(emptyList())
    val availableGenres = _availableGenres.asStateFlow()

    private val _availableTags = MutableStateFlow<List<String>>(emptyList())
    val availableTags = _availableTags.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        fetchMetadata()
        searchAnime()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        triggerDebouncedSearch()
    }

    fun setFormat(format: String?) {
        _selectedFormat.value = format
        searchAnime()
    }

    fun setStatus(status: String?) {
        _selectedStatus.value = status
        searchAnime()
    }

    fun setCountry(country: String?) {
        _selectedCountry.value = country
        searchAnime()
    }

    fun setSeason(season: String?) {
        _selectedSeason.value = season
        searchAnime()
    }

    fun setYearRange(from: Int?, to: Int?) {
        _fromYear.value = from
        _toYear.value = to
        searchAnime()
    }

    fun setEpisodesRange(from: Int?, to: Int?) {
        _episodesFrom.value = from
        _episodesTo.value = to
        searchAnime()
    }

    fun setDurationRange(from: Int?, to: Int?) {
        _durationFrom.value = from
        _durationTo.value = to
        searchAnime()
    }

    fun selectSingleGenre(genre: String) {
        
        _searchQuery.value = ""
        _selectedFormat.value = null
        _selectedStatus.value = null
        _selectedCountry.value = null
        _selectedSeason.value = null
        _fromYear.value = null
        _toYear.value = null
        _episodesFrom.value = null
        _episodesTo.value = null
        _durationFrom.value = null
        _durationTo.value = null
        _selectedTags.value = emptySet()
        _selectedSort.value = "POPULARITY_DESC"
        
        _selectedGenres.value = setOf(genre)
        searchAnime()
    }

    fun selectSingleTag(tag: String) {
        
        _searchQuery.value = ""
        _selectedFormat.value = null
        _selectedStatus.value = null
        _selectedCountry.value = null
        _selectedSeason.value = null
        _fromYear.value = null
        _toYear.value = null
        _episodesFrom.value = null
        _episodesTo.value = null
        _durationFrom.value = null
        _durationTo.value = null
        _selectedGenres.value = emptySet()
        _selectedSort.value = "POPULARITY_DESC"
        
        _selectedTags.value = setOf(tag)
        searchAnime()
    }

    fun toggleGenre(genre: String) {
        val current = _selectedGenres.value.toMutableSet()
        if (current.contains(genre)) {
            current.remove(genre)
        } else {
            current.add(genre)
        }
        _selectedGenres.value = current
        searchAnime()
    }

    fun toggleTag(tag: String) {
        val current = _selectedTags.value.toMutableSet()
        if (current.contains(tag)) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        _selectedTags.value = current
        searchAnime()
    }

    fun setSort(sort: String) {
        _selectedSort.value = sort
        searchAnime()
    }

    fun clearAllFilters() {
        _selectedFormat.value = null
        _selectedStatus.value = null
        _selectedCountry.value = null
        _selectedSeason.value = null
        _fromYear.value = null
        _toYear.value = null
        _episodesFrom.value = null
        _episodesTo.value = null
        _durationFrom.value = null
        _durationTo.value = null
        _selectedGenres.value = emptySet()
        _selectedTags.value = emptySet()
        _selectedSort.value = "POPULARITY_DESC"
        searchAnime()
    }

    private fun triggerDebouncedSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500) 
            searchAnime()
        }
    }

    private fun fetchMetadata() {
        viewModelScope.launch {
            try {
                
                val query = """
                    query {
                      GenreCollection
                      MediaTagCollection {
                        name
                        category
                        isAdult
                      }
                    }
                """.trimIndent()
                val request = GraphQLRequest(query, emptyMap())
                val response = RetrofitClient.apiService.fetchAnime(request)
                
                val genres = response.data.GenreCollection?.filterNotNull() ?: emptyList()
                val tags = response.data.MediaTagCollection?.filter { it.isAdult == false }?.mapNotNull { it.name } ?: emptyList()
                
                if (genres.isNotEmpty()) {
                    _availableGenres.value = genres.sorted()
                }
                if (tags.isNotEmpty()) {
                    _availableTags.value = tags.sorted()
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Failed to fetch genres/tags metadata, using fallback", e)
            } finally {
                
                if (_availableGenres.value.size < 5) {
                    _availableGenres.value = listOf(
                        "Action", "Adventure", "Comedy", "Drama", "Ecchi", "Fantasy", 
                        "Horror", "Mahou Shoujo", "Mecha", "Music", "Mystery", 
                        "Psychological", "Romance", "Sci-Fi", "Slice of Life", "Sports", 
                        "Supernatural", "Thriller", "Harem", "Josei", "Seinen", "Shoujo", "Shounen"
                    ).sorted()
                }
                if (_availableTags.value.size < 5) {
                    _availableTags.value = listOf(
                        "Isekai", "Magic", "School", "Demons", "Super Power", 
                        "Military", "Historical", "Martial Arts", "Vampire", "Space", 
                        "Game", "CGI", "Post-Apocalyptic", "Tragedy", "Gore", 
                        "Survival", "Time Manipulation", "Anti-Hero", "Coming of Age"
                    ).sorted()
                }
            }
        }
    }

    private var currentPage = 1
    private var isCurrentlyLoadingNextPage = false
    private var hasReachedEnd = false

    fun searchAnime() {
        searchAnime(loadMore = false)
    }

    fun searchAnime(loadMore: Boolean = false) {
        if (isCurrentlyLoadingNextPage) return
        
        if (loadMore) {
            if (hasReachedEnd) return
            isCurrentlyLoadingNextPage = true
            currentPage++
        } else {
            currentPage = 1
            hasReachedEnd = false
            _isLoading.value = true
        }

        viewModelScope.launch {
            try {
                val variables = mutableMapOf<String, Any>()
                val queryParams = mutableListOf<String>()
                val queryDecl = mutableListOf<String>()

                
                queryParams.add("type: ANIME")
                queryParams.add("isAdult: false")

                val currQuery = _searchQuery.value.trim()
                if (currQuery.isNotEmpty()) {
                    queryDecl.add("\$search: String")
                    queryParams.add("search: \$search")
                    variables["search"] = currQuery
                }

                _selectedFormat.value?.let {
                    queryDecl.add("\$format: MediaFormat")
                    queryParams.add("format: \$format")
                    variables["format"] = it
                }

                _selectedStatus.value?.let {
                    queryDecl.add("\$status: MediaStatus")
                    queryParams.add("status: \$status")
                    variables["status"] = it
                }

                _selectedCountry.value?.let {
                    queryDecl.add("\$country: CountryCode")
                    queryParams.add("countryOfOrigin: \$country")
                    variables["country"] = it
                }

                _selectedSeason.value?.let {
                    queryDecl.add("\$season: MediaSeason")
                    queryParams.add("season: \$season")
                    variables["season"] = it
                }

                _fromYear.value?.let {
                    queryDecl.add("\$startDateGreater: Int")
                    queryParams.add("startDate_greater: \$startDateGreater")
                    variables["startDateGreater"] = it * 10000
                }

                _toYear.value?.let {
                    queryDecl.add("\$startDateLesser: Int")
                    queryParams.add("startDate_lesser: \$startDateLesser")
                    variables["startDateLesser"] = it * 10000 + 1231
                }

                _episodesFrom.value?.let {
                    queryDecl.add("\$episodesGreater: Int")
                    queryParams.add("episodes_greater: \$episodesGreater")
                    variables["episodesGreater"] = it - 1
                }

                _episodesTo.value?.let {
                    queryDecl.add("\$episodesLesser: Int")
                    queryParams.add("episodes_lesser: \$episodesLesser")
                    variables["episodesLesser"] = it + 1
                }

                _durationFrom.value?.let {
                    queryDecl.add("\$durationGreater: Int")
                    queryParams.add("duration_greater: \$durationGreater")
                    variables["durationGreater"] = it - 1
                }

                _durationTo.value?.let {
                    queryDecl.add("\$durationLesser: Int")
                    queryParams.add("duration_lesser: \$durationLesser")
                    variables["durationLesser"] = it + 1
                }

                val gList = _selectedGenres.value.toList()
                if (gList.isNotEmpty()) {
                    queryDecl.add("\$genres: [String]")
                    queryParams.add("genre_in: \$genres")
                    variables["genres"] = gList
                }

                val tList = _selectedTags.value.toList()
                if (tList.isNotEmpty()) {
                    queryDecl.add("\$tags: [String]")
                    queryParams.add("tag_in: \$tags")
                    variables["tags"] = tList
                }

                
                queryDecl.add("\$sort: [MediaSort]")
                queryParams.add("sort: \$sort")
                variables["sort"] = listOf(_selectedSort.value)

                
                queryDecl.add("\$page: Int")
                variables["page"] = currentPage

                val declStr = if (queryDecl.isNotEmpty()) "(${queryDecl.joinToString(", ")})" else ""
                val paramsStr = if (queryParams.isNotEmpty()) "(${queryParams.joinToString(", ")})" else ""

                val query = """
                    query $declStr {
                      Page(page: ${'$'}page, perPage: 40) {
                        media $paramsStr {
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
                          seasonYear
                          averageScore
                          status
                          description
                          genres
                        }
                      }
                    }
                """.trimIndent()
                
                val request = GraphQLRequest(query, variables)
                val response = RetrofitClient.apiService.fetchAnime(request)
                val newMedia = response.data.Page?.media ?: emptyList()
                
                _error.value = null 
                
                if (loadMore) {
                    if (newMedia.isEmpty()) {
                        hasReachedEnd = true
                    } else {
                        _searchResults.value = _searchResults.value + newMedia
                    }
                } else {
                    _searchResults.value = newMedia
                    if (newMedia.isEmpty()) {
                        hasReachedEnd = true
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search error", e)
                val isOffline = e is java.io.IOException || e is java.net.UnknownHostException
                _error.value = if (isOffline) "OFFLINE" else (e.localizedMessage ?: e.message ?: "Unknown error")
                if (!loadMore) {
                    _searchResults.value = emptyList()
                }
            } finally {
                _isLoading.value = false
                isCurrentlyLoadingNextPage = false
            }
        }
    }
}
