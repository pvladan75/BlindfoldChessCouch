// model/Board.kt
package com.program.blindfoldchesscouch.model

class Board {
    private val pieces: MutableMap<Square, Piece> = mutableMapOf()

    fun setupStartingPosition() {
        pieces.clear()
        for (fileChar in 'a'..'h') {
            pieces[Square(fileChar, 2)] = Piece(PieceType.PAWN, Color.WHITE)
            pieces[Square(fileChar, 7)] = Piece(PieceType.PAWN, Color.BLACK)
        }
        pieces[Square('a', 1)] = Piece(PieceType.ROOK, Color.WHITE)
        pieces[Square('h', 1)] = Piece(PieceType.ROOK, Color.WHITE)
        pieces[Square('a', 8)] = Piece(PieceType.ROOK, Color.BLACK)
        pieces[Square('h', 8)] = Piece(PieceType.ROOK, Color.BLACK)
        pieces[Square('b', 1)] = Piece(PieceType.KNIGHT, Color.WHITE)
        pieces[Square('g', 1)] = Piece(PieceType.KNIGHT, Color.WHITE)
        pieces[Square('b', 8)] = Piece(PieceType.KNIGHT, Color.BLACK)
        pieces[Square('g', 8)] = Piece(PieceType.KNIGHT, Color.BLACK)
        pieces[Square('c', 1)] = Piece(PieceType.BISHOP, Color.WHITE)
        pieces[Square('f', 1)] = Piece(PieceType.BISHOP, Color.WHITE)
        pieces[Square('c', 8)] = Piece(PieceType.BISHOP, Color.BLACK)
        pieces[Square('f', 8)] = Piece(PieceType.BISHOP, Color.BLACK)
        pieces[Square('d', 1)] = Piece(PieceType.QUEEN, Color.WHITE)
        pieces[Square('d', 8)] = Piece(PieceType.QUEEN, Color.BLACK)
        pieces[Square('e', 1)] = Piece(PieceType.KING, Color.WHITE)
        pieces[Square('e', 8)] = Piece(PieceType.KING, Color.BLACK)
    }

    fun getPieceAt(square: Square): Piece? = pieces[square]
    fun placePiece(square: Square, piece: Piece) { pieces[square] = piece }
    fun removePiece(square: Square) { pieces.remove(square) }

    fun makeMove(move: Move): Boolean {
        val movingPiece = pieces[move.from] ?: return false

        when (move.moveType) {
            MoveType.NORMAL -> {
                pieces.remove(move.from)
                pieces[move.to] = movingPiece
            }
            MoveType.CASTLING_KING_SIDE -> {
                val rank = if (movingPiece.color == Color.WHITE) 1 else 8
                pieces.remove(Square('e', rank)); pieces[Square('g', rank)] = Piece(PieceType.KING, movingPiece.color)
                pieces.remove(Square('h', rank)); pieces[Square('f', rank)] = Piece(PieceType.ROOK, movingPiece.color)
            }
            MoveType.CASTLING_QUEEN_SIDE -> {
                val rank = if (movingPiece.color == Color.WHITE) 1 else 8
                pieces.remove(Square('e', rank)); pieces[Square('c', rank)] = Piece(PieceType.KING, movingPiece.color)
                pieces.remove(Square('a', rank)); pieces[Square('d', rank)] = Piece(PieceType.ROOK, movingPiece.color)
            }
            MoveType.EN_PASSANT -> {
                pieces.remove(move.from); pieces[move.to] = movingPiece
                val capturedPawnRank = if (movingPiece.color == Color.WHITE) move.to.rank - 1 else move.to.rank + 1
                pieces.remove(Square(move.to.file, capturedPawnRank))
            }
            MoveType.PROMOTION -> {
                if (move.promotionPieceType == null) return false
                pieces.remove(move.from)
                pieces[move.to] = Piece(move.promotionPieceType, movingPiece.color)
            }
        }
        return true
    }

    fun getAllPieces(): Map<Square, Piece> = pieces.toMap()

    fun clearBoard() { pieces.clear() }

    fun loadFen(fen: String) {
        clearBoard()
        val fenPositionPart = fen.substringBefore(' ')
        var rank = 8
        var file = 'a'

        for (char in fenPositionPart) {
            when {
                char == '/' -> {
                    rank--
                    file = 'a'
                }
                char.isDigit() -> {
                    file = (file.code + char.digitToInt()).toChar()
                }
                else -> {
                    val square = Square.fromAlgebraicNotation("$file$rank")
                    val piece = Piece.fromFenChar(char)
                    if (square != null && piece != null) {
                        placePiece(square, piece)
                    }
                    file = (file.code + 1).toChar()
                }
            }
        }
    }

    fun copy(): Board {
        val newBoard = Board()
        newBoard.pieces.putAll(this.pieces)
        return newBoard
    }
}