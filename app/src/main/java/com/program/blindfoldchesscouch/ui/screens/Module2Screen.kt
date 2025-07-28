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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.program.blindfoldchesscouch.model.Board
import com.program.blindfoldchesscouch.model.Square
import com.program.blindfoldchesscouch.util.getDrawableResourceForPiece
import com.program.blindfoldchesscouch.viewmodel.Module2ViewModel

private val lightSquareColorM2 = Color(0xFFF0D9B5)
private val darkSquareColorM2 = Color(0xFFB58863)

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
            Text(
                text = uiState.statusMessage,
                style = MaterialTheme.typography.headlineSmall
            )

            Module2ChessBoard(
                board = uiState.board,
                arePiecesVisible = uiState.arePiecesVisible,
                onSquareClick = { /* Logika za klik će doći kasnije */ }
            )

            if (uiState.arePiecesVisible) {
                Button(
                    onClick = { viewModel.onStartClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp)
                ) {
                    Text("Start")
                }
            } else {
                // Prazan prostor da tabla ostane centrirana
                Spacer(modifier = Modifier.height(82.dp))
            }
        }
    }
}

@Composable
fun Module2ChessBoard(
    board: Board,
    arePiecesVisible: Boolean,
    onSquareClick: (Square) -> Unit
) {
    Column(modifier = Modifier.border(2.dp, Color.Black)) {
        for (rank in 8 downTo 1) {
            Row {
                for (file in 'a'..'h') {
                    val square = Square(file, rank)
                    val baseColor = if ((file - 'a' + rank) % 2 == 0) darkSquareColorM2 else lightSquareColorM2

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(baseColor)
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
                                    .alpha(if (arePiecesVisible) 1f else 0f) // Ključna logika za skrivanje
                            )
                        }
                    }
                }
            }
        }
    }
}