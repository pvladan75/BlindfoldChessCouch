// У новом фајлу model/TutorialModels.kt
package com.program.blindfoldchesscouch.model

import androidx.annotation.StringRes
import com.program.blindfoldchesscouch.model.Square

// Definiše jedan segment unutar koraka tutorijala
sealed class TutorialSegment

// Segment koji sadrži samo tekst
data class TextSegment(val text: String) : TutorialSegment()

// Segment koji predstavlja akciju označavanja polja
data class HighlightSegment(val square: Square) : TutorialSegment()

// Segment koji predstavlja akciju brisanja svih oznaka
object ClearHighlightSegment : TutorialSegment()

// Segment koji predstavlja pauzu у kucanju
data class PauseSegment(val durationMillis: Long) : TutorialSegment()

// Definicija jednog koraka tutorijala (сада једноставнија)
data class TutorialStep(
    @StringRes val textResId: Int,
    val fen: String
)