// model/Game.kt
package com.program.blindfoldchesscouch.model

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

    fun getCurrentBoard(): Board {
        return board
    }

    /**
     * NOVO: Kreira FEN string na osnovu trenutnog stanja partije.
     */
    fun toFen(): String {
        val fenBuilder = StringBuilder()

        // 1. Deo: Pozicija figura
        for (rank in 8 downTo 1) {
            var emptySquares = 0
            for (file in 'a'..'h') {
                val piece = board.getPieceAt(Square(file, rank))
                if (piece == null) {
                    emptySquares++
                } else {
                    if (emptySquares > 0) {
                        fenBuilder.append(emptySquares)
                        emptySquares = 0
                    }
                    fenBuilder.append(piece.toFenChar())
                }
            }
            if (emptySquares > 0) {
                fenBuilder.append(emptySquares)
            }
            if (rank > 1) {
                fenBuilder.append('/')
            }
        }

        // 2. Deo: Igrač na potezu
        fenBuilder.append(' ').append(if (currentPlayer == Color.WHITE) 'w' else 'b')

        // 3. Deo: Prava na rokadu
        val castlingRights = StringBuilder()
        if (whiteKingSideCastlingAllowed) castlingRights.append('K')
        if (whiteQueenSideCastlingAllowed) castlingRights.append('Q')
        if (blackKingSideCastlingAllowed) castlingRights.append('k')
        if (blackQueenSideCastlingAllowed) castlingRights.append('q')
        fenBuilder.append(' ').append(if (castlingRights.isNotEmpty()) castlingRights.toString() else "-")

        // 4. Deo: En passant polje
        fenBuilder.append(' ').append(enPassantTargetSquare?.toAlgebraicNotation() ?: "-")

        // 5. i 6. Deo: Brojači poteza
        fenBuilder.append(' ').append(halfMoveClock)
        fenBuilder.append(' ').append(fullMoveNumber)

        return fenBuilder.toString()
    }


    fun tryMakeMove(move: Move): Boolean {
        val pieceToMove = board.getPieceAt(move.from)
        if (pieceToMove == null || pieceToMove.color != currentPlayer) {
            return false
        }
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
        if (movingPiece.type == PieceType.PAWN || move.capturedPiece != null) { halfMoveClock = 0 } else { halfMoveClock++ }
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
        if (move.capturedPiece?.type == PieceType.ROOK) {
            if (move.to == Square('a', 1)) whiteQueenSideCastlingAllowed = false
            if (move.to == Square('h', 1)) whiteKingSideCastlingAllowed = false
            if (move.to == Square('a', 8)) blackQueenSideCastlingAllowed = false
            if (move.to == Square('h', 8)) blackKingSideCastlingAllowed = false
        }
        if (movingPiece.type == PieceType.PAWN) {
            if (movingPiece.color == Color.WHITE && move.from.rank == 2 && move.to.rank == 4) { enPassantTargetSquare = Square(move.from.file, 3) }
            else if (movingPiece.color == Color.BLACK && move.from.rank == 7 && move.to.rank == 5) { enPassantTargetSquare = Square(move.from.file, 6) }
        }
    }

    fun getPseudoLegalMoves(): List<Move> {
        val allMoves = mutableListOf<Move>(); val allPieces = board.getAllPieces()
        for ((square, piece) in allPieces) { if (piece.color == currentPlayer) { allMoves.addAll(MoveGenerator.generateMovesForPiece(piece, square, this)) } }; return allMoves
    }

    fun isSquareAttacked(square: Square, attackerColor: Color): Boolean {
        val allPieces = board.getAllPieces()
        for ((pieceSquare, piece) in allPieces) {
            if (piece.color == attackerColor) {
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

    fun getLegalMoves(): List<Move> {
        val pseudoLegalMoves = getPseudoLegalMoves()
        val legalMoves = mutableListOf<Move>()
        val originalPlayer = this.currentPlayer
        for (move in pseudoLegalMoves) {
            val tempGame = this.copyAndMakeMove(move)
            val kingSquare = tempGame.board.findKing(originalPlayer)
            if (kingSquare != null && !tempGame.isSquareAttacked(kingSquare, tempGame.currentPlayer)) {
                legalMoves.add(move)
            }
        }
        return legalMoves
    }

    private fun copyAndMakeMove(move: Move): Game {
        val newGame = Game(); newGame.board = this.board.copy(); newGame.currentPlayer = this.currentPlayer
        newGame.board.makeMove(move); newGame.currentPlayer = newGame.currentPlayer.opposite(); return newGame
    }
}

fun Board.findKing(color: Color): Square? {
    return this.getAllPieces().entries.find { (_, piece) -> piece.type == PieceType.KING && piece.color == color }?.key
}