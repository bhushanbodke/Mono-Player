package com.example.monoplayer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        vm.Save_data_files(vm.AllFiles.value);
        Log.e("stop","stopped")
    }
}

@Composable
fun main_screen(vm:MyViewModel) {
    val screen = vm.screen.collectAsState();
    if (screen.value != Screens.VideoPlayer) {
        Surface(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
            ) {
                Row(Modifier.fillMaxWidth().height(125.dp).padding(top = 50.dp,start = 20.dp,end = 20.dp)
                    , horizontalArrangement = Arrangement.Center
                    , verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(55.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.primary)
                        ,contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable._767328470870_removebg_preview),
                            contentDescription = "icon",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Spacer(Modifier.width(20.dp));
                    Text(
                        text = "MONO",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 50.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(10.dp));
                HomeScreen(vm)
            }
        }
    }
    else{
        VlcEngine(vm);
    }
}
