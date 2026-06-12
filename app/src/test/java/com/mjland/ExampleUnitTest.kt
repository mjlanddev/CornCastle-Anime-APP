package com.mjland

import com.mjland.api.RetrofitClient
import com.mjland.model.GraphQLRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ExampleUnitTest {
  @Test
  fun testFetchDetails() = runBlocking {
    val id = 15125 
    val query = """
        query (${'$'}id: Int) {
          Media(id: ${'$'}id, type: ANIME) {
            id
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
              thumbnail
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
    
    try {
        val response = RetrofitClient.apiService.fetchAnime(request)
        println("SUCCESS FETCHING DETAILS! Media id: ${response.data.Media?.id}")
        assertNotNull(response.data.Media)
    } catch (e: Exception) {
        println("FAILED FETCHING DETAILS! Exception message: ${e.message}")
        e.printStackTrace()
        fail(e.message)
    }
  }

  @Test
  fun testFetchSchedule() = runBlocking {
    val startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
    val query = """
        query (${'$'}airingAt_greater: Int) {
          Page(page: 1, perPage: 15) {
            airingSchedules(airingAt_greater: ${'$'}airingAt_greater, sort: TIME) {
              id
              airingAt
              episode
              media {
                id
                title {
                  romaji
                  english
                }
                coverImage {
                  large
                }
                format
              }
            }
          }
        }
    """.trimIndent()
    
    val variables = mapOf(
        "airingAt_greater" to startOfToday.toInt()
    )
    val request = GraphQLRequest(query, variables)
    
    try {
        val response = RetrofitClient.apiService.fetchAnime(request)
        val list = response.data.Page?.airingSchedules
        val info = "SUCCESS FETCHING SCHEDULE! Received ${list?.size} items. startOfToday=$startOfToday. Items: " + list?.map { "[airingAt=${it.airingAt}, title=${it.media?.title?.english ?: it.media?.title?.romaji}]" }?.joinToString(", ")
        throw RuntimeException(info)
    } catch (e: Exception) {
        if (e is RuntimeException && e.message?.contains("SUCCESS FETCHING SCHEDULE") == true) {
            throw e
        }
        println("FAILED FETCHING SCHEDULE! Exception message: ${e.message}")
        e.printStackTrace()
        fail(e.message)
    }
  }

  @Test
  fun testSearchAnime() = runBlocking {
    val queryDecl = mutableListOf<String>()
    val queryParams = mutableListOf<String>()
    val variables = mutableMapOf<String, Any>()

    queryParams.add("type: ANIME")
    queryParams.add("isAdult: false")

    
    queryDecl.add("\$sort: [MediaSort]")
    queryParams.add("sort: \$sort")
    variables["sort"] = listOf("POPULARITY_DESC")

    
    queryDecl.add("\$page: Int")
    variables["page"] = 1

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
    println("GENERATED QUERY:\n$query")
    println("GENERATED VARIABLES:\n$variables")

    try {
        val response = RetrofitClient.apiService.fetchAnime(request)
        println("SUCCESS FETCHING SEARCH! Received ${response.data.Page?.media?.size} items.")
        assertNotNull(response.data.Page?.media)
    } catch (e: Exception) {
        println("FAILED FETCHING SEARCH! Exception message: ${e.message}")
        e.printStackTrace()
        fail(e.message)
    }
  }

  @Test
  fun testSearchViewModelInit() = runBlocking {
    val model = com.mjland.viewmodel.SearchViewModel()
    
    var attempts = 0
    while (model.isLoading.value && attempts < 40) {
        kotlinx.coroutines.delay(100)
        attempts++
    }
    println("ViewModel loading finished. Results size: ${model.searchResults.value.size}")
    if (model.searchResults.value.isEmpty()) {
        fail("ViewModel searchResults is empty!")
    }
  }

  @Test
  fun testSearchByQuery() = runBlocking {
    val model = com.mjland.viewmodel.SearchViewModel()
    
    model.onSearchQueryChanged("Naruto")
    
    var attempts = 0
    while ((model.isLoading.value || model.searchResults.value.isEmpty() || model.searchResults.value.any { it.title?.english?.contains("Naruto", ignoreCase = true) != true && it.title?.romaji?.contains("Naruto", ignoreCase = true) != true }) && attempts < 50) {
        kotlinx.coroutines.delay(100)
        attempts++
    }
    println("Search by query finished. Results size: ${model.searchResults.value.size}")
    println("Results titles: ${model.searchResults.value.map { it.title?.english ?: it.title?.romaji }}")
    assertTrue("Results should not be empty", model.searchResults.value.isNotEmpty())
  }
}

