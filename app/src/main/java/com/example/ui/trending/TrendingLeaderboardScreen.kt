package com.example.ui.trending

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CountryRegion
import com.example.model.TrendingLeaderboardItem
import com.example.ui.EntrelaViewModel
import com.example.ui.Screen
import com.example.ui.components.GradientAvatar
import com.example.ui.theme.*

@Composable
fun TrendingLeaderboardScreen(
    viewModel: EntrelaViewModel,
    onBack: () -> Unit
) {
    val supportedRegions = viewModel.supportedRegions
    val allItems by viewModel.allLeaderboardItems.collectAsState()
    val isWorldwide by viewModel.isWorldwideLeaderboard.collectAsState()
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val isLocationEnabled by viewModel.isLocationEnabled.collectAsState()

    val filteredList = remember(isWorldwide, selectedRegion, allItems) {
        if (isWorldwide) {
            allItems.sortedByDescending { it.trendScore }
        } else {
            val countryItems = allItems.filter { it.country.code == selectedRegion.code }
            if (countryItems.isNotEmpty()) countryItems.sortedByDescending { it.trendScore }
            else allItems.sortedByDescending { it.trendScore } // Fallback to all if region empty
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EntrelaDarkBg)
            .testTag("trending_leaderboard_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("leaderboard_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Trending Sparks Leaderboard",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "🔥", fontSize = 16.sp)
                    }
                    Text(
                        text = "Real-time AI Video Ranking & Viral Velocity",
                        color = EntrelaTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Dual Toggle: Worldwide vs Country-Wise
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EntrelaDarkSurface)
                    .border(1.dp, EntrelaDarkBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                // Worldwide Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isWorldwide) Brush.horizontalGradient(listOf(EntrelaPrimary, EntrelaSecondary))
                            else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable {
                            viewModel.setLeaderboardFilter(true, supportedRegions[0])
                        }
                        .padding(vertical = 10.dp)
                        .testTag("leaderboard_tab_worldwide"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌐", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Worldwide Rankings",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isWorldwide) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                // Country-Wise Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (!isWorldwide) Brush.horizontalGradient(listOf(EntrelaSecondary, EntrelaPrimary))
                            else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable {
                            viewModel.setLeaderboardFilter(false, supportedRegions[1]) // Default to Japan
                        }
                        .padding(vertical = 10.dp)
                        .testTag("leaderboard_tab_country"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🗺️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Country-Wise",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (!isWorldwide) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Country Selector row if Country-Wise mode is active
            AnimatedVisibility(visible = !isWorldwide) {
                Column(modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Country Region:",
                            color = EntrelaTextMuted,
                            fontSize = 11.sp
                        )
                        if (isLocationEnabled) {
                            Text(
                                text = "📍 GPS Matched",
                                color = EntrelaOnlineGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(supportedRegions.filter { it.code != "ALL" }) { region ->
                            val isSelected = region.code == selectedRegion.code
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) EntrelaPrimary.copy(alpha = 0.25f) else EntrelaDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) EntrelaPrimary else EntrelaDarkBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.setLeaderboardFilter(false, region)
                                    }
                                    .testTag("country_filter_${region.code}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = region.flagEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = region.name,
                                        color = if (isSelected) Color.White else EntrelaTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Leaderboard Items Stream
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredList) { item ->
                    LeaderboardCard(
                        item = item,
                        onClick = {
                            viewModel.navigateTo(Screen.REELS)
                            viewModel.showToast("Playing #${item.rank}: ${item.title} ✨")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardCard(
    item: TrendingLeaderboardItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = EntrelaDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (item.rank) {
                1 -> EntrelaGold.copy(alpha = 0.8f)
                2 -> Color(0xFFC0C0C0).copy(alpha = 0.7f)
                3 -> Color(0xFFCD7F32).copy(alpha = 0.7f)
                else -> EntrelaDarkBorder
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_item_${item.rank}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number / Medal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(36.dp)
            ) {
                val rankText = when (item.rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "#${item.rank}"
                }
                Text(
                    text = rankText,
                    color = Color.White,
                    fontSize = if (item.rank <= 3) 22.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Rank Change delta
                if (item.rankChange > 0) {
                    Text(
                        text = "▲ +${item.rankChange}",
                        color = EntrelaOnlineGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (item.rankChange < 0) {
                    Text(
                        text = "▼ ${item.rankChange}",
                        color = EntrelaHeartRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "▬ 0",
                        color = EntrelaTextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Thumbnail Preview
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EntrelaDarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (item.drawableRes != null) {
                    Image(
                        painter = painterResource(id = item.drawableRes),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.country.flagEmoji} @${item.creatorUsername}",
                        color = EntrelaTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Trend Score Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EntrelaPrimary.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaPrimary.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔥", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${item.trendScore}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "🎵 ${item.audioTrack}",
                    color = EntrelaPrimaryVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Stats: Views, Likes, Shares
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👁️ ${item.viewsCount / 1000}k",
                        color = EntrelaTextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "❤️ ${item.likesCount / 1000}k",
                        color = EntrelaTextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "✨ ${item.sparkSharesCount / 1000}k",
                        color = EntrelaTextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
