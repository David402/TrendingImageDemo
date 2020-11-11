package com.github.david402.giphydemo

import android.app.Application

class GiphyApplication : Application() {
    val giphyRepository : GiphyRepository
        get() = ServiceLocator.provideSearchRepository(this)
}