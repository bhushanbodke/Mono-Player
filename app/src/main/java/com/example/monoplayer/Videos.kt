package com.example.monoplayer

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun videosPermission(vm: MyViewModel, onPermissionGranted: () -> Unit) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionState = rememberPermissionState(permission)

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            onPermissionGranted()
            vm.setScreen(Screens.Home)
        }
    }

    if (!permissionState.status.isGranted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(R.drawable.twotone_folder_24),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (permissionState.status.shouldShowRationale)
                    "Grant permission to see your videos."
                else "Video permission is required.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { permissionState.launchPermissionRequest() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun videos(vm: MyViewModel) {
    val folders by vm.foldersList.collectAsState()
    val gridValue by vm.GridValue.collectAsState()
    val listState: LazyGridState = rememberLazyGridState()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (gridValue == 0) 1 else if (gridValue == 1) 1 else gridValue),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(folders.size, key = { folders[it].path }) { id ->
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
fun ListView(vm: MyViewModel, folder: FolderModel) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                vm.changeTitlePath(folder.path)
                vm.setScreen(Screens.Videos)
            },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp).height(100.dp)
        ) {
            Box(
                Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                folderThumbs(vm, folder.previewThumbnails)
            }
            Column(
                Modifier.padding(horizontal = 16.dp).weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = folder.name.substringAfterLast("/"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${folder.videoCount.toInt()} Videos • ${folder.totalSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GridView(vm: MyViewModel, folder: FolderModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable {
            vm.changeTitlePath(folder.path)
            vm.setScreen(Screens.Videos)
        }
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(140.dp)) {
                folderThumbs(vm, folder.previewThumbnails)
                // Video Count Badge
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${folder.videoCount.toInt()}",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = folder.name.substringAfterLast("/"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = folder.totalSize,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun Grid3View(vm: MyViewModel, folder: FolderModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                vm.changeTitlePath(folder.path)
                vm.setScreen(Screens.Videos)
            }
    ) {
        Box(
            Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            folderThumbs(vm, folder.previewThumbnails)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = folder.name.substringAfterLast("/"),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun folderThumbs(vm: MyViewModel, paths: List<VideoModel>) {
    Box(Modifier.fillMaxSize()) {
        if (paths.isEmpty()) {
            Icon(
                painter = painterResource(R.drawable.play_arrow_24),
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center).size(30.dp),
                tint = MaterialTheme.colorScheme.primary.copy(0.3f)
            )
        } else {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.weight(1f)) {
                    ThumbnailWrapper(paths.getOrNull(0), Modifier.weight(1f))
                    if (paths.size >= 2) ThumbnailWrapper(paths.getOrNull(1), Modifier.weight(1f))
                }
                if (paths.size >= 3) {
                    Row(Modifier.weight(1f)) {
                        ThumbnailWrapper(paths.getOrNull(2), Modifier.weight(1f))
                        ThumbnailWrapper(paths.getOrNull(3), Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ThumbnailWrapper(video: VideoModel?, modifier: Modifier) {
    Box(modifier.fillMaxSize().padding(0.5.dp)) {
        if (video != null) {
            VideoThumbnail(video)
        } else {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.05f)))
        }
    }
}