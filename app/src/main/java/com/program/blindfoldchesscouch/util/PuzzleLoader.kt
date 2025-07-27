// util/PuzzleLoader.kt
package com.program.blindfoldchesscouch.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.program.blindfoldchesscouch.model.PieceType
import com.program.blindfoldchesscouch.model.Puzzle
import java.io.IOException

object PuzzleLoader {

    // Keširana (privremeno sačuvana) lista slagalica da ne bismo čitali veliki fajl svaki put
    private var cachedPuzzles: List<Puzzle>? = null

    /**
     * Učitava sve slagalice iz puzzles.json fajla u assets folderu.
     * Koristi keširanje da bi se izbeglo ponovno čitanje fajla ako je već učitan.
     */
    private fun loadAllPuzzles(context: Context): List<Puzzle> {
        // Ako su slagalice već učitane u memoriju, samo ih vrati
        cachedPuzzles?.let { return it }

        return try {
            // Otvori stream ka puzzles.json fajlu unutar assets foldera
            val jsonString = context.assets.open("puzzles.json")
                .bufferedReader()
                .use { it.readText() }

            // Koristi Gson biblioteku da automatski konvertuje JSON string u listu Puzzle objekata
            val listType = object : TypeToken<List<Puzzle>>() {}.type
            val puzzles: List<Puzzle> = Gson().fromJson(jsonString, listType)

            // Sačuvaj učitane slagalice u keš za buduću upotrebu i vrati ih
            cachedPuzzles = puzzles
            puzzles
        } catch (ioException: IOException) {
            // U slučaju greške (npr. fajl ne postoji), ispiši grešku i vrati praznu listu
            ioException.printStackTrace()
            emptyList()
        }
    }

    /**
     * Pronalazi nasumičnu slagalicu koja odgovara izabranoj kombinaciji figura.
     *
     * @param context Kontekst aplikacije.
     * @param pieceConfig Mapa koja opisuje koliko je koje figure korisnik izabrao
     * (npr. {PieceType.KNIGHT=1, PieceType.BISHOP=1}).
     * @return Slučajno odabrana Puzzle koja odgovara kriterijumu, ili null ako nijedna nije pronađena.
     */
    fun findPuzzleFor(context: Context, pieceConfig: Map<PieceType, Int>): Puzzle? {
        val allPuzzles = loadAllPuzzles(context)

        // Konvertujemo našu pieceConfig mapu u format koji odgovara onom u JSON-u
        // Primer: {PieceType.KNIGHT=1} -> {"N"=1}
        val targetJsonPieces = pieceConfig.mapKeys { it.key.toChar().toString() }

        // Filtriramo sve slagalice i tražimo one koje imaju identičnu 'pieces' mapu
        val matchingPuzzles = allPuzzles.filter { puzzle ->
            puzzle.pieces == targetJsonPieces
        }

        // Iz liste pronađenih, vraćamo jedan nasumični element, ili null ako je lista prazna
        return matchingPuzzles.randomOrNull()
    }
}