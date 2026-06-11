package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<User?>

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUser(): User?

    @Query("SELECT * FROM users WHERE isCurrentUser = 0")
    fun getAllOtherUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%'")
    fun searchUsersFlow(query: String): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    // --- Posts ---
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserPostsFlow(userId: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: String): Post?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    // --- Comments ---
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPostFlow(postId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    // --- Reels ---
    @Query("SELECT * FROM reels ORDER BY timestamp DESC")
    fun getAllReelsFlow(): Flow<List<Reel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: Reel)

    @Update
    suspend fun updateReel(reel: Reel)

    // --- Stories (filtered in code or by parameter) ---
    @Query("SELECT * FROM stories WHERE timestamp >= :cutoff ORDER BY timestamp DESC")
    fun getActiveStoriesFlow(cutoff: Long): Flow<List<Story>>

    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStoriesFlow(): Flow<List<Story>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: Story)

    // --- Follows ---
    @Query("SELECT * FROM follows WHERE followerId = :followerId AND followedId = :followedId LIMIT 1")
    suspend fun getFollow(followerId: String, followedId: String): Follow?

    @Query("SELECT COUNT(*) FROM follows WHERE followedId = :userId")
    fun getFollowerCountFlow(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM follows WHERE followerId = :userId")
    fun getFollowingCountFlow(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: Follow)

    @Delete
    suspend fun deleteFollow(follow: Follow)

    @Query("SELECT followedId FROM follows WHERE followerId = :userId")
    suspend fun getFollowingIds(userId: String): List<String>

    @Query("SELECT followedId FROM follows WHERE followerId = :userId")
    fun getFollowingIdsFlow(userId: String): Flow<List<String>>

    // --- Conversations & Messages ---
    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    fun getAllConversationsFlow(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :convoId LIMIT 1")
    suspend fun getConversationById(convoId: String): Conversation?

    @Query("SELECT * FROM conversations WHERE userId = :otherUserId LIMIT 1")
    suspend fun getConversationByOtherUser(otherUserId: String): Conversation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Query("SELECT * FROM messages WHERE conversationId = :convoId ORDER BY timestamp ASC")
    fun getMessagesForConversationFlow(convoId: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
}
