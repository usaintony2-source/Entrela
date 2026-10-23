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
import androidx.compose.ui.draw.scale
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
import com.example.model.ReelComment
import com.example.model.ReelItem
import com.example.ui.EntrelaViewModel
import com.example.ui.components.GradientAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Vertical scrolling feed component that fetches and displays short video reels
 * with like and comment interactions using Firestore as the backend.
 */
@Composable
fun VerticalReelsFeed(
    viewModel: EntrelaViewModel,
    modifier: Modifier = Modifier,
    headerContent: (@Composable () -> Unit)? = null
) {
    val reels by viewModel.reels.collectAsState()

    VerticalReelsFeed(
        reels = reels,
        onLikeReel = { reelId -> viewModel.toggleLikeReel(reelId) },
        onCommentReel = { reelId -> viewModel.openComments(reelId) },
        onShareReel = { reel -> viewModel.openShare(reel) },
        onBookmarkReel = { reelId -> viewModel.toggleBookmarkReel(reelId) },
        onToggleFollowCreator = { creatorId -> viewModel.toggleFollowCreator(creatorId) },
        modifier = modifier,
        headerContent = headerContent
    )
}

/**
 * Modular and reusable VerticalReelsFeed supporting direct parameters and callbacks.
 */
@Composable
fun VerticalReelsFeed(
    reels: List<ReelItem>,
    onLikeReel: (String) -> Unit,
    onCommentReel: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShareReel: (ReelItem) -> Unit = {},
    onBookmarkReel: (String) -> Unit = {},
    onToggleFollowCreator: (String) -> Unit = {},
    playbackProgressOverride: Float? = null,
    headerContent: (@Composable () -> Unit)? = null
) {
    val pagerState = rememberPagerState(pageCount = { reels.size })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EntrelaDarkBg)
            .testTag("vertical_reels_feed_container")
    ) {
        if (reels.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("reels_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = EntrelaDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.VideoLibrary,
                                contentDescription = "No reels",
                                tint = EntrelaPrimaryVariant,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Reels Available",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Be the first to record a spark or pull to refresh from Firestore!",
                        color = EntrelaTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // Full-screen snapping VerticalPager
            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("vertical_reels_pager")
            ) { page ->
                val reel = reels[page]
                ReelFeedItem(
                    reel = reel,
                    onLike = { onLikeReel(reel.id) },
                    onComment = { onCommentReel(reel.id) },
                    onShare = { onShareReel(reel) },
                    onBookmark = { onBookmarkReel(reel.id) },
                    onToggleFollow = { onToggleFollowCreator(reel.creatorId) },
                    playbackProgressOverride = playbackProgressOverride,
                    headerContent = if (page == 0) headerContent else null
                )
            }
        }
    }
}

/**
 * Individual short video reel card with background video presentation,
 * interactive action rail, double-tap to like, play/pause, and caption.
 */
@Composable
fun ReelFeedItem(
    reel: ReelItem,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onBookmark: () -> Unit,
    onToggleFollow: () -> Unit,
    playbackProgressOverride: Float? = null,
    headerContent: (@Composable () -> Unit)? = null
) {
    var isPlaying by remember { mutableStateOf(true) }
    var showPlayPauseIcon by remember { mutableStateOf(false) }
    var showHeartAnimation by remember { mutableStateOf(false) }
    var isCaptionExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Vinyl rotation & playback progress infinite transition
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

    var manualProgress by remember(reel.id) { mutableStateOf<Float?>(null) }
    var pausedProgress by remember(reel.id) { mutableStateOf<Float?>(null) }

    val currentProgressFraction = when {
        playbackProgressOverride != null -> playbackProgressOverride
        manualProgress != null -> manualProgress!!
        !isPlaying -> pausedProgress ?: autoProgress
        else -> autoProgress
    }

    val currentOnLike by rememberUpdatedState(onLike)

    val heartPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartPulse"
    )

    // Like button bounce animation
    val likeScale = remember { Animatable(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(reel.id) {
                detectTapGestures(
                    onTap = {
                        if (isPlaying) {
                            pausedProgress = currentProgressFraction
                        } else {
                            pausedProgress = null
                            manualProgress = null
                        }
                        isPlaying = !isPlaying
                        showPlayPauseIcon = true
                    },
                    onDoubleTap = {
                        showHeartAnimation = !showHeartAnimation
                        currentOnLike()
                    }
                )
            }
            .testTag("reel_page_${reel.id}")
    ) {
        // Video / Backdrop presentation layer
        if (reel.drawableRes != null) {
            Image(
                painter = painterResource(id = reel.drawableRes),
                contentDescription = reel.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("reel_video_${reel.id}")
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1F1C2C), Color(0xFF2C194D), Color(0xFF0F0C20))
                        )
                    )
                    .testTag("reel_video_${reel.id}")
            )
        }

        // Dark gradients for high readability
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

        // Play / Pause feedback icon
        LaunchedEffect(showPlayPauseIcon) {
            if (showPlayPauseIcon) {
                delay(800)
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

        // Double-tap Big Heart Pop Animation (toggled on double-tap)
        AnimatedVisibility(
            visible = showHeartAnimation,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .testTag("heart_pop_${reel.id}")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .scale(heartPulse)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                EntrelaHeartRed.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
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
        }

        // Top optional header content (Stories row or top bar)
        if (headerContent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
            ) {
                headerContent()
            }
        }

        // Right-Side Vertical Floating Action Rail
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Creator Avatar & Follow Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.testTag("reel_avatar_${reel.id}")
            ) {
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
                            .clickable { onToggleFollow() }
                            .testTag("follow_creator_${reel.creatorId}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Follow",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Like Interaction Button
            ReelFeedActionButton(
                icon = if (reel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = formatCount(reel.likesCount),
                tint = if (reel.isLiked) EntrelaHeartRed else Color.White,
                onClick = {
                    coroutineScope.launch {
                        likeScale.animateTo(1.35f, tween(100))
                        likeScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    onLike()
                },
                modifier = Modifier.scale(likeScale.value),
                testTag = "reel_like_${reel.id}"
            )

            // Comment Interaction Button
            ReelFeedActionButton(
                icon = Icons.Default.ChatBubbleOutline,
                label = formatCount(reel.commentsCount),
                tint = Color.White,
                onClick = onComment,
                testTag = "reel_comments_${reel.id}"
            )

            // Share Interaction Button
            ReelFeedActionButton(
                icon = Icons.Default.Share,
                label = "${reel.sharesCount}",
                tint = Color.White,
                onClick = onShare,
                testTag = "reel_share_${reel.id}"
            )

            // Bookmark Interaction Button
            ReelFeedActionButton(
                icon = if (reel.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                label = if (reel.isBookmarked) "Saved" else "Save",
                tint = if (reel.isBookmarked) EntrelaGold else Color.White,
                onClick = onBookmark,
                testTag = "reel_bookmark_${reel.id}"
            )

            // Rotating Vinyl Sound Disc
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    .rotate(if (isPlaying) rotation else 0f)
                    .testTag("reel_vinyl_${reel.id}"),
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

        // Bottom-Left Overlay: Creator username, Caption, Tags, Audio Track
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, bottom = 24.dp)
        ) {
            // Creator Handle & Verified Tag
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "@${reel.creatorUsername}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("reel_creator_${reel.id}")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.Verified,
                    contentDescription = "Verified Creator",
                    tint = EntrelaPrimaryVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Caption with expandable toggle
            Text(
                text = reel.caption,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = if (isCaptionExpanded) 6 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable { isCaptionExpanded = !isCaptionExpanded }
                    .testTag("reel_caption_${reel.id}")
            )

            // Tags
            if (reel.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reel.tags.joinToString(" "),
                    color = EntrelaPrimaryVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Track Marquee
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("reel_audio_${reel.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${reel.audioTrack} • ${reel.artistName}",
                    color = Color.White,
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
                .pointerInput(reel.id) {
                    detectTapGestures(
                        onPress = { offset ->
                            val seekFraction = (offset.x / size.width).coerceIn(0f, 1f)
                            manualProgress = seekFraction
                        }
                    )
                }
                .testTag("reel_progress_indicator_${reel.id}")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = currentProgressFraction.coerceIn(0f, 1f))
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

/**
 * Standard action button item on the right-side reel interaction rail.
 */
@Composable
private fun ReelFeedActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .testTag(testTag)
            .clickable { onClick() }
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
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(java.util.Locale.US, "%.1fk", count / 1_000.0)
        else -> "$count"
    }
}
