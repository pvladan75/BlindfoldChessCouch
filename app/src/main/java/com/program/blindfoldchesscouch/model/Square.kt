// model/Square.kt
package com.program.blindfoldchesscouch.model

/**
 * Represents a single square on the chessboard.
 * @param file The file (column) of the square, 'a' to 'h'.
 * @param rank The rank (row) of the square, 1 to 8.
 */
data class Square(val file: Char, val rank: Int) {

    init {
        require(file in 'a'..'h') { "File must be between 'a' and 'h'." }
        require(rank in 1..8) { "Rank must be between 1 and 8." }
    }

    /**
     * Returns the algebraic notation of the square (e.g., "e4").
     */
    fun toAlgebraicNotation(): String {
        return "$file$rank"
    }

    override fun toString(): String {
        return toAlgebraicNotation()
    }

    companion object {
        /**
         * Creates a Square from its algebraic notation string (e.g., "e4").
         * Returns null if the notation is invalid.
         */
        fun fromAlgebraicNotation(notation: String): Square? {
            if (notation.length != 2) return null
            val file = notation[0]
            val rank = notation[1].digitToIntOrNull() // Ovo vraća Int?

            // Proveravamo da li je rank non-null i da li je unutar opsega pre kreiranja Square objekta
            if (file in 'a'..'h' && rank != null && rank in 1..8) { // Dodata provera rank != null
                return Square(file, rank) // Sada je 'rank' garantovano Int, a ne Int?
            }
            return null
        }
    }
}