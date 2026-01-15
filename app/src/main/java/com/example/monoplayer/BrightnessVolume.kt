package com.example.monoplayer

import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
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
    mediaPlayer: MediaPlayer,
    activity: MainActivity,
    isControlsVisible: Boolean,
    toggleControls: () -> Unit
) {

    var currentBrightness by remember {
        val initial = activity.window.attributes.screenBrightness
        mutableStateOf(if (initial < 0) 0.5f else initial)
    }
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
    var currentScale by remember { mutableStateOf(1f) }
    var displayScale by remember { mutableStateOf("") }
    var showAspect by remember { mutableStateOf(false) }
    var gestureType by remember { mutableStateOf("NONE") }

    var orientation by remember { mutableStateOf(activity.requestedOrientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) }


    DisposableEffect(Unit) {
        onDispose { updateBrightness(activity, currentBrightness) }
    }
    LaunchedEffect(activity.requestedOrientation) {
        orientation = activity.requestedOrientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    LaunchedEffect(forward,backward) {
        if(forward){delay(500);forward = false}
        if(backward){delay(500);backward = false}
    }

    Box(Modifier.fillMaxSize()) {
        // --- TOUCH LAYER ---
        Box(
            Modifier
                .fillMaxSize()
                // BLOCK 1: Handles Taps (Play/Pause Toggle, Long Press 2x, Double Tap Seek)
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
                // BLOCK 2: Handles Continuous Dragging (Volume, Brightness, Seek)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Prevent accidental triggers near the screen edges
                            val edgeMargin = 40.dp.toPx()
                            validDrag = offset.x > edgeMargin && offset.x < (size.width - edgeMargin)

                            if (validDrag) {
                                seekPosition = mediaPlayer.time
                                addedTime = 0L
                                isSeeking = false
                            }
                        },
                        onDragEnd = {
                            // If we were seeking, apply the final time change now
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

                            // 1. HORIZONTAL DRAG (Seeking)
                            // Triggered if movement is primarily horizontal or we are already seeking
                            if ((isSeeking || absX > absY + 5) && !showVolume && !showBrightness) {
                                isSeeking = true
                                val added = (dragAmount.x * 50).toLong()
                                addedTime += added
                                // We update the local variable for UI feedback
                                seekPosition = (mediaPlayer.time + addedTime).coerceIn(0L, mediaPlayer.length)
                            }

                            // 2. VERTICAL DRAG (Volume & Brightness)
                            else if (!isSeeking) {
                                if (change.position.x > size.width / 2) {
                                    // Right Side: Volume
                                    showVolume = true
                                    volumeState = (volumeState + (if (dragAmount.y < 0) 2 else -2)).coerceIn(0, 200)
                                    mediaPlayer.volume = volumeState
                                } else {
                                    // Left Side: Brightness
                                    showBrightness = true
                                    currentBrightness = (currentBrightness - (dragAmount.y / 1000f)).coerceIn(0f, 1f)
                                    updateBrightness(activity, currentBrightness)
                                }
                            }
                        }
                    )
                }
                // BLOCK 3: Monitors finger release to stop 2x Fast Forward
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
                    text = "+${formatTime(addedTime)}(${formatTime(seekPosition)})", // Helper function for MM:SS
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        AnimatedVisibility(
            visible = backward,
            modifier = Modifier.align(Alignment.CenterStart).offset(200.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Spacer(modifier = Modifier.width(40.dp))
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape) // Shadow follows the circle
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_fast_rewind_24),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "rewind",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
        AnimatedVisibility(
            visible = forward,
            modifier = Modifier.align(Alignment.CenterEnd).offset((-200.dp)),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape) // Shadow follows the circle
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_fast_forward_24),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "forword",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(40.dp))
            }
        }


        AnimatedVisibility(
            visible = isHolding,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn()+ expandHorizontally(),
            exit = fadeOut()+ shrinkHorizontally(),

        ) {
            Spacer(modifier = Modifier.width(20.dp))
            Box(
                modifier = Modifier
                    .shadow(elevation = 8.dp, shape = CircleShape) // Shadow follows the circle
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape)
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "2x", // Helper function for MM:SS
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        painter = painterResource(R.drawable.baseline_fast_forward_24),
                        contentDescription = "fastfor",
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = showVolume,
            modifier = Modifier.align(Alignment.TopCenter).offset(x = if (isControlsVisible)100.dp else 20.dp),
            enter = slideInVertically(),
            exit = slideOutVertically ()
        ) {
            Row(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Icon(modifier = Modifier.size(30.dp), contentDescription = "vol"
                    , tint = MaterialTheme.colorScheme.primary
                    ,painter = painterResource(
                        when(volumeState){
                            0 -> R.drawable.baseline_volume_off_24
                            in 1..50 -> R.drawable.baseline_volume_mute_24
                            in 51..75 -> R.drawable.baseline_volume_down_24
                            else -> R.drawable.baseline_volume_up_24
                        }
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                VerticalBar(progress = volumeState / 200f)
                IndicatorLabel("$volumeState%")
            }
        }
        AnimatedVisibility(
            visible = showBrightness,
            modifier = Modifier.align(Alignment.TopCenter).offset(x = if (isControlsVisible)100.dp else 20.dp),
            enter = slideInVertically(),
            exit = slideOutVertically ())
            {
            Row(
                modifier = Modifier.align(Alignment.TopCenter).padding(top=30.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Icon(modifier = Modifier.size(30.dp), contentDescription = "bright"
                    , tint = MaterialTheme.colorScheme.primary
                    ,painter = painterResource(
                        when((currentBrightness * 100).toInt()){
                            in 0 .. 10 -> R.drawable.baseline_brightness_low_24
                            in 10..90 -> R.drawable.baseline_brightness_medium_24
                            in 90..100 -> R.drawable.baseline_brightness_high_24
                            else -> R.drawable.baseline_volume_up_24
                        }
                    )

                )
                Spacer(modifier = Modifier.width(10.dp))
                VerticalBar(progress = currentBrightness)
                IndicatorLabel("${(currentBrightness * 100).toInt()}%")
            }
        }
    }
}
@Composable
fun VerticalBar(progress: Float) {
    Box(
        Modifier.padding(vertical = 8.dp).height(10.dp).width(175.dp)
            .clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            Modifier.fillMaxHeight()
                .graphicsLayer {
                scaleX = progress.coerceIn(0f, 1f)
                transformOrigin = TransformOrigin(0f, 0.5f)}
                    .fillMaxWidth()// Scale from left
                .align(Alignment.CenterStart).background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun IndicatorLabel(text: String) {
    Box(Modifier.width(45.dp).clip(RoundedCornerShape(20.dp))) {
        Text(text = text, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
    }
}
fun updateBrightness(activity:MainActivity, level: Float) {
    val params = activity?.window?.attributes
    params?.screenBrightness = level.coerceIn(0f, 1f)
    activity?.window?.attributes = params

}

