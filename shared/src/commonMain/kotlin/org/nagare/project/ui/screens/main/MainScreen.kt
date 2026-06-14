package org.nagare.project.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.nagare.project.Strings
import org.nagare.project.data.model.Usuari
import org.nagare.project.data.repository.AuthRepository
import org.nagare.project.data.repository.UsuariRepository
import org.nagare.project.ui.screens.activitats.ActivitatsScreen
import org.nagare.project.ui.screens.activitats.DetallActivitatScreen
import org.nagare.project.ui.screens.entrenos.EntrenosScreen
import org.nagare.project.ui.screens.noticies.NoticiesScreen
import org.nagare.project.ui.screens.perfil.PerfilScreen

private const val NOTICIES = "noticies"
private const val ACTIVITATS = "activitats"
private const val ENTRENOS = "entrenos"
private const val DETALL_ACTIVITAT = "detall_activitat/{activitatId}"
private const val PERFIL = "perfil"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val authRepo: AuthRepository = koinInject()
    val usuariRepo: UsuariRepository = koinInject()

    var usuari by remember { mutableStateOf<Usuari?>(null) }

    LaunchedEffect(Unit) {
        val uid = Firebase.auth.currentUser?.uid ?: return@LaunchedEffect
        usuari = try { usuariRepo.getUsuari(uid) } catch (e: Exception) { null }
    }

    val u = usuari ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute by navController.currentBackStackEntryAsState()
    val rutaActual = currentRoute?.destination?.route

    val rutesAmbDrawer = listOf(NOTICIES, ACTIVITATS, ENTRENOS, PERFIL)
    val mostrarDrawerUI = rutaActual in rutesAmbDrawer

    val seccions = listOf(
        NOTICIES to Strings.NOTICIES,
        ACTIVITATS to Strings.ACTIVITATS,
        ENTRENOS to Strings.ENTRENOS,
        PERFIL to Strings.PERFIL
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = mostrarDrawerUI,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${u.nom} ${u.cognoms}", style = MaterialTheme.typography.titleMedium)
                    Text(u.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                seccions.forEach { (ruta, label) ->
                    NavigationDrawerItem(
                        label = { Text(label) },
                        selected = rutaActual == ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(ruta) {
                                popUpTo(NOTICIES) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                HorizontalDivider()
                TextButton(
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            authRepo.signOut()
                            onLogout()
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(Strings.TANCAR_SESSIO, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (mostrarDrawerUI) {
                    TopAppBar(
                        title = {
                            val titol = seccions.firstOrNull { it.first == rutaActual }?.second ?: Strings.NOTICIES
                            Text(titol)
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Text("☰")
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = NOTICIES,
                modifier = Modifier.padding(padding)
            ) {
                composable(NOTICIES) {
                    NoticiesScreen(usuari = u)
                }
                composable(ACTIVITATS) {
                    ActivitatsScreen(usuari = u, navController = navController)
                }
                composable(ENTRENOS) {
                    EntrenosScreen(usuari = u)
                }
                composable(DETALL_ACTIVITAT) { backStack ->
                    val activitatId = backStack.arguments?.getString("activitatId") ?: ""
                    DetallActivitatScreen(
                        activitatId = activitatId,
                        usuari = u,
                        navController = navController
                    )
                }
                composable(PERFIL) {
                    PerfilScreen(usuari = u, onActualitzat = { novasDades ->
                        usuari = novasDades
                    })
                }
            }
        }
    }
}
