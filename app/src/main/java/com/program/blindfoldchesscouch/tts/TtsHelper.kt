// tts/TtsHelper.kt
package com.program.blindfoldchesscouch.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Pomoćna klasa koja enkapsulira logiku za Android Text-to-Speech (TTS) engine.
 *
 * @param context Kontekst aplikacije, potreban za inicijalizaciju TTS-a.
 */
class TtsHelper(context: Context) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context, this)

    /**
     * Poziva se kada je TTS engine spreman.
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Postavljamo jezik na engleski, jer su koordinate na engleskom (npr. 'e4')
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    /**
     * Javna metoda za izgovaranje teksta.
     * @param text Tekst koji treba izgovoriti.
     */
    fun speak(text: String) {
        // Proveravamo da li je TTS spreman pre nego što pokušamo da ga koristimo
        if (tts.isSpeaking) {
            tts.stop()
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    /**
     * Metoda za oslobađanje resursa kada TTS više nije potreban.
     * Treba je pozvati iz ViewModel-ovog onCleared() metoda.
     */
    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}