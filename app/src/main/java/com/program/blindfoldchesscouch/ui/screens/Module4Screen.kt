// ui/screens/Module4Screen.kt
package com.program.blindfoldchesscouch.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.program.blindfoldchesscouch.R
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.viewmodel.Block
import com.program.blindfoldchesscouch.viewmodel.Module4ViewModel

@Composable
fun Module4Screen(
    viewModel: Module4ViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // *** ИЗМЕНА: Прослеђујемо све нове информације у InfoPanel ***
                InfoPanel(
                    statusMessage = uiState.statusMessage,
                    lastBlackMove = uiState.lastBlackMove,
                    blackPiecesInfo = uiState.blackPiecesInfo
                )

                InteractiveChessBoard(
                    board = uiState.game.getCurrentBoard(),
                    selectedSquare = uiState.selectedSquare,
                    blocks = uiState.blocks,
                    arePiecesVisible = uiState.arePiecesVisible,
                    onSquareClick = { square ->
                        viewModel.onSquareClicked(square)
                    }
                )
            }

            ActionButtons(
                arePiecesVisible = uiState.arePiecesVisible,
                onNextPositionClick = { viewModel.onNextPositionClicked() },
                onToggleVisibilityClick = { viewModel.onTogglePieceVisibility() }
            )
        }
    }
}

// *** ИЗМЕНА: InfoPanel сада приказује више информација ***
@Composable
fun InfoPanel(statusMessage: String, lastBlackMove: String?, blackPiecesInfo: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = statusMessage,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (lastBlackMove != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lastBlackMove,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = blackPiecesInfo,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


// --- Остатак кода остаје исти ---
@Composable
fun ActionButtons(
    arePiecesVisible: Boolean,
    onNextPositionClick: () -> Unit,
    onToggleVisibilityClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = onNextPositionClick) {
            Text("Sledeća pozicija")
        }
        Button(onClick = onToggleVisibilityClick) {
            Text(if (arePiecesVisible) "Sakrij figure" else "Otkrij figure")
        }
    }
}

@Composable
fun InteractiveChessBoard(
    board: Board,
    selectedSquare: Square?,
    blocks: List<Block>,
    arePiecesVisible: Boolean,
    onSquareClick: (Square) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(Modifier.fillMaxSize()) {
            for (rank in 8 downTo 1) {
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    for (file in 'a'..'h') {
                        val square = Square(file, rank)
                        val isSelected = square == selectedSquare
                        val blockOnSquare = blocks.find { it.square == square }

                        InteractiveChessSquare(
                            square = square,
                            board = board,
                            isSelected = isSelected,
                            block = blockOnSquare,
                            arePiecesVisible = arePiecesVisible,
                            onClick = { onSquareClick(square) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveChessSquare(
    square: Square,
    board: Board,
    isSelected: Boolean,
    block: Block?,
    arePiecesVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lightSquareColor = Color(0xFFF0D9B5)
    val darkSquareColor = Color(0xFFB58863)
    val selectedColor = Color(0xFF6C9950)

    val squareColor = if (isSelected) selectedColor else if ((square.file - 'a' + square.rank) % 2 == 0) darkSquareColor else lightSquareColor

    Box(
        modifier = modifier
            .background(squareColor)
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val piece = board.getPieceAt(square)

        if (arePiecesVisible) {
            if (piece != null) {
                ChessSquare(square = square, board = board, modifier = Modifier.fillMaxSize())
            } else if (block != null) {
                BlockView(block = block)
            }
        } else {
            if (block != null) {
                BlockView(block = block)
            }
        }
    }
}

@Composable
fun BlockView(block: Block) {
    Image(
        painter = painterResource(id = R.drawable.block),
        contentDescription = "Block",
        modifier = Modifier.fillMaxSize(0.8f)
    )
    Text(
        text = block.turnsLeft.toString(),
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}