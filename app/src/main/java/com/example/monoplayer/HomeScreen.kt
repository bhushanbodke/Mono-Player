package com.example.monoplayer

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: MyViewModel) {
    val screen by vm.screen.collectAsState()
    val currentPath by vm.titlePath.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    val gridvalue by vm.GridValue.collectAsState()
    val scrollState = rememberScrollState()
    val isRefreshing by vm.isRefreshing.collectAsState()

    // Determine the path display
    val title = if (screen == Screens.Home) "Internal Storage" else currentPath

    // Auto-scroll breadcrumbs to the end when path changes
    LaunchedEffect(title) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(40.dp))
            // --- BREADCRUMBS ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                val pathParts = title.split("/").filter { it.isNotEmpty() }
                pathParts.forEachIndexed { index, word ->
                    Text(
                        text = if (word.length > 20) word.take(15) + "..." else word,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (index < pathParts.size - 1) {
                        Icon(
                            painterResource(R.drawable.baseline_arrow_forward_ios_24),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(10.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // --- ACTION BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (screen == Screens.Home) "Library" else vm.titlePath.value.substringAfterLast("/"),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                IconButton(onClick = { vm.toggleGrid() }) {
                    Icon(
                        painter = painterResource(
                            when (gridvalue) {
                                1 -> R.drawable.view_list_24
                                2 -> R.drawable.grid_view_24
                                else -> R.drawable.grid_on_24
                            }
                        ),
                        contentDescription = "Toggle Grid",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = { showSettings = true }) {
                    Icon(
                        painter = painterResource(R.drawable.sort_24),
                        contentDescription = "Sort Options",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // --- CONTENT AREA ---
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { vm.refreshDataWithLoading() },
                modifier = Modifier.weight(1f)
            ) {
                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally(animationSpec = tween(400)) { it } + fadeIn()).togetherWith(
                                slideOutHorizontally(animationSpec = tween(400)) { -it } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally(animationSpec = tween(400)) { -it } + fadeIn()).togetherWith(
                                slideOutHorizontally(animationSpec = tween(400)) { it } + fadeOut()
                            )
                        }
                    },
                    label = "ScreenTransition"
                ) { targetScreen ->
                    // Wrap in Box with key to force a new transition
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (targetScreen) {
                            Screens.Home -> videos(vm)
                            Screens.Videos -> FolderScreen(vm)
                            else -> videos(vm)
                        }
                    }
                }
            }
        }

        // --- RESUME FAB ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-16).dp)
                .padding(15.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable() {
                val searchPath = if (screen == Screens.Home) "Internal Storage/" else title
                val folderData = vm.lastPlayedFolder.value.find { it.folder == searchPath }
                folderData?.lastVideoId?.let { id ->
                    vm.AllFiles.value.find { it.VideoId == id }?.let { video ->
                        vm.updateCurrentVideo(video)
                        vm.setScreen(Screens.VideoPlayer)
                    }
                }
            },
        ){
            Icon(imageVector = Icons.Default.PlayArrow
                ,tint = MaterialTheme.colorScheme.surface, contentDescription =  null
                ,modifier= Modifier.size(60.dp))
        }

        // --- SORT BOTTOM SHEET OVERLAY ---
        AnimatedVisibility(
            visible = showSettings,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            SortSheetOverlay(vm) { showSettings = false }
        }
    }
}

@Composable
fun SortSheetOverlay(vm: MyViewModel, onHide: () -> Unit) {
    val currentSort by vm.sort.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onHide() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Box(
                    Modifier
                        .size(40.dp, 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Sort Videos By",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                appsort.values().forEach { option ->
                    val isSelected = currentSort == option.id

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else Color.Transparent
                            )
                            .clickable {
                                vm.updateSort(option.id)
                                onHide()
                            }
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = option.displayName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "Selected"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}