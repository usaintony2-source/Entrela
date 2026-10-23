package com.example.ui.reels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ReelItem
import com.example.ui.EntrelaViewModel
import com.example.ui.components.GradientAvatar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelShareSheet(
    reel: ReelItem,
    viewModel: EntrelaViewModel,
    onDismiss: () -> Unit
) {
    val conversations by viewModel.conversations.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = EntrelaDarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = EntrelaDarkBorder) },
        modifier = Modifier.testTag("reel_share_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Share Reel",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = EntrelaTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Send directly to connections",
                color = EntrelaTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Direct matches row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(conversations) { conv ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(64.dp)
                            .clickable {
                                viewModel.sendMessage(
                                    conv.id,
                                    "Check out this reel by @${reel.creatorUsername}: \"${reel.caption.take(40)}...\" 🎬"
                                )
                                viewModel.showToast("Shared with ${conv.otherUserName}! ✨")
                                onDismiss()
                            }
                    ) {
                        GradientAvatar(emoji = conv.otherUserAvatarEmoji, size = 52.dp, isOnline = conv.isOnline)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = conv.otherUserName.substringBefore(" "),
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShareOption(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy Link",
                    onClick = {
                        viewModel.showToast("Link copied to clipboard: entrela.app/r/${reel.id}")
                        onDismiss()
                    }
                )
                ShareOption(
                    icon = Icons.Default.Share,
                    label = "More Apps",
                    onClick = {
                        viewModel.showToast("Sharing reel: ${reel.caption.take(30)}...")
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ShareOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(EntrelaDarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = EntrelaPrimaryVariant)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
