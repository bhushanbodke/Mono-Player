package com.example.monoplayer

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer


@Composable
fun VlcEngine(vm: MyViewModel)
 {
    val activity = LocalActivity.current as MainActivity
    val currentVideo = vm.currentVideo.collectAsState()
    val audioManager = remember { activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

     BackHandler (){
         vm.setScreen(Screens.Videos)
     }
    val (libVLC, mediaPlayer) = remember {
        val lib = LibVLC(activity, arrayListOf(
            "--file-caching=3000",
            "--network-caching=3000",
            "--codec=all",
            "--gain=2.0",
            "--audio-resampler=soxr"
        ))
        val mp = MediaPlayer(lib)
        lib to mp
    }

    // 2. This Effect runs every time the Video URI changes (Playlist switch)
    DisposableEffect(currentVideo.value?.uri) {
        vm.changeTitlePath(currentVideo.value!!.path.substringBeforeLast("/"))
        val video = currentVideo.value ?: return@DisposableEffect onDispose {}
        val uri = Uri.parse(video.uri) ?: return@DisposableEffect onDispose {}

        val pfd = try {
            activity.contentResolver.openFileDescriptor(uri, "r")
        } catch (e: Exception) {
            null
        }

        pfd?.let { fd ->
            val media = Media(libVLC, fd.fileDescriptor).apply {
                addOption(":sub-text-scale=${vm.subScale.value}")
                setHWDecoderEnabled(true, false)
            }

            // SETTING MEDIA SAFELY
            mediaPlayer.media = media

            mediaPlayer.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Playing -> {
                        if (mediaPlayer.position == 0f && video.Time > 0f) {
                            mediaPlayer.position = video.Time
                        }
                    }
                    MediaPlayer.Event.EndReached -> {
                        Handler(Looper.getMainLooper()).post {
                            vm.ChangeTime(video.VideoId, 0F, true)
                            vm.setScreen(Screens.Videos)
                        }
                    }
                }
            }

            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            mediaPlayer.play()

            // Cleanup just the MEDIA, not the PLAYER, when switching tracks
            onDispose {
                if (!mediaPlayer.isReleased) {
                    vm.ChangeTime(video.VideoId, mediaPlayer.position)
                    vm.UpdateLastPlayed(video.path.substringBeforeLast("/"),video.VideoId)
                    mediaPlayer.stop()
                }
                media.release()
                fd.close()
            }
        } ?: onDispose {}
    }

    // 3. Final cleanup for the Engine when leaving the player screen entirely
    DisposableEffect(Unit) {
        enterVideoMode(vm,activity);
        onDispose {
            if (!mediaPlayer.isReleased) {
                // We use try-catch because if media is gone, position access crashes
                try {
                    vm.ChangeTime(currentVideo.value!!.VideoId, mediaPlayer.position);
                    vm.UpdateLastPlayed(currentVideo.value!!.path.substringBeforeLast("/"),currentVideo.value!!.VideoId)
                } catch (e: Exception) { }
            mediaPlayer.release()
            libVLC.release()
            audioManager.abandonAudioFocus(null)
            exitVideoMode(activity);
            }
    }
    }

    VideoPlayer(vm, mediaPlayer, currentVideo.value!!)
}


