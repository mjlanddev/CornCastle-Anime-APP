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
                
                val variables = mapOf(
                    "airingAt_greater" to startOfToday.toInt(),
                    "page" to 1
                )
                val request = GraphQLRequest(query, variables)
                val response = RetrofitClient.apiService.fetchAnime(request)
                val pageData = response.data.Page
                
                allSchedules.addAll(pageData?.airingSchedules ?: emptyList())
                allSchedules.sortBy { it.airingAt ?: Long.MAX_VALUE }
                
                if (allSchedules.isNotEmpty()) {
                    _schedules.value = allSchedules
                    _error.value = null 
                } else {
                    _schedules.value = emptyList()
                    _error.value = null
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
