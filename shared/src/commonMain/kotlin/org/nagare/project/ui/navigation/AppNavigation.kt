package org.nagare.project.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import org.nagare.project.data.repository.UsuariRepository
import org.nagare.project.ui.screens.auth.CompletarPerfilScreen
import org.nagare.project.ui.screens.auth.LoginScreen
import org.nagare.project.ui.screens.auth.RegisterScreen
import org.nagare.project.ui.screens.main.MainScreen
import org.koin.compose.koinInject

private const val RUTA_LOGIN = "login"
private const val RUTA_REGISTER = "register"
private const val RUTA_COMPLETAR_PERFIL = "completar_perfil/{uid}/{email}"
private const val RUTA_MAIN = "main"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val usuariRepo: UsuariRepository = koinInject()

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val user = Firebase.auth.currentUser
        startDestination = when {
            user == null -> RUTA_LOGIN
            else -> {
                val perfil = try { usuariRepo.getUsuari(user.uid) } catch (e: Exception) { null }
                if (perfil == null) {
                    val email = user.email ?: ""
                    "completar_perfil/${user.uid}/$email"
                } else {
                    RUTA_MAIN
                }
            }
        }
    }

    val dest = startDestination ?: return

    NavHost(navController = navController, startDestination = dest) {
        composable(RUTA_LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    scope.launch {
                        val user = Firebase.auth.currentUser ?: return@launch
                        val perfil = try { usuariRepo.getUsuari(user.uid) } catch (e: Exception) { null }
                        if (perfil == null) {
                            val email = user.email ?: ""
                            navController.navigate("completar_perfil/${user.uid}/$email") {
                                popUpTo(RUTA_LOGIN) { inclusive = true }
                            }
                        } else {
                            navController.navigate(RUTA_MAIN) {
                                popUpTo(RUTA_LOGIN) { inclusive = true }
                            }
                        }
                    }
                },
                onAnarARegistre = { navController.navigate(RUTA_REGISTER) }
            )
        }

        composable(RUTA_REGISTER) {
            RegisterScreen(
                onRegistreSuccess = { uid, email ->
                    navController.navigate("completar_perfil/$uid/$email") {
                        popUpTo(RUTA_REGISTER) { inclusive = true }
                    }
                },
                onAnarALogin = { navController.popBackStack() }
            )
        }

        composable(RUTA_COMPLETAR_PERFIL) { backStack ->
            val email = backStack.arguments?.getString("email") ?: ""
            CompletarPerfilScreen(
                email = email,
                onPerfilDesat = {
                    navController.navigate(RUTA_MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(RUTA_MAIN) {
            MainScreen(
                onLogout = {
                    navController.navigate(RUTA_LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
