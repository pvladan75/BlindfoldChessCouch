package com.program.blindfoldchesscouch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.program.blindfoldchesscouch.ui.screens.MainMenuScreen
import com.program.blindfoldchesscouch.ui.screens.Module1Screen
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

        // Namenska ruta samo za naš implementirani Modul 1
        composable(trainingModules[0].route) { // Pretpostavljamo da je Modul 1 prvi na listi
            Module1Screen() // Pozivamo namenski ekran za Modul 1
        }

        // Generičke rute za sve ostale module koje još nismo napravili.
        // `drop(1)` preskače prvi element liste (Modul 1)
        trainingModules.drop(1).forEach { module ->
            composable(module.route) {
                // Ostali moduli i dalje koriste stari, generički ekran.
                ModuleScreen(
                    module = module,
                    gameViewModel = gameViewModel
                )
            }
        }
    }
}