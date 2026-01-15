package com.example.monoplayer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(vm:MyViewModel) {
    BackHandler() {
        vm.setScreen(Screens.Home)
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 50.dp, start = 10.dp, end = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically)
            {
                IconButton(onClick = { vm.setScreen(Screens.Home) })
                {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,contentDescription = "back",
                        Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.size(20.dp))
                Text(
                    text = "Settings",
                    Modifier.fillMaxWidth(),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.size(20.dp))
            ApperanceSettings(vm);
            Spacer(Modifier.size(20.dp))
            VideoPlayerSettings(vm);
        }
    }
}


@Composable
fun ApperanceSettings(vm:MyViewModel){
    val isLightMode by vm.isLightMode.collectAsState()
    Text(
        text = "Apperance settings",
        Modifier.fillMaxWidth(),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.size(20.dp))
    Row(Modifier.fillMaxWidth().height(80.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.background)
        .padding(10.dp)
        , verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(R.drawable.dark_mode_24), contentDescription = "dark"
            ,Modifier.size(30.dp),tint = MaterialTheme.colorScheme.primary)
        Text(text = "Light Mode",Modifier.weight(1f).padding(start = 10.dp),fontSize = 15.sp,color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = isLightMode,
            onCheckedChange = { vm.toggleLightMode() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.tertiary,
            )
        )
    }
}
@Composable
fun VideoPlayerSettings(vm:MyViewModel){
    val wavyBar by vm.WavyBar.collectAsState()
    Text(
        text = "Video Player ",
        Modifier.fillMaxWidth(),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary

    )
    Spacer(Modifier.size(20.dp))
    Text(
        text = "Progress Bar Type",
        Modifier.fillMaxWidth().padding(start = 10.dp),
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurface
    )
    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).height(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    2.dp, MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(10.dp))
                .background(if(wavyBar) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.background)
                .clickable {
                    vm.toggleWavy()
                },)
        {
            Icon(
                modifier = Modifier.size(70.dp),
                painter = painterResource(id = R.drawable.airwave),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "waves"
            )
            Text(text = "Wavy",fontSize = 14.sp,color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.size(20.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).height(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    2.dp, MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(10.dp))
                .background(if(!wavyBar) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.background)
                .clickable{
                    vm.toggleWavy()
                })
        {
            Icon(
                modifier = Modifier.size(70.dp),
                painter = painterResource(id = R.drawable.slider),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "waves"
            )
            Text(text = "straight",fontSize = 14.sp,color = MaterialTheme.colorScheme.onSurface)
        }
    }
}