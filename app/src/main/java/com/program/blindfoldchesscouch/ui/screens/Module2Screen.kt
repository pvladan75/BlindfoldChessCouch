// in ui/screens/Module2Screen.kt
package com.program.blindfoldchesscouch.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.util.getDrawableResourceForPiece
import com.program.blindfoldchesscouch.viewmodel.Module2SessionState
import com.program.blindfoldchesscouch.viewmodel.Module2UiState
import com.program.blindfoldchesscouch.viewmodel.Module2ViewModel
import java.util.concurrent.TimeUnit

private val lightSquareColorM2 = Color(0xFFF0D9B5)
private val darkSquareColorM2 = Color(0xFFB58863)
private val selectedSquareColor = Color(0x99F1C40F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Module2Screen(viewModel: Module2ViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Modul 2: Mat na slepo") }) }
    ) { padding ->
        when (uiState.sessionState) {
            Module2SessionState.SETUP -> SetupView(
                modifier = Modifier.padding(padding),
                selectedDifficulty = uiState.selectedDifficulty,
                statusMessage = uiState.statusMessage,
                onDifficultyChange = { viewModel.onDifficultySelected(it) },
                onStartSession = { viewModel.onStartSession() }
            )
            Module2SessionState.IN_PROGRESS -> GameView(
                modifier = Modifier.padding(padding),
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupView(
    modifier: Modifier = Modifier,
    selectedDifficulty: String,
    statusMessage: String,
    onDifficultyChange: (String) -> Unit,
    onStartSession: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(statusMessage, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp))
        Text("Izaberi težinu:", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        val difficulties = listOf("easy", "medium", "hard")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            difficulties.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = difficulties.size),
                    onClick = { onDifficultyChange(label) },
                    selected = selectedDifficulty == label
                ) {
                    Text(label.replaceFirstChar { it.uppercase() })
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onStartSession,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Započni sesiju")
        }
    }
}

@Composable
fun GameView(modifier: Modifier = Modifier, viewModel: Module2ViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        StatusPanel(uiState = uiState)

        val boardToShow = if (uiState.isReviewMode) uiState.reviewBoard else uiState.game.getCurrentBoard()
        Module2ChessBoard(
            board = boardToShow,
            arePiecesVisible = uiState.arePiecesVisible,
            selectedSquare = uiState.selectedSquare,
            onSquareClick = { square -> viewModel.onSquareClicked(square) }
        )

        val isGameStarted = uiState.statusMessage != "Pritisni START za početak"
        if (!isGameStarted) {
            Button(
                onClick = { viewModel.onStartClicked() },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
            ) { Text("Start") }
        } else {
            if (uiState.isReviewMode) {
                ReviewPanel(
                    uiState = uiState,
                    onExitReview = { viewModel.onExitReviewMode() },
                    onPreviousMove = { viewModel.onPreviousMoveInReview() },
                    onNextMove = { viewModel.onNextMoveInReview() },
                    onGoToStart = { viewModel.onGoToStartOfReview() },
                    onGoToEnd = { viewModel.onGoToEndOfReview() }
                )
            } else {
                ControlPanel(
                    uiState = uiState,
                    onToggleVisibility = { viewModel.onToggleVisibilityClicked() },
                    onNextPosition = { viewModel.onNextPositionClicked() },
                    onEnterReview = { viewModel.onEnterReviewMode() }
                )
            }
        }
    }
}

@Composable
fun ReviewPanel(
    uiState: Module2UiState,
    onExitReview: () -> Unit,
    onPreviousMove: () -> Unit,
    onNextMove: () -> Unit,
    onGoToStart: () -> Unit,
    onGoToEnd: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Pregled partije", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onExitReview) {
                    Icon(Icons.Default.Close, contentDescription = "Zatvori pregled")
                }
            }
            Text("Potez: ${uiState.reviewMoveIndex + 1} / ${uiState.fullMoveHistory.size}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onGoToStart) { Icon(Icons.Filled.FirstPage, contentDescription = "Idi na početak") }
                IconButton(onClick = onPreviousMove) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prethodni potez") }
                IconButton(onClick = onNextMove) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Sledeći potez") }
                IconButton(onClick = onGoToEnd) { Icon(Icons.AutoMirrored.Filled.LastPage, contentDescription = "Idi na kraj") }
            }
        }
    }
}

@Composable
fun ControlPanel(
    uiState: Module2UiState,
    onToggleVisibility: () -> Unit,
    onNextPosition: () -> Unit,
    onEnterReview: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onToggleVisibility) { Text(if (uiState.arePiecesVisible) "Sakrij figure" else "Pokaži figure") }
            Button(onClick = onNextPosition) { Text("Sledeća pozicija") }
        }
        Button(
            onClick = onEnterReview,
            enabled = uiState.game.getMoveHistory().isNotEmpty()
        ) {
            Text("Prikaži poteze")
        }
    }
}

@Composable
fun StatusPanel(uiState: Module2UiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.heightIn(min = 72.dp)
    ) {
        if (uiState.sessionState == Module2SessionState.IN_PROGRESS) {
            Text(text = "Zagonetka: ${uiState.currentPuzzleIndex + 1}/${uiState.totalPuzzles}", style = MaterialTheme.typography.titleMedium)
        }
        Text(text = uiState.statusMessage, style = MaterialTheme.typography.headlineSmall)
        val isGameStarted = uiState.statusMessage != "Pritisni START za početak"
        if (isGameStarted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                val lastMoveText = uiState.lastMove?.let {
                    val player = if (it.piece?.color == com.program.blindfoldchesscouch.model.Color.WHITE) "Beli" else "Crni"
                    "$player: ${it.toAlgebraicNotation()}"
                } ?: "---"
                Text(text = lastMoveText, fontWeight = FontWeight.Bold)
                val time = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(uiState.puzzleTimerMillis), TimeUnit.MILLISECONDS.toSeconds(uiState.puzzleTimerMillis) % 60)
                Text(text = time)
            }
        }
    }
}

@Composable
fun Module2ChessBoard(
    board: Board,
    arePiecesVisible: Boolean,
    selectedSquare: Square?,
    onSquareClick: (Square) -> Unit
) {
    Column(modifier = Modifier.border(2.dp, Color.Black)) {
        for (rank in 8 downTo 1) {
            Row {
                for (file in 'a'..'h') {
                    val square = Square(file, rank)
                    // *** ИСПРАВКА ЈЕ ОВДЕ ***
                    val normalColor = if ((file - 'a' + rank) % 2 == 0) lightSquareColorM2 else darkSquareColorM2
                    val baseColor = if (square == selectedSquare) selectedSquareColor else normalColor
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).background(baseColor).clickable { onSquareClick(square) }, contentAlignment = Alignment.Center) {
                        board.getPieceAt(square)?.let { piece ->
                            Image(
                                painter = painterResource(id = getDrawableResourceForPiece(piece = piece)),
                                contentDescription = piece.toString(),
                                modifier = Modifier.fillMaxSize().padding(4.dp).alpha(if (arePiecesVisible) 1f else 0f)
                            )
                        }
                    }
                }
            }
        }
    }
}