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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.paint
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
    mediaPlayer: MediaPlayer,
    brightness: Float,
    activity: MainActivity,
    toggleControls: () -> Unit
) {

    var brightness by rememberSaveable { mutableStateOf(brightness) }
    var volumeState by rememberSaveable { mutableStateOf(mediaPlayer.volume) }
    var ShowVolume by rememberSaveable { mutableStateOf(false) }
    var ShowBrightness by rememberSaveable { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableLongStateOf(0L) }
    var validDrag by remember { mutableStateOf(true) }
    var isHolding by remember { mutableStateOf(false) }
    var AddedTime by remember { mutableLongStateOf(0L) }
    var Forword by remember { mutableStateOf(false) }
    var Backword by remember { mutableStateOf(false) }

    var orientation by remember { mutableStateOf(activity.requestedOrientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) }


    DisposableEffect(Unit) {
        onDispose { updateBrightness(activity, brightness) }
    }
    LaunchedEffect(activity.requestedOrientation) {
        orientation = activity.requestedOrientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    LaunchedEffect(Forword,Backword) {
        if(Forword){delay(500);Forword = false}
        if(Backword){delay(500);Backword = false}
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
                        },
                        onDoubleTap = {offset ->
                            if (offset.x < size.width / 2){
                                Backword = true;
                                val targetTime = mediaPlayer.time - 10000L
                                mediaPlayer.time = targetTime.coerceAtLeast(0L)
                            }
                            else{
                                Forword = true;
                                val targetTime = mediaPlayer.time - 10000L
                                mediaPlayer.time = targetTime.coerceAtMost(mediaPlayer.length)}
                        }
                    )
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
                        onDragStart = {offset->
                            val edgeMargin = 40.dp.toPx()
                            validDrag = offset.x > edgeMargin && offset.x < (size.width - edgeMargin)
                            if (validDrag) {
                                seekPosition = mediaPlayer.time
                                AddedTime = 0L
                                isSeeking = false
                            }
                        },
                        onDragEnd = {
                            if (isSeeking) {
                                mediaPlayer.time = seekPosition // Final seek on release
                                isSeeking = false
                            }
                            ShowVolume = false
                            ShowBrightness = false
                        },
                        onDrag = { change, dragAmount ->
                            if (!validDrag) return@detectDragGestures
                            change.consume()
                            val absX = Math.abs(dragAmount.x)
                            val absY = Math.abs(dragAmount.y)

                            if ((isSeeking || absX > absY) && !ShowVolume && !ShowBrightness) {
                                isSeeking = true
                                val added = (dragAmount.x * 50).toLong()
                                AddedTime += added
                                val totalDuration = mediaPlayer.length
                                val newPosition = (seekPosition + added).coerceIn(0L, if (totalDuration > 0) totalDuration else Long.MAX_VALUE)
                                if (Math.abs(newPosition - mediaPlayer.time) > 200) {
                                    mediaPlayer.time = newPosition
                                }
                                seekPosition = newPosition

                            }else if (!isSeeking) {
                                if (change.position.x > size.width / 2) {
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
        AnimatedVisibility(
            visible = Backword,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }),
            exit = slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
        ) {
            Spacer(modifier = Modifier.width(40.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape) // Shadow follows the circle
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_fast_rewind_24),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "rewind",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        AnimatedVisibility(
            visible = Forword,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }),
            exit = slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape) // Shadow follows the circle
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_fast_forward_24),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "forword",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(40.dp))
            }
        }


        AnimatedVisibility(
            visible = isHolding,
            modifier = Modifier.align(Alignment.CenterStart),
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
            visible = ShowVolume,
            modifier = Modifier.align(Alignment.TopCenter),
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
            visible = ShowBrightness,
            modifier = Modifier.align(Alignment.TopCenter),
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
                        when((brightness * 100).toInt()){
                            in 0 .. 10 -> R.drawable.baseline_brightness_low_24
                            in 10..90 -> R.drawable.baseline_brightness_medium_24
                            in 90..100 -> R.drawable.baseline_brightness_high_24
                            else -> R.drawable.baseline_volume_up_24
                        }
                    )

                )
                Spacer(modifier = Modifier.width(10.dp))
                VerticalBar(progress = brightness)
                IndicatorLabel("${(brightness * 100).toInt()}%")
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

