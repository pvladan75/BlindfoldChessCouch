// model/TutorialData.kt
package com.program.blindfoldchesscouch.model

import com.program.blindfoldchesscouch.R

// Novi model koji predstavlja jednu temu u meniju za uputstva
data class TutorialTopic(
    val id: String,
    val title: String,
    val description: String
)

object TutorialRepository {

    // Lista svih dostupnih tema koje ćemo prikazati u meniju
    val topics = listOf(
        TutorialTopic(
            id = "board_and_squares",
            title = "Osnove: Tabla i polja",
            description = "Naučite kako da vizuelizujete tablu i imenujete svako polje."
        ),
        TutorialTopic(
            id = "module_1_guide",
            title = "Uputstvo za Modul 1",
            description = "Kako funkcioniše trening vizuelizacije figura."
        ),
        TutorialTopic(
            id = "module_2_guide",
            title = "Uputstvo za Modul 2",
            description = "Kako da igrate protiv šahovskog endžina."
        )
    )

    // Scenario za prvu temu
    val boardAndSquaresTutorial: List<TutorialStep> = listOf(
        TutorialStep(
            textResId = R.string.tutorial_step_1,
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_2,
            fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_3,
            fen = "r1bqkbnr/pppppppp/2n5/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 1 2"
        )
    )

    // Ovde ćemo dodati scenarije za ostale tutorijale
    val module1Tutorial: List<TutorialStep> = listOf(
        // TODO: Dodati korake za Modul 1
    )
    val module2Tutorial: List<TutorialStep> = listOf(
        // TODO: Dodati korake za Modul 2
    )

    // Funkcija koja vraća pravi scenario na osnovu ID-ja
    fun getScriptById(topicId: String?): List<TutorialStep> {
        return when (topicId) {
            "board_and_squares" -> boardAndSquaresTutorial
            "module_1_guide" -> module1Tutorial
            "module_2_guide" -> module2Tutorial
            else -> boardAndSquaresTutorial // Vrati osnovni ako je greška
        }
    }
}