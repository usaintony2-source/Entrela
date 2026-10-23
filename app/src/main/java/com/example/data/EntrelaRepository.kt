package com.example.data

import com.example.R
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EntrelaRepository(
    private val firestoreReelsRepo: FirestoreReelsRepository = FirestoreReelsRepository()
) {

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            firestoreReelsRepo.observeReels(initialReels()).collect { list ->
                if (list.isNotEmpty()) {
                    _reels.value = list
                }
            }
        }
    }

    // Current Auth State
    private val _currentUser = MutableStateFlow<UserProfile?>(
        // Default initialized with sample logged-in user for instant usability,
        // or user can log out / sign in
        UserProfile(
            id = "user_me",
            name = "Alex Rivera",
            username = "alex_rivera",
            email = "alex@entrela.app",
            dateOfBirth = "2000-08-15",
            bio = "Chasing sunsets, creating memories, and looking for genuine sparks ✨ Let's explore together!",
            avatarEmoji = "💫",
            relationshipStatus = RelationshipStatus.OPEN_TO_CONNECT,
            interests = listOf("Travel ✈️", "Music 🎵", "Nightlife 🍸", "Photography 📷", "Fitness 🏋️"),
            followersCount = 1420,
            followingCount = 385,
            totalLikes = 28400
        )
    )
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Password reset helper
    private val _pendingResetCode = MutableStateFlow<String?>(null)
    val pendingResetCode: StateFlow<String?> = _pendingResetCode.asStateFlow()

    private val _pendingResetEmail = MutableStateFlow<String?>(null)
    val pendingResetEmail: StateFlow<String?> = _pendingResetEmail.asStateFlow()

    // Reels Feed
    private val _reels = MutableStateFlow<List<ReelItem>>(initialReels())
    val reels: StateFlow<List<ReelItem>> = _reels.asStateFlow()

    // Stories Feed
    private val _stories = MutableStateFlow<List<UserStoryGroup>>(initialStories())
    val stories: StateFlow<List<UserStoryGroup>> = _stories.asStateFlow()

    // Active Conversations
    private val _conversations = MutableStateFlow<List<Conversation>>(initialConversations())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // Matches / Sparks
    private val _matches = MutableStateFlow<List<MatchProfile>>(initialMatches())
    val matches: StateFlow<List<MatchProfile>> = _matches.asStateFlow()

    // --- Authentication Actions ---

    fun login(email: String, pass: String): Boolean {
        if (email.isNotBlank() && pass.length >= 6) {
            _currentUser.value = UserProfile(
                email = email,
                name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                username = email.substringBefore("@").lowercase()
            )
            _isLoggedIn.value = true
            return true
        }
        return false
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        username: String,
        dob: String
    ): Boolean {
        if (name.isNotBlank() && email.contains("@") && pass.length >= 6 && username.isNotBlank()) {
            _currentUser.value = UserProfile(
                name = name,
                email = email,
                username = username,
                dateOfBirth = dob,
                bio = "Hey there! I just joined Entrela. Looking to connect!",
                relationshipStatus = RelationshipStatus.OPEN_TO_CONNECT,
                interests = listOf("Music 🎵", "Travel ✈️", "Coffee ☕")
            )
            _isLoggedIn.value = true
            return true
        }
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun requestPasswordReset(email: String): String {
        _pendingResetEmail.value = email
        val code = (100000..999999).random().toString()
        _pendingResetCode.value = code
        return code
    }

    fun resetPasswordWithCode(code: String, newPass: String): Boolean {
        if (_pendingResetCode.value != null && _pendingResetCode.value == code.trim() && newPass.length >= 6) {
            _pendingResetCode.value = null
            _pendingResetEmail.value = null
            return true
        }
        return false
    }

    fun updateProfile(
        name: String,
        bio: String,
        status: RelationshipStatus,
        interests: List<String>,
        avatarEmoji: String
    ) {
        _currentUser.update { current ->
            current?.copy(
                name = name,
                bio = bio,
                relationshipStatus = status,
                interests = interests,
                avatarEmoji = avatarEmoji
            )
        }
    }

    // --- Reels Actions ---

    fun toggleLikeReel(reelId: String) {
        val currentUserId = _currentUser.value?.id ?: "user_me"
        var isNowLiked = false
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    val newIsLiked = !reel.isLiked
                    isNowLiked = newIsLiked
                    val newCount = if (newIsLiked) reel.likesCount + 1 else (reel.likesCount - 1).coerceAtLeast(0)
                    reel.copy(isLiked = newIsLiked, likesCount = newCount)
                } else reel
            }
        }
        firestoreReelsRepo.toggleLikeReel(reelId, currentUserId, isNowLiked)
    }

    fun toggleBookmarkReel(reelId: String) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) reel.copy(isBookmarked = !reel.isBookmarked) else reel
            }
        }
    }

    fun toggleFollowCreator(creatorId: String) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.creatorId == creatorId) {
                    reel.copy(isFollowingCreator = !reel.isFollowingCreator)
                } else reel
            }
        }
    }

    fun addCommentToReel(reelId: String, text: String) {
        if (text.isBlank()) return
        val user = _currentUser.value ?: return
        val newComment = ReelComment(
            id = "c_${System.currentTimeMillis()}",
            userName = user.name,
            userAvatarEmoji = user.avatarEmoji,
            text = text,
            timestamp = "Just now"
        )
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    reel.copy(
                        commentsCount = reel.commentsCount + 1,
                        comments = listOf(newComment) + reel.comments
                    )
                } else reel
            }
        }
        firestoreReelsRepo.addCommentToReel(reelId, newComment)
    }

    fun toggleLikeComment(reelId: String, commentId: String) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    val updatedComments = reel.comments.map { comment ->
                        if (comment.id == commentId) {
                            val liked = !comment.isLiked
                            comment.copy(
                                isLiked = liked,
                                likesCount = if (liked) comment.likesCount + 1 else (comment.likesCount - 1).coerceAtLeast(0)
                            )
                        } else comment
                    }
                    reel.copy(comments = updatedComments)
                } else reel
            }
        }
    }

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
    ): StoryItem? {
        val user = _currentUser.value ?: return null
        val now = System.currentTimeMillis()
        val newStory = StoryItem(
            id = "story_$now",
            caption = caption.ifBlank {
                if (mediaType == StoryMediaType.VIDEO) "24h Video Moment 🎬 ($filterName)"
                else "24h Photo Spark 📸 ($filterName)"
            },
            drawableRes = drawableRes,
            mediaUri = mediaUri,
            mediaType = mediaType,
            filterName = filterName,
            filterOverlayHex = filterOverlayHex,
            filterIntensity = filterIntensity,
            gradientColors = bgGradient,
            timestamp = "Just now",
            expiresInText = "24h left",
            createdAtMillis = now,
            expiresAtMillis = now + 24 * 60 * 60 * 1000L
        )

        _stories.update { groups ->
            val hasMine = groups.any { it.isCurrentUser }
            if (hasMine) {
                groups.map { group ->
                    if (group.isCurrentUser) {
                        group.copy(
                            stories = listOf(newStory) + group.stories,
                            hasUnseenStories = true
                        )
                    } else group
                }
            } else {
                val myGroup = UserStoryGroup(
                    userId = user.id,
                    userName = "Your Story",
                    userAvatarEmoji = user.avatarEmoji,
                    isCurrentUser = true,
                    hasUnseenStories = true,
                    stories = listOf(newStory)
                )
                listOf(myGroup) + groups
            }
        }
        return newStory
    }

    fun markStoryGroupSeen(userId: String) {
        _stories.update { groups ->
            groups.map { group ->
                if (group.userId == userId) group.copy(hasUnseenStories = false) else group
            }
        }
    }

    // --- Chat Actions ---

    fun sendMessage(
        conversationId: String,
        text: String,
        mediaType: MessageMediaType = MessageMediaType.TEXT,
        mediaRes: Int? = null
    ) {
        val user = _currentUser.value ?: return
        val now = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val msg = ChatMessage(
            id = "m_${System.currentTimeMillis()}",
            senderId = user.id,
            text = text,
            timestamp = now,
            isMine = true,
            mediaType = mediaType,
            mediaAttachmentRes = mediaRes
        )

        _conversations.update { convs ->
            convs.map { conv ->
                if (conv.id == conversationId) {
                    val preview = if (mediaType == MessageMediaType.IMAGE) "📷 Photo" else text
                    conv.copy(
                        lastMessage = preview,
                        lastMessageTime = "Just now",
                        messages = conv.messages + msg
                    )
                } else conv
            }
        }

        // Automated friendly reply from match
        val conv = _conversations.value.find { it.id == conversationId } ?: return
        scope.launch {
            delay(1400)
            val replies = listOf(
                "That's awesome! Loved seeing your latest story ✨",
                "Haha totally agree! Have you checked out the new rooftop lounge?",
                "You have great taste! We should definitely plan something 🥂",
                "I was thinking the exact same thing! How's your week going?",
                "Sending good vibes your way! 💫"
            )
            val replyText = replies.random()
            val replyTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            val replyMsg = ChatMessage(
                id = "m_rep_${System.currentTimeMillis()}",
                senderId = conv.otherUserId,
                text = replyText,
                timestamp = replyTime,
                isMine = false
            )

            _conversations.update { convs ->
                convs.map { c ->
                    if (c.id == conversationId) {
                        c.copy(
                            lastMessage = replyText,
                            lastMessageTime = replyTime,
                            messages = c.messages + replyMsg
                        )
                    } else c
                }
            }
        }
    }

    fun startOrGetConversation(match: MatchProfile): String {
        val existing = _conversations.value.find { it.otherUserId == match.id }
        if (existing != null) return existing.id

        val newId = "conv_${match.id}"
        val newConv = Conversation(
            id = newId,
            otherUserId = match.id,
            otherUserName = match.name,
            otherUserAvatarEmoji = match.avatarEmoji,
            relationshipStatus = match.relationshipStatus.label,
            isOnline = match.isOnline,
            lastMessage = "You matched! Say hi to ${match.name} ✨",
            lastMessageTime = "Just now",
            unreadCount = 0,
            messages = listOf(
                ChatMessage(
                    id = "welcome_${match.id}",
                    senderId = match.id,
                    text = "Hey! Saw we matched on Entrela, love your profile! 😊",
                    timestamp = "Just now",
                    isMine = false
                )
            )
        )
        _conversations.update { listOf(newConv) + it }
        return newId
    }

    // --- Seed Data ---

    private fun initialReels(): List<ReelItem> {
        return listOf(
            ReelItem(
                id = "reel_1",
                creatorId = "c_mateo",
                creatorName = "Mateo & Sofia",
                creatorUsername = "mateo_sofia",
                creatorAvatarEmoji = "💑",
                drawableRes = R.drawable.reel_bg_couple_1792260319176,
                caption = "Late night walks through Tokyo alleyways with my favorite person in the whole universe 🏮✨ Who else loves night adventures?",
                tags = listOf("#TokyoVibes", "#LoveStory", "#EntrelaSparks", "#NightOut"),
                audioTrack = "Neon Romance - Tokyo City Pop",
                artistName = "Lofi Nights",
                likesCount = 14820,
                isLiked = true,
                commentsCount = 284,
                sharesCount = 612,
                isBookmarked = true,
                isFollowingCreator = true,
                comments = listOf(
                    ReelComment("c1", "Elena Gomez", "🌸", "This is pure movie magic! 😍 Where in Shinjuku was this?", "1h ago", 24, true),
                    ReelComment("c2", "Lucas Ray", "⚡", "The lighting in this reel is out of this world!", "3h ago", 9),
                    ReelComment("c3", "Camila Santos", "💖", "Couple goals forever! Keep inspiring us ✨", "5h ago", 15)
                )
            ),
            ReelItem(
                id = "reel_2",
                creatorId = "c_clara",
                creatorName = "Clara Evans",
                creatorUsername = "clara_sky",
                creatorAvatarEmoji = "🍸",
                drawableRes = R.drawable.reel_bg_party_1792260318860,
                caption = "Friday night rooftop sparks! 🥂 NYC skyline never misses when surrounded by the right energy. Tag your weekend crew!",
                tags = listOf("#Nightlife", "#NYC", "#Sparklers", "#EntrelaMoments"),
                audioTrack = "Blinding Lights (Acoustic Club Edit)",
                artistName = "The Neon Collective",
                likesCount = 9430,
                isLiked = false,
                commentsCount = 153,
                sharesCount = 204,
                comments = listOf(
                    ReelComment("c4", "Jordan Vance", "🔥", "That cocktail looks incredible!! Which rooftop is this?", "2h ago", 18),
                    ReelComment("c5", "Maya Lin", "✨", "The vibes are unmatched Clara! 🍾", "4h ago", 6)
                )
            ),
            ReelItem(
                id = "reel_3",
                creatorId = "c_aria",
                creatorName = "Aria Chen",
                creatorUsername = "aria_wander",
                creatorAvatarEmoji = "🌴",
                drawableRes = R.drawable.reel_bg_travel_1792260334284,
                caption = "Golden hour infinity pool in Ubud, Bali 🌅 Taking a deep breath and letting go. Have you ever felt time stop?",
                tags = listOf("#Bali", "#GoldenHour", "#SunsetLovers", "#MindfulWander"),
                audioTrack = "Tropical Sunset Dream",
                artistName = "Serenade Bali",
                likesCount = 31200,
                isLiked = true,
                commentsCount = 512,
                sharesCount = 1200,
                comments = listOf(
                    ReelComment("c6", "David K.", "🌺", "Bali sunsets are truly sacred. Gorgeous shot!", "30m ago", 41, true),
                    ReelComment("c7", "Zoe Taylor", "✨", "Adding this exact pool to my bucket list right now!", "2h ago", 19)
                )
            )
        )
    }

    private fun initialStories(): List<UserStoryGroup> {
        return listOf(
            UserStoryGroup(
                userId = "user_me",
                userName = "Your Story",
                userAvatarEmoji = "💫",
                isCurrentUser = true,
                hasUnseenStories = false,
                isOnline = true,
                isLive = false,
                statusText = "Feeling inspired ✨",
                stories = listOf(
                    StoryItem(
                        id = "s_my1",
                        caption = "Catching morning espresso before heading out ☕",
                        gradientColors = listOf(0xFF8A2BE2, 0xFFFF2D6C),
                        timestamp = "4h ago"
                    )
                )
            ),
            UserStoryGroup(
                userId = "u_mateo",
                userName = "Mateo",
                userAvatarEmoji = "⚡",
                isCurrentUser = false,
                hasUnseenStories = true,
                isOnline = true,
                isLive = true,
                statusText = "In Studio 🎧",
                avatarDrawableRes = R.drawable.reel_bg_couple_1792260319176,
                stories = listOf(
                    StoryItem(
                        id = "s_mat1",
                        caption = "Live from the studio! New acoustics coming together 🎸🎧",
                        gradientColors = listOf(0xFFFF416C, 0xFFFF4B2B),
                        timestamp = "1h ago"
                    ),
                    StoryItem(
                        id = "s_mat2",
                        caption = "Midnight ramen spot discovered in Shibuya 🍜",
                        drawableRes = R.drawable.reel_bg_couple_1792260319176,
                        timestamp = "3h ago"
                    )
                )
            ),
            UserStoryGroup(
                userId = "u_clara",
                userName = "Clara",
                userAvatarEmoji = "🥂",
                isCurrentUser = false,
                hasUnseenStories = true,
                isOnline = true,
                isLive = false,
                statusText = "Rooftop 🍸",
                avatarDrawableRes = R.drawable.reel_bg_party_1792260318860,
                stories = listOf(
                    StoryItem(
                        id = "s_cla1",
                        caption = "Sunset drinks over Manhattan skyline 🌆✨",
                        drawableRes = R.drawable.reel_bg_party_1792260318860,
                        timestamp = "2h ago"
                    )
                )
            ),
            UserStoryGroup(
                userId = "u_aria",
                userName = "Aria",
                userAvatarEmoji = "🌸",
                isCurrentUser = false,
                hasUnseenStories = true,
                isOnline = true,
                isLive = false,
                statusText = "Bali travel 🌿",
                avatarDrawableRes = R.drawable.reel_bg_travel_1792260334284,
                stories = listOf(
                    StoryItem(
                        id = "s_aria1",
                        caption = "Jungle trek sunrise in Bali 🌿🐒",
                        drawableRes = R.drawable.reel_bg_travel_1792260334284,
                        timestamp = "5h ago"
                    )
                )
            ),
            UserStoryGroup(
                userId = "u_elena",
                userName = "Elena",
                userAvatarEmoji = "🎨",
                isCurrentUser = false,
                hasUnseenStories = true,
                isOnline = false,
                isLive = false,
                statusText = "Gallery Prep 🎨",
                stories = listOf(
                    StoryItem(
                        id = "s_ele1",
                        caption = "Finishing this neon acrylic canvas for the weekend gallery show 🎨✨",
                        gradientColors = listOf(0xFF654EA3, 0xFFEAAFC8),
                        timestamp = "6h ago"
                    )
                )
            ),
            UserStoryGroup(
                userId = "u_lucas",
                userName = "Lucas",
                userAvatarEmoji = "🏄",
                isCurrentUser = false,
                hasUnseenStories = false,
                isOnline = true,
                isLive = false,
                statusText = "Catching waves 🌊",
                stories = listOf(
                    StoryItem(
                        id = "s_luc1",
                        caption = "Morning surf session was electric today 🌊",
                        gradientColors = listOf(0xFF00C6FF, 0xFF0072FF),
                        timestamp = "8h ago"
                    )
                )
            )
        )
    }

    private fun initialConversations(): List<Conversation> {
        return listOf(
            Conversation(
                id = "conv_1",
                otherUserId = "u_mateo",
                otherUserName = "Mateo Silva",
                otherUserAvatarEmoji = "⚡",
                relationshipStatus = "Open to connect",
                isOnline = true,
                lastSeenText = "Active now",
                lastMessage = "You: Sounds like a great plan! See you there 🎵",
                lastMessageTime = "12:45 PM",
                unreadCount = 0,
                messages = listOf(
                    ChatMessage("m1", "u_mateo", "Hey Alex! Loved that reel you shared earlier", "12:30 PM", false),
                    ChatMessage("m2", "user_me", "Thanks Mateo! That sunset in Bali was unreal", "12:35 PM", true),
                    ChatMessage("m3", "u_mateo", "Are you going to the rooftop music set this Saturday?", "12:40 PM", false),
                    ChatMessage("m4", "user_me", "Sounds like a great plan! See you there 🎵", "12:45 PM", true)
                )
            ),
            Conversation(
                id = "conv_2",
                otherUserId = "u_clara",
                otherUserName = "Clara Evans",
                otherUserAvatarEmoji = "🥂",
                relationshipStatus = "Single",
                isOnline = true,
                lastSeenText = "Active now",
                lastMessage = "Let's definitely grab matcha this weekend! 🍵",
                lastMessageTime = "10:15 AM",
                unreadCount = 2,
                messages = listOf(
                    ChatMessage("m5", "u_clara", "Hey there! Saw your status with the vinyl collection", "10:05 AM", false),
                    ChatMessage("m6", "u_clara", "Let's definitely grab matcha this weekend! 🍵", "10:15 AM", false)
                )
            ),
            Conversation(
                id = "conv_3",
                otherUserId = "u_aria",
                otherUserName = "Aria Chen",
                otherUserAvatarEmoji = "🌸",
                relationshipStatus = "Dating",
                isOnline = false,
                lastSeenText = "20m ago",
                lastMessage = "Aria sent a photo",
                lastMessageTime = "Yesterday",
                unreadCount = 0,
                messages = listOf(
                    ChatMessage("m7", "u_aria", "Look what I just spotted at the gallery!", "Yesterday", false),
                    ChatMessage("m8", "u_aria", "", "Yesterday", false, MessageMediaType.IMAGE, R.drawable.reel_bg_travel_1792260334284),
                    ChatMessage("m9", "user_me", "Whoa, that color harmony is brilliant!", "Yesterday", true)
                )
            )
        )
    }

    private fun initialMatches(): List<MatchProfile> {
        return listOf(
            MatchProfile(
                id = "match_1",
                name = "Sofia Luna",
                age = 23,
                avatarEmoji = "💫",
                bio = "Indie music enthusiast, film photographer, looking for authentic connections 🌟",
                relationshipStatus = RelationshipStatus.OPEN_TO_CONNECT,
                interests = listOf("Music 🎵", "Photography 📷", "Cinema 🎬"),
                compatibility = 94,
                isOnline = true,
                distanceMiles = 0.8,
                city = "Shibuya, Tokyo"
            ),
            MatchProfile(
                id = "match_2",
                name = "Liam Carter",
                age = 25,
                avatarEmoji = "🚀",
                bio = "Tech builder by day, weekend mountain hiker. Let's talk sci-fi and great espresso ☕",
                relationshipStatus = RelationshipStatus.SINGLE,
                interests = listOf("Tech 💻", "Travel ✈️", "Fitness 🏋️"),
                compatibility = 88,
                isOnline = true,
                distanceMiles = 1.4,
                city = "Roppongi, Tokyo"
            ),
            MatchProfile(
                id = "match_3",
                name = "Camila Cruz",
                age = 22,
                avatarEmoji = "🌺",
                bio = "Art director & dancer. Always up for spontaneous road trips and live acoustic sets 💃",
                relationshipStatus = RelationshipStatus.DATING,
                interests = listOf("Art 🎨", "Nightlife 🍸", "Music 🎵"),
                compatibility = 91,
                isOnline = false,
                distanceMiles = 3.2,
                city = "Ginza, Tokyo"
            ),
            MatchProfile(
                id = "match_4",
                name = "Noah Hayes",
                age = 26,
                avatarEmoji = "🎸",
                bio = "Guitarist, record collector, foodie. Seeking good conversations and concert buddies!",
                relationshipStatus = RelationshipStatus.LOOKING_FOR_FRIENDS,
                interests = listOf("Music 🎵", "Cooking 🍳", "Nightlife 🍸"),
                compatibility = 85,
                isOnline = true,
                distanceMiles = 4.7,
                city = "Shinjuku, Tokyo"
            )
        )
    }

    // --- Location & Nearby Proximity ---
    private val _isLocationEnabled = MutableStateFlow(false)
    val isLocationEnabled: StateFlow<Boolean> = _isLocationEnabled.asStateFlow()

    fun setLocationEnabled(enabled: Boolean) {
        _isLocationEnabled.value = enabled
    }

    // --- Live Streaming Suite ---
    private val _isLiveStreaming = MutableStateFlow(false)
    val isLiveStreaming: StateFlow<Boolean> = _isLiveStreaming.asStateFlow()

    private val _liveViewersCount = MutableStateFlow(1420)
    val liveViewersCount: StateFlow<Int> = _liveViewersCount.asStateFlow()

    private val _liveComments = MutableStateFlow<List<LiveComment>>(initialLiveComments())
    val liveComments: StateFlow<List<LiveComment>> = _liveComments.asStateFlow()

    fun startLiveStream() {
        _isLiveStreaming.value = true
        _liveViewersCount.value = (1200..1600).random()
        startLiveSimulation()
    }

    fun endLiveStream() {
        _isLiveStreaming.value = false
    }

    fun sendLiveComment(text: String, isSpark: Boolean = false) {
        val user = _currentUser.value ?: return
        val newC = LiveComment(
            id = "lc_${System.currentTimeMillis()}",
            userName = user.name,
            userAvatarEmoji = user.avatarEmoji,
            text = text,
            isSpark = isSpark
        )
        _liveComments.update { it + newC }
    }

    private fun startLiveSimulation() {
        scope.launch {
            val randomViewers = listOf(
                Pair("Elena", "This stream quality is crisp!! 🔥"),
                Pair("Mateo", "Love the vibe here Alex 🎶"),
                Pair("Sofia", "Sent 100 Sparks ✨✨✨"),
                Pair("Jordan", "Where are you broadcasting from?"),
                Pair("Aria", "Couple goals forever 💖"),
                Pair("Lucas", "Drop the song ID! 🎧")
            )
            while (_isLiveStreaming.value) {
                delay((3000..5000).random().toLong())
                if (!_isLiveStreaming.value) break
                val (name, text) = randomViewers.random()
                val isSpark = text.contains("Sparks")
                val comment = LiveComment(
                    id = "lc_${System.currentTimeMillis()}",
                    userName = name,
                    userAvatarEmoji = listOf("🌸", "⚡", "✨", "🍸", "🌴", "🔥").random(),
                    text = text,
                    isSpark = isSpark
                )
                _liveComments.update { it + comment }
                _liveViewersCount.update { it + (-5..12).random() }
            }
        }
    }

    private fun initialLiveComments(): List<LiveComment> {
        return listOf(
            LiveComment("1", "Sofia Luna", "💫", "Hey everyone!! Happy Friday! ✨"),
            LiveComment("2", "Mateo Silva", "⚡", "Welcome to the live stream Alex! 🔥"),
            LiveComment("3", "Clara Evans", "🥂", "Sent a Spark! 💫💫", isSpark = true)
        )
    }

    // --- Audio Catalog (Spotify API & Local Storage) ---
    val spotifyTracks: List<AudioTrackItem> = listOf(
        AudioTrackItem("sp_1", "Blinding Lights (Entrela VIP)", "The Weeknd", "3:20", "🎧", AudioSourceType.SPOTIFY, 98),
        AudioTrackItem("sp_2", "Midnight City Romance", "M83", "4:03", "🌃", AudioSourceType.SPOTIFY, 94),
        AudioTrackItem("sp_3", "Espresso Sparks", "Sabrina Carpenter", "2:55", "☕", AudioSourceType.SPOTIFY, 99),
        AudioTrackItem("sp_4", "Levitating (Club Mix)", "Dua Lipa", "3:23", "✨", AudioSourceType.SPOTIFY, 96),
        AudioTrackItem("sp_5", "Starboy (Synthwave Edit)", "The Weeknd, Daft Punk", "3:50", "⚡", AudioSourceType.SPOTIFY, 95),
        AudioTrackItem("sp_6", "Golden Hour Acoustic", "JVKE", "3:29", "🌅", AudioSourceType.SPOTIFY, 93)
    )

    val localTracks: List<AudioTrackItem> = listOf(
        AudioTrackItem("loc_1", "Tokyo Neon Nights.mp3", "Local Device Audio", "2:45", "📁", AudioSourceType.LOCAL_STORAGE),
        AudioTrackItem("loc_2", "Rooftop Chill Guitar.wav", "Voice Recorder / Storage", "1:30", "📁", AudioSourceType.LOCAL_STORAGE),
        AudioTrackItem("loc_3", "Summer Sunset Beat.mp3", "Music Folder", "3:10", "📁", AudioSourceType.LOCAL_STORAGE)
    )

    // --- Video Export & Download ---
    fun saveExportedVideoToStorage(
        draft: VideoDraft,
        caption: String
    ): String {
        val user = _currentUser.value ?: return ""
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "ENTRELA_EXPORT_${timestamp}.mp4"
        val storagePath = "/storage/emulated/0/Movies/Entrela/$fileName"

        // Also add the newly exported video directly into Reels!
        val newReelId = "reel_${System.currentTimeMillis()}"
        val newReel = ReelItem(
            id = newReelId,
            creatorId = user.id,
            creatorName = user.name,
            creatorUsername = user.username,
            creatorAvatarEmoji = user.avatarEmoji,
            drawableRes = draft.videoRes,
            caption = caption.ifBlank { "New high-definition clip created in Entrela Video Studio ✨ #${draft.filter.name} #EntrelaExport" },
            tags = listOf("#EntrelaStudio", "#${draft.filter.name.filter { !it.isWhitespace() }}", "#Sparks"),
            audioTrack = draft.audioTrack?.title ?: "Original Sound (Studio Mix)",
            artistName = draft.audioTrack?.artist ?: user.name,
            likesCount = 1,
            isLiked = true,
            commentsCount = 0,
            isFollowingCreator = true
        )

        _reels.update { listOf(newReel) + it }

        // Real-time AI Video Trend Analysis and ranking injection
        val analysis = com.example.ai.AIVideoTrendAnalyzer.analyzeVideo(draft, caption, _selectedRegion.value)
        val userItem = TrendingLeaderboardItem(
            rank = 1,
            reelId = newReelId,
            title = if (caption.isNotBlank()) caption else "My Entrela Spark ✨ (${draft.filter.name})",
            creatorName = user.name,
            creatorUsername = user.username,
            creatorAvatarEmoji = user.avatarEmoji,
            country = _selectedRegion.value,
            trendScore = analysis.trendScore,
            viewsCount = 19500,
            likesCount = 4100,
            sparkSharesCount = 920,
            audioTrack = draft.audioTrack?.title ?: "Original Sound (Studio Mix)",
            drawableRes = draft.videoRes,
            rankChange = 1
        )

        _allLeaderboardItems.update { current ->
            (listOf(userItem) + current.filter { it.reelId != newReelId })
                .sortedByDescending { it.trendScore }
                .mapIndexed { index, item -> item.copy(rank = index + 1) }
        }

        return storagePath
    }

    // --- AI Video Trend Analysis & Leaderboards ---
    val supportedRegions = com.example.ai.AIVideoTrendAnalyzer.supportedCountries

    private val _selectedRegion = MutableStateFlow(supportedRegions[0]) // Worldwide default
    val selectedRegion: StateFlow<CountryRegion> = _selectedRegion.asStateFlow()

    private val _isWorldwideLeaderboard = MutableStateFlow(true)
    val isWorldwideLeaderboard: StateFlow<Boolean> = _isWorldwideLeaderboard.asStateFlow()

    private val _allLeaderboardItems = MutableStateFlow<List<TrendingLeaderboardItem>>(initialLeaderboardItems())
    val allLeaderboardItems: StateFlow<List<TrendingLeaderboardItem>> = _allLeaderboardItems.asStateFlow()

    fun setLeaderboardFilter(isWorldwide: Boolean, region: CountryRegion) {
        _isWorldwideLeaderboard.value = isWorldwide
        _selectedRegion.value = region
    }

    fun analyzeVideoDraft(draft: VideoDraft, caption: String): TrendAnalysisResult {
        return com.example.ai.AIVideoTrendAnalyzer.analyzeVideo(
            draft = draft,
            caption = caption,
            region = _selectedRegion.value
        )
    }

    private fun initialLeaderboardItems(): List<TrendingLeaderboardItem> {
        val jp = supportedRegions[1]
        val us = supportedRegions[2]
        val gb = supportedRegions[3]
        val ind = supportedRegions[4]
        val br = supportedRegions[5]
        val kr = supportedRegions[6]

        return listOf(
            TrendingLeaderboardItem(
                rank = 1,
                reelId = "reel_1",
                title = "Tokyo Alleyway Sparks & City Walk 🏮",
                creatorName = "Mateo & Sofia",
                creatorUsername = "mateo_sofia",
                creatorAvatarEmoji = "💑",
                country = jp,
                trendScore = 98.7f,
                viewsCount = 428000,
                likesCount = 89400,
                sparkSharesCount = 14200,
                audioTrack = "Neon Romance - Tokyo City Pop",
                drawableRes = R.drawable.reel_bg_couple_1792260319176,
                rankChange = 2
            ),
            TrendingLeaderboardItem(
                rank = 2,
                reelId = "reel_2",
                title = "NYC Rooftop Summer Beats & Sparklers 🥂",
                creatorName = "Clara Evans",
                creatorUsername = "clara_sky",
                creatorAvatarEmoji = "🍸",
                country = us,
                trendScore = 97.4f,
                viewsCount = 312000,
                likesCount = 64200,
                sparkSharesCount = 9800,
                audioTrack = "Blinding Lights (Acoustic Club Edit)",
                drawableRes = R.drawable.reel_bg_party_1792260318860,
                rankChange = 1
            ),
            TrendingLeaderboardItem(
                rank = 3,
                reelId = "reel_3",
                title = "Golden Hour Infinity Pool in Ubud 🌅",
                creatorName = "Aria Chen",
                creatorUsername = "aria_wander",
                creatorAvatarEmoji = "🌴",
                country = jp,
                trendScore = 96.8f,
                viewsCount = 289000,
                likesCount = 58900,
                sparkSharesCount = 8400,
                audioTrack = "Tropical Sunset Dream",
                drawableRes = R.drawable.reel_bg_travel_1792260334284,
                rankChange = -1
            ),
            TrendingLeaderboardItem(
                rank = 4,
                reelId = "reel_4",
                title = "London Underground Acoustic Sessions 🎸",
                creatorName = "Oliver Bennett",
                creatorUsername = "oliver_music",
                creatorAvatarEmoji = "🇬🇧",
                country = gb,
                trendScore = 95.2f,
                viewsCount = 194000,
                likesCount = 41200,
                sparkSharesCount = 6300,
                audioTrack = "Midnight City Romance",
                drawableRes = R.drawable.reel_bg_couple_1792260319176,
                rankChange = 3
            ),
            TrendingLeaderboardItem(
                rank = 5,
                reelId = "reel_5",
                title = "Mumbai Marine Drive Golden Skyline 🌊",
                creatorName = "Priya Sharma",
                creatorUsername = "priya_wander",
                creatorAvatarEmoji = "🇮🇳",
                country = ind,
                trendScore = 94.6f,
                viewsCount = 210000,
                likesCount = 47800,
                sparkSharesCount = 7100,
                audioTrack = "Espresso Sparks",
                drawableRes = R.drawable.reel_bg_travel_1792260334284,
                rankChange = 0
            ),
            TrendingLeaderboardItem(
                rank = 6,
                reelId = "reel_6",
                title = "Rio de Janeiro Sunset Carnival Energy 💃",
                creatorName = "Thiago Silva",
                creatorUsername = "thiago_rio",
                creatorAvatarEmoji = "🇧🇷",
                country = br,
                trendScore = 93.9f,
                viewsCount = 182000,
                likesCount = 39400,
                sparkSharesCount = 5900,
                audioTrack = "Levitating (Club Mix)",
                drawableRes = R.drawable.reel_bg_party_1792260318860,
                rankChange = 2
            ),
            TrendingLeaderboardItem(
                rank = 7,
                reelId = "reel_7",
                title = "Seoul Hongdae Night Street Cypher 🇰🇷✨",
                creatorName = "Min-Jun Park",
                creatorUsername = "minjun_beats",
                creatorAvatarEmoji = "⚡",
                country = kr,
                trendScore = 93.1f,
                viewsCount = 168000,
                likesCount = 35600,
                sparkSharesCount = 5200,
                audioTrack = "Starboy (Synthwave Edit)",
                drawableRes = R.drawable.reel_bg_couple_1792260319176,
                rankChange = -2
            )
        )
    }
}
