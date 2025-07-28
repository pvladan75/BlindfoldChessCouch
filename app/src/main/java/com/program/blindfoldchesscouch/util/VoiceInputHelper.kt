// In util/VoiceInputHelper.kt
package com.program.blindfoldchesscouch.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * Pomoćna klasa koja upravlja Android Speech-to-Text funkcionalnošću.
 *
 * @param context Kontekst aplikacije.
 * @param onResult Lambda funkcija koja se poziva sa препознатим текстом.
 * @param onStateChange Lambda funkcija za praćenje stanja (npr. da li sluša или ne).
 */
class VoiceInputHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onStateChange: (Boolean) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            onStateChange(true) // Počelo je slušanje
            println("VOICE_INPUT: Spreman za govor...")
        }

        override fun onBeginningOfSpeech() {
            println("VOICE_INPUT: Korisnik je počeo da priča...")
        }

        override fun onResults(results: Bundle?) {
            onStateChange(false) // Slušanje je završeno
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                // Uzimamo prvi, najverovatniji rezultat
                val text = matches[0]
                println("VOICE_INPUT: Prepoznat tekst: $text")
                onResult(text) // Šaljemo rezultat nazad
            }
        }

        override fun onError(error: Int) {
            onStateChange(false) // Slušanje je završeno (sa greškom)
            println("VOICE_INPUT: Greška pri prepoznavanju: $error")
        }

        // Ostale metode nam trenutno ne trebaju, ali moraju biti tu
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            println("VOICE_INPUT: Prepoznavanje govora nije dostupno na ovom uređaju.")
            return
        }

        // Inicijalizujemo SpeechRecognizer ako već nije
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(recognitionListener)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // ИЗМЕНА: Језик је сада постављен на енглески
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        speechRecognizer?.startListening(intent)
    }

    fun destroy() {
        onStateChange(false)
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}