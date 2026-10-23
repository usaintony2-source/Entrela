package com.example.ui.video

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.EntrelaViewModel
import com.example.ui.theme.*

@Composable
fun VideoRecorderScreen(
    viewModel: EntrelaViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasCameraPermission = perms[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableFloatStateOf(0f) }
    val maxDurationSec = 30f
    var isFlashOn by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var selectedSpeed by remember { mutableStateOf("1x") }

    // Pulsing record button animation
    val infiniteTransition = rememberInfiniteTransition(label = "rec")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    // Recording duration timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording && recordingDurationSec < maxDurationSec) {
                kotlinx.coroutines.delay(100)
                recordingDurationSec += 0.1f
            }
            if (recordingDurationSec >= maxDurationSec) {
                isRecording = false
            }
        }
    }

    val chosenVideoRes = if (isFrontCamera) R.drawable.reel_bg_party_1792260318860 else R.drawable.reel_bg_couple_1792260319176

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_recorder_container")
    ) {
        // High-framerate video preview simulation
        Image(
            painter = painterResource(id = chosenVideoRes),
            contentDescription = "Camera Feed",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        // Top Header Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Progress Bar for Recording
            LinearProgressIndicator(
                progress = { (recordingDurationSec / maxDurationSec).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = EntrelaPrimary,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .testTag("recorder_close_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                // Selected sound pill / Spotify layer selector
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable {
                        viewModel.showToast("Audio track will be mixed in the Video Editor ✨")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = EntrelaPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Sound / Spotify",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Flash toggle
                IconButton(
                    onClick = { isFlashOn = !isFlashOn },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isFlashOn) EntrelaGold.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (isFlashOn) EntrelaGold else Color.White
                    )
                }
            }
        }

        // Right side camera adjustments: Speed, Flip, Timer
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Flip Camera
            IconButton(
                onClick = { isFrontCamera = !isFrontCamera },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .testTag("recorder_flip_camera_btn")
            ) {
                Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip", tint = Color.White)
            }

            // Speed Selector
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.clickable {
                    selectedSpeed = when (selectedSpeed) {
                        "0.5x" -> "1x"
                        "1x" -> "2x"
                        "2x" -> "3x"
                        else -> "0.5x"
                    }
                }
            ) {
                Text(
                    text = selectedSpeed,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            // High FPS Mode Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = EntrelaPrimary.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "60 FPS",
                    color = EntrelaPrimaryVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        // Bottom Bar: Gallery import, Big Record Button, Next -> Video Editor
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Time recorded counter
            Text(
                text = String.format("00:%02d / 00:30", recordingDurationSec.toInt()),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Import from gallery
                IconButton(
                    onClick = {
                        viewModel.proceedToVideoEditor(
                            recordedDrawable = R.drawable.reel_bg_travel_1792260334284,
                            durationSec = 15f
                        )
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("recorder_import_btn")
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Import", tint = Color.White)
                }

                // Big Record Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(6.dp)
                        .clickable {
                            isRecording = !isRecording
                        }
                        .testTag("recorder_toggle_record_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                if (isRecording) EntrelaHeartRed
                                else EntrelaPrimary
                            )
                            .padding(if (isRecording) 14.dp else 4.dp)
                    ) {
                        if (isRecording) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                            )
                        }
                    }
                }

                // Proceed to Video Editor
                IconButton(
                    onClick = {
                        val duration = if (recordingDurationSec > 1f) recordingDurationSec else 15f
                        viewModel.proceedToVideoEditor(
                            recordedDrawable = chosenVideoRes,
                            durationSec = duration
                        )
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EntrelaPrimary)
                        .testTag("recorder_next_to_editor_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Next", tint = Color.White)
                }
            }
        }
    }
}
