package com.example.monoplayer

import android.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.delay

@Composable
fun FolderScreen(vm:MyViewModel) {
    val files = vm.folderFiles.collectAsState();
    val screen = vm.screen.collectAsState();
    BackHandler(enabled = screen.value == Screens.Videos) {
        vm.setScreen(Screens.Home);
        vm.titlePath.value = "Internal Storage/";
    }

        LazyColumn(Modifier.fillMaxSize().overscroll(rememberOverscrollEffect())) {
            items(files.value.size) { id ->
                Spacer(modifier = Modifier.size(5.dp));
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().height(125.dp).padding(10.dp)
                        .clip(RoundedCornerShape(10.dp)).clickable
                            (
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = MaterialTheme.colorScheme.secondary)
                        )
                        {
                            vm.updateCurrentVideo(files.value[id].id);
                            vm.setScreen(Screens.VideoPlayer);
                        }
                ) {
                    Box(Modifier.size(120.dp, 84.dp)) {
                        VideoThumbnail(files.value[id].path, width = 120, height = 80)
                        if (files.value[id].Time > 0f) {
                            Box(Modifier.align(Alignment.BottomCenter)) {
                                SquareProgressBar(progress = files.value[id].Time.toFloat()/files.value[id].duration.toFloat())
                            }
                        }
                    }

                    Column() {
                        Text(
                            text = files.value[id].name,
                            fontSize = 17.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 10.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        );
                        Row() {
                            Text(
                                text = formatDuration(files.value[id].duration),
                                modifier = Modifier.padding(start = 12.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = formatFileSize(files.value[id].size),
                                modifier = Modifier.padding(start = 12.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            );
                        }
                    }
                }
            }
        }
}
@OptIn( ExperimentalGlideComposeApi::class)
@Composable
fun VideoThumbnail(videoPath: String,width: Int, height:Int) {
    GlideImage(
        model = videoPath,
        contentDescription = "Video Thumbnail",
        modifier = Modifier
            .size(width.dp, height.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun SquareProgressBar(progress: Float,height:Int = 4) { // progress is 0.0f to 1.0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(Color.White) // This is the "Track"
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress) // This sets the length
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary) // This is the "Progress"
        )
    }
}

