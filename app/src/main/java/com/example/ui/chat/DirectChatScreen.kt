package com.example.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.activity.compose.BackHandler
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.MessageMediaType
import com.example.ui.EntrelaViewModel
import com.example.ui.components.GradientAvatar
import com.example.ui.theme.*

@Composable
fun DirectChatScreen(
    conversationId: String,
    viewModel: EntrelaViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val conversations by viewModel.conversations.collectAsState()
    val conv = conversations.find { it.id == conversationId }
    var showPhotoPicker by remember { mutableStateOf(false) }

    if (conv == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Conversation not found", color = Color.White)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EntrelaDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Chat Header
            Surface(
                color = EntrelaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    GradientAvatar(
                        emoji = conv.otherUserAvatarEmoji,
                        size = 42.dp,
                        isOnline = conv.isOnline
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = conv.otherUserName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = conv.relationshipStatus,
                                color = EntrelaPrimaryVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = if (conv.isOnline) "Active now" else conv.lastSeenText,
                            color = if (conv.isOnline) EntrelaOnlineGreen else EntrelaTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    // Call buttons
                    IconButton(
                        onClick = { viewModel.showToast("Voice calling ${conv.otherUserName}...") }
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = EntrelaTextSecondary)
                    }
                    IconButton(
                        onClick = { viewModel.showToast("Starting video call with ${conv.otherUserName}...") }
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = EntrelaPrimary)
                    }
                }
            }

            // Core Chat Interface (Message bubbles stream, quick sparks & input bar)
            ChatInterface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                messages = conv.messages,
                onSendMessage = { text ->
                    viewModel.sendMessage(conv.id, text)
                },
                recipientName = conv.otherUserName,
                recipientAvatarEmoji = conv.otherUserAvatarEmoji,
                isRecipientOnline = conv.isOnline,
                placeholderText = "Type a message...",
                onAttachMediaClick = { showPhotoPicker = !showPhotoPicker },
                onSendVoiceNoteClick = {
                    viewModel.sendMessage(
                        conversationId = conv.id,
                        text = "🎙️ Voice note (0:14)",
                        mediaType = MessageMediaType.AUDIO
                    )
                },
                isPhotoPickerOpen = showPhotoPicker,
                attachmentContent = if (showPhotoPicker) {
                    {
                        Surface(
                            color = EntrelaDarkSurfaceVariant,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Share Photo Attachment",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    listOf(
                                        Pair(R.drawable.reel_bg_couple_1792260319176, "Night City"),
                                        Pair(R.drawable.reel_bg_party_1792260318860, "Rooftop Party"),
                                        Pair(R.drawable.reel_bg_travel_1792260334284, "Bali Sunset")
                                    ).forEach { (res, label) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    viewModel.sendMessage(
                                                        conversationId = conv.id,
                                                        text = "Shared photo: $label",
                                                        mediaType = MessageMediaType.IMAGE,
                                                        mediaRes = res
                                                    )
                                                    showPhotoPicker = false
                                                }
                                        ) {
                                            Image(
                                                painter = painterResource(id = res),
                                                contentDescription = label,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            )
                                            Text(text = label, color = EntrelaTextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else null
            )
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    ChatMessageBubble(msg = msg)
}
