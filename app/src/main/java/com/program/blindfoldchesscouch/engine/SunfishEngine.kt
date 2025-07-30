// in engine/SunfishEngine.kt
package com.program.blindfoldchesscouch.engine

import android.util.Log
import kotlin.math.abs
import kotlin.math.max

private const val N = -10
private const val E = 1
private const val S = 10
private const val W = -1

private const val TAG = "SunfishDebug"

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
    val initialBoardString = "          \n          \n rnbqkbnr \n pppppppp \n ........\n ........\n ........\n ........\n PPPPPPPP \n RNBQKBNR \n          \n          \n".replace(" ", "")

    val directions = mapOf(
        'P' to listOf(N, N + N, N + W, N + E),
        'N' to listOf(N + N + E, E + N + E, E + S + E, S + S + E, S + S + W, W + S + W, W + N + W, N + N + W),
        'B' to listOf(N + E, S + E, S + W, N + W),
        'R' to listOf(N, E, S, W),
        'Q' to listOf(N, E, S, W, N + E, S + E, S + W, N + W),
        'K' to listOf(N, E, S, W, N + E, S + E, S + W, N + W)
    )

    val MATE_LOWER = piece.getValue('K') - 10 * piece.getValue('Q')
    val MATE_UPPER = piece.getValue('K') + 10 * piece.getValue('Q')
}

data class SunfishMove(val i: Int, val j: Int, val prom: Char? = null)

data class SunfishPosition(
    val board: CharArray,
    val score: Int,
    val wc: Pair<Boolean, Boolean>,
    val bc: Pair<Boolean, Boolean>,
    val ep: Int,
    val kp: Int
) {
    fun genMoves(): List<SunfishMove> {
        val moves = mutableListOf<SunfishMove>()
        board.forEachIndexed { i, p ->
            if (!p.isUpperCase()) return@forEachIndexed
            SunfishEngineConfig.directions[p]?.forEach { d ->
                var j = i + d
                while (true) {
                    if (j !in board.indices) break
                    val q = board[j]
                    if (q.isWhitespace() || q.isUpperCase()) break
                    if (p == 'P') {
                        if (d in listOf(N, N + N) && q != '.') break
                        if (d == N + N && (i < 81 || board[i + N] != '.')) break
                        if (d in listOf(N + W, N + E) && q == '.' && j != ep) break
                        if (j in SunfishEngineConfig.A8..SunfishEngineConfig.H8) {
                            for (prom in "NBRQ") { moves.add(SunfishMove(i, j, prom)) }
                            break
                        }
                    }
                    moves.add(SunfishMove(i, j))
                    if (p in "PNK" || q.isLowerCase()) break
                    if (i == SunfishEngineConfig.A1 && j + E < board.size && board[j + E] == 'K' && wc.first) { moves.add(SunfishMove(j + E, j + W)) }
                    if (i == SunfishEngineConfig.H1 && j + W < board.size && board[j + W] == 'K' && wc.second) { moves.add(SunfishMove(j + W, j + E)) }
                    j += d
                }
            }
        }
        return moves
    }

    fun rotate(nullMove: Boolean = false): SunfishPosition {
        val newBoard = board.reversedArray().map { it.swapCase() }.toCharArray()
        val newEp = if (ep != 0 && !nullMove) 119 - ep else 0
        val newKp = if (kp != 0 && !nullMove) 119 - kp else 0
        return SunfishPosition(newBoard, -score, bc, wc, newEp, newKp)
    }

    // NOVO: Funkcija koja proverava da li je kralj napadnut
    fun isKingAttacked(): Boolean {
        val kingPos = board.indexOf('K')
        if (kingPos == -1) return false

        val opponentPos = this.rotate(true)
        val ourKingInOpponentView = 119 - kingPos

        val opponentMoves = opponentPos.genMoves()
        for (move in opponentMoves) {
            if (move.j == ourKingInOpponentView) {
                return true
            }
        }
        return false
    }

    fun move(move: SunfishMove): SunfishPosition {
        val (i, j, prom) = move
        val p = board[i]
        val newBoard = board.clone()
        var newWc = wc
        var newBc = bc
        var newEp = 0
        var newKp = 0
        newBoard[j] = p
        newBoard[i] = '.'
        if (i == SunfishEngineConfig.A1) newWc = Pair(false, newWc.second)
        if (i == SunfishEngineConfig.H1) newWc = Pair(newWc.first, false)
        if (j == SunfishEngineConfig.A8) newBc = Pair(newBc.first, false)
        if (j == SunfishEngineConfig.H8) newBc = Pair(false, newBc.second)
        when (p) {
            'K' -> {
                newWc = Pair(false, false)
                if (abs(j - i) == 2) {
                    newKp = (i + j) / 2
                    val rookStart = if (j < i) SunfishEngineConfig.A1 else SunfishEngineConfig.H1
                    newBoard[rookStart] = '.'
                    newBoard[newKp] = 'R'
                }
            }
            'P' -> {
                if (j in SunfishEngineConfig.A8..SunfishEngineConfig.H8) { newBoard[j] = prom!! }
                if (j - i == 2 * N) { newEp = i + N }
                if (j == ep) { newBoard[j + S] = '.' }
            }
        }
        val newPos = SunfishPosition(newBoard, score + value(move), newWc, newBc, newEp, newKp)
        return newPos.rotate()
    }

    fun value(move: SunfishMove): Int {
        val (i, j, prom) = move
        val p = board[i]
        val q = board[j]
        var score = (SunfishEngineConfig.pst[p]?.get(j) ?: 0) - (SunfishEngineConfig.pst[p]?.get(i) ?: 0)
        if (q.isLowerCase()) {
            score += SunfishEngineConfig.pst[q.uppercaseChar()]?.get(119 - j) ?: 0
        }
        if (p == 'K' && abs(i - j) == 2) {
            val rookPos = (i + j) / 2
            val rookStart = if (j < i) SunfishEngineConfig.A1 else SunfishEngineConfig.H1
            score += (SunfishEngineConfig.pst['R']?.get(rookPos) ?: 0) - (SunfishEngineConfig.pst['R']?.get(rookStart) ?: 0)
        }
        if (p == 'P') {
            if (j in SunfishEngineConfig.A8..SunfishEngineConfig.H8) {
                score += (SunfishEngineConfig.pst[prom!!]?.get(j) ?: 0) - (SunfishEngineConfig.pst['P']?.get(j) ?: 0)
            }
            if (j == ep) {
                score += SunfishEngineConfig.pst['P']?.get(119 - (j + S)) ?: 0
            }
        }
        return score
    }

    private fun Char.swapCase(): Char = if (isUpperCase()) lowercaseChar() else uppercaseChar()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SunfishPosition
        if (!board.contentEquals(other.board)) return false
        if (score != other.score) return false
        if (wc != other.wc) return false
        if (bc != other.bc) return false
        if (ep != other.ep) return false
        if (kp != other.kp) return false
        return true
    }

    override fun hashCode(): Int {
        var result = board.contentHashCode()
        result = 31 * result + score
        result = 31 * result + wc.hashCode()
        result = 31 * result + bc.hashCode()
        result = 31 * result + ep
        result = 31 * result + kp
        return result
    }
}

private data class TTEntry(val lower: Int, val upper: Int)

class SunfishSearcher {
    private var nodes = 0
    private var history = setOf<List<Char>>()
    private val ttScore = mutableMapOf<Pair<SunfishPosition, Int>, TTEntry>()
    private val ttMove = mutableMapOf<SunfishPosition, SunfishMove>()

    private fun bound(pos: SunfishPosition, gamma: Int, depth: Int, canNull: Boolean = true, currentDepth: Int = 0): Int {
        if (currentDepth > 64) return pos.score

        nodes++
        val d = max(depth, 0)
        if (pos.score <= -SunfishEngineConfig.MATE_LOWER) return -SunfishEngineConfig.MATE_UPPER
        val entry = ttScore.getOrDefault(Pair(pos, d), TTEntry(-SunfishEngineConfig.MATE_UPPER, SunfishEngineConfig.MATE_UPPER))
        if (entry.lower >= gamma) return entry.lower
        if (entry.upper < gamma) return entry.upper
        if (d > 0 && canNull && pos.board.toList() in history) return 0

        var best = -SunfishEngineConfig.MATE_UPPER
        var bestMove: SunfishMove? = null

        if (d > 2 && canNull && abs(pos.score) < 500) {
            val nullPos = pos.rotate(nullMove = true)
            val nullScore = -bound(nullPos, 1 - gamma, d - 3, canNull = false, currentDepth = currentDepth + 1)
            best = max(best, nullScore)
            if (best >= gamma) return best
        }

        val moves = pos.genMoves()
        val sortedMoves = moves.sortedByDescending { pos.value(it) }
        val killer = ttMove[pos]

        // NOVO: Filtriramo SVE poteze koji ostavljaju kralja u šahu
        val legalMoves = (listOfNotNull(killer) + sortedMoves).distinct().filter { move ->
            !pos.move(move).rotate(true).isKingAttacked()
        }

        for (move in legalMoves) {
            val score = -bound(pos.move(move), 1 - gamma, d - 1, true, currentDepth = currentDepth + 1)
            if (score > best) {
                best = score
                bestMove = move
                if (best >= gamma) {
                    ttMove[pos] = move
                    break
                }
            }
        }

        if (bestMove != null) {
            ttMove[pos] = bestMove
        }

        if (d > 0 && best == -SunfishEngineConfig.MATE_UPPER) {
            val flipped = pos.rotate(nullMove = true)
            val inCheck = bound(flipped, SunfishEngineConfig.MATE_UPPER, 0, false, currentDepth = currentDepth + 1) > SunfishEngineConfig.MATE_LOWER
            best = if (inCheck) -SunfishEngineConfig.MATE_LOWER else 0
        }
        if (best >= gamma) ttScore[Pair(pos, d)] = TTEntry(best, entry.upper)
        if (best < gamma) ttScore[Pair(pos, d)] = TTEntry(entry.lower, best)
        return best
    }

    fun search(history: List<SunfishPosition>, timeLimitSec: Double = 1.0): Pair<SunfishMove?, Int> {
        nodes = 0
        this.history = history.map { it.board.toList() }.toSet()
        ttScore.clear()
        ttMove.clear()
        var bestMove: SunfishMove? = null
        var score = 0
        val startTime = System.currentTimeMillis()
        for (depth in 1..100) {
            score = bound(history.last(), 0, depth, canNull = false)
            bestMove = ttMove[history.last()]
            if ((System.currentTimeMillis() - startTime) / 1000.0 > timeLimitSec) {
                break
            }
        }
        return Pair(bestMove, score)
    }
}

class SunfishEngine {
    private val searcher = SunfishSearcher()
    private var history: List<SunfishPosition>
    private var isEngineCalculatingForBlack = false

    init {
        val initialBoard = SunfishEngineConfig.initialBoardString.toCharArray()
        val initialPos = SunfishPosition(
            board = initialBoard, score = 0, wc = Pair(true, true), bc = Pair(true, true), ep = 0, kp = 0
        )
        history = listOf(initialPos)
    }

    fun setPositionFromFen(fen: String) {
        val parts = fen.split(" ")
        val piecePlacement = parts[0]
        val activeColor = parts[1]
        val castling = parts[2]
        val enPassant = parts[3]
        val newBoard = " ".repeat(120).toCharArray()
        var i = 0
        var rank = 8
        var file = 0
        while (i < piecePlacement.length) {
            val char = piecePlacement[i]
            when {
                char == '/' -> { rank--; file = 0 }
                char.isDigit() -> { file += char.digitToInt() }
                else -> {
                    val boardIndex = SunfishEngineConfig.A8 + file - (rank - 8) * 10
                    newBoard[boardIndex] = char
                    file++
                }
            }
            i++
        }
        for (r in 1..8) {
            for (f in 0..7) {
                val boardIndex = SunfishEngineConfig.A1 + f - 10 * (r - 1)
                if (newBoard[boardIndex] == ' ') {
                    newBoard[boardIndex] = '.'
                }
            }
        }
        val wc = Pair(castling.contains('Q'), castling.contains('K'))
        val bc = Pair(castling.contains('q'), castling.contains('k'))
        val ep = if (enPassant != "-") parse(enPassant) else 0
        var position = SunfishPosition(newBoard, 0, wc, bc, ep, 0)

        isEngineCalculatingForBlack = (activeColor == "b")

        if (isEngineCalculatingForBlack) {
            position = position.rotate()
        }
        history = listOf(position)
    }

    fun searchBestMove(timeLimitSec: Double = 1.0): String? {
        val (bestMove, score) = searcher.search(history, timeLimitSec)

        if (bestMove != null) {
            history = history + listOf(history.last().move(bestMove))
            var from = bestMove.i
            var to = bestMove.j

            if (isEngineCalculatingForBlack) {
                from = 119 - from
                to = 119 - to
            }

            val moveStr = "${render(from)}${render(to)}${bestMove.prom?.lowercase() ?: ""}"
            return moveStr
        } else {
            return null
        }
    }

    private fun parse(algebraic: String): Int {
        val file = algebraic[0] - 'a'
        val rank = algebraic[1].digitToInt()
        return SunfishEngineConfig.A1 + file - 10 * (rank - 1)
    }

    private fun render(i: Int): String {
        val rank = (119 - i) / 10 - 1
        val file = (i % 10) - 1
        return ('a' + file).toString() + rank.toString()
    }
}