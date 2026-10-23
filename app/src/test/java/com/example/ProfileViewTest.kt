package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.model.RelationshipStatus
import com.example.model.UserProfile
import com.example.ui.profile.ProfileView
import com.example.ui.profile.ProfileViewTab
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
class ProfileViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testProfileViewDisplaysUserInfo() {
        val testUser = UserProfile(
            id = "user_test_123",
            name = "Elena Rostova",
            username = "elena_r",
            email = "elena@entrela.app",
            bio = "Visual artist & filmmaker based in Berlin. Creating stories through motion and color ✨",
            avatarEmoji = "🎨",
            relationshipStatus = RelationshipStatus.OPEN_TO_CONNECT,
            followersCount = 4850,
            followingCount = 620,
            interests = listOf("Art 🎨", "Cinema 🎬", "Travel ✈️")
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileView(
                    user = testUser,
                    reels = emptyList(),
                    isCurrentUser = true
                )
            }
        }

        // 1. Verify User Information
        // Name & Username
        composeTestRule.onNodeWithTag("profile_name").assertExists()
        composeTestRule.onNodeWithText("Elena Rostova").assertExists()
        composeTestRule.onNodeWithTag("profile_username").assertExists()
        composeTestRule.onNodeWithText("@elena_r").assertExists()

        // Avatar
        composeTestRule.onNodeWithTag("profile_avatar").assertExists()

        // Bio
        composeTestRule.onNodeWithTag("profile_bio").assertExists()
        composeTestRule.onNodeWithText(
            "Visual artist & filmmaker based in Berlin. Creating stories through motion and color ✨"
        ).assertExists()

        // Counts of followers, following, and reels
        composeTestRule.onNodeWithTag("profile_stat_followers").assertExists()
        composeTestRule.onNodeWithText("4.9k").assertExists()

        composeTestRule.onNodeWithTag("profile_stat_following").assertExists()
        composeTestRule.onNodeWithText("620").assertExists()

        composeTestRule.onNodeWithTag("profile_stat_reels").assertExists()
        composeTestRule.onNodeWithText("0").assertExists()
    }

    @Test
    fun testProfileViewFirebaseAuthAccountManagement() {
        val testUser = UserProfile(
            id = "user_auth_777",
            name = "Lucas Vance",
            username = "lucas_v",
            email = "lucas@entrela.app",
            bio = "Exploring sound and cinema",
            avatarEmoji = "⚡",
            relationshipStatus = RelationshipStatus.SINGLE,
            followersCount = 1200,
            followingCount = 300
        )

        var logoutTriggered = false
        var resetPasswordRequestedEmail: String? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileView(
                    user = testUser,
                    initialTab = ProfileViewTab.ACCOUNT,
                    onLogout = { logoutTriggered = true },
                    onPasswordResetRequested = { email -> resetPasswordRequestedEmail = email }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Verify Firebase Auth account management card is visible
        composeTestRule.onNodeWithTag("profile_firebase_card", useUnmergedTree = true).assertExists()

        // Verify Authenticated Email is shown
        composeTestRule.onNodeWithTag("profile_firebase_email", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("lucas@entrela.app").assertExists()

        // Verify Password Reset action button exists and can be clicked
        composeTestRule.onNodeWithTag("profile_reset_password_button", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("profile_reset_password_button", useUnmergedTree = true).performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Verify Confirmation dialog for sending reset email appears
        composeTestRule.onNodeWithTag("confirm_send_reset_email_button", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("confirm_send_reset_email_button", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        // Check password reset callback was invoked with user's email
        assertEquals("lucas@entrela.app", resetPasswordRequestedEmail)

        // Verify Log out button exists and opens confirmation dialog
        composeTestRule.onNodeWithTag("profile_logout_button", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("profile_logout_button", useUnmergedTree = true).performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("confirm_logout_button", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("confirm_logout_button", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        assertTrue("Logout should have been triggered", logoutTriggered)
    }
}
