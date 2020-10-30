package com.github.david402.githubsearchdemo.util

import androidx.fragment.app.Fragment
import com.github.david402.githubsearchdemo.GiphyApplication
import com.github.david402.githubsearchdemo.ViewModelFactory

fun Fragment.getViewModelFactory(): ViewModelFactory {
    val repository = (requireContext().applicationContext as GiphyApplication).giphyRepository
    return ViewModelFactory(repository, this)
}