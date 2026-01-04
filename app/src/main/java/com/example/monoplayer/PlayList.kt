package com.example.monoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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

@Composable
fun Playlist(vm: MyViewModel
    ,ToggleShowControls:()->Unit
    ){
    val folderFiles = vm.folderFiles.collectAsState()
    val currentVideo = vm.currentVideo.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier=Modifier.fillMaxSize().padding(top = 10.dp, bottom = 10.dp)
    ) {
        Box(Modifier.size(50.dp).clip(shape = CircleShape).background(MaterialTheme.colorScheme.surface.copy(0.8f))
            .clickable{ToggleShowControls()}
            , contentAlignment = Alignment.Center
        )
        {
            Icon(painterResource(R.drawable.baseline_arrow_forward_ios_24), contentDescription = "back",
                Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
        }
            LazyColumn(Modifier.fillMaxHeight().width(450.dp)
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                .overscroll(rememberOverscrollEffect())) {
                items(folderFiles.value.size){id->
                    Spacer(modifier = Modifier.size(5.dp));
                    Row(
                        modifier = Modifier.fillMaxWidth().height(75.dp).padding(start = 5.dp, end = 5.dp)
                            .clickable
                                (
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = MaterialTheme.colorScheme.primary))
                            {
                                vm.updateCurrentVideo(folderFiles.value[id].id);
                            }
                    ) {
                        if (currentVideo.value == folderFiles.value[id].id)
                            Spacer(
                                modifier = Modifier.height(50.dp).width(5.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            );
                        else
                            Spacer(modifier = Modifier.fillMaxHeight().width(20.dp));
                        Spacer(modifier = Modifier.width(5.dp));
                        Box(Modifier.size(100.dp, 62.dp)) {
                            VideoThumbnail(folderFiles.value[id].path, 100, 60)
                            if (folderFiles.value[id].Time > 0f) {
                                Box(Modifier.align(Alignment.BottomCenter)) {
                                    SquareProgressBar(progress = folderFiles.value[id].Time, 2)
                                }
                            }
                        }
                        Column() {
                        Text(
                            text = folderFiles.value[id].name,
                            Modifier.padding(start = 10.dp, end = 20.dp),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.W600,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = formatTime(folderFiles.value[id].duration.toLong()),
                            Modifier.padding(start = 10.dp, end = 10.dp),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.W200,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    }
                }
            }
    }
}
@Composable
fun PlaylistP(vm: MyViewModel
             ,ToggleShowControls:()->Unit
){
    val folderFiles = vm.folderFiles.collectAsState()
    val currentVideo = vm.currentVideo.collectAsState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier=Modifier.fillMaxSize().padding(top = 10.dp, bottom = 10.dp)
    ) {
        Box(Modifier.size(50.dp).clip(shape = CircleShape).background(MaterialTheme.colorScheme.surface.copy(0.8f))
            .clickable{ToggleShowControls()}
            , contentAlignment = Alignment.Center
        )
        {
            Icon(painterResource(R.drawable.baseline_keyboard_arrow_down_24), contentDescription = "back",
                Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
        }
        LazyRow(Modifier.fillMaxWidth().height(300.dp)
            .weight(1f)
            .padding(start=5.dp,end=5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Transparent)
            .overscroll(rememberOverscrollEffect())) {
            items(folderFiles.value.size){id->
                Spacer(modifier = Modifier.size(2.dp));
                Column(
                    modifier = Modifier.width(120.dp).fillMaxHeight().padding(start = 5.dp, end = 5.dp)
                        .clickable
                            (
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = MaterialTheme.colorScheme.primary))
                        {
                            vm.updateCurrentVideo(folderFiles.value[id].id);
                        }
                ) {
                    if(currentVideo.value==folderFiles.value[id].id)
                        Spacer(modifier = Modifier.height(5.dp).width(110.dp).background(MaterialTheme.colorScheme.primary));
                    else
                        Spacer(modifier = Modifier.height(15.dp).width(110.dp));
                    Spacer(modifier = Modifier.height(5.dp));
                    Box(Modifier.size(100.dp, 62.dp)) {
                        VideoThumbnail(folderFiles.value[id].path, 120, 150)
                        if (folderFiles.value[id].Time > 0f) {
                            Box(Modifier.align(Alignment.BottomCenter)) {
                                SquareProgressBar(progress = folderFiles.value[id].Time,2)
                            }
                        }
                    }
                    Text(text = folderFiles.value[id].name
                        ,Modifier.padding(top = 2.dp, bottom = 2.dp)
                        , maxLines = 3
                        , overflow = TextOverflow.Ellipsis
                        , fontSize = 8.sp
                        , lineHeight = 10.sp
                        , textAlign = TextAlign.Center
                        , fontWeight = FontWeight.W600
                        , color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(text = formatTime(folderFiles.value[id].duration.toLong())
                        ,Modifier.padding(start = 10.dp, end = 10.dp)
                        , fontSize = 8.sp
                        , fontWeight = FontWeight.W200
                        , color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
