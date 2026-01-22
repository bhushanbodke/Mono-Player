package com.example.monoplayer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(vm:MyViewModel) {
    BackHandler() {
        vm.setScreen(Screens.Home)
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 50.dp, start = 10.dp, end = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
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
            Spacer(Modifier.size(20.dp))
            LibraryPrivacy(vm)
        }
    }
}

@Composable
fun LibraryPrivacy(vm: MyViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(R.drawable.video_library_24),
            null,tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = "Library & Privacy",
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
    excludedFolders(vm)
    hiddenFolder(vm);
}

@Composable
fun hiddenFolder(vm: MyViewModel){
    val hiddenFolders by vm.hiddenFolders.collectAsState()
    var expanded by remember{ mutableStateOf(false)}
    val context = LocalContext.current

    Spacer(Modifier.size(20.dp))
    Row(Modifier.fillMaxWidth().padding(start = 10.dp),verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.folder_eye),null
            , tint = MaterialTheme.colorScheme.onSurface
            ,modifier = Modifier.size(30.dp))
        Column( modifier = Modifier.padding(start = 10.dp)) {
            Text(
                text = "Hidden Folders",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Videos in these folders are hidden from your system.",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(170.dp),
                color = Color.Gray,
                lineHeight = 10.sp,
                fontSize = 10.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically,modifier =  Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable{
                expanded = !expanded
            }
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp))
        {
            Text(
                text = "${hiddenFolders.size} Folders",
                fontSize = 14.sp,
                modifier =  Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(imageVector = if(expanded)Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,tint = MaterialTheme.colorScheme.onSurface,contentDescription = null)
        }
    }
    if(expanded){
        hiddenFolders.forEach { folder ->
            HiddenFolderItem(
                folderName = folder.fullPath.substringAfterLast("/"),
                fullPath = folder.fullPath,
                onRemove = { vm.unhideFolder(context,folder) }
            )

        }
    }
}
@Composable
fun excludedFolders(vm: MyViewModel){
    val excludedFolders by vm.excludedFolders.collectAsState()
    var expanded by remember{ mutableStateOf(false)}

    Spacer(Modifier.size(20.dp))
    Row(Modifier.fillMaxWidth().padding(start = 10.dp),verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.folder_off_24),null
            , tint = MaterialTheme.colorScheme.onSurface
            ,modifier = Modifier.size(30.dp))
        Column( modifier = Modifier.padding(start = 10.dp)) {
            Text(
                text = "Excluded Folders",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Videos in these folders are hidden from your library.",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(170.dp),
                color = Color.Gray,
                lineHeight = 10.sp,
                fontSize = 10.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically,modifier =  Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable{
                expanded = !expanded
            }
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp))
        {
            Text(
                text = "${excludedFolders.size} Folders",
                fontSize = 14.sp,
                modifier =  Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(imageVector = if(expanded)Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,tint = MaterialTheme.colorScheme.onSurface,contentDescription = null)
        }
    }
    if(expanded){
        excludedFolders.forEach { folder ->
            HiddenFolderItem(
                folderName = folder.fullPath.substringAfterLast("/"),
                fullPath = folder.fullPath,
                onRemove = { vm.removeFromExcluded(folder.id) }
            )

        }
    }
}
@Composable
fun HiddenFolderItem(folderName: String, fullPath: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 70.dp,end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(R.drawable.twotone_folder_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(25.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folderName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 13.sp
            )
            Text(
                text = fullPath,
                color = Color.Gray,
                fontSize = 10.sp,
                lineHeight = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                painterResource(R.drawable.twotone_delete_24),
                contentDescription = "Remove",
                tint = Color.Red.copy(alpha = 0.8f)
            )
        }
    }
}


@Composable
fun ApperanceSettings(vm:MyViewModel){
    val isLightMode by vm.isLightMode.collectAsState()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(R.drawable.color_lens_24),
            null,tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = "Apperance settings",
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(Modifier.size(20.dp))
    Row(Modifier.fillMaxWidth().padding(start = 10.dp)
        ,verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(R.drawable.dark_mode_24), contentDescription = "dark"
            ,Modifier.size(30.dp),tint = MaterialTheme.colorScheme.onSurface)
        Text(text = "Light Mode",Modifier.weight(1f).padding(start = 10.dp)
            ,fontSize = 15.sp
            ,fontWeight = FontWeight.SemiBold
            ,color = MaterialTheme.colorScheme.onSurface)
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(R.drawable.video_settings_24),
            null,tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = "Video Player ",
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary

        )
    }
    Spacer(Modifier.size(20.dp))
    Row(Modifier.fillMaxWidth().padding(start = 10.dp),verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(R.drawable.linear_scale_24), contentDescription = "dark"
            ,Modifier.size(30.dp),tint = MaterialTheme.colorScheme.onSurface)
        Text(
            text = "Progress Bar Type",
            Modifier.fillMaxWidth().padding(start = 10.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).height(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    2.dp, MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(10.dp))
                .background(if(wavyBar) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.background)
                .clickable {
                    vm.toggleWavy()
                },)
        {
            Icon(
                modifier = Modifier.size(70.dp),
                painter = painterResource(id = R.drawable.airwave),
                tint = if(wavyBar) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary,
                contentDescription = "waves"
            )
            Text(text = "Wavy",fontSize = 14.sp,color = if(wavyBar) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.size(20.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).height(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    2.dp, MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(10.dp))
                .background(if(!wavyBar) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.background)
                .clickable{
                    vm.toggleWavy()
                })
        {
            Icon(
                modifier = Modifier.size(70.dp),
                painter = painterResource(id = R.drawable.slider),
                tint = if(!wavyBar) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary,
                contentDescription = "waves"
            )
            Text(text = "straight",fontSize = 14.sp,color =if(!wavyBar) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onBackground)
        }
    }
}