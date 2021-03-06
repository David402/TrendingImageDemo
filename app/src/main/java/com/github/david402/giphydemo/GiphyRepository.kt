package com.github.david402.giphydemo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.david402.giphydemo.data.GifObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface SearchApi {
    suspend fun performSearch(query: String): List<User>
    suspend fun performSearch(query: String, size: Int): List<User>
}

class GiphyRepository {
    companion object {
        private const val TAG = "Search Repository"
        private const val DEFAULT_RESULT_MAX_SIZE = 10
    }

    private val giphyService = GiphyServicesBuilder.buildService(GiphyServices::class.java)

//    /**
//     * This function will perform Github search api for users matches `query` with Default result size
//     *
//     * Return - A list of `User` or empty list if no result matched
//     * Throw - NetworkErrorException when Github API Rate Limit hits
//     */
//    override suspend fun performSearch(query: String): List<User> {
//        return performSearch(query, DEFAULT_RESULT_MAX_SIZE)
//    }
//
//    /**
//     * This function will perform Github search api for users matches `query` with custom result size
//     *
//     * Return - A list of `User` or empty list if no result matched
//     * Throw - NetworkErrorException when Github API Rate Limit hits
//     */
//    override suspend fun performSearch(query: String, size: Int): List<User> = withContext(Dispatchers.IO) {
//        val users = searchGithubUsers(query, size)
//        lateinit var result: List<User>
//        val elapsed = measureTimeMillis {
//            result = getUserInfo(users)
//        }
//        println("API call time elapsed: $elapsed")
//        result
//    }
    private val _gifs: MutableLiveData<List<GifObject>> = MutableLiveData()
    private val _selectedGif: MutableLiveData<GifObject> = MutableLiveData()

    fun observeGifs() = _gifs
    fun observeGif(id: String): LiveData<GifObject> = run {
        val gif = _gifs.value?.find { it.id == id }
        _selectedGif.value = gif
        _selectedGif
    }

    suspend fun updateTrendingGifs() {
        _gifs.value =  withContext(Dispatchers.IO) {
            val call = giphyService.getTrendingGifs()
            val response = call.execute()
            if (!response.isSuccessful) {
                Log.d(TAG,"API call failed")
                Log.d(TAG, "code: " + response.code() + ", message: " + response.message())
            }
            val result = response.body()?.data
            result?: emptyList()
        }
    }

    suspend fun searchGifs(query: String) {
        _gifs.value = withContext(Dispatchers.IO) {
            val call = giphyService.searchGifs(query)
            val response = call.execute()
            if (!response.isSuccessful) {
                Log.d(TAG,"API call failed")
                Log.d(TAG, "code: " + response.code() + ", message: " + response.message())
            }
            val result = response.body()?.data
            result?: emptyList()
        }
    }

//    private suspend fun searchGithubUsers(query: String, size: Int = DEFAULT_RESULT_MAX_SIZE): List<GithubUser> {
//        Log.d(TAG, "search user - $query")
//        return withContext(Dispatchers.IO) {
//            val call = giphyService.searchUsers(query, size)
//            val response = call.execute()
//            if (!response.isSuccessful) {
//                Log.d(TAG,"API call failed")
//                Log.d(TAG, "code: " + response.code() + ", message: " + response.message())
//                throw NetworkErrorException("Github API Rate Limit Hits.")
//            }
//            val result = response.body()?.items
//            result?: emptyList()
//        }
//    }

//    private suspend fun getUserInfo(users: List<GithubUser>): List<User> = users.parallelStream().map { user ->
//        val call = giphyService.getUserInfo(user.login) // Retrofit call
//        val response = call.execute()
//        if (!response.isSuccessful) {
//            Log.d(
//                TAG,
//                "API call failed - code=${response.code()}, message: ${response.message()}"
//            )
//            throw NetworkErrorException("Github API Rate Limit Hits.")
//        }
//        val result: UserInfoResult? = response.body()
//        User(result?.login!!, result?.avatar_url!!, result?.public_repos!!)
//    }.collect(Collectors.toList())

    inline fun measureTimeMillis(block: () -> Unit): Long {
        val start = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - start
    }
}