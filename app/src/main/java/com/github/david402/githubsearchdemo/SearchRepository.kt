package com.github.david402.githubsearchdemo

import android.accounts.NetworkErrorException
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.stream.Collectors

interface SearchApi {
    suspend fun performSearch(query: String): List<User>
    suspend fun performSearch(query: String, size: Int): List<User>
}

class SearchRepository() : SearchApi {
    companion object {
        private const val DEFAULT_RESULT_MAX_SIZE = 10
    }

    private val TAG = "Search Repository"
    private val githubService = GithubServiceBuilder.buildService(GithubServices::class.java)

    /**
     * This function will perform Github search api for users matches `query` with Default result size
     *
     * Return - A list of `User` or empty list if no result matched
     * Throw - NetworkErrorException when Github API Rate Limit hits
     */
    override suspend fun performSearch(query: String): List<User> {
        return performSearch(query, DEFAULT_RESULT_MAX_SIZE)
    }

    inline fun measureTimeMillis(block: () -> Unit): Long {
        val start = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - start
    }

    /**
     * This function will perform Github search api for users matches `query` with custom result size
     *
     * Return - A list of `User` or empty list if no result matched
     * Throw - NetworkErrorException when Github API Rate Limit hits
     */
    override suspend fun performSearch(query: String, size: Int): List<User> = withContext(Dispatchers.IO) {
        val users = searchGithubUsers(query, size)
        lateinit var result: List<User>
        val elapsed = measureTimeMillis {
            result = getUserInfo(users)
        }
        println("API call time elapsed: $elapsed")
        result
    }

    private suspend fun searchGithubUsers(query: String, size: Int = DEFAULT_RESULT_MAX_SIZE): List<GithubUser> {
        Log.d(TAG, "search user - $query")
        return withContext(Dispatchers.IO) {
            val call = githubService.searchUsers(query, size)
            Log.d(TAG,"API call - " + call.request().toString())
            val response = call.execute()
            if (!response.isSuccessful) {
                Log.d(TAG,"API call failed")
                Log.d(TAG, "code: " + response.code() + ", message: " + response.message())
                throw NetworkErrorException("Github API Rate Limit Hits.")
            }
            Log.d(TAG,"API call successful")
            Log.d(TAG,"res.body() - ${response.body()}")
            val result = response.body()?.items
            result?: emptyList()
        }
    }

    private suspend fun getUserInfo(users: List<GithubUser>): List<User> = users.parallelStream().map { user ->
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

    }.collect(Collectors.toList())
}