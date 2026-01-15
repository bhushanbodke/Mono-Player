package com.example.monoplayer

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.io.File
import java.io.FileOutputStream




@Entity
data class VideoModel(
    @Id var id: Long = 0,
    @Unique
    var VideoId: Long = 0,
    var name: String = "",
    var duration: Int = 0,
    var size: Long = 0,
    var uri: String = "",
    var path: String = "",
    var folder: String = "",
    var Time: Float = 0f,
    var DateAdded:Float = 0f,
    var isFinished:Boolean = false,
    var isNew:Boolean = false,
    var Width:Int = 0,
    var Height:Int = 0
)

fun ListVideos(context: Context):List<VideoModel>{
    var videoList = mutableListOf<VideoModel>()
    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT,
    )
    val query = context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        null, // No filter (get everything)
        null,
        "${MediaStore.Video.Media.DATE_ADDED} DESC"
    )
    query?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
        val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
        val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)



        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            // Combine the base URI with the ID to get the specific file's URI
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
            )

            videoList.add(
                VideoModel(
                    id =  0,
                    VideoId = id,
                    name = cursor.getString(nameCol),
                    duration = cursor.getInt(durationCol),
                    size = cursor.getLong(sizeCol),
                    uri = contentUri.toString(),
                    path = cursor.getString(dataCol),
                    Time = 0f,
                    DateAdded = cursor.getFloat(dateCol),
                    isFinished = false,
                    isNew = IsNew(cursor.getLong(dateCol)),
                    Width = cursor.getInt(widthCol),
                    Height = cursor.getInt(heightCol)
                )
            )
        }
    }
    return videoList;
}


fun IsNew(time:Long):Boolean{
    val currentTimeMillis = System.currentTimeMillis();
    val timeDifference = currentTimeMillis - time*1000L;
    val limit = 48 * 60 * 60 * 1000L
    val isNew = (timeDifference) < limit
    return isNew
}

fun formatFileSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()

    val size = sizeInBytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format("%.2f %s", size, units[digitGroups])
}

fun formatDuration(durationMs: Int): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02dhr %02dmin  %02ds", hours, minutes, seconds)
    } else {
        String.format("%02dmin %02ds", minutes, seconds)
    }
}

@Composable
fun CustomScrollIndicatorLazyColumn(
    listState: LazyListState
)
{
    var trackHeightPx by remember { mutableIntStateOf(0) }

    // 1. Detect if scrolling is happening
    val isScrolling = listState.isScrollInProgress

    // 2. Smoothly animate the alpha (0f is invisible, 1f is visible)
    val alpha by animateFloatAsState(
        targetValue = if (isScrolling) 1f else 0f,
        animationSpec = tween (durationMillis = 500), // Fades out over half a second
        label = "PillAlpha"
    )

    val scrollProgress by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty() || layoutInfo.totalItemsCount <= visibleItems.size) {
                return@derivedStateOf 0f
            }
            val firstItem = visibleItems.first()
            val totalItems = layoutInfo.totalItemsCount
            val percentageOfFirstItem = listState.firstVisibleItemScrollOffset.toFloat() / firstItem.size
            val preciseIndex = listState.firstVisibleItemIndex.toFloat() + percentageOfFirstItem
            (preciseIndex / (totalItems - layoutInfo.visibleItemsInfo.size)).coerceIn(0f, 1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Custom scrollbar Track
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .align(Alignment.CenterEnd)
                .padding(vertical = 12.dp)
                .graphicsLayer { this.alpha = alpha } // 3. Apply the animated alpha here
                .onGloballyPositioned { trackHeightPx = it.size.height }
        ) {
            val thumbHeight = 40.dp
            val thumbHeightPx = with(LocalDensity.current) { thumbHeight.toPx() }
            val offsetY = ((trackHeightPx - thumbHeightPx) * scrollProgress).toInt()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thumbHeight)
                    .offset { IntOffset(x = 0, y = offsetY) }
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

fun deleteMediaByUri(
    context: Context,
    uri: Uri,
    launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
) {
    val contentResolver = context.contentResolver

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ : Request permission to delete
            val pendingIntent = MediaStore.createDeleteRequest(contentResolver, listOf(uri))
            val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            launcher.launch(request)
        } else {
            // Android 10 and below : Try direct delete
            try {
                contentResolver.delete(uri, null, null)
            } catch (e: SecurityException) {
                // Handle Android 10 specifically if it throws a security error
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                    val request = IntentSenderRequest.Builder(e.userAction.actionIntent.intentSender).build()
                    launcher.launch(request)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}




fun toggleOrientation(activity: MainActivity, isCurrentlyLocked: Boolean,vm: MyViewModel) {
    if (isCurrentlyLocked) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    } else {
        // LOCK: Determine current state and freeze it
        val rotation = activity.windowManager.defaultDisplay.rotation

        when (rotation) {
            Surface.ROTATION_90 -> {
                activity.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; vm.saveLastOrientation(0); }

            Surface.ROTATION_270 -> {
                activity.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE; vm.saveLastOrientation(1); }

            Surface.ROTATION_180 -> {
                activity.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT; vm.saveLastOrientation(2); }

            else -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                vm.saveLastOrientation(2)
            }
        }
    }
}
fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
fun enterVideoMode(vm: MyViewModel,activity: MainActivity) {
    activity.requestedOrientation  = when(vm.lastOrientation.value){
        0->ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        1->ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        2->ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else->ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }
}

fun exitVideoMode(activity: MainActivity) {
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}
