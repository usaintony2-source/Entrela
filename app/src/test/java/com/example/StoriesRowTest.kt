package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.model.StoryItem
import com.example.model.UserStoryGroup
import com.example.ui.stories.StoriesRow
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StoriesRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testStoriesRowRendersAvatarsAndStatusIndicators() {
        val testStories = listOf(
            UserStoryGroup(
                userId = "user_me",
                userName = "Your Story",
                userAvatarEmoji = "💫",
                isCurrentUser = true,
                hasUnseenStories = false,
                isOnline = true,
                isLive = false,
                stories = listOf(
                    StoryItem(
                        id = "s_my1",
                        caption = "Morning espresso ☕",
                        timestamp = "2h ago"
                    )
                )
            ),
            UserStoryGroup(
                userId = "u_mateo",
                userName = "Mateo",
                userAvatarEmoji = "⚡",
                isCurrentUser = false,
                hasUnseenStories = true,
                isOnline = true,
                isLive = true, // LIVE status indicator
                stories = listOf(
                    StoryItem(
                        id = "s_mat1",
                        caption = "Studio session 🎸",
                        timestamp = "1h ago"
                    )
                )
            ),
            UserStoryGroup(
                userId = "u_clara",
                userName = "Clara",
                userAvatarEmoji = "🥂",
                isCurrentUser = false,
                hasUnseenStories = true,
                isOnline = true, // Online presence status indicator
                isLive = false,
                stories = listOf(
                    StoryItem(
                        id = "s_cla1",
                        caption = "Rooftop sunset 🌆",
                        timestamp = "3h ago"
                    )
                )
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                StoriesRow(
                    stories = testStories,
                    onStoryClick = {},
                    onAddStoryClick = {}
                )
            }
        }

        // Verify row and avatar items exist
        composeTestRule.onNodeWithTag("stories_row").assertExists()
        composeTestRule.onNodeWithTag("story_item_my_story").assertExists()
        composeTestRule.onNodeWithTag("story_avatar_u_mateo").assertExists()
        composeTestRule.onNodeWithTag("story_avatar_u_clara").assertExists()

        // Verify status indicators exist: LIVE badge on Mateo, Online presence dot on Clara
        composeTestRule.onNodeWithTag("status_indicator_live_u_mateo", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("status_indicator_online_u_clara", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("add_story_button", useUnmergedTree = true).assertExists()

        // Verify labels
        composeTestRule.onNodeWithText("Your Story").assertExists()
        composeTestRule.onNodeWithText("Mateo").assertExists()
        composeTestRule.onNodeWithText("Clara").assertExists()
    }

    @Test
    fun testTapAvatarToViewStoryContent() {
        var clickedGroup: UserStoryGroup? = null

        val mateoStory = UserStoryGroup(
            userId = "u_mateo",
            userName = "Mateo",
            userAvatarEmoji = "⚡",
            isCurrentUser = false,
            hasUnseenStories = true,
            isOnline = true,
            isLive = false,
            stories = listOf(
                StoryItem(
                    id = "s_mat1",
                    caption = "Studio acoustics 🎧",
                    timestamp = "1h ago"
                )
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                StoriesRow(
                    stories = listOf(mateoStory),
                    onStoryClick = { clickedGroup = it },
                    onAddStoryClick = {}
                )
            }
        }

        // Tap Mateo's avatar to view temporary story content
        composeTestRule.onNodeWithTag("story_avatar_u_mateo").performClick()

        assertNotNull(clickedGroup)
        assertEquals("u_mateo", clickedGroup?.userId)
        assertEquals("Mateo", clickedGroup?.userName)
        assertEquals("Studio acoustics 🎧", clickedGroup?.stories?.first()?.caption)
    }

    @Test
    fun testTapAddStoryButtonTriggersCallback() {
        var addStoryTriggered = false

        composeTestRule.setContent {
            MyApplicationTheme {
                StoriesRow(
                    stories = emptyList(),
                    onStoryClick = {},
                    onAddStoryClick = { addStoryTriggered = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("add_story_button", useUnmergedTree = true).performClick()
        assertTrue(addStoryTriggered)
    }

    @Test
    fun testStoryCreatorCaptureFilterAndPost24HourStory() {
        var postedCaption: String? = null
        var postedMediaType: com.example.model.StoryMediaType? = null
        var postedFilter: com.example.model.StoryFilterPreset? = null
        var postedDrawable: Int? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                com.example.ui.stories.StoryCreatorStudio(
                    onPostStory = { caption, _, drawableRes, _, mediaType, filterPreset, _ ->
                        postedCaption = caption
                        postedDrawable = drawableRes
                        postedMediaType = mediaType
                        postedFilter = filterPreset
                    },
                    onDismiss = {}
                )
            }
        }

        // Verify 24h badge, preview canvas, mode switcher, capture button, and filters row exist
        composeTestRule.onNodeWithTag("story_24h_badge").assertExists()
        composeTestRule.onNodeWithTag("story_preview_canvas").assertExists()
        composeTestRule.onNodeWithTag("story_mode_photo").assertExists()
        composeTestRule.onNodeWithTag("story_mode_video").assertExists()
        composeTestRule.onNodeWithTag("story_capture_button").assertExists()
        composeTestRule.onNodeWithTag("story_gallery_picker_button").assertExists()
        composeTestRule.onNodeWithTag("story_filters_row").assertExists()

        // Switch to Video mode and capture/select clip
        composeTestRule.onNodeWithTag("story_mode_video").performClick()
        composeTestRule.onNodeWithTag("story_capture_button").performClick()

        // Apply the Cyberpunk filter and verify filter overlay and intensity slider appear
        composeTestRule.onNodeWithTag("story_filter_Cyberpunk").performClick()
        composeTestRule.onNodeWithTag("story_filter_overlay").assertExists()
        composeTestRule.onNodeWithTag("story_filter_intensity_slider").assertExists()

        // Post as 24-hour story
        composeTestRule.onNodeWithTag("create_story_publish_button").performClick()

        assertEquals(com.example.model.StoryMediaType.VIDEO, postedMediaType)
        assertEquals("Cyberpunk", postedFilter?.name)
        assertNotNull(postedDrawable)
    }
}
