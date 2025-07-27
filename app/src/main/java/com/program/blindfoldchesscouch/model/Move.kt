// model/Move.kt
package com.program.blindfoldchesscouch.model

/**
 * Represents a special type of move, like a castling or en passant.
 */
enum class MoveType {
    NORMAL,      // Regular piece move
    CASTLING_KING_SIDE, // Kraljeva rokada (O-O)
    CASTLING_QUEEN_SIDE, // Damina rokada (O-O-O)
    EN_PASSANT,  // En passant hvatanje
    PROMOTION    // Promocija pešaka
}

/**
 * Represents a chess move.
 * @param from The starting square of the piece.
 * @param to The destination square of the piece.
 * @param piece The piece that is moving. (Can be null if move is not yet linked to a piece)
 * @param capturedPiece The piece that was captured, if any. Null if no capture occurred.
 * @param moveType The type of the move (NORMAL, CASTLING, EN_PASSANT, PROMOTION).
 * @param promotionPieceType The type of piece the pawn promotes to, if moveType is PROMOTION. Null otherwise.
 */
data class Move(
    val from: Square,
    val to: Square,
    val piece: Piece? = null, // Can be set after move is validated or made
    val capturedPiece: Piece? = null,
    val moveType: MoveType = MoveType.NORMAL,
    val promotionPieceType: PieceType? = null // Only relevant for promotion moves
) {
    init {
        // Basic validation for promotion moves
        if (moveType == MoveType.PROMOTION) {
            require(piece?.type == PieceType.PAWN) { "Promotion move must involve a pawn." }
            require(promotionPieceType != null) { "Promotion move must specify a promotion piece type." }
            require(promotionPieceType !in listOf(PieceType.PAWN, PieceType.KING)) {
                "Pawn cannot promote to Pawn or King."
            }
            // Add checks for 'to' square rank (8th for White, 1st for Black) if needed
        }
    }

    /**
     * Returns a string representation of the move in algebraic notation.
     * This is a simplified representation and might not cover all FEN/SAN complexities.
     * Examples: "e2e4", "e7e8Q", "O-O", "O-O-O", "e5xf6" (for en passant, or general capture)
     */
    fun toAlgebraicNotation(): String {
        return when (moveType) {
            MoveType.CASTLING_KING_SIDE -> "O-O"
            MoveType.CASTLING_QUEEN_SIDE -> "O-O-O"
            MoveType.PROMOTION -> "${from.toAlgebraicNotation()}${to.toAlgebraicNotation()}${promotionPieceType?.toChar()}"
            else -> {
                val captureIndicator = if (capturedPiece != null) "x" else ""
                // A very simplified notation; in real SAN, it would include piece type and disambiguation
                "${from.toAlgebraicNotation()}${captureIndicator}${to.toAlgebraicNotation()}"
            }
        }
    }

    override fun toString(): String {
        return toAlgebraicNotation()
    }

    companion object {
        /**
         * Helper function to create a normal move.
         */
        fun normal(from: Square, to: Square, piece: Piece? = null, capturedPiece: Piece? = null): Move {
            return Move(from, to, piece, capturedPiece, MoveType.NORMAL)
        }

        /**
         * Helper function to create a king-side castling move.
         */
        fun kingSideCastling(): Move {
            // These squares are fixed for king-side castling
            return Move(Square('e', 1), Square('g', 1), moveType = MoveType.CASTLING_KING_SIDE) // White
            // Note: For Black, this would be ('e', 8) to ('g', 8)
            // You might want to pass the color or handle this based on whose turn it is
            // For now, assuming it's from white's perspective for simplicity or needs to be adjusted based on the board context
        }

        /**
         * Helper function to create a queen-side castling move.
         */
        fun queenSideCastling(): Move {
            // These squares are fixed for queen-side castling
            return Move(Square('e', 1), Square('c', 1), moveType = MoveType.CASTLING_QUEEN_SIDE) // White
            // Similar note as kingSideCastling for black
        }

        /**
         * Helper function to create an en passant move.
         * The 'capturedPiece' in an en passant is typically a pawn on the 'to' square's rank and 'from' square's file.
         */
        fun enPassant(from: Square, to: Square, piece: Piece, capturedPawn: Piece): Move {
            return Move(from, to, piece, capturedPawn, MoveType.EN_PASSANT)
        }

        /**
         * Helper function to create a promotion move.
         */
        fun promotion(from: Square, to: Square, piece: Piece, promotionTo: PieceType, capturedPiece: Piece? = null): Move {
            return Move(from, to, piece, capturedPiece, MoveType.PROMOTION, promotionTo)
        }
    }
}