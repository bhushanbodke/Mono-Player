package com.example.monoplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun innerSettings(vm: MyViewModel,hideWindow: () -> Unit){
    Box(Modifier.fillMaxSize().clickable( interactionSource = remember { MutableInteractionSource() },
        indication = null){hideWindow()}) {
        Box(Modifier.fillMaxHeight()
            .align(Alignment.CenterEnd)
            .offset(x = 20.dp)
            .width(400.dp)
            .background(MaterialTheme.colorScheme.surface.copy(0.8f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {})
        {
            VerticalDivider(Modifier.width(4.dp).align(Alignment.CenterStart), color = MaterialTheme.colorScheme.primary.copy(0.5f))
            Column(Modifier.fillMaxSize().padding(10.dp).align(Alignment.Center)) {
                Row(verticalAlignment = Alignment.CenterVertically){
                    IconButton(
                        onClick = {hideWindow()},
                        modifier = Modifier.background(Color.Black.copy(0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, "back", tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(text = "Settings" , color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                wavyBarToggle(vm)
                ModernUIToggle(vm)
            }
        }
    }
}

@Composable
fun ModernUIToggle(vm: MyViewModel){
    val setting by vm.settings.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = setting.modernUI,
                onValueChange = { vm.toggleModernUI() },
                role = Role.Switch
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Minimal UI",
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            modifier = Modifier.scale(0.8f),
            checked = setting.modernUI,
            onCheckedChange = null
        )
    }
}
@Composable
fun wavyBarToggle(vm: MyViewModel){
    val setting by vm.settings.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = setting.WavyBar,
                onValueChange = { vm.toggleWavy() },
                role = Role.Switch
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Wavy Bar",
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            modifier = Modifier.scale(0.8f),
            checked = setting.WavyBar,
            onCheckedChange = null
        )
    }
}


