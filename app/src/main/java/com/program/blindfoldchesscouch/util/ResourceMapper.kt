// util/ResourceMapper.kt
package com.program.blindfoldchesscouch.util

import androidx.annotation.DrawableRes
import com.program.blindfoldchesscouch.R
import com.program.blindfoldchesscouch.model.Color
import com.program.blindfoldchesscouch.model.Piece
import com.program.blindfoldchesscouch.model.PieceType

/**
 * Pomoćna funkcija koja mapira objekat Piece u njegov odgovarajući Drawable resurs.
 * Sada ispravno vraća ID iz R klase, jer su slike u drawable folderu.
 *
 * @param piece Figura za koju tražimo sliku.
 * @return Int vrednost koja predstavlja ID resursa (npr. R.drawable.white_pawn).
 */
@DrawableRes
fun getDrawableResourceForPiece(piece: Piece): Int {
    return when (piece.color) {
        Color.WHITE -> when (piece.type) {
            PieceType.PAWN -> R.drawable.white_pawn
            PieceType.KNIGHT -> R.drawable.white_knight
            PieceType.BISHOP -> R.drawable.white_bishop
            PieceType.ROOK -> R.drawable.white_rook
            PieceType.QUEEN -> R.drawable.white_queen
            PieceType.KING -> R.drawable.white_king
        }
        Color.BLACK -> when (piece.type) {
            PieceType.PAWN -> R.drawable.black_pawn
            PieceType.KNIGHT -> R.drawable.black_knight
            PieceType.BISHOP -> R.drawable.black_bishop
            PieceType.ROOK -> R.drawable.black_rook
            PieceType.QUEEN -> R.drawable.black_queen
            PieceType.KING -> R.drawable.black_king
        }
    }
}