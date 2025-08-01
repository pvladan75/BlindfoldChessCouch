package com.program.blindfoldchesscouch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.program.blindfoldchesscouch.ui.screens.*
import com.program.blindfoldchesscouch.viewmodel.GameViewModel

object AppRoutes {
    const val MAIN_MENU = "main_menu"
    const val TUTORIAL_MENU = "tutorial_menu"
    const val INSTRUCTIONS = "instructions/{topicId}"
}

@Composable
fun AppNavigation(gameViewModel: GameViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoutes.MAIN_MENU) {

        composable(AppRoutes.MAIN_MENU) {
            MainMenuScreen(
                onModuleSelected = { moduleRoute -> navController.navigate(moduleRoute) },
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable(AppRoutes.TUTORIAL_MENU) {
            TutorialMenuScreen(onTopicSelected = { topicId ->
                navController.navigate("instructions/$topicId")
            })
        }

        composable(
            route = AppRoutes.INSTRUCTIONS,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) {
            InstructionsScreen()
        }

        trainingModules.forEach { module ->
            composable(module.route) {
                when (module.route) {
                    // *** ИСПРАВКА ЈЕ ОВДЕ ***
                    "module_1" -> Module1Screen(
                        // Prosleđujemo funkciju za povratak na glavni meni
                        onNavigateToMainMenu = {
                            navController.navigate(AppRoutes.MAIN_MENU) {
                                // Brišemo sve sa steka da korisnik ne može da se vrati nazad
                                popUpTo(AppRoutes.MAIN_MENU) { inclusive = true }
                            }
                        },
                        // Prosleđujemo funkciju za restartovanje Modula 1
                        onNavigateToModule1Setup = {
                            navController.navigate(module.route) {
                                popUpTo(module.route) { inclusive = true }
                            }
                        }
                    )
                    "module_2" -> Module2Screen()
                    "module_3" -> Module3Screen()
                    "module_4" -> Module4Screen()
                    else -> ModuleScreen(
                        module = module,
                        gameViewModel = gameViewModel
                    )
                }
            }
        }
    }
}