package com.example.monoplayer

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.videolan.libvlc.MediaPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerControls(
    vm: MyViewModel,
    video: VideoModel?,
    mediaPlayer: MediaPlayer,
    togglePlaylist: () -> Unit,
    togglePlaylistFalse: () -> Unit,
) {
    val context = LocalContext.current
    val currentVideo = vm.currentVideo.collectAsState()
    var Display by remember { mutableStateOf(display.none) }
    var LockedControl by remember { mutableStateOf(false) }
    var controlResetTrigger by remember { mutableStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var subtitles by remember { mutableStateOf<Map<String, List<SubtitleLine>?>>(emptyMap()) }
    var currentSubtitles by remember { mutableStateOf("Disable") }

    val activity = LocalActivity.current as MainActivity
    val orientation = rememberOrientation()
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE

    BackHandler (){
        if(Display != display.none){
            Display = display.none
        }
        else vm.setScreen(Screens.Videos)
    }

    // Subtitle Loading Logic
    LaunchedEffect(currentVideo.value?.path) {
        val videoPath = currentVideo.value?.path ?: return@LaunchedEffect
        val results = withContext(Dispatchers.IO) {
            val tempMap = mutableMapOf<String, List<SubtitleLine>?>()
            tempMap["Disable"] = null
            findsubs(context, videoPath).forEach { subFile ->
                try {
                    tempMap[subFile.name] = parseSrt(subFile)
                } catch (e: Exception) {
                    Log.e("SUB_LOADER", "Failed to parse ${subFile.name}", e)
                }
            }
            tempMap
        }
        subtitles = results
    }

    // Auto-hide controls logic
    LaunchedEffect(Display, controlResetTrigger, isDragging) {
        if (Display == display.control && !isDragging) {
            delay(3000)
            Display = display.none
        }
        if(Display == display.lock){
            delay(3000)
            Display = display.none
        }
    }

    val pokeControls = {
        if (Display == display.none) Display = display.control else controlResetTrigger++
    }

    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(
            color = MaterialTheme.colorScheme.secondary,
            rippleAlpha = RippleAlpha(0.2f, 0.2f, 0.1f, 0.5f)
        )
    ) {
        Box(Modifier.fillMaxSize()) {
            SubtitleBox(vm, mediaPlayer, Display,subtitles[currentSubtitles])

            // Tap surface for showing/hiding main controls
            if (!LockedControl) {
                BrightnessVolume(vm, mediaPlayer, activity, Display == display.control, toggleControls = {
                    Display = if (Display == display.none) display.control else display.none
                    pokeControls()
                })
            } else {
                Box(Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTapGestures{ Display = display.lock }
                })
            }

            // --- ANIMATED OVERLAYS ---

            // 1. Main Player Controls (Fade In/Out)
            AnimatedVisibility(
                visible = Display == display.control,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (isLandscape) {

                    LandScapeControls(vm, video, mediaPlayer, true,
                        { Display = display.lock; LockedControl = true },
                        { Display = display.subtitles },
                        { Display = display.playlist; togglePlaylist() },
                        { Display = display.none },
                        { Display = display.audioSelector },
                        { pokeControls() },
                        { isDragging = it },
                        { Display = display.settings })
                } else {
                    PortraitControls(vm, video, mediaPlayer, true,
                        { Display = display.lock; LockedControl = true },
                        { Display = display.subtitles },
                        { Display = display.playlist; togglePlaylist() },
                        { Display = display.none },
                        { Display = display.audioSelector },
                        { pokeControls() },
                        { isDragging = it },
                        { Display = display.settings })
                }
            }

            // 2. Playlist Overlay (Slide up from Bottom)
            AnimatedVisibility(
                visible = Display == display.playlist,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    Display = display.control
                    togglePlaylist()
                }) {
                    Box(Modifier.align(Alignment.BottomCenter).height(250.dp).fillMaxWidth()) {
                        PlaylistP(vm)
                    }
                }
            }

            // 3. Subtitles Selector (Slide in from Right)
            AnimatedVisibility(
                visible = Display == display.subtitles,
                enter = scaleIn() ,
                exit = scaleOut()
            ) {
                subtitles(vm, mediaPlayer, video!!.path,subtitles, currentSubtitles,
                    {subtitles = it},
                    { currentSubtitles = it },
                    { mediaPlayer.play() ; Display = display.none; })
            }

            // 4. Audio Selector (Slide in from Right)
            AnimatedVisibility(
                visible = Display == display.audioSelector,
                enter = scaleIn() ,
                exit = scaleOut()
            ) {
                audioSelector(vm, mediaPlayer, { Display = display.control })
            }

            // 5. Settings Menu (Slide in from Right)
            AnimatedVisibility(
                visible = Display == display.settings,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                innerSettings(vm, { Display = display.control })
            }

            // 6. Lock Screen Icon (Fade + Scale)
            AnimatedVisibility(
                visible = Display == display.lock,
                enter =slideInVertically { it } ,
                exit = slideOutVertically {it}
            ) {
                Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { Display = display.none } }) {
                    IconButton(
                        onClick = { Display = display.control; LockedControl = false },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).size(60.dp)
                            .clip(CircleShape).background(MaterialTheme.colorScheme.surface.copy(0.6f))
                    ) {
                        Icon(painterResource(R.drawable.twotone_lock_24), "Unlock", tint = Color.White, modifier = Modifier.size(35.dp))
                    }
                }
            }

            // System UI Management
            LaunchedEffect(Display) {
                if (Display == display.none) {
                    hide_layout(activity)
                    togglePlaylistFalse()
                }
            }
        }
    }
}

@Composable
fun rememberOrientation(): Int {
    val configuration = LocalConfiguration.current
    var orientation by remember { mutableStateOf(configuration.orientation) }
    LaunchedEffect(configuration) {
        orientation = configuration.orientation
    }
    return orientation
}