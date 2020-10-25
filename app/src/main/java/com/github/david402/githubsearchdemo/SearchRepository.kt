package com.github.david402.githubsearchdemo

import android.content.res.AssetManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.UnknownHostException
import kotlin.random.Random

interface SearchApi {
    suspend fun performSearch(query: String): List<User>
}

class SearchRepository(private val assets: AssetManager,
                       private val maxResult: Int = DEFAULT_RESULT_MAX_SIZE) : SearchApi {
    companion object {
        private const val DEFAULT_RESULT_MAX_SIZE = 250
        private const val RANDOM_ERROR_THRESHOLD = 0.75
    }

    private val TAG = "Search Repository"
    val githubService = GithubServiceBuilder.buildService(GithubServices::class.java)

    override suspend fun performSearch(query: String): List<User> = withContext(Dispatchers.IO) {
        val users = searchGithubUsers(query)
        getUserInfo(users)
    }

    private suspend fun searchGithubUsers(query: String): List<GithubUser> {
        Log.d(TAG, "search user - $query")
        return withContext(Dispatchers.IO) {
            val call = githubService.searchUsers(query, 10)
            Log.d(TAG,"API call - " + call.request().toString())
            val response = call.execute()
            if (!response.isSuccessful) {
                Log.d(TAG,"API call failed")
                Log.d(TAG, "code: " + response.code() + ", message: " + response.message())
            }
            Log.d(TAG,"API call successful")
            Log.d(TAG,"res.body() - ${response.body()}")
            val result = response.body()?.items
            result?: emptyList()
        }
    }

    private suspend fun getUserInfo(users: List<GithubUser>): List<User> = users?.map { user ->
        withContext(Dispatchers.IO) {
            val call = githubService.getUserInfo(user.login)
            val response = call.execute()
            if (!response.isSuccessful) {
                Log.d(TAG,"(2) API call failed - code=${response.code()}, message: ${response.message()}")
                User(user.login, user.avatar_url, -1)
            }
            val result: UserInfoResult = response.body()!!
            Log.d(TAG,"(2) API call successful")
            Log.d(TAG,"(2) res.body() - ${response.body()}")
            User(result?.login, result?.avatar_url, result?.public_repos)
        }
    }
}