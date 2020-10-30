package com.github.david402.githubsearchdemo

import android.content.Context

object ServiceLocator {
    private var giphyRepository: GiphyRepository? = null

    fun provideSearchRepository(context: Context): GiphyRepository {
        synchronized(this) {
            return giphyRepository ?: giphyRepository ?: createSearchRepository()
        }
    }

    private fun createSearchRepository(): GiphyRepository {
        val repository = GiphyRepository()
        giphyRepository = repository
        return repository
    }
}