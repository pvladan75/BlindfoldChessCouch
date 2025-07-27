// model/Piece.kt
package com.program.blindfoldchesscouch.model

/**
 * Represents the color of a chess piece.
 */
enum class Color {
    WHITE,
    BLACK;

    /**
     * Returns the opposite color.
     */
    fun opposite(): Color {
        return when (this) {
            WHITE -> BLACK
            BLACK -> WHITE
        }
    }
}

/**
 * Represents the type of a chess piece.
 */
enum class PieceType {
    PAWN,    // Pešak
    KNIGHT,  // Skakač
    BISHOP,  // Lovac
    ROOK,    // Top
    QUEEN,   // Kraljica
    KING;    // Kralj

    /**
     * Returns the character representation of the piece type (e.g., 'P' for Pawn, 'N' for Knight).
     * For pawns, it returns 'P', for others, the first letter of their name.
     */
    fun toChar(): Char {
        return when (this) {
            PAWN -> 'P'
            KNIGHT -> 'N'
            BISHOP -> 'B'
            ROOK -> 'R'
            QUEEN -> 'Q'
            KING -> 'K'
        }
    }
}

/**
 * Represents a chess piece with its type and color.
 */
data class Piece(val type: PieceType, val color: Color) {

    /**
     * Returns the FEN (Forsyth-Edwards Notation) character for the piece.
     * Uppercase for White, lowercase for Black.
     * e.g., 'P' for White Pawn, 'q' for Black Queen.
     */
    fun toFenChar(): Char {
        val char = type.toChar()
        return if (color == Color.WHITE) char else char.lowercaseChar()
    }

    override fun toString(): String {
        return "${color.name} ${type.name}"
    }
}