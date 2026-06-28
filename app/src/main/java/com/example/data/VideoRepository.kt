package com.example.data

import kotlinx.coroutines.flow.Flow

class VideoRepository(private val videoDao: VideoDao) {

    // Hardcoded static high-quality video catalog
    val videoCatalog = listOf(
        Video(
            id = "vid_001",
            title = "Big Buck Bunny - Official CGI Animation Movie",
            description = "A large and lovable rabbit deals with three mischievous rodents who try to disrupt his peaceful garden life. Created by the Blender Foundation as an open-source animation movie project, showcasing early 3D sculpting and particle physics.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600",
            channelName = "Blender Foundation",
            channelAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop",
            views = "4.2M views",
            duration = "9:56",
            category = "Animation",
            uploadDate = "3 years ago",
            likesCount = "240K",
            dislikesCount = "1.2K"
        ),
        Video(
            id = "vid_002",
            title = "Sintel - Open CGI Movie",
            description = "Sintel is an independently produced short CGI film by the Blender Foundation. It tells the touching story of a girl searching for her baby dragon, showcasing stunning visual environments, atmospheric lighting, and emotional character development.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600",
            channelName = "Durian Project",
            channelAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop",
            views = "1.8M views",
            duration = "14:48",
            category = "Fantasy",
            uploadDate = "1 year ago",
            likesCount = "112K",
            dislikesCount = "800"
        ),
        Video(
            id = "vid_003",
            title = "Tears of Steel - Sci-Fi VFX Short Film",
            description = "Set in a dystopian future Amsterdam, a group of scientists attempts to rescue the world from destructive giant robots by recreating a painful memory. It showcases advanced green screen compositing, CGI modeling, and action cinematography.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600",
            channelName = "Mango Project",
            channelAvatar = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=100&h=100&fit=crop",
            views = "2.5M views",
            duration = "12:14",
            category = "Sci-Fi",
            uploadDate = "4 years ago",
            likesCount = "190K",
            dislikesCount = "2.4K"
        ),
        Video(
            id = "vid_004",
            title = "Elephant's Dream - First Open CGI Movie",
            description = "The world's first open-source 3D animated film, exploring a strange, complex world inside an enormous typing machine where two characters with contrasting personalities try to escape surreal constructs.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600",
            channelName = "Orange Project",
            channelAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
            views = "980K views",
            duration = "10:53",
            category = "Surreal",
            uploadDate = "5 years ago",
            likesCount = "45K",
            dislikesCount = "410"
        ),
        Video(
            id = "vid_005",
            title = "Wilderness Exploration: Campfire & Outdoors",
            description = "Experience the calming ambient blazes of a campfire in the deep forest wilderness. Enjoy high-fidelity atmospheric forest sounds, crickets, and a crackling wood fire. Perfect for relaxation, study, or deep sleep.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=600",
            channelName = "Nature Channel",
            channelAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100&h=100&fit=crop",
            views = "120K views",
            duration = "0:15",
            category = "Nature",
            uploadDate = "2 months ago",
            likesCount = "15K",
            dislikesCount = "45"
        ),
        Video(
            id = "vid_006",
            title = "Ultimate Escapes: Scenic Coastal Drive",
            description = "Take a drive along the spectacular cliffs of the Pacific coast. Highlighting gorgeous dynamic scenery, rugged sea caves, and sweeping ocean horizons during early golden hour.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1506012787146-f92b2d7d6d96?w=600",
            channelName = "Travel & Beyond",
            channelAvatar = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=100&h=100&fit=crop",
            views = "340K views",
            duration = "0:15",
            category = "Travel",
            uploadDate = "3 months ago",
            likesCount = "29K",
            dislikesCount = "102"
        ),
        Video(
            id = "vid_007",
            title = "Epic Road Trip: Offroad Mud & Mountains",
            description = "Join an energetic road trip diving through muddy tracks, river beds, and steep mountain passes. Test your driving limits with high-speed offroad maneuvers, dramatic vehicle drifts, and stunning peak panoramas.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600",
            channelName = "Travel & Beyond",
            channelAvatar = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=100&h=100&fit=crop",
            views = "510K views",
            duration = "0:15",
            category = "Travel",
            uploadDate = "5 months ago",
            likesCount = "48K",
            dislikesCount = "180"
        ),
        Video(
            id = "vid_008",
            title = "Night Drive: Neon Lights & Cityscapes",
            description = "Cruising through the illuminated, busy streets of Tokyo at night. View the magnificent neon billboards, skyscraper light trails, and busy pedestrian crossings synced with ambient lo-fi music.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1519608487953-e999c86e7455?w=600",
            channelName = "Tech & Travel",
            channelAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop",
            views = "180K views",
            duration = "0:15",
            category = "Travel",
            uploadDate = "1 month ago",
            likesCount = "12K",
            dislikesCount = "33"
        )
    )

    fun getVideoById(id: String): Video? = videoCatalog.find { it.id == id }

    // --- History ---
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>> = videoDao.getWatchHistory()

    suspend fun addToHistory(videoId: String) {
        videoDao.insertWatchHistory(WatchHistoryEntity(videoId = videoId))
    }

    suspend fun removeFromHistory(videoId: String) {
        videoDao.deleteWatchHistoryById(videoId)
    }

    suspend fun clearHistory() {
        videoDao.clearWatchHistory()
    }

    // --- Liked Videos ---
    fun getLikedVideos(): Flow<List<LikedVideoEntity>> = videoDao.getLikedVideos()

    suspend fun addLikedVideo(videoId: String) {
        videoDao.insertLikedVideo(LikedVideoEntity(videoId = videoId))
    }

    suspend fun removeLikedVideo(videoId: String) {
        videoDao.deleteLikedVideoById(videoId)
    }

    // --- Watch Later ---
    fun getWatchLater(): Flow<List<WatchLaterEntity>> = videoDao.getWatchLater()

    suspend fun addWatchLater(videoId: String) {
        videoDao.insertWatchLater(WatchLaterEntity(videoId = videoId))
    }

    suspend fun removeWatchLater(videoId: String) {
        videoDao.deleteWatchLaterById(videoId)
    }

    // --- Subscriptions ---
    fun getSubscriptions(): Flow<List<SubscriptionEntity>> = videoDao.getSubscriptions()

    suspend fun subscribeToChannel(channelName: String) {
        videoDao.insertSubscription(SubscriptionEntity(channelName = channelName))
    }

    suspend fun unsubscribeFromChannel(channelName: String) {
        videoDao.deleteSubscription(channelName = channelName)
    }
}
