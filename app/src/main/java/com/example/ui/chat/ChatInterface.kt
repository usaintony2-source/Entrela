package com.example.ui.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.MessageMediaType
import com.example.ui.components.GradientAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reusable ChatInterface component that allows users to send and receive text messages.
 * Implements a message bubbles layout with incoming/outgoing bubbles, an input text field,
 * quick spark prompts, and a responsive send button.
 */
@Composable
fun ChatInterface(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>? = null,
    onSendMessage: ((String) -> Unit)? = null,
    onReceiveMessage: ((ChatMessage) -> Unit)? = null,
    placeholderText: String = "Type a message...",
    recipientName: String = "Elena Rostova",
    recipientAvatarEmoji: String = "✨",
    isRecipientOnline: Boolean = true,
    quickSuggestions: List<String> = listOf("Hey there! 👋", "Loved your reel! 🔥", "Coffee sometime? ☕", "Spark sent ✨"),
    showQuickSuggestions: Boolean = true,
    simulateReplies: Boolean = (onSendMessage == null),
    onAttachMediaClick: (() -> Unit)? = null,
    onSendVoiceNoteClick: (() -> Unit)? = null,
    isPhotoPickerOpen: Boolean = false,
    attachmentContent: (@Composable () -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Internal message state for standalone usage
    val internalMessages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "m_init_1",
                senderId = "u_partner",
                text = "Hey! I saw your latest Entrela reel, the vibes were incredible! ✨",
                timestamp = "10:14 AM",
                isMine = false
            ),
            ChatMessage(
                id = "m_init_2",
                senderId = "u_me",
                text = "Thanks so much! Glad you liked the audio sync and colors 🌸",
                timestamp = "10:15 AM",
                isMine = true
            )
        )
    }

    // Effective message stream (external controlled or internal state)
    val effectiveMessages = messages ?: internalMessages

    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    // Auto-scroll when new messages arrive
    LaunchedEffect(effectiveMessages.size, isTyping) {
        if (effectiveMessages.isNotEmpty()) {
            listState.animateScrollToItem(effectiveMessages.size - 1)
        }
    }

    // Helper to dispatch a message
    fun dispatchMessage(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isEmpty()) return

        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        if (onSendMessage != null) {
            onSendMessage(trimmed)
            if (simulateReplies) {
                coroutineScope.launch {
                    isTyping = true
                    delay(1200)
                    isTyping = false

                    val replies = listOf(
                        "That sounds amazing! Let's definitely connect soon ✨",
                        "Haha completely agree! Loved the vibe 💖",
                        "Yes! Are you going to the Shibuya music festival this weekend? 🎶",
                        "Totally! Sending you a spark right now ⚡",
                        "Awesome! Always love catching your updates on Entrela!"
                    )
                    val replyMessage = ChatMessage(
                        id = "m_reply_${System.currentTimeMillis()}",
                        senderId = "u_partner",
                        text = replies.random(),
                        timestamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
                        isMine = false
                    )
                    onReceiveMessage?.invoke(replyMessage)
                }
            }
        } else {
            // Standalone mode: Add to internal message stream
            val myMessage = ChatMessage(
                id = "m_${System.currentTimeMillis()}",
                senderId = "u_me",
                text = trimmed,
                timestamp = currentTime,
                isMine = true
            )
            internalMessages.add(myMessage)

            // Simulate incoming reply for interactive send & receive demonstration
            coroutineScope.launch {
                isTyping = true
                delay(1200)
                isTyping = false

                val replies = listOf(
                    "That sounds amazing! Let's definitely connect soon ✨",
                    "Haha completely agree! Loved the vibe 💖",
                    "Yes! Are you going to the Shibuya music festival this weekend? 🎶",
                    "Totally! Sending you a spark right now ⚡",
                    "Awesome! Always love catching your updates on Entrela!"
                )
                val replyText = replies.random()
                val incomingTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                val replyMessage = ChatMessage(
                    id = "m_reply_${System.currentTimeMillis()}",
                    senderId = "u_partner",
                    text = replyText,
                    timestamp = incomingTime,
                    isMine = false
                )
                internalMessages.add(replyMessage)
                onReceiveMessage?.invoke(replyMessage)
            }
        }
        inputText = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EntrelaDarkBg)
    ) {
        // Message Bubbles Stream
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (effectiveMessages.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .testTag("chat_empty_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = EntrelaDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "💬", fontSize = 32.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Messages Yet",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Say hello to $recipientName or tap a prompt below to spark the conversation!",
                            color = EntrelaTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("chat_messages_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(effectiveMessages, key = { it.id }) { msg ->
                        ChatMessageBubble(
                            msg = msg,
                            recipientAvatarEmoji = recipientAvatarEmoji
                        )
                    }

                    // Typing indicator bubble
                    if (isTyping) {
                        item(key = "typing_indicator") {
                            TypingIndicatorBubble(avatarEmoji = recipientAvatarEmoji)
                        }
                    }
                }
            }
        }

        // Quick Suggestion Chips
        if (showQuickSuggestions && quickSuggestions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EntrelaDarkSurface.copy(alpha = 0.6f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickSuggestions.forEach { suggestion ->
                    Surface(
                        onClick = { dispatchMessage(suggestion) },
                        shape = RoundedCornerShape(20.dp),
                        color = EntrelaDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("chat_quick_suggestion_${suggestion.take(8)}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = suggestion,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Attachment drawer content if expanded
        if (attachmentContent != null) {
            attachmentContent()
        }

        // Bottom Input Text Field & Send Button
        Surface(
            color = EntrelaDarkSurface,
            border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = EntrelaDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media attachment button if handler provided
                if (onAttachMediaClick != null) {
                    IconButton(
                        onClick = onAttachMediaClick,
                        modifier = Modifier.testTag("chat_photo_attach_button")
                    ) {
                        Icon(
                            imageVector = if (isPhotoPickerOpen) Icons.Default.Close else Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach Media",
                            tint = EntrelaPrimaryVariant
                        )
                    }
                }

                // Voice note button if handler provided
                if (onSendVoiceNoteClick != null) {
                    IconButton(
                        onClick = onSendVoiceNoteClick,
                        modifier = Modifier.testTag("chat_voice_note_button")
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice note",
                            tint = EntrelaSecondaryVariant
                        )
                    }
                }

                // Input Text Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = placeholderText,
                            color = EntrelaTextMuted,
                            fontSize = 14.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EntrelaPrimary,
                        unfocusedBorderColor = EntrelaDarkBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = EntrelaDarkSurfaceVariant,
                        unfocusedContainerColor = EntrelaDarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                focusManager.clearFocus()
                                dispatchMessage(inputText)
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button with animated highlight
                val hasText = inputText.isNotBlank()
                val sendButtonColor by animateColorAsState(
                    targetValue = if (hasText) EntrelaPrimary else EntrelaPrimary.copy(alpha = 0.5f),
                    label = "sendButtonColor"
                )

                IconButton(
                    onClick = {
                        if (hasText) {
                            dispatchMessage(inputText)
                        }
                    },
                    enabled = hasText,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(sendButtonColor)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Message Bubble layout supporting text, optional image, audio waveform, and delivery status.
 */
@Composable
fun ChatMessageBubble(
    msg: ChatMessage,
    recipientAvatarEmoji: String = "✨",
    modifier: Modifier = Modifier
) {
    val isMine = msg.isMine

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chat_message_bubble_${msg.id}")
            .testTag(if (isMine) "chat_bubble_outgoing" else "chat_bubble_incoming"),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Incoming message avatar
            if (!isMine) {
                GradientAvatar(
                    emoji = recipientAvatarEmoji,
                    size = 28.dp,
                    isOnline = false
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMine) 18.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 18.dp
                ),
                color = if (isMine) Color.Transparent else EntrelaDarkSurfaceVariant,
                border = if (!isMine) androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder) else null,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Box(
                    modifier = if (isMine) {
                        Modifier.background(
                            Brush.horizontalGradient(
                                listOf(EntrelaPrimary, EntrelaSecondary)
                            )
                        )
                    } else Modifier
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        // Image attachment if present
                        if (msg.mediaType == MessageMediaType.IMAGE && msg.mediaAttachmentRes != null) {
                            Image(
                                painter = painterResource(id = msg.mediaAttachmentRes),
                                contentDescription = "Shared photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .padding(bottom = 6.dp)
                            )
                        }

                        // Audio waveform simulation if audio message
                        if (msg.mediaType == MessageMediaType.AUDIO) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play audio",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.height(18.dp)
                                ) {
                                    listOf(8, 16, 12, 20, 10, 14, 22, 12, 6, 16, 10).forEach { h ->
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(h.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color.White.copy(alpha = 0.85f))
                                        )
                                    }
                                }
                            }
                        }

                        // Message text
                        if (msg.text.isNotBlank()) {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                                modifier = Modifier.testTag("chat_message_text_${msg.id}")
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Timestamp and status indicator
                        Row(
                            modifier = Modifier.align(Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg.timestamp,
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 10.sp
                            )
                            if (isMine) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Delivered",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated bubble representing partner typing status.
 */
@Composable
private fun TypingIndicatorBubble(avatarEmoji: String) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_typing_indicator")
    ) {
        GradientAvatar(emoji = avatarEmoji, size = 28.dp, isOnline = true)
        Spacer(modifier = Modifier.width(6.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = EntrelaDarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "typing",
                    color = EntrelaTextSecondary,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Text(
                    text = "•••",
                    color = EntrelaPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
