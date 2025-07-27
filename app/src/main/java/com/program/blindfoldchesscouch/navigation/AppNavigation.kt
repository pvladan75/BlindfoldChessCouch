// navigation/AppNavigation.kt
package com.program.blindfoldchesscouch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.program.blindfoldchesscouch.ui.screens.MainMenuScreen
import com.program.blindfoldchesscouch.ui.screens.ModuleScreen
import com.program.blindfoldchesscouch.viewmodel.GameViewModel

object AppRoutes {
    const val MAIN_MENU = "main_menu"
}

@Composable
fun AppNavigation(gameViewModel: GameViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoutes.MAIN_MENU) {
        composable(AppRoutes.MAIN_MENU) {
            MainMenuScreen(
                modules = trainingModules, // Koristi listu iz TrainingModules.kt
                onModuleSelected = { moduleRoute ->
                    navController.navigate(moduleRoute)
                }
            )
        }

        trainingModules.forEach { module -> // Koristi listu iz TrainingModules.kt
            composable(module.route) {
                ModuleScreen(
                    module = module,
                    gameViewModel = gameViewModel
                )
            }
        }
    }
}