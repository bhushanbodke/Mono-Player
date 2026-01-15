package com.example.monoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.videolan.libvlc.MediaPlayer

@Composable
fun audioSelector(vm: MyViewModel, mediaPlayer: MediaPlayer,function:()->Unit){
    val tracks: Array<MediaPlayer.TrackDescription>? = mediaPlayer.audioTracks
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
                .width(400.dp)
                .clip(RoundedCornerShape(30.dp))
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(30.dp)
                )
                .background(MaterialTheme.colorScheme.surface.copy(0.8f))
                .padding(20.dp)
                .clickable(enabled = false) {}
                )
        {
            Column() {
                Text(text = "Audio Tracks",color = MaterialTheme.colorScheme.primary, fontSize = 23.sp)
                tracks?.forEach { track ->
                    val isSelected = track.id == mediaPlayer.audioTrack
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth().clickable {
                        selectAudioTrack(mediaPlayer, track.id)
                        function()
                    })
                    {
                        Text(modifier = Modifier.weight(1f),
                            text = track.name,fontSize = 15.sp,
                            color = (if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface))
                        if(isSelected)Icon(imageVector = Icons.Default.CheckCircle,tint = MaterialTheme.colorScheme.primary, contentDescription = "check",)
                    }
                }
            }
        }
    }
}

fun selectAudioTrack(mediaPlayer: MediaPlayer, index: Int) {
    val tracks = mediaPlayer.audioTracks
    if (tracks != null && index < tracks.size) {
        mediaPlayer.audioTrack = tracks[index].id
    }
}