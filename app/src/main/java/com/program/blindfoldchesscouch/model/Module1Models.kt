// model/Module1Models.kt
package com.program.blindfoldchesscouch.model

import androidx.compose.runtime.Stable

@Stable
sealed class QuizType(val title: String, val description: String) {
    object FindSquare : QuizType("Prepoznavanje polja", "Vežbajte brzo prepoznavanje polja na tabli.")
    object AdvancedTactics : QuizType("Napredni taktički kviz", "Rešavajte zadatke o napadnutim poljima i bojama polja.")
}

@Stable
sealed class AdvancedQuestion {
    abstract val questionText: String
    abstract val options: List<String>
    abstract val correctOptions: Set<String>
    abstract val board: Board

    data class AttackedSquaresQuestion(
        override val questionText: String,
        override val options: List<String>,
        override val correctOptions: Set<String>,
        override val board: Board
    ) : AdvancedQuestion()

    data class InterceptQuestion(
        override val questionText: String,
        override val options: List<String>,
        override val correctOptions: Set<String>,
        override val board: Board
    ) : AdvancedQuestion()

    data class SquareColorQuestion(
        override val questionText: String,
        override val options: List<String>,
        override val correctOptions: Set<String>,
        override val board: Board = Board()
    ) : AdvancedQuestion()
}