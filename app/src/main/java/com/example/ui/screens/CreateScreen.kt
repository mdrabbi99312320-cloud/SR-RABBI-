package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SocialViewModel

@Composable
fun CreateScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf("post") } // post, story, reel
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Safe Header
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
                    text = "Creation Studio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Express yourself with Posts, Stories, or Reels",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Segmented selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val types = listOf(
                "post" to "Post" to Icons.Default.PostAdd,
                "story" to "Story" to Icons.Default.Star,
                "reel" to "Reel" to Icons.Default.MovieFilter
            )

            types.forEach { (pair, icon) ->
                val (key, label) = pair
                val isSelected = selectedType == key
                
                Button(
                    onClick = {
                        selectedType = key
                        focusManager.clearFocus()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("create_tab_$key"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    elevation = null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Dynamic viewport body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            when (selectedType) {
                "post" -> CreatePostSubView(onPublish = { text, loc ->
                    viewModel.createPost(text, loc)
                })
                "story" -> CreateStorySubView(onPublish = { text, bg, loc ->
                    viewModel.createStory(text, bg, loc)
                })
                "reel" -> CreateReelSubView(onPublish = { caption, bg, music ->
                    viewModel.createReel(caption, bg, music)
                })
            }
        }
    }
}

@Composable
fun CreatePostSubView(
    onPublish: (String, String?) -> Unit
) {
    var textPost by remember { mutableStateOf("") }
    var setLocationTag by remember { mutableStateOf("") }
    var showLocChooser by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Compose New Feed Post",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = textPost,
                onValueChange = { textPost = it },
                placeholder = { Text("What is happening Sajiur? Share details...", fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("create_post_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Location check-in row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("Tag Check-In Location", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (setLocationTag.isBlank()) "No Location Tagged" else setLocationTag,
                        fontSize = 11.sp,
                        color = if (setLocationTag.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        fontWeight = if (setLocationTag.isBlank()) FontWeight.Normal else FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showLocChooser = !showLocChooser }) {
                    Text(if (showLocChooser) "Close" else "Tag", fontSize = 11.sp)
                }
            }

            if (showLocChooser) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        "Banani Club, Dhaka", "Srimangal Tea garden, Sylhet", "Stadium, Chittagong", "Varendra Museum, Rajshahi"
                    )
                    presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .clickable {
                                    setLocationTag = preset
                                    showLocChooser = false
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(preset, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onPublish(textPost, if (setLocationTag.isBlank()) null else setLocationTag) },
                enabled = textPost.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("publish_post_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Publish Post", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CreateStorySubView(
    onPublish: (String, Int, String?) -> Unit
) {
    var storyText by remember { mutableStateOf("") }
    var selectedGradientIndex by remember { mutableStateOf(0) }
    var storyLocation by remember { mutableStateOf("") }

    val activeGradients = BrandGradients

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Compose expiring 24h Story",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Background preview container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(activeGradients[selectedGradientIndex]))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (storyText.isBlank()) "Story text preview goes here..." else storyText,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gradients Picker
            Text("Select Visual Gradient Background Theme", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeGradients.forEachIndexed { index, colors ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.sweepGradient(colors))
                            .border(
                                width = if (selectedGradientIndex == index) 3.dp else 0.dp,
                                color = if (selectedGradientIndex == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedGradientIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = storyText,
                onValueChange = { storyText = it },
                placeholder = { Text("Write your story mood...", fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_story_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = storyLocation,
                onValueChange = { storyLocation = it },
                placeholder = { Text("Add Location label (e.g. Dhaka)...", fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_story_location_input"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp)) },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onPublish(storyText, selectedGradientIndex, if (storyLocation.isBlank()) null else storyLocation) },
                enabled = storyText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("publish_story_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Publish Story", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CreateReelSubView(
    onPublish: (String, Int, String) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var soundtrack by remember { mutableStateOf("Desi Beats Mix") }
    var bgInd by remember { mutableStateOf(0) }

    val activeGradients = BrandGradients

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Compose New Video Reel",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                placeholder = { Text("Write an interesting caption for the reel...", fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("create_reel_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = soundtrack,
                onValueChange = { soundtrack = it },
                placeholder = { Text("Soundtrack Name (e.g. Lo-Fi Beats)", fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_reel_sound_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Backdrop Picker
            Text("Backdrop Theme Gradient", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeGradients.forEachIndexed { index, colors ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.sweepGradient(colors))
                            .border(
                                width = if (bgInd == index) 3.dp else 0.dp,
                                color = if (bgInd == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { bgInd = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onPublish(caption, bgInd, soundtrack) },
                enabled = caption.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("publish_reel_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.MovieFilter, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Publish Reel", fontWeight = FontWeight.Bold)
            }
        }
    }
}
