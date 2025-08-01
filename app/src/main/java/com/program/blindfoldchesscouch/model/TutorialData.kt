// model/TutorialData.kt
package com.program.blindfoldchesscouch.model

import com.program.blindfoldchesscouch.R

data class TutorialTopic(
    val id: String,
    val title: String,
    val description: String
)

object TutorialRepository {

    val topics = listOf(
        TutorialTopic(
            id = "board_and_squares",
            title = "Osnove: Tabla i figure", // Malo smo promenili naslov
            description = "Naučite kako da vizuelizujete tablu, polja i kretanje figura."
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

    // *** ИЗМЕНА: Проширена листа са свим твојим новим корацима и FEN-овима ***
    val boardAndSquaresTutorial: List<TutorialStep> = listOf(
        TutorialStep(
            textResId = R.string.tutorial_step_0, // Uvodni korak
            fen = "8/8/8/8/8/8/8/8 w - - 0 1" // Prazna tabla za uvod
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_1,
            fen = "8/8/8/8/8/8/8/8 w - - 0 1" // Prazna tabla
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_2,
            fen = "8/8/8/8/8/8/8/8 w - - 0 1" // Prazna tabla
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_3,
            fen = "8/8/8/8/8/8/8/8 w - - 0 1" // Prazna tabla
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_4,
            fen = "8/8/8/8/8/8/8/8 w - - 0 1" // Top na c4
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_5,
            fen = "8/8/8/8/8/5B2/8/8 w - - 0 1" // Lovac na f3
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_6,
            fen = "8/8/8/3Q4/8/8/8/8 w - - 0 1" // Dama na d5
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_7,
            fen = "8/8/8/4N3/8/8/8/8 w - - 0 1" // Skakač na e5
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_8,
            fen = "8/8/8/8/8/8/4P3/8 w - - 0 1" // Beli pešak na e2
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_9,
            fen = "8/3p4/8/8/8/8/8/8 b - - 0 1" // Crni pešak na d7
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_10,
            fen = "8/8/8/8/8/8/8/8 w - - 0 1" // Prazna tabla za kraj
        )
    )

    val module1Tutorial: List<TutorialStep> = listOf(
        // TODO: Dodati korake za Modul 1
    )
    val module2Tutorial: List<TutorialStep> = listOf(
        // TODO: Dodati korake za Modul 2
    )

    fun getScriptById(topicId: String?): List<TutorialStep> {
        return when (topicId) {
            "board_and_squares" -> boardAndSquaresTutorial
            "module_1_guide" -> module1Tutorial
            "module_2_guide" -> module2Tutorial
            else -> boardAndSquaresTutorial
        }
    }
}