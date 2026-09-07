package com.flux.android

import android.app.Application
import com.flux.android.di.AppContainer

class FluxApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        appContainer.analytics.appOpen()
    }
}
