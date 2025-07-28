// in ui/screens/Module2Screen.kt
package com.program.blindfoldchesscouch.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.program.blindfoldchesscouch.model.Move
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.util.getDrawableResourceForPiece
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
        topBar = {
            TopAppBar(title = { Text("Modul 2: Mat na slepo") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            StatusPanel(uiState = uiState)

            Module2ChessBoard(
                board = uiState.game.getCurrentBoard(),
                arePiecesVisible = uiState.arePiecesVisible,
                selectedSquare = uiState.selectedSquare,
                onSquareClick = { square -> viewModel.onSquareClicked(square) }
            )

            // Logika za prikazivanje "Start" dugmeta ili kontrolnog panela
            val isGameStarted = uiState.statusMessage != "Pritisni START za početak"
            if (!isGameStarted) {
                Button(
                    onClick = { viewModel.onStartClicked() },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
                ) {
                    Text("Start")
                }
            } else {
                ControlPanel(
                    uiState = uiState,
                    onToggleVisibility = { viewModel.onToggleVisibilityClicked() },
                    onNextPosition = { viewModel.onNextPositionClicked() }
                )
            }
        }
    }
}

/**
 * NOVO: Panel sa kontrolama za igru.
 */
@Composable
fun ControlPanel(
    uiState: Module2UiState,
    onToggleVisibility: () -> Unit,
    onNextPosition: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = onToggleVisibility) {
            Text(if (uiState.arePiecesVisible) "Sakrij figure" else "Pokaži figure")
        }
        Button(onClick = onNextPosition) {
            Text("Sledeća pozicija")
        }
    }
}


@Composable
fun StatusPanel(uiState: Module2UiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        // Prikazujemo i redni broj zagonetke
        Text(
            text = "Zagonetka: ${uiState.currentPuzzleIndex + 1}/${uiState.totalPuzzles}",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = uiState.statusMessage,
            style = MaterialTheme.typography.headlineSmall
        )

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

                val time = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(uiState.puzzleTimerMillis),
                    TimeUnit.MILLISECONDS.toSeconds(uiState.puzzleTimerMillis) % 60
                )
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
    // ... (ova funkcija ostaje potpuno ista kao pre)
    Column(modifier = Modifier.border(2.dp, Color.Black)) {
        for (rank in 8 downTo 1) {
            Row {
                for (file in 'a'..'h') {
                    val square = Square(file, rank)
                    val baseColor = if (square == selectedSquare) {
                        selectedSquareColor
                    } else {
                        if ((file - 'a' + rank) % 2 == 0) darkSquareColorM2 else lightSquareColorM2
                    }
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f).background(baseColor).clickable { onSquareClick(square) },
                        contentAlignment = Alignment.Center
                    ) {
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