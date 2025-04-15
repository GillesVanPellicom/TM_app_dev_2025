package com.example.movietracker.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbService {
    @GET("trending/all/day")
    fun getTrending(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): Call<TrendingResponse>
}

data class TrendingResponse(
    @SerializedName("results") val results: List<MovieListItem>
)

data class MovieListItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val movieReleaseDate: String?,
    @SerializedName("first_air_date") val tvReleaseDate: String?,

) {
    val releaseDate: String?
        get() = movieReleaseDate ?: tvReleaseDate

    val formattedReleaseInfo: String?
        get() {
            return if (movieReleaseDate != null) movieReleaseDate?.take(4) else tvReleaseDate?.take(4)
        }
}