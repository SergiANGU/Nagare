package org.nagare.project.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.nagare.project.data.repository.ActivitatsRepository
import org.nagare.project.data.repository.AuthRepository
import org.nagare.project.data.repository.EntrenosRepository
import org.nagare.project.data.repository.NoticiesRepository
import org.nagare.project.data.repository.UsuariRepository
import org.nagare.project.ui.screens.auth.CompletarPerfilViewModel
import org.nagare.project.ui.screens.auth.LoginViewModel
import org.nagare.project.ui.screens.auth.RegisterViewModel
import org.nagare.project.ui.screens.activitats.ActivitatsViewModel
import org.nagare.project.ui.screens.activitats.DetallActivitatViewModel
import org.nagare.project.ui.screens.entrenos.EntrenosViewModel
import org.nagare.project.ui.screens.noticies.NoticiesViewModel

val appModule = module {
    // Repositoris
    single { AuthRepository() }
    single { UsuariRepository() }
    single { NoticiesRepository() }
    single { ActivitatsRepository() }
    single { EntrenosRepository() }

    // ViewModels
    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { CompletarPerfilViewModel(get(), get()) }
    viewModel { NoticiesViewModel(get(), get()) }
    viewModel { ActivitatsViewModel(get(), get()) }
    viewModel { DetallActivitatViewModel(get(), get()) }
    viewModel { EntrenosViewModel(get(), get()) }
}
