package com.example.monoplayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.videolan.libvlc.MediaPlayer

@Composable
fun classicUI(vm: MyViewModel,
              mediaPlayer: MediaPlayer,
              activity: MainActivity,
              showLock: () -> Unit,
              showSubtitle: () -> Unit,
              togglePlaylist: () -> Unit,
              showAudioSelector: () -> Unit,
              onAction: () -> Unit,
              changeAspectRatio: () -> Unit)
{
    Spacer(Modifier.width(20.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally)  {
        ModernOrientationButton(vm)
        Text(text = "Orientation",color = Color.White, lineHeight = 10.sp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { showLock() ;onAction()}) {
            Icon(painterResource(R.drawable.twotone_lock_24), null, tint = Color.White)
        }
        Text(text = "Lock",color = Color.White, lineHeight = 10.sp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { showAudioSelector();onAction() }) {
            Icon(painterResource(R.drawable.baseline_music_note_24), null, tint = Color.White)
        }
        Text(text = "Audio",color = Color.White, lineHeight = 10.sp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = {
            changeAspectRatio()
            onAction()
        }) {
            Icon(painterResource(R.drawable.fit_screen_24), null, tint = Color.White)
        }
        Text(text = "Aspect Ratio",color = Color.White, lineHeight = 10.sp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { /* BG Play logic */ }) {
            Icon(painterResource(R.drawable.headphones_24), null, tint = Color.White)}
        Text(text = "Background",color = Color.White, lineHeight = 10.sp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { showSubtitle() ;onAction()}) {
            Icon(painterResource(R.drawable.twotone_subtitles_24), null, tint = Color.White)
        }
        Text(text = "Subtitles",color = Color.White, lineHeight = 10.sp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = {
            vm.isPip.value = true
            activity.enterPipMode(mediaPlayer.isPlaying);
            onAction()
        }) {
            Icon(painterResource(R.drawable.baseline_picture_in_picture_alt_24), null, tint = Color.White)
        }
        Text(text = "Pip",color = Color.White, lineHeight = 10.sp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { togglePlaylist();onAction() }) {
            Icon(painterResource(R.drawable.playlist_play_24), null, tint = Color.White)
        }
        Text(text = "Playlist",color = Color.White, lineHeight = 10.sp, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.width(20.dp))
}