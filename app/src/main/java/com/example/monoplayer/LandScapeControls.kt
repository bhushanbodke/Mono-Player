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
import androidx.compose.material.icons.filled.Settings
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
    onDraggingChanged: (Boolean) -> Unit,
    showSettings: () -> Unit
) {
    val isModernUI  by vm.modernUI.collectAsState()
    var isDragging by remember { mutableStateOf(false) }
    var showAspect by rememberSaveable { mutableStateOf(false) }
    var showForward by rememberSaveable { mutableStateOf(false) }
    var showBackward by rememberSaveable { mutableStateOf(false) }
    var currentRatioText by remember { mutableStateOf("Default") }
    var isPlaying by remember { mutableStateOf(mediaPlayer.isPlaying) }
    val activity = LocalActivity.current as MainActivity

    LaunchedEffect(showAspect) {
        if (showAspect) {
            delay(1500)
            showAspect = false
        }
    }
    LaunchedEffect(showForward,showBackward) {
        if (showForward||showBackward) {
            delay(1000)
            showForward = false
            showBackward = false
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
                    modifier = Modifier.weight(1f),
                    text = video?.name ?: "Unknown Video",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(shadow = Shadow(Color.Black, Offset(2f, 2f), 4f))
                )
                IconButton(
                    onClick = {showSettings()},
                    modifier = Modifier.background(Color.Black.copy(0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.Settings, "back", tint = Color.White)
                }
            }

            // CENTER CONTROLS (Playback)
            Row(Modifier.align(Alignment.Center),verticalAlignment = Alignment.CenterVertically)
            {
                IconButton(
                    modifier=Modifier.size(60.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(0.3f), CircleShape),
                    onClick = { mediaPlayer.time -= 10000L; onAction(); showBackward = true; }) {
                    Icon(
                        painterResource(R.drawable.replay_10_24), null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(50.dp)
                    )
                }
                Spacer(Modifier.width(150.dp))
                Column() {
                    Icon(
                        painterResource(if (isPlaying) R.drawable.pause_24 else R.drawable.play_arrow_24),
                        null, tint = Color.White, modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(0.3f), CircleShape)
                            .clickable {
                                if (isPlaying) mediaPlayer.pause() else mediaPlayer.play()
                                isPlaying = !isPlaying;
                                onAction()
                            }
                    )
                }
                Spacer(Modifier.width(150.dp))
                IconButton(
                    modifier=Modifier.size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(0.3f), CircleShape),
                    onClick = { mediaPlayer.time += 10000L; onAction(); showForward = true; }) {
                    Icon(
                        painterResource(R.drawable.twotone_forward_10_24), null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(50.dp)
                    )
                }
            }


            // --- ASPECT RATIO INDICATOR ---
            if(showForward||showBackward){
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 70.dp),
                    color = Color.Black.copy(0.8f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (showForward) "+ 10" else "- 10", fontSize = 20.sp,fontWeight = FontWeight.Bold, color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                }
            }
            if (showAspect) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 70.dp),
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
                    .padding(bottom = 10.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
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
                        .padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    if(isModernUI){
                        modernUI(
                            vm,
                            mediaPlayer,
                            activity,
                            showLock,
                            showSubtitle,
                            togglePlaylist,
                            showAudioSelector,
                            onAction,
                            changeAspectRatio = {
                                currentRatioText =
                                    changeAspectRatio(mediaPlayer, activity) ?: "Default"
                                showAspect = true;
                            }
                        )
                    }
                    else{
                        classicUI(
                            vm,
                            mediaPlayer,
                            activity,
                            showLock,
                            showSubtitle,
                            togglePlaylist,
                            showAudioSelector,
                            onAction,
                            changeAspectRatio = {
                                currentRatioText =
                                    changeAspectRatio(mediaPlayer, activity) ?: "Default"
                                showAspect = true;
                            }
                        )
                    }
                }
            }
        }
    }
}







@Composable
fun ModernOrientationButton(vm: MyViewModel) {
    val activity = LocalActivity.current as MainActivity
    val isOrientLock by vm.IsOrientLocked.collectAsState()
    IconButton(
        onClick = {
            toggleOrientation(activity, isOrientLock, vm)
            vm.IsOrientLocked.value = !isOrientLock;
        },)
    {
        Icon(
            painterResource(if (isOrientLock) R.drawable.twotone_screen_lock_landscape_24 else R.drawable.twotone_screen_rotation_24),
            null, tint = Color.White
        )
    }
}

fun changeAspectRatio(mediaPlayer: MediaPlayer, activity: MainActivity): String {
    val screen = activity.getScreenRatio()
    val screenRatioStr = "${screen.first}:${screen.second}"

    val (nextRatio, nextScale, label) = when (mediaPlayer.aspectRatio) {
        null -> Triple(screenRatioStr, MediaPlayer.ScaleType.SURFACE_FILL, "Stretch")
        screenRatioStr -> Triple("FIT", MediaPlayer.ScaleType.SURFACE_BEST_FIT, "Fit to Screen")
        "FIT" -> Triple("ZOOM", MediaPlayer.ScaleType.SURFACE_FIT_SCREEN, "Zoom")
        else -> Triple(null, MediaPlayer.ScaleType.SURFACE_BEST_FIT, "Default")
    }

    mediaPlayer.aspectRatio = nextRatio
    mediaPlayer.videoScale = nextScale

    return label
}