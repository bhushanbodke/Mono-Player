package com.example.monoplayer

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(vm:MyViewModel) {
    val screen by vm.screen.collectAsState();
    var currentPath = vm.titlePath.collectAsState();

    val title = if (screen == Screens.Home) {
        "Internal Storage/"
    } else {
        currentPath.value
    }

    Box(Modifier.fillMaxSize().padding(10.dp)) {
    Column(Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.size(20.dp))
        Box(
            Modifier
                .padding(10.dp, top = 5.dp, end = 10.dp).height(40.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 10.dp).horizontalScroll(rememberScrollState())
            ) {
            for(word in title.split("/")){
                if(word=="") continue
                Text(
                    text = if(word.split(" ").size >= 3) word.split(" ").take(3).joinToString(" ")+"..." else word,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                );
                Icon(painter = painterResource(R.drawable.baseline_arrow_forward_ios_24)
                    , contentDescription = "arrow"
                    ,modifier=Modifier.size(23.dp)
                    ,tint = MaterialTheme.colorScheme.primary)}
            }
        }
        Spacer(modifier = Modifier.size(5.dp))


        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                // If the new screen is higher than the old one, slide left (forward)
                // Otherwise, slide right (backward)
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(
                        slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()).togetherWith(
                        slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "ScreenTransition"
        ) { targetScreen ->
            // This is where your logic goes
            when (targetScreen) {
                Screens.Home ->  videos(vm)
                Screens.Videos ->  FolderScreen(vm)
                else -> videos(vm)
            }
        }
    }
        Box(
            Modifier.align(Alignment.BottomEnd).padding(bottom = 25.dp, end = 20.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    var id = 0L;
                    id = vm.lastPlayedFolder.value.find { it.folder == title }?.lastVideoId?:0L
                    if (id != 0L) {
                        val video = vm.AllFiles.value.find { it.VideoId == id };
                        if(video!=null)vm.updateCurrentVideo(video)
                        vm.setScreen(Screens.VideoPlayer)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable._767328470870_removebg_preview),
                contentDescription = "play",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(65.dp)
                    )
        }
    }
}




