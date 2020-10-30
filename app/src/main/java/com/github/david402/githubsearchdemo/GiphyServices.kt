package com.github.david402.githubsearchdemo

import com.github.david402.githubsearchdemo.data.GifObject
import com.github.david402.githubsearchdemo.data.ImageObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.github.david402.githubsearchdemo.data.Result as Result

interface GiphyServices {
    @GET("/v1/gifs/trending")
    fun getTrendingGifs(@Query("api_key") text: String = Configs.API_KEY): Call<Result>
    @GET("/v1/gifs/search")
    fun searchGifs(@Path ("q") query: String): Call<Result>
}

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
