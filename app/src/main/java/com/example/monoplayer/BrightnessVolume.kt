package com.example.monoplayer

import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    mediaPlayer: MediaPlayer,
    activity: MainActivity,
    toggleControls: () -> Unit
) {
    val originalBrightness = remember {
        val current = activity.window.attributes.screenBrightness
        if (current < 0) 0.5f else current
    }
    var brightness by rememberSaveable { mutableStateOf(originalBrightness) }
    var volumeState by rememberSaveable { mutableStateOf(mediaPlayer.volume) }
    var ShowVolume by rememberSaveable { mutableStateOf(false) }
    var ShowBrightness by rememberSaveable { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableLongStateOf(0L) }
    var isHolding by remember { mutableStateOf(false) }
    var AddedTime by remember { mutableLongStateOf(0L) }
    var orientation by remember { mutableStateOf(activity.requestedOrientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) }


    DisposableEffect(Unit) {
        onDispose { updateBrightness(activity, originalBrightness) }
    }
    LaunchedEffect(activity.requestedOrientation) {
        orientation = activity.requestedOrientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    Box(Modifier.fillMaxSize()) {
        // --- TOUCH LAYER ---
        Box(
            Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { toggleControls() },
                        onLongPress = {
                            mediaPlayer.setRate(2.0f)
                            isHolding = true;
                        })
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            // Detect finger lift
                            if (event.type == PointerEventType.Release && isHolding) {
                                isHolding = false
                                mediaPlayer.setRate(1.0f)
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            // Reset flags
                            isSeeking = false
                        },
                        onDragEnd = {
                            if (isSeeking) {
                                mediaPlayer.time = seekPosition // Apply final seek to VLC
                                isSeeking = false
                            }
                            ShowVolume = false
                            ShowBrightness = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val absX = Math.abs(dragAmount.x)
                            val absY = Math.abs(dragAmount.y)

                            if (isSeeking || absX > absY) {
                                isSeeking = true
                                val added = (dragAmount.x * 100).toLong()
                                AddedTime += added
                                val totalDuration = mediaPlayer.length
                                seekPosition = (seekPosition + added).coerceIn(0L, if (totalDuration > 0) totalDuration else Long.MAX_VALUE)
                                mediaPlayer.time = seekPosition
                            }else if (!isSeeking) {
                                if (change.position.x < size.width / 2) {
                                    ShowVolume = true
                                    val newVol = (volumeState + (if (dragAmount.y < 0) 2 else -2)).coerceIn(0, 200)
                                    mediaPlayer.volume = newVol
                                    volumeState = newVol
                                } else {
                                    ShowBrightness = true
                                    brightness = (brightness - (dragAmount.y / 1000f)).coerceIn(0f, 1f)
                                    updateBrightness(activity, brightness)
                                }
                            }
                        }
                    )
                }
        )
        AnimatedVisibility(
            visible = isSeeking,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn()+ expandHorizontally(),
            exit = fadeOut()+ shrinkHorizontally()
        ) {
            Box(
                Modifier.clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .padding(20.dp)
            ) {
                Text(
                    text = "+${formatTime(AddedTime)}(${formatTime(seekPosition)})", // Helper function for MM:SS
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.size(20.dp))
        AnimatedVisibility(
            visible = isHolding,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = fadeIn()+ expandHorizontally(),
            exit = fadeOut()+ shrinkHorizontally()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier=Modifier.clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .padding(20.dp)
            ) {
                Text(
                    text = "2x", // Helper function for MM:SS
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(painter = painterResource(R.drawable.baseline_fast_forward_24)
                    , contentDescription = "fastfor"
                    ,modifier=Modifier.size(30.dp)
                    , tint = MaterialTheme.colorScheme.primary)
            }
        }
        AnimatedVisibility(
            visible = ShowVolume,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = fadeIn()+ expandHorizontally(),
            exit = fadeOut()+ shrinkHorizontally()
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = if(orientation) 20.dp else 50.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Box(Modifier.padding(start = 8.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface.copy(0.5f))) {
                    Text("$volumeState% Vol", fontSize = 12.sp, modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IndicatorLabel("200")
                    VerticalBar(progress = volumeState / 200f)
                    IndicatorLabel("0")
                }
            }
        }
        AnimatedVisibility(
            visible = ShowBrightness,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = fadeIn()+ expandHorizontally(),
            exit = fadeOut()+shrinkHorizontally()
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = if(orientation) 20.dp else 50.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IndicatorLabel("100%")
                    VerticalBar(progress = brightness)
                    IndicatorLabel("0%")
                }
                Box(
                    Modifier.padding(end = 8.dp).clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(0.5f))
                ) {
                    Text(
                        "${(brightness * 100).toInt()}% Bright",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun VerticalBar(progress: Float) {
    Box(
        Modifier.padding(vertical = 8.dp).height(175.dp).width(10.dp)
            .clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            Modifier.fillMaxWidth().fillMaxHeight(progress.coerceIn(0f, 1f))
                .align(Alignment.BottomCenter).background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun IndicatorLabel(text: String) {
    Box(Modifier.width(45.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface.copy(0.5f))) {
        Text(text = text, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
    }
}
fun updateBrightness(activity:MainActivity, level: Float) {
    val params = activity?.window?.attributes
    params?.screenBrightness = level.coerceIn(0f, 1f)
    activity?.window?.attributes = params

}

