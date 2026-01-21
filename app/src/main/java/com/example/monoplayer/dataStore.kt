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
    var UiModeLight: Boolean = false,
    var lastOrient:Int = 0,
    var GridValue:Int = 1,
    var GroupByFolder:Boolean = true,
    var WavyBar:Boolean = true,
    var modernUI:Boolean = true

)




