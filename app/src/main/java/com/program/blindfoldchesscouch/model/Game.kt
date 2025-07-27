// model/Game.kt
package com.program.blindfoldchesscouch.model

/**
 * Represents the current state of a chess game, primarily for training purposes,
 * without full validation of complex chess rules like check, checkmate, or castling legality.
 * Focuses on board state, current player, and move history.
 */
class Game {
    private val board: Board = Board()
    var currentPlayer: Color = Color.WHITE
        private set // Only mutable within the Game class

    private val moveHistory: MutableList<Move> = mutableListOf()

    // These flags will still exist for potential future use or basic display,
    // but their updates will be simplified as we won't validate castling moves in detail.
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

    /**
     * Resets the game to the standard starting position.
     */
    fun resetGame() {
        board.setupStartingPosition()
        currentPlayer = Color.WHITE
        moveHistory.clear()
        whiteKingSideCastlingAllowed = true // Reset to true
        whiteQueenSideCastlingAllowed = true
        blackKingSideCastlingAllowed = true
        blackQueenSideCastlingAllowed = true
        enPassantTargetSquare = null
        halfMoveClock = 0
        fullMoveNumber = 1
    }

    /**
     * Returns the current state of the board.
     */
    fun getCurrentBoard(): Board {
        return board
    }

    /**
     * Attempts to make a move. This simplified version performs basic checks
     * (e.g., piece ownership) but *does not validate complex chess rules*
     * like check, checkmate, castling legality, or path obstructions for sliding pieces.
     * It relies on Board.makeMove() for physical board changes.
     *
     * @param move The move to attempt.
     * @return True if the move was "syntactically" valid and made, false otherwise.
     */
    fun tryMakeMove(move: Move): Boolean {
        val pieceToMove = board.getPieceAt(move.from)

        // Basic validation: Check if there's a piece at 'from' square and if it belongs to current player
        if (pieceToMove == null || pieceToMove.color != currentPlayer) {
            println("Invalid move: No piece or not current player's piece at ${move.from}")
            return false
        }

        // For training modules, we might assume the moves provided are 'intended' moves,
        // and focus on visualizing their outcome.
        val success = board.makeMove(move)

        if (success) {
            moveHistory.add(move) // Add to history only if move was successful

            // Update simplified game state based on the move
            updateGameState(move, movingPiece = pieceToMove)

            // Switch current player
            currentPlayer = currentPlayer.opposite()

            // Increment full move number if Black just moved
            if (currentPlayer == Color.WHITE) {
                fullMoveNumber++
            }
            return true
        }
        return false
    }

    /**
     * Updates internal game state variables after a move is made.
     * This includes simplified updates for castling rights, en passant target, half-move clock.
     * Complex castling/check logic is omitted.
     */
    private fun updateGameState(move: Move, movingPiece: Piece) {
        // Reset en passant target square at start of turn
        enPassantTargetSquare = null

        // Handle half-move clock
        if (movingPiece.type == PieceType.PAWN || move.capturedPiece != null) {
            halfMoveClock = 0 // Reset if pawn moves or capture occurs
        } else {
            halfMoveClock++ // Increment otherwise
        }

        // --- Simplified update for castling rights ---
        if (movingPiece.type == PieceType.KING) {
            when (movingPiece.color) {
                Color.WHITE -> {
                    whiteKingSideCastlingAllowed = false
                    whiteQueenSideCastlingAllowed = false
                }
                Color.BLACK -> {
                    blackKingSideCastlingAllowed = false
                    blackQueenSideCastlingAllowed = false
                }
            }
        }
        if (movingPiece.type == PieceType.ROOK) {
            when (movingPiece.color) {
                Color.WHITE -> {
                    if (move.from == Square('a', 1)) whiteQueenSideCastlingAllowed = false
                    if (move.from == Square('h', 1)) whiteKingSideCastlingAllowed = false
                }
                Color.BLACK -> {
                    if (move.from == Square('a', 8)) blackQueenSideCastlingAllowed = false
                    if (move.from == Square('h', 8)) blackKingSideCastlingAllowed = false
                }
            }
        }
        if (move.capturedPiece?.type == PieceType.ROOK) {
            if (move.to == Square('a', 1)) whiteQueenSideCastlingAllowed = false
            if (move.to == Square('h', 1)) whiteKingSideCastlingAllowed = false
            if (move.to == Square('a', 8)) blackQueenSideCastlingAllowed = false
            if (move.to == Square('h', 8)) blackKingSideCastlingAllowed = false
        }


        // Handle en passant target square (if a pawn moves two squares)
        if (movingPiece.type == PieceType.PAWN) {
            if (movingPiece.color == Color.WHITE && move.from.rank == 2 && move.to.rank == 4) {
                enPassantTargetSquare = Square(move.from.file, 3)
            } else if (movingPiece.color == Color.BLACK && move.from.rank == 7 && move.to.rank == 5) {
                enPassantTargetSquare = Square(move.from.file, 6)
            }
        }
    }

    /**
     * Generiše sve pseudo-legalne poteze za trenutnog igrača.
     * Koristi MoveGenerator za izračunavanje poteza za svaku figuru.
     */
    fun getPseudoLegalMoves(): List<Move> {
        val allMoves = mutableListOf<Move>()
        val allPieces = board.getAllPieces() // Dobijamo mapu svih figura

        for ((square, piece) in allPieces) {
            // Generišemo poteze samo za figure igrača koji je na potezu
            if (piece.color == currentPlayer) {
                // Prosleđujemo ceo 'game' objekat (this) našem generatoru
                allMoves.addAll(MoveGenerator.generateMovesForPiece(piece, square, this))
            }
        }

        // Možemo ostaviti logovanje za debagovanje
        // println("Generated ${allMoves.size} moves for $currentPlayer.")
        // allMoves.forEach { println(it) }

        return allMoves
    }
}