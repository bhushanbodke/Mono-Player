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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer

@Composable
fun PlayerControls(
    vm: MyViewModel,
    video:VideoModel?,
    mediaPlayer: MediaPlayer
)
{
    var isLocked by rememberSaveable { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(false) }
    var ShowPlaylist by remember { mutableStateOf(false) }
    val activity = LocalActivity.current as MainActivity
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(showControls) {
        show_layout(activity,showControls,ShowPlaylist);
        if (showControls) {
            delay(3000) // Wait 3 seconds
            showControls = false // Hide them
            show_layout(activity,showControls,ShowPlaylist);
        }
    }
    Box(Modifier.fillMaxSize())
    {
        BrightnessVolume(mediaPlayer,activity,toggleControls = {
            showControls = !showControls
        })
        AnimatedVisibility(
            visible = ShowPlaylist,
            enter = if (isLandscape) slideInHorizontally { it } else slideInVertically { it },
            exit = if (isLandscape) slideOutHorizontally { it } else slideOutVertically { it },
            modifier = Modifier.align(if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter)
        ) {
            if (configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
                Box(
                    Modifier.width(500.dp).height(500.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}) {
                    Playlist(vm, ToggleShowControls = {
                        ShowPlaylist = false
                        showControls = true
                    })
                }
            } else {
                Box(
                    Modifier.fillMaxWidth().height(250.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}) {
                    PlaylistP(vm, ToggleShowControls = {
                        ShowPlaylist = false
                        showControls = true
                    })
                }
            }
        }
        if (showControls && !ShowPlaylist) {
                Text(
                    text = video?.name.toString(),
                    modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f)
                                )
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
                        Box(modifier = Modifier.weight(1f)) {
                            Spacer(modifier = Modifier.width(10.dp))
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
                                    .size(30.dp)
                                    .clickable {
                                        mediaPlayer.position -=0.01f
                                    }

                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Icon(
                                painter = painterResource(if (isPlaying) R.drawable.twotone_pause_circle_24 else R.drawable.twotone_play_circle_24),
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier
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
                                    .size(30.dp)
                                    .clickable {
                                        mediaPlayer.position -=0.01f
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
                                    .size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Icon(
                                painter = painterResource(R.drawable.twotone_list_24),
                                contentDescription = "playlist",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable {
                                        ShowPlaylist = !ShowPlaylist
                                    }
                            )
                        }
                    }
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

fun show_layout(activity: MainActivity,show:Boolean,showPlaylist:Boolean){
    activity?.let {
        val window = it.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (show && !showPlaylist) controller.show(WindowInsetsCompat.Type.systemBars())
        else controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}




fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}