package com.example.monoplayer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.videolan.libvlc.MediaPlayer


@Composable
fun VideoProgressSlider(
    vm: MyViewModel,
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
        Text(text = formatTime(mediaPlayer.time), fontSize = 14.sp, color = Color.White)
        Box(modifier = Modifier.weight(1f)) {
            SmoothWavyVideoSlider(
                vm,
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
        Text(text = formatTime(mediaPlayer.length), fontSize = 14.sp, color = Color.White)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmoothWavyVideoSlider(vm: MyViewModel,progress: Float, onSeek: (Float) -> Unit, onfinished: () -> Unit) {
    val WavyBar = vm.WavyBar.collectAsState().value
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
        Slider(
            value = progress,
            onValueChange = onSeek,
            onValueChangeFinished = onfinished,
            thumb = {
                Box(
                    Modifier
                        .size(8.dp,20.dp).clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF7DBEDC), RectangleShape)
                )
            },
            track = {
                Canvas(modifier = Modifier.fillMaxWidth().height(30.dp)) {
                    val centerY = size.height / 2
                    val trackWidth = size.width
                    val activeTrackEndPoint = trackWidth * progress

                    drawLine(
                        color = Color(0xFF212226).copy(alpha = 0.6f),
                        start = Offset(activeTrackEndPoint, centerY),
                        end = Offset(trackWidth, centerY),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )

                    // 2. Draw Active Track (The Wavy part)
                    if(WavyBar){
                        val path = Path()
                        val waveLength = 10f
                        val amplitude = 8f

                        path.moveTo(0f, centerY)

                        // Only loop up to the activeTrackEndPoint
                        for (x in 0..activeTrackEndPoint.toInt() step 5) {
                            val y = centerY + (amplitude * kotlin.math.sin((x / waveLength) + phaseShift))
                            path.lineTo(x.toFloat(), y)
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFF7DBEDC),
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )
                    }
                    else{
                    drawLine(
                        color = Color(0xFF7DBEDC),
                        start = Offset(0f, centerY),
                        end = Offset(activeTrackEndPoint, centerY),
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }
                }
            }
        )
    }
}