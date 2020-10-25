package com.github.david402.githubsearchdemo

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubServices {
    @GET("/search/users")
    fun searchUsers(@Query("q") text: String, @Query("per_page") pageSize: Int): Call<SearchUsersResult>
    @GET("/users/{id}")
    fun getUserInfo(@Path ("id") id: String): Call<UserInfoResult>
}


data class SearchUsersResult(
    var total_count: Int,
    var incomplete_results: Boolean,
    var items: List<GithubUser>
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

object GithubServiceBuilder {
    private val client = OkHttpClient.Builder().build()
    private const val ENDPOINT = "https://api.github.com/";
    private val retrofit = Retrofit.Builder()
        .baseUrl(ENDPOINT)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}
