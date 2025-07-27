// model/Board.kt
package com.program.blindfoldchesscouch.model

/**
 * Represents the chessboard and the positions of all pieces on it.
 */
class Board {
    // A map to store pieces, where the key is the Square and the value is the Piece.
    private val pieces: MutableMap<Square, Piece> = mutableMapOf()

    /**
     * Initializes the board to the standard starting chess position.
     */
    fun setupStartingPosition() {
        pieces.clear()

        // Pawns
        for (fileChar in 'a'..'h') {
            pieces[Square(fileChar, 2)] = Piece(PieceType.PAWN, Color.WHITE)
            pieces[Square(fileChar, 7)] = Piece(PieceType.PAWN, Color.BLACK)
        }

        // Rooks
        pieces[Square('a', 1)] = Piece(PieceType.ROOK, Color.WHITE)
        pieces[Square('h', 1)] = Piece(PieceType.ROOK, Color.WHITE)
        pieces[Square('a', 8)] = Piece(PieceType.ROOK, Color.BLACK)
        pieces[Square('h', 8)] = Piece(PieceType.ROOK, Color.BLACK)

        // Knights
        pieces[Square('b', 1)] = Piece(PieceType.KNIGHT, Color.WHITE)
        pieces[Square('g', 1)] = Piece(PieceType.KNIGHT, Color.WHITE)
        pieces[Square('b', 8)] = Piece(PieceType.KNIGHT, Color.BLACK)
        pieces[Square('g', 8)] = Piece(PieceType.KNIGHT, Color.BLACK)

        // Bishops
        pieces[Square('c', 1)] = Piece(PieceType.BISHOP, Color.WHITE)
        pieces[Square('f', 1)] = Piece(PieceType.BISHOP, Color.WHITE)
        pieces[Square('c', 8)] = Piece(PieceType.BISHOP, Color.BLACK)
        pieces[Square('f', 8)] = Piece(PieceType.BISHOP, Color.BLACK)

        // Queens
        pieces[Square('d', 1)] = Piece(PieceType.QUEEN, Color.WHITE)
        pieces[Square('d', 8)] = Piece(PieceType.QUEEN, Color.BLACK)

        // Kings
        pieces[Square('e', 1)] = Piece(PieceType.KING, Color.WHITE)
        pieces[Square('e', 8)] = Piece(PieceType.KING, Color.BLACK)
    }

    /**
     * Returns the piece at the given square, or null if the square is empty.
     */
    fun getPieceAt(square: Square): Piece? {
        return pieces[square]
    }

    /**
     * Places a piece on the board at a specific square.
     * If there's already a piece on that square, it will be replaced.
     */
    fun placePiece(square: Square, piece: Piece) {
        pieces[square] = piece
    }

    /**
     * Removes a piece from the given square.
     */
    fun removePiece(square: Square) {
        pieces.remove(square)
    }

    /**
     * Moves a piece from one square to another, handling special move types.
     * Does not validate if the move is legal according to chess rules, only performs the physical changes on the board.
     *
     * @param move The Move object describing the action.
     * @return True if the move was successfully performed (i.e., there was a piece to move), false otherwise.
     */
    fun makeMove(move: Move): Boolean {
        val movingPiece = pieces[move.from]

        // Ensure there is a piece to move
        if (movingPiece == null) {
            println("No piece found at ${move.from} to make move: $move") // For debugging
            return false
        }

        // Handle different move types
        when (move.moveType) {
            MoveType.NORMAL -> {
                // Remove captured piece if any
                if (move.capturedPiece != null) {
                    pieces.remove(move.to)
                }
                pieces.remove(move.from)
                pieces[move.to] = movingPiece
            }
            MoveType.CASTLING_KING_SIDE -> {
                // Assuming move.from is king's start square and move.to is king's end square
                val color = movingPiece.color
                val rank = if (color == Color.WHITE) 1 else 8

                // Move King (e1 to g1 for White, e8 to g8 for Black)
                pieces.remove(Square('e', rank))
                pieces[Square('g', rank)] = Piece(PieceType.KING, color)

                // Move Rook (h1 to f1 for White, h8 to f8 for Black)
                pieces.remove(Square('h', rank))
                pieces[Square('f', rank)] = Piece(PieceType.ROOK, color)
            }
            MoveType.CASTLING_QUEEN_SIDE -> {
                // Assuming move.from is king's start square and move.to is king's end square
                val color = movingPiece.color
                val rank = if (color == Color.WHITE) 1 else 8

                // Move King (e1 to c1 for White, e8 to c8 for Black)
                pieces.remove(Square('e', rank))
                pieces[Square('c', rank)] = Piece(PieceType.KING, color)

                // Move Rook (a1 to d1 for White, a8 to d8 for Black)
                pieces.remove(Square('a', rank))
                pieces[Square('d', rank)] = Piece(PieceType.ROOK, color)
            }
            MoveType.EN_PASSANT -> {
                // Move the attacking pawn
                pieces.remove(move.from)
                pieces[move.to] = movingPiece

                // Remove the captured pawn (it's on the square "behind" the 'to' square from attacker's perspective)
                val capturedPawnRank = if (movingPiece.color == Color.WHITE) move.to.rank - 1 else move.to.rank + 1
                pieces.remove(Square(move.to.file, capturedPawnRank))
            }
            MoveType.PROMOTION -> {
                // Remove the pawn and place the new promoted piece
                // The 'promotionPieceType' in the Move object should be non-null for this move type.
                if (move.promotionPieceType == null) {
                    println("Promotion move requires promotionPieceType to be specified.") // For debugging
                    return false
                }
                pieces.remove(move.from)
                pieces[move.to] = Piece(move.promotionPieceType, movingPiece.color)
            }
        }
        return true
    }

    /**
     * Returns a map of all pieces currently on the board and their respective squares.
     */
    fun getAllPieces(): Map<Square, Piece> {
        return pieces.toMap() // Return a copy to prevent external modification
    }

    /**
     * Prints a textual representation of the board to the console.
     * Useful for debugging.
     */
    fun printBoard() {
        println("  a b c d e f g h")
        println(" +-----------------+")
        for (rank in 8 downTo 1) {
            print("$rank|")
            for (fileChar in 'a'..'h') {
                val square = Square(fileChar, rank)
                val piece = getPieceAt(square)
                val charToPrint = piece?.toFenChar() ?: '.'
                print("$charToPrint ")
            }
            println("|")
        }
        println(" +-----------------+")
    }

    /**
     * Clears all pieces from the board.
     */
    fun clearBoard() {
        pieces.clear()
    }
    /**
     * NOVO: Čisti tablu i postavlja figure na osnovu FEN stringa.
     * Podržava samo deo FEN-a koji se odnosi na poziciju figura.
     */
    fun loadFen(fen: String) {
        clearBoard() // Prvo očistimo tablu

        val fenPositionPart = fen.substringBefore(' ')
        var rank = 8
        var file = 'a'

        for (char in fenPositionPart) {
            when {
                char == '/' -> {
                    rank--
                    file = 'a'
                }
                char.isDigit() -> {
                    val emptySquares = char.digitToInt()
                    file = (file.code + emptySquares).toChar()
                }
                else -> {
                    val square = Square.fromAlgebraicNotation("$file$rank")
                    val piece = Piece.fromFenChar(char)
                    if (square != null && piece != null) {
                        placePiece(square, piece)
                    }
                    file = (file.code + 1).toChar()
                }
            }
        }
    }
}