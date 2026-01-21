package com.example.monoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.videolan.libvlc.MediaPlayer

@Composable
fun modernUI(vm: MyViewModel,
             mediaPlayer: MediaPlayer,
             activity: MainActivity,
             showLock: () -> Unit,
             showSubtitle: () -> Unit,
             togglePlaylist: () -> Unit,
             showAudioSelector: () -> Unit,
             onAction: () -> Unit,
             changeAspectRatio: () -> Unit)
{
    // LEFT PILL (Settings)
    Spacer(Modifier.width(20.dp))
    ControlPill {
        ModernOrientationButton(vm)
        IconButton(onClick = { showLock() ;onAction()}) {
            Icon(painterResource(R.drawable.twotone_lock_24), null, tint = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        IconButton(onClick = { showAudioSelector();onAction() }) {
            Icon(painterResource(R.drawable.baseline_music_note_24), null, tint = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        IconButton(onClick = {
            changeAspectRatio()
            onAction()
        }) {
            Icon(painterResource(R.drawable.fit_screen_24), null, tint = Color.White)
        }
    }
    // RIGHT PILL (Tools)
    ControlPill {
        IconButton(onClick = { /* BG Play logic */ }) {
            Icon(painterResource(R.drawable.headphones_24), null, tint = Color.White)}
        Spacer(Modifier.width(12.dp))
        IconButton(onClick = { showSubtitle() ;onAction()}) {
            Icon(painterResource(R.drawable.twotone_subtitles_24), null, tint = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        IconButton(onClick = {
            vm.isPip.value = true
            activity.enterPipMode(mediaPlayer.isPlaying);
            onAction()
        }) {
            Icon(painterResource(R.drawable.baseline_picture_in_picture_alt_24), null, tint = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        IconButton(onClick = { togglePlaylist();onAction() }) {
            Icon(painterResource(R.drawable.playlist_play_24), null, tint = Color.White)
        }
    }
    Spacer(Modifier.width(20.dp))
}


@Composable
fun ControlPill(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(0.3f))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}