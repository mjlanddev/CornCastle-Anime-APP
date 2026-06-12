package com.mjland.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjland.api.RetrofitClient
import com.mjland.model.AiringSchedule
import com.mjland.model.GraphQLRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.util.Calendar
import java.util.TimeZone

class ScheduleViewModel : ViewModel() {
    private val _schedules = MutableStateFlow<List<AiringSchedule>>(emptyList())
    val schedules = _schedules.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        fetchSchedule()
    }

    fun fetchSchedule() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis / 1000
                
                val query = """
                    query (${'$'}airingAt_greater: Int, ${'$'}page: Int) {
                      Page(page: ${'$'}page, perPage: 100) {
                        pageInfo {
                          hasNextPage
                        }
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
                
                val allSchedules = mutableListOf<AiringSchedule>()
                var firstError: Exception? = null

                kotlinx.coroutines.supervisorScope {
                    val jobs = (1..6).map { page ->
                        launch {
                            val variables = mapOf(
                                "airingAt_greater" to startOfToday.toInt(),
                                "page" to page
                            )
                            val request = GraphQLRequest(query, variables)
                            try {
                                val response = RetrofitClient.apiService.fetchAnime(request)
                                val pageData = response.data.Page
                                synchronized(allSchedules) {
                                    allSchedules.addAll(pageData?.airingSchedules ?: emptyList())
                                }
                            } catch (e: Exception) {
                                synchronized(this@ScheduleViewModel) {
                                    if (firstError == null) firstError = e
                                }
                            }
                        }
                    }
                    jobs.forEach { it.join() }
                }

                
                allSchedules.sortBy { it.airingAt ?: Long.MAX_VALUE }
                
                if (allSchedules.isNotEmpty() || firstError == null) {
                    _schedules.value = allSchedules
                    _error.value = null 
                } else {
                    throw firstError!!
                }
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Schedule error", e)
                val isOffline = e is java.io.IOException || e is java.net.UnknownHostException
                _error.value = if (isOffline) "OFFLINE" else (e.localizedMessage ?: e.message ?: "Unknown error")
                _schedules.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
