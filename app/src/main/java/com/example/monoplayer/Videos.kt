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
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun videosPermission(vm:MyViewModel) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    }
    else{
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionState = rememberPermissionState(permission)
    if (permissionState.status.isGranted) {
        videos(vm)
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (permissionState.status.shouldShowRationale) {
                "The app needs to see your videos to play them. Please grant permission."
            } else {
                "Video permission is required for this app to work."
            }

            Text(textToShow, textAlign = TextAlign.Center,color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    }
}
@Composable
fun videos(vm: MyViewModel) {
    LaunchedEffect(Unit) {
        vm.updated_folder()
    }
    val folderMap = vm.folderMap.collectAsState();
    val folderNames = folderMap.value.keys.toList();
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().overscroll(rememberOverscrollEffect())) {
            items(folderNames.size) { id ->
                Spacer(modifier = Modifier.size(5.dp));
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(10.dp).height(100.dp)
                        .clip(RoundedCornerShape(10.dp)).clickable
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
                        Modifier.size(70.dp).clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.twotone_folder_24),
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.secondary,
                            contentDescription = "folder"
                        )
                    }
                    Column() {
                        Text(
                            text = folderNames[id],
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 10.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        );
                        Row() {
                            Text(
                                text = "${(folderMap.value.getValue(folderNames[id])[0]).toInt()}  videos",
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                    .padding(start = 10.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            );
                            Text(
                                text = folderMap.value.getValue(folderNames[id])[1],
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                    .padding(start = 10.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            );

                        }
                    }
                }

            }

        }

    }
}
