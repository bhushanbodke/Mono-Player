package com.example.monoplayer

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer

@Composable
fun BrightnessVolume(
    vm: MyViewModel,
    mediaPlayer: MediaPlayer,
    activity: MainActivity,
    isControlsVisible: Boolean,
    toggleControls: () -> Unit
) {

    var volumeState by rememberSaveable { mutableStateOf(mediaPlayer.volume) }
    var showVolume by rememberSaveable { mutableStateOf(false) }
    var showBrightness by rememberSaveable { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableLongStateOf(0L) }
    var validDrag by remember { mutableStateOf(true) }
    var isHolding by remember { mutableStateOf(false) }
    var addedTime by remember { mutableLongStateOf(0L) }
    var forward by remember { mutableStateOf(false) }
    var backward by remember { mutableStateOf(false) }
    var verticalSwipeAccumulator by remember { mutableStateOf(0f) }

    val savedBrightness by vm.currentBrightness.collectAsState()
    var currentBrightness by remember { mutableStateOf(savedBrightness) }

    LaunchedEffect(Unit) {
        if (savedBrightness >= 0f) {
            updateBrightness(activity, savedBrightness)
        }
    }


    LaunchedEffect(forward, backward) {
        if (forward) { delay(500); forward = false }
        if (backward) { delay(500); backward = false }
    }

    Box(Modifier.fillMaxSize()) {
        // --- TOUCH LAYER ---
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { toggleControls() },
                        onLongPress = {
                            mediaPlayer.setRate(2.0f)
                            isHolding = true
                        },
                        onDoubleTap = { offset ->
                            if (offset.x < size.width / 2) {
                                backward = true
                                mediaPlayer.time = (mediaPlayer.time - 10000L).coerceAtLeast(0L)
                            } else {
                                forward = true
                                mediaPlayer.time = (mediaPlayer.time + 10000L).coerceAtMost(mediaPlayer.length)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val edgeMargin = 40.dp.toPx()
                            validDrag = offset.x > edgeMargin && offset.x < (size.width - edgeMargin)
                            if (validDrag) {
                                seekPosition = mediaPlayer.time
                                addedTime = 0L
                                isSeeking = false
                                verticalSwipeAccumulator = 0f
                            }
                        },
                        onDragEnd = {
                            if (isSeeking) {
                                mediaPlayer.time = seekPosition
                                isSeeking = false
                            }
                            showVolume = false
                            showBrightness = false
                        },
                        onDrag = { change, dragAmount ->
                            if (!validDrag) return@detectDragGestures
                            change.consume()
                            val absX = Math.abs(dragAmount.x)
                            val absY = Math.abs(dragAmount.y)

                            if ((isSeeking || absX > absY + 15) && !showVolume && !showBrightness) {
                                isSeeking = true
                                val added = (dragAmount.x * 50).toLong()
                                addedTime += added
                                seekPosition = (mediaPlayer.time + addedTime).coerceIn(0L, mediaPlayer.length)
                            }
                            else if (!isSeeking) {
                                verticalSwipeAccumulator += dragAmount.y
                                if (Math.abs(verticalSwipeAccumulator) > 25f) {
                                    if (change.position.x > size.width / 2) {
                                        showVolume = true
                                        val audioManager = activity.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
                                        val direction = if (dragAmount.y < 0) android.media.AudioManager.ADJUST_RAISE else android.media.AudioManager.ADJUST_LOWER

                                        audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, direction, 0)

                                        val currentVol = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
                                        val maxSystemVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
                                        volumeState = (currentVol * 100) / maxSystemVol
                                    } else {
                                        showBrightness = true
                                        val newLevel = (currentBrightness - (dragAmount.y / 1000f)).coerceIn(0f, 1f)
                                        currentBrightness = newLevel
                                        updateBrightness(activity, newLevel)
                                        vm.updateSavedBrightness(newLevel)
                                    }
                                    verticalSwipeAccumulator = 0f
                                 }
                        }   }
                    )
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Release && isHolding) {
                                isHolding = false
                                mediaPlayer.setRate(1.0f)
                            }
                        }
                    }
                }
        )

        // --- UI OVERLAYS ---

        // Seeking Indicator
        AnimatedVisibility(
            visible = isSeeking,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Surface(
                color = Color.Black.copy(0.7f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.shadow(12.dp, RoundedCornerShape(16.dp))
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (addedTime >= 0) "+${formatTime(addedTime)}" else formatTime(addedTime),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = formatTime(seekPosition),
                        color = Color.White.copy(0.7f),
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Double Tap Indicators (Rewind/Forward)
        GestureIconOverlay(visible = backward, isForward = false, Modifier.align(Alignment.CenterStart).offset(x = 100.dp))
        GestureIconOverlay(visible = forward, isForward = true, Modifier.align(Alignment.CenterEnd).offset(x = (-100.dp)))

        // 2x Speed Indicator
        AnimatedVisibility(
            visible = isHolding,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp),
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CapsuleShape,
                modifier = Modifier.shadow(8.dp, CapsuleShape)
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.baseline_fast_forward_24), null, Modifier.size(20.dp), tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("2X SPEED", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }

        // Volume HUD
        HUDOverlay(
            visible = showVolume,
            icon = when(volumeState) {
                0 -> R.drawable.baseline_volume_off_24
                in 1..50 -> R.drawable.baseline_volume_down_24
                else -> R.drawable.baseline_volume_up_24
            },
            progress = volumeState / 100f,
            label = "$volumeState%",
            isControlsVisible = isControlsVisible,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Brightness HUD
        HUDOverlay(
            visible = showBrightness,
            icon = when((currentBrightness * 100).toInt()) {
                in 0..30 -> R.drawable.baseline_brightness_low_24
                in 31..70 -> R.drawable.baseline_brightness_medium_24
                else -> R.drawable.baseline_brightness_high_24
            },
            progress = currentBrightness,
            label = "${(currentBrightness * 100).toInt()}%",
            isControlsVisible = isControlsVisible,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun HUDOverlay(
    visible: Boolean,
    icon: Int,
    progress: Float,
    label: String,
    isControlsVisible: Boolean,
    modifier: Modifier
) {
    val topOffset by animateDpAsState(if (isControlsVisible) 70.dp else 40.dp)

    AnimatedVisibility(
        visible = visible,
        modifier = modifier.offset(y = topOffset),
        enter = fadeIn() + slideInVertically { -20 },
        exit = fadeOut() + slideOutVertically { -20 }
    ) {
        Surface(
            color = Color.Black.copy(0.6f),
            shape = CapsuleShape,
            modifier = Modifier.border(1.dp, Color.White.copy(0.1f), CapsuleShape)
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(icon), null, Modifier.size(20.dp), tint = Color.White)
                Spacer(Modifier.width(12.dp))
                Box(Modifier.width(150.dp).height(6.dp).clip(CapsuleShape).background(Color.White.copy(0.2f))) {
                    Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Color.White))
                }
                Spacer(Modifier.width(12.dp))
                Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
            }
        }
    }
}

@Composable
fun GestureIconOverlay(visible: Boolean, isForward: Boolean, modifier: Modifier) {
    AnimatedVisibility(visible = visible, modifier = modifier, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
        Box(
            modifier = Modifier.size(70.dp).background(Color.White.copy(0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(if (isForward) R.drawable.baseline_fast_forward_24 else R.drawable.baseline_fast_rewind_24),
                null, Modifier.size(40.dp), tint = Color.White
            )
        }
    }
}

val CapsuleShape = RoundedCornerShape(50)
fun updateBrightness(activity: Activity, level: Float) {
    val window = activity.window
    val layoutParams = window.attributes
    layoutParams.screenBrightness = level.coerceIn(0f, 1f)
    window.attributes = layoutParams
}