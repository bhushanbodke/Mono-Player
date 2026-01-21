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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.VideoDecoder
import com.bumptech.glide.signature.ObjectKey

@Composable
fun FolderScreen(vm: MyViewModel) {
    val files by vm.folderFiles.collectAsState()
    val screen by vm.screen.collectAsState()
    val gridValue by vm.GridValue.collectAsState()
    val settings by vm.settings.collectAsState()
    val activity = LocalActivity.current as? MainActivity

    var moreVisible by remember { mutableStateOf(false) }
    var selectedVideo by remember { mutableStateOf(VideoModel()) }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vm.removeFile(selectedVideo.VideoId)
        }
    }

    BackHandler(enabled = screen == Screens.Videos) {
        vm.setScreen(Screens.Home)
        vm.titlePath.value = "Internal Storage/"
    }

    val listState = rememberLazyGridState()
    LaunchedEffect(settings.Sort) {
        listState.animateScrollToItem(0)
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (gridValue == 0) 1 else gridValue),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(files.size, key = { files[it].VideoId }) { id ->
                val video = files[id]
                val onMore = { selectedVideo = video; moreVisible = true }
                    when (gridValue) {
                        1 -> ListViewVideos(vm, video, onMore)
                        2 -> GridViewVideos(vm, video, onMore)
                        else -> Grid3ViewVideos(vm, video, onMore)
                    }
            }
        }

        AnimatedVisibility(visible = moreVisible, enter = fadeIn(), exit = fadeOut()) {
            MoreInfo(selectedVideo, { moreVisible = false }, deleteLauncher)
        }
    }
}

@Composable
fun ListViewVideos(vm: MyViewModel, video: VideoModel, ShowMoreInfo: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
            vm.updateCurrentVideo(video)
            vm.setScreen(Screens.VideoPlayer)
        },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp).height(90.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.width(130.dp).fillMaxHeight().clip(RoundedCornerShape(8.dp))) {
                VideoThumbnail(video)
                if (video.isNew) Box(Modifier.align(Alignment.TopStart)) { TextInfo("New", Color.Cyan) }
                if (video.isFinished) Icon(painterResource(R.drawable.baseline_done_all_24), null, Modifier.size(18.dp).align(Alignment.TopEnd).padding(2.dp).background(Color.Black.copy(0.5f), CircleShape), tint = Color.Green)

                Text(
                    text = formatTime(video.duration.toLong()),
                    modifier = Modifier.align(Alignment.BottomEnd).background(Color.Black.copy(0.7f), RoundedCornerShape(4.dp)).padding(2.dp),
                    fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, lineHeight = 10.sp
                )
                if (video.Time > 0f) SquareProgressBar(video.Time, 4, Modifier.align(Alignment.BottomCenter))
            }

            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(text = video.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 2, lineHeight = 18.sp ,overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Text(text = "${formatFileSize(video.size)} • ${video.Width}p", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(onClick = ShowMoreInfo) {
                Icon(painterResource(R.drawable.baseline_more_vert_24), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun GridViewVideos(vm: MyViewModel, video: VideoModel, ShowMoreInfo: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
            detectTapGestures(onTap = { vm.updateCurrentVideo(video); vm.setScreen(Screens.VideoPlayer) }, onLongPress = { ShowMoreInfo() })
        }
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(140.dp)) {
                VideoThumbnail(video)
                Text(
                    text = formatTime(video.duration.toLong()),
                    modifier = Modifier.align(Alignment.BottomEnd).background(Color.Black.copy(0.7f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 10.sp, color = Color.White, lineHeight = 10.sp
                )
                if (video.Time > 0f) SquareProgressBar(video.Time, 4, Modifier.align(Alignment.BottomCenter))
            }
            Column(Modifier.padding(8.dp)) {
                Text(text = video.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = formatFileSize(video.size), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun Grid3ViewVideos(vm: MyViewModel, video: VideoModel, ShowMoreInfo: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().pointerInput(Unit) {
            detectTapGestures(onTap = { vm.updateCurrentVideo(video); vm.setScreen(Screens.VideoPlayer) }, onLongPress = { ShowMoreInfo() })
        }
    ) {
        Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp))) {
            VideoThumbnail(video)
            if (video.Time > 0f) SquareProgressBar(video.Time, 3, Modifier.align(Alignment.BottomCenter))
        }
        Text(text = video.name, fontSize = 10.sp, maxLines = 1, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 2.dp).fillMaxWidth(), textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun MoreInfo(video: VideoModel, ToggleVisible: () -> Unit, launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {
    val context = LocalContext.current
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.6f)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { ToggleVisible() }, contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f).wrapContentHeight().clickable(enabled = false) {},
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp))) {
                    VideoThumbnailLoop(video.uri, video.duration.toLong())
                    Icon(painterResource(R.drawable.play_arrow_24), null, Modifier.align(Alignment.Center).size(48.dp), tint = Color.White.copy(0.8f))
                }
                Text(text = video.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                ListItem(
                    headlineContent = { Text("Delete Video", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(painterResource(R.drawable.twotone_delete_24), null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { deleteMediaByUri(context, video.uri.toUri(), launcher); ToggleVisible() }
                )
                TextButton(onClick = ToggleVisible, modifier = Modifier.align(Alignment.End)) { Text("Close") }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VideoThumbnail(video: VideoModel, high: Boolean = false) {
    GlideImage(
        model = video.uri,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    ) {
        it.override(if (high) 600 else 300, if (high) 400 else 200)
            .frame(10000000)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .signature(ObjectKey(video.DateAdded))
    }
}

@Composable
fun SquareProgressBar(progress: Float, height: Int, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(height.dp).background(Color.Gray.copy(0.3f))) {
        Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
    }
}

@Composable
fun TextInfo(text: String, color: Color = Color.White) {
    Text(
        text = text,
        fontSize = 8.sp,
        color = color,
        fontWeight = FontWeight.Black,
        // FORCE the line height to be small so it doesn't push the pill boundaries
        lineHeight = 8.sp,
        style = LocalTextStyle.current.copy(
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        modifier = Modifier
            .padding(4.dp) // Space away from the edge of the thumbnail
            .background(Color.Black.copy(0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp) // Internal pill padding
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VideoThumbnailLoop(videoUri: String, durationMs: Long) {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1200); tick = (tick + 1) % 10 } }
    val timestampUs = remember(tick) { (durationMs / 10) * tick * 1000L }

    AnimatedContent(targetState = tick, label = "") {
        GlideImage(model = videoUri, contentDescription = null, modifier = Modifier.fillMaxSize()) {
            it.set(VideoDecoder.TARGET_FRAME, timestampUs).centerCrop()
        }
    }
}