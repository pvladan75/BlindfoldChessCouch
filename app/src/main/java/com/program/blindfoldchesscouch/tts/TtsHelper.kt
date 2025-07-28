// tts/TtsHelper.kt
package com.program.blindfoldchesscouch.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    fun speak(text: String) {
        if (tts.isSpeaking) {
            tts.stop()
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    /**
     * Sets the speech rate.
     * @param speed A value from 0.1 (very slow) to 2.0 (very fast). Normal is 1.0f.
     */
    fun setSpeechSpeed(speed: Float) {
        tts.setSpeechRate(speed)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}