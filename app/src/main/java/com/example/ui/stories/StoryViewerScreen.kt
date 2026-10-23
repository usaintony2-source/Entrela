package com.example.ui.stories

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MessageMediaType
import com.example.model.UserStoryGroup
import com.example.ui.EntrelaViewModel
import com.example.ui.components.GradientAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.isActive

@Composable
fun StoryViewerScreen(
    group: UserStoryGroup,
    viewModel: EntrelaViewModel,
    onClose: () -> Unit
) {
    var currentStoryIndex by remember { mutableStateOf(0) }
    var replyText by remember { mutableStateOf("") }
    val stories = group.stories
    val totalStories = stories.size

    val activeStory = stories.getOrNull(currentStoryIndex) ?: stories.first()
    val progress = remember(currentStoryIndex) { Animatable(0f) }
    var isPaused by remember { mutableStateOf(false) }

    // Auto progress timer
    LaunchedEffect(currentStoryIndex, isPaused) {
        if (!isPaused) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = ((1f - progress.value) * 4500).toInt(),
                    easing = LinearEasing
                )
            )
            if (isActive && progress.value >= 1f) {
                if (currentStoryIndex < totalStories - 1) {
                    currentStoryIndex++
                } else {
                    onClose()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(currentStoryIndex) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        if (offset.x < size.width * 0.35f) {
                            if (currentStoryIndex > 0) {
                                currentStoryIndex--
                            }
                        } else {
                            if (currentStoryIndex < totalStories - 1) {
                                currentStoryIndex++
                            } else {
                                onClose()
                            }
                        }
                    }
                )
            }
            .testTag("story_viewer_container")
    ) {
        // Story Visual Background
        if (activeStory.drawableRes != null) {
            Image(
                painter = painterResource(id = activeStory.drawableRes),
                contentDescription = activeStory.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (activeStory.filterOverlayHex != 0x00000000L) {
                val filterColor = Color(activeStory.filterOverlayHex).let {
                    it.copy(alpha = (it.alpha * activeStory.filterIntensity).coerceIn(0f, 0.7f))
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(filterColor)
                        .testTag("story_viewer_filter_overlay")
                )
            }
            // Soft overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )
        } else {
            val colors = activeStory.gradientColors.map { Color(it) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = if (colors.size >= 2) colors else listOf(EntrelaPrimary, EntrelaSecondary)
                        )
                    )
            )
        }

        // Story Caption Center
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = activeStory.caption,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
        }

        // Top Controls: Segmented Progress Bars & Creator Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Segmented Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0 until totalStories) {
                    val segProgress = when {
                        i < currentStoryIndex -> 1f
                        i == currentStoryIndex -> progress.value
                        else -> 0f
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.35f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(segProgress)
                                .background(Color.White)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Creator Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GradientAvatar(
                    emoji = group.userAvatarEmoji,
                    size = 38.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.userName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${activeStory.timestamp} • ${activeStory.expiresInText} • ${activeStory.filterName}",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("story_close_button")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Reaction & Reply Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Quick Emoji Reactions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("🔥", "❤️", "😂", "😮", "👏", "🥂").forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .clickable {
                                // Find or open conversation with creator and send emoji reaction
                                val conv = viewModel.conversations.value.find { it.otherUserId == group.userId }
                                if (conv != null) {
                                    viewModel.sendMessage(conv.id, emoji)
                                }
                                viewModel.showToast("Sent $emoji to ${group.userName}")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }
            }

            // Message Reply Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Reply to ${group.userName}...", color = Color.White.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(alpha = 0.6f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("story_reply_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            val conv = viewModel.conversations.value.find { it.otherUserId == group.userId }
                            if (conv != null) {
                                viewModel.sendMessage(conv.id, replyText.trim())
                            }
                            viewModel.showToast("Message sent to ${group.userName}")
                            replyText = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(EntrelaPrimary)
                        .testTag("story_reply_send_button")
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
