package com.nexusdigital.rivavideo.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionOff
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.nexusdigital.rivavideo.data.AudioLanguageTrack
import com.nexusdigital.rivavideo.data.ChannelDetails
import com.nexusdigital.rivavideo.data.OfflineVideoItem
import com.nexusdigital.rivavideo.data.RegionPreference
import com.nexusdigital.rivavideo.data.ShortItem
import com.nexusdigital.rivavideo.data.SubtitleTrack
import com.nexusdigital.rivavideo.data.VideoItem
import com.nexusdigital.rivavideo.data.VideoQualityTrack
import com.nexusdigital.rivavideo.data.WatchHistoryItem
import com.nexusdigital.rivavideo.data.formatDuration
import com.nexusdigital.rivavideo.data.parseRelativeAgeSeconds
import com.nexusdigital.rivavideo.data.parseViews
import com.nexusdigital.rivavideo.video.VideoUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow

fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

/**
 * ROOT APP UI:
 * - YouTube Home Feed: Full-width video cards with 16:9 previews, channel avatars, view count, publish time.
 * - YouTube Watch Screen: 16:9 Player on top (auto-rotates to landscape on fullscreen), Subtitle / CC toggle,
 *   Always-visible Audio Language switcher, HD Quality selector, Subscribe button, Like/Share, and Up Next video list.
 */
@Composable
fun RivaVideoApp(viewModel: RivaVideoViewModel) {
    val dark = isSystemInDarkTheme()
    val feed by viewModel.feed.collectAsStateWithLifecycle()
    val feedLoading by viewModel.feedLoading.collectAsStateWithLifecycle()
    val feedError by viewModel.feedError.collectAsStateWithLifecycle()
    val activeCategory by viewModel.activeCategory.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val regionPreference by viewModel.regionPreference.collectAsStateWithLifecycle()
    val showRegionPicker by viewModel.showRegionPicker.collectAsStateWithLifecycle()
    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val watchIndex by viewModel.watchIndex.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val shorts by viewModel.shorts.collectAsStateWithLifecycle()
    val shortsLoading by viewModel.shortsLoading.collectAsStateWithLifecycle()
    val activeChannel by viewModel.activeChannel.collectAsStateWithLifecycle()
    val channelLoading by viewModel.channelLoading.collectAsStateWithLifecycle()
    val isPlayerMinimized by viewModel.isPlayerMinimized.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RivaL.bg(dark)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when (currentTab) {
                    RivaVideoViewModel.AppTab.HOME -> {
                        YouTubeHomeScreen(
                            dark = dark,
                            feed = feed,
                            loading = feedLoading,
                            error = feedError,
                            activeCategory = activeCategory,
                            categories = availableCategories,
                            regionPreference = regionPreference,
                            onOpenRegionPicker = viewModel::openRegionPicker,
                            searchText = searchText,
                            onCategory = viewModel::loadCategory,
                            onSearchText = viewModel::setSearchText,
                            onSearchSubmit = viewModel::submitSearch,
                            onVideoClick = viewModel::openWatch,
                            onChannelClick = { id, name -> viewModel.openChannel(id, name) },
                        )
                    }
                    RivaVideoViewModel.AppTab.SHORTS -> {
                        YouTubeShortsScreen(
                            dark = dark,
                            viewModel = viewModel,
                            shorts = shorts,
                            loading = shortsLoading,
                            onChannelClick = { id, name -> viewModel.openChannel(id, name) },
                        )
                    }
                    RivaVideoViewModel.AppTab.PROFILE -> {
                        YouTubeProfileScreen(
                            dark = dark,
                            viewModel = viewModel,
                        )
                    }
                }
            }

            // YouTube Floating Miniplayer Bar — slides up from bottom with spring animation
            AnimatedVisibility(
                visible = isPlayerMinimized && watchIndex != null && feed.isNotEmpty(),
                enter = slideInVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                    initialOffsetY = { it },
                ) + scaleIn(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                    initialScale = 0.90f,
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ) + fadeIn(tween(220)),
                exit = slideOutVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    targetOffsetY = { it },
                ) + scaleOut(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    targetScale = 0.90f,
                    transformOrigin = TransformOrigin(0.5f, 1f),
                ) + fadeOut(tween(180)),
            ) {
                val miniVideo = if (watchIndex != null && feed.isNotEmpty())
                    feed.getOrNull(watchIndex!!.coerceIn(0, feed.lastIndex))
                else null
                if (miniVideo != null) {
                    YouTubeMiniPlayerBar(
                        dark = dark,
                        video = miniVideo,
                        ui = ui,
                        progressFlow = viewModel.progress,
                        onExpand = viewModel::expandPlayer,
                        onTogglePlay = viewModel::togglePlayPause,
                        onClose = viewModel::closeMiniPlayer,
                    )
                }
            }

            // YouTube Bottom Navigation Bar (Smoothly slides in/out when minimizing/expanding video or viewing channel)
            AnimatedVisibility(
                visible = (watchIndex == null || isPlayerMinimized) && activeChannel == null,
                enter = slideInVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                    initialOffsetY = { it },
                ) + fadeIn(tween(180)),
                exit = slideOutVertically(
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    targetOffsetY = { it },
                ) + fadeOut(tween(150)),
            ) {
                YouTubeBottomNavBar(
                    currentTab = currentTab,
                    onSelectTab = viewModel::selectTab,
                    dark = dark,
                )
            }
        }

        // Region / Content Preference Dialog (First launch or manual switch)
        if (showRegionPicker) {
            RegionPreferenceDialog(
                dark = dark,
                currentRegion = regionPreference,
                onSelectRegion = viewModel::selectRegionPreference,
                onDismiss = viewModel::closeRegionPicker,
            )
        }

        // YouTube Channel Screen Overlay
        activeChannel?.let { channel ->
            YouTubeChannelScreen(
                channel = channel,
                loading = channelLoading,
                dark = dark,
                onClose = viewModel::closeChannel,
                onToggleSubscribe = viewModel::toggleChannelSubscribe,
                onVideoClick = viewModel::openVideoFromChannel,
            )
        }

        // YouTube Watch Screen Overlay (Hidden when minimized to floating miniplayer)
        // Animates: slide up from bottom on open, slide down + shrink on minimize
        val showWatch = watchIndex != null && feed.isNotEmpty() && !isPlayerMinimized
        AnimatedVisibility(
            visible = showWatch,
            enter = slideInVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                initialOffsetY = { it },
            ) + scaleIn(
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                initialScale = 0.85f,
                transformOrigin = TransformOrigin(0.1f, 0.95f),
            ) + fadeIn(tween(260)),
            exit = slideOutVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                targetOffsetY = { it },
            ) + scaleOut(
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                targetScale = 0.85f,
                transformOrigin = TransformOrigin(0.1f, 0.95f),
            ) + fadeOut(tween(220)),
        ) {
            val safeIndex = (watchIndex ?: 0).coerceIn(0, feed.lastIndex)
            YouTubeWatchScreen(
                dark = dark,
                viewModel = viewModel,
                feed = feed,
                currentIndex = safeIndex,
                onClose = viewModel::closeWatch,
                onMinimize = viewModel::minimizePlayer,
                onSelectRelated = { newIndex ->
                    viewModel.openWatch(newIndex)
                },
                onChannelClick = { id, name -> viewModel.openChannel(id, name) },
            )
        }
    }
}

// ---------------- YOUTUBE HOME SCREEN FEED ----------------

@Composable
private fun YouTubeHomeScreen(
    dark: Boolean,
    feed: List<VideoItem>,
    loading: Boolean,
    error: String?,
    activeCategory: RivaVideoViewModel.Category,
    categories: List<RivaVideoViewModel.Category>,
    regionPreference: RegionPreference,
    onOpenRegionPicker: () -> Unit,
    searchText: String,
    onCategory: (RivaVideoViewModel.Category) -> Unit,
    onSearchText: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onVideoClick: (Int) -> Unit,
    onChannelClick: (String?, String?) -> Unit,
) {
    var searchOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        // YouTube App Top Bar
        if (searchOpen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { searchOpen = false }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = RivaL.text(dark),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(RivaL.pill(dark)),
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = onSearchText,
                        placeholder = {
                            Text(
                                "Search YouTube…",
                                color = RivaL.textDim(dark),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        trailingIcon = {
                            if (searchText.isNotBlank()) {
                                IconButton(onClick = { onSearchText("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = RivaL.textDim(dark))
                                }
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = RivaL.red(dark),
                            focusedTextColor = RivaL.text(dark),
                            unfocusedTextColor = RivaL.text(dark),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = {
                    onSearchSubmit()
                    searchOpen = false
                }) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = RivaL.text(dark),
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // YouTube Red Logo Icon
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RivaL.red(dark))
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "RivaVideo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RivaL.text(dark),
                    letterSpacing = (-0.5).sp,
                )
                Spacer(Modifier.width(8.dp))
                // Quick Region Switcher Pill (India flag, etc.)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(RivaL.pill(dark))
                        .clickable(onClick = onOpenRegionPicker)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = regionPreference.flag,
                        fontSize = 14.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { searchOpen = true }) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = RivaL.text(dark),
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }

        // Horizontal Category Filter Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories) { category ->
                val selected = category.label == activeCategory.label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selected) RivaL.text(dark) else RivaL.pill(dark),
                        )
                        .clickable { onCategory(category) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = category.label,
                        color = if (selected) RivaL.bg(dark) else RivaL.text(dark),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Video Feed
        when {
            loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = RivaL.red(dark), strokeWidth = 3.dp)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Loading YouTube feed…",
                        color = RivaL.textDim(dark),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            error != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(
                        text = error,
                        color = RivaL.textDim(dark),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onCategory(activeCategory) },
                        colors = ButtonDefaults.buttonColors(containerColor = RivaL.red(dark)),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retry")
                    }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                itemsIndexed(feed, key = { _, item -> item.videoId }) { index, item ->
                    YouTubeVideoCard(
                        item = item,
                        dark = dark,
                        onClick = { onVideoClick(index) },
                        onChannelClick = onChannelClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun YouTubeVideoCard(
    item: VideoItem,
    dark: Boolean,
    onClick: () -> Unit,
    onChannelClick: ((String?, String?) -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(bottom = 18.dp),
    ) {
        // 16:9 Thumbnail Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(RivaL.panelMid(dark)),
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // Duration Pill (bottom-right)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xE6000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = formatDuration(item.durationMs),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                )
            }
        }

        // Details Row below Thumbnail
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            val avatarClickModifier = if (onChannelClick != null) {
                Modifier.clickable { onChannelClick(item.channelId, item.channel) }
            } else Modifier

            // Circular Channel Avatar
            if (!item.channelThumbnailUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = item.channelThumbnailUrl,
                    contentDescription = item.channel,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .then(avatarClickModifier),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RivaL.pill(dark))
                        .then(avatarClickModifier),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.channel.firstOrNull()?.uppercase() ?: "Y",
                        color = RivaL.text(dark),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Title & Channel / Views metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = RivaL.text(dark),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(3.dp))
                val views = item.viewCount ?: "1.2M views"
                val time = item.publishedTime ?: "recently"
                Text(
                    text = "${item.channel} • $views • $time",
                    color = RivaL.textDim(dark),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = avatarClickModifier,
                )
            }

            // 3-dots Menu
            IconButton(onClick = {}) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = RivaL.textDim(dark),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ---------------- YOUTUBE FLOATING MINIPLAYER BAR ----------------

@Composable
private fun YouTubeMiniPlayerBar(
    dark: Boolean,
    video: VideoItem,
    ui: VideoUiState,
    progressFlow: StateFlow<PlaybackProgress>,
    onExpand: () -> Unit,
    onTogglePlay: () -> Unit,
    onClose: () -> Unit,
) {
    val progress by progressFlow.collectAsStateWithLifecycle()
    val progressFraction = remember(progress.positionMs, progress.durationMs) {
        if (progress.durationMs > 0L) {
            (progress.positionMs.toFloat() / progress.durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    Surface(
        color = if (dark) Color(0xFF212121) else Color(0xFFF2F2F2),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onExpand),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 16:9 Mini video preview thumbnail
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black),
                ) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (ui.isBuffering) {
                        CircularProgressIndicator(
                            color = RivaL.red(dark),
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center),
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                // Title and Channel metadata
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = video.title,
                        color = RivaL.text(dark),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = video.channel,
                        color = RivaL.textDim(dark),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Play / Pause Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier.size(38.dp),
                ) {
                    Icon(
                        imageVector = if (ui.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (ui.isPlaying) "Pause" else "Play",
                        tint = RivaL.text(dark),
                        modifier = Modifier.size(24.dp),
                    )
                }

                // Cut / Close Button (❌)
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(38.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Player",
                        tint = RivaL.text(dark),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            // Real-time slim progress bar line at bottom edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(if (dark) Color(0x33FFFFFF) else Color(0x22000000)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progressFraction)
                        .height(2.dp)
                        .background(RivaL.red(dark)),
                )
            }
        }
    }
}

// ---------------- YOUTUBE WATCH SCREEN (WITH AUTO SCREEN ROTATION ON FULLSCREEN) ----------------

@Composable
private fun YouTubeWatchScreen(
    dark: Boolean,
    viewModel: RivaVideoViewModel,
    feed: List<VideoItem>,
    currentIndex: Int,
    onClose: () -> Unit,
    onMinimize: () -> Unit = onClose,
    onSelectRelated: (Int) -> Unit,
    onChannelClick: (String?, String?) -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val isResolving by viewModel.isResolving.collectAsStateWithLifecycle()

    var isFullscreen by remember { mutableStateOf(false) }
    val animScale = remember { Animatable(1f) }
    val animOffsetX = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var zoomPillText by remember { mutableStateOf<String?>(null) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showAudioLanguageDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isFullscreen, currentIndex) {
        animScale.snapTo(1f)
        animOffsetX.snapTo(0f)
        animOffsetY.snapTo(0f)
        zoomPillText = null
    }

    val currentVideo = feed.getOrNull(currentIndex)
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    val screenW = remember(activity) {
        val dm = activity?.resources?.displayMetrics
        dm?.widthPixels?.toFloat() ?: 1920f
    }
    val screenH = remember(activity) {
        val dm = activity?.resources?.displayMetrics
        dm?.heightPixels?.toFloat() ?: 1080f
    }
    val landscapeW = maxOf(screenW, screenH)
    val landscapeH = minOf(screenW, screenH).coerceAtLeast(1f)
    val fillScale = remember(landscapeW, landscapeH) {
        val screenAspect = landscapeW / landscapeH
        val videoAspect = 16f / 9f
        (if (screenAspect > videoAspect) screenAspect / videoAspect else videoAspect / screenAspect).coerceIn(1.15f, 2.5f)
    }

    // Intercept Back button: Fullscreen mode -> exit fullscreen; Portrait mode -> minimize to floating miniplayer
    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
    }
    BackHandler(enabled = !isFullscreen) {
        onMinimize()
    }

    // Auto-rotate screen to Landscape when Fullscreen is ON, and Portrait when OFF
    DisposableEffect(isFullscreen) {
        val window = activity?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, it.decorView) }

        if (isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
            insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Load video stream when current index changes
    LaunchedEffect(currentIndex) {
        viewModel.loadForPage(currentIndex)
    }

    // Subtitle Language Dialog
    if (showSubtitleDialog) {
        SubtitleLanguageDialog(
            dark = dark,
            availableSubtitles = ui.availableSubtitles,
            selectedLang = ui.selectedSubtitleLang,
            isSubtitlesEnabled = ui.isSubtitlesEnabled,
            onSelect = { sub ->
                viewModel.selectSubtitleLanguage(sub)
                showSubtitleDialog = false
            },
            onDismiss = { showSubtitleDialog = false },
        )
    }

    // Audio Language Dialog
    if (showAudioLanguageDialog) {
        AudioLanguageDialog(
            dark = dark,
            availableTracks = ui.availableAudioLanguages,
            selectedName = ui.selectedAudioLanguage,
            onSelect = { track ->
                viewModel.selectAudioLanguage(track)
                showAudioLanguageDialog = false
            },
            onDismiss = { showAudioLanguageDialog = false },
        )
    }

    // Video Quality Selection Dialog (HD 1080p, 720p, 480p, 360p)
    if (showQualityDialog) {
        VideoQualityDialog(
            dark = dark,
            availableQualities = ui.availableQualities,
            selectedQuality = ui.qualityLabel,
            onSelect = { quality ->
                viewModel.switchQuality(quality)
                showQualityDialog = false
            },
            onDismiss = { showQualityDialog = false },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RivaL.bg(dark)),
    ) {
        // Animate fullscreen <-> portrait transition with smooth scale + fade animation
        AnimatedContent(
            targetState = isFullscreen,
            transitionSpec = {
                if (targetState) {
                    // Going fullscreen: smooth cinematic scale + fade
                    (fadeIn(tween(320, easing = FastOutSlowInEasing)) +
                            scaleIn(
                                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                                initialScale = 0.90f,
                            )) togetherWith
                            (fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 1.06f))
                } else {
                    // Exiting fullscreen: smooth scale + fade back to portrait
                    (fadeIn(tween(320, easing = FastOutSlowInEasing)) +
                            scaleIn(
                                spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                                initialScale = 1.06f,
                            )) togetherWith
                            (fadeOut(tween(220)) + scaleOut(tween(220), targetScale = 0.90f))
                }
            },
            label = "fullscreen_transition",
            modifier = Modifier.fillMaxSize(),
        ) { fullscreen ->
        if (fullscreen) {
            // FULLSCREEN VIDEO VIEW (SCREEN ROTATED TO LANDSCAPE)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                // Scalable and pannable video player surface
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .graphicsLayer {
                            scaleX = animScale.value
                            scaleY = animScale.value
                            translationX = animOffsetX.value
                            translationY = animOffsetY.value
                        },
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                        },
                        update = { view ->
                            val player = viewModel.attachedPlayer()
                            if (view.player !== player) view.player = player
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                YouTubePlayerOverlay(
                    dark = dark,
                    ui = ui,
                    progressFlow = viewModel.progress,
                    isFullscreen = true,
                    onTogglePlay = viewModel::togglePlayPause,
                    onSeek = viewModel::seekTo,
                    onSeekRelative = viewModel::seekRelative,
                    onClose = { isFullscreen = false },
                    onToggleFullscreen = { isFullscreen = false },
                    onToggleSubtitles = viewModel::toggleSubtitles,
                    onOpenSubtitleDialog = { showSubtitleDialog = true },
                    onOpenAudioDialog = { showAudioLanguageDialog = true },
                    onOpenQualityDialog = { showQualityDialog = true },
                    onSelectSpeed = viewModel::setPlaybackSpeed,
                    onRetry = { viewModel.retryCurrentVideo(currentIndex) },
                    zoomScale = animScale.value,
                    zoomOffset = androidx.compose.ui.geometry.Offset(animOffsetX.value, animOffsetY.value),
                    onZoomChange = { scale, offset ->
                        coroutineScope.launch {
                            animScale.snapTo(scale)
                            animOffsetX.snapTo(offset.x)
                            animOffsetY.snapTo(offset.y)
                        }
                        zoomPillText = when {
                            scale <= 1.05f -> "Original"
                            kotlin.math.abs(scale - fillScale) < 0.08f -> "Zoomed to fill"
                            scale >= 3.95f -> "Max Zoom (4.0x)"
                            else -> String.format(java.util.Locale.US, "%.1fx", scale)
                        }
                    },
                    onZoomFill = {
                        zoomPillText = "Zoomed to fill"
                        coroutineScope.launch {
                            launch { animScale.animateTo(fillScale, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                            launch { animOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                            launch { animOffsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                        }
                    },
                    onResetZoom = {
                        zoomPillText = "Original"
                        coroutineScope.launch {
                            launch { animScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                            launch { animOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                            launch { animOffsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                        }
                    },
                    zoomPillText = zoomPillText,
                    onClearZoomPill = { zoomPillText = null },
                    fillScale = fillScale,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            // STANDARD YOUTUBE WATCH SCREEN (16:9 Player on top + details + scrollable related list)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                // Top 16:9 Sticky Player
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                        .clipToBounds(),
                ) {
                    val showVideo = currentVideo != null && ui.videoId == currentVideo.videoId
                    if (showVideo) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    useController = false
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            update = { view ->
                                val player = viewModel.attachedPlayer()
                                if (view.player !== player) view.player = player
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = animScale.value
                                    scaleY = animScale.value
                                    translationX = animOffsetX.value
                                    translationY = animOffsetY.value
                                },
                        )
                    } else {
                        // Poster image while resolving
                        AsyncImage(
                            model = currentVideo?.thumbnailUrl,
                            contentDescription = currentVideo?.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    if (isResolving || (ui.isBuffering && showVideo)) {
                        CircularProgressIndicator(
                            color = RivaL.red(dark),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(40.dp),
                            strokeWidth = 3.dp,
                        )
                    }

                    // YouTube Player Overlay Controls
                    YouTubePlayerOverlay(
                        dark = dark,
                        ui = ui,
                        progressFlow = viewModel.progress,
                        isFullscreen = false,
                        onTogglePlay = viewModel::togglePlayPause,
                        onSeek = viewModel::seekTo,
                        onSeekRelative = viewModel::seekRelative,
                        onClose = onMinimize,
                        onMinimize = onMinimize,
                        onToggleFullscreen = { isFullscreen = true },
                        onToggleSubtitles = viewModel::toggleSubtitles,
                        onOpenSubtitleDialog = { showSubtitleDialog = true },
                        onOpenAudioDialog = { showAudioLanguageDialog = true },
                        onOpenQualityDialog = { showQualityDialog = true },
                        onSelectSpeed = viewModel::setPlaybackSpeed,
                        onRetry = { viewModel.retryCurrentVideo(currentIndex) },
                        zoomScale = animScale.value,
                        zoomOffset = androidx.compose.ui.geometry.Offset(animOffsetX.value, animOffsetY.value),
                        onZoomChange = { scale, offset ->
                            coroutineScope.launch {
                                animScale.snapTo(scale)
                                animOffsetX.snapTo(offset.x)
                                animOffsetY.snapTo(offset.y)
                            }
                            zoomPillText = when {
                                scale <= 1.05f -> "Original"
                                kotlin.math.abs(scale - fillScale) < 0.08f -> "Zoomed to fill"
                                scale >= 3.95f -> "Max Zoom (4.0x)"
                                else -> String.format(java.util.Locale.US, "%.1fx", scale)
                            }
                        },
                        onZoomFill = {
                            zoomPillText = "Zoomed to fill"
                            coroutineScope.launch {
                                launch { animScale.animateTo(fillScale, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                                launch { animOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                                launch { animOffsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                            }
                        },
                        onResetZoom = {
                            zoomPillText = "Original"
                            coroutineScope.launch {
                                launch { animScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                                launch { animOffsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                                launch { animOffsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) }
                            }
                        },
                        zoomPillText = zoomPillText,
                        onClearZoomPill = { zoomPillText = null },
                        fillScale = fillScale,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Scrollable Content Beneath Player (LazyColumn for buttery smooth 60/120 FPS recycling)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 32.dp),
                ) {
                    currentVideo?.let { video ->
                        item(key = "title") {
                            Text(
                                text = video.title,
                                color = RivaL.text(dark),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp),
                            )
                        }

                        item(key = "metadata") {
                            Text(
                                text = "${video.viewCount ?: "1.4M views"} • ${video.publishedTime ?: "2 weeks ago"}",
                                color = RivaL.textDim(dark),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                            )
                        }

                        item(key = "channel") {
                            Spacer(Modifier.height(8.dp))
                            // Channel Info & Subscribe Button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onChannelClick(video.channelId, video.channel) },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (!video.channelThumbnailUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = video.channelThumbnailUrl,
                                            contentDescription = video.channel,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape),
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(RivaL.pill(dark)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = video.channel.firstOrNull()?.uppercase() ?: "Y",
                                                color = RivaL.text(dark),
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = video.channel,
                                                color = RivaL.text(dark),
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Verified",
                                                tint = RivaL.textDim(dark),
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                        Text(
                                            text = "1.5M subscribers",
                                            color = RivaL.textDim(dark),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }

                                // Subscribe Button (YouTube Pill Style)
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (ui.isSubscribed) RivaL.pill(dark) else RivaL.text(dark),
                                    modifier = Modifier.clickable { viewModel.toggleSubscribe() },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (ui.isSubscribed) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = RivaL.text(dark),
                                                modifier = Modifier.size(16.dp),
                                            )
                                            Spacer(Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = if (ui.isSubscribed) "Subscribed" else "Subscribe",
                                            color = if (ui.isSubscribed) RivaL.text(dark) else RivaL.bg(dark),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                }
                            }
                        }

                        item(key = "actions") {
                            Spacer(Modifier.height(14.dp))
                            // Action Buttons Bar (Always shows Like, CC, Audio Language, Quality, Share, Download)
                            YouTubeActionButtonsRow(
                                dark = dark,
                                ui = ui,
                                isSavedOffline = viewModel.isVideoSavedOffline(video.videoId),
                                onToggleLike = viewModel::toggleLike,
                                onToggleDislike = viewModel::toggleDislike,
                                onToggleSaveOffline = { viewModel.toggleSaveOffline(video) },
                                onOpenLanguage = { showAudioLanguageDialog = true },
                                onOpenSubtitles = { showSubtitleDialog = true },
                                onOpenQuality = { showQualityDialog = true },
                            )
                        }

                        item(key = "description_box") {
                            Spacer(Modifier.height(14.dp))
                            var descExpanded by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RivaL.pill(dark))
                                    .clickable { descExpanded = !descExpanded }
                                    .padding(12.dp),
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${video.viewCount ?: "1.4M views"} • ${video.publishedTime ?: "recently"}",
                                            color = RivaL.text(dark),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "#trending #video",
                                            color = RivaL.red(dark),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = video.title,
                                        color = RivaL.text(dark),
                                        fontSize = 12.sp,
                                        maxLines = if (descExpanded) 20 else 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 16.sp,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (descExpanded) "Show less" else "...more",
                                        color = RivaL.textDim(dark),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }

                        item(key = "comments") {
                            Spacer(Modifier.height(14.dp))
                            // Comments Section Teaser
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RivaL.pill(dark))
                                    .padding(12.dp),
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Comments",
                                            fontWeight = FontWeight.Bold,
                                            color = RivaL.text(dark),
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "2.8K",
                                            color = RivaL.textDim(dark),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Awesome video in crystal clear HD! 🔥",
                                        color = RivaL.text(dark),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }

                        item(key = "upnext_header") {
                            Spacer(Modifier.height(16.dp))
                            // Up Next / Related Videos Header
                            Text(
                                text = "Up next",
                                color = RivaL.text(dark),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            )
                        }

                        // Up next videos list (Lazy rendering)
                        itemsIndexed(feed, key = { _, item -> "related_${item.videoId}" }) { idx, related ->
                            if (idx != currentIndex) {
                                YouTubeRelatedVideoCard(
                                    item = related,
                                    dark = dark,
                                    onClick = { onSelectRelated(idx) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// ---------------- YOUTUBE PLAYER OVERLAY CONTROLS ----------------

@Composable
private fun YouTubePlayerOverlay(
    dark: Boolean,
    ui: VideoUiState,
    progressFlow: StateFlow<PlaybackProgress>,
    isFullscreen: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekRelative: (Long) -> Unit,
    onClose: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onToggleSubtitles: () -> Unit,
    onOpenSubtitleDialog: () -> Unit,
    onOpenAudioDialog: () -> Unit = {},
    onOpenQualityDialog: () -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onRetry: () -> Unit,
    zoomScale: Float = 1f,
    zoomOffset: androidx.compose.ui.geometry.Offset = androidx.compose.ui.geometry.Offset.Zero,
    onZoomChange: ((Float, androidx.compose.ui.geometry.Offset) -> Unit)? = null,
    onZoomFill: (() -> Unit)? = null,
    onResetZoom: (() -> Unit)? = null,
    zoomPillText: String? = null,
    onClearZoomPill: (() -> Unit)? = null,
    fillScale: Float = 1.25f,
    onMinimize: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var speedMenuOpen by remember { mutableStateOf(false) }
    var settingsMenuOpen by remember { mutableStateOf(false) }
    var rewindAnim by remember { mutableStateOf(false) }
    var forwardAnim by remember { mutableStateOf(false) }

    val currentZoomScale by rememberUpdatedState(zoomScale)
    val currentZoomOffset by rememberUpdatedState(zoomOffset)
    val currentOnZoomChange by rememberUpdatedState(onZoomChange)
    val currentOnZoomFill by rememberUpdatedState(onZoomFill)
    val currentOnResetZoom by rememberUpdatedState(onResetZoom)
    val currentOnSeekRelative by rememberUpdatedState(onSeekRelative)
    val currentOnToggleFullscreen by rememberUpdatedState(onToggleFullscreen)
    val currentOnMinimize by rememberUpdatedState(onMinimize)

    // Auto-hide controls after 3.5 seconds when playing
    LaunchedEffect(controlsVisible, ui.isPlaying, lastInteraction) {
        if (controlsVisible && ui.isPlaying) {
            delay(3500)
            controlsVisible = false
        }
    }

    LaunchedEffect(zoomPillText) {
        if (zoomPillText != null) {
            delay(1400)
            onClearZoomPill?.invoke()
        }
    }

    LaunchedEffect(rewindAnim) {
        if (rewindAnim) {
            delay(650)
            rewindAnim = false
        }
    }

    LaunchedEffect(forwardAnim) {
        if (forwardAnim) {
            delay(650)
            forwardAnim = false
        }
    }

    Box(
        modifier = modifier
            .pointerInput(isFullscreen) {
                var lastTapTime = 0L
                var lastTapPos = androidx.compose.ui.geometry.Offset.Zero

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downTime = System.currentTimeMillis()
                    val downPos = down.position
                    var isDrag = false
                    var totalDragY = 0f
                    var isPinch = false
                    var pinchStartScale = currentZoomScale
                    var prevPinchDist = -1f
                    var prevCentroid = androidx.compose.ui.geometry.Offset.Zero
                    var currentScale = currentZoomScale
                    var currentOff = currentZoomOffset
                    val dragStartOffset = currentZoomOffset

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedChanges = event.changes.filter { it.pressed }

                        if (pressedChanges.isEmpty()) {
                            val upTime = System.currentTimeMillis()
                            if (isPinch) {
                                if (pinchStartScale <= 1.05f) {
                                    // Outward pinch from original snaps directly to Zoomed to fill!
                                    if (currentScale > 1.05f && currentScale <= fillScale * 1.15f) {
                                        currentOnZoomFill?.invoke()
                                    } else if (currentScale > fillScale * 1.15f) {
                                        currentOnZoomChange?.invoke(currentScale, currentOff)
                                    } else {
                                        currentOnResetZoom?.invoke()
                                    }
                                } else {
                                    // Inward pinch from Zoomed to fill snaps back to Original!
                                    if (currentScale < pinchStartScale * 0.96f || currentScale < fillScale * 0.95f) {
                                        currentOnResetZoom?.invoke()
                                    } else if (kotlin.math.abs(currentScale - fillScale) < 0.20f) {
                                        currentOnZoomFill?.invoke()
                                    } else {
                                        currentOnZoomChange?.invoke(currentScale, currentOff)
                                    }
                                }
                            } else if (isDrag) {
                                if (currentScale > 1.05f) {
                                    // Panned while zoomed - offset already applied
                                } else {
                                    if (!isFullscreen) {
                                        if (totalDragY < -40f) {
                                            currentOnToggleFullscreen()
                                        } else if (totalDragY > 40f) {
                                            currentOnMinimize?.invoke()
                                        }
                                    } else {
                                        if (totalDragY > 40f) {
                                            currentOnToggleFullscreen()
                                        }
                                    }
                                }
                            } else if (upTime - downTime < 300) {
                                val dist = (downPos - lastTapPos).getDistance()
                                val isDouble = (upTime - lastTapTime < 350) && dist < 140f
                                if (isDouble) {
                                    lastTapTime = 0L
                                    lastInteraction = System.currentTimeMillis()
                                    if (isFullscreen) {
                                        if (downPos.x in (size.width * 0.3f)..(size.width * 0.7f)) {
                                            if (currentScale <= 1.08f) {
                                                currentOnZoomFill?.invoke()
                                            } else {
                                                currentOnResetZoom?.invoke()
                                            }
                                        } else if (downPos.x < size.width * 0.3f) {
                                            currentOnSeekRelative(-10_000L)
                                            rewindAnim = true
                                            forwardAnim = false
                                        } else {
                                            currentOnSeekRelative(10_000L)
                                            forwardAnim = true
                                            rewindAnim = false
                                        }
                                    } else {
                                        if (currentScale > 1.05f) {
                                            currentOnResetZoom?.invoke()
                                        } else {
                                            if (downPos.x < size.width * 0.5f) {
                                                currentOnSeekRelative(-10_000L)
                                                rewindAnim = true
                                                forwardAnim = false
                                            } else {
                                                currentOnSeekRelative(10_000L)
                                                forwardAnim = true
                                                rewindAnim = false
                                            }
                                        }
                                    }
                                } else {
                                    lastTapTime = upTime
                                    lastTapPos = downPos
                                    controlsVisible = !controlsVisible
                                    lastInteraction = System.currentTimeMillis()
                                }
                            }
                            break
                        } else if (pressedChanges.size >= 2 && currentOnZoomChange != null) {
                            // Multi-touch: PINCH TO ZOOM & PAN
                            if (!isPinch) {
                                isPinch = true
                                pinchStartScale = currentScale
                            }
                            controlsVisible = false
                            val p0 = pressedChanges[0].position
                            val p1 = pressedChanges[1].position
                            val dist = (p0 - p1).getDistance()
                            val centroid = (p0 + p1) / 2f

                            if (prevPinchDist > 0f) {
                                val zoomFactor = dist / prevPinchDist
                                val panDelta = centroid - prevCentroid
                                val newScale = (currentScale * zoomFactor).coerceIn(1.0f, 4.0f)

                                val maxOffsetX = (size.width.toFloat() * (newScale - 1f)) / 2f
                                val maxOffsetY = (size.height.toFloat() * (newScale - 1f)) / 2f
                                val newOffX = (currentOff.x + panDelta.x).coerceIn(-maxOffsetX, maxOffsetX)
                                val newOffY = (currentOff.y + panDelta.y).coerceIn(-maxOffsetY, maxOffsetY)
                                val newOffset = androidx.compose.ui.geometry.Offset(newOffX, newOffY)

                                currentScale = newScale
                                currentOff = newOffset
                                currentOnZoomChange?.invoke(newScale, newOffset)
                            }
                            prevPinchDist = dist
                            prevCentroid = centroid
                            event.changes.forEach { it.consume() }
                        } else if (pressedChanges.size == 1 && !isPinch) {
                            val change = pressedChanges[0]
                            val delta = change.position - downPos
                            val deltaDistance = delta.getDistance()

                            if (currentScale > 1.05f && currentOnZoomChange != null) {
                                if (deltaDistance > 10f) {
                                    isDrag = true
                                    val maxOffsetX = (size.width.toFloat() * (currentScale - 1f)) / 2f
                                    val maxOffsetY = (size.height.toFloat() * (currentScale - 1f)) / 2f
                                    val targetOffX = (dragStartOffset.x + delta.x).coerceIn(-maxOffsetX, maxOffsetX)
                                    val targetOffY = (dragStartOffset.y + delta.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    val newOffset = androidx.compose.ui.geometry.Offset(targetOffX, targetOffY)
                                    currentOff = newOffset
                                    currentOnZoomChange?.invoke(currentScale, newOffset)
                                    change.consume()
                                }
                            } else {
                                val dragY = change.position.y - downPos.y
                                if (kotlin.math.abs(dragY) > 25f) {
                                    isDrag = true
                                    totalDragY = dragY
                                }
                            }
                        }
                    }
                }
            },
    ) {
        // Error Card with Retry Button
        ui.error?.let { errorMsg ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE6141414))
                    .padding(16.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Playback Error",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = errorMsg,
                        color = Color(0xFFCCCCCC),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = RivaL.red(dark)),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retry", fontSize = 13.sp)
                    }
                }
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
            ) {
                // Top Bar Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Dismiss",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    if (isFullscreen) {
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (zoomScale > 1.05f) Color(0x99CC0000) else Color(0x66000000),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    lastInteraction = System.currentTimeMillis()
                                    if (zoomScale > 1.05f) currentOnResetZoom?.invoke() else currentOnZoomFill?.invoke()
                                }
                                .padding(horizontal = 2.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = if (zoomScale > 1.05f) Icons.Filled.ZoomOutMap else Icons.Filled.Fullscreen,
                                    contentDescription = if (zoomScale > 1.05f) "Original" else "Fit to display",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (zoomScale > 1.05f) "Original" else "Fit to display",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    } else if (zoomScale > 1.05f && onResetZoom != null) {
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x99CC0000),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    lastInteraction = System.currentTimeMillis()
                                    currentOnResetZoom?.invoke()
                                }
                                .padding(horizontal = 2.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.ZoomOutMap, contentDescription = "Reset Zoom", tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Original", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Subtitle / Closed-Caption (CC) Toggle Button
                    IconButton(
                        onClick = {
                            lastInteraction = System.currentTimeMillis()
                            onToggleSubtitles()
                        },
                    ) {
                        Icon(
                            imageVector = if (ui.isSubtitlesEnabled) Icons.Filled.ClosedCaption else Icons.Filled.ClosedCaptionOff,
                            contentDescription = "Subtitles",
                            tint = if (ui.isSubtitlesEnabled) Color(0xFF3EA6FF) else Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    // Quality Badge (CLICKABLE TO SWITCH RESOLUTION: 1080p, 720p, etc.)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x66FFFFFF),
                        modifier = Modifier
                            .clickable {
                                lastInteraction = System.currentTimeMillis()
                                onOpenQualityDialog()
                            }
                            .padding(horizontal = 2.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.HighQuality,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = ui.qualityLabel ?: "HD",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    // YouTube Settings Gear Menu
                    Box {
                        IconButton(
                            onClick = {
                                lastInteraction = System.currentTimeMillis()
                                settingsMenuOpen = true
                            },
                        ) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = settingsMenuOpen,
                            onDismissRequest = { settingsMenuOpen = false },
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.HighQuality, contentDescription = null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Text("Quality • ${ui.qualityLabel ?: "HD"}")
                                    }
                                },
                                onClick = {
                                    settingsMenuOpen = false
                                    onOpenQualityDialog()
                                },
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.ClosedCaption, contentDescription = null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Text("Subtitles • " + if (ui.isSubtitlesEnabled) (ui.selectedSubtitleLang ?: "On") else "Off")
                                    }
                                },
                                onClick = {
                                    settingsMenuOpen = false
                                    onOpenSubtitleDialog()
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Speed, contentDescription = null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(10.dp))
                                        Text("Playback Speed • ${ui.playbackSpeed}x")
                                    }
                                },
                                onClick = {
                                    settingsMenuOpen = false
                                    speedMenuOpen = true
                                },
                            )
                        }

                        // Speed Submenu
                        DropdownMenu(
                            expanded = speedMenuOpen,
                            onDismissRequest = { speedMenuOpen = false },
                        ) {
                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${speed}x" + if (speed == ui.playbackSpeed) " ✓" else "",
                                            fontWeight = if (speed == ui.playbackSpeed) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        onSelectSpeed(speed)
                                        speedMenuOpen = false
                                    },
                                )
                            }
                        }
                    }

                    // Fullscreen Toggle
                    IconButton(onClick = onToggleFullscreen) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }

                // Center Controls (10s Rewind, Play/Pause, 10s Forward)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    IconButton(onClick = {
                        lastInteraction = System.currentTimeMillis()
                        onSeekRelative(-10_000L)
                    }) {
                        Icon(
                            Icons.Filled.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }

                    // YouTube Play/Pause Circle
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC000000))
                            .border(1.5.dp, Color(0x66FFFFFF), CircleShape)
                            .clickable(onClick = {
                                lastInteraction = System.currentTimeMillis()
                                onTogglePlay()
                            }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (ui.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp),
                        )
                    }

                    IconButton(onClick = {
                        lastInteraction = System.currentTimeMillis()
                        onSeekRelative(10_000L)
                    }) {
                        Icon(
                            Icons.Filled.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                // Bottom Seek Bar & Duration (Isolated to prevent full-screen recomposition lag)
                PlayerProgressBar(
                    progressFlow = progressFlow,
                    dark = dark,
                    isFullscreen = isFullscreen,
                    onSeek = onSeek,
                    onToggleFullscreen = onToggleFullscreen,
                    onInteraction = { lastInteraction = System.currentTimeMillis() },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }

        // 10s Rewind Ripple Overlay (Left Half)
        AnimatedVisibility(
            visible = rewindAnim,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 1.15f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Replay10,
                        contentDescription = "-10 seconds",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "10 seconds",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // 10s Fast Forward Ripple Overlay (Right Half)
        AnimatedVisibility(
            visible = forwardAnim,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 1.15f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Forward10,
                        contentDescription = "+10 seconds",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "10 seconds",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Floating YouTube Zoom Pill (Center Top)
        AnimatedVisibility(
            visible = !zoomPillText.isNullOrEmpty(),
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (isFullscreen) 24.dp else 12.dp),
        ) {
            zoomPillText?.let { label ->
                Surface(
                    color = Color(0xCC181818),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0x40FFFFFF)),
                    shadowElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ZoomIn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

// ---------------- ISOLATED PROGRESS & SEEK BAR (60/120 FPS FLUID) ----------------

@Composable
private fun PlayerProgressBar(
    progressFlow: StateFlow<PlaybackProgress>,
    dark: Boolean,
    isFullscreen: Boolean,
    onSeek: (Long) -> Unit,
    onToggleFullscreen: () -> Unit,
    onInteraction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by progressFlow.collectAsStateWithLifecycle()
    var dragging by remember { mutableStateOf(false) }
    var dragProgressMs by remember { mutableLongStateOf(0L) }
    val shownPosition = if (dragging) dragProgressMs else progress.positionMs
    val total = progress.durationMs

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xEE000000))),
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        if (total > 0L) {
            val playedFrac = (shownPosition.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            val bufferedFrac = (progress.bufferedPositionMs.toFloat() / total.toFloat()).coerceIn(0f, 1f)

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .pointerInput(total) {
                        detectTapGestures { offset ->
                            val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            onSeek((frac * total).toLong())
                            onInteraction()
                        }
                    }
                    .pointerInput(total) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                dragging = true
                                val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                dragProgressMs = (frac * total).toLong()
                                onInteraction()
                            },
                            onDragEnd = {
                                onSeek(dragProgressMs)
                                dragging = false
                                onInteraction()
                            },
                            onDragCancel = {
                                dragging = false
                            },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                val frac = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                dragProgressMs = (frac * total).toLong()
                                onInteraction()
                            },
                        )
                    },
                contentAlignment = Alignment.CenterStart,
            ) {
                val fullWidth = maxWidth
                val trackH = if (dragging) 3.5.dp else 2.dp

                // Inactive track (ultra-slim)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(trackH)
                        .clip(RoundedCornerShape(trackH / 2))
                        .background(Color(0x33FFFFFF)),
                )

                // Buffered track (ultra-slim)
                if (bufferedFrac > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(bufferedFrac)
                            .height(trackH)
                            .clip(RoundedCornerShape(trackH / 2))
                            .background(Color(0x66FFFFFF)),
                    )
                }

                // Played track (pure YouTube red, ultra-slim)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(playedFrac)
                        .height(trackH)
                        .clip(RoundedCornerShape(trackH / 2))
                        .background(Color(0xFFFF0000)),
                )

                // Thumb knob (only when actively dragging/scrubbing, hidden during normal playback)
                if (dragging) {
                    Box(
                        modifier = Modifier
                            .padding(start = ((fullWidth * playedFrac) - 5.dp).coerceAtLeast(0.dp))
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF0000)),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${formatDuration(shownPosition)} / ${formatDuration(total)}",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = onToggleFullscreen,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ---------------- YOUTUBE ACTION BUTTONS ROW ----------------

@Composable
private fun YouTubeActionButtonsRow(
    dark: Boolean,
    ui: VideoUiState,
    isSavedOffline: Boolean,
    onToggleLike: () -> Unit,
    onToggleDislike: () -> Unit,
    onToggleSaveOffline: () -> Unit,
    onOpenLanguage: () -> Unit = {},
    onOpenSubtitles: () -> Unit,
    onOpenQuality: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Like / Dislike Pill
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = RivaL.pill(dark),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.clickable(onClick = onToggleLike),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ThumbUp,
                            contentDescription = "Like",
                            tint = if (ui.isLiked) RivaL.red(dark) else RivaL.text(dark),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (ui.isLiked) "Liked" else "142K",
                            color = RivaL.text(dark),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(16.dp)
                            .background(RivaL.textDim(dark).copy(alpha = 0.3f)),
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Filled.ThumbDown,
                        contentDescription = "Dislike",
                        tint = if (ui.isDisliked) RivaL.red(dark) else RivaL.text(dark),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onToggleDislike),
                    )
                }
            }
        }

        // Save for Offline / Download Pill
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSavedOffline) RivaL.text(dark) else RivaL.pill(dark),
                modifier = Modifier.clickable(onClick = onToggleSaveOffline),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (isSavedOffline) Icons.Filled.DownloadDone else Icons.Filled.Download,
                        contentDescription = null,
                        tint = if (isSavedOffline) RivaL.bg(dark) else RivaL.text(dark),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isSavedOffline) "Saved" else "Download",
                        color = if (isSavedOffline) RivaL.bg(dark) else RivaL.text(dark),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Subtitles (CC) Button
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (ui.isSubtitlesEnabled) RivaL.text(dark) else RivaL.pill(dark),
                modifier = Modifier.clickable(onClick = onOpenSubtitles),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.ClosedCaption,
                        contentDescription = null,
                        tint = if (ui.isSubtitlesEnabled) RivaL.bg(dark) else RivaL.text(dark),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (ui.isSubtitlesEnabled) "CC: ${ui.selectedSubtitleLang?.uppercase() ?: "ON"}" else "CC (Subtitles)",
                        color = if (ui.isSubtitlesEnabled) RivaL.bg(dark) else RivaL.text(dark),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // HD Quality Pill Button
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = RivaL.pill(dark),
                modifier = Modifier.clickable(onClick = onOpenQuality),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.HighQuality,
                        contentDescription = null,
                        tint = RivaL.text(dark),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = ui.qualityLabel ?: "HD Quality",
                        color = RivaL.text(dark),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Share Button
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = RivaL.pill(dark),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = null,
                        tint = RivaL.text(dark),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        color = RivaL.text(dark),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

    }
}

// ---------------- YOUTUBE RELATED / SUGGESTED VIDEO CARD ----------------

@Composable
private fun YouTubeRelatedVideoCard(
    item: VideoItem,
    dark: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .width(136.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(RivaL.panelMid(dark)),
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xE6000000))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(
                    text = formatDuration(item.durationMs),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = RivaL.text(dark),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = item.channel,
                color = RivaL.textDim(dark),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${item.viewCount ?: "950K views"} • ${item.publishedTime ?: "recently"}",
                color = RivaL.textDim(dark),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
            )
        }
    }
}

// ---------------- VIDEO QUALITY SELECTION DIALOG (HD 1080p, 720p, etc.) ----------------

@Composable
private fun VideoQualityDialog(
    dark: Boolean,
    availableQualities: List<VideoQualityTrack>,
    selectedQuality: String?,
    onSelect: (VideoQualityTrack) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.HighQuality,
                    contentDescription = null,
                    tint = RivaL.red(dark),
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Video Quality",
                    fontWeight = FontWeight.Bold,
                    color = RivaL.text(dark),
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (availableQualities.isEmpty()) {
                    Text(
                        text = "Auto HD (Current: ${selectedQuality ?: "720p HD"})",
                        color = RivaL.text(dark),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 10.dp),
                    )
                } else {
                    availableQualities.forEach { q ->
                        val isSelected = selectedQuality == q.qualityLabel || (selectedQuality?.contains(q.qualityLabel) == true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(q) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = q.qualityLabel,
                                    color = if (isSelected) RivaL.red(dark) else RivaL.text(dark),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                )
                                Text(
                                    text = if (q.height >= 720) "High Definition • Smooth 60fps" else "Standard Definition",
                                    color = RivaL.textDim(dark),
                                    fontSize = 11.sp,
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = RivaL.red(dark))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RivaL.text(dark))
            }
        },
        containerColor = RivaL.panel(dark),
    )
}


// ---------------- SUBTITLE LANGUAGE SELECTION DIALOG ----------------

@Composable
private fun SubtitleLanguageDialog(
    dark: Boolean,
    availableSubtitles: List<SubtitleTrack>,
    selectedLang: String?,
    isSubtitlesEnabled: Boolean,
    onSelect: (SubtitleTrack?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Subtitles / Captions (CC)",
                fontWeight = FontWeight.Bold,
                color = RivaL.text(dark),
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Turn OFF option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(null) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Turn Off Subtitles",
                        color = if (!isSubtitlesEnabled) RivaL.red(dark) else RivaL.text(dark),
                        fontWeight = if (!isSubtitlesEnabled) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                    )
                    if (!isSubtitlesEnabled) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = RivaL.red(dark))
                    }
                }

                if (availableSubtitles.isEmpty()) {
                    Text(
                        text = "No additional subtitle tracks available for this video.",
                        color = RivaL.textDim(dark),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    availableSubtitles.forEach { sub ->
                        val isSelected = isSubtitlesEnabled && selectedLang == sub.languageCode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(sub) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = sub.name,
                                color = if (isSelected) RivaL.red(dark) else RivaL.text(dark),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                            )
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = RivaL.red(dark))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RivaL.text(dark))
            }
        },
        containerColor = RivaL.panel(dark),
    )
}


// ---------------- AUDIO LANGUAGE SELECTION DIALOG ----------------

@Composable
private fun AudioLanguageDialog(
    dark: Boolean,
    availableTracks: List<AudioLanguageTrack>,
    selectedName: String?,
    onSelect: (AudioLanguageTrack) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Audiotrack,
                    contentDescription = null,
                    tint = RivaL.red(dark),
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Audio Language",
                    fontWeight = FontWeight.Bold,
                    color = RivaL.text(dark),
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (availableTracks.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Original Audio (Default)",
                            color = RivaL.red(dark),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(Icons.Filled.Check, contentDescription = null, tint = RivaL.red(dark))
                    }
                    Text(
                        text = "Is video ke liye YouTube par additional dubbed audio tracks available nahi hain.",
                        color = RivaL.textDim(dark),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                } else {
                    availableTracks.forEach { track ->
                        val isSelected = selectedName == track.name || (selectedName == null && track.id == "default")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(track) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = track.name,
                                color = if (isSelected) RivaL.red(dark) else RivaL.text(dark),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                            )
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = RivaL.red(dark))
                            }
                        }
                    }
                }

                if (availableTracks.size <= 1) {
                    Spacer(Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = RivaL.pill(dark)),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 YouTube par har video me multiple audio tracks nahi hote. Agar creator ne multiple dubbed audios upload ki hain to yahan list hoti hain.",
                                color = RivaL.textDim(dark),
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RivaL.text(dark))
            }
        },
        containerColor = RivaL.panel(dark),
    )
}

// ---------------- YOUTUBE SHORTS SCREEN (FULLSCREEN VERTICAL PAGER) ----------------

@Composable
private fun YouTubeShortsScreen(
    dark: Boolean,
    viewModel: RivaVideoViewModel,
    shorts: List<ShortItem>,
    loading: Boolean,
    onChannelClick: (String?, String?) -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val isResolving by viewModel.isResolving.collectAsStateWithLifecycle()
    val activeIndex by viewModel.activeShortIndex.collectAsStateWithLifecycle()

    if (loading && shorts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = RivaL.red(dark), strokeWidth = 3.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Loading Shorts… ⚡",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        return
    }

    if (shorts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp),
            ) {
                Text(
                    text = "Koi Shorts nahi mili",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.loadShorts() },
                    colors = ButtonDefaults.buttonColors(containerColor = RivaL.red(dark)),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Retry")
                }
            }
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = activeIndex.coerceIn(0, shorts.lastIndex),
        pageCount = { shorts.size },
    )

    LaunchedEffect(pagerState.currentPage) {
        viewModel.switchShort(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
        ) { page ->
            val short = shorts[page]
            val isCurrent = page == pagerState.currentPage

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { viewModel.togglePlayPause() },
                        )
                    },
            ) {
                // Video Surface or Poster Thumbnail
                if (isCurrent && ui.videoId == short.videoId) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            }
                        },
                        update = { view ->
                            val player = viewModel.attachedPlayer()
                            if (view.player !== player) view.player = player
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    AsyncImage(
                        model = short.thumbnailUrl,
                        contentDescription = short.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Loading / Buffering Spinner
                if (isCurrent && (isResolving || ui.isBuffering)) {
                    CircularProgressIndicator(
                        color = RivaL.red(dark),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(44.dp),
                        strokeWidth = 3.dp,
                    )
                }

                // Paused overlay icon
                if (isCurrent && !ui.isPlaying && !ui.isBuffering && !isResolving) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                // Top Header Overlay ("Shorts" title)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Shorts",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.loadShorts() }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                // Bottom Gradient Scrim for readable text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xD9000000)),
                            ),
                        ),
                )

                // Right Floating Action Buttons Column
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Creator Avatar (Clickable to open channel)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                            .clickable { onChannelClick(short.channelId, short.channel) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!short.channelAvatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = short.channelAvatarUrl,
                                contentDescription = short.channel,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(RivaL.red(dark)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = short.channel.firstOrNull()?.uppercase() ?: "Y",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                )
                            }
                        }
                    }

                    // Like Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.toggleShortLike(page) },
                    ) {
                        Icon(
                            imageVector = if (short.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (short.isLiked) Color(0xFFFF2D55) else Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (short.isLiked) "Liked" else short.likesCount,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    // Comments Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Comment,
                            contentDescription = "Comments",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = short.commentsCount,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    // Share Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Share",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    // Audio Vinyl Disc Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262626))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Audiotrack,
                            contentDescription = "Audio",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Bottom Left Channel & Video Details
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 14.dp, end = 76.dp, bottom = 24.dp),
                ) {
                    // Channel Handle & Subscribe Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onChannelClick(short.channelId, short.channel) },
                    ) {
                        Text(
                            text = "@${short.channel.replace(" ", "")}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = RivaL.red(dark),
                            modifier = Modifier.clickable { viewModel.toggleSubscribe() },
                        ) {
                            Text(
                                text = if (ui.isSubscribed) "Subscribed" else "Subscribe",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Short Title
                    Text(
                        text = short.title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                    )

                    Spacer(Modifier.height(8.dp))

                    // Audio track info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Audiotrack,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Original sound - ${short.channel}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Thin progress line at the bottom
                if (isCurrent) {
                    val progress by viewModel.progress.collectAsStateWithLifecycle()
                    val fraction = if (progress.durationMs > 0L) {
                        (progress.positionMs.toFloat() / progress.durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomStart)
                            .background(Color.White.copy(alpha = 0.2f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(2.dp)
                                .background(RivaL.red(dark)),
                        )
                    }
                }
            }
        }
    }
}

// ---------------- YOUTUBE BOTTOM NAVIGATION BAR ----------------

@Composable
private fun YouTubeBottomNavBar(
    currentTab: RivaVideoViewModel.AppTab,
    onSelectTab: (RivaVideoViewModel.AppTab) -> Unit,
    dark: Boolean,
) {
    Surface(
        color = RivaL.bg(dark),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, RivaL.pill(dark)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 6.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Home
                val isHome = currentTab == RivaVideoViewModel.AppTab.HOME
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(RivaVideoViewModel.AppTab.HOME) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Home",
                        tint = if (isHome) RivaL.red(dark) else RivaL.textDim(dark),
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "Home",
                        color = if (isHome) RivaL.text(dark) else RivaL.textDim(dark),
                        fontSize = 11.sp,
                        fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
                    )
                }

                // Shorts
                val isShorts = currentTab == RivaVideoViewModel.AppTab.SHORTS
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(RivaVideoViewModel.AppTab.SHORTS) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlashOn,
                        contentDescription = "Shorts",
                        tint = if (isShorts) RivaL.red(dark) else RivaL.textDim(dark),
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "Shorts",
                        color = if (isShorts) RivaL.text(dark) else RivaL.textDim(dark),
                        fontSize = 11.sp,
                        fontWeight = if (isShorts) FontWeight.Bold else FontWeight.Medium,
                    )
                }

                // Profile / You
                val isProfile = currentTab == RivaVideoViewModel.AppTab.PROFILE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(RivaVideoViewModel.AppTab.PROFILE) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "You",
                        tint = if (isProfile) RivaL.red(dark) else RivaL.textDim(dark),
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "You",
                        color = if (isProfile) RivaL.text(dark) else RivaL.textDim(dark),
                        fontSize = 11.sp,
                        fontWeight = if (isProfile) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}

// ---------------- YOUTUBE CHANNEL SCREEN ----------------

@Composable
private fun YouTubeChannelScreen(
    channel: ChannelDetails,
    loading: Boolean,
    dark: Boolean,
    onClose: () -> Unit,
    onToggleSubscribe: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
) {
    var selectedSortTab by remember { mutableStateOf("Latest") }

    val displayedVideos = remember(channel.videos, selectedSortTab) {
        when (selectedSortTab) {
            "Popular" -> channel.videos.sortedWith(compareByDescending { parseViews(it.viewCount) })
            "Oldest" -> channel.videos.sortedWith(compareByDescending { parseRelativeAgeSeconds(it.publishedTime) })
            else -> channel.videos.sortedWith(compareBy { parseRelativeAgeSeconds(it.publishedTime) }) // "Latest" (New to Old)
        }
    }

    BackHandler(enabled = true) {
        onClose()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RivaL.bg(dark))
            .statusBarsPadding(),
    ) {
        // Channel Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = RivaL.text(dark),
                )
            }
            Text(
                text = channel.title,
                color = RivaL.text(dark),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Share, contentDescription = "Share", tint = RivaL.text(dark))
            }
            IconButton(onClick = {}) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = RivaL.text(dark))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // 1. Channel Banner
            item(key = "channel_banner") {
                if (!channel.bannerUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = channel.bannerUrl,
                        contentDescription = "Channel Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 5f)
                            .background(RivaL.panelMid(dark)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF202020),
                                        Color(0xFF333333),
                                        Color(0xFF202020),
                                    ),
                                ),
                            ),
                    )
                }
            }

            // 2. Channel Avatar, Name, Handle, Subs, Subscribe Button
            item(key = "channel_header_info") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!channel.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = channel.avatarUrl,
                                contentDescription = channel.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, RivaL.bg(dark), CircleShape),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(RivaL.pill(dark)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = channel.title.firstOrNull()?.uppercase() ?: "C",
                                    color = RivaL.text(dark),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = channel.title,
                                color = RivaL.text(dark),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            if (!channel.handle.isNullOrEmpty()) {
                                Text(
                                    text = channel.handle,
                                    color = RivaL.textDim(dark),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Text(
                                text = "${channel.subscriberCount} • ${channel.videoCount ?: ""}",
                                color = RivaL.textDim(dark),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    if (!channel.description.isNullOrEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = channel.description,
                            color = RivaL.textDim(dark),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Subscribe Button (Full Width YouTube Pill)
                    Button(
                        onClick = onToggleSubscribe,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (channel.isSubscribed) RivaL.pill(dark) else RivaL.text(dark),
                            contentColor = if (channel.isSubscribed) RivaL.text(dark) else RivaL.bg(dark),
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                    ) {
                        if (channel.isSubscribed) {
                            Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            text = if (channel.isSubscribed) "Subscribed" else "Subscribe",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Videos",
                        color = RivaL.text(dark),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(Modifier.height(10.dp))

                    // YouTube Style Filter Chips: Latest (New to Old) / Popular / Oldest
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Latest", "Popular", "Oldest").forEach { tab ->
                            val isSelected = selectedSortTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) {
                                            if (dark) Color.White else Color.Black
                                        } else {
                                            if (dark) Color(0xFF272727) else Color(0xFFE5E5E5)
                                        }
                                    )
                                    .clickable { selectedSortTab = tab }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                            ) {
                                Text(
                                    text = tab,
                                    color = if (isSelected) {
                                        if (dark) Color.Black else Color.White
                                    } else {
                                        RivaL.text(dark)
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }

            // 3. Channel Videos
            if (loading && displayedVideos.isEmpty()) {
                item(key = "channel_loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = RivaL.red(dark), strokeWidth = 3.dp)
                    }
                }
            } else if (displayedVideos.isEmpty()) {
                item(key = "channel_empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Koi video nahi mili",
                            color = RivaL.textDim(dark),
                        )
                    }
                }
            } else {
                itemsIndexed(displayedVideos, key = { _, video -> "ch_vid_${video.videoId}" }) { _, video ->
                    YouTubeVideoCard(
                        item = video,
                        dark = dark,
                        onClick = { onVideoClick(video) },
                        onChannelClick = null,
                    )
                }
            }
        }
    }
}

// ---------------- REGION & CONTENT PREFERENCE DIALOG ----------------

@Composable
private fun RegionPreferenceDialog(
    dark: Boolean,
    currentRegion: RegionPreference,
    onSelectRegion: (RegionPreference) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Content Preference",
                color = RivaL.text(dark),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Aap kis type ka content zyada dekhna chahte hain? Isse aapka home feed personalize hoga:",
                    color = RivaL.textDim(dark),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                RegionPreference.values().forEach { pref ->
                    val selected = pref == currentRegion
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) RivaL.red(dark).copy(alpha = 0.15f) else RivaL.pill(dark).copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selected) RivaL.red(dark) else RivaL.pill(dark),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectRegion(pref)
                                onDismiss()
                            },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = pref.flag,
                                fontSize = 24.sp,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pref.title,
                                    color = RivaL.text(dark),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = pref.description,
                                    color = RivaL.textDim(dark),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            if (selected) {
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = RivaL.red(dark),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RivaL.red(dark), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = RivaL.panelMid(dark),
    )
}

// ---------------- YOUTUBE PROFILE & LIBRARY SCREEN ----------------

@Composable
private fun YouTubeProfileScreen(
    dark: Boolean,
    viewModel: RivaVideoViewModel,
) {
    val isBgEnabled by viewModel.isBackgroundPlaybackEnabled.collectAsStateWithLifecycle()
    val regionPreference by viewModel.regionPreference.collectAsStateWithLifecycle()
    val offlineVideos by viewModel.offlineVideos.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RivaL.bg(dark))
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        // 1. Profile Top Bar / User Card
        item(key = "profile_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    RivaL.red(dark),
                                    Color(0xFFFF8A80),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "R",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Riva User",
                        color = RivaL.text(dark),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "@rivavideo • Offline & Ad-Free",
                        color = RivaL.textDim(dark),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        // 2. Background Playback Toggle Card
        item(key = "bg_playback_setting") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RivaL.panelMid(dark)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isBgEnabled) RivaL.red(dark).copy(alpha = 0.2f) else RivaL.pill(dark)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Audiotrack,
                            contentDescription = null,
                            tint = if (isBgEnabled) RivaL.red(dark) else RivaL.textDim(dark),
                            modifier = Modifier.size(22.dp),
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Background Playback",
                            color = RivaL.text(dark),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isBgEnabled) "Video audio background me chalta rahega" else "App minimize hone par video ruk jayegi",
                            color = RivaL.textDim(dark),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    Switch(
                        checked = isBgEnabled,
                        onCheckedChange = { viewModel.toggleBackgroundPlayback() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RivaL.bg(dark),
                            checkedTrackColor = RivaL.red(dark),
                            uncheckedThumbColor = RivaL.textDim(dark),
                            uncheckedTrackColor = RivaL.pill(dark),
                        ),
                    )
                }
            }
        }

        // 3. Content Region Selector Card
        item(key = "region_setting") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RivaL.panelMid(dark)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { viewModel.openRegionPicker() },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = regionPreference.flag,
                        fontSize = 28.sp,
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Content Region",
                                color = RivaL.textDim(dark),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = regionPreference.title,
                            color = RivaL.text(dark),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = regionPreference.description,
                            color = RivaL.textDim(dark),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = RivaL.pill(dark),
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Text(
                            text = "Change",
                            color = RivaL.red(dark),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }

        // 4. Watch History Section (Last 7 Days)
        item(key = "history_header") {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = RivaL.red(dark),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "History (Last 7 Days)",
                    color = RivaL.text(dark),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (watchHistory.isNotEmpty()) {
                    TextButton(onClick = viewModel::clearWatchHistory) {
                        Text(
                            text = "Clear all",
                            color = RivaL.textDim(dark),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        if (watchHistory.isEmpty()) {
            item(key = "history_empty") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RivaL.panelMid(dark))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Koi watch history nahi hai. Jo videos aap dekhenge woh yahan aayengi.",
                        color = RivaL.textDim(dark),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            item(key = "history_row") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(watchHistory, key = { "hist_${it.videoId}_${it.watchedAtMs}" }) { item ->
                        ProfileHistoryCard(
                            item = item,
                            dark = dark,
                            onClick = { viewModel.playHistoryVideo(item) },
                        )
                    }
                }
            }
        }

        // 5. Offline Saved Videos Section
        item(key = "offline_header") {
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.DownloadDone,
                    contentDescription = null,
                    tint = RivaL.red(dark),
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Saved Videos (Offline)",
                    color = RivaL.text(dark),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = RivaL.pill(dark),
                ) {
                    Text(
                        text = "${offlineVideos.size}",
                        color = RivaL.text(dark),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
        }

        if (offlineVideos.isEmpty()) {
            item(key = "offline_empty") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RivaL.panelMid(dark))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.DownloadDone,
                            contentDescription = null,
                            tint = RivaL.textDim(dark),
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Koi offline save video nahi hai.",
                            color = RivaL.text(dark),
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Video player me 'Save' pill dabayein taaki yahan se kabhi bhi chala sakein.",
                            color = RivaL.textDim(dark),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            items(offlineVideos, key = { "offline_${it.videoId}" }) { item ->
                ProfileOfflineVideoCard(
                    item = item,
                    dark = dark,
                    onPlay = { viewModel.playOfflineVideo(item) },
                    onRemove = { viewModel.removeOfflineVideo(item.videoId) },
                )
            }
        }

        // 6. About App Card
        item(key = "app_about") {
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "RivaVideo v1.0.0 (Release)",
                    color = RivaL.textDim(dark),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Modern YouTube Client • Full HD • Ad-Free",
                    color = RivaL.textDim(dark).copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun ProfileHistoryCard(
    item: WatchHistoryItem,
    dark: Boolean,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RivaL.panelMid(dark)),
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black),
            ) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                if (item.durationMs != null && item.durationMs > 0) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                    ) {
                        Text(
                            text = formatDuration(item.durationMs),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.title,
                    color = RivaL.text(dark),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.channel,
                    color = RivaL.textDim(dark),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ProfileOfflineVideoCard(
    item: OfflineVideoItem,
    dark: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RivaL.panelMid(dark)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onPlay),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
            ) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                if (item.durationMs != null && item.durationMs > 0) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                    ) {
                        Text(
                            text = formatDuration(item.durationMs),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = RivaL.text(dark),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.channel,
                    color = RivaL.textDim(dark),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remove",
                    tint = RivaL.textDim(dark),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
