package com.example.monoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.videolan.libvlc.MediaPlayer
import java.io.File




@Composable
fun SubtitleBox(
    mediaPlayer: MediaPlayer,
    subtitles: List<SubtitleLine>?
) {
    // Current text state for the UI
    var currentSubText by remember { mutableStateOf("") }

    // This Timer runs independently
    LaunchedEffect (subtitles) {
        if (subtitles.isNullOrEmpty()) {
            currentSubText = ""
            return@LaunchedEffect
        }
        while (true) {
            if (!mediaPlayer.isReleased && mediaPlayer.isPlaying) {
                val time = mediaPlayer.time
                val index = subtitles.binarySearch { line ->
                    when {
                        time < line.start -> 1  // Look in the left half
                        time > line.end -> -1   // Look in the right half
                        else -> 0                       // Found the match!
                    }
                }
                val newText = if (index >= 0) subtitles[index].text else ""
                if (currentSubText != newText) {
                    currentSubText = newText
                }
            }
            delay(100)
        }
    }
    // The UI Layer
    if (currentSubText.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = currentSubText,
                color = Color.White,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier
//                    .background(
//                        color = Color.Black.copy(alpha = 0.7f),
//                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
//                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
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
