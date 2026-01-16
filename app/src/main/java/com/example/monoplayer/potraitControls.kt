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
import androidx.compose.ui.text.style.TextAlign
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
    onDraggingChanged: (Boolean) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(mediaPlayer.isPlaying) }
    var showAspect by rememberSaveable { mutableStateOf(false) }
    var currentRatioText by remember { mutableStateOf("Default") }
    val activity = LocalActivity.current as MainActivity

    LaunchedEffect(showAspect) {
        if (showAspect) {
            delay(1200)
            showAspect = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        // --- GESTURE LAYER ---
        BrightnessVolume(vm,mediaPlayer, activity, isControlsVisible, toggleControls = toggleShow)

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
                    modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)
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
            }
        }

        // --- ASPECT RATIO INDICATOR ---
        if (showAspect) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black.copy(0.8f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    currentRatioText,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
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
            ) {
                VideoProgressSlider(
                    vm, mediaPlayer, isDragging,
                    onSeek = { isDragging = true; mediaPlayer.position = it },
                    onFinished = { isDragging = false },onDraggingChanged
                )

                // MAIN PLAYBACK ROW
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.baseline_fast_rewind_24), null,
                        Modifier.size(36.dp).clickable { mediaPlayer.time -= 10000L },
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
                        painterResource(R.drawable.baseline_fast_forward_24), null,
                        Modifier.size(36.dp).clickable { mediaPlayer.time += 10000L },
                        tint = Color.White
                    )
                }

                // UTILITY BAR (Floating Pills)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PortraitUtilityPill {
                        IconButton(onClick = showLock) {
                            Icon(painterResource(R.drawable.twotone_lock_24), null, tint = Color.White)
                        }
                        IconButton(onClick = {
                            currentRatioText = changeAspectRatio(mediaPlayer, activity) ?: "Default"
                            showAspect = true
                        }) {
                            Icon(painterResource(R.drawable.fit_screen_24), null, tint = Color.White)
                        }
                    }

                    PortraitUtilityPill {
                        ModernOrientationButton(vm)
                        IconButton(onClick = { /* BG Play logic */ }) {
                            Icon(painterResource(R.drawable.headphones_24), null, tint = Color.White)
                        }
                    }

                    PortraitUtilityPill {
                        IconButton(onClick = showSubtitle) {
                            Icon(painterResource(R.drawable.twotone_subtitles_24), null, tint = Color.White)
                        }
                        IconButton(onClick = {
                            vm.isPip.value = true
                            activity.enterPipMode(mediaPlayer.isPlaying)
                        }) {
                            Icon(painterResource(R.drawable.baseline_picture_in_picture_alt_24), null, tint = Color.White)
                        }
                        IconButton(onClick = togglePlaylist) {
                            Icon(painterResource(R.drawable.playlist_play_24), null, tint = Color.White)
                        }
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
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(0.12f))
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp)),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}