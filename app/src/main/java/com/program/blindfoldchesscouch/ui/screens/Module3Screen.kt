// ui/screens/Module3Screen.kt
package com.program.blindfoldchesscouch.ui.screens

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.program.blindfoldchesscouch.model.*
import com.program.blindfoldchesscouch.util.getDrawableResourceForPiece
import com.program.blindfoldchesscouch.viewmodel.*
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

    when (uiState.sessionState) {
        SessionState.SETUP -> SetupView(uiState, viewModel)
        SessionState.IN_PROGRESS -> TestView(uiState, viewModel)
        SessionState.FINISHED -> {
            TestView(uiState, viewModel)
            SessionEndDialog(uiState = uiState, onDismiss = { viewModel.onDismissDialog() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupView(uiState: Module3UiState, viewModel: Module3ViewModel) {
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

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Text("Blindfold Mod")
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = uiState.isBlindfoldMode,
                    onCheckedChange = { viewModel.onBlindfoldToggled(it) }
                )
            }

            Spacer(Modifier.weight(1f))
            if(uiState.infoMessage != null) {
                Text(uiState.infoMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }
            Button(
                onClick = { viewModel.onStartSession() },
                enabled = uiState.isStartButtonEnabled,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Započni Sesiju (10 zagonetki)", fontSize = 18.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestView(uiState: Module3UiState, viewModel: Module3ViewModel) {
    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                TestStatsPanel(uiState = uiState)
                Spacer(modifier = Modifier.height(16.dp))

                Module3ChessBoard(
                    board = uiState.board,
                    moveHighlight = uiState.moveHighlight,
                    feedbackSquare = uiState.feedbackSquare,
                    isBlindfold = uiState.isBlindfoldMode,
                    visiblePieceSquare = uiState.visiblePieceForAnimation,
                    forceShowPieces = uiState.forceShowPieces,
                    puzzlePhase = uiState.puzzlePhase,
                    onSquareClick = { viewModel.onSquareClicked(it) }
                )

                Spacer(modifier = Modifier.weight(1f))

                TestControlPanel(
                    uiState = uiState,
                    onStartInteraction = { viewModel.onStartPuzzleInteraction() },
                    onShowPieces = { viewModel.onShowPiecesClicked() },
                    onNextPuzzle = { viewModel.onLoadNextPuzzle() },
                    onVoiceInput = { viewModel.startVoiceRecognition() }
                )
            }

            AnimatedVisibility(
                visible = uiState.puzzlePhase == PuzzlePhase.COMPLETED,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        text = "Zagonetka ${uiState.stats.puzzlesCompleted + 1} rešena!",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun TestControlPanel(
    uiState: Module3UiState,
    onStartInteraction: () -> Unit,
    onShowPieces: () -> Unit,
    onNextPuzzle: () -> Unit,
    onVoiceInput: () -> Unit
) {
    val micPermissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 100.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (uiState.puzzlePhase) {
            PuzzlePhase.MEMORIZE -> {
                Button(
                    onClick = onStartInteraction,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) { Text("Start", fontSize = 18.sp) }
            }
            PuzzlePhase.AWAITING_INPUT -> {
                if (uiState.isBlindfoldMode && !uiState.forceShowPieces) {
                    Button(onClick = onShowPieces, modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Pokaži figure (neuspešno)")
                    }
                }
            }
            PuzzlePhase.FAILED_REVEALED -> {
                Button(
                    onClick = onNextPuzzle,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) { Text("Sledeća pozicija", fontSize = 18.sp) }
            }
            else -> { }
        }

        if (uiState.puzzlePhase != PuzzlePhase.AWAITING_INPUT || (uiState.isBlindfoldMode && !uiState.forceShowPieces)) {
            Spacer(modifier = Modifier.width(16.dp))
        }

        IconButton(
            onClick = {
                if (micPermissionState.status.isGranted) {
                    onVoiceInput()
                } else {
                    micPermissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Unesi odgovor glasom",
                modifier = Modifier.fillMaxSize(0.7f),
                tint = when {
                    uiState.isListeningForVoice -> Color.Red
                    micPermissionState.status.isGranted -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
        }
    }
}

@Composable
private fun PieceCountSelector(pieceType: PieceType, currentCount: Int, range: IntRange, onCountChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = getDrawableResourceForPiece(Piece(pieceType, com.program.blindfoldchesscouch.model.Color.WHITE))),
            contentDescription = pieceType.name,
            modifier = Modifier.size(40.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (currentCount > range.first) onCountChange(currentCount - 1) }, enabled = currentCount > range.first) {
                Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text("$currentCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
            IconButton(onClick = { if (currentCount < range.last) onCountChange(currentCount + 1) }, enabled = currentCount < range.last) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TestStatsPanel(uiState: Module3UiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Zagonetka: ${uiState.stats.puzzlesCompleted + 1}/10",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Greške: ${uiState.stats.mistakes}")

            val time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(uiState.stats.sessionTimerMillis),
                TimeUnit.MILLISECONDS.toSeconds(uiState.stats.sessionTimerMillis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uiState.stats.sessionTimerMillis))
            )
            Text("Vreme: $time")

            Box(modifier = Modifier.width(IntrinsicSize.Min)) {
                if (uiState.puzzlePhase != PuzzlePhase.MEMORIZE) {
                    Text("Potez: ${uiState.currentStepIndex + 1}/${uiState.totalStepsInPuzzle}")
                }
            }
        }
    }
}

@Composable
private fun SessionEndDialog(uiState: Module3UiState, onDismiss: () -> Unit) {
    val time = String.format("%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(uiState.stats.sessionTimerMillis),
        TimeUnit.MILLISECONDS.toSeconds(uiState.stats.sessionTimerMillis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uiState.stats.sessionTimerMillis))
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kraj Sesije!") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Uspešno rešeno: ${10 - uiState.stats.failedPuzzles}/10")
                Text("Ukupno grešaka (klikovi): ${uiState.stats.mistakes}")
                Divider()
                Text("Finalno vreme: $time", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Počni novu sesiju") }
        }
    )
}

@Composable
fun Module3ChessBoard(
    board: Board,
    moveHighlight: Move?,
    feedbackSquare: Pair<Square, Boolean>?,
    isBlindfold: Boolean,
    visiblePieceSquare: Square?,
    forceShowPieces: Boolean,
    puzzlePhase: PuzzlePhase,
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
                            val isVisible = puzzlePhase == PuzzlePhase.MEMORIZE || !isBlindfold || square == visiblePieceSquare || forceShowPieces
                            Image(
                                painter = painterResource(id = getDrawableResourceForPiece(piece = piece)),
                                contentDescription = piece.toString(),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .alpha(if (isVisible) 1f else 0f)
                            )
                        }
                    }
                }
            }
        }
    }
}