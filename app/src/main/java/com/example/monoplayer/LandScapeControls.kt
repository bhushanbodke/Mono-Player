package com.example.monoplayer

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
    showAudioSelector: () -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var showAspect by rememberSaveable { mutableStateOf(false) }
    var currentRatioText by remember { mutableStateOf("Default") }
    var isPlaying by remember { mutableStateOf(true) }
    val activity = LocalActivity.current as MainActivity

    LaunchedEffect(showAspect){
        if(showAspect){delay(1000);showAspect = false}
    }

    Box(Modifier.fillMaxSize())
    {
        // --- GESTURE LAYER (Double tap sides to seek) ---
        BrightnessVolume(mediaPlayer, activity, isControlsVisible,toggleControls = {
           toggleShow()
        })
        // --- TOP TITLE BAR ---
        Row(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick = { vm.setScreen(Screens.Videos) })
            {
                Icon(
                    imageVector = Icons.Default.ArrowBack,contentDescription = "back",
                    Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = video?.name.toString(),
                modifier = Modifier
                    .fillMaxWidth()
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
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.6f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
                )
            )
        }
        Column(Modifier.align(Alignment.CenterEnd).offset(x=(-50.dp),y=0.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
            .padding(vertical = 5.dp)){
            ModernOrientationButton(vm)
            IconButton(onClick = {}) {
                Icon(
                    painterResource( R.drawable.headphones_24),
                    contentDescription = "background play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }

        }
        if(showAspect){
            Box(
                Modifier
                    .size(120.dp, 60.dp) // Slightly wider for ratios like 16:9
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.7f)), // Darker background for visibility
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentRatioText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
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
                vm,
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
                    .padding(horizontal = 32.dp) // More side padding for landscape
                    .height(80.dp)
            ) {
                // LEFT SECTION: Locking & Orientation
                Row(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lock Button
                    IconButton(onClick = showLock) {
                        Icon(
                            painter = painterResource(R.drawable.twotone_lock_24),
                            contentDescription = "Lock Controls",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Music/Audio Track Button
                    IconButton(onClick = { showAudioSelector()}) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_music_note_24),
                            contentDescription = "Audio Tracks",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Aspect Ratio Button
                    IconButton(onClick = {
                        val newRatio = changeAspectRatio(mediaPlayer, activity)
                        currentRatioText = newRatio ?: "Default"
                        showAspect = true
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.fit_screen_24),
                            contentDescription = "Aspect Ratio",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // CENTER SECTION: Playback
                Row(
                    modifier = Modifier.weight(3f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier=Modifier.size(50.dp,45.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(0.6f))
                        .clickable(){ mediaPlayer.time = (mediaPlayer.time - 10000L).coerceAtLeast(0L) }
                    ,contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.baseline_fast_rewind_24)
                             ,modifier= Modifier.fillMaxSize()
                            , contentDescription = null
                            , tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    Box(modifier=Modifier.size(80.dp,60.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(0.6f))
                        .clickable{
                        isPlaying = !isPlaying
                        if (isPlaying) mediaPlayer.play() else mediaPlayer.pause()
                    } ,contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.play_arrow_24 else R.drawable.pause_24),
                            contentDescription = "null",
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    Box(modifier=Modifier.size(50.dp,45.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(0.6f))
                        .clickable(){ mediaPlayer.time += 10000L }
                        ,contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.baseline_fast_forward_24)
                            , modifier= Modifier.fillMaxSize()
                            , contentDescription = null
                            , tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // RIGHT SECTION: Media Options
                Row(
                    modifier = Modifier
                        .weight(1.2f)
                        // 1. Unified container styling
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(30.dp)
                        )
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Subtitles Button
                    IconButton(onClick = showSubtitle) {
                        Icon(
                            painter = painterResource(R.drawable.twotone_subtitles_24),
                            contentDescription = "Subtitles",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // PiP Button
                    IconButton(onClick = {
                        vm.isPip.value = true
                        activity.enterPipMode(mediaPlayer.isPlaying)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_picture_in_picture_alt_24),
                            contentDescription = "Picture in Picture",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Playlist Button
                    IconButton(onClick = togglePlaylist) {
                        Icon(
                            painter = painterResource(R.drawable.playlist_play_24),
                            contentDescription = "Playlist",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernOrientationButton(vm: MyViewModel, modifier: Modifier = Modifier) {
    val activity = LocalActivity.current as MainActivity
    val isOrientLock = vm.IsOrientLocked.collectAsState()
    IconButton (onClick = {
        toggleOrientation(activity,isOrientLock.value,vm)
        vm.IsOrientLocked.value = !vm.IsOrientLocked.value
    }) {
        Icon(
            painterResource( if (isOrientLock.value) R.drawable.twotone_screen_lock_landscape_24 else R.drawable.twotone_screen_rotation_24),
            contentDescription = "Orientation",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
        )
    }
}

fun changeAspectRatio(mediaPlayer: MediaPlayer,activity: MainActivity): String? {
    val screenRatio = activity.getScreenRatio()
    val nextRatio = when (mediaPlayer.aspectRatio) {
        null -> "16:9"
        "16:9" -> "4:3"
        "4:3" -> screenRatio // This is your "Fit to Screen / Fill"
        else -> null // Back to "Default"
    }
    mediaPlayer.aspectRatio = nextRatio
    return if(nextRatio==screenRatio) "Fit" else nextRatio
}