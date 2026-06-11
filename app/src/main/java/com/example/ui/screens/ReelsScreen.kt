package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Reel
import com.example.ui.SocialViewModel
import java.util.*

@Composable
fun ReelsScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val reels by viewModel.reels.collectAsState()
    val listState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (reels.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MovieFilter,
                    contentDescription = "No Reels",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Reels Yet",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Explore creativity or share your own first video reel!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.selectTab("create") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Reel")
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(reels) { index, reel ->
                    ReelViewportItem(
                        reel = reel,
                        viewportHeight = screenHeight,
                        onLikeToggle = { viewModel.toggleLikeReel(reel.id) },
                        onCommentClick = {
                            // Quick follow-up message simulation or conversational redirect
                            viewModel.openChatWithUser(
                                com.example.data.User(
                                    id = reel.userId,
                                    name = reel.authorName,
                                    username = reel.authorName.lowercase().replace(" ", "_"),
                                    avatarUrl = reel.authorAvatar,
                                    bio = ""
                                )
                            )
                        },
                        onFollowToggle = { viewModel.toggleFollowUser(reel.userId) },
                        viewModel = viewModel
                    )
                }
            }

            // High aesthetic header overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reels",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = 0.5.sp
                )
                IconButton(
                    onClick = { viewModel.selectTab("create") },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Create Reel", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun ReelViewportItem(
    reel: Reel,
    viewportHeight: androidx.compose.ui.unit.Dp,
    onLikeToggle: () -> Unit,
    onCommentClick: () -> Unit,
    onFollowToggle: () -> Unit,
    viewModel: SocialViewModel
) {
    val gradientColors = BrandGradients[reel.bgGradientIndex % BrandGradients.size]
    val followingIds by viewModel.followingIdsList.collectAsState()
    val isFollowing = followingIds.contains(reel.userId) || reel.userId == "user_me"

    // Infinite rotation for record CD disk
    val infiniteTransition = rememberInfiniteTransition(label = "CD Rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(viewportHeight - 80.dp) // Leave screen space for navigation bars
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // High polish backdrop visual canvas artwork instead of empty black screen
        AnimatedSoundVisualizer(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp)
        )

        // Bottom and side dark scrim overlay to make typography easily legible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Overlay: Bottom Left Content details
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 24.dp, end = 80.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = reel.authorAvatar,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = reel.authorName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "@${reel.authorName.lowercase().replace(" ", "_")}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }

                if (reel.userId != "user_me") {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (isFollowing) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(onClick = onFollowToggle)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isFollowing) "Following" else "Follow",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Caption Text
            Text(
                text = reel.caption,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Music Soundtrack info bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reel.musicTitle,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Overlay: Right Sidebar HUD containing action triggers
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like Button HUD
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onLikeToggle,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(46.dp)
                        .testTag("reel_like_btn_${reel.id}")
                ) {
                    Icon(
                        imageVector = if (reel.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like Reel",
                        tint = if (reel.isLikedByMe) Color.Red else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = reel.likesCount.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comment (Triggers instant user PM/Direct Message as contextual shorthand)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .size(46.dp)
                        .testTag("reel_comment_btn_${reel.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Comment,
                        contentDescription = "Comment/Direct Message author",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = reel.commentCount.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share / Send Button
            IconButton(
                onClick = {
                    // Start a chat to share the reel!
                    viewModel.selectTab("inbox")
                },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated rotating CD disk cover representing standard dynamic media players
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .rotate(rotationAngle)
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            ) {
                AsyncImage(
                    model = reel.authorAvatar,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun AnimatedSoundVisualizer(modifier: Modifier = Modifier) {
    var phase by remember { mutableStateOf(0f) }
    
    // Animate phase endlessly to drive nice waves
    val infiniteTransition = rememberInfiniteTransition(label = "Phase Wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val waveCount = 3
        val centerY = height * 0.5f

        for (w in 0 until waveCount) {
            val amplitude = 30f * (w + 1)
            val wavelength = width * 1.2f / (w + 1)
            val strokeColor = Color.White.copy(alpha = 0.08f * (waveCount - w))
            
            val path = androidx.compose.ui.graphics.Path()
            var first = true
            
            for (x in 0..width.toInt() step 5) {
                val currentX = x.toFloat()
                // Calculates animated sinus waves
                val sine = Math.sin(((currentX / wavelength) * 2 * Math.PI) + waveOffset + (w * 1.5)).toFloat()
                val currentY = centerY + (sine * amplitude)
                
                if (first) {
                    path.moveTo(currentX, currentY)
                    first = false
                } else {
                    path.lineTo(currentX, currentY)
                }
            }
            
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
