package org.nagare.project

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import org.koin.compose.KoinContext
import org.nagare.project.ui.navigation.AppNavigation
import org.nagare.project.ui.theme.NagareTheme

@Composable
fun App() {
    KoinContext {
        NagareTheme {
            Surface {
                AppNavigation()
            }
        }
    }
}
