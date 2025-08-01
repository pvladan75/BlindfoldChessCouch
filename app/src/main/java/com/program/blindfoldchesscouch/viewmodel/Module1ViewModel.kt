// viewmodel/Module1ViewModel.kt
package com.program.blindfoldchesscouch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.program.blindfoldchesscouch.model.*
import com.program.blindfoldchesscouch.tts.TtsHelper
import com.program.blindfoldchesscouch.util.AdvancedQuizGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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

enum class AnswerFeedbackType { CORRECT, INCORRECT, NONE }
data class AnswerFeedback(
    val type: AnswerFeedbackType,
    val correctOptions: Set<String> = emptySet(),
    val selectedOptions: Set<String> = emptySet()
)

sealed class NavigationEvent {
    object NavigateToMainMenu : NavigationEvent()
    object NavigateToModule1Setup : NavigationEvent()
}

// Ažuriran UiState da podrži SVE modove
data class Module1UiState(
    val mode: TrainingMode = TrainingMode.OBSERVATION, // Glavni selektor
    val selectedQuizType: QuizType = QuizType.FindSquare,
    val isSessionActive: Boolean = false,
    val board: Board = Board(),

    // Stanje za Observation Mode
    val isObservationRunning: Boolean = false,
    val highlightDurationSeconds: Float = 2f,
    val highlightedSquare: Square? = null,
    val taskText: String = "Pritisnite START za početak",

    // Stanje za FindSquare Quiz
    val findSquare_target: Square? = null,
    val findSquare_wrongAttempt: Square? = null,

    // Stanje za Advanced Quiz
    val advanced_currentQuestion: AdvancedQuestion? = null,
    val advanced_selectedAnswers: Set<String> = emptySet(),
    val advanced_feedback: AnswerFeedback = AnswerFeedback(AnswerFeedbackType.NONE),

    // Zajedničko stanje za kvizove
    val quizStats: QuizStats = QuizStats(),
    val showEndSessionDialog: Boolean = false
)

class Module1ViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(Module1UiState())
    val uiState: StateFlow<Module1UiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val ttsHelper = TtsHelper(application)
    private var observationJob: Job? = null
    private var quizTimerJob: Job? = null
    private var findSquare_quizQuestions: List<Square> = emptyList()
    private var advanced_quizQuestions: List<AdvancedQuestion> = emptyList()

    init {
        _uiState.value.board.clearBoard()
    }

    // --- Glavni Event-i iz UI-a ---

    fun onModeChange(newMode: TrainingMode) {
        stopAllCoroutines()
        _uiState.update { it.copy(
            mode = newMode,
            isSessionActive = false,
            isObservationRunning = false,
            taskText = "Pritisnite START za početak"
        )}
    }

    fun onStartSession() {
        stopAllCoroutines()
        _uiState.update { it.copy(isSessionActive = true, quizStats = QuizStats(taskNumber = 1), showEndSessionDialog = false) }
        when (uiState.value.selectedQuizType) {
            is QuizType.FindSquare -> startFindSquareQuiz()
            is QuizType.AdvancedTactics -> startAdvancedQuiz()
        }
    }

    // --- Logika za Observation Mode ---

    fun onToggleObservation() {
        if (uiState.value.isObservationRunning) {
            stopAllCoroutines()
            _uiState.update { it.copy(isObservationRunning = false, taskText = "Pauzirano") }
        } else {
            startObservationMode()
        }
    }

    private fun startObservationMode() {
        stopAllCoroutines()
        _uiState.update { it.copy(isObservationRunning = true, board = Board()) } // Prazna tabla
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
                _uiState.update { it.copy(highlightedSquare = null) }
                delay(500) // Kratka pauza između
            }
        }
    }

    fun onDurationChange(seconds: Float) {
        _uiState.update { it.copy(highlightDurationSeconds = seconds) }
        if (uiState.value.isObservationRunning) {
            startObservationMode() // Restartuj sa novim trajanjem
        }
    }

    // ... (ostatak koda za kvizove i navigaciju ostaje isti)
    // --- Logika za stari kviz "Prepoznavanje polja" ---

    private fun startFindSquareQuiz() {
        findSquare_quizQuestions = List(20) { getRandomSquare() }
        quizTimerJob = startTimer()
        loadNextFindSquareQuestion()
    }

    private fun loadNextFindSquareQuestion() {
        val currentTaskNum = uiState.value.quizStats.taskNumber
        if (currentTaskNum <= 20) {
            val nextSquare = findSquare_quizQuestions[currentTaskNum - 1]
            val taskText = nextSquare.toAlgebraicNotation()
            _uiState.update {
                it.copy(
                    findSquare_target = nextSquare,
                    taskText = taskText,
                    board = Board()
                )
            }
            ttsHelper.speak(taskText)
        } else {
            endQuizSession()
        }
    }

    fun onFindSquareAnswer(clickedSquare: Square) {
        if (!uiState.value.isSessionActive) return

        val currentState = uiState.value
        val correctSquare = currentState.findSquare_target
        val newTotalAttempts = currentState.quizStats.totalAttempts + 1

        if (clickedSquare == correctSquare) {
            val newCorrectAnswers = currentState.quizStats.correctAnswers + 1
            _uiState.update {
                it.copy(quizStats = it.quizStats.copy(
                    correctAnswers = newCorrectAnswers,
                    totalAttempts = newTotalAttempts,
                    taskNumber = it.quizStats.taskNumber + 1
                ))
            }
            loadNextFindSquareQuestion()
        } else {
            _uiState.update {
                it.copy(
                    quizStats = it.quizStats.copy(totalAttempts = newTotalAttempts),
                    findSquare_wrongAttempt = clickedSquare
                )
            }
            viewModelScope.launch {
                delay(300)
                _uiState.update { it.copy(findSquare_wrongAttempt = null) }
            }
        }
    }

    private fun getRandomSquare(): Square {
        val file = ('a'..'h').random()
        val rank = (1..8).random()
        return Square(file, rank)
    }

    // --- Logika za NOVI kviz ---

    fun onQuizTypeSelected(quizType: QuizType) {
        _uiState.update { it.copy(selectedQuizType = quizType) }
    }

    fun onAdvancedAnswerToggled(option: String) {
        _uiState.update {
            val newAnswers = it.advanced_selectedAnswers.toMutableSet()
            if (option in newAnswers) newAnswers.remove(option) else newAnswers.add(option)
            it.copy(advanced_selectedAnswers = newAnswers)
        }
    }

    fun onConfirmAdvancedAnswer() {
        val question = uiState.value.advanced_currentQuestion ?: return
        val selected = uiState.value.advanced_selectedAnswers
        val correct = question.correctOptions
        val isCorrect = selected == correct

        _uiState.update { it.copy(
            quizStats = it.quizStats.copy(
                correctAnswers = if (isCorrect) it.quizStats.correctAnswers + 1 else it.quizStats.correctAnswers,
                taskNumber = it.quizStats.taskNumber + 1,
                totalAttempts = it.quizStats.totalAttempts + 1
            ),
            advanced_feedback = AnswerFeedback(
                type = if (isCorrect) AnswerFeedbackType.CORRECT else AnswerFeedbackType.INCORRECT,
                correctOptions = correct,
                selectedOptions = selected
            )
        )}

        viewModelScope.launch {
            delay(2500)
            if (uiState.value.quizStats.taskNumber > 10) {
                endQuizSession()
            } else {
                loadNextAdvancedQuestion()
            }
        }
    }

    private fun startAdvancedQuiz() {
        advanced_quizQuestions = AdvancedQuizGenerator.generateQuizSession()
        quizTimerJob = startTimer()
        loadNextAdvancedQuestion()
    }

    private fun loadNextAdvancedQuestion() {
        val currentTaskNum = uiState.value.quizStats.taskNumber
        if (currentTaskNum <= 10) {
            val newQuestion = advanced_quizQuestions[currentTaskNum - 1]
            _uiState.update {
                it.copy(
                    advanced_currentQuestion = newQuestion,
                    board = newQuestion.board,
                    advanced_selectedAnswers = emptySet(),
                    advanced_feedback = AnswerFeedback(AnswerFeedbackType.NONE)
                )
            }
        }
    }

    // --- Zajednička logika ---

    private fun endQuizSession() {
        stopAllCoroutines()
        _uiState.update { it.copy(showEndSessionDialog = true, isSessionActive = false) }
    }

    private fun startTimer(): Job {
        return viewModelScope.launch {
            while(true) {
                delay(1.seconds)
                _uiState.update { it.copy(
                    quizStats = it.quizStats.copy(sessionTimeMillis = it.quizStats.sessionTimeMillis + 1000)
                )}
            }
        }
    }

    fun onEndDialogDismiss() {
        _uiState.update { it.copy(showEndSessionDialog = false, isSessionActive = false) }
    }

    fun onNavigateToMainMenu() {
        viewModelScope.launch { _navigationEvent.emit(NavigationEvent.NavigateToMainMenu) }
    }

    fun onNavigateToModule1Setup() {
        // Ne treba event, samo resetujemo stanje na početak modula
        stopAllCoroutines()
        _uiState.update { Module1UiState() }
    }

    private fun stopAllCoroutines() {
        observationJob?.cancel()
        quizTimerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}