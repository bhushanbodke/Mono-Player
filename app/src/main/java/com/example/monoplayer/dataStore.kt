package com.example.monoplayer

import io.objectbox.annotation.ConflictStrategy
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import java.io.File



@Entity
data class lastPlayed(
    @Id var id: Long = 0,
    @Unique(onConflict = ConflictStrategy.REPLACE)
    var folder: String="",
    var lastVideoId:Long = 0
)

@Entity
data class VideoModel(
    @Id var id: Long = 0,
    @Unique(onConflict = ConflictStrategy.REPLACE)
    var VideoId: Long = 0,
    var name: String = "",
    var duration: Int = 0,
    var size: Long = 0,
    var uri: String = "",
    var path: String = "",
    var folder: String = "",
    var Time: Float = 0f
) {
    // Optional: automatically set folder if not provided
    init {
        if (folder.isEmpty() && path.isNotEmpty()) {
            folder = File(path).parentFile?.name ?: "Internal Storage"
        }
    }
}
