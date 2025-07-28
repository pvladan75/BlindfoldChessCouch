// model/Game.kt
package com.program.blindfoldchesscouch.model

/**
 * Represents the current state of a chess game.
 */
class Game {
    private var board: Board = Board()
    var currentPlayer: Color = Color.WHITE
        private set

    private val moveHistory: MutableList<Move> = mutableListOf()

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
        if (movingPiece.type == PieceType.PAWN || move.capturedPiece != null) {
            halfMoveClock = 0
        } else {
            halfMoveClock++
        }
        if (movingPiece.type == PieceType.KING) {
            if (movingPiece.color == Color.WHITE) {
                whiteKingSideCastlingAllowed = false
                whiteQueenSideCastlingAllowed = false
            } else {
                blackKingSideCastlingAllowed = false
                blackQueenSideCastlingAllowed = false
            }
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
            if (movingPiece.color == Color.WHITE && move.from.rank == 2 && move.to.rank == 4) {
                enPassantTargetSquare = Square(move.from.file, 3)
            } else if (movingPiece.color == Color.BLACK && move.from.rank == 7 && move.to.rank == 5) {
                enPassantTargetSquare = Square(move.from.file, 6)
            }
        }
    }

    fun getPseudoLegalMoves(): List<Move> {
        val allMoves = mutableListOf<Move>()
        val allPieces = board.getAllPieces()
        for ((square, piece) in allPieces) {
            if (piece.color == currentPlayer) {
                allMoves.addAll(MoveGenerator.generateMovesForPiece(piece, square, this))
            }
        }
        return allMoves
    }

    /**
     * Proverava da li je dato polje napadnuto od strane igrača date boje.
     */
    fun isSquareAttacked(square: Square, attackerColor: Color): Boolean {
        val allPieces = board.getAllPieces()
        for ((pieceSquare, piece) in allPieces) {
            if (piece.color == attackerColor) {
                val moves = MoveGenerator.generateMovesForPiece(piece, pieceSquare, this)
                if (moves.any { it.to == square }) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Proverava da li je kralj igrača date boje trenutno u šahu.
     */
    fun isKingInCheck(color: Color): Boolean {
        val kingSquare = board.findKing(color)
        return if (kingSquare != null) {
            isSquareAttacked(kingSquare, color.opposite())
        } else {
            false
        }
    }

    /**
     * Vraća listu svih potpuno legalnih poteza za trenutnog igrača.
     */
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
        val newGame = Game()
        newGame.board = this.board.copy()
        newGame.currentPlayer = this.currentPlayer
        newGame.board.makeMove(move)
        newGame.currentPlayer = newGame.currentPlayer.opposite()
        return newGame
    }
} // <-- КРАЈ Game КЛАСЕ

/**
 * Proširujemo Board klasu sa pomoćnom funkcijom za pronalaženje kralja.
 */
fun Board.findKing(color: Color): Square? {
    return this.getAllPieces().entries.find { (_, piece) ->
        piece.type == PieceType.KING && piece.color == color
    }?.key
}