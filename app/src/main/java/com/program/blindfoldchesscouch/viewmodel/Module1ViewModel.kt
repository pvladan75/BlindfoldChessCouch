// viewmodel/Module1ViewModel.kt
package com.program.blindfoldchesscouch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Color
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.tts.TtsHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

// --- Definicije stanja i modova ---

enum class TrainingMode { OBSERVATION, QUIZ }

data class QuizStats(
    val taskNumber: Int = 0,
    val correctAnswers: Int = 0,
    val totalAttempts: Int = 0,
    val sessionTimeMillis: Long = 0
)

data class Module1UiState(
    val mode: TrainingMode = TrainingMode.OBSERVATION,
    val board: Board = Board(),
    val boardOrientation: Color = Color.WHITE,
    val highlightedSquare: Square? = null,
    val targetSquare: Square? = null,
    val taskText: String = "Pritisnite START za početak", // Izmenjen početni tekst
    val quizStats: QuizStats = QuizStats(),
    val showEndSessionDialog: Boolean = false,
    val highlightDurationSeconds: Float = 2f,
    val wrongSquareClicked: Square? = null,
    val isObservationRunning: Boolean = false // NOVO: Pratimo da li je mod posmatranja aktivan
)

// --- ViewModel ---

class Module1ViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(Module1UiState())
    val uiState: StateFlow<Module1UiState> = _uiState.asStateFlow()

    private val ttsHelper = TtsHelper(application)
    private var observationJob: Job? = null
    private var quizTimerJob: Job? = null
    private var quizQuestions: List<Square> = emptyList()

    init {
        // Kreira praznu tablu, ali više ne pokreće mod automatski
        _uiState.value.board.clearBoard()
    }

    // --- Javne metode koje UI poziva (Event-i) ---

    fun onModeChange(newMode: TrainingMode) {
        stopAllCoroutines() // Uvek zaustavi sve kada se menja mod
        _uiState.update {
            it.copy(
                mode = newMode,
                isObservationRunning = false, // Resetuj stanje
                taskText = if (newMode == TrainingMode.OBSERVATION) "Pritisnite START za početak" else "Dobrodošli u kviz!",
                highlightedSquare = null
            )
        }
        if (newMode == TrainingMode.QUIZ) {
            startNewQuiz()
        }
    }

    /**
     * NOVO: Funkcija za Start/Stop dugme
     */
    fun onToggleObservation() {
        if (uiState.value.isObservationRunning) {
            stopAllCoroutines()
            _uiState.update { it.copy(isObservationRunning = false, taskText = "Pauzirano") }
        } else {
            startObservationMode()
        }
    }

    fun onSquareClicked(square: Square) {
        if (uiState.value.mode == TrainingMode.QUIZ && uiState.value.targetSquare != null) {
            processQuizAttempt(square)
        }
    }

    fun onFlipBoard() {
        _uiState.update { it.copy(boardOrientation = it.boardOrientation.opposite()) }
    }

    fun onDurationChange(seconds: Float) {
        _uiState.update { it.copy(highlightDurationSeconds = seconds) }
        // Ako je mod aktivan, restartuj ga sa novim trajanjem
        if (uiState.value.isObservationRunning) {
            startObservationMode()
        }
    }

    fun onDismissEndSessionDialog() {
        _uiState.update { it.copy(showEndSessionDialog = false) }
        onModeChange(TrainingMode.OBSERVATION)
    }

    // --- Logika za Modove ---

    private fun startObservationMode() {
        stopAllCoroutines()
        _uiState.update { it.copy(isObservationRunning = true) } // Postavi da je aktivan
        observationJob = viewModelScope.launch {
            while (true) {
                val randomSquare = getRandomSquare()
                _uiState.update {
                    it.copy(
                        highlightedSquare = randomSquare,
                        taskText = randomSquare.toAlgebraicNotation()
                    )
                }
                ttsHelper.speak(randomSquare.toAlgebraicNotation())
                delay((uiState.value.highlightDurationSeconds * 1000).toLong())
            }
        }
    }

    private fun startNewQuiz() {
        stopAllCoroutines()
        quizQuestions = List(20) { getRandomSquare() }
        _uiState.update {
            it.copy(
                quizStats = QuizStats(),
                highlightedSquare = null,
                showEndSessionDialog = false
            )
        }
        startQuizTimer()
        nextQuizQuestion()
    }

    private fun processQuizAttempt(clickedSquare: Square) {
        val currentState = uiState.value
        val correctSquare = currentState.targetSquare
        val newTotalAttempts = currentState.quizStats.totalAttempts + 1

        if (clickedSquare == correctSquare) {
            val newCorrectAnswers = currentState.quizStats.correctAnswers + 1
            _uiState.update {
                it.copy(quizStats = it.quizStats.copy(
                    correctAnswers = newCorrectAnswers,
                    totalAttempts = newTotalAttempts
                ))
            }
            if (currentState.quizStats.taskNumber >= 20) {
                endQuizSession()
            } else {
                nextQuizQuestion()
            }
        } else {
            _uiState.update {
                it.copy(
                    quizStats = it.quizStats.copy(totalAttempts = newTotalAttempts),
                    wrongSquareClicked = clickedSquare
                )
            }
            viewModelScope.launch {
                delay(300)
                _uiState.update { it.copy(wrongSquareClicked = null) }
            }
        }
    }

    private fun nextQuizQuestion() {
        val currentTaskNum = uiState.value.quizStats.taskNumber
        if (currentTaskNum < 20) {
            val nextSquare = quizQuestions[currentTaskNum]
            _uiState.update {
                it.copy(
                    targetSquare = nextSquare,
                    taskText = nextSquare.toAlgebraicNotation(),
                    quizStats = it.quizStats.copy(taskNumber = currentTaskNum + 1)
                )
            }
            ttsHelper.speak(nextSquare.toAlgebraicNotation())
        }
    }

    private fun endQuizSession() {
        stopAllCoroutines()
        _uiState.update { it.copy(showEndSessionDialog = true) }
    }

    private fun startQuizTimer() {
        quizTimerJob = viewModelScope.launch {
            while(true) {
                delay(1.seconds)
                _uiState.update { it.copy(
                    quizStats = it.quizStats.copy(
                        sessionTimeMillis = it.quizStats.sessionTimeMillis + 1000
                    )
                )}
            }
        }
    }

    private fun stopAllCoroutines() {
        observationJob?.cancel()
        quizTimerJob?.cancel()
    }

    private fun getRandomSquare(): Square {
        val file = ('a'..'h').random()
        val rank = (1..8).random()
        return Square(file, rank)
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}