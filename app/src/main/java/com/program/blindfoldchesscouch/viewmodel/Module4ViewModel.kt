package com.program.blindfoldchesscouch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.program.blindfoldchesscouch.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Definišemo stanje korisničkog interfejsa za Modul 4
data class Module4UiState(
    val game: Game = Game(),
    val statusMessage: String = "Modul 4: Lov na figure. Beli igra.",
    val selectedSquare: Square? = null,
    val isGameOver: Boolean = false
)

class Module4ViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(Module4UiState())
    val uiState: StateFlow<Module4UiState> = _uiState.asStateFlow()

    init {
        setupSamplePosition()
    }

    // Funkcija za postavljanje početne pozicije zadatka
    fun setupSamplePosition() {
        viewModelScope.launch {
            val newGame = Game()
            // Primer FEN-a: Beli Top i Lovac protiv Crne Dame i Skakača
            // Kasnije ovo možemo učitavati iz fajla
            newGame.loadFen("3q4/8/8/2n5/8/2B5/8/R7 w - - 0 1")
            _uiState.update {
                it.copy(
                    game = newGame,
                    statusMessage = "Beli je na potezu. Ulovi sve crne figure!",
                    isGameOver = false,
                    selectedSquare = null
                )
            }
        }
    }

    // Logika koja se izvršava kada korisnik (beli) klikne na polje
    fun onSquareClicked(square: Square) {
        val currentState = _uiState.value
        if (currentState.isGameOver || currentState.game.currentPlayer == Color.BLACK) return

        val selected = currentState.selectedSquare
        if (selected == null) {
            // Biranje figure
            val piece = currentState.game.getCurrentBoard().getPieceAt(square)
            if (piece != null && piece.color == Color.WHITE) {
                _uiState.update { it.copy(selectedSquare = square, statusMessage = "Izabrano ${square.toAlgebraicNotation()}. Igraj na...") }
            }
        } else {
            // Pomeranje figure
            val from = selected
            val to = square
            val game = currentState.game
            val legalMoves = game.getLegalMoves()
            val intendedMove = legalMoves.find { it.from == from && it.to == to }

            if (intendedMove != null) {
                val newGame = game.copy()
                newGame.tryMakeMove(intendedMove)

                // Provera da li je igra gotova
                val blackPiecesLeft = newGame.getCurrentBoard().getAllPieces().any { it.value.color == Color.BLACK }
                if (!blackPiecesLeft) {
                    _uiState.update {
                        it.copy(
                            game = newGame,
                            selectedSquare = null,
                            statusMessage = "Pobeda! Sve crne figure su ulovljene.",
                            isGameOver = true
                        )
                    }
                    return
                }

                _uiState.update {
                    it.copy(
                        game = newGame,
                        selectedSquare = null,
                        statusMessage = "Crni razmišlja..."
                    )
                }
                playBlacksResponse()
            } else {
                _uiState.update { it.copy(selectedSquare = null, statusMessage = "Nije validan potez. Beli je ponovo na potezu.") }
            }
        }
    }

    // Privremena, nasumična AI za crnog
    private fun playBlacksResponse() {
        viewModelScope.launch {
            val game = _uiState.value.game
            val allLegalMoves = game.getLegalMoves()

            // Pravilo: Crni ne može da jede. Filtriramo sve poteze koji su ujedno i uzimanje.
            val nonCaptureMoves = allLegalMoves.filter { move ->
                game.getCurrentBoard().getPieceAt(move.to) == null
            }

            if (nonCaptureMoves.isNotEmpty()) {
                // Ako ima poteza, odigraj nasumičan
                val randomMove = nonCaptureMoves.random()
                val newGame = game.copy()
                newGame.tryMakeMove(randomMove)
                _uiState.update {
                    it.copy(
                        game = newGame,
                        statusMessage = "Beli je na potezu."
                    )
                }
            } else {
                // Ako je crni blokiran i nema poteza, preskače red
                val newGame = game.copy()
                newGame.tryMakeMove(Move(Square('a',1),Square('a',1))) // Dummy move to switch player
                _uiState.update {
                    it.copy(
                        game = newGame,
                        statusMessage = "Crni je blokiran! Beli je ponovo na potezu."
                    )
                }
            }
        }
    }
}