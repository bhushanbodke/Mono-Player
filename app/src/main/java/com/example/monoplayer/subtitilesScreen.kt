package com.example.monoplayer

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.CheckCircle
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
import java.io.File
import kotlin.collections.addAll

@Composable
fun subtitles(vm: MyViewModel
              , mediaPlayer: MediaPlayer
              ,allsubtitles:Map<String,List<SubtitleLine>?>
              ,currentsubtitles: String
              ,changeSubtitles:(String)->Unit
              ,function:()->Unit){

    BackHandler() {
        function()
    }


    var IsDownloadOpen = remember { mutableStateOf(false) }
    val tracks = remember {
        mediaPlayer.spuTracks?.toList() ?: emptyList()
    }
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
                    .align(Alignment.BottomCenter)
                    .width(600.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(20.dp)
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
                            .padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(Modifier.padding(bottom = 10.dp)) {
                        Column(modifier = Modifier.weight(0.5f)) {
//                            SubSize(vm, mediaPlayer);
//                            Row(Modifier.padding(start = 20.dp).clickable {IsDownloadOpen.value = true;}) {
//                                Text(text="Download Subtitles",fontSize = 15.sp,color=Color.White)
//                            }
                        }
                        VerticalDivider(
                            Modifier
                                .fillMaxHeight()
                                .width(1.5.dp)
                                .padding(bottom = 20.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                                .verticalScroll(rememberScrollState())
                        )
                        {
                            if (tracks.isEmpty()) {
                            } else {
                                    for (track in mediaPlayer.spuTracks) {
                                        val isSelected = track.id == mediaPlayer.spuTrack
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Row(Modifier.fillMaxWidth().clickable {
                                            mediaPlayer.spuTrack = track.id
                                            function()
                                        })
                                        {
                                            Text(
                                                modifier = Modifier.weight(1f),
                                                text = track.name,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            )
                                            if(isSelected)Icon(imageVector = Icons.Default.CheckCircle,tint = MaterialTheme.colorScheme.primary, contentDescription = "check",)
                                        }
                                    }
                            }
                            for(sub in allsubtitles){
                                val isSelected = sub.key == currentsubtitles
                                Row(Modifier.fillMaxWidth().clickable {
                                    changeSubtitles(sub.key)
                                },verticalAlignment = Alignment.CenterVertically)
                                {
                                    Text(
                                        text = sub.key,
                                        modifier = Modifier.padding(vertical = 10.dp).weight(1f),
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                        contentDescription = "check",
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

fun findsubs(
    context: Context,
    videoPath: String
): List<File> {
    val videoFile = File(videoPath)
    val parentDir = videoFile.parentFile ?: return emptyList()

    if (!parentDir.exists()) {
        parentDir.mkdirs()
    }
    val subtitleFiles = parentDir.listFiles { file ->
        file.name.endsWith(".srt")
    }
    return subtitleFiles.toList()
}
