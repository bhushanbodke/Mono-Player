package com.example.monoplayer

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerControls(
    vm: MyViewModel,
    video:VideoModel?,
    mediaPlayer: MediaPlayer,
    togglePlaylist:()->Unit,
    togglePlaylistFalse:()->Unit,
)
{
    var isLocked by rememberSaveable { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var Display by remember { mutableStateOf(display.none) }
    var LockedControl by remember { mutableStateOf(false) }
    var brightness by rememberSaveable { mutableStateOf(-1f) }

    val activity = LocalActivity.current as MainActivity
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Display) {
        if (Display !=display.none && Display != display.playlist && Display != display.subtitles) {
            delay(3000)
            Display = display.none
        }
    }

    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(
            color = MaterialTheme.colorScheme.secondary,
            rippleAlpha = RippleAlpha(0.2f, 0.2f, 0.1f, 0.5f)
        )
    ) {

        Box(Modifier.fillMaxSize()) {
            if (!LockedControl) {
                BrightnessVolume(mediaPlayer,brightness, activity, toggleControls = {
                    Display = if (Display == display.none) display.control else display.none
                })
            } else {
                Box(Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { Display = display.lock }
                    })
            }
            when (Display) {
                display.none -> {
                    hide_layout(activity);
                    togglePlaylistFalse();
                }
                display.control ->
                {
                        Text(
                            text = video?.name.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.5f), Color.Transparent
                                        )
                                    )
                                )
                                .padding(
                                    top = 40.dp,
                                    start = 50.dp,
                                    end = 50.dp
                                ),
                            color = Color.White, // Add color so it's visible on a dark video
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            if (change.pressed) {
                                                change.consume()
                                            }
                                        }
                                    )
                                }
                        )
                        {
                            VideoProgressSlider(
                                mediaPlayer,
                                isDraggingExternal = isDragging,
                                onSeek = {
                                    isDragging = true
                                    mediaPlayer.position = it
                                },
                                onFinished = {
                                    isDragging = false
                                })
                            // buttons on bottom
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                                    .fillMaxWidth()
                                    .height(75.dp)
                            )
                            {
                                Row(
                                    modifier = Modifier.weight(1f), // Give the center more "space"
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OrientationButton(
                                        isLocked = isLocked,
                                        onToggleLock = {
                                            if (!isLocked) {
                                                activity?.requestedOrientation =
                                                    ActivityInfo.SCREEN_ORIENTATION_LOCKED
                                                isLocked = true
                                            } else {
                                                activity?.requestedOrientation =
                                                    ActivityInfo.SCREEN_ORIENTATION_USER
                                                isLocked = false
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(20.dp))
                                        Icon(
                                            painterResource(R.drawable.twotone_lock_24),
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    Display = display.lock
                                                    LockedControl = true;
                                                },
                                            contentDescription = "Lock",
                                            tint = Color.White,
                                        )

                                }
                                Row(
                                    modifier = Modifier.weight(3f), // Give the center more "space"
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        painter = painterResource(R.drawable.baseline_replay_10_24),
                                        contentDescription = "back",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .size(30.dp)
                                            .clickable {
                                                val targetTime = mediaPlayer.time + 10000L
                                                mediaPlayer.time = targetTime.coerceAtLeast(0L)
                                            }

                                    )
                                    Spacer(modifier = Modifier.width(30.dp))
                                    Icon(
                                        painter = painterResource(if (isPlaying) R.drawable.twotone_pause_circle_24 else R.drawable.twotone_play_circle_24),
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .size(50.dp)
                                            .clickable {
                                                isPlaying = !isPlaying
                                                if (isPlaying) {
                                                    mediaPlayer.play()
                                                } else {
                                                    mediaPlayer.pause()
                                                }
                                            })
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Icon(
                                        painter = painterResource(R.drawable.twotone_forward_10_24),
                                        contentDescription = "back",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .size(30.dp)
                                            .clickable {
                                                val targetTime = mediaPlayer.time - 10000L
                                                mediaPlayer.time = targetTime.coerceAtLeast(0L)
                                            }
                                    )
                                }
                                Row(
                                    modifier = Modifier.weight(1f), // Give the center more "space"
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.twotone_subtitles_24),
                                        contentDescription = "back",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .size(30.dp)
                                            .clickable { Display = display.subtitles }
                                    )
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Icon(
                                        painter = painterResource(R.drawable.twotone_list_24),
                                        contentDescription = "playlist",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .size(30.dp)
                                            .clickable {
                                                Display = display.playlist
                                                togglePlaylist();
                                            }
                                    )
                                }
                            }
                        }
                    }
                display.playlist->
                    {
                        Box(Modifier.fillMaxSize().clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { Display = display.control
                            togglePlaylist()})
                        {
                        AnimatedVisibility(
                            visible = Display == display.playlist,
                            enter =  slideInVertically { it },
                            exit =  slideOutVertically { it },
                            modifier = Modifier.align(Alignment.BottomCenter).height(180.dp).fillMaxWidth()
                        ) {  PlaylistP(vm) }
                        }
                }
                display.lock ->
                    {
                    Box(Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { Display = display.none; }
                        }) {
                        Box(Modifier
                            .size(50.dp)
                            .align(Alignment.CenterStart)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(0.5f)))
                        {
                            Icon(
                                painter = painterResource(R.drawable.twotone_lock_24),
                                modifier = Modifier
                                    .padding(5.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { Display = display.control; LockedControl = false },
                                contentDescription = "Lock",
                                tint = Color.White
                            )
                        }
                    }
                }
                display.subtitles ->
                    subtitles(vm,mediaPlayer,{Display = display.none})
                else->{}

            }
        }
    }
}


@Composable
fun OrientationButton(
    isLocked: Boolean,
    onToggleLock: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Icon(
        painter = painterResource(
            id = when {
                isLocked && isLandscape -> R.drawable.twotone_screen_lock_landscape_24
                isLocked -> R.drawable.twotone_screen_lock_portrait_24
                isLandscape -> R.drawable.twotone_stay_primary_landscape_24
                else -> R.drawable.twotone_stay_primary_portrait_24
            }
        ),
        modifier = Modifier
            .size(30.dp)
            .clickable { onToggleLock() },
        contentDescription = "Orientation Lock",
        tint = Color.White
    )
}


fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}