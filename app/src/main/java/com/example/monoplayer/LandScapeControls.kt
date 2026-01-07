package com.example.monoplayer

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.videolan.libvlc.MediaPlayer

@Composable
fun LandScapeControls(
    vm: MyViewModel,
    video: VideoModel?,
    mediaPlayer: MediaPlayer,
    showLock: () -> Unit,
    showSubtitle: () -> Unit,
    togglePlaylist: () -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var isLocked by rememberSaveable { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    val activity = LocalActivity.current as MainActivity

    Box(Modifier.fillMaxSize()) {
        // --- GESTURE LAYER (Double tap sides to seek) ---
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures (
                        onDoubleTap = { offset ->
                            val isRightSide = offset.x > size.width / 2
                            if (isRightSide) {
                                mediaPlayer.time += 10000L
                            } else {
                                mediaPlayer.time = (mediaPlayer.time - 10000L).coerceAtLeast(0L)
                            }
                        }
                    )
                }
        )

        // --- TOP TITLE BAR ---
        Text(
            text = video?.name.toString(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
                .padding(top = 24.dp, start = 60.dp, end = 60.dp, bottom = 20.dp),
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // --- BOTTOM CONTROLS ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
        ) {
            VideoProgressSlider(
                mediaPlayer,
                isDraggingExternal = isDragging,
                onSeek = {
                    isDragging = true
                    mediaPlayer.position = it
                },
                onFinished = { isDragging = false }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp) // More side padding for landscape
                    .height(80.dp)
            ) {
                // LEFT SECTION: Locking & Orientation
                Row(
                    modifier = Modifier.weight(1.2f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModernOrientationButton(vm)
                    Spacer(modifier = Modifier.width(24.dp))
                    IconButton(onClick = showLock) {
                        Icon(painterResource(R.drawable.twotone_lock_24), null, Modifier.size(28.dp), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    IconButton(onClick = { /* Handle Music Button Click */ }) {
                        Icon(painterResource(R.drawable.baseline_music_note_24), null, Modifier.size(28.dp), tint = Color.White)
                    }

                }

                // CENTER SECTION: Playback
                Row(
                    modifier = Modifier.weight(3f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { mediaPlayer.time = (mediaPlayer.time - 10000L).coerceAtLeast(0L) }) {
                        Icon(painterResource(R.drawable.baseline_replay_10_24), null, Modifier.size(32.dp), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    IconButton(onClick = {
                        isPlaying = !isPlaying
                        if (isPlaying) mediaPlayer.play() else mediaPlayer.pause()
                    }) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.twotone_pause_circle_24 else R.drawable.twotone_play_circle_24),
                            null, Modifier.size(60.dp), tint = Color.White // Larger Play Button
                        )
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    IconButton(onClick = { mediaPlayer.time += 10000L }) {
                        Icon(painterResource(R.drawable.twotone_forward_10_24), null, Modifier.size(32.dp), tint = Color.White)
                    }
                }

                // RIGHT SECTION: Media Options
                Row(
                    modifier = Modifier.weight(1.2f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = showSubtitle) {
                        Icon(painterResource(R.drawable.twotone_subtitles_24), null, Modifier.size(28.dp), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = {
                        vm.isPip.value = true
                        activity.enterPipMode(mediaPlayer.isPlaying)
                    }) {
                        Icon(painterResource(R.drawable.baseline_picture_in_picture_alt_24), null, Modifier.size(28.dp), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = togglePlaylist) {
                        Icon(painterResource(R.drawable.twotone_list_24), null, Modifier.size(28.dp), tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernOrientationButton(vm: MyViewModel, modifier: Modifier = Modifier) {
    val activity = LocalActivity.current as MainActivity
    // rememberSaveable survives configuration changes!
    val isOrientLock = vm.IsOrientLocked.collectAsState()

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .clickable {
                toggleOrientation(activity,isOrientLock.value,vm)
                vm.IsOrientLocked.value = !vm.IsOrientLocked.value
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource( if (isOrientLock.value) R.drawable.twotone_screen_lock_landscape_24 else R.drawable.twotone_screen_rotation_24),
            contentDescription = "Orientation",
            tint = if (isOrientLock.value) MaterialTheme.colorScheme.primary else Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}