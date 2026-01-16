package com.example.monoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.videolan.libvlc.MediaPlayer

@Composable
fun subtitles(vm: MyViewModel, mediaPlayer: MediaPlayer,function:()->Unit){

    var IsDownloadOpen = remember { mutableStateOf(false) }

    if(IsDownloadOpen.value){
        DownloadSubs(vm, mediaPlayer);
    }
    else
    {
        Box(
            Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    function()
                })
        {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .width(600.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface.copy(0.8f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {})
            {
                Column(Modifier.align(Alignment.Center)) {
                    Text(
                        text = "Subtitles",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    )
                    Row() {
                        Column(modifier = Modifier.weight(1f)) {
                            SubSize(vm, mediaPlayer);
                            Row(Modifier.padding(start = 20.dp).clickable {IsDownloadOpen.value = true;}) {
                                Text(text="Download Subtitles",fontSize = 15.sp,color=Color.White)
                            }
                        }
                        VerticalDivider(
                            Modifier
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        )
                        {
                            if (!mediaPlayer.spuTracks.isNullOrEmpty()) {
                                if (mediaPlayer.spuTracks.size > 1) {
                                    for (track in mediaPlayer.spuTracks) {
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Row(Modifier.fillMaxWidth().clickable {
                                            mediaPlayer.spuTrack = track.id
                                            function()
                                        }) {
                                            Spacer(modifier = Modifier.width(30.dp))
                                            if (track.id == mediaPlayer.spuTrack) {
                                                Icon(
                                                    painterResource(R.drawable.baseline_check_circle_outline_24),
                                                    modifier = Modifier.size(30.dp),
                                                    contentDescription = "check",
                                                    tint = Color.White
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.size(30.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = track.name,
                                                color = if (track.id == mediaPlayer.spuTrack) Color.White else Color(
                                                    0xFFbfbfbf
                                                ),
                                                fontSize = 15.sp,
                                                fontWeight = if (track.id == mediaPlayer.spuTrack) FontWeight.Bold else FontWeight.Normal,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubSize(vm: MyViewModel, mediaPlayer: MediaPlayer)
{
    val subScale by vm.subScale.collectAsState()

        Column() {
            Text(
                text = "Subtitle Size",
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                fontSize = 22.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            {
                Text(
                    text = "-",
                    modifier = Modifier
                        .clickable {
                            if (subScale > 0.1f) {
                                val newScale = subScale - 0.1f
                                vm.subScale.value = newScale
                            }
                        },
                    fontSize = 60.sp,
                    color = Color.White
                );

                Text(
                    text = "${subScale}",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "+",
                    modifier = Modifier
                        .clickable {
                            if (subScale < 3f) {
                                val newScale = subScale + 0.1f
                                vm.subScale.value = newScale
                            }
                        },
                    fontSize = 60.sp,
                    color = Color.White
                );
            }
        }
}

@Composable
fun DownloadSubs(vm: MyViewModel,mediaPlayer: MediaPlayer){
    var subtitles = vm.downloadedSubs.collectAsState().value
    val currentVideo =  vm.currentVideo.collectAsState()
    var name = remember {mutableStateOf(currentVideo.value!!.name )};
    var Season =  remember {mutableStateOf(" ")};
    var Episode=  remember {mutableStateOf(" ")};
    val context = LocalContext.current

    Box(Modifier.fillMaxSize()){
        Column(Modifier.align(Alignment.Center).size(800.dp,600.dp).verticalScroll(rememberScrollState()).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).padding(10.dp)
            ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    modifier = Modifier.weight(2f),
                    value =name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Movie/Tv Name") },
                )
                TextField(
                    modifier = Modifier.weight(1f),
                    value = Season.value,
                    onValueChange = { Season.value = it },
                    label = { Text("Season") },
                )
                TextField(
                    modifier = Modifier.weight(1f),
                    value = Episode.value,
                    onValueChange = { Episode.value = it },
                    label = { Text("Episode") },
                )
                Icon(
                    painterResource(R.drawable.baseline_search_24),
                    contentDescription = null,
                    tint = Color.White,
                    modifier=Modifier.size(50.dp).clip(CircleShape).clickable {
                        vm.searchSubtitles(name.value)
                    },
                )
            }
            for (subtitle in subtitles){
                    Spacer(Modifier.size(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(subtitle.first, color = Color.White)
                        Icon(
                            painter = painterResource(R.drawable.arrow_circle_down_24),
                            contentDescription = null,
                            tint = Color.White,
                            modifier=Modifier.size(50.dp).clip(CircleShape).clickable {
                                vm.downloadSubtitles(subtitle.second,context)
                            }
                        );
                    }
            }
        }
    }
}