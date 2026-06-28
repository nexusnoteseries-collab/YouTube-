package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiService
import com.example.data.LikedVideoEntity
import com.example.data.SubscriptionEntity
import com.example.data.Video
import com.example.data.VideoDatabase
import com.example.data.VideoRepository
import com.example.data.WatchHistoryEntity
import com.example.data.WatchLaterEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Model representing an interactive comment on a video.
 */
data class Comment(
    val author: String,
    val authorAvatar: String,
    val content: String,
    val timeAgo: String = "Baru saja",
    val likesCount: Int = 0
)

/**
 * Model representing a chat message in the AI video helper.
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VideoRepository

    // Base flows from database
    val watchHistory: StateFlow<List<WatchHistoryEntity>>
    val likedVideos: StateFlow<List<LikedVideoEntity>>
    val watchLater: StateFlow<List<WatchLaterEntity>>
    val subscriptions: StateFlow<List<SubscriptionEntity>>

    // UI state flows
    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedVideo = MutableStateFlow<Video?>(null)
    val selectedVideo: StateFlow<Video?> = _selectedVideo.asStateFlow()

    // Comments stored in memory (key: videoId)
    private val _commentsState = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val commentsState = _commentsState.asStateFlow()

    // AI Chat history state (key: videoId)
    private val _chatHistoryState = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val chatHistoryState = _chatHistoryState.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    init {
        val database = VideoDatabase.getDatabase(application)
        repository = VideoRepository(database.videoDao())

        // Read direct from DB
        watchHistory = repository.getWatchHistory().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        likedVideos = repository.getLikedVideos().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        watchLater = repository.getWatchLater().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        subscriptions = repository.getSubscriptions().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed some initial realistic comments
        _commentsState.value = mapOf(
            "vid_001" to listOf(
                Comment("Budi Santoso", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100", "Bunnynya lucu sekali tapi kasihan dijahili terus 😂 Kualitas animasinya luar biasa untuk proyek open source!"),
                Comment("Siti Aminah", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100", "Ini film animasi 3D masa kecil saya. Masih asyik ditonton sampai sekarang!"),
                Comment("Andi Wijaya", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100", "Sangat menginspirasi bagi pengembang game dan animator tanah air.")
            ),
            "vid_002" to listOf(
                Comment("Rian Hidayat", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=100", "Sedih banget pas bagian akhirnya, gak nyangka Sintel bakal salah paham... 😭"),
                Comment("Dewi Lestari", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100", "Efek visual naga dan partikel saljunya halus banget! Blender juara!")
            ),
            "vid_003" to listOf(
                Comment("Fajar Pratama", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100", "Kombinasi CGI dan aktor riilnya sangat mulus. Amsterdam versi masa depan keren abis!"),
                Comment("Agus Hermawan", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=100", "Robot-robot raksasanya ngeri banget. Rekomendasi fiksi ilmiah yang wajib ditonton.")
            ),
            "vid_005" to listOf(
                Comment("Hendra Setiawan", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=100", "Lofi & suara kayu berderak ini menemani saya coding semalaman. Nyaman sekali."),
                Comment("Rina Kartika", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100", "Visualnya membuat pikiran tenang setelah seharian bekerja.")
            )
        )
    }

    // Get catalog of all videos
    val videos: List<Video> = repository.videoCatalog

    // Filtered list based on category & search query
    val filteredVideos: StateFlow<List<Video>> = combine(
        _selectedCategory,
        _searchQuery
    ) { category, query ->
        videos.filter { video ->
            val matchesCategory = category == "Semua" || video.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isEmpty() ||
                    video.title.contains(query, ignoreCase = true) ||
                    video.description.contains(query, ignoreCase = true) ||
                    video.channelName.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.videoCatalog
    )

    // Set Category Filter
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // Set Search Query
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Selected Video for Playback (add to history upon selection)
    fun selectVideo(video: Video?) {
        _selectedVideo.value = video
        if (video != null) {
            viewModelScope.launch {
                repository.addToHistory(video.id)
            }
            // Seed welcome AI summary message if empty
            if (_chatHistoryState.value[video.id].isNullOrEmpty()) {
                viewModelScope.launch {
                    _aiLoading.value = true
                    val summary = GeminiService.getAiResponse(video, null)
                    _chatHistoryState.value = _chatHistoryState.value.toMutableMap().apply {
                        this[video.id] = listOf(ChatMessage(summary, isUser = false))
                    }
                    _aiLoading.value = false
                }
            }
        }
    }

    // --- History interactions ---
    fun removeFromHistory(videoId: String) {
        viewModelScope.launch {
            repository.removeFromHistory(videoId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- Like Interactions ---
    fun toggleLike(video: Video, isLiked: Boolean) {
        viewModelScope.launch {
            if (isLiked) {
                repository.removeLikedVideo(video.id)
            } else {
                repository.addLikedVideo(video.id)
            }
        }
    }

    // --- Watch Later Interactions ---
    fun toggleWatchLater(video: Video, isAdded: Boolean) {
        viewModelScope.launch {
            if (isAdded) {
                repository.removeWatchLater(video.id)
            } else {
                repository.addWatchLater(video.id)
            }
        }
    }

    // --- Subscriptions Interactions ---
    fun toggleSubscription(channelName: String, isSubscribed: Boolean) {
        viewModelScope.launch {
            if (isSubscribed) {
                repository.unsubscribeFromChannel(channelName)
            } else {
                repository.subscribeToChannel(channelName)
            }
        }
    }

    // --- Comment Interactions ---
    fun addComment(videoId: String, content: String) {
        if (content.isBlank()) return
        val newComment = Comment(
            author = "Pengguna (Anda)",
            authorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100",
            content = content
        )
        val currentComments = _commentsState.value[videoId] ?: emptyList()
        _commentsState.value = _commentsState.value.toMutableMap().apply {
            this[videoId] = listOf(newComment) + currentComments
        }
    }

    // --- AI Chat Interactions ---
    fun sendAiQuestion(video: Video, question: String) {
        if (question.isBlank()) return

        val userMessage = ChatMessage(text = question, isUser = true)
        val currentChat = _chatHistoryState.value[video.id] ?: emptyList()
        
        _chatHistoryState.value = _chatHistoryState.value.toMutableMap().apply {
            this[video.id] = currentChat + userMessage
        }

        viewModelScope.launch {
            _aiLoading.value = true
            val aiResponse = GeminiService.getAiResponse(video, question)
            val aiMessage = ChatMessage(text = aiResponse, isUser = false)
            
            _chatHistoryState.value = _chatHistoryState.value.toMutableMap().apply {
                this[video.id] = (this[video.id] ?: emptyList()) + aiMessage
            }
            _aiLoading.value = false
        }
    }
}
