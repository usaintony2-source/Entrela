package com.example.ui.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioSourceType
import com.example.model.AudioTrackItem
import com.example.ui.EntrelaViewModel
import com.example.ui.Screen
import com.example.ui.components.EntrelaGradientButton
import com.example.ui.theme.*

private enum class EditorTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TRIM("Trim & Cut", Icons.Default.ContentCut),
    FILTERS("Filters", Icons.Default.AutoFixHigh),
    TRANSITIONS("Transitions", Icons.Default.Transform),
    AUDIO("Spotify & Audio", Icons.Default.MusicNote)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    viewModel: EntrelaViewModel,
    onBack: () -> Unit
) {
    val draft by viewModel.activeVideoDraft.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val exportedPath by viewModel.exportedFilePath.collectAsState()
    val draftAnalysis by viewModel.currentDraftAnalysis.collectAsState()

    var selectedTab by remember { mutableStateOf(EditorTab.AUDIO) }
    var isPlaying by remember { mutableStateOf(true) }
    var captionText by remember { mutableStateOf("") }
    var showExportModal by remember { mutableStateOf(false) }

    // Waveform live audio playback animation
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wave"
    )

    // Export Dialog with real progress & AI Trend Insights
    if (showExportModal || isExporting || exportedPath != null) {
        ExportVideoDialog(
            isExporting = isExporting,
            progress = exportProgress,
            savedPath = exportedPath,
            caption = captionText,
            analysis = draftAnalysis,
            onCaptionChange = { captionText = it },
            onApplyHashtags = { tags ->
                captionText = if (captionText.isBlank()) tags else "$captionText $tags"
                viewModel.updateDraftAnalysis(captionText)
            },
            onConfirmExport = {
                viewModel.exportAndDownloadVideo(captionText)
            },
            onDismiss = {
                showExportModal = false
                viewModel.clearExportState()
            },
            onGoToReels = {
                showExportModal = false
                viewModel.clearExportState()
                viewModel.navigateTo(Screen.REELS)
            },
            onOpenLeaderboard = {
                showExportModal = false
                viewModel.clearExportState()
                viewModel.openLeaderboard()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EntrelaDarkBg)
            .testTag("video_editor_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("editor_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    IconButton(
                        onClick = { viewModel.openLeaderboard() },
                        modifier = Modifier.testTag("editor_leaderboard_btn")
                    ) {
                        Icon(Icons.Default.Leaderboard, contentDescription = "Trending Sparks Leaderboard", tint = EntrelaGold)
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Entrela Video Studio",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "HD 1080p • 60 FPS",
                        color = EntrelaPrimaryVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Export & Download Button
                Button(
                    onClick = { showExportModal = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EntrelaPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("editor_export_btn")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export HD", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Video Preview Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .clickable { isPlaying = !isPlaying },
                contentAlignment = Alignment.Center
            ) {
                // Background Video Still
                Image(
                    painter = painterResource(id = draft.videoRes),
                    contentDescription = "Video Canvas",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Video Filter Overlay
                if (draft.filter.overlayColorHex != 0x00000000L) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(draft.filter.overlayColorHex))
                    )
                }

                // Active Filter & Transition Badges
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${draft.filter.iconEmoji} ${draft.filter.name}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EntrelaSecondary.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${draft.transition.iconEmoji} ${draft.transition.name}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Play / Pause Overlay Icon
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }

                // Layered Music Pill on Video
                if (draft.audioTrack != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (draft.audioTrack?.sourceType == AudioSourceType.SPOTIFY) "🟢" else "📁", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${draft.audioTrack?.title} • ${draft.audioTrack?.artist}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Duration Indicator
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = String.format("%.1fs / %.1fs", draft.trimEndSec - draft.trimStartSec, draft.originalDurationSec),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Editor Sub-tabs Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EntrelaDarkSurface),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EditorTab.values().forEach { tab ->
                    val isSelected = tab == selectedTab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp)
                            .testTag("editor_tab_${tab.name}")
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) EntrelaPrimary else EntrelaTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tab.label,
                            color = if (isSelected) Color.White else EntrelaTextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(modifier = Modifier.width(24.dp).height(2.dp).background(EntrelaPrimary))
                        }
                    }
                }
            }

            // Tab Panels
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                when (selectedTab) {
                    EditorTab.TRIM -> {
                        TrimAndCutPanel(
                            draft = draft,
                            onTrimChange = { start, end -> viewModel.updateTrim(start, end) }
                        )
                    }

                    EditorTab.FILTERS -> {
                        FiltersPanel(
                            filters = viewModel.videoFilters,
                            activeFilter = draft.filter,
                            onSelect = { viewModel.selectFilter(it) }
                        )
                    }

                    EditorTab.TRANSITIONS -> {
                        TransitionsPanel(
                            transitions = viewModel.videoTransitions,
                            activeTransition = draft.transition,
                            onSelect = { viewModel.selectTransition(it) }
                        )
                    }

                    EditorTab.AUDIO -> {
                        AudioMixerPanel(
                            draft = draft,
                            spotifyTracks = viewModel.spotifyTracks,
                            localTracks = viewModel.localTracks,
                            onSelectTrack = { viewModel.selectAudioTrack(it) },
                            onVolumeChange = { orig, music -> viewModel.updateVolumes(orig, music) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrimAndCutPanel(
    draft: com.example.model.VideoDraft,
    onTrimChange: (Float, Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Trim Video Clip", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Adjust the start and end handles to cut unwanted parts of the video.",
            color = EntrelaTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Waveform Timeline Strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(EntrelaDarkSurfaceVariant)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 24) {
                    val h = ((i * 7 + 13) % 28 + 10).dp
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(h)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (i in 3..18) EntrelaPrimaryVariant else EntrelaTextMuted
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trim Start Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Trim Start", color = EntrelaTextSecondary, fontSize = 12.sp)
            Text(String.format("%.1fs", draft.trimStartSec), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = draft.trimStartSec,
            onValueChange = { newStart ->
                if (newStart < draft.trimEndSec - 1f) {
                    onTrimChange(newStart, draft.trimEndSec)
                }
            },
            valueRange = 0f..draft.originalDurationSec,
            colors = SliderDefaults.colors(thumbColor = EntrelaPrimary, activeTrackColor = EntrelaPrimary)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Trim End Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Trim End", color = EntrelaTextSecondary, fontSize = 12.sp)
            Text(String.format("%.1fs", draft.trimEndSec), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = draft.trimEndSec,
            onValueChange = { newEnd ->
                if (newEnd > draft.trimStartSec + 1f) {
                    onTrimChange(draft.trimStartSec, newEnd)
                }
            },
            valueRange = 0f..draft.originalDurationSec,
            colors = SliderDefaults.colors(thumbColor = EntrelaSecondary, activeTrackColor = EntrelaSecondary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onTrimChange(0f, draft.originalDurationSec) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EntrelaDarkSurfaceVariant),
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset Cut", color = Color.White, fontSize = 12.sp)
            }
            Button(
                onClick = { onTrimChange(draft.trimStartSec, (draft.trimStartSec + 15f).coerceAtMost(draft.originalDurationSec)) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EntrelaPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Text("Snap 15s Reel", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FiltersPanel(
    filters: List<com.example.model.VideoFilterItem>,
    activeFilter: com.example.model.VideoFilterItem,
    onSelect: (com.example.model.VideoFilterItem) -> Unit
) {
    Column {
        Text("Video Filters & Atmosphere", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filters) { f ->
                val isSelected = f.id == activeFilter.id
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) EntrelaPrimary.copy(alpha = 0.25f) else EntrelaDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) EntrelaPrimary else EntrelaDarkBorder
                    ),
                    modifier = Modifier
                        .width(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(f) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(f.iconEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = f.name,
                            color = if (isSelected) Color.White else EntrelaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransitionsPanel(
    transitions: List<com.example.model.VideoTransitionItem>,
    activeTransition: com.example.model.VideoTransitionItem,
    onSelect: (com.example.model.VideoTransitionItem) -> Unit
) {
    Column {
        Text("Video Transitions", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(transitions) { t ->
                val isSelected = t.id == activeTransition.id
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) EntrelaSecondary.copy(alpha = 0.25f) else EntrelaDarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) EntrelaSecondary else EntrelaDarkBorder
                    ),
                    modifier = Modifier
                        .width(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(t) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(t.iconEmoji, fontSize = 26.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = t.name,
                            color = if (isSelected) Color.White else EntrelaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioMixerPanel(
    draft: com.example.model.VideoDraft,
    spotifyTracks: List<AudioTrackItem>,
    localTracks: List<AudioTrackItem>,
    onSelectTrack: (AudioTrackItem?) -> Unit,
    onVolumeChange: (Float, Float) -> Unit
) {
    var audioSubTab by remember { mutableStateOf(0) } // 0 = Spotify API, 1 = Local Audio
    var searchQuery by remember { mutableStateOf("") }

    val filteredTracks = remember(audioSubTab, searchQuery) {
        val baseList = if (audioSubTab == 0) spotifyTracks else localTracks
        if (searchQuery.isBlank()) baseList
        else baseList.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Dual Volume Sliders (Original Voice vs Background Track)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Camera Mic: ${(draft.originalAudioVolume * 100).toInt()}%",
                    color = EntrelaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = draft.originalAudioVolume,
                    onValueChange = { onVolumeChange(it, draft.musicVolume) },
                    colors = SliderDefaults.colors(thumbColor = EntrelaTertiary, activeTrackColor = EntrelaTertiary)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Music Layer: ${(draft.musicVolume * 100).toInt()}%",
                    color = EntrelaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = draft.musicVolume,
                    onValueChange = { onVolumeChange(draft.originalAudioVolume, it) },
                    colors = SliderDefaults.colors(thumbColor = EntrelaPrimary, activeTrackColor = EntrelaPrimary)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Toggle Spotify API vs Local Audio Storage
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(EntrelaDarkSurfaceVariant)
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (audioSubTab == 0) Color(0xFF1DB954).copy(alpha = 0.35f) else Color.Transparent)
                    .clickable { audioSubTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🟢 Spotify API Catalog", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (audioSubTab == 1) EntrelaSecondary.copy(alpha = 0.4f) else Color.Transparent)
                    .clickable { audioSubTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("📁 Local Device Audio", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search in selected source
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (audioSubTab == 0) "Search Spotify tracks, artists..." else "Search device audio tracks...", color = EntrelaTextMuted, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EntrelaTextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (audioSubTab == 0) Color(0xFF1DB954) else EntrelaSecondary,
                unfocusedBorderColor = EntrelaDarkBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = EntrelaDarkSurface,
                unfocusedContainerColor = EntrelaDarkSurface
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Track List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredTracks.forEach { track ->
                val isChosen = draft.audioTrack?.id == track.id
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isChosen) EntrelaPrimary.copy(alpha = 0.2f) else EntrelaDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isChosen) EntrelaPrimary else EntrelaDarkBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isChosen) onSelectTrack(null) else onSelectTrack(track)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(track.coverEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(track.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("${track.artist} • ${track.durationText}", color = EntrelaTextSecondary, fontSize = 11.sp)
                        }
                        if (isChosen) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EntrelaPrimary
                            ) {
                                Text("Layered", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectTrack(track) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+ Layer", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportVideoDialog(
    isExporting: Boolean,
    progress: Float,
    savedPath: String?,
    caption: String,
    analysis: com.example.model.TrendAnalysisResult,
    onCaptionChange: (String) -> Unit,
    onApplyHashtags: (String) -> Unit,
    onConfirmExport: () -> Unit,
    onDismiss: () -> Unit,
    onGoToReels: () -> Unit,
    onOpenLeaderboard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EntrelaDarkSurface,
        title = {
            Text(
                text = if (savedPath != null) "Export & Download Complete! 📥" else "Export Video to Device",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (isExporting) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            color = EntrelaPrimary,
                            trackColor = EntrelaDarkSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when {
                                progress < 0.4f -> "Applying HD color filters & transitions..."
                                progress < 0.8f -> "Blending Spotify audio & audio waveforms..."
                                else -> "Writing 1080p MP4 file to local storage..."
                            },
                            color = EntrelaTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (savedPath != null) {
                    Surface(
                        color = EntrelaDarkSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✅", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Saved to Storage in HD (1080p 60fps):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(savedPath, color = EntrelaOnlineGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Published to your Reels feed and entered into Trending Sparks! ✨", color = EntrelaTextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI Trend Insights panel
                    AITrendInsightsPanel(
                        analysis = analysis,
                        onApplyHashtags = onApplyHashtags,
                        onOpenLeaderboard = onOpenLeaderboard
                    )
                } else {
                    Text(
                        text = "Your video will be encoded in High Definition (1080p 60FPS) and saved directly to device storage.",
                        color = EntrelaTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = caption,
                        onValueChange = onCaptionChange,
                        label = { Text("Reel Caption / Description", color = EntrelaTextMuted) },
                        placeholder = { Text("Check out my new video! #Entrela", color = EntrelaTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EntrelaPrimary,
                            unfocusedBorderColor = EntrelaDarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = EntrelaDarkSurfaceVariant,
                            unfocusedContainerColor = EntrelaDarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (savedPath != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenLeaderboard,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EntrelaGold.copy(alpha = 0.8f))
                    ) {
                        Text("Leaderboard 🔥", color = EntrelaGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onGoToReels,
                        colors = ButtonDefaults.buttonColors(containerColor = EntrelaPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reels 🚀", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else if (!isExporting) {
                Button(
                    onClick = onConfirmExport,
                    colors = ButtonDefaults.buttonColors(containerColor = EntrelaPrimary),
                    modifier = Modifier.testTag("dialog_confirm_export_btn")
                ) {
                    Text("Start HD Download", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isExporting) {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = EntrelaTextSecondary)
                }
            }
        }
    )
}
