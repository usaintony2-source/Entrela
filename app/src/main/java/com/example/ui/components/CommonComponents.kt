package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RelationshipStatus
import com.example.ui.theme.*

@Composable
fun EntrelaLogo(modifier: Modifier = Modifier, sizeSp: Int = 32) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleAlpha"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size((sizeSp * 1.35).dp)
                .clip(CircleShape)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(EntrelaPrimary, EntrelaSecondary)),
                    shape = CircleShape
                )
                .padding(2.dp)
                .clip(CircleShape)
                .background(EntrelaDarkBg),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.entrela_logo_v2_1792264922406),
                contentDescription = "Entrela Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Entrela",
                color = Color.White,
                fontSize = sizeSp.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "⚡",
                fontSize = (sizeSp * 0.45).sp
            )
        }
    }
}

@Composable
fun EntrelaGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "gradient_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = EntrelaDarkSurfaceVariant
        ),
        contentPadding = PaddingValues(),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(elevation = if (enabled) 12.dp else 0.dp, shape = RoundedCornerShape(16.dp), spotColor = EntrelaPrimary)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) {
                        Brush.horizontalGradient(
                            colors = listOf(EntrelaPrimary, EntrelaSecondary)
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF33334A), Color(0xFF28283B))
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) Color.White else EntrelaTextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: RelationshipStatus,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = EntrelaSecondary.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, EntrelaSecondary.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status.emoji,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.label,
                color = EntrelaPrimaryVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun GradientAvatar(
    emoji: String,
    size: Dp = 56.dp,
    hasStory: Boolean = false,
    isOnline: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        val borderModifier = if (hasStory) {
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            EntrelaPrimary,
                            EntrelaSecondary,
                            EntrelaTertiary,
                            EntrelaPrimary
                        )
                    )
                )
                .padding(2.5.dp)
        } else {
            Modifier.size(size)
        }

        Box(
            modifier = borderModifier
                .clip(CircleShape)
                .background(EntrelaDarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = (size.value * 0.46).sp,
                textAlign = TextAlign.Center
            )
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size((size.value * 0.28).dp)
                    .clip(CircleShape)
                    .background(EntrelaDarkBg)
                    .padding(2.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(EntrelaOnlineGreen)
                )
            }
        }
    }
}

@Composable
fun InterestChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) EntrelaPrimary.copy(alpha = 0.25f) else EntrelaDarkSurfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) EntrelaPrimary else EntrelaDarkBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else EntrelaTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
