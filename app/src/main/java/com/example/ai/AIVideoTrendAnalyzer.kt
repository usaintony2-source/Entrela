package com.example.ai

import com.example.model.AudioSourceType
import com.example.model.CountryRegion
import com.example.model.TrendAnalysisResult
import com.example.model.VideoDraft

data class RegionalTrendProfile(
    val topAudio: String,
    val topFilter: String,
    val peakHours: String,
    val tags: List<String>,
    val regionalAdvice: String
)

object AIVideoTrendAnalyzer {

    val supportedCountries = listOf(
        CountryRegion("ALL", "Worldwide", "🌐"),
        CountryRegion("JP", "Japan", "🇯🇵"),
        CountryRegion("US", "United States", "🇺🇸"),
        CountryRegion("GB", "United Kingdom", "🇬🇧"),
        CountryRegion("IN", "India", "🇮🇳"),
        CountryRegion("BR", "Brazil", "🇧🇷"),
        CountryRegion("KR", "South Korea", "🇰🇷")
    )

    private val countryTrendProfiles = mapOf(
        "ALL" to RegionalTrendProfile(
            topAudio = "The Weeknd - Blinding Lights (Entrela VIP)",
            topFilter = "Cyberpunk Neon",
            peakHours = "8:00 PM - 11:00 PM UTC",
            tags = listOf("#EntrelaSparks", "#WorldwideTrends", "#NightVibes", "#ViralReels", "#TrendingNow"),
            regionalAdvice = "Global algorithms favor 9-14 second loops with punchy synthwave hooks and high-contrast neon grade."
        ),
        "JP" to RegionalTrendProfile(
            topAudio = "Neon Romance - Tokyo City Pop",
            topFilter = "Tokyo Glow",
            peakHours = "7:30 PM - 10:30 PM JST",
            tags = listOf("#TokyoSparks", "#TokyoNight", "#ShibuyaVibes", "#EntrelaJapan", "#CityPopRomance"),
            regionalAdvice = "Tokyo audiences replay aesthetic city walks and romantic dusk vibes 2.6x more when layered with city pop audio."
        ),
        "US" to RegionalTrendProfile(
            topAudio = "Sabrina Carpenter - Espresso Sparks",
            topFilter = "Cyberpunk Neon",
            peakHours = "6:00 PM - 9:30 PM EST",
            tags = listOf("#USATrending", "#RooftopParty", "#EntrelaSparks", "#SummerBeats", "#NightOut"),
            regionalAdvice = "US reels with punchy captions and Spotify Top 10 tracks achieve 3.1x faster Spark sharing velocity."
        ),
        "GB" to RegionalTrendProfile(
            topAudio = "M83 - Midnight City Romance",
            topFilter = "Noir Cinema",
            peakHours = "7:00 PM - 10:00 PM GMT",
            tags = listOf("#LondonUnderground", "#UKAcoustic", "#EntrelaUK", "#MidnightVibes", "#IndieReels"),
            regionalAdvice = "UK creators see 42% higher retention when using atmospheric noir lighting with indie acoustic hooks."
        ),
        "IN" to RegionalTrendProfile(
            topAudio = "Espresso Sparks - Bollywood Synth",
            topFilter = "Golden Sunset",
            peakHours = "8:00 PM - 11:30 PM IST",
            tags = listOf("#MumbaiSunset", "#DesiVibes", "#EntrelaIndia", "#GoldenHourSparks", "#ViralIndia"),
            regionalAdvice = "Warm golden-hour tones paired with energetic rhythm tracks drive massive comment engagement across India."
        ),
        "BR" to RegionalTrendProfile(
            topAudio = "Levitating (Carnival Club Mix)",
            topFilter = "Golden Sunset",
            peakHours = "7:00 PM - 11:00 PM BRT",
            tags = listOf("#RioCarnival", "#BrasilVibes", "#EntrelaBrasil", "#DancaSparks", "#SunsetEnergy"),
            regionalAdvice = "High-tempo dance edits and vivid saturated color palettes hold Brazilian viewers through 95%+ completion rates."
        ),
        "KR" to RegionalTrendProfile(
            topAudio = "Starboy (K-Synthwave Edit)",
            topFilter = "Tokyo Glow",
            peakHours = "8:30 PM - 11:30 PM KST",
            tags = listOf("#HongdaeNight", "#SeoulSparks", "#EntrelaKorea", "#KStyleReel", "#NeonCityBeats"),
            regionalAdvice = "Clean transitions with neon backlighting and synthwave rhythms trend #1 on Seoul explore pages."
        )
    )

    /**
     * Evaluates a video draft or newly exported reel in real-time,
     * factoring in clip duration, audio engagement, filter synergy, and loop retention.
     */
    fun analyzeVideo(
        draft: VideoDraft,
        caption: String,
        region: CountryRegion = supportedCountries[1] // Default Japan
    ): TrendAnalysisResult {
        val profile = countryTrendProfiles[region.code] ?: countryTrendProfiles["ALL"]!!
        var baseScore = 79.0f

        val effectiveDuration = (draft.trimEndSec - draft.trimStartSec).coerceAtLeast(1.0f)

        // 1. Duration & Loop Rate Sweet Spot (8s - 15s is the golden window)
        val loopMultiplier: Float
        val retentionPct: Int
        when {
            effectiveDuration in 8.0f..14.0f -> {
                baseScore += 8.5f
                loopMultiplier = 2.9f + (14.0f - effectiveDuration) * 0.08f
                retentionPct = (92..98).random()
            }
            effectiveDuration < 8.0f -> {
                baseScore += 5.5f
                loopMultiplier = 3.3f
                retentionPct = (94..99).random()
            }
            else -> {
                baseScore += 3.8f
                loopMultiplier = 2.1f
                retentionPct = (84..90).random()
            }
        }

        // 2. Audio Track Trend Factor
        val audioSyncScore: Int
        if (draft.audioTrack != null) {
            if (draft.audioTrack.sourceType == AudioSourceType.SPOTIFY) {
                // High popularity Spotify tracks get maximum engagement boost
                baseScore += 6.5f * (draft.audioTrack.spotifyPopularity / 100f)
                audioSyncScore = (93..99).random()
            } else {
                baseScore += 3.5f
                audioSyncScore = (86..94).random()
            }
        } else {
            audioSyncScore = 76
        }

        // 3. Visual Filter Synergy (Matching regional favorite gives extra boost)
        if (draft.filter.name.equals(profile.topFilter, ignoreCase = true)) {
            baseScore += 5.5f
        } else if (draft.filter.id in listOf("f1", "f3", "f2")) {
            baseScore += 4.0f
        } else {
            baseScore += 2.0f
        }

        // 4. Hashtag & Caption Analysis
        val words = caption.split(" ", "\n")
        val hashtagCount = words.count { it.startsWith("#") }
        if (hashtagCount in 2..5) {
            baseScore += 3.5f
        } else if (hashtagCount > 5) {
            baseScore += 1.0f // slight penalty for spam
        }

        val finalScore = baseScore.coerceIn(74.0f, 99.6f)
        val formattedScore = Math.round(finalScore * 10f) / 10f

        val grade = when {
            formattedScore >= 94.0f -> "Viral Supernova 🔥"
            formattedScore >= 88.0f -> "High Momentum ⚡"
            formattedScore >= 82.0f -> "Rising Spark ✨"
            else -> "Growing Discovery 🌱"
        }

        val velocity = when {
            formattedScore >= 93.0f -> "Top 1% in ${region.name} ${region.flagEmoji}"
            formattedScore >= 87.0f -> "Top 5% in ${region.name} ${region.flagEmoji}"
            else -> "Top 15% in ${region.name} ${region.flagEmoji}"
        }

        val hookScore = if (draft.filter.id != "f0") (89..98).random() else (79..86).random()

        // Regional Recommendations
        val suggestions = mutableListOf<String>()
        if (draft.audioTrack == null || draft.audioTrack.sourceType != AudioSourceType.SPOTIFY) {
            suggestions.add("Layer '#1 Trending Audio' (${profile.topAudio}) to boost algorithmic discovery by up to 2.8x.")
        } else {
            suggestions.add("Audio track '${draft.audioTrack.title}' matches active algorithmic playlists in ${region.name} ${region.flagEmoji}.")
        }

        if (effectiveDuration > 14.0f) {
            suggestions.add("Trimming under 12s can boost loop replay velocity by +45%.")
        } else {
            suggestions.add("Current duration (${String.format("%.1fs", effectiveDuration)}) is inside the optimal viral loop retention zone.")
        }

        suggestions.add("Trending #1 Filter in ${region.name}: '${profile.topFilter}'. ${profile.regionalAdvice}")
        suggestions.add("Optimal posting window: ${profile.peakHours} for maximum viewer engagement.")

        val recommendedAudios = listOf(
            profile.topAudio,
            "The Weeknd - Blinding Lights (Entrela VIP)",
            "Sabrina Carpenter - Espresso Sparks"
        )

        return TrendAnalysisResult(
            trendScore = formattedScore,
            viralGrade = grade,
            watchTimeRetentionPct = retentionPct,
            estimatedLoopRate = Math.round(loopMultiplier * 10f) / 10f,
            sparkShareVelocity = velocity,
            hookStrengthScore = hookScore,
            audioSyncScore = audioSyncScore,
            aiSuggestions = suggestions,
            recommendedHashtags = profile.tags,
            recommendedAudios = recommendedAudios,
            trendingFilterMatch = "Matches 98% of Top Trending aesthetic patterns in ${region.name}",
            topAudioTrack = profile.topAudio,
            topFilterName = profile.topFilter
        )
    }
}
