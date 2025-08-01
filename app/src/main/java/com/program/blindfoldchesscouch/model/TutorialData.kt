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
            fen = "8/8/8/8/8/8/8/8 w - - 0 1"
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_5,
            fen = "8/8/8/8/8/8/8/8 w - - 0 1"
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_6,
            fen = "8/8/8/8/2R5/8/8/8 w - - 0 1"// Top na c4
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_7,
            fen = "8/8/8/8/8/4B3/8/8 w - - 0 1" // Lovac na e3
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_8,
            fen = "8/8/8/8/2B5/4B3/8/8 w - - 0 1" // Lovci na e3 i c4
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_9,
            fen = "8/8/8/8/2Q5/8/8/8 w - - 0 1" // Dama na c4
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_10,
            fen = "8/8/8/8/8/5N2/8/8 w - - 0 1" // Skakač na f3
        ),
        TutorialStep(
            textResId = R.string.tutorial_step_11,
            fen = "8/8/8/8/8/8/8/8 w - - 0 1" //
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