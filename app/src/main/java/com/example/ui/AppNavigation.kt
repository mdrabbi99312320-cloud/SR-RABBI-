package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*

@Composable
fun AppNavigation(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeChatConvoId by viewModel.activeChatConvoId.collectAsState()

    Scaffold(
        bottomBar = {
            // Hide bottom navigation bar when inside individual chat threads to maximize vertical typing space
            if (activeChatConvoId == null && currentTab != "chat") {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_bottom_navigation"),
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = currentTab == "feed",
                        onClick = { viewModel.selectTab("feed") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "feed") Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home Feed"
                            )
                        },
                        label = { Text("Feed", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_feed")
                    )

                    NavigationBarItem(
                        selected = currentTab == "reels",
                        onClick = { viewModel.selectTab("reels") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "reels") Icons.Filled.MovieFilter else Icons.Outlined.MovieFilter,
                                contentDescription = "Reels"
                            )
                        },
                        label = { Text("Reels", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_reels")
                    )

                    NavigationBarItem(
                        selected = currentTab == "create",
                        onClick = { viewModel.selectTab("create") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Share Moments"
                            )
                        },
                        label = { Text("Share", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_share")
                    )

                    NavigationBarItem(
                        selected = currentTab == "location",
                        onClick = { viewModel.selectTab("location") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "location") Icons.Filled.MyLocation else Icons.Outlined.MyLocation,
                                contentDescription = "Location Radar"
                            )
                        },
                        label = { Text("Radar", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_location")
                    )

                    NavigationBarItem(
                        selected = currentTab == "inbox",
                        onClick = { viewModel.selectTab("inbox") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "inbox") Icons.Filled.Forum else Icons.Outlined.Forum,
                                contentDescription = "Inbox"
                            )
                        },
                        label = { Text("Inbox", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_inbox")
                    )

                    NavigationBarItem(
                        selected = currentTab == "profile",
                        onClick = { viewModel.selectTab("profile") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "My Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Transitions based on tab selection
            when {
                activeChatConvoId != null || currentTab == "chat" -> {
                    ChatScreen(viewModel = viewModel)
                }
                else -> {
                    Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                        when (tab) {
                            "feed" -> FeedScreen(viewModel = viewModel)
                            "reels" -> ReelsScreen(viewModel = viewModel)
                            "create" -> CreateScreen(viewModel = viewModel)
                            "location" -> LocationScreen(viewModel = viewModel)
                            "inbox" -> InboxScreen(viewModel = viewModel)
                            "profile" -> ProfileScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
