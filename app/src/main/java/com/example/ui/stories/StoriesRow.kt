package com.example.ui.stories

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserStoryGroup
import com.example.ui.EntrelaViewModel
import com.example.ui.theme.*

/**
 * StoriesRow component displayed at the top of the home screen.
 * Shows circular user avatars with status indicators (Unseen Story ring,
 * Online status indicator dot, Live streaming badge, and Add Story badge),
 * allowing users to tap to view temporary story content.
 */
@Composable
fun StoriesRow(
    stories: List<UserStoryGroup>,
    onStoryClick: (UserStoryGroup) -> Unit,
    onAddStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stories_row")
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // "Your Story" always comes first
        val myStoryGroup = stories.firstOrNull { it.isCurrentUser }
        item(key = "my_story_item") {
            YourStoryAvatarItem(
                myStoryGroup = myStoryGroup,
                onAddClick = onAddStoryClick,
                onViewClick = {
                    if (myStoryGroup != null && myStoryGroup.stories.isNotEmpty()) {
                        onStoryClick(myStoryGroup)
                    } else {
                        onAddStoryClick()
                    }
                }
            )
        }

        // Other creators and friends' stories
        items(
            items = stories.filter { !it.isCurrentUser },
            key = { it.userId }
        ) { group ->
            StoryAvatarItem(
                group = group,
                onClick = { onStoryClick(group) }
            )
        }
    }
}

/**
 * Overloaded convenience constructor integrating directly with [EntrelaViewModel].
 */
@Composable
fun StoriesRow(
    stories: List<UserStoryGroup>,
    viewModel: EntrelaViewModel,
    modifier: Modifier = Modifier
) {
    StoriesRow(
        stories = stories,
        onStoryClick = { group -> viewModel.openStoryViewer(group) },
        onAddStoryClick = { viewModel.setCreateStoryOpen(true) },
        modifier = modifier
    )
}

/**
 * Circular avatar for "Your Story" with status ring and "+" create action badge.
 */
@Composable
private fun YourStoryAvatarItem(
    myStoryGroup: UserStoryGroup?,
    onAddClick: () -> Unit,
    onViewClick: () -> Unit
) {
    val hasStories = myStoryGroup?.stories?.isNotEmpty() == true
    val avatarEmoji = myStoryGroup?.userAvatarEmoji ?: "💫"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onViewClick() }
            .testTag("story_item_my_story")
    ) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Story Ring (if active story exists)
            val ringBrush = if (hasStories) {
                Brush.sweepGradient(
                    listOf(
                        EntrelaPrimary,
                        EntrelaSecondary,
                        EntrelaTertiary,
                        EntrelaPrimary
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.15f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(ringBrush)
                    .padding(2.5.dp) // Ring thickness
                    .clip(CircleShape)
                    .background(EntrelaDarkBg) // Gap between ring and avatar
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF2C1654), Color(0xFF140D26))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = avatarEmoji,
                        fontSize = 24.sp
                    )
                }
            }

            // Add (+) Badge button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(EntrelaDarkBg)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(EntrelaPrimary, EntrelaSecondary)
                        )
                    )
                    .clickable { onAddClick() }
                    .testTag("add_story_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Story",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "Your Story",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Circular user avatar item showing status indicators:
 * - Unseen story gradient ring vs seen story muted ring
 * - "LIVE" status indicator badge when user is live
 * - Glowing green status indicator dot when user is online
 * - Tap interaction to view temporary story content
 */
@Composable
private fun StoryAvatarItem(
    group: UserStoryGroup,
    onClick: () -> Unit
) {
    val hasUnseen = group.hasUnseenStories
    val isLive = group.isLive
    val isOnline = group.isOnline

    // Pulsing animation for Live status indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_live_${group.userId}")
    val livePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_pulse_scale"
    )

    // Sweep rotation for vibrant story ring
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
            .testTag("story_avatar_${group.userId}")
    ) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Story Ring Border
            val borderBrush = when {
                isLive -> Brush.sweepGradient(
                    listOf(
                        EntrelaHeartRed,
                        EntrelaSecondary,
                        Color(0xFFFF5252),
                        EntrelaHeartRed
                    )
                )
                hasUnseen -> Brush.sweepGradient(
                    listOf(
                        EntrelaPrimary,
                        EntrelaSecondary,
                        EntrelaTertiary,
                        EntrelaPrimary
                    )
                )
                else -> Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.18f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(borderBrush)
                    .padding(2.5.dp) // Ring thickness
                    .clip(CircleShape)
                    .background(EntrelaDarkBg) // Gap separating ring from inner avatar
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar image or emoji
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF281E48), Color(0xFF130F24))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (group.avatarDrawableRes != null) {
                        Image(
                            painter = painterResource(id = group.avatarDrawableRes),
                            contentDescription = "${group.userName} avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = group.userAvatarEmoji,
                            fontSize = 24.sp
                        )
                    }
                }
            }

            // Status Indicator #1: LIVE Badge
            if (isLive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .scale(livePulseScale)
                        .shadow(4.dp, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(EntrelaHeartRed, EntrelaSecondary)
                            )
                        )
                        .padding(horizontal = 6.dp, vertical = 1.5.dp)
                        .testTag("status_indicator_live_${group.userId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            } else if (isOnline) {
                // Status Indicator #2: Online Green Presence Dot
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(EntrelaDarkBg)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(EntrelaOnlineGreen) // Emerald Green Online Status
                        .testTag("status_indicator_online_${group.userId}")
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = group.userName,
            color = if (hasUnseen || isLive) Color.White else EntrelaTextSecondary,
            fontSize = 11.sp,
            fontWeight = if (hasUnseen || isLive) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("story_username_${group.userId}")
        )
    }
}
