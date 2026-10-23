package com.nexusdigital.rivavideo.data

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.contentOrNull

// ------- models -------

data class VideoItem(
    val videoId: String,
    val title: String,
    val channel: String,
    val durationMs: Long,
    val thumbnailUrl: String?,
    val viewCount: String? = null,
    val publishedTime: String? = null,
    val channelThumbnailUrl: String? = null,
    val channelId: String? = null,
)

data class ChannelDetails(
    val channelId: String,
    val title: String,
    val handle: String? = null,
    val subscriberCount: String? = null,
    val videoCount: String? = null,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val description: String? = null,
    val isSubscribed: Boolean = false,
    val videos: List<VideoItem> = emptyList(),
)

data class ShortItem(
    val videoId: String,
    val title: String,
    val channel: String = "YouTube Creator",
    val channelId: String? = null,
    val channelAvatarUrl: String? = null,
    val thumbnailUrl: String? = null,
    val viewCount: String? = null,
    val likesCount: String = "180K",
    val commentsCount: String = "2.6K",
    val isLiked: Boolean = false,
)

@kotlinx.serialization.Serializable
data class OfflineVideoItem(
    val videoId: String,
    val title: String,
    val channel: String,
    val durationMs: Long,
    val thumbnailUrl: String?,
    val savedAtMs: Long = System.currentTimeMillis(),
)

@kotlinx.serialization.Serializable
data class WatchHistoryItem(
    val videoId: String,
    val title: String,
    val channel: String,
    val durationMs: Long,
    val thumbnailUrl: String?,
    val watchedAtMs: Long = System.currentTimeMillis(),
)

enum class RegionPreference(
    val id: String,
    val title: String,
    val flag: String,
    val description: String,
    val defaultQuery: String,
) {
    INDIA(
        id = "india",
        title = "India (Trending & Top Creators)",
        flag = "🇮🇳",
        description = "Indian Trending, CarryMinati, Total Gaming, BB Ki Vines, Bollywood, Hindi Comedy",
        defaultQuery = "Indian trending videos carryminati total gaming bhuvan bam carry dhruv rathee round2hell techno gamerz",
    ),
    GLOBAL(
        id = "global",
        title = "Global / Worldwide",
        flag = "🌍",
        description = "Trending international videos, top English creators, pop music, tech & cinema",
        defaultQuery = "trending viral videos mrbeast music cinema technology",
    ),
    HINDI_DUBBED(
        id = "hindi_dubbed",
        title = "Hindi Dubbed Movies",
        flag = "🎬",
        description = "South Indian Hindi dubbed blockbuster movies, Hollywood Hindi action",
        defaultQuery = "south indian hindi dubbed full movie blockbuster action hindi",
    ),
    DEVOTIONAL(
        id = "devotional",
        title = "Devotional & Bhakti",
        flag = "🕉️",
        description = "Bhakti geet, aarti, bhajan, katha, live darshan & spiritual satsang",
        defaultQuery = "bhakti geet aarti bhajan katha ramayan krishna live satsang",
    ),
    GAMING(
        id = "gaming",
        title = "Gaming & Esports",
        flag = "🎮",
        description = "Live gaming streams, esports tournaments, GTA 5, BGMI, Free Fire",
        defaultQuery = "trending gaming live stream techno gamerz total gaming mortal bgmi gta 5",
    )
}

data class SubtitleTrack(
    val languageCode: String,
    val name: String,
    val vttUrl: String,
)

data class AudioLanguageTrack(
    val id: String,
    val name: String,
    val url: String,
    val mimeType: String = "audio/mp4",
    val bitrate: Long = 0L,
    val itag: Int = 0,
)

data class VideoQualityTrack(
    val itag: Int,
    val qualityLabel: String,
    val url: String,
    val mimeType: String = "video/mp4",
    val height: Int = 720,
    val bitrate: Long = 0L,
)

data class StreamResolution(
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val hlsUrl: String? = null,
    val isMuxed: Boolean = false,
    val mimeType: String = "video/mp4",
    val itag: Int = 0,
    val qualityLabel: String? = null,
    val userAgent: String = "",
    val headers: Map<String, String> = emptyMap(),
    val subtitles: List<SubtitleTrack> = emptyList(),
    val audioLanguages: List<AudioLanguageTrack> = emptyList(),
    val availableQualities: List<VideoQualityTrack> = emptyList(),
    val expiresAtMs: Long = System.currentTimeMillis() + 2 * 60 * 60 * 1000L,
) {
    /** Backwards-compatible or simple stream URL fallback. */
    val url: String
        get() = hlsUrl ?: videoUrl ?: audioUrl ?: ""
}

// ------- JSON helpers (shared model file) -------

fun JsonElement?.asJsonObjectOrNull(): JsonObject? = this as? JsonObject

fun JsonElement?.asJsonArrayOrNull(): JsonArray? = this as? JsonArray

fun JsonElement?.asStringOrNull(): String? {
    if (this == null || this is JsonNull) return null
    if (this is JsonPrimitive && this.isString) return contentOrNull
    return null
}

fun JsonElement?.asIntOrNull(): Int? {
    if (this == null || this is JsonNull) return null
    if (this is JsonPrimitive) {
        if (this.isString) return this.contentOrNull?.toIntOrNull()
        return this.doubleOrNull?.toInt()
    }
    return null
}

fun JsonElement?.asLongOrNull(): Long? {
    if (this == null || this is JsonNull) return null
    if (this is JsonPrimitive) {
        if (this.isString) return this.contentOrNull?.toLongOrNull()
        return this.doubleOrNull?.toLong()
    }
    return null
}

fun JsonElement?.asBooleanOrNull(): Boolean? {
    if (this == null || this is JsonNull) return null
    if (this is JsonPrimitive) {
        return this.contentOrNull?.toBooleanStrictOrNull()
    }
    return null
}

/** ["run1", "run2"] -> "run1run2". */
fun JsonElement?.asTextRuns(): String? {
    val runs = this?.asJsonObjectOrNull()?.get("runs")?.asJsonArrayOrNull() ?: return null
    return runs.joinToString("") { run ->
        run.asJsonObjectOrNull()?.get("text").asStringOrNull() ?: ""
    }.ifBlank { null }
}

fun JsonElement?.asSimpleText(): String? =
    this?.asJsonObjectOrNull()?.get("simpleText").asStringOrNull()

/** Checks simpleText first; if absent, falls back to runs[].text. */
fun JsonElement?.asSimpleOrRunText(): String? =
    this?.asSimpleText() ?: this?.asTextRuns()

/** "8:09" / "1:02:33" -> ms. */
fun parseDuration(text: String?): Long {
    if (text.isNullOrBlank()) return 0L
    val parts = text.split(":").map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.any { it.toIntOrNull() == null }) return 0L
    val seconds = parts.fold(0L) { acc, p -> acc * 60 + (p.toLongOrNull() ?: 0L) }
    return seconds * 1000L
}

/** ms -> "8:09" / "1:02:33". */
fun formatDuration(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%d:%02d".format(m, sec)
}

/**
 * Approximate age of a video in seconds from relative strings like:
 * "5 minutes ago", "3 hours ago", "2 days ago", "1 week ago", "4 months ago", "2 years ago"
 * Supports English and Hindi phrases.
 * Returns 0 for "Just now" / live, and Long.MAX_VALUE - 1000L for unknown.
 */
fun parseRelativeAgeSeconds(pubTime: String?): Long {
    if (pubTime.isNullOrBlank()) return Long.MAX_VALUE - 1000L
    val lower = pubTime.lowercase().trim()
    if (lower.contains("just now") || lower.contains("live") || lower.contains("watching") ||
        lower.contains("premieres") || lower.contains("premiere") || lower.contains("अभी") || lower.contains("लाइव")
    ) {
        return 0L
    }

    // Number followed by time unit
    val regex = Regex("""(\d+)\s*(second|minute|hour|day|week|month|year|सेकंड|मिनट|घंटे|घंटा|दिन|हफ्ते|हफ़्ते|सप्ताह|महीने|महीना|साल|वर्ष)""")
    val match = regex.find(lower)
    if (match != null) {
        val count = match.groupValues[1].toLongOrNull() ?: 1L
        val unit = match.groupValues[2]
        val mult = when {
            unit.startsWith("second") || unit.contains("सेकंड") -> 1L
            unit.startsWith("minute") || unit.contains("मिनट") -> 60L
            unit.startsWith("hour") || unit.contains("घंट") -> 3600L
            unit.startsWith("day") || unit.contains("दिन") -> 86400L
            unit.startsWith("week") || unit.contains("हफ्त") || unit.contains("सप्ताह") -> 7L * 86400L
            unit.startsWith("month") || unit.contains("महीन") -> 30L * 86400L
            unit.startsWith("year") || unit.contains("साल") || unit.contains("वर्ष") -> 365L * 86400L
            else -> 86400L
        }
        return count * mult
    }

    // Single unit e.g. "a day ago", "an hour ago", "a month ago"
    val singleRegex = Regex("""(a|an|one|ek|ik)\s*(second|minute|hour|day|week|month|year)""")
    val singleMatch = singleRegex.find(lower)
    if (singleMatch != null) {
        val unit = singleMatch.groupValues[2]
        return when {
            unit.startsWith("second") -> 1L
            unit.startsWith("minute") -> 60L
            unit.startsWith("hour") -> 3600L
            unit.startsWith("day") -> 86400L
            unit.startsWith("week") -> 7L * 86400L
            unit.startsWith("month") -> 30L * 86400L
            unit.startsWith("year") -> 365L * 86400L
            else -> 86400L
        }
    }

    return Long.MAX_VALUE - 1000L
}

/** Parses views like "132M views", "50K views", "1.2B views", "701 views" to numeric count for sorting. */
fun parseViews(viewsStr: String?): Long {
    if (viewsStr.isNullOrBlank()) return 0L
    val clean = viewsStr.lowercase().replace(",", "").replace("views", "").replace("view", "").trim()
    val regex = Regex("""([\d.]+)\s*([kmbcr]?)""")
    val match = regex.find(clean) ?: return 0L
    val num = match.groupValues[1].toDoubleOrNull() ?: 0.0
    val suffix = match.groupValues[2]
    val mult = when (suffix) {
        "k" -> 1_000.0
        "m" -> 1_000_000.0
        "b" -> 1_000_000_000.0
        "cr" -> 10_000_000.0 // crore in Indian notation
        else -> 1.0
    }
    return (num * mult).toLong()
}
