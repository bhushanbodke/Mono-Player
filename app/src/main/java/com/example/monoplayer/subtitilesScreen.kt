package com.example.monoplayer

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.videolan.libvlc.MediaPlayer

@Composable
fun subtitles(vm: MyViewModel, mediaPlayer: MediaPlayer,function:()->Unit){
    Box(Modifier
        .fillMaxSize()
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            function()
        })
    {
        Box(Modifier
            .align(Alignment.Center)
            .width(500.dp)
            .height(350.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(0.5f))
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

@Composable
fun SubSize(vm: MyViewModel, mediaPlayer: MediaPlayer) {
    val subScale by vm.subScale.collectAsState()
    Column() {
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

