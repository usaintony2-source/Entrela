package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.EntrelaRepository
import com.example.model.AudioSourceType
import com.example.model.VideoDraft
import com.example.model.VideoFilterItem
import com.example.model.VideoTransitionItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Entrela", appName)
    }

    @Test
    fun `test entrela auth and reels flow`() {
        val repository = EntrelaRepository()
        
        // Test Login
        val loginSuccess = repository.login("user@entrela.app", "secretPass")
        assertTrue(loginSuccess)
        assertEquals("User", repository.currentUser.value?.name)

        // Test Reels
        val initialReels = repository.reels.value
        assertTrue(initialReels.isNotEmpty())
        val firstReelId = initialReels.first().id
        val initialLikes = initialReels.first().likesCount

        // Like Reel
        repository.toggleLikeReel(firstReelId)
        val afterLike = repository.reels.value.first { it.id == firstReelId }
        assertTrue(afterLike.likesCount != initialLikes)

        // Add Comment
        repository.addCommentToReel(firstReelId, "Amazing vibe! ✨")
        val afterComment = repository.reels.value.first { it.id == firstReelId }
        assertEquals("Amazing vibe! ✨", afterComment.comments.first().text)

        // Test Password Reset
        val code = repository.requestPasswordReset("user@entrela.app")
        assertNotNull(code)
        val resetSuccess = repository.resetPasswordWithCode(code, "newStrongPassword123")
        assertTrue(resetSuccess)
    }

    @Test
    fun `test live stream and video editor export`() {
        val repository = EntrelaRepository()

        // Test Live Stream
        repository.startLiveStream()
        assertTrue(repository.isLiveStreaming.value)
        repository.sendLiveComment("Testing live broadcast stream! 🔥", isSpark = true)
        assertTrue(repository.liveComments.value.any { it.text.contains("Testing live broadcast") })
        repository.endLiveStream()
        assertTrue(!repository.isLiveStreaming.value)

        // Test Spotify & Local Audio Catalogs
        assertTrue(repository.spotifyTracks.isNotEmpty())
        assertTrue(repository.spotifyTracks.first().sourceType == AudioSourceType.SPOTIFY)
        assertTrue(repository.localTracks.isNotEmpty())
        assertTrue(repository.localTracks.first().sourceType == AudioSourceType.LOCAL_STORAGE)

        // Test Video Draft Export & Download
        val draft = VideoDraft(
            videoRes = R.drawable.reel_bg_party_1792260318860,
            originalDurationSec = 15f,
            trimStartSec = 0f,
            trimEndSec = 10f,
            filter = VideoFilterItem("f1", "Cyberpunk Neon", "🌆", 0x28FF2D6C),
            transition = VideoTransitionItem("t1", "Cross-Fade", "✨"),
            audioTrack = repository.spotifyTracks.first()
        )
        val savedPath = repository.saveExportedVideoToStorage(draft, "My edited video reel")
        assertTrue(savedPath.contains("ENTRELA_EXPORT_"))
        assertTrue(savedPath.endsWith(".mp4"))
        // Newly exported video should now appear in reels
        assertTrue(repository.reels.value.any { it.caption.contains("My edited video reel") })

        // Test Location Proximity
        repository.setLocationEnabled(true)
        assertTrue(repository.isLocationEnabled.value)
    }
}
