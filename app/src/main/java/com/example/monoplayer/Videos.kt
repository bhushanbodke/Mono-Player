package com.example.monoplayer

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlin.collections.getValue
import kotlin.text.substringAfterLast


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun videosPermission(vm:MyViewModel,onPermissionGranted:()->Unit) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    }
    else{
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionState = rememberPermissionState(permission)
    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            onPermissionGranted() // Tells MainActivity to refresh data
            vm.setScreen(Screens.Home) // Moves to Home screen
        }
    }


    if (!permissionState.status.isGranted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (permissionState.status.shouldShowRationale) {
                "The app needs to see your videos to play them. Please grant permission."
            } else {
                "Video permission is required for this app to work."
            }

            Text(textToShow, textAlign = TextAlign.Center,color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("Grant Permission",color = MaterialTheme.colorScheme.surface)
            }
        }
    }
}
@Composable
fun videos(vm: MyViewModel) {
    val folders by vm.foldersList.collectAsState() // Use the pre-calculated list
    val gridValue by vm.GridValue.collectAsState()
    val listState: LazyGridState = rememberLazyGridState()

    Box(Modifier.fillMaxSize())
    {
            LazyVerticalGrid(
                columns =  GridCells.Fixed(if(gridValue == 0) 1 else gridValue),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy( 8.dp),
                horizontalArrangement = Arrangement.spacedBy( 8.dp),
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
            )
            {
                items(folders.size, key = { folders[it].path}) { id ->
                    val folder = folders[id]
                    when (gridValue) {
                        1 -> ListView(vm, folder)
                        2 -> GridView(vm, folder)
                        else -> Grid3View(vm, folder)
                    }
                }

            }
        }
    }

@Composable
fun Grid3View(vm: MyViewModel,folder: FolderModel)
{
    Column(
        Modifier
            .height(120.dp)
            .clip(RoundedCornerShape(5.dp))
            .clickable
                (
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.secondary)
            )
            {
                vm.changeTitlePath(folder.path);
                vm.setScreen(Screens.Videos);
            }
    ) {
        Box(Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFF323634))
           , contentAlignment = Alignment.Center) {
            folderThumbs(vm,folder.previewThumbnails)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = folder.name.substringAfterLast("/"),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 5.dp,end = 2.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        );
    }
}
@Composable
fun GridView(vm: MyViewModel,folder: FolderModel)
{
    Column(
        Modifier
            .height(200.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable
                (
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.secondary)
            )
            {
                vm.changeTitlePath(folder.path);
                vm.setScreen(Screens.Videos);
            }
    ) {
        Box(Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF323634))
            .padding(2.dp), contentAlignment = Alignment.Center) {
            folderThumbs(vm,folder.previewThumbnails)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = folder.name.substringAfterLast("/"),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 10.dp,end = 10.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        );
        Row() {
            Spacer(Modifier.width(5.dp))
            TextInfo("${(folder.videoCount).toInt()}  videos")
            Spacer(Modifier.width(5.dp))
            TextInfo(folder.totalSize)
        }
    }
}
@Composable
fun ListView(vm: MyViewModel,folder: FolderModel)
{
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable
                (
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.secondary)
            )
            {
                vm.changeTitlePath(folder.path);
                vm.setScreen(Screens.Videos);
            }
    )
    {
        Box(
            Modifier
                .width(160.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF323634))
                .padding(2.dp)
            ,
            contentAlignment = Alignment.Center
        ) {
            folderThumbs(vm,folder.previewThumbnails)
        }
        Column() {
            Text(
                text = folder.name.substringAfterLast("/"),
                fontSize = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 10.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            );
            Row() {
                Spacer(Modifier.width(12.dp))
                TextInfo("${(folder.videoCount).toInt()}  videos")
                Spacer(Modifier.width(12.dp))
                TextInfo(folder.totalSize)
            }
        }
    }
}

@Composable
fun folderThumbs(vm: MyViewModel,paths:List<VideoModel>)
{
    Column(Modifier.fillMaxSize()) {

        // ROW 1
        Row(Modifier
            .weight(1f)
            .fillMaxWidth()) {
            Box(Modifier
                .weight(1f)
                .padding(2.dp)
                .fillMaxHeight()) {
                NoPreview(20,10);

                VideoThumbnail(paths[0]) }

            if (paths.size == 2 || paths.size == 4) {
                Box(Modifier
                    .weight(1f)
                    .padding(2.dp)
                    .fillMaxHeight()) {
                    NoPreview(20,10);

                    VideoThumbnail(paths[1]) }
            }
        }

        if (paths.size >= 3) {
            Row(Modifier
                .weight(1f)
                .fillMaxWidth()) {
                val indexForThird = if (paths.size == 3) 1 else 2
                Box(Modifier
                    .weight(1f)
                    .padding(2.dp)
                    .fillMaxHeight()) {
                    NoPreview(20,10);
                    VideoThumbnail(paths[indexForThird]) }

                val indexForFourth = if (paths.size == 3) 2 else 3
                Box(Modifier
                    .weight(1f)
                    .padding(2.dp)
                    .fillMaxHeight()) {
                    NoPreview(20,10);

                    VideoThumbnail(paths[indexForFourth]) }
            }
        }
    }
}