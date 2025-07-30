// model/MoveGenerator.kt
package com.program.blindfoldchesscouch.model

object MoveGenerator {

    fun generateMovesForPiece(piece: Piece, from: Square, game: Game): List<Move> {
        val board = game.getCurrentBoard() // Ispravan poziv
        return when (piece.type) {
            PieceType.PAWN -> generatePawnMoves(piece, from, game)
            PieceType.KNIGHT -> generateKnightMoves(piece, from, board)
            PieceType.BISHOP -> generateSlidingMoves(piece, from, board, BishopDirections)
            PieceType.ROOK -> generateSlidingMoves(piece, from, board, RookDirections)
            PieceType.QUEEN -> generateSlidingMoves(piece, from, board, QueenDirections)
            PieceType.KING -> generateKingMoves(piece, from, game)
        }
    }

    private val RookDirections = listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))
    private val BishopDirections = listOf(Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1))
    private val QueenDirections = RookDirections + BishopDirections

    private fun generatePawnMoves(piece: Piece, from: Square, game: Game): List<Move> {
        val moves = mutableListOf<Move>()
        val board = game.getCurrentBoard() // Ispravan poziv
        val startFileIndex = from.file - 'a'

        val direction = if (piece.color == Color.WHITE) 1 else -1
        val startingRank = if (piece.color == Color.WHITE) 2 else 7
        val promotionRank = if (piece.color == Color.WHITE) 8 else 1

        fun addPawnMove(to: Square, capturedPiece: Piece? = null) {
            if (to.rank == promotionRank) {
                moves.add(Move.promotion(from, to, piece, PieceType.QUEEN, capturedPiece))
                moves.add(Move.promotion(from, to, piece, PieceType.ROOK, capturedPiece))
                moves.add(Move.promotion(from, to, piece, PieceType.BISHOP, capturedPiece))
                moves.add(Move.promotion(from, to, piece, PieceType.KNIGHT, capturedPiece))
            } else {
                moves.add(Move.normal(from, to, piece, capturedPiece))
            }
        }

        val oneStepForwardRank = from.rank + direction
        if (oneStepForwardRank in 1..8) {
            val oneStepSquare = Square(from.file, oneStepForwardRank)
            if (board.getPieceAt(oneStepSquare) == null) {
                addPawnMove(oneStepSquare)

                if (from.rank == startingRank) {
                    val twoStepsForwardRank = from.rank + (2 * direction)
                    val twoStepsSquare = Square(from.file, twoStepsForwardRank)
                    if (board.getPieceAt(twoStepsSquare) == null) {
                        moves.add(Move.normal(from, twoStepsSquare, piece))
                    }
                }
            }
        }

        val captureRank = from.rank + direction
        if (captureRank in 1..8) {
            for (fileOffset in listOf(-1, 1)) {
                val targetFile = from.file + fileOffset
                if (targetFile in 'a'..'h') {
                    val toSquare = Square(targetFile, captureRank)
                    val pieceAtTarget = board.getPieceAt(toSquare)
                    if (pieceAtTarget != null && pieceAtTarget.color != piece.color) {
                        addPawnMove(toSquare, capturedPiece = pieceAtTarget)
                    }
                }
            }
        }

        game.enPassantTargetSquare?.let { enPassantSquare ->
            if (captureRank == enPassantSquare.rank && (from.file - 1 == enPassantSquare.file || from.file + 1 == enPassantSquare.file)) {
                val capturedPawnSquare = Square(enPassantSquare.file, from.rank)
                val capturedPawn = board.getPieceAt(capturedPawnSquare)
                if (capturedPawn != null) {
                    moves.add(Move.enPassant(from, enPassantSquare, piece, capturedPawn))
                }
            }
        }

        return moves
    }

    private fun generateKingMoves(piece: Piece, from: Square, game: Game): List<Move> {
        val moves = mutableListOf<Move>()
        val board = game.getCurrentBoard() // Ispravan poziv
        val startFileIndex = from.file - 'a'
        val startRankIndex = from.rank - 1

        for ((rankOffset, fileOffset) in QueenDirections) {
            val targetRankIndex = startRankIndex + rankOffset
            val targetFileIndex = startFileIndex + fileOffset

            if (targetRankIndex in 0..7 && targetFileIndex in 0..7) {
                val toSquare = Square('a' + targetFileIndex, targetRankIndex + 1)
                val pieceAtTarget = board.getPieceAt(toSquare)

                if (pieceAtTarget == null || pieceAtTarget.color != piece.color) {
                    moves.add(Move.normal(from, toSquare, piece, capturedPiece = pieceAtTarget))
                }
            }
        }

        val rank = if (piece.color == Color.WHITE) 1 else 8
        if (from == Square('e', rank)) {
            if ((piece.color == Color.WHITE && game.whiteKingSideCastlingAllowed) || (piece.color == Color.BLACK && game.blackKingSideCastlingAllowed)) {
                if (board.getPieceAt(Square('f', rank)) == null && board.getPieceAt(Square('g', rank)) == null) {
                    moves.add(Move(from, Square('g', rank), piece, moveType = MoveType.CASTLING_KING_SIDE))
                }
            }
            if ((piece.color == Color.WHITE && game.whiteQueenSideCastlingAllowed) || (piece.color == Color.BLACK && game.blackQueenSideCastlingAllowed)) {
                if (board.getPieceAt(Square('b', rank)) == null && board.getPieceAt(Square('c', rank)) == null && board.getPieceAt(Square('d', rank)) == null) {
                    moves.add(Move(from, Square('c', rank), piece, moveType = MoveType.CASTLING_QUEEN_SIDE))
                }
            }
        }
        return moves
    }

    private fun generateKnightMoves(piece: Piece, from: Square, board: Board): List<Move> {
        val moves = mutableListOf<Move>()
        // ... (ostatak funkcije je nepromenjen)
        val startFileIndex = from.file - 'a'
        val startRankIndex = from.rank - 1

        val knightMoves = listOf(
            Pair(2, 1), Pair(2, -1), Pair(-2, 1), Pair(-2, -1),
            Pair(1, 2), Pair(1, -2), Pair(-1, 2), Pair(-1, -2)
        )

        for ((rankOffset, fileOffset) in knightMoves) {
            val targetRankIndex = startRankIndex + rankOffset
            val targetFileIndex = startFileIndex + fileOffset

            if (targetRankIndex in 0..7 && targetFileIndex in 0..7) {
                val toSquare = Square('a' + targetFileIndex, targetRankIndex + 1)
                val pieceAtTarget = board.getPieceAt(toSquare)

                if (pieceAtTarget == null || pieceAtTarget.color != piece.color) {
                    moves.add(Move.normal(from, toSquare, piece, capturedPiece = pieceAtTarget))
                }
            }
        }
        return moves
    }

    private fun generateSlidingMoves(piece: Piece, from: Square, board: Board, directions: List<Pair<Int, Int>>): List<Move> {
        val moves = mutableListOf<Move>()
        // ... (ostatak funkcije je nepromenjen)
        val startFileIndex = from.file - 'a'
        val startRankIndex = from.rank - 1

        for ((rankDir, fileDir) in directions) {
            var currentRankIndex = startRankIndex + rankDir
            var currentFileIndex = startFileIndex + fileDir

            while (currentRankIndex in 0..7 && currentFileIndex in 0..7) {
                val toSquare = Square('a' + currentFileIndex, currentRankIndex + 1)
                val pieceAtTarget = board.getPieceAt(toSquare)

                if (pieceAtTarget == null) {
                    moves.add(Move.normal(from, toSquare, piece))
                } else {
                    if (pieceAtTarget.color != piece.color) {
                        moves.add(Move.normal(from, toSquare, piece, capturedPiece = pieceAtTarget))
                    }
                    break
                }
                currentRankIndex += rankDir
                currentFileIndex += fileDir
            }
        }
        return moves
    }
}