package com.example.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.*

data class NavTabItem(
    val screen: Screen,
    val label: String,
    val activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)

val NavTabs = listOf(
    NavTabItem(Screen.REELS, "Reels", Icons.Default.VideoLibrary, Icons.Outlined.VideoLibrary, "nav_reels"),
    NavTabItem(Screen.EXPLORE_SPARKS, "Sparks", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder, "nav_sparks"),
    NavTabItem(Screen.CHATS, "Chats", Icons.Default.ChatBubble, Icons.Outlined.ChatBubbleOutline, "nav_chats"),
    NavTabItem(Screen.PROFILE, "Profile", Icons.Default.Person, Icons.Outlined.Person, "nav_profile")
)

@Composable
fun EntrelaBottomNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = EntrelaDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaDarkBorder),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabs.forEach { tab ->
                val isSelected = currentScreen == tab.screen
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) EntrelaPrimary else EntrelaTextMuted,
                    label = "iconColor"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigate(tab.screen) }
                        .padding(vertical = 4.dp)
                        .testTag(tab.tag)
                ) {
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .padding(horizontal = 12.dp)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .clip(CircleShape)
                                        .background(EntrelaPrimary.copy(alpha = 0.2f))
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                            contentDescription = tab.label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = tab.label,
                        color = if (isSelected) Color.White else EntrelaTextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
