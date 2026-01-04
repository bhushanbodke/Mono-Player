package com.example.monoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer


@Composable
fun VideoProgressSlider(
    mediaPlayer: MediaPlayer,
    isDraggingExternal: Boolean,
    onSeek: (Float) -> Unit,
    onFinished: () -> Unit
) {
    // This state is local to JUST the slider
    var localProgress by remember { mutableStateOf(0f) }
    var internalIsDragging by remember { mutableStateOf(false) }

    // The "Loop" lives here now, isolated from the rest of the UI
    LaunchedEffect(mediaPlayer) {
        while (true) {
            if (mediaPlayer.isPlaying && !internalIsDragging && !isDraggingExternal) {
                localProgress = mediaPlayer.position
            }
            delay(200)
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Current Time
        Text(text = formatTime(mediaPlayer.time), fontSize = 12.sp, color = Color.White)

        // The Slider now uses .weight(1f) to fill the middle space only
        Box(modifier = Modifier.weight(1f)) {
            SmoothVideoSlider(
                progress = localProgress,
                onSeek = {
                    internalIsDragging = true
                    localProgress = it
                    onSeek(it)
                },
                onfinished = {
                    internalIsDragging = false
                    onFinished()
                }
            )
        }

        Text(text = formatTime(mediaPlayer.length), fontSize = 12.sp, color = Color.White)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmoothVideoSlider(progress: Float, onSeek: (Float) -> Unit,onfinished:()->Unit) {
    Box(Modifier.fillMaxWidth().height(30.dp)) {
        Slider(
            value = progress,
            onValueChange = onSeek,
            onValueChangeFinished = onfinished,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            thumb = {
                Box(
                    Modifier
                        .size(15.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    // THIS is how you make it look like a line safely
                    modifier = Modifier.height(4.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.5f)
                    )
                )
            }
        )
    }
}