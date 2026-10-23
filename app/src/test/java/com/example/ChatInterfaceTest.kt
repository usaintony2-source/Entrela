package com.example

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.model.ChatMessage
import com.example.ui.chat.ChatInterface
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChatInterfaceTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testChatInterfaceRendersMessages() {
        val testMessages = listOf(
            ChatMessage(
                id = "m1",
                senderId = "u_partner",
                text = "Hello from Tokyo! 🗼",
                timestamp = "11:00 AM",
                isMine = false
            ),
            ChatMessage(
                id = "m2",
                senderId = "u_me",
                text = "Hey there, beautiful weather today! ☀️",
                timestamp = "11:01 AM",
                isMine = true
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ChatInterface(
                    messages = testMessages,
                    onSendMessage = {}
                )
            }
        }

        // Verify message stream list and bubbles
        composeTestRule.onNodeWithTag("chat_messages_list").assertExists()
        composeTestRule.onNodeWithText("Hello from Tokyo! 🗼").assertExists()
        composeTestRule.onNodeWithText("Hey there, beautiful weather today! ☀️").assertExists()
        composeTestRule.onNodeWithTag("chat_message_bubble_m1").assertExists()
        composeTestRule.onNodeWithTag("chat_message_bubble_m2").assertExists()

        // Verify input field and send button
        composeTestRule.onNodeWithTag("chat_message_input").assertExists()
        composeTestRule.onNodeWithTag("chat_send_button").assertExists()
    }

    @Test
    fun testSendMessageCallbackAndInputClearing() {
        var sentMessage: String? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                ChatInterface(
                    messages = emptyList(),
                    onSendMessage = { sentMessage = it }
                )
            }
        }

        // Input text and verify send button can be clicked
        composeTestRule.onNodeWithTag("chat_message_input").performTextInput("Can't wait to connect! ✨")
        composeTestRule.onNodeWithTag("chat_send_button").performClick()

        // Verify sent text matches
        assertEquals("Can't wait to connect! ✨", sentMessage)

        // Verify input field is cleared
        composeTestRule.onNodeWithTag("chat_message_input").assertTextEquals("", "Type a message...")
    }

    @Test
    fun testEmptyInputDoesNotTriggerSend() {
        var sentMessage: String? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                ChatInterface(
                    messages = emptyList(),
                    onSendMessage = { sentMessage = it }
                )
            }
        }

        // Attempt clicking send button when text is empty
        composeTestRule.onNodeWithTag("chat_send_button").performClick()
        assertNull(sentMessage)

        // Attempt clicking send button when text is only blank spaces
        composeTestRule.onNodeWithTag("chat_message_input").performTextInput("   ")
        composeTestRule.onNodeWithTag("chat_send_button").performClick()
        assertNull(sentMessage)
    }

    @Test
    fun testReceiveMessageDynamicList() {
        val messagesList = mutableStateListOf(
            ChatMessage(
                id = "msg_1",
                senderId = "u_me",
                text = "Hey! How is your day going?",
                timestamp = "12:00 PM",
                isMine = true
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ChatInterface(
                    messages = messagesList,
                    onSendMessage = {}
                )
            }
        }

        // Check initial message
        composeTestRule.onNodeWithText("Hey! How is your day going?").assertExists()

        // Simulate receiving a new message dynamically
        messagesList.add(
            ChatMessage(
                id = "msg_2",
                senderId = "u_partner",
                text = "It's going great! Just finished editing my new reel 🎬",
                timestamp = "12:02 PM",
                isMine = false
            )
        )
        composeTestRule.waitForIdle()

        // Verify newly received message is displayed in the list
        composeTestRule.onNodeWithText("It's going great! Just finished editing my new reel 🎬").assertExists()
        composeTestRule.onNodeWithTag("chat_message_bubble_msg_2").assertExists()
    }

    @Test
    fun testEmptyStateWhenNoMessages() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ChatInterface(
                    messages = emptyList(),
                    recipientName = "Elena",
                    onSendMessage = {}
                )
            }
        }

        // Empty state is rendered
        composeTestRule.onNodeWithTag("chat_empty_state").assertExists()
        composeTestRule.onNodeWithText("No Messages Yet").assertExists()
    }

    @Test
    fun testQuickSuggestionTriggersSend() {
        var dispatchedText: String? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                ChatInterface(
                    messages = emptyList(),
                    quickSuggestions = listOf("Spark sent ✨"),
                    onSendMessage = { dispatchedText = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("chat_quick_suggestion_Spark se").performClick()
        assertEquals("Spark sent ✨", dispatchedText)
    }
}
