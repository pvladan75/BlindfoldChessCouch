// model/Puzzle.kt
package com.program.blindfoldchesscouch.model

import com.google.gson.annotations.SerializedName

/**
 * Predstavlja jednu kompletnu slagalicu iz JSON fajla.
 */
data class Puzzle(
    @SerializedName("id") val id: Long,
    @SerializedName("pieces") val pieces: Map<String, Int>,
    @SerializedName("initial_fen") val initialFen: String,
    @SerializedName("solution") val solution: List<PuzzleStep>
)

/**
 * Predstavlja jedan korak (potez) u rešenju slagalice.
 */
data class PuzzleStep(
    @SerializedName("move") val moveNotation: String,
    @SerializedName("interacting_square") val interactingSquareNotation: String
)