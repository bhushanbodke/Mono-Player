package com.example.monoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlaylistP(vm: MyViewModel) {
    val folderFiles = vm.folderFiles.collectAsState()
    val currentVideo = vm.currentVideo.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                )
            )
            .padding(top = 10.dp)
    ) {
        Spacer(Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth() // Changed from fillMaxSize to avoid stretching
                .height(150.dp) // Give it a specific height for the row
                .padding(horizontal = 8.dp)
        ) {
            items(folderFiles.value.size) { id ->
                val video = folderFiles.value[id]

                Column(
                    modifier = Modifier
                        .width(160.dp) // Adjusted width to a reasonable size
                        .padding(horizontal = 6.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = MaterialTheme.colorScheme.primary)
                        ) {
                            // FIXED: Pass the VideoId (Long) as required by your ViewModel
                            vm.updateCurrentVideo(video)
                        }
                ) {
                    // Thumbnail Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp) // Standard 16:9 or similar ratio
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        VideoThumbnail(video)

                        // Time Label
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(0.6f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = formatTime(video.duration.toLong()),
                                modifier = Modifier
                                    .padding(1.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(0.6f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 8.sp,
                                lineHeight = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Progress Bar
                        if (video.Time > 0f) {
                            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                                SquareProgressBar(progress = video.Time,   3)
                            }
                        }
                    }

                    // Video Title
                    Text(
                        text = video.name,
                        modifier = Modifier.padding(top = 6.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Medium,
                        color = if (currentVideo.value?.VideoId == video.VideoId)
                            MaterialTheme.colorScheme.primary
                        else Color.White
                    )
                }
            }
        }
    }
}
