// in viewmodel/Module2ViewModel.kt
package com.program.blindfoldchesscouch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.program.blindfoldchesscouch.model.Game
import com.program.blindfoldchesscouch.model.Move
import com.program.blindfoldchesscouch.model.Square
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Module2UiState(
    val game: Game = Game(),
    val arePiecesVisible: Boolean = true,
    val statusMessage: String = "Pritisni START za početak",
    val selectedSquare: Square? = null,
    val isGameOver: Boolean = false,
    val puzzleTimerMillis: Long = 0L,
    val lastMove: Move? = null,
    val currentPuzzleIndex: Int = 0, // <-- NOVO: Pratimo na kojoj smo zagonetki
    val totalPuzzles: Int = 0 // <-- NOVO: Znamo ukupan broj
)

class Module2ViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(Module2UiState())
    val uiState: StateFlow<Module2UiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    // NOVO: Lista FEN stringova za sve zagonetke
    private val puzzleFenList = listOf(
        "8/8/1k6/8/7Q/4K3/8/8 w - - 0 1", // Prva pozicija
        "5R2/8/8/8/2k5/6K1/8/8 w - - 0 1"  // Druga pozicija koju si predložio
    )

    init {
        _uiState.update { it.copy(totalPuzzles = puzzleFenList.size) }
        loadPuzzle(0)
    }

    private fun loadPuzzle(index: Int) {
        stopTimer()
        viewModelScope.launch {
            val newGame = Game()
            val fen = puzzleFenList[index]
            newGame.getCurrentBoard().loadFen(fen)
            // Resetujemo stanje za novu zagonetku
            _uiState.update {
                Module2UiState(
                    game = newGame,
                    currentPuzzleIndex = index,
                    totalPuzzles = puzzleFenList.size
                )
            }
        }
    }

    // --- NOVE FUNKCIJE ---
    fun onNextPositionClicked() {
        val nextIndex = (uiState.value.currentPuzzleIndex + 1) % puzzleFenList.size // Vraća na početak
        loadPuzzle(nextIndex)
    }

    fun onToggleVisibilityClicked() {
        if (uiState.value.isGameOver || uiState.value.statusMessage == "Pritisni START za početak") return
        _uiState.update { it.copy(arePiecesVisible = !it.arePiecesVisible) }
    }
    // --- KRAJ NOVIH FUNKCIJA ---


    fun onStartClicked() {
        _uiState.update {
            it.copy(
                arePiecesVisible = false,
                statusMessage = "Beli je na potezu."
            )
        }
        startTimer()
    }

    fun onSquareClicked(square: Square) {
        val currentState = _uiState.value
        if (currentState.arePiecesVisible || currentState.isGameOver) return

        val selected = currentState.selectedSquare

        if (selected == null) {
            val piece = currentState.game.getCurrentBoard().getPieceAt(square)
            if (piece != null && piece.color == currentState.game.currentPlayer) {
                _uiState.update { it.copy(selectedSquare = square, statusMessage = "Izabrano ${square.toAlgebraicNotation()}. Igraj na...") }
            }
        } else {
            val from = selected
            val to = square
            val legalMoves = currentState.game.getLegalMoves()
            val intendedMove = legalMoves.find { it.from == from && it.to == to }

            if (intendedMove != null) {
                currentState.game.tryMakeMove(intendedMove)

                if (currentState.game.getLegalMoves().isEmpty()) {
                    stopTimer()
                    val message = if (currentState.game.isKingInCheck(currentState.game.currentPlayer)) "Mat! Čestitamo!" else "Pat! Nerešeno."
                    _uiState.update { it.copy(selectedSquare = null, statusMessage = message, isGameOver = true, lastMove = intendedMove) }
                    return
                }

                _uiState.update {
                    it.copy(
                        selectedSquare = null,
                        statusMessage = "Crni razmišlja...",
                        lastMove = intendedMove
                    )
                }
                playBlacksResponse()
            } else {
                _uiState.update { it.copy(selectedSquare = null, statusMessage = "Nije validan potez. Beli je ponovo na potezu.") }
            }
        }
    }

    private fun playBlacksResponse() {
        viewModelScope.launch {
            delay(1000)
            val currentState = _uiState.value
            val blackLegalMoves = currentState.game.getLegalMoves()

            if (blackLegalMoves.isNotEmpty()) {
                val blackMove = blackLegalMoves.random()
                currentState.game.tryMakeMove(blackMove)

                if (currentState.game.getLegalMoves().isEmpty()) {
                    stopTimer()
                    val message = if (currentState.game.isKingInCheck(currentState.game.currentPlayer)) "Mat! Crni je pobedio." else "Pat! Nerešeno."
                    _uiState.update { it.copy(statusMessage = message, isGameOver = true, lastMove = blackMove) }
                } else {
                    _uiState.update { it.copy(statusMessage = "Beli je na potezu.", lastMove = blackMove) }
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(puzzleTimerMillis = 0L) } // Resetuj tajmer na 0
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(puzzleTimerMillis = it.puzzleTimerMillis + 1000) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}