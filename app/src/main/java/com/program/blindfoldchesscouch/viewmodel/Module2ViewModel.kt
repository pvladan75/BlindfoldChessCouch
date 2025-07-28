// in viewmodel/Module2ViewModel.kt
package com.program.blindfoldchesscouch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.program.blindfoldchesscouch.model.Board
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stanje korisničkog interfejsa za Modul 2.
 */
data class Module2UiState(
    val board: Board = Board(),
    val arePiecesVisible: Boolean = true,
    val statusMessage: String = "Pritisni START za početak"
)

/**
 * ViewModel za Modul 2 - Mat na slepo.
 */
class Module2ViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(Module2UiState())
    val uiState: StateFlow<Module2UiState> = _uiState.asStateFlow()

    init {
        loadPuzzle()
    }

    /**
     * Učitava početnu poziciju zagonetke.
     */
    private fun loadPuzzle() {
        viewModelScope.launch {
            val board = Board()
            // FEN za početnu poziciju: Dama i Kralj vs Kralj
            val fen = "8/8/1k6/8/7Q/4K3/8/8 w - - 0 1"
            board.loadFen(fen)
            _uiState.update { it.copy(board = board) }
        }
    }

    /**
     * Poziva se kada korisnik pritisne "Start".
     * Sakriva figure i priprema za unos poteza.
     */
    fun onStartClicked() {
        _uiState.update {
            it.copy(
                arePiecesVisible = false,
                statusMessage = "Beli je na potezu."
            )
        }
    }
}