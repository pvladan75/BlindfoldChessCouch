// viewmodel/Module3ViewModel.kt
package com.program.blindfoldchesscouch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.program.blindfoldchesscouch.model.*
import com.program.blindfoldchesscouch.tts.TtsHelper
import com.program.blindfoldchesscouch.util.PuzzleLoader
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- UI State Definitions ---
enum class SessionState { SETUP, IN_PROGRESS, FINISHED }
enum class PuzzlePhase { MEMORIZE, AWAITING_INPUT, COMPLETED, FAILED_REVEALED }

data class TestStats(
    val mistakes: Int = 0,
    val sessionTimerMillis: Long = 0,
    val puzzlesCompleted: Int = 0,
    val failedPuzzles: Int = 0
)

data class Module3UiState(
    val sessionState: SessionState = SessionState.SETUP,
    val puzzlePhase: PuzzlePhase = PuzzlePhase.MEMORIZE,
    val pieceSelection: Map<PieceType, Int> = mapOf(
        PieceType.KNIGHT to 0, PieceType.BISHOP to 0, PieceType.ROOK to 0, PieceType.QUEEN to 0
    ),
    val isStartButtonEnabled: Boolean = false,
    val selectedPuzzleLength: Int = 10,
    val board: Board = Board(),
    val currentPuzzle: Puzzle? = null,
    val currentStepIndex: Int = 0,
    val totalStepsInPuzzle: Int = 0, // <-- AŽURIRANO
    val stats: TestStats = TestStats(),
    val moveHighlight: Move? = null,
    val feedbackSquare: Pair<Square, Boolean>? = null,
    val infoMessage: String? = null,
    val isBlindfoldMode: Boolean = false,
    val isTimerRunning: Boolean = false, // <-- AŽURIRANO
    val visiblePieceForAnimation: Square? = null,
    val forceShowPieces: Boolean = false
)

class Module3ViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(Module3UiState())
    val uiState: StateFlow<Module3UiState> = _uiState.asStateFlow()

    private val ttsHelper = TtsHelper(application)
    private var sessionPuzzles: List<Puzzle> = emptyList()
    private var timerJob: Job? = null
    private var puzzleWasFailed = false

    init {
        _uiState.value.board.clearBoard()
        ttsHelper.setSpeechSpeed(0.85f)
    }

    fun onPieceCountChange(pieceType: PieceType, count: Int) {
        _uiState.update { currentState ->
            val newSelection = currentState.pieceSelection.toMutableMap()
            newSelection[pieceType] = count
            val totalPieces = newSelection.values.sum()
            currentState.copy(
                pieceSelection = newSelection,
                isStartButtonEnabled = totalPieces >= 3
            )
        }
    }

    fun onPuzzleLengthChange(length: Int) {
        _uiState.update { it.copy(selectedPuzzleLength = length) }
    }

    fun onStartSession() {
        val puzzles = mutableSetOf<Puzzle>()
        var attempts = 0
        while (puzzles.size < 10 && attempts < 1000) {
            PuzzleLoader.findPuzzleFor(getApplication(), uiState.value.pieceSelection)?.let {
                if (it.solution.size >= uiState.value.selectedPuzzleLength) {
                    puzzles.add(it)
                }
            }
            attempts++
        }
        if (puzzles.isNotEmpty()) {
            sessionPuzzles = puzzles.toList()
            loadPuzzle(0)
        } else {
            _uiState.update { it.copy(infoMessage = "Nije pronađeno dovoljno zadataka za ovu kombinaciju.") }
        }
    }

    fun onStartPuzzleInteraction() {
        resumeTimer()
        playNextPuzzleMove()
    }

    fun onBlindfoldToggled(isBlindfold: Boolean) {
        _uiState.update { it.copy(isBlindfoldMode = isBlindfold) }
    }

    fun onShowPiecesClicked() {
        if (!puzzleWasFailed) {
            puzzleWasFailed = true
            _uiState.update { it.copy(
                stats = it.stats.copy(failedPuzzles = it.stats.failedPuzzles + 1)
            )}
        }
        _uiState.update { it.copy(
            forceShowPieces = true,
            puzzlePhase = PuzzlePhase.FAILED_REVEALED
        ) }
    }

    fun onLoadNextPuzzle() {
        val state = uiState.value
        val nextPuzzleIndex = state.stats.puzzlesCompleted + 1
        if (nextPuzzleIndex >= sessionPuzzles.size) {
            endSession()
        } else {
            loadPuzzle(nextPuzzleIndex)
        }
    }

    fun onSquareClicked(square: Square) {
        val state = uiState.value
        if (state.sessionState != SessionState.IN_PROGRESS || state.puzzlePhase != PuzzlePhase.AWAITING_INPUT) return
        val correctSquare = Square.fromAlgebraicNotation(state.currentPuzzle!!.solution[state.currentStepIndex].interactingSquareNotation)

        if (square == correctSquare) {
            viewModelScope.launch {
                _uiState.update { it.copy(feedbackSquare = Pair(square, true)) }
                delay(400)
                _uiState.update { it.copy(feedbackSquare = null, moveHighlight = null) }
                val nextStepIndex = state.currentStepIndex + 1
                if (nextStepIndex >= state.selectedPuzzleLength) {
                    pauseTimer()
                    _uiState.update { it.copy(puzzlePhase = PuzzlePhase.COMPLETED) }
                    delay(2000)
                    onLoadNextPuzzle()
                } else {
                    _uiState.update { it.copy(currentStepIndex = nextStepIndex) }
                    playNextPuzzleMove()
                }
            }
        } else {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        stats = it.stats.copy(mistakes = it.stats.mistakes + 1),
                        feedbackSquare = Pair(square, false)
                    )
                }
                delay(500)
                _uiState.update { it.copy(feedbackSquare = null) }
            }
        }
    }

    fun onDismissDialog() {
        _uiState.value = Module3UiState()
    }

    private fun loadPuzzle(puzzleIndex: Int) {
        pauseTimer()
        puzzleWasFailed = false
        val puzzle = sessionPuzzles[puzzleIndex]
        val board = Board().apply { loadFen(puzzle.initialFen) }

        _uiState.update {
            it.copy(
                sessionState = SessionState.IN_PROGRESS,
                puzzlePhase = PuzzlePhase.MEMORIZE,
                currentPuzzle = puzzle,
                board = board,
                currentStepIndex = 0,
                totalStepsInPuzzle = it.selectedPuzzleLength,
                moveHighlight = null,
                forceShowPieces = false,
                stats = it.stats.copy(puzzlesCompleted = puzzleIndex)
            )
        }
    }

    private fun playNextPuzzleMove() {
        viewModelScope.launch {
            _uiState.update { it.copy(puzzlePhase = PuzzlePhase.AWAITING_INPUT) }
            val state = uiState.value
            val puzzleStep = state.currentPuzzle!!.solution[state.currentStepIndex]
            val fromSq = Square.fromAlgebraicNotation(puzzleStep.moveNotation.substringBefore('-'))!!
            val toSq = Square.fromAlgebraicNotation(puzzleStep.moveNotation.substringAfter('-'))!!
            val piece = state.board.getPieceAt(fromSq)!!
            val move = Move(fromSq, toSq, piece)
            val moveText = " ${fromSq.toAlgebraicNotation()} to ${toSq.toAlgebraicNotation()}"
            ttsHelper.speak(moveText)
            val boardAfterMove = state.board.copy().apply { makeMove(move) }
            if (state.isBlindfoldMode && !state.forceShowPieces) {
                _uiState.update { it.copy(visiblePieceForAnimation = fromSq, moveHighlight = move) }
                delay(600)
                _uiState.update { it.copy(board = boardAfterMove, visiblePieceForAnimation = toSq) }
                delay(600)
                _uiState.update { it.copy(visiblePieceForAnimation = null) }
            } else {
                _uiState.update { it.copy(moveHighlight = move) }
                delay(1000)
                _uiState.update { it.copy(board = boardAfterMove) }
            }
        }
    }

    private fun endSession() {
        pauseTimer()
        _uiState.update { it.copy(sessionState = SessionState.FINISHED) }
    }

    private fun resumeTimer() {
        if (uiState.value.isTimerRunning) return
        _uiState.update { it.copy(isTimerRunning = true) }

        if (timerJob == null || timerJob?.isActive == false) {
            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    if (_uiState.value.isTimerRunning) {
                        _uiState.update { it.copy(stats = it.stats.copy(sessionTimerMillis = it.stats.sessionTimerMillis + 1000)) }
                    }
                }
            }
        }
    }

    private fun pauseTimer() {
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        ttsHelper.shutdown()
    }
}