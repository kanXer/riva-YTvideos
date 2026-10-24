package com.nexusdigital.rivavideo.data

import java.util.concurrent.ConcurrentHashMap

/**
 * Video data repository: search feed + stream urls (2h cached per videoId —
 * YouTube stream URLs valid ~3-6h hote hain).
 */
class VideoRepository(
    private val api: InnerTubeVideoClient = InnerTubeVideoClient(),
) {
    private val streamCache = ConcurrentHashMap<String, StreamResolution>()

    suspend fun searchVideos(query: String): List<VideoItem> =
        api.searchVideos(query)

    suspend fun searchChannels(query: String): List<ChannelSearchResult> =
        api.searchChannels(query)

    suspend fun fetchShorts(query: String = "#shorts viral trending", params: String? = null): List<ShortItem> =
        api.fetchShorts(query, params)

    suspend fun fetchChannel(channelId: String): ChannelDetails =
        api.fetchChannel(channelId)

    suspend fun resolveStream(videoId: String, forceRefresh: Boolean = false): StreamResolution {
        if (!forceRefresh) {
            streamCache[videoId]?.let { cached ->
                if (System.currentTimeMillis() < cached.expiresAtMs - 10 * 60 * 1000L) {
                    return cached
                }
                streamCache.remove(videoId)
            }
        } else {
            streamCache.remove(videoId)
        }
        val fresh = api.resolveStream(videoId)
        streamCache[videoId] = fresh
        return fresh
    }

    fun clearCache(videoId: String) {
        streamCache.remove(videoId)
    }
}
