package com.example.monoplayer

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

@Composable
fun VlcEngine (vm: MyViewModel){
    val screen = vm.screen.collectAsState()
    val activity = LocalActivity.current as MainActivity
    val currentVideo = vm.currentVideo.collectAsState()
    val context = LocalActivity.current
    val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager


    BackHandler (enabled = screen.value == Screens.VideoPlayer) {
        vm.setScreen(Screens.Videos)
    }



    LaunchedEffect (Unit) {
        vm.changeTitlePath(currentVideo.value!!.path.substringBeforeLast("/"))
    }



    vm.libVLC = remember { LibVLC(vm.context, arrayListOf(
        "--file-caching=3000",
        "--network-caching=3000",
        "--codec=all",
        "--drop-late-frames",
        "--skip-frames",
        "--audio-resampler=soxr"
    )) }
    val mediaPlayer = remember { MediaPlayer(vm.libVLC) }


    DisposableEffect (Uri.parse(currentVideo.value?.uri))
    {

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        val uri = Uri.parse(currentVideo.value?.uri) ?: return@DisposableEffect onDispose {}

        // 1. Get the File Descriptor from the ContentResolver
        val pfd = vm.context.contentResolver.openFileDescriptor(uri, "r")

        pfd?.let {
            // 2. Create Media using the File Descriptor
            val media = Media(vm.libVLC, pfd.fileDescriptor).apply {
                addOption(":sub-text-scale=${vm.subScale.value}")
                setHWDecoderEnabled(true, false) // Hardware on, but no frame-skipping
            }

            mediaPlayer.media = media
            mediaPlayer.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Playing -> {
                        // Only set position once. Use a flag or check if position is still 0
                        if (mediaPlayer.position == 0f && currentVideo.value?.Time != 0f) {
                            mediaPlayer.position = currentVideo.value?.Time ?: 0f
                        }
                    }
                    MediaPlayer.Event.ESAdded -> {
                        val tracks = mediaPlayer.spuTracks
                        if (tracks != null && tracks.size > 1 && mediaPlayer.spuTrack == -1) {
                            mediaPlayer.spuTrack = tracks[1].id
                        }
                    }
                    MediaPlayer.Event.EndReached -> {
                        vm.ChangeTime(currentVideo.value!!.VideoId, 0F,true)
                    }
                }
            }

            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            mediaPlayer.play()

            onDispose {
                if (!mediaPlayer.isReleased) {
                    vm.ChangeTime(currentVideo.value!!.VideoId, mediaPlayer.position)
                    vm.UpdateLastPlayed("Internal Storage/", currentVideo.value!!.VideoId)
                    vm.UpdateLastPlayed(vm.titlePath.value, currentVideo.value!!.VideoId)
                }
                media.release()
                it.close()
            }
        } ?: onDispose { }
    }
    // 3. Display the Video
    DisposableEffect(Unit) {
        onDispose {
            try {
                if (!mediaPlayer.isReleased && mediaPlayer.hasMedia()) {
                    vm.ChangeTime(currentVideo.value!!.VideoId, mediaPlayer.position)
                    vm.UpdateLastPlayed("Internal Storage/", currentVideo.value!!.VideoId)
                    vm.UpdateLastPlayed(vm.titlePath.value, currentVideo.value!!.VideoId)

                }
            } catch (e: Exception) {
            }

            mediaPlayer.stop()
            mediaPlayer.release()
            vm.libVLC.release()
            audioManager.abandonAudioFocus(null)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    VideoPlayer(vm,mediaPlayer,currentVideo.value!!)
}

