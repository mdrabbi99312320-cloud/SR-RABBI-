package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String,
    val bio: String,
    val locationLabel: String = "No Location Shared",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String,
    val userId: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val imageUrl: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val commentsCount: Int = 0
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey val id: String,
    val postId: String,
    val userId: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reels")
data class Reel(
    @PrimaryKey val id: String,
    val userId: String,
    val authorName: String,
    val authorAvatar: String,
    val caption: String,
    val bgGradientIndex: Int = 0,
    val musicTitle: String = "Original Audio",
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val commentCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey val id: String,
    val userId: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val bgGradientIndex: Int = 0,
    val locationLabel: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "follows")
data class Follow(
    @PrimaryKey val id: String, // followerId_followedId
    val followerId: String,
    val followedId: String
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val locationLabel: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
