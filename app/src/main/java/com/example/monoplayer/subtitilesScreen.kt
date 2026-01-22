package com.example.monoplayer

import android.annotation.SuppressLint
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.videolan.libvlc.MediaPlayer
import java.io.File
import kotlin.collections.addAll

@Composable
fun subtitles( vm: MyViewModel
              ,mediaPlayer: MediaPlayer
              ,videoPath: String
              ,allsubtitles:Map<String,List<SubtitleLine>?>
              ,currentsubtitles: String
               ,changeAllSubtitles:(Map<String,List<SubtitleLine>?>)->Unit
              ,changeSubtitles:(String)->Unit
              ,function:()->Unit)
{

    BackHandler() {
        function()
    }
    LaunchedEffect(Unit) {
        mediaPlayer.pause()
    }
    var OpenDialogueState = remember { mutableStateOf(false) }
    val tracks = remember {
        mediaPlayer.spuTracks?.toList() ?: emptyList()
    }
    if(OpenDialogueState.value == false)
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
            Box(Modifier.fillMaxSize().blur(10.dp)) {

            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .width(700.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
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
                        text = "Subtitle settings",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp,vertical = 10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.padding(bottom = 10.dp)) {
                        Column(modifier = Modifier.weight(0.7f)) {
                            Text(
                                text = "APPEARANCE",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 13.dp)
                            )
                            SubSize(vm); //subtitle size
                            TransToggle(vm); // background transparency
                            BoldToggle(vm); //Bold
                            ShadowToggle(vm)//Shadow
                        }
                        Spacer(Modifier.fillMaxHeight().width(10.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        ){
                            Row(){
                                Text(
                                    text = "SUBTITLE SOURCE",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    modifier = Modifier
                                        .weight(1f)
                                )
                                Row(modifier = Modifier.clickable(){OpenDialogueState.value = true}) {
                                    Text(
                                        text = "Add Ext Subtitles",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        modifier = Modifier)
                                    Text(
                                        text = " +",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 20.sp,
                                        modifier = Modifier)
                                }
                            }
                            Column(Modifier.padding(start = 10.dp).verticalScroll(rememberScrollState()))
                            {

                                if (tracks.isNotEmpty()) {
                                    Text(
                                        modifier = Modifier.padding(top = 10.dp),
                                        text ="Embeded subtitles",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    for (track in mediaPlayer.spuTracks) {
                                        val isSelected = track.id == mediaPlayer.spuTrack
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                mediaPlayer.spuTrack = track.id
                                                function()
                                            })
                                        {

                                            Text(
                                                modifier = Modifier.weight(1f).padding(horizontal = 10.dp,vertical = 10.dp),
                                                text = track.name,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            )
                                            if(isSelected)Icon(imageVector = Icons.Default.CheckCircle,tint = MaterialTheme.colorScheme.primary, contentDescription = "check",)
                                        }
                                    }
                                }
                                if(allsubtitles.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        modifier = Modifier.padding(top = 10.dp),
                                        text ="External subtitles",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    for(sub in allsubtitles){
                                        val isSelected = sub.key == currentsubtitles
                                        Row(Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                changeSubtitles(sub.key)
                                            },verticalAlignment = Alignment.CenterVertically)
                                        {
                                            Text(
                                                text = sub.key,
                                                modifier = Modifier
                                                    .padding(horizontal = 10.dp,vertical = 10.dp)
                                                    .weight(1f),
                                                fontSize = 13.sp,
                                                lineHeight = 15.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isSelected) Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                tint = MaterialTheme.colorScheme.primary,
                                                contentDescription = "check",
                                            )
                                            else{
                                                Spacer(modifier = Modifier.width(10.dp))
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
    else{
        OpenDialogue(videoPath, allsubtitles,changeAllSubtitles,changeSubtitles,{OpenDialogueState.value = false})
    }
}


@Composable
fun OpenDialogue( videoPath: String
                  ,allsubtitles:Map<String,List<SubtitleLine>?>
                  ,changeAllSubtitles:(Map<String,List<SubtitleLine>?>)->Unit
                  ,changeSubtitles:(String)->Unit
                  ,hide: () -> Unit) {
    var parentDir = remember { mutableStateOf(File(videoPath).parentFile) }


    Box(Modifier.fillMaxSize().clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null){hide()}) {
        Box(Modifier
            .size(500.dp, 350.dp)
            .align(Alignment.BottomCenter)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(20.dp)
            )
            .background(MaterialTheme.colorScheme.surface.copy(0.8f))
            .padding(horizontal = 20.dp , vertical = 10.dp )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null){}
        ) {
            Column() {
                Row(Modifier.fillMaxWidth()) {
                    for(name in parentDir.value.path.substringAfter("emulated/").split("/")){
                        Text(text = "${name} /", modifier = Modifier.clickable {
                            val new_file = File(parentDir.value.path.substringBefore(name)+name)
                            if (new_file.isDirectory){parentDir.value = new_file}
                        },color= MaterialTheme.colorScheme.onSurface,fontSize = 18.sp,fontWeight = FontWeight.SemiBold)
                    }
                }
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    if(parentDir.value.path != "/storage/emulated/0"){
                        Row(Modifier.fillMaxWidth().padding(top = 10.dp).clickable(){
                            parentDir.value = parentDir.value.parentFile
                        }, verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.twotone_folder_24),modifier=Modifier.align(Alignment.CenterVertically).padding(5.dp), contentDescription = null,tint = MaterialTheme.colorScheme.onSurface)
                            Text(text = "...",color = MaterialTheme.colorScheme.onSurface,fontSize = 20.sp,)
                        }
                    }
                    parentDir.value.listFiles().forEach{file ->
                        if(file.name.endsWith(".srt") || file.isDirectory){
                            if(file.isDirectory){
                                Row(Modifier.fillMaxWidth().padding(top = 10.dp).clickable(){
                                    parentDir.value = file
                                }, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painterResource(R.drawable.twotone_folder_24),modifier=Modifier.align(Alignment.CenterVertically), contentDescription = null,tint = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "${file.name}",color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 10.dp),fontSize = 16.sp,)
                                }
                            }
                            else{
                                Text(text = "${file.name}",color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 10.dp).clickable{
                                    val temp = allsubtitles.toMutableMap()
                                    temp[file.name] = parseSrt(file)
                                    changeAllSubtitles(temp)
                                    changeSubtitles(file.name)
                                    hide()
                                },fontSize = 16.sp,)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ShadowToggle(vm: MyViewModel){
    val subSetting by vm.subtitleSettings.collectAsState()
    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = subSetting.isShadow,
                onValueChange = { vm.setSubSettings(isShadow = !subSetting.isShadow) },
                role = Role.Switch
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Shadow",
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            modifier = Modifier.scale(0.7f),
            checked = subSetting.isShadow,
            onCheckedChange = null
        )
    }
}

@Composable
fun BoldToggle(vm: MyViewModel){
    val subSetting by vm.subtitleSettings.collectAsState()
    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = subSetting.isTransperant,
                onValueChange = { vm.setSubSettings(isBold = !subSetting.isBold) },
                role = Role.Switch
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Bold Subtitle",
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            modifier = Modifier.scale(0.7f),
            checked = subSetting.isBold,
            onCheckedChange = null
        )
    }
}

@Composable
fun TransToggle(vm: MyViewModel){
    val subSetting by vm.subtitleSettings.collectAsState()
    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = subSetting.isTransperant,
                onValueChange = { vm.setSubSettings(isTransperant = !subSetting.isTransperant) },
                role = Role.Switch
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Transparent Background",
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            modifier = Modifier.scale(0.7f),
            checked = subSetting.isTransperant,
            onCheckedChange = null
        )
    }
}
@Composable
fun SubSize(vm: MyViewModel)
{
    val subSetting by vm.subtitleSettings.collectAsState()
        Column( modifier = Modifier
            .fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Font size",
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(start = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = {vm.setSubSettings(size = 20)}) {
                    Icon(imageVector = Icons.Default.Refresh ,tint=MaterialTheme.colorScheme.onSurface, contentDescription = "default size")
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(MaterialTheme.colorScheme.surface.copy(0.8f),RoundedCornerShape(40.dp))
            )
            {
                TextButton(onClick = {
                    vm.setSubSettings(size = subSetting.size - 1)
                },modifier = Modifier.padding(3.dp).background(Color.Gray.copy(0.3f), CircleShape)) {
                    Text(
                        text = "-",
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${subSetting.size}px",
                    modifier = Modifier.padding(horizontal = 5.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface

                )
                TextButton(onClick = {
                    vm.setSubSettings(size = subSetting.size + 1)
                },modifier = Modifier.padding(3.dp).background(Color.Gray.copy(0.3f), CircleShape)) {
                    Text(
                        text = "+",
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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
        Column(Modifier
            .align(Alignment.Center)
            .size(800.dp, 600.dp)
            .verticalScroll(rememberScrollState())
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp)
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
                    modifier=Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable {
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
                            modifier=Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .clickable {
                                    vm.downloadSubtitles(subtitle.second, context)
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
