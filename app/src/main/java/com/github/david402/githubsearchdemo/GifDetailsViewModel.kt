package com.github.david402.githubsearchdemo

import androidx.lifecycle.*
import com.github.david402.githubsearchdemo.data.GifObject

class GifDetailsViewModel(
    private val giphyRepository: GiphyRepository
) : ViewModel() {
    private val _gifId = MutableLiveData<String>()
    private val _gif = _gifId.switchMap { gifId ->
        giphyRepository.observeGif(gifId)
    }
    val gif: LiveData<GifObject> = _gif

    fun start(id: String) {
        _gifId.value = id
    }
}