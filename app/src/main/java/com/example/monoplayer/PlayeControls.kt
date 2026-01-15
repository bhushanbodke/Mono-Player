package com.example.monoplayer

import android.content.Context
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
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
    var Display by remember { mutableStateOf(display.none) }
    var LockedControl by remember { mutableStateOf(false) }
    var brightness by rememberSaveable { mutableStateOf(-1f) }


    val activity = LocalActivity.current as MainActivity
    val configuration = LocalConfiguration.current
    val orientation = rememberOrientation()
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE



    LaunchedEffect(Display) {
        when (Display) {
            display.control, display.lock -> {
                // Auto-hide controls or lock icon after 3 seconds
                delay(3000)
                Display = display.none
            }
            display.playlist, display.subtitles, display.audioSelector, display.none -> {
            }
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
                BrightnessVolume(mediaPlayer, activity,Display== display.control,toggleControls = {
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
                    if (isLandscape) {
                        LandScapeControls(vm,video,mediaPlayer,Display== display.control
                            ,{Display =display.lock;LockedControl = true}
                            ,{Display = display.subtitles}
                            ,{Display = display.playlist;togglePlaylist();}
                            ,toggleShow = {Display =  display.none}
                            ,showAudioSelector = {Display = display.audioSelector})
                    } else {
                        PortraitControls(vm,video,mediaPlayer,Display== display.control
                            ,{Display =display.lock;LockedControl = true}
                            ,{Display = display.subtitles}
                            ,{Display = display.playlist;togglePlaylist();}
                            ,toggleShow = {Display =  display.none}
                            ,showAudioSelector = {Display = display.audioSelector})
                        }
                    }
                display.playlist->
                    {
                        Box(Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                Display = display.control
                                togglePlaylist()
                            })
                        {
                        AnimatedVisibility(
                            visible = Display == display.playlist,
                            enter =  slideInVertically { it },
                            exit =  slideOutVertically { it },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .height(180.dp)
                                .fillMaxWidth()
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
                            IconButton(onClick = {Display = display.control; LockedControl = false }) {
                                Icon(
                                    painter = painterResource(R.drawable.twotone_lock_24),
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentDescription = "Lock",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                display.subtitles -> {mediaPlayer.pause();
                    subtitles(vm, mediaPlayer, { Display = display.none; mediaPlayer.play() })
                }
                display.audioSelector -> audioSelector(vm,mediaPlayer,{Display = display.control})
                else->{}

            }
        }
    }
}



@Composable
fun rememberOrientation(): Int {
    val configuration = LocalConfiguration.current
    var orientation by remember { mutableStateOf(configuration.orientation) }

    // This updates whenever the configuration changes
    LaunchedEffect(configuration) {
        orientation = configuration.orientation
    }

    return orientation
}
