package com.example.monoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.videolan.libvlc.MediaPlayer
import java.io.File




@Composable
fun SubtitleBox(
    vm: MyViewModel,
    mediaPlayer: MediaPlayer,
    Display: display,
    subtitles: List<SubtitleLine>?
) {
    // Current text state for the UI
    var currentSubText by remember { mutableStateOf("") }
    val subSetting by vm.subtitleSettings.collectAsState()

    // This Timer runs independently
    LaunchedEffect (mediaPlayer,subtitles) {
        if (subtitles.isNullOrEmpty()) {
            currentSubText = ""
            return@LaunchedEffect
        }
            withContext(Dispatchers.Default) {
                while (true) {
                    if (!mediaPlayer.isReleased && mediaPlayer.isPlaying) {
                        val time = mediaPlayer.time
                        val index = subtitles.binarySearch { line ->
                            when {
                                time < line.start -> 1
                                time > line.end -> -1
                                else -> 0
                            }
                        }
                        val newText = if (index >= 0) subtitles[index].text else ""
                        if (currentSubText != newText) {
                            withContext(Dispatchers.Main) {
                                currentSubText = newText
                            }
                        }
                    }
                    delay(50)
                }
            }
        }
    // The UI Layer
    if (currentSubText.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (Display== display.control)110.dp else 20.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = currentSubText,
                color = Color.White,
                fontSize = subSetting.size.sp,
                textAlign = TextAlign.Center,
                lineHeight = (subSetting.size + 2).sp,
                fontWeight = if(subSetting.isBold) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .background(
                            color = if (!subSetting.isTransperant)Color.Black.copy(alpha = 0.7f) else Color.Transparent ,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = TextStyle(shadow = if (subSetting.isShadow) {
                    Shadow(
                        color = Color.Black,
                        offset = androidx.compose.ui.geometry.Offset(3f, 3f),
                        blurRadius = 5f
                    )
                } else {
                    null
                })
            )
        }
    }
}







data class SubtitleLine(
    val start: Long, // milliseconds
    val end: Long,   // milliseconds
    val text: String
)

fun parseSrt(file: File): List<SubtitleLine> {
    val lines = file.readLines()
    val subs = mutableListOf<SubtitleLine>()
    var i = 0
    while (i < lines.size) {
        if (lines[i].contains("-->")) {
            val times = lines[i].split(" --> ")
            val start = timeToMs(times[0].trim())
            val end = timeToMs(times[1].trim())
            val text = StringBuilder()
            i++
            while (i < lines.size && lines[i].isNotBlank()) {
                text.append(lines[i]).append("\n")
                i++
            }
            subs.add(SubtitleLine(start, end, text.toString().trim()))
        }
        i++
    }
    return subs
}

private fun timeToMs(time: String): Long {
    return try {
        val parts = time.replace(",", ".").trim().split(":")
        val hours = parts[0].toLong() * 3600000L
        val minutes = parts[1].toLong() * 60000L
        val secParts = parts[2].split(".")
        val seconds = secParts[0].toLong() * 1000L
        val msString = if (secParts.size > 1) secParts[1].padEnd(3, '0').take(3) else "0"
        val milliseconds = msString.toLong()
        hours + minutes + seconds + milliseconds
    } catch (e: Exception) {
        0L
    }
}
