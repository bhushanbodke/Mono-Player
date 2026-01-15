package com.example.monoplayer
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

data class SubDlResponse(
    val status: Boolean,
    val results: List<SubDlEntry>,
    val subtitles: List<SubtitleFile>
)

data class SubDlEntry(
    val sd_id: Int,
    val type: String,
    val name: String,
    val imdb_id: String?,
    val tmdb_id: Int?,
    val year: Int
)
data class SubtitleFile(
    val name:String,
    val lang: String,
    val release: String,
    val url: String,
    val author: String?,
    @SerializedName("hi")
    val isHearingImpaired: Boolean
)


interface SubDlApiService {
    @GET("subtitles") // This is the endpoint path
    suspend fun getSubtitles(
        @Query("api_key") apiKey: String = "d2VNS-uLi8EVWIP9meNBYVC-mtf_Kf1A",
        @Query("file_name") file_name : String,
        @Query("languages") lang: String = "en",
        @Query("season_number") season_number:String?=null,
        @Query("episode_number") episode_number:String?=null,
    ): SubDlResponse
}
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS) // Time to establish connection
    .readTimeout(30, TimeUnit.SECONDS)    // Time to wait for data
    .writeTimeout(30, TimeUnit.SECONDS)   // Time to send data
    .build()

object RetrofitClient {
    private const val BASE_URL = "https://api.subdl.com/api/v1/"

    val api: SubDlApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Converts JSON to your Data Class
            .build()
            .create(SubDlApiService::class.java)
    }
}

interface DownloadFile{
    @Streaming
    @GET("{filePath}")
    suspend fun downloadFile(@Path("filePath") fileUrl: String): Response<ResponseBody>
}

object FileDownloadClient {
    private const val BASE_URL = "https://dl.subdl.com/subtitle/"
    val api: DownloadFile by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(DownloadFile::class.java)
    }
}



fun unzipInternal(zipFile: File, targetDir: File) {
    ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
        var entry = zis.nextEntry
        while (entry != null) {
            val newFile = File(targetDir, entry.name)

            // Security check: ensure files stay inside targetDir
            if (!newFile.canonicalPath.startsWith(targetDir.canonicalPath)) {
                throw SecurityException("Zip slip attempt detected")
            }

            if (entry.isDirectory) {
                newFile.mkdirs()
            } else {
                newFile.parentFile?.mkdirs()
                newFile.outputStream().use { zis.copyTo(it) }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }
    }
}
