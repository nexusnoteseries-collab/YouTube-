package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    // --- Watch History ---
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteWatchHistoryById(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()

    // --- Liked Videos ---
    @Query("SELECT * FROM liked_videos ORDER BY likedAt DESC")
    fun getLikedVideos(): Flow<List<LikedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLikedVideo(liked: LikedVideoEntity)

    @Query("DELETE FROM liked_videos WHERE videoId = :videoId")
    suspend fun deleteLikedVideoById(videoId: String)

    // --- Watch Later ---
    @Query("SELECT * FROM watch_later ORDER BY addedAt DESC")
    fun getWatchLater(): Flow<List<WatchLaterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchLater(watchLater: WatchLaterEntity)

    @Query("DELETE FROM watch_later WHERE videoId = :videoId")
    suspend fun deleteWatchLaterById(videoId: String)

    // --- Subscriptions ---
    @Query("SELECT * FROM subscriptions ORDER BY subscribedAt DESC")
    fun getSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(sub: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE channelName = :channelName")
    suspend fun deleteSubscription(channelName: String)
}
