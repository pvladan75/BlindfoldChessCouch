package com.program.nativelib

object NativeLib {

    init {
        System.loadLibrary("nativelib")
    }

    /**
     * Prima FEN poziciju i vreme za razmišljanje, i vraća najbolji potez.
     */
    external fun getBestMove(fen: String, searchTimeMillis: Int): String
}