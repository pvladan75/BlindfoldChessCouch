// ui/screens/InstructionsScreen.kt
package com.program.blindfoldchesscouch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.viewmodel.InstructionsViewModel

@Composable
fun InstructionsScreen(
    viewModel: InstructionsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // LaunchedEffect će se pokrenuti samo ako se promeni korak i ako korak nije null
    LaunchedEffect(uiState.currentStep) {
        uiState.currentStep?.let { step ->
            val fullText = context.getString(step.textResId)
            viewModel.processTutorialStep(fullText)
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = uiState.displayedText,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp)
            )

            TutorialChessBoard(
                board = uiState.board,
                highlightedSquares = uiState.highlightedSquares
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.onBackClicked() },
                    enabled = uiState.canGoBack && !uiState.isProcessing
                ) {
                    Text("Nazad")
                }
                Button(
                    onClick = { viewModel.onNextClicked() },
                    enabled = uiState.canGoForward && !uiState.isProcessing
                ) {
                    Text("Napred")
                }
            }
        }
    }
}

@Composable
fun TutorialChessBoard(
    board: Board,
    highlightedSquares: Set<Square>
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
                        TutorialChessSquare(
                            square = square,
                            board = board,
                            isHighlighted = square in highlightedSquares,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialChessSquare(
    square: Square,
    board: Board,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val lightSquareColor = Color(0xFFF0D9B5)
    val darkSquareColor = Color(0xFFB58863)
    val highlightColor = Color(0x996C9950)

    val squareColor = if ((square.file - 'a' + square.rank) % 2 == 0) darkSquareColor else lightSquareColor

    Box(
        modifier = modifier
            .background(squareColor)
            .aspectRatio(1f)
    ) {
        val piece = board.getPieceAt(square)
        if (piece != null) {
            ChessSquare(square = square, board = board, modifier = Modifier.fillMaxSize())
        }

        if (isHighlighted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(highlightColor)
            )
        }
    }
}