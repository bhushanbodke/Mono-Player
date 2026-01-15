package com.example.monoplayer



import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    showAudioSelector: () -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var showAspect by rememberSaveable { mutableStateOf(false) }
    var currentRatioText by remember { mutableStateOf("Default") }
    val activity = LocalActivity.current as MainActivity

    // Aspect Ratio Indicator Auto-hide
    LaunchedEffect (showAspect) {
        if (showAspect) {
            delay(1000)
            showAspect = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        // --- GESTURE LAYER ---
        BrightnessVolume(mediaPlayer, activity, isControlsVisible, toggleControls = {
            toggleShow()
        })

        // --- TOP TITLE BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
                .padding(top = 40.dp, start = 10.dp, end = 10.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.setScreen(Screens.Videos) }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back",
                    modifier = Modifier.size(30.dp),
                    tint = Color.White
                )
            }
            Text(
                text = video?.name ?: "Unknown Video",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Medium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(shadow = Shadow(Color.Black.copy(0.6f), Offset(2f, 2f), 4f))
            )
        }

        // --- ASPECT RATIO INDICATOR ---
        if (showAspect) {
            Box(
                Modifier
                    .size(120.dp, 60.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = currentRatioText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // --- BOTTOM CONTROLS ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
                .padding(bottom = 32.dp)
        ) {
            VideoProgressSlider(
                vm, mediaPlayer,
                isDraggingExternal = isDragging,
                onSeek = { isDragging = true; mediaPlayer.position = it },
                onFinished = { isDragging = false }
            )

            // CENTER SECTION: Playback Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(35.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(35.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { mediaPlayer.time = (mediaPlayer.time - 10000L).coerceAtLeast(0L) }) {
                        Icon(painterResource(R.drawable.baseline_fast_rewind_24), null, Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(20.dp))
                    IconButton(onClick = {
                        isPlaying = !isPlaying
                        if (isPlaying) mediaPlayer.play() else mediaPlayer.pause()
                    }) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause_24 else R.drawable.play_arrow_24),
                            null, modifier = Modifier.size(45.dp), tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                    IconButton(onClick = { mediaPlayer.time += 10000L }) {
                        Icon(painterResource(R.drawable.baseline_fast_forward_24), null, Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // UTILITY SECTION: Bottom Row Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Utilities Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                        .padding(horizontal = 8.dp)
                ) {
                    IconButton(onClick = showLock) {
                        Icon(painterResource(R.drawable.twotone_lock_24), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        val newRatio = changeAspectRatio(mediaPlayer, activity)
                        currentRatioText = newRatio ?: "Default"
                        showAspect = true
                    }) {
                        Icon(painterResource(R.drawable.fit_screen_24), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // Orientation & Headphones Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                        .padding(horizontal = 8.dp)
                ) {
                    ModernOrientationButton(vm)
                    IconButton(onClick = {}) {
                        Icon(painterResource(R.drawable.headphones_24), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // Right Media Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                        .padding(horizontal = 8.dp)
                ) {
                    IconButton(onClick = showSubtitle) {
                        Icon(painterResource(R.drawable.twotone_subtitles_24), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = {
                        vm.isPip.value = true
                        activity.enterPipMode(mediaPlayer.isPlaying)
                    }) {
                        Icon(painterResource(R.drawable.baseline_picture_in_picture_alt_24), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = togglePlaylist) {
                        Icon(painterResource(R.drawable.playlist_play_24), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}