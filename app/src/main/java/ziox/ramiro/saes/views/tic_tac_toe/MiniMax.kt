package ziox.ramiro.saes.views.tic_tac_toe

import kotlin.math.max
import kotlin.math.min


class MiniMax(private val aiPlayer: Boolean, private val opponent: Boolean) {
    data class Move(var x: Int, var y: Int)

    fun findBestMove(boardOriginal: Array<Array<Boolean?>>) : Move? {
        var bestVal = -1000
        var bestMove : Move? = null
        val board = cloneBoard(boardOriginal)

        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == null) {
                    board[i][j] = aiPlayer
                    val moveVal: Int = miniMax(board, 0, false)
                    board[i][j] = null
                    if (moveVal > bestVal) {
                        bestMove = Move(j, i)
                        bestVal = moveVal
                    }
                }
            }
        }

        return bestMove
    }

    private fun cloneBoard(board: Array<Array<Boolean?>>) = Array(3){
        board[it].clone()
    }

    fun isMovesLeft(board: Array<Array<Boolean?>>) : Boolean{
        for (row in board){
            for (col in row){
                if (col == null) {
                    return true
                }
            }
        }

        return false
    }

    fun evaluate(board: Array<Array<Boolean?>>): Int {
        for (row in 0..2) {
            if (board[row][0] == board[row][1] &&
                board[row][1] == board[row][2]
            ) {
                if (board[row][0] == aiPlayer) return +10 else if (board[row][0] == opponent) return -10
            }
        }

        for (col in 0..2) {
            if (board[0][col] == board[1][col] &&
                board[1][col] == board[2][col]
            ) {
                if (board[0][col] == aiPlayer) return +10 else if (board[0][col] == opponent) return -10
            }
        }

        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (board[0][0] == aiPlayer) return +10 else if (board[0][0] == opponent) return -10
        }
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (board[0][2] == aiPlayer) return +10 else if (board[0][2] == opponent) return -10
        }

        return 0
    }

    private fun miniMax(
        boardOriginal: Array<Array<Boolean?>>,
        depth: Int, isMax: Boolean
    ): Int {
        val score = evaluate(boardOriginal)
        val board = cloneBoard(boardOriginal)

        if (score == 10) return score
        if (score == -10) return score
        if (!isMovesLeft(board)) return 0

        return if (isMax) {
            var best = -1000

            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == null) {
                        board[i][j] = aiPlayer

                        best = max(best, miniMax(board, depth + 1, !isMax))

                        board[i][j] = null
                    }
                }
            }
            best - depth
        } else {
            var best = 1000
            for (i in 0..2) {
                for (j in 0..2) {
                    if (board[i][j] == null) {
                        board[i][j] = opponent
                        best = min(best, miniMax(board, depth + 1, !isMax))
                        board[i][j] = null
                    }
                }
            }
            best + depth
        }
    }
}