// in engine/SunfishEngine.kt
package com.program.blindfoldchesscouch.engine

import android.util.Log
import kotlin.math.abs
import kotlin.math.max

// --- Pomoćne funkcije ---
private fun parse(algebraic: String): Int {
    val file = algebraic[0] - 'a'
    val rank = algebraic[1].digitToInt()
    return SunfishEngineConfig.A1 - (rank - 1) * 10 + file
}

private fun render(i: Int): String {
    val rank = 8 - ((i - SunfishEngineConfig.A8) / 10)
    val file = 'a' + ((i - SunfishEngineConfig.A8) % 10)
    return "$file$rank"
}
// -----------------------

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
        'P' to intArrayOf(0,0,0,0,0,0,0,0,78,83,86,73,102,82,85,90,7,29,21,44,40,31,44,7,-17,16,-2,15,14,0,15,-13,-26,3,10,9,6,1,0,-23,-22,9,5,-11,-10,-2,3,-19,-31,8,-7,-37,-36,-14,3,-31,0,0,0,0,0,0,0,0),
        'N' to intArrayOf(-66,-53,-75,-75,-10,-55,-58,-70,-3,-6,100,-36,4,62,-4,-14,10,67,1,74,73,27,62,-2,24,24,45,37,33,41,25,17,-1,5,31,21,22,35,2,0,-18,10,13,22,18,15,11,-14,-23,-15,2,0,2,0,-23,-20,-74,-23,-26,-24,-19,-35,-22,-69),
        'B' to intArrayOf(-59,-78,-82,-76,-23,-107,-37,-50,-11,20,35,-42,-39,31,2,-22,-9,39,-32,41,52,-10,28,-14,25,17,20,34,26,25,15,10,13,10,17,23,17,16,0,7,14,25,24,15,8,25,20,15,19,20,11,6,7,6,20,16,-7,2,-15,-12,-14,-15,-10,-10),
        'R' to intArrayOf(35,29,33,4,37,33,56,50,55,29,56,67,55,62,34,60,19,35,28,33,45,27,25,15,0,5,16,13,18,-4,-9,-6,-28,-35,-16,-21,-13,-29,-46,-30,-42,-28,-42,-25,-25,-35,-26,-46,-53,-38,-31,-26,-29,-43,-44,-53,-30,-24,-18,5,-2,-18,-31,-32),
        'Q' to intArrayOf(6,1,-8,-104,69,24,88,26,14,32,60,-10,20,76,57,24,-2,43,32,60,72,63,43,2,1,-16,22,17,25,20,-13,-6,-14,-15,-2,-5,-1,-10,-20,-22,-30,-6,-13,-11,-16,-11,-16,-27,-36,-18,0,-19,-15,-15,-21,-38,-39,-30,-31,-13,-31,-36,-34,-42),
        'K' to intArrayOf(4,54,47,-99,-99,60,83,-62,-32,10,55,56,56,55,10,3,-62,12,-57,44,-67,28,37,-31,-55,50,11,-4,-19,13,0,-49,-55,-43,-52,-28,-51,-47,-8,-50,-47,-42,-43,-79,-64,-32,-29,-32,-4,3,-14,-50,-57,-18,13,4,17,30,-3,-14,6,-1,40,18)
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
data class UndoInfo(val ep: Int, val kp: Int, val wc: Pair<Boolean, Boolean>, val bc: Pair<Boolean, Boolean>, val capturedPiece: Char)

class SunfishPosition(
    var board: CharArray,
    var score: Int,
    var wc: Pair<Boolean, Boolean>,
    var bc: Pair<Boolean, Boolean>,
    var ep: Int,
    var kp: Int
) {
    fun genMoves(): List<SunfishMove> {
        val moves = mutableListOf<SunfishMove>()
        for (i in board.indices) {
            val p = board[i]
            if (!p.isUpperCase()) continue

            for (d in SunfishEngineConfig.directions.getValue(p)) {
                var j = i + d
                while (j in board.indices && !board[j].isWhitespace()) {
                    val q = board[j]
                    if (q.isUpperCase()) break

                    if (p == 'P') {
                        if (d in listOf(N, N + N) && q != '.') break
                        if (d == N + N && (i < 81 || board[i + N] != '.')) break
                        if (d in listOf(N + W, N + E) && q == '.' && j != ep) break
                        if (j in SunfishEngineConfig.A8..SunfishEngineConfig.H8) {
                            "NBRQ".forEach { prom -> moves.add(SunfishMove(i, j, prom)) }
                            break
                        }
                    }
                    moves.add(SunfishMove(i, j))

                    if (p in "PNK" || q.isLowerCase()) break
                    if (i == SunfishEngineConfig.A1 && board.getOrNull(j + E) == 'K' && wc.first) moves.add(SunfishMove(j + E, j + W))
                    if (i == SunfishEngineConfig.H1 && board.getOrNull(j + W) == 'K' && wc.second) moves.add(SunfishMove(j + W, j + E))

                    j += d
                }
            }
        }
        return moves
    }

    fun isKingAttacked(): Boolean {
        val kingPos = board.indexOf('K')
        if (kingPos == -1) return true

        for (d in SunfishEngineConfig.directions.getValue('R')) {
            var j = kingPos + d
            while (j in board.indices && !board[j].isWhitespace()) {
                val piece = board[j]
                if (piece == 'r' || piece == 'q') return true
                if (piece != '.') break
                j += d
            }
        }
        for (d in SunfishEngineConfig.directions.getValue('B')) {
            var j = kingPos + d
            while (j in board.indices && !board[j].isWhitespace()) {
                val piece = board[j]
                if (piece == 'b' || piece == 'q') return true
                if (piece != '.') break
                j += d
            }
        }
        for (d in SunfishEngineConfig.directions.getValue('N')) {
            val j = kingPos + d
            if (j in board.indices && board[j] == 'n') return true
        }
        if (board.getOrNull(kingPos + N + W) == 'p' || board.getOrNull(kingPos + N + E) == 'p') return true

        for (d in SunfishEngineConfig.directions.getValue('K')) {
            val j = kingPos + d
            if (j in board.indices && board[j] == 'k') return true
        }
        return false
    }

    fun makeMove(move: SunfishMove): UndoInfo {
        val (i, j, prom) = move
        val p = board[i]
        val q = board[j]

        val undo = UndoInfo(ep, kp, wc, bc, q)
        ep = 0; kp = 0

        board[j] = p; board[i] = '.'

        if (i == SunfishEngineConfig.A1) wc = Pair(false, wc.second)
        if (i == SunfishEngineConfig.H1) wc = Pair(wc.first, false)
        if (j == SunfishEngineConfig.A8) bc = Pair(bc.first, false)
        if (j == SunfishEngineConfig.H8) bc = Pair(false, bc.second)

        when (p) {
            'K' -> {
                wc = Pair(false, false)
                if (abs(j - i) == 2) {
                    kp = (i + j) / 2
                    val rookStart = if (j < i) SunfishEngineConfig.A1 else SunfishEngineConfig.H1
                    board[rookStart] = '.'; board[kp] = 'R'
                }
            }
            'P' -> {
                if (j in SunfishEngineConfig.A8..SunfishEngineConfig.H8) { board[j] = prom!! }
                if (j - i == 2 * N) { ep = i + N }
                if (j == undo.ep && q == '.') { board[j + S] = '.' }
            }
        }

        score += value(move, q)
        return undo
    }

    fun unmakeMove(move: SunfishMove, undo: UndoInfo) {
        val (i, j, _) = move
        val p = board[j]

        ep = undo.ep; kp = undo.kp; wc = undo.wc; bc = undo.bc
        board[i] = p; board[j] = undo.capturedPiece

        if (p == 'P') {
            if (j in SunfishEngineConfig.A8..SunfishEngineConfig.H8) { board[i] = 'P' }
            if (j == ep && undo.capturedPiece == '.') { board[j+S] = 'p' }
        }
        if (p == 'K' && abs(j-i) == 2) {
            val rookStart = if (j < i) SunfishEngineConfig.A1 else SunfishEngineConfig.H1
            board[rookStart] = 'R'; board[kp] = '.'
        }

        score -= value(move, undo.capturedPiece)
    }

    fun value(move: SunfishMove, captured: Char): Int {
        val (i, j, prom) = move
        val p = board[i]
        var scoreChange = (SunfishEngineConfig.pst[p]?.get(j) ?: 0) - (SunfishEngineConfig.pst[p]?.get(i) ?: 0)
        if (captured.isLowerCase()) {
            scoreChange += SunfishEngineConfig.pst[captured.uppercaseChar()]?.get(119 - j) ?: 0
        }
        if (p == 'P') {
            if (j in SunfishEngineConfig.A8..SunfishEngineConfig.H8) scoreChange += (SunfishEngineConfig.pst[prom!!]?.get(j) ?: 0) - (SunfishEngineConfig.pst['P']?.get(j) ?: 0)
            if (j == ep && captured == '.') scoreChange += SunfishEngineConfig.pst['P']?.get(119 - (j + S)) ?: 0
        }
        return scoreChange
    }

    fun rotate() {
        board = board.reversedArray().map { it.swapCase() }.toCharArray()
        score = -score
        val tempWc = wc; wc = bc; bc = tempWc
        if (ep != 0) ep = 119 - ep
        if (kp != 0) kp = 119 - kp
    }

    private fun Char.swapCase(): Char = if (isUpperCase()) lowercaseChar() else uppercaseChar()
}

private data class TTEntry(val lower: Int, val upper: Int)

class SunfishSearcher(private var pos: SunfishPosition) {
    private var nodes = 0
    private val ttScore = mutableMapOf<Long, TTEntry>()
    private val ttMove = mutableMapOf<Long, SunfishMove>()
    private var isBlackToMoveForLog: Boolean = false

    private fun bound(gamma: Int, depth: Int): Int {
        nodes++
        val d = max(depth, 0)

        val posHash = pos.board.contentHashCode().toLong()
        val entry = ttScore.getOrDefault(posHash, TTEntry(-SunfishEngineConfig.MATE_UPPER, SunfishEngineConfig.MATE_UPPER))
        if (entry.lower >= gamma) return entry.lower
        if (entry.upper < gamma) return entry.upper

        if (d == 0) return pos.score

        var best = -SunfishEngineConfig.MATE_UPPER
        var bestMove: SunfishMove? = null

        val pseudoLegalMoves = pos.genMoves()

        // Logovanje pseudo-legalnih poteza - ово је корисно за дебаговање
        // Log.d(TAG, "Pseudo-legalni potezi: ${pseudoLegalMoves.joinToString { render(it.i) + render(it.j) }}")

        // *** ИЗМЕНА ЈЕ ОВДЕ ***
        // Ово је исправна логика за филтрирање легалних потеза.
        // Уклањамо `pos.rotate()` позиве из петље.
        val legalMoves = mutableListOf<SunfishMove>()
        for (move in pseudoLegalMoves) {
            val undo = pos.makeMove(move)
            // Након нашег потеза, проверавамо да ли је НАШ краљ нападнут.
            // Функција isKingAttacked() ради тачно то.
            if (!pos.isKingAttacked()) {
                legalMoves.add(move)
            }
            // Враћамо потез да бисмо могли да тестирамо следећи.
            pos.unmakeMove(move, undo)
        }

        // Log.d(TAG, "Legalni potezi: ${legalMoves.joinToString { render(it.i) + render(it.j) }}")


        if (legalMoves.isEmpty()) {
            return if (pos.isKingAttacked()) -SunfishEngineConfig.MATE_LOWER else 0
        }

        val sortedMoves = legalMoves.sortedByDescending { pos.value(it, pos.board[it.j]) }
        val killer = ttMove[posHash]

        for (move in (listOfNotNull(killer) + sortedMoves).distinct()) {
            val undo = pos.makeMove(move)
            pos.rotate()

            val score = -bound(1 - gamma, d - 1)

            pos.rotate()
            pos.unmakeMove(move, undo)

            if (score > best) {
                best = score
                bestMove = move
                if (best >= gamma) {
                    ttMove[posHash] = move
                    break
                }
            }
        }

        if (bestMove != null) {
            ttMove[posHash] = bestMove
        }

        if (best >= gamma) ttScore[posHash] = TTEntry(best, entry.upper)
        if (best < gamma) ttScore[posHash] = TTEntry(entry.lower, best)

        return best
    }

    fun search(isBlack: Boolean, timeLimitSec: Double = 1.0): Pair<SunfishMove?, Int> {
        this.isBlackToMoveForLog = isBlack
        nodes = 0
        ttScore.clear()
        ttMove.clear()
        var bestMove: SunfishMove? = null
        var score = 0
        val startTime = System.currentTimeMillis()
        for (depth in 1..100) {
            score = bound(0, depth)
            bestMove = ttMove[pos.board.contentHashCode().toLong()]
            if ((System.currentTimeMillis() - startTime) / 1000.0 > timeLimitSec) {
                break
            }
        }
        return Pair(bestMove, score)
    }
}

class SunfishEngine {
    private var pos: SunfishPosition
    private var searcher: SunfishSearcher
    private var isEngineCalculatingForBlack = false

    init {
        val initialBoard = SunfishEngineConfig.initialBoardString.toCharArray()
        pos = SunfishPosition(initialBoard.clone(), 0, Pair(true, true), Pair(true, true), 0, 0)
        searcher = SunfishSearcher(pos)
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
                    val boardIndex = SunfishEngineConfig.A8 + file + (8 - rank) * 10
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

        pos.board = newBoard
        pos.wc = Pair(castling.contains('Q'), castling.contains('K'))
        pos.bc = Pair(castling.contains('q'), castling.contains('k'))
        pos.ep = if (enPassant != "-") parse(enPassant) else 0
        pos.kp = 0
        pos.score = 0

        isEngineCalculatingForBlack = (activeColor == "b")

        if (isEngineCalculatingForBlack) {
            pos.rotate()
        }
    }

    fun searchBestMove(timeLimitSec: Double = 1.0): String? {
        val (bestMove, _) = searcher.search(isEngineCalculatingForBlack, timeLimitSec)

        if (bestMove != null) {
            var from = bestMove.i
            var to = bestMove.j

            if (isEngineCalculatingForBlack) {
                from = 119 - from
                to = 119 - to
            }

            return "${render(from)}${render(to)}${bestMove.prom?.lowercase() ?: ""}"
        } else {
            return null
        }
    }
}