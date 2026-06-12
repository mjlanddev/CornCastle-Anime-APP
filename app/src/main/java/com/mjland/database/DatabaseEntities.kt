package com.mjland.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val animeId: Int,
    val title: String,
    val coverImage: String?,
    val bannerImage: String? = null,
    val genres: String? = null, 
    val averageScore: Int?,
    val format: String?,
    val episodes: Int?,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "continue_watching")
data class ContinueWatchingEntity(
    @PrimaryKey val animeId: Int,
    val title: String,
    val coverImage: String?,
    val bannerImage: String? = null,
    val genres: String? = null,
    val averageScore: Int?,
    val format: String?,
    val episodes: Int?,
    val lastProgress: Float? = null,
    val lastLanguage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "episode_watch_history", primaryKeys = ["animeId", "episodeNumber"])
data class EpisodeWatchHistoryEntity(
    val animeId: Int,
    val episodeNumber: Int,
    val title: String,
    val coverImage: String?,
    val progressPercent: Float, 
    val currentTimeSeconds: Long,
    val durationSeconds: Long,
    val language: String, 
    val timestamp: Long = System.currentTimeMillis()
)
