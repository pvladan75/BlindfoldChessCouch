// Ispravna lokacija: app/src/test/java/com/program/blindfoldchesscouch/engine/SunfishEngineTest.kt
package com.program.blindfoldchesscouch.engine

import com.program.blindfoldchesscouch.model.Game
import com.program.blindfoldchesscouch.model.Move
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SunfishEngineTest {

    private val engine = SunfishEngine()

    @Test
    fun `engine should not choose a move that leaves king in check`() = runBlocking {
        val game = Game()
        game.loadFen("8/4k3/8/3Q4/4n3/8/8/7K b - - 3 2")
        val legalMoves = game.getLegalMoves()

        val engineMoveStr = engine.searchBestMove()
        assertNotNull("Engine mora da pronađe potez.", engineMoveStr)

        val isMoveInLegalList = legalMoves.any { it.toAlgebraicNotation() == engineMoveStr }
        assertTrue("Potez koji je engine vratio mora biti u listi legalnih poteza.", isMoveInLegalList)
    }

    @Test
    fun `engine should find the only legal move in a critical position`() = runBlocking {
        val game = Game()
        game.loadFen("8/8/8/8/8/2K5/1Q6/1k6 b - - 14 9")
        val legalMoves = game.getLegalMoves()

        val engineMoveStr = engine.searchBestMove()

        assertNotNull("Engine mora da pronađe potez.", engineMoveStr)
        assertEquals("Jedini legalan potez je uzimanje dame.", "b1b2", engineMoveStr)
    }

    @Test
    fun `engine should not move king into an attacked square`() = runBlocking {
        val game = Game()
        game.loadFen("8/8/8/6N1/1B6/8/1K2k3/8 b - - 3 2")
        val legalMoves = game.getLegalMoves()

        val engineMoveStr = engine.searchBestMove()

        assertNotNull("Engine mora da pronađe potez.", engineMoveStr)
        val isMoveInLegalList = legalMoves.any { it.toAlgebraicNotation() == engineMoveStr }
        assertTrue("Engine ne sme da odigra potez na napadnuto polje d2.", isMoveInLegalList)
    }

    @Test
    fun `engine should return null when checkmated`() = runBlocking {
        val game = Game()
        game.loadFen("1k2Q3/8/1K6/8/8/8/8/8 b - - 13 7")
        val legalMoves = game.getLegalMoves()

        assertEquals("U mat poziciji ne sme biti legalnih poteza.", 0, legalMoves.size)

        val engineMoveStr = engine.searchBestMove()
        assertEquals("U mat poziciji, engine treba da vrati null.", null, engineMoveStr)
    }

    private fun Move.toAlgebraicNotation(): String {
        return "${this.from.toAlgebraicNotation()}${this.to.toAlgebraicNotation()}"
    }
}