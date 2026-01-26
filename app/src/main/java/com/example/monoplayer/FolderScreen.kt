package com.example.monoplayer

import android.app.Activity
import android.net.Uri
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
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
    var showSettings by remember { mutableStateOf(false) }
    val stableGridValue by rememberUpdatedState(gridValue)



    var moreVisible by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedVideo by remember { mutableStateOf(VideoModel()) }

    val selectedVideoUris = remember { mutableStateListOf<String>() }

    fun addToSelected(video: VideoModel) {
        if (video.uri !in selectedVideoUris) {
            selectedVideoUris.add(video.uri)
        }
    }

    fun removeSelected(video: VideoModel) {
        selectedVideoUris.remove(video.uri)
        if (selectedVideoUris.isEmpty()) {
            selectionMode = false
        }
    }


    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedVideoUris.forEach { videoUri-> vm.removeFile(videoUri) }
        }
    }

    BackHandler(enabled = screen == Screens.Videos) {
        if(selectionMode){
            selectedVideoUris.clear();
            selectionMode = false;
        }
        else{
            vm.setScreen(Screens.Home)
            vm.titlePath.value = "Internal Storage/"
        }
    }

    val listState = rememberLazyGridState()
    LaunchedEffect(settings.Sort) {
        listState.animateScrollToItem(0)
    }
    LaunchedEffect(Unit) {
        val savedIndex = vm.getSavedPosition(vm.titlePath.value)
        listState.animateScrollToItem(savedIndex)
    }
    DisposableEffect(Unit) {
        onDispose {
            // This runs when FolderScreen is swapped out or the path changes
            val lastIndex = listState.firstVisibleItemIndex
            vm.saveFolderPosition(vm.titlePath.value, lastIndex)
        }
    }

    Box(Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.align(Alignment.TopCenter)) {
            ActionBar(vm,selectionMode,selectedVideoUris.size == files.size
                ,{files.forEach { video -> addToSelected(video) }}
                ,{selectedVideoUris.clear()}
                ,{ showSettings = true })
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (gridValue == 0) 1 else gridValue),
                contentPadding = PaddingValues(start = 12.dp,top = 12.dp, end = 12.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(if(gridValue == 3 ) 6.dp else 8.dp),
                state = listState,
                modifier = Modifier.fillMaxSize().animateContentSize()
            ) {
                items(files.size, key = { files[it].VideoId }, contentType = { "VideoModel" }) { id ->
                    val video = files[id]
                    val onMore = {
                        selectedVideo = video;
                        moreVisible = true;
                    }
                    val isSelected by remember(video.uri) {
                        derivedStateOf { video.uri in selectedVideoUris }
                    }
                    when (gridValue) {
                        1 -> ListViewVideos(
                            vm, video, isSelected, selectionMode, { selectionMode = it }, onMore,
                            { addToSelected(video) },
                            { removeSelected(video) })

                        2 -> GridViewVideos( vm, video, isSelected, selectionMode, { selectionMode = it }, onMore,
                            { addToSelected(video) },
                            { removeSelected(video) })
                        else -> Grid3ViewVideos(
                            vm, video, isSelected, selectionMode, { selectionMode = it }, onMore,
                            { addToSelected(video) },
                            { removeSelected(video) })
                    }
                }
            }
        }
        AnimatedVisibility(visible = moreVisible, enter = fadeIn(), exit = fadeOut()) {
            MoreInfo(selectedVideo, { moreVisible = false }, deleteLauncher)
        }
        // --- SORT BOTTOM SHEET OVERLAY ---
        AnimatedVisibility(
            visible = showSettings,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            SortSheetOverlay(vm) { showSettings = false }
        }
        if (selectionMode){
            Box(Modifier.align(Alignment.BottomCenter)){
                multipleSelectionmenu(selectedVideoUris.size,selectedVideoUris,deleteLauncher,{selectionMode = false; selectedVideoUris.clear()})
            }
        }
        else{
            // --- RESUME FAB ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-16).dp, y = (-16).dp)
                    .padding(15.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable() {
                        val searchPath = vm.titlePath.value
                        val folderData = vm.lastPlayedFolder.value.find { it.folder == searchPath }
                        folderData?.lastVideoId?.let { id ->
                            vm.AllFiles.value.find { it.VideoId == id }?.let { video ->
                                vm.updateCurrentVideo(video)
                                vm.setScreen(Screens.VideoPlayer)
                            }
                        }
                    },
            ){
                Icon(imageVector = Icons.Default.PlayArrow
                    ,tint = MaterialTheme.colorScheme.surface, contentDescription =  null
                    ,modifier= Modifier.size(60.dp))
            }
        }
    }
}


@Composable
fun ActionBar(vm: MyViewModel
              ,selectionMode:Boolean
              ,isSelectedAll:Boolean
              ,addAll:()->Unit
              ,removeAll:()->Unit
              ,showSettings:()->Unit
              ) {
    val gridvalue by vm.GridValue.collectAsState()

// --- ACTION BAR ---
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp)),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Text(
            text = vm.titlePath.value.substringAfterLast("/"),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        IconButton(onClick = { vm.toggleGrid() }) {
            Icon(
                painter = painterResource(
                    when (gridvalue) {
                        1 -> R.drawable.view_list_24
                        2 -> R.drawable.grid_view_24
                        else -> R.drawable.grid_on_24
                    }
                ),
                contentDescription = "Toggle Grid",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        IconButton(onClick = showSettings) {
            Icon(
                painter = painterResource(R.drawable.sort_24),
                contentDescription = "Sort Options",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        if(selectionMode){
            IconButton(onClick = {
                    if(isSelectedAll) removeAll()
                    else addAll()
            }) {
                Icon(
                    painter = painterResource(if(isSelectedAll) R.drawable.deselect_24 else R.drawable.select_all_24),
                    contentDescription = "select all",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
@Composable
fun multipleSelectionmenu(
        selectedSize:Int
     ,selectedVideoUris:List<String>
     ,launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
     ,clearSelection:()->Unit)
    {
        val context = LocalContext.current
        val selectedUris = selectedVideoUris.map { it.toUri() }

        Row(verticalAlignment = Alignment.CenterVertically
            ,modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 25.dp, top = 10.dp))
        {
            Spacer(Modifier.weight(1f));
            Row(verticalAlignment = Alignment.CenterVertically
                , modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                Icon(
                    painter = painterResource(R.drawable.content_copy_24),
                    contentDescription = "Sort Options",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(text = "Copy",fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)

            }
            Spacer(Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically
                , modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {}
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                Icon(
                    painter = painterResource(R.drawable.drive_file_move_rtl_24),
                    contentDescription = "Sort Options",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(text = "Move",fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically
                , modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { deleteMediaByUri(context, selectedUris, launcher) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                Icon(
                    painter = painterResource(R.drawable.twotone_delete_24),
                    contentDescription = "Sort Options",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
                Text(text = "Delete",fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)

            }
            Spacer(Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically
                , modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { shareVideos(context, selectedUris) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(text = "Share",fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)

            }
            Spacer(Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically
                , modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 6.dp, vertical = 4.dp))
            {
                Text(text = "${selectedSize}",fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(2.dp))
                Text(text = "Selected",fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.size(8.dp))
        }
}



@Composable
fun ListViewVideos(
    vm: MyViewModel,
    video: VideoModel,
    isSelected: Boolean,
    selectionMode: Boolean,
    changeSelectionMode: (Boolean) -> Unit,
    ShowMoreInfo: () -> Unit,
    addToSelected: () -> Unit,
    removeSelected: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(isSelected, selectionMode) {
                detectTapGestures(
                    onTap = {
                        if (selectionMode) {
                            if (isSelected) {
                                removeSelected()
                            } else {
                                addToSelected()
                            }
                        } else {
                            vm.updateCurrentVideo(video)
                            vm.setScreen(Screens.VideoPlayer)
                        }
                    },
                    onLongPress = {
                        if (!isSelected) {
                            addToSelected()
                        }
                        if (!selectionMode) {
                            changeSelectionMode(true)
                        }
                    }
                )
            },
        color = if (isSelected)
            MaterialTheme.colorScheme.secondary.copy(0.5f)
        else
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 4.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(90.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                VideoThumbnail(video)

                // Selection overlay
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    )
                }

                if (video.isNew && !isSelected) {
                    Box(Modifier.align(Alignment.TopStart)) {
                        TextInfo("New", Color.Cyan)
                    }
                }

                if (video.isFinished) {
                    Icon(
                        painterResource(R.drawable.baseline_done_all_24),
                        null,
                        Modifier
                            .size(18.dp)
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .background(Color.Black.copy(0.5f), CircleShape),
                        tint = Color.Green
                    )
                }

                Text(
                    text = formatTime(video.duration.toLong()),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.Black.copy(0.7f), RoundedCornerShape(4.dp))
                        .padding(2.dp),
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 10.sp
                )

                if (video.Time > 0f) {
                    SquareProgressBar(video.Time, 4, Modifier.align(Alignment.BottomCenter))
                }
            }

            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = video.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = "${formatFileSize(video.size)} • ${video.Width}p",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isSelected) {
                IconButton(onClick = { if(selectionMode) addToSelected() else ShowMoreInfo() }) {
                    Icon(
                        painterResource(R.drawable.baseline_more_vert_24),
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                IconButton(onClick = {removeSelected()}) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun GridViewVideos(
    vm: MyViewModel,
    video: VideoModel,
    isSelected: Boolean,
    selectionMode: Boolean,
    changeSelectionMode: (Boolean) -> Unit,
    ShowMoreInfo: () -> Unit,
    addToSelected: () -> Unit,
    removeSelected: () -> Unit) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .pointerInput(isSelected, selectionMode) {
                detectTapGestures(
                    onTap = {
                        if (selectionMode) {
                            if (isSelected) {
                                removeSelected()
                            } else {
                                addToSelected()
                            }
                        } else {
                            vm.updateCurrentVideo(video)
                            vm.setScreen(Screens.VideoPlayer)
                        }
                    },
                    onLongPress = {
                        if (!isSelected) {
                            addToSelected()
                        }
                        if (!selectionMode) {
                            changeSelectionMode(true)
                        }
                    }
                )
            },
        color = if (isSelected)
            MaterialTheme.colorScheme.secondary.copy(0.5f)
        else
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 4.dp else 2.dp)
    {
        Column {
            Box(Modifier
                .fillMaxWidth()
                .height(140.dp)) {
                VideoThumbnail(video)
                Text(
                    text = formatTime(video.duration.toLong()),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.Black.copy(0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
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
fun Grid3ViewVideos(
    vm: MyViewModel,
    video: VideoModel,
    isSelected: Boolean,
    selectionMode: Boolean,
    changeSelectionMode: (Boolean) -> Unit,
    ShowMoreInfo: () -> Unit,
    addToSelected: () -> Unit,
    removeSelected: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.secondary.copy(0.5f)
        else
            Color.Transparent,
        tonalElevation = if (isSelected) 4.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(isSelected, selectionMode) {
                detectTapGestures(
                    onTap = {
                        if (selectionMode) {
                            if (isSelected) removeSelected() else addToSelected()
                        } else {
                            vm.updateCurrentVideo(video)
                            vm.setScreen(Screens.VideoPlayer)
                        }
                    },
                    onLongPress = {
                        if (!isSelected) addToSelected()
                        if (!selectionMode) changeSelectionMode(true)
                    }
                )
            }
    ) {
        Column() {
            Box(Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))) {
                VideoThumbnail(video)

                if (isSelected) {
                    Box(Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
                }

                if (video.Time > 0f) SquareProgressBar(video.Time, 3, Modifier.align(Alignment.BottomCenter))
            }
            Text(
                text = video.name,
                fontSize = 10.sp,
                maxLines = 1,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
@Composable
fun MoreInfo(video: VideoModel, ToggleVisible: () -> Unit, launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {
    val context = LocalContext.current
    BackHandler(onBack = ToggleVisible)

    Box(Modifier
        .fillMaxSize()
        .background(Color.Black.copy(0.6f))
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { ToggleVisible() }, contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Box(Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))) {
                    VideoThumbnail(video, true)
                    Icon(painterResource(R.drawable.play_arrow_24), null, Modifier
                        .align(Alignment.Center)
                        .size(48.dp), tint = Color.White.copy(0.8f))
                }
                Text(text = video.name, fontSize = 15.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
                // -----Title name ----
                Text(text = video.path , lineHeight = 14.sp ,color = MaterialTheme.colorScheme.onSurface.copy(0.8f), fontSize = 13.sp)
                // -----Size----
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.storage_24), null,tint = MaterialTheme.colorScheme.onSurface)
                    Column(Modifier.padding(start = 20.dp)) {
                        Text(text = "Size" , color = MaterialTheme.colorScheme.onSurface,fontWeight = FontWeight.SemiBold, lineHeight = 10.sp,fontSize = 13.sp)
                        Text(text = "${formatFileSize(video.size)}" , color = MaterialTheme.colorScheme.onSurface,fontWeight = FontWeight.Medium,lineHeight = 10.sp,fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.aspect_ratio_24), null,tint = MaterialTheme.colorScheme.onSurface)
                    Column(Modifier.padding(start = 20.dp)) {
                        Text(text = "Resolution" , color = MaterialTheme.colorScheme.onSurface,fontWeight = FontWeight.SemiBold,lineHeight = 10.sp, fontSize = 13.sp)
                        Text(text = "${video.Width} x ${video.Height}" , color = MaterialTheme.colorScheme.onSurface,fontWeight = FontWeight.Medium,lineHeight = 10.sp, fontSize = 12.sp)
                    }
                }
                // -----Duration----
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.access_time_24), null,tint = MaterialTheme.colorScheme.onSurface)
                    Column(Modifier.padding(start = 20.dp)) {
                        Text(text = "Duration" , color = MaterialTheme.colorScheme.onSurface,fontWeight = FontWeight.SemiBold,lineHeight = 10.sp, fontSize = 13.sp)
                        Text(text = "${formatTime(video.duration.toLong())}" , color = MaterialTheme.colorScheme.onSurface,fontWeight = FontWeight.Medium,lineHeight = 10.sp, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ListItem(
                    headlineContent = { Text("Share Video", color = MaterialTheme.colorScheme.onSurface) },
                    leadingContent = { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.onSurface) },
                    modifier = Modifier.clickable { shareVideos(context, listOf(video.uri.toUri()));}
                )
                ListItem(
                    headlineContent = { Text("Delete Video", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(painterResource(R.drawable.twotone_delete_24), null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { deleteMediaByUri(context, listOf(video.uri.toUri()), launcher); ToggleVisible() }
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
        contentScale = ContentScale.Crop,
    ) {
        it.override(if (high) 600 else 300, if (high) 400 else 200)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .signature(ObjectKey(video.DateAdded))
            .dontAnimate()
    }
}

@Composable
fun SquareProgressBar(progress: Float, height: Int, modifier: Modifier = Modifier) {
    Box(modifier
        .fillMaxWidth()
        .height(height.dp)
        .background(Color.Gray.copy(0.3f))) {
        Box(Modifier
            .fillMaxWidth(progress)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.primary))
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




@Composable
fun MonoScrollbar(
    listState: LazyGridState,
    modifier: Modifier = Modifier
) {
    // 1. Calculate scroll progress (0.0 to 1.0)
    val scrollFraction by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) 0f
            else {
                val firstVisible = listState.firstVisibleItemIndex.toFloat()
                val visibleCount = layoutInfo.visibleItemsInfo.size.toFloat()
                // Prevent division by zero if total items are less than visible items
                if (totalItems <= visibleCount) 0f
                else (firstVisible / (totalItems - visibleCount)).coerceIn(0f, 1f)
            }
        }
    }

    // 2. Map 0..1 to -1..1 for BiasAlignment
    val verticalBias by remember {
        derivedStateOf { (scrollFraction * 2f) - 1f }
    }

    // 3. Only show if the list is actually scrollable
    val isScrollable by remember {
        derivedStateOf { listState.layoutInfo.totalItemsCount > listState.layoutInfo.visibleItemsInfo.size }
    }

    // 4. Show/Hide with animation
    AnimatedVisibility(
        visible = isScrollable && listState.isScrollInProgress,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(end = 4.dp, top = 12.dp, bottom = 12.dp)
                .width(4.dp)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .align(BiasAlignment(0f, verticalBias))
                    .fillMaxWidth()
                    .fillMaxHeight(0.1f) // Size of the "Pill"
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            )
        }
    }
}