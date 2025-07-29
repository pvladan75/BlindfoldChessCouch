// in util/Module2PuzzleLoader.kt
package com.program.blindfoldchesscouch.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.program.blindfoldchesscouch.model.Module2Puzzle
import java.io.IOException

object Module2PuzzleLoader {

    fun loadPuzzles(context: Context, difficulty: String): List<Module2Puzzle> {
        val fileName = when (difficulty) {
            "easy" -> "easy_puzzles.json"
            "medium" -> "medium_puzzles.json"
            "hard" -> "hard_puzzles.json"
            else -> return emptyList()
        }

        return try {
            // Čitamo fajl iz 'assets' foldera
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Module2Puzzle>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            // Vraćamo praznu listu ako fajl ne postoji ili dođe do greške
            emptyList()
        }
    }
}