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
data class Setting(
    @Id var id: Long = 0,
    var Sort: Int = 0,
    var UiMode: Boolean = false,
)


@Entity
data class VideoModels(
    @Id var id: Long = 0,
    @Unique
    var VideoId: Long = 0,
    var Time: Float = 0f,
    var isFinished:Boolean = false,
    var isNew:Boolean = true,
)

