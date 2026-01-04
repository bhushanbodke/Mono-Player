package com.example.monoplayer

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log


fun ListVideos(context: Context,vm:MyViewModel){
    var videoList = mutableListOf<VideoModel>()
    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DATA
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

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            // Combine the base URI with the ID to get the specific file's URI
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
            )

            videoList.add(
                VideoModel(
                    id = 0 ,
                    VideoId = id,
                    name = cursor.getString(nameCol),
                    duration = cursor.getInt(durationCol),
                    size = cursor.getLong(sizeCol),
                    uri = contentUri.toString(),
                    path = cursor.getString(dataCol),
                    Time = 0f
                )
            )
        }
    }
    vm.updateMap(videoList);

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