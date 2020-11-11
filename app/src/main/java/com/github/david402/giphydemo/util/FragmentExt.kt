package com.github.david402.giphydemo.util

import androidx.fragment.app.Fragment
import com.github.david402.giphydemo.GiphyApplication
import com.github.david402.giphydemo.ViewModelFactory

fun Fragment.getViewModelFactory(): ViewModelFactory {
    val repository = (requireContext().applicationContext as GiphyApplication).giphyRepository
    return ViewModelFactory(repository, this)
}