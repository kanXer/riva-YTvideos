package com.nexusdigital.rivavideo.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.nexusdigital.rivavideo.data.AudioLanguageTrack
import com.nexusdigital.rivavideo.data.ChannelDetails
import com.nexusdigital.rivavideo.data.OfflineVideoItem
import com.nexusdigital.rivavideo.data.RegionPreference
import com.nexusdigital.rivavideo.data.ShortItem
import com.nexusdigital.rivavideo.data.SubtitleTrack
import com.nexusdigital.rivavideo.data.VideoItem
import com.nexusdigital.rivavideo.data.VideoRepository
import com.nexusdigital.rivavideo.data.WatchHistoryItem
import com.nexusdigital.rivavideo.data.parseRelativeAgeSeconds
import com.nexusdigital.rivavideo.video.VideoPlayer
import com.nexusdigital.rivavideo.video.VideoUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class PlaybackProgress(
    val positionMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val durationMs: Long = 0L,
)

class RivaVideoViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = VideoRepository()
    private val playerHolder = VideoPlayer(app)
    private val prefs = app.getSharedPreferences("riva_video_prefs", Context.MODE_PRIVATE)
    private val jsonHelper = Json { ignoreUnknownKeys = true }

    private val _ui = MutableStateFlow(VideoUiState())
    val ui: StateFlow<VideoUiState> = _ui.asStateFlow()

    /** Video khatam hone pe 1 tick — UI pager next page par animate karega. */
    private val _endedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    val endedEvents: SharedFlow<Unit> = _endedEvents.asSharedFlow()

    private val _feed = MutableStateFlow<List<VideoItem>>(emptyList())
    val feed: StateFlow<List<VideoItem>> = _feed.asStateFlow()

    private val _feedLoading = MutableStateFlow(false)
    val feedLoading: StateFlow<Boolean> = _feedLoading.asStateFlow()

    private val _feedError = MutableStateFlow<String?>(null)
    val feedError: StateFlow<String?> = _feedError.asStateFlow()

    // Region Preference Setup
    private val savedRegionId = prefs.getString("preferred_region", null)
    private val initialRegion = RegionPreference.entries.find { it.id == savedRegionId } ?: RegionPreference.INDIA

    private val _regionPreference = MutableStateFlow(initialRegion)
    val regionPreference: StateFlow<RegionPreference> = _regionPreference.asStateFlow()

    private val _showRegionPicker = MutableStateFlow(savedRegionId == null)
    val showRegionPicker: StateFlow<Boolean> = _showRegionPicker.asStateFlow()

    private val _availableCategories = MutableStateFlow(getCategoriesForRegion(initialRegion))
    val availableCategories: StateFlow<List<Category>> = _availableCategories.asStateFlow()

    private val _activeCategory = MutableStateFlow(_availableCategories.value.first())
    val activeCategory: StateFlow<Category> = _activeCategory.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    /** Null = watch screen close; non-null = settle index. */
    private val _watchIndex = MutableStateFlow<Int?>(null)
    val watchIndex: StateFlow<Int?> = _watchIndex.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    private val _progress = MutableStateFlow(PlaybackProgress())
    val progress: StateFlow<PlaybackProgress> = _progress.asStateFlow()

    enum class AppTab { HOME, SHORTS, PROFILE }

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _shorts = MutableStateFlow<List<ShortItem>>(emptyList())
    val shorts: StateFlow<List<ShortItem>> = _shorts.asStateFlow()

    private val _shortsLoading = MutableStateFlow(false)
    val shortsLoading: StateFlow<Boolean> = _shortsLoading.asStateFlow()

    private val _activeShortIndex = MutableStateFlow(0)
    val activeShortIndex: StateFlow<Int> = _activeShortIndex.asStateFlow()

    private val _activeChannel = MutableStateFlow<ChannelDetails?>(null)
    val activeChannel: StateFlow<ChannelDetails?> = _activeChannel.asStateFlow()

    private val _channelLoading = MutableStateFlow(false)
    val channelLoading: StateFlow<Boolean> = _channelLoading.asStateFlow()

    // Background Playback Toggle (Default true)
    private val _isBackgroundPlaybackEnabled = MutableStateFlow(prefs.getBoolean("bg_playback_enabled", true))
    val isBackgroundPlaybackEnabled: StateFlow<Boolean> = _isBackgroundPlaybackEnabled.asStateFlow()

    // Offline Saved Videos
    private val _offlineVideos = MutableStateFlow<List<OfflineVideoItem>>(loadOfflineVideosFromPrefs())
    val offlineVideos: StateFlow<List<OfflineVideoItem>> = _offlineVideos.asStateFlow()

    // Watch History (Last 7 Days)
    private val _watchHistory = MutableStateFlow<List<WatchHistoryItem>>(loadWatchHistoryFromPrefs())
    val watchHistory: StateFlow<List<WatchHistoryItem>> = _watchHistory.asStateFlow()

    private var feedJob: Job? = null
    private var resolveJob: Job? = null
    private var loadedVideoId: String? = null

    init {
        exoListeners()
        loadCategory(_availableCategories.value.first())

        // High-frequency Position & buffer ticker isolated from UI state to guarantee 60/120fps smoothness
        viewModelScope.launch {
            while (isActive) {
                val p = playerHolder.exoPlayer
                val pos = p.currentPosition.coerceAtLeast(0L)
                val buf = p.bufferedPosition.coerceAtLeast(0L)
                val dur = p.duration.takeIf { it > 0L } ?: 0L
                _progress.value = PlaybackProgress(
                    positionMs = pos,
                    bufferedPositionMs = buf,
                    durationMs = dur,
                )
                if (dur > 0L && _ui.value.durationMs != dur) {
                    _ui.value = _ui.value.copy(durationMs = dur)
                }
                delay(200)
            }
        }
    }

    private var savedPositionBeforeBackground: Long = 0L
    private var wasPlayingBeforeBackground: Boolean = false

    private fun exoListeners() {
        playerHolder.exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _ui.value = _ui.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _ui.value = _ui.value.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                )
                if (playbackState == Player.STATE_ENDED) {
                    _endedEvents.tryEmit(Unit)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val currentPos = playerHolder.exoPlayer.currentPosition.takeIf { it > 0L }
                    ?: _progress.value.positionMs
                val currentIdx = _watchIndex.value
                // Seamlessly auto-retry from current position on network or stream disconnect
                if (currentIdx != null && currentPos > 0L) {
                    viewModelScope.launch {
                        delay(500)
                        val item = _feed.value.getOrNull(currentIdx)
                        if (item != null && loadedVideoId == item.videoId) {
                            repo.clearCache(item.videoId)
                            loadForPage(currentIdx, forceRefresh = true, startPositionMs = currentPos)
                            return@launch
                        }
                    }
                }
                _ui.value = _ui.value.copy(
                    error = error.message ?: "Playback failed. Dobara try karein.",
                    isBuffering = false,
                )
            }
        })
    }

    fun loadCategory(category: Category) {
        feedJob?.cancel()
        _activeCategory.value = category
        _feedLoading.value = true
        _feedError.value = null
        feedJob = viewModelScope.launch {
            try {
                val videos = repo.searchVideos(category.query)
                _feed.value = videos
                _feedError.value = if (videos.isEmpty()) "Koi video nahi mili" else null
            } catch (t: Throwable) {
                _feedError.value = "Feed load failed: ${t.message}"
            } finally {
                _feedLoading.value = false
            }
        }
    }

    fun setSearchText(value: String) {
        _searchText.value = value
    }

    fun submitSearch() {
        val text = _searchText.value.trim()
        if (text.isEmpty()) return
        loadCategory(Category(label = text, query = text))
    }

    fun openWatch(index: Int) {
        _watchIndex.value = index
    }

    fun closeWatch() {
        resolveJob?.cancel()
        playerHolder.exoPlayer.pause()
        _watchIndex.value = null
        loadedVideoId = null
    }

    /** Settled page ka video resolve + stream playback. */
    fun loadForPage(index: Int, forceRefresh: Boolean = false, startPositionMs: Long = 0L) {
        val item = _feed.value.getOrNull(index) ?: return
        recordWatchHistory(item)
        if (!forceRefresh && loadedVideoId == item.videoId && startPositionMs == 0L) return
        loadedVideoId = item.videoId

        resolveJob?.cancel()
        resolveJob = viewModelScope.launch {
            _isResolving.value = true
            _ui.value = _ui.value.copy(
                videoId = item.videoId,
                title = item.title,
                channel = item.channel,
                thumbnailUrl = item.thumbnailUrl,
                error = null,
                isBuffering = true,
            )
            try {
                val resolution = withContext(Dispatchers.IO) {
                    repo.resolveStream(item.videoId, forceRefresh = forceRefresh)
                }
                playerHolder.playStream(
                    videoId = item.videoId,
                    title = item.title,
                    channel = item.channel,
                    thumbnailUrl = item.thumbnailUrl,
                    resolution = resolution,
                    startPositionMs = startPositionMs,
                )
                _ui.value = _ui.value.copy(
                    qualityLabel = resolution.qualityLabel ?: (if (resolution.isMuxed) "360p" else "720p HD"),
                    availableQualities = resolution.availableQualities,
                    availableSubtitles = resolution.subtitles,
                    availableAudioLanguages = resolution.audioLanguages,
                    selectedAudioLanguage = resolution.audioLanguages.firstOrNull()?.name ?: "Original",
                    isSubtitlesEnabled = false,
                    selectedSubtitleLang = null,
                    error = null,
                )
            } catch (t: Throwable) {
                if (t is kotlinx.coroutines.CancellationException) throw t
                loadedVideoId = null
                _ui.value = _ui.value.copy(
                    error = "Streaming failed: ${t.message ?: "Stream resolve nahi hua"}",
                    isBuffering = false,
                )
            } finally {
                _isResolving.value = false
            }
        }
    }

    fun retryCurrentVideo(pageIndex: Int) {
        val item = _feed.value.getOrNull(pageIndex) ?: return
        val currentPos = playerHolder.exoPlayer.currentPosition.takeIf { it > 0L } ?: _progress.value.positionMs
        repo.clearCache(item.videoId)
        loadedVideoId = null
        loadForPage(pageIndex, forceRefresh = true, startPositionMs = currentPos)
    }

    fun togglePlayPause() {
        val p = playerHolder.exoPlayer
        if (p.isPlaying) {
            p.pause()
        } else {
            p.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val target = positionMs.coerceAtLeast(0L)
        playerHolder.exoPlayer.seekTo(target)
        _progress.value = _progress.value.copy(positionMs = target)
    }

    fun seekRelative(offsetMs: Long) {
        playerHolder.seekRelative(offsetMs)
        _progress.value = _progress.value.copy(positionMs = playerHolder.exoPlayer.currentPosition)
    }

    fun setPlaybackSpeed(speed: Float) {
        playerHolder.setPlaybackSpeed(speed)
        _ui.value = _ui.value.copy(playbackSpeed = speed)
    }

    fun toggleMute() {
        val newMuted = !_ui.value.isMuted
        playerHolder.setMuted(newMuted)
        _ui.value = _ui.value.copy(isMuted = newMuted)
    }

    fun toggleSubtitles() {
        val newEnabled = !_ui.value.isSubtitlesEnabled
        playerHolder.setSubtitlesEnabled(newEnabled, _ui.value.selectedSubtitleLang)
        _ui.value = _ui.value.copy(isSubtitlesEnabled = newEnabled)
    }

    fun selectSubtitleLanguage(track: SubtitleTrack?) {
        if (track == null) {
            playerHolder.setSubtitlesEnabled(false)
            _ui.value = _ui.value.copy(isSubtitlesEnabled = false, selectedSubtitleLang = null)
        } else {
            playerHolder.setSubtitlesEnabled(true, track.languageCode)
            _ui.value = _ui.value.copy(isSubtitlesEnabled = true, selectedSubtitleLang = track.languageCode)
        }
    }

    fun selectAudioLanguage(track: AudioLanguageTrack) {
        val item = _ui.value
        val videoId = item.videoId ?: return
        playerHolder.switchAudioTrack(videoId, item.title, item.channel, item.thumbnailUrl, track)
        _ui.value = _ui.value.copy(selectedAudioLanguage = track.name)
    }

    fun switchQuality(track: com.nexusdigital.rivavideo.data.VideoQualityTrack) {
        val item = _ui.value
        val videoId = item.videoId ?: return
        playerHolder.switchVideoQuality(videoId, item.title, item.channel, item.thumbnailUrl, track)
        _ui.value = _ui.value.copy(qualityLabel = track.qualityLabel)
    }

    fun toggleSubscribe() {
        _ui.value = _ui.value.copy(isSubscribed = !_ui.value.isSubscribed)
    }

    fun toggleLike() {
        val current = _ui.value.isLiked
        _ui.value = _ui.value.copy(
            isLiked = !current,
            isDisliked = if (!current) false else _ui.value.isDisliked,
        )
    }

    fun toggleDislike() {
        val current = _ui.value.isDisliked
        _ui.value = _ui.value.copy(
            isDisliked = !current,
            isLiked = if (!current) false else _ui.value.isLiked,
        )
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
        if (tab == AppTab.SHORTS) {
            if (_shorts.value.isEmpty()) {
                loadShorts()
            } else {
                playShortAtIndex(_activeShortIndex.value)
            }
        } else if (tab == AppTab.HOME) {
            if (_feed.value.isEmpty()) {
                loadCategory(_availableCategories.value.first())
            }
        }
    }

    fun openRegionPicker() {
        _showRegionPicker.value = true
    }

    fun closeRegionPicker() {
        _showRegionPicker.value = false
    }

    fun selectRegionPreference(pref: RegionPreference) {
        _regionPreference.value = pref
        _showRegionPicker.value = false
        prefs.edit().putString("preferred_region", pref.id).apply()
        val cats = getCategoriesForRegion(pref)
        _availableCategories.value = cats
        loadCategory(cats.first())
    }

    fun toggleBackgroundPlayback() {
        val next = !_isBackgroundPlaybackEnabled.value
        _isBackgroundPlaybackEnabled.value = next
        prefs.edit().putBoolean("bg_playback_enabled", next).apply()
    }

    fun isVideoSavedOffline(videoId: String): Boolean =
        _offlineVideos.value.any { it.videoId == videoId }

    fun toggleSaveOffline(video: VideoItem) {
        val current = _offlineVideos.value.toMutableList()
        val existingIdx = current.indexOfFirst { it.videoId == video.videoId }
        if (existingIdx >= 0) {
            current.removeAt(existingIdx)
        } else {
            current.add(
                0,
                OfflineVideoItem(
                    videoId = video.videoId,
                    title = video.title,
                    channel = video.channel,
                    durationMs = video.durationMs,
                    thumbnailUrl = video.thumbnailUrl,
                ),
            )
        }
        _offlineVideos.value = current
        saveOfflineVideosToPrefs(current)
    }

    fun removeOfflineVideo(videoId: String) {
        val current = _offlineVideos.value.filterNot { it.videoId == videoId }
        _offlineVideos.value = current
        saveOfflineVideosToPrefs(current)
    }

    fun playOfflineVideo(offline: OfflineVideoItem) {
        val video = VideoItem(
            videoId = offline.videoId,
            title = offline.title,
            channel = offline.channel,
            durationMs = offline.durationMs,
            thumbnailUrl = offline.thumbnailUrl,
        )
        openVideoFromChannel(video)
    }

    private fun loadOfflineVideosFromPrefs(): List<OfflineVideoItem> {
        val raw = prefs.getString("offline_saved_videos", null) ?: return emptyList()
        return try {
            jsonHelper.decodeFromString<List<OfflineVideoItem>>(raw)
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun saveOfflineVideosToPrefs(list: List<OfflineVideoItem>) {
        try {
            prefs.edit().putString("offline_saved_videos", jsonHelper.encodeToString(list)).apply()
        } catch (_: Throwable) {}
    }

    fun clearWatchHistory() {
        _watchHistory.value = emptyList()
        prefs.edit().remove("watch_history_7d").apply()
    }

    fun playHistoryVideo(item: WatchHistoryItem) {
        val video = VideoItem(
            videoId = item.videoId,
            title = item.title,
            channel = item.channel,
            durationMs = item.durationMs,
            thumbnailUrl = item.thumbnailUrl,
        )
        openVideoFromChannel(video)
    }

    private fun recordWatchHistory(video: VideoItem) {
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        val current = _watchHistory.value.filter { it.watchedAtMs >= sevenDaysAgo }.toMutableList()
        current.removeAll { it.videoId == video.videoId }
        current.add(
            0,
            WatchHistoryItem(
                videoId = video.videoId,
                title = video.title,
                channel = video.channel,
                durationMs = video.durationMs,
                thumbnailUrl = video.thumbnailUrl,
                watchedAtMs = System.currentTimeMillis(),
            ),
        )
        _watchHistory.value = current
        saveWatchHistoryToPrefs(current)
    }

    private fun loadWatchHistoryFromPrefs(): List<WatchHistoryItem> {
        val raw = prefs.getString("watch_history_7d", null) ?: return emptyList()
        return try {
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            jsonHelper.decodeFromString<List<WatchHistoryItem>>(raw).filter { it.watchedAtMs >= sevenDaysAgo }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun saveWatchHistoryToPrefs(list: List<WatchHistoryItem>) {
        try {
            prefs.edit().putString("watch_history_7d", jsonHelper.encodeToString(list)).apply()
        } catch (_: Throwable) {}
    }

    fun loadShorts() {
        viewModelScope.launch {
            _shortsLoading.value = true
            try {
                val list = repo.fetchShorts()
                _shorts.value = list
                if (list.isNotEmpty()) {
                    playShortAtIndex(0)
                }
            } catch (t: Throwable) {
                // Ignore transient errors
            } finally {
                _shortsLoading.value = false
            }
        }
    }

    fun switchShort(index: Int) {
        val list = _shorts.value
        if (index in list.indices) {
            _activeShortIndex.value = index
            playShortAtIndex(index)
        }
    }

    private fun playShortAtIndex(index: Int) {
        val short = _shorts.value.getOrNull(index) ?: return
        resolveJob?.cancel()
        resolveJob = viewModelScope.launch {
            _isResolving.value = true
            _ui.value = _ui.value.copy(
                videoId = short.videoId,
                title = short.title,
                channel = short.channel,
                thumbnailUrl = short.thumbnailUrl,
                error = null,
                isBuffering = true,
            )
            try {
                val resolution = withContext(Dispatchers.IO) {
                    repo.resolveStream(short.videoId)
                }
                playerHolder.playStream(
                    videoId = short.videoId,
                    title = short.title,
                    channel = short.channel,
                    thumbnailUrl = short.thumbnailUrl,
                    resolution = resolution,
                )
                _ui.value = _ui.value.copy(
                    qualityLabel = resolution.qualityLabel ?: "720p HD",
                    availableQualities = resolution.availableQualities,
                    availableSubtitles = resolution.subtitles,
                    availableAudioLanguages = resolution.audioLanguages,
                    selectedAudioLanguage = resolution.audioLanguages.firstOrNull()?.name ?: "Original",
                    error = null,
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(
                    error = "Streaming failed: ${t.message ?: "Stream resolve nahi hua"}",
                    isBuffering = false,
                )
            } finally {
                _isResolving.value = false
            }
        }
    }

    fun toggleShortLike(index: Int) {
        val list = _shorts.value.toMutableList()
        val s = list.getOrNull(index) ?: return
        list[index] = s.copy(isLiked = !s.isLiked)
        _shorts.value = list
    }

    fun openChannel(channelId: String?, fallbackChannelName: String? = null) {
        val cName = fallbackChannelName ?: "YouTube Creator"
        _channelLoading.value = true
        _activeChannel.value = ChannelDetails(
            channelId = channelId ?: "UC_temp",
            title = cName,
            subscriberCount = "Loading channel...",
            videos = emptyList(),
        )
        viewModelScope.launch {
            try {
                val details = if (!channelId.isNullOrBlank() && channelId.startsWith("UC")) {
                    repo.fetchChannel(channelId)
                } else {
                    val searchResults = repo.searchVideos(cName)
                    val sortedSearch = searchResults
                        .distinctBy { it.videoId }
                        .sortedWith(compareBy { parseRelativeAgeSeconds(it.publishedTime) })
                    ChannelDetails(
                        channelId = channelId ?: "channel_$cName",
                        title = cName,
                        subscriberCount = "1.5M subscribers",
                        videoCount = "${sortedSearch.size} videos",
                        videos = sortedSearch,
                    )
                }
                _activeChannel.value = details
            } catch (t: Throwable) {
                val fallbackVideos = repo.searchVideos(cName)
                val sortedFallback = fallbackVideos
                    .distinctBy { it.videoId }
                    .sortedWith(compareBy { parseRelativeAgeSeconds(it.publishedTime) })
                _activeChannel.value = ChannelDetails(
                    channelId = channelId ?: "UC_fallback",
                    title = cName,
                    subscriberCount = "1.5M subscribers",
                    videoCount = "${sortedFallback.size} videos",
                    videos = sortedFallback,
                )
            } finally {
                _channelLoading.value = false
            }
        }
    }

    fun closeChannel() {
        _activeChannel.value = null
    }

    fun toggleChannelSubscribe() {
        val c = _activeChannel.value ?: return
        _activeChannel.value = c.copy(isSubscribed = !c.isSubscribed)
    }

    fun openVideoFromChannel(video: VideoItem) {
        _activeChannel.value = null
        val currentFeed = _feed.value.toMutableList()
        val existingIndex = currentFeed.indexOfFirst { it.videoId == video.videoId }
        if (existingIndex >= 0) {
            openWatch(existingIndex)
        } else {
            currentFeed.add(0, video)
            _feed.value = currentFeed
            openWatch(0)
        }
    }

    fun pauseForBackground() {
        if (!_isBackgroundPlaybackEnabled.value) {
            playerHolder.exoPlayer.pause()
        }
    }

    /** App minimize hone par state save karein aur background setting ke mutabik pause karein */
    fun onAppBackgrounded() {
        val player = playerHolder.exoPlayer
        savedPositionBeforeBackground = player.currentPosition.takeIf { it > 0L } ?: _progress.value.positionMs
        wasPlayingBeforeBackground = player.isPlaying

        if (!_isBackgroundPlaybackEnabled.value) {
            player.pause()
        }
    }

    /** App wapas foreground aane par bina kisi playback error ke usi jagah se video continue karein */
    fun onAppForegrounded() {
        val player = playerHolder.exoPlayer
        _ui.value = _ui.value.copy(error = null)

        val idx = _watchIndex.value ?: return
        val pos = savedPositionBeforeBackground.takeIf { it > 0L } ?: player.currentPosition

        if (!_isBackgroundPlaybackEnabled.value) {
            if (wasPlayingBeforeBackground) {
                if (player.playbackState == Player.STATE_IDLE || player.playerError != null) {
                    // Stream expire ya dead ho gaya tha background me, automatically silently resume karein
                    loadForPage(idx, forceRefresh = true, startPositionMs = pos)
                } else {
                    if (pos > 0L) player.seekTo(pos)
                    player.play()
                }
            }
        } else {
            // Background playback enabled tha, agar stream fail hua tha to auto recover karein
            if (player.playerError != null || player.playbackState == Player.STATE_IDLE) {
                loadForPage(idx, forceRefresh = true, startPositionMs = pos)
            }
        }
    }

    /** UI PlayerView attach ke liye active player. */
    fun attachedPlayer(): Player = playerHolder.exoPlayer

    override fun onCleared() {
        playerHolder.release()
        super.onCleared()
    }

    /** Discover chips: label dikhega, query YouTube search jaata hai. */
    data class Category(val label: String, val query: String)

    companion object {
        fun getCategoriesForRegion(pref: RegionPreference): List<Category> {
            return when (pref) {
                RegionPreference.INDIA -> listOf(
                    Category("All", pref.defaultQuery),
                    Category("Trending", "trending videos today India"),
                    Category("Top YouTubers", "carryminati total gaming bhuvan bam ashish chanchlani dhruv rathee round2hell"),
                    Category("Hindi Comedy", "best standup comedy Hindi videos"),
                    Category("Dubbed Movies", "south indian hindi dubbed full movie blockbuster action"),
                    Category("Bollywood", "bollywood latest songs trailers 2026"),
                    Category("Desi Hip-Hop", "desi hip hop rap songs hindi punjabi"),
                    Category("Cricket", "cricket match highlights India"),
                    Category("Food & Travel", "Indian street food travel vlog"),
                    Category("Tech", "smartphone review in hindi"),
                )
                RegionPreference.GLOBAL -> listOf(
                    Category("All", pref.defaultQuery),
                    Category("Global Trending", "trending viral videos worldwide"),
                    Category("MrBeast", "MrBeast latest challenges"),
                    Category("Pop Music", "top billboard songs 2026"),
                    Category("Cinema", "official hollywood movie trailers 2026"),
                    Category("Tech & AI", "latest technology AI robotics"),
                    Category("Gaming", "popular gaming gameplay 2026"),
                )
                RegionPreference.HINDI_DUBBED -> listOf(
                    Category("All Dubbed", pref.defaultQuery),
                    Category("South Blockbusters", "south indian movie hindi dubbed blockbuster new"),
                    Category("Hollywood Hindi", "hollywood action movies in hindi"),
                    Category("MrBeast Hindi", "MrBeast in Hindi dubbed episodes"),
                    Category("Action", "south indian action movie hindi dubbed"),
                    Category("Horror & Thriller", "hindi dubbed horror thriller movie"),
                )
                RegionPreference.DEVOTIONAL -> listOf(
                    Category("All Bhakti", pref.defaultQuery),
                    Category("Shri Ram", "shri ram bhajan katha aarti"),
                    Category("Krishna", "krishna bhajan kirtan live"),
                    Category("Hanuman Chalisa", "hanuman chalisa super fast gulshan kumar"),
                    Category("Shiv Ji", "shiv tandav stotram bhajan"),
                    Category("Aarti", "sampoorna aarti sangrah hindi"),
                )
                RegionPreference.GAMING -> listOf(
                    Category("All Gaming", pref.defaultQuery),
                    Category("BGMI Live", "BGMI live stream tournament highlights"),
                    Category("Free Fire", "total gaming free fire gameplay"),
                    Category("GTA 5 Roleplay", "techno gamerz GTA 5 roleplay hindi"),
                    Category("Minecraft", "minecraft hindi gameplay building"),
                )
            }
        }
    }
}
