package com.program.blindfoldchesscouch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.program.blindfoldchesscouch.ui.screens.MainMenuScreen
import com.program.blindfoldchesscouch.ui.screens.Module1Screen
import com.program.blindfoldchesscouch.ui.screens.Module2Screen
import com.program.blindfoldchesscouch.ui.screens.Module3Screen
import com.program.blindfoldchesscouch.ui.screens.Module4Screen // <-- НОВИ IMPORT
import com.program.blindfoldchesscouch.ui.screens.ModuleScreen
import com.program.blindfoldchesscouch.viewmodel.GameViewModel

/**
 * Definiše rute (putanje) za ekrane u aplikaciji.
 */
object AppRoutes {
    const val MAIN_MENU = "main_menu"
}

/**
 * Glavni kompozabil za navigaciju koji upravlja svim ekranima u aplikaciji.
 *
 * @param gameViewModel Generički ViewModel koji se prosleđuje modulima
 * koji još uvek nemaju svoj namenski ViewModel.
 */
@Composable
fun AppNavigation(gameViewModel: GameViewModel) {
    val navController = rememberNavController()

    // NavHost je kontejner koji upravlja zamenom ekrana.
    NavHost(navController = navController, startDestination = AppRoutes.MAIN_MENU) {

        // Definicija rute za Glavni Meni
        composable(AppRoutes.MAIN_MENU) {
            MainMenuScreen(
                modules = trainingModules, // Koristi listu iz TrainingModules.kt
                onModuleSelected = { moduleRoute ->
                    // Ova lambda funkcija se poziva kada korisnik klikne na modul
                    navController.navigate(moduleRoute)
                }
            )
        }

        // --- NOVI, UNAPREĐENI NAČIN RUKOVANJA RUTAMA ---
        // Prolazimo kroz SVE module definisane u TrainingModules.kt
        trainingModules.forEach { module ->
            composable(module.route) {
                // Koristimo `when` da odlučimo koji ekran da prikažemo za koju rutu
                when (module.route) {
                    "module_1" -> Module1Screen()
                    "module_2" -> Module2Screen()
                    "module_3" -> Module3Screen()
                    "module_4" -> Module4Screen() // <-- НОВА ЛИНИЈА

                    // Svi ostali moduli će koristiti generički ekran
                    else -> ModuleScreen(
                        module = module,
                        gameViewModel = gameViewModel
                    )
                }
            }
        }
    }
}