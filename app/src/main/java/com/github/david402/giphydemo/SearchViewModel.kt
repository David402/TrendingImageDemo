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

    private val _items: LiveData<List<GifObject>> = giphyRepository.observeGifs()

    val items: LiveData<List<GifObject>> = _items

    private val _dataLoading = MutableLiveData<Boolean>(false)
    val dataLoading: LiveData<Boolean> = _dataLoading
    private val _openGifEvent = MutableLiveData<Event<String>>()
    val openGifEvent: LiveData<Event<String>> = _openGifEvent

    @ExperimentalCoroutinesApi
    @VisibleForTesting
    internal val queryChannel = ConflatedBroadcastChannel<String>()

    fun refresh() = runBlocking{
        _dataLoading.value = true
        viewModelScope.launch {
            giphyRepository.updateTrendingGifs()
            _dataLoading.value = false
        }
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
                    _dataLoading.value = true
                    viewModelScope.launch {
//                    withContext(ioDispatcher) {
                        giphyRepository.searchGifs(it)
                        _dataLoading.value = false
                    }
                } else {
                }
            } catch (e: Throwable) {
//                when (e) {
//                    is CancellationException -> {
//                        println("Search was cancelled!")
//                        throw e
//                    }
//                    is NetworkErrorException -> {
//                        println("Search rate limit exceeded!")
//                        RateLimitError(e)
//                    }
//                    else -> {
//                        ErrorResult(e)
//                    }
//                }
                giphyRepository.observeGifs()
            }
        }
        .catch { emit(giphyRepository.observeGifs()) }

    @FlowPreview
    @ExperimentalCoroutinesApi
    val searchResult = internalSearchResult.asLiveData()
}
