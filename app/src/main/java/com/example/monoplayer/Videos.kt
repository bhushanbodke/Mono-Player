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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


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

            Text(textToShow, textAlign = TextAlign.Center,color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("Grant Permission",color = MaterialTheme.colorScheme.surface)
            }
        }
    }
}
@Composable
fun videos(vm: MyViewModel) {
    val folderMap = vm.folderMap.collectAsState();
    val folderNames = folderMap.value.keys.toList();
    val listState = rememberLazyListState()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState,modifier = Modifier
            .fillMaxSize()
            .overscroll(rememberOverscrollEffect())) {
            items(folderNames.size) { id ->
                Spacer(modifier = Modifier.size(5.dp));
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
                            vm.changeTitlePath(folderMap.value.getValue(folderNames[id])[2]);
                            vm.setScreen(Screens.Videos);
                        }
                ) {
                    Box(
                        Modifier
                            .width(150.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF323634))
                            .padding(2.dp)
                        ,
                        contentAlignment = Alignment.Center
                    ) {
                        folderThumbs(vm,folderNames[id])
                    }
                    Column() {
                        Text(
                            text = folderNames[id].substringAfterLast("/"),
                            fontSize = 18.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 10.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        );
                        Row() {
                            Text(
                                text = "${(folderMap.value.getValue(folderNames[id])[0]).toInt()}  videos •" +
                                        " ${folderMap.value.getValue(folderNames[id])[1]}",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(start = 10.dp),
                                fontSize = 12.sp,
                                color = Color(0xFFacb0b0)
                            );
                        }
                    }
                }

            }

        }
        CustomScrollIndicatorLazyColumn(listState =listState)
    }
}

@Composable
fun folderThumbs(vm: MyViewModel,name:String){
    val videos = vm.getFourVideos(name)

    Column(Modifier.fillMaxSize()) {
        if (videos.isEmpty()) return@Column

        // ROW 1
        Row(Modifier.weight(1f).fillMaxWidth()) {
            Box(Modifier.weight(1f).padding(2.dp).fillMaxHeight()) { VideoThumbnail(videos[0].path) }

            if (videos.size == 2 || videos.size == 4) {
                Box(Modifier.weight(1f).padding(2.dp).fillMaxHeight()) { VideoThumbnail(videos[1].path) }
            }
        }

        if (videos.size >= 3) {
            Row(Modifier.weight(1f).fillMaxWidth()) {
                val indexForThird = if (videos.size == 3) 1 else 2
                Box(Modifier.weight(1f).padding(2.dp).fillMaxHeight()) { VideoThumbnail(videos[indexForThird].path) }

                val indexForFourth = if (videos.size == 3) 2 else 3
                Box(Modifier.weight(1f).padding(2.dp).fillMaxHeight()) { VideoThumbnail(videos[indexForFourth].path) }
            }
        }
    }
}