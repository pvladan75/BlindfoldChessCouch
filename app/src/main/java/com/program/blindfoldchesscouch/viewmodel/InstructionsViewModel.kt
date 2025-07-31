package com.program.blindfoldchesscouch.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.program.blindfoldchesscouch.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Definicija UiState ostaje ista
data class InstructionsUiState(
    val currentStep: TutorialStep?, // Može biti null ako scenario ne postoji
    val board: Board = Board(),
    val displayedText: String = "",
    val isProcessing: Boolean = true,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = true,
    val highlightedSquares: Set<Square> = emptySet()
)

class InstructionsViewModel(
    savedStateHandle: SavedStateHandle // Za prihvatanje argumenata iz navigacije
) : ViewModel() {

    // Čitamo topicId koji je prosleđen kroz navigaciju
    private val topicId: String? = savedStateHandle["topicId"]

    // Učitavamo pravi scenario na osnovu dobijenog ID-ja
    private val tutorialScript: List<TutorialStep> = TutorialRepository.getScriptById(topicId)

    private val _uiState = MutableStateFlow(
        InstructionsUiState(currentStep = tutorialScript.firstOrNull())
    )
    val uiState: StateFlow<InstructionsUiState> = _uiState.asStateFlow()

    private var currentStepIndex = 0
    private var processorJob: Job? = null

    init {
        // Učitavamo prvi korak ako scenario postoji
        if (tutorialScript.isNotEmpty()) {
            loadStep(currentStepIndex)
        } else {
            // Slučaj ako scenario ne postoji (npr. prazna lista za Modul 1)
            _uiState.update { it.copy(isProcessing = false, displayedText = "Ovaj tutorijal još uvek nije dostupan.") }
        }
    }

    fun onNextClicked() {
        if (currentStepIndex < tutorialScript.lastIndex) {
            currentStepIndex++
            loadStep(currentStepIndex)
        }
    }

    fun onBackClicked() {
        if (currentStepIndex > 0) {
            currentStepIndex--
            loadStep(currentStepIndex)
        }
    }

    private fun loadStep(index: Int) {
        processorJob?.cancel()
        val step = tutorialScript[index]
        val game = Game()
        game.loadFen(step.fen)

        _uiState.update {
            it.copy(
                currentStep = step,
                board = game.getCurrentBoard(),
                isProcessing = true,
                displayedText = "",
                canGoBack = index > 0,
                canGoForward = index < tutorialScript.lastIndex,
                highlightedSquares = emptySet()
            )
        }
    }

    private fun parseText(text: String): List<TutorialSegment> {
        val segments = mutableListOf<TutorialSegment>()
        val regex = """<(\w+)>([^<]*)</\1>""".toRegex()
        var lastIndex = 0

        regex.findAll(text).forEach { matchResult ->
            if (matchResult.range.first > lastIndex) {
                segments.add(TextSegment(text.substring(lastIndex, matchResult.range.first)))
            }

            val tagName = matchResult.groupValues[1]
            val tagContent = matchResult.groupValues[2]

            when (tagName) {
                "hl" -> {
                    segments.add(TextSegment(tagContent))
                    Square.fromAlgebraicNotation(tagContent)?.let {
                        segments.add(HighlightSegment(it))
                    }
                }
                "pause" -> tagContent.toLongOrNull()?.let {
                    segments.add(PauseSegment(it))
                }
                "clear" -> segments.add(ClearHighlightSegment)
            }
            lastIndex = matchResult.range.last + 1
        }

        if (lastIndex < text.length) {
            segments.add(TextSegment(text.substring(lastIndex)))
        }
        return segments
    }

    fun processTutorialStep(text: String) {
        processorJob?.cancel()
        val segments = parseText(text)

        processorJob = viewModelScope.launch {
            _uiState.update { it.copy(displayedText = "", highlightedSquares = emptySet()) }

            for (segment in segments) {
                when (segment) {
                    is TextSegment -> {
                        segment.text.forEach { char ->
                            _uiState.update { it.copy(displayedText = it.displayedText + char) }
                            delay(40)
                        }
                    }
                    is HighlightSegment -> {
                        _uiState.update {
                            it.copy(highlightedSquares = it.highlightedSquares + segment.square)
                        }
                    }
                    is ClearHighlightSegment -> {
                        _uiState.update { it.copy(highlightedSquares = emptySet()) }
                    }
                    is PauseSegment -> {
                        delay(segment.durationMillis)
                    }
                }
            }
            _uiState.update { it.copy(isProcessing = false) }
        }
    }
}