// ui/screens/Module4Screen.kt
package com.program.blindfoldchesscouch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.viewmodel.Module4ViewModel
import com.program.blindfoldchesscouch.viewmodel.Module4UiState

// Glavna funkcija ekrana za Modul 4
@Composable
fun Module4Screen(
    viewModel: Module4ViewModel = viewModel() // Automatski dobijamo instancu našeg novog ViewModel-a
) {
    // Pratimo stanje (UiState) iz ViewModel-a
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info panel za poruke
            InfoPanel(message = uiState.statusMessage)

            // Interaktivna šahovska tabla
            InteractiveChessBoard(
                board = uiState.game.getCurrentBoard(),
                selectedSquare = uiState.selectedSquare,
                onSquareClick = { square ->
                    // Svaki klik na polje prosleđujemo ViewModel-u
                    viewModel.onSquareClicked(square)
                }
            )
        }
    }
}

// Komponenta za prikaz poruka
@Composable
fun InfoPanel(message: String) {
    Text(
        text = message,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

// Specijalna verzija table koja reaguje na klik i označava selektovano polje
@Composable
fun InteractiveChessBoard(
    board: Board,
    selectedSquare: Square?,
    onSquareClick: (Square) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Osigurava da je tabla uvek kvadrat
            .border(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(Modifier.fillMaxSize()) {
            for (rank in 8 downTo 1) {
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    for (file in 'a'..'h') {
                        val square = Square(file, rank)
                        val isSelected = square == selectedSquare
                        InteractiveChessSquare(
                            square = square,
                            board = board,
                            isSelected = isSelected,
                            onClick = { onSquareClick(square) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// Polje na tabli koje je klikabilno i može biti označeno
@Composable
fun InteractiveChessSquare(
    square: Square,
    board: Board,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lightSquareColor = Color(0xFFF0D9B5)
    val darkSquareColor = Color(0xFFB58863)
    val selectedColor = Color(0xFF6C9950) // Boja za označeno polje

    val squareColor = if (isSelected) {
        selectedColor
    } else if ((square.file - 'a' + square.rank) % 2 == 0) {
        darkSquareColor
    } else {
        lightSquareColor
    }

    Box(
        modifier = modifier
            .background(squareColor)
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        // Koristimo postojeću ChessSquare komponentu za iscrtavanje figure
        val piece = board.getPieceAt(square)
        if(piece != null){
            ChessSquare(square = square, board = board, modifier = Modifier.fillMaxSize())
        }
    }
}