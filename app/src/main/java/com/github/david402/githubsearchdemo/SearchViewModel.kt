package com.github.david402.githubsearchdemo

import android.accounts.NetworkErrorException
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest

sealed class SearchResult
class ValidResult(val result: List<GifObject>) : SearchResult()
object EmptyResult : SearchResult()
object EmptyQuery : SearchResult()
class ErrorResult(val e: Throwable) : SearchResult()
object TerminalError : SearchResult()
class RateLimitError(val e: Throwable) : SearchResult()

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    companion object {
        const val SEARCH_DELAY_MS = 100L
        const val MIN_QUERY_LENGTH = 3
    }

    private val _forceUpdate = MutableLiveData<Boolean>(false)
    private val _items: LiveData<List<GifObject>> = _forceUpdate.switchMap { forceUpdate ->
        if (forceUpdate) {
            _dataLoading.value = true

            viewModelScope.launch {
                searchRepository.updateTrendingGifs()
                _dataLoading.value = false
            }
        }
        searchRepository.obeserveGifs()
    }


    val items: LiveData<List<GifObject>> = _items

    private val _dataLoading = MutableLiveData<Boolean>(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    @ExperimentalCoroutinesApi
    @VisibleForTesting
    internal val queryChannel = BroadcastChannel<String>(Channel.CONFLATED)

    fun refresh() {
        _forceUpdate.value = true
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @VisibleForTesting
    internal val internalSearchResult = queryChannel
        .asFlow()
        .debounce(SEARCH_DELAY_MS)
        .mapLatest {
            try {
                if (it.length >= MIN_QUERY_LENGTH) {
                    withContext(ioDispatcher) {
                        searchRepository.searchGifs(it)
                    }
//                    println("Search result: ${searchResult.size} hits")

//                    if (searchResult.isNotEmpty()) {
//                        ValidResult(searchResult)
//                    } else {
//                        EmptyResult
//                    }
                } else {
                    EmptyQuery
                }
            } catch (e: Throwable) {
                if (e is CancellationException) {
                    println("Search was cancelled!")
                    throw e
                } else if (e is NetworkErrorException) {
                    println("Search rate limit exceeded!")
                    RateLimitError(e)
                } else {
                    ErrorResult(e)
                }
            }
        }
        .catch { emit(TerminalError) }

    @FlowPreview
    @ExperimentalCoroutinesApi
    val searchResult = internalSearchResult.asLiveData()

    fun handleSearchResult(items: List<GifObject>) {

    }

    class Factory(private val dispatcher: CoroutineDispatcher) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SearchViewModel(SearchRepository(), dispatcher) as T
        }
    }
}
