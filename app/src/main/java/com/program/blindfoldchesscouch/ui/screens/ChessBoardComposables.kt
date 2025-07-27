// ui/screens/ChessBoardComposables.kt
package com.program.blindfoldchesscouch.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource // Važan import
import androidx.compose.ui.unit.dp
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.util.getDrawableResourceForPiece

private val lightSquare = Color(0xFFF0D9B5)
private val darkSquare = Color(0xFFB58863)

@Composable
fun ChessBoard(board: Board) {
    Column {
        for (rank in 8 downTo 1) {
            Row {
                for (file in 'a'..'h') {
                    val squareColor = if ((file - 'a' + rank) % 2 == 0) darkSquare else lightSquare
                    ChessSquare(
                        square = Square(file, rank),
                        board = board,
                        modifier = Modifier
                            .weight(1f)
                            .background(squareColor)
                    )
                }
            }
        }
    }
}

@Composable
fun ChessSquare(square: Square, board: Board, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val piece = board.getPieceAt(square)
        if (piece != null) {
            // Sada koristimo standardni `painterResource` umesto Coil-a.
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