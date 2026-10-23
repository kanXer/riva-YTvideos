package com.nexusdigital.rivavideo.data

import android.util.Log
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * YouTube InnerTube client — video streaming & search.
 *
 * Implements client playback strategy documented in docs/MUSIC_API_DOCUMENTATION.md:
 * - Playback clients: VISIONOS (primary tokenless client, unthrottled direct URLs + HLS)
 *   -> ANDROID (fallback)
 *   -> IOS (fallback with public key)
 *   -> TVHTML5 (last resort fallback)
 * - Format resolution:
 *   1. Prioritizes HD (1080p / 720p HD) adaptive video streams merged with 140 AAC audio.
 *   2. Collects all available video qualities (1080p, 720p, 480p, 360p, etc.).
 *   3. Collects audio language tracks (including Default/Original and any dubbed tracks).
 *   4. Subtitles: extracts captionTracks into WebVTT (vttUrl).
 *   5. Fallback to HLS or direct muxed streams if adaptive is unavailable.
 */
class InnerTubeVideoClient(
    private val http: OkHttpClient = defaultHttp(),
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Volatile
    private var cachedVisitorData: String? = null

    suspend fun searchVideos(query: String): List<VideoItem> = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            put("context", buildJsonObject {
                put("client", buildJsonObject {
                    put("clientName", JsonPrimitive(WEB_CLIENT_NAME))
                    put("clientVersion", JsonPrimitive(WEB_CLIENT_VERSION))
                    put("hl", JsonPrimitive("en"))
                    put("gl", JsonPrimitive("IN"))
                })
            })
            put("query", JsonPrimitive(query))
            // YouTube InnerTube official video-only filter
            put("params", JsonPrimitive("EgIQAQ%3D%3D"))
        }
        val root = executePost(
            "$BASE/youtubei/v1/search?prettyPrint=false",
            body,
            headers = mapOf(
                "User-Agent" to WEB_UA,
                "Origin" to "https://www.youtube.com",
                "X-YouTube-Client-Name" to "1",
                "X-YouTube-Client-Version" to WEB_CLIENT_VERSION,
            ),
        )
        val parsed = json.parseToJsonElement(root).asJsonObjectOrNull() ?: return@withContext emptyList()
        val vis = parsed["responseContext"]?.asJsonObjectOrNull()?.get("visitorData").asStringOrNull()
        if (!vis.isNullOrBlank()) {
            cachedVisitorData = vis
        }

        collectVideoRenderers(parsed)
            .mapNotNull { it.toVideoItem() }
            .distinctBy { it.videoId }
    }

    suspend fun fetchShorts(query: String = "#shorts viral trending"): List<ShortItem> = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            put("context", buildJsonObject {
                put("client", buildJsonObject {
                    put("clientName", JsonPrimitive(WEB_CLIENT_NAME))
                    put("clientVersion", JsonPrimitive(WEB_CLIENT_VERSION))
                    put("hl", JsonPrimitive("en"))
                    put("gl", JsonPrimitive("IN"))
                })
            })
            put("query", JsonPrimitive(query))
        }
        val root = executePost(
            "$BASE/youtubei/v1/search?prettyPrint=false",
            body,
            headers = mapOf(
                "User-Agent" to WEB_UA,
                "Origin" to "https://www.youtube.com",
                "X-YouTube-Client-Name" to "1",
                "X-YouTube-Client-Version" to WEB_CLIENT_VERSION,
            ),
        )
        val parsed = json.parseToJsonElement(root).asJsonObjectOrNull() ?: return@withContext emptyList()
        val list = mutableListOf<ShortItem>()

        fun walk(e: JsonElement) {
            when (e) {
                is JsonObject -> {
                    val s = e["shortsLockupViewModel"]?.asJsonObjectOrNull()
                    if (s != null) {
                        val reelEp = s["onTap"]?.asJsonObjectOrNull()
                            ?.get("innertubeCommand")?.asJsonObjectOrNull()
                            ?.get("reelWatchEndpoint")?.asJsonObjectOrNull()
                        val vid = reelEp?.get("videoId").asStringOrNull()
                        val params = reelEp?.get("params").asStringOrNull()
                        val channelId = if (params != null) {
                            try {
                                val decoded = android.util.Base64.decode(params, android.util.Base64.DEFAULT).decodeToString()
                                "UC[a-zA-Z0-9_-]{22}".toRegex().find(decoded)?.value
                            } catch (_: Throwable) {
                                "UC[a-zA-Z0-9_-]{22}".toRegex().find(params)?.value
                            }
                        } else null
                        val title = s["overlayMetadata"]?.asJsonObjectOrNull()
                            ?.get("primaryText")?.asJsonObjectOrNull()
                            ?.get("content").asStringOrNull()
                        val views = s["overlayMetadata"]?.asJsonObjectOrNull()
                            ?.get("secondaryText")?.asJsonObjectOrNull()
                            ?.get("content").asStringOrNull()
                        val thumb = s["thumbnailViewModel"]?.asJsonObjectOrNull()
                            ?.get("thumbnailViewModel")?.asJsonObjectOrNull()
                            ?.get("image")?.asJsonObjectOrNull()
                            ?.get("sources")?.asJsonArrayOrNull()?.lastOrNull()
                            ?.asJsonObjectOrNull()?.get("url").asStringOrNull()
                            ?: "https://i.ytimg.com/vi/$vid/hqdefault.jpg"

                        if (!vid.isNullOrBlank() && !title.isNullOrBlank()) {
                            list.add(
                                ShortItem(
                                    videoId = vid,
                                    title = title,
                                    channel = "YouTube Creator",
                                    channelId = channelId,
                                    thumbnailUrl = thumb,
                                    viewCount = views,
                                ),
                            )
                        }
                    }

                    val r = e["reelItemRenderer"]?.asJsonObjectOrNull()
                    if (r != null) {
                        val vid = r["videoId"].asStringOrNull()
                        val reelEp = r["navigationEndpoint"]?.asJsonObjectOrNull()?.get("reelWatchEndpoint")?.asJsonObjectOrNull()
                        val channelTitle = r["channelTitleText"].asSimpleOrRunText()
                        val params = reelEp?.get("params").asStringOrNull()
                        val channelId = r["channelId"].asStringOrNull() ?: if (params != null) {
                            try {
                                val decoded = android.util.Base64.decode(params, android.util.Base64.DEFAULT).decodeToString()
                                "UC[a-zA-Z0-9_-]{22}".toRegex().find(decoded)?.value
                            } catch (_: Throwable) {
                                "UC[a-zA-Z0-9_-]{22}".toRegex().find(params)?.value
                            }
                        } else null
                        val title = r["headline"].asSimpleOrRunText()
                        val views = r["viewCountText"].asSimpleOrRunText()
                        val thumb = r["thumbnail"]?.asJsonObjectOrNull()
                            ?.get("thumbnails")?.asJsonArrayOrNull()?.lastOrNull()
                            ?.asJsonObjectOrNull()?.get("url").asStringOrNull()
                            ?: "https://i.ytimg.com/vi/$vid/hqdefault.jpg"

                        if (!vid.isNullOrBlank() && !title.isNullOrBlank()) {
                            list.add(
                                ShortItem(
                                    videoId = vid,
                                    title = title,
                                    channel = channelTitle ?: "YouTube Creator",
                                    channelId = channelId,
                                    thumbnailUrl = thumb,
                                    viewCount = views,
                                ),
                            )
                        }
                    }

                    e.values.forEach(::walk)
                }
                is JsonArray -> e.forEach(::walk)
                else -> Unit
            }
        }

        walk(parsed)
        list.distinctBy { it.videoId }
    }

    suspend fun fetchChannel(channelId: String): ChannelDetails = withContext(Dispatchers.IO) {
        fun makeBrowseBody(params: String? = null) = buildJsonObject {
            put("context", buildJsonObject {
                put("client", buildJsonObject {
                    put("clientName", JsonPrimitive(WEB_CLIENT_NAME))
                    put("clientVersion", JsonPrimitive(WEB_CLIENT_VERSION))
                    put("hl", JsonPrimitive("en"))
                    put("gl", JsonPrimitive("IN"))
                })
            })
            put("browseId", JsonPrimitive(channelId))
            if (params != null) {
                put("params", JsonPrimitive(params))
            }
        }

        // 1. First attempt: Fetch channel Videos tab (Latest uploads: newest to oldest)
        var parsed: JsonObject? = null
        try {
            val root = executePost(
                "$BASE/youtubei/v1/browse?prettyPrint=false",
                makeBrowseBody("EgZ2aWRlb3PyBgQKAjoA"),
                headers = mapOf(
                    "User-Agent" to WEB_UA,
                    "Origin" to "https://www.youtube.com",
                    "X-YouTube-Client-Name" to "1",
                    "X-YouTube-Client-Version" to WEB_CLIENT_VERSION,
                ),
            )
            parsed = json.parseToJsonElement(root).asJsonObjectOrNull()
        } catch (_: Throwable) {
            // will fallback below
        }

        // 2. Fallback: If Videos tab failed or returned empty contents, fetch channel home
        var homeParsed: JsonObject? = null
        if (parsed == null || parsed["contents"] == null) {
            val root = executePost(
                "$BASE/youtubei/v1/browse?prettyPrint=false",
                makeBrowseBody(null),
                headers = mapOf(
                    "User-Agent" to WEB_UA,
                    "Origin" to "https://www.youtube.com",
                    "X-YouTube-Client-Name" to "1",
                    "X-YouTube-Client-Version" to WEB_CLIENT_VERSION,
                ),
            )
            homeParsed = json.parseToJsonElement(root).asJsonObjectOrNull()
                ?: throw IOException("Channel data parse nahi hua")
            if (parsed == null) parsed = homeParsed
        }

        val header = parsed?.get("header")?.asJsonObjectOrNull()?.get("pageHeaderRenderer")?.asJsonObjectOrNull()
            ?: parsed?.get("header")?.asJsonObjectOrNull()?.get("c4TabbedHeaderRenderer")?.asJsonObjectOrNull()
            ?: homeParsed?.get("header")?.asJsonObjectOrNull()?.get("pageHeaderRenderer")?.asJsonObjectOrNull()
            ?: homeParsed?.get("header")?.asJsonObjectOrNull()?.get("c4TabbedHeaderRenderer")?.asJsonObjectOrNull()

        val title = header?.get("pageTitle").asStringOrNull()
            ?: header?.get("title").asStringOrNull()
            ?: "YouTube Creator"

        val banner = header?.get("content")?.asJsonObjectOrNull()
            ?.get("pageHeaderViewModel")?.asJsonObjectOrNull()
            ?.get("banner")?.asJsonObjectOrNull()
            ?.get("imageBannerViewModel")?.asJsonObjectOrNull()
            ?.get("image")?.asJsonObjectOrNull()
            ?.get("sources")?.asJsonArrayOrNull()?.lastOrNull()
            ?.asJsonObjectOrNull()?.get("url").asStringOrNull()
            ?: header?.get("banner")?.asJsonObjectOrNull()
                ?.get("thumbnails")?.asJsonArrayOrNull()?.lastOrNull()
                ?.asJsonObjectOrNull()?.get("url").asStringOrNull()

        val avatar = header?.get("content")?.asJsonObjectOrNull()
            ?.get("pageHeaderViewModel")?.asJsonObjectOrNull()
            ?.get("image")?.asJsonObjectOrNull()
            ?.get("decoratedAvatarViewModel")?.asJsonObjectOrNull()
            ?.get("avatar")?.asJsonObjectOrNull()
            ?.get("avatarViewModel")?.asJsonObjectOrNull()
            ?.get("image")?.asJsonObjectOrNull()
            ?.get("sources")?.asJsonArrayOrNull()?.lastOrNull()
            ?.asJsonObjectOrNull()?.get("url").asStringOrNull()
            ?: header?.get("avatar")?.asJsonObjectOrNull()
                ?.get("thumbnails")?.asJsonArrayOrNull()?.lastOrNull()
                ?.asJsonObjectOrNull()?.get("url").asStringOrNull()

        var handle: String? = null
        var subCount: String? = null
        var videoCount: String? = null

        val metaRows = header?.get("content")?.asJsonObjectOrNull()
            ?.get("pageHeaderViewModel")?.asJsonObjectOrNull()
            ?.get("metadata")?.asJsonObjectOrNull()
            ?.get("contentMetadataViewModel")?.asJsonObjectOrNull()
            ?.get("metadataRows")?.asJsonArrayOrNull().orEmpty()

        for (row in metaRows) {
            val parts = row.asJsonObjectOrNull()?.get("metadataParts")?.asJsonArrayOrNull().orEmpty()
            for (p in parts) {
                val t = p.asJsonObjectOrNull()?.get("text")?.asJsonObjectOrNull()?.get("content").asStringOrNull()
                if (t != null) {
                    if (t.startsWith("@")) handle = t
                    else if (t.contains("subscriber", ignoreCase = true)) subCount = t
                    else if (t.contains("video", ignoreCase = true)) videoCount = t
                }
            }
        }

        if (subCount == null) {
            subCount = header?.get("subscriberCountText").asSimpleOrRunText()
        }

        val videos = mutableListOf<VideoItem>()

        fun extractVideos(e: JsonElement) {
            when (e) {
                is JsonObject -> {
                    val lvm = e["lockupViewModel"]?.asJsonObjectOrNull()
                    if (lvm != null) {
                        val vid = lvm["contentId"].asStringOrNull()
                        val meta = lvm["metadata"]?.asJsonObjectOrNull()?.get("lockupMetadataViewModel")?.asJsonObjectOrNull()
                        val vTitle = meta?.get("title")?.asJsonObjectOrNull()?.get("content").asStringOrNull()
                        val rows = meta?.get("metadata")?.asJsonObjectOrNull()
                            ?.get("contentMetadataViewModel")?.asJsonObjectOrNull()
                            ?.get("metadataRows")?.asJsonArrayOrNull().orEmpty()

                        var views: String? = null
                        var pubTime: String? = null
                        for (row in rows) {
                            val parts = row.asJsonObjectOrNull()?.get("metadataParts")?.asJsonArrayOrNull().orEmpty()
                            for (p in parts) {
                                val t = p.asJsonObjectOrNull()?.get("text")?.asJsonObjectOrNull()?.get("content").asStringOrNull()
                                if (t != null) {
                                    if (views == null && (t.contains("view", ignoreCase = true) || t.contains("watching", ignoreCase = true))) {
                                        views = t
                                    } else if (pubTime == null && (t.contains("ago", ignoreCase = true) || t.contains("streamed", ignoreCase = true) || t.contains("premier", ignoreCase = true) || t.contains("पहले", ignoreCase = true))) {
                                        pubTime = t
                                    }
                                }
                            }
                        }

                        var durMs = 180_000L
                        val contentImg = lvm["contentImage"]?.asJsonObjectOrNull()
                        val overlays = contentImg?.get("thumbnailViewModel")?.asJsonObjectOrNull()?.get("overlays")?.asJsonArrayOrNull().orEmpty()
                        for (ov in overlays) {
                            val badges = ov.asJsonObjectOrNull()?.get("thumbnailBottomOverlayViewModel")?.asJsonObjectOrNull()?.get("badges")?.asJsonArrayOrNull().orEmpty()
                            for (b in badges) {
                                val bText = b.asJsonObjectOrNull()?.get("thumbnailBadgeViewModel")?.asJsonObjectOrNull()?.get("text").asStringOrNull()
                                val parsedDur = parseDuration(bText)
                                if (parsedDur > 0L) durMs = parsedDur
                            }
                        }

                        val thumb = contentImg
                            ?.get("thumbnailViewModel")?.asJsonObjectOrNull()
                            ?.get("image")?.asJsonObjectOrNull()
                            ?.get("sources")?.asJsonArrayOrNull()?.lastOrNull()
                            ?.asJsonObjectOrNull()?.get("url").asStringOrNull()
                            ?: "https://i.ytimg.com/vi/$vid/hqdefault.jpg"

                        if (!vid.isNullOrBlank() && !vTitle.isNullOrBlank()) {
                            videos.add(
                                VideoItem(
                                    videoId = vid,
                                    title = vTitle,
                                    channel = title,
                                    durationMs = durMs,
                                    thumbnailUrl = thumb,
                                    viewCount = views,
                                    publishedTime = pubTime,
                                    channelThumbnailUrl = avatar,
                                    channelId = channelId,
                                ),
                            )
                        }
                    }

                    val gvr = e["gridVideoRenderer"]?.asJsonObjectOrNull()
                    if (gvr != null) {
                        val vid = gvr["videoId"].asStringOrNull()
                        val vTitle = gvr["title"].asSimpleOrRunText()
                        val views = gvr["shortViewCountText"].asSimpleOrRunText() ?: gvr["viewCountText"].asSimpleOrRunText()
                        val pubTime = gvr["publishedTimeText"].asSimpleOrRunText()
                        val thumb = gvr["thumbnail"]?.asJsonObjectOrNull()
                            ?.get("thumbnails")?.asJsonArrayOrNull()?.lastOrNull()
                            ?.asJsonObjectOrNull()?.get("url").asStringOrNull()
                            ?: "https://i.ytimg.com/vi/$vid/hqdefault.jpg"

                        var durMs = 180_000L
                        val overlays = gvr["thumbnailOverlays"]?.asJsonArrayOrNull().orEmpty()
                        for (ov in overlays) {
                            val t = ov.asJsonObjectOrNull()?.get("thumbnailOverlayTimeStatusRenderer")?.asJsonObjectOrNull()?.get("text").asSimpleOrRunText()
                            val parsedDur = parseDuration(t)
                            if (parsedDur > 0L) durMs = parsedDur
                        }

                        if (!vid.isNullOrBlank() && !vTitle.isNullOrBlank()) {
                            videos.add(
                                VideoItem(
                                    videoId = vid,
                                    title = vTitle,
                                    channel = title,
                                    durationMs = durMs,
                                    thumbnailUrl = thumb,
                                    viewCount = views,
                                    publishedTime = pubTime,
                                    channelThumbnailUrl = avatar,
                                    channelId = channelId,
                                ),
                            )
                        }
                    }

                    val vr = e["videoRenderer"]?.asJsonObjectOrNull()
                    if (vr != null && lvm == null) {
                        vr.toVideoItem()?.let { videos.add(it) }
                    }

                    e.values.forEach(::extractVideos)
                }
                is JsonArray -> e.forEach(::extractVideos)
                else -> Unit
            }
        }

        extractVideos(parsed?.get("contents") ?: parsed ?: JsonObject(emptyMap()))

        // If videos tab had 0 videos, extract from homeParsed if available
        if (videos.isEmpty() && homeParsed != null) {
            extractVideos(homeParsed["contents"] ?: homeParsed)
        }

        // Sort videos strictly date-wise: newest (smallest age seconds) to oldest
        val sortedVideos = videos
            .distinctBy { it.videoId }
            .sortedWith(compareBy { parseRelativeAgeSeconds(it.publishedTime) })

        ChannelDetails(
            channelId = channelId,
            title = title,
            handle = handle,
            subscriberCount = subCount ?: "Subscribers",
            videoCount = videoCount ?: "${sortedVideos.size} videos",
            avatarUrl = avatar,
            bannerUrl = banner,
            videos = sortedVideos,
        )
    }

    private fun getOrFetchVisitorData(): String? {
        cachedVisitorData?.let { return it }
        return synchronized(this) {
            cachedVisitorData?.let { return it }
            try {
                val body = buildJsonObject {
                    put("context", buildJsonObject {
                        put("client", buildJsonObject {
                            put("clientName", JsonPrimitive("WEB"))
                            put("clientVersion", JsonPrimitive(WEB_CLIENT_VERSION))
                            put("hl", JsonPrimitive("en"))
                            put("gl", JsonPrimitive("IN"))
                        })
                    })
                }
                val resp = executePost(
                    "$BASE/youtubei/v1/visitor_id?prettyPrint=false",
                    body,
                    headers = mapOf(
                        "User-Agent" to WEB_UA,
                        "Origin" to "https://www.youtube.com",
                    ),
                )
                val parsed = json.parseToJsonElement(resp).asJsonObjectOrNull()
                val vis = parsed?.get("responseContext")?.asJsonObjectOrNull()?.get("visitorData").asStringOrNull()
                if (!vis.isNullOrBlank()) {
                    cachedVisitorData = vis
                    vis
                } else null
            } catch (t: Throwable) {
                Log.w(TAG, "Visitor id fetch error: ${t.message}")
                null
            }
        }
    }

    suspend fun resolveStream(videoId: String): StreamResolution = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        for (client in playbackClients) {
            try {
                val resolution = requestPlayer(videoId, client)
                if (resolution != null) return@withContext resolution
            } catch (t: Throwable) {
                errors += "${client.name}: ${t.message ?: t.javaClass.simpleName}"
            }
        }
        throw IOException(
            buildString {
                append("Video stream resolve nahi hua. ")
                if (errors.isNotEmpty()) append(errors.joinToString(" | "))
            },
        )
    }

    // ------ Playback clients & player API ------

    private data class PlaybackClient(
        val name: String,
        val version: String,
        val url: String,
        val userAgent: String,
        val extraContext: Map<String, JsonElement> = emptyMap(),
        val requestHeaders: Map<String, String> = emptyMap(),
        val playbackHeaders: Map<String, String> = emptyMap(),
        val includePlaybackContext: Boolean = false,
    )

    private val playbackClients = listOf(
        // 1. VISIONOS (Primary tokenless client from yt-dlp & Musify documentation)
        PlaybackClient(
            name = "VISIONOS",
            version = "1.02",
            url = "$BASE/youtubei/v1/player?prettyPrint=false",
            userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15",
            extraContext = mapOf(
                "deviceMake" to JsonPrimitive("Apple"),
                "deviceModel" to JsonPrimitive("RealityDevice17,1"),
                "userAgent" to JsonPrimitive("Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15"),
                "osName" to JsonPrimitive("visionOS"),
                "osVersion" to JsonPrimitive("26.5"),
                "timeZone" to JsonPrimitive("UTC"),
                "utcOffsetMinutes" to JsonPrimitive(0),
            ),
            requestHeaders = mapOf(
                "X-Youtube-Client-Name" to "VISIONOS",
                "X-Youtube-Client-Version" to "1.02",
                "Origin" to "https://www.youtube.com",
                "Sec-Fetch-Mode" to "navigate",
                "Cookie" to "CONSENT=YES+cb",
            ),
            playbackHeaders = mapOf(
                "Origin" to "https://www.youtube.com",
                "Referer" to "https://www.youtube.com/",
            ),
            includePlaybackContext = true,
        ),
        // 2. ANDROID (Fallback playback client)
        PlaybackClient(
            name = "ANDROID",
            version = "21.26.364",
            url = "$BASE/youtubei/v1/player?prettyPrint=false",
            userAgent = "com.google.android.youtube/21.26.364 (Linux; U; Android 11) gzip",
            extraContext = mapOf(
                "androidSdkVersion" to JsonPrimitive(30),
                "osName" to JsonPrimitive("Android"),
                "osVersion" to JsonPrimitive("11"),
            ),
            requestHeaders = mapOf(
                "Origin" to "https://www.youtube.com",
            ),
            playbackHeaders = mapOf(
                "User-Agent" to "com.google.android.youtube/21.26.364 (Linux; U; Android 11) gzip",
            ),
        ),
        // 3. IOS (Fallback client with public key)
        PlaybackClient(
            name = "IOS",
            version = "21.26.4",
            url = "$BASE/youtubei/v1/player?key=AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc&prettyPrint=false",
            userAgent = "com.google.ios.youtube/21.26.4 (iPhone16,2; U; CPU iOS 18_3_2 like Mac OS X;)",
            extraContext = mapOf(
                "deviceMake" to JsonPrimitive("Apple"),
                "deviceModel" to JsonPrimitive("iPhone16,2"),
                "osName" to JsonPrimitive("IOS"),
                "osVersion" to JsonPrimitive("18.3.2.22D82"),
                "platform" to JsonPrimitive("MOBILE"),
            ),
            requestHeaders = mapOf(
                "Origin" to "https://www.youtube.com",
                "X-Youtube-Client-Name" to "IOS",
                "X-Youtube-Client-Version" to "21.26.4",
            ),
            playbackHeaders = mapOf(
                "Origin" to "https://www.youtube.com",
            ),
        ),
        // 4. TVHTML5 (Last resort fallback)
        PlaybackClient(
            name = "TVHTML5",
            version = "7.20260707.07.00",
            url = "$BASE/youtubei/v1/player?prettyPrint=false",
            userAgent = "Mozilla/5.0 (ChromiumStylePlatform) Cobalt/25.lts.30.1034943-gold (unlike Gecko), Unknown_TV_Unknown_0/Unknown (Unknown, Unknown)",
            extraContext = mapOf(
                "gl" to JsonPrimitive("US"),
            ),
            requestHeaders = mapOf(
                "Origin" to "https://www.youtube.com",
                "Sec-Fetch-Mode" to "navigate",
            ),
            playbackHeaders = mapOf(
                "Origin" to "https://www.youtube.com",
            ),
        ),
    )

    private fun requestPlayer(videoId: String, client: PlaybackClient): StreamResolution? {
        val visitor = getOrFetchVisitorData()
        val body = buildJsonObject {
            put("context", buildJsonObject {
                put("client", buildJsonObject {
                    put("clientName", JsonPrimitive(client.name))
                    put("clientVersion", JsonPrimitive(client.version))
                    put("hl", JsonPrimitive("en"))
                    put("gl", JsonPrimitive("IN"))
                    if (!visitor.isNullOrBlank()) {
                        put("visitorData", JsonPrimitive(visitor))
                    }
                    client.extraContext.forEach { (k, v) -> put(k, v) }
                })
            })
            put("videoId", JsonPrimitive(videoId))
            put("contentCheckOk", JsonPrimitive(true))
            put("racyCheckOk", JsonPrimitive(true))
            if (client.includePlaybackContext) {
                put("playbackContext", buildJsonObject {
                    put("contentPlaybackContext", buildJsonObject {
                        put("html5Preference", JsonPrimitive("HTML5_PREF_WANTS"))
                    })
                })
            }
        }

        val headers = mutableMapOf<String, String>()
        headers["User-Agent"] = client.userAgent
        headers.putAll(client.requestHeaders)

        val root = json.parseToJsonElement(
            executePost(client.url, body, headers = headers),
        ).asJsonObjectOrNull() ?: return null

        val status = root["playabilityStatus"]?.asJsonObjectOrNull()
        val playability = status?.get("status").asStringOrNull()
        if (playability != null && playability != "OK") {
            val reason = status?.get("reason").asStringOrNull() ?: playability
            throw IOException(reason)
        }

        // Subtitles / Captions extraction
        val captions = root["captions"]
            ?.asJsonObjectOrNull()?.get("playerCaptionsTracklistRenderer")
            ?.asJsonObjectOrNull()?.get("captionTracks")?.asJsonArrayOrNull()
            ?.mapNotNull { it.asJsonObjectOrNull() }.orEmpty()

        val subtitleTracks = captions.mapNotNull { c ->
            val baseUrl = c["baseUrl"].asStringOrNull() ?: return@mapNotNull null
            val lang = c["languageCode"].asStringOrNull() ?: "en"
            val name = c["name"].asTextRuns()
                ?: c["name"]?.asJsonObjectOrNull()?.get("simpleText").asStringOrNull()
                ?: lang
            SubtitleTrack(
                languageCode = lang,
                name = name,
                vttUrl = if (baseUrl.contains("fmt=")) baseUrl else "$baseUrl&fmt=vtt",
            )
        }

        val streaming = root["streamingData"]?.asJsonObjectOrNull() ?: return null

        val formats = streaming["formats"]?.asJsonArrayOrNull()
            ?.mapNotNull { it.asJsonObjectOrNull() }.orEmpty()
        val adaptiveFormats = streaming["adaptiveFormats"]?.asJsonArrayOrNull()
            ?.mapNotNull { it.asJsonObjectOrNull() }.orEmpty()
        val hlsManifestUrl = streaming["hlsManifestUrl"].asStringOrNull()

        // 1. Extract and sort ALL available Video Qualities from adaptiveFormats
        val videoFormats = adaptiveFormats.filter { f ->
            val mime = f["mimeType"].asStringOrNull().orEmpty()
            mime.startsWith("video/") && f["url"].asStringOrNull() != null
        }

        val qualityTracks = videoFormats
            .mapNotNull { f ->
                val itag = f["itag"].asIntOrNull() ?: return@mapNotNull null
                val height = f["height"].asIntOrNull() ?: return@mapNotNull null
                val rawLabel = f["qualityLabel"].asStringOrNull() ?: "${height}p"
                val qualityLabel = if (height >= 720 && !rawLabel.contains("HD", ignoreCase = true)) {
                    "$rawLabel HD"
                } else {
                    rawLabel
                }
                val url = f["url"].asStringOrNull() ?: return@mapNotNull null
                val mime = f["mimeType"].asStringOrNull() ?: "video/mp4"
                val bitrate = f["bitrate"].asLongOrNull() ?: 0L
                VideoQualityTrack(
                    itag = itag,
                    qualityLabel = qualityLabel,
                    url = url,
                    mimeType = mime,
                    height = height,
                    bitrate = bitrate,
                )
            }
            // Prefer avc1/H.264 for hardware decoding acceleration, then mp4 over webm, then sort by highest resolution
            .sortedWith(
                compareByDescending<VideoQualityTrack> { it.height }
                    .thenByDescending {
                        val m = it.mimeType.lowercase()
                        if (m.contains("avc1")) 3 else if (m.contains("mp4")) 2 else 1
                    }
                    .thenBy { it.bitrate },
            )
            .distinctBy { it.height }

        // 2. Extract best Audio Candidate (prefer itag 140 AAC 128kbps)
        val audioFormats = adaptiveFormats.filter { f ->
            val mime = f["mimeType"].asStringOrNull().orEmpty()
            mime.startsWith("audio/") && f["url"].asStringOrNull() != null
        }

        val audioCandidate = audioFormats
            .maxWithOrNull(
                compareBy<JsonObject> {
                    val itag = it["itag"].asIntOrNull() ?: 0
                    if (itag == 140) 3 else if (itag == 251) 2 else 1
                }.thenBy {
                    it["bitrate"].asLongOrNull() ?: 0L
                },
            )

        val bestAudioUrl = audioCandidate?.get("url").asStringOrNull()
        val bestAudioMime = audioCandidate?.get("mimeType").asStringOrNull() ?: "audio/mp4"
        val bestAudioBitrate = audioCandidate?.get("bitrate").asLongOrNull() ?: 0L

        // 3. Audio Languages list: Extract and deduplicate all audio language options
        val dubbedTracksMap = mutableMapOf<String, AudioLanguageTrack>()
        val defaultLangIds = mutableSetOf<String>()

        audioFormats.forEach { f ->
            val audioTrackObj = f["audioTrack"]?.asJsonObjectOrNull() ?: return@forEach
            val displayName = audioTrackObj["displayName"].asStringOrNull() ?: return@forEach
            val langId = audioTrackObj["id"].asStringOrNull() ?: displayName
            val isDefault = audioTrackObj["audioIsDefault"].asBooleanOrNull() ?: false
            if (isDefault) defaultLangIds.add(langId)

            val url = f["url"].asStringOrNull() ?: return@forEach
            val mime = f["mimeType"].asStringOrNull() ?: "audio/mp4"
            val itag = f["itag"].asIntOrNull() ?: 0
            val bitrate = f["bitrate"].asLongOrNull() ?: 0L

            // Prefer itag 140 AAC or highest bitrate
            val score = (if (itag == 140) 2 else (if (itag == 251) 1 else 0)) * 1_000_000L + bitrate
            val existing = dubbedTracksMap[langId]
            val existingScore = (if (existing?.itag == 140) 2 else (if (existing?.itag == 251) 1 else 0)) * 1_000_000L + (existing?.bitrate ?: 0L)

            if (existing == null || score > existingScore) {
                val formattedName = if (isDefault && !displayName.contains("original", ignoreCase = true)) {
                    "$displayName (Original)"
                } else {
                    displayName
                }
                dubbedTracksMap[langId] = AudioLanguageTrack(
                    id = langId,
                    name = formattedName,
                    url = url,
                    mimeType = mime,
                    bitrate = bitrate,
                    itag = itag,
                )
            }
        }

        val audioLanguages = mutableListOf<AudioLanguageTrack>()
        if (dubbedTracksMap.isNotEmpty()) {
            // Sort: Default / Original first, Hindi second (if present), then alphabetically
            val sortedDubbed = dubbedTracksMap.values.sortedWith(
                compareBy<AudioLanguageTrack> { track ->
                    if (defaultLangIds.contains(track.id) || track.name.contains("original", ignoreCase = true)) 0
                    else if (track.name.contains("hindi", ignoreCase = true)) 1
                    else 2
                }.thenBy { it.name },
            )
            audioLanguages.addAll(sortedDubbed)
        } else if (bestAudioUrl != null) {
            // Regular video without multi-audio tracks
            audioLanguages.add(
                AudioLanguageTrack(
                    id = "default",
                    name = "Original Audio (Default)",
                    url = bestAudioUrl,
                    mimeType = bestAudioMime,
                    bitrate = bestAudioBitrate,
                ),
            )
        }

        val effectiveQualities = if (qualityTracks.isNotEmpty()) {
            qualityTracks
        } else {
            formats.filter { it["url"].asStringOrNull() != null }.mapNotNull { f ->
                val itag = f["itag"].asIntOrNull() ?: return@mapNotNull null
                val rawLabel = f["qualityLabel"].asStringOrNull() ?: "360p"
                val url = f["url"].asStringOrNull() ?: return@mapNotNull null
                val mime = f["mimeType"].asStringOrNull() ?: "video/mp4"
                val height = f["height"].asIntOrNull() ?: 360
                VideoQualityTrack(
                    itag = itag,
                    qualityLabel = rawLabel,
                    url = url,
                    mimeType = mime,
                    height = height,
                    bitrate = f["bitrate"].asLongOrNull() ?: 0L,
                )
            }
        }

        // 4. PRIORITY 1: HD Adaptive Stream (Fast-loading 720p HD default for instant startup & zero buffering, or 1080p)
        val bestHdTrack = qualityTracks.firstOrNull { it.height == 720 }
            ?: qualityTracks.firstOrNull { it.height == 1080 }
            ?: qualityTracks.firstOrNull { it.height >= 480 }
            ?: qualityTracks.firstOrNull()

        if (bestHdTrack != null && bestAudioUrl != null) {
            Log.d(TAG, "Playing Adaptive HD Stream: video=${bestHdTrack.qualityLabel} (itag ${bestHdTrack.itag}), audio=${audioCandidate?.get("itag")}, qualities=${effectiveQualities.size}")
            return StreamResolution(
                videoUrl = bestHdTrack.url,
                audioUrl = bestAudioUrl,
                hlsUrl = null,
                isMuxed = false,
                mimeType = bestHdTrack.mimeType,
                itag = bestHdTrack.itag,
                qualityLabel = bestHdTrack.qualityLabel,
                userAgent = client.userAgent,
                headers = client.playbackHeaders,
                subtitles = subtitleTracks,
                audioLanguages = audioLanguages,
                availableQualities = effectiveQualities,
            )
        }

        // 5. PRIORITY 2: HLS Adaptive Master Playlist
        if (!hlsManifestUrl.isNullOrEmpty()) {
            return StreamResolution(
                videoUrl = null,
                audioUrl = null,
                hlsUrl = hlsManifestUrl,
                isMuxed = false,
                mimeType = "application/x-mpegURL",
                itag = 0,
                qualityLabel = "HD Adaptive (HLS)",
                userAgent = client.userAgent,
                headers = client.playbackHeaders,
                subtitles = subtitleTracks,
                audioLanguages = audioLanguages,
                availableQualities = effectiveQualities,
            )
        }

        // 6. PRIORITY 3: Legacy Muxed Candidate (itag 18 / 22) as fallback
        val muxedCandidate = formats
            .filter { f ->
                f["url"].asStringOrNull() != null
            }
            .maxByOrNull { it["height"].asIntOrNull()?.toLong() ?: (it["bitrate"].asLongOrNull() ?: 0L) }

        if (muxedCandidate != null) {
            val muxedUrl = muxedCandidate["url"].asStringOrNull()
            if (muxedUrl != null) {
                val fallbackQualities = if (effectiveQualities.isNotEmpty()) effectiveQualities else {
                    formats.filter { it["url"].asStringOrNull() != null }.mapNotNull { f ->
                        val itag = f["itag"].asIntOrNull() ?: return@mapNotNull null
                        val rawLabel = f["qualityLabel"].asStringOrNull() ?: "360p"
                        val url = f["url"].asStringOrNull() ?: return@mapNotNull null
                        val mime = f["mimeType"].asStringOrNull() ?: "video/mp4"
                        val height = f["height"].asIntOrNull() ?: 360
                        VideoQualityTrack(
                            itag = itag,
                            qualityLabel = rawLabel,
                            url = url,
                            mimeType = mime,
                            height = height,
                            bitrate = f["bitrate"].asLongOrNull() ?: 0L,
                        )
                    }.sortedByDescending { it.height }
                }.ifEmpty {
                    listOf(
                        VideoQualityTrack(
                            itag = muxedCandidate["itag"].asIntOrNull() ?: 18,
                            qualityLabel = muxedCandidate["qualityLabel"].asStringOrNull() ?: "360p",
                            url = muxedUrl,
                            mimeType = muxedCandidate["mimeType"].asStringOrNull() ?: "video/mp4",
                            height = muxedCandidate["height"].asIntOrNull() ?: 360,
                            bitrate = muxedCandidate["bitrate"].asLongOrNull() ?: 0L,
                        ),
                    )
                }

                val fallbackAudioLanguages = if (audioLanguages.isNotEmpty()) audioLanguages else {
                    listOf(
                        AudioLanguageTrack(
                            id = "default",
                            name = "Original Audio",
                            url = muxedUrl,
                            mimeType = muxedCandidate["mimeType"].asStringOrNull() ?: "video/mp4",
                            bitrate = muxedCandidate["bitrate"].asLongOrNull() ?: 0L,
                            itag = muxedCandidate["itag"].asIntOrNull() ?: 18,
                        ),
                    )
                }

                return StreamResolution(
                    videoUrl = muxedUrl,
                    audioUrl = null,
                    hlsUrl = null,
                    isMuxed = true,
                    mimeType = muxedCandidate["mimeType"].asStringOrNull() ?: "video/mp4",
                    itag = muxedCandidate["itag"].asIntOrNull() ?: 18,
                    qualityLabel = muxedCandidate["qualityLabel"].asStringOrNull() ?: "360p",
                    userAgent = client.userAgent,
                    headers = client.playbackHeaders,
                    subtitles = subtitleTracks,
                    audioLanguages = fallbackAudioLanguages,
                    availableQualities = fallbackQualities,
                )
            }
        }

        // 7. Last resort: Video-only fallback
        if (bestHdTrack != null) {
            return StreamResolution(
                videoUrl = bestHdTrack.url,
                audioUrl = null,
                hlsUrl = null,
                isMuxed = false,
                mimeType = bestHdTrack.mimeType,
                itag = bestHdTrack.itag,
                qualityLabel = bestHdTrack.qualityLabel,
                userAgent = client.userAgent,
                headers = client.playbackHeaders,
                subtitles = subtitleTracks,
                audioLanguages = audioLanguages,
                availableQualities = qualityTracks,
            )
        }

        return null
    }

    private fun JsonObject.hasAudio(): Boolean {
        val mime = this["mimeType"].asStringOrNull() ?: return false
        return mime.contains("mp4a") || mime.contains("opus")
    }

    // ------ /search parse ------

    private fun collectVideoRenderers(element: JsonElement): List<JsonObject> {
        val out = mutableListOf<JsonObject>()
        fun walk(e: JsonElement) {
            when (e) {
                is JsonObject -> {
                    val vr = e["videoRenderer"]
                    if (vr is JsonObject) out.add(vr)
                    e.values.forEach(::walk)
                }
                is JsonArray -> e.forEach(::walk)
                else -> Unit
            }
        }
        walk(element)
        return out
    }

    private fun JsonObject.toVideoItem(): VideoItem? {
        val id = this["videoId"].asStringOrNull()?.takeIf { it.isNotBlank() } ?: return null
        val title = this["title"].asTextRuns() ?: return null
        val durationMs = parseDuration(this["lengthText"].asSimpleOrRunText())
        if (durationMs == 0L) return null // live / no-duration items skip
        val channel = this["ownerText"].asTextRuns()
            ?: this["longBylineText"].asTextRuns()
            ?: "YouTube"
        val thumb = this["thumbnail"]
            ?.asJsonObjectOrNull()?.get("thumbnails")?.asJsonArrayOrNull()
            ?.lastOrNull()?.asJsonObjectOrNull()?.get("url").asStringOrNull()

        val viewCount = this["shortViewCountText"].asSimpleOrRunText()
            ?: this["viewCountText"].asSimpleOrRunText()
        val publishedTime = this["publishedTimeText"].asSimpleOrRunText()
        val channelThumb = this["channelThumbnailSupportedRenderers"]
            ?.asJsonObjectOrNull()?.get("channelThumbnailWithLinkRenderer")
            ?.asJsonObjectOrNull()?.get("thumbnail")?.asJsonObjectOrNull()
            ?.get("thumbnails")?.asJsonArrayOrNull()?.firstOrNull()
            ?.asJsonObjectOrNull()?.get("url").asStringOrNull()

        val channelId = this["ownerText"]?.asJsonObjectOrNull()?.get("runs")?.asJsonArrayOrNull()
            ?.firstOrNull()?.asJsonObjectOrNull()?.get("navigationEndpoint")?.asJsonObjectOrNull()
            ?.get("browseEndpoint")?.asJsonObjectOrNull()?.get("browseId").asStringOrNull()
            ?: this["channelThumbnailSupportedRenderers"]?.asJsonObjectOrNull()
                ?.get("channelThumbnailWithLinkRenderer")?.asJsonObjectOrNull()
                ?.get("navigationEndpoint")?.asJsonObjectOrNull()
                ?.get("browseEndpoint")?.asJsonObjectOrNull()?.get("browseId").asStringOrNull()

        return VideoItem(
            videoId = id,
            title = title,
            channel = channel,
            durationMs = durationMs,
            thumbnailUrl = thumb,
            viewCount = viewCount,
            publishedTime = publishedTime,
            channelThumbnailUrl = channelThumb,
            channelId = channelId,
        )
    }

    // ------ plumbing ------

    private fun executePost(
        url: String,
        body: JsonObject,
        headers: Map<String, String>,
    ): String {
        val request = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .apply { headers.forEach { (k, v) -> header(k, v) } }
            .build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("InnerTube HTTP ${response.code}")
            return response.body?.string() ?: throw IOException("Empty InnerTube response")
        }
    }

    companion object {
        private const val TAG = "InnerTubeVideoClient"
        const val BASE = "https://www.youtube.com"
        const val WEB_CLIENT_NAME = "WEB"
        const val WEB_CLIENT_VERSION = "2.20260708.01.00"
        const val WEB_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"

        private fun defaultHttp(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build()
    }
}
