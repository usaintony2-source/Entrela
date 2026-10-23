package com.example.ui.profile

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ReelItem
import com.example.model.RelationshipStatus
import com.example.model.UserProfile
import com.example.ui.EntrelaViewModel
import com.example.ui.Screen
import com.example.ui.components.GradientAvatar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

private const val TAG = "ProfileView"

enum class ProfileViewTab(val title: String, val icon: ImageVector) {
    REELS("Reels", Icons.Default.VideoLibrary),
    ACCOUNT("Account & Security", Icons.Default.Shield),
    MOMENTS("Moments", Icons.Default.AutoAwesome),
    LIKED("Liked", Icons.Default.Favorite)
}

/**
 * ProfileView component displaying complete user information:
 * - Profile picture / avatar with status & edit shortcuts
 * - Name, handle (@username), and verified badge
 * - Bio & relationship status
 * - Followers and following counters (plus reels and sparks)
 * - Interests tags
 * - Seamless integration with Firebase Auth for account management:
 *   - Current authenticated email, UID, and provider info
 *   - Password reset via Firebase Auth
 *   - Account verification
 *   - Account sign-out and deletion with safeguards
 */
@Composable
fun ProfileView(
    viewModel: EntrelaViewModel,
    modifier: Modifier = Modifier,
    onEditProfileClick: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val reels by viewModel.reels.collectAsState()
    val user = currentUser ?: UserProfile()

    ProfileView(
        user = user,
        reels = reels,
        modifier = modifier,
        onEditProfile = {
            if (onEditProfileClick != null) {
                onEditProfileClick()
            } else {
                viewModel.setEditProfileOpen(true)
            }
        },
        onLogout = {
            if (onLogoutClick != null) {
                onLogoutClick()
            } else {
                viewModel.logout()
            }
        },
        onRecordReel = { viewModel.openVideoRecorder() },
        onShareProfile = { viewModel.showToast("Profile link copied! Share with friends ✨") },
        onAddStory = { viewModel.setCreateStoryOpen(true) },
        onNavigateToReels = { viewModel.navigateTo(Screen.REELS) },
        onPasswordResetRequested = { email ->
            viewModel.showToast("Password reset email sent to $email ✉️")
        },
        onProfileUpdated = { newName, newBio ->
            viewModel.updateProfile(
                name = newName,
                bio = newBio,
                status = user.relationshipStatus,
                interests = user.interests,
                avatarEmoji = user.avatarEmoji
            )
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileView(
    user: UserProfile,
    reels: List<ReelItem> = emptyList(),
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean = true,
    initialTab: ProfileViewTab = ProfileViewTab.REELS,
    firebaseAuthInstance: FirebaseAuth? = null,
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    onRecordReel: () -> Unit = {},
    onShareProfile: () -> Unit = {},
    onAddStory: () -> Unit = {},
    onNavigateToReels: () -> Unit = {},
    onPasswordResetRequested: (String) -> Unit = {},
    onProfileUpdated: (String, String) -> Unit = { _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(initialTab) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showResetPasswordConfirmDialog by remember { mutableStateOf(false) }
    var resetFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // Safe Firebase Auth access
    val firebaseAuth: FirebaseAuth? = remember(firebaseAuthInstance) {
        if (firebaseAuthInstance != null) {
            firebaseAuthInstance
        } else {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Throwable) {
                Log.w(TAG, "FirebaseAuth not initialized or running in test environment: ${e.message}")
                null
            }
        }
    }

    // Reactive Firebase User
    var currentFirebaseUser by remember { mutableStateOf<FirebaseUser?>(firebaseAuth?.currentUser) }

    // Refresh Firebase user on compose
    LaunchedEffect(firebaseAuth) {
        currentFirebaseUser = firebaseAuth?.currentUser
    }

    val displayEmail = currentFirebaseUser?.email ?: user.email
    val authUid = currentFirebaseUser?.uid ?: "entrela_uid_${user.id}"
    val isEmailVerified = currentFirebaseUser?.isEmailVerified ?: true
    val authProvider = remember(currentFirebaseUser) {
        val providers = currentFirebaseUser?.providerData?.map { it.providerId }?.filter { it.isNotBlank() }
        when {
            providers?.contains("google.com") == true -> "Google Sign-In"
            providers?.contains("password") == true -> "Email & Password"
            currentFirebaseUser?.isAnonymous == true -> "Guest Account"
            else -> "Firebase Cloud Auth"
        }
    }

    // Dialog: Logout Confirmation
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out of Entrela?", color = Color.White) },
            text = {
                Text(
                    "You will be securely signed out from Firebase Authentication and local sessions.",
                    color = EntrelaTextSecondary
                )
            },
            containerColor = EntrelaDarkSurface,
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        try {
                            firebaseAuth?.signOut()
                        } catch (e: Exception) {
                            Log.w(TAG, "Firebase signOut error: ${e.message}")
                        }
                        onLogout()
                    },
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Log Out", color = EntrelaHeartRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = EntrelaTextSecondary)
                }
            }
        )
    }

    // Dialog: Delete Account Safeguard
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Entrela Account?", color = EntrelaHeartRed) },
            text = {
                Text(
                    "This action permanently removes your profile, reels, and Firebase Authentication credentials. This cannot be undone.",
                    color = EntrelaTextSecondary
                )
            },
            containerColor = EntrelaDarkSurface,
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        try {
                            currentFirebaseUser?.delete()
                        } catch (e: Exception) {
                            Log.w(TAG, "Firebase account deletion error: ${e.message}")
                        }
                        onLogout()
                    },
                    modifier = Modifier.testTag("confirm_delete_account_button")
                ) {
                    Text("Delete Permanently", color = EntrelaHeartRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Keep Account", color = EntrelaTextSecondary)
                }
            }
        )
    }

    // Dialog: Password Reset Confirmation
    if (showResetPasswordConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetPasswordConfirmDialog = false },
            title = { Text("Send Password Reset Email?", color = Color.White) },
            text = {
                Text(
                    "A secure Firebase password reset link will be sent to:\n$displayEmail",
                    color = EntrelaTextSecondary
                )
            },
            containerColor = EntrelaDarkSurface,
            confirmButton = {
                Button(
                    onClick = {
                        showResetPasswordConfirmDialog = false
                        try {
                            firebaseAuth?.sendPasswordResetEmail(displayEmail)
                        } catch (e: Exception) {
                            Log.w(TAG, "sendPasswordResetEmail error: ${e.message}")
                        }
                        resetFeedbackMessage = "Reset email dispatched to $displayEmail"
                        onPasswordResetRequested(displayEmail)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EntrelaPrimary),
                    modifier = Modifier.testTag("confirm_send_reset_email_button")
                ) {
                    Text("Send Email", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPasswordConfirmDialog = false }) {
                    Text("Cancel", color = EntrelaTextSecondary)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EntrelaDarkBg)
            .testTag("profile_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            // --- Top App Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "@${user.username}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("profile_username")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    if (user.isVerified) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified account",
                            tint = EntrelaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditProfile,
                        modifier = Modifier.testTag("profile_edit_icon_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White)
                    }
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.testTag("profile_logout_icon_btn")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = EntrelaTextSecondary)
                    }
                }
            }

            // Scrollable Profile Header & Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture / Avatar with Gradient Ring
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 10.dp)
                        .testTag("profile_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    GradientAvatar(
                        emoji = user.avatarEmoji,
                        size = 92.dp,
                        hasStory = true,
                        onClick = onEditProfile
                    )

                    // Quick Edit Avatar Button Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(EntrelaPrimary)
                            .border(2.dp, EntrelaDarkBg, CircleShape)
                            .clickable { onEditProfile() }
                            .testTag("profile_avatar_edit_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change avatar",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Full Name
                Text(
                    text = user.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("profile_name")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Relationship Status Badge
                StatusBadge(status = user.relationshipStatus)

                Spacer(modifier = Modifier.height(14.dp))

                // Counts: Followers, Following, Reels, Sparks
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(EntrelaDarkSurface)
                        .border(1.dp, EntrelaDarkBorder, RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        label = "Reels",
                        value = "${reels.size}",
                        testTag = "profile_stat_reels"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(26.dp)
                            .background(EntrelaDarkBorder)
                    )
                    StatItem(
                        label = "Followers",
                        value = formatCount(user.followersCount),
                        testTag = "profile_stat_followers"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(26.dp)
                            .background(EntrelaDarkBorder)
                    )
                    StatItem(
                        label = "Following",
                        value = formatCount(user.followingCount),
                        testTag = "profile_stat_following"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(26.dp)
                            .background(EntrelaDarkBorder)
                    )
                    StatItem(
                        label = "Sparks",
                        value = "28.4k",
                        testTag = "profile_stat_sparks"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bio
                Text(
                    text = user.bio,
                    color = EntrelaTextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .testTag("profile_bio")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Interests Tags
                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    user.interests.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = EntrelaDarkSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 3.dp)
                        ) {
                            Text(
                                text = tag,
                                color = EntrelaTertiary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Row: Edit Profile, Record/Edit Reel, Share Profile
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EntrelaDarkSurfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("profile_edit_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onRecordReel,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EntrelaPrimary),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(42.dp)
                            .testTag("profile_record_reel_button")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record / Edit", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onShareProfile,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EntrelaDarkSurfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("profile_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Feedback banner if reset email sent
            AnimatedVisibility(visible = resetFeedbackMessage != null) {
                Surface(
                    color = EntrelaDarkSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EntrelaOnlineGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = resetFeedbackMessage ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { resetFeedbackMessage = null },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = EntrelaTextMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Tab Navigation (Reels, Account & Security, Moments, Liked)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EntrelaDarkSurface),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileViewTab.values().forEach { tab ->
                    val isSelected = tab == selectedTab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_tab_${tab.name}")
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) EntrelaPrimary else EntrelaTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tab.title,
                            color = if (isSelected) Color.White else EntrelaTextMuted,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(2.dp)
                                    .background(EntrelaPrimary)
                            )
                        }
                    }
                }
            }

            // Tab Content Pane
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                when (selectedTab) {
                    ProfileViewTab.REELS -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            reels.chunked(3).forEach { rowReels ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    rowReels.forEach { reel ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(0.72f)
                                                .background(EntrelaDarkSurfaceVariant)
                                                .clickable { onNavigateToReels() }
                                        ) {
                                            if (reel.drawableRes != null) {
                                                Image(
                                                    painter = painterResource(id = reel.drawableRes),
                                                    contentDescription = reel.caption,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }

                                            // Views count badge
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "${reel.likesCount / 100}k",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    repeat(3 - rowReels.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    ProfileViewTab.ACCOUNT -> {
                        // Integrated Firebase Auth Account Management Console
                        FirebaseAuthAccountManagementView(
                            displayEmail = displayEmail,
                            authUid = authUid,
                            authProvider = authProvider,
                            isEmailVerified = isEmailVerified,
                            onSendResetPassword = { showResetPasswordConfirmDialog = true },
                            onEditProfile = onEditProfile,
                            onLogout = { showLogoutDialog = true },
                            onDeleteAccount = { showDeleteAccountDialog = true }
                        )
                    }

                    ProfileViewTab.MOMENTS -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = EntrelaPrimary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "24h Status Moments",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your temporary stories and highlights will appear here.",
                                    color = EntrelaTextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onAddStory,
                                    colors = ButtonDefaults.buttonColors(containerColor = EntrelaPrimary)
                                ) {
                                    Text("+ Add Story")
                                }
                            }
                        }
                    }

                    ProfileViewTab.LIKED -> {
                        val likedReels = reels.filter { it.isLiked }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            likedReels.chunked(3).forEach { rowReels ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    rowReels.forEach { reel ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(0.72f)
                                                .background(EntrelaDarkSurfaceVariant)
                                                .clickable { onNavigateToReels() }
                                        ) {
                                            if (reel.drawableRes != null) {
                                                Image(
                                                    painter = painterResource(id = reel.drawableRes),
                                                    contentDescription = reel.caption,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Icon(
                                                Icons.Default.Favorite,
                                                contentDescription = "Liked",
                                                tint = EntrelaHeartRed,
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(6.dp)
                                                    .size(16.dp)
                                            )
                                        }
                                    }
                                    repeat(3 - rowReels.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Account Management component specifically integrating Firebase Auth details and controls:
 * - Linked Email
 * - Firebase UID
 * - Auth Provider
 * - Verification status
 * - Password reset actions
 * - Sign out and account deletion safeguards
 */
@Composable
fun FirebaseAuthAccountManagementView(
    displayEmail: String,
    authUid: String,
    authProvider: String,
    isEmailVerified: Boolean,
    onSendResetPassword: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("profile_firebase_management_panel"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Firebase Cloud Auth Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EntrelaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_firebase_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Firebase Security",
                            tint = EntrelaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Firebase Auth Identity",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EntrelaOnlineGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "ACTIVE",
                            color = EntrelaOnlineGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Divider(color = EntrelaDarkBorder, thickness = 1.dp)

                // Email Row
                AccountInfoRow(
                    icon = Icons.Default.Email,
                    label = "Authenticated Email",
                    value = displayEmail,
                    testTag = "profile_firebase_email"
                )

                // Auth Provider Row
                AccountInfoRow(
                    icon = Icons.Default.Lock,
                    label = "Sign-In Method",
                    value = authProvider,
                    testTag = "profile_firebase_provider"
                )

                // Firebase UID Row
                AccountInfoRow(
                    icon = Icons.Default.Key,
                    label = "Firebase UID",
                    value = authUid.take(18) + if (authUid.length > 18) "…" else "",
                    isMonospace = true,
                    testTag = "profile_firebase_uid"
                )

                // Verification Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isEmailVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isEmailVerified) EntrelaOnlineGreen else EntrelaGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEmailVerified) "Email Verified" else "Email Pending Verification",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    if (!isEmailVerified) {
                        TextButton(
                            onClick = onSendResetPassword,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Verify", color = EntrelaPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Account Management Actions Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EntrelaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Account Management",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Password Reset Button
                OutlinedButton(
                    onClick = onSendResetPassword,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_reset_password_button")
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = EntrelaPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Password Reset Email", fontSize = 13.sp)
                }

                // Edit Profile details
                OutlinedButton(
                    onClick = onEditProfile,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = EntrelaTertiary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Bio & Details", fontSize = 13.sp)
                }

                // Sign Out Button
                Button(
                    onClick = onLogout,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EntrelaDarkSurfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_logout_button")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out of Firebase Account", color = Color.White, fontSize = 13.sp)
                }
            }
        }

        // Danger Zone: Account Deletion
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EntrelaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaHeartRed.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Danger Zone",
                    color = EntrelaHeartRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Permanently delete your Entrela profile and associated Firebase credentials.",
                    color = EntrelaTextSecondary,
                    fontSize = 11.sp
                )
                Button(
                    onClick = onDeleteAccount,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EntrelaHeartRed.copy(alpha = 0.85f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_delete_account_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AccountInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isMonospace: Boolean = false,
    testTag: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = EntrelaTextMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = EntrelaTextSecondary, fontSize = 12.sp)
        }

        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            modifier = if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag(testTag)
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = EntrelaTextMuted,
            fontSize = 11.sp
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fk", count / 1_000.0)
        else -> count.toString()
    }
}
