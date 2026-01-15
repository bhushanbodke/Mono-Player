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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm:MyViewModel) {
    val screen by vm.screen.collectAsState();
    var currentPath = vm.titlePath.collectAsState();
    var showSettings = remember { mutableStateOf(false) }
    var gridvalue = vm.GridValue.collectAsState();
    val scrollState = rememberScrollState()
    val isRefreshing by vm.isRefreshing.collectAsState()




    val title = if (screen == Screens.Home) {
        "Internal Storage/"
    } else {
        currentPath.value
    }
    LaunchedEffect(title) {
        scrollState.scrollTo(scrollState.maxValue)
    }
    Box(Modifier
        .fillMaxSize()
        .padding(5.dp)) {
    Column(Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.size(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .padding(start = 10.dp)
                    .horizontalScroll(scrollState)
            ) {
            for(word in title.split("/")){
                if(word=="") continue
                Text(
                    text = if(word.split(" ").size >= 3) word.split(" ").take(3).joinToString(" ")+"..." else word,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                );
                Spacer(modifier = Modifier.size(5.dp))
                Icon(
                    painterResource(R.drawable.baseline_arrow_forward_ios_24)
                    , contentDescription = "arrow"
                    ,modifier=Modifier.size(23.dp)
                    ,tint = MaterialTheme.colorScheme.onSurface)}
            }
        Row()
        {
            Spacer(Modifier
                .fillMaxWidth()
                .weight(1f))
            IconButton(onClick = {vm.toggleGrid()}) {
                Icon(painter =painterResource(
                    when(gridvalue.value) {
                        1 -> {R.drawable.view_list_24}
                        2 -> {R.drawable.grid_view_24}
                        else -> {R.drawable.grid_on_24}
                    }
                )
                    , contentDescription = "view",
                    Modifier.size(25.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Box() {
                IconButton(onClick = {showSettings.value = true}) {
                    Icon(
                        painter = painterResource(R.drawable.sort_24), "sort",
                        Modifier.size(25.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { vm.refreshDataWithLoading() },
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
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
                when (targetScreen) {
                    Screens.Home -> videos(vm)
                    Screens.Videos -> FolderScreen(vm)
                    else -> videos(vm)
                }
            }
        }
    }
            IconButton(
                modifier=Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 25.dp, end = 20.dp)
                    .size(75.dp),
                onClick = {
                    var id = 0L;
                    id = vm.lastPlayedFolder.value.find { it.folder == title }?.lastVideoId?:0L
                    if (id != 0L) {
                        val video = vm.AllFiles.value.find { it.VideoId == id };
                        if(video!=null)vm.updateCurrentVideo(video)
                        vm.setScreen(Screens.VideoPlayer)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.twotone_play_circle_24),
                    contentDescription = "play",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

        AnimatedVisibility(visible = showSettings.value,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ){
            sortScreen(vm,{ showSettings.value = false })
        }
    }
}




@Composable
fun sortScreen(vm:MyViewModel,hide:()->Unit){
    var sort = vm.sort.collectAsState();
    Box(Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() },indication = null){
        hide();
    }){
        Box(Modifier.fillMaxWidth().height(430.dp).clip(RoundedCornerShape(10.dp)).align(Alignment.BottomCenter).background(MaterialTheme.colorScheme.background).padding(20.dp)) {
            Column() {
                Text(text = "SORT BY" , color = MaterialTheme.colorScheme.primary , fontSize = 20.sp)
                Spacer(modifier = Modifier.size(5.dp))
                for (s in appsort.values()) {
                    val isSelected = sort.value == s.id
                    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.fillMaxWidth().padding(10.dp).clickable
                        (interactionSource = remember { MutableInteractionSource() },indication = null)
                    {
                        vm.updateSort(s.id)
                        hide()
                    }){
                        Text(text = s.displayName,modifier = Modifier.weight(1f), color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White, fontSize = 15.sp,)
                        if(isSelected)Icon(imageVector = Icons.Default.Check ,tint = MaterialTheme.colorScheme.primary, contentDescription = "check",)
                    }
                }
            }
        }
    }
}