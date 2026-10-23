package com.example.ui.profile

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RelationshipStatus
import com.example.model.UserProfile
import com.example.ui.EntrelaViewModel
import com.example.ui.auth.outlinedTextFieldColors
import com.example.ui.components.EntrelaGradientButton
import com.example.ui.components.InterestChip
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileSheet(
    currentUser: UserProfile,
    viewModel: EntrelaViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentUser.name) }
    var bio by remember { mutableStateOf(currentUser.bio) }
    var status by remember { mutableStateOf(currentUser.relationshipStatus) }
    var avatarEmoji by remember { mutableStateOf(currentUser.avatarEmoji) }
    var selectedInterests by remember { mutableStateOf(currentUser.interests.toSet()) }

    val allInterests = listOf(
        "Music 🎵", "Travel ✈️", "Nightlife 🍸", "Fitness 🏋️",
        "Gaming 🎮", "Cooking 🍳", "Photography 📷", "Art 🎨",
        "Cinema 🎬", "Tech 💻", "Fashion 👗", "Beach 🏖️"
    )
    val avatarChoices = listOf("💫", "✨", "🌺", "⚡", "🥂", "🎨", "🏄", "🚀", "🎸", "🌟")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = EntrelaDarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = EntrelaDarkBorder) },
        modifier = Modifier.testTag("edit_profile_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Profile",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = EntrelaTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Avatar picker
            Text(text = "Choose Avatar", color = EntrelaTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(avatarChoices) { emoji ->
                    val isSelected = emoji == avatarEmoji
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) EntrelaPrimary.copy(alpha = 0.3f) else EntrelaDarkSurfaceVariant)
                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) EntrelaPrimary else EntrelaDarkBorder, CircleShape)
                            .clickable { avatarEmoji = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name", color = EntrelaTextMuted) },
                colors = outlinedTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("edit_name_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bio
            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 160) bio = it },
                label = { Text("Account Bio", color = EntrelaTextMuted) },
                colors = outlinedTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("edit_bio_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Relationship Status
            Text(text = "Relationship Status", color = EntrelaTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RelationshipStatus.values().forEach { st ->
                    val isSelected = st == status
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) EntrelaPrimary.copy(alpha = 0.25f) else EntrelaDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) EntrelaPrimary else EntrelaDarkBorder),
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { status = st }
                    ) {
                        Text(
                            text = "${st.emoji} ${st.label}",
                            color = if (isSelected) Color.White else EntrelaTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interests
            Text(text = "Interests", color = EntrelaTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                            selectedInterests = if (isSelected) selectedInterests - interest else selectedInterests + interest
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            EntrelaGradientButton(
                text = "Save Changes ✨",
                onClick = {
                    viewModel.updateProfile(
                        name = name.ifBlank { currentUser.name },
                        bio = bio,
                        status = status,
                        interests = selectedInterests.toList(),
                        avatarEmoji = avatarEmoji
                    )
                },
                testTag = "edit_profile_save_button"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
