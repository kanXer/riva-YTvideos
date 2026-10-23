package com.nexusdigital.rivavideo.video

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.nexusdigital.rivavideo.data.AudioLanguageTrack
import com.nexusdigital.rivavideo.data.StreamResolution
import com.nexusdigital.rivavideo.data.SubtitleTrack
import com.nexusdigital.rivavideo.data.VideoQualityTrack
import java.io.File
import java.io.IOException

/**
 * App-process ExoPlayer holder for video streaming.
 * - Supports HD Adaptive Streaming (1080p / 720p HD)
 * - Supports Merged separate Video + Audio streams (MergingMediaSource)
 * - Supports Adaptive HLS (HlsMediaSource)
 * - Supports Quality Switching on the fly
 * - Subtitle / Closed-Caption rendering & ON/OFF toggle
 * - Multi-language audio track switching
 * - Per-stream User-Agent and Origin/Referer matching to prevent HTTP 403 Forbidden.
 * - LRU media cache for instant seeking and replays.
 */
@OptIn(UnstableApi::class)
class VideoPlayer(private val appContext: Context) {

    val exoPlayer: ExoPlayer by lazy {
        val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 10_000,
                /* maxBufferMs = */ 60_000,
                /* bufferForPlaybackMs = */ 500,
                /* bufferForPlaybackAfterRebufferMs = */ 1_000,
            )
            .setBackBuffer(
                /* backBufferDurationMs = */ 30_000,
                /* retainBackBufferFromKeyframe = */ true,
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        ExoPlayer.Builder(appContext)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setLoadControl(loadControl)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build().apply {
                // By default keep text subtitles off until user clicks CC
                trackSelectionParameters = trackSelectionParameters.buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    .build()
            }
    }

    private var currentResolution: StreamResolution? = null

    private fun buildDataSourceFactory(resolution: StreamResolution): DataSource.Factory {
        val userAgent = resolution.userAgent.ifBlank { DEFAULT_USER_AGENT }
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(12_000)
            .setReadTimeoutMs(20_000)

        if (resolution.headers.isNotEmpty()) {
            httpFactory.setDefaultRequestProperties(resolution.headers)
        }

        val upstream = DefaultDataSource.Factory(appContext, httpFactory)
        val cache = getCache(appContext)
        return if (cache != null) {
            CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(upstream)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        } else {
            upstream
        }
    }

    fun playStream(
        videoId: String,
        title: String,
        channel: String,
        thumbnailUrl: String?,
        resolution: StreamResolution,
        audioUrlOverride: String? = null,
        videoUrlOverride: String? = null,
        startPositionMs: Long = 0L,
    ) {
        currentResolution = resolution
        val dataSourceFactory = buildDataSourceFactory(resolution)
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(channel)
            .setArtworkUri(thumbnailUrl?.let { Uri.parse(it) })
            .build()

        // Configure subtitle configurations
        val subtitleConfigs = resolution.subtitles.map { sub ->
            MediaItem.SubtitleConfiguration.Builder(Uri.parse(sub.vttUrl))
                .setMimeType(MimeTypes.TEXT_VTT)
                .setLanguage(sub.languageCode)
                .setLabel(sub.name)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
        }

        val baseMediaItem = MediaItem.Builder()
            .setMediaId(videoId)
            .setMediaMetadata(metadata)
            .setSubtitleConfigurations(subtitleConfigs)

        val activeAudioUrl = audioUrlOverride ?: resolution.audioUrl
        val activeVideoUrl = videoUrlOverride ?: resolution.videoUrl

        val mediaSource: MediaSource = when {
            // 1. Adaptive separate video + audio streams (merged seamlessly)
            !activeVideoUrl.isNullOrEmpty() && !activeAudioUrl.isNullOrEmpty() -> {
                val videoItem = MediaItem.Builder()
                    .setMediaId("${videoId}_v")
                    .setUri(activeVideoUrl)
                    .setMediaMetadata(metadata)
                    .setSubtitleConfigurations(subtitleConfigs)
                    .build()
                val audioItem = MediaItem.Builder()
                    .setMediaId("${videoId}_a")
                    .setUri(activeAudioUrl)
                    .build()

                val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(videoItem)
                val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioItem)
                MergingMediaSource(videoSource, audioSource)
            }

            // 2. HLS Master Playlist (Adaptive multi-bitrate)
            !resolution.hlsUrl.isNullOrEmpty() && audioUrlOverride == null && videoUrlOverride == null -> {
                val item = baseMediaItem.setUri(resolution.hlsUrl).build()
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(item)
            }

            // 3. Direct muxed mp4 (video + audio ek saath)
            resolution.isMuxed && !activeVideoUrl.isNullOrEmpty() && audioUrlOverride == null -> {
                val item = baseMediaItem.setUri(activeVideoUrl).build()
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(item)
            }

            // 4. Video-only fallback
            !activeVideoUrl.isNullOrEmpty() -> {
                val item = baseMediaItem.setUri(activeVideoUrl).build()
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(item)
            }

            // 5. Audio-only fallback
            !activeAudioUrl.isNullOrEmpty() -> {
                val item = baseMediaItem.setUri(activeAudioUrl).build()
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(item)
            }

            else -> throw IOException("No playable stream URL found")
        }

        if (startPositionMs > 0L) {
            exoPlayer.setMediaSource(mediaSource, startPositionMs)
        } else {
            exoPlayer.setMediaSource(mediaSource)
        }
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun setSubtitlesEnabled(enabled: Boolean, preferredLanguage: String? = null) {
        val builder = exoPlayer.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !enabled)
        if (preferredLanguage != null) {
            builder.setPreferredTextLanguage(preferredLanguage)
        }
        exoPlayer.trackSelectionParameters = builder.build()
    }

    fun switchAudioTrack(
        videoId: String,
        title: String,
        channel: String,
        thumbnailUrl: String?,
        track: AudioLanguageTrack,
    ) {
        val res = currentResolution ?: return
        val currentPos = exoPlayer.currentPosition
        playStream(
            videoId = videoId,
            title = title,
            channel = channel,
            thumbnailUrl = thumbnailUrl,
            resolution = res,
            audioUrlOverride = track.url,
            startPositionMs = currentPos,
        )
    }

    fun switchVideoQuality(
        videoId: String,
        title: String,
        channel: String,
        thumbnailUrl: String?,
        quality: VideoQualityTrack,
    ) {
        val res = currentResolution ?: return
        val currentPos = exoPlayer.currentPosition
        val updatedRes = res.copy(
            videoUrl = quality.url,
            qualityLabel = quality.qualityLabel,
            itag = quality.itag,
        )
        currentResolution = updatedRes
        playStream(
            videoId = videoId,
            title = title,
            channel = channel,
            thumbnailUrl = thumbnailUrl,
            resolution = updatedRes,
            videoUrlOverride = quality.url,
            startPositionMs = currentPos,
        )
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(speed.coerceIn(0.25f, 3.0f))
    }

    fun setMuted(muted: Boolean) {
        exoPlayer.volume = if (muted) 0f else 1f
    }

    fun seekRelative(offsetMs: Long) {
        val current = exoPlayer.currentPosition
        val duration = exoPlayer.duration.takeIf { it > 0L } ?: Long.MAX_VALUE
        val target = (current + offsetMs).coerceIn(0L, duration)
        exoPlayer.seekTo(target)
    }

    fun release() {
        exoPlayer.release()
    }

    companion object {
        const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15"

        @Volatile
        private var simpleCacheInstance: SimpleCache? = null

        @Synchronized
        fun getCache(context: Context): SimpleCache? {
            return try {
                simpleCacheInstance ?: synchronized(this) {
                    simpleCacheInstance ?: run {
                        val cacheDir = File(context.applicationContext.cacheDir, "video_cache")
                        SimpleCache(
                            cacheDir,
                            LeastRecentlyUsedCacheEvictor(200 * 1024 * 1024L),
                            StandaloneDatabaseProvider(context.applicationContext),
                        ).also { simpleCacheInstance = it }
                    }
                }
            } catch (t: Throwable) {
                android.util.Log.w("VideoPlayer", "Could not initialize media cache: ${t.message}", t)
                null
            }
        }
    }
}

/** Comprehensive UI state wrapper for YouTube-style video playback. */
data class VideoUiState(
    val videoId: String? = null,
    val title: String = "",
    val channel: String = "",
    val thumbnailUrl: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isMuted: Boolean = false,
    val qualityLabel: String? = null,
    val availableQualities: List<VideoQualityTrack> = emptyList(),
    val isSubtitlesEnabled: Boolean = false,
    val selectedSubtitleLang: String? = null,
    val availableSubtitles: List<SubtitleTrack> = emptyList(),
    val availableAudioLanguages: List<AudioLanguageTrack> = emptyList(),
    val selectedAudioLanguage: String? = null,
    val isSubscribed: Boolean = false,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val error: String? = null,
) {
    fun hasVideo(id: String): Boolean = videoId == id
}
