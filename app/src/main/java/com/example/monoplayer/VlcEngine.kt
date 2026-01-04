package com.example.monoplayer

import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

@Composable
fun VlcEngine (vm: MyViewModel){
    val screen = vm.screen.collectAsState()
    val activity = LocalActivity.current as MainActivity
    val context = LocalContext.current
    val AllFiles = vm.AllFiles.collectAsState()
    val currentVideo = vm.currentVideo.collectAsState()

    BackHandler (enabled = screen.value == Screens.VideoPlayer) {
        vm.setScreen(Screens.Videos)
    }


    var video = AllFiles.value.find { it.id == currentVideo.value };

    LaunchedEffect (Unit) {
        vm.changeTitlePath(video!!.path.substringBeforeLast("/"))
    }

    val libVLC = remember { LibVLC(context, arrayListOf(
        "--file-caching=3000",
        "--network-caching=3000",
        "--codec=all",
        "--drop-late-frames",
        "--skip-frames"
    )) }
    val mediaPlayer = remember { MediaPlayer(libVLC) }


    DisposableEffect (Uri.parse(video?.uri))
    {

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        val uri = Uri.parse(video?.uri) ?: return@DisposableEffect onDispose {}

        // 1. Get the File Descriptor from the ContentResolver
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")

        pfd?.let {
            // 2. Create Media using the File Descriptor
            val media = Media(libVLC, pfd.fileDescriptor).apply {
                setHWDecoderEnabled(true, false) // Hardware on, but no frame-skipping
            }

            mediaPlayer.media = media
            mediaPlayer.setEventListener { event ->
                if (event.type == MediaPlayer.Event.Playing) {
                    mediaPlayer.position = video?.Time ?: 0f
                    mediaPlayer.setEventListener(null)
                }
            }
            mediaPlayer.play()

            onDispose {
                if (!mediaPlayer.isReleased) {
                    vm.ChangeTime(video!!.id, mediaPlayer.position)
                    vm.UpdateLastPlayed(video.path.substringBeforeLast("/"), video.id)
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
                    vm.ChangeTime(currentVideo.value, mediaPlayer.position)
                }
            } catch (e: Exception) {
            }

            mediaPlayer.stop()
            mediaPlayer.release()
            libVLC.release()
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    Box(modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { ctx ->
                VLCVideoLayout(ctx).apply {
                    // Attach the player to the layout
                    mediaPlayer.attachViews(this, null, false, false)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        PlayerControls(vm,video, mediaPlayer);
    }
}