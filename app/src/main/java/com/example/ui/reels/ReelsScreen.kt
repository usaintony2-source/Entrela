package com.example.ui.reels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ReelItem
import com.example.ui.EntrelaViewModel
import com.example.ui.Screen
import com.example.ui.components.EntrelaLogo
import com.example.ui.components.GradientAvatar
import com.example.ui.stories.StoriesRow
import com.example.ui.theme.*

@Composable
fun ReelsScreen(viewModel: EntrelaViewModel) {
    val stories by viewModel.stories.collectAsState()

    VerticalReelsFeed(
        viewModel = viewModel,
        modifier = Modifier.fillMaxSize(),
        headerContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EntrelaLogo(sizeSp = 22)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.startLiveStream() },
                            modifier = Modifier.testTag("top_go_live_btn")
                        ) {
                            Icon(Icons.Default.Podcasts, contentDescription = "Go Live", tint = EntrelaHeartRed)
                        }
                        IconButton(
                            onClick = { viewModel.openVideoRecorder() },
                            modifier = Modifier.testTag("top_record_video_btn")
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "Record Reel", tint = Color.White)
                        }
                        IconButton(
                            onClick = { viewModel.setCreateStoryOpen(true) },
                            modifier = Modifier.testTag("top_add_story_btn")
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Story", tint = Color.White)
                        }
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.CHATS) },
                            modifier = Modifier.testTag("top_open_chats_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = EntrelaPrimary) {
                                        Text("2", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Direct Chats", tint = Color.White)
                            }
                        }
                    }
                }

                // Status / Stories horizontal row at top of home screen
                StoriesRow(
                    stories = stories,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
private fun ReelPageItem(
    reel: ReelItem,
    viewModel: EntrelaViewModel,
    storiesRow: @Composable () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var showPlayPauseIcon by remember { mutableStateOf(false) }
    var showHeartAnimation by remember { mutableStateOf(false) }

    // Vinyl rotation & playback progress animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val autoProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "playbackProgress"
    )

    // Equalizer wave animation
    val eq1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq1"
    )
    val eq2 by infiniteTransition.animateFloat(
        initialValue = 14f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq2"
    )
    val eq3 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq3"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(reel.id) {
                detectTapGestures(
                    onTap = {
                        isPlaying = !isPlaying
                        showPlayPauseIcon = true
                    },
                    onDoubleTap = {
                        showHeartAnimation = !showHeartAnimation
                        viewModel.toggleLikeReel(reel.id)
                    }
                )
            }
            .testTag("reel_page_${reel.id}")
    ) {
        // Reel Video / Background Layer
        if (reel.drawableRes != null) {
            Image(
                painter = painterResource(id = reel.drawableRes),
                contentDescription = reel.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant gradient if no drawable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1F1C2C), Color(0xFF928DAB))
                        )
                    )
            )
        }

        // Dark Gradients for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.75f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Play / Pause animated badge
        LaunchedEffect(showPlayPauseIcon) {
            if (showPlayPauseIcon) {
                kotlinx.coroutines.delay(800)
                showPlayPauseIcon = false
            }
        }
        AnimatedVisibility(
            visible = showPlayPauseIcon,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPlaying) "Playing" else "Paused",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Double-tap Big Heart Pop Animation
        AnimatedVisibility(
            visible = showHeartAnimation,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .testTag("heart_pop_${reel.id}")
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Liked",
                tint = EntrelaHeartRed,
                modifier = Modifier
                    .size(96.dp)
                    .testTag("double_tap_heart_icon_${reel.id}")
            )
        }

        // Top Header: Logo, Direct Chat action, and Status Stories Carousel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EntrelaLogo(sizeSp = 22)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.startLiveStream() },
                        modifier = Modifier.testTag("top_go_live_btn")
                    ) {
                        Icon(Icons.Default.Podcasts, contentDescription = "Go Live", tint = EntrelaHeartRed)
                    }
                    IconButton(
                        onClick = { viewModel.openVideoRecorder() },
                        modifier = Modifier.testTag("top_record_video_btn")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Record Reel", tint = Color.White)
                    }
                    IconButton(
                        onClick = { viewModel.setCreateStoryOpen(true) },
                        modifier = Modifier.testTag("top_add_story_btn")
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Story", tint = Color.White)
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.CHATS) },
                        modifier = Modifier.testTag("top_open_chats_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = EntrelaPrimary) {
                                    Text("2", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Direct Chats", tint = Color.White)
                        }
                    }
                }
            }

            // Status / Stories horizontal row at top of home screen
            storiesRow()
        }

        // Right Side Floating Interaction Column
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Creator Avatar with follow badge
            Box(contentAlignment = Alignment.Center) {
                GradientAvatar(
                    emoji = reel.creatorAvatarEmoji,
                    size = 48.dp
                )
                if (!reel.isFollowingCreator) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(EntrelaPrimary)
                            .align(Alignment.BottomCenter)
                            .offset(y = 6.dp)
                            .clickable { viewModel.toggleFollowCreator(reel.creatorId) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Follow", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Like Action
            ReelActionItem(
                icon = if (reel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = "${reel.likesCount}",
                tint = if (reel.isLiked) EntrelaHeartRed else Color.White,
                onClick = { viewModel.toggleLikeReel(reel.id) },
                testTag = "reel_like_${reel.id}"
            )

            // Comments Action
            ReelActionItem(
                icon = Icons.Default.ChatBubbleOutline,
                label = "${reel.commentsCount}",
                tint = Color.White,
                onClick = { viewModel.openComments(reel.id) },
                testTag = "reel_comments_${reel.id}"
            )

            // Share Action
            ReelActionItem(
                icon = Icons.Default.Share,
                label = "${reel.sharesCount}",
                tint = Color.White,
                onClick = { viewModel.openShare(reel) },
                testTag = "reel_share_${reel.id}"
            )

            // Bookmark Action
            ReelActionItem(
                icon = if (reel.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                label = if (reel.isBookmarked) "Saved" else "Save",
                tint = if (reel.isBookmarked) EntrelaGold else Color.White,
                onClick = { viewModel.toggleBookmarkReel(reel.id) },
                testTag = "reel_bookmark_${reel.id}"
            )

            // Rotating Vinyl Disc
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    .rotate(if (isPlaying) rotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(EntrelaPrimary)
                )
            }
        }

        // Bottom Left Info Overlay: Creator, Caption, Audio Track
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, bottom = 24.dp)
        ) {
            // Creator handle & follow badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "@${reel.creatorUsername}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (reel.isFollowingCreator) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Following",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Caption
            Text(
                text = reel.caption,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Hashtags
            Text(
                text = reel.tags.joinToString(" "),
                color = EntrelaTertiary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Track details bar with live equalizer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                // Live Soundwave Bars
                Row(
                    modifier = Modifier.height(18.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(modifier = Modifier.width(3.dp).height(if (isPlaying) eq1.dp else 4.dp).background(EntrelaPrimary))
                    Box(modifier = Modifier.width(3.dp).height(if (isPlaying) eq2.dp else 8.dp).background(EntrelaSecondary))
                    Box(modifier = Modifier.width(3.dp).height(if (isPlaying) eq3.dp else 6.dp).background(EntrelaPrimaryVariant))
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${reel.audioTrack} • ${reel.artistName}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Visual Playback Progress Indicator at the bottom of the reel
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.5.dp)
                .background(Color.White.copy(alpha = 0.25f))
                .testTag("reel_progress_indicator_${reel.id}")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = autoProgress.coerceIn(0f, 1f))
                    .background(
                        Brush.horizontalGradient(
                            listOf(EntrelaPrimary, EntrelaSecondary, EntrelaPrimaryVariant)
                        )
                    )
                    .testTag("reel_progress_bar_${reel.id}")
            )
        }
    }
}

@Composable
private fun ReelActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
