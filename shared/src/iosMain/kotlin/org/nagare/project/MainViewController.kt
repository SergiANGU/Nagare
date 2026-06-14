package org.nagare.project

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.startKoin
import org.nagare.project.di.appModule

fun MainViewController() = ComposeUIViewController { App() }

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}