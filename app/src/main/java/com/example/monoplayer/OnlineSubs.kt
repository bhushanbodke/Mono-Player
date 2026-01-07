package com.example.monoplayer
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

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
        @Query("query") movieName: String,
        @Query("languages") lang: String = "en",
        @Query("season_number") season:String?=null,
        @Query("episode_number ") episode:String?=null,
    ): SubDlResponse
}


object RetrofitClient {
    private const val BASE_URL = "https://api.subdl.com/api/v1/"

    val api: SubDlApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Converts JSON to your Data Class
            .build()
            .create(SubDlApiService::class.java)
    }
}