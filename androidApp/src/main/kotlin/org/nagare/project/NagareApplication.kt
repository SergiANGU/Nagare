package org.nagare.project

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.nagare.project.di.appModule

class NagareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NagareApplication)
            modules(appModule)
        }
    }
}
