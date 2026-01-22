package com.example.monoplayer

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.example.monoplayer.ui.theme.MonoPlayerTheme

class MainActivity : ComponentActivity() {
    lateinit var vm: MyViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        vm = ViewModelProvider(this)[MyViewModel::class.java]
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonoPlayerTheme(vm) {
                main_screen(vm);
            }
        }
    }
    override fun onStop() {
        super.onStop()
        vm.Save_data_files();
        vm.saveAllSettings();
    }

    fun getScreenRatio(): Pair<Int, Int> {
        val metrics = resources.displayMetrics
        val width = maxOf(metrics.widthPixels, metrics.heightPixels)
        val height = minOf(metrics.widthPixels, metrics.heightPixels)
        return Pair(width, height)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        vm.isPip.value = isInPictureInPictureMode
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun enterPipMode(isPlaying:Boolean) {
        val intent = Intent("ACTION_VIDEO_CONTROL")
        val pendingIntent = PendingIntent.getBroadcast(
            this, if (isPlaying) 1 else 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val icon = if (isPlaying) R.drawable.twotone_pause_circle_24 else R.drawable.twotone_play_circle_24

        val action = RemoteAction(
            Icon.createWithResource(this, icon),
            "Play/Pause",
            "Play/Pause",
            pendingIntent
        )
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9)) // Set to your video's aspect ratio
            .build()
        enterPictureInPictureMode(params)
    }
}

@Composable
fun main_screen(vm:MyViewModel) {
    val screen = vm.screen.collectAsState().value;
    val context = LocalContext.current

    var videoPer = remember { mutableStateOf(false) };


    LaunchedEffect(videoPer.value) {
        if (hasVideoPermission(context) || videoPer.value) {
                vm.refreshData();

        } else {
            vm.setScreen(Screens.permissions)
        }
    }


    if (screen != Screens.VideoPlayer) {
        Surface(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        )
        {
            if(screen != Screens.settings){
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                {
                    when(screen){
                        Screens.permissions -> {videosPermission(vm,onPermissionGranted={videoPer.value=true})}
                        Screens.Home -> {
                            Column(
                            )
                            {
                                Row(
                                    Modifier.fillMaxWidth().height(100.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .padding(top = 50.dp, start = 10.dp, end = 20.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                )
                                {
                                    Spacer(Modifier.width(20.dp))
                                    Text(
                                        text = "MONO",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 50.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row() {
                                        IconButton(onClick = { vm.setScreen(Screens.settings) }) {
                                            Icon(
                                                imageVector = Icons.Default.Settings, "settings",
                                                Modifier.size(30.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                HomeScreen(vm)
                            }
                        }
                        else->{
                            Column(
                            )
                            {
                                Row(
                                    Modifier.fillMaxWidth().height(80.dp)
                                        .padding(top = 50.dp, start = 10.dp, end = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                )
                                {
                                    IconButton(onClick = {vm.setScreen(Screens.Home)}) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back",
                                            Modifier.size(30.dp),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "MONO",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 30.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { vm.setScreen(Screens.settings) }) {
                                        Icon(
                                            imageVector = Icons.Default.Settings, "settings",
                                            Modifier.size(30.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                HomeScreen(vm)
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = screen == Screens.settings,
                enter = slideInHorizontally { it } ,
                exit = slideOutHorizontally { it }
            ) {
                SettingsScreen(vm);
            }
        }
    }
    else{
        VlcEngine(vm);
    }
}

fun hasVideoPermission(context: android.content.Context): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.READ_MEDIA_VIDEO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
