// ui/screens/Module3Screen.kt
package com.program.blindfoldchesscouch.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
// IKONICE VIŠE NISU POTREBNE, PA SU IMPORTOVI UKLONJENI
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Move
import com.program.blindfoldchesscouch.model.PieceType
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.util.getDrawableResourceForPiece
import com.program.blindfoldchesscouch.viewmodel.Module3UiState
import com.program.blindfoldchesscouch.viewmodel.Module3ViewModel
import com.program.blindfoldchesscouch.viewmodel.TestState
import java.util.concurrent.TimeUnit

// --- Boje za ovaj modul ---
private val lightSquareColorM3 = Color(0xFFF0D9B5)
private val darkSquareColorM3 = Color(0xFFB58863)
private val moveFromColor = Color(0x993498DB)
private val moveToColor = Color(0x99F1C40F)
private val feedbackCorrectColor = Color(0xAA2ECC71)
private val feedbackWrongColor = Color(0xAAE74C3C)

@Composable
fun Module3Screen(viewModel: Module3ViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.testState) {
        TestState.SETUP -> SetupView(uiState, viewModel)
        TestState.IN_PROGRESS -> TestView(uiState, viewModel)
        TestState.FINISHED -> {
            TestView(uiState, viewModel)
            TestEndDialog(uiState = uiState, onDismiss = { viewModel.onDismissDialog() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupView(uiState: Module3UiState, viewModel: Module3ViewModel) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Modul 3: Interaktivni Parovi") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Izaberite figure za vežbu:", style = MaterialTheme.typography.titleLarge)

            PieceCountSelector(PieceType.QUEEN, uiState.pieceSelection[PieceType.QUEEN] ?: 0, 0..1) { count -> viewModel.onPieceCountChange(PieceType.QUEEN, count) }
            PieceCountSelector(PieceType.ROOK, uiState.pieceSelection[PieceType.ROOK] ?: 0, 0..2) { count -> viewModel.onPieceCountChange(PieceType.ROOK, count) }
            PieceCountSelector(PieceType.BISHOP, uiState.pieceSelection[PieceType.BISHOP] ?: 0, 0..2) { count -> viewModel.onPieceCountChange(PieceType.BISHOP, count) }
            PieceCountSelector(PieceType.KNIGHT, uiState.pieceSelection[PieceType.KNIGHT] ?: 0, 0..2) { count -> viewModel.onPieceCountChange(PieceType.KNIGHT, count) }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Izaberite dužinu testa:", style = MaterialTheme.typography.titleMedium)
            val lengthOptions = listOf(10, 20, 30)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                lengthOptions.forEach { length ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = lengthOptions.indexOf(length), count = lengthOptions.size),
                        onClick = { viewModel.onPuzzleLengthChange(length) },
                        selected = uiState.selectedPuzzleLength == length
                    ) { Text("$length") }
                }
            }

            Spacer(Modifier.weight(1f))

            if (uiState.infoMessage != null) {
                Text(uiState.infoMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = { viewModel.onStartTest() },
                enabled = uiState.isStartButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Start Test", fontSize = 18.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestView(uiState: Module3UiState, viewModel: Module3ViewModel) {
    Scaffold(
        topBar = { TopAppBar(title = { TestStatsPanel(uiState) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Module3ChessBoard(
                board = uiState.board,
                moveHighlight = uiState.moveHighlight,
                feedbackSquare = uiState.feedbackSquare,
                onSquareClick = { viewModel.onSquareClicked(it) }
            )
        }
    }
}

/**
 * Pomoćna komponenta za biranje broja figura. SAD KORISTI TEKST UMESTO IKONICA.
 */
@Composable
private fun PieceCountSelector(pieceType: PieceType, currentCount: Int, range: IntRange, onCountChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = getDrawableResourceForPiece(com.program.blindfoldchesscouch.model.Piece(pieceType, com.program.blindfoldchesscouch.model.Color.WHITE))),
            contentDescription = pieceType.name,
            modifier = Modifier.size(40.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (currentCount > range.first) onCountChange(currentCount - 1) }, enabled = currentCount > range.first) {
                // ZAMENA IKONICE SA TEKSTOM
                Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text("$currentCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            IconButton(onClick = { if (currentCount < range.last) onCountChange(currentCount + 1) }, enabled = currentCount < range.last) {
                // ZAMENA IKONICE SA TEKSTOM
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TestStatsPanel(uiState: Module3UiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val time = String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(uiState.stats.timerMillis),
            TimeUnit.MILLISECONDS.toSeconds(uiState.stats.timerMillis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uiState.stats.timerMillis))
        )
        Text("Potez: ${uiState.currentStepIndex + 1}/${uiState.selectedPuzzleLength}")
        Text("Vreme: $time")
        Text("Greške: ${uiState.stats.mistakes}")
    }
}

@Composable
private fun TestEndDialog(uiState: Module3UiState, onDismiss: () -> Unit) {
    val time = String.format(
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(uiState.stats.timerMillis),
        TimeUnit.MILLISECONDS.toSeconds(uiState.stats.timerMillis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uiState.stats.timerMillis))
    )
    val pieceConfigText = uiState.pieceSelection
        .filter { it.value > 0 }
        .map { "${it.key.name} x${it.value}" }.joinToString()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kraj Testa!") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dužina testa: ${uiState.selectedPuzzleLength} poteza")
                Text("Korišćene figure: $pieceConfigText")
                Divider()
                Text("Finalno vreme: $time", fontWeight = FontWeight.Bold)
                Text("Broj grešaka: ${uiState.stats.mistakes}", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Novi Test") }
        }
    )
}

@Composable
fun Module3ChessBoard(
    board: Board,
    moveHighlight: Move?,
    feedbackSquare: Pair<Square, Boolean>?,
    onSquareClick: (Square) -> Unit
) {
    Column(modifier = Modifier.border(2.dp, Color.Black)) {
        for (rank in 8 downTo 1) {
            Row {
                for (file in 'a'..'h') {
                    val square = Square(file, rank)
                    val baseColor = if ((file - 'a' + rank) % 2 == 0) darkSquareColorM3 else lightSquareColorM3

                    val finalColor = when (square) {
                        feedbackSquare?.first -> if (feedbackSquare.second) feedbackCorrectColor else feedbackWrongColor
                        moveHighlight?.from -> moveFromColor
                        moveHighlight?.to -> moveToColor
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