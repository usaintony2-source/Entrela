package com.example.model

enum class RelationshipStatus(val label: String, val emoji: String) {
    SINGLE("Single", "💫"),
    OPEN_TO_CONNECT("Open to connect", "✨"),
    DATING("Dating", "💌"),
    IN_RELATIONSHIP("In a relationship", "❤️"),
    LOOKING_FOR_FRIENDS("Looking for friends", "🤝"),
    COMPLICATED("It's complicated", "🌀")
}

data class UserProfile(
    val id: String = "u_current",
    val name: String = "Alex Rivera",
    val username: String = "alex_rivera",
    val email: String = "alex@entrela.app",
    val dateOfBirth: String = "2000-05-14",
    val bio: String = "Chasing sunsets, creating memories, and looking for genuine sparks ✨ Let's explore together!",
    val avatarRes: Int? = null,
    val avatarEmoji: String = "💫",
    val relationshipStatus: RelationshipStatus = RelationshipStatus.OPEN_TO_CONNECT,
    val interests: List<String> = listOf("Travel ✈️", "Music 🎵", "Nightlife 🍸", "Photography 📷", "Fitness 🏋️"),
    val followersCount: Int = 1420,
    val followingCount: Int = 385,
    val totalLikes: Int = 28400,
    val isVerified: Boolean = true
)

data class ReelComment(
    val id: String,
    val userName: String,
    val userAvatarEmoji: String,
    val text: String,
    val timestamp: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)

data class ReelItem(
    val id: String,
    val creatorId: String,
    val creatorName: String,
    val creatorUsername: String,
    val creatorAvatarEmoji: String,
    val drawableRes: Int? = null,
    val caption: String,
    val tags: List<String>,
    val audioTrack: String,
    val artistName: String,
    val likesCount: Int,
    val isLiked: Boolean = false,
    val commentsCount: Int,
    val sharesCount: Int = 42,
    val isBookmarked: Boolean = false,
    val isFollowingCreator: Boolean = false,
    val comments: List<ReelComment> = emptyList()
)

enum class StoryMediaType(val label: String) {
    PHOTO("Photo"),
    VIDEO("Video"),
    TEXT_GRADIENT("Text")
}

data class StoryFilterPreset(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val overlayColorHex: Long = 0x00000000,
    val secondaryTintHex: Long = 0x00000000,
    val description: String = ""
)

data class StoryItem(
    val id: String,
    val caption: String,
    val drawableRes: Int? = null,
    val mediaUri: String? = null,
    val mediaType: StoryMediaType = StoryMediaType.PHOTO,
    val filterName: String = "Original",
    val filterOverlayHex: Long = 0x00000000,
    val filterIntensity: Float = 1.0f,
    val gradientColors: List<Long> = listOf(0xFF8A2BE2, 0xFFFF2D6C),
    val timestamp: String = "2h ago",
    val expiresInText: String = "24h left",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val expiresAtMillis: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
    val isViewed: Boolean = false
)

data class UserStoryGroup(
    val userId: String,
    val userName: String,
    val userAvatarEmoji: String,
    val isCurrentUser: Boolean = false,
    val hasUnseenStories: Boolean = true,
    val stories: List<StoryItem>,
    val isOnline: Boolean = true,
    val isLive: Boolean = false,
    val statusText: String? = null,
    val avatarDrawableRes: Int? = null
)

enum class MessageMediaType {
    TEXT,
    IMAGE,
    AUDIO
}

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: String,
    val isMine: Boolean,
    val mediaType: MessageMediaType = MessageMediaType.TEXT,
    val mediaAttachmentRes: Int? = null,
    val audioDurationSec: Int = 0
)

data class Conversation(
    val id: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserAvatarEmoji: String,
    val relationshipStatus: String,
    val isOnline: Boolean,
    val lastSeenText: String = "Active now",
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val messages: List<ChatMessage> = emptyList()
)

data class MatchProfile(
    val id: String,
    val name: String,
    val age: Int,
    val avatarEmoji: String,
    val bio: String,
    val relationshipStatus: RelationshipStatus,
    val interests: List<String>,
    val compatibility: Int,
    val isOnline: Boolean,
    val distanceMiles: Double = 1.2,
    val city: String = "Tokyo, Shibuya"
)

// Live Stream Models
data class LiveComment(
    val id: String,
    val userName: String,
    val userAvatarEmoji: String,
    val text: String,
    val timestamp: String = "now",
    val isSpark: Boolean = false
)

// Video Suite Models
enum class AudioSourceType {
    SPOTIFY,
    LOCAL_STORAGE
}

data class AudioTrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val durationText: String,
    val coverEmoji: String = "🎵",
    val sourceType: AudioSourceType = AudioSourceType.SPOTIFY,
    val spotifyPopularity: Int = 92
)

data class VideoFilterItem(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val overlayColorHex: Long = 0x00000000
)

data class VideoTransitionItem(
    val id: String,
    val name: String,
    val iconEmoji: String
)

data class VideoDraft(
    val id: String = "draft_1",
    val videoRes: Int,
    val originalDurationSec: Float = 15.0f,
    val trimStartSec: Float = 0.0f,
    val trimEndSec: Float = 15.0f,
    val filter: VideoFilterItem,
    val transition: VideoTransitionItem,
    val audioTrack: AudioTrackItem? = null,
    val originalAudioVolume: Float = 0.7f,
    val musicVolume: Float = 0.9f
)

// AI Trend Analysis & Leaderboards
data class CountryRegion(
    val code: String,
    val name: String,
    val flagEmoji: String
)

data class TrendAnalysisResult(
    val trendScore: Float,
    val viralGrade: String,
    val watchTimeRetentionPct: Int,
    val estimatedLoopRate: Float,
    val sparkShareVelocity: String,
    val hookStrengthScore: Int,
    val audioSyncScore: Int,
    val aiSuggestions: List<String>,
    val recommendedHashtags: List<String>,
    val recommendedAudios: List<String>,
    val trendingFilterMatch: String,
    val topAudioTrack: String = "The Weeknd - Blinding Lights (Entrela VIP)",
    val topFilterName: String = "Cyberpunk Neon"
)

data class TrendingLeaderboardItem(
    val rank: Int,
    val reelId: String,
    val title: String,
    val creatorName: String,
    val creatorUsername: String,
    val creatorAvatarEmoji: String,
    val country: CountryRegion,
    val trendScore: Float,
    val viewsCount: Int,
    val likesCount: Int,
    val sparkSharesCount: Int,
    val audioTrack: String,
    val drawableRes: Int? = null,
    val rankChange: Int = 0 // +2, 0, -1
)

