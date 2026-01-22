package com.example.monoplayer


import android.app.Application
import android.content.Context
import android.media.MediaScannerConnection
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.objectbox.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException


enum class display{
    none,
    playlist,
    control,
    lock,
    subtitles,
    audioSelector,
    settings
}

data class FolderModel(
    val name: String,
    val path: String,
    val videoCount: Int,
    val totalSize: String,
    val previewThumbnails: List<VideoModel> // Store paths of first 4 videos here
)

data class subSetting(
    val size:Int = 20,
    val isTransperant: Boolean =  false,
    val isBold: Boolean = false,
    val isShadow: Boolean = true,
    )
enum class appsort(val id: Int, val displayName: String){
    nameAsc(0,"File name (A to Z)"),
    nameDsc(1,"File name (Z to A)"),
    sizeAsc(2,"Size (smallest first)"),
    sizeDsc(3,"Size (largest first)"),
    dateAsc(4,"Date (oldest first)"),
    dateDsc(5,"Date (newest first)"),
    new(6,"New (newest first)")
}

enum class Screens{
    Home,
    Videos,
    VideoPlayer,
    permissions,
    settings

}
class MyViewModel(application: Application) : AndroidViewModel(application) {
    val userBox: Box<VideoModel> = MyApp.boxStore.boxFor(VideoModel::class.java)
    val lastPlayedBox: Box<lastPlayed> = MyApp.boxStore.boxFor(lastPlayed::class.java)
    val settingBox: Box<Setting> = MyApp.boxStore.boxFor(Setting::class.java)

    val excludedFoldersBox:Box<ExcludedFolder> = MyApp.boxStore.boxFor(ExcludedFolder::class.java)
    val hiddenFolderBox:Box<HiddenFolder> = MyApp.boxStore.boxFor(HiddenFolder::class.java)

    val lastPlayedFolder = MutableStateFlow<List<lastPlayed>>(lastPlayedBox.all)
    val titlePath = MutableStateFlow("Internal Storage/")
    var AllFiles = MutableStateFlow<List<VideoModel>>(userBox.all);
    var currentVideo = MutableStateFlow<VideoModel?>(null);
    val foldersList = MutableStateFlow<List<FolderModel>>(listOf())
    val isPip = MutableStateFlow(false)
    val isRefreshing = MutableStateFlow(false)
    val IsOrientLocked = MutableStateFlow(false);
    val currentBrightness = MutableStateFlow(-1f);





    val settings  = MutableStateFlow(settingBox.all.firstOrNull()?:Setting())
    val sort = MutableStateFlow(settings.value.Sort)
    val GridValue = MutableStateFlow(settings.value.GridValue)
    val isLightMode = MutableStateFlow(settings.value.UiModeLight);
    val lastOrientation = MutableStateFlow(settings.value.lastOrient);
    val WavyBar = MutableStateFlow(settings.value.WavyBar)
    val modernUI = MutableStateFlow(settings.value.modernUI)

    val subtitleSettings  = MutableStateFlow(subSetting());

    val hiddenFolders = MutableStateFlow(hiddenFolderBox.all)
    val excludedFolders = MutableStateFlow(excludedFoldersBox.all)


    fun hideFolder(context:Context,folderPath: String) {
        val folder = File(folderPath)
        if (folder.exists() && folder.isDirectory) {
            val nomedia = File(folder, ".nomedia")
            try {
                if (!nomedia.exists()) {
                    nomedia.createNewFile() // This "hides" the contents
                }
                hiddenFolderBox.put(HiddenFolder(0,folderPath))
                MediaScannerConnection.scanFile(context, arrayOf(folderPath), null, null)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        hiddenFolders.value = hiddenFolderBox.all
        refreshDataWithLoading()
    }

    fun unhideFolder(context:Context,folder: HiddenFolder) {
        val nomedia = File(folder.fullPath, ".nomedia")
        if (nomedia.exists()) {
            nomedia.delete() // This "unhides" the contents
            MediaScannerConnection.scanFile(context, arrayOf(folder.fullPath), null, null)
        }
        hiddenFolderBox.remove(folder.id)
        hiddenFolders.value = hiddenFolderBox.all
        refreshData()

    }

    fun addExcluded(path:String){
        excludedFoldersBox.put(ExcludedFolder(0,path))
        excludedFolders.value = excludedFoldersBox.all
        refreshDataWithLoading()

    }
    fun removeFromExcluded(id: Long){
        excludedFoldersBox.remove(id)
        excludedFolders.value = excludedFoldersBox.all
        refreshData()
    }

    fun setSubSettings(
        size: Int = subtitleSettings.value.size,
        isTransperant: Boolean = subtitleSettings.value.isTransperant,
        isBold: Boolean = subtitleSettings.value.isBold,
        isShadow: Boolean = subtitleSettings.value.isShadow
    ){
        subtitleSettings.update { currentSettings ->
            currentSettings.copy(
                size = size,
                isTransperant = isTransperant,
                isBold = isBold,
                isShadow = isShadow
            )
        }
    }

    fun updateSavedBrightness(value: Float) {
        currentBrightness.value = value
    }

    fun saveAllSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentSettings = settings.value
                if (currentSettings.id == 0L) {
                    val existing = settingBox.all.firstOrNull()
                    if (existing != null) currentSettings.id = existing.id
                }
                settingBox.put(currentSettings)
                Log.d("STORAGE", "Settings saved successfully on exit")
            } catch (e: Exception) {
                Log.e("STORAGE", "Failed to save settings: ${e.message}")
            }
        }
    }
    private fun updateSettings(update: (Setting) -> Setting) {
        settings.value = update(settings.value)
    }
    fun toggleModernUI(){
        modernUI.value = !modernUI.value
        updateSettings { it.copy(modernUI = !it.modernUI) }
    }
    fun toggleLightMode(){
        isLightMode.value = !isLightMode.value;
        updateSettings { it.copy(UiModeLight = !it.UiModeLight) }
    }
    fun saveLastOrientation(value: Int) {
        lastOrientation.value = value;
        updateSettings { it.copy(lastOrient = value) }
    }
    fun toggleGrid() {
        val newGrid = when(settings.value.GridValue) {
            1 -> 2
            2 -> 3
            else -> 1
        }
        GridValue.value =newGrid;
        updateSettings { it.copy(GridValue = newGrid) }
    }
    fun toggleWavy() {
        WavyBar.value = !WavyBar.value
        updateSettings { it.copy(WavyBar = !it.WavyBar) }
    }
    fun updateSort(newSort: Int) {
        sort.value = newSort
        updateSettings { it.copy(Sort = newSort) }
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
        sortList(sort.value);
    }

    init {
        viewModelScope.launch {
            sort.collect { sort ->
                sortList(sort)
            }
        }
    }

    fun sortList(sort: Int?) {
        viewModelScope.launch {
        if (sort == null) return@launch
        Log.e("sorted","sorted")
        folderFiles.value = when (sort) {
            0 -> {folderFiles.value.sortedBy { it.name }}
            1 -> {folderFiles.value.sortedByDescending { it.name }}
            2 -> {folderFiles.value.sortedBy { it.size }}
            3 -> {folderFiles.value.sortedByDescending { it.size }}
            4 -> {folderFiles.value.sortedBy { it.DateAdded }}
            5 -> {folderFiles.value.sortedByDescending { it.DateAdded }}
            6 -> {folderFiles.value.sortedByDescending { it.isNew }}
            else -> {folderFiles.value}
            }
        }
    }

    fun ChangeTime(id: Long, time: Float,finished:Boolean= false) {
        val file = AllFiles.value.find { it.VideoId == id }
        file?.Time = time
        file?.isNew = false
        file?.isFinished = finished
        val entity = userBox.query().equal(VideoModel_.VideoId, id).build().findFirst()
        if (entity != null) {
            entity.Time = time
            entity.isNew = false
            entity.isFinished = finished
            userBox.put(entity)
        } else {

        }
    }

    fun updated_folder() {
        viewModelScope.launch(Dispatchers.IO) {
            val excludedPaths = excludedFoldersBox.all.map { it.fullPath }
            val hiddenPaths = hiddenFolderBox.all.map { it.fullPath }
            val allBlacklisted = excludedPaths + hiddenPaths
            val allVideos = AllFiles.value
            val grouped = allVideos.groupBy { it.path.substringBeforeLast("/") }
                .filter { (folderPath, _) ->
                    allBlacklisted.none { folderPath.startsWith(it) }}

            val newList = grouped.map { (path, videos) ->
                FolderModel(
                    name = path.substringAfterLast("/"),
                    path = path,
                    videoCount = videos.size,
                    totalSize = formatFileSize(videos.sumOf { it.size }),
                    previewThumbnails = videos.take(4) // Get thumbnails here!
                )
            }.sortedBy { it.name }

            foldersList.value = newList
        }
    }



    fun Save_data_files() {
        userBox.put(AllFiles.value);
    }


    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            val freshVideos = ListVideos(getApplication())
            val savedData = userBox.all
            val savedMap = savedData.associateBy { it.VideoId }
            freshVideos.forEach { video ->
                val saved = savedMap[video.VideoId]
                if (saved != null) {
                    // Existing file: Restore progress
                    video.Time = saved.Time
                    video.isFinished = saved.isFinished
                    video.isNew = saved.isNew && video.isNew
                    video.id = saved.id
                } else {
                    video.Time = 0f
                    video.isNew = true
                    video.isFinished = false
                }
            }

            withContext(Dispatchers.Main) {
                AllFiles.value = freshVideos
                updated_folder()
            }
            userBox.put(freshVideos)
        }
    }

    fun refreshDataWithLoading() {
        viewModelScope.launch {
            isRefreshing.value = true
            refreshData()
            delay(500)
            isRefreshing.value = false
        }
    }

    fun removeFile(videoId: Long){
        viewModelScope.launch (Dispatchers.IO) {
            folderFiles.value = folderFiles.value.filter { it.VideoId != videoId }
            AllFiles.value = AllFiles.value.filter { it.VideoId != videoId }
        }
    }

    val downloadedSubs = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    fun searchSubtitles(name:String) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadedSubs.value = SubtitleRepo.searchSubtitles("Interstellar")
        }
    }
    fun downloadSubtitles(Id:Int,context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            SubtitleRepo.downloadFile(Id, context.cacheDir)
        }
    }
}