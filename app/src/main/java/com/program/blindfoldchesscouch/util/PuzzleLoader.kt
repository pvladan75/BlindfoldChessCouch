// util/PuzzleLoader.kt
package com.program.blindfoldchesscouch.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.program.blindfoldchesscouch.model.PieceType
import com.program.blindfoldchesscouch.model.Puzzle
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream

object PuzzleLoader {

    // Sada keširamo mapu gde je ključ ime fajla, a vrednost lista slagalica
    private val puzzlesCache = mutableMapOf<String, List<Puzzle>>()
    private const val PUZZLE_DIR = "puzzles" // Ime foldera u internoj memoriji

    /**
     * Glavna funkcija. Proverava da li su fajlovi raspakovani, pronalazi pravi JSON i vraća slagalicu.
     */
    fun findPuzzleFor(context: Context, pieceConfig: Map<PieceType, Int>): Puzzle? {
        try {
            // 1. Korak: Osiguraj da je ZIP raspakovan
            unzipPuzzlesIfNeeded(context)

            // 2. Korak: Sastavi ime fajla na osnovu izbora korisnika
            val filename = constructFilename(pieceConfig)

            // 3. Korak: Učitaj slagalice iz odgovarajućeg fajla
            val puzzles = loadPuzzlesFromFile(context, filename)

            // 4. Korak: Vrati nasumičnu slagalicu iz liste
            return puzzles.randomOrNull()

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Sastavlja ime fajla (npr. "puzzles_B1N1.json") iz mape figura.
     */
    private fun constructFilename(pieceConfig: Map<PieceType, Int>): String {
        // Filtriramo samo figure koje je korisnik izabrao (broj > 0)
        val parts = pieceConfig.filter { it.value > 0 }.map {
            // Pretvaramo npr. PieceType.KNIGHT u "N" i dodajemo broj
            val pieceChar = it.key.toChar().toString()
            "$pieceChar${it.value}"
        }

        // Sortiramo delove da bismo uvek dobili isti redosled (B1N1 umesto N1B1)
        val sortedParts = parts.sorted().joinToString("")

        return "puzzles_${sortedParts}.json"
    }

    /**
     * Učitava i parsira određeni JSON fajl iz interne memorije.
     * Koristi keširanje da se izbegne ponovno čitanje.
     */
    private fun loadPuzzlesFromFile(context: Context, filename: String): List<Puzzle> {
        // Proveri keš prvo
        if (puzzlesCache.containsKey(filename)) {
            return puzzlesCache[filename]!!
        }

        val puzzleFile = File(context.filesDir, "$PUZZLE_DIR/$filename")
        if (!puzzleFile.exists()) {
            println("Fajl ne postoji: $filename")
            return emptyList()
        }

        val jsonString = puzzleFile.bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<Puzzle>>() {}.type
        val puzzles: List<Puzzle> = Gson().fromJson(jsonString, listType)

        // Stavi u keš i vrati
        puzzlesCache[filename] = puzzles
        return puzzles
    }

    /**
     * Raspakuje puzzles.zip iz assets u internu memoriju aplikacije.
     * Radi samo jednom, pri prvom pokretanju.
     */
    private fun unzipPuzzlesIfNeeded(context: Context) {
        val puzzleDir = File(context.filesDir, PUZZLE_DIR)
        // Ako folder već postoji, preskoči raspakivanje
        if (puzzleDir.exists()) {
            return
        }

        println("Raspakujem puzzles.zip po prvi put...")
        puzzleDir.mkdirs() // Kreiraj folder

        try {
            context.assets.open("puzzles.zip").use { assetInputStream ->
                ZipInputStream(assetInputStream).use { zipInputStream ->
                    var entry = zipInputStream.nextEntry
                    while (entry != null) {
                        val file = File(puzzleDir, entry.name)
                        // Proveravamo da nije u pitanju folder
                        if (!entry.isDirectory) {
                            FileOutputStream(file).use { fileOutputStream ->
                                zipInputStream.copyTo(fileOutputStream)
                            }
                        }
                        entry = zipInputStream.nextEntry
                    }
                }
            }
            println("Raspakivanje završeno.")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}