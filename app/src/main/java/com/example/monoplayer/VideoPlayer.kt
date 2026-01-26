package com.example.monoplayer

import android.content.Context
import android.view.SurfaceView
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.flow.MutableStateFlow
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File
import java.nio.file.Files

@Composable
fun VideoPlayer(vm: MyViewModel, mediaPlayer: MediaPlayer, currentVideo: VideoModel) {
    var playlistVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isPip = vm.isPip.collectAsState().value
    val transitionProgress by animateFloatAsState(
        targetValue = if (playlistVisible) 1f else 0f,
        animationSpec = tween (400)
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { context ->
                SurfaceView(context).apply {
                    keepScreenOn = true
                    holder.setFormat(android.graphics.PixelFormat.RGBX_8888)
                    val vout = mediaPlayer.vlcVout

                    holder.addCallback(object : android.view.SurfaceHolder.Callback {
                        override fun surfaceCreated(p0: android.view.SurfaceHolder) {
                            if (vout.areViewsAttached()) {
                                vout.detachViews()
                            }
                            vout.setVideoView(this@apply)
                            vout.attachViews()
                        }
                        override fun surfaceChanged(p0: android.view.SurfaceHolder, format: Int, width: Int, height: Int) {
                            vout.setWindowSize(width, height)
                        }

                        override fun surfaceDestroyed(p0: android.view.SurfaceHolder) {
                            vout.detachViews()
                        }
                    })
                }
            },
            update = { view ->
                val vout = mediaPlayer.vlcVout
                // If it's not attached (like after returning to app), force re-attach
                if (!vout.areViewsAttached()) {
                    vout.setVideoView(view)
                    vout.attachViews()
                }
            },
            modifier = Modifier.fillMaxSize()
                .graphicsLayer{
                    val scale = 1f - (transitionProgress * 0.4f)
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    translationY = 0f
                    shape = RoundedCornerShape(20.dp)

                }
        )

        if(!isPip){
            PlayerControls(vm,currentVideo,
                mediaPlayer
                ,togglePlaylist = {playlistVisible = !playlistVisible}
                ,togglePlaylistFalse = {playlistVisible = false}
            );
        }
    }
}

fun hide_layout(activity: MainActivity){
    WindowCompat.getInsetsController(activity.window, activity.window.decorView).hide(
        WindowInsetsCompat.Type.systemBars()
    )
}
fun show_layout(activity: MainActivity){
    WindowCompat.getInsetsController(activity.window, activity.window.decorView).show(
        WindowInsetsCompat.Type.systemBars()
    )
}

