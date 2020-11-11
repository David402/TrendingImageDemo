package com.github.david402.giphydemo

import android.accounts.NetworkErrorException
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import com.github.david402.giphydemo.data.GifObject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*

sealed class SearchResult
class ValidResult(val result: List<GifObject>) : SearchResult()
object EmptyResult : SearchResult()
object EmptyQuery : SearchResult()
class ErrorResult(val e: Throwable) : SearchResult()
object TerminalError : SearchResult()
class RateLimitError(val e: Throwable) : SearchResult()

class SearchViewModel(
    private val giphyRepository: GiphyRepository,
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
                giphyRepository.updateTrendingGifs()
                _dataLoading.value = false
            }
        }
        giphyRepository.observeGifs()
    }


    val items: LiveData<List<GifObject>> = _items

    private val _dataLoading = MutableLiveData<Boolean>(false)
    val dataLoading: LiveData<Boolean> = _dataLoading
    private val _openGifEvent = MutableLiveData<Event<String>>()
    val openGifEvent: LiveData<Event<String>> = _openGifEvent

    @ExperimentalCoroutinesApi
    @VisibleForTesting
    internal val queryChannel = BroadcastChannel<String>(Channel.CONFLATED)

    fun refresh() {
        _forceUpdate.value = true
    }

    fun selectGif(id: String) {
        _openGifEvent.value = Event(id)
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
                        giphyRepository.searchGifs(it)
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
}
