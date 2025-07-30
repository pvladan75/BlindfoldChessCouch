// in engine/SunfishEngine.kt
package com.program.blindfoldchesscouch.engine

import kotlin.math.abs
import kotlin.math.max

// Definišemo smerove van klase radi pregледnosti
private const val N = -10
private const val E = 1
private const val S = 10
private const val W = -1

/**
 * Овај објекат садржи све константе и почетна подешавања
 * за наш Sunfish енџин, преведене из Python кода.
 */
object SunfishEngineConfig {
    private val piece = mapOf(
        'P' to 100, 'N' to 280, 'B' to 320, 'R' to 479, 'Q' to 929, 'K' to 60000
    )
    private val pstSource = mapOf(
        'P' to intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 78, 83, 86, 73, 102, 82, 85, 90, 7, 29, 21, 44, 40, 31, 44, 7, -17, 16, -2, 15, 14, 0, 15, -13, -26, 3, 10, 9, 6, 1, 0, -23, -22, 9, 5, -11, -10, -2, 3, -19, -31, 8, -7, -37, -36, -14, 3, -31, 0, 0, 0, 0, 0, 0, 0, 0
        ),
        'N' to intArrayOf(
            -66, -53, -75, -75, -10, -55, -58, -70, -3, -6, 100, -36, 4, 62, -4, -14, 10, 67, 1, 74, 73, 27, 62, -2, 24, 24, 45, 37, 33, 41, 25, 17, -1, 5, 31, 21, 22, 35, 2, 0, -18, 10, 13, 22, 18, 15, 11, -14, -23, -15, 2, 0, 2, 0, -23, -20, -74, -23, -26, -24, -19, -35, -22, -69
        ),
        'B' to intArrayOf(
            -59, -78, -82, -76, -23, -107, -37, -50, -11, 20, 35, -42, -39, 31, 2, -22, -9, 39, -32, 41, 52, -10, 28, -14, 25, 17, 20, 34, 26, 25, 15, 10, 13, 10, 17, 23, 17, 16, 0, 7, 14, 25, 24, 15, 8, 25, 20, 15, 19, 20, 11, 6, 7, 6, 20, 16, -7, 2, -15, -12, -14, -15, -10, -10
        ),
        'R' to intArrayOf(
            35, 29, 33, 4, 37, 33, 56, 50, 55, 29, 56, 67, 55, 62, 34, 60, 19, 35, 28, 33, 45, 27, 25, 15, 0, 5, 16, 13, 18, -4, -9, -6, -28, -35, -16, -21, -13, -29, -46, -30, -42, -28, -42, -25, -25, -35, -26, -46, -53, -38, -31, -26, -29, -43, -44, -53, -30, -24, -18, 5, -2, -18, -31, -32
        ),
        'Q' to intArrayOf(
            6, 1, -8, -104, 69, 24, 88, 26, 14, 32, 60, -10, 20, 76, 57, 24, -2, 43, 32, 60, 72, 63, 43, 2, 1, -16, 22, 17, 25, 20, -13, -6, -14, -15, -2, -5, -1, -10, -20, -22, -30, -6, -13, -11, -16, -11, -16, -27, -36, -18, 0, -19, -15, -15, -21, -38, -39, -30, -31, -13, -31, -36, -34, -42
        ),
        'K' to intArrayOf(
            4, 54, 47, -99, -99, 60, 83, -62, -32, 10, 55, 56, 56, 55, 10, 3, -62, 12, -57, 44, -67, 28, 37, -31, -55, 50, 11, -4, -19, 13, 0, -49, -55, -43, -52, -28, -51, -47, -8, -50, -47, -42, -43, -79, -64, -32, -29, -32, -4, 3, -14, -50, -57, -18, 13, 4, 17, 30, -3, -14, 6, -1, 40, 18
        )
    )
    val pst: Map<Char, IntArray> = pstSource.mapValues { (key, table) ->
        val pieceValue = piece.getValue(key)
        val paddedTable = IntArray(120)
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                paddedTable[21 + i * 10 + j] = table[i * 8 + j] + pieceValue
            }
        }
        paddedTable
    }

    const val A1 = 91; const val H1 = 98; const val A8 = 21; const val H8 = 28

    val initialBoardString = "         \n         \n rnbqkbnr\n pppppppp\n ........\n ........\n ........\n ........\n PPPPPPPP\n RNBQKBNR\n         \n         \n".replace(" ", "")

    val directions = mapOf(
        'P' to listOf(N, N + N, N + W, N + E), 'N' to listOf(N + N + E, E + N + E, E + S + E, S + S + E, S + S + W, W + S + W, W + N + W, N + N + W), 'B' to listOf(N + E, S + E, S + W, N + W), 'R' to listOf(N, E, S, W), 'Q' to listOf(N, E, S, W, N + E, S + E, S + W, N + W), 'K' to listOf(N, E, S, W, N + E, S + E, S + W, N + W)
    )

    // Konstante za pretragu
    val MATE_LOWER = piece.getValue('K') - 10 * piece.getValue('Q')
    val MATE_UPPER = piece.getValue('K') + 10 * piece.getValue('Q')
}

// --- Klase za logiku ---

data class SunfishMove(val i: Int, val j: Int, val prom: Char? = null)

data class SunfishPosition(
    val board: CharArray, val score: Int, val wc: Pair<Boolean, Boolean>, val bc: Pair<Boolean, Boolean>, val ep: Int, val kp: Int
) {
    fun genMoves(): List<SunfishMove> { /* ... (nepromenjeno) ... */ return emptyList() }
    fun rotate(): SunfishPosition { /* ... (nepromenjeno) ... */ return this }
    fun move(move: SunfishMove): SunfishPosition { /* ... (nepromenjeno) ... */ return this }
    fun value(move: SunfishMove): Int { /* ... (nepromenjeno) ... */ return 0 }
    private fun Char.swapCase(): Char = if (isUpperCase()) lowercaseChar() else uppercaseChar()
}

// --- NOVO: Klasa za pretragu ---

/**
 * Predstavlja unos u transpozicionoj tabeli.
 */
private data class TTEntry(val lower: Int, val upper: Int)

/**
 * Glavna klasa za pretragu, "mozak" engine-a.
 */
class SunfishSearcher {
    private var nodes = 0
    private var history = setOf<SunfishPosition>()
    private val ttScore = mutableMapOf<Pair<SunfishPosition, Int>, TTEntry>()
    private val ttMove = mutableMapOf<SunfishPosition, SunfishMove>()

    /**
     * Glavna funkcija za pretragu, koristi alfa-beta algoritam.
     */
    private fun bound(pos: SunfishPosition, gamma: Int, depth: Int, canNull: Boolean = true): Int {
        nodes++
        val d = max(depth, 0)

        if (pos.score <= -SunfishEngineConfig.MATE_LOWER) return -SunfishEngineConfig.MATE_UPPER

        val entry = ttScore.getOrDefault(Pair(pos, d), TTEntry(-SunfishEngineConfig.MATE_UPPER, SunfishEngineConfig.MATE_UPPER))
        if (entry.lower >= gamma) return entry.lower
        if (entry.upper < gamma) return entry.upper

        if (d > 0 && canNull && pos in history) return 0

        var best = -SunfishEngineConfig.MATE_UPPER

        // Generišemo i sortiramo poteze
        val moves = pos.genMoves().sortedByDescending { pos.value(it) }
        val killer = ttMove[pos]

        // Null-move pruning (preskakanje poteza)
        if (d > 2 && canNull && abs(pos.score) < 500) {
            val nullScore = -bound(pos.rotate(), 1 - gamma, d - 3, canNull = false)
            best = max(best, nullScore)
            if (best >= gamma) return best
        }

        // Pretraga poteza
        for (move in (listOfNotNull(killer) + moves).distinct()) {
            val score = -bound(pos.move(move), 1 - gamma, d - 1)
            if (score > best) {
                best = score
                if (best >= gamma) {
                    ttMove[pos] = move
                    break
                }
            }
        }

        // Provera za pat i mat
        if (d > 2 && best == -SunfishEngineConfig.MATE_UPPER) {
            val flipped = pos.rotate()
            val inCheck = bound(flipped, SunfishEngineConfig.MATE_UPPER, 0) == SunfishEngineConfig.MATE_UPPER
            best = if (inCheck) -SunfishEngineConfig.MATE_LOWER else 0
        }

        // Čuvanje rezultata u transpozicionu tabelu
        if (best >= gamma) ttScore[Pair(pos, d)] = TTEntry(best, entry.upper)
        if (best < gamma) ttScore[Pair(pos, d)] = TTEntry(entry.lower, best)

        return best
    }

    /**
     * Glavna funkcija koja se poziva spolja. Koristi iterativno produbljivanje.
     * @param history Lista prethodnih pozicija da bi se izbeglo ponavljanje.
     * @return Sekvenca rezultata (dubina, najbolji potez).
     */
    fun search(history: List<SunfishPosition>, timeLimitSec: Double = 1.0): Pair<SunfishMove?, Int> {
        nodes = 0
        this.history = history.toSet()
        ttScore.clear()
        ttMove.clear()

        var bestMove: SunfishMove? = null
        var score = 0

        val startTime = System.currentTimeMillis()

        // Iterativno produbljivanje
        for (depth in 1..100) { // Limitiramo dubinu da ne bi trajalo večno
            score = bound(history.last(), 0, depth, canNull = false)
            bestMove = ttMove[history.last()]

            // Proveri da li je vreme isteklo
            if ((System.currentTimeMillis() - startTime) / 1000.0 > timeLimitSec) {
                break
            }
        }

        return Pair(bestMove, score)
    }
}
/**
 * NOVO: Glavna klasa koja služi као "most" između naše aplikacije i Sunfish logike.
 */
class SunfishEngine {
    private val searcher = SunfishSearcher()
    private var history: List<SunfishPosition>

    init {
        // Inicijalizujemo tablu sa početnom pozicijom
        val initialBoard = SunfishEngineConfig.initialBoardString.toCharArray()
        val initialPos = SunfishPosition(
            board = initialBoard,
            score = 0,
            wc = Pair(true, true),
            bc = Pair(true, true),
            ep = 0,
            kp = 0
        )
        history = listOf(initialPos)
    }

    /**
     * Postavlja poziciju na osnovu FEN stringa.
     */
    fun setPositionFromFen(fen: String) {
        // TODO: Implementirati punu logiku za konverziju FEN-a u SunfishPosition
        // Za sada, ova funkcija samo resetuje istoriju na početnu poziciju
        // da bismo izbegli greške. Ovo ćemo morati da unapredimo.
        val initialBoard = SunfishEngineConfig.initialBoardString.toCharArray()
        val initialPos = SunfishPosition(
            board = initialBoard,
            score = 0,
            wc = Pair(true, true),
            bc = Pair(true, true),
            ep = 0,
            kp = 0
        )
        history = listOf(initialPos)
    }

    /**
     * Pronalazi najbolji potez za trenutnu poziciju.
     * @return Potez u "long algebraic notation" formatu (npr. "g1f3").
     */
    fun searchBestMove(timeLimitSec: Double = 1.0): String? {
        val (bestMove, _) = searcher.search(history, timeLimitSec)

        if (bestMove != null) {
            // Primenjujemo potez na našu internu istoriju
            history = history + listOf(history.last().move(bestMove))

            // Konvertujemo indekse (0-119) u algebarsku notaciju (a1, h8)
            val from = render(bestMove.i)
            val to = render(bestMove.j)
            return "$from$to${bestMove.prom?.lowercase() ?: ""}"
        }
        return null
    }

    // Pomoćna funkcija za konverziju indeksa u notaciju
    private fun render(i: Int): String {
        val rank = (i - SunfishEngineConfig.A1) / 10
        val file = (i - SunfishEngineConfig.A1) % 10
        return "${'a' + file}${-rank + 1}"
    }
}