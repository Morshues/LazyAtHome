package com.morshues.lazyathome

import android.app.Application
import com.morshues.lazyathome.data.network.UrlProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject
    lateinit var urlProvider: UrlProvider

    override fun onCreate() {
        super.onCreate()
        UrlProvider.init(urlProvider)
    }
}
