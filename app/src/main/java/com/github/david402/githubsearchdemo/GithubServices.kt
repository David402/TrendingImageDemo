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
//    val id: Int,
//    val node_id: String,
    val avatar_url: String
//    val gravatar_id: String?,
//    val url: String,
//    val html_url: String,
//    val followers_url: String,
//    val following_url: String,
//    val gists_url: String,
//    val starred_url: String,
//    val subscriptions_url: String,
//    val organizations_url: String,
//    val repos_url: String,
//    val events_url: String,
//    val received_events_url: String,
//    val type: String,
//    val site_admin: Boolean,
//    val score: Double
)

data class UserInfoResult(
    val login: String,
//    val id: Int,
//    val node_id: String,
    val avatar_url: String,
//    val gravatarId: String?,
//    val url: String,
//    val htmlUrl: String,
//    val followersUrl: String,
//    val followingUrl: String,
//    val gistsUrl: String,
//    val starredUrl: String,
//    val subscriptionsUrl: String,
//    val organizationsUrl: String,
//    val reposUrl: String,
//    val eventsUrl: String,
//    val receivedEventsUrl: String,
//    val type: String,
//    val siteAdmin: Boolean,
//    val name: String,
//    val company: String,
//    val blog: String,
//    val location: String,
//    val email: String?,
//    val hireable: Boolean?,
//    val bio: String?,
//    val twitterUsername: String?,
    val public_repos: Int
//    val public_gists: Int,
//    val followers: Int,
//    val following: Int
)

data class User(
    val name: String,
    val avatarUrl: String,
    val publicRepos: Int
)

object GithubServiceBuilder {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}
