package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.User
import com.example.ui.SocialViewModel
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val otherUsers by viewModel.otherUsers.collectAsState()

    var checkInText by remember { mutableStateOf("") }
    var selectedUserForFocus by remember { mutableStateOf<User?>(null) }
    val focusManager = LocalFocusManager.current

    // Infinite sweep animation for the canvas radar
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top app header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Location Sharing Radar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real-time coordinate sharing & active friend check-ins",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // High fidelity radar viewport canvas
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing the holographic scanning grid
                        RadarMeshGrid(
                            sweepAngle = sweepAngle,
                            currentUser = currentUser,
                            selectedUser = selectedUserForFocus,
                            friendsLocations = otherUsers
                        )

                        // Float info box about centered item
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "Active Beacons: ${otherUsers.size + 1}",
                                color = Color(0xFF38ef7d),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Focus notification label
                        selectedUserForFocus?.let { focusUser ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(12.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tracking: ${focusUser.name} (${focusUser.locationLabel})",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear focus",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { selectedUserForFocus = null }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Manual check-in card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Share Current Check-In",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Update your location label to checkin instantly",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presets = listOf(
                                "Banani Block E, Dhaka" to (23.7937 to 90.4066),
                                "Lalbagh Fort, Dhaka" to (23.7188 to 90.3882),
                                "Srimangal Resort, Sylhet" to (24.3065 to 91.7295),
                                "Cox's Bazar Beach 🌊" to (21.4272 to 91.9701),
                                "Shah Makhdum, Rajshahi" to (24.3745 to 88.6042)
                            )
                            presets.forEach { (label, coords) ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            viewModel.updateProfile(
                                                currentUser?.name ?: "Sajiur Rahman",
                                                currentUser?.bio ?: "",
                                                label
                                            )
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Text Field custom
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = checkInText,
                                onValueChange = { checkInText = it },
                                placeholder = { Text("Or write manual location name...", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("location_checkin_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (checkInText.isNotBlank()) {
                                        viewModel.updateProfile(
                                            currentUser?.name ?: "Sajiur Rahman",
                                            currentUser?.bio ?: "",
                                            checkInText
                                        )
                                        checkInText = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                enabled = checkInText.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("location_checkin_submit_btn")
                            ) {
                                Text("Post")
                            }
                        }
                    }
                }
            }

            // Friend list listing with location coordinates
            item {
                Text(
                    text = "Nearby Active Friends",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            items(otherUsers) { friend ->
                val distance = calculateDistanceInKm(
                    currentUser?.latitude ?: 23.8103,
                    currentUser?.longitude ?: 90.4125,
                    friend.latitude,
                    friend.longitude
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable { selectedUserForFocus = friend }
                        .testTag("radar_user_item_${friend.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedUserForFocus?.id == friend.id)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = friend.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friend.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = friend.locationLabel,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "~${String.format("%.1f", distance)} km",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = { viewModel.openChatWithUser(friend) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("ping_friend_btn_${friend.id}")
                            ) {
                                Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Ping Chat", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadarMeshGrid(
    sweepAngle: Float,
    currentUser: User?,
    selectedUser: User?,
    friendsLocations: List<User>
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = min(size.width, size.height) / 2 * 0.9f

        // Draw concentric scanning rings
        for (i in 1..4) {
            val radius = maxRadius * (i / 4.0f)
            drawCircle(
                color = Color(0xFF00ffcc).copy(alpha = 0.15f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw crosshair axis lines
        drawLine(
            color = Color(0xFF00ffcc).copy(alpha = 0.2f),
            start = Offset(center.x - maxRadius, center.y),
            end = Offset(center.x + maxRadius, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color(0xFF00ffcc).copy(alpha = 0.2f),
            start = Offset(center.x, center.y - maxRadius),
            end = Offset(center.x, center.y + maxRadius),
            strokeWidth = 1.dp.toPx()
        )

        // Draw compass headings
        // North, East, South, West would go here implicitly in layout space.

        // Draw active radar sweep beam
        val sweepAngleRad = Math.toRadians(sweepAngle.toDouble())
        val endX = center.x + maxRadius * cos(sweepAngleRad).toFloat()
        val endY = center.y + maxRadius * sin(sweepAngleRad).toFloat()
        drawLine(
            color = Color(0xFF00ffcc).copy(alpha = 0.8f),
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 2.dp.toPx()
        )

        // Draw center home position (current User Sajiur)
        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color(0xFF0072ff),
            radius = 4.dp.toPx(),
            center = center
        )

        // Position other friends relative to home (center = Dhaka coordinate basis)
        val homeLat = currentUser?.latitude ?: 23.8103
        val homeLng = currentUser?.longitude ?: 90.4125

        friendsLocations.forEachIndexed { idx, friend ->
            // Simple geographic scaling to viewport offset
            val latDiff = friend.latitude - homeLat
            val lngDiff = friend.longitude - homeLng

            // High amplification scale factor for geographic representation in limits of Bangladesh map
            val scale = 30f 
            val rawOffset = Offset(
                center.x + (lngDiff * scale).toFloat() * 10f,
                center.y - (latDiff * scale).toFloat() * 10f // Cartesion Y matches negative Latitudes
            )

            // Bound checking distance to keep within radar circle limits
            val currentDist = sqrt((rawOffset.x - center.x).pow(2) + (rawOffset.y - center.y).pow(2))
            val finalOffset = if (currentDist > maxRadius) {
                // Vector normalization to radar edge
                val angle = atan2(rawOffset.y - center.y, rawOffset.x - center.x)
                Offset(
                    center.x + maxRadius * cos(angle),
                    center.y + maxRadius * sin(angle)
                )
            } else {
                rawOffset
            }

            val isFocused = selectedUser?.id == friend.id
            val blinkAlpha = if (isFocused) 0.9f else 0.5f

            // Blend glowing target circle
            drawCircle(
                color = if (isFocused) Color.Yellow else Color(0xFF00ffcc),
                radius = if (isFocused) 8.dp.toPx() else 5.dp.toPx(),
                center = finalOffset,
                alpha = blinkAlpha
            )

            if (isFocused) {
                drawCircle(
                    color = Color.Yellow.copy(alpha = 0.3f),
                    radius = 16.dp.toPx(),
                    center = finalOffset,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}

// Haversine distance formula to calculate exact real km difference
fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
