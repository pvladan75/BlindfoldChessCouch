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
    const val TUTORIAL_MENU = "tutorial_menu" // Ruta za novi ekran sa sadržajem uputstva
    const val INSTRUCTIONS = "instructions/{topicId}" // Ruta za interaktivni ekran sada prima argument
}

@Composable
fun AppNavigation(gameViewModel: GameViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoutes.MAIN_MENU) {

        composable(AppRoutes.MAIN_MENU) {
            MainMenuScreen(
                onModuleSelected = { moduleRoute -> navController.navigate(moduleRoute) },
                onNavigate = { route ->
                    // Klik na "Uputstvo" u meniju će pozvati ovo sa "tutorial_menu"
                    navController.navigate(route)
                }
            )
        }

        // Nova ruta za meni sa uputstvima
        composable(AppRoutes.TUTORIAL_MENU) {
            TutorialMenuScreen(onTopicSelected = { topicId ->
                // Kada korisnik izabere temu, navigiramo na interaktivni ekran sa ID-jem te teme
                navController.navigate("instructions/$topicId")
            })
        }

        // Ažurirana ruta za interaktivni ekran koja prihvata "topicId"
        composable(
            route = AppRoutes.INSTRUCTIONS,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Prosleđujemo topicId ViewModel-u preko SavedStateHandle-a (automatski)
            InstructionsScreen()
        }

        // Rute za module ostaju iste
        trainingModules.forEach { module ->
            composable(module.route) {
                when (module.route) {
                    "module_1" -> Module1Screen()
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