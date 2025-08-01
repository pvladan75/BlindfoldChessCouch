// util/AdvancedQuizGenerator.kt
package com.program.blindfoldchesscouch.util

import com.program.blindfoldchesscouch.model.*
import com.program.blindfoldchesscouch.model.Color
import kotlin.random.Random

object AdvancedQuizGenerator {

    private val allSquares = (1..8).flatMap { rank -> ('a'..'h').map { file -> Square(file, rank) } }
    private val slidingPieceTypes = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP)
    private val allPieceTypes = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

    fun generateQuizSession(): List<AdvancedQuestion> {
        val questions = mutableListOf<AdvancedQuestion>()

        // 1. Dodajemo tačno 2 pitanja o boji polja
        repeat(2) { questions.add(generateSquareColorQuestion()) }

        // 2. Dodajemo 4 pitanja o napadnutim poljima
        repeat(4) { questions.add(generateAttackedSquaresQuestion()) }

        // 3. Dodajemo 4 pitanja o presretanju
        repeat(4) { questions.add(generateInterceptQuestion()) }

        return questions.shuffled()
    }

    private fun generateAttackedSquaresQuestion(): AdvancedQuestion {
        val gameForFen = Game()
        gameForFen.getCurrentBoard().clearBoard()

        val pieceType = allPieceTypes.random()
        val pieceColor = Color.WHITE
        val pieceSquare = allSquares.random()
        val piece = Piece(pieceType, pieceColor)

        gameForFen.getCurrentBoard().placePiece(pieceSquare, piece)
        val fen = gameForFen.toFen()

        val gameForAnalysis = Game()
        gameForAnalysis.loadFen(fen)
        val boardForQuestion = gameForAnalysis.getCurrentBoard()

        val legalMoves = gameForAnalysis.getLegalMoves().map { it.to }
        val correctOptions = legalMoves.shuffled().take(Random.nextInt(1, 3)).toSet()

        val incorrectOptions = allSquares.filterNot { it in correctOptions || it == pieceSquare }
            .shuffled().take(4 - correctOptions.size).toSet()

        val options = (correctOptions + incorrectOptions).map { it.toAlgebraicNotation() }.shuffled()

        return AdvancedQuestion.AttackedSquaresQuestion(
            questionText = "${piece.type.name.lowercase().replaceFirstChar { it.uppercase() }} на ${pieceSquare.toAlgebraicNotation()}. Која поља напада?",
            options = options,
            correctOptions = correctOptions.map { it.toAlgebraicNotation() }.toSet(),
            board = boardForQuestion
        )
    }

    // *** НОВА, ИМПЛЕМЕНТИРАНА ВЕРЗИЈА ***
    private fun generateInterceptQuestion(): AdvancedQuestion {
        // Pokušavamo nekoliko puta da nađemo validno pitanje
        repeat(10) {
            val pieceType = slidingPieceTypes.random()
            val pieceColor = Color.WHITE
            val pieceSquare = allSquares.random()
            val piece = Piece(pieceType, pieceColor)

            val game = Game()
            game.getCurrentBoard().clearBoard()
            game.getCurrentBoard().placePiece(pieceSquare, piece)
            game.loadFen(game.toFen()) // Učitamo da bi se interno stanje igre ispravno postavilo

            val allPossibleMoves = game.getLegalMoves()
            val potentialTargets = allSquares.filterNot { sq -> allPossibleMoves.any { it.to == sq } || sq == pieceSquare }

            if (potentialTargets.isEmpty()) return@repeat // Nema meta, probaj ponovo

            val targetSquare = potentialTargets.random()

            val correctMoves = mutableSetOf<Square>()
            for (move in allPossibleMoves) {
                val tempGame = Game()
                tempGame.getCurrentBoard().clearBoard()
                tempGame.getCurrentBoard().placePiece(move.to, piece)
                tempGame.loadFen(tempGame.toFen())
                if (tempGame.getLegalMoves().any { it.to == targetSquare }) {
                    correctMoves.add(move.to)
                }
            }

            if (correctMoves.isNotEmpty()) {
                val incorrectMoves = allPossibleMoves.map { it.to }.filterNot { it in correctMoves }

                val finalCorrectOptions = correctMoves.shuffled().take(Random.nextInt(1, 3)).toSet()
                val finalIncorrectOptions = incorrectMoves.shuffled().take(4 - finalCorrectOptions.size).toSet()

                val options = (finalCorrectOptions + finalIncorrectOptions).map { it.toAlgebraicNotation() }.shuffled()

                return AdvancedQuestion.InterceptQuestion(
                    questionText = "${piece.type.name.lowercase().replaceFirstChar { it.uppercase() }} на ${pieceSquare.toAlgebraicNotation()}. Где треба да се помери да би напао/ла поље ${targetSquare.toAlgebraicNotation()}?",
                    options = options,
                    correctOptions = finalCorrectOptions.map { it.toAlgebraicNotation() }.toSet(),
                    board = game.getCurrentBoard()
                )
            }
        }
        // Fallback u slučaju da ne uspemo da generišemo pitanje (jako retko)
        return generateAttackedSquaresQuestion()
    }

    private fun generateSquareColorQuestion(): AdvancedQuestion {
        val options = allSquares.shuffled().take(4)
        val correctOptions = options.filter {
            val isFileEven = (it.file - 'a' + 1) % 2 == 0
            val isRankEven = it.rank % 2 == 0
            isFileEven == isRankEven
        }.map { it.toAlgebraicNotation() }.toSet()

        if (correctOptions.isEmpty()) {
            return generateSquareColorQuestion()
        }

        return AdvancedQuestion.SquareColorQuestion(
            questionText = "Која од наведених поља су црна?",
            options = options.map { it.toAlgebraicNotation() },
            correctOptions = correctOptions
        )
    }
}