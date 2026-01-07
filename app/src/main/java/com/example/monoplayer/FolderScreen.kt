package com.example.monoplayer

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.VideoDecoder

@Composable
fun FolderScreen(vm:MyViewModel) {
    var files = vm.folderFiles.collectAsState();
    val screen = vm.screen.collectAsState();
    val activity = LocalActivity.current as MainActivity
    var MoreVisible by remember { mutableStateOf(false) }
    var SelectedVideoId by remember { mutableStateOf(VideoModel()) }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vm.removeFile(SelectedVideoId.VideoId)
        }
    }

    BackHandler(enabled = screen.value == Screens.Videos) {
        vm.setScreen(Screens.Home);
        vm.titlePath.value = "Internal Storage/";
    }

    LaunchedEffect(Unit) {
        activity?.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.screenBrightness = -1f // -1f resets to system preference
            window.attributes = layoutParams
        }
    }

    val listState = rememberLazyListState()

    Box(Modifier.fillMaxSize())
    {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                userScrollEnabled = true,
                modifier = Modifier
                    .fillMaxSize()
                    .overscroll(rememberOverscrollEffect())
            )
            {
                items(files.value.size, key = { files.value[it].VideoId }, contentType = { "VideoModel" })
                { id ->
                    Box(Modifier.fillMaxWidth().animateItem()){
                        Spacer(modifier = Modifier.size(5.dp));
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(125.dp)
                                .padding(start = 5.dp, top = 10.dp)
                                .clip(RoundedCornerShape(10.dp))

                        ) {
                            Box(Modifier
                                .size(120.dp, 84.dp)
                            ) {
                                Box(Modifier.align(Alignment.Center)){
                                    Box(Modifier
                                        .align(Alignment.Center)
                                        .size(120.dp, 80.dp)){
                                        VideoThumbnail(files.value[id].path)
                                    }
                                    if(files.value[id].isFinished) {
                                        Icon(painterResource(R.drawable.baseline_done_all_24), contentDescription = "isfinshed",
                                            Modifier
                                                .size(15.dp)
                                                .align(Alignment.TopEnd)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(0.6f)))
                                    }
                                    Text(
                                        text = formatTime(files.value[id].duration.toLong()),
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
                                if (files.value[id].Time > 0f) {
                                    Box(Modifier.align(Alignment.BottomCenter)) {
                                        SquareProgressBar(progress = files.value[id].Time,4)
                                    }
                                }
                            }

                            Column(
                                Modifier
                                    .weight(1f)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                vm.updateCurrentVideo(files.value[id]);
                                                vm.setScreen(Screens.VideoPlayer);
                                            },
                                            onLongPress = {
                                                MoreVisible = true
                                                SelectedVideoId = files.value[id]
                                            }
                                        )
                                    }
                            ) {
                                Text(
                                    text = files.value[id].name,
                                    fontSize = 16.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 10.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                );
                                Row() {
                                    if (files.value[id].isNew) {
                                        Text(
                                            text = "New",
                                            modifier = Modifier.padding(start = 12.dp),
                                            fontSize = 12.sp,
                                            color =Color.Cyan
                                        );
                                    }
                                    val videoInfo = remember(files.value[id]) {
                                        " • ${formatFileSize(files.value[id].size)} • ${files.value[id].Width}x${files.value[id].Height}"
                                    }
                                    Text(
                                        text = videoInfo,
                                        fontSize = 12.sp,
                                        color =Color(0xFFacb0b0)
                                    );

                                }
                            }
                            IconButton(
                                onClick = {
                                    MoreVisible = true
                                    SelectedVideoId = files.value[id]
                                }
                            ) {
                                Icon(painterResource(R.drawable.baseline_more_vert_24), contentDescription = "more"
                                    , Modifier
                                        .width(30.dp)
                                        .height(30.dp))
                            }
                        }
                    }
                }
            }
            CustomScrollIndicatorLazyColumn(listState = listState)
        }
        AnimatedVisibility(
            visible = MoreVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MoreInfo(SelectedVideoId,{ MoreVisible = false },deleteLauncher)
        }
    }
}



@Composable
fun MoreInfo(video: VideoModel, ToggleVisible:()->Unit
             ,launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>){
    BackHandler() {
        ToggleVisible()
    }
    val context = LocalContext.current;
    Box(Modifier
        .fillMaxSize()
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            ToggleVisible()
        }) {
        Column (Modifier
            .align(Alignment.Center)
            .height(700.dp)
            .fillMaxWidth()
            .padding(start = 3.dp, end = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {}
        ){
            Box(Modifier
                .fillMaxWidth()
                .height(250.dp)){
                VideoThumbnailLoop(video.uri,video.duration.toLong());
            }
            Text(text = video.name, fontSize = 18.sp, fontWeight = FontWeight.Bold,modifier=Modifier.padding(20.dp))
            HorizontalDivider(Modifier
                .padding(start = 20.dp, end = 20.dp)
                .fillMaxWidth()
                .height(10.dp), color = MaterialTheme.colorScheme.primary)
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable{
                    deleteMediaByUri(context,video.uri.toUri(),launcher)
                    ToggleVisible()
                }
            ) {
                Spacer(Modifier.width(20.dp))
                Icon(painterResource(R.drawable.twotone_delete_24), contentDescription = "delete",
                    Modifier.size(40.dp), tint = Color.White)
                Spacer(Modifier.width(20.dp))
                Text(text = "Delete File", fontSize = 15.sp, fontWeight = FontWeight.Bold,color = Color.Gray)
            }
        }
    }
}
@OptIn( ExperimentalGlideComposeApi::class)
@Composable
fun VideoThumbnail(videoPath: String,high: Boolean = false) {
    if(!high){GlideImage(
        model = videoPath,
        contentDescription = "Video Thumbnail",
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop

    )
    {
            it.override(300, 200)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.twotone_video_file_24)
                .error(R.drawable.twotone_subtitles_24)
    }}
    else{
        GlideImage(
            model = videoPath,
            contentDescription = "Video Thumbnail",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop

        ){
            it.diskCacheStrategy(DiskCacheStrategy.ALL)
        }
    }
}

@Composable
fun SquareProgressBar(progress: Float ,height:Int) {
        val progressColor = MaterialTheme.colorScheme.primary
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .background(Color.White)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        color = progressColor,
                        size = size.copy(width = size.width * progress)
                    )
                }
        )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VideoThumbnailLoop(videoUri: String, durationMs: Long) {
    // 1. Create a timer (Clock) that ticks 0 to 9 every second
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            tick = (tick + 1) % 10
        }
    }
    // Formula: (Duration / 10 intervals) * current tick * 1000 (to get us)
    val timestampUs = remember(tick) {
        (durationMs / 10) * tick * 1000L
    }
    // 3. Display using GlideImage
    AnimatedContent(targetState = tick, label = "VideoThumbnailLoop") {
        GlideImage(
            model = videoUri,
            contentDescription = "Thumbnail loop",
            modifier = Modifier.fillMaxSize().animateContentSize(),
        ) {
            it.set(VideoDecoder.TARGET_FRAME, timestampUs)
                .centerCrop()
        }
    }
}
