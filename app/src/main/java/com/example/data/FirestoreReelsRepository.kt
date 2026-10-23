package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.ReelComment
import com.example.model.ReelItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Repository integrating vertical short video reels feed with Firestore as the backend.
 * Provides real-time snapshot listeners for reels, like updates, and comments.
 */
class FirestoreReelsRepository(
    private val firestore: FirebaseFirestore? = null,
    private val defaultDatabaseId: String? = null
) {
    companion object {
        private const val TAG = "FirestoreReelsRepo"
        const val REELS_COLLECTION = "reels"
        const val COMMENTS_COLLECTION = "comments"
    }

    // Convenience constructor resolving the database ID from string resources
    constructor(context: Context) : this(
        firestore = try {
            val dbId = context.applicationContext.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to initialize Firestore instance with database ID: ${e.message}")
            null
        },
        defaultDatabaseId = try {
            context.applicationContext.getString(R.string.firestore_database_id)
        } catch (e: Exception) {
            null
        }
    )

    private fun getDb(): FirebaseFirestore? {
        if (firestore != null) return firestore
        if (defaultDatabaseId != null) {
            return try {
                FirebaseFirestore.getInstance(defaultDatabaseId)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get Firestore with defaultDatabaseId: ${e.message}")
                null
            }
        }
        return null
    }

    /**
     * Observe real-time list of reels from Firestore.
     * Emits fallback list if Firestore is unavailable or empty.
     */
    fun observeReels(fallbackReels: List<ReelItem>): Flow<List<ReelItem>> = callbackFlow {
        val db = getDb()
        if (db == null) {
            trySend(fallbackReels)
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "user_me"
            listener = db.collection(REELS_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing reels from Firestore: ${error.message}")
                        trySend(fallbackReels)
                        return@addSnapshotListener
                    }

                    if (snapshot == null || snapshot.isEmpty) {
                        // Seed initial reels if collection is empty
                        seedReelsIfEmpty(fallbackReels)
                        trySend(fallbackReels)
                        return@addSnapshotListener
                    }

                    val reelsList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val creatorId = doc.getString("creatorId") ?: "creator"
                            val creatorName = doc.getString("creatorName") ?: "Creator"
                            val creatorUsername = doc.getString("creatorUsername") ?: "creator"
                            val creatorAvatarEmoji = doc.getString("creatorAvatarEmoji") ?: "✨"
                            val caption = doc.getString("caption") ?: ""
                            val tags = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                            val audioTrack = doc.getString("audioTrack") ?: "Original Sound"
                            val artistName = doc.getString("artistName") ?: creatorName
                            val likesCount = (doc.getLong("likesCount") ?: 0L).toInt()
                            val commentsCount = (doc.getLong("commentsCount") ?: 0L).toInt()
                            val sharesCount = (doc.getLong("sharesCount") ?: 0L).toInt()

                            val likedUserIds = (doc.get("likedUserIds") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                            val isLiked = likedUserIds.contains(currentUserId)

                            // Match with existing drawable resources if present
                            val matchingFallback = fallbackReels.find { it.id == id }

                            ReelItem(
                                id = id,
                                creatorId = creatorId,
                                creatorName = creatorName,
                                creatorUsername = creatorUsername,
                                creatorAvatarEmoji = creatorAvatarEmoji,
                                drawableRes = matchingFallback?.drawableRes,
                                caption = caption,
                                tags = tags,
                                audioTrack = audioTrack,
                                artistName = artistName,
                                likesCount = likesCount,
                                isLiked = isLiked,
                                commentsCount = commentsCount,
                                sharesCount = sharesCount,
                                isBookmarked = matchingFallback?.isBookmarked ?: false,
                                isFollowingCreator = matchingFallback?.isFollowingCreator ?: false,
                                comments = matchingFallback?.comments ?: emptyList()
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing reel document ${doc.id}: ${e.message}")
                            null
                        }
                    }

                    if (reelsList.isNotEmpty()) {
                        trySend(reelsList)
                    } else {
                        trySend(fallbackReels)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach reels snapshot listener: ${e.message}")
            trySend(fallbackReels)
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Seeds default reels in Firestore on initial run if collection is empty.
     */
    fun seedReelsIfEmpty(initialReels: List<ReelItem>) {
        val db = getDb() ?: return
        try {
            val batch = db.batch()
            for (reel in initialReels) {
                val docRef = db.collection(REELS_COLLECTION).document(reel.id)
                val reelMap = hashMapOf<String, Any>(
                    "id" to reel.id,
                    "creatorId" to reel.creatorId,
                    "creatorName" to reel.creatorName,
                    "creatorUsername" to reel.creatorUsername,
                    "creatorAvatarEmoji" to reel.creatorAvatarEmoji,
                    "caption" to reel.caption,
                    "tags" to reel.tags,
                    "audioTrack" to reel.audioTrack,
                    "artistName" to reel.artistName,
                    "likesCount" to reel.likesCount,
                    "commentsCount" to reel.commentsCount,
                    "sharesCount" to reel.sharesCount,
                    "likedUserIds" to if (reel.isLiked) listOf("user_me") else emptyList<String>(),
                    "createdAt" to System.currentTimeMillis().toString()
                )
                batch.set(docRef, reelMap, SetOptions.merge())

                // Seed top comments
                for (comment in reel.comments) {
                    val commentDoc = docRef.collection(COMMENTS_COLLECTION).document(comment.id)
                    val commentMap = hashMapOf<String, Any>(
                        "id" to comment.id,
                        "reelId" to reel.id,
                        "userName" to comment.userName,
                        "userAvatarEmoji" to comment.userAvatarEmoji,
                        "text" to comment.text,
                        "timestamp" to comment.timestamp,
                        "likesCount" to comment.likesCount,
                        "createdAt" to System.currentTimeMillis().toString()
                    )
                    batch.set(commentDoc, commentMap, SetOptions.merge())
                }
            }
            batch.commit().addOnFailureListener { e ->
                Log.w(TAG, "Batch seeding reels failed: ${e.message}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Seeding reels failed: ${e.message}")
        }
    }

    /**
     * Update like status and like counter on Firestore for a specific reel.
     */
    fun toggleLikeReel(reelId: String, userId: String, isLiked: Boolean) {
        val db = getDb() ?: return
        try {
            val docRef = db.collection(REELS_COLLECTION).document(reelId)
            val updates = hashMapOf<String, Any>(
                "likesCount" to FieldValue.increment(if (isLiked) 1L else -1L),
                "likedUserIds" to if (isLiked) FieldValue.arrayUnion(userId) else FieldValue.arrayRemove(userId)
            )
            docRef.update(updates).addOnFailureListener { e ->
                Log.w(TAG, "Failed to update like status on Firestore: ${e.message}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception updating like on Firestore: ${e.message}")
        }
    }

    /**
     * Add a comment to a reel in Firestore and increment the comment count.
     */
    fun addCommentToReel(reelId: String, comment: ReelComment) {
        val db = getDb() ?: return
        try {
            val reelRef = db.collection(REELS_COLLECTION).document(reelId)
            val commentRef = reelRef.collection(COMMENTS_COLLECTION).document(comment.id)

            val commentData = hashMapOf<String, Any>(
                "id" to comment.id,
                "reelId" to reelId,
                "userName" to comment.userName,
                "userAvatarEmoji" to comment.userAvatarEmoji,
                "text" to comment.text,
                "timestamp" to comment.timestamp,
                "likesCount" to comment.likesCount,
                "createdAt" to System.currentTimeMillis().toString()
            )

            db.runBatch { batch ->
                batch.set(commentRef, commentData)
                batch.update(reelRef, "commentsCount", FieldValue.increment(1L))
            }.addOnFailureListener { e ->
                Log.w(TAG, "Failed to add comment to Firestore: ${e.message}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception adding comment to Firestore: ${e.message}")
        }
    }

    /**
     * Observes real-time comments stream for a specific reel.
     */
    fun observeComments(reelId: String, fallbackComments: List<ReelComment>): Flow<List<ReelComment>> = callbackFlow {
        val db = getDb()
        if (db == null) {
            trySend(fallbackComments)
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(REELS_COLLECTION).document(reelId)
                .collection(COMMENTS_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing comments: ${error.message}")
                        trySend(fallbackComments)
                        return@addSnapshotListener
                    }

                    if (snapshot == null || snapshot.isEmpty) {
                        trySend(fallbackComments)
                        return@addSnapshotListener
                    }

                    val comments = snapshot.documents.mapNotNull { doc ->
                        try {
                            ReelComment(
                                id = doc.getString("id") ?: doc.id,
                                userName = doc.getString("userName") ?: "User",
                                userAvatarEmoji = doc.getString("userAvatarEmoji") ?: "✨",
                                text = doc.getString("text") ?: "",
                                timestamp = doc.getString("timestamp") ?: "Just now",
                                likesCount = (doc.getLong("likesCount") ?: 0L).toInt()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(comments)
                }
        } catch (e: Exception) {
            trySend(fallbackComments)
        }

        awaitClose {
            listener?.remove()
        }
    }
}
