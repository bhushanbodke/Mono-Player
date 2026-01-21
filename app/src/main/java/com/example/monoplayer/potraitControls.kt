package com.example.monoplayer

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer

@Composable
fun PortraitControls(
    vm: MyViewModel,
    video: VideoModel?,
    mediaPlayer: MediaPlayer,
    isControlsVisible: Boolean,
    showLock: () -> Unit,
    showSubtitle: () -> Unit,
    togglePlaylist: () -> Unit,
    toggleShow: () -> Unit,
    showAudioSelector: () -> Unit,
    onAction: () -> Unit,
    onDraggingChanged: (Boolean) -> Unit,
    showSettings: () -> Unit
) {
    val isModernUI by vm.modernUI.collectAsState()
    var isDragging by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(mediaPlayer.isPlaying) }
    var showAspect by rememberSaveable { mutableStateOf(false) }
    var showForward by rememberSaveable { mutableStateOf(false) }
    var showBackward by rememberSaveable { mutableStateOf(false) }
    var currentRatioText by remember { mutableStateOf("Default") }
    val activity = LocalActivity.current as MainActivity

    // Auto-hide indicators
    LaunchedEffect(showAspect) {
        if (showAspect) { delay(1200); showAspect = false }
    }
    LaunchedEffect(showForward, showBackward) {
        if (showForward || showBackward) {
            delay(1000)
            showForward = false
            showBackward = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        // --- GESTURE LAYER ---
        BrightnessVolume(vm, mediaPlayer, activity, isControlsVisible, toggleControls = toggleShow)

        // --- TOP BAR (Animated) ---
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(0.7f), Color.Transparent)))
                    .padding(top = 48.dp, start = 12.dp, end = 12.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { vm.setScreen(Screens.Videos) },
                    modifier = Modifier.background(Color.Black.copy(0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, "back", tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = video?.name ?: "Unknown Video",
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(shadow = Shadow(Color.Black, Offset(2f, 2f), 4f))
                )
                IconButton(
                    onClick = {showSettings()},
                    modifier = Modifier.background(Color.Black.copy(0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.Settings, "back", tint = Color.White)
                }
            }
        }
        // --- CENTER INDICATORS (Aspect / Seek) ---
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            if (showForward || showBackward) {
                Surface(color = Color.Black.copy(0.8f), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = if (showForward) "+ 10" else "- 10",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                }
            }
            if (showAspect) {
                Surface(color = Color.Black.copy(0.8f), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        currentRatioText, color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        fontWeight = FontWeight.Black, fontSize = 18.sp
                    )
                }
            }
        }

        // --- BOTTOM CONTROLS (Animated) ---
        AnimatedVisibility(
            visible = isControlsVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.8f))))
                    .padding(bottom = 32.dp, top = 20.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            ) {
                VideoProgressSlider(
                    vm, mediaPlayer, isDragging,
                    onSeek = { isDragging = true; mediaPlayer.position = it },
                    onFinished = { isDragging = false }, onDraggingChanged
                )

                // MAIN PLAYBACK ROW
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.replay_10_24), null,
                        Modifier.size(45.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(0.2f), CircleShape)
                            .clickable { mediaPlayer.time -= 10000L; onAction(); showBackward = true },
                        tint = Color.White
                    )

                    Spacer(Modifier.width(32.dp))

                    Box(
                        Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable {
                                if (isPlaying) mediaPlayer.pause() else mediaPlayer.play()
                                isPlaying = !isPlaying
                                onAction()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause_24 else R.drawable.play_arrow_24),
                            null, Modifier.size(40.dp), tint = Color.Black
                        )
                    }

                    Spacer(Modifier.width(32.dp))

                    Icon(
                        painterResource(R.drawable.twotone_forward_10_24), null,
                        Modifier.size(45.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(0.2f), CircleShape)
                            .clickable { mediaPlayer.time += 10000L; onAction(); showForward = true },
                        tint = Color.White
                    )
                }

                // UTILITY BAR (Modern vs Classic logic)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isModernUI) {
                        PortraitUtilityPill{
                            IconButton(onClick = { onAction(); showLock() }) {
                                Icon(painterResource(R.drawable.twotone_lock_24), null, tint = Color.White)
                            }
                            IconButton(onClick = {
                                onAction()
                                currentRatioText = changeAspectRatio(mediaPlayer, activity)
                                showAspect = true
                            }) {
                                Icon(painterResource(R.drawable.fit_screen_24), null, tint = Color.White)
                            }
                        }

                        PortraitUtilityPill {
                            ModernOrientationButton(vm)
                            IconButton(onClick = { onAction(); showAudioSelector() }) {
                                Icon(painterResource(R.drawable.headphones_24), null, tint = Color.White)
                            }
                        }

                        PortraitUtilityPill {
                            IconButton(onClick = { onAction(); showSubtitle() }) {
                                Icon(painterResource(R.drawable.twotone_subtitles_24), null, tint = Color.White)
                            }
                            IconButton(onClick = {
                                vm.isPip.value = true
                                activity.enterPipMode(mediaPlayer.isPlaying)
                            }) {
                                Icon(painterResource(R.drawable.baseline_picture_in_picture_alt_24), null, tint = Color.White)
                            }
                            IconButton(onClick = { onAction(); togglePlaylist() }) {
                                Icon(painterResource(R.drawable.playlist_play_24), null, tint = Color.White)
                            }
                        }
                    } else {
                        // Classic UI Logic (Simplified Row)
                        IconButton(onClick = showLock) { Icon(painterResource(R.drawable.twotone_lock_24), null, tint = Color.White) }
                        IconButton(onClick = showSubtitle) { Icon(painterResource(R.drawable.twotone_subtitles_24), null, tint = Color.White) }
                        IconButton(onClick = togglePlaylist) { Icon(painterResource(R.drawable.playlist_play_24), null, tint = Color.White) }
                        IconButton(onClick = {
                            currentRatioText = changeAspectRatio(mediaPlayer, activity)
                            showAspect = true
                        }) { Icon(painterResource(R.drawable.fit_screen_24), null, tint = Color.White) }
                    }
                }
            }
        }
    }
}

@Composable
fun PortraitUtilityPill(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 4.dp) // Space between different pills
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(0.12f)) // Glass effect
            .border(
                width = 1.dp,
                color = Color.White.copy(0.15f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 4.dp), // Internal padding for icons
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content
    )
}