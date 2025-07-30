package com.program.blindfoldchesscouch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.program.blindfoldchesscouch.model.*
import com.program.blindfoldchesscouch.tts.TtsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sign
import kotlin.random.Random

private fun Move.toSpokenNotation(): String {
    return "${this.from.toAlgebraicNotation()} ${this.to.toAlgebraicNotation()}"
}

data class Block(val square: Square, var turnsLeft: Int = 3)

data class Module4UiState(
    val game: Game = Game(),
    val statusMessage: String = "Modul 4: Lov na figure. Beli igra.",
    val selectedSquare: Square? = null,
    val isGameOver: Boolean = false,
    val blocks: List<Block> = emptyList(),
    val arePiecesVisible: Boolean = true,
    val lastBlackMove: String? = null,
    val blackPiecesInfo: String = ""
)

class Module4ViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(Module4UiState())
    val uiState: StateFlow<Module4UiState> = _uiState.asStateFlow()

    private val ttsHelper = TtsHelper(application)

    init {
        generateRandomPositionAndLoad()
    }

    fun onNextPositionClicked() {
        generateRandomPositionAndLoad()
    }

    private fun generateRandomPositionAndLoad() {
        viewModelScope.launch {
            val whitePieceTypes = mutableListOf<PieceType>()
            val blackPieceTypes = mutableListOf<PieceType>()
            val piecePlacement = mutableMapOf<Square, Piece>()

            val majorPieces = listOf(PieceType.QUEEN, PieceType.ROOK)
            val allPossiblePieces = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

            whitePieceTypes.add(majorPieces.random())
            repeat(2) {
                whitePieceTypes.add(allPossiblePieces.random())
            }

            val numberOfBlackPieces = Random.nextInt(2, 5)
            repeat(numberOfBlackPieces) {
                blackPieceTypes.add(allPossiblePieces.random())
            }

            val allSquares = (1..8).flatMap { rank -> ('a'..'h').map { file -> Square(file, rank) } }.toMutableList()

            whitePieceTypes.forEach { pieceType ->
                val randomSquare = allSquares.random()
                piecePlacement[randomSquare] = Piece(pieceType, Color.WHITE)
                allSquares.remove(randomSquare)
            }

            val tempGame = Game()
            val tempFen = boardMapToFen(piecePlacement)
            tempGame.loadFen(tempFen)

            val safeSquares = allSquares.filter { square ->
                !tempGame.isSquareAttacked(square, Color.WHITE)
            }

            if (safeSquares.size >= blackPieceTypes.size) {
                val shuffledSafeSquares = safeSquares.shuffled()
                blackPieceTypes.forEachIndexed { index, pieceType ->
                    val square = shuffledSafeSquares[index]
                    piecePlacement[square] = Piece(pieceType, Color.BLACK)
                }
            } else {
                generateRandomPositionAndLoad()
                return@launch
            }

            val finalFen = boardMapToFen(piecePlacement)
            val newGame = Game()
            newGame.loadFen(finalFen)

            _uiState.update {
                it.copy(
                    game = newGame,
                    statusMessage = "Beli je na potezu. Ulovi sve crne figure!",
                    isGameOver = false,
                    selectedSquare = null,
                    blocks = emptyList(),
                    lastBlackMove = null,
                    blackPiecesInfo = getBlackPiecesInfo(newGame.getCurrentBoard())
                )
            }
        }
    }

    private fun boardMapToFen(placement: Map<Square, Piece>): String {
        val fen = StringBuilder()
        for (rank in 8 downTo 1) {
            var emptySquares = 0
            for (file in 'a'..'h') {
                val piece = placement[Square(file, rank)]
                if (piece == null) {
                    emptySquares++
                } else {
                    if (emptySquares > 0) {
                        fen.append(emptySquares)
                        emptySquares = 0
                    }
                    fen.append(piece.toFenChar())
                }
            }
            if (emptySquares > 0) {
                fen.append(emptySquares)
            }
            if (rank > 1) {
                fen.append('/')
            }
        }
        fen.append(" w - - 0 1")
        return fen.toString()
    }


    fun onTogglePieceVisibility() {
        _uiState.update { it.copy(arePiecesVisible = !it.arePiecesVisible) }
    }


    fun onSquareClicked(square: Square) {
        val currentState = _uiState.value
        if (currentState.isGameOver || currentState.game.currentPlayer == Color.BLACK) return

        val selected = currentState.selectedSquare
        if (selected == null) {
            val piece = currentState.game.getCurrentBoard().getPieceAt(square)
            if (piece != null && piece.color == Color.WHITE) {
                _uiState.update { it.copy(selectedSquare = square, statusMessage = "Izabrano ${square.toAlgebraicNotation()}. Igraj na...") }
            }
        } else {
            val from = selected
            val to = square
            val game = currentState.game
            val legalMoves = game.getLegalMoves()
            val intendedMove = legalMoves.find { it.from == from && it.to == to }

            if (intendedMove != null) {
                ttsHelper.speak(intendedMove.toSpokenNotation())

                val newGame = game.copy()
                newGame.tryMakeMove(intendedMove)

                val blocksAfterMove = currentState.blocks.filter { it.square != to }

                val movingPiece = game.getCurrentBoard().getPieceAt(from)!!
                val newBlocks = generateBlocksForMove(intendedMove, movingPiece)
                val updatedBlocks = blocksAfterMove + newBlocks

                val newBlackPiecesInfo = getBlackPiecesInfo(newGame.getCurrentBoard())

                val blackPiecesLeft = newGame.getCurrentBoard().getAllPieces().any { it.value.color == Color.BLACK }
                if (!blackPiecesLeft) {
                    _uiState.update {
                        it.copy(
                            game = newGame,
                            selectedSquare = null,
                            statusMessage = "Pobeda! Sve crne figure su ulovljene.",
                            isGameOver = true,
                            blocks = updatedBlocks,
                            blackPiecesInfo = newBlackPiecesInfo
                        )
                    }
                    return
                }

                _uiState.update {
                    it.copy(
                        game = newGame,
                        selectedSquare = null,
                        statusMessage = "Crni razmišlja...",
                        blocks = updatedBlocks,
                        blackPiecesInfo = newBlackPiecesInfo,
                        lastBlackMove = null
                    )
                }
                playBlacksResponse()
            } else {
                _uiState.update { it.copy(selectedSquare = null, statusMessage = "Nije validan potez. Beli je ponovo na potezu.") }
            }
        }
    }

    private fun playBlacksResponse() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val game = currentState.game

            val nextTurnBlocks = currentState.blocks.map {
                it.copy(turnsLeft = it.turnsLeft - 1)
            }.filter { it.turnsLeft > 0 }

            val allLegalMoves = game.getLegalMoves()
            val validBlackMoves = allLegalMoves.filter { move ->
                val movingPiece = game.getCurrentBoard().getPieceAt(move.from) ?: return@filter false
                if (game.getCurrentBoard().getPieceAt(move.to) != null) return@filter false
                val landsOnBlock = nextTurnBlocks.any { it.square == move.to }
                if (landsOnBlock) return@filter false
                if (movingPiece.type != PieceType.KNIGHT) {
                    if (isPathBlocked(move.from, move.to, nextTurnBlocks)) return@filter false
                }
                true
            }

            var chosenMove: Move? = null
            if (validBlackMoves.isNotEmpty()) {
                val blackPieceSquares = game.getCurrentBoard().getAllPieces()
                    .filter { it.value.color == Color.BLACK }
                    .map { it.key }
                val threatenedSquares = blackPieceSquares.filter { game.isSquareAttacked(it, Color.WHITE) }

                if (threatenedSquares.isNotEmpty()) {
                    val safeEscapeMoves = validBlackMoves.filter { move ->
                        threatenedSquares.contains(move.from) && !game.isSquareAttacked(move.to, Color.WHITE)
                    }
                    if (safeEscapeMoves.isNotEmpty()) {
                        chosenMove = safeEscapeMoves.random()
                    } else {
                        val allEscapeMoves = validBlackMoves.filter { move -> threatenedSquares.contains(move.from) }
                        if (allEscapeMoves.isNotEmpty()) {
                            chosenMove = allEscapeMoves.random()
                        }
                    }
                }

                if (chosenMove == null) {
                    val safeMoves = validBlackMoves.filter { move ->
                        !game.isSquareAttacked(move.to, Color.WHITE)
                    }
                    if (safeMoves.isNotEmpty()) {
                        chosenMove = safeMoves.random()
                    } else {
                        chosenMove = validBlackMoves.random()
                    }
                }
            }

            val newGame = game.copy()
            if (chosenMove != null) {
                ttsHelper.speak(chosenMove.toSpokenNotation())
                newGame.tryMakeMove(chosenMove)
                _uiState.update {
                    it.copy(
                        game = newGame,
                        statusMessage = "Beli je na potezu.",
                        blocks = nextTurnBlocks,
                        lastBlackMove = "Последњи потез црног: ${chosenMove.from.toAlgebraicNotation()}-${chosenMove.to.toAlgebraicNotation()}"
                    )
                }
            } else {
                // *** ИЗМЕНА: Исправна логика за прескакање потеза ***
                // Уместо "dummy" потеза, ручно мењамо играча у FEN стрингу.
                val currentFen = newGame.toFen()
                val fenWithWhiteToMove = currentFen.replaceFirst(" b ", " w ")
                newGame.loadFen(fenWithWhiteToMove)

                _uiState.update {
                    it.copy(game = newGame, statusMessage = "Crni je blokiran! Beli je ponovo na potezu.", blocks = nextTurnBlocks)
                }
            }
        }
    }

    private fun getBlackPiecesInfo(board: Board): String {
        val pieceCounts = board.getAllPieces()
            .filter { it.value.color == Color.BLACK }
            .map { it.value.type }
            .groupingBy { it }
            .eachCount()

        if (pieceCounts.isEmpty()) return "Нема црних фигура."

        val pieceSymbols = mapOf(
            PieceType.QUEEN to '♛',
            PieceType.ROOK to '♜',
            PieceType.BISHOP to '♝',
            PieceType.KNIGHT to '♞'
        )

        val description = pieceCounts.entries.joinToString(", ") { (type, count) ->
            "${pieceSymbols[type]} x$count"
        }

        return "Преостало: ${pieceCounts.values.sum()} ($description)"
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }

    private fun generateBlocksForMove(move: Move, movingPiece: Piece): List<Block> {
        val newBlocks = mutableListOf<Block>()
        when (movingPiece.type) {
            PieceType.KNIGHT -> {
                newBlocks.add(Block(square = move.from))
            }
            PieceType.ROOK, PieceType.BISHOP, PieceType.QUEEN -> {
                val from = move.from
                val to = move.to
                val dx = (to.file - from.file).sign
                val dy = (to.rank - from.rank).sign

                var currentFile = from.file.code
                var currentRank = from.rank

                while (currentFile != to.file.code || currentRank != to.rank) {
                    newBlocks.add(Block(square = Square(currentFile.toChar(), currentRank)))
                    currentFile += dx
                    currentRank += dy
                }
            }
            else -> {}
        }
        return newBlocks
    }

    private fun isPathBlocked(from: Square, to: Square, blocks: List<Block>): Boolean {
        val dx = (to.file - from.file).sign
        val dy = (to.rank - from.rank).sign

        var currentFile = from.file.code + dx
        var currentRank = from.rank + dy

        while (currentFile != to.file.code || currentRank != to.rank) {
            val currentSquare = Square(currentFile.toChar(), currentRank)
            if (blocks.any { it.square == currentSquare }) {
                return true
            }
            currentFile += dx
            currentRank += dy
        }
        return false
    }
}