// MainActivity.kt
package com.program.blindfoldchesscouch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // <-- Proverite da li je ovo importovano
import com.program.blindfoldchesscouch.navigation.AppNavigation
import com.program.blindfoldchesscouch.ui.theme.BlindfoldChessCouchTheme
import com.program.blindfoldchesscouch.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    // Kreiramo ViewModel ovde, na nivou aktivnosti
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BlindfoldChessCouchTheme {
                // Prosleđujemo kreirani ViewModel našoj navigaciji
                AppNavigation(gameViewModel = gameViewModel)
            }
        }
    }
}