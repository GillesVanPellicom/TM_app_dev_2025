package com.example.movietracker.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbService {



    @GET("trending/all/day")
    fun getTrending(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int
    ): Call<TrendingResponse>

    @GET("movie/{movie_id}")
    fun getMovie(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Call<MovieResponse>

    @GET("tv/{id}")
    fun getTvShow(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): Call<TvShowResponse>

    @GET("search/multi")
    fun searchMoviesOrShows(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("language") language: String = "en-US"
    ): Call<TrendingResponse>

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3/"
        private const val API_KEY = "140b81b85e8e8baf9d417e99a3c9ab7e"

        fun create(): TmdbService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TmdbService::class.java)
        }

        fun getApiKey(): String = API_KEY
    }
}

data class TrendingResponse(
    @SerializedName("results") val results: List<TrendingListItem>
)

data class TrendingListItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val movieReleaseDate: String?,
    @SerializedName("first_air_date") val tvReleaseDate: String?,
    @SerializedName("media_type") val mediaType: String?,
    @SerializedName("popularity") val popularity: Double = 0.0

) {
    val releaseDate: String?
        get() = movieReleaseDate ?: tvReleaseDate

    val formattedReleaseInfo: String?
        get() {
            return if (mediaType == "movie") movieReleaseDate?.take(4) else tvReleaseDate?.take(4)
        }
}

data class MovieResponse(
    @SerializedName("adult") val adult: Boolean,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("belongs_to_collection") val belongsToCollection: Any?, // Use a specific type if the structure is known
    @SerializedName("budget") val budget: Int,
    @SerializedName("genres") val genres: List<Genre>,
    @SerializedName("homepage") val homepage: String?,
    @SerializedName("id") val id: Int,
    @SerializedName("imdb_id") val imdbId: String?,
    @SerializedName("original_language") val originalLanguage: String?,
    @SerializedName("original_title") val originalTitle: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("popularity") val popularity: Double,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("production_companies") val productionCompanies: List<ProductionCompany>,
    @SerializedName("production_countries") val productionCountries: List<ProductionCountry>,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("revenue") val revenue: Int,
    @SerializedName("runtime") val runtime: Int?,
    @SerializedName("spoken_languages") val spokenLanguages: List<SpokenLanguage>,
    @SerializedName("status") val status: String?,
    @SerializedName("tagline") val tagline: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("video") val video: Boolean,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("vote_count") val voteCount: Int
)

data class TvShowResponse(
    val adult: Boolean,
    val backdropPath: String?,
    val createdBy: List<Creator>,
    val episodeRunTime: List<Int>,
    @SerializedName("first_air_date") val firstAirDate: String?,
    val genres: List<Genre>,
    val homepage: String?,
    val id: Int,
    val inProduction: Boolean,
    val languages: List<String>,
    val lastAirDate: String?,
    val lastEpisodeToAir: Episode?,
    val name: String?,
    val nextEpisodeToAir: Episode?,
    val networks: List<Network>,
    val numberOfEpisodes: Int,
    val numberOfSeasons: Int,
    val originCountry: List<String>,
    @SerializedName("original_language") val originalLanguage: String,
    val originalName: String,
    val overview: String?,
    val popularity: Double,
    @SerializedName("poster_path") val posterPath: String?,
    val productionCompanies: List<ProductionCompany>,
    val productionCountries: List<ProductionCountry>,
    val seasons: List<Season>,
    val spokenLanguages: List<SpokenLanguage>,
    val status: String,
    val tagline: String?,
    val type: String,
    val voteAverage: Double,
    val voteCOunt: Int
)

data class Creator(val name: String)
data class Genre(val id: Int, val name: String)
data class Episode(
    val id: Int,
    val name: String,
    val overView: String,
    val voteAverage: Double,
    val voteCount: Int,
    val airDate: String?,
    val episodeNumber: Int,
    val episodeType: String,
    val productionCode: String,
    val runtime: Int?,
    val seasonNumber: Int,
    val showId: Int,
    val stillPath: String?
)

data class Network(val id: Int, val name: String)
data class ProductionCompany(val id: Int, val name: String)
data class ProductionCountry(val iso31661: String, val name: String)
data class Season(
    val airDate: String?,
    val episodeCount: Int,
    val id: Int,
    val name: String,
    val overview: String,
    val posterPath: String?,
    val seasonNumber: Int,
    val voteAverage: Double
)

data class SpokenLanguage(val englishName: String, val iso6391: String, val name: String)