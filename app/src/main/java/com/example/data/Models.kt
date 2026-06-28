package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Memory model representing a video in VidTube.
 */
data class Video(
    val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val channelName: String,
    val channelAvatar: String,
    val views: String,
    val duration: String,
    val category: String,
    val uploadDate: String,
    val likesCount: String,
    val dislikesCount: String
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "liked_videos")
data class LikedVideoEntity(
    @PrimaryKey val videoId: String,
    val likedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_later")
data class WatchLaterEntity(
    @PrimaryKey val videoId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val channelName: String,
    val subscribedAt: Long = System.currentTimeMillis()
)
