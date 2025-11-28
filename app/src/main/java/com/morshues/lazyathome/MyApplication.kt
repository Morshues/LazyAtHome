package com.morshues.lazyathome

import android.app.Application
import com.morshues.lazyathome.di.AppModule

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
    }
}