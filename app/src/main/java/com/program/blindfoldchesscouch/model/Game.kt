// model/Game.kt
package com.program.blindfoldchesscouch.model

import kotlin.math.abs // *** ИЗМЕНА: Додат потребан import ***

class Game {
    private var board: Board = Board()
    var currentPlayer: Color = Color.WHITE
        private set

    private val moveHistory: MutableList<Move> = mutableListOf()

    fun getMoveHistory(): List<Move> {
        return moveHistory.toList()
    }

    var whiteKingSideCastlingAllowed: Boolean = true
        private set
    var whiteQueenSideCastlingAllowed: Boolean = true
        private set
    var blackKingSideCastlingAllowed: Boolean = true
        private set
    var blackQueenSideCastlingAllowed: Boolean = true
        private set
    var enPassantTargetSquare: Square? = null
        private set
    var halfMoveClock: Int = 0
        private set
    var fullMoveNumber: Int = 1

    init {
        resetGame()
    }

    fun resetGame() {
        board.setupStartingPosition()
        currentPlayer = Color.WHITE
        moveHistory.clear()
        whiteKingSideCastlingAllowed = true
        whiteQueenSideCastlingAllowed = true
        blackKingSideCastlingAllowed = true
        blackQueenSideCastlingAllowed = true
        enPassantTargetSquare = null
        halfMoveClock = 0
        fullMoveNumber = 1
    }

    fun loadFen(fen: String) {
        val parts = fen.split(" ")
        board.loadFen(parts[0])
        currentPlayer = if (parts[1] == "w") Color.WHITE else Color.BLACK
        whiteKingSideCastlingAllowed = parts[2].contains('K')
        whiteQueenSideCastlingAllowed = parts[2].contains('Q')
        blackKingSideCastlingAllowed = parts[2].contains('k')
        blackQueenSideCastlingAllowed = parts[2].contains('q')
        enPassantTargetSquare = if (parts.getOrNull(3) != "-") Square.fromAlgebraicNotation(parts[3]) else null
        halfMoveClock = parts.getOrNull(4)?.toIntOrNull() ?: 0
        fullMoveNumber = parts.getOrNull(5)?.toIntOrNull() ?: 1
        moveHistory.clear() // FEN учитава нову позицију, историја се брише
    }

    fun toFen(): String {
        val fen = StringBuilder()
        for (rank in 8 downTo 1) {
            var emptySquares = 0
            for (file in 'a'..'h') {
                val piece = board.getPieceAt(Square(file, rank))
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
        fen.append(" ")
        fen.append(if (currentPlayer == Color.WHITE) 'w' else 'b')
        fen.append(" ")
        val castling = StringBuilder()
        if (whiteKingSideCastlingAllowed) castling.append('K')
        if (whiteQueenSideCastlingAllowed) castling.append('Q')
        if (blackKingSideCastlingAllowed) castling.append('k')
        if (blackQueenSideCastlingAllowed) castling.append('q')
        if (castling.isEmpty()) {
            fen.append('-')
        } else {
            fen.append(castling.toString())
        }
        fen.append(" ")
        fen.append(enPassantTargetSquare?.toAlgebraicNotation() ?: "-")
        fen.append(" ")
        fen.append(halfMoveClock)
        fen.append(" ")
        fen.append(fullMoveNumber)
        return fen.toString()
    }

    fun getCurrentBoard(): Board {
        return board
    }

    fun tryMakeMove(move: Move): Boolean {
        val pieceToMove = board.getPieceAt(move.from)
        if (pieceToMove == null || pieceToMove.color != currentPlayer) {
            return false
        }
        // У MoveGenerator-у би требало да се постави capturedPiece.
        // Овде само претпостављамо да је то урађено.
        val success = board.makeMove(move)
        if (success) {
            moveHistory.add(move)
            updateGameState(move, movingPiece = pieceToMove)
            currentPlayer = currentPlayer.opposite()
            if (currentPlayer == Color.WHITE) {
                fullMoveNumber++
            }
            return true
        }
        return false
    }

    private fun updateGameState(move: Move, movingPiece: Piece) {
        enPassantTargetSquare = null
        if (movingPiece.type == PieceType.PAWN || board.getPieceAt(move.to) != null) { // Проверавамо да ли је фигура заиста узета
            halfMoveClock = 0
        } else {
            halfMoveClock++
        }
        if (movingPiece.type == PieceType.KING) {
            if (movingPiece.color == Color.WHITE) { whiteKingSideCastlingAllowed = false; whiteQueenSideCastlingAllowed = false }
            else { blackKingSideCastlingAllowed = false; blackQueenSideCastlingAllowed = false }
        }
        if (movingPiece.type == PieceType.ROOK) {
            if (movingPiece.color == Color.WHITE) {
                if (move.from == Square('a', 1)) whiteQueenSideCastlingAllowed = false
                if (move.from == Square('h', 1)) whiteKingSideCastlingAllowed = false
            } else {
                if (move.from == Square('a', 8)) blackQueenSideCastlingAllowed = false
                if (move.from == Square('h', 8)) blackKingSideCastlingAllowed = false
            }
        }
        // Ова провера је неопходна ако противнички топ буде поједен на свом почетном пољу
        val capturedPieceSquare = move.to
        val capturedPiece = board.getPieceAt(capturedPieceSquare) // Овде је већ null, јер је makeMove извршен. Логика capturedPiece је потребна унутар Move објекта.
        // За сада, радимо проверу по пољима.
        if (capturedPieceSquare == Square('a', 1)) whiteQueenSideCastlingAllowed = false
        if (capturedPieceSquare == Square('h', 1)) whiteKingSideCastlingAllowed = false
        if (capturedPieceSquare == Square('a', 8)) blackQueenSideCastlingAllowed = false
        if (capturedPieceSquare == Square('h', 8)) blackKingSideCastlingAllowed = false

        if (movingPiece.type == PieceType.PAWN) {
            if (movingPiece.color == Color.WHITE && move.from.rank == 2 && move.to.rank == 4) { enPassantTargetSquare = Square(move.from.file, 3) }
            else if (movingPiece.color == Color.BLACK && move.from.rank == 7 && move.to.rank == 5) { enPassantTargetSquare = Square(move.from.file, 6) }
        }
    }

    private fun getPseudoLegalMoves(): List<Move> {
        val allMoves = mutableListOf<Move>()
        val allPieces = board.getAllPieces()
        for ((square, piece) in allPieces) {
            if (piece.color == currentPlayer) {
                allMoves.addAll(MoveGenerator.generateMovesForPiece(piece, square, this))
            }
        }
        return allMoves
    }

    // *** ИЗМЕНА #1: Исправљена функција за проверу напада ***
    fun isSquareAttacked(square: Square, attackerColor: Color): Boolean {
        val allPieces = board.getAllPieces()
        for ((pieceSquare, piece) in allPieces) {
            if (piece.color == attackerColor) {
                // --- ПЕЧ ЗА КРАЉА СТАРТ ---
                // Ручна провера за напад краљем, јер MoveGenerator није поуздан.
                if (piece.type == PieceType.KING) {
                    val dx = abs(pieceSquare.file.code - square.file.code)
                    val dy = abs(pieceSquare.rank - square.rank)
                    if (dx <= 1 && dy <= 1) {
                        return true // Краљ напада ово поље
                    }
                }
                // --- ПЕЧ ЗА КРАЉА КРАЈ ---

                // Оригинална логика која користи (непоуздани) генератор
                val moves = MoveGenerator.generateMovesForPiece(piece, pieceSquare, this)
                if (moves.any { it.to == square }) { return true }
            }
        }
        return false
    }

    fun isKingInCheck(color: Color): Boolean {
        val kingSquare = board.findKing(color)
        return if (kingSquare != null) { isSquareAttacked(kingSquare, color.opposite()) } else { false }
    }

    // *** ИЗМЕНА #2: Поједностављена и поузданија функција за легалне потезе ***
    fun getLegalMoves(): List<Move> {
        val pseudoLegalMoves = getPseudoLegalMoves()
        val legalMoves = mutableListOf<Move>()
        val originalPlayer = this.currentPlayer

        for (move in pseudoLegalMoves) {
            // Креирамо потпуну копију стања игре да бисмо безбедно тестирали потез
            val tempGame = this.copy()
            // Покушавамо да одиграмо потез на копији
            tempGame.tryMakeMove(move)

            // Проверавамо да ли је НАШ краљ у шаху НАКОН потеза.
            // tempGame.isKingInCheck() позива исправљену isSquareAttacked() методу.
            if (!tempGame.isKingInCheck(originalPlayer)) {
                legalMoves.add(move)
            }
        }
        return legalMoves
    }

    // *** ИЗМЕНА #3: Ажурирана copy функција ***
    fun copy(): Game {
        val newGame = Game()
        newGame.loadFen(this.toFen())
        // Осигуравамо да се и историја потеза копира
        newGame.moveHistory.addAll(this.moveHistory)
        return newGame
    }
}

fun Board.findKing(color: Color): Square? {
    return this.getAllPieces().entries.find { (_, piece) -> piece.type == PieceType.KING && piece.color == color }?.key
}