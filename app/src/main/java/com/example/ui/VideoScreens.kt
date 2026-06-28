package com.example.ui

import android.content.Intent
import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.data.Video

@Composable
fun VideoAppContent(viewModel: VideoViewModel) {
    var currentTab by remember { mutableStateOf("home") }
    val selectedVideo by viewModel.selectedVideo.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main Content Screen Area
                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        "home" -> HomeScreen(viewModel = viewModel)
                        "search" -> SearchScreen(viewModel = viewModel)
                        "library" -> LibraryScreen(viewModel = viewModel)
                    }
                }

                // Bottom Navigation Bar (hidden when playing full-screen video)
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav"),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == "home",
                        onClick = { currentTab = "home" },
                        icon = { Icon(if (currentTab == "home") Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                        label = { Text("Beranda") }
                    )
                    NavigationBarItem(
                        selected = currentTab == "search",
                        onClick = { currentTab = "search" },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        label = { Text("Cari") }
                    )
                    NavigationBarItem(
                        selected = currentTab == "library",
                        onClick = { currentTab = "library" },
                        icon = { Icon(if (currentTab == "library") Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary, contentDescription = "Library") },
                        label = { Text("Koleksi") }
                    )
                }
            }

            // Slide-up Video Detail Overlay
            AnimatedVisibility(
                visible = selectedVideo != null,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(350)) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                selectedVideo?.let { video ->
                    VideoDetailScreen(
                        video = video,
                        viewModel = viewModel,
                        onClose = { viewModel.selectVideo(null) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: VideoViewModel) {
    val videos by viewModel.filteredVideos.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categories = listOf("Semua", "Animation", "Sci-Fi", "Nature", "Travel", "Surreal")

    Column(modifier = Modifier.fillMaxSize()) {
        // App Branding Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = "VidTube Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "VidTube",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            )
        }

        // Horizontal Category Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("chip_$category")
                )
            }
        }

        // List of videos
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(videos) { video ->
                VideoCard(video = video, onClick = { viewModel.selectVideo(video) })
            }
        }
    }
}

@Composable
fun VideoCard(video: Video, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .clickable(onClick = onClick)
            .testTag("video_card_${video.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Thumbnail Layer with Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Duration Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.duration,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Video Meta Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Creator Avatar
                AsyncImage(
                    model = video.channelAvatar,
                    contentDescription = video.channelName,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Title and descriptions
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${video.channelName} • ${video.views} • ${video.uploadDate}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: VideoViewModel) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val videos by viewModel.filteredVideos.collectAsStateWithLifecycle()
    val searchTags = listOf("Animasi", "Sci-Fi", "Relaksasi", "Nature", "Naga", "Travel")

    Column(modifier = Modifier.fillMaxSize()) {
        // Modern Search Bar
        SearchBar(
            query = query,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onSearch = { viewModel.updateSearchQuery(it) },
            active = false,
            onActiveChange = {},
            placeholder = { Text("Cari judul, kreator, atau kategori...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("search_bar")
        ) {}

        // Fast Tag recommendations
        Text(
            text = "Rekomendasi Pencarian",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searchTags) { tag ->
                AssistChip(
                    onClick = {
                        val searchQuery = when (tag) {
                            "Animasi" -> "Animation"
                            "Naga" -> "Sintel"
                            "Relaksasi" -> "Campfire"
                            else -> tag
                        }
                        viewModel.updateSearchQuery(searchQuery)
                    },
                    label = { Text(tag) }
                )
            }
        }

        // List of searched videos
        if (videos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "Not found",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Video tidak ditemukan",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(videos) { video ->
                    VideoCard(video = video, onClick = { viewModel.selectVideo(video) })
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(viewModel: VideoViewModel) {
    val watchHistoryEntities by viewModel.watchHistory.collectAsStateWithLifecycle()
    val likedEntities by viewModel.likedVideos.collectAsStateWithLifecycle()
    val watchLaterEntities by viewModel.watchLater.collectAsStateWithLifecycle()
    val subscriptionEntities by viewModel.subscriptions.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Convert Database Entities to Memory Video models
    val watchHistoryVideos = watchHistoryEntities.mapNotNull { entity ->
        viewModel.videos.find { it.id == entity.videoId }
    }
    val likedVideos = likedEntities.mapNotNull { entity ->
        viewModel.videos.find { it.id == entity.videoId }
    }
    val watchLaterVideos = watchLaterEntities.mapNotNull { entity ->
        viewModel.videos.find { it.id == entity.videoId }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Creator Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120",
                        contentDescription = "User avatar",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Ahmad Sulaeman",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Subscribed: ${subscriptionEntities.size} • Liked: ${likedVideos.size}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Watch Later Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tonton Nanti (${watchLaterVideos.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.WatchLater, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (watchLaterVideos.isEmpty()) {
            item {
                Text(
                    text = "Belum ada video ditambahkan.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(watchLaterVideos) { video ->
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .clickable { viewModel.selectVideo(video) },
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = video.thumbnailUrl,
                                        contentDescription = video.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text(
                                    text = video.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Liked Videos List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Video yang Disukai (${likedVideos.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (likedVideos.isEmpty()) {
            item {
                Text(
                    text = "Belum ada video disukai.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(likedVideos) { video ->
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .clickable { viewModel.selectVideo(video) },
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = video.thumbnailUrl,
                                        contentDescription = video.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text(
                                    text = video.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Subscribed Channels Row
        item {
            Text(
                text = "Langganan (${subscriptionEntities.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (subscriptionEntities.isEmpty()) {
            item {
                Text(
                    text = "Belum berlangganan ke saluran manapun.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(subscriptionEntities) { sub ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                // Dynamic find a video of this channel to click play
                                val chVideo = viewModel.videos.find { it.channelName == sub.channelName }
                                if (chVideo != null) {
                                    viewModel.selectVideo(chVideo)
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sub.channelName.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sub.channelName,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(70.dp)
                            )
                        }
                    }
                }
            }
        }

        // Watch History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Tontonan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (watchHistoryVideos.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearHistory() }) {
                        Text("Hapus Semua", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
            }
        }

        if (watchHistoryVideos.isEmpty()) {
            item {
                Text(
                    text = "Belum ada riwayat tontonan.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            items(watchHistoryVideos) { video ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { viewModel.selectVideo(video) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = video.channelName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.removeFromHistory(video.id) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove from history",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoDetailScreen(
    video: Video,
    viewModel: VideoViewModel,
    onClose: () -> Unit
) {
    val likedEntities by viewModel.likedVideos.collectAsStateWithLifecycle()
    val watchLaterEntities by viewModel.watchLater.collectAsStateWithLifecycle()
    val subscriptionEntities by viewModel.subscriptions.collectAsStateWithLifecycle()

    val commentsMap by viewModel.commentsState.collectAsStateWithLifecycle()
    val chatHistoryMap by viewModel.chatHistoryState.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()

    val isLiked = likedEntities.any { it.videoId == video.id }
    val isAddedToWatchLater = watchLaterEntities.any { it.videoId == video.id }
    val isSubscribed = subscriptionEntities.any { it.channelName == video.channelName }

    val comments = commentsMap[video.id] ?: emptyList()
    val chatMessages = chatHistoryMap[video.id] ?: emptyList()

    var activeTab by remember { mutableStateOf("comments") } // or "ai"
    var descriptionExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Standard Custom Styled Video Player Wrapper
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        ) {
            AndroidVideoPlayer(videoUrl = video.videoUrl)

            // Overlap Close button in top left
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .testTag("player_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Close Player", tint = Color.White)
            }
        }

        // Scrollable Body Content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Title block
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            lineHeight = 22.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${video.views} • ${video.uploadDate} • ${video.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expandable Description Box
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { descriptionExpanded = !descriptionExpanded },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Deskripsi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Icon(
                                    if (descriptionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = video.description,
                                fontSize = 12.sp,
                                maxLines = if (descriptionExpanded) 100 else 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Actions Block (Like, Watch Later, Share)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Like button
                    Button(
                        onClick = { viewModel.toggleLike(video, isLiked) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isLiked) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = "Like",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isLiked) "Disukai" else video.likesCount, fontSize = 12.sp)
                    }

                    // Watch later button
                    Button(
                        onClick = { viewModel.toggleWatchLater(video, isAddedToWatchLater) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAddedToWatchLater) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isAddedToWatchLater) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = if (isAddedToWatchLater) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Watch Later",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tonton Nanti", fontSize = 12.sp)
                    }

                    // Share button
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, video.title)
                                putExtra(Intent.EXTRA_TEXT, "Ayo nonton video \"${video.title}\" di VidTube: ${video.videoUrl}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan via"))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan", fontSize = 12.sp)
                    }
                }
            }

            // Channel/Subscription Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = video.channelAvatar,
                        contentDescription = video.channelName,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(video.channelName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("1.5M Pelanggan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { viewModel.toggleSubscription(video.channelName, isSubscribed) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSubscribed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.onSurface,
                            contentColor = if (isSubscribed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("subscribe_button")
                    ) {
                        Text(if (isSubscribed) "Disubskripsi" else "Subscribe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tab bar for Comments & AI Assistant
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    TabItem(
                        text = "Komentar (${comments.size})",
                        isActive = activeTab == "comments",
                        onClick = { activeTab = "comments" },
                        modifier = Modifier.weight(1f)
                    )
                    TabItem(
                        text = "Asisten AI ✨",
                        isActive = activeTab == "ai",
                        onClick = { activeTab = "ai" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Render active tab contents
            if (activeTab == "comments") {
                item {
                    CommentBoxInput(onSendComment = { viewModel.addComment(video.id, it) })
                }
                if (comments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada komentar. Jadilah yang pertama!", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(comments) { comment ->
                        CommentItemRow(comment = comment)
                    }
                }
            } else {
                // AI Gemini Assistant View
                item {
                    AiAssistantView(
                        chatMessages = chatMessages,
                        aiLoading = aiLoading,
                        onAskQuestion = { viewModel.sendAiQuestion(video, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun TabItem(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent)
            )
        }
    }
}

@Composable
fun CommentBoxInput(onSendComment: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120",
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Tulis komentar publik...", fontSize = 13.sp) },
            modifier = Modifier
                .weight(1f)
                .testTag("comment_input"),
            shape = RoundedCornerShape(20.dp),
            maxLines = 3,
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            onSendComment(text)
                            text = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Kirim", tint = MaterialTheme.colorScheme.primary)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (text.isNotBlank()) {
                    onSendComment(text)
                    text = ""
                }
            })
        )
    }
}

@Composable
fun CommentItemRow(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comment.authorAvatar,
            contentDescription = comment.author,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.author, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(comment.timeAgo, fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(comment.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AiAssistantView(
    chatMessages: List<ChatMessage>,
    aiLoading: Boolean,
    onAskQuestion: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Explanatory note
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Tanyakan apa saja kepada AI seputar video ini. AI dapat menganalisis deskripsi dan merespons secara langsung!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat Log Display
        chatMessages.forEach { msg ->
            val alignEnd = msg.isUser
            val bubbleColor = if (msg.isUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            val textColor = if (msg.isUser) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(
                            color = bubbleColor,
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (alignEnd) 12.dp else 0.dp,
                                bottomEnd = if (alignEnd) 0.dp else 12.dp
                            )
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = msg.text,
                        color = textColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        if (aiLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI sedang berpikir...", fontSize = 11.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat input field
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Tanyakan sesuatu ke AI...", fontSize = 13.sp) },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_chat_input"),
            maxLines = 2,
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (query.isNotBlank() && !aiLoading) {
                            onAskQuestion(query)
                            query = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Kirim", tint = MaterialTheme.colorScheme.primary)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (query.isNotBlank() && !aiLoading) {
                    onAskQuestion(query)
                    query = ""
                }
            })
        )
    }
}

@Composable
fun AndroidVideoPlayer(videoUrl: String) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var duration by remember { mutableStateOf(0) }
    var currentPosition by remember { mutableStateOf(0) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }

    // Auto fade-out controls timer
    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(4000)
            isControlsVisible = false
        }
    }

    // Keep updating current duration position slider
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(500)
            currentPosition = currentPosition // will be fetched from View below
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isControlsVisible = !isControlsVisible
            }
    ) {
        // Native Android VideoView integration
        var videoViewRef: VideoView? = null
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoPath(videoUrl)
                    setOnPreparedListener { mp ->
                        isBuffering = false
                        duration = duration
                        mp.isLooping = true
                        start()
                    }
                    setOnInfoListener { _, what, _ ->
                        if (what == 3) { // MEDIA_INFO_VIDEO_RENDERING_START
                            isBuffering = false
                        }
                        true
                    }
                }
            },
            update = { view ->
                videoViewRef = view
                if (isPlaying) {
                    view.start()
                } else {
                    view.pause()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Custom controller loop to update seek position labels dynamically
        LaunchedEffect(isPlaying) {
            while (true) {
                videoViewRef?.let {
                    currentPosition = it.currentPosition
                    duration = it.duration
                }
                delay(250)
            }
        }

        // Loading spinner during buffering
        if (isBuffering) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Black Semi-transparent controller overlay
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Play / Pause center toggle
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .align(Alignment.Center)
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play Toggle",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Bottom Seek Slider Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current position duration text
                    Text(
                        text = formatTime(currentPosition),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Progress Slider
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                        onValueChange = { percent ->
                            videoViewRef?.let {
                                val seekToMs = (percent * duration).toInt()
                                it.seekTo(seekToMs)
                                currentPosition = seekToMs
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Total duration text
                    Text(
                        text = formatTime(duration),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Convert Milliseconds duration to video play time labels (mm:ss)
private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
