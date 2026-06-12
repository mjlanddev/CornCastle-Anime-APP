package com.mjland.database

import android.content.Context
import kotlinx.coroutines.flow.Flow
import com.mjland.api.AniListSyncHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val bookmarkDao = database.bookmarkDao()
    private val continueWatchingDao = database.continueWatchingDao()
    private val episodeWatchHistoryDao = database.episodeWatchHistoryDao()

    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarksFlow()
    val allContinueWatching: Flow<List<ContinueWatchingEntity>> = continueWatchingDao.getAllContinueWatchingFlow()
    val allWatchHistory: Flow<List<EpisodeWatchHistoryEntity>> = episodeWatchHistoryDao.getAllWatchHistoryFlow()

    fun getWatchHistoryForAnimeFlow(animeId: Int): Flow<List<EpisodeWatchHistoryEntity>> {
        return episodeWatchHistoryDao.getWatchHistoryForAnimeFlow(animeId)
    }

    suspend fun getWatchHistoryForAnime(animeId: Int): List<EpisodeWatchHistoryEntity> {
        return episodeWatchHistoryDao.getWatchHistoryForAnime(animeId)
    }

    fun getWatchHistoryForEpisodeFlow(animeId: Int, episodeNumber: Int): Flow<EpisodeWatchHistoryEntity?> {
        return episodeWatchHistoryDao.getWatchHistoryForEpisodeFlow(animeId, episodeNumber)
    }

    suspend fun getWatchHistoryForEpisode(animeId: Int, episodeNumber: Int): EpisodeWatchHistoryEntity? {
        return episodeWatchHistoryDao.getWatchHistoryForEpisode(animeId, episodeNumber)
    }

    suspend fun saveEpisodeWatchHistory(
        animeId: Int,
        episodeNumber: Int,
        title: String,
        coverImage: String?,
        progressPercent: Float,
        currentTimeSeconds: Long,
        durationSeconds: Long,
        language: String,
        format: String? = "TV",
        bannerImage: String? = null,
        genres: List<String>? = null,
        totalEpisodes: Int? = null
    ) {
        val history = EpisodeWatchHistoryEntity(
            animeId = animeId,
            episodeNumber = episodeNumber,
            title = title,
            coverImage = coverImage,
            progressPercent = progressPercent,
            currentTimeSeconds = currentTimeSeconds,
            durationSeconds = durationSeconds,
            language = language,
            timestamp = System.currentTimeMillis()
        )
        episodeWatchHistoryDao.insertWatchHistory(history)

        if (totalEpisodes != null && totalEpisodes > 0 && episodeNumber >= totalEpisodes && progressPercent > 0.9f) {
            
            
            continueWatchingDao.deleteContinueWatching(animeId)
        } else {
            val entry = ContinueWatchingEntity(
                animeId = animeId,
                title = title,
                coverImage = coverImage,
                bannerImage = bannerImage,
                genres = genres?.joinToString(","),
                averageScore = null,
                format = format ?: "TV",
                episodes = episodeNumber, 
                lastProgress = progressPercent,
                lastLanguage = language,
                timestamp = System.currentTimeMillis()
            )
            continueWatchingDao.insertContinueWatching(entry)
        }
    }

    suspend fun removeWatchHistoryForAnime(animeId: Int) {
        episodeWatchHistoryDao.deleteWatchHistoryForAnime(animeId)
    }

    suspend fun toggleBookmark(
        animeId: Int,
        title: String,
        coverImage: String?,
        bannerImage: String?,
        genres: List<String>?,
        averageScore: Int?,
        format: String?,
        episodes: Int?
    ) {
        val existing = bookmarkDao.getBookmark(animeId)
        if (existing != null) {
            bookmarkDao.deleteBookmark(animeId)
        } else {
            val bookmark = BookmarkEntity(
                animeId = animeId,
                title = title,
                coverImage = coverImage,
                bannerImage = bannerImage,
                genres = genres?.joinToString(","),
                averageScore = averageScore,
                format = format,
                episodes = episodes
            )
            bookmarkDao.insertBookmark(bookmark)
        }

        
        CoroutineScope(Dispatchers.IO).launch {
            AniListSyncHelper.syncBookmarkFavorite(context, animeId)
        }
    }

    suspend fun isBookmarked(animeId: Int): Boolean {
        return bookmarkDao.getBookmark(animeId) != null
    }

    suspend fun addContinueWatching(
        animeId: Int,
        title: String,
        coverImage: String?,
        bannerImage: String?,
        genres: List<String>?,
        averageScore: Int?,
        format: String?,
        episodes: Int?
    ) {
        val entry = ContinueWatchingEntity(
            animeId = animeId,
            title = title,
            coverImage = coverImage,
            bannerImage = bannerImage,
            genres = genres?.joinToString(","),
            averageScore = averageScore,
            format = format,
            episodes = episodes,
            timestamp = System.currentTimeMillis()
        )
        continueWatchingDao.insertContinueWatching(entry)
    }

    suspend fun removeContinueWatching(animeId: Int) {
        continueWatchingDao.deleteContinueWatching(animeId)
    }

    suspend fun clearContinueWatching() {
        continueWatchingDao.clearAll()
    }

    suspend fun clearAllBookmarks() {
        bookmarkDao.clearAll()
    }

    suspend fun clearAllEpisodeWatchHistory() {
        episodeWatchHistoryDao.clearAllHistory()
    }

    suspend fun syncAnilistFavorites(anilistFavs: List<com.mjland.model.AnimeMedia>) {
        val currentLocalMap = bookmarkDao.getAllBookmarks().associateBy { it.animeId }
        for (fav in anilistFavs) {
            if (!currentLocalMap.containsKey(fav.id)) {
                val bookmark = BookmarkEntity(
                    animeId = fav.id,
                    title = fav.title?.english ?: fav.title?.romaji ?: fav.title?.native ?: "",
                    coverImage = fav.coverImage?.large ?: fav.coverImage?.extraLarge,
                    bannerImage = fav.bannerImage,
                    genres = fav.genres?.joinToString(","),
                    averageScore = fav.averageScore,
                    format = fav.format,
                    episodes = fav.episodes,
                    timestamp = System.currentTimeMillis()
                )
                bookmarkDao.insertBookmark(bookmark)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ProfileRepository? = null

        fun getInstance(context: Context): ProfileRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ProfileRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
