package com.example.monoplayer



import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.videolan.libvlc.MediaPlayer
@Composable
fun PortraitControls(
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
        // --- TOP TITLE ---
        Text(
            text = video?.name ?: "Unknown Video",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(0.7f), Color.Transparent)
                    )
                )
                .padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 40.dp),
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            maxLines = 2
        )

        // --- BOTTOM CONTROLS ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(0.7f))
                    )
                )
                .padding(bottom = 24.dp) // Extra padding for system navigation bars
        ) {
            // 1. Slider
            VideoProgressSlider(
                mediaPlayer,
                isDraggingExternal = isDragging,
                onSeek = {
                    isDragging = true
                    mediaPlayer.position = it
                },
                onFinished = { isDragging = false }
            )

            // 2. Playback Buttons (Replay, Play/Pause, Forward)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { mediaPlayer.time = (mediaPlayer.time - 10000L).coerceAtLeast(0L) }) {
                    Icon(painterResource(R.drawable.baseline_replay_10_24), null, Modifier.size(35.dp), tint = Color.White)
                }
                Spacer(Modifier.width(40.dp))
                IconButton(onClick = {
                    isPlaying = !isPlaying
                    if (isPlaying) mediaPlayer.play() else mediaPlayer.pause()
                }) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.twotone_pause_circle_24 else R.drawable.twotone_play_circle_24),
                        null, Modifier.size(65.dp), tint = Color.White
                    )
                }
                Spacer(Modifier.width(40.dp))
                IconButton(onClick = { mediaPlayer.time = mediaPlayer.time + 10000L }) {
                    Icon(painterResource(R.drawable.twotone_forward_10_24), null, Modifier.size(35.dp), tint = Color.White)
                }
            }

            // 3. Utility Buttons (Lock, Subs, PiP, Playlist)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrientationButton(
                    isLocked = isLocked,
                    onToggleLock = {
                        isLocked = !isLocked
                        activity.requestedOrientation = if (isLocked) ActivityInfo.SCREEN_ORIENTATION_LOCKED else ActivityInfo.SCREEN_ORIENTATION_USER
                    }
                )
                IconButton(onClick = showLock) {
                    Icon(painterResource(R.drawable.twotone_lock_24), null, Modifier.size(28.dp), tint = Color.White)
                }
                IconButton(onClick = showSubtitle) {
                    Icon(painterResource(R.drawable.twotone_subtitles_24), null, Modifier.size(28.dp), tint = Color.White)
                }
                IconButton(onClick = { }) {
                    Icon(painterResource(R.drawable.baseline_music_note_24), null, Modifier.size(28.dp), tint = Color.White)
                }
                IconButton(onClick = { vm.isPip.value = true; activity.enterPipMode(mediaPlayer.isPlaying) }) {
                    Icon(painterResource(R.drawable.baseline_picture_in_picture_alt_24), null, Modifier.size(28.dp), tint = Color.White)
                }
                IconButton(onClick = togglePlaylist) {
                    Icon(painterResource(R.drawable.twotone_list_24), null, Modifier.size(28.dp), tint = Color.White)
                }
            }
        }
    }
}