package com.example.monoplayer

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
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
fun LandScapeControls(
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
    var showAspect by rememberSaveable { mutableStateOf(false) }
    var currentRatioText by remember { mutableStateOf("Default") }
    var isPlaying by remember { mutableStateOf(mediaPlayer.isPlaying) }
    val activity = LocalActivity.current as MainActivity

    LaunchedEffect(showAspect) {
        if (showAspect) {
            delay(1500)
            showAspect = false
        }
    }

    AnimatedVisibility(
        visible = isControlsVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 }
    ) {
        Box(Modifier.fillMaxSize()) {
            BrightnessVolume(vm,mediaPlayer, activity, isControlsVisible, toggleControls = toggleShow)
            // --- TOP GRADIENT (Title Area) ---
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(0.7f), Color.Transparent)
                        )
                    )
            )
            // --- TOP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(shadow = Shadow(Color.Black, Offset(2f, 2f), 4f))
                )
            }

            // --- RIGHT SIDE PANEL (Floaters) ---
            Column(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(0.4f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModernOrientationButton(vm)
                IconButton(onClick = { /* BG Play logic */ }) {
                    Icon(painterResource(R.drawable.headphones_24), null, tint = Color.White, modifier = Modifier.size(24.dp))
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

            // --- BOTTOM SECTION ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(0.8f))
                        )
                    )
                    .padding(bottom = 16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ){}
            ) {
                VideoProgressSlider(
                    vm, mediaPlayer, isDragging,
                    onSeek = { isDragging = true; mediaPlayer.position = it },
                    onFinished = { isDragging = false },
                    onDraggingChanged = onDraggingChanged
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT PILL (Settings)
                    ControlPill {
                        IconButton(onClick = { showLock() ;onAction()}) {
                            Icon(painterResource(R.drawable.twotone_lock_24), null, tint = Color.White)
                        }
                        IconButton(onClick = { showAudioSelector();onAction() }) {
                            Icon(painterResource(R.drawable.baseline_music_note_24), null, tint = Color.White)
                        }
                        IconButton(onClick = {
                            currentRatioText = changeAspectRatio(mediaPlayer, activity) ?: "Default"
                            showAspect = true;
                            onAction()
                        }) {
                            Icon(painterResource(R.drawable.fit_screen_24), null, tint = Color.White)
                        }
                    }

                    // CENTER CONTROLS (Playback)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.baseline_fast_rewind_24), null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { mediaPlayer.time -= 10000L; onAction() }
                        )
                        Spacer(Modifier.width(24.dp))
                        Box(
                            Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable {
                                    if (isPlaying) mediaPlayer.pause() else mediaPlayer.play()
                                    isPlaying = !isPlaying;
                                    onAction()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(if (isPlaying) R.drawable.pause_24 else R.drawable.play_arrow_24),
                                null, tint = Color.Black, modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.width(24.dp))
                        Icon(
                            painterResource(R.drawable.baseline_fast_forward_24), null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { mediaPlayer.time += 10000L; onAction() }
                        )
                    }

                    // RIGHT PILL (Tools)
                    ControlPill {
                        IconButton(onClick = { showSubtitle() ;onAction()}) {
                            Icon(painterResource(R.drawable.twotone_subtitles_24), null, tint = Color.White)
                        }
                        IconButton(onClick = {
                            vm.isPip.value = true
                            activity.enterPipMode(mediaPlayer.isPlaying);
                            onAction()
                        }) {
                            Icon(painterResource(R.drawable.baseline_picture_in_picture_alt_24), null, tint = Color.White)
                        }
                        IconButton(onClick = { togglePlaylist();onAction() }) {
                            Icon(painterResource(R.drawable.playlist_play_24), null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlPill(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(0.15f))
            .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(24.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun ModernOrientationButton(vm: MyViewModel) {
    val activity = LocalActivity.current as MainActivity
    val isOrientLock by vm.IsOrientLocked.collectAsState()
    IconButton(
        onClick = {
            toggleOrientation(activity, isOrientLock, vm)
            vm.IsOrientLocked.value = !isOrientLock;
        },
        modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)
    ) {
        Icon(
            painterResource(if (isOrientLock) R.drawable.twotone_screen_lock_landscape_24 else R.drawable.twotone_screen_rotation_24),
            null, tint = Color.White, modifier = Modifier.size(20.dp)
        )
    }
}

fun changeAspectRatio(mediaPlayer: MediaPlayer,activity: MainActivity): String? {
    val screenRatio = activity.getScreenRatio() // e.g., 20:9
    val screenRatioStr = "${screenRatio.first}:${screenRatio.second}"

    val (nextRatio, returnString) = when (mediaPlayer.aspectRatio) {
        null -> "16:9" to "16:9"
        "16:9" -> "4:3" to "4:3"
        "4:3" -> screenRatioStr to "Fit Screen"
        screenRatioStr -> "16:10" to "16:10" // Adding more common options
        else -> null to "Default"
    }
    mediaPlayer.aspectRatio = nextRatio
    return returnString
}