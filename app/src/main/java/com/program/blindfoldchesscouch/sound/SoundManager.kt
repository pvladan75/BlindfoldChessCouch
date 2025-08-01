// sound/SoundManager.kt
package com.program.blindfoldchesscouch.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.program.blindfoldchesscouch.R

class SoundManager(context: Context) {

    private var soundPool: SoundPool? = null
    private var typewriterSoundId: Int = 0
    private var isSoundLoaded = false

    init {
        // Kreiramo AudioAttributes da bismo definisali kako će se zvuk ponašati
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME) // Definišemo da je zvuk za igru/UI efekat
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // Kreiramo instancu SoundPool-a
        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Koliko zvukova može da se pusti istovremeno
            .setAudioAttributes(audioAttributes)
            .build()

        // Postavljamo listener koji će nas obavestiti kada je zvuk učitan i spreman
        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isSoundLoaded = true
            }
        }

        // Učitavamo naš typing.ogg fajl iz res/raw foldera
        typewriterSoundId = soundPool?.load(context, R.raw.typing, 1) ?: 0
    }

    // Funkcija koju ćemo pozivati da pustimo zvuk kucanja
    fun playTypeSound() {
        if (isSoundLoaded) {
            soundPool?.play(typewriterSoundId, 1f, 1f, 0, 0, 1f)
        }
    }

    // Veoma važna funkcija za oslobađanje resursa kada nam SoundManager više ne treba
    fun release() {
        soundPool?.release()
        soundPool = null
    }
}