package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.EntrelaRepository
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    PROFILE_SETUP,
    REELS,
    EXPLORE_SPARKS,
    CHATS,
    DIRECT_CHAT,
    PROFILE,
    STORY_VIEWER,
    LIVE_STREAM,
    VIDEO_RECORDER,
    VIDEO_EDITOR,
    TRENDING_LEADERBOARD
}

class EntrelaViewModel(
    private val repository: EntrelaRepository = EntrelaRepository()
) : ViewModel() {

    // Current navigation destination
    private val _currentScreen = MutableStateFlow(Screen.REELS)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation stack for back button handling
    private val navStack = mutableListOf(Screen.REELS)

    // Current active chat conversation ID
    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    // Current active story viewing group
    private val _activeStoryGroup = MutableStateFlow<UserStoryGroup?>(null)
    val activeStoryGroup: StateFlow<UserStoryGroup?> = _activeStoryGroup.asStateFlow()

    // Sheet / Dialog states
    private val _activeCommentsReelId = MutableStateFlow<String?>(null)
    val activeCommentsReelId: StateFlow<String?> = _activeCommentsReelId.asStateFlow()

    private val _activeShareReel = MutableStateFlow<ReelItem?>(null)
    val activeShareReel: StateFlow<ReelItem?> = _activeShareReel.asStateFlow()

    private val _isCreateStoryOpen = MutableStateFlow(false)
    val isCreateStoryOpen: StateFlow<Boolean> = _isCreateStoryOpen.asStateFlow()

    private val _isEditProfileOpen = MutableStateFlow(false)
    val isEditProfileOpen: StateFlow<Boolean> = _isEditProfileOpen.asStateFlow()

    // Notification toast / alert message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Repository flows
    val currentUser = repository.currentUser
    val isLoggedIn = repository.isLoggedIn
    val reels = repository.reels
    val stories = repository.stories
    val conversations = repository.conversations
    val matches = repository.matches
    val isLocationEnabled = repository.isLocationEnabled
    val isLiveStreaming = repository.isLiveStreaming
    val liveViewersCount = repository.liveViewersCount
    val liveComments = repository.liveComments
    val spotifyTracks = repository.spotifyTracks
    val localTracks = repository.localTracks
    val pendingResetCode = repository.pendingResetCode
    val pendingResetEmail = repository.pendingResetEmail

    // Video Suite Preset Lists
    val videoFilters = listOf(
        VideoFilterItem("f0", "Normal", "✨", 0x00000000),
        VideoFilterItem("f1", "Cyberpunk Neon", "🌆", 0x28FF2D6C),
        VideoFilterItem("f2", "Golden Sunset", "🌅", 0x25FFA000),
        VideoFilterItem("f3", "Tokyo Glow", "🏮", 0x288A2BE2),
        VideoFilterItem("f4", "Noir Cinema", "🎬", 0x40000000),
        VideoFilterItem("f5", "Romance Soft", "💖", 0x25FF6584)
    )

    val videoTransitions = listOf(
        VideoTransitionItem("t0", "Cut", "✂️"),
        VideoTransitionItem("t1", "Cross-Fade", "✨"),
        VideoTransitionItem("t2", "Zoom Blur", "🔍"),
        VideoTransitionItem("t3", "Glitch Neon", "⚡"),
        VideoTransitionItem("t4", "Flash Light", "💡")
    )

    // Video Draft State for Editing
    private val _activeVideoDraft = MutableStateFlow(
        VideoDraft(
            videoRes = R.drawable.reel_bg_party_1792260318860,
            originalDurationSec = 15.0f,
            trimStartSec = 0.0f,
            trimEndSec = 15.0f,
            filter = videoFilters[1], // Cyberpunk Neon
            transition = videoTransitions[1],
            audioTrack = repository.spotifyTracks.first(),
            originalAudioVolume = 0.6f,
            musicVolume = 0.9f
        )
    )
    val activeVideoDraft: StateFlow<VideoDraft> = _activeVideoDraft.asStateFlow()

    // Video Exporting State
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    private val _exportedFilePath = MutableStateFlow<String?>(null)
    val exportedFilePath: StateFlow<String?> = _exportedFilePath.asStateFlow()

    // AI Trend Analysis & Leaderboards
    val supportedRegions = repository.supportedRegions
    val selectedRegion = repository.selectedRegion
    val isWorldwideLeaderboard = repository.isWorldwideLeaderboard
    val allLeaderboardItems = repository.allLeaderboardItems

    private val _currentDraftAnalysis = MutableStateFlow(
        repository.analyzeVideoDraft(_activeVideoDraft.value, "#EntrelaStudio #ViralReel")
    )
    val currentDraftAnalysis: StateFlow<TrendAnalysisResult> = _currentDraftAnalysis.asStateFlow()

    fun updateDraftAnalysis(caption: String) {
        _currentDraftAnalysis.value = repository.analyzeVideoDraft(_activeVideoDraft.value, caption)
    }

    fun setLeaderboardFilter(isWorldwide: Boolean, region: CountryRegion) {
        repository.setLeaderboardFilter(isWorldwide, region)
    }

    fun openLeaderboard() {
        navigateTo(Screen.TRENDING_LEADERBOARD)
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // --- Navigation ---
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            navStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.lastIndex)
            _currentScreen.value = navStack.last()
            return true
        }
        return false
    }

    fun openDirectChat(conversationId: String) {
        _activeConversationId.value = conversationId
        navigateTo(Screen.DIRECT_CHAT)
    }

    fun openStoryViewer(group: UserStoryGroup) {
        _activeStoryGroup.value = group
        repository.markStoryGroupSeen(group.userId)
        navigateTo(Screen.STORY_VIEWER)
    }

    fun openComments(reelId: String) {
        _activeCommentsReelId.value = reelId
    }

    fun closeComments() {
        _activeCommentsReelId.value = null
    }

    fun openShare(reel: ReelItem) {
        _activeShareReel.value = reel
    }

    fun closeShare() {
        _activeShareReel.value = null
    }

    fun setCreateStoryOpen(open: Boolean) {
        _isCreateStoryOpen.value = open
    }

    fun setEditProfileOpen(open: Boolean) {
        _isEditProfileOpen.value = open
    }

    // --- Location / Nearby Sparks ---
    fun setLocationPermissionGranted(granted: Boolean) {
        repository.setLocationEnabled(granted)
        if (granted) {
            showToast("GPS Active: Nearby Sparks & Distance enabled 📍")
        }
    }

    // --- Live Stream Suite ---
    fun startLiveStream() {
        repository.startLiveStream()
        navigateTo(Screen.LIVE_STREAM)
    }

    fun endLiveStream() {
        repository.endLiveStream()
        navigateBack()
        showToast("Live broadcast ended. Thanks for streaming! ✨")
    }

    fun sendLiveComment(text: String, isSpark: Boolean = false) {
        repository.sendLiveComment(text, isSpark)
    }

    // --- Video Recorder & Editor ---
    fun openVideoRecorder() {
        navigateTo(Screen.VIDEO_RECORDER)
    }

    fun proceedToVideoEditor(recordedDrawable: Int = R.drawable.reel_bg_party_1792260318860, durationSec: Float = 15f) {
        _activeVideoDraft.update {
            it.copy(
                videoRes = recordedDrawable,
                originalDurationSec = durationSec,
                trimStartSec = 0f,
                trimEndSec = durationSec
            )
        }
        navigateTo(Screen.VIDEO_EDITOR)
    }

    fun updateTrim(startSec: Float, endSec: Float) {
        _activeVideoDraft.update {
            it.copy(trimStartSec = startSec, trimEndSec = endSec)
        }
    }

    fun selectFilter(filter: VideoFilterItem) {
        _activeVideoDraft.update { it.copy(filter = filter) }
    }

    fun selectTransition(transition: VideoTransitionItem) {
        _activeVideoDraft.update { it.copy(transition = transition) }
    }

    fun selectAudioTrack(track: AudioTrackItem?) {
        _activeVideoDraft.update { it.copy(audioTrack = track) }
        if (track != null) {
            showToast("Layered audio: ${track.title} 🎵")
        }
    }

    fun updateVolumes(originalVolume: Float, musicVolume: Float) {
        _activeVideoDraft.update {
            it.copy(originalAudioVolume = originalVolume, musicVolume = musicVolume)
        }
    }

    fun exportAndDownloadVideo(caption: String) {
        viewModelScope.launch {
            _isExporting.value = true
            _exportProgress.value = 0.1f
            delay(400)
            _exportProgress.value = 0.35f
            delay(500)
            _exportProgress.value = 0.70f
            delay(450)
            _exportProgress.value = 0.95f
            delay(300)
            _exportProgress.value = 1.0f

            val savedPath = repository.saveExportedVideoToStorage(_activeVideoDraft.value, caption)
            _exportedFilePath.value = savedPath
            _currentDraftAnalysis.value = repository.analyzeVideoDraft(_activeVideoDraft.value, caption)
            _isExporting.value = false
            showToast("Video downloaded in HD & posted to Reels! 📥✨")
        }
    }

    fun clearExportState() {
        _exportedFilePath.value = null
    }

    // --- Authentication Actions ---
    fun login(email: String, pass: String): Boolean {
        val success = repository.login(email, pass)
        if (success) {
            navStack.clear()
            navStack.add(Screen.REELS)
            _currentScreen.value = Screen.REELS
            showToast("Welcome back to Entrela ✨")
        }
        return success
    }

    fun register(name: String, email: String, pass: String, username: String, dob: String): Boolean {
        val success = repository.register(name, email, pass, username, dob)
        if (success) {
            navigateTo(Screen.PROFILE_SETUP)
        }
        return success
    }

    fun completeProfileSetup(
        bio: String,
        status: RelationshipStatus,
        interests: List<String>,
        avatarEmoji: String
    ) {
        val name = currentUser.value?.name ?: "Entrela User"
        repository.updateProfile(name, bio, status, interests, avatarEmoji)
        navStack.clear()
        navStack.add(Screen.REELS)
        _currentScreen.value = Screen.REELS
        showToast("Profile ready! Welcome to Entrela ✨")
    }

    fun requestPasswordReset(email: String): String {
        val code = repository.requestPasswordReset(email)
        showToast("Reset code sent! Hint: $code")
        return code
    }

    fun resetPasswordWithCode(code: String, newPass: String): Boolean {
        val success = repository.resetPasswordWithCode(code, newPass)
        if (success) {
            showToast("Password reset successfully! Please sign in.")
            navigateTo(Screen.LOGIN)
        }
        return success
    }

    fun logout() {
        repository.logout()
        navStack.clear()
        navStack.add(Screen.LOGIN)
        _currentScreen.value = Screen.LOGIN
        showToast("Logged out of Entrela")
    }

    // --- Reels Actions ---
    fun toggleLikeReel(reelId: String) = repository.toggleLikeReel(reelId)
    fun toggleBookmarkReel(reelId: String) = repository.toggleBookmarkReel(reelId)
    fun toggleFollowCreator(creatorId: String) = repository.toggleFollowCreator(creatorId)
    fun addCommentToReel(reelId: String, text: String) = repository.addCommentToReel(reelId, text)
    fun toggleLikeComment(reelId: String, commentId: String) = repository.toggleLikeComment(reelId, commentId)

    // --- Stories Actions ---
    fun addStory(
        caption: String,
        bgGradient: List<Long> = listOf(0xFFFF2D6C, 0xFF8A2BE2),
        drawableRes: Int? = null,
        mediaUri: String? = null,
        mediaType: StoryMediaType = StoryMediaType.PHOTO,
        filterName: String = "Original",
        filterOverlayHex: Long = 0x00000000,
        filterIntensity: Float = 1.0f
    ) {
        repository.addStory(
            caption = caption,
            bgGradient = bgGradient,
            drawableRes = drawableRes,
            mediaUri = mediaUri,
            mediaType = mediaType,
            filterName = filterName,
            filterOverlayHex = filterOverlayHex,
            filterIntensity = filterIntensity
        )
        _isCreateStoryOpen.value = false
        showToast("24h ${mediaType.label} Story posted with $filterName filter! 🕒")
    }

    // --- Chat Actions ---
    fun sendMessage(
        conversationId: String,
        text: String,
        mediaType: MessageMediaType = MessageMediaType.TEXT,
        mediaRes: Int? = null
    ) {
        repository.sendMessage(conversationId, text, mediaType, mediaRes)
    }

    fun startChatWithMatch(match: MatchProfile) {
        val convId = repository.startOrGetConversation(match)
        openDirectChat(convId)
    }

    // --- Profile Actions ---
    fun updateProfile(
        name: String,
        bio: String,
        status: RelationshipStatus,
        interests: List<String>,
        avatarEmoji: String
    ) {
        repository.updateProfile(name, bio, status, interests, avatarEmoji)
        _isEditProfileOpen.value = false
        showToast("Profile updated successfully ✨")
    }
}
