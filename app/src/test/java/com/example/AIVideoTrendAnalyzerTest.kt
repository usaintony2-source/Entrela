package com.example

import com.example.ai.AIVideoTrendAnalyzer
import com.example.data.EntrelaRepository
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class AIVideoTrendAnalyzerTest {

    @Test
    fun testAIVideoTrendAnalysisCalculation() {
        val draft = VideoDraft(
            videoRes = 1,
            originalDurationSec = 15.0f,
            trimStartSec = 1.0f,
            trimEndSec = 11.0f, // 10s optimal duration
            filter = VideoFilterItem("f1", "Cyberpunk Neon", "🌆"),
            transition = VideoTransitionItem("t1", "Cross-Fade", "✨"),
            audioTrack = AudioTrackItem("sp_1", "Blinding Lights", "The Weeknd", "3:20", "🎵", AudioSourceType.SPOTIFY, 98),
            originalAudioVolume = 0.5f,
            musicVolume = 0.9f
        )

        val resultWorldwide = AIVideoTrendAnalyzer.analyzeVideo(
            draft = draft,
            caption = "Amazing Tokyo city lights! #EntrelaSparks #NightVibes #TokyoSparks",
            region = AIVideoTrendAnalyzer.supportedCountries[0]
        )

        assertTrue("Trend score should be high for optimal reel", resultWorldwide.trendScore >= 85.0f)
        assertTrue("Loop rate should be estimated >= 2.0x", resultWorldwide.estimatedLoopRate >= 2.0f)
        assertTrue("Retention should be above 90%", resultWorldwide.watchTimeRetentionPct >= 90)
        assertNotNull(resultWorldwide.topAudioTrack)
        assertNotNull(resultWorldwide.topFilterName)
        assertTrue("Recommendations should not be empty", resultWorldwide.aiSuggestions.isNotEmpty())
        assertTrue("Hashtags should contain recommendations", resultWorldwide.recommendedHashtags.isNotEmpty())
    }

    @Test
    fun testRegionalTrendDifferences() {
        val draft = VideoDraft(
            videoRes = 1,
            originalDurationSec = 12.0f,
            trimStartSec = 0.0f,
            trimEndSec = 12.0f,
            filter = VideoFilterItem("f3", "Tokyo Glow", "🏮"),
            transition = VideoTransitionItem("t0", "Cut", "✂️")
        )

        val japanRegion = AIVideoTrendAnalyzer.supportedCountries.first { it.code == "JP" }
        val resultJapan = AIVideoTrendAnalyzer.analyzeVideo(
            draft = draft,
            caption = "Tokyo Night Walk",
            region = japanRegion
        )

        assertEquals("Tokyo Glow", resultJapan.topFilterName)
        assertTrue(resultJapan.recommendedHashtags.any { it.contains("Tokyo", ignoreCase = true) })
    }

    @Test
    fun testVideoExportRankInjection() {
        val repository = EntrelaRepository()
        val draft = VideoDraft(
            videoRes = 1,
            originalDurationSec = 10.0f,
            trimStartSec = 0.0f,
            trimEndSec = 10.0f,
            filter = VideoFilterItem("f1", "Cyberpunk Neon", "🌆"),
            transition = VideoTransitionItem("t1", "Cross-Fade", "✨"),
            audioTrack = repository.spotifyTracks.first()
        )

        val path = repository.saveExportedVideoToStorage(draft, "Viral Sunset Reel #Entrela")
        assertTrue("Export path should contain .mp4", path.endsWith(".mp4"))

        val leaderboard = repository.allLeaderboardItems.value
        assertTrue("Leaderboard should have items", leaderboard.isNotEmpty())
        val userItem = leaderboard.find { it.title.contains("Viral Sunset") }
        assertNotNull("User item should be present on leaderboard", userItem)
        assertTrue("User item should have high trend score", (userItem?.trendScore ?: 0f) > 80f)
    }
}
