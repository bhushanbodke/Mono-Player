package com.example.monoplayer

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.objectbox.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import kotlin.collections.mapOf



enum class display{
    none,
    playlist,
    control,
    lock,
    subtitles
}

enum class appsort(val id: Int, val displayName: String,val value: Boolean){
    nameAsc(0,"Name",false),
    nameDsc(1,"Name ",true),
    sizeAsc(2,"Size ",false),
    sizeDsc(3,"Size ",true),
    dateAsc(4,"Date ",false),
    dateDsc(5,"Date ",true)
}

enum class Screens{
    Home,
    Videos,
    VideoPlayer,
    permissions
}
class MyViewModel(application: Application) : AndroidViewModel(application) {
    val userBox: Box<VideoModels> = MyApp.boxStore.boxFor(VideoModels::class.java)
    val lastPlayedBox: Box<lastPlayed> = MyApp.boxStore.boxFor(lastPlayed::class.java)
    val settingBox: Box<Setting> = MyApp.boxStore.boxFor(Setting::class.java)

    val lastPlayedFolder = MutableStateFlow<List<lastPlayed>>(lastPlayedBox.all)
    val titlePath = MutableStateFlow("Internal Storage/")
    var AllFiles = MutableStateFlow<List<VideoModel>>(listOf());
    var SavedFiles = MutableStateFlow<MutableList<VideoModels>>(userBox.all);
    var currentVideo = MutableStateFlow<VideoModel?>(null);
    var folderMap = MutableStateFlow<Map<String, List<String>>>(mapOf());
    val subScale = MutableStateFlow(1.0f)

    val settings  = MutableStateFlow(settingBox.all)
    lateinit var libVLC:LibVLC;


    lateinit var context:Context

    fun updateSort(sort: Int){
        val currentSetting = settingBox.all.firstOrNull() ?: Setting()
        currentSetting.Sort = sort
        settingBox.put(currentSetting)
        settings.value = listOf(currentSetting)
    }

    fun UpdateLastPlayed(folder: String, id: Long) {
        lastPlayedBox.put(lastPlayed(0, folder = folder, lastVideoId = id))
        lastPlayedBox.put(lastPlayed(0, folder = "Internal Storage/", lastVideoId = id))
        lastPlayedFolder.value = lastPlayedBox.all;
    }
    fun updateCurrentVideo(video: VideoModel){
        currentVideo.value = video
    }
    val screen = MutableStateFlow(Screens.Home) // initial screen
    fun setScreen(Newscreen: Screens) {
        screen.value = Newscreen
    }


    fun changeTitlePath(value: String) {
        titlePath.value = value;
        Get_Files();
    }

    val folderFiles = MutableStateFlow<List<VideoModel>>(listOf())
    fun Get_Files() {
        viewModelScope.launch {
            folderFiles.value =
                AllFiles.value.filter { it.path.substringBeforeLast("/") == titlePath.value };}
    }

    init {
        viewModelScope.launch {
            settings.collect { newSort ->
                if(newSort.isEmpty())return@collect
                folderFiles.value = when (newSort[0].Sort) {
                    0 -> { folderFiles.value.sortedBy { it.name }}
                    1 -> {folderFiles.value.sortedByDescending { it.name }}
                    2 -> { folderFiles.value.sortedBy { it.size }}
                    3 -> { folderFiles.value.sortedByDescending { it.size }}
                    4 -> {folderFiles.value.sortedBy { it.DateAdded }}
                    5 -> { folderFiles.value.sortedByDescending { it.DateAdded }}
                    else->{folderFiles.value}
                }
            }
        }
    }

    fun ChangeTime(id: Long, time: Float,finished:Boolean= false) {
        val file = AllFiles.value.find { it.VideoId == id }
        file?.Time = time
        file?.isNew = false
        file?.isFinished = finished
        val entity = userBox.query().equal(VideoModels_.VideoId, id).build().findFirst()
        if (entity != null) {
            entity.Time = time
            entity.isNew = false
            entity.isFinished = finished
            userBox.put(entity)
        } else {
            userBox.put(VideoModels(VideoId = id, Time = time, isNew = false ,isFinished = finished))
        }
    }

    fun updated_folder() {
        viewModelScope.launch (Dispatchers.IO) {
            var folder_map = mutableMapOf<String, List<String>>();
            var folder_name: List<String> =
                AllFiles.value.groupBy { it.path.substringBeforeLast("/") }.keys.toList();
            for (folder in folder_name) {
                var all_videos =
                    AllFiles.value.filter { it.path.substringBeforeLast("/") == folder };
                var count = all_videos.count();
                var size = all_videos.sumOf { it.size };
                folder_map[folder] = listOf(
                    count.toString(),
                    formatFileSize(size),
                    all_videos[0].path.substringBeforeLast("/")
                );
            }
            folderMap.value = folder_map;
        }
    }



    fun Save_data_files() {
        userBox.put(SavedFiles.value);
    }


    fun refreshData(context: Context, vm:MyViewModel, isLoading:()->Unit) {
        viewModelScope.launch (Dispatchers.IO) {
            var videos = ListVideos(context, vm);
            val dbVideos = userBox.all
            if (SavedFiles.value.isEmpty()) {
                AllFiles.value = videos;
            } else {
                for (video in videos) {
                    val Savedvideo= dbVideos.find { it.VideoId == video.VideoId };
                    if (Savedvideo != null) {
                        video.Time = Savedvideo.Time;
                        video.isFinished = Savedvideo.isFinished;
                        video.isNew = Savedvideo.isNew&&video.isNew;
                    }
                    else{
                        userBox.put(VideoModels(VideoId = video.VideoId, Time = 0f, isFinished = false, isNew = true));
                    }
                }
                AllFiles.value = videos
                SavedFiles.value = userBox.all
            }
            Save_data_files()
            withContext(Dispatchers.Main) {
                isLoading()
            }
        }
    }

    fun removeFile(videoId: Long){
        viewModelScope.launch (Dispatchers.IO) {
            folderFiles.value = folderFiles.value.filter { it.VideoId != videoId }
            AllFiles.value = AllFiles.value.filter { it.VideoId != videoId }
        }
    }

    fun getFourVideos(folderName:String): MutableList<VideoModel>{
        return AllFiles.value.filter { it.path.substringBeforeLast("/") == folderName }.take(4).toMutableList()
    }
}