package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class SocialRepository(private val socialDao: SocialDao) {

    // --- Current User and Profile Flow ---
    val currentUser: Flow<User?> = socialDao.getCurrentUserFlow()
    val otherUsers: Flow<List<User>> = socialDao.getAllOtherUsersFlow()

    fun searchUsers(query: String): Flow<List<User>> = socialDao.searchUsersFlow(query)

    suspend fun getCurrentUserSync(): User? = socialDao.getCurrentUser()

    suspend fun updateCurrentUser(user: User) {
        socialDao.updateUser(user)
    }

    // --- Posts ---
    val allPosts: Flow<List<Post>> = socialDao.getAllPostsFlow()

    fun getUserPosts(userId: String): Flow<List<Post>> = socialDao.getUserPostsFlow(userId)

    suspend fun createPost(text: String, imageUrl: String? = null, location: String? = null, lat: Double? = null, lng: Double? = null) {
        val current = getCurrentUserSync() ?: return
        val newPost = Post(
            id = "post_${UUID.randomUUID()}",
            userId = current.id,
            authorName = current.name,
            authorAvatar = current.avatarUrl,
            text = text,
            imageUrl = imageUrl,
            location = location,
            latitude = lat,
            longitude = lng,
            timestamp = System.currentTimeMillis()
        )
        socialDao.insertPost(newPost)
    }

    suspend fun toggleLikePost(postId: String) {
        val post = socialDao.getPostById(postId) ?: return
        val updatedPost = post.copy(
            isLikedByMe = !post.isLikedByMe,
            likesCount = if (post.isLikedByMe) post.likesCount - 1 else post.likesCount + 1
        )
        socialDao.updatePost(updatedPost)
    }

    // --- Comments ---
    fun getCommentsForPost(postId: String): Flow<List<Comment>> = socialDao.getCommentsForPostFlow(postId)

    suspend fun addComment(postId: String, text: String) {
        val current = getCurrentUserSync() ?: return
        val comment = Comment(
            id = "comment_${UUID.randomUUID()}",
            postId = postId,
            userId = current.id,
            authorName = current.name,
            authorAvatar = current.avatarUrl,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        socialDao.insertComment(comment)

        // Increment comments count on Post
        val post = socialDao.getPostById(postId)
        if (post != null) {
            socialDao.updatePost(post.copy(commentsCount = post.commentsCount + 1))
        }
    }

    // --- Reels ---
    val allReels: Flow<List<Reel>> = socialDao.getAllReelsFlow()

    suspend fun createReel(caption: String, bgIndex: Int, music: String) {
        val current = getCurrentUserSync() ?: return
        val newReel = Reel(
            id = "reel_${UUID.randomUUID()}",
            userId = current.id,
            authorName = current.name,
            authorAvatar = current.avatarUrl,
            caption = caption,
            bgGradientIndex = bgIndex,
            musicTitle = music,
            timestamp = System.currentTimeMillis()
        )
        socialDao.insertReel(newReel)
    }

    suspend fun toggleLikeReel(reelId: String) {
        // Since we insert a reel, we can update its state. It works similarly to post likes. We query all to find the reel.
        // For simplicity, let's allow toggling likes by updating the item in database if it exists:
        val reels = socialDao.getAllReelsFlow().firstOrNull() ?: return
        val target = reels.find { it.id == reelId } ?: return
        val updated = target.copy(
            isLikedByMe = !target.isLikedByMe,
            likesCount = if (target.isLikedByMe) target.likesCount - 1 else target.likesCount + 1
        )
        socialDao.insertReel(updated) // REPLACE
    }

    // --- Stories ---
    // Only return stories posted in the last 24 hours (86,400,000 milliseconds)
    fun getActiveStories(): Flow<List<Story>> {
        val cutoff = System.currentTimeMillis() - 86400000L
        return socialDao.getActiveStoriesFlow(cutoff)
    }

    val allStoriesRaw: Flow<List<Story>> = socialDao.getAllStoriesFlow()

    suspend fun createStory(text: String, bgIndex: Int, location: String? = null, lat: Double? = null, lng: Double? = null) {
        val current = getCurrentUserSync() ?: return
        val newStory = Story(
            id = "story_${UUID.randomUUID()}",
            userId = current.id,
            authorName = current.name,
            authorAvatar = current.avatarUrl,
            text = text,
            bgGradientIndex = bgIndex,
            locationLabel = location,
            latitude = lat,
            longitude = lng,
            timestamp = System.currentTimeMillis()
        )
        socialDao.insertStory(newStory)
    }

    // --- Follow Flow ---
    fun getFollowersCount(userId: String): Flow<Int> = socialDao.getFollowerCountFlow(userId)
    fun getFollowingCount(userId: String): Flow<Int> = socialDao.getFollowingCountFlow(userId)
    fun getFollowingIds(userId: String): Flow<List<String>> = socialDao.getFollowingIdsFlow(userId)

    suspend fun toggleFollow(targetUserId: String) {
        val current = getCurrentUserSync() ?: return
        if (current.id == targetUserId) return
        val followKey = "${current.id}_$targetUserId"
        val existing = socialDao.getFollow(current.id, targetUserId)
        if (existing != null) {
            socialDao.deleteFollow(existing)
        } else {
            socialDao.insertFollow(Follow(id = followKey, followerId = current.id, followedId = targetUserId))
        }
    }

    suspend fun isFollowing(targetUserId: String): Boolean {
        val current = getCurrentUserSync() ?: return false
        return socialDao.getFollow(current.id, targetUserId) != null
    }

    // --- Chat & Conversations ---
    val allConversations: Flow<List<Conversation>> = socialDao.getAllConversationsFlow()

    fun getMessages(conversationId: String): Flow<List<Message>> = socialDao.getMessagesForConversationFlow(conversationId)

    suspend fun sendMessage(conversationId: String, text: String, locationLabel: String? = null, lat: Double? = null, lng: Double? = null) {
        val current = getCurrentUserSync() ?: return
        val convo = socialDao.getConversationById(conversationId) ?: return
        
        val message = Message(
            id = "msg_${UUID.randomUUID()}",
            conversationId = conversationId,
            senderId = current.id,
            receiverId = convo.userId,
            text = text,
            timestamp = System.currentTimeMillis(),
            locationLabel = locationLabel,
            latitude = lat,
            longitude = lng
        )
        socialDao.insertMessage(message)

        // Update conversation summary
        val updatedConvo = convo.copy(
            lastMessage = text,
            lastMessageTime = System.currentTimeMillis()
        )
        socialDao.insertConversation(updatedConvo)

        // Simulate interactive automated partner response in 1.5 seconds!
        simulateAutomatedReply(conversationId, convo.userId, convo.userName, text)
    }

    private suspend fun simulateAutomatedReply(convoId: String, otherUserId: String, otherName: String, userText: String) {
        kotlinx.coroutines.delay(1200)
        
        val replyText = when {
            userText.contains("hello", true) || userText.contains("hi", true) || userText.contains("hey", true) -> {
                "Hey! Sajiur! Great to hear from you. Have you checked out the Stories map with Location sharing?"
            }
            userText.contains("location", true) || userText.contains("map", true) || userText.contains("where", true) -> {
                "I am currently checked-in! Check my status on the Profile page or the Location radar."
            }
            userText.contains("reel", true) || userText.contains("video", true) -> {
                "Haha cool! I just posted another short Reel today, hope you like it!"
            }
            else -> {
                "That soundsawesome! SR Inbox is fully working. Let's catch up later today."
            }
        }

        val partnerReply = Message(
            id = "msg_${UUID.randomUUID()}",
            conversationId = convoId,
            senderId = otherUserId,
            receiverId = "user_me",
            text = replyText,
            timestamp = System.currentTimeMillis()
        )
        socialDao.insertMessage(partnerReply)

        val convo = socialDao.getConversationById(convoId)
        if (convo != null) {
            socialDao.insertConversation(convo.copy(
                lastMessage = replyText,
                lastMessageTime = System.currentTimeMillis(),
                unreadCount = convo.unreadCount + 1
            ))
        }
    }

    suspend fun startChatWithUser(otherUser: User): String {
        val current = getCurrentUserSync() ?: throw IllegalStateException("Current user not logged in")
        val existing = socialDao.getConversationByOtherUser(otherUser.id)
        if (existing != null) {
            return existing.id
        }

        val convoId = "convo_${UUID.randomUUID()}"
        val newConvo = Conversation(
            id = convoId,
            userId = otherUser.id,
            userName = otherUser.name,
            userAvatar = otherUser.avatarUrl,
            lastMessage = "Started a conversation",
            lastMessageTime = System.currentTimeMillis()
        )
        socialDao.insertConversation(newConvo)
        return convoId
    }

    suspend fun clearUnreads(convoId: String) {
        val convo = socialDao.getConversationById(convoId) ?: return
        socialDao.insertConversation(convo.copy(unreadCount = 0))
    }

    // --- Prepopulate Initial Realistic Seed Data ---
    suspend fun preseedDataIfEmpty() {
        val current = socialDao.getCurrentUser()
        if (current != null) return // Already seeded

        // 1. Current user Sajiur
        val me = User(
            id = "user_me",
            name = "Sajiur Rahman",
            username = "sajiur_inbox",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
            bio = "Co-founder of SR Inbox. Let's connect, share local moments, and swipe beautiful reels! 🚀✨",
            locationLabel = "Dhaka, Bangladesh",
            latitude = 23.8103,
            longitude = 90.4125,
            isCurrentUser = true
        )
        socialDao.insertUser(me)

        // 2. Interactive Friend Users
        val f1 = User(
            id = "user_tariq",
            name = "Tariqul Islam",
            username = "tariq_dev",
            avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=150&q=80",
            bio = "Crafting high-quality software architectures. Android & Kotlin expert. Reach out!",
            locationLabel = "Gulshan Radar, Dhaka",
            latitude = 23.7925,
            longitude = 90.4078
        )
        val f2 = User(
            id = "user_rez",
            name = "Rezwana Chowdhury",
            username = "rez_travels",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
            bio = "Travel Photographer 📷. Capturing the exquisite beauty of Sylhet tea estates and beyond.",
            locationLabel = "Srimangal, Sylhet",
            latitude = 24.3065,
            longitude = 91.7295
        )
        val f3 = User(
            id = "user_nahid",
            name = "Nahid Hasan",
            username = "nahid_sports",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
            bio = "Cricket and fitness enthusiast. Training in Chittagong. Hard work beats talent!",
            locationLabel = "Chittagong Stadium",
            latitude = 22.3569,
            longitude = 91.7832
        )
        val f4 = User(
            id = "user_farhana",
            name = "Farhana Ahmed",
            username = "farhana_foodie",
            avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&q=80",
            bio = "Exploring tasty authentic local cuisines in Rajshahi. Food is my true love. 🥭🍲",
            locationLabel = "Shaheb Bazar, Rajshahi",
            latitude = 24.3745,
            longitude = 88.6042
        )

        socialDao.insertUser(f1)
        socialDao.insertUser(f2)
        socialDao.insertUser(f3)
        socialDao.insertUser(f4)

        // 3. Follow connections (Mutual or follow-by-me)
        socialDao.insertFollow(Follow("user_me_user_tariq", "user_me", "user_tariq"))
        socialDao.insertFollow(Follow("user_me_user_rez", "user_me", "user_rez"))
        socialDao.insertFollow(Follow("user_tariq_user_me", "user_tariq", "user_me"))
        socialDao.insertFollow(Follow("user_rez_user_me", "user_rez", "user_me"))
        socialDao.insertFollow(Follow("user_nahid_user_me", "user_nahid", "user_me"))

        // 4. Seeding Posts
        val post1 = Post(
            id = "post_1",
            userId = "user_tariq",
            authorName = "Tariqul Islam",
            authorAvatar = f1.avatarUrl,
            text = "Developing the Jetpack Compose navigation architecture for the upcoming SR Inbox release. The transition animations with shared elements are extremely responsive! 🎯💯 #android #compose",
            location = "Gulshan, Dhaka",
            latitude = 23.7925,
            longitude = 90.4078,
            timestamp = System.currentTimeMillis() - 3600000 * 2, // 2 hours ago
            likesCount = 14,
            isLikedByMe = true,
            commentsCount = 2
        )
        val post2 = Post(
            id = "post_2",
            userId = "user_rez",
            authorName = "Rezwana Chowdhury",
            authorAvatar = f2.avatarUrl,
            text = "Golden hour amidst the tea plants of Srimangal. The air is so fresh and wet here! Greenery as far as the eye can see. 🫖🏔️💚 #touring #bangladesh",
            location = "Srimangal, Sylhet",
            latitude = 24.3065,
            longitude = 91.7295,
            timestamp = System.currentTimeMillis() - 3600000 * 5, // 5 hours ago
            likesCount = 38,
            isLikedByMe = false,
            commentsCount = 4
        )
        val post3 = Post(
            id = "post_3",
            userId = "user_nahid",
            authorName = "Nahid Hasan",
            authorAvatar = f3.avatarUrl,
            text = "Just finished a morning 10k running session. Fitness level is peaking! Ready for the upcoming league matches. Let's do this! 🏃‍♂️💪🏏 #athletics",
            location = "Chittagong, Bangladesh",
            latitude = 22.3569,
            longitude = 91.7832,
            timestamp = System.currentTimeMillis() - 3600000 * 12, // 12 hours ago
            likesCount = 21,
            isLikedByMe = false,
            commentsCount = 1
        )
        socialDao.insertPost(post1)
        socialDao.insertPost(post2)
        socialDao.insertPost(post3)

        // Seed Comments
        socialDao.insertComment(Comment("cmt_1", "post_1", "user_me", "Sajiur Rahman", me.avatarUrl, "Excellent design Tariq! Let's test the offline database synchronization.", System.currentTimeMillis() - 3600000))
        socialDao.insertComment(Comment("cmt_2", "post_1", "user_rez", "Rezwana Chowdhury", f2.avatarUrl, "I want to try this out! Invite me to beta.", System.currentTimeMillis() - 1800000))
        socialDao.insertComment(Comment("cmt_3", "post_2", "user_me", "Sajiur Rahman", me.avatarUrl, "Stunning view, Rezwana! Bring back some pure tea leaves please 😋", System.currentTimeMillis() - 3600000 * 4))

        // 5. Seeding Reels
        val reel1 = Reel(
            id = "reel_1",
            userId = "user_rez",
            authorName = "Rezwana Chowdhury",
            authorAvatar = f2.avatarUrl,
            caption = "Wandering through the green tea gardens of Sylhet. This view is therapeutic! 🌿✨",
            bgGradientIndex = 0,
            musicTitle = "Summer Acoustics - Taylor",
            likesCount = 120,
            isLikedByMe = false,
            commentCount = 18,
            timestamp = System.currentTimeMillis() - 3600000 * 3
        )
        val reel2 = Reel(
            id = "reel_2",
            userId = "user_tariq",
            authorName = "Tariqul Islam",
            authorAvatar = f1.avatarUrl,
            caption = "Clean code is key. Visualized custom Canvas canvas logic in Composable in 15 seconds! 💻🔥",
            bgGradientIndex = 1,
            musicTitle = "Synthwave Vibes - Neon",
            likesCount = 89,
            isLikedByMe = true,
            commentCount = 12,
            timestamp = System.currentTimeMillis() - 3600000 * 8
        )
        val reel3 = Reel(
            id = "reel_3",
            userId = "user_farhana",
            authorName = "Farhana Ahmed",
            authorAvatar = f4.avatarUrl,
            caption = "Fritters & famous Kalai Roti of Rajshahi Shaheb Bazar! Absolutely delicious 🤤 #foodvlog",
            bgGradientIndex = 2,
            musicTitle = "Spicy Beats - Desi",
            likesCount = 203,
            isLikedByMe = false,
            commentCount = 31,
            timestamp = System.currentTimeMillis() - 3600000 * 24
        )
        socialDao.insertReel(reel1)
        socialDao.insertReel(reel2)
        socialDao.insertReel(reel3)

        // 6. Seeding Stories (Active & Expired to test 24 hours filters)
        // Active Stories (timestamp > now - 24 hours):
        val storyActive1 = Story(
            id = "story_act_1",
            userId = "user_rez",
            authorName = "Rezwana Chowdhury",
            authorAvatar = f2.avatarUrl,
            text = "Rainy Day vibes here! Enjoying hot ginger tea ☕🌧️",
            bgGradientIndex = 1,
            locationLabel = "Srimangal, Sylhet",
            latitude = 24.3065,
            longitude = 91.7295,
            timestamp = System.currentTimeMillis() - 3600000 * 3 // 3 hours ago (active)
        )
        val storyActive2 = Story(
            id = "story_act_2",
            userId = "user_tariq",
            authorName = "Tariqul Islam",
            authorAvatar = f1.avatarUrl,
            text = "Deploying to production, wish me luck guys! 🚀🧑‍💻",
            bgGradientIndex = 3,
            locationLabel = "Gulshan, Dhaka",
            latitude = 23.7925,
            longitude = 90.4078,
            timestamp = System.currentTimeMillis() - 3600000 * 10 // 10 hours ago (active)
        )
        val storyActive3 = Story(
            id = "story_act_3",
            userId = "user_farhana",
            authorName = "Farhana Ahmed",
            authorAvatar = f4.avatarUrl,
            text = "Sweet, fresh, juicy mangoes directly harvested from Kansat orchard! 🥭",
            bgGradientIndex = 0,
            locationLabel = "Kansat, Rajshahi",
            latitude = 24.3745,
            longitude = 88.6042,
            timestamp = System.currentTimeMillis() - 3600000 * 18 // 18 hours ago (active)
        )

        // Expired Story (timestamp < now - 24 hours):
        val storyExpired = Story(
            id = "story_exp_1",
            userId = "user_nahid",
            authorName = "Nahid Hasan",
            authorAvatar = f3.avatarUrl,
            text = "Heavy workout routine is complete. Feeling exhausted 🏋️‍♂️",
            bgGradientIndex = 2,
            locationLabel = "Chittagong, Bangladesh",
            latitude = 22.3569,
            longitude = 91.7832,
            timestamp = System.currentTimeMillis() - 3600000 * 28 // 28 hours ago (EXPIRED!)
        )

        socialDao.insertStory(storyActive1)
        socialDao.insertStory(storyActive2)
        socialDao.insertStory(storyActive3)
        socialDao.insertStory(storyExpired)

        // 7. Seeding Conversations & Messages
        val convo1Id = "convo_tariq"
        val convo1 = Conversation(
            id = convo1Id,
            userId = "user_tariq",
            userName = "Tariqul Islam",
            userAvatar = f1.avatarUrl,
            lastMessage = "Let's share some locations soon.",
            lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 15,
            unreadCount = 1
        )
        socialDao.insertConversation(convo1)

        val m1 = Message("m_t_1", convo1Id, "user_tariq", "user_me", "Hey Sajiur! Did you check out the new design?", System.currentTimeMillis() - 1000 * 60 * 60)
        val m2 = Message("m_t_2", convo1Id, "user_me", "user_tariq", "Yes, looks really clean!", System.currentTimeMillis() - 1000 * 60 * 45)
        val m3 = Message("m_t_3", convo1Id, "user_tariq", "user_me", "Awesome! I'll test it right away.", System.currentTimeMillis() - 1000 * 60 * 30)
        val m4 = Message("m_t_4", convo1Id, "user_tariq", "user_me", "Let's share some locations soon.", System.currentTimeMillis() - 1000 * 60 * 15)
        socialDao.insertMessage(m1)
        socialDao.insertMessage(m2)
        socialDao.insertMessage(m3)
        socialDao.insertMessage(m4)

        val convo2Id = "convo_rez"
        val convo2 = Conversation(
            id = convo2Id,
            userId = "user_rez",
            userName = "Rezwana Chowdhury",
            userAvatar = f2.avatarUrl,
            lastMessage = "I am currently checked-in at the tea gardens!",
            lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 30,
            unreadCount = 0
        )
        socialDao.insertConversation(convo2)

        val m5 = Message("m_r_1", convo2Id, "user_me", "user_rez", "Are you coding or traveling in Sylhet?", System.currentTimeMillis() - 1000 * 60 * 120)
        val m6 = Message("m_r_2", convo2Id, "user_rez", "user_me", "Traveling! I am currently checked-in at the tea gardens!", System.currentTimeMillis() - 1000 * 60 * 30)
        socialDao.insertMessage(m5)
        socialDao.insertMessage(m6)
    }
}
