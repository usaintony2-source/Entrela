package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.auth.ForgotPasswordScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.ProfileSetupScreen
import com.example.ui.auth.RegisterScreen
import com.example.ui.chat.ChatsScreen
import com.example.ui.chat.DirectChatScreen
import com.example.ui.explore.ExploreScreen
import com.example.ui.navigation.EntrelaBottomNavBar
import com.example.ui.profile.EditProfileSheet
import com.example.ui.profile.ProfileScreen
import com.example.ui.reels.ReelCommentsSheet
import com.example.ui.reels.ReelShareSheet
import com.example.ui.reels.ReelsScreen
import com.example.ui.stories.CreateStoryDialog
import com.example.ui.stories.StoryViewerScreen
import com.example.ui.theme.EntrelaDarkBg
import com.example.ui.theme.EntrelaDarkSurfaceVariant
import com.example.ui.theme.EntrelaPrimary

@Composable
fun EntrelaApp(
    viewModel: EntrelaViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeConvId by viewModel.activeConversationId.collectAsState()
    val activeStoryGroup by viewModel.activeStoryGroup.collectAsState()
    val commentsReelId by viewModel.activeCommentsReelId.collectAsState()
    val shareReel by viewModel.activeShareReel.collectAsState()
    val isCreateStoryOpen by viewModel.isCreateStoryOpen.collectAsState()
    val isEditProfileOpen by viewModel.isEditProfileOpen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val reels by viewModel.reels.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Android Hardware / Predictive Back Handler
    val isMainTab = currentScreen in listOf(Screen.REELS, Screen.EXPLORE_SPARKS, Screen.CHATS, Screen.PROFILE)
    BackHandler(enabled = !isMainTab || currentScreen != Screen.REELS) {
        if (!viewModel.navigateBack()) {
            if (currentScreen != Screen.REELS) {
                viewModel.navigateTo(Screen.REELS)
            }
        }
    }

    Scaffold(
        containerColor = EntrelaDarkBg,
        bottomBar = {
            if (isMainTab) {
                EntrelaBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isMainTab) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            when (currentScreen) {
                Screen.LOGIN -> LoginScreen(viewModel = viewModel)
                Screen.REGISTER -> RegisterScreen(viewModel = viewModel)
                Screen.FORGOT_PASSWORD -> ForgotPasswordScreen(viewModel = viewModel)
                Screen.PROFILE_SETUP -> ProfileSetupScreen(viewModel = viewModel)
                Screen.REELS -> ReelsScreen(viewModel = viewModel)
                Screen.EXPLORE_SPARKS -> ExploreScreen(viewModel = viewModel)
                Screen.CHATS -> ChatsScreen(viewModel = viewModel)
                Screen.DIRECT_CHAT -> {
                    DirectChatScreen(
                        conversationId = activeConvId ?: "",
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                Screen.PROFILE -> ProfileScreen(viewModel = viewModel)
                Screen.STORY_VIEWER -> {
                    if (activeStoryGroup != null) {
                        StoryViewerScreen(
                            group = activeStoryGroup!!,
                            viewModel = viewModel,
                            onClose = { viewModel.navigateBack() }
                        )
                    }
                }
                Screen.LIVE_STREAM -> {
                    com.example.ui.live.LiveStreamScreen(
                        viewModel = viewModel,
                        onClose = { viewModel.endLiveStream() }
                    )
                }
                Screen.VIDEO_RECORDER -> {
                    com.example.ui.video.VideoRecorderScreen(
                        viewModel = viewModel,
                        onClose = { viewModel.navigateBack() }
                    )
                }
                Screen.VIDEO_EDITOR -> {
                    com.example.ui.video.VideoEditorScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                Screen.TRENDING_LEADERBOARD -> {
                    com.example.ui.trending.TrendingLeaderboardScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
            }

            // Interactive Bottom Sheets & Modals
            val commentReel = reels.find { it.id == commentsReelId }
            if (commentReel != null) {
                ReelCommentsSheet(
                    reel = commentReel,
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeComments() }
                )
            }

            if (shareReel != null) {
                ReelShareSheet(
                    reel = shareReel!!,
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeShare() }
                )
            }

            if (isCreateStoryOpen) {
                CreateStoryDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.setCreateStoryOpen(false) }
                )
            }

            if (isEditProfileOpen && currentUser != null) {
                EditProfileSheet(
                    currentUser = currentUser!!,
                    viewModel = viewModel,
                    onDismiss = { viewModel.setEditProfileOpen(false) }
                )
            }

            // Global Feedback Toast
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp)
            ) {
                Surface(
                    color = EntrelaDarkSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaPrimary.copy(alpha = 0.6f)),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = toastMessage ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                LaunchedEffect(toastMessage) {
                    if (toastMessage != null) {
                        kotlinx.coroutines.delay(2800)
                        viewModel.clearToast()
                    }
                }
            }
        }
    }
}
