// MainActivity.kt
package com.program.blindfoldchesscouch

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.program.blindfoldchesscouch.navigation.AppNavigation
import com.program.blindfoldchesscouch.ui.theme.BlindfoldChessCouchTheme
import com.program.blindfoldchesscouch.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // installSplashScreen() је и даље потребан да би систем знао
        // када да пребаци са Splash теме на главну тему апликације.
        installSplashScreen()

        setContent {
            BlindfoldChessCouchTheme {
                AppNavigation(gameViewModel = gameViewModel)
            }
        }

        // Ову функцију остављамо за будућу имплементацију нотификација
        trackUserActivity()
    }

    private fun trackUserActivity() {
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putLong("last_practice_timestamp", System.currentTimeMillis())
            apply()
        }
    }
}