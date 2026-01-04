package com.example.monoplayer

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(vm:MyViewModel) {
    val screen by vm.screen.collectAsState();
    val title by vm.titlePath.collectAsState();
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (vm.AllFiles.value.isEmpty())
        {
            Log.e("folders","requested data ")
            ListVideos(context,vm)
        }
    }
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.size(10.dp))
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
                Screens.Home ->  videosPermission(vm)
                Screens.Videos ->  FolderScreen(vm)
                else -> videosPermission(vm)
            }
        }
    }
        Icon(
            painter = painterResource(id = R.drawable.twotone_play_circle_24),
            contentDescription = "play",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.BottomEnd)
                .size(110.dp)
                .padding(bottom = 40.dp, end = 30.dp).clickable {
                    if(vm.lastPlayedFolder.value.find { it.folder == title } != null) {
                    vm.updateCurrentVideo(vm.lastPlayedFolder.value.find { it.folder == title }!!.lastVideoId)
                    }
                    if (screen != Screens.VideoPlayer) {
                        vm.setScreen(Screens.VideoPlayer)
                    }
                })

    }
}




