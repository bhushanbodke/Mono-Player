package com.example.monoplayer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import io.objectbox.Box
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.collections.mapOf



enum class Engine{
    vlc,
    exo
}

enum class Screens{
    Home,
    Videos,
    VideoPlayer,
}
class MyViewModel(application: Application) : AndroidViewModel(application) {
    val userBox: Box<VideoModel> = MyApp.boxStore.boxFor(VideoModel::class.java)
    val lastPlayedBox: Box<lastPlayed> = MyApp.boxStore.boxFor(lastPlayed::class.java)

    val lastPlayedFolder = MutableStateFlow<List<lastPlayed>>(lastPlayedBox.all)

    fun UpdateLastPlayed(folder:String,id:Long){
        lastPlayedBox.put(lastPlayed(0,folder = folder, lastVideoId = id))
        lastPlayedBox.put(lastPlayed(0,folder = "Internal Storage/", lastVideoId = id))
        lastPlayedFolder.value = lastPlayedBox.all;
    }

    val screen = MutableStateFlow(Screens.Home) // initial screen
    fun setScreen(Newscreen: Screens) { screen.value = Newscreen}

    val engine = MutableStateFlow(Engine.exo)
    fun setEngine(NewEngine: Engine) {engine.value = NewEngine}
    val titlePath = MutableStateFlow("Internal Storage/")

    fun changeTitlePath(value:String){
        titlePath.value = value;
        Get_Files();
    }

    var AllFiles = MutableStateFlow<List<VideoModel>>(userBox.all.sortedBy { it.name.lowercase() });

    fun updateMap(VideoList:List<VideoModel>){
        Save_data_files(VideoList);
        AllFiles.value = userBox.all;
        Log.e("folders",AllFiles.toString())
    }

    val folderFiles = MutableStateFlow<List<VideoModel>>(listOf())
    fun Get_Files(){
        folderFiles.value = AllFiles.value.filter{ it.path.substringBeforeLast("/") == titlePath.value };
    }
    fun ChangeTime(id:Long,time:Float){
        AllFiles.value.find { it.id == id }?.Time = time;
        }


    var folderMap = MutableStateFlow<Map<String,List<String>>>(mapOf());

    fun updated_folder(){
        var folder_map = mutableMapOf<String, List<String>>();
        var folder_name: List<String> = AllFiles.value.groupBy { it.folder }.keys.toList();
        for (folder in folder_name) {
            var all_videos = AllFiles.value.filter { it.folder == folder };
            var count = all_videos.count();
            var size = all_videos.sumOf { it.size };
            folder_map[folder] = listOf(count.toString(), formatFileSize(size), all_videos[0].path.substringBeforeLast("/"));
        }
        folderMap.value = folder_map;
    }
    val currentVideo = MutableStateFlow<Long>(0);
    fun updateCurrentVideo(videoId:Long) {
        currentVideo.value = videoId;
    }
    fun Save_data_files(VideoList:List<VideoModel>){
        userBox.put(VideoList);
    }
}