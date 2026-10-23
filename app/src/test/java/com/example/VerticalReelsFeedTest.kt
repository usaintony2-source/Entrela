package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.FirestoreReelsRepository
import com.example.model.ReelComment
import com.example.model.ReelItem
import com.example.ui.reels.VerticalReelsFeed
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VerticalReelsFeedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleReel = ReelItem(
        id = "reel_test_101",
        creatorId = "c_elena",
        creatorName = "Elena Rostova",
        creatorUsername = "elena_r",
        creatorAvatarEmoji = "🌸",
        caption = "Exploring neon Tokyo night alleys ✨ #TokyoSparks #Entrela",
        tags = listOf("#TokyoSparks", "#Entrela"),
        audioTrack = "Neon Romance - Tokyo City Pop",
        artistName = "Lofi Nights",
        likesCount = 2450,
        isLiked = false,
        commentsCount = 88,
        sharesCount = 120,
        comments = listOf(
            ReelComment("c_1", "Alex Rivera", "💫", "Incredible cinematography! 🔥", "10m ago", 5)
        )
    )

    @Test
    fun testVerticalReelsFeedRendersReelContent() {
        composeTestRule.setContent {
            MyApplicationTheme {
                VerticalReelsFeed(
                    reels = listOf(sampleReel),
                    onLikeReel = {},
                    onCommentReel = {}
                )
            }
        }

        // Verify vertical reels feed container and pager
        composeTestRule.onNodeWithTag("vertical_reels_feed_container").assertExists()
        composeTestRule.onNodeWithTag("vertical_reels_pager").assertExists()

        // Verify reel page and video card
        composeTestRule.onNodeWithTag("reel_page_reel_test_101").assertExists()
        composeTestRule.onNodeWithTag("reel_video_reel_test_101").assertExists()

        // Verify creator username and caption
        composeTestRule.onNodeWithTag("reel_creator_reel_test_101").assertExists()
        composeTestRule.onNodeWithText("@elena_r").assertExists()
        composeTestRule.onNodeWithTag("reel_caption_reel_test_101").assertExists()
        composeTestRule.onNodeWithText("Exploring neon Tokyo night alleys ✨ #TokyoSparks #Entrela").assertExists()

        // Verify Action Rail buttons
        composeTestRule.onNodeWithTag("reel_like_reel_test_101").assertExists()
        composeTestRule.onNodeWithTag("reel_comments_reel_test_101").assertExists()
        composeTestRule.onNodeWithTag("reel_share_reel_test_101").assertExists()
        composeTestRule.onNodeWithTag("reel_bookmark_reel_test_101").assertExists()
        composeTestRule.onNodeWithTag("reel_vinyl_reel_test_101").assertExists()
    }

    @Test
    fun testLikeAndCommentInteractions() {
        var likedReelId: String? = null
        var commentedReelId: String? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                VerticalReelsFeed(
                    reels = listOf(sampleReel),
                    onLikeReel = { likedReelId = it },
                    onCommentReel = { commentedReelId = it }
                )
            }
        }

        // Test Like interaction
        composeTestRule.onNodeWithTag("reel_like_reel_test_101").performClick()
        assertEquals("reel_test_101", likedReelId)

        // Test Comment interaction
        composeTestRule.onNodeWithTag("reel_comments_reel_test_101").performClick()
        assertEquals("reel_test_101", commentedReelId)
    }

    @Test
    fun testEmptyStateWhenNoReels() {
        composeTestRule.setContent {
            MyApplicationTheme {
                VerticalReelsFeed(
                    reels = emptyList(),
                    onLikeReel = {},
                    onCommentReel = {}
                )
            }
        }

        // Verify empty state is displayed
        composeTestRule.onNodeWithTag("reels_empty_state").assertExists()
        composeTestRule.onNodeWithText("No Reels Available").assertExists()
    }

    @Test
    fun testVisualPlaybackProgressIndicator() {
        composeTestRule.setContent {
            MyApplicationTheme {
                VerticalReelsFeed(
                    reels = listOf(sampleReel),
                    onLikeReel = {},
                    onCommentReel = {},
                    playbackProgressOverride = 0.45f
                )
            }
        }

        // Verify visual progress indicator and progress bar exist at the bottom of the reel
        composeTestRule.onNodeWithTag("reel_progress_indicator_reel_test_101").assertExists()
        composeTestRule.onNodeWithTag("reel_progress_bar_reel_test_101").assertExists()
    }

    @Test
    fun testDoubleTapGestureTogglesLikeHeartAnimation() {
        var likeToggleCount = 0

        composeTestRule.setContent {
            MyApplicationTheme {
                VerticalReelsFeed(
                    reels = listOf(sampleReel),
                    onLikeReel = { likeToggleCount++ },
                    onCommentReel = {}
                )
            }
        }

        // Initially, the heart pop animation is not visible
        composeTestRule.onNodeWithTag("heart_pop_reel_test_101").assertDoesNotExist()

        // Perform first double-tap gesture on the reel page
        composeTestRule.onNodeWithTag("reel_page_reel_test_101").performTouchInput {
            doubleClick()
        }

        // Verify like callback was triggered and heart animation is toggled ON
        assertEquals(1, likeToggleCount)
        composeTestRule.onNodeWithTag("heart_pop_reel_test_101").assertExists()
        composeTestRule.onNodeWithTag("double_tap_heart_icon_reel_test_101").assertExists()

        // Perform second double-tap gesture to toggle the heart animation OFF
        composeTestRule.onNodeWithTag("reel_page_reel_test_101").performTouchInput {
            doubleClick()
        }

        assertEquals(2, likeToggleCount)
        composeTestRule.onNodeWithTag("heart_pop_reel_test_101").assertDoesNotExist()
    }

    @Test
    fun testFirestoreReelsRepositoryMethods() {
        val repo = FirestoreReelsRepository()

        // Verify toggleLikeReel runs safely without unhandled exceptions
        repo.toggleLikeReel("reel_test_101", "user_123", true)
        repo.toggleLikeReel("reel_test_101", "user_123", false)

        // Verify addCommentToReel runs safely without unhandled exceptions
        repo.addCommentToReel(
            "reel_test_101",
            ReelComment("c_test_99", "Sarah", "✨", "Beautiful!", "Just now")
        )

        assertTrue(true)
    }
}
