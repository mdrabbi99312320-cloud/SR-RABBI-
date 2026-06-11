package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SocialViewModel(private val repository: SocialRepository) : ViewModel() {

    // --- Navigation and Active Overlay States ---
    private val _currentTab = MutableStateFlow("feed")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _selectedStory = MutableStateFlow<Story?>(null)
    val selectedStory: StateFlow<Story?> = _selectedStory.asStateFlow()

    private val _activeChatConvoId = MutableStateFlow<String?>(null)
    val activeChatConvoId: StateFlow<String?> = _activeChatConvoId.asStateFlow()

    private val _activeChatPartner = MutableStateFlow<User?>(null)
    val activeChatPartner: StateFlow<User?> = _activeChatPartner.asStateFlow()

    // --- Search Query State ---
    private val _userSearchQuery = MutableStateFlow("")
    val userSearchQuery: StateFlow<String> = _userSearchQuery.asStateFlow()

    // --- Data Streams ---
    val currentUser: StateFlow<User?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val posts: StateFlow<List<Post>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeStories: StateFlow<List<Story>> = repository.getActiveStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reels: StateFlow<List<Reel>> = repository.allReels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val otherUsers: StateFlow<List<User>> = repository.otherUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<Conversation>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic Search ---
    val searchedUsers: StateFlow<List<User>> = _userSearchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.otherUsers
            } else {
                repository.searchUsers(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Chat Messages Flow ---
    val activeChatMessages: StateFlow<List<Message>> = _activeChatConvoId
        .flatMapLatest { convoId ->
            if (convoId == null) flowOf(emptyList())
            else repository.getMessages(convoId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Followers / Following Counters ---
    val followersCount: StateFlow<Int> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(0)
            else repository.getFollowersCount(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val followingCount: StateFlow<Int> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(0)
            else repository.getFollowingCount(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val followingIdsList: StateFlow<List<String>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getFollowingIds(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Kickoff database pre-seeding
        viewModelScope.launch {
            repository.preseedDataIfEmpty()
        }
    }

    // --- Tab Changer ---
    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    // --- Stories Handler ---
    fun openStory(story: Story) {
        _selectedStory.value = story
    }

    fun closeStory() {
        _selectedStory.value = null
    }

    // --- Actions ---
    fun toggleLikePost(postId: String) {
        viewModelScope.launch {
            repository.toggleLikePost(postId)
        }
    }

    fun addCommentToPost(postId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(postId, text)
        }
    }

    fun getCommentsForPost(postId: String): kotlinx.coroutines.flow.Flow<List<com.example.data.Comment>> {
        return repository.getCommentsForPost(postId)
    }

    fun toggleLikeReel(reelId: String) {
        viewModelScope.launch {
            repository.toggleLikeReel(reelId)
        }
    }

    fun toggleFollowUser(otherUserId: String) {
        viewModelScope.launch {
            repository.toggleFollow(otherUserId)
        }
    }

    fun setSearchQuery(query: String) {
        _userSearchQuery.value = query
    }

    // --- Create Content Flow ---
    fun createPost(text: String, location: String? = null, lat: Double? = null, lng: Double? = null) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.createPost(text, null, location, lat, lng)
            _currentTab.value = "feed"
        }
    }

    fun createReel(caption: String, bgIndex: Int, soundtrack: String) {
        if (caption.isBlank()) return
        viewModelScope.launch {
            repository.createReel(caption, bgIndex, soundtrack)
            _currentTab.value = "reels"
        }
    }

    fun createStory(text: String, bgIndex: Int, location: String? = null, lat: Double? = null, lng: Double? = null) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.createStory(text, bgIndex, location, lat, lng)
            _currentTab.value = "feed"
        }
    }

    // --- Active Chat & Conversations Flow ---
    fun openChatWithUser(otherUser: User) {
        viewModelScope.launch {
            val convoId = repository.startChatWithUser(otherUser)
            _activeChatPartner.value = otherUser
            _activeChatConvoId.value = convoId
            repository.clearUnreads(convoId)
            _currentTab.value = "chat"
        }
    }

    fun openConversation(convo: Conversation) {
        viewModelScope.launch {
            // Find full otherUser object
            val users = repository.otherUsers.first()
            val user = users.find { it.id == convo.userId } ?: User(
                id = convo.userId,
                name = convo.userName,
                username = convo.userName.lowercase().replace(" ", "_"),
                avatarUrl = convo.userAvatar,
                bio = ""
            )
            _activeChatPartner.value = user
            _activeChatConvoId.value = convo.id
            repository.clearUnreads(convo.id)
            _currentTab.value = "chat"
        }
    }

    fun sendChatMessage(text: String, locationSharing: Boolean = false) {
        val convoId = _activeChatConvoId.value ?: return
        if (text.isBlank() && !locationSharing) return
        
        viewModelScope.launch {
            if (locationSharing) {
                // Share current user's location coordinates
                val current = repository.getCurrentUserSync()
                val locLabel = current?.locationLabel ?: "Dhaka, Bangladesh"
                val lat = current?.latitude ?: 23.8103
                val lng = current?.longitude ?: 90.4125
                repository.sendMessage(convoId, "📍 Shared Check-in Location", locLabel, lat, lng)
            } else {
                repository.sendMessage(convoId, text)
            }
        }
    }

    fun closeChat() {
        _activeChatConvoId.value = null
        _activeChatPartner.value = null
        _currentTab.value = "inbox"
    }

    // --- Profile Customizer Settings ---
    fun updateProfile(name: String, bio: String, location: String) {
        viewModelScope.launch {
            val current = repository.getCurrentUserSync() ?: return@launch
            val updated = current.copy(
                name = name,
                bio = bio,
                locationLabel = location
            )
            repository.updateCurrentUser(updated)
        }
    }

    // Reset database fully to original seed
    fun resetData(context: Context) {
        viewModelScope.launch {
            // Drop database operations or simple local cleanups
            // Since we use SQLite fallbackDestructiveMigration, we can clear everything or delete & preseed
            // For simple instant feedback, we can rewrite the database
            val db = AppDatabase.getDatabase(context)
            db.clearAllTables()
            repository.preseedDataIfEmpty()
            _activeChatConvoId.value = null
            _activeChatPartner.value = null
            _currentTab.value = "feed"
        }
    }

    // --- Factory ---
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val database = AppDatabase.getDatabase(context)
            val repository = SocialRepository(database.socialDao())
            @Suppress("UNCHECKED_CAST")
            return SocialViewModel(repository) as T
        }
    }
}
