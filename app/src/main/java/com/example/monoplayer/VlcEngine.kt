package com.example.monoplayer

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.io.File
import java.nio.file.Files


@Composable
fun VlcEngine(vm: MyViewModel)
 {
    val activity = LocalActivity.current as MainActivity
    val currentVideo = vm.currentVideo.collectAsState()
    val audioManager = remember { activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val lifecycleOwner = LocalLifecycleOwner.current

     val (libVLC, mediaPlayer) = remember {
         val lib = LibVLC(activity, arrayListOf(
             "--file-caching=1500",
             "--network-caching=1500",
             "--audio-resampler=soxr",
             "--clock-jitter=0",
             "--gain=2.0",
             "--no-stats",
             "--no-osd",
             "--sub-autodetect-file",
             "--no-video-title-show" // Don't waste time rendering title text
         ))
         val mp = MediaPlayer(lib)
         lib to mp
     }


     val focusChangeListener = remember {
         AudioManager.OnAudioFocusChangeListener { focusChange ->
             when (focusChange) {
                 AudioManager.AUDIOFOCUS_LOSS -> {
                     if (mediaPlayer.let { it != null && !it.isReleased }) {
                         mediaPlayer.pause()
                     }
                 }
                 AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                     if (mediaPlayer.let { it != null && !it.isReleased }) {
                         mediaPlayer.pause()
                     }
                 }
                 AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                     mediaPlayer.volume = 50
                 }
                 AudioManager.AUDIOFOCUS_GAIN -> {
                     mediaPlayer.volume = 100
                     mediaPlayer.play()
                 }
             }
         }
     }

     DisposableEffect(lifecycleOwner) {
         val observer = LifecycleEventObserver{ _, event ->
             when (event) {
                 Lifecycle.Event.ON_PAUSE -> {
                     vm.ChangeTime(currentVideo.value!!.VideoId, mediaPlayer.position)
                     vm.UpdateLastPlayed(currentVideo.value!!.path.substringBeforeLast("/"),currentVideo.value!!.VideoId)
                     audioManager.abandonAudioFocus(focusChangeListener)
                     mediaPlayer.pause()
                 }
                 Lifecycle.Event.ON_RESUME -> {
                     if (!mediaPlayer.isReleased) {
                         mediaPlayer.vlcVout.setWindowSize(activity.window.decorView.width, activity.window.decorView.height)
                         val result = audioManager.requestAudioFocus(
                             focusChangeListener,
                             AudioManager.STREAM_MUSIC,
                             AudioManager.AUDIOFOCUS_GAIN
                         )
                         if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                             mediaPlayer.play()
                         }
                     }
                 }
                 else -> {}
             }
         }
         lifecycleOwner.lifecycle.addObserver(observer)
         onDispose {
             lifecycleOwner.lifecycle.removeObserver(observer)
         }
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
            val videoFile = File(video.path)
            val media = Media(libVLC, fd.fileDescriptor).apply {
                setHWDecoderEnabled(true, true)
                addOption(":clock-jitter=0")
                addOption(":file-caching=1500")
                addOption(":no-interact")
            }
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

            audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            mediaPlayer.play()
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


