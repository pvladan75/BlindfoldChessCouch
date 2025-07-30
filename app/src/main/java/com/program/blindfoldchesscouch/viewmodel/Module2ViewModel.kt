// viewmodel/Module2ViewModel.kt
package com.program.blindfoldchesscouch.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.program.blindfoldchesscouch.engine.SunfishEngine
import com.program.blindfoldchesscouch.model.*
import com.program.blindfoldchesscouch.tts.TtsHelper
import com.program.blindfoldchesscouch.util.Module2PuzzleLoader
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class Module2SessionState { SETUP, IN_PROGRESS }

data class Module2UiState(
    val sessionState: Module2SessionState = Module2SessionState.SETUP,
    val selectedDifficulty: String = "easy",
    val currentPuzzles: List<Module2Puzzle> = emptyList(),
    val game: Game = Game(),
    val arePiecesVisible: Boolean = true,
    val statusMessage: String = "Izaberi težinu i pritisni START",
    val selectedSquare: Square? = null,
    val isGameOver: Boolean = false,
    val puzzleTimerMillis: Long = 0L,
    val lastMove: Move? = null,
    val currentPuzzleIndex: Int = 0,
    val totalPuzzles: Int = 0,
    val isReviewMode: Boolean = false,
    val reviewBoard: Board = Board(),
    val reviewMoveIndex: Int = -1,
    val fullMoveHistory: List<Move> = emptyList()
)

class Module2ViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(Module2UiState())
    val uiState: StateFlow<Module2UiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private val ttsHelper = TtsHelper(application)
    private val sunfishEngine = SunfishEngine()

    private fun loadPuzzle(index: Int) {
        stopTimer()
        val currentState = uiState.value
        if (index >= currentState.currentPuzzles.size) return

        viewModelScope.launch {
            val newGame = Game()
            val fen = currentState.currentPuzzles[index].fen
            newGame.loadFen(fen)
            _uiState.update {
                it.copy(
                    game = newGame,
                    currentPuzzleIndex = index,
                    statusMessage = "Pritisni START za početak",
                    arePiecesVisible = true,
                    isGameOver = false,
                    isReviewMode = false,
                    lastMove = null,
                    puzzleTimerMillis = 0L,
                    selectedSquare = null
                )
            }
        }
    }

    fun onDifficultySelected(difficulty: String) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun onStartSession() {
        val puzzles = Module2PuzzleLoader.loadPuzzles(getApplication(), uiState.value.selectedDifficulty)
        if (puzzles.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    sessionState = Module2SessionState.IN_PROGRESS,
                    currentPuzzles = puzzles.shuffled(),
                    totalPuzzles = puzzles.size
                )
            }
            loadPuzzle(0)
        } else {
            _uiState.update { it.copy(statusMessage = "Greška: Fajl '${uiState.value.selectedDifficulty}_puzzles.json' nije pronađen.") }
        }
    }

    fun onNextPositionClicked() {
        val nextIndex = (uiState.value.currentPuzzleIndex + 1) % uiState.value.totalPuzzles
        loadPuzzle(nextIndex)
    }

    fun onStartClicked() {
        _uiState.update {
            it.copy(
                arePiecesVisible = false,
                statusMessage = if (it.game.currentPlayer == Color.WHITE) "Beli je na potezu." else "Crni je na potezu."
            )
        }
        startTimer()
    }

    fun onSquareClicked(square: Square) {
        if (uiState.value.isReviewMode) return
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
            val game = currentState.game
            val legalMoves = game.getLegalMoves()
            val intendedMove = legalMoves.find { it.from == from && it.to == to }

            if (intendedMove != null) {
                val newGame = game.copy()
                newGame.tryMakeMove(intendedMove)
                ttsHelper.speak("${intendedMove.from.toAlgebraicNotation()} ${intendedMove.to.toAlgebraicNotation()}")

                if (newGame.getLegalMoves().isEmpty()) {
                    stopTimer()
                    val message = if (newGame.isKingInCheck(newGame.currentPlayer)) "Mat! Čestitamo!" else "Pat! Nerešeno."
                    ttsHelper.speak(message)
                    _uiState.update { it.copy(
                        game = newGame,
                        selectedSquare = null,
                        statusMessage = message,
                        isGameOver = true,
                        lastMove = intendedMove,
                        arePiecesVisible = true
                    )}
                    return
                }

                _uiState.update { it.copy(
                    game = newGame,
                    selectedSquare = null,
                    statusMessage = "Crni razmišlja...",
                    lastMove = intendedMove
                )}
                playBlacksResponse()

            } else {
                _uiState.update { it.copy(selectedSquare = null, statusMessage = "Nije validan potez. Beli je ponovo na potezu.") }
            }
        }
    }

    private fun playBlacksResponse() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.update { it.copy(statusMessage = "Crni razmišlja...") }

            val fenForEngine = currentState.game.toFen()
            Log.d("ViewModelDebug", "-----------------------------------------")
            Log.d("ViewModelDebug", "FEN poslat engine-u: $fenForEngine")
            Log.d("ViewModelDebug", "Legalni potezi za crnog PRE engine-a (${currentState.game.getLegalMoves().size}): ${currentState.game.getLegalMoves().joinToString { it.toAlgebraicNotation() }}")

            val bestMoveString = withContext(Dispatchers.Default) {
                sunfishEngine.setPositionFromFen(fenForEngine)
                sunfishEngine.searchBestMove(1.0)
            }

            Log.d("ViewModelDebug", "Engine je vratio potez: $bestMoveString")

            if (bestMoveString != null && bestMoveString.length >= 4) {
                val fromSquare = Square.fromAlgebraicNotation(bestMoveString.substring(0, 2))
                val toSquare = Square.fromAlgebraicNotation(bestMoveString.substring(2, 4))

                if (fromSquare != null && toSquare != null) {
                    val gameBeforeBlacksMove = _uiState.value.game
                    val blackMove = gameBeforeBlacksMove.getLegalMoves().find { it.from == fromSquare && it.to == toSquare }

                    if (blackMove != null) {
                        val gameAfterBlacksMove = gameBeforeBlacksMove.copy()
                        gameAfterBlacksMove.tryMakeMove(blackMove)
                        ttsHelper.speak("${blackMove.from.toAlgebraicNotation()} ${blackMove.to.toAlgebraicNotation()}")

                        if (gameAfterBlacksMove.getLegalMoves().isEmpty()) {
                            stopTimer()
                            val message = if (gameAfterBlacksMove.isKingInCheck(gameAfterBlacksMove.currentPlayer)) "Mat! Crni je pobedio." else "Pat! Nerešeno."
                            ttsHelper.speak(message)
                            _uiState.update { it.copy(
                                game = gameAfterBlacksMove,
                                statusMessage = message,
                                isGameOver = true,
                                lastMove = blackMove,
                                arePiecesVisible = true
                            )}
                        } else {
                            _uiState.update { it.copy(
                                game = gameAfterBlacksMove,
                                statusMessage = "Beli je na potezu.",
                                lastMove = blackMove
                            )}
                        }
                    } else {
                        Log.e("ViewModelDebug", "!!! NELEGALAN POTEZ !!! Engine je vratio '$bestMoveString', ali nije pronađen u listi legalnih poteza.")
                        _uiState.update { it.copy(statusMessage = "Engine je vratio nelegalan potez: $bestMoveString", arePiecesVisible = true) }
                    }
                } else {
                    Log.e("ViewModelDebug", "Engine je vratio neispravan string za potez: '$bestMoveString'")
                    _uiState.update { it.copy(statusMessage = "Greška u parsiranju poteza engine-a.", arePiecesVisible = true) }
                }
            } else {
                // *** ИЗМЕНА ЈЕ ОВДЕ ***
                // Ова грана се извршава када енџин врати `null`.
                Log.e("ViewModelDebug", "Engine je vratio NULL. Provera za mat/pat.")

                val game = _uiState.value.game
                // Проверавамо стање игре користећи поуздани генератор потеза из Game модела.
                if (game.getLegalMoves().isEmpty()) {
                    stopTimer()
                    // Проверавамо да ли је тренутни играч (црни) у шаху.
                    val message = if (game.isKingInCheck(game.currentPlayer)) {
                        "Mat! Beli je pobedio."
                    } else {
                        "Pat! Nerešeno."
                    }
                    ttsHelper.speak(message)
                    _uiState.update { it.copy(
                        statusMessage = message,
                        isGameOver = true,
                        arePiecesVisible = true
                    )}
                } else {
                    // Овај случај не би требало да се деси, али је ту за сваки случај.
                    Log.e("ViewModelDebug", "Kritična greška: Engine nema poteza, ali ViewModel pronalazi legalne poteze!")
                    _uiState.update { it.copy(statusMessage = "Greška u sinhronizaciji sa engine-om.", arePiecesVisible = true) }
                }
            }
        }
    }

    fun onEnterReviewMode() { val history = uiState.value.game.getMoveHistory(); if (history.isEmpty()) return; _uiState.update { it.copy(isReviewMode = true, fullMoveHistory = history, arePiecesVisible = true) }; goToMoveInReview(history.lastIndex) }
    fun onExitReviewMode() { _uiState.update { it.copy(isReviewMode = false, arePiecesVisible = it.isGameOver) } }
    fun onNextMoveInReview() { val currentIndex = uiState.value.reviewMoveIndex; if (currentIndex < uiState.value.fullMoveHistory.lastIndex) { goToMoveInReview(currentIndex + 1) } }
    fun onPreviousMoveInReview() { val currentIndex = uiState.value.reviewMoveIndex; if (currentIndex > -1) { goToMoveInReview(currentIndex - 1) } }
    fun onGoToStartOfReview() { goToMoveInReview(-1) }
    fun onGoToEndOfReview() { val lastIndex = uiState.value.fullMoveHistory.lastIndex; goToMoveInReview(lastIndex) }
    private fun goToMoveInReview(index: Int) { val initialFen = uiState.value.currentPuzzles[uiState.value.currentPuzzleIndex].fen; val history = uiState.value.fullMoveHistory; val tempGame = Game(); tempGame.loadFen(initialFen); for (i in 0..index) { tempGame.tryMakeMove(history[i]) }; _uiState.update { it.copy(reviewBoard = tempGame.getCurrentBoard(), reviewMoveIndex = index) } }
    fun onToggleVisibilityClicked() { if (uiState.value.isGameOver || uiState.value.statusMessage == "Pritisni START za početak") return; _uiState.update { it.copy(arePiecesVisible = !it.arePiecesVisible) } }
    private fun startTimer() { timerJob?.cancel(); _uiState.update { it.copy(puzzleTimerMillis = 0L) }; timerJob = viewModelScope.launch { while (true) { delay(1000); _uiState.update { it.copy(puzzleTimerMillis = it.puzzleTimerMillis + 1000) } } } }
    private fun stopTimer() { timerJob?.cancel() }
    override fun onCleared() { super.onCleared(); stopTimer(); ttsHelper.shutdown() }
}