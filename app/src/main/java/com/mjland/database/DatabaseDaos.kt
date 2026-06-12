package com.mjland.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarksFlow(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    suspend fun getAllBookmarks(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE animeId = :animeId LIMIT 1")
    suspend fun getBookmark(animeId: Int): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE animeId = :animeId LIMIT 1")
    fun getBookmarkFlow(animeId: Int): Flow<BookmarkEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE animeId = :animeId")
    suspend fun deleteBookmark(animeId: Int)

    @Query("DELETE FROM bookmarks")
    suspend fun clearAll()
}

@Dao
interface ContinueWatchingDao {
    @Query("SELECT * FROM continue_watching ORDER BY timestamp DESC")
    fun getAllContinueWatchingFlow(): Flow<List<ContinueWatchingEntity>>

    @Query("SELECT * FROM continue_watching WHERE animeId = :animeId LIMIT 1")
    suspend fun getContinueWatching(animeId: Int): ContinueWatchingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContinueWatching(continueWatching: ContinueWatchingEntity)

    @Query("DELETE FROM continue_watching WHERE animeId = :animeId")
    suspend fun deleteContinueWatching(animeId: Int)

    @Query("DELETE FROM continue_watching")
    suspend fun clearAll()
}

@Dao
interface EpisodeWatchHistoryDao {
    @Query("SELECT * FROM episode_watch_history ORDER BY timestamp DESC")
    fun getAllWatchHistoryFlow(): Flow<List<EpisodeWatchHistoryEntity>>

    @Query("SELECT * FROM episode_watch_history WHERE animeId = :animeId ORDER BY episodeNumber ASC")
    fun getWatchHistoryForAnimeFlow(animeId: Int): Flow<List<EpisodeWatchHistoryEntity>>

    @Query("SELECT * FROM episode_watch_history WHERE animeId = :animeId ORDER BY episodeNumber ASC")
    suspend fun getWatchHistoryForAnime(animeId: Int): List<EpisodeWatchHistoryEntity>

    @Query("SELECT * FROM episode_watch_history WHERE animeId = :animeId AND episodeNumber = :episodeNumber LIMIT 1")
    suspend fun getWatchHistoryForEpisode(animeId: Int, episodeNumber: Int): EpisodeWatchHistoryEntity?

    @Query("SELECT * FROM episode_watch_history WHERE animeId = :animeId AND episodeNumber = :episodeNumber LIMIT 1")
    fun getWatchHistoryForEpisodeFlow(animeId: Int, episodeNumber: Int): Flow<EpisodeWatchHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(history: EpisodeWatchHistoryEntity)

    @Query("DELETE FROM episode_watch_history WHERE animeId = :animeId AND episodeNumber = :episodeNumber")
    suspend fun deleteWatchHistoryForEpisode(animeId: Int, episodeNumber: Int)

    @Query("DELETE FROM episode_watch_history WHERE animeId = :animeId")
    suspend fun deleteWatchHistoryForAnime(animeId: Int)

    @Query("DELETE FROM episode_watch_history")
    suspend fun clearAllHistory()
}
