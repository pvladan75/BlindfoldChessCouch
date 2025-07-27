// viewmodel/GameViewModel.kt
package com.program.blindfoldchesscouch.viewmodel

import androidx.lifecycle.ViewModel
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel koji upravlja stanjem igre.
 */
class GameViewModel : ViewModel() {

    // Kreiramo instancu igre. Njen `init` blok automatski poziva `resetGame()`,
    // tako da je tabla odmah popunjena figurama.
    private val _game = Game()

    // Odmah uzimamo popunjenu tablu i postavljamo je kao početno stanje.
    private val _boardState = MutableStateFlow(_game.getCurrentBoard())
    val boardState: StateFlow<Board> = _boardState.asStateFlow()

    // `init` blok nam više nije potreban ovde, jer se sve rešava
    // prilikom inicijalizacije property-ja iznad.
}