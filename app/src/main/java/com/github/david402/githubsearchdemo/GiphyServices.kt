package com.github.david402.githubsearchdemo

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GiphyServices {
    @GET("/v1/gifs/trending")
    fun getTrendingGifs(@Query("api_key") text: String = Configs.API_KEY): Call<Result>
    @GET("/v1/gifs/search")
    fun searchGifs(@Path ("q") query: String): Call<Result>
}


data class Result(
    val data: List<GifObject>,
    val pagination: PaginationObject,
    val meta: MetaObject
)

data class GifObject (
    val type: String,
    val id: String,
    val slug: String,
    val url: String,
    val embed_url: String,
    val username: String,
    val source: String,
    val rating: String,
    val title: String,
    val images: ImagesObject
)

data class ImagesObject(
    val original: ImageObject,
    val downsized: ImageObject
)

data class ImageObject (
    val height: Int,
    val width: Int,
    val url: String
)
data class MetaObject(
    val msg: String,
    val status: String,
    @Field("response_id") val responseId: String
)
data class PaginationObject(
    val offset: Int,
    @Field("total_count") val totalCount: Int,
    val count: Int
)
data class GithubUser (
    val login: String,
    val avatar_url: String
)

data class UserInfoResult(
    val login: String,
    val avatar_url: String,
    val public_repos: Int
)

data class User(
    val name: String,
    val avatarUrl: String,
    val publicRepos: Int
)

object GiphyServicesBuilder {
    private val client = OkHttpClient.Builder().build()
    private const val ENDPOINT = "https://api.giphy.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(ENDPOINT)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}
