// ui/screens/Module1Screen.kt
package com.program.blindfoldchesscouch.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.program.blindfoldchesscouch.model.*
import com.program.blindfoldchesscouch.model.Color as ChessColor
import com.program.blindfoldchesscouch.util.getDrawableResourceForPiece
import com.program.blindfoldchesscouch.viewmodel.*
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit

// --- Boje ---
private val lightSquareColorM1 = Color(0xFFF0D9B5)
private val darkSquareColorM1 = Color(0xFFB58863)
private val highlightColor = Color(0xB3FFEB3B)
private val wrongHighlightColor = Color(0x99D1362F)
private val feedbackCorrectColor = Color(0xAA2ECC71)
private val feedbackWrongColor = Color(0xAAE74C3C)
private val selectedAnswerColor = Color(0xFFD2B4DE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Module1Screen(
    viewModel: Module1ViewModel = viewModel(),
    onNavigateToMainMenu: () -> Unit,
    onNavigateToModule1Setup: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when(event) {
                is NavigationEvent.NavigateToMainMenu -> onNavigateToMainMenu()
                is NavigationEvent.NavigateToModule1Setup -> onNavigateToModule1Setup()
            }
        }
    }

    if (uiState.showEndSessionDialog) {
        QuizEndDialog(
            stats = uiState.quizStats,
            onDismiss = { viewModel.onEndDialogDismiss() },
            onGoToMainMenu = { viewModel.onNavigateToMainMenu() },
            onGoToModule1Setup = { viewModel.onNavigateToModule1Setup() }
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Modul 1: Vizuelizacija") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            ModeSelector(
                selectedMode = uiState.mode,
                onModeChange = { viewModel.onModeChange(it) }
            )

            when (uiState.mode) {
                TrainingMode.OBSERVATION -> ObservationView(uiState, viewModel)
                TrainingMode.QUIZ -> {
                    if (uiState.isSessionActive) {
                        TestView(uiState, viewModel)
                    } else {
                        QuizSetupView(uiState, viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelector(selectedMode: TrainingMode, onModeChange: (TrainingMode) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            onClick = { onModeChange(TrainingMode.OBSERVATION) },
            selected = selectedMode == TrainingMode.OBSERVATION
        ) { Text("Posmatranje") }
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            onClick = { onModeChange(TrainingMode.QUIZ) },
            selected = selectedMode == TrainingMode.QUIZ
        ) { Text("Kviz") }
    }
}


@Composable
private fun ObservationView(uiState: Module1UiState, viewModel: Module1ViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = uiState.taskText,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp).height(60.dp)
        )

        ClickableChessBoard(
            board = uiState.board,
            orientation = ChessColor.WHITE,
            highlightedSquare = uiState.highlightedSquare,
            onSquareClick = {}
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = { viewModel.onToggleObservation() },
                modifier = Modifier.width(200.dp)
            ) {
                Text(if (uiState.isObservationRunning) "Stop" else "Start")
            }
            Spacer(Modifier.height(16.dp))
            Text("Trajanje prikaza (sekunde): ${String.format("%.1f", uiState.highlightDurationSeconds)}")
            Slider(
                value = uiState.highlightDurationSeconds,
                onValueChange = { viewModel.onDurationChange(it) },
                valueRange = 1f..5f,
                steps = 7,
                enabled = !uiState.isObservationRunning
            )
        }
    }
}

@Composable
private fun QuizSetupView(uiState: Module1UiState, viewModel: Module1ViewModel) {
    val quizTypes = listOf(QuizType.FindSquare, QuizType.AdvancedTactics)
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top)
    ) {
        Text("Izaberite tip kviza:", style = MaterialTheme.typography.titleMedium)

        quizTypes.forEach { quizType ->
            val isSelected = uiState.selectedQuizType::class == quizType::class
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = { viewModel.onQuizTypeSelected(quizType) },
                        role = Role.RadioButton
                    )
                    .border(
                        width = 2.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = MaterialTheme.shapes.medium
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(quizType.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(quizType.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { viewModel.onStartSession() },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Započni", fontSize = 18.sp)
        }
    }
}

@Composable
private fun TestView(uiState: Module1UiState, viewModel: Module1ViewModel) {
    val questionText = when (uiState.selectedQuizType) {
        is QuizType.FindSquare -> uiState.findSquare_target?.toAlgebraicNotation() ?: "Učitavanje..."
        is QuizType.AdvancedTactics -> uiState.advanced_currentQuestion?.questionText ?: "Učitavanje..."
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        QuizStatsView(stats = uiState.quizStats, quizType = uiState.selectedQuizType)

        Text(
            text = questionText,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.heightIn(min = 70.dp).padding(top = 8.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (uiState.selectedQuizType) {
                is QuizType.FindSquare -> {
                    ClickableChessBoard(
                        board = uiState.board,
                        orientation = ChessColor.WHITE,
                        wrongSquare = uiState.findSquare_wrongAttempt,
                        onSquareClick = { viewModel.onFindSquareAnswer(it) }
                    )
                }
                is QuizType.AdvancedTactics -> {
                    uiState.advanced_currentQuestion?.let { question ->
                        ChessBoard(board = question.board)
                        Spacer(Modifier.height(16.dp))
                        AdvancedQuizOptions(
                            question = question,
                            selectedAnswers = uiState.advanced_selectedAnswers,
                            feedback = uiState.advanced_feedback,
                            onAnswerToggled = { viewModel.onAdvancedAnswerToggled(it) }
                        )
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { viewModel.onConfirmAdvancedAnswer() },
                            enabled = uiState.advanced_feedback.type == AnswerFeedbackType.NONE,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp)
                        ) {
                            Text("Potvrdi odgovor")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedQuizOptions(
    question: AdvancedQuestion,
    selectedAnswers: Set<String>,
    feedback: AnswerFeedback,
    onAnswerToggled: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(question.options) { option ->
            val isSelected = option in selectedAnswers
            val feedbackType = when {
                feedback.type == AnswerFeedbackType.NONE -> null
                option in feedback.correctOptions -> true
                option in selectedAnswers && option !in feedback.correctOptions -> false
                else -> null
            }

            val cardColors = when (feedbackType) {
                true -> CardDefaults.cardColors(containerColor = feedbackCorrectColor)
                false -> CardDefaults.cardColors(containerColor = feedbackWrongColor)
                null -> CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            }

            Card(
                modifier = Modifier.aspectRatio(3.5f).clickable(
                    enabled = feedback.type == AnswerFeedbackType.NONE,
                    onClick = { onAnswerToggled(option) }
                ),
                colors = cardColors,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    Text(option, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun QuizStatsView(stats: QuizStats, quizType: QuizType) {
    val totalTasks = if (quizType is QuizType.FindSquare) 20 else 10
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
        Text("Zadatak: ${stats.taskNumber}/$totalTasks")
        Text("Tačnih: ${stats.correctAnswers}")
        val time = String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(stats.sessionTimeMillis),
            TimeUnit.MILLISECONDS.toSeconds(stats.sessionTimeMillis) % 60
        )
        Text("Vreme: $time")
    }
}

@Composable
fun ClickableChessBoard(
    board: Board,
    orientation: ChessColor,
    onSquareClick: (Square) -> Unit,
    highlightedSquare: Square? = null,
    wrongSquare: Square? = null,
) {
    val ranks = if (orientation == ChessColor.WHITE) (8 downTo 1) else (1..8)
    val files = if (orientation == ChessColor.WHITE) ('a'..'h') else ('h' downTo 'a')

    Column(modifier = Modifier.border(2.dp, Color.Black).aspectRatio(1f)) {
        for (rank in ranks) {
            Row(Modifier.weight(1f)) {
                for (file in files) {
                    val square = Square(file, rank)
                    val baseColor = if ((file - 'a' + rank) % 2 == 0) lightSquareColorM1 else darkSquareColorM1
                    val finalColor = when (square) {
                        wrongSquare -> wrongHighlightColor
                        highlightedSquare -> highlightColor
                        else -> baseColor
                    }
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f).background(finalColor).clickable { onSquareClick(square) },
                        contentAlignment = Alignment.Center
                    ) {
                        board.getPieceAt(square)?.let { piece ->
                            Image(
                                painter = painterResource(id = getDrawableResourceForPiece(piece = piece)),
                                contentDescription = piece.toString(),
                                modifier = Modifier.fillMaxSize().padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// *** ИЗМЕНА: Комплетно реструктуриран дијалог ***
@Composable
fun QuizEndDialog(
    stats: QuizStats,
    onDismiss: () -> Unit,
    onGoToMainMenu: () -> Unit,
    onGoToModule1Setup: () -> Unit
) {
    val totalTasks = if (stats.taskNumber > 10) 20 else 10
    AlertDialog(
        onDismissRequest = onDismiss,
        // Pomeramo sve unutar `text` propertija da bismo imali punu kontrolu
        title = { Text(text = "Kraj Sesije!", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tačnih odgovora: ${stats.correctAnswers}/${totalTasks}")
                Spacer(Modifier.height(4.dp))
                Text("Ukupno pokušaja: ${stats.totalAttempts}")
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                val time = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(stats.sessionTimeMillis),
                    TimeUnit.MILLISECONDS.toSeconds(stats.sessionTimeMillis) % 60
                )
                Text("Finalno vreme: $time", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(24.dp))

                // Sada su dugmići deo glavnog sadržaja i biće ispravno raspoređeni
                Button(onClick = onDismiss, modifier=Modifier.fillMaxWidth()) {
                    Text("Igraj ponovo (OK)")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onGoToModule1Setup, modifier=Modifier.fillMaxWidth()) {
                    Text("Meni Modula 1")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onGoToMainMenu, modifier=Modifier.fillMaxWidth()) {
                    Text("Glavni meni")
                }
            }
        },
        // Ove slotove ostavljamo prazne
        confirmButton = {},
        dismissButton = {}
    )
}