package com.example.monoplayer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            MonoPlayerTheme {
                main_screen(vm);
            }
        }
    }
    override fun onStop() {
        super.onStop()
        vm.Save_data_files();
    }
}

@Composable
fun main_screen(vm:MyViewModel) {
    val screen = vm.screen.collectAsState();
    vm.context = LocalContext.current

    val isLoading = remember { mutableStateOf(false) }
    var videoPer = remember { mutableStateOf(false) };
    val hasLoadedInitial = remember { mutableStateOf(false) }
    var showSettings = remember { mutableStateOf(false) }
    var settingSort = remember { mutableStateOf(0) }

    LaunchedEffect(videoPer.value) {
        if (hasVideoPermission(vm.context) || videoPer.value) {
            if (!hasLoadedInitial.value) {
                isLoading.value = true

                vm.refreshData(vm.context, vm, isLoading = {
                    isLoading.value = false
                    hasLoadedInitial.value = true
                    vm.updated_folder()
                })
            }
        } else {
            vm.setScreen(Screens.permissions)
        }
    }


    if (screen.value != Screens.VideoPlayer) {
        Surface(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
        )
        {
            Box(Modifier.fillMaxSize())
            {
                if(screen.value == Screens.permissions){videosPermission(vm,onPermissionGranted={videoPer.value=true})}
                else{
                Column(
                )
                {
                    Row(
                        Modifier.fillMaxWidth().height(125.dp).padding(top = 50.dp, start = 10.dp, end = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(20.dp))
                        Text(
                            text = "MONO",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 50.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            Modifier
                                .shadow(elevation = 8.dp, shape = CircleShape)
                                .clickable(enabled = !isLoading.value) { // Disable button while loading
                                    isLoading.value = true
                                    vm.refreshData(vm.context, vm, isLoading = { isLoading.value = false })
                                    vm.updated_folder()
                                }
                                .padding(8.dp)
                        ) {
                            Row() {
                                Icon(
                                    painterResource(R.drawable.baseline_sort_24), "sort",
                                    Modifier.size(30.dp).clickable{showSettings.value = true},
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    painterResource(R.drawable.baseline_refresh_24), "refresh",
                                    Modifier.size(30.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp));
                    if (isLoading.value) {
                        Spacer(Modifier.height(200.dp));
                            Text(
                                modifier=Modifier.align(Alignment.CenterHorizontally).padding(start = 40.dp,top= 50.dp),
                                text = "Loading ...",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                            )
                    }
                    else{HomeScreen(vm)}
                }}
            }
            if (showSettings.value) {
                FullScreenSettings(vm,onDismiss = { showSettings.value = false }
                    ,onChange = {newValue->settingSort.value = newValue},settingSort.value)
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

@Composable
fun FullScreenSettings(
    vm: MyViewModel,
    onDismiss: () -> Unit,
    onChange:(Int)->Unit,sort:Int) {
    BackHandler() {
        onDismiss()
    }
    Box(Modifier
        .fillMaxSize()
        .background(Color.Black.copy(0.5f))
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            onDismiss()
        }) {
        Column (Modifier
            .align(Alignment.BottomCenter)
            .offset(y = (-50).dp)
            .height(400.dp)
            .width(400.dp)
            .padding(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF323634))
            .clickable( interactionSource = remember { MutableInteractionSource() },
                indication = null){}
        )
        {
            Text("Sort",fontSize = 25.sp,modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center,fontWeight = FontWeight.Bold)
            HorizontalDivider(Modifier.size(5.dp).padding(start=20.dp, end = 20.dp),color = MaterialTheme.colorScheme.secondary)
            for(i in enumValues<appsort>())
            {
                Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.padding(start=20.dp, top = 15.dp)) {
                    if(sort == i.id){
                        Icon(modifier=Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary,painter = painterResource(R.drawable.baseline_check_circle_outline_24), contentDescription = "check")
                    }
                    else{Spacer(Modifier.size(20.dp))}
                    Spacer(Modifier.size(20.dp))
                    Text(i.displayName,fontSize = 18.sp,modifier= Modifier.clickable {
                        onChange(i.id)
                        vm.updateSort(i.id)
                    })
                    Icon(modifier=Modifier.size(20.dp),tint = MaterialTheme.colorScheme.primary,painter = painterResource(if(i.value)R.drawable.baseline_arrow_downward_24 else R.drawable.baseline_arrow_upward_24), contentDescription = "up")
                }
            }
        }
    }
}