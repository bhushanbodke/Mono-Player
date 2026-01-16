package com.example.monoplayer

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// 1. ONE INTERFACE (Everything here)
interface SubtitleApi {
    @GET("api/v1/subtitles")
    suspend fun search(
        @Header("Api-Key") apiKey: String,
        @Header("User-Agent") userAgent: String,
        @Query("query") query: String,
        @Query("languages") lang: String = "en"
    ): ResponseBody // Returns raw JSON so we don't need Data Classes

    @POST("api/v1/download")
    suspend fun getLink(
        @Header("Api-Key") apiKey: String,
        @Header("User-Agent") userAgent: String,
        @Body body: Map<String, Int> // Using a Map is easier than a Data Class
    ): ResponseBody
}

// 2. ONE OBJECT (The logic)
object SubtitleRepo {
    private val api = Retrofit.Builder()
        .baseUrl("https://api.opensubtitles.com/api/v1/subtitles")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SubtitleApi::class.java)

    private const val KEY = "yEQ48lHJU8aFpyi1m4Ik8fytK8kPI7v1"
    private const val UA = "mono player"

    suspend fun searchSubtitles(name: String): List<Pair<String, Int>> {
        val response = api.search(KEY, UA, name).string()
        val json = JSONObject(response)
        val data = json.getJSONArray("data")

        val list = mutableListOf<Pair<String, Int>>()
        for (i in 0 until data.length()) {
            val attr = data.getJSONObject(i).getJSONObject("attributes")
            val fileId = attr.getJSONArray("files").getJSONObject(0).getInt("file_id")
            list.add(attr.getString("release") to fileId)
        }
        return list
    }

    suspend fun downloadFile(fileId: Int, context: java.io.File): java.io.File? {
        val linkJson = api.getLink(KEY, UA, mapOf("file_id" to fileId)).string()
        val url = JSONObject(linkJson).getString("link")

        // Use standard URL download
        val connection = java.net.URL(url).openConnection()
        val file = java.io.File(context, "sub_$fileId.srt")
        connection.getInputStream().use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }
}