package com.example.ui.explore

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.MatchProfile
import com.example.ui.EntrelaViewModel
import com.example.ui.components.GradientAvatar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(viewModel: EntrelaViewModel) {
    val context = LocalContext.current
    val isLocationEnabled by viewModel.isLocationEnabled.collectAsState()
    val matches by viewModel.matches.collectAsState()

    var sortByDistance by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.setLocationPermissionGranted(granted)
        if (granted) sortByDistance = true
    }

    val displayedMatches = remember(matches, sortByDistance) {
        if (sortByDistance) matches.sortedBy { it.distanceMiles }
        else matches
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
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Explore & Sparks ✨",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Find connections nearby and share experiences",
                        color = EntrelaTextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Go Live quick CTA
                Button(
                    onClick = { viewModel.startLiveStream() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EntrelaHeartRed),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("explore_go_live_btn")
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Go Live", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Dedicated Trending Sparks Leaderboard Hub Card
            Surface(
                onClick = { viewModel.openLeaderboard() },
                shape = RoundedCornerShape(18.dp),
                color = EntrelaDarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.2.dp, EntrelaGold.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("explore_trending_leaderboard_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(EntrelaGold, EntrelaPrimary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Trending Sparks Leaderboard",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EntrelaGold.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaGold.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "TOP 100",
                                    color = EntrelaGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isLocationEnabled) "Worldwide & Localized (Japan 🇯🇵) real-time AI rankings" else "Worldwide & Country rankings powered by AI Trend Engine",
                            color = EntrelaTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "View Leaderboard",
                        tint = EntrelaGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Location Proximity Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = EntrelaDarkSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isLocationEnabled) EntrelaPrimary.copy(alpha = 0.5f) else EntrelaDarkBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = if (isLocationEnabled) EntrelaPrimary else EntrelaTextMuted,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isLocationEnabled) "Precise Location Active 📍" else "Discover Nearby Sparks",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isLocationEnabled) "Showing matches near Shibuya & Tokyo" else "Enable GPS to view distance & meet people around you",
                            color = EntrelaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    if (!isLocationEnabled) {
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EntrelaPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("enable_gps_button")
                        ) {
                            Text("Enable", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        FilterChip(
                            selected = sortByDistance,
                            onClick = { sortByDistance = !sortByDistance },
                            label = { Text("Nearest", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EntrelaPrimary.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedMatches, key = { it.id }) { match ->
                    MatchDetailedCard(
                        match = match,
                        showDistance = isLocationEnabled,
                        onConnect = {
                            viewModel.startChatWithMatch(match)
                        },
                        onWave = {
                            viewModel.showToast("Waved at ${match.name}! 👋")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchDetailedCard(
    match: MatchProfile,
    showDistance: Boolean,
    onConnect: () -> Unit,
    onWave: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = EntrelaDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_profile_card_${match.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar, Info, Compatibility badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GradientAvatar(
                    emoji = match.avatarEmoji,
                    size = 56.dp,
                    hasStory = true,
                    isOnline = match.isOnline
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${match.name}, ${match.age}",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(status = match.relationshipStatus)
                        if (showDistance) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📍 ${match.distanceMiles} mi",
                                color = EntrelaTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Match percentage badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(EntrelaPrimary, EntrelaSecondary)
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${match.compatibility}% Spark",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Proximity city tag if GPS is on
            if (showDistance) {
                Text(
                    text = "Nearby in ${match.city}",
                    color = EntrelaTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Bio
            Text(
                text = match.bio,
                color = EntrelaTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Interests chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                match.interests.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EntrelaDarkSurfaceVariant
                    ) {
                        Text(
                            text = tag,
                            color = EntrelaTertiary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions: Wave & Direct Message
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onWave,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text("Wave 👋", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EntrelaPrimary),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat Now ✨", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
