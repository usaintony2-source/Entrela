package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RelationshipStatus
import com.example.ui.EntrelaViewModel
import com.example.ui.components.EntrelaGradientButton
import com.example.ui.components.InterestChip
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(viewModel: EntrelaViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    var bio by remember {
        mutableStateOf(currentUser?.bio ?: "Connecting on Entrela ✨ Passionate about music, traveling, and good conversations.")
    }
    var selectedStatus by remember {
        mutableStateOf(currentUser?.relationshipStatus ?: RelationshipStatus.OPEN_TO_CONNECT)
    }
    val allInterests = listOf(
        "Music 🎵", "Travel ✈️", "Nightlife 🍸", "Fitness 🏋️",
        "Gaming 🎮", "Cooking 🍳", "Photography 📷", "Art 🎨",
        "Cinema 🎬", "Tech 💻", "Fashion 👗", "Beach 🏖️",
        "Books 📚", "Coffee ☕", "Festival 🎪", "Dogs 🐕"
    )
    var selectedInterests by remember {
        mutableStateOf(currentUser?.interests?.toSet() ?: setOf("Music 🎵", "Travel ✈️", "Coffee ☕"))
    }
    val avatarChoices = listOf("💫", "✨", "🌺", "⚡", "🥂", "🎨", "🏄", "🚀", "🎸", "🌟", "🌸", "🔥")
    var selectedAvatar by remember {
        mutableStateOf(currentUser?.avatarEmoji ?: "💫")
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
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Step Header
            Text(
                text = "Welcome, ${currentUser?.name ?: "Explorer"}! 👋",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Set up your profile to let people discover your vibe",
                color = EntrelaTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar Preview with edit badge
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(EntrelaPrimary, EntrelaSecondary)
                            )
                        )
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(EntrelaDarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedAvatar,
                            fontSize = 48.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(EntrelaPrimary)
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Upload Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Choose your avatar icon or photo",
                color = EntrelaTextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal avatar choices
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(avatarChoices) { emoji ->
                    val isSelected = emoji == selectedAvatar
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) EntrelaPrimary.copy(alpha = 0.3f) else EntrelaDarkSurfaceVariant)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) EntrelaPrimary else EntrelaDarkBorder,
                                shape = CircleShape
                            )
                            .clickable { selectedAvatar = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 22.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Bio
            Text(
                text = "Account Bio",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 160) bio = it },
                label = { Text("Tell everyone what you're passionate about...", color = EntrelaTextMuted) },
                supportingText = {
                    Text(
                        text = "${bio.length}/160",
                        color = EntrelaTextMuted,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                minLines = 3,
                maxLines = 4,
                colors = outlinedTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_setup_bio_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Relationship Status
            Text(
                text = "Relationship Status",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RelationshipStatus.values().forEach { status ->
                    val isSelected = status == selectedStatus
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) EntrelaPrimary.copy(alpha = 0.25f) else EntrelaDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) EntrelaPrimary else EntrelaDarkBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedStatus = status }
                            .testTag("status_option_${status.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = status.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = status.label,
                                color = if (isSelected) Color.White else EntrelaTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Interests
            Text(
                text = "Personal Interests (Select at least 3)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                allInterests.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest)
                    InterestChip(
                        text = interest,
                        selected = isSelected,
                        onClick = {
                            selectedInterests = if (isSelected) {
                                selectedInterests - interest
                            } else {
                                selectedInterests + interest
                            }
                        },
                        modifier = Modifier.testTag("interest_chip_$interest")
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Complete Setup CTA
            EntrelaGradientButton(
                text = "Complete Profile & Enter Entrela ✨",
                onClick = {
                    viewModel.completeProfileSetup(
                        bio = bio.ifBlank { "Excited to meet new people on Entrela!" },
                        status = selectedStatus,
                        interests = selectedInterests.toList(),
                        avatarEmoji = selectedAvatar
                    )
                },
                testTag = "profile_setup_finish_button"
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
