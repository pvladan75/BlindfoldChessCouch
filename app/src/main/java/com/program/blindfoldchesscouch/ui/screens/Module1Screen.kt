// ui/screens/Module1Screen.kt
package com.program.blindfoldchesscouch.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.util.getDrawableResourceForPiece
import com.program.blindfoldchesscouch.viewmodel.Module1UiState
import com.program.blindfoldchesscouch.viewmodel.Module1ViewModel
import com.program.blindfoldchesscouch.viewmodel.TrainingMode
import java.util.concurrent.TimeUnit

// --- BOJE ---
private val lightSquareColor = Color(0xFFF0D9B5)
private val darkSquareColor = Color(0xFFB58863)
private val highlightColor = Color(0xB3FFEB3B) // Jarko žuta, uočljivija
private val wrongHighlightColor = Color(0x99D1362F)


@Composable
fun Module1Screen(viewModel: Module1ViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showEndSessionDialog) {
        QuizEndDialog(
            stats = uiState.quizStats,
            onDismiss = { viewModel.onDismissEndSessionDialog() }
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopInfoPanel(uiState)

            ClickableChessBoard(
                board = uiState.board,
                orientation = uiState.boardOrientation,
                highlightedSquare = uiState.highlightedSquare,
                wrongSquare = uiState.wrongSquareClicked,
                onSquareClick = { square -> viewModel.onSquareClicked(square) }
            )

            BottomControlPanel(uiState, viewModel)
        }
    }
}

@Composable
private fun BottomControlPanel(uiState: Module1UiState, viewModel: Module1ViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {

        SegmentedButtonRow(
            selectedMode = uiState.mode,
            onModeChange = { viewModel.onModeChange(it) }
        )

        Spacer(Modifier.height(16.dp))

        if (uiState.mode == TrainingMode.OBSERVATION) {
            // NOVO: Start/Stop dugme
            Button(
                onClick = { viewModel.onToggleObservation() },
                modifier = Modifier.width(200.dp)
            ) {
                // Tekst na dugmetu se menja u zavisnosti od stanja
                Text(if (uiState.isObservationRunning) "Stop" else "Start")
            }

            Spacer(Modifier.height(16.dp))

            Text("Trajanje prikaza (sekunde): ${String.format("%.1f", uiState.highlightDurationSeconds)}")
            Slider(
                value = uiState.highlightDurationSeconds,
                onValueChange = { viewModel.onDurationChange(it) },
                valueRange = 1f..5f,
                steps = 7,
                // Onemogući slider dok je mod aktivan da se izbegnu bagovi
                enabled = !uiState.isObservationRunning
            )
        } else {
            Button(onClick = { viewModel.onModeChange(TrainingMode.QUIZ) }) {
                Text("Restartuj Kviz")
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = { viewModel.onFlipBoard() }) {
            Text("Okreni tablu")
        }
    }
}

// ... ostatak fajla (TopInfoPanel, QuizStatsView, SegmentedButtonRow, itd.) ostaje isti ...
@Composable
private fun TopInfoPanel(uiState: Module1UiState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Perspektiva: ${uiState.boardOrientation.name}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = uiState.taskText,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (uiState.mode == TrainingMode.QUIZ) {
            QuizStatsView(stats = uiState.quizStats)
        }
    }
}

@Composable
private fun QuizStatsView(stats: com.program.blindfoldchesscouch.viewmodel.QuizStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
        Text("Zadatak: ${stats.taskNumber}/20")
        Text("Pogoci: ${stats.correctAnswers}/${stats.totalAttempts}")
        val time = String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(stats.sessionTimeMillis),
            TimeUnit.MILLISECONDS.toSeconds(stats.sessionTimeMillis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(stats.sessionTimeMillis))
        )
        Text("Vreme: $time")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButtonRow(selectedMode: TrainingMode, onModeChange: (TrainingMode) -> Unit) {
    val options = listOf("Posmatranje", "Kviz")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            val mode = if (index == 0) TrainingMode.OBSERVATION else TrainingMode.QUIZ
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onModeChange(mode) },
                selected = selectedMode == mode
            ) {
                Text(label)
            }
        }
    }
}


@Composable
fun QuizEndDialog(stats: com.program.blindfoldchesscouch.viewmodel.QuizStats, onDismiss: () -> Unit) {
    val time = String.format("%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(stats.sessionTimeMillis),
        TimeUnit.MILLISECONDS.toSeconds(stats.sessionTimeMillis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(stats.sessionTimeMillis))
    )
    val wrongAttempts = stats.totalAttempts - stats.correctAnswers

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kraj Sesije!") },
        text = {
            Column {
                Text("Vreme: $time")
                Text("Ukupno netačnih pokušaja: $wrongAttempts")
                Text("Sjajan posao! Nastavite da vežbate.")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("U redu") }
        }
    )
}

@Composable
fun ClickableChessBoard(
    board: Board,
    orientation: com.program.blindfoldchesscouch.model.Color,
    highlightedSquare: Square?,
    wrongSquare: Square?,
    onSquareClick: (Square) -> Unit
) {
    val ranks = if (orientation == com.program.blindfoldchesscouch.model.Color.WHITE) (8 downTo 1) else (1..8)
    val files = if (orientation == com.program.blindfoldchesscouch.model.Color.WHITE) ('a'..'h') else ('h' downTo 'a')

    Column(modifier = Modifier.border(2.dp, Color.Black)) {
        for (rank in ranks) {
            Row {
                for (file in files) {
                    val square = Square(file, rank)
                    val baseColor = if ((file - 'a' + rank) % 2 == 0) darkSquareColor else lightSquareColor
                    val finalColor = when (square) {
                        highlightedSquare -> highlightColor
                        wrongSquare -> wrongHighlightColor
                        else -> baseColor
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(finalColor)
                            .clickable { onSquareClick(square) },
                        contentAlignment = Alignment.Center
                    ) {
                        board.getPieceAt(square)?.let { piece ->
                            Image(
                                painter = painterResource(id = getDrawableResourceForPiece(piece = piece)),
                                contentDescription = piece.toString(),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}