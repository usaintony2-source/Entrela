package com.example.ui.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserStoryGroup
import com.example.ui.EntrelaViewModel
import com.example.ui.components.GradientAvatar
import com.example.ui.theme.*

@Composable
fun StoriesCarousel(
    stories: List<UserStoryGroup>,
    viewModel: EntrelaViewModel,
    modifier: Modifier = Modifier
) {
    StoriesRow(
        stories = stories,
        viewModel = viewModel,
        modifier = modifier
    )
}

@Composable
private fun YourStoryItem(
    hasStories: Boolean,
    avatarEmoji: String,
    onAddClick: () -> Unit,
    onViewClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onViewClick() }
            .testTag("story_item_my_story")
    ) {
        Box(
            modifier = Modifier.size(62.dp),
            contentAlignment = Alignment.Center
        ) {
            GradientAvatar(
                emoji = avatarEmoji,
                size = 58.dp,
                hasStory = hasStories,
                onClick = onViewClick
            )

            // Add (+) Badge
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(EntrelaPrimary, EntrelaSecondary)
                        )
                    )
                    .align(Alignment.BottomEnd)
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

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your Story",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StoryUserItem(
    group: UserStoryGroup,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
            .testTag("story_user_${group.userId}")
    ) {
        GradientAvatar(
            emoji = group.userAvatarEmoji,
            size = 60.dp,
            hasStory = group.hasUnseenStories,
            onClick = onClick
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = group.userName,
            color = if (group.hasUnseenStories) Color.White else EntrelaTextSecondary,
            fontSize = 11.sp,
            fontWeight = if (group.hasUnseenStories) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
