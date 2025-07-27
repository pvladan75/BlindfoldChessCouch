// viewmodel/Module3ViewModel.kt
package com.program.blindfoldchesscouch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Move
import com.program.blindfoldchesscouch.model.Piece
import com.program.blindfoldchesscouch.model.PieceType
import com.program.blindfoldchesscouch.model.Puzzle
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.util.PuzzleLoader
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- Definicije stanja za UI ---

// Enum koji prati u kojoj je fazi test
enum class TestState {
    SETUP, // Korisnik bira figure i podešavanja
    IN_PROGRESS, // Test je u toku
    FINISHED // Test je završen, prikazuje se dijalog
}

// Data klasa za čuvanje statistike
data class TestStats(
    val mistakes: Int = 0,
    val timerMillis: Long = 0
)

// Data klasa koja drži sve informacije potrebne za iscrtavanje ekrana
data class Module3UiState(
    val testState: TestState = TestState.SETUP,
    val pieceSelection: Map<PieceType, Int> = mapOf(
        PieceType.KNIGHT to 0, PieceType.BISHOP to 0, PieceType.ROOK to 0, PieceType.QUEEN to 0
    ),
    val isStartButtonEnabled: Boolean = false,
    val selectedPuzzleLength: Int = 10,
    val board: Board = Board(),
    val currentPuzzle: Puzzle? = null,
    val currentStepIndex: Int = 0,
    val stats: TestStats = TestStats(),
    val moveHighlight: Move? = null, // Za animaciju poteza programa
    val feedbackSquare: Pair<Square, Boolean>? = null, // Za feedback na klik korisnika (polje, da li je tačno)
    val infoMessage: String? = null // Za poruke korisniku (npr. "Nema zadatka")
)

// --- ViewModel ---

class Module3ViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(Module3UiState())
    val uiState: StateFlow<Module3UiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    // --- Javne funkcije koje poziva UI (Eventi) ---

    /** Poziva se kada korisnik promeni broj neke figure. */
    fun onPieceCountChange(pieceType: PieceType, count: Int) {
        _uiState.update { currentState ->
            val newSelection = currentState.pieceSelection.toMutableMap()
            newSelection[pieceType] = count
            val totalPieces = newSelection.values.sum()
            currentState.copy(
                pieceSelection = newSelection,
                isStartButtonEnabled = totalPieces >= 2
            )
        }
    }

    /** Poziva se kada korisnik izabere dužinu testa. */
    fun onPuzzleLengthChange(length: Int) {
        _uiState.update { it.copy(selectedPuzzleLength = length) }
    }

    /** Poziva se kada korisnik pritisne "Start". */
    fun onStartTest() {
        viewModelScope.launch {
            val puzzle = PuzzleLoader.findPuzzleFor(getApplication(), uiState.value.pieceSelection)

            if (puzzle != null && puzzle.solution.size >= uiState.value.selectedPuzzleLength) {
                val board = Board()
                board.loadFen(puzzle.initialFen)

                _uiState.update {
                    it.copy(
                        testState = TestState.IN_PROGRESS,
                        currentPuzzle = puzzle,
                        board = board,
                        currentStepIndex = 0,
                        stats = TestStats(),
                        infoMessage = null
                    )
                }
                startTimer()
                playNextPuzzleMove()
            } else {
                // Nije pronađen odgovarajući zadatak
                _uiState.update { it.copy(infoMessage = "Nije pronađen zadatak za izabranu kombinaciju.") }
            }
        }
    }

    /** Poziva se kada korisnik klikne na polje na tabli. */
    fun onSquareClicked(square: Square) {
        val state = uiState.value
        // Reagujemo na klik samo ako je test u toku i čekamo odgovor
        if (state.testState != TestState.IN_PROGRESS || state.moveHighlight != null) return

        val correctSquare = Square.fromAlgebraicNotation(state.currentPuzzle!!.solution[state.currentStepIndex].interactingSquareNotation)

        if (square == correctSquare) {
            // Tačan odgovor
            viewModelScope.launch {
                _uiState.update { it.copy(feedbackSquare = Pair(square, true)) }
                delay(500) // Pokaži zeleni feedback na pola sekunde
                _uiState.update { it.copy(feedbackSquare = null) }

                val nextStepIndex = state.currentStepIndex + 1
                if (nextStepIndex >= state.selectedPuzzleLength) {
                    endTest()
                } else {
                    _uiState.update { it.copy(currentStepIndex = nextStepIndex) }
                    playNextPuzzleMove()
                }
            }
        } else {
            // Pogrešan odgovor
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        stats = it.stats.copy(mistakes = it.stats.mistakes + 1),
                        feedbackSquare = Pair(square, false)
                    )
                }
                delay(500) // Pokaži crveni feedback
                _uiState.update { it.copy(feedbackSquare = null) }
            }
        }
    }

    /** Poziva se za zatvaranje dijaloga na kraju testa. */
    fun onDismissDialog() {
        _uiState.update { it.copy(
            testState = TestState.SETUP,
            currentPuzzle = null,
            stats = TestStats()
        )}
    }

    // --- Privatna logika za tok testa ---

    private fun playNextPuzzleMove() {
        viewModelScope.launch {
            val state = uiState.value
            val puzzleStep = state.currentPuzzle!!.solution[state.currentStepIndex]

            // Parsiraj potez iz "e2-e4" formata u Move objekat
            val fromSquare = Square.fromAlgebraicNotation(puzzleStep.moveNotation.substringBefore('-'))!!
            val toSquare = Square.fromAlgebraicNotation(puzzleStep.moveNotation.substringAfter('-'))!!
            val pieceOnFrom = state.board.getPieceAt(fromSquare)!!
            val move = Move(fromSquare, toSquare, pieceOnFrom)

            // Prikazi animaciju
            _uiState.update { it.copy(moveHighlight = move) }
            delay(1000) // Potez je vidljiv 1 sekundu

            // Izvrši potez na internoj tabli
            val newBoard = state.board.apply { makeMove(move) }

            _uiState.update { it.copy(board = newBoard, moveHighlight = null) }
            // Sada čekamo da korisnik klikne
        }
    }

    private fun endTest() {
        stopTimer()
        _uiState.update { it.copy(testState = TestState.FINISHED) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(stats = it.stats.copy(timerMillis = it.stats.timerMillis + 1000)) }
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