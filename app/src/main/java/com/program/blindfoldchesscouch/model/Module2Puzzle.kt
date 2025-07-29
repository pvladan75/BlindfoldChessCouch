// in model/Module2Puzzle.kt
package com.program.blindfoldchesscouch.model

import com.google.gson.annotations.SerializedName

/**
 * Predstavlja jednu zagonetku za Modul 2, učitanu iz JSON fajla.
 */
data class Module2Puzzle(
    @SerializedName("id") val id: Int,
    @SerializedName("fen") val fen: String,
    @SerializedName("evaluation") val evaluation: String
)